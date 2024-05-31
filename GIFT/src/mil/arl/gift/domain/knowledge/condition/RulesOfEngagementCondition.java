/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.condition;

import generated.dkf.LearnerActionEnumType;
import generated.dkf.WeaponControlStatusEnum;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.course.dkf.ConditionDescription;
import mil.arl.gift.common.course.dkf.ConditionDescription.FileDescription;
import mil.arl.gift.common.course.dkf.team.TeamMember;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.ta.state.EntityIdentifier;
import mil.arl.gift.common.ta.state.WeaponFire;
import mil.arl.gift.net.api.message.Message;

//TODO: this doesn't handle grenades because this class needs to look at detonations as well
/**
 * This condition checks the rules of engagement (ROE) 
 * 
 * @author mhoffman
 */
public class RulesOfEngagementCondition extends AbstractCondition {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(RulesOfEngagementCondition.class);
    
    /** the default assessment for this condition */
    private static final AssessmentLevelEnum DEFAULT_ASSESSMENT = AssessmentLevelEnum.AT_EXPECTATION;
    
    /** how long to wait (seconds) between weapon fire bursts before assessing again */
    private static final Long REASSESSMENT_TIME = 2500L;
    
    /**
     * contains the types of GIFT messages this condition needs in order to provide assessments
     */
    private static final List<MessageTypeEnum> simulationInterests;    
    static{
        simulationInterests = new ArrayList<MessageTypeEnum>();
        simulationInterests.add(MessageTypeEnum.WEAPON_FIRE);
    }
    
    /**
     * contains the type of overall assessment scorers this condition can populate for an AAR
     */
    private static final Set<Class<?>> overallAssessmentTypes;
    static{
        overallAssessmentTypes = new HashSet<Class<?>>();
        overallAssessmentTypes.add(generated.dkf.Count.class);
    }
    
    /** information about the purpose of this condition */
    private static final ConditionDescription DESCRIPTION = new FileDescription(new File("docs" + File.separator + "conditions" + File.separator + "RulesOfEngagement.GIFT Domain condition description.html"), "Rules of engagement");

    /** the wcs configuration for this condition */
    private WeaponControlStatusEnum status;
    
    /**
     * Default constructor - required for authoring logic
     */
    public RulesOfEngagementCondition(){
        super(DEFAULT_ASSESSMENT, REASSESSMENT_TIME);
    }

    /**
     * Class constructor
     * 
     * @param wcs the rules of engagement condition
     */
    public RulesOfEngagementCondition(WeaponControlStatusEnum wcs){
        this();
        
        this.status = wcs;
        
        updateAssessment(DEFAULT_ASSESSMENT);
    }
    
    /**
     * Class constructor - set attributes from dkf content
     * 
     * @param roe - dkf content for this condition
     */
    public RulesOfEngagementCondition(generated.dkf.RulesOfEngagementCondition roe){
        this();
        
        this.status = roe.getWcs().getValue();
        
        //save any authored real time assessment rules
        if(roe.getRealTimeAssessmentRules() != null){            
            addRealTimeAssessmentRules(roe.getRealTimeAssessmentRules());
        }
        
        if(roe.getTeamMemberRefs() != null){
            setTeamMembersBeingAssessed(roe.getTeamMemberRefs());
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
    public boolean handleTrainingAppGameState(Message message){
        
        //really only interested in weapon fire messages
        if(message.getMessageType() == MessageTypeEnum.WEAPON_FIRE){
            
            WeaponFire fire = (WeaponFire)message.getPayload();
            
            if(logger.isDebugEnabled()){
                logger.debug("Received message of "+fire);
            }
            
            EntityIdentifier entityId = fire.getFiringEntityID();
            TeamMember<?> teamMember = isConditionAssessedTeamMember(entityId);
            if(teamMember == null){
                //TODO: analyze target of fire for learner to enable tight wcs condition
                return false;
            }
            
            AssessmentLevelEnum level = null;
            int quantity = fire.getBurstDescriptor().getQuantity();
            
            if(status == WeaponControlStatusEnum.TIGHT){
                //TODO: determine if the weapon fire is from learner and if its warranted
                
            }else if(status == WeaponControlStatusEnum.HOLD){
                
                if(logger.isDebugEnabled()){
                    logger.debug("Condition is violated due to weapon control status of "+status);
                }
                
                addViolator(teamMember, entityId);
                scoringEventStarted(quantity);

                AssessmentLevelEnum authoredLevel = getAuthoredRealTimeAssessment();
                if(authoredLevel != null){
                    //one of the authored assessment rules has been satisfied
                    level = authoredLevel;                
                }else{
                    level = AssessmentLevelEnum.BELOW_EXPECTATION;
                }               

            }else if(status == WeaponControlStatusEnum.FREE){
                
                if(logger.isDebugEnabled()){
                    logger.debug("Learner is free to shoot because the condition's weapon control status is "+status);
                }
                
                AssessmentLevelEnum authoredLevel = getAuthoredRealTimeAssessment();
                if(authoredLevel != null){
                    //one of the authored assessment rules has been satisfied
                    level = authoredLevel;                
                }else{
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
        
        return false;
    }
    
    /**
     * Set the condition's assessment explanation based on the team members being assessed on this condition
     * and are currently violating the condition parameters.
     * @return true if the assessment explanation value for this condition changed during this method.
     */
    private boolean setAssessmentExplanation(){
        
        //update assessment explanation
        Set<TeamMember<?>> violators = buildViolatorsInfo();
        boolean changed = false;
        if(violators.isEmpty()){
            changed = assessmentExplanation != null;
            assessmentExplanation = null;
        }else{
            StringBuilder assessmentExplanationBuilder = new StringBuilder();
            Iterator<TeamMember<?>> itr = violators.iterator();
            assessmentExplanationBuilder.append("{");
            while(itr.hasNext()){
                TeamMember<?> violator = itr.next();
                assessmentExplanationBuilder.append(violator.getName());
                if(itr.hasNext()){
                    assessmentExplanationBuilder.append(", ");
                }
            }
            assessmentExplanationBuilder.append("} violated the rules of engagement of ").append(status.value());   
            
            String newAssessmentExplanation = assessmentExplanationBuilder.toString();
            changed = !newAssessmentExplanation.equals(assessmentExplanation);
            assessmentExplanation = newAssessmentExplanation;
        }
        
        return changed;
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
        return false;
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[RulesOfEngagementCondition: ");
        sb.append(super.toString());
        sb.append(", status = ").append(status);
        sb.append("]");
        
        return sb.toString();
    }
    
}
