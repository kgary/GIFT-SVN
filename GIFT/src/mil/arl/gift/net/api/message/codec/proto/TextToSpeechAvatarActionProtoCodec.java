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

import generated.proto.common.DisplayAvatarActionProto.TextToSpeechAvatarAction;
import mil.arl.gift.common.AvatarData;
import mil.arl.gift.common.DisplayTextToSpeechAvatarAction;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf
 * TextToSpeechAvatarAction.
 * 
 * @author cpolynice
 *
 */
public class TextToSpeechAvatarActionProtoCodec
        implements ProtoCodec<TextToSpeechAvatarAction, DisplayTextToSpeechAvatarAction> {

    /* Codec that will be used to convert to/from an Avatar Data. */
    private static AvatarDataProtoCodec codec = new AvatarDataProtoCodec();

    @Override
    public DisplayTextToSpeechAvatarAction convert(TextToSpeechAvatarAction protoObject) {
        if (protoObject == null) {
            return null;
        }

        boolean isPreload = protoObject.hasPreloadOnly() ? protoObject.getPreloadOnly().getValue() : false;
        AvatarData avatar = protoObject.hasAvatar() ? codec.convert(protoObject.getAvatar()) : null;
        String text = protoObject.hasText() ? protoObject.getText().getValue() : null;

        DisplayTextToSpeechAvatarAction tSAvatarAction = new DisplayTextToSpeechAvatarAction(avatar, text);
        tSAvatarAction.setPreloadOnly(isPreload);
        return tSAvatarAction;
    }

    @Override
    public TextToSpeechAvatarAction map(DisplayTextToSpeechAvatarAction commonObject) {
        if (commonObject == null) {
            return null;
        }

        TextToSpeechAvatarAction.Builder builder = TextToSpeechAvatarAction.newBuilder();

        builder.setPreloadOnly(BoolValue.of(commonObject.isPreloadOnly()));
        Optional.ofNullable(codec.map(commonObject.getAvatar())).ifPresent(builder::setAvatar);
        Optional.ofNullable(commonObject.getText()).ifPresent(text -> {
            builder.setText(StringValue.of(text));
        });

        return builder.build();
    }

}
