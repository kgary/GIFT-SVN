/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import com.google.protobuf.DoubleValue;

import generated.proto.common.AbstractAnswerScoreProto;
import generated.proto.common.AbstractAnswerScoreProto.PointDetails;
import mil.arl.gift.common.survey.score.ReplyAnswerScore;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for protobuf encoding/decoding a Reply Answer
 * Score, one of the possible types of Answer Scores.
 * 
 * @author cpolynice
 */
public class ReplyAnswerScoreProtoCodec
        implements ProtoCodec<AbstractAnswerScoreProto.ReplyAnswerScore, ReplyAnswerScore> {

    @Override
    public ReplyAnswerScore convert(AbstractAnswerScoreProto.ReplyAnswerScore protoObject) {
        
        double totalEarnedPoints = 0, highestPossiblePoints = 0;
        if (protoObject.hasPointDetails()){
           PointDetails pointDetails = protoObject.getPointDetails();
           totalEarnedPoints = pointDetails.hasTotalEarnedPoints() ? pointDetails.getTotalEarnedPoints().getValue() : 0;
           highestPossiblePoints = pointDetails.hasHighestPossiblePoints () ? pointDetails.getHighestPossiblePoints().getValue() : 0;
        } else {
            throw new MessageDecodeException(this.getClass().getName(), "The point details are null ");
        }

        return new ReplyAnswerScore(totalEarnedPoints, highestPossiblePoints);
    }

    @Override
    public AbstractAnswerScoreProto.ReplyAnswerScore map(ReplyAnswerScore commonObject) {
        AbstractAnswerScoreProto.ReplyAnswerScore.Builder builder = AbstractAnswerScoreProto.ReplyAnswerScore
                .newBuilder();

        builder.setPointDetails(
                PointDetails.newBuilder().setHighestPossiblePoints(DoubleValue.of(commonObject.getHighestPossiblePoints()))
                        .setTotalEarnedPoints(DoubleValue.of(commonObject.getTotalEarnedPoints())));

        return builder.build();
    }

}
