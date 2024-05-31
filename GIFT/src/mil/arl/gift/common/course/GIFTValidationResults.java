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
import java.util.Date;
import java.util.List;

import mil.arl.gift.common.io.DetailedException;

/**
 * Contains validation results for parsing a GIFT XML file.
 * 
 * @author mhoffman
 *
 */
public class GIFTValidationResults implements Serializable {

    private static final long serialVersionUID = 1L;    
    
    /** 
     * a severe validation issue</br>
     * Can be null.</br>
     * </br>
     * Author - this should cause the course to be invalid</br>
     * Learner - this should prevent the course from being taken
     */
    private DetailedExceptionSerializedWrapper criticalIssueDetails;
    
    /** 
     * important issues, not considered severe but still issues that need to be resolved</br>
     * Can be null or empty.</br>
     * </br>
     * Author - these should cause the course to be invalid</br>
     * Learner - these should prevent the course from being taken  
     */
    private List<DetailedExceptionSerializedWrapper> importantIssuesDetails;
    
    /** 
     * other validation issues, not critical or important but may have an affect on course execution. Often times 
     * these are caused by a lack of Internet connection when the course references URLs that can't be reached (e.g. youtube videos)</br>
     * Can be null or empty.</br>
     * Author - these should cause the course to be invalid</br>
     * Learner - these should NOT prevent the course from being taken.  
     */ 
    private List<DetailedExceptionSerializedWrapper> warningIssuesDetails;
    
    /**
     * the date of the last successful validation
     * Will be null if the validation failed.
     * Currently will also be null if the file being validated is not the course.
     */
    private Date lastSuccessfulValidationDate = null;
    
    /** (optional) number of course objects checked when producing this validation result */
    private int numOfCourseObjectsChecked = 0;
    
    /**
     * Constructor
     */
    public GIFTValidationResults() {  }

    /**
     * Get the important issues, not considered severe but still issues that need to be
     * resolved</br>
     * Can be null or empty.</br>
     * </br>
     * Author - these should cause the course to be invalid</br>
     * Learner - these should prevent the course from being taken
     * 
     * @return collection of warning issues. Can be null or empty.
     */
    public List<DetailedExceptionSerializedWrapper> getImportantIssues() {
        return importantIssuesDetails;
    }
    
    /**
     * Merge the provided validation results into this validation results.
     * If this validation result already has a critical issue, the other validation
     * result critical issue will be ignored.
     * 
     * @param otherGIFTValidationResults the other validation result to merge into this
     * validation result.  If null this method does nothing.
     */
    public void addGIFTValidationResults(GIFTValidationResults otherGIFTValidationResults){
        
        if(otherGIFTValidationResults == null){
            return;
        }
        
        if(criticalIssueDetails == null){
            setCriticalIssue(otherGIFTValidationResults.getCriticalIssue());
        }
        
        addImportantIssues(otherGIFTValidationResults.getImportantIssues());        
        addWarningIssues(otherGIFTValidationResults.getWarningIssues());
        setNumOfCourseObjectsChecked(getNumOfCourseObjectsChecked() + otherGIFTValidationResults.getNumOfCourseObjectsChecked());
    }
    
    /**
     * Add an important issue, not considered severe but still an issue that needs to be resolved</br>
     * </br>
     * Author - these should cause the course to be invalid</br>
     * Learner - these should prevent the course from being taken  
     * 
     * @param issue an issue to add as a warning issue. Can't be null.
     */
    public void addImportantIssue(DetailedException issue){
        
        if(issue == null){
            throw new IllegalArgumentException("The issue can't be null.");
        }
        
        if(importantIssuesDetails == null){
            importantIssuesDetails = new ArrayList<>();
        }
        
        importantIssuesDetails.add(new DetailedExceptionSerializedWrapper(issue));
    }
    
