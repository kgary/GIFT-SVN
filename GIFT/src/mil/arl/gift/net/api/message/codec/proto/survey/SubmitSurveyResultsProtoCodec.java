/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto.survey;

import java.util.Optional;

import com.google.protobuf.StringValue;

import generated.proto.common.survey.SubmitSurveyResultsProto;
import mil.arl.gift.common.SubmitSurveyResults;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for protobuf encoding/decoding the survey results
 * instance.
 * 
 * @author sharrison
 */
public class SubmitSurveyResultsProtoCodec
        implements ProtoCodec<SubmitSurveyResultsProto.SubmitSurveyResults, SubmitSurveyResults> {

    /** Codec used to convert a survey response class. */
    private static SurveyResponseProtoCodec surveyResponseCodec = new SurveyResponseProtoCodec();

    @Override
    public SubmitSurveyResults convert(SubmitSurveyResultsProto.SubmitSurveyResults protoObject) {
        String giftKey = protoObject.hasGiftKey() ? protoObject.getGiftKey().getValue() : null;
        String courseName = protoObject.hasCourseName() ? protoObject.getCourseName().getValue() : "";

        return new SubmitSurveyResults(giftKey, courseName,
                surveyResponseCodec.convert(protoObject.getSurveyResponse()));
    }

    @Override
    public SubmitSurveyResultsProto.SubmitSurveyResults map(SubmitSurveyResults commonObject) {
        if (commonObject == null) {
            return null;
        }

        final SubmitSurveyResultsProto.SubmitSurveyResults.Builder builder = SubmitSurveyResultsProto.SubmitSurveyResults
                .newBuilder();

        Optional.ofNullable(commonObject.getGiftKey()).ifPresent(giftKey -> {
            builder.setGiftKey(StringValue.of(giftKey));
        });
        
        Optional.ofNullable(commonObject.getCourseName()).ifPresent(courseName -> {
            builder.setCourseName(StringValue.of(courseName));
        });
        
        Optional.ofNullable(surveyResponseCodec.map(commonObject.getSurveyResponse()))
                .ifPresent(builder::setSurveyResponse);

        return builder.build();
    }
}
