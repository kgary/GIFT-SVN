/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.Optional;

import org.apache.commons.codec.binary.Base64;

import com.google.protobuf.Int32Value;
import com.google.protobuf.StringValue;

import generated.proto.common.ImageDataProto;
import mil.arl.gift.common.ImageData;
import mil.arl.gift.common.enums.ImageFormatEnum;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from an ImageData message.
 * 
 * @author cpolynice
 *
 */
public class ImageDataProtoCodec implements ProtoCodec<ImageDataProto.ImageData, ImageData> {

    /* Codec that will be used to convert/map the protobuf enum. */
    private static AbstractEnumObjectProtoCodec enumCodec = new AbstractEnumObjectProtoCodec();

    @Override
    public ImageData convert(ImageDataProto.ImageData protoObject) {
        if (protoObject == null) {
            return null;
        }

        byte[] data = null;
        Integer width = null;
        Integer height = null;
        ImageFormatEnum format = null;

        String dataBase64 = protoObject.hasData() ? protoObject.getData().getValue() : null;

        data = Base64.decodeBase64(dataBase64);
        width = protoObject.hasWidth() ? protoObject.getWidth().getValue() : null;
        height = protoObject.hasHeight() ? protoObject.getHeight().getValue() : null;
        format = (ImageFormatEnum) enumCodec.convert(protoObject.getFormat());

        /* Check for required inputs. */
        if (width == null) {
            throw new MessageDecodeException(this.getClass().getName(), "The image width is null");
        }

        if (height == null) {
            throw new MessageDecodeException(this.getClass().getName(), "The image height is null");
        }

        if (format == null) {
            throw new MessageDecodeException(this.getClass().getName(), "The image format is null");
        }

        return new ImageData(data, width, height, format);
    }

    @Override
    public ImageDataProto.ImageData map(ImageData commonObject) {
        if (commonObject == null) {
            return null;
        }
        
        ImageDataProto.ImageData.Builder builder = ImageDataProto.ImageData.newBuilder();
        
        Optional.ofNullable(commonObject.getData()).ifPresent(data -> {
            builder.setData(StringValue.of(Base64.encodeBase64String(data)));
        });
        builder.setWidth(Int32Value.of(commonObject.getWidth()));
        builder.setHeight(Int32Value.of(commonObject.getHeight()));
        Optional.ofNullable(enumCodec.map(commonObject.getFormat())).ifPresent(builder::setFormat);

        return builder.build();
    }
}
