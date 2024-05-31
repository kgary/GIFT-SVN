/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.shared;

import java.math.BigInteger;

import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.view.client.HasData;

import mil.arl.gift.tools.authoring.server.gat.client.model.record.AssessmentRecord;

// TODO: Auto-generated Javadoc
/**
 * The Interface DkfTaskAssessmentEditor.
 */
public interface AssessmentEditor {

	/**
	 * Gets the no assessment radio input.
	 *
	 * @return the no assessment radio input
	 */
	HasValue<Boolean> getNoAssessmentRadioInput();

	/**
	 * Gets the condition assessment radio input.
	 *
	 * @return the condition assessment radio input
	 */
	HasValue<Boolean> getConditionAssessmentRadioInput();

	/**
	 * Gets the survey assessment radio input.
	 *
	 * @return the survey assessment radio input
	 */
	HasValue<Boolean> getSurveyAssessmentRadioInput();

	/**
	 * Gets the survey select input.
	 *
	 * @return the survey select input
	 */
	HasValue<String> getSurveySelectInput();

	/**
	 * Gets the question assessment data display.
	 *
	 * @return the question assessment data display
	 */
	HasData<AssessmentRecord> getQuestionAssessmentDataDisplay();

	/**
	 * Gets the adds the question button input.
	 *
	 * @return the adds the question button input
	 */
	HasClickHandlers getAddQuestionButtonInput();

	/**
	 * Gets the delete question button click input.
	 *
	 * @return the delete question button click input
	 */
	HasClickHandlers getDeleteQuestionButtonClickInput();
	
	/**
	 * Gets the delete question button enabled input.
	 *
	 * @return the delete question button enabled input
	 */
	HasEnabled getDeleteQuestionButtonEnabledInput();

	/**
	 * Gets the selected survey choice.
	 *
	 * @return the selected survey choice
	 */
	String getSelectedSurveyChoice();

	/**
	 * Sets the selected survey choice.
	 *
	 * @param string the new selected survey choice
	 */
	void setSelectedSurveyChoice(String string);

	
	/**
	 * Sets the selected survey choice.
	 *
	 * @param string the new selected survey choice
	 * @param fireEvents whether or not to fire events
	 */
	void setSelectedSurveyChoice(String string, boolean fireEvents);

	/**
	 * Redraw.
	 */
	void redraw();

	/**
	 * Sets whether or not this editor should be enabled.
	 *
	 * @param enabled whether or not this editor should be enabled.
	 */
	void setEnabled(boolean enabled);

	/**
	 * Show the none assessment panel.
	 */
	void showNoneAssessmentPanel();

	/**
	 * Show the condition assessment panel.
	 */
	void showConditionAssessmentPanel();
	
	/**
	 * Show the survey assessment panel.
	 */
	void showSurveyAssessmentPanel();

	/**
	 * 
	 */
	void showHasAssessmentPanel();

	/**
	 * 
	 */
	void hideSurveyWarning();

	/**
	 * @param warningHtml
	 */
	void showSurveyWarning(String warningHtml);

	/**
	 * Sets the selected survey context for the survey selection dialog.
	 * 
	 * @param surveyContextId The selected survey context id.
	 */
	void setSelectedSurveyContext(BigInteger surveyContextId);

	/**
     * Sets the survey name of the selected survey context for the user.
     * 
     * @param name The survey name of the selected survey context id.
     */
    void setSelectedSurveyChoiceLabel(String name);

}

