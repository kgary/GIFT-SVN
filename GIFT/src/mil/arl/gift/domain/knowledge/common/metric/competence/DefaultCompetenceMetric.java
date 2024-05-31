/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.common.metric.competence;

import java.util.UUID;

import mil.arl.gift.common.ConceptAssessment;
import mil.arl.gift.domain.knowledge.Concept;
import mil.arl.gift.domain.knowledge.IntermediateConcept;
import mil.arl.gift.domain.knowledge.Task;
import mil.arl.gift.domain.knowledge.common.AbstractPerformanceAssessmentNode;
import mil.arl.gift.domain.knowledge.common.AssessmentProxy;
import mil.arl.gift.domain.knowledge.common.ProxyTaskAssessment;
import mil.arl.gift.domain.knowledge.condition.AbstractCondition;

/**
 * This is the default competence metric algorithm for performance nodes.  It merely
 * averages the competence values of the node's children.
 * 
 * @author mhoffman
 *
 */
public class DefaultCompetenceMetric implements CompetenceMetricInterface {

    @Override
    public boolean setCompetence(AbstractPerformanceAssessmentNode node, AssessmentProxy assessmentProxy, Boolean holdValue) {

        if(node instanceof IntermediateConcept){ 
            return setIntermediateConceptCompetence(node, assessmentProxy, holdValue);
        }else if(node instanceof Task){
            return setTaskCompetence(node, assessmentProxy, holdValue);
        }else{
            return setConceptCompetence(node, assessmentProxy, holdValue);
        }

    }
    
    private boolean setConceptCompetence(AbstractPerformanceAssessmentNode node, AssessmentProxy assessmentProxy, Boolean holdValue) {

        Concept concept = (Concept)node;
        
        //
        // Calculate aggregate confidence and competence scores
        //
        float competenceTotal = 0f;
        for(AbstractCondition condition : concept.getConditions()){
            
            competenceTotal += condition.getCompetence();
        }
        
        float avgCompetence = competenceTotal/concept.getConditions().size();
        
        if(concept.getAssessment().getCompetence() != avgCompetence){
            concept.getAssessment().updateCompetence(avgCompetence);
            if(holdValue != null){
                concept.getAssessment().setCompetenceHold(holdValue);
            }
            return true;
        }
        
        if(holdValue != null){
            concept.getAssessment().setCompetenceHold(holdValue);
        }
        return false;
    }
    
    private boolean setTaskCompetence(AbstractPerformanceAssessmentNode node, AssessmentProxy assessmentProxy, Boolean holdValue) {
        
        Task task = (Task)node;
        ProxyTaskAssessment taskAssessment = task.getAssessment();
        
        float competenceTotal = 0;
        
        //
        // Calculate aggregate confidence for this concept
        //
        for(UUID conceptAssessmentId : taskAssessment.getConcepts()){
            
            //get the concept's assessment from the proxy
            ConceptAssessment cAssessment = (ConceptAssessment)assessmentProxy.get(conceptAssessmentId);
            competenceTotal += cAssessment.getCompetence();
        }
        
        float avgCompetence = competenceTotal/taskAssessment.getConcepts().size();
        
        if(taskAssessment.getCompetence() !=  avgCompetence){
            taskAssessment.updateCompetence(avgCompetence);
            if(holdValue != null){
                taskAssessment.setCompetenceHold(holdValue);
            }
            return true;
        }
        
        if(holdValue != null){
            taskAssessment.setCompetenceHold(holdValue);
        }
        return false;
    }
    
    private boolean setIntermediateConceptCompetence(AbstractPerformanceAssessmentNode node, AssessmentProxy assessmentProxy, Boolean holdValue) {
     
        IntermediateConcept intermediateConcept = (IntermediateConcept)node;
        
        //
        // Calculate aggregate confidence and competence scores
        //
        float competenceTotal = 0f;
        for(Concept concept : intermediateConcept.getConcepts()){
            
            competenceTotal += concept.getAssessment().getCompetence();
        }
        
        float avgCompetence = competenceTotal/intermediateConcept.getConcepts().size();
        
        if(intermediateConcept.getAssessment().getConfidence() != avgCompetence){
            intermediateConcept.getAssessment().updateCompetence(avgCompetence);
            if(holdValue != null){
                intermediateConcept.getAssessment().setCompetenceHold(holdValue);
            }
            return true;
        }
        
        if(holdValue != null){
            intermediateConcept.getAssessment().setCompetenceHold(holdValue);
        }
        return false;
    }
    
    @Override
    public String toString(){
        
        return "[DefaultCompetenceMetric]";
    }

}
