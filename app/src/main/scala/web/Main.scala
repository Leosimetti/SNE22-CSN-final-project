package web

import cats.effect._
import com.comcast.ip4s._
import org.http4s.ember.server._
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir._
import cats.effect.IO
import org.http4s.HttpRoutes

object Main extends IOApp {

  def countCharacters(s: String): IO[Either[Unit, Int]] =
    IO.pure(Right[Unit, Int](s.length))

  val countCharactersEndpoint: PublicEndpoint[String, Unit, Int, Any] =
    endpoint
      .post
      .in(stringBody)
      .out(plainBody[Int])

  val countCharactersRoutes: HttpRoutes[IO] =
    Http4sServerInterpreter[IO]()
      .toRoutes(countCharactersEndpoint.serverLogic(countCharacters))


  override def run(args: List[String]): IO[ExitCode] =
    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"1488")
      .withHttpApp(countCharactersRoutes.orNotFound)
      .build
      .useForever
      .as(ExitCode.Success)
}
