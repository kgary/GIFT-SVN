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

import generated.proto.common.DisplayAvatarActionProto;
import mil.arl.gift.common.AvatarData;
import mil.arl.gift.common.DisplayAvatarAction;
import mil.arl.gift.common.DisplayScriptedAvatarAction;
import mil.arl.gift.common.DisplayTextToSpeechAvatarAction;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a DisplayAvatarAction.
 * 
 * @author cpolynice
 *
 */
public class DisplayAvatarActionProtoCodec
        implements ProtoCodec<DisplayAvatarActionProto.DisplayAvatarAction, DisplayAvatarAction> {

    /* Codec that will be used to convert to/from an Avatar Data. */
    private static AvatarDataProtoCodec avatarCodec = new AvatarDataProtoCodec();

    /* Codec that will be used to convert to/from a TextToSpeechAvatarAction. */
    private static TextToSpeechAvatarActionProtoCodec textCodec = new TextToSpeechAvatarActionProtoCodec();

    /* Codec that will be used to convert to/from a PreRenderedAvatarAction. */
    private static DisplayScriptedAvatarActionProtoCodec renderCodec = new DisplayScriptedAvatarActionProtoCodec();

    @Override
    public DisplayAvatarAction convert(DisplayAvatarActionProto.DisplayAvatarAction protoObject) {
        if (protoObject == null) {
            return null;
        }

        if (protoObject.hasTextToSpeechAvatarAction()) {
            return textCodec.convert(protoObject.getTextToSpeechAvatarAction());
        } else if (protoObject.hasDisplayScriptedAvatarAction()) {
            return renderCodec.convert(protoObject.getDisplayScriptedAvatarAction());
        } else {
            boolean isPreload = protoObject.hasPreloadOnly() ? protoObject.getPreloadOnly().getValue() : false;
            AvatarData avatar = protoObject.hasAvatar() ? avatarCodec.convert(protoObject.getAvatar()) : null;

            DisplayAvatarAction displayAvatarAction = new DisplayAvatarAction(avatar);
            displayAvatarAction.setPreloadOnly(isPreload);
            return displayAvatarAction;
        }
    }

    @Override
    public DisplayAvatarActionProto.DisplayAvatarAction map(DisplayAvatarAction commonObject) {
        if (commonObject == null) {
            return null;
        }

        DisplayAvatarActionProto.DisplayAvatarAction.Builder builder = DisplayAvatarActionProto.DisplayAvatarAction
                .newBuilder();

        builder.setPreloadOnly(BoolValue.of(commonObject.isPreloadOnly()));
        Optional.ofNullable(avatarCodec.map(commonObject.getAvatar())).ifPresent(builder::setAvatar);

        if (commonObject instanceof DisplayTextToSpeechAvatarAction) {
            builder.setTextToSpeechAvatarAction(textCodec.map((DisplayTextToSpeechAvatarAction) commonObject));
        } else if (commonObject instanceof DisplayScriptedAvatarAction) {
            builder.setDisplayScriptedAvatarAction(renderCodec.map((DisplayScriptedAvatarAction) commonObject));
        }

        return builder.build();
    }

}
