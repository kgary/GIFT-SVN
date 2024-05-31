/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.Optional;

import com.google.protobuf.Int32Value;

import generated.proto.common.AbstractSensorAttributeValueProto.SensorIntegerValue;
import mil.arl.gift.common.enums.SensorAttributeNameEnum;
import mil.arl.gift.common.sensor.IntegerValue;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a SensorIntegerValue
 * message.
 * 
 * @author cpolynice
 *
 */
public class SensorIntegerValueProtoCodec implements ProtoCodec<SensorIntegerValue, IntegerValue> {

    /* Codec that will be used to convert/map the protobuf enum. */
    private static AbstractEnumObjectProtoCodec enumCodec = new AbstractEnumObjectProtoCodec();

    @Override
    public IntegerValue convert(SensorIntegerValue protoObject) {
        int value = protoObject.hasValue() ? protoObject.getValue().getValue() : 0;
        SensorAttributeNameEnum name = (SensorAttributeNameEnum) enumCodec.convert(protoObject.getName());

        return new IntegerValue(name, value);
    }

    @Override
    public SensorIntegerValue map(IntegerValue commonObject) {
        if (commonObject == null) {
            return null;
        }

        SensorIntegerValue.Builder builder = SensorIntegerValue.newBuilder();

        builder.setValue(Int32Value.of(commonObject.getNumber().intValue()));
        Optional.ofNullable(enumCodec.map(commonObject.getName())).ifPresent(builder::setName);

        return builder.build();
    }

}
