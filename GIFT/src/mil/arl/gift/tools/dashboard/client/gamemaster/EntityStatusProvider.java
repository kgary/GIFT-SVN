/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.gamemaster;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import mil.arl.gift.common.gwt.client.util.AbstractProvider;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.dashboard.client.gamemaster.EntityStatusProvider.EntityStatusChangeHandler;
import mil.arl.gift.tools.map.client.draw.MilitarySymbol.Status;

/**
 * A singleton class that manages entity status changes.
 * 
 * @author sharrison
 */
public class EntityStatusProvider extends AbstractProvider<EntityStatusChangeHandler> {
    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(EntityStatusProvider.class.getName());

    /** The instance of this class */
    private static EntityStatusProvider instance = null;

    /**
     * Singleton constructor
     */
    private EntityStatusProvider() {
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
    public static EntityStatusProvider getInstance() {
        if (instance == null) {
            instance = new EntityStatusProvider();
        }

        return instance;
    }

    /**
     * The entity status has been changed.
     * 
     * @param domainSessionId the domain session id that the entity belongs to.
     * @param teamRole the team role of the entity. If blank, the status change
     *        will be ignored.
     * @param newStatus the new status value. Can't be null.
     * @param oldStatus the old status value. If the same as the new status, the
     *        status change will be ignored.
     */
    public void entityStatusChange(final int domainSessionId, final String teamRole, final Status newStatus,
            final Status oldStatus) {
        if (logger.isLoggable(Level.FINE)) {
            StringBuilder sb = new StringBuilder("entityStatusChange(");
            List<Object> params = Arrays.<Object>asList(domainSessionId, teamRole, newStatus, oldStatus);
            StringUtils.join(", ", params, sb);
            logger.fine(sb.append(")").toString());
        }

        if (newStatus == null) {
            throw new IllegalArgumentException("The parameter 'newStatus' cannot be null.");
        } else if (StringUtils.isBlank(teamRole) || newStatus == oldStatus) {
            /* No change */
            return;
        }

        /* Check if the session pushing the entity status change is
         * whitelisted */
        if (RegisteredSessionProvider.getInstance().isRegistered(domainSessionId)) {

            /* Notify handlers */
            executeHandlers(new SafeHandlerExecution<EntityStatusChangeHandler>() {
                @Override
                public void execute(EntityStatusChangeHandler handler) {
                    handler.entityStatusChange(domainSessionId, teamRole, newStatus, oldStatus);
                }
            });
        }
    }

    /**
     * Handler for listening to entity status changes.
     * 
     * @author sharrison
     */
    public interface EntityStatusChangeHandler {
        /**
         * The entity status has been updated.
         * 
         * @param domainSessionId the id of the changed entity's domain session.
         * @param teamRole the team role of the entity that changed.
         * @param newStatus the new status value.
         * @param oldStatus the old status value.
         */
        void entityStatusChange(int domainSessionId, String teamRole, Status newStatus, Status oldStatus);
    }
}
