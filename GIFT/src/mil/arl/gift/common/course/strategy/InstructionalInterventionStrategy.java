/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.course.strategy;

import generated.dkf.Feedback;
import generated.dkf.InstructionalIntervention;
import mil.arl.gift.common.course.dkf.strategy.AbstractDKFStrategy;

/**
 * This class contains information on a instructional intervention strategy.
 *
 * @author mhoffman
 *
 */
public class InstructionalInterventionStrategy extends AbstractDKFStrategy {

    /** the Feedback instructional intervention tactic */
    private Feedback tactic;

    /**
     * Class constructor - set attributes
     *
     * @param name unique name of a strategy that this activity is mapped to
     * @param instructionalIntervention dkf.xsd generated class instance
     */
    public InstructionalInterventionStrategy(String name, InstructionalIntervention instructionalIntervention){
        super(name, instructionalIntervention.getStrategyHandler());
        this.tactic = instructionalIntervention.getFeedback();

        if(instructionalIntervention.getDelayAfterStrategy() != null && instructionalIntervention.getDelayAfterStrategy().getDuration() != null){
            this.setDelayAfterStrategy(instructionalIntervention.getDelayAfterStrategy().getDuration().floatValue());
        }
    }

    /**
     * Return the {@link Feedback} for this instructional intervention
     *
     * @return Feedback the feedback for this instructional intervention
     */
    public Feedback getFeedback() {
        return tactic;
    }

    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[InstructionalInterventionStrategy: ");
        sb.append(super.toString());
        sb.append(", tactic = ").append(tactic);
        sb.append("]");

        return sb.toString();
    }
}
