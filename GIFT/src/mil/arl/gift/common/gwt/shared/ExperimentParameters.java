/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.shared;

import mil.arl.gift.common.util.StringUtils;

/**
 * The ExperimentParameters class is a Gwt compatible class that can be used by
 * both client and server logic. It contains the various parameters that are
 * used to start experiments.
 * 
 * @author sharrison
 *
 */
public class ExperimentParameters extends AbstractCourseLaunchParameters {

    /** The id of the experiment to run */
    private String experimentId;

    /** (Optional) The URL to return to once the experiment is complete */
    private String returnUrl;

    /**
     * The user session id of an existing experiment. This should only be
     * populated if we are trying to continue a running experiment
     */
    private String userSessionId;

    /**
     * Constructor - needed for Gwt serialization.
     */
    private ExperimentParameters() {
    }

    /**
     * Constructor
     * 
     * @param experimentId the id of the experiment to run. Can't be blank.
     */
    public ExperimentParameters(String experimentId) {
        this(experimentId, null);
    }

    /**
     * Constructor
     * 
     * @param experimentId the id of the experiment to run. Can't be blank.
     * @param returnUrl the URL to return to once the experiment is complete.
     *        Can be null.
     */
    public ExperimentParameters(String experimentId, String returnUrl) {
        this();
        setExperimentId(experimentId);
        setReturnUrl(returnUrl);
    }

    /**
     * Set the experiment Id for the parameters
     * 
     * @param experimentId the experiment id. Can't be blank.
     */
    private void setExperimentId(String experimentId) {
        if (StringUtils.isBlank(experimentId)) {
            throw new IllegalArgumentException("The parameter 'experimentId' cannot be blank.");
        }

        this.experimentId = experimentId;
    }

    /**
     * Retrieve the parameter's experiment id
     * 
     * @return the unique id of the experiment
     */
    public String getExperimentId() {
        return experimentId;
    }

    /**
     * Sets the optional return URL.
     * 
     * @param returnUrl the URL to return to once the experiment is complete.
     */
    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }

    /**
     * Retrieve the return URL for this experiment.
     * 
     * @return the URL to return to once the experiment is complete. Can be null
     *         if the experiment does not redirect to a specific URL after
     *         completion.
     */
    public String getReturnUrl() {
        return returnUrl;
    }

    /**
     * Set the optional user session id of the experiment. The user session
     * should point to a currently running experiment.
     * 
     * @param userSessionId the user session of an existing active experiment.
     *        Can be null if no experiment is currently running.
     */
    public void setUserSessionId(String userSessionId) {
        this.userSessionId = userSessionId;
    }

    /**
     * Retrieve the user session id of the experiment. The user session should
     * point to a currently running experiment.
     * 
     * @return the user session of an existing active experiment. Can be null if
     *         not trying to resume an experiment.
     */
    public String getUserSessionId() {
        return userSessionId;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[ExperimentParameters: ");
        sb.append("experimentId=").append(getExperimentId());
        sb.append(", returnUrl=").append(getReturnUrl());
        sb.append(", userSessionId=").append(getUserSessionId());
        sb.append("]");
        return sb.toString();
    }
}
