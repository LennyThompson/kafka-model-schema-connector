package com.air6500.connector.utils;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Collectors;

public class Config
{
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

    public static Config init(String strConfigPath)
    {
        if(strConfigPath != null && !strConfigPath.isEmpty())
        {
            if(Files.exists(Paths.get(strConfigPath)))
            {
                try
                {
                    String strJson = new String(Files.readAllBytes(Paths.get(strConfigPath)));
                    return new Gson().fromJson(strJson, Config.class);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
        return  new Config();
    }
}

