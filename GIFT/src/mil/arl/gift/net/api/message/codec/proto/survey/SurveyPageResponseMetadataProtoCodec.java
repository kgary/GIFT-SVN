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

import com.google.protobuf.Int32Value;
import com.google.protobuf.StringValue;

import generated.proto.common.survey.AbstractQuestionResponseMetadataProto;
import generated.proto.common.survey.SurveyPageResponseMetadataProto;
import mil.arl.gift.common.survey.AbstractQuestionResponseMetadata;
import mil.arl.gift.common.survey.SurveyPageResponseMetadata;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.net.proto.ProtoCodec;

/** 
 *  This class is responsible for protobuf encoding/decoding a SurveyPageResponseMetadata 
 *  instance.
 * 
 *  @author cpolynice
 *  
 */
public class SurveyPageResponseMetadataProtoCodec
        implements ProtoCodec<SurveyPageResponseMetadataProto.SurveyPageResponseMetadata, SurveyPageResponseMetadata> {

    private static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss", TimeZone.getDefault(),
            Locale.getDefault());

    private static AbstractQuestionResponseMetadataProtoCodec codec = new AbstractQuestionResponseMetadataProtoCodec();

    private static synchronized Date parseDate(String dateStr) throws ParseException {
        return df.parse(dateStr);
    }

    private static String getTimeStampAsString(Date timeStamp) {
        return timeStamp != null ? fdf.format(timeStamp) : null;
    }

    @Override
    public SurveyPageResponseMetadata convert(SurveyPageResponseMetadataProto.SurveyPageResponseMetadata protoObject) {
        if (protoObject == null) {
            return null;
        }

        SurveyPageResponseMetadata pageResponseMetadata = new SurveyPageResponseMetadata();

        String startTimeString = protoObject.hasStartTime()
                ? protoObject.getStartTime().getValue()
                : null;
        String endTimeString = protoObject.hasEndTime()
                ? protoObject.getEndTime().getValue()
                : null;
        int surveyPageResponseId = protoObject.hasSurveyPageResponseId() ? protoObject.getSurveyPageResponseId().getValue() : 0;
        int surveyResponseId = protoObject.hasSurveyResponseId() ? protoObject.getSurveyResponseId().getValue() : 0;
        int surveyPageId = protoObject.hasSurveyPageId() ? protoObject.getSurveyPageId().getValue() : 0;
        List<AbstractQuestionResponseMetadata> questionList = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(protoObject.getQuestionResponseMetadataList())) {
            for (AbstractQuestionResponseMetadataProto.AbstractQuestionResponseMetadata metadata : protoObject
                    .getQuestionResponseMetadataList()) {
                questionList.add(codec.convert(metadata));
            }
        }

        try {
            pageResponseMetadata.setStartTime(startTimeString != null ? parseDate(startTimeString) : null);
            pageResponseMetadata.setEndTime(endTimeString != null ? parseDate(endTimeString) : null);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        pageResponseMetadata.setSurveyPageResponseId(surveyPageResponseId);
        pageResponseMetadata.setSurveyResponseId(surveyResponseId);
        pageResponseMetadata.setSurveyPageId(surveyPageId);
        pageResponseMetadata.setQuestionMetadata(questionList);

        return pageResponseMetadata;
    }

    @Override
    public SurveyPageResponseMetadataProto.SurveyPageResponseMetadata map(SurveyPageResponseMetadata commonObject) {
        if (commonObject == null) {
            return null;
        }
        
        SurveyPageResponseMetadataProto.SurveyPageResponseMetadata.Builder builder = SurveyPageResponseMetadataProto.SurveyPageResponseMetadata.newBuilder();
        
        Optional.ofNullable(commonObject.getSurveyPageResponseId()).ifPresent(surveyPageResponseId -> {
            builder.setSurveyPageResponseId(Int32Value.of(surveyPageResponseId));
        });
        
        Optional.ofNullable(commonObject.getSurveyResponseId()).ifPresent(surveyResponseId -> {
            builder.setSurveyResponseId(Int32Value.of(surveyResponseId));
        });
        
        Optional.ofNullable(commonObject.getSurveyPageId()).ifPresent(surveyPageId -> {
            builder.setSurveyPageId(Int32Value.of(surveyPageId));
        });
        
        Optional.ofNullable(getTimeStampAsString(commonObject.getStartTime())).ifPresent(startTime -> {
            builder.setStartTime(StringValue.of(startTime));
        });
        
        Optional.ofNullable(getTimeStampAsString(commonObject.getEndTime())).ifPresent(endTime -> {
            builder.setEndTime(StringValue.of(endTime));
        });

        if (CollectionUtils.isNotEmpty(commonObject.getQuestionResponses())) {
            for (AbstractQuestionResponseMetadata questionResponse : commonObject.getQuestionResponses()) {
                Optional.ofNullable(codec.map(questionResponse)).ifPresent(builder::addQuestionResponseMetadata);
            }
        }

        return builder.build();
    }
}
