/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.Optional;

import generated.dkf.LearnerAction;
import generated.proto.common.ApplyStrategyLearnerActionProto;
import mil.arl.gift.common.ApplyStrategyLearnerAction;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from an
 * ApplyStrategyLearnerAction message.
 * 
 * @author cpolynice
 *
 */
public class ApplyStrategyLearnerActionProtoCodec
        implements ProtoCodec<ApplyStrategyLearnerActionProto.ApplyStrategyLearnerAction, ApplyStrategyLearnerAction> {

    /* Codec responsible for converting to/from a protobuf LearnerAction. */
    private static LearnerActionProtoCodec codec = new LearnerActionProtoCodec();

    @Override
    public ApplyStrategyLearnerAction convert(ApplyStrategyLearnerActionProto.ApplyStrategyLearnerAction protoObject) {
        if (protoObject == null) {
            return null;
        }

        LearnerAction action = null;

        if (protoObject.hasLearnerAction()) {
            action = codec.convert(protoObject.getLearnerAction());
        }

        return new ApplyStrategyLearnerAction(action);
    }

    @Override
    public ApplyStrategyLearnerActionProto.ApplyStrategyLearnerAction map(ApplyStrategyLearnerAction commonObject) {
        if (commonObject == null) {
            return null;
        }

        ApplyStrategyLearnerActionProto.ApplyStrategyLearnerAction.Builder builder = ApplyStrategyLearnerActionProto.ApplyStrategyLearnerAction
                .newBuilder();
        Optional.ofNullable(codec.map(commonObject.getLearnerAction())).ifPresent(builder::setLearnerAction);
        return builder.build();
    }

}
