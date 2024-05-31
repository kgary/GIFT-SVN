/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang.SerializationUtils;

import com.google.protobuf.ByteString;
import com.google.protobuf.StringValue;

import generated.proto.common.CourseStateProto;
import generated.proto.common.CourseStateProto.AttributeOrValue;
import mil.arl.gift.common.CourseState;
import mil.arl.gift.common.CourseState.ExpandableCourseObjectStateEnum;
import mil.arl.gift.common.CourseState.RequiredLearnerStateAttributes;
import mil.arl.gift.common.CourseState.RequiredLearnerStateAttributes.AttributeValues;
import mil.arl.gift.common.CourseState.RequiredLearnerStateAttributes.ConceptAttributeValues;
import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;
import mil.arl.gift.common.enums.MerrillQuadrantEnum;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.net.api.message.MessageEncodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf CourseState
 * object.
 * 
 * @author cpolynice
 *
 */
public class CourseStateProtoCodec implements ProtoCodec<CourseStateProto.CourseState, CourseState> {

    /* Codec that will be used to convert to/from a protobuf enum. */
    private static final AbstractEnumObjectProtoCodec enumCodec = new AbstractEnumObjectProtoCodec();

    /* Codec that will be used to convert to/from attribute values. */
    private static final AttributeValuesProtoCodec attributeCodec = new AttributeValuesProtoCodec();

    @Override
    public CourseState convert(CourseStateProto.CourseState protoObject) {
        if (protoObject == null) {
            return null;
        }

        String transitionImpl = protoObject.hasTransitionImpl() ? protoObject.getTransitionImpl().getValue() : null;

        if (transitionImpl == null) {
            /* legacy message (pre 5.0?) */
            transitionImpl = "UNKNOWN";
        }

        CourseState state = new CourseState(transitionImpl);

        if (protoObject.hasQuadrant()) {
            state.setNextQuadrant((MerrillQuadrantEnum) enumCodec.convert(protoObject.getQuadrant()));
        }

        if (CollectionUtils.isNotEmpty(protoObject.getRequiredLearnerStateAttributesList())) {
            Map<LearnerStateAttributeNameEnum, AttributeValues> learnerStateAttributesMap = new HashMap<>();

            for (AttributeOrValue attrV : protoObject.getRequiredLearnerStateAttributesList()) {
                if (attrV.hasAttributeValue() && attrV.hasAttributeName()) {
                    /* a learner state attribute with more granular information
                     * was encoded */

                    LearnerStateAttributeNameEnum attrName = (LearnerStateAttributeNameEnum) enumCodec
                            .convert(attrV.getAttributeName());
                    AttributeValues value = attributeCodec.convert(attrV.getAttributeValue());
                    learnerStateAttributesMap.put(attrName, value);
                } else if (attrV.hasAttributeName()) {
                    /* a learner state attribute with no attribute value
                     * specification */

                    LearnerStateAttributeNameEnum attrName = (LearnerStateAttributeNameEnum) enumCodec
                            .convert(attrV.getAttributeName());
                    learnerStateAttributesMap.put(attrName, null);
                }
            }

            RequiredLearnerStateAttributes reqAttrs = new RequiredLearnerStateAttributes(learnerStateAttributesMap);
            state.setRequiredLearnerStateAttributes(reqAttrs);
        }

        if (CollectionUtils.isNotEmpty(protoObject.getLearnerStateShelfLifeMap())) {
            Map<LearnerStateAttributeNameEnum, Serializable> shelfLifeMap = new HashMap<>();

            for (Map.Entry<String, ByteString> shelfLife : protoObject.getLearnerStateShelfLifeMap().entrySet()) {
                LearnerStateAttributeNameEnum key = LearnerStateAttributeNameEnum.valueOf(shelfLife.getKey());
                Object value = SerializationUtils.deserialize(shelfLife.getValue().toByteArray());

                if (value != null) {
                    shelfLifeMap.put(key, (Serializable) value);
                }
            }

            state.setLearnerStateShelfLife(shelfLifeMap);
        }

        if (protoObject.hasExpandableCourseObjectState()) {
            /* Legacy message won't have this. */
            ExpandableCourseObjectStateEnum expandableCourseObjectStateEnum = ExpandableCourseObjectStateEnum
                    .valueOf(protoObject.getExpandableCourseObjectState().getValue());
            state.setExpandableCourseObjectState(expandableCourseObjectStateEnum);
        }

        return state;
    }

    @Override
    public CourseStateProto.CourseState map(CourseState commonObject) {
        if (commonObject == null) {
            return null;
        }

        CourseStateProto.CourseState.Builder builder = CourseStateProto.CourseState.newBuilder();

        Optional.ofNullable(enumCodec.map(commonObject.getNextQuadrant())).ifPresent(builder::setQuadrant);
        Optional.ofNullable(commonObject.getNextTransitionImplementation()).ifPresent(transition -> {
            builder.setTransitionImpl(StringValue.of(transition));
        });
        Optional.ofNullable(commonObject.getExpandableCourseObjectState()).ifPresent(state -> {
            builder.setExpandableCourseObjectState(StringValue.of(state.name()));
        });

        if (commonObject.getRequiredLearnerStateAttributes() != null) {
            RequiredLearnerStateAttributes reqAttrs = commonObject.getRequiredLearnerStateAttributes();
            Map<LearnerStateAttributeNameEnum, AttributeValues> learnerStateAttributesMap = reqAttrs.getLearnerStateAttributesMap();
            
            if(learnerStateAttributesMap != null) {               
                for (LearnerStateAttributeNameEnum attr : learnerStateAttributesMap.keySet()) { 
                    AttributeValues values = learnerStateAttributesMap.get(attr);
                    if (values == null) {
                        AttributeOrValue attributeName = AttributeOrValue.newBuilder().setAttributeName(enumCodec.map(attr)).build();
                        builder.addRequiredLearnerStateAttributes(attributeName);
                    } else {
                        // a more granular set of values is specified
                        if (values instanceof ConceptAttributeValues) {
                            ConceptAttributeValues conceptValues = (ConceptAttributeValues) values;
                            AttributeOrValue attributeValue = AttributeOrValue.newBuilder()
                                    .setAttributeName(enumCodec.map(attr))
                                    .setAttributeValue(attributeCodec.map(conceptValues)).build();
                            builder.addRequiredLearnerStateAttributes(attributeValue);
                        } else {
                            throw new MessageEncodeException(this.getClass().getName(), "Found unhandled AttributeValues instance of "+values+" that needs encode logic.");
                        }
                    }
                }
            }
        }

        if (CollectionUtils.isNotEmpty(commonObject.getLearnerStateShelfLife())) {
            Map<String, ByteString> protoShelfLife = new HashMap<>();
            
            for (Map.Entry<LearnerStateAttributeNameEnum, Serializable> shelfLife : commonObject.getLearnerStateShelfLife().entrySet()) {
                String key = shelfLife.getKey().getName();
                byte[] value = SerializationUtils.serialize(shelfLife.getValue());

                if (value != null) {
                    protoShelfLife.put(key, ByteString.copyFrom(value));
                }
            }

            builder.putAllLearnerStateShelfLife(protoShelfLife);
        }

        return builder.build();
    }

}
