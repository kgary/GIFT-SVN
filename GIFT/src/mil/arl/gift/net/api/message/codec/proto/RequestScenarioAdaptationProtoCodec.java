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
import mil.arl.gift.common.RequestScenarioAdaptation;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a RequestScenarioAdaptation
 * message.
 * 
 * @author cpolynice
 *
 */
public class RequestScenarioAdaptationProtoCodec
        implements ProtoCodec<AbstractPedagogicalRequestProto.RequestScenarioAdaptation, RequestScenarioAdaptation> {

    @Override
    public RequestScenarioAdaptation convert(AbstractPedagogicalRequestProto.RequestScenarioAdaptation protoObject) {
        if (protoObject == null) {
            return null;
        }

        String strategyName = protoObject.hasStrategyName() ? protoObject.getStrategyName().getValue() : null;
        float waitTime = protoObject.hasWaitTime() ? protoObject.getWaitTime().getValue() : 0;
        String reason = protoObject.hasReason() ? protoObject.getReason().getValue() : null;
        RequestScenarioAdaptation tactic = new RequestScenarioAdaptation(strategyName);

        if (protoObject.hasMacro()) {
            tactic.setIsMacroRequest(protoObject.getMacro().getValue());
        }

        tactic.setDelayAfterStrategy(waitTime);
        tactic.setReasonForRequest(reason);
        return tactic;
    }

    @Override
    public AbstractPedagogicalRequestProto.RequestScenarioAdaptation map(RequestScenarioAdaptation commonObject) {
        if (commonObject == null) {
            return null;
        }

        AbstractPedagogicalRequestProto.RequestScenarioAdaptation.Builder builder = AbstractPedagogicalRequestProto.RequestScenarioAdaptation
                .newBuilder();

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
