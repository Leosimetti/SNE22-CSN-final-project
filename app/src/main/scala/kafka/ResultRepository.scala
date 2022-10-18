package kafka
import cats.effect.Async
import fs2.kafka.{ConsumerSettings, KafkaConsumer}
import shared.types.{ProblemId, UserId}
import shared.{ResultFrontendView, ResultKafkaView}

trait ResultRepository[F[_]] {
  def getResults(userId: UserId, problemId: ProblemId): F[List[ResultFrontendView]]
}

object ResultRepository {
  def apply[F[_]: Async](
      consumerSettings: ConsumerSettings[F, ProblemId, ResultKafkaView]
  ): ResultRepository[F] = new ResultRepository[F] {
    override def getResults(userId: UserId, problemId: ProblemId): F[List[ResultFrontendView]] = {
      KafkaConsumer
        .stream(consumerSettings)
        .subscribeTo(problemId)
        .records
        .filter { committable =>
          committable.record.value.submission.userId == userId
        }
        .map(_.record.value.toFrontendView)
        .compile
        .toList
    }
  }
}
