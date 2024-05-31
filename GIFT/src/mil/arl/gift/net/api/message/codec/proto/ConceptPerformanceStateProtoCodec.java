/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.Optional;

import com.google.protobuf.BoolValue;

import generated.proto.common.AbstractPerformanceStateProto;
import mil.arl.gift.common.state.ConceptPerformanceState;
import mil.arl.gift.common.state.PerformanceStateAttribute;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a ConceptPerformanceState
 * message.
 * 
 * @author cpolynice
 *
 */
public class ConceptPerformanceStateProtoCodec
        implements ProtoCodec<AbstractPerformanceStateProto.ConceptPerformanceState, ConceptPerformanceState> {

    /* Codec that will be used to convert to/from an
     * AbstractPerformanceStateAttribute message. */
    private static AbstractPerformanceStateAttributeProtoCodec attrCodec = new AbstractPerformanceStateAttributeProtoCodec();

    @Override
    public ConceptPerformanceState convert(AbstractPerformanceStateProto.ConceptPerformanceState protoObject) {
        if (protoObject == null) {
            return null;
        }

        if (protoObject.hasState()) {
            PerformanceStateAttribute state = (PerformanceStateAttribute) attrCodec.convert(protoObject.getState());
            ConceptPerformanceState cPerfState = new ConceptPerformanceState(state);

            if (protoObject.hasHasObservedAssessment()) {
                cPerfState.setContainsObservedAssessmentCondition(protoObject.getHasObservedAssessment().getValue());
            }

            return cPerfState;
        } else {
            throw new MessageDecodeException(this.getClass().getName(),
                    "Exception logged while decoding an object to create " + ConceptPerformanceState.class.getName());
        }

    }

    @Override
    public AbstractPerformanceStateProto.ConceptPerformanceState map(ConceptPerformanceState commonObject) {
        if (commonObject == null) {
            return null;
        }

        AbstractPerformanceStateProto.ConceptPerformanceState.Builder builder = AbstractPerformanceStateProto.ConceptPerformanceState
                .newBuilder();

        builder.setHasObservedAssessment(BoolValue.of(commonObject.isContainsObservedAssessmentCondition()));
        Optional.ofNullable(attrCodec.map(commonObject.getState())).ifPresent(builder::setState);

        return builder.build();
    }
}
