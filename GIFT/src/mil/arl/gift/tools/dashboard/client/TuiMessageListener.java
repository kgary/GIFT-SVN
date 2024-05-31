/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client;



import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.constants.IconType;

import com.google.gwt.user.client.History;

import mil.arl.gift.common.gwt.client.IFrameMessageHandlerChild;
import mil.arl.gift.common.gwt.client.IFrameMessageListener;
import mil.arl.gift.common.gwt.client.iframe.IFrameMessageType;
import mil.arl.gift.common.gwt.client.iframe.messages.AbstractIFrameMessage;
import mil.arl.gift.common.gwt.client.iframe.messages.ControlApplicationMessage;
import mil.arl.gift.common.gwt.client.iframe.messages.DisplayDialogMessage;
import mil.arl.gift.common.gwt.client.iframe.messages.DisplayNotificationMessage;
import mil.arl.gift.common.gwt.client.iframe.messages.EndCourseMessage;
import mil.arl.gift.common.gwt.client.iframe.messages.HistoryItemMessage;
import mil.arl.gift.common.gwt.client.iframe.messages.IFrameSimpleMessage;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ModalDialogCallback;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.dashboard.shared.ScreenEnum;


/**
 * The TUI MessageListener handles messages received from the TUI when using cross-domain communication via
 * iframes.  
 * 
 * @author nblomberg
 *
 */
public class TuiMessageListener implements IFrameMessageListener {
   
    /** logger which can be used to log to the browser console window */
    private static Logger logger = Logger.getLogger(TuiMessageListener.class.getName());

    /**
     * Constructor 
     */
    public TuiMessageListener() {
        
    }


    @Override
    public boolean handleMessage(AbstractIFrameMessage msg) {
        
        logger.fine("receiving message: " + msg.getMsgType());
        
        if (msg instanceof IFrameSimpleMessage) {
            
            IFrameSimpleMessage simpleMsg = (IFrameSimpleMessage)msg;
            if (simpleMsg.getMsgType() == IFrameMessageType.COURSE_READY) {
                
                // TODO - Showing the tui here is better, but there is an issue with the popup
                // on CourseHeader.java where the popup is getting cutoff.
                // If that issue gets resolved, then we can show the tui in the ready state here
                // to prevent the 'tui frame' from briefly being shown before it's ready.
                // UiManager.getInstance().showEmbeddedTui(true);
            } else if (simpleMsg.getMsgType() == IFrameMessageType.COURSE_STARTING) {
                // Show the tui here (see todo comment above, this could be moved to the COURSE_READY state instead).
                UiManager.getInstance().showEmbeddedTui(true);
            } else if (simpleMsg.getMsgType() == IFrameMessageType.TUI_READY) {
                logger.info("setting tui ready.");
                UiManager.getInstance().setTuiReady(true);
            }else {
                logger.warning("Unhandled IFrameSimpleMessage of type " + msg.getMsgType());
            }
        } else if (msg instanceof EndCourseMessage) {
            
            EndCourseMessage endCourseMsg = (EndCourseMessage)msg;
            
            String domainId = endCourseMsg.getDomainId();

            // Fire off a request to clean up the course runtime folder by course id.
            logger.fine("End course message received with domain id: " + domainId);

            //either the TUI is still in the iframe of the dashboard or this message is the tutor
            //replying to the request to end the course and the dashboard is currently waiting for this notification.
            if (UiManager.getInstance().isEmbeddedTuiShowing() || UiManager.getInstance().isEndCourseTimerRunning()) {
                UiManager.getInstance().onCourseEnded();
            } else {
                // this may be okay if an existing session was found (ie the user is opening a session from another web browser).
                logger.warning("End course message received while the tui iframe is not being shown.  This can be okay if another session was found when starting a course.");
            }            

        } else if (msg instanceof DisplayDialogMessage) {
            DisplayDialogMessage dialogMsg = (DisplayDialogMessage)msg;
            
            // These come from tui DialogTypeEnum, we translate them to the dashboard dialog type here.
            // Note that we ignore 'loading' dialog types from the tui.
            if (dialogMsg.getDialogType().compareTo("ERROR_DIALOG") == 0 ||
                dialogMsg.getDialogType().compareTo("FATAL_ERROR_DIALOG") == 0) {
                
                //need to cancel BsCourseRuntimeWidget tutorUrlTimer logic so it doesn't
                //auto hide the error dialog about to be shown when the timer fires
                UiManager.getInstance().cancelTutorIFrameTimer();
                
                ModalDialogCallback callback = new ModalDialogCallback() {

                    @Override
                    public void onClose() {
                        UiManager.getInstance().displayScreen(ScreenEnum.MYCOURSES);
                    }
                    
                };
                
                UiManager.getInstance().displayDetailedErrorDialog(dialogMsg.getDialogTitle(), dialogMsg.getDialogMessage(), 
                        dialogMsg.getDialogDetails(), null, UiManager.getInstance().getCurrentCourseName(), callback);
                
            } else if (dialogMsg.getDialogType().compareTo("INFO_DIALOG") == 0) {
                
                UiManager.getInstance().displayInfoDialog(dialogMsg.getDialogTitle(), dialogMsg.getDialogMessage());
            } else {
                logger.fine("Unsupported dialog type received from the tui.  Dashboard dialog will not be displayed. Received type: " + dialogMsg.getDialogType());
            }
                
        } else if (msg instanceof ControlApplicationMessage) {
            
            if(logger.isLoggable(Level.INFO)) {
                logger.info("Passing app control message to parent frame : " + msg.toString());
            }
            
            //if the TUI is attempting to control a mobile application, pass the message on to the dashboard's parent (i.e. the mobile app)
            IFrameMessageHandlerChild.getInstance().sendMessage(msg);
            
        } else if (msg instanceof DisplayNotificationMessage) {   

            DisplayNotificationMessage notifyMsg = (DisplayNotificationMessage) msg;

            // Convert the icon css into a supported bootstrap icon type.
            IconType iconType = null;
            if (notifyMsg.getIconType() != null && !notifyMsg.getIconType().isEmpty()) {
                iconType = IconType.fromStyleName(notifyMsg.getIconType());
            }
            
            // Display the notification in the dashboard.
            UiManager.getInstance().displayNotifyMessage(notifyMsg.getTitle(), notifyMsg.getMessage(), iconType);
            
        } else if (msg instanceof HistoryItemMessage) {
            HistoryItemMessage historyMsg = (HistoryItemMessage) msg;
            final String newToken = historyMsg.getHistoryItem();
            if (!StringUtils.equalsIgnoreCase(newToken, History.getToken())) {
                HistoryManager.getInstance().replaceHistory(newToken);
            }
        } else {
            logger.warning("Unhandled message of type: " + msg.getMsgType());
        }
        
        return false;
    }
    
    
   
}
