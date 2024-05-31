/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.survey;

import java.io.Serializable;
import java.util.Date;

/**
 * A single element of a response to a survey question
 *
 * @author jleonard
 */
public class QuestionResponseElement implements Serializable {

    private static final long serialVersionUID = 1L;

    /** the unique id of the question response.  Will be 0 until the survey database creates this id */
    private int questionResponseId;

    /** contains the text for a response.  Shouldn't be null but can be empty */
    private String text;
    
    /** contains the choice for a multiple choice, matrix of choices or rating scale question */
    private ListOption choice;

    /** the option list of the response.  Will NOT be null only for multiple choice, matrix of choices and rating scale */
    private OptionList choices;

    /** the row choice for a matrix of choices response.  Will be null for all other question types */
    private ListOption rowChoice;

    /** the row choices for a matrix of choices.  Will be null for all other question types */
    private OptionList rowChoices;
    
    /** the index of the response for the row selected for matrix of choices. Will be null for all other question types */
    private Integer rowIndex;

    /** the index of the response for the column selected. Will NOT be null for multiple choice, matrix of choice and rating scale */
    private Integer columnIndex;

    /** the time at which this answer was given.  Can't be null */
    private Date answerTime;

    /**
     * Default Constructor - don't use.
     *
     * Required by IsSerializable to exist and be public
     */
    public QuestionResponseElement() {
    }

    /**
     * Constructor - used for free response, highlight passage, summarize passage and slider questions.
     *
     * @param answer The answer to the question.  Can't be null but can be empty.
     * @param answerTime When the question was answered.  Can't be null.
     */
    public QuestionResponseElement(String answer, Date answerTime) {
        setText(answer);
        setAnswerTime(answerTime);
    }

    /**
     * Constructor - used for a single choice to a multiple choice or rating scale questions.
     *
     * @param columnIndex the index of the answer to the question. Can't be null.
     * @param ListOption The answer to the question.  Can't be null.
     * @param textOptionList The option list where the answer came from
     * @param answerTime When the question was answered. Can't be null.
     */
    public QuestionResponseElement(Integer columnIndex, ListOption choice, OptionList choices, Date answerTime) {
        setColumnIndex(columnIndex);
        setChoice(choice);
        setText(choice.getText());
        setChoices(choices);
        setAnswerTime(answerTime);
    }

    /**
     * Constructor - used for a single choice in a matrix of choices question. 
     *
     * @param columnIndex the column index to the answer of the question. Can't be null.
     * @param columnChoice The column answer to the question.  Can't be null.
     * @param columnChoices The possible column choices where the answer came from.  Can't be null.
     * @param rowIndex The row index to the answer of the row of the question. Can't be null.
     * @param rowChoice The row answer of the row of the question.  Can't be null.
     * @param rowChoices The possible row choices where the answer came from.  Can't be null.
     * @param answerTime When the question was answered.  Can't be null.
     */
    public QuestionResponseElement(Integer columnIndex, ListOption columnChoice, OptionList columnChoices, Integer rowIndex, ListOption rowChoice, OptionList rowChoices, Date answerTime) {
        setColumnIndex(columnIndex);
        setChoice(columnChoice);
        setText(columnChoice.getText());
        setChoices(columnChoices);
        setRowIndex(rowIndex);
        setRowChoice(rowChoice);
        setRowChoices(rowChoices);
        setAnswerTime(answerTime);
    }

    /**
     * Constructor - used when reading in a question response from the survey database.
     *
     * @param text The text of this response.  Can't be null.
     * @param choices The option list where the answer came from.  Will not be null for multiple choice, rating scale and matrix of choices.
     * @param rowText The text of the row of the question.  Will not be null for matrix of choices.
     * @param rowChoices The option list where the row text came from.  Will not be null for matrix of choices.
     * @param answerTime When the question was answered.  Can't be null.
     */
    public QuestionResponseElement(String text, OptionList choices, String rowText, OptionList rowChoices, Date answerTime) {

        setText(text);
        this.choices = choices; //don't use the setChoices method as legacy messages can have choices be null.
        this.rowChoices = rowChoices;  //don't use the setRowChoices method as legacy messages can have rowChoices be null.
        setAnswerTime(answerTime);
        
        //Multiple choice, Matrix of choices and rating scale 'choice' list option
        if(choices != null){
            for(int i=0; i<choices.getListOptions().size(); i++) {
                ListOption choice = choices.getListOptions().get(i);
                
                if(choice.getText().equalsIgnoreCase(text)){
                    //found match
                    setColumnIndex(i);
                    setChoice(choice);
                    break;
                }
            }
            
            if(getChoice() == null){
                // choice not found in current question's choices, create new choice for in memory usage only.
                setChoice(new ListOption(0, text));
            }
        }
        
        //Matrix of choices 'row choice' list option
        if(rowChoices != null && rowText != null){
            for(int i=0; i<rowChoices.getListOptions().size(); i++) {
                ListOption rowChoice = rowChoices.getListOptions().get(i);
                
                if(rowChoice.getText().equalsIgnoreCase(rowText)){
                    //found match
                    setRowIndex(i);
                    setRowChoice(rowChoice);
                    break;
                }
            }
            
            if(getRowChoice() == null){
                //row choice not found in current question's row choices, create new row choice for in memory usage only.
                setRowChoice(new ListOption(0, rowText));
            }
        }
    }

    /**
     * Gets the ID of the response
     *
     * @return int The ID of the response
     */
    public int getQuestionResponseId() {
        return questionResponseId;
    }

