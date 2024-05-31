/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import generated.proto.common.AbstractReportProto;
import mil.arl.gift.common.AbstractReport;
import mil.arl.gift.common.ExplosiveHazardSpotReport;
import mil.arl.gift.common.NineLineReport;
import mil.arl.gift.common.SpotReport;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.api.message.MessageEncodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from an AbstractReport message.
 * 
 * @author cpolynice
 *
 */
public class AbstractReportProtoCodec implements ProtoCodec<AbstractReportProto.AbstractReport, AbstractReport> {

    /* Codec that will be used to convert to/from an ExplosiveHazardSpotReport
     * instance. */
    private static ExplosiveHazardSpotReportProtoCodec explosiveCodec = new ExplosiveHazardSpotReportProtoCodec();

    /* Codec that will be used to convert to/from an ExplosiveHazardSpotReport
     * instance. */
    private static NineLineReportProtoCodec nineCodec = new NineLineReportProtoCodec();

    /* Codec that will be used to convert to/from an ExplosiveHazardSpotReport
     * instance. */
    private static SpotReportProtoCodec spotCodec = new SpotReportProtoCodec();

    @Override
    public AbstractReport convert(AbstractReportProto.AbstractReport protoObject) {
        if (protoObject == null) {
            return null;
        }

        if (protoObject.hasExplosiveHazardSpotReport()) {
            return explosiveCodec.convert(protoObject.getExplosiveHazardSpotReport());
        } else if (protoObject.hasNineLineReport()) {
            return nineCodec.convert(protoObject.getNineLineReport());
        } else if (protoObject.hasSpotReport()) {
            return spotCodec.convert(protoObject.getSpotReport());
        } else {
            throw new MessageDecodeException(this.getClass().getName(), "Exception logged while converting ");
        }
    }

    @Override
    public AbstractReportProto.AbstractReport map(AbstractReport commonObject) {
        if (commonObject == null) {
            return null;
        }

        AbstractReportProto.AbstractReport.Builder builder = AbstractReportProto.AbstractReport.newBuilder();

        if (commonObject instanceof ExplosiveHazardSpotReport) {
            builder.setExplosiveHazardSpotReport(explosiveCodec.map((ExplosiveHazardSpotReport) commonObject));
        } else if (commonObject instanceof NineLineReport) {
            builder.setNineLineReport(nineCodec.map((NineLineReport) commonObject));
        } else if (commonObject instanceof SpotReport) {
            builder.setSpotReport(spotCodec.map((SpotReport) commonObject));
        } else {
            throw new MessageEncodeException(this.getClass().getName(),
                    "Found unhandled report type of " + commonObject);
        }

        return builder.build();
    }

}
