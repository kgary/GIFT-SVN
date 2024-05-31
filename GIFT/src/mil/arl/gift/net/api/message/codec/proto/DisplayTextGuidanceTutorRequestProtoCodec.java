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
import mil.arl.gift.common.DisplayTextGuidanceTutorRequest;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf
 * DisplayTextGuidanceTutorRequest message.
 * 
 * @author cpolynice
 *
 */
public class DisplayTextGuidanceTutorRequestProtoCodec implements
        ProtoCodec<AbstractDisplayGuidanceTutorRequestProto.DisplayTextGuidanceTutorRequest, DisplayTextGuidanceTutorRequest> {

    @Override
    public DisplayTextGuidanceTutorRequest convert(
            AbstractDisplayGuidanceTutorRequestProto.DisplayTextGuidanceTutorRequest protoObject) {
        if (protoObject == null) {
            return null;
        }

        int displayDuration;
        boolean whileTALoads, fullScreen;
        String text;

        if (protoObject.hasText()) {
            displayDuration = protoObject.hasDisplayDuration() ? protoObject.getDisplayDuration().getValue() : 0;
            fullScreen = protoObject.hasFullscreen() ? protoObject.getFullscreen().getValue() : false;
            whileTALoads = protoObject.hasWhileTaLoads() ? protoObject.getWhileTaLoads().getValue() : false;
            text = protoObject.hasText() ? protoObject.getText().getValue() : null;

            DisplayTextGuidanceTutorRequest displayTextGuidanceTutorRequest = new DisplayTextGuidanceTutorRequest(text,
                    fullScreen, displayDuration);
            displayTextGuidanceTutorRequest.setWhileTrainingAppLoads(whileTALoads);

            return displayTextGuidanceTutorRequest;
        } else {
            return DisplayTextGuidanceTutorRequest.EMPTY_REQUEST;
        }
    }

    @Override
    public AbstractDisplayGuidanceTutorRequestProto.DisplayTextGuidanceTutorRequest map(
            DisplayTextGuidanceTutorRequest commonObject) {
        if (commonObject == null) {
            return null;
        }

        AbstractDisplayGuidanceTutorRequestProto.DisplayTextGuidanceTutorRequest.Builder builder = AbstractDisplayGuidanceTutorRequestProto.DisplayTextGuidanceTutorRequest
                .newBuilder();

        builder.setDisplayDuration(Int32Value.of(commonObject.getDisplayDuration()));
        builder.setWhileTaLoads(BoolValue.of(commonObject.isWhileTrainingAppLoads()));
        builder.setFullscreen(BoolValue.of(commonObject.isFullscreen()));

        Optional.ofNullable(commonObject.getText()).ifPresent(text -> {
            builder.setText(StringValue.of(text));
        });

        return builder.build();
    }

}
