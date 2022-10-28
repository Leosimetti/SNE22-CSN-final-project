import pika
import json
import logging as log
import jsonschema
import uuid
import kafka
import kafka.errors
import argparse
import time
from judge import judge
import schema_pb2 as schema

parser = argparse.ArgumentParser()
parser.add_argument(
    "-log",
    "--loglevel",
    default="warning",
    help="Provide logging level. Example --loglevel debug, default=warning",
)

args = parser.parse_args()

log.basicConfig(level=args.loglevel.upper())

RABBITMQ_HOST = "172.29.86.80"
RABBITMQ_QUEUE = "TaskQueue"

KAFKA_HOST = "172.29.86.80"

log.info(f"opening blocking connection on {RABBITMQ_HOST}")
with pika.BlockingConnection(pika.ConnectionParameters(f"{RABBITMQ_HOST}")) as connection:  # type: ignore
    log.info(f"blocking connection opened")
    log.info(f"opening channel")
    with connection.channel() as channel:
        log.info("channel opened")
        try:
            log.info("entering work loop")
            for method, properties, body in channel.consume(f"{RABBITMQ_QUEUE}"):
                log.info(f"received task")
                log.debug(f"method: {method}")
                log.debug(f"properties: {properties}")
                log.debug(f"body: {body}")

                log.info("parsing body")

                try:
                    task = schema.Task()
                    task.ParseFromString(body)
                except Exception as e:
                    log.error(f"parsing task body failed")
                    log.error(e)
                    log.error(f"ignoring due to error")
                    continue

                log.info(f"starting judgement")

                result = judge(task)

                log.info("judgement complete")
                log.debug(f"result: {result}")

                log.info("connecting to kafka")


                def value_serializer(v):
                    log.info(f"serializing value {v}")
                    serial = v.SerializeToString()
                    log.debug(f"serialized value: {serial}")
                    return serial



                producer = kafka.KafkaProducer(
                    bootstrap_servers=[f"{KAFKA_HOST}:9092"],
                    key_serializer=lambda k: bytes(k, 'utf-8'),
                    value_serializer=value_serializer,
                )
                log.info("kafka producer connected")

                log.info("sending judgement result")
                try:
                    future = producer.send(
                        topic=task.userSubmission.userId, key=task.problem.problemId, value=result
                    )
                    record_metadata = future.get(timeout=10)
                except kafka.errors.KafkaError as e:
                    log.error(f"unable to send judgement result to kafka")
                    log.debug(f"{type(record_metadata)}: {record_metadata}")  # type: ignore
                    log.debug(f"{type(e)}: {e}")
                    log.info("ignoring task due to error")
                    continue
                log.info("judgement result sent")

                log.info("acknowledging task completion")
                # channel.basic_ack(method.delivery_tag)  # type: ignore
                input('continue?')
        except KeyboardInterrupt:
            print("Interrupted, canceling channel")
            channel.cancel()
