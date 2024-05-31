/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.Optional;

import javax.vecmath.Vector3d;

import generated.proto.common.DetonationProto;
import mil.arl.gift.common.enums.DetonationResultEnum;
import mil.arl.gift.common.ta.state.BurstDescriptor;
import mil.arl.gift.common.ta.state.Detonation;
import mil.arl.gift.common.ta.state.EntityIdentifier;
import mil.arl.gift.common.ta.state.EntityType;
import mil.arl.gift.common.ta.state.EventIdentifier;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf Detonation
 * instance.
 * 
 * @author cpolynice
 *
 */
public class DetonationProtoCodec implements ProtoCodec<DetonationProto.Detonation, Detonation> {

    /**
     * The {@link BurstDescriptor} to use when decoding a {@link Detonation}
     * payload if one was not provided in the {@link JSONObject}.
     */
    private static final BurstDescriptor DEFAULT_BURST_DESCRIPTOR = new BurstDescriptor(
            new EntityType(0, 0, 0, 0, 0, 0, 0), 0, 0, 0, 0);

    /* Codec that will be used to convert to/from a protobuf enum. */
    private static AbstractEnumObjectProtoCodec enumCodec = new AbstractEnumObjectProtoCodec();

    /* Codec that will be used to convert to/from a BurstDescriptor. */
    private static BurstDescriptorProtoCodec burstCodec = new BurstDescriptorProtoCodec();

    /* Codec that will be used to convert to/from an EntityIdentifier. */
    private static EntityIdentifierProtoCodec entityCodec = new EntityIdentifierProtoCodec();

    /* Codec that will be used to convert to/from an EventIdentifier. */
    private static EventIdentifierProtoCodec eventCodec = new EventIdentifierProtoCodec();

    /* Codec that will be used to convert to/from a Vector3D. */
    private static Vector3DProtoCodec vectorCodec = new Vector3DProtoCodec();

    @Override
    public Detonation convert(DetonationProto.Detonation protoObject) {
        if (protoObject == null) {
            return null;
        }
        
        DetonationResultEnum detonationResult = protoObject.hasDetonationResult() ? (DetonationResultEnum) enumCodec.convert(protoObject.getDetonationResult()) : null;
        EventIdentifier eventID = protoObject.hasEventId() ? eventCodec.convert(protoObject.getEventId()) : null;
        EntityIdentifier firingEntityID = protoObject.hasFiringEntityId()
                ? entityCodec.convert(protoObject.getFiringEntityId())
                : null;
        EntityIdentifier munitionID = protoObject.hasMunitionId() ? entityCodec.convert(protoObject.getMunitionId())
                : null;
        EntityIdentifier targetEntityID = protoObject.hasTargetEntityId()
                ? entityCodec.convert(protoObject.getTargetEntityId())
                : null;
        Vector3d location = protoObject.hasLocation() ? vectorCodec.convert(protoObject.getLocation()) : null;
        Vector3d velocity = protoObject.hasVelocity() ? vectorCodec.convert(protoObject.getVelocity()) : null;
        BurstDescriptor burstDescriptor = protoObject.hasBurstDescriptor()
                ? burstCodec.convert(protoObject.getBurstDescriptor())
                : DEFAULT_BURST_DESCRIPTOR;

        return new Detonation(firingEntityID, targetEntityID, munitionID, eventID, velocity, location, burstDescriptor,
                detonationResult);
    }

    @Override
    public DetonationProto.Detonation map(Detonation commonObject) {
        if (commonObject == null) {
            return null;
        }

        DetonationProto.Detonation.Builder builder = DetonationProto.Detonation.newBuilder();

        Optional.ofNullable(enumCodec.map(commonObject.getDetonationResult())).ifPresent(builder::setDetonationResult);
        Optional.ofNullable(eventCodec.map(commonObject.getEventID())).ifPresent(builder::setEventId);
        Optional.ofNullable(entityCodec.map(commonObject.getFiringEntityID())).ifPresent(builder::setFiringEntityId);
        Optional.ofNullable(vectorCodec.map(commonObject.getLocation())).ifPresent(builder::setLocation);
        Optional.ofNullable(burstCodec.map(commonObject.getBurstDescriptor())).ifPresent(builder::setBurstDescriptor);
        Optional.ofNullable(entityCodec.map(commonObject.getMunitionID())).ifPresent(builder::setMunitionId);
        Optional.ofNullable(entityCodec.map(commonObject.getTargetEntityID())).ifPresent(builder::setTargetEntityId);
        Optional.ofNullable(vectorCodec.map(commonObject.getVelocity())).ifPresent(builder::setVelocity);
        return builder.build();
    }

}
