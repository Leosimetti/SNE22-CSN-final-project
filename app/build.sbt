ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

val http4sVersion = "0.23.16"
val rabbitMqVersion = "5.0.0"

lazy val root = (project in file("."))
  .settings(
    name := "CSN-final-project",
    scalacOptions += "-Wnonunit-statement",
  )

libraryDependencies ++= Seq(
  "com.github.fd4s" %% "fs2-kafka" % "3.0.0-M8",
  "org.slf4j" % "slf4j-simple" % "2.0.3",
  "com.softwaremill.sttp.tapir" %% "tapir-core" % "1.1.2",
  "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % "1.1.2",
  "org.http4s" %% "http4s-ember-server" % http4sVersion,
  "org.http4s" %% "http4s-ember-client" % http4sVersion,
  "dev.profunktor" %% "fs2-rabbit" % rabbitMqVersion,
  "dev.profunktor" %% "fs2-rabbit-circe" % rabbitMqVersion,
  "io.circe" %% "circe-generic" % "0.14.3",
)
