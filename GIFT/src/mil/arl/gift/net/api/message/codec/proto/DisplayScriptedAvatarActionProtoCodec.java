/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.Optional;

import com.google.protobuf.BoolValue;
import com.google.protobuf.StringValue;

import generated.proto.common.DisplayAvatarActionProto;
import mil.arl.gift.common.AvatarData;
import mil.arl.gift.common.DisplayScriptedAvatarAction;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf
 * PreRenderedAvatarAction instance.
 * 
 * @author cpolynice
 *
 */
public class DisplayScriptedAvatarActionProtoCodec
        implements ProtoCodec<DisplayAvatarActionProto.DisplayScriptedAvatarAction, DisplayScriptedAvatarAction> {

    /* Codec that will be used to convert to/from an Avatar Data. */
    private static AvatarDataProtoCodec codec = new AvatarDataProtoCodec();

    @Override
    public DisplayScriptedAvatarAction convert(DisplayAvatarActionProto.DisplayScriptedAvatarAction protoObject) {
        if (protoObject == null) {
            return null;
        }

        boolean isPreload = protoObject.hasPreloadOnly() ? protoObject.getPreloadOnly().getValue() : false;
        AvatarData avatar = protoObject.hasAvatar() ? codec.convert(protoObject.getAvatar()) : null;
        String key = protoObject.hasKey() ? protoObject.getKey().getValue() : null;

        DisplayScriptedAvatarAction scriptedAvatarAction = new DisplayScriptedAvatarAction(avatar, key);
        scriptedAvatarAction.setPreloadOnly(isPreload);
        return scriptedAvatarAction;
    }

    @Override
    public DisplayAvatarActionProto.DisplayScriptedAvatarAction map(DisplayScriptedAvatarAction commonObject) {
        if (commonObject == null) {
            return null;
        }

        DisplayAvatarActionProto.DisplayScriptedAvatarAction.Builder builder = DisplayAvatarActionProto.DisplayScriptedAvatarAction.newBuilder();

        builder.setPreloadOnly(BoolValue.of(commonObject.isPreloadOnly()));
        Optional.ofNullable(codec.map(commonObject.getAvatar())).ifPresent(builder::setAvatar);
        Optional.ofNullable(commonObject.getAction()).ifPresent(key -> {
            builder.setKey(StringValue.of(key));
        });

        return builder.build();
    }

}
