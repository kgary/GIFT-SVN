/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.Optional;

import javax.vecmath.Point3d;

import com.google.protobuf.DoubleValue;

import generated.proto.common.GeolocationProto;
import mil.arl.gift.common.coordinate.GDC;
import mil.arl.gift.common.ta.state.Geolocation;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf Geolocation.
 * 
 * @author cpolynice
 *
 */
public class GeolocationProtoCodec implements ProtoCodec<GeolocationProto.Geolocation, Geolocation> {

    /* Codec that will be used to convert to/from a 3D Point. */
    private static Point3DProtoCodec codec = new Point3DProtoCodec();

    @Override
    public Geolocation convert(GeolocationProto.Geolocation protoObject) {
        if (protoObject == null) {
            return null;
        }

        Point3d coordPoint = protoObject.hasCoordinatesProperty() ? codec.convert(protoObject.getCoordinatesProperty())
                : null;
        /* Ignore elevation since test phones to not properly track it. */
        GDC coordinates = coordPoint != null ? new GDC(coordPoint.getX(), coordPoint.getY(), 0) : null;

        Double accuracy = protoObject.hasAccuracyProperty() ? protoObject.getAccuracyProperty().getValue() : null;
        Double altitudeAccuracy = protoObject.hasAltitudeAccuracyProperty()
                ? protoObject.getAltitudeAccuracyProperty().getValue()
                : null;
        Double heading = protoObject.hasHeadingProperty() ? protoObject.getHeadingProperty().getValue() : null;
        Double speed = protoObject.hasSpeedProperty() ? protoObject.getSpeedProperty().getValue() : null;

        return new Geolocation(coordinates, accuracy, altitudeAccuracy, heading, speed);
    }

    @Override
    public GeolocationProto.Geolocation map(Geolocation commonObject) {
        if (commonObject == null) {
            return null;
        }

        GeolocationProto.Geolocation.Builder builder = GeolocationProto.Geolocation.newBuilder();

        /* Ignore elevation since test phones to not properly track it. */
        Point3d coordPoint = new Point3d(commonObject.getCoordinates().getLatitude(),
                commonObject.getCoordinates().getLongitude(), 0);
        builder.setCoordinatesProperty(codec.map(coordPoint));
        Optional.ofNullable(commonObject.getAccuracy()).ifPresent(accuracy -> {
            builder.setAccuracyProperty(DoubleValue.of(accuracy));
        });
        Optional.ofNullable(commonObject.getAltitudeAccuracy()).ifPresent(altitude -> {
            builder.setAltitudeAccuracyProperty(DoubleValue.of(altitude));
        });
        Optional.ofNullable(commonObject.getHeading()).ifPresent(heading -> {
            builder.setHeadingProperty(DoubleValue.of(heading));
        });
        Optional.ofNullable(commonObject.getSpeed()).ifPresent(speed -> {
            builder.setSpeedProperty(DoubleValue.of(speed));
        });

        return builder.build();
    }

}
