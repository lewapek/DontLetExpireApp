package pl.kp.dontletexpire.service

import cats.syntax.traverse.*
import doobie.Transactor
import doobie.implicits.*
import io.github.arainko.ducktape.*
import pl.kp.dontletexpire.db.TransactionSupport
import pl.kp.dontletexpire.db.repo.{ItemColumns, ItemRepo, StorageColumns, StorageRepo}
import pl.kp.dontletexpire.errors.AppError
import pl.kp.dontletexpire.errors.AppError.{ResultError, ValidationError}
import pl.kp.dontletexpire.model.*
import pl.kp.dontletexpire.utils.ZIOUtils.*
import zio.{IO, Task, ZIO, ZLayer}

class CommandService(transactor: Transactor[Task]) extends TransactionSupport(transactor):
  def addStorage(input: StorageInput): IO[AppError, Storage] =
    StorageRepo.insert(input).transactional

  def replaceStorage(id: StorageId, input: StorageInput): IO[AppError, Boolean] =
    StorageRepo.replace(id, input).transactional.trueIfPositive

  def deleteStorage(id: StorageId): IO[AppError, Boolean] =
    StorageRepo.delete(id).transactional.trueIfPositive

  def addItem(input: ItemInput): IO[AppError, Item] =
    validate(input) *> ItemRepo.insert(input).transactional

  def replaceItem(id: ItemId, input: ItemInput): IO[AppError, Boolean] =
    validate(input) *> ItemRepo.replace(id, input).transactional.trueIfPositive

  def deleteItem(id: ItemId): IO[AppError, Boolean] =
    ItemRepo.delete(id).transactional.trueIfPositive

  def incrementItemQuantity(id: ItemId, n: Int): IO[AppError, Boolean] =
    val io =
      for
        maybeItem <- ItemRepo.select(id)
        nAffected <- maybeItem.traverse { item =>
          val newQuantity = item.quantity + n
          if newQuantity > 0 then ItemRepo.updateQuantity(id, newQuantity)
          else ItemRepo.delete(id)
        }
      yield nAffected
    io.transactional.someOrFail(AppError.result("Item to change quantity not found")).trueIfPositive
  end incrementItemQuantity

  private def validate(itemInput: ItemInput): IO[AppError, Unit] =
    ZIO.fail(AppError.validation("Item quantity can't be negative")).when(itemInput.quantity < 0).unit
end CommandService

object CommandService:
  val live = ZLayer.fromZIO(
    for transactor <- ZIO.service[Transactor[Task]]
    yield CommandService(transactor)
  )

  def addStorage(input: StorageInput): ZIO[CommandService, AppError, Storage] =
    ZIO.serviceWithZIO[CommandService](_.addStorage(input))

  def replaceStorage(id: StorageId, input: StorageInput): ZIO[CommandService, AppError, Boolean] =
    ZIO.serviceWithZIO[CommandService](_.replaceStorage(id, input))

  def deleteStorage(id: StorageId): ZIO[CommandService, AppError, Boolean] =
    ZIO.serviceWithZIO[CommandService](_.deleteStorage(id))

  def addItem(input: ItemInput): ZIO[CommandService, AppError, Item] =
    ZIO.serviceWithZIO[CommandService](_.addItem(input))

  def replaceItem(id: ItemId, input: ItemInput): ZIO[CommandService, AppError, Boolean] =
    ZIO.serviceWithZIO[CommandService](_.replaceItem(id, input))

  def deleteItem(id: ItemId): ZIO[CommandService, AppError, Boolean] =
    ZIO.serviceWithZIO[CommandService](_.deleteItem(id))

  def incrementItemQuantity(id: ItemId, n: Int): ZIO[CommandService, AppError, Boolean] =
    ZIO.serviceWithZIO[CommandService](_.incrementItemQuantity(id, n))

end CommandService
