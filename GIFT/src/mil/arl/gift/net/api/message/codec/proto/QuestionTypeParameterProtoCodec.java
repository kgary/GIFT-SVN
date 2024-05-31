/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.Optional;

import com.google.protobuf.Int32Value;
import com.google.protobuf.StringValue;

import generated.proto.common.QuestionTypeParameterProto;
import mil.arl.gift.common.GetKnowledgeAssessmentSurveyRequest.ConceptParameters.QuestionTypeParameter;
import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf
 * QuestionTypeParameter.
 * 
 * @author cpolynice
 *
 */
public class QuestionTypeParameterProtoCodec
        implements ProtoCodec<QuestionTypeParameterProto.QuestionTypeParameter, QuestionTypeParameter> {

    /* Codec that will be used to convert to/from a protobuf abstract enum. */
    private static AbstractEnumObjectProtoCodec enumCodec = new AbstractEnumObjectProtoCodec();

    @Override
    public QuestionTypeParameter convert(QuestionTypeParameterProto.QuestionTypeParameter protoObject) {
        if (protoObject == null) {
            return null;
        }

        int numberOfQuestions = protoObject.hasNumberQuestions() ? protoObject.getNumberQuestions().getValue() : 0;
        String propertyValue = protoObject.hasPropertyValue() ? protoObject.getPropertyValue().getValue() : null;
        SurveyPropertyKeyEnum propEnum = protoObject.hasQuestionProp()
                ? (SurveyPropertyKeyEnum) enumCodec.convert(protoObject.getQuestionProp())
                : null;

        return new QuestionTypeParameter(propEnum, propertyValue, numberOfQuestions);
    }

    @Override
    public QuestionTypeParameterProto.QuestionTypeParameter map(QuestionTypeParameter commonObject) {
        if (commonObject == null) {
            return null;
        }
        
        QuestionTypeParameterProto.QuestionTypeParameter.Builder builder = QuestionTypeParameterProto.QuestionTypeParameter.newBuilder();

        builder.setNumberQuestions(Int32Value.of(commonObject.getNumberOfQuestions()));
        Optional.ofNullable(enumCodec.map(commonObject.getQuestionProperty())).ifPresent(builder::setQuestionProp);
        Optional.ofNullable(commonObject.getPropertyValue()).ifPresent(propVal -> {
            builder.setPropertyValue(StringValue.of(propVal));
        });

        return builder.build();
    }

}
