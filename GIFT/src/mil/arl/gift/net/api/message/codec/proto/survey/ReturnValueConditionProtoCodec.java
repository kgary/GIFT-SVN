/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto.survey;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.DoubleValue;
import com.google.protobuf.StringValue;

import generated.proto.common.survey.ReturnValueConditionProto;
import mil.arl.gift.common.enums.AbstractEnum;
import mil.arl.gift.common.enums.OperatorEnum;
import mil.arl.gift.common.survey.score.ReturnValueCondition;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.api.message.codec.proto.AbstractEnumObjectProtoCodec;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for protobuf encoding/decoding a Return Value
 * Condition instance.
 * 
 * @author cpolynice
 * 
 */
public class ReturnValueConditionProtoCodec
        implements ProtoCodec<ReturnValueConditionProto.ReturnValueCondition, ReturnValueCondition> {
    /** Logger for the class. */
    private static Logger logger = LoggerFactory.getLogger(ReturnValueConditionProtoCodec.class);

    /** Codec to convert {@link AbstractEnum} */
    private final AbstractEnumObjectProtoCodec abstractEnumCodec = new AbstractEnumObjectProtoCodec();

    @Override
    public ReturnValueCondition convert(ReturnValueConditionProto.ReturnValueCondition protoObject) {
        if (protoObject == null) {
            return null;
        }

        double value;
        OperatorEnum sign;
        AbstractEnum returnValue;

        try {
            value = protoObject.hasValue() ? protoObject.getValue().getValue() : 0;
            sign = protoObject.hasSign() ? OperatorEnum.valueOf(protoObject.getSign().getValue()) : null;

            if (sign == null) {
                throw new MessageDecodeException(this.getClass().getName(), "The sign is null");
            }

            returnValue = protoObject.hasReturnValue() ? abstractEnumCodec.convert(protoObject.getReturnValue()) : null;
            return new ReturnValueCondition(sign, value, returnValue);
        } catch (Exception e) {
            logger.error("Caught exception while creating an attribute scorer properties from " + protoObject, e);
            throw new MessageDecodeException(this.getClass().getName(), "Exception logged while decoding", e);
        }
    }

    @Override
    public ReturnValueConditionProto.ReturnValueCondition map(ReturnValueCondition commonObject) {
        if (commonObject == null) {
            return null;
        }

        ReturnValueConditionProto.ReturnValueCondition.Builder builder = ReturnValueConditionProto.ReturnValueCondition
                .newBuilder();
        Optional.ofNullable(commonObject.getValue()).ifPresent(value -> {
            builder.setValue(DoubleValue.of(value));
        });
        
        Optional.ofNullable(commonObject.getSign().getName()).ifPresent(sign -> {
            builder.setSign(StringValue.of(sign));
        });
        
        Optional.ofNullable(abstractEnumCodec.map(commonObject.getReturnValue())).ifPresent(builder::setReturnValue);

        return builder.build();
    }

}
