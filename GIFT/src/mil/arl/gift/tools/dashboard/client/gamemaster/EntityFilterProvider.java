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
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import mil.arl.gift.common.gwt.client.util.AbstractProvider;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.dashboard.client.gamemaster.EntityFilterProvider.EntitySelectionChangeHandler;

/**
 * A singleton class that handles which domain sessions are allowed to have
 * their messages processed.
 *
 * @author sharrison
 */
public class EntityFilterProvider extends AbstractProvider<EntitySelectionChangeHandler> {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(EntityFilterProvider.class.getName());

    /** The instance of this class */
    private static EntityFilterProvider instance = null;

    /**
     * Singleton constructor
     */
    private EntityFilterProvider() {
    }

    /**
     * Accessor to the provider singleton object. If it doesn't exist yet it
     * will be created.
     *
     * @return the instance to the provider singleton object.
     */
    public static EntityFilterProvider getInstance() {
        if (instance == null) {
            instance = new EntityFilterProvider();
        }

        return instance;
    }

    /**
     * Notify any listeners that the provided entity role was selected by the
     * entity filter.
     *
     * @param domainSessionId the id of the domain session where the entity was
     *        selected.
     * @param entityRole the team role of the selected entity(s).
     */
    public void entitySelected(final int domainSessionId, final Set<String> entityRole) {
        if (logger.isLoggable(Level.FINE)) {
            StringBuilder sb = new StringBuilder("entitySelected(");
            List<Object> params = Arrays.<Object>asList(domainSessionId, entityRole);
            StringUtils.join(", ", params, sb);
            logger.fine(sb.append(")").toString());
        }

        if (CollectionUtils.isEmpty(entityRole)) {
            return;
        }

        /* Notify handlers */
        executeHandlers(new SafeHandlerExecution<EntitySelectionChangeHandler>() {
            @Override
            public void execute(EntitySelectionChangeHandler handler) {
                handler.entitySelected(domainSessionId, entityRole);
            }
        });
    }

    /**
     * Handler for listening to events from the entity filter.
     *
     * @author sharrison
     */
    public interface EntitySelectionChangeHandler {

        /**
         * The entity(s) was selected by the entity filter.
         *
         * @param domainSessionId the id of the domain session where the entity
         *        was selected.
         * @param entityRole the team role of the selected entity(s).
         */
        void entitySelected(int domainSessionId, Set<String> entityRole);
    }
}
