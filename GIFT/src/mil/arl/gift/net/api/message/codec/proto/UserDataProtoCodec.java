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

import generated.proto.common.UserDataProto;
import mil.arl.gift.common.UserData;
import mil.arl.gift.common.enums.GenderEnum;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf UserData.
 * 
 * @author cpolynice
 *
 */
public class UserDataProtoCodec implements ProtoCodec<UserDataProto.UserData, UserData> {

    /* Codec that will be used to convert to/from a protobuf AbstractEnum. */
    private static final AbstractEnumObjectProtoCodec enumCodec = new AbstractEnumObjectProtoCodec();

    @Override
    public UserData convert(UserDataProto.UserData protoObject) {
        if (protoObject == null) {
            return null;
        }

        Integer userId = protoObject.hasUserId() ? protoObject.getUserId().getValue() : null;
        GenderEnum gender = protoObject.hasGender() ? (GenderEnum) enumCodec.convert(protoObject.getGender()) : null;
        String lmsUserName = protoObject.hasLmsUsername() ? protoObject.getLmsUsername().getValue() : null;

        UserData userData;
        if (userId != null) {
            userData = new UserData(userId, lmsUserName, gender);
        } else {
            userData = new UserData(lmsUserName, gender);
        }

        if (protoObject.hasUserName()) {
            userData.setUsername(protoObject.getUserName().getValue());
        }

        if (protoObject.hasExperimentId()) {
            userData.setExperimentId(protoObject.getExperimentId().getValue());
        }

        return userData;
    }

    @Override
    public UserDataProto.UserData map(UserData commonObject) {
        if (commonObject == null) {
            return null;
        }

        UserDataProto.UserData.Builder builder = UserDataProto.UserData.newBuilder();

        Optional.ofNullable(enumCodec.map(commonObject.getGender())).ifPresent(builder::setGender);
        Optional.ofNullable(commonObject.getUserId()).ifPresent(id -> {
            builder.setUserId(Int32Value.of(id));
        });
        Optional.ofNullable(commonObject.getExperimentId()).ifPresent(expId -> {
            builder.setExperimentId(StringValue.of(expId));
        });
        Optional.ofNullable(commonObject.getUsername()).ifPresent(username -> {
            builder.setUserName(StringValue.of(username));
        });
        Optional.ofNullable(commonObject.getLMSUserName()).ifPresent(lmsUsername -> {
            builder.setLmsUsername(StringValue.of(lmsUsername));
        });

        return builder.build();
    }
}
