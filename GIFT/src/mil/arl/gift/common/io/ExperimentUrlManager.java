/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.io;

import mil.arl.gift.common.util.StringUtils;

/**
 * An object that manages the creation of experiment URLs.
 * 
 * @author nroberts
 */
public class ExperimentUrlManager {

    /** The URL parameter for the experiment ID */
    public static final String EXPERIMENT_ID_URL_PARAMETER = "eid";

    /** The URL parameter for the course collection ID */
    public static final String COURSE_COLLECTION_ID_URL_PARAMETER = "courseCollectionId";

    /** The URL parameter for the return URL */
    public static final String RETURN_URL_PARAMETER = "returnUrl";

    /**
     * Gets the experiment URL corresponding to the given experiment. Note: This
     * should only be called from locations with access to the common properties
     * file (i.e. not client-side code), since the properties are needed to set
     * the correct host.
     * 
     * @param experimentId the ID of the experiment for which to get the URL. Can't be blank.
     * @return the URL for the experiment
     */
    public static String getExperimentUrl(String experimentId) {
        if (StringUtils.isBlank(experimentId)) {
            throw new IllegalArgumentException("The parameter 'experimentId' cannot be blank.");
        }

        return buildUrlPath(EXPERIMENT_ID_URL_PARAMETER, experimentId);
    }

    /**
     * Builds the {@link CourseCollection} URL for a given
     * {@link CourseCollection} id.
     *
     * @param collectionId The id for which to build the URL. Can't be null or
     *        empty.
     * @return The constructed URL. Can't be null or empty.
     */
    public static String getCourseCollectionUrl(String collectionId) {
        if (StringUtils.isBlank(collectionId)) {
            throw new IllegalArgumentException("The parameter 'collectionId' cannot be blank.");
        }

        return buildUrlPath(COURSE_COLLECTION_ID_URL_PARAMETER, collectionId);
    }

    /**
     * Helper method to build url paths
     * 
     * @param key the parameter key
     * @param value the parameter value
     * @return the url path
     */
    private static String buildUrlPath(String key, String value) {
        StringBuilder url = new StringBuilder();
        url.append(CommonProperties.getInstance().getDashboardURL());

        if (!url.toString().endsWith(Constants.FORWARD_SLASH)) {
            url.append(Constants.FORWARD_SLASH);
        }

        url.append("?");
        url.append(key);
        url.append("=");
        url.append(value);

        return url.toString();
    }
}
