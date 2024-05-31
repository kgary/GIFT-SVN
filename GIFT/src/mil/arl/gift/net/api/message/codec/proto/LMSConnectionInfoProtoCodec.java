/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import com.google.protobuf.StringValue;

import generated.proto.common.LMSConnectionInfoProto;
import mil.arl.gift.common.LMSConnectionInfo;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf
 * LMSConnectionInfo.
 * 
 * @author cpolynice
 *
 */
public class LMSConnectionInfoProtoCodec
        implements ProtoCodec<LMSConnectionInfoProto.LMSConnectionInfo, LMSConnectionInfo> {

    @Override
    public LMSConnectionInfo convert(LMSConnectionInfoProto.LMSConnectionInfo protoObject) {
        if (protoObject == null) {
            return null;
        }

        return protoObject.hasName() ? new LMSConnectionInfo(protoObject.getName().getValue()) : null;
    }

    @Override
    public LMSConnectionInfoProto.LMSConnectionInfo map(LMSConnectionInfo commonObject) {
        if (commonObject == null) {
            return null;
        }

        LMSConnectionInfoProto.LMSConnectionInfo.Builder builder = LMSConnectionInfoProto.LMSConnectionInfo
                .newBuilder();

        return StringUtils.isNotBlank(commonObject.getName())
                ? builder.setName(StringValue.of(commonObject.getName())).build()
                : builder.build();
    }

}
