/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.ert.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point class for GWT on the client side
 */
public class EventReportTool implements EntryPoint {

    @Override
    public void onModuleLoad() {
        RootPanel.get("rootContainer").clear();
        RootPanel.get("rootContainer").add(new EventReportCreatorWidget());
    }
}