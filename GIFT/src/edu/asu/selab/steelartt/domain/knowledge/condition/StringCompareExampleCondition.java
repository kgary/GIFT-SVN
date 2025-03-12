package mil.arl.gift.domain.knowledge.condition;

import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.io.File;

import javax.vecmath.Point3d;
import javax.vecmath.Vector2d;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generated.dkf.LearnerActionEnumType;
import generated.dkf.RealTimeAssessmentRules;
import mil.arl.gift.common.coordinate.CoordinateUtil;
import mil.arl.gift.common.coordinate.GDC;
import mil.arl.gift.common.course.dkf.ConditionDescription;
import mil.arl.gift.common.course.dkf.ConditionDescription.FileDescription;
import mil.arl.gift.common.course.dkf.team.TeamMember;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.ta.request.VariablesStateRequest;
import mil.arl.gift.common.ta.request.VariablesStateRequest.VARIABLE_TYPE;
import mil.arl.gift.common.ta.request.VariablesStateRequest.VariableInfo;
import mil.arl.gift.common.ta.state.EntityIdentifier;
import mil.arl.gift.common.ta.state.EntityState;
import mil.arl.gift.common.ta.state.VariablesStateResult;
import mil.arl.gift.common.ta.state.VariablesState.VariableState;
import mil.arl.gift.common.ta.state.VariablesState.WeaponState;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.domain.knowledge.condition.SessionConditionsBlackboardMgr.ConditionEntityState;
import mil.arl.gift.net.api.message.Message;


public class MuzzleFlaggingCondition extends AbstractCondition {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(StringCompareExampleCondition.class);

    private static final ConditionDescription DESCRIPTION = new FileDescription(
            Paths.get("docs", "conditions", "StringMatching.GIFT Domain condition description.html").toFile(),
            "Muzzle Flagging");
    
    /**
     * contains the types of GIFT messages this condition needs in order to provide assessments
     */
    private static List<MessageTypeEnum> simulationInterests;   
    static{
        simulationInterests = new ArrayList<MessageTypeEnum>();
        simulationInterests.add(MessageTypeEnum.SIMPLE_EXAMPLE_STATE);
    }
    
    @Override
    public ConditionDescription getDescription() {
        return DESCRIPTION;
    }

    

    @Override
    public List<MessageTypeEnum> getSimulationInterests() {
        return simulationInterests;
    }

    /** a key to look for in the game state message */
    private String conditionKey;

    /**
     * Default constructor - required for authoring logic
    */
    public StringCompareExampleCondition (){

    }
    
    /**
     * Class constructor
     *
     * @param input configuration parameters for this condition
    */
    public StringCompareExampleCondition (generated.dkf.GenericConditionInput input){

        //assuming that there are only 1 name:value pair in the list
        List<generated.dkf.Nvpair> pairs = input.getNvpair();
        generated.dkf.Nvpair pair = pairs.get(0);

        //set this condition's key value which will be used for string matching
        //with incoming game state messages
        conditionKey = pair.getValue();

    }

    @Override
    public AssessmentLevelEnum handleTrainingAppGameState(Message message) {

        AssessmentLevelEnum level = null;

        //since this class is only registered to receive 1 type of message, casting w/o checking type
        SimpleExampleState state = (SimpleExampleState)message.getPayload();
        if(state.getVar().equals(conditionKey)){
            //found the condition key in the game state message content

            logger.info("Found "+conditionKey+" in the ExampleState game state message.");

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

        return level;
   }
}
