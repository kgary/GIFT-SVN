/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.math.BigDecimal;
import java.util.Optional;

import com.google.protobuf.FloatValue;
import com.google.protobuf.StringValue;

import generated.course.BooleanEnum;
import generated.course.Size;
import generated.course.YoutubeVideoProperties;
import generated.proto.common.YoutubeVideoPropertiesProto;
import mil.arl.gift.common.enums.VideoCssUnitsEnum;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf
 * YoutubeVideoProperties message.
 * 
 * @author cpolynice
 *
 */
public class YoutubeVideoPropertiesProtoCodec
        implements ProtoCodec<YoutubeVideoPropertiesProto.YoutubeVideoProperties, YoutubeVideoProperties> {

    @Override
    public YoutubeVideoProperties convert(YoutubeVideoPropertiesProto.YoutubeVideoProperties protoObject) {
        if (protoObject == null) {
            return null;
        }

        YoutubeVideoProperties properties = new YoutubeVideoProperties();
        generated.course.Size size;
        BooleanEnum allowFullScreen;

        if (protoObject.hasWidth() && protoObject.hasHeight()) {
            String width = String.valueOf(protoObject.getWidth().getValue());
            String height = String.valueOf(protoObject.getHeight().getValue());

            size = new Size();
            size.setWidth(new BigDecimal(width));
            size.setHeight(new BigDecimal(height));

            if (protoObject.hasWidthUnits()) {
                size.setWidthUnits(protoObject.getWidthUnits().getValue());
            } else {
                size.setWidthUnits(VideoCssUnitsEnum.PIXELS.getName());
            }

            if (protoObject.hasHeightUnits()) {
                size.setHeightUnits(protoObject.getHeightUnits().getValue());
            } else {
                size.setWidthUnits(VideoCssUnitsEnum.PIXELS.getName());
            }

            if (protoObject.hasConstrainToScreen()) {
                BooleanEnum constrainToScreen = BooleanEnum.fromValue(protoObject.getConstrainToScreen().getValue());
                size.setConstrainToScreen(constrainToScreen);
            } else {
                size.setConstrainToScreen(BooleanEnum.FALSE);
            }

            properties.setSize(size);
        }

        if (protoObject.hasFullScreen()) {
            /* This is an optional value. */
            allowFullScreen = BooleanEnum.fromValue(protoObject.getFullScreen().getValue());
            properties.setAllowFullScreen(allowFullScreen);
        }

        return properties;
    }

    @Override
    public YoutubeVideoPropertiesProto.YoutubeVideoProperties map(YoutubeVideoProperties commonObject) {
        if (commonObject == null) {
            return null;
        }

        YoutubeVideoPropertiesProto.YoutubeVideoProperties.Builder builder = YoutubeVideoPropertiesProto.YoutubeVideoProperties
                .newBuilder();

        if (commonObject.getSize() != null) {
            Optional.ofNullable(commonObject.getSize().getWidth()).ifPresent(width -> {
                builder.setWidth(FloatValue.of(width.floatValue()));
            });

            Optional.ofNullable(commonObject.getSize().getHeight()).ifPresent(height -> {
                builder.setHeight(FloatValue.of(height.floatValue()));
            });

            Optional.ofNullable(commonObject.getSize().getWidthUnits()).ifPresent(units -> {
                builder.setWidthUnits(StringValue.of(units));
            });

            Optional.ofNullable(commonObject.getSize().getHeightUnits()).ifPresent(units -> {
                builder.setHeightUnits(StringValue.of(units));
            });

            Optional.ofNullable(commonObject.getSize().getConstrainToScreen()).ifPresent(constrain -> {
                builder.setConstrainToScreen(StringValue.of(constrain.value()));
            });
        }

        Optional.ofNullable(commonObject.getAllowFullScreen()).ifPresent(fullScreen -> {
            builder.setFullScreen(StringValue.of(fullScreen.value()));
        });

        return builder.build();
    }
}
