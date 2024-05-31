/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.survey;

/**
 * An enumeration of how complete a survey is
 *
 * @author jleonard
 */
public enum SurveyCompletionStatus {
    
    COMPLETE,
    MISSING_OPTIONAL,
    MISSING_REQUIRED,
    NOT_SUBMITTED
}
