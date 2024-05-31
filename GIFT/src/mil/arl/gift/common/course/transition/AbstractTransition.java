/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.course.transition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.enums.AbstractEnum;

/**
 * This is the base class for the transition classes. It contains the previous and current state values that
 * describe a possible transition.   
 * 
 * @author mhoffman
 *
 */
public abstract class AbstractTransition {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(AbstractTransition.class);
    private static boolean isDebug = logger.isDebugEnabled();
    
    /** non-unique name for this transition. */
    private final String transitionName;
            
    /** the previous enumerated value for the transition */
    protected final AbstractEnum goalPrevious;
    
    /** the current enumerated value for this transition */
    protected final AbstractEnum goalCurrent;
    
    /** 
     * the previously received current value for this transition.  
     * This is used to determine that the previously received current value for this transition is the
     * same as the latest received previous value which would then be used to determine if a transition (i.e. change)
     * in value for this transition actually took place in cases where wild cards are used for previous or current.
     */
    private AbstractEnum lastValue;
    
    /**
     * optional label that is paired with the learnerAttributeKey, this is usually a course concept.  
     * E.g. 'Knowledge' learner attribute with 'suppress OPFOR with well-aimed fire' course concept key.
     */
    private final String label;
    
    /** whether the transition was satisfied with the last update and check performed */
    protected boolean satisfied;
    
    /** whether this transition was previously satisfied and has not yet received an
     * update that changed its satisfied state */
    private boolean isStillNotionallySatisfied;
    
    /** 
     * the time at which this transition was instantiated, used for checking pre-existing learner state values
     * against this transition's rules
     */
    private final Long startedTime = System.currentTimeMillis();
    
    /**
     * Class constructor - set attributes
     * 
     * @param previous - the previous enumerated value for the transition.  Can't be null.
     * @param current - the current enumerated value for this transition.  Can't be null.
     * @param transitionName - non-unique name for this transition.  Can't be null or empty string.
     * @param label - optional label that is paired with the learnerAttributeKey, this is usually a course concept.  
     * E.g. 'Knowledge' learner attribute with 'suppress OPFOR with well-aimed fire' course concept key.
     */
    public AbstractTransition(AbstractEnum previous, AbstractEnum current, String transitionName, String label){
        
        if(previous == null && current == null){
            throw new IllegalArgumentException("Both previous and current values can't be null.  Only one of them can be null.");
        }
        
        this.goalPrevious = previous;
        this.goalCurrent = current;
        
        if(transitionName == null || transitionName.isEmpty()){
            throw new IllegalArgumentException("The transition name can't be null.");
        }
        
        this.transitionName = transitionName;
        
        this.label = label;
    }
    
    /**
     * Return an optional label that is paired with the learnerAttributeKey, this is usually a course concept.  
     * E.g. 'Knowledge' learner attribute with 'suppress OPFOR with well-aimed fire' course concept key.
     * @return can be null
     */
    public String getLabel() {
        return label;
    }
    
