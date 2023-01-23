package pl.kp.dontletexpire

import doobie.util.transactor.Transactor
import pl.kp.dontletexpire.config.{AppConfig, DbConfig, HttpConfig}
import pl.kp.dontletexpire.service.{CommandService, HealthcheckService, QueryService, QueryServiceQueries, StatusService}
import pl.kp.dontletexpire.utils.NewType
import zio.{Clock, RIO, Task}

object Types:
  type Requirements = HttpConfig & DbConfig & AppConfig & HealthcheckService & Transactor[Task] & CommandService &
    QueryService & QueryServiceQueries & StatusService

  type AppTask[T] = RIO[Requirements, T]

  object Offset extends NewType[Long]
  type Offset = Offset.Type

  object Limit extends NewType[Long]
  type Limit = Limit.Type
end Types
