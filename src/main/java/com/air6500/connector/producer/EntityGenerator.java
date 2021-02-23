package org.acme.kafka.streams.producer.generator;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import common.Common;
import entity.Entity;
import entity.Entity.EntityData;
import header.HeaderOuterClass;
import io.smallrye.mutiny.Multi;
import io.smallrye.reactive.messaging.kafka.Record;
import kinematics.KinematicsOuterClass;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.jboss.logging.Logger;
import position.PositionOuterClass;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class EntityGenerator
{
    private static int NEXT_ID = 0;
    private static Random RANDOM_COORD_GENERATOR = new Random();
    private Map<String, EntityData> m_mapEntityTrack;
    private Iterator<Map.Entry<String, EntityData>> m_iterEntity;

    private static final Logger LOG = Logger.getLogger(EntityGenerator.class);

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

    private double nextZ()
    {
        return 0.0;
    }

    private void initTrackMap()
    {
        m_mapEntityTrack = IntStream.range(1, 50)
            .mapToObj
                (
                    (index) ->
                    {
                        String uuid = nextUuid();
                        return EntityData.newBuilder()
                            .setId(Common.Id.newBuilder().setName(uuid)
                            .setUuid(Common.Uuid.newBuilder().setValue(uuid).build()))
                            .setKinematics
                                (
                                    KinematicsOuterClass.Kinematics.newBuilder().setPosition
                                        (
                                            PositionOuterClass.Position.newBuilder().setCartesian
                                                (
                                                    PositionOuterClass.CartesianPosition.newBuilder().setX(nextX()).setY(nextY()).setZ(nextZ()).build()
                                                ).build()
                                        ).build()
                                )
                            .build();
                    }
                )
        .collect
            (
                Collectors.toMap
                    (
                        data -> data.getId().getUuid().toString(),
                        data -> data
                    )
            );
        m_iterEntity = m_mapEntityTrack.entrySet().iterator();
    }

    private EntityData nextEntity()
    {
        if(m_mapEntityTrack == null)
        {
            initTrackMap();
        }

        if(!m_iterEntity.hasNext())
        {
            m_iterEntity = m_mapEntityTrack.entrySet().iterator();
        }
        EntityData entity = m_iterEntity.next().getValue();
        Common.Uuid uuid = entity.getId().getUuid();
        m_mapEntityTrack.put
            (
                uuid.getValue(),
                EntityData.newBuilder()
                    .setId(Common.Id.newBuilder().setName(uuid.getValue())
                    .setUuid(uuid))
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
                    .build());
        return entity;
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

        Entity.EntityMessage.Builder buildEntity = Entity.EntityMessage.newBuilder();
        buildEntity
            .setHeader(header);
        IntStream.range(1, 10)
            .forEach
                (
                    (index) ->
                    {
                        buildEntity.addData(nextEntity());
                    }
                );

        return buildEntity.build();
    }

    @Outgoing("entity-data-quarkus")
    public Multi<Record<String, Entity.EntityMessage>> generateEntities()
    {
        return Multi.createFrom().ticks()
            .every(Duration.ofMillis(500))
            .onOverflow().drop()
            .map
            (
                tick ->
                {
                    String strUuid = nextUuid();
                    Entity.EntityMessage msgEntity = nextEntityMessage();
                    LOG.infov("entity: {0}, tracking: {1}", strUuid, msgEntity.getDataList().stream().map(data -> data.getId().getUuid().getValue()).collect(Collectors.joining(",")));
                    return Record.of(nextUuid(), msgEntity);
                }
            );
    }

    @Outgoing("tracked-entities")
    public Multi<Record<String, String>> trackedEntities()
    {
        return Multi.createFrom().items
        (
            m_mapEntityTrack.entrySet().stream()
            .map
            (
                entry -> Record.of
                (
                    entry.getKey(),
                    "{ \"id\" : "
                        + entry.getValue().getId().getUuid()
                        + ", \"position\" : { \"x\": "
                        + entry.getValue().getKinematics().getPosition().getCartesian().getX()
                        + ", \"y\": "
                        + entry.getValue().getKinematics().getPosition().getCartesian().getY()
                        + ", \"z\": "
                        + entry.getValue().getKinematics().getPosition().getCartesian().getZ()
                        + "} }"
                )
            )
        );
    }
}

