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

import generated.proto.common.RemoveEntityProto;
import mil.arl.gift.common.ta.state.EntityIdentifier;
import mil.arl.gift.common.ta.state.RemoveEntity;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf RemoveEntity.
 * 
 * @author cpolynice
 *
 */
public class RemoveEntityProtoCodec implements ProtoCodec<RemoveEntityProto.RemoveEntity, RemoveEntity> {

    /* Codec that will be used to convert to/from a protobuf
     * EntityIdentifier. */
    private static final EntityIdentifierProtoCodec codec = new EntityIdentifierProtoCodec();

    @Override
    public RemoveEntity convert(RemoveEntityProto.RemoveEntity protoObject) {
        if (protoObject == null) {
            return null;
        }

        EntityIdentifier originatingEntityID = protoObject.hasOriginatingEntityId()
                ? codec.convert(protoObject.getOriginatingEntityId())
                : null;
        EntityIdentifier receivingEntityID = protoObject.hasReceivingEntityId()
                ? codec.convert(protoObject.getReceivingEntityId())
                : null;
        int requestID = protoObject.hasRequestId() ? protoObject.getRequestId().getValue() : 0;

        return new RemoveEntity(originatingEntityID, receivingEntityID, requestID);
    }

    @Override
    public RemoveEntityProto.RemoveEntity map(RemoveEntity commonObject) {
        if (commonObject == null) {
            return null;
        }
        
        RemoveEntityProto.RemoveEntity.Builder builder = RemoveEntityProto.RemoveEntity.newBuilder();
        
        builder.setRequestId(Int32Value.of(commonObject.getRequestId()));
        Optional.ofNullable(codec.map(commonObject.getOriginatingId())).ifPresent(builder::setOriginatingEntityId);
        Optional.ofNullable(codec.map(commonObject.getReceivingId())).ifPresent(builder::setReceivingEntityId);
        
        return builder.build();
    }
}
