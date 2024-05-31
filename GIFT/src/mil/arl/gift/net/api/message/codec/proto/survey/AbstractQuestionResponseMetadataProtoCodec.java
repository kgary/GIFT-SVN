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

import com.google.protobuf.Int32Value;

import generated.proto.common.survey.AbstractQuestionResponseMetadataProto;
import generated.proto.common.survey.QuestionResponseElementMetadataProto;
import mil.arl.gift.common.survey.AbstractQuestionResponseMetadata;
import mil.arl.gift.common.survey.QuestionResponseElementMetadata;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.net.proto.ProtoCodec;

/* 
 *  This class is responsible for protobuf encoding/decoding an AbstractQuestionResponseElementMetadata 
 *  instance.
 * 
 *  @author cpolynice
 *  
 */
public class AbstractQuestionResponseMetadataProtoCodec implements
        ProtoCodec<AbstractQuestionResponseMetadataProto.AbstractQuestionResponseMetadata, AbstractQuestionResponseMetadata> {

    private static QuestionResponseElementMetadataProtoCodec codec = new QuestionResponseElementMetadataProtoCodec();

    @Override
    public AbstractQuestionResponseMetadata convert(
            AbstractQuestionResponseMetadataProto.AbstractQuestionResponseMetadata protoObject) {
        if (protoObject == null) {
            return null;
        }

        AbstractQuestionResponseMetadata responseMetadata = new AbstractQuestionResponseMetadata();

        int surveyPageResponseId = protoObject.hasSurveyPageResponseId() ? protoObject.getSurveyPageResponseId().getValue() : 0;
        int surveyQuestionId = protoObject.hasSurveyQuestionId() ? protoObject.getSurveyQuestionId().getValue() : 0;
        List<Integer> optionIdsOrder = new ArrayList<>();
        List<QuestionResponseElementMetadata> responseElementMetadata = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(protoObject.getOptionsIdsOrderList())) {
            optionIdsOrder = new ArrayList<>(protoObject.getOptionsIdsOrderList());
        }

        if (CollectionUtils.isNotEmpty(protoObject.getResponseElementsMetadataList())) {
            for (QuestionResponseElementMetadataProto.QuestionResponseElementMetadata elements : protoObject
                    .getResponseElementsMetadataList()) {
                responseElementMetadata.add(codec.convert(elements));
            }
        }

        responseMetadata.setResponses(responseElementMetadata);
        responseMetadata.setSurveyQuestionId(surveyQuestionId);
        responseMetadata.setSurveyPageResponseId(surveyPageResponseId);
        responseMetadata.setOptionOrder(optionIdsOrder);

        return responseMetadata;
    }

    @Override
    public AbstractQuestionResponseMetadataProto.AbstractQuestionResponseMetadata map(
            AbstractQuestionResponseMetadata commonObject) {
        if (commonObject == null) {
            return null;
        }

        AbstractQuestionResponseMetadataProto.AbstractQuestionResponseMetadata.Builder builder = AbstractQuestionResponseMetadataProto.AbstractQuestionResponseMetadata
                .newBuilder();
        
        builder.setSurveyPageResponseId(Int32Value.of(commonObject.getSurveyPageResponseId()));
        
        builder.setSurveyQuestionId(Int32Value.of(commonObject.getSurveyQuestionId()));
        
        Optional.ofNullable(commonObject.getOptionOrder()).ifPresent(builder::addAllOptionsIdsOrder);

        if (CollectionUtils.isNotEmpty(commonObject.getResponses())) {
            for (QuestionResponseElementMetadata elements : commonObject.getResponses()) {
                Optional.ofNullable(codec.map(elements)).ifPresent(builder::addResponseElementsMetadata);
            }
        }

        return builder.build();
    }

}