    /**
     * Add a collection of validation issues, not critical or important but may have an affect on course execution. Often times 
     * these are caused by a lack of Internet connection when the course references URLs that can't be reached (e.g. youtube videos)</br>
     * Can be null or empty.</br>
     * Author - these should cause the course to be invalid</br>
     * Learner - these should NOT prevent the course from being taken.  
     * 
     * @param issues a collection of warning issues. If the collection is null or empty, nothing happens. 
     */
    public void addWarningIssues(List<? extends DetailedException> issues){
        
        if(issues == null || issues.isEmpty()){
            return;
        }
        
        for(DetailedException t : issues){
            addWarningIssue(t);
        }
    }

    /**
     * Return the collection of validation issues, not critical or important but may have an affect
     * on course execution. Often times these are caused by a lack of Internet connection when the
     * course references URLs that can't be reached (e.g. youtube videos)</br>
     * Can be null or empty.</br>
     * Author - these should cause the course to be invalid</br>
     * Learner - these should NOT prevent the course from being taken.
     * 
     * @return collection of warning issues. Can be null or empty.
     */
    public List<DetailedExceptionSerializedWrapper> getWarningIssues() {
        return warningIssuesDetails;
    }
    
    /**
     * Add a validation issues, not critical or important but may have an affect on course execution. Often times 
     * these are caused by a lack of Internet connection when the course references URLs that can't be reached (e.g. youtube videos)</br>
     * Can be null or empty.</br>
     * Author - these should cause the course to be invalid</br>
     * Learner - these should NOT prevent the course from being taken. 
     * 
     * @param issue a warning issue to add. Can't be null.
     */
    public void addWarningIssue(DetailedException issue){
        
        if(issue == null){
            throw new IllegalArgumentException("The issue can't be null.");
        }
        
        if(warningIssuesDetails == null){
            warningIssuesDetails = new ArrayList<>();
        }
        
        warningIssuesDetails.add(new DetailedExceptionSerializedWrapper(issue));
    }
    
    /**
     * Add a collection of important issues, not considered severe but still an issue that needs to be resolved</br>
     * </br>
     * Author - these should cause the course to be invalid</br>
     * Learner - these should prevent the course from being taken  
     * 
     * @param issues a collection of important issues.  If the collection is null or empty, nothing happens.
     */
    public void addImportantIssues(List<? extends DetailedException> issues){
        
        if(issues == null || issues.isEmpty()){
            return;
        }
        
        for(DetailedException t : issues){
            addImportantIssue(t);
        }
    }

    /**
     * Return the critical validation issue.</br>
     * Can be null.</br>
     * </br>
     * Author - this should cause the course to be invalid</br>
     * Learner - this should prevent the course from being taken
     * 
     * @return can be null if there was no critical validation issue.
     */
    public DetailedExceptionSerializedWrapper getCriticalIssue() {
        return criticalIssueDetails == null ? null : criticalIssueDetails;
    }

    /**
     * Set the severe validation issue</br>
     * Can be null.</br>
     * </br>
     * Author - this should cause the course to be invalid</br>
     * Learner - this should prevent the course from being taken
     * 
     * @param issue the severe issue.  Can be null.
     */
    public void setCriticalIssue(DetailedException issue) {
        this.criticalIssueDetails = issue == null ? null : new DetailedExceptionSerializedWrapper(issue);
    }

    /**
     * Return true if there is a 'critical' issue.
     * See {@link #criticalIssue} for more information.
     * 
     * @return true iff there is critical issue in this instance.
     */
    public boolean hasCriticalIssue() {
        return this.criticalIssueDetails != null;
    }
    
    /**
     * Return true if there are 'important' issues.
     * See {@link #importantIssues} for more information.
     * 
     * @return true iff there are 'important' issues in this instance.
     */
    public boolean hasImportantIssues(){
        return this.importantIssuesDetails != null && !this.importantIssuesDetails.isEmpty();
    }
    
    /**
     * Return true if there are 'warning' issues.
     * See {@link #warningIssues} for more information.
     * 
     * @return true iff there are 'warning' issues in this instance.
     */
    public boolean hasWarningIssues(){
        return this.warningIssuesDetails != null && !this.warningIssuesDetails.isEmpty();
    }
    
