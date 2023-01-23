package pl.kp.dontletexpire.suites

import cats.syntax.option.*
import fs2.io.net.tls.TLSParameters
import io.github.arainko.ducktape.*
import pl.kp.dontletexpire.Types.{Limit, Offset}
import pl.kp.dontletexpire.errors.AppError
import pl.kp.dontletexpire.model.{Item, ItemInput, StorageInput}
import pl.kp.dontletexpire.service.{CommandService, QueryService, QueryServiceQueries}
import pl.kp.dontletexpire.utils.TimelinePoint
import zio.ZIO
import zio.test.*
import zio.test.Assertion.*

import scala.concurrent.duration.*

object ItemSuite:
  private val addStorage1 = CommandService.addStorage(StorageInput("s1", None))
  private val addStorage2 = CommandService.addStorage(StorageInput("s2", None))
  private val addStorage3 = CommandService.addStorage(StorageInput("s3", None))

  private def sortByDeadlineAscQuantityDesc(inputs: List[ItemInput]): List[ItemInput] =
    inputs.sortBy(input => (input.deadline, -input.quantity))

  val instance = suite("Item")(
    test("should create new item") {
      for
        storage <- addStorage1
        now     <- TimelinePoint.now
        input    = ItemInput(storage.id, "cheese", "tasty one".some, 2, now.plusDuration(7.days))
        created <- CommandService.addItem(input)
      yield assertTrue(created.to[ItemInput] == input)
    },
    test("should replace item and get replaced one") {
      for
        storage1    <- addStorage1
        storage2    <- addStorage2
        now         <- TimelinePoint.now
        input        = ItemInput(storage1.id, "cheese", "tasty one".some, 2, now.plusDuration(7.days))
        created     <- CommandService.addItem(input)
        replaceInput = ItemInput(storage2.id, "cheese", "very tasty one".some, 2, now.plusDuration(4.days))
        isReplaced  <- CommandService.replaceItem(created.id, replaceInput)
        findResult  <- QueryService.item(created.id)
      yield assertTrue(isReplaced) && assert(findResult)(
        isSome(equalTo(replaceInput.into[Item].transform(Field.const(_.id, created.id))))
      )
    },
    test("should create and delete item") {
      for
        storage1   <- addStorage1
        now        <- TimelinePoint.now
        input       = ItemInput(storage1.id, "cheese", "tasty one".some, 2, now.plusDuration(7.days))
        created    <- CommandService.addItem(input)
        isDeleted  <- CommandService.deleteItem(created.id)
        findResult <- QueryService.item(created.id)
      yield assertTrue(isDeleted) && assert(findResult)(isNone)
    },
    test("should return all items sorted by deadline") {
      for
        storage1 <- addStorage1
        storage2 <- addStorage2
        storage3 <- addStorage3
        now      <- TimelinePoint.now
        inputs = List(
          ItemInput(storage1.id, "cheese", "tasty one".some, 2, now.plusDuration(7.days)),
          ItemInput(storage1.id, "cheese", "smells".some, 3, now.minusDuration(1.days)),
          ItemInput(storage2.id, "milk", None, 1, now.plusDuration(45.minutes)),
          ItemInput(storage1.id, "dumplings", None, 12, now.minusDuration(20.days)),
          ItemInput(storage3.id, "eggs", None, 6, now.minusDuration(1.days)),
          ItemInput(storage3.id, "lemon", None, 1, now.minusDuration(1.5.days)),
          ItemInput(storage1.id, "mayonnaise", None, 1, now.minusDuration(7.days))
        )
        _             <- ZIO.foreach(inputs)(CommandService.addItem)
        expectedSorted = sortByDeadlineAscQuantityDesc(inputs)
        topAll        <- QueryService.topItemsByDeadline(storages = List.empty, offset = None, limit = None)
        topAllLim5 <- QueryService.topItemsByDeadline(
          storages = List.empty,
          offset = None,
          limit = Limit(5).some
        )
        topAllOffset2Lim4 <- QueryService.topItemsByDeadline(
          storages = List.empty,
          offset = Offset(2).some,
          limit = Limit(4).some
        )
        topAllStorage1      <- QueryService.topItemsByDeadline(List(storage1.id), None, None)
        topAllStorage12     <- QueryService.topItemsByDeadline(List(storage1.id, storage2.id), None, None)
        topAllStorage3Lim1  <- QueryService.topItemsByDeadline(List(storage3.id), None, Limit(1).some)
        topStorage23Offset1 <- QueryService.topItemsByDeadline(List(storage2.id, storage3.id), Offset(1).some, None)
      yield assertTrue(topAll.map(_.to[ItemInput]) == expectedSorted) &&
        assertTrue(topAllLim5.map(_.to[ItemInput]) == expectedSorted.take(5)) &&
        assertTrue(topAllOffset2Lim4.map(_.to[ItemInput]) == expectedSorted.slice(2, 6)) &&
        assertTrue(topAllStorage1.map(_.to[ItemInput]) == expectedSorted.filter(_.storageId == storage1.id)) &&
        assertTrue(topAllStorage12.map(_.to[ItemInput]) == expectedSorted.filter(item => Set(storage1.id, storage2.id).contains(item.storageId))) &&
        assertTrue(topAllStorage3Lim1.map(_.to[ItemInput]) == expectedSorted.filter(_.storageId == storage3.id).take(1)) &&
        assertTrue(topStorage23Offset1.map(_.to[ItemInput]) == expectedSorted.filter(item => Set(storage2.id, storage3.id).contains(item.storageId)).drop(1))
    }
  )
end ItemSuite
