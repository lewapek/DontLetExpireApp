package pl.kp.dontletexpire

import cats.syntax.foldable.*
import doobie.Transactor
import doobie.implicits.*
import pl.kp.dontletexpire.Types.Requirements
import pl.kp.dontletexpire.config.Config
import pl.kp.dontletexpire.db.PostgresDatabase
import pl.kp.dontletexpire.db.repo.{ItemRepo, StorageRepo}
import pl.kp.dontletexpire.service.*
import pl.kp.dontletexpire.suites.{ItemSuite, StorageSuite}
import zio.interop.catz.*
import zio.test.*
import zio.test.TestAspect.*
import zio.{Scope, Task, URIO, ZIO, ZLayer}

object MainIT extends ZIOSpecDefault:
  val layer = ZLayer.make[Requirements](
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

  private val tables = List(
    StorageRepo.tableFr,
    ItemRepo.tableFr
  ).intercalate(fr",")

  private def truncateAll: URIO[Transactor[Task], Unit] =
    for
      transactor <- ZIO.service[Transactor[Task]]
      _          <- fr"TRUNCATE $tables CASCADE".update.run.transact(transactor).orDie
    yield ()

  override def spec: Spec[TestEnvironment with Scope, Any] = {
    suite("DontLetExpire integrations tests")(
      StorageSuite.instance,
      ItemSuite.instance
    ) @@ sequential @@ before(truncateAll) @@ afterAll(truncateAll)
  }.provideLayerShared(layer)
end MainIT
