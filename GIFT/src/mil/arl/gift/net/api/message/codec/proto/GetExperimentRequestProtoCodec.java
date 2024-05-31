/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import com.google.protobuf.StringValue;

import generated.proto.common.GetExperimentRequestProto;
import mil.arl.gift.common.GetExperimentRequest;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf
 * GetExperimentRequest.
 * 
 * @author cpolynice
 *
 */
public class GetExperimentRequestProtoCodec
        implements ProtoCodec<GetExperimentRequestProto.GetExperimentRequest, GetExperimentRequest> {

    @Override
    public GetExperimentRequest convert(GetExperimentRequestProto.GetExperimentRequest protoObject) {
        if (protoObject == null) {
            return null;
        }

        return protoObject.hasExperimentId() ? new GetExperimentRequest(protoObject.getExperimentId().getValue())
                : null;
    }

    @Override
    public GetExperimentRequestProto.GetExperimentRequest map(GetExperimentRequest commonObject) {
        if (commonObject == null) {
            return null;
        }
        
        GetExperimentRequestProto.GetExperimentRequest.Builder builder = GetExperimentRequestProto.GetExperimentRequest
                .newBuilder();
        return commonObject.getExperimentId() != null
                ? builder.setExperimentId(StringValue.of(commonObject.getExperimentId())).build()
                : builder.build();
    }

}
