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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.Int32Value;

import generated.proto.common.survey.QuestionResponseElementProto;
import generated.proto.common.survey.QuestionResponseProto;
import generated.proto.common.survey.QuestionResponseProto.QuestionResponse;
import mil.arl.gift.common.survey.AbstractQuestion;
import mil.arl.gift.common.survey.AbstractQuestionResponse;
import mil.arl.gift.common.survey.AbstractSurveyQuestion;
import mil.arl.gift.common.survey.QuestionResponseElement;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/** 
 *  This class is responsible for protobuf encoding/decoding a Question Response
 *  instance.
 * 
 *  @author cpolynice
 *  
 */
public class QuestionResponseProtoCodec implements
        ProtoCodec<QuestionResponseProto.QuestionResponse, AbstractQuestionResponse> {

    /** Logger for the class. */
    private static Logger logger = LoggerFactory.getLogger(QuestionResponseProto.QuestionResponse.class);

    /** Codecs that will be used to convert to/from protobuf representations. */
    private static SurveyElementProtoCodec surveyElementCodec = new SurveyElementProtoCodec();
    private static QuestionResponseElementProtoCodec questionResponseElementCodec = new QuestionResponseElementProtoCodec();

    @Override
    @SuppressWarnings("unchecked")
    public AbstractQuestionResponse convert(QuestionResponse protoObject) {
        if (protoObject == null) {
            return null;
        }

        int surveyPageResponseId;
        List<QuestionResponseElement> responses = new ArrayList<>();
        List<Integer> optionIdsOrder;
        AbstractSurveyQuestion<? extends AbstractQuestion> surveyQuestion;

        try {
            surveyQuestion = (AbstractSurveyQuestion<? extends AbstractQuestion>) surveyElementCodec
                    .convert(protoObject.getSurveyQuestion());

            surveyPageResponseId = protoObject.hasSurveyPageResponseId()
                    ? protoObject.getSurveyPageResponseId().getValue()
                    : 0;

            for (QuestionResponseElementProto.QuestionResponseElement questionResponses : protoObject
                    .getResponsesList()) {
                responses.add(questionResponseElementCodec.convert(questionResponses));
            }

            optionIdsOrder = CollectionUtils.isNotEmpty(protoObject.getOptionIdsOrderList())
                    ? new ArrayList<>(protoObject.getOptionIdsOrderList())
                    : null;

            // Check for required inputs
            if (surveyQuestion == null) {
                throw new MessageDecodeException(this.getClass().getName(), "The survey question is null");
            }

        } catch (Exception e) {
            logger.error("Caught exception while creating question response data from " + protoObject, e);
            throw new MessageDecodeException(this.getClass().getName(), "Exception logged while decoding");
        }

        AbstractQuestionResponse response = AbstractQuestionResponse.createResponse(surveyQuestion,
                surveyPageResponseId, responses);
        response.setOptions(optionIdsOrder);

        return response;
    }

    @Override
    public QuestionResponse map(AbstractQuestionResponse commonObject) {
        if (commonObject == null) {
            return null;
        }

        QuestionResponseProto.QuestionResponse.Builder builder = QuestionResponseProto.QuestionResponse.newBuilder();
        
        builder.setSurveyPageResponseId(Int32Value.of(commonObject.getSurveyPageResponseId()));
        
        Optional.ofNullable(surveyElementCodec.map(commonObject.getSurveyQuestion()))
                .ifPresent(builder::setSurveyQuestion);
        Optional.ofNullable(commonObject.getOptionOrder()).ifPresent(builder::addAllOptionIdsOrder);

        if (commonObject.getResponses() != null) {
            for (QuestionResponseElement responses : commonObject.getResponses()) {
                Optional.ofNullable(questionResponseElementCodec.map(responses)).ifPresent(builder::addResponses);
            }
        }

        return builder.build();
    }

}
