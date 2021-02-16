package com.air6500.connector.utils;

import com.air6500.connector.Main;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Collectors;

public class Config
{
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class.getName());
    private static final String PRODUCER = "producer";
    private static final String CONSUMER = "consumer";

    @SerializedName("type")
    private String m_strType;
    @SerializedName("bootstraps")
    private String[] m_listBootstraps;
    @SerializedName("topic")
    private String m_strTopic;
    @SerializedName("schema-registry")
    private String m_strSchemaRegistry;
    @SerializedName("producer-rate")
    private int m_nProducerRate;
    @SerializedName("consumer-group")
    private String m_strConsumerGroup;

    public Config()
    {}

    public String getType()
    {
        return m_strType;
    }
    public String[] getBootstraps()
    {
        return m_listBootstraps;
    }
    public String getTopic()
    {
        return m_strTopic;
    }
    public String getSchemaReistry()
    {
        return m_strSchemaRegistry;
    }
    public int getProducerRate()
    {
        return m_nProducerRate;
    }
    public String getConsumerGroup()
    {
        return m_strConsumerGroup;
    }

    public String getBootstrapsString()
    {
        return Arrays.stream(m_listBootstraps).collect(Collectors.joining(","));
    }

    public boolean isValid()
    {
        return getIsProducerConfig() || getIsConsumerConfig();
    }

    public boolean getIsProducerConfig()
    {
        return m_strType != null && !m_strType.isEmpty() && m_strType.toLowerCase().compareTo(PRODUCER) == 0;
    }

    public boolean getIsConsumerConfig()
    {
        return m_strType != null && !m_strType.isEmpty() && m_strType.toLowerCase().compareTo(CONSUMER) == 0;
    }

    public static Config init(String strConfigPath)
    {
        LOGGER.debug("Config file: " + strConfigPath);
        if(strConfigPath != null && !strConfigPath.isEmpty())
        {
            if(Files.exists(Paths.get(strConfigPath)))
            {
                LOGGER.debug("Config file exists, deserialising json");
                try
                {
                    String strJson = new String(Files.readAllBytes(Paths.get(strConfigPath)));
                    return new Gson().fromJson(strJson, Config.class);
                }
                catch (IOException e)
                {
                    LOGGER.error(e.getMessage());
                }
            }
        }
        LOGGER.error("Failed to deserialise from config file...");
        return  new Config();
    }
}

