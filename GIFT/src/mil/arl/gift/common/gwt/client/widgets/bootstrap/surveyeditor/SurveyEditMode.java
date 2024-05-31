/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor;

/**
 * The modes of authoring supported by the survey editor panel.
 *   Writing Mode - Users can author question text, responses.
 *   Scoring Mode - Users can author scoring logic (eg. response point values).  
 */
public enum SurveyEditMode {
    WritingMode,
    ScoringMode,
}