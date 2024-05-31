/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dbio;

import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ParseBool;
import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.ift.CellProcessor;


/**
 * Java bean with field names/types corresponding to the columns in the METER CLS survey answer export file.
 * Note that file was originally in XLSX format. Prior to saving as CSV, unnecessary columns were deleted,
 * leaving just these columns: Question_ID, Answer_text, correct.
 *  
 *  @author cragusa
 *  
 *  Example CSV file snippet including header:
 *  ---------------------------------------------------------------------
 *  Question_ID,Answer_text,Correct
 *   1,10%,0
 *   1,50%,0
 *   1,75%,0
 *   1,90%,1
 *   2,Continue firing at the enemy,1
 *   2,Stop firing and go to the fallen soldier,0
 *   ---------------------------------------------------------------------
 *
 */
public class AnswerBean {

    //original header: ID,Question_ID,Answer_text,Correct
    //reduced header: Question_ID,Answer_text,Correct
    /**
     * Array of CellProcessors used by this Bean when CSV file is parsed using
     * SuperCSV
     */
    static final CellProcessor[] cellProcessors = new CellProcessor[]{
        new Optional(new ParseInt()), // Question_ID (number)
        null, // Answer Text
        new Optional(new ParseBool()), // Correct
    };

    private int Question_ID;

    private String Answer_text;

    private boolean correct;

    /**
     * Get the question ID (foreign key)
     *
     * @return int Get ID of the question
     */
    public int getQuestion_ID() {
        return Question_ID;
    }

    /**
     * Sets the question ID (foreign key)
     *
     * @param question_ID a unique id of a question
     */
    public void setQuestion_ID(int question_ID) {
        Question_ID = question_ID;
    }

    /**
     * Get the answer text
     *
     * @return the answer text
     */
    public String getAnswer_text() {
        return Answer_text;
    }

    /**
     * Set the answer text
     *
     * @param answer_text the contents of an answer
     */
    public void setAnswer_text(String answer_text) {
        Answer_text = answer_text;
    }

    /**
     * Determine if this answer is the correct answer.
     *
     * @return true if this is the correct answer, otherwise false.
     */
    public boolean isCorrect() {
        return correct;
    }

    /**
     * Sets if this is the correct answer (or not)
     *
     * @param correct is the answer correct
     */
    public void setCorrect(boolean correct) {
        this.correct = correct;
    }

    @Override
    public String toString() {
        return "AnswerBean [Question_ID=" + Question_ID + ", Answer_text="
                + Answer_text + ", correct=" + correct + "]";
    }
}
