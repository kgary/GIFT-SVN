/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.Optional;

import com.google.protobuf.BoolValue;
import com.google.protobuf.StringValue;

import generated.proto.common.InitializePedagogicalModelRequestProto;
import mil.arl.gift.common.InitializePedagogicalModelRequest;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf
 * InitializePedagogicalModelRequest.
 * 
 * @author cpolynice
 *
 */
public class InitializePedagogicalModelRequestProtoCodec implements
        ProtoCodec<InitializePedagogicalModelRequestProto.InitializePedagogicalModelRequest, InitializePedagogicalModelRequest> {

    @Override
    public InitializePedagogicalModelRequest convert(
            InitializePedagogicalModelRequestProto.InitializePedagogicalModelRequest protoObject) {
        if (protoObject == null) {
            return null;
        }

        String actions = protoObject.hasActions() ? protoObject.getActions().getValue() : null;
        boolean courseActions = protoObject.hasCourseActions() ? protoObject.getCourseActions().getValue() : false;

        InitializePedagogicalModelRequest pedRequest = new InitializePedagogicalModelRequest(actions, courseActions);
        String pedConfig = protoObject.hasConfiguration() ? protoObject.getConfiguration().getValue() : null;
        pedRequest.setPedModelConfig(pedConfig);

        return pedRequest;
    }

    @Override
    public InitializePedagogicalModelRequestProto.InitializePedagogicalModelRequest map(
            InitializePedagogicalModelRequest commonObject) {
        if (commonObject == null) {
            return null;
        }

        InitializePedagogicalModelRequestProto.InitializePedagogicalModelRequest.Builder builder = InitializePedagogicalModelRequestProto.InitializePedagogicalModelRequest
                .newBuilder();

        builder.setCourseActions(BoolValue.of(commonObject.isCourseActions()));
        Optional.ofNullable(commonObject.getActions()).ifPresent(action -> {
            builder.setActions(StringValue.of(action));
        });
        Optional.ofNullable(commonObject.getPedModelConfig()).ifPresent(config -> {
            builder.setConfiguration(StringValue.of(config));
        });

        return builder.build();
    }

}
