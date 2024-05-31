/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import generated.proto.common.survey.ConceptOverallDetailsProto;
import mil.arl.gift.common.survey.score.SurveyConceptAssessmentScore.ConceptOverallDetails;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.net.proto.ProtoCodec;

public class ConceptOverallDetailsProtoCodec
        implements ProtoCodec<ConceptOverallDetailsProto.ConceptOverallDetails, ConceptOverallDetails> {

    @Override
    public ConceptOverallDetails convert(ConceptOverallDetailsProto.ConceptOverallDetails protoObject) {
        if (protoObject == null) {
            return null;
        }

        List<Integer> correctQuestions = new ArrayList<>();
        List<Integer> incorrectQuestions = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(protoObject.getCorrectQuestionsList())) {
            correctQuestions = new ArrayList<>(protoObject.getCorrectQuestionsList());
        }

        if (CollectionUtils.isNotEmpty(protoObject.getIncorrectQuestionsList())) {
            incorrectQuestions = new ArrayList<>(protoObject.getIncorrectQuestionsList());
        }

        return new ConceptOverallDetails(new HashSet<>(correctQuestions), new HashSet<>(incorrectQuestions));
    }

    @Override
    public ConceptOverallDetailsProto.ConceptOverallDetails map(ConceptOverallDetails commonObject) {
        if (commonObject == null) {
            return null;
        }

        ConceptOverallDetailsProto.ConceptOverallDetails.Builder builder = ConceptOverallDetailsProto.ConceptOverallDetails
                .newBuilder();

        Optional.ofNullable(commonObject.getCorrectQuestions()).ifPresent(builder::addAllCorrectQuestions);
        Optional.ofNullable(commonObject.getIncorrectQuestions()).ifPresent(builder::addAllIncorrectQuestions);
        return builder.build();
    }
}
