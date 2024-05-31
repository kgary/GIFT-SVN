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
import mil.arl.gift.common.ExplosiveHazardSpotReport;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from an ExplosiveHazardSpotReport
 * message.
 * 
 * @author cpolynice
 *
 */
public class ExplosiveHazardSpotReportProtoCodec
        implements ProtoCodec<AbstractReportProto.ExplosiveHazardSpotReport, ExplosiveHazardSpotReport> {

    /* Codec responsible for converting to/from a protobuf LearnerAction. */
    private static LearnerActionProtoCodec codec = new LearnerActionProtoCodec();

    @Override
    public ExplosiveHazardSpotReport convert(AbstractReportProto.ExplosiveHazardSpotReport protoObject) {
        if (protoObject == null) {
            return null;
        }

        LearnerAction action = null;

        if (protoObject.hasLearnerAction()) {
            action = codec.convert(protoObject.getLearnerAction());
        }

        return new ExplosiveHazardSpotReport(action);
    }

    @Override
    public AbstractReportProto.ExplosiveHazardSpotReport map(ExplosiveHazardSpotReport commonObject) {
        if (commonObject == null) {
            return null;
        }

        AbstractReportProto.ExplosiveHazardSpotReport.Builder builder = AbstractReportProto.ExplosiveHazardSpotReport
                .newBuilder();
        Optional.ofNullable(codec.map(commonObject.getLearnerAction())).ifPresent(builder::setLearnerAction);
        return builder.build();
    }

}
