/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import generated.proto.common.AbstractScaleScoreProto;
import mil.arl.gift.common.survey.score.ReplyScaleScore;
import mil.arl.gift.net.proto.ProtoCodec;

public class ReplyScaleScoreProtoCodec implements ProtoCodec<AbstractScaleScoreProto.ReplyScaleScore, ReplyScaleScore> {

    @Override
    public ReplyScaleScore convert(AbstractScaleScoreProto.ReplyScaleScore protoObject) {
        return new ReplyScaleScore();
    }

    @Override
    public AbstractScaleScoreProto.ReplyScaleScore map(ReplyScaleScore commonObject) {
        return AbstractScaleScoreProto.ReplyScaleScore.newBuilder().build();
    }

}
