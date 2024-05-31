/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.webmonitor.util;

import java.util.logging.Level;
import java.util.logging.Logger;

import mil.arl.gift.common.gwt.client.util.AbstractProvider;
import mil.arl.gift.tools.dashboard.client.bootstrap.webmonitor.util.WebMonitorStatusProvider.WebMonitorStatusChangeHandler;
import mil.arl.gift.tools.dashboard.shared.rpcs.WebMonitorStatus;

/**
 * A provider that provides information about the status of the web monitor to any registered handlers
 * 
 * @author nroberts
 */
public class WebMonitorStatusProvider extends AbstractProvider<WebMonitorStatusChangeHandler> {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(WebMonitorStatusProvider.class.getName());

    /** The instance of this class */
    private static WebMonitorStatusProvider instance = null;
    
    /** The latest status information that was recieved for the web monitor service */
    private WebMonitorStatus latestStatus;

    /**
     * Singleton constructor
     */
    private WebMonitorStatusProvider() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(".ctor()");
        }
    }

    /**
     * Accessor to the provider singleton object. If it doesn't exist yet it
     * will be created.
     * 
     * @return the instance to the provider singleton object.
     */
    public static WebMonitorStatusProvider getInstance() {
        if (instance == null) {
            instance = new WebMonitorStatusProvider();
        }

        return instance;
    }

    /**
     * Updates the web monitor status stored by this provider and sends and notifies all
     * registered handlers that said status has changed
     * 
     * @param moduleTypeToQueues the new module statuses. Cannot be null.
     */
    public void setMonitorStatus(final WebMonitorStatus monitorStatus) {
        boolean changed = latestStatus == null || !latestStatus.equals(monitorStatus);
        this.latestStatus = monitorStatus;

        if (changed) {
            /* Notify handlers */
            executeHandlers(new SafeHandlerExecution<WebMonitorStatusChangeHandler>() {
                @Override
                public void execute(WebMonitorStatusChangeHandler handler) {
                    handler.onMonitorStatusChanged(WebMonitorStatusProvider.this.getMonitorStatus());
                }
            });
        }
    }
    
    /**
     * Gets the web monitor status stored by this provider, i.e. static information about the 
     * web monitor that doesn't change much over its lifetime.
     * 
     * @return the current module statuses. Can be null if no status has been received yet.
     */
    public WebMonitorStatus getMonitorStatus(){
        return latestStatus;
    }

    /**
     * A handler used to handle web monitor status updates
     * 
     * @author nroberts
     */
    public interface WebMonitorStatusChangeHandler {
        
        /**
         * Notifies this listener that the web monitor status has changed
         * 
         * @param status the new web monitor status. Cannot be null.
         */
        void onMonitorStatusChanged(WebMonitorStatus status);
    }
}
