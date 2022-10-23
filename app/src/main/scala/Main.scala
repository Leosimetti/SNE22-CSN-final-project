import cats.effect.{ExitCode, IO, IOApp, Resource}
import fs2.grpc.syntax.all._
import fs2.kafka.{AutoOffsetReset, ConsumerSettings}
import io.grpc.ServerServiceDefinition
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder
import kafka.ResultRepository
import kafka.implicits._
import shared.types.ProblemId
import shared.{AppFs2Grpc, Result}
import web.ApplicationServer

object Main extends IOApp {

  val consumerSettings: ConsumerSettings[IO, ProblemId, Result] =
    ConsumerSettings[IO, ProblemId, Result]
      .withAutoOffsetReset(AutoOffsetReset.Earliest)
      .withBootstrapServers("192.168.31.206:9092")
      .withGroupId("group")

  val resultRepository: ResultRepository[IO] = ResultRepository[IO](consumerSettings)
  val appService: Resource[IO, ServerServiceDefinition] =
    AppFs2Grpc.bindServiceResource(ApplicationServer(resultRepository))

  override def run(args: List[String]): IO[ExitCode] = appService.use(service =>
    NettyServerBuilder
      .forPort(9999)
      .addService(service)
      .resource[IO]
      .evalMap(server => IO.delay(server.start()))
      .useForever
  )
}
