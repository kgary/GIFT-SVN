/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.HashMap;
import java.util.Map;

import com.google.protobuf.StringValue;

import generated.proto.common.AbstractEnumObjectProto.AbstractEnumObject;
import generated.proto.common.AttributeValuesProto;
import mil.arl.gift.common.CourseState.RequiredLearnerStateAttributes.AttributeValues;
import mil.arl.gift.common.CourseState.RequiredLearnerStateAttributes.ConceptAttributeValues;
import mil.arl.gift.common.enums.ExpertiseLevelEnum;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.api.message.MessageEncodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf AttributeValues
 * instance.
 * 
 * @author cpolynice
 *
 */
public class AttributeValuesProtoCodec implements ProtoCodec<AttributeValuesProto.AttributeValues, AttributeValues> {

    /* String value that represents a NULL encoded attribute. */
    private static final String NULL = "null";

    /* Codec that will be used to convert to/from a protobuf enum. */
    private static AbstractEnumObjectProtoCodec enumCodec = new AbstractEnumObjectProtoCodec();

    /**
     * Converts the given protobuf map to the common object map representation.
     * 
     * @param protoMap the protobuf map
     * @return the common object map
     */
    private Map<String, ExpertiseLevelEnum> convertConceptExpertiseLevelMap(Map<String, AbstractEnumObject> protoMap) {
        if (protoMap == null) {
            return null;
        }

        Map<String, ExpertiseLevelEnum> commonMap = new HashMap<>();

        for (Map.Entry<String, AbstractEnumObject> level : protoMap.entrySet()) {
            String key = level.getKey();

            /* We need to check for an attribute that has no value encoded
             * inside. Protobuf does not support null values so we need to get
             * around this by checking if the attribute name and value equals a
             * string denoting NULL. */
            if (level.getValue().getClassName().getValue().equals(NULL)
                    && level.getValue().getEnumName().getValue().equals(NULL)) {
                commonMap.put(key, null);
            } else {
                commonMap.put(key, (ExpertiseLevelEnum) enumCodec.convert(level.getValue()));
            }
        }

        return commonMap;
    }

    /**
     * Maps the given common object map to the protobuf representation.
     * 
     * @param commonMap the common object map
     * @return the protobuf map
     */
    private Map<String, AbstractEnumObject> mapConceptExpertiseLevelMap(Map<String, ExpertiseLevelEnum> commonMap) {
        if (commonMap == null) {
            return null;
        }

        Map<String, AbstractEnumObject> protoMap = new HashMap<>();

        for (Map.Entry<String, ExpertiseLevelEnum> level : commonMap.entrySet()) {
            String key = level.getKey();
            AbstractEnumObject value = enumCodec.map(level.getValue());

            if (value != null) {
                protoMap.put(key, value);
            } else {
                protoMap.put(key, AbstractEnumObject.newBuilder().setClassName(StringValue.of(NULL))
                        .setEnumName(StringValue.of(NULL)).build());
            }
        }

        return protoMap;
    }

    @Override
    public AttributeValues convert(AttributeValuesProto.AttributeValues protoObject) {
        if (protoObject == null) {
            return null;
        }

        if (protoObject.hasConceptAttributeValues()) {
            return new ConceptAttributeValues(convertConceptExpertiseLevelMap(
                            protoObject.getConceptAttributeValues().getConceptExpertiseLevelMap()));
        } else {
            throw new MessageDecodeException(this.getClass().getName(), "Exception logged while converting ");
        }
    }

    @Override
    public AttributeValuesProto.AttributeValues map(AttributeValues commonObject) {
        if (commonObject == null) {
            return null;
        }

        AttributeValuesProto.AttributeValues.Builder builder = AttributeValuesProto.AttributeValues.newBuilder();

        if (commonObject instanceof ConceptAttributeValues) {
            AttributeValuesProto.ConceptAttributeValues conceptValue = AttributeValuesProto.ConceptAttributeValues.newBuilder()
                    .putAllConceptExpertiseLevel(mapConceptExpertiseLevelMap(((ConceptAttributeValues) commonObject).getConceptExpertiseLevel())).build();
            builder.setConceptAttributeValues(conceptValue);
        } else {
            throw new MessageEncodeException(this.getClass().getName(),
                    "Found unhandled attribute value of " + commonObject);
        }

        return builder.build();
    }

}
