package pl.kp.dontletexpire

import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.Router
import org.http4s.server.middleware.CORS
import pl.kp.dontletexpire.Types.{AppTask, Requirements}
import pl.kp.dontletexpire.config.{Config, HealthckeckConfig, HttpConfig}
import pl.kp.dontletexpire.db.PostgresDatabase
import pl.kp.dontletexpire.errors.AppError
import pl.kp.dontletexpire.errors.AppError.DbLevelError
import pl.kp.dontletexpire.http.{HealthcheckCtrl, ServerHttp4s}
import pl.kp.dontletexpire.service.{CommandService, HealthcheckService, QueryService, QueryServiceQueries, StatusService}
import zio.interop.catz.*
import zio.logging.LogFormat
import zio.logging.backend.SLF4J
import zio.{Clock, LogLevel, RIO, Runtime, Scope, ULayer, ZIO, ZIOApp, ZIOAppArgs, ZIOAppDefault, ZLayer}

object Main extends ZIOAppDefault:

  val appLayer: ULayer[Requirements] = ZLayer.make[Requirements](
    Scope.default,
    Config.live,
    Config.httpLayer,
    Config.dbLayer,
    Config.appLayer,
    PostgresDatabase.transactorLive,
    HealthcheckService.live,
    CommandService.live,
    QueryService.live,
    QueryServiceQueries.live,
    StatusService.live
  )

  override val bootstrap =
    zio.Runtime.removeDefaultLoggers >>> SLF4J.slf4j(LogFormat.line + LogFormat.cause)

  override def run: ZIO[ZIOAppArgs with Scope, Any, Any] =
    ZIO.logInfo("DontLetIgnore app start") *> ServerHttp4s.run
      .provideLayer(appLayer)
      .tapError(error => ZIO.logError(s"Error $error"))
      .tapDefect(throwable => ZIO.logError(s"Defect: $throwable"))
      .exitCode
  end run
end Main
