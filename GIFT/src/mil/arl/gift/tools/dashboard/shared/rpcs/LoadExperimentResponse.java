/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.shared.rpcs;

import mil.arl.gift.common.gwt.client.DetailedRpcResponse;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.util.StringUtils;

/**
 * A response to loading an experiment course in the dashboard. Contains
 * information about the loaded experiment including the new runtime path and
 * the original source location.
 * 
 * @author sharrison
 *
 */
public class LoadExperimentResponse extends DetailedRpcResponse {

    /**
     * The runtime folder that the course was copied to
     */
    private String courseRuntimeFolder;

    /** The original course folder that was copied */
    private String courseSourceFolder;

    /**
     * Default Constructor
     *
     * Required for GWT
     */
    private LoadExperimentResponse() {
    }

    /**
     * Set information about the course validation result.
     * 
     * @param courseRuntimeFolder the runtime folder that the course was copied
     *        to. Can't be blank.
     * @param courseSourceFolder the original course folder that was copied.
     *        Can't be blank.
     */
    public LoadExperimentResponse(String courseRuntimeFolder, String courseSourceFolder) {
        this();
        if (StringUtils.isBlank(courseRuntimeFolder)) {
            throw new IllegalArgumentException(
                    "The courseRuntimeFolder can't be blank when indicating a successful response.");
        } else if (StringUtils.isBlank(courseSourceFolder)) {
            throw new IllegalArgumentException(
                    "The courseSourceFolder can't be blank when indicating a successful response.");
        }

        setIsSuccess(true);
        this.courseRuntimeFolder = courseRuntimeFolder;
        this.courseSourceFolder = courseSourceFolder;
    }

    /**
     * Set information about the course validation result.
     * 
     * @param errorMessage the message that should be shown back to the user if
     *        an error is encountered. Can't be blank.
     * @param errorDetails a more robust error message mentioning details that
     *        may be useful for developers. Can be null.
     * @param t the throwable exception used to parse the stack trace. Can be
     *        null.
     */
    public LoadExperimentResponse(String errorMessage, String errorDetails, Throwable t) {
        this();
        if (StringUtils.isBlank(errorMessage)) {
            throw new IllegalArgumentException("The errorMessage can't be blank.");
        }

        setIsSuccess(false);
        setErrorMessage(errorMessage);
        setResponse(errorMessage);
        setErrorDetails(errorDetails);
        if (t != null) {
            setErrorStackTrace(DetailedException.getFullStackTrace(t));
        }
    }

    /**
     * Retrieve the course runtime folder.
     * 
     * @return the course runtime folder.
     */
    public String getCourseRuntimeFolder() {
        return courseRuntimeFolder;
    }

    /**
     * Retrieve the course source folder.
     * 
     * @return the course source folder.
     */
    public String getCourseSourceFolder() {
        return courseSourceFolder;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[LoadExperimentResponse: ");
        sb.append("courseRuntimeFolder: ").append(getCourseRuntimeFolder());
        sb.append(", courseSourceFolder: ").append(getCourseSourceFolder());
        sb.append(", ").append(super.toString());
        sb.append("]");
        return sb.toString();
    }
}
