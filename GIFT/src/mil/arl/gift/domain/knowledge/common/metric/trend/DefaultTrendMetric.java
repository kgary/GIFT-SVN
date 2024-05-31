/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.common.metric.trend;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.domain.knowledge.Concept;
import mil.arl.gift.domain.knowledge.IntermediateConcept;
import mil.arl.gift.domain.knowledge.Task;
import mil.arl.gift.domain.knowledge.common.AbstractPerformanceAssessmentNode;
import mil.arl.gift.domain.knowledge.common.AssessmentProxy;

/**
 * This is the default trend metric algorithm for performance node's.  It doesn't 
 * set the trend.
 * 
 * @author mhoffman
 *
 */
public class DefaultTrendMetric implements TrendMetricInterface {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(DefaultTrendMetric.class);
    
    /**
     * Store up to 3 competencies, the trend algorithm only needs up to 3 competencies 
     */
    private static final int MAX_WINDOW_SIZE = 3;
    private List<Float> compentencies = new ArrayList<>(MAX_WINDOW_SIZE);

    @Override
    public boolean setTrend(AbstractPerformanceAssessmentNode node, AssessmentProxy assessmentProxy, Boolean holdValue) {
        
        if(node instanceof IntermediateConcept){  
            return setIntermediateConceptTrend(node, assessmentProxy, holdValue);
        }else if(node instanceof Task){
            return setTaskTrend(node, assessmentProxy, holdValue);
        }else{
            return setConceptTrend(node, assessmentProxy, holdValue);
        }
    }

    private boolean setConceptTrend(AbstractPerformanceAssessmentNode node, AssessmentProxy assessmentProxy, Boolean holdValue) {

        Concept concept = (Concept) node;

        addCompetency(concept.getAssessment().getCompetence());
        
        float newTrend = calculateTrend(concept.getAssessment().getTrend(), concept.getAssessment().getCompetence());

        if(logger.isInfoEnabled()){
            logger.info("Concept "+ concept.getName() +" trend changed from " + concept.getAssessment().getTrend() + " : "+ newTrend);
        }
        
        concept.getAssessment().updateTrend(newTrend);
        if(holdValue != null){
            concept.getAssessment().setTrendHold(holdValue);
        }

        return true;
    }
    
    private boolean setTaskTrend(AbstractPerformanceAssessmentNode node, AssessmentProxy assessmentProxy, Boolean holdValue) {
        
        Task task = (Task) node;

        addCompetency(task.getAssessment().getCompetence());
        
        float newTrend = calculateTrend(task.getAssessment().getTrend(), task.getAssessment().getCompetence());

        if(logger.isInfoEnabled()){
            logger.info("Task "+ task.getName() +" trend changed from " + task.getAssessment().getTrend() + " : "+ newTrend);
        }
        
        task.getAssessment().updateTrend(newTrend);
        if(holdValue != null){
            task.getAssessment().setTrendHold(holdValue);
        }

        return true;
    }
    
    private boolean setIntermediateConceptTrend(AbstractPerformanceAssessmentNode node, AssessmentProxy assessmentProxy, Boolean holdValue) {
     
        IntermediateConcept iconcept = (IntermediateConcept) node;

        addCompetency(iconcept.getAssessment().getCompetence());
        
        float newTrend = calculateTrend(iconcept.getAssessment().getTrend(), iconcept.getAssessment().getCompetence());

        if(logger.isInfoEnabled()){
            logger.info("Subconcept "+ iconcept.getName() +" trend changed from " + iconcept.getAssessment().getTrend() + " : "+ newTrend);
        }
        
        iconcept.getAssessment().updateTrend(newTrend);
        if(holdValue != null){
            iconcept.getAssessment().setTrendHold(holdValue);
        }

        return true;
    }
    
    /**
     * Add the incoming performance assessment node competency value to the list, 
     * making sure to store no more than 3 values.
     * 
     * @param competencyValue the incoming competence value for a task/concept
     */
    private void addCompetency(float competencyValue){
        
        if((compentencies.size()+1) > MAX_WINDOW_SIZE){
            //adding this item will push the size over the specified max size.
            compentencies.remove(0);
        }
        
        compentencies.add(competencyValue);
    }
    
    /**
     * Calculate the competency trend.
     * 
     * @param currentTrend the current performance assessment node trend value, used as the default if needed
     * @param currentCompetence the new competence value that should be used in the trend analysis, i.e. used to update the trend value.
     * @return the newly calculated trend value.
     */
    private float calculateTrend(float currentTrend, float currentCompetence){
        
        float newTrend = currentTrend;
        
        int competenceListSize = compentencies.size();
        if (competenceListSize == 1) {
            newTrend = currentCompetence;
        } else if (competenceListSize == 2) {
                newTrend = compentencies.get(1) - compentencies.get(0);
        } else if (competenceListSize > 2) {
            newTrend = Math.max(
                    (compentencies.get(competenceListSize-1)
                            - compentencies.get(competenceListSize - 3)),
                    (compentencies.get(competenceListSize-1)
                            - compentencies.get(competenceListSize - 2)));
        }
        
        return newTrend;
    }
}
