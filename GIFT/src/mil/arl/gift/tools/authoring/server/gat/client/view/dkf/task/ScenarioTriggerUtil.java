/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task;

import static mil.arl.gift.common.gwt.client.SafeHtmlUtils.bold;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

import generated.dkf.ChildConceptEnded;
import generated.dkf.Concept;
import generated.dkf.ConceptAssessment;
import generated.dkf.ConceptEnded;
import generated.dkf.Coordinate;
import generated.dkf.EndTriggers;
import generated.dkf.EntityLocation;
import generated.dkf.LearnerActionReference;
import generated.dkf.LearnerId;
import generated.dkf.LearnerLocation;
import generated.dkf.PointRef;
import generated.dkf.Scenario;
import generated.dkf.ScenarioStarted;
import generated.dkf.StartLocation;
import generated.dkf.StartTriggers;
import generated.dkf.StrategyApplied;
import generated.dkf.StartTriggers.Trigger.TriggerMessage;
import generated.dkf.Task;
import generated.dkf.TaskEnded;
import generated.dkf.TriggerLocation;
import generated.dkf.EntityLocation.EntityId;
import mil.arl.gift.common.EnumerationNotFoundException;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;

/**
 * A utility class that contains various useful methods for operating on scenario triggers.
 * 
 * @author sharrison
 */
public class ScenarioTriggerUtil {
    
    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(ScenarioTriggerUtil.class.getName());


