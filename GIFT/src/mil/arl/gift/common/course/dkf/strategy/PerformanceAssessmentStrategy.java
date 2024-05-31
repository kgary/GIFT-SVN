/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.course.dkf.strategy;

import java.io.Serializable;

import generated.dkf.PerformanceAssessment;

/**
 * This class contains information on a performance assessment strategy.
 * 
 * @author mhoffman
 *
 */
public class PerformanceAssessmentStrategy extends AbstractDKFStrategy {
    
    /** the Performance Assessment content for this performance assessment strategy */
    private PerformanceAssessment performanceAssessment;
    
    /**
     * Class constructor - set attributes
     * 
     * @param name - the name of this strategy
     * @param performanceAssessment - dkf.xsd generated class instance
     */
    public PerformanceAssessmentStrategy(String name, PerformanceAssessment performanceAssessment){
        super(name, performanceAssessment.getStrategyHandler());
        this.performanceAssessment = performanceAssessment;
        
        if(performanceAssessment.getDelayAfterStrategy() != null && performanceAssessment.getDelayAfterStrategy().getDuration() != null){
            this.setDelayAfterStrategy(performanceAssessment.getDelayAfterStrategy().getDuration().floatValue());
        }
    }
    
    /**
     * Return the performance assessment type for this strategy (e.g. conversation, performance node id)
     * 
     * @return currently generated.dkf.PerformanceAssessment.{Conversation, PerformanceNode}
     */
    public Serializable getAssessmentType(){
        return performanceAssessment.getAssessmentType();
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[InstructionalInterventionStrategy: ");
        sb.append(super.toString());        
        sb.append(", assessmentType: ");
        
        Serializable assessmentType = getAssessmentType();
        if(assessmentType instanceof generated.dkf.Conversation){
            generated.dkf.Conversation conversation = (generated.dkf.Conversation)assessmentType;
            
            if(conversation.getType() instanceof generated.dkf.AutoTutorSKO){
                generated.dkf.AutoTutorSKO atSKO = (generated.dkf.AutoTutorSKO)conversation.getType();
                if(atSKO.getScript() instanceof generated.dkf.LocalSKO){
                    generated.dkf.LocalSKO localSKO = (generated.dkf.LocalSKO)atSKO.getScript();
                    sb.append(" local SKO = ").append(localSKO.getFile());
                }else if(atSKO.getScript() instanceof generated.dkf.ATRemoteSKO){
                    generated.dkf.ATRemoteSKO remoteSKO = (generated.dkf.ATRemoteSKO)atSKO.getScript();
                    sb.append(" remote SKO = ").append(remoteSKO.getURL());
                }
            }else if(conversation.getType() instanceof String){
                sb.append(" conversation file = ").append(conversation.getType());
            }
        }else if(assessmentType instanceof generated.dkf.PerformanceAssessment.PerformanceNode){
            generated.dkf.PerformanceAssessment.PerformanceNode perfNode = (generated.dkf.PerformanceAssessment.PerformanceNode)assessmentType;
            sb.append(" performance node = ").append(perfNode.getNodeId());
        }        
        
        sb.append("]");
        
        return sb.toString();
    }
}
