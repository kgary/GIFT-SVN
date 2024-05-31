/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.events;

import java.util.List;

import com.google.web.bindery.event.shared.binder.GenericEvent;

import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.SurveyWidgetId;

/**
 * Event fired when the associated concepts value is changed for a question in a question bank survey.
 * It is used by the Question Container Widget to notify the Survey Editor Panel to update it's backing data model.
 * 
 * @author cpadilla
 *
 */
public class AssociatedConceptsChangedEvent extends GenericEvent {
    
    /** The list of concepts that are selected when the associated concepts value was changed */
    private List<String> concepts;

    /** The widget id of the survey whose question's associated concepts were changed */
    private SurveyWidgetId widgetId;
    
    /**
     * Constructor
     * 
     * @param widgetId - the widget id of the survey whose question's associated concepts value changed
     * @param concepts - the list of concepts that are selected when the associated concepts value was changed
     */
    public AssociatedConceptsChangedEvent(SurveyWidgetId widgetId, List<String> concepts) {
        
        if(widgetId == null) {
            throw new IllegalArgumentException("The value of widgetId can't be null");
        }
        
        if(concepts == null) {
            throw new IllegalArgumentException("The value of concepts can't be null");
        }
        
        this.widgetId = widgetId;
        this.concepts = concepts;
    }
    
    /**
     * Gets the list of concepts that were changed
     * 
     * @return The list of concepts that were changed
     */
    public List<String> getConcepts() {
        return concepts;
    }
    
    /**
     * Gets the survey widget whose question's associated concepts value changed
     * 
     * @return the survey widget id
     */
    public SurveyWidgetId getWidgetId() {
        return widgetId;
    }
    
    @Override
    public String toString() {
        return new StringBuilder()
                .append("[AssociatedConceptsChangedEvent: widgetId = ")
                .append(widgetId)
                .append(", concepts = ")
                .append(concepts)
                .append("]")
                .toString();
    }
}