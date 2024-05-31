/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.google.protobuf.BoolValue;
import com.google.protobuf.FloatValue;
import com.google.protobuf.Int32Value;
import com.google.protobuf.StringValue;

import generated.proto.common.AbstractAssessmentProto;
import generated.proto.common.AbstractEnumObjectProto.AbstractEnumObject;
import generated.proto.common.AssessmentContainerProto;
import mil.arl.gift.common.AbstractAssessment.PerformanceNodeStateEnum;
import mil.arl.gift.common.ConceptAssessment;
import mil.arl.gift.common.IntermediateConceptAssessment;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.common.util.ProtobufConversionUtil;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for protobuf encoding/decoding a Concept Assessment
 * message.
 * 
 * @author cpolynice
 *
 */
public class ConceptAssessmentProtoCodec
        implements ProtoCodec<AbstractAssessmentProto.ConceptAssessment, ConceptAssessment> {

    /* Codec that will be used to convert to/from enumerations. */
    private static AbstractEnumObjectProtoCodec enumCodec = new AbstractEnumObjectProtoCodec();

    /* Codec that will be used to convert to/from an Intermediate Concept
     * Assessment message. */
    private static IntermediateConceptAssessmentProtoCodec iConceptCodec = new IntermediateConceptAssessmentProtoCodec();

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

    @Override
    public ConceptAssessment convert(AbstractAssessmentProto.ConceptAssessment protoObject) {
        if (protoObject == null) {
            return null;
        }

        if (protoObject.hasAssessmentData()) {
            AssessmentContainerProto.AssessmentContainer container = protoObject.getAssessmentData();
            int id = container.hasId() ? container.getId().getValue() : 0;
            long time = container.hasTime() ? ProtobufConversionUtil.convertTimestampToMillis(container.getTime()) : 0;
            String name = container.hasName() ? container.getName().getValue() : null;
            UUID courseNodeId = container.hasCourseId() ? UUID.fromString(container.getCourseId().getValue())
                    : UUID.randomUUID();
            AssessmentLevelEnum assessmentLevel = container.hasAssessment()
                    ? (AssessmentLevelEnum) enumCodec.convert(container.getAssessment())
                    : null;

            ConceptAssessment cAss = new ConceptAssessment(name, assessmentLevel, time, id, courseNodeId);

            if (container.hasPriority()) {
                cAss.updatePriority(container.getPriority().getValue());
            }

            if (container.hasNodeState()) {
                cAss.setNodeStateEnum(PerformanceNodeStateEnum.valueOf(container.getNodeState().getValue()));
            }

            if (container.hasConfidence()) {
                cAss.updateConfidence(container.getConfidence().getValue());
            }

            if (container.hasCompetence()) {
                cAss.updateCompetence(container.getCompetence().getValue());
            }

            if (container.hasTrend()) {
                cAss.updateTrend(container.getTrend().getValue());
            }

            if (container.hasEvaluator()) {
                cAss.setEvaluator(container.getEvaluator().getValue());
            }

            if (container.hasObserverComment()) {
                cAss.setObserverComment(container.getObserverComment().getValue());
            }

            if (container.hasObserverMedia()) {
                cAss.setObserverMedia(container.getObserverMedia().getValue());
            }

            if (CollectionUtils.isNotEmpty(container.getAssessmentExplanationList())) {
                cAss.setAssessmentExplanation(new HashSet<>(container.getAssessmentExplanationList()));
            }
            
            if (CollectionUtils.isNotEmpty(container.getAssessedTeamOrgEntitiesMap())) {
                cAss.addAssessedTeamOrgEntries(
                        convertAssessedTeamOrgEntities(container.getAssessedTeamOrgEntitiesMap()));
            }

            if (container.hasObservedAssessment()) {
                cAss.setContainsObservedAssessmentCondition(container.getObservedAssessment().getValue());
            }

            if (container.hasScenarioSupport()) {
                cAss.setScenarioSupportNode(container.getScenarioSupport().getValue());
            }

            if (container.hasAssessmentHold()) {
                cAss.setAssessmentHold(container.getAssessmentHold().getValue());
            }

            if (container.hasTrendHold()) {
                cAss.setTrendHold(container.getTrendHold().getValue());
            }

            if (container.hasConfidenceHold()) {
                cAss.setConfidenceHold(container.getConfidenceHold().getValue());
            }

            if (container.hasCompetenceHold()) {
                cAss.setCompetenceHold(container.getCompetenceHold().getValue());
            }

            if (container.hasPriorityHold()) {
                cAss.setPriorityHold(container.getPriorityHold().getValue());
            }

            if (container.hasAuthoritativeResource()) {
                cAss.setAuthoritativeResource(container.getAuthoritativeResource().getValue());
            }

            return cAss;
        } else if (protoObject.hasIntermediateConceptAssessment()) {
            return iConceptCodec.convert(protoObject.getIntermediateConceptAssessment());
        } else {
            return null;
        }
    }

    @Override
    public AbstractAssessmentProto.ConceptAssessment map(ConceptAssessment commonObject) {
        if (commonObject == null) {
            return null;
        }

        AbstractAssessmentProto.ConceptAssessment.Builder builder = AbstractAssessmentProto.ConceptAssessment
                .newBuilder();
        AssessmentContainerProto.AssessmentContainer.Builder container = AssessmentContainerProto.AssessmentContainer
                .newBuilder();

        if (commonObject instanceof IntermediateConceptAssessment) {
            builder.setIntermediateConceptAssessment(iConceptCodec.map((IntermediateConceptAssessment) commonObject));
        } else {
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
                container.putAllAssessedTeamOrgEntities(mapAssessedTeamOrgEntities(entities));
            });
            Optional.ofNullable(commonObject.getAuthoritativeResource()).ifPresent(authRes -> {
                container.setAuthoritativeResource(StringValue.of(authRes));
            });

            builder.setAssessmentData(container);
        }

        return builder.build();
    }
}
