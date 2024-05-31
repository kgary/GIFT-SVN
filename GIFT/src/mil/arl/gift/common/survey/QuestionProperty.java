/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.survey;

import java.io.Serializable;
import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;

/**
 * A property of a question, can store a string value and a option list value
 *
 * @author jleonard
 */
public class QuestionProperty implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private int questionPropertyId;

    private int questionId;

    private SurveyPropertyKeyEnum propertyKey;

    private String propertyValue;

    private OptionList propertyOptionList;

    /**
     * Default Constructor
     *
     * Required by IsSerializable to exist and be public
     */
    public QuestionProperty() {
    }

    /**
     * Constructor
     *
     * @param propertyKey The key of the property
     * @param propertyValue The value of the property
     */
    public QuestionProperty(SurveyPropertyKeyEnum propertyKey, String propertyValue) {
        this.propertyKey = propertyKey;
        this.propertyValue = propertyValue;
        this.propertyOptionList = null;
    }

    /**
     * Constructor
     *
     * @param propertyKey The key of the property
     * @param propertyOptionList The option list value of the property
     */
    public QuestionProperty(SurveyPropertyKeyEnum propertyKey, OptionList propertyOptionList) {
        this.propertyKey = propertyKey;
        this.propertyValue = null;
        this.propertyOptionList = propertyOptionList;
    }

    /**
     * Constructor
     *
     * @param questionPropertyId The ID of the question property
     * @param questionId The ID of the question this is a property of
     * @param propertyKey The key of the property
     * @param propertyValue The value of the property
     * @param propertyOptionList The option list value of the property
     */
    public QuestionProperty(int questionPropertyId, int questionId, SurveyPropertyKeyEnum propertyKey, String propertyValue, OptionList propertyOptionList) {
        this.questionPropertyId = questionPropertyId;
        this.questionId = questionId;
        this.propertyKey = propertyKey;
        this.propertyValue = propertyValue;
        this.propertyOptionList = propertyOptionList;
    }

    /**
     * Gets the ID of the question property
     *
     * @return int The ID of the question property
     */
    public int getQuestionPropertyId() {
        return questionPropertyId;
    }

    /**
     * Gets the ID of the question this is a property of
     *
     * @return int The ID of the question this is a property of
     */
    public int getQuestionId() {
        return questionId;
    }
    
    public void setQuestionId(int questionId) {
        
        this.questionId = questionId;
    }

    /**
     * Gets the key of the property
     *
     * @return String The key of the property
     */
    public SurveyPropertyKeyEnum getPropertyKey() {
        return propertyKey;
    }

    /**
     * Gets the value of the property
     *
     * @return String The value of the property
     */
    public String getPropertyValue() {
        return propertyValue;
    }

    /**
     * Sets the value of the property
     *
     * @param propertyValue The value of the property
     */
    public void setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
    }

    /**
     * Gets the option list value of the property
     *
     * @return GwtOptionList The option list value of the property
     */
    public OptionList getPropertyOptionList() {
        return propertyOptionList;
    }

    /**
     * Sets the option list value of the property
     *
     * @param propertyOptionList The option list value of the property
     */
    public void setPropertyOptionList(OptionList propertyOptionList) {
        this.propertyOptionList = propertyOptionList;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[QuestionProperty: ");
        sb.append(" id  = ").append(getQuestionPropertyId());
        sb.append(" question id  = ").append(getQuestionId());
        sb.append(", key = ").append(getPropertyKey());
        sb.append(", value = ").append(getPropertyValue());
        sb.append(", option list = ").append(getPropertyOptionList());
        sb.append("]");
        return sb.toString();
    }
}
