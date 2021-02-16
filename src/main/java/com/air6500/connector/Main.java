package com.air6500.connector;

import com.air6500.connector.consumer.EntityDeserializer;
import com.air6500.connector.producer.EntityMessageGenerator;
import com.air6500.connector.producer.EntitySerializer;
import com.air6500.connector.utils.Config;
import com.air6500.connector.utils.KafkaConnectionTester;
import entity.Entity;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializer;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializer;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializerConfig;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class Main
{
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class.getName());

    public static void main(String[] args) throws InterruptedException
    {
        LOGGER.info("**** Running as kafka producer ****");
        LOGGER.info("Arg count: " + args.length + ", Args = " + Arrays.stream(args).collect(Collectors.joining(" ")));
        Options options = new Options().addOption("c", "config", true, "Path to config json file");

        CommandLineParser cmdLineParser = new DefaultParser();
        try
        {
            CommandLine cmdLine = cmdLineParser.parse(options, args);
            String strConfigPath = cmdLine.getOptionValue("config");
            LOGGER.info("Command line: size = " + cmdLine.getArgList().size() + ", config = " + cmdLine.getOptionValue("config"));

            Config config = Config.init(strConfigPath);
            if(config.isValid())
            {
                LOGGER.info("Valid configuration assumed");

                LOGGER.info("bootstraps: " + config.getBootstrapsString());
                LOGGER.info("topic: " + config.getTopic());
                LOGGER.info("rate: " + config.getProducerRate());

                if(config.getIsProducerConfig())
                {
                    doProducer(config);
                }
                else if(config.getIsConsumerConfig())
                {
                    doConsumer(config);
                }
            }
        }
        catch (ParseException e)
        {
            LOGGER.error(e.getMessage());
        }

    }

    private static boolean doProducer(Config config)
    {
        LOGGER.info("Running as producer");
        if(config.getBootstraps().length > 0)
        {
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

                try
                {
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
                }
                catch (InterruptedException e)
                {
                    LOGGER.error(e.getMessage());
                }
                finally
                {
                    producer.close();
                }
            }

        }
        return false;
    }

    private static boolean doConsumer(Config config)
    {
        LOGGER.info("Running as consumer");
        if(config.getBootstraps().length > 0)
        {
            if (KafkaConnectionTester.checkKafkaConnection(config))
            {
                Properties props = new Properties();
                props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getBootstrapsString());
                props.put(ConsumerConfig.GROUP_ID_CONFIG, config.getConsumerGroup());
                props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
                if (config.getSchemaReistry() != null && !config.getSchemaReistry().isEmpty())
                {
                    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaProtobufDeserializer.class);
                    props.put(KafkaProtobufSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, config.getSchemaReistry());
                } else
                {
                    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, EntityDeserializer.class);
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
                            LOGGER.info(String.format("offset = %d, key = %s, value = %s \n", record.offset(), record.key(), record.value()));
                        }
                    }
                }
                finally
                {
                    consumer.close();
                }
            }

        }
        return false;
    }

}
