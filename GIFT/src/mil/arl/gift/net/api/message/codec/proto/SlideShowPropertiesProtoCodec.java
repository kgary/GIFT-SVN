/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.Optional;

import com.google.protobuf.StringValue;

import generated.course.BooleanEnum;
import generated.course.SlideShowProperties;
import generated.proto.common.SlideShowPropertiesProto;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf
 * SlideShowProperties message.
 * 
 * @author cpolynice
 *
 */
public class SlideShowPropertiesProtoCodec
        implements ProtoCodec<SlideShowPropertiesProto.SlideShowProperties, SlideShowProperties> {

    @Override
    public SlideShowProperties convert(SlideShowPropertiesProto.SlideShowProperties protoObject) {
        if (protoObject == null) {
            return null;
        }

        SlideShowProperties properties = new SlideShowProperties();

        if (CollectionUtils.isNotEmpty(protoObject.getSlidePathsList())) {
            properties.getSlideRelativePath().addAll(protoObject.getSlidePathsList());
        }

        if (protoObject.hasDisplayPreviousButton()) {
            properties.setDisplayPreviousSlideButton(
                    BooleanEnum.fromValue(protoObject.getDisplayPreviousButton().getValue().toLowerCase()));
        }

        if (protoObject.hasKeepContinueButton()) {
            properties.setKeepContinueButton(
                    BooleanEnum.fromValue(protoObject.getKeepContinueButton().getValue().toLowerCase()));
        }

        return properties;
    }

    @Override
    public SlideShowPropertiesProto.SlideShowProperties map(SlideShowProperties commonObject) {
        if (commonObject == null) {
            return null;
        }

        SlideShowPropertiesProto.SlideShowProperties.Builder builder = SlideShowPropertiesProto.SlideShowProperties
                .newBuilder();

        if (commonObject.getSlideRelativePath() != null) {
            builder.addAllSlidePaths(commonObject.getSlideRelativePath());
        }

        Optional.ofNullable(commonObject.getDisplayPreviousSlideButton()).ifPresent(button -> {
            builder.setDisplayPreviousButton(StringValue.of(button.value()));
        });

        Optional.ofNullable(commonObject.getKeepContinueButton()).ifPresent(button -> {
            builder.setKeepContinueButton(StringValue.of(button.value()));
        });

        return builder.build();
    }
}
