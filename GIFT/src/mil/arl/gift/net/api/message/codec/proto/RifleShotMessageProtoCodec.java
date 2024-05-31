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

import com.google.protobuf.FloatValue;
import com.google.protobuf.Int32Value;

import generated.proto.common.RifleShotMessageProto;
import mil.arl.gift.common.ta.state.RifleShotMessage;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf RifleShotMessage.
 * 
 * @author cpolynice
 *
 */
public class RifleShotMessageProtoCodec
        implements ProtoCodec<RifleShotMessageProto.RifleShotMessage, RifleShotMessage> {

    /* Codec that will be used to convert to/from a protobuf Point3D. */
    private static final Point3DProtoCodec codec = new Point3DProtoCodec();

    @Override
    public RifleShotMessage convert(RifleShotMessageProto.RifleShotMessage protoObject) {
        if (protoObject == null) {
            return null;
        }

        int shotNumber = protoObject.hasShotNumber() ? protoObject.getShotNumber().getValue() : 0;
        float result = protoObject.hasResult() ? protoObject.getResult().getValue() : 0;
        Point3d location = protoObject.hasLocation() ? codec.convert(protoObject.getLocation()) : null;

        return new RifleShotMessage(location, result, shotNumber);
    }

    @Override
    public RifleShotMessageProto.RifleShotMessage map(RifleShotMessage commonObject) {
        if (commonObject == null) {
            return null;
        }

        RifleShotMessageProto.RifleShotMessage.Builder builder = RifleShotMessageProto.RifleShotMessage.newBuilder();

        builder.setShotNumber(Int32Value.of(commonObject.getShotNumber()));
        builder.setResult(FloatValue.of(commonObject.getResult()));
        Optional.ofNullable(codec.map(commonObject.getLocation())).ifPresent(builder::setLocation);

        return builder.build();
    }
}
