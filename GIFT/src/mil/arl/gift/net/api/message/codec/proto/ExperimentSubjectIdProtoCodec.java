/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.Optional;

import com.google.protobuf.Int32Value;
import com.google.protobuf.StringValue;

import generated.proto.common.ExperimentSubjectIdProto;
import mil.arl.gift.common.experiment.ExperimentSubjectId;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf
 * ExperimentSubjectId.
 * 
 * @author cpolynice
 *
 */
public class ExperimentSubjectIdProtoCodec
        implements ProtoCodec<ExperimentSubjectIdProto.ExperimentSubjectId, ExperimentSubjectId> {

    @Override
    public ExperimentSubjectId convert(ExperimentSubjectIdProto.ExperimentSubjectId protoObject) {
        if (protoObject == null) {
            return null;
        }

        int subjectId = protoObject.hasSubjectId() ? protoObject.getSubjectId().getValue() : 0;
        String experimentId = protoObject.hasExperimentId() ? protoObject.getExperimentId().getValue() : null;

        return new ExperimentSubjectId(subjectId, experimentId);
    }

    @Override
    public ExperimentSubjectIdProto.ExperimentSubjectId map(ExperimentSubjectId commonObject) {
        if (commonObject == null) {
            return null;
        }

        ExperimentSubjectIdProto.ExperimentSubjectId.Builder builder = ExperimentSubjectIdProto.ExperimentSubjectId
                .newBuilder();

        builder.setSubjectId(Int32Value.of(commonObject.getSubjectId()));
        Optional.ofNullable(commonObject.getExperimentId()).ifPresent(id -> {
            builder.setExperimentId(StringValue.of(id));
        });

        return builder.build();
    }

}
