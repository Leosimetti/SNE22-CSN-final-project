import cats.effect.{ExitCode, IO, IOApp}
import dev.profunktor.fs2rabbit.config.Fs2RabbitConfig
import dev.profunktor.fs2rabbit.interpreter.RabbitClient
import fs2.grpc.syntax.all._
import fs2.kafka.{AutoOffsetReset, ConsumerSettings}
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder
import kafka.ResultRepository
import kafka.implicits._
import rabbitmq.TaskRepository
import shared.types.ProblemId
import shared.{AppFs2Grpc, Result}
import web.ApplicationServer

import scala.concurrent.duration._

object Main extends IOApp {

  val consumerSettings: ConsumerSettings[IO, ProblemId, Result] =
    ConsumerSettings[IO, ProblemId, Result]
      .withAutoOffsetReset(AutoOffsetReset.Earliest)
      .withBootstrapServers("localhost:9092")
      .withGroupId("group")

  val rabbitSettings: Fs2RabbitConfig = Fs2RabbitConfig(
    host = "localhost",
    port = 5672,
    virtualHost = "/",
    connectionTimeout = 3.minutes,
    ssl = false,
    username = Some("guest"),
    password = Some("guest"),
    requeueOnNack = false,
    requeueOnReject = false,
    internalQueueSize = Some(500),
  )

  override def run(args: List[String]): IO[ExitCode] = {
    val app = for {
      rabbitClient <- RabbitClient.default[IO](rabbitSettings).resource
      submitRepo = TaskRepository[IO](rabbitClient)
      resultRepo = ResultRepository[IO](consumerSettings)
      appService <-
        AppFs2Grpc.bindServiceResource(ApplicationServer(submitRepo, resultRepo))
    } yield appService

    app.use(service =>
      NettyServerBuilder
        .forPort(9999)
        .addService(service)
        .resource[IO]
        .evalMap(server => IO.delay(server.start()))
        .useForever
    )
  }
}
