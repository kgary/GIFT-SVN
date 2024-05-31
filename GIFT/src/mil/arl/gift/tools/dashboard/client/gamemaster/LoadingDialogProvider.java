/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.gamemaster;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;

import mil.arl.gift.common.gwt.client.widgets.bootstrap.BsLoadingDialogBox;
import mil.arl.gift.common.util.StringUtils;

/**
 * A singleton class that manages the game master components with blocking or
 * time-intensive loads.
 * 
 * @author sharrison
 */
public class LoadingDialogProvider {
    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(LoadingDialogProvider.class.getName());

    /** The instance of the class */
    private static LoadingDialogProvider instance = null;

    /**
     * The type of component or action that is being loaded.
     * 
     * @author sharrison
     */
    public enum LoadingType {
        /** Fetching the playback log from the server */
        FETCH_PLACKBACK_LOG,
        /** Connecting or disconnecting from the gateway connections */
        GATEWAY_CONNECTIONS,
        /** Removing the playback log patches */
        PATCH_REMOVAL,
        /** Registering a session */
        REGISTERED_SESSION,
        /** Loading the session data panel */
        SESSION_DATA_PANEL,
        /** Loading the state pane */
        STATE_PANE,
        /** Reloading the timeline (server call) */
        TIMELINE_RELOAD,
        /** Refreshing the timeline (drawing) */
        TIMELINE_REFRESH,
        /** Loading the playback videos */
        VIDEOS;
    }

    /**
     * The priority which the {@link LoadingType} should be displayed.
     * 
     * @author sharrison
     */
    public enum LoadingPriority {
        /** Low priority is used for non-important informational data */
        LOW(2),
        /** Medium priority is used for important informational data */
        MEDIUM(5),
        /**
         * High priority is used for highly relevant information that the user
         * needs to see
         */
        HIGH(7),
        /**
         * Immediate priority will be shown at once regardless of any other load
         * request
         */
        IMMEDIATE(10);

        /** The priority level. Higher values are shown first. */
        private final int level;

        /**
         * Constructor.
         * 
         * @param level the priority level. Higher values are shown first.
         */
        private LoadingPriority(int level) {
            this.level = level;
        }
    }

    /**
     * Using a LinkedHashMap to preserve insertion order. When the
     * {@link LoadingPriority} is the same, this will act like a FIFO queue.
     */
    private LinkedHashMap<LoadingType, LoadingRequest> loadingComponents = new LinkedHashMap<>();

    /** The request that is currently being shown in the loading dialog */
    private LoadingRequest beingShown;

