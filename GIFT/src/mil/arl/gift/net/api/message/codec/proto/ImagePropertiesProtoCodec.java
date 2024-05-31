/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import generated.course.ImageProperties;
import generated.proto.common.ImagePropertiesProto;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf ImageProperties
 * message.
 * 
 * @author cpolynice
 *
 */
public class ImagePropertiesProtoCodec implements ProtoCodec<ImagePropertiesProto.ImageProperties, ImageProperties> {

    @Override
    public ImageProperties convert(ImagePropertiesProto.ImageProperties protoObject) {
        return new ImageProperties();
    }

    @Override
    public ImagePropertiesProto.ImageProperties map(ImageProperties commonObject) {
        return ImagePropertiesProto.ImageProperties.newBuilder().build();
    }

}
