/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.client.widgets;

import java.util.logging.Logger;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.DisplayMessageTutorRequest;
import mil.arl.gift.common.gwt.client.RpcResponse;
import mil.arl.gift.tutor.client.BrowserSession;
import mil.arl.gift.tutor.client.coursewidgets.ContentWidget;
import mil.arl.gift.tutor.client.coursewidgets.CourseHeaderWidget;
import mil.arl.gift.tutor.shared.CloseAction;
import mil.arl.gift.tutor.shared.WidgetInstance;
import mil.arl.gift.tutor.shared.properties.DisplayMessageWidgetProperties;
import mil.arl.gift.tutor.shared.properties.WidgetProperties;

/**
 * A widget that displays an html message to the user
 *
 * @author bzahid
 */
public class DisplayMessageWidget extends Composite {

	private static Logger logger = Logger.getLogger(DisplayMessageWidget.class.getName());

	private static DisplayMessageWidgetUiBinder uiBinder = GWT.create(DisplayMessageWidgetUiBinder.class);
	
    @UiField
    protected ContentWidget contentWidget;
    
    interface DisplayMessageWidgetUiBinder extends UiBinder<Widget, DisplayMessageWidget> {
    }

    /**
     * Constructor
     *
     * @param instance The instance of the display text widget
     */
    public DisplayMessageWidget(WidgetInstance instance) {
    	initWidget(uiBinder.createAndBindUi(this));
        init(instance);        
    }
    
    private void init(WidgetInstance instance) {

    	WidgetProperties properties = instance.getWidgetProperties();
    	
        if (properties != null) {
            DisplayMessageTutorRequest parameters = DisplayMessageWidgetProperties.getParameters(properties);
            
            if(DisplayMessageWidgetProperties.isEmptyRequest(properties)) {
            	
            	// Notify the server that the TUI was cleared
            	BrowserSession.getInstance().sendActionToServer(new CloseAction(instance.getWidgetId()), new AsyncCallback<RpcResponse>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        logger.warning("Request to close the guidance widget failed on the server: " + caught.getMessage());
                    }

                    @Override
                    public void onSuccess(RpcResponse result) {
                       // Nothing to do
                    }
                });
            	
            } else if (parameters.getMessage() != null) {
                contentWidget.setMessageHTML(parameters.getMessage());
                
            } else {
               logger.warning("No message to display");
            }
                        
            if (DisplayMessageWidgetProperties.getHasContinueButton(properties)) {
            	
            	if(parameters.getTitle() != null) {
            		CourseHeaderWidget.getInstance().setHeaderTitle(parameters.getTitle());
            	}
            	
            	CourseHeaderWidget.getInstance().setContinuePageId(instance.getWidgetId());
            }
        }
    }
}
