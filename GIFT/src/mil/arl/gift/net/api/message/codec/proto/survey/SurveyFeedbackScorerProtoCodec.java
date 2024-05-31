/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto.survey;

import generated.proto.common.survey.SurveyFeedbackScorerProto;
import mil.arl.gift.common.survey.score.SurveyFeedbackScorer;
import mil.arl.gift.net.proto.ProtoCodec; 

public class SurveyFeedbackScorerProtoCodec
        implements ProtoCodec<SurveyFeedbackScorerProto.SurveyFeedbackScorer, SurveyFeedbackScorer> {

    @Override
    public SurveyFeedbackScorer convert(SurveyFeedbackScorerProto.SurveyFeedbackScorer protoObject) {
        return new SurveyFeedbackScorer();
    }

    @Override
    public SurveyFeedbackScorerProto.SurveyFeedbackScorer map(SurveyFeedbackScorer commonObject) {
        return SurveyFeedbackScorerProto.SurveyFeedbackScorer.newBuilder().build();
    }

}
