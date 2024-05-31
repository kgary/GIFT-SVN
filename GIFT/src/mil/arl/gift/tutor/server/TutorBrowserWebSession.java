/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.server;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.WebClientInformation;
import mil.arl.gift.common.gwt.client.RpcResponse;
import mil.arl.gift.common.gwt.server.AsyncReturnBlocker;
import mil.arl.gift.common.gwt.server.BrowserWebSession;
import mil.arl.gift.common.gwt.server.websocket.AbstractServerWebSocket;
import mil.arl.gift.common.gwt.server.websocket.ServerWebSocketHandler;
import mil.arl.gift.tutor.server.websocket.TutorServerWebSocket;
import mil.arl.gift.tutor.server.websocket.TutorWebSocketServlet;
import mil.arl.gift.tutor.shared.AbstractAction;
import mil.arl.gift.tutor.shared.ActionTypeEnum;
import mil.arl.gift.tutor.shared.DeactivateAction;
import mil.arl.gift.tutor.shared.DialogInstance;
import mil.arl.gift.tutor.shared.DialogTypeEnum;
import mil.arl.gift.tutor.shared.DisplayDialogAction;
import mil.arl.gift.tutor.shared.DisplayWidgetAction;
import mil.arl.gift.tutor.shared.EndCourseAction;
import mil.arl.gift.tutor.shared.WidgetInstance;
import mil.arl.gift.tutor.shared.WidgetLocationEnum;
import mil.arl.gift.tutor.shared.WidgetTypeEnum;
import mil.arl.gift.tutor.shared.properties.WidgetProperties;
import mil.arl.gift.tutor.shared.websocket.messages.TutorActionMessage;


/**
 * The TutorBrowserWebSession class is repsonsible for managin connection between the web browser and server.
 * It extends the base class common BrowserWebSession, and adds functionality needed for handling domain sessions
 * and special logic for cleaning up the browser web session that is needed for the Tutor.  It also wraps
 * the logic for sending Tutor Actions as web socket messages.
 *
 * @author jleonard
 */
public class TutorBrowserWebSession extends BrowserWebSession implements ServerWebSocketHandler {

    /**
     * instance of the logger
     */
    private static Logger logger = LoggerFactory.getLogger(TutorBrowserWebSession.class);
        
    /** Delay used to cleanup the browser web session. */
    private static final int CLEANUP_SESSION_DELAY_MS = 10000;
    
    /** Return blocker that waits until a domain session has been created. */
    private AsyncReturnBlocker<?> domainSessionBlocker = null;

    /** Flag to indicate if the browser web session is suspended, which means that it will no longer
     * receive updates of the domain session web state. 
     */
    boolean isSuspended = false;
    
    /**
     * Constructor
     *
     * @param userSessionKey The ID of the user session associated with this browser session
     * @param clientInfo information about the client browser
     * @param socketId the ID of the socket that should be used for this browser session's client
     */
    public TutorBrowserWebSession(String userSessionKey, WebClientInformation clientInfo, String socketId) {
        super(userSessionKey, clientInfo);
        
        
        
        //get the websocket that was created for the client's HTTP session and use it to send actions to the client
        TutorServerWebSocket tutorSocket = TutorWebSocketServlet.getWebSocket(socketId);

        setWebSocket(tutorSocket);

        if(tutorSocket != null){
        	
        	this.setWebSocket(tutorSocket);
        	tutorSocket.setSocketHandler(this);
        	
        } else {
        	throw new IllegalArgumentException("No web socket was found with the provided web socket ID " + socketId);
        }
        
    }

    /**
     * Sets the domain session return blocker.
     * 
     * @param domainSessionBlocker An AsyncReturnBlocker that waits until a domain session
     * has been created to return a value.
     */
    public void setDomainSessionReturnBlocker(AsyncReturnBlocker<?> domainSessionBlocker) {
    	this.domainSessionBlocker = domainSessionBlocker;
    }
    
    /**
     * Notifies the domain session return blocker. 
     */
    public void timeoutDomainSessionReturnBlocker() {
    	if(domainSessionBlocker != null) {
    		domainSessionBlocker.setReturnValue(null);
    	}
    }
    
