/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.protobuf.BoolValue;
import com.google.protobuf.StringValue;

import generated.proto.common.ApplyStrategiesProto;
import generated.proto.common.StrategyToApplyProto;
import mil.arl.gift.common.course.strategy.ApplyStrategies;
import mil.arl.gift.common.course.strategy.StrategyToApply;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.net.proto.ProtoCodec;

public class ApplyStrategiesProtoCodec implements ProtoCodec<ApplyStrategiesProto.ApplyStrategies, ApplyStrategies> {
    private static final StrategyToApplyProtoCodec codec = new StrategyToApplyProtoCodec();
    @Override
    public ApplyStrategies convert(generated.proto.common.ApplyStrategiesProto.ApplyStrategies protoObject) {
        if (protoObject == null) {
            return null;
        }
        
        List<StrategyToApply> strategiesList = new ArrayList<>();
        String evaluator = protoObject.hasEvaluator() ? protoObject.getEvaluator().getValue() : null;
        
        if (CollectionUtils.isNotEmpty(protoObject.getStrategiesList())) {
            for (StrategyToApplyProto.StrategyToApply strategy : protoObject.getStrategiesList()) {
                strategiesList.add(codec.convert(strategy));
            }
        }
        
        ApplyStrategies applyStrategies = new ApplyStrategies(strategiesList, evaluator);

        if (protoObject.hasScenarioSupport()) {
            applyStrategies.setScenarioSupport(protoObject.getScenarioSupport().getValue());
        }

        return applyStrategies;
    }

    @Override
    public generated.proto.common.ApplyStrategiesProto.ApplyStrategies map(ApplyStrategies commonObject) {
        if (commonObject == null) {
            return null;
        }
        
        ApplyStrategiesProto.ApplyStrategies.Builder builder = ApplyStrategiesProto.ApplyStrategies.newBuilder();
        
        builder.setScenarioSupport(BoolValue.of(commonObject.isScenarioSupport()));

        Optional.ofNullable(commonObject.getEvaluator()).ifPresent(evaluator -> {
            builder.setEvaluator(StringValue.of(evaluator));
        });
        
        if (CollectionUtils.isNotEmpty(commonObject.getStrategies())) {
            for (StrategyToApply strategy : commonObject.getStrategies()) {
                builder.addStrategies(codec.map(strategy));
            }
        }
        
        return builder.build();
    }

}
