/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.survey.score;

import java.io.Serializable;
import mil.arl.gift.common.enums.AbstractEnum;
import mil.arl.gift.common.enums.OperatorEnum;

/**
 * A return value condition
 *
 * @author jleonard
 */
public class ReturnValueCondition implements Serializable {

    private static final long serialVersionUID = 1L;

    private OperatorEnum sign;

    private double value;

    private AbstractEnum returnValue;

    /**
     * Default Constructor
     *
     * Required to exist and be public for GWT compatability
     */
    public ReturnValueCondition() {
    }

    /**
     * Constructor
     *
     * @param sign The sign of the condition
     * @param value The value of the condition
     * @param returnValue Return value when this condition is met
     */
    public ReturnValueCondition(OperatorEnum sign, double value, AbstractEnum returnValue) {
        
        if(sign == null){
            throw new IllegalArgumentException("The sign can't be null.");
        }else if(returnValue == null){
            throw new IllegalArgumentException("The return value can't be null.");
        }else if(Double.isNaN(value)){
            throw new IllegalArgumentException("The value is NaN.");
        }

        this.sign = sign;
        this.value = value;
        this.returnValue = returnValue;
    }

    /**
     * Gets the sign of the condition
     *
     * @return OperatorEnum The sign of the condition
     */
    public OperatorEnum getSign() {

        return sign;
    }

    /**
     * Gets the value of the condition
     *
     * @return double The value of the condition
     */
    public double getValue() {

        return value;
    }

    /**
     * Gets the return value when the condition is met
     *
     * @return AbstractEnum The return value when the condition is met
     */
    public AbstractEnum getReturnValue() {

        return returnValue;
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[ReturnValueCondition: ");
        sb.append(sign).append(" ").append(value).append(" -> ").append(returnValue);
        sb.append("]");
        return sb.toString();
    }
}
