/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.course;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import mil.arl.gift.common.io.DetailedException;

/**
 * Contains validation results for a course including the validation results for
 * each course object in the course, successful or not.
 * 
 * @author mhoffman
 *
 */
public class CourseValidationResults extends GIFTValidationResults {
    
    private static final long serialVersionUID = 1L;
    
    /** name of the course being validated */
    private String courseName;
    
    /**
     * contains the validation results of each course object in the course.
     * the size will match the number of course objects.
     */
    private List<CourseObjectValidationResults> courseObjectResults = new ArrayList<CourseObjectValidationResults>(0); 
    
    /**
     * Required for GWT - do not use!
     */
    public CourseValidationResults(){}
    
    /**
     * Set attribute
     * 
     * @param courseName name of the course being validated
     */
    public CourseValidationResults(String courseName){
        
        if(courseName == null || courseName.isEmpty()){
            throw new IllegalArgumentException("The course name can't be null or empty.");
        }
        
        this.courseName = courseName;
    }
    
    /**
     * Return the name of the course being validated
     * 
     * @return wont be null or empty string
     */
    public String getCourseName(){
        return courseName;
    }

    /**
     * Return the validation results of each course object in the course.
     * 
     * @return the list of course object validation results.  Won't be null.  The size will equal the number
     * of course objects found in the course XML object that was validated.
     */
    public List<CourseObjectValidationResults> getCourseObjectResults() {
        return courseObjectResults;
    }

    /**
     * Add a course object validation result.
     * 
     * @param courseObjectResult the validation result of a course object.  Can't be null.
     */
    public void addCourseObjectResults(CourseObjectValidationResults courseObjectResult) {
        
        if(courseObjectResult == null){
            throw new IllegalArgumentException("The course object result can't be null.");
        }

        this.courseObjectResults.add(courseObjectResult);
    }    

    /**
     * Add an important issue (see GIFTValidationResults.importantIssues) to the course object validation results.
     * 
     * @param issue the validation issue of a course object.  Can't be null.
     * @param index The index of the course object. Must be non negative.
     */
    public void addImportantIssue(DetailedException issue, int index) {
    	
        if(issue == null){
            throw new IllegalArgumentException("The issue can't be null.");
        }

        if(index < 0) {
        	throw new IllegalArgumentException("The course object index cannot be negative.");
        }
        
        if(index > this.courseObjectResults.size()) {
        	
        	CourseObjectValidationResults courseObjectResult = new CourseObjectValidationResults();
        	GIFTValidationResults results = new GIFTValidationResults();
        	results.addImportantIssue(issue);
        	courseObjectResult.setValidationResults(results);
        	this.courseObjectResults.add(courseObjectResult);	
        	
        } else {
        	
        	if(courseObjectResults.get(index).getValidationResults() == null) {
        		courseObjectResults.get(index).setValidationResults(new GIFTValidationResults());
        	}
        	
        	courseObjectResults.get(index).getValidationResults().addWarningIssue(issue);
        }
    }
    
    /**
     * Returns whether or not the GIFT Validation Results has critical issues. Ignores this class'
     * course objects.
     * 
     * @return true if there are critical issues that are not from course objects; false otherwise.
     */
    public boolean hasCriticalIssueIgnoreCourseObjects() {

        return super.hasCriticalIssue();
    }
    
