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

import generated.proto.common.survey.AttributeScorerPropertiesProto;
import generated.proto.common.survey.ReturnValueConditionProto;
import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;
import mil.arl.gift.common.survey.score.AttributeScorerProperties;
import mil.arl.gift.common.survey.score.ReturnValueCondition;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.api.message.codec.proto.AbstractEnumObjectProtoCodec;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for protobuf encoding/decoding an Attribute Scorer
 * Properties instance.
 * 
 * @author cpolynice
 * 
 */
public class AttributeScorerPropertiesProtoCodec
        implements ProtoCodec<AttributeScorerPropertiesProto.AttributeScorerProperties, AttributeScorerProperties> {

    /** Logger for the class. */
    private static Logger logger = LoggerFactory
            .getLogger(AttributeScorerPropertiesProto.AttributeScorerProperties.class);

    /**
     * Codec that will be used to convert to/from a
     * LearnerStateAttributeNameEnum.
     */
    private static AbstractEnumObjectProtoCodec enumCodec = new AbstractEnumObjectProtoCodec();

    /**
     * Codec that handles converting to/from protobuf Return Value Condition
     * instance.
     */
    private static ReturnValueConditionProtoCodec returnValueConditionCodec = new ReturnValueConditionProtoCodec();

    @Override
    public AttributeScorerProperties convert(AttributeScorerPropertiesProto.AttributeScorerProperties protoObject) {
        if (protoObject == null) {
            return null;
        }

        LearnerStateAttributeNameEnum attributeType;
        List<ReturnValueCondition> returnConditions = new ArrayList<ReturnValueCondition>();

        try {
            attributeType = protoObject.hasAttributeType()
                    ? (LearnerStateAttributeNameEnum) enumCodec.convert(protoObject.getAttributeType())
                    : null;

            if (CollectionUtils.isNotEmpty(protoObject.getReturnConditionsList())) {
                for (ReturnValueConditionProto.ReturnValueCondition returnValueConditions : protoObject
                        .getReturnConditionsList()) {
                    returnConditions.add(returnValueConditionCodec.convert(returnValueConditions));
                }
            }
        } catch (Exception e) {
            logger.error("Caught exception while creating an attribute scorer properties from " + protoObject, e);
            throw new MessageDecodeException(this.getClass().getName(), "Exception logged while decoding", e);
        }

        return new AttributeScorerProperties(attributeType, returnConditions);
    }

    @Override
    public AttributeScorerPropertiesProto.AttributeScorerProperties map(AttributeScorerProperties commonObject) {
        if (commonObject == null) {
            return null;
        }
        AttributeScorerPropertiesProto.AttributeScorerProperties.Builder builder = AttributeScorerPropertiesProto.AttributeScorerProperties
                .newBuilder();

        Optional.ofNullable(enumCodec.map(commonObject.getAttributeType())).ifPresent(builder::setAttributeType);

        if (commonObject.getReturnConditions() != null) {
            for (ReturnValueCondition returnValueConditions : commonObject.getReturnConditions()) {
                Optional.ofNullable(returnValueConditionCodec.map(returnValueConditions))
                        .ifPresent(builder::addReturnConditions);
            }
        }

        return builder.build();
    }
}
