package kafka

import cats.effect.Async
import fs2.kafka.KafkaConsumer
import kafka.implicits._
import shared.{Config, ProblemPublicData}

trait ProblemRepository[F[_]] {
  def getProblems: F[List[ProblemPublicData]]
}

object ProblemRepository {
  def apply[F[_]: Async](config: Config): ProblemRepository[F] =
    new ProblemRepository[F] {
      override def getProblems: F[List[ProblemPublicData]] =
        KafkaConsumer
          .stream(ConsumerSettings.forProblemIdKey[F, ProblemPublicData](config))
          .subscribeTo(config.kafka.publicProblemsTopic)
          .records
          .map { commitable =>
            commitable.record.value
          }
          .take(
            1 // TODO: update this number
          )
          .compile
          .toList
    }
}
