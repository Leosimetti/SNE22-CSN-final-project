package client

import cats.effect.{ExitCode, IO, IOApp}
import org.http4s.ember.client._
import org.http4s.client._
import cats._, cats.effect._, cats.implicits._
import org.http4s.Uri

object App extends IOApp.Simple {

  override def run: IO[Unit] =
    EmberClientBuilder.default[IO].build.use { client =>
      client
        .get("https://www.google.com")(IO.println)
    }


}
