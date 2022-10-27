package shared
import pureconfig.ConfigReader
import pureconfig.generic.semiauto.deriveReader

case class Config(
    kafka: KafkaConfig,
    rabbitMq: RabbitMqConfig,
    serverPort: Int,
)

object Config {
  implicit val reader: ConfigReader[Config] = deriveReader
}

case class KafkaConfig(
    host: String,
    port: Int,
    groupId: String,
    privateProblemsTopic: String,
    publicProblemsTopic: String,
)

object KafkaConfig {
  implicit val reader: ConfigReader[KafkaConfig] = deriveReader
}

case class RabbitMqConfig(
    host: String,
    port: Int,
    username: String,
    password: String,
    taskQueueName: String,
    taskExchangeName: String,
    taskRoutingKey: String,
)

object RabbitMqConfig {
  implicit val reader: ConfigReader[RabbitMqConfig] = deriveReader
}
