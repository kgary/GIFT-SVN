/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.Optional;

import generated.proto.common.AbstractSensorAttributeValueProto.SensorImageValue;
import mil.arl.gift.common.ImageData;
import mil.arl.gift.common.enums.SensorAttributeNameEnum;
import mil.arl.gift.common.sensor.ImageValue;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a SensorImageValue message.
 * 
 * @author cpolynice
 *
 */
public class SensorImageValueProtoCodec implements ProtoCodec<SensorImageValue, ImageValue> {

    /* Codec that will be used to convert/map the protobuf enum. */
    private static AbstractEnumObjectProtoCodec enumCodec = new AbstractEnumObjectProtoCodec();

    /* Codec that will be used to convert/map the Tuple3d message. */
    private static ImageDataProtoCodec imageCodec = new ImageDataProtoCodec();

    @Override
    public ImageValue convert(SensorImageValue protoObject) {
        if (protoObject == null) {
            return null;
        }

        ImageData value = protoObject.hasValue() ? imageCodec.convert(protoObject.getValue()) : null;
        SensorAttributeNameEnum name = (SensorAttributeNameEnum) enumCodec.convert(protoObject.getName());

        return new ImageValue(name, value);
    }

    @Override
    public SensorImageValue map(ImageValue commonObject) {
        if (commonObject == null) {
            return null;
        }

        SensorImageValue.Builder builder = SensorImageValue.newBuilder();

        Optional.ofNullable(imageCodec.map(commonObject.getImageData())).ifPresent(builder::setValue);
        Optional.ofNullable(enumCodec.map(commonObject.getName())).ifPresent(builder::setName);

        return builder.build();
    }

}
