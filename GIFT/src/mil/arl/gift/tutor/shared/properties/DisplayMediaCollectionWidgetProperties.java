/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.shared.properties;

import java.util.ArrayList;

import generated.course.LessonMaterialList.Assessment;
import mil.arl.gift.common.gwt.shared.MediaHtml;

/**
 * Wrapper for a widget's properties to get/set display media specific properties
 *
 * @author jleonard
 */
public class DisplayMediaCollectionWidgetProperties {

    /* Property names */
    private final static String MEDIA_HTML_LIST_PROPERTY = "MEDIA_HTML_LIST";
    private final static String ASSESSMENT = "ASSESSMENT";
    private final static String OVERDWELLED = "OVERDWELLED";
    private final static String UNDERDWELLED = "UNDERDWELLED";
    
    /**
     * Gets the list of HTML for the widget to display
     * @param properties The properties of the web page with the media
     * @return The list of HTML for the widget to display
     */
    @SuppressWarnings("unchecked")
    public static ArrayList<MediaHtml> getMediaHtmlList(WidgetProperties properties) {
        return (ArrayList<MediaHtml>)properties.getPropertyValue(MEDIA_HTML_LIST_PROPERTY);
    }

    /**
     * Sets the HTML of the widget to display
     * 
     * @param properties The properties of the web page with the media
     * @param mediaHtmlList The list of HTML for the widget to display
     */
    public static void setMediaHtmlList(WidgetProperties properties, ArrayList<MediaHtml> mediaHtmlList) {
        properties.setPropertyValue(MEDIA_HTML_LIST_PROPERTY, mediaHtmlList);
    }
    
    /**
     * Gets the assessment rules to use for the widget to display
     * 
     * @param properties The properties of the web page with the media
     * @return The assessment rules to use
     */
    public static Assessment getAssessment(WidgetProperties properties) {
        
        Object assessment = properties.getPropertyValue(ASSESSMENT);
        
        if(assessment != null && assessment instanceof Assessment){
            return (Assessment) assessment;
            
        } else {
            return null;
        }
    }

    /**
     * Sets the assessment rules to use for the widget to display
     * 
     * @param properties The properties of the web page with the media
     * @param assessment the assessment rules to use
     */
    public static void setAssessment(WidgetProperties properties, Assessment assessment) {
        properties.setPropertyValue(ASSESSMENT, assessment);
    }
    
    /**
     * Gets whether or now the learner spent too much time on this widget
     * 
     * @param properties The properties of the web page with the media
     * @return whether or now the learner spent too much time on this widget
     */
    public static Boolean getOverDwelled(WidgetProperties properties) {
        
        Object overDwell = properties.getPropertyValue(OVERDWELLED);
        
        if(overDwell != null && overDwell instanceof Boolean){
            return (Boolean) overDwell;
            
        } else {
            return false;
        }
    }

    /**
     * Sets whether or now the learner spent too much time on this widget
     * 
     * @param properties The properties of the web page with the media
     * @param overDwelled whether or now the learner spent too much time on this widget
     */
    public static void setOverDwelled(WidgetProperties properties, Boolean overDwelled) {
        properties.setPropertyValue(OVERDWELLED, overDwelled);
    }
    
    /**
     * Gets whether or now the learner spent too little time on this widget
     * 
     * @param properties The properties of the web page with the media
     * @return whether or now the learner spent too little time on this widget
     */
    public static Boolean getUnderDwelled(WidgetProperties properties) {
        
        Object underDwell = properties.getPropertyValue(UNDERDWELLED);
        
        if(underDwell != null && underDwell instanceof Boolean){
            return (Boolean) underDwell;
            
        } else {
            return false;
        }
    }

    /**
     * Sets whether or now the learner spent too little time on this widget
     * 
     * @param properties The properties of the web page with the media
     * @param underDwelled whether or now the learner spent too little time on this widget
     */
    public static void setUnderDwelled(WidgetProperties properties, Boolean underDwelled) {
        properties.setPropertyValue(UNDERDWELLED, underDwelled);
    }
    
}
