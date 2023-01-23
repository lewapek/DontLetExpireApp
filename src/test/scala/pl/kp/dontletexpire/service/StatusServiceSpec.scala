package pl.kp.dontletexpire.service

import pl.kp.dontletexpire.config.AppConfig
import pl.kp.dontletexpire.model.*
import pl.kp.dontletexpire.service.StatusService
import pl.kp.dontletexpire.utils.TimelinePoint
import zio.test.*
import zio.{Scope, ULayer, ZIO, ZLayer}

import scala.concurrent.duration.*

object StatusServiceSpec extends ZIOSpecDefault {

  val nearlyExpiredDuration = 3.5.days

  val deadline   = ZIO.fromEither(TimelinePoint.fromString("25.01.2023 14:15"))
  val sampleItem = deadline.map(Item(ItemId(1), StorageId(1), "milk", None, 2, _))

  override def spec: Spec[TestEnvironment with Scope, Any] = suite("StatusService")(
    test("should still be nearly expired exactly at the deadline") {
      for
        item   <- sampleItem
        time    = item.deadline
        status <- StatusService.itemStatus(time, item)
      yield assertTrue(status == ItemStatus(ItemStatusType.NearlyExpired, 0))
    },
    test("should be expired 1 millis after deadline") {
      for
        item   <- sampleItem
        time    = item.deadline.plusDuration(1.milli)
        status <- StatusService.itemStatus(time, item)
      yield assertTrue(status == ItemStatus(ItemStatusType.Expired, -1))
    },
    test("should be Ok exactly 'nearlyExpired time' before deadline") {
      for
        item   <- sampleItem
        time    = item.deadline.minusDuration(nearlyExpiredDuration)
        status <- StatusService.itemStatus(time, item)
      yield assertTrue(status == ItemStatus(ItemStatusType.Ok, (nearlyExpiredDuration).toMillis))
    },
    test("should recognize nearly expired 'nearlyExpired time' +1 millis before deadline") {
      for
        item   <- sampleItem
        time    = item.deadline.minusDuration(nearlyExpiredDuration).plusDuration(1.milli)
        status <- StatusService.itemStatus(time, item)
      yield assertTrue(status == ItemStatus(ItemStatusType.NearlyExpired, (nearlyExpiredDuration - 1.milli).toMillis))
    }
  ).provideLayerShared(layer)

  val layer: ULayer[StatusService] =
    ZLayer.succeed(AppConfig(nearlyExpired = nearlyExpiredDuration, queryBatchSize = 1)) >>> StatusService.live
}
