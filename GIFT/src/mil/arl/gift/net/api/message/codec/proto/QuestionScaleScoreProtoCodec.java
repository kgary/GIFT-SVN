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

import generated.proto.common.AbstractScaleScoreProto;
import mil.arl.gift.common.survey.score.AbstractScale;
import mil.arl.gift.common.survey.score.QuestionScaleScore;
import mil.arl.gift.common.survey.score.ReplyScaleScore;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * Codec class used to convert a question scale score
 * 
 * @author sharrison
 */
public class QuestionScaleScoreProtoCodec
        implements ProtoCodec<AbstractScaleScoreProto.QuestionScaleScore, QuestionScaleScore> {
    /** The codec to convert an abstract scale */
    private static AbstractScaleProtoCodec abstractScaleCodec = new AbstractScaleProtoCodec();

    /** The codec to convert a reply scale */
    private static ReplyScaleScoreProtoCodec replyScaleCodec = new ReplyScaleScoreProtoCodec();

    @Override
    public QuestionScaleScore convert(AbstractScaleScoreProto.QuestionScaleScore protoObject) {
        if (protoObject == null) {
            return null;
        }

        List<ReplyScaleScore> replyScores = new ArrayList<>();

        if (protoObject.getReplyScoresList() != null) {
            for (AbstractScaleScoreProto.ReplyScaleScore replyScore : protoObject.getReplyScoresList()) {
                replyScores.add(replyScaleCodec.convert(replyScore));
            }
        }

        QuestionScaleScore questionScaleScore = new QuestionScaleScore();
        questionScaleScore.getReplyScores().addAll(replyScores);
        return questionScaleScore;
    }

    @Override
    public AbstractScaleScoreProto.QuestionScaleScore map(QuestionScaleScore commonObject) {
        if (commonObject == null) {
            return null;
        }

        AbstractScaleScoreProto.QuestionScaleScore.Builder builder = AbstractScaleScoreProto.QuestionScaleScore
                .newBuilder();

        if (commonObject.getScales() != null) {
            for (AbstractScale scale : commonObject.getScales()) {
                Optional.ofNullable(abstractScaleCodec.map(scale)).ifPresent(builder::addScales);
            }
        }

        if (commonObject.getReplyScores() != null) {
            for (ReplyScaleScore replyScore : commonObject.getReplyScores()) {
                Optional.ofNullable(replyScaleCodec.map(replyScore)).ifPresent(builder::addReplyScores);
            }
        }

        return builder.build();
    }

}
