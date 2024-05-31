/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.events;

import com.google.web.bindery.event.shared.binder.GenericEvent;

import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.SurveyPageWidget;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.AbstractSelectSurveyItemWidget.SurveyItemType;

/**
 * Event sent when a new survey item of a specific type should be added to a survey.
 * 
 * @author nblomberg
 *
 */
public class SurveyAddNewItemEvent extends GenericEvent {

	
    /** The page widget that is requesting a new survey item to be added. */
    private SurveyPageWidget parentPage = null;
    
    /** The type of item that should be added to the page. */
    private SurveyItemType itemType = SurveyItemType.CLOSE_ITEM;
    
    /** 
     * 
     * Constructor (default)
     * 
     */
	public SurveyAddNewItemEvent(SurveyPageWidget pageWidget, SurveyItemType type) {
		setParentPage(pageWidget);
		
		setItemType(type);
	}

	/**
	 * Accessor to get the parent page widget
	 * 
	 * @return SurveyPageWidget - The page that the survey item will be added to.  Null can be returned.
	 */
    public SurveyPageWidget getParentPage() {
        return parentPage;
    }

    /**
     * Accessor to set the parent page widget. 
     * 
     * @param parentPage - The page that the survey item will be added to.
     */
    public void setParentPage(SurveyPageWidget parentPage) {
        this.parentPage = parentPage;
    }

    /**
     * Accessor to set the type of survey item to add to the page.
     * 
     * @return SurveyItemType - The type of item to add to the survey page.
     */
    public SurveyItemType getItemType() {
        return itemType;
    }

    /**
     * Accessor to set the type of survey item to add to the page.
     * 
     * @param itemType - The tyep of item to add to the survey page. 
     *      
     */
    public void setItemType(SurveyItemType itemType) {
        this.itemType = itemType;
    }

}
