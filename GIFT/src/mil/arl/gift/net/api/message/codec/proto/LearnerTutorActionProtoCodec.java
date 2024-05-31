/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import generated.proto.common.LearnerTutorActionProto;
import mil.arl.gift.common.LearnerTutorAction;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf
 * LearnerTutorAction.
 * 
 * @author cpolynice
 *
 */
public class LearnerTutorActionProtoCodec
        implements ProtoCodec<LearnerTutorActionProto.LearnerTutorAction, LearnerTutorAction> {

    /* Codec that will be used to convert to/from a protobuf
     * AbstractLearnerTutorAction. */
    private static AbstractLearnerTutorActionProtoCodec codec = new AbstractLearnerTutorActionProtoCodec();

    @Override
    public LearnerTutorAction convert(LearnerTutorActionProto.LearnerTutorAction protoObject) {
        if (protoObject == null) {
            return null;
        }

        return protoObject.hasAction() ? new LearnerTutorAction(codec.convert(protoObject.getAction())) : null;
    }

    @Override
    public LearnerTutorActionProto.LearnerTutorAction map(LearnerTutorAction commonObject) {
        if (commonObject == null) {
            return null;
        }

        LearnerTutorActionProto.LearnerTutorAction.Builder builder = LearnerTutorActionProto.LearnerTutorAction
                .newBuilder();

        return commonObject.getAction() != null ? builder.setAction(codec.map(commonObject.getAction())).build()
                : builder.build();
    }

}
