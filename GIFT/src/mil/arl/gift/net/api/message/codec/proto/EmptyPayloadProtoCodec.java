/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import generated.proto.common.EmptyPayloadProto;
import generated.proto.common.EmptyPayloadProto.EmptyPayload;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf EmptyPayload.
 * 
 * @author cpolynice
 *
 */
public class EmptyPayloadProtoCodec implements ProtoCodec<EmptyPayloadProto.EmptyPayload, Object> {

    @Override
    public Object convert(EmptyPayload protoObject) {
        return null;
    }

    @Override
    public EmptyPayload map(Object commonObject) {
        return EmptyPayloadProto.EmptyPayload.newBuilder().build();
    }

}
