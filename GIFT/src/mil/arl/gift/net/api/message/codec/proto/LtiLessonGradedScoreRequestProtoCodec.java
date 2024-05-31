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

import generated.proto.common.LtiLessonGradedScoreRequestProto;
import mil.arl.gift.common.lti.LtiLessonGradedScoreRequest;
import mil.arl.gift.common.score.GradedScoreNode;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf
 * LtiLessonGradedScoreRequest.
 * 
 * @author cpolynice
 *
 */
public class LtiLessonGradedScoreRequestProtoCodec implements
        ProtoCodec<LtiLessonGradedScoreRequestProto.LtiLessonGradedScoreRequest, LtiLessonGradedScoreRequest> {

    /* Codec that will be used to convert to/from a GradedScoreNode. */
    private static final GradedScoreNodeProtoCodec codec = new GradedScoreNodeProtoCodec();

    @Override
    public LtiLessonGradedScoreRequest convert(
            LtiLessonGradedScoreRequestProto.LtiLessonGradedScoreRequest protoObject) {
        if (protoObject == null) {
            return null;
        }

        GradedScoreNode gradedScoreNode = protoObject.hasScoreNode() ? codec.convert(protoObject.getScoreNode()) : null;
        List<String> concepts = CollectionUtils.isNotEmpty(protoObject.getConceptsList())
                ? new ArrayList<>(protoObject.getConceptsList())
                : new ArrayList<>();

        return new LtiLessonGradedScoreRequest(gradedScoreNode, concepts);
    }

    @Override
    public LtiLessonGradedScoreRequestProto.LtiLessonGradedScoreRequest map(
            LtiLessonGradedScoreRequest commonObject) {
        if (commonObject == null) {
            return null;
        }
        
        LtiLessonGradedScoreRequestProto.LtiLessonGradedScoreRequest.Builder builder = LtiLessonGradedScoreRequestProto.LtiLessonGradedScoreRequest.newBuilder();
        
        Optional.ofNullable(codec.map(commonObject.getGradedScoreNode())).ifPresent(builder::setScoreNode);
        Optional.ofNullable(commonObject.getCourseConcepts()).ifPresent(builder::addAllConcepts);
        
        return builder.build();
    }

}
