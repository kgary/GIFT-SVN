/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.condition;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generated.dkf.LearnerActionEnumType;
import mil.arl.gift.common.course.dkf.ConditionDescription;
import mil.arl.gift.common.course.dkf.ConditionDescription.FileDescription;
import mil.arl.gift.common.course.dkf.team.TeamMember;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.ta.state.EntityIdentifier;
import mil.arl.gift.common.ta.state.WeaponFire;
import mil.arl.gift.net.api.message.Message;

/**
 * This condition assesses whether a fire team is conducting the correct rate of fire as a team.
 * This uses the Talking the guns ratio formula of:<br/>
 * 1 - (total dead space time / total drill time)
 * @author mhoffman
 *
 */
public class FireTeamRateOfFireCondition extends AbstractCondition {
    
    /** The logger for the class */
    private static final Logger logger = LoggerFactory.getLogger(FireTeamRateOfFireCondition.class);
    
    /** The message types that this condition is interested in. */
    private static final List<MessageTypeEnum> simulationInterests = Arrays.asList(
            MessageTypeEnum.WEAPON_FIRE);
    
    /**
     * The type of overall assessment scorers this condition can populate for an
     * AAR.
     */
    private static final Set<Class<?>> overallAssessmentTypes = new HashSet<>();
    static {
        overallAssessmentTypes.add(generated.dkf.Count.class);
        overallAssessmentTypes.add(generated.dkf.ViolationTime.class);
    }

    /** Information about the purpose of this condition */
    private static final ConditionDescription description = new FileDescription(
            Paths.get("docs", "conditions", "FireTeamRateOfFire.GIFT Domain condition description.html").toFile(),
            "Fire Team Rate of Fire");
    
    /** name of the thread used to assess the rate of fire after a certain window of time */
    private static final String TIMER_NAME = "Fire Team Rate of Fire timer";
    
    /** 
     * default amount of time in seconds to have rapid fire rate of fire to be assessed At Expectation.  
     * This happens during the rapid fire interval and therefore should be less than or equal too the rapid fire interval value. */
    private static final int DEFAULT_RAPID_FIRE_ASSESSMENT_MS = 30000;
    
    /**
     * default window of time in seconds to look for the minimum rapid fire interval.  
     * This window starts at the beginning of assessing this condition.  
     * The value should be equal too or greater than the minimum rapid fire interval value.
     */
    private static final int DEFAULT_RAPID_FIRE_INTERVAL_MS = 60000;
    
    /** default seconds until first assessment - NOTE: matches authoring tool and dkf.xsd default */
    private static final int DEFAULT_FIRST_ASSESSMENT_MS = 30000;
    
    /** defaults seconds until assessments after the first assessment - NOTE: matches authoring tool and dkf.xsd default */
    private static final int DEFAULT_SUBSEQUENT_ASSESSMENT_MS = 5000;    
    
    /** amount of time in milliseconds to add to the left and right of a shot to make the shot have duration */
    private static final long ALPHA = 250; 
    
    /** default minimum amount of shots to get At Expectation on the rapid fire assessment portion of this condition  */
    private static final int DEFAULT_RAPID_FIRE_SHOTS_PER_MIN = 300;
    
    /** the lower bound rate of fire ratio to use for an At Expectation assessment [0, 1.0]*/
    private final double atExpectationLowerBound;
    
    /** the upper bound rate of fire ratio to use for an At Expectation assessment [0, 1.0]*/
    private final double atExpectationUpperBound;
    
    /** 
     * window of time in seconds to look for the minimum rapid fire interval.  
     * This window starts at the beginning of assessing this condition.  
     * The value should be equal too or greater than the minimum rapid fire interval value {@link #minRapidFireMs}.
     */
    private final int rapidFireIntervalMs;
    
    /**
     * amount of time in seconds to have rapid fire rate of fire to be assessed At Expectation.  
     * This happens during the rapid fire interval and therefore should be less than or equal too the rapid fire interval value {@link #rapidFireIntervalMs}.
     */
    private final int minRapidFireMs;
    
    /** the amount of shots per minute to reach At Expectation on the rapid fire assessment portion of this condition*/
    private final int rapidFireRatePerMin;
    
