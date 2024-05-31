/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import javax.vecmath.Vector3d;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.DoubleValue;

import generated.proto.common.Vector3DProto;
import generated.proto.common.Vector3DProto.Vector3D;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for protobuf encoding/decoding an Vector #D
 * instance.
 * 
 * @author cpolynice
 */
public class Vector3DProtoCodec implements ProtoCodec<Vector3DProto.Vector3D, Vector3d> {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(Vector3DProtoCodec.class);

    @Override
    public Vector3d convert(Vector3D protoObject) {
        if (protoObject == null) {
            return null;
        }

        double x, y, z;

        if (protoObject.hasX() && protoObject.hasY() && protoObject.hasZ()) {
            x = protoObject.getX().getValue();
            y = protoObject.getY().getValue();
            z = protoObject.getZ().getValue();
            return new Vector3d(x, y, z);
        } else {
            logger.error("Could not convert the given protobuf object to the common object.");
            throw new MessageDecodeException(this.getClass().getName(), "Found unhandled type of " + protoObject);
        }
    }

    @Override
    public Vector3D map(Vector3d commonObject) {
        if (commonObject == null) {
            return null;
        }

        Vector3DProto.Vector3D.Builder builder = Vector3DProto.Vector3D.newBuilder();

        builder.setX(DoubleValue.of(commonObject.getX()));
        builder.setY(DoubleValue.of(commonObject.getY()));
        builder.setZ(DoubleValue.of(commonObject.getZ()));

        return builder.build();
    }
}
