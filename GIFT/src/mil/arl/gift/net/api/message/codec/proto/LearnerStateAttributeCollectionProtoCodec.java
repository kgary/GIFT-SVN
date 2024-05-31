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
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.Int64Value;
import com.google.protobuf.StringValue;

import generated.proto.common.AbstractLearnerStateAttributeProto;
import mil.arl.gift.common.enums.AbstractEnum;
import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;
import mil.arl.gift.common.state.AbstractLearnerStateAttribute;
import mil.arl.gift.common.state.LearnerStateAttribute;
import mil.arl.gift.common.state.LearnerStateAttributeCollection;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for protobuf encoding/decoding an LearnerStateAttributeCollection class.
 * 
 * @author oamer
 */
public class LearnerStateAttributeCollectionProtoCodec implements
        ProtoCodec<AbstractLearnerStateAttributeProto.LearnerStateAttributeCollection, LearnerStateAttributeCollection> {

    /** The logger for the class */
    private static Logger logger = LoggerFactory.getLogger(LearnerStateAttributeCollectionProtoCodec.class);

    private static final AbstractLearnerStateAttributeProtoCodec codec = new AbstractLearnerStateAttributeProtoCodec();

    private static Map<String, LearnerStateAttribute> convertAttributeCollection(
            Map<String, AbstractLearnerStateAttributeProto.AbstractLearnerStateAttribute> protoMap) {
        if (protoMap == null) {
            return null;
        }

        Map<String, LearnerStateAttribute> commonMap = new HashMap<>();

        for (Map.Entry<String, AbstractLearnerStateAttributeProto.AbstractLearnerStateAttribute> attribute : protoMap
                .entrySet()) {
            String key = attribute.getKey();
            AbstractLearnerStateAttribute value = codec.convert(attribute.getValue());

            if (key != null && value != null) {
                commonMap.put(key, (LearnerStateAttribute) value);
            }
        }

        return commonMap;
    }

    private static Map<String, AbstractLearnerStateAttributeProto.AbstractLearnerStateAttribute> mapAttributeCollection(
            Map<String, LearnerStateAttribute> commonMap) {
        if (commonMap == null) {
            return null;
        }

        Map<String, AbstractLearnerStateAttributeProto.AbstractLearnerStateAttribute> protoMap = new HashMap<>();

        for (Map.Entry<String, LearnerStateAttribute> attribute : commonMap.entrySet()) {
            String key = attribute.getKey();
            AbstractLearnerStateAttributeProto.AbstractLearnerStateAttribute value = codec.map(attribute.getValue());

            if (key != null && value != null) {
                protoMap.put(key, value);
            }
        }

        return protoMap;
    }

    @Override
    public LearnerStateAttributeCollection convert(
            AbstractLearnerStateAttributeProto.LearnerStateAttributeCollection protoObject) {
        if (protoObject == null) {
            return null;
        }

        try {
            AbstractEnum short_term = null;
            AbstractEnum long_term = null;
            AbstractEnum predicted = null;
            LearnerStateAttributeNameEnum name = null;
            Map<String, LearnerStateAttribute> collectionMap = convertAttributeCollection(
                    protoObject.getAttributeCollectionMap());

            if (protoObject.hasName()) {
                name = LearnerStateAttributeNameEnum.valueOf(protoObject.getName().getValue());
                short_term = protoObject.hasShortTerm() ? name.getAttributeValue(protoObject.getShortTerm().getValue())
                        : null;
                long_term = protoObject.hasLongTerm() ? name.getAttributeValue(protoObject.getLongTerm().getValue())
                        : null;
                predicted = protoObject.hasPredicted() ? name.getAttributeValue(protoObject.getPredicted().getValue())
                        : null;

                LearnerStateAttributeCollection collection = new LearnerStateAttributeCollection(name, collectionMap);
                collection.setShortTerm(short_term);
                collection.setLongTerm(long_term);
                collection.setPredicted(predicted);
                return collection;
            } else {
                /* IssueID #5008: For cases where an attribute that is encoded
                 * does not have a name, return null so the value is not encoded
                 * inside the collection of concepts/attributes.
                 * 
                 * This case handles when a proto log was created prior to fixes
                 * to the learner state attribute collection proto logic.  This is a
                 * a workaround that will allow decoding of the protobuf message to continue. */
                logger.warn(
                        "The protobuf object does not have a name attribute encoded inside. Returning null so the value will not be placed in subsequent attributes. '");
                return null;
            }
        } catch (Exception e) {
            throw new MessageDecodeException(this.getClass().getName(),
                    "Exception logged while decoding learner state attribute collection from\n" + protoObject, e);
        }
    }
    

    @Override
    public AbstractLearnerStateAttributeProto.LearnerStateAttributeCollection map(
            LearnerStateAttributeCollection commonObject) {
        if (commonObject == null) {
            return null;
        }
        
        AbstractLearnerStateAttributeProto.LearnerStateAttributeCollection.Builder builder = AbstractLearnerStateAttributeProto.LearnerStateAttributeCollection
                .newBuilder();
        
        Optional.ofNullable(mapAttributeCollection(commonObject.getAttributes()))
                .ifPresent(builder::putAllAttributeCollection);

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
