/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.google.protobuf.BoolValue;
import com.google.protobuf.DoubleValue;
import com.google.protobuf.FloatValue;
import com.google.protobuf.Int32Value;
import com.google.protobuf.StringValue;

import generated.proto.common.AbstractAssessmentProto;
import generated.proto.common.AbstractEnumObjectProto.AbstractEnumObject;
import generated.proto.common.AssessmentContainerProto;
import generated.proto.common.AssessmentContainerProto.AssessmentContainer;
import mil.arl.gift.common.AbstractAssessment.PerformanceNodeStateEnum;
import mil.arl.gift.common.ConceptAssessment;
import mil.arl.gift.common.TaskAssessment;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.common.util.ProtobufConversionUtil;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for protobuf encoding/decoding a task assessment
 * message.
 * 
 * @author cpolynice
 *
 */
public class TaskAssessmentProtoCodec implements ProtoCodec<AbstractAssessmentProto.TaskAssessment, TaskAssessment> {

    /* Codec that will be used to convert to/from an Abstract Assessment
     * instance. */
    private static AbstractAssessmentProtoCodec conceptCodec = new AbstractAssessmentProtoCodec();

    /* Codec that will be used to convert to/from enumerations. */
    private static AbstractEnumObjectProtoCodec enumCodec = new AbstractEnumObjectProtoCodec();

    /**
     * Converts a protobuf map to the common object map using attributes
     * specific to the common object instance.
     * 
     * @param protoMap the map of assessed team org entites in protobuf
     * @return the common object team org entities map
     */
    public static Map<String, AssessmentLevelEnum> convertAssessedTeamOrgEntities(
            Map<String, AbstractEnumObject> protoMap) {
        if (protoMap == null) {
            return null;
        }

        Map<String, AssessmentLevelEnum> teamOrgEntities = new HashMap<>();

        for (Map.Entry<String, AbstractEnumObject> entity : protoMap.entrySet()) {
            String key = entity.getKey();
            AssessmentLevelEnum value = (AssessmentLevelEnum) enumCodec.convert(entity.getValue());

            if (value != null) {
                teamOrgEntities.put(key, value);
            }
        }

        return teamOrgEntities;
    }

    /**
     * Maps a common object map to the protobuf map using attributes specific to
     * the protobuf message.
     * 
     * @param commonMap the map of assessed team org entites
     * @return the protobuf team org entities map
     */
    public static Map<String, AbstractEnumObject> mapAssessedTeamOrgEntities(
            Map<String, AssessmentLevelEnum> commonMap) {
        if (commonMap == null) {
            return null;
        }

        Map<String, AbstractEnumObject> teamOrgEntities = new HashMap<>();

        for (Map.Entry<String, AssessmentLevelEnum> entity : commonMap.entrySet()) {
            String key = entity.getKey();
            AbstractEnumObject value = enumCodec.map(entity.getValue());

            if (value != null) {
                teamOrgEntities.put(key, value);
            }
        }

        return teamOrgEntities;
    }

    /**
     * Converts the given protobuf list of concept assessments into the common
     * object representation.
     * 
     * @param protoList the protobuf list of concepts
     * @return the common object list of concepts.
     */
    private static List<ConceptAssessment> convertList(List<AbstractAssessmentProto.AbstractAssessment> protoList) {
        if (CollectionUtils.isEmpty(protoList)) {
            return null;
        }

        List<ConceptAssessment> concepts = new ArrayList<>();

        for (AbstractAssessmentProto.AbstractAssessment concept : protoList) {
            concepts.add((ConceptAssessment) conceptCodec.convert(concept));
        }

        return concepts;
    }

    /**
     * Converts the given common object list of concept assessments into the
     * protobuf list representation.
     * 
     * @param commonList the list of concepts
     * @return the protobuf list of concepts.
     */
    private static List<AbstractAssessmentProto.AbstractAssessment> mapList(List<ConceptAssessment> commonList) {
        if (commonList == null) {
            return null;
        }

        List<AbstractAssessmentProto.AbstractAssessment> concepts = new ArrayList<>();

        for (ConceptAssessment concept : commonList) {
            concepts.add(conceptCodec.map(concept));
        }

        return concepts;
    }

