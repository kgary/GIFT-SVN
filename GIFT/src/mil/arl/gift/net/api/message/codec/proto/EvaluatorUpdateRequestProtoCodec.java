/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.google.protobuf.BoolValue;
import com.google.protobuf.FloatValue;
import com.google.protobuf.Int32Value;
import com.google.protobuf.StringValue;

import generated.proto.common.AbstractEnumObjectProto.AbstractEnumObject;
import generated.proto.common.EvaluatorUpdateRequestProto;
import mil.arl.gift.common.AbstractAssessment.PerformanceNodeStateEnum;
import mil.arl.gift.common.EvaluatorUpdateRequest;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.common.util.ProtobufConversionUtil;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf Evaluator Update
 * Request.
 * 
 * @author cpolynice
 *
 */
public class EvaluatorUpdateRequestProtoCodec
        implements ProtoCodec<EvaluatorUpdateRequestProto.EvaluatorUpdateRequest, EvaluatorUpdateRequest> {

    /* Codec that will be used to convert to/from an abstract enum. */
    private static AbstractEnumObjectProtoCodec enumCodec = new AbstractEnumObjectProtoCodec();

    /**
     * Converts the given protobuf map into the common object representation.
     * 
     * @param protoMap the protobuf map
     * @return the common object map.
     */
    private static Map<String, AssessmentLevelEnum> convertEntities(Map<String, AbstractEnumObject> protoMap) {
        if (protoMap == null) {
            return null;
        }

        Map<String, AssessmentLevelEnum> commonMap = new HashMap<>();

        for (Map.Entry<String, AbstractEnumObject> entity : protoMap.entrySet()) {
            String key = entity.getKey();
            AssessmentLevelEnum value = (AssessmentLevelEnum) enumCodec.convert(entity.getValue());

            if (value != null) {
                commonMap.put(key, value);
            }
        }

        return commonMap;
    }

    /**
     * Maps the given common object map into the protobuf representation.
     * 
     * @param commonMap the common object map
     * @return the protobuf map representation.
     */
    private static Map<String, AbstractEnumObject> mapEntities(Map<String, AssessmentLevelEnum> commonMap) {
        if (commonMap == null) {
            return null;
        }

        Map<String, AbstractEnumObject> protoMap = new HashMap<>();

        for (Map.Entry<String, AssessmentLevelEnum> entity : commonMap.entrySet()) {
            String key = entity.getKey();
            AbstractEnumObject value = enumCodec.map(entity.getValue());

            if (value != null) {
                protoMap.put(key, value);
            }
        }

        return protoMap;
    }

    @Override
    public EvaluatorUpdateRequest convert(EvaluatorUpdateRequestProto.EvaluatorUpdateRequest protoObject) {
        if (protoObject == null) {
            return null;
        }
        
        String name = protoObject.hasName() ? protoObject.getName().getValue() : null;
        String evaluator = protoObject.hasEvaluator() ? protoObject.getEvaluator().getValue() : null;
        long timeStamp = protoObject.hasTimestamp()
                ? ProtobufConversionUtil.convertTimestampToMillis(protoObject.getTimestamp())
                : 0;

        EvaluatorUpdateRequest request = new EvaluatorUpdateRequest(name, evaluator, timeStamp);

        if (protoObject.hasConfidence()) {
            request.setConfidenceMetric(protoObject.getCompetence().getValue());
        }
        
        if (protoObject.hasCompetence()) {
            request.setCompetenceMetric(protoObject.getCompetence().getValue());
        }

        if (protoObject.hasTrend()) {
            request.setTrendMetric(protoObject.getTrend().getValue());
        }

        if (protoObject.hasPriority()) {
            request.setPriorityMetric(protoObject.getPriority().getValue());
        }

        if (protoObject.hasState()) {
            request.setState(PerformanceNodeStateEnum.valueOf(protoObject.getState().getValue()));
        }

        if (protoObject.hasAssessmentHold()) {
            request.setAssessmentHold(protoObject.getAssessmentHold().getValue());
        }

        if (protoObject.hasConfidenceHold()) {
            request.setConfidenceHold(protoObject.getConfidenceHold().getValue());
        }

        if (protoObject.hasCompetenceHold()) {
            request.setCompetenceHold(protoObject.getCompetenceHold().getValue());
        }

        if (protoObject.hasTrendHold()) {
            request.setTrendHold(protoObject.getTrendHold().getValue());
        }

        if (protoObject.hasPriorityHold()) {
            request.setPriorityHold(protoObject.getPriorityHold().getValue());
        }

        if (protoObject.hasPerformance()) {
            request.setPerformanceMetric((AssessmentLevelEnum) enumCodec.convert(protoObject.getPerformance()));
        }

        if (protoObject.hasReason()) {
            request.setReason(protoObject.getReason().getValue());
        }

        if (protoObject.hasMediaFile()) {
            request.setMediaFile(protoObject.getMediaFile().getValue());
        }

        if (CollectionUtils.isNotEmpty(protoObject.getTeamOrgEntitiesMap())) {
            request.setTeamOrgEntities(convertEntities(protoObject.getTeamOrgEntitiesMap()));
        }

        return request;
    }

    @Override
    public EvaluatorUpdateRequestProto.EvaluatorUpdateRequest map(EvaluatorUpdateRequest commonObject) {
        if (commonObject == null) {
            return null;
        }

        EvaluatorUpdateRequestProto.EvaluatorUpdateRequest.Builder builder = EvaluatorUpdateRequestProto.EvaluatorUpdateRequest
                .newBuilder();

        builder.setTimestamp(ProtobufConversionUtil.convertMillisToTimestamp(commonObject.getTimestamp()));

        Optional.ofNullable(commonObject.isAssessmentHold()).ifPresent(assessment -> {
            builder.setAssessmentHold(BoolValue.of(assessment));
        });
        Optional.ofNullable(commonObject.isConfidenceHold()).ifPresent(confidence -> {
            builder.setConfidenceHold(BoolValue.of(confidence));
        });
        Optional.ofNullable(commonObject.isCompetenceHold()).ifPresent(competence -> {
            builder.setCompetenceHold(BoolValue.of(competence));
        });
        Optional.ofNullable(commonObject.isPriorityHold()).ifPresent(priority -> {
            builder.setPriorityHold(BoolValue.of(priority));
        });
        Optional.ofNullable(commonObject.isTrendHold()).ifPresent(trend -> {
            builder.setTrendHold(BoolValue.of(trend));
        });

        Optional.ofNullable(enumCodec.map(commonObject.getPerformanceMetric())).ifPresent(builder::setPerformance);
        Optional.ofNullable(mapEntities(commonObject.getTeamOrgEntities())).ifPresent(builder::putAllTeamOrgEntities);
        Optional.ofNullable(commonObject.getEvaluator()).ifPresent(evaluator -> {
            builder.setEvaluator(StringValue.of(evaluator));
        });
        Optional.ofNullable(commonObject.getNodeName()).ifPresent(name -> {
            builder.setName(StringValue.of(name));
        });
        Optional.ofNullable(commonObject.getConfidenceMetric()).ifPresent(confidence -> {
            builder.setConfidence(FloatValue.of(confidence));
        });
        Optional.ofNullable(commonObject.getCompetenceMetric()).ifPresent(competence -> {
            builder.setCompetence(FloatValue.of(competence));
        });
        Optional.ofNullable(commonObject.getTrendMetric()).ifPresent(trend -> {
            builder.setTrend(FloatValue.of(trend));
        });
        Optional.ofNullable(commonObject.getPriorityMetric()).ifPresent(priority -> {
            builder.setPriority(Int32Value.of(priority));
        });
        Optional.ofNullable(commonObject.getState()).ifPresent(state -> {
            builder.setState(StringValue.of(state.name()));
        });
        Optional.ofNullable(commonObject.getReason()).ifPresent(reason -> {
            builder.setReason(StringValue.of(reason));
        });
        Optional.ofNullable(commonObject.getMediaFile()).ifPresent(file -> {
            builder.setMediaFile(StringValue.of(file));
        });

        return builder.build();
    }
}
