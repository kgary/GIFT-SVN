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

import generated.proto.common.LtiGetUserRequestProto;
import mil.arl.gift.common.lti.LtiGetUserRequest;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf
 * LtiGetUserRequest.
 * 
 * @author cpolynice
 *
 */
public class LtiGetUserRequestProtoCodec
        implements ProtoCodec<LtiGetUserRequestProto.LtiGetUserRequest, LtiGetUserRequest> {

    @Override
    public LtiGetUserRequest convert(LtiGetUserRequestProto.LtiGetUserRequest protoObject) {
        if (protoObject == null) {
            return null;
        }

        String consumerKey = protoObject.hasConsumerKey() ? protoObject.getConsumerKey().getValue() : null;
        String consumerId = protoObject.hasConsumerId() ? protoObject.getConsumerId().getValue() : null;

        return new LtiGetUserRequest(consumerKey, consumerId);
    }

    @Override
    public LtiGetUserRequestProto.LtiGetUserRequest map(LtiGetUserRequest commonObject) {
        if (commonObject == null) {
            return null;
        }
        
        LtiGetUserRequestProto.LtiGetUserRequest.Builder builder = LtiGetUserRequestProto.LtiGetUserRequest.newBuilder();
        
        Optional.ofNullable(commonObject.getConsumerKey()).ifPresent(key -> {
            builder.setConsumerKey(StringValue.of(key));
        });
        Optional.ofNullable(commonObject.getConsumerId()).ifPresent(id -> {
            builder.setConsumerId(StringValue.of(id));
        });
        
        return builder.build();
    }

}
