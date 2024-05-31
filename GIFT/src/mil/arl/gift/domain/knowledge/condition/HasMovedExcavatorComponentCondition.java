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
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generated.dkf.ExcavatorComponentEnum;
import generated.dkf.LearnerActionEnumType;
import mil.arl.gift.common.course.dkf.ConditionDescription;
import mil.arl.gift.common.course.dkf.ConditionDescription.FileDescription;
import mil.arl.gift.common.enums.ArticulationParameterTypeDesignatorEnum;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.ta.state.ArticulationParameter;
import mil.arl.gift.common.ta.state.EntityState;
import mil.arl.gift.net.api.message.Message;

/**
 * This condition checks if a specific Excavator component has been moved. 
 * 
 * @author mhoffman
 */
public class HasMovedExcavatorComponentCondition extends AbstractCondition {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(HasMovedExcavatorComponentCondition.class);
    
    private static final AssessmentLevelEnum DEFAULT_ASSESSMENT = AssessmentLevelEnum.BELOW_EXPECTATION;
    private static final AssessmentLevelEnum SUCCESS_ASSESSMENT = AssessmentLevelEnum.ABOVE_EXPECTATION;
    
    private static final float PI_OVER_TWO = 0.5f * (float)Math.PI;
    private static final float PI_OVER_FOUR = 0.25f * (float)Math.PI;
    
    private static final float SWING_ANGLE_ACCUMULATION_SUCCESS_THRESHOLD_DEFAULT = PI_OVER_FOUR;
    private static final float BOOM_ANGLE_ACCUMULATION_SUCCESS_THRESHOLD_DEFAULT = PI_OVER_FOUR;
    private static final float ARM_ANGLE_ACCUMULATION_SUCCESS_THRESHOLD_DEFAULT = PI_OVER_TWO;
    private static final float BUCKET_ANGLE_ACCUMULATION_SUCCESS_THRESHOLD_DEFAULT = PI_OVER_TWO;
    
