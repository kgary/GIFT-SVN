/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.condition.simile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mil.arl.gift.common.course.dkf.ConditionDescription;
import mil.arl.gift.common.course.dkf.ConditionDescription.InlineDescription;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.enums.DeploymentModeEnum;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.ta.state.Detonation;
import mil.arl.gift.common.ta.state.EntityState;
import mil.arl.gift.common.ta.state.GenericJSONState;
import mil.arl.gift.common.ta.state.WeaponFire;
import mil.arl.gift.domain.DomainModuleProperties;
import mil.arl.gift.domain.knowledge.condition.AbstractCondition;
import mil.arl.gift.net.api.message.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generated.dkf.LearnerActionEnumType;

/**
 * This class is responsible for communicating with the Simile assessment engine
 * for a GIFT condition. The Simile engine will assess this condition and reply
 * with results that can be used in the Task/Concept performance node hierarchy.
 * 
 * @author asanchez
 * 
 */
public class SIMILEInterfaceCondition extends AbstractCondition {
    /**************************
     * Private Fields *
     **************************/
    
    private static final InlineDescription DESCRIPTION = new InlineDescription("Uses the SIMILE assessment engine to assess game state "+
            "messages based on rules defined in an ixs file authored using the SIMILE workbench.", "SIMILE");

    /**
     * Contains the types of GIFT messages this condition needs in order to
     * provide assessments.
     */
    private static final List<MessageTypeEnum> _SimulationInterests;
    static {
    	_SimulationInterests = new ArrayList<MessageTypeEnum>();
    	// _SimulationInterests.add( MessageTypeEnum.TC3_GAME_STATE );
    	_SimulationInterests.add(MessageTypeEnum.GENERIC_JSON_STATE);
    	_SimulationInterests.add(MessageTypeEnum.ENTITY_STATE);
    	_SimulationInterests.add(MessageTypeEnum.WEAPON_FIRE);
    	_SimulationInterests.add(MessageTypeEnum.DETONATION);
    	_SimulationInterests.add(MessageTypeEnum.STOP_FREEZE);
    }
    
    /**
     * contains the type of overall assessment scorers this condition can populate for an AAR
     */
    private static final Set<Class<?>> overallAssessmentTypes;
    static{
        overallAssessmentTypes = new HashSet<Class<?>>();
    }

    /**
     * Simile interface instance.
     */
    private Simile _Simile = null;

    /**
     * Input configuration from the DKF which caused this class to be
     * instantiated.
     */
    private generated.dkf.SIMILEConditionInput _Input;

    /**
     * Instance of the logger.
     */
    private static Logger logger = LoggerFactory
	    .getLogger(SIMILEInterfaceCondition.class);

    /******************************
     * Public Constructor *
     ******************************/
    
    /** Required to have a no arg constructor for authoring purposes */
    public SIMILEInterfaceCondition() {
        _Simile = Simile.GetInstance();
    }

    /**
     * Class constructor.
     * 
     * @param input
     *            - Input parameters used to configure the Simile assessment
     *            engine.
     */
    public SIMILEInterfaceCondition(generated.dkf.SIMILEConditionInput input) {
    	_Simile = Simile.GetInstance();

    	_Input = input;
    }
    
    // team member refs not allowed as an input
    @Override
    public String hasValidTeamMemberRefs(){
        return null;
    }
    
    @Override
    public void setCourseFolder(mil.arl.gift.common.io.AbstractFolderProxy courseFolder) {
        super.setCourseFolder(courseFolder);
        
        try{
            _Simile.SetupSimileForGIFT(this, _Input, courseFolder);
        }catch(IOException ioException){
            throw new RuntimeException(ioException);
        }
    }
    
    @Override
    public void initialize(mil.arl.gift.domain.knowledge.common.ConditionActionInterface conditionActionInterface) {
        super.initialize(conditionActionInterface);
        
        //Currently the GIFT SIMILE interaction doesn't support concurrent uses of the same SIMILE Rule name, therefore
        //we should limit the use of this condition to only desktop mode
        if(DomainModuleProperties.getInstance().getDeploymentMode() == DeploymentModeEnum.SERVER){
            throw new DetailedException("Failed to initialize the SIMILE assessment condition", 
                    "The SIMILE assessment condition (SIMILEInterfaceCondition.java) doesn't support concurrent uses of the same named SIMILE rule.  For this reason we have decided to limit the use of SIMILE to more control environments like GIFT desktop deployments.  If you would like to use SIMILE to assess performance, please run GIFT on your computer in Desktop mode.", 
                    null);
        }
        
        _Simile.initialize();
    }

    /**************************
     * Public Methods *
     **************************/

