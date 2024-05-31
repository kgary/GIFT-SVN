/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.shared;

import java.math.BigInteger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;

import mil.arl.gift.tools.authoring.server.gat.client.model.record.AssessmentRecord;
import mil.arl.gift.tools.authoring.server.gat.client.view.AbstractHelpEnabledComposite;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.WarningDialog;

// TODO: Auto-generated Javadoc
/**
 * The Class DkfTaskAssessmentEditorImpl.
 */
public class AssessmentEditorImpl extends AbstractHelpEnabledComposite implements AssessmentEditor {
	
	

	/** The ui binder. */
	private static AssessmentEditorImplUiBinder uiBinder = GWT
			.create(AssessmentEditorImplUiBinder.class);

	/**
	 * The Interface AssessmentEditorImplUiBinder.
	 */
	interface AssessmentEditorImplUiBinder extends
			UiBinder<Widget, AssessmentEditorImpl> {
	}
	
	/** The container deck layout panel. */
	protected @UiField DeckPanel containerDeckLayoutPanel;
	
	/** The assessment panel. */
	protected @UiField Widget assessmentPanel;
	
	/** The warning label to display when this editor is disabled. */
	protected @UiField VerticalPanel disabledWarningLabel;

	/** The no assessment radio input. */
	protected @UiField CheckBox noAssessmentRadioInput;
	
	/** The condition assessment radio input. */
	protected @UiField RadioButton conditionAssessmentRadioInput;
	
	/** The survey assessment radio input. */
	protected @UiField RadioButton surveyAssessmentRadioInput;
	
	/** The assessment deck panel. */
	protected @UiField DeckPanel assessmentDeckPanel;
	
	/** The none panel. */
	protected @UiField HTMLPanel nonePanel;
	
	/** The condition panel. */
	protected @UiField VerticalPanel conditionPanel;
	
	/** The survey panel. */
	protected @UiField SurveyAssessmentPanel surveyPanel;
	
	@UiField
	protected Widget hasAssessmentPanel;
	
	@UiField
	protected DeckPanel hasAssessmentDeck;
	
	/** Whether or not the user should be notified that the editor is disabled. */
	private boolean shouldNotifyWhenDisabled = false;

	/**
	 * Instantiates a new dkf task assessment editor impl.
	 */
	public AssessmentEditorImpl() {
		
		initWidget(uiBinder.createAndBindUi(this));
		
		String name = Document.get().createUniqueId();
		noAssessmentRadioInput.setName(name);
		conditionAssessmentRadioInput.setName(name);
		surveyAssessmentRadioInput.setName(name);
	}

	/* (non-Javadoc)
	 * @see mil.arl.gift.tools.authoring.gat.client.view.edit.DkfTaskAssessmentEditor#getNoAssessmentRadioInput()
	 */
	@Override
	public HasValue<Boolean> getNoAssessmentRadioInput() {
		return noAssessmentRadioInput;
	}

	/* (non-Javadoc)
	 * @see mil.arl.gift.tools.authoring.gat.client.view.edit.DkfTaskAssessmentEditor#getConditionAssessmentRadioInput()
	 */
	@Override
	public HasValue<Boolean> getConditionAssessmentRadioInput() {
		return conditionAssessmentRadioInput;
	}

	/* (non-Javadoc)
	 * @see mil.arl.gift.tools.authoring.gat.client.view.edit.DkfTaskAssessmentEditor#getSurveyAssessmentRadioInput()
	 */
	@Override
	public HasValue<Boolean> getSurveyAssessmentRadioInput() {
		return surveyAssessmentRadioInput;
	}

	/* (non-Javadoc)
	 * @see mil.arl.gift.tools.authoring.gat.client.view.edit.DkfTaskAssessmentEditor#getSurveySelectInput()
	 */
	@Override
	public HasValue<String> getSurveySelectInput() {
		return surveyPanel.getSurveySelectInput();
	}

	/* (non-Javadoc)
	 * @see mil.arl.gift.tools.authoring.gat.client.view.edit.DkfTaskAssessmentEditor#getQuestionAssessmentDataDisplay()
	 */
	@Override
	public HasData<AssessmentRecord> getQuestionAssessmentDataDisplay() {
		return surveyPanel.getQuestionAssessmentDataDisplay();
	}

	/* (non-Javadoc)
	 * @see mil.arl.gift.tools.authoring.gat.client.view.edit.DkfTaskAssessmentEditor#getAddQuestionButtonInput()
	 */
	@Override
	public HasClickHandlers getAddQuestionButtonInput() {
		return surveyPanel.getAddQuestionButtonInput();
	}

	/* (non-Javadoc)
	 * @see mil.arl.gift.tools.authoring.gat.client.view.edit.DkfTaskAssessmentEditor#getDeleteQuestionButtonInput()
	 */
	@Override
	public HasClickHandlers getDeleteQuestionButtonClickInput() {
		return surveyPanel.getDeleteQuestionButtonClickInput();
	}
	
