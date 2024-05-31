/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.Optional;

import com.google.protobuf.DoubleValue;
import com.google.protobuf.Int32Value;
import com.google.protobuf.StringValue;

import generated.proto.common.AbstractScoreNodeProto;
import mil.arl.gift.common.EnumerationNotFoundException;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.score.TaskScoreNode;
import mil.arl.gift.net.proto.ProtoCodec;

/* 
 *  This class is responsible for protobuf encoding/decoding a TaskScoreNode instance.
 * 
 *  @author mhoffman
 *  
 */
public class TaskScoreNodeProtoCodec implements ProtoCodec<AbstractScoreNodeProto.TaskScoreNode, TaskScoreNode> {

    @Override
    public TaskScoreNode convert(AbstractScoreNodeProto.TaskScoreNode protoObject) {
        if (protoObject == null) {
            return null;
        }

        String name = protoObject.getGradedScoreNode().hasName() ? protoObject.getGradedScoreNode().getName().getValue() : null;

        AssessmentLevelEnum assessmentLevel = AssessmentLevelEnum.UNKNOWN;
        try {
            assessmentLevel = protoObject.getGradedScoreNode().hasGrade() ? AssessmentLevelEnum.valueOf(protoObject.getGradedScoreNode().getGrade().getValue()) : assessmentLevel;
        }catch(@SuppressWarnings("unused") EnumerationNotFoundException e) {
            // Legacy messages - convert old PassFailEnum name to new AssessmentLevelEnum (#5197), other enums cases default to unknown            
            assessmentLevel = AssessmentLevelEnum.fromPassFailEnum(protoObject.getGradedScoreNode().getGrade().getValue());            
        }

        String evaluator = protoObject.getGradedScoreNode().hasEvaluator() ? protoObject.getGradedScoreNode().getEvaluator().getValue() : null;
        String comment = protoObject.getGradedScoreNode().hasObserverComment() ? protoObject.getGradedScoreNode().getObserverComment().getValue() : null;
        String media = protoObject.getGradedScoreNode().hasObserverMedia() ? protoObject.getGradedScoreNode().getObserverMedia().getValue() : null;
        TaskScoreNode tsn = new TaskScoreNode(name, assessmentLevel);

        GradedScoreNodeProtoCodec.convertChildren(protoObject.getGradedScoreNode(), tsn);

        if (protoObject.getGradedScoreNode().hasPerformanceNodeId()) {
            tsn.setPerformanceNodeId(protoObject.getGradedScoreNode().getPerformanceNodeId().getValue());
        }
        
        if(protoObject.hasDifficulty()) {
            tsn.setDifficulty(protoObject.getDifficulty().getValue());
        }
        
        if(protoObject.hasDifficultyReason()) {
            tsn.setDifficultyReason(protoObject.getDifficultyReason().getValue());
        }
        
        if(protoObject.hasStress()) {
            tsn.setStress(protoObject.getStress().getValue());
        }
        
        if(protoObject.hasStressReason()) {
            tsn.setStressReason(protoObject.getStressReason().getValue());
        }
        
        tsn.setEvaluator(evaluator);
        tsn.setObserverComment(comment);
        tsn.setObserverMedia(media);

        return tsn;
    }

    @Override
    public AbstractScoreNodeProto.TaskScoreNode map(TaskScoreNode commonObject) {
        if (commonObject == null) {
            return null;
        }

        final AbstractScoreNodeProto.TaskScoreNode.Builder builder = AbstractScoreNodeProto.TaskScoreNode
                .newBuilder();

        Optional.ofNullable(commonObject.getName()).ifPresent(name -> {
            builder.getGradedScoreNodeBuilder().setName(StringValue.of(name));
        });

        Optional.ofNullable(commonObject.getPerformanceNodeId()).ifPresent(performanceNodeId -> {
            builder.getGradedScoreNodeBuilder().setPerformanceNodeId(Int32Value.of(performanceNodeId));
        });

        Optional.ofNullable(commonObject.getAssessment().toString()).ifPresent(grade -> {
            builder.getGradedScoreNodeBuilder().setGrade(StringValue.of(grade));
        });
        
        Optional.ofNullable(commonObject.getEvaluator()).ifPresent(evaluator -> {
            builder.getGradedScoreNodeBuilder().setEvaluator(StringValue.of(evaluator));
        });
        
        Optional.ofNullable(commonObject.getObserverComment()).ifPresent(comment -> {
            builder.getGradedScoreNodeBuilder().setObserverComment(StringValue.of(comment));
        });
        
        Optional.ofNullable(commonObject.getObserverMedia()).ifPresent(media -> {
            builder.getGradedScoreNodeBuilder().setObserverMedia(StringValue.of(media));
        });
     
        GradedScoreNodeProtoCodec.mapChildren(commonObject, builder.getGradedScoreNodeBuilder());
        
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
