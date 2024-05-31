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
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.extras.notify.client.ui.Notify;
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
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;

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
import generated.course.Course;
import generated.course.Example;
import generated.course.FixedDecayMandatoryBehavior;
import generated.course.Guidance;
import generated.course.ImageProperties;
import generated.course.LessonMaterial;
import generated.course.LessonMaterialList;
import generated.course.LtiConcepts;
import generated.course.LtiProperties;
import generated.course.LtiProvider;
import generated.course.MandatoryOption;
import generated.course.MerrillsBranchPoint;
import generated.course.MerrillsBranchPoint.Quadrants;
import generated.course.PDFProperties;
import generated.course.Practice;
import generated.course.Practice.PracticeConcepts;
import generated.course.Recall;
import generated.course.Recall.PresentSurvey;
import generated.course.Recall.PresentSurvey.ConceptSurvey;
import generated.course.Remediation;
import generated.course.Rule;
import generated.course.SimpleMandatoryBehavior;
import generated.course.SlideShowProperties;
import generated.course.TrainingApplication;
import generated.course.TrainingApplicationWrapper;
import generated.course.Transitions;
import generated.course.YoutubeVideoProperties;
import generated.course.VideoProperties;
import generated.metadata.ActivityType;
import generated.metadata.Attribute;
import generated.metadata.Attributes;
import generated.metadata.BooleanEnum;
import generated.metadata.Metadata;
import mil.arl.gift.common.EnumerationNotFoundException;
import mil.arl.gift.common.GenericDataProvider;
import mil.arl.gift.common.TrainingAppCourseObjectWrapper;
import mil.arl.gift.common.course.TrainingAppUtil;
import mil.arl.gift.common.enums.MerrillQuadrantEnum;
import mil.arl.gift.common.enums.MetadataAttributeEnum;
import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.common.enums.TrainingApplicationEnum;
import mil.arl.gift.common.enums.TrainingApplicationStateEnum;
import mil.arl.gift.common.gwt.client.GenericRpcResponse;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.BsLoadingDialogBox;
import mil.arl.gift.common.gwt.shared.ServerProperties;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileUtil;
import mil.arl.gift.common.metadata.MetadataWrapper;
import mil.arl.gift.common.metadata.QuadrantRequest;
import mil.arl.gift.common.survey.AbstractQuestion;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.event.EditorDirtyEvent;
import mil.arl.gift.tools.authoring.server.gat.client.event.course.CourseConceptsChangedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.event.course.CourseLoadedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.event.course.CourseLtiProvidersChangedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.event.course.CourseObjectDisabledEvent;
import mil.arl.gift.tools.authoring.server.gat.client.event.course.CourseObjectRenamedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.event.course.QuestionBankChangedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.event.course.TrainingApplicationImportEvent;
import mil.arl.gift.tools.authoring.server.gat.client.presenter.AbstractGatPresenter;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.mbp.MbpView;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.mbp.MbpView.CandidateConcept;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.mbp.MbpView.CandidateMetadataAttribute;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.mbp.MetadataFileDeletionDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.mbp.MetadataFileReferenceListDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.survey.KnowledgeAssessmentSlider;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.survey.PresentSurveyView.MandatoryBehaviorOptionChoice;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.ta.InteropsEditedCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.ta.PowerPointFileChangedCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.DeleteRemoveCancelCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.DeleteRemoveCancelDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.ErrorDetailsDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.WarningDialog;
import mil.arl.gift.tools.authoring.server.gat.shared.GwtSurveySystemProperties;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GenericGatServiceResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.DeleteMetadata;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.GenerateLessonMaterialReferenceFile;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.GenerateLessonMaterialReferenceFileResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.GenerateMetadataFile;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.GenerateMetadataFileResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.GenerateQuestionExportReferenceFile;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.GenerateTrainingAppReferenceFile;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.GenerateTrainingAppReferenceFileResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.GetMerrillQuadrantFiles;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.GetMerrillQuadrantFilesResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.GetMetadataFilesForMerrillQuadrant;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.GetPracticeApplications;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.GetPracticeApplicationsResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.DeleteWorkspaceFiles;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.StringListResult;
import mil.arl.gift.tools.authoring.server.gat.shared.model.course.PracticeApplicationObject;

public class MbpPresenter extends AbstractGatPresenter implements MbpView.Presenter {

    /**
     * Interface for the event binder for this class.  
     * @author cragusa
     */
    interface MyEventBinder extends EventBinder<MbpPresenter> {
    }
    
    private static final Logger logger = Logger.getLogger(MbpPresenter.class.getName());   
    
    /** Binder for handling events. */
    private static final MyEventBinder eventBinder = GWT
            .create(MyEventBinder.class);
    
    private static int DEFAULT_RECALL_ALLOWED_ATTEMPTS;
    private static int DEFAULT_PRACTICE_ALLOWED_ATTEMPTS;
    
    private MbpView view;
    
    /** The course currently being edited. Used to get the list of concepts. */
    private Course currentCourse = null;
    
    /** The current course folder path. */
    private String currentCoursePath = "";
    
    /** The path to the course folder. */
    private String courseFolderPath = "";
    
    /** The Merrill's Branch Point currently being edited */
    private MerrillsBranchPoint currentMbp; 
    
    /** Metadata object used to edit metadata and upload it to the server */
    private Metadata currentMetadata;
    
    /** Data provider for the 'Concepts:' table */
    private ListDataProvider<CandidateConcept> conceptsTableDataProvider = new ListDataProvider<CandidateConcept>();
    
    /** Data provider for the Practice 'Concepts:' table */
    private ListDataProvider<CandidateConcept> practiceConceptsTableDataProvider = new ListDataProvider<CandidateConcept>();
    
    /** Data provider for the 'Questions:' table */
    private ListDataProvider<ConceptQuestions> questionsTableDataProvider = new ListDataProvider<ConceptQuestions>();
    
    /** Data provider for the 'Scoring:' table */
    private ListDataProvider<ConceptQuestions> scoringTableDataProvider = new ListDataProvider<ConceptQuestions>();
    
    // Fields related to the 'Add Content' and 'Add Application' dialogs ------------------------------------------------------------------
    
    /** The list of LTI Providers */
    private GenericDataProvider<LtiProvider> contentLtiProvidersDataProvider = new GenericDataProvider<LtiProvider>();  
    
    /** Data provider for the 'Concepts:' table in the 'Add Content' and 'Add Application' dialogs */
    private ListDataProvider<CandidateConcept> contentConceptsTableDataProvider = new ListDataProvider<CandidateConcept>();
    
    /** Selection model for the 'Concepts:' table in the 'Add Content' and 'Add Application' dialogs */
    private SingleSelectionModel<CandidateConcept> contentConceptsTableSelectionModel = new SingleSelectionModel<CandidateConcept>();
    
    /** Data provider for the 'Attributes:' table in the 'Add Content' and 'Add Application' dialogs */
    private ListDataProvider<CandidateMetadataAttribute> contentAttributesTableDataProvider = new ListDataProvider<CandidateMetadataAttribute>();

    /** An enum used to identify whether the 'Add Content' or 'Add Application' dialog is currently showing and adjust data accordingly */
    private static enum DialogEditingMode{
    	CONTENT,
    	APPLICATION;
    }
    
    private static final int DAYS_TO_MILLI = 24 * 60 * 60 * 1000;
    
    /** Keeps track of extraneous concept questions in Recall since it is overwritten when this widget is loaded */
    private static HashMap<String, ConceptQuestions> extraneousConcepts = new HashMap<String, ConceptQuestions>();
    
    /** The current dialog editing mode */
    private DialogEditingMode currentDialogMode = DialogEditingMode.CONTENT;
    
    /** Whether or not the transition error dialog has been shown*/
    private boolean hasTransitionErrorBeenShown = false;
            
    private Practice currentPractice = null;
    
    /**
     * Value of the allowed recall attempts used when the allowed recall attempts checkbox value is changed.
     * This value is null when the presenter is loaded for the first time until the allowed recall attempts
     * checkbox value is changed.
     **/
    private Integer lastRecallAttempts = null;
    
    /**
     * Value of the allowed practice attempts used when the allowed practice attempts checkbox value is changed.
     * This value is null when the presenter is loaded for the first time until the allowed practice attempts
     * checkbox value is changed.
     **/
    private Integer lastPracticeAttempts = null;
    
    private Timer contentFileRefreshTimer = new Timer(){

		@Override
		public void run() {
            logger.info("calling refresh content files for content file refresh timer");
			refreshContentFiles(null);
		}
    	
    };
    
    /**
     * Instantiates a new merrill's branch point presenter.
     */
    public MbpPresenter(MbpView view) {       
    	super();
    	
    	//Fetches the default attempts string values from the GatClientUtility
    	ServerProperties properties = GatClientUtility.getServerProperties();
    	String recallString = properties.getPropertyValue(ServerProperties.DEFAULT_MBP_RECALL_ALLOWED_ATTEMPTS);
    	String practiceString = properties.getPropertyValue(ServerProperties.DEFAULT_MBP_PRACTICE_ALLOWED_ATTEMPTS);
    	
    	//Parses the strings as integers
    	DEFAULT_RECALL_ALLOWED_ATTEMPTS = Integer.parseInt(recallString);
    	DEFAULT_PRACTICE_ALLOWED_ATTEMPTS = Integer.parseInt(practiceString);
    	
    	this.view = view;
    	
    	start();
    	
    	init();
    }
    
    /**
     * Loads the given {@link MerrillsBranchPoint} into the view for editing
     * 
     * @param mbp the mbp to edit
     */
    public void edit(MerrillsBranchPoint mbp, Course course){
    	
    	this.currentMbp = mbp;
    	
        currentCourse = course;
        currentCoursePath = GatClientUtility.getBaseCourseFolderPath();
        
        String courseName = GatClientUtility.getCourseFolderName(currentCoursePath);
        courseFolderPath = currentCoursePath.substring(0, currentCoursePath.indexOf(courseName) + courseName.length());
        makeDataValidForView();
        populateView();
    }
    
    @Override
    protected Logger getLogger() {
        return logger;
    }
    
