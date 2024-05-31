/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.ums.db.table;

import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.UniqueConstraint;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@Table(name = "surveyelement", uniqueConstraints =
@UniqueConstraint(columnNames = {"surveyPageId_FK", "elementNumber"}))
public class DbSurveyElement {

    private int surveyElementId;

    private DbSurveyPage surveyPage;

    private DbSurveyElementType surveyElementType;

    private int questionId;

    private int elementNumber;

    private Set<DbSurveyElementProperty> properties;

    /**
     * Default Constructor
     */
    public DbSurveyElement() {
    }

    @Id
    @Column(name = "surveyElementId_PK")
    @TableGenerator(name = "surveyElementId", table = "surveyelementpktb", pkColumnName = "surveyElementKey", pkColumnValue = "surveyElementValue", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "surveyElementId")
    public int getSurveyElementId() {

        return surveyElementId;
    }

    public void setSurveyElementId(int surveyElementId) {

        this.surveyElementId = surveyElementId;
    }

    //if a survey question is deleted, don't want survey page to be deleted
    @ManyToOne
    @JoinColumn(name = "surveyPageId_FK", nullable = false)
    public DbSurveyPage getSurveyPage() {

        return surveyPage;
    }

    public void setSurveyPage(DbSurveyPage surveyPage) {

        this.surveyPage = surveyPage;
    }

    //if a survey element is deleted, don't want the survey element type to be deleted
    @ManyToOne
    @JoinColumn(name = "surveyElementTypeId_FK", nullable = false)
    public DbSurveyElementType getSurveyElementType() {

        return surveyElementType;
    }

    public void setSurveyElementType(DbSurveyElementType surveyElementType) {

        this.surveyElementType = surveyElementType;
    }

    @ForeignKey(name = "questionId")
    @Column(name = "questionId_FK", nullable = true)
    public int getQuestionId() {

        return questionId;
    }

    public void setQuestionId(int questionId) {

        this.questionId = questionId;
    }

    @Column(nullable = false)
    public int getElementNumber() {

        return elementNumber;
    }

    public void setElementNumber(int elementNumber) {

        this.elementNumber = elementNumber;
    }

    //created bi-directional 
    //now when the survey element is deleted, the survey element properties are deleted
    @OneToMany(mappedBy = "surveyElement", targetEntity = DbSurveyElementProperty.class, cascade = CascadeType.ALL)
    @LazyCollection(LazyCollectionOption.FALSE)
    public Set<DbSurveyElementProperty> getProperties() {

        return properties;
    }

    public void setProperties(Set<DbSurveyElementProperty> properties) {

        this.properties = properties;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {

            return true;
        }

        if (obj instanceof DbSurveyElement) {

            DbSurveyElement surveyQuestion = (DbSurveyElement) obj;

            return surveyQuestion.getSurveyElementId() != 0 && surveyQuestion.getSurveyElementId() == this.getSurveyElementId();
        }

        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + this.surveyElementId;
        return hash;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[DbSurveyElement:");
        sb.append(" id = ").append(getSurveyElementId());
        sb.append(", surveyPage = ").append(getSurveyPage());
        sb.append(", surveyElementType = ").append(getSurveyElementType());
        sb.append(", questionId = ").append(getQuestionId());
        sb.append(", elementNumber = ").append(getElementNumber());
        sb.append(", properties = {");
        for (DbSurveyElementProperty property : properties) {

            sb.append(property.toString()).append(", ");
        }

        sb.append("}");
        sb.append("]");

        return sb.toString();
    }
}
