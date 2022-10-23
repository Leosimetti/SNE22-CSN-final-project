package web

import cats.effect._
import io.grpc.Metadata
import kafka.ResultRepository
import shared._

final case class ApplicationServer(
    /* rabbitMq: RabbitMqRepository[IO] */
    resultBackend: ResultRepository[IO]
) extends AppFs2Grpc[IO, Metadata] {

  // TODO:
  private def putTaskIntoRabbitMQ(s: UserSubmission): IO[SubmitResponse] = {
    for {
      _ <- IO.println(s)
    } yield SubmitResponse("VSYO NORM")
  }

  override def submit(request: UserSubmission, ctx: Metadata): IO[SubmitResponse] = putTaskIntoRabbitMQ(request)

  override def mySubmissions(request: MySubmissionsRequest, ctx: Metadata): IO[MySubmissionsResponse] =
    resultBackend.getResults(request.userId).map(MySubmissionsResponse(_))

  override def myTaskSubmissions(request: MyTaskSubmissionsRequest, ctx: Metadata): IO[MySubmissionsResponse] =
    resultBackend.getTaskResults(request.userId, request.problemId).map(MySubmissionsResponse(_))
}
