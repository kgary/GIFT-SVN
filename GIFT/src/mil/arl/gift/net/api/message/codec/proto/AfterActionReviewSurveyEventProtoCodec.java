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

import com.google.protobuf.StringValue;

import generated.proto.common.AbstractScoreProto;
import generated.proto.common.AfterActionReviewSurveyEventProto;
import mil.arl.gift.common.AfterActionReviewSurveyEvent;
import mil.arl.gift.common.survey.SurveyResponseMetadata;
import mil.arl.gift.common.survey.score.ScoreInterface;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.net.api.message.codec.proto.survey.SurveyResponseMetadataProtoCodec;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from an
 * AfterActionReviewSurveyEvent.
 * 
 * @author cpolynice
 *
 */
public class AfterActionReviewSurveyEventProtoCodec implements
        ProtoCodec<AfterActionReviewSurveyEventProto.AfterActionReviewSurveyEvent, AfterActionReviewSurveyEvent> {

    private static SurveyResponseMetadataProtoCodec responseMetadataCodec = new SurveyResponseMetadataProtoCodec();

    private static AbstractScoreProtoCodec scoreCodec = new AbstractScoreProtoCodec();

    @Override
    public AfterActionReviewSurveyEvent convert(AfterActionReviewSurveyEventProto.AfterActionReviewSurveyEvent protoObject) {
        if (protoObject == null) {
            return null;
        }
        
        SurveyResponseMetadata surveyResponseMetadata = null;

        if (protoObject.hasSurveyResponseMetadata()) {
            surveyResponseMetadata = responseMetadataCodec.convert(protoObject.getSurveyResponseMetadata());
        }

        List<ScoreInterface> scores = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(protoObject.getScoresList())) {
            for (AbstractScoreProto.AbstractScore score : protoObject.getScoresList()) {
                scores.add(scoreCodec.convert(score));
            }
        }
        
        String courseObjectName = protoObject.hasCourseObjectName() ? protoObject.getCourseObjectName().getValue()
                : AbstractAfterActionReviewEventProtoCodec.LEGACY_COURSE_OBJECT_NAME;
        
        return new AfterActionReviewSurveyEvent(courseObjectName, surveyResponseMetadata, scores);
    }

    @Override
    public AfterActionReviewSurveyEventProto.AfterActionReviewSurveyEvent map(AfterActionReviewSurveyEvent commonObject) {
        if (commonObject == null) {
            return null;
        }

        AfterActionReviewSurveyEventProto.AfterActionReviewSurveyEvent.Builder builder = AfterActionReviewSurveyEventProto.AfterActionReviewSurveyEvent
                .newBuilder();
        Optional.ofNullable(responseMetadataCodec.map(commonObject.getSurveyResponseMetadata()))
                .ifPresent(builder::setSurveyResponseMetadata);

        if (CollectionUtils.isNotEmpty(commonObject.getSurveyScores())) {
            for (ScoreInterface scores : commonObject.getSurveyScores()) {
                Optional.ofNullable(scoreCodec.map(scores)).ifPresent(builder::addScores);
            }
        }

        Optional.ofNullable(commonObject.getCourseObjectName()).ifPresent(courseObjName -> {
            builder.setCourseObjectName(StringValue.of(courseObjName));
        });

        return builder.build();
    }

}
