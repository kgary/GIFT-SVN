/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import generated.proto.common.AbstractLearnerStateAttributeProto;
import mil.arl.gift.common.state.AbstractLearnerStateAttribute;
import mil.arl.gift.common.state.LearnerStateAttribute;
import mil.arl.gift.common.state.LearnerStateAttributeCollection;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.api.message.MessageEncodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for protobuf encoding/decoding an AbstractLearnerStateAttribute class.
 * 
 * @author oamer
 */
public class AbstractLearnerStateAttributeProtoCodec implements ProtoCodec<AbstractLearnerStateAttributeProto.AbstractLearnerStateAttribute, AbstractLearnerStateAttribute> {
    
    /* Codec that will be used to convert to/from a protobuf {@link
     * LearnerStateAttribute}. */
    private static LearnerStateAttributeProtoCodec attrCodec = new LearnerStateAttributeProtoCodec();

    /* Codec that will be used to convert to/from a protobuf {@link
     * LearnerStateAttributeCollection}. */
    private static LearnerStateAttributeCollectionProtoCodec collCodec = new LearnerStateAttributeCollectionProtoCodec();
    
    @Override
    public AbstractLearnerStateAttribute convert(
            AbstractLearnerStateAttributeProto.AbstractLearnerStateAttribute protoObject) {
        if (protoObject == null) {
            return null;
        }
      
        if (protoObject.hasLearnerStateAttribute()) {
            return attrCodec.convert(protoObject.getLearnerStateAttribute());
        } else if (protoObject.hasLearnerStateAttributeCollection()) {
            return collCodec.convert(protoObject.getLearnerStateAttributeCollection());
        } else {
            throw new MessageDecodeException(this.getClass().getName(), "Exception logged while converting ");
        }        
    }

    @Override
    public AbstractLearnerStateAttributeProto.AbstractLearnerStateAttribute map(
            AbstractLearnerStateAttribute commonObject) {
        if (commonObject == null) {
            return null;
        }
        
        AbstractLearnerStateAttributeProto.AbstractLearnerStateAttribute.Builder builder = AbstractLearnerStateAttributeProto.AbstractLearnerStateAttribute.newBuilder();
        
        if (commonObject instanceof LearnerStateAttributeCollection) {
            builder.setLearnerStateAttributeCollection(collCodec.map((LearnerStateAttributeCollection) commonObject));
        }else if (commonObject instanceof LearnerStateAttribute) {
            builder.setLearnerStateAttribute(attrCodec.map((LearnerStateAttribute) commonObject));
        } else {
            throw new MessageEncodeException(this.getClass().getName(), "Exception logged while converting ");
        }
        
        return builder.build();
    }

}
