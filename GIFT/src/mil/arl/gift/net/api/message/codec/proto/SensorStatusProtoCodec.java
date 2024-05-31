/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.Optional;

import com.google.protobuf.BoolValue;
import com.google.protobuf.StringValue;

import generated.proto.common.SensorStatusProto;
import mil.arl.gift.common.enums.SensorTypeEnum;
import mil.arl.gift.common.sensor.SensorError;
import mil.arl.gift.common.sensor.SensorStatus;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf SensorStatus.
 * 
 * @author cpolynice
 *
 */
public class SensorStatusProtoCodec implements ProtoCodec<SensorStatusProto.SensorStatus, SensorStatus> {

    /* Codec that will be used to convert to/from a protobuf AbstractEnum. */
    private static final AbstractEnumObjectProtoCodec enumCodec = new AbstractEnumObjectProtoCodec();

    @Override
    public SensorStatus convert(SensorStatusProto.SensorStatus protoObject) {
        if (protoObject == null) {
            return null;
        }

        String sensorName = protoObject.hasSensorName() ? protoObject.getSensorName().getValue() : null;
        String message = protoObject.hasErrorMsg() ? protoObject.getErrorMsg().getValue() : null;
        SensorTypeEnum sensorType = protoObject.hasSensorType()
                ? (SensorTypeEnum) enumCodec.convert(protoObject.getSensorType())
                : null;

        if (protoObject.hasIsError() && protoObject.getIsError().getValue()) {
            return new SensorError(sensorName, sensorType, message);
        } else {
            return new SensorStatus(sensorName, sensorType, message);
        }
    }

    @Override
    public SensorStatusProto.SensorStatus map(SensorStatus commonObject) {
        if (commonObject == null) {
            return null;
        }

        SensorStatusProto.SensorStatus.Builder builder = SensorStatusProto.SensorStatus.newBuilder();

        builder.setIsError(BoolValue.of(commonObject.isErrorMessage()));
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
