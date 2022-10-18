import cats.effect.{ExitCode, IO, IOApp}
import com.comcast.ip4s._
import fs2.kafka.{AutoOffsetReset, ConsumerSettings}
import kafka.ResultRepository
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.middleware.CORS
import shared.ResultKafkaView
import shared.types.ProblemId
import web.ApplicationServer

object Main extends IOApp {

  val consumerSettings: ConsumerSettings[IO, ProblemId, ResultKafkaView] =
    ConsumerSettings[IO, ProblemId, ResultKafkaView]
      .withAutoOffsetReset(AutoOffsetReset.Earliest)
      .withBootstrapServers("localhost:9092")
      .withGroupId("group")

  val resultRepository: ResultRepository[IO] = ResultRepository[IO](consumerSettings)
  val appServer: ApplicationServer = ApplicationServer(resultRepository)

  override def run(args: List[String]): IO[ExitCode] =
    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"1488")
      .withHttpApp(CORS.policy.withAllowOriginAll(appServer.routes).orNotFound)
      .build
      .useForever
      .as(ExitCode.Success)

}