    /**
     * Builds the description string for the trigger type. Each type has a unique description
     * template.
     * 
     * @param trigger the trigger used to build the description.
     * @return the description of the trigger built by using the trigger type and the data provided
     *         within the trigger.
     */
    public static String buildTriggerDescription(Serializable trigger) {

        Serializable value;
        BigDecimal delay;
        TriggerMessage triggerMessage = null;

        if (trigger instanceof StartTriggers.Trigger) {
            StartTriggers.Trigger startTrigger = (StartTriggers.Trigger) trigger;
            value = startTrigger.getTriggerType();
            delay = startTrigger.getTriggerDelay();
            triggerMessage = startTrigger.getTriggerMessage();

        } else if (trigger instanceof EndTriggers.Trigger) {
            EndTriggers.Trigger endTrigger = (EndTriggers.Trigger) trigger;
            value = endTrigger.getTriggerType();
            delay = endTrigger.getTriggerDelay();

        } else if (trigger instanceof Scenario.EndTriggers.Trigger) {
            Scenario.EndTriggers.Trigger scenarioEndTrigger = (Scenario.EndTriggers.Trigger) trigger;
            value = scenarioEndTrigger.getTriggerType();
            delay = scenarioEndTrigger.getTriggerDelay();

        } else {
            throw new IllegalArgumentException("Cannot build a description for trigger. Unknown origin: " + trigger);
        }

        SafeHtmlBuilder sb = new SafeHtmlBuilder();
        
        String the = "The";
        if (delay != null) {
            sb.appendEscaped(delay.toString()).appendHtmlConstant(" seconds after ");
            
            // ensure case sensitivity when building sentence
            the = the.toLowerCase();
        }
        
        if (value instanceof ConceptAssessment) {
            
            ConceptAssessment conceptAssessment = (ConceptAssessment) value;
            BigInteger nodeId = conceptAssessment.getConcept();
            String name = getNameForId(nodeId);
            
            String assessmentName = conceptAssessment.getResult();
            
            try {
                //try to get the display name of the assessment value, if it properly maps to an enumeration
                assessmentName = AssessmentLevelEnum.valueOf(assessmentName).getDisplayName();
                
            } catch (EnumerationNotFoundException e) {
                
                if(logger.isLoggable(Level.WARNING)) {
                    logger.warning("Ignoring malformed concept assessment level. " + e.toString());
                }
            }
            sb.append(bold(name)).appendHtmlConstant("'s performance assessment level reaches ")
                    .append(bold(assessmentName));

        } else if (value instanceof ConceptEnded) {

            BigInteger nodeId = ((ConceptEnded) value).getNodeId();
            String name = getNameForId(nodeId);
            sb.appendHtmlConstant(the).appendHtmlConstant(" assessment has finished covering ").append(bold(name));

        } else if (value instanceof ChildConceptEnded) {

            BigInteger nodeId = ((ChildConceptEnded) value).getNodeId();
            String name = getNameForId(nodeId);
            sb.appendHtmlConstant(the).appendHtmlConstant(" assessment has finished covering ").append(bold(name));

        } else if (value instanceof TaskEnded) {

            BigInteger nodeId = ((TaskEnded) value).getNodeId();
            String name = getNameForId(nodeId);
            sb.appendHtmlConstant(the).appendHtmlConstant(" task ").append(bold(name)).appendHtmlConstant(" has ended");

        } else if (value instanceof LearnerActionReference) {
            LearnerActionReference learnerActionReference = (LearnerActionReference) value;
            String name = learnerActionReference.getName();
            sb.appendHtmlConstant(the)
                    .appendHtmlConstant(" learner has performed the ")
                    .append(bold(name != null ? name : ""))
                    .appendHtmlConstant(" learner action");
        } else if (value instanceof LearnerLocation) {

            Coordinate coord = ((LearnerLocation) value).getCoordinate();
            sb.appendHtmlConstant(the).appendHtmlConstant(" learner reaches location ")
                    .append(bold(ScenarioClientUtility.prettyPrintCoordinate(coord))).appendHtmlConstant(" in the assessment environment");

        } else if (value instanceof EntityLocation) {

            EntityId entityId = ((EntityLocation) value).getEntityId();
            
            StringBuffer locationDetails = new StringBuffer();
            TriggerLocation triggerLoc = ((EntityLocation) value).getTriggerLocation();
            Coordinate triggerCoord = triggerLoc.getCoordinate();
            PointRef ptRef = triggerLoc.getPointRef();
            if(triggerCoord != null) {
                locationDetails.append(ScenarioClientUtility.prettyPrintCoordinate(triggerCoord));
            }else if(ptRef != null) {
                locationDetails.append("'").append(ptRef.getValue()).append("'");
                if(ptRef.getDistance() != null) {
                    locationDetails.append(" w/in ").append(ptRef.getDistance().doubleValue()).append(" meter(s)");
                }
            }else {
                locationDetails.append("not specified");
            }

            Serializable entityIdType = entityId.getTeamMemberRefOrLearnerId();
            if(entityIdType instanceof LearnerId){
                LearnerId learnerId = (LearnerId)entityIdType;
                if(learnerId.getType() instanceof StartLocation){
                    StartLocation startLocation = (StartLocation)learnerId.getType();
                    
                    Coordinate startCoord = startLocation.getCoordinate();

                    sb.appendHtmlConstant(the).appendHtmlConstant(" entity starting at ")
                            .append(bold(ScenarioClientUtility.prettyPrintCoordinate(startCoord))).appendHtmlConstant(" reaches location ")
                            .append(bold(locationDetails.toString()))
                            .appendHtmlConstant(" in the assessment environment");
                    
                }else if(learnerId.getType() instanceof String){
                    String entityMarking = (String) learnerId.getType();
                    
                    sb.appendHtmlConstant(the).appendHtmlConstant(" entity named ")
                            .append(bold(entityMarking)).appendHtmlConstant(" reaches location ")
                            .append(bold(locationDetails.toString()))
                            .appendHtmlConstant(" in the assessment environment");
                }
                
            }else if(entityIdType instanceof EntityLocation.EntityId.TeamMemberRef){
                String entityMarking = ((EntityLocation.EntityId.TeamMemberRef)entityIdType).getValue();
                
                sb.appendHtmlConstant(the).appendHtmlConstant(" entity named ")
                        .append(bold(entityMarking)).appendHtmlConstant(" reaches location ")
                        .append(bold(locationDetails.toString()))
                        .appendHtmlConstant(" in the assessment environment");
            }
            
        } else if (value instanceof ScenarioStarted){
            sb.appendHtmlConstant("The scenario has started");
            
        } else if(value instanceof StrategyApplied){
            StrategyApplied sApplied = (StrategyApplied)value;
            String strategyName = sApplied.getStrategyName();
            if(strategyName == null){
                // in case the strategy was deleted, still need a non-null string for 'bold()'
                strategyName = "";
            }
            sb.appendHtmlConstant(the).appendHtmlConstant(" strategy named ").append(bold(strategyName)).appendHtmlConstant(" is applied");
            
        } else {
            throw new IllegalArgumentException("Cannot build a description for unknown trigger type: " + value);
        }

        // end all trailing sentences with a period
        sb.appendHtmlConstant(".");

        if (triggerMessage != null && triggerMessage.getStrategy() != null && !triggerMessage.getStrategy().getStrategyActivities().isEmpty()) {
        	List<Serializable> activities = triggerMessage.getStrategy().getStrategyActivities();
        	sb.appendHtmlConstant(" ").appendHtmlConstant(String.valueOf(activities.size()))
        	    .appendHtmlConstant(activities.size() == 1 ? " activity" : " activities")
        	    .appendHtmlConstant(" will be presented when this trigger activates.");
        }

        return sb.toSafeHtml().asString();
    }

