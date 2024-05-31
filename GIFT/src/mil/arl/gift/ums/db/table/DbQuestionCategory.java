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
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "questioncategory")
public class DbQuestionCategory implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private DbCategory category;

    private DbQuestion question;

    /**
     * Default constructor - required by Hibernate
     */
    public DbQuestionCategory() {
    }

    /**
     * Class constructor
     *
     * @param questionCategory the category to associate with this instance
     * @param question the question to associate with this instance
     */
    public DbQuestionCategory(DbCategory questionCategory, DbQuestion question) {
        
        this.category = questionCategory;
        this.question = question;
    }

    //if a category question is deleted, don't want QuestionCategory to be deleted
    @ManyToOne
    @JoinColumn(name = "questionCategoryId_FK")
    @Id
    public DbCategory getCategory() {
        
        return category;
    }

    public void setCategory(DbCategory category) {
        
        this.category = category;
    }

    //if a category question is deleted, don't want question to be deleted
    @ManyToOne
    @JoinColumn(name = "questionId_FK")
    @Id
    public DbQuestion getQuestion() {
        
        return question;
    }

    public void setQuestion(DbQuestion question) {

        this.question = question;
    }

    @Override
    public int hashCode() {
        
        int hash = 3;
        hash = 43 * hash + Objects.hashCode(this.category);
        hash = 43 * hash + Objects.hashCode(this.question);
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

        final DbQuestionCategory other = (DbQuestionCategory) obj;

        if (!Objects.equals(this.category, other.getCategory())) {

            return false;
        }

        if (!Objects.equals(this.question, other.getQuestion())) {

            return false;
        }

        return true;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[QuestionCategory:");
        sb.append(" question = ").append(getQuestion());
        sb.append(", category = ").append(getCategory());
        sb.append("]");

        return sb.toString();
    }
}
