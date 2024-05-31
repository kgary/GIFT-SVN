/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.Date;

import generated.proto.common.DomainSessionStartTimeReplyProto.DomainSessionStartTimeReply;
import mil.arl.gift.common.util.ProtobufConversionUtil;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf
 * DomainSessionStartTimeReply.
 * 
 * @author cpolynice
 *
 */
public class DomainSessionStartTimeReplyProtoCodec implements ProtoCodec<DomainSessionStartTimeReply, Date> {

    @Override
    public Date convert(DomainSessionStartTimeReply protoObject) {
        if (protoObject == null) {
            return null;
        }

        return protoObject.hasDomainSessionStartTime()
                ? new Date(ProtobufConversionUtil.convertTimestampToMillis(protoObject.getDomainSessionStartTime()))
                : null;
    }

    @Override
    public DomainSessionStartTimeReply map(Date commonObject) {
        return commonObject != null
                ? DomainSessionStartTimeReply.newBuilder()
                        .setDomainSessionStartTime(ProtobufConversionUtil.convertDateToTimestamp(commonObject)).build()
                : DomainSessionStartTimeReply.newBuilder().build();
    }
}
