package com.air6500.connector.producer;

import com.air6500.connector.utils.Config;
import com.air6500.connector.utils.KafkaConnectionTester;
import entity.Entity;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializer;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializerConfig;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ConnectException;
import java.util.Arrays;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class Main
{
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class.getName());

    public static void main(String[] args) throws InterruptedException
    {
        LOGGER.info("**** Running as kafka producer ****");
        Options options = new Options().addOption("c", "config", true, "Path to config json file");

        CommandLineParser cmdLineParser = new DefaultParser();
        try
        {
            CommandLine cmdLine = cmdLineParser.parse(options, args);
            String strConfigPath = cmdLine.getOptionValue("config");
            Config config = Config.init(strConfigPath);
            if(config.getBootstraps().length > 0)
            {
                LOGGER.info("Valid configuration assumed");

                LOGGER.info("bootstraps: " + config.getBootstrapsString());
                LOGGER.info("topic: " + config.getTopic());
                LOGGER.info("rate: " + config.getProducerRate());

                if (KafkaConnectionTester.checkKafkaConnection(config))
                {
                    Properties props = new Properties();
                    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getBootstrapsString());
                    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
                    if (config.getSchemaReistry() != null && !config.getSchemaReistry().isEmpty())
                    {
                        LOGGER.info("Using the KafkaProtobufSerializer");
                        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaProtobufSerializer.class);
                        props.put(KafkaProtobufSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, config.getSchemaReistry());
                    } else
                    {
                        LOGGER.info("Using the EntitySerializer");
                        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, EntitySerializer.class);
                    }

                    Producer<String, Entity.EntityMessage> producer = new KafkaProducer<String, Entity.EntityMessage>(props);

                    EntityMessageGenerator generator = new EntityMessageGenerator();

                    long nProductionCount = 0;
                    while (true)
                    {
                        ProducerRecord<String, Entity.EntityMessage> record
                            = new ProducerRecord<String, Entity.EntityMessage>(config.getTopic(), generator.nextUuid(), generator.nextEntityMessage());
                        Thread.sleep(config.getProducerRate() == 0 ? config.getProducerRate() : 1000);
                        try
                        {
                            RecordMetadata metaData = producer.send(record).get();
                            LOGGER.debug("METADATA - " + metaData.toString());
                            ++nProductionCount;
                        } catch (InterruptedException e)
                        {
                            LOGGER.error(e.getMessage());
                        } catch (ExecutionException e)
                        {
                            LOGGER.error(e.getMessage());
                        }
                        if (nProductionCount != 0 && nProductionCount % 20 == 0)
                        {
                            LOGGER.info("Produced " + nProductionCount + " messages");
                        }
                    }
//        producer.close();
                }
            }
        }
        catch (ParseException e)
        {
            LOGGER.error(e.getMessage());
        }

    }
}
