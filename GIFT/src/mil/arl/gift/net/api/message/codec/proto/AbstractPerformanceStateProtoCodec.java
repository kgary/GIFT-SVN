/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import generated.proto.common.AbstractPerformanceStateProto;
import mil.arl.gift.common.state.AbstractPerformanceState;
import mil.arl.gift.common.state.ConceptPerformanceState;
import mil.arl.gift.common.state.IntermediateConceptPerformanceState;
import mil.arl.gift.common.state.TaskPerformanceState;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.api.message.MessageEncodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from an AbstractPerformanceState
 * message.
 * 
 * @author cpolynice
 *
 */
public class AbstractPerformanceStateProtoCodec
        implements ProtoCodec<AbstractPerformanceStateProto.AbstractPerformanceState, AbstractPerformanceState> {

    /* Codec that will be used to convert to/from a ConceptPerformanceState
     * message. */
    private static ConceptPerformanceStateProtoCodec conceptCodec = new ConceptPerformanceStateProtoCodec();

    /* Codec that will be used to convert to/from an
     * IntermediateConceptPerformanceState message. */
    private static IntermediateConceptPerformanceStateProtoCodec iConceptCodec = new IntermediateConceptPerformanceStateProtoCodec();

    /* Codec that will be used to convert to/from a TaskPerformanceState
     * message. */
    private static TaskPerformanceStateProtoCodec taskCodec = new TaskPerformanceStateProtoCodec();

    @Override
    public AbstractPerformanceState convert(AbstractPerformanceStateProto.AbstractPerformanceState protoObject) {
        if (protoObject == null) {
            return null;
        }

        if (protoObject.hasConceptPerformanceState()) {
            return conceptCodec.convert(protoObject.getConceptPerformanceState());
        } else if (protoObject.hasIntermediateConceptPerformanceState()) {
            return iConceptCodec.convert(protoObject.getIntermediateConceptPerformanceState());
        } else if (protoObject.hasTaskPerformanceState()) {
            return taskCodec.convert(protoObject.getTaskPerformanceState());
        } else {
            throw new MessageDecodeException(this.getClass().getName(), "Exception logged while converting ");
        }
    }

    @Override
    public AbstractPerformanceStateProto.AbstractPerformanceState map(AbstractPerformanceState commonObject) {
        if (commonObject == null) {
            return null;
        }

        AbstractPerformanceStateProto.AbstractPerformanceState.Builder builder = AbstractPerformanceStateProto.AbstractPerformanceState
                .newBuilder();

        if (commonObject instanceof ConceptPerformanceState) {
            builder.setConceptPerformanceState(conceptCodec.map((ConceptPerformanceState) commonObject));
        } else if (commonObject instanceof IntermediateConceptPerformanceState) {
            builder.setIntermediateConceptPerformanceState(
                    iConceptCodec.map((IntermediateConceptPerformanceState) commonObject));
        } else if (commonObject instanceof TaskPerformanceState) {
            builder.setTaskPerformanceState(taskCodec.map((TaskPerformanceState) commonObject));
        } else {
            throw new MessageEncodeException(this.getClass().getName(),
                    "Found unhandled Assessment of " + commonObject);
        }

        return builder.build();
    }
}
