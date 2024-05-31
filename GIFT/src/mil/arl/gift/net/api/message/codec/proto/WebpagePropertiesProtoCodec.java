/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import generated.course.WebpageProperties;
import generated.proto.common.WebpagePropertiesProto;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf WebpageProperties
 * message.
 * 
 * @author cpolynice
 *
 */
public class WebpagePropertiesProtoCodec
        implements ProtoCodec<WebpagePropertiesProto.WebpageProperties, WebpageProperties> {

    @Override
    public WebpageProperties convert(WebpagePropertiesProto.WebpageProperties protoObject) {
        return new WebpageProperties();
    }

    @Override
    public WebpagePropertiesProto.WebpageProperties map(WebpageProperties commonObject) {
        return WebpagePropertiesProto.WebpageProperties.newBuilder().build();
    }

}
