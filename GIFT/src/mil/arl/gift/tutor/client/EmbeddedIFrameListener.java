/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.client;



import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconType;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;

import mil.arl.gift.common.gwt.client.IFrameMessageListener;
import mil.arl.gift.common.gwt.client.RpcResponse;
import mil.arl.gift.common.gwt.client.iframe.IFrameMessageType;
import mil.arl.gift.common.gwt.client.iframe.messages.AbstractIFrameMessage;
import mil.arl.gift.common.gwt.client.iframe.messages.ApplicationEventMessage;
import mil.arl.gift.common.gwt.client.iframe.messages.IFrameSimpleMessage;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ModalDialogBox;
import mil.arl.gift.tutor.client.widgets.UserActionAvatarContainer;



/**
 * The EmbeddedIFrameListener is repsonsible for handling iframe (cross-domain) messages
 * received from the Tui to the dashboard.  This listener can respond to any messages that
 * are received and handle them as needed.  It needs to be registered to the IFrameMessagHandlerChild class
 * during the gwt onModuleLoad().  See TutorUserWebInterface.java for where this is currently registered.
 * 
 * @author nblomberg
 *
 */
public class EmbeddedIFrameListener implements IFrameMessageListener {
   
    /** logger which can be used to log to the browser console window */
    private static Logger logger = Logger.getLogger(EmbeddedIFrameListener.class.getName());

    /** A timer used to track how long it takes for GIFT to receive geolocation data from the mobile app */
    private static Timer mobileAppGeolocationTimeout = null;
    
    /** A dialog to display when the mobile app takes too long to send geolocation data to GIFT */
    private static ModalDialogBox geolocationTimeoutDialog = null;


    /**
     * Constructor 
     */
    public EmbeddedIFrameListener() {
        
    }


    @Override
    public boolean handleMessage(AbstractIFrameMessage msg) {
        
        logger.fine("receiving message: " + msg.getMsgType());
        
        if (msg instanceof IFrameSimpleMessage) {
            
            IFrameSimpleMessage simpleMsg = (IFrameSimpleMessage)msg;
            if (simpleMsg.getMsgType() == IFrameMessageType.STOP_COURSE) {

                
                // End the user's domain session and log them out.
                if (BrowserSession.getInstance() != null) {
                    final AsyncCallback<RpcResponse> logoutCallback = new AsyncCallback<RpcResponse>() {
                        @Override
                        public void onFailure(Throwable t) {
                            logger.severe("Throwable caught logging out user: " + t.toString());
                        }

                        @Override
                        public void onSuccess(RpcResponse result) {
                            if (!result.isSuccess()) {
                                logger.severe("Error logging out user");
                            } else {
                                logger.fine("User logged out successfully.");
                            }
                        }
                    };
                    
                    BrowserSession.getInstance().userEndDomainSessionAndLogout(logoutCallback);
                }
                
                
                
            } else {
                logger.warning("Unhandled IFrameSimpleMessage of type " + msg.getMsgType());
            }
             
        } else if(msg instanceof ApplicationEventMessage){
            
            ApplicationEventMessage event = (ApplicationEventMessage) msg;
            
            //Need to wrap the raw message into a new string created by GWT Java code, since strings created
            //from native JavaScript can't be sent over RPC communications. This is because the strings created
            //by JavaScript aren't actually proper Java strings; they just get cast as Strings by GWT and, as such,
            //only support the subset of the String API needed for client-side code. This prevents them from being
            //serialized properly for RPCs, since they won't translate to proper Java strings on the server end. 
            //Using GWT Java code to create a string avoids this problem by creating a proper Java string that 
            //can be sent over RPCs just fine.
            String newMessage = new StringBuilder(event.getEventMessage()).toString();
                
            if(mobileAppGeolocationTimeout == null && isSimanStartResponseMessage(newMessage)) {
                
                //create a timer to track how long it takes GIFT to receive geolocation data after the app should start sending it
                mobileAppGeolocationTimeout = new Timer() {
                    
                    @Override
                    public void run() {
                        
                        if(geolocationTimeoutDialog == null) {
                            
                            geolocationTimeoutDialog = new ModalDialogBox();
                            geolocationTimeoutDialog.setGlassEnabled(true);
                            geolocationTimeoutDialog.setText("Location timeout");
                            geolocationTimeoutDialog.setWidget(new HTML(
                                    "The app hasn't received your GPS location for several seconds. This may indicate that "
                                    + "this app doesn't have the appropriate location services permission.<br/><br/>"
                                    + "Please open the settings app, make sure that location services are enabled "
                                    + "and that this app has permission to use location services. You can also uninstall then "
                                    + "install the app again to be prompted for giving permissions for location services."));
                            
                            final Button closeButton = new Button("Close");
                            closeButton.setType(ButtonType.DANGER);
                            closeButton.addClickHandler(new ClickHandler() {
                                
                                @Override
                                public void onClick(ClickEvent event) {
                                    
                                    closeButton.setEnabled(false);
                                    closeButton.setIcon(IconType.SPINNER);
                                    closeButton.setIconSpin(true);
                                    
                                    //clear out the avatar container's web application to avoid showing unnecessary dialogs and handling
                                    //geolocations while ending the course
                                    UserActionAvatarContainer.getInstance().unloadWebApplication();
                                    
                                    //end the course so that the user does not continue in a bad state
                                    BrowserSession.getInstance().endDomainSession();
                                }
                            });
                            
                            geolocationTimeoutDialog.setFooterWidget(closeButton);
                        }
                        
                        geolocationTimeoutDialog.center();
                    }
                };
                
                //wait 15 seconds for GIFT to receive geolocation data from the mobile app
                mobileAppGeolocationTimeout.schedule(15000);
            
            } else if(mobileAppGeolocationTimeout != null 
                    && mobileAppGeolocationTimeout.isRunning() 
                    && isGeolocationMessage(newMessage)) {
                
                //geolocation data was received before the timeout finished, so cancel it
                mobileAppGeolocationTimeout.cancel();
                
            } else if(geolocationTimeoutDialog != null && isGeolocationMessage(newMessage)) {
                
                //geolocation data was received while showing the timeout message, so hide the message since it is no longer needed
                geolocationTimeoutDialog.hide();
                geolocationTimeoutDialog = null;
            }
            
            //notify the server that a an event was received from a wrapper application
            BrowserSession.getInstance().sendEmbeddedAppState(newMessage);
            
        } else {
            logger.warning("Unhandled message of type: " + msg.getMsgType());
        }
        
        return false;
    }
    
    /**
     * Determines if a JSON string is a response to a Siman Start message
     * @param message the message, as JSON, to be tested
     * @return true if the message is a response to a Siman Start message, false otherwise
     */
    private native boolean isSimanStartResponseMessage(String message) /*-{
        try {
            var msg = JSON.parse(message);
            
            //Check if message is a SimanResponse
            if(msg.type === "SimanResponse" && msg.payload === "Start") {
                return true;
            } else {
                return false;
            }
        } catch(err) {
            return false;
        }
    }-*/;
    
    /**
     * Determines if a JSON string is a Geolocation message
     * @param message the message, as JSON, to be tested
     * @return true if the message is a Geolocation message, false otherwise
     */
    private native boolean isGeolocationMessage(String message) /*-{
        try {
            var msg = JSON.parse(message);
            
            //Check if message is a SimanResponse
            if(msg.type === "Geolocation") {
                return true;
            } else {
                return false;
            }
        } catch(err) {
            return false;
        }
    }-*/;
   
}
