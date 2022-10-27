package rabbitmq

import cats.data.Kleisli
import cats.effect._
import cats.implicits._
import dev.profunktor.fs2rabbit.config.Fs2RabbitConfig
import dev.profunktor.fs2rabbit.config.declaration.DeclarationQueueConfig
import dev.profunktor.fs2rabbit.interpreter.RabbitClient
import dev.profunktor.fs2rabbit.json.Fs2JsonEncoder
import dev.profunktor.fs2rabbit.model.AckResult.Ack
import dev.profunktor.fs2rabbit.model.AmqpFieldValue.{LongVal, StringVal}
import dev.profunktor.fs2rabbit.model._
import dev.profunktor.fs2rabbit.resiliency.ResilientStream
import fs2.{Pipe, Stream}

import java.nio.charset.StandardCharsets.UTF_8
import scala.concurrent.duration._

class AutoAckFlow[F[_]: Async, A](
    consumer: Stream[F, AmqpEnvelope[A]],
    logger: Pipe[F, AmqpEnvelope[A], AckResult],
    publisher: AmqpMessage[String] => F[Unit],
) {
  import io.circe.generic.auto._
  case class Address(number: Int, streetName: String)
  case class Person(id: Long, name: String, address: Address)

  private val jsonEncoder = new Fs2JsonEncoder
  import jsonEncoder.jsonEncode

  val jsonPipe: Pipe[F, AmqpMessage[Person], AmqpMessage[String]] =
    _.covary[F].map(jsonEncode[Person])

  val simpleMessage: AmqpMessage[String] =
    AmqpMessage(
      "Hey!",
      AmqpProperties(headers = Map("demoId" -> LongVal(123), "app" -> StringVal("fs2RabbitDemo"))),
    )

  val classMessage: AmqpMessage[Person] = AmqpMessage(
    Person(1L, "Sherlock", Address(212, "Baker St")),
    AmqpProperties.empty,
  )

  val flow: Stream[F, Unit] =
    Stream(
      Stream(simpleMessage).covary[F].evalMap(publisher),
      Stream(classMessage).covary[F].through(jsonPipe).evalMap(publisher),
      consumer.through(logger).evalMap(ack => Async[F].delay(println(ack))),
    ).parJoin(3)

}

class AutoAckConsumerDemo[F[_]: Async](R: RabbitClient[F]) {

  private val queueName = QueueName("testQ")
  private val exchangeName = ExchangeName("testEX")
  private val routingKey = RoutingKey("testRK")
  implicit val stringMessageEncoder: Kleisli[F, AmqpMessage[String], AmqpMessage[Array[Byte]]] =
    Kleisli(s => s.copy(payload = s.payload.getBytes(UTF_8)).pure[F])

  def logPipe: Pipe[F, AmqpEnvelope[String], AckResult] = _.evalMap { amqpMsg =>
    Async[F].delay(println(s"Consumed: $amqpMsg")).as(Ack(amqpMsg.deliveryTag))
  }

  val program: F[Unit] = R.createConnectionChannel.use { implicit channel =>
    for {
      _ <- R.declareQueue(DeclarationQueueConfig.default(queueName))
      _ <- R.declareExchange(exchangeName, ExchangeType.Topic)
      _ <- R.bindQueue(queueName, exchangeName, routingKey)
      publisher <- R.createPublisher[AmqpMessage[String]](exchangeName, routingKey)
      consumer <- R.createAutoAckConsumer[String](queueName)
      _ <- new AutoAckFlow[F, String](consumer, logPipe, publisher).flow.compile.drain
    } yield ()
  }
}

object AutoAckConsumerDemo extends IOApp {

  private val config: Fs2RabbitConfig = Fs2RabbitConfig(
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

  override def run(args: List[String]): IO[ExitCode] =
    RabbitClient.default[IO](config).resource.use { client =>
      ResilientStream
        .runF(new AutoAckConsumerDemo[IO](client).program)
        .as(ExitCode.Success)
    }

}
