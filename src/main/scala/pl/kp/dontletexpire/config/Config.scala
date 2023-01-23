package pl.kp.dontletexpire.config

import cats.syntax.either.*
import pl.kp.dontletexpire.errors.{AppError, AppThrowable}
import pureconfig.*
import pureconfig.error.{CannotConvert, ConfigReaderFailures, FailureReason}
import pureconfig.generic.derivation.ProductConfigReaderDerivation
import pureconfig.generic.derivation.default.*
import zio.*
import pl.kp.dontletexpire.utils.ZIOUtils.*

import scala.concurrent.duration.{Duration, FiniteDuration}

object Config:
  private val source = ConfigSource.default

  val live: ULayer[Config] = ZLayer.fromZIO {
    (
      for
        _ <- ZIO.logDebug("Constructing config layer")
        loaded <- ZIO
          .fromEither(source.load[Config])
          .mapError(e => AppError.validation("Cannot read config: " + e.toString))
        _ <- validate(loaded)
      yield loaded
    ).catchAll(e => ZIO.die(e.toThrowable))
  }

  val httpLayer: URLayer[Config, HttpConfig] = ZLayer.fromZIO {
    ZIO.logDebug("Constructing http config layer") *> ZIO.service[Config].map(_.http)
  }

  val dbLayer: URLayer[Config, DbConfig] = ZLayer.fromZIO {
    ZIO.logDebug("Constructing db config layer") *> ZIO.service[Config].map(_.db)
  }

  val appLayer: URLayer[Config, AppConfig] = ZLayer.fromZIO {
    ZIO.logDebug("Constructing item config layer") *> ZIO.service[Config].map(_.app)
  }

  private def validate(config: Config): IO[AppError, Unit] =
    ZIO
      .fail(AppError.validation("Error validating config: nearly expired duration cannot be negative"))
      .when(config.app.nearlyExpired.toMillis < 0)
      .unit
end Config

final case class Config(
    http: HttpConfig,
    db: DbConfig,
    app: AppConfig
) derives ConfigReader

final case class HttpConfig(
    host: String,
    port: Int,
    healthcheck: HealthckeckConfig
)

final case class HealthckeckConfig(postgresTimeoutSeconds: Int)

final case class DbConfig(
    dataSource: DbDataSource,
    connectionTimeout: Int,
    minimumIdle: Int,
    maximumPoolSize: Int
):
  def user: String     = dataSource.user
  def password: String = dataSource.password
  def jdbcUrl: String =
    s"jdbc:postgresql://${dataSource.host}:${dataSource.port}/${dataSource.databaseName}"
  def driver: String = dataSource.driver
end DbConfig

final case class DbDataSource(
    host: String,
    port: Int,
    user: String,
    password: String,
    databaseName: String,
    driver: String
)

final case class AppConfig(
    nearlyExpired: FiniteDuration,
    queryBatchSize: Int
)
