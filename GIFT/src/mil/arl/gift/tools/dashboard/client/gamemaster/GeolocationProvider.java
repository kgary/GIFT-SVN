/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.gamemaster;

import java.util.logging.Level;
import java.util.logging.Logger;

import mil.arl.gift.common.gwt.client.util.AbstractProvider;
import mil.arl.gift.tools.dashboard.client.gamemaster.GeolocationProvider.GeolocationUpdateHandler;
import mil.arl.gift.tools.dashboard.shared.messages.DetonationUpdate;
import mil.arl.gift.tools.dashboard.shared.messages.EntityStateUpdate;
import mil.arl.gift.tools.dashboard.shared.messages.RemoveEntityMessage;

/**
 * A singleton class that manages geolocation updates.
 *
 * @author sharrison
 */
public class GeolocationProvider extends AbstractProvider<GeolocationUpdateHandler> {
    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(GeolocationProvider.class.getName());

    /** The instance of this class */
    private static GeolocationProvider instance = null;

    /**
     * Singleton constructor
     */
    private GeolocationProvider() {
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
    public static GeolocationProvider getInstance() {
        if (instance == null) {
            instance = new GeolocationProvider();
        }

        return instance;
    }

    /**
     * Request that a specific entity be removed.
     *
     * @param removeEntityMessage contains information about the entity to
     *        remove. Can't be null.
     */
    public void removeEntityRequest(final RemoveEntityMessage removeEntityMessage) {
        if (removeEntityMessage == null) {
            throw new IllegalArgumentException("The parameter 'removeEntityMessage' cannot be null.");
        }

        /* Notify handlers */
        executeHandlers(new SafeHandlerExecution<GeolocationUpdateHandler>() {
            @Override
            public void execute(GeolocationUpdateHandler handler) {
                handler.removeEntityRequest(removeEntityMessage);
            }
        });
    }

    /**
     * The location of an entity has been updated.
     *
     * @param update contains data about the entity's location. Can't be null.
     */
    public void entityLocationUpdate(final EntityStateUpdate update) {
        if (update == null) {
            throw new IllegalArgumentException("The parameter 'update' cannot be null.");
        }

        /* Check if the session pushing the entity location update is
         * whitelisted */
        final int domainSessionId = update.getSessionEntityId().getHostDomainSessionId();
        if (!RegisteredSessionProvider.getInstance().isRegistered(domainSessionId)) {
            return;
        }

        /* Notify handlers */
        executeHandlers(new SafeHandlerExecution<GeolocationUpdateHandler>() {
            @Override
            public void execute(GeolocationUpdateHandler handler) {
                handler.entityLocationUpdate(update);
            }
        });
    }
    
    /**
     * The location of an detonation has been updated.
     *
     * @param update contains data about the detonation's location. Can't be null.
     */
    public void detonationLocationUpdate(final DetonationUpdate update) {
        
        if (update == null) {
            throw new IllegalArgumentException("The parameter 'update' cannot be null.");
        }

        /* Notify handlers */
        executeHandlers(new SafeHandlerExecution<GeolocationUpdateHandler>() {
            @Override
            public void execute(GeolocationUpdateHandler handler) {
                handler.detonationLocationUpdate(update);
            }
        });
    }

    /**
     * Handler for listening to geolocation updates.
     *
     * @author sharrison
     */
    public interface GeolocationUpdateHandler {
        /**
         * The location of an entity has been updated.
         *
         * @param update contains data about the entity's location.
         */
        void entityLocationUpdate(EntityStateUpdate update);

        /**
         * Request to remove a specific entity.
         *
         * @param remove contains information about the entity to remove. Can't
         *        be null.
         */
        void removeEntityRequest(RemoveEntityMessage remove);
        
        /**
         * The location of an detonation has been updated.
         *
         * @param update contains data about the detonation's location.
         */
        void detonationLocationUpdate(DetonationUpdate update);
    }
}
