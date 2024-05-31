/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import com.google.protobuf.Int32Value;
import com.google.protobuf.StringValue;

import generated.proto.common.PowerPointStateProto;
import mil.arl.gift.common.ta.state.PowerPointState;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf PowerPointState.
 * 
 * @author cpolynice
 *
 */
public class PowerPointStateProtoCodec implements ProtoCodec<PowerPointStateProto.PowerPointState, PowerPointState> {

    @Override
    public PowerPointState convert(PowerPointStateProto.PowerPointState protoObject) {
        if (protoObject == null) {
            return null;
        }

        if (protoObject.hasErrorMsg()) {
            return new PowerPointState(protoObject.getErrorMsg().getValue());
        } else {
            int slideIndex = protoObject.hasSlideIndex() ? protoObject.getSlideIndex().getValue() : 0;
            int slideCount = protoObject.hasSlideCount() ? protoObject.getSlideCount().getValue() : 0;
            return new PowerPointState(slideIndex, slideCount);
        }
    }

    @Override
    public PowerPointStateProto.PowerPointState map(PowerPointState commonObject) {
        if (commonObject == null) {
            return null;
        }

        PowerPointStateProto.PowerPointState.Builder builder = PowerPointStateProto.PowerPointState.newBuilder();

        if (StringUtils.isNotBlank(commonObject.getErrorMessage())) {
            return builder.setErrorMsg(StringValue.of(commonObject.getErrorMessage())).build();
        } else {
            return builder.setSlideCount(Int32Value.of(commonObject.getSlideCount()))
                    .setSlideIndex(Int32Value.of(commonObject.getSlideIndex())).build();
        }
    }
}
