/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.ums.db.table;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "surveycontextsurvey")
public class DbSurveyContextSurvey implements Serializable {

    /**
     * default - auto generated
     */
    private static final long serialVersionUID = 1L;

    private DbSurvey survey;

    private DbSurveyContext surveyContext;

    private String giftKey;

    /**
     * Default constructor - required by Hibernate
     */
    public DbSurveyContextSurvey() {
    }

    /**
     * Class constructor
     *
     * @param surveyContext - the survey context this survey is a part of
     * @param survey - the survey that is associated with the GIFT key in the
     * survey context
     * @param giftKey - the key used to identify a survey in a survey context
     */
    public DbSurveyContextSurvey(DbSurveyContext surveyContext, DbSurvey survey, String giftKey) {
        this.surveyContext = surveyContext;
        this.survey = survey;
        this.giftKey = giftKey;
    }

    //if a SurveyContextSurvey is deleted, don't want SurveyContext to be deleted
    @ManyToOne
    @JoinColumn(name = "surveyContextId_FK")
    @Id
    public DbSurveyContext getSurveyContext() {
        return surveyContext;
    }

    public void setSurveyContext(DbSurveyContext surveyContext) {
        this.surveyContext = surveyContext;
    }

    //can't be null
    @Column(nullable = false)
    @Id
    public String getGiftKey() {
        return giftKey;
    }

    public void setGiftKey(String giftKey) {
        this.giftKey = giftKey;
    }

    //if a experiment survey is deleted, don't want survey to be deleted
    @ManyToOne
    @JoinColumn(name = "surveyId_FK", nullable = false)
    public DbSurvey getSurvey() {
        return survey;
    }

    public void setSurvey(DbSurvey survey) {
        this.survey = survey;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.surveyContext);
        hash = 53 * hash + Objects.hashCode(this.giftKey);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {

        if (obj == null) {

            return false;
        }

        if (getClass() != obj.getClass()) {

            return false;
        }

        final DbSurveyContextSurvey other = (DbSurveyContextSurvey) obj;

        if (!Objects.equals(this.surveyContext, other.getSurveyContext())) {

            return false;
        }

        if (!Objects.equals(this.giftKey, other.getGiftKey())) {

            return false;
        }

        return true;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[SurveyContextSurvey:");
        sb.append(" surveyContext = ").append(getSurveyContext());
        sb.append(", survey = ").append(getSurvey());
        sb.append(", giftKey = ").append(getGiftKey());
        sb.append("]");

        return sb.toString();
    }
}
