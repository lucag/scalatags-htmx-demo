val sbtSoftwareMillVersion = "2.0.14"
val scalaFmtVersion        = "2.5.1"
val sbtRevolverVersion     = "0.10.0"
val sbtUpdatesversion      = "0.6.4"

addSbtPlugin("org.scalameta"          % "sbt-scalafmt"            % scalaFmtVersion)
addSbtPlugin(
  "com.softwaremill.sbt-softwaremill" % "sbt-softwaremill-common" % sbtSoftwareMillVersion
)
addSbtPlugin("io.spray"               % "sbt-revolver"            % sbtRevolverVersion)
addSbtPlugin("com.timushev.sbt"       % "sbt-updates"             % sbtUpdatesversion)
