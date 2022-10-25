import cats.effect.{ExitCode, IO, IOApp}
import com.linecorp.armeria.common.grpc.protocol.GrpcHeaderNames
import com.linecorp.armeria.common.scalapb.ScalaPbJsonMarshaller
import com.linecorp.armeria.common.{HttpHeaderNames, HttpMethod}
import com.linecorp.armeria.server.Server
import com.linecorp.armeria.server.cors.CorsService
import com.linecorp.armeria.server.grpc.GrpcService
import com.linecorp.armeria.server.logging.LoggingService
import dev.profunktor.fs2rabbit.config.Fs2RabbitConfig
import dev.profunktor.fs2rabbit.interpreter.RabbitClient
import fs2.kafka.{AutoOffsetReset, ConsumerSettings}
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

    app.use { service =>
      val corsBuilder = CorsService
        .builderForAnyOrigin()
        .allowRequestMethods(HttpMethod.POST)
        .allowRequestHeaders( // Allow POST method.
          // Allow Content-type and X-GRPC-WEB headers.
          HttpHeaderNames.CONTENT_TYPE,
          HttpHeaderNames.of("X-GRPC-WEB"),
          HttpHeaderNames.of("X-USER-AGENT"),
        )
        .exposeHeaders( // Expose trailers of the HTTP response to the client.
          GrpcHeaderNames.GRPC_STATUS,
          GrpcHeaderNames.GRPC_MESSAGE,
          GrpcHeaderNames.ARMERIA_GRPC_THROWABLEPROTO_BIN,
        )

      val grpcService =
        GrpcService
          .builder()
          // Add your ScalaPB gRPC stub using `bindService()`
          .addService(service)
          // Register `ScalaPbJsonMarshaller` for supporting gRPC JSON format.
//          .jsonMarshallerFactory(_ => ScalaPbJsonMarshaller())
          .enableUnframedRequests(false)
          .build()


      // Creates Armeria Server for ScalaPB gRPC stub.
      val server = Server
        .builder()
        .http(9999)
//        .https(httpsPort)
        .service(grpcService, corsBuilder.newDecorator())
        .decorator(LoggingService.newDecorator())
        // Add DocService for browsing the list of gRPC services and
        // invoking a service operation from a web form.
        // See https://armeria.dev/docs/server-docservice for more information.
//        .serviceUnder("/docs", new DocService())
        .build()

      IO.fromCompletableFuture(IO.delay(server.start())).as(ExitCode.Success)

    }
  }
}
