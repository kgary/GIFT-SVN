/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.props;

import java.util.logging.Logger;

import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.PropertySetListener;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;

/**
 * The AnswerFieldTextBoxPropertySetWidget class is responsible for displaying the controls
 * that determine if the answer text box is shown as a text box or text area.
 * 
 * @author nblomberg
 *
 */
public class AnswerFieldTextBoxPropertySetWidget extends AbstractPropertySetWidget  {

    private static Logger logger = Logger.getLogger(AnswerFieldTextBoxPropertySetWidget.class.getName());
    
	private static WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);

	interface WidgetUiBinder extends
			UiBinder<Widget, AnswerFieldTextBoxPropertySetWidget> {
	}

	/**
	 * Constructor (default)
	 * 
	 * @param propertySet - The property set for the widget.  
	 * @param listener - The listener that will handle changes to the properties.
	 */
    public AnswerFieldTextBoxPropertySetWidget(AnswerFieldTextBoxPropertySet propertySet, PropertySetListener listener) {
	    super(propertySet, listener);
	    logger.info("constructor()");
	    initWidget(uiBinder.createAndBindUi(this));

	}

}
