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

import generated.proto.common.WeaponFireProto;
import mil.arl.gift.common.ta.state.BurstDescriptor;
import mil.arl.gift.common.ta.state.EntityIdentifier;
import mil.arl.gift.common.ta.state.EventIdentifier;
import mil.arl.gift.common.ta.state.WeaponFire;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf WeaponFire.
 * 
 * @author cpolynice
 *
 */
public class WeaponFireProtoCodec implements ProtoCodec<WeaponFireProto.WeaponFire, WeaponFire> {

    /* Codec that will be used to convert to/from a protobuf BurstDescriptor. */
    private static final BurstDescriptorProtoCodec burstCodec = new BurstDescriptorProtoCodec();

    /* Codec that will be used to convert to/from a protobuf
     * EntityIdentifier. */
    private static final EntityIdentifierProtoCodec entityCodec = new EntityIdentifierProtoCodec();

    /* Codec that will be used to convert to/from a protobuf EventIdentifier. */
    private static final EventIdentifierProtoCodec eventCodec = new EventIdentifierProtoCodec();

    /* Codec that will be used to convert to/from a protobuf Vector3D. */
    private static final Vector3DProtoCodec vectorCodec = new Vector3DProtoCodec();

    @Override
    public WeaponFire convert(WeaponFireProto.WeaponFire protoObject) {
        if (protoObject == null) {
            return null;
        }

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
        BurstDescriptor burstDescriptor = protoObject.hasBurstDesc() ? burstCodec.convert(protoObject.getBurstDesc())
                : null;

        return new WeaponFire(firingEntityID, targetEntityID, munitionID, eventID, velocity, location, burstDescriptor);
    }

    @Override
    public WeaponFireProto.WeaponFire map(WeaponFire commonObject) {
        if (commonObject == null) {
            return null;
        }

        WeaponFireProto.WeaponFire.Builder builder = WeaponFireProto.WeaponFire.newBuilder();

        Optional.ofNullable(eventCodec.map(commonObject.getEventID())).ifPresent(builder::setEventId);
        Optional.ofNullable(entityCodec.map(commonObject.getFiringEntityID())).ifPresent(builder::setFiringEntityId);
        Optional.ofNullable(entityCodec.map(commonObject.getMunitionID())).ifPresent(builder::setMunitionId);
        Optional.ofNullable(entityCodec.map(commonObject.getTargetEntityID())).ifPresent(builder::setTargetEntityId);
        Optional.ofNullable(vectorCodec.map(commonObject.getLocation())).ifPresent(builder::setLocation);
        Optional.ofNullable(vectorCodec.map(commonObject.getVelocity())).ifPresent(builder::setVelocity);
        Optional.ofNullable(burstCodec.map(commonObject.getBurstDescriptor())).ifPresent(builder::setBurstDesc);

        return builder.build();
    }

}