    /**
     * Handles the messages sent from the training application to Simile.
     * 
     * @param message - The simulation message we need to send to Simile from the training application.
     * @return AssessmentLevelEnum - Returns the feedback we got from Simile.
     *         This is always null since the feedback we get from Simile will be
     *         handled by the triggered rule callback.
     */
    @Override
    public boolean handleTrainingAppGameState(Message message) {
    	if (_Simile == null) {
    	    logger.error("The Simile object is null while handling training game state messages!");
    	    return false;
    	}
    
    	// This is where TC3 game states will come in when domain module
    	// receives them.
    	if (message.getMessageType() == MessageTypeEnum.GENERIC_JSON_STATE) {
    	    GenericJSONState gState = (GenericJSONState) message.getPayload();
    	    _Simile.ProcessMessage(gState);
    	}else if (message.getMessageType() == MessageTypeEnum.ENTITY_STATE) {
                GenericJSONState gState = GenericJSONState.fromEntityState((EntityState) message.getPayload());
                _Simile.ProcessMessage(gState);
        }else if (message.getMessageType() == MessageTypeEnum.WEAPON_FIRE) {
            GenericJSONState gState = GenericJSONState.fromWeaponFire((WeaponFire) message.getPayload());
            _Simile.ProcessMessage(gState);
        }else if (message.getMessageType() == MessageTypeEnum.DETONATION) {
            GenericJSONState gState = GenericJSONState.fromDetonation((Detonation) message.getPayload());
            _Simile.ProcessMessage(gState);
    	} else if (message.getMessageType() == MessageTypeEnum.STOP_FREEZE) {
    	    _Simile.StopSimile();
    	}
    
    	return false;
    }

    /**
     * Gets the list of the types of messages the Simile condition cares about.
     * 
     * @return List<MessageTypeEnum> - Returns the list of messages this
     *         condition listens to.
     */
    @Override
    public List<MessageTypeEnum> getSimulationInterests() {
        return _SimulationInterests;
    }

    /**
     * Stops the condition and calls the Simile stop command to clean up any
     * objects from the simulation.
     */
    @Override
    public void stop() {
    	if (_Simile == null) {
    	    logger.error("The Simile object is null while trying to force Simile to stop!");
    	    return;
    	}
    
    	_Simile.StopSimile();
    
    	super.stop();
    }

    /**********************************************
     * Public Simulation Results Callback *
     **********************************************/

    /**
     * When a result is determined (at any point in time) from Simile, this
     * method is called to report it to GIFT.
     * 
     * @param atExpectationAssessment
     *            - True if the triggered concept is an 'At Expectation'
     *            concept.
     * @param isSatisfied
     *            - True if the rule has been satisfied.
     * @param isConditionCompleted
     *            - True if the condition has been completed.
     */
    public void SimulationResults(Boolean atExpectationAssessment,
	    Boolean isSatisfied, Boolean isConditionCompleted) {
	/*
         System.out.println( _Input.getConditionKey().toString() +
         "--> At Expectation: " + atExpectationAssessment.toString() );
         System.out.println( _Input.getConditionKey().toString() +
         "--> Satisfied: " + isSatisfied.toString() );
         System.out.println( _Input.getConditionKey().toString() +
         "--> Completed: " + isConditionCompleted.toString() );
	 */
        
        if(conditionActionInterface == null){
            logger.error("Received a simulation result before the condition was initialized:\n"
                    + _Input.getConditionKey().toString() + "--> At Expectation: " + atExpectationAssessment.toString() + "\n"
                    + _Input.getConditionKey().toString() + "--> Satisfied: " + isSatisfied.toString() + "\n"
                    + _Input.getConditionKey().toString() + "--> Completed: " + isConditionCompleted.toString());
            return;
        }
        
        if(logger.isInfoEnabled()){
            logger.info("Received a simulation result:\n"
                    + _Input.getConditionKey().toString() + "--> At Expectation: " + atExpectationAssessment.toString() + "\n"
                    + _Input.getConditionKey().toString() + "--> Satisfied: " + isSatisfied.toString() + "\n"
                    + _Input.getConditionKey().toString() + "--> Completed: " + isConditionCompleted.toString());
        }

        if (isSatisfied) {
            // If the concept is an 'At Expectation' concept, then the user is
            // good; else the user falls below.
            updateAssessment(atExpectationAssessment ? AssessmentLevelEnum.AT_EXPECTATION
                : AssessmentLevelEnum.BELOW_EXPECTATION);
        } else {
            updateAssessment(atExpectationAssessment ? AssessmentLevelEnum.BELOW_EXPECTATION
                : AssessmentLevelEnum.AT_EXPECTATION);
        }

        // This callback pushes the new assessment up the task/concept
        // hierarchy.

        conditionActionInterface.conditionAssessmentCreated(this);

        if (isConditionCompleted && !hasCompleted()) {
            // Setting the condition to be completed allows the conceptEnded
            // task trigger to be fired (when appropriate).
            conditionCompleted();
        }

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
