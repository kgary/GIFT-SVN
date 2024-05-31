/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.presenter.dkf;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.URL;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;

import generated.dkf.Actions;
import generated.dkf.Actions.InstructionalStrategies;
import generated.dkf.Actions.StateTransitions;
import generated.dkf.ApplicationCompletedCondition;
import generated.dkf.Assessment;
import generated.dkf.AvailableLearnerActions;
import generated.dkf.ChildConceptEnded;
import generated.dkf.Concept;
import generated.dkf.Concepts;
import generated.dkf.Condition;
import generated.dkf.Conditions;
import generated.dkf.EndTriggers;
import generated.dkf.Input;
import generated.dkf.Objects;
import generated.dkf.Resources;
import generated.dkf.Scenario;
import generated.dkf.Task;
import generated.dkf.Tasks;
import mil.arl.gift.common.course.CourseValidationResults;
import mil.arl.gift.common.enums.DeploymentModeEnum;
import mil.arl.gift.common.enums.TrainingApplicationEnum;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.BsLoadingDialogBox;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.DetailsDialogBox;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ModalDialogBox;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ModalDialogCallback;
import mil.arl.gift.common.gwt.client.widgets.file.CancelCallback;
import mil.arl.gift.common.gwt.shared.DocumentUtil;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileUtil;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.enums.CourseObjectNames.CourseObjectName;
import mil.arl.gift.tools.authoring.server.gat.client.event.EditorDirtyEvent;
import mil.arl.gift.tools.authoring.server.gat.client.event.SurveyContextSelectedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.event.course.CourseConceptsChangedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.event.dkf.ScenarioLoadedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.place.DkfPlace;
import mil.arl.gift.tools.authoring.server.gat.client.presenter.AbstractGatPresenter;
import mil.arl.gift.tools.authoring.server.gat.client.survey.SurveyRpcService;
import mil.arl.gift.tools.authoring.server.gat.client.survey.SurveyRpcServiceAsync;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.DkfView;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.DefaultGatFileSaveAsDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.DefaultGatFileSelectionDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.ErrorDetailsDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.FileValidationDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.WarningDialog;
import mil.arl.gift.tools.authoring.server.gat.shared.CopySurveyContextResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.SaveJaxbObjectResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.LockFileResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.dkf.LockDkf;
import mil.arl.gift.tools.authoring.server.gat.shared.action.dkf.SaveDkf;
import mil.arl.gift.tools.authoring.server.gat.shared.action.dkf.UnlockDkf;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.FetchJAXBObject;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.FetchJAXBObjectResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.ValidateFile;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.ValidateFileResult;


/**
 * Top level presenter for DKFs.
 *
 * @author cragusa
 */
public class DkfPresenter extends AbstractGatPresenter implements DkfView.Presenter {
	
	/**
	 * The Interface MyEventBinder.
	 */
	interface MyEventBinder extends EventBinder<DkfPresenter> {
	}
	
	/** The logger. */
	private static Logger logger = Logger.getLogger(DkfPresenter.class.getName());

	/** The Constant eventBinder. */
	private static final MyEventBinder eventBinder = GWT
			.create(MyEventBinder.class);
		
	/** The dkf path. */
	private String filePath;
	
	/** The current scenario. */
	private Scenario currentScenario;
	
	/** The style name for the gat <hr> tag which is defined in the GiftAuthoringTool.css file */
    private static final String GAT_HR_STYLE = "gathr";
	
	/** The place controller. */
	@Inject
	private PlaceController placeController;	

	/** The dkf view. */
	@Inject
	private DkfView dkfView;
	
	/** The is dkf dirty. */
	private boolean isDkfDirty;
	
	/** whether or not this activity should allow editing */
    private boolean readOnly;
    
    /** whether or not a new file should be created based on the file path */
    private boolean createNewFile;
    
    /** 
     * special condition if a file is locked and another user tries to open the file
     * should currently only happen in desktop mode when users can see each other's workspaces
     */
    private boolean fileAlreadyLocked = false;
	
	/**
	 * File New dialog.
	 */
	private DefaultGatFileSaveAsDialog fileNewDialog;
	
    
    /** The survey context id for the course that the dkf belongs to. */
    private BigInteger courseSurveyContextId = BigInteger.ZERO;
    
    /** Boolean to indicate if the user selects an existing dkf file and is in the process of importing the dkf. */
    private boolean isImportedDkf = false;
    
    /** Boolean to indicate if the user selected an existing dkf file and is in the process of importing the dkf from the GIFT Wrap page. */
    private boolean isGIFTWrap = false;

    /**
     * Boolean to indicate if the user is using this DKF to run a playback log
     */
    private boolean isPlayback = false;
    
    /**
     * Boolean to indicate if the DKF is being used as remediation content
     */
    private boolean isRemediation = false;

    /**
     * RPC service that is used to retrieve the surveys from the database
     */
    private final SurveyRpcServiceAsync rpcService = GWT.create(SurveyRpcService.class);
	
	/** Heartbeat timer so the server doesn't unlock the file we're editing. */
	private Timer lockTimer = new Timer() {
		@Override
		public void run() {
			if(filePath != null) {
				lockFile(filePath, false);
			}
		}
	};
	
	/**
	 * Instantiates a new dkf presenter.
	 */
	@Inject
	public DkfPresenter() {
		super();
		exposeNativeFunctions();
	}
		
	/* (non-Javadoc)
	 * @see mil.arl.gift.tools.authoring.gat.client.view.DkfView.Presenter#confirmStop()
	 */
	@Override
	public String confirmStop() {
//	   if(isDkfDirty) {
//	       return createUnsavedMessage();
//	   } else {
//		   return null;
//	   }
		//Nick: Set to null for now to prevent the "Are you sure" dialog from showing
		return null;
	}
	
	/**
     * Create a message to show to the user indicating the current document has unsaved changes.
     * 
     * @return the message to display
     */
    private String createUnsavedMessage(){
        return "There are unsaved changes to '"+filePath+"' that will be discarded.";
    }

