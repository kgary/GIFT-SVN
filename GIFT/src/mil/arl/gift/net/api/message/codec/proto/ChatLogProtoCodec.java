/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.protobuf.Int32Value;

import generated.proto.common.ChatLogProto;
import mil.arl.gift.common.ChatLog;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a ChatLog message.
 * 
 * @author cpolynice
 *
 */
public class ChatLogProtoCodec implements ProtoCodec<ChatLogProto.ChatLog, ChatLog> {

    @Override
    public ChatLog convert(ChatLogProto.ChatLog protoObject) {
        if (protoObject == null) {
            return null;
        }

        int chatId = protoObject.hasChatId() ? protoObject.getChatId().getValue() : 0;
        List<String> tutorEntries = CollectionUtils.isNotEmpty(protoObject.getTutorEntriesList())
                ? new ArrayList<>(protoObject.getTutorEntriesList())
                : new ArrayList<>();
        List<String> userEntries = CollectionUtils.isNotEmpty(protoObject.getUserEntriesList())
                ? new ArrayList<>(protoObject.getUserEntriesList())
                : new ArrayList<>();

        return new ChatLog(chatId, tutorEntries, userEntries);
    }

    @Override
    public ChatLogProto.ChatLog map(ChatLog commonObject) {
        if (commonObject == null) {
            return null;
        }

        ChatLogProto.ChatLog.Builder builder = ChatLogProto.ChatLog.newBuilder();

        builder.setChatId(Int32Value.of(commonObject.getChatId()));
        Optional.ofNullable(commonObject.getTutorEntries()).ifPresent(builder::addAllTutorEntries);
        Optional.ofNullable(commonObject.getUserEntries()).ifPresent(builder::addAllUserEntries);

        return builder.build();
    }
}
