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
import com.google.protobuf.StringValue;

import generated.proto.common.LtiGetUserResponseProto.LtiGetUserResponse;
import mil.arl.gift.common.lti.LtiUserId;
import mil.arl.gift.common.lti.LtiUserRecord;
import mil.arl.gift.common.util.ProtobufConversionUtil;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf
 * LtiGetUserResponse.
 * 
 * @author cpolynice
 *
 */
public class LtiGetUserResponseProtoCodec implements ProtoCodec<LtiGetUserResponse, LtiUserRecord> {

    @Override
    public LtiUserRecord convert(LtiGetUserResponse protoObject) {
        if (protoObject == null) {
            return null;
        }

        int giftUserId = protoObject.hasGlobalUserId() ? protoObject.getGlobalUserId().getValue() : 0;
        long ltiTimestamp = protoObject.hasLtiTimestamp()
                ? ProtobufConversionUtil.convertTimestampToMillis(protoObject.getLtiTimestamp())
                : 0;
        String consumerKey = protoObject.hasConsumerKey() ? protoObject.getConsumerKey().getValue() : null;
        String consumerId = protoObject.hasConsumerId() ? protoObject.getConsumerId().getValue() : null;

        return new LtiUserRecord(new LtiUserId(consumerKey, consumerId), giftUserId,
                ltiTimestamp);
    }

    @Override
    public LtiGetUserResponse map(LtiUserRecord commonObject) {
        if (commonObject == null) {
            return null;
        }

        LtiGetUserResponse.Builder builder = LtiGetUserResponse.newBuilder();

        builder.setGlobalUserId(Int32Value.of(commonObject.getGlobalUserId()));
        Optional.ofNullable(commonObject.getLtiTimestamp()).ifPresent(time -> {
            builder.setLtiTimestamp(ProtobufConversionUtil.convertMillisToTimestamp(time.getTime()));
        });

        if (commonObject.getLtiUserId() != null) {
            Optional.ofNullable(commonObject.getLtiUserId().getConsumerKey()).ifPresent(key -> {
                builder.setConsumerKey(StringValue.of(key));
            });
            Optional.ofNullable(commonObject.getLtiUserId().getConsumerId()).ifPresent(id -> {
                builder.setConsumerId(StringValue.of(id));
            });
        }

        return builder.build();
    }

}
