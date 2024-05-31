/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.protobuf.BoolValue;

import generated.proto.common.AbstractPerformanceStateProto;
import mil.arl.gift.common.state.ConceptPerformanceState;
import mil.arl.gift.common.state.IntermediateConceptPerformanceState;
import mil.arl.gift.common.state.PerformanceStateAttribute;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from an
 * IntermediateConceptPerformanceState message.
 * 
 * @author cpolynice
 *
 */
public class IntermediateConceptPerformanceStateProtoCodec implements
        ProtoCodec<AbstractPerformanceStateProto.IntermediateConceptPerformanceState, IntermediateConceptPerformanceState> {

    /* Codec that will be used to convert to/from an
     * AbstractPerformanceStateAttribute message. */
    private static AbstractPerformanceStateAttributeProtoCodec attrCodec = new AbstractPerformanceStateAttributeProtoCodec();

    /* Codec that will be used to convert to/from a ConceptPerformanceState
     * message. */
    private static ConceptPerformanceStateProtoCodec conceptCodec = new ConceptPerformanceStateProtoCodec();

    /* Codec that will be used to convert to/from a protobuf
     * IntermediateConceptPerformanceState. */
    private static IntermediateConceptPerformanceStateProtoCodec iConceptCodec = new IntermediateConceptPerformanceStateProtoCodec();

    /**
     * Converts the given protobuf list into the common object representation.
     * 
     * @param protoList the protobuf list
     * @return the converted common object list.
     */
    private static List<ConceptPerformanceState> convertConcepts(
            List<AbstractPerformanceStateProto.ConceptPerformanceStateChoice> protoList) {
        if (protoList == null) {
            return null;
        }

        List<ConceptPerformanceState> commonList = new ArrayList<>();

        for (AbstractPerformanceStateProto.ConceptPerformanceStateChoice concept : protoList) {
            if (concept.hasConceptPerformanceState()) {
                commonList.add(conceptCodec.convert(concept.getConceptPerformanceState()));
            } else if (concept.hasIntermediateConceptPerformanceState()) {
                commonList.add(iConceptCodec.convert(concept.getIntermediateConceptPerformanceState()));
            } else {
                throw new MessageDecodeException(TaskPerformanceStateProtoCodec.class.getName(),
                        "Found unhandled concept choice of type " + concept);
            }
        }

        return commonList;
    }

    /**
     * Maps the given common object list into the protobuf representation.
     * 
     * @param commonList the common object list
     * @return the protobuf list representation.
     */
    private static List<AbstractPerformanceStateProto.ConceptPerformanceStateChoice> mapConcepts(
            List<ConceptPerformanceState> commonList) {
        if (commonList == null) {
            return null;
        }

        List<AbstractPerformanceStateProto.ConceptPerformanceStateChoice> protoList = new ArrayList<>();

        for (ConceptPerformanceState concept : commonList) {
            AbstractPerformanceStateProto.ConceptPerformanceStateChoice.Builder cBuilder = AbstractPerformanceStateProto.ConceptPerformanceStateChoice
                    .newBuilder();

            if (concept instanceof IntermediateConceptPerformanceState) {
                cBuilder.setIntermediateConceptPerformanceState(
                        iConceptCodec.map((IntermediateConceptPerformanceState) concept));
            } else {
                cBuilder.setConceptPerformanceState(conceptCodec.map(concept));
            }

            protoList.add(cBuilder.build());
        }

        return protoList;
    }

    @Override
    public IntermediateConceptPerformanceState convert(
            AbstractPerformanceStateProto.IntermediateConceptPerformanceState protoObject) {
        if (protoObject == null) {
            return null;
        }

        if (protoObject.hasState()) {
            PerformanceStateAttribute state = (PerformanceStateAttribute) attrCodec.convert(protoObject.getState());
            List<ConceptPerformanceState> concepts = convertConcepts(
                    protoObject.getConceptPerformanceStateChoicesList());
            IntermediateConceptPerformanceState icPerfState = new IntermediateConceptPerformanceState(state, concepts);

            if (protoObject.hasHasObservedAssessment()) {
                icPerfState.setContainsObservedAssessmentCondition(protoObject.getHasObservedAssessment().getValue());
            }

            return icPerfState;
        } else {
            throw new MessageDecodeException(this.getClass().getName(),
                    "Exception logged while decoding an object to create "
                            + IntermediateConceptPerformanceState.class.getName());
        }
    }

    @Override
    public AbstractPerformanceStateProto.IntermediateConceptPerformanceState map(
            IntermediateConceptPerformanceState commonObject) {
        if (commonObject == null) {
            return null;
        }

        AbstractPerformanceStateProto.IntermediateConceptPerformanceState.Builder builder = AbstractPerformanceStateProto.IntermediateConceptPerformanceState
                .newBuilder();

        Optional.ofNullable(attrCodec.map(commonObject.getState())).ifPresent(builder::setState);
        Optional.ofNullable(mapConcepts(commonObject.getConcepts()))
                .ifPresent(builder::addAllConceptPerformanceStateChoices);
        builder.setHasObservedAssessment(BoolValue.of(commonObject.isContainsObservedAssessmentCondition()));

        return builder.build();
    }

}
