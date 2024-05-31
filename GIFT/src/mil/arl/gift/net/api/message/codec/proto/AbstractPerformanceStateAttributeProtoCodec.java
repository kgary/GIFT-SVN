/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import generated.proto.common.AbstractPerformanceStateAttributeProto;
import mil.arl.gift.common.state.AbstractPerformanceStateAttribute;
import mil.arl.gift.common.state.PerformanceStateAttribute;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.api.message.MessageEncodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from an
 * AbstractPerformanceStateAttribute message.
 * 
 * @author cpolynice
 *
 */
public class AbstractPerformanceStateAttributeProtoCodec implements
        ProtoCodec<AbstractPerformanceStateAttributeProto.AbstractPerformanceStateAttribute, AbstractPerformanceStateAttribute> {

    /* Codec that will be used to convert to/from a protobuf
     * PerformanceStateAttribute. */
    private static PerformanceStateAttributeProtoCodec performanceCodec = new PerformanceStateAttributeProtoCodec();

    @Override
    public AbstractPerformanceStateAttribute convert(
            AbstractPerformanceStateAttributeProto.AbstractPerformanceStateAttribute protoObject) {
        if (protoObject == null) {
            return null;
        }

        if (protoObject.hasPerformanceStateAttribute()) {
            return performanceCodec.convert(protoObject.getPerformanceStateAttribute());
        } else {
            throw new MessageDecodeException(this.getClass().getName(), "Exception logged while converting ");
        }
    }

    @Override
    public AbstractPerformanceStateAttributeProto.AbstractPerformanceStateAttribute map(
            AbstractPerformanceStateAttribute commonObject) {
        if (commonObject == null) {
            return null;
        }

        AbstractPerformanceStateAttributeProto.AbstractPerformanceStateAttribute.Builder builder = AbstractPerformanceStateAttributeProto.AbstractPerformanceStateAttribute
                .newBuilder();

        if (commonObject instanceof PerformanceStateAttribute) {
            builder.setPerformanceStateAttribute(performanceCodec.map((PerformanceStateAttribute) commonObject));
        } else {
            throw new MessageEncodeException(this.getClass().getName(),
                    "Found unhandled PerformanceStateAttribute of " + commonObject);
        }

        return builder.build();
    }

}
