/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import com.google.protobuf.Int64Value;

import generated.proto.common.StartResumeProto;
import mil.arl.gift.common.ta.state.StartResume;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf StartResume
 * state.
 * 
 * @author cpolynice
 *
 */
public class StartResumeProtoCodec implements ProtoCodec<StartResumeProto.StartResume, StartResume> {

    @Override
    public StartResume convert(StartResumeProto.StartResume protoObject) {
        if (protoObject == null) {
            return null;
        }

        long realWorldTime = protoObject.hasRealTime() ? protoObject.getRealTime().getValue() : 0;
        long simulationTime = protoObject.hasSimTime() ? protoObject.getSimTime().getValue() : 0;
        long requestID = protoObject.hasRequestId() ? protoObject.getRequestId().getValue() : 0;

        return new StartResume(realWorldTime, simulationTime, requestID);
    }

    @Override
    public StartResumeProto.StartResume map(StartResume commonObject) {
        if (commonObject == null) {
            return null;
        }

        StartResumeProto.StartResume.Builder builder = StartResumeProto.StartResume.newBuilder();

        builder.setRealTime(Int64Value.of(commonObject.getRealWorldTime()));
        builder.setSimTime(Int64Value.of(commonObject.getSimulationTime()));
        builder.setRequestId(Int64Value.of(commonObject.getRequestID()));

        return builder.build();
    }

}
