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

import generated.proto.common.DomainSelectionRequestProto;
import mil.arl.gift.common.AbstractRuntimeParameters;
import mil.arl.gift.common.DomainSelectionRequest;
import mil.arl.gift.common.WebClientInformation;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf
 * DomainSelectionRequest.
 * 
 * @author cpolynice
 *
 */
public class DomainSelectionRequestProtoCodec
        implements ProtoCodec<DomainSelectionRequestProto.DomainSelectionRequest, DomainSelectionRequest> {

    /* Codec that will be used to convert to/from an
     * AbstractRuntimeParameters. */
    private static AbstractRuntimeParametersProtoCodec runtimeCodec = new AbstractRuntimeParametersProtoCodec();

    /* Codec that will be used to convert to/from a WebClientInformation. */
    private static WebClientInformationProtoCodec webCodec = new WebClientInformationProtoCodec();

    @Override
    public DomainSelectionRequest convert(DomainSelectionRequestProto.DomainSelectionRequest protoObject) {
        if (protoObject == null) {
            return null;
        }

        String domainName = protoObject.hasDomainName() ? protoObject.getDomainName().getValue() : null;
        String lmsUsername = protoObject.hasLmsUsername() ? protoObject.getLmsUsername().getValue() : null;
        WebClientInformation info = protoObject.hasClientInfo() ? webCodec.convert(protoObject.getClientInfo()) : null;

        /* The runtime parameters are optional and may not exist. */
        AbstractRuntimeParameters runtimeParams = null;

        if (protoObject.hasRuntimeParams()) {
            runtimeParams = runtimeCodec.convert(protoObject.getRuntimeParams());
        }

        if (protoObject.hasDomainSourceId()) {
            return new DomainSelectionRequest(lmsUsername, domainName, protoObject.getDomainSourceId().getValue(), info,
                    runtimeParams);

        } else {
            /* legacy message support */
            return new DomainSelectionRequest(lmsUsername, domainName, domainName, info, runtimeParams);
        }
    }

    @Override
    public DomainSelectionRequestProto.DomainSelectionRequest map(DomainSelectionRequest commonObject) {
        if (commonObject == null) {
            return null;
        }

        DomainSelectionRequestProto.DomainSelectionRequest.Builder builder = DomainSelectionRequestProto.DomainSelectionRequest
                .newBuilder();

        Optional.ofNullable(webCodec.map(commonObject.getClientInformation())).ifPresent(builder::setClientInfo);
        Optional.ofNullable(runtimeCodec.map(commonObject.getRuntimeParams())).ifPresent(builder::setRuntimeParams);
        Optional.ofNullable(commonObject.getDomainRuntimeId()).ifPresent(name -> {
            builder.setDomainName(StringValue.of(name));
        });
        Optional.ofNullable(commonObject.getDomainSourceId()).ifPresent(id -> {
            builder.setDomainSourceId(StringValue.of(id));
        });
        Optional.ofNullable(commonObject.getLmsUsername()).ifPresent(username -> {
            builder.setLmsUsername(StringValue.of(username));
        });

        return builder.build();
    }

}