	/* (non-Javadoc)
	 * @see mil.arl.gift.tools.authoring.gat.client.view.edit.DkfTaskAssessmentEditor#getDeleteQuestionButtonInput()
	 */
	@Override
	public HasEnabled getDeleteQuestionButtonEnabledInput() {
		return surveyPanel.getDeleteQuestionButtonEnabledInput();
	}
	
	/* (non-Javadoc)
	 * @see mil.arl.gift.tools.authoring.gat.client.view.edit.DkfTaskAssessmentEditor#showNoneAssessmentPanel()
	 */
	@Override
	public void showNoneAssessmentPanel(){	
		hasAssessmentDeck.showWidget(hasAssessmentDeck.getWidgetIndex(nonePanel));
	}
	
	@Override
	public void showHasAssessmentPanel(){	
		hasAssessmentDeck.showWidget(hasAssessmentDeck.getWidgetIndex(hasAssessmentPanel));
	}

	/* (non-Javadoc)
	 * @see mil.arl.gift.tools.authoring.gat.client.view.edit.DkfTaskAssessmentEditor#showSConditionAssessmentPanel()
	 */
	@Override
	public void showConditionAssessmentPanel(){	
		assessmentDeckPanel.showWidget(assessmentDeckPanel.getWidgetIndex(conditionPanel));
	}
	
	/* (non-Javadoc)
	 * @see mil.arl.gift.tools.authoring.gat.client.view.edit.DkfTaskAssessmentEditor#showSurveyAssessmentPanel()
	 */
	@Override
	public void showSurveyAssessmentPanel(){	
		assessmentDeckPanel.showWidget(assessmentDeckPanel.getWidgetIndex(surveyPanel));
		surveyPanel.redraw();
	}

	/* (non-Javadoc)
	 * @see mil.arl.gift.tools.authoring.gat.client.view.edit.HelpEnabledFormComposite#showHelpForWidget(java.lang.Object)
	 */
	@Override
	protected void showHelpForWidget(Object widget) {
		// TODO Auto-generated method stub		
	}
	
	/* (non-Javadoc)
	 * @see mil.arl.gift.tools.authoring.gat.client.view.edit.DkfTaskAssessmentEditor#getSelectedSurveyChoice()
	 */
	@Override
	public String getSelectedSurveyChoice(){
		return surveyPanel.getSelectedSurveyChoice();
	}
	
	/* (non-Javadoc)
	 * @see mil.arl.gift.tools.authoring.gat.client.view.edit.DkfTaskAssessmentEditor#setSelectedSurveyChoice(java.lang.String)
	 */
	@Override
	public void setSelectedSurveyChoice(String choice) {
		
		surveyPanel.setSelectedSurveyChoice(choice);
	}
	
	/* (non-Javadoc)
	 * @see mil.arl.gift.tools.authoring.gat.client.view.edit.DkfTaskAssessmentEditor#setSelectedSurveyChoice(java.lang.String, boolean)
	 */
	@Override
	public void setSelectedSurveyChoice(String choice, boolean fireEvents) {
		
		surveyPanel.setSelectedSurveyChoice(choice, fireEvents);
	}
	
	/* (non-Javadoc)
     * @see mil.arl.gift.tools.authoring.gat.client.view.edit.DkfTaskAssessmentEditor#setSelectedSurveyChoiceLabel(java.lang.String)
     */
    @Override
    public void setSelectedSurveyChoiceLabel(String choice) {
        
        surveyPanel.setSelectedSurveyChoiceLabel(choice);
    }
	
	/* (non-Javadoc)
	 * @see mil.arl.gift.tools.authoring.gat.client.view.edit.DkfTaskAssessmentEditor#setSelectedSurveyChoice(java.lang.String)
	 */
	@Override
	public void setSelectedSurveyContext(BigInteger surveyContextId) {
		
		surveyPanel.setSelectedSurveyContext(surveyContextId);
	}
	

	/* (non-Javadoc)
	 * @see mil.arl.gift.tools.authoring.gat.client.view.edit.DkfTaskAssessmentEditor#redraw()
	 */
	@Override
	public void redraw() {
		
		surveyPanel.redraw();
		
		if(shouldNotifyWhenDisabled){
			WarningDialog.warning("Unhandled configuration", "The selected performance node contains multiple performance assessments, however, this editor currently only supports a single performance assessment. Please use the standard DKF Authoring Tool (DAT) to make any required changes.");
			shouldNotifyWhenDisabled = false;
		}
	}

	/* (non-Javadoc)
	 * @see mil.arl.gift.tools.authoring.gat.client.view.edit.DkfTaskAssessmentEditor#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(boolean enabled) {		
		
		containerDeckLayoutPanel.showWidget(
				enabled 
					? containerDeckLayoutPanel.getWidgetIndex(assessmentPanel) 
					: containerDeckLayoutPanel.getWidgetIndex(disabledWarningLabel));
		
		shouldNotifyWhenDisabled = enabled ? false : true;
	}
	
	@Override
	public void showSurveyWarning(String warningHtml){		
		surveyPanel.showSurveyWarning(warningHtml);
	}
	
	@Override
	public void hideSurveyWarning(){
		surveyPanel.hideSurveyWarning();
	}
}

