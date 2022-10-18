package shared

import cats.effect.Sync
import cats.syntax.all._
import fs2.Stream
import fs2.kafka.Deserializer
import io.circe
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import org.latestbit.circe.adt.codec._
import sttp.tapir.Schema

object types {
  type UserId = String
  type ProblemId = String
  type ExecutionTimeRatio = Double
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
      duration: ExecutionTimeRatio,
      override val submission: UserSubmission,
  ) extends ResultKafkaView

  /** For some failure cases the duration may not be available: Ideally this should be split into cases where the
    * duration is relevant, and the cases where the duration is irrelevant.
    */
  final case class Failure(
      duration: Option[ExecutionTimeRatio],
      override val submission: UserSubmission,
  ) extends ResultKafkaView

  implicit val encoderSuccess: Encoder.AsObject[Success] = deriveEncoder
  implicit val decoderSuccess: Decoder[Success] = deriveDecoder

  implicit val encoderFailure: Encoder.AsObject[Failure] = deriveEncoder
  implicit val decoderFailure: Decoder[Failure] = deriveDecoder

  implicit val schema: Schema[ResultFrontendView] = Schema.derived
  implicit val encoder: Encoder[ResultKafkaView] = JsonTaggedAdtCodec.createEncoder("type")
  implicit val decoder: Decoder[ResultKafkaView] = JsonTaggedAdtCodec.createDecoder("type")

  implicit def deserializer[F[_]](implicit F: Sync[F]): Deserializer[F, ResultKafkaView] = Deserializer.instance {
    case (_, _, data) =>
      val string = Stream.emits(data).through(fs2.text.utf8.decode).compile.string
      for {
        json <- F.fromEither(circe.parser.parse(string))
        decoded <- F.fromEither(decoder.decodeJson(json))
      } yield decoded
  }

//  final case class WrongAnswer(...)
//  final case class CompilationError(...)
//  final case class TimeoutError(...)
//  final case class RuntimeError(...)

  implicit class ToFrontendViewOps(kafkaView: ResultKafkaView) {
    def toFrontendView: ResultFrontendView = kafkaView match {
      case Success(duration, submission) =>
        ResultFrontendView.Success(Solution(submission.solution.code, submission.solution.language), duration)
      case Failure(duration, submission) =>
        ResultFrontendView.Failure(Solution(submission.solution.code, submission.solution.language), duration)
    }
  }

}

/** This is the result of the checking that the frontend will receive from the result backend (Kafka).
  */
sealed trait ResultFrontendView
object ResultFrontendView {
  final case class Success(solution: Solution, duration: ExecutionTimeRatio) extends ResultFrontendView
  final case class Failure(solution: Solution, duration: Option[ExecutionTimeRatio]) extends ResultFrontendView

  implicit val encoderSuccess: Encoder.AsObject[Success] = deriveEncoder
  implicit val decoderSuccess: Decoder[Success] = deriveDecoder

  implicit val encoderFailure: Encoder.AsObject[Failure] = deriveEncoder
  implicit val decoderFailure: Decoder[Failure] = deriveDecoder

  implicit val schema: Schema[ResultFrontendView] = Schema.derived
  implicit val encoder: Encoder[ResultFrontendView] = JsonTaggedAdtCodec.createEncoder("type")
  implicit val decoder: Decoder[ResultFrontendView] = JsonTaggedAdtCodec.createDecoder("type")

}
