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
 * A panel used to display important events related to module status in the GIFT system, such as launch errors
 * 
 * @author nroberts
 */
public class EventsPanel extends Composite {

    private static EventsPanelUiBinder uiBinder = GWT.create(EventsPanelUiBinder.class);

    interface EventsPanelUiBinder extends UiBinder<Widget, EventsPanel> {
    }

    /**
     * Creates a new panel for displaying events related to module status
     */
    public EventsPanel() {
        initWidget(uiBinder.createAndBindUi(this));
    }

}
