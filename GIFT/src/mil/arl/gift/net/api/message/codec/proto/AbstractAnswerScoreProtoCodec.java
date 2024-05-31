/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.Optional;

import generated.proto.common.AbstractAnswerScoreProto;
import mil.arl.gift.common.survey.score.AbstractAnswerScore;
import mil.arl.gift.common.survey.score.QuestionAnswerScore;
import mil.arl.gift.common.survey.score.ReplyAnswerScore;
import mil.arl.gift.common.survey.score.SurveyAnswerScore;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.api.message.MessageEncodeException;
import mil.arl.gift.net.api.message.codec.proto.survey.SurveyAnswerScoreProtoCodec;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for protobuf encoding/decoding an Answer Score, the
 * base class implementation of an Answer Score.
 * 
 * @author cpolynice
 */
public class AbstractAnswerScoreProtoCodec
        implements ProtoCodec<AbstractAnswerScoreProto.AbstractAnswerScore, AbstractAnswerScore> {

    /** The question answer score codec */
    private static QuestionAnswerScoreProtoCodec questionCodec = new QuestionAnswerScoreProtoCodec();

    /** The reply answer score codec */
    private static ReplyAnswerScoreProtoCodec replyCodec = new ReplyAnswerScoreProtoCodec();

    /** The survey answer score codec */
    private static SurveyAnswerScoreProtoCodec surveyCodec = new SurveyAnswerScoreProtoCodec();

    @Override
    public AbstractAnswerScore convert(AbstractAnswerScoreProto.AbstractAnswerScore protoObject) {
        if (protoObject == null) {
            return null;
        }

        if (protoObject.hasQuestionAnswerScore()) {
            return questionCodec.convert(protoObject.getQuestionAnswerScore());
        } else if (protoObject.hasReplyAnswerScore()) {
            return replyCodec.convert(protoObject.getReplyAnswerScore());
        } else if (protoObject.hasSurveyAnswerScore()) {
            return surveyCodec.convert(protoObject.getSurveyAnswerScore());
        } else {
            throw new MessageDecodeException(this.getClass().getName(), "Exception logged while converting ");
        }
    }

    @Override
    public AbstractAnswerScoreProto.AbstractAnswerScore map(AbstractAnswerScore commonObject) {
        if (commonObject == null) {
            return null;
        }

        AbstractAnswerScoreProto.AbstractAnswerScore.Builder builder = AbstractAnswerScoreProto.AbstractAnswerScore
                .newBuilder();

        if (commonObject instanceof QuestionAnswerScore) {
            Optional.ofNullable(questionCodec.map((QuestionAnswerScore) commonObject))
                    .ifPresent(builder::setQuestionAnswerScore);
        } else if (commonObject instanceof ReplyAnswerScore) {
            Optional.ofNullable(replyCodec.map((ReplyAnswerScore) commonObject))
                    .ifPresent(builder::setReplyAnswerScore);
        } else if (commonObject instanceof SurveyAnswerScore) {
            Optional.ofNullable(surveyCodec.map((SurveyAnswerScore) commonObject))
                    .ifPresent(builder::setSurveyAnswerScore);
        } else {
            throw new MessageEncodeException(this.getClass().getName(),
                    "Found unhandled type of " + commonObject);
        }

        return builder.build();
    }
}
