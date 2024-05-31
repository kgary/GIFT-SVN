/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap;

import mil.arl.gift.common.gwt.shared.ExperimentParameters;
import mil.arl.gift.common.gwt.shared.LtiParameters;


/**
 * Class used to manage course load parameters.
 * 
 * @author mhoffman
 *
 */
public class LoadCourseParameters{
    
    /** the selected domain runtime id which is the path to the course used when taking the course */
    private String courseRuntimeId;
    
    /** the selected domain source id which is the path to the authored course */
    private String courseSourceId;
    
    /** the selected domain name which is the name of the course */
    private String courseDomainName;
    
    /** Optional. The experiment parameters for the course launch */
    private ExperimentParameters experimentParameters;

    /** Optional. The lti parameters for the course launch */
    private LtiParameters ltiParameters;
   
    
    
    /**
     * Needed for GWT serialization
     */
    public LoadCourseParameters(){ }
    
    /**
     * Set attributes
     * 
     * @param courseRuntimeId the selected domain runtime id which is the path to the course used when taking the course.
     * @param courseSourceId the selected domain source id which is the path to the authored course
     * @param domainName the selected domain name. This name will be displayed in the toolbar when the course starts.
     */
    public LoadCourseParameters(String courseRuntimeId, String courseSourceId, String domainName){
        this.courseRuntimeId = courseRuntimeId;
        this.courseSourceId = courseSourceId;
        this.courseDomainName = domainName;
    }

    /**
     * Get the runtime id of the course
     * 
     * @return the runtime if of the course
     */
    public String getCourseRuntimeId() {
        return courseRuntimeId;
    }

    /**
     * Get the source id of the course
     * 
     * @return the source id of the course
     */
    public String getCourseSourceId() {
        return courseSourceId;
    }
    
    /**
     * Gets the course domain name
     * 
     * @return the domain name
     */
    public String getCourseDomainName() {
        return courseDomainName;
    }

    /**
     * Get the experiment parameters
     * 
     * @return the experiment parameters
     */
    public ExperimentParameters getExperimentParameters() {
        return experimentParameters;
    }

    /**
     * Set the experiment parameters.
     * 
     * @param experimentParameters the parameters of the experiment. Can't be set if
     *        {@link #ltiParameters} exists.
     */
    public void setExperimentParameters(ExperimentParameters experimentParameters) {
        if (getLtiParameters() != null) {
            throw new IllegalArgumentException("Can't set experiment parameters if the LTI parameters are already set");
        }
        this.experimentParameters = experimentParameters;
    }

    /**
     * Get the LTI parameters
     * 
     * @return the LTI parameters
     */
    public LtiParameters getLtiParameters() {
        return ltiParameters;
    }

    /**
     * Set the LTI parameters
     * 
     * @param ltiParameters the parameters of the LTI. Can't be set if
     *        {@link #experimentParameters} exists.
     */
    public void setLtiParameters(LtiParameters ltiParameters) {
        if (getExperimentParameters() != null) {
            throw new IllegalArgumentException(
                    "Can't set the LTI parameters if the experiment parameters are already set");
        }
        this.ltiParameters = ltiParameters;
    }

    @Override
    public String toString(){
        
        StringBuilder sb = new StringBuilder();
        sb.append("[LoadCourseParameters: ");
        sb.append("courseRuntimeId = ").append(getCourseRuntimeId());
        sb.append(", courseSourceId = ").append(getCourseSourceId());
        sb.append(", courseDomainName = ").append(getCourseDomainName());
        sb.append(", experimentParameters = ").append(getExperimentParameters());
        sb.append(", ltiParameters = ").append(getLtiParameters());
        sb.append("]");
        return sb.toString();
    }
}
