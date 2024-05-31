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

import generated.proto.common.DomainSessionProto;
import mil.arl.gift.common.DomainSession;
import mil.arl.gift.common.UserSession;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf DomainSession.
 * 
 * @author cpolynice
 *
 */
public class DomainSessionProtoCodec implements ProtoCodec<DomainSessionProto.DomainSession, DomainSession> {

    /* Codec that will be used to convert to/from a UserSession. */
    private static UserSessionProtoCodec codec = new UserSessionProtoCodec();

    @Override
    public DomainSession convert(DomainSessionProto.DomainSession protoObject) {
        if (protoObject == null) {
            return null;
        }

        int userID = 0;
        UserSession userSession = protoObject.hasUserSessionDetails()
                ? codec.convert(protoObject.getUserSessionDetails())
                : null;

        if (userSession != null) {
            userID = protoObject.getUserSessionDetails().hasUserId()
                    ? protoObject.getUserSessionDetails().getUserId().getValue()
                    : 0;
        }

        int dsId = protoObject.hasDsId() ? protoObject.getDsId().getValue() : 0;
        String dName = protoObject.hasDomainName() ? protoObject.getDomainName().getValue() : null;

        DomainSession dSession;
        if (protoObject.hasSourceId()) {
            dSession = new DomainSession(dsId, userID, dName, protoObject.getSourceId().getValue());
        } else {
            /* legacy message support */
            dSession = new DomainSession(dsId, userID, dName, dName);
        }
        
        Integer subjectId = protoObject.hasSubjectId() ? protoObject.getSubjectId().getValue() : null;
        dSession.setSubjectId(subjectId);

        dSession.copyFromUserSession(userSession);
        return dSession;
    }

    @Override
    public DomainSessionProto.DomainSession map(DomainSession commonObject) {
        if (commonObject == null) {
            return null;
        }

        DomainSessionProto.DomainSession.Builder builder = DomainSessionProto.DomainSession.newBuilder();

        builder.setDsId(Int32Value.of(commonObject.getDomainSessionId()));
        Optional.ofNullable(codec.map(commonObject)).ifPresent(builder::setUserSessionDetails);
        Optional.ofNullable(commonObject.getDomainRuntimeId()).ifPresent(name -> {
            builder.setDomainName(StringValue.of(name));
        });
        Optional.ofNullable(commonObject.getDomainSourceId()).ifPresent(id -> {
            builder.setSourceId(StringValue.of(id));
        });
        Optional.ofNullable(commonObject.getSubjectId()).ifPresent(id -> {
            builder.setSubjectId(Int32Value.of(id));
        });
        
        return builder.build();
    }
}
