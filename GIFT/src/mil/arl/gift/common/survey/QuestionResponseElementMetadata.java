/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.survey;

import java.io.Serializable;

/**
 * A class that contains the basic metadata of an {@link QuestionResponseElement}.
 * Acts as a lightweight version of the {@link QuestionResponseElement} class that 
 * can be used in many circumstances.
 * @see QuestionResponseElement
 * @author tflowers
 *
 */
public class QuestionResponseElementMetadata implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private int questionResponseId;
    private String text;
    private String rowText;
    private Integer rowIndex;
    private Integer columnIndex;
    
    /**
     * Constructor used for deserialization
     */
    public QuestionResponseElementMetadata() {
        
    }
    
    /**
     * Constructor that populates the metadata fields from the data contained 
     * within the given QuestionResponseElement
     * @param responseElement the QuestionResponseElement to pull metadata from
     */
    public QuestionResponseElementMetadata(QuestionResponseElement responseElement) {
        setQuestionResponseId(responseElement.getQuestionResponseId());
        setText(responseElement.getText());
        setColumnIndex(responseElement.getColumnIndex());
        setRowIndex(responseElement.getRowIndex());
        
        if(responseElement.getRowChoice() != null){
            setRowText(responseElement.getRowChoice().getText());
        }
    }

    /**
     * Getter for the question response id
     * @return the value of the question response id
     */
    public int getQuestionResponseId() {
        return questionResponseId;
    }

    /**
     * Setter for the question response id
     * @param questionResponseId the new value of the question response id
     */
    public void setQuestionResponseId(int questionResponseId) {
        this.questionResponseId = questionResponseId;
    }

    /**
     * Getter for the text
     * @return the value of the text, can be null
     */
    public String getText() {
        return text;
    }

    /**
     * Setter for the text
     * @param text the new value of the text, can be null
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Getter for the row text
     * @return the value of the row text, can be null
     */
    public String getRowText() {
        return rowText;
    }

    /**
     * Setter for the row text
     * @param rowText the new value of the row text, can be null
     */
    public void setRowText(String rowText) {
        this.rowText = rowText;
    }
    
    /**
     * Getter for the row index
     * @return the value of the row index
     */
    public Integer getRowIndex() {
        return this.rowIndex;
    }
    
    /**
     * Setter for the row index
     * @param rowIndex the new value of the row index
     */
    public void setRowIndex(Integer rowIndex) {
        this.rowIndex = rowIndex;
    }

    /**
     * Getter for the column index
     * @return the value of the column index
     */
    public Integer getColumnIndex() {
        return this.columnIndex;
    }
    
    /**
     * Setter for the column index
     * @param columnIndex the new value of the column index
     */
    public void setColumnIndex(Integer columnIndex) {
        this.columnIndex = columnIndex;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("[QuestionResponseElementMetadata: ")
                .append("questionResponseId=").append(getQuestionResponseId())
                .append(", text=").append(getText())
                .append(", rowText=").append(getRowText())
                .append(", rowIndex=").append(getRowIndex())
                .append(", columnIndex=").append(getColumnIndex())
                .append("]").toString();
    }
}
