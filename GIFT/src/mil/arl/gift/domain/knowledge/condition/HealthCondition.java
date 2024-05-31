/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.condition;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generated.dkf.LearnerActionEnumType;
import mil.arl.gift.common.course.dkf.ConditionDescription;
import mil.arl.gift.common.course.dkf.ConditionDescription.FileDescription;
import mil.arl.gift.common.course.dkf.team.TeamMember;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.enums.DamageEnum;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.ta.state.EntityState;
import mil.arl.gift.net.api.message.Message;

/**
 * This condition checks whether team members are healthy.
 *
 * @author mhoffman
 *
 */
public class HealthCondition extends AbstractCondition {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(HealthCondition.class);

    /** the default assessment */
    private static final AssessmentLevelEnum DEFAULT_ASSESSMENT = AssessmentLevelEnum.AT_EXPECTATION;

    /**
     * contains the types of GIFT messages this condition needs in order to
     * provide assessments
     */
    private static final List<MessageTypeEnum> simulationInterests;

    static {
        simulationInterests = new ArrayList<MessageTypeEnum>();
        simulationInterests.add(MessageTypeEnum.ENTITY_STATE);
    }

    /**
     * contains the type of overall assessment scorers this condition can populate for an AAR
     */
    private static final Set<Class<?>> overallAssessmentTypes;
    static{
        overallAssessmentTypes = new HashSet<Class<?>>();
        overallAssessmentTypes.add(generated.dkf.Count.class);
        overallAssessmentTypes.add(generated.dkf.ViolationTime.class);
    }

    /** information about the purpose of this condition */
    private static final ConditionDescription DESCRIPTION =
            new FileDescription(new File("docs" + File.separator + "conditions" + File.separator + "Health.GIFT Domain condition description.html"), "Health");

    /**
     * Default constructor - required for authoring logic
     */
    public HealthCondition(){ }

    /**
     * Class constructor - set attributes from dkf content
     *
     * @param conditionInput - dkf content for this condition
     */
    public HealthCondition(generated.dkf.HealthConditionInput conditionInput) {

        //save any authored real time assessment rules
        if(conditionInput.getRealTimeAssessmentRules() != null){
            addRealTimeAssessmentRules(conditionInput.getRealTimeAssessmentRules());
        }

        if (conditionInput.getTeamMemberRefs() != null) {
            setTeamMembersBeingAssessed(conditionInput.getTeamMemberRefs());
        }

        AssessmentLevelEnum authoredLevel = getAuthoredRealTimeAssessment();
        if(authoredLevel != null){
            //set the initial assessment to the authored real time assessment value
            updateAssessment(authoredLevel);
        }else{
            updateAssessment(DEFAULT_ASSESSMENT);
        }
    }

    @Override
    public boolean handleTrainingAppGameState(Message message) {

        final int prevViolatorCount = getViolatorSize();
        if (message.getMessageType() == MessageTypeEnum.ENTITY_STATE) {

            EntityState entityState = (EntityState) message.getPayload();

            //only re-assess this condition if the entity state message describes the learner
            TeamMember<?> teamMember = isConditionAssessedTeamMember(entityState.getEntityID());
            if (teamMember == null) {
                return false;
            }

            AssessmentLevelEnum level = null;
            boolean isHealthy = entityState.getAppearance().getDamage() == DamageEnum.HEALTHY;
            if(isHealthy){
                boolean wasNotHealthyBefore = removeViolator(entityState.getEntityID());

                if(wasNotHealthyBefore){
                    // went from not healthy to healthy                    
                    level = handleSuccess(teamMember);
                    if(getViolatorSize() == 0){
                        // need to notify the assessment(s) tracking the group of team members 
                        // being assessed by this condition
                        handleSuccess();
                    }

                }
            }else{
                // team member is not healthy
                // If already a violator, this won't change anything
                addViolator(teamMember, entityState.getEntityID());

                // if already violating, this will just check if any time based assessments have
                // a new assessment to provide
                // otherwise the scoring event will start for this team member (and the ones tracking the group
                // of team members being assessed by this condition)
                level = handleViolation(teamMember);
            }

            if (level != null || prevViolatorCount != getViolatorSize()) {
                updateAssessment(level);
                return true;
            }

        }

        return false;
    }

