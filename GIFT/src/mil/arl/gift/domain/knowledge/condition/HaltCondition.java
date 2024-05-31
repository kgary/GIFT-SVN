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
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.ta.state.EntityState;
import mil.arl.gift.common.ta.state.EntityType;
import mil.arl.gift.net.api.message.Message;

/**
 * This condition checks whether the team is halting within a time window.
 *
 * @author mhoffman
 *
 */
public class HaltCondition extends AbstractCondition {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(HaltCondition.class);

    /** the default assessment */
    private static final AssessmentLevelEnum DEFAULT_ASSESSMENT = AssessmentLevelEnum.UNKNOWN;

    /** zero speed value used for halt speed check */
    private static final double ZERO = 0.0;

    /** m/s speed value used for halt speed check of platform entities */
    private static final double PLATFORM_STOPPED = 1.0;

    /** m/s speed value used for halt speed check of lifeform entities */
    private static final double LIFEFORM_STOPPED = 0.5;

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
            new FileDescription(new File("docs" + File.separator + "conditions" + File.separator + "Halt.GIFT Domain condition description.html"), "Halt");

    /**
     * Default constructor - required for authoring logic
     */
    public HaltCondition(){ }

    /**
     * Class constructor - set attributes from dkf content
     *
     * @param haltConditionInput - dkf content for this condition
     */
    public HaltCondition(generated.dkf.HaltConditionInput haltConditionInput) {

        //save any authored real time assessment rules
        if(haltConditionInput.getRealTimeAssessmentRules() != null){
            addRealTimeAssessmentRules(haltConditionInput.getRealTimeAssessmentRules());
        }

        if (haltConditionInput.getTeamMemberRefs() != null) {
            setTeamMembersBeingAssessed(haltConditionInput.getTeamMemberRefs());
        }

        AssessmentLevelEnum authoredLevel = getAuthoredRealTimeAssessment();
        if(authoredLevel != null){
            //set the initial assessment to the authored real time assessment value
            updateAssessment(authoredLevel);
        }else{
            updateAssessment(DEFAULT_ASSESSMENT);
        }
    }

    /**
     * Return the max speed (m/s) used to determine if an entity of a certain
     * type has stopped or not.<br/>
     * Note: this was mainly created because VBS platforms don't always report 0 m/s linear
     * velocity.
     *
     * @param entityType contains the type of entity needing a max speed value to be returned.
     * @return the max speed a entity can have based on entity type to determine if the entity
     * is close enough to stopped.
     */
    private double getEntityTypeStoppedSpeed(EntityType entityType){

        if(entityType == null){
            return ZERO;
        }

        int kind = entityType.getEntityKind();
        if(kind == EntityType.ENTITY_TYPE_PLATFORM){
            return PLATFORM_STOPPED;
        }else if(kind == EntityType.ENTITY_TYPE_LIFEFORM){
            return LIFEFORM_STOPPED;
        }else{
            return ZERO;
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
            boolean isStopped = entityState.getLinearVelocity().length() <= getEntityTypeStoppedSpeed(entityState.getEntityType());
            if(isStopped){
                //this entity is stopped
                boolean wasMoving = removeViolator(entityState.getEntityID());

                if(wasMoving){
                    //this entity was moving and is now stopped
                    level = handleSuccess(teamMember);

                    if(getViolatorSize() == 0){
                        // all known members have stopped
                        // need to notify the assessment(s) tracking the group of team members 
                        // being assessed by this condition
                        handleSuccess();
                    }else{
                        // start the scoring event when the first member stops to calculate the time it takes to stop
                        // and use that in the assessment calculation
                        // Calling this repeatedly while the violation is happening will not
                        // start another scoring event
                        level = handleViolation();
                    }
                }


            }else{
                // this entity is moving, collect all assessed members as violators right now even
                // know they aren't violating anything.  See 'getViolatorSize() == 0' above.
                // calling this repeatedly on the same entity will not do anything
                addViolator(teamMember, entityState.getEntityID());

                if(isScoringEventActive()){
                    // the group scoring event that is tracking all team members being assessed in this condition
                    // instance is active (see 'handleViolation' call above) and the group is in the act of halting, 
                    // but this member is still moving.  Add scoring event information.
                    // Calling this repeatedly w/o a handleSuccess call will not cause an increase in scoring 
                    // count value for this member.
                    level = handleViolation(teamMember);
                }
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
     * Handle the case where the learner is below the speed limit.
     *
     * @param teamMembersNotViolating The {@link TeamMember<?>}s who are
     * not violating this condition. Use no value to update the group assessment
     * of team members being assessed by this condition instance.
     * @return an assessment level if the assessment has changed for this condition or null
     * if the assessment has not changed.
     */
    private AssessmentLevelEnum handleSuccess(TeamMember<?>...teamMembersNotViolating){

        AssessmentLevelEnum level = null;

        //its ok to call this repeatedly w/o starting an event
        scoringEventEnded(teamMembersNotViolating);

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
     * Handle the case where the learner is NOT obeying the speed limit at this moment.
     *
     * @param violatingTeamMembers The {@link TeamMember<?>}s who are
     * violating this condition. Can be empty.
     * @return an assessment level if the assessment has changed for this condition or null
     * if the assessment has not changed.
     */
    private AssessmentLevelEnum handleViolation(TeamMember<?>...violatingTeamMembers){

        AssessmentLevelEnum level = null;

        if(isScoringEventActive(violatingTeamMembers)){
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
                logger.debug("Violated halt");
            }

            scoringEventStarted(violatingTeamMembers);

            AssessmentLevelEnum authoredLevel = getAuthoredRealTimeAssessment();
            if(authoredLevel != null){
                //one of the authored assessment rules has been satisfied
                level = authoredLevel;
            }else if(getViolatorSize() == 0){
                //no authored assessment rules AND no current violators
                
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
        sb.append("[HaltCondition: ");
        sb.append(super.toString());
        sb.append("]");

        return sb.toString();
    }
}
