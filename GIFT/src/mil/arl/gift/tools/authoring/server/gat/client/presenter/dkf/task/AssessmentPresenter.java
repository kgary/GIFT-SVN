/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.presenter.dkf.task;

import generated.dkf.Assessments;
import generated.dkf.Assessments.ConditionAssessment;
import generated.dkf.Assessments.Survey;
import generated.dkf.Question;
import generated.dkf.Questions;
import generated.dkf.Reply;
import generated.dkf.Scenario;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.survey.AbstractQuestion;
import mil.arl.gift.common.survey.AbstractSurveyElement;
import mil.arl.gift.common.survey.AbstractSurveyQuestion;
import mil.arl.gift.common.survey.Constants;
import mil.arl.gift.common.survey.ListOption;
import mil.arl.gift.common.survey.MatrixOfChoicesQuestion;
import mil.arl.gift.common.survey.MultipleChoiceQuestion;
import mil.arl.gift.common.survey.RatingScaleQuestion;
import mil.arl.gift.common.survey.SurveyContextSurvey;
import mil.arl.gift.common.survey.SurveyPage;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.event.EditorDirtyEvent;
import mil.arl.gift.tools.authoring.server.gat.client.model.record.AssessmentRecord;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.shared.AssessmentEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.SelectQuestionDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.WarningDialog;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;


/**
 * A presenter used to populate the assessment editor and handle its events.
 */
public class AssessmentPresenter {
	
	/** The Constant logger. */
	private static final Logger logger = Logger.getLogger(AssessmentPresenter.class.getName());
		
	/** The editor. */
	private AssessmentEditor editor;
	
	/** The handler registrations. */
	private List<HandlerRegistration> handlerRegistrations = new ArrayList<HandlerRegistration>();
			
	/*
	 * TODO: For now this list of assessments doesn't do much since the assessment editor currently only allows users to edit one assessment.
	 * In the future, however, there should be a way to present the list of assessments to the user (such as a DataGrid) so that they can
	 * add, delete, and edit assessments as they wish.
	 */
	/** The assessments. */
	private Assessments assessments;
	
	/** The assessments. */
	private Scenario currentScenario = null;
	
	/*
	 * TODO: Once the assessment editor allows users to edit multiple assessments, all logic concerning this assessment should ideally go into 
	 * a separate dialog that is displayed whenever a user tries to edit or add a single assessment.
	 */
	/** The assessment. */
	private Serializable assessment;
	
	/** The gift surveys. */
	private List<SurveyContextSurvey> giftSurveys = new ArrayList<SurveyContextSurvey>();
	
	/** The no assessment radio input. */
	HasValue<Boolean> noAssessmentRadioInput;
	
	/** The condition assessment radio input. */
	HasValue<Boolean> conditionAssessmentRadioInput;
	
	/** The survey assessment radio input. */
	HasValue<Boolean> surveyAssessmentRadioInput;

	/** The survey select input. */
	HasChangeHandlers surveySelectInput;

	/** The question assessment data display. */
	HasData<AssessmentRecord> questionAssessmentDataDisplay;
	
	/** The question assessment data provider. */
	ListDataProvider<AssessmentRecord> questionAssessmentDataProvider = new ListDataProvider<AssessmentRecord>();
	
	/**  The question assessment selection model. */
	MultiSelectionModel<AssessmentRecord> questionAssessmentSelectionModel = new MultiSelectionModel<AssessmentRecord>();

	/** The input for adding a question. */
	HasClickHandlers addQuestionButtonInput;
	
	/** The clickable input for deleting a question. */
	HasClickHandlers deleteQuestionButtonClickInput;
	
	/** The enablable input for deleteing a question. */
	HasEnabled deleteQuestionButtonEnabledInput;
	
	private Survey currentSurveyAssessment = null;
	
	/** true if the current user has permission to the selected dkf context */
	private boolean hasPermissionToContext;
	
	private SelectQuestionDialog selectQuestionDialog = new SelectQuestionDialog();
	
	/**
	 * Instantiates a new dkf task trigger presenter.
	 *
	 * @param editor the editor
	 */
	AssessmentPresenter(AssessmentEditor editor) {
		
		this.editor = editor;

		init();
	}
	
