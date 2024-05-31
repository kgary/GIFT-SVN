/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.client.widgets;

import mil.arl.gift.tutor.shared.WidgetInstance;
import mil.arl.gift.tutor.shared.WidgetTypeEnum;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * An interface extending IsWidget used for widgets that possess logic for updating their internal data.
 * 
 * @author nroberts
 */
public interface IsUpdateableWidget extends IsWidget{

	/**
	 * Signals for this updateable widget to update its internal data based on the properties in the widget instance provided.
	 * 
	 * @param widgetInstance the widget instance containing the updated properties
	 */
	public void update(WidgetInstance widgetInstance);
	
	/**
	 * Gets the widget type
	 * 
	 * @return The widget type
	 */
	public WidgetTypeEnum getWidgetType();
}