    /** 
     * the amount of shots per rapid fire assessment interval to reach At expectation on the rapid fire 
     * assessment portion of this condition
     * E.g. if the interval is 30 seconds and the authored rounds per min is 300, this value would be 300 / 2 = 150 rounds per 30 seconds.
     */
    private final int rapidFireRatePerInterval;
    
    /**
     * the highest rate of fire (rounds per minute) found in the rapid fire rate interval.
     */
    private int highestRapidFireRatePerInterval = 0;
    
    /** seconds until first assessment (e.g. 30 seconds) */
    private final int firstAssessmentMs;
    
    /** seconds until assessments after the first assessment [0, 1.0] */
    private final int subsequentAssessmentMs;
    
    /** the last calculated rate of fire ratio (between 0 and 1.0) */
    private double lastRateOfFireRatio = 0.0;
    
    /** used when the last shot window in the current assessment window leaks into the next assessment window
     * This should be an epoch value in milliseconds.  If 0, there is no previous right shot time value to consider.
     */
    private long prevRightShotTime = 0;
       
    /** used to keep track of shots that have happened since the last assessment window, contains epoch times */
    private List<Long> currShotTimes = new ArrayList<>();
    
    /**
     * used to repeatedly schedule a task that assesses the fire team rate of fire
     */
    private SchedulableTimer assessmentTimer = null;
    
    /** flag used to indicate if the rapid fire assessment portion of this condition is finished */
    private boolean hasFinishedRapidFireAssessment = false;
    
    /**
     * Empty constructor required for authoring logic to work.
     */
    public FireTeamRateOfFireCondition() {
        if (logger.isInfoEnabled()) {
            logger.info("FireTeamRateOfFireCondition()");
        }
     
        this.atExpectationLowerBound = 0.2;
        this.atExpectationUpperBound = 0.35;
        this.firstAssessmentMs = DEFAULT_FIRST_ASSESSMENT_MS;
        this.subsequentAssessmentMs = DEFAULT_SUBSEQUENT_ASSESSMENT_MS;
        this.rapidFireIntervalMs = DEFAULT_RAPID_FIRE_INTERVAL_MS;
        this.minRapidFireMs = DEFAULT_RAPID_FIRE_ASSESSMENT_MS;
        this.rapidFireRatePerMin = DEFAULT_RAPID_FIRE_SHOTS_PER_MIN;
        this.rapidFireRatePerInterval = (int) (rapidFireRatePerMin / (60000 / (double)minRapidFireMs));
    }
    
