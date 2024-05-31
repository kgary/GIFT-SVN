/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.Optional;

import com.google.protobuf.StringValue;

import generated.proto.common.EntityMarkingProto;
import mil.arl.gift.common.ta.state.EntityMarking;
import mil.arl.gift.net.proto.ProtoCodec;


/**
 * This class is responsible for protobuf encoding/decoding an Entity Marking
 * instance.
 * 
 * @author cpolynice
 */
public class EntityMarkingProtoCodec implements ProtoCodec<EntityMarkingProto.EntityMarking, EntityMarking> {

    @Override
    public EntityMarking convert(EntityMarkingProto.EntityMarking protoObject) {
        if (protoObject == null) {
            return null;
        }
        String entityCharacterSet, entityMarking, giftDisplayName;

        entityCharacterSet = protoObject.hasEntityCharSet() ? protoObject.getEntityCharSet().getValue() : null;
        entityMarking = protoObject.hasEntityMarking() ? protoObject.getEntityMarking().getValue() : null;
        giftDisplayName = protoObject.hasGiftDisplayName() ? protoObject.getGiftDisplayName().getValue() : null;

        return new EntityMarking(entityCharacterSet, entityMarking, giftDisplayName);
    }

    @Override
    public EntityMarkingProto.EntityMarking map(EntityMarking commonObject) {
        if (commonObject == null) {
            return null;
        }

        EntityMarkingProto.EntityMarking.Builder builder = EntityMarkingProto.EntityMarking.newBuilder();

        Optional.ofNullable(commonObject.getEntityCharacterSet()).ifPresent(charSet -> {
            builder.setEntityCharSet(StringValue.of(charSet));
        });

        Optional.ofNullable(commonObject.getEntityMarking()).ifPresent(marking -> {
            builder.setEntityMarking(StringValue.of(marking));
        });

        Optional.ofNullable(commonObject.getGiftDisplayName()).ifPresent(displayName -> {
            builder.setGiftDisplayName(StringValue.of(displayName));
        });

        return builder.build();
    }

}