    /**
     * Boolean to indicate if the browser web session is for an experiment
     * 
     * @return True if the session is being used for an experiment, false otherwise.
     */
    private boolean isExperimentSession() {
        boolean isExperimentSession = false;
        UserWebSession userSession = TutorModule.getInstance().getUserSession(getUserSessionKey());
        if((userSession != null
                && userSession.getUserSessionInfo() != null
                && userSession.getUserSessionInfo().getExperimentId() != null)){
            isExperimentSession = true;
        }
        
        return isExperimentSession;
    }
    
    @Override
    protected synchronized void onSessionStopping() {
        suspendDomainSessionListening();
        
        // For experiments, the session shows the experiment complete widget instead of handling deactivate action.
        if(!isExperimentSession()){
            sendWebSocketMessage(new DeactivateAction("The browser session is stopping"));
       }
        

    }

    @Override
    protected synchronized void onSessionEnding() {
       
    }

    @Override
    protected void onSessionEnded() {
        if (logger.isDebugEnabled()) {
            logger.debug("onSessionEnded()");
        }
    }

    /**
     * Sends a websocket message to the web client.  The action is serialized via the websocket
     * and sent to the client.
     * 
     * @param action The action to send to the client.
     */
    public void sendWebSocketMessage(AbstractAction action) {  	
        
        // Convert the action into a websocket message.
        TutorActionMessage message = new TutorActionMessage(action);
        sendWebSocketMessage(message);
    }
    
    /**
     * Returns if the browser web session is suspended, which means it is paused
     * from listening to domain session updates.  
     * 
     * @return True if the session is suspended, false otherwise.
     */
    public boolean isSuspended() {
        return isSuspended;
    }

    /**
     * Registers the browser session as a listener of the user's active domain
     * session
     *
     * @return boolean If the browser session is now listening to a domain
     * session
     */
    public void resumeDomainSessionListening() {
        isSuspended = false;

    }

    /**
     * Unregisters the browser session as a listener of the user's active domain
     * session
     *
     * @return boolean If the browser session was unregistered from listening to
     * a domain session
     */
    public void suspendDomainSessionListening() {
        
        isSuspended = true;
        
    }


	@Override
    public String toString() {
        
        StringBuffer sb = new StringBuffer();
        sb.append("[TutorBrowserWebSession: ");
        sb.append("userSessionKey = ").append(getUserSessionKey());
        sb.append(", browserSession = ").append(getBrowserSessionKey());
        sb.append(", status = ").append(getSessionStatus());
        sb.append(", clientInfo = ").append(getClientInformation());
        sb.append(", isExperimentSession = ").append(isExperimentSession());

        sb.append("]");
        return sb.toString();      
    }

    @Override 
    public void endSession(){
    	
    	super.endSession();
    }
    


    @Override
    public void onSocketClosed(AbstractServerWebSocket socket) {
        if (logger.isDebugEnabled()) {
            logger.debug("onSocketClosed()");
        }

        // Close out the session (if it hasn't been cleaned up already).  This is now handled
        // by the user web (parent) session since a refresh causes an entirely new browser web session
        // to be created.  This delay allows things such as page refreshes the ability to resume the existing session
        // as in the case of experiments (and simple mode).  
        UserWebSession userSession = TutorModule.getInstance().getUserSession(getUserSessionKey());
        if (userSession != null) {
            userSession.startEndSessionTimer(CLEANUP_SESSION_DELAY_MS);   
        }
    }

    @Override
    public void onSocketOpened(AbstractServerWebSocket socket) {
        
        super.onSocketOpened(socket);
        
        if (logger.isDebugEnabled()) {
            logger.debug("onSocketOpened()");
        }
        
        // Make sure to stop any end session timer that may have been triggered when a new socket is connected.
        UserWebSession userSession = TutorModule.getInstance().getUserSession(getUserSessionKey());
        if (userSession != null) {
            userSession.cancelEndSessionTimeoutTask();
        }
    }
    
