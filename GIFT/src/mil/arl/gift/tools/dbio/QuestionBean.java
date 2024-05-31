/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dbio;

import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.ift.CellProcessor;


/**
 * Java bean with field names/types corresponding to the columns in the METER CLS survey question export file.
 * Note that file was originally in XLSX format. Prior to saving as CSV, unnecessary columns were deleted, 
 * leaving just these columns: Task_number,Question_number,Question_text,Prioritization_CLS
 *  
 *  Example CSV file snippet:
 *  --------------------------------------------------------------------------------------------------------------------------------
 *    Task_number,Question_number,Question_text,Prioritization_CLS
 *    1.4,1,"Of the deaths that occur during ground combat, about what percent die before reaching a medical facility?",Important
 *    1.5,2,Your unit is in ground combat. You see a soldier fall as though he has been shot. Your primary duty is to:,Life-saving
 *  --------------------------------------------------------------------------------------------------------------------------------
 *  
 *  @author cragusa
 *
 */
public class QuestionBean {
    
    //original header: ID,Course,Category,Experience,Task_number,Question_number,Question_text,Question_type,Prioritization_CLS    
    //reduced header: Task_number,Question_number,Question_text,Prioritization_CLS
    
    /**
     * Array of CellProcessors used by this Bean when CSV file is parsed using SuperCSV
     */
    static final CellProcessor[] cellProcessors = new CellProcessor[] {
     
        null, //5 - Task_number
        new Optional(new ParseInt()), //6 - Question number
        null, //7 - Question Text
        null, //9 - Prioritization_CLS
        
    };    
    
    private String Task_number; 
    private int Question_number;
    private String Question_text;
    private String Prioritization_CLS; 
       
    /**
     * Gets the task number.  Note that this is very CLS specific and may be left out of other import batches
     * @return the task number as a string.
     */
    public String getTask_number() {
        return Task_number;
    }
    
    /**
     * Sets the task number. Again, this is very CLS specific.
     * 
     * @param task_number the unique task number
     */
    public void setTask_number(String task_number) {
        Task_number = task_number;
    }
    
    /**
     * Get the question number.
     * 
     * @return the question number as an int
     */
    public int getQuestion_number() {
        return Question_number;
    }
    
    /**
     * Set the question number
     * 
     * @param question_number the unique question number
     */
    public void setQuestion_number(int question_number) {
        Question_number = question_number;
    }
    
    /**
     * Get the question text.
     * 
     * @return the question text
     */
    public String getQuestion_text() {
        return Question_text;
    }
    
    /**
     * Sets the question text.
     * 
     * @param question_text the contents of the question
     */
    public void setQuestion_text(String question_text) {
        Question_text = question_text;
    }       
    
    /**
     * Get the CLS question priority
     * 
     * @return the question priority as as string.
     */
    public String getPrioritization_CLS() {
        return Prioritization_CLS;
    }
    
    /**
     * Sets the question priority.
     * 
     * @param prioritization_CLS the priority label of the question
     */
    public void setPrioritization_CLS(String prioritization_CLS) {
        Prioritization_CLS = prioritization_CLS;
    }
    
    @Override
    public String toString() {
        return "QuestionBean [Task_number=" + Task_number
                + ", Question_number=" + Question_number + ", Question_text="
                + Question_text + ", Prioritization_CLS=" + Prioritization_CLS
                + "]";
    }
}


