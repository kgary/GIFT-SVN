/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.ums.db.table;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "questionresponse")
public class DbQuestionResponse {

    private int questionResponseId;

    private String text;

    private DbOptionList textOptionList;

    private String rowText;

    private DbOptionList rowTextOptionList;

    private DbSurveyElement surveyQuestion;

    private DbSurveyPageResponse surveyPageResponse;

    private Date answerTime;

    /**
     * Default Constructor
     */
    public DbQuestionResponse() {
    }

    /**
     * Class constructor
     *
     * @param text - the response to the survey question
     * @param textOptionList - the option list the answer came from
     * @param rowText - the text of the row the response came from
     * @param rowTextOptionList - the option list for the row the answer came from
     * @param surveyQuestion - the survey question asked
     * @param surveyPageResponse - the survey the question was asked
     * @param answerTime how long it took to answer the question
     */
    public DbQuestionResponse(String text, DbOptionList textOptionList, String rowText, DbOptionList rowTextOptionList, DbSurveyElement surveyQuestion, DbSurveyPageResponse surveyPageResponse, Date answerTime) {
        this.text = text;
        this.textOptionList = textOptionList;
        this.rowText = rowText;
        this.rowTextOptionList = rowTextOptionList;
        this.surveyQuestion = surveyQuestion;
        this.surveyPageResponse = surveyPageResponse;
        this.answerTime = answerTime;
    }

    //primary key, auto-generated sequentially
    @Id
    @Column(name = "questionResponseId_PK")
    @TableGenerator(name = "questionResponseId", table = "questionresponsepktb", pkColumnName = "questionResponsekey", pkColumnValue = "questionResponsevalue", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "questionResponseId")
    public int getQuestionResponseId() {
        return questionResponseId;
    }

    public void setQuestionResponseId(int questionResponseId) {
        this.questionResponseId = questionResponseId;
    }

    //can't be null
    @Column(nullable = false, length = 32000)
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    //if a reponse is deleted, don't want the option list to be deleted
    @ManyToOne
    @JoinColumn(name = "textOptionListId_FK")
    public DbOptionList getTextOptionList() {
        return textOptionList;
    }

    public void setTextOptionList(DbOptionList textOptionList) {
        this.textOptionList = textOptionList;
    }

    public String getRowText() {
        return rowText;
    }

    public void setRowText(String rowText) {
        this.rowText = rowText;
    }

    //if a reponse is deleted, don't want the option list to be deleted
    @ManyToOne
    @JoinColumn(name = "rowTextOptionListId_FK")
    public DbOptionList getRowTextOptionList() {
        return rowTextOptionList;
    }

    public void setRowTextOptionList(DbOptionList rowTextOptionList) {
        this.rowTextOptionList = rowTextOptionList;
    }

    //if a response is deleted, don't want question to be deleted
    @ManyToOne
    @JoinColumn(name = "surveyQuestionId_FK")
    public DbSurveyElement getSurveyQuestion() {
        return surveyQuestion;
    }

    public void setSurveyQuestion(DbSurveyElement surveyQuestion) {
        this.surveyQuestion = surveyQuestion;
    }

    //if a response is deleted, don't want the survey page response to be deleted
    @ManyToOne
    @JoinColumn(name = "surveyPageResponseId_FK")
    public DbSurveyPageResponse getSurveyPageResponse() {
        return surveyPageResponse;
    }

    public void setSurveyPageResponse(DbSurveyPageResponse surveyPageResponse) {
        this.surveyPageResponse = surveyPageResponse;
    }

    @Temporal(TemporalType.TIMESTAMP)
    public Date getAnswerTime() {
        return answerTime;
    }

    public void setAnswerTime(Date answerTime) {
        this.answerTime = answerTime;
    }

    /**
     * Return a string representation of this class
     *
     * @return String - a string representation of this class
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[QuestionResponse: ");
        sb.append(" id = ").append(getQuestionResponseId());
        sb.append(", text = ").append(getText());
        sb.append(", text option list = ").append(getTextOptionList() != null ? getTextOptionList() : "null");
        sb.append(", row text = ").append(getRowText() != null ? getRowText() : "null");
        sb.append(", text = ").append(getRowTextOptionList() != null ? getRowTextOptionList() : "null");
        sb.append(", survey question = ").append(getSurveyQuestion());
        sb.append(", survey page response id = ").append(getSurveyPageResponse() != null ? getSurveyPageResponse().getSurveyPageResponseId() : "null");
        sb.append(", answer time = ").append(getAnswerTime());
        sb.append("]");
        return sb.toString();
    }
}
