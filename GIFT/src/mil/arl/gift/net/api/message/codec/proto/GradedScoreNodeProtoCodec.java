/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.Optional;

import com.google.protobuf.Int32Value;
import com.google.protobuf.StringValue;

import generated.proto.common.AbstractScoreNodeProto;
import mil.arl.gift.common.EnumerationNotFoundException;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.score.AbstractScoreNode;
import mil.arl.gift.common.score.GradedScoreNode;
import mil.arl.gift.net.proto.ProtoCodec;

/* 
 *  This class is responsible for protobuf encoding/decoding a GradedScoreNode instance.
 * 
 *  @author cpolynice
 *  
 */
public class GradedScoreNodeProtoCodec implements ProtoCodec<AbstractScoreNodeProto.GradedScoreNode, GradedScoreNode> {
    /**
     * Codec that will assist in converting to/from protobuf representations of
     * class.
     */
    private static AbstractScoreNodeProtoCodec codec = new AbstractScoreNodeProtoCodec();

    @Override
    public GradedScoreNode convert(AbstractScoreNodeProto.GradedScoreNode protoObject) {
        if (protoObject == null) {
            return null;
        }

        String name = protoObject.hasName() ? protoObject.getName().getValue() : null;
        AssessmentLevelEnum assessmentLevel = AssessmentLevelEnum.UNKNOWN;
        try {
            assessmentLevel = protoObject.hasGrade() ? AssessmentLevelEnum.valueOf(protoObject.getGrade().getValue()) : assessmentLevel;
        }catch(@SuppressWarnings("unused") EnumerationNotFoundException e) {
            // Legacy messages - convert old PassFailEnum name to new AssessmentLevelEnum (#5197), other enums cases default to unknown            
            assessmentLevel = AssessmentLevelEnum.fromPassFailEnum(protoObject.getGrade().getValue());            
        }

        String evaluator = protoObject.hasEvaluator() ? protoObject.getEvaluator().getValue() : null;
        String comment = protoObject.hasObserverComment() ? protoObject.getObserverComment().getValue() : null;
        String media = protoObject.hasObserverMedia() ? protoObject.getObserverMedia().getValue() : null;
        
        GradedScoreNode gsn = new GradedScoreNode(name, assessmentLevel);

        convertChildren(protoObject, gsn);

        if (protoObject.hasPerformanceNodeId()) {
            gsn.setPerformanceNodeId(protoObject.getPerformanceNodeId().getValue());
        }
        
        gsn.setEvaluator(evaluator);
        gsn.setObserverComment(comment);
        gsn.setObserverMedia(media);

        return gsn;
    }
    
    /**
     * Converts the child node proto objects into AbstractScoreNode objects and adds them as children to the graded score node
     * @param gradedScoreNodeProtoObject contains zero or more children proto objects to convert.  Can't be null.
     * @param gsn where to place the converted, new, non-proto objects.  Can't be null.
     */
    public static void convertChildren(AbstractScoreNodeProto.GradedScoreNode gradedScoreNodeProtoObject, GradedScoreNode gsn) {
        
        for (AbstractScoreNodeProto.AbstractScoreNode child : gradedScoreNodeProtoObject.getChildrenList()) {
            AbstractScoreNode node = codec.convert(child);
            if (node != null) {
                gsn.addChild(node);
            }
        }
    }

    @Override
    public AbstractScoreNodeProto.GradedScoreNode map(GradedScoreNode commonObject) {
        if (commonObject == null) {
            return null;
        }

        final AbstractScoreNodeProto.GradedScoreNode.Builder builder = AbstractScoreNodeProto.GradedScoreNode
                .newBuilder();

        Optional.ofNullable(commonObject.getName()).ifPresent(name -> {
            builder.setName(StringValue.of(name));
        });

        Optional.ofNullable(commonObject.getPerformanceNodeId()).ifPresent(performanceNodeId -> {
            builder.setPerformanceNodeId(Int32Value.of(performanceNodeId));
        });

        Optional.ofNullable(commonObject.getAssessment().toString()).ifPresent(grade -> {
            builder.setGrade(StringValue.of(grade));
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
     
        mapChildren(commonObject, builder);

        return builder.build();
    }
    
    /**
     * Converts the graded score node children into protobuf objects
     * @param commonObject the object that contains zero or more children to convert to protobuf.  Can't be null.
     * @param builder where the protobuf objects are created.  Can't be null.
     */
    public static void mapChildren(GradedScoreNode commonObject, final AbstractScoreNodeProto.GradedScoreNode.Builder builder) {
        
        if (commonObject.getChildren() != null) {
            for (AbstractScoreNode child : commonObject.getChildren()) {
                AbstractScoreNodeProto.AbstractScoreNode value = codec.map(child);
                if (value != null) {
                    builder.addChildren(value);
                }
            }
        }
    }

}
