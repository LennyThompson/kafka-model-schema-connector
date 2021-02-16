# Kafka Model Schema Connector

Simple producer/consumer pair for kafka tests.

## Producer

The *com.air6500.connector.Main* will simply generate random *EntityMessage* messages into the kafka topic defined in the config.

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
  "type": "producer",
  "bootstraps": ["localhost:9092"],
  "topic": "test-entity-java-producer",
  "producer-rate": 500,
  "consumer-group": "test-entity-java-consumer"
}
```

* _type_ is the run time type of the app (can be one of "producer" or "consumer")
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

## Docker

Add a docker-compose to dockerise the packaged build running as either a producer or consumer.

This meant adjusting the docker settings for at least my local kafka environment.

In order to produce or consume to/from kafka in docker the producer or consumer docker must be in the same network as the kafka broker, which means applying the following to any existing kafka runtime.

Firstly a named docker network must be created (for instance)

```shell
docker network create kafka-network
```

Then the existing kafka docker-compose will need to be updated to use the network ```docker down``` if needed.

```yaml
networks:
  external:
    name: kafka-network
```
But dont docker up just yet...

Then the broker (or brokers) listening configuration will have to be altered.

```yaml
  broker:
    .
    hostname: broker
    .
    .
    .
    ports:
      - "9092:9092"
      # add this (for each broker) with a port that either localhost or docker container can access
      - "19092:19092" 
      - "9101:9101"
    .
    .
    environment:
      .
      # Add new protocol map entry (for localhost for instance - you can all it whatever you like)
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT,PLAINTEXT_LOCALHOST:PLAINTEXT
      # and now associate the protocol with local host and the port declared above, and associate the other with the kafka-network name
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://broker:29092,PLAINTEXT_HOST://broker:9092,PLAINTEXT_LOCALHOST://localhost:19092

```

Now the kafka broker(s) will be listening on localhost:19092 for connections from non dockerised clients, and broker:9092, for dockerised clients. What could be simpler?

Now you can docker up the kafka docker compose.

In the configuration for the producer you will need ot update the bootstraps depending on the host of the app.

For a docker host (as in the _docker-compose.yml_) use 

```json
{
  .,
  .
  "bootstraps": [
    "broker:9092"
  ],
  .
  .
}
```

This will be the same for producers as well as consumers.

And for non docerised instances, use 

```json
{
  .,
  .
  "bootstraps": [
    "localhost:19092"
  ],
  .
  .
}
```

Keep in mind that the naming must be consistent between the docker-compose'd configurations (ie network name, broker name and ports).