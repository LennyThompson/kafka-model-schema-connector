package com.air6500.connector.producer;

import com.google.protobuf.CodedOutputStream;
import entity.Entity;
import org.apache.kafka.common.serialization.Serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class EntitySerializer implements Serializer<Entity.EntityMessage>
{

    @Override
    public byte[] serialize(String topic, Entity.EntityMessage data)
    {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        CodedOutputStream outputStream = CodedOutputStream.newInstance(byteStream);
        try
        {
            data.writeTo(outputStream);
            outputStream.flush();
            return byteStream.toByteArray();

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return null;
    }
}
