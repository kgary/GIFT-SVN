/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.util;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.logging.Logger;

import generated.course.Course;
import generated.course.MerrillsBranchPoint;
import generated.course.PresentSurvey;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.BsLoadingDialogBox;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ModalDialogBox;
import mil.arl.gift.common.survey.Survey;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.event.course.QuestionBankChangedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.survey.SurveyRpcService;
import mil.arl.gift.tools.authoring.server.gat.client.survey.SurveyRpcServiceAsync;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.SelectSurveyContextCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.SurveyEditorModal;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.AbstractSurveyResources;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.CourseSurveyResources;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.AddSurveyDialog.SurveyDialogOption;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.WarningDialog;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.DeleteSurvey;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.DeleteSurveyContextResult;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.constants.ButtonType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;


/**
 * A widget used to add or edit a question bank survey to use with a course object. From this widget, users can choose
 * to add or edit an existing question bank.   This is similar to the SurveyPicker widget, but is used only for question bank survey types.
 * 
 * @author nblomberg
 */
public class SurveyPickerQuestionBank extends Composite implements
		SelectSurveyContextCallback,
		HasValue<String> {

    private static Logger logger = Logger.getLogger(SurveyPickerQuestionBank.class.getName());
    
	private static SurveyPickerUiBinder uiBinder = GWT
			.create(SurveyPickerUiBinder.class);

	interface SurveyPickerUiBinder extends UiBinder<Widget, SurveyPickerQuestionBank> {
	}

	@UiField
	protected Button editSurveyButton;

	@UiField
	protected Panel surveyChoiceInner;

	@UiField
	protected Widget surveyChoicePanel;
	
	private ModalDialogBox confirmRemoveDialog = new ModalDialogBox();
	
	@UiHandler("editSurveyButton") 
	void onEditSurveyButtonClick(ClickEvent event) {
	    logger.info("EditSurvey button clicked.");

	    if (surveyResources != null) {
	        final int contextId = surveyResources.getSurveyContextId();
	        
	        if (contextId > 0) {
	            SurveyEditorModal.getSurveyFromContextKey(getValue(), contextId, new AsyncCallback<Survey>() {

		                @Override
		                public void onFailure(Throwable t) {
		                    displayError("Failed to get a survey", "An exception was thrown by the server: " + t.getMessage());
		                }

		                @Override
		                public void onSuccess(Survey result) {
	                    
	                    if (result != null) {       	
	                    	onLoadExistingSurveyWithSameContextKey(result);
	                    	
	                    } else {
	                    	onAddNewQuestionBankSurvey();
	                    }
	                    
	                }
	                
	            });
	        } else {
	            displayError("Invalid value", "Unable to get the survey context from the database.  The context Id is not valid.");
	        }
	        
	    } else {
	        displayError("Invalid value", "Unable to get the survey context from the database.  The course object data is not valid.");
	    }
	    
	}

	/** The set of global survey resources (i.e. survey context, concepts, etc.) that should be referred to while editing surveys */
	private AbstractSurveyResources surveyResources = null;

	/**
	 * The name of the survey that is displayed in the editor panel. This can be
	 * a default name.
	 */
	private String transitionName = "";

	/** The key for the survey that is currently selected*/
	private String value = null;
	
	/** optional command to execute once the question bank survey has been found */
	private Command surveyLoadedCommand = null;
	
	/**
     * RPC service that is used to retrieve the surveys from the database
     */
    private final SurveyRpcServiceAsync rpcService = GWT.create(SurveyRpcService.class);
    
    /**
     * The HTML that is presented in the confirmDialog
     */
    
    private HTML confirmMessage = new HTML();
    /** The ID of the currently loaded survey context */
    private BigInteger currentSurveyContextId = null;

	/**
	 * Creates a new survey picker
	 */
	public SurveyPickerQuestionBank() {
		initWidget(uiBinder.createAndBindUi(this));

		confirmRemoveDialog.setGlassEnabled(true);
		confirmRemoveDialog.setText("Delete Survey?");
		
		FlowPanel footer = new FlowPanel();
		
		Button yesButton = new Button("Delete Content", new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				
				deleteSelectedSurvey(false);
				
				confirmRemoveDialog.hide();
			}
		});
		
		yesButton.setType(ButtonType.DANGER);
		footer.add(yesButton);

		Button cancelButton = new Button("Cancel", new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				confirmRemoveDialog.hide();
			}
		});
		
		cancelButton.setType(ButtonType.PRIMARY);
		footer.add(cancelButton);
		
		confirmRemoveDialog.setFooterWidget(footer);
		
	}

	public void onAddNewQuestionBankSurvey() {
		// This should only be set for new surveys, not existing surveys.
	    // NOTE - Setting the survey context key to null here so that the survey editor will generate a new one based
        // on the existing survey that has been selected from the survey selection dialog.
	    
	    if (surveyResources.hasConcepts()) {
	        SurveyEditorModal.initializeAndShow(SurveyDialogOption.ASSESSLEARNER_QUESTIONBANK, surveyResources,
	                getTransitionName(), null, this);
	    } else {
	        // display an error to inform the user that they must add concepts before adding the survey.
	        displayError("No course concepts", "This course currently has no concepts.  Please add some concepts to the course before adding a Question Bank Survey.");
	    }
		

	}
	
	/**
	 * Displays an error to the user and logs the error to the browser console.
	 * 
	 * @param title - the title of the error dialog
	 * @param errorStr - The error message to be displayed.
	 */
	private void displayError(String title, String errorStr) {
	    logger.severe(errorStr);
        WarningDialog.error(title, errorStr);
	}
	
	/**
	 * Loads an existing survey and keeps the same survey context key.
	 * 
	 * @param survey - The survey to be loaded.
	 */
	private void onLoadExistingSurveyWithSameContextKey(Survey survey) {
	    SurveyEditorModal.initializeAndShow(surveyResources, survey, getTransitionName(), value, this, false);
	}

	/**
	 * Accessor to set the course object data for this survey.
	 * 
	 * @param currentCourse - The current course object data.
	 */
	public void setCourseData(Course currentCourse) {
		surveyResources = new CourseSurveyResources(currentCourse);
		
		String affectedTransitions = "<ul>";
		for(Serializable transition : currentCourse.getTransitions().getTransitionType()) {
		    //Other Merrills Branch Points
		    if(transition instanceof MerrillsBranchPoint) {
		        MerrillsBranchPoint mbp = (MerrillsBranchPoint) transition;
		        affectedTransitions += "<li>" + mbp.getTransitionName() + "</li>";
		    }
		    
		    //Surveys with Question Banks
		    if(transition instanceof PresentSurvey) {
		        PresentSurvey survey = (PresentSurvey) transition;
		        if(survey.getSurveyChoice() instanceof PresentSurvey.ConceptSurvey) {
		            affectedTransitions += "<li>" + survey.getTransitionName() + "</li>";
		        }
		    }
		}
		affectedTransitions += "</ul>";
		
		confirmRemoveDialog.remove(confirmMessage);
		
		confirmMessage = new HTML(
                "Do you wish to <b>permanently</b> delete the course question bank?"
                + "<br/><br/>"
                + "The following course objects will be affected:<br/>"
                + affectedTransitions
        );
		
		confirmMessage.getElement().getStyle().setPadding(10, Unit.PX);
        confirmMessage.getElement().getStyle().setProperty("maxWidth", "700px");
        
        confirmRemoveDialog.add(confirmMessage);
	}

	/**
	 * Sets the survey transition name for the survey choice panel.
	 * 
	 * @param name
	 *            - The name of the survey.
	 */
	public void setTransitionName(String name) {

		if (name != null) {
			this.transitionName = name;
		}
	}

	/**
	 * Gets the survey transition name for the survey choice panel.
	 * 
	 * @return String - the survey transition name for the survey choice panel.
	 */
	public String getTransitionName() {
		return this.transitionName;
	}
	
	/**
	 * Set the command to execute once the survey is loaded for the course object (not necessarily loaded into the survey composer).
	 * @param newCommand can be null
	 */
    public void setSurveyLoadedCommand(Command newCommand) {
        surveyLoadedCommand = newCommand;
    }

	public void updateDisplay() {

		if (value != null) {

			surveyChoicePanel.addStyleName("presentSurveyChoice-Selected");

			surveyChoiceInner.setVisible(true);
			editSurveyButton.setVisible(true);
			
			if(surveyLoadedCommand != null){
			    surveyLoadedCommand.execute();
			}
		}
	}

	@Override
	public HandlerRegistration addValueChangeHandler(
			ValueChangeHandler<String> handler) {
		
		return addHandler(handler, ValueChangeEvent.getType());
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public void setValue(String value) {
		setValue(value, false);
	}

	@Override
	public void setValue(String value, boolean fireEvents) {

		if (value == this.value
				|| (this.value != null && this.value.equals(value))) {
			return;
		}

		String before = this.value;
		this.value = value;

		updateDisplay();

		if (fireEvents) {
			ValueChangeEvent.fireIfNotEqual(this, before, value);
		}
	}

    @Override
    public void onSurveyContextSelected(String surveyContextKey, int surveyContextId, Survey survey) {

        if (surveyContextKey != null && !surveyContextKey.isEmpty()) {
        	
        	String oldKey = value;
        	
            setValue(surveyContextKey, true);
            
            if(surveyContextKey.equals(oldKey)){
            	
            	//the survey's name might have changed even if the GIFT key is the same, so update the display just in case
            	updateDisplay();
            	
            	SharedResources.getInstance().getEventBus().fireEvent(new QuestionBankChangedEvent(this));
            }
        }
        
    }
    
    /**
     * Deletes the currently selected survey from the database and removes it from the course's survey context
     * 
     * @param deleteResponses whether or not the survey's learner responses should be deleted alongside it
     */
    private void deleteSelectedSurvey(boolean deleteResponses){
    	
    	if(value != null && !GatClientUtility.isReadOnly()){
    		
    		if(currentSurveyContextId != null){
    		
	    		DeleteSurvey action = new DeleteSurvey(
						GatClientUtility.getUserName(),
						value,
						currentSurveyContextId,					
						deleteResponses,
						GatClientUtility.getServerProperties().getBypassSurveyPermissionsCheck()
				);
				
				BsLoadingDialogBox.display("Deleting Survey", "Please wait while GIFT deletes this survey.");
				
				SharedResources.getInstance().getDispatchService().execute(action, new AsyncCallback<DeleteSurveyContextResult>() {
	
					@Override
					public void onFailure(Throwable caught) {
						
						BsLoadingDialogBox.remove();
						
						OkayCancelDialog.show(
								"Unable to Delete Survey", 
								"GIFT was unable to delete this survey. " 
								+ (caught.getMessage() != null ? caught.getMessage() : "An unknown problem prevented the deletion.")
								+ "<br/><br/>"
								+ "Do you still want to remove this survey reference without deleting the survey?", 
								"Yes, Remove Reference", 
								new OkayCancelCallback() {
							
							@Override
							public void okay() {
								setValue(null, true);
							}
							
							@Override
							public void cancel() {
								//Nothing to do
							}
						});
					}
	
					@Override
					public void onSuccess(DeleteSurveyContextResult result) {
						
						BsLoadingDialogBox.remove();
						
						if(result.isSuccess()){	
							
							setValue(null, true);
							saveCourse();
							
							SharedResources.getInstance().getEventBus().fireEvent(new QuestionBankChangedEvent(SurveyPickerQuestionBank.this));
							
						} else {
							
							if(result.hadSurveyResponses()){
								
								OkayCancelDialog.show(
										"Delete Survey Responses?", 
										"GIFT was unable to delete this survey's resources. " 
										+ (result.getErrorMsg() != null ? result.getErrorMsg() : "One or more survey elements have "
												+ "learner responses that need to be deleted first.")
										+ "<br/><br/>"
										+ "Do you want to delete these responses and continue deleting this survey?", 
										"Yes, Delete Responses", 
										new OkayCancelCallback() {
									
									@Override
									public void okay() {
										deleteSelectedSurvey(true);
									}
									
									@Override
									public void cancel() {
										//Nothing to do
									}
								});
								
							} else {
							
								OkayCancelDialog.show(
										"Unable to Delete Survey", 
										"GIFT was unable to delete this survey. " 
										+ (result.getErrorMsg() != null ? result.getErrorMsg() : "An unknown problem prevented the deletion.")
										+ "<br/><br/>"
										+ "Do you still want to remove this survey reference without deleting the survey?", 
										"Yes, Remove Reference", 
										new OkayCancelCallback() {
									
									@Override
									public void okay() {
										setValue(null, true);
									}
									
									@Override
									public void cancel() {
										//Nothing to do
									}
								});
							}
						}		
					}
				});
				
    		} else {
    			
    			WarningDialog.error("Unable to Delete Survey", 
    					"GIFT was unable to delete this survey because this course's survey resources were not loaded properly."
    					+ "<br/><br/>"
    					+ "Please try reloading this course to see if the problem persists. "
    			);
    		}
    	}
    }
    
    /**
     * Sets survey context ID this widget should use when the user attempts to delete a survey
     * 
     * @param contextId the survey context ID
     */
    public void setSurveyContextId(BigInteger contextId){
		currentSurveyContextId = contextId;
	}
    
    /**
     * Saves the course in the background so it no longer references
     * a deleted object if the user cancels the editor instead of saving
     */
    private native static void saveCourse()/*-{
	
		if($wnd.saveCourse != null){
			$wnd.saveCourse();
		} 
	
	}-*/;
}
