/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.Optional;

import generated.proto.common.AbstractUserSessionDetailsProto;
import mil.arl.gift.common.usersession.AbstractUserSessionDetails;
import mil.arl.gift.common.usersession.LtiUserSessionDetails;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.api.message.MessageEncodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * The AbstractUserSessionDetailsProtoCodec codec is responsible for determining
 * the type of UserSessionDetails class that will be protobuf encoded/decoded.
 * 
 * @author cpolynice
 *
 */
public class AbstractUserSessionDetailsProtoCodec
        implements ProtoCodec<AbstractUserSessionDetailsProto.AbstractUserSessionDetails, AbstractUserSessionDetails> {

    /** Codec used to convert to/from LtiUserId. */
    private static LtiUserSessionDetailsProtoCodec ltiUserSessionDetailsCodec = new LtiUserSessionDetailsProtoCodec();

    @Override
    public AbstractUserSessionDetails convert(AbstractUserSessionDetailsProto.AbstractUserSessionDetails protoObject) {
        if (protoObject == null) {
            return null;
        }
        
        if (protoObject.hasLtiUserSessionDetails()) {
            return ltiUserSessionDetailsCodec.convert(protoObject.getLtiUserSessionDetails());
        } else {
            throw new MessageDecodeException(this.getClass().getName(), "Exception logged while converting ");
        }
    }

    @Override
    public AbstractUserSessionDetailsProto.AbstractUserSessionDetails map(AbstractUserSessionDetails commonObject) {
        if (commonObject == null) {
            return null;
        }

        AbstractUserSessionDetailsProto.AbstractUserSessionDetails.Builder builder = AbstractUserSessionDetailsProto.AbstractUserSessionDetails
                .newBuilder();

        if (commonObject instanceof LtiUserSessionDetails) {
            Optional.ofNullable(ltiUserSessionDetailsCodec.map((LtiUserSessionDetails) commonObject))
                    .ifPresent(builder::setLtiUserSessionDetails);
        } else {
            throw new MessageEncodeException(this.getClass().getName(),
                    "Found unhandled abstract team unit of " + commonObject);
        }

        return builder.build();
    }
}
