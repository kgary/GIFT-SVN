/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.ta.request;

import mil.arl.gift.common.util.StrategyUtil;

/**
 * This class is used to control the environment by setting it to a specific value.
 * 
 * @author jleonard
 */
public class EnvironmentControl {

    /** The type of environment to control display */
    private generated.dkf.EnvironmentAdaptation environmentAdaptation = null;
    
    /** the stress value for this strategy, optional or between {@link #StrategyUtil.MIN_STRESS} and {@link #StrategyUtil.MAX_STRESS} */
    private Double stress;

    /**
     * Class constructor
     *
     * @param environmentType The environment to control
     */
    public EnvironmentControl(generated.dkf.EnvironmentAdaptation environmentAdaptation) {
        this.environmentAdaptation = environmentAdaptation;
    }

    /**
     * Returns the type of environment that is to be controlled
     * 
     * @return The environment type
     */
    public generated.dkf.EnvironmentAdaptation getEnvironmentStatusType() {
        return environmentAdaptation;
    }
    
    /**
     * Return the stress value for this strategy.  The value is normally set in the DKF.
     * @return null if not set, or a value between {@link #TaskScoreNode.MIN_STRESS} and {@link #TaskScoreNode.MAX_STRESS}.
     */
    public Double getStress() {
        return stress;
    }

    /**
     * Set the stress value for this strategy.  The value is normally set in the DKF.
     * 
     * @param stress can be null to indicate the value is not set, otherwise a value between {@link #StrategyUtil.MIN_STRESS} 
     * and {@link #StrategyUtil.MAX_STRESS} is used.  If outside of those bounds the value will be changed to {@link #StrategyUtil.MIN_STRESS} 
     * or {@link #StrategyUtil.MAX_STRESS};
     */
    public void setStress(Double stress) {
        
        if(stress != null) {
            if(stress < StrategyUtil.MIN_STRESS) {
                stress = StrategyUtil.MIN_STRESS;
            }else if(stress > StrategyUtil.MAX_STRESS) {
                stress = StrategyUtil.MAX_STRESS;
            }
        }
        this.stress = stress;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder("[EnvironmentControl: ");
        sb.append("type = ").append(getEnvironmentStatusType());
        sb.append(", stress = ").append(stress);
        sb.append("]");

        return sb.toString();
    }
}
