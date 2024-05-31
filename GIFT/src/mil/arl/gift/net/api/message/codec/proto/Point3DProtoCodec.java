/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import javax.vecmath.Point3d;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.DoubleValue;

import generated.proto.common.Point3DProto;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for protobuf encoding/decoding a Point 3D
 * instance.
 * 
 * @author cpolynice
 */
public class Point3DProtoCodec implements ProtoCodec<Point3DProto.Point3D, Point3d> {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(Point3DProtoCodec.class);

    @Override
    public Point3d convert(Point3DProto.Point3D protoObject) {
        if (protoObject == null) {
            return null;
        }

        double x, y, z;

        if (protoObject.hasX() && protoObject.hasY() && protoObject.hasZ()) {
            x = protoObject.getX().getValue();
            y = protoObject.getY().getValue();
            z = protoObject.getZ().getValue();
            return new Point3d(x, y, z);
        } else {
            logger.error("Could not convert the given protobuf object to the common object.");
        }

        return null;
    }

    @Override
    public Point3DProto.Point3D map(Point3d commonObject) {
        if (commonObject == null) {
            return null;
        }

        Point3DProto.Point3D.Builder builder = Point3DProto.Point3D.newBuilder();

        builder.setX(DoubleValue.of(commonObject.getX()));
        builder.setY(DoubleValue.of(commonObject.getY()));
        builder.setZ(DoubleValue.of(commonObject.getZ()));

        return builder.build();
    }

}
