/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto.survey;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.BoolValue;
import com.google.protobuf.Int32Value;
import com.google.protobuf.StringValue;

import generated.proto.common.survey.SurveyPageResponseProto;
import generated.proto.common.survey.SurveyResponseProto;
import mil.arl.gift.common.survey.Survey.SurveyTypeEnum;
import mil.arl.gift.common.survey.SurveyPageResponse;
import mil.arl.gift.common.survey.SurveyResponse;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.common.util.ProtobufConversionUtil;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for protobuf encoding/decoding the survey response
 * instance.
 * 
 * @author sharrison
 */
public class SurveyResponseProtoCodec implements ProtoCodec<SurveyResponseProto.SurveyResponse, SurveyResponse> {
    /** Logger for the class. */
    private static Logger logger = LoggerFactory.getLogger(SurveyResponseProtoCodec.class);

    /** Codec used to convert the survey page response class */
    private static SurveyPageResponseProtoCodec pageResponseCodec = new SurveyPageResponseProtoCodec();

    /** Codec used to convert the survey scorer class */
    private static SurveyScorerProtoCodec surveyScorerCodec = new SurveyScorerProtoCodec();

    @Override
    public SurveyResponse convert(SurveyResponseProto.SurveyResponse protoObject) {
        if (protoObject == null) {
            return null;
        }

        SurveyResponse response = new SurveyResponse();

        try {
            response.setSurveyId(protoObject.hasSurveyId() ? protoObject.getSurveyId().getValue() : 0);
            response.setSurveyResponseId(protoObject.hasSurveyResponseId() ? protoObject.getSurveyResponseId().getValue() : 0);
            response.setSurveyContextId(protoObject.hasSurveyContextId() ? protoObject.getSurveyContextId().getValue() : 0);
            response.setHasFillInTheBlankQuestionWithIdealAnswer(
                    protoObject.hasHasFillInTheBlankQuestionWithIdealAnswer() ? protoObject.getHasFillInTheBlankQuestionWithIdealAnswer().getValue() : false);
            response.setGiftKey(protoObject.hasGiftKey() ? protoObject.getGiftKey().getValue() : null);
            response.setSurveyName(protoObject.hasSurveyName() ? protoObject.getSurveyName().getValue() : null);

            if (protoObject.hasStartTime()) {
                response.setSurveyStartTime(
                        new Date(ProtobufConversionUtil.convertTimestampToMillis(protoObject.getStartTime())));
            }

            if (protoObject.hasEndTime()) {
                response.setSurveyEndTime(
                        new Date(ProtobufConversionUtil.convertTimestampToMillis(protoObject.getEndTime())));
            }

            final List<SurveyPageResponse> pageResponses = response.getSurveyPageResponses();
            for (SurveyPageResponseProto.SurveyPageResponse protoPageResponse : protoObject
                    .getSurveyPageResponsesList()) {
                pageResponses.add(pageResponseCodec.convert(protoPageResponse));
            }

            if (protoObject.hasSurveyType()) {
                response.setSurveyType(SurveyTypeEnum.valueOf(protoObject.getSurveyType().getValue()));
            }

            if (protoObject.hasSurveyScorer()) {
                response.setSurveyScorerModel(surveyScorerCodec.convert(protoObject.getSurveyScorer()));
            }

            return response;
        } catch (Exception e) {
            logger.error("Caught exception while creating a survey response from " + protoObject, e);
            throw new MessageDecodeException(this.getClass().getName(), "Exception logged while decoding");
        }
    }

    @Override
    public SurveyResponseProto.SurveyResponse map(SurveyResponse commonObject) {
        if (commonObject == null) {
            return null;
        }

        final SurveyResponseProto.SurveyResponse.Builder builder = SurveyResponseProto.SurveyResponse.newBuilder();
        builder.setSurveyId(Int32Value.of(commonObject.getSurveyId()));
        builder.setSurveyResponseId(Int32Value.of(commonObject.getSurveyResponseId()));
        builder.setSurveyContextId(Int32Value.of(commonObject.getSurveyContextId()));
        builder.setHasFillInTheBlankQuestionWithIdealAnswer(BoolValue.of(commonObject.getHasFillInTheBlankQuestionWithIdealAnswer()));

        Optional.ofNullable(commonObject.getSurveyStartTime()).ifPresent(time -> {
            builder.setStartTime(ProtobufConversionUtil.convertMillisToTimestamp(time.getTime()));
        });

        Optional.ofNullable(commonObject.getSurveyEndTime()).ifPresent(time -> {
            builder.setEndTime(ProtobufConversionUtil.convertMillisToTimestamp(time.getTime()));
        });

        Optional.ofNullable(commonObject.getGiftKey()).ifPresent(giftKey -> {
            builder.setGiftKey(StringValue.of(giftKey));
        });
        
        Optional.ofNullable(commonObject.getSurveyName()).ifPresent(surveyName -> {
            builder.setSurveyName(StringValue.of(surveyName));
        });
        
        builder.setSurveyType(StringValue.of(commonObject.getSurveyType().name()));

        if (CollectionUtils.isNotEmpty(commonObject.getSurveyPageResponses())) {
            for (SurveyPageResponse pageResponse : commonObject.getSurveyPageResponses()) {
                Optional.ofNullable(pageResponseCodec.map(pageResponse)).ifPresent(builder::addSurveyPageResponses);
            }
        }

        Optional.ofNullable(surveyScorerCodec.map(commonObject.getSurveyScorerModel()))
                .ifPresent(builder::setSurveyScorer);

        return builder.build();
    }
}
