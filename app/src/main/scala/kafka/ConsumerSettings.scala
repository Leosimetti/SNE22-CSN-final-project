package kafka

import cats.effect.Sync
import fs2.kafka.{AutoOffsetReset, Deserializer, ConsumerSettings => KafkaConsumerSettings}
import shared.types.ProblemId

object ConsumerSettings {
  def forProblemIdKey[F[_]: Sync, T](
      config: shared.Config
  )(implicit aboba: Deserializer[F, T]): KafkaConsumerSettings[F, ProblemId, T] =
    KafkaConsumerSettings[F, ProblemId, T]
      .withAutoOffsetReset(AutoOffsetReset.Earliest)
      .withBootstrapServers(s"${config.kafka.host}:${config.kafka.port}")
      .withGroupId(config.kafka.groupId)
}
