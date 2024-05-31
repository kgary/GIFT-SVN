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
import java.util.List;

/**
 * A page in a survey
 *
 * @author jleonard
 */
public class SurveyPage implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private int surveyPageId;

    private String name;

    private int surveyId;

    private List<AbstractSurveyElement> surveyElements = new ArrayList<AbstractSurveyElement>();

    private SurveyItemProperties properties = new SurveyItemProperties();

    /**
     * Default Constructor
     *
     * Required by GWT to exist and be public in order to be compatible
     */
    public SurveyPage() {
    }

    /**
     * Constructor
     *
     * @param id The ID of the survey page
     * @param name The name of the survey page
     */
    public SurveyPage(int id, String name) {
        this.surveyPageId = id;
        this.name = name;
    }

    /**
     * Class constructor - set required attributes
     *
     * @param surveyPageId The ID of the survey
     * @param name The folder the survey is in
     * @param surveyId The ID of the survey this page is in
     */
    public SurveyPage(int surveyPageId, String name, int surveyId) {

        this.surveyPageId = surveyPageId;
        this.name = name;
        this.surveyId = surveyId;
    }

    /**
     * Class constructor - set required attributes
     *
     * @param surveyPageId The ID of the survey
     * @param name The folder the survey is in
     * @param surveyId The ID of the survey this page is in
     * @param surveyElements The elements of the survey page
     * @param properties The properties of the survey question
     */
    public SurveyPage(int surveyPageId, String name, int surveyId, List<AbstractSurveyElement> surveyElements, SurveyItemProperties properties) {
        this(surveyPageId, name, surveyId);

        this.surveyElements = surveyElements;
        this.properties = properties;
    }

    /**
     * Gets the ID of the survey page
     *
     * @return int The ID of the survey page
     */
    public int getId() {
        return surveyPageId;
    }

    /**
     * Sets the ID of the survey page
     *
     * @param id The ID of the survey page
     */
    public void setId(int id) {
        this.surveyPageId = id;
    }

    /**
     * Gets the name of the survey page
     *
     * @return String The name of the survey page
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the survey page
     *
     * @param name The name of the survey page
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the ID of the survey this page is in
     *
     * @return int The ID of the survey this page is in
     */
    public int getSurveyId() {
        return surveyId;
    }

    /**
     * Sets the ID of the survey this page is in
     *
     * @param surveyId The ID of the survey this page is in
     */
    public void setSurveyId(int surveyId) {
        this.surveyId = surveyId;
    }

    /**
     * Gets the survey elements on this survey page
     *
     * @return List<AbstractSurveyElement> The list of survey elements on this
     * page
     */
    public List<AbstractSurveyElement> getElements() {
        return surveyElements;
    }

    /**
     * Sets the survey elements on this survey page
     *
     * @param surveyElements The survey elements on this survey page
     */
    public void setElements(List<AbstractSurveyElement> surveyElements) {
        this.surveyElements = surveyElements;
    }

    /**
     * Gets the properties for the survey page
     *
     * @return Map<SurveyPropertyKeyEnum, String> The properties for the survey
     * page
     */
    public SurveyItemProperties getProperties() {

        return properties;
    }

    /**
     * Sets the properties for the survey page
     *
     * @param properties The properties for the survey page
     */
    public void setProperties(SurveyItemProperties properties) {

        this.properties = properties;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[Survey Page: ");
        sb.append("id = ").append(getId());
        sb.append(", name = ").append(getName());
        sb.append(", survey id = ").append(getSurveyId());

        sb.append(", questions = {");
        for (AbstractSurveyElement element : getElements()) {
            sb.append(element).append(", ");
        }
        sb.append("}");
        sb.append(", properties = ").append(getProperties());
        sb.append("]");

        return sb.toString();
    }
    
    /**
     * Modifies any media file URLs within this survey page so that the given host URL is prepended to them.
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
        
        for(AbstractSurveyElement element : getElements()) {
            element.applySurveyMediaHost(originUri);
        }
    }
}
