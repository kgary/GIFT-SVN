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
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.ta.state.SimpleExampleState;
import mil.arl.gift.net.api.message.Message;

/**
 * This string matching condition was originally created as a simple example of a condition
 * implementation to be used with the 'Simple Example State' game state messages.  This condition
 * will look for a string match between the condition's input configuration parameter value (the key) and
 * the game state message contents (the var in SimpleExampleState.java).  Once the values match, this
 * condition returns At Expectation.
 * 
 * The purpose of this condition is for development purposes as it provides a simple example
 * of assessment logic and works with the GIFT Developer Guide document as well as the various
 * Simple Example related code and the C# Simple Example Training Application.
 *  
 * @author mhoffman
 *
 */
public class StringMatchingExampleCondition extends AbstractCondition {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(StringMatchingExampleCondition.class);
    
    /**
     * contains the types of GIFT messages this condition needs in order to provide assessments
     */
    private static final List<MessageTypeEnum> simulationInterests;    
    static{
        simulationInterests = new ArrayList<MessageTypeEnum>();
        simulationInterests.add(MessageTypeEnum.SIMPLE_EXAMPLE_STATE);
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
    private static final ConditionDescription DESCRIPTION = new FileDescription(new File("docs" + File.separator + "conditions" + File.separator + "StringMatching.GIFT Domain condition description.html"), "String Matching");
    
    /** a key to look for in the game state message */
    private String conditionKey;
    
    /**
     * Default constructor - required for authoring logic
     */
    public StringMatchingExampleCondition(){
        
    }
    
    /**
     * Class constructor
     * 
     * @param input configuration parameters for this condition
     */
    public StringMatchingExampleCondition(generated.dkf.GenericConditionInput input){
        
        //assuming that there are only 1 name:value pair in the list
        List<generated.dkf.Nvpair> pairs = input.getNvpair();
        generated.dkf.Nvpair pair = pairs.get(0);
        
        //set this condition's key value which will be used for string matching 
        //with incoming game state messages
        conditionKey = pair.getValue();        
    }
    
    // team member refs not allowed as an input
    @Override
    public String hasValidTeamMemberRefs(){
        return null;
    }

    @Override
    public boolean handleTrainingAppGameState(Message message) {
        
        AssessmentLevelEnum level = null;
        
        //since this class is only registered to receive 1 type of message, casting w/o checking type 
        SimpleExampleState state = (SimpleExampleState)message.getPayload();
        if(state.getVar().equals(conditionKey)){
            //found the condition key in the game state message content
            
            if(logger.isInfoEnabled()){
                logger.info("Found "+conditionKey+" in the ExampleState game state message.");
            }
            
            // This is used to indicate that an important event for this condition needs to be scored.
            // In this case we could use the scoring logic to indicate how many times the string was matched 
            // and to start a timer to determine how much time elapses until this condition is not satisfied.
            // Scoring is used as an overall assessment of a lesson and is normally presented in 
            // After Action Review (AAR) and stored in an LMS/LRS.
            // Note: the DKF responsible for configuring this condition must have scoring attributes in order
            //       for this call to do anything.
            scoringEventStarted();

            level = AssessmentLevelEnum.AT_EXPECTATION;
            updateAssessment(level);
            
        }else{
            // This is used to indicate that an important event for this condition needs to be scored.
            // In this case we are using it to indicate the amount of time since this condition was satisfied.
            // Note: the DKF responsible for configuring this condition must have scoring attributes in order
            //       for this call to do anything.
            scoringEventEnded();
            
            level = AssessmentLevelEnum.BELOW_EXPECTATION;
            updateAssessment(level);
        }
        
        return true;
    }

    @Override
    public ConditionDescription getDescription() {
        return DESCRIPTION;
    }

    @Override
    public List<MessageTypeEnum> getSimulationInterests() {
        return simulationInterests;
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

}
