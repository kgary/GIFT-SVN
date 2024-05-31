/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.Optional;

import generated.proto.common.AbstractScaleScoreProto;
import mil.arl.gift.common.survey.score.AbstractScaleScore;
import mil.arl.gift.common.survey.score.QuestionScaleScore;
import mil.arl.gift.common.survey.score.ReplyScaleScore;
import mil.arl.gift.common.survey.score.SurveyScaleScore;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.api.message.MessageEncodeException;
import mil.arl.gift.net.api.message.codec.proto.survey.SurveyScaleScoreProtoCodec;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for protobuf encoding/decoding an Abstract Scale
 * Score.
 * 
 * @author cpolynice
 */
public class AbstractScaleScoreProtoCodec
        implements ProtoCodec<AbstractScaleScoreProto.AbstractScaleScore, AbstractScaleScore> {

    /** The question scale score codec */
    private static QuestionScaleScoreProtoCodec questionCodec = new QuestionScaleScoreProtoCodec();

    /** The reply scale score codec */
    private static ReplyScaleScoreProtoCodec replyCodec = new ReplyScaleScoreProtoCodec();

    /** The survey scale score codec */
    private static SurveyScaleScoreProtoCodec surveyCodec = new SurveyScaleScoreProtoCodec();

    @Override
    public AbstractScaleScore convert(AbstractScaleScoreProto.AbstractScaleScore protoObject) {
        if (protoObject == null) {
            return null;
        }

        if (protoObject.hasQuestionScaleScore()) {
            return questionCodec.convert(protoObject.getQuestionScaleScore());
        } else if (protoObject.hasReplyScaleScore()) {
            return replyCodec.convert(protoObject.getReplyScaleScore());
        } else if (protoObject.hasSurveyScaleScore()) {
            return surveyCodec.convert(protoObject.getSurveyScaleScore());
        } else {
            throw new MessageDecodeException(this.getClass().getName(), "Exception logged while converting ");
        }
    }

    @Override
    public AbstractScaleScoreProto.AbstractScaleScore map(AbstractScaleScore commonObject) {
        if (commonObject == null) {
            return null;
        }

        AbstractScaleScoreProto.AbstractScaleScore.Builder builder = AbstractScaleScoreProto.AbstractScaleScore
                .newBuilder();

        if (commonObject instanceof QuestionScaleScore) {
            Optional.ofNullable(questionCodec.map((QuestionScaleScore) commonObject))
                    .ifPresent(builder::setQuestionScaleScore);
        } else if (commonObject instanceof ReplyScaleScore) {
            Optional.ofNullable(replyCodec.map((ReplyScaleScore) commonObject)).ifPresent(builder::setReplyScaleScore);
        } else if (commonObject instanceof SurveyScaleScore) {
            Optional.ofNullable(surveyCodec.map((SurveyScaleScore) commonObject))
                    .ifPresent(builder::setSurveyScaleScore);
        } else {
            throw new MessageEncodeException(this.getClass().getName(),
                    "Found unhandled guidance type of " + commonObject);
        }

        return builder.build();
    }

}