	/**
	 * Creates the valid scenario object.
	 *
	 * @param scenarioName the name of the scenario, not including file path or file extension 
	 * @return the scenario
	 */
	private Scenario createValidScenarioObject(String scenarioName){
		
		final String SCENARIO_NAME = scenarioName;
        final String SCENARIO_DESC = "This is the simplest DKF possible. Update this text with a description of your DKF.";
        
        final BigInteger TASK_ID = BigInteger.valueOf(1);
        final String TASK_NAME = "Complete App Task";
        final BigInteger CONCEPT_ID = BigInteger.valueOf(2);
        final String CONCEPT_NAME = "Complete App Concept";
        final String DEFAULT_COMPLETION_TIME = "00:30:00";
        
        final String DEFAULT_CONDITION_IMPL = "domain.knowledge.condition.ApplicationCompletedCondition";
        
		Scenario scenario = new Scenario(); {
			
			scenario.setName(SCENARIO_NAME);
			scenario.setDescription(SCENARIO_DESC);
			scenario.setEndTriggers(null);

			Actions actions = new Actions(); {
				InstructionalStrategies strategies = new InstructionalStrategies();
				actions.setInstructionalStrategies(strategies);
				StateTransitions transitions = new StateTransitions();
				actions.setStateTransitions(transitions);
			}			
			scenario.setActions(actions);
			


			Assessment assessment = new Assessment(); {				
				Objects objects = new Objects();
				assessment.setObjects(objects);
				
				Tasks tasks = new Tasks(); {
				    
				    Task task = new Task();
				    task.setName(TASK_NAME);
				    task.setNodeId(TASK_ID);
				    
				    EndTriggers endTriggers = new EndTriggers(); {
				        ChildConceptEnded childConceptEnded = new ChildConceptEnded();
				        childConceptEnded.setNodeId(CONCEPT_ID);
				        EndTriggers.Trigger trigger = new EndTriggers.Trigger();
				        trigger.setTriggerType(childConceptEnded);
				        endTriggers.getTrigger().add(trigger);
				    }				    
				    task.setEndTriggers(endTriggers);
				    
				    Concepts concepts = new Concepts();  {				        
		                Concept concept = new Concept();
                        concept.setName(CONCEPT_NAME);
		                concept.setNodeId(CONCEPT_ID);
		                
		                Conditions conditions = new Conditions(); {
		                    
		                    Condition condition = new Condition();		                    
		                    condition.setConditionImpl(DEFAULT_CONDITION_IMPL);
		                    Input input = new Input(); {
		                        
		                        ApplicationCompletedCondition applicationCompletedCondition = new ApplicationCompletedCondition();
		                        applicationCompletedCondition.setIdealCompletionDuration(DEFAULT_COMPLETION_TIME);		                        
		                        input.setType(applicationCompletedCondition);
		                    }
		                    condition.setInput(input);
		                    
		                    conditions.getCondition().add(condition);
		                }	
		                
		                concept.setConditionsOrConcepts(conditions);
		                concepts.getConcept().add(concept);
				    }
				    task.setConcepts(concepts);
				    
				    tasks.getTask().add(task);			    
				}				
				
				assessment.setTasks(tasks);
			}
			scenario.setAssessment(assessment);

			Resources resources = new Resources(); {
				AvailableLearnerActions learnerActions = new AvailableLearnerActions();
				resources.setAvailableLearnerActions(learnerActions);
				//it's okay for survey context to be null
				
				// Set the survey context from the course object.  The user cannot edit this.
				resources.setSurveyContext(courseSurveyContextId);
			}
			scenario.setResources(resources);
		}
		
		return scenario;
	}

	/**
	 * Update the header html.
	 */
	private void updateHeaderHtml() {
//		StringBuilder builder = new StringBuilder();
//	        
//		if(currentScenario != null){
//			
//		    String filename = "not saved";
//		    if(filePath != null) {
//		    	filename = filePath.substring(Math.max(filePath.lastIndexOf('/'), filePath.lastIndexOf('\\')) + 1, filePath.length());
//			}
//		    
//		    String version  = currentScenario.getVersion();
//		    if(version == null) {
//		    	version = "not set";
//		}
//		
//		    String dkfName = currentScenario.getName();
//	
//	    
//		    builder.append("<div style=\"padding: 5px;\">");
//		    builder.append("").append(dkfName);
//		    builder.append("<hr class='").append(GAT_HR_STYLE).append("'/>");
//		    builder.append("File: ").append(filename);
//		    builder.append("<hr class='").append(GAT_HR_STYLE).append("'/>");
//		    builder.append("Ver: ").append(version);
//		    builder.append("<hr class='").append(GAT_HR_STYLE).append("'/>");
//		    builder.append("</div>");
//	    }
//		dkfView.getDkfHeaderView().setHTML(builder.toString());
	}
	
