/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.Optional;

import generated.proto.common.AbstractRawScoreProto;
import mil.arl.gift.common.score.AbstractRawScore;
import mil.arl.gift.common.score.DefaultRawScore;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.api.message.MessageEncodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * 
 * Abstract superclass for RawScore classes implemented in protobuf.
 * 
 * @author cpolynice
 */
public class AbstractRawScoreProtoCodec
        implements ProtoCodec<AbstractRawScoreProto.AbstractRawScore, AbstractRawScore> {

    /**
     * Codec used that will convert to/from protobuf instances of a
     * DefaultRawScore.
     */
    private static DefaultRawScoreProtoCodec codec = new DefaultRawScoreProtoCodec();

    @Override
    public AbstractRawScore convert(AbstractRawScoreProto.AbstractRawScore protoObject) {
        if (protoObject == null) {
            return null;
        }

        if (protoObject.hasDefaultRawScore()) {
            return codec.convert(protoObject.getDefaultRawScore());
        } else {
            throw new MessageDecodeException(this.getClass().getName(), "Exception logged while converting ");
        }
    }

    @Override
    public AbstractRawScoreProto.AbstractRawScore map(AbstractRawScore commonObject) {
        if (commonObject == null) {
            return null;
        }

        AbstractRawScoreProto.AbstractRawScore.Builder builder = AbstractRawScoreProto.AbstractRawScore.newBuilder();

        if (commonObject instanceof DefaultRawScore) {
            Optional.ofNullable(codec.map((DefaultRawScore) commonObject)).ifPresent(builder::setDefaultRawScore);
        } else {
            throw new MessageEncodeException(this.getClass().getName(),
                    "Found unhandled score type of " + commonObject);
        }

        return builder.build();
    }
}
