/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.survey;

import java.io.Serializable;

import mil.arl.gift.common.enums.SurveyElementTypeEnum;
import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.util.StringUtils;

/**
 * An abstract representation of an element in a survey.
 *
 * @author jleonard
 */
public abstract class AbstractSurveyElement implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;

    private int surveyPageId;

    private SurveyItemProperties properties;

    /**
     * Default Constructor
     *
     * Required by IsSerializable to exist and be public
     */
    public AbstractSurveyElement() {

    }

    /**
     * Constructor
     *
     * @param id unique id of a survey element
     * @param pageId the unique page id containing the survey element
     * @param properties properties of the survey element. Can't be null.
     */
    public AbstractSurveyElement(int id, int pageId, SurveyItemProperties properties) {

        if (properties == null) {
            throw new IllegalArgumentException("The properties can't be null.");
        }

        this.id = id;
        this.surveyPageId = pageId;
        this.properties = properties;
    }

    /**
     * Gets the ID of the survey element
     *
     * @return int The ID of the survey element
     */
    public int getId() {

        return id;
    }

    /**
     * Set the ID of the survey element
     *
     * @param id The ID of the survey element
     */
    public void setId(int id) {

        this.id = id;
    }

    /**
     * Gets the identifier the UI associates with this element
     * 
     * @return the value of the widget id, can be null
     */
    public String getWidgetId() {
        return (String) properties.getPropertyValue(SurveyPropertyKeyEnum.QUESTION_WIDGET_ID);
    }

    /**
     * Sets the identifier the UI associates with this element
     * 
     * @param widgetId the new value of the widget id
     */
    public void setWidgetId(String widgetId) {
        if(StringUtils.isBlank(widgetId)) {
            getProperties().removeProperty(SurveyPropertyKeyEnum.QUESTION_WIDGET_ID);
        } else {
            getProperties().setPropertyValue(SurveyPropertyKeyEnum.QUESTION_WIDGET_ID, widgetId);
        }
    }

    /**
     * Gets the type of element this is
     *
     * @return SurveyElementTypeEnum The type of element this is
     */
    public abstract SurveyElementTypeEnum getSurveyElementType();

    /**
     * Gets the ID of the survey page this survey element is in
     *
     * @return int The ID of the survey page this survey element is in
     */
    public int getSurveyPageId() {
        return surveyPageId;
    }

    /**
     * Set the ID of the survey page this survey element is on
     * 
     * @param surveyPageId The ID of the survey page this survey element is in
     */
    public void setSurveyPageId(int surveyPageId) {
        this.surveyPageId = surveyPageId;
    }

    /**
     * Gets the properties for the survey element
     *
     * @return SurveyItemProperties The properties for the survey element. Won't
     *         be null.
     */
    public SurveyItemProperties getProperties() {

        return properties;
    }
    
    /**
     * Modifies any media file URLs within this element so that the given host URL is prepended to them.
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
        applySurveyMediaHost(originUri, getProperties());
    }
    
    /**
     * Modifies any media file URLs within the given properties so that the given host URL is prepended to them.
     * This is needed to make sure that media files in a survey can be consistently displayed even when 
     * different hosts may be used to reach them, such as media file located in course folders that need to
     * be reached through the domain content server.
     * <br/><br/>
     * Note: This only applies to media files that use relative file paths (i.e. files uploaded to course 
     * folders). The URLs used to reach legacy media files will not be changed.
     * 
     * @param originUri the URL where media files should be served from. Cannot be null. 
     * @param props the properties containing the media file references that need to be updated. Cannot be null.
     */
    protected void applySurveyMediaHost(String originUri, SurveyItemProperties props) {
        
        if(originUri == null) {
            throw new IllegalArgumentException("The origin URI used to reach survey media files cannot be null");
        }
        
        if(props == null) {
            throw new IllegalArgumentException("The properties used to find media file references cannot be null");
        }
        
        /* Find any survey elements that reference a media file in a workspace folder */
        if(props != null 
                && props.hasProperty(SurveyPropertyKeyEnum.QUESTION_MEDIA_TYPE_KEY)
                && props.hasProperty(SurveyPropertyKeyEnum.QUESTION_IMAGE_KEY)) {
            
            Serializable location = props.getPropertyValue(SurveyPropertyKeyEnum.QUESTION_IMAGE_KEY);
            if(location == null || !(location instanceof String)) {
                return;
            }
            
            String courseRelativeLocation = (String) location;
            String locationUri = originUri + Constants.FORWARD_SLASH + courseRelativeLocation;
            
            props.setPropertyValue(SurveyPropertyKeyEnum.QUESTION_IMAGE_KEY, locationUri);
        }
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[Survey Element: ");
        sb.append("id = ").append(getId());
        sb.append(", widgetId = ").append(getWidgetId());
        sb.append(", pageId = ").append(getSurveyPageId());
        sb.append(", surveyElementType = ").append(getSurveyElementType());
        sb.append(", properties = ").append(getProperties());
        sb.append("]");

        return sb.toString();
    }
}