    /**
     * Processes an incoming action from the web client.
     *
     * @param action The action received
     */
    public final RpcResponse handleClientAction(AbstractAction action) {
        if (logger.isDebugEnabled()) {
            logger.debug("handleClientAction() called with action: " + action);
        }
        RpcResponse response = null;
        UserWebSession userSession = TutorModule.getInstance().getUserSession(getUserSessionKey());
        if (userSession != null) {
            
            try{
                if(action.getActionType() == ActionTypeEnum.SYNCHRONIZE_CLIENT_STATE
                        && userSession.getDomainWebState() != null) {
                    
                    /*
                     * Nick - There's a small time window between when the BrowserWebSession is constructed on the server
                     * and when the client establishes the web socket connection to it where the client's web socket can
                     * receive actions from the server before the client's BrowserSession is actually ready to handle them.
                     * 
                     * This can cause the client to miss the first few actions sent when the domain session starts if the timing
                     * is absolutely perfect (which can happen if the server takes a while to send back the response for the RPC
                     * request that created the BrowserWebSession, particularly if the server is under stress).
                     * 
                     * Ideally, we should set things up so that the domain session can't start sending messages until the web socket
                     * connection is opened, but that would take a significant rewrite of how we create and initialize 
                     * BrowserWebSessions, so as a quick and dirty solution for now, I'm re-sending the BrowserWebSession's 
                     * current domain state (if applicable) whenever the client session becomes ready to handle it, in case that client 
                     * missed a couple of messages.
                     */
                    userSession.getDomainWebState().resumeBrowserSession(this);
                    
                } else {
                
                    // Pass the message up to the parent user web session to be processed.
                    userSession.handleClientAction(action);
                }
                
                response = new RpcResponse(userSession.getUserSessionKey(), getBrowserSessionKey(), true, "success");

            }catch(Throwable t){
                logger.error("Exception caught in doAction with the message: " + t.getMessage()+".  The action is "+action+" for session "+userSession, t);
                response = new RpcResponse(userSession.getUserSessionKey(), getBrowserSessionKey(), false, "An exception thrown when trying to apply the action on the server.");
                response.setAdditionalInformation("The error reads:\n"+t.getMessage());
            }
            
        } else {
            logger.warn("doAction userSession = null, using null for userSessionId and "+  getBrowserSessionKey() + " for broserSessionId and response = false");
            response = new RpcResponse(null, getBrowserSessionKey(), false, "Not a valid browser session.");
        }
        
        return response;
    }
    
    
    /**
     * Displays a dialog in the browser web page.
     * 
     * @param type Type of dialog to display.
     * @param title Title of the dialog.
     * @param message Message to be presented int he dialog.
     */
    public void displayDialog(DialogTypeEnum type, String title, String message) {
        displayDialog(new DialogInstance(UUID.randomUUID().toString(), type, title, message));
    }
    
    /**
     * Display a dialog in the web page
     *
     * @param instance The instance of a dialog to display
     */
    public void displayDialog(DialogInstance instance) {
        sendWebSocketMessage(new DisplayDialogAction(instance));
    }
    
    /**
     * Resumes the browser web session (resumes it to listening for domain session updates).
     */
    public void resumeBrowserSession() {
        
        UserWebSession userSession = TutorModule.getInstance().getUserSession(getUserSessionKey());
        if(userSession != null) {
            userSession.resumeBrowserWebSession(this);
        }
    }

    @Override
    protected void onSessionStopped() {
        // do nothing.
        
    }

    
    /**
     * Called when the domain session is closed.  This does not necessarily close the browser session
     * since in Simple mode, the user remains in the tutor and goes to the select domain widget.  In the
     * case of simple mode, the browser session remains open.
     * 
     */
    public void onDomainSessionClosed() {
        if (logger.isDebugEnabled()) {
            logger.debug("onDomainSessionClosed() for session: " + this);
        }
       
        
        if(isExperimentSession()){
            
            //if this browser session was running an experiment course, go to the experiment completion page
            WidgetProperties properties = new WidgetProperties();
            properties.setIsFullscreen(true);
            
            WidgetInstance instance = new WidgetInstance(WidgetTypeEnum.EXPERIMENT_COMPLETE_WIDGET, properties);
            
            sendWebSocketMessage(new DisplayWidgetAction(instance, WidgetLocationEnum.ARTICLE));
            
        } else {
            sendWebSocketMessage(new DisplayWidgetAction(new WidgetInstance(WidgetTypeEnum.SELECT_DOMAIN_WIDGET), WidgetLocationEnum.ARTICLE));
        }
        
        
        // This action is only used for 'embedded' mode so the client can respond to when the domain session is ended.
        sendWebSocketMessage(new EndCourseAction(""));
        
    }

    public void processAction(AbstractAction action) {
        
        sendWebSocketMessage(action);
    }
}
