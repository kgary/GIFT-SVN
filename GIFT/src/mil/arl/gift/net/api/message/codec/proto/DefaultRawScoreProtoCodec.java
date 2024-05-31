/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import com.google.protobuf.StringValue;

import generated.proto.common.AbstractRawScoreProto;
import mil.arl.gift.common.score.DefaultRawScore;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * 
 * Default String-based protobuf implementation of a RawScore.
 * 
 * @author cpolynice
 *
 */
public class DefaultRawScoreProtoCodec implements ProtoCodec<AbstractRawScoreProto.DefaultRawScore, DefaultRawScore> {

    @Override
    public DefaultRawScore convert(AbstractRawScoreProto.DefaultRawScore protoObject) {
        if (protoObject == null) {
            return null;
        }

        String value = protoObject.hasValue() ? protoObject.getValue().getValue() : null;
        String units = protoObject.hasUnits() ? protoObject.getUnits().getValue() : null;
        return new DefaultRawScore(value, units);
    }

    @Override
    public AbstractRawScoreProto.DefaultRawScore map(DefaultRawScore commonObject) {
        if (commonObject == null) {
            return null;
        }

        String value = StringUtils.isNotBlank(commonObject.getValueAsString()) ? commonObject.getValueAsString() : null;
        String units = StringUtils.isNotBlank(commonObject.getUnitsLabel()) ? commonObject.getUnitsLabel() : null;
        return AbstractRawScoreProto.DefaultRawScore.newBuilder().setValue(StringValue.of(value))
                .setUnits(StringValue.of(units)).build();
    }
}
