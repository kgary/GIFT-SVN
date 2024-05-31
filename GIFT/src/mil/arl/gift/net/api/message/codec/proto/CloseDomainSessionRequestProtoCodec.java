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

import generated.proto.common.CloseDomainSessionRequestProto;
import mil.arl.gift.common.CloseDomainSessionRequest;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a CloseDomainSessionRequest
 * message.
 * 
 * @author cpolynice
 *
 */
public class CloseDomainSessionRequestProtoCodec
        implements ProtoCodec<CloseDomainSessionRequestProto.CloseDomainSessionRequest, CloseDomainSessionRequest> {

    @Override
    public CloseDomainSessionRequest convert(CloseDomainSessionRequestProto.CloseDomainSessionRequest protoObject) {
        if (protoObject == null) {
            return null;
        }

        CloseDomainSessionRequest request = new CloseDomainSessionRequest();

        if (protoObject.hasReason()) {
            request.setReason(protoObject.getReason().getValue());
        }

        return request;
    }

    @Override
    public CloseDomainSessionRequestProto.CloseDomainSessionRequest map(CloseDomainSessionRequest commonObject) {
        if (commonObject == null) {
            return null;
        }

        CloseDomainSessionRequestProto.CloseDomainSessionRequest.Builder builder = CloseDomainSessionRequestProto.CloseDomainSessionRequest
                .newBuilder();

        Optional.ofNullable(commonObject.getReason()).ifPresent(reason -> {
            builder.setReason(StringValue.of(reason));
        });
        return builder.build();
    }
}
