/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.gamemaster;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import mil.arl.gift.common.enums.LessonLevelEnum;
import mil.arl.gift.common.gwt.client.util.AbstractProvider;
import mil.arl.gift.tools.dashboard.client.Dashboard;
import mil.arl.gift.tools.dashboard.client.gamemaster.PermissionsProvider.PermissionUpdateHandler;

/**
 * A singleton class that manages permission updates.
 * 
 * @author mhoffman
 */
public class PermissionsProvider extends AbstractProvider<PermissionUpdateHandler> {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(PermissionsProvider.class.getName());

    /** The instance of this class */
    private static PermissionsProvider instance = null;

    /** the current game master enumerated mode */
    private Mode currentMode = Mode.ACTIVE_SESSION;

    /**
     * Enumeration of the different types of modes the game master can be in.
     * 
     * @author mhoffman
     *
     */
    public enum Mode {

        /** playing a past domain session log, i.e. the timeline is visible */
        PAST_SESSION_PLAYBACK(
                Component.END_KNOWLEDGE_SESSION,
                Component.AUTO_APPLY_STRATEGIES,
                Component.STRATEGY_REQUESTS,
                Component.STRATEGY_PRESETS,
                Component.TASK_START_END),

        /**
         * playing a past domain session log as an active session, i.e.
         * emulating learner(s)
         */
        ACTIVE_SESSION_PLAYBACK(),

        /** a live domain session, normally with a training application */
        ACTIVE_SESSION();

        /** the components which should be hidden from the game master user */
        private final Set<Component> disAllowedComponents;

        /**
         * Set attribute
         * 
         * @param components the components which should be hidden from the game
         *        master user
         */
        private Mode(Component... components) {
            disAllowedComponents = new HashSet<>(Arrays.asList(components));
            
            if(Dashboard.getInstance().getServerProperties().getLessonLevel() == LessonLevelEnum.RTA){
                // game master is not in charge of approving strategies when in RTA lesson level, the external
                // system connected via the GW module is.
                disAllowedComponents.add(Component.STRATEGY_REQUESTS);
            }
        }

        /**
         * Return the components which should be hidden from the game master
         * user
         * 
         * @return will not be null, but can be empty.
         */
        public Set<Component> getDisallowedComponents() {
            return disAllowedComponents;
        }

    }

    /**
     * Enumeration of the different type of UI components that could be hidden
     * depending on the type of game master mode.
     * 
     * @author mhoffman
     *
     */
    public enum Component {
        /** the button for ending the knowledge session */
        END_KNOWLEDGE_SESSION,

        /** the checkbox for 'auto apply strategies' */
        AUTO_APPLY_STRATEGIES,

        /** the strategy requests header/stack of the assessment panel */
        STRATEGY_REQUESTS,

        /** the strategy presets header/stack of the assessment panel */
        STRATEGY_PRESETS,

        /**
         * the ability to change/send an evaluator update request and the UI
         * components related to this
         */
        EVALUATOR_UPDATE,

        /** the play/pause buttons on a task */
        TASK_START_END
    }

    /**
     * Singleton constructor
     */
    private PermissionsProvider() {
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
    public static PermissionsProvider getInstance() {
        if (instance == null) {
            instance = new PermissionsProvider();
        }

        return instance;
    }

    /**
     * Return the current permission mode.
     * 
     * @return the current mode. Can't be null.
     */
    public Mode getCurrentMode() {
        return currentMode;
    }

    /**
     * Update the current game master enumerated mode which will then notify any
     * registered handlers of the disallowed components to hide.
     * 
     * @param mode the enumerated game master mode to set as the current mode
     */
    public void permissionUpdate(final Mode mode) {

        if (logger.isLoggable(Level.INFO)) {
            logger.info("setting permission mode to " + mode);
        }

        if (mode != null) {
            this.currentMode = mode;

            /* Notify handlers */
            executeHandlers(new SafeHandlerExecution<PermissionUpdateHandler>() {
                @Override
                public void execute(PermissionUpdateHandler handler) {
                    handler.permissionUpdate(mode.getDisallowedComponents());
                }
            });
        }
    }

    /**
     * Add a handler to listen to permission updates. Also notifies the handler
     * of the current disallowed components based on the current enumerated
     * mode. Make sure to call this after binding.
     * 
     * @param handler the handler to add.
     */
    @Override
    public void addHandler(PermissionUpdateHandler handler) {
        super.addHandler(handler);

        if (currentMode != null) {
            // notify handler of current state
            handler.permissionUpdate(currentMode.getDisallowedComponents());
        }
    }

    /**
     * Used to notify listeners of game master permission updates and which
     * components should be hidden from the user at this time.
     * 
     * @author mhoffman
     *
     */
    public interface PermissionUpdateHandler {

        /**
         * Notification of the current enumerated components that should be
         * hidden from the user based on the current enumerated mode of the game
         * master.
         * 
         * @param disallowedComponents collection of enumerated components that
         *        should be hidden. Won't be null but can be empty
         */
        void permissionUpdate(Set<Component> disallowedComponents);
    }
}
