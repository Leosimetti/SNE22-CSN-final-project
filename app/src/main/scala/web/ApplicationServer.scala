package web

import cats.effect._
import fs2.Stream
import io.grpc.Metadata
import kafka.ResultRepository
import rabbitmq.TaskRepository
import shared._

final case class ApplicationServer(
    rabbitMq: TaskRepository[IO],
    resultBackend: ResultRepository[IO],
) extends AppFs2Grpc[IO, Metadata] {

  override def submit(request: UserSubmission, ctx: Metadata): IO[SubmitResponse] = rabbitMq.submitTask(request)

  override def mySubmissions(request: MySubmissionsRequest, ctx: Metadata): Stream[IO, MySubmissionsResponse] =
    resultBackend.getResults(request.userId).map(MySubmissionsResponse(_))

  override def myTaskSubmissions(request: MyTaskSubmissionsRequest, ctx: Metadata): Stream[IO, MySubmissionsResponse] =
    resultBackend.getTaskResults(request.userId, request.problemId).map(MySubmissionsResponse(_))
}
