/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.survey;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * A survey context
 *
 * @author jleonard
 */
public class SurveyContext implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private int id;

    private String name;

    private List<SurveyContextSurvey> surveys = new ArrayList<SurveyContextSurvey>();
    
    private HashSet<String> visibleToUserNames = new HashSet<String>();
    
    private HashSet<String> editableToUserNames = new HashSet<String>();

    /**
     * Default Constructor
     * 
     * Required by IsSerializable to exist and be public
     */
    public SurveyContext() {
    }

    /**
     * Constructor
     *
     * @param context The GwtSurveyContext to copy
     */
    public SurveyContext(SurveyContext context) {
        this.id = context.getId();
        this.name = context.getName();
        this.surveys.addAll(context.getContextSurveys());
        this.visibleToUserNames.addAll(context.getVisibleToUserNames());
        this.editableToUserNames.addAll(context.getEditableToUserNames());
    }

    /**
     * Constructor
     *
     * @param name The name of the survey context
     */
    public SurveyContext(String name) {
        this.name = name;
    }

    /**
     * Constructor
     *
     * @param id The ID of the survey context
     * @param name The name of the survey context
     * @param surveys The list of surveys in this context
     * @param visibleToUserNames User names that can see the question
     * @param editableToUserNames User names that can edit the question
     */
    public SurveyContext(int id, String name, List<SurveyContextSurvey> surveys, Collection<String> visibleToUserNames, Collection<String> editableToUserNames) {
        this.id = id;
        this.name = name;
        this.surveys = surveys;
        
        if(visibleToUserNames != null) {
        	this.visibleToUserNames.addAll(visibleToUserNames);
        }
        
        if(editableToUserNames != null) {
        	this.editableToUserNames.addAll(editableToUserNames);
        }
    }

    public int getId() {
        return id;
    }

    /**
     * Sets the ID of the survey context
     *
     * @param id The ID of the survey context
     */
    public void setId(int id) {
        this.id = id;
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

    /**
     * Gets the name of the survey context
     *
     * @return String The name of the survey context
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the survey context
     *
     * @param name The name of the survey context
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the surveys in the survey context
     *
     * @return ArrayList<GwtSurveyContextSurvey> The surveys in the survey
     * context
     */
    public List<SurveyContextSurvey> getContextSurveys() {
        return surveys;
    }

    /**
     * Sets the surveys in the survey context
     *
     * @param surveys The surveys in the survey context
     */
    public void setContextSurveys(List<SurveyContextSurvey> surveys) {
        this.surveys = surveys;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[SurveyContext: id = ");
        builder.append(id);
        builder.append(", name = ");
        builder.append(name);
        builder.append(", surveys = ");
        builder.append(surveys);
        builder.append(", visibleToUserNames = ");
        builder.append(visibleToUserNames);
        builder.append(", editableToUserNames = ");
        builder.append(editableToUserNames);
        builder.append("]");
        return builder.toString();
    }
    
    
}
