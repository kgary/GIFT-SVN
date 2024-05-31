/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import generated.course.VideoProperties;
import generated.proto.common.VideoPropertiesProto;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf VideoProperties
 * message.
 * 
 * @author cpolynice
 *
 */
public class VideoPropertiesProtoCodec implements ProtoCodec<VideoPropertiesProto.VideoProperties, VideoProperties> {

    @Override
    public VideoProperties convert(VideoPropertiesProto.VideoProperties protoObject) {
        return new VideoProperties();
    }

    @Override
    public generated.proto.common.VideoPropertiesProto.VideoProperties map(VideoProperties commonObject) {
        return VideoPropertiesProto.VideoProperties.newBuilder().build();
    }

}
