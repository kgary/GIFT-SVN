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
import generated.proto.common.FilteredSensorDataProto;
import mil.arl.gift.common.enums.SensorAttributeNameEnum;
import mil.arl.gift.common.enums.SensorTypeEnum;
import mil.arl.gift.common.sensor.AbstractSensorAttributeValue;
import mil.arl.gift.common.sensor.FilteredSensorData;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.common.util.ProtobufConversionUtil;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf
 * FilteredSensorData.
 * 
 * @author cpolynice
 *
 */
public class FilteredSensorDataProtoCodec
        implements ProtoCodec<FilteredSensorDataProto.FilteredSensorData, FilteredSensorData> {

    /* Codec that will be used to convert to/from a protobuf abstract enum. */
    private static AbstractEnumObjectProtoCodec enumCodec = new AbstractEnumObjectProtoCodec();

    /* Codec that will be used to convert to/from an
     * AbstractSensorAttributeValue. */
    private static AbstractSensorAttributeValueProtoCodec attributeCodec = new AbstractSensorAttributeValueProtoCodec();

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
            AbstractSensorAttributeValue value = attributeCodec.convert(attribute.getValue());

            if (value != null) {
                commonMap.put(key, value);
            }
        }

        return commonMap;
    }

    /**
     * Maps the given common object map to the protobuf representation.
     * 
     * @param commonMap the common object map.
     * @return the protobuf map representation.
     */
    private static Map<String, AbstractSensorAttributeValueProto.AbstractSensorAttributeValue> mapAttributeValues(
            Map<SensorAttributeNameEnum, AbstractSensorAttributeValue> commonMap) {
        if (commonMap == null) {
            return null;
        }

        Map<String, AbstractSensorAttributeValueProto.AbstractSensorAttributeValue> protoMap = new HashMap<>();

        for (Map.Entry<SensorAttributeNameEnum, AbstractSensorAttributeValue> attribute : commonMap.entrySet()) {
            String key = attribute.getKey().getName();
            AbstractSensorAttributeValueProto.AbstractSensorAttributeValue value = attributeCodec
                    .map(attribute.getValue());

            if (value != null) {
                protoMap.put(key, value);
            }
        }

        return protoMap;
    }

    @Override
    public FilteredSensorData convert(FilteredSensorDataProto.FilteredSensorData protoObject) {
        if (protoObject == null) {
            return null;
        }
        
        String filterName, sensorName;
        long elapsedTime;
        SensorTypeEnum sensorType;
        Map<SensorAttributeNameEnum, AbstractSensorAttributeValue> sensorFilterAttributeToValue;

        try {

            filterName = protoObject.hasFilterName() ? protoObject.getFilterName().getValue() : null;
            sensorName = protoObject.hasSensorName() ? protoObject.getSensorName().getValue() : null;
            sensorType = protoObject.hasSensorType() ? (SensorTypeEnum) enumCodec.convert(protoObject.getSensorType()) : null;
            elapsedTime = protoObject.hasElapsedTime()
                    ? ProtobufConversionUtil.convertTimestampToMillis(protoObject.getElapsedTime())
                    : 0;
            sensorFilterAttributeToValue = CollectionUtils.isNotEmpty(protoObject.getAttributeValuesMap())
                    ? convertAttributeValues(protoObject.getAttributeValuesMap())
                    : null;

        } catch (Exception e) {
            throw new MessageDecodeException(this.getClass().getName(), "Exception logged while decoding", e);
        }

        /* Check for required inputs. */
        if (filterName == null) {
            throw new MessageDecodeException(this.getClass().getName(), "The filter name is null");
        } else if (sensorName == null) {
            throw new MessageDecodeException(this.getClass().getName(), "The sensor name is null");
        }

        return new FilteredSensorData(filterName, sensorName, sensorType, elapsedTime, sensorFilterAttributeToValue);
    }

    @Override
    public FilteredSensorDataProto.FilteredSensorData map(FilteredSensorData commonObject) {
        if (commonObject == null) {
            return null;
        }

        FilteredSensorDataProto.FilteredSensorData.Builder builder = FilteredSensorDataProto.FilteredSensorData
                .newBuilder();

        builder.setElapsedTime(ProtobufConversionUtil.convertMillisToTimestamp(commonObject.getElapsedTime()));
        Optional.ofNullable(enumCodec.map(commonObject.getSensorType())).ifPresent(builder::setSensorType);
        Optional.ofNullable(mapAttributeValues(commonObject.getAttributeValues()))
                .ifPresent(builder::putAllAttributeValues);
        Optional.ofNullable(commonObject.getFilterName()).ifPresent(name -> {
            builder.setFilterName(StringValue.of(name));
        });
        Optional.ofNullable(commonObject.getSensorName()).ifPresent(sensorName -> {
            builder.setSensorName(StringValue.of(sensorName));
        });

        return builder.build();
    }

}
