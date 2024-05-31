/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.gamemaster;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import mil.arl.gift.common.gwt.client.util.AbstractProvider;
import mil.arl.gift.tools.dashboard.client.gamemaster.TimelineProvider.TimelineChangeHandler;

/**
 * Provider to listen to timeline updates and to get the current playback time
 * position.
 * 
 * @author sharrison
 */
public class TimelineProvider extends AbstractProvider<TimelineChangeHandler> {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(TimelineProvider.class.getName());

    /** The instance of this class */
    private static TimelineProvider instance = null;

    /** The instance of the timeline chart */
    private Long playbackTime;
    
    /** 
     * The last time when the playback began playing. Used to determine how much time has passed in
     * order to perform dead reckoning.
     */
    private Long playStartMillis = null;

    /**
     * Singleton constructor
     */
    private TimelineProvider() {
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
    public static TimelineProvider getInstance() {
        if (instance == null) {
            instance = new TimelineProvider();
        }

        return instance;
    }

    /** Resets the playback time to an uninitialized state */
    public void reset() {
        playbackTime = null;
        playStartMillis = null;
    }

    /**
     * Set the current playback time.
     * 
     * @param playbackTime the playback time to set.
     */
    public void setPlaybackTime(final long playbackTime) {
        boolean changed = this.playbackTime == null || this.playbackTime.compareTo(playbackTime) != 0;
        this.playbackTime = playbackTime;

        if (changed) {
            /* Notify handlers */
            executeHandlers(new SafeHandlerExecution<TimelineChangeHandler>() {
                @Override
                public void execute(TimelineChangeHandler handler) {
                    handler.onPlayheadMoved(playbackTime);
                }
            });
        }
        
        if(playStartMillis != null) {
            
            //if a session is currently playing, reset the time that playing was started
            playStartMillis = null;
            play();
        }
    }

    /**
     * Force a reload for the timeline.
     */
    public void reloadTimeline() {
        /* Notify handlers */
        executeHandlers(new SafeHandlerExecution<TimelineChangeHandler>() {
            @Override
            public void execute(TimelineChangeHandler handler) {
                handler.reloadTimeline();
            }
        });
    }

    /**
     * Shows only the given tasks in the timeline.
     * 
     * @param taskIds the task id to show. If null or empty, all tasks should be
     *        shown.
     */
    public void showTasks(final Set<Integer> taskIds) {
        /* Notify handlers */
        executeHandlers(new SafeHandlerExecution<TimelineChangeHandler>() {
            @Override
            public void execute(TimelineChangeHandler handler) {
                handler.showTasks(taskIds);
            }
        });
    }

    /**
     * Get the current playback time. <br/>
     * <br/>
     * For past sessions, this is calculated as a combination of two factors:
     * the last playback time reported by the server + how much time has passed
     * while the session was playing. This dead-reckoning logic helps deal with
     * occasions when there is a lull in the session playback where few server
     * updates are pushed to the client to update the playback time.
     * 
     * @return the current playback time.
     * @throws UnsupportedOperationException if the playbackTime is null.
     */
    public long getPlaybackTime() {
        if (playbackTime == null) {
            throw new UnsupportedOperationException("The parameter 'playbackTime' has never been set.");
        }
        
        return playbackTime + (playStartMillis != null 
                ? System.currentTimeMillis() - playStartMillis
                : 0);
    }
    
    /**
     * Begins incrementing the playback time as the session is played back. This does <u>NOT</u> affect the 
     * actual playback on the server, but it does help the client say in-sync with the server playback time
     * during periods of low server traffic.
     * <br/><br/>
     * This should ideally be invoked after performing server operations to begin playing the session.
     */
    public void play() {
        
        if(playStartMillis == null) {
            playStartMillis = System.currentTimeMillis();
        }
    }
    
    /**
     * Stops incrementing the playback time as the session is played back. This does <u>NOT</u> affect the 
     * actual playback on the server, but it does help the client say in-sync with the server playback time
     * during periods of low server traffic.
     * <br/><br/>
     * This should ideally be invoked after performing server operations to pause the session.
     */
    public void pause() {
        
        if(playbackTime != null && playStartMillis != null ) {
            playbackTime += (System.currentTimeMillis() - playStartMillis);
        }
        
        playStartMillis = null;
    }
    
    /**
     * Whether the playback time is currently paused
     * 
     * @return whether the time is paused
     */
    public boolean isPaused() {
        
        if(playbackTime == null) {
            
            /* This is an active session, which cannot be paused */
            return false;
        }
        
        return playStartMillis == null;
    }

    /**
     * Handler for listening to timeline updates or pushing changes to the timeline.
     * 
     * @author sharrison
     */
    public interface TimelineChangeHandler {
        /**
         * The playhead has been moved to the provided position.
         * 
         * @param playbackTime the current playback time
         */
        void onPlayheadMoved(long playbackTime);

        /**
         * Force a reload for the timeline.
         */
        void reloadTimeline();

        /**
         * Shows only the given tasks in the timeline.
         * 
         * @param taskIds the task id to show. If null or empty, all tasks
         *        should be shown.
         */
        void showTasks(Set<Integer> taskIds);
    }
}
