/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mil.arl.gift.common.course.CourseConceptsUtil;

/**
 * An LMS data request is used to retrieve the training history of a learner
 * from the LMS
 *
 * @author mhoffman
 */
public class LMSDataRequest {

    /** The unique user name of a user that wants to get their information from the LMS */
    private String userName;

    /**  use to retrieve specific records by their id */
    private List<PublishLessonScoreResponse> publishedScores = new ArrayList<>();

    /** 
     * the zero based index of the record to return, useful for paging records to the requestor 
     * Note: value is ignored if specific records to retrieve are specified
     */
    private int pageStart;

    /** 
     * how many records to return for this request 
     * Note: value is ignored if specific records to retrieve are specified
     */
    private int pageSize;

    /** 
     * whether to sort by date with newest records first 
     * Note: value is ignored if specific records to retrieve are specified 
     */
    private boolean sortDescending;
    
    /** 
     * (optional) used to filter for specific LMS records on domains/courses 
     * Note: value is ignored if specific records to retrieve are specified
     * e.g. Public/TSP 07-GFT-0137 Vignettes/TSP 07-GFT-0137 ClearBldg.jtc_shakarat.course.xml
     */
    private Set<String> domainIds = new HashSet<>();
    
    /**
     * (optional) used to filter for specific LMS records for specified domain sessions
     * Note: value is ignored if specific records to retrieve are specified
     */
    private Set<Integer> domainSessionIds = null;
    
    /** (optional) course concepts being used in this course, can be used to query for initial learner state information */
    private generated.course.Concepts courseConcepts = null;
    
    /**
     * Flag to indicate if the learner sent the request. If the learner sent the request (flag is
     * true) we want the response to contain the learner state attributes data in the form of a list
     * of {@link AbstractScale}s. If the request was not sent from the learner (flag is false) then
     * we want the response to contain full LMS course records.
     */
    private boolean learnerRequest;

    /** 
     * the recommended number of records to retrieve per request to limit
     * network packet size 
     */
    public static final int RECOMMENDED_RECORDS_PER_REQUEST = 10;
    
    /**
     * the time to use when only the state information since that point in time are needed. This
     * is useful when the previous state information is saved and only state updates in the external system since the last
     * request was made are needed. 
     */
    private Date sinceWhen;

    /**
     * Gets all records for a user from the LMS
     *
     * @param userName - the unique lms user name of a user that wants to get
     * their information from the LMS.  Can't be null or empty.
     */
    public LMSDataRequest(String userName) {
        setUserName(userName);
    }
    
    /**
     * Add a domain session id to the collection of ids to use to filter the returned records.
     * Note: value is ignored if specific records to retrieve are specified
     * @param dsId if null this method does nothing.
     */
    public void addDomainSessionId(Integer dsId){
        
        if(dsId == null){
            return;
        }
        
        if(domainSessionIds == null){
            domainSessionIds = new HashSet<>();
        }
        
        domainSessionIds.add(dsId);
    }
    
    /**
     * Return the unique set of domain session ids to filter the returned results by.
     * @return can be null if no domain session ids where added.
     */
    public Set<Integer> getDomainSessionIds(){
        return domainSessionIds;
    }
    
    /**
     * Return the course concepts being used in this course, can be used to query for initial learner state information
     * @return can be null
     */
    public generated.course.Concepts getCourseConcepts() {
        return courseConcepts;
    }

    /**
     * Set the course concepts being used in this course, can be used to query for initial learner state information
     * @param courseConcepts can be null
     */
    public void setCourseConcepts(generated.course.Concepts courseConcepts) {
        this.courseConcepts = courseConcepts;
    }

    /**
     * Set the unique user name of a user that wants to get their information
     * from the LMS
     * @param username the unique user name.  Can not be null or empty string.
     */
    private void setUserName(String username){
     
        if(username == null || username.isEmpty()){
            throw new IllegalArgumentException("The user name can't be null or empty.");
        }
        
        this.userName = username;
    }

    /**
     * Return unique user name of a user that wants to get their information
     * from the LMS
     *
     * @return the unique user name.  Will not be null or empty string.
     */
    public String getUserName() {
        return userName;
    }
    
    /**
     * Return the identifiable information for the records wanting to be retrieved from the LMS
     *  
     * @return the specific records to retrieve.  Can be empty but not null.
     */
    public List<PublishLessonScoreResponse> getPublishedScores(){
        return publishedScores;
    }
    
    /**
     * Set the specific records to retrieve.
     * 
     * @param publishedScores collection of identifiable information for published score located in the LMS.
     * If null is provided, nothing is done.  
     */
    public void setPublishedScores(Collection<PublishLessonScoreResponse> publishedScores){
        
        if (publishedScores != null) {
            this.publishedScores.addAll(publishedScores);
        }
    }

    /**
     * Gets the index to start getting LMS Records from from the entire query
     * result
     *
     * @return int The index to start getting LMS Records from.  Will be a non-negative integer.
     */
    public int getPageStart() {
        return pageStart;
    }
    
    /**
     * Set the index of the first record returned in this request.  For example if the request
     * should return the 5th and onward records, the value should be 4 (zero based index).
     * 
     * @param pageStart The index of the first record of the page returned. Can't be negative.
     * Note: value is ignored if specific records to retrieve are specified
     */
    public void setPageStart(int pageStart){
        
        if(pageStart < 0){
            throw new IllegalArgumentException("The page start can't be negative");
        }
        
        this.pageStart = pageStart;
    }

