package pl.kp.dontletexpire.errors

import cats.syntax.option.*
import pl.kp.dontletexpire.errors.AppError.DbLevelError

sealed trait AppError:
  def toThrowable: AppThrowable
end AppError

object AppError:
  def db(msg: String, cause: Option[Throwable] = None): DbLevelError =
    DbLevelError(msg, cause)
  def db(throwable: Throwable): DbLevelError =
    DbLevelError(s"Db level error: ${throwable.getMessage}", throwable.some)
  def validation(msg: String): ValidationError =
    ValidationError(msg)
  def result(msg: String): ValidationError =
    ValidationError(msg)

  final case class DbLevelError(msg: String, cause: Option[Throwable]) extends AppError:
    override def toThrowable: AppThrowable = AppThrowable.create(msg, cause)
  final case class ValidationError(msg: String) extends AppError:
    override def toThrowable: AppThrowable = AppThrowable.create(msg)
  final case class ResultError(msg: String) extends AppError:
    override def toThrowable: AppThrowable = AppThrowable.create(msg)
end AppError
