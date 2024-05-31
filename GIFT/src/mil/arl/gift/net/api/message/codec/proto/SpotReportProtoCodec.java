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
import generated.proto.common.AbstractReportProto;
import mil.arl.gift.common.SpotReport;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a SpotReport message.
 * 
 * @author cpolynice
 *
 */
public class SpotReportProtoCodec implements ProtoCodec<AbstractReportProto.SpotReport, SpotReport> {

    /* Codec responsible for converting to/from a protobuf LearnerAction. */
    private static LearnerActionProtoCodec codec = new LearnerActionProtoCodec();

    @Override
    public SpotReport convert(AbstractReportProto.SpotReport protoObject) {
        if (protoObject == null) {
            return null;
        }

        LearnerAction action = null;

        if (protoObject.hasLearnerAction()) {
            action = codec.convert(protoObject.getLearnerAction());
        }

        return new SpotReport(action);
    }

    @Override
    public AbstractReportProto.SpotReport map(SpotReport commonObject) {
        if (commonObject == null) {
            return null;
        }

        AbstractReportProto.SpotReport.Builder builder = AbstractReportProto.SpotReport.newBuilder();
        Optional.ofNullable(codec.map(commonObject.getLearnerAction())).ifPresent(builder::setLearnerAction);
        return builder.build();
    }

}