    private void init() { 
        
        if(GatClientUtility.isReadOnly()){
            logger.info("Setting to read only");
            
            view.getAddApplicationButtonHasVisibility().setVisible(false);
            view.getAddExampleContentButtonHasVisibility().setVisible(false);
            view.getAddRuleContentButtonHasVisibility().setVisible(false);
            view.getAddRemediationContentButtonHasVisibility().setVisible(false);
            view.getRuleGuidanceCreator().setEnabled(false);
            view.getExampleGuidanceCreator().setEnabled(false);
            view.getRecallGuidanceCreator().setEnabled(false);
            view.getShowPracticePanelCheckboxHasEnabled().setEnabled(false);
            view.getShowPracticeAllowedAttemptsCheckBoxHasEnabled().setEnabled(false);
            view.getPracticeAllowedAttemptsSpinnerHasEnabled().setEnabled(false);
            view.getShowRecallAllowedAttemptsCheckBoxHasEnabled().setEnabled(false);
            view.getRecallAllowedAttemptsSpinnerHasEnabled().setEnabled(false);
            view.getDisabledInputHasEnabled().setEnabled(false);
            view.getExcludeRuleExampleContentCheckBoxHasEnabled().setEnabled(false);
            view.getSelectedConceptsPanel().setVisible(false);  //don't show the course concept picker
            view.getSelectedConceptsTagBlockerPanel().block();  //block click events to prevent removing selected course concepts
            view.getSelectedPracticeConceptsPanel().setVisible(false); //don't show the practice course concept picker
            view.getMandatoryCheckBoxHasEnabled().setEnabled(false);
        }

        // LTI, setting the content and remediation content reference editor's concepts and LTI provider lists
        conceptsTableDataProvider.addDataDisplay(view.getContentReferenceEditor().getMediaPanel().getConceptsTable());
        conceptsTableDataProvider.addDataDisplay(view.getRemediationContentReferenceEditor().getMediaPanel().getConceptsTable());
        contentLtiProvidersDataProvider.createChild(view.getContentReferenceEditor().getMediaPanel().getCourseLtiProviderList());
        contentLtiProvidersDataProvider.createChild(view.getRemediationContentReferenceEditor().getMediaPanel().getCourseLtiProviderList());
        
        view.getCourseConceptsSelectedPracticeConcepts().addValueChangeHandler(new ValueChangeHandler<List<String>>() {

            @Override
            public void onValueChange(ValueChangeEvent<List<String>> selectedPracticeConcepts) {

                logger.info("practice concepts are now "+selectedPracticeConcepts.getValue());
                
                //update Practice data model
                Iterator<CandidateConcept> itr = practiceConceptsTableDataProvider.getList().iterator();
                while(itr.hasNext()){
                    CandidateConcept candidateConcept = itr.next();
                    String conceptName = candidateConcept.getConceptName();
                    if(selectedPracticeConcepts.getValue().contains(conceptName)){
                        candidateConcept.setChosen(true);
                    }else{
                        
                        int checkOnLearningConceptIndex = conceptsTableDataProvider.getList().indexOf(candidateConcept);
                        if(checkOnLearningConceptIndex != -1){
                            //don't allow deselect if the concept is in the check on learning concepts
                            CandidateConcept checkOnLearnerConcept = conceptsTableDataProvider.getList().get(checkOnLearningConceptIndex);
                            candidateConcept.setChosen(checkOnLearnerConcept.isChosen());
                        }

                    }
                    
                    updatePracticeQuadrant(candidateConcept);
                }

            }
        });
        
        view.getCourseConceptsSelectedConcepts().addValueChangeHandler(new ValueChangeHandler<List<String>>() {

            @Override
            public void onValueChange(ValueChangeEvent<List<String>> selectedConcepts) {
                
                logger.info("Check on learner concepts are now "+selectedConcepts.getValue());

                // update the conceptsTableDataProvider data model
                Iterator<CandidateConcept> itr = conceptsTableDataProvider.getList().iterator();
                while(itr.hasNext()){
                    CandidateConcept candidateConcept = itr.next();
                    String conceptName = candidateConcept.getConceptName();
                    boolean wasChosenBefore = candidateConcept.isChosen();
                    
                    int practiceConceptIndex = practiceConceptsTableDataProvider.getList().indexOf(candidateConcept);
                    
                    if(selectedConcepts.getValue().contains(conceptName)){
                        logger.info("Updating to chosen for course concept "+conceptName);
                        candidateConcept.setChosen(true);
                        
                        //make sure the data model has the concept
                        if(currentMbp != null && currentMbp.getConcepts() != null && !currentMbp.getConcepts().getConcept().contains(conceptName)){
                            currentMbp.getConcepts().getConcept().add(conceptName);
                        }
                        
                        // select in practice concepts
                        if(practiceConceptIndex != -1){
                            practiceConceptsTableDataProvider.getList().get(practiceConceptIndex).setChosen(true);
                            practiceConceptsTableDataProvider.getList().get(practiceConceptIndex).setRemainSelected(true);
                        }
                    }else{
                        logger.info("Updating to NOT chosen for course concept "+conceptName+", practice concept list index = "+practiceConceptIndex+", chosen before ="+wasChosenBefore);
                        candidateConcept.setChosen(false);
                        
                        //make sure the data model has the concept REMOVED
                        if(currentMbp != null && currentMbp.getConcepts() != null && currentMbp.getConcepts().getConcept().contains(conceptName)){
                            currentMbp.getConcepts().getConcept().remove(conceptName);
                        }
                        
                        // deselect from practice concepts
                        
                        if(practiceConceptIndex != -1 && wasChosenBefore){
                            // keep the practice concept if this is not an inherited check on learning concept that was just deselected
                            practiceConceptsTableDataProvider.getList().get(practiceConceptIndex).setRemainSelected(false);
                            practiceConceptsTableDataProvider.getList().get(practiceConceptIndex).setChosen(false);
                        }
                        
                        view.removeSlider(conceptName);
                    }
                    
                    if(practiceConceptIndex != -1 && getPractice() != null){ 
                        logger.info("Updating practice quadrants concept "+practiceConceptsTableDataProvider.getList().get(practiceConceptIndex));
                        updatePracticeQuadrant(practiceConceptsTableDataProvider.getList().get(practiceConceptIndex));
                        eventBus.fireEvent(new EditorDirtyEvent());
                    }                    

                }//end while  
                               
                // update practice concepts multiple select and tags input
                view.updatePracticeConcepts(selectedConcepts.getValue());
                
                conceptsTableDataProvider.refresh();                
                
                populateQuestionsAndScoringTables();
                
                contentFileRefreshTimer.cancel();
                contentFileRefreshTimer.schedule(2500);
                
                eventBus.fireEvent(new EditorDirtyEvent());
            }
        });        
        
        // intialize the 'Rule Phase:' components
        
        handlerRegistrations.add(view.getAddRuleContentButton().addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				
				if(GatClientUtility.isReadOnly()){
					
					WarningDialog.error("Read only", "Rule content cannot be added in Read-Only mode.");
					
					return;
				}
				
				logger.info("Creating Add Rule content dialog");
				
				currentDialogMode = DialogEditingMode.CONTENT;				
				
    			currentMetadata = new Metadata();
                generated.metadata.PresentAt presentAt = new generated.metadata.PresentAt();
                presentAt.setMerrillQuadrant(MerrillQuadrantEnum.RULE.getName());
                currentMetadata.setPresentAt(presentAt);
    			
    			validateCurrentContent();
				
    			view.setContentDialogTitle("Add Rule Content");
    			view.getContentReferenceEditor().edit(currentMetadata);
				view.showAddContentDialog();
				
				populateAddFileDialogConcepts();
			}
		}));
        
        handlerRegistrations.add(view.getRuleGuidanceCreator().addValueChangeHandler(new ValueChangeHandler<Guidance>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<Guidance> event) {
				
				if(event.getValue() != null){
				    
				    logger.info("Adding after rule guidance");
				
					Transitions transitions = new Transitions();
					transitions.getTransitionType().clear();
					transitions.getTransitionType().add(event.getValue());
					
					event.getValue().setTransitionName(currentMbp.getTransitionName() + " - After Rule Guidance");
					
					setTransitions(MerrillQuadrantEnum.RULE, transitions);
					
				} else {
				    
				    logger.info("Nullify after rule guidance");
					
					setTransitions(MerrillQuadrantEnum.RULE, null);
				}
				
				view.getRuleTransitionsWarning().setHTML("");
			}
		}));
             
        // initialize 'Example Phase:' components
        
        handlerRegistrations.add(view.getAddExampleContentButton().addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				
				if(GatClientUtility.isReadOnly()){
					
					WarningDialog.error("Read only", "Example content cannot be added in Read-Only mode.");
					
					return;
				}
				
				logger.info("Creating Add Example content dialog");
				
				currentDialogMode = DialogEditingMode.CONTENT;
				
				currentMetadata = new Metadata();
                generated.metadata.PresentAt presentAt = new generated.metadata.PresentAt();
                presentAt.setMerrillQuadrant(MerrillQuadrantEnum.EXAMPLE.getName());
                currentMetadata.setPresentAt(presentAt);
    			
    			validateCurrentContent();
				
    			view.setContentDialogTitle("Add Example Content");
    			view.getContentReferenceEditor().edit(currentMetadata);
				view.showAddContentDialog();
				
				populateAddFileDialogConcepts();
			}
		}));
        
        handlerRegistrations.add(view.getExampleGuidanceCreator().addValueChangeHandler(new ValueChangeHandler<Guidance>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<Guidance> event) {
				
				if(event.getValue() != null){
				
					Transitions transitions = new Transitions();
					transitions.getTransitionType().clear();
					transitions.getTransitionType().add(event.getValue());
					
					event.getValue().setTransitionName(currentMbp.getTransitionName() + " - After Example Guidance");
					
					setTransitions(MerrillQuadrantEnum.EXAMPLE, transitions);
					
				} else {
					
					setTransitions(MerrillQuadrantEnum.EXAMPLE, null);
				}
				
				view.getExampleTransitionsWarning().setHTML("");
			}
		}));
        
        // initialize 'Remediation Phase:' components
        
        handlerRegistrations.add(view.getAddRemediationContentButton().addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				
				if(GatClientUtility.isReadOnly()){
					
					WarningDialog.error("Read only", "Remediation content cannot be added in Read-Only mode.");
					
					return;
				}
				
				currentDialogMode = DialogEditingMode.CONTENT;				
				
    			currentMetadata = new Metadata();
    			generated.metadata.PresentAt presentAt = new generated.metadata.PresentAt();
    			presentAt.setRemediationOnly(BooleanEnum.TRUE);
    			currentMetadata.setPresentAt(presentAt);
    			
    			validateCurrentRemediationContent();
				
    			view.setContentDialogTitle("Add Remediation Content");
    			view.getRemediationContentReferenceEditor().edit(currentMetadata);
				view.showAddRemediationDialog();
				
				populateAddFileDialogConcepts();
			}
		}));
        
        
        // initialize the 'Recall phase:' components
        
        handlerRegistrations.add(view.getRecallGuidanceCreator().addValueChangeHandler(new ValueChangeHandler<Guidance>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<Guidance> event) {
				
				if(event.getValue() != null){
				
					Transitions transitions = new Transitions();
					transitions.getTransitionType().clear();
					transitions.getTransitionType().add(event.getValue());
					
					event.getValue().setTransitionName(currentMbp.getTransitionName() + " - After Recall Guidance");
					
					setTransitions(MerrillQuadrantEnum.RECALL, transitions);
					
				} else {
					
					setTransitions(MerrillQuadrantEnum.RECALL, null);
				}
				
				view.getRecallTransitionsWarning().setHTML("");
			}
		}));
        
        handlerRegistrations.add(view.getShowRecallAllowedAttemptsCheckBox().addValueChangeHandler(new ValueChangeHandler<Boolean>(){

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> newValue) {
                Recall recall = getRecall();
                
                if(newValue.getValue()) {
					view.getRecallAllowedAttemptsPanel().setVisible(newValue.getValue());

                    //Synchronizes the spinners value
                    if(recall != null && recall.getAllowedAttempts() != null && recall.getAllowedAttempts().intValue() != 0) {
                        view.getRecallAllowedAttemptsSpinner().setValue(recall.getAllowedAttempts().intValue());
                    } else if (lastRecallAttempts != null) {
                        view.getRecallAllowedAttemptsSpinner().setValue(lastRecallAttempts);
                    } else {
                        view.getRecallAllowedAttemptsSpinner().setValue(DEFAULT_RECALL_ALLOWED_ATTEMPTS);
                    }
                } else {
					view.getRecallAllowedAttemptsPanel().setVisible(false);

                    if(recall != null) {
                    	// Save the last value of the spinner to display when the checkbox is checked again 
                    	if (recall.getAllowedAttempts() != null && recall.getAllowedAttempts().intValue() != 0) {
                    		lastRecallAttempts = recall.getAllowedAttempts().intValue();
                    	}

						recall.setAllowedAttempts(null);
                    }
                }
            }
            
        }));
        
        handlerRegistrations.add(view.getRecallAllowedAttemptsSpinner().addValueChangeHandler(new ValueChangeHandler<Integer>(){

            @Override
            public void onValueChange(ValueChangeEvent<Integer> newValue) {
                Recall recall = getRecall();
                
                if(recall != null) {
                    recall.setAllowedAttempts(BigInteger.valueOf(newValue.getValue()));
                }
            }
            
        }));
        
        handlerRegistrations.add(view.getDisabledInput().addValueChangeHandler(new ValueChangeHandler<Boolean>(){
            
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                
                if(currentMbp != null){        
                    
                    currentMbp.setDisabled(event.getValue() != null && event.getValue()
                            ? generated.course.BooleanEnum.TRUE
                            : generated.course.BooleanEnum.FALSE
                    );
                                        
                    eventBus.fireEvent(new EditorDirtyEvent());
                                        
                    SharedResources.getInstance().getEventBus().fireEvent(new CourseObjectDisabledEvent(currentMbp));
                }
            }
            
        }));

        handlerRegistrations.add(view.getExcludeRuleExampleContentCheckBox().addValueChangeHandler(new ValueChangeHandler<Boolean>() {
        
        
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> newValue) {
                
                Remediation remediation = getRemediation();
                if(remediation == null){
                    remediation = new Remediation();
                    
                    //add new remediation after recall
                    Quadrants quadrants = currentMbp.getQuadrants();
                    for(int index = 0; index < quadrants.getContent().size(); index++){
                        
                        Serializable quadrant = quadrants.getContent().get(index);
                        if(quadrant instanceof Recall){
                            
                            if(logger.isLoggable(Level.INFO)){
                                logger.info("Adding remediation element to Mbp course object named "+currentMbp.getTransitionName()+".");
                            }
                            quadrants.getContent().add(index+1, remediation);
                        }
                    }
                }
                
                remediation.setExcludeRuleExampleContent(newValue.getValue() ? generated.course.BooleanEnum.TRUE : generated.course.BooleanEnum.FALSE);
                
                // refresh remediation phase table if shown
                logger.info("calling refresh content files for excluding rule-example remediation checkbox");
                refreshContentFiles(MerrillQuadrantEnum.REMEDIATION_AFTER_RECALL);
                
                eventBus.fireEvent(new EditorDirtyEvent());
            }
        }));
        
        
        // initialize the 'Questions:' table
        
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
        
        
        // initialize the 'Practice Phase:' components
        
        handlerRegistrations.add(view.getAddApplicationButton().addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				
				if(GatClientUtility.isReadOnly()){
					
					WarningDialog.error("Read only", "Practice applications cannot be added in Read-Only mode.");
					
					return;
				}
				
				if(getPractice().getPracticeConcepts().getCourseConcept().isEmpty()) {
					
					WarningDialog.error("No Practice Concepts Selected", "Please select one or more concepts before adding an application.");
					
					return;
				}
				
    			 			
    			TrainingApplication trainingApp = new TrainingApplication();		    			
    			trainingApp.setFinishedWhen(TrainingApplicationStateEnum.STOPPED.getDisplayName());
    			
    			view.getPracticeInteropEditor().setTrainingApplication(trainingApp);
    			view.getPracticeGuidanceCreator().setValue(trainingApp.getGuidance());
    			
    			currentMetadata = new Metadata();
                generated.metadata.PresentAt presentAt = new generated.metadata.PresentAt();
                presentAt.setMerrillQuadrant(MerrillQuadrantEnum.PRACTICE.getName());
                currentMetadata.setPresentAt(presentAt);
    			
    			validateCurrentPracticeApplication();
    			
    			currentDialogMode = DialogEditingMode.APPLICATION;
    			
    			view.setApplicationDialogTitle("Add Practice Application");
				view.showAddApplicationDialog();
				
				populateAddFileDialogConcepts();
			}
		}));
        
        handlerRegistrations.add(view.getShowPracticeAllowedAttemptsCheckBox().addValueChangeHandler(new ValueChangeHandler<Boolean>(){

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> arg0) {
                Practice practice = getPractice();
                
                if(arg0.getValue()) {
					view.getPracticeAllowedAttemptsPanel().setVisible(arg0.getValue());
                    
                    //Update the spinner value to match
                    if(practice != null && practice.getAllowedAttempts() != null && practice.getAllowedAttempts().intValue() != 0) {
                        view.getPracticeAllowedAttemptsSpinner().setValue(practice.getAllowedAttempts().intValue());
                    } else if (lastPracticeAttempts != null) {
                    	view.getPracticeAllowedAttemptsSpinner().setValue(lastPracticeAttempts);
                    } else {
                        view.getPracticeAllowedAttemptsSpinner().setValue(DEFAULT_PRACTICE_ALLOWED_ATTEMPTS);
                    }
                } else {
                	view.getPracticeAllowedAttemptsPanel().setVisible(false);

                    if(practice != null) {
                    	// Save the last value of the spinner to display when the checkbox is checked again 
                    	if (practice.getAllowedAttempts() != null && practice.getAllowedAttempts().intValue() != 0) {
                    		lastPracticeAttempts = practice.getAllowedAttempts().intValue();
                    	}

                        practice.setAllowedAttempts(null);
                    }
                }
            }
            
        }));

        handlerRegistrations.add(view.getPracticeAllowedAttemptsSpinner().addValueChangeHandler(new ValueChangeHandler<Integer>(){

            @Override
            public void onValueChange(ValueChangeEvent<Integer> arg0) {
                Practice practice = getPractice();
                
                if(practice != null) {
                    practice.setAllowedAttempts(BigInteger.valueOf(arg0.getValue()));
                }
            }
            
        }));
        
        
        // initialize the 'Concepts:' table in the 'Add Content' and 'Add Application' dialogs
        
        view.getContentConceptsTable().setSelectionModel(contentConceptsTableSelectionModel);
        view.getRemediationConceptsTable().setSelectionModel(contentConceptsTableSelectionModel);
        view.getApplicationConceptsTable().setSelectionModel(contentConceptsTableSelectionModel);
        
        contentConceptsTableDataProvider.addDataDisplay(view.getContentConceptsTable());
        contentConceptsTableDataProvider.addDataDisplay(view.getApplicationConceptsTable());
        contentConceptsTableDataProvider.addDataDisplay(view.getRemediationConceptsTable());
        
        contentConceptsTableSelectionModel.addSelectionChangeHandler(new Handler() {
			
			@Override
			public void onSelectionChange(SelectionChangeEvent event) {
				
			    logger.info("contentConceptsTableSelectionModel.onSelectionChange");
			    refreshContentAttributesTable();
			}
		});
        
        view.setContentConceptSelectionColumnFieldUpdater(new FieldUpdater<CandidateConcept, Boolean>() {

			@Override
			public void update(int index, CandidateConcept candidate, Boolean hasBeenSelected) {
			    
				if(currentMetadata != null){
					
					if(currentMetadata.getConcepts() == null){
						currentMetadata.setConcepts(new generated.metadata.Metadata.Concepts());
					}
					
					generated.metadata.Concept concept = null;
					
					for(generated.metadata.Concept existingConcept : currentMetadata.getConcepts().getConcept()){
						
						if(existingConcept.getName() != null && existingConcept.getName().equals(candidate.getConceptName())){
							
							concept = existingConcept;
						}
					}
					
					logger.info("ContentConceptSelectionColumnFieldUpdater: candidateConcept = "+candidate+", hasBeenSelected = "+hasBeenSelected+", previouslySelected = "+concept);
					if(hasBeenSelected){
						
						if(concept == null){
							
							concept = new generated.metadata.Concept();
							concept.setName(candidate.getConceptName());
							
							logger.info("setContentConceptSelectionColumnFieldUpdater: Adding concept "+concept.getName()+" to currentMetadata");
							candidate.setChosen(currentMetadata.getConcepts().getConcept().add(concept));
							
						} else {
							candidate.setChosen(true);
						}
						
					} else {
						
						if(concept != null){
                            logger.info("setContentConceptSelectionColumnFieldUpdater: Removing concept "+concept.getName()+" from currentMetadata");
							candidate.setChosen(!currentMetadata.getConcepts().getConcept().remove(concept));
							
						} else {
							candidate.setChosen(false);
						}
					}
					
					populateAddFileDialogConcepts();
					
					if(hasBeenSelected && concept != null){					
						selectContentConceptName(concept.getName());
					}
					
					view.updateContentOnConceptChange(candidate.getConceptName(), candidate.isChosen());
	                   
                    // now that the currentMetadata has possibly been changed, 
					// recheck if the attributes table choices should be shown
                    refreshContentAttributesTable();
                    
					validateCurrentContent();
				}
			}
		});  
        
        view.setRemediationConceptSelectionColumnFieldUpdater(new FieldUpdater<CandidateConcept, Boolean>() {

			@Override
			public void update(int index, CandidateConcept candidate, Boolean hasBeenSelected) {

				if(currentMetadata != null){
					
					if(currentMetadata.getConcepts() == null){
						currentMetadata.setConcepts(new generated.metadata.Metadata.Concepts());
					}
					
					generated.metadata.Concept concept = null;
					
					for(generated.metadata.Concept existingConcept : currentMetadata.getConcepts().getConcept()){
						
						if(existingConcept.getName() != null && existingConcept.getName().equals(candidate.getConceptName())){
							
							concept = existingConcept;
						}
					}
					
					if(hasBeenSelected){
						
						if(concept == null){
							
							concept = new generated.metadata.Concept();
							
							updateActivityType(concept);
                            
                            concept.setName(candidate.getConceptName());
                            
                            logger.info("setRemediationConceptSelectionColumnFieldUpdater: Adding concept "+concept.getName()+" to currentMetadata");
                            candidate.setChosen(currentMetadata.getConcepts().getConcept().add(concept));
							
						} else {
							candidate.setChosen(true);
						}
						
					} else {
						
						if(concept != null){
						    logger.info("setRemediationConceptSelectionColumnFieldUpdater: Removing concept "+concept.getName()+" from currentMetadata");
							candidate.setChosen(!currentMetadata.getConcepts().getConcept().remove(concept));
							
						} else {
							candidate.setChosen(false);
						}
					}
					
					populateAddFileDialogConcepts();
					
					if(hasBeenSelected && concept != null){					
						selectContentConceptName(concept.getName());
					}
					
					view.updateContentOnConceptChange(candidate.getConceptName(), candidate.isChosen());
					
					// now that the currentMetadata has possibly been changed, 
                    // recheck if the attributes table choices should be shown
                    refreshContentAttributesTable();
					
					validateCurrentRemediationContent();
				}
			}
		});  
        
        view.setApplicationConceptSelectionColumnFieldUpdater(new FieldUpdater<CandidateConcept, Boolean>() {

			@Override
			public void update(int index, CandidateConcept candidate, Boolean hasBeenSelected) {

				if(currentMetadata != null){
					
					if(currentMetadata.getConcepts() == null){
						currentMetadata.setConcepts(new generated.metadata.Metadata.Concepts());
					}
					
					generated.metadata.Concept concept = null;
					
					for(generated.metadata.Concept existingConcept : currentMetadata.getConcepts().getConcept()){
						
						if(existingConcept.getName() != null && existingConcept.getName().equals(candidate.getConceptName())){
							
							concept = existingConcept;
						}
					}
					
					if(hasBeenSelected){
						
						if(concept == null){
							
							concept = new generated.metadata.Concept();
							concept.setName(candidate.getConceptName());
							
							logger.info("setApplicationConceptSelectionColumnFieldUpdater: Adding concept "+concept.getName()+" to currentMetadata");
							candidate.setChosen(currentMetadata.getConcepts().getConcept().add(concept));
							
						} else {
							candidate.setChosen(true);
						}
						
					} else {
						
						if(concept != null){
						    logger.info("setApplicationConceptSelectionColumnFieldUpdater: Removing concept "+concept.getName()+" from currentMetadata");
							candidate.setChosen(!currentMetadata.getConcepts().getConcept().remove(concept));
							
						} else {
							candidate.setChosen(false);
						}
					}
					
					populateAddFileDialogConcepts();
					
					if(hasBeenSelected && concept != null){					
						selectContentConceptName(concept.getName());
					}
					
					// now that the currentMetadata has possibly been changed, 
                    // recheck if the attributes table choices should be shown
                    refreshContentAttributesTable();
					
					validateCurrentPracticeApplication();
				}
			}
		});  
        
        
        // initialize the 'Attributes:' table in the 'Add Content' and 'Add Application' dialogs       

        view.setContentAttributeSelectionColumnFieldUpdater(new FieldUpdater<CandidateMetadataAttribute, Boolean>() {

			@Override
			public void update(int index, CandidateMetadataAttribute candidate, Boolean hasBeenSelected) {
			    
                if(candidate.getParentConcept().getActivityType() == null){
                    candidate.getParentConcept().setActivityType(new ActivityType());
                }
                
                Serializable parentConceptActivity = candidate.getParentConcept().getActivityType().getType();
                generated.metadata.Attributes attributes = null;
                if(parentConceptActivity instanceof generated.metadata.ActivityType.Passive){                   
                    
                    if(((generated.metadata.ActivityType.Passive)parentConceptActivity).getAttributes() == null){
                        
                        attributes = new generated.metadata.Attributes(); 
                        ((generated.metadata.ActivityType.Passive)parentConceptActivity).setAttributes(attributes);
                        
                    }else{                      
                        attributes = ((generated.metadata.ActivityType.Passive)parentConceptActivity).getAttributes();
                    }
                }else{
                    
                    //either null, constructive or active - set to passive
                    candidate.getParentConcept().setActivityType(new generated.metadata.ActivityType());
                    generated.metadata.ActivityType.Passive passive = new generated.metadata.ActivityType.Passive();
                    candidate.getParentConcept().getActivityType().setType(passive);
                    attributes = new generated.metadata.Attributes();
                    passive.setAttributes(attributes);
                }
				
				Attribute attribute = null;
				
                for(Attribute existingAttribute : attributes.getAttribute()){
                    
                    if(existingAttribute.getValue() != null && candidate.getAttribute().getName().equals(existingAttribute.getValue())){
                        attribute = existingAttribute;
                    }
                }			
				
                if(hasBeenSelected){
                    
                    if(attribute == null){
                        
                        attribute = new Attribute();
                        attribute.setValue(candidate.getAttribute().getName());
                        
                        candidate.setChosen(attributes.getAttribute().add(attribute));
                        
                    } else {
                        candidate.setChosen(true);
                    }
                    
                } else {
                    
                    if(attribute != null){                      
                        candidate.setChosen(!attributes.getAttribute().remove(attribute));
                    
                    } else {
                        candidate.setChosen(false);
                    }
                }
				
				validateCurrentContent();
			}
		});
        
        view.setRemediationAttributeSelectionColumnFieldUpdater(new FieldUpdater<CandidateMetadataAttribute, Boolean>() {

			@Override
			public void update(int index, CandidateMetadataAttribute candidate, Boolean hasBeenSelected) {
			    
			    if(candidate.getParentConcept().getActivityType() == null){
                    candidate.getParentConcept().setActivityType(new ActivityType());
                }
				
			    Serializable parentConceptActivity = candidate.getParentConcept().getActivityType().getType();
			    generated.metadata.Attributes attributes = null;
			    if(parentConceptActivity instanceof generated.metadata.ActivityType.Passive){			        
			        
			        if(((generated.metadata.ActivityType.Passive)parentConceptActivity).getAttributes() == null){
    			        attributes = new generated.metadata.Attributes(); 
    			        ((generated.metadata.ActivityType.Passive)parentConceptActivity).setAttributes(attributes);
			        }else{
			            attributes = ((generated.metadata.ActivityType.Passive)parentConceptActivity).getAttributes();
			        }
				}else{
				    //either null, constructive or active - set to passive
                    candidate.getParentConcept().setActivityType(new generated.metadata.ActivityType());
                    generated.metadata.ActivityType.Passive passive = new generated.metadata.ActivityType.Passive();
                    candidate.getParentConcept().getActivityType().setType(passive);
                    attributes = new generated.metadata.Attributes();
                    passive.setAttributes(attributes);
				}
				
				Attribute attribute = null;
				
				for(Attribute existingAttribute : attributes.getAttribute()){
					
					if(existingAttribute.getValue() != null && candidate.getAttribute().getName().equals(existingAttribute.getValue())){
						attribute = existingAttribute;
					}
				}				
				
				if(hasBeenSelected){
					
					if(attribute == null){
						
						attribute = new Attribute();
						attribute.setValue(candidate.getAttribute().getName());
						
						candidate.setChosen(attributes.getAttribute().add(attribute));
						
					} else {
						candidate.setChosen(true);
					}
					
				} else {
					
					if(attribute != null){						
						candidate.setChosen(!attributes.getAttribute().remove(attribute));
					
					} else {
						candidate.setChosen(false);
					}
				}
				
				validateCurrentRemediationContent();
			}
		});
        
        view.setApplicationAttributeSelectionColumnFieldUpdater(new FieldUpdater<CandidateMetadataAttribute, Boolean>() {

			@Override
			public void update(int index, CandidateMetadataAttribute candidate, Boolean hasBeenSelected) {
				
			    if(candidate.getParentConcept().getActivityType() == null){
                    candidate.getParentConcept().setActivityType(new ActivityType());
                }
                
                Serializable parentConceptActivity = candidate.getParentConcept().getActivityType().getType();
                generated.metadata.Attributes attributes = null;
                if(parentConceptActivity instanceof generated.metadata.ActivityType.Passive){                    
                    
                    if(((generated.metadata.ActivityType.Passive)parentConceptActivity).getAttributes() == null){
                        
                        attributes = new generated.metadata.Attributes(); 
                        ((generated.metadata.ActivityType.Passive)parentConceptActivity).setAttributes(attributes);
                        
                    }else{
                        attributes = ((generated.metadata.ActivityType.Passive)parentConceptActivity).getAttributes();
                    }
                    
                }else{
                    
                    //either null, constructive or active - set to passive
                    candidate.getParentConcept().setActivityType(new generated.metadata.ActivityType());
                    generated.metadata.ActivityType.Passive passive = new generated.metadata.ActivityType.Passive();
                    candidate.getParentConcept().getActivityType().setType(passive);
                    attributes = new generated.metadata.Attributes();
                    passive.setAttributes(attributes);
                }
                
                Attribute attribute = null;
                
                for(Attribute existingAttribute : attributes.getAttribute()){
                    
                    if(existingAttribute.getValue() != null && candidate.getAttribute().getName().equals(existingAttribute.getValue())){
                        attribute = existingAttribute;
                    }
                }               
                
                if(hasBeenSelected){
                    
                    if(attribute == null){
                        
                        attribute = new Attribute();
                        attribute.setValue(candidate.getAttribute().getName());
                        
                        candidate.setChosen(attributes.getAttribute().add(attribute));
                        
                    } else {
                        candidate.setChosen(true);
                    }
                    
                } else {
                    
                    if(attribute != null){                      
                        candidate.setChosen(!attributes.getAttribute().remove(attribute));
                    
                    } else {
                        candidate.setChosen(false);
                    }
                }
				
				validateCurrentPracticeApplication();
			}
		});
        
        contentAttributesTableDataProvider.addDataDisplay(view.getContentAttributesTable());
        contentAttributesTableDataProvider.addDataDisplay(view.getRemediationAttributesTable());
        contentAttributesTableDataProvider.addDataDisplay(view.getApplicationAttributesTable());
        
        //edit training app guidance button
        handlerRegistrations.add(view.getPracticeGuidanceCreator().addValueChangeHandler(new ValueChangeHandler<Guidance>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<Guidance> event) {
				
			    if (view.getPracticeInteropEditor() != null && view.getPracticeInteropEditor().getCurrentTrainingApp() != null) {
				
					if(event.getValue() != null){
						
						event.getValue().setTransitionName(currentMbp.getTransitionName() + " - Practice Guidance");
						
						view.getPracticeInteropEditor().getCurrentTrainingApp().setGuidance(event.getValue());
						
					} else {
						
					    view.getPracticeInteropEditor().getCurrentTrainingApp().setGuidance(null);
					}
				}
			}
		}));
        
        handlerRegistrations.add(view.getContentAddButton().addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {

				boolean errorOccurred = false;
				boolean enabled = view.getContentAddButtonEnabled().isEnabled();
				
				if(currentMetadata != null && enabled){
					
                    Serializable content = currentMetadata.getContent();
                    if(content instanceof generated.metadata.Metadata.Simple &&
                            ((generated.metadata.Metadata.Simple)content).getValue() != null && 
                            !((generated.metadata.Metadata.Simple)content).getValue().isEmpty()){
						
						String currentContentFileName = ((generated.metadata.Metadata.Simple)content).getValue();
						
						//using the file name (including file extension) when naming the metadata XML file in order
						//to support uploading two content files named the same but with different extensions (e.g. loyalty.pps, loyalty.html)
						int beginIndex = currentContentFileName.lastIndexOf("/") + 1;
						String contentFileName = currentContentFileName.substring(beginIndex);
						String metadataFileName = currentCoursePath + "/" + contentFileName + AbstractSchemaHandler.METADATA_FILE_EXTENSION;
						
						Notify.notify("<html>Content for <b>'" + currentContentFileName + "'</b> has been successfully uploaded to the server.");
						
						generateMetadataFile(metadataFileName, currentContentFileName);
						
                    } else if(content instanceof generated.metadata.Metadata.URL &&
                            ((generated.metadata.Metadata.URL)content).getValue() != null && 
                            !((generated.metadata.Metadata.URL)content).getValue().isEmpty()){
                        
                        String currentUrl = ((generated.metadata.Metadata.URL)content).getValue();
						
						//escape some characters to make a valid Windows file name
						//Notes:
						// 1) remove slashes to make a valid Nuxeo file name 
						//    (otherwise Nuxeo returns 400 response code for getDocumentEntityByName)
						// 2) remove equals because gat.client.util.PlaceParamParser uses equals sign as delimeter and that logic provides
						//    a null metadata file relative path value which makes its way through the 
						//    MetadataPlace->MetadataActivity->LockMetadat->FileTreeModel logic resulting in an illegal arg exception.
						// 3) only use the leading 30 characters of the URL to prevent long filenames in the file system
						//    
						String escapedUrl = currentUrl;

						escapedUrl = escapedUrl.replace("/", "");
						escapedUrl = escapedUrl.replace("\\", "");
						escapedUrl = escapedUrl.replace("\"", "");
						escapedUrl = escapedUrl.replace(":", ""); //%3A
						escapedUrl = escapedUrl.replace("*", ""); //%2A
						escapedUrl = escapedUrl.replace("<", ""); //%3C
						escapedUrl = escapedUrl.replace(">", ""); //%3E
						escapedUrl = escapedUrl.replace("|", ""); //%7C
						escapedUrl = escapedUrl.replace("?", ""); //%3F
						escapedUrl = escapedUrl.replace("=", "");
						
                        if(escapedUrl.length() > 30){
                            escapedUrl = escapedUrl.substring(0, 29) + "...";
                        }
						
						Date date = new Date();
						DateTimeFormat format = DateTimeFormat.getFormat("ddMMyyhhmmss");						
						String metadataFileName = currentCoursePath + "/" + escapedUrl + "_"+format.format(date) + AbstractSchemaHandler.METADATA_FILE_EXTENSION;
						
						Notify.notify("<html>Web address content for <b>'" + currentUrl + "'</b> has been "
								+ "successfully uploaded to the server.");
						
						generateMetadataFile(metadataFileName, currentUrl);
						
					} else if(view.getContentReferenceEditor().getLessonMaterial() != null){
						
						//define a unique name for the lesson material file being generated
						//(Nick: This is based off the same naming convention used in
						//GatClientUtility.createModalDialogUrlWithParams(String, String, String, HashMap<String, String>)).
						Date date = new Date();
						DateTimeFormat format = DateTimeFormat.getFormat("ddMMyyhhmmss");
						String currentContentFileName = "MediaContent_" + format.format(date);
						
						//using the file name (including file extension) when naming the metadata XML file in order
						//to support uploading two content files named the same but with different extensions (e.g. loyalty.pps, loyalty.html)
						int beginIndex = currentContentFileName.lastIndexOf("/") + 1;
						String contentFileName = currentContentFileName.substring(beginIndex);
						String base = currentCoursePath + "/" + contentFileName;
						
						String lessonMaterialFilePath = base + AbstractSchemaHandler.LESSON_MATERIAL_FILE_EXTENSION;
						String metadataFilePath = base + AbstractSchemaHandler.METADATA_FILE_EXTENSION;
						
                        if(content == null || !(content instanceof generated.metadata.Metadata.LessonMaterial)){
                            content = new generated.metadata.Metadata.LessonMaterial();
                            currentMetadata.setContent(content);
                        }
                        
                        ((generated.metadata.Metadata.LessonMaterial)currentMetadata.getContent()).setValue(contentFileName + AbstractSchemaHandler.LESSON_MATERIAL_FILE_EXTENSION);
						
						generateLessonMaterialReferenceFile(lessonMaterialFilePath, view.getContentReferenceEditor().getLessonMaterial());
						generateMetadataFile(metadataFilePath, currentContentFileName);
						
						eventBus.fireEvent(new EditorDirtyEvent());
						
					} else {
						errorOccurred = true;
					}
					
				} else {
					errorOccurred = true;
				}
				
				if(errorOccurred && enabled){
					WarningDialog.warning("Failed to add content", "Could not generate metadata file. An error occurred "
							+ "while getting the  file name needed to generate the files.");
				}
			}

		}));
        
        handlerRegistrations.add(view.getRemediationAddButton().addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {

				boolean errorOccurred = false;
				boolean enabled = view.getRemediationAddButtonEnabled().isEnabled();
				
				if(currentMetadata != null && enabled){
					
				    Serializable content = currentMetadata.getContent();
					if(content instanceof generated.metadata.Metadata.Simple &&
					        ((generated.metadata.Metadata.Simple)content).getValue() != null && 
					        !((generated.metadata.Metadata.Simple)content).getValue().isEmpty()){
						
						String currentContentFileName = ((generated.metadata.Metadata.Simple)content).getValue();
						
						//using the file name (including file extension) when naming the metadata XML file in order
						//to support uploading two content files named the same but with different extensions (e.g. loyalty.pps, loyalty.html)
						int beginIndex = currentContentFileName.lastIndexOf("/") + 1;
						String contentFileName = currentContentFileName.substring(beginIndex);
						String metadataFileName = currentCoursePath + "/" + contentFileName + AbstractSchemaHandler.METADATA_FILE_EXTENSION;
						
						Notify.notify("<html>Content for <b>'" + currentContentFileName + "'</b> has been successfully uploaded to the server.");
						
						generateMetadataFile(metadataFileName, currentContentFileName);
						
					} else if(content instanceof generated.metadata.Metadata.URL &&
                            ((generated.metadata.Metadata.URL)content).getValue() != null && 
                            !((generated.metadata.Metadata.URL)content).getValue().isEmpty()){
						
						String currentUrl = ((generated.metadata.Metadata.URL)content).getValue();
						
						//escape some characters to make a valid Windows file name
						//Notes:
						// 1) remove slashes to make a valid Nuxeo file name 
						//    (otherwise Nuxeo returns 400 response code for getDocumentEntityByName)
						// 2) remove equals because gat.client.util.PlaceParamParser uses equals sign as delimeter and that logic provides
						//    a null metadata file relative path value which makes its way through the 
						//    MetadataPlace->MetadataActivity->LockMetadat->FileTreeModel logic resulting in an illegal arg exception.
						// 3) only use the leading 30 characters of the URL to prevent long filenames in the file system
						//    
						String escapedUrl = currentUrl;

						escapedUrl = escapedUrl.replace("/", "");
						escapedUrl = escapedUrl.replace("\\", "");
						escapedUrl = escapedUrl.replace("\"", "");
						escapedUrl = escapedUrl.replace(":", ""); //%3A
						escapedUrl = escapedUrl.replace("*", ""); //%2A
						escapedUrl = escapedUrl.replace("<", ""); //%3C
						escapedUrl = escapedUrl.replace(">", ""); //%3E
						escapedUrl = escapedUrl.replace("|", ""); //%7C
						escapedUrl = escapedUrl.replace("?", ""); //%3F
						escapedUrl = escapedUrl.replace("=", "");
						
                        if(escapedUrl.length() > 30){
                            escapedUrl = escapedUrl.substring(0, 29) + "...";
                        }
						
						Date date = new Date();
						DateTimeFormat format = DateTimeFormat.getFormat("ddMMyyhhmmss");						
						String metadataFileName = currentCoursePath + "/" + escapedUrl + "_"+format.format(date) + AbstractSchemaHandler.METADATA_FILE_EXTENSION;
						
						Notify.notify("<html>Web address content for <b>'" + currentUrl + "'</b> has been "
								+ "successfully uploaded to the server.");
						
						generateMetadataFile(metadataFileName, currentUrl);
						
					} else if(view.getRemediationContentReferenceEditor().getLessonMaterial() != null){
						
						//define a unique name for the lesson material file being generated
						//(Nick: This is based off the same naming convention used in
						//GatClientUtility.createModalDialogUrlWithParams(String, String, String, HashMap<String, String>)).
						Date date = new Date();
						DateTimeFormat format = DateTimeFormat.getFormat("ddMMyyhhmmss");
						String currentContentFileName = "MediaContent_" + format.format(date);
						
						//using the file name (including file extension) when naming the metadata XML file in order
						//to support uploading two content files named the same but with different extensions (e.g. loyalty.pps, loyalty.html)
						int beginIndex = currentContentFileName.lastIndexOf("/") + 1;
						String contentFileName = currentContentFileName.substring(beginIndex);
						String base = currentCoursePath + "/" + contentFileName;
						
						String lessonMaterialFilePath = base + AbstractSchemaHandler.LESSON_MATERIAL_FILE_EXTENSION;
						String metadataFilePath = base + AbstractSchemaHandler.METADATA_FILE_EXTENSION;
						
						if(content == null || !(content instanceof generated.metadata.Metadata.LessonMaterial)){
						    content = new generated.metadata.Metadata.LessonMaterial();
						    currentMetadata.setContent(content);
						}
						
						((generated.metadata.Metadata.LessonMaterial)currentMetadata.getContent()).setValue(contentFileName + AbstractSchemaHandler.LESSON_MATERIAL_FILE_EXTENSION);
						
						generateLessonMaterialReferenceFile(lessonMaterialFilePath, view.getRemediationContentReferenceEditor().getLessonMaterial());
						generateMetadataFile(metadataFilePath, currentContentFileName);
						
						eventBus.fireEvent(new EditorDirtyEvent());	
						
					} else if(view.getRemediationContentReferenceEditor().getConversationTree() != null){
					    
					    // define a unique name for the metadata file being generated
					    Date date = new Date();
                        DateTimeFormat format = DateTimeFormat.getFormat("ddMMyyhhmmss");
					    String currentContentFileName = "ConversationTree_" + format.format(date);
					    
					    int beginIndex = currentContentFileName.lastIndexOf("/") + 1;
                        String contentFileName = currentContentFileName.substring(beginIndex);
					    String base = currentCoursePath + "/" + contentFileName;
					    String metadataFilePath = base + AbstractSchemaHandler.METADATA_FILE_EXTENSION;
					    
					    if(content == null || !(content instanceof generated.metadata.Metadata.Simple)){
					        content = new generated.metadata.Metadata.Simple();
					        currentMetadata.setContent(content);
					    }
					    
					    ((generated.metadata.Metadata.Simple)currentMetadata.getContent()).setValue(view.getRemediationContentReferenceEditor().getConversationTree().getName());
					    
					    generateMetadataFile(metadataFilePath, view.getRemediationContentReferenceEditor().getConversationTree().getName());
					    
					    eventBus.fireEvent(new EditorDirtyEvent());   
					    
                    } else if(view.getRemediationContentReferenceEditor().getQuestionExport() != null){
                        
                        //define a unique name for the lesson material file being generated
                        //(Nick: This is based off the same naming convention used in
                        //GatClientUtility.createModalDialogUrlWithParams(String, String, String, HashMap<String, String>)).
                        Date date = new Date();
                        DateTimeFormat format = DateTimeFormat.getFormat("ddMMyyhhmmss");
                        String currentContentFileName = "QuestionContent_" + format.format(date);
                        
                        //using the file name (including file extension) when naming the metadata XML file in order
                        //to support uploading two content files named the same but with different extensions (e.g. loyalty.pps, loyalty.html)
                        int beginIndex = currentContentFileName.lastIndexOf("/") + 1;
                        String contentFileName = currentContentFileName.substring(beginIndex);
                        String base = currentCoursePath + "/" + contentFileName;
                        
                        String exportFilePath = base + FileUtil.QUESTION_EXPORT_SUFFIX;
                        String metadataFilePath = base + AbstractSchemaHandler.METADATA_FILE_EXTENSION;
                        
                        if(content == null || !(content instanceof generated.metadata.Metadata.Simple)){
                            content = new generated.metadata.Metadata.Simple();
                            currentMetadata.setContent(content);
                        }
                        
                        ((generated.metadata.Metadata.Simple)currentMetadata.getContent()).setValue(contentFileName + FileUtil.QUESTION_EXPORT_SUFFIX);
                        
                        generateQuestionExportReferenceFile(exportFilePath, view.getRemediationContentReferenceEditor().getQuestionExport());
                        generateMetadataFile(metadataFilePath, currentContentFileName);
                        
                        eventBus.fireEvent(new EditorDirtyEvent());
					
                    } else if (view.getRemediationContentReferenceEditor().getTrainingApp() != null) {
                        // adding a new remediation training application. This is entered when the remediation editor is loading a training application remediation application.

                        TrainingApplication trainingApp = view.getRemediationContentReferenceEditor().getTrainingApp() ;
                        
                        if (trainingApp.getDkfRef() != null
                                && trainingApp.getDkfRef().getFile() != null) {
                            final String dkfFilePath = trainingApp.getDkfRef().getFile();
                            int beginIndex = dkfFilePath.lastIndexOf("/") + 1;
                            int lastIndex = dkfFilePath.lastIndexOf(AbstractSchemaHandler.DKF_FILE_EXTENSION);
                            String dkfFileName = dkfFilePath.substring(beginIndex, lastIndex);

                            String base = currentCoursePath + "/" + dkfFileName;
                            String trainingAppFilePath = base + AbstractSchemaHandler.TRAINING_APP_FILE_EXTENSION;
                            final String metadataFilePath = base + AbstractSchemaHandler.METADATA_FILE_EXTENSION;

                            if (trainingApp.getTransitionName() == null
                                    || trainingApp.getTransitionName().isEmpty()) {
                                trainingApp.setTransitionName(base);
                            }

                            if (content == null || !(content instanceof generated.metadata.Metadata.TrainingApp)) {
                                content = new generated.metadata.Metadata.TrainingApp();
                                currentMetadata.setContent(content);
                            }

                            ((generated.metadata.Metadata.TrainingApp) currentMetadata.getContent()).setValue(dkfFileName + AbstractSchemaHandler.TRAINING_APP_FILE_EXTENSION);

                            if (currentMetadata.getDisplayName() == null) {
                                currentMetadata.setDisplayName(trainingApp.getTransitionName());
                            }

                            // create the training app reference file first and then the metadata file because the metadata file content type to show  
                            // in the ui comes from the training app file contents.  If these two files are created asynchronously, a race condition ensues
                            // which causes a file not found on the training app file in GenerateMetadataFileHandler.
                            generateTrainingAppReferenceFile(trainingAppFilePath, trainingApp, new AsyncCallback<GatServiceResult>() {

                                @Override
                                public void onFailure(Throwable caught) {
                                    // no-op - error handled before reaching here
                                }

                                @Override
                                public void onSuccess(GatServiceResult result) {
                                    if(result.isSuccess()){
                                        generateMetadataFile(metadataFilePath, dkfFilePath);
                                    }
                                }
                            });

                            eventBus.fireEvent(new EditorDirtyEvent());
                        } else {
                            WarningDialog.warning("Failed to add content",
                                    "Could not generate metadata and training application reference files. An error occurred "
                                            + "while getting the DKF file name needed to generate the files.");
                        }
                    
                    } else if (view.getRemediationContentReferenceEditor().getInteractiveLessonMaterial() != null) {
                        // adding a new interactive remediation lesson material. This is entered when the remediation editor is loading a interactive lesson material content.

                        String name = null;

                        LessonMaterial lm = view.getRemediationContentReferenceEditor().getInteractiveLessonMaterial();
                        if (lm.getLessonMaterialList() != null && lm.getLessonMaterialList().getMedia() != null) {
                            for (generated.course.Media media : lm.getLessonMaterialList().getMedia()) {
                                
                                if (media.getMediaTypeProperties() instanceof LtiProperties) {
                                    LtiProperties properties = (LtiProperties) media.getMediaTypeProperties();
                                    name = media.getName();
                                    if (properties.getLtiConcepts() == null) {
                                        properties.setLtiConcepts(new LtiConcepts());
                                    }

                                    properties.getLtiConcepts().getConcepts().clear();
                                    if (currentMetadata != null && currentMetadata.getConcepts() != null) {
                                        for (generated.metadata.Concept concept : currentMetadata.getConcepts().getConcept()) {
                                            properties.getLtiConcepts().getConcepts().add(concept.getName());
                                        }
                                    }
                                    
                                    // there should only be 1 LTI properties media
                                    break;
                                }
                            }
                        }

                        if (currentMetadata.getDisplayName() == null) {
                            currentMetadata.setDisplayName(name);
                        }

                        // define a unique name for the lesson material file being generated (Nick: This
                        // is based off the same naming convention used in
                        // GatClientUtility.createModalDialogUrlWithParams(String, String, String,
                        // HashMap<String, String>)).
                        Date date = new Date();
                        DateTimeFormat format = DateTimeFormat.getFormat("ddMMyyhhmmss");
                        String currentContentFileName = "MediaContent_" + format.format(date);

                        // using the file name (including file extension) when naming the metadata XML
                        // file in order to support uploading two content files named the same but with
                        // different extensions (e.g. loyalty.pps, loyalty.html)
                        int beginIndex = currentContentFileName.lastIndexOf("/") + 1;
                        String contentFileName = currentContentFileName.substring(beginIndex);
                        String base = currentCoursePath + "/" + contentFileName;

                        String lessonMaterialFilePath = base + AbstractSchemaHandler.LESSON_MATERIAL_FILE_EXTENSION;
                        String metadataFilePath = base + AbstractSchemaHandler.METADATA_FILE_EXTENSION;

                        if (content == null || !(content instanceof generated.metadata.Metadata.LessonMaterial)) {
                            content = new generated.metadata.Metadata.LessonMaterial();
                            currentMetadata.setContent(content);
                        }

                        ((generated.metadata.Metadata.LessonMaterial) currentMetadata.getContent()).setValue(contentFileName + AbstractSchemaHandler.LESSON_MATERIAL_FILE_EXTENSION);

                        generateLessonMaterialReferenceFile(lessonMaterialFilePath, lm.getLessonMaterialList());
                        generateMetadataFile(metadataFilePath, currentContentFileName);

                        eventBus.fireEvent(new EditorDirtyEvent());
                        
					} else {
						errorOccurred = true;
					}
					
				} else {
					errorOccurred = true;
				}
				
				if(errorOccurred && enabled){
					WarningDialog.warning("Failed to add content", "Could not generate metadata file. An error occurred "
							+ "while getting the file name needed to generate the files.");
				}
			}

		}));
        
        // Executed when the 'add' button is clicked in the 'Add Practice Application' dialog
        
        handlerRegistrations.add(view.getApplicationAddButton().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {

                if (view.getPracticeInteropEditor() == null) {
                    WarningDialog.warning("Failed to add content", "The practice editor doesn't exist.");
                } else if (!view.getApplicationAddButtonEnabled().isEnabled()) {
                    WarningDialog.warning("Failed to add content", "The add button for the practice editor is disabled.");
                } else if (view.getPracticeInteropEditor().getCurrentTrainingApp() != null) {
                    // adding a new practice training application. This is entered when the practice editor is loading a training application practice application.

                    if (view.getPracticeInteropEditor().getCurrentTrainingApp().getDkfRef() != null
                            && view.getPracticeInteropEditor().getCurrentTrainingApp().getDkfRef().getFile() != null) {
                        final String dkfFilePath = view.getPracticeInteropEditor().getCurrentTrainingApp().getDkfRef().getFile();
                        int beginIndex = dkfFilePath.lastIndexOf("/") + 1;
                        int lastIndex = dkfFilePath.lastIndexOf(AbstractSchemaHandler.DKF_FILE_EXTENSION);
                        String dkfFileName = dkfFilePath.substring(beginIndex, lastIndex);

                        String base = currentCoursePath + "/" + dkfFileName;
                        String trainingAppFilePath = base + AbstractSchemaHandler.TRAINING_APP_FILE_EXTENSION;
                        final String metadataFilePath = base + AbstractSchemaHandler.METADATA_FILE_EXTENSION;

                        if (view.getPracticeInteropEditor().getCurrentTrainingApp().getTransitionName() == null
                                || view.getPracticeInteropEditor().getCurrentTrainingApp().getTransitionName().isEmpty()) {
                            view.getPracticeInteropEditor().getCurrentTrainingApp().setTransitionName(base);
                        }

                        Serializable content = currentMetadata.getContent();
                        if (content == null || !(content instanceof generated.metadata.Metadata.TrainingApp)) {
                            content = new generated.metadata.Metadata.TrainingApp();
                            currentMetadata.setContent(content);
                        }

                        ((generated.metadata.Metadata.TrainingApp) currentMetadata.getContent()).setValue(dkfFileName + AbstractSchemaHandler.TRAINING_APP_FILE_EXTENSION);

                        if (currentMetadata.getDisplayName() == null) {
                            currentMetadata.setDisplayName(view.getPracticeInteropEditor().getCurrentTrainingApp().getTransitionName());
                        }

                        // create the training app reference file first and then the metadata file because the metadata file content type to show  
                        // in the ui comes from the training app file contents.  If these two files are created asynchronously, a race condition ensues
                        // which causes a file not found on the training app file in GenerateMetadataFileHandler.
                        generateTrainingAppReferenceFile(trainingAppFilePath, view.getPracticeInteropEditor().getCurrentTrainingApp(), new AsyncCallback<GatServiceResult>() {

                            @Override
                            public void onFailure(Throwable caught) {
                                // no-op - error handled before reaching here
                            }

                            @Override
                            public void onSuccess(GatServiceResult result) {
                                if(result.isSuccess()){
                                    generateMetadataFile(metadataFilePath, dkfFilePath);
                                }
                            }
                        });

                        eventBus.fireEvent(new EditorDirtyEvent());
                    } else {
                        WarningDialog.warning("Failed to add content",
                                "Could not generate metadata and training application reference files. An error occurred "
                                        + "while getting the DKF file name needed to generate the files.");
                    }
                } else if (view.getPracticeInteropEditor().getCurrentLessonMaterial() != null) {
                    // adding a new practice lesson material. This is entered when the practice editor is loading a lesson material practice application.

                    String name = null;

                    LessonMaterial lm = view.getPracticeInteropEditor().getCurrentLessonMaterial();
                    if (lm.getLessonMaterialList() != null && lm.getLessonMaterialList().getMedia() != null) {
                        for (generated.course.Media media : lm.getLessonMaterialList().getMedia()) {
                            
                            if (media.getMediaTypeProperties() instanceof LtiProperties) {
                                LtiProperties properties = (LtiProperties) media.getMediaTypeProperties();
                                name = media.getName();
                                if (properties.getLtiConcepts() == null) {
                                    properties.setLtiConcepts(new LtiConcepts());
                                }

                                properties.getLtiConcepts().getConcepts().clear();
                                if (currentMetadata != null && currentMetadata.getConcepts() != null) {
                                    for (generated.metadata.Concept concept : currentMetadata.getConcepts().getConcept()) {
                                        properties.getLtiConcepts().getConcepts().add(concept.getName());
                                    }
                                }
                                
                                // there should only be 1 LTI properties media
                                break;
                            }
                        }
                    }

                    if (currentMetadata.getDisplayName() == null) {
                        currentMetadata.setDisplayName(name);
                    }

                    // define a unique name for the lesson material file being generated (Nick: This
                    // is based off the same naming convention used in
                    // GatClientUtility.createModalDialogUrlWithParams(String, String, String,
                    // HashMap<String, String>)).
                    Date date = new Date();
                    DateTimeFormat format = DateTimeFormat.getFormat("ddMMyyhhmmss");
                    String currentContentFileName = "MediaContent_" + format.format(date);

                    // using the file name (including file extension) when naming the metadata XML
                    // file in order to support uploading two content files named the same but with
                    // different extensions (e.g. loyalty.pps, loyalty.html)
                    int beginIndex = currentContentFileName.lastIndexOf("/") + 1;
                    String contentFileName = currentContentFileName.substring(beginIndex);
                    String base = currentCoursePath + "/" + contentFileName;

                    String lessonMaterialFilePath = base + AbstractSchemaHandler.LESSON_MATERIAL_FILE_EXTENSION;
                    String metadataFilePath = base + AbstractSchemaHandler.METADATA_FILE_EXTENSION;

                    Serializable content = currentMetadata.getContent();
                    if (content == null || !(content instanceof generated.metadata.Metadata.LessonMaterial)) {
                        content = new generated.metadata.Metadata.LessonMaterial();
                        currentMetadata.setContent(content);
                    }

                    ((generated.metadata.Metadata.LessonMaterial) currentMetadata.getContent()).setValue(contentFileName + AbstractSchemaHandler.LESSON_MATERIAL_FILE_EXTENSION);

                    generateLessonMaterialReferenceFile(lessonMaterialFilePath, lm.getLessonMaterialList());
                    generateMetadataFile(metadataFilePath, currentContentFileName);

                    eventBus.fireEvent(new EditorDirtyEvent());
                } else {
                    WarningDialog.warning("Failed to add content", "Could not generate metadata for training lesson material.");
                }
            }
        }));
		
		// ClickHandler for the Practice Phase checkbox
		
		handlerRegistrations.add(view.getShowPracticePanelCheckBox().addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				
				view.setPracticePanelVisible(event.getValue());
				
				if(event.getValue() ) {				
					
					if(getPractice() == null){
						
						if(currentPractice != null){
							
							if(currentPractice.getPracticeConcepts() == null) {
								currentPractice.setPracticeConcepts(new PracticeConcepts());
							}
							
							currentPractice.getPracticeConcepts().getCourseConcept().clear();
														
						} else {
						
							// Create the practice quadrant
							Practice practice = new Practice();
							practice.setPracticeConcepts(new PracticeConcepts());
														
							currentPractice = practice;
						}
					
						// Add concepts that were selected in the practice concepts table to the practice quadrant
						for(CandidateConcept concept : practiceConceptsTableDataProvider.getList()) {
							if(concept.isChosen()) {
							    logger.info("ADDING "+concept.getConceptName()+" to practice concepts");
								currentPractice.getPracticeConcepts().getCourseConcept().add(concept.getConceptName());
							}
						}
						
						// Add concepts that were auto-selected from the MBP concepts table to the practice quadrant
						List<String> checkOnLearningConcepts = new ArrayList<String>();
						for(CandidateConcept concept : conceptsTableDataProvider.getList()) {
							if(concept.isChosen()){
							    
							    checkOnLearningConcepts.add(concept.getConceptName());
							    
							    if(!currentPractice.getPracticeConcepts().getCourseConcept().contains(concept.getConceptName())) {
							        logger.info("ADDING "+concept.getConceptName()+" to practice concepts because check on learning");
							        currentPractice.getPracticeConcepts().getCourseConcept().add(concept.getConceptName());
							    }							    
							}
						}

						currentMbp.getQuadrants().getContent().add(currentPractice);	
						
						refreshContentFiles(MerrillQuadrantEnum.PRACTICE);
						
						view.updatePracticeConcepts(checkOnLearningConcepts);
					
					}
								
					//Initialize practice allowed attempts spinner
					if(getPractice() != null && getPractice().getAllowedAttempts() != null && getPractice().getAllowedAttempts().intValue() != 0) {
					    view.getShowPracticeAllowedAttemptsCheckBox().setValue(true);
					    view.getPracticeAllowedAttemptsPanel().setVisible(true);
					    view.getPracticeAllowedAttemptsSpinner().setValue(getPractice().getAllowedAttempts().intValue());
					} else {
					    view.getShowPracticeAllowedAttemptsCheckBox().setValue(false);
					    view.getPracticeAllowedAttemptsPanel().setVisible(false);
					}
					
				} else {
					
					currentPractice = getPractice();
					
					currentMbp.getQuadrants().getContent().remove(currentPractice);
				}
				
				eventBus.fireEvent(new EditorDirtyEvent());
			}
		})); 
        
        handlerRegistrations.add(view.getRuleRefreshButton().addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
                logger.info("calling refresh content files for rule refresh button");
				refreshContentFiles(MerrillQuadrantEnum.RULE);
			}
		}));
        
        handlerRegistrations.add(view.getExampleRefreshButton().addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
                logger.info("calling refresh content files for example refresh button");
				refreshContentFiles(MerrillQuadrantEnum.EXAMPLE);
			}
		}));
        
        handlerRegistrations.add(view.getRemediationRefreshButton().addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				logger.info("calling refresh content files for remediation refresh button");
				refreshContentFiles(MerrillQuadrantEnum.REMEDIATION_AFTER_RECALL);
			}
		}));
        
        handlerRegistrations.add(view.getPracticeRefreshButton().addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
                logger.info("calling refresh content files for practice refresh button");
				refreshContentFiles(MerrillQuadrantEnum.PRACTICE);
			}
		}));
        
        view.getPracticeInteropEditor().setPowerPointFileChangeCallback(new PowerPointFileChangedCallback() {
			
			@Override
			public void onFileChanged(final String newFilePath) {
				
				BsLoadingDialogBox.display("Please Wait", "Checking for metadata references.");
				
				if(newFilePath != null){
					
                    if(currentMetadata != null && currentMetadata.getPresentAt().getMerrillQuadrant() != null){
                        
                        GetMetadataFilesForMerrillQuadrant action = new GetMetadataFilesForMerrillQuadrant(
                                GatClientUtility.getUserName(), 
                                courseFolderPath + "/" + newFilePath, 
                                currentMetadata.getPresentAt().getMerrillQuadrant()
                        );
						
						SharedResources.getInstance().getDispatchService().execute(action, new AsyncCallback<StringListResult>(){

							@Override
							public void onFailure(Throwable thrown) {
								
								BsLoadingDialogBox.remove();
								
								new ErrorDetailsDialog(
										"An error occurred while checking for metadata references. You can continue adding this practice application to "
										+ "this Merrill's branch point, but if you want to edit an existing metadata file for this "
										+ "practice application, you will have to find it manually in the file navigator to edit it.", 
										thrown.toString(), 
										null).center();
							}

							@Override
							public void onSuccess(StringListResult result) {
								
								BsLoadingDialogBox.remove();
								
								if(result.isSuccess() && result.getStrings() != null){
									
									if(!result.getStrings().isEmpty()){
									
										new MetadataFileReferenceListDialog(newFilePath, result.getStrings()).center();
									}
									
								} else {
									
									if(result.getErrorMsg() != null){
										
										if(result.getErrorDetails() != null){
											
											new ErrorDetailsDialog(
													result.getErrorMsg(), 
													result.getErrorDetails(), 
													null).center();
										} else {
											
											new ErrorDetailsDialog(
													"An error occurred while checking for metadata references. You can continue adding this practice application to "
															+ "this Merrill's branch point, but if you want to edit an existing metadata file for this "
															+ "practice application, you will have to find it manually in the file navigator to edit it.", 
													result.getErrorMsg(), 
													null).center();
										}
									
									
									} else {
										
										if(result.getErrorDetails() != null){
											
											new ErrorDetailsDialog(
												"An error occurred while checking for metadata references. You can continue adding this practice application to "
														+ "this Merrill's branch point, but if you want to edit an existing metadata file for this "
														+ "practice application, you will have to find it manually in the file navigator to edit it.", 
												result.getErrorDetails(), 
												null).center();
						
										} else {
										
											new ErrorDetailsDialog(
													"An error occurred while checking for metadata references. You can continue adding this practice application to "
															+ "this Merrill's branch point, but if you want to edit an existing metadata file for this "
															+ "practice application, you will have to find it manually in the file navigator to edit it.", 
												"No details available.", 
												null).center();
									}
								}
							}
						}
						
					});
					}
				}
			}
		});
        
        view.getPracticeInteropEditor().setInteropsEditedCallback(new InteropsEditedCallback() {
			
			@Override
			public void onEdit() {
				validateCurrentPracticeApplication();
			}
		});
        
        view.getPracticeApplicationDeleteColumn().setFieldUpdater(new FieldUpdater<PracticeApplicationObject, String>() {
			
			@Override
			public void update(int index, final PracticeApplicationObject object, String value) {
				
			    if (!GatClientUtility.isReadOnly()) {
			        String objectName = "UNKNOWN";
	                
                    if (object != null) {
                        if (object.getTrainingApplication() != null) {

                            TrainingAppCourseObjectWrapper trainingAppObjectWrapper = object.getTrainingApplication();
                            if (trainingAppObjectWrapper.getValidationException() != null) {
                                objectName = trainingAppObjectWrapper.getInvalidObjectIdentifier();
                            } else if (trainingAppObjectWrapper.getTrainingApplicationObj().getTransitionName() != null) {
                                objectName = trainingAppObjectWrapper.getTrainingApplicationObj().getTransitionName();
                            }
                        } else if (object.getLessonMaterial() != null) {

                            LessonMaterialList lmList = object.getLessonMaterial();
                            if (lmList.getMedia() != null && !lmList.getMedia().isEmpty()) {
                                objectName = lmList.getMedia().get(0).getName();
                            }
                        }
                    }
	                
	                OkayCancelDialog.show(
	                        "Delete Training Application Object?", 
	                        "Are you sure you want to delete " + (
	                                objectName != null 
	                                        ? "<b>" + objectName + "</b>" 
	                                        : "this training application object"
	                        ) + "?", 
	                        "Yes, delete this application", 
	                        new OkayCancelCallback() {
	                            
	                            @Override
	                            public void okay() {
	                                
	                                if(object == null){
	                                    return;
	                                }
	                                
	                                if(object.getMetadataFilesMap() != null && object.getMetadataFilesMap().size() == 1){
	                                    
	                                    final String metadataFilePath = object.getMetadataFilesMap().keySet().iterator().next();
	                                    
	                                    DeleteMetadata action = new DeleteMetadata(
	                                            GatClientUtility.getUserName(), 
	                                            GatClientUtility.getBrowserSessionKey(),
	                                            GatClientUtility.getBaseCourseFolderPath(), 
	                                            metadataFilePath, 
	                                            true
	                                    );
	                                    
	                                    BsLoadingDialogBox.display("Deleting Practice Application", "Please wait while GIFT deletes this "
	                                            + "practice application.");
	                                    
	                                    SharedResources.getInstance().getDispatchService().execute(action, new AsyncCallback<GenericGatServiceResult<Void>>() {

	                                        @Override
	                                        public void onFailure(Throwable caught) {
	                                            
	                                            BsLoadingDialogBox.remove();
	                                            
	                                            DetailedException exception = new DetailedException(
	                                                    "GIFT was unable to delete this practice application. An unexpected error occurred "
	                                                    + "during the deletion.", 
	                                                    caught.toString(), 
	                                                    caught
	                                            );
	                                            
	                                            ErrorDetailsDialog dialog = new ErrorDetailsDialog(
	                                                    exception.getReason(), 
	                                                    exception.getDetails(), 
	                                                    exception.getErrorStackTrace()
	                                            );
	                                            
	                                            dialog.center();
	                                        }

	                                        @Override
	                                        public void onSuccess(GenericGatServiceResult<Void> result) {
	                                            
	                                            BsLoadingDialogBox.remove();
	                                            
	                                            if(result.getResponse().getWasSuccessful()){
	                                                
	                                                if(!view.removePracticeApplication(metadataFilePath)){
	                                                    //fail safe... update the practice application list with a server call
                                                        logger.info("calling refresh content files for delete practice application");
                                                        refreshContentFiles(MerrillQuadrantEnum.PRACTICE);
	                                                }	                                                
	                                                
	                                            } else {
	                                                
	                                                ErrorDetailsDialog dialog = new ErrorDetailsDialog(
	                                                        result.getResponse().getException().getReason(), 
	                                                        result.getResponse().getException().getDetails(), 
	                                                        result.getResponse().getException().getErrorStackTrace()
	                                                );
	                                                
	                                                dialog.center();
	                                            }
	                                        }
	                                    });
	                                    
	                                } else {
	                                    
	                                    String name = null;
	                                    
                                        if (object.getTrainingApplication() != null) {
                                            TrainingAppCourseObjectWrapper trainingAppObjectWrapper = object.getTrainingApplication();
                                            if (trainingAppObjectWrapper.getValidationException() != null) {
                                                name = trainingAppObjectWrapper.getInvalidObjectIdentifier();
                                            } else if (trainingAppObjectWrapper.getTrainingApplicationObj().getTransitionName() != null) {
                                                name = trainingAppObjectWrapper.getTrainingApplicationObj().getTransitionName();
                                            }
                                        } else if (object.getLessonMaterial() != null) {
                                            LessonMaterialList lmList = object.getLessonMaterial();
                                            if (lmList.getMedia() != null && !lmList.getMedia().isEmpty()) {
                                                name = lmList.getMedia().get(0).getName();
                                            }
                                        }
	                                    
	                                    MetadataFileDeletionDialog dialog = new MetadataFileDeletionDialog(name, 
	                                            object.getMetadataFilesMap().keySet(), new Command() {
	                                        
	                                        @Override
	                                        public void execute() {
	                                            logger.info("calling refresh content files for metadata file deletion dialog");
	                                            refreshContentFiles(MerrillQuadrantEnum.PRACTICE);
	                                        }
	                                    });
	                                    dialog.center();
	                                }
	                            }
	                            
	                            @Override
	                            public void cancel() {
	                                //Nothing to do
	                            }
	                });
			    }
				
			}
		});
        
        view.setRefreshMetadataCommand(new Command() {
			
			@Override
			public void execute() {
	             logger.info("calling refresh content files for refresh metadata command");
				refreshContentFiles(null);
			}
		});
        
        view.getChangePracticeApplicationButton().addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				
                final TrainingApplication trainingApp;
                if (view.getPracticeInteropEditor().getCurrentTrainingApp() == null) {
                    trainingApp = new TrainingApplication();
                } else {
                    trainingApp = view.getPracticeInteropEditor().getCurrentTrainingApp();
				}
				
                if(trainingApp.getDkfRef() != null && StringUtils.isNotBlank(trainingApp.getDkfRef().getFile())){
                    // show are you sure you want to change training applications and either delete
                    // the dkf or just remove the reference to the dkf 
                    
                    // get the training application type for display purposes
                    String tAppStr = "current";
                    TrainingApplicationEnum tappEnum = TrainingAppUtil.getTrainingAppType(trainingApp);
                    if(tappEnum != null){
                        tAppStr = tappEnum.getDisplayName();
                    }
                    
                    DeleteRemoveCancelDialog.show("Change Practice Application?", "The real time assessment created for this "+tAppStr+" practice can not be applied"
                            + " to another practice application.<br/><br/>"
                            + "If you proceed with ONLY changing the practice application, the real time assessment file will be kept and can be selected from your workspace later.<br/></br>"
                            + "(<b>file name:</b> "+trainingApp.getDkfRef().getFile()+")", new DeleteRemoveCancelCallback() {
                                
                                @Override
                                public void remove() {
                                    
                                    logger.info("Removing reference to real time assessment file: "+trainingApp.getDkfRef().getFile());
                                    view.getPracticeInteropEditor().removeAssessment();
                                    trainingApp.setDkfRef(null);
                                    trainingApp.setInterops(null);
                                    trainingApp.setEmbeddedApps(null);
                                
                                    view.getPracticeInteropEditor().setTrainingApplication(trainingApp);
                                }
                                
                                @Override
                                public void delete() {
                                    
                                    logger.info("Removing real time assessment file from server: "+trainingApp.getDkfRef().getFile());
                                    final String filename = GatClientUtility.getBaseCourseFolderPath()
                                                        + "/"
                                                        + trainingApp.getDkfRef().getFile();
                                    
                                    String username = GatClientUtility.getUserName();
                                    String browserSessionKey = GatClientUtility.getBrowserSessionKey();
                                    List<String> filesToDelete = new ArrayList<String>(1);
                                    filesToDelete.add(filename);
                                    
                                    //Performs the deletion on the server
                                    DeleteWorkspaceFiles action = new DeleteWorkspaceFiles(username, browserSessionKey, filesToDelete, true);
                                    SharedResources.getInstance().getDispatchService().execute(action, new AsyncCallback<GatServiceResult>(){

                                        @Override
                                        public void onFailure(Throwable throwable) {
                                            ErrorDetailsDialog dialog = new ErrorDetailsDialog(
                                                    "Failed to delete the real time assessment file.", 
                                                    throwable.getMessage(), 
                                                    DetailedException.getFullStackTrace(throwable));
                                            dialog.setDialogTitle("Deletion Failed");
                                            dialog.center();
                                        }

                                        @Override
                                        public void onSuccess(GatServiceResult result) {
                                            if(result.isSuccess()) {
                                                remove();
                                            }else if(result.getErrorMsg() != null) {
                                                logger.warning("Unable to delete the file: " + filename + "\nError Message: " + result.getErrorMsg());
                                            }
                                        }
                                        
                                    });
                                    
                                    view.getPracticeInteropEditor().removeAssessment();
                                    trainingApp.setDkfRef(null);                                    
                                    trainingApp.setInterops(null);
                                    trainingApp.setEmbeddedApps(null);
                                
                                    view.getPracticeInteropEditor().setTrainingApplication(trainingApp);
                                }
                                
                                @Override
                                public void cancel() {
                                    //nothing to do                                    
                                }
                            }, "Delete Real Time assessment and change application", "Change Application");
                }else{              
                    trainingApp.setInterops(null);
                    trainingApp.setEmbeddedApps(null);
                
                    view.getPracticeInteropEditor().setTrainingApplication(trainingApp);
                }
			}
		});
        
        view.getPracticeGuidanceCreator().hideMessageEditor(true);
        view.getPracticeInteropEditor().setChoiceSelectionListener(new Command() {
			
			@Override
			public void execute() {
			    
			    logger.info("running choice selection listener");
				
				boolean hasTrainingApp = view.getPracticeInteropEditor().getCurrentTrainingApp() != null;
				boolean hasLessonMaterial = view.getPracticeInteropEditor().getCurrentLessonMaterial() != null;
				
				if(hasTrainingApp && 
						(view.getPracticeInteropEditor().getCurrentTrainingApp().getInterops() != null || view.getPracticeInteropEditor().getCurrentTrainingApp().getEmbeddedApps() != null)){
					
				    
					if(view.getPracticeInteropEditor().getCurrentTrainingApp().getEmbeddedApps() != null){
						
						//hide loading messages for embedded apps, since they don't make sense
						view.getPracticeGuidanceCreator().setVisible(false);
						
					} else {
						view.getPracticeGuidanceCreator().setVisible(true);
					}
					
					view.getChangePracticeApplicationButton().setVisible(true);
				
                } else if (hasLessonMaterial) {

                    // hide loading messages for LTI provider apps, since they don't make sense
                    view.getPracticeGuidanceCreator().setVisible(false);
                    view.getChangePracticeApplicationButton().setVisible(true);
				} else {
					
					if(hasTrainingApp){
						
						//reset the training app guidance
					    view.getPracticeInteropEditor().getCurrentTrainingApp().setGuidance(null);
						view.getPracticeGuidanceCreator().setValue(null);
					}
					
					view.getPracticeGuidanceCreator().setVisible(false);
					view.getChangePracticeApplicationButton().setVisible(false);
				}
				
				validateCurrentPracticeApplication();
				
				logger.info("FINISHED running choice selection listener");
			}
		});
        
        view.getContentReferenceEditor().setOnChangeCommand(new Command() {
			
			@Override
			public void execute() {
				validateCurrentContent();
			}
		});
        
        view.getRemediationContentReferenceEditor().setOnChangeCommand(new Command() {
			
			@Override
			public void execute() {
				
				if(currentMetadata != null && currentMetadata.getConcepts() != null){
					
					//if concepts have been authored, we need to reset their activity data in case the user switched
					//to a content type that uses a different activity type
					
					for(generated.metadata.Concept concept : currentMetadata.getConcepts().getConcept()){
						updateActivityType(concept);
					}
				}
				
				populateAddFileDialogConcepts();
				
				validateCurrentRemediationContent();
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
                
                currentMbp.setMandatoryOption(mandatoryOption);                
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
                
                if (currentMbp.getMandatoryOption() == null) {
                    currentMbp.setMandatoryOption(new MandatoryOption());
                }
                
                currentMbp.getMandatoryOption().setMandatoryBehavior(behavior);
                updateMandatoryControls(behavior);
            }
        }));
        
        //Learner state shelf life
        handlerRegistrations.add(view.getLearnerStateShelfLife().addValueChangeHandler(new ValueChangeHandler<Integer>() {

            @Override
            public void onValueChange(ValueChangeEvent<Integer> event) {
                Serializable behavior = currentMbp.getMandatoryOption() == null ? null
                        : currentMbp.getMandatoryOption().getMandatoryBehavior();
                
                FixedDecayMandatoryBehavior fixedDecayBehavior;
                if(behavior instanceof FixedDecayMandatoryBehavior) {
                    fixedDecayBehavior = (FixedDecayMandatoryBehavior) behavior;
                } else {
                    fixedDecayBehavior = new FixedDecayMandatoryBehavior();
                    currentMbp.getMandatoryOption().setMandatoryBehavior(fixedDecayBehavior);
                }
                
                int shelfLife = event.getValue() * DAYS_TO_MILLI;
                fixedDecayBehavior.setLearnerStateShelfLife(BigInteger.valueOf(shelfLife));
            }
        }));
    }
    
    /**
     * Updates the activity type of the given concept based on the current visual state of the editor. 
     * If constructive, active, or interactive content is being authored, then this method will update 
     * the concept to use the Constructive, Active, or Interactive activity type, respectively.
     * 
     * @param concept the concept whose activity type should be updated. If null, this method will do nothing.
     */
    private void updateActivityType(generated.metadata.Concept concept) {
        
        if(concept == null) {
            return;
        }
        
        if(concept.getActivityType() == null){
            concept.setActivityType(new generated.metadata.ActivityType());
        }
            
        Serializable type = null;
        
        if(currentMetadata.getPresentAt() != null                                   
                && generated.metadata.BooleanEnum.TRUE.equals(currentMetadata.getPresentAt().getRemediationOnly())){
            
            if(view.getRemediationContentReferenceEditor().getQuestionExport() != null) {
            
                AbstractQuestion question = view.getRemediationContentReferenceEditor().getQuestionExport();
                
                if(question.getProperties().hasProperty(SurveyPropertyKeyEnum.IS_ANSWER_FIELD_TEXT_BOX_KEY)
                        && question.getProperties().getBooleanPropertyValue(SurveyPropertyKeyEnum.IS_ANSWER_FIELD_TEXT_BOX_KEY)){
                    
                    //summarize content should use the Constructive activity type
                    type = new generated.metadata.ActivityType.Constructive();
                    
                } else {
                    
                    //highlight content should use the Active activity type
                    type = new generated.metadata.ActivityType.Active();
                }   
                
            } else if(view.getRemediationContentReferenceEditor().getConversationTree() != null){
                
                // conversation tree should use the Active activity type
                type = new generated.metadata.ActivityType.Active();
                
            } else if(view.getRemediationContentReferenceEditor().getTrainingApp() != null){
                
                //training app content should use the Active activity type
                type = new generated.metadata.ActivityType.Interactive();
                
            } else if(view.getRemediationContentReferenceEditor().getInteractiveLessonMaterial() != null){
                
                //interactive lesson material content should use the Active activity type
                type = new generated.metadata.ActivityType.Interactive();
            }
        }
        
        if(type != null){
            concept.getActivityType().setType(type);
        
        } else {
            concept.getActivityType().setType(new generated.metadata.ActivityType.Passive());
        }
    }
    
    /**
     * Decides what to show on the content attributes table depending on whether the concept
     * is selected and whether the data model already has attributes selected for that concept.
     */
    private void refreshContentAttributesTable(){
        
        contentAttributesTableDataProvider.getList().clear();
        
        if(contentConceptsTableSelectionModel.getSelectedObject() != null){
        
            String conceptName = contentConceptsTableSelectionModel.getSelectedObject().getConceptName();
            logger.info("Selected concept name is "+conceptName+", current metadata concepts: "+currentMetadata.getConcepts().getConcept());
            generated.metadata.Concept concept = null;
            
            for(generated.metadata.Concept existingConcept : currentMetadata.getConcepts().getConcept()){
                
                if(existingConcept.getName() != null && existingConcept.getName().equals(conceptName)){                     
                    concept = existingConcept;
                    break;
                }
            }
            
            // when the concept is null it means that the metadata begin built (currentMetadata) does
            // not have the concept as a selected concept
            if(concept != null){
                
                logger.info("Selected concept "+concept.getName());
                
                updateActivityType(concept);
                
                if(concept.getActivityType().getType() instanceof generated.metadata.ActivityType.Passive &&
                        ((generated.metadata.ActivityType.Passive)concept.getActivityType().getType()).getAttributes() == null){
                    ((generated.metadata.ActivityType.Passive)concept.getActivityType().getType()).setAttributes(new Attributes());                     
                }
                
                List<MetadataAttributeEnum> existingAttributes = new ArrayList<MetadataAttributeEnum>();
                
                if(concept.getActivityType().getType() instanceof generated.metadata.ActivityType.Passive){
                    for(Attribute attribute: ((generated.metadata.ActivityType.Passive)concept.getActivityType().getType()).getAttributes().getAttribute()){
                    
                        try{
                            
                            if(attribute.getValue() != null){
                                existingAttributes.add(MetadataAttributeEnum.valueOf(attribute.getValue()));
                            }
                            
                        }catch(EnumerationNotFoundException e){
                            logger.log(Level.SEVERE, "Caught exception while getting metadata attributes for a concept.", e);
                        }
                    }
                }
                                        
                //populate collection of possible metadata attributes
                for(MetadataAttributeEnum attribute : MetadataAttributeEnum.VALUES()){
                    
                    if(attribute.isContentAttribute() && currentDialogMode == DialogEditingMode.CONTENT){
                    
                        contentAttributesTableDataProvider.getList().add(new CandidateMetadataAttribute(concept, attribute, existingAttributes.contains(attribute)));
                    
                    } else if(attribute.isPracticeAttribute() && currentDialogMode == DialogEditingMode.APPLICATION){
                    
                        contentAttributesTableDataProvider.getList().add(new CandidateMetadataAttribute(concept, attribute, existingAttributes.contains(attribute)));
                    }
                }
            
            }
            
        }
        
        contentAttributesTableDataProvider.refresh();
    }
	
    /**
	 * Requests the list of content files for one or more adaptive courseflow phases (e.g. Rule) from the server
	 * and updates the UI with those list(s).
	 * 
	 * @param specificQuadrant update the content files for a specific phase.  If null, the rule+example+remediation
	 * phase content lists will be requested from the server and updated.
	 */
	private void refreshContentFiles(final MerrillQuadrantEnum specificQuadrant) {
		
		//get set of course concepts not selected for the Adaptive courseflow course object
		List<String> otherCourseConcepts = new ArrayList<>();
		for(CandidateConcept candidateConcept : conceptsTableDataProvider.getList()){
		    
		    if(currentMbp.getConcepts() == null 
		    		|| !currentMbp.getConcepts().getConcept().contains(candidateConcept.getConceptName())){
		    	
		        otherCourseConcepts.add(candidateConcept.getConceptName());
		    }
		}
		
	    //get set of course concepts not selected for the PRACTICE part of the Adaptive courseflow course object
        List<String> otherPracticeCourseConcepts = new ArrayList<>();
        for(CandidateConcept candidateConcept : practiceConceptsTableDataProvider.getList()){
            
        	if(currentMbp.getConcepts() == null 
		    		|| !currentMbp.getConcepts().getConcept().contains(candidateConcept.getConceptName())){
        		
                otherPracticeCourseConcepts.add(candidateConcept.getConceptName());
            }
        }
        
        //when the course object has no course concepts selected, don't query the server for metadata cause it will
        //return an empty list
        if(currentMbp.getConcepts() == null || currentMbp.getConcepts().getConcept().isEmpty()){
            view.setRuleFilePanelLoading(false);
            view.setExampleFilePanelLoading(false);
            view.setRemediationFilePanelLoading(false);
            view.setPracticeApplicationsPanelLoading(false);
            
            view.setRuleFiles(null);        
            view.setExampleFiles(null);    
            view.setRemediationFiles(null);
            view.setPracticeApplications(null);
            return;
        }
        
        logger.info("Building getmerrillquadrantfiles action.");
		
		GetMerrillQuadrantFiles action = 
				new GetMerrillQuadrantFiles(GatClientUtility.getUserName(), courseFolderPath);
		
		if(specificQuadrant == null){
		    //Handles Rule, Example, Remediation phases
    		QuadrantRequest ruleRequest = 
    		        new QuadrantRequest(MerrillQuadrantEnum.RULE, false, currentMbp.getConcepts().getConcept(), otherCourseConcepts);
            QuadrantRequest exampleRequest = 
                    new QuadrantRequest(MerrillQuadrantEnum.EXAMPLE, false, currentMbp.getConcepts().getConcept(), otherCourseConcepts);
            QuadrantRequest remediationRequest = 
                    new QuadrantRequest(MerrillQuadrantEnum.REMEDIATION_AFTER_RECALL, true, currentMbp.getConcepts().getConcept(), otherCourseConcepts);
            Remediation remediation = getRemediation();
            if(remediation != null){
                remediationRequest.setExcludeRuleExampleContent(remediation.getExcludeRuleExampleContent() == generated.course.BooleanEnum.TRUE);
            }
            
            action.addRequest(ruleRequest);
            action.addRequest(exampleRequest);
            action.addRequest(remediationRequest);
            
            view.setRuleFilePanelLoading(true);
            view.setExampleFilePanelLoading(true);
            view.setRemediationFilePanelLoading(true);
            
		}else if(specificQuadrant == MerrillQuadrantEnum.RULE){
		    
		    view.setRuleFilePanelLoading(true);
		    
            QuadrantRequest request = 
                    new QuadrantRequest(specificQuadrant, false, currentMbp.getConcepts().getConcept(), otherCourseConcepts);
          
            action.addRequest(request);
		    
		}else if(specificQuadrant == MerrillQuadrantEnum.EXAMPLE){
		    
		    view.setExampleFilePanelLoading(true);
		    
            QuadrantRequest request = 
                    new QuadrantRequest(specificQuadrant, false, currentMbp.getConcepts().getConcept(), otherCourseConcepts);
          
            action.addRequest(request);
          
		}else if(specificQuadrant == MerrillQuadrantEnum.REMEDIATION_AFTER_RECALL){
	          
            view.setRemediationFilePanelLoading(true);
		    
            QuadrantRequest remediationRequest = 
                    new QuadrantRequest(MerrillQuadrantEnum.REMEDIATION_AFTER_RECALL, true, currentMbp.getConcepts().getConcept(), otherCourseConcepts);
            Remediation remediation = getRemediation();
            if(remediation != null){
                remediationRequest.setExcludeRuleExampleContent(remediation.getExcludeRuleExampleContent() == generated.course.BooleanEnum.TRUE);
            }
            
            action.addRequest(remediationRequest);
		}

		if(!action.getRequests().isEmpty()){
            logger.info("Requesting getmerrillquadrantfiles action.\n"+action);
    		
            try{
        		SharedResources.getInstance().getDispatchService().execute(action, new AsyncCallback<GetMerrillQuadrantFilesResult>() {
        
        			@Override
        			public void onFailure(Throwable thrown) {
        			    
        			    logger.severe("Received failure when trying to retrieve the metadata files : "+thrown.getMessage());
        				
        				List<String> stackTrace = new ArrayList<String>();
        				
        				if(thrown.getStackTrace() != null){
        					for(StackTraceElement e : thrown.getStackTrace()){
        						stackTrace.add(e.toString());
        					}
        				}
                        
                        view.setRuleFilePanelLoading(false);
                        view.setExampleFilePanelLoading(false);
                        view.setRemediationFilePanelLoading(false);
        				
        				ErrorDetailsDialog detailsDialog = new ErrorDetailsDialog(
        						"An error occurred while getting the list of Rule content files.",
        						thrown.toString(), stackTrace);
        				
        				detailsDialog.setDialogTitle("Failed to Get Rule Content Files");
        				detailsDialog.center();
        			}
        
        			@Override
        			public void onSuccess(GetMerrillQuadrantFilesResult result) {
        			    
        			    logger.info("Received response from server with merrill quadrant metadata search results.");
        				
                        view.setRuleFilePanelLoading(false);
                        view.setExampleFilePanelLoading(false);
                        view.setRemediationFilePanelLoading(false);
        				
        				if(result.isSuccess()){	
        				    
        				    if(specificQuadrant == null){
        				        view.setRuleFiles(result.getSearchResult().getResultsForQuadrant(MerrillQuadrantEnum.RULE));        
        				        view.setExampleFiles(result.getSearchResult().getResultsForQuadrant(MerrillQuadrantEnum.EXAMPLE));    
        				        view.setRemediationFiles(result.getSearchResult().getResultsForQuadrant(MerrillQuadrantEnum.REMEDIATION_AFTER_RECALL));
        				    }else if(specificQuadrant == MerrillQuadrantEnum.RULE){
                                view.setRuleFiles(result.getSearchResult().getResultsForQuadrant(MerrillQuadrantEnum.RULE));        
        				    }else if(specificQuadrant == MerrillQuadrantEnum.EXAMPLE){
                                view.setExampleFiles(result.getSearchResult().getResultsForQuadrant(MerrillQuadrantEnum.EXAMPLE));   
        				    }else if(specificQuadrant == MerrillQuadrantEnum.REMEDIATION_AFTER_RECALL){
                                view.setRemediationFiles(result.getSearchResult().getResultsForQuadrant(MerrillQuadrantEnum.REMEDIATION_AFTER_RECALL));   
                            }
        				
        				} else {
        					
        					if(result.getErrorDetails() != null){
        						
        						ErrorDetailsDialog detailsDialog = new ErrorDetailsDialog(
        								result.getErrorMsg(),
        								result.getErrorDetails(), 
        								result.getErrorStackTrace());
        						
        						detailsDialog.setDialogTitle("Failed to Get Rule Content Files");
        						detailsDialog.center();
        						
        					} else {
        						
        						WarningDialog.alert("Failed to get Rule files", result.getErrorMsg());
        					}
        				}
        			}
        		});
            }catch(Throwable t){
                logger.log(Level.SEVERE, "Failed to query the server for metadata.", t);
            }
		}
		
		if((specificQuadrant == null || specificQuadrant == MerrillQuadrantEnum.PRACTICE) && currentPractice != null){
		    
            view.setPracticeApplicationsPanelLoading(true);
		    
		    GetPracticeApplications getApplications = 
	                new GetPracticeApplications(GatClientUtility.getUserName(), courseFolderPath, 
	                        currentPractice.getPracticeConcepts().getCourseConcept(), otherPracticeCourseConcepts);
		     
		    try{
		        SharedResources.getInstance().getDispatchService().execute(getApplications, new AsyncCallback<GetPracticeApplicationsResult>() {

		         @Override
		         public void onFailure(Throwable caught) {
		             
                     List<String> stackTrace = new ArrayList<String>();
                     
                     if(caught.getStackTrace() != null){
                         for(StackTraceElement e : caught.getStackTrace()){
                             stackTrace.add(e.toString());
                         }
                     }
                     
                     view.setPracticeApplicationsPanelLoading(false);
                     
                     ErrorDetailsDialog detailsDialog = new ErrorDetailsDialog(
                             "An error occurred while getting the list of Practice content files.",
                             caught.toString(), stackTrace);
                     
                     detailsDialog.setDialogTitle("Failed to Get Practice Content Files");
                     detailsDialog.center();
		         }

		         @Override
		         public void onSuccess(
		                 GetPracticeApplicationsResult result) {
		             
		             view.setPracticeApplicationsPanelLoading(false);
		                 
		             if(result.isSuccess()){
		                 
		                 List<PracticeApplicationObject> practiceApplications = result.getPracticeApplications();
		                 
		                 if(practiceApplications != null){
		                     view.setPracticeApplications(practiceApplications);		                     
		                 } else {
		                     view.setPracticeApplications(new ArrayList<PracticeApplicationObject>());
		                 }
		                 
		             } else {
		                 
		                   if(result.getErrorDetails() != null){
		                       
		                       ErrorDetailsDialog detailsDialog = new ErrorDetailsDialog(
		                               result.getErrorMsg(),
		                               result.getErrorDetails(), 
		                               result.getErrorStackTrace());
		                       
		                       detailsDialog.setDialogTitle("Failed to Get Practice Content Files");
		                       detailsDialog.center();
		                       
		                   } else {
		                       
		                       WarningDialog.alert("Failed to get Practice files", result.getErrorMsg());
		                   }
		             }
		         }
		     });
            }catch(Throwable t){
                logger.log(Level.SEVERE, "Failed to query the server for Practice metadata.", t);
            }
		}//end if on practice
		
	}
    
    /**
     * Generates a metadata file for the given file name
     * 
	 * @param filename the name of the file to generate a metadata file for
	 * @param contentFileName the name of the content file being added to the metadata (e.g. *.ppsm file)
	 */
	private void generateMetadataFile(final String filename, final String contentFilename) {
		
		if(currentMetadata != null){

			AsyncCallback<GenerateMetadataFileResult> callback = new AsyncCallback<GenerateMetadataFileResult>(){
	
				@Override
				public void onFailure(Throwable e) {
					BsLoadingDialogBox.remove();
					WarningDialog.error("Failed to create metadata", "An error occurred while communicating with the server to generate a metadata file for " + filename);
				}
	
				@Override
				public void onSuccess(GenerateMetadataFileResult result) {
					BsLoadingDialogBox.remove();
					if(result.isSuccess()){
						
						Notify.notify("<html>A metadata file has been created for you based on the attributes you selected.</html>");
						
						if(currentDialogMode.equals(DialogEditingMode.CONTENT)){
							view.hideAddContentDialog();
							view.hideAddRemediationDialog();
						}
						
						if(!GatClientUtility.isReadOnly()){
		                    GatClientUtility.saveCourseAndNotify(); // #4913 - now saving course in order to not loose recently added course objects that lead to this metadata being edited
		                }
						
                        //get the phase where the content was added too (not necessarily this course object depending on the concepts selected)
                        MerrillQuadrantEnum phase = null;
                        if(currentMetadata.getPresentAt().getMerrillQuadrant() != null){                            

                            if(currentMetadata.getPresentAt().getMerrillQuadrant().equals(MerrillQuadrantEnum.RULE.getName())){
                                phase = MerrillQuadrantEnum.RULE;
                                
                            } else if(currentMetadata.getPresentAt().getMerrillQuadrant().equals(MerrillQuadrantEnum.EXAMPLE.getName())){
                                phase = MerrillQuadrantEnum.EXAMPLE;
                            
                            } else if(currentMetadata.getPresentAt().getMerrillQuadrant().equals(MerrillQuadrantEnum.PRACTICE.getName())){
                                phase = MerrillQuadrantEnum.PRACTICE;
                                
                            } 
                            
                        }else if(BooleanEnum.TRUE.equals(currentMetadata.getPresentAt().getRemediationOnly())){
                            phase = MerrillQuadrantEnum.REMEDIATION_AFTER_RECALL;
                        }
                                                        
                        MetadataWrapper metadataWrapper = result.getMetadataWrapper();
                        if(metadataWrapper != null && phase != null && phase != MerrillQuadrantEnum.PRACTICE){
                            
                            logger.info("Added content for the "+phase+" phase on the server.  Updating content table on the client next.");
                            
                            //check if this course object's content list should be updated                                
                            if(currentMbp.getConcepts() == null || currentMbp.getConcepts().getConcept().isEmpty()){
                                //course object has no concepts selected
                                return;
                            }else if(currentMetadata.getConcepts() != null && !currentMetadata.getConcepts().getConcept().isEmpty()){
                                //created metadata has concepts, need to compare against this course objects selected concepts
                                
                                boolean foundConcept = false;
                                for(generated.metadata.Concept metadataConcept : currentMetadata.getConcepts().getConcept()){
                                    
                                    foundConcept = false;
                                    
                                    //the selected concepts for this course object
                                    for(String courseObjectConcept : currentMbp.getConcepts().getConcept()){
                                        
                                        if(metadataConcept.getName() != null && metadataConcept.getName().equalsIgnoreCase(courseObjectConcept)){
                                            //found concept match
                                            foundConcept = true;
                                            break;
                                        }
                                    }
                                    
                                    if(!foundConcept){
                                        break;
                                    }
                                }
                                
                                //only update the client side list if the metadata that was just created is for the concepts 
                                //taught by this course object
                                if(foundConcept){
                                    view.addContentFile(metadataWrapper, phase);
                                }
                            }

                            
                        }else if(phase != null){
                            //for practice and as a fail safe... update the specific phase's content list with a server call
                            logger.info("calling refresh content files for generated metadata file");
                            refreshContentFiles(phase);
                        }
						
					} else {						
						ErrorDetailsDialog dialog = new ErrorDetailsDialog(result.getErrorMsg(), result.getErrorDetails(), result.getErrorStackTrace());
						dialog.setText("Error");
						dialog.center();
					}
				}
				
			};
			
			BsLoadingDialogBox.display("Generating Metadata", "Writing metadata file to the course.");
			
			GenerateMetadataFile action = new GenerateMetadataFile(currentMetadata, filename);
			action.setUserName(GatClientUtility.getUserName());
			
			SharedResources.getInstance().getDispatchService().execute(action, callback);
		
		} else {
			WarningDialog.error("Failed to create metadata", "An error occurred while generating an accompanying metadata file for the specified file.");
		}
	}
	
	/**
	 * Update the this Adaptive courseflow course object backing data model for the practice phase
	 * by either adding or removing the concept based on whether the concept has been chosen.
	 *  
	 * @param candidate the concept to add/remove from the practice phase of this adaptive courseflow data model
	 */
	private void updatePracticeQuadrant(CandidateConcept candidate) {
		
		Practice practice = getPractice();
		
		if(practice == null) {
			// Don't do anything; the user must add a practice application first
			return;
		}
		
		PracticeConcepts concepts = practice.getPracticeConcepts();
		
		if(candidate.isChosen() && !concepts.getCourseConcept().contains(candidate.getConceptName())) {
		    logger.info("ADDING "+candidate.getConceptName()+" to practice in updatePracticeQuadrant");
			concepts.getCourseConcept().add(candidate.getConceptName());
				
		} else if(!candidate.isChosen()) {
		    logger.info("REMOVING "+candidate.getConceptName()+" from practice in updatePracticeQuadrant");
			concepts.getCourseConcept().remove(candidate.getConceptName());
		}
	}
	
    /**
     * Generates a training application reference file for the given file name
     * 
     * @param filename the name of the file to generate a training application reference file for. If null, this method will do nothing.
     * @param app the training application to save to the file. If null, this method will do nothing.
     * @param callback used to notify the caller of the server response, just in a more generic did it succeed or fail. Shouldn't be null, really
     * because there isn't a use case for null at this time.
     */
    private void generateTrainingAppReferenceFile(final String filename, TrainingApplication app, final AsyncCallback<GatServiceResult> callback) {
        
        if(filename == null || app == null) {
            return;
        }
        
        if(currentMetadata != null){

            AsyncCallback<GenerateTrainingAppReferenceFileResult> serverCallback = new AsyncCallback<GenerateTrainingAppReferenceFileResult>(){
    
                @Override
                public void onFailure(Throwable e) {
                    
                    WarningDialog.error("Failed to create metadata", "An error occurred while communicating with the server to generate a training app reference file for " + filename);
                    callback.onFailure(e);
                }
    
                @Override
                public void onSuccess(GenerateTrainingAppReferenceFileResult result) {
                    
                    if(result.isSuccess()){
                        
                        Notify.notify("<html>A reference to your training application has been successfully uploaded to the server.");
                        
                        if(currentDialogMode.equals(DialogEditingMode.APPLICATION)){
                            view.hideAddApplicationDialog();
                        }
                        
                    } else {                        
                        ErrorDetailsDialog dialog = new ErrorDetailsDialog(result.getErrorMsg(), result.getErrorDetails(), result.getErrorStackTrace());
                        dialog.setText("Error");
                        dialog.center();
                    }
                    
                    // create a generic result with the same success flag value
                    GatServiceResult genericResult = new GatServiceResult();
                    genericResult.setSuccess(result.isSuccess());
                    callback.onSuccess(genericResult);
                }
                
            };
            
            String userName = GatClientUtility.getUserName();
            
            GenerateTrainingAppReferenceFile action = new GenerateTrainingAppReferenceFile();
            TrainingApplicationWrapper currentTAWrapper = new TrainingApplicationWrapper();
            currentTAWrapper.setTrainingApplication(app);
            action.setTrainingAppWrapper(currentTAWrapper);
            action.setTargetFilename(filename);
            action.setUserName(userName);
            
            SharedResources.getInstance().getDispatchService().execute(action, serverCallback);
        
        } else {
            WarningDialog.error("Failed to create metadata", "An error occurred while generating an accompanying training app reference file for the specified file.");
        }
    }
	
	/**
     * Generates a lesson material reference file for the given file name
     * 
	 * @param filename the name of the file to generate a lesson material reference file for
	 */
	private void generateLessonMaterialReferenceFile(final String filename, final LessonMaterialList lessonMaterial) {
		
		if(currentMetadata != null){

			AsyncCallback<GenerateLessonMaterialReferenceFileResult> callback = new AsyncCallback<GenerateLessonMaterialReferenceFileResult>(){
	
				@Override
				public void onFailure(Throwable e) {
					
					WarningDialog.error("Failed to create metadata", "An error occurred while communicating with the server to generate a lesson material reference file for " + filename);
				}
	
				@Override
				public void onSuccess(GenerateLessonMaterialReferenceFileResult result) {
					
					if(result.isSuccess()){
						
						//figure out what type of lesson material was generated
						String type = null;
						
						if(lessonMaterial != null
								&& !lessonMaterial.getMedia().isEmpty()){
							
							Serializable properties = lessonMaterial.getMedia().get(0);
							
							if(properties != null){
								
								if(properties instanceof SlideShowProperties){
									type = "slide show";
									
								} else if(properties instanceof PDFProperties){
									type = "PDF";
								
								} else if(properties instanceof ImageProperties){
									type = "image";
									
								} else if(properties instanceof VideoProperties){
                                    type = "video";
								
								} else if(properties instanceof YoutubeVideoProperties){
									type = "YouTube video";
									
								} else if (properties instanceof LtiProperties) {
								    type = "LTI provider";
								}
							}
						}
						
						if(type != null){
							
							Notify.notify("<html>Your " + type + " content has been successfully uploaded to the server.</html>");
							
						} else {
							
							Notify.notify("<html>Your content has been successfully uploaded to the server.</html>");
						}
						
						if(currentDialogMode.equals(DialogEditingMode.APPLICATION)){
							view.hideAddApplicationDialog();
						}
						
					} else {						
						ErrorDetailsDialog dialog = new ErrorDetailsDialog(result.getErrorMsg(), result.getErrorDetails(), result.getErrorStackTrace());
						dialog.setText("Error");
						dialog.center();
					}
				}
				
			};
			
			String userName = GatClientUtility.getUserName();
			
			GenerateLessonMaterialReferenceFile action = new GenerateLessonMaterialReferenceFile();
			action.setLessonMaterialList(lessonMaterial);
			action.setTargetFilename(filename);
			action.setUserName(userName);
			
			SharedResources.getInstance().getDispatchService().execute(action, callback);
		
		} else {
			WarningDialog.error("Failed to create metadata", "An error occurred while generating an accompanying training app reference file for the specified file.");
		}
	}
	
    /**
     * Generates a question export reference file for the given file name
     * 
     * @param filename the name of the file to generate a question export reference file for
     */
    private void generateQuestionExportReferenceFile(final String filename, final AbstractQuestion question) {
        
        if(currentMetadata != null){

            AsyncCallback<GenericGatServiceResult<Void>> callback = new AsyncCallback<GenericGatServiceResult<Void>>(){
    
                @Override
                public void onFailure(Throwable e) {
                    
                    WarningDialog.error("Faile to create file", "An error occurred while communicating with the server to generate a question export reference file for " + filename);
                }
    
                @Override
                public void onSuccess(GenericGatServiceResult<Void> result) {
                    
                    GenericRpcResponse<Void> response = result.getResponse();
                    
                    if(response.getWasSuccessful()){
                        
                        //figure out what type of lesson material was generated
                        String type = null;
                        
                        if(question != null && question.getProperties() != null){
                            
                            if(question.getProperties().hasProperty(SurveyPropertyKeyEnum.IS_ANSWER_FIELD_TEXT_BOX_KEY)){
                                type = "summarize text";
                                
                            } else {
                                type = "highlight text";
                            }
                        }
                        
                        if(type != null){
                            
                            Notify.notify("<html>Your " + type + " content has been successfully uploaded to the server.</html>");
                            
                        } else {
                            
                            Notify.notify("<html>Your content has been successfully uploaded to the server.</html>");
                        }
                        
                        if(currentDialogMode.equals(DialogEditingMode.APPLICATION)){
                            view.hideAddApplicationDialog();
                        }
                        
                    } else {                        
                        ErrorDetailsDialog dialog = new ErrorDetailsDialog(
                                response.getException().getReason(), 
                                response.getException().getDetails(), 
                                response.getException().getErrorStackTrace()
                        );
                        dialog.setText("Error");
                        dialog.center();
                    }
                }
                
            };
            
            String userName = GatClientUtility.getUserName();
            
            GenerateQuestionExportReferenceFile action = new GenerateQuestionExportReferenceFile();
            action.setQuestion(question);
            action.setTargetFilename(filename);
            action.setUserName(userName);
            
            SharedResources.getInstance().getDispatchService().execute(action, callback);
        
        } else {
            WarningDialog.error("Failed to create file", "An error occurred while generating the specified question export file.");
        }
    }
	
    /**
     * A training application has been imported from GIFT Wrap. Check if this class is the intended
     * target and perform the necessary actions if it is.
     * 
     * @param event the event
     */
    @EventHandler
    protected void onTrainingApplicationImportEvent(TrainingApplicationImportEvent event) {
        view.getAddApplicationInteropEditor().handleTrainingApplicationImport(event.getTrainingApplication());
    }

	@EventHandler
	protected void onCourseLoaded(CourseLoadedEvent event){		
		hasTransitionErrorBeenShown = false;
	}
	
	@EventHandler
	protected void onCourseConceptsChanged(CourseConceptsChangedEvent event){		
		
		//need to refresh the view whenever the concepts are changed, since basically everything is affected by the concepts
		populateView();
	}
	
	@EventHandler
    protected void onCourseLtiProvidersChanged(CourseLtiProvidersChangedEvent event){       
        
        //need to refresh the view whenever the course's LTI providers are changed
        populateView();
    }
    
    /**
     * Populates the view based on the current Merrills Branch Point.
     * 
     * !!!! Note: the method makeDataValidForView() will recreate the transitions list so if 
     *       some new element was added make sure it isn't being cleared out in that method !!!!
     */
    private void populateView(){

        view.setCourseSurveyContextId(currentCourse.getSurveyContext());
    	
    	view.getDisabledInput().setValue(null);
    	  	
    	//clear the 'Concepts:' table
    	conceptsTableDataProvider.getList().clear();  	
    	conceptsTableDataProvider.refresh();
    	
    	practiceConceptsTableDataProvider.getList().clear();  	
    	practiceConceptsTableDataProvider.refresh(); 
    	
    	//clear the LTI providers' list
    	contentLtiProvidersDataProvider.getList().clear();
    	contentLtiProvidersDataProvider.refresh();
        
    	if(currentMbp != null){
    		
            view.getDisabledInput().setValue(currentMbp.getDisabled() != null ? currentMbp.getDisabled().equals(generated.course.BooleanEnum.TRUE) : false // not
                                                                                                                                          // checked
                                                                                                                                          // by
                                                                                                                                          // default
                    , true);
            
            // set mandatory value
            MandatoryOption mandatoryOption;
            if (currentMbp.getMandatoryOption() != null) {
                // there is a value set in the incoming data model
                mandatoryOption = currentMbp.getMandatoryOption();
            } else {
                // there is no value set in the incoming data model, provide it with a new instance
                currentMbp.setMandatoryOption(mandatoryOption = new MandatoryOption());
            }
            
            /* If the course author didn't specify a mandatoryBehavior, default to using a 
             * behavior that will always use existing learner state when available in order to 
             * skip the adaptive course flow. */
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

    		currentPractice = getPractice();
    		
    		Transitions ruleTransitions = getTransitions(MerrillQuadrantEnum.RULE);
    		
    		view.getRuleTransitionsWarning().setHTML("");
    		
    		String ruleErrorMessage = null;
    		
    		view.showRuleWarning(false);
    		
    		if(ruleTransitions == null){
    			
    			view.getRuleGuidanceCreator().setValue(null);
    		
    		} else if(ruleTransitions.getTransitionType().size() == 1){
    			
    			if(ruleTransitions.getTransitionType().get(0) instanceof Guidance){
    			
    				view.getRuleGuidanceCreator().setValue((Guidance) ruleTransitions.getTransitionType().get(0));
    				
    			} else {
    				
    				ruleErrorMessage = ""
    						+ "Warning! The authoring tool that you are using doesn't support some of the authored features of "
    						+ "this course. Specifically, it looks like this course has a transition following this phase that is not a "
    						+ "\"Guidance\" message. As a result, if you select the option to show a message after this phase you might "
    						+ "destroy some of the transitions already authored.<br/>"
    						+ "<br/>"
 							+"If you wish to edit this specific transition, please use the legacy Course Authoring Tool (CAT), which can be "
 							+ "launched from the GIFT Control Panel, which is accessed from the GIFT\\scripts\\launchControlPanel.bat "
 							+ "launcher.";
    			}
    			
    		} else {
    			
				ruleErrorMessage = ""
						+ "Warning! The authoring tool that you are using doesn't support some of the authored features of "
						+ "this course. Specifically, it looks like this course has multiple transitions following this phase. "
						+ "As a result, if you select the option to show a message after this phase you might "
						+ "destroy some of the transitions already authored.<br/>"
						+ "<br/>"
						+"If you wish to edit this specific transition, please use the legacy Course Authoring Tool (CAT), which can be "
						+ "launched from the GIFT Control Panel, which is accessed from the GIFT\\scripts\\launchControlPanel.bat "
						+ "launcher.";
    		}
    		
    		if(ruleErrorMessage != null){
    			
    			view.getRuleTransitionsWarning().setHTML(""
    					+ 	"<span style='color: red; font-weight: bold;'>" 
    					+ 		ruleErrorMessage
    					+ 	"</span>"
    			);
    			
    			view.getRuleGuidanceCreator().setValue(null);
    			
    			view.showRuleWarning(true);
    			
    			showTransitionWarningDialog();
    			
    		}
    		
    		
    		Transitions exampleTransitions = getTransitions(MerrillQuadrantEnum.EXAMPLE);
    		
    		view.getExampleTransitionsWarning().setHTML("");
    		
    		String exampleErrorMessage = null;
    		view.showExampleWarning(false);
    		
    		if(exampleTransitions == null){
    			
    			view.getExampleGuidanceCreator().setValue(null);
    		
    		} else if(exampleTransitions.getTransitionType().size() == 1){
    			
    			if(exampleTransitions.getTransitionType().get(0) instanceof Guidance){
    			
    				view.getExampleGuidanceCreator().setValue((Guidance) exampleTransitions.getTransitionType().get(0));
    				
    			} else {
    				
    				exampleErrorMessage = ""
    						+ "Warning! The authoring tool that you are using doesn't support some of the authored features of "
    						+ "this course. Specifically, it looks like this course has a transition following this phase that is not a "
    						+ "\"Guidance\" message. As a result, if you select the option to show a message after this phase you might "
    						+ "destroy some of the transitions already authored.<br/>"
    						+ "<br/>"
 							+"If you wish to edit this specific transition, please use the legacy Course Authoring Tool (CAT), which can be "
 							+ "launched from the GIFT Control Panel, which is accessed from the GIFT\\scripts\\launchControlPanel.bat "
 							+ "launcher.";
    			}
    			
    		} else {
    			
    			exampleErrorMessage = ""
						+ "Warning! The authoring tool that you are using doesn't support some of the authored features of "
						+ "this course. Specifically, it looks like this course has multiple transitions following this phase. "
						+ "As a result, if you select the option to show a message after this phase you might "
						+ "destroy some of the transitions already authored.<br/>"
						+ "<br/>"
						+"If you wish to edit this specific transition, please use the legacy Course Authoring Tool (CAT), which can be "
						+ "launched from the GIFT Control Panel, which is accessed from the GIFT\\scripts\\launchControlPanel.bat "
						+ "launcher.";
    		}
    		
    		if(exampleErrorMessage != null){
    			
    			view.getExampleTransitionsWarning().setHTML(""
    					+ 	"<span style='color: red; font-weight: bold;'>" 
    					+ 		exampleErrorMessage
    					+ 	"</span>"
    			);
    			
    			view.getExampleGuidanceCreator().setValue(null);
    			
    			view.showExampleWarning(true);
    			
    			showTransitionWarningDialog();
    		}
    		
    		Transitions recallTransitions = getTransitions(MerrillQuadrantEnum.RECALL);
    		
    		view.getRecallTransitionsWarning().setHTML("");
    		
    		String recallErrorMessage = null;
    		view.showRecallWarning(false);
    		
    		if(recallTransitions == null){
    			
    			view.getRecallGuidanceCreator().setValue(null);
    		
    		} else if(recallTransitions.getTransitionType().size() == 1){
    			
    			if(recallTransitions.getTransitionType().get(0) instanceof Guidance){
    			
    				view.getRecallGuidanceCreator().setValue((Guidance) recallTransitions.getTransitionType().get(0));
    				
    			} else {
    				
    				recallErrorMessage = ""
    						+ "Warning! The authoring tool that you are using doesn't support some of the authored features of "
    						+ "this course. Specifically, it looks like this course has a transition following this phase that is not a "
    						+ "\"Guidance\" message. As a result, if you select the option to show a message after this phase you might "
    						+ "destroy some of the transitions already authored.<br/>"
    						+ "<br/>"
 							+"If you wish to edit this specific transition, please use the legacy Course Authoring Tool (CAT), which can be "
 							+ "launched from the GIFT Control Panel, which is accessed from the GIFT\\scripts\\launchControlPanel.bat "
 							+ "launcher.";
    			}
    			
    		} else {
    			
    			recallErrorMessage = ""
						+ "Warning! The authoring tool that you are using doesn't support some of the authored features of "
						+ "this course. Specifically, it looks like this course has multiple transitions following this phase. "
						+ "As a result, if you select the option to show a message after this phase you might "
						+ "destroy some of the transitions already authored.<br/>"
						+ "<br/>"
						+"If you wish to edit this specific transition, please use the legacy Course Authoring Tool (CAT), which can be "
						+ "launched from the GIFT Control Panel, which is accessed from the GIFT\\scripts\\launchControlPanel.bat "
						+ "launcher.";
    		}
    		
    		if(recallErrorMessage != null){
    			
    			view.getRecallTransitionsWarning().setHTML(""
    					+ 	"<span style='color: red; font-weight: bold;'>" 
    					+ 		recallErrorMessage
    					+ 	"</span>"
    			);
    			
    			view.getRecallGuidanceCreator().setValue(null);
    			
    			view.showRecallWarning(true);
    			
    			showTransitionWarningDialog();
    		}
    	
    		//populate the 'Concepts:' table
	    	if(currentCourse != null){
	    		
	    	    // the authored course concepts
                List<String> conceptNames = new ArrayList<String>();
                
	    		if(currentCourse.getConcepts() != null && currentCourse.getConcepts().getListOrHierarchy() != null){
	    			
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
	    			
	    			List<String> checkOnLearningConcepts = new ArrayList<>();
	    			List<String> practiceConcepts = new ArrayList<>();
	    			for(String conceptName : conceptNames){
	    				
	    				if(currentMbp.getConcepts().getConcept().contains(conceptName)){
	    					conceptsTableDataProvider.getList().add(new CandidateConcept(conceptName, true));
	    					checkOnLearningConcepts.add(conceptName);
	    				} else {
	    					conceptsTableDataProvider.getList().add(new CandidateConcept(conceptName, false));
	    				}
	    				
	    				if(getPractice() != null && getPractice().getPracticeConcepts().getCourseConcept().contains(conceptName)) {
	    				    logger.info("ADDING "+conceptName+" to practice concepts table data provider because its in the Practice object (CHOSEN)");
	    					practiceConceptsTableDataProvider.getList().add(new CandidateConcept(conceptName, true));
	    					practiceConcepts.add(conceptName);
	    				} else {
                            logger.info("ADDING "+conceptName+" to practice concepts table data provider because its in the Practice object (NOT CHOSEN)");
	    					practiceConceptsTableDataProvider.getList().add(new CandidateConcept(conceptName, false));
	    				}
	    				
	    			}
	    			
	    			// populate the components with the authored concept selections 
	    			view.setCourseConcepts(conceptNames, checkOnLearningConcepts, practiceConcepts);
	    			
	    			checkForExtraneousConcepts(conceptNames);
	    			conceptsTableDataProvider.refresh();
	    			
	    		} else {
	    		    
	    		    // let the view know there are no concepts
                    view.setCourseConcepts(conceptNames, conceptNames, conceptNames);
	    			
	    			WarningDialog.error("Missing course concepts", "Adaptive Courseflow requires course concepts to be defined and the course concepts could not be loaded. "
	    					+ "<br/><br/>Please check the course properties to verify that you have specified concepts "
	    					+ "to cover in this course.");
	    		}    		
	    	}
	    	
	    	// populate the LTI providers' list
            List<LtiProvider> ltiProviderIds = GatClientUtility.getCourseLtiProviders();
            if (ltiProviderIds != null) {
                contentLtiProvidersDataProvider.getList().addAll(ltiProviderIds);
                contentLtiProvidersDataProvider.refresh();
            }
	    	
	    	Remediation remediation = getRemediation();
	    	if(remediation != null){
	    	    view.getExcludeRuleExampleContentCheckBox().setValue(remediation.getExcludeRuleExampleContent() == generated.course.BooleanEnum.TRUE);
	    	}else{
	    	    view.getExcludeRuleExampleContentCheckBox().setValue(false);
	    	}
	    	
	    	Practice practice = getPractice();
	    	
	    	if(practice != null){
	    		view.getShowPracticePanelCheckBox().setValue(true);
	    		view.setPracticePanelVisible(true);
	    		if(practice.getAllowedAttempts() != null) {
					//Determine whether the specified attempt count is infinite or finite
					if (practice.getAllowedAttempts().intValue() == 0) {
						// If unlimited is specified, hide the UI for specifying attempts
						view.getShowPracticeAllowedAttemptsCheckBox().setValue(false);
						view.getPracticeAllowedAttemptsPanel().setVisible(false);
					} else {
						// If specified as a finite value, make the UI match the specified value
						view.getShowPracticeAllowedAttemptsCheckBox().setValue(true);
						view.getPracticeAllowedAttemptsPanel().setVisible(true);
						view.getPracticeAllowedAttemptsSpinner().setValue(practice.getAllowedAttempts().intValue());
					}
	    		} else {
	    		    view.getShowPracticeAllowedAttemptsCheckBox().setValue(false);
	    		    view.getPracticeAllowedAttemptsPanel().setVisible(false);
	    		}
	    	} else {
	    		view.getShowPracticePanelCheckBox().setValue(false);
	    		view.setPracticePanelVisible(false);
	    		view.getShowPracticeAllowedAttemptsCheckBox().setValue(false);
	    	}
	    	
	        view.setCourseFolderPath(courseFolderPath);
	    	
	    	//populate the 'Questions:' and 'Scoring:' tables
	    	populateQuestionsAndScoringTables(); 	
    	}

        view.getSurveyPickerQuestionBank().setCourseData(this.currentCourse);
        view.getSurveyPickerQuestionBank().setSurveyContextId(this.currentCourse.getSurveyContext());
        view.getSurveyPickerQuestionBank().setTransitionName(currentMbp.getTransitionName());
        view.getSurveyPickerQuestionBank().setValue(GwtSurveySystemProperties.KNOWLEDGE_ASSESSMENT_QBANK_GIFT_KEY);
        view.getSurveyPickerQuestionBank().updateDisplay();
    }
    
    /**
	 * Populates the 'Questions:' and 'Scoring:' tables based on what the user has selected in the 'Concepts:' table. 
	 * 
	 * If a new concept has been selected, a new entry in these tables will be created and will be given Easy, Medium, and Hard values of 
	 * 1, 0, and 0 and Expert, Journeyman, and Novice values of 1, 0, 0 by default. 
	 * 
	 * Concepts that are deselected will be removed from the 'Questions:' and 'Scoring:' tables
	 */
	private void populateQuestionsAndScoringTables() {
		
    	Recall recall = getRecall();
    	
    	if(recall == null){
            WarningDialog.error("Missing Recall", "Ooops.  Somehow the Adaptive Courseflow Recall object was not created prior to trying to populate the question and scoring components.");
    	    return;
    	}
    	
    	//Initialize the recall attempts ui
    	if(recall.getAllowedAttempts() != null) {
    	    //Determine whether the specified attempt count is infinite or finite
    	    if(recall.getAllowedAttempts().intValue() == 0) {
    	        //If unlimited is specified, hide the UI for specifying attempts
    	        view.getShowRecallAllowedAttemptsCheckBox().setValue(false);
    	        view.getRecallAllowedAttemptsPanel().setVisible(false);
    	    } else {
    	        //If specified as a finite value, make the UI match the specified value
    	        view.getShowRecallAllowedAttemptsCheckBox().setValue(true);
    	        view.getRecallAllowedAttemptsPanel().setVisible(true);
    	        view.getRecallAllowedAttemptsSpinner().setValue(recall.getAllowedAttempts().intValue());
    	    }
    	} else {
    	    //The attempts are specified so make the UI match what the default runtime behavior is
    	    view.getShowRecallAllowedAttemptsCheckBox().setValue(false);
    	    view.getRecallAllowedAttemptsPanel().setVisible(false);
    	}
    	
    	List<ConceptQuestions> questionSetToDisplay = new ArrayList<ConceptQuestions>();
		
		//go through the concepts in the 'Concepts:' table
		for(CandidateConcept candidate : conceptsTableDataProvider.getList()){
			
			if(candidate.isChosen()){				
				
				ConceptQuestions questionsToAdd = null;	
				
				//if the user has selected a concept, get the existing concept questions for that concept if the MBP has any
				for(ConceptQuestions questions : recall.getPresentSurvey().getSurveyChoice().getConceptQuestions()){
					
					if(questions.getName() != null && questions.getName().equals(candidate.getConceptName())){
						
						questionsToAdd = questions;
						extraneousConcepts.remove(questions.getName());
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
		
		//re-populate the MBP's concept questions, since changes in the 'Concepts:' table may have affected them
		recall.getPresentSurvey().getSurveyChoice().getConceptQuestions().clear();			
		recall.getPresentSurvey().getSurveyChoice().getConceptQuestions().addAll(questionSetToDisplay);
		if(!extraneousConcepts.isEmpty()) {
			// Keep the extraneous concepts available
			recall.getPresentSurvey().getSurveyChoice().getConceptQuestions().addAll(extraneousConcepts.values());
		}
		
		//set what to display in the 'Questions:' and 'Scoring:' table
		questionsTableDataProvider.setList(questionSetToDisplay);
		scoringTableDataProvider.setList(questionSetToDisplay);
		
		//refresh the data providers to update their corresponding displays
		questionsTableDataProvider.refresh();
		scoringTableDataProvider.refresh();
		view.refreshSliderPanel(conceptsTableDataProvider.getList());
	}
	
	/**
	 * Checks for extraneous concept question names and adds them to the view
	 *  
	 * @param conceptNames The list of concept names available in the course.
	 */
	private void checkForExtraneousConcepts(List<String> conceptNames) {

		if(currentMbp != null && currentMbp.getQuadrants() != null) {
			if(extraneousConcepts.isEmpty()) {
				Recall recall = getRecall();
				for(ConceptQuestions conceptQuestion : recall.getPresentSurvey().getSurveyChoice().getConceptQuestions()) {
					if(!conceptNames.contains(conceptQuestion.getName())) {
						appendExtraneousSlider(conceptQuestion);
						extraneousConcepts.put(conceptQuestion.getName(), conceptQuestion);
						currentMbp.getConcepts().getConcept().remove(conceptQuestion.getName());
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
				Recall recall = getRecall();
				recall.getPresentSurvey().getSurveyChoice().getConceptQuestions().remove(conceptQuestion);
			}
			
		});
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
    		
    		// Get the slider with old values
    		KnowledgeAssessmentSlider slider = view.updateSlider(questions.getName(), total, questions.getAssessmentRules());
    		if(slider != null) {
    		    int[] level = slider.calculateAllLevels();
	    		questions.getAssessmentRules().getAboveExpectation().setNumberCorrect(BigInteger.valueOf(level[2]));
				questions.getAssessmentRules().getAtExpectation().setNumberCorrect(BigInteger.valueOf(level[1]));
				questions.getAssessmentRules().getBelowExpectation().setNumberCorrect(BigInteger.ZERO);

				// Update slider again with the new assessment rule values
                view.updateSlider(questions.getName(), total, questions.getAssessmentRules());
    		}
    	}
    }
    
	/**
	 * Populates the 'Concepts:' table in the 'Add Content:' dialog
	 */
	private void populateAddFileDialogConcepts(){
	    
	    logger.info("Populating Add metadata concept dialog with course concepts.");
		
        // clear the 'Concepts:' table in the 'Add Content' dialog
        contentConceptsTableDataProvider.getList().clear();
        contentConceptsTableDataProvider.refresh();

		//populate the 'Concepts:' table
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
    			
    			List<String> existingConceptNames = new ArrayList<String>();
    			
    			if(currentMetadata.getConcepts() == null){
    				currentMetadata.setConcepts(new generated.metadata.Metadata.Concepts());
    			}
    			
    			for(generated.metadata.Concept concept : currentMetadata.getConcepts().getConcept()){
    				
    				if(concept.getName() != null){
    					existingConceptNames.add(concept.getName());
    				}
    			}
    			
    			for(String conceptName : conceptNames){
    				
    				if(existingConceptNames.contains(conceptName)){
    					contentConceptsTableDataProvider.getList().add(new CandidateConcept(conceptName, true));
    					
    				} else {				
    					contentConceptsTableDataProvider.getList().add(new CandidateConcept(conceptName, false));
    				}
    			}
    			
    			contentConceptsTableDataProvider.refresh();
    			
    		} else {
    			
    			WarningDialog.error("Missing course concepts", "The list of concepts covered by this course could not be loaded. "
    					+ "Please check the course summary to verify that you have specified a list or hieracrhy of concepts "
    					+ "to cover in this course.");
    		}
    	}
    	
    	contentAttributesTableDataProvider.getList().clear();
    	contentAttributesTableDataProvider.refresh();
	}
	
	/**
	 * Selects a concept name in the 'Concepts:' table in the 'Add Content:' dialog.
	 * 
	 * @param name the concept name
	 */
	private void selectContentConceptName(String name){
		
		if(name == null){
			throw new IllegalArgumentException("The name of the concept to select cannot be null.");
		}
		
		for(CandidateConcept concept : contentConceptsTableDataProvider.getList()){
			
			if(concept.getConceptName().equals(name)){
				
				contentConceptsTableSelectionModel.setSelected(concept, true);
				break;
			}
		}
	}
	
	/**
	 * Gets the Recall element of the current Merrill's Branch Point. Can return null if the MBP is invalid and makeDataValidForView() 
	 * has not yet been called.
	 * 
	 * @return the Recall element of the current Merrill's Branch Point
	 */
	private Recall getRecall(){
		
		for(Serializable content : currentMbp.getQuadrants().getContent()){
			
			if(content instanceof Recall){
				return (Recall) content;
			}
		}
		
		//this should never happen as long as this method is called after makeDataValidForView() since a Recall is required
		return null;
	}
	
	/**
	 * Gets the Remediation element of the current Merrill's Branch Point.  Can return null if the MBP doesn't have the
	 * optional Remediation element.
	 * 
	 * @return the remediation element.  Can be null.
	 */
	private Remediation getRemediation(){
	    
        for(Serializable content : currentMbp.getQuadrants().getContent()){
            
            if(content instanceof Remediation){
                return (Remediation) content;
            }
        }
        
        return null;
	}
	
	/**
	 * Gets the Practice element of the current Merrill's Branch Point. Can return null if the MBP is invalid and makeDataValidForView() 
	 * has not yet been called.
	 * 
	 * @return the Practice element of the current Merrill's Branch Point
	 */
	private Practice getPractice(){
		
		for(Serializable content : currentMbp.getQuadrants().getContent()){
			
			if(content instanceof Practice){
				return (Practice) content;
			}
		}
		
		//this should never happen as long as this method is called after makeDataValidForView() since a Recall is required
		return null;
	}
	
	/**
	 * Gets the Transitions object that comes after the specified MBP quadrant
	 * 
	 * @param quadrant the quadrant the transition succeeds
	 * @return the Transitions object that comes after the specified MBP quadrant
	 */
	private Transitions getTransitions(MerrillQuadrantEnum quadrant){
		
		Transitions recallTransitions = null;
		
		for(Serializable content : currentMbp.getQuadrants().getContent()){
			
			if(quadrant.equals(MerrillQuadrantEnum.RULE) 
					&& content instanceof Rule){
				
				//get after-rule transitions
				return getTransitionsAfterContent(content);
				
			} else if(quadrant.equals(MerrillQuadrantEnum.EXAMPLE) 
					&& content instanceof Example){
				
				//get after-example transitions
				return getTransitionsAfterContent(content);
				
			} else if(quadrant.equals(MerrillQuadrantEnum.RECALL) 
					&& content instanceof Recall){
				
				//get after-recall transitions
				recallTransitions = getTransitionsAfterContent(content);
			
			} else if(quadrant.equals(MerrillQuadrantEnum.RECALL) 
					&& content instanceof Remediation){
				
				//if a remediation phase is present, get recall transitions after it
				return getTransitionsAfterContent(content);
			}
			
		}
		
		if(recallTransitions != null){
			
			//if a remediation phase is NOT present, get recall transitions after it
			return recallTransitions;
		}
		
		return null;
	}
	
	/**
	 * Gets the transitions immediately succeeding the specified quadrant content, if any such transitions exist
	 * 
	 * @param content the content that the transitions should follow
	 * @return the transitions
	 */
	private Transitions getTransitionsAfterContent(Serializable content){
		
		int index = currentMbp.getQuadrants().getContent().indexOf(content);
		
		if(index + 1 < currentMbp.getQuadrants().getContent().size()){
			
			Serializable next = currentMbp.getQuadrants().getContent().get(index + 1);
			
			if(next instanceof Transitions){
				
				//if the element following the content in the quadrant content list is a set of transitions, return those transitions
				return (Transitions) next;
			}
		}
			
		return null;
	}
	
	/**
	 * Sets the Transitions object that comes after the specified MBP quadrant
	 * 
	 * @param quadrant the quadrant the transition succeeds
	 * @param transitions the Transitions object to put after the specified MBP quadrant
	 */
	private void setTransitions(MerrillQuadrantEnum quadrant, Transitions transitions){
		
		Recall recallToSetTransitionsAfter = null;
		
		for(Serializable content : currentMbp.getQuadrants().getContent()){
			
			if(quadrant.equals(MerrillQuadrantEnum.RULE) 
					&& content instanceof Rule){
				
				//rule quadrant specified
				setTransitionsAfterContent(content, transitions);
					
				break;
				
			} else if(quadrant.equals(MerrillQuadrantEnum.EXAMPLE) 
					&& content instanceof Example){
				
				//example quadrant specified
				setTransitionsAfterContent(content, transitions);
					
				break;
				
			} else if(quadrant.equals(MerrillQuadrantEnum.RECALL) 
					&& content instanceof Recall){
				
				//if recall quadrant is specified, see if there's a remediation phase after it before adding the transitions
				recallToSetTransitionsAfter = (Recall) content;
				
			} else if(quadrant.equals(MerrillQuadrantEnum.RECALL) 
					&& content instanceof Remediation){
				
				//if a recall quadrant is specified and a remediation phase is present, we need to put the transitions after the remediation phase
				setTransitionsAfterContent(content, transitions);
				
				recallToSetTransitionsAfter = null;
					
				break;
			}
		}
		
		if(recallToSetTransitionsAfter != null){
		
			//if a recall quadrant is specified and NO remediation phase is present, we need to put the transitions after the Recall phase
			setTransitionsAfterContent(recallToSetTransitionsAfter, transitions);
		}
		
	}
	
	/**
	 * Sets the transitions immediately succeeding the specified quadrant content
	 * 
	 * @param content the content that the transitions should follow
	 * @param transitions the transitions
	 */
	private void setTransitionsAfterContent(Serializable content,
			Transitions transitions) {
				
		int index = currentMbp.getQuadrants().getContent().indexOf(content);
		
		if(index + 1 < currentMbp.getQuadrants().getContent().size()){
			
			Serializable next = currentMbp.getQuadrants().getContent().get(index + 1);
			
			if(next instanceof Transitions){
				
				currentMbp.getQuadrants().getContent().remove(next);
				
				//after-content transitions exist, so we need to replace them
				if(transitions != null){
					if(!next.equals(transitions)){					
						currentMbp.getQuadrants().getContent().add(index + 1, transitions);
					}
					
				} 
				
			} else if(transitions != null){
				
				//no after-content transitions exist, so just add any transitions after the content
				currentMbp.getQuadrants().getContent().add(index + 1, transitions);
			}
			
		} else {
			
			if(transitions != null){
				
				//the content is the last content element, so just add any transitions after it
				currentMbp.getQuadrants().getContent().add(index, transitions);
			}
		}
	}

	/**
	 * If necessary, adds any elements to the current Merrill's Branch Point that are needed to complete the view and allow editing.
	 */
	private void makeDataValidForView() {
		
		//sets an empty list of concepts
		if(currentMbp.getConcepts() == null){
			currentMbp.setConcepts(new MerrillsBranchPoint.Concepts());
		}
		
		//sets a new Quadrants element
		if(currentMbp.getQuadrants() == null){
			currentMbp.setQuadrants(new Quadrants());
		}		
		
		//sets the rule, example, recall, practice, and transitions of a Quadrant, if they exist
		Rule rule = null;
		Example example = null;
		Recall recall = null;
		Remediation remediation = null;
		Practice practice = null;
		
		//sets the after-rule, after-example, and after-recall transitions of a Quadrant, if they exist
		Transitions ruleTransitions = null;
		Transitions exampleTransitions = null;
		Transitions recallTransitions = null;
		
		for(Serializable contentObject : currentMbp.getQuadrants().getContent()){
			
			if(rule == null && contentObject instanceof Rule){
				
				rule = (Rule) contentObject;
				
				ruleTransitions = getTransitionsAfterContent(contentObject);
				
			} else if(example == null && contentObject instanceof Example){
				
				example = (Example) contentObject;
				
				exampleTransitions = getTransitionsAfterContent(contentObject);
				
			} else if(recall == null && contentObject instanceof Recall){
				
				recall = (Recall) contentObject;
				
				//if remediation phase is NOT present, recall transitions may come after it
				Transitions transitions = getTransitionsAfterContent(contentObject);
			    
			    if(transitions != null){
			    	recallTransitions = transitions;
			    }
			
			} else if(practice == null && contentObject instanceof Practice){
				practice = (Practice) contentObject;
				
			} else if(remediation == null && contentObject instanceof Remediation){
				
			    remediation = (Remediation) contentObject;
			    
			    //if remediation phase is present, recall transitions may come after it
			    Transitions transitions = getTransitionsAfterContent(contentObject);
			    
			    if(transitions != null){
			    	recallTransitions = transitions;
			    }
			}
		}
		
		//sets a new Rule if none was found
		if(rule == null){
			rule = new Rule();
		}
		
		//sets a new Example if none was found
		if(example == null){
			example = new Example();
		}
		
		//sets a new Recall if none was found
		if(recall == null){
			recall = new Recall();
			recall.setAllowedAttempts(BigInteger.valueOf(DEFAULT_RECALL_ALLOWED_ATTEMPTS));
		}	
		
		//sets a new PresentSurvey for the Recall if none was found
		if(recall.getPresentSurvey() == null){
			recall.setPresentSurvey(new PresentSurvey());
		}
		
		//sets a new Concept Survey for thePresentSurvey for the Recall if none was found
		if(recall.getPresentSurvey().getSurveyChoice() == null){
			recall.getPresentSurvey().setSurveyChoice(new ConceptSurvey());
		}
		
		recall.getPresentSurvey().getSurveyChoice().setGIFTSurveyKey(GwtSurveySystemProperties.KNOWLEDGE_ASSESSMENT_QBANK_GIFT_KEY);
		
		//re-add all the elements in the Quadrant in the expected order
		List<Serializable> content = currentMbp.getQuadrants().getContent();
		
		if(logger.isLoggable(Level.INFO)){
		    logger.info("REBUILDING MBP COURSE OBJECT named "+currentMbp.getTransitionName()+".");
		}
		content.clear();
		
		content.add(rule);
		
		//re-add the after-rule transitions if any were found
		if(ruleTransitions != null){
			content.add(ruleTransitions);
		}
		
		content.add(example);
		
		//re-add the after-example transitions if any were found
		if(exampleTransitions != null){
			content.add(exampleTransitions);
		}
		
		content.add(recall);
		
		if(remediation != null){
		    content.add(remediation);
		}
		
		//re-add the after-recall transitions if any were found
		if(recallTransitions != null){
			content.add(recallTransitions);
		}
		
		content.add(practice);
	}
	
	/**
	 * Validates the current content and indicates to the user what fields still need to be filled
	 */
	private void validateCurrentContent(){
		
		StringBuilder errorMsg = new StringBuilder();
		
		String contentValidationErrors = view.getContentReferenceEditor().getValidationErrors();
		
		if(contentValidationErrors != null){
			errorMsg.append(contentValidationErrors);
		}
		
		if(currentMetadata != null){
			
			if(currentMetadata.getConcepts() == null
					|| currentMetadata.getConcepts().getConcept() == null
					|| currentMetadata.getConcepts().getConcept().isEmpty()){
				
				errorMsg.append("")
						.append("<li>")
						.append("At least one metadata concept must be chosen.")
						.append("</li>");
				
			} else {
				
				for(generated.metadata.Concept concept : currentMetadata.getConcepts().getConcept()){
					
                    if(concept.getActivityType() != null &&
                            concept.getActivityType().getType() instanceof generated.metadata.ActivityType.Passive){
                        
                        generated.metadata.Attributes attributes = ((generated.metadata.ActivityType.Passive)concept.getActivityType().getType()).getAttributes();
                        if(attributes == null 
                            || attributes.getAttribute() == null
                            || attributes.getAttribute().isEmpty()){
						
                            errorMsg.append("")
                                    .append("<li>")
                                    .append("At least one metadata attribute must be chosen for concept '").append(concept.getName()).append("'.")
                                    .append("</li>");
                        }
                        
                    }else if(currentMetadata.getPresentAt() != null && currentMetadata.getPresentAt().getMerrillQuadrant() != null){
                        //which phase to present this content has been set but the specific concept activity has not.
                        //currently the rule/example/practice phases are passive only and passive requires metadata attributes.  The remediation
                        //phase doesn't use the presentAt value but remediation only attribute instead.

                        errorMsg.append("")
                            .append("<li>")
                            .append("At least one metadata attribute must be chosen for concept '").append(concept.getName()).append("'.")
                            .append("</li>");
                    }
				}
				
			}
			
		}
			
		if(!errorMsg.toString().isEmpty()){
			
			view.getContentValidationErrorText().setHTML(""
					+ "<div style='width: 100%; color: red; font-weight: bold;'> "
					+ 		"The following problem(s) have been detected in this content:"
					+ 		"<ul>"
					+ 			errorMsg.toString() 
					+		"</ul>"
					+ 		"You must correct these problems before you can add your content."
					+ "</div>"
					+ "<hr style='border-top-style: solid; border-top-width: 1px; border-top-color: #AAAAAA;'/>"
			);
			
			view.getContentAddButtonEnabled().setEnabled(false);
		
		} else {
			
			view.getContentValidationErrorText().setHTML("");
			
			view.getContentAddButtonEnabled().setEnabled(true);
		}
	}
	
	/**
	 * Validates the current remediation content and indicates to the user what fields still need to be filled
	 */
	private void validateCurrentRemediationContent(){
	
		StringBuilder errorMsg = new StringBuilder();
		
		String contentValidationErrors = view.getRemediationContentReferenceEditor().getValidationErrors();
		
		if(contentValidationErrors != null){
			errorMsg.append(contentValidationErrors);
		}
		
		if(currentMetadata != null){
			
			if(currentMetadata.getConcepts() == null
					|| currentMetadata.getConcepts().getConcept() == null
					|| currentMetadata.getConcepts().getConcept().isEmpty()){
				
				errorMsg.append("")
						.append("<li>")
						.append("At least one metadata concept must be chosen.")
						.append("</li>");
				
			} else if(view.getRemediationContentReferenceEditor().getQuestionExport() == null &&
			        view.getRemediationContentReferenceEditor().getConversationTree() == null){
				
				//check the attributes for any content type other than
			    // - Highlight or Summarize Passage
			    // - Conversation tree
				
				for(generated.metadata.Concept concept : currentMetadata.getConcepts().getConcept()){
					
					if(concept.getActivityType().getType() instanceof generated.metadata.ActivityType.Passive){
					    
					    generated.metadata.Attributes attributes = ((generated.metadata.ActivityType.Passive)concept.getActivityType().getType()).getAttributes();
					    if(attributes == null 
							|| attributes.getAttribute() == null
							|| attributes.getAttribute().isEmpty()){
						
    						errorMsg.append("")
    								.append("<li>")
    								.append("At least one metadata attribute must be chosen for concept '").append(concept.getName()).append("'.")
    								.append("</li>");
					    }
						
					}else if(currentMetadata.getPresentAt() != null && currentMetadata.getPresentAt().getMerrillQuadrant() != null){
                        //which phase to present this content has been set but the specific concept activity has not.
                        //currently the rule/example/practice phases are passive only and passive requires metadata attributes.  The remediation
                        //phase doesn't use the presentAt value but remediation only attribute instead.

                        errorMsg.append("")
                            .append("<li>")
                            .append("At least one metadata attribute must be chosen for concept '").append(concept.getName()).append("'.")
                            .append("</li>");
                    }
				}
				
			}
		}
			
		if(!errorMsg.toString().isEmpty()){
			
			view.getRemediationValidationErrorText().setHTML(""
					+ "<div style='width: 100%; color: red; font-weight: bold;'> "
					+ 		"The following problem(s) have been detected in this content:"
					+ 		"<ul>"
					+ 			errorMsg.toString() 
					+		"</ul>"
					+ 		"You must correct these problems before you can add your content."
					+ "</div>"
					+ "<hr style='border-top-style: solid; border-top-width: 1px; border-top-color: #AAAAAA;'/>"
			);
			
			view.getRemediationAddButtonEnabled().setEnabled(false);
		
		} else {
			
			view.getRemediationValidationErrorText().setHTML("");
			
			view.getRemediationAddButtonEnabled().setEnabled(true);
		}
	}
	
	
	/**
	 * Validates the current training application reference and indicates to the user what fields still need to be filled
	 */
	private void validateCurrentPracticeApplication(){
	    
		StringBuilder errorMsg = new StringBuilder();
		
        if (view.getPracticeInteropEditor() != null && (view.getPracticeInteropEditor().getCurrentTrainingApp() != null
                || view.getPracticeInteropEditor().getCurrentLessonMaterial() != null)) {
			
			String taValidationErrors = view.getPracticeInteropEditor().getValidationErrors();
			
			if(taValidationErrors != null){
				errorMsg.append(taValidationErrors);
			}
		}
		
		if(currentMetadata != null){
			
			if(currentMetadata.getConcepts() == null
					|| currentMetadata.getConcepts().getConcept() == null
					|| currentMetadata.getConcepts().getConcept().isEmpty()){
				
				errorMsg.append("")
						.append("<li>")
						.append("At least one metadata concept must be chosen.")
						.append("</li>");
				
			} else {
				
				for(generated.metadata.Concept concept : currentMetadata.getConcepts().getConcept()){
					
                    if(concept.getActivityType() != null && 
                            concept.getActivityType().getType() instanceof generated.metadata.ActivityType.Passive){
                        
                        generated.metadata.Attributes attributes = ((generated.metadata.ActivityType.Passive)concept.getActivityType().getType()).getAttributes();
                        if(attributes == null 
                            || attributes.getAttribute() == null
                            || attributes.getAttribute().isEmpty()){
                        
                            errorMsg.append("")
                                    .append("<li>")
                                    .append("At least one metadata attribute must be chosen for concept '").append(concept.getName()).append("'.")
                                    .append("</li>");
                        }
                    }else if(currentMetadata.getPresentAt() != null && currentMetadata.getPresentAt().getMerrillQuadrant() != null){
                        //which phase to present this content has been set but the specific concept activity has not.
                        //currently the rule/example/practice phases are passive only and passive requires metadata attributes.  The remediation
                        //phase doesn't use the presentAt value but remediation only attribute instead.

                        errorMsg.append("")
                            .append("<li>")
                            .append("At least one metadata attribute must be chosen for concept '").append(concept.getName()).append("'.")
                            .append("</li>");
                    }
				}
				
			}
			
		}
			
		if(!errorMsg.toString().isEmpty()){
			
			view.getApplicationValidationErrorText().setHTML(""
					+ "<div style='width: 100%; color: red; font-weight: bold;'> "
					+ 		"The following problem(s) have been detected in this practice application:"
					+ 		"<ul>"
					+ 			errorMsg.toString() 
					+		"</ul>"
					+ 		"You must correct these problems before you can add your practice application."
					+ "</div>"
					+ "<hr style='border-top-style: solid; border-top-width: 1px; border-top-color: #AAAAAA;'/>"
			);
			
			view.getApplicationAddButtonEnabled().setEnabled(false);
		
		} else {
			
			view.getApplicationValidationErrorText().setHTML("");
			
			view.getApplicationAddButtonEnabled().setEnabled(true);
		}
	}
	
	@EventHandler
	protected void onCourseObjectRenamed(CourseObjectRenamedEvent event){
		
		if(currentMbp != null && currentMbp.equals(event.getCourseObject())){
			view.getSurveyPickerQuestionBank().setTransitionName(currentMbp.getTransitionName());
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
    
    private void showTransitionWarningDialog(){
    	
    	if(!hasTransitionErrorBeenShown){
    	
	    	WarningDialog.warning("Adaptive courseflow error", "A problem was detected in one or more <br/>Adaptive courseflow course objects.<br/>"
	    			+ "<br/>"
	    			+ "Please check this course's Adaptive courseflow course objects and look for any error messages that need addressing.");
	    	
	    	hasTransitionErrorBeenShown = true;
    	}
    }
    
    @Override
    public void start(){
    	super.start();
    	eventRegistration = eventBinder.bindEventHandlers(this, eventBus);
    }
    
    @Override
    public void stop(){
    	super.stop();
    } 
    
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
            // expand the options panel by default so the user sees this option
            view.setMbpOptionsVisible(true);
        }
    }
}