    /**
     * Gets the name of the node associated with the node id.
     * 
     * @param nodeId ID of the node whose name is requested.
     * @return The name of the node associated with the given id.
     */
    private static String getNameForId(BigInteger nodeId) {

        Task taskWithId = ScenarioClientUtility.getTaskWithId(nodeId);
        if (taskWithId != null) {
            return taskWithId.getName();
        }

        Concept conceptWithId = ScenarioClientUtility.getConceptWithId(nodeId);
        if (conceptWithId != null) {
            return conceptWithId.getName();
        }

        return "";
    }
    
    /**
     * Determines if the provided node id is a match for the provided trigger. A match is found by
     * examining the trigger's type and if that type contains a node id, comparing the values.
     * 
     * @param trigger the trigger to examine. If null, false is returned.
     * @param nodeId to node id to compare against. If null, false is returned.
     * @return true if the trigger type contains a node id with the same value as the provided node
     *         id; false otherwise.
     * @throws UnsupportedOperationException if the trigger type is unknown
     */
    public static boolean doesTriggerMatchNodeId(Serializable trigger, BigInteger nodeId) {
        if (trigger == null || nodeId == null) {
            return false;
        }

        Serializable triggerType;
        if (trigger instanceof StartTriggers.Trigger) {
            final StartTriggers.Trigger startTrigger = (StartTriggers.Trigger) trigger;
            triggerType = startTrigger.getTriggerType();
        } else if (trigger instanceof EndTriggers.Trigger) {
            final EndTriggers.Trigger endTrigger = (EndTriggers.Trigger) trigger;
            triggerType = endTrigger.getTriggerType();
        } else if (trigger instanceof Scenario.EndTriggers.Trigger) {
            final Scenario.EndTriggers.Trigger scenarioEndTrigger = (Scenario.EndTriggers.Trigger) trigger;
            triggerType = scenarioEndTrigger.getTriggerType();
        } else {
            throw new UnsupportedOperationException("Unknown trigger '" + trigger + "'");
        }

        BigInteger triggerNodeId;
        if (triggerType instanceof ConceptAssessment) {
            triggerNodeId = ((ConceptAssessment) triggerType).getConcept();
        } else if (triggerType instanceof ConceptEnded) {
            triggerNodeId = ((ConceptEnded) triggerType).getNodeId();
        } else if (triggerType instanceof ChildConceptEnded) {
            triggerNodeId = ((ChildConceptEnded) triggerType).getNodeId();
        } else if (triggerType instanceof TaskEnded) {
            triggerNodeId = ((TaskEnded) triggerType).getNodeId();
        } else {
            return false;
        }

        return nodeId.equals(triggerNodeId);
    }

