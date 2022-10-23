package kafka

import cats.effect.Sync
import cats.syntax.all._
import fs2.kafka.{Deserializer, Serializer}
import shared.ResultMessage.SealedValue
import shared.{Failure, Result, ResultMessage, Success}

package object implicits {

  implicit def resultDeserializer[F[_]](implicit F: Sync[F]): Deserializer[F, Result] = Deserializer.instance {
    case (_, _, data) =>
      ResultMessage.parseFrom(data).sealedValue match {
        case SealedValue.Empty          => Result.Empty.pure[F].widen[Result]
        case SealedValue.Success(value) => value.pure[F].widen[Result]
        case SealedValue.Failure(value) => value.pure[F].widen[Result]
      }
  }

  implicit def resultSerializer[F[_]](implicit F: Sync[F]): Serializer[F, Result] = Serializer.instance {
    case (_, _, result) =>
      result match {
        case Result.Empty => ResultMessage(SealedValue.Empty).toByteArray.pure[F]
        case empty: Result.NonEmpty =>
          empty match {
            case s: Success => s.toByteArray.pure[F]
            case f: Failure => f.toByteArray.pure[F]
          }
      }
  }

}
