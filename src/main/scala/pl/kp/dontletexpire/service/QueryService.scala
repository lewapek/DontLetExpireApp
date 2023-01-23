package pl.kp.dontletexpire.service

import cats.data.NonEmptyList
import doobie.util.transactor.Transactor
import pl.kp.dontletexpire.Types.{Limit, Offset}
import pl.kp.dontletexpire.config.AppConfig
import pl.kp.dontletexpire.db.TransactionSupport
import pl.kp.dontletexpire.db.repo.{ItemRepo, StorageRepo}
import pl.kp.dontletexpire.errors.AppError
import pl.kp.dontletexpire.model
import pl.kp.dontletexpire.model.*
import pl.kp.dontletexpire.model.view.ItemView
import pl.kp.dontletexpire.utils.TimelinePoint
import zio.query.{DataSource, Query, Request, ZQuery}
import zio.{Chunk, Clock, IO, NonEmptyChunk, Task, ZIO, ZLayer}

import java.time.Instant

class QueryService(transactor: Transactor[Task]) extends TransactionSupport(transactor):
  def storage(id: StorageId): IO[AppError, Option[Storage]] =
    StorageRepo.select(id).transactional

  def storages(ids: NonEmptyList[StorageId]): IO[AppError, List[Storage]] =
    StorageRepo.select(ids).transactional

  def topItemsByDeadline(
      storages: List[StorageId],
      offset: Option[Offset],
      limit: Option[Limit]
  ): IO[AppError, List[Item]] =
    ItemRepo.topItemsByDeadline(storages, offset, limit).transactional

  def item(id: ItemId): IO[AppError, Option[Item]] =
    ItemRepo.select(id).transactional

end QueryService

object QueryService:
  val live = ZLayer.fromZIO(
    for transactor <- ZIO.service[Transactor[Task]]
    yield QueryService(transactor)
  )

  def topItemsByDeadline(
      storages: List[StorageId],
      offset: Option[Offset],
      limit: Option[Limit]
  ): ZIO[QueryService, AppError, List[Item]] =
    ZIO.serviceWithZIO[QueryService](_.topItemsByDeadline(storages, offset, limit))

  def item(id: ItemId): ZIO[QueryService, AppError, Option[Item]] =
    ZIO.serviceWithZIO[QueryService](_.item(id))
end QueryService
