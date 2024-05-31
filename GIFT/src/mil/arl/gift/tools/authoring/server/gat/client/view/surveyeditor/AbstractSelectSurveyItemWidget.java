/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor;

import java.util.logging.Logger;

import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.user.client.ui.Composite;

import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.events.SurveyAddNewItemEvent;

/**
 * The abstract class that allows users to select a type of widget to insert into the survey.  Different modes
 * may extend new classes that change which types are allowed.  For example, in dynamic question bank survey types,
 * only scored widgets are allowed to be inserted into the survey.
 * 
 * @author nblomberg
 *
 */
public abstract class AbstractSelectSurveyItemWidget extends Composite  {

    private static Logger logger = Logger.getLogger(AbstractSelectSurveyItemWidget.class.getName());
    
	
    /**
     * The supported types of survey items that can be edited via the Survey Editor.
     * 
     * @author nblomberg
     *
     */
    public enum SurveyItemType {
        INFORMATIVE_TEXT,
        FREE_RESPONSE,
        MULTIPLE_CHOICE,
        MATRIX_OF_CHOICES,
        SLIDER_BAR,
        ESSAY,
        TRUE_FALSE,
        RATING_SCALE,
        COPY_EXISTING_ITEM,
        CLOSE_ITEM
    }
	
    /** The parent survey page that the select survey item widget belongs to.  Each survey page
     * can support only one 'add item' operation at a time.
     */
    protected SurveyPageWidget parentPage = null;
    
    /** The unique widget id for the select survey item widget.  */
    protected SurveyWidgetId widgetId = null;
	
	
    
    // Handler for when a survey item is selected.  This fires a new event to notify the survey page
    // that a new item should be added (or the user cancelled).
    protected MouseDownHandler closeHandler = new MouseDownHandler() {

        @Override
        public void onMouseDown(MouseDownEvent event) {
            logger.info("firing SurveyAddNewItemEvent");           
            
            AbstractSelectSurveyItemWidget.SurveyItemType type = convertSourceToItemType(event.getSource());
            
            logger.info("Survey Item of type " + type + " selected.");
            
            SharedResources.getInstance().getEventBus().fireEvent(new SurveyAddNewItemEvent(parentPage, type));
            
        }
    };
    
	
	

	/**
	 * Constructor (default)
	 */
	public AbstractSelectSurveyItemWidget() {
	    
	    logger.info("constructor()");

	    // Initialize the unique widget id for this element.
        widgetId = new SurveyWidgetId();
	}
	
	    

	/**
	 * Accessor to retrieve the widget id for the select survey item widget.
	 * 
	 * @return SurveyWidgetId - The widget id for the select survey item widget.
	 */
    public SurveyWidgetId getWidgetId() {
        return widgetId;
    }
    
    /** 
     * Sets the parent survey page widget, which should be the page widget that controls
     * the SelectSurveyItemWidget.
     * 
     * @param parent - The parent survey page widget.  This should not be null.
     */
    public void setParentPageWidget(SurveyPageWidget parent) {
        parentPage = parent;
    }
    
    /**
     * Converts the source widget object into the survey item type. 
     * 
     * @param source - The source widget of the selection event (typically this will be a button widget).
     * 
     * @return SurveyItemType - The type of survey item that was selected.
     */
    abstract protected SurveyItemType convertSourceToItemType(Object source);



}
