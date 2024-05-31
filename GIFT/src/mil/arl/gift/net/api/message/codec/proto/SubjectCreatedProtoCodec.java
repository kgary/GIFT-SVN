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

import generated.proto.common.SubjectCreatedProto;
import mil.arl.gift.common.experiment.SubjectCreated;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf SubjectCreated
 * instance.
 * 
 * @author cpolynice
 *
 */
public class SubjectCreatedProtoCodec implements ProtoCodec<SubjectCreatedProto.SubjectCreated, SubjectCreated> {

    @Override
    public SubjectCreated convert(SubjectCreatedProto.SubjectCreated protoObject) {
        if (protoObject == null) {
            return null;
        }

        String courseId = protoObject.hasCourseId() ? protoObject.getCourseId().getValue() : null;
        String preSessionId = protoObject.hasPreSessionId() ? protoObject.getPreSessionId().getValue() : null;
        return new SubjectCreated(courseId, preSessionId);
    }

    @Override
    public SubjectCreatedProto.SubjectCreated map(SubjectCreated commonObject) {
        if (commonObject == null) {
            return null;
        }

        SubjectCreatedProto.SubjectCreated.Builder builder = SubjectCreatedProto.SubjectCreated.newBuilder();

        Optional.ofNullable(commonObject.getCourseId()).ifPresent(courseId -> {
            builder.setCourseId(StringValue.of(courseId));
        });
        Optional.ofNullable(commonObject.getPreSessionId()).ifPresent(preSessionId -> {
            builder.setPreSessionId(StringValue.of(preSessionId));
        });

        return builder.build();
    }
}
