/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import com.google.protobuf.Int32Value;

import generated.proto.common.DomainSessionStartTimeRequestProto;
import generated.proto.common.DomainSessionStartTimeRequestProto.DomainSessionStartTimeRequest;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf
 * DomainSessionStartTimeRequest.
 * 
 * @author cpolynice
 *
 */
public class DomainSessionStartTimeRequestProtoCodec
        implements ProtoCodec<DomainSessionStartTimeRequestProto.DomainSessionStartTimeRequest, Integer> {

    @Override
    public Integer convert(DomainSessionStartTimeRequest protoObject) {
        if (protoObject == null) {
            return null;
        }

        return protoObject.hasDomainSessionId() ? protoObject.getDomainSessionId().getValue() : 0;
    }

    @Override
    public DomainSessionStartTimeRequest map(Integer commonObject) {
        return commonObject != null
                ? DomainSessionStartTimeRequest.newBuilder().setDomainSessionId(Int32Value.of(commonObject)).build()
                : DomainSessionStartTimeRequest.newBuilder().build();
    }

}
