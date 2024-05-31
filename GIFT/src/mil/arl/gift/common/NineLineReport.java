/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

/**
 * This class represents the contents of a 9-line report.
 * 
 * @author mhoffman
 *
 */
public class NineLineReport extends AbstractReport {

    /**
     * Set attribute
     * 
     * @param learnerAction authored learner action information related to this tutor action.  Can be null if there
     * is no authored information to provide here.
     */
    public NineLineReport(generated.dkf.LearnerAction learnerAction){
        super(learnerAction);
    }

    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[NineLineReport: ");
        sb.append(super.toString());
        sb.append("]");

        return sb.toString();
    }
}
