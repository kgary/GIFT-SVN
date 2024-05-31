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
import generated.proto.common.PaceCountStartedProto;
import mil.arl.gift.common.PaceCountStarted;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a PaceCountStarted message.
 * 
 * @author cpolynice
 *
 */
public class PaceCountStartedProtoCodec
        implements ProtoCodec<PaceCountStartedProto.PaceCountStarted, PaceCountStarted> {

    /* Codec responsible for converting to/from a protobuf LearnerAction. */
    private static LearnerActionProtoCodec codec = new LearnerActionProtoCodec();

    @Override
    public PaceCountStarted convert(PaceCountStartedProto.PaceCountStarted protoObject) {
        if (protoObject == null) {
            return null;
        }

        LearnerAction action = null;

        if (protoObject.hasLearnerAction()) {
            action = codec.convert(protoObject.getLearnerAction());
        }

        return new PaceCountStarted(action);
    }

    @Override
    public PaceCountStartedProto.PaceCountStarted map(PaceCountStarted commonObject) {
        if (commonObject == null) {
            return null;
        }

        PaceCountStartedProto.PaceCountStarted.Builder builder = PaceCountStartedProto.PaceCountStarted.newBuilder();
        Optional.ofNullable(codec.map(commonObject.getLearnerAction())).ifPresent(builder::setLearnerAction);
        return builder.build();
    }

}
