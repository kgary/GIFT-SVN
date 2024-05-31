/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap;

import java.util.logging.Logger;

import mil.arl.gift.common.gwt.client.JsniUtility;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.AbstractBsWidget;
import mil.arl.gift.common.gwt.shared.ServerProperties;
import mil.arl.gift.tools.dashboard.client.BrowserSession;
import mil.arl.gift.tools.dashboard.client.Dashboard;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;


/**
 * The LtiConsumerEndWidget is a page that is as an ending page for a user that
 * has taken the GIFT course from an LTI Tool Consumer.  The page can be used to
 * provide last guidance for the user to proceed back to the LTI Tool Consumer's course.
 * 
 * @author nblomberg
 *
 */
public class LtiConsumerEndWidget extends AbstractBsWidget {
    
    private static Logger logger = Logger.getLogger(LtiConsumerEndWidget.class.getName());

    private static LtiConsumerCompleteUiBinder uiBinder = GWT.create(LtiConsumerCompleteUiBinder.class);

    
    interface LtiConsumerCompleteUiBinder extends UiBinder<Widget, LtiConsumerEndWidget> {
    }
    
    @UiField
    protected HTML closeMessage;
    
    @UiField
    FlowPanel mainPanel;
    
    /** the image containing the configurable logo */
    @UiField
    protected Image logoImage;

    /**
     * Constructor
     */
    public LtiConsumerEndWidget(Object initParams) {
        logger.info("LtiConsumerEndWidget() called.");
        initWidget(uiBinder.createAndBindUi(this));
        
        // set the background
        String backgroundImage = Dashboard.getInstance().getServerProperties().getPropertyValue(ServerProperties.BACKGROUND_IMAGE);
        mainPanel.getElement().getStyle().setBackgroundImage("url('"+backgroundImage+"')");
        
        closeMessage.setVisible(JsniUtility.isTopmostFrame());
        
        String logoUrl = Dashboard.getInstance().getServerProperties().getPropertyValue(ServerProperties.LOGO);
        logoImage.setUrl(logoUrl);
        
        // Close any existing websocket.
        if (BrowserSession.getInstance() != null) {
            BrowserSession.getInstance().closeWebSocket();
        }
    }

}
