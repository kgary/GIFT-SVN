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
import mil.arl.gift.common.NineLineReport;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a NineLineReport message.
 * 
 * @author cpolynice
 *
 */
public class NineLineReportProtoCodec implements ProtoCodec<AbstractReportProto.NineLineReport, NineLineReport> {

    /* Codec responsible for converting to/from a protobuf LearnerAction. */
    private static LearnerActionProtoCodec codec = new LearnerActionProtoCodec();

    @Override
    public NineLineReport convert(AbstractReportProto.NineLineReport protoObject) {
        if (protoObject == null) {
            return null;
        }

        LearnerAction action = null;

        if (protoObject.hasLearnerAction()) {
            action = codec.convert(protoObject.getLearnerAction());
        }

        return new NineLineReport(action);
    }

    @Override
    public AbstractReportProto.NineLineReport map(NineLineReport commonObject) {
        if (commonObject == null) {
            return null;
        }

        AbstractReportProto.NineLineReport.Builder builder = AbstractReportProto.NineLineReport.newBuilder();
        Optional.ofNullable(codec.map(commonObject.getLearnerAction())).ifPresent(builder::setLearnerAction);
        return builder.build();
    }

}
