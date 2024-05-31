/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.condition;

import generated.dkf.LearnerActionEnumType;
import generated.dkf.StartLocation;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.vecmath.Point3d;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.coordinate.AbstractCoordinate;
import mil.arl.gift.common.coordinate.CoordinateUtil;
import mil.arl.gift.common.coordinate.GCC;
import mil.arl.gift.common.course.dkf.ConditionDescription;
import mil.arl.gift.common.course.dkf.ConditionDescription.FileDescription;
import mil.arl.gift.common.course.dkf.team.TeamMember;
import mil.arl.gift.common.course.dkf.team.TeamUtil;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.enums.DetonationResultEnum;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.ta.state.Detonation;
import mil.arl.gift.common.ta.state.EntityIdentifier;
import mil.arl.gift.common.ta.state.EntityState;
import mil.arl.gift.domain.DomainDKFHandler;
import mil.arl.gift.net.api.message.Message;

/**
 * This condition checks whether the lifeform target was hit correctly 
 * (e.g. 2 hit (double tap)  in the chest area, not missing) 
 * 
 * Assessments:
 *  - Below Expectation
 *      : shot fired doesn't hit an entity
 *      : shot fired doesn't hit an identified target
 *  - At Expectation
 *      : hit identified target two or more times     
 * 
 * @author mhoffman
 *
 */
public class LifeformTargetAccuracyCondition extends AbstractCondition {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(LifeformTargetAccuracyCondition.class);
    
    private static final AssessmentLevelEnum DEFAULT_ASSESSMENT = AssessmentLevelEnum.AT_EXPECTATION;
    
    private static final Long REASSESSMENT_TIME = 2500L;
    
    
    /** Cached point used when checking adding of entities. */
    private Point3d cachedPoint = new Point3d();
    
    /**
     * contains the types of GIFT messages this condition needs in order to provide assessments
     */
    private static final List<MessageTypeEnum> simulationInterests;    
    static{
        simulationInterests = new ArrayList<MessageTypeEnum>();
        simulationInterests.add(MessageTypeEnum.ENTITY_STATE);
        simulationInterests.add(MessageTypeEnum.DETONATION);
    }
    
    /**
     * contains the type of overall assessment scorers this condition can populate for an AAR
     */
    private static final Set<Class<?>> overallAssessmentTypes;
    static{
        overallAssessmentTypes = new HashSet<Class<?>>();
        overallAssessmentTypes.add(generated.dkf.Count.class);
    }
    
    /** information about the purpose of this condition */
    private static final ConditionDescription DESCRIPTION = new FileDescription(new File("docs" + File.separator + "conditions" + File.separator + "LifeformTargetAccuracy.GIFT Domain condition description.html"), "Shot Accuracy");

    /** collection of starting locations for entities to kill */
    private List<GCC> entityStartLocations;
    
    /** collection of entities still remaining to be killed - used to filter entity state messages */
    private List<EntityIdentifier> entitiesRemaining = new ArrayList<EntityIdentifier>();
    
    /**
     * collection of team member names from the team org that have yet to matched with 
     * an entity identifier.  Once identified the entity will be added to the entitiesRemaining collection.
     */
    private Set<String> missingEntities = new HashSet<String>();
    
    /** keeps track of the number of hits on each target */
    private Map<EntityIdentifier, AtomicInteger> entityToHitCount = new HashMap<EntityIdentifier, AtomicInteger>();
    
    /** flag used to indicate if at least one enemy was found */
    private boolean haveInit = false;
    
    /**
     * Default Constructor - required for authoring logic
     */
    public LifeformTargetAccuracyCondition(){
        super(DEFAULT_ASSESSMENT, REASSESSMENT_TIME);
    }
    
    /**
     * Class constructor - set attributes
     * 
     * @param entityStartLocations collection of starting locations for entities to kill 
     */
    @Deprecated
    public LifeformTargetAccuracyCondition(List<GCC> entityStartLocations){
        this();
        
        this.entityStartLocations = entityStartLocations;
        
        updateAssessment(DEFAULT_ASSESSMENT);
    }
    
