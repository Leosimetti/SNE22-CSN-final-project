ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

val http4sVersion = "0.23.16"
val rabbitMqVersion = "5.0.0"
val tapirVersion = "1.1.3"

lazy val app = (project in file("app"))
  .settings(
    javaOptions := Seq("-J-Dcom.linecorp.armeria.useOpenSsl=false"),
    name := "CSN-final-project",
    scalacOptions += "-Wnonunit-statement",
    libraryDependencies ++= Seq(
      "com.github.fd4s" %% "fs2-kafka" % "3.0.0-M8",
      "io.grpc" % "grpc-netty-shaded" % scalapb.compiler.Version.grpcJavaVersion,
      "org.slf4j" % "slf4j-api" % "2.0.3",
      "ch.qos.logback" % "logback-classic" % "1.4.4" % Runtime,
      "dev.profunktor" %% "fs2-rabbit" % rabbitMqVersion,
      "dev.profunktor" %% "fs2-rabbit-circe" % rabbitMqVersion,
      "io.circe" %% "circe-generic" % "0.14.3",
      "org.latestbit" %% "circe-tagged-adt-codec" % "0.10.0",
      "com.softwaremill.sttp.tapir" %% "tapir-core" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % tapirVersion,
      "org.http4s" %% "http4s-ember-server" % http4sVersion,
      "org.http4s" %% "http4s-ember-client" % http4sVersion,
      "com.linecorp.armeria" %% "armeria-scalapb" % "1.20.1",
    ),
    Compile / PB.protoSources := Seq(file("protobuf")),
    scalapbCodeGeneratorOptions ++= Seq(
      CodeGeneratorOption.FlatPackage
    ),
  )
  .enablePlugins(Fs2Grpc)
