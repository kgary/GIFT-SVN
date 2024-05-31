/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.condition;

import generated.dkf.LearnerActionEnumType;
import generated.dkf.PowerPointDwellCondition.Slides.Slide;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.ta.state.PowerPointState;
import mil.arl.gift.domain.knowledge.common.ConditionActionInterface;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the base PowerPoint Dwell condition that contains the common logic
 * for Dwell conditions.
 *
 * @author mhoffman
 */
public abstract class AbstractPowerPointDwellCondition extends AbstractCondition {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(AbstractPowerPointDwellCondition.class);

    /**
     * contains the types of GIFT messages this condition needs in order to
     * provide assessments
     */
    private static final List<MessageTypeEnum> simulationInterests;

    static {
        simulationInterests = new ArrayList<MessageTypeEnum>();
        simulationInterests.add(MessageTypeEnum.POWERPOINT_STATE);
        simulationInterests.add(MessageTypeEnum.STOP_FREEZE);
    }
    
    /**
     * contains the type of overall assessment scorers this condition can populate for an AAR
     */
    private static final Set<Class<?>> overallAssessmentTypes;
    static{
        overallAssessmentTypes = new HashSet<Class<?>>();
        overallAssessmentTypes.add(generated.dkf.Count.class);
        overallAssessmentTypes.add(generated.dkf.CompletionTime.class);
    }
    
    /** map of slide index to slide assessment information - Note: not all
     * slides have to be specified */
    protected Map<Integer, Slide> slideIndexMap;

    /** input/configuration from the dkf */
    protected generated.dkf.PowerPointDwellCondition input;

    /** single timer instance for all slide timer tasks */
    protected ReschedulableTimer slideTimer;

    /** the last received powerpoint state */
    protected PowerPointState lastState;
    
    /**
     * Default constructor - required for authoring logic
     */
    public AbstractPowerPointDwellCondition(){
        
    }

    /**
     * Class constructor - configure condition with input from domain knowledge
     *
     * @param input configuration parameters for this condition
     */
    public AbstractPowerPointDwellCondition(generated.dkf.PowerPointDwellCondition input) {

        if (input == null) {
            throw new IllegalArgumentException("The input knowledge can't be null");
        }

        slideIndexMap = new HashMap<>(input.getSlides().getSlide().size());
        for (Slide slide : input.getSlides().getSlide()) {
            slideIndexMap.put(slide.getIndex().intValue(), slide);
        }

        this.input = input;
    }
    
    // team member refs not allowed as an input
    @Override
    public String hasValidTeamMemberRefs(){
        return null;
    }

    @Override
    public List<MessageTypeEnum> getSimulationInterests() {
        return simulationInterests;
    }
    
    @Override
    public void initialize(ConditionActionInterface conditionActionInterface){
        super.initialize(conditionActionInterface);
        
        slideTimer = new ReschedulableTimer();
    }    
    
    /**
     * Return the amount of time (seconds) that is considered the threshold for
     * over dwelling on the slide index provided.
     * 
     * @param slideIndex zero based index of slides to look up the over-dwell threshold time for
     * @return the time in seconds that is considered the max time the learner should spend on the slide specified.
     */
    protected double getSlideTime(int slideIndex){
        
        double timeInSeconds;
        Slide slideInfo = slideIndexMap.get(slideIndex);
        if(slideInfo != null){
            timeInSeconds = slideInfo.getTimeInSeconds();
        }else{
            timeInSeconds = input.getDefault().getTimeInSeconds();                
        }
        
        return timeInSeconds;
    }

    /**
     * A slide dwell time has been violated. Update this condition's assessment
     * and record the event for scoring purposes.
     *
     * @param slideIndex The index of the slide that the learner has violated
     * its constraints
     */
    protected void timeViolation(int slideIndex) {

        if(logger.isInfoEnabled()){
            logger.info("Learner violated time condition for slide index of " + slideIndex);
        }

        scoringEventStarted();
    }
    
