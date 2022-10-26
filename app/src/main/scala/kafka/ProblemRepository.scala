package kafka
import cats.effect.Async
import fs2.kafka.{ConsumerSettings, KafkaConsumer}
import shared.ProblemPublicData
import shared.types.ProblemId

trait ProblemRepository[F[_]] {
  def getProblems: F[List[ProblemPublicData]]
}

object ProblemRepository {
  def apply[F[_]: Async](consumerSettings: ConsumerSettings[F, ProblemId, ProblemPublicData]): ProblemRepository[F] =
    new ProblemRepository[F] {
      override def getProblems: F[List[ProblemPublicData]] =
        KafkaConsumer
          .stream(consumerSettings)
          .subscribeTo("problemsPublic")
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
