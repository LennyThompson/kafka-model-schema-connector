package com.air6500.connector.producer;

import com.air6500.connector.utils.Config;
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
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class Main
{
    public static void main(String[] args) throws InterruptedException
    {
        Options options = new Options().addOption("c", "config", true, "Path to config json file");

        CommandLineParser cmdLineParser = new DefaultParser();
        try
        {
            CommandLine cmdLine = cmdLineParser.parse(options, args);
            String strConfigPath = options.getOption("config").getValue();
            Config config = Config.init(strConfigPath);
            if(config.getBootstraps().length > 0)
            {
                String strBootstraps = Arrays.stream(config.getBootstraps()).collect(Collectors.joining(","));
                Properties props = new Properties();
                props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, strBootstraps);
                props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
                props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, EntitySerializer.class);
                if(config.getSchemaReistry() != null && !config.getSchemaReistry().isEmpty())
                {
                    props.put(KafkaProtobufSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081");
                }

                Producer<String, Entity.EntityMessage> producer = new KafkaProducer<String, Entity.EntityMessage>(props);

                EntityMessageGenerator generator = new EntityMessageGenerator();

                while(true)
                {
                    ProducerRecord<String, Entity.EntityMessage> record
                        = new ProducerRecord<String, Entity.EntityMessage>(config.getTopic(), generator.nextUuid(), generator.nextEntityMessage());
                    Thread.sleep(config.getProducerRate() == 0 ? config.getProducerRate() : 1000);
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
        catch (ParseException e)
        {
            e.printStackTrace();
        }

    }
}
