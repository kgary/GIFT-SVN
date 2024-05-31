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

import generated.proto.common.survey.SurveyGiftDataProto;
import mil.arl.gift.common.survey.Survey;
import mil.arl.gift.common.survey.SurveyGiftData;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf SurveyGiftData.
 * 
 * @author cpolynice
 *
 */
public class SurveyGiftDataProtoCodec implements ProtoCodec<SurveyGiftDataProto.SurveyGiftData, SurveyGiftData> {

    /* Codec that will be used to convert to/from Surveys. */
    private static final SurveyProtoCodec codec = new SurveyProtoCodec();

    @Override
    public SurveyGiftData convert(SurveyGiftDataProto.SurveyGiftData protoObject) {
        if (protoObject == null) {
            return null;
        }

        Survey survey = protoObject.hasSurvey() ? codec.convert(protoObject.getSurvey()) : null;
        String giftKey = protoObject.hasGiftKey() ? protoObject.getGiftKey().getValue() : null;

        return new SurveyGiftData(giftKey, survey);
    }

    @Override
    public SurveyGiftDataProto.SurveyGiftData map(SurveyGiftData commonObject) {
        if (commonObject == null) {
            return null;
        }
        
        SurveyGiftDataProto.SurveyGiftData.Builder builder = SurveyGiftDataProto.SurveyGiftData.newBuilder();
        
        Optional.ofNullable(codec.map(commonObject.getSurvey())).ifPresent(builder::setSurvey);
        Optional.ofNullable(commonObject.getGiftKey()).ifPresent(key -> {
            builder.setGiftKey(StringValue.of(key));
        });

        return builder.build();
    }
}
