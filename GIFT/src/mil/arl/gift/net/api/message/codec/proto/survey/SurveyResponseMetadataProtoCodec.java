/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto.survey;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.TimeZone;

import org.apache.commons.lang.time.FastDateFormat;

import com.google.protobuf.BoolValue;
import com.google.protobuf.Int32Value;
import com.google.protobuf.StringValue;

import generated.proto.common.survey.SurveyPageResponseMetadataProto;
import generated.proto.common.survey.SurveyResponseMetadataProto;
import mil.arl.gift.common.survey.Survey.SurveyTypeEnum;
import mil.arl.gift.common.survey.SurveyPageResponseMetadata;
import mil.arl.gift.common.survey.SurveyResponseMetadata;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for protobuf encoding/decoding the survey response
 * metadata instance.
 * 
 * @author sharrison
 */
public class SurveyResponseMetadataProtoCodec
        implements ProtoCodec<SurveyResponseMetadataProto.SurveyResponseMetadata, SurveyResponseMetadata> {

    private static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss", TimeZone.getDefault(),
            Locale.getDefault());

    private static SurveyPageResponseMetadataProtoCodec codec = new SurveyPageResponseMetadataProtoCodec();

    private static synchronized Date parseDate(String dateStr) throws ParseException {
        return df.parse(dateStr);
    }

    private static String getTimeStampAsString(Date timeStamp) {
        return timeStamp != null ? fdf.format(timeStamp) : null;
    }

    @Override
    public SurveyResponseMetadata convert(SurveyResponseMetadataProto.SurveyResponseMetadata protoObject) {
        if (protoObject == null) {
            return null;
        }

        SurveyResponseMetadata responseMetadata = new SurveyResponseMetadata();

        String startTimeString = protoObject.hasStartTime() ? protoObject.getStartTime().getValue() : null;
        String endTimeString = protoObject.hasEndTime() ? protoObject.getEndTime().getValue() : null;

        String giftKey = protoObject.hasGiftKey() ? protoObject.getGiftKey().getValue() : null;
        boolean hasIdealAnswer = protoObject.hasHasIdealAnswer() ? protoObject.getHasIdealAnswer().getValue() : false;
        int surveyContextId = protoObject.hasSurveyContextId() ? protoObject.getSurveyContextId().getValue() : 0;
        Date surveyStartTime = null;
        Date surveyEndTime = null;

        try {
            surveyStartTime = parseDate(startTimeString);
            surveyEndTime = parseDate(endTimeString);
        } catch (Exception e) {
            throw new MessageDecodeException(this.getClass().getName(), "Exception logged while decoding", e);
        }

        int surveyId = protoObject.hasSurveyId() ? protoObject.getSurveyId().getValue() : 0;
        String surveyName = protoObject.hasSurveyName() ? protoObject.getSurveyName().getValue() : null;
        int surveyResponseId = protoObject.hasSurveyResponseId() ? protoObject.getSurveyResponseId().getValue() : 0;

        if (protoObject.hasSurveyType()) {
            // legacy messages didn't have this field
            responseMetadata.setSurveyType(SurveyTypeEnum.valueOf(protoObject.getSurveyType().getValue()));
        }

        List<SurveyPageResponseMetadata> pageResponses = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(protoObject.getSurveyPageResponsesList())) {
            for (SurveyPageResponseMetadataProto.SurveyPageResponseMetadata responses : protoObject
                    .getSurveyPageResponsesList()) {
                pageResponses.add(codec.convert(responses));
            }
        }

        responseMetadata.setSurveyPageResponses(pageResponses);

        responseMetadata.setGiftKey(giftKey);
        responseMetadata.setHasIdealAnswer(hasIdealAnswer);
        responseMetadata.setSurveyContextId(surveyContextId);
        responseMetadata.setSurveyEndTime(surveyEndTime);
        responseMetadata.setSurveyId(surveyId);
        responseMetadata.setSurveyName(surveyName);
        responseMetadata.setSurveyResponseId(surveyResponseId);
        responseMetadata.setSurveyStartTime(surveyStartTime);

        return responseMetadata;
    }

    @Override
    public SurveyResponseMetadataProto.SurveyResponseMetadata map(SurveyResponseMetadata commonObject) {
        if (commonObject == null) {
            return null;
        }

        SurveyResponseMetadataProto.SurveyResponseMetadata.Builder builder = SurveyResponseMetadataProto.SurveyResponseMetadata
                .newBuilder();

        Optional.ofNullable(commonObject.getGiftKey()).ifPresent(giftKey -> {
            builder.setGiftKey(StringValue.of(giftKey));
        });
        
        builder.setHasIdealAnswer(BoolValue.of(commonObject.getHasIdealAnswer()));
        
        builder.setSurveyContextId(Int32Value.of(commonObject.getSurveyContextId()));
        
        
        Optional.ofNullable(getTimeStampAsString(commonObject.getSurveyStartTime())).ifPresent(surveyStartTime -> {
            builder.setStartTime(StringValue.of(surveyStartTime));
        });
        
        Optional.ofNullable(getTimeStampAsString(commonObject.getSurveyEndTime())).ifPresent(surveyEndTime -> {
            builder.setEndTime(StringValue.of(surveyEndTime));
        });
        
        builder.setSurveyId(Int32Value.of(commonObject.getSurveyId()));
        
        builder.setSurveyResponseId(Int32Value.of(commonObject.getSurveyResponseId()));
        
        Optional.ofNullable(commonObject.getSurveyName()).ifPresent(surveyName -> {
            builder.setSurveyName(StringValue.of(surveyName));
        });

        builder.setSurveyType(StringValue.of(commonObject.getSurveyType().name()));

        if (CollectionUtils.isNotEmpty(commonObject.getSurveyPageResponses())) {
            for (SurveyPageResponseMetadata response : commonObject.getSurveyPageResponses()) {
                Optional.ofNullable(codec.map(response)).ifPresent(builder::addSurveyPageResponses);
            }
        }

        return builder.build();
    }
}
