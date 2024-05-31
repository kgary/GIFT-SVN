/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.Optional;

import com.google.protobuf.BoolValue;
import com.google.protobuf.FloatValue;
import com.google.protobuf.StringValue;

import generated.proto.common.AbstractPedagogicalRequestProto;
import mil.arl.gift.common.RequestBranchAdaptation;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a RequestBranchAdaptation
 * message.
 * 
 * @author cpolynice
 *
 */
public class RequestBranchAdaptationProtoCodec
        implements ProtoCodec<AbstractPedagogicalRequestProto.RequestBranchAdaptation, RequestBranchAdaptation> {

    /* Codec that will be used to convert to/from a BranchAdaptationStrategy
     * message. */
    private static BranchAdaptationStrategyProtoCodec codec = new BranchAdaptationStrategyProtoCodec();

    @Override
    public RequestBranchAdaptation convert(AbstractPedagogicalRequestProto.RequestBranchAdaptation protoObject) {
        if (protoObject == null) {
            return null;
        }

        if (protoObject.hasStrategy()) {
            float waitTime = protoObject.hasWaitTime() ? protoObject.getWaitTime().getValue() : 0;
            String reason = protoObject.hasReason() ? protoObject.getReason().getValue() : null;

            RequestBranchAdaptation branchAdaptation = new RequestBranchAdaptation(
                    codec.convert(protoObject.getStrategy()));

            if (protoObject.hasMacro()) {
                branchAdaptation.setIsMacroRequest(protoObject.getMacro().getValue());
            }

            branchAdaptation.setDelayAfterStrategy(waitTime);
            branchAdaptation.setReasonForRequest(reason);
            return branchAdaptation;
        } else {
            return null;
        }
    }

    @Override
    public AbstractPedagogicalRequestProto.RequestBranchAdaptation map(RequestBranchAdaptation commonObject) {
        if (commonObject == null) {
            return null;
        }

        AbstractPedagogicalRequestProto.RequestBranchAdaptation.Builder builder = AbstractPedagogicalRequestProto.RequestBranchAdaptation
                .newBuilder();

        Optional.ofNullable(codec.map(commonObject.getStrategy())).ifPresent(builder::setStrategy);

        builder.setWaitTime(FloatValue.of(commonObject.getDelayAfterStrategy()));
        builder.setMacro(BoolValue.of(commonObject.isMacroRequest()));
        Optional.ofNullable(commonObject.getStrategyName()).ifPresent(name -> {
            builder.setStrategyName(StringValue.of(name));
        });
        Optional.ofNullable(commonObject.getReasonForRequest()).ifPresent(reason -> {
            builder.setReason(StringValue.of(reason));
        });

        return builder.build();
    }
}
