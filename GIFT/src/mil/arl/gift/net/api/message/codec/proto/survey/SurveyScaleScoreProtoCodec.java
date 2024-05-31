/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto.survey;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import generated.proto.common.AbstractScaleScoreProto;
import mil.arl.gift.common.survey.score.QuestionScaleScore;
import mil.arl.gift.common.survey.score.SurveyScaleScore;
import mil.arl.gift.net.api.message.codec.proto.QuestionScaleScoreProtoCodec;
import mil.arl.gift.net.proto.ProtoCodec;

public class SurveyScaleScoreProtoCodec
        implements ProtoCodec<AbstractScaleScoreProto.SurveyScaleScore, SurveyScaleScore> {

    private static QuestionScaleScoreProtoCodec codec = new QuestionScaleScoreProtoCodec();

    @Override
    public SurveyScaleScore convert(AbstractScaleScoreProto.SurveyScaleScore protoObject) {
        if (protoObject == null) {
            return null;
        }

        List<QuestionScaleScore> questionScores = new ArrayList<>();

        for (AbstractScaleScoreProto.QuestionScaleScore questionScore : protoObject.getQuestionScoresList()) {
            questionScores.add(codec.convert(questionScore));
        }

        SurveyScaleScore questionScaleScore = new SurveyScaleScore();
        questionScaleScore.getQuestionScores().addAll(questionScores);
        return questionScaleScore;
    }

    @Override
    public AbstractScaleScoreProto.SurveyScaleScore map(SurveyScaleScore commonObject) {
        if (commonObject == null) {
            return null;
        }

        AbstractScaleScoreProto.SurveyScaleScore.Builder builder = AbstractScaleScoreProto.SurveyScaleScore
                .newBuilder();

        if (commonObject.getQuestionScores() != null) {
            for (QuestionScaleScore questionScore : commonObject.getQuestionScores()) {
                Optional.ofNullable(codec.map(questionScore)).ifPresent(builder::addQuestionScores);
            }
        }

        return builder.build();
    }

}
