package shared

import io.circe.Decoder

import scala.concurrent.duration.Duration
import io.circe.generic.semiauto.deriveDecoder
import org.latestbit.circe.adt.codec._

object types {
  type UserId = String
  type ProblemId = String
  type ExecutionTimeThreshold = Double
}

import types._

sealed trait Language

object Language {
  case object Python extends Language

  implicit val decoder: Decoder[Language] = JsonTaggedAdtCodec.createPureEnumDecoder[Language]()
}

case class ReferenceSolution(code: String, language: Language)
object ReferenceSolution {
  implicit val decoder: Decoder[ReferenceSolution] = deriveDecoder
}
case class UserSolution(userId: UserId, code: String, language: Language)
object UserSolution {
  implicit val decoder: Decoder[UserSolution] = deriveDecoder
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
    userSolution: UserSolution,
)
object Task {
  implicit val decoder: Decoder[Task] = deriveDecoder
}

sealed trait Result {
  val userId: UserId
  val problemId: ProblemId
}

object Result {

  case class Success(
      duration: Duration,
      override val userId: UserId,
      override val problemId: ProblemId,
  ) extends Result

  case class Failure(
      duration: Duration,
      override val userId: UserId,
      override val problemId: ProblemId,
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
