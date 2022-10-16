package shared

import scala.concurrent.duration.Duration

object types {
  type UserId = String
  type ProblemId = String
  type ExecutionTimeThreshold = Double
}

import types._

sealed trait Language

object Language {
  case object Python extends Language
}

case class ReferenceSolution(code: String, language: Language)
case class UserSolution(userId: UserId, code: String, language: Language)

case class Problem(
    problemId: ProblemId,
    testInputs: List[String],
    referenceSolutions: List[ReferenceSolution],
    executionTimeThreshold: ExecutionTimeThreshold,
)

case class Task(
    problem: Problem,
    userSolution: UserSolution,
)

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

  // WrongAnswer

  case class CompilationError(
      msg: String,
      override val userId: UserId,
      override val problemId: ProblemId,
  ) extends Result

  case class TimeoutError(
      duration: Duration,
      override val userId: UserId,
      override val problemId: ProblemId,
  ) extends Result

  case class RuntimeError(
      msg: String,
      duration: Duration,
      exitCode: Int,
      override val userId: UserId,
      override val problemId: ProblemId,
  ) extends Result
}
