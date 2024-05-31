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

import javax.vecmath.Point3d;

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
 * if so, calculates the mean distance (dispersion) of the shots from the center
 * of the shot group. If the dispersion is greater than expected, this condition
 * considers itself below expectations. If the dispersion is greater or equal to
 * expected, this condition considers itself at expectations.
 * 
 * @author ohasan
 */
public class MarksmanshipPrecisionCondition extends AbstractCondition {

    /**
     * Reference to the Logger object.
     */
    private static Logger logger = LoggerFactory
            .getLogger(MarksmanshipPrecisionCondition.class);

    private static final AssessmentLevelEnum DEFAULT_ASSESSMENT = AssessmentLevelEnum.AT_EXPECTATION;

    private static final double MAXIMUM_EXPECTED_DISPERSION = 0.5;

    private int expectedNumberOfShots = 0;

    private List<Point3d> shotLocationList = new ArrayList<Point3d>();

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
    private static final ConditionDescription DESCRIPTION = new FileDescription(new File("docs" + File.separator + "conditions" + File.separator + "MarksmanshipPrecision.GIFT Domain condition description.html"), "Marksmanship Precision");
    
    /**
     * Default constructor - required for authoring logic
     */
    public MarksmanshipPrecisionCondition(){
        
    }

    /**
     * Class constructor
     * 
     * @param input - domain knowledge input configuration params
     */
    public MarksmanshipPrecisionCondition(
            generated.dkf.MarksmanshipPrecisionCondition input) {

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

            shotLocationList.add(shotMessage.getLocation());

            if (shotLocationList.size() == expectedNumberOfShots) {

                // if we have received the expected number of shots for this
                // session, this condition is now complete

                if(logger.isDebugEnabled()){
                    logger.debug("Center of shot group: " + getCenterOfShotGroup());
                }

                if(logger.isDebugEnabled()){
                    logger.debug("Mean distance of shots from shot group center: "
                            + getMeanDistanceOfShotsFromShotGroupCenter());
                }

                double meanDistance = getMeanDistanceOfShotsFromShotGroupCenter();

                if (meanDistance > MAXIMUM_EXPECTED_DISPERSION) {

                    level = AssessmentLevelEnum.BELOW_EXPECTATION;
                } else {

                    level = AssessmentLevelEnum.AT_EXPECTATION;
                }

                scoringEventStarted();

                conditionCompleted();

                updateAssessment(level);
                
                return true;
            }
        }

        return false;
    }

    /**
     * Returns the location of the shot group center. The shot group center is
     * determined by taking the average of the X-coordinate and the average of
     * the Y-coordinate over all received shots.
     * 
     * @return the location of the shot group center
     */
    private Point3d getCenterOfShotGroup() {

        int numberOfShots = shotLocationList.size();
        double xSum = 0.0f;
        double ySum = 0.0f;

        for (Point3d shotLocation : shotLocationList) {

            xSum += shotLocation.x;
            ySum = +shotLocation.y;
        }

        return new Point3d(xSum / numberOfShots, ySum / numberOfShots, 0);
    }

    /**
     * Returns the mean distance of the received shots to the center of the shot
     * group. This is the measure of precision and reflects the mean dispersion
     * across all shots with respect to the center of the shot group.
     * 
     * @return the mean distance of the received shots to the center of the shot
     *         group
     */
    private double getMeanDistanceOfShotsFromShotGroupCenter() {

        Double tempDistance = 0.0;

        Point3d centerOfShotGroup = getCenterOfShotGroup();
        double xDist = 0.0;
        double yDist = 0.0;

        if(logger.isDebugEnabled()){
            logger.debug("Center of shot group: " + centerOfShotGroup);
        }

        for (Point3d shotLocation : shotLocationList) {

            xDist = (shotLocation.x - centerOfShotGroup.x)
                    * (shotLocation.x - centerOfShotGroup.x);
            yDist = (shotLocation.y - centerOfShotGroup.y)
                    * (shotLocation.y - centerOfShotGroup.y);

            tempDistance += Math.sqrt(xDist + yDist);
        }

        return tempDistance / shotLocationList.size();
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
