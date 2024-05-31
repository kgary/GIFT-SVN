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

import generated.proto.common.InitializeDomainSessionRequestProto;
import mil.arl.gift.common.InitializeDomainSessionRequest;
import mil.arl.gift.common.WebClientInformation;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf
 * InitializeDomainSessionRequest.
 * 
 * @author cpolynice
 *
 */
public class InitializeDomainSessionRequestProtoCodec implements
        ProtoCodec<InitializeDomainSessionRequestProto.InitializeDomainSessionRequest, InitializeDomainSessionRequest> {

    /* Codec that will be used to convert to/from a WebClientInformation
     * instance. */
    private static WebClientInformationProtoCodec codec = new WebClientInformationProtoCodec();

    @Override
    public InitializeDomainSessionRequest convert(
            InitializeDomainSessionRequestProto.InitializeDomainSessionRequest protoObject) {
        if (protoObject == null) {
            return null;
        }

        String courseFileName = protoObject.hasCourseFileName() ? protoObject.getCourseFileName().getValue() : null;
        String tutorTopicId = protoObject.hasTopicId() ? protoObject.getTopicId().getValue() : null;
        WebClientInformation clientInfo = protoObject.hasClientInfo() ? codec.convert(protoObject.getClientInfo())
                : null;

        return new InitializeDomainSessionRequest(courseFileName, tutorTopicId, clientInfo);
    }

    @Override
    public InitializeDomainSessionRequestProto.InitializeDomainSessionRequest map(
            InitializeDomainSessionRequest commonObject) {
        if (commonObject == null) {
            return null;
        }

        InitializeDomainSessionRequestProto.InitializeDomainSessionRequest.Builder builder = InitializeDomainSessionRequestProto.InitializeDomainSessionRequest
                .newBuilder();

        Optional.ofNullable(codec.map(commonObject.getClientInfo())).ifPresent(builder::setClientInfo);
        Optional.ofNullable(commonObject.getDomainCourseFileName()).ifPresent(filename -> {
            builder.setCourseFileName(StringValue.of(filename));
        });
        Optional.ofNullable(commonObject.getTutorTopicId()).ifPresent(id -> {
            builder.setTopicId(StringValue.of(id));
        });

        return builder.build();
    }

}
