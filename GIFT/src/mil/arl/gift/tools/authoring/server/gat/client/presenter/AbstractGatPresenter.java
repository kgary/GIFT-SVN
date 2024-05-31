/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.presenter;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import net.customware.gwt.dispatch.client.DispatchAsync;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;

import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.WarningDialog;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;

/**
 * The Class AbstractGatPresenter.
 */
public abstract class AbstractGatPresenter {

	/** The dispatch service. */
	@Inject
	protected DispatchAsync dispatchService;
	
	/** The event bus. */
	protected EventBus eventBus = SharedResources.getInstance().getEventBus();	
	
	/** The event registration. */
	protected HandlerRegistration eventRegistration;
	
	/** The handler registrations. */
	protected List<HandlerRegistration> handlerRegistrations = new ArrayList<HandlerRegistration>();
		
	/**
	 * Instantiates a new abstract gat presenter.
	 */
	protected AbstractGatPresenter() {
		getLogger().fine("new instance");
	}
	
	//TODO: this probably belongs in the view
    /**
	 * Show waiting.
	 *
	 * @param waiting the waiting
	 */
	protected void showWaiting(boolean waiting) {
		if(waiting) {
			RootPanel.getBodyElement().getStyle().setProperty("cursor", "wait");
		} else {
			RootPanel.getBodyElement().getStyle().setProperty("cursor", "default");
		}
	}
    
    /**
     * Setup view.
     *
     * @param containerWidget the container widget
     * @param widget the widget
     */
    protected void setupView(final AcceptsOneWidget containerWidget, IsWidget widget) {    	
		if(widget != null) {			
			if(containerWidget != null) {
				containerWidget.setWidget(widget);
			}
		}
    }
        
    
    /**
     * Gets the exception cause message.
     *
     * @param t the t
     * @return the exception cause message
     */
    private String getExceptionCauseMessage(Throwable t) {
        
       while (t.getCause() != null) {
           t = t.getCause();
       }
       
       String msg  = t.getMessage()          != null ? t.getMessage()          : "unknown";
       String lMsg = t.getLocalizedMessage() != null ? t.getLocalizedMessage() : "unknown";
       
       if( !msg.equals(lMsg) ) {
           msg += "(" + lMsg + ")";
       }
       
       return msg;
    }
    
    
    /**
     * Handle callback failure.
     *
     * @param logger the logger
     * @param methodName the method name
     * @param t the t
     */
    protected void handleCallbackFailure(Logger logger, String methodName, Throwable t) {
        
        String entry = "";
        
        entry += "There was a problem communicating with the server while executing '" + methodName + "'.<br>";
        entry += "Reason: " + getExceptionCauseMessage(t) + "<br>";
        entry += "Click 'Close', save any unsaved work (if appropriate), then refresh your browser (F5).<br>";
        entry += "If the problem persists, try restarting the server.<br>";
        
        entry += "<br><div style='padding-top: 10px;'>The GAT logs in output/logger/tools may (or may not) contain more information.</div>";
        
    	WarningDialog.error("Server Request Failed", entry);
    	logger.warning(entry);
    }
    
    /**
     * Log success.
     *
     * @param logger the logger
     * @param msg the msg
     */
    protected void logSuccess(Logger logger, String msg) {
    	logger.info(msg + ": SUCCESS");
    }
    
    /**
     * Handle server side failure.
     *
     * @param logger the logger
     * @param msg the msg
     * @param result the result
     */
    protected void handleServerSideFailure(Logger logger, String msg, GatServiceResult result) {
    	String entry = msg + ": Server error! (reason: '" + result.getErrorMsg() + "')<br><div style='padding-top: 10px;'>Please check the GAT logs in output/logger/tools for more information.</div>";
    	//Window.alert(entry);
    	WarningDialog.error("Server Request Failed", entry);
    	logger.warning(entry);
    }
    
    /**
     * Start.
     */
    protected void start() {
    	getLogger().fine("starting");
    }
    
    /**
     * Notifies this presenter to stop its presenting logic
     */
    public void stopPresenting(){
    	stop();
    }
    
    /**
     * Stop.
     */
    protected void stop() {    	
    	getLogger().fine("stopping");   
    	
    	if(eventRegistration != null) {
    		eventRegistration.removeHandler();
    	}
    	
    	for(HandlerRegistration registration : handlerRegistrations) {
    		if (registration != null) {
    			registration.removeHandler();
    		}
    	}
    }
    
    /**
     * Gets the logger.
     *
     * @return the logger
     */
    protected abstract Logger getLogger(); 

}
