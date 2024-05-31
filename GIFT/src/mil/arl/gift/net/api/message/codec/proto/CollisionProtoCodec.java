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

import generated.proto.common.CollisionProto;
import mil.arl.gift.common.ta.state.Collision;
import mil.arl.gift.common.ta.state.EntityIdentifier;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for convering to/from a Collision message.
 * 
 * @author cpolynice
 *
 */
public class CollisionProtoCodec implements ProtoCodec<CollisionProto.Collision, Collision> {

    /* Codec that will be used to convert to/from an EntityIdentifier. */
    private static EntityIdentifierProtoCodec codec = new EntityIdentifierProtoCodec();

    @Override
    public Collision convert(CollisionProto.Collision protoObject) {
        if (protoObject == null) {
            return null;
        }

        int collisionType = protoObject.hasCollisionType() ? protoObject.getCollisionType().getValue() : null;
        EntityIdentifier issuingID = protoObject.hasIssuingEntity() ? codec.convert(protoObject.getIssuingEntity())
                : null;
        EntityIdentifier collidingId = protoObject.hasCollidingEntity()
                ? codec.convert(protoObject.getCollidingEntity())
                : null;

        return new Collision(issuingID, collidingId, collisionType);
    }

    @Override
    public CollisionProto.Collision map(Collision commonObject) {
        if (commonObject == null) {
            return null;
        }

        CollisionProto.Collision.Builder builder = CollisionProto.Collision.newBuilder();

        Optional.ofNullable(commonObject.getCollisionType()).ifPresent(type -> {
            builder.setCollisionType(Int32Value.of(type));
        });
        Optional.ofNullable(codec.map(commonObject.getCollidingEntityID())).ifPresent(builder::setCollidingEntity);
        Optional.ofNullable(codec.map(commonObject.getIssuingEntityID())).ifPresent(builder::setIssuingEntity);

        return builder.build();
    }
}
