/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.ArrayList;
import java.util.Optional;

import com.google.protobuf.BoolValue;
import com.google.protobuf.Int32Value;
import com.google.protobuf.StringValue;

import generated.proto.common.DisplayChatWindowUpdateRequestProto;
import mil.arl.gift.common.DisplayAvatarAction;
import mil.arl.gift.common.DisplayChatWindowUpdateRequest;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf
 * DisplayChatWindowUpdateRequest.
 * 
 * @author cpolynice
 *
 */
public class DisplayChatWindowUpdateRequestProtoCodec implements
        ProtoCodec<DisplayChatWindowUpdateRequestProto.DisplayChatWindowUpdateRequest, DisplayChatWindowUpdateRequest> {

    /* Codec that will be used to convert to/from a DisplayAvatarAction. */
    private static DisplayAvatarActionProtoCodec codec = new DisplayAvatarActionProtoCodec();

    @Override
    public DisplayChatWindowUpdateRequest convert(
            DisplayChatWindowUpdateRequestProto.DisplayChatWindowUpdateRequest protoObject) {
        if (protoObject == null) {
            return null;
        }

        String text = protoObject.hasText() ? protoObject.getText().getValue() : null;
        Boolean closed = protoObject.hasClosed() ? protoObject.getClosed().getValue() : null;
        DisplayAvatarAction action = protoObject.hasAction() ? codec.convert(protoObject.getAction()) : null;
        int chatId = protoObject.hasId() ? protoObject.getId().getValue() : null;

        if (closed == null) {
            throw new MessageDecodeException(this.getClass().getName(), "The chat closed value is null");
        }

        DisplayChatWindowUpdateRequest request = new DisplayChatWindowUpdateRequest(closed, chatId);
        request.setAvatarAction(action);
        request.setText(text);

        if (CollectionUtils.isNotEmpty(protoObject.getChoicesList())) {
            request.setChoices(new ArrayList<>(protoObject.getChoicesList()));
        }

        if (protoObject.hasFreeResponse()) {
            request.setAllowFreeResponse(protoObject.getFreeResponse().getValue());
        }

        return request;
    }

    @Override
    public DisplayChatWindowUpdateRequestProto.DisplayChatWindowUpdateRequest map(
            DisplayChatWindowUpdateRequest commonObject) {
        if (commonObject == null) {
            return null;
        }

        DisplayChatWindowUpdateRequestProto.DisplayChatWindowUpdateRequest.Builder builder = DisplayChatWindowUpdateRequestProto.DisplayChatWindowUpdateRequest
                .newBuilder();

        builder.setClosed(BoolValue.of(commonObject.isChatClosed()));
        builder.setFreeResponse(BoolValue.of(commonObject.shouldAllowFreeResponse()));
        builder.setId(Int32Value.of(commonObject.getChatId()));
        Optional.ofNullable(codec.map(commonObject.getAvatarAction())).ifPresent(builder::setAction);
        Optional.ofNullable(commonObject.getChoices()).ifPresent(builder::addAllChoices);
        Optional.ofNullable(commonObject.getText()).ifPresent(text -> {
            builder.setText(StringValue.of(text));
        });

        return builder.build();
    }
}
