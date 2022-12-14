package kafka

import cats.effect.{IO, IOApp}
import fs2.Stream
import fs2.kafka.{KafkaProducer, ProducerRecord, ProducerRecords, ProducerSettings}
import kafka.implicits._
import shared.Language.python
import shared.types.ProblemId
import shared.{Result, Solution, Success}

object PopulateResultTopic extends IOApp.Simple {

  val producerSettings: ProducerSettings[IO, ProblemId, Result] =
    ProducerSettings[IO, ProblemId, Result]
      .withBootstrapServers("localhost:9092")

  val run: IO[Unit] = {

    val produces = KafkaProducer.stream(producerSettings).flatMap { producer =>
      Stream("L", "+ratio", "+didn't ask", "L", "L", "GG", "GG", "GG")
        .map { taskID =>
          val solution = Solution(code = "heh()", language = python)
          val res: Result = Success(duration = 0.2d, taskId = taskID, solution = Some(solution))
          ProducerRecords
            .one(ProducerRecord("aboba", "KEKW", res))
        }
        .covary[IO]
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
