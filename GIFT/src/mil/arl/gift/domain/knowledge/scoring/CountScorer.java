/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.scoring;

import generated.dkf.UnitsEnumType;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.enums.AssessmentLevelEnum;

/**
 * This scoring class maintains a count on a performance node for scoring purposes.
 * 
 * @author mhoffman
 *
 */
public class CountScorer extends AbstractScorer {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(CountScorer.class);
    
    /** list of evaluators */
    private final List<OperatorEval> evaluators;
    
    /** current count */
    private int currentCnt = 0;
    
    /** the default assessment to give for this scorer */
    private static final AssessmentLevelEnum DEFAULT_ASSESSMENT = AssessmentLevelEnum.BELOW_EXPECTATION;
    
    /**
     * Class constructor - set attributes
     * 
     * @param name - the display name of this scorer
     * @param units - the units of measurement the score produces by this scorer
     * @param evaluators - evaluation logic for this scorer
     */
    public CountScorer(String name, UnitsEnumType units, List<OperatorEval> evaluators){
        super(name, units);
                
        this.evaluators = evaluators;
    }
    
    /**
     * Returns a new instance of this scorer.  Doesn't currently copy the current count value (maybe it should?).
     * 
     * @param scorerToCopy the scorer to copy
     * @return the new instance of the scorer
     */
    public static CountScorer deepCopy(CountScorer scorerToCopy){
        
        CountScorer newScorer = new CountScorer(scorerToCopy.getName(), scorerToCopy.getUnits(), scorerToCopy.getEvaluators());
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
     * Increment the current count by 1
     */
    public void increment(){
        
        currentCnt++;
        if(logger.isDebugEnabled()){
            logger.debug(this + " just incremented its count");
        }
    }
    
    /**
     * Increment the current count by the value provided
     * 
     * @param value - the value to add to the count
     */
    public void add(int value){
        currentCnt += value;
        if(logger.isDebugEnabled()){
            logger.debug(this + " just increased its count by "+value);
        }
    }

    @Override
    public AssessmentLevelEnum getAssessment() {
       
        AssessmentLevelEnum assessment = DEFAULT_ASSESSMENT;
        
        for(OperatorEval evaluator : evaluators){
            
            if(evaluator.isTrue(currentCnt)){
                assessment = evaluator.getAssessment();
                break;
            }
        }
        
        if(logger.isDebugEnabled()){
            logger.debug("Assessment for "+this+" is "+assessment);
        }
        
        return assessment;
    }

    @Override
    public String getRawScore() {
        return String.valueOf(currentCnt);
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[CountScorer: ");
        sb.append(super.toString());
        sb.append(", current count = ").append(getRawScore());
        
        sb.append(", evaluators = {");
        for(OperatorEval evaluator : evaluators){
            sb.append(evaluator).append(", ");
        }
        sb.append("}");
        
        sb.append("]");
        
        return sb.toString();
    }

}