    /**
     * Return true if there are issues.
     * 
     * @return true iff there are issues in this instance.
     */
    public boolean hasIssues(){
        return hasCriticalIssue() || hasImportantIssues() || hasWarningIssues();
    }
    
    /**
     * Return the first issue found in this instance.
     * 
     * @return the critical issue is returned first,</br>
     * followed by the first {@link #importantIssues} issue found,</br>
     * then the first {@link #warningIssues} issue.</br> 
     * Can return null if there are no issues.
     */
    public Throwable getFirstError(){
        
        if(criticalIssueDetails != null){
            return criticalIssueDetails;
        }else if(importantIssuesDetails != null && !importantIssuesDetails.isEmpty()){
            return importantIssuesDetails.get(0);
        }else if(warningIssuesDetails != null && !warningIssuesDetails.isEmpty()){
            return warningIssuesDetails.get(0);
        }
        
        return null;
    }
    
    /**
     * Return the date of the last successful validation.
     * 
     * @return will be null if any of the following are true:</br>
     * 1. this validation result contains issues
     * 2. this validation result is for a GIFT XML file other than a course.xml
     */
    public Date getLastSuccessfulValidationDate() {
        return lastSuccessfulValidationDate;
    }

    /**
     * Set the date of the last successful validation of the course.
     * 
     * @param lastSuccessfulValidationDate the date of the last successful validation.  Use null to indicate
     * the course was not successfully validated.
     */
    public void setLastSuccessfulValidationDate(Date lastSuccessfulValidationDate) {
        this.lastSuccessfulValidationDate = lastSuccessfulValidationDate;
    }

    /**
     * Return the number of course objects checked when producing this validation result
     * @return default is 0, won't be negative.
     */
    public int getNumOfCourseObjectsChecked() {
        return numOfCourseObjectsChecked;
    }

    /**
     * Set the number of course objects checked when producing this validation result
     * @param numOfCourseObjectsChecked should be a positive number
     */
    public void setNumOfCourseObjectsChecked(int numOfCourseObjectsChecked) {
        if(numOfCourseObjectsChecked < 0){
            throw new IllegalArgumentException("The number of course objects checked can't be a negative number.");
        }
        this.numOfCourseObjectsChecked = numOfCourseObjectsChecked;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[GIFTValidationResults: ");
        builder.append("lastSuccessfulValidation = ");
        builder.append(lastSuccessfulValidationDate);
        builder.append("\n, criticalIssueDetails=\n");
        builder.append(criticalIssueDetails);
        builder.append("\n, importantIssuesDetails=");
        builder.append(importantIssuesDetails);
        builder.append("\n, warningIssuesDetails=");
        builder.append(warningIssuesDetails);
        builder.append("]");
        return builder.toString();
    }
    
    /**
     * This inner class is used to retrieve the stack trace of a detailed exception on the server side
     * in order to expose this stack trace on the GWT client side.
     * 
     * @author mhoffman
     *
     */
    public static class DetailedExceptionSerializedWrapper extends DetailedException{
        
        private static final long serialVersionUID = 1L;
        
        private List<String> stackTrace;
        
        /**
         * Required for GWT - do not use!
         */
        public DetailedExceptionSerializedWrapper(){ }
        
        /**
         * Creates a copy of the detailed exception and retrieves the stack trace from the cause of the exception (if not null).
         * 
         * @param detailedException the exception to convert into a serialized version to deliver to the GWT client.  Can't be null.
         */
        public DetailedExceptionSerializedWrapper(DetailedException detailedException){
            super(detailedException.getReason(), detailedException.getDetails(), detailedException.getCause());
            
            stackTrace = detailedException.getErrorStackTrace();
        }
        
        /**
         * Return the detailed exception stack trace.
         * 
         * @return a representation of the stack trace. Won't be null but can be empty if the cause value is null for this class.
         */
        public List<String> getSerializedStackTrace(){
            return stackTrace;
        }
    }

}
