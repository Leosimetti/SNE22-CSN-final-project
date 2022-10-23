package rabbitmq

import cats.data.Kleisli
import cats.effect.Async
import cats.effect.std.UUIDGen
import cats.implicits._
import dev.profunktor.fs2rabbit.model.AckResult.NAck
import dev.profunktor.fs2rabbit.config.declaration.DeclarationQueueConfig
import dev.profunktor.fs2rabbit.effects.EnvelopeDecoder
import dev.profunktor.fs2rabbit.interpreter.RabbitClient
import dev.profunktor.fs2rabbit.model.{
  AckResult,
  AmqpEnvelope,
  AmqpMessage,
  AmqpProperties,
  ExchangeName,
  ExchangeType,
  QueueName,
  RoutingKey,
}
import dev.profunktor.fs2rabbit.resiliency.ResilientStream
import fs2.{Pipe, Stream}
import shared.{Problem, SubmitResponse, Task, UserSubmission}

trait TaskRepository[F[_]] {
  def submitTask(submission: UserSubmission): F[SubmitResponse]
}

object TaskRepository {

  def apply[F[_]: Async: UUIDGen](
      client: RabbitClient[F]
  ): TaskRepository[F] = new TaskRepository[F] {

    val queueName = QueueName("testQ")
    val exchangeName = ExchangeName("testEX")
    val routingKey = RoutingKey("testRK")
    implicit val messageEncoder: Kleisli[F, AmqpMessage[Array[Byte]], AmqpMessage[Array[Byte]]] =
      Kleisli(s => s.pure[F])

    implicit val messageDecoder: EnvelopeDecoder[F, Array[Byte]] =
      Kleisli(s => s.payload.pure[F])

    override def submitTask(submission: UserSubmission): F[SubmitResponse] = {
      // TODO: add UUID generation
      val response: SubmitResponse = SubmitResponse("I like aboba")

      val task: F[AmqpMessage[Task]] = {
        UUIDGen[F].randomUUID
          .map(uuid =>
            AmqpMessage(
              Task(
                taskId = uuid.toString,
                problem = Problem().some,
                userSubmission = submission.some,
              ),
              AmqpProperties.empty,
            )
          )

      }

      def logPipe: Pipe[F, AmqpMessage[Task], AmqpMessage[Task]] = _.evalMap { msg =>
        Async[F]
          .delay(println(s"Sent: $msg"))
          .as(msg)
      }

      val serializePipe: Pipe[F, AmqpMessage[Task], AmqpMessage[Array[Byte]]] =
        _.covary[F].map { msg =>
          msg.copy(
            payload = msg.payload.toByteArray
          )
        }

      val program: F[Unit] = client.createConnectionChannel.use { implicit channel =>
        for {
          task <- task
          _ <- client.declareQueue(DeclarationQueueConfig.default(queueName))
          _ <- client.declareExchange(exchangeName, ExchangeType.Topic)
          _ <- client.bindQueue(queueName, exchangeName, routingKey)
          publisher <- client.createPublisher[AmqpMessage[Array[Byte]]](exchangeName, routingKey)
          flow = Stream(task, task, task)
            .covary[F]
            .through(logPipe)
            .through(serializePipe)
            .evalMap(publisher)

          _ <- flow.compile.drain
        } yield ()
      }

      ResilientStream
        .runF(program)
        .as(response)
    }

  }

}
