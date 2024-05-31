/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import generated.proto.common.AbstractPedagogicalRequestProto;
import mil.arl.gift.common.AbstractPedagogicalRequest;
import mil.arl.gift.common.RequestBranchAdaptation;
import mil.arl.gift.common.RequestDoNothingTactic;
import mil.arl.gift.common.RequestInstructionalIntervention;
import mil.arl.gift.common.RequestMidLessonMedia;
import mil.arl.gift.common.RequestPerformanceAssessment;
import mil.arl.gift.common.RequestScenarioAdaptation;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.api.message.MessageEncodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from an
 * AbstractPedagogicalRequest message.
 * 
 * @author cpolynice
 *
 */
public class AbstractPedagogicalRequestProtoCodec
        implements ProtoCodec<AbstractPedagogicalRequestProto.AbstractPedagogicalRequest, AbstractPedagogicalRequest> {

    /* Codec that will be used to convert to/from a
     * RequestInstructionalIntervention. */
    private static RequestInstructionalInterventionProtoCodec instructionalCodec = new RequestInstructionalInterventionProtoCodec();

    /* Codec that will be used to convert to/from a RequestMidLessonMedia. */
    private static RequestMidLessonMediaProtoCodec mediaCodec = new RequestMidLessonMediaProtoCodec();

    /* Codec that will be used to convert to/from a
     * RequestPerformanceAssessment. */
    private static RequestPerformanceAssessmentProtoCodec performanceCodec = new RequestPerformanceAssessmentProtoCodec();

    /* Codec that will be used to convert to/from a
     * RequestScenarioAdaptation. */
    private static RequestScenarioAdaptationProtoCodec scenarioCodec = new RequestScenarioAdaptationProtoCodec();

    /* Codec that will be used to convert to/from a RequestBranchAdaptation. */
    private static RequestBranchAdaptationProtoCodec branchCodec = new RequestBranchAdaptationProtoCodec();

    /* Codec that will be used to convert to/from a RequestDoNothingStrategy. */
    private static RequestDoNothingStrategyProtoCodec nothingCodec = new RequestDoNothingStrategyProtoCodec();

    @Override
    public AbstractPedagogicalRequest convert(AbstractPedagogicalRequestProto.AbstractPedagogicalRequest protoObject) {
        if (protoObject == null) {
            return null;
        }

        AbstractPedagogicalRequest request; 
        if (protoObject.hasRequestInstructionalIntervention()) {
            request = instructionalCodec.convert(protoObject.getRequestInstructionalIntervention());
        } else if (protoObject.hasRequestMidLessonMedia()) {
            request = mediaCodec.convert(protoObject.getRequestMidLessonMedia());
        } else if (protoObject.hasRequestPerformanceAssessment()) {
            request = performanceCodec.convert(protoObject.getRequestPerformanceAssessment());
        } else if (protoObject.hasRequestScenarioAdaptation()) {
            request = scenarioCodec.convert(protoObject.getRequestScenarioAdaptation());
        } else if (protoObject.hasRequestBranchAdaptation()) {
            request = branchCodec.convert(protoObject.getRequestBranchAdaptation());
        } else if (protoObject.hasRequestDoNothingStrategy()) {
            request = nothingCodec.convert(protoObject.getRequestDoNothingStrategy());
        } else {
            throw new MessageDecodeException(this.getClass().getName(), "Exception logged while converting ");
        }
        
        // optional = (new Feb 2022, #5174) - collection of DKF XML node (Task/concept) ids that caused this strategy to be requested.
        List<Integer> taskConceptIds = protoObject.getTaskConceptIdsList();
        if(CollectionUtils.isNotEmpty(taskConceptIds)) {
            Set<Integer> nodeIds = new HashSet<>(taskConceptIds);
            request.setTaskConceptsAppliedToo(nodeIds);
        }
        
        return request;
    }

    @Override
    public AbstractPedagogicalRequestProto.AbstractPedagogicalRequest map(AbstractPedagogicalRequest commonObject) {
        if (commonObject == null) {
            return null;
        }

        AbstractPedagogicalRequestProto.AbstractPedagogicalRequest.Builder builder = AbstractPedagogicalRequestProto.AbstractPedagogicalRequest
                .newBuilder();

        if (commonObject instanceof RequestInstructionalIntervention) {
            builder.setRequestInstructionalIntervention(
                    instructionalCodec.map((RequestInstructionalIntervention) commonObject));
        } else if (commonObject instanceof RequestMidLessonMedia) {
            builder.setRequestMidLessonMedia(mediaCodec.map((RequestMidLessonMedia) commonObject));
        } else if (commonObject instanceof RequestPerformanceAssessment) {
            builder.setRequestPerformanceAssessment(performanceCodec.map((RequestPerformanceAssessment) commonObject));
        } else if (commonObject instanceof RequestScenarioAdaptation) {
            builder.setRequestScenarioAdaptation(scenarioCodec.map((RequestScenarioAdaptation) commonObject));
        } else if (commonObject instanceof RequestBranchAdaptation) {
            builder.setRequestBranchAdaptation(branchCodec.map((RequestBranchAdaptation) commonObject));
        } else if (commonObject instanceof RequestDoNothingTactic) {
            builder.setRequestDoNothingStrategy(nothingCodec.map((RequestDoNothingTactic) commonObject));
        } else {
            throw new MessageEncodeException(this.getClass().getName(),
                    "Found unhandled ped request of " + commonObject);
        }
        
        // optional = (new Feb 2022, #5174) - collection of DKF XML node (Task/concept) ids that caused this strategy to be requested.
        if(CollectionUtils.isNotEmpty(commonObject.getTaskConceptsAppliedToo())) {
            builder.addAllTaskConceptIds(commonObject.getTaskConceptsAppliedToo());
        }

        return builder.build();
    }
}
