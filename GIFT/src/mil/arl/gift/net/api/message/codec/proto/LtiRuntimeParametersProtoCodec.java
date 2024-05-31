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

import generated.proto.common.AbstractRuntimeParametersProto;
import mil.arl.gift.common.lti.LtiRuntimeParameters;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a LtiRuntimeParameters
 * message.
 * 
 * @author cpolynice
 *
 */
public class LtiRuntimeParametersProtoCodec
        implements ProtoCodec<AbstractRuntimeParametersProto.LtiRuntimeParameters, LtiRuntimeParameters> {

    @Override
    public LtiRuntimeParameters convert(AbstractRuntimeParametersProto.LtiRuntimeParameters protoObject) {
        if (protoObject == null) {
            return null;
        }

        String consumerKey = protoObject.hasConsumerKey() ? protoObject.getConsumerKey().getValue() : null;
        String serviceUrl = protoObject.hasServiceUrl() ? protoObject.getServiceUrl().getValue() : null;
        String lisSourcedid = protoObject.hasLisSourcedid() ? protoObject.getLisSourcedid().getValue() : null;

        LtiRuntimeParameters runtimeParams = new LtiRuntimeParameters(consumerKey, serviceUrl, lisSourcedid);
        return runtimeParams;
    }

    @Override
    public AbstractRuntimeParametersProto.LtiRuntimeParameters map(LtiRuntimeParameters commonObject) {
        if (commonObject == null) {
            return null;
        }

        AbstractRuntimeParametersProto.LtiRuntimeParameters.Builder builder = AbstractRuntimeParametersProto.LtiRuntimeParameters
                .newBuilder();

        Optional.ofNullable(commonObject.getConsumerKey()).ifPresent(key -> {
            builder.setConsumerKey(StringValue.of(key));
        });
        Optional.ofNullable(commonObject.getOutcomeServiceUrl()).ifPresent(url -> {
            builder.setServiceUrl(StringValue.of(url));
        });
        Optional.ofNullable(commonObject.getLisSourcedid()).ifPresent(id -> {
            builder.setLisSourcedid(StringValue.of(id));
        });

        return builder.build();
    }
}
