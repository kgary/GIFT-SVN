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

import com.google.protobuf.Int32Value;
import com.google.protobuf.StringValue;

import generated.proto.common.survey.SurveyElementProto;
import generated.proto.common.survey.SurveyItemPropertyValueProto;
import generated.proto.common.survey.SurveyPageProto;
import mil.arl.gift.common.EnumerationNotFoundException;
import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.common.survey.AbstractSurveyElement;
import mil.arl.gift.common.survey.SurveyItemProperties;
import mil.arl.gift.common.survey.SurveyPage;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/** 
 *  This class is responsible for protobuf encoding/decoding a Survey Page
 *  instance.
 * 
 *  @author cpolynice
 *  
 */
public class SurveyPageProtoCodec implements ProtoCodec<SurveyPageProto.SurveyPage, SurveyPage> {

    /**
     * Codecs that will be used to convert to/from protobuf representations of
     * class.
     */
    private static SurveyElementProtoCodec surveyElementCodec = new SurveyElementProtoCodec();
    private static SurveyItemPropertyValueProtoCodec surveyItemPropertyValueCodec = new SurveyItemPropertyValueProtoCodec();

    @Override
    public SurveyPage convert(SurveyPageProto.SurveyPage protoObject) {
        if (protoObject == null) {
            return null;
        }

        int surveyId, surveyPageId;
        String name;
        List<AbstractSurveyElement> surveyElements = new ArrayList<>();
        Map<SurveyPropertyKeyEnum, Serializable> properties = new HashMap<>();

        try {
            surveyId = protoObject.hasSurveyId() ? protoObject.getSurveyId().getValue() : 0;
            surveyPageId = protoObject.hasSurveyPageId() ? protoObject.getSurveyPageId().getValue() : 0;
            name = protoObject.hasSurveyPageName() ? protoObject.getSurveyPageName().getValue() : null;

            for (SurveyElementProto.SurveyElement elements : protoObject.getElementsList()) {
                surveyElements.add(surveyElementCodec.convert(elements));
            }

            if (protoObject.getPropertiesMap() != null) {
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
            } else {
                throw new MessageDecodeException(this.getClass().getName(), "The properties are null");
            }

            // Check for required inputs
            if (name == null) {
                throw new MessageDecodeException(this.getClass().getName(), "The survey page name is null");
            }

            return new SurveyPage(surveyPageId, name, surveyId, surveyElements, new SurveyItemProperties(properties));
        } catch (Exception e) {
            throw new MessageDecodeException(this.getClass().getName(),
                    "Exception logged while decoding survey page data", e);
        }
    }

    @Override
    public SurveyPageProto.SurveyPage map(SurveyPage commonObject) {
        if (commonObject == null) {
            return null;
        }

        SurveyPageProto.SurveyPage.Builder builder = SurveyPageProto.SurveyPage.newBuilder();
        
        builder.setSurveyPageId(Int32Value.of(commonObject.getId()));
        builder.setSurveyId(Int32Value.of(commonObject.getSurveyId()));
        
        Optional.ofNullable(commonObject.getName()).ifPresent(name -> {
            builder.setSurveyPageName(StringValue.of(name));
        });

        if (commonObject.getElements() != null) {
            for (AbstractSurveyElement elements : commonObject.getElements()) {
                Optional.ofNullable(surveyElementCodec.map(elements)).ifPresent(builder::addElements);
            }
        }

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