	/**
	 * Initializes the assessment presenter.
	 */
	void init() {
		
		
		noAssessmentRadioInput = editor.getNoAssessmentRadioInput();
		HandlerRegistration handler = noAssessmentRadioInput.addValueChangeHandler(new ValueChangeHandler<Boolean>(){

			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				
				if(!event.getValue()){
					
					if(assessment instanceof Survey){
						currentSurveyAssessment = (Survey) assessment;
					}
					
					assessment = null;	
					
					if(!assessments.getAssessmentTypes().isEmpty()){
						assessments.getAssessmentTypes().clear();
					}					
					
					populateAssessmentEditor();
					
				} else {
					
					if(editor.getConditionAssessmentRadioInput().getValue().booleanValue()){
						
						if(assessment instanceof Survey){
							currentSurveyAssessment = (Survey) assessment;
						}
						
						assessment = new ConditionAssessment();		
						
						assessments.getAssessmentTypes().clear();
						assessments.getAssessmentTypes().add(assessment);
						
						populateAssessmentEditor();
					
					} else {
						
						if(currentSurveyAssessment != null){
							assessment = currentSurveyAssessment;	
							
						} else {
							assessment = new Survey();
							
							((Survey) assessment).setGIFTSurveyKey(null);		
							
							((Survey) assessment).setQuestions(new Questions());
						}
						
						assessments.getAssessmentTypes().clear();
						assessments.getAssessmentTypes().add(assessment);
						
						populateAssessmentEditor();
					}
				}
				
				SharedResources.getInstance().getEventBus().fireEvent(new EditorDirtyEvent());
			}
			
		});
		handlerRegistrations.add(handler);
		
