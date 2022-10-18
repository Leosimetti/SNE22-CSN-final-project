package shared

import io.circe.generic.semiauto.deriveDecoder
import io.circe.{Decoder, Encoder}
import org.latestbit.circe.adt.codec._

import scala.concurrent.duration.Duration

object types {
  type UserId = String
  type ProblemId = String
  type ExecutionTimeThreshold = Double
}

import shared.types._

sealed trait Language

object Language {
  case object Python extends Language

  implicit val decoder: Decoder[Language] = JsonTaggedAdtCodec.createPureEnumDecoder[Language]()
  implicit val encoder: Encoder[Language] = JsonTaggedAdtCodec.createPureEnumEncoder[Language]()

}

final case class ReferenceSolution(code: String, language: Language)
object ReferenceSolution {
  implicit val decoder: Decoder[ReferenceSolution] = deriveDecoder
}
final case class UserSubmission(problemId: ProblemId, userId: UserId, code: String, language: Language)
object UserSubmission {
  implicit val decoder: Decoder[UserSubmission] = deriveDecoder
}

final case class Problem(
    problemId: ProblemId,
    testInputs: List[String],
    referenceSolutions: List[ReferenceSolution],
    executionTimeThreshold: ExecutionTimeThreshold,
)
object Problem {
  implicit val decoder: Decoder[Problem] = deriveDecoder
}

final case class Task(
    problem: Problem,
    userSolution: UserSubmission,
)
object Task {
  implicit val decoder: Decoder[Task] = deriveDecoder
}

sealed trait ResultKafkaView {
  val submission: UserSubmission
}

/** This is the result of the checking that will be stored in Kafka. For now we store the entire user submission along
  * with the solution because we can not get them elsewhere.
  */
object ResultKafkaView {
  final case class Success(
      duration: Duration,
      override val submission: UserSubmission,
  ) extends ResultKafkaView

  /** For some failure cases the duration may not be available: Ideally this should be split into cases where the
    * duration is relevant, and the cases where the duration is irrelevant.
    */
  final case class Failure(
      duration: Option[Duration],
      override val submission: UserSubmission,
  ) extends ResultKafkaView

//  final case class WrongAnswer(...)
//  final case class CompilationError(...)
//  final case class TimeoutError(...)
//  final case class RuntimeError(...)

  implicit class ToFrontendViewOps(kafkaView: ResultKafkaView) {
    def toFrontendView: ResultFrontendView = kafkaView match {
      case Success(duration, submission) => ResultFrontendView.Success(submission.code, submission.language, duration)
      case Failure(duration, submission) => ResultFrontendView.Failure(submission.code, submission.language, duration)
    }
  }

}

/** This is the result of the checking that the frontend will receive from the result backend (Kafka).
  */
sealed trait ResultFrontendView
object ResultFrontendView {
  final case class Success(code: String, language: Language, duration: Duration) extends ResultFrontendView
  final case class Failure(code: String, language: Language, duration: Option[Duration]) extends ResultFrontendView
}
