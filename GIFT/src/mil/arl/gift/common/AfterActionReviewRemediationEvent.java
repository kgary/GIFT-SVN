/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

import java.util.ArrayList;
import java.util.List;

/**
 * This class contains information about a Remediation event to display in an AAR.
 * 
 * @author mhoffman
 *
 */
public class AfterActionReviewRemediationEvent extends
        AbstractAfterActionReviewEvent {

    /** collection of course concepts needing remediation */
    private List<String> remediationInfo;
    
    /**
     * Build an empty remediation.
     * 
     * @param courseObjectName the unique name of the course object that created this event to be reviewed.
     */
    public AfterActionReviewRemediationEvent(String courseObjectName){
        super(courseObjectName);
        
        remediationInfo = new ArrayList<>();
    }
    
    /**
     * Use the remediation information provided.
     * 
     * @param courseObjectName the unique name of the course object that created this event to be reviewed.
     * @param remediationInfoList collection of information about the remediation event
     */
    public AfterActionReviewRemediationEvent(String courseObjectName, List<String> remediationInfoList){
        super(courseObjectName);
        
        if(remediationInfoList == null || remediationInfoList.isEmpty()){
            throw new IllegalArgumentException("The remediation info list can't be null or empty.");
        }
        
        this.remediationInfo = remediationInfoList;
    }
    
    /**
     * Add information about a concept needing remediation.
     * 
     * @param information about the remediation needed as part of this AAR event
     */
    public void addRemediationInfo(String information){
        remediationInfo.add(information);
    }
    
    /**
     * Add information about a concept needing remediation.
     * 
     * @param informationCollection one or more entries containing information about the remediation needed as part of this AAR event
     */
    public void addRemediationInfo(List<String> informationCollection){
        remediationInfo.addAll(informationCollection);
    }
    
    /**
     * Return the remediation information to present in an AAR event.
     * Currently this is a collection of messages about the concepts needing remediation.
     * 
     * @return the remediation information, won't be null or empty.
     */
    public List<String> getRemediationInfo(){
        return remediationInfo;
    }
    
    @Override
    public String toString(){
        
        StringBuilder sb = new StringBuilder();
        sb.append("[AfterActionReviewRemediationEvent: ");
        sb.append(super.toString());
        
        sb.append(", remediationInfo = {");
        for(String info : remediationInfo){
            sb.append(" ").append(info).append(",");
        }
        sb.append("}");
        sb.append("]");
        return sb.toString();
    }
}
