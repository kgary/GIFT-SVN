/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.scoring;

import generated.dkf.UnitsEnumType;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.enums.AssessmentLevelEnum;

/**
 * This scoring class contain information about the time it took to complete a condition.
 * 
 * @author mhoffman
 *
 */
public class CompletionTimeScorer extends AbstractScorer {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(CompletionTimeScorer.class);
    
    /** list of evaluators */
    private final List<OperatorEval> evaluators;
    
    /** amount of time it took to complete a condition */
    private Calendar timeToComplete = null;
    
    /** the default assessment to give for this scorer */
    private static final AssessmentLevelEnum DEFAULT_ASSESSMENT = AssessmentLevelEnum.BELOW_EXPECTATION;
    
    /**
     * Class constructor - set attributes
     * 
     * @param name - the display name of this scorer
     * @param units - the units associated with this scorer (e.g. "violations")
     * @param evaluators - the evaluators used to produce a score
     */
    public CompletionTimeScorer(String name, UnitsEnumType units, List<OperatorEval> evaluators){
        super(name, units);
        
        this.evaluators = evaluators;
    }
    
    /**
     * Returns a new instance of this scorer.
     * 
     * @param scorerToCopy the scorer to copy
     * @return the new instance of the scorer
     */
    public static CompletionTimeScorer deepCopy(CompletionTimeScorer scorerToCopy){
        
        CompletionTimeScorer newScorer = new CompletionTimeScorer(scorerToCopy.getName(), scorerToCopy.getUnits(), scorerToCopy.getEvaluators());
        newScorer.setInternalUseOnly(scorerToCopy.isInternalUseOnly());
        return newScorer;
    }
    
    /**
     * Return the collection of evaluation rules for this scorer.
     * @return the evaluation rules for this scorer.
     */
    public List<OperatorEval> getEvaluators(){
        return Collections.unmodifiableList(evaluators);
    }
    
    /**
     * Set the amount of time it took to complete a condition
     * 
     * @param timeToComplete - elapsed simulation time
     */
    public void setTimeToComplete(Calendar timeToComplete){
                
        this.timeToComplete = timeToComplete;
        
        if(logger.isDebugEnabled()){
            logger.debug(this + " received time to complete value");
        }
    }

    @Override
    public AssessmentLevelEnum getAssessment() {
        
        AssessmentLevelEnum assessment = DEFAULT_ASSESSMENT;
        
        if(timeToComplete == null){
            assessment = AssessmentLevelEnum.UNKNOWN;
            
        }else{
        
            for(OperatorEval evaluator : evaluators){
                
                if(evaluator.isTrue(timeToComplete.getTimeInMillis())){
                    assessment = evaluator.getAssessment();
                    break;
                }
            }
        }
        
        if(logger.isDebugEnabled()){
            logger.debug("Assessment for "+this+" is "+assessment);
        }
        
        return assessment;
    }

    @Override
    public String getRawScore() {
        
        if(timeToComplete == null){
            if(logger.isInfoEnabled()){
                logger.info("The raw score is null for "+this.getName()+", using default of "+DEFAULT_RAW_SCORE);
            }
            return DEFAULT_RAW_SCORE;
        }
        
        return fdf.format(timeToComplete);
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[IntegrationScorer: ");
        sb.append(super.toString());
        sb.append(", time to complete = ").append(getRawScore());
        
        sb.append(", evaluators = {");
        for(OperatorEval evaluator : evaluators){
            sb.append(evaluator).append(", ");
        }
        sb.append("}");
        sb.append("]");
        
        return sb.toString();
    }

}
