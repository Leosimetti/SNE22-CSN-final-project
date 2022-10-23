package web
import cats.effect.{IO, IOApp, Resource}
import fs2.Stream
import fs2.grpc.syntax.all._
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder
import io.grpc.{ManagedChannel, Metadata}
import org.slf4j.{Logger, LoggerFactory}
import shared.{AppFs2Grpc, MySubmissionsRequest}

object ApplicationTestClient extends IOApp.Simple {
  val logger: Logger = LoggerFactory.getLogger("app-client")

  def managedChannelResource: Resource[IO, ManagedChannel] =
    NettyChannelBuilder
      .forAddress("localhost", 9999)
      .usePlaintext()
      .resource[IO]

  val request: MySubmissionsRequest = MySubmissionsRequest(
    userId = "aboba"
  )

  def sendRequest(client: AppFs2Grpc[IO, Metadata]): Stream[IO, Unit] =
    client.mySubmissions(request, new Metadata()).evalMap { reply =>
      IO(logger.info(reply.result.asMessage.toProtoString))
    }

  val run: IO[Unit] =
    managedChannelResource
      .flatMap(managedChannel => AppFs2Grpc.clientResource[IO, Metadata](managedChannel, identity))
      .use(client => sendRequest(client).compile.drain)
}
