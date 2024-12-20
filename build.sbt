val currentScalaVersion   = "3.6.2"
val emailValidatorVersion = "1.7"
val flywayVersion         = "10.21.0"
val hikariVersion         = "6.2.1"
val jwtVersion            = "4.4.0"
val logbackVersion        = "1.5.12"
val password4jVersion     = "1.7.3"
val quillVersion          = "4.8.6"
val sqliteVersion         = "3.47.0.0"
val sttpZioJsonVersion    = "3.10.1"
val zioConfigVersion      = "4.0.2"
val zioLoggingVersion     = "2.4.0"
val zioTestVersion        = "2.1.12"

val config = Seq(
  "dev.zio" %% "zio-config-typesafe" % zioConfigVersion,
  "dev.zio" %% "zio-config-magnolia" % zioConfigVersion
)

val db = Seq(
  "com.zaxxer"   % "HikariCP"       % hikariVersion,
  "io.getquill" %% "quill-jdbc-zio" % quillVersion,
  "org.flywaydb" % "flyway-core"    % flywayVersion,
  "org.xerial"   % "sqlite-jdbc"    % sqliteVersion
)

val html = Seq(
  "com.lihaoyi" %% "scalatags" % "0.13.1"
)

val http  = Seq(
  "dev.zio" %% "zio-http" % "3.0.1"
)
val tests = Seq(
  "dev.zio"                       %% "zio-logging"       % zioLoggingVersion,
  "dev.zio"                       %% "zio-logging-slf4j" % zioLoggingVersion,
  "dev.zio"                       %% "zio-test"          % zioTestVersion     % Test,
  "dev.zio"                       %% "zio-test-sbt"      % zioTestVersion     % Test,
  "com.softwaremill.sttp.client3" %% "zio-json"          % sttpZioJsonVersion % Test,
  "ch.qos.logback"                 % "logback-classic"   % logbackVersion
)

lazy val `zio-http-htmx` =
  (project in file("."))
    .settings(
      Seq(
        name                 := "zio-http-htmx",
        version              := "0.1.2-SNAPSHOT",
        organization         := "com.rockthejvm",
        scalaVersion         := currentScalaVersion,
        Test / fork          := true,
        scalacOptions       --= Seq("-Ykind-projector"),
        scalacOptions       ++= Seq(
          "-Xmax-inlines",
          "64",
          "-Xkind-projector"
        ),
        libraryDependencies ++= config ++ db ++ tests ++ html ++ http,
        testFrameworks       := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))
      )
    )
