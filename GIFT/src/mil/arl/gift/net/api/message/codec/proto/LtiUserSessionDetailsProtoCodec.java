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

import generated.proto.common.LtiUserSessionDetailsProto;
import mil.arl.gift.common.lti.LtiUserId;
import mil.arl.gift.common.usersession.LtiUserSessionDetails;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * The LtiUserSessionDetailsProtoCodec class is class responsible for protobuf
 * encoding/decoding the LtiUserSessionDetails class.
 * 
 * @author cpolynice
 *
 */
public class LtiUserSessionDetailsProtoCodec
        implements ProtoCodec<LtiUserSessionDetailsProto.LtiUserSessionDetails, LtiUserSessionDetails> {

    /** Codec used to convert to/from LtiUserId. */
    private static LtiUserIdProtoCodec ltiUserIdCodec = new LtiUserIdProtoCodec();

    @Override
    public LtiUserSessionDetails convert(LtiUserSessionDetailsProto.LtiUserSessionDetails protoObject) {
        if (protoObject == null) {
            return null;
        }
        
        int globalUserId = protoObject.getGlobalUserId().getValue();
        String dataSetId = protoObject.hasDataSetId() ? protoObject.getDataSetId().getValue() : null;
        LtiUserId ltiUserId = protoObject.hasLtiUserId() ? ltiUserIdCodec.convert(protoObject.getLtiUserId())
                : null;

        return new LtiUserSessionDetails(ltiUserId, dataSetId, globalUserId);
    }

    @Override
    public LtiUserSessionDetailsProto.LtiUserSessionDetails map(LtiUserSessionDetails commonObject) {
        if (commonObject == null) {
            return null;
        }
        
        LtiUserSessionDetailsProto.LtiUserSessionDetails.Builder builder = LtiUserSessionDetailsProto.LtiUserSessionDetails.newBuilder();
        
        Optional.ofNullable(commonObject.getGlobalUserId()).ifPresent(globalUserId -> {
            builder.setGlobalUserId(Int32Value.of(globalUserId));
        });
        
        Optional.ofNullable(commonObject.getDataSetId()).ifPresent(dataSetId -> {
            builder.setDataSetId(StringValue.of(dataSetId));
        });
        
        Optional.ofNullable(ltiUserIdCodec.map(commonObject.getLtiUserId())).ifPresent(builder::setLtiUserId);

        return builder.build();
    }
}
