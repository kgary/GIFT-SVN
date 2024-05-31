/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto.survey;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.map.CaseInsensitiveMap;

import generated.proto.common.survey.ConceptOverallDetailsProto;
import generated.proto.common.survey.SurveyConceptAssessmentScoreProto;
import mil.arl.gift.common.survey.score.SurveyConceptAssessmentScore;
import mil.arl.gift.common.survey.score.SurveyConceptAssessmentScore.ConceptOverallDetails;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.net.api.message.codec.proto.ConceptOverallDetailsProtoCodec;
import mil.arl.gift.net.proto.ProtoCodec;

public class SurveyConceptAssessmentScoreProtoCodec implements
        ProtoCodec<SurveyConceptAssessmentScoreProto.SurveyConceptAssessmentScore, SurveyConceptAssessmentScore> {

    private static ConceptOverallDetailsProtoCodec codec = new ConceptOverallDetailsProtoCodec();

    @SuppressWarnings("unchecked")
    @Override
    public SurveyConceptAssessmentScore convert(
            SurveyConceptAssessmentScoreProto.SurveyConceptAssessmentScore protoObject) {
        if (protoObject == null) {
            return null;
        }

        Map<String, ConceptOverallDetails> conceptDetailsMap = new HashMap<>();

        if (CollectionUtils.isNotEmpty(protoObject.getConceptDetailsMap())) {
            for (Map.Entry<String, ConceptOverallDetailsProto.ConceptOverallDetails> details : protoObject
                    .getConceptDetailsMap().entrySet()) {
                String key = StringUtils.isNotBlank(details.getKey()) ? details.getKey() : null;
                ConceptOverallDetails value = null;
                if (details.getValue() != null) {
                    value = codec.convert(details.getValue());
                }

                conceptDetailsMap.put(key, value);
            }
        }

        conceptDetailsMap = new CaseInsensitiveMap(conceptDetailsMap);
        return new SurveyConceptAssessmentScore(conceptDetailsMap);
    }

    @Override
    public SurveyConceptAssessmentScoreProto.SurveyConceptAssessmentScore map(
            SurveyConceptAssessmentScore commonObject) {
        if (commonObject == null) {
            return null;
        }
        
        SurveyConceptAssessmentScoreProto.SurveyConceptAssessmentScore.Builder builder = SurveyConceptAssessmentScoreProto.SurveyConceptAssessmentScore.newBuilder();
        
        if (commonObject.getConceptDetails() != null) {
            for (Map.Entry<String, ConceptOverallDetails> details : commonObject.getConceptDetails().entrySet()) {
                String key = StringUtils.isNotBlank(details.getKey()) ? details.getKey() : null;
                ConceptOverallDetailsProto.ConceptOverallDetails value = details.getValue() != null
                        ? codec.map(details.getValue())
                        : null;

                builder.putConceptDetails(key, value);
            }
        }

        return builder.build();
    }

}
