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
 * This scoring class maintains time information about a performance node for scoring purposes.
 * 
 * @author mhoffman
 *
 */
public class IntegrationScorer extends AbstractScorer {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(IntegrationScorer.class);
    
    /** list of evaluators */
    private final List<OperatorEval> evaluators;
    
    /** current total amount of time */
    private Calendar currentTime;
    
    /** used to keep track of intervals of time to add to the current running total */
    private Calendar startTime = null;
    
    private static final AssessmentLevelEnum DEFAULT_ASSESSMENT = AssessmentLevelEnum.BELOW_EXPECTATION;
    
    /**
     * Class constructor - set attributes
     * 
     * @param name - the name of the scorer
     * @param units - the units associated with this scorer (e.g. "violations")
     * @param evaluators - the evaluators used to produce a score
     */
    public IntegrationScorer(String name, UnitsEnumType units, List<OperatorEval> evaluators){
        super(name, units);
        
        this.evaluators = evaluators;
        
        currentTime = Calendar.getInstance();
        currentTime.setTimeInMillis(0);
    }
    
    /**
     * Returns a new instance of this scorer.
     * 
     * @param scorerToCopy the scorer to copy
     * @return the new instance of the scorer
     */
    public static IntegrationScorer deepCopy(IntegrationScorer scorerToCopy){
        
        IntegrationScorer newScorer = new IntegrationScorer(scorerToCopy.getName(), scorerToCopy.getUnits(), scorerToCopy.getEvaluators());
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

    @Override
    public AssessmentLevelEnum getAssessment() {
        
        AssessmentLevelEnum assessment = DEFAULT_ASSESSMENT;
        
        for(OperatorEval evaluator : evaluators){
            
            if(startTime == null){
                //the timer (event) being scored is not running/active, therefore use the time calculated and saved
                if(evaluator.isTrue(currentTime.getTimeInMillis())){
                    assessment = evaluator.getAssessment();
                    break;
                }
            }else{
                //the timer (event) is still ongoing, therefore calculate the time elapsed as of now (plus any previously captured time)
                long ongoingTime = currentTime.getTimeInMillis() + (int)(Calendar.getInstance().getTimeInMillis() - startTime.getTimeInMillis());
                if(evaluator.isTrue(ongoingTime)){
                    assessment = evaluator.getAssessment();
                    break;
                }
            }
        }
        
        return assessment;
    }

    @Override
    public String getRawScore() {
        return fdf.format(currentTime);     
    }
    
    /**
     * Return whether or not this integration scorer has been started and is waiting for
     * some event to stop it.
     * 
     * @return boolean - true iff this scorer is currently keeping track of the elapsed time of an event
     */
    public boolean hasStarted(){
        return startTime != null;
    }
    
    /**
     * Start an integration event of time
     */
    public void start(){
        startTime = Calendar.getInstance();
        
        if(logger.isInfoEnabled()){
            logger.info("A time interval has been started");
        }
    }
    
    /**
     * Stop an integration event of time and add the interval to the current running total
     */
    public void stop(){
        
        if(startTime != null){
            currentTime.add(Calendar.MILLISECOND, (int)(Calendar.getInstance().getTimeInMillis() - startTime.getTimeInMillis()));
            
            if(logger.isDebugEnabled()){
                logger.debug("A time interval has been stopped resulting in new current running time value of "+currentTime);
            }
        }
        
        //reset
        startTime = null;
    }
    
    @Override
    public void cleanup(){
        
        //stop any currently running timers so currentTime can be calculated
        stop();
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[IntegrationScorer: ");
        sb.append(super.toString());
        sb.append(", current time = ").append(getRawScore());
        
        sb.append(", evaluators = {");
        for(OperatorEval evaluator : evaluators){
            sb.append(evaluator).append(", ");
        }
        sb.append("}");
        sb.append("]");
        
        return sb.toString();
    }
    
}
