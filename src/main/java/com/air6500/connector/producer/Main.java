package com.air6500.connector.producer;

import common.Common;
import entity.Entity;
import header.HeaderOuterClass;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializer;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializerConfig;
import kinematics.KinematicsOuterClass;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import position.PositionOuterClass;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class Main
{
    public static void main(String[] args) throws InterruptedException
    {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, EntitySerializer.class);
        props.put(KafkaProtobufSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081");

        Producer<String, Entity.EntityMessage> producer = new KafkaProducer<String, Entity.EntityMessage>(props);

        String topic = "test-entity-java-producer-1";

        EntityMessageGenerator generator = new EntityMessageGenerator();

        while(true)
        {
            ProducerRecord<String, Entity.EntityMessage> record
                = new ProducerRecord<String, Entity.EntityMessage>(topic, generator.nextUuid(), generator.nextEntityMessage());
            Thread.sleep(500);
            try
            {
                producer.send(record).get();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            catch (ExecutionException e)
            {
                e.printStackTrace();
            }
        }
//        producer.close();
    }
}
