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
import java.util.Map.Entry;

import generated.proto.common.AbstractLearnerStateAttributeProto;
import generated.proto.common.AbstractLearnerStateProto;
import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;
import mil.arl.gift.common.state.AbstractLearnerStateAttribute;
import mil.arl.gift.common.state.CognitiveState;
import mil.arl.gift.common.state.LearnerStateAttribute;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for protobuf encoding/decoding an CognitiveState class.
 * 
 * @author oamer
 */
public class CognitiveStateProtoCodec implements ProtoCodec<AbstractLearnerStateProto.CognitiveState, CognitiveState> {
    private static AbstractLearnerStateAttributeProtoCodec codec = new AbstractLearnerStateAttributeProtoCodec();

    @Override
    public CognitiveState convert(AbstractLearnerStateProto.CognitiveState protoObject) {
        if (protoObject == null) {
            return null;
        }
        
        Map<LearnerStateAttributeNameEnum,LearnerStateAttribute> attributes = new HashMap<>();
        
        if (CollectionUtils.isNotEmpty(protoObject.getAttributesMap())) {
            for (Entry<String, AbstractLearnerStateAttributeProto.AbstractLearnerStateAttribute> attribute : protoObject
                    .getAttributesMap().entrySet()) {
                LearnerStateAttributeNameEnum key = LearnerStateAttributeNameEnum.valueOf(attribute.getKey());
                AbstractLearnerStateAttribute value = codec.convert(attribute.getValue());
                
                if (value != null) {
                    attributes.put(key, (LearnerStateAttribute) value);
                }
            }
        }
        
        return new CognitiveState(attributes);
    }

    @Override
    public AbstractLearnerStateProto.CognitiveState map(CognitiveState commonObject) {
        if (commonObject == null) {
            return null;
        }
        
        AbstractLearnerStateProto.CognitiveState.Builder builder = AbstractLearnerStateProto.CognitiveState.newBuilder();
        Map<String, AbstractLearnerStateAttributeProto.AbstractLearnerStateAttribute> attributes = new HashMap<>();
        
        if (CollectionUtils.isNotEmpty(commonObject.getAttributes())) {
            for (Entry<LearnerStateAttributeNameEnum, LearnerStateAttribute> attribute : commonObject.getAttributes().entrySet()) {
                String key = attribute.getKey().getName();
                AbstractLearnerStateAttributeProto.AbstractLearnerStateAttribute value = codec.map(attribute.getValue());
                
                if (value != null) {
                    attributes.put(key, value);
                }
            }
        }
        
        builder.putAllAttributes(attributes);
        return builder.build();
    }

}
