/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Controls that learners can use to influence an active scenario
 *
 * @author nroberts
 */
public class ScenarioControls implements IsSerializable {

    /** whether or not the learner should be able to manually stop the scenario through the tutoring interface */
    private boolean enableManualStop;
    
    /**
     * Default Constructor
     *
     * Required by IsSerializable to exist
     */
    private ScenarioControls() {
    }

    /**
     * Constructor
     *
     * @param enableManualStop whether or not the learner should be able to manually stop the scenario through the tutoring interface
     */
    public ScenarioControls(boolean enableManualStop) {
        this();

        this.enableManualStop = enableManualStop;
    }

    /**
     * Gets whether or not the learner should be able to manually stop the scenario through the tutoring interface
     * 
     * @return whether or not the learner should be able to manually stop the scenario through the tutoring interface
     */
    public boolean isManualStopEnabled() {
        return enableManualStop;
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[ScenarioControls: ");
        sb.append("enableManualStop = ").append(enableManualStop);
        sb.append("]");
        return sb.toString();
    }
}
