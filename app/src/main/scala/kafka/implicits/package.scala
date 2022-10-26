package kafka

import cats.effect.Sync
import cats.syntax.all._
import fs2.kafka.{Deserializer, Serializer}
import shared.ResultMessage.SealedValue
import shared.{ProblemPrivateData, ProblemPublicData, Result, ResultMessage}

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
    case (_, _, result) => result.asMessage.toByteArray.pure[F]
  }

  implicit def problemPublicDeserializer[F[_]](implicit F: Sync[F]): Deserializer[F, ProblemPublicData] =
    Deserializer.instance { case (_, _, data) =>
      ProblemPublicData.parseFrom(data).pure[F]
    }

  implicit def problemPublicSerializer[F[_]](implicit F: Sync[F]): Serializer[F, ProblemPublicData] =
    Serializer.instance { case (_, _, result) =>
      result.toByteArray.pure[F]
    }

  implicit def problemPrivateDeserializer[F[_]](implicit F: Sync[F]): Deserializer[F, ProblemPrivateData] =
    Deserializer.instance { case (_, _, data) =>
      ProblemPrivateData.parseFrom(data).pure[F]
    }

  implicit def problemPrivateSerializer[F[_]](implicit F: Sync[F]): Serializer[F, ProblemPrivateData] =
    Serializer.instance { case (_, _, result) =>
      result.toByteArray.pure[F]
    }

}
