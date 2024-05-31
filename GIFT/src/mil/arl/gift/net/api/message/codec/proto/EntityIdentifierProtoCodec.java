/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.Optional;

import com.google.protobuf.Int32Value;

import generated.proto.common.EntityIdentifierProto;
import mil.arl.gift.common.ta.state.EntityIdentifier;
import mil.arl.gift.common.ta.state.SimulationAddress;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for protobuf encoding/decoding an Entity Identifier
 * instance.
 * 
 * @author cpolynice
 */
public class EntityIdentifierProtoCodec
        implements ProtoCodec<EntityIdentifierProto.EntityIdentifier, EntityIdentifier> {

    /* Codec that will be used to convert to/from a protobuf SimulationAddress
     * instance. */
    private final SimulationAddressProtoCodec simAddrCodec = new SimulationAddressProtoCodec();

    @Override
    public EntityIdentifier convert(EntityIdentifierProto.EntityIdentifier protoObject) {
        if (protoObject == null) {
            return null;
        }
        
        Integer entityId = null;
        SimulationAddress simAddr = null;
        
        if (protoObject.hasEntityId()) {
            entityId = protoObject.getEntityId().getValue();
        }
        
        if (protoObject.hasSimulationAddress()) {
            simAddr = simAddrCodec.convert(protoObject.getSimulationAddress());
        }
        
        return new EntityIdentifier(simAddr, entityId);
    }

    @Override
    public EntityIdentifierProto.EntityIdentifier map(EntityIdentifier commonObject) {
        if (commonObject == null) {
            return null;
        }
        
        EntityIdentifierProto.EntityIdentifier.Builder builder = EntityIdentifierProto.EntityIdentifier.newBuilder();
        
        Optional.ofNullable(commonObject.getEntityID()).ifPresent(entId -> {
            builder.setEntityId(Int32Value.of(entId));
        });

        Optional.ofNullable(simAddrCodec.map(commonObject.getSimulationAddress()))
                .ifPresent(builder::setSimulationAddress);

        return builder.build();
    }
}
