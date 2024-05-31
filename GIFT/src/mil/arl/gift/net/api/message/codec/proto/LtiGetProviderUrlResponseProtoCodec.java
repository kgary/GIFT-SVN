/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import org.apache.commons.lang.StringUtils;

import com.google.protobuf.StringValue;

import generated.proto.common.LtiGetProviderUrlResponseProto.LtiGetProviderUrlResponse;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf
 * LtiGetProviderUrlResponse.
 * 
 * @author cpolynice
 *
 */
public class LtiGetProviderUrlResponseProtoCodec implements ProtoCodec<LtiGetProviderUrlResponse, String> {

    @Override
    public String convert(LtiGetProviderUrlResponse protoObject) {
        if (protoObject == null) {
            return null;
        }

        return protoObject.hasUrl() ? protoObject.getUrl().getValue() : null;
    }

    @Override
    public LtiGetProviderUrlResponse map(String commonObject) {
        return StringUtils.isNotBlank(commonObject)
                ? LtiGetProviderUrlResponse.newBuilder().setUrl(StringValue.of(commonObject)).build()
                : LtiGetProviderUrlResponse.newBuilder().build();
    }

}
