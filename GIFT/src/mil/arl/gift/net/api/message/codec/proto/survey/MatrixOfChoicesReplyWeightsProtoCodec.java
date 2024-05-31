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

import generated.proto.common.survey.MatrixOfChoicesReplyWeightsProto;
import generated.proto.common.survey.MatrixOfChoicesReplyWeightsProto.ReplyWeightsList;
import mil.arl.gift.common.survey.MatrixOfChoicesReplyWeights;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/** 
 *  This class is responsible for protobuf encoding/decoding a Matrix Of Choices
 *  Reply Weights instance.
 * 
 *  @author cpolynice
 *  
 */
public class MatrixOfChoicesReplyWeightsProtoCodec implements
        ProtoCodec<MatrixOfChoicesReplyWeightsProto.MatrixOfChoicesReplyWeights, MatrixOfChoicesReplyWeights> {

    /** Logger for the class. */
    private static Logger logger = LoggerFactory
            .getLogger(MatrixOfChoicesReplyWeightsProto.MatrixOfChoicesReplyWeights.class);

    @Override
    public MatrixOfChoicesReplyWeights convert(
            MatrixOfChoicesReplyWeightsProto.MatrixOfChoicesReplyWeights protoObject) {
        if (protoObject == null) {
            return null;
        }

        List<List<Double>> replyWeights = new ArrayList<>();

        try {

            if (CollectionUtils.isNotEmpty(protoObject.getReplyWeightsList())) {
                for (ReplyWeightsList replyWeightsObj : protoObject.getReplyWeightsList()) {
                    List<Double> rowReplyWeights = new ArrayList<>();
                    rowReplyWeights.addAll(new ArrayList<>(replyWeightsObj.getRowReplyWeightsList()));
                    replyWeights.add(rowReplyWeights);
                }
            }
        }

        catch (Exception e) {
            logger.error("Caught exception while creating matrix of choices reply weights from " + protoObject, e);
            throw new MessageDecodeException(this.getClass().getName(), "Exception logged while decoding");
        }

        return new MatrixOfChoicesReplyWeights(replyWeights);
    }

    @Override
    public MatrixOfChoicesReplyWeightsProto.MatrixOfChoicesReplyWeights map(MatrixOfChoicesReplyWeights commonObject) {
        if (commonObject == null) {
            return null;
        }

        MatrixOfChoicesReplyWeightsProto.MatrixOfChoicesReplyWeights.Builder builder = MatrixOfChoicesReplyWeightsProto.MatrixOfChoicesReplyWeights
                .newBuilder();

        if (commonObject.getReplyWeights() != null) {
            for (List<Double> doubleList : commonObject.getReplyWeights()) {
                ReplyWeightsList.Builder rowBuilder = ReplyWeightsList.newBuilder();

                for (Double replyWeight : doubleList) {
                    rowBuilder.addRowReplyWeights(replyWeight);
                }

                builder.addReplyWeights(rowBuilder);
            }
        }

        return builder.build();
    }

}
