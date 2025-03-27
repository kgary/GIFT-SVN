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
import mil.arl.gift.common.ta.state.SimpleExampleState;


public class SteelarttCondition extends AbstractCondition {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(SteelarttCondition.class);

    /** the authored parameters for this condition */
    private generated.dkf.SteelarttConditionInput steelarttInput = null;


    /** the default assessment for this condition when no location based assessment has taken place yet */
    private static final AssessmentLevelEnum DEFAULT_ASSESSMENT = AssessmentLevelEnum.AT_EXPECTATION;

    private static final ConditionDescription DESCRIPTION = new FileDescription(
            Paths.get("docs", "conditions", "StringMatching.GIFT Domain condition description.html").toFile(),
            "Steelartt Condition");
    
    /**
     * contains the types of GIFT messages this condition needs in order to provide assessments
     */
    private static List<MessageTypeEnum> simulationInterests;   
    static{
        simulationInterests = new ArrayList<MessageTypeEnum>();
        simulationInterests.add(MessageTypeEnum.ENTITY_STATE);
        // probably add all the other steelartt messagetypeEnums
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

    @Override
    public ConditionDescription getDescription() {
        return DESCRIPTION;
    }

    @Override
    public Set<Class<?>> getOverallAssessmenTypes() {
        return overallAssessmentTypes;
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
    public boolean canComplete() {
        return false;
    }

    /** a key to look for in the game state message */
    private String conditionKey;

    /**
     * Default constructor - required for authoring logic
    */
    public SteelarttCondition (){

    }
    
    /**
     * Class constructor
     *
     * @param input configuration parameters for this condition
    */
    public SteelarttCondition (generated.dkf.SteelarttConditionInput input){
        
        this.steelarttInput = input;
        if(steelarttInput.getWoundIdentified().isEmpty()){
            throw new IllegalArgumentException("There are no wound-identification inputs");
        }

        //for this conition rn -lets assume no real time assessment rules, hence commenting.
        // if(steelarttInput.getRealTimeAssessmentRules() != null){
        //     addRealTimeAssessmentRules(steelarttInput.getRealTimeAssessmentRules());
        // }
        // 
        // AssessmentLevelEnum authoredLevel = getAuthoredRealTimeAssessment();
        // if(authoredLevel != null){
        //     //set the initial assessment to the authored real time assessment value
        //     updateAssessment(authoredLevel);
        // }else{
        //     updateAssessment(DEFAULT_ASSESSMENT);
        // }

        // setting the team references which 
        if(steelarttInput.getTeamMemberRefs() != null){
            setTeamMembersBeingAssessed(steelarttInput.getTeamMemberRefs());
        }

    }

    @Override
    public boolean handleTrainingAppGameState(Message message) {

        AssessmentLevelEnum level = null;
        if (message.getMessageType() == MessageTypeEnum.ENTITY_STATE) {
            // for now entitystate, but later add scenario-def or timer or triage status etc
            EntityState entityState = (EntityState) message.getPayload();

            //only re-assess this condition if the entity state message describes the learner
            TeamMember<?> teamMember = isConditionAssessedTeamMember(entityState.getEntityID());
            if(teamMember == null){
                return false;
            }

            // if(evaluateCondition(entityState.what???))
        }

        // SimpleExampleState state = (SimpleExampleState)message.getPayload();
        // if(state.getVar().equals(conditionKey)){
        //     logger.info("Found "+conditionKey+" in the ExampleState game state message.");
        //     scoringEventStarted();
        //     level = AssessmentLevelEnum.AT_EXPECTATION;
        //     updateAssessment(level);

        // }else{
        //     scoringEventEnded();
        //     level = AssessmentLevelEnum.BELOW_EXPECTATION;
        //     updateAssessment(level);
        // }

        return true;
   }
}
