package kafka

import cats.effect.{IO, IOApp}
import fs2.Stream
import fs2.kafka._
import kafka.implicits._
import shared.Language.python
import shared.types.ProblemId
import shared.{Result, Solution, Success}

object Main extends IOApp.Simple {
  val run: IO[Unit] = {

    val producerSettings =
      ProducerSettings[IO, ProblemId, Result]
        .withBootstrapServers("localhost:9092")

    val solution = Solution(code = "print()", language = python)
    val res: Result = Success(duration = 0.2d, solution = Some(solution))
    val produces = KafkaProducer.stream(producerSettings).flatMap { producer =>
      Stream("zhepa", "a+b")
        .evalMap { problemId =>
          IO.pure(
            ProducerRecords
              .one(ProducerRecord("aboba", problemId, res))
          )
        }
        .through(KafkaProducer.pipe(producerSettings, producer))
    }

//    def processRecord(
//                       record: ConsumerRecord[Int, String]
//                     ): IO[(Int, String)] =
//      IO.pure(record.key -> record.value)

    //    val consumerSettings =
//      ConsumerSettings[IO, Int, String]
//        .withAutoOffsetReset(AutoOffsetReset.Earliest)
//        .withBootstrapServers("localhost:9092")
//        .withGroupId("group")
//
//    val stream =
//      KafkaConsumer
//        .stream(consumerSettings)
//        .subscribeTo("topic")
//        .records
//        .mapAsync(25) { committable =>
//          processRecord(committable.record)
//            .map { case (key, value) =>
//              val record = ProducerRecord("topic", key, value)
//              ProducerRecords.one(record, committable.offset)
//            }
//        }
//        .through(KafkaProducer.pipe(producerSettings))
//        .map(_.passthrough)
//        .through(commitBatchWithin(500, 15.seconds))

    produces.compile.drain
  }
}
