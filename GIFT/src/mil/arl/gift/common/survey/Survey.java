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

import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.common.survey.score.SurveyScorer;

/**
 * A survey of questions
 *
 * @author jleonard
 */
public class Survey implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private int id;

    private String folder;

    private String name;

    private List<SurveyPage> surveyPages = new ArrayList<SurveyPage>();
    
    private HashSet<String> visibleToUserNames = new HashSet<String>();
    
    private HashSet<String> editableToUserNames = new HashSet<String>();

    private SurveyProperties properties;
    
    /**
     * The enumerated survey types in GIFT
     * 
     * @author mhoffman
     *
     */
    public enum SurveyTypeEnum{
        COLLECTINFO_SCORED("COLLECTINFO_SCORED", "Scored"),
        COLLECTINFO_NOTSCORED("COLLECTINFO_NOTSCORED", "Not Scored"),
        ASSESSLEARNER_STATIC("ASSESSLEARNER_STATIC", "Knowledge"),
        ASSESSLEARNER_QUESTIONBANK("ASSESSLEARNER_QUESTIONBANK", "QBank");
        
        /** 
         * enum unique name
         */
        private final String name;
        
        /**
         * enum display name
         */
        private final String displayName;
        
        /**
         * Set attributes
         * @param name unique enum name, shouldn't be null or empty (But exception not thrown because its an enum)
         * @param displayName the display name for the enum, shouldn't be null or empty (But exception not thrown because its an enum)
         */
        private SurveyTypeEnum(String name, String displayName) {
            this.name = name;
            this.displayName = displayName;
        }
        
        /**
         * Return the unique enun name
         * @return won't be null or empty.
         */
        public String getName() {
            return name;
        }
        
        /**
         * Return the display name for the enum
         * @return won't be null or empty.
         */
        public String getDisplayName() {
            return displayName;
        }        
        
    }

    /**
     * Default Constructor
     *
     * Required by IsSerializable to exist and be public
     */
    public Survey() {
    }

    /**
     * Constructor
     *
     * @param folder The folder the survey is in
     * @param name The name of the survey
     */
    public Survey(String folder, String name) {
        this.folder = folder;
        this.name = name;
    }

    /**
     * Class constructor - set required attributes
     *
     * @param id The ID of the survey
     * @param name The folder the survey is in
     * @param surveyPages The pages of the survey
     * @param folder The folder the survey is in
     * @param properties The properties of the survey question
     * @param visibleToUserNames User names that can see the question
     * @param editableToUserNames User names that can edit the question
     */
    public Survey(int id, String name, List<SurveyPage> surveyPages, String folder, SurveyProperties properties, Collection<String> visibleToUserNames, Collection<String> editableToUserNames) {

        this.id = id;
        this.name = name;
        this.surveyPages = surveyPages;
        this.folder = folder;
        this.properties = properties;
        
        if(visibleToUserNames != null) {
        	this.visibleToUserNames.addAll(visibleToUserNames);
        }
        
        if(editableToUserNames != null) {
        	this.editableToUserNames.addAll(editableToUserNames);
        }
    }

    /**
     * Creates an equivalent survey from a given SurveyResponse.
     * <br/><br/><b>WARNING:</b> This is not the actual survey that was 
     * taken and therefore is not exactly the same as the survey that was 
     * actually taken by the user. Should only be used for display purposes.
     * @param response the response to create the survey from, can't be null
     * @return a Survey that emulates the Survey that the SurveyResponse 
     * responded to 
     */
    public static Survey createFromResponse(SurveyResponse response) {
        if(response == null) {
            throw new IllegalArgumentException("response can't be null");
        }
        
        Survey toRet = new Survey();
        
        toRet.setProperties(new SurveyProperties());
        
        //Adds all the pages to the survey
        for(SurveyPageResponse pageResponse : response.getSurveyPageResponses()) {
            toRet.getPages().add(pageResponse.getSurveyPage());
        }
        
        toRet.setId(response.getSurveyId());
        toRet.setName(response.getSurveyName());
        toRet.setScorerModel(response.getSurveyScorerModel());
        
        return toRet;
    }
    
    /**
     * Gets the ID of the survey
     *
     * @return The ID of the survey
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the ID of the survey
     *
     * @param id The ID of the survey
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the folder the survey is in
     *
     * @return GwtFolder The folder the survey is in
     */
    public String getFolder() {
        return folder;
    }

    /**
     * Sets the folder the survey is in
     *
     * @param folder The folder the survey is in
     */
    public void setFolder(String folder) {
        this.folder = folder;
    }

    /**
     * Gets the name of the survey
     *
     * @return String The name of the survey
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the survey
     *
     * @param name The name of the survey
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the pages of the survey
     *
     * @return ArrayList<GwtSurveyPage> The pages of the survey
     */
    public List<SurveyPage> getPages() {
        return surveyPages;
    }

    /**
     * Sets the pages of the survey
     *
     * @param surveyPages The pages of the survey
     */
    public void setPages(ArrayList<SurveyPage> surveyPages) {
        this.surveyPages = surveyPages;
    }

    /**
     * Gets the properties for the survey
     *
     * @return Map<GwtSurveyPropertyKeyEnum, String> The properties for the
     * survey question
     */
    public SurveyProperties getProperties() {

        return properties;
    }

    /**
     * Sets the properties for the survey
     *
     * @param properties The properties for the survey
     */
    public void setProperties(SurveyProperties properties) {

        this.properties = properties;
    }

    /**
     * Gets the scorer for the survey
     *
     * @return SurveyScorer The survey scorer
     */
    public SurveyScorer getScorerModel() {
        return (SurveyScorer) getProperties().getPropertyValue(SurveyPropertyKeyEnum.SCORERS);        
    }
    
    public SurveyScorer getScorerModelForNewSurvey(){
    	return (SurveyScorer) getProperties().getPropertyValue(SurveyPropertyKeyEnum.SCORERS);
    }
    
    /**
     * Returns the survey type for this survey based on the survey property {@link #SurveyPropertyKeyEnum.SURVEY_TYPE}
     * 
     * @return null if the property is not set, otherwise the enumerated survey type
     */
    public SurveyTypeEnum getSurveyType(){        
        return getProperties().getSurveyType();
    }

    /**
     * Sets the scorer for the survey
     *
     * @param scorerModel The survey scorer
     */
    public void setScorerModel(SurveyScorer scorerModel) {

        if (scorerModel != null) {

            getProperties().setPropertyValue(SurveyPropertyKeyEnum.SCORERS, scorerModel);

        } else {

            getProperties().removeProperty(SurveyPropertyKeyEnum.SCORERS);
        }
    }

    /**
     * Returns the number of pages in the survey
     *
     * @return int the number of pages in the survey
     */
    public int getPageCount() {

        return surveyPages.size();
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
     * Returns the total number of questions across all pages in the survey
     *
     * @return int The total number of questions in the survey
     */
    public int getTotalQuestionCount() {

        int questionCount = 0;

        for (SurveyPage surveyPage : surveyPages) {

            questionCount += surveyPage.getElements().size();
        }

        return questionCount;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[Survey: ");
        sb.append("id = ").append(getId());
        sb.append(", name = ").append(getName());

        sb.append(", folder = ");
        if (getFolder() != null) {
            sb.append(getFolder());
        } else {
            sb.append("null");
        }
        sb.append(", pages = {");
        for (SurveyPage page : getPages()) {
            sb.append(page).append(", ");
        }
        sb.append("}");
        sb.append(", properties = ").append(getProperties());
        sb.append("]");
        return sb.toString();
    }
    
    /**
	 * Copies the survey, resetting ids so new objects will be created where appropriate
     * 
     * @param survey The survey to copy
     * @param userName The name of the user
     * @param userCopy if true, remove public from list of visible usernames, otherwise leave it
     * @return Survey The copied survey
     */
    public static Survey copySurvey(Survey survey, String userName, boolean userCopy) {
        
        Survey copiedSurvey = new Survey(0, survey.getName(), survey.getPages(), survey.getFolder(), survey.getProperties(), survey.getVisibleToUserNames(), survey.getEditableToUserNames());
        
        for(SurveyPage surveyPage : survey.getPages()) {
            
            surveyPage.setId(0);
            
            for(AbstractSurveyElement surveyElement : surveyPage.getElements()) {
                
                surveyElement.setId(0);
            }
        }
        
        copiedSurvey.getEditableToUserNames().add(userName);
        copiedSurvey.getVisibleToUserNames().add(userName);
        
        if(userCopy){
        	copiedSurvey.getVisibleToUserNames().remove("*");
        }
        
        return copiedSurvey;        
    }
    
    /**
     * Copies the survey, resetting ids of all survey elements (elements, pages, etc).
     * 
     * @param survey The survey to copy
     * @param userName The name of the user
     * @param userCopy if true, remove public from list of visible usernames, otherwise leave it
     * @return Survey The copied survey
     */
    public static Survey deepCopy(Survey survey, String userName, boolean userCopy) {
        
        Survey copiedSurvey = new Survey(0, survey.getName(), survey.getPages(), survey.getFolder(), survey.getProperties(), survey.getVisibleToUserNames(), survey.getEditableToUserNames());
        
        for(SurveyPage surveyPage : survey.getPages()) {
            
            surveyPage.setId(0);
            
            for(AbstractSurveyElement surveyElement : surveyPage.getElements()) {
                
                surveyElement.setId(0);
                
                if(surveyElement instanceof AbstractSurveyQuestion) {
                    @SuppressWarnings("unchecked")
                    AbstractSurveyQuestion<? extends AbstractQuestion> surveyQuestion = (AbstractSurveyQuestion<? extends AbstractQuestion>) surveyElement;
                    
                    if (surveyQuestion.getQuestion() != null) {
                        surveyQuestion.getQuestion().setQuestionId(0);
                    }
                    
                }
            }
        }
        
        copiedSurvey.getEditableToUserNames().add(userName);
        copiedSurvey.getVisibleToUserNames().add(userName);
        
        if(userCopy){
            copiedSurvey.getVisibleToUserNames().remove("*");
        }
        
        return copiedSurvey;        
    }
    
    /**
     * Modifies any media file URLs within this survey so that the given host URL is prepended to them.
     * This is needed to make sure that media files in a survey can be consistently displayed even when 
     * different hosts may be used to reach them, such as media file located in course folders that need to
     * be reached through the domain content server.
     * <br/><br/>
     * Note: This only applies to media files that use relative file paths (i.e. files uploaded to course 
     * folders). The URLs used to reach legacy media files will not be changed.
     * 
     * @param originUri the URL where media files should be served from. Cannot be null. 
     */
    public void applySurveyMediaHost(String originUri) {
        
        if(originUri == null) {
            throw new IllegalArgumentException("The origin URI used to reach survey media files cannot be null");
        }
        
        for(SurveyPage page : getPages()) {
            page.applySurveyMediaHost(originUri);
        }
    }
}
