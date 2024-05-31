/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.props;

import java.util.logging.Logger;

import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.AbstractPropertySet;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.PropertySetListener;

import com.google.gwt.user.client.ui.Composite;

/**
 * The AbstractPropertySetWidget is the base class for all other property set widgets.
 * It encapsulates functionality such as storing the property set and the listener for the widget.
 * 
 * @author nblomberg
 *
 */
public abstract class AbstractPropertySetWidget extends Composite  {

    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(AbstractPropertySetWidget.class.getName());

	/** The property set for the widget. */
	protected AbstractPropertySet propSet = null;
	
	/** The listener that should be called to notify of property changes. */
	protected PropertySetListener propListener = null;

	/**
	 * Constructor (default)
	 * 
	 * @param propertySet - The property set that will be used for the widget.
	 * @param listener - The listener that will handle changes to the property set. 
	 */
    public AbstractPropertySetWidget(AbstractPropertySet propertySet, PropertySetListener listener) {
	    
        propSet = propertySet;
        propListener = listener;
	}
    
    
    /**
     * Notifies the property set listener that the property set has been changed.
     */
    public void notifyPropertySetChanged() {
        propListener.onPropertySetChange(propSet);
    }

}
