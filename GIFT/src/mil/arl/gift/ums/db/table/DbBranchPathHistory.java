/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.ums.db.table;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "branchpathhistory")
public class DbBranchPathHistory implements Serializable{

    private static final long serialVersionUID = 1L;

    private String courseId;

    private String experimentId;
    
    private int branchId;
    
    private int pathId;
    
    private int actualCnt;
    
    private int cnt;

    public DbBranchPathHistory() {}

    /**
     * unique id for a course among all courses in a single GIFT instance (not across GIFT instances)
     * @return unique id for a course
     */
    @Id
    @Column(name = "courseId_PK")
    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    /**
     * unique id for an experiment among all experiments across GIFT instances
     * @return the experimentId
     */
    @Id
    @Column(name = "experimentId_PK")
    public String getExperimentId() {
        return experimentId;
    }

    /**
     * Sets the experiment id
     * @param experimentId the experimentId to set
     */
    public void setExperimentId(String experimentId) {
        this.experimentId = experimentId;
    }

    /**
     * unique id for a branch (within a course, not across courses)
     * @return unique id for a branch. Will be greater than zero.
     */
    @Id
    @Column(name = "branchId_PK")
    public int getBranchId() {
        return branchId;
    }

    public void setBranchId(int branchId) {
        
        if(branchId <= 0){
            throw new IllegalArgumentException("The branch id must be greater than zero.");
        }
        
        this.branchId = branchId;
    }

    /**
     * unique id for a path (within a branch in a course, not across all branches in a course)
     * @return the unique id for a path. Will be greater than zero.
     */
    @Id
    @Column(name = "pathId_PK")
    public int getPathId() {
        return pathId;
    }

    public void setPathId(int pathId) {
        
        if(pathId <= 0){
            throw new IllegalArgumentException("The branch id must be greater than zero.");
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
    
    @Override
    public boolean equals(Object obj) {
        
        if (obj == null) {
            
            return false;
        }
        
        if (getClass() != obj.getClass()) {
            
            return false;
        }
        
        DbBranchPathHistory other = (DbBranchPathHistory) obj;
        
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
        sb.append("[DbBranchPathHistory:");
        sb.append(" courseId = ").append(getCourseId());
        sb.append(", branchId = ").append(getBranchId());
        sb.append(", pathId = ").append(getPathId());
        sb.append(", actualCnt = ").append(getActualCnt());
        sb.append(", cnt = ").append(getCnt());
        sb.append("]");

        return sb.toString();
    }
    
}
