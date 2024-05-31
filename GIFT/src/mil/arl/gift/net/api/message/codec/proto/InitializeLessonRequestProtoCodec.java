/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import com.google.protobuf.StringValue;

import generated.proto.common.InitializeLessonRequestProto;
import mil.arl.gift.common.InitializeLessonRequest;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf
 * InitializeLessonRequest instance.
 * 
 * @author cpolynice
 *
 */
public class InitializeLessonRequestProtoCodec implements ProtoCodec<InitializeLessonRequestProto.InitializeLessonRequest, InitializeLessonRequest> {

    @Override
    public InitializeLessonRequest convert(InitializeLessonRequestProto.InitializeLessonRequest protoObject) {
        if (protoObject == null) {
            return null;
        }

        String contentRef = protoObject.hasContentRef() ? protoObject.getContentRef().getValue() : "UNKNOWN";
        return new InitializeLessonRequest(contentRef);
    }

    @Override
    public InitializeLessonRequestProto.InitializeLessonRequest map(InitializeLessonRequest commonObject) {
        if (commonObject == null) {
            return InitializeLessonRequestProto.InitializeLessonRequest.getDefaultInstance();
        }

        InitializeLessonRequestProto.InitializeLessonRequest.Builder builder = InitializeLessonRequestProto.InitializeLessonRequest
                .newBuilder();
        return StringUtils.isNotBlank(commonObject.getContentReference())
                ? builder.setContentRef(StringValue.of(commonObject.getContentReference())).build()
                : builder.build();
    }

}
