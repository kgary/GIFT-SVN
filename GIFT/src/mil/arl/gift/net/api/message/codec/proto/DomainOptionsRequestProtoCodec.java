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

import generated.proto.common.DomainOptionsRequestProto;
import mil.arl.gift.common.DomainOptionsRequest;
import mil.arl.gift.common.WebClientInformation;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf
 * DomainOptionsRequest.
 * 
 * @author cpolynice
 *
 */
public class DomainOptionsRequestProtoCodec
        implements ProtoCodec<DomainOptionsRequestProto.DomainOptionsRequest, DomainOptionsRequest> {

    /* Codec that will be used to convert to/from a WebClientInformation. */
    private static WebClientInformationProtoCodec codec = new WebClientInformationProtoCodec();

    @Override
    public DomainOptionsRequest convert(DomainOptionsRequestProto.DomainOptionsRequest protoObject) {
        if (protoObject == null) {
            return null;
        }

        WebClientInformation info = protoObject.hasClientInfo() ? codec.convert(protoObject.getClientInfo()) : null;
        DomainOptionsRequest request = new DomainOptionsRequest(info);

        if (protoObject.hasLMSUsername()) {
            request.setLMSUserName(protoObject.getLMSUsername().getValue());
        }

        return request;
    }

    @Override
    public DomainOptionsRequestProto.DomainOptionsRequest map(DomainOptionsRequest commonObject) {
        if (commonObject == null) {
            return null;
        }

        DomainOptionsRequestProto.DomainOptionsRequest.Builder builder = DomainOptionsRequestProto.DomainOptionsRequest
                .newBuilder();

        Optional.ofNullable(codec.map(commonObject.getWebClientInformation())).ifPresent(builder::setClientInfo);
        Optional.ofNullable(commonObject.getLMSUserName()).ifPresent(username -> {
            builder.setLMSUsername(StringValue.of(username));
        });

        return builder.build();
    }
}
