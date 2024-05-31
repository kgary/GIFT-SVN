/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import generated.proto.common.AbstractRuntimeParametersProto;
import mil.arl.gift.common.AbstractRuntimeParameters;
import mil.arl.gift.common.lti.LtiRuntimeParameters;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.api.message.MessageEncodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from an AbstractRuntimeParameters
 * message.
 * 
 * @author cpolynice
 *
 */
public class AbstractRuntimeParametersProtoCodec
        implements ProtoCodec<AbstractRuntimeParametersProto.AbstractRuntimeParameters, AbstractRuntimeParameters> {

    /* Codec that will be used to convert to/from a LtiRuntimeParameters
     * instance. */
    private static LtiRuntimeParametersProtoCodec codec = new LtiRuntimeParametersProtoCodec();

    @Override
    public AbstractRuntimeParameters convert(AbstractRuntimeParametersProto.AbstractRuntimeParameters protoObject) {
        if (protoObject == null) {
            return null;
        }

        if (protoObject.hasLtiRuntimeParameters()) {
            return codec.convert(protoObject.getLtiRuntimeParameters());
        } else {
            throw new MessageDecodeException(this.getClass().getName(), "Exception logged while converting ");
        }
    }

    @Override
    public AbstractRuntimeParametersProto.AbstractRuntimeParameters map(AbstractRuntimeParameters commonObject) {
        if (commonObject == null) {
            return null;
        }

        AbstractRuntimeParametersProto.AbstractRuntimeParameters.Builder builder = AbstractRuntimeParametersProto.AbstractRuntimeParameters
                .newBuilder();

        if (commonObject instanceof LtiRuntimeParameters) {
            builder.setLtiRuntimeParameters(codec.map((LtiRuntimeParameters) commonObject));
        } else {
            throw new MessageEncodeException(this.getClass().getName(),
                    "Found unhandled runtime parameters of " + commonObject);
        }

        return builder.build();
    }
}