    /**
     * Set and validate configuration attributes
     * @param input configuration for this condition
     */
    public FireTeamRateOfFireCondition(generated.dkf.FireTeamRateOfFireCondition input){
        super(AssessmentLevelEnum.UNKNOWN);
        if (logger.isInfoEnabled()) {
            logger.info("FireTeamRateOfFireCondition(" + input + ")");
        }
        
        if (input == null) {
            throw new IllegalArgumentException("The parameter 'input' cannot be null.");
        }
        
        setTeamMembersBeingAssessed(input.getTeamMemberRefs());
        
        this.atExpectationUpperBound = input.getAtExpectationUpperBound().doubleValue();
        this.atExpectationLowerBound = input.getBelowExpectationUpperBound().doubleValue();
        if(atExpectationLowerBound > atExpectationUpperBound){
            throw new RuntimeException("The lower bound value "+atExpectationLowerBound+" is greater than the upper bound value "+atExpectationUpperBound+".");
        }
        
        if(input.getMinRapidFireInterval() != null) {
            if(input.getMinRapidFireInterval().intValue() <= 0) {
                throw new RuntimeException("The minimum rapid fire seconds value of "+input.getMinRapidFireInterval()+" is not greater than 0");
            }
            this.minRapidFireMs = input.getMinRapidFireInterval().intValue() * 1000;
        }else {
            this.minRapidFireMs = DEFAULT_RAPID_FIRE_ASSESSMENT_MS;
        }
        
        if(input.getRapidFireInterval() != null) {
            if(input.getRapidFireInterval().intValue() <= 0) {
                throw new RuntimeException("The rapid fire interval seconds value of "+input.getRapidFireInterval()+" is not greater than 0");
            }
            this.rapidFireIntervalMs = input.getRapidFireInterval().intValue() * 1000;
        }else {
            this.rapidFireIntervalMs = DEFAULT_RAPID_FIRE_INTERVAL_MS;
        }
        
        if(this.rapidFireIntervalMs < this.minRapidFireMs) {
            throw new RuntimeException("The rapid fire interval seconds value of "+this.rapidFireIntervalMs+
                    " is not greater than or equal too the minimum rapid fire seconds value of "+this.minRapidFireMs+".");
        }
        
        if(input.getRapidFireRoundsPerMinute() != null) {
            
            if(input.getRapidFireRoundsPerMinute().intValue() <= 0) {
                throw new RuntimeException("The rounds per minutes of "+input.getRapidFireRoundsPerMinute()+" is not greater than 0");
            }
            
            this.rapidFireRatePerMin = input.getRapidFireRoundsPerMinute().intValue();
        }else {
            this.rapidFireRatePerMin = DEFAULT_RAPID_FIRE_SHOTS_PER_MIN;
        }
        
        this.rapidFireRatePerInterval = (int) (rapidFireRatePerMin / (60000 / (double)minRapidFireMs));
        
        if(input.getSecUntilFirstAssessment() != null){
            
            if(input.getSecUntilFirstAssessment().intValue() <= 0){
                throw new RuntimeException("The seconds until the first assessment of "+input.getSecUntilFirstAssessment()+" is not greater than 0");
            }
            
            this.firstAssessmentMs = input.getSecUntilFirstAssessment().intValue() * 1000;
        }else{
            this.firstAssessmentMs = DEFAULT_FIRST_ASSESSMENT_MS;
        }
        
        if(input.getSubsequentAssessmentInterval() != null){
            
            if(input.getSubsequentAssessmentInterval().intValue() <= 0){
                throw new RuntimeException("The seconds until each assessment after the first of "+input.getSubsequentAssessmentInterval()+" is not greater than 0");
            }
            
            this.subsequentAssessmentMs = input.getSubsequentAssessmentInterval().intValue() * 1000;
        }else{
            this.subsequentAssessmentMs = DEFAULT_FIRST_ASSESSMENT_MS;
        }

    }
    
    @Override
    public void start(){
        super.start();
        
        //start the rate of fire assessment repeating task thread
        if(assessmentTimer == null){
            // the timer is not running and the timer is needed in order to determine if the fire team has violated the 
            // rate of fire over a window of time

            assessmentTimer = new SchedulableTimer(TIMER_NAME);
            
            TimerTask timerTask = new RapidAssessmentTimerTask();

            // when to fire the first assessment and subsequent assessments
            assessmentTimer.schedule(timerTask, rapidFireIntervalMs);

            if(logger.isDebugEnabled()) {
                logger.debug("Started the fire team RAPID rate of fire condition.  Starting the assessment timer task scheduler.");
            }
        }
    }
    
    @Override
    public void stop(){
        super.stop();
        
        // finish the repeating timer task
        if(assessmentTimer != null){
            assessmentTimer.cancel();
        }
        
        // the next time the ancestor task starts should not be influenced by
        // the previous set of shots collected
        currShotTimes.clear();
    }
    
    /**
     * Updates the value of {@link AbstractCondition#assessmentExplanation}.
     */
    private void updateExplanation() {
        
        if(AssessmentLevelEnum.BELOW_EXPECTATION.equals(getAssessment())){
            // failed something...
            final StringBuilder sb = new StringBuilder();
            if(hasFinishedRapidFireAssessment) {
                // failed sustained rate of fire at the current assessment time
                sb.append("Actual: ").append(String.format("%.2f", lastRateOfFireRatio)).append(", Standard: ").append(String.format("%.2f", atExpectationLowerBound)).append(" (TG ratio)");
            }else {
                // failed rapid fire assessment
                int detectedRPM = (int) (highestRapidFireRatePerInterval * (60000 / (double)minRapidFireMs));
                sb.append("Peak: ").append(detectedRPM).append(",  Standard: ").append(rapidFireRatePerMin).append(" (Rounds/Minute)");
            }
            assessmentExplanation = sb.toString();
        }else{
            // passed something...
            final StringBuilder sb = new StringBuilder();
            if(hasFinishedRapidFireAssessment) {
                // passed sustained rate of fire at the current assessment time...
                if(AssessmentLevelEnum.AT_EXPECTATION.equals(getAssessment())) {
                    // met the standard TG ratio
                    sb.append("Met Standard TG ratio: ").append(String.format("%.2f", atExpectationLowerBound));
                }else {
                    // above the standard TG ratio
                    sb.append("Exceeded Standard TG ratio: ").append(String.format("%.2f", atExpectationUpperBound));
                }
            }else {
                // passed rapid fire assessment
                int detectedRPM = (int) (highestRapidFireRatePerInterval * (60000 / (double)minRapidFireMs));
                sb.append("Peak: ").append(detectedRPM).append(",  Standard: ").append(rapidFireRatePerMin).append(" (Rounds/Minute)");
            }
            assessmentExplanation = sb.toString();
        }
    }
    
