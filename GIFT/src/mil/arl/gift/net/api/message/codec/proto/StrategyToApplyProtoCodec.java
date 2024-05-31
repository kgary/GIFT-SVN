/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.io.ByteArrayInputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import com.google.protobuf.StringValue;

import generated.dkf.Strategy;
import generated.proto.common.StrategyToApplyProto;
import mil.arl.gift.common.course.strategy.StrategyToApply;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.UnmarshalledFile;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf StrategyToApply.
 * 
 * @author cpolynice
 *
 */
public class StrategyToApplyProtoCodec implements ProtoCodec<StrategyToApplyProto.StrategyToApply, StrategyToApply> {

    @Override
    public StrategyToApply convert(StrategyToApplyProto.StrategyToApply protoObject) {
        if (protoObject == null) {
            return null;
        }

        try {
            String trigger = protoObject.hasTrigger() ? protoObject.getTrigger().getValue() : null;
            String strategyString = protoObject.hasStrategy() ? protoObject.getStrategy().getValue() : null;
            Strategy strategy = null;

            if (strategyString != null) {
                byte[] strategyBytes = strategyString.getBytes();
                UnmarshalledFile strategyFile = AbstractSchemaHandler.parseAndValidate(Strategy.class,
                        new ByteArrayInputStream(strategyBytes), (java.io.File) null, true);
                strategy = (Strategy) strategyFile.getUnmarshalled();
            }

            String evaluator = protoObject.hasEvaluator() ? protoObject.getEvaluator().getValue() : null;
            StrategyToApply strategyToApply = new StrategyToApply(strategy, trigger, evaluator);
            List<Integer> taskConceptIdsList = protoObject.getTaskConceptIdsList();
            strategyToApply.setTaskConceptsAppliedToo(new HashSet<>(taskConceptIdsList));
            
            return strategyToApply;
        } catch (Exception e) {
            throw new MessageDecodeException(StrategyToApplyProtoCodec.class.getName(),
                    "There was a problem decoding " + protoObject, e);
        }
    }

    @Override
    public StrategyToApplyProto.StrategyToApply map(StrategyToApply commonObject) {
        if (commonObject == null) {
            return null;
        }
        
        StrategyToApplyProto.StrategyToApply.Builder builder = StrategyToApplyProto.StrategyToApply.newBuilder();

        Optional.ofNullable(commonObject.getTrigger()).ifPresent(trigger -> {
            builder.setTrigger(StringValue.of(trigger));
        });
        try {
            String stratString = AbstractSchemaHandler.getAsXMLString(commonObject.getStrategy(), Strategy.class,
                    AbstractSchemaHandler.DKF_SCHEMA_FILE);

            Optional.ofNullable(stratString).ifPresent(str -> {
                builder.setStrategy(StringValue.of(str));
            });
            Optional.ofNullable(commonObject.getEvaluator()).ifPresent(evaluator -> {
                builder.setEvaluator(StringValue.of(evaluator));
            });
            Optional.ofNullable(commonObject.getTaskConceptsAppliedToo()).ifPresent(taskConceptsAppliedToo -> {
                builder.addAllTaskConceptIds(taskConceptsAppliedToo);
            });

            return builder.build();
        } catch (Exception e) {
            throw new MessageDecodeException(StrategyToApplyProtoCodec.class.getName(),
                    "There was a problem encoding " + commonObject, e);
        }
    }

}
