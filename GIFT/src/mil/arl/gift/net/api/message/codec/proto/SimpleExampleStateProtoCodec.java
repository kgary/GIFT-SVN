/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import com.google.protobuf.StringValue;

import generated.proto.common.SimpleExampleStateProto;
import mil.arl.gift.common.ta.state.SimpleExampleState;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf
 * SimpleExampleState.
 * 
 * @author cpolynice
 *
 */
public class SimpleExampleStateProtoCodec
        implements ProtoCodec<SimpleExampleStateProto.SimpleExampleState, SimpleExampleState> {

    @Override
    public SimpleExampleState convert(SimpleExampleStateProto.SimpleExampleState protoObject) {
        if (protoObject == null) {
            return null;
        }

        return protoObject.hasVar() ? new SimpleExampleState(protoObject.getVar().getValue()) : null;
    }

    @Override
    public SimpleExampleStateProto.SimpleExampleState map(SimpleExampleState commonObject) {
        if (commonObject == null) {
            return null;
        }
        
        SimpleExampleStateProto.SimpleExampleState.Builder builder = SimpleExampleStateProto.SimpleExampleState
                .newBuilder();
        return StringUtils.isNotBlank(commonObject.getVar()) ? builder.setVar(StringValue.of(commonObject.getVar())).build() : builder.build();
    }

}
