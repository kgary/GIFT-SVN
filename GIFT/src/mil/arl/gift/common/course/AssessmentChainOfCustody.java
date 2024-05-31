/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.course;

import mil.arl.gift.common.util.StringUtils;

/**
 * Wrapper around attributes used to unique identify an assessment session.
 * 
 * @author mhoffman
 *
 */
public class AssessmentChainOfCustody {

    /** the user id of the user that hosted the original live session where the course record originated. */
    private final int userId;
    
    /** the domain session id of the hosted live session where the course record originated. */
    private final int domainSessionId;
    
    /**
     * the absolute path to the parent folder of domain session output, 
     * e.g. E:\work\trunk\GIFT\output\domainSessions\domainSession1347_uId1
     */
    private final String sessionOutputFolder;
    
    /**
     * can be null for legacy logs that didn't have the dkf file provided, otherwise this will
     * be the dkf file name used to assess the session found in the log, e.g. vbs2.TSP 07-GFT-0137 ClearBldg.dkf.xml
     */
    private final String dkfFileName;
    
    /** the session log file name, e.g. domainSession1347_uId1_2021-11-10_09-29-59.protobuf.bin */
    private final String sessionLogFilename;
    
    /**
     * 
     * @param userId the user id of the user that hosted the original live session where the course record originated.
     * @param domainSessionId the domain session id of the hosted live session where the course record originated.
     * @param sessionOutputFolder the absolute path to the parent folder of domain session output, 
     * e.g. E:\work\trunk\GIFT\output\domainSessions\domainSession1347_uId1
     * @param dkfFileName can be null for legacy logs that didn't have the dkf file provided, otherwise this will
     * be the dkf file name used to assess the session found in the log, e.g. vbs2.TSP 07-GFT-0137 ClearBldg.dkf.xml
     * @param sessionLogFilename the session log file name, e.g. domainSession1347_uId1_2021-11-10_09-29-59.protobuf.bin
     */
    public AssessmentChainOfCustody(int userId, int domainSessionId, 
            String sessionOutputFolder, String dkfFileName, String sessionLogFilename){
        
        if(userId < 1){
            throw new IllegalArgumentException("The user id is not greater than 0 but is "+userId);
        }else if(domainSessionId < 1){
            throw new IllegalArgumentException("The domain id is not greater than 0 but is "+domainSessionId);
        }else if(StringUtils.isBlank(sessionOutputFolder)){
            throw new IllegalArgumentException("The session output folder is blank");
        }else if(StringUtils.isBlank(dkfFileName)){
            throw new IllegalArgumentException("The dkf file name is blank");
        }else if(StringUtils.isBlank(sessionLogFilename)){
            throw new IllegalArgumentException("The session log file name is blank");
        }
        
        this.userId = userId;
        this.domainSessionId = domainSessionId;
        this.sessionOutputFolder = sessionOutputFolder;
        this.dkfFileName = dkfFileName;
        this.sessionLogFilename = sessionLogFilename;
    }

    /**
     * Get the unique GIFT user id of the host of an assessment.
     * @return the user id of the user that hosted the original live session where the course record originated.
     */
    public int getUserid() {
        return userId;
    }

    /**
     * Get the unique GIFT domain session id of the host of an assessment.
     * @return the domain session id of the hosted live session where the course record originated.
     */
    public int getDomainsessionid() {
        return domainSessionId;
    }

    /**
     * Get the path to the folder where the assessment session output is saved.
     * @return the absolute path to the parent folder of domain session output, 
     * e.g. E:\work\trunk\GIFT\output\domainSessions\domainSession1347_uId1
     */
    public String getSessionoutputfolder() {
        return sessionOutputFolder;
    }

    /**
     * Get the DKF file name that was used to configure the assessment engine.
     * @return can be null for legacy logs that didn't have the dkf file provided, otherwise this will
     * be the dkf file name used to assess the session found in the log, e.g. vbs2.TSP 07-GFT-0137 ClearBldg.dkf.xml
     */
    public String getDkffilename() {
        return dkfFileName;
    }

    /**
     * Get the GIFT domain session log file name that contains assessments for a session.
     * @return the session log file name, e.g. domainSession1347_uId1_2021-11-10_09-29-59.protobuf.bin
     */
    public String getSessionlogfilename() {
        return sessionLogFilename;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dkfFileName == null) ? 0 : dkfFileName.hashCode());
        result = prime * result + domainSessionId;
        result = prime * result + ((sessionLogFilename == null) ? 0 : sessionLogFilename.hashCode());
        result = prime * result + ((sessionOutputFolder == null) ? 0 : sessionOutputFolder.hashCode());
        result = prime * result + userId;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof AssessmentChainOfCustody)) {
            return false;
        }
        AssessmentChainOfCustody other = (AssessmentChainOfCustody) obj;
        if (dkfFileName == null) {
            if (other.dkfFileName != null) {
                return false;
            }
        } else if (!dkfFileName.equals(other.dkfFileName)) {
            return false;
        }
        if (domainSessionId != other.domainSessionId) {
            return false;
        }
        if (sessionLogFilename == null) {
            if (other.sessionLogFilename != null) {
                return false;
            }
        } else if (!sessionLogFilename.equals(other.sessionLogFilename)) {
            return false;
        }
        if (sessionOutputFolder == null) {
            if (other.sessionOutputFolder != null) {
                return false;
            }
        } else if (!sessionOutputFolder.equals(other.sessionOutputFolder)) {
            return false;
        }
        if (userId != other.userId) {
            return false;
        }
        return true;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("[AssessmentChainOfCustody: ");
        sb.append("user id = ").append(userId);
        sb.append(", domain session id = ").append(domainSessionId);
        sb.append(", dkfFileName = ").append(dkfFileName);
        sb.append(", sessionLogFilename = ").append(sessionLogFilename);
        sb.append(", sessionOutputFolder = ").append(sessionOutputFolder);
        sb.append("]");
        return sb.toString();
    }  
}
