package pl.kp.dontletexpire.http

import io.circe.generic.auto.*
import io.circe.syntax.*
import org.http4s.HttpRoutes
import org.http4s.circe.*
import org.http4s.dsl.Http4sDsl
import pl.kp.dontletexpire.service.HealthcheckService
import zio.*
import zio.interop.catz.*
import pl.kp.dontletexpire.utils.ZIOUtils.*

object HealthcheckCtrl:
  def routes[R <: HealthcheckService]: HttpRoutes[RIO[R, *]] =
    type T[A] = RIO[R, A]
    val dsl = Http4sDsl[T]
    import dsl.*
    HttpRoutes.of[T] { case GET -> Root =>
        for
          status <- HealthcheckService.status.errorAsThrowable
          response <-
            if status.isOk then Ok(status.asJson)
            else InternalServerError(status.asJson)
        yield response
    }
  end routes
end HealthcheckCtrl
