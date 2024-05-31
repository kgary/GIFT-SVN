/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.Optional;

import javax.vecmath.Tuple3d;

import generated.proto.common.AbstractSensorAttributeValueProto.SensorTuple3D;
import mil.arl.gift.common.enums.SensorAttributeNameEnum;
import mil.arl.gift.common.sensor.Tuple3dValue;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a SensorTuple3dValue
 * message.
 * 
 * @author cpolynice
 *
 */
public class SensorTuple3dValueProtoCodec implements ProtoCodec<SensorTuple3D, Tuple3dValue> {

    /* Codec that will be used to convert/map the protobuf enum. */
    private static AbstractEnumObjectProtoCodec enumCodec = new AbstractEnumObjectProtoCodec();

    /* Codec that will be used to convert/map the Tuple3d message. */
    private static Tuple3DProtoCodec tupleCodec = new Tuple3DProtoCodec();

    @Override
    public Tuple3dValue convert(SensorTuple3D protoObject) {
        if (protoObject == null) {
            return null;
        }

        Tuple3d value = protoObject.hasValue() ? tupleCodec.convert(protoObject.getValue()) : null;
        SensorAttributeNameEnum name = (SensorAttributeNameEnum) enumCodec.convert(protoObject.getName());

        return new Tuple3dValue(name, value);
    }

    @Override
    public SensorTuple3D map(Tuple3dValue commonObject) {
        if (commonObject == null) {
            return null;
        }

        SensorTuple3D.Builder builder = SensorTuple3D.newBuilder();

        Optional.ofNullable(tupleCodec.map(commonObject.getTuple3d())).ifPresent(builder::setValue);
        Optional.ofNullable(enumCodec.map(commonObject.getName())).ifPresent(builder::setName);

        return builder.build();
    }
}
