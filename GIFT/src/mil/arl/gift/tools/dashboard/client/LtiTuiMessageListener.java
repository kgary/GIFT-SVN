/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client;



import java.util.logging.Logger;

import mil.arl.gift.common.gwt.client.iframe.messages.AbstractIFrameMessage;
import mil.arl.gift.common.gwt.client.iframe.messages.DisplayDialogMessage;
import mil.arl.gift.tools.dashboard.client.bootstrap.DashboardErrorWidget;
import mil.arl.gift.tools.dashboard.shared.ScreenEnum;

/**
 * The LtiTuiMessageListener class extends the TuiMessageListener.
 * It primarily overrides the handling of the tui messages such that the user can be
 * redirected to the proper lti screen when errors are received from the tui.
 * 
 * @author nblomberg
 *
 */
public class LtiTuiMessageListener extends TuiMessageListener {
   
    /** logger which can be used to log to the browser console window */
    private static Logger logger = Logger.getLogger(LtiTuiMessageListener.class.getName());

    /**
     * Constructor 
     */
    public LtiTuiMessageListener() {
        super();
    }


    @Override
    public boolean handleMessage(AbstractIFrameMessage msg) {
        
        
        boolean handled = false;
        logger.fine("receiving message: " + msg.getMsgType());

        if (msg instanceof DisplayDialogMessage) {
            DisplayDialogMessage dialogMsg = (DisplayDialogMessage)msg;
            
            // These come from tui DialogTypeEnum, we translate them to the dashboard dialog type here.
            // Note that we ignore 'loading' dialog types from the tui.
            if (dialogMsg.getDialogType().compareTo("ERROR_DIALOG") == 0 ||
                dialogMsg.getDialogType().compareTo("FATAL_ERROR_DIALOG") == 0) {
                
                //need to cancel BsCourseRuntimeWidget tutorUrlTimer logic so it doesn't
                //auto hide the error dialog about to be shown when the timer fires
                UiManager.getInstance().cancelTutorIFrameTimer();
                
                UiManager.getInstance().displayScreen(ScreenEnum.DASHBOARD_ERROR_PAGE, new DashboardErrorWidget.ErrorMessage(
                        dialogMsg.getDialogTitle(), dialogMsg.getDialogMessage(), null
                ));
                
            } else if (dialogMsg.getDialogType().compareTo("INFO_DIALOG") == 0) {
                
                UiManager.getInstance().displayInfoDialog(dialogMsg.getDialogTitle(), dialogMsg.getDialogMessage());
            } else {
                logger.fine("Unsupported dialog type received from the tui.  Dashboard dialog will not be displayed. Received type: " + dialogMsg.getDialogType());
            }
                
            handled = true;
            
        } else {
            handled = super.handleMessage(msg);
        }
        
        return handled;
    }
    
    
   
}
