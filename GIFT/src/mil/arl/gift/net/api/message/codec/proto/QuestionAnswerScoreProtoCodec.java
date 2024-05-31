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

import com.google.protobuf.DoubleValue;

import generated.proto.common.AbstractAnswerScoreProto;
import generated.proto.common.AbstractAnswerScoreProto.PointDetails;
import mil.arl.gift.common.survey.score.QuestionAnswerScore;
import mil.arl.gift.common.survey.score.ReplyAnswerScore;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for protobuf encoding/decoding a Question Answer
 * Score, one of the possible types of Answer Scores.
 * 
 * @author cpolynice
 */
public class QuestionAnswerScoreProtoCodec
        implements ProtoCodec<AbstractAnswerScoreProto.QuestionAnswerScore, QuestionAnswerScore> {

    /** The reply answer score codec */
    private static ReplyAnswerScoreProtoCodec replyCodec = new ReplyAnswerScoreProtoCodec();

    @Override
    public QuestionAnswerScore convert(AbstractAnswerScoreProto.QuestionAnswerScore protoObject) {
        if (protoObject == null) {
            return null;
        }

        double totalEarnedPoints = 0;
        double highestPossibleScore = 0;
        List<ReplyAnswerScore> replyScores = new ArrayList<>();

        if (protoObject.hasPointDetails()) {
            totalEarnedPoints = protoObject.getPointDetails().getTotalEarnedPoints().getValue();
            highestPossibleScore = protoObject.getPointDetails().getHighestPossiblePoints().getValue();
        } else {
            throw new MessageDecodeException(this.getClass().getName(), "The point details are null ");
        }

        if (protoObject.getReplyScoresList() != null) {
            for (AbstractAnswerScoreProto.ReplyAnswerScore score : protoObject.getReplyScoresList()) {
                replyScores.add(replyCodec.convert(score));
            }
        } else {
            throw new MessageDecodeException(this.getClass().getName(), "The reply scores are null");
        }

        QuestionAnswerScore questionAnswerScore = new QuestionAnswerScore(totalEarnedPoints, highestPossibleScore);
        questionAnswerScore.getReplyScores().addAll(replyScores);
        return questionAnswerScore;
    }

    @Override
    public AbstractAnswerScoreProto.QuestionAnswerScore map(QuestionAnswerScore commonObject) {
        if (commonObject == null) {
            return null;
        }

        AbstractAnswerScoreProto.QuestionAnswerScore.Builder builder = AbstractAnswerScoreProto.QuestionAnswerScore
                .newBuilder();

        builder.setPointDetails(
                PointDetails.newBuilder().setHighestPossiblePoints(DoubleValue.of(commonObject.getHighestPossiblePoints()))
                        .setTotalEarnedPoints(DoubleValue.of(commonObject.getTotalEarnedPoints())));

        if (commonObject.getReplyScores() != null) {
            for (ReplyAnswerScore replyScore : commonObject.getReplyScores()) {
                Optional.ofNullable(replyCodec.map(replyScore)).ifPresent(builder::addReplyScores);
            }
        }

        return builder.build();
    }

}
