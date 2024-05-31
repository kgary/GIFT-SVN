/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.Optional;

import generated.proto.common.AbstractScoreNodeProto;
import mil.arl.gift.common.score.AbstractScoreNode;
import mil.arl.gift.common.score.GradedScoreNode;
import mil.arl.gift.common.score.RawScoreNode;
import mil.arl.gift.common.score.TaskScoreNode;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.api.message.MessageEncodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/** 
 *  This class is responsible for protobuf encoding/decoding an AbstractScoreNode instance.
 * 
 *  @author cpolynice
 *  
 */
public class AbstractScoreNodeProtoCodec
        implements ProtoCodec<AbstractScoreNodeProto.AbstractScoreNode, AbstractScoreNode> {
    /*
     * Codecs that will assist in converting to/from protobuf representations of
     * the classes.
     */
    private static TaskScoreNodeProtoCodec taskCodec = new TaskScoreNodeProtoCodec();
    private static GradedScoreNodeProtoCodec gradedCodec = new GradedScoreNodeProtoCodec();
    private static RawScoreNodeProtoCodec rawCodec = new RawScoreNodeProtoCodec();

    @Override
    public AbstractScoreNode convert(AbstractScoreNodeProto.AbstractScoreNode protoObject) {
        if (protoObject == null) {
            return null;
        }

        if(protoObject.hasTaskScoreNode()) {
            return taskCodec.convert(protoObject.getTaskScoreNode());
        }else if (protoObject.hasGradedScoreNode()) {
            return gradedCodec.convert(protoObject.getGradedScoreNode());
        } else if (protoObject.hasRawScoreNode()) {
            return rawCodec.convert(protoObject.getRawScoreNode());
        } else {
            throw new MessageDecodeException(this.getClass().getName(), "Exception logged while decoding");
        }
    }

    @Override
    public AbstractScoreNodeProto.AbstractScoreNode map(AbstractScoreNode commonObject) {
        if (commonObject == null) {
            return null;
        }

        AbstractScoreNodeProto.AbstractScoreNode.Builder builder = AbstractScoreNodeProto.AbstractScoreNode
                .newBuilder();

        if(commonObject instanceof TaskScoreNode) {
            Optional.ofNullable(taskCodec.map((TaskScoreNode) commonObject)).ifPresent(builder::setTaskScoreNode);
        }else if (commonObject instanceof GradedScoreNode) {
            Optional.ofNullable(gradedCodec.map((GradedScoreNode) commonObject)).ifPresent(builder::setGradedScoreNode);
        } else if (commonObject instanceof RawScoreNode) {
            Optional.ofNullable(rawCodec.map((RawScoreNode) commonObject)).ifPresent(builder::setRawScoreNode);
        } else {
            throw new MessageEncodeException(this.getClass().getName(),
                    "Found unhandled score node type of " + commonObject);
        }

        return builder.build();
    }

}
