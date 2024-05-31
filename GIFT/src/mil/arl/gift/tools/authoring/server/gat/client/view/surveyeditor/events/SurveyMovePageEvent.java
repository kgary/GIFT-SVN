/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.events;

import com.google.web.bindery.event.shared.binder.GenericEvent;

import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.SurveyWidgetId;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.SurveyPageWidget.MoveOrderEnum;

/**
 * Event that is called to signal that a page should be moved in the list of pages.
 * The page id signals which page should be moved.
 * The MoveOrderEnum controls the direction that the page should be moved (up or down).
 * 
 * @author nblomberg
 *
 */
public class SurveyMovePageEvent extends GenericEvent {

    /** The order that the page should be moved in the list. */
	private MoveOrderEnum pageOrder;
	
	/** The pageId that should be moved. */
	private SurveyWidgetId widgetId = null;
	
	/**
	 * Constructor
	 * 
	 * @param id - The pageId to be moved.
	 * @param order - The order the page should be moved (up or down).
	 */
	public SurveyMovePageEvent(SurveyWidgetId id, MoveOrderEnum order) {
		setWidgetId(id);
		setPageOrder(order);
	}

	/**
	 * Accessor to get the page order.
	 * 
	 * @return PageOrderEnum - The order the page should be moved (up or down) in the list.
	 */
    public MoveOrderEnum getPageOrder() {
        return pageOrder;
    }

    /**
     * Accessor to set the order the page should be moved in the list (up or down).
     * @param pageOrder - The order the page should be moved in the list (up or down).
     */
    public void setPageOrder(MoveOrderEnum pageOrder) {
        this.pageOrder = pageOrder;
    }

    /**
     * Accessor to get the page widget id that should be moved.
     * 
     * @return SurveyWidgetId - The id of the page widget that should be moved.
     */
    public SurveyWidgetId getWidgetId() {
        return widgetId;
    }

    /**
     * Accessor to set the page widget id that should be moved.
     * 
     * @param wId - the page widget id that should be moved.
     */
    public void setWidgetId(SurveyWidgetId wId) {
        this.widgetId = wId;
    }


}
