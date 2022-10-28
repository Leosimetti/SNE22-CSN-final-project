package kafka

import fs2.kafka.{RecordSerializer, ProducerSettings => KafkaProducerSettings}
import shared.types.ProblemId

object ProducerSettings {
  def forProblemIdKey[F[_], T](host: String, port: Int)(implicit
      v: RecordSerializer[F, T],
      k: RecordSerializer[F, ProblemId],
  ): KafkaProducerSettings[F, ProblemId, T] =
    KafkaProducerSettings[F, ProblemId, T]
      .withBootstrapServers(s"$host:$port")

}
