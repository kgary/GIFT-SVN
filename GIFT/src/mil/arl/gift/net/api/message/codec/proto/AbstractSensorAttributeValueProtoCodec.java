/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import generated.proto.common.AbstractSensorAttributeValueProto;
import mil.arl.gift.common.sensor.AbstractSensorAttributeValue;
import mil.arl.gift.common.sensor.DoubleValue;
import mil.arl.gift.common.sensor.ImageValue;
import mil.arl.gift.common.sensor.IntegerValue;
import mil.arl.gift.common.sensor.StringValue;
import mil.arl.gift.common.sensor.Tuple3dValue;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.api.message.MessageEncodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from an
 * AbstractSensorAttributeValue message.
 * 
 * @author cpolynice
 *
 */
public class AbstractSensorAttributeValueProtoCodec implements
        ProtoCodec<AbstractSensorAttributeValueProto.AbstractSensorAttributeValue, AbstractSensorAttributeValue> {

    /* Codec that will be used to convert/map a SensorDoubleValue. */
    private static SensorDoubleValueProtoCodec doubleCodec = new SensorDoubleValueProtoCodec();

    /* Codec that will be used to convert/map a SensorIntegerValue. */
    private static SensorIntegerValueProtoCodec intCodec = new SensorIntegerValueProtoCodec();

    /* Codec that will be used to convert/map a SensorStringValue. */
    private static SensorStringValueProtoCodec stringCodec = new SensorStringValueProtoCodec();

    /* Codec that will be used to convert/map a SensorTuple3dValue. */
    private static SensorTuple3dValueProtoCodec tupleCodec = new SensorTuple3dValueProtoCodec();

    /* Codec that will be used to convert/map a SensorImageValue. */
    private static SensorImageValueProtoCodec imageCodec = new SensorImageValueProtoCodec();

    @Override
    public AbstractSensorAttributeValue convert(
            AbstractSensorAttributeValueProto.AbstractSensorAttributeValue protoObject) {
        if (protoObject == null) {
            return null;
        }

        if (protoObject.hasSensorDoubleValue()) {
            return doubleCodec.convert(protoObject.getSensorDoubleValue());
        } else if (protoObject.hasSensorIntegerValue()) {
            return intCodec.convert(protoObject.getSensorIntegerValue());
        } else if (protoObject.hasSensorStringValue()) {
            return stringCodec.convert(protoObject.getSensorStringValue());
        } else if (protoObject.hasSensorTuple3D()) {
            return tupleCodec.convert(protoObject.getSensorTuple3D());
        } else if (protoObject.hasSensorImageValue()) {
            return imageCodec.convert(protoObject.getSensorImageValue());
        } else {
            throw new MessageDecodeException(this.getClass().getName(), "Exception logged while converting ");
        }
    }

    @Override
    public AbstractSensorAttributeValueProto.AbstractSensorAttributeValue map(
            AbstractSensorAttributeValue commonObject) {
        if (commonObject == null) {
            return null;
        }

        AbstractSensorAttributeValueProto.AbstractSensorAttributeValue.Builder builder = AbstractSensorAttributeValueProto.AbstractSensorAttributeValue
                .newBuilder();

        if (commonObject instanceof DoubleValue) {
            builder.setSensorDoubleValue(doubleCodec.map((DoubleValue) commonObject));
        } else if (commonObject instanceof IntegerValue) {
            builder.setSensorIntegerValue(intCodec.map((IntegerValue) commonObject));
        } else if (commonObject instanceof StringValue) {
            builder.setSensorStringValue(stringCodec.map((StringValue) commonObject));
        } else if (commonObject instanceof Tuple3dValue) {
            builder.setSensorTuple3D(tupleCodec.map((Tuple3dValue) commonObject));
        } else if (commonObject instanceof ImageValue) {
            builder.setSensorImageValue(imageCodec.map((ImageValue) commonObject));
        } else {
            throw new MessageEncodeException(this.getClass().getName(), "Found unhandled value of " + commonObject);
        }

        return builder.build();
    }
}
