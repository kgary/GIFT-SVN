/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.survey.score;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import mil.arl.gift.common.enums.AbstractEnum;
import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;
import mil.arl.gift.common.enums.OperatorEnum;

/**
 * The scoring properties of an attribute
 *
 * @author jleonard
 */
public class AttributeScorerProperties implements Serializable {

    private static final long serialVersionUID = 1L;

    private LearnerStateAttributeNameEnum attributeType;

    private List<ReturnValueCondition> returnConditions = new ArrayList<ReturnValueCondition>();

    /**
     * Default Constructor
     *
     * Required to exist and be public for GWT compatability
     */
    public AttributeScorerProperties() {
    }

    /**
     * Constructor
     *
     * @param attributeType The attribute enum type.  Can't be null.
     * @param returnConditions The return conditions for this attribute,
     * can be empty but not null
     */
    public AttributeScorerProperties(LearnerStateAttributeNameEnum attributeType, List<ReturnValueCondition> returnConditions) {
        
        if(attributeType == null){
            throw new IllegalArgumentException("The attribute type can't be null.");
        }

        this.attributeType = attributeType;

        setReturnConditions(returnConditions);
    }

    /**
     * Gets the type of attribute this is scoring
     *
     * @return LearnerStateAttributeNameEnum The type of attribute this is
     * scoring.  Won't be null.
     */
    public LearnerStateAttributeNameEnum getAttributeType() {

        return attributeType;
    }
    
    @Override
    public boolean equals(Object other){
        
        if(this == other){
            return true;
        }else if(other == null){
            return false;
        }else if(other instanceof AttributeScorerProperties &&
                this.attributeType.equals(((AttributeScorerProperties)other).getAttributeType())){
            return true;
        }
        
        return false;
    }
    
    @Override
    public int hashCode(){
        return attributeType.getValue();
    }
    
    /**
     * Sets the type of attribute this is scoring
     * 
     * @param type the new attribute type
     */
    public void setAttributeType(LearnerStateAttributeNameEnum type){
    	this.attributeType = type;
    }

    /**
     * Gets the return conditions for this attribute
     *
     * @return List<ReturnValueCondition<? extends AbstractAttributeValuesEnum>>
     * The list of return conditions for this attribute
     */
    public List<ReturnValueCondition> getReturnConditions() {

        return returnConditions;
    }

    /**
     * Gets the enumeration return value for a given value
     *
     * @param noDefault If no condition matches the value and this is true,
     * return null, otherwise the default value
     * @param value The value
     * @return AbstractAttributeValuesEnum The return value for a given value
     */
    public AbstractEnum getReturnValue(boolean noDefault, double value) {
        
        for (ReturnValueCondition returnValueModel : getReturnConditions()) {

            if (returnValueModel.getSign() == OperatorEnum.EQUALS) {

                if (value == returnValueModel.getValue()) {

                    return returnValueModel.getReturnValue();
                }

            } else if (returnValueModel.getSign() == OperatorEnum.GT) {

                if (value > returnValueModel.getValue()) {

                    return returnValueModel.getReturnValue();
                }

            } else if (returnValueModel.getSign() == OperatorEnum.GTE) {

                if (value >= returnValueModel.getValue()) {

                    return returnValueModel.getReturnValue();
                }

            } else if (returnValueModel.getSign() == OperatorEnum.LT) {

                if (value < returnValueModel.getValue()) {

                    return returnValueModel.getReturnValue();
                }

            } else if (returnValueModel.getSign() == OperatorEnum.LTE) {

                if (value <= returnValueModel.getValue()) {

                    return returnValueModel.getReturnValue();
                }
            }
        }

        if (noDefault) {

            return null;

        } else {

            return attributeType.getAttributeDefaultValue();
        }
    }

    /**
     * Gets the enumeration return value for a given value, will always return a
     * valid value (never null)
     *
     * @param value The value
     * @return AbstractAttributeValuesEnum The return value for a given value
     */
    public AbstractEnum getReturnValue(double value) {

        return getReturnValue(false, value);
    }
    
    /**
     * Sets the return value conditions list
     * 
     * @param conditions The list of return value conditioned for the
     * question, can be empty but not null
     */
    public void setReturnConditions(List<ReturnValueCondition> conditions){
    	if(conditions == null){
            throw new IllegalArgumentException("The attribute scorers can't be null.");
        }
        this.returnConditions = new ArrayList<ReturnValueCondition>(conditions);
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[AttributeScorerProperties: ");
        sb.append("attributeType = ").append(getAttributeType());
        sb.append(", conditions = {");
        for(ReturnValueCondition condition : returnConditions){
            sb.append(" ").append(condition).append(",");
        }
        sb.append("}");
        sb.append("]");
        return sb.toString();
    }
}
