/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto.survey;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.Int32Value;

import generated.proto.common.survey.QuestionResponseProto;
import generated.proto.common.survey.SurveyPageResponseProto;
import mil.arl.gift.common.survey.AbstractQuestionResponse;
import mil.arl.gift.common.survey.SurveyPage;
import mil.arl.gift.common.survey.SurveyPageResponse;
import mil.arl.gift.common.util.ProtobufConversionUtil;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/** 
 *  This class is responsible for protobuf encoding/decoding a Survey Element instance. 
 *  
 *  @author cpolynice
 *  
 */
public class SurveyPageResponseProtoCodec
        implements ProtoCodec<SurveyPageResponseProto.SurveyPageResponse, SurveyPageResponse> {

    /** Logger for the class. */
    private static Logger logger = LoggerFactory.getLogger(SurveyPageResponseProto.SurveyPageResponse.class);

    /**
     * Codecs that will be used to convert to/from protobuf representations of
     * class.
     */
    private static SurveyPageProtoCodec surveyPageCodec = new SurveyPageProtoCodec();
    private static QuestionResponseProtoCodec questionResponseCodec = new QuestionResponseProtoCodec();

    @Override
    public SurveyPageResponse convert(SurveyPageResponseProto.SurveyPageResponse protoObject) {
        if (protoObject == null) {
            return null;
        }

        int surveyPageResponseId, surveyResponseId;
        SurveyPage surveyPage;
        Date startTime, endTime;
        List<AbstractQuestionResponse> responses = new ArrayList<>();

        try {
            surveyPageResponseId = protoObject.hasSurveyPageResponseId()
                    ? protoObject.getSurveyPageResponseId().getValue()
                    : 0;
            surveyResponseId = protoObject.hasSurveyResponseId() ? protoObject.getSurveyResponseId().getValue() : 0;
            surveyPage = protoObject.hasSurveyPage() ? surveyPageCodec.convert(protoObject.getSurveyPage())
                    : null;
            startTime = protoObject.hasStartTime() ? new Date(ProtobufConversionUtil.convertTimestampToMillis(protoObject.getStartTime())) : null;
            endTime = protoObject.hasEndTime() ? new Date(ProtobufConversionUtil.convertTimestampToMillis(protoObject.getEndTime())) : null;

            for (QuestionResponseProto.QuestionResponse questionResponses : protoObject.getQuestionResponsesList()) {
                responses.add(questionResponseCodec.convert(questionResponses));
            }

            return new SurveyPageResponse(surveyPageResponseId, surveyResponseId, surveyPage, startTime, endTime,
                    responses);
        } catch (Exception e) {
            logger.error("Caught exception while creating survey page response data from " + protoObject, e);
            throw new MessageDecodeException(this.getClass().getName(), "Exception logged while decoding");
        }
    }

    @Override
    public SurveyPageResponseProto.SurveyPageResponse map(SurveyPageResponse commonObject) {
        if (commonObject == null) {
            return null;
        }

        SurveyPageResponseProto.SurveyPageResponse.Builder builder = SurveyPageResponseProto.SurveyPageResponse
                .newBuilder();
        
        builder.setSurveyPageResponseId(Int32Value.of(commonObject.getSurveyPageResponseId()));
        
        builder.setSurveyResponseId(Int32Value.of(commonObject.getSurveyResponseId()));

        if (commonObject.getSurveyPage() != null) {
            builder.setSurveyPage(surveyPageCodec.map(commonObject.getSurveyPage()));
        }

        Optional.ofNullable(ProtobufConversionUtil.convertDateToTimestamp(commonObject.getStartTime())).ifPresent(builder::setStartTime);
        Optional.ofNullable(ProtobufConversionUtil.convertDateToTimestamp(commonObject.getEndTime())).ifPresent(builder::setEndTime);

        if (commonObject.getQuestionResponses() != null) {
            for (AbstractQuestionResponse responses : commonObject.getQuestionResponses()) {
                Optional.ofNullable(questionResponseCodec.map(responses)).ifPresent(builder::addQuestionResponses);
            }
        }

        return builder.build();
    }

}
