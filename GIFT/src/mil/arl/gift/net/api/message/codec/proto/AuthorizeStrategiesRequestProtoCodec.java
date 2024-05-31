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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.xml.sax.SAXException;

import com.google.protobuf.StringValue;

import generated.dkf.Strategy;
import generated.proto.common.AuthorizeStrategiesRequestProto;
import generated.proto.common.AuthorizeStrategiesRequestProto.StrategyToApplyList;
import generated.proto.common.AuthorizeStrategiesRequestProto.StringList;
import generated.proto.common.StrategyToApplyProto;
import mil.arl.gift.common.course.strategy.AuthorizeStrategiesRequest;
import mil.arl.gift.common.course.strategy.StrategyToApply;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.UnmarshalledFile;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.api.message.codec.json.AuthorizeStrategiesRequestJSON;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from an
 * AuthorizeStrategiesRequest message.
 * 
 * @author cpolynice
 *
 */
public class AuthorizeStrategiesRequestProtoCodec
        implements ProtoCodec<AuthorizeStrategiesRequestProto.AuthorizeStrategiesRequest, AuthorizeStrategiesRequest> {
    
    /** common codec used to encode/decode StrategyToApply object */
    private static StrategyToApplyProtoCodec strategyToApplyProtoCodec = new StrategyToApplyProtoCodec();

    @Override
    public AuthorizeStrategiesRequest convert(AuthorizeStrategiesRequestProto.AuthorizeStrategiesRequest protoObject) {
        if (protoObject == null) {
            return null;
        }

        try {
            String evaluator = protoObject.hasEvaluator() ? protoObject.getEvaluator().getValue() : null;

            Map<String, List<StrategyToApply>> reasonsToStrategies = new HashMap<>();

            if(protoObject.getRequestsLegacyCount() > 0) {
                // legacy as of #5174 Feb 2022 - change from StringList to StrategyToApply
                for (Map.Entry<String, StringList> reasonToStrategy : protoObject.getRequestsLegacyMap().entrySet()) {
                    String key = reasonToStrategy.getKey();
                    List<StrategyToApply> value = new ArrayList<>();
                    List<String> reasonValues = CollectionUtils.isNotEmpty(reasonToStrategy.getValue().getStrategiesList())
                            ? new ArrayList<>(reasonToStrategy.getValue().getStrategiesList())
                            : null;
                    reasonsToStrategies.put(key, value);
                    
                    if (reasonValues != null) {
                        for (String reason : reasonValues) {
                            String strategyXML = reason;
                            UnmarshalledFile strategiesFile = AbstractSchemaHandler.parseAndValidate(Strategy.class,
                                    new ByteArrayInputStream(strategyXML.getBytes()), (java.io.File) null, true);
    
                            /* Decode the XML string representation of the strategies */
                            Strategy strategy = (Strategy) strategiesFile.getUnmarshalled();
                            value.add(new StrategyToApply(strategy, reason, evaluator));
                        }
                    }
                }
            }else {
                for(Map.Entry<String, StrategyToApplyList> reasonToStrategyToApply : protoObject.getRequestsMap().entrySet()) {
                    String key = reasonToStrategyToApply.getKey();
                    StrategyToApplyList strategiesToApplyProto = reasonToStrategyToApply.getValue();
                    List<StrategyToApply> value = new ArrayList<>();
                    reasonsToStrategies.put(key, value);
                    
                    // populate the map value list
                    for(StrategyToApplyProto.StrategyToApply strategyToApplyProto : strategiesToApplyProto.getStrategiesToApplyList()) {
                        StrategyToApply strategyToApply = strategyToApplyProtoCodec.convert(strategyToApplyProto);
                        value.add(strategyToApply);
                    }
                    
                }
            }

            return new AuthorizeStrategiesRequest(reasonsToStrategies, evaluator);
        } catch (JAXBException | SAXException | FileNotFoundException parseEx) {
            throw new MessageDecodeException(AuthorizeStrategiesRequestJSON.class.getName(),
                    "There was an issue parsing " + protoObject, parseEx);
        }
    }

    @Override
    public AuthorizeStrategiesRequestProto.AuthorizeStrategiesRequest map(AuthorizeStrategiesRequest commonObject) {
        if (commonObject == null) {
            return null;
        }

        AuthorizeStrategiesRequestProto.AuthorizeStrategiesRequest.Builder builder = AuthorizeStrategiesRequestProto.AuthorizeStrategiesRequest
                .newBuilder();

        for (Map.Entry<String, List<StrategyToApply>> entry : commonObject.getRequests().entrySet()) {
            /* Convert each strategy to its XML string and put each XML
             * string in a list. */
            String key = entry.getKey();
            StrategyToApplyList.Builder strategiesToApplyBuilder = StrategyToApplyList.newBuilder();
            for (StrategyToApply strategyToApply : entry.getValue()) {
                StrategyToApplyProto.StrategyToApply strategyToApplyProto = strategyToApplyProtoCodec.map(strategyToApply);
                strategiesToApplyBuilder.addStrategiesToApply(strategyToApplyProto);
            }

            builder.putRequests(key, strategiesToApplyBuilder.build());
        }

        if (commonObject.getEvaluator() != null) {
            builder.setEvaluator(StringValue.of(commonObject.getEvaluator()));
        }

        return builder.build();

    }
}
