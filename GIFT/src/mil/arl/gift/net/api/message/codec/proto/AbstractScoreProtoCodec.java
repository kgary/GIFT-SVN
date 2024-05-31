/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.Optional;

import generated.proto.common.AbstractScoreProto;
import generated.proto.common.AbstractScoreProto.AbstractScore;
import mil.arl.gift.common.survey.score.AbstractAnswerScore;
import mil.arl.gift.common.survey.score.AbstractScaleScore;
import mil.arl.gift.common.survey.score.ScoreInterface;
import mil.arl.gift.common.survey.score.SurveyConceptAssessmentScore;
import mil.arl.gift.common.survey.score.SurveyFeedbackScorer;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.api.message.MessageEncodeException;
import mil.arl.gift.net.api.message.codec.proto.survey.SurveyConceptAssessmentScoreProtoCodec;
import mil.arl.gift.net.api.message.codec.proto.survey.SurveyFeedbackScorerProtoCodec;
import mil.arl.gift.net.proto.ProtoCodec;

public class AbstractScoreProtoCodec implements ProtoCodec<AbstractScoreProto.AbstractScore, ScoreInterface> {

    private static AbstractAnswerScoreProtoCodec answerScoreCodec = new AbstractAnswerScoreProtoCodec();

    private static AbstractScaleScoreProtoCodec scaleScoreCodec = new AbstractScaleScoreProtoCodec();

    private static SurveyConceptAssessmentScoreProtoCodec conceptAssessmentCodec = new SurveyConceptAssessmentScoreProtoCodec();

    private static SurveyFeedbackScorerProtoCodec feedbackScorerCodec = new SurveyFeedbackScorerProtoCodec();

    @Override
    public ScoreInterface convert(AbstractScore protoObject) {
        if (protoObject == null) {
            return null;
        }
        
        if (protoObject.hasAbstractAnswerScore()) {
            return answerScoreCodec.convert(protoObject.getAbstractAnswerScore());
        } else if (protoObject.hasAbstractScaleScore()) {
            return scaleScoreCodec.convert(protoObject.getAbstractScaleScore());
        } else if (protoObject.hasSurveyConceptAssessmentScore()) {
            return conceptAssessmentCodec.convert(protoObject.getSurveyConceptAssessmentScore());
        } else if (protoObject.hasSurveyFeedbackScorer()) {
            return feedbackScorerCodec.convert(protoObject.getSurveyFeedbackScorer());
        } else {
            throw new MessageDecodeException(this.getClass().getName(), "Exception logged while converting ");
        }
    }

    @Override
    public AbstractScore map(ScoreInterface commonObject) {
        if (commonObject == null) {
            return null;
        }

        AbstractScore.Builder builder = AbstractScore.newBuilder();

        if (commonObject instanceof AbstractAnswerScore) {
            Optional.ofNullable(answerScoreCodec.map((AbstractAnswerScore) commonObject))
                    .ifPresent(builder::setAbstractAnswerScore);
        } else if (commonObject instanceof AbstractScaleScore) {
            Optional.ofNullable(scaleScoreCodec.map((AbstractScaleScore) commonObject))
                    .ifPresent(builder::setAbstractScaleScore);
        } else if (commonObject instanceof SurveyConceptAssessmentScore) {
            Optional.ofNullable(conceptAssessmentCodec.map((SurveyConceptAssessmentScore) commonObject))
                    .ifPresent(builder::setSurveyConceptAssessmentScore);
        } else if (commonObject instanceof SurveyFeedbackScorer) {
            Optional.ofNullable(feedbackScorerCodec.map((SurveyFeedbackScorer) commonObject))
                    .ifPresent(builder::setSurveyFeedbackScorer);
        } else {
            throw new MessageEncodeException(this.getClass().getName(),
                    "Found unhandled guidance type of " + commonObject);
        }

        return builder.build();
    }

}
