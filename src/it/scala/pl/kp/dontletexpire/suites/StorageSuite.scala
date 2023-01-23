package pl.kp.dontletexpire.suites

import cats.syntax.option.*
import io.github.arainko.ducktape.*
import pl.kp.dontletexpire.errors.AppError
import pl.kp.dontletexpire.model.{Storage, StorageInput}
import pl.kp.dontletexpire.service.{CommandService, QueryService, QueryServiceQueries}
import zio.test.*
import zio.test.Assertion.*

object StorageSuite:
  val instance = suite("Storage")(
    test("should create new storage") {
      val input = StorageInput("fridge1", "abcd".some)
      for created <- CommandService.addStorage(input)
      yield assertTrue(created.to[StorageInput] == input)
    },
    test("should not create 2 storages with the same name") {
      val input = StorageInput("abc", None)
      val zio = for
        _ <- CommandService.addStorage(input)
        _ <- CommandService.addStorage(input)
      yield ()
      assertZIO(zio.exit)(fails(isSubtype[AppError.DbLevelError](anything)))
    },
    test("should replace storage and get replaced one") {
      val input        = StorageInput("yet another", "abcd".some)
      val replaceInput = StorageInput("taka sytuacja", None)
      for
        created    <- CommandService.addStorage(input)
        isReplaced <- CommandService.replaceStorage(created.id, replaceInput)
        replaced   <- QueryServiceQueries.storage(created.id).run
      yield assertTrue(isReplaced) && assert(replaced)(
        isSome(equalTo(replaceInput.into[Storage].transform(Field.const(_.id, created.id))))
      )
    },
    test("should create and delete storage") {
      val input = StorageInput("yet another", "abcd".some)
      for
        created    <- CommandService.addStorage(input)
        isDeleted  <- CommandService.deleteStorage(created.id)
        findResult <- QueryServiceQueries.storage(created.id).run
      yield assertTrue(isDeleted) && assert(findResult)(isNone)
    }
  )
end StorageSuite
