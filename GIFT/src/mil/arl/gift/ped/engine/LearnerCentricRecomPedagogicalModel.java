/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.ped.engine;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import mil.arl.gift.common.CourseState;
import mil.arl.gift.common.InitializeDomainSessionRequest;
import mil.arl.gift.common.InitializePedagogicalModelRequest;
import mil.arl.gift.common.PedagogicalRequest;
import mil.arl.gift.common.RequestBranchAdaptation;
import mil.arl.gift.common.CourseState.RequiredLearnerStateAttributes.AttributeValues;
import mil.arl.gift.common.CourseState.RequiredLearnerStateAttributes.ConceptAttributeValues;
import mil.arl.gift.common.course.strategy.BranchAdaptationStrategy;
import mil.arl.gift.common.course.strategy.BranchAdaptationStrategy.ProgressionInfo;
import mil.arl.gift.common.enums.ExpertiseLevelEnum;
import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.state.LearnerState;
import mil.arl.gift.common.state.LearnerStateAttribute;
import mil.arl.gift.common.state.LearnerStateAttributeCollection;
import mil.arl.gift.ped.PedagogicalModel;

/**
 * Implementation of PedagogicalModel interface for Learner Centric Recommendations.
 * This class decides whether to skip the next transition depending on whether or 
 * not there is existing learner state for the attributes that are covered in the 
 * next transition.
 * 
 * @author sharrison
 */
public class LearnerCentricRecomPedagogicalModel implements PedagogicalModel {
    
    /** the reason this model might request a pedagogical request */
    private static final String ADAPTATION_REASON = "Continue to the next course object because the current"+
            " learner state doesn't contain the state information that will be collected";

    /**
     * the last learner state received. Will be null until the first learner state has been handled.
     */
    private LearnerState prevLearnerState;
    
    /**
     * The map which specifies the duration that previous learner state's should still be considered valid 
     * (measured in milliseconds) for each state attribute
     */
    private Map<LearnerStateAttributeNameEnum, Serializable> learnerStateShelfLifeMap = new HashMap<>();

    @Override
    public void getPedagogicalActions(LearnerState state, PedagogicalRequest requests) {
        // update previous state for next incoming state
        prevLearnerState = state;
    }

    @Override
    public void initialize(InitializeDomainSessionRequest initDomainSessionRequest) {
        // nothing to do right now...
    }

    @Override
    public void initialize(InitializePedagogicalModelRequest initRequest) throws DetailedException {
        // nothing to do right now...
    }

    /**
     * Checks if all the given learner state attributes exist in the learner state.
     * 
     * @param learnerStateAttributesMap the learner state attributes that are required for the next
     * course object.  The values are optional and can contain additional, more granular information such 
     * as a course concept must have 'Journeyman' Expertise level enum for 'Knowledge' learner state attribute. 
     * Can be null or empty.
     * @param request  the pedagogical request being built which contains pedagogical actions to possibly
     * add a RequestBranchAdaptation pedagogical request if the previous learner 
     * state contains a value for each of the learner state attributes within either
     * the cognitive or affective state, and none of the learner state attributes are 
     * stale (see isLearnerStateStale method). If any of the previous conditions are 
     * broken or the list of learner state attributes is null/empty or there is no 
     * previous learner state, return without adding anything.
     */
    private void learnerContainsStateAttributes(Map<LearnerStateAttributeNameEnum, AttributeValues> learnerStateAttributesMap,
            PedagogicalRequest request) {

        // Given data must exist, otherwise return false.
        if (learnerStateAttributesMap == null || learnerStateAttributesMap.isEmpty() || prevLearnerState == null) {
            return;
        }

        // Check if cognitive data exists.
        boolean cognitiveExists = false;
        if (prevLearnerState.getCognitive() != null && !prevLearnerState.getCognitive().getAttributes().isEmpty()) {
            cognitiveExists = true;
        }

        // Check if affective data exists.
        boolean affectiveExists = false;
        if (prevLearnerState.getAffective() != null && !prevLearnerState.getAffective().getAttributes().isEmpty()) {
            affectiveExists = true;
        }

        // check if all the survey learner state attributes exist in the user's learner state; if
        // any do not, return null.
        for (LearnerStateAttributeNameEnum lsEnum : learnerStateAttributesMap.keySet()) {
            
            LearnerStateAttribute cognitiveAttr = cognitiveExists ? prevLearnerState.getCognitive().getAttributes().get(lsEnum) : null;
            LearnerStateAttribute affectiveAttr = affectiveExists ? prevLearnerState.getAffective().getAttributes().get(lsEnum) : null;
            
            if(cognitiveAttr == null && affectiveAttr == null){
                // there is not learner state information for this learner state attribute
                return;
            }
            
            AttributeValues attributeValues = learnerStateAttributesMap.get(lsEnum);
            
            // does learner state attribute exist within cognitive or affective attributes?
            if (!isLearnerStateStale(cognitiveAttr, attributeValues)
                    || !isLearnerStateStale(affectiveAttr, attributeValues)) {
                // it exists, check next learner state attribute
                continue;
            } else {
                // it doesn't exist, return null.
                return;
            }
        }

        // all given learner state attributes were contained in the user's learner state. Return a
        // progression pedagogical request.
        RequestBranchAdaptation branchAdaptationRequest = new RequestBranchAdaptation(new BranchAdaptationStrategy(new ProgressionInfo()));
        request.addRequest(ADAPTATION_REASON, branchAdaptationRequest);
    }
    