    /**
     * Sets the ID of the response
     *
     * @param questionResponseId The ID of the response.  Must be greater than 0.
     */
    public void setQuestionResponseId(int questionResponseId) {
        
        if(questionResponseId < 1){
            throw new IllegalArgumentException("The response id "+questionResponseId+" is not valid.");
        }
        
        this.questionResponseId = questionResponseId;
    }

    /**
     * Gets the answer of the response
     *
     * @return String The answer of the response. Can be null if there is a question widget with
     *         multiple responses and one or more of the responses were not answered.
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the answer of the response
     *
     * @param text The answer of the response.  Can't be null but can be empty.
     */
    private void setText(String text) {
        
        if(text == null){
            throw new IllegalArgumentException("The answer text can't be null.");
        }
        
        this.text = text;
    }

    /**
     * Return the choice list option.
     * 
     * @return will not be null for matrix of choices, multiple choice and rating scale.
     */
    public ListOption getChoice() {
        return choice;
    }

    /**
     * Set the choice list option.
     * 
     * @param choice can't be null.
     */
    private void setChoice(ListOption choice) {
        
        if(choice == null){
            throw new IllegalArgumentException("The choice can't be null.");
        }
        
        this.choice = choice;
    }

    /**
     * Gets the option list the answer came from
     *
     * @return The option list the answer came from.  Will not be null for multiple choice, rating scale and matrix of choices.
     */
    public OptionList getChoices() {
        return choices;
    }

    /**
     * Sets the option list the answer came from
     *
     * @param choices The option list the answer came from.  Can't be null.
     */
    private void setChoices(OptionList choices) {
        
        if(choices == null){
            throw new IllegalArgumentException("The choices can't be null.");
        }
        
        this.choices = choices;
    }

    /**
     * Gets the row of the response
     *
     * @return The row response for matrix of choices.  Will be null for all other question types.
     */
    public ListOption getRowChoice() {
        return rowChoice;
    }

    /**
     * Sets the row choice of a matrix of choices question response
     *
     * @param rowChoice The text of the row of the response. Can't be null.
     */
    private void setRowChoice(ListOption rowChoice) {
        
        if(rowChoice == null){
            throw new IllegalArgumentException("The rowChoice can't be null.");
        }
        
        this.rowChoice = rowChoice;
    }

    /**
     * Gets the option list the row text came from
     *
     * @return The option list the row text came from.  Will not be null for multiple choice.
     */
    public OptionList getRowChoices() {
        return rowChoices;
    }

    /**
     * Sets the option list for a matrix of choices row
     *
     * @param rowChoices The option list the row text came from.  Can't be null.
     */
    private void setRowChoices(OptionList rowChoices) {
        
        if(rowChoices == null){
            throw new IllegalArgumentException("The row choices can't be null.");
        }
        
        this.rowChoices = rowChoices;
    }

    /**
     * Gets the row index of the selected response
     *
     * @return The index of the row for the selected response. Will be null for all other question types.
     */
    public Integer getRowIndex() {
        return rowIndex;
    }

    /**
     * Sets the row index for a matrix of choices response
     *
     * @param rowIndex The index of the row for the selected response.
     */
    private void setRowIndex(Integer rowIndex) {
        
        if(rowIndex == null){
            throw new IllegalArgumentException("The row index can't be null.");
        }
        
        this.rowIndex = rowIndex;
    }

    /**
     * Gets the column index of the selected response
     *
     * @return The index of the column for the selected response. Will not be null for multiple choice.
     */
    public Integer getColumnIndex() {
        return columnIndex;
    }

    /**
     * Sets the column index for a multiple choice response
     *
     * @param columnIndex The index of the column for the selected response.
     */
    private void setColumnIndex(Integer columnIndex) {
        
        if(columnIndex == null){
            throw new IllegalArgumentException("The column index can't be null.");
        }
        
        this.columnIndex = columnIndex;
    }

    /**
     * Gets the time the question was answered
     *
     * @return Date The time the question was answered. Can be null if there is a question widget
     *         with multiple responses and one or more of the responses were not answered.
     */
    public Date getAnswerTime() {
        return answerTime;
    }

    /**
     * Sets the time the question was answered
     *
     * @param answerTime The time the question was answered. Can't be null.
     */
    private void setAnswerTime(Date answerTime) {
        
        if(answerTime == null){
            throw new IllegalArgumentException("The answer time can't be null.");
        }
        
        this.answerTime = answerTime;
    }

    @Override
    public int hashCode() {
        return this.getQuestionResponseId(); //guaranteed to be unique
    }

    @Override
    public boolean equals(Object o) {

        if (o == null) {
            return false;
        } else if (o instanceof QuestionResponseElement) {
            return this.getQuestionResponseId() == ((QuestionResponseElement) o).getQuestionResponseId();
        }

        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[QuestionResponseElement: ");
        sb.append(" id = ").append(getQuestionResponseId());
        sb.append(", text = ").append(getText());
        sb.append(", choice = ").append(getChoice() != null ? getChoice() : "null");
        sb.append(", choice option list = ").append(getChoices() != null ? getChoices() : "null");
        sb.append(", row choice = ").append(getRowChoice() != null ? getRowChoice() : "null");
        sb.append(", row choice option list = ").append(getRowChoices() != null ? getRowChoices() : "null");
        sb.append(", answer time = ").append(getAnswerTime());
        sb.append("]");
        return sb.toString();
    }
}
