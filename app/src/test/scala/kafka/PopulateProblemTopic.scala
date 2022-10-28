package kafka

import cats.effect.{IO, IOApp}
import cats.implicits.catsSyntaxTuple2Parallel
import fs2.Stream
import fs2.kafka.{
  KafkaProducer,
  ProducerRecord,
  ProducerRecords,
  ProducerResult,
  ProducerSettings => KafkaProducerSettings,
}
import kafka.implicits._
import shared._
import shared.types.ProblemId

object PopulateProblemTopic extends IOApp.Simple {

  def dumpIntoKafka[K, V](
      items: List[V],
      getKey: V => K,
      producerSettings: KafkaProducerSettings[IO, K, V],
      topic: String,
  ): Stream[IO, ProducerResult[Unit, K, V]] = {
    KafkaProducer
      .stream(producerSettings)
      .flatMap { producer =>
        Stream
          .emits(items)
          .map(item => ProducerRecords.one(ProducerRecord(topic, getKey(item), item)))
          .covary[IO]
          .through(KafkaProducer.pipe(producerSettings, producer))
      }
  }

  val problemDescriptions: List[ProblemPublicData] = List(
    ProblemPublicData(
      problemId = "AplusB",
      problemText = "Add 2 numbers separated by newlines.",
      executionTimeThreshold = 1.2,
      examples = Seq(
        TestData(input = "1\n2", output = "3"),
        TestData(input = "10000\n20000", output = "30000"),
      ),
      inputDescription = "Input is two numbers separated by newlines.",
      outputDescription = "Output is a single number.",
    ),
    ProblemPublicData(
      problemId = "factorial",
      problemText = "Compute the factorial of a number N.",
      executionTimeThreshold = 1.2,
      examples = Seq(
        TestData(input = "1", output = "1"),
        TestData(input = "10", output = "3628800"),
      ),
      inputDescription = "Input is a single number.",
      outputDescription = "Output is a single number.",
    ),
    ProblemPublicData(
      problemId = "fibonacci",
      problemText = "Compute the Nth Fibonacci numbers.",
      executionTimeThreshold = 1.2,
      examples = Seq(
        TestData(input = "1", output = "1"),
        TestData(input = "2", output = "1"),
        TestData(input = "3", output = "2"),
        TestData(input = "4", output = "3"),
        TestData(input = "5", output = "5"),
      ),
      inputDescription = "Input is a single number.",
      outputDescription = "Output is a single number.",
    ),
  )

  val problemTests: List[ProblemPrivateData] = List(
    ProblemPrivateData(
      problemId = "AplusB",
      testInputs = Seq(
        TestData(input = "1\n2", output = "3"),
        TestData(input = "10000\n20000", output = "30000"),
      ),
      referenceSolutions = Seq(
        Solution(code = """print(int(input()) + int(input()))""", language = Language.python)
      ),
    ),
    ProblemPrivateData(
      problemId = "factorial",
      testInputs = Seq(
        TestData(input = "1", output = "1"),
        TestData(input = "10", output = "3628800"),
      ),
      referenceSolutions = Seq(
        Solution(
          code = """
              |import math
              |print(math.factorial(int(input())))
              |""".stripMargin,
          language = Language.python,
        )
      ),
    ),
    ProblemPrivateData(
      problemId = "fibonacci",
      testInputs = Seq(
        TestData(input = "1", output = "1"),
        TestData(input = "2", output = "1"),
        TestData(input = "3", output = "2"),
        TestData(input = "4", output = "3"),
        TestData(input = "5", output = "5"),
      ),
      referenceSolutions = Seq(
        Solution(
          code = """
          |class Fibonacci:
          |    def __init__(self):
          |        self.cache = [0, 1]
          |
          |    def __call__(self, n):
          |        # Validate the value of n
          |        if not (isinstance(n, int) and n >= 0):
          |            raise ValueError(f'Positive integer number expected, got "{n}"')
          |
          |        # Check for computed Fibonacci numbers
          |        if n < len(self.cache):
          |            return self.cache[n]
          |        else:
          |            # Compute and cache the requested Fibonacci number
          |            fib_number = self(n - 1) + self(n - 2)
          |            self.cache.append(fib_number)
          |
          |        return self.cache[n]
          |
          |print(Fibonacci()(int(input())))
          |""".stripMargin,
          language = Language.python,
        )
      ),
    ),
  )

  override def run: IO[Unit] = {
    val pub = dumpIntoKafka[ProblemId, ProblemPublicData](
      problemDescriptions,
      _.problemId,
      ProducerSettings.forProblemIdKey("172.29.104.5", 9092),
      "problemsPublic",
    )

    val priv = dumpIntoKafka[ProblemId, ProblemPrivateData](
      problemTests,
      _.problemId,
      ProducerSettings.forProblemIdKey("172.29.104.5", 9092),
      "problemsPrivate",
    )

    (
      priv.compile.drain,
      pub.compile.drain,
    ).parTupled.void
  }
}
