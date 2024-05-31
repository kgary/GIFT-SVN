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

import generated.proto.common.ModuleAllocationReplyProto;
import mil.arl.gift.common.module.ModuleAllocationReply;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf
 * ModuleAllocationReply.
 * 
 * @author cpolynice
 *
 */
public class ModuleAllocationReplyProtoCodec
        implements ProtoCodec<ModuleAllocationReplyProto.ModuleAllocationReply, ModuleAllocationReply> {

    @Override
    public ModuleAllocationReply convert(ModuleAllocationReplyProto.ModuleAllocationReply protoObject) {
        if (protoObject == null) {
            return null;
        }

        String deniedMsg = protoObject.hasRequestDeniedMsg() ? protoObject.getRequestDeniedMsg().getValue() : null;
        String additionalInfo = protoObject.hasAdditionalInfo() ? protoObject.getAdditionalInfo().getValue() : null;

        ModuleAllocationReply reply;
        if (deniedMsg != null) {
            reply = new ModuleAllocationReply(deniedMsg);
        } else {
            reply = new ModuleAllocationReply();
        }

        reply.setAdditionalInformation(additionalInfo);
        return reply;
    }

    @Override
    public ModuleAllocationReplyProto.ModuleAllocationReply map(ModuleAllocationReply commonObject) {
        if (commonObject == null) {
            return null;
        }

        ModuleAllocationReplyProto.ModuleAllocationReply.Builder builder = ModuleAllocationReplyProto.ModuleAllocationReply
                .newBuilder();

        Optional.ofNullable(commonObject.getAdditionalInformation()).ifPresent(info -> {
            builder.setAdditionalInfo(StringValue.of(info));
        });
        Optional.ofNullable(commonObject.getRequestDeniedMessage()).ifPresent(msg -> {
            builder.setRequestDeniedMsg(StringValue.of(msg));
        });

        return builder.build();
    }
}
