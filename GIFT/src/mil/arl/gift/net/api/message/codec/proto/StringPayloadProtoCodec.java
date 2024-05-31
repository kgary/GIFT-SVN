/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import com.google.protobuf.StringValue;

import generated.proto.common.StringPayloadProto.StringPayload;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from messages where the payload
 * is a single string.
 * 
 * @author cpolynice
 *
 */
public class StringPayloadProtoCodec implements ProtoCodec<StringPayload, String> {

    @Override
    public String convert(StringPayload protoObject) {
        if (protoObject == null) {
            return null;
        }

        if (protoObject.hasPayload()) {
            return protoObject.getPayload().getValue();
        } else {
            throw new MessageDecodeException(this.getClass().getName(),
                    "Found unhandled payload type of " + protoObject);
        }
    }

    @Override
    public StringPayload map(String commonObject) {
        return commonObject != null ? StringPayload.newBuilder().setPayload(StringValue.of(commonObject)).build()
                : StringPayload.newBuilder().build();
    }

}
