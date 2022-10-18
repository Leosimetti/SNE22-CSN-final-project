package web

import cats.effect._
import cats.syntax.all._
import kafka.ResultRepository
import org.http4s.HttpRoutes
import shared.types.{ProblemId, UserId}
import shared.{ResultFrontendView, UserSubmission}
import sttp.tapir._
import sttp.tapir.json.circe._
import sttp.tapir.server.http4s.Http4sServerInterpreter
import web.ApplicationServer._

final case class ApplicationServer(
    /* rabbitMq: RabbitMqRepository[IO] */
    resultBackend: ResultRepository[IO]
) {

  private def putTaskIntoRabbitMQ(s: UserSubmission): IO[Either[Unit, String]] = {
    for {
      _ <- IO.println(s)
      res <- IO.pure(Right[Unit, String]("VSYO NORM"))
    } yield res
  }

  val routes: HttpRoutes[IO] =
    Http4sServerInterpreter[IO]()
      .toRoutes(
        List(
          submitResultEndpoint.serverLogic(putTaskIntoRabbitMQ),
          mySubmissionsEndpoint.serverLogic { case (userId, problemId) =>
            resultBackend.getResults(userId, problemId).map(_.asRight[Unit])
          },
        )
      )

}

object ApplicationServer {

  val submitResultEndpoint: PublicEndpoint[UserSubmission, Unit, String, Any] =
    endpoint.post
      .in(jsonBody[UserSubmission])
      .in("submit")
      .out(plainBody[String])

  val mySubmissionsEndpoint: PublicEndpoint[(UserId, ProblemId), Unit, List[ResultFrontendView], Any] =
    endpoint.get
      .in("mySubmissions")
      .in(query[UserId]("userId"))
      .in(query[ProblemId]("problemId"))
      .out(jsonBody[List[ResultFrontendView]])

}
