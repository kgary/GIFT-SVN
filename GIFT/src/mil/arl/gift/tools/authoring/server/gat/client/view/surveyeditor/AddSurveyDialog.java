/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor;

import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Container;
import org.gwtbootstrap3.client.ui.Tooltip;

import mil.arl.gift.common.gwt.client.widgets.bootstrap.ModalDialogBox;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;



/**
 * The Add Survey Dialog box uses the improved GAT UI flow to allow a user to select a type of
 * survey that will be used in the course.  The author of the course can select from the following choices.

 *   - Collect Learner Info (Not Actionable) -> Author wants to create a survey which will collect information from the user.
 *   - Collect Learner Info (Actionable) -> Author wants to create a survey which collects information from the user, and is scored.
 *   - Assess Learner Knowledge -> Author wants to assess learner state with a static set of questions.
 *   - Cancel - Cancel out of the dialog.
 *   
 * Note that that Dynamic Question Banks are now authored from a separate interface within the GAT.
 *  
 * The dialog is structured to simplify the process of survey creation for the author. 
 * 
 * @author nblomberg
 *
 */
public class AddSurveyDialog extends ModalDialogBox {

    private static Logger logger = Logger.getLogger(AddSurveyDialog.class.getName());
    
	private static AddSurveyDialogUiBinder uiBinder = GWT.create(AddSurveyDialogUiBinder.class);

	interface AddSurveyDialogUiBinder extends
			UiBinder<Widget, AddSurveyDialog> {
	}
	
	private static String TITLE_ADD_SURVEY = "Create a New Survey";
	
	/** Enumeration of all possible choices from the addsurvey dialog box. */
	public enum SurveyDialogOption {
	    CANCEL,
	    COLLECTINFO_SCORED,
	    COLLECTINFO_NOTSCORED,
	    ASSESSLEARNER_STATIC,
	    ASSESSLEARNER_QUESTIONBANK,
	}
	
	@UiField
	Button cancelButton;
	
	@UiField
	Button buttonCuiNa;
	
	@UiField
	Button buttonCuiA;
	
	@UiField
	Button buttonAlk;

	@UiField
	Container dialogFooter;
	
	@UiField
	Tooltip tooltipCuiA;
	
	@UiField
	Tooltip tooltipCuiNa;
	
	@UiField
	Tooltip tooltipAlk;
	
	@UiHandler("buttonCuiNa") 
	void onButtonCuiClick(ClickEvent event) {
	    logger.info("Collect Learner Information (Not Actionable) button was clicked.");
	    onSurveyChoiceSelected(SurveyDialogOption.COLLECTINFO_NOTSCORED);
	}
	
	@UiHandler("buttonCuiA") 
    void onButtonCuiNsClick(ClickEvent event) {
	    logger.info("Collect Learner Information (Actionable) button was clicked.");
	    onSurveyChoiceSelected(SurveyDialogOption.COLLECTINFO_SCORED);
    }
	
	@UiHandler("buttonAlk") 
    void onButtonAlkClick(ClickEvent event) {
	    logger.info("Assess Learner Knowledge button was clicked.");
	    onSurveyChoiceSelected(SurveyDialogOption.ASSESSLEARNER_STATIC);
    }
	
	@UiHandler("cancelButton")
	void onCancelButtonClick(ClickEvent event) {
	    logger.info("Cancel button was clicked.");
	    onSurveyChoiceSelected(SurveyDialogOption.CANCEL);
	}
	
	/** Callback to send the choice that was selected from the dialog. */
	private AddSurveyDialogChoiceCallback callback = null;
	
	
	/**
	 * Creates a new dialog for selecting a survey context survey 
	 */
	public AddSurveyDialog(AddSurveyDialogChoiceCallback cb) {
	    
	    logger.info("AddSurveyDialog()");
	    
	    
		setWidget(uiBinder.createAndBindUi(this));
		
		
		setGlassEnabled(true);
		
		callback = cb;
		setText(TITLE_ADD_SURVEY);

        
	    setFooterWidget(dialogFooter);
	}
	
	/**
	 * Handler for when a survey choice has been selected from the dialog.
	 * 
	 * @param choice - The choice that was selected from the dialog.
	 */
	private void onSurveyChoiceSelected(SurveyDialogOption choice) {
	    
	    logger.info("Survey Option Selected = " + choice.toString());
	    
	    if (callback != null) {
	        callback.onChoiceSelected(choice);
	    }
	    
        hide();
        
	}
	
	@Override
	public void center() {
	    logger.info("Center() called.");
	    
	    // Hide the tooltips at the start when the dialog is first shown.
	    tooltipAlk.hide();
	    tooltipCuiA.hide();
	    tooltipCuiNa.hide();
	    
	    super.center();
	}


    /**
     * A callback class that will return the option that the user selects from the
     * add survey dialog.
     * 
     * @author nblomberg
     *
     */
	public interface AddSurveyDialogChoiceCallback {
	    
	    void onChoiceSelected(SurveyDialogOption choice);
	}

	
}
