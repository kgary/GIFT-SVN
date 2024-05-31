/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.ums.db.table;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import mil.arl.gift.common.CourseRecord;


/**
 * The DbCourse class represents a database record in the course table.  This class
 * maps the database object to the java object (via Hibernate).
 * 
 * @author nblomberg
 *
 */
@Entity
@Table(name = "course")
public class DbCourse {  
    
    /** The UUID for the course.  This id should never change and must be unique for each course. */
    private String courseId;
    /** The owner of the course.  A '*' indicates that the course is accessible for all users (such as in desktop mode or public courses). */
    private String ownerName;
    /** The relative path to the course.  This can change if the user renames the course. */
    private String coursePath;
   
    /** Indicates if the course associated with this UUID was deleted by the user. In this case, the course record
     *  remains in the database, primarily so that the UUID is not re-used when new courses are created.
     */
    private Boolean isDeleted;
    
    /**
     * Constructor - needed for serialization
     */
    public DbCourse() {
        
    }
    
    /** 
     * Constructor - Convert a CourseRecord object into a DbCourse object.
     * 
     * @param courseRecord The course record to convert to a db course object.
     */
    public DbCourse(CourseRecord courseRecord) {
     
       setCourseId(courseRecord.getCourseId());
       setCoursePath(courseRecord.getCoursePath());
       setOwnerName(courseRecord.getOwnerName());
       setDeleted(courseRecord.isDeleted());
    }
    

    @Id
    @Column(name = "course_id")
    public String getCourseId() {
        return courseId;
    }


    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }


    @Column(name = "course_path")
    public String getCoursePath() {
        return coursePath;
    }

    public void setCoursePath(String coursePath) {
        this.coursePath = coursePath;
    }
    
    @Column(name = "owner_name")
    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }
    
    /**
     * @return the isDeleted
     */
    @Column(name = "isDeleted")
    public Boolean isDeleted() {
        return isDeleted;
    }

    /**
     * @param isDeleted the isDeleted to set
     */
    public void setDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("DbCourse = [");
        sb.append(" courseId = ").append(getCourseId());
        sb.append(", coursePath = ").append(getCoursePath());
        sb.append(", ownerName = ").append(getOwnerName());
        sb.append(", isDeleted = ").append(isDeleted());
        sb.append("]");
        return sb.toString();

    }
}
