/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.Optional;

import generated.proto.common.AbstractSensorAttributeValueProto;
import generated.proto.common.AbstractSensorAttributeValueProto.SensorDoubleValue;
import mil.arl.gift.common.enums.SensorAttributeNameEnum;
import mil.arl.gift.common.sensor.DoubleValue;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a SensorDoubleValue message.
 * 
 * @author cpolynice
 *
 */
public class SensorDoubleValueProtoCodec
        implements ProtoCodec<AbstractSensorAttributeValueProto.SensorDoubleValue, DoubleValue> {

    /* Codec that will be used to convert/map the protobuf enum. */
    private static AbstractEnumObjectProtoCodec enumCodec = new AbstractEnumObjectProtoCodec();

    @Override
    public DoubleValue convert(SensorDoubleValue protoObject) {
        if (protoObject == null) {
            return null;
        }

        double value = protoObject.hasValue() ? protoObject.getValue().getValue() : 0;
        SensorAttributeNameEnum name = (SensorAttributeNameEnum) enumCodec.convert(protoObject.getName());

        return new DoubleValue(name, value);
    }

    @Override
    public SensorDoubleValue map(DoubleValue commonObject) {
        if (commonObject == null) {
            return null;
        }

        SensorDoubleValue.Builder builder = SensorDoubleValue.newBuilder();

        builder.setValue(com.google.protobuf.DoubleValue.of(commonObject.getNumber().doubleValue()));
        Optional.ofNullable(enumCodec.map(commonObject.getName())).ifPresent(builder::setName);

        return builder.build();
    }

}
