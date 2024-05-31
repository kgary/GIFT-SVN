/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto.survey;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.google.protobuf.Int32Value;

import generated.proto.common.survey.SurveyElementProto;
import generated.proto.common.survey.SurveyElementProto.SurveyElement;
import generated.proto.common.survey.SurveyItemPropertyValueProto;
import mil.arl.gift.common.EnumerationNotFoundException;
import mil.arl.gift.common.enums.SurveyElementTypeEnum;
import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.common.survey.AbstractQuestion;
import mil.arl.gift.common.survey.AbstractSurveyElement;
import mil.arl.gift.common.survey.AbstractSurveyQuestion;
import mil.arl.gift.common.survey.SurveyItemProperties;
import mil.arl.gift.common.survey.TextSurveyElement;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.api.message.codec.proto.AbstractEnumObjectProtoCodec;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for protobuf encoding/decoding a Survey Element
 * instance.
 * 
 * @author cpolynice
 * 
 */
public class SurveyElementProtoCodec implements ProtoCodec<SurveyElementProto.SurveyElement, AbstractSurveyElement> {

    /** Codecs that will be used to convert to/from protobuf representations. */
    private static AbstractEnumObjectProtoCodec enumCodec = new AbstractEnumObjectProtoCodec();
    private static QuestionProtoCodec questionCodec = new QuestionProtoCodec();
    private static SurveyItemPropertyValueProtoCodec surveyItemPropertyValueCodec = new SurveyItemPropertyValueProtoCodec();

    @Override
    public AbstractSurveyElement convert(SurveyElement protoObject) {
        if (protoObject == null) {
            return null;
        }

        int id, surveyPageId;
        SurveyElementTypeEnum surveyElementType;
        AbstractQuestion question = null;
        Map<SurveyPropertyKeyEnum, Serializable> properties = new HashMap<>();

        try {
            id = protoObject.hasId() ? protoObject.getId().getValue() : 0;
            surveyPageId = protoObject.hasSurveyPageId() ? protoObject.getSurveyPageId().getValue() : 0;
            surveyElementType = protoObject.hasType() ? (SurveyElementTypeEnum) enumCodec.convert(protoObject.getType())
                    : null;

            if (protoObject.hasQuestion()) {
                question = questionCodec.convert(protoObject.getQuestion());
            }

            try {
                for (Map.Entry<String, SurveyItemPropertyValueProto.SurveyItemPropertyValue> property : protoObject
                        .getPropertiesMap().entrySet()) {
                    String surveyPropertyKey = property.getKey();
                    SurveyItemPropertyValueProto.SurveyItemPropertyValue surveyItem = property.getValue();
                    SurveyPropertyKeyEnum surveyType = null;

                    if (surveyPropertyKey != null) {
                        surveyType = SurveyPropertyKeyEnum.valueOf(surveyPropertyKey);
                    }

                    Serializable value = surveyItemPropertyValueCodec.convert(surveyItem);

                    if (value != null) {
                        properties.put(surveyType, value);
                    }
                }
            } catch (EnumerationNotFoundException e) {
                if (StringUtils.isNotBlank(e.getMissingValue())) {
                    /* attempt to remove the unsupported property */
                    protoObject.getPropertiesMap().remove(e.getMissingValue());
                } else {
                    throw e;
                }
            }
            
            if (surveyElementType == null) {
                throw new MessageDecodeException(this.getClass().getName(), "The survey element type is null");
            }

            if (surveyElementType == SurveyElementTypeEnum.QUESTION_ELEMENT) {
                if (question == null) {
                    throw new MessageDecodeException(this.getClass().getName(), "The question is null");
                }
                return AbstractSurveyQuestion.createSurveyQuestion(id, surveyPageId, question,
                        new SurveyItemProperties(properties));

            } else if (surveyElementType == SurveyElementTypeEnum.TEXT_ELEMENT) {
                return new TextSurveyElement(id, surveyPageId, new SurveyItemProperties(properties));
            } else {
                throw new MessageDecodeException(this.getClass().getName(),
                        "Unknown survey element type: " + surveyElementType);
            }

        } catch (Exception e) {
            throw new MessageDecodeException(this.getClass().getName(),
                    "Exception logged while decoding survey element data", e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public SurveyElement map(AbstractSurveyElement commonObject) {
        if (commonObject == null) {
            return null;
        }

        SurveyElementProto.SurveyElement.Builder builder = SurveyElementProto.SurveyElement.newBuilder();
        
        builder.setId(Int32Value.of(commonObject.getId()));
        builder.setSurveyPageId(Int32Value.of(commonObject.getSurveyPageId()));
        
        if (commonObject instanceof AbstractSurveyQuestion) {
            Optional.ofNullable(questionCodec
                    .map(((AbstractSurveyQuestion<? extends AbstractQuestion>) commonObject).getQuestion()))
                    .ifPresent(builder::setQuestion);
        }

        Optional.ofNullable(enumCodec.map(commonObject.getSurveyElementType())).ifPresent(builder::setType);

        if (commonObject.getProperties() != null) {
            for (SurveyPropertyKeyEnum surveyPropertyKey : commonObject.getProperties().getKeys()) {
                String surveyKey = surveyPropertyKey.getName();
                SurveyItemPropertyValueProto.SurveyItemPropertyValue value = surveyItemPropertyValueCodec
                        .map(commonObject.getProperties().getPropertyValue(surveyPropertyKey));

                if (value != null) {
                    builder.putProperties(surveyKey, value);
                }
            }
        }

        return builder.build();
    }
}
