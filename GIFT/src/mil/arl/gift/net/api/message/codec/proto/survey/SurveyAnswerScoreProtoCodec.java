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

import com.google.protobuf.DoubleValue;

import generated.proto.common.AbstractAnswerScoreProto;
import generated.proto.common.AbstractAnswerScoreProto.PointDetails;
import mil.arl.gift.common.survey.score.QuestionAnswerScore;
import mil.arl.gift.common.survey.score.SurveyAnswerScore;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.api.message.codec.proto.QuestionAnswerScoreProtoCodec;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for protobuf encoding/decoding a Survey Answer
 * Score.
 * 
 * @author cpolynice
 */
public class SurveyAnswerScoreProtoCodec
        implements ProtoCodec<AbstractAnswerScoreProto.SurveyAnswerScore, SurveyAnswerScore> {

    /**
     * Codec that will be used to assist in converting to/from a question answer
     * score.
     */
    private static QuestionAnswerScoreProtoCodec codec = new QuestionAnswerScoreProtoCodec();

    @Override
    public SurveyAnswerScore convert(AbstractAnswerScoreProto.SurveyAnswerScore protoObject) {
        double totalEarnedPoints = 0;
        double highestPossibleScore = 0;
        List<QuestionAnswerScore> questionScores = new ArrayList<>();

        if (protoObject.hasPointDetails()) {
            PointDetails pointDetails = protoObject.getPointDetails();
            totalEarnedPoints = pointDetails.hasTotalEarnedPoints() ? pointDetails.getTotalEarnedPoints().getValue()
                    : 0;
            highestPossibleScore = pointDetails.hasHighestPossiblePoints()
                    ? pointDetails.getHighestPossiblePoints().getValue()
                    : 0;
        } else {
            throw new MessageDecodeException(this.getClass().getName(), "The point details are null ");
        }

        for (AbstractAnswerScoreProto.QuestionAnswerScore score : protoObject.getQuestionScoresList()) {
            questionScores.add(codec.convert(score));
        }

        SurveyAnswerScore surveyAnswerScore = new SurveyAnswerScore(totalEarnedPoints, highestPossibleScore);
        surveyAnswerScore.getQuestionScores().addAll(questionScores);
        return surveyAnswerScore;
    }

    @Override
    public AbstractAnswerScoreProto.SurveyAnswerScore map(SurveyAnswerScore commonObject) {
        if (commonObject == null) {
            return null;
        }

        AbstractAnswerScoreProto.SurveyAnswerScore.Builder builder = AbstractAnswerScoreProto.SurveyAnswerScore
                .newBuilder();

        builder.setPointDetails(
                PointDetails.newBuilder().setHighestPossiblePoints(DoubleValue.of(commonObject.getHighestPossiblePoints()))
                        .setTotalEarnedPoints(DoubleValue.of(commonObject.getTotalEarnedPoints())));

        if (CollectionUtils.isNotEmpty(commonObject.getQuestionScores())) {
            for (QuestionAnswerScore answerScore : commonObject.getQuestionScores()) {
                Optional.ofNullable(codec.map(answerScore)).ifPresent(builder::addQuestionScores);
            }
        }

        return builder.build();
    }
}