    /**
     * Class constructor - set attributes from dkf content
     * 
     * @param lifeformAccuracy - dkf content for this condition
     */
    public LifeformTargetAccuracyCondition(generated.dkf.LifeformTargetAccuracyCondition lifeformAccuracy){
        this();
        
        if(lifeformAccuracy.getEntities() != null){
            this.entityStartLocations = new ArrayList<GCC>(lifeformAccuracy.getEntities().getStartLocation().size());
            for(StartLocation slocation : lifeformAccuracy.getEntities().getStartLocation()){
                AbstractCoordinate coord = DomainDKFHandler.buildCoordinate(slocation.getCoordinate());
                entityStartLocations.add(CoordinateUtil.getInstance().convertToGCC(coord));
            }
            
            if(lifeformAccuracy.getEntities().getTeamMemberRef() != null){
                for(String teamMemberRef : lifeformAccuracy.getEntities().getTeamMemberRef()){
                    // add to a list that will be manipulated during the life cycle of this condition
                    missingEntities.add(teamMemberRef);
                }
            }
        }
        
        // validation check
        if(missingEntities.isEmpty() && (entityStartLocations == null || entityStartLocations.isEmpty())){
            throw new IllegalArgumentException("No targets have been specified.");
        }
        
        //save any authored real time assessment rules
        if(lifeformAccuracy.getRealTimeAssessmentRules() != null){            
            addRealTimeAssessmentRules(lifeformAccuracy.getRealTimeAssessmentRules());
        }
        
        if(lifeformAccuracy.getTeamMemberRefs() != null){
            setTeamMembersBeingAssessed(lifeformAccuracy.getTeamMemberRefs());
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
        
        // really only interested in entity state messages
        if (message.getMessageType() == MessageTypeEnum.ENTITY_STATE) {

            EntityState entityState = (EntityState) message.getPayload();            

            // before doing anything check to make sure there are still hostiles
            // to kill
            if (haveInit && entitiesRemaining.isEmpty() && missingEntities.isEmpty()) {
                return false;
            }
            
            //check if the entity needs to be added to the list of remaining entities to kill
            checkAddEntity(entityState);

        } else if (message.getMessageType() == MessageTypeEnum.DETONATION) {

            Detonation dMessage = (Detonation) message.getPayload();

            // shot must be fired by learner
            TeamMember<?> teamMember = isConditionAssessedTeamMember(dMessage.getFiringEntityID());
            if(teamMember != null){              

                AssessmentLevelEnum level = null;

                // did the shot hit a lifeform
                if (dMessage.getDetonationResult() == DetonationResultEnum.ENTITY_IMPACT) {

                    // was it a hit on a known, alive, target?
                    if (entitiesRemaining.contains(dMessage.getTargetEntityID())) {

                        if(logger.isDebugEnabled()){
                            logger.debug("Learner fired shot hit an identified target");
                        }
                        
                        removeViolator(dMessage.getFiringEntityID());
                        
                        //check number of shots target has been hit by
                        int hitCnt = entityToHitCount.get(dMessage.getTargetEntityID()).incrementAndGet();
                        if(hitCnt >= 2){
                            //target has been hit by 2 or more rounds
                            
                            AssessmentLevelEnum authoredLevel = getAuthoredRealTimeAssessment();
                            if(authoredLevel == null){
                                //only change the level if there isn't an authored real time assessment rule
                                //... the idea is if there are authored rules and your are doing above, than at,
                                // than below over time that you shouldn't be able to go back to at expectation
                                // if you just hit a single target successfully.
                                level = AssessmentLevelEnum.AT_EXPECTATION;
                                
                                removeAllViolators(); // the condition is completed, 
                                                      // clear out violators in order to show positive assessment explanation   
                            }
                        }
                        
                    }else{
                        //shot hit a non-target entity
                        
                        if(logger.isDebugEnabled()){
                            logger.debug("Learner fired shot hit an unidentified/dead target");
                        }
                        
                        addViolator(teamMember, dMessage.getFiringEntityID());
                        
                        //score the bad shot
                        scoringEventStarted();
                        
                        AssessmentLevelEnum authoredLevel = getAuthoredRealTimeAssessment();
                        if(authoredLevel != null){
                            //one of the authored assessment rules has been satisfied
                            level = authoredLevel;                
                        }else{
                            level = AssessmentLevelEnum.BELOW_EXPECTATION;
                        }                        
                    }
                    
                }else{
                    //shot missed hitting any entity
                    
                    if(logger.isDebugEnabled()){
                        logger.debug("Learner fired shot missed lifeform");
                    }
                    
                    addViolator(teamMember, dMessage.getFiringEntityID());
                    
                    //score the bad shot
                    scoringEventStarted();
                    
                    AssessmentLevelEnum authoredLevel = getAuthoredRealTimeAssessment();
                    if(authoredLevel != null){
                        //one of the authored assessment rules has been satisfied
                        level = authoredLevel;                
                    }else{
                        level = AssessmentLevelEnum.BELOW_EXPECTATION;
                    }                    
                }
                
                // set an explanation for missing a target, eliminating all targets or no explanation for continuing to eliminate targets
                setAssessmentExplanation();     
                
                updateAssessment(level);
                return true;
            }

        }
        
        return false;
    }
    
    /**
     * Check whether the entity described by the entity state message provided is at/very-near one
     * of this condition's entity start locations.  If so, then add the entity id to the list of hostiles.
     * 
     * @param entityState The EntityState to check with.
     */
    private void checkAddEntity(EntityState entityState){

        if(entityStartLocations != null && !entityStartLocations.isEmpty()){
            boolean added = false;
            int i = 0;
            for(i = 0; i < entityStartLocations.size(); i++){
                
                GCC location = entityStartLocations.get(i);
                CoordinateUtil.getInstance().convertIntoPoint(location, cachedPoint);
                if(entityState.getLocation().distance(cachedPoint) < TeamUtil.START_RADIUS){
                    //found entity
                    
                    if(!entitiesRemaining.contains(entityState.getEntityID())){
                        //add new entity
                        entitiesRemaining.add(entityState.getEntityID());
                        entityToHitCount.put(entityState.getEntityID(), new AtomicInteger());
                        added = true;
                    }else{
                        logger.error("found an entity to be within more than one start location: "+entityState.getEntityID());
                    }
                    
                    break;
                }
            }
            
            if(added){
                
                if(logger.isDebugEnabled()){
                    logger.debug("removing entityStartLocation: "+entityStartLocations.get(i)+" which is now mapped to entity id "+entityState.getEntityID());
                }
                entityStartLocations.remove(i);
                haveInit = true;
            }
        }
        
        if(missingEntities != null && !missingEntities.isEmpty()){
            // there are still entities that haven't been identified
                
            TeamMember<?> teamMember =  getTeamMemberFromTeamOrg(entityState);
            if(teamMember != null && missingEntities.contains(teamMember.getName())){
                // found an entity, add it to the list of entities remaining to be analyzed
                entitiesRemaining.add(entityState.getEntityID());
                entityToHitCount.put(entityState.getEntityID(), new AtomicInteger());
                missingEntities.remove(teamMember.getName());
                
                if(logger.isDebugEnabled()){
                    logger.debug("removing missingEntity: "+teamMember.getName()+" which is now mapped to entity id "+entityState.getEntityID());
                }
                
                haveInit = true;
            }

        }
        
    }
    
    @Override
    public List<MessageTypeEnum> getSimulationInterests(){
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
     * Set the condition's assessment explanation based on the team members being assessed on this condition
     * and are currently violating the condition parameters.
     * @return true if the assessment explanation value for this condition changed during this method.
     */
    private boolean setAssessmentExplanation(){
        
        //update assessment explanation
        Set<TeamMember<?>> violators = buildViolatorsInfo();
        boolean changed = false;
        if(violators.isEmpty()){
            
            if(getAssessment() == AssessmentLevelEnum.AT_EXPECTATION && entitiesRemaining.isEmpty()){
                // all targets have been eliminated, show an explanation stating that
                String newAssessmentExplanation = "All designated target(s) have been eliminated";
                changed = !newAssessmentExplanation.equals(assessmentExplanation);
                assessmentExplanation = newAssessmentExplanation;
            }else{
                changed = assessmentExplanation != null;
                assessmentExplanation = null;
            }
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
            assessmentExplanationBuilder.append("} fired their weapon(s) and missed the designated target(s)");   
            
            String newAssessmentExplanation = assessmentExplanationBuilder.toString();
            changed = !newAssessmentExplanation.equals(assessmentExplanation);
            assessmentExplanation = newAssessmentExplanation;
        }
        
        return changed;
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
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[LifeformTargetAccuracyCondition: ");
        sb.append(super.toString());
        sb.append("]");
        
        return sb.toString();
    }

}
