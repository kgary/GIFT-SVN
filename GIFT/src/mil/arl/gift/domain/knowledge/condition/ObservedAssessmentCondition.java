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
import generated.dkf.TeamMemberRefs;
import mil.arl.gift.common.course.dkf.ConditionDescription;
import mil.arl.gift.common.course.dkf.ConditionDescription.FileDescription;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.domain.knowledge.VariablesHandler.ActorVariables;
import mil.arl.gift.domain.knowledge.VariablesHandler.TeamMemberActor;
import mil.arl.gift.net.api.message.Message;

/**
 * This condition is used to indicate that the concept will not involve GIFT automatically assessing
 * it and will instead be monitored manually by an instructor who will use the Game Master
 * application to provide real time assessment. Internally it is used to satisfy the requirement
 * that a leaf concept have a condition.
 * 
 * @author sharrison
 */
public class ObservedAssessmentCondition extends AbstractCondition {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(ObservedAssessmentCondition.class);

    /** information about the purpose of this condition */
    private static final ConditionDescription DESCRIPTION = new FileDescription(new File("docs" + File.separator
            + "conditions" + File.separator + "ObservedAssessment.GIFT Domain condition description.html"),
            "Observed Assessment");

    /**
     * contains the types of GIFT messages this condition needs in order to provide assessments
     */
    private static final List<MessageTypeEnum> simulationInterests;
    static {
        simulationInterests = new ArrayList<MessageTypeEnum>();
        simulationInterests.add(MessageTypeEnum.ENTITY_STATE);
    }
    
    /**
     * contains the type of overall assessment scorers this condition can populate for an AAR
     */
    private static final Set<Class<?>> overallAssessmentTypes;
    static{
        overallAssessmentTypes = new HashSet<Class<?>>();
    }

    /**
     * Default constructor - required for authoring logic
     */
    public ObservedAssessmentCondition() {
    }

    /**
     * Class constructor
     * 
     * @param observedAssessment - configuration parameter for this condition
     */
    public ObservedAssessmentCondition(generated.dkf.ObservedAssessmentCondition observedAssessment) {
        
        if(observedAssessment.getTeamMemberRefs() != null){
            setTeamMembersBeingAssessed(observedAssessment.getTeamMemberRefs());
        }
    }
    
    // Don't require the observed condition to specify team members even if there is a
    // team org defined.  For this condition the team member refs is provided to help the observer
    // know who to assess but the observer can change the team members at their discretion.
    @Override
    public String hasValidTeamMemberRefs(){
        return null;
    }

    @Override
    public boolean handleTrainingAppGameState(Message message) {
        // do nothing

        return false;
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
    public void assessmentUpdatedExternally() {
        
        if(logger.isDebugEnabled()){
            logger.debug("Received external update");
        }
        scoringEventStarted();
        
        /* Let any listeners know when an observed assessment has last occurred */
        TeamMemberRefs teamMembers = getTeamMembersBeingAssessed();
        if(teamMembers != null && !teamMembers.getTeamMemberRef().isEmpty()) {
            for(String teamMember : teamMembers.getTeamMemberRef()) {
                varsHandler.setVariable(new TeamMemberActor(teamMember), ActorVariables.OBSERVED_ASSESSMENT, System.currentTimeMillis()); 
            }
        
        } else {
            varsHandler.setVariable(new TeamMemberActor(null), ActorVariables.OBSERVED_ASSESSMENT, System.currentTimeMillis());
        }
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
    public String toString() {

        StringBuffer sb = new StringBuffer();
        sb.append("[ObservedAssessmentCondition: ");
        sb.append(super.toString());
        sb.append("]");

        return sb.toString();
    }
}
