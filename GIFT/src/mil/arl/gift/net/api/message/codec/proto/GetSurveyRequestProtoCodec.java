/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.google.protobuf.Int32Value;
import com.google.protobuf.StringValue;

import generated.proto.common.ConceptParametersProto;
import generated.proto.common.GetSurveyRequestProto;
import mil.arl.gift.common.GetKnowledgeAssessmentSurveyRequest;
import mil.arl.gift.common.GetKnowledgeAssessmentSurveyRequest.ConceptParameters;
import mil.arl.gift.common.GetSurveyRequest;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a protobuf GetSurveyRequest.
 * 
 * @author cpolynice
 *
 */
public class GetSurveyRequestProtoCodec
        implements ProtoCodec<GetSurveyRequestProto.GetSurveyRequest, GetSurveyRequest> {

    /* Codec that will be used to convert to/from a ConceptParameters
     * instance. */
    private static ConceptParametersProtoCodec codec = new ConceptParametersProtoCodec();

    /**
     * Converts the given protobuf map into the common object representation.
     * 
     * @param protoMap the protobuf map
     * @return the common map.
     */
    private static Map<String, ConceptParameters> convertConcepts(
            Map<String, ConceptParametersProto.ConceptParameters> protoMap) {
        if (protoMap == null) {
            return null;
        }

        Map<String, ConceptParameters> commonMap = new HashMap<>();

        for (Map.Entry<String, ConceptParametersProto.ConceptParameters> concept : protoMap.entrySet()) {
            String key = concept.getKey();
            ConceptParameters value = codec.convert(concept.getValue());

            if (value != null) {
                commonMap.put(key, value);
            }
        }

        return commonMap;
    }

    /**
     * Maps the given common object map to the protobuf representation.
     * 
     * @param commonMap the common object map
     * @return the protobuf map.
     */
    private static Map<String, ConceptParametersProto.ConceptParameters> mapConcepts(
            Map<String, ConceptParameters> commonMap) {
        if (commonMap == null) {
            return null;
        }

        Map<String, ConceptParametersProto.ConceptParameters> protoMap = new HashMap<>();

        for (Map.Entry<String, ConceptParameters> concept : commonMap.entrySet()) {
            String key = concept.getKey();
            ConceptParametersProto.ConceptParameters value = codec.map(concept.getValue());

            if (value != null) {
                protoMap.put(key, value);
            }
        }

        return protoMap;
    }

    @Override
    public GetSurveyRequest convert(GetSurveyRequestProto.GetSurveyRequest protoObject) {
        if (protoObject == null) {
            return null;
        }

        String giftKey = protoObject.hasGiftKey() ? protoObject.getGiftKey().getValue() : null;
        int surveyContextId = protoObject.hasSurveyContextId() ? protoObject.getSurveyContextId().getValue() : 0;

        if (CollectionUtils.isNotEmpty(protoObject.getConceptsMap())) {
            return new GetKnowledgeAssessmentSurveyRequest(surveyContextId,
                    convertConcepts(protoObject.getConceptsMap()));
        } else {
            return new GetSurveyRequest(surveyContextId, giftKey);
        }
    }

    @Override
    public GetSurveyRequestProto.GetSurveyRequest map(GetSurveyRequest commonObject) {
        if (commonObject == null) {
            return null;
        }
        
        GetSurveyRequestProto.GetSurveyRequest.Builder builder = GetSurveyRequestProto.GetSurveyRequest.newBuilder();

        builder.setSurveyContextId(Int32Value.of(commonObject.getSurveyContextId()));
        Optional.ofNullable(commonObject.getGiftKey()).ifPresent(key -> {
            builder.setGiftKey(StringValue.of(key));
        });

        if (commonObject instanceof GetKnowledgeAssessmentSurveyRequest) {
            Optional.ofNullable(mapConcepts(((GetKnowledgeAssessmentSurveyRequest) commonObject).getConcepts()))
                    .ifPresent(builder::putAllConcepts);
        }

        return builder.build();
    }
}
