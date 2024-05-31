/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.shared;


/**
 * The LtiParameters class is a Gwt compatible class that can be used by both client and server logic.
 * It contains the various parameters that are used to start courses via the LTI specification.
 * 
 * @author nblomberg
 *
 */
public class LtiParameters extends AbstractCourseLaunchParameters  {

    /** The consumer key of the lti user launching the course. */
    private String consumerKey;
    /** The consumer id of the lti user launching the course. */
    private String consumerId;
    /** The course id (domain id) containing the path to the original location (not runtime location) of the course. */
    private String courseId;
    /** (Optional) The data set id which is used to record the domain sessions for data collection. */
    private String dataSetId;
    /** (Optioanl) The url to the lis outcome service.  Used if GIFT will send scores back to the Tool Consumer. */
    private String lisServiceUrl;
    /** (Optional) The lis sourcedid which is used for scoring.  This identifies the tool consumer user / course information.   This is
     * only used if GIFT will send scores back to the Tool Consumer. 
     */
    private String lisSourcedid;
    
    /**
     * Constructor - needed for Gwt serialization.
     */
    public LtiParameters() {
        
    }
    
    /**
     * Constructor (default)
     */
    public LtiParameters(String consumerKey, String consumerId, String courseId, String dataSetId,
                                        String lisServiceUrl, String lisSourcedid) {

        setConsumerKey(consumerKey);
        setConsumerId(consumerId);
        setCourseId(courseId);
        setDataSetId(dataSetId);
        setLisServiceUrl(lisServiceUrl);
        setLisSourcedid(lisSourcedid);
    }

    /**
     * Gets the consumer key of the lti user launching the course.
     * 
     * @return the consumer key of the lti user launching the course.
     */
    public String getConsumerKey() {
        return consumerKey;
    }

    /**
     * Sets the consumer key of the lti user launching the course.
     * 
     * @param consumerKey The consumer key of the lti user launching the course.
     */
    public void setConsumerKey(String consumerKey) {
        this.consumerKey = consumerKey;
    }

    /**
     * Gets the consumer id of the lti user launching the course.
     * 
     * @return The consumer id of the lti user launching the course.
     */
    public String getConsumerId() {
        return consumerId;
    }

    /**
     * Sets the consumer id of the lti user launching the course.
     * 
     * @param consumerId The consumer id of the lti user launching the course.
     */
    public void setConsumerId(String consumerId) {
        this.consumerId = consumerId;
    }

    /**
     * Gets the course id (domain id) containing the path to the original location 
     * (not runtime location) of the course.
     *  
     * @return The course id of the course that will be started.
     */
    public String getCourseId() {
        return courseId;
    }

    /**
     * Sets the course id (domain id) containing the path to the original location 
     * (not runtime location) of the course. 
     * @param courseId The course id of the course that will be started.
     */
    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    /**
     * @return the dataSetId
     */
    public String getDataSetId() {
        return dataSetId;
    }

    /**
     * @param dataSetId the dataSetId to set
     */
    public void setDataSetId(String dataSetId) {
        this.dataSetId = dataSetId;
    }

    /**
     * @return the lisServiceUrl
     */
    public String getLisServiceUrl() {
        return lisServiceUrl;
    }

    /**
     * @param lisServiceUrl the lisServiceUrl to set
     */
    public void setLisServiceUrl(String lisServiceUrl) {
        this.lisServiceUrl = lisServiceUrl;
    }

    /**
     * @return the lisSourcedid
     */
    public String getLisSourcedid() {
        return lisSourcedid;
    }

    /**
     * @param lisSourcedid the lisSourcedid to set
     */
    public void setLisSourcedid(String lisSourcedid) {
        this.lisSourcedid = lisSourcedid;
    }

    @Override
    public String toString(){
        
        StringBuilder sb = new StringBuilder();
        sb.append("[LtiParameters: ");
        sb.append("consumerKey=").append(getConsumerKey());
        sb.append(", consumerId=").append(getConsumerId());
        sb.append(", courseId=").append(getCourseId());
        sb.append(", dataSetId=").append(getDataSetId());
        sb.append(", lisServiceUrl=").append(getLisServiceUrl());
        sb.append(", lisSourcedid=").append(getLisSourcedid());
        sb.append("]");
        return sb.toString();
    }
    
}
