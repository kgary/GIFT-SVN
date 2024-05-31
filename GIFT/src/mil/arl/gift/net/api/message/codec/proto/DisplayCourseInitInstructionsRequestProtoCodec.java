/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.ArrayList;
import java.util.Optional;

import com.google.protobuf.StringValue;

import generated.proto.common.DisplayCourseInitInstructionsRequestProto;
import mil.arl.gift.common.DisplayCourseInitInstructionsRequest;
import mil.arl.gift.common.DisplayCourseInitInstructionsRequest.GatewayStateEnum;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a
 * DisplayCourseInitInstructionsRequest.
 * 
 * @author cpolynice
 *
 */
public class DisplayCourseInitInstructionsRequestProtoCodec implements
        ProtoCodec<DisplayCourseInitInstructionsRequestProto.DisplayCourseInitInstructionsRequest, DisplayCourseInitInstructionsRequest> {

    @Override
    public DisplayCourseInitInstructionsRequest convert(
            DisplayCourseInitInstructionsRequestProto.DisplayCourseInitInstructionsRequest protoObject) {
        if (protoObject == null) {
            return null;
        }

        GatewayStateEnum state = protoObject.hasGwState()
                ? GatewayStateEnum.valueOf(protoObject.getGwState().getValue())
                : null;

        DisplayCourseInitInstructionsRequest request;
        if (CollectionUtils.isNotEmpty(protoObject.getAssetUrlsList())) {
            ArrayList<String> assetURLs = new ArrayList<>(protoObject.getAssetUrlsList());
            request = new DisplayCourseInitInstructionsRequest(assetURLs);
        } else {
            request = new DisplayCourseInitInstructionsRequest();
        }

        request.setGatewayState(state);
        return request;
    }

    @Override
    public DisplayCourseInitInstructionsRequestProto.DisplayCourseInitInstructionsRequest map(
            DisplayCourseInitInstructionsRequest commonObject) {
        if (commonObject == null) {
            return null;
        }

        DisplayCourseInitInstructionsRequestProto.DisplayCourseInitInstructionsRequest.Builder builder = DisplayCourseInitInstructionsRequestProto.DisplayCourseInitInstructionsRequest
                .newBuilder();

        Optional.ofNullable(commonObject.getAssetURLs()).ifPresent(builder::addAllAssetUrls);
        Optional.ofNullable(commonObject.getGatewayState()).ifPresent(state -> {
            builder.setGwState(StringValue.of(state.toString()));
        });

        return builder.build();
    }
}
