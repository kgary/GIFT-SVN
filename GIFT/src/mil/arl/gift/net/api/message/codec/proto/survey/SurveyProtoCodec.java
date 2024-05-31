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

import generated.proto.common.survey.SurveyItemPropertyValueProto;
import generated.proto.common.survey.SurveyPageProto;
import generated.proto.common.survey.SurveyProto;
import mil.arl.gift.common.EnumerationNotFoundException;
import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.common.survey.Constants;
import mil.arl.gift.common.survey.Survey;
import mil.arl.gift.common.survey.SurveyPage;
import mil.arl.gift.common.survey.SurveyProperties;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf Survey.
 * 
 * @author cpolynice
 *
 */
public class SurveyProtoCodec implements ProtoCodec<SurveyProto.Survey, Survey> {

    /* Codec that will be used to convert to/from a SurveyPage. */
    private static SurveyPageProtoCodec pageCodec = new SurveyPageProtoCodec();

    /* Codec that will be used to convert to/from a SurveyItemPropertyValue. */
    private static SurveyItemPropertyValueProtoCodec propValCodec = new SurveyItemPropertyValueProtoCodec();

    /**
     * Converts the given protobuf list to the common object representation.
     * 
     * @param protoList the protobuf list
     * @return the common object list representation.
     */
    private static List<SurveyPage> convertList(List<SurveyPageProto.SurveyPage> protoList) {
        if (protoList == null) {
            return null;
        }

        List<SurveyPage> commonList = new ArrayList<>();

        for (SurveyPageProto.SurveyPage page : protoList) {
            commonList.add(pageCodec.convert(page));
        }

        return commonList;
    }

    /**
     * Converts the given protobuf map to the common object representation.
     * 
     * @param protoMap the protobuf map
     * @return the common object map representation.
     */
    private static Map<SurveyPropertyKeyEnum, Serializable> convertMap(
            Map<String, SurveyItemPropertyValueProto.SurveyItemPropertyValue> protoMap) {
        if (protoMap == null) {
            return null;
        }

        Map<SurveyPropertyKeyEnum, Serializable> commonMap = new HashMap<>();

        for (Map.Entry<String, SurveyItemPropertyValueProto.SurveyItemPropertyValue> property : protoMap.entrySet()) {
            try {
                SurveyPropertyKeyEnum key = SurveyPropertyKeyEnum.valueOf(property.getKey());
                Serializable value = propValCodec.convert(property.getValue());
                
                if (value != null) {
                    commonMap.put(key, value);
                }
            } catch (EnumerationNotFoundException e) {
                if (StringUtils.isNotBlank(e.getMissingValue())) {
                    /* attempt to remove the unsupported property */
                    protoMap.remove(e.getMissingValue());
                } else {
                    throw e;
                }
            }
        }

        return commonMap;
    }

    /**
     * Maps the given common object list to the protobuf representation.
     * 
     * @param commonList the common object list
     * @return the protobuf list representation.
     */
    private static List<SurveyPageProto.SurveyPage> mapList(List<SurveyPage> commonList) {
        if (commonList == null) {
            return null;
        }

        List<SurveyPageProto.SurveyPage> protoList = new ArrayList<>();

        for (SurveyPage page : commonList) {
            protoList.add(pageCodec.map(page));
        }

        return protoList;
    }

    /**
     * Maps the give common object map to the protobuf representation.
     * 
     * @param commonMap the common object map
     * @return the protobuf map representation.
     */
    private static Map<String, SurveyItemPropertyValueProto.SurveyItemPropertyValue> mapMap(
            Survey commonMap) {
        if (commonMap == null) {
            return null;
        }

        Map<String, SurveyItemPropertyValueProto.SurveyItemPropertyValue> protoMap = new HashMap<>();

        for (SurveyPropertyKeyEnum surveyPropertyKey : commonMap.getProperties().getKeys()) {
            String key = surveyPropertyKey.getName();
            SurveyItemPropertyValueProto.SurveyItemPropertyValue value = propValCodec
                    .map(commonMap.getProperties().getPropertyValue(surveyPropertyKey));

            if (value != null) {
                protoMap.put(key, value);
            }
        }

        return protoMap;
    }

    @Override
    public Survey convert(SurveyProto.Survey protoObject) {
        if (protoObject == null) {
            return null;
        }

        int id = protoObject.hasId() ? protoObject.getId().getValue() : 0;
        String name = protoObject.hasName() ? protoObject.getName().getValue() : null;
        String folder = protoObject.hasFolder() ? protoObject.getFolder().getValue() : null;

        List<SurveyPage> surveyPages = CollectionUtils.isNotEmpty(protoObject.getSurveyPagesList())
                ? convertList(protoObject.getSurveyPagesList())
                : new ArrayList<>();
        Map<SurveyPropertyKeyEnum, Serializable> properties = CollectionUtils.isNotEmpty(protoObject.getPropertiesMap())
                ? convertMap(protoObject.getPropertiesMap())
                : new HashMap<>();
        List<String> visibleToUserNames = CollectionUtils.isNotEmpty(protoObject.getVisibleToUserNamesList())
                ? new ArrayList<>(protoObject.getVisibleToUserNamesList())
                : null;
        List<String> editableToUserNames = CollectionUtils.isNotEmpty(protoObject.getEditableToUserNamesList())
                ? new ArrayList<>(protoObject.getEditableToUserNamesList())
                : null;

        // Check for required inputs
        if (name == null) {
            throw new MessageDecodeException(this.getClass().getName(), "The survey name is null");
        } else if (surveyPages == null) {
            throw new MessageDecodeException(this.getClass().getName(), "The survey pages are null");
        } else if (properties == null) {
            throw new MessageDecodeException(this.getClass().getName(), "The properties are null");
        }

        if (visibleToUserNames == null) {
            visibleToUserNames = new ArrayList<String>();
            visibleToUserNames.add(Constants.VISIBILITY_WILDCARD);
        }

        if (editableToUserNames == null) {
            editableToUserNames = new ArrayList<String>();
            editableToUserNames.add(Constants.EDITABLE_WILDCARD);
        }

        return new Survey(id, name, surveyPages, folder, new SurveyProperties(properties), visibleToUserNames,
                editableToUserNames);
    }

    @Override
    public SurveyProto.Survey map(Survey commonObject) {
        if (commonObject == null) {
            return null;
        }
        
        SurveyProto.Survey.Builder builder = SurveyProto.Survey.newBuilder();
        
        builder.setId(Int32Value.of(commonObject.getId()));
        Optional.ofNullable(mapList(commonObject.getPages())).ifPresent(builder::addAllSurveyPages);
        Optional.ofNullable(mapMap(commonObject)).ifPresent(builder::putAllProperties);
        Optional.ofNullable(commonObject.getVisibleToUserNames()).ifPresent(builder::addAllVisibleToUserNames);
        Optional.ofNullable(commonObject.getEditableToUserNames()).ifPresent(builder::addAllEditableToUserNames);
        Optional.ofNullable(commonObject.getName()).ifPresent(name -> {
            builder.setName(StringValue.of(name));
        });
        Optional.ofNullable(commonObject.getFolder()).ifPresent(folder -> {
            builder.setFolder(StringValue.of(folder));
        });
        
        return builder.build();
    }
}
