/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.presenter.course;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.extras.slider.client.ui.Range;
import org.gwtbootstrap3.extras.slider.client.ui.base.event.SlideStopEvent;
import org.gwtbootstrap3.extras.slider.client.ui.base.event.SlideStopHandler;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.ListDataProvider;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;

import generated.course.ATRemoteSKO;
import generated.course.AutoTutorSKO;
import generated.course.AutoTutorSession;
import generated.course.BooleanEnum;
import generated.course.ConceptNode;
import generated.course.ConceptQuestions;
import generated.course.ConceptQuestions.AssessmentRules;
import generated.course.ConceptQuestions.AssessmentRules.AboveExpectation;
import generated.course.ConceptQuestions.AssessmentRules.AtExpectation;
import generated.course.ConceptQuestions.AssessmentRules.BelowExpectation;
import generated.course.ConceptQuestions.QuestionTypes;
import generated.course.Concepts;
import generated.course.Concepts.Hierarchy;
import generated.course.Concepts.List.Concept;
import generated.course.Conversation;
import generated.course.ConversationTreeFile;
import generated.course.Course;
import generated.course.DkfRef;
import generated.course.FixedDecayMandatoryBehavior;
import generated.course.MandatoryOption;
import generated.course.PresentSurvey;
import generated.course.PresentSurvey.ConceptSurvey;
import generated.course.SimpleMandatoryBehavior;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.BsLoadingDialogBox;
import mil.arl.gift.common.gwt.client.widgets.file.CancelCallback;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.survey.Survey.SurveyTypeEnum;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.event.EditorDirtyEvent;
import mil.arl.gift.tools.authoring.server.gat.client.event.course.CourseConceptsChangedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.event.course.CourseObjectDisabledEvent;
import mil.arl.gift.tools.authoring.server.gat.client.event.course.QuestionBankChangedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.event.course.SurveysChangedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.place.ConversationPlace;
import mil.arl.gift.tools.authoring.server.gat.client.place.DkfPlace;
import mil.arl.gift.tools.authoring.server.gat.client.presenter.AbstractGatPresenter;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.mbp.MbpView.CandidateConcept;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.survey.KnowledgeAssessmentSlider;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.survey.PresentSurveyView;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.survey.PresentSurveyView.GIFTAutotutorSessionTypeEditingMode;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.survey.PresentSurveyView.MandatoryBehaviorOptionChoice;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.survey.PresentSurveyView.SurveyTypeEditingMode;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.DefaultGatFileSelectionDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.DeleteRemoveCancelCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.DeleteRemoveCancelDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.ErrorDetailsDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.NewOrExistingFileDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.WarningDialog;
import mil.arl.gift.tools.authoring.server.gat.shared.GwtSurveySystemProperties;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.CopyTemplateFile;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.CopyWorkspaceFilesResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.DeleteWorkspaceFiles;

/**
 * The presenter used to populate the PresentSurvey editor and modify the underlying data model
 * 
 * @author nroberts
 */
public class PresentSurveyPresenter extends AbstractGatPresenter implements PresentSurveyView.Presenter {
	
	/**
     * Interface for the event binder for this class.  
     * @author cragusa
     */
    interface MyEventBinder extends EventBinder<PresentSurveyPresenter> {
    }
    
    /** The Constant logger. */
    private static final Logger logger = Logger.getLogger(PresentSurveyPresenter.class.getName());   
    
    /** Binder for handling events. */
    private static final MyEventBinder eventBinder = GWT
            .create(MyEventBinder.class);

    private static final int DAYS_TO_MILLI = 24 * 60 * 60 * 1000;

    /** The view. */
    private PresentSurveyView view;
    
    /** The course currently being edited. Used to get the list of concepts. */
    private Course currentCourse = null;
    
    /** The current present survey. */
    private PresentSurvey currentPresentSurvey; 
    
    private String currentGIFTSurveyKey = null;
    
    private ConceptSurvey currentConceptSurvey = null;
    
    private Conversation currentConversation = null;
    
    private ConversationTreeFile currentConversationTree = null;
    
    /** Data provider for the 'Concepts:' table */
    private ListDataProvider<CandidateConcept> conceptsTableDataProvider = new ListDataProvider<CandidateConcept>();
    
    /** Data provider for the 'Questions:' table */
    private ListDataProvider<ConceptQuestions> questionsTableDataProvider = new ListDataProvider<ConceptQuestions>();
    
    /** Data provider for the 'Scoring:' table */
    private ListDataProvider<ConceptQuestions> scoringTableDataProvider = new ListDataProvider<ConceptQuestions>();
    
    /** Keeps track of extraneous concept questions since they are overwritten when this widget is loaded */
    private static HashMap<String, ConceptQuestions> extraneousConcepts = new HashMap<String, ConceptQuestions>();
    
    private boolean shouldShowConceptsErrorMessage = false;
    
    /** The path to the current course */
    private String coursePath;
    
    private boolean isSelectingExistingDkf = false;
    
    /**
     * Instantiates a new present survey presenter.
     */
    public PresentSurveyPresenter(PresentSurveyView view) {
    	
    	super();
    	
    	this.view = view;
    	
    	start();
    	
    	init();
    }
    
    /* (non-Javadoc)
     * @see mil.arl.gift.tools.authoring.gat.client.presenter.AbstractGatPresenter#getLogger()
     */
    @Override
    protected Logger getLogger() {
        return logger;
    }
    
