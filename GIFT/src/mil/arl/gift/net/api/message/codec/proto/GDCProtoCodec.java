/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import com.google.protobuf.DoubleValue;

import generated.proto.common.AbstractCoordinateProto;
import mil.arl.gift.common.coordinate.GDC;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for protobuf encoding/decoding a GDC coordinate.
 * 
 * @author cpolynice
 *
 */
public class GDCProtoCodec implements ProtoCodec<AbstractCoordinateProto.GDC, GDC> {

    @Override
    public GDC convert(AbstractCoordinateProto.GDC protoObject) {
        if (protoObject == null) {
            return null;
        }

        double lat, lon, elev;

        lat = protoObject.hasLatitude() ? protoObject.getLatitude().getValue() : 0;
        lon = protoObject.hasLongitude() ? protoObject.getLongitude().getValue() : 0;
        elev = protoObject.hasElevation() ? protoObject.getElevation().getValue() : 0;

        return new GDC(lat, lon, elev);
    }

    @Override
    public AbstractCoordinateProto.GDC map(GDC commonObject) {
        if (commonObject == null) {
            return null;
        }

        AbstractCoordinateProto.GDC.Builder builder = AbstractCoordinateProto.GDC.newBuilder();

        builder.setLatitude(DoubleValue.of(commonObject.getLatitude()));
        builder.setLongitude(DoubleValue.of(commonObject.getLongitude()));
        builder.setElevation(DoubleValue.of(commonObject.getElevation()));

        return builder.build();
    }

}
