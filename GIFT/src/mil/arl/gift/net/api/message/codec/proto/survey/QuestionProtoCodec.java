/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto.survey;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.Int32Value;
import com.google.protobuf.StringValue;

import generated.proto.common.survey.QuestionProto;
import generated.proto.common.survey.QuestionProto.Question;
import generated.proto.common.survey.SurveyItemPropertyValueProto;
import mil.arl.gift.common.enums.QuestionTypeEnum;
import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.common.survey.AbstractQuestion;
import mil.arl.gift.common.survey.Constants;
import mil.arl.gift.common.survey.SurveyItemProperties;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.api.message.codec.proto.AbstractEnumObjectProtoCodec;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for protobuf encoding/decoding a Question instance.
 * 
 * @author cpolynice
 * 
 */
public class QuestionProtoCodec implements ProtoCodec<QuestionProto.Question, AbstractQuestion> {

    /** Logger for the class. */
    private static Logger logger = LoggerFactory.getLogger(QuestionProto.Question.class);

    /**
     * Codec that will be used to convert to/from a
     * LearnerStateAttributeNameEnum.
     */
    private static AbstractEnumObjectProtoCodec enumCodec = new AbstractEnumObjectProtoCodec();

    /**
     * Codec that will be used to convert to/from protobuf representation of
     * class.
     */
    private static SurveyItemPropertyValueProtoCodec surveyItemPropertyValueCodec = new SurveyItemPropertyValueProtoCodec();

    @Override
    public AbstractQuestion convert(Question protoObject) {
        if (protoObject == null) {
            return null;
        }

        int questionId;
        String text;
        QuestionTypeEnum questionType = null;
        List<String> categories;
        Map<SurveyPropertyKeyEnum, Serializable> properties = new HashMap<>();
        List<String> visibleToUserNames;
        List<String> editableToUserNames;

        try {
            questionId = protoObject.getId().getValue();
            text = protoObject.hasQuestionText() ? protoObject.getQuestionText().getValue() : null;
            questionType = protoObject.hasQuestionType()
                    ? (QuestionTypeEnum) enumCodec.convert(protoObject.getQuestionType())
                    : null;

            if (protoObject.getPropertiesMap() != null) {
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
            }

            categories = CollectionUtils.isNotEmpty(protoObject.getCategoriesList())
                    ? new ArrayList<>(protoObject.getCategoriesList())
                    : new ArrayList<>();
            visibleToUserNames = CollectionUtils.isNotEmpty(protoObject.getVisibleToUserNamesList())
                    ? new ArrayList<>(protoObject.getVisibleToUserNamesList())
                    : null;
            editableToUserNames = CollectionUtils.isNotEmpty(protoObject.getEditableToUserNamesList())
                    ? new ArrayList<>(protoObject.getEditableToUserNamesList())
                    : null;

            // Check for required inputs
            if (text == null) {
                throw new MessageDecodeException(this.getClass().getName(), "The question text is null");
            } else if (questionType == null) {
                throw new MessageDecodeException(this.getClass().getName(), "The question type is null");
            }

            if (visibleToUserNames == null) {
                visibleToUserNames = new ArrayList<String>();
                visibleToUserNames.add(Constants.VISIBILITY_WILDCARD);
            }

            if (editableToUserNames == null) {
                editableToUserNames = new ArrayList<String>();
                editableToUserNames.add(Constants.EDITABLE_WILDCARD);
            }

        } catch (Exception e) {

            logger.error("Caught exception while creating question data from " + protoObject, e);
            throw new MessageDecodeException(this.getClass().getName(), "Exception logged while decoding", e);
        }

        return AbstractQuestion.createQuestion(questionType, questionId, text, new SurveyItemProperties(properties),
                categories, visibleToUserNames, editableToUserNames);
    }

    @Override
    public Question map(AbstractQuestion commonObject) {
        if (commonObject == null) {
            return null;
        }

        QuestionTypeEnum questionType = QuestionTypeEnum.valueOf(commonObject);

        QuestionProto.Question.Builder builder = QuestionProto.Question.newBuilder();
        
        builder.setId(Int32Value.of(commonObject.getQuestionId()));
        
        Optional.ofNullable(commonObject.getText()).ifPresent(text -> {
            builder.setQuestionText(StringValue.of(text));
        });
        
        Optional.ofNullable(commonObject.getCategories()).ifPresent(builder::addAllCategories);
        Optional.ofNullable(commonObject.getVisibleToUserNames()).ifPresent(builder::addAllVisibleToUserNames);
        Optional.ofNullable(commonObject.getEditableToUserNames()).ifPresent(builder::addAllEditableToUserNames);
        Optional.ofNullable(enumCodec.map(questionType)).ifPresent(builder::setQuestionType);

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
