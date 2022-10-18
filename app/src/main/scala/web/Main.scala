package web

import cats.effect._
import com.comcast.ip4s._
import org.http4s.ember.server._
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir._
import cats.effect.IO
import org.http4s.HttpRoutes
import shared.UserSubmission
import sttp.tapir.json.circe._
import org.http4s.server.middleware._

object Main extends IOApp {

  def putTaskIntoRabbitMQ(s: UserSubmission): IO[Either[Unit, String]] = {
    for {
      _ <- IO.println(s)
      res <- IO.pure(Right[Unit, String]("VSYO NORM"))
    } yield res

  }

  val submitResultEndpoint: PublicEndpoint[UserSubmission, Unit, String, Any] =
    endpoint.post
      .in(jsonBody[UserSubmission])
      .in("submit")
      .out(plainBody[String])

  val submitResultRoutes: HttpRoutes[IO] =
    Http4sServerInterpreter[IO]()
      .toRoutes(submitResultEndpoint.serverLogic(putTaskIntoRabbitMQ))

  override def run(args: List[String]): IO[ExitCode] =
    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"1488")
      .withHttpApp(CORS.policy.withAllowOriginAll(submitResultRoutes).orNotFound)
      .build
      .useForever
      .as(ExitCode.Success)
}
