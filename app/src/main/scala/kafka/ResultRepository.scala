package kafka
import cats.data.NonEmptyChain
import cats.effect.Async
import cats.syntax.all._
import fs2.kafka.{CommittableConsumerRecord, KafkaConsumer}
import fs2.{Chunk, Pipe, Stream}
import kafka.implicits._
import shared.types.{ProblemId, UserId}
import shared.{Config, Failure, Result, Success}
trait ResultRepository[F[_]] {
  def getResults(userId: UserId): Stream[F, Result.NonEmpty]
  def getTaskResults(userId: UserId, problemId: ProblemId): Stream[F, Result.NonEmpty]
}

object ResultRepository {
  def apply[F[_]: Async](config: Config): ResultRepository[F] = new ResultRepository[F] {

    private def mapGroupReduceGroupsOfSize[I, K, O](
        groupSize: Int
    )(fk: I => K)(collapse: NonEmptyChain[I] => O): Pipe[F, I, O] = {

      type State = Map[K, NonEmptyChain[I]]

      @annotation.tailrec
      def loop(
          remaining: Chunk[I],
          state: State,
          results: Chunk[O] = Chunk.empty[O],
      ): (State, Chunk[O]) = {
        remaining.head match {
          case None => (state, results)
          case Some(head) =>
            val tail = remaining.drop(1)
            val updatedKey = fk(head)
            val updatedState = state.updatedWith(updatedKey) {
              case Some(results) => Some(results.append(head))
              case None          => Some(NonEmptyChain.one(head))
            }
            val updatedValue = updatedState(updatedKey)

            if (updatedValue.length == groupSize) {
              loop(
                tail,
                updatedState.removed(updatedKey),
                results ++ Chunk(collapse(updatedValue)),
              )
            } else
              loop(tail, updatedState, results)
        }
      }

      in =>
        in.scanChunks(Map.empty[K, NonEmptyChain[I]]) { case (state, next) =>
          loop(next, state)
        }
    }

    private def recordStream(userId: UserId): Stream[F, CommittableConsumerRecord[F, ProblemId, Result]] =
      KafkaConsumer
        .stream(ConsumerSettings.forProblemIdKey[F, Result](config))
        .subscribeTo(userId)
        .records

    private def reduceResults(results: NonEmptyChain[Result.NonEmpty]): Result.NonEmpty = {

      val (durationSum, isFailure) = results.foldLeft((0.0d, results.head)) {
        case ((sum, f), Success(duration, _, _, _)) => (sum + duration, f)
        case ((sum, _), fail: Failure)              => (sum, fail)
      }

      isFailure match {
        case s: Success => s.copy(duration = durationSum / results.length)
        case f: Failure => f
      }
    }

    private val nonEmptyResults: Pipe[F, CommittableConsumerRecord[F, ProblemId, Result], Result.NonEmpty] =
      commitables =>
        commitables
          .collect { committable =>
            committable.record.value match {
              case res: Result.NonEmpty => res
            }
          }
          .through(mapGroupReduceGroupsOfSize[Result.NonEmpty, String, Result.NonEmpty](3) {
            case Success(_, _, taskId, _) => taskId
            case Failure(_, _, taskId, _) => taskId
          } { chain =>
            reduceResults(chain)
          })

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
