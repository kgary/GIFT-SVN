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
import mil.arl.gift.common.ta.state.RifleShotMessage;
import mil.arl.gift.net.api.message.Message;

/**
 * Condition class that checks if the expected number of shots were received. If
 * the expected number of shots was received, the condition is assessed as at
 * expectation. Otherwise, the condition is assessed as below expectation.
 * 
 * @author ohasan
 */
public class NumberOfShotsFiredCondition extends AbstractCondition {

    /**
     * Reference to the Logger object.
     */
    private static Logger logger = LoggerFactory
            .getLogger(NumberOfShotsFiredCondition.class);

    private static final AssessmentLevelEnum DEFAULT_ASSESSMENT = AssessmentLevelEnum.BELOW_EXPECTATION;

    private int expectedNumberOfShots = 0;

    private int numberOfShotsReceived = 0;

    /**
     * Contains the types of GIFT messages this condition needs in order to
     * provide assessments.
     */
    private static final List<MessageTypeEnum> simulationInterests;

    static {
        simulationInterests = new ArrayList<MessageTypeEnum>();
        simulationInterests.add(MessageTypeEnum.RIFLE_SHOT_MESSAGE);
    }
    
    /**
     * contains the type of overall assessment scorers this condition can populate for an AAR
     */
    private static final Set<Class<?>> overallAssessmentTypes;
    static{
        overallAssessmentTypes = new HashSet<Class<?>>();
        overallAssessmentTypes.add(generated.dkf.Count.class);
        overallAssessmentTypes.add(generated.dkf.CompletionTime.class);
    }
    
    /** information about the purpose of this condition */
    private static final ConditionDescription DESCRIPTION = new FileDescription(new File("docs" + File.separator + "conditions" + File.separator + "NumberOfShots.GIFT Domain condition description.html"), "Shot count");
    
    /**
     * Default constructor - required for authoring logic
     */
    public NumberOfShotsFiredCondition(){
        
    }

    /**
     * Class constructor
     * 
     * @param input - domain knowledge input configuration params
     */
    public NumberOfShotsFiredCondition(
            generated.dkf.NumberOfShotsFiredCondition input) {

        this.expectedNumberOfShots = (Integer.valueOf(
                input.getExpectedNumberOfShots())).intValue();

        updateAssessment(DEFAULT_ASSESSMENT);
    }
    
    // team member refs not allowed as an input
    @Override
    public String hasValidTeamMemberRefs(){
        return null;
    }

    @Override
    public boolean handleTrainingAppGameState(Message message) {

        AssessmentLevelEnum level = DEFAULT_ASSESSMENT;

        if (message.getMessageType() == MessageTypeEnum.RIFLE_SHOT_MESSAGE) {

            RifleShotMessage shotMessage = (RifleShotMessage) message
                    .getPayload();

            logger.error("Received shot message: " + shotMessage);

            numberOfShotsReceived++;
            
            //count number of shots fired
            scoringEventStarted();

            if (numberOfShotsReceived == expectedNumberOfShots) {

                // if we have received the expected number of shots for this
                // session, this condition is now complete
                logger.error("Received the expected number of shots");

                level = AssessmentLevelEnum.AT_EXPECTATION;

                conditionCompleted();
            }

            updateAssessment(level);
        }

        return true;
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
}
