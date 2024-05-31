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

import generated.dkf.LearnerAction;
import generated.dkf.ScenarioControls;
import generated.proto.common.DisplayLearnerActionsTutorRequestProto;
import generated.proto.common.LearnerActionProto;
import mil.arl.gift.common.DisplayLearnerActionsTutorRequest;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from a
 * DisplayLearnerActionsTutorRequest instance.
 * 
 * @author cpolynice
 *
 */
public class DisplayLearnerActionsTutorRequestProtoCodec implements
        ProtoCodec<DisplayLearnerActionsTutorRequestProto.DisplayLearnerActionsTutorRequest, DisplayLearnerActionsTutorRequest> {

    /* Codec that will be used to convert to/from a LearnerAction. */
    private static LearnerActionProtoCodec learnerCodec = new LearnerActionProtoCodec();

    /* Codec that will be used to convert to/from a ScenarioControls
     * instance. */
    private static ScenarioControlsProtoCodec scenarioCodec = new ScenarioControlsProtoCodec();

    /**
     * Converts the given protobuf list to the common object representation.
     * 
     * @param protoList the protobuf list
     * @return the common object list representation.
     */
    private static List<LearnerAction> convertList(List<LearnerActionProto.LearnerAction> protoList) {
        if (protoList == null) {
            return null;
        }

        List<LearnerAction> commonList = new ArrayList<>();

        for (LearnerActionProto.LearnerAction action : protoList) {
            commonList.add(learnerCodec.convert(action));
        }

        return commonList;
    }

    /**
     * Maps the given common object list to the protobuf representation.
     * 
     * @param commonList the common object list
     * @return the protobuf list representation.
     */
    private static List<LearnerActionProto.LearnerAction> mapList(List<LearnerAction> commonList) {
        if (commonList == null) {
            return null;
        }

        List<LearnerActionProto.LearnerAction> protoList = new ArrayList<>();

        for (LearnerAction action : commonList) {
            protoList.add(learnerCodec.map(action));
        }

        return protoList;
    }

    @Override
    public DisplayLearnerActionsTutorRequest convert(DisplayLearnerActionsTutorRequestProto.DisplayLearnerActionsTutorRequest protoObject) {
        if (protoObject == null) {
            return null;
        }
        
        List<LearnerAction> actionsList = CollectionUtils.isNotEmpty(protoObject.getActionsListList())
                ? convertList(protoObject.getActionsListList())
                : new ArrayList<>();
        ScenarioControls controls = protoObject.hasScenarioControls()
                ? scenarioCodec.convert(protoObject.getScenarioControls())
                : null;

        return new DisplayLearnerActionsTutorRequest(actionsList, controls);
    }

    @Override
    public DisplayLearnerActionsTutorRequestProto.DisplayLearnerActionsTutorRequest map(
            DisplayLearnerActionsTutorRequest commonObject) {
        if (commonObject == null) {
            return null;
        }

        DisplayLearnerActionsTutorRequestProto.DisplayLearnerActionsTutorRequest.Builder builder = DisplayLearnerActionsTutorRequestProto.DisplayLearnerActionsTutorRequest
                .newBuilder();

        Optional.ofNullable(mapList(commonObject.getActions())).ifPresent(builder::addAllActionsList);
        Optional.ofNullable(scenarioCodec.map(commonObject.getControls())).ifPresent(builder::setScenarioControls);

        return builder.build();
    }

}
