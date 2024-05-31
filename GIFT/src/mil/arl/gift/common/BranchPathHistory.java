/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

import mil.arl.gift.common.util.StringUtils;

/**
 * Contains information about a branch path history which tracks the number
 * of learners that have entered a particular path in a course.
 * 
 * @author mhoffman
 *
 */
public class BranchPathHistory {

    /** The course id */
    private String courseId;
    
    /** The experiment id */
    private String experimentId;

    /** The name of the path selected for the learner to take. Can be null if no path has been selected yet */
    private String pathName;
    
    /** The branch id */
    private int branchId;
    
    /** The path id */
    private int pathId;
    
    /** The actual number of learners that have entered this path which can be different */
    private int actualCnt;
    
    /** The count used when determining which path of a branch the learner should enter */
    private int cnt;
    
    /** Whether to increment the history count of a path in the database */
    private boolean increment = false;

    /** Whether this update is to indicate a path is ending */
    private boolean pathEnd = false;
    
    /**
     * Constructor used to send the latest branch path history information.
     * 
     * @param courseId unique id for a course among all courses in a single GIFT instance (not across GIFT instances).  Can't be null or empty.
     * @param branchId unique id for a branch (within a course, not across courses).  Must be greater than zero.
     * @param pathId unique id for a path (within a branch in a course, not across all branches in a course). Must be greater than zero.
     * @param actualCnt the actual number of learners that have entered this path which can be different
     * than the count value because the count value can be manually changed by a researcher/instructor.  Must be a positive number.
     * @param cnt this number is used when determining which path of a branch the current 
     * learner should enter.  It could be equal to the the number of learners that have entered 
     * this path or it could have been manually changed by a researcher/instructor.  Must be a positive number.
     */
    public BranchPathHistory(String courseId, String experimentId, int branchId, int pathId, int actualCnt, int cnt){
        setCourseId(courseId);
        setExperimentId(experimentId);
        setBranchId(branchId);
        setPathId(pathId);
        setActualCnt(actualCnt);
        setCnt(cnt);
    }
    
    /**
     * Constructor used to send the latest branch path history information.
     * 
     * @param courseId unique id for a course among all courses in a single GIFT instance (not across GIFT instances).  Can't be null or empty.
     * @param experimentId the unique id of an experiment. Can't be null
     * @param branchId unique id for a branch (within a course, not across courses).  Must be greater than zero.
     * @param pathId unique id for a path (within a branch in a course, not across all branches in a course). Must be greater than zero.
     * @param actualCnt the actual number of learners that have entered this path which can be different
     * than the count value because the count value can be manually changed by a researcher/instructor.  Must be a positive number.
     * @param cnt this number is used when determining which path of a branch the current 
     * learner should enter.  It could be equal to the the number of learners that have entered 
     * this path or it could have been manually changed by a researcher/instructor.  Must be a positive number.
     * @param pathname the name of the path chosen
     * @param pathEnd whether the path is ending
     */
    public BranchPathHistory(String courseId, String experimentId, int branchId, int pathId, int actualCnt, int cnt, String pathName, boolean pathEnd){
        setCourseId(courseId);
        setExperimentId(experimentId);
        setBranchId(branchId);
        setPathId(pathId);
        setActualCnt(actualCnt);
        setCnt(cnt);
        setPathName(pathName);
        setPathEnding(pathEnd);
    }

    /**
     * Constructor used to request the latest branch path history information.
     * 
     * @param courseId unique id for a course among all courses in a single GIFT instance (not across GIFT instances).  Can't be null or empty.
     * @param experimentId the unique id of an experiment. Can be null
     * @param branchId unique id for a branch (within a course, not across courses).  Must be greater than zero.
     * @param pathId unique id for a path (within a branch in a course, not across all branches in a course). Must be greater than zero.
     */
    public BranchPathHistory(String courseId, String experimentId, int branchId, int pathId){
        setCourseId(courseId);
        setExperimentId(experimentId);
        setBranchId(branchId);
        setPathId(pathId);
    }

    /**
     * Constructor used to request the latest branch path history information.
     * 
     * @param courseId unique id for a course among all courses in a single GIFT instance (not across GIFT instances).  Can't be null or empty.
     * @param experimentId the unique id of an experiment. Can be null
     * @param branchId unique id for a branch (within a course, not across courses).  Must be greater than zero.
     * @param pathId unique id for a path (within a branch in a course, not across all branches in a course). Must be greater than zero.
     * @param name the name of the path chosen
     * @param pathEnd whether the path is ending
     */
    public BranchPathHistory(String domainSourceId, String experimentId, int intValue, int intValue2, String name, boolean pathEnd) {
        this(domainSourceId, experimentId, intValue, intValue2);
        setPathName(name);
        setPathEnding(pathEnd);
    }

