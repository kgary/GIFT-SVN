/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.google.gwt.user.client.rpc.IsSerializable;

import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;
import mil.arl.gift.common.survey.Survey.SurveyTypeEnum;

/**
 * A lightweight overview of a survey
 *
 * @author jleonard
 */
public class SurveyHeader implements IsSerializable {

    private int id;

    /**
     * The folder the survey is in.  Can be empty or null.
     */
    private String folder;

    /**
     * The name of the survey
     */
    private String name;

    /**
     * The number of pages in the survey.  Must be non-negative.
     */
    private int pageCount;

    /**
     * The number of elements in the survey.  Can be zero.
     */
    private int elementCount;
    
    /**
     * the number of questions in the survey.  Will be equal to or less than {@link #elementCount}.
     */
    private int questionCount;
    
    /**
     * The type of survey.  Won't be null.
     */
    private SurveyTypeEnum surveyType;
    
    /**
     * set of learner state attributes scored by this survey.  Can be empty but not null.
     */
    private Set<LearnerStateAttributeNameEnum> learnerStateAttrs;
    
    private HashSet<String> visibleToUserNames = new HashSet<String>();
    
    private HashSet<String> editableToUserNames = new HashSet<String>();
    
    /**
     * lock status of the survey
     */
    private boolean locked = false;

    /**
     * Default Constructor
     *
     * Required by IsSerializable to exist and be public
     */
    @SuppressWarnings("unused")
    private SurveyHeader() {
    }

    /**
     * Constructor
     * 
     * @param id The ID of the survey. Must be greater than zero.
     * @param folder The folder the survey is in.  Can be empty or null.
     * @param name The name of the survey
     * @param pageCount The number of pages in the survey.  Must be non-negative.
     * @param elementCount The number of elements in the survey.  Can be zero.
     * @param questionCount the number of questions in the survey.  Must be equal to or less than {@link #elementCount}.
     * @param visibleToUserNames User names that can see the question
     * @param editableToUserNames User names that can edit the question
     */
    public SurveyHeader(int id, String folder, String name, int pageCount, 
            int elementCount, int questionCount, SurveyTypeEnum surveyType, 
            Set<LearnerStateAttributeNameEnum> learnerStateAttrs,
            Collection<String> visibleToUserNames, Collection<String> editableToUserNames) {
        
        if(id <= 0) {
            throw new IllegalArgumentException("The survey id is less than 0");
        }else if(pageCount < 0) {
            throw new IllegalArgumentException("The page count is less than 0");
        }else if(elementCount < 0) {
            throw new IllegalArgumentException("The element count is less than 0");
        }else if(questionCount < 0) {
            throw new IllegalArgumentException("The question count is less than 0");
        }else if(elementCount < questionCount) {
            throw new IllegalArgumentException("The element count ("+elementCount+") is less than the question count ("+questionCount+")");
        }else if(surveyType == null) {
            throw new IllegalArgumentException("The survey type is null");
        }else if(learnerStateAttrs == null) {
            throw new IllegalArgumentException("The learner state attributes is null");
        }

        this.id = id;
        this.folder = folder;
        this.name = name;
        this.pageCount = pageCount;
        this.elementCount = elementCount;
        this.questionCount = questionCount;
        this.surveyType = surveyType;
        this.learnerStateAttrs = learnerStateAttrs;
        
        if(visibleToUserNames != null) {
        	this.visibleToUserNames.addAll(visibleToUserNames);
        }
        
        if(editableToUserNames != null) {
        	this.editableToUserNames.addAll(editableToUserNames);
        }
    }

    /**
     * Gets the ID of the survey
     * 
     * @return The ID of the survey. Will be greater than zero. 
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the folder the survey is in
     * 
     * @return The folder the survey is in. Can be null or empty.
     */
    public String getFolder() {
        return folder;
    }

    /**
     * Gets the name of the survey
     * 
     * @return The name of the survey
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the number of pages in the survey
     * 
     * @return The number of pages in the survey.  Must be non-negative.
     */
    public int getPageCount() {
        return pageCount;
    }

    /**
     * Gets the number of elements in the survey
     * 
     * @return The number of elements in the survey.  Can be zero.
     */
    public int getElementCount() {
        return elementCount;
    }
    
    /**
     * Return the number of questions in the survey.  
     * @return Must be equal to or less than {@link #elementCount}.
     */
    public int getQuestionCount() {
        return questionCount;
    }
    
    /**
     * Return the type of survey. 
     * @return  Won't be null.
     */
    public SurveyTypeEnum getSurveyType() {
        return surveyType;
    }
    
    /**
     * Gets the lock status of the survey.
     * @return True if the survey is locked, false otherwise.
     */
    public boolean isLocked() {
		return locked;
	}

    /**
     * Sets the survey header's lock status.
     * @param locked True if it is locked, false otherwise.
     */
	public void setLocked(boolean locked) {
		this.locked = locked;
	}
	
	/**
	 * Return the learner state attributes scored by this survey.
	 * @return can be empty but not null.
	 */
	public Set<LearnerStateAttributeNameEnum> getLearnerStateAttributes(){
	    return learnerStateAttrs;
	}

    public HashSet<String> getVisibleToUserNames() {
		return visibleToUserNames;
	}

	public void setVisibleToUserNames(HashSet<String> visibleToUserNames) {
		this.visibleToUserNames = visibleToUserNames;
	}

	public HashSet<String> getEditableToUserNames() {
		return editableToUserNames;
	}

	public void setEditableToUserNames(HashSet<String> editableToUserNames) {
		this.editableToUserNames = editableToUserNames;
	}

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[SurveyHeader: id=");
        builder.append(id);
        builder.append(", surveyType=");
        builder.append(surveyType);
        builder.append(", folder=");
        builder.append(folder);
        builder.append(", name=");
        builder.append(name);
        builder.append(", learnerStateAttrs=");
        builder.append(learnerStateAttrs);
        builder.append(", pageCount=");
        builder.append(pageCount);
        builder.append(", elementCount=");
        builder.append(elementCount);
        builder.append(", questionCount=");
        builder.append(questionCount);
        builder.append(", visibleToUserNames=");
        builder.append(visibleToUserNames);
        builder.append(", editableToUserNames=");
        builder.append(editableToUserNames);
        builder.append(", locked=");
        builder.append(locked);
        builder.append("]");
        return builder.toString();
    }
	
	
}
