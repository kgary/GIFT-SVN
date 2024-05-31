/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.Date;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.DoubleValue;
import com.google.protobuf.StringValue;

import generated.proto.common.AbstractScaleProto;
import mil.arl.gift.common.enums.AbstractEnum;
import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;
import mil.arl.gift.common.state.ConceptStateRecord;
import mil.arl.gift.common.survey.score.AbstractScale;
import mil.arl.gift.common.survey.score.QuestionScale;
import mil.arl.gift.common.survey.score.SurveyScale;
import mil.arl.gift.common.util.ProtobufConversionUtil;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.api.message.MessageEncodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for protobuf encoding/decoding an Abstract Scale.
 * 
 * @author sharrison
 */
public class AbstractScaleProtoCodec implements ProtoCodec<AbstractScaleProto.AbstractScale, AbstractScale> {
    /** Logger for the class. */
    private static Logger logger = LoggerFactory.getLogger(AbstractScaleProtoCodec.class);

    /** Codec to convert {@link AbstractEnum} */
    private final AbstractEnumObjectProtoCodec abstractEnumCodec = new AbstractEnumObjectProtoCodec();

    @Override
    public AbstractScale convert(AbstractScaleProto.AbstractScale protoObject) {
        if (protoObject == null) {
            return null;
        }

        if (protoObject.hasSurveyScale()) {
            AbstractScaleProto.SurveyScale protoSuveyScale = protoObject.getSurveyScale();

            AbstractEnum value = protoSuveyScale.hasValue() ? abstractEnumCodec.convert(protoSuveyScale.getValue())
                    : null;
            LearnerStateAttributeNameEnum attribute = protoSuveyScale.hasAttribute()
                    ? (LearnerStateAttributeNameEnum) abstractEnumCodec.convert(protoSuveyScale.getAttribute())
                    : null;
            SurveyScale scale = new SurveyScale(attribute, value, protoSuveyScale.getRawValue().getValue());
            
            if (protoSuveyScale.hasTimeStamp()) {
                scale.setTimeStamp(
                        new Date(ProtobufConversionUtil.convertTimestampToMillis(protoSuveyScale.getTimeStamp())));
            }
            
            return scale;
        } else if (protoObject.hasQuestionScale()) {
            AbstractScaleProto.QuestionScale protoQuestionScale = protoObject.getQuestionScale();

            LearnerStateAttributeNameEnum attribute = protoQuestionScale.hasAttribute()
                    ? (LearnerStateAttributeNameEnum) abstractEnumCodec.convert(protoQuestionScale.getAttribute())
                    : null;

            QuestionScale scale = new QuestionScale(attribute, protoQuestionScale.getRawValue().getValue());

            if (protoQuestionScale.hasValue()) {
                AbstractEnum value = protoQuestionScale.hasValue()
                        ? abstractEnumCodec.convert(protoQuestionScale.getValue())
                        : null;
                if (value != null) {
                    scale.setValue(value);
                }
            }

            if (protoQuestionScale.hasTimeStamp()) {
                scale.setTimeStamp(
                        new Date(ProtobufConversionUtil.convertTimestampToMillis(protoQuestionScale.getTimeStamp())));
            }
            
            return scale;
        } else if (protoObject.hasConceptStateRecord()) {
            AbstractScaleProto.ConceptStateRecord protoConceptRecord = protoObject.getConceptStateRecord();

            ConceptStateRecord record = new ConceptStateRecord(
                    protoConceptRecord.hasConcept() ? protoConceptRecord.getConcept().getValue() : null,
                    (LearnerStateAttributeNameEnum) abstractEnumCodec.convert(protoConceptRecord.getAttribute()),
                    abstractEnumCodec.convert(protoConceptRecord.getValue()));
            
            if (protoConceptRecord.hasTimeStamp()) {
                record.setTimeStamp(
                        new Date(ProtobufConversionUtil.convertTimestampToMillis(protoConceptRecord.getTimeStamp())));
            }

            return record;
        } else {
            logger.error("Cannot convert a protobuf AbstractScale without a scale object.");
            throw new MessageDecodeException(this.getClass().getName(), "Exception logged while converting ");
        }
    }

    @Override
    public AbstractScaleProto.AbstractScale map(AbstractScale commonObject) {
        if (commonObject == null) {
            return null;
        }

        AbstractScaleProto.AbstractScale.Builder builder = AbstractScaleProto.AbstractScale.newBuilder();

        if (commonObject instanceof SurveyScale) {
            AbstractScaleProto.SurveyScale.Builder sScale = AbstractScaleProto.SurveyScale.newBuilder();
            Optional.ofNullable(ProtobufConversionUtil.convertDateToTimestamp(commonObject.getTimeStamp()))
                    .ifPresent(sScale::setTimeStamp);
            Optional.ofNullable(abstractEnumCodec.map(commonObject.getAttribute())).ifPresent(sScale::setAttribute);
            Optional.ofNullable(abstractEnumCodec.map(commonObject.getValue())).ifPresent(sScale::setValue);
            sScale.setRawValue(DoubleValue.of(commonObject.getRawValue()));
            builder.setSurveyScale(sScale);
        } else if (commonObject instanceof QuestionScale) {
            AbstractScaleProto.QuestionScale.Builder qScale = AbstractScaleProto.QuestionScale.newBuilder();
            Optional.ofNullable(ProtobufConversionUtil.convertDateToTimestamp(commonObject.getTimeStamp()))
                    .ifPresent(qScale::setTimeStamp);
            Optional.ofNullable(abstractEnumCodec.map(commonObject.getAttribute()))
                    .ifPresent(qScale::setAttribute);
            Optional.ofNullable(abstractEnumCodec.map(commonObject.getValue())).ifPresent(qScale::setValue);
            qScale.setRawValue(DoubleValue.of(commonObject.getRawValue()));
            builder.setQuestionScale(qScale);
        } else if (commonObject instanceof ConceptStateRecord) {
            AbstractScaleProto.ConceptStateRecord.Builder cRecord = AbstractScaleProto.ConceptStateRecord.newBuilder();
            Optional.ofNullable(((ConceptStateRecord) commonObject).getCourseConcept()).ifPresent(concept -> {
                cRecord.setConcept(StringValue.of(concept));
            });
            Optional.ofNullable(ProtobufConversionUtil.convertDateToTimestamp(commonObject.getTimeStamp()))
                    .ifPresent(cRecord::setTimeStamp);
            Optional.ofNullable(abstractEnumCodec.map(commonObject.getAttribute())).ifPresent(cRecord::setAttribute);
            Optional.ofNullable(abstractEnumCodec.map(commonObject.getValue())).ifPresent(cRecord::setValue);
            builder.setConceptStateRecord(cRecord);
        } else {
            throw new MessageEncodeException(this.getClass().getName(),
                    "Found unhandled scale type of " + commonObject);
        }

        return builder.build();
    }

}
