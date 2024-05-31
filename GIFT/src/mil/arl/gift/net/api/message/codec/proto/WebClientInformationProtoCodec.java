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

import generated.proto.common.WebClientInformationProto;
import mil.arl.gift.common.WebClientInformation;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf
 * WebClientInformation.
 * 
 * @author cpolynice
 *
 */
public class WebClientInformationProtoCodec
        implements ProtoCodec<WebClientInformationProto.WebClientInformation, WebClientInformation> {

    /* Codec that will be responsible for converting to/from a
     * MobileAppProperties. */
    private static MobileAppPropertiesProtoCodec codec = new MobileAppPropertiesProtoCodec();

    @Override
    public WebClientInformation convert(WebClientInformationProto.WebClientInformation protoObject) {
        if (protoObject == null) {
            return null;
        }

        WebClientInformation info = new WebClientInformation();

        if (protoObject.hasClientAddress()) {
            info.setClientAddress(protoObject.getClientAddress().getValue());
        }

        if (protoObject.hasMobileAppProperties()) {
            info.setMobileAppProperties(codec.convert(protoObject.getMobileAppProperties()));
        }

        return info;
    }

    @Override
    public WebClientInformationProto.WebClientInformation map(WebClientInformation commonObject) {
        if (commonObject == null) {
            return null;
        }

        WebClientInformationProto.WebClientInformation.Builder builder = WebClientInformationProto.WebClientInformation
                .newBuilder();

        Optional.ofNullable(codec.map(commonObject.getMobileAppProperties()))
                .ifPresent(builder::setMobileAppProperties);
        Optional.ofNullable(commonObject.getClientAddress()).ifPresent(addr -> {
            builder.setClientAddress(StringValue.of(addr));
        });

        return builder.build();
    }
}
