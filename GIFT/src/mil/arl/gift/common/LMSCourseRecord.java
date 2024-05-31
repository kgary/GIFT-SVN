/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

import java.io.Serializable;
import java.util.Date;

import mil.arl.gift.common.course.CourseRecordRef;
import mil.arl.gift.common.score.GradedScoreNode;

/**
 * This class contains a single course record.
 *
 * @author mhoffman
 */
public class LMSCourseRecord implements Comparable<LMSCourseRecord>, Serializable {

    private static final long serialVersionUID = 1L;

    /** The reference of the course record. 
     * Can be null if {@link #LMSCourseRecord(String, GradedScoreNode, Date)} or {@link #LMSCourseRecord(int, String, GradedScoreNode, Date)} is used */
    private CourseRecordRef courseRecordRef;
    
    /** The name of the domain from which this record was generated */
    private String domainName;

    /** the root node of this record */
    private GradedScoreNode root;

    /** the date at which this record was created */
    private Date date;
    
    /** information about the LMS containing this record */
    private LMSConnectionInfo lmsConnectionInfo;
    
    /** 
     * (optional) an identifier used to group LMS course records together based on an event such as 
     * a learner taking a course (i.e. domain session id) 
     */
    private Integer giftEventId;

    /**
     * Constructor - Default - for gwt serialization.
     */
    @SuppressWarnings("unused")
    private LMSCourseRecord() {

    }
    
    /**
     * Set attributes - used when publishing a course record.
     * @param domainName The name of the domain option with which this record is associated.  Can be null.
     * @param root the root node of this record.  Can't be null.
     * @param date the date the grade was given.  Can't be null.
     */
    public LMSCourseRecord(String domainName, GradedScoreNode root, Date date) {
        this.domainName = domainName;
        setRoot(root);
        setDate(date);
    }
    
    /**
     * Set attributes - used when retrieving a course record by its reference.
     * @param courseRecordRef the reference to the course record.  Can't be null.
     * @param domainName The name of the domain option with which this record is associated.  Can be null.
     * @param root the root node of this record.  Can't be null.
     * @param date the date the grade was given.  Can't be null.
     */
    public LMSCourseRecord(CourseRecordRef courseRecordRef, String domainName, GradedScoreNode root, Date date) {
        this(domainName, root, date);
        
        if(courseRecordRef == null){
            throw new IllegalArgumentException("The course record ref is null");
        }
        
        this.courseRecordRef = courseRecordRef;

    }
    
    /**
     * Set class attributes.  
     * @deprecated Use {@link #LMSCourseRecord(CourseRecordRef, String, GradedScoreNode, Date)} instead.
     *
     * @param courseRecordId The ID of the course record
     * @param domainName - The name of the domain option with which this record is associated.  Can be null.
     * @param root - the root node of this record.  Can't be null.
     * @param date - the date the grade was given.  Can't be null.
     */
    @Deprecated
    public LMSCourseRecord(int courseRecordId, String domainName, GradedScoreNode root, Date date) {
        
        this.courseRecordRef = CourseRecordRef.buildCourseRecordRefFromInt(courseRecordId);
        
        this.domainName = domainName;
        setRoot(root);
        setDate(date);
    }
    
    /**
     * Creates a new instance copy of the provided {@link LMSCourseRecord}.  This will
     * also copy the {@link GradedScoreNode} inside.
     * @param original the {@link LMSCourseRecord} to copy
     * @return the new {@link LMSCourseRecord}
     */
    public static LMSCourseRecord deepCopy(LMSCourseRecord original){
        
        if(original == null){
            return null;
        }
        
        LMSCourseRecord copy = new LMSCourseRecord(CourseRecordRef.deepCopy(original.getCourseRecordRef()), 
                original.getDomainName(), GradedScoreNode.deepCopy(original.getRoot()), original.getDate());
        if(original.getLMSConnectionInfo() != null){
            copy.setLMSConnectionInfo(original.getLMSConnectionInfo());
        }
        if(original.getGIFTEventId() != null){
            copy.setGiftEventId(original.getGIFTEventId());
        }

        return copy;
    }

    /**
     * Return the identifier used to group LMS course records together based on an event such as 
     * a learner taking a course (i.e. domain session id).
     * 
     * @return can be null if never set
     */
    public Integer getGIFTEventId() {
        return giftEventId;
    }

    /**
     * Set the identifier used to group LMS course records together based on an event such as 
     * a learner taking a course (i.e. domain session id) 
     * 
     * @param giftEventId a identifier to use, e.g. domain session id
     */
    public void setGiftEventId(int giftEventId) {
        this.giftEventId = giftEventId;
    }
    
    /**
     * The reference to the course record(s).
     * @return the reference to the course record being tracked by this object.
     */
    public CourseRecordRef getCourseRecordRef(){
        return courseRecordRef;
    }
    
    /**
     * Gets the name of the domain from which this course was generated
     * 
     * @return The name of the domain.  Can be null.
     */
    public String getDomainName() {
        return domainName;
    }

    /**
     * Gets the root of the course record score tree.  This is normally the name of the course.
     * 
     * @return The root of the course record
     */
    public GradedScoreNode getRoot() {
        return root;
    }
    
    /**
     * Set the the root node of this record
     * @param root the root node of this record. Can't be null.
     */
    public void setRoot(GradedScoreNode root){
        
        if (root == null) {
            throw new IllegalArgumentException("The root node can't be null");
        }

        this.root = root;
    }

    /**
     * Gets the date the course was completed
     * 
     * @return The date the course was completed
     */
    public Date getDate() {
        return date;
    }
    
    /**
     * Set the date the course was completed
     * @param date the date the grade was given.  Can't be null.
     */
    private void setDate(Date date){
        
        if (date == null) {
            throw new IllegalArgumentException("The date the grade was given can't be null");
        }

        this.date = date;
    }
    
    /**
     * Set the LMS connection information for the lms containing this record.
     * 
     * @param lmsConnectionInfo  information about the LMS containing this record.  Can't be null.
     */
    public void setLMSConnectionInfo(LMSConnectionInfo lmsConnectionInfo){
        
        if(lmsConnectionInfo == null){
            throw new IllegalArgumentException("The connection info can't be null.");
        }
        
        this.lmsConnectionInfo = lmsConnectionInfo;
    }
    
    /**
     * Returns the LMS connection information for this record.
     * 
     * @return LMSConnectionInfo information about the LMS containing this record
     */ 
    public LMSConnectionInfo getLMSConnectionInfo(){
        return lmsConnectionInfo;
    }


    @Override
    public int compareTo(LMSCourseRecord otherCourseRecord) {
        return this.getDate().compareTo(otherCourseRecord.getDate());
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[LMSCourseRecord: ");
        sb.append("ref = ").append(getCourseRecordRef());
        sb.append(", domain = ").append(getDomainName());
        sb.append(", eventId = ").append(getGIFTEventId());
        sb.append(", LMS connection Info = ").append(getLMSConnectionInfo());
        sb.append(", root = ").append(getRoot());
        sb.append(", date = ").append(getDate());
        sb.append("]");

        return sb.toString();
    }

}
