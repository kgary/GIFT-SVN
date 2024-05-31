/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto.survey;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generated.proto.common.survey.FreeResponseReplyWeightsProto;
import generated.proto.common.survey.FreeResponseReplyWeightsProto.ReplyWeightsTier1;
import generated.proto.common.survey.FreeResponseReplyWeightsProto.ReplyWeightsTier2;
import mil.arl.gift.common.survey.FreeResponseReplyWeights;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/** 
 *  This class is responsible for protobuf encoding/decoding a Free Response 
 *  Reply Weights instance.
 * 
 *  @author cpolynice
 *  
 */
public class FreeResponseReplyWeightsProtoCodec
        implements ProtoCodec<FreeResponseReplyWeightsProto.FreeResponseReplyWeights, FreeResponseReplyWeights> {

    /** Logger for the class. */
    private static Logger logger = LoggerFactory
            .getLogger(FreeResponseReplyWeightsProto.FreeResponseReplyWeights.class);

    @Override
    public FreeResponseReplyWeights convert(FreeResponseReplyWeightsProto.FreeResponseReplyWeights protoObject) {
        if (protoObject == null) {
            return null;
        }

        List<List<List<Double>>> replyWeights_tier1 = new ArrayList<>();

        try {
            if (CollectionUtils.isNotEmpty(protoObject.getReplyWeightsList())) {
                for (ReplyWeightsTier2 replyWeightObj : protoObject.getReplyWeightsList()) {
                    List<List<Double>> replyWeights_tier2 = new ArrayList<>();

                    for (ReplyWeightsTier1 tier1ReplyWeightObj : replyWeightObj.getRowReplyWeightsList()) {
                        List<Double> replyWeights_tier3 = new ArrayList<>();
                        replyWeights_tier3.addAll(new ArrayList<>(tier1ReplyWeightObj.getRowReplyWeightsList()));
                        replyWeights_tier2.add(replyWeights_tier3);
                    }

                    replyWeights_tier1.add(replyWeights_tier2);
                }
            }
        } catch (Exception e) {
            logger.error("Caught exception while creating free response reply weights from " + protoObject, e);
            throw new MessageDecodeException(this.getClass().getName(), "Exception logged while decoding");
        }

        return new FreeResponseReplyWeights(replyWeights_tier1);
    }

    @Override
    public FreeResponseReplyWeightsProto.FreeResponseReplyWeights map(FreeResponseReplyWeights commonObject) {
        if (commonObject == null) {
            return null;
        }
        
        FreeResponseReplyWeightsProto.FreeResponseReplyWeights.Builder builder = FreeResponseReplyWeightsProto.FreeResponseReplyWeights.newBuilder();
        
        if (commonObject.getReplyWeights() != null) {
            for (List<List<Double>> responseFieldList : commonObject.getReplyWeights()) {
                ReplyWeightsTier2.Builder tier2Builder = ReplyWeightsTier2.newBuilder();

                for (List<Double> doubleList : responseFieldList) {
                    ReplyWeightsTier1.Builder tier1Builder = ReplyWeightsTier1.newBuilder();

                    for (Double replyWeight : doubleList) {
                        tier1Builder.addRowReplyWeights(replyWeight);
                    }

                    tier2Builder.addRowReplyWeights(tier1Builder);
                }

                builder.addReplyWeights(tier2Builder);
            }
        }

        return builder.build();
    }
}
