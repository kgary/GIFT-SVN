/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.Optional;

import com.google.protobuf.Int64Value;
import com.google.protobuf.StringValue;

import generated.proto.common.AbstractLearnerStateAttributeProto;
import mil.arl.gift.common.enums.AbstractEnum;
import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;
import mil.arl.gift.common.state.LearnerStateAttribute;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for protobuf encoding/decoding a LearnerStateAttribute class.
 * 
 * @author oamer
 */
public class LearnerStateAttributeProtoCodec
        implements ProtoCodec<AbstractLearnerStateAttributeProto.LearnerStateAttribute, LearnerStateAttribute> {
    @Override
    public LearnerStateAttribute convert(
            AbstractLearnerStateAttributeProto.LearnerStateAttribute protoObject) {
        if (protoObject == null) {
            return null;
        }
        
        long long_term_timestamp, predicted_timestamp;
        
        long_term_timestamp = protoObject.hasLongTermTimestamp() ? protoObject.getLongTermTimestamp().getValue() : null;
        predicted_timestamp = protoObject.hasPredictedTimestamp() ? protoObject.getPredictedTimestamp().getValue() : null;
        
        AbstractEnum short_term = null;
        AbstractEnum long_term = null;
        AbstractEnum predicted = null;
        LearnerStateAttributeNameEnum name = null;
        
        try {
            if (protoObject.hasName()) {
                name = LearnerStateAttributeNameEnum.valueOf(protoObject.getName().getValue());
                short_term = protoObject.hasShortTerm() ? name.getAttributeValue(protoObject.getShortTerm().getValue())
                        : null;
                long_term = protoObject.hasLongTerm() ? name.getAttributeValue(protoObject.getLongTerm().getValue())
                        : null;
                predicted = protoObject.hasPredicted() ? name.getAttributeValue(protoObject.getPredicted().getValue())
                        : null;
            }

            Long short_term_timestamp = protoObject.hasShortTermTimestamp() ? protoObject.getShortTermTimestamp().getValue() : null;
            
            if (short_term_timestamp == null) {
                return new LearnerStateAttribute(name, short_term, long_term, predicted);
                } else {
                return new LearnerStateAttribute(name, short_term, short_term_timestamp, long_term, long_term_timestamp,
                        predicted, predicted_timestamp);
                }
        } catch (Exception e) {
            throw new MessageDecodeException("MessageProto", "The Learner State Attribute Name Enum is null", e);
        }
    }

    @Override
    public AbstractLearnerStateAttributeProto.LearnerStateAttribute map(
            LearnerStateAttribute commonObject) {
        if (commonObject == null) {
            return null;
        }
        
        AbstractLearnerStateAttributeProto.LearnerStateAttribute.Builder builder = AbstractLearnerStateAttributeProto.LearnerStateAttribute
                .newBuilder();
        
        Optional.ofNullable(commonObject.getName()).ifPresent(name -> {
            builder.setName(StringValue.of(name.toString()));
        });

        Optional.ofNullable(commonObject.getShortTerm()).ifPresent(shortTerm -> {
            builder.setShortTerm(StringValue.of(shortTerm.toString()));
        });

        Optional.ofNullable(commonObject.getLongTerm()).ifPresent(longTerm -> {
            builder.setLongTerm(StringValue.of(longTerm.toString()));
        });

        Optional.ofNullable(commonObject.getPredicted()).ifPresent(predicted -> {
            builder.setPredicted(StringValue.of(predicted.toString()));
        });

        Optional.ofNullable(commonObject.getShortTermTimestamp()).ifPresent(time-> {
            builder.setShortTermTimestamp(Int64Value.of(time));
        });
        
        Optional.ofNullable(commonObject.getLongTermTimestamp()).ifPresent(time-> {
            builder.setLongTermTimestamp(Int64Value.of(time));
        });
        
        Optional.ofNullable(commonObject.getPredictedTimestamp()).ifPresent(time-> {
            builder.setPredictedTimestamp(Int64Value.of(time));
        });
        
        return builder.build();
    }
}
