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

import generated.proto.common.UserSessionProto;
import mil.arl.gift.common.UserSession;
import mil.arl.gift.common.UserSessionType;
import mil.arl.gift.net.api.message.UserSessionMessage;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for protobuf encoding/decoding a User Session.
 * 
 * @author cpolynice
 *
 */
public class UserSessionProtoCodec implements ProtoCodec<UserSessionProto.UserSession, UserSession> {

    /** Codec used to convert to/from an AbstractUserSessionDetails instance. */
    private static AbstractUserSessionDetailsProtoCodec detailsCodec = new AbstractUserSessionDetailsProtoCodec();

    @Override
    public UserSession convert(UserSessionProto.UserSession protoObject) {
        if (protoObject == null) {
            return null;
        }

        if (protoObject.getUserId().getValue() == UserSessionMessage.PRE_USER_UNKNOWN_ID) {
            return null;
        }

        UserSession userSession = new UserSession(protoObject.getUserId().getValue());

        userSession.setUsername(protoObject.hasUsername() ? protoObject.getUsername().getValue() : null);
        userSession.setExperimentId(
                protoObject.hasExperimentId() ? protoObject.getExperimentId().getValue() : null);
        userSession.setSessionType(protoObject.hasSessionType()
                ? UserSessionType.valueOf(protoObject.getSessionType().getValue())
                : UserSessionType.GIFT_USER);

        if (protoObject.hasSessionDetails()) {
            userSession.setSessionDetails(detailsCodec.convert(protoObject.getSessionDetails()));
        }

        return userSession;
    }

    @Override
    public UserSessionProto.UserSession map(UserSession commonObject) {
        if (commonObject == null) {
            return null;
        }

        UserSessionProto.UserSession.Builder builder = UserSessionProto.UserSession.newBuilder();

        builder.setUserId(Int32Value.of(commonObject.getUserId()));
        
        Optional.ofNullable(commonObject.getUsername()).ifPresent(userName -> {
            builder.setUsername(StringValue.of(userName));
        });
        
        Optional.ofNullable(commonObject.getExperimentId()).ifPresent(experimentId -> {
            builder.setExperimentId(StringValue.of(experimentId));
        });
        
        Optional.ofNullable(commonObject.getSessionType().name()).ifPresent(sessionType -> {
            builder.setSessionType(StringValue.of(sessionType));
        });
        
        Optional.ofNullable(detailsCodec.map(commonObject.getSessionDetails())).ifPresent(builder::setSessionDetails);

        return builder.build();
    }
}