    private void init() {
        
        if (logger.isLoggable(Level.INFO)) {
            logger.info("init()");
        }
        
        if(GatClientUtility.isReadOnly()) {
            view.getFullScreenCheckBoxHasEnabled().setEnabled(false);
            view.getShowResponsesCheckboxHasEnabled().setEnabled(false);
            view.getUseResultsCheckboxHasEnabled().setEnabled(false);
            view.getMandatoryCheckBoxHasEnabled().setEnabled(false);
            view.getDisabledInputHasEnabled().setEnabled(false);
            view.getConversationUrlBox().setEnabled(false);
        }
        
        // Set the dkf error callback
        view.setCancelCallback(new CancelCallback() {

            @Override
            public void onCancel() {
                logger.info("The dkf modal was cancled due to an error.  Clearing the selected dkf in the interface.");
                DkfRef dkfRef = new DkfRef();
                
                AutoTutorSession autoTutorSession;
                if(currentConversation.getType() instanceof AutoTutorSession){
                    //reuse existing object
                    autoTutorSession = (AutoTutorSession) currentConversation.getType();
                    dkfRef.setFile(null); 
                    autoTutorSession.setAutoTutorConfiguration(dkfRef); 
                }
                
                view.hideDkfFileLabel();
                
            }
            
        });
    	
	    // Use results to influence course flow check box
	    
	    handlerRegistrations.add(view.getUseResultsCheckBox().addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				
				if(currentConceptSurvey != null){
					
					Boolean shouldSkipConcepts = event.getValue();
					
					currentConceptSurvey.setSkipConceptsByExamination(
							shouldSkipConcepts != null && shouldSkipConcepts
								? BooleanEnum.TRUE
								: BooleanEnum.FALSE
					);
					
					eventBus.fireEvent(new EditorDirtyEvent());
					
				}
			}
		})); 
	    
	    
	    //Select AutoTutor DKF button
	    
	    handlerRegistrations.add(view.getSelectDKFFileButton().addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
								
				if(GatClientUtility.isReadOnly()) {
        	    	WarningDialog.error("Read only", "File selection is disabled in Read-Only mode.");
        	    	return;
        	    }
				
				NewOrExistingFileDialog.showCreateOrSelect("Real-Time Assessment", new ClickHandler() {

					@Override
					public void onClick(ClickEvent createEvent) {
					    
					    
					    HashMap<String, String> paramMap = new HashMap<String, String>();
                        // Pass the course survey context id into the url via a parameter.
                        paramMap.put(DkfPlace.PARAM_SURVEYCONTEXTID, currentCourse.getSurveyContext().toString());
                        
                        final String url = GatClientUtility.createModalDialogUrlWithParams(
                                coursePath,
                                "AutoTutor_", AbstractSchemaHandler.DKF_FILE_EXTENSION, paramMap);

						view.showDkfModalEditor(coursePath, url);
						
					}
					
				}, new ClickHandler() {

					@Override
					public void onClick(ClickEvent selectEvent) {
					    view.getFileSelector().setAllowedFileExtensions(new String[]{AbstractSchemaHandler.DKF_FILE_EXTENSION});
					    view.setFileSelectionDialogIntroMessage(DefaultGatFileSelectionDialog.CHOOSE_DKF_FILE);
						view.setFileSelectionDialogVisible(true);
						
						isSelectingExistingDkf = true;
					}
					
				});
				
			}
		}));
	    
	    handlerRegistrations.add(view.getSelectConversationTreeFileButton().addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                                
            	if(GatClientUtility.isReadOnly()) {
        	    	WarningDialog.error("Read only", "File selection is disabled in Read-Only mode.");
        	    	return;
        	    }
            	
                NewOrExistingFileDialog.showCreateOrSelect("Conversation Tree", new ClickHandler() {

                    @Override
                    public void onClick(ClickEvent createEvent) {
                        String url = GatClientUtility.createModalDialogUrl(
                                coursePath, "ConversationTree_", AbstractSchemaHandler.CONVERSATION_TREE_FILE_EXTENSION);
                        view.showConversationTreeModalEditor(coursePath, url, currentPresentSurvey.getTransitionName());
                        
                    }
                    
                }, new ClickHandler() {

                    @Override
                    public void onClick(ClickEvent selectEvent) {
                        view.getFileSelector().setAllowedFileExtensions(new String[]{AbstractSchemaHandler.CONVERSATION_TREE_FILE_EXTENSION});
                        view.setFileSelectionDialogIntroMessage(DefaultGatFileSelectionDialog.CHOOSE_CONVERSATION_TREE_FILE_OBJECT);
                        isSelectingExistingDkf = false;
                        view.setFileSelectionDialogVisible(true);
                    }
                    
                });
                
            }
        }));
	    
	    handlerRegistrations.add(view.getEditDkfButton().addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				
				DkfRef dkfRef = (DkfRef)((AutoTutorSession) currentConversation.getType()).getAutoTutorConfiguration();

				HashMap<String, String> paramMap = new HashMap<String, String>();
                // Pass the course survey context id into the url via a parameter.
                paramMap.put(DkfPlace.PARAM_SURVEYCONTEXTID, currentCourse.getSurveyContext().toString());
                paramMap.put(DkfPlace.PARAM_READONLY, Boolean.toString(GatClientUtility.isReadOnly()));
                
                final String url = GatClientUtility.getModalDialogUrlWithParams(
                        coursePath,
                        dkfRef.getFile(), paramMap);
                
				view.showDkfModalEditor(coursePath, url);
			}
	    	
	    }));
	    
	    	    
	    //File Selection
	    handlerRegistrations.add(view.getFileSelectionDialog().addValueChangeHandler(new ValueChangeHandler<String>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
			    
			    if(logger.isLoggable(Level.INFO)){
			        logger.info("fileSelectionDialog onValueChange() called, event = " + event);
			    }
			    
				if(currentConversation != null){
				    
				    //AutoTutor Session
				    if(event.getValue().endsWith(AbstractSchemaHandler.DKF_FILE_EXTENSION)){
                        DkfRef dkfRef;
                        
                        AutoTutorSession autoTutorSession;
                        if(currentConversation.getType() instanceof AutoTutorSession){
                            //reuse existing object
                            autoTutorSession = (AutoTutorSession) currentConversation.getType();
                            
                            if(autoTutorSession.getAutoTutorConfiguration() instanceof DkfRef){
                                //reuse existing object
                                dkfRef = (DkfRef) autoTutorSession.getAutoTutorConfiguration();
                            }else{
                                dkfRef = new DkfRef();
                            }
                        }else{
                            autoTutorSession = new AutoTutorSession();
                            dkfRef = new DkfRef();
                            currentConversation.setType(autoTutorSession);
                        }
                        
                        dkfRef.setFile(event.getValue()); 
                        autoTutorSession.setAutoTutorConfiguration(dkfRef);                    
                        
                        view.showDkfFileLabel(event.getValue());
                        view.getConversationUrlBox().setValue(null);
                        
                        if (isSelectingExistingDkf) {
                            logger.info("isSelectingDkf is true, showing the dkf modal dialog.");
                            // showdkfmodal
                            HashMap<String, String> paramMap = new HashMap<String, String>();
                            
                            // Check to see if the imported dkf has survey references that need to be updated.
                            // Pass the course survey context id into the url via a parameter.
                            paramMap.put(DkfPlace.PARAM_SURVEYCONTEXTID, currentCourse.getSurveyContext().toString());
                            paramMap.put(DkfPlace.PARAM_IMPORTEDDKF, Boolean.TRUE.toString());
                            paramMap.put(DkfPlace.PARAM_READONLY, Boolean.toString(GatClientUtility.isReadOnly()));
                            
                            final String url = GatClientUtility.getModalDialogUrlWithParams(
                                    coursePath,
                                    dkfRef.getFile(), paramMap);

                            view.showDkfModalEditor(coursePath, url);
                        } 
                        
                        
                        
				    } else if (event.getValue().endsWith(AbstractSchemaHandler.CONVERSATION_TREE_FILE_EXTENSION)){
				        if(currentConversationTree == null){
				            currentConversationTree = new ConversationTreeFile();
				        }
				        currentConversationTree.setName(event.getValue());
				        currentConversation.setType(currentConversationTree);
				        
				        view.showConversationTreeFileLabel(event.getValue());				        				        
				        view.hideDkfFileLabel();
				        view.getConversationUrlBox().setValue(null);
				    }				    
                    
				} else {
					
					WarningDialog.error("No Conversation", "An error occurred while setting the DKF file to be used by this AutoTutor Session.");
				}
				
				isSelectingExistingDkf = false;
			}
			
			
		}));
	    
	    handlerRegistrations.add(view.getRemoveDkfButton().addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				if(!GatClientUtility.isReadOnly()){
					if(currentConversation != null){
					    
					    DeleteRemoveCancelDialog.show("Delete Real-Time Assessment",
					            "Do you wish to <b>permanently delete</b> this real-time assessment or simply remove this reference to prevent it from being used in this part of the course?<br><br>"
	                            + "Other course objects will be unable to use this real-time assessment if it is deleted, which may cause validation issues if this real-time assessment is being referenced in other parts of the course.", 
					            new DeleteRemoveCancelCallback(){
	
	                                @Override
	                                public void delete() {
	                                    //Variables needed to delete the file
	                                    String username = GatClientUtility.getUserName();
	                                    String browserSessionKey = GatClientUtility.getBrowserSessionKey();
	                                    List<String> filesToDelete = new ArrayList<String>();
	                                    
	                                    //Gets the file name of the file to be deleted
	                                    AutoTutorSession autoTutorSession = (AutoTutorSession) currentConversation.getType();
	                                    DkfRef dkfRef = (DkfRef) autoTutorSession.getAutoTutorConfiguration();
	                                    final String filename = GatClientUtility.getBaseCourseFolderPath()
	                                                        + "/"
	                                                        + dkfRef.getFile();
	                                    filesToDelete.add(filename);
	                                    
	                                    //Performs the deletion
	                                    DeleteWorkspaceFiles action = new DeleteWorkspaceFiles(username, browserSessionKey, filesToDelete, true);
	                                    SharedResources.getInstance().getDispatchService().execute(action, new AsyncCallback<GatServiceResult>(){
	
	                                        @Override
	                                        public void onFailure(Throwable arg0) {
	                                            ErrorDetailsDialog dialog = new ErrorDetailsDialog(
	                                                    "Failed to delete the file.", 
	                                                    arg0.getMessage(), 
	                                                    DetailedException.getFullStackTrace(arg0));
	                                            dialog.setDialogTitle("Deletion Failed");
	                                            dialog.center();
	                                        }
	
	                                        @Override
	                                        public void onSuccess(GatServiceResult arg0) {
	                                            if(arg0.isSuccess()) {
	                                                remove();
	                                                saveCourse();
	                                            }
	                                            else {
	                                                logger.warning("Was unable to delete the file: " + filename + "\nError Message: " + arg0.getErrorMsg());
	                                            }
	                                        }
	                                        
	                                    });
	                                }
	
	                                @Override
	                                public void remove() {
	                                    
	                                    DkfRef dkfRef = new DkfRef();
	                                    
	                                    AutoTutorSession autoTutorSession;
	                                    if(currentConversation.getType() instanceof AutoTutorSession){
	                                        //reuse existing object
	                                        autoTutorSession = (AutoTutorSession) currentConversation.getType();
	                                        dkfRef.setFile(null); 
	                                        autoTutorSession.setAutoTutorConfiguration(dkfRef); 
	                                    }
	                                    
	                                    view.hideDkfFileLabel();
	                                    
	                                }
	
	                                @Override
	                                public void cancel() {
	                                    
	                                }
					        
					    });
	                                   
					}
				}
			}        	
        }));
	    
	    handlerRegistrations.add(view.getConversationUrlBox().addValueChangeHandler(new ValueChangeHandler<String>() {

            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
	
                if(currentConversation != null){
	                                                        
                    if(currentConversation.getType() == null 
                            || !(currentConversation.getType() instanceof AutoTutorSession)) {
                        currentConversation.setType(new AutoTutorSession());
	                                                            }
	
                    AutoTutorSession autoTutorSession = (AutoTutorSession) currentConversation.getType();
	                                                            
                    if(autoTutorSession.getAutoTutorConfiguration() == null 
                            || !(autoTutorSession.getAutoTutorConfiguration() instanceof AutoTutorSKO)) {
                        autoTutorSession.setAutoTutorConfiguration(new AutoTutorSKO());
	                                                    }
	
                    AutoTutorSKO skoRef = (AutoTutorSKO) autoTutorSession.getAutoTutorConfiguration();
	                                                            
                    if(skoRef.getScript() == null 
                            || !(skoRef.getScript() instanceof ATRemoteSKO)) {
                        skoRef.setScript(new ATRemoteSKO());
	                                                            }
	                                                            
                    ATRemoteSKO remoteSko = (ATRemoteSKO) skoRef.getScript();
	
                    ATRemoteSKO.URL url = new ATRemoteSKO.URL();
                    url.setAddress(event.getValue());
	                                                        
                    remoteSko.setURL(url);
	                                                    }
	            }  
        }));
	    
	    handlerRegistrations.add(view.getRemoveConversationTreeButton().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
            	if(!GatClientUtility.isReadOnly()){
	                if(currentConversation != null){
	                    
	                    if(currentConversation.getType() instanceof ConversationTreeFile){
	                        final ConversationTreeFile conversationTree = (ConversationTreeFile) currentConversation.getType();
	                        DeleteRemoveCancelDialog.show("Delete Conversation Tree",
	                                "Do you wish to <b>permanently delete</b> this conversation tree or simply remove this reference to prevent it from being used in this part of the course?<br><br>"
	                                        + "Other course objects will be unable to use this conversation tree if it is deleted, which may cause validation issues if this conversation tree is being referenced in other parts of the course.",
	                                new DeleteRemoveCancelCallback(){
	
	                                    @Override
	                                    public void delete() {
	                                        String username = GatClientUtility.getUserName();
	                                        String browserSessionKey = GatClientUtility.getBrowserSessionKey();
	                                        final String path = GatClientUtility.getBaseCourseFolderPath() + "/" + conversationTree.getName();
	                                        
	                                        List<String> filesToDelete = new ArrayList<String>();
	                                        filesToDelete.add(path);
	                                        
	                                        DeleteWorkspaceFiles action = new DeleteWorkspaceFiles(username, browserSessionKey, filesToDelete, true);
	                                        SharedResources.getInstance().getDispatchService().execute(action, new AsyncCallback<GatServiceResult>(){
	
	                                            @Override
	                                            public void onFailure(Throwable arg0) {
	                                                ErrorDetailsDialog dialog = new ErrorDetailsDialog(
	                                                        "Failed to delete the file.", 
	                                                        arg0.getMessage(), 
	                                                        DetailedException.getFullStackTrace(arg0));
	                                                dialog.setDialogTitle("Deletion Failed");
	                                                dialog.center();
	                                            }
	
	                                            @Override
	                                            public void onSuccess(GatServiceResult arg0) {
	                                                if(arg0.isSuccess()){
	                                                    conversationTree.setName(null);
	                                                    view.hideConversationTreeFileLabel();
	                                                    saveCourse();
	                                                }
	                                                else{
	                                                    logger.warning("Was unable to delete the file: " + path + "\nError Message: " + arg0.getErrorMsg());
	                                                }
	                                                
	                                            }
	                                            
	                                        });
	                                    }
	
	                                    @Override
	                                    public void remove() {
	                                        conversationTree.setName(null);
	                                        view.hideConversationTreeFileLabel();
	                                    }
	
	                                    @Override
	                                    public void cancel() {
	                                        
	                                    }
	                            
	                        });
	                    }                    
	                }
            	}
            }           
        }));
	    
	    handlerRegistrations.add(view.getEditConversationTreeButton().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
            	HashMap<String, String> paramMap = new HashMap<String, String>();
            	paramMap.put(ConversationPlace.PARAM_READONLY, Boolean.toString(GatClientUtility.isReadOnly()));
            	paramMap.put(ConversationPlace.PARAM_FULLSCREEN, Boolean.toString(BooleanEnum.TRUE.equals(currentPresentSurvey.getFullScreen())));
            	
                String name = ((ConversationTreeFile) currentConversation.getType()).getName();
                String url = GatClientUtility.getModalDialogUrlWithParams(
                        coursePath, name, paramMap);
                
                view.showConversationTreeModalEditor(coursePath, url, currentPresentSurvey.getTransitionName());
            }           
        }));
	    
	    //Survey Key list box	    
	    
	    handlerRegistrations.add(view.getSurveyPicker().addValueChangeHandler(new ValueChangeHandler<String>() {

			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
					
				currentPresentSurvey.setSurveyChoice(event.getValue());
			
				currentGIFTSurveyKey = (String) currentPresentSurvey.getSurveyChoice();	
				
				eventBus.fireEvent(new EditorDirtyEvent());
			}
		}));
	    
	    
	    //Full Screen check box
	    
	    handlerRegistrations.add(view.getFullScreenCheckBox().addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {

				currentPresentSurvey.setFullScreen(
						event.getValue()
							? BooleanEnum.TRUE
							: BooleanEnum.FALSE
				);
				
				eventBus.fireEvent(new EditorDirtyEvent());
			}
		}));
	    
        handlerRegistrations.add(view.getDisabledInput().addValueChangeHandler(new ValueChangeHandler<Boolean>() {

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {

                if (currentPresentSurvey != null) {

                    currentPresentSurvey.setDisabled(
                            event.getValue() != null && event.getValue() ? BooleanEnum.TRUE : BooleanEnum.FALSE);

                    eventBus.fireEvent(new EditorDirtyEvent());

                    SharedResources.getInstance().getEventBus()
                            .fireEvent(new CourseObjectDisabledEvent(currentPresentSurvey));
                }
            }

        }));
	    
	    
	    //Show Responses check box
	    
	    handlerRegistrations.add(view.getShowResponsesCheckBox().addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {

				currentPresentSurvey.setShowInAAR(
						event.getValue()
							? BooleanEnum.TRUE
							: BooleanEnum.FALSE
				);
				
				eventBus.fireEvent(new EditorDirtyEvent());
			}
		}));
	    
        // Register command that updates mandatory control visibility when the selected survey changes
	    view.getSurveyPicker().setSurveyLoadedCommand(new Command() {
            
            @Override
            public void execute() {
                SurveyTypeEnum type = view.getSurveyPicker().getSelectedSurveyType();
                boolean showMandatoryControls = type != null 
                        && type != SurveyTypeEnum.COLLECTINFO_NOTSCORED
                        && type != SurveyTypeEnum.ASSESSLEARNER_STATIC;
                view.setMandatoryControlsVisibility(showMandatoryControls);
            }
        });
	    
	    // Register command that updates mandatory control visibility when the Question Bank is loaded
	    view.getSurveyPickerQuestionBank().setSurveyLoadedCommand(new Command() {
            
            @Override
            public void execute() {
                view.setMandatoryControlsVisibility(true);
            }
        });
	    
        // Mandatory check box
	    handlerRegistrations.add(view.getMandatoryCheckBox().addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                if (logger.isLoggable(Level.INFO)) {
                    logger.info("mandatoryCheckBox.onValueChange(" + event.getValue() + ")");
                }
                
                MandatoryOption mandatoryOption = new MandatoryOption();
                
                if(event.getValue()) {
                    MandatoryBehaviorOptionChoice behaviorEnum = view.getMandatoryBehaviorSelector().getValue(); 
                    if(behaviorEnum == MandatoryBehaviorOptionChoice.ALWAYS) {
                        SimpleMandatoryBehavior always = new SimpleMandatoryBehavior();
                        always.setUseExistingLearnerStateIfAvailable(false);
                        mandatoryOption.setMandatoryBehavior(always);
                    } else if(behaviorEnum == MandatoryBehaviorOptionChoice.AFTER) {
                        FixedDecayMandatoryBehavior after = new FixedDecayMandatoryBehavior();
                        BigInteger shelfLife = BigInteger.valueOf(view.getLearnerStateShelfLife().getValue() * DAYS_TO_MILLI);
                        after.setLearnerStateShelfLife(shelfLife);
                        mandatoryOption.setMandatoryBehavior(after);
                    }
                } else {
                    SimpleMandatoryBehavior simpleBehavior = new SimpleMandatoryBehavior();
                    simpleBehavior.setUseExistingLearnerStateIfAvailable(true);
                    mandatoryOption.setMandatoryBehavior(simpleBehavior);
                }
                
                currentPresentSurvey.setMandatoryOption(mandatoryOption);                
                updateMandatoryControls(mandatoryOption.getMandatoryBehavior());
                
                eventBus.fireEvent(new EditorDirtyEvent());
            }
        }));
        
        //Mandatory behavior selection
        handlerRegistrations.add(view.getMandatoryBehaviorSelector().addValueChangeHandler(new ValueChangeHandler<MandatoryBehaviorOptionChoice>() {

            @Override
            public void onValueChange(ValueChangeEvent<MandatoryBehaviorOptionChoice> event) {
                MandatoryBehaviorOptionChoice behaviorEnum = event.getValue();
                
                Serializable behavior = null;
                if(behaviorEnum == MandatoryBehaviorOptionChoice.AFTER) {
                    FixedDecayMandatoryBehavior fixedBehavior = new FixedDecayMandatoryBehavior();
                    int shelfLifeDays = view.getLearnerStateShelfLife().getValue();
                    fixedBehavior.setLearnerStateShelfLife(BigInteger.valueOf(shelfLifeDays * DAYS_TO_MILLI));
                    behavior = fixedBehavior;
                } else if(behaviorEnum == MandatoryBehaviorOptionChoice.ALWAYS) {
                    SimpleMandatoryBehavior simpleBehavior = new SimpleMandatoryBehavior();
                    simpleBehavior.setUseExistingLearnerStateIfAvailable(false);
                    behavior = simpleBehavior;
                }
                
                if (currentPresentSurvey.getMandatoryOption() == null) {
                    currentPresentSurvey.setMandatoryOption(new MandatoryOption());
                }
                
                currentPresentSurvey.getMandatoryOption().setMandatoryBehavior(behavior);
                updateMandatoryControls(behavior);
            }
        }));
        
        //Learner state shelf life
        handlerRegistrations.add(view.getLearnerStateShelfLife().addValueChangeHandler(new ValueChangeHandler<Integer>() {

            @Override
            public void onValueChange(ValueChangeEvent<Integer> event) {
                Serializable behavior = currentPresentSurvey.getMandatoryOption() == null ? null
                        : currentPresentSurvey.getMandatoryOption().getMandatoryBehavior();
                
                FixedDecayMandatoryBehavior fixedDecayBehavior;
                if(behavior instanceof FixedDecayMandatoryBehavior) {
                    fixedDecayBehavior = (FixedDecayMandatoryBehavior) behavior;
                } else {
                    fixedDecayBehavior = new FixedDecayMandatoryBehavior();
                    currentPresentSurvey.getMandatoryOption().setMandatoryBehavior(fixedDecayBehavior);
                }
                
                int shelfLife = event.getValue() * DAYS_TO_MILLI;
                fixedDecayBehavior.setLearnerStateShelfLife(BigInteger.valueOf(shelfLife));
            }
        }));
	    
	    //Knowledge Assessment concepts table
	    
	    conceptsTableDataProvider.addDataDisplay(view.getConceptCellTable());
	    
	    view.setConceptSelectionColumnFieldUpdater(new FieldUpdater<CandidateConcept, Boolean>() {
			
			@Override
			public void update(int index, CandidateConcept candidate, Boolean hasBeenSelected) {
				
				if(currentConceptSurvey != null){
					
					ConceptQuestions existingConceptQuestions = null;
					
					//find existing questions matching the name selected
					for(ConceptQuestions conceptQuestions : currentConceptSurvey.getConceptQuestions()){
						
						if(conceptQuestions.getName() != null
								&& conceptQuestions.getName().equals(candidate.getConceptName())){
							
							existingConceptQuestions = conceptQuestions;
							break;
						}
					}
					
					if(hasBeenSelected){
						
						if(existingConceptQuestions == null){	
							
							//create a default set of questions for the concept and add them
							ConceptQuestions questionsToAdd = new ConceptQuestions();
							
							questionsToAdd.setName(candidate.getConceptName());
							
							questionsToAdd.setQuestionTypes(new QuestionTypes());
							questionsToAdd.getQuestionTypes().setEasy(BigInteger.ONE);
							questionsToAdd.getQuestionTypes().setMedium(BigInteger.ZERO);
							questionsToAdd.getQuestionTypes().setHard(BigInteger.ZERO);
							
							questionsToAdd.setAssessmentRules(new AssessmentRules());
							questionsToAdd.getAssessmentRules().setAboveExpectation(new AboveExpectation());
							questionsToAdd.getAssessmentRules().getAboveExpectation().setNumberCorrect(BigInteger.ONE);
							questionsToAdd.getAssessmentRules().setAtExpectation(new AtExpectation());
							questionsToAdd.getAssessmentRules().getAtExpectation().setNumberCorrect(BigInteger.ZERO);
							
							questionsToAdd.getAssessmentRules().setBelowExpectation(new BelowExpectation());
							questionsToAdd.getAssessmentRules().getBelowExpectation().setNumberCorrect(BigInteger.ZERO);
							existingConceptQuestions = questionsToAdd;
							candidate.setChosen(currentConceptSurvey.getConceptQuestions().add(questionsToAdd));
						
						} else {
							candidate.setChosen(true);
						}
						
					} else {
						
						if(existingConceptQuestions != null){
							candidate.setChosen(!currentConceptSurvey.getConceptQuestions().remove(existingConceptQuestions));
							
						} else {
							candidate.setChosen(false);
						}
						
						if(!candidate.isChosen()) {
							view.removeSlider(candidate.getConceptName());
						}
					}
					
					populateQuestionsAndScoringTables();
					
					eventBus.fireEvent(new EditorDirtyEvent());
					
				}
			}
		});
	    
	    
	    //Knowledge Assessment questions table
        
        questionsTableDataProvider.addDataDisplay(view.getQuestionCellTable());
        
        view.setEasyColumnFieldUpdater(new FieldUpdater<ConceptQuestions, String>() {

			@Override
			public void update(int index, ConceptQuestions questions, String easy) {

				try{
					BigInteger number = new BigInteger(easy.trim());
					
					if(questions.getQuestionTypes() != null){
						
						if(number.compareTo(BigInteger.ZERO) < 0){
							
							WarningDialog.error("Invalid value", "Please enter a positive value.");			
							
							view.undoQuestionsTableChanges(questions);
							view.redrawQuestionsCellTable();
							
						} else if(number.compareTo(BigInteger.ZERO) == 0
								&& questions.getQuestionTypes().getMedium() != null 
								&& questions.getQuestionTypes().getMedium().compareTo(BigInteger.ZERO) == 0
								&& questions.getQuestionTypes().getHard() != null 
								&& questions.getQuestionTypes().getHard().compareTo(BigInteger.ZERO) == 0){

							WarningDialog.error("Invalid value", "At least one Easy, Medium, or Hard question must exist for this concept.");	
							
							view.undoQuestionsTableChanges(questions);
							view.redrawQuestionsCellTable();
								
						} else {
							
							questions.getQuestionTypes().setEasy(number);
							updateSliderView(questions);
							
							eventBus.fireEvent(new EditorDirtyEvent());
						}
					}
					
					
				} catch(@SuppressWarnings("unused") NumberFormatException e){
					
					WarningDialog.error("Invalid value", "Please enter a positive integer value.");					
					
					view.undoQuestionsTableChanges(questions);
					view.redrawQuestionsCellTable();
				}
			}
		});
        
        view.setMediumColumnFieldUpdater(new FieldUpdater<ConceptQuestions, String>() {

			@Override
			public void update(int index, ConceptQuestions questions, String medium) {

				try{
					BigInteger number = new BigInteger(medium.trim());
					
					if(questions.getQuestionTypes() != null){
						
						if(number.compareTo(BigInteger.ZERO) < 0){

							WarningDialog.error("Invalid value", "Please enter a positive value.");			
							
							view.undoQuestionsTableChanges(questions);
							view.redrawQuestionsCellTable();
							
						} else if(number.compareTo(BigInteger.ZERO) == 0
								&& questions.getQuestionTypes().getEasy() != null 
								&& questions.getQuestionTypes().getEasy().compareTo(BigInteger.ZERO) == 0
								&& questions.getQuestionTypes().getHard() != null 
								&& questions.getQuestionTypes().getHard().compareTo(BigInteger.ZERO) == 0){

							WarningDialog.error("Invalid value", "At least one Easy, Medium, or Hard question must exist for this concept.");
							
							view.undoQuestionsTableChanges(questions);
							view.redrawQuestionsCellTable();
								
						} else {
							
							questions.getQuestionTypes().setMedium(number);
							updateSliderView(questions);
							
							eventBus.fireEvent(new EditorDirtyEvent());
						}
					}
					
					
				} catch(@SuppressWarnings("unused") NumberFormatException e){
					
					WarningDialog.error("Invalid value", "Please enter a positive integer value.");		
					
					view.undoQuestionsTableChanges(questions);
					view.redrawQuestionsCellTable();
				}
			}
		});
        
        view.setHardColumnFieldUpdater(new FieldUpdater<ConceptQuestions, String>() {

			@Override
			public void update(int index, ConceptQuestions questions, String hard) {

				try{
					BigInteger number = new BigInteger(hard.trim());
					
					if(questions.getQuestionTypes() != null){
						
						if(number.compareTo(BigInteger.ZERO) < 0){

							WarningDialog.error("Invalid value", "Please enter a positive value.");	
							
							view.undoQuestionsTableChanges(questions);
							view.redrawQuestionsCellTable();
							
						} else if(number.compareTo(BigInteger.ZERO) == 0
								&& questions.getQuestionTypes().getEasy() != null 
								&& questions.getQuestionTypes().getEasy().compareTo(BigInteger.ZERO) == 0
								&& questions.getQuestionTypes().getMedium() != null 
								&& questions.getQuestionTypes().getMedium().compareTo(BigInteger.ZERO) == 0){

							WarningDialog.error("Invalid value", "At least one Easy, Medium, or Hard question must exist for this concept.");
							
							view.undoQuestionsTableChanges(questions);
							view.redrawQuestionsCellTable();
								
						} else {
							
							questions.getQuestionTypes().setHard(number);
							updateSliderView(questions);
							
							eventBus.fireEvent(new EditorDirtyEvent());
						}
					}
					
					
				} catch(@SuppressWarnings("unused") NumberFormatException e){
					
					WarningDialog.error("Invalid value", "Please enter a positive integer value.");
					
					view.undoQuestionsTableChanges(questions);
					view.redrawQuestionsCellTable();
				}
			}
		});
        
    }


    /**
     * Loads the given {@link PresentSurvey} into the view for editing
     * 
     * @param aar the survey to edit
     */
    public void edit(PresentSurvey survey, Course course) {

        currentPresentSurvey = survey;
        currentCourse = course;   
        coursePath = GatClientUtility.getBaseCourseFolderPath();
		
        populateView();
    }   
    
    /**
     * Populates the view based on the current present survey.
     */
    private void populateView(){
    	
    	if (logger.isLoggable(Level.INFO)) {
            logger.info("populateView()");
        }
        
    	view.setSurveyContextId(currentCourse != null ? currentCourse.getSurveyContext() : null);
    	view.setCourseData(currentCourse);
    	
    	boolean errorOccurred = false;
    	
    	view.getDisabledInput().setValue(null);
    	
    	currentGIFTSurveyKey = null;
    	
    	currentConceptSurvey = new ConceptSurvey();
    	currentConceptSurvey.setGIFTSurveyKey(GwtSurveySystemProperties.KNOWLEDGE_ASSESSMENT_QBANK_GIFT_KEY);
    	view.getSurveyPickerQuestionBank().setValue(currentConceptSurvey.getGIFTSurveyKey());
    	
    	currentConversation = new Conversation();
    	currentConversationTree = new ConversationTreeFile();
    	
    	//clear the 'Concepts:' table
    	conceptsTableDataProvider.getList().clear();  	
    	conceptsTableDataProvider.refresh();   	
    	view.hideDkfFileLabel();
	    view.hideConversationTreeFileLabel();
	    view.getConversationUrlBox().setValue(null);
	    view.setAutotutorTypeEditingMode(GIFTAutotutorSessionTypeEditingMode.SKO);
    	
    	if(currentPresentSurvey != null){
    	    
    	    view.setTransitionName(currentPresentSurvey.getTransitionName());
    		
    		boolean hasValidSurveyChoice = true;
	        
	        //set survey choice
	        if(currentPresentSurvey.getSurveyChoice() != null){
	            
	            //default visibility
	            view.setShowResponsesVisibility(true);
	        	
	        	if(currentPresentSurvey.getSurveyChoice() instanceof String){
	        		
	        		currentGIFTSurveyKey = (String) currentPresentSurvey.getSurveyChoice();
	        		
	        		view.getSurveyPicker().setValue(currentGIFTSurveyKey);
	        		
                    view.setSurveyTypeEditingMode(SurveyTypeEditingMode.GIFT_SURVEY);
                    
                    view.getDisabledInput()
                            .setValue(currentPresentSurvey.getDisabled() != null
                                    ? currentPresentSurvey.getDisabled().equals(BooleanEnum.TRUE)
                                    : false // not checked by default
                                    , true);
	        	    
	        	    view.getShowResponsesCheckBox().setValue(
	        				currentPresentSurvey.getShowInAAR() != null
	        					? currentPresentSurvey.getShowInAAR().equals(BooleanEnum.TRUE)
	        					: true
	        		);
	        	
	        	} else if(currentPresentSurvey.getSurveyChoice() instanceof ConceptSurvey){
	        		
	        		currentConceptSurvey = (ConceptSurvey) currentPresentSurvey.getSurveyChoice();
	        		currentPresentSurvey.setSurveyChoice(currentConceptSurvey);
	        		
	        		currentConceptSurvey.setGIFTSurveyKey(GwtSurveySystemProperties.KNOWLEDGE_ASSESSMENT_QBANK_GIFT_KEY);
	        		
	        		view.getSurveyPickerQuestionBank().setValue(currentConceptSurvey.getGIFTSurveyKey());
	        		
                    view.getDisabledInput()
                            .setValue(currentPresentSurvey.getDisabled() != null
                                    ? currentPresentSurvey.getDisabled().equals(BooleanEnum.TRUE)
                                    : false // not checked by default
                                    , true);

	        	    view.getShowResponsesCheckBox().setValue(
	        				currentPresentSurvey.getShowInAAR() != null
	        					? currentPresentSurvey.getShowInAAR().equals(BooleanEnum.TRUE)
	        					: true
	        		);
	        		
	        		view.getUseResultsCheckBox().setValue(
	        				currentConceptSurvey.getSkipConceptsByExamination() != null && currentConceptSurvey.getSkipConceptsByExamination().equals(BooleanEnum.TRUE)
	        		);
	        		
	        		view.setSurveyTypeEditingMode(SurveyTypeEditingMode.QUESTION_BANK);
	        		
	        		
	        	} else if(currentPresentSurvey.getSurveyChoice() instanceof Conversation){
	        		
	        	    currentConversation = (Conversation)currentPresentSurvey.getSurveyChoice();
	        	    
	        	    if(currentConversation.getType() == null){
	        	    	currentConversation.setType(new AutoTutorSession());
	        	    }
	        	    
	        	    if(currentConversation.getType() instanceof AutoTutorSession){
	        	        
	        	        AutoTutorSession currentAutoTutor = (AutoTutorSession) currentConversation.getType();
    	        		
    	        		currentPresentSurvey.setSurveyChoice(currentConversation);
    	        		
                        view.getDisabledInput()
                                .setValue(currentPresentSurvey.getDisabled() != null
                                        ? currentPresentSurvey.getDisabled().equals(BooleanEnum.TRUE)
                                        : false // not checked by default
                                        , true);
    	        		
    	        		//show conversation in AAR is not supported
    	        		view.setShowResponsesVisibility(false);
                        
    	        		view.setSurveyTypeEditingMode(SurveyTypeEditingMode.AUTOTUTOR);
                        
    	        		if(currentAutoTutor.getAutoTutorConfiguration() != null){
        	        		if(currentAutoTutor.getAutoTutorConfiguration() instanceof generated.course.DkfRef){
    	        			
        	        			view.setAutotutorTypeEditingMode(GIFTAutotutorSessionTypeEditingMode.DKF);
        	        		    generated.course.DkfRef dkfRef = (generated.course.DkfRef)currentAutoTutor.getAutoTutorConfiguration();
        	        		    if(dkfRef.getFile() != null){
        	        		        view.showDkfFileLabel(dkfRef.getFile());
        	        		    }
        	        		} else if(currentAutoTutor.getAutoTutorConfiguration() instanceof generated.course.AutoTutorSKO){
        	        		    view.setAutotutorTypeEditingMode(GIFTAutotutorSessionTypeEditingMode.SKO);
        	        		    generated.course.AutoTutorSKO skoRef = (generated.course.AutoTutorSKO) currentAutoTutor.getAutoTutorConfiguration();
        	        		    
        	        		    if(skoRef.getScript() == null 
        	        		            || !(skoRef.getScript() instanceof ATRemoteSKO)){
                                   skoRef.setScript(new ATRemoteSKO());
                                }
        	        		    
        	        		    ATRemoteSKO remoteSko = (ATRemoteSKO) skoRef.getScript();
        	        		    
        	        		    if(remoteSko.getURL() == null) {
        	        		        remoteSko.setURL(new ATRemoteSKO.URL());
        	        		}
        	        		    
        	        		    view.getConversationUrlBox().setValue(remoteSko.getURL().getAddress());
    	        		}
    	        		}
    	        		
	        	    } else if(currentConversation.getType() instanceof ConversationTreeFile){
	        	        currentConversationTree = (ConversationTreeFile) currentConversation.getType();
	        	        currentPresentSurvey.setSurveyChoice(currentConversation);
                        
                        view.getDisabledInput()
                                .setValue(currentPresentSurvey.getDisabled() != null
                                        ? currentPresentSurvey.getDisabled().equals(BooleanEnum.TRUE)
                                        : false // not checked by default
                                        , true);
                        
                        //show conversation in AAR is not supported
                        view.setShowResponsesVisibility(false);
                        
                        // if the conversation tree has not been set (is null/empty), create a conversation tree file on server from the
                        // template conversation tree.
                        if(currentConversationTree.getName() == null || currentConversationTree.getName().isEmpty()){

                              if (logger.isLoggable(Level.INFO)) {
                                  logger.info("Attempting to copy the 'default' conversation tree file to use as a default.");
                              }
                              
                              CopyTemplateFile action = new CopyTemplateFile(GatClientUtility.getUserName(), GatClientUtility.getBaseCourseFolderPath(),
                                      currentPresentSurvey.getTransitionName(), AbstractSchemaHandler.CONVERSATION_TREE_FILE_EXTENSION);

                              BsLoadingDialogBox.display("Creating Conversation Tree", "Creating conversation tree resouces, please wait...");
                              SharedResources.getInstance().getDispatchService().execute(action, new AsyncCallback<CopyWorkspaceFilesResult>() {

                                  @Override
                                  public void onFailure(Throwable caught) {

                                      if (logger.isLoggable(Level.WARNING)) {
                                          logger.warning("Caught exception while copying conversation tree template file: '" + caught.toString()
                                                  + "' Application will not have default conversation tree file initialized.");
                                      }

                                      BsLoadingDialogBox.remove();
                                      view.showConversationTreeFileLabel(null);
                                  }

                                  @Override
                                  public void onSuccess(CopyWorkspaceFilesResult result) {

                                      BsLoadingDialogBox.remove();
                                      if (result.isSuccess()) {
                                          String convTreeFile = result.getCopiedFilename();

                                          if (convTreeFile != null) {
                                              currentConversationTree.setName(convTreeFile);

                                              if (logger.isLoggable(Level.INFO)) {
                                                  logger.info("The default conversation tree file was copied successfully to the conversation tree course object.");
                                              }
                                              
                                              view.showConversationTreeFileLabel(convTreeFile);
                                          } else {
                                              if (logger.isLoggable(Level.WARNING)) {
                                                  logger.warning(
                                                          "Failed to copy the conversation tree template for the conversation tree course object because the conversation tree file is null:\n"
                                                                  + result.getErrorMsg() + "\n" + result.getErrorDetails());
                                              }

                                              view.showConversationTreeFileLabel(null);
                                          }
                                      } else {
                                          if (logger.isLoggable(Level.WARNING)) {
                                              logger.warning("Failed to copy the conversation tree template for the conversation tree course object:\n" + result.getErrorMsg() + "\n"
                                                      + result.getErrorDetails());
                                          }

                                          view.showConversationTreeFileLabel(null);
                                      }
                                  }
                              });
                          } else {
                              view.showConversationTreeFileLabel(currentConversationTree.getName());  
                          }
                        view.showConversationTreeFileLabel(currentConversationTree.getName());	
				                                
                        view.setSurveyTypeEditingMode(SurveyTypeEditingMode.CONVERSATION_TREE);
	        	    }
	        		
	        	} else {
	        		hasValidSurveyChoice = false;
	        	}        	
	        	
	        } else {
	        	hasValidSurveyChoice = false;
	        }
	        
	        //if the survey choice is not valid, populate the view using a default survey context survey
	        if(!hasValidSurveyChoice){
	        	
        		currentPresentSurvey.setSurveyChoice(currentGIFTSurveyKey);
        		
        		view.getSurveyPicker().setValue(currentGIFTSurveyKey, true);
        		
        		view.setSurveyTypeEditingMode(SurveyTypeEditingMode.GIFT_SURVEY);
	        }
	        
	        //set fullscreen value
            boolean fullScreenValue = true;  
            if(currentPresentSurvey.getFullScreen() != null){
                //there is a value set in the incoming data model
                
                fullScreenValue = currentPresentSurvey.getFullScreen().equals(BooleanEnum.TRUE) ? true : false;
            }else{
                //there is no value set in the incoming data model, set the value to true since that is the default
                currentPresentSurvey.setFullScreen(BooleanEnum.TRUE);
            }

            //update the check box
            view.getFullScreenCheckBox().setValue(fullScreenValue); 

	        //set show responses value
            boolean showResponsesValue = true;  
            if(currentPresentSurvey.getShowInAAR() != null){
                //there is a value set in the incoming data model
                
                showResponsesValue = currentPresentSurvey.getShowInAAR().equals(BooleanEnum.TRUE) ? true : false;
            }else{
                //there is no value set in the incoming data model, set the value to true since that is the default
                currentPresentSurvey.setShowInAAR(BooleanEnum.TRUE);
            }

            //update the check box
            view.getShowResponsesCheckBox().setValue(showResponsesValue); 
            
            // set disable value
            boolean disableValue = false;
            if (currentPresentSurvey.getDisabled() != null) {
                // there is a value set in the incoming data model
                disableValue = currentPresentSurvey.getDisabled().equals(BooleanEnum.TRUE) ? true : false;
            } else {
                // there is no value set in the incoming data model, set the
                // value to false since that is the default
                currentPresentSurvey.setDisabled(BooleanEnum.FALSE);
            }

            //update the check box
            view.getDisabledInput().setValue(disableValue); 

            //Initialize the mandatory controls visibility to hidden until the survey type can be determined
            view.setMandatoryControlsVisibility(false);
            
            // set mandatory value
            MandatoryOption mandatoryOption;
            if (currentPresentSurvey.getMandatoryOption() != null) {
                // there is a value set in the incoming data model
                mandatoryOption = currentPresentSurvey.getMandatoryOption();
            } else {
                // there is no value set in the incoming data model, provide it with a new instance
                currentPresentSurvey.setMandatoryOption(mandatoryOption = new MandatoryOption());
            }
            
            /* If the course author didn't specify a mandatoryBehavior, default to using a 
             * behavior that will always use existing learner state when available in order to 
             * skip the survey. */
            if(mandatoryOption.getMandatoryBehavior() == null) {
                SimpleMandatoryBehavior simpleBehavior = new SimpleMandatoryBehavior();
                simpleBehavior.setUseExistingLearnerStateIfAvailable(true);
                mandatoryOption.setMandatoryBehavior(simpleBehavior);
            }
            
            if(mandatoryOption.getMandatoryBehavior() instanceof FixedDecayMandatoryBehavior) {
                int learnerStateShelfLife;
                FixedDecayMandatoryBehavior fixedDecay = (FixedDecayMandatoryBehavior) mandatoryOption.getMandatoryBehavior();
                if(fixedDecay.getLearnerStateShelfLife() != null) {
                    learnerStateShelfLife = fixedDecay.getLearnerStateShelfLife().intValue() / DAYS_TO_MILLI;
                } else {
                    /* Use 1 as the default value of the learner state shelf life
                     * since 1 is the lowest value that can be entered by the author. */
                    learnerStateShelfLife = 1;
                    fixedDecay.setLearnerStateShelfLife(BigInteger.valueOf(learnerStateShelfLife * DAYS_TO_MILLI));
                }
                
                view.getLearnerStateShelfLife().setValue(learnerStateShelfLife);
            }

            updateMandatoryControls(mandatoryOption.getMandatoryBehavior());
            
    		//populate the Knowledge Assessment concepts table
	    	if(currentCourse != null){
	    		
	    		if(currentCourse.getConcepts() != null && currentCourse.getConcepts().getListOrHierarchy() != null){
	    			
	    			List<String> conceptNames = new ArrayList<String>();
	    			
	    			if(currentCourse.getConcepts().getListOrHierarchy() instanceof Hierarchy){
	    				
	    				Hierarchy conceptHierarchy = (Hierarchy) currentCourse.getConcepts().getListOrHierarchy();
	    				
	    				if(conceptHierarchy.getConceptNode() != null){
		    				
		    				ConceptNode currentNode = conceptHierarchy.getConceptNode();
		    				
		    				conceptNames.addAll(getChildConceptNames(currentNode));	    				
	    				}
	    				
	    			} else {
	    				
	    				Concepts.List conceptList = (Concepts.List) currentCourse.getConcepts().getListOrHierarchy();
	    				
	    				if(conceptList.getConcept() != null){
	    					
	    					for(Concept concept : conceptList.getConcept()){
	    						
	    						if(concept.getName() != null){
	    							conceptNames.add(concept.getName());
	    						}
	    					}
	    				}
	    			}
	    			
	    			for(String conceptName : conceptNames){
	    				
	    				boolean wasConceptFound = false;
	    				
	    				for(ConceptQuestions questions : currentConceptSurvey.getConceptQuestions()){
	    					
	    					if(questions.getName() != null && questions.getName().equals(conceptName)){
	    						wasConceptFound = true;
	    						break;
	    					}
	    				}
	    				
	    				if(wasConceptFound){
	    					
	    					conceptsTableDataProvider.getList().add(new CandidateConcept(conceptName, true));
	    					
	    				} else {
	    					conceptsTableDataProvider.getList().add(new CandidateConcept(conceptName, false));
	    				}
	    			}
	    			
	    			checkForExtraneousConcepts(conceptNames);
	    			
	    			conceptsTableDataProvider.refresh();
	    			
	    			shouldShowConceptsErrorMessage = false;
	    			
	    		} else {
	    			
	    			shouldShowConceptsErrorMessage = true;
	    		}    		
	    	}
	    	
	    	//populate the 'Questions:' and 'Scoring:' tables
	    	populateQuestionsAndScoringTables();
	        
    	} else {
    		errorOccurred = true;
    	}
    	
    	if(errorOccurred){
    		WarningDialog.error("Failed to build view", "An error occurred while populating the editor.");
    	}
    	
    	// Update the display of the survey choice panel (& sub widgets).
    	view.refreshView();
    }      
    
    /**
     * Gets the names of all concepts nodes under the given concept node using a breadth-first traversal
     * 
     * @param node the concept node to start with
     * @return the names of all concepts nodes under the given concept node
     */
    private List<String> getChildConceptNames(ConceptNode node){
    	
    	List<String> childConceptNames = new ArrayList<String>();
    	
    	if(node.getConceptNode() != null){
    		
    		if(node.getName() != null){
    			childConceptNames.add(node.getName());
    		}
    		
    		for(ConceptNode childNode : node.getConceptNode()){
    			childConceptNames.addAll(getChildConceptNames(childNode));
    		}
    	}
    	
    	return childConceptNames;
    }
    
	/**
	 * Updates the slider associated with the concept question
	 * 
	 * @param questions The concept questions containing the data to update the slider with
	 */
    private void updateSliderView(ConceptQuestions questions) {
    	if(questions.getAssessmentRules() != null) {
    		int total = 0;
    		total += questions.getQuestionTypes().getEasy().intValue();
    		total += questions.getQuestionTypes().getMedium().intValue();
    		total += questions.getQuestionTypes().getHard().intValue();
    		KnowledgeAssessmentSlider slider = view.updateSlider(questions.getName(), total, questions.getAssessmentRules());
    		
    		if(slider != null) {
    		    int[] level = slider.calculateAllLevels();
	    		questions.getAssessmentRules().getAboveExpectation().setNumberCorrect(BigInteger.valueOf(level[2]));
				questions.getAssessmentRules().getAtExpectation().setNumberCorrect(BigInteger.valueOf(level[1]));
				questions.getAssessmentRules().getBelowExpectation().setNumberCorrect(BigInteger.ZERO);
    		}
    	}
    }
    
    /**
	 * Populates the Knowledge Assessment questions and scoring tables based on what the user has selected in the concepts table. 
	 * 
	 * If a new concept has been selected, a new entry in these tables will be created and will be given Easy, Medium, and Hard values of 
	 * 1, 0, and 0 and Expert, Journeyman, and Novice values of 1, 0, 0 by default. 
	 * 
	 * Concepts that are deselected will be removed from the 'Questions:' and 'Scoring:' tables
	 */
	private void populateQuestionsAndScoringTables() {		
    	
    	List<ConceptQuestions> questionSetToDisplay = new ArrayList<ConceptQuestions>();
		
		//go through the concepts in the 'Concepts:' table
		for(CandidateConcept candidate : conceptsTableDataProvider.getList()){
			
			if(candidate.isChosen()){				
				
				ConceptQuestions questionsToAdd = null;	
				
				//if the user has selected a concept, get the existing concept questions for that concept if the MBP has any
				for(ConceptQuestions questions : currentConceptSurvey.getConceptQuestions()){
					
					if(questions.getName() != null && questions.getName().equals(candidate.getConceptName())){
						
						questionsToAdd = questions;
						break;
					}		
				}
				
				//if any values are missing, generate default values in their place so the entry for this concept can be displayed
				if(questionsToAdd == null){
					questionsToAdd = new ConceptQuestions();
				}
				
				if(questionsToAdd.getQuestionTypes() == null){
					questionsToAdd.setQuestionTypes(new QuestionTypes());
					questionsToAdd.setName(candidate.getConceptName());
				}
				
				if(questionsToAdd.getQuestionTypes().getEasy() == null){
					questionsToAdd.getQuestionTypes().setEasy(BigInteger.ONE);
				}
				
				if(questionsToAdd.getQuestionTypes().getMedium() == null){
					questionsToAdd.getQuestionTypes().setMedium(BigInteger.ZERO);
				}
				
				if(questionsToAdd.getQuestionTypes().getHard() == null){
					questionsToAdd.getQuestionTypes().setHard(BigInteger.ZERO);
				}
				
				if(questionsToAdd.getAssessmentRules() == null){
					questionsToAdd.setAssessmentRules(new AssessmentRules());
				}
				
				if(questionsToAdd.getAssessmentRules().getAboveExpectation() == null){
					questionsToAdd.getAssessmentRules().setAboveExpectation(new AboveExpectation());
				}
				
				if(questionsToAdd.getAssessmentRules().getAboveExpectation().getNumberCorrect() == null){
					questionsToAdd.getAssessmentRules().getAboveExpectation().setNumberCorrect(BigInteger.ONE);
				}
				
				if(questionsToAdd.getAssessmentRules().getAtExpectation() == null){
					questionsToAdd.getAssessmentRules().setAtExpectation(new AtExpectation());
				}
				
				if(questionsToAdd.getAssessmentRules().getAtExpectation().getNumberCorrect() == null){
					questionsToAdd.getAssessmentRules().getAtExpectation().setNumberCorrect(BigInteger.ZERO);
				}
				
				if(questionsToAdd.getAssessmentRules().getBelowExpectation() == null){
					questionsToAdd.getAssessmentRules().setBelowExpectation(new BelowExpectation());
				}
				
				if(questionsToAdd.getAssessmentRules().getBelowExpectation().getNumberCorrect() == null){
					questionsToAdd.getAssessmentRules().getBelowExpectation().setNumberCorrect(BigInteger.ZERO);
				}
				
				//now that the concept questions valid for displaying, add it to the list of questions to display
				questionSetToDisplay.add(questionsToAdd);
				
				final ConceptQuestions questions = questionsToAdd;
				final KnowledgeAssessmentSlider slider = view.appendSlider(candidate.getConceptName());
				if(slider != null) {
					updateSliderView(questions);
					slider.addSlideStopHandler(new SlideStopHandler<Range>() {
	
						@Override
						public void onSlideStop(SlideStopEvent<Range> event) {
						    int[] level = slider.calculateAllLevels();
							questions.getAssessmentRules().getAboveExpectation().setNumberCorrect(BigInteger.valueOf(level[2]));
							questions.getAssessmentRules().getAtExpectation().setNumberCorrect(BigInteger.valueOf(level[1]));
							questions.getAssessmentRules().getBelowExpectation().setNumberCorrect(BigInteger.ZERO);
						}
						
					});
				}
			}
		}
		
		view.refreshSliderPanel(conceptsTableDataProvider.getList());
		
		//repopulate the PresentSurvey's concept questions, since changes in the 'Concepts:' table may have affected them
		currentConceptSurvey.getConceptQuestions().clear();			
		currentConceptSurvey.getConceptQuestions().addAll(questionSetToDisplay);
		if(!extraneousConcepts.isEmpty()) {
			// Keep the extraneous concepts available
			currentConceptSurvey.getConceptQuestions().addAll(extraneousConcepts.values());
		}
		
		//set what to display in the 'Questions:' and 'Scoring:' table
		questionsTableDataProvider.setList(questionSetToDisplay);
		scoringTableDataProvider.setList(questionSetToDisplay);
		
		//refresh the data providers to update their corresponding displays
		questionsTableDataProvider.refresh();
		scoringTableDataProvider.refresh();
	}
    
	/**
	 * Checks for extraneous concept question names and adds them to the view
	 *  
	 * @param conceptNames The list of concept names available in the course.
	 */
	private void checkForExtraneousConcepts(List<String> conceptNames) {
		
		if(currentConceptSurvey != null) {
			if(extraneousConcepts.isEmpty()) {
				for(ConceptQuestions conceptQuestion : currentConceptSurvey.getConceptQuestions()) {
					if(!conceptNames.contains(conceptQuestion.getName())) {
						appendExtraneousSlider(conceptQuestion);
						extraneousConcepts.put(conceptQuestion.getName(), conceptQuestion);
					}
				}
			} else {
				for(ConceptQuestions conceptQuestion : extraneousConcepts.values()) {
					appendExtraneousSlider(conceptQuestion);
				}
			}
		}
	}
	
	/**
	 * Adds a slider representing an extraneous concept to the view
	 * 
	 * @param conceptQuestion The extraneous concept question 
	 */
	private void appendExtraneousSlider(final ConceptQuestions conceptQuestion) {
		int totalQuestions = 0;
		if(conceptQuestion.getQuestionTypes() != null) {
			totalQuestions += conceptQuestion.getQuestionTypes().getEasy().intValue();
			totalQuestions += conceptQuestion.getQuestionTypes().getMedium().intValue();
			totalQuestions += conceptQuestion.getQuestionTypes().getHard().intValue();
		} else {
			totalQuestions = 1;
		}
				
		view.appendExtraneousSlider(conceptQuestion.getName(), totalQuestions, conceptQuestion.getAssessmentRules(), new ScheduledCommand() {
			
			@Override
			public void execute() {
				extraneousConcepts.remove(conceptQuestion.getName());
				currentConceptSurvey.getConceptQuestions().remove(conceptQuestion);
			}
			
		});
	}
	
    /* (non-Javadoc)
     * @see mil.arl.gift.tools.authoring.gat.client.presenter.AbstractGatPresenter#start()
     */
    @Override
    public void start(){
    	super.start();
    	eventRegistration = eventBinder.bindEventHandlers(this, eventBus);
    }
    
    /* (non-Javadoc)
     * @see mil.arl.gift.tools.authoring.gat.client.presenter.AbstractGatPresenter#stop()
     */
    @Override
    public void stop(){
    	super.stop();
    }
    
    @EventHandler
	protected void onCourseConceptsChanged(CourseConceptsChangedEvent event){		
		
		//need to refresh the view whenever the course's concepts are changed
		populateView();
	}
    
    @EventHandler
    protected void onSurveysChanged(SurveysChangedEvent event){
        
        logger.info("Notified of surveys changed event.");
    	
    	//update the display whenever any surveys are changed, since it could affect the survey this widget references
    	if(view.getSurveyPicker() != null && !view.getSurveyPicker().equals(event.getSurveySource())){
    		view.getSurveyPicker().updateDisplay();
    	}
    	
        if(currentPresentSurvey != null){
            logger.info("Setting present survey course object useOriginal value to "+event.useOriginal());
            currentPresentSurvey.setSharedSurvey(event.useOriginal() ? BooleanEnum.TRUE : BooleanEnum.FALSE);
        }
    }
    
    @EventHandler
    protected void onQuestionBankChanged(QuestionBankChangedEvent event){
    	
    	//update the display whenever the question bank is changed, since it could affect the question bank this widget references
    	if(view.getSurveyPickerQuestionBank() != null && !view.getSurveyPickerQuestionBank().equals(event.getSurveySource())){
    		view.getSurveyPickerQuestionBank().updateDisplay();
    	}
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
    
    /**
     * Updates the visibility of all elements that are dependent on the value of the 
     * mandatory behavior
     * @param newBehavior the newly set value of the mandatory behavior
     */
    private void updateMandatoryControls(Serializable newBehavior) {
        
        if (logger.isLoggable(Level.INFO)) {
            logger.info("updateMandatoryVisibilities(" + newBehavior + ")");
        }
        
        MandatoryBehaviorOptionChoice behaviorEnum = null;
        
        boolean isShelfLifeVisible;
        boolean isBehaviorVisible;
        boolean isChecked;
        
        if(newBehavior instanceof SimpleMandatoryBehavior) {
            SimpleMandatoryBehavior simpleBehavior = (SimpleMandatoryBehavior) newBehavior;
            isShelfLifeVisible = false;
            if(simpleBehavior.isUseExistingLearnerStateIfAvailable()) {
                isChecked = false;
                isBehaviorVisible = false;
            } else {
                behaviorEnum = MandatoryBehaviorOptionChoice.ALWAYS;
                isChecked = true;
                isBehaviorVisible = true;
            }
        } else {
            behaviorEnum = MandatoryBehaviorOptionChoice.AFTER;
            isChecked = true;
            isBehaviorVisible = true;
            isShelfLifeVisible = true;
        }
        
        view.getMandatoryBehaviorSelectorHasVisibility().setVisible(isBehaviorVisible);
        view.getLearnerStateShelfLifeHasVisibility().setVisible(isShelfLifeVisible);
        view.getMandatoryCheckBox().setValue(isChecked);
        if(isBehaviorVisible) {
            view.getMandatoryBehaviorSelector().setValue(behaviorEnum);
        }
        
        if(isChecked){
            // expand the options panel by default so the user sees the mandatory widgets
            view.setSurveyOptionsVisible(true);
        }
    }
}