    /**
     * A method that handles the case when a the fire team is non properly
     * maintaining rate of fire
     *
     * @return True if this violation resulted in a change in the assessment
     *         value, otherwise false.
     */
    private boolean handleViolation() {
        if (logger.isDebugEnabled()) {
            logger.debug("handleViolation()");
        }
        
        final AssessmentLevelEnum prevAssessment = getAssessment();
        
        /* If a scoring event is ongoing, reassess using the custom real-time
         * assessment in the event that the assessment value is dependent on
         * violation time. */
        if (isScoringEventActive()) {

            /* If the custom assessment is unchanged, no further action is
             * needed. */
            if (getAssessment() == AssessmentLevelEnum.BELOW_EXPECTATION) {
                return false;
            }

            /* Update the assessment value. */
            updateAssessment(AssessmentLevelEnum.BELOW_EXPECTATION);
            updateExplanation(); // must be done after the assessment is updated
            return true;
        }

        scoringEventStarted();

        /* If the previous assessment is already below expectation, no
         * assessment change is possible. */
        if (prevAssessment == AssessmentLevelEnum.BELOW_EXPECTATION) {
            return false;
        }

        updateAssessment(AssessmentLevelEnum.BELOW_EXPECTATION);
        updateExplanation(); // must be done after the assessment is updated
        return true;
    }
    
    /**
     * A method that handles the case when the fire team is properly maintaining rate of fire.
     *
     * @param newAssessmentLevel the new assessment level to set.  Should be At or Above Expectation.
     * @return True if this results in a change in the assessment
     *         value.
     */
    private boolean handleSuccess(AssessmentLevelEnum newAssessmentLevel) {
        if (logger.isDebugEnabled()) {
            logger.debug("handleSuccess()");
        }
        
        final AssessmentLevelEnum prevAssessment = getAssessment();        
        scoringEventEnded();

        /* If the previous assessment is already the new assessment, no further
         * action is needed (i.e. don't send the same good assessment). */
        if (prevAssessment.equals(newAssessmentLevel)) {
            return false;
        }

        updateAssessment(newAssessmentLevel);
        updateExplanation(); // must be done after the assessment is updated
        return true;
    }

    @Override
    public boolean handleTrainingAppGameState(Message message) {

        if (logger.isDebugEnabled()) {
            logger.debug("handleTrainingAppGameState(" + message + ")");
        }

        final MessageTypeEnum msgType = message.getMessageType();

        if (MessageTypeEnum.WEAPON_FIRE.equals(msgType)) {
            
            WeaponFire weaponFire = (WeaponFire)message.getPayload();
            
            EntityIdentifier firingEntityId = weaponFire.getFiringEntityID();
            TeamMember<?> assessedTeamMember = isConditionAssessedTeamMember(firingEntityId);
            if(assessedTeamMember != null){
                // an assessed team member is firing 
                
                // collect shot times during the current assessment window
                synchronized(currShotTimes){
                    currShotTimes.add(message.getTimeStamp());
                }
            }
        }
        
        return false;
    }

    @Override
    public ConditionDescription getDescription() {
        return description;
    }

    @Override
    public List<MessageTypeEnum> getSimulationInterests() {
        return simulationInterests;
    }