    /** 
     * amount of milliseconds to wait between assessments created by this condition
     * this prevents a rapid fire of assessments when the max assessments is greater than one
     * due to the high frequency of entity state from the DE Testbed application (60Hz)
     */
    private static final long REASSESS_DELAY = 3000;

    
    /**
     * contains the types of GIFT messages this condition needs in order to provide assessments
     */
    private static final List<MessageTypeEnum> simulationInterests;    
    static{
        simulationInterests = new ArrayList<MessageTypeEnum>();
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
    private static final ConditionDescription DESCRIPTION = new FileDescription(new File("docs" + File.separator + "conditions" + File.separator + "HasMovedExcavatorComponent.GIFT Domain condition description.html"), "Move Excavator Component");

    /** the wcs for this condition */
    private generated.dkf.HasMovedExcavatorComponentInput hasMovedInput;
    
    private Map<ExcavatorComponentEnum, ComponentWrapper> componentTypeToWrapper = new HashMap<>();

    /** whether or not the condition is finished being assessed */
    private boolean hasPassed = false;
    
    /** 
     * the maximum number of assessments (i.e. Above Expectation) being returned by this condition 
     * This is configurable by the DKF input for this condition.
     * Once the assessment count reaches the max assessments, the condition is completed.
     */
    private int maxAssessments = 1;
    private int assessmentsCnt = 0;
    
    /** used to separate assessments by a delay to prevent a rapid fire of assessment values */
    private Timer reassessTimer = new Timer("Re-assess Timer");
    
    /**
     * Default constructor - required for authoring logic
     */
    public HasMovedExcavatorComponentCondition(){
        super(DEFAULT_ASSESSMENT);
    }

    /**
     * Class constructor
     * 
     * @param hasMovedInput the input for this condition which contains the component being checked
     */
    public HasMovedExcavatorComponentCondition(generated.dkf.HasMovedExcavatorComponentInput hasMovedInput){
        this();
        
        this.hasMovedInput = hasMovedInput;
        
        //process input
        for(generated.dkf.HasMovedExcavatorComponentInput.Component component : hasMovedInput.getComponent()){
            
            if(componentTypeToWrapper.containsKey(component.getComponentType())){
                //ERROR
                throw new IllegalArgumentException("Found two instances of the component type "+component.getComponentType()+" for a condition input.");
            }
            
            ComponentWrapper wrapper = new ComponentWrapper();
            wrapper.componentInfo = component;
            
            componentTypeToWrapper.put(component.getComponentType(), wrapper);    

        }
        
        if (this.hasMovedInput.getMaxAssessments() != null) {       	
        	this.maxAssessments = this.hasMovedInput.getMaxAssessments().intValue();
        }

        setAngleThresholds();
    }
    
    // team member refs not allowed as an input
    @Override
    public String hasValidTeamMemberRefs(){
        return null;
    }
 
    /**
     * Set the threshold for how many degrees this condition's component must rotate in order for the assessment to be satisfied.
     * This threshold is specified by the user in the condition's input in the dkf.
     */
    private void setAngleThresholds() {
    	
        for(ComponentWrapper componentWrapper : componentTypeToWrapper.values()){
            
            generated.dkf.HasMovedExcavatorComponentInput.Component component = componentWrapper.componentInfo;
            
            if (component.getDirectionType() != null) {
                
                if (component.getDirectionType() 
                        instanceof generated.dkf.HasMovedExcavatorComponentInput.Component.Bidirectional) {
                    
                    // The component must move a certain amount of degrees in each direction  
                    // in order for the assessment to rise above expectation.
                    generated.dkf.HasMovedExcavatorComponentInput.Component.Bidirectional bidirectionalValues = 
                            (generated.dkf.HasMovedExcavatorComponentInput.Component.Bidirectional)component.getDirectionType();
                    
                    componentWrapper.POSITIVE_ANGLE_ACCUMULATION_SUCCESS_THRESHOLD = (float)Math.toRadians(bidirectionalValues.getPositiveRotation());
                    componentWrapper.NEGATIVE_ANGLE_ACCUMULATION_SUCCESS_THRESHOLD = (float)Math.toRadians(bidirectionalValues.getNegativeRotation());
                    
                }else {
                    // The component needs to move a certain amount of degrees in any direction
                    // in order for the assessment to rise above expectation.
                    componentWrapper.anyDirection = true;
                    
                    double anyDirectionValue = (Double)component.getDirectionType();
        
                    componentWrapper.POSITIVE_ANGLE_ACCUMULATION_SUCCESS_THRESHOLD = (float)Math.toRadians(anyDirectionValue);
                    componentWrapper.NEGATIVE_ANGLE_ACCUMULATION_SUCCESS_THRESHOLD = 0; // this is not used if anyDirection is selected
                }
                
            }else {
                // No threshold specified by user.  Use defaults
                ExcavatorComponentEnum componentType = component.getComponentType();    
                
                if (componentType == ExcavatorComponentEnum.ARM) {
                    
                    componentWrapper.POSITIVE_ANGLE_ACCUMULATION_SUCCESS_THRESHOLD = ARM_ANGLE_ACCUMULATION_SUCCESS_THRESHOLD_DEFAULT;
                    componentWrapper.NEGATIVE_ANGLE_ACCUMULATION_SUCCESS_THRESHOLD = ARM_ANGLE_ACCUMULATION_SUCCESS_THRESHOLD_DEFAULT;
                    
                }else if (componentType == ExcavatorComponentEnum.BOOM) {
                    
                    componentWrapper.POSITIVE_ANGLE_ACCUMULATION_SUCCESS_THRESHOLD = BOOM_ANGLE_ACCUMULATION_SUCCESS_THRESHOLD_DEFAULT;
                    componentWrapper.NEGATIVE_ANGLE_ACCUMULATION_SUCCESS_THRESHOLD = BOOM_ANGLE_ACCUMULATION_SUCCESS_THRESHOLD_DEFAULT;
                    
                }else if (componentType == ExcavatorComponentEnum.BUCKET) {
                    
                    componentWrapper.POSITIVE_ANGLE_ACCUMULATION_SUCCESS_THRESHOLD = BUCKET_ANGLE_ACCUMULATION_SUCCESS_THRESHOLD_DEFAULT;
                    componentWrapper.NEGATIVE_ANGLE_ACCUMULATION_SUCCESS_THRESHOLD = BUCKET_ANGLE_ACCUMULATION_SUCCESS_THRESHOLD_DEFAULT;
                    
                }else if (componentType == ExcavatorComponentEnum.SWING) {
                    
                    componentWrapper.POSITIVE_ANGLE_ACCUMULATION_SUCCESS_THRESHOLD = SWING_ANGLE_ACCUMULATION_SUCCESS_THRESHOLD_DEFAULT;
                    componentWrapper.NEGATIVE_ANGLE_ACCUMULATION_SUCCESS_THRESHOLD = SWING_ANGLE_ACCUMULATION_SUCCESS_THRESHOLD_DEFAULT;
                }
            }
        }
		
    }
    
    /**
     * Normalize an angle to (-PI, PI)
     * @param angle Input angle from [-2PI, 2PI]
     * @return Normalize angle from (-PI, PI)
     */
    double normalizeAngle(double angle) {
        
        if(angle > Math.PI) {

            return angle - (2.0 * Math.PI);
        }
        else if (angle < -Math.PI) {
            
            return angle + (2.0 * Math.PI);
        }

        return angle;
    }

    /**
     * Calculates the signed change between two angles
     * @param angleA The first angle
     * @param angleB The second Angle
     * @return Signed value representing the change between angleA and angleB
     */
    double calculateAngleDelta(double angleA, double angleB) {

        double angle = normalizeAngle(angleB) - normalizeAngle(angleA);
        angle = normalizeAngle(angle);
        return angle;
    }

    /**
     * Updates the angle delta accumulation values and asses if the thresholds have been passed
     * @param currentAngle The parts current angle
     * @param positiveAngleAccumulationSuccessThreshold Positive angle threshold to compare against
     * @param negativeAngleAccumulationSuccessThreshold Negative angle threshold to compare against
     * @return ABOVE_EXPECTATION if both thresholds have been passed, otherwise null
     */
    AssessmentLevelEnum updateAngleAccumulationAndAssess(float currentAngle,
                                                         ComponentWrapper componentWrapper) {

        double angleDelta = this.calculateAngleDelta(componentWrapper.previousAngle, currentAngle);
        if(angleDelta > 0) {

            componentWrapper.positiveAngleAccumulation += angleDelta;
            
        }else if(angleDelta < 0) {

            componentWrapper.negativeAngleAccumulation += angleDelta;
        }
        
        if (!componentWrapper.anyDirection) {
        	// Determine if the bidirectional threshold has been satisfied
        	if (componentWrapper.positiveAngleAccumulation >= componentWrapper.POSITIVE_ANGLE_ACCUMULATION_SUCCESS_THRESHOLD &&
        	        componentWrapper.negativeAngleAccumulation <= -componentWrapper.NEGATIVE_ANGLE_ACCUMULATION_SUCCESS_THRESHOLD) {
        	    
        	    if(logger.isInfoEnabled()){
        	        logger.info("The "+componentWrapper.componentInfo.getComponentType()+" has reached its bidirectional movement thresholds: positive -> "+
        	            componentWrapper.positiveAngleAccumulation+"(actual) >= "+componentWrapper.POSITIVE_ANGLE_ACCUMULATION_SUCCESS_THRESHOLD+"(threshold), negative ->"+
        	            componentWrapper.negativeAngleAccumulation+"(actual) >= "+componentWrapper.NEGATIVE_ANGLE_ACCUMULATION_SUCCESS_THRESHOLD+"(threshold)");
        	    }
                
                return SUCCESS_ASSESSMENT;
            }
        	
        }else {
        	// Determine if the anyDirection threshold has been satisfied.
        	if ((componentWrapper.positiveAngleAccumulation + -componentWrapper.negativeAngleAccumulation) >= componentWrapper.POSITIVE_ANGLE_ACCUMULATION_SUCCESS_THRESHOLD) {
        		
        	    if(logger.isInfoEnabled()){
        	        logger.info("The "+componentWrapper.componentInfo.getComponentType()+" has reached its any direction movement thresholds: "+
        	            (componentWrapper.positiveAngleAccumulation + -componentWrapper.negativeAngleAccumulation)+"(actual) >= "+componentWrapper.POSITIVE_ANGLE_ACCUMULATION_SUCCESS_THRESHOLD+"(threshold)");
        	    }
        	    
        		return SUCCESS_ASSESSMENT;
        	}  	
        }
            
        return null;
    }
    
    @Override
    public boolean handleTrainingAppGameState(Message message){
        
        AssessmentLevelEnum level = null;
        
        if(message.getMessageType() == MessageTypeEnum.ENTITY_STATE){

            EntityState eState = (EntityState)message.getPayload();
            
            if(logger.isDebugEnabled()){
                //logger.debug("Received message of "+eState);
            }
            
            if(!this.hasPassed) {
                List<ArticulationParameter> artParams = eState.getArticulationParameters();
                for(ArticulationParameter artParam : artParams) {

                    if(artParam.getParameterTypeDesignator() == ArticulationParameterTypeDesignatorEnum.ARTICULATED_PART) {
                       
                        int parameterTypeLowBits = artParam.decodeParameterTypeLowBits();
                        if(parameterTypeLowBits == 11) { // Azimuth

                            float angle = artParam.decodeParameterValue();
                            int parameterTypeHighBits = artParam.decodeParameterTypeHighBits();
                            switch(parameterTypeHighBits) {

                            case 4096: // Primary turret number 1
                                if(componentTypeToWrapper.containsKey(ExcavatorComponentEnum.SWING)) {
                                    
                                    ComponentWrapper componentWrapper = componentTypeToWrapper.get(ExcavatorComponentEnum.SWING);
                                    if(!componentWrapper.hasReceivedInitialAngle) {

                                        componentWrapper.previousAngle = angle;
                                        componentWrapper.hasReceivedInitialAngle = true;
                                    }
                                    
                                    level = updateAngleAccumulationAndAssess(angle,
                                            componentWrapper);

                                    componentWrapper.previousAngle = angle;
                                }
                                break;
                            default:
                                logger.warn("Invalid articulation parameter type high bits: "+parameterTypeHighBits);
                                break;
                            }
                        }
                        else if(parameterTypeLowBits == 13) { // Elevation

                            float angle = artParam.decodeParameterValue();
                            int parameterTypeHighBits = artParam.decodeParameterTypeHighBits();
                            switch(parameterTypeHighBits) {

                            case 4416: // Primary gun number 1
                                if(componentTypeToWrapper.containsKey(ExcavatorComponentEnum.BOOM)) {

                                    ComponentWrapper componentWrapper = componentTypeToWrapper.get(ExcavatorComponentEnum.BOOM);
                                    if(!componentWrapper.hasReceivedInitialAngle) {
                                        
                                        componentWrapper.previousAngle = angle;
                                        componentWrapper.hasReceivedInitialAngle = true;
                                    }
                                    
                                    level = updateAngleAccumulationAndAssess(angle,
                                            componentWrapper);
                                    
                                    componentWrapper.previousAngle = angle;
                                }
                                break;
                            case 4448: // Primary gun number 2
                                if(componentTypeToWrapper.containsKey(ExcavatorComponentEnum.ARM)) {

                                    ComponentWrapper componentWrapper = componentTypeToWrapper.get(ExcavatorComponentEnum.ARM);
                                    if(!componentWrapper.hasReceivedInitialAngle) {
                                        
                                        componentWrapper.previousAngle = angle;
                                        componentWrapper.hasReceivedInitialAngle = true;
                                    }
                                    
                                    level = updateAngleAccumulationAndAssess(angle,
                                            componentWrapper);
                                    
                                    componentWrapper.previousAngle = angle;
                                }
                                break;
                            case 4480: // Primary gun number 3
                                if(componentTypeToWrapper.containsKey(ExcavatorComponentEnum.BUCKET)) {

                                    ComponentWrapper componentWrapper = componentTypeToWrapper.get(ExcavatorComponentEnum.BUCKET);
                                    if(!componentWrapper.hasReceivedInitialAngle) {
                                        
                                        componentWrapper.previousAngle = angle;
                                        componentWrapper.hasReceivedInitialAngle = true;
                                    }
                                    
                                    level = updateAngleAccumulationAndAssess(angle,
                                            componentWrapper);
                                    
                                    componentWrapper.previousAngle = angle;
                                }
                                break;
                            default:
                                logger.warn("Invalid articulation parameter type high bits: "+parameterTypeHighBits);
                                break;
                            }
                        }
                    }
                    
                    if(level != null){
                        break;
                    }
                }
            }
           
            if(level != null) {

                this.scoringEventStarted();
                this.updateAssessment(level); 
                
                assessmentsCnt++;
                
                if(assessmentsCnt == maxAssessments){
                    this.conditionCompleted();
                    this.hasPassed = true;  //so this condition isn't assessed again
                }else{
                    //reset angle accumulation
                    resetComponents();
                    
                    //delay the next accumulation a little bit to prevent
                    //rapid assessments by this condition, especially when entity states are coming
                    //at 60Hz from DE Testbed application
                    //Note: this can only be reached upon the first assessment level being set (i.e. not null)
                    //      OR the a previously schedule timer was fired causing the hasPassed to be false causing
                    //      another assessment to be created.
                    this.hasPassed = true;  //so this condition isn't assessed again... until the timer expires
                    reassessTimer.schedule(new TimerTask() {
                        
                        @Override
                        public void run() {
                            hasPassed = false;   //so this condition will start assessing again                         
                        }
                    }, REASSESS_DELAY);
                }
                
                return true;               
            }
        }
        
        return false;
    }
    
    private void resetComponents(){
        
        for(ComponentWrapper componentWrapper : componentTypeToWrapper.values()){
            componentWrapper.positiveAngleAccumulation = 0;
            componentWrapper.negativeAngleAccumulation = 0;
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
        sb.append("[HasMovedExcavatorComponentCondition: ");
        sb.append(super.toString());
        sb.append(", configuration = ").append(hasMovedInput);
        sb.append("]");
        
        return sb.toString();
    }
    
    /**
     * This inner class is used to wrap the variables tied to a specific excavator component being tracked
     * for this condition.
     * 
     * @author mhoffman
     *
     */
    private static class ComponentWrapper{
        
        boolean hasReceivedInitialAngle = false;
        
        float POSITIVE_ANGLE_ACCUMULATION_SUCCESS_THRESHOLD = 0;
        float NEGATIVE_ANGLE_ACCUMULATION_SUCCESS_THRESHOLD = 0;
        
        float previousAngle;

        float positiveAngleAccumulation;
        float negativeAngleAccumulation;
        
        /** 
         * whether the accumulation of component movement is bidirectional 
         * (i.e. positive and negative around a single articulation point) 
         * or any direction (i.e. both positive and negative movement is accumulated under one value)
         */
        boolean anyDirection = false;
        
        /**
         * the DKF authored condition input to use for configuration this components angle thresholds
         */
        generated.dkf.HasMovedExcavatorComponentInput.Component componentInfo; 
    }
    
}
