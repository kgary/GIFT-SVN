/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto.survey;

import java.util.Optional;

import mil.arl.gift.common.survey.SliderRange.ScaleType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.DoubleValue;
import com.google.protobuf.StringValue;

import generated.proto.common.survey.SliderRangeProto;
import mil.arl.gift.common.survey.SliderRange;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/** 
 *  This class is responsible for protobuf encoding/decoding a Slider Range
 *  instance.
 * 
 *  @author cpolynice
 *  
 */
public class SliderRangeProtoCodec implements ProtoCodec<SliderRangeProto.SliderRange, SliderRange> {
    /** Logger for the class. */
    private static Logger logger = LoggerFactory.getLogger(SliderRangeProto.SliderRange.class);

    @Override
    public SliderRange convert(SliderRangeProto.SliderRange protoObject) {
        if (protoObject == null) {
            return null;
        }

        double minValue, maxValue, stepSize;
        ScaleType scaleType;

        try {
            minValue = protoObject.getMinValue().getValue();
            maxValue = protoObject.getMaxValue().getValue();
            stepSize = protoObject.hasStepSize() ? protoObject.getStepSize().getValue() : 1;
            scaleType = protoObject.hasScaleType() 
                    ? ScaleType.valueOf(protoObject.getScaleType().getValue()) 
                    : ScaleType.LINEAR;
            
            SliderRange sliderRange = new SliderRange(minValue, maxValue);
            sliderRange.setStepSize(stepSize);
            sliderRange.setScaleType(scaleType);
            return sliderRange;
        } catch (Exception e) {
            logger.error("Caught exception while creating slider range from " + protoObject, e);
            throw new MessageDecodeException(this.getClass().getName(), "Exception logged while decoding");
        }
    }

    @Override
    public SliderRangeProto.SliderRange map(SliderRange commonObject) {
        if (commonObject == null) {
            return null;
        }

        SliderRangeProto.SliderRange.Builder builder = SliderRangeProto.SliderRange.newBuilder();
        
        builder.setMinValue(DoubleValue.of(commonObject.getMinValue()));
        builder.setMaxValue(DoubleValue.of(commonObject.getMaxValue()));
        builder.setStepSize(DoubleValue.of(commonObject.getStepSize()));
        
        Optional.ofNullable(commonObject.getScaleType()).ifPresent(scale -> {
            builder.setScaleType(StringValue.of(scale.name()));
        });
            
        return builder.build();
    }

}
