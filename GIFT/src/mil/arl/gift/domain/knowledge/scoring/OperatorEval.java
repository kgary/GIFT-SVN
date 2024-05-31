/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.scoring;

import generated.dkf.UnitsEnumType;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.enums.OperatorEnum;
import mil.arl.gift.domain.DomainDKFHandler;

/**
 * This class contains the logic to compare two values using a single operator.
 * 
 * @author mhoffman
 *
 */
public class OperatorEval {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(OperatorEval.class);

    /** the relational operator to evaluate with */
    private final OperatorEnum operator;
    
    /** the value to compare against */
    private final long value;
    
    /** assessment to give if an evaluation is true */
    private final AssessmentLevelEnum assessment;
    
    /**
     * Class constructor - set attributes
     * 
     * @param value the value to compare against
     * @param operator the relational operator to evaluate with
     * @param assessment assessment to give if an evaluation is true
     */
    public OperatorEval(long value, OperatorEnum operator, AssessmentLevelEnum assessment){        

        if(operator == null){
            throw new IllegalArgumentException("The operator can't be null");
        }
        
        this.operator = operator;
        
        if(assessment == null){
            throw new IllegalArgumentException("The assessment can't be null");
        }
        
        this.assessment = assessment;        
        this.value = value;
    }
    
    /**
     * Class constructor - set attributes from dkf content
     * 
     * @param evaluator - dkf content for evaluator
     * @param units the units of measurement for the incoming value to compare against
     */
    public OperatorEval(generated.dkf.Evaluator evaluator, UnitsEnumType units){
        
        String rawValue = evaluator.getValue();
        long value;
        if(units == UnitsEnumType.COUNT){
            //units are a count - integer
            
            value = Integer.parseInt(rawValue);
            
        }else if(units == UnitsEnumType.HH_MM_SS){
            //units are a hh:mm:ss timestamp
            
            Date timestamp = null;
            try{
                timestamp = DomainDKFHandler.atTime_df.convertStringToDate(rawValue);
            }catch(Exception e){
                logger.error("Caught exception while trying to parse evaluator value of '"+rawValue+"'.", e);
                throw new IllegalArgumentException("Caught exception while parsing time stamp, check log for more details", e);
            }
            
            value = timestamp.getTime();
            
        }else{
            //ERROR
            throw new IllegalArgumentException("Found unhandled units of "+units+" in list of evaluators.  If this is a valid unit type, please make sure to add logic here which supports it.");
        }
        
        this.value = value;
        
        if(OperatorEnum.valueOf(evaluator.getOperator()) == null){
            throw new IllegalArgumentException("The operator can't be null");
        }
        
        this.operator = OperatorEnum.valueOf(evaluator.getOperator());

        if(AssessmentLevelEnum.valueOf(evaluator.getAssessment()) == null){
            throw new IllegalArgumentException("The assessment can't be null");
        }
        
        this.assessment = AssessmentLevelEnum.valueOf(evaluator.getAssessment());
        
    }
    
    public AssessmentLevelEnum getAssessment(){
        return assessment;
    }
    
    public long getValue(){
        return value;
    }

    @Override
    public boolean equals(Object otherOperatorEval){
        
        if(otherOperatorEval == null){
            return false;
        }
        
        return ((OperatorEval)otherOperatorEval).value == this.value && ((OperatorEval)otherOperatorEval).operator == this.operator;
    }
    
    @Override
    public int hashCode(){
    	
    	// Start with prime number
    	int hash = 1041;
    	int mult = 53;
    	
    	if(this != null) {
	    	// Take another prime as multiplier, add members used in equals
    		
	    	hash = (int) (mult * hash + this.value);
	    	hash = mult * hash + this.operator.hashCode();
    	}
    	
    	return hash;
    }
    
    
    /**
     * Compare the values using the operator
     * 
     * @param otherValue - value to compare against this class's value
     * @return boolean - whether the comparison is true
     */
    public boolean isTrue(int otherValue){
        return isTrue((long)otherValue);
    }
    
    /**
     * Compare the values using the operator
     * 
     * @param otherValue - value to compare against this class's value
     * @return boolean - whether the comparison is true
     */
    public boolean isTrue(long otherValue){
        
        boolean satisfied = false;
        
        if(operator == OperatorEnum.EQUALS){
            satisfied = otherValue == value;
        }else if(operator == OperatorEnum.GT){
            satisfied = otherValue > value;
        }else if(operator == OperatorEnum.GTE){
            satisfied = otherValue >= value;
        }else if(operator == OperatorEnum.LT){
            satisfied = otherValue < value;
        }else if(operator == OperatorEnum.LTE){
            satisfied = otherValue <= value;
        }
        
        return satisfied;
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[OperatorEval: ");
        sb.append(", value = ").append(value);
        sb.append(", operator = ").append(operator);
        sb.append(", assessment = ").append(assessment);
        sb.append("]");
        
        return sb.toString();
    }
    
}
