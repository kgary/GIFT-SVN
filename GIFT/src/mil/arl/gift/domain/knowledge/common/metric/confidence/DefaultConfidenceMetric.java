/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.common.metric.confidence;

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
 * This is the default confidence metric algorithm used on performance nodes.  It merely
 * averages the confidence values of the node's children.
 * 
 * @author mhoffman
 *
 */
public class DefaultConfidenceMetric implements ConfidenceMetricInterface {

    @Override
    public boolean setConfidence(AbstractPerformanceAssessmentNode node, AssessmentProxy assessmentProxy, Boolean holdValue) {

        if(node instanceof IntermediateConcept){    
            return setIntermediateConceptConfidence(node, assessmentProxy, holdValue);
        }else if(node instanceof Task){
            return setTaskConfidence(node, assessmentProxy, holdValue);
        }else{
            return setConceptConfidence(node, assessmentProxy, holdValue);
        }
    }
    
    private boolean setConceptConfidence(AbstractPerformanceAssessmentNode node, AssessmentProxy assessmentProxy, Boolean holdValue){
        
        Concept concept = (Concept)node;
        
        //
        // Calculate aggregate confidence and competence scores
        //
        float confidenceTotal = 0f;
        for(AbstractCondition condition : concept.getConditions()){
            
            confidenceTotal += condition.getConfidence();
        }
        
        float avgConfidence = confidenceTotal/concept.getConditions().size();
        
        if(concept.getAssessment().getConfidence() != avgConfidence){
            concept.getAssessment().updateConfidence(avgConfidence);
            if(holdValue != null){
                concept.getAssessment().setConfidenceHold(holdValue);
            }
            return true;
        }
        
        if(holdValue != null){
            concept.getAssessment().setConfidenceHold(holdValue);
        }
        return false;
    }
    
    private boolean setTaskConfidence(AbstractPerformanceAssessmentNode node, AssessmentProxy assessmentProxy, Boolean holdValue){
        
        Task task = (Task)node;
        ProxyTaskAssessment taskAssessment = task.getAssessment();
        
        float confidenceTotal = 0;
        
        //
        // Calculate aggregate confidence for this concept
        //
        for(UUID conceptAssessmentId : taskAssessment.getConcepts()){
            
            //get the concept's assessment from the proxy
            ConceptAssessment cAssessment = (ConceptAssessment)assessmentProxy.get(conceptAssessmentId);
            confidenceTotal += cAssessment.getConfidence();
        }
        
        float avgConfidence = confidenceTotal/taskAssessment.getConcepts().size();
        
        if(taskAssessment.getConfidence() !=  avgConfidence){
            taskAssessment.updateConfidence(avgConfidence);
            if(holdValue != null){
                taskAssessment.setConfidenceHold(holdValue);
            }
            return true;
        }
        
        if(holdValue != null){
            taskAssessment.setConfidenceHold(holdValue);
        }
        return false;
    }
    
    private boolean setIntermediateConceptConfidence(AbstractPerformanceAssessmentNode node, AssessmentProxy assessmentProxy, Boolean holdValue){
        
        IntermediateConcept intermediateConcept = (IntermediateConcept)node;
        
        //
        // Calculate aggregate confidence and competence scores
        //
        float confidenceTotal = 0f;
        for(Concept concept : intermediateConcept.getConcepts()){
            
            confidenceTotal += concept.getAssessment().getConfidence();
        }
        
        float avgConfidence = confidenceTotal/intermediateConcept.getConcepts().size();
        
        if(intermediateConcept.getAssessment().getConfidence() != avgConfidence){
            intermediateConcept.getAssessment().updateConfidence(avgConfidence);
            if(holdValue != null){
                intermediateConcept.getAssessment().setConfidenceHold(holdValue);
            }
            return true;
        }
        
        if(holdValue != null){
            intermediateConcept.getAssessment().setConfidenceHold(holdValue);
        }
        return false;
    }    

    @Override
    public String toString(){
        
        return "[DefaultConfidenceMetric]";
    }

}
