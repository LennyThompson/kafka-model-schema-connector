package com.air6500.connector.consumer;

import com.air6500.connector.utils.Config;
import common.Common;
import entity.Entity;
import header.HeaderOuterClass;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializer;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializer;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializerConfig;
import kinematics.KinematicsOuterClass;
import org.apache.commons.cli.*;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringDeserializer;
import position.PositionOuterClass;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class Main
{
    public static void main(String[] args)
    {
        Options options = new Options().addOption("c", "config", true, "Path to config json file");

        CommandLineParser cmdLineParser = new DefaultParser();
        try
        {
            CommandLine cmdLine = cmdLineParser.parse(options, args);
            String strConfigPath = options.getOption("config").getValue();
            Config config = Config.init(strConfigPath);
            if (config.getBootstraps().length > 0)
            {
                String strBootstraps = Arrays.stream(config.getBootstraps()).collect(Collectors.joining(","));
                Properties props = new Properties();
                props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, strBootstraps);
                props.put(ConsumerConfig.GROUP_ID_CONFIG, config.getConsumerGroup());
                props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
                props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, EntityDeserializer.class);
                if(config.getSchemaReistry() != null && !config.getSchemaReistry().isEmpty())
                {
                    props.put(KafkaProtobufSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081");
                }
                props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

                final Consumer<String, Entity.EntityMessage> consumer = new KafkaConsumer<String, Entity.EntityMessage>(props);
                consumer.subscribe(Arrays.asList(config.getTopic()));

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
                } finally
                {
                    consumer.close();
                }
            }
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }
    }
}
