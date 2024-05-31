/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import generated.proto.common.AbstractEnumObjectProto.AbstractEnumObject;
import generated.proto.common.InteropConnectionsInfoProto;
import mil.arl.gift.common.InteropConnectionsInfo;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf
 * InteropConnectionsInfo instance.
 * 
 * @author cpolynice
 *
 */
public class InteropConnectionsInfoProtoCodec
        implements ProtoCodec<InteropConnectionsInfoProto.InteropConnectionsInfo, InteropConnectionsInfo> {

    /* Codec that will be used to convert to/from a protobuf abstract enum. */
    private static AbstractEnumObjectProtoCodec enumCodec = new AbstractEnumObjectProtoCodec();

    @Override
    public InteropConnectionsInfo convert(InteropConnectionsInfoProto.InteropConnectionsInfo protoObject) {
        if (protoObject == null) {
            return null;
        }

        InteropConnectionsInfo info = new InteropConnectionsInfo();

        if (CollectionUtils.isNotEmpty(protoObject.getTypesList())) {
            for (AbstractEnumObject type : protoObject.getTypesList()) {
                info.addSupportedMessageType((MessageTypeEnum) enumCodec.convert(type));
            }

            return info;
        }

        return null;
    }

    @Override
    public InteropConnectionsInfoProto.InteropConnectionsInfo map(InteropConnectionsInfo commonObject) {
        if (commonObject == null) {
            return null;
        }

        InteropConnectionsInfoProto.InteropConnectionsInfo.Builder builder = InteropConnectionsInfoProto.InteropConnectionsInfo
                .newBuilder();

        if (CollectionUtils.isNotEmpty(commonObject.getSupportedMessages())) {
            for (MessageTypeEnum type : commonObject.getSupportedMessages()) {
                builder.addTypes(enumCodec.map(type));
            }

            return builder.build();
        }

        return builder.build();
    }

}
