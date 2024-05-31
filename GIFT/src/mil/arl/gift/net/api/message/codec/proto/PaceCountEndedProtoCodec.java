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
import generated.proto.common.PaceCountEndedProto;
import mil.arl.gift.common.PaceCountEnded;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a PaceCountEnded message.
 * 
 * @author cpolynice
 *
 */
public class PaceCountEndedProtoCodec implements ProtoCodec<PaceCountEndedProto.PaceCountEnded, PaceCountEnded> {

    /* Codec responsible for converting to/from a protobuf LearnerAction. */
    private static LearnerActionProtoCodec codec = new LearnerActionProtoCodec();

    @Override
    public PaceCountEnded convert(PaceCountEndedProto.PaceCountEnded protoObject) {
        if (protoObject == null) {
            return null;
        }

        LearnerAction action = null;

        if (protoObject.hasLearnerAction()) {
            action = codec.convert(protoObject.getLearnerAction());
        }

        return new PaceCountEnded(action);
    }

    @Override
    public PaceCountEndedProto.PaceCountEnded map(PaceCountEnded commonObject) {
        if (commonObject == null) {
            return null;
        }

        PaceCountEndedProto.PaceCountEnded.Builder builder = PaceCountEndedProto.PaceCountEnded.newBuilder();
        Optional.ofNullable(codec.map(commonObject.getLearnerAction())).ifPresent(builder::setLearnerAction);
        return builder.build();
    }

}
