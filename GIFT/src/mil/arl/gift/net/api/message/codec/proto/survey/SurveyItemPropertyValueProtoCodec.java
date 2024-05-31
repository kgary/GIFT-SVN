/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto.survey;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.StringValue;

import generated.proto.common.survey.SurveyItemPropertyValueProto;
import generated.proto.common.survey.SurveyItemPropertyValueProto.SurveyItemPropertyValue;
import mil.arl.gift.common.survey.FreeResponseReplyWeights;
import mil.arl.gift.common.survey.MatrixOfChoicesReplyWeights;
import mil.arl.gift.common.survey.OptionList;
import mil.arl.gift.common.survey.SliderRange;
import mil.arl.gift.common.survey.score.QuestionScorer;
import mil.arl.gift.common.survey.score.SurveyScorer;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.proto.ProtoCodec; 

/** 
 *  This class is responsible for protobuf encoding/decoding a Survey Item Property
 *  Value.
 *  @author cpolynice
 *  
 */
public class SurveyItemPropertyValueProtoCodec
        implements ProtoCodec<SurveyItemPropertyValueProto.SurveyItemPropertyValue, Serializable> {

    /** Logger for the class. */
    private static Logger logger = LoggerFactory.getLogger(SurveyItemPropertyValueProto.SurveyItemPropertyValue.class);

    /** Codecs that convert to/from protobuf representations of the classes. */
    private static OptionListProtoCodec optionListCodec = new OptionListProtoCodec();
    private static SurveyScorerProtoCodec surveyScorerCodec = new SurveyScorerProtoCodec();
    private static QuestionScorerProtoCodec questionScorerCodec = new QuestionScorerProtoCodec();
    private static SliderRangeProtoCodec sliderRangeCodec = new SliderRangeProtoCodec();
    private static MatrixOfChoicesReplyWeightsProtoCodec matrixReplyWeightsCodec = new MatrixOfChoicesReplyWeightsProtoCodec();
    private static FreeResponseReplyWeightsProtoCodec freeResponseReplyWeightsCodec = new FreeResponseReplyWeightsProtoCodec();
    private static SurveyItemPropertyValueListProtoCodec propValListCodec = new SurveyItemPropertyValueListProtoCodec();

    @Override
    public Serializable convert(SurveyItemPropertyValue protoObject) {
        if (protoObject == null) {
            return null;
        }
        
        try {
            if (protoObject.hasOptionList()) {
                 return optionListCodec.convert(protoObject.getOptionList());
            } else if (protoObject.hasStringPayload()) {
                 return protoObject.getStringPayload().getValue();
            } else if (protoObject.hasSurveyScorer()) {
                 return surveyScorerCodec.convert(protoObject.getSurveyScorer());
            } else if (protoObject.hasQuestionScorer()) {
                 return questionScorerCodec.convert(protoObject.getQuestionScorer());
            } else if (protoObject.hasSliderRange()) {
                 return sliderRangeCodec.convert(protoObject.getSliderRange());
            } else if (protoObject.hasMatrixOfChoicesReplyWeights()) {
                 return matrixReplyWeightsCodec.convert(protoObject.getMatrixOfChoicesReplyWeights());
            } else if (protoObject.hasFreeResponseReplyWeights()) {
                 return freeResponseReplyWeightsCodec.convert(protoObject.getFreeResponseReplyWeights());
            } else if (protoObject.hasSurveyItemPropertyValueList()) {
                return (Serializable) propValListCodec.convert(protoObject.getSurveyItemPropertyValueList());
            } else {
                 throw new MessageDecodeException(this.getClass().getName(), "No property value");
            }
        } catch (Exception e) {
            logger.error("Caught exception while creating a question property from " + protoObject, e);
            throw new MessageDecodeException(this.getClass().getName(), "Exception logged while decoding", e);
        }

    }

    @Override
    @SuppressWarnings("unchecked")
    public SurveyItemPropertyValue map(Serializable commonObject) {
        if (commonObject == null) {
            return null;
        }

        SurveyItemPropertyValueProto.SurveyItemPropertyValue.Builder builder = SurveyItemPropertyValueProto.SurveyItemPropertyValue
                .newBuilder();

        if (commonObject instanceof OptionList) {
            Optional.ofNullable(optionListCodec.map((OptionList) commonObject)).ifPresent(builder::setOptionList);
        } else if (commonObject instanceof String) {
            Optional.ofNullable((String) commonObject).ifPresent(str -> {
                builder.setStringPayload(StringValue.of(str));
            });
        } else if (commonObject instanceof SurveyScorer) {
            Optional.ofNullable(surveyScorerCodec.map((SurveyScorer) commonObject)).ifPresent(builder::setSurveyScorer);
        } else if (commonObject instanceof QuestionScorer) {
            Optional.ofNullable(questionScorerCodec.map((QuestionScorer) commonObject))
                    .ifPresent(builder::setQuestionScorer);
        } else if (commonObject instanceof SliderRange) {
            Optional.ofNullable(sliderRangeCodec.map((SliderRange) commonObject)).ifPresent(builder::setSliderRange);
        } else if (commonObject instanceof MatrixOfChoicesReplyWeights) {
            Optional.ofNullable(matrixReplyWeightsCodec.map((MatrixOfChoicesReplyWeights) commonObject))
                    .ifPresent(builder::setMatrixOfChoicesReplyWeights);
        } else if (commonObject instanceof FreeResponseReplyWeights) {
            Optional.ofNullable(freeResponseReplyWeightsCodec.map((FreeResponseReplyWeights) commonObject)).ifPresent(builder::setFreeResponseReplyWeights);
        } else if (commonObject instanceof List) {
            Optional.ofNullable(propValListCodec.map((List<Serializable>) commonObject))
                    .ifPresent(builder::setSurveyItemPropertyValueList);
        }

        else {
            throw new UnsupportedOperationException();
        }

        return builder.build();
    }

}
