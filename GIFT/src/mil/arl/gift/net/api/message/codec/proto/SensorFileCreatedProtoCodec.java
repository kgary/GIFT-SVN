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

import generated.proto.common.SensorFileCreatedProto;
import mil.arl.gift.common.enums.SensorTypeEnum;
import mil.arl.gift.common.sensor.SensorFileCreated;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf SensorFileCreated
 * message.
 * 
 * @author cpolynice
 *
 */
public class SensorFileCreatedProtoCodec
        implements ProtoCodec<SensorFileCreatedProto.SensorFileCreated, SensorFileCreated> {

    /* Codec that will be used to convert to/from a protobuf AbstractEnum. */
    private static final AbstractEnumObjectProtoCodec enumCodec = new AbstractEnumObjectProtoCodec();

    @Override
    public SensorFileCreated convert(SensorFileCreatedProto.SensorFileCreated protoObject) {
        if (protoObject == null) {
            return null;
        }

        String fileName = protoObject.hasFileName() ? protoObject.getFileName().getValue() : null;
        SensorTypeEnum sensorType = protoObject.hasSensorType()
                ? (SensorTypeEnum) enumCodec.convert(protoObject.getSensorType())
                : null;

        return new SensorFileCreated(fileName, sensorType);
    }

    @Override
    public SensorFileCreatedProto.SensorFileCreated map(SensorFileCreated commonObject) {
        if (commonObject == null) {
            return null;
        }

        SensorFileCreatedProto.SensorFileCreated.Builder builder = SensorFileCreatedProto.SensorFileCreated
                .newBuilder();

        Optional.ofNullable(enumCodec.map(commonObject.getSensorType())).ifPresent(builder::setSensorType);
        Optional.ofNullable(commonObject.getFileName()).ifPresent(name -> {
            builder.setFileName(StringValue.of(name));
        });

        return builder.build();
    }

}
