/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events;

import java.io.Serializable;

import mil.arl.gift.common.gwt.client.util.ScenarioElementUtil;

/**
 * An event to signal when a Jump To button has been pressed
 * 
 * @author cpadilla
 */
public class JumpToEvent extends ScenarioEditorEvent {

    /**
     * The scenario object to jump to
     */
    private Serializable parentScenarioObject;
    
    /**
     * The scenario object within the parent to select
     * Optional.
     */
    private Serializable childScenarioObject;
    
    /** The option to pin the current tab on jump */
    private boolean pinTabOnJump = false;

    /**
     * Constructor. The intent of this constructor is to "clear" the current selection. To perform
     * an actual jump use {@link #JumpToEvent(Serializable)}.
     */
    public JumpToEvent() {
    }
    
    /**
     * Constructor
     * 
     * @param parentScenarioObject the scenario object destination to jump to. Can't be null.
     */
    public JumpToEvent(Serializable parentScenarioObject) {
        if (parentScenarioObject == null) {
            throw new IllegalArgumentException("The parameter 'scenarioObject' cannot be null.");
        } else if (!ScenarioElementUtil.isObjectAnAssessmentObject(parentScenarioObject)) {
            throw new IllegalArgumentException("The parameter 'scenarioObject' must be an assessment object.");
        }

        this.parentScenarioObject = parentScenarioObject;
    }

    /**
     * Get the scenario object that the jump is navigating to
     * 
     * @return the scenario object. Can't be null.
     */
    public Serializable getScenarioObject() {
        return parentScenarioObject;
    }
    
    /**
     * Set the child scenario object of the scenario object being jumped to.  This is optional and
     * is used to open a subcomponent on the parent scenario object editor.
     * 
     * @param childScenarioObject can be null.
     */
    public void setChildScenarioObject(Serializable childScenarioObject){
        this.childScenarioObject = childScenarioObject;
    }
    
    /**
     * Get the child scenario object that the jump is navigating too after navigating
     * to the parent scenario object
     * @return can be null
     */
    public Serializable getChildScenarioObject(){
        return childScenarioObject;
    }
    
    /**
     * The option to pin the current tab on jump
     * 
     * @return true to pin the current tab on jump; false otherwise.
     */
    public boolean isPinTabOnJump() {
        return pinTabOnJump;
    }

    /**
     * The option to pin the current tab on jump
     * 
     * @param pinTabOnJump true to pin the current tab on jump; false otherwise.
     */
    public void setPinTabOnJump(boolean pinTabOnJump) {
        this.pinTabOnJump = pinTabOnJump;
    }

    @Override
    public String toString() {
        return new StringBuilder("[JumpToEvent: ")
                .append("scenarioObject = ").append(getScenarioObject())
                .append(", childScenarioObject = ").append(getChildScenarioObject())
                .append(", pinTabOnJump = ").append(isPinTabOnJump())
                .append("]").toString();
    }
}
