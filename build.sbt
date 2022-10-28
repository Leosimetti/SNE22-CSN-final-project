ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

val http4sVersion = "0.23.16"
val rabbitMqVersion = "5.0.0"
val tapirVersion = "1.1.3"

lazy val app = (project in file("app"))
  .settings(
    name := "CSN-final-project",
    scalacOptions += "-Wnonunit-statement",
    libraryDependencies ++= Seq(
      "com.github.pureconfig" %% "pureconfig" % "0.17.1",
      "com.github.fd4s" %% "fs2-kafka" % "3.0.0-M8",
      "io.grpc" % "grpc-netty-shaded" % scalapb.compiler.Version.grpcJavaVersion,
      "org.slf4j" % "slf4j-api" % "2.0.3",
      "ch.qos.logback" % "logback-classic" % "1.4.4" % Runtime,
      "dev.profunktor" %% "fs2-rabbit" % rabbitMqVersion,
    ),
    mainClass := Some("Main")
  )
  .enablePlugins(Fs2Grpc)
  .settings(
    Compile / PB.protoSources := Seq(file("protobuf")),
    scalapbCodeGeneratorOptions ++= Seq(
      CodeGeneratorOption.FlatPackage
    ),
  )
  .enablePlugins(DockerPlugin)
  .enablePlugins(JavaAppPackaging)
  .settings(
    dockerExposedPorts := Seq(9999),
    dockerBaseImage := "eclipse-temurin:17"
  )
