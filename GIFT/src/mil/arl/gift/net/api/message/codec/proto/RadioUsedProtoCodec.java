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
import generated.proto.common.RadioUsedProto;
import mil.arl.gift.common.RadioUsed;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a RadioUsed message.
 * 
 * @author cpolynice
 *
 */
public class RadioUsedProtoCodec implements ProtoCodec<RadioUsedProto.RadioUsed, RadioUsed> {

    /* Codec responsible for converting to/from a protobuf LearnerAction. */
    private static LearnerActionProtoCodec codec = new LearnerActionProtoCodec();

    @Override
    public RadioUsed convert(RadioUsedProto.RadioUsed protoObject) {
        if (protoObject == null) {
            return null;
        }

        LearnerAction action = null;

        if (protoObject.hasLearnerAction()) {
            action = codec.convert(protoObject.getLearnerAction());
        }

        return new RadioUsed(action);
    }

    @Override
    public RadioUsedProto.RadioUsed map(RadioUsed commonObject) {
        if (commonObject == null) {
            return null;
        }

        RadioUsedProto.RadioUsed.Builder builder = RadioUsedProto.RadioUsed.newBuilder();
        Optional.ofNullable(codec.map(commonObject.getLearnerAction())).ifPresent(builder::setLearnerAction);
        return builder.build();
    }
}