    /**
     * Determines if the provided learner action name is a match for the provided trigger. A match
     * is found by examining the trigger's type and if that type contains the same learner action
     * name.
     * 
     * @param trigger the trigger to examine. If null, false is returned.
     * @param learnerActionName the name of the learner action to compare. If blank, false is
     *        returned.
     * @return a non null {@link LearnerActionReference} if the trigger type contains the same name; null otherwise.
     * @throws UnsupportedOperationException if the trigger type is unknown
     */
    public static LearnerActionReference doesTriggerMatchLearnerActionName(Serializable trigger,
            String learnerActionName) {
        if (trigger == null || StringUtils.isBlank(learnerActionName)) {
            return null;
        }

        Serializable triggerType;
        if (trigger instanceof StartTriggers.Trigger) {
            final StartTriggers.Trigger startTrigger = (StartTriggers.Trigger) trigger;
            triggerType = startTrigger.getTriggerType();
        } else if (trigger instanceof EndTriggers.Trigger) {
            final EndTriggers.Trigger endTrigger = (EndTriggers.Trigger) trigger;
            triggerType = endTrigger.getTriggerType();
        } else if (trigger instanceof Scenario.EndTriggers.Trigger) {
            final Scenario.EndTriggers.Trigger scenarioEndTrigger = (Scenario.EndTriggers.Trigger) trigger;
            triggerType = scenarioEndTrigger.getTriggerType();
        } else {
            throw new UnsupportedOperationException("Unknown trigger '" + trigger + "'");
        }

        if (!(triggerType instanceof LearnerActionReference)) {
            return null;
        }

        LearnerActionReference learnerActionReference = (LearnerActionReference) triggerType;
        if (StringUtils.equals(learnerActionName, learnerActionReference.getName())) {
            return learnerActionReference;
        }

        return null;
    }
    
    /**
     * Determines if the provided strategy name is a match for the provided trigger.  A match
     * is found  by examining the trigger's type and if that type contains the same strategy name.
     * @param trigger the trigger to examine.  If null, false is returned.
     * @param strategyName the name of a strategy to compare.  If blank, false is returned
     * @return a non null {@link StrategyApplied} if the trigger type contains the same strategy name, null otherwise.
     */
    public static StrategyApplied doesTriggerMatchStrategyName(Serializable trigger, String strategyName){
        if (trigger == null || StringUtils.isBlank(strategyName)) {
            return null;
        }
        
        Serializable triggerType;
        if (trigger instanceof StartTriggers.Trigger) {
            final StartTriggers.Trigger startTrigger = (StartTriggers.Trigger) trigger;
            triggerType = startTrigger.getTriggerType();
        } else if (trigger instanceof EndTriggers.Trigger) {
            final EndTriggers.Trigger endTrigger = (EndTriggers.Trigger) trigger;
            triggerType = endTrigger.getTriggerType();
        } else if (trigger instanceof Scenario.EndTriggers.Trigger) {
            final Scenario.EndTriggers.Trigger scenarioEndTrigger = (Scenario.EndTriggers.Trigger) trigger;
            triggerType = scenarioEndTrigger.getTriggerType();
        } else {
            throw new UnsupportedOperationException("Unknown trigger '" + trigger + "'");
        }

        if (!(triggerType instanceof StrategyApplied)) {
            return null;
        }

        StrategyApplied sApplied = (StrategyApplied) triggerType;
        if (StringUtils.equals(strategyName, sApplied.getStrategyName())) {
            return sApplied;
        }

        return null;
        
    }
}
