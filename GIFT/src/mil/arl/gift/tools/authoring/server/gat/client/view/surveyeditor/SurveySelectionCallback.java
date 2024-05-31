/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor;

import mil.arl.gift.common.survey.Survey;

/**
 * The callback interface for selecting a survey from the survey selection dialog.
 * 
 * @author nblomberg
 *
 */
public interface SurveySelectionCallback {
	
	/**
	 * Called when selection has been made from the survey selection dialog.
	 * The dialog should return a survey object that has been selected.
	 * 
	 * @param survey - The survey that was selected.
	 * @param useOriginal - whether the survey being loaded is the original survey and not a copy of an existing survey or a new survey.
	 */
    void onSelection(Survey survey, boolean useOriginal);
    
    /**
     * Called when the user cancels the survey selection dialog.
     */
    void onCancel();

	
}
