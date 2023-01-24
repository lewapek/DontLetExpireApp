package pl.kp.dontletexpire.service

import pl.kp.dontletexpire.config.AppConfig
import pl.kp.dontletexpire.errors.AppError.ValidationError
import pl.kp.dontletexpire.model.{Item, ItemStatus, ItemStatusType}
import pl.kp.dontletexpire.utils.TimelinePoint
import zio.{UIO, URIO, ZIO, ZLayer}

import java.time.Instant
import scala.concurrent.duration.*

class StatusService(nearlyExpiredDuration: FiniteDuration):
  def itemStatus(time: TimelinePoint, item: Item): ItemStatus =
    val statusType =
      if time > item.deadline then ItemStatusType.Expired
      else if time.plusDuration(nearlyExpiredDuration) > item.deadline then ItemStatusType.NearlyExpired
      else ItemStatusType.Ok
    ItemStatus(statusType, (item.deadline - time).toMillis)
  end itemStatus
end StatusService

object StatusService:
  val live = ZLayer.fromZIO(
    for config <- ZIO.service[AppConfig]
    yield StatusService(config.nearlyExpired)
  )
  def itemStatus(time: TimelinePoint, item: Item): URIO[StatusService, ItemStatus] =
    ZIO.serviceWith[StatusService](_.itemStatus(time, item))
end StatusService
