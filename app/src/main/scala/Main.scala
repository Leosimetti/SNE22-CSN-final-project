import cats.effect.{IO, Resource, ResourceApp}
import cats.syntax.all._
import dev.profunktor.fs2rabbit.config.Fs2RabbitConfig
import dev.profunktor.fs2rabbit.interpreter.RabbitClient
import fs2.grpc.syntax.all._
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder
import kafka.{ProblemRepository, ResultRepository}
import pureconfig.ConfigSource
import rabbitmq.TaskRepository
import shared.{AppFs2Grpc, Config}
import web.ApplicationServer

import scala.concurrent.duration._

object Main extends ResourceApp.Simple {

  override def run: Resource[IO, Unit] = {
    for {
      config <- Resource.eval {
        IO.delay(
          ConfigSource.default.load[Config]
        ).flatMap(res => IO.fromEither(res.leftMap(e => new Exception(e.prettyPrint()))))
      }
      rabbitSettings = Fs2RabbitConfig(
        host = config.rabbitMq.host,
        port = config.rabbitMq.port,
        virtualHost = "/",
        connectionTimeout = 3.minutes,
        ssl = false,
        username = config.rabbitMq.username.some,
        password = config.rabbitMq.password.some,
        requeueOnNack = false,
        requeueOnReject = false,
        internalQueueSize = Some(500),
      )
      rabbitClient <- RabbitClient.default[IO](rabbitSettings).resource
      resultRepo = ResultRepository[IO](config)
      submitRepo = TaskRepository[IO](config, rabbitClient)
      problemRepo = ProblemRepository[IO](config)
      appService <-
        AppFs2Grpc.bindServiceResource(ApplicationServer(problemRepo, submitRepo, resultRepo))
      _ <-
        Resource.eval(
          NettyServerBuilder
            .forPort(9999)
            .addService(appService)
            .resource[IO]
            .evalMap(srv => IO.delay(srv.start()))
            .useForever
        )
    } yield ()
  }
}