    @Override
    public boolean canComplete() {
        return false;
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
     * Notify the parent concept to this condition that the condition has a new assessment
     * outside the handle training app game state method call
     */
    private void sendAsynchAssessmentNotification(){
        sendAssessmentEvent();
    }    
    
    /**
     * Returns the amount of time not in a shot window before the first shot and after the last shot
     * in the current assessment window.  Also takes into account any shot window from the previous assessment
     * window that leaks into this assessment window.
     * @param firstShotLeftMost the left most time of the first shot in the current assessment window.
     * @param lastShotRightMost the right most time of the last shot in the current assessment window
     * @param windowStart the time at which this assessment window begins
     * @param timeNow the time at which this assessment window ends
     * @return the dead space time found before the first shot and after the last shot.  Will be >= 0.
     */
    private long getDeadSpaceTimeOnEdges(long firstShotLeftMost, long lastShotRightMost, long windowStart, long timeNow){
        
        long edgeDeadSpaceTimeMS = 0;
         
        if(prevRightShotTime == 0){
            // the last shot from the previous assessment window didn't cross over into this assessment window
            
            // add dead space to the left of this shot
            long leftOfShotDeadSpaceMS = firstShotLeftMost - windowStart;
            if(leftOfShotDeadSpaceMS > 0){
                edgeDeadSpaceTimeMS += leftOfShotDeadSpaceMS;
            }
        }else{
            // the last shot from the previous assessment window DID cross over into this assessment window
            
            // add dead space to the left of this shot, using the last shot as the starting point
            long leftOfShotDeadSpaceMS = firstShotLeftMost - prevRightShotTime;
            if(leftOfShotDeadSpaceMS > 0){
                edgeDeadSpaceTimeMS += leftOfShotDeadSpaceMS;
            }
        }
        
        // add dead space to the right of this shot
        // 
        long rightOfShotDeadSpaceMS = timeNow - lastShotRightMost;
        if(rightOfShotDeadSpaceMS > 0){
            // will be negative if the right of the shot window was outside of the right time for this assessment window
            edgeDeadSpaceTimeMS += rightOfShotDeadSpaceMS;
            prevRightShotTime = 0; //reset
        }else{
            // save the right time for this shot because it goes into the next assessment window
            prevRightShotTime = lastShotRightMost;
        }
        
        return edgeDeadSpaceTimeMS;
    }

    @Override
    public String toString() {
        return new StringBuilder("[FireTeamRateOfFireCondition: ")
                .append("lastRateOfFireRatio = ").append(lastRateOfFireRatio)
                .append(", ").append(super.toString())
                .append(']').toString();
    }
    
    /**
     * This class is the timer task which runs at the appropriately scheduled time frame.  It will
     * assess the fire team's rate of fire over the current window using a rapid rate of fire analysis.
     *
     * @author mhoffman
     *
     */
    private class RapidAssessmentTimerTask extends TimerTask{
        
        @Override
        public void run() {

            if(logger.isInfoEnabled()){
                logger.info("fire team RAPID rate of fire assessment timer task fired.");
            }
                        
            boolean assessmentChanged = false;
            
            long timeNow = System.currentTimeMillis();
            long windowStart = timeNow - rapidFireIntervalMs; // use data from the last 'rapidFireIntervalMs' window of time (e.g. 30 seconds)
            
            try{
                
                List<Integer> shotsInEachSecond = new ArrayList<>();

                  synchronized(currShotTimes){ 
                      
                      Iterator<Long> currShotTimeItr = currShotTimes.iterator();
                      int shotSecIndex = 0;
                      int cnt = 0;
                      while(currShotTimeItr.hasNext()) {
                          
                          // the current shot epoch time being analyzed
                          long shot = currShotTimeItr.next();
                          
                          // the epoch time for the start of the current window of 1 second
                          long start = windowStart + 1000 * shotSecIndex;
                          // the epoch time for the end of the current window of 1 second
                          long end = start + 1000;
    
                          if(shot > start && shot <= end) {
                              // shot is part of current window 
                              cnt++;
                          }else if(shot > start) {
                              // shot is part of a future window
                              
                              // save current window's number of shots
                              shotsInEachSecond.add(cnt);
                              
                              // to change the start/end times on the next loop
                              shotSecIndex++;
                              
                              // find where this shot should be counted in the list of 1 second windows
                              while(true) {
                                  start = windowStart + 1000 * shotSecIndex;
                                  end = start + 1000;
                                  if(shot > start && shot <= end) {
                                      // found the window this shot is in
                                      
                                      // to account for this shot in the next loop
                                      cnt = 1;
                                      
                                      break;
                                  }else {
                                      shotSecIndex++;
                                      
                                      // this shot is not in the next window
                                      shotsInEachSecond.add(0);
                                  }
                              }
                             
                          }
    
                      }
                  
                  }
              
                  //
                  // now determine if there are minRapidFireMs worth of rapid fire
                  //
                  
                  // the shotsInEachSecond is indexed by second by second, determine how many indexes should be 
                  // included in each calculation
                  int numOfIndexesToInclude = minRapidFireMs / 1000;
                  if(numOfIndexesToInclude > 0) {
                  
                      // prevent out of bounds exception
                      int endStart = Math.min(numOfIndexesToInclude, shotsInEachSecond.size());
                      for(int start = 0, end = endStart; end < shotsInEachSecond.size(); start++, end++) {
                          
                          int shotsPerWindow = 0;
                          for(int index = start; index < end; index++) {
                              shotsPerWindow += shotsInEachSecond.get(index);
                          }
                          
                          if(shotsPerWindow > highestRapidFireRatePerInterval) {
                              highestRapidFireRatePerInterval = shotsPerWindow;
                          }
                          
                          // determine if shots in this window meet or exceed the required rounds per window
                          if(shotsPerWindow >= rapidFireRatePerInterval) {
                              assessmentChanged = handleSuccess(AssessmentLevelEnum.AT_EXPECTATION);
                          }
                      }
                  }
                  
                  if(!assessmentChanged) {
                      assessmentChanged = handleViolation();
                  }
                 
                if(assessmentChanged){
                    sendAsynchAssessmentNotification();
                }
            
            }catch(Throwable t){
                logger.error("An error occurred when trying to calculate the ratio.", t);
            }
            
            // update flag that the assessment has completed for rapid fire portion of this condition
            hasFinishedRapidFireAssessment = true;
            
            // now schedule the sustained rate of fire assessment timer task
            TimerTask timerTask = new SustainedAssessmentTimerTask();

            // when to fire the first assessment and subsequent assessments
            assessmentTimer.schedule(timerTask, firstAssessmentMs, subsequentAssessmentMs);
        }
    }
    
    /**
     * This class is the timer task which runs at the appropriately scheduled time frame.  It will
     * assess the fire team's rate of fire over the current window using a sustained rate of fire analysis.
     *
     * @author mhoffman
     *
     */
    private class SustainedAssessmentTimerTask extends TimerTask{
        
        @Override
        public void run() {

            if(logger.isInfoEnabled()){
                logger.info("fire team SUSTAINED rate of fire assessment timer task fired.");
            }

            // calculate talking the guns ratio
            double calculatedRatio = 0;
                        
            boolean assessmentChanged = false;
            
            // calculate dead space time
            double totalDeadSpaceTimeMS = 0;
            long timeNow = System.currentTimeMillis();
            long windowStart = timeNow - firstAssessmentMs; // use data from the last 'firstAssessmentMs' window of time (e.g. 30 seconds)
            long assessmentDuration = firstAssessmentMs; // should always be 'firstAssessmentMs' window of time (e.g. 30 seconds)
            List<Long> expandedShotTimes = new ArrayList<>();
            
            try{
            
                synchronized(currShotTimes){        
                    
                    // convert instantaneous shot times into a duration over 2 * ALPHA
                    // Implemented based on the corresponding talking the guns research findings
                    Iterator<Long> currShotTimeItr = currShotTimes.iterator();
                    while(currShotTimeItr.hasNext()) {
                        
                        Long shotTime = currShotTimeItr.next();
                        if(shotTime == null) {
                            // remove this bad shot time object
                            currShotTimeItr.remove();
                            continue;
                        }else if(shotTime < windowStart) {
                            // remove this shot that is before the current assessment window
                            currShotTimeItr.remove();
                            continue;
                        }
                        
                        expandedShotTimes.add(shotTime - ALPHA);
                        expandedShotTimes.add(shotTime + ALPHA);  
                    }
                    
                }
                    
                if(expandedShotTimes.isEmpty()){
                    totalDeadSpaceTimeMS = assessmentDuration;
                    if(logger.isDebugEnabled()){
                        logger.debug("There were no shots during the window of "+assessmentDuration+" ms.");
                    }
                }else if(expandedShotTimes.size() == 2){
                    // only 1 shot, just calculate time before and after the shot window
                    
                    totalDeadSpaceTimeMS = getDeadSpaceTimeOnEdges(expandedShotTimes.get(0), expandedShotTimes.get(1), windowStart, timeNow);    
                    
                    if(logger.isDebugEnabled()){
                        logger.debug("There was only 1 shot during the window of "+assessmentDuration+" ms.");
                    }
                    
                }else{
                    // 2 or more shots, meaning at least 4 entries in the expandedShotTimes
                    
                    List<Long> toRemove = new ArrayList<>();
                    
                    // remove overlapping shots to make a single window for those overlapping shots
                    // start Left on the 2nd entry - cause the first entry will be checked in getDeadSpaceTimeOnEdges.
                    // start Right on the 3rd entry
                    // 
                    for(int left = 1, right = 2; right < expandedShotTimes.size(); left++, right++){
                        
                        Long shotTimeLeft = expandedShotTimes.get(left);
                        Long shotTimeRight = expandedShotTimes.get(right);
                        
                        if(shotTimeLeft > shotTimeRight){
                            // overlapping shots
                            toRemove.add(shotTimeLeft);
                            toRemove.add(shotTimeRight);
                        }
                    }
                    
                    expandedShotTimes.removeAll(toRemove);
                    
                    // now calculate dead space by looking at 
                    // 1. the beginning (before 0 index), 
                    // 2. between odd->even indexes since 0 based (e.g. 1 and 2),
                    // 3. and after the last (after size - 1 index)
                    
                    // edge of shots in this window 
                    // handles case 1 and 3 above.
                    totalDeadSpaceTimeMS = getDeadSpaceTimeOnEdges(expandedShotTimes.get(0), expandedShotTimes.get(expandedShotTimes.size()-1), windowStart, timeNow); 
                    
                    for(int left = 1, right = 2; right < expandedShotTimes.size(); left = left + 2, right = right + 2){
                        
                        long deadSpace = expandedShotTimes.get(right) - expandedShotTimes.get(left);
                        if(deadSpace > 0){
                            // the values should be in ascending numerical order starting at index 0, therefore deadSpace should never be negative.  But just in case.
                            totalDeadSpaceTimeMS+= deadSpace;
                        }
                    }
                    
                    if(logger.isDebugEnabled()){
                        logger.debug("There were a total of "+expandedShotTimes.size()+" shot groups during the window of "+assessmentDuration+" ms.");
                    }
                }
                
                if(totalDeadSpaceTimeMS > assessmentDuration){
                    // making sure a negative value greater than 1 is not set as calculatedRatio
                    logger.warn("The total dead space time "+totalDeadSpaceTimeMS+" is larger than the assessment duration of "+assessmentDuration+".  This shouldn't happen.");
                    return;
                }
                
                // calculate talking the guns ratio
                calculatedRatio = 1 - (totalDeadSpaceTimeMS / assessmentDuration);
                
                if(logger.isDebugEnabled()){
                    logger.debug("TG ratio is "+calculatedRatio+" using dead space time (ms) of "+totalDeadSpaceTimeMS+" and duration (ms) "+assessmentDuration);
                }
                               
                // update global variable before updating assessment and assessment explanation
                lastRateOfFireRatio = calculatedRatio;
                    
                if(calculatedRatio < atExpectationLowerBound){
                    assessmentChanged = handleViolation();
                }else if(calculatedRatio <= atExpectationUpperBound){
                    assessmentChanged = handleSuccess(AssessmentLevelEnum.AT_EXPECTATION);
                }else{
                    assessmentChanged = handleSuccess(AssessmentLevelEnum.ABOVE_EXPECTATION);
                }
                 
                if(assessmentChanged){
                    sendAsynchAssessmentNotification();
                }
            
            }catch(Throwable t){
                logger.error("An error occurred when trying to calculate the ratio.", t);
            }

        }

        @Override
        public String toString(){
            StringBuilder sb = new StringBuilder("[FireTeamRateOfFireCondition-AssessmentTimerTask: ");
            sb.append("]");

            return sb.toString();
        }
    }
}
