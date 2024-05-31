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

import generated.proto.common.AvatarDataProto;
import mil.arl.gift.common.AvatarData;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from an AvatarData message.
 * 
 * @author cpolynice
 *
 */
public class AvatarDataProtoCodec implements ProtoCodec<AvatarDataProto.AvatarData, AvatarData> {

    @Override
    public AvatarData convert(AvatarDataProto.AvatarData protoObject) {
        if (protoObject == null) {
            return null;
        }

        int height = protoObject.hasHeight() ? protoObject.getHeight().getValue() : 0;
        int width = protoObject.hasWidth() ? protoObject.getWidth().getValue() : 0;
        String url = protoObject.hasUrl() ? protoObject.getUrl().getValue() : null;

        return new AvatarData(url, height, width);
    }

    @Override
    public AvatarDataProto.AvatarData map(AvatarData commonObject) {
        if (commonObject == null) {
            return null;
        }

        AvatarDataProto.AvatarData.Builder builder = AvatarDataProto.AvatarData.newBuilder();

        builder.setHeight(Int32Value.of(commonObject.getHeight()));
        builder.setWidth(Int32Value.of(commonObject.getWidth()));
        Optional.ofNullable(commonObject.getURL()).ifPresent(url -> {
            builder.setUrl(StringValue.of(url));
        });

        return builder.build();
    }
}
