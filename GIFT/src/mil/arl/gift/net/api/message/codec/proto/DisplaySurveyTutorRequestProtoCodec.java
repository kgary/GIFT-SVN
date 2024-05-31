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

import generated.proto.common.DisplaySurveyTutorRequestProto;
import mil.arl.gift.common.DisplaySurveyTutorRequest;
import mil.arl.gift.common.survey.Survey;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.api.message.codec.proto.survey.SurveyProtoCodec;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf
 * DisplaySurveyTutorRequest.
 * 
 * @author cpolynice
 *
 */
public class DisplaySurveyTutorRequestProtoCodec
        implements ProtoCodec<DisplaySurveyTutorRequestProto.DisplaySurveyTutorRequest, DisplaySurveyTutorRequest> {

    /* Codec that will be used to convert to/from a Survey. */
    private static SurveyProtoCodec codec = new SurveyProtoCodec();

    @Override
    public DisplaySurveyTutorRequest convert(DisplaySurveyTutorRequestProto.DisplaySurveyTutorRequest protoObject) {
        if (protoObject == null) {
            return null;
        }

        Survey survey = null;
        Boolean fullScreen = Boolean.FALSE;

        if (protoObject.hasSurvey()) {
            survey = codec.convert(protoObject.getSurvey());
            fullScreen = protoObject.getFullscreen().getValue();
        } else {
            throw new MessageDecodeException(this.getClass().getName(), "Exception logged while converting ");
        }

        DisplaySurveyTutorRequest request = new DisplaySurveyTutorRequest(survey);
        request.setFullscreen(fullScreen);

        return request;
    }

    @Override
    public DisplaySurveyTutorRequestProto.DisplaySurveyTutorRequest map(DisplaySurveyTutorRequest commonObject) {
        if (commonObject == null) {
            return null;
        }

        DisplaySurveyTutorRequestProto.DisplaySurveyTutorRequest.Builder builder = DisplaySurveyTutorRequestProto.DisplaySurveyTutorRequest
                .newBuilder();

        builder.setFullscreen(BoolValue.of(commonObject.useFullscreen()));
        Optional.ofNullable(codec.map(commonObject.getSurvey())).ifPresent(builder::setSurvey);

        return builder.build();
    }

}
