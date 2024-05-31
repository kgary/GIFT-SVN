/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.course.transition;

import mil.arl.gift.common.enums.AbstractEnum;
import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;

/**
 * This transition class if for enumerated learner state attributes (e.g. "Engagement") which can have a state (e.g. "low").
 * 
 * @author mhoffman
 *
 */
public class LearnerStateAttributeTransition extends AbstractTransition {

    /** the enumerated attribute name (e.g. Arousal) that has an associated state which can change */
    private LearnerStateAttributeNameEnum name;
    
    /** 
     * whether the last learner state analyzed contained the attribute this transition is looking for
     * This is needed because 'satisfied' is set during a learner state message analysis and then reset
     * when building the pedagogical requests.  Therefore we need to know whether a new 'satisfied' = true
     * is change in activation of this class or a continuation of the last activation in order to determine
     * whether this class should activate it's overall state transition.
     */
    private boolean hasActivated;
    
    /**
     * whether this transition has sibling transitions, i.e. other logical expressions in the same overall state transition.
     * This is important in order to manage when this transition is the only logical expression in the overall state transition
     * and therefore we don't want this being satisfied = true again and again, causing the overall state transition to
     * activate again and again while the learner state attribute remains constant.
     */
    private boolean hasSiblingTransitions;
    
    /**
     * Class constructor - set attributes
     * 
     * @param name - the enumerated attribute name (e.g. Arousal) that has an associated state which can change. Can't be null.
     * @param previous - the previous enumerated value for the transition 
     * @param current - the current enumerated value for this transition
     * @param label - optional label that is paired with the learnerAttributeKey, this is usually a course concept.  
     * E.g. 'Knowledge' learner attribute with 'suppress OPFOR with well-aimed fire' course concept key.
     * @param hasSiblingTransitions whether this transition has sibling transitions, i.e. other logical expressions in the same overall state transition.
     * This is important in order to manage when this transition is the only logical expression in the overall state transition
     * and therefore we don't want this being satisfied = true again and again, causing the overall state transition to
     * activate again and again while the learner state attribute remains constant.
     */
    public LearnerStateAttributeTransition(LearnerStateAttributeNameEnum name, 
            AbstractEnum previous, AbstractEnum current, String label, boolean hasSiblingTransitions){
        super(previous, current, name.getName(), label);
        this.name = name;
        
        this.hasSiblingTransitions = hasSiblingTransitions;
    }
    
    /**
     * Return the enumerated attribute name (e.g. Arousal) that has an associated state which can change
     * 
     * @return won't be null
     */
    public LearnerStateAttributeNameEnum getAttributeName(){
        return name;
    }
    
    /**
     * Over-ridding to look for current state of a learner attribute and NOT a transition from one state to another
     */
    @Override
    public void shouldTransition(AbstractEnum previous, long previousTimestamp, AbstractEnum current, long currentTimestamp){
        
        if(satisfied) {
            // short-circuit - this transition was already satisfied using the current learner state object and has yet
            //                 to be used when collecting instructional strategy requests.
            return;
        }else if(this.goalPrevious != null) {
            // looking for a change from one state value to another state value, which this method doesn't handle
            // but AbstractTransition does.
            super.shouldTransition(previous, previousTimestamp, current, currentTimestamp);
            return;
        }
        
        satisfied = this.goalCurrent == current;
        
        if(satisfied) {
            // the transition state is satisfied
            
            if(hasActivated) {
                // the last check on this transition was satisfied, so nothing has changed.
                
                if(!hasSiblingTransitions) {
                    // this transition has NO siblings, i.e. there are NO other expressions that will be checked 
                    // for the overall state transition to activate.  Here this is used to prevent this transition
                    // type from firing repeatedly when nothing else will prevent the overall state transition from activating

                    // over-ride the satisfied so this transition isn't fired repeatedly when nothing has actually changed
                    satisfied = false;
                }

            }else {
                // the last check on this transition was NOT satisfied, so something has changed.
                // set the flag to prevent this from firing repeatedly in future checks
                hasActivated = true;
            }
        }else {
            // the transition is not satisfied in this current check
            
            if(hasActivated) {
                // a previous check deemed this transition satisfied, but now it isn't.
                // reset the flag to allow the transition to be satisfied, again, in a future check
                hasActivated = false;
            }
        }
    }
    
    @Override
    protected boolean shouldIncludePreExistingState(){
        return true;
    }
        
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[LearnerStateAttributeTransition: ");
        sb.append(super.toString());
        sb.append(", attribute = ").append(name);
        sb.append("]");
     
        return sb.toString();
    }
    
}