		conditionAssessmentRadioInput = editor.getConditionAssessmentRadioInput();
		handler = conditionAssessmentRadioInput.addValueChangeHandler(new ValueChangeHandler<Boolean>(){

			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				
				if(event.getValue()){
					
					if(assessment instanceof Survey){
						currentSurveyAssessment = (Survey) assessment;
					}
					
					assessment = new ConditionAssessment();		
					
					//Ed LaFave commented out this logic because it doesn't appear to be valid.
					//However, there is a good chance that I don't understand some uses cases so
					//I left the code here in case it needs to be commented back in.
					/*if(assessments.getAssessmentTypes().isEmpty()){
						assessments.getAssessmentTypes().add(assessment);
					}*/
					assessments.getAssessmentTypes().clear();
					assessments.getAssessmentTypes().add(assessment);
					
					populateAssessmentEditor();
				}
				
				SharedResources.getInstance().getEventBus().fireEvent(new EditorDirtyEvent());
			}
			
		});
		handlerRegistrations.add(handler);
		
		surveyAssessmentRadioInput = editor.getSurveyAssessmentRadioInput();
		handler = surveyAssessmentRadioInput.addValueChangeHandler(new ValueChangeHandler<Boolean>(){

			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				
				if(event.getValue()){
					
					if(currentSurveyAssessment != null){
						assessment = currentSurveyAssessment;	
						
					} else {
						assessment = new Survey();
						
						((Survey) assessment).setGIFTSurveyKey(null);		
						
						((Survey) assessment).setQuestions(new Questions());
					}
					
					assessments.getAssessmentTypes().clear();
					assessments.getAssessmentTypes().add(assessment);
					
					populateAssessmentEditor();
				}
				
				SharedResources.getInstance().getEventBus().fireEvent(new EditorDirtyEvent());
			}
			
		});
		handlerRegistrations.add(handler);

		handler = editor.getSurveySelectInput().addValueChangeHandler(new ValueChangeHandler<String>(){

			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				
				if(assessment instanceof Survey){
					
					((Survey) assessment).setGIFTSurveyKey(event.getValue());	
					selectQuestionDialog.setSurveyGIFTKey(event.getValue());
				
					//remove any questions that were added previously, as they are no longer valid
					((Survey) assessment).setQuestions(new Questions());
					
					clearAssessmentEditor();
					refreshAssessmentEditor();
				}
				
				SharedResources.getInstance().getEventBus().fireEvent(new EditorDirtyEvent());
			}
			
		});
		handlerRegistrations.add(handler);
		
		deleteQuestionButtonEnabledInput = editor.getDeleteQuestionButtonEnabledInput();
		deleteQuestionButtonEnabledInput.setEnabled(false);

		questionAssessmentDataDisplay = editor.getQuestionAssessmentDataDisplay();
		questionAssessmentDataProvider.addDataDisplay(questionAssessmentDataDisplay);
		
		questionAssessmentDataDisplay.setSelectionModel(questionAssessmentSelectionModel);
		handler = questionAssessmentSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
			
			@Override
			public void onSelectionChange(SelectionChangeEvent event) {
				
				for(AssessmentRecord record : questionAssessmentSelectionModel.getSelectedSet()){
					
					//Select the question above the selected reply
					if(record.getQuestionOrReply() instanceof Reply){
						
						for(int i = questionAssessmentDataProvider.getList().indexOf(record) - 1; i >= 0; i--){
							
							AssessmentRecord nextRecord = questionAssessmentDataProvider.getList().get(i);
							
							if(nextRecord.getQuestionOrReply() instanceof Question){
								
								record = nextRecord;
								questionAssessmentSelectionModel.setSelected(record, true);
								
								break;
							}				
						}
					}
							
					//Select all replies below the question
					for(int i = questionAssessmentDataProvider.getList().indexOf(record) + 1; i < questionAssessmentDataProvider.getList().size(); i++){
						
						AssessmentRecord nextRecord = questionAssessmentDataProvider.getList().get(i);
						
						if(nextRecord.getQuestionOrReply() instanceof Question){
							break;
						}
						
						questionAssessmentSelectionModel.setSelected(nextRecord, true);
					}
				}
				
				deleteQuestionButtonEnabledInput.setEnabled(true);
			}
		});
		handlerRegistrations.add(handler);

		addQuestionButtonInput = editor.getAddQuestionButtonInput();
		handler = addQuestionButtonInput.addClickHandler(new ClickHandler(){

			@Override
			public void onClick(ClickEvent event) {
				
				if(currentScenario != null 
						&& currentScenario.getResources() != null){
					
					selectQuestionDialog.setSurveyContextId(currentScenario.getResources().getSurveyContext());
					
				} else {
					selectQuestionDialog.setSurveyContextId(null);
				}
				
				selectQuestionDialog.setValue(null);
				selectQuestionDialog.center();
			}
			
		});
		handlerRegistrations.add(handler);
		
		handlerRegistrations.add(selectQuestionDialog.addValueChangeHandler(new ValueChangeHandler<AbstractSurveyQuestion<?>>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<AbstractSurveyQuestion<?>> event) {
				
				if(event.getValue() == null){
					WarningDialog.error("Selection missiong", "Please select a question to add.");
				}
					
				addQuestion(event.getValue());
				
				selectQuestionDialog.hide();

			}
		}));
		
		deleteQuestionButtonClickInput = editor.getDeleteQuestionButtonClickInput();
		handler = deleteQuestionButtonClickInput.addClickHandler(new ClickHandler(){

			@Override
			public void onClick(ClickEvent arg0) {
				deleteQuestion();
			}
			
		});
		handlerRegistrations.add(handler);
	}

	/**
	 * Stop.
	 */
	public void stop() {
		
		for(HandlerRegistration reg: handlerRegistrations) {
			reg.removeHandler();
		}
		handlerRegistrations.clear();
	}

	/**
	 * Sets the assessments.
	 *
	 * @param assessments the new assessments
	 */
	public void setAssessments(Assessments assessments, Scenario currentScenario) {
		this.assessments = assessments;	
		this.currentScenario = currentScenario;
		
		currentSurveyAssessment = null;
		
		//TODO: Once multiple assessments are supported, this block should be removed.
		Serializable assessment;
		
		editor.setEnabled(true);
		
		if(assessments.getAssessmentTypes().isEmpty()){
			assessment = null;
			
		} else if(assessments.getAssessmentTypes().size() > 1){
			
			editor.setEnabled(false);
			
			assessment = assessments.getAssessmentTypes().get(0);
		
		} else {			
			assessment = assessments.getAssessmentTypes().get(0);
		}
		
		//TODO: Once multiple assessments are supported, this call should be triggered by some input instead of being called here.
		editAssessment(assessment);
	}		
	
	/**
	 * Edits the assessment.
	 *
	 * @param assessment the assessment
	 */
	public void editAssessment(Serializable assessment){
		
		this.assessment = assessment;
		
		String surveyChoice = editor.getSelectedSurveyChoice();
		
		if(giftSurveys != null 
				&& assessment != null 
				&& assessment instanceof Survey){
			
			if(((Survey) assessment).getGIFTSurveyKey() == null || ((Survey) assessment).getGIFTSurveyKey().isEmpty()){
			
				for(SurveyContextSurvey giftSurveyContextSurvey : giftSurveys){
					
					if(giftSurveyContextSurvey.getKey().equals(surveyChoice)){
						
						((Survey) assessment).setGIFTSurveyKey(giftSurveyContextSurvey.getKey());
						
					} else {
						
						((Survey) assessment).setGIFTSurveyKey(null);
					}
				}
			}
		}
		
		populateAssessmentEditor();
	}
	
	/**
	 * Clears the assessment editor.
	 */
	private void clearAssessmentEditor(){	
		
		questionAssessmentDataProvider.getList().clear();
	}
	
	/**
	 * Refreshes the assessment editor.
	 */
	private void refreshAssessmentEditor(){
		questionAssessmentDataProvider.refresh();
	}

	/**
	 * Populates the assessment editor.
	 */
	private void populateAssessmentEditor() {
		
		clearAssessmentEditor();
		refreshAssessmentEditor();
		
		if(assessment == null){
			editor.showNoneAssessmentPanel();
			noAssessmentRadioInput.setValue(false);
			editor.showNoneAssessmentPanel();
			
		}else if(assessment instanceof ConditionAssessment){
			editor.showConditionAssessmentPanel();
			conditionAssessmentRadioInput.setValue(true);
			noAssessmentRadioInput.setValue(true);
			editor.showHasAssessmentPanel();
			
		} else if(assessment instanceof Survey){
			
			Survey surveyAssessment = (Survey) assessment;
			
			editor.showSurveyAssessmentPanel();
			surveyAssessmentRadioInput.setValue(true);
			
			noAssessmentRadioInput.setValue(true);
			editor.showHasAssessmentPanel();
			
			selectQuestionDialog.setSurveyGIFTKey(surveyAssessment.getGIFTSurveyKey());			
			
			if(giftSurveys != null && !giftSurveys.isEmpty()){
				
				if(surveyAssessment.getGIFTSurveyKey() != null){
					
				    
				   
				    editor.setSelectedSurveyChoice(surveyAssessment.getGIFTSurveyKey(), false);
					
					SurveyContextSurvey foundSurvey = null;
					
					for(SurveyContextSurvey giftSurveyContextSurvey : giftSurveys){
						
						if(surveyAssessment.getGIFTSurveyKey().equals(giftSurveyContextSurvey.getKey())){	
							
						    // Set the label to be the name of the survey rather than the name of the survey context key.
						    editor.setSelectedSurveyChoiceLabel(giftSurveyContextSurvey.getSurvey().getName());
						    
						    
							editor.hideSurveyWarning();
							
							foundSurvey = giftSurveyContextSurvey;
							if(!hasPermissionToContext && !foundSurvey.getSurvey().getVisibleToUserNames().contains(GatClientUtility.getUserName()) && !foundSurvey.getSurvey().getVisibleToUserNames().contains(Constants.VISIBILITY_WILDCARD)){
								editor.setSelectedSurveyChoice("(You do not have permissions to this Survey Context or the selected Survey) - " + surveyAssessment.getGIFTSurveyKey());
							}
							else if(!hasPermissionToContext && (foundSurvey.getSurvey().getVisibleToUserNames().contains(GatClientUtility.getUserName()) || foundSurvey.getSurvey().getVisibleToUserNames().contains(Constants.VISIBILITY_WILDCARD))){
								editor.setSelectedSurveyChoice("(You do have permission to the survey but not the dkf Survey Context) - " + surveyAssessment.getGIFTSurveyKey());
							}
							break;
						}
						
					} 
					
					if(surveyAssessment.getQuestions() != null){
					
						for(Question question : surveyAssessment.getQuestions().getQuestion()){
														
							
							AbstractQuestion foundQuestion = null;
							
							if(foundSurvey != null && foundSurvey.getSurvey() != null){
							
								for(SurveyPage page : foundSurvey.getSurvey().getPages()){
									
									for(AbstractSurveyElement element : page.getElements()){
										
										if(element instanceof AbstractSurveyQuestion<?>){
											
											AbstractQuestion giftQuestion = ((AbstractSurveyQuestion<?>) element).getQuestion();
											
											if(question.getKey() != null && question.getKey().intValue() == element.getId()){
												
												AssessmentRecord record = new AssessmentRecord(question, giftQuestion.getText());
												questionAssessmentDataProvider.getList().add(record);													
												
												foundQuestion = giftQuestion;
												
												break;
											}
										}										
										
										if(foundQuestion != null){
											break;
										}
									}
									
									if(foundQuestion != null){
										break;
									}
								}
							}
							
							if(foundQuestion == null){
								
								SafeHtmlBuilder sb = new SafeHtmlBuilder();
								sb.appendHtmlConstant("<div class='warningLabel'><u>Invalid</u><br/>Not present in database.</div>");
								
								AssessmentRecord record = new AssessmentRecord(
										question, 
										"Question ID = " + (question.getKey() != null ? question.getKey().toString() : "None"),
										sb.toSafeHtml());						
								
								questionAssessmentDataProvider.getList().add(record);
							
							}
							
							List<ListOption> listOptionList = new ArrayList<ListOption>();
							
							if(foundQuestion != null){
							
								if(foundQuestion instanceof MultipleChoiceQuestion){				
									
									listOptionList.addAll(((MultipleChoiceQuestion) foundQuestion).getReplyOptionSet().getListOptions());
									
								} else if (foundQuestion instanceof MatrixOfChoicesQuestion){
									
									listOptionList.addAll(((MatrixOfChoicesQuestion) foundQuestion).getColumnOptions().getListOptions());
									listOptionList.addAll(((MatrixOfChoicesQuestion) foundQuestion).getRowOptions().getListOptions());
								
								} else if(foundQuestion instanceof RatingScaleQuestion){
									
									listOptionList.addAll(((RatingScaleQuestion) foundQuestion).getReplyOptionSet().getListOptions());
								}				
							}
							
							for(Reply reply : question.getReply()){
								
								boolean foundReply = false;
								
								for(ListOption listOption : listOptionList){
									
									if(reply.getKey() != null && reply.getKey().intValue() == listOption.getId()){	
										
										AssessmentRecord replyRecord = new AssessmentRecord(
												reply, 
												listOption.getText());
										
										questionAssessmentDataProvider.getList().add(replyRecord);
										
										foundReply = true;
										
										break;
									}
								}
								
								if(!foundReply){
									
									SafeHtmlBuilder sb = new SafeHtmlBuilder();
									sb.appendHtmlConstant("<div class='warningLabel'><u>Invalid</u><br/>Not present in database.</div>");
									
									AssessmentRecord replyRecord = new AssessmentRecord(
											reply, 
											"Reply ID = " + (reply.getKey() != null ? reply.getKey().toString() : "None"),
											sb.toSafeHtml());
									
									questionAssessmentDataProvider.getList().add(replyRecord);
								}
							}
							
						}
					}
				}
			} else {			
				editor.setSelectedSurveyChoice(surveyAssessment.getGIFTSurveyKey(), false);
			}

			refreshAssessmentEditor();
			
		} else {
			logger.fine("Encountered unknown assessment type of '" + assessment.getClass().getSimpleName() + "' while populating the assessment editor.");
		}
	}
	
	/**
	 * Adds a question to a survey assessment.
	 */
	private void addQuestion(AbstractSurveyQuestion<?> surveyQuestion) {
		
		if(assessment instanceof Survey && surveyQuestion != null && surveyQuestion.getQuestion() != null){
			
			AbstractQuestion question = surveyQuestion.getQuestion();
				
			if(question instanceof MultipleChoiceQuestion
					|| question instanceof MatrixOfChoicesQuestion 
					|| question instanceof RatingScaleQuestion){
				
				Question addQuestion = new Question();
				addQuestion.setKey(BigInteger.valueOf(surveyQuestion.getId()));
				
				((Survey) assessment).getQuestions().getQuestion().add(addQuestion);
				
				AssessmentRecord record = new AssessmentRecord(addQuestion, question.getText());
				questionAssessmentDataProvider.getList().add(record);
				
				
				List<ListOption> listOptionList = new ArrayList<ListOption>();
				
				if(question instanceof MultipleChoiceQuestion){				
					
					listOptionList.addAll(((MultipleChoiceQuestion) question).getReplyOptionSet().getListOptions());
					
				} else if (question instanceof MatrixOfChoicesQuestion){
					
					listOptionList.addAll(((MatrixOfChoicesQuestion) question).getColumnOptions().getListOptions());
					listOptionList.addAll(((MatrixOfChoicesQuestion) question).getRowOptions().getListOptions());
				
				} else if(question instanceof RatingScaleQuestion){
					
					listOptionList.addAll(((RatingScaleQuestion) question).getReplyOptionSet().getListOptions());
				}
				
				for(ListOption listOption : listOptionList){
					
					Reply reply = new Reply();
					
					reply.setKey(BigInteger.valueOf(listOption.getId()));
					reply.setResult(AssessmentLevelEnum.VALUES().get(0).getDisplayName());
					
					addQuestion.getReply().add(reply);
					
					AssessmentRecord replyRecord = new AssessmentRecord(reply, listOption.getText());
					questionAssessmentDataProvider.getList().add(replyRecord);
				}
				
				questionAssessmentDataProvider.refresh();
				
				SharedResources.getInstance().getEventBus().fireEvent(new EditorDirtyEvent());
			}
			
		} else {
			logger.warning("Tried to add question when assessment is not of type 'Survey'.");
		}
	}
	
	/**
	 * Deletes a question to a survey assessment.
	 */
	private void deleteQuestion() {
		
		if(assessment instanceof Survey){
			
			final List<Question> selectedQuestions = new ArrayList<Question>();
			final List<AssessmentRecord> selectedRecords = new ArrayList<AssessmentRecord>();
		
			for(AssessmentRecord record : questionAssessmentSelectionModel.getSelectedSet()){
				
				if(record.getQuestionOrReply() instanceof Question){					
					selectedQuestions.add((Question) record.getQuestionOrReply());				
				}	
				
				selectedRecords.add(record);
			}		
			
			boolean isSingular = selectedQuestions.size() == 1;
			
			StringBuilder sb = new StringBuilder();
			if(isSingular){
				sb.append("Are you sure you want to remove the selected question from this survey assessment?");
				
			} else {
				sb.append("Are you sure you want to remove the selected questions from this survey assessment?");
			}
			
			OkayCancelDialog.show(
					"Delete " + (isSingular ? "Question" : "Questions") + "?", 
					sb.toString(), 
					"Yes, delete " + (isSingular ? " this question" : " these questions"),
					new OkayCancelCallback() {
						
						@Override
						public void okay() {
							
							((Survey) assessment).getQuestions().getQuestion().removeAll(selectedQuestions);
							questionAssessmentDataProvider.getList().removeAll(selectedRecords);
							
							questionAssessmentDataProvider.refresh();
							
							deleteQuestionButtonEnabledInput.setEnabled(!questionAssessmentDataProvider.getList().isEmpty());
							
							SharedResources.getInstance().getEventBus().fireEvent(new EditorDirtyEvent());
						}
						
						@Override
						public void cancel() {
							//Nothing to do
						}
					});
			
			
		}
	}
	
	

	/**
	 * Sets the surveys.
	 *
	 * @param giftSurveys the new surveys
	 * @param hasPermissions true if the user has permissions to the current survey context
	 * @param surveyContextId the id of the selected survey context
	 */
	public void setSurveys(List<SurveyContextSurvey> giftSurveys, boolean hasPermissions, int surveyContextId) {
		hasPermissionToContext = hasPermissions;
		this.giftSurveys = giftSurveys;
		
		List<String> surveyNames = new ArrayList<String>();
		if(hasPermissions){
			if(giftSurveys != null) {
	
			    for(SurveyContextSurvey survey : giftSurveys){
			    	if(survey.getSurvey().getVisibleToUserNames().contains(GatClientUtility.getUserName()) || survey.getSurvey().getVisibleToUserNames().contains(Constants.VISIBILITY_WILDCARD)){
			    		surveyNames.add(survey.getKey());
			    	}
			    }
			}
		}
		
		editor.setSelectedSurveyContext(BigInteger.valueOf(surveyContextId));
		
		if(!surveyNames.isEmpty()){
			
			if(assessment != null && assessment instanceof Survey){
				
				if(((Survey) assessment).getGIFTSurveyKey() != null){
			
					if(editor.getSelectedSurveyChoice() != null && 
							!editor.getSelectedSurveyChoice().equals(((Survey) assessment).getGIFTSurveyKey())) {
						
						editor.setSelectedSurveyChoice(((Survey) assessment).getGIFTSurveyKey());
					}	
				}
			}
		}
		
		populateAssessmentEditor();
	}
	
}
