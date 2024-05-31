/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;

import com.google.protobuf.Int32Value;
import com.google.protobuf.StringValue;

import generated.proto.common.AbstractScoreNodeProto;
import generated.proto.common.AbstractScoreNodeProto.RawScoreNode.AssessmentLevel;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.score.AbstractRawScore;
import mil.arl.gift.common.score.RawScore;
import mil.arl.gift.common.score.RawScoreNode;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for protobuf encoding/decoding a RawScoreNode.
 * 
 * @author cpolynice
 * 
 */
public class RawScoreNodeProtoCodec implements ProtoCodec<AbstractScoreNodeProto.RawScoreNode, RawScoreNode> {
    /**
     * Codec used to convert to/from protobuf representations of an
     * AbstractRawScore.
     */
    private static AbstractRawScoreProtoCodec rawScoreCodec = new AbstractRawScoreProtoCodec();

    @Override
    public RawScoreNode convert(AbstractScoreNodeProto.RawScoreNode protoObject) {
        if (protoObject == null) {
            return null;
        }

        String name = protoObject.hasName() ? protoObject.getName().getValue() : null;
        AssessmentLevelEnum assessment = convert(protoObject.getAssessmentLevel());
        RawScore rawScore = null;
        
        String evaluator = protoObject.hasEvaluator() ? protoObject.getEvaluator().getValue() : null;
        String comment = protoObject.hasObserverComment() ? protoObject.getObserverComment().getValue() : null;
        String media = protoObject.hasObserverMedia() ? protoObject.getObserverMedia().getValue() : null;

        if (protoObject.hasRawScore()) {
            rawScore = rawScoreCodec.convert(protoObject.getRawScore());
        }
        
        RawScoreNode scoreNode;

        if (CollectionUtils.isNotEmpty(protoObject.getWhoList())) {
            scoreNode = new RawScoreNode(name, rawScore, assessment,
                    new HashSet<>(new ArrayList<>(protoObject.getWhoList())));
        } else {
            scoreNode = new RawScoreNode(name, rawScore, assessment);
        }

        scoreNode.setEvaluator(evaluator);
        scoreNode.setObserverComment(comment);
        scoreNode.setObserverMedia(media);
        
        return scoreNode;
    }

    @Override
    public AbstractScoreNodeProto.RawScoreNode map(RawScoreNode commonObject) {
        if (commonObject == null) {
            return null;
        }

        AbstractScoreNodeProto.RawScoreNode.Builder builder = AbstractScoreNodeProto.RawScoreNode.newBuilder();
        builder.setAssessmentLevel(convert(commonObject.getAssessment()));
        Optional.ofNullable(commonObject.getUsernames()).ifPresent(builder::addAllWho);
        Optional.ofNullable(rawScoreCodec.map((AbstractRawScore) commonObject.getRawScore()))
                .ifPresent(builder::setRawScore);
        Optional.ofNullable(commonObject.getName()).ifPresent(name -> {
            builder.setName(StringValue.of(name));
        });

        Optional.ofNullable(commonObject.getPerformanceNodeId()).ifPresent(performanceNodeId -> {
            builder.setPerformanceNodeId(Int32Value.of(performanceNodeId));
        });
        
        Optional.ofNullable(commonObject.getEvaluator()).ifPresent(evaluator -> {
            builder.setEvaluator(StringValue.of(evaluator));
        });
        
        Optional.ofNullable(commonObject.getObserverComment()).ifPresent(comment -> {
            builder.setObserverComment(StringValue.of(comment));
        });
        
        Optional.ofNullable(commonObject.getObserverMedia()).ifPresent(media -> {
            builder.setObserverMedia(StringValue.of(media));
        });

        return builder.build();
    }

    /**
     * Converts from {@link AssessmentLevel} to {@link AssessmentLevelEnum}.
     * 
     * @param type the type to convert.
     * @return the {@link AssessmentLevelEnum}
     */
    public static AssessmentLevelEnum convert(AssessmentLevel type) {
        if (type == null || AssessmentLevel.UNRECOGNIZED == type) {
            return null;
        } else if (AssessmentLevel.BELOW_EXPECTATION.equals(type)) {
            return AssessmentLevelEnum.BELOW_EXPECTATION;
        } else if (AssessmentLevel.AT_EXPECTATION.equals(type)) {
            return AssessmentLevelEnum.AT_EXPECTATION;
        } else if (AssessmentLevel.ABOVE_EXPECTATION.equals(type)) {
            return AssessmentLevelEnum.ABOVE_EXPECTATION;
        } else if (AssessmentLevel.UNKNOWN_ASSESSMENTLEVEL.equals(type)) {
            return AssessmentLevelEnum.UNKNOWN;
        } else {
            throw new UnsupportedOperationException("The assessment level '" + type + "' was not found.");
        }
    }

    /**
     * Converts from {@link AssessmentLevelEnum} to {@link AssessmentLevel}.
     * 
     * @param type the type to convert.
     * @return the {@link AssessmentLevel}
     */
    public static AssessmentLevel convert(AssessmentLevelEnum type) {
        if (type == null) {
            return AssessmentLevel.UNRECOGNIZED;
        } else if (AssessmentLevelEnum.BELOW_EXPECTATION.equals(type)) {
            return AssessmentLevel.BELOW_EXPECTATION;
        } else if (AssessmentLevelEnum.AT_EXPECTATION.equals(type)) {
            return AssessmentLevel.AT_EXPECTATION;
        } else if (AssessmentLevelEnum.ABOVE_EXPECTATION.equals(type)) {
            return AssessmentLevel.ABOVE_EXPECTATION;
        } else if (AssessmentLevelEnum.UNKNOWN.equals(type)) {
            return AssessmentLevel.UNKNOWN_ASSESSMENTLEVEL;
        } else {
            throw new UnsupportedOperationException("The assessment level '" + type + "' was not found.");
        }
    }
}
