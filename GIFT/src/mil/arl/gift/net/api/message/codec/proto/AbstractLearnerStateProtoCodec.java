/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import generated.proto.common.AbstractLearnerStateProto;
import mil.arl.gift.common.state.AbstractLearnerState;
import mil.arl.gift.common.state.AffectiveState;
import mil.arl.gift.common.state.CognitiveState;
import mil.arl.gift.net.api.message.MessageEncodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for protobuf encoding/decoding an AbstractLearnerState class.
 * 
 * @author oamer
 */
public class AbstractLearnerStateProtoCodec implements ProtoCodec<AbstractLearnerStateProto.AbstractLearnerState, AbstractLearnerState> {
    private static AffectiveStateProtoCodec affectiveStateCodec = new AffectiveStateProtoCodec();
    private static CognitiveStateProtoCodec cognitiveStateCodec = new CognitiveStateProtoCodec();

    @Override
    public AbstractLearnerState convert(AbstractLearnerStateProto.AbstractLearnerState protoObject) {
        if (protoObject == null) {
            return null;
        }
        
        if (protoObject.hasAffectiveState()) {
            return affectiveStateCodec.convert(protoObject.getAffectiveState());
        } else if (protoObject.hasCognitiveState()) {
            return cognitiveStateCodec.convert(protoObject.getCognitiveState());
        } else {
            return null;
        }
    }

    @Override
    public AbstractLearnerStateProto.AbstractLearnerState map(
            AbstractLearnerState commonObject) {
        if (commonObject == null) {
            return null;
        }
        
        AbstractLearnerStateProto.AbstractLearnerState.Builder builder = AbstractLearnerStateProto.AbstractLearnerState.newBuilder();
        
        if (commonObject instanceof AffectiveState) {
            builder.setAffectiveState(affectiveStateCodec.map((AffectiveState) commonObject));
        } else if (commonObject instanceof CognitiveState) {
            builder.setCognitiveState(cognitiveStateCodec.map((CognitiveState) commonObject));
        } else {
            throw new MessageEncodeException(this.getClass().getName(), "Found unhandled learner tutor action of " + commonObject);
        }
        
        return builder.build();
    }

}
