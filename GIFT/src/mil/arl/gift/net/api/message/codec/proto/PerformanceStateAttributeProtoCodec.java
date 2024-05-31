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
import com.google.protobuf.Int64Value;
import com.google.protobuf.StringValue;

import generated.proto.common.AbstractPerformanceStateAttributeProto;
import mil.arl.gift.common.AbstractAssessment.PerformanceNodeStateEnum;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.state.PerformanceStateAttribute;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a PerformanceStateAttribute
 * message.
 * 
 * @author cpolynice
 *
 */
public class PerformanceStateAttributeProtoCodec implements ProtoCodec<AbstractPerformanceStateAttributeProto.PerformanceStateAttribute, PerformanceStateAttribute> {

    /* Codec that will be used to convert the given protobuf enum into the
     * common object representation. */
    private static AbstractEnumObjectProtoCodec enumCodec = new AbstractEnumObjectProtoCodec();

    /**
     * Converts a protobuf map to the common object map using attributes
     * specific to the common object instance.
     * 
     * @param protoMap the map of assessed team org entites in protobuf
     * @return the common object team org entities map
     */
    public static Map<String, AssessmentLevelEnum> convertAssessedTeamOrgEntities(Map<String, String> protoMap) {
        if (protoMap == null) {
            return null;
        }

        Map<String, AssessmentLevelEnum> teamOrgEntities = new HashMap<>();

        for (Map.Entry<String, String> entity : protoMap.entrySet()) {
            String key = entity.getKey();
            AssessmentLevelEnum value = AssessmentLevelEnum.valueOf(entity.getValue());

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
    public static Map<String, String> mapAssessedTeamOrgEntities(Map<String, AssessmentLevelEnum> commonMap) {
        if (commonMap == null) {
            return null;
        }

        Map<String, String> teamOrgEntities = new HashMap<>();

        for (Map.Entry<String, AssessmentLevelEnum> entity : commonMap.entrySet()) {
            String key = entity.getKey();
            AssessmentLevelEnum value = entity.getValue();

            if (value != null) {
                teamOrgEntities.put(key, value.getName());
            }
        }

        return teamOrgEntities;
    }

    @Override
    public PerformanceStateAttribute convert(AbstractPerformanceStateAttributeProto.PerformanceStateAttribute protoObject) {
        if (protoObject == null) {
            return null;
        }
          
        String name = protoObject.hasName() ? protoObject.getName().getValue() : null;
        int id = protoObject.hasId() ? protoObject.getId().getValue() : 0;
        String courseId = protoObject.hasCourseId() ? protoObject.getCourseId().getValue() : null;
        AssessmentLevelEnum shortTerm = protoObject.hasShortTerm()
                ? (AssessmentLevelEnum) enumCodec.convert(protoObject.getShortTerm())
                : null;
        AssessmentLevelEnum longTerm = protoObject.hasLongTerm()
                ? (AssessmentLevelEnum) enumCodec.convert(protoObject.getLongTerm())
                : null;
        AssessmentLevelEnum predicted = protoObject.hasPredicted()
                ? (AssessmentLevelEnum) enumCodec.convert(protoObject.getPredicted())
                : null;
        Long shortTermTimestamp = protoObject.hasShortTermTimestamp() ? protoObject.getShortTermTimestamp().getValue() : null;
        long longTermTimestamp = protoObject.hasLongTermTimestamp() ? protoObject.getLongTermTimestamp().getValue() : 0;
        long predictedTimestamp = protoObject.hasPredictedTimestamp() ? protoObject.getPredictedTimestamp().getValue()
                : 0;
        
        PerformanceStateAttribute pStateAttr;     
        if (shortTermTimestamp == null) {
            /* Legacy message format. */
            pStateAttr = new PerformanceStateAttribute(name, id, UUID.randomUUID().toString(), shortTerm, longTerm,
                    predicted);
        } else {
            pStateAttr = new PerformanceStateAttribute(name, id, courseId, shortTerm, shortTermTimestamp, longTerm,
                    longTermTimestamp, predicted, predictedTimestamp);
        }

        if (protoObject.hasEvaluator()) {
            pStateAttr.setEvaluator(protoObject.getEvaluator().getValue());
        }

        if (protoObject.hasObserverComment()) {
            pStateAttr.setObserverComment(protoObject.getObserverComment().getValue());
        }

        if (protoObject.hasObserverMedia()) {
            pStateAttr.setObserverMedia(protoObject.getObserverMedia().getValue());
        }

        if (CollectionUtils.isNotEmpty(protoObject.getAssessmentExplanationList())) {
            pStateAttr.setAssessmentExplanation(new HashSet<>(protoObject.getAssessmentExplanationList()), true);
        }

        if (CollectionUtils.isNotEmpty(protoObject.getAssessedTeamOrgEntitiesMap())) {
            pStateAttr.setAssessedTeamOrgEntities(
                    convertAssessedTeamOrgEntities(protoObject.getAssessedTeamOrgEntitiesMap()));
        }

        if (protoObject.hasNodeState()) {
            pStateAttr.setNodeStateEnum(PerformanceNodeStateEnum.valueOf(protoObject.getNodeState().getValue()));
        }

        if (protoObject.hasConfidence()) {
            pStateAttr.setConfidence(protoObject.getConfidence().getValue(), true);
        }

        if (protoObject.hasCompetence()) {
            pStateAttr.setCompetence(protoObject.getCompetence().getValue(), true);
        }

        if (protoObject.hasPriority()) {
            pStateAttr.setPriority(protoObject.getPriority().getValue(), true);
        }

        if (protoObject.hasTrend()) {
            pStateAttr.setTrend(protoObject.getTrend().getValue(), true);
        }

        if (protoObject.hasScenarioSupport()) {
            pStateAttr.setScenarioSupportNode(protoObject.getScenarioSupport().getValue());
        }

        if (protoObject.hasAssessmentHold()) {
            pStateAttr.setAssessmentHold(protoObject.getAssessmentHold().getValue());
        }

        if (protoObject.hasConfidenceHold()) {
            pStateAttr.setConfidenceHold(protoObject.getConfidenceHold().getValue());
        }

        if (protoObject.hasCompetenceHold()) {
            pStateAttr.setCompetenceHold(protoObject.getCompetenceHold().getValue());
        }

        if (protoObject.hasPriorityHold()) {
            pStateAttr.setPriorityHold(protoObject.getPriorityHold().getValue());
        }

        if (protoObject.hasTrendHold()) {
            pStateAttr.setTrendHold(protoObject.getTrendHold().getValue());
        }

        if (protoObject.hasAuthoritativeResource()) {
            pStateAttr.setAuthoritativeResource(protoObject.getAuthoritativeResource().getValue());
        }

        return pStateAttr;
    }

    @Override
    public AbstractPerformanceStateAttributeProto.PerformanceStateAttribute map(
            PerformanceStateAttribute commonObject) {
        if (commonObject == null) {
            return null;
        }

        AbstractPerformanceStateAttributeProto.PerformanceStateAttribute.Builder builder = AbstractPerformanceStateAttributeProto.PerformanceStateAttribute
                .newBuilder();

        builder.setId(Int32Value.of(commonObject.getNodeId()));
        Optional.ofNullable(commonObject.getName()).ifPresent(name -> {
            builder.setName(StringValue.of(name));
        });
        Optional.ofNullable(commonObject.getNodeCourseId()).ifPresent(id -> {
            builder.setCourseId(StringValue.of(id));
        });
        Optional.ofNullable(enumCodec.map(commonObject.getShortTerm())).ifPresent(builder::setShortTerm);
        builder.setShortTermTimestamp(Int64Value.of(commonObject.getShortTermTimestamp()));
        Optional.ofNullable(enumCodec.map(commonObject.getLongTerm())).ifPresent(builder::setLongTerm);
        builder.setLongTermTimestamp(Int64Value.of(commonObject.getLongTermTimestamp()));
        Optional.ofNullable(enumCodec.map(commonObject.getPredicted())).ifPresent(builder::setPredicted);
        builder.setPredictedTimestamp(Int64Value.of(commonObject.getPredictedTimestamp()));
        Optional.ofNullable(commonObject.getNodeStateEnum()).ifPresent(state -> {
            builder.setNodeState(StringValue.of(state.getName()));
        });
        builder.setConfidence(FloatValue.of(commonObject.getConfidence()));
        builder.setCompetence(FloatValue.of(commonObject.getCompetence()));
        builder.setTrend(FloatValue.of(commonObject.getTrend()));
        builder.setAssessmentHold(BoolValue.of(commonObject.isAssessmentHold()));
        builder.setPriorityHold(BoolValue.of(commonObject.isPriorityHold()));
        builder.setConfidenceHold(BoolValue.of(commonObject.isConfidenceHold()));
        builder.setCompetenceHold(BoolValue.of(commonObject.isCompetenceHold()));
        builder.setTrendHold(BoolValue.of(commonObject.isTrendHold()));
        builder.setScenarioSupport(BoolValue.of(commonObject.isScenarioSupportNode()));

        /* Optional priority value for the performance assessment node. */
        Optional.ofNullable(commonObject.getPriority()).ifPresent(priority -> {
            builder.setPriority(Int32Value.of(priority));
        });

        /* Optional evaluator value for the performance assessment node. */
        Optional.ofNullable(commonObject.getEvaluator()).ifPresent(eval -> {
            builder.setEvaluator(StringValue.of(eval));
        });

        /* Optional observer comment value for the performance assessment
         * node. */
        Optional.ofNullable(commonObject.getObserverComment()).ifPresent(comment -> {
            builder.setObserverComment(StringValue.of(comment));
        });

        /* Optional observer media value for the performance assessment node. */
        Optional.ofNullable(commonObject.getObserverMedia()).ifPresent(media -> {
            builder.setObserverMedia(StringValue.of(media));
        });

        /* Optional authoritative resource value for the performance assessment
         * node. */
        Optional.ofNullable(commonObject.getAuthoritativeResource()).ifPresent(authRes -> {
            builder.setAuthoritativeResource(StringValue.of(authRes));
        });

        /* Optional assessment explanation value for the performance assessment
         * node. */
        Optional.ofNullable(commonObject.getAssessmentExplanation()).ifPresent(builder::addAllAssessmentExplanation);

        Optional.ofNullable(mapAssessedTeamOrgEntities(commonObject.getAssessedTeamOrgEntities()))
                .ifPresent(builder::putAllAssessedTeamOrgEntities);

        return builder.build();
    }

}
