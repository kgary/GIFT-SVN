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

import generated.proto.common.LtiUserIdProto;
import mil.arl.gift.common.lti.LtiUserId;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * The LtiUserIdProtoCodec class is the codec repsonsible for protobuf
 * encoding/decoding the LtiUserId class.
 * 
 * @author cpolynice
 *
 */
public class LtiUserIdProtoCodec implements ProtoCodec<LtiUserIdProto.LtiUserID, LtiUserId> {

    @Override
    public LtiUserId convert(LtiUserIdProto.LtiUserID protoObject) {
        if (protoObject == null) {
            return null;
        }
        
        String consumerKey = protoObject.hasConsumerKey() ? protoObject.getConsumerKey().getValue() : null;
        String consumerId = protoObject.hasConsumerId() ? protoObject.getConsumerId().getValue() : null;

        return new LtiUserId(consumerKey, consumerId);
    }

    @Override
    public LtiUserIdProto.LtiUserID map(LtiUserId commonObject) {
        if (commonObject == null) {
            return null;
        }

        LtiUserIdProto.LtiUserID.Builder builder = LtiUserIdProto.LtiUserID.newBuilder();
        
        Optional.ofNullable(commonObject.getConsumerKey()).ifPresent(key ->
        {
        builder.setConsumerKey(StringValue.of(key));
        });
        
        Optional.ofNullable(commonObject.getConsumerId()).ifPresent(id ->
        {
        builder.setConsumerId(StringValue.of(id));
        });

        return builder.build();
    }
}
