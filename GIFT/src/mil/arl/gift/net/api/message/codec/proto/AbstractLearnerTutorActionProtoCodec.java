/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.message.codec.proto;

import generated.proto.common.AbstractLearnerTutorActionProto;
import mil.arl.gift.common.AbstractLearnerTutorAction;
import mil.arl.gift.common.AbstractReport;
import mil.arl.gift.common.ApplyStrategyLearnerAction;
import mil.arl.gift.common.AssessMyLocationTutorAction;
import mil.arl.gift.common.FinishScenario;
import mil.arl.gift.common.PaceCountEnded;
import mil.arl.gift.common.PaceCountStarted;
import mil.arl.gift.common.RadioUsed;
import mil.arl.gift.common.TutorMeLearnerTutorAction;
import mil.arl.gift.net.api.message.MessageDecodeException;
import mil.arl.gift.net.api.message.MessageEncodeException;
import mil.arl.gift.net.proto.ProtoCodec;

/**
 * This class is responsible for converting to/from an Abstract Learner Tutor
 * Action message.
 * 
 * @author cpolynice
 *
 */
public class AbstractLearnerTutorActionProtoCodec
        implements ProtoCodec<AbstractLearnerTutorActionProto.AbstractLearnerTutorAction, AbstractLearnerTutorAction> {

    /* Codec that will be used to convert to/from a RadioUsed instance. */
    private static RadioUsedProtoCodec radioCodec = new RadioUsedProtoCodec();

    /* Codec that will be used to convert to/from a FinishScenario instance. */
    private static FinishScenarioProtoCodec finishCodec = new FinishScenarioProtoCodec();

    /* Codec that will be used to convert to/from a PaceCountStarted
     * instance. */
    private static PaceCountStartedProtoCodec startCodec = new PaceCountStartedProtoCodec();

    /* Codec that will be used to convert to/from a PaceCountEnded instance. */
    private static PaceCountEndedProtoCodec endCodec = new PaceCountEndedProtoCodec();

    /* Codec that will be used to convert to/from an AbstractReport instance. */
    private static AbstractReportProtoCodec abstractCodec = new AbstractReportProtoCodec();

    /* Codec that will be used to convert to/from a TutorMeLearnerTutorAction
     * instance. */
    private static TutorMeLearnerTutorActionProtoCodec tutorCodec = new TutorMeLearnerTutorActionProtoCodec();

    /* Codec that will be used to convert to/from a AssessMyLocationTutorAction
     * instance. */
    private static AssessMyLocationTutorActionProtoCodec assessCodec = new AssessMyLocationTutorActionProtoCodec();

    /* Codec that will be used to convert to/from a ApplyStrategyLearnerAction
     * instance. */
    private static ApplyStrategyLearnerActionProtoCodec applyCodec = new ApplyStrategyLearnerActionProtoCodec();

    @Override
    public AbstractLearnerTutorAction convert(AbstractLearnerTutorActionProto.AbstractLearnerTutorAction protoObject) {
        if (protoObject == null) {
            return null;
        }

        if (protoObject.hasRadioUsed()) {
            return radioCodec.convert(protoObject.getRadioUsed());
        } else if (protoObject.hasFinishScenario()) {
            return finishCodec.convert(protoObject.getFinishScenario());
        } else if (protoObject.hasPaceCountStarted()) {
            return startCodec.convert(protoObject.getPaceCountStarted());
        } else if (protoObject.hasPaceCountEnded()) {
            return endCodec.convert(protoObject.getPaceCountEnded());
        } else if (protoObject.hasAbstractReport()) {
            return abstractCodec.convert(protoObject.getAbstractReport());
        } else if (protoObject.hasTutorMeLearnerTutorAction()) {
            return tutorCodec.convert(protoObject.getTutorMeLearnerTutorAction());
        } else if (protoObject.hasAssessMyLocationTutorAction()) {
            return assessCodec.convert(protoObject.getAssessMyLocationTutorAction());
        } else if (protoObject.hasApplyStrategyLearnerAction()) {
            return applyCodec.convert(protoObject.getApplyStrategyLearnerAction());
        } else {
            throw new MessageDecodeException(this.getClass().getName(), "Exception logged while converting ");
        }
    }

    @Override
    public AbstractLearnerTutorActionProto.AbstractLearnerTutorAction map(AbstractLearnerTutorAction commonObject) {
        if (commonObject == null) {
            return null;
        }
        
        AbstractLearnerTutorActionProto.AbstractLearnerTutorAction.Builder builder = AbstractLearnerTutorActionProto.AbstractLearnerTutorAction.newBuilder();
        
        if (commonObject instanceof RadioUsed) {
            builder.setRadioUsed(radioCodec.map((RadioUsed) commonObject));
        } else if (commonObject instanceof FinishScenario) {
            builder.setFinishScenario(finishCodec.map((FinishScenario) commonObject));
        } else if (commonObject instanceof PaceCountStarted) {
            builder.setPaceCountStarted(startCodec.map((PaceCountStarted) commonObject));
        } else if (commonObject instanceof PaceCountEnded) {
            builder.setPaceCountEnded(endCodec.map((PaceCountEnded) commonObject));
        } else if (commonObject instanceof AbstractReport) {
            builder.setAbstractReport(abstractCodec.map((AbstractReport) commonObject));
        } else if (commonObject instanceof TutorMeLearnerTutorAction) {
            builder.setTutorMeLearnerTutorAction(tutorCodec.map((TutorMeLearnerTutorAction) commonObject));
        } else if (commonObject instanceof AssessMyLocationTutorAction) {
            builder.setAssessMyLocationTutorAction(assessCodec.map((AssessMyLocationTutorAction) commonObject));
        } else if (commonObject instanceof ApplyStrategyLearnerAction) {
            builder.setApplyStrategyLearnerAction(applyCodec.map((ApplyStrategyLearnerAction) commonObject));
        } else {
            throw new MessageEncodeException(this.getClass().getName(),
                    "Found unhandled learner tutor action of " + commonObject);
        }

        return builder.build();
    }

}
