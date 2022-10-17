package shared

import io.circe.generic.semiauto.deriveDecoder
import io.circe.{Decoder, Encoder}
import org.latestbit.circe.adt.codec._

import scala.concurrent.duration.Duration

object types {
  type UserId = String
  type ProblemId = String
  type ExecutionTimeThreshold = Double
  type SubmissionId = String
}

import shared.types._

sealed trait Language

object Language {
  case object Python extends Language

  implicit val decoder: Decoder[Language] = JsonTaggedAdtCodec.createPureEnumDecoder[Language]()
  implicit val encoder: Encoder[Language] = JsonTaggedAdtCodec.createPureEnumEncoder[Language]()

}

case class ReferenceSolution(code: String, language: Language)
object ReferenceSolution {
  implicit val decoder: Decoder[ReferenceSolution] = deriveDecoder
}
case class UserSubmission(submissionId: SubmissionId, userId: UserId, code: String, language: Language)
object UserSubmission {
  implicit val decoder: Decoder[UserSubmission] = deriveDecoder
}

case class Problem(
    problemId: ProblemId,
    testInputs: List[String],
    referenceSolutions: List[ReferenceSolution],
    executionTimeThreshold: ExecutionTimeThreshold,
)
object Problem {
  implicit val decoder: Decoder[Problem] = deriveDecoder
}

case class Task(
    problem: Problem,
    userSolution: UserSubmission,
)
object Task {
  implicit val decoder: Decoder[Task] = deriveDecoder
}

sealed trait Result {
  val submissionId: SubmissionId
}

object Result {
  case class Success(
      duration: Duration,
      override val submissionId: SubmissionId,
  ) extends Result

  case class Failure(
      duration: Duration,
      override val submissionId: SubmissionId,
  ) extends Result

  // WrongAnswer

//  case class CompilationError(
//      msg: String,
//      override val userId: UserId,
//      override val problemId: ProblemId,
//  ) extends Result
//
//  case class TimeoutError(
//      duration: Duration,
//      override val userId: UserId,
//      override val problemId: ProblemId,
//  ) extends Result
//
//  case class RuntimeError(
//      msg: String,
//      duration: Duration,
//      exitCode: Int,
//      override val userId: UserId,
//      override val problemId: ProblemId,
//  ) extends Result
}
