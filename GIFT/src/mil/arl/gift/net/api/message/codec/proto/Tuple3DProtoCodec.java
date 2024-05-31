/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import javax.vecmath.Point3d;
import javax.vecmath.Tuple3d;
import javax.vecmath.Vector3d;

import generated.proto.common.Tuple3DProto;
import generated.proto.common.Tuple3DProto.Tuple3D;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.api.message.MessageEncodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a Tuple3D message.
 * 
 * @author cpolynice
 *
 */
public class Tuple3DProtoCodec implements ProtoCodec<Tuple3DProto.Tuple3D, Tuple3d> {

    /* Codec that will be used to convert to/from a Vector3D instance. */
    private static Vector3DProtoCodec vecCodec = new Vector3DProtoCodec();

    /* Codec that will be used to convert to/from a Point3D instance. */
    private static Point3DProtoCodec pointCodec = new Point3DProtoCodec();

    @Override
    public Tuple3d convert(Tuple3D protoObject) {
        if (protoObject == null) {
            return null;
        }

        if (protoObject.hasVector3D()) {
            return vecCodec.convert(protoObject.getVector3D());
        } else if (protoObject.hasPoint3D()) {
            return pointCodec.convert(protoObject.getPoint3D());
        } else {
            throw new MessageDecodeException(this.getClass().getName(), "Found unhandled value type of " + protoObject);
        }
    }

    @Override
    public Tuple3D map(Tuple3d commonObject) {
        if (commonObject == null) {
            return null;
        }

        Tuple3D.Builder builder = Tuple3D.newBuilder();

        if (commonObject instanceof Vector3d) {
            builder.setVector3D(vecCodec.map((Vector3d) commonObject));
        } else if (commonObject instanceof Point3d) {
            builder.setPoint3D(pointCodec.map((Point3d) commonObject));
        } else {
            throw new MessageEncodeException(this.getClass().getName(), "Found unhandled value of " + commonObject);
        }

        return builder.build();
    }

}