    /**
     * Gets how many LMS Records to return
     *
     * If 0 is returned, all records are returned
     *
     * @return How many LMS Records to return
     */
    public int getPageSize() {
        return pageSize;
    }
    
    /**
     * Set how many LMS records to return.
     * 
     * @param pageSize If 0 is returned, all records are returned (that also satisfy other attributes set in this request).
     * Can't be negative.
     * Note: value is ignored if specific records to retrieve are specified
     */
    public void setPageSize(int pageSize){
        
        if(pageSize < 0){
            throw new IllegalArgumentException("The page size can't be negative");
        }
        
        this.pageSize = pageSize;
    }

    /**
     * Gets if the LMS Records are to be sorted with the newest records
     * appearing in the list first
     *
     * @return boolean If the LMS Records are to be sorted with the newest
     * records first
     */
    public boolean getSortDescending() {
        return sortDescending;
    }
    
    /**
     * Set whether the records should be sorted by date, newest first.
     * 
     * @param sortDescending true if the newest records (that also satisfy the other attributes set in this request)
     * should be earlier in the list</br>
     * Note: value is ignored if specific records to retrieve are specified
     */
    public void setShouldSortDescending(boolean sortDescending){
        this.sortDescending = sortDescending;
    }

    /**
     * Return the domain ids used to filter for specific LMS records on domains/courses
     * 
     * @return can be empty to indicate that the field is not used.<br/>
     * e.g. Public/TSP 07-GFT-0137 Vignettes/TSP 07-GFT-0137 ClearBldg.jtc_shakarat.course.xml<br/>
     * Notes: <br/>
     * i. value is ignored if specific records to retrieve are specified.<br/>
     * ii. if page size is set the value will apply to each domain specified<br/>
     * iii. if page start is set the value will apply to each domain specified
     */
    public Set<String> getDomainIds() {
        return domainIds;
    }

    /**
     * Add a domain ids to the collection used to filter for specific LMS records on domains/courses
     * 
     * @param domainId can't be null or empty string.<br/>
     * e.g. Public/TSP 07-GFT-0137 Vignettes/TSP 07-GFT-0137 ClearBldg.jtc_shakarat.course.xml<br/>
     * Note: value is ignored if specific records to retrieve are specified
     */
    public void addDomainId(String domainId) {
        
        if(domainId == null || domainId.isEmpty()){
            throw new IllegalArgumentException("The domain id can't be null or empty.");
        }
        
        domainIds.add(domainId);
    }
    
    /**
     * Add the collection of domain ids to this class collection of domain ids used to filter
     * for specific LMS records on domains/courses.
     * 
     * @param domainIds collection of course ids to add.<br/>
     * e.g. entry Public/TSP 07-GFT-0137 Vignettes/TSP 07-GFT-0137 ClearBldg.jtc_shakarat.course.xml
     */
    public void addDomainIds(Set<String> domainIds){
        
        if(domainIds != null){
            this.domainIds.addAll(domainIds);
        }
    }
    
    /**
     * @return the flag to indicate if the learner sent the request. If the learner sent the request
     *         (flag is true) we want the response to contain the learner state attributes data in
     *         the form of a list of {@link AbstractScale}s. If the request was not sent from the
     *         learner (flag is false) then we want the response to contain full LMS course records.
     */
    public boolean isLearnerRequest() {
        return learnerRequest;
    }

    /**
     * @param learnerRequest true if this request is originating from the learner; false otherwise.
     */
    public void setLearnerRequest(boolean learnerRequest) {
        this.learnerRequest = learnerRequest;
    }

    /**
     * Return the time to use when only the state information since that point in time are needed. This
     * is useful when the previous state information is saved and only state updates in the external system since the last
     * request was made are needed. 
     * @return is optional. Can be null when not used.
     */
    public Date getSinceWhen() {
        return sinceWhen;
    }

    /**
     * Set the time to use when only the state information since that point in time are needed. This
     * is useful when the previous state information is saved and only state updates in the external system since the last
     * request was made are needed. 
     * @param sinceWhen is optional. Can be null when not used.
     */
    public void setSinceWhen(Date sinceWhen) {
        this.sinceWhen = sinceWhen;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[LMSDataRequest: ");
        sb.append(" userName = ").append(getUserName());
        sb.append(", domainIds = ").append(getDomainIds());
        if(getDomainSessionIds() != null){
            sb.append(", domainSessionIds = ").append(getDomainSessionIds());
        }
        
        if(getCourseConcepts() != null){
            sb.append(", concepts = ").append(CourseConceptsUtil.getConceptNameList(courseConcepts));
        }
        
        sb.append(", Score Keys = {");
        for (PublishLessonScoreResponse publishedScore : getPublishedScores()) {

            sb.append(publishedScore).append(", ");
        }
        sb.append("}");
        
        sb.append(", Page Start = ").append(getPageStart());
        sb.append(", Page Size = ").append(getPageSize());
        sb.append(", Sort Descending = ").append(getSortDescending());
        sb.append(", Learner Request = ").append(isLearnerRequest());
        sb.append(", since when = ").append(getSinceWhen());
        sb.append("]");

        return sb.toString();
    }
}
