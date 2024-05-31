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
import mil.arl.gift.tools.dashboard.client.bootstrap.webmonitor.util.ModuleMessageProvider.ModuleMessageHandler;
import mil.arl.gift.tools.dashboard.shared.messages.monitor.AbstractMessageUpdate;
import mil.arl.gift.tools.dashboard.shared.messages.monitor.MessageFilterChangedUpdate;
import mil.arl.gift.tools.dashboard.shared.messages.monitor.MessageFilterChoicesUpdate;
import mil.arl.gift.tools.dashboard.shared.messages.monitor.MessageHeaderStatusUpdate;
import mil.arl.gift.tools.dashboard.shared.messages.monitor.MessageListenChangedUpdate;
import mil.arl.gift.tools.dashboard.shared.messages.monitor.MessageReceivedUpdate;
import mil.arl.gift.tools.dashboard.shared.messages.monitor.MessageRemovedUpdate;
import mil.arl.gift.tools.dashboard.shared.messages.monitor.MessageWatchedDomainSessionUpdate;

/**
 * A provider that provides information about messages received by the web monitor's server end
 * 
 * @author nroberts
 */
public class ModuleMessageProvider extends AbstractProvider<ModuleMessageHandler> {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(ModuleMessageProvider.class.getName());

    /** The instance of this class */
    private static ModuleMessageProvider instance = null;

    /**
     * Singleton constructor
     */
    private ModuleMessageProvider() {
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
    public static ModuleMessageProvider getInstance() {
        if (instance == null) {
            instance = new ModuleMessageProvider();
        }

        return instance;
    }

    /**
     * Notifies any listening handlers that a message update has been received
     * 
     * @param update the update to apply. Cannot be null.
     */
    public void update(final AbstractMessageUpdate update) {
        /* Notify handlers */
        executeHandlers(new SafeHandlerExecution<ModuleMessageHandler>() {
            @Override
            public void execute(ModuleMessageHandler handler) {
                
                if(update instanceof MessageReceivedUpdate) {
                    handler.onMessageReceived((MessageReceivedUpdate) update);
                    
                } else if(update instanceof MessageRemovedUpdate) {
                    handler.onMessageRemoved((MessageRemovedUpdate) update);
                    
                } else if(update instanceof MessageFilterChangedUpdate) {
                    handler.onMessageFilterChanged((MessageFilterChangedUpdate) update);
                    
                } else if(update instanceof MessageListenChangedUpdate) {
                    handler.onMessageListenChanged((MessageListenChangedUpdate) update);
                    
                } else if(update instanceof MessageHeaderStatusUpdate) {
                    handler.onMessageHeaderStatusChanged((MessageHeaderStatusUpdate) update);
                    
                } else if(update instanceof MessageFilterChoicesUpdate) {
                    handler.onMessageFilterChoices((MessageFilterChoicesUpdate) update);
                    
                } else if(update instanceof MessageWatchedDomainSessionUpdate) {
                    handler.onMessageWatchedDomainSessionsChanged((MessageWatchedDomainSessionUpdate) update);
                    
                } else {
                    logger.severe("Unable to handle message update: " + update);
                }
            }
        });
    }

    /**
     * A handler used to handle web monitor status updates
     * 
     * @author nroberts
     */
    public interface ModuleMessageHandler {
        
        /**
         * Handles when a message has been received
         * 
         * @param update the update containing the information to handle. Cannot be null.
         */
        void onMessageReceived(MessageReceivedUpdate update);
        
        /**
         * Handles when a message has been removed
         * 
         * @param update the update containing the information to handle. Cannot be null.
         */
        void onMessageRemoved(MessageRemovedUpdate update);
        
        /**
         * Handles when the filtered messages have changed
         * 
         * @param update the update containing the information to handle. Cannot be null.
         */
        void onMessageFilterChanged(MessageFilterChangedUpdate update);
        
        /**
         * Handles when the web monitor starts/stops listening for message updates
         * 
         * @param update the update containing the information to handle. Cannot be null.
         */
        void onMessageListenChanged(MessageListenChangedUpdate update);
        
        /**
         * Handles when the setting to display advanced headers is changed
         * 
         * @param update the update containing the information to handle. Cannot be null.
         */
        void onMessageHeaderStatusChanged(MessageHeaderStatusUpdate update);
        
        /**
         * Handles when the available or selected filter choices change
         * 
         * @param update the update containing the information to handle. Cannot be null.
         */
        void onMessageFilterChoices(MessageFilterChoicesUpdate update);
        
        /**
         * Handles when the domain sessions being watched have changed
         * 
         * @param update the update containing the information to handle. Cannot be null.
         */
        void onMessageWatchedDomainSessionsChanged(MessageWatchedDomainSessionUpdate update);
    }
}
