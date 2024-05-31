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
import generated.proto.common.TutorMeLearnerTutorActionProto;
import mil.arl.gift.common.TutorMeLearnerTutorAction;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a TutorMeLearnerTutorAction
 * message.
 * 
 * @author cpolynice
 *
 */
public class TutorMeLearnerTutorActionProtoCodec implements ProtoCodec<TutorMeLearnerTutorActionProto.TutorMeLearnerTutorAction, TutorMeLearnerTutorAction> {

    /* Codec responsible for converting to/from a protobuf LearnerAction. */
    private static LearnerActionProtoCodec codec = new LearnerActionProtoCodec();
    
    @Override
    public TutorMeLearnerTutorAction convert(TutorMeLearnerTutorActionProto.TutorMeLearnerTutorAction protoObject) {
        if (protoObject == null) {
            return null;
        }

        LearnerAction action = null;

        if (protoObject.hasLearnerAction()) {
            /* Legacy messages won't have this field. */
            action = codec.convert(protoObject.getLearnerAction());
        } else {
            String actionName = protoObject.getActionName().getValue();
            action = new LearnerAction();
            action.setDisplayName(actionName);
        }

        return new TutorMeLearnerTutorAction(action);
    }

    @Override
    public TutorMeLearnerTutorActionProto.TutorMeLearnerTutorAction map(TutorMeLearnerTutorAction commonObject) {
        if (commonObject == null) {
            return null;
        }
        
        TutorMeLearnerTutorActionProto.TutorMeLearnerTutorAction.Builder builder = TutorMeLearnerTutorActionProto.TutorMeLearnerTutorAction
                .newBuilder();
        Optional.ofNullable(codec.map(commonObject.getLearnerAction())).ifPresent(builder::setLearnerAction);
        return builder.build();
    }

}
