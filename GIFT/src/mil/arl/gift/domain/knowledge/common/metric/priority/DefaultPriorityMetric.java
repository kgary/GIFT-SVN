/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.common.metric.priority;

import mil.arl.gift.domain.knowledge.IntermediateConcept;
import mil.arl.gift.domain.knowledge.Task;
import mil.arl.gift.domain.knowledge.common.AbstractPerformanceAssessmentNode;
import mil.arl.gift.domain.knowledge.common.AssessmentProxy;

/**
 * This is the default algorithm for calculating priority for a performance node.  It
 * doesn't set the priority.
 * 
 * @author mhoffman
 *
 */
public class DefaultPriorityMetric implements PriorityMetricInterface {

    @Override
    public boolean setPriority(AbstractPerformanceAssessmentNode node, AssessmentProxy assessmentProxy, Boolean holdValue) {

        if(node instanceof IntermediateConcept){ 
            return setIntermediateConceptPriority(node, assessmentProxy, holdValue);
        }else if(node instanceof Task){
            return setTaskPriority(node, assessmentProxy, holdValue);
        }else{
            return setConceptPriority(node, assessmentProxy, holdValue);
        }

    }
    
    private boolean setConceptPriority(AbstractPerformanceAssessmentNode node, AssessmentProxy assessmentProxy, Boolean holdValue) {

        //no algorithm
        return false;
    }
    
    private boolean setTaskPriority(AbstractPerformanceAssessmentNode node, AssessmentProxy assessmentProxy, Boolean holdValue) {
        
        //no algorithm
        return false;
    }
    
    private boolean setIntermediateConceptPriority(AbstractPerformanceAssessmentNode node, AssessmentProxy assessmentProxy, Boolean holdValue) {
     
        //no algorithm
        return false;
    }
    
    @Override
    public String toString(){
        
        return "[DefaultPriorityMetric]";
    }
}
