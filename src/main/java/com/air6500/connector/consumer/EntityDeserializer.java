package com.air6500.connector.consumer;

import com.google.protobuf.CodedInputStream;
import entity.Entity;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;

public class EntityDeserializer implements Deserializer<Entity.EntityMessage>
{

    @Override
    public Entity.EntityMessage deserialize(String topic, byte[] data)
    {
        Entity.EntityMessage entityMessage = null;
        try
        {
            entityMessage = Entity.EntityMessage.parseFrom(CodedInputStream.newInstance(data));

            return entityMessage;
        } catch (Exception e)
        {
            //ToDo
        }

        return null;
    }
}