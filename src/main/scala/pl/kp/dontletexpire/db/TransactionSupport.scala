package pl.kp.dontletexpire.db

import doobie.ConnectionIO
import doobie.implicits.*
import doobie.util.transactor.Transactor
import pl.kp.dontletexpire.errors.AppError
import pl.kp.dontletexpire.utils.ZIOUtils.*
import zio.interop.catz.*
import zio.{IO, Task, ZIO}

trait TransactionSupport(transactor: Transactor[Task]):
  extension [T](io: ConnectionIO[T])
    def transactional: IO[AppError.DbLevelError, T] = io.transact(transactor).asDbLevelError
  end extension
end TransactionSupport
