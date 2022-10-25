package web
import cats.effect.std.Dispatcher
import cats.effect.{IO, IOApp}
import com.linecorp.armeria.client.Clients
import com.linecorp.armeria.client.grpc.{GrpcClientOptions, GrpcClientStubFactory}
import com.linecorp.armeria.client.logging.LoggingClient
import com.linecorp.armeria.client.retry.{RetryRule, RetryingClient}
import com.linecorp.armeria.common.Flags
import io.grpc.{Channel, Metadata, ServiceDescriptor}
import org.slf4j.{Logger, LoggerFactory}
import shared._

object ApplicationTestClient extends IOApp.Simple {
  val logger: Logger = LoggerFactory.getLogger("app-client")


  val helloService = Dispatcher[IO].map(disp =>
    Clients
      .builder(s"gproto+http://127.0.0.1:9999/")
      .option(GrpcClientOptions.GRPC_CLIENT_STUB_FACTORY.newValue(new GrpcClientStubFactory {

        override def findServiceDescriptor(clientType: Class[_]): ServiceDescriptor =
          AppGrpc.SERVICE

        override def newClientStub(clientType: Class[_], channel: Channel): AnyRef =
          AppFs2Grpc.stub[IO](disp, channel)

      }))
      .responseTimeout(java.time.Duration.ofSeconds(10))
      .decorator(RetryingClient.newDecorator(RetryRule.onUnprocessed()))
      .decorator(LoggingClient.newDecorator())
      .build(classOf[AppFs2Grpc[IO, Metadata]])
  )

  val request: UserSubmission = UserSubmission(
    problemId = "a+b",
    userId = "aboba",
    solution = Some(Solution(code = "print", Language.python)),
  )

  def sendRequest(client: AppFs2Grpc[IO, Metadata]): IO[Unit] =
    client.submit(request, new Metadata()).flatMap { reply =>
      IO(logger.info(reply.status))
    }

  val run: IO[Unit] = {
    System.setProperty("com.linecorp.armeria.useOpenSsl", "false")
    println(Flags.useOpenSsl())
    helloService.use(service => service.submit(request, new Metadata()).flatMap(IO.println))
//    IO.fromFuture(IO.delay(helloService.submit(request))).flatMap(IO.println)
  }
//    managedChannelResource
//      .flatMap(managedChannel => AppFs2Grpc.clientResource[IO, Metadata](managedChannel, identity))
//      .use(client => sendRequest(client))
}
