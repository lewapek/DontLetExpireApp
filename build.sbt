ThisBuild / version := IO.read(file("version"))

ThisBuild / scalaVersion := "3.2.2"

lazy val root = (project in file("."))
  .configs(IntegrationTest)
  .settings(
    name := "DontLetExpire",
    libraryDependencies ++= Dependencies.zio ++
      Dependencies.caliban ++
      Dependencies.logback ++
      Dependencies.pureconfig ++
      Dependencies.http4s ++
      Dependencies.circe ++
      Dependencies.doobie ++
      Dependencies.flyway ++
      Dependencies.ducktape,
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
    scalacOptions ++= Seq(
      "-Ykind-projector:underscores",
      "-Xmax-inlines:64"
    ),
    Defaults.itSettings,
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", _*) => MergeStrategy.discard
      case _                        => MergeStrategy.first
    }
  )
