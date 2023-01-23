package pl.kp.dontletexpire.service

import doobie.*
import doobie.implicits.*
import doobie.postgres.implicits.*
import io.circe.{Decoder, Encoder}
import pl.kp.dontletexpire.config.{HealthckeckConfig, HttpConfig}
import pl.kp.dontletexpire.errors.AppError
import zio.*
import zio.DurationSyntax.*
import zio.interop.catz.*

import scala.language.postfixOps

class HealthcheckService(
    healthckeckConfig: HealthckeckConfig,
    transactor: Transactor[Task]
):
  def status: IO[AppError, HealthcheckStatusSummary] =
    for postgres <-
        fr"SELECT 1"
          .query[Int]
          .option
          .transact(transactor)
          .timeoutTo(HealthcheckStatus.Timeout)(identity)(
            healthckeckConfig.postgresTimeoutSeconds seconds
          )
          .map {
            case Some(1) => HealthcheckStatus.Ok
            case _       => HealthcheckStatus.NotOk
          }
          .mapError(AppError.db)
    yield HealthcheckStatusSummary(postgres)

object HealthcheckService:
  def status: ZIO[HealthcheckService, AppError, HealthcheckStatusSummary] =
    ZIO.serviceWithZIO[HealthcheckService](_.status)

  val live = ZLayer.fromZIO {
    for
      _          <- ZIO.logDebug("Constructing healthcheck layer")
      httpConfig <- ZIO.service[HttpConfig]
      transactor <- ZIO.service[Transactor[Task]]
    yield HealthcheckService(httpConfig.healthcheck, transactor)
  }

case class HealthcheckStatusSummary(postgres: HealthcheckStatus):
  def isOk: Boolean = postgres == HealthcheckStatus.Ok

enum HealthcheckStatus:
  case Ok, NotOk, Timeout

object HealthcheckStatus:
  given encoder: Encoder[HealthcheckStatus] =
    Encoder.encodeString.contramap(_.toString)
end HealthcheckStatus
