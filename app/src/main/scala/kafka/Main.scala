//package kafka
//
//import cats.effect.{IO, IOApp}
//import fs2.kafka._
//import scala.concurrent.duration._
//import fs2.Stream
//
//object Main extends IOApp.Simple {
//  val run: IO[Unit] = {
//    def processRecord(
//        record: ConsumerRecord[String, String]
//    ): IO[(String, String)] =
//      IO.pure(record.key -> record.value)
//
//    val producerSettings =
//      ProducerSettings[IO, Int, String]
//        .withBootstrapServers("localhost:9092")
//    val produces = KafkaProducer.stream(producerSettings).flatMap { producer =>
//      Stream
//        .range(1, 100)
//        .evalMap { i =>
//          ProducerRecords
//            .one(ProducerRecord("topic", i, i.toString + i.toString))
//        }
//        .through(KafkaProducer.pipe(producerSettings, producer))
//    }
//
//    val consumerSettings =
//      ConsumerSettings[IO, String, String]
//        .withAutoOffsetReset(AutoOffsetReset.Earliest)
//        .withBootstrapServers("localhost:9092")
//        .withGroupId("group")
//
//    val producerSettings =
//      ProducerSettings[IO, String, String]
//        .withBootstrapServers("localhost:9092")
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
//
//    stream.compile.drain
//  }
//}