    @Override
    protected void violatorUpdated(Set<TeamMember<?>> removedViolators){

        if(removedViolators != null && !removedViolators.isEmpty()){

            AssessmentLevelEnum level;
            if(getViolatorSize() == 0){
                //all known members have stopped
                level = handleSuccess(removedViolators.toArray(new TeamMember<?>[removedViolators.size()]));
            }else{
                //start the scoring event when the first member stops
                // Calling this repeatedly while the violation is happening will not
                // start another scoring event
                level = handleViolation(removedViolators.toArray(new TeamMember<?>[removedViolators.size()]));
            }

            if(level != null){
                updateAssessment(level);
                if(conditionActionInterface != null){
                    conditionActionInterface.conditionAssessmentCreated(this);
                }
            }
        }
    }

    /**
     * Handle the case where all of the team members are healthy.
     *
     * @param teamMembers the team members NOT violating this condition.  Use no value to update the group assessment
     * of team members being assessed by this condition instance.
     * @return an assessment level if the assessment has changed for this condition or null
     * if the assessment has not changed.
     */
    private AssessmentLevelEnum handleSuccess(TeamMember<?>... teamMembers){

        AssessmentLevelEnum level = null;

        //its ok to call this repeatedly w/o starting an event
        scoringEventEnded(teamMembers);

        AssessmentLevelEnum authoredLevel = getAuthoredRealTimeAssessment();
        if(authoredLevel != null){
            //one of the authored assessment rules has been satisfied

            if(getAssessment() != authoredLevel){
                //this is a new assessment, don't want to keep sending old, non-changed assessment
                level = authoredLevel;
            }
        }else if(getViolatorSize() == 0){
            //no authored assessment rules AND no current violators

            if(getAssessment() != AssessmentLevelEnum.AT_EXPECTATION){
                //found to be under the speed limit AND was not previously, therefore the level has changed

                level = AssessmentLevelEnum.AT_EXPECTATION;
            }
        }

        return level;
    }

    /**
     * Handle the case where one or more of the team members are not healthy at this moment.
     *
     * @param teamMembers the team members violating this condition.  
     * @return an assessment level if the assessment has changed for this condition or null
     * if the assessment has not changed.
     */
    private AssessmentLevelEnum handleViolation(TeamMember<?>... teamMembers){

        AssessmentLevelEnum level = null;

        if(isScoringEventActive(teamMembers)){
            //make sure the level hasn't changed due to an ongoing time of violation

            AssessmentLevelEnum authoredLevel = getAuthoredRealTimeAssessment();
            if(authoredLevel != null && getAssessment() != authoredLevel){
                //only set the level if the assessment is different than the current assessment to indicate
                //a new assessment has taken place and needs to be communicated throughout gift
                level = authoredLevel;
            }
        }else{
            //this is a new event, not a continuation of an ongoing event

            if(logger.isDebugEnabled()) {
                logger.debug("Violated health");
            }

            scoringEventStarted(teamMembers);

            AssessmentLevelEnum authoredLevel = getAuthoredRealTimeAssessment();
            if(authoredLevel != null){
                //one of the authored assessment rules has been satisfied
                level = authoredLevel;
            }else{
                if(getAssessment() != AssessmentLevelEnum.BELOW_EXPECTATION){
                    //not currently violating this condition, therefore treat this as a new violation
                    level = AssessmentLevelEnum.BELOW_EXPECTATION;
                }
            }
        }

        return level;
    }

    @Override
    public List<MessageTypeEnum> getSimulationInterests() {
        return simulationInterests;
    }

    @Override
    public ConditionDescription getDescription() {
        return DESCRIPTION;
    }

    @Override
    public Set<LearnerActionEnumType> getLearnerActionsNeeded() {
        return null;
    }

    @Override
    public Set<Class<?>> getOverallAssessmenTypes() {
        return overallAssessmentTypes;
    }

    @Override
    public boolean canComplete() {
        return false;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[HealthCondition: ");
        sb.append(super.toString());
        sb.append("]");

        return sb.toString();
    }
}
