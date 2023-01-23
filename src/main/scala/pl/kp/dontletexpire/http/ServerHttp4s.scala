package pl.kp.dontletexpire.http

import caliban.{CalibanError, GraphQLInterpreter, Http4sAdapter}
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.Router
import org.http4s.server.middleware.CORS
import pl.kp.dontletexpire.Types.{AppTask, Requirements}
import pl.kp.dontletexpire.config.HttpConfig
import zio.ZIO
import zio.interop.catz.*

object ServerHttp4s:
  val run: AppTask[Nothing] =
    for
      httpConfig         <- ZIO.service[HttpConfig]
      graphqlInterpreter <- GraphqlSchema.api.interpreter
      server             <- runServer(httpConfig, graphqlInterpreter)
    yield server

  private def runServer(
      httpConfig: HttpConfig,
      graphQLInterpreter: GraphQLInterpreter[Requirements, CalibanError]
  ): AppTask[Nothing] =
    BlazeServerBuilder[AppTask].withoutBanner
      .bindHttp(httpConfig.port, httpConfig.host)
      .withHttpApp(
        CORS
          .policy(
            Router[AppTask](
              "/healthcheck" -> HealthcheckCtrl.routes,
              "/graphql"     -> Http4sAdapter.makeHttpService(graphQLInterpreter)
            ).orNotFound
          )
      )
      .resource
      .useForever
  end runServer
end ServerHttp4s
