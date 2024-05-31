/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.aar;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 *  The panel containing the legend dialog that show what the colors and symbols on the legend represent.
 * 
 * @author kquiroga
 */
public class TimelineLegendDialog extends PopupPanel {

    private static TimelineHelpDialogUiBinder uiBinder = GWT.create(TimelineHelpDialogUiBinder.class);
    
    /** Interface to allow UiBinder timeline legend dialog access for widget*/
    interface TimelineHelpDialogUiBinder extends UiBinder<Widget, TimelineLegendDialog> {
    }
    
    /** Set timeline legend dialog to be hidden and binded with the dialog*/
    public TimelineLegendDialog() {
        
        setWidget(uiBinder.createAndBindUi(this));
        setAutoHideEnabled(true);
}
    
}
