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
import com.google.protobuf.Int64Value;

import generated.proto.common.StopFreezeProto;
import mil.arl.gift.common.ta.state.StopFreeze;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf StopFreeze state.
 * 
 * @author cpolynice
 *
 */
public class StopFreezeProtoCodec implements ProtoCodec<StopFreezeProto.StopFreeze, StopFreeze> {

    @Override
    public StopFreeze convert(StopFreezeProto.StopFreeze protoObject) {
        if (protoObject == null) {
            return null;
        }

        long realWorldTime = protoObject.hasRealTime() ? protoObject.getRealTime().getValue() : 0;
        int reason = protoObject.hasReason() ? protoObject.getReason().getValue() : 0;
        int frozenBehavior = protoObject.hasFrozenBehavior() ? protoObject.getFrozenBehavior().getValue() : 0;
        long requestID = protoObject.hasRequestId() ? protoObject.getRequestId().getValue() : 0;

        return new StopFreeze(realWorldTime, reason, frozenBehavior, requestID);
    }

    @Override
    public StopFreezeProto.StopFreeze map(StopFreeze commonObject) {
        if (commonObject == null) {
            return null;
        }
        
        StopFreezeProto.StopFreeze.Builder builder = StopFreezeProto.StopFreeze.newBuilder();
        
        builder.setRealTime(Int64Value.of(commonObject.getRealWorldTime()));
        builder.setRequestId(Int64Value.of(commonObject.getRequestID()));
        Optional.ofNullable(commonObject.getReason()).ifPresent(reason -> {
            builder.setReason(Int32Value.of(reason));
        });
        Optional.ofNullable(commonObject.getFrozenBehavior()).ifPresent(behavior -> {
            builder.setFrozenBehavior(Int32Value.of(behavior));
        });

        return builder.build();
    }
}
