/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

import java.util.UUID;

import mil.arl.gift.common.enums.DeploymentModeEnum;
import mil.arl.gift.common.io.CommonProperties;
import mil.arl.gift.common.io.Constants;


/**
 * Represents a record in the 'course' table of the database.  This class can be used on the gwt client
 * code as well as server code.
 * 
 * @author nblomberg
 *
 */
public class CourseRecord {

    /** Identifier to indicate that any user can view the course record. */
    public static final String PUBLIC_OWNER = "*";
   
    /** The course UUID which is a unique identifier of a course.  This never changes. */
    private String courseId;

    /** The owner name of the course.  A '*' indicates that there is no owner (such as for public courses or in desktop mode). */
    private String ownerName;
   
    /** The relative path to the course.   This can change for example during a course rename. */
    private String coursePath;
    
    /** Indicates if the course has been deleted by the user. */
    private Boolean isDeleted;
    

    /** 
     * Constructor - needed for gwt serialization
     */
    private CourseRecord() {
        
    }
    
    /**
     * Constructor
     * 
     * @param courseId The course UUID which is a unique identifier of a course.
     * @param coursePath The source course path (relative path) to the course.<br/>
     * e.g. mhoffman/4879 test new1 - Copy/4879 test new1 - Copy.course.xml
     * @param ownerName The owner name of the course. Can be {@value #PUBLIC_OWNER} when anyone can manage this course.
     */
    public CourseRecord(String courseId, String coursePath, String ownerName, Boolean isDeleted) { 
        this();
        setCourseId(courseId);
        setCoursePath(coursePath);
        setOwnerName(ownerName);
        setDeleted(isDeleted);
    }
    
    /**
     * Constructor - Generates a UUID for the course based on the path and owner name.
     * 
     * @param coursePath The source course path (relative path) to the course.<br/>
     * e.g. mhoffman/4879 test new1 - Copy/4879 test new1 - Copy.course.xml
     * @param ownerName The owner name of the course.  Can be {@value #PUBLIC_OWNER} when anyone can manage this course.
     */
    public CourseRecord(String coursePath, String ownerName) {
        this();
        String courseId = UUID.randomUUID().toString();
        setCourseId(courseId);
        setCoursePath(coursePath);
        setOwnerName(ownerName);
        setDeleted(false);
    }
    
    /**
     * Accessor to get the course id (UUID) for the course.
     * 
     * @return The course UUID which is a unique identifier of a course.
     */
    public String getCourseId() {
        return courseId;
    }

    /**
     * Accessor to set the course id (UUID) for the course.
     * 
     * @param courseId The course UUID which is a unique identifier of a course. 
     */
    private void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    /**
     * Accessor to get the owner name for the course.
     * 
     * @return The owner name of the course. Can be {@value #PUBLIC_OWNER} to indicate this course is 
     * owned by everyone.  That is the value for all courses in Desktop mode.
     */
    public String getOwnerName() {
        return ownerName;
    }
    
    /**
     * Return whether the owner of this course record is the public (i.e. anyone, everyone).
     * @return true if the owner is the public value {@link #PUBLIC_OWNER}.
     */
    public boolean isPublicOwner(){
        return PUBLIC_OWNER.equals(ownerName);
    }

    /**
     * Accessor to set the owner name for the course.
     * 
     * @param ownerName The owner name of the course.
     */
    private void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }
    
    /**
     * Accessor to get the course path for the course.
     * 
     * @return The relative path to the course.<br/>
     * e.g. mhoffman/4879 test new1 - Copy/4879 test new1 - Copy.course.xml
     */
    public String getCoursePath() {
        return coursePath;
    }

    /**
     * Accessor to set the course path for the course.
     * 
     * @param coursePath The relative path to the course.<br/>
     * e.g. mhoffman/4879 test new1 - Copy/4879 test new1 - Copy.course.xml
     */
    public void setCoursePath(String coursePath) {
        this.coursePath = coursePath;
    }
    
    /**
     * Converts a course relative path to the 'runtime' path if needed.  There
     * are certain cases where a relative path in server mode contains a "/" or "\" character
     * at the start of the path.  The course runtime expects that the leading slash is removed.
     * This utility method helps strip out the leading slash (in server mode) when needed.  If it
     * is not needed, then the string is unchanged.
     * 
     * This is used to help centralize the places where this path conversion logic may need to be done.
     * 
     * @param coursePath The relative course path to convert (if needed).
     * @return The modified relative path (if a leading slash needed to be removed).
     */
    static public String convertRuntimeCoursePath(String coursePath) {
     
        if (CommonProperties.getInstance().getDeploymentMode() == DeploymentModeEnum.SERVER) {
            if (coursePath.startsWith(Constants.FORWARD_SLASH) ||
                    coursePath.startsWith(Constants.BACKWARD_SLASH)) {
                
                // Remove beginning slash character (only for server mode).
                coursePath = coursePath.substring(1);
            }
        }
        

        return coursePath;
    }


    /**
     * Return whether this course was previously deleted but is still a record in the database.
     * @return the isDeleted value
     */
    public Boolean isDeleted() {
        return isDeleted;
    }

    /**
     * Set whether this course is deleted or not as understood by the database record.
     * @param isDeleted the value to set, true if this course has been deleted (e.g. the course folder no longer exists)
     */
    public void setDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[CourseRecord: ");
        sb.append("courseId = ").append(getCourseId());
        sb.append(", coursePath = ").append(getCoursePath());
        sb.append(", ownerName = ").append(getOwnerName());
        sb.append(", isDeleted = ").append(isDeleted());
        sb.append("]");
        return sb.toString();
    }
}