	/**
	 * Gets the Scenario JAXB object corresponding to dkf file given by the specified path.
	 * 
	 * @param path the path to the DKF file from which to get the Scenario
	 * @param isGiftWrap whether the dkf presenter is being launched from gift wrap UI instead of the course creator
	 */
	private void doGetScenarioObject(final String path, boolean isGiftWrap) {
		final String msg = "doGetScenarioObject";
		logger.info(msg +" on path '"+path+"'");
		
		if(path == null || path.equals("")) {
    		//I don't know why showFileNewDialog can't be called directly but
    		//I think there is an issue with the file browser classes.
    		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
				@Override
				public void execute() {
					showFileNewDialog();
				}
			});
    		return;
    	}

		if(createNewFile) {
			String scenarioName = CourseObjectName.DKF.getDisplayName();
			currentScenario = createValidScenarioObject(scenarioName);
			filePath = path;
			saveDkf(path, false);
			loadDkf();
			
			return;
		}
		
		AsyncCallback<FetchJAXBObjectResult> callback = new AsyncCallback<FetchJAXBObjectResult>(){

			@Override
			public void onFailure(Throwable t) {
				handleCallbackFailure(logger, msg, t);
				GatClientUtility.cancelModal();
			}

			@Override
			public void onSuccess(FetchJAXBObjectResult result) {
				if(result.isSuccess()){
					
					currentScenario = (Scenario)result.getJAXBObject();
					if(!fileAlreadyLocked){
						setReadOnly(!result.getModifiable());
					}
					setFilePath(path);
					loadDkf();					
					showWaiting(false);
					
					if(result.wasConversionAttempted() && result.getModifiable()) {
						
    	    			WarningDialog.warning("File conversion", "The file you've loaded was created with an old version of GIFT, "
    	    					+ "but we were able to update it for you. This file has already been saved and "
    	    					+ "is ready for modification. A backup of the original file was created: " 
    	    					+ result.getFileName() + FileUtil.BACKUP_FILE_EXTENSION);
    	    			
    	    			//if the file was converted, we need to save it after the conversion
    	    			saveDkf(path, false);
					}

				} else {
					
					final ErrorDetailsDialog dialog = new ErrorDetailsDialog(
							result.getErrorMsg(), 
							result.getErrorDetails(), 
							result.getErrorStackTrace());				
					dialog.setText("Failed to Load File");
					
					dialog.addCloseHandler(new CloseHandler<PopupPanel>() {
						
						@Override
						public void onClose(CloseEvent<PopupPanel> event) {
							GatClientUtility.cancelModal();
						}
					});
					
					Scheduler.get().scheduleDeferred(new ScheduledCommand() {
						
						@Override
						public void execute() {
							showWaiting(false);
							dialog.center();
						}
					});
				}
						
				logger.fine("Got scenario object for DKF at '" + path + "'.");
			}		
		};
						
		logger.info("Getting scenario object for DKF at '" + path + "'");
		
		String userName = GatClientUtility.getUserName();
					
		FetchJAXBObject action = new FetchJAXBObject();
		action.setRelativePath(path);
		action.setUserName(userName);
		action.setUseParentAsCourse(isGiftWrap);
		showWaiting(true);
						
		dispatchService.execute(action, callback);
	}
						
	/**
	 * Loads the DKF
	 */
	private void loadDkf() {
	    
		logger.info("loadDkf() called.");
		
        ScenarioClientUtility.setScenario(currentScenario);
        ScenarioClientUtility.setIsPlayback(isPlayback);
        ScenarioClientUtility.setIsRemediation(isRemediation);

		// Check to see if the user has imported the dkf and has just selected an existing dkf from the 'Select Existing' dialog.
		if (currentScenario.getResources() != null) {
		    Resources resources = currentScenario.getResources();
		    if (resources.getSurveyContext() == null || resources.getSurveyContext().equals(BigInteger.ZERO)) {
		        // if an older dkf did not have a survey context associated with it, it will be given the survey context
		        // that the course uses now, since the user can no longer select it.
		        currentScenario.getResources().setSurveyContext(courseSurveyContextId);
		        
		        logger.info("Dkf did not have a survey context, so setting the dkf to use the course survey context.");
		    } else if (resources.getSurveyContext() != null && resources.getSurveyContext() != courseSurveyContextId) {

		        if (isImportedDkf) {		            
		            // Copy the surveys used in the DKF into the current course survey context.
		            
		            Set<String> giftKeys = new HashSet<String>(0);
		            if(currentScenario.getAssessment() != null && currentScenario.getAssessment().getTasks() != null){
		                
                        for(generated.dkf.Task task : currentScenario.getAssessment().getTasks().getTask()){
                            
                            if(task.getAssessments() != null){
                                
                                for(Serializable assessmentType : task.getAssessments().getAssessmentTypes()){
                                    
                                    if(assessmentType instanceof generated.dkf.Assessments.Survey){
                                        
                                        generated.dkf.Assessments.Survey survey = (generated.dkf.Assessments.Survey)assessmentType;
                                        
                                        if(survey.getGIFTSurveyKey() != null && !survey.getGIFTSurveyKey().isEmpty()){
                                            giftKeys.add(survey.getGIFTSurveyKey());
                                        }
                                    }
                                }
                            }
                            
                            if(task.getConcepts() != null){
                                getNodeAssessmentSurveyKeys(task.getConcepts(), giftKeys);
                            }
                        }
		            }

		            
		            if(!giftKeys.isEmpty()){
		                logger.info("Dkf is being imported and the dkf survey context does not match the course survey context, copying the "+giftKeys.size()+" surveys referenced in the DKF into the survey context.");

                        final int currentSurveyContextId = resources.getSurveyContext().intValue();
		                copySurveysIntoSurveyContext(currentSurveyContextId, giftKeys, courseSurveyContextId.intValue());
		            }
		            
		        } else {
		            // This is okay, this means that the survey context does not match the course survey context, but the user
		            // has chosen to leave the file and settings 'as-is'.  We don't want to keep prompting the user to 
		            // copy the surveys in this case.
		            logger.info("Survey Context does not match the course survey context.");
		        }
		    } else {
		        logger.info("currentScenario.getResources() is null, not setting the survey context.");
		    }		    
		    
		}
		
		ScenarioClientUtility.getConditionsThatCanComplete(new AsyncCallback<Set<String>>() {

            @Override
            public void onFailure(Throwable error) {
                complete();
            }

            @Override
            public void onSuccess(Set<String> result) {
                complete();
            }
            
            private void complete(){
                
                updateHeaderHtml();
                
                setDirty(false);
                
                dkfView.getScenarioOutline().edit(currentScenario);
                dkfView.getObjectEditorPanel().setScenario(currentScenario);
                
                eventBus.fireEvent(new ScenarioLoadedEvent(currentScenario));
                
                //Hide the file loading dialog now that the dkf has loaded
                BsLoadingDialogBox.remove();
                
                redefineSaveEmbeddedCourseObject();
                
                logger.info("Finished loadDkf()");
            }
        });
					
		logger.info("waiting for server to reply to getConditionsThatCanComplete call before finish loading dkf...");
	}
	
	/**
	 * Recursively look for survey gift keys in the assessment portions of the concepts elements. 
	 * 
	 * @param concepts the dkf concepts to descend into looking for gift key values.  If null this method
	 * adds no entries to the provided set of gift keys.
	 * @param giftKeys the collection containing the unique set of gift keys that were found.  Can't be null.
	 */
	private void getNodeAssessmentSurveyKeys(generated.dkf.Concepts concepts, Set<String> giftKeys){
	    
	    if(concepts == null){
	        return;
	    }
	    
        for(generated.dkf.Concept concept : concepts.getConcept()){
            
            if(concept.getAssessments() != null){
                for(Serializable assessmentType : concept.getAssessments().getAssessmentTypes()){
                    
                    if(assessmentType instanceof generated.dkf.Assessments.Survey){
                        
                        generated.dkf.Assessments.Survey survey = (generated.dkf.Assessments.Survey)assessmentType;
                        
                        if(survey.getGIFTSurveyKey() != null && !survey.getGIFTSurveyKey().isEmpty()){
                            giftKeys.add(survey.getGIFTSurveyKey());
                        }
                    }
                }
            }
            
            if(concept.getConditionsOrConcepts() instanceof generated.dkf.Concepts){
                getNodeAssessmentSurveyKeys((generated.dkf.Concepts)concept.getConditionsOrConcepts(), giftKeys);
            }
        }
	}
	
	
	/**
	 * Calls the server to copy the surveys from an imported dkf into the course survey context.
	 * 
	 * @param srcSurveyContextId - The source survey context id that the surveys will be copied from.
	 * @param srcSurveyContextGiftKeys - the gift keys of the surveys on the source survey context to be copied
	 * @param destSurveyContextId - The destination survey context id that the surveys will be copied to.
	 */
	private void copySurveysIntoSurveyContext(int srcSurveyContextId, Set<String> srcSurveyContextGiftKeys, int destSurveyContextId) {
	    
	    BsLoadingDialogBox.display("Copying Survey(s)", "Coping the surveys from the original dkf into the course.  Please wait.");
	    rpcService.copySurveyContext(srcSurveyContextId, srcSurveyContextGiftKeys, destSurveyContextId, GatClientUtility.getUserName(), new AsyncCallback<CopySurveyContextResult>() {

            @Override
            public void onFailure(Throwable t) {
                logger.info("copySurveyContext() failed. A throwable was returned: " + t.getMessage());
                BsLoadingDialogBox.remove();
                
                WarningDialog.error("Failed to copy surveys", "Unable to copy the surveys from the Real-Time Assessment file.  The server returned a severe error (" + t.getMessage() + 
                                    ").", new ModalDialogCallback() {

                                        @Override
                                        public void onClose() {
                                            // Close the dkf presenter since this is a critical error, and clear out the dkf file
                                            // since it is not valid.
                                            GatClientUtility.cancelModal(true);
                                        }
                    
                });
                
                
            }

            @Override
            public void onSuccess(CopySurveyContextResult result) {
                BsLoadingDialogBox.remove();
                
                if (result != null) {
                    int surveyContextId = result.getSurveyContext().getId();
                    logger.info("Copy successful.  Updated this course survey context "+ courseSurveyContextId.toString()+" with dkf references surveys from survey context " + surveyContextId);
                    currentScenario.getResources().setSurveyContext(BigInteger.valueOf(surveyContextId));
                    updateSurveyContextKeys(currentScenario.getAssessment(), result.getSurveyIdMapping());
                    
                    // Let other editors know that a new survey context has been selected.
                    eventBus.fireEvent(new SurveyContextSelectedEvent(result.getSurveyContext()));

                    logger.info("SurveyId map is size of: " + result.getSurveyIdMapping().size());
                    
                    WarningDialog.alert("Successful copy", "This Real-Time Assessment file survey references have been linked properly " + 
                                        "within the current course by creating copies of the surveys from the original file for your current course." + 
                                        "<br/><br/><font color='red'><b>You will need to re-add any question assessments to the " + 
                                        "Real-Time Assessment file.</b></font>", new ModalDialogCallback() {

                                            @Override
                                            public void onClose() {
                                                // Save the dkf file with the updated values.
                                                saveDkf(filePath, false);
                                            }
                        
                    });
                    
                    
                } else {
                    logger.info("copySurveyContext() failed. Survey Context returned was null.");
                    
                    WarningDialog.error("Unable to copy surveys", "Unable to copy the surveys from the Real-Time Assessment file. The server returned an invalid " + 
                                        " survey context.", new ModalDialogCallback() {

                                            @Override
                                            public void onClose() {
                                                // Close the dkf presenter since this is a critical error, and clear out the dkf file
                                                // since it is not valid.
                                                GatClientUtility.cancelModal(true);
                                                
                                            }
                        
                    });
                }
                
            }
	        
	    });
	}

	/**
	 * Goes through the DKF Task/Concepts and updates any old gift survey key references to use the
	 * new key values have been created on the server so that the dkf is using new survey references and is
	 * not sharing survey references from the original course.
	 * 
	 * @param rootAssessment - The root assessment element from the dkf.
	 * @param surveyIdMapping - Mapping of Old to New GIFT Survey Keys, where Old is the original gift survey context key and the 
     *                          New is the survey context key that should be used in the dkf instead.
	 */
	protected void updateSurveyContextKeys(Assessment rootAssessment, HashMap<String, String> surveyIdMapping) {
	    logger.info("updateSurveyContextKeys() called.");
	    if (rootAssessment == null) {
	        logger.severe("rootAssessment is null.");
	        return;
	    }
	    
	    if (surveyIdMapping == null) {
	        logger.severe("surveyIdMapping is null.");
	        return;
	    }
	    
        for (Task task : rootAssessment.getTasks().getTask()) {
            
            if (task == null) {
                continue;
            }
            
            // Go through each concept (this is recursive).
            if (task.getConcepts() != null && task.getConcepts().getConcept() != null) {
                updateConceptAssessmentTypes(task.getConcepts().getConcept(), surveyIdMapping);
            }
            
            
            // Check the task assessments.
            if (task.getAssessments() != null && task.getAssessments().getAssessmentTypes() != null) {
                updateAssessmentTypes(task.getAssessments().getAssessmentTypes(), surveyIdMapping);
            }
        }
        
    }
	
	/**
	 * Recursive function that iterates through each concept and updates any survey assessment
	 * to use the new GIFTSurveyKeys provided by the server.
	 * 
	 * @param conceptList - List of concepts to iterate through.
	 * @param surveyIdMapping - Mapping of Old to New GIFT Survey Keys, where Old is the original gift survey context key and the 
     *                          New is the survey context key that should be used in the dkf instead.
	 */
	private void updateConceptAssessmentTypes(List<Concept> conceptList, HashMap<String, String> surveyIdMapping) {	    
	    if (conceptList == null) {
	        return;
	    }
	    
	    for (Concept concept : conceptList) {
	        
	        if (concept == null) {
	            continue;
	        }
	        
	        if (concept.getAssessments() != null && concept.getAssessments().getAssessmentTypes() != null) {
	            updateAssessmentTypes(concept.getAssessments().getAssessmentTypes(), surveyIdMapping);
	        }
            
            
            if (concept.getConditionsOrConcepts() != null && concept.getConditionsOrConcepts() instanceof Concepts) {
                Concepts concepts = (Concepts)concept.getConditionsOrConcepts();
                updateConceptAssessmentTypes(concepts.getConcept(), surveyIdMapping);
                
            }
        }
	}
	
	/**
	 * Goes through an assessment list (from concepts or tasks) and updates any old survey context key values to use
	 * the new survey context key values from the server.
	 * 
	 * @param assessmentList - List of assessments to iterate over.
	 * @param surveyIdMapping - Mapping of Old to New GIFT Survey Keys, where Old is the original gift survey context key and the 
	 *                          New is the survey context key that should be used in the dkf instead.
	 */
	private void updateAssessmentTypes(List<Serializable> assessmentList, HashMap<String, String> surveyIdMapping) {
	    
	    if (assessmentList == null) {
	        return;
	    }
	    
	    for(Serializable assessment : assessmentList){
	        if (assessment == null) {
	            continue;
	        }
	        
            if (assessment instanceof generated.dkf.Assessments.Survey) {
                generated.dkf.Assessments.Survey surveyAssessment = (generated.dkf.Assessments.Survey)assessment;
                
                // Swap the key values.
                if (surveyIdMapping.containsKey(surveyAssessment.getGIFTSurveyKey())) {
                    String newKey = surveyIdMapping.get(surveyAssessment.getGIFTSurveyKey());
                    
                    logger.info("Setting old key: " + surveyAssessment.getGIFTSurveyKey() + " to new key: " + newKey);
                    surveyAssessment.setGIFTSurveyKey(newKey);
                    
                    if (surveyAssessment.getQuestions() != null && surveyAssessment.getQuestions().getQuestion() != null) {
                        logger.info("Clearing assessment questions.");
                        surveyAssessment.getQuestions().getQuestion().clear();
                    }
                }
                
            }
        }
	}

    /**
	 * Save the dkf.
	 *
	 * @param path the path to save the file to.
	 * @param showConfirmationDialog whether or not to display the confirmation dialog
	 */
	private void saveDkf(final String path, final boolean showConfirmationDialog) {
		saveDkf(path, showConfirmationDialog, false);
	}
	
	/**
	 * Sets the current embedded course object to be saved and is
	 * called by nested editors to reset the value as well. 
	 */
	public void redefineSaveEmbeddedCourseObject(){
		GatClientUtility.defineSaveEmbeddedCourseObject(createSaveObject(filePath));
	}
	
	/**
     * Save the dkf.
     *
     * @param path the path to save the file to.
     * @param showConfirmationDialog whether or not to display the confirmation
     *        dialog
     * @param validateFile whether or not to validate the file after saving
     */
	private void saveDkf(final String path, final boolean showConfirmationDialog, final boolean validateFile) {
	    if (logger.isLoggable(Level.FINE)) {
            StringBuilder sb = new StringBuilder("saveDkf(");
            List<Object> params = Arrays.<Object>asList(path, showConfirmationDialog, validateFile);
            StringUtils.join(", ", params, sb);
            logger.fine(sb.append(")").toString());
        }

		final String msg = "saveDkf";
		
		if(readOnly && path != null && filePath != null && path.equals(filePath)){
			WarningDialog.warning("Read only", "Cannot overwrite original file while viewing a read-only copy. <br/>"
					+ "<br/>"
					+ "If you wish to save this DKF to a different file, please specify another file name.");
			return;
		}

		AsyncCallback<SaveJaxbObjectResult> callback = new AsyncCallback<SaveJaxbObjectResult>() {
			@Override
			public void onFailure(Throwable t) {
				showWaiting(false);     				
				
				if(filePath != null){
					handleCallbackFailure(logger, msg, t);
					
				} else {
					
					fileNewDialog.hide();
					fileNewDialog.center();
					
					WarningDialog.error("DKF creation", "Unable to create DKF: " + t.getLocalizedMessage());
				}
			}

			@Override
			public void onSuccess(SaveJaxbObjectResult result) {                     
				showWaiting(false);

				if(result.isSuccess()) {                                    
					logSuccess(logger, msg);
					
					fileNewDialog.hide();
					
					//Let the user know the save result.
					if(showConfirmationDialog) {
						String message = "File saved successfully!";
						if(!result.isSchemaValid()) {
							message += "\n\nThe authored content is not valid against the schema. Therefore, this file should not be used with GIFT until the contents are correct. Using this incomplete file can result in GIFT not executing correctly.";
						}
						WarningDialog.info("Invalid content", message);
					}
					
					//This condition is true when we load a file from the
					//server and then use the Save-As functionality to save as
					//a different file. In this case it is our responsibility
					//to release the lock on the original file since we're no
					//longer using it.
					if(filePath != null && !filePath.equals(path)) {
						
						if(readOnly){
							setReadOnly(false);
						}
						
						unlockFile();
					}
					
					//When we ask the server to save a dkf it generates
					//a new version for that dkf before saving. The newly
					//generated version is based on the previous version.
					//So if we don't save the newly generated version in the
					//dkf object then the server will generate the same
					//version every time we save.
					String newVersion = result.getNewVersion();
					currentScenario.setVersion(newVersion);
					
					setDirty(false);
					
					setFilePath(path);
					
					updateHeaderHtml();
					
					if(validateFile) {
						validateFile(filePath, GatClientUtility.getUserName());
					}
					
					//notify any listeners that the scenario has finished saving
					GatClientUtility.onScenarioSaved(path);
					
					  if(result.getExceptionsList().isEmpty() == false) {
                      	
                      	List<DetailedException> errorMessages = result.getExceptionsList();
                      	
                      	CourseValidationResults validationResults = new CourseValidationResults("Course");
                      	
                      	validationResults.addWarningIssues(errorMessages);
                      	
                      	DetailsDialogBox dialog = new ErrorDetailsDialog("Issues Found", validationResults, false);
                      	
                          dialog.setTitle("xTSP Export Errors");
                          dialog.setText("xTSP Export Errors");
                          dialog.center();                                                                                                             	
                      }
				}
				else {
											
					fileNewDialog.hide();
					fileNewDialog.center();

					ErrorDetailsDialog dialog = new ErrorDetailsDialog(result.getErrorMsg(), result.getErrorDetails(), result.getErrorStackTrace());
					dialog.setText("Error");
					dialog.center();
				}
			}
		};
		
		//This flag will be false if we're saving to the same file we just
		//opened. It'll be true if we're saving to a brand new file or if we're
		//overwriting an existing file on disk (Save-As).
		boolean acquireInsteadOfRenew = false;
		if(filePath == null || !filePath.equals(path)) {
			acquireInsteadOfRenew = true;
		}
		
		String userName = GatClientUtility.getUserName();

		SaveDkf action = new SaveDkf();
		action.setPath(path);
		action.setScenario(currentScenario);
		action.setAcquireLockInsteadOfRenew(acquireInsteadOfRenew);
		action.setUserName(userName);
		action.setIsGIFTWrap(GatClientUtility.isGIFTWrapMode());
		showWaiting(true);    
		dispatchService.execute(action, callback);      
	}
	
    /**
	 * Tells the server to lock the DKF at the given path.
	 * @param path Path of the DKF to lock.
	 * @param acquisition True if we're trying to acquire the lock for the
	 * first time, false if we're simply renewing the lock.
	 */
	private void lockFile(String path, final boolean acquisition) {

        final String username = GatClientUtility.getUserName();

        /* Can't guarantee that there will be a browser session key when locking DKF files. If it is
         * null, use the username. This can happen if we are accessing the DKF Editor without going
         * through any of the access points that require a user (for example Desktop Mode-System
         * Tray-GIFT Wrap). */
        final String browserSessionKey = GatClientUtility.getBrowserSessionKey() != null
                ? GatClientUtility.getBrowserSessionKey()
                : username;

        // This callback does absolutely nothing. In the case of acquiring the
        //lock that is the appropriate course of action. However, there seem to
        //be two ways to fail to acquire the lock:
        //
        //  1.) After saving a brand new file OR executing a Save-As, another
        //  user sees that DKF in the dashboard and acquires the lock before
        //  you.
        //  2.) The communication to the server simply failed.
        //
        //For the first case we'd probably want to tell the user what happened
        //and jump back to the dashboard. The second case is less clear,
        //perhaps we'd want to do the same.
        //
        //TODO Handle the failure case.
        AsyncCallback<LockFileResult> callback =  new AsyncCallback<LockFileResult>() {
            @Override
            public void onFailure(Throwable t) {
                // nothing to do
            }
            @Override
            public void onSuccess(LockFileResult result) {
                
                if(result.isSuccess()) {
                    
                    boolean isReadOnly = !result.getCourseFileAccessDetails().userHasWritePermission(username,
                        browserSessionKey);
                    
                    ScenarioClientUtility.setReadOnly(isReadOnly);
                
                } else {
                    logger.warning("Failed to re-acquire lock for editing DKF. Reason:" + result.getErrorMsg());
                }
            }
        };
        
                        
        //Try to lock the DKF.
        LockDkf lockDkf = new LockDkf();
        lockDkf.setRelativePath(path);
        lockDkf.setUserName(username);
        lockDkf.setBrowserSessionKey(browserSessionKey);
        
        dispatchService.execute(lockDkf, callback);
	}
	
    /**
	 * Unlocks the current DKF.
	 */
	private void unlockFile() {
		
		if(!readOnly){
			//This callback does absolutely nothing. In the case of releasing the
			//lock that is the appropriate course of action. It seems the only way
			//the lock wouldn't be released is if communication to the server
			//fails. The probability of that is very low but if we don't handle
			//it then that DKF will be locked forever.
			//
			//TODO Handle the failure case.
			AsyncCallback<GatServiceResult> callback =  new AsyncCallback<GatServiceResult>() {
		        @Override
				public void onFailure(Throwable t) {
		        	// nothing to do
	    	    }
		        @Override
				public void onSuccess(GatServiceResult result) {
		        	// nothing to do
		        }
		    };
		    
		    String userName = GatClientUtility.getUserName();

            /* Can't guarantee that there will be a browser session key when
             * DKF files were locked. If it is null, use the username. This can
             * happen if we are accessing the DKF Editor without going through
             * any of the access points that require a user (for example Desktop
             * Mode-System Tray-GIFT Wrap). */
            final String browserSessionKey = StringUtils.isNotBlank(GatClientUtility.getBrowserSessionKey())
                    ? GatClientUtility.getBrowserSessionKey()
                    : userName;
		    
		    //Try to lock the DKF before we open it.
		    UnlockDkf unlockDkf = new UnlockDkf(filePath);
		    unlockDkf.setUserName(userName);
		    unlockDkf.setBrowserSessionKey(browserSessionKey);
		    
		    dispatchService.execute(unlockDkf, callback);
		}
	}
	
	/* (non-Javadoc)
	 * @see mil.arl.gift.tools.authoring.gat.client.presenter.AbstractGatPresenter#getLogger()
	 */
	@Override
	protected Logger getLogger() {
		return logger;
	}
			
				
	/**
	 * Go to.
	 *
	 * @param place the place
	 */
	private void goTo(Place place) {
		placeController.goTo(place);
	}
	
	private void createNewFile(String path) {
		String scenarioName = fileNewDialog.getFileNameTextBox().getValue();
		currentScenario = createValidScenarioObject(scenarioName);
		saveDkf(path, false);
		loadDkf();
	}
	
	/**
	 * Inits the.
	 *
	 */
	@Inject
	private void init() {
	    
	    logger.fine("DkfPresenter init()");
	    
	    dkfView.getScenarioOutline().addSelectionHandler(new SelectionHandler<Serializable>() {
            
            @Override
            public void onSelection(SelectionEvent<Serializable> event) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("onSelection(" + event + ")");
                }
                
                //if an object is selected in the scenario outline, open an editor for it
                dkfView.getObjectEditorPanel().startEditing(event.getSelectedItem());
            }
        });
	}
	
	/**
	 * Initializes the file new dialog based on the deployment mode. 
	 * In this case, if the dkf editor is launched in server mode, then
	 * the dialog should not allow the user to save outside of the username folder.
	 * 
	 * @param mode - The deployment mode that the application is in (eg. SERVER/DESKTOP/EXPERIMENT)
	 */
	private void initFileNewDialog(DeploymentModeEnum mode) {
	    
	    if (mode == DeploymentModeEnum.SERVER) {
            fileNewDialog = new DefaultGatFileSaveAsDialog(true);
	    } else {
            fileNewDialog = new DefaultGatFileSaveAsDialog(false);
        }
        
	    fileNewDialog.setAllowedFileExtensions(new String[]{AbstractSchemaHandler.DKF_FILE_EXTENSION});
	    fileNewDialog.setIntroMessageHTML(""
                +   "<div style='padding: 10px; font-weight: bold;'>"
                +       "Please double-click the course folder you would like to associate this DKF with to select it.<br/>"
                +       "<br/>"
                +       "Once your course is selected, you can optionally create a subfolder to place the DKF in."
                +   "</div>");
        fileNewDialog.setConfirmButtonText("Create");
        fileNewDialog.setFileNameLabelText("DKF Name:");
        fileNewDialog.setText("New DKF");
        fileNewDialog.setCloseOnConfirm(false);
        
        
        // Setup handler registrations.
        HandlerRegistration registration = null;
        
        registration = fileNewDialog.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                final String path = getFileNewPath();
                if(path == null) {
                    fileNewDialog.reallowConfirm();
                    return;
                }
                
                //Checks for illegal characters in the name of the file.
                String validationMsg = DocumentUtil.validateFileName(fileNewDialog.getFileNameTextBox().getValue());
                if(validationMsg != null){
                	WarningDialog.illegalCharactersWarning(validationMsg);
                	fileNewDialog.reallowConfirm();
                	return;
                }
                
                //If the path is within the course folder then the first
                //element will be the user name, the second will be the course
                //folder, and the last element will be the file name.
                int length = path.split("/").length;
                if(length < 3) {
                    
                    WarningDialog.warning("Invalid path", "All new files must be created inside a course folder.");
                    
                    fileNewDialog.reallowConfirm();
                    return;
                }
                
                int index = path.lastIndexOf("/") + 1;
                String fileName = path.substring(index);
                boolean exists = fileNewDialog.isFileOrFolderInCurrentDirectory(fileName);
                if(!exists || path.equals(filePath)) {
                    createNewFile(path);
                } else {
                    OkayCancelDialog.show("Confirm Overwrite", "A DKF named '" + fileName +"' already exists at '" + path + "'. "
                            + "Would you like to overwrite this DKF?", "Overwrite", new OkayCancelCallback() {
                        @Override
                        public void okay() {
                            createNewFile(path);
                        }
                        
                        @Override
                        public void cancel() {
                            fileNewDialog.reallowConfirm();
                        }
                    });
                }
            }
        });
        handlerRegistrations.add(registration);
        
        fileNewDialog.setCancelCallback(new CancelCallback() {
            
            @Override
            public void onCancel() {
                if(filePath == null) {
                    fileNewDialog.hide();
                    GatClientUtility.cancelModal();
                }
            }
        });
	}
	

	
	private void showFileNewDialog() { 
		fileNewDialog.clearFileName();
		fileNewDialog.center();
	}
    
    private String getFileNewPath() {
    	String path = fileNewDialog.getValue();
    	if(path == null) {
    		return null;
    	}
    	
    	if(!path.endsWith(AbstractSchemaHandler.DKF_FILE_EXTENSION)) {
			path = path.concat(AbstractSchemaHandler.DKF_FILE_EXTENSION);
		}
		return path;
    }
	
	private Serializable findSibling(Serializable node, ListDataProvider<Serializable> listDataProvider) {
	    
		Serializable previousNode = null;
	    
		Iterator<Serializable> iter = listDataProvider.getList().iterator();
		while(iter.hasNext()) {
	    
			Serializable currentNode = iter.next();
			if(currentNode.equals(node)) {
				if( iter.hasNext() ) {	
					Serializable sibling = iter.next();
					return sibling;
					
				} else {	
					return previousNode;
	}
	
			} else {
				previousNode = currentNode;
			}				
		}
	    
		return null;
	        }
	    
	/**
	 * On the editor dirty event.
	 *
	 * @param event the event
	 */
	@EventHandler
	protected void onEditorDirty(EditorDirtyEvent event) {
	    setDirty(true);
	
	    //If the header information changed then we have to update the view.
	    updateHeaderHtml();
	}

	/**
	 * On discard.
	 */
	private void onDiscard() {
		doGetScenarioObject(filePath, false);
		dkfView.hideDkfObjectModal();
	}
	
	/* (non-Javadoc)
	 * @see mil.arl.gift.tools.authoring.gat.client.view.DkfView.Presenter#start(com.google.gwt.user.client.ui.AcceptsOneWidget, mil.arl.gift.tools.authoring.gat.shared.id.DkfId)
	 */
	@Override
	public void start(final AcceptsOneWidget containerWidget, HashMap<String, String> startParams) {		

		super.start();    
		
		logger.info("start() called with params: " + startParams);
		
		// Setup any start parameters that have been passed in via the url.
		DeploymentModeEnum mode = DeploymentModeEnum.DESKTOP;
		String modeParam = startParams.get(DkfPlace.PARAM_DEPLOYMODE);
		String createParam = startParams.get(DkfPlace.PARAM_CREATENEW);
		String surveyContextParam = startParams.get(DkfPlace.PARAM_SURVEYCONTEXTID);
		String importedDkf = startParams.get(DkfPlace.PARAM_IMPORTEDDKF);
		String dkfReadOnly = startParams.get(DkfPlace.PARAM_READONLY);
        String dkfTrainingApp = startParams.get(DkfPlace.PARAM_TRAINING_APP);
        String giftWrap = startParams.get(DkfPlace.PARAM_GIFTWRAP);
        String playback = startParams.get(DkfPlace.PARAM_PLAYBACK);
        String remediation = startParams.get(DkfPlace.PARAM_REMEDIATION);
		
		if (modeParam != null) {
		    mode = DeploymentModeEnum.valueOf(modeParam);
		} else {
		    logger.severe("Start mode could not be determined.  Defaulting to DESKTOP.");
		}
		
		if(createParam != null) {
			createNewFile = Boolean.valueOf(createParam);
		} else {
			createNewFile = false;
		}
		
		if (surveyContextParam != null) {
		    Integer contextId = null;
		    try {
		       contextId = Integer.parseInt(surveyContextParam);
		    } catch (@SuppressWarnings("unused") NumberFormatException e) {
		        contextId = null;
		    }
		    
		    if (contextId != null) {
		        this.courseSurveyContextId = BigInteger.valueOf(contextId);
		    }
		}
		
		if (importedDkf != null) {
		    isImportedDkf = Boolean.valueOf(importedDkf);
		} else {
		    isImportedDkf = false;
		}
		
		if (giftWrap != null) {
            isGIFTWrap = Boolean.valueOf(giftWrap);
        } else {
            isGIFTWrap = false;
        }

        if (playback != null) {
            isPlayback = Boolean.valueOf(playback);
        } else {
            isPlayback = false;
        }
        
        if (remediation != null) {
            isRemediation = Boolean.valueOf(remediation);
        } else {
            isRemediation = false;
        }

		if (this.courseSurveyContextId == BigInteger.ZERO) {
		    logger.severe("Unable to get the survey context id for the course.  Survey editing in the dkf will not work properly.");
		}
		
		/* 
         * Nick: Need to URL decode the path to prevent an issue in Firefox where URL encoded characters (namely spaces as %20s) aren't 
         * properly decoded before they get to this point
         */
		String encodedPath = startParams.get(DkfPlace.PARAM_FILEPATH);
		final String path = encodedPath != null ? URL.decode(encodedPath) : encodedPath;

		// Initialize the dialogs based on what mode the presenter is deployed in.
		initFileNewDialog(mode);
		
		if(path == null || path.isEmpty() || path.equals("null")) {
			startPart2(containerWidget, path, isGIFTWrap);
		} else {
			
			if(dkfReadOnly != null && Boolean.valueOf(dkfReadOnly)) {
				// This is true if the course was opened in read-only mode. Avoid attempting to acquire the lock for 
				// now because files within a course folder are not locked in read-only mode, and so the lock would 
				// be acquired incorrectly.
				
				setReadOnly(Boolean.valueOf(dkfReadOnly));
				fileAlreadyLocked = Boolean.valueOf(dkfReadOnly);
				startPart2(containerWidget, path, isGIFTWrap);
				
			} else {
			    
			    /* Can't guarantee that there will be a browser session key when locking DKF files. If it is
		         * null, use the username. This can happen if we are accessing the DKF Editor without going
		         * through any of the access points that require a user (for example Desktop Mode-System
		         * Tray-GIFT Wrap). */
		        final String browserSessionKey = StringUtils.isNotBlank(GatClientUtility.getBrowserSessionKey())
		                ? GatClientUtility.getBrowserSessionKey()
		                : GatClientUtility.getUserName();
			
				LockDkf lockDkf = new LockDkf();
				lockDkf.setAcquisition(true);
				lockDkf.setRelativePath(path);
				lockDkf.setBrowserSessionKey(browserSessionKey);
				lockDkf.setUserName(GatClientUtility.getUserName());
				
				dispatchService.execute(lockDkf, new AsyncCallback<LockFileResult>() {
	
					@Override
					public void onFailure(Throwable t) {
					    if (logger.isLoggable(Level.FINE)) {
                            logger.fine("lockDkf.onFailure()");
                        }

						startPart2(containerWidget, path, isGIFTWrap);
						setReadOnly(true);
					}
	
					@Override
					public void onSuccess(LockFileResult result) {
                        if (logger.isLoggable(Level.FINE)) {
                            logger.fine("lockDkf.onSuccess(" + result.isSuccess() + ")");
                        }

						startPart2(containerWidget, path, isGIFTWrap);
						
						boolean isReadOnly = true;
						
						if(result.isSuccess()) {
    						isReadOnly = !result.getCourseFileAccessDetails().userHasWritePermission(GatClientUtility.getUserName(),
    		                        browserSessionKey);
    						
						} else {
						    logger.warning("Failed to lock DKF for editing. Reason:" + result.getErrorMsg());
						}
						
						ScenarioClientUtility.setReadOnly(isReadOnly);
                        
                        setReadOnly(isReadOnly);
                        fileAlreadyLocked = isReadOnly;
					}			
				});
			}
        }

        TrainingApplicationEnum taType = TrainingApplicationEnum.valueOf(dkfTrainingApp);
        ScenarioClientUtility.setTrainingAppType(taType);
	}
	
	/**
	 * Execute the second phase of loading the dkf.
	 * 
	 * @param containerWidget the container widget 
	 * @param path the path to the DKF file from which to get the Scenario 
	 * @param isGiftWrap whether the dkf presenter is being launched from gift wrap UI instead of the course creator
	 */
	private void startPart2(AcceptsOneWidget containerWidget, String path, boolean isGiftWrap) {
	    
	    if(!readOnly) {
	        
    		//Every minute remind the server that the DKF needs to be locked
    		//because we're still working on it.
    		lockTimer.scheduleRepeating(60000);
	    }
		
		setupView(containerWidget, dkfView.asWidget());
		eventRegistration = eventBinder.bindEventHandlers(this, eventBus);
		
		doGetScenarioObject(path, isGiftWrap);
	}
	
	/* (non-Javadoc)
	 * @see mil.arl.gift.tools.authoring.gat.client.presenter.AbstractGatPresenter#stop()
	 */
	@Override
	public void stop() {		
		super.stop();
		
		lockTimer.cancel();
		
		//This method gets called when we're jumping back to the dashboard, we
		//need to unlock the DKF so it is editable again.
		unlockFile();
	}
	
	private void setDirty(boolean dirty) {
		//TODO: NICK - Determine if this is still needed
	}
	
	/**
	 * Sets the file path and updates the GUI to reflect the
	 * new path.
	 * @param path
	 */
	private void setFilePath(String path) {
	    
	    logger.info("Set file path to '"+path+"'.");
		
		filePath = path;
		
		//Let the file selection dialog know which folder files should be
		//uploaded/copied to.
		String courseFolderPath = filePath.substring(0,  filePath.lastIndexOf("/"));
		DefaultGatFileSelectionDialog.courseFolderPath = courseFolderPath;
	}
	
	@Override
	public void setReadOnly(boolean readOnly){
	    if (logger.isLoggable(Level.FINE)) {
            logger.fine("setReadOnly(" + readOnly + ")");
        }

		this.readOnly = readOnly;
		
		dkfView.setReadOnly(readOnly);
	}
		
	private native JavaScriptObject createSaveObject(String path)/*-{

		var that = this;
		var saveObj = function() {
			that.@mil.arl.gift.tools.authoring.server.gat.client.presenter.dkf.DkfPresenter::saveDkf(Ljava/lang/String;Z)(path, false);
		};

		return saveObj;
    }-*/;
		
	/**
	 * Validates the specified file and displays a dialog containing validation 
	 * errors to the client.
	 * 
	 * @param filePath The path of the file to validate.
	 * @param userName The user validating the file.
	 */
	private void validateFile(final String filePath, final String userName) {
		
		ValidateFile action = new ValidateFile();
		
		action.setRelativePath(filePath);
		action.setUserName(userName);
		
		BsLoadingDialogBox.display("Validating File", "Validating, please wait...");
		
		dispatchService.execute(action, new AsyncCallback<ValidateFileResult>() {
			
			@Override
			public void onFailure(Throwable cause) {				
				BsLoadingDialogBox.remove();
				
				ArrayList<String> stackTrace = new ArrayList<String>();
				
				if(cause.getStackTrace() != null) {
					stackTrace.add(cause.toString());
					
					for(StackTraceElement e : cause.getStackTrace()) {
						stackTrace.add("at " + e.toString());
					}
				}
				
				ErrorDetailsDialog dialog = new ErrorDetailsDialog(
						"Failed to validate the file.", 
						cause.toString(), stackTrace);
				dialog.setDialogTitle("Validation Failed");
				dialog.center();
			}

			@Override
			public void onSuccess(ValidateFileResult result) {
				BsLoadingDialogBox.remove();
				
				if(result.isSuccess()) {
					ModalDialogBox dialog = new ModalDialogBox();
					dialog.setText("Validation Complete");
					dialog.setWidget(new Label("No errors found."));
					dialog.setCloseable(true);
					dialog.center();
				} else {
					FileValidationDialog dialog = new FileValidationDialog(result.getFileName(),
							result.getReason(), result.getDetails(), result.getStackTrace());
					dialog.setText("Validation Errors");
					dialog.center();
				}				
			}			
		});
	}
	
	/**
	 * Handles when the course's concepts are changed. Notifies any sub-editors that display course concept data
	 * so that their data can be refreshed.
	 */
	private void onCourseConceptsChanged() {
	    
	    if(eventBus != null) {
	        eventBus.fireEvent(new CourseConceptsChangedEvent(null));
	    }
	}
	
	/**
	 * Save the DKF. Called by native functions (see {@link #exposeNativeFunctions()}.
	 */
	private void saveDkf() {
	    if(!readOnly && filePath != null){
	        saveDkf(filePath, false);	
	    }
	}
	
	public native void exposeNativeFunctions()/*-{

		var that = this;

		$wnd.stop = $entry(function() {
			that.@mil.arl.gift.tools.authoring.server.gat.client.presenter.dkf.DkfPresenter::stop()();
		});

		$wnd.redefineSaveEmbeddedCourseObject = $entry(function() {
			that.@mil.arl.gift.tools.authoring.server.gat.client.presenter.dkf.DkfPresenter::redefineSaveEmbeddedCourseObject()();
		});
		
		$wnd.onCourseConceptsChanged = $entry(function() {
            that.@mil.arl.gift.tools.authoring.server.gat.client.presenter.dkf.DkfPresenter::onCourseConceptsChanged()();
        });
        
        $wnd.saveCourseAndNotify = $entry(function(){
            that.@mil.arl.gift.tools.authoring.server.gat.client.presenter.dkf.DkfPresenter::saveDkf()();
            $wnd.parent.saveCourseAndNotify();
        });

    }-*/;
	
	
	
}
