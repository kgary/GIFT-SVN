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
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.vecmath.Point3d;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generated.dkf.LearnerActionEnumType;
import mil.arl.gift.common.ta.state.LoSResult;
import mil.arl.gift.common.ta.state.LoSResult.VisibilityResult;
import mil.arl.gift.common.LoSQuery;
import mil.arl.gift.common.course.dkf.ConditionDescription;
import mil.arl.gift.common.course.dkf.ConditionDescription.FileDescription;
import mil.arl.gift.common.course.dkf.team.TeamMember;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.ta.state.EntityIdentifier;
import mil.arl.gift.common.ta.state.EntityState;
import mil.arl.gift.domain.knowledge.common.PlaceOfInterestInterface;
import mil.arl.gift.domain.knowledge.common.Point;
import mil.arl.gift.domain.knowledge.common.PlacesOfInterestManager;
import mil.arl.gift.net.api.message.Message;

/**
 * This condition checks if points of interest have been identified.
 * 
 * @author mhoffman
 *
 */
public class IdentifyPOIsCondition extends AbstractCondition {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(IdentifyPOIsCondition.class);
    
    /** the default assessment for this condition */
    private static final AssessmentLevelEnum DEFAULT_ASSESSMENT = AssessmentLevelEnum.BELOW_EXPECTATION;
    
    /** min amount of time between LoS Queries (milliseconds) */
    private static final long MIN_TIME_BETWEEN_LOS_QUERY = 1000;
    
    /** the min amount of percent visible a result has to be for clear LoS */
    //Note: the value of 0.5 came from testing w/ VBS, where clearly seeing a location produced values around 0.52-0.56
    private static final double MIN_CLEAR_VISIBILITY_PERCENT = 0.5;    
    
    /**
     * contains the types of GIFT messages this condition needs in order to provide assessments
     */
    private static final List<MessageTypeEnum> simulationInterests;    
    static{
        simulationInterests = new ArrayList<MessageTypeEnum>();
        simulationInterests.add(MessageTypeEnum.LOS_RESULT);
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
        overallAssessmentTypes.add(generated.dkf.CompletionTime.class);
    }
    
    /** information about the purpose of this condition */
    private static final ConditionDescription DESCRIPTION = new FileDescription(new File("docs" + File.separator + "conditions" + File.separator + "IdentifyPOIs.GIFT Domain condition description.html"), "Identify Locations");
    
    /** points of interest(s) */
    private List<Point> pois = new ArrayList<>();
    
    /** input parameters for this condition */
    private generated.dkf.IdentifyPOIsCondition identifyPOIsInput = null;
    
    /** list of POIs still remaining to be identified */    
    private List<Point3d> remainingPOIs = new ArrayList<Point3d>();
    
	/**
	 * mapping of entity ids (from entity state messages) for the team members this condition is assessing for
	 * to the data associated with the last Line of sight query made for that entity.
	 */
    private Map<EntityIdentifier, EntityStateQueryWrapper> prevEntityStateMap = new HashMap<>();
    
    /** contains the entity markings of the team members that are being assessed in this condition */
    private Set<String> assessedMarkings = new HashSet<>();
    
    /** used to track any outstanding LoS request that hasn't returned yet */
    private LoSQuery outstandingLoSRequest = null;
    
    /**
     * Simple data wrapper to help keep track of previous entity state info for a single
     * team member.
     * 
     * @author mhoffman
     *
     */
    private class EntityStateQueryWrapper{
        
        /** time at which the last query happened */
        long lastQuery = System.currentTimeMillis();
        
        /** the last entity state received for a team member */
        EntityState prevEntityState = null;
    }
    
    /**
     * Default constructor - required for authoring logic
     */
    public IdentifyPOIsCondition(){
        
    }
    
    /**
     * Class constructor - only allow wrappers to decode and populate this class
     * 
     * @param points the points to populate this condition with
     */
    public IdentifyPOIsCondition(List<Point> points){
        
        for(Point point : points){
            addPoint(point);
        }
        
        updateAssessment(DEFAULT_ASSESSMENT);
    }
    
