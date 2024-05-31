/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.Optional;

import com.google.protobuf.Int32Value;
import com.google.protobuf.StringValue;

import generated.proto.common.MobileAppPropertiesProto;
import mil.arl.gift.common.MobileAppProperties;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf
 * MobileAppProperties.
 * 
 * @author cpolynice
 *
 */
public class MobileAppPropertiesProtoCodec
        implements ProtoCodec<MobileAppPropertiesProto.MobileAppProperties, MobileAppProperties> {

    @Override
    public MobileAppProperties convert(MobileAppPropertiesProto.MobileAppProperties protoObject) {
        if (protoObject == null) {
            return null;
        }

        MobileAppProperties properties = new MobileAppProperties();

        if (protoObject.hasPlatform()) {
            properties.setPlatform(protoObject.getPlatform().getValue());
        }

        if (protoObject.hasVersion()) {
            properties.setVersion(protoObject.getVersion().getValue());
        }

        if (protoObject.hasScreenWidth()) {
            properties.setScreenWidth(protoObject.getScreenWidth().getValue());
        }

        if (protoObject.hasScreenHeight()) {
            properties.setScreenHeight(protoObject.getScreenHeight().getValue());
        }

        return properties;
    }

    @Override
    public MobileAppPropertiesProto.MobileAppProperties map(MobileAppProperties commonObject) {
        if (commonObject == null) {
            return null;
        }

        MobileAppPropertiesProto.MobileAppProperties.Builder builder = MobileAppPropertiesProto.MobileAppProperties
                .newBuilder();

        builder.setScreenWidth(Int32Value.of(commonObject.getScreenWidth()));
        builder.setScreenHeight(Int32Value.of(commonObject.getScreenHeight()));
        Optional.ofNullable(commonObject.getPlatform()).ifPresent(platform -> {
            builder.setPlatform(StringValue.of(platform));
        });
        Optional.ofNullable(commonObject.getVersion()).ifPresent(version -> {
            builder.setVersion(StringValue.of(version));
        });

        return builder.build();
    }
}