    @Override
    public TaskAssessment convert(AbstractAssessmentProto.TaskAssessment protoObject) {
        if (protoObject == null) {
            return null;
        }

        AssessmentContainer container = protoObject.hasBaseAssessment() ? protoObject.getBaseAssessment() : null;

        List<ConceptAssessment> concepts = CollectionUtils.isNotEmpty(protoObject.getConceptsList())
                ? convertList(protoObject.getConceptsList())
                : null;

        if (concepts == null) {
            throw new MessageDecodeException(getClass().getName(),
                    "IntermediateConceptAssessmentProtoCodec does not have any ConceptAssessments.");
        }

        if (container != null) {
            int id = container.hasId() ? container.getId().getValue() : 0;
            long time = container.hasTime() ? ProtobufConversionUtil.convertTimestampToMillis(container.getTime()) : 0;
            String name = container.hasName() ? container.getName().getValue() : null;
            UUID courseNodeId = container.hasCourseId() ? UUID.fromString(container.getCourseId().getValue())
                    : UUID.randomUUID();
            AssessmentLevelEnum assessmentLevel = container.hasAssessment()
                    ? (AssessmentLevelEnum) enumCodec.convert(container.getAssessment())
                    : null;

            TaskAssessment tAss = new TaskAssessment(name, assessmentLevel, time, concepts, id,
                    courseNodeId);

            if (container.hasPriority()) {
                tAss.updatePriority(container.getPriority().getValue());
            }

            if (container.hasNodeState()) {
                tAss.setNodeStateEnum(PerformanceNodeStateEnum.valueOf(container.getNodeState().getValue()));
            }

            if (container.hasConfidence()) {
                tAss.updateConfidence(container.getConfidence().getValue());
            }

            if (container.hasCompetence()) {
                tAss.updateCompetence(container.getCompetence().getValue());
            }

            if (container.hasTrend()) {
                tAss.updateTrend(container.getTrend().getValue());
            }

            if (container.hasEvaluator()) {
                tAss.setEvaluator(container.getEvaluator().getValue());
            }

            if (container.hasObserverComment()) {
                tAss.setObserverComment(container.getObserverComment().getValue());
            }

            if (container.hasObserverMedia()) {
                tAss.setObserverMedia(container.getObserverMedia().getValue());
            }

            if (CollectionUtils.isNotEmpty(container.getAssessmentExplanationList())) {
                tAss.setAssessmentExplanation(new HashSet<>(container.getAssessmentExplanationList()));
            }

            if (CollectionUtils.isNotEmpty(container.getAssessedTeamOrgEntitiesMap())) {
                tAss.addAssessedTeamOrgEntries(ConceptAssessmentProtoCodec
                        .convertAssessedTeamOrgEntities(container.getAssessedTeamOrgEntitiesMap()));
            }

            if (container.hasObservedAssessment()) {
                tAss.setContainsObservedAssessmentCondition(container.getObservedAssessment().getValue());
            }

            if (container.hasScenarioSupport()) {
                tAss.setScenarioSupportNode(container.getScenarioSupport().getValue());
            }

            if (container.hasAssessmentHold()) {
                tAss.setAssessmentHold(container.getAssessmentHold().getValue());
            }

            if (container.hasTrendHold()) {
                tAss.setTrendHold(container.getTrendHold().getValue());
            }

            if (container.hasConfidenceHold()) {
                tAss.setConfidenceHold(container.getConfidenceHold().getValue());
            }

            if (container.hasCompetenceHold()) {
                tAss.setCompetenceHold(container.getCompetenceHold().getValue());
            }

            if (container.hasPriorityHold()) {
                tAss.setPriorityHold(container.getPriorityHold().getValue());
            }

            if (container.hasAuthoritativeResource()) {
                tAss.setAuthoritativeResource(container.getAuthoritativeResource().getValue());
            }
            
            if(protoObject.hasDifficulty()) {
                tAss.setDifficulty(protoObject.getDifficulty().getValue());
            }
            
            if(protoObject.hasDifficultyReason()) {
                tAss.setDifficultyReason(protoObject.getDifficultyReason().getValue());
            }
            
            if(protoObject.hasStress()) {
                tAss.setStress(protoObject.getStress().getValue());
            }
            
            if(protoObject.hasStressReason()) {
                tAss.setStressReason(protoObject.getStressReason().getValue());
            }

            return tAss;
        }

        return null;
    }

