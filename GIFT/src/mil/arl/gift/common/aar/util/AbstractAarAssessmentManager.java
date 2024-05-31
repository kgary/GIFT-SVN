/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.aar.util;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import mil.arl.gift.common.EvaluatorUpdateRequest;
import mil.arl.gift.common.aar.ScoreNodeUpdate;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.course.dkf.session.SessionScenarioInfo;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.score.GradedScoreNode;
import mil.arl.gift.common.state.PerformanceState;
import mil.arl.gift.common.state.PerformanceStateAttribute;
import mil.arl.gift.common.util.StringUtils;

/**
 * An object that is capable of performing complex operations involved
 * with modifying assessments in an AAR environment where there is no
 * live knowledge session.
 * <br/><br/>
 * This is used to utilize features like assessment rollups that require
 * a deeper understanding of a domain knowledge session than can be grasped
 * by looking at a single log message.
 * 
 * @author nroberts
 */
public abstract class AbstractAarAssessmentManager {
    
    /** The name of the DKF from which to derive assessment rules */
    private String dkfFileName;
    
    /** The knowledge session from a log that is associated with the DKF */
    private AbstractKnowledgeSession session;

    /**
     * Creates a new assessment manager that references the given DKF file and knowledge session
     * to build its assessment model
     * 
     * @param dkfFileName the name of the DKF from which to derive assessment rules. Cannot be null.
     * @param session the knowledge session from a log that is associated with the DKF. Cannot be null.
     */
    protected AbstractAarAssessmentManager(String dkfFileName, AbstractKnowledgeSession session) {
        
        if(StringUtils.isBlank(dkfFileName)) {
            throw new IllegalArgumentException("The name of the DKF file associated "
                    + "with an assessment manager must not be empty");
        }
        
        if(session == null) {
            throw new IllegalArgumentException("The knowledge session associate with an assessment manager cannot be null");
        }
        
        this.dkfFileName = dkfFileName;
        this.session = session;
    }

    /**
     * Applies the given evaluator update request to its target node in the given performance state and then
     * rolls up up any new assessment value for that node to its parent nodes as needed.
     * 
     * @param request the evaluator update request to apply. Cannot be null.
     * @param perfState the performance state whose assessments should be modified. Cannot be null.
     * @return a list of all the node performance states that were modified. Will not be null.
     */
    abstract public List<PerformanceStateAttribute> applyAndRollUp(EvaluatorUpdateRequest request, PerformanceState perfState);
    
    /**
     * Loads scenario information from the given DKF into this manager to determine how assessments should 
     * be handled within specific performance nodes. Additional information from this manager's knowledge
     * session will be used to help fill out the assessment model to match the log.
     * <br/><br/>
     * This is primarily used to obtain the performance metric algorithms that are used to roll
     * up assessments.
     * 
     * @param dkf the DKF whose scenario information should be loaded. Cannot be null. 
     * @throws Exception if a problem occurs while fetching the DKF
     */
    public abstract void loadDkf(File dkf) throws Exception;

    /**
     * Gets information about the structure of the scenario that has been loaded into this manager.
     * Requires {@link #loadDkf(File)} to have been called beforehand.
     * 
     * @return information about the loaded scenario. Can be null, if no scenario has been loaded yet.
     */
    public abstract SessionScenarioInfo getScenario();

    /**
     * Generates a graded score node for the entire scenario based on the given condition assessments
     * were provided by an observer controller
     * 
     * @param conceptToConditionAssessments the condition assessments provided by an observer controller. Cannot be null
     * @param courseConcepts the course concepts. Cannot be null.
     * @return a graded score node for the entire scenario that was generated using the provided assessments.
     */
    public abstract GradedScoreNode scoreOverallAsessments(Map<Integer, List<ScoreNodeUpdate>> conceptToConditionAssessments, 
            Collection<String> courseConcepts);

    /**
     * Calculates the assessment levels of any parent performance assessment nodes using the given 
     * condition assessments. Similar to {@link #applyAndRollUp(EvaluatorUpdateRequest, PerformanceState)}, 
     * this emulates an assessment roll up, but this method is not intended to modify any existing state and
     * is simply used to allow an observer controller to preview what their assessment changes
     * 
     * @param ocAssessments the assessments provided by the observer controller. This is expected to be a map from
     * each lead concept ID to the assessments of all the conditions underneath it. Cannot be null.
     * @return the calculated assessment levels of the parent nodes. Cannot be null.
     */
    public abstract Map<Integer, AssessmentLevelEnum> calculateRollUp(Map<Integer, List<ScoreNodeUpdate>> ocAssessments);

    /**
     * Gets the name of the DKF from which to derive assessment rules
     * Example: "React to Contact.dkf.xml"
     * 
     * @return the DKF file name. Will not be null.
     */
    public String getDkfFileName() {
        return dkfFileName;
    }

    /**
     * Gets the knowledge session from a log that is associated with the DKF
     * 
     * @return the knowledge session. Will not be null.
     */
    public AbstractKnowledgeSession getKnowledgeSession() {
        return session;
    }
}
