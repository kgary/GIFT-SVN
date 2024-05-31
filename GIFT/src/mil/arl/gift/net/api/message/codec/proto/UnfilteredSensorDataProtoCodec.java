/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.google.protobuf.StringValue;

import generated.proto.common.AbstractSensorAttributeValueProto;
import generated.proto.common.UnfilteredSensorDataProto;
import mil.arl.gift.common.enums.SensorAttributeNameEnum;
import mil.arl.gift.common.enums.SensorTypeEnum;
import mil.arl.gift.common.sensor.AbstractSensorAttributeValue;
import mil.arl.gift.common.sensor.UnfilteredSensorData;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.common.util.ProtobufConversionUtil;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf
 * UnfilteredSensorData.
 * 
 * @author cpolynice
 *
 */
public class UnfilteredSensorDataProtoCodec
        implements ProtoCodec<UnfilteredSensorDataProto.UnfilteredSensorData, UnfilteredSensorData> {

    /* Codec that will be used to convert to/from a protobuf AbstractEnum. */
    private static final AbstractEnumObjectProtoCodec enumCodec = new AbstractEnumObjectProtoCodec();

    /* Codec that will be used to convert to/from a protobuf
     * AbstractSensorAttributeValue. */
    private static final AbstractSensorAttributeValueProtoCodec sensorCodec = new AbstractSensorAttributeValueProtoCodec();

    /**
     * Converts the given protobuf map to the common object representation.
     * 
     * @param protoMap the protobuf map
     * @return the common object map representation.
     */
    private static Map<SensorAttributeNameEnum, AbstractSensorAttributeValue> convertAttributeValues(
            Map<String, AbstractSensorAttributeValueProto.AbstractSensorAttributeValue> protoMap) {
        if (protoMap == null) {
            return null;
        }

        Map<SensorAttributeNameEnum, AbstractSensorAttributeValue> commonMap = new HashMap<>();

        for (Map.Entry<String, AbstractSensorAttributeValueProto.AbstractSensorAttributeValue> attribute : protoMap
                .entrySet()) {
            SensorAttributeNameEnum key = SensorAttributeNameEnum.valueOf(attribute.getKey());
            AbstractSensorAttributeValue value = sensorCodec.convert(attribute.getValue());

            if (key != null && value != null) {
                commonMap.put(key, value);
            }
        }

        return commonMap;
    }

    /**
     * Maps the given common object map to the protobuf map representation.
     * 
     * @param commonMap the common object map
     * @return the protobuf map representation.
     */
    private static Map<String, AbstractSensorAttributeValueProto.AbstractSensorAttributeValue> mapAttributeValues(
            Map<SensorAttributeNameEnum, AbstractSensorAttributeValue> commonMap) {
        if (commonMap == null) {
            return null;
        }

        Map<String, AbstractSensorAttributeValueProto.AbstractSensorAttributeValue> protoMap = new HashMap<>();

        for (Map.Entry<SensorAttributeNameEnum, AbstractSensorAttributeValue> attribute : commonMap
                .entrySet()) {
            String key = attribute.getKey().getName();
            AbstractSensorAttributeValueProto.AbstractSensorAttributeValue value = sensorCodec
                    .map(attribute.getValue());

            if (key != null && value != null) {
                protoMap.put(key, value);
            }
        }

        return protoMap;
    }

    @Override
    public UnfilteredSensorData convert(UnfilteredSensorDataProto.UnfilteredSensorData protoObject) {
        if (protoObject == null) {
            return null;
        }

        String sensorName = protoObject.hasSensorName() ? protoObject.getSensorName().getValue() : null;
        SensorTypeEnum sensorType = protoObject.hasSensorType()
                ? (SensorTypeEnum) enumCodec.convert(protoObject.getSensorType())
                : null;
        long elapsedTime = protoObject.hasElapsedTime()
                ? ProtobufConversionUtil.convertTimestampToMillis(protoObject.getElapsedTime())
                : 0;
        Map<SensorAttributeNameEnum, AbstractSensorAttributeValue> sensorAttributeToValue = CollectionUtils.isNotEmpty(
                protoObject.getAttributeValuesMap()) ? convertAttributeValues(protoObject.getAttributeValuesMap())
                        : null;

        /* Check for required inputs. */
        if (sensorName == null) {
            throw new MessageDecodeException(this.getClass().getName(), "The sensor name is null");
        }

        return new UnfilteredSensorData(sensorName, sensorType, elapsedTime, sensorAttributeToValue);
    }

    @Override
    public UnfilteredSensorDataProto.UnfilteredSensorData map(UnfilteredSensorData commonObject) {
        if (commonObject == null) {
            return null;
        }

        UnfilteredSensorDataProto.UnfilteredSensorData.Builder builder = UnfilteredSensorDataProto.UnfilteredSensorData
                .newBuilder();

        builder.setElapsedTime(ProtobufConversionUtil.convertMillisToTimestamp(commonObject.getElapsedTime()));
        Optional.ofNullable(mapAttributeValues(commonObject.getAttributeValues()))
                .ifPresent(builder::putAllAttributeValues);
        Optional.ofNullable(enumCodec.map(commonObject.getSensorType())).ifPresent(builder::setSensorType);
        Optional.ofNullable(commonObject.getSensorName()).ifPresent(name -> {
            builder.setSensorName(StringValue.of(name));
        });

        return builder.build();
    }

}
