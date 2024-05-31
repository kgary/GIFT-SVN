/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.protobuf.StringValue;

import generated.proto.common.ConceptParametersProto;
import generated.proto.common.QuestionTypeParameterProto;
import mil.arl.gift.common.GetKnowledgeAssessmentSurveyRequest.ConceptParameters;
import mil.arl.gift.common.GetKnowledgeAssessmentSurveyRequest.ConceptParameters.QuestionTypeParameter;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf
 * ConceptParameters.
 * 
 * @author cpolynice
 *
 */
public class ConceptParametersProtoCodec
        implements ProtoCodec<ConceptParametersProto.ConceptParameters, ConceptParameters> {

    /* Codec that will be used to convert to/from a QuestionTypeParameter. */
    private static QuestionTypeParameterProtoCodec codec = new QuestionTypeParameterProtoCodec();

    /**
     * Converts the given protobuf list into the common object representation.
     * 
     * @param protoList the protobuf list
     * @return the common object list.
     */
    private static List<QuestionTypeParameter> convertQuestionParams(
            List<QuestionTypeParameterProto.QuestionTypeParameter> protoList) {
        if (protoList == null) {
            return null;
        }

        List<QuestionTypeParameter> commonList = new ArrayList<>();

        for (QuestionTypeParameterProto.QuestionTypeParameter param : protoList) {
            commonList.add(codec.convert(param));
        }

        return commonList;
    }

    /**
     * Maps the given common object list to the protobuf list representation.
     * 
     * @param commonList the common object list
     * @return the protobuf list.
     */
    private static List<QuestionTypeParameterProto.QuestionTypeParameter> mapQuestionParams(
            List<QuestionTypeParameter> commonList) {
        if (commonList == null) {
            return null;
        }

        List<QuestionTypeParameterProto.QuestionTypeParameter> protoList = new ArrayList<>();

        for (QuestionTypeParameter param : commonList) {
            protoList.add(codec.map(param));
        }

        return protoList;
    }

    @Override
    public ConceptParameters convert(ConceptParametersProto.ConceptParameters protoObject) {
        if (protoObject == null) {
            return null;
        }

        String conceptName = protoObject.hasConceptName() ? protoObject.getConceptName().getValue() : null;
        List<QuestionTypeParameter> paramsObjList = CollectionUtils.isNotEmpty(protoObject.getQuestionParamsList())
                ? convertQuestionParams(protoObject.getQuestionParamsList())
                : null;
        ConceptParameters conceptParameters = new ConceptParameters(conceptName, paramsObjList);

        if (CollectionUtils.isNotEmpty(protoObject.getPreferQuestionsList())) {
            for (Integer qId : protoObject.getPreferQuestionsList()) {
                conceptParameters.addPreferredQuestion(qId);
            }
        }

        if (CollectionUtils.isNotEmpty(protoObject.getAvoidQuestionsList())) {
            for (Integer qId : protoObject.getAvoidQuestionsList()) {
                conceptParameters.addAvoidQuestion(qId);
            }
        }

        return conceptParameters;

    }

    @Override
    public ConceptParametersProto.ConceptParameters map(ConceptParameters commonObject) {
        if (commonObject == null) {
            return null;
        }

        ConceptParametersProto.ConceptParameters.Builder builder = ConceptParametersProto.ConceptParameters
                .newBuilder();

        Optional.ofNullable(commonObject.getPreferredQuestions()).ifPresent(builder::addAllPreferQuestions);
        Optional.ofNullable(commonObject.getAvoidQuestions()).ifPresent(builder::addAllAvoidQuestions);
        Optional.ofNullable(mapQuestionParams(commonObject.getQuestionParams()))
                .ifPresent(builder::addAllQuestionParams);
        Optional.ofNullable(commonObject.getConceptName()).ifPresent(name -> {
            builder.setConceptName(StringValue.of(name));
        });

        return builder.build();
    }

}
