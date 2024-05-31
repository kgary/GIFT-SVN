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
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@Table(name = "question")
public class DbQuestion {

    private int questionId;

    private String text;

    private DbQuestionType questionType;

    private Set<DbCategory> categories;

    private Set<DbQuestionProperty> questionProperties;
    
    private Set<String> visibleToUserNames;
    
    private Set<String> editableToUserNames;

    /**
     * Default Constructor
     */
    public DbQuestion() {
    }

    /**
     * Class constructor
     *
     * @param text the content of the question
     * @param questionType the type of question this is
     */
    public DbQuestion(String text, DbQuestionType questionType) {
        this.text = text;
        this.questionType = questionType;
    }

    //primary key, auto-generated sequentially
    @Id
    @Column(name = "questionId_PK")
    @TableGenerator(name = "questionId", table = "questionpktb", pkColumnName = "questionkey", pkColumnValue = "questionvalue", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "questionId")
    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }
    
    @ElementCollection
    @LazyCollection(LazyCollectionOption.FALSE)
    @CollectionTable(name="QuestionVisibleToUserNames")
    @Column(name="UserName")
    public Set<String> getVisibleToUserNames() {
		return visibleToUserNames;
	}

	public void setVisibleToUserNames(Set<String> visibleToUserNames) {
		this.visibleToUserNames = visibleToUserNames;
	}
    
    @ElementCollection
    @LazyCollection(LazyCollectionOption.FALSE)
    @CollectionTable(name="QuestionEditableToUserNames")
    @Column(name="UserName")
    public Set<String> getEditableToUserNames() {
		return editableToUserNames;
	}

	public void setEditableToUserNames(Set<String> editableToUserNames) {
		this.editableToUserNames = editableToUserNames;
	}

	//can't be null
    @Column(nullable = false, length = 32000)
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    //if a question is deleted, don't want the question type to be deleted
    @ManyToOne
    @JoinColumn(name = "questionTypeId", nullable = false)
    public DbQuestionType getQuestionType() {
        return questionType;
    }

    public void setQuestionType(DbQuestionType questionType) {
        this.questionType = questionType;
    }

    //created bi-directional 
    //now when question category is deleted, the category questions are deleted
    @ManyToMany(cascade = CascadeType.ALL/* , fetch = FetchType.EAGER */)
    @JoinTable(name = "questioncategory", joinColumns = {
        @JoinColumn(name = "questionId_FK")}, inverseJoinColumns = {
        @JoinColumn(name = "questionCategoryId_FK")})
    @LazyCollection(LazyCollectionOption.FALSE)
    public Set<DbCategory> getCategories() {
        return categories;
    }

    public void setCategories(Set<DbCategory> categories) {
        this.categories = categories;
    }

    //created bi-directional 
    //now when question is deleted, the question questionProperties are deleted
    @OneToMany(targetEntity = DbQuestionProperty.class, mappedBy = "question", cascade = CascadeType.ALL/* , fetch=FetchType.LAZY */)
    @LazyCollection(LazyCollectionOption.FALSE)
    public Set<DbQuestionProperty> getQuestionProperties() {
        return questionProperties;
    }

    public void setQuestionProperties(Set<DbQuestionProperty> properties) {
        this.questionProperties = properties;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {

            return true;
        }

        if (obj instanceof DbQuestion) {

            DbQuestion question = (DbQuestion) obj;

            return question.getQuestionId() != 0 && question.getQuestionId() == this.getQuestionId();
        }

        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + this.questionId;
        return hash;
    }

    /**
     * Return a string representation of this class
     *
     * @return String - a string representation of this class
     */
    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[Question:");
        sb.append(" questionId = ").append(getQuestionId());
        sb.append(", text = ").append(getText());
        sb.append(", question type = ").append(getQuestionType());
        sb.append(", properties = {");
        for (DbQuestionProperty property : questionProperties) {
            sb.append(property.toString()).append(", ");
        }
        sb.append("}");
        sb.append(", categories = {");
        for (DbCategory category : categories) {
            sb.append(category.toString()).append(", ");
        }
        sb.append("}");
        sb.append("]");

        return sb.toString();
    }
}
