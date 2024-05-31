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
import generated.proto.common.AssessMyLocationTutorActionProto;
import mil.arl.gift.common.AssessMyLocationTutorAction;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from an
 * AssessMyLocationTutorAction message.
 * 
 * @author cpolynice
 *
 */
public class AssessMyLocationTutorActionProtoCodec implements
        ProtoCodec<AssessMyLocationTutorActionProto.AssessMyLocationTutorAction, AssessMyLocationTutorAction> {

    /* Codec responsible for converting to/from a protobuf LearnerAction. */
    private static LearnerActionProtoCodec codec = new LearnerActionProtoCodec();

    @Override
    public AssessMyLocationTutorAction convert(
            AssessMyLocationTutorActionProto.AssessMyLocationTutorAction protoObject) {
        if (protoObject == null) {
            return null;
        }

        LearnerAction action = null;

        if (protoObject.hasLearnerAction()) {
            action = codec.convert(protoObject.getLearnerAction());
        }

        return new AssessMyLocationTutorAction(action);
    }

    @Override
    public AssessMyLocationTutorActionProto.AssessMyLocationTutorAction map(AssessMyLocationTutorAction commonObject) {
        if (commonObject == null) {
            return null;
        }

        AssessMyLocationTutorActionProto.AssessMyLocationTutorAction.Builder builder = AssessMyLocationTutorActionProto.AssessMyLocationTutorAction
                .newBuilder();
        Optional.ofNullable(codec.map(commonObject.getLearnerAction())).ifPresent(builder::setLearnerAction);
        return builder.build();
    }

}