    @Override
    public AbstractAssessmentProto.TaskAssessment map(TaskAssessment commonObject) {
        if (commonObject == null) {
            return null;
        }

        AssessmentContainerProto.AssessmentContainer.Builder container = AssessmentContainerProto.AssessmentContainer
                .newBuilder();

        container.setTime(ProtobufConversionUtil.convertMillisToTimestamp(commonObject.getTime()));
        container.setId(Int32Value.of(commonObject.getNodeId()));
        container.setConfidence(FloatValue.of(commonObject.getConfidence()));
        container.setCompetence(FloatValue.of(commonObject.getCompetence()));
        container.setTrend(FloatValue.of(commonObject.getTrend()));
        container.setAssessmentHold(BoolValue.of(commonObject.isAssessmentHold()));
        container.setPriorityHold(BoolValue.of(commonObject.isPriorityHold()));
        container.setConfidenceHold(BoolValue.of(commonObject.isConfidenceHold()));
        container.setCompetenceHold(BoolValue.of(commonObject.isCompetenceHold()));
        container.setTrendHold(BoolValue.of(commonObject.isTrendHold()));
        container.setScenarioSupport(BoolValue.of(commonObject.isScenarioSupportNode()));
        container.setObservedAssessment(BoolValue.of(commonObject.isContainsObservedAssessmentCondition()));

        Optional.ofNullable(commonObject.getName()).ifPresent(name -> {
            container.setName(StringValue.of(name));
        });
        Optional.ofNullable(enumCodec.map(commonObject.getAssessmentLevel())).ifPresent(assessment -> {
            container.setAssessment(assessment);
        });
        Optional.ofNullable(commonObject.getNodeStateEnum().getName()).ifPresent(state -> {
            container.setNodeState(StringValue.of(state));
        });
        Optional.ofNullable(commonObject.getCourseNodeId().toString()).ifPresent(id -> {
            container.setCourseId(StringValue.of(id));
        });
        Optional.ofNullable(commonObject.getPriority()).ifPresent(priority -> {
            container.setPriority(Int32Value.of(priority));
        });
        Optional.ofNullable(commonObject.getEvaluator()).ifPresent(evaluator -> {
            container.setEvaluator(StringValue.of(evaluator));
        });
        Optional.ofNullable(commonObject.getObserverComment()).ifPresent(comment -> {
            container.setObserverComment(StringValue.of(comment));
        });
        Optional.ofNullable(commonObject.getObserverMedia()).ifPresent(media -> {
            container.setObserverMedia(StringValue.of(media));
        });
        Optional.ofNullable(commonObject.getAssessmentExplanation()).ifPresent(explanation -> {
            container.addAllAssessmentExplanation(explanation);
        });
        Optional.ofNullable(commonObject.getAssessedTeamOrgEntities()).ifPresent(entities -> {
            container.putAllAssessedTeamOrgEntities(ConceptAssessmentProtoCodec.mapAssessedTeamOrgEntities(entities));
        });
        Optional.ofNullable(commonObject.getAuthoritativeResource()).ifPresent(authRes -> {
            container.setAuthoritativeResource(StringValue.of(authRes));
        });
        
        AbstractAssessmentProto.TaskAssessment.Builder taskAssessmentBuilder = AbstractAssessmentProto.TaskAssessment.newBuilder();
        taskAssessmentBuilder.setBaseAssessment(container);
        taskAssessmentBuilder.addAllConcepts(mapList(commonObject.getConceptAssessments()));
        
        Optional.ofNullable(commonObject.getDifficulty()).ifPresent(difficulty -> {
            taskAssessmentBuilder.setDifficulty(DoubleValue.of(difficulty));
        });
        
        Optional.ofNullable(commonObject.getDifficultyReason()).ifPresent(difficultyReason -> {
            taskAssessmentBuilder.setDifficultyReason(StringValue.of(difficultyReason));
        });
        
        Optional.ofNullable(commonObject.getStress()).ifPresent(stress -> {
            taskAssessmentBuilder.setStress(DoubleValue.of(stress));
        });
        
        Optional.ofNullable(commonObject.getStressReason()).ifPresent(stressReason -> {
            taskAssessmentBuilder.setStressReason(StringValue.of(stressReason));
        });
        
        return taskAssessmentBuilder.build();
    }
}