    /**
     * Set the name of the path chosen
     * @param name the name of the path chosen
     */
    public void setPathName(String name) {
        pathName = name;
    }

    /**
     * Get the name of the path chosen
     * @return the pathname
     */
    public String getPathName() {
        return pathName;
    }

    /**
     * Set whether the path is ending
     * @param pathEnd whether the path is ending
     */
    public void setPathEnding(boolean pathEnd) {
        this.pathEnd = pathEnd;
    }

    /**
     * Get whether the path is ending
     * @return whether the path is ending
     */
    public boolean isPathEnding() {
        return this.pathEnd;
    }

    /**
     * unique id for a course among all courses in a single GIFT instance (not across GIFT instances)
     * @return unique id for a course
     */
    public String getCourseId() {
        return courseId;
    }

    private void setCourseId(String courseId) {
        
        if(StringUtils.isBlank(courseId)){
            throw new IllegalArgumentException("The course id can't be null or empty.");
        }
        
        this.courseId = courseId;
    }

    /**
     * unique id for a branch (within a course, not across courses)
     * @return unique id for a branch.  Will be greater than zero.
     */
    public int getBranchId() {
        return branchId;
    }

    private void setBranchId(int branchId) {
        
        if(branchId <= 0){
            throw new IllegalArgumentException("The branch id must be greater than zero.");
        }
        
        this.branchId = branchId;
    }

    /**
     * unique id for a path (within a branch in a course, not across all branches in a course)
     * @return the unique id for a path
     */
    public int getPathId() {
        return pathId;
    }

    public void setPathId(int pathId) {
        
        if(pathId <= 0){
            throw new IllegalArgumentException("The path id must be greater than zero.");
        }
        
        this.pathId = pathId;
    }

    /**
     * the actual number of learners that have entered this path which can be different
     * than the count value because the count value can be manually changed by a researcher/instructor.
     * 
     * @return the actual number of learners that have entered this path.  Will be positive.
     */
    public int getActualCnt() {
        return actualCnt;
    }

    public void setActualCnt(int actualCnt) {
        
        if(cnt < 0){
            throw new IllegalArgumentException("The actual count can't be negative.");
        }
        
        this.actualCnt = actualCnt;
    }

    /**
     * this number is used when determining which path of a branch the current 
     * learner should enter.  It could be equal to the the number of learners that have entered 
     * this path or it could have been manually changed by a researcher/instructor.
     * 
     * @return the number to use when determining which path of a branch the current learner should enter.  Will be positive.
     */
    public int getCnt() {
        return cnt;
    }

    public void setCnt(int cnt) {
        
        if(cnt < 0){
            throw new IllegalArgumentException("The count can't be negative.");
        }
        
        this.cnt = cnt;
    }
    
    /**
     * Whether this history entry is merely meant to increment the count in the database
     * as an indication that a learner is entering this path.  If true the actual count and count 
     * values will be ignored when updating the database entry.
     * 
     * @return flag used to determine if the database should increment the current count
     */
    public boolean shouldIncrement() {
        return increment;
    }

    public void setIncrement(boolean increment) {
        this.increment = increment;
    }

    /**
     * Gets the experimentId
     * 
     * @return the experimentId
     */
    public String getExperimentId() {
        return experimentId;
    }

    /**
     * Sets the experimentId. Can be null
     * 
     * @param experimentId the experimentId to set
     */
    public void setExperimentId(String experimentId) {
        this.experimentId = experimentId;
    }

    @Override
    public boolean equals(Object obj) {
        
        if (obj == null) {
            
            return false;
        }
        
        if (getClass() != obj.getClass()) {
            
            return false;
        }
        
        BranchPathHistory other = (BranchPathHistory) obj;
        
        if(this.getCourseId() != null && other.getCourseId() != null &&
                !this.getCourseId().equals(other.getCourseId())){
            return false;
        }
        
        if(this.getPathId() != other.getPathId()){
            return false;
        }
        
        if(this.getBranchId() != other.getBranchId()){
            return false;
        }
        
        if(this.getActualCnt() != other.getActualCnt()){
            return false;
        }
        
        if(this.getCnt() != other.getCnt()){
            return false;
        }
        
        return true;        
    }
    
    @Override
    public int hashCode() {
        
        int hash = 7;        
        hash = 89 * hash + cnt + actualCnt + branchId + pathId;
        
        if(courseId != null){
            hash += courseId.hashCode();
        }
        
        return hash;
    }
    
    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[BranchPathHistory:");
        sb.append(" courseId = ").append(getCourseId());
        sb.append(", branchId = ").append(getBranchId());
        sb.append(", pathId = ").append(getPathId());
        sb.append(", actualCnt = ").append(getActualCnt());
        sb.append(", cnt = ").append(getCnt());
        sb.append(", shouldIncrement = ").append(shouldIncrement());
        sb.append("]");

        return sb.toString();
    }
}
