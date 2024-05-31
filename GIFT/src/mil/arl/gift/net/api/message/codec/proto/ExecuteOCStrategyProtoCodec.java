/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.util.Optional;

import javax.xml.bind.JAXBException;

import org.xml.sax.SAXException;

import com.google.protobuf.BoolValue;
import com.google.protobuf.StringValue;

import generated.dkf.Strategy;
import generated.proto.common.ExecuteOCStrategyProto;
import mil.arl.gift.common.course.strategy.ExecuteOCStrategy;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.UnmarshalledFile;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf
 * ExecuteOCStrategy.
 * 
 * @author cpolynice
 *
 */
public class ExecuteOCStrategyProtoCodec
        implements ProtoCodec<ExecuteOCStrategyProto.ExecuteOCStrategy, ExecuteOCStrategy> {

    @Override
    public ExecuteOCStrategy convert(ExecuteOCStrategyProto.ExecuteOCStrategy protoObject) {
        if (protoObject == null) {
            return null;
        }

        try {
            String strategyXML = protoObject.hasOcStrategy() ? protoObject.getOcStrategy().getValue() : null;

            if (strategyXML == null) {
                throw new MessageDecodeException(ExecuteOCStrategyProtoCodec.class.getName(),
                        "The strategyXML cannot be null.");
            }

            UnmarshalledFile strategiesFile = AbstractSchemaHandler.parseAndValidate(Strategy.class,
                    new ByteArrayInputStream(strategyXML.getBytes()), (java.io.File) null, true);

            /* Decode the XML string representation of the strategies */
            Strategy strategy = (Strategy) strategiesFile.getUnmarshalled();
            String evaluator = protoObject.hasEvaluator() ? protoObject.getEvaluator().getValue() : null;
            String reason = protoObject.hasReason() ? protoObject.getReason().getValue() : null;

            ExecuteOCStrategy ocStrategy = new ExecuteOCStrategy(strategy, evaluator, reason);

            if (protoObject.hasScenarioSupport()) {
                ocStrategy.setScenarioSupport(protoObject.getScenarioSupport().getValue());
            }

            return ocStrategy;
        } catch (JAXBException | SAXException | FileNotFoundException parseEx) {
            throw new MessageDecodeException(ExecuteOCStrategyProtoCodec.class.getName(),
                    "There was an issue parsing " + protoObject, parseEx);
        }
    }

    @Override
    public ExecuteOCStrategyProto.ExecuteOCStrategy map(ExecuteOCStrategy commonObject) {
        if (commonObject == null) {
            return null;
        }

        ExecuteOCStrategyProto.ExecuteOCStrategy.Builder builder = ExecuteOCStrategyProto.ExecuteOCStrategy
                .newBuilder();

        /* Encode the strategies as an XML string */
        try {
            String strategyString = AbstractSchemaHandler.getAsXMLString(commonObject.getStrategy(), Strategy.class,
                    AbstractSchemaHandler.DKF_SCHEMA_FILE);

            builder.setScenarioSupport(BoolValue.of(commonObject.isScenarioControl()));

            Optional.ofNullable(strategyString).ifPresent(oc -> {
                builder.setOcStrategy(StringValue.of(oc));
            });
            Optional.ofNullable(commonObject.getEvaluator()).ifPresent(evaluator -> {
                builder.setEvaluator(StringValue.of(evaluator));
            });
            Optional.ofNullable(commonObject.getReason()).ifPresent(reason -> {
                builder.setReason(StringValue.of(reason));
            });

            return builder.build();
        } catch (SAXException | JAXBException e) {
            throw new MessageDecodeException(ExecuteOCStrategyProtoCodec.class.getName(),
                    "There was an issue encoding the OC strategy", e);
        }
    }

}
