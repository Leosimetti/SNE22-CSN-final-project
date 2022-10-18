package shared

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import org.latestbit.circe.adt.codec._
import sttp.tapir.Schema

object types {
  type UserId = String
  type ProblemId = String
  type ExecutionTimeRation = Double
  type ExecutionTimeThreshold = Double
}

import shared.types._

sealed trait Language

object Language {
  case object Python extends Language

  implicit val decoder: Decoder[Language] = JsonTaggedAdtCodec.createPureEnumDecoder[Language]()
  implicit val encoder: Encoder[Language] = JsonTaggedAdtCodec.createPureEnumEncoder[Language]()
  implicit val schema: Schema[Language] = Schema.derived
}

final case class Solution(code: String, language: Language)
object Solution {
  implicit val decoder: Decoder[Solution] = deriveDecoder
  implicit val encoder: Encoder[Solution] = deriveEncoder
  implicit val schema: Schema[Solution] = Schema.derived
}

final case class UserSubmission(problemId: ProblemId, userId: UserId, solution: Solution)
object UserSubmission {
  implicit val decoder: Decoder[UserSubmission] = deriveDecoder
  implicit val encoder: Encoder[UserSubmission] = deriveEncoder
  implicit val schema: Schema[UserSubmission] = Schema.derived
}

final case class Problem(
    problemId: ProblemId,
    testInputs: List[String],
    referenceSolutions: List[Solution],
    executionTimeThreshold: ExecutionTimeThreshold,
)
object Problem {
  implicit val decoder: Decoder[Problem] = deriveDecoder
}

final case class Task(
    problem: Problem,
    userSubmission: UserSubmission,
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
      duration: ExecutionTimeRation,
      override val submission: UserSubmission,
  ) extends ResultKafkaView

  /** For some failure cases the duration may not be available: Ideally this should be split into cases where the
    * duration is relevant, and the cases where the duration is irrelevant.
    */
  final case class Failure(
      duration: Option[ExecutionTimeRation],
      override val submission: UserSubmission,
  ) extends ResultKafkaView

//  final case class WrongAnswer(...)
//  final case class CompilationError(...)
//  final case class TimeoutError(...)
//  final case class RuntimeError(...)

  implicit class ToFrontendViewOps(kafkaView: ResultKafkaView) {
    def toFrontendView: ResultFrontendView = kafkaView match {
      case Success(duration, submission) =>
        ResultFrontendView.Success(submission.solution.code, submission.solution.language, duration)
      case Failure(duration, submission) =>
        ResultFrontendView.Failure(submission.solution.code, submission.solution.language, duration)
    }
  }

}

/** This is the result of the checking that the frontend will receive from the result backend (Kafka).
  */
sealed trait ResultFrontendView
object ResultFrontendView {
  final case class Success(code: String, language: Language, duration: ExecutionTimeRation) extends ResultFrontendView
  final case class Failure(code: String, language: Language, duration: Option[ExecutionTimeRation])
      extends ResultFrontendView
}