    @Override
    public Set<LearnerActionEnumType> getLearnerActionsNeeded() {
        return null;
    }
    
    @Override
    public Set<Class<?>> getOverallAssessmenTypes() {
        return overallAssessmentTypes;
    }

    /**
     * A slide dwell timer task has been activated, meaning that the slide has
     * been dwelled on for long than the timed amount.
     *
     * @param index The index of the slide
     */
    protected abstract void timeExpired(int index);

    /**
     * This class is responsible for scheduling or rescheduling a timer task
     * without having to create a new thread.
     *
     * @author mhoffman
     */
    protected class ReschedulableTimer extends Timer {

        private TimeoutTimerTask timerTask = null;

        /**
         * Class constructor - set the thread name
         */
        public ReschedulableTimer() {
            super("PowerPointDwellTimer");
        }

        /**
         * Schedule a timer task with the given delay
         *
         * @param delay - amount of time (seconds) before task is executed.  If zero the timer task
         * is not scheduled and the existing timer task is set to null.
         * @param slideIndex - slide information to assess against
         * @return boolean - true if a previous slide timer task was canceled in
         * order to schedule this request.
         */
        public boolean schedule(double delay, int slideIndex) {

            if (timerTask == null && delay > 0) {
                timerTask = new TimeoutTimerTask(slideIndex);
                scheduleAfterCheck(timerTask, (long) (delay * 1000));
                return false;
            } else {
                return reschedule((long) (delay * 1000), slideIndex);
            }
        }
        
        /**
         * Return whether or not there is a task scheduled that has not run yet.
         * 
         * @return true if there is a task currently scheduled that has not run yet, false if not.
         */
        public boolean isScheduled(){
            return timerTask != null && !timerTask.hasRun();
        }

        /**
         * Reschedules the timer task with the given delay
         *
         * @param delay - amount of time (seconds) before task is executed. If zero the timer task
         * is not scheduled and the existing timer task is set to null.
         * @param slideIndex - slide information to assess against
         * @return boolean - true if a previous slide timer task was canceled in
         * order to schedule this request.
         */
        private boolean reschedule(long delay, int slideIndex) {

            boolean canceledTask = timerTask != null && timerTask.cancel();
            if(delay > 0){
                timerTask = new TimeoutTimerTask(slideIndex);
                scheduleAfterCheck(timerTask, delay);
            }else{
                timerTask = null;
            }

            return canceledTask;
        }

        /**
         * Schedule the timer task only if the delay puts the execution of the
         * task in the future.
         *
         * @param timerTask
         * @param delay - amount of time (milliseconds) before task is executed
         */
        private void scheduleAfterCheck(TimeoutTimerTask timerTask, long delay) {

            if (delay > 0) {
                schedule(timerTask, delay);
            }
        }

        /**
         * Gets the index of the slide this timer is for
         *
         * @return int The index of the slide this timer is for
         */
        public int getSlideIndex() {
            return timerTask != null ? timerTask.getSlideIndex() : -1;
        }
    }

    /**
     * A TimerTask used to detect if a slide has been dwelled upon long enough.
     */
    private class TimeoutTimerTask extends TimerTask {

        /** the (one-based) slide index this timeout logic is associated with */
        private int slideIndex;
        
        /** whether this timer task has been executed (i.e. the run method called) */
        private AtomicBoolean hasRun = new AtomicBoolean();

        /**
         * Class constructor
         * 
         * @param slideIndex the slide associated with this timer
         */
        public TimeoutTimerTask(int slideIndex) {
            this.slideIndex = slideIndex;
        }

        @Override
        public void run() {
            hasRun.set(true);
            timeExpired(slideIndex);
        }
        
        /**
         * Return whether this timer task has been executed (i.e. the run method called)
         * @return true if the run method has been called.
         */
        public boolean hasRun(){
            return hasRun.get();
        }

        /**
         * Return the (one-based) slide index this timeout logic is associated with
         * @return the slide index
         */
        public int getSlideIndex() {
            return slideIndex;
        }
    }
}
