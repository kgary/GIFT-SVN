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
 * The SliderLabelPropertySetWidget is reponsible for displaying the controls that
 * allow the author to set the labels for the slider question.
 * 
 * @author nblomberg
 *
 */
public class SliderLabelPropertySetWidget extends AbstractPropertySetWidget  {

    private static Logger logger = Logger.getLogger(SliderLabelPropertySetWidget.class.getName());
    
	private static WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);

	interface WidgetUiBinder extends
			UiBinder<Widget, SliderLabelPropertySetWidget> {
	}

    public SliderLabelPropertySetWidget(SliderLabelPropertySet propertySet, PropertySetListener listener) {
	    super(propertySet, listener);
	    logger.info("constructor()");
	    initWidget(uiBinder.createAndBindUi(this));
	}

}
