package com.air6500.connector.producer;

import common.Common;
import entity.Entity;
import header.HeaderOuterClass;
import kinematics.KinematicsOuterClass;
import position.PositionOuterClass;

import java.util.Random;

public class EntityMessageGenerator
{
    private static int NEXT_ID = 0;
    private static Random RANDOM_COORD_GENERATOR = new Random();

    private int getNextId()
    {
        NEXT_ID++;
        return NEXT_ID;
    }

    public String nextUuid()
    {
        return String.format("Uuid:%09d", getNextId());
    }

    private double nextX()
    {
        return -2000.0 + (2000.0 - (-2000.0)) * RANDOM_COORD_GENERATOR.nextDouble();
    }

    private double nextY()
    {
        return -2000.0 + (2000.0 - (-2000.0)) * RANDOM_COORD_GENERATOR.nextDouble();
    }

    public Entity.EntityMessage nextEntityMessage()
    {
        String key = nextUuid();
        Common.Id idService = Common.Id.newBuilder()
            .setName("service")
            .setUuid(Common.Uuid.newBuilder().setValue("uuid:000000999").build())
            .build();
        Common.Id idSystem = Common.Id.newBuilder()
            .setName("system")
            .setUuid(Common.Uuid.newBuilder().setValue("uuid:000000998").build())
            .build();
        HeaderOuterClass.Header header = HeaderOuterClass.Header.newBuilder()
            .setService(idService)
            .setSystem(idSystem)
            .build();

        Entity.EntityMessage msgEntity = Entity.EntityMessage.newBuilder()
            .setHeader(header)
            .addData
                (
                    Entity.EntityData.newBuilder()
                        .setId(Common.Id.newBuilder().setName("new entity").setUuid(Common.Uuid.newBuilder().setValue(nextUuid()).build()))
                        .setKinematics
                            (
                                KinematicsOuterClass.Kinematics.newBuilder().setPosition
                                    (
                                        PositionOuterClass.Position.newBuilder().setCartesian
                                            (
                                                PositionOuterClass.CartesianPosition.newBuilder().setX(nextX()).setY(nextY()).setZ(0.0).build()
                                            ).build()
                                    ).build()
                            )
                        .build()
                )
            .addData
                (
                    Entity.EntityData.newBuilder()
                        .setId(Common.Id.newBuilder().setName("new entity").setUuid(Common.Uuid.newBuilder().setValue(nextUuid()).build()))
                        .setKinematics
                            (
                                KinematicsOuterClass.Kinematics.newBuilder().setPosition
                                    (
                                        PositionOuterClass.Position.newBuilder().setCartesian
                                            (
                                                PositionOuterClass.CartesianPosition.newBuilder().setX(nextX()).setY(nextX()).setZ(0.0).build()
                                            ).build()
                                    ).build()
                            )
                        .build()
                )
            .addData
                (
                    Entity.EntityData.newBuilder()
                        .setId(Common.Id.newBuilder().setName("new entity").setUuid(Common.Uuid.newBuilder().setValue(nextUuid()).build()))
                        .setKinematics
                            (
                                KinematicsOuterClass.Kinematics.newBuilder().setPosition
                                    (
                                        PositionOuterClass.Position.newBuilder().setCartesian
                                            (
                                                PositionOuterClass.CartesianPosition.newBuilder().setX(nextX()).setY(nextY()).setZ(0.0).build()
                                            ).build()
                                    ).build()
                            )
                        .build()
                )
            .build();

        return msgEntity;
    }
}
