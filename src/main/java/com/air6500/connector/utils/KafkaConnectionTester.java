package com.air6500.connector.utils;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class KafkaConnectionTester
{
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaConnectionTester.class.getName());

    public static boolean checkKafkaConnection(Config config)
    {
        Properties properties = new Properties();
        properties.put("bootstrap.servers", config.getBootstrapsString());
        properties.put("connections.max.idle.ms", 10000);
        properties.put("request.timeout.ms", 5000);
        try (AdminClient client = KafkaAdminClient.create(properties))
        {
            ListTopicsResult topics = client.listTopics();
            Set<String> topicNames = topics.names().get();
            if (topicNames.isEmpty())
            {
                LOGGER.info("No kafka topics defined on broker");
            }
            else
            {
                LOGGER.info("Broker topics:");
                topicNames.stream().forEach(topic -> LOGGER.info("- " + topic));
            }
            return true;
        }
        catch (InterruptedException | ExecutionException exc)
        {
            LOGGER.error(exc.getMessage());
            LOGGER.error("Unable to connect to a kafka broker - check kafka is running on " + config.getBootstrapsString());
        }
        return false;

    }
}
