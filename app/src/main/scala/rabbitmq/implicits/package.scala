package rabbitmq

import cats.effect.Sync
import cats.syntax.all._
import fs2.kafka.{Deserializer, Serializer}
import shared.ResultMessage.SealedValue
import shared.{Result, ResultMessage}

package object implicits {

  implicit def taskDeserializer[F[_]](implicit F: Sync[F]): Deserializer[F, Result] = Deserializer.instance {
    case (_, _, data) =>
      ResultMessage.parseFrom(data).sealedValue match {
        case SealedValue.Empty          => Result.Empty.pure[F].widen[Result]
        case SealedValue.Success(value) => value.pure[F].widen[Result]
        case SealedValue.Failure(value) => value.pure[F].widen[Result]
      }
  }

  implicit def resultSerializer[F[_]](implicit F: Sync[F]): Serializer[F, Result] = Serializer.instance {
    case (_, _, result) => result.asMessage.toByteArray.pure[F]
  }

}
