package pl.kp.dontletexpire.http

import caliban.CalibanError.ExecutionError
import caliban.GraphQL.graphQL
import caliban.schema.{GenericSchema, Schema, SchemaDerivation}
import caliban.{CalibanError, RootResolver}
import pl.kp.dontletexpire.Types.{Limit, Offset, Requirements}
import pl.kp.dontletexpire.config.HttpConfig
import pl.kp.dontletexpire.db.repo.StorageColumns
import pl.kp.dontletexpire.errors.AppError.DbLevelError
import pl.kp.dontletexpire.errors.{AppError, AppThrowable}
import pl.kp.dontletexpire.model.*
import pl.kp.dontletexpire.model.view.{ItemView, ItemsView}
import pl.kp.dontletexpire.service.{CommandService, HealthcheckService, QueryService, QueryServiceQueries}
import pl.kp.dontletexpire.utils.ZIOUtils.*
import zio.query.ZQuery
import zio.{IO, ZIO}

import java.time.*
import java.time.format.DateTimeFormatter
import scala.util.Try

object GraphqlSchema extends GenericSchema[Requirements]:
  type CalibanIO[R, A]    = ZIO[R, CalibanError, A]
  type CalibanQuery[R, A] = ZQuery[R, CalibanError, A]

  case class IdArgs[Id](id: Id)
  case class IdInputArgs[Id, Input](id: Id, input: Input)
  case class TopItemsArgs(storages: List[StorageId], offset: Option[Offset], limit: Option[Limit])

  case class Queries(
      storageById: IdArgs[StorageId] => CalibanQuery[QueryServiceQueries, Option[Storage]],
      topItemsByDeadline: TopItemsArgs => CalibanQuery[QueryServiceQueries, ItemsView]
  )
  case class Mutations(
      addStorage: StorageInput => CalibanIO[CommandService, Storage],
      replaceStorage: IdInputArgs[StorageId, StorageInput] => CalibanIO[CommandService, Boolean],
      deleteStorage: IdArgs[StorageId] => CalibanIO[CommandService, Boolean],
      addItem: ItemInput => CalibanIO[CommandService, Item],
      replaceItem: IdInputArgs[ItemId, ItemInput] => CalibanIO[CommandService, Boolean],
      deleteItem: IdArgs[ItemId] => CalibanIO[CommandService, Boolean],
      incrementItemQuantity: IdInputArgs[ItemId, Int] => CalibanIO[CommandService, Boolean]
  )

  val queries = Queries(
    args => QueryServiceQueries.storage(args.id).errorAsCaliban,
    args => QueryServiceQueries.topItemsByDeadline(args.storages, args.offset, args.limit).errorAsCaliban
  )
  val mutations = Mutations(
    CommandService.addStorage(_).errorAsCaliban,
    args => CommandService.replaceStorage(args.id, args.input).errorAsCaliban,
    args => CommandService.deleteStorage(args.id).errorAsCaliban,
    CommandService.addItem(_).errorAsCaliban,
    args => CommandService.replaceItem(args.id, args.input).errorAsCaliban,
    args => CommandService.deleteItem(args.id).errorAsCaliban,
    args => CommandService.incrementItemQuantity(args.id, args.input).errorAsCaliban
  )

  val api = graphQL[Requirements, Queries, Mutations, Unit](RootResolver(queries, mutations))

  given Schema[Any, ItemStatus] = Schema.gen
end GraphqlSchema