    /**
     * Determines whether or not the learner state is still valid based on the shelf life
     * specified for the given learner state attribute type.
     * @param attr the learner state attribute to test for staleness
     * @param attributeValues the learner state attribute values that are required for the next course object. 
     * The values can contain additional, more granular information such as a course concept 
     * must have 'Journeyman' Expertise level enum for 'Knowledge' learner state attribute. Can be null.
     * @param stateShelfLife contains the shelf life rule.  Currently supports a Long value.
     * @return false if the learner state is considered fresh by the specified shelf life
     * contained within the learnerStateShelfLifeMap or no shelf life is specified for the 
     * given attribute type.
     */
    public static boolean isLearnerStateStale(LearnerStateAttribute attr, AttributeValues attributeValues, Serializable stateShelfLife){
        
        long now = System.currentTimeMillis();
        
        if(attributeValues != null){
            // there is additional information about the learner state attribute(s) that should be checked
            
            if(attributeValues instanceof ConceptAttributeValues){
                ConceptAttributeValues conceptValues = (ConceptAttributeValues)attributeValues;
                
                if(!(attr instanceof LearnerStateAttributeCollection)){
                    // the attributes to look for needs a learner state attribute collection but 
                    // this learner state attribute is not a collection
                    return true;
                }
                
                LearnerStateAttributeCollection learnerStateCollection = (LearnerStateAttributeCollection)attr;
                Map<String, ExpertiseLevelEnum> conceptExpertiseMap = conceptValues.getConceptExpertiseLevel();
                for(String conceptName : conceptExpertiseMap.keySet()){
                    
                    LearnerStateAttribute currConceptAttr = learnerStateCollection.getConceptExpertiseLevel(conceptName);
                    ExpertiseLevelEnum reqExpertiseLevel = conceptExpertiseMap.get(conceptName);
                    if(currConceptAttr == null){
                        // the required concept was not found in the learner state collection, therefore
                        // can't check if the attribute is stale
                        return true;
                    }else if(currConceptAttr.getShortTerm() == ExpertiseLevelEnum.UNKNOWN){
                        // an unknown current assessment on the required course concept is the same as not having an assessment at all
                        return true;
                    }else if(reqExpertiseLevel == null || currConceptAttr.getShortTerm().equals(reqExpertiseLevel)){
                        // the required concept's expertise level is optional OR matches that of the current learner state for 
                        // that concept, use the short term time stamp for shelf life comparison
                        
                        if(isExpiredShelfLife(stateShelfLife, now, currConceptAttr.getShortTermTimestamp())){
                            // found a stale concept attribute
                            return true;
                        }
                    }else{
                        // the expertise level required doesn't match the current value for this concept
                        return true;
                    }
                }
                
                // all required concept state attribute values are fresh enough
                return false;
                
            }
            
            // need to implement
            throw new RuntimeException("Found unhandled AttributeValues type of "+attributeValues+" that needs to be implemented.");
            
        }else{        
            // Check the shelf life of the attribute

            //The learner state shelf life is expressed as milliseconds
            return isExpiredShelfLife(stateShelfLife, now, attr.getShortTermTimestamp());
        }
    }
    
