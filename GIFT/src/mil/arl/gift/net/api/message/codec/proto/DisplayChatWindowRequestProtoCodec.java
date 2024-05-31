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
import com.google.protobuf.Int32Value;
import com.google.protobuf.StringValue;

import generated.proto.common.DisplayChatWindowRequestProto;
import mil.arl.gift.common.DisplayAvatarAction;
import mil.arl.gift.common.DisplayChatWindowRequest;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a DisplayChatWindowRequest.
 * 
 * @author cpolynice
 *
 */
public class DisplayChatWindowRequestProtoCodec
        implements ProtoCodec<DisplayChatWindowRequestProto.DisplayChatWindowRequest, DisplayChatWindowRequest> {

    /* Codec that will be used to convert to/from a DisplayAvatarAction. */
    private static DisplayAvatarActionProtoCodec codec = new DisplayAvatarActionProtoCodec();

    @Override
    public DisplayChatWindowRequest convert(DisplayChatWindowRequestProto.DisplayChatWindowRequest protoObject) {
        if (protoObject == null) {
            return null;
        }

        DisplayAvatarAction avatar = null;

        if (protoObject.hasAvatar()) {
            avatar = codec.convert(protoObject.getAvatar());
        }

        String tutorName = protoObject.hasTutorName() ? protoObject.getTutorName().getValue() : null;
        String userName = protoObject.hasUserName() ? protoObject.getUserName().getValue() : null;
        String chatName = protoObject.hasChatName() ? protoObject.getChatName().getValue() : "NO NAME";
        String description = protoObject.hasDescription() ? protoObject.getDescription().getValue() : "NO DESCRIPTION";
        Integer chatId = protoObject.hasId() ? protoObject.getId().getValue() : 0;

        DisplayChatWindowRequest request = new DisplayChatWindowRequest(chatId, avatar, tutorName, userName);

        boolean bypass = protoObject.hasBypass() ? protoObject.getBypass().getValue() : false;
        request.setProvideBypass(bypass);
        request.setFullscreen(protoObject.hasFullscreen() ? protoObject.getFullscreen().getValue() : false);
        request.setChatName(chatName);
        request.setDescription(description);
        return request;
    }

    @Override
    public DisplayChatWindowRequestProto.DisplayChatWindowRequest map(DisplayChatWindowRequest commonObject) {
        if (commonObject == null) {
            return null;
        }
        
        DisplayChatWindowRequestProto.DisplayChatWindowRequest.Builder builder = DisplayChatWindowRequestProto.DisplayChatWindowRequest.newBuilder();
        
        builder.setFullscreen(BoolValue.of(commonObject.isFullscreen()));
        builder.setBypass(BoolValue.of(commonObject.shouldProvideBypass()));
        builder.setId(Int32Value.of(commonObject.getChatId()));
        Optional.ofNullable(codec.map(commonObject.getAvatar())).ifPresent(builder::setAvatar);
        Optional.ofNullable(commonObject.getTutorName()).ifPresent(tutor -> {
            builder.setTutorName(StringValue.of(tutor));
        });
        Optional.ofNullable(commonObject.getUserName()).ifPresent(username -> {
            builder.setUserName(StringValue.of(username));
        });
        Optional.ofNullable(commonObject.getChatName()).ifPresent(chatname -> {
            builder.setChatName(StringValue.of(chatname));
        });
        Optional.ofNullable(commonObject.getDescription()).ifPresent(description -> {
            builder.setDescription(StringValue.of(description));
        });

        return builder.build();
    }
}
