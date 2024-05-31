/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets.bootstrap;

import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.Label;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;

/**
 * The page load error widget is a simple widget that can be used in the event there is a critical error loading a page.
 * In this case, an icon is displayed with a title and description of the page load error.
 * 
 * @author nblomberg
 *
 */
public class BsPageLoadErrorWidget extends AbstractBsWidget {

    private static BsPageLoadErrorWidgetUiBinder uiBinder = GWT.create(BsPageLoadErrorWidgetUiBinder.class);
    
    interface BsPageLoadErrorWidgetUiBinder extends UiBinder<Widget, BsPageLoadErrorWidget> { }

    @UiField
    Heading ctrlTitle;
   
    @UiField
    Label ctrlMessage;


    /**
     * Constructor - default
     */
    public BsPageLoadErrorWidget() {
    	 
        initWidget(uiBinder.createAndBindUi(this));
        
    }
    
    /**
     * Constructor - Allows the user to assign the title and message of the page load error screen.
     * 
     * @param title - Title of the error that will be displayed to the user.
     * @param message - The message (description) of the error that will be displayed to the user.  Supports html text.
     */
    public BsPageLoadErrorWidget(String title, String message) {
        this();
        
        ctrlTitle.setText(title); 
        ctrlMessage.setHTML(message);
    }
    
}