    /**
     * Determines whether or not the learner state is still valid based on the shelf life
     * specified for the given learner state attribute type.
     * @param attr the learner state attribute to test for staleness
     * @param attributeValues the learner state attribute values that are required for the next course object. 
     * The values can contain additional, more granular information such as a course concept 
     * must have 'Journeyman' Expertise level enum for 'Knowledge' learner state attribute. Can be null.
     * @return false if the learner state is considered fresh by the specified shelf life
     * contained within the learnerStateShelfLifeMap or no shelf life is specified for the 
     * given attribute type.
     */
    private boolean isLearnerStateStale(LearnerStateAttribute attr, AttributeValues attributeValues) {
        
        if(attr == null){
            return true;
        }
        
        //Get the shelf life of the given attribute
        Serializable stateShelfLife = learnerStateShelfLifeMap.get(attr.getName());  
        return isLearnerStateStale(attr, attributeValues, stateShelfLife);
    }
        
    /**
     * Return whether the difference between now and the learner state attribute time exceeds the shelf
     * life rule.
     * @param stateShelfLife contains the shelf life rule.  Currently supports a Long value.
     * @param now the time it is now, used to compare all states at a current point in time to the
     * same time stamp.
     * @param stateTime when a learner state attribute was last updated
     * @return true if the learner state attribute has exceeded it's shelf life
     */
    private static boolean isExpiredShelfLife(Serializable stateShelfLife, long now, long stateTime){
        
        if(stateShelfLife instanceof Long){
            long shelfLifeAsMilliseconds = (Long)stateShelfLife;
            long learnerStateAge = now - stateTime;
            return learnerStateAge > shelfLifeAsMilliseconds; 
        }else{
            //There was no specified behavior for how to determine if the state was still valid, assume it is valid
            return false;
        }
    }

    @Override
    public PedagogicalRequest handleCourseStateUpdate(CourseState state) {

        // Copy each of the course state's entries into the learnerStateShelfLifeMap
        for (LearnerStateAttributeNameEnum attrName : state.getLearnerStateShelfLife().keySet()) {
            learnerStateShelfLifeMap.put(attrName, state.getLearnerStateShelfLife().get(attrName));
        }
        
        if (state.getRequiredLearnerStateAttributes() != null &&
                state.getRequiredLearnerStateAttributes().getLearnerStateAttributesMap() != null) {

            // Check if any of the next attribute enums are exclusive to concepts. If even one of them
            // is, then we do not want to create a pedagogical request since we want to maintain the
            // existing courseflow (e.g. we do not want to skip a survey since each concept's attribute
            // will need to be evaluated individually).
            Map<LearnerStateAttributeNameEnum, AttributeValues> learnerStateAttributesMap = 
                    state.getRequiredLearnerStateAttributes().getLearnerStateAttributesMap();
            for (LearnerStateAttributeNameEnum nameEnum : learnerStateAttributesMap.keySet()) {
                
                AttributeValues attributeValues = learnerStateAttributesMap.get(nameEnum);
                if(attributeValues instanceof ConceptAttributeValues){
                    continue;
                }else if (nameEnum.isExclusiveToConcepts()) {
                    return null;  // if any learner state attributes defined as required but should also mention
                                  // specific course concepts then the logic should be using ConceptAttributeValues
                }
            }

            PedagogicalRequest request = new PedagogicalRequest();                
            learnerContainsStateAttributes(learnerStateAttributesMap, request);
            return request;
        }

        return null;
    }
    
    @Override
    public void handleLessonStarted() {
        // nothing        
    }

    @Override
    public String toString() {

        StringBuffer sb = new StringBuffer();
        sb.append("[LearnerCentricRecomPedagogicalModel: ");
        sb.append("previousLearnerState = ").append(prevLearnerState);
        sb.append("]");
        return sb.toString();
    }
}
