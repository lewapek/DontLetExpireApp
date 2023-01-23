import sbt._

object Dependencies {
  val zio = Seq(
    "dev.zio" %% "zio-interop-cats"  % "3.3.0",
    "dev.zio" %% "zio"               % "2.0.5",
    "dev.zio" %% "zio-test"          % "2.0.5" % "it,test",
    "dev.zio" %% "zio-test-sbt"      % "2.0.5" % "it,test",
    "dev.zio" %% "zio-logging"       % "2.1.7",
    "dev.zio" %% "zio-logging-slf4j" % "2.1.7"
  )

  val logback = Seq(
    "ch.qos.logback" % "logback-classic" % "1.2.11"
  )

  val caliban = Seq(
    "com.github.ghostdogpr" %% "caliban"        % "2.0.2",
    "com.github.ghostdogpr" %% "caliban-http4s" % "2.0.2"
  )

  val pureconfig = Seq(
    "com.github.pureconfig" %% "pureconfig-core" % "0.17.1"
  )

  val circe = Seq(
    "io.circe" %% "circe-core"    % "0.14.2",
    "io.circe" %% "circe-generic" % "0.14.2",
    "io.circe" %% "circe-parser"  % "0.14.2"
  )

  val http4s = Seq(
    "org.http4s" %% "http4s-core"         % "0.23.10",
    "org.http4s" %% "http4s-dsl"          % "0.23.10",
    "org.http4s" %% "http4s-blaze-server" % "0.23.10",
    "org.http4s" %% "http4s-circe"        % "0.23.10"
  )

  val doobie = Seq(
    "org.tpolecat" %% "doobie-core"     % "1.0.0-RC2",
    "org.tpolecat" %% "doobie-postgres" % "1.0.0-RC2",
    "org.tpolecat" %% "doobie-refined"  % "1.0.0-RC2",
    "org.tpolecat" %% "doobie-hikari"   % "1.0.0-RC2"
  )

  val flyway = Seq(
    "org.flywaydb" % "flyway-core" % "9.11.0"
  )

  // chimney like scala3 similar case classes transformations
  val ducktape = Seq(
    "io.github.arainko" %% "ducktape" % "0.1.2"
  )
}
