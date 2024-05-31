/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.Optional;

import generated.proto.common.LearnerStateProto;
import mil.arl.gift.common.state.AffectiveState;
import mil.arl.gift.common.state.CognitiveState;
import mil.arl.gift.common.state.LearnerState;
import mil.arl.gift.common.state.PerformanceState;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf LearnerState.
 * 
 * @author cpolynice
 *
 */
public class LearnerStateProtoCodec implements ProtoCodec<LearnerStateProto.LearnerState, LearnerState> {

    /* Codec that will be used to convert to/from a PerformanceState. */
    private static PerformanceStateProtoCodec performanceCodec = new PerformanceStateProtoCodec();

    /* Codec that will be used to convert to/from a CognitiveState. */
    private static CognitiveStateProtoCodec cognitiveCodec = new CognitiveStateProtoCodec();

    /* Codec that will be used to convert to/from an AffectiveState. */
    private static AffectiveStateProtoCodec affectiveCodec = new AffectiveStateProtoCodec();

    @Override
    public LearnerState convert(LearnerStateProto.LearnerState protoObject) {
        if (protoObject == null) {
            return null;
        }

        PerformanceState performance = protoObject.hasPerformanceState()
                ? performanceCodec.convert(protoObject.getPerformanceState())
                : null;
        CognitiveState cognitive = protoObject.hasCognitiveState()
                ? cognitiveCodec.convert(protoObject.getCognitiveState())
                : null;
        AffectiveState affective = protoObject.hasAffectiveState()
                ? affectiveCodec.convert(protoObject.getAffectiveState())
                : null;

        return new LearnerState(performance, cognitive, affective);
    }

    @Override
    public LearnerStateProto.LearnerState map(LearnerState commonObject) {
        if (commonObject == null) {
            return null;
        }

        LearnerStateProto.LearnerState.Builder builder = LearnerStateProto.LearnerState.newBuilder();

        Optional.ofNullable(performanceCodec.map(commonObject.getPerformance()))
                .ifPresent(builder::setPerformanceState);
        Optional.ofNullable(cognitiveCodec.map(commonObject.getCognitive())).ifPresent(builder::setCognitiveState);
        Optional.ofNullable(affectiveCodec.map(commonObject.getAffective())).ifPresent(builder::setAffectiveState);

        return builder.build();
    }

}
