/*
* Copyright Dignitas Technologies, LLC
* This file and its contents are governed by one or more distribution and
* copyright statements as described in the LICENSE.txt file distributed with
* this work.
*/            
package mil.arl.gift.tools.dashboard.client.bootstrap.webmonitor.status;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 * A section of the web monitor that contains various UI components used to view and modify
 * the status of GIFT's modules
 * 
 * @author nroberts
 */
public class WebMonitorStatusPanel extends Composite {

    private static WebMonitorStatusPanelUiBinder uiBinder = GWT.create(WebMonitorStatusPanelUiBinder.class);

    interface WebMonitorStatusPanelUiBinder extends UiBinder<Widget, WebMonitorStatusPanel> {
    }

    /**
     * Creates a new panel used to view and modify the status of GIFT's modules
     */
    public WebMonitorStatusPanel() {
        initWidget(uiBinder.createAndBindUi(this));
    }

}
