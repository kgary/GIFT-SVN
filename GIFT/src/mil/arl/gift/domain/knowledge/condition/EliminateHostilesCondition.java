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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import mil.arl.gift.common.enums.DamageEnum;
import mil.arl.gift.common.enums.DetonationResultEnum;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.ta.state.Detonation;
import mil.arl.gift.common.ta.state.EntityIdentifier;
import mil.arl.gift.common.ta.state.EntityState;
import mil.arl.gift.domain.DomainDKFHandler;
import mil.arl.gift.domain.knowledge.VariablesHandler.AbstractAssessmentActor;
import mil.arl.gift.domain.knowledge.VariablesHandler.ActorVariables;
import mil.arl.gift.domain.knowledge.VariablesHandler.TeamMemberActor;
import mil.arl.gift.net.api.message.Message;

/**
 * This condition checks whether enemy have been killed 
 * 
 * @author mhoffman
 *
 */
public class EliminateHostilesCondition extends AbstractCondition {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(EliminateHostilesCondition.class);
    
    private static final AssessmentLevelEnum DEFAULT_ASSESSMENT = AssessmentLevelEnum.BELOW_EXPECTATION;
    
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
        overallAssessmentTypes.add(generated.dkf.ViolationTime.class);
        overallAssessmentTypes.add(generated.dkf.CompletionTime.class);
    }
    
    /** information about the purpose of this condition */
    private static final ConditionDescription DESCRIPTION = new FileDescription(new File("docs" + File.separator + "conditions" + File.separator + "EliminateHostiles.GIFT Domain condition description.html"), "Eliminate Hostiles");
    
    /** 
     * collection of starting locations for entities to kill 
     * As entity ids are determined for targets at these locations this list will become smaller until the list is empty.
     * CAN BE NULL.
     */
    private List<GCC> entityStartLocations;
    
    /** 
     * collection of entities still remaining to be killed 
     * As enemy are killed they are removed from this list until the list is empty.
     */
    private List<EntityIdentifier> entitiesRemaining = new ArrayList<EntityIdentifier>();
    
    /**
     * collection of team member names from the team org that have yet to matched with 
     * an entity identifier.  Once identified the entity will be added to the entitiesRemaining collection.
     */
    private Set<String> missingEntities = new HashSet<String>();
    
    /**
     * the initialized count of targets to eliminate
     */
    private int originalTargetSize = 0;
    
    /** flag used to indicate if at least one enemy was found */
    private boolean haveInit = false;
    
    /** Point used to check the distance when adding entities. */
    private Point3d cachedPoint = new Point3d();
    
    /**
     * Default constructor - required for authoring logic
     */
    public EliminateHostilesCondition(){ }

    /**
     * Class constructor - set attributes
     * 
     * @param entityStartLocations - collection of points representing the start locations of the enemy to eliminate.
     * Shouldn't be null or empty.
     */
    public EliminateHostilesCondition(List<GCC> entityStartLocations){   
        
        this.entityStartLocations = entityStartLocations;
        originalTargetSize = entityStartLocations.size();
        
        updateAssessment(DEFAULT_ASSESSMENT);
    } 
    
    /**
     * Class constructor - set attributes from the dkf content
     * 
     * @param eliminateHostiles - dkf content for this condition.  Shouldn't be null.
     */
    public EliminateHostilesCondition(generated.dkf.EliminateHostilesCondition eliminateHostiles){   
        
        if(eliminateHostiles.getEntities() != null){
            this.entityStartLocations = new ArrayList<GCC>(eliminateHostiles.getEntities().getStartLocation().size());
            for(StartLocation sLocation : eliminateHostiles.getEntities().getStartLocation()){
                
                AbstractCoordinate coord = DomainDKFHandler.buildCoordinate(sLocation.getCoordinate());
                GCC gcc = CoordinateUtil.getInstance().convertToGCC(coord);
                entityStartLocations.add(gcc);
            }
            
            originalTargetSize = entityStartLocations.size();
            
            for(String teamMemberRef : eliminateHostiles.getEntities().getTeamMemberRef()){
                // add to a list that will be manipulated during the life cycle of this condition
                missingEntities.add(teamMemberRef);
            }
            
            originalTargetSize += missingEntities.size();
        }        

        
        // validation check
        if(missingEntities.isEmpty() && (entityStartLocations == null || entityStartLocations.isEmpty())){
            throw new IllegalArgumentException("No targets have been specified.");
        }
        
        //save any authored real time assessment rules
        if(eliminateHostiles.getRealTimeAssessmentRules() != null){            
            addRealTimeAssessmentRules(eliminateHostiles.getRealTimeAssessmentRules());
        }
        
        if(eliminateHostiles.getTeamMemberRefs() != null){
            setTeamMembersBeingAssessed(eliminateHostiles.getTeamMemberRefs());
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
    public boolean handleTrainingAppGameState(Message message){
                
        //really only interested in entity state messages
        if(message.getMessageType() == MessageTypeEnum.ENTITY_STATE){
            
            EntityState entityState = (EntityState)message.getPayload();
            
            //before doing anything check to make sure there are still hostiles to kill
            if(haveInit && entitiesRemaining.isEmpty() && missingEntities.isEmpty()){
                return false;
            }
            
            //check if the entity needs to be added to the list of remaining entities to kill
            checkAddEntity(entityState);
            
            //only re-assess this condition if the entity state message describes a known enemy
            if(!entitiesRemaining.contains(entityState.getEntityID())){
                return false;
            }
            
            AssessmentLevelEnum level = null;
            
            if(entityState.getAppearance().getDamage() != DamageEnum.HEALTHY){
                //entity is damaged
                
                //remove from list of entities remaining to be killed
                entitiesRemaining.remove(entityState.getEntityID());                
                
                //update score
                scoringEventStarted();
                
                // track the time at which the shooter eliminates the target
            	TeamMember<?> targetMember = getTeamMemberFromTeamOrg(entityState);
            	AbstractAssessmentActor<?> targetActor = null;
            	if(targetMember != null) {
            		targetActor = new TeamMemberActor(targetMember.getName());
            	} else {
            		//TODO: Need to track open time for points as well
            	}
            	
            	if(targetActor != null) {
            		
            		// track how much time has passed since learners engaged target
            		Long openTime = varsHandler.getVariable(targetActor, ActorVariables.OPEN_TIME);
                	if(openTime != null) {
                	    varsHandler.setVariable(targetActor, ActorVariables.KILL_EFFICIENCY, 
                                System.currentTimeMillis() - openTime);
            		}
                	
                	//track how much time has passed since target appeared
                	Long upTime = varsHandler.getVariable(targetActor, ActorVariables.TARGET_UP);
                    if(upTime != null) {
                        varsHandler.setVariable(targetActor, ActorVariables.KILL_TIME, 
                                System.currentTimeMillis() - upTime);
                    }
            	}
                
                if(logger.isDebugEnabled()){
                    logger.debug("Removed entity id "+entityState.getEntityID()+" from list of entities remaining to be killed");
                }
            }            

            AssessmentLevelEnum authoredLevel = getAuthoredRealTimeAssessment();
            if(authoredLevel != null){
                //one of the authored assessment rules has been satisfied
                level = authoredLevel;                
            }

            if(entitiesRemaining.isEmpty() && missingEntities.isEmpty()){
                //using the current game state message it was determined that 
                //all hostiles have been eliminated 
                
                if(logger.isDebugEnabled()){
                    logger.debug("Eliminated all hostiles");
                }
                if(level == null){
                    //use the default assessment logic
                    level = AssessmentLevelEnum.AT_EXPECTATION;
                }
                
                // track how long it took to eliminate all targets
                TeamMember<?> targetMember = getTeamMemberFromTeamOrg(entityState);
            	AbstractAssessmentActor<?> targetActor = null;
            	if(targetMember != null) {
            		targetActor = new TeamMemberActor(targetMember.getName());
            	} else {
            		//TODO: Need to track time for points as well
            	}
            	
            	if(targetActor != null) {
            		
                	varsHandler.setVariable(targetActor, ActorVariables.TARGET_ENGAGEMENT_TIME, 
                			System.currentTimeMillis() - startAtTime.getTime());
            	}
                
                conditionCompleted();
                
            }else if(level == null && getAssessment() != DEFAULT_ASSESSMENT){
                //(use the default assessment logic)
                //still have enemy to kill - the assessment value is not the currently reported value, therefore
                //the level has changed.
                level = DEFAULT_ASSESSMENT;
            }
            
            if(level != null){
                setAssessmentExplanation();
                updateAssessment(level);
                return true;
            }

        } else if (message.getMessageType() == MessageTypeEnum.DETONATION) {

            Detonation dMessage = (Detonation) message.getPayload();

            // shot must be fired by learner
            TeamMember<?> teamMember = isConditionAssessedTeamMember(dMessage.getFiringEntityID());
            if(teamMember != null){              

                // did the shot hit a lifeform
                if (dMessage.getDetonationResult() == DetonationResultEnum.ENTITY_IMPACT) {

                    // was it a hit on a known, alive, target?
                    if (entitiesRemaining.contains(dMessage.getTargetEntityID())) {
                        
                        TeamMember<?> targetMember = getTeamMemberFromTeamOrg(dMessage.getTargetEntityID());
                        AbstractAssessmentActor<?> targetActor = null;
                        if(targetMember != null) {
                            targetActor = new TeamMemberActor(targetMember.getName());
                        } else {
                            //TODO: Need to track time for points as well
                        }
                        
                        if(targetActor != null
                                && varsHandler.getVariable(targetActor, ActorVariables.TARGET_FIRST_STRIKE) == null) {
                            
                            // track the first time the target is hit
                            varsHandler.setVariable(targetActor, ActorVariables.TARGET_FIRST_STRIKE, System.currentTimeMillis());
                        }                    
                    }
                }
            }
        }
        
        return false;
    }
    
    /**
     * Check whether the entity described by the entity state provided is at/very-near one
     * of this condition's entity start locations.  If so, then add the entity id to the list of hostiles.
     * 
     * @param entityState Entity state info 
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
        StringBuilder assessmentExplanationBuilder = new StringBuilder();
        
        assessmentExplanationBuilder.append("Eliminated ").append(originalTargetSize - entitiesRemaining.size()).append(" of ").append(originalTargetSize).append(" targets.");

        String newAssessmentExplanation = assessmentExplanationBuilder.toString();
        boolean changed = !newAssessmentExplanation.equals(assessmentExplanation);
        assessmentExplanation = newAssessmentExplanation;
        
        return changed;
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
        
        /* Track when learner fails to eliminate any targets within the timeframe */
        try {
            for(EntityIdentifier remainingEntity : entitiesRemaining) {
                TeamMember<?> targetMember = getTeamMemberFromTeamOrg(remainingEntity);
                AbstractAssessmentActor<?> targetActor = null;
                if(targetMember != null) {
                    targetActor = new TeamMemberActor(targetMember.getName());
                } else {
                    //TODO: Need to track time for points as well
                }
                
                Long openTime = varsHandler.getVariable(targetActor, ActorVariables.OPEN_TIME);
                if(targetActor != null && openTime != null) {
                    
                    varsHandler.setVariable(targetActor, ActorVariables.NO_KILL_TIME, 
                            System.currentTimeMillis() - openTime);
                }
            }
        } catch(Exception e) {
            logger.warn("Failed to update no kill time variable while stopping condition", e);
        }
        
        super.stop();
    }

    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[EliminateHostilesCondition: ");
        sb.append(super.toString());
        sb.append("]");
        
        return sb.toString();
    }

}
