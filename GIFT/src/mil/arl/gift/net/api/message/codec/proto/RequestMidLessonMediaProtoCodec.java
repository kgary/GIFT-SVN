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
import com.google.protobuf.FloatValue;
import com.google.protobuf.StringValue;

import generated.proto.common.AbstractPedagogicalRequestProto;
import mil.arl.gift.common.RequestMidLessonMedia;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a RequestMidLessonMedia
 * message.
 * 
 * @author cpolynice
 *
 */
public class RequestMidLessonMediaProtoCodec
        implements ProtoCodec<AbstractPedagogicalRequestProto.RequestMidLessonMedia, RequestMidLessonMedia> {

    @Override
    public RequestMidLessonMedia convert(AbstractPedagogicalRequestProto.RequestMidLessonMedia protoObject) {
        if (protoObject == null) {
            return null;
        }
        
        String strategyName = protoObject.hasStrategyName() ? protoObject.getStrategyName().getValue() : null;
        float waitTime = protoObject.hasWaitTime() ? protoObject.getWaitTime().getValue() : 0;
        String reason = protoObject.hasReason() ? protoObject.getReason().getValue() : null;
        RequestMidLessonMedia tactic = new RequestMidLessonMedia(strategyName);

        if (protoObject.hasMacro()) {
            tactic.setIsMacroRequest(protoObject.getMacro().getValue());
        }

        tactic.setDelayAfterStrategy(waitTime);
        tactic.setReasonForRequest(reason);
        return tactic;
    }

    @Override
    public AbstractPedagogicalRequestProto.RequestMidLessonMedia map(RequestMidLessonMedia commonObject) {
        if (commonObject == null) {
            return null;
        }

        AbstractPedagogicalRequestProto.RequestMidLessonMedia.Builder builder = AbstractPedagogicalRequestProto.RequestMidLessonMedia
                .newBuilder();
        
        builder.setWaitTime(FloatValue.of(commonObject.getDelayAfterStrategy()));
        builder.setMacro(BoolValue.of(commonObject.isMacroRequest()));
        Optional.ofNullable(commonObject.getStrategyName()).ifPresent(name -> {
            builder.setStrategyName(StringValue.of(name));
        });
        Optional.ofNullable(commonObject.getReasonForRequest()).ifPresent(reason -> {
            builder.setReason(StringValue.of(reason));
        });

        return builder.build();
    }
}
