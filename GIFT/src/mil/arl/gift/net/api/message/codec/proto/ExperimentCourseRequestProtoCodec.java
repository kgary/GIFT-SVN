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

import generated.proto.common.ExperimentCourseRequestProto;
import mil.arl.gift.common.ExperimentCourseRequest;
import mil.arl.gift.common.WebClientInformation;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf
 * ExperimentCourseRequest.
 * 
 * @author cpolynice
 *
 */
public class ExperimentCourseRequestProtoCodec
        implements ProtoCodec<ExperimentCourseRequestProto.ExperimentCourseRequest, ExperimentCourseRequest> {

    /* Codec that will be used to convert to/from a protobuf
     * WebClientInformation. */
    private static WebClientInformationProtoCodec codec = new WebClientInformationProtoCodec();

    @Override
    public ExperimentCourseRequest convert(ExperimentCourseRequestProto.ExperimentCourseRequest protoObject) {
        if (protoObject == null) {
            return null;
        }

        String experimentId = protoObject.hasExperimentId() ? protoObject.getExperimentId().getValue() : null;
        String experimentFolder = protoObject.hasExperimentFolder() ? protoObject.getExperimentFolder().getValue()
                : null;
        WebClientInformation info = protoObject.hasClientInfo() ? codec.convert(protoObject.getClientInfo()) : null;
        String preSessionId = protoObject.hasPreSessionId() ? protoObject.getPreSessionId().getValue() : null;

        return new ExperimentCourseRequest(experimentId, experimentFolder, info, preSessionId);
    }

    @Override
    public ExperimentCourseRequestProto.ExperimentCourseRequest map(ExperimentCourseRequest commonObject) {
        if (commonObject == null) {
            return null;
        }

        ExperimentCourseRequestProto.ExperimentCourseRequest.Builder builder = ExperimentCourseRequestProto.ExperimentCourseRequest
                .newBuilder();

        Optional.ofNullable(codec.map(commonObject.getClientInformation())).ifPresent(builder::setClientInfo);
        Optional.ofNullable(commonObject.getExperimentId()).ifPresent(id -> {
            builder.setExperimentId(StringValue.of(id));
        });
        Optional.ofNullable(commonObject.getExperimentFolder()).ifPresent(folder -> {
            builder.setExperimentFolder(StringValue.of(folder));
        });
        Optional.ofNullable(commonObject.getPreSessionId()).ifPresent(preId -> {
            builder.setPreSessionId(StringValue.of(preId));
        });

        return builder.build();
    }

}
