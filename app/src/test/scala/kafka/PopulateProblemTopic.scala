package kafka

import cats.effect.{IO, IOApp}
import cats.syntax.all._
import fs2.Stream
import fs2.kafka.{
  KafkaProducer,
  ProducerRecord,
  ProducerRecords,
  ProducerResult,
  ProducerSettings => KafkaProducerSettings,
}
import kafka.implicits._
import shared.types.ProblemId
import shared.{ProblemPrivateData, ProblemPublicData}

object PopulateProblemTopic extends IOApp.Simple {

  def dumpIntoKafka[K, V](
      items: List[V],
      getKey: V => K,
      producerSettings: KafkaProducerSettings[IO, K, V],
  ): Stream[IO, ProducerResult[Unit, K, V]] = {
    KafkaProducer
      .stream(producerSettings)
      .flatMap { producer =>
        Stream
          .emits(items)
          .map(item => ProducerRecords.one(ProducerRecord("problemsPublic", getKey(item), item)))
          .covary[IO]
          .through(KafkaProducer.pipe(producerSettings, producer))
      }
  }

  val problemDescriptions: List[ProblemPublicData] = List(
    ProblemPublicData(
      problemId = "a+b",
      problemText = "Add 2 numbers separated by newlines.",
      executionTimeThreshold = 1.2,
    ),
    ProblemPublicData(
      problemId = "factorial",
      problemText = "Compute the factorial of a number N.",
      executionTimeThreshold = 1.2,
    ),
    ProblemPublicData(
      problemId = "fibonacci",
      problemText = "Compute the Nth fibonacci numbers.",
      executionTimeThreshold = 1.2,
    ),
  )

  val problemTests: List[ProblemPrivateData] = List(
    ProblemPrivateData(problemId = "a+b", testInputs = Seq("1,2=3", "10000,10000=20000")),
    ProblemPrivateData(problemId = "factorial", testInputs = Seq("1=1", "0=1", "5=120")),
    ProblemPrivateData(problemId = "fibonacci", testInputs = Seq("1=1", "2=1", "3=2", "4=3", "5=5")),
  )

  override def run: IO[Unit] = {
    val pub = dumpIntoKafka[ProblemId, ProblemPublicData](
      problemDescriptions,
      _.problemId,
      ProducerSettings.forProblemIdKey("172.29.86.80", 9092),
    )

    val priv = dumpIntoKafka[ProblemId, ProblemPrivateData](
      problemTests,
      _.problemId,
      ProducerSettings.forProblemIdKey("172.29.86.80", 9092),
    )

    (priv.compile.drain, pub.compile.drain).parTupled.void
  }
}
