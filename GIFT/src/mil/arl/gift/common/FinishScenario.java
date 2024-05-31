/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

/**
 * This class represents the learner tutor action of manually finishing a scenario
 * 
 * @author nroberts
 */
public class FinishScenario extends AbstractLearnerTutorAction {

    /**
     * Default constructor
     */
    public FinishScenario(){
        super(null);
    }

    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[FinishScenario: ");
        sb.append(super.toString());
        sb.append("]");

        return sb.toString();
    }
}
