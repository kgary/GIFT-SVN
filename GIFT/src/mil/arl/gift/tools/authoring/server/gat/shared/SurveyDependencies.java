/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * A class for holding the dependencies of a survey
 *
 * @author jleonard
 */
public class SurveyDependencies implements IsSerializable {

    private boolean hasResponses = false;

    private String dependencies = null;

    /**
     * Default Constructor
     *
     * Required to exist and be public for IsSerializable
     */
    public SurveyDependencies() {
    }

    /**
     * Constructor
     *
     * @param hasResponses If the survey has responses
     * @param dependencies What non-response dependencies there are for the
     * survey
     */
    public SurveyDependencies(boolean hasResponses, String dependencies) {

        this.hasResponses = hasResponses;
        this.dependencies = dependencies;
    }

    /**
     * Gets if the survey has responses
     *
     * @return boolean If the survey has responses
     */
    public boolean hasResponses() {

        return hasResponses;
    }

    /**
     * Gets the non-response dependencies there are for the survey, formatted in
     * HTML
     *
     * @return String The HTML string of the dependencies there are for the
     * survey
     */
    public String getDependencies() {

        return dependencies;
    }
}
