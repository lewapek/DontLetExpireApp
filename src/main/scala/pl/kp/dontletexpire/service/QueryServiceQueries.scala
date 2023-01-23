package pl.kp.dontletexpire.service

import cats.data.{NonEmptyList, NonEmptySet}
import pl.kp.dontletexpire.Types.{Limit, Offset}
import pl.kp.dontletexpire.config.AppConfig
import pl.kp.dontletexpire.errors.AppError
import pl.kp.dontletexpire.model.view.{ItemView, ItemsView}
import pl.kp.dontletexpire.model.{Storage, StorageId}
import pl.kp.dontletexpire.service.QueryService
import pl.kp.dontletexpire.utils.TimelinePoint
import zio.query.{DataSource, Query, Request, ZQuery}
import zio.{Chunk, ZIO, ZLayer}

class QueryServiceQueries(appConfig: AppConfig, queryService: QueryService):
  private final case class GetStorage(id: StorageId) extends Request[AppError, Option[Storage]]
  private val storageDataSource: DataSource[Any, GetStorage] =
    DataSource
      .fromFunctionBatchedZIO[Any, AppError, GetStorage, Option[Storage]]("StorageById") { chunk =>
        NonEmptyList.fromList(chunk.toList).fold(ZIO.succeed(Chunk.empty)) { nonEmpty =>
          for
            found      <- queryService.storages(nonEmpty.map(_.id))
            idToStorage = found.map(storage => storage.id -> storage).toMap
          yield chunk.map(request => idToStorage.get(request.id))
        }
      }
      .batchN(appConfig.queryBatchSize)
  end storageDataSource

  def storage(id: StorageId): Query[AppError, Option[Storage]] =
    ZQuery.fromRequest(GetStorage(id))(storageDataSource)

  def topItemsByDeadline(
      storages: List[StorageId],
      offset: Option[Offset],
      limit: Option[Limit]
  ): Query[AppError, ItemsView] =
    for
      items <- ZQuery.fromZIO(queryService.topItemsByDeadline(storages, offset, limit))
      now   <- ZQuery.fromZIO(TimelinePoint.now)
      views <- ZQuery.foreach(items)(item => ZQuery.succeed(ItemView.from(now, item)))
    yield ItemsView(views.size, now, views)
  end topItemsByDeadline
end QueryServiceQueries

object QueryServiceQueries:
  val live = ZLayer.fromZIO(
    for
      appConfig    <- ZIO.service[AppConfig]
      queryService <- ZIO.service[QueryService]
    yield QueryServiceQueries(appConfig, queryService)
  )

  def storage(id: StorageId): ZQuery[QueryServiceQueries, AppError, Option[Storage]] =
    ZQuery.serviceWithQuery[QueryServiceQueries](_.storage(id))

  def topItemsByDeadline(
      storages: List[StorageId],
      offset: Option[Offset],
      limit: Option[Limit]
  ): ZQuery[QueryServiceQueries, AppError, ItemsView] =
    ZQuery.serviceWithQuery[QueryServiceQueries](_.topItemsByDeadline(storages, offset, limit))
end QueryServiceQueries
