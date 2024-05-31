/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import generated.proto.common.AbstractCoordinateProto;
import mil.arl.gift.common.coordinate.AGL;
import mil.arl.gift.common.coordinate.AbstractCoordinate;
import mil.arl.gift.common.coordinate.GCC;
import mil.arl.gift.common.coordinate.GDC;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.api.message.MessageEncodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class serves as the base representation for all protobuf abstract
 * coordinates.
 * 
 * @author cpolynice
 *
 */
public class AbstractCoordinateProtoCodec
        implements ProtoCodec<AbstractCoordinateProto.AbstractCoordinate, AbstractCoordinate> {

    /* Codec that will be used to convert to/from an AGL coordinate. */
    private static AGLProtoCodec aglCodec = new AGLProtoCodec();

    /* Codec that will be used to convert to/from a GCC coordinate. */
    private static GCCProtoCodec gccCodec = new GCCProtoCodec();

    /* Codec that will be used to convert to/from a GDC coordinate. */
    private static GDCProtoCodec gdcCodec = new GDCProtoCodec();

    @Override
    public AbstractCoordinate convert(AbstractCoordinateProto.AbstractCoordinate protoObject) {
        if (protoObject == null) {
            return null;
        }

        if (protoObject.hasAgl()) {
            return aglCodec.convert(protoObject.getAgl());
        } else if (protoObject.hasGcc()) {
            return gccCodec.convert(protoObject.getGcc());
        } else if (protoObject.hasGdc()) {
            return gdcCodec.convert(protoObject.getGdc());
        } else {
            throw new MessageDecodeException(this.getClass().getName(), "Exception logged while converting ");
        }
    }

    @Override
    public AbstractCoordinateProto.AbstractCoordinate map(AbstractCoordinate commonObject) {
        if (commonObject == null) {
            return null;
        }

        AbstractCoordinateProto.AbstractCoordinate.Builder builder = AbstractCoordinateProto.AbstractCoordinate
                .newBuilder();

        if (commonObject instanceof AGL) {
            builder.setAgl(aglCodec.map((AGL) commonObject));
        } else if (commonObject instanceof GCC) {
            builder.setGcc(gccCodec.map((GCC) commonObject));
        } else if (commonObject instanceof GDC) {
            builder.setGdc(gdcCodec.map((GDC) commonObject));
        } else {
            throw new MessageEncodeException(this.getClass().getName(),
                    "Found unhandled type of " + commonObject);
        }

        return builder.build();
    }

}
