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
import com.google.protobuf.DoubleValue;
import com.google.protobuf.StringValue;

import generated.proto.common.AbstractPerformanceStateProto;
import mil.arl.gift.common.state.ConceptPerformanceState;
import mil.arl.gift.common.state.IntermediateConceptPerformanceState;
import mil.arl.gift.common.state.PerformanceStateAttribute;
import mil.arl.gift.common.state.TaskPerformanceState;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf
 * TaskPerformanceState instance.
 * 
 * @author cpolynice
 *
 */
public class TaskPerformanceStateProtoCodec
        implements ProtoCodec<AbstractPerformanceStateProto.TaskPerformanceState, TaskPerformanceState> {

    /* Codec that will be used to convert to/from a protobuf
     * ConceptPerformanceState. */
    private static ConceptPerformanceStateProtoCodec conceptCodec = new ConceptPerformanceStateProtoCodec();

    /* Codec that will be used to convert to/from a protobuf
     * IntermediateConceptPerformanceState. */
    private static IntermediateConceptPerformanceStateProtoCodec iConceptCodec = new IntermediateConceptPerformanceStateProtoCodec();

    /* Codec that will be used to convert to/from a protobuf
     * PerformanceStateAttribute. */
    private static AbstractPerformanceStateAttributeProtoCodec performanceCodec = new AbstractPerformanceStateAttributeProtoCodec();

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
    public TaskPerformanceState convert(AbstractPerformanceStateProto.TaskPerformanceState protoObject) {
        if (protoObject == null) {
            return null;
        }

        PerformanceStateAttribute state = protoObject.hasState()
                ? (PerformanceStateAttribute) performanceCodec.convert(protoObject.getState())
                : null;
        List<ConceptPerformanceState> concepts = CollectionUtils
                .isNotEmpty(protoObject.getConceptPerformanceStateChoicesList())
                        ? convertConcepts(protoObject.getConceptPerformanceStateChoicesList())
                : null;

        TaskPerformanceState tPerfState = new TaskPerformanceState(state, concepts);

        if (protoObject.hasHasObservedAssessment()) {
            tPerfState.setContainsObservedAssessmentCondition(protoObject.getHasObservedAssessment().getValue());
        }
        
        if(protoObject.hasDifficulty()) {
            tPerfState.setDifficulty(protoObject.getDifficulty().getValue());
        }
        
        if(protoObject.hasDifficultyReason()) {
            tPerfState.setDifficultyReason(protoObject.getDifficultyReason().getValue());
        }
        
        if(protoObject.hasStress()) {
            tPerfState.setStress(protoObject.getStress().getValue());
        }
        
        if(protoObject.hasStressReason()) {
            tPerfState.setStressReason(protoObject.getStressReason().getValue());
        }

        return tPerfState;
    }

    @Override
    public AbstractPerformanceStateProto.TaskPerformanceState map(TaskPerformanceState commonObject) {
        if (commonObject == null) {
            return null;
        }
        
        AbstractPerformanceStateProto.TaskPerformanceState.Builder builder = AbstractPerformanceStateProto.TaskPerformanceState
                .newBuilder();
        
        builder.setHasObservedAssessment(BoolValue.of(commonObject.isContainsObservedAssessmentCondition()));
        Optional.ofNullable(performanceCodec.map(commonObject.getState())).ifPresent(builder::setState);
        Optional.ofNullable(mapConcepts(commonObject.getConcepts()))
                .ifPresent(builder::addAllConceptPerformanceStateChoices);
        
        Optional.ofNullable(commonObject.getDifficulty()).ifPresent(difficulty -> {
            builder.setDifficulty(DoubleValue.of(difficulty));
        });
        
        Optional.ofNullable(commonObject.getDifficultyReason()).ifPresent(difficultyReason ->{
            builder.setDifficultyReason(StringValue.of(difficultyReason));
        });
        
        Optional.ofNullable(commonObject.getStress()).ifPresent(stress -> {
            builder.setStress(DoubleValue.of(stress));
        });
        
        Optional.ofNullable(commonObject.getStressReason()).ifPresent(stressReason ->{
            builder.setStressReason(StringValue.of(stressReason));
        });        
        
        return builder.build();
    }

}
