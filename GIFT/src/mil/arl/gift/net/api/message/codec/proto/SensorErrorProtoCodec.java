/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.Optional;

import com.google.protobuf.StringValue;

import generated.proto.common.SensorErrorProto;
import mil.arl.gift.common.enums.SensorTypeEnum;
import mil.arl.gift.common.sensor.SensorError;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf SensorError.
 * 
 * @author cpolynice
 *
 */
public class SensorErrorProtoCodec implements ProtoCodec<SensorErrorProto.SensorError, SensorError> {

    /* Codec that will be used to convert to/from a protobuf AbstractEnum. */
    private static final AbstractEnumObjectProtoCodec enumCodec = new AbstractEnumObjectProtoCodec();

    @Override
    public SensorError convert(SensorErrorProto.SensorError protoObject) {
        if (protoObject == null) {
            return null;
        }

        String sensorName = protoObject.hasSensorName() ? protoObject.getSensorName().getValue() : null;
        String message = protoObject.hasErrorMsg() ? protoObject.getErrorMsg().getValue() : null;
        SensorTypeEnum sensorType = protoObject.hasSensorType()
                ? (SensorTypeEnum) enumCodec.convert(protoObject.getSensorType())
                : null;

        return new SensorError(sensorName, sensorType, message);
    }

    @Override
    public SensorErrorProto.SensorError map(SensorError commonObject) {
        if (commonObject == null) {
            return null;
        }

        SensorErrorProto.SensorError.Builder builder = SensorErrorProto.SensorError.newBuilder();

        Optional.ofNullable(enumCodec.map(commonObject.getSensorType())).ifPresent(builder::setSensorType);
        Optional.ofNullable(commonObject.getSensorName()).ifPresent(name -> {
            builder.setSensorName(StringValue.of(name));
        });
        Optional.ofNullable(commonObject.getMessage()).ifPresent(msg -> {
            builder.setErrorMsg(StringValue.of(msg));
        });

        return builder.build();
    }
}
