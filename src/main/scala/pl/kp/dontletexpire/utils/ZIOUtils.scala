package pl.kp.dontletexpire.utils

import caliban.CalibanError.ExecutionError
import pl.kp.dontletexpire.errors.AppError.{DbLevelError, ValidationError}
import pl.kp.dontletexpire.errors.{AppError, AppThrowable}
import zio.query.ZQuery
import zio.{RIO, ZIO}

object ZIOUtils:
  extension [R, E, A](zio: ZIO[R, E, A])
    def nonNegativeOrFail[E1 >: E](fe: A => E1)(using numeric: Numeric[A]): ZIO[R, E1, A] =
      zio.flatMap(a => if numeric.gteq(a, numeric.zero) then ZIO.succeed(a) else ZIO.fail(fe(a)))

    def trueIfPositive(using numeric: Numeric[A]): ZIO[R, E, Boolean] =
      zio.map(a => numeric.gt(a, numeric.zero))

    def asDbLevelError(using ev: E <:< Throwable): ZIO[R, DbLevelError, A] =
      zio.mapError(e => AppError.db(e))

    def errorAsThrowable(using ev: E <:< AppError): ZIO[R, AppThrowable, A] =
      zio.mapError(e => ev(e).toThrowable)

    def errorAsCaliban(using ev: E <:< AppError): ZIO[R, ExecutionError, A] =
      zio.mapError(e => ExecutionError(ev(e).toThrowable.getMessage))
  end extension

  extension [R, E, A](z: ZQuery[R, E, A])
    def errorAsCaliban(using ev: E <:< AppError): ZQuery[R, ExecutionError, A] =
      z.mapError(e => ExecutionError(ev(e).toThrowable.getMessage))
  end extension

end ZIOUtils