    /**
     * Singleton constructor
     */
    private LoadingDialogProvider() {
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
    public static LoadingDialogProvider getInstance() {
        if (instance == null) {
            instance = new LoadingDialogProvider();
        }

        return instance;
    }

    /**
     * Notify that a component or action requires a loading dialog.
     * 
     * @param loadingType the loading type specifying what is requesting the
     *        loading dialog. Can't be null.
     * @param title the title of the loading dialog. Can't be null or empty.
     * @param message the message content of the loading dialog. Can't be null
     *        or empty.
     */
    public void startLoading(final LoadingType loadingType, final String title, final String message) {
        startLoading(loadingType, null, title, message);
    }

    /**
     * Notify that a component or action requires a loading dialog.
     * 
     * @param loadingType the loading type specifying what is requesting the
     *        loading dialog. Can't be null.
     * @param priority the priority which the {@link LoadingType} should be
     *        displayed. If null, it will default to
     *        {@link LoadingPriority#MEDIUM}.
     * @param title the title of the loading dialog. Can't be null or empty.
     * @param message the message content of the loading dialog. Can't be null
     *        or empty.
     */
    public void startLoading(LoadingType loadingType, LoadingPriority priority, String title, String message) {
        if (loadingType == null) {
            throw new IllegalArgumentException("The parameter 'loadingType' cannot be null.");
        } else if (StringUtils.isBlank(title)) {
            throw new IllegalArgumentException("The parameter 'title' cannot be blank.");
        } else if (StringUtils.isBlank(message)) {
            throw new IllegalArgumentException("The parameter 'message' cannot be blank.");
        } else if (loadingComponents.containsKey(loadingType)) {
            return;
        }

        final LoadingRequest loadingRequest = new LoadingRequest(loadingType, priority, title, message);
        loadingComponents.put(loadingType, loadingRequest);

        if (!BsLoadingDialogBox.isDialogVisible() || LoadingPriority.IMMEDIATE.equals(priority)
                || loadingRequest.isHigherPriority(beingShown)) {
            BsLoadingDialogBox.display(title, message);
            beingShown = loadingRequest;
        }
    }

    /**
     * Notify that a component or action has finished loading.
     * 
     * @param loadingType the loading type specifying what has finished loading.
     *        If null, nothing will happen.
     */
    public void loadingComplete(LoadingType loadingType) {
        if (loadingType == null || !loadingComponents.containsKey(loadingType)) {
            return;
        }

        LoadingRequest removed = loadingComponents.remove(loadingType);
        if(removed != null){
            logger.info("removed loading component of " +removed);
        }

        if (loadingComponents.isEmpty()) {
            /* Defer hiding the dialog so any processes can finish */
            Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                @Override
                public void execute() {
                    if (!loadingComponents.isEmpty()) {
                        /* Another request came in before the scheduler could
                         * call this */
                        return;
                    }
                    BsLoadingDialogBox.remove();
                    beingShown = null;
                }
            });
        } else if (loadingType == beingShown.loadingType) {
            LoadingRequest requestToShow = null;
            final Iterator<LoadingRequest> requestItr = loadingComponents.values().iterator();
            do {
                /* Guaranteed to have at least 1 request in the map */
                LoadingRequest nextRequest = requestItr.next();
                if (requestToShow == null || nextRequest.isHigherPriority(requestToShow)) {
                    requestToShow = nextRequest;
                }
            } while (requestItr.hasNext());

            beingShown = requestToShow;
            BsLoadingDialogBox.updateTitle(requestToShow.title);
            BsLoadingDialogBox.updateMessage(requestToShow.message);
        }
    }

    /**
     * The container class for the loading request properties.
     * 
     * @author sharrison
     */
    private class LoadingRequest {
        /** The loading type specifying what is requesting the loading dialog */
        private LoadingType loadingType;
        /** The priority which the {@link LoadingType} should be displayed */
        private LoadingPriority priority;
        /** The title of the loading dialog */
        private String title;
        /** The message content of the loading dialog */
        private String message;

        /**
         * Constructor.
         * 
         * @param loadingType the loading type specifying what is requesting the
         *        loading dialog. Can't be null.
         * @param priority the priority which the {@link LoadingType} should be
         *        displayed. If null, it will default to
         *        {@link LoadingPriority#MEDIUM}.
         * @param title the title of the loading dialog. Can't be null or empty.
         * @param message the message content of the loading dialog. Can't be
         *        null or empty.
         */
        public LoadingRequest(LoadingType loadingType, LoadingPriority priority, String title, String message) {
            if (loadingType == null) {
                throw new IllegalArgumentException("The parameter 'loadingType' cannot be null.");
            } else if (StringUtils.isBlank(title)) {
                throw new IllegalArgumentException("The parameter 'title' cannot be blank.");
            } else if (StringUtils.isBlank(message)) {
                throw new IllegalArgumentException("The parameter 'message' cannot be blank.");
            }

            this.loadingType = loadingType;
            this.priority = priority == null ? LoadingPriority.MEDIUM : priority;
            this.title = title;
            this.message = message;
        }

        /**
         * Checks if this request is a higher priority than the one comparing
         * against.
         * 
         * @param other the request to compare against. Can't be null.
         * @return true if the other request has a lower priority value; false
         *         if the other request has a high priority value.
         */
        public boolean isHigherPriority(LoadingRequest other) {
            if (other == null) {
                throw new IllegalArgumentException("The parameter 'other' cannot be null.");
            }

            return Integer.compare(this.priority.level, other.priority.level) > 0;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("[LoadingRequest: loadingType=");
            builder.append(loadingType);
            builder.append(", priority=");
            builder.append(priority);
            builder.append(", title=");
            builder.append(title);
            builder.append(", message=");
            builder.append(message);
            builder.append("]");
            return builder.toString();
        }        
        
    }
}
