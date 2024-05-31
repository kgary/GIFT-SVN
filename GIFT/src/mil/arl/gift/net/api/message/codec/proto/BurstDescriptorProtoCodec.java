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

import generated.proto.common.BurstDescriptorProto;
import mil.arl.gift.common.ta.state.BurstDescriptor;
import mil.arl.gift.common.ta.state.EntityType;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a BurstDescriptor message.
 * 
 * @author cpolynice
 *
 */
public class BurstDescriptorProtoCodec implements ProtoCodec<BurstDescriptorProto.BurstDescriptor, BurstDescriptor> {

    /* Codec that will be used to convert to/from an EntityType message. */
    private static EntityTypeProtoCodec codec = new EntityTypeProtoCodec();

    @Override
    public BurstDescriptor convert(BurstDescriptorProto.BurstDescriptor protoObject) {
        if (protoObject == null) {
            return null;
        }

        int fuse = protoObject.hasFuse() ? protoObject.getFuse().getValue() : 0;
        int quantity = protoObject.hasQuantity() ? protoObject.getQuantity().getValue() : 0;
        int rate = protoObject.hasRate() ? protoObject.getRate().getValue() : 0;
        int warhead = protoObject.hasWarhead() ? protoObject.getWarhead().getValue() : 0;
        EntityType munitionType = protoObject.hasMunitionType() ? codec.convert(protoObject.getMunitionType()) : null;

        return new BurstDescriptor(munitionType, warhead, fuse, quantity, rate);
    }

    @Override
    public BurstDescriptorProto.BurstDescriptor map(BurstDescriptor commonObject) {
        if (commonObject == null) {
            return null;
        }

        BurstDescriptorProto.BurstDescriptor.Builder builder = BurstDescriptorProto.BurstDescriptor.newBuilder();

        builder.setFuse(Int32Value.of(commonObject.getFuse()));
        builder.setQuantity(Int32Value.of(commonObject.getQuantity()));
        builder.setRate(Int32Value.of(commonObject.getRate()));
        builder.setWarhead(Int32Value.of(commonObject.getWarhead()));
        Optional.ofNullable(codec.map(commonObject.getMunitionType())).ifPresent(builder::setMunitionType);

        return builder.build();
    }
}
