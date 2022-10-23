package kafka
import cats.effect.Async
import fs2.kafka.{CommittableConsumerRecord, ConsumerSettings, KafkaConsumer}
import fs2.{Pipe, Stream}
import shared.Result
import shared.types.{ProblemId, UserId}

trait ResultRepository[F[_]] {
  def getResults(userId: UserId): Stream[F, Result.NonEmpty]
  def getTaskResults(userId: UserId, problemId: ProblemId): Stream[F, Result.NonEmpty]
}

object ResultRepository {
  def apply[F[_]: Async](
      consumerSettings: ConsumerSettings[F, ProblemId, Result]
  ): ResultRepository[F] = new ResultRepository[F] {

    private def recordStream(userId: UserId): Stream[F, CommittableConsumerRecord[F, ProblemId, Result]] =
      KafkaConsumer
        .stream(consumerSettings)
        .subscribeTo(userId)
        .records

    private val nonEmptyResults: Pipe[F, CommittableConsumerRecord[F, ProblemId, Result], Result.NonEmpty] =
      commitables =>
        commitables.collect { committable =>
          committable.record.value match {
            case res: Result.NonEmpty => res
          }
        }

    override def getResults(userId: UserId): Stream[F, Result.NonEmpty] = {
      recordStream(userId)
        .through(nonEmptyResults)
    }

    override def getTaskResults(userId: UserId, problemId: ProblemId): Stream[F, Result.NonEmpty] =
      recordStream(userId)
        .filter(_.record.key == problemId)
        .through(nonEmptyResults)

  }
}
