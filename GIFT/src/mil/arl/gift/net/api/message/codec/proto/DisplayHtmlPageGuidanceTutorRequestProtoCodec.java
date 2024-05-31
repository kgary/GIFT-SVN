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
import com.google.protobuf.Int32Value;
import com.google.protobuf.StringValue;

import generated.proto.common.AbstractDisplayGuidanceTutorRequestProto;
import mil.arl.gift.common.DisplayHtmlPageGuidanceTutorRequest;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf
 * DisplayHtmlPageGuidanceTutorRequest message.
 * 
 * @author cpolynice
 *
 */
public class DisplayHtmlPageGuidanceTutorRequestProtoCodec implements
        ProtoCodec<AbstractDisplayGuidanceTutorRequestProto.DisplayHtmlPageGuidanceTutorRequest, DisplayHtmlPageGuidanceTutorRequest> {

    @Override
    public DisplayHtmlPageGuidanceTutorRequest convert(
            AbstractDisplayGuidanceTutorRequestProto.DisplayHtmlPageGuidanceTutorRequest protoObject) {
        if (protoObject == null) {
            return null;
        }

        int displayDuration;
        boolean whileTALoads, fullScreen;
        String url, message;

        displayDuration = protoObject.hasDisplayDuration() ? protoObject.getDisplayDuration().getValue() : 0;
        fullScreen = protoObject.hasFullscreen() ? protoObject.getFullscreen().getValue() : false;
        whileTALoads = protoObject.hasWhileTaLoads() ? protoObject.getWhileTaLoads().getValue() : false;
        message = protoObject.hasMessage() ? protoObject.getMessage().getValue() : null;
        url = protoObject.hasUrl() ? protoObject.getUrl().getValue() : null;

        DisplayHtmlPageGuidanceTutorRequest displayHtmlPageGuidanceTutorRequest = new DisplayHtmlPageGuidanceTutorRequest(
                url, message, fullScreen, displayDuration);
        displayHtmlPageGuidanceTutorRequest.setWhileTrainingAppLoads(whileTALoads);

        return displayHtmlPageGuidanceTutorRequest;
    }

    @Override
    public AbstractDisplayGuidanceTutorRequestProto.DisplayHtmlPageGuidanceTutorRequest map(
            DisplayHtmlPageGuidanceTutorRequest commonObject) {
        if (commonObject == null) {
            return null;
        }

        AbstractDisplayGuidanceTutorRequestProto.DisplayHtmlPageGuidanceTutorRequest.Builder builder = AbstractDisplayGuidanceTutorRequestProto.DisplayHtmlPageGuidanceTutorRequest
                .newBuilder();

        builder.setDisplayDuration(Int32Value.of(commonObject.getDisplayDuration()));
        builder.setWhileTaLoads(BoolValue.of(commonObject.isWhileTrainingAppLoads()));
        builder.setFullscreen(BoolValue.of(commonObject.isFullscreen()));

        Optional.ofNullable(commonObject.getUrl()).ifPresent(url -> {
            builder.setUrl(StringValue.of(url));
        });
        Optional.ofNullable(commonObject.getMessage()).ifPresent(message -> {
            builder.setMessage(StringValue.of(message));
        });

        return builder.build();
    }

}
