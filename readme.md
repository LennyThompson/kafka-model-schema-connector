# Kafka Model Schema Connector

Simple producer/consumer pair for kafka tests.

## Producer

The *com.air6500.connector.producer.Main* will simply generate random *EntityMessage* messages into the kafka topic defined in the config.

# Consumer

The *com.air6500.connector.consumer.Main* will simply consume all messages on the configured topic as EntityMessage messages. Anything else will fail to deserialise.

## Connection Test

Uses the AdminClient to test the existence of a kafka broker by waiting on a timeout...

There is no simple way of detecting if a kafka client can or cant connect to a kafka broker.

TODO - add 'offline' mode, where producer pushes messages to say mongo??? and sends any messages once connection is established.

## Config

Currently very simple... same schema for producer and consumer, which looks like

```json
{
  "bootstraps": ["localhost:9092"],
  "topic": "test-entity-java-producer",
  "producer-rate": 500,
  "consumer-group": "test-entity-java-consumer"
}
```

* _bootstraps_ is an array of kafka broker uris
* _topic_ is the topic to produce/consume to
* _producer-rate_ is the milliseconds between messages for the producer (ignored by the consumer)
* _consumer-group_ is the group the consumer will use (required by the java *Consumer*)
* _schema-registry_ is the uri of the kafka schema-registry instance, and if present the producer or consumer will use the _KafkaProtobufSerializer_, or _KafkaProtobufDeserializer_, to produce and consume messages. Note this is not compatible with other implementations of kafka producers and consumers (c++, dart, rust etc.) but is directly compatible with ksql, and other confluent components.

## Logging

Uses log4j wrapped by slf4j.

By default will log to console and a rolling appender, with more or less the same content.

Update the log4j2.json, and rebuild if you want to change...

TODO - add config for logging level.