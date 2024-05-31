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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimerTask;

import mil.arl.gift.common.course.dkf.ConditionDescription;
import mil.arl.gift.common.course.dkf.ConditionDescription.FileDescription;
import mil.arl.gift.common.course.dkf.team.TeamMember;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.ta.state.EntityState;
import mil.arl.gift.net.api.message.Message;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generated.dkf.LearnerActionEnumType;

/**
 * This condition checks the spacing between two objects.
 *
 * @author mhoffman
 *
 */
public class SpacingCondition extends AbstractCondition {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(SpacingCondition.class);

    /** the default assessment level for this condition */
    private static final AssessmentLevelEnum DEFAULT_ASSESSMENT = AssessmentLevelEnum.AT_EXPECTATION;
    
    /** name of the thread used to prevent thrashing of assessments from a spacing violator */
    private static final String TIMER_NAME = "Spacing violation timer";
    
    /**
     * amount of time in milliseconds between checking a entity's entity state
     * Also used for the amount of between assessment output of this condition if a time was 
     * not specified by the author for this condition. 
     */
    private static final Long DURATION_BETWEEN_CHECKS = 1000L;
    
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
            new FileDescription(new File("docs" + File.separator + "conditions" + File.separator + "Spacing.GIFT Domain condition description.html"), "Spacing");
    
    /** the authored configuration for this condition */
    private generated.dkf.SpacingCondition spacingConditionInput;
    
    /** how long in milliseconds the spacing must be violated before the assessment changes */
    private int minAssessmentDurationMS = 0;
    
    /**
     * the timer used when the spacing is actively being violated, need to know if the violation
     * has been sustained for a certain amount of time as provided by the author
     */
    private SchedulableTimer spacingViolationTimer = null;
       
    /** 
     * mapping of unique team member name to the information being tracked for assessment for this condition
     */
    private Map<String, AssessmentWrapper> teamMemberRefAssessmentMap = new HashMap<>();
    
    /**
     * Default constructor - required for authoring logic
     */
    public SpacingCondition(){ }

    /**
     * Class constructor - set attributes from dkf content
     *
     * @param spacingInput - dkf content for this condition
     */
    public SpacingCondition(generated.dkf.SpacingCondition spacingInput) {
        
        this.spacingConditionInput = spacingInput;
        minAssessmentDurationMS = spacingInput.getMinDurationBeforeViolation() != null ? spacingInput.getMinDurationBeforeViolation().intValue() * 1000 : DURATION_BETWEEN_CHECKS.intValue();
        
        //extract team member refs to store in condition's collection of team members
        setTeamMembersBeingAssessed(new generated.dkf.TeamMemberRefs());
        for(generated.dkf.SpacingCondition.SpacingPair space : spacingInput.getSpacingPair()){
            
            String entityA = space.getFirstObject().getTeamMemberRef();
            String entityB = space.getSecondObject().getTeamMemberRef();
            
            // can't be the same entity referenced 2x
            if(entityA.equalsIgnoreCase(entityB)){
                logger.warn("Skipping spacing condition rule that has the same team member ref '"+entityA+"' listed twice.");
                continue;
            }
            
            // need at least one sets of spacing authored
            if(space.getAcceptable() == null && space.getIdeal() == null){
                logger.warn("Skipping space condition rule for '"+entityA+"' and '"+entityB+"' because the ideal and acceptable limits were not authored.");
                continue;
            }
            
            // min and max spacing must be logical
            if(space.getIdeal() != null && 
                    space.getIdeal().getIdealMinSpacing().doubleValue() >= space.getIdeal().getIdealMaxSpacing().doubleValue()){
                logger.warn("Skipping space condition rule for '"+entityA+"' and '"+entityB+"' because the ideal min spacing "+
                        space.getIdeal().getIdealMinSpacing()+" is greater than the ideal max spacing "+space.getIdeal().getIdealMaxSpacing()+".");
                continue;
            }else if(space.getAcceptable() != null && 
                    space.getAcceptable().getAcceptableMinSpacing().doubleValue() >= space.getAcceptable().getAcceptableMaxSpacing().doubleValue()){
                logger.warn("Skipping space condition rule for '"+entityA+"' and '"+entityB+"' because the acceptable min spacing "+
                        space.getAcceptable().getAcceptableMinSpacing()+" is greater than the acceptable max spacing "+space.getAcceptable().getAcceptableMaxSpacing()+".");
                continue;
            }
            
            AssessmentWrapper assessmentWrapperEntityA = teamMemberRefAssessmentMap.get(entityA);
            if(assessmentWrapperEntityA == null){
                assessmentWrapperEntityA = new AssessmentWrapper(entityA);
                teamMemberRefAssessmentMap.put(entityA, assessmentWrapperEntityA);
            }
            
            assessmentWrapperEntityA.getSpacingPairs().add(space);
            
            AssessmentWrapper assessmentWrapperEntityB = teamMemberRefAssessmentMap.get(entityB);
            if(assessmentWrapperEntityB == null){
                assessmentWrapperEntityB = new AssessmentWrapper(entityB);
                teamMemberRefAssessmentMap.put(entityB, assessmentWrapperEntityB);
            }
            
            assessmentWrapperEntityB.getSpacingPairs().add(space);
        }
        
        getTeamMembersBeingAssessed().getTeamMemberRef().addAll(teamMemberRefAssessmentMap.keySet());

        updateAssessment(DEFAULT_ASSESSMENT);
    }

    @Override
    public boolean handleTrainingAppGameState(Message message) {

        if (message.getMessageType() == MessageTypeEnum.ENTITY_STATE) {

            EntityState entityState = (EntityState) message.getPayload();

            //only re-assess this condition if the entity state message describes the learner
            TeamMember<?> teamMember = isConditionAssessedTeamMember(entityState.getEntityID());
            if (teamMember == null) {
                return false;
            }            

            AssessmentWrapper assessmentWrapper = teamMemberRefAssessmentMap.get(teamMember.getName());
            if(assessmentWrapper == null){
                //ERROR - should have been created in constructor
                return false;
            }
            
            // update last received entity state
            assessmentWrapper.setEntityState(entityState);
                
            // check if this entity should be evaluated
            Long lastAssessmentCheck = assessmentWrapper.getLastAssessmentCheck();
            if(lastAssessmentCheck != null && System.currentTimeMillis() - lastAssessmentCheck < DURATION_BETWEEN_CHECKS){
                return false;
            }  
                
            if(spacingViolationTimer == null){
                // the timer is not running and the timer is needed in order to determine if the learner has violated the spacing
                // for a long enough time
                    
                spacingViolationTimer = new SchedulableTimer(TIMER_NAME);
                            
                // run assessment reporting logic twice as often as the time an assessment must be maintained
                spacingViolationTimer.scheduleAtFixedRate(new SpacingViolationTimeTask(), minAssessmentDurationMS / 2, minAssessmentDurationMS / 2);
                        
                if(logger.isDebugEnabled()) {
                    logger.debug("Started violating the spacing condition.  Starting spacing violation timer");
                }
            }
            
            // update the last time this entity was evaluated
            assessmentWrapper.updateLastAssessmentCheck();
                
            // evaluate and save the assessment for this entity
            AssessmentLevelEnum level = evaluateCondition(entityState, teamMember, null);
            if(level != null && level != assessmentWrapper.getAssessment()){
                // don't update the assessment time if the assessment hasn't changed
                assessmentWrapper.updateAssessment(level);
            }
                
            if(getViolatorSize() == 0){                    
                // there are no violators left, i.e. every team member is within the acceptable (or ideal) ranges
                scoringEventEnded();                    
            }
        }    

        return false;
    }
    
    /**
     * Set the condition's assessment explanation based on the team members being assessed on this condition
     * and are currently violating the condition parameters.
     * @return true if the assessment explanation value for this condition changed during this method.
     */
    private boolean setAssessmentExplanation(){
        
        //update assessment explanation
        Set<TeamMember<?>> violators = buildViolatorsInfo();
        boolean changed = false;
        if(violators.isEmpty()){
            changed = assessmentExplanation != null;
            assessmentExplanation = null;
        }else{
            StringBuilder assessmentExplanationBuilder = new StringBuilder();
            Iterator<TeamMember<?>> itr = violators.iterator();
            assessmentExplanationBuilder.append("{");
            while(itr.hasNext()){
                TeamMember<?> violator = itr.next();
                assessmentExplanationBuilder.append(violator.getName());
                if(itr.hasNext()){
                    assessmentExplanationBuilder.append(", ");
                }
            }
            assessmentExplanationBuilder.append("} are not properly spaced.");   
            String newAssessmentExplanation = assessmentExplanationBuilder.toString();
            changed = !newAssessmentExplanation.equals(assessmentExplanation);
            assessmentExplanation = newAssessmentExplanation;
        }
        
        return changed;
    }
    
    @Override
    protected void violatorUpdated(Set<TeamMember<?>> removedViolators){
        
        if(removedViolators != null && !removedViolators.isEmpty() && getViolatorSize() == 0){
            //no more violators
            
            //TODO implement cleanup here
//            for(EntityIdentifier eId : removedViolators){
//                lastAssessmentMap.remove(eId.get)
//            }
            
            // stop any pending spacing violation timer task from firing later
            if(spacingViolationTimer != null){
                spacingViolationTimer.cancel();
                spacingViolationTimer = null;
            }
            
            scoringEventEnded(); 
            
            //update assessment explanation
            setAssessmentExplanation(); 
            
            AssessmentLevelEnum level = DEFAULT_ASSESSMENT;
            updateAssessment(level);
            if(conditionActionInterface != null){
                conditionActionInterface.conditionAssessmentCreated(this);
            }
        }
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
    
    /**
     * Return whether this entity state is violating any spacing constraints for this condition.
     * 
     * @param entityState the entity state for a team member this condition is interest in.
     * @param teamMember the team member associated with this entity state
     * @return the current assessment for this condition.  Null if no assessment could be determined.
     */
    private AssessmentLevelEnum evaluateCondition(EntityState entityState, TeamMember<?> teamMember, Set<String> ignoreList){

        AssessmentWrapper assessmentWrapper = teamMemberRefAssessmentMap.get(teamMember.getName());
        if(assessmentWrapper == null){
            return null;
        }
        
        List<generated.dkf.SpacingCondition.SpacingPair> spaces = assessmentWrapper.getSpacingPairs();
        AssessmentLevelEnum assessment = null;
        if(spaces != null){
            
            for(generated.dkf.SpacingCondition.SpacingPair spacingPair : spaces){
                
                // find the other entity
                String otherTeamMemberRef;
                if(spacingPair.getFirstObject().getTeamMemberRef().equalsIgnoreCase(teamMember.getName())){
                    otherTeamMemberRef = spacingPair.getSecondObject().getTeamMemberRef();
                }else{
                    otherTeamMemberRef = spacingPair.getFirstObject().getTeamMemberRef();
                }
                
                AssessmentWrapper otherEntityAssessmentWrapper = teamMemberRefAssessmentMap.get(otherTeamMemberRef);
                if(otherEntityAssessmentWrapper == null){
                    continue;
                }
                
                EntityState otherEntityState = otherEntityAssessmentWrapper.getEntityState();
                if(otherEntityState == null){
                    //the other entity state hasn't been captured yet
                    continue;
                }else if(ignoreList != null && !ignoreList.isEmpty()){
                    // check if the other entity is on the ignore list, if so move onto the next
                    // spacing pair
                    
                    boolean onIgnoreList = false;
                    for(String teamMemberRef : ignoreList){
                        
                        if(teamMemberRef.equals(otherTeamMemberRef)){
                            onIgnoreList = true;
                            break;
                        }
                    }
                
                    if(onIgnoreList){
                        continue;
                    }
                }
                
                double distance = entityState.getLocation().distance(otherEntityState.getLocation());
                
                // Check ideal range first, acceptable range second
                // If already have an assessment, the assessment can't increase but only decrease, therefore
                // don't check ideal if already assessed at acceptable.
                if((assessment == null || assessment == AssessmentLevelEnum.ABOVE_EXPECTATION) &&
                        distance >= spacingPair.getIdeal().getIdealMinSpacing().doubleValue() && 
                        distance <= spacingPair.getIdeal().getIdealMaxSpacing().doubleValue()){
                    assessment = AssessmentLevelEnum.ABOVE_EXPECTATION;
                }else if(distance >= spacingPair.getAcceptable().getAcceptableMinSpacing().doubleValue() && 
                        distance <= spacingPair.getAcceptable().getAcceptableMaxSpacing().doubleValue()){
                    assessment = AssessmentLevelEnum.AT_EXPECTATION;
                }else if(distance < spacingPair.getAcceptable().getAcceptableMinSpacing().doubleValue() || 
                        distance > spacingPair.getAcceptable().getAcceptableMaxSpacing().doubleValue()){
                    assessment = AssessmentLevelEnum.BELOW_EXPECTATION;
                }

                if(assessment == AssessmentLevelEnum.BELOW_EXPECTATION){
                    // add the current entity as a violator
                    addViolator(teamMember, entityState.getEntityID());
                    
                    // add the other entity in this spacing pair as a violator
                    TeamMember<?> otherEntityTeamMember = getTeamMember(otherTeamMemberRef);
                    if(otherEntityTeamMember != null){
                        addViolator(otherEntityTeamMember, otherEntityState.getEntityID());
                    }
                }else{
                    // check if the other entity in this spacing pair is violating any constraints
                    // now that the current entity and the other entity are not violating this constraint
                    
                    TeamMember<?> otherEntityTeamMember = getTeamMember(otherTeamMemberRef);
                    if(otherEntityTeamMember != null){

                        // create list of team members checked thus far so they aren't
                        // checked again in the next method call
                        Set<String> updatedIgnores = new HashSet<>();
                        if(ignoreList != null){
                            updatedIgnores.addAll(ignoreList);
                        }
                        updatedIgnores.add(teamMember.getName());
                        AssessmentLevelEnum otherEntityAssessment = evaluateCondition(otherEntityState, otherEntityTeamMember, updatedIgnores);
                        if(otherEntityAssessment == null || otherEntityAssessment != AssessmentLevelEnum.BELOW_EXPECTATION){
                            // other entity assessment is not violating any of its spacing pairs either,
                            // remove other entity from violators list as well
                            removeViolator(otherEntityState.getEntityID());
                        }
        
                    }
                }
        
            }// end for
            
            if(assessment != null && assessment != AssessmentLevelEnum.BELOW_EXPECTATION){
                
                //determined that the current entity is not violating any spacing pairs
                removeViolator(entityState.getEntityID());
            }
        }// end if
        
        return assessment;
    }
    
    /**
     * Notify the parent concept to this condition that the condition has a new assessment 
     * outside the handle training app game state method call (i.e. because the violation timer task fired)
     */
    private void sendAsynchAssessmentNotification(){
        sendAssessmentEvent();
    }

    @Override
    public Set<Class<?>> getOverallAssessmenTypes() {
        return overallAssessmentTypes;
    }

    @Override
    public boolean canComplete() {
        return true;
    }
    
    @Override
    public void stop() {
        super.stop();
        
        if(spacingViolationTimer != null) {
            
            //clean up any timers that are still active when this condition is stopped
            spacingViolationTimer.cancel();
            spacingViolationTimer = null;
        }
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[SpacingCondition: ");
        sb.append(super.toString());
        sb.append(", minViolationDuration(msec) = ").append(minAssessmentDurationMS);
        sb.append(", spacingPairs = {");
        
        for(generated.dkf.SpacingCondition.SpacingPair spacingPair : spacingConditionInput.getSpacingPair()){
            sb.append("\n").append(spacingPair.getFirstObject().getTeamMemberRef()).append(" : ").append(spacingPair.getSecondObject().getTeamMemberRef());
            
            if(spacingPair.getIdeal() != null){
                sb.append(" ideal =").append(spacingPair.getIdeal().getIdealMinSpacing()).append(" to ").append(spacingPair.getIdeal().getIdealMaxSpacing());
            }
            
            if(spacingPair.getAcceptable() != null){
                sb.append(" acceptable =").append(spacingPair.getAcceptable().getAcceptableMinSpacing()).append(" to ").append(spacingPair.getAcceptable().getAcceptableMaxSpacing());
            }
              
            sb.append(",");
        }
        sb.append("}");
        sb.append("]");

        return sb.toString();
    }
    
    /**
     * Used to track per team member information for assessment purposes.
     * 
     * @author mhoffman
     *
     */
    private class AssessmentWrapper{
        
        /** last entity state update received for a team member */
        private EntityState entityState;
        
        /** epoch time for when the last assessment value was set */
        private Long lastAssessmentUpdate;
        
        /** the last assessment value calculated for this team member */
        private AssessmentLevelEnum assessment;
        
        /** the team member name for the team member being assessed */
        private String teamMemberRef;
        
        /** epoch time for when the last assessment check was performed */
        private Long lastAssessmentCheck;
        
        /** 
         * collection of different authored spacing parameters that mention that 
         * member (either as entity one or entity two).
         */
        private List<generated.dkf.SpacingCondition.SpacingPair> spacingPairs = new ArrayList<>();
        
        /**
         * Set attribute
         * 
         * @param teamMemberRef the team member name for the team member being assessed.  Can't be null or empty.
         */
        public AssessmentWrapper(String teamMemberRef){
            
            if(StringUtils.isEmpty(teamMemberRef)){
                throw new IllegalArgumentException("The team member ref is null or empty.");
            }
            
            this.teamMemberRef = teamMemberRef;
        }
        
        /**
         * Return collection of different authored spacing parameters that mention that 
         * member (either as entity one or entity two).
         * @return list will not be null but can be empty.
         */
        public List<generated.dkf.SpacingCondition.SpacingPair> getSpacingPairs(){
            return spacingPairs;
        }
        
        /**
         * Return the team member name for the team member being assessed
         * @return won't be null or empty.
         */
        public String getTeamMemberRef(){
            return teamMemberRef;
        }
        
        /**
         * Set the time at which the last assessment check was performed for this team member
         * to the current epoch time.
         */
        public void updateLastAssessmentCheck(){
            lastAssessmentCheck = System.currentTimeMillis();
        }
        
        /**
         * Return the epoch time for when the last assessment check was performed for this team member
         * @return can be null if {@link #updateLastAssessmentCheck()} was never called.
         */
        public Long getLastAssessmentCheck(){
            return lastAssessmentCheck;
        }
        
        /**
         * Set the latest entity state for this team member.
         * 
         * @param entityState can't be null
         */
        public void setEntityState(EntityState entityState){
            if(entityState == null){
                return;
            }
            
            this.entityState = entityState;
        }
        
        /**
         * Set the current assessment for this team member for this condition.
         * 
         * @param assessment can't be null.
         */
        public void updateAssessment(AssessmentLevelEnum assessment){
            if(assessment == null){
                return;
            }
            
            this.assessment = assessment;
            this.lastAssessmentUpdate = System.currentTimeMillis();
        }
        
        /**
         * Return the current assessment for this team member for this condition.
         * 
         * @return will be null if {@link #updateAssessment(AssessmentLevelEnum)} has not been called.
         */
        public AssessmentLevelEnum getAssessment(){
            return assessment;
        }
        
        /**
         * Return the epoch time at which the assessment for this team member for this condition
         * was last set.
         * @return will be null if {@link #updateAssessment(AssessmentLevelEnum)} has not been called.
         */
        public Long getLastAssessmentTime(){
            return lastAssessmentUpdate;
        }
        
        /**
         * Return the last entity state received for this team member.
         * @return will be null if {@link #setEntityState(EntityState)} was not called.
         */
        public EntityState getEntityState(){
            return entityState;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("[AssessmentWrapper: ");
            builder.append("lastAssessmentUpdate = ");
            builder.append(getLastAssessmentTime());
            builder.append(", assessment = ");
            builder.append(getAssessment());
            builder.append(", teamMemberRef = ");
            builder.append(getTeamMemberRef());
            builder.append(", lastAssessmentCheck = ");
            builder.append(getLastAssessmentCheck());
            builder.append(", entityState = ");
            builder.append(getEntityState());
            builder.append("]");
            return builder.toString();
        }        
        
    }
    
    /**
     * This class is the timer task which runs at the appropriately scheduled date.  It will
     * cause a change in assessment when the spacing constraints have been violated long enough as set by the author.
     * 
     * @author mhoffman
     *
     */
    private class SpacingViolationTimeTask extends TimerTask{
        
        @Override
        public void run() {
            
            if(logger.isInfoEnabled()){
                logger.info("spacing violation timer task fired.");
            }
            
            AssessmentLevelEnum level = null;
            synchronized(teamMemberRefAssessmentMap){
                
                long now = System.currentTimeMillis();
                
                // check the assessment map for the lowest expectation result
                for(AssessmentWrapper assessmentWrapper : teamMemberRefAssessmentMap.values()){
                    
                    AssessmentLevelEnum assessment = assessmentWrapper.getAssessment();
                    if(assessment == null){
                        continue;
                    }
                    
                    Long assessmentTime = assessmentWrapper.getLastAssessmentTime();
                    if(now - assessmentTime >= minAssessmentDurationMS){
                    
                    if(assessment == AssessmentLevelEnum.BELOW_EXPECTATION){
                        // at any point, this is the lowest assessment possible
                        level = assessment;
                        break;
                    }else if(level == null){
                        level = assessment;
                    }else if(level == AssessmentLevelEnum.ABOVE_EXPECTATION && assessment == AssessmentLevelEnum.AT_EXPECTATION){
                        // set to lower assessment
                        level = assessment;
                    }
                }
                }
            }
            
            if(level == null){
                //unable to determine, could be because no assessment has been taking place long enough
                return;
            }
            
            //update assessment explanation
            boolean assessmentExplanationChanged = setAssessmentExplanation(); 
            
            if(level != getAssessment()) {
                if(logger.isDebugEnabled()){                    
                    logger.debug("changing assessment level to "+level+" from "+getAssessment()+" with explanation of '"+getAssessmentExplanation()+"' for "+this);
                }
                if(getViolatorSize() > 0){ 
                    // the violation changed and there is at least one violator, means
                    // a new event occurred
                    scoringEventStarted();
                }
                updateAssessment(level);
                sendAsynchAssessmentNotification();
            }else if(assessmentExplanationChanged){
                sendAsynchAssessmentNotification();
            }
            
            // there are no violators, make sure any started/running scoring event has ended
            if(getViolatorSize() == 0){                                    
                scoringEventEnded();                    
            }
            
            if(spacingViolationTimer != null) {
                spacingViolationTimer.cancel();
                spacingViolationTimer = null;
            }
        }
        
        @Override
        public String toString(){
            
            StringBuilder sb = new StringBuilder("[SpacingViolationTimeTask: ");
            sb.append(" team members = {");
            for(AssessmentWrapper assessmentWrapper : teamMemberRefAssessmentMap.values()){
                sb.append("(").append(assessmentWrapper.getTeamMemberRef()).append(" : ").append(assessmentWrapper.getAssessment()).append("), ");
        }
            sb.append("}");
            sb.append("]");
            
            return sb.toString();
    }
}
}
