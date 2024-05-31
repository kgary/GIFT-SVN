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
 * Condition class that checks if the expected number of shots were received and
 * if so, determines if all shots were within the expected distance from the
 * target center. For each shot, if its distance from the target center was
 * greater than expected, this condition rates a score of below expectations for
 * that shot. If the distance from the target center was less than or equal to
 * expected, this condition considers that shot at expectations.
 * 
 * @author ohasan
 */
public class MarksmanshipSessionCompleteCondition extends AbstractCondition {

    /**
     * Reference to the Logger object.
     */
    private static Logger logger = LoggerFactory
            .getLogger(MarksmanshipSessionCompleteCondition.class);

    private static final AssessmentLevelEnum DEFAULT_ASSESSMENT = AssessmentLevelEnum.AT_EXPECTATION;

    private static final float MINIMUM_EXPECTED_RESULT = 5.0f;

    private int expectedNumberOfShots = 0;

    private List<Float> shotResultList = new ArrayList<Float>();

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
    private static final ConditionDescription DESCRIPTION = new FileDescription(new File("docs" + File.separator + "conditions" + File.separator + "MarksmanshipSessionComplete.GIFT Domain condition description.html"), "Marksmanship Session Completed");
    
    /**
     * Default constructor - required for authoring logic
     */
    public MarksmanshipSessionCompleteCondition(){
        
    }

    /**
     * Class constructor
     * 
     * @param input - domain knowledge input configuration params
     */
    public MarksmanshipSessionCompleteCondition(
            generated.dkf.MarksmanshipSessionCompleteCondition input) {

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

            if(logger.isDebugEnabled()){
                logger.debug("Received shot message: " + shotMessage);
            }

            shotResultList.add(shotMessage.getResult());

            if (shotResultList.size() == expectedNumberOfShots) {

                // if we have received the expected number of shots for this
                // session, this condition is now complete

                if(logger.isDebugEnabled()){
                    logger.debug("Result:  fractional: " + getFractionalResult()
                        + " average: " + getAverageResult());
                }

                // for each of the shot results, determine if the shot was
                // within the expected distance from the target center and
                // assess the shot
                for (Float result : shotResultList) {

                    if (result >= MINIMUM_EXPECTED_RESULT) {

                        level = AssessmentLevelEnum.AT_EXPECTATION;
                    } else {

                        level = AssessmentLevelEnum.BELOW_EXPECTATION;
                    }

                    scoringEventStarted();
                }

                conditionCompleted();
            }

            updateAssessment(level);
        }

        return true;
    }

    /**
     * Returns the average result of the received shots. The average result is
     * the average of the result value for each shot.
     * 
     * @return the average result of the received shots
     * @see #getFractionalResult()
     */
    private float getAverageResult() {

        return getFractionalResult() / shotResultList.size();
    }

    /**
     * Returns the fractional result of the received shots. The fractional
     * result is the sum of the result value for each shot.
     * 
     * @return the fractional result of the received shots
     * @see #getAverageResult()
     */
    private float getFractionalResult() {

        float returnValue = 0.0f;

        for (Float result : shotResultList) {

            returnValue += result;
        }

        return returnValue;
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
