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

import generated.proto.common.LoginRequestProto;
import mil.arl.gift.common.LoginRequest;
import mil.arl.gift.common.UserSessionType;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf LoginRequest.
 * 
 * @author cpolynice
 *
 */
public class LoginRequestProtoCodec implements ProtoCodec<LoginRequestProto.LoginRequest, LoginRequest> {

    @Override
    public LoginRequest convert(LoginRequestProto.LoginRequest protoObject) {
        if (protoObject == null) {
            return null;
        }

        int userId = protoObject.hasUserId() ? protoObject.getUserId().getValue() : 0;
        LoginRequest request = new LoginRequest(userId);

        if (protoObject.hasUserName()) {
            request.setUsername(protoObject.getUserName().getValue());
        }

        if (protoObject.hasUserType() && StringUtils.isNotBlank(protoObject.getUserType().getValue())) {
            request.setUserType(UserSessionType.valueOf(protoObject.getUserType().getValue()));
        } else {
            request.setUserType(UserSessionType.GIFT_USER);
        }

        return request;
    }

    @Override
    public LoginRequestProto.LoginRequest map(LoginRequest commonObject) {
        if (commonObject == null) {
            return null;
        }

        LoginRequestProto.LoginRequest.Builder builder = LoginRequestProto.LoginRequest.newBuilder();

        builder.setUserId(Int32Value.of(commonObject.getUserId()));
        Optional.ofNullable(commonObject.getUsername()).ifPresent(username -> {
            builder.setUserName(StringValue.of(username));
        });
        Optional.ofNullable(commonObject.getUserType()).ifPresent(type -> {
            builder.setUserType(StringValue.of(type.name()));
        });

        return builder.build();
    }

}