    /**
     * Class constructor - set attributes for dkf content
     * 
     * @param identifyPOIs - dkf content for this conditions
     */
    public IdentifyPOIsCondition(generated.dkf.IdentifyPOIsCondition identifyPOIs){
        
        this.identifyPOIsInput = identifyPOIs;
        
        //save any authored real time assessment rules
        if(identifyPOIs.getRealTimeAssessmentRules() != null){            
            addRealTimeAssessmentRules(identifyPOIs.getRealTimeAssessmentRules());
        }
        
        if(identifyPOIsInput.getTeamMemberRefs() != null){
            setTeamMembersBeingAssessed(identifyPOIsInput.getTeamMemberRefs());
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
    public void setPlacesOfInterestManager(PlacesOfInterestManager placesOfInterestManager){
        super.setPlacesOfInterestManager(placesOfInterestManager);  
        
        if(identifyPOIsInput != null){
            
            for(generated.dkf.PointRef pointRef : identifyPOIsInput.getPois().getPointRef()){
                PlaceOfInterestInterface placeOfInterest = placesOfInterestManager.getPlacesOfInterest(pointRef.getValue());
                
                if(placeOfInterest instanceof Point){
                    addPoint((Point) placeOfInterest);
                }
            } 
        }

    }
    
    /**
     * Add a point to the points of interest for this condition
     * 
     * @param point the common point representation of a dkf point to add
     */
    private void addPoint(Point point){
        pois.add(point);
        remainingPOIs.add(new Point3d(point.x, point.y, point.z)); 
    }

    @Override
    public boolean handleTrainingAppGameState(Message message) {
        AssessmentLevelEnum level = null;
        
        if(!remainingPOIs.isEmpty()){
            //only bother if there are still POIs to check against
            
            //interested in entity state messages for the purpose of checking changes changed in learner spatial info
            //in order to spawn LoS queries
            if(message.getMessageType() == MessageTypeEnum.ENTITY_STATE){
                
                EntityState entityState = (EntityState)message.getPayload();
                
                //careful when using this debug when receiving entity state at high frequency
//              if(isDebug){
//                  logger.debug("Received message of "+entityState);
//              }
                
                //only re-assess spatial info if the entity state message describes a team member this condition is interested in
                TeamMember<?> teamMember = isConditionAssessedTeamMember(entityState.getEntityID());
                if(teamMember != null){  
                    
                    // gather entity markings of assessed members
                    if(teamMember.getIdentifier() instanceof String){
                        assessedMarkings.add((String) teamMember.getIdentifier());
                    }
                    
                    EntityStateQueryWrapper esWrapper = prevEntityStateMap.get(entityState.getEntityID());
                    if(esWrapper == null){
                        esWrapper = new EntityStateQueryWrapper();
                        prevEntityStateMap.put(entityState.getEntityID(), esWrapper);
                    }else if(!entityState.getOrientation().equals(esWrapper.prevEntityState.getOrientation())){
                        //there is a change in orientation
                    
                        //has it been long enough since the last LoS query            
                        if(outstandingLoSRequest == null && 
                                System.currentTimeMillis() - esWrapper.lastQuery > MIN_TIME_BETWEEN_LOS_QUERY){
                            
                            outstandingLoSRequest = new LoSQuery(remainingPOIs, conditionInstanceID.toString(), assessedMarkings);
                            
                            //request a LoS Query
                            if(conditionActionInterface != null){
                                conditionActionInterface.trainingApplicationRequest(outstandingLoSRequest);
                            }
                            
                            esWrapper.lastQuery = System.currentTimeMillis();
                        }
                    }
                    
                    // save for the next check for this particular entity (team member)
                    esWrapper.prevEntityState = entityState;
                }

            }else if(message.getMessageType() == MessageTypeEnum.LOS_RESULT){
                
                LoSResult losResult = (LoSResult)message.getPayload();
                
                if(!losResult.getRequestId().equals(conditionInstanceID.toString())){
                    //this line of sight result is not for this condition's line of sight request
                    return false;
                }
                
                //careful when using this debug when receiving LOS results at high frequency
//                if(isDebug){
//                    logger.debug("Received message of "+losResult);
//                }
                
                Map<String, List<VisibilityResult>> entityVisibilityResults = losResult.getEntitiesLoSResults();
                Set<Point3d> requestedPointsToRemove = new HashSet<>();
                for(String entityMarking : entityVisibilityResults.keySet()){
                    
                    TeamMember<?> assessedMember = getTeamMemberFromTeamOrg(entityMarking);
                    
                    for(VisibilityResult result : entityVisibilityResults.get(entityMarking)){
                        
                        if(remainingPOIs.size() <= result.getIndexOfPointFromRequest()){
                            // not a valid index in the list
                            continue;
                        }
                        
                        Point3d point = remainingPOIs.get(result.getIndexOfPointFromRequest());                            
                        double visibility = result.getVisbilityPercent();
                        if(visibility >= MIN_CLEAR_VISIBILITY_PERCENT){
                            
                            if(logger.isDebugEnabled()){
                                logger.debug("Removing point = "+point+" due to clear visibility percent of "+visibility);
                            }
                            requestedPointsToRemove.add(point);
                            
                            scoringEventStarted(assessedMember);
                        }
                    }
                }
                
                // update the list of points that still need to be checked
                remainingPOIs.removeAll(requestedPointsToRemove);
                
                // reset - allow another LoS request in the future
                outstandingLoSRequest = null;
                
                AssessmentLevelEnum authoredLevel = getAuthoredRealTimeAssessment();
                if(authoredLevel != null){
                    //one of the authored assessment rules has been satisfied
                    level = authoredLevel;                
                }
                
                // this game state message has removed the last POI from the list,
                // therefore nothing else left to identify
                if(remainingPOIs.isEmpty()){
                    conditionCompleted();
                    
                    if(level == null){
                        //there are no authored real time assessment rules, use the default logic
                        level = AssessmentLevelEnum.AT_EXPECTATION;
                    }
                }
                
                //update assessment explanation
                boolean assessmentExplanationChanged = setAssessmentExplanation(); 
                
                if(level != null){
                    updateAssessment(level);
                    return true;
                }else if(assessmentExplanationChanged){
                    return true;
                }
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
        boolean changed = false;
        StringBuilder assessmentExplanationBuilder = new StringBuilder();
        assessmentExplanationBuilder.append("Has viewed ").append((pois.size() - remainingPOIs.size())).append(" of ").append(pois.size()).append(" locations.");   
        
        String newAssessmentExplanation = assessmentExplanationBuilder.toString();
        changed = !newAssessmentExplanation.equals(assessmentExplanation);
        assessmentExplanation = newAssessmentExplanation;
        
        return changed;
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
        return true;
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[IdentifyPOIsCondition: ");
        sb.append(super.toString());
        sb.append(" # POIs = ").append(pois.size());
        sb.append(", # remaining POIs = ").append(remainingPOIs.size());
        sb.append("]");
        
        return sb.toString();
    }

}
