/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import javax.vecmath.Point3d;

import generated.proto.common.AbstractCoordinateProto;
import mil.arl.gift.common.coordinate.AGL;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.api.message.MessageEncodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for protobuf encoding/decoding an AGL coordinate.
 * 
 * @author cpolynice
 *
 */
public class AGLProtoCodec implements ProtoCodec<AbstractCoordinateProto.AGL, AGL> {

    /* Codec that will be used to convert to/from a Point3D instance. */
    private static Point3DProtoCodec codec = new Point3DProtoCodec();

    @Override
    public AGL convert(generated.proto.common.AbstractCoordinateProto.AGL protoObject) {
        if (protoObject == null) {
            return null;
        }

        if (protoObject.hasCoordinateVal()) {
            Point3d point = codec.convert(protoObject.getCoordinateVal());
            return new AGL(point.getX(), point.getY(), point.getZ());
        } else {
            throw new MessageDecodeException(this.getClass().getName(), "Exception logged while converting ");
        }
    }

    @Override
    public AbstractCoordinateProto.AGL map(AGL commonObject) {
        if (commonObject == null) {
            return null;
        }

        try {
            return AbstractCoordinateProto.AGL.newBuilder()
                    .setCoordinateVal(
                            codec.map(new Point3d(commonObject.getX(), commonObject.getY(), commonObject.getZ())))
                    .build();
        } catch (Exception e) {
            throw new MessageEncodeException(this.getClass().getName(), "There was a problem encoding", e);
        }
    }

}
