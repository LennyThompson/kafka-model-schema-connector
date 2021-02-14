package com.air6500.connector.consumer;

import common.Common;
import entity.Entity;
import header.HeaderOuterClass;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializer;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializer;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializerConfig;
import kinematics.KinematicsOuterClass;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringDeserializer;
import position.PositionOuterClass;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class Main
{
    public static void main(String[] args)
    {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "temp-consumer-2");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, EntityDeserializer.class);
        props.put(KafkaProtobufSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        String topic = "test-entity-dart-producer";
        final Consumer<String, Entity.EntityMessage> consumer = new KafkaConsumer<String, Entity.EntityMessage>(props);
        consumer.subscribe(Arrays.asList(topic));

        try
        {
            while (true)
            {
                ConsumerRecords<String, Entity.EntityMessage> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, Entity.EntityMessage> record : records)
                {
                    Entity.EntityMessage msg = record.value();
                    System.out.printf("offset = %d, key = %s, value = %s \n", record.offset(), record.key(), record.value());
                }
            }
        }
        finally
        {
            consumer.close();
        }
    }
}