    @Override
    public boolean hasCriticalIssue() {
        
        if(super.hasCriticalIssue()){
           return true; 
        }
        
        for(CourseObjectValidationResults coResult : courseObjectResults){
            
            if(coResult.getValidationResults() != null && coResult.getValidationResults().hasCriticalIssue()){
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Returns whether or not the GIFT Validation Results has important issues. Ignores this class'
     * course objects.
     * 
     * @return true if there are important issues that are not from course objects; false otherwise.
     */
    public boolean hasImportantIssuesIgnoreCourseObjects() {

        return super.hasImportantIssues();
    }
    
    @Override
    public boolean hasImportantIssues(){
        
        if(super.hasImportantIssues()){
            return true; 
         }
         
         for(CourseObjectValidationResults coResult : courseObjectResults){
             
             if(coResult.getValidationResults() != null && coResult.getValidationResults().hasImportantIssues()){
                 return true;
             }
         }
         
         return false;
    }
    
    /**
     * Returns whether or not the GIFT Validation Results has warning issues. Ignores this class'
     * course objects.
     * 
     * @return true if there are warning issues that are not from course objects; false otherwise.
     */
    public boolean hasWarningIssuesIgnoreCourseObjects() {

        return super.hasWarningIssues();
    }
    
    @Override
    public boolean hasWarningIssues(){
        
        if(super.hasWarningIssues()){
            return true; 
         }
         
         for(CourseObjectValidationResults coResult : courseObjectResults){
             
             if(coResult.getValidationResults() != null && coResult.getValidationResults().hasWarningIssues()){
                 return true;
             }
         }
         
         return false;
    }
    
    @Override
    public Throwable getFirstError(){
        
        Throwable issue = super.getFirstError();  //course level issue
        if(issue == null){
            //look at course objects
             
            for(CourseObjectValidationResults courseObjectResult : courseObjectResults){
                
                if(courseObjectResult.getValidationResults() != null){                    
                    issue = courseObjectResult.getValidationResults().getFirstError();
                }
                
                if(issue != null){
                    break;
                }
            }
        }
        
        return issue;
    } 
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[CourseValidationResults: courseName=");
        builder.append(courseName);
        builder.append(", courseObjectResults=");
        builder.append(courseObjectResults);
        builder.append("]");
        return builder.toString();
    }



    /**
     * Contains the validation results of a course object.
     * 
     * @author mhoffman
     *
     */
    public static class CourseObjectValidationResults implements Serializable{
       
        private static final long serialVersionUID = 1L;

        /** unique name of the course object in the course */
        private String courseObjectName;
        
        /** 
         * (optional) collection of authored branch names with the highest level (i.e. top level course tree course object)
         * being in the first position of the collection
         */
        private LinkedList<String> authoredBranchAncestortNames;
        
        /** the icon file path of the course object in the course */
        private String courseObjectIcon;
        
        /** validation results for a course object */
        private GIFTValidationResults validationResults;
        
        /**
         * Required for GWT - do not use!
         */
        public CourseObjectValidationResults() {}
        
        /**
         * Set attribute
         * 
         * @param courseObjectName unique name of the course object in the course being validated
         */
        public CourseObjectValidationResults(String courseObjectName){
            
            if(courseObjectName == null || courseObjectName.isEmpty()){
                throw new IllegalArgumentException("The course object name can't be null or empty.");
            }
            
            this.courseObjectName = courseObjectName;
        }
        
        /**
         * Return the name of the course object being validated
         * 
         * @return unique name of the course object in the course being validated.  Wont be null or empty.
         */
        public String getCourseObjectName(){
            return courseObjectName;
        }

        /**
         * Returns the icon file path for the course object. Can be null.
         * 
         * @return can be null. The icon file path for the course object.
         */
        public String getCourseObjectIcon() {
            return courseObjectIcon;
        }

        /**
         * Set the course object icon file path. E.g. "images/pdf_icon.png".
         * 
         * @param courseObjectIcon the course object icon file path to set. Can be null.
         */
        public void setCourseObjectIcon(String courseObjectIcon) {
            this.courseObjectIcon = courseObjectIcon;
        }

        /**
         * Return the validation results for a course object
         * 
         * @return can be null.  If null assume successful validation for now.
         */
        public GIFTValidationResults getValidationResults() {
            return validationResults;
        }

        /**
         * Set the validation results for a course object.
         * 
         * @param validationResults the results of validation checks
         */
        public void setValidationResults(GIFTValidationResults validationResults) {
            this.validationResults = validationResults;
        }        

        /**
         * Return the collection of authored branch course object names with the highest level (i.e. top level course tree course object)
         * being in the first position of the collection.  This is useful when the validation issue represented by 
         * this class instance is nested under one or more authored branch course objects.
         * @return can be null or empty.
         */
        public LinkedList<String> getAuthoredBranchAncestortNames() {
            return authoredBranchAncestortNames;
        }

        /**
         * Set the collection of authored branch course object names with the highest level (i.e. top level course tree course object)
         * being in the first position of the collection.  This is useful when the validation issue represented by 
         * this class instance is nested under one or more authored branch course objects.
         * @param authoredBranchAncestortNames can be null or empty.
         */
        public void setAuthoredBranchAncestortNames(LinkedList<String> authoredBranchAncestortNames) {
            this.authoredBranchAncestortNames = authoredBranchAncestortNames;
        } 

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("[CourseObjectValidationResults: courseObjectName=");
            builder.append(courseObjectName);
            if(authoredBranchAncestortNames != null){
                builder.append(", authoredBranchAncestortNames = ").append(authoredBranchAncestortNames);
            }
            builder.append(", courseObjectIcon=");
            builder.append(courseObjectIcon);
            builder.append(", validationResults=");
            builder.append(validationResults);
            builder.append("]");
            return builder.toString();
        }     
        
    }
}
