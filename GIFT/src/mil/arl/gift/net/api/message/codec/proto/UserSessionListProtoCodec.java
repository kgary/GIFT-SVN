/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.ArrayList;
import java.util.List;

import generated.proto.common.UserSessionListProto;
import generated.proto.common.UserSessionProto;
import mil.arl.gift.common.UserSession;
import mil.arl.gift.common.UserSessionList;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf UserSessionList.
 * 
 * @author cpolynice
 *
 */
public class UserSessionListProtoCodec implements ProtoCodec<UserSessionListProto.UserSessionList, UserSessionList> {

    /* Codec that will be used to convert to/from a protobuf UserSession. */
    private static final UserSessionProtoCodec codec = new UserSessionProtoCodec();

    /**
     * Converts the given protobuf list to the common object representation.
     * 
     * @param protoList the protobuf list
     * @return the common object list.
     */
    private static List<UserSession> convertUserSessions(List<UserSessionProto.UserSession> protoList) {
        if (protoList == null) {
            return null;
        }

        List<UserSession> commonList = new ArrayList<>();

        for (UserSessionProto.UserSession session : protoList) {
            commonList.add(codec.convert(session));
        }

        return commonList;
    }

    /**
     * Maps the given common object list to the protobuf list representation.
     * 
     * @param commonList the common object list.
     * @return the protobuf list.
     */
    private static List<UserSessionProto.UserSession> mapUserSessions(List<UserSession> commonList) {
        if (commonList == null) {
            return null;
        }

        List<UserSessionProto.UserSession> protoList = new ArrayList<>();

        for (UserSession session : commonList) {
            protoList.add(codec.map(session));
        }

        return protoList;
    }

    @Override
    public UserSessionList convert(UserSessionListProto.UserSessionList protoObject) {
        if (protoObject == null) {
            return null;
        }

        List<UserSession> userSessions = CollectionUtils.isNotEmpty(protoObject.getUserSessionsList())
                ? convertUserSessions(protoObject.getUserSessionsList())
                : new ArrayList<>(0);

        return new UserSessionList(userSessions);
    }

    @Override
    public UserSessionListProto.UserSessionList map(UserSessionList commonObject) {
        if (commonObject == null) {
            return null;
        }

        UserSessionListProto.UserSessionList.Builder builder = UserSessionListProto.UserSessionList.newBuilder();

        return CollectionUtils.isNotEmpty(commonObject.getUserSessions())
                ? builder.addAllUserSessions(mapUserSessions(commonObject.getUserSessions())).build()
                : builder.build();
    }
}