    /**
     * Determine whether the provided states match the states of this transition.</br>
     * To check if the transition was satisfied use {@link #isSatisfied()}.</br>
     * To check if the transition was satisfied AND reset the state for the next check use {@link #isSatisfiedAndReset()}.</br>
     * Note: the previous value can be null if this is the first state value of a given attribute.
     * 
     * @param previous - the previous enumerated state.  Can be null if this is the first update.
     * @param previousTimestamp the time stamp at which the previous value was set.  Can be 0 if this is the first update.
     * @param current - the current enumerated state
     * @param currentTimestamp the time stamp at which the current value was set.
     */
    public void shouldTransition(AbstractEnum previous, long previousTimestamp, AbstractEnum current, long currentTimestamp){
        
        //Test cases to consider:
        //   i. previous = unknown, current = unknown, this.previous = unknown, this.current = null --- Desired = false
        //
        
        // Logic is as follows:
        // P =  previous, C = current, TP = transition previous, TC = transition current 
        // PTime = previous timestamp, CTime = current timestamp
        // 
        // when P != C                       [a change in state value has happened]
        //   [the state value changed has satisfied the transition authored when...]
        //   i. if P = TP and C = TC   
        //  ii. if P = * and C = TC 
        // iii. if P = TP and C = *
        //  
        // when P = C and PTime != CTime     [the same state values has arrived again, as an update, not a repeat of the previous state]
        //   i, ii, iii above apply
        //
        
        /* Not satisfied until proven otherwise, UNLESS the previous and 
         * current states are the same AND this transition is already satisfied
         * 
         * Previously, this was always initialized to false, but this ended up causing
         * a bug captured in #5528, so the AND checks below were added to fix it */
        if(satisfied) {
            isStillNotionallySatisfied = true;
        }
        
        if(satisfied) {
            // short-circuit - this transition was already satisfied using the current learner state object and has yet
            //                 to be used when collecting instructional strategy requests.
            return;
        }
        
        satisfied = false;
        
        if(this.goalPrevious == null){
            //CASE ii. don't care about the previous value, it can be anything
            
            //check that the last value received matches the incoming previous, otherwise we missed
            //a state update and I don't know how to recover from that right now.
            if(this.lastValue == previous){
                
                //the current value must be different than the previous OR 
                //a new timestamp is provided - to satisfy this transition OR
                //the current timestamp is older than this transition 
                //    -> meaning the transition doesn't care about the previous state only the current state and 
                //       the current state was established before this transition was instantiated, therefore this 
                //       transition will always be satisfied (?)
                if(previous != current || 
                        (previousTimestamp != 0 && previousTimestamp != currentTimestamp) ||
                        (startedTime > currentTimestamp && shouldIncludePreExistingState())){
                    
                    satisfied = this.goalCurrent == current;
                    isStillNotionallySatisfied = false;
                    
                    if(isDebug && satisfied){
                        logger.debug(transitionName + " transition was satisfied because: 'the previous value is not important and the current value of "+current+" matches the goal current value'.");
                    }
                }
            }
        }else if(this.goalCurrent == null){
            //CASE iii. don't care about the current value, it can be anything
            
            //check that the previous and current are different OR a new timestamp is provided - to satisfy this transition
            if(previous != current || (previousTimestamp != 0 && previousTimestamp != currentTimestamp)){
                
                satisfied = this.goalPrevious == previous;
                isStillNotionallySatisfied = false;
                
                if(isDebug && satisfied){
                    logger.debug(transitionName + " transition was satisfied because: 'the current value is not important and the previous value of "+previous+" matches the goal previous value'.");
                }
            }
            
        }else{
            
            //CASE i. check that the previous and current are different OR a new timestamp is provided - to satisfy this transition
            if(previous != current || (previousTimestamp != 0 && previousTimestamp != currentTimestamp)){

                satisfied = this.goalCurrent == current && this.goalPrevious == previous;
                isStillNotionallySatisfied = false;
                
                if(isDebug && satisfied){
                    logger.debug(transitionName + " transition was satisfied because: 'the previous value of "+previous+" matches the goal previous value and the current value of "+current+" matches the goal current value'.");
                }
            }
        }        
        
        //update the last value received for the next time this is checked
        lastValue = current;
        
    }
    
    /**
     * Return whether or not this transition has been satisfied based on the last state update provided.</br>
     * Use {@link #isSatisfiedAndReset()} to get this value AND reset it for the next learner state check.
     * 
     * @return true iff the last state update satisfied the rules of this transition
     */
    public boolean isSatisfied(){
        return satisfied;
    }
    
    /**
     * Return whether or not this transition has been satisfied based on the last state update provided.</br>
     * This also resets the flag so it can be set on the next learner state check.</br>
     * Use {@link #isSatisfied()} if you want the value and do NOT want to reset the flag.
     * @return true iff the last state update satisfied the rules of this transition
     */
    public boolean isSatisfiedAndReset() {
        boolean beforeReset = satisfied;
        satisfied = false;
        return beforeReset;
    }
    
    /**
     * Whether or not this transition was previously satisfied and has not yet received an
     * update that changed its satisfied state.<br/><br/>
     * 
     * This is different than {@link #isSatisfied()} in the sense that {@link #isSatisfied()}
     * reflects a change in state while this method reflects a lack of change.
     * 
     * For example, if a transition checks if a button was pressed, then receiving an update
     * that presses the button would return true for {@link #isSatisfied()} but false for
     * this method.
     * 
     * @return whether the transition is still notionally satisfied
     */
    public boolean isStillNotionallySatisfied() {
        return isStillNotionallySatisfied;
    }
    
    /**
     * Whether or not this transition considers pre-existing state values as a transition of
     * state.  i.e. if a learner state attribute checked by this transition pre-dates this
     * transition, should that attribute's time stamp be ignored and therefore consider it
     * a new state value. </br>
     * E.g. initial learner state when starting a course sets Grit at High, this happens before a DKF in the course
     * E.g. a score survey sets the Grit at High, this happens before a DKF in the course
     * 
     * @return boolean default is false, can be over-ridden.
     */
    protected boolean shouldIncludePreExistingState(){
        return false;
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("name = ").append(transitionName);
        sb.append(", label = ").append(label);
        sb.append(", last value = ").append(lastValue);
        sb.append(", goal:previous = ").append(goalPrevious);
        sb.append(", goal:current = ").append(goalCurrent);  
        sb.append(", satisfied = ").append(satisfied);
        
        return sb.toString();
    }
}
