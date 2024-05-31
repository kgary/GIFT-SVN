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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.shared.event.ModalHideEvent;
import org.gwtbootstrap3.client.shared.event.ModalHideHandler;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.extras.notify.client.constants.NotifyType;
import org.gwtbootstrap3.extras.notify.client.ui.Notify;
import org.gwtbootstrap3.extras.notify.client.ui.NotifySettings;

import com.github.gwtd3.api.D3;
import com.github.gwtd3.api.core.Value;
import com.github.gwtd3.api.functions.DatumFunction;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DragEndEvent;
import com.google.gwt.event.dom.client.DragEndHandler;
import com.google.gwt.event.dom.client.DragStartEvent;
import com.google.gwt.event.dom.client.DragStartHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.ClosingEvent;
import com.google.gwt.user.client.Window.ClosingHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;

import generated.course.AAR;
import generated.course.AuthoredBranch;
import generated.course.AuthoredBranch.Paths.Path;
import generated.course.AutoTutorSession;
import generated.course.BooleanEnum;
import generated.course.ConceptNode;
import generated.course.ConceptQuestions;
import generated.course.Concepts;
import generated.course.Concepts.Hierarchy;
import generated.course.Concepts.List.Concept;
import generated.course.Conversation;
import generated.course.ConversationTreeFile;
import generated.course.Course;
import generated.course.Course.Configurations;
import generated.course.CustomInteropInputs;
import generated.course.DETestbedInteropInputs;
import generated.course.DISInteropInputs;
import generated.course.DkfRef;
import generated.course.EmbeddedApp;
import generated.course.EmbeddedApps;
import generated.course.GenericLoadInteropInputs;
import generated.course.Guidance;
import generated.course.Guidance.Message;
import generated.course.HAVENInteropInputs;
import generated.course.ImageProperties;
import generated.course.Interop;
import generated.course.InteropInputs;
import generated.course.Interops;
import generated.course.LessonMaterial;
import generated.course.LessonMaterialList;
import generated.course.LtiConcepts;
import generated.course.LtiProperties;
import generated.course.LtiProvider;
import generated.course.LtiProviders;
import generated.course.Media;
import generated.course.MerrillsBranchPoint;
import generated.course.MobileApp;
import generated.course.PDFProperties;
import generated.course.PowerPointInteropInputs;
import generated.course.Practice;
import generated.course.PresentSurvey;
import generated.course.PresentSurvey.ConceptSurvey;
import generated.course.Recall;
import generated.course.RIDEInteropInputs;
import generated.course.SimpleExampleTAInteropInputs;
import generated.course.SlideShowProperties;
import generated.course.TC3InteropInputs;
import generated.course.TrainingApplication;
import generated.course.Transitions;
import generated.course.UnityInteropInputs;
import generated.course.VBSInteropInputs;
import generated.course.VREngageInteropInputs;
import generated.course.WebpageProperties;
import generated.course.YoutubeVideoProperties;
import generated.course.VideoProperties;
import mil.arl.gift.common.course.CourseFileAccessDetails.CourseFileUserPermissionsDetails;
import mil.arl.gift.common.course.TrainingAppUtil;
import mil.arl.gift.common.enums.DeploymentModeEnum;
import mil.arl.gift.common.enums.TrainingApplicationEnum;
import mil.arl.gift.common.gwt.client.GenericRpcResponse;
import mil.arl.gift.common.gwt.client.IFrameMessageHandlerChild;
import mil.arl.gift.common.gwt.client.iframe.IFrameMessageType;
import mil.arl.gift.common.gwt.client.iframe.messages.IFrameSimpleMessage;
import mil.arl.gift.common.gwt.client.lti.LtiProviderJSO;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.BsLoadingDialogBox;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ModalDialogBox;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ModalDialogCallback;
import mil.arl.gift.common.gwt.client.widgets.file.CancelCallback;
import mil.arl.gift.common.gwt.shared.DocumentUtil;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.DetailedExceptionSerializedWrapper;
import mil.arl.gift.common.io.FileTreeModel;
import mil.arl.gift.common.io.FileUtil;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.GatClientBundle;
import mil.arl.gift.tools.authoring.server.gat.client.GatRpcService;
import mil.arl.gift.tools.authoring.server.gat.client.GatRpcServiceAsync;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.enums.CourseObjectNames.CourseObjectName;
import mil.arl.gift.tools.authoring.server.gat.client.event.EditorDirtyEvent;
import mil.arl.gift.tools.authoring.server.gat.client.event.FormFieldFocusEvent;
import mil.arl.gift.tools.authoring.server.gat.client.event.course.CourseConceptsChangedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.event.course.CourseFolderChangedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.event.course.CourseLoadedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.event.course.CourseLtiProvidersChangedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.event.course.CourseObjectDisabledEvent;
import mil.arl.gift.tools.authoring.server.gat.client.event.course.CourseObjectDoneEditingEvent;
import mil.arl.gift.tools.authoring.server.gat.client.event.course.CourseObjectOpenedForEditingEvent;
import mil.arl.gift.tools.authoring.server.gat.client.event.course.CourseObjectRedrawEvent;
import mil.arl.gift.tools.authoring.server.gat.client.event.course.CourseObjectRenamedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.event.course.CustomFormFieldFocusEvent;
import mil.arl.gift.tools.authoring.server.gat.client.event.course.ScenarioSavedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.event.course.SurveysChangedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.event.course.TrainingApplicationImportEvent;
import mil.arl.gift.tools.authoring.server.gat.client.event.course.authoredbranch.BranchSelectedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.place.CoursePlace;
import mil.arl.gift.tools.authoring.server.gat.client.presenter.AbstractGatPresenter;
import mil.arl.gift.tools.authoring.server.gat.client.survey.SurveyRpcService;
import mil.arl.gift.tools.authoring.server.gat.client.survey.SurveyRpcServiceAsync;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.util.NotifyUtil;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.CourseConceptUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.CourseHelp;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.CourseView;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.authoredbranch.tree.AuthoredBranchTree;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.authoredbranch.tree.TreeNodeEnum;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.header.HeaderView;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.tree.CourseElementUtil;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.tree.CourseElementUtil.CopyCourseObjectCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.PresentSurveyReference;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.SurveyEditorModal;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.ContextMenu;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.DefaultGatFileSaveAsDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.DefaultGatFileSelectionDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.DeleteRemoveCancelCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.DeleteRemoveCancelDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.ErrorDetailsDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.FileValidationDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.KeyValuePairDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.NewOrExistingFileDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.SetNameDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.WarningDialog;
import mil.arl.gift.tools.authoring.server.gat.shared.ExternalScenarioImportResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.SaveJaxbObjectResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.FetchContentAddress;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.FetchContentAddressResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.FetchCourseHistory;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.FetchCourseHistoryResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.FetchLtiProperties;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.FetchLtiPropertiesResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.ImportTrainingApplicationObject;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.ImportTrainingApplicationObjectResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.LockCourse;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.LockFileResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.SaveCourse;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.UnlockCourse;
import mil.arl.gift.tools.authoring.server.gat.shared.action.learner.UnlockLearnerConfiguration;
import mil.arl.gift.tools.authoring.server.gat.shared.action.pedagogy.UnlockPedagogyConfiguration;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.CopyTemplateFile;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.CopyWorkspaceFilesResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.DeleteWorkspaceFiles;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.FetchJAXBObject;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.FetchJAXBObjectResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.GetOrCreateSurveyContext;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.GetOrCreateSurveyContextResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.GetRemainingDiskSpace;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.GetRemainingDiskSpaceResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.ValidateFile;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.ValidateFileResult;

/**
 * A presenter used to load courses and populate course data in the Course Editor.
 */
public class CoursePresenter extends AbstractGatPresenter implements CourseView.Presenter {
    
    /**
     * The Interface MyEventBinder.
     */
    interface MyEventBinder extends EventBinder<CoursePresenter> {
    }

    //the model
    
    /** The logger. */
    private static Logger logger = Logger.getLogger(CoursePresenter.class.getName());
    
    /** The Constant eventBinder. */
    private static final MyEventBinder eventBinder = GWT.create(MyEventBinder.class);
    
    /**  RPC service that is used to retrieve the surveys from the database */
    private final SurveyRpcServiceAsync surveyService = GWT.create(SurveyRpcService.class);
    
    private final GatRpcServiceAsync rpcService = GWT.create(GatRpcService.class);
    
    /**
     * used to parse and display course history date time values for the GAT history panel
     */
    private static final DateTimeFormat COURSE_HISTORY_DATE_FORMAT = DateTimeFormat.getFormat("MM/dd/yy HH:mm:ss z");
    private static final DateTimeFormat VALIDATION_DATE_FORMAT = DateTimeFormat.getFormat("yyyy:MM:dd HH:mm:ss:SSS Z");
    private static final String UNKNOWN_HISTORY_VALUE = "unknown";

    /** The current course. */
    private Course currentCourse = null;
        
    /** The training application imported from Gift Wrap */
    private TrainingApplication importedTrainingApp = null;
    
    /** The training application scenario file imported from Gift Wrap */
    private String importedScenarioID = null;
        
    /**  The data provider for the concept list. */
    private ListDataProvider<Concepts.List.Concept> conceptListDataProvider = new ListDataProvider<Concepts.List.Concept>();
    
    /**  The data provider for the LTI providers list. */
    private ListDataProvider<LtiProvider> ltiProviderListDataProvider = new ListDataProvider<LtiProvider>();

    /** The course path. */
    private String filePath;
    
    /** The name of the dkf generated when a training application transition is created. */
    private String taGeneratedDkf;
    
    /** The place controller. */
    @Inject
    private PlaceController placeController;    
    
    /** The edit course view. */
    @Inject
    private CourseView editCourseView;
    
    /** The is course dirty. */
    private boolean isCourseDirty = false;
    
    /** amount of time in milliseconds to ignore presenter start requests after a stop request was made */
    private static long JUSTED_STOP_WINDOW_MS = 100;    
    
    /** the time in milliseconds at the last time the presenter was stopped (calling stop method) */
    private static long lastStopTimeMS = 0l;
    
    /**
     * File New dialog.
     */
    private DefaultGatFileSaveAsDialog fileNewDialog = null;
    
    /** Heartbeat timer so the server doesn't unlock the file we're editing. */
    private Timer lockTimer = new Timer() {
        @Override
        public void run() {
            if(filePath != null) {
                lockFile(filePath, false);
            }
        }
    };
    
    /** whether or not this activity should allow editing */
    private boolean readOnly = false;
    
    /** Indicates whether the course has dependencies on data collection data sets.  If this is true,
     * the use should be warned that making changes could invalidate the data collection results. 
     */
    private boolean hasDataCollectionDependencies = false;
    
    /**
     * Indiates if the user has bypassed the data collection warning.  
     */
    private boolean userBypassedDataCollectionWarning = false;
    
    /** Indicates if the course is modifiable by the user (ie user has permissions to the file). */
    private boolean userCanModify = true;
    
    /** Indicates if this instance of the gat is the one who has the lock (acquired the lock). */
    private boolean hasAcquiredCourseLock = false;
    
    
    /** 
     * special condition if a file is locked and another user tries to open the file
     * should only happen in desktop mode when users can see each other's workspaces
     */
    private boolean fileAlreadyLocked = false;
    
    /** Default configuration label */
    private static final String DEFAULT_CONFIG = "default";
    
    /** Custom configuration label */
    private static final String CUSTOM_CONFIG = "custom";
    
    /** 
     * Represents the name attribute in the course.xml file used to determine whether or not
     * the course folder needs to be renamed. Initialized when the course loads and is updated 
     * when the rename operation is successful on the server. 
     */
    private String originalCourseName = null;
       
    private SetNameDialog setTransitionNameDialog =  new SetNameDialog(null, null, "Create");
    
    /**
     * A dialog used to add and edit LTI providers within the course properties. The values that will be set are an identifier and a Key/Value pair.
     */
    private KeyValuePairDialog addLtiProviderDialog = new KeyValuePairDialog("Add LTI Provider", "Identifier:", "Client Key:", "Client Shared Secret:", "Add LTI Provider");
        
    private ModalDialogBox giftWrapDialog = new ModalDialogBox();
    
    private TrainingApplication currentTrainingApp;
    
    
    /**
     * This is set when an author is editing an LTI provider within the course properties. Null otherwise.
     */
    private LtiProvider ltiProviderBeingEdited;
    
    /** String value shown to the user when the LTI data needs to be hidden */
    private final static String PROTECTED_LTI_DATA = "**protected**";
    
    private HandlerRegistration setTransitionNameHandler = null;
    
    /** The course id that is retrieved from the server for the lti settings.  This is only shown if
     * the user is the owner of the course and has write permissions to the course.
     * It is set to null if the user should not be able to view the lti settings for the course.
     */
    private String ltiCourseId = null;
    
    /** Whether or not a refresh operation is currently underway. Used to avoid sending multiple refresh calls while the one is still being processed. */
    private boolean isRefreshing = false;
    
    /** The transition whose DOM element is currently being dragged over the course tree*/
    private Serializable transitionBeingDragged = null;
    
    //context menus
    private ContextMenu transitionNodeContextMenu = new ContextMenu();
    private ContextMenu transitionAuthoredBranchNodeContextMenu = new ContextMenu();
    
    /** A timer used to refresh the disk space label periodically */
    private Timer refreshTimer = new Timer(){
    
        @Override
        public void run() {
            
            if(!isRefreshing){
                updateDiskSpace();
            }   
        }
        
    };

    /** 
     * The window of an editor that is currently listening for when the course's concepts change. This window will
     * need to be used to notify said editor when a change occurs
     */
    private JavaScriptObject editorToNotifyOnConceptsChange; 
    
    /**
     * Instantiates a new course presenter.
     */
    @Inject
    public CoursePresenter() {
        super();   
        logger.info("constructor");
    }
    
    /* (non-Javadoc)
     * @see mil.arl.gift.tools.authoring.gat.client.view.CourseView.Presenter#confirmStop()
     */
    @Override
    public String confirmStop() {        
//        if( isCourseDirty ) {
//            return createUnsavedMessage();
//        } else {
//            return null;
//        }
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
     * Creates the valid course object.
     *
     * @return the course
     */
    private Course createValidCourseObject(String courseName) {

        Course course = new Course();
        course.setName(courseName);
        Serializable firstCourseObject = null;        
                
        if (GatClientUtility.isRtaLessonLevel()){
            
            // if GIFT's lesson level is set to RTA, give new courses a training app with no interops by default so the author
            // can select the training application type
            TrainingApplication app = new TrainingApplication();
            app.setTransitionName("Training Application");
            firstCourseObject = app;
            
          } else {
              
              // otherwise, give new courses an example guidance course object by default
              Message message = new Message();
              message.setContent("<p style=\"font-family: Arial; font-size: 20px; text-align: center;\">Welcome"
                      + " to GIFT!<br><br>This is an example of a \"Guidance\" transition authored using the "
                      + "GIFT Authoring Tool (GAT)<br><br>Transitions can be used to present lesson material, "
                      + "elicit surveys, provide feedback during training applications, and so much more.</span>");
              Guidance guidance = new Guidance();
              guidance.setTransitionName("Example Guidance");
              guidance.setFullScreen(BooleanEnum.TRUE);        
              guidance.setGuidanceChoice(message);
              firstCourseObject = guidance;
          }
        
        Transitions transitions = new Transitions();
        transitions.getTransitionType().add(firstCourseObject);
        
        course.setTransitions(transitions);
        
        return course;
    }
    
    /**
     * Display selected transition.
     */
    private void displaySelectedTransition(Serializable transitionToDisplay) {    
        
        if(transitionToDisplay == null) {    
            logger.warning("call to 'displaySelectedTransition' but there are no transitions to select.");
            return;
        }
        
        //make the course tree reload the course in case a new transition was added
        editCourseView.getCourseTree().loadTree(currentCourse);        
            
        //start editing the selected transition
        editCourseView.getEditorPanel().startEditing(transitionToDisplay);    
        
        //fire the branch selected event to show the branch
        if (transitionToDisplay instanceof AuthoredBranch) {
            SharedResources.getInstance().getEventBus().fireEvent(new BranchSelectedEvent((AuthoredBranch) transitionToDisplay));
        }
    }

    /**
     * Gets the Course JAXB object corresponding to course file given by the specified path.
     * 
     * @param path the path to the course file from which to get the Course
     */
    private void doGetCourseObject(final String path) {
        final String msg = "doGetCourseObject";
        logger.info(msg);
        
        if(path == null) {
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

        AsyncCallback<FetchJAXBObjectResult> callback = new AsyncCallback<FetchJAXBObjectResult>(){

            @Override
            public void onFailure(Throwable t) {
                handleCallbackFailure(logger, msg, t);
                showWaiting(false);
                GatClientUtility.returnToDashboard();
            }

            @Override
            public void onSuccess(FetchJAXBObjectResult result) {
                
                // This is a chained sequence of rpcs.  First the FetchJAXBObject request is made, then
                // the FetchLtiProperties request is made.  Once both of those are completed, then the course
                // loading continues.        
                doGetLtiPropertiesDuringLoad(path, result);
            }        
        };
        
        logger.info("Getting course object for Course at '" + path + "'");
        
        String userName = GatClientUtility.getUserName();
        
        FetchJAXBObject action = new FetchJAXBObject();
        action.setRelativePath(path);
        action.setUserName(userName);
        showWaiting(true);
        
        dispatchService.execute(action, callback);
    }

    /**
     * Handler for when a save has successfully completed and all server calls are completed.
     * 
     * @param saveNotification The notify event that was shown when the save process started.
     * @param showConfirmationDialog Boolean to indicate if a confirmation dialog should be presented to the user after the save.
     * @param validateCourse Boolean to indicate if the course needs to be validated after the save.
     * @param reloadWhenComplete Boolean to indicate if a reload should occur.
     * @param path The file relative path to the course.
     * @param result The cached SaveJaxbObjectResult that occurred during the save process.
     */
    private void handleSaveSuccess(Notify saveNotification, boolean showConfirmationDialog, boolean validateCourse, boolean reloadWhenComplete, final String path, SaveJaxbObjectResult result) {
        showWaiting(false);
        
        if(result.isSuccess()) {
            
            fileNewDialog.hide();
            
            //Let the user know the save result.
            if(showConfirmationDialog) {
                
                Notify.notify("", "File saved successfully!</br>Verify your course can be taken by using the validate course button.", IconType.SAVE, NotifyUtil.generateDefaultSettings());
                
                saveNotification.hide();    
                
            } else if(!validateCourse){
                saveNotification.hide();
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
                
                unlockFile(false);
            }

            //When we ask the server to save a course it generates
            //a new version for that course before saving. The newly
            //generated version is based on the previous version.
            //So if we don't save the newly generated version in the
            //course object then the server will generate the same
            //version every time we save.
            String newVersion = result.getNewVersion();
            currentCourse.setVersion(newVersion);
            
            setFilePath(path);
            
            isCourseDirty = false;
            
            if(validateCourse) {
                validateFile(filePath, GatClientUtility.getUserName(), saveNotification);
            }
            
            //update the course tree to match the server's copy of the course
            editCourseView.getCourseTree().loadTree(currentCourse);
            
            if(reloadWhenComplete){
                loadCourse();
            }
            
            fetchCourseHistory();
            
        } else {
            
            saveNotification.hide();
            
            String label = "Unable to save course because the server returned an error.";
            String title = "Failed to Save Course";
            
            if(filePath == null){                       
                fileNewDialog.hide();
                fileNewDialog.center();
                label = "Unable to create course because the server returned an error.";
                title = "Failed to Create Course";
            }
            
            ErrorDetailsDialog error;
            String details = result.getErrorDetails();
            if(details == null){
                //need to provide a useful details, try placing the error message as the details
                
                details = result.getErrorMsg();
                if(details == null){
                    details = "Unfortunately a useful error message was not provided.";
                }
                error = new ErrorDetailsDialog(
                        label,
                        details, 
                        result.getErrorStackTrace());
            }else{
                //show both the result's error details and details in the dialogs details
                error = new ErrorDetailsDialog(
                        label,
                        result.getErrorMsg() + "\n" + details, 
                        result.getErrorStackTrace());
            }
            
            error.setText(title);
            error.center();
        }
    }
    
    /**
     * Determines if the editor should be in readonly mode and sets the editor appropriately based
     * on the various rules used to determine readonly mode.  The general rules are:
     *   1) Editor defaults to readonly.
     *   2) If the file lock cannot be granted, then it stays readonly.
     *   3) If the user cannot modify the file, then it stays readonly (permissions).
     *   4) If the course has dependencies on data collection the course stays readonly (with an unlock option for the user).
     *   5) If all those checks pass, then the readonly flag is removed for the editor and the user can edit the course.
     */
    private void determineAndSetReadOnly() {
        
        // Default to readonly.
        setReadOnly(true);
                
        // If the file is already locked, then it is still readonly.
        if (!fileAlreadyLocked) {
            
            // If the user can't modify, the course is readonly.
            if (userCanModify) {
                if (hasDataCollectionDependencies && !userBypassedDataCollectionWarning) {
                    // Still readonly, but user has the ability to unlock the course.
                    editCourseView.showLockLabel(true);
                } else {
                    // Course can be modified!
                    logger.info("determineAndSetReadOnly() - setting read only to false because file is not already locked"+
                            ", user can modify, doesn't have data collection depenedencies AND user didn't bypass the data collection warning.");
                    setReadOnly(false);
                    editCourseView.showLockLabel(false);
                }
            }
        }

    }
    
    /**
     * Handler for when a load has successfully completed and all server calls are completed.
     * 
     * @param path The file relative path to the course.
     * @param result The cached FetchJAXBObjectResult result that occurred during the load process. 
     */
    private void handleLoadSuccess(final String path, FetchJAXBObjectResult result) {

        //remove any editors that might have been modifying objects from a previous course
        editCourseView.getEditorPanel().stopAllEditing();
        
        if(result.isSuccess()){                    

            currentCourse = (Course) result.getJAXBObject(); 
            
            userCanModify = result.getModifiable();

            setFilePath(path);
            
            loadCourse();
            
            determineAndSetReadOnly();
            
            if(result.wasConversionAttempted() && result.getModifiable()) {
                
                WarningDialog.warning("File updated", "The file you've loaded was created with an old version of GIFT, "
                        + "but we were able to update it for you. This file has already been saved and "
                        + "is ready for modification. A backup of the original file was created: " 
                        + result.getFileName() + FileUtil.BACKUP_FILE_EXTENSION);
                
                //if the file was converted, we need to save it after the conversion
                saveCourse(path, false);
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
                    GatClientUtility.returnToDashboard();
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
            
        logger.fine("Got course object for course at '" + filePath + "'.");

        
    }
    
    /**
     * Fetches the lti properties from the server. This is part of a chained rpc call that happens during the load
     * process of the Gat (while loading into an existing course). 
     * 
     * @param path The relative file path to the course.
     * @param fetchJAXBObjectResult The cached FetchJAXBObjectResult result that occurred during the load process. 
     */
    private void doGetLtiPropertiesDuringLoad(final String path, final FetchJAXBObjectResult fetchJAXBObjectResult) {
        final String msg = "doGetLtiPropertiesDuringLoad";
        logger.info(msg);
        AsyncCallback<FetchLtiPropertiesResult> callback = new AsyncCallback<FetchLtiPropertiesResult>() {

            @Override
            public void onFailure(Throwable t) {
                logger.severe(msg + " onFailure()");
                handleCallbackFailure(logger, msg, t);
                showWaiting(false);
                GatClientUtility.returnToDashboard();
            }

            @Override
            public void onSuccess(FetchLtiPropertiesResult result) {
                logger.info(msg + " onSuccess() result=" + result);
                if (result.isSuccess()) {
                    
                    if (result.getCourseId() != null) {
                        // User has access to the lti properties panel.
                        ltiCourseId = result.getCourseId();
                    }     
                    
                    setHasDataCollectionDependencies(result.hasDataSets());
                } else {
                    logger.severe("Unable to fetch the lti properties for the course.");
                    ltiCourseId = null;
                }
                
                // Continue loading the course normally.
                handleLoadSuccess(path, fetchJAXBObjectResult);
                
            }
            
        };
        
        String userName = GatClientUtility.getUserName();
        FetchLtiProperties action = new FetchLtiProperties(path, userName);
        showWaiting(true);
        
        dispatchService.execute(action, callback);
        
    }
    
    /**
     * Fetches the lti properties from the server. This is part of a chained rpc call that happens during the save
     * process of the Gat (while saving an existing course OR the save process that occurs when a new course is created). 
     * 
     * @param saveNotification The notify event that was shown when the save process started.
     * @param showConfirmationDialog Boolean to indicate if a confirmation dialog should be presented to the user after the save.
     * @param validateCourse Boolean to indicate if the course needs to be validated after the save.
     * @param reloadWhenComplete Boolean to indicate if a reload should occur.
     * @param path The file relative path to the course.
     * @param result The cached SaveJaxbObjectResult that occurred during the save process.
     */
    private void doGetLtiPropertiesDuringSave(final Notify saveNotification,final boolean showConfirmationDialog, 
                                              final boolean validateCourse, final boolean reloadWhenComplete, 
                                              final String path, final SaveJaxbObjectResult saveJaxbObjectResult) {
 
        final String msg = "doGetLtiPropertiesDuringSave";
        logger.info(msg);
        AsyncCallback<FetchLtiPropertiesResult> callback = new AsyncCallback<FetchLtiPropertiesResult>() {

            @Override
            public void onFailure(Throwable t) {
                logger.severe(msg + " onFailure()");
                handleCallbackFailure(logger, msg, t);
                showWaiting(false);
                GatClientUtility.returnToDashboard();
            }

            @Override
            public void onSuccess(FetchLtiPropertiesResult result) {
                logger.info(msg + " onSuccess(): result=" + result);
                if (result.isSuccess()) {
                    
                    if (result.getCourseId() != null) {
                        // User has access to the lti properties panel.
                        ltiCourseId = result.getCourseId();
                    } 
                    
                    setHasDataCollectionDependencies(result.hasDataSets());

                } else {
                    logger.severe("Unable to fetch the lti properties for the course.");
                }
                
                // Continue saving the course normally
                handleSaveSuccess(saveNotification, showConfirmationDialog, validateCourse, reloadWhenComplete, path, saveJaxbObjectResult);
                
            }
            
        };
        
        String userName = GatClientUtility.getUserName();
        FetchLtiProperties action = new FetchLtiProperties(path, userName);
        showWaiting(true);
        
        dispatchService.execute(action, callback);
        
    }
    
    /**
     * Updates the internal variable to indicate if the course has dependencies on
     * data collection.
     * 
     * @param hasDependencies True if the course has dependencies on data collection, false otherwise.
     */
    private void setHasDataCollectionDependencies(boolean hasDependencies) {
        hasDataCollectionDependencies = hasDependencies;
    }
    
    /**
     * Loads a course.
     */
    private void loadCourse() {
        isCourseDirty = false;
        
        doGetSurveyContexts();
        
        //load concepts and their references to the appropriate resources
        CourseConceptUtility.setCourse(currentCourse);

        populateHeaderView();
        
        /*
         * Expose the global survey's composer's JavaScript functions to any sub-editor iframe windows so that all
         * of the GAT's editors use the same survey composer instance.
         */
        SurveyEditorModal.exposeNativeFunctions();

        eventBus.fireEvent(new CourseLoadedEvent(currentCourse));
        
        editCourseView.createCourseTree(currentCourse);

        editCourseView.getTreeManager().showBranchTree(null);
        
        if(GatClientUtility.isRtaLessonLevel() 
                && currentCourse.getTransitions() != null
                && currentCourse.getTransitions().getTransitionType().size() == 1) {
            
            //if GIFT's lesson level is set to RTA and only one course object is available, begin editing it by default
            displaySelectedTransition(currentCourse.getTransitions().getTransitionType().get(0));
        }
        
        //Hide the file loading dialog now that the course has loaded
        BsLoadingDialogBox.remove();
        showWaiting(false);

        /* If an external scenario is being loaded, try to automatically begin editing it */
        if(GatClientUtility.getExternalScenarioId() != null) {
            Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                
                @Override
                public void execute() {
                    
                    /* External scenario courses always have a single training application course object */
                    Serializable scenarioObject = currentCourse.getTransitions().getTransitionType().get(0);
                    editCourseView.getCourseTree().selectCourseObject(scenarioObject);
                    editCourseView.editExternalScenarioDkf(scenarioObject);
    }
            });
        }
    }
        
    /**
     * Do get survey contexts.
     */
    private void doGetSurveyContexts(){

        final String msg = "doGetSurveyContexts";
        logger.info(msg);

        if(!readOnly){
            
            //load the survey context referenced by the course or create a new survey context if the course isn't yet referencing one
            AsyncCallback<GetOrCreateSurveyContextResult> callback = new AsyncCallback<GetOrCreateSurveyContextResult>(){
    
                @Override
                public void onFailure(Throwable t) {
                    
                    BsLoadingDialogBox.remove();
                    
                    handleCallbackFailure(logger, msg, t);
                }
    
                @Override
                public void onSuccess(GetOrCreateSurveyContextResult result) {
                    
                    BsLoadingDialogBox.remove();
    
                    if(result.isSuccess()){
                        
                        Integer loadedContext = result.getSurveyContext();
                        
                        if(currentCourse != null){
                            
                            BigInteger newContextId = BigInteger.valueOf(loadedContext);
                            
                            //if a new survey context was generated, we need to save the course with the new survey context ID
                            //so that the survey context can be deleted when the course is deleted
                            if(!newContextId.equals(currentCourse.getSurveyContext())){
                                
                                currentCourse.setSurveyContext(newContextId);
                                
                                saveCourse(filePath, false);
                            }
                        }
    
                    } else {
                        
                        final ErrorDetailsDialog dialog = new ErrorDetailsDialog(
                                result.getErrorMsg(), 
                                result.getErrorDetails(), 
                                result.getErrorStackTrace());    
                        
                        dialog.setText("Failed to Load Survey Context");
                        
                        dialog.center();
                    }
                }        
            };
    
            if(currentCourse != null) {
                logger.info("Getting survey context.");
                
                BsLoadingDialogBox.display("Loading Course Resources", "Please wait while this course's resources are loaded.");
                
                GetOrCreateSurveyContext action = new GetOrCreateSurveyContext(
                        GatClientUtility.getUserName(),
                        currentCourse.getName(), 
                        currentCourse.getSurveyContext()
                );
                
                dispatchService.execute(action, callback);
            }
            
        }
    }
    
    private void showFileNewDialog() {
        fileNewDialog.clearFileName();
        fileNewDialog.center();
        
        // set focus to the new course name text field.  Must be after the dialog is shown.
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            @Override
            public void execute() {
                fileNewDialog.getFileSelector().getFileNameFocusInput().setFocus(true);
            }
        });

    }
    
    private String getFileNewPath() {
        String path = fileNewDialog.getValue();
        if(path == null) {
            return null;
        }
        
        path = path.trim();
        
        int index = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\')) + 1;
        String courseFolderName = path.substring(index, path.length());
        
        path += Constants.FORWARD_SLASH + courseFolderName + AbstractSchemaHandler.COURSE_FILE_EXTENSION;
        return path;
    }
    
    /**
     * Save the Course in the background 
     * (no validation, reload, or confirmation dialog)
     */
    private void saveCourse(){
        saveCourse(filePath, false, false, false);
    }
    
    private void saveCourseAndNotify() {
        saveCourse(filePath, true, false, false);
    }
    
    private void saveCourse(String path, boolean showConfirmationDialog, boolean validateCourse, boolean reloadWhenComplete) {
        saveCourse(path, showConfirmationDialog, validateCourse, reloadWhenComplete, null);
    }
    
    /**
     * Save the course.
     *
     * @param path the path to save the course to.
     * @param showConfirmationDialog whether or not to display the confirmation dialog
     */
    private void saveCourse(final String path, final boolean showConfirmationDialog) {
        saveCourse(path, showConfirmationDialog, false, false, null);
    }

    /**
     * Save the course.
     *
     * @param path the path to save the course to.
     * @param showConfirmationDialog whether or not to display the confirmation dialog
     * @param validateCourse whether or not to validate the course after saving
     * @param reloadWhenComplete whether or not the course should be reloaded after being saved
     * @param saveCallback callback to execute after the course has been saved. Can be null.
     */
    private void saveCourse(final String path, final boolean showConfirmationDialog, 
            final boolean validateCourse, final boolean reloadWhenComplete, final AsyncCallback<Boolean> saveCallback) {
        
        if(readOnly && path != null && filePath != null && path.equals(filePath)){
            WarningDialog.warning("Read only", "Cannot overwrite original file while viewing a read-only copy. <br/>"
                    + "<br/>"
                    + "If you wish to save this course to a different file, please specify another file name.");
            return;
        }
        
        final NotifySettings settings = NotifyUtil.generateDefaultSettings();
        settings.setDelay(0);    
        
        final Notify saveNotification = Notify.notify("", "Saving course, please wait.", IconType.SAVE, settings);
        
        final AsyncCallback<SaveJaxbObjectResult> callback = new AsyncCallback<SaveJaxbObjectResult>() {
            
            @Override
            public void onFailure(Throwable throwable) {
                showWaiting(false);        
                saveNotification.hide();

                String label = "Unable to save course: ";
                String title = "Failed to Save Course";

                if(filePath == null){
                    label = "Unable to create course: ";
                    title = "Failed to Create Course";
                    
                    fileNewDialog.hide();
                    fileNewDialog.center();
                    fileNewDialog.reallowCancel();
                }
                
                ErrorDetailsDialog error = new ErrorDetailsDialog(
                        label + throwable.getMessage(),
                        throwable.toString(), 
                        DetailedException.getFullStackTrace(throwable));
                error.setText(title);
                error.center();
                
                if(saveCallback != null) {
                    saveCallback.onSuccess(false);
                }
            }
            
            @Override
            public void onSuccess(final SaveJaxbObjectResult result) {

                // This is a chained sequence of rpcs.  First the SaveJaxbObject request is made, then
                // the FetchLtiProperties request is made.  Once both of those are completed, then the course
                // saving continues.       
                doGetLtiPropertiesDuringSave(
                        saveNotification, 
                        showConfirmationDialog, 
                        validateCourse, 
                        reloadWhenComplete, 
                        (filePath != null 
                                ? filePath      //use filePath for existing courses in case the course is renamed
                                : path          //use path for new courses since the final filePath hasn't been decided yet
                        ),
                        result
                );
                
                if(saveCallback != null) {
                    saveCallback.onSuccess(true);
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
        
        logger.fine("Saving course: " + path);    
        
        final SaveCourse action = new SaveCourse();
        action.setCourse(currentCourse);
        action.setPath(path);
        action.setAcquireLockInsteadOfRenew(acquireInsteadOfRenew);
        action.setUserName(userName);
        showWaiting(true);
        
        if(originalCourseName != null && !originalCourseName.equals(currentCourse.getName())) {
            
            // Prevent saving until after the course is renamed successfully
            rpcService.renameCourse(userName, path, currentCourse.getName(), new AsyncCallback<GenericRpcResponse<String>>() {

                @Override
                public void onFailure(Throwable caught) {
                    
                    ErrorDetailsDialog dialog = new ErrorDetailsDialog(
                            "An error occurred on the server while trying to save the course", 
                            caught.toString(), 
                            DetailedException.getFullStackTrace(caught));
                    
                    dialog.setText("Error Saving Course");
                    dialog.center();
                    
                    showWaiting(false);
                    saveNotification.hide();
                    fileNewDialog.reallowCancel();
                }

                @Override
                public void onSuccess(GenericRpcResponse<String> result) {
                    
                    if(result.getWasSuccessful()){
                        
                        //complete the remainder of the save operation
                        setFilePath(result.getContent());
                        action.setPath(result.getContent());
                        
                        dispatchService.execute(action, callback);
                        
                        //send notification that the course folder was just changed on the server
                        //this is useful for opened course object editors to update the path to the course folder
                        CourseFolderChangedEvent folderChangedEvent = new CourseFolderChangedEvent();
                        eventBus.fireEvent(folderChangedEvent);
                        
                    } else {
                        
                        editCourseView.getCourseNameButton().setValue(originalCourseName, true);
                        
                        ErrorDetailsDialog dialog = new ErrorDetailsDialog(
                                result.getException().getReason(), 
                                result.getException().getDetails(), 
                                result.getException().getErrorStackTrace()
                        );
                        
                        dialog.setText("Error Saving Course");
                        dialog.center();
                        
                        showWaiting(false);
                        saveNotification.hide();
                        fileNewDialog.reallowCancel();
                    }
                }
                
            });
        } else {
            dispatchService.execute(action, callback);      
        }
    }
    
    /**
     * Preview the course 
     * 
     * @param index The index of the course transition that should be previewed.  If negative, the preview
     * will start at the first course object.
     */
    private void previewCourse(final int index) {
        
        if(readOnly) {
            //no save mention or options in this case
            
              final NewOrExistingFileDialog dialog = new NewOrExistingFileDialog("Preview", 
                        new ClickHandler() {
        
                            @Override
                            public void onClick(ClickEvent event) {
                                // preview from beginning of course                             
                                GatClientUtility.previewCourseObject(0, filePath);
                            }
                            
                        }, 
                        
                        new ClickHandler() {
        
                            @Override
                            public void onClick(ClickEvent event) {
                                // Preview from current course object
                                GatClientUtility.previewCourseObject(index < 0 ? 0 : index, filePath);
                            }
                            
                });
                
                dialog.getCreateNewButton().setText("Preview");
                dialog.getSelectExistingButton().setText("Preview from selected object");
                dialog.getSelectExistingButton().getElement().getStyle().setProperty("padding", "0px 20px");
                dialog.getSelectExistingButton().getElement().getStyle().setProperty("whiteSpace", "normal");
                dialog.center();
            
        } else {
            //need to mention that the course will be saved
            
            final NewOrExistingFileDialog dialog = new NewOrExistingFileDialog("Preview", 
                    new ClickHandler() {
    
                        @Override
                        public void onClick(ClickEvent event) {
                            // Save then preview from beginning of course
                            
                            saveCourse(filePath, false, false, false, new AsyncCallback<Boolean>() {
    
                                @Override
                                public void onFailure(Throwable cause) {
                                    // Nothing to do
                                }
    
                                @Override
                                public void onSuccess(Boolean success) {
                                    if(success) {
                                        GatClientUtility.previewCourseObject(0, filePath);
                                    }
                                }
                                
                            });
                        }
                        
                    }, 
                    
                    new ClickHandler() {
    
                        @Override
                        public void onClick(ClickEvent event) {
                            // Save then preview from selected course object
                            
                            saveCourse(filePath, false, false, false, new AsyncCallback<Boolean>() {
                                
                                @Override
                                public void onFailure(Throwable cause) {
                                    // Nothing to do
                                }
    
                                @Override
                                public void onSuccess(Boolean success) {
                                    if(success) {
                                        GatClientUtility.previewCourseObject(index < 0 ? 0 : index, filePath);
                                    }
                                }
                                
                            });
                        }
                        
            });
            
            dialog.setOptionalMessage("The course will be saved before previewing in order to capture any changes that might have been made.");
            dialog.getCreateNewButton().setText("Save then Preview");
            dialog.getSelectExistingButton().setText("Save then Preview from selected object");
            dialog.getSelectExistingButton().getElement().getStyle().setProperty("padding", "0px 20px");
            dialog.getSelectExistingButton().getElement().getStyle().setProperty("whiteSpace", "normal");
            dialog.center();
        }
    }
    
    /**
     * Tells the server to lock the course at the given path.
     * @param path Path of the course to lock.
     * @param acquisition True if we're trying to acquire the lock for the
     * first time, false if we're simply renewing the lock.
     */
    private void lockFile(String path, final boolean acquisition) {
        
        final String userName = GatClientUtility.getUserName();
        final String browserSessionKey = GatClientUtility.getBrowserSessionKey();
        
        AsyncCallback<LockFileResult> callback = new AsyncCallback<LockFileResult>() {
            @Override
            public void onFailure(Throwable t) {

                logger.info("lockcourse failure - " + t);
                if(acquisition){
                    refreshData();
                }
            }

            @Override
            public void onSuccess(LockFileResult result) {

                //Handle a server side failure
                if(!result.isSuccess()) {
                    StringBuilder sb = new StringBuilder("Failed to lock course: ")
                            .append(result.getErrorMsg()).append("\n")
                            .append(result.getErrorDetails()).append("\n")
                            .append(result.getErrorStackTrace());
                    logger.severe(sb.toString());
                    
                    return;
                }
                
                logger.info("lockcourse result details -\n" + result.getCourseFileAccessDetails());

                //Determine if write access was still granted
                boolean hasWriteAccess = false;
                if(result.getCourseFileAccessDetails() != null) {
                    List<CourseFileUserPermissionsDetails> accessList = new ArrayList<>(
                            result.getCourseFileAccessDetails().getUsersPermissions().values());
                    editCourseView.setUserAccessList(accessList);
                    
                    hasWriteAccess = result.getCourseFileAccessDetails().userHasWritePermission(userName, browserSessionKey);                    
                }
                
                if (hasWriteAccess) {
                    if(acquisition){
                        refreshData();
                    }
                }
            }
        };
        
        logger.info("lockFile request: acquisition = "+acquisition);
        //Try to lock the course.
        LockCourse lockCourse = new LockCourse();
        lockCourse.setPath(path);
        lockCourse.setUserName(userName);
        lockCourse.setBrowserSessionKey(browserSessionKey);
        dispatchService.execute(lockCourse, callback);
    }
    
    /**
     * Releases the lock on the current course.
     * @param isWindowClosing - flag for if unlockFile is called while window is closing. If true, the async callback does nothing.
     */
    private void unlockFile(final boolean isWindowClosing) {

        if(filePath != null){
            
            //This callback does absolutely nothing. In the case of releasing the
            //lock that is the appropriate course of action. It seems the only way
            //the lock wouldn't be released is if communication to the server
            //fails. The probability of that is very low but if we don't handle
            //it then that course will be locked forever.
            //
            //TODO Handle the failure case.
            AsyncCallback<GatServiceResult> callback =  new AsyncCallback<GatServiceResult>() {
                @Override
                public void onFailure(Throwable t) {
                    
                    logger.warning("Received failure to requesting to unlock course on the server");
                    if (!isWindowClosing) {
                        refreshData();
                    }
                }
                @Override
                public void onSuccess(GatServiceResult result) {
                    
                    logger.info("Received response to requesting to unlock course on the server - "+result);
                    if (!isWindowClosing) {
                        if (result != null && result.isSuccess() && hasAcquiredCourseLock) {
                            hasAcquiredCourseLock = false;
                        }

                        refreshData();
                    }
                }
            };
            
            String userName = GatClientUtility.getUserName();
            String browserSessionKey = GatClientUtility.getBrowserSessionKey();
            
            //Try to unlock the course.
            UnlockCourse unlockCourse = new UnlockCourse();
            unlockCourse.setRelativePath(filePath);
            unlockCourse.setUserName(userName);
            unlockCourse.setBrowserSessionKey(browserSessionKey);

            logger.info("Requesting to unlock course on the server");
            dispatchService.execute(unlockCourse, callback);
        }
    }

    /* (non-Javadoc)
     * @see mil.arl.gift.tools.authoring.gat.client.presenter.AbstractGatPresenter#getLogger()
     */
    @Override
    protected Logger getLogger() {
        return logger;
    }
    
    private void createNewFile(String path) {
        
        String courseName = fileNewDialog.getFileNameTextBox().getValue();
        currentCourse = createValidCourseObject(courseName);
                
        //save the new course and reload it so that a survey context can be generated for it
        saveCourse(path, false, false, true);
        
        IFrameMessageHandlerChild.getInstance().sendMessage(new IFrameSimpleMessage(IFrameMessageType.GAT_FILES_OPEN));
    }

    /**
     * Initializes the view.
     */
    @Inject
    private void init() {
        logger.info("CoursePresenter::init() called.");

        // Setup defaults for some conditions that can affect readonly for the file.
        fileAlreadyLocked = false;
        userCanModify = true;
        hasDataCollectionDependencies = false;
        userBypassedDataCollectionWarning = false;
        hasAcquiredCourseLock = false;
        ltiCourseId = null;
        
        setupMenuCommands();
      
        
        handlerRegistrations.add(editCourseView.getHeaderView().getNameInput().addValueChangeHandler(new ValueChangeHandler<String>(){

            @Override
            public void onValueChange(ValueChangeEvent<String> event) {

                if(currentCourse != null && !event.getValue().trim().isEmpty()){
                    currentCourse.setName(event.getValue());
                    SharedResources.getInstance().getEventBus().fireEvent(new EditorDirtyEvent());
                } else if (currentCourse != null) {
                    editCourseView.getHeaderView().getNameInput().setValue(currentCourse.getName());
                }
            }
            
        }));
        
        handlerRegistrations.add(editCourseView.getHeaderView().getDescriptionInput().addValueChangeHandler(new ValueChangeHandler<String>(){

            @Override
            public void onValueChange(ValueChangeEvent<String> event) {

                if(currentCourse != null){
                    currentCourse.setDescription(event.getValue());
                    
                    SharedResources.getInstance().getEventBus().fireEvent(new EditorDirtyEvent());
                }
            }
            
        }));
        
        editCourseView.getHeaderView().setAddDescriptionHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                editCourseView.displayDescriptionEditor();
            }
            
        });
        
        handlerRegistrations.add(editCourseView.getHeaderView().getExcludeCheckBox().addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                
                if(currentCourse != null){
                
                    if(event.getValue() != null && event.getValue()){
                        currentCourse.setExclude(BooleanEnum.TRUE);
                        
                    } else {
                        currentCourse.setExclude(null);
                    }
                    
                    SharedResources.getInstance().getEventBus().fireEvent(new EditorDirtyEvent());
                }
            }
        }));
        
        addLtiProviderDialog.setConfirmClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                String identifier = addLtiProviderDialog.getIdentifier();
                String key = addLtiProviderDialog.getKey();
                String value = addLtiProviderDialog.getValue();
                
                if (identifier == null || identifier.trim().isEmpty()) {

                    WarningDialog.warning("Invalid Identifier", "LTI providers can only be referenced through a valid identifier.");
                    return;

                } else if (doesLtiProviderIdentifierCauseConflict(identifier)) {

                    WarningDialog.warning("Duplicate LTI Provider Identifier", "LTI provider identifiers must be unique. Be aware that some LTI providers are system configurations. ");
                    return;
                } else if (key == null || key.trim().isEmpty()) {

                    WarningDialog.warning("Invalid Client Key", "The LTI provider client key cannot be left empty.");
                    return;
                } else if (value == null || value.trim().isEmpty()) {

                    WarningDialog.warning("Invalid Client Shared Secret", "The LTI provider client shared secret cannot be left empty.");
                    return;
                }
                
                if (ltiProviderBeingEdited == null) {
                    // new provider
                    LtiProvider provider = new LtiProvider();
                    provider.setIdentifier(identifier.trim());
                    provider.setKey(key.trim());
                    provider.setSharedSecret(value.trim());
                    if (currentCourse.getLtiProviders() == null) {
                        LtiProviders ltiProviders = new LtiProviders();
                        ltiProviders.getLtiProvider().add(provider);
                        currentCourse.setLtiProviders(ltiProviders);
                    } else {
                        currentCourse.getLtiProviders().getLtiProvider().add(provider);
                    }
                } else {
                    String oldId = ltiProviderBeingEdited.getIdentifier();
                    ltiProviderBeingEdited.setIdentifier(identifier.trim());
                    ltiProviderBeingEdited.setKey(key.trim());
                    ltiProviderBeingEdited.setSharedSecret(value.trim());
                    
                    updateLtiProviderIdReferences(oldId, identifier);
                }
                
                courseLtiProvidersChanged();
                
                ltiProviderBeingEdited = null;    
                addLtiProviderDialog.hide();
            }
        });
        
        handlerRegistrations.add(editCourseView.getHeaderView().getAddLtiProviderListButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {

                ltiProviderBeingEdited = null;
                addLtiProviderDialog.clearTextBoxFields();
                addLtiProviderDialog.setText("Add LTI Provider");
                addLtiProviderDialog.setConfirmButtonText("Add LTI Provider");
                
                addLtiProviderDialog.center();
            }
        }));

        handlerRegistrations.add(editCourseView.getHeaderView().addEmptyLtiProvidersListPanelClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {

                ltiProviderBeingEdited = null;
                addLtiProviderDialog.clearTextBoxFields();
                addLtiProviderDialog.center();
            }
        }));
              
        editCourseView.getHeaderView().getLtiProviderListProtectClientDataColumn().setFieldUpdater(new FieldUpdater<LtiProvider, Boolean>() {

            @Override
            public void update(int index, final LtiProvider provider, Boolean value) {

                // do nothing if read only. Want to maintain current state.
                if (GatClientUtility.isReadOnly()) {
                    return;
                }
                
                if (Boolean.TRUE.equals(value)) {

                    String message = "Are you sure you want to make this LTI provider protected? To ensure your privacy, make sure to select 'inline' or 'modal' as the LTI provider display mode 'new window' will display the specified URL in the address bar to connect to the client.";

                    OkayCancelDialog.show("Protect " + provider.getIdentifier() + "?", message, "Protect", new OkayCancelCallback() {

                        @Override
                        public void okay() {
                            provider.setProtectClientData(BooleanEnum.TRUE);
                            ltiProviderListDataProvider.refresh();
                        }

                        @Override
                        public void cancel() {
                            provider.setProtectClientData(BooleanEnum.FALSE);
                            ltiProviderListDataProvider.refresh();
                        }

                    });
                } else {
                    provider.setProtectClientData(BooleanEnum.FALSE);
                    ltiProviderListDataProvider.refresh();
                }
            }
        });
        
        editCourseView.getHeaderView().getLtiProviderListEditColumn().setFieldUpdater(new FieldUpdater<LtiProvider, String>() {

            @Override
            public void update(int index, LtiProvider provider, String value) {

                ltiProviderBeingEdited = provider;
                
                addLtiProviderDialog.setIdentifier(provider.getIdentifier());
                if (GatClientUtility.isReadOnly()) {
                    addLtiProviderDialog.setKey(PROTECTED_LTI_DATA);
                    addLtiProviderDialog.setValue(PROTECTED_LTI_DATA);
                } else {
                    addLtiProviderDialog.setKey(provider.getKey());
                    addLtiProviderDialog.setValue(provider.getSharedSecret());
                }
                
                addLtiProviderDialog.setText("Edit LTI Provider");
                addLtiProviderDialog.setConfirmButtonText("Apply");
                addLtiProviderDialog.center();
            }
        });
        
        editCourseView.getHeaderView().getLtiProviderListRemoveColumn().setFieldUpdater(new FieldUpdater<LtiProvider, String>() {

            @Override
            public void update(int index, final LtiProvider provider, String value) {

                List<String> ltiProviderReferences = getLtiProviderReferences(provider.getIdentifier());

                // references exist, so we need to prevent the deletion and show the concepts
                StringBuilder sb = new StringBuilder();
                sb.append("Are you sure you want to remove the LTI provider <b>");
                sb.append(provider.getIdentifier());
                sb.append("</b>?");
                if (!ltiProviderReferences.isEmpty()) {
                    sb.append(" It is being used in the following course transitions: <ul>");

                    for (String reference : ltiProviderReferences) {
                        sb.append("<li>");
                        sb.append(reference);
                        sb.append("</li>");
                    }

                    sb.append("</ul>");
                }

                OkayCancelDialog.show("Remove LTI Provider?", sb.toString(), "Remove", new OkayCancelCallback() {

                    @Override
                    public void okay() {
                        if (currentCourse.getLtiProviders() == null || currentCourse.getLtiProviders().getLtiProvider().isEmpty()) {
                            return;
                        }

                        currentCourse.getLtiProviders().getLtiProvider().remove(provider);
                        editCourseView.getHeaderView().showLtiProvidersListEmptyPanel(currentCourse.getLtiProviders().getLtiProvider().isEmpty());

                        courseLtiProvidersChanged();
                    }

                    @Override
                    public void cancel() {
                        // Nothing to do
                    }

                });
            }
        });
        
        handlerRegistrations.add(editCourseView.getCourseNameButton().addValueChangeHandler(new ValueChangeHandler<String>() {

            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                
                if(currentCourse != null && !event.getValue().trim().isEmpty()){
                    
                    final String newCourseName = event.getValue().trim();
                    
                    String validationMsg = DocumentUtil.validateFileName(newCourseName);
                    if (validationMsg != null) {
                        editCourseView.getCourseNameButton().setValue(originalCourseName, true);
                        WarningDialog.warning("Invalid Course Name", validationMsg +  "<br/><br/>Please enter a new name.");
                        return;
                    }
                    
                    editCourseView.getCourseNameButton().setValue(newCourseName); //apply string trim to text field, don't fire event 
                    
                    // Check for a course name conflict if the user enters a new name
                    
                    if(originalCourseName != null 
                            && !originalCourseName.equalsIgnoreCase(newCourseName) 
                            && !currentCourse.getName().equalsIgnoreCase(newCourseName)) {
                        
                        BsLoadingDialogBox.display("Checking Course Name", "Checking the course name for conflicts with existing courses. Please wait...");
                        
                        rpcService.checkCourseName(GatClientUtility.getUserName(), filePath, newCourseName, new AsyncCallback<GenericRpcResponse<Boolean>>() {

                            @Override
                            public void onFailure(Throwable caught) {
                                
                                BsLoadingDialogBox.remove();
                                ErrorDetailsDialog dialog = new ErrorDetailsDialog(
                                        "An error occurred on the server while checking the course name for conflicts.", 
                                        caught.toString(), 
                                        DetailedException.getFullStackTrace(caught));
                                
                                dialog.setText("Error");
                                dialog.center();
                                
                            }

                            @Override
                            public void onSuccess(GenericRpcResponse<Boolean> result) {
                                
                                BsLoadingDialogBox.remove();
                                
                                if(result.getWasSuccessful()){ 
                                    
                                    currentCourse.setName(newCourseName);
                                    
                                } else {
                                    
                                    editCourseView.getCourseNameButton().setValue(originalCourseName, true);
                                    
                                    ErrorDetailsDialog dialog = new ErrorDetailsDialog(
                                            result.getException().getReason(), 
                                            result.getException().getDetails(), 
                                            result.getException().getErrorStackTrace()
                                    );
                                    
                                    dialog.setText("Duplicate Course Name");
                                    dialog.center();
                                    
                                }
                            }
                        });
                        
                    } else {
                        currentCourse.setName(newCourseName);
                    }
                    
                } else if (currentCourse != null) {
                    
                    editCourseView.getHeaderView().getNameInput().setValue(currentCourse.getName());
                    editCourseView.getCourseNameButton().setValue(currentCourse.getName());
                }
            }
            
        }));
        
        handlerRegistrations.add(editCourseView.getHeaderView().getClearImageButton().addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                showWarningAndSetValue();
            }
            
            private void showWarningAndSetValue(){
                
                OkayCancelDialog.show(
                        "Deselect Course Image?", 
                        "Are you sure you would like to remove the current course image? if yes, then the default image will be used.",
                        "Yes",
                        new OkayCancelCallback() {
                    
                    @Override
                    public void okay() {
                        currentCourse.setImage(null);
                        editCourseView.getHeaderView().getCourseImageNameLabel().setText("Default image selected.");
                        editCourseView.getHeaderView().setClearImageButtonVisible(false);
                        editCourseView.getHeaderView().setPreviewTileIconVisible(false);
                        eventBus.fireEvent(new EditorDirtyEvent());
                    }
                    
                    @Override
                    public void cancel() {
                        //Nothing to do
                    }
                });
            }
        }));
        
        handlerRegistrations.add(editCourseView.getHeaderView().getPreviewTileIcon().addDomHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                String userName = GatClientUtility.getUserName();
                final FetchContentAddress action = new FetchContentAddress(getCourseFolderPath(), currentCourse.getImage(), userName);

                dispatchService.execute(action, new AsyncCallback<FetchContentAddressResult>() {

                    @Override
                    public void onFailure(Throwable cause) {
                        ErrorDetailsDialog dialog = new ErrorDetailsDialog(
                                "Preview Image onFailure", 
                                cause.getMessage(), 
                                DetailedException.getFullStackTrace(cause));    
                                dialog.setDialogTitle("Preview Failed");
                                dialog.center();
                    }

                    @Override
                    public void onSuccess(FetchContentAddressResult result) {
                        ModalDialogBox imagePreviewDialog = new ModalDialogBox();
                        Image previewImage;
                        logger.info("Fetch course tile result = " + result);

                        if(result.isSuccess()) {
                            previewImage = new Image(result.getContentURL());
                            previewImage.setSize("310px", "140px");
                        } else {
                            previewImage = new Image(GatClientBundle.INSTANCE.image_not_found());
                        }
                        imagePreviewDialog.setSize("340px", "330px");
                        imagePreviewDialog.setAnimationEnabled(true);
                        imagePreviewDialog.add(previewImage);
                        imagePreviewDialog.setCloseable(true);
                        imagePreviewDialog.setHtml("<html><font size=\"3\"><b>Image is scaled to tile size.</font></b></html>");
                        imagePreviewDialog.center();
                    }            
                });
            }
            
        }, ClickEvent.getType()));
        
        handlerRegistrations.add(editCourseView.getHeaderView().getCourseTileImageButton().addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                logger.info("CourseTileImage clicked with readOnly = " + readOnly);
                if (!readOnly) {
                    NewOrExistingFileDialog.showSelectOrUseDefault("Course Tile Image", new ClickHandler() {

                        @Override
                        public void onClick(ClickEvent event) {
                            editCourseView.getHeaderView().showCourseImageFileDialog();
                        }
                        
                    }, new ClickHandler() {

                        @Override
                        public void onClick(ClickEvent event) {
                            currentCourse.setImage(null);
                            editCourseView.getHeaderView().getCourseTileImage().setResource(GatClientBundle.INSTANCE.course_default());
                            editCourseView.getHeaderView().getCourseTileImage().setWidth("310px");
                            editCourseView.getHeaderView().getCourseTileImage().setHeight("140px");
                        }
                        
                    });
                }
                
            }
            
        }));
        
        handlerRegistrations.add(editCourseView.getHeaderView().getRefreshCourseHistoryButton().addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                logger.info("RefreshCourseHistory clicked");
                fetchCourseHistory();
            }
            
        }));
        
        logger.info("Setting handler registration for learner & ped");
        handlerRegistrations.add(editCourseView.getHeaderView().getLearnerFileButton().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                
                logger.info("Learner file link clicked with readonly = " + readOnly);
                if (!readOnly) {
                    NewOrExistingFileDialog dialog = new NewOrExistingFileDialog(CourseObjectName.LEARNER_CONFIG.getDisplayName(), new ClickHandler() {

                        @Override
                        public void onClick(ClickEvent event) {
                            editCourseView.getHeaderView().showLearnerConfigEditor(getCourseFolderPath(), null);
                        }
                        
                    }, new ClickHandler() {

                        @Override
                        public void onClick(ClickEvent event) {
                            editCourseView.getHeaderView().showLearnerSelectDialog();
                        }
                        
                    });
                    
                    dialog.getCreateNewButton().setText("Use Template");
                    dialog.getSelectExistingButton().setText("Use Existing");
                    
                    dialog.center();
                }
                
            }
        }));
        
        handlerRegistrations.add(editCourseView.getHeaderView().getLearnerEditButton().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                
                if(currentCourse.getConfigurations() == null){
                    currentCourse.setConfigurations(new Configurations());
                }
                
                if(currentCourse.getConfigurations().getLearner() != null){
                
                    editCourseView.getHeaderView().showLearnerConfigEditor(
                            getCourseFolderPath(), 
                            currentCourse.getConfigurations().getLearner()
                    );
                    
                } else {
                    
                    editCourseView.getHeaderView().showLearnerConfigEditor(getCourseFolderPath(), null);
                }
            }
            
        }));
        
        handlerRegistrations.add(editCourseView.getHeaderView().getPedFileButton().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                logger.info("Ped file link clicked with readonly = " + readOnly);
                if (!readOnly) {
                    NewOrExistingFileDialog dialog = new NewOrExistingFileDialog(
                            "Replace " + CourseObjectName.PED_CONFIG.getDisplayName(), 
                            new ClickHandler() {

                                @Override
                                public void onClick(ClickEvent event) {
                                    editCourseView.getHeaderView().showPedConfigEditor(getCourseFolderPath(), null);
                                }
                                
                            }, new ClickHandler() {
        
                                @Override
                                public void onClick(ClickEvent event) {
                                    editCourseView.getHeaderView().showPedSelectDialog();
                                }
                                
                            }
                    );
                    
                    dialog.getCreateNewButton().setText("Use Template");
                    dialog.getSelectExistingButton().setText("Use Existing");
                    
                    dialog.center();
                }
                
            }
            
        }));
        
        handlerRegistrations.add(editCourseView.getHeaderView().getPedEditButton().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                
                if(currentCourse.getConfigurations() == null){
                    currentCourse.setConfigurations(new Configurations());
                }
                
                if(currentCourse.getConfigurations().getPedagogy() != null){
                
                    editCourseView.getHeaderView().showPedConfigEditor(
                            getCourseFolderPath(), 
                            currentCourse.getConfigurations().getPedagogy()
                    );
                    
                } else {
                    
                    editCourseView.getHeaderView().showPedConfigEditor(getCourseFolderPath(), null);
                }                
            }
            
        }));
        
        handlerRegistrations.add(editCourseView.getHeaderView().getLearnerFileDialog().addValueChangeHandler(new ValueChangeHandler<String>() {

            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                
                if(currentCourse.getConfigurations() == null){
                    currentCourse.setConfigurations(new Configurations());
                }
                
                String previousFile = currentCourse.getConfigurations().getLearner();
                String newFile = null;
                
                if(event.getValue() == null || event.getValue().isEmpty()) {
                    newFile = null;
                } else {
                    newFile = event.getValue();
                }
                
                currentCourse.getConfigurations().setLearner(newFile);
                
                if(previousFile != null && !previousFile.equals(newFile)){
                    
                    final String previousFilePath = getCourseFolderPath() + Constants.FORWARD_SLASH + previousFile;
                    
                    UnlockLearnerConfiguration action = new UnlockLearnerConfiguration();
                    action.setRelativePath(previousFilePath);
                    action.setUserName(GatClientUtility.getUserName());
                    action.setBrowserSessionKey(GatClientUtility.getBrowserSessionKey());
                    
                    //unlock the previous configuration file
                    SharedResources.getInstance().getDispatchService().execute(action, new AsyncCallback<GatServiceResult>() {

                        @Override
                        public void onFailure(Throwable caught) {
                            
                             ErrorDetailsDialog dialog = new ErrorDetailsDialog(
                                     "Failed to delete previous learner configuration file.", 
                                     caught.getMessage(), 
                                     DetailedException.getFullStackTrace(caught));
                             dialog.setDialogTitle("Learner Configuration Cleanup Failed");
                             dialog.center();
                        }

                        @Override
                        public void onSuccess(GatServiceResult result) {
                            
                            List<String> workspacePaths = new ArrayList<String>();
                            workspacePaths.add(previousFilePath);
                            
                            DeleteWorkspaceFiles deleteAction = new DeleteWorkspaceFiles(
                                    GatClientUtility.getUserName(), 
                                    GatClientUtility.getBrowserSessionKey(),
                                    workspacePaths, 
                                    true
                            );
                            
                            //delete the previous configuration file
                            SharedResources.getInstance().getDispatchService().execute(deleteAction, new AsyncCallback<GatServiceResult>() {

                                @Override
                                public void onFailure(Throwable caught) {
                                    
                                     ErrorDetailsDialog dialog = new ErrorDetailsDialog(
                                             "Failed to delete previous learner configuration file.", 
                                             caught.getMessage(), 
                                             DetailedException.getFullStackTrace(caught));
                                     dialog.setDialogTitle("Learner Configuration Cleanup Failed");
                                     dialog.center();
                                }

                                @Override
                                public void onSuccess(GatServiceResult result) {
                                    
                                     if(!result.isSuccess()){
                                         
                                         ErrorDetailsDialog dialog = new ErrorDetailsDialog(
                                                 "Failed to delete previous learner configuration file.", 
                                                 result.getErrorMsg(), 
                                                 result.getErrorStackTrace());
                                         dialog.setDialogTitle("Learner Configuration Cleanup Failed");
                                         dialog.center();
                                     }
                                }
                            });
                        }
                        
                    });    
                }    
                
                eventBus.fireEvent(new EditorDirtyEvent());
            }
        }));
        
        handlerRegistrations.add(editCourseView.getHeaderView().getPedagogicalFileDialog().addValueChangeHandler(new ValueChangeHandler<String>() {

            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                
                if(currentCourse.getConfigurations() == null){
                    currentCourse.setConfigurations(new Configurations());
                }
                
                String previousFile = currentCourse.getConfigurations().getPedagogy();
                String newFile = null;
                
                if(event.getValue() == null || event.getValue().isEmpty()) {
                    newFile = null;
                } else {
                    newFile = event.getValue();
                }
                
                currentCourse.getConfigurations().setPedagogy(newFile);
                
                if(previousFile != null && !previousFile.equals(newFile)){
                    
                    final String previousFilePath = getCourseFolderPath() + Constants.FORWARD_SLASH + previousFile;
                    
                    UnlockPedagogyConfiguration action = new UnlockPedagogyConfiguration();
                    action.setRelativePath(previousFilePath);
                    action.setUserName(GatClientUtility.getUserName());
                    action.setBrowserSessionKey(GatClientUtility.getBrowserSessionKey());
                    
                    //unlock the previous configuration file
                    SharedResources.getInstance().getDispatchService().execute(action, new AsyncCallback<GatServiceResult>() {

                        @Override
                        public void onFailure(Throwable caught) {
                            
                             ErrorDetailsDialog dialog = new ErrorDetailsDialog(
                                     "Failed to delete previous pedagogical configuration file.", 
                                     caught.getMessage(), 
                                     DetailedException.getFullStackTrace(caught));
                             dialog.setDialogTitle("Pedagogical Configuration Cleanup Failed");
                             dialog.center();
                        }

                        @Override
                        public void onSuccess(GatServiceResult result) {
                            
                            List<String> workspacePaths = new ArrayList<String>();
                            workspacePaths.add(previousFilePath);
                            
                            DeleteWorkspaceFiles deleteAction = new DeleteWorkspaceFiles(
                                    GatClientUtility.getUserName(), 
                                    GatClientUtility.getBrowserSessionKey(),
                                    workspacePaths, 
                                    true
                            );
                            
                            //delete the previous configuration file
                            SharedResources.getInstance().getDispatchService().execute(deleteAction, new AsyncCallback<GatServiceResult>() {

                                @Override
                                public void onFailure(Throwable caught) {
                                    
                                     ErrorDetailsDialog dialog = new ErrorDetailsDialog(
                                             "Failed to delete previous pedagogical configuration file.", 
                                             caught.getMessage(), 
                                             DetailedException.getFullStackTrace(caught));
                                     dialog.setDialogTitle("Pedagogical Configuration Cleanup Failed");
                                     dialog.center();
                                }

                                @Override
                                public void onSuccess(GatServiceResult result) {
                                    
                                     if(!result.isSuccess()){
                                         
                                         ErrorDetailsDialog dialog = new ErrorDetailsDialog(
                                                 "Failed to delete previous pedagogical configuration file.", 
                                                 result.getErrorMsg(), 
                                                 result.getErrorStackTrace());
                                         dialog.setDialogTitle("Pedagogical Configuration Cleanup Failed");
                                         dialog.center();
                                     }
                                }
                            });
                        }
                        
                    });    
                }
                
                eventBus.fireEvent(new EditorDirtyEvent());
            }
        }));
                
        handlerRegistrations.add(editCourseView.getHeaderView().getCourseImageFileDialog().addValueChangeHandler(new ValueChangeHandler<String>() {

            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                
                currentCourse.setImage(event.getValue());
                
                logger.fine("Selection is: " + event.getValue());
                
                editCourseView.getHeaderView().getCourseImageNameLabel().setText(event.getValue());
                
                editCourseView.getHeaderView().setClearImageButtonVisible(true);
                editCourseView.getHeaderView().setPreviewTileIconVisible(true);
                
                fetchCourseImage();
                
                eventBus.fireEvent(new EditorDirtyEvent());
                
                
            }
        }));
        
        handlerRegistrations.add(editCourseView.getHeaderView().getCourseImageFileDialog().addValueChangeHandler(new ValueChangeHandler<String>() {

            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                
                currentCourse.setImage(event.getValue());
                
                logger.fine("Selection is: " + event.getValue());
                
                editCourseView.getHeaderView().getCourseImageNameLabel().setText(event.getValue());
                
                editCourseView.getHeaderView().setClearImageButtonVisible(true);
                editCourseView.getHeaderView().setPreviewTileIconVisible(true);
                
                eventBus.fireEvent(new EditorDirtyEvent());
                
                
            }
        }));
        
        //update the cursor to indicate that course objects can be dragged into course tree nodes
        editCourseView.getCourseTree().setSVGDragOverFunction(new DatumFunction<Void>() {
            
            @Override
            public Void apply(Element context, Value d, int index) {
                
                //by default, the browser prevents dropping elements into other elements, so we need to disable this
                D3.event().preventDefault();
                
                return null;
            }
        });
                
        //handle when the user drops a course object on top of a course tree node
        editCourseView.getCourseTree().setSVGDropFunction(new DatumFunction<Void>() {
                
            @Override
            public Void apply(Element context, Value d, int index) {
                    
                D3.event().preventDefault(); //prevents Firefox from navigating away from the page when drag data is added
                        
                final Serializable dragged = transitionBeingDragged;
                transitionBeingDragged = null;
                
                if(dragged != null){
                    dragCourseObject(dragged, d);
                }
                
                return null;
            }
        });
        
        
        editCourseView.getCourseTree().setNodeClickFunction(new DatumFunction<Void>() {
            
                @Override
            public Void apply(Element context, Value d, int index) {
                    
                mil.arl.gift.tools.authoring.server.gat.client.view.course.tree.TreeNode node = 
                        d.<mil.arl.gift.tools.authoring.server.gat.client.view.course.tree.TreeNode> as();
                
                TreeNodeEnum type = TreeNodeEnum.valueOf(node.getType());
                
                if(type != null){
                    
                    if(TreeNodeEnum.TRANSITION.equals(type)){
                        
                        Serializable courseObject = editCourseView.getCourseTree().getCourseObject(node);
                        
                        if(courseObject != null){
                            displaySelectedTransition(courseObject);
                }
                    }
                
            }
        
                return null;
            }
        });
        
        editCourseView.getCourseTree().setNodeContextFunction(new DatumFunction<Void>() {

            @Override
            public Void apply(Element context, Value d, int index) {
                    
                mil.arl.gift.tools.authoring.server.gat.client.view.course.tree.TreeNode node = 
                        d.<mil.arl.gift.tools.authoring.server.gat.client.view.course.tree.TreeNode> as();
                
                editCourseView.getCourseTree().setSelectedNode(node, true);
                
                TreeNodeEnum type = TreeNodeEnum.valueOf(node.getType());
                
                if(type != null){
                    
                    if(TreeNodeEnum.TRANSITION.equals(type)){                        
                        
                        transitionNodeContextMenu.setPopupPosition(
                                D3.event().getClientX(),
                                D3.event().getClientY()
                        );
                        transitionNodeContextMenu.show();
                }
                }            
                
                return null;
            }
        });

        //
        // Course object action handlers
        //

        addWebpageHandlers();
        addLocalWebpageHandlers();
        addStructuredReviewHandlers();
        addGuidanceHandlers();
        addAuthoredBranchHandlers();

        addMediaCollectionHandlers();
        addImageHandlers();
        addPDFHandlers();
        addSlideshowHandlers();
        addYoutubeVideoHandlers();  
        addVideoHandlers(); 
        
        addAdaptiveCourseflowHandlers();

        addAutoTutorHandlers();
        addConversationTreeHandlers();
        addQuestionBankHandlers();
        addSurveyHandlers();

        addPowerPointHandlers();
        addVBSHandlers();
        addTC3Handlers();
        addTestbedHandlers();
        addSudokuHandlers();
        addAresHandlers();
        addUnityHandlers();
        addMobileAppHandlers();
        addVREngageHandlers();
        addHAVENHandlers();
        addRIDEHandlers();
        addUnityStandaloneHandlers();
        addExampleAppHandlers();
        
        addLTIHandlers();
        
        addCourseTreeHandlers();
        
        
        giftWrapDialog.setGlassEnabled(true);
        giftWrapDialog.setText("GIFT Wrap is Running");
        giftWrapDialog.setWidget(new HTML("<p style=\"font-family: Arial; font-size: 15px; margin: 6px;\">"
                + "Please close the GIFT Wrap window to continue editing this course.<br/><br/>If you leave this page, "
                + "changes made in GIFT Wrap will not be saved.</p>"));
        
        
        editCourseView.getHeaderView().setPropertiesConceptsClickHandler(new ClickHandler(){
            
            @Override
            public void onClick(ClickEvent event) {
                
                //load and show the course's concepts when the appropriate button is pressed
                showCourseConceptsEditor();
            }
        });
        
        editCourseView.setPropertiesDialogClosedCommand(new Command() {
            
            @Override
            public void execute() {
                
                //the course concepts may have changed, so notify the listening editor, if there is one
                notifyListenerEditorWhenConceptsChange();
                
                populateHeaderView();
            }
        });
        
        Window.addWindowClosingHandler(new ClosingHandler() {
            
            @Override
            public void onWindowClosing(ClosingEvent event) {
                logger.info("CoursePresenter close handler fired, calling stop()");
                stop();
            }
        });
        
    }
    
    public void addWebpageHandlers(){
        
        editCourseView.getAddWebAddressButton().addDomHandler(new DragStartHandler() {

            @Override
            public void onDragStart(DragStartEvent event) {
                
                //Prevent drag events when the course is read only
                if(readOnly) {
                    event.preventDefault();
                    return;
                }
                
                event.setData("text", "anything"); //need to add some drag data for dragging to work in Firefox
                
                LessonMaterial lm = new LessonMaterial();
                LessonMaterialList lmList = new LessonMaterialList();
                Media media = new Media();
                
                media.setMediaTypeProperties(new WebpageProperties());
                media.setUri("http://www.example.com");
                lmList.getMedia().add(media);
                lmList.setIsCollection(BooleanEnum.FALSE);
                lm.setLessonMaterialList(lmList);
                
                transitionBeingDragged = lm;
                
                //scroll the course tree as the user drags this object
                editCourseView.getCourseTree().startDragScrolling();
            }
            
        }, DragStartEvent.getType());
        
        editCourseView.getAddWebAddressButton().addDomHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent arg0) {
                
                editCourseView.getHelpHTML().setHTML(CourseHelp.INFORMATION_FROM_WEB_COURSE_OBJECT_HELP);
                
                arg0.stopPropagation();
            }
        }, ClickEvent.getType());
    
        editCourseView.getAddWebAddressButton().addDomHandler(new DragEndHandler() {

            @Override
            public void onDragEnd(DragEndEvent event) {
                
                //stop scrolling the course tree as the user drags this object
                editCourseView.getCourseTree().endDragScrolling();
            }
            
        }, DragEndEvent.getType());
    }
    
    public void addLocalWebpageHandlers(){
        
        editCourseView.getAddLocalWebpageButton().addDomHandler(new DragStartHandler() {

            @Override
            public void onDragStart(DragStartEvent event) {
                
                //Prevent drag events when the course is read only
                if(readOnly) {
                    event.preventDefault();
                    return;
                }
                
                event.setData("text", "anything"); //need to add some drag data for dragging to work in Firefox
                
                LessonMaterial lm = new LessonMaterial();
                LessonMaterialList lmList = new LessonMaterialList();
                Media media = new Media();
                
                media.setMediaTypeProperties(new WebpageProperties());
                lmList.getMedia().add(media);
                lmList.setIsCollection(BooleanEnum.FALSE);
                lm.setLessonMaterialList(lmList);
                
                transitionBeingDragged = lm;
            }
            
        }, DragStartEvent.getType());
        
        editCourseView.getAddLocalWebpageButton().addDomHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent arg0) {
                
                editCourseView.getHelpHTML().setHTML(CourseHelp.INFORMATION_FROM_FILE_COURSE_OBJECT_HELP);
                
                arg0.stopPropagation();
            }
        }, ClickEvent.getType());
    
        editCourseView.getAddLocalWebpageButton().addDomHandler(new DragEndHandler() {

            @Override
            public void onDragEnd(DragEndEvent event) {
                
                //stop scrolling the course tree as the user drags this object
                editCourseView.getCourseTree().endDragScrolling();
            }
            
        }, DragEndEvent.getType());
    }
    
    public void addStructuredReviewHandlers(){
        
        editCourseView.getAddAARButton().addDomHandler(new DragStartHandler() {

            @Override
            public void onDragStart(DragStartEvent event) {
                
                //Prevent drag events when the course is read only
                if(readOnly) {
                    event.preventDefault();
                    return;
                }
                
                event.setData("text", "anything"); //need to add some drag data for dragging to work in Firefox
                
                AAR aar = new AAR();
                
                transitionBeingDragged = aar;
                
                //scroll the course tree as the user drags this object
                editCourseView.getCourseTree().startDragScrolling();
            }
            
        }, DragStartEvent.getType());
        
        editCourseView.getAddAARButton().addDomHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent arg0) {

                editCourseView.getHelpHTML().setHTML(CourseHelp.STRUCTURED_REVIEW_COURSE_OBJECT_HELP);
                
                //prevent the CourseViewImpl RootPanel from clearing the help text with its click handler
                arg0.stopPropagation();
            }
        }, ClickEvent.getType());

        editCourseView.getAddAARButton().addDomHandler(new DragEndHandler() {

            @Override
            public void onDragEnd(DragEndEvent event) {
                
                //stop scrolling the course tree as the user drags this object
                editCourseView.getCourseTree().endDragScrolling();
            }
            
        }, DragEndEvent.getType());

    }
    
    /**
     * Adds the event handlers needed to allow the authored branch course object to be interacted with
     * and dragged onto the course tree.
     */
    private void addAuthoredBranchHandlers(){
        
        editCourseView.getAddAuthoredBranchButton().addDomHandler(new DragStartHandler() {

            @Override
            public void onDragStart(DragStartEvent event) {
                
                //Prevent drag events when the course is read only
                if(readOnly) {
                    event.preventDefault();
                    return;
                }
                
                event.setData("text", "anything"); //need to add some drag data for dragging to work in Firefox
                
                AuthoredBranch authoredBranch = GatClientUtility.generateNewAuthoredBranch(currentCourse);
                
                transitionBeingDragged = authoredBranch;
                
                //scroll the course tree as the user drags this object
                editCourseView.getCourseTree().startDragScrolling();
            }
            
        }, DragStartEvent.getType());
        
        editCourseView.getAddAuthoredBranchButton().addDomHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent arg0) {

                editCourseView.getHelpHTML().setHTML(CourseHelp.AUTHORED_BRANCH_COURSE_OBJECT_HELP);
                
                //prevent the CourseViewImpl RootPanel from clearing the help text with its click handler
                arg0.stopPropagation();
            }
        }, ClickEvent.getType());

        editCourseView.getAddAuthoredBranchButton().addDomHandler(new DragEndHandler() {

            @Override
            public void onDragEnd(DragEndEvent event) {
                
                //stop scrolling the course tree as the user drags this object
                editCourseView.getCourseTree().endDragScrolling();
            }
            
        }, DragEndEvent.getType());

        //update the cursor to indicate that course objects can be dragged into course tree nodes
//        editCourseView.getAddAuthoredBranchButton().setSVGDragOverFunction(new DatumFunction<Void>() {
//            
//            @Override
//            public Void apply(Element context, Value d, int index) {
//                
//                //by default, the browser prevents dropping elements into other elements, so we need to disable this
//                D3.event().preventDefault();
//                
//                return null;
//            }
//        });

    }
    
    public void addMediaCollectionHandlers(){
        
        editCourseView.getAddLessonMaterialButton().addDomHandler(new DragStartHandler() {

            @Override
            public void onDragStart(DragStartEvent event) {
                
                //Prevent drag events when the course is read only
                if(readOnly) {
                    event.preventDefault();
                    return;
                }
                
                event.setData("text", "anything"); //need to add some drag data for dragging to work in Firefox
                
                LessonMaterial lm = new LessonMaterial();
                
                transitionBeingDragged = lm;
                
                //scroll the course tree as the user drags this object
                editCourseView.getCourseTree().startDragScrolling();
            }
            
        }, DragStartEvent.getType());
        
        editCourseView.getAddLessonMaterialButton().addDomHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent arg0) {

                editCourseView.getHelpHTML().setHTML(CourseHelp.MEDIA_COLLECTION_COURSE_OBJECT_HELP);
                
                //prevent the CourseViewImpl RootPanel from clearing the help text with its click handler
                arg0.stopPropagation();
            }
        }, ClickEvent.getType());
        
        editCourseView.getAddLessonMaterialButton().addDomHandler(new DragEndHandler() {

            @Override
            public void onDragEnd(DragEndEvent event) {
                
                //stop scrolling the course tree as the user drags this object
                editCourseView.getCourseTree().endDragScrolling();
            }
            
        }, DragEndEvent.getType());

    }
    
    public void addImageHandlers(){
        
        editCourseView.getAddImageButton().addDomHandler(new DragStartHandler() {

            @Override
            public void onDragStart(DragStartEvent event) {
                
                //Prevent drag events when the course is read only
                if(readOnly) {
                    event.preventDefault();
                    return;
                }
                
                event.setData("text", "anything"); //need to add some drag data for dragging to work in Firefox
                
                LessonMaterial lm = new LessonMaterial();
                LessonMaterialList lmList = new LessonMaterialList();
                Media media = new Media();
                
                media.setMediaTypeProperties(new ImageProperties());
                lmList.getMedia().add(media);
                lmList.setIsCollection(BooleanEnum.FALSE);
                lm.setLessonMaterialList(lmList);
                
                transitionBeingDragged = lm;
            }
            
        }, DragStartEvent.getType());
        
        editCourseView.getAddImageButton().addDomHandler(new DragEndHandler() {

            @Override
            public void onDragEnd(DragEndEvent event) {
                
                //stop scrolling the course tree as the user drags this object
                editCourseView.getCourseTree().endDragScrolling();
            }
            
        }, DragEndEvent.getType());
        
        editCourseView.getAddImageButton().addDomHandler(new ClickHandler() {
                
                @Override
                public void onClick(ClickEvent arg0) {
                    
                    editCourseView.getHelpHTML().setHTML(CourseHelp.IMAGE_COURSE_OBJECT_HELP);
                    
                    arg0.stopPropagation();
                }
            }, ClickEvent.getType());
    }
    
    public void addGuidanceHandlers(){
        
           //enable dragging course objects
        editCourseView.getAddGuidanceButton().addDomHandler(new DragStartHandler() {

            @Override
            public void onDragStart(DragStartEvent event) {
                
                //Prevent drag events when the course is read only
                if(readOnly) {
                    event.preventDefault();
                    return;
                }
                
                event.setData("text", "anything"); //need to add some drag data for dragging to work in Firefox
                
                Message message = new Message();
                message.setContent("Enter your message here!");
                
                Guidance guidance = new Guidance();
                guidance.setGuidanceChoice(message);
                
                transitionBeingDragged = guidance;
                
                //scroll the course tree as the user drags this object
                editCourseView.getCourseTree().startDragScrolling();
            }
            
        }, DragStartEvent.getType());
        
        editCourseView.getAddGuidanceButton().addDomHandler(new DragEndHandler() {

            @Override
            public void onDragEnd(DragEndEvent event) {
                
                //stop scrolling the course tree as the user drags this object
                editCourseView.getCourseTree().endDragScrolling();
            }
            
        }, DragEndEvent.getType());
        
        editCourseView.getAddGuidanceButton().addDomHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent arg0) {

                editCourseView.getHelpHTML().setHTML(CourseHelp.INFORMATION_AS_TEXT_COURSE_OBJECT_HELP);
                
                //prevent the CourseViewImpl RootPanel from clearing the help text with its click handler
                arg0.stopPropagation();
            }
        }, ClickEvent.getType());
    }
    
    /**
     * Adds the drag and click handlers for the LTI course object
     */
    public void addLTIHandlers() {

        // enable dragging course objects
        editCourseView.getAddLTIButton().addDomHandler(new DragStartHandler() {

            @Override
            public void onDragStart(DragStartEvent event) {

                // Prevent drag events when the course is read only
                if (readOnly) {
                    event.preventDefault();
                    return;
                }
                
                event.setData("text", "anything"); //need to add some drag data for dragging to work in Firefox
                
                LessonMaterial lm = new LessonMaterial();
                LessonMaterialList lmList = new LessonMaterialList();
                Media media = new Media();
                
                media.setMediaTypeProperties(new LtiProperties());
                lmList.getMedia().add(media);
                lmList.setIsCollection(BooleanEnum.FALSE);
                lm.setLessonMaterialList(lmList);
                
                transitionBeingDragged = lm;
                
                //scroll the course tree as the user drags this object
                editCourseView.getCourseTree().startDragScrolling();
            }

        }, DragStartEvent.getType());

        editCourseView.getAddLTIButton().addDomHandler(new DragEndHandler() {

            @Override
            public void onDragEnd(DragEndEvent event) {

                // stop scrolling the course tree as the user drags this object
                editCourseView.getCourseTree().endDragScrolling();
            }

        }, DragEndEvent.getType());

        editCourseView.getAddLTIButton().addDomHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {

                editCourseView.getHelpHTML().setHTML(CourseHelp.LTI_COURSE_OBJECT_HELP);

                // prevent the CourseViewImpl RootPanel from clearing the help text with its click
                // handler
                arg0.stopPropagation();
            }
        }, ClickEvent.getType());
    }
    
    public void addPDFHandlers(){
        
        editCourseView.getAddPDFButton().addDomHandler(new DragStartHandler() {

            @Override
            public void onDragStart(DragStartEvent event) {
                
                //Prevent drag events when the course is read only
                if(readOnly) {
                    event.preventDefault();
                    return;
                }
                
                event.setData("text", "anything"); //need to add some drag data for dragging to work in Firefox
                
                LessonMaterial lm = new LessonMaterial();
                LessonMaterialList lmList = new LessonMaterialList();
                Media media = new Media();
                
                media.setMediaTypeProperties(new PDFProperties());
                lmList.getMedia().add(media);
                lmList.setIsCollection(BooleanEnum.FALSE);
                lm.setLessonMaterialList(lmList);
                
                transitionBeingDragged = lm;
                
                //scroll the course tree as the user drags this object
                editCourseView.getCourseTree().startDragScrolling();
            }
            
        }, DragStartEvent.getType());
        
        editCourseView.getAddPDFButton().addDomHandler(new DragEndHandler() {

            @Override
            public void onDragEnd(DragEndEvent event) {
                
                //stop scrolling the course tree as the user drags this object
                editCourseView.getCourseTree().endDragScrolling();
            }
            
        }, DragEndEvent.getType());
        
        editCourseView.getAddPDFButton().addDomHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent arg0) {

                editCourseView.getHelpHTML().setHTML(CourseHelp.PDF_COURSE_OBJECT_HELP);
                
                arg0.stopPropagation();
            }
        }, ClickEvent.getType());
    }
    
    public void addSlideshowHandlers(){
        
        editCourseView.getAddSlideShowButton().addDomHandler(new DragStartHandler() {

            @Override
            public void onDragStart(DragStartEvent event) {
                
                //Prevent drag events when the course is read only
                if(readOnly) {
                    event.preventDefault();
                    return;
                }
                
                event.setData("text", "anything"); //need to add some drag data for dragging to work in Firefox
                
                LessonMaterial lm = new LessonMaterial();
                LessonMaterialList lmList = new LessonMaterialList();
                Media media = new Media();
                
                media.setMediaTypeProperties(new SlideShowProperties());
                lmList.getMedia().add(media);
                lmList.setIsCollection(BooleanEnum.FALSE);
                lm.setLessonMaterialList(lmList);
                
                transitionBeingDragged = lm;
                
                //scroll the course tree as the user drags this object
                editCourseView.getCourseTree().startDragScrolling();
            }
            
        }, DragStartEvent.getType());
        
        editCourseView.getAddSlideShowButton().addDomHandler(new DragEndHandler() {

            @Override
            public void onDragEnd(DragEndEvent event) {
                
                //stop scrolling the course tree as the user drags this object
                editCourseView.getCourseTree().endDragScrolling();
            }
            
        }, DragEndEvent.getType());
        
        editCourseView.getAddSlideShowButton().addDomHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent arg0) {

                editCourseView.getHelpHTML().setHTML(CourseHelp.SLIDE_SHOW_COURSE_OBJECT_HELP);
                
                arg0.stopPropagation();
            }
        }, ClickEvent.getType());
    }
    
    public void addVideoHandlers(){
        
        editCourseView.getAddVideoButton().addDomHandler(new DragStartHandler() {

            @Override
            public void onDragStart(DragStartEvent event) {
                
                //Prevent drag events when the course is read only
                if(readOnly) {
                    event.preventDefault();
                    return;
                }
                
                event.setData("text", "anything"); //need to add some drag data for dragging to work in Firefox
                
                LessonMaterial lm = new LessonMaterial();
                LessonMaterialList lmList = new LessonMaterialList();
                Media media = new Media();
                
                media.setMediaTypeProperties(new VideoProperties());
                lmList.getMedia().add(media);
                lmList.setIsCollection(BooleanEnum.FALSE);
                lm.setLessonMaterialList(lmList);
                
                transitionBeingDragged = lm;
            }
            
        }, DragStartEvent.getType());
        
        editCourseView.getAddVideoButton().addDomHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent arg0) {
                
                editCourseView.getHelpHTML().setHTML(CourseHelp.VIDEO_COURSE_OBJECT_HELP);
                
                arg0.stopPropagation();
            }
        }, ClickEvent.getType());
    
        editCourseView.getAddVideoButton().addDomHandler(new DragEndHandler() {

            @Override
            public void onDragEnd(DragEndEvent event) {
                
                //stop scrolling the course tree as the user drags this object
                editCourseView.getCourseTree().endDragScrolling();
            }
            
        }, DragEndEvent.getType());
    }
    
    public void addYoutubeVideoHandlers(){
        
        editCourseView.getAddYoutubeVideoButton().addDomHandler(new DragStartHandler() {

            @Override
            public void onDragStart(DragStartEvent event) {
                
                //Prevent drag events when the course is read only
                if(readOnly) {
                    event.preventDefault();
                    return;
                }
                
                event.setData("text", "anything"); //need to add some drag data for dragging to work in Firefox
                
                LessonMaterial lm = new LessonMaterial();
                LessonMaterialList lmList = new LessonMaterialList();
                Media media = new Media();
                
                media.setMediaTypeProperties(new YoutubeVideoProperties());
                lmList.getMedia().add(media);
                lmList.setIsCollection(BooleanEnum.FALSE);
                lm.setLessonMaterialList(lmList);
                
                transitionBeingDragged = lm;
            }
            
        }, DragStartEvent.getType());
        
        editCourseView.getAddYoutubeVideoButton().addDomHandler(new DragEndHandler() {

            @Override
            public void onDragEnd(DragEndEvent event) {
                
                //stop scrolling the course tree as the user drags this object
                editCourseView.getCourseTree().endDragScrolling();
            }
            
        }, DragEndEvent.getType());
        
        editCourseView.getAddYoutubeVideoButton().addDomHandler(new ClickHandler() {
                
                @Override
                public void onClick(ClickEvent arg0) {

                    editCourseView.getHelpHTML().setHTML(CourseHelp.YOUTUBE_COURSE_OBJECT_HELP);
                    
                    arg0.stopPropagation();
                }
            }, ClickEvent.getType());
    }
    
    public void addAdaptiveCourseflowHandlers(){
        
        editCourseView.getAddMBPButton().addDomHandler(new DragStartHandler() {

            @Override
            public void onDragStart(DragStartEvent event) {
                
                //Prevent drag events when the course is read only
                if(readOnly) {
                    event.preventDefault();
                    return;
                }
                
                event.setData("text", "anything"); //need to add some drag data for dragging to work in Firefox
                
                MerrillsBranchPoint mbp = new MerrillsBranchPoint();   
                
                transitionBeingDragged = mbp;
                
                //scroll the course tree as the user drags this object
                editCourseView.getCourseTree().startDragScrolling();
            }
            
        }, DragStartEvent.getType());
        
        editCourseView.getAddMBPButton().addDomHandler(new DragEndHandler() {

            @Override
            public void onDragEnd(DragEndEvent event) {
                
                //stop scrolling the course tree as the user drags this object
                editCourseView.getCourseTree().endDragScrolling();
            }
            
        }, DragEndEvent.getType());
        
        editCourseView.getAddMBPButton().addDomHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent arg0) {

                editCourseView.getHelpHTML().setHTML(CourseHelp.ADAPTIVE_COURSE_OBJECT_HELP);
                
                //prevent the CourseViewImpl RootPanel from clearing the help text with its click handler
                arg0.stopPropagation();
            }
        }, ClickEvent.getType());
    }
    
    public void addAutoTutorHandlers(){
        
        editCourseView.getAddAutoTutorButton().addDomHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent arg0) {

                editCourseView.getHelpHTML().setHTML(CourseHelp.AUTOTUTOR_COURSE_OBJECT_HELP);
                
                //prevent the CourseViewImpl RootPanel from clearing the help text with its click handler
                arg0.stopPropagation();
            }
        }, ClickEvent.getType());
        
        editCourseView.getAddAutoTutorButton().addDomHandler(new DragStartHandler() {

            @Override
            public void onDragStart(DragStartEvent event) {
                            
                //Prevent drag events when the course is read only
                if(readOnly) {
                    event.preventDefault();
                    return;
                }
                
                event.setData("text", "anything"); //need to add some drag data for dragging to work in Firefox
        
                PresentSurvey presentSurvey = new PresentSurvey();
                
                Conversation conversation = new Conversation();
                conversation.setType(new AutoTutorSession());
                
                presentSurvey.setSurveyChoice(conversation);

                transitionBeingDragged = presentSurvey;
                
                //scroll the course tree as the user drags this object
                editCourseView.getCourseTree().startDragScrolling();
            }
            
        }, DragStartEvent.getType());
        
        editCourseView.getAddAutoTutorButton().addDomHandler(new DragEndHandler() {

            @Override
            public void onDragEnd(DragEndEvent event) {
                
                //stop scrolling the course tree as the user drags this object
                editCourseView.getCourseTree().endDragScrolling();
            }
            
        }, DragEndEvent.getType());
    }
    
    public void addConversationTreeHandlers(){
        
        editCourseView.getAddConversationTreeButton().addDomHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent arg0) {

                editCourseView.getHelpHTML().setHTML(CourseHelp.CONVERSATION_TREE_COURSE_OBJECT_HELP);
                
                //prevent the CourseViewImpl RootPanel from clearing the help text with its click handler
                arg0.stopPropagation();
            }
        }, ClickEvent.getType());
        
        editCourseView.getAddConversationTreeButton().addDomHandler(new DragStartHandler() {

            @Override
            public void onDragStart(DragStartEvent event) {
                            
                //Prevent drag events when the course is read only
                if(readOnly) {
                    event.preventDefault();
                    return;
                }
                
                event.setData("text", "anything"); //need to add some drag data for dragging to work in Firefox
        
                PresentSurvey presentSurvey = new PresentSurvey();
                
                Conversation conversation = new Conversation();
                conversation.setType(new ConversationTreeFile());
                
                presentSurvey.setSurveyChoice(conversation);

                transitionBeingDragged = presentSurvey;
                
                //scroll the course tree as the user drags this object
                editCourseView.getCourseTree().startDragScrolling();
            }
            
        }, DragStartEvent.getType());
        
        editCourseView.getAddConversationTreeButton().addDomHandler(new DragEndHandler() {

            @Override
            public void onDragEnd(DragEndEvent event) {
                
                //stop scrolling the course tree as the user drags this object
                editCourseView.getCourseTree().endDragScrolling();
            }
            
        }, DragEndEvent.getType());
    }
    
    public void addQuestionBankHandlers(){
     
        editCourseView.getAddQuestionBankButton().addDomHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent arg0) {

                editCourseView.getHelpHTML().setHTML(CourseHelp.QUESTION_BANK_COURSE_OBJECT_HELP);
                
                //prevent the CourseViewImpl RootPanel from clearing the help text with its click handler
                arg0.stopPropagation();
            }
        }, ClickEvent.getType());
        
        editCourseView.getAddQuestionBankButton().addDomHandler(new DragStartHandler() {

            @Override
            public void onDragStart(DragStartEvent event) {
                        
                //Prevent drag events when the course is read only
                if(readOnly) {
                    event.preventDefault();
                    return;
                }
                
                event.setData("text", "anything"); //need to add some drag data for dragging to work in Firefox
        
                PresentSurvey presentSurvey = new PresentSurvey();
                presentSurvey.setSurveyChoice(new ConceptSurvey());

                transitionBeingDragged = presentSurvey;
                
                //scroll the course tree as the user drags this object
                editCourseView.getCourseTree().startDragScrolling();
            }
            
        }, DragStartEvent.getType());
        
        editCourseView.getAddQuestionBankButton().addDomHandler(new DragEndHandler() {

            @Override
            public void onDragEnd(DragEndEvent event) {
                
                //stop scrolling the course tree as the user drags this object
                editCourseView.getCourseTree().endDragScrolling();
            }
            
        }, DragEndEvent.getType());
    }
    
    public void addSurveyHandlers(){
        
        editCourseView.getAddSurveyButton().addDomHandler(new DragStartHandler() {

            @Override
            public void onDragStart(DragStartEvent event) {
            
                //Prevent drag events when the course is read only
                if(readOnly) {
                    event.preventDefault();
                    return;
                }
                
                event.setData("text", "anything"); //need to add some drag data for dragging to work in Firefox
        
                PresentSurvey presentSurvey = new PresentSurvey();

                transitionBeingDragged = presentSurvey;
                
                //scroll the course tree as the user drags this object
                editCourseView.getCourseTree().startDragScrolling();
            }
            
        }, DragStartEvent.getType());
        
        editCourseView.getAddSurveyButton().addDomHandler(new DragEndHandler() {

            @Override
            public void onDragEnd(DragEndEvent event) {
                
                //stop scrolling the course tree as the user drags this object
                editCourseView.getCourseTree().endDragScrolling();
            }
            
        }, DragEndEvent.getType());
        
        editCourseView.getAddSurveyButton().addDomHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent arg0) {

                editCourseView.getHelpHTML().setHTML(CourseHelp.SURVEY_COURSE_OBJECT_HELP);
                
                //prevent the CourseViewImpl RootPanel from clearing the help text with its click handler
                arg0.stopPropagation();
            }
        }, ClickEvent.getType());
    }
    
    public void addPowerPointHandlers(){
        
        editCourseView.getAddTAButton().addDomHandler(new DragStartHandler() {
            
            @Override
            public void onDragStart(DragStartEvent event) {
                
                //Prevent drag events when the course is read only
                if(readOnly) {
                    event.preventDefault();
                    return;
                }
                
                event.setData("text", "anything"); //need to add some drag data for dragging to work in Firefox
                
                transitionBeingDragged = generateTrainingApp(TrainingApplicationEnum.POWERPOINT);
                
                //scroll the course tree as the user drags this object
                editCourseView.getCourseTree().startDragScrolling();
            }

        }, DragStartEvent.getType());
                
        editCourseView.getAddTAButton().addDomHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent arg0) {

                editCourseView.getHelpHTML().setHTML(CourseHelp.POWERPOINT_COURSE_OBJECT_HELP);
                
                //prevent the CourseViewImpl RootPanel from clearing the help text with its click handler
                arg0.stopPropagation();
            }
        }, ClickEvent.getType());
        
        
        editCourseView.getAddTAButton().addDomHandler(new DragEndHandler() {

            @Override
            public void onDragEnd(DragEndEvent event) {
                
                //stop scrolling the course tree as the user drags this object
                editCourseView.getCourseTree().endDragScrolling();
            }
            
        }, DragEndEvent.getType());
    }
    
    public void addVBSHandlers(){
        
        editCourseView.getAddVbsButton().addDomHandler(new DragStartHandler() {
            
            @Override
            public void onDragStart(DragStartEvent event) {
                
                //Prevent drag events when the course is read only
                if(readOnly) {
                    event.preventDefault();
                    return;
                }
                
                event.setData("text", "anything"); //need to add some drag data for dragging to work in Firefox     
                
                transitionBeingDragged = generateTrainingApp(TrainingApplicationEnum.VBS);
                
                //scroll the course tree as the user drags this object
                editCourseView.getCourseTree().startDragScrolling();
            }
        
        }, DragStartEvent.getType());
            
        editCourseView.getAddVbsButton().addDomHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent arg0) {

                editCourseView.getHelpHTML().setHTML(CourseHelp.VBS_COURSE_OBJECT_HELP);
                
                //prevent the CourseViewImpl RootPanel from clearing the help text with its click handler
                arg0.stopPropagation();
            }
        }, ClickEvent.getType());
        
        
        editCourseView.getAddVbsButton().addDomHandler(new DragEndHandler() {

            @Override
            public void onDragEnd(DragEndEvent event) {
                
                //stop scrolling the course tree as the user drags this object
                editCourseView.getCourseTree().endDragScrolling();
            }
            
        }, DragEndEvent.getType());
    }
    
    public void addTC3Handlers(){
        
        editCourseView.getAddTc3Button().addDomHandler(new DragStartHandler() {
            
            @Override
            public void onDragStart(DragStartEvent event) {
                
                //Prevent drag events when the course is read only
                if(readOnly) {
                    event.preventDefault();
                    return;
                }
                
                event.setData("text", "anything"); //need to add some drag data for dragging to work in Firefox     
                
                transitionBeingDragged = generateTrainingApp(TrainingApplicationEnum.TC3);
                
                //scroll the course tree as the user drags this object
                editCourseView.getCourseTree().startDragScrolling();
            }
        
        }, DragStartEvent.getType());
            
        editCourseView.getAddTc3Button().addDomHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent arg0) {

                editCourseView.getHelpHTML().setHTML(CourseHelp.TC3_COURSE_OBJECT_HELP);
                
                //prevent the CourseViewImpl RootPanel from clearing the help text with its click handler
                arg0.stopPropagation();
            }
        }, ClickEvent.getType());
        
        editCourseView.getAddTc3Button().addDomHandler(new DragEndHandler() {

            @Override
            public void onDragEnd(DragEndEvent event) {
                
                //stop scrolling the course tree as the user drags this object
                editCourseView.getCourseTree().endDragScrolling();
            }
            
        }, DragEndEvent.getType());
    }
    
    private void addTestbedHandlers(){
        
        editCourseView.getAddTestbedButton().addDomHandler(new DragStartHandler() {
            
            @Override
            public void onDragStart(DragStartEvent event) {
                
                //Prevent drag events when the course is read only
                if(readOnly) {
                    event.preventDefault();
                    return;
                }
                
                event.setData("text", "anything"); //need to add some drag data for dragging to work in Firefox     
                
                transitionBeingDragged = generateTrainingApp(TrainingApplicationEnum.DE_TESTBED);
                
                //scroll the course tree as the user drags this object
                editCourseView.getCourseTree().startDragScrolling();
            }
        
        }, DragStartEvent.getType());
            
        editCourseView.getAddTestbedButton().addDomHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent arg0) {

                editCourseView.getHelpHTML().setHTML(CourseHelp.DE_TESTBED_COURSE_OBJECT_HELP);
                
                //prevent the CourseViewImpl RootPanel from clearing the help text with its click handler
                arg0.stopPropagation();
            }
        }, ClickEvent.getType());
        
        editCourseView.getAddTestbedButton().addDomHandler(new DragEndHandler() {

            @Override
            public void onDragEnd(DragEndEvent event) {
                
                //stop scrolling the course tree as the user drags this object
                editCourseView.getCourseTree().endDragScrolling();
            }
            
        }, DragEndEvent.getType());
    }
    
    private void addSudokuHandlers(){
        
//          editCourseView.getAddSudokuButton().addDomHandler(new DragStartHandler() {
//              
//              @Override
//              public void onDragStart(DragStartEvent event) {
//                  
//                  event.setData("text", "anything"); //need to add some drag data for dragging to work in Firefox     
//                  
//                  transitionBeingDragged = generateTrainingApp(TrainingApplicationEnum.SUDOKU);
//              }
//          
//          }, DragStartEvent.getType());
//              
//            editCourseView.getAddSudokuButton().addDomHandler(new ClickHandler() {
//                
//                @Override
//                public void onClick(ClickEvent arg0) {
//
//                    editCourseView.getHelpHTML().setHTML(CourseHelp.EXTERNAL_APP_COURSE_OBJECT_HELP);
//                    
//                    //prevent the CourseViewImpl RootPanel from clearing the help text with its click handler
//                    arg0.stopPropagation();
//                }
//            }, ClickEvent.getType());
    }
    
    private void addAresHandlers(){
        
        editCourseView.getAddAresButton().addDomHandler(new DragStartHandler() {
            
            @Override
            public void onDragStart(DragStartEvent event) {
                
                //Prevent drag events when the course is read only
                if(readOnly) {
                    event.preventDefault();
                    return;
                }
                
                event.setData("text", "anything"); //need to add some drag data for dragging to work in Firefox     
                
                transitionBeingDragged = generateTrainingApp(TrainingApplicationEnum.ARES);
                
                //scroll the course tree as the user drags this object
                editCourseView.getCourseTree().startDragScrolling();
            }
        
        }, DragStartEvent.getType());
            
        editCourseView.getAddAresButton().addDomHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent arg0) {

                editCourseView.getHelpHTML().setHTML(CourseHelp.ARES_COURSE_OBJECT_HELP);
                
                //prevent the CourseViewImpl RootPanel from clearing the help text with its click handler
                arg0.stopPropagation();
            }
        }, ClickEvent.getType());
        
        editCourseView.getAddAresButton().addDomHandler(new DragEndHandler() {

            @Override
            public void onDragEnd(DragEndEvent event) {
                
                //stop scrolling the course tree as the user drags this object
                editCourseView.getCourseTree().endDragScrolling();
            }
            
        }, DragEndEvent.getType());
    }
    
    private void addUnityHandlers() {
        editCourseView.getAddUnityButton().addDomHandler(new DragStartHandler() {

            @Override
            public void onDragStart(DragStartEvent event) {
                
                //Prevent drag events when the course is read only
                if(readOnly) {
                    event.preventDefault();
                    return;
                }
                
                event.setData("text", "anything"); //need to add some drag data for dragging to work in Firefox 
                
                TrainingApplication trainingApp = new TrainingApplication();
                
                EmbeddedApps embeddedApps = new EmbeddedApps();
                EmbeddedApp embeddedApp = new EmbeddedApp();
                embeddedApps.setEmbeddedApp(embeddedApp);
                
                trainingApp.setEmbeddedApps(embeddedApps);
                
                transitionBeingDragged = trainingApp;
                
                //scroll the course tree as the user drags this object
                editCourseView.getCourseTree().startDragScrolling();
            }
            
        }, DragStartEvent.getType());
        
        editCourseView.getAddUnityButton().addDomHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent arg0) {
                editCourseView.getHelpHTML().setHTML(CourseHelp.UNITY_COURSE_OBJECT_HELP);
                
                //prevent the CourseViewImpl RootPanel from clearing the help text with its click handler
                arg0.stopPropagation();
            }
            
        }, ClickEvent.getType());
        
        editCourseView.getAddUnityButton().addDomHandler(new DragEndHandler() {

            @Override
            public void onDragEnd(DragEndEvent arg0) {
                //stop scrolling the course tree as the user drags this object
                editCourseView.getCourseTree().endDragScrolling();
            }
            
        }, DragEndEvent.getType());
    }
    
    private void addMobileAppHandlers() {
        editCourseView.getAddMobileAppButton().addDomHandler(new DragStartHandler() {

            @Override
            public void onDragStart(DragStartEvent event) {
                
                //Prevent drag events when the course is read only
                if(readOnly) {
                    event.preventDefault();
                    return;
                }
                
                event.setData("text", "anything"); //need to add some drag data for dragging to work in Firefox 
                
                TrainingApplication trainingApp = new TrainingApplication();
                
                EmbeddedApps embeddedApps = new EmbeddedApps();
                EmbeddedApp embeddedApp = new EmbeddedApp();
                MobileApp mobileApp = new MobileApp();
                
                embeddedApp.setEmbeddedAppImpl(mobileApp);
                embeddedApps.setEmbeddedApp(embeddedApp);
                
                trainingApp.setEmbeddedApps(embeddedApps);
                
                transitionBeingDragged = trainingApp;
                
                //scroll the course tree as the user drags this object
                editCourseView.getCourseTree().startDragScrolling();
            }
            
        }, DragStartEvent.getType());
        
        editCourseView.getAddMobileAppButton().addDomHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent arg0) {
                editCourseView.getHelpHTML().setHTML(CourseHelp.MOBILE_APP_COURSE_OBJECT_HELP);
                
                //prevent the CourseViewImpl RootPanel from clearing the help text with its click handler
                arg0.stopPropagation();
            }
            
        }, ClickEvent.getType());
        
        editCourseView.getAddMobileAppButton().addDomHandler(new DragEndHandler() {

            @Override
            public void onDragEnd(DragEndEvent arg0) {
                //stop scrolling the course tree as the user drags this object
                editCourseView.getCourseTree().endDragScrolling();
            }
            
        }, DragEndEvent.getType());
    }
    
    /**
     * Adds the UI handlers needed to allow the user to create VR-Engage course objects
     */
    private void addVREngageHandlers() {
        editCourseView.getAddVREngageButton().addDomHandler(new DragStartHandler() {

            @Override
            public void onDragStart(DragStartEvent event) {
                
                //Prevent drag events when the course is read only
                if(readOnly) {
                    event.preventDefault();
                    return;
                }
                
                event.setData("text", "anything"); //need to add some drag data for dragging to work in Firefox 
                
                transitionBeingDragged = generateTrainingApp(TrainingApplicationEnum.VR_ENGAGE);
                
                //scroll the course tree as the user drags this object
                editCourseView.getCourseTree().startDragScrolling();
            }
            
        }, DragStartEvent.getType());
        
        editCourseView.getAddVREngageButton().addDomHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent arg0) {
                editCourseView.getHelpHTML().setHTML(CourseHelp.VR_ENGAGE_COURSE_OBJECT_HELP);
                
                //prevent the CourseViewImpl RootPanel from clearing the help text with its click handler
                arg0.stopPropagation();
            }
            
        }, ClickEvent.getType());
        
        editCourseView.getAddVREngageButton().addDomHandler(new DragEndHandler() {

            @Override
            public void onDragEnd(DragEndEvent arg0) {
                //stop scrolling the course tree as the user drags this object
                editCourseView.getCourseTree().endDragScrolling();
            }
            
        }, DragEndEvent.getType());
    }
    
    /**
     * Adds the UI handlers needed to allow the user to create HAVEN course objects
     */
    private void addHAVENHandlers() {
        editCourseView.getAddHAVENButton().addDomHandler(new DragStartHandler() {

            @Override
            public void onDragStart(DragStartEvent event) {
                
                //Prevent drag events when the course is read only
                if(readOnly) {
                    event.preventDefault();
                    return;
                }
                
                event.setData("text", "anything"); //need to add some drag data for dragging to work in Firefox 
                
                transitionBeingDragged = generateTrainingApp(TrainingApplicationEnum.HAVEN);
                
                //scroll the course tree as the user drags this object
                editCourseView.getCourseTree().startDragScrolling();
            }
            
        }, DragStartEvent.getType());
        
        editCourseView.getAddHAVENButton().addDomHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent arg0) {
                editCourseView.getHelpHTML().setHTML(CourseHelp.HAVEN_COURSE_OBJECT_HELP);
                
                //prevent the CourseViewImpl RootPanel from clearing the help text with its click handler
                arg0.stopPropagation();
            }
            
        }, ClickEvent.getType());
        
        editCourseView.getAddHAVENButton().addDomHandler(new DragEndHandler() {

            @Override
            public void onDragEnd(DragEndEvent arg0) {
                //stop scrolling the course tree as the user drags this object
                editCourseView.getCourseTree().endDragScrolling();
            }
            
        }, DragEndEvent.getType());
    }
    
    /**
     * Adds the UI handlers needed to allow the user to create RIDE course objects
     */
    private void addRIDEHandlers() {
        editCourseView.getAddRIDEButton().addDomHandler(new DragStartHandler() {

            @Override
            public void onDragStart(DragStartEvent event) {
                
                //Prevent drag events when the course is read only
                if(readOnly) {
                    event.preventDefault();
                    return;
                }
                
                event.setData("text", "anything"); //need to add some drag data for dragging to work in Firefox 
                
                transitionBeingDragged = generateTrainingApp(TrainingApplicationEnum.RIDE);
                
                //scroll the course tree as the user drags this object
                editCourseView.getCourseTree().startDragScrolling();
            }
            
        }, DragStartEvent.getType());
        
        editCourseView.getAddRIDEButton().addDomHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent arg0) {
                editCourseView.getHelpHTML().setHTML(CourseHelp.RIDE_COURSE_OBJECT_HELP);
                
                //prevent the CourseViewImpl RootPanel from clearing the help text with its click handler
                arg0.stopPropagation();
            }
            
        }, ClickEvent.getType());
        
        editCourseView.getAddRIDEButton().addDomHandler(new DragEndHandler() {

            @Override
            public void onDragEnd(DragEndEvent arg0) {
                //stop scrolling the course tree as the user drags this object
                editCourseView.getCourseTree().endDragScrolling();
            }
            
        }, DragEndEvent.getType());
    }
    
	/**
     * Adds the UI handlers needed to allow the user to create Unity Standalone course objects
     */
    private void addUnityStandaloneHandlers() {
        editCourseView.getAddUnityStandaloneButton().addDomHandler(new DragStartHandler() {

            @Override
            public void onDragStart(DragStartEvent event) {
                
                //Prevent drag events when the course is read only
                if(readOnly) {
                    event.preventDefault();
                    return;
                }
                
                event.setData("text", "anything"); //need to add some drag data for dragging to work in Firefox 
                
                transitionBeingDragged = generateTrainingApp(TrainingApplicationEnum.UNITY_DESKTOP);
                
                //scroll the course tree as the user drags this object
                editCourseView.getCourseTree().startDragScrolling();
            }
            
        }, DragStartEvent.getType());
        
        editCourseView.getAddUnityStandaloneButton().addDomHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent arg0) {
                editCourseView.getHelpHTML().setHTML(CourseHelp.UNITY_DESKTOP_COURSE_OBJECT_HELP);
                
                //prevent the CourseViewImpl RootPanel from clearing the help text with its click handler
                arg0.stopPropagation();
            }
            
        }, ClickEvent.getType());
        
        editCourseView.getAddUnityStandaloneButton().addDomHandler(new DragEndHandler() {

            @Override
            public void onDragEnd(DragEndEvent arg0) {
                //stop scrolling the course tree as the user drags this object
                editCourseView.getCourseTree().endDragScrolling();
            }
            
        }, DragEndEvent.getType());
    }
	
    private void addExampleAppHandlers(){
        
        editCourseView.getAddExampleAppButton().addDomHandler(new DragStartHandler() {
            
            @Override
            public void onDragStart(DragStartEvent event) {
                
                //Prevent drag events when the course is read only
                if(readOnly) {
                    event.preventDefault();
                    return;
                }
                
                event.setData("text", "anything"); //need to add some drag data for dragging to work in Firefox     
                
                transitionBeingDragged = generateTrainingApp(TrainingApplicationEnum.SIMPLE_EXAMPLE_TA);
                
                //scroll the course tree as the user drags this object
                editCourseView.getCourseTree().startDragScrolling();
            }
        
        }, DragStartEvent.getType());
            
        editCourseView.getAddExampleAppButton().addDomHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent arg0) {

                editCourseView.getHelpHTML().setHTML(CourseHelp.EXAMPLE_APP_COURSE_OBJECT_HELP);
                
                //prevent the CourseViewImpl RootPanel from clearing the help text with its click handler
                arg0.stopPropagation();
            }
        }, ClickEvent.getType());
        
        editCourseView.getAddExampleAppButton().addDomHandler(new DragEndHandler() {

            @Override
            public void onDragEnd(DragEndEvent event) {
                
                //stop scrolling the course tree as the user drags this object
                editCourseView.getCourseTree().endDragScrolling();
            }
            
        }, DragEndEvent.getType());
    }
    
    private void addCourseTreeHandlers(){
        
      //enable dragging course tree nodes
        editCourseView.getCourseTree().setNodeDragStartFunction(new DatumFunction<Void>() {
            
            @Override
            public Void apply(Element context, Value d, int index) {
                
                if(readOnly){
                    return null;
                }
                
                mil.arl.gift.tools.authoring.server.gat.client.view.course.tree.TreeNode node = 
                        d.<mil.arl.gift.tools.authoring.server.gat.client.view.course.tree.TreeNode> as();
                
                if(node != null){
                    
                TreeNodeEnum type = TreeNodeEnum.valueOf(node.getType());
                
                if(type != null){
                    
                    if(TreeNodeEnum.TRANSITION.equals(type)){
                            transitionBeingDragged = editCourseView.getCourseTree().getCourseObject(node);
                        
                        }                       
                    }
                }
                
                return null;
                } 
        });
                
        //select course tree nodes as course objects are dragged over them
        editCourseView.getCourseTree().setNodeDragEnterFunction(new DatumFunction<Void>() {
            
            @Override
            public Void apply(Element context, Value d, int index) {
                
                mil.arl.gift.tools.authoring.server.gat.client.view.course.tree.TreeNode node = 
                        d.<mil.arl.gift.tools.authoring.server.gat.client.view.course.tree.TreeNode> as();
                
                editCourseView.getCourseTree().setSelectedNode(node, true);
                
                return null;
            }
        });
        
        //update the cursor to indicate that course objects can be dragged into course tree nodes
        editCourseView.getCourseTree().setNodeDragOverFunction(new DatumFunction<Void>() {

            @Override
            public Void apply(Element context, Value d, int index) {
                    
                //by default, the browser prevents dropping elements into other elements, so we need to disable this
                D3.event().preventDefault();
                
                return null;
            }
        });
                
        //handle when the user drops a course object on top of a course tree node
        editCourseView.getCourseTree().setNodeDropFunction(new DatumFunction<Void>() {
            
            @Override
            public Void apply(Element context, Value d, int index) {
                
                D3.event().preventDefault(); //prevents Firefox from navigating away from the page when drag data is added
                
                final Serializable dragged = transitionBeingDragged;
                transitionBeingDragged = null;
                
                dragCourseObject(dragged, d);
                
                return null;
            }
        });
        
        //update the cursor to indicate that course objects can be dragged into course tree nodes
        editCourseView.getCourseTree().setSVGDragOverFunction(new DatumFunction<Void>() {
            
            @Override
            public Void apply(Element context, Value d, int index) {
                
                //by default, the browser prevents dropping elements into other elements, so we need to disable this
                D3.event().preventDefault();
                
                return null;
            }
        });
                
        //handle when the user drops a course object on top of a course tree node
        editCourseView.getCourseTree().setSVGDropFunction(new DatumFunction<Void>() {
                
            @Override
            public Void apply(Element context, Value d, int index) {
                    
                D3.event().preventDefault(); //prevents Firefox from navigating away from the page when drag data is added
                        
                final Serializable dragged = transitionBeingDragged;
                transitionBeingDragged = null;
                
                if(dragged != null){
                    dragCourseObject(dragged, d);
                }
                
                return null;
            }
        });
        
        editCourseView.getCourseTree().setDeleteNodeFunction(new DatumFunction<Void>() {

            @Override
            public Void apply(Element context, Value d, int index) {
                
                editCourseView.getEditorPanel().stopEditing(transitionBeingDragged, true, true);
                currentCourse.getTransitions().getTransitionType().remove(transitionBeingDragged);
                
                if(transitionBeingDragged instanceof AuthoredBranch) {
                    
                    /* If an authored branch is being deleted, then we need to make sure that we stop
                     * editing any course objects that are pinned inside of it */
                    AuthoredBranch currBranch = (AuthoredBranch) transitionBeingDragged;
                    
                    if(currBranch.getPaths() != null){
                        for(Path path : currBranch.getPaths().getPath()){
                            
                            if(path.getCourseobjects() != null){
                                
                                for(Serializable courseObject : path.getCourseobjects().getAAROrAuthoredBranchOrEnd()){
                                    editCourseView.getEditorPanel().stopEditing(courseObject, true, true);
                                }
                            }
                        }
                    }
                }
                
                transitionBeingDragged = null;
                saveCourse(filePath, true);
                return null;
            }
            
        });
        
    }

    /**
     * Handles when the given course object is dragged into an SVG element representing the given datum. If the dragged
     * object has not already been added to the course, this method will first prompt the user to give it a name before
     * attempting to add it. If the object HAS been added to the course, this method will move the object to the 
     * appropriate location. This method assumes the datum is a course tree node.
     * 
     * @param dragged the course object that was dragged.
     * @param d the datum corresponding to the SVG element that was dragged into
     */
    private void dragCourseObject(final Serializable dragged, Value d) {
        dragCourseObject(dragged, d, false);
    }

    /**
     * Handles when the given course object is dragged into an SVG element representing the given datum. If the dragged
     * object has not already been added to the course, this method will first prompt the user to give it a name before
     * attempting to add it. If the object HAS been added to the course, this method will move the object to the 
     * appropriate location
     * 
     * @param dragged the course object that was dragged.
     * @param d the datum corresponding to the SVG element that was dragged into
     * @param isBranch whether the datum is a branch tree node or a course tree node
     */
    private void dragCourseObject(final Serializable dragged, Value d, boolean isBranch) {
        if (isBranch) {
            final mil.arl.gift.tools.authoring.server.gat.client.view.course.authoredbranch.tree.TreeNode node = 
                    d.<mil.arl.gift.tools.authoring.server.gat.client.view.course.authoredbranch.tree.TreeNode> as();
            dragCourseObject(dragged, node);

        } else {
            final mil.arl.gift.tools.authoring.server.gat.client.view.course.tree.TreeNode node = 
                    d.<mil.arl.gift.tools.authoring.server.gat.client.view.course.tree.TreeNode> as();
            dragCourseObject(dragged, node);
        }
    }
    
    /**
     * Handles when the given course object is dragged into an SVG element representing the given datum. If the dragged
     * object has not already been added to the course, this method will first prompt the user to give it a name before
     * attempting to add it. If the object HAS been added to the course, this method will move the object to the 
     * appropriate location
     * 
     * @param dragged the course object that was dragged.
     * @param node the course tree node corresponding to the SVG element that was dragged into
     */
    private void dragCourseObject(final Serializable dragged, final mil.arl.gift.tools.authoring.server.gat.client.view.course.tree.TreeNode node) {
        
        if(GatClientUtility.isRtaLessonLevel()
                && currentCourse.getTransitions() != null
                && !currentCourse.getTransitions().getTransitionType().isEmpty()) {
            
            if (currentCourse.getTransitions().getTransitionType().size() == 1) {
                
                //if only one course object type is present, allow the author to replace it via dragging
                Serializable toReplace = currentCourse.getTransitions().getTransitionType().get(0);
                
                if(toReplace instanceof TrainingApplication) {
                    
                    //if the object being replaced is a training app, need to ask if DKF should be deleted
                    promptChangeTrainingAppIfNeeded((TrainingApplication) toReplace, dragged);
                    
                } else {
                    
                    //otherwise, just perform the replacement
                    replaceCourseObject(toReplace, dragged);
                }
                
            }
            
            //don't allow more than one course object in RTA mode
            return;
        }
        
        if(dragged != null){

           AuthoredBranch branch = editCourseView.getTreeManager().getCurrentBranch();
           
           // no branch selected
           if (branch == null) {
           
                if(currentCourse.getTransitions() == null){
                    currentCourse.setTransitions(new Transitions());
                }
                
                Serializable dropTarget = editCourseView.getCourseTree().getCourseObject(node);
                
                if(dropTarget != null && dropTarget.equals(dragged)){
                    return; //don't do anything if a transition is dragged on top of itself
                }
                
                final int startIndex = currentCourse.getTransitions().getTransitionType().indexOf(dragged);
                
                if(startIndex < 0){
                    
                    //handle when a new course object has been dragged
                    String exampleText = null;
                    
                    if(dragged instanceof Guidance){
                        
                        exampleText = "You may want to enter a name that describes what this " +CourseObjectName.GUIDANCE.getDisplayName()+ " contains "
                                + "at a glance.<br/>For example, \"Introduction\" or \"Lesson Complete\"";
                        
                    } else if(dragged instanceof PresentSurvey){
                        
                        exampleText = "You may want to enter a name that describes what this " +CourseObjectName.SURVEY.getDisplayName()+ " contains "
                                + "at a glance.<br/>For example, \"Knowledge Assessment Survey\" or \"AutoTutor Session\"";
                    
                    } else if(dragged instanceof LessonMaterial){
                        
                        exampleText = "You may want to enter a name that describes what this " +CourseObjectName.LESSON_MATERIAL.getDisplayName()+ " contains "
                                + "at a glance.<br/>For example, \"Concept Material\" or \"Pre-Lesson Resources\"";
                        
                    } else if(dragged instanceof TrainingApplication){
                        
                        exampleText = "You may want to enter a name that describes what this " +CourseObjectName.TRAINING_APPLICATION.getDisplayName()+ " contains "
                                + "at a glance.<br/>For example, \"Introduction Powerpoint\" or \"VBS Training Scenario\"";
                    }
                    
                    final String finalExampleText = exampleText;
                    
                    final ValueChangeHandler<String> nameChangeHandler = new ValueChangeHandler<String>() {

                        @Override
                        public void onValueChange(ValueChangeEvent<String> event) {
                            
                            if(event.getValue() == null || event.getValue().isEmpty()) {
                                WarningDialog.error("Invalid value", "The " +CourseElementUtil.getTypeDisplayName(dragged)+" name cannot be empty.");
                                return;
                            }else if(event.getValue().trim().isEmpty()){
                                WarningDialog.error("Invalid value", "The " +CourseElementUtil.getTypeDisplayName(dragged)+" name must contain characters other than spaces.");
                                return;
                            }
                            
                            String name = event.getValue().trim();
                            
                            if(currentCourse != null && currentCourse.getTransitions() != null){
                                
                                if(!GatClientUtility.isCourseObjectNameValid(name, currentCourse, null)){
                                    setTransitionNameDialog.hide();
                                    WarningDialog.error("Duplicate name", "The name '" + name + "' is already in use.", new ModalDialogCallback() {

                                        @Override
                                        public void onClose() {
                                            setTransitionNameDialog.show();
                                        }
                                    });
                                    return;
                                }
                            }
                            
                            setTransitionNameDialog.hide();
                            
                            try {
                                CourseElementUtil.setTransitionName(dragged, name);
                            } catch (IllegalArgumentException e) {
                                logger.log(Level.SEVERE, "There was a a problem setting the name for the dragged course object.", e);
                            }
                            
                            if(dragged instanceof TrainingApplication) {
                                CopyTemplateFile action = new CopyTemplateFile(
                                        GatClientUtility.getUserName(), 
                                        getCourseFolderPath(), 
                                        name, AbstractSchemaHandler.DKF_FILE_EXTENSION);
                                
                                BsLoadingDialogBox.display("Creating External Application", "Creating external application resouces, please wait...");
                                dispatchService.execute(action, new AsyncCallback<CopyWorkspaceFilesResult>(){

                                    @Override
                                    public void onFailure(Throwable caught) {
                                        BsLoadingDialogBox.remove();
                                        logger.warning("Caught exception while copying dkf template file: " + caught.toString());
                                        dragCourseObject(dragged, node, startIndex);        
                                    }

                                    @Override
                                    public void onSuccess(CopyWorkspaceFilesResult result) {
                                        
                                        BsLoadingDialogBox.remove();
                                        if(result.isSuccess()){
                                            String dkfFile = result.getCopiedFilename();
                                            
                                            if(dkfFile != null) {
                                                DkfRef dkfRef = new DkfRef();
                                                dkfRef.setFile(dkfFile);
                                                ((TrainingApplication)dragged).setDkfRef(dkfRef);
                                                taGeneratedDkf = dkfFile;
                                                
                                            } else {
                                                logger.warning("Failed to copy the dkf template for the external application transition:\n"
                                                        + result.getErrorMsg() + "\n" + result.getErrorDetails());
                                            }
                                        }else{
                                            logger.warning("Failed to copy the dkf template for the external application transition:\n"
                                                    + result.getErrorMsg() + "\n" + result.getErrorDetails());
                                        }
                                        
                                        dragCourseObject(dragged, node, startIndex);        
                                                                            
                                    }
                                    
                                });
                                
                            } else {
                                dragCourseObject(dragged, node, startIndex);
                            }
                                                                                            
                        }
                    };
                    
                    final String icon = CourseElementUtil.getTypeIcon(dragged);
                    if(dragged instanceof TrainingApplication){
                        
                        if (!CourseElementUtil.isGIFTWrapSupported(dragged)) {
                            showSetTransitionNameDialog(CourseElementUtil.getTypeDisplayName(dragged), icon, finalExampleText,
                                    nameChangeHandler);
                        } else {                 
                            importedTrainingApp = null;
                            
                            NewOrExistingFileDialog.showCreateOrImportFromGIFTWrap(
                                    CourseElementUtil.getCourseObjectTypeImgTag(icon) + " Add External Application",
                                    new ClickHandler() {
                                        @Override
                                        public void onClick(ClickEvent event) {
                                            // prompt the user to enter a name for the new transition
                                            showSetTransitionNameDialog(CourseElementUtil.getTypeDisplayName(dragged), icon,
                                                    finalExampleText, nameChangeHandler);
                                        }
                                    }, new ClickHandler() {
                                        @Override
                                        public void onClick(ClickEvent event) {
                                            editCourseView.getGiftWrapDialog().addHideHandler(new ModalHideHandler() {
                                                @Override
                                                public void onHide(ModalHideEvent evt) {
                                                    if (importedTrainingApp != null) {
                                                        try {
                                                            CourseElementUtil.setTransitionName(importedTrainingApp,
                                                                    importedTrainingApp.getTransitionName());
                                                            dragCourseObject(importedTrainingApp, node, startIndex);
                                                            if(!GatClientUtility.isReadOnly()){
                                                                GatClientUtility.saveCourseAndNotify(); // #4913 - now saving course in order to not loose recently added course objects that lead to this gift wrap object being edited
                                                            }
                                                        } catch (IllegalArgumentException e) {
                                                            logger.log(Level.SEVERE,
                                                                    "There was a a problem importing the training application course object.",
                                                                    e);
                                                        }
                                                    }
                                                }

                                            });

                                            TrainingApplicationEnum type = TrainingAppUtil
                                                    .getTrainingAppType((TrainingApplication) dragged);
                                            editCourseView.getGiftWrapDialog().setCourseObjectUrl("GIFT Wrap",
                                                    GatClientUtility.createGIFTWrapModalUrl(type));
                                            editCourseView.getGiftWrapDialog().show();
                                        }
                                    });
                        }
                        
                    } else {
                        
                        //prompt the user to enter a name for the new transition
                        showSetTransitionNameDialog(CourseElementUtil.getTypeDisplayName(dragged), icon, finalExampleText, nameChangeHandler);
                    }                                
                    
                } else {
                    
                    //handle when an existing course object has been dragged
                    dragCourseObject(dragged, node, startIndex);
                }
           } else { // use the current branch
               
                if(currentCourse.getTransitions() == null){
                    currentCourse.setTransitions(new Transitions());
                }
                
                Serializable dropTarget = editCourseView.getCourseTree().getCourseObject(node);
                
                if(dropTarget != null && dropTarget.equals(dragged)){
                    return; //don't do anything if a transition is dragged on top of itself
                }
                
                final int startIndex = currentCourse.getTransitions().getTransitionType().indexOf(dragged);
                
                if(startIndex < 0){
                    
                    //handle when a new course object has been dragged
                    String exampleText = null;
                    
                    if(dragged instanceof Guidance){
                        
                        exampleText = "You may want to enter a name that describes what this " +CourseObjectName.GUIDANCE.getDisplayName()+ " contains "
                                + "at a glance.<br/>For example, \"Introduction\" or \"Lesson Complete\"";
                        
                    } else if(dragged instanceof PresentSurvey){
                        
                        exampleText = "You may want to enter a name that describes what this " +CourseObjectName.SURVEY.getDisplayName()+ " contains "
                                + "at a glance.<br/>For example, \"Knowledge Assessment Survey\" or \"AutoTutor Session\"";
                    
                    } else if(dragged instanceof LessonMaterial){
                        
                        exampleText = "You may want to enter a name that describes what this " +CourseObjectName.LESSON_MATERIAL.getDisplayName()+ " contains "
                                + "at a glance.<br/>For example, \"Concept Material\" or \"Pre-Lesson Resources\"";
                        
                    } else if(dragged instanceof TrainingApplication){
                        
                        exampleText = "You may want to enter a name that describes what this " +CourseObjectName.TRAINING_APPLICATION.getDisplayName()+ " contains "
                                + "at a glance.<br/>For example, \"Introduction Powerpoint\" or \"VBS Training Scenario\"";
                    }
                    
                    final String finalExampleText = exampleText;
                    
                    final ValueChangeHandler<String> nameChangeHandler = new ValueChangeHandler<String>() {

                        @Override
                        public void onValueChange(ValueChangeEvent<String> event) {
                            
                            if(event.getValue() == null || event.getValue().isEmpty()) {
                                WarningDialog.error("Invalid value", "The " +CourseElementUtil.getTypeDisplayName(dragged)+" name cannot be empty.");
                                return;
                            }else if(event.getValue().trim().isEmpty()){
                                WarningDialog.error("Invalid value", "The " +CourseElementUtil.getTypeDisplayName(dragged)+" name must contain characters other than spaces.");
                                return;
                            }
                            
                            String name = event.getValue().trim();
                            
                            if(currentCourse != null && currentCourse.getTransitions() != null){
                                
                                if(!GatClientUtility.isCourseObjectNameValid(name, currentCourse, null)){
                                    setTransitionNameDialog.hide();
                                    WarningDialog.error("Duplicate name", "The name '" + name + "' is already in use.", new ModalDialogCallback() {

                                        @Override
                                        public void onClose() {
                                            setTransitionNameDialog.show();
                                        }
                                    });
                                    return;
                                }
                            }
                            
                            setTransitionNameDialog.hide();
                            
                            try {
                                CourseElementUtil.setTransitionName(dragged, name);
                            } catch (IllegalArgumentException e) {
                                logger.log(Level.SEVERE, "There was a a problem setting the name for the dragged course object.", e);
                            }
                            
                            if(dragged instanceof TrainingApplication) {
                                CopyTemplateFile action = new CopyTemplateFile(
                                        GatClientUtility.getUserName(), 
                                        getCourseFolderPath(), 
                                        name, AbstractSchemaHandler.DKF_FILE_EXTENSION);
                                
                                BsLoadingDialogBox.display("Creating External Application", "Creating external application resouces, please wait...");
                                dispatchService.execute(action, new AsyncCallback<CopyWorkspaceFilesResult>(){

                                    @Override
                                    public void onFailure(Throwable caught) {
                                        BsLoadingDialogBox.remove();
                                        logger.warning("Caught exception while copying dkf template file: " + caught.toString());
                                        dragCourseObject(dragged, node, startIndex);        
                                    }

                                    @Override
                                    public void onSuccess(CopyWorkspaceFilesResult result) {
                                        
                                        BsLoadingDialogBox.remove();
                                        if(result.isSuccess()){
                                            String dkfFile = result.getCopiedFilename();
                                            
                                            if(dkfFile != null) {
                                                DkfRef dkfRef = new DkfRef();
                                                dkfRef.setFile(dkfFile);
                                                ((TrainingApplication)dragged).setDkfRef(dkfRef);
                                                taGeneratedDkf = dkfFile;
                                                
                                            } else {
                                                logger.warning("Failed to copy the dkf template for the external application transition:\n"
                                                        + result.getErrorMsg() + "\n" + result.getErrorDetails());
                                            }
                                        }else{
                                            logger.warning("Failed to copy the dkf template for the external application transition:\n"
                                                    + result.getErrorMsg() + "\n" + result.getErrorDetails());
                                        }
                                        
                                        dragCourseObject(dragged, node, startIndex);        
                                                                            
                                    }
                                    
                                });
                                
                            } else {
                                dragCourseObject(dragged, node, startIndex);
                            }
                                                                                            
                        }
                    };
                    
                    final String icon = CourseElementUtil.getTypeIcon(dragged);
                    if(dragged instanceof TrainingApplication){
                        
                        if (!CourseElementUtil.isGIFTWrapSupported(dragged)) {
                            showSetTransitionNameDialog(CourseElementUtil.getTypeDisplayName(dragged), icon, finalExampleText,
                                    nameChangeHandler);
                        } else {                 
                            importedTrainingApp = null;
                            
                            NewOrExistingFileDialog.showCreateOrImportFromGIFTWrap(
                                    CourseElementUtil.getCourseObjectTypeImgTag(icon) + " Add External Application",
                                    new ClickHandler() {
                                        @Override
                                        public void onClick(ClickEvent event) {
                                            // prompt the user to enter a name for the new transition
                                            showSetTransitionNameDialog(CourseElementUtil.getTypeDisplayName(dragged), icon,
                                                    finalExampleText, nameChangeHandler);
                                        }
                                    }, new ClickHandler() {
                                        @Override
                                        public void onClick(ClickEvent event) {
                                            editCourseView.getGiftWrapDialog().addHideHandler(new ModalHideHandler() {
                                                @Override
                                                public void onHide(ModalHideEvent evt) {
                                                    if (importedTrainingApp != null) {
                                                        try {
                                                            CourseElementUtil.setTransitionName(importedTrainingApp,
                                                                    importedTrainingApp.getTransitionName());
                                                            dragCourseObject(importedTrainingApp, node, startIndex);
                                                            if(!GatClientUtility.isReadOnly()){
                                                                GatClientUtility.saveCourseAndNotify(); // #4913 - now saving course in order to not loose recently added course objects that lead to this gift wrap object being edited
                                                            }
                                                        } catch (IllegalArgumentException e) {
                                                            logger.log(Level.SEVERE,
                                                                    "There was a a problem importing the training application course object.",
                                                                    e);
                                                        }
                                                    }
                                                }

                                            });

                                            TrainingApplicationEnum type = TrainingAppUtil
                                                    .getTrainingAppType((TrainingApplication) dragged);
                                            editCourseView.getGiftWrapDialog().setCourseObjectUrl("GIFT Wrap",
                                                    GatClientUtility.createGIFTWrapModalUrl(type));
                                            editCourseView.getGiftWrapDialog().show();
                                        }
                                    });
                        }
                        
                    } else {
                        
                        //prompt the user to enter a name for the new transition
                        showSetTransitionNameDialog(CourseElementUtil.getTypeDisplayName(dragged), icon, finalExampleText, nameChangeHandler);
                    }                                
                    
                } else {
                    
                    //handle when an existing course object has been dragged
                    dragCourseObject(dragged, node, startIndex);
                }
           }
        }
    }

    /**
     * If necessary, prompts the user to ask them if they want to delete the DKF for the given course object before they replace it. 
     * If they confirm the deletion or choose to just remove the reference, then the course object will be replaced with the
     * provided replacement
     * 
     * @param original the course object being replaced whose DKF should be checked. If null, this method will do nothing.
     * @param replacement the course object to replace the original when the user confirms. If null, this method will do nothing.
     */
    private void promptChangeTrainingAppIfNeeded(final TrainingApplication original, final Serializable replacement) {
        
        if(original == null || replacement == null) {
            return;
        }
        
        if(original.getDkfRef() != null && StringUtils.isNotBlank(original.getDkfRef().getFile())){
            // show are you sure you want to change training applications and either delete
            // the dkf or just remove the reference to the dkf 
            
            // get the training application type for display purposes
            String tAppStr = "current";
            TrainingApplicationEnum tappEnum = TrainingAppUtil.getTrainingAppType(original);
            if(tappEnum != null){
                tAppStr = tappEnum.getDisplayName();
            }
            
            DeleteRemoveCancelDialog.show("Change Training Application?", "The real time assessment created for this "+tAppStr+" application can not be applied"
                    + " to another training application.<br/><br/>"
                    + "If you proceed with ONLY changing the training application, the real time assessment file will be kept and can be selected from your workspace later.<br/></br>"
                    + "(<b>file name:</b> "+original.getDkfRef().getFile()+")", new DeleteRemoveCancelCallback() {
                        
                        @Override
                        public void remove() {
                            
                            logger.info("Removing reference to real time assessment file: "+original.getDkfRef().getFile());
                            replaceCourseObject(original, replacement);
                        }
                        
                        @Override
                        public void delete() {
                            
                            logger.info("Removing real time assessment file from server: "+original.getDkfRef().getFile());
                            final String filename = GatClientUtility.getBaseCourseFolderPath()
                                                + "/"
                                                + original.getDkfRef().getFile();
                            
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
                            
                            replaceCourseObject(original, replacement);
                        }
                        
                        @Override
                        public void cancel() {
                            //nothing to do                                    
                        }
                    }, "Delete Real Time assessment and change application", "Change Application");
        }else{    
            replaceCourseObject(original, replacement);
        }
    }

    /**
     * Replaces the given course object in the course with the replacement and loads the replacement
     * into the appropriate editor
     * 
     * @param original the original course object in the course to replace. If null, this method will do nothing.
     * @param replacement the course object to replace the original with. If null, this method will do nothing.
     */
    private void replaceCourseObject(Serializable original, Serializable replacement) {
        
        if(original == null || replacement == null) {
            return;
        }
        
        //copy the original course object's name to the replacement
        CourseElementUtil.setTransitionName(replacement, GatClientUtility.getTransitionName(original));
        
        //replace the course object in the course
        currentCourse.getTransitions().getTransitionType().set(0, replacement);
        
        //close the original object's editor, update the course tree, and open the replacement object's editor
        editCourseView.getEditorPanel().stopEditing(original, true, true);
        editCourseView.getCourseTree().loadTree(currentCourse);
        displaySelectedTransition(replacement);
    }

    /**
     * Handles when the given course object is dragged into an SVG element representing the given datum. If the dragged
     * object has not already been added to the course, this method will first prompt the user to give it a name before
     * attempting to add it. If the object HAS been added to the course, this method will move the object to the 
     * appropriate location
     * 
     * @param dragged the course object that was dragged.
     * @param node the branch tree node corresponding to the SVG element that was dragged into
     */
    private void dragCourseObject(final Serializable dragged, final mil.arl.gift.tools.authoring.server.gat.client.view.course.authoredbranch.tree.TreeNode node) {
        
        if(dragged != null){

           AuthoredBranch branch = editCourseView.getTreeManager().getCurrentBranch();
           
           // no branch selected
           if (branch != null) {
               
                if(currentCourse.getTransitions() == null){
                    currentCourse.setTransitions(new Transitions());
                }
                
                Serializable dropTarget = editCourseView.getTreeManager().getCourseObject(node);
                
                if(dropTarget != null && dropTarget.equals(dragged)){
                    return; //don't do anything if a transition is dragged on top of itself
                }
                
                final int startIndex = currentCourse.getTransitions().getTransitionType().indexOf(dragged);
                
                if(startIndex < 0){
                    
                    //handle when a new course object has been dragged
                    String exampleText = null;
                    
                    if(dragged instanceof Guidance){
                        
                        exampleText = "You may want to enter a name that describes what this " +CourseObjectName.GUIDANCE.getDisplayName()+ " contains "
                                + "at a glance.<br/>For example, \"Introduction\" or \"Lesson Complete\"";
                        
                    } else if(dragged instanceof PresentSurvey){
                        
                        exampleText = "You may want to enter a name that describes what this " +CourseObjectName.SURVEY.getDisplayName()+ " contains "
                                + "at a glance.<br/>For example, \"Knowledge Assessment Survey\" or \"AutoTutor Session\"";
                    
                    } else if(dragged instanceof LessonMaterial){
                        
                        exampleText = "You may want to enter a name that describes what this " +CourseObjectName.LESSON_MATERIAL.getDisplayName()+ " contains "
                                + "at a glance.<br/>For example, \"Concept Material\" or \"Pre-Lesson Resources\"";
                        
                    } else if(dragged instanceof TrainingApplication){
                        
                        exampleText = "You may want to enter a name that describes what this " +CourseObjectName.TRAINING_APPLICATION.getDisplayName()+ " contains "
                                + "at a glance.<br/>For example, \"Introduction Powerpoint\" or \"VBS Training Scenario\"";
                    }
                    
                    final String finalExampleText = exampleText;
                    
                    final ValueChangeHandler<String> nameChangeHandler = new ValueChangeHandler<String>() {

                        @Override
                        public void onValueChange(ValueChangeEvent<String> event) {
                            
                            if(event.getValue() == null || event.getValue().isEmpty()) {
                                WarningDialog.error("Invalid value", "The " +CourseElementUtil.getTypeDisplayName(dragged)+" name cannot be empty.");
                                return;
                            }else if(event.getValue().trim().isEmpty()){
                                WarningDialog.error("Invalid value", "The " +CourseElementUtil.getTypeDisplayName(dragged)+" name must contain characters other than spaces.");
                                return;
                            }
                            
                            String name = event.getValue().trim();
                            
                            if (!GatClientUtility.isCourseObjectNameValid(name, currentCourse, null)) {
                                setTransitionNameDialog.hide();
                                WarningDialog.error("Duplicate name", "The name '" + name + "' is already in use.", new ModalDialogCallback() {

                                    @Override
                                    public void onClose() {
                                        setTransitionNameDialog.show();
                                    }
                                });
                                return;
                            } 
                            
                            setTransitionNameDialog.hide();
                            
                            try {
                                CourseElementUtil.setTransitionName(dragged, name);
                            } catch (IllegalArgumentException e) {
                                logger.log(Level.SEVERE, "There was a a problem setting the name for the dragged course object.", e);
                            }
                            
                            if(dragged instanceof TrainingApplication) {
                                CopyTemplateFile action = new CopyTemplateFile(
                                        GatClientUtility.getUserName(), 
                                        getCourseFolderPath(), 
                                        name, AbstractSchemaHandler.DKF_FILE_EXTENSION);
                                
                                BsLoadingDialogBox.display("Creating External Application", "Creating external application resouces, please wait...");
                                dispatchService.execute(action, new AsyncCallback<CopyWorkspaceFilesResult>(){

                                    @Override
                                    public void onFailure(Throwable caught) {
                                        BsLoadingDialogBox.remove();
                                        logger.warning("Caught exception while copying dkf template file: " + caught.toString());
                                        dragCourseObject(dragged, node, startIndex);        
                                    }

                                    @Override
                                    public void onSuccess(CopyWorkspaceFilesResult result) {
                                        
                                        BsLoadingDialogBox.remove();
                                        if(result.isSuccess()){
                                            String dkfFile = result.getCopiedFilename();
                                            
                                            if(dkfFile != null) {
                                                DkfRef dkfRef = new DkfRef();
                                                dkfRef.setFile(dkfFile);
                                                ((TrainingApplication)dragged).setDkfRef(dkfRef);
                                                taGeneratedDkf = dkfFile;
                                                
                                            } else {
                                                logger.warning("Failed to copy the dkf template for the external application transition:\n"
                                                        + result.getErrorMsg() + "\n" + result.getErrorDetails());
                                            }
                                        }else{
                                            logger.warning("Failed to copy the dkf template for the external application transition:\n"
                                                    + result.getErrorMsg() + "\n" + result.getErrorDetails());
                                        }
                                        
                                        dragCourseObject(dragged, node, startIndex);        
                                                                            
                                    }
                                    
                                });
                                
                            } else {
                                dragCourseObject(dragged, node, startIndex);
                            }
                                                                                            
                        }
                    };
                    
                    final String icon = CourseElementUtil.getTypeIcon(dragged);
                    if(dragged instanceof TrainingApplication){
                        
                        if (!CourseElementUtil.isGIFTWrapSupported(dragged)) {
                            showSetTransitionNameDialog(CourseElementUtil.getTypeDisplayName(dragged), icon, finalExampleText,
                                    nameChangeHandler);
                        } else {                 
                            importedTrainingApp = null;
                            
                            NewOrExistingFileDialog.showCreateOrImportFromGIFTWrap(
                                    CourseElementUtil.getCourseObjectTypeImgTag(icon) + " Add External Application",
                                    new ClickHandler() {
                                        @Override
                                        public void onClick(ClickEvent event) {
                                            // prompt the user to enter a name for the new transition
                                            showSetTransitionNameDialog(CourseElementUtil.getTypeDisplayName(dragged), icon,
                                                    finalExampleText, nameChangeHandler);
                                        }
                                    }, new ClickHandler() {
                                        @Override
                                        public void onClick(ClickEvent event) {
                                            editCourseView.getGiftWrapDialog().addHideHandler(new ModalHideHandler() {
                                                @Override
                                                public void onHide(ModalHideEvent evt) {
                                                    if (importedTrainingApp != null) {
                                                        try {
                                                            CourseElementUtil.setTransitionName(importedTrainingApp,
                                                                    importedTrainingApp.getTransitionName());
                                                            dragCourseObject(importedTrainingApp, node, startIndex);
                                                            if(!GatClientUtility.isReadOnly()){
                                                                GatClientUtility.saveCourseAndNotify(); // #4913 - now saving course in order to not loose recently added course objects that lead to this gift wrap object being edited
                                                            }
                                                        } catch (IllegalArgumentException e) {
                                                            logger.log(Level.SEVERE,
                                                                    "There was a a problem importing the training application course object.",
                                                                    e);
                                                        }
                                                    }
                                                }

                                            });

                                            TrainingApplicationEnum type = TrainingAppUtil
                                                    .getTrainingAppType((TrainingApplication) dragged);
                                            editCourseView.getGiftWrapDialog().setCourseObjectUrl("GIFT Wrap",
                                                    GatClientUtility.createGIFTWrapModalUrl(type));
                                            editCourseView.getGiftWrapDialog().show();
                                        }
                                    });
                        }
                        
                    } else {
                        
                        //prompt the user to enter a name for the new transition
                        showSetTransitionNameDialog(CourseElementUtil.getTypeDisplayName(dragged), icon, finalExampleText, nameChangeHandler);
                    }                                
                    
                } else {
                    
                    //handle when an existing course object has been dragged
                    dragCourseObject(dragged, node, startIndex);
                }
           }
        }
    }

    /**
     * Drags the given transition to the location of the given course tree node and inserts it in the node's place
     * 
     * @param dragged the transition to insert
     * @param node the node where the transition should be inserted
     * @param startIndex the index that the transition was dragged from
     */
    private void dragCourseObject(
            Serializable dragged,
            mil.arl.gift.tools.authoring.server.gat.client.view.course.tree.TreeNode node,
            int startIndex) {
        
        TreeNodeEnum type = node != null ? TreeNodeEnum.valueOf(node.getType()) : null;

            if(type != null){
                
                if(TreeNodeEnum.TRANSITION.equals(type)){
                    
                    Serializable dropTarget = editCourseView.getCourseTree().getCourseObject(node);

                    if(dropTarget != null){
                        
                        int insertIndex = currentCourse.getTransitions().getTransitionType().indexOf(dropTarget);
                        
                        if(insertIndex >= 0){
                    
                            if(startIndex >= 0){
                                
                                if(startIndex == insertIndex - 1){
                                    
                                    //if a transition was dragged on top of the transition below it, swap the transitions
                                    Collections.swap(currentCourse.getTransitions().getTransitionType(), startIndex, insertIndex);
                                    
                                } else {
                        
                                    //otherwise, insert the dragged transition in the target's position
                                    currentCourse.getTransitions().getTransitionType().remove(dragged);
                                    currentCourse.getTransitions().getTransitionType().add(insertIndex, dragged);
                                }
                            
                                //update the tree to match the new state of the course
                                editCourseView.getCourseTree().loadTree(currentCourse);
                            
                            } else {
                            
                                //add the new transition
                                currentCourse.getTransitions().getTransitionType().add(insertIndex, dragged);
                                
                                //edit the new transition
                                displaySelectedTransition(dragged);         
                        }
                        
                        } else {
                            
                            //this really should never happen
                        WarningDialog.error("Failed to insert", "An error occurred while inserting a transition.");
                    }       
                    
                    } else {
                        
                        //this really should never happen
                    WarningDialog.error("Failed to insert", "An error occurred while inserting a transition. The target could not be found.");
                }
                
            } else if(TreeNodeEnum.COURSE_END.equals(type)){
                
                //move the transition to the end of the course
                currentCourse.getTransitions().getTransitionType().remove(dragged);
                currentCourse.getTransitions().getTransitionType().add(dragged);
                
                if(startIndex >= 0){
                    
                    //update the tree to match the new state of the course
                    editCourseView.getCourseTree().loadTree(currentCourse);
                    
                } else {
    
                    //edit the new transition
                    displaySelectedTransition(dragged);     
                }

            }

        } else {
            // if null, add it to the course end or the first path/last? path of the branch
            
            AuthoredBranch displayedBranch = editCourseView.getTreeManager().getCurrentBranch();

            // no branch shown, add to end of course
            if (displayedBranch == null) {

                //move the transition to the end of the course
                currentCourse.getTransitions().getTransitionType().remove(dragged);
                currentCourse.getTransitions().getTransitionType().add(dragged);
                
                if(startIndex >= 0){
                    
                    //save the moved transition
                    saveCourse(filePath, true);
                    
                } else {

                    //edit the new transition
                    displaySelectedTransition(dragged);    
                }

            }
        }
    }

    /**
     * Drags the given transition to the location of the given course tree node and inserts it in the node's place
     * 
     * @param dragged the transition to insert
     * @param node the node where the transition should be inserted
     * @param startIndex the index that the transition was dragged from
     */
    private void dragCourseObject(
            Serializable dragged,
            mil.arl.gift.tools.authoring.server.gat.client.view.course.authoredbranch.tree.TreeNode node,
            int startIndex) {
        
        TreeNodeEnum type = node != null ? TreeNodeEnum.valueOf(node.getType()) : null;

            if(type != null){
                
                if(TreeNodeEnum.TRANSITION.equals(type)){
                    
                    Serializable dropTarget = editCourseView.getTreeManager().getCourseObject(node);

                    if(dropTarget != null){
                        
                        Path path = editCourseView.getTreeManager().getPathOfElement(dropTarget);
                        int insertIndex = path.getCourseobjects().getAAROrAuthoredBranchOrEnd().indexOf(dropTarget);
                        
                        if(insertIndex >= 0){
                    
                            if(startIndex >= 0){
                                
                                if(startIndex == insertIndex - 1){
                                    
                                    //if a transition was dragged on top of the transition below it, swap the transitions
                                    Collections.swap(path.getCourseobjects().getAAROrAuthoredBranchOrEnd(), startIndex, insertIndex);

                                } else {
                        
                                    //otherwise, insert the dragged transition in the target's position
                                    path.getCourseobjects().getAAROrAuthoredBranchOrEnd().remove(dragged);
                                    path.getCourseobjects().getAAROrAuthoredBranchOrEnd().add(insertIndex, dragged);
                                }
                            
                                //update the tree to match the new state of the course
                                editCourseView.getTreeManager().showCurrentBranchTree();
                                editCourseView.getEditorPanel().startEditing(dragged);    
                            
                            } else {
                            
                                //add the new transition
                                path.getCourseobjects().getAAROrAuthoredBranchOrEnd().add(insertIndex, dragged);
                                
                                //edit the new transition
                                editCourseView.getTreeManager().showCurrentBranchTree();
                                editCourseView.getEditorPanel().startEditing(dragged);    
                            }
                        
                        } else {
                                
                            //this really should never happen
                            WarningDialog.error("Failed to insert", "An error occurred while inserting a transition.");
                        }       
                    
                    } else {
                        
                    //this really should never happen
                    WarningDialog.error("Failed to insert", "An error occurred while inserting a transition. The target could not be found.");
                }
                
            } else if(TreeNodeEnum.PATH_END.equals(type)) {
    
                AuthoredBranch displayedWidget = editCourseView.getTreeManager().getCurrentBranch();
                
                // no branch shown, add to end of course
                if (displayedWidget != null) {

                    mil.arl.gift.tools.authoring.server.gat.client.view.course.authoredbranch.tree.TreeNode pathStart = null;
                    for (mil.arl.gift.tools.authoring.server.gat.client.view.course.authoredbranch.tree.TreeNode s : editCourseView.getTreeManager().getBranchTree(editCourseView.getTreeManager().getCurrentBranch()).nodeToCourseElement.keySet()) {

                        // save a reference to the path start
                        if (TreeNodeEnum.PATH_START.equals(TreeNodeEnum.valueOf(s.getType()))) {
                            pathStart = s;
                        }

                        if (s.children() != null) {
                            for (int i =0; i<s.children().length(); i++) {
                                mil.arl.gift.tools.authoring.server.gat.client.view.course.authoredbranch.tree.TreeNode ne = s.children().get(i).cast();
                                if (node != null && ne.id() == node.id()) {
                                    // found the path this end node belongs to
                                    editCourseView.getTreeManager().addToPathEnd(pathStart, dragged);
                                    editCourseView.getEditorPanel().startEditing(dragged);    
                                    return;
                                }
                            }
                        }
                    }
                }

            } else if(TreeNodeEnum.COURSE_END.equals(type)){
                //TODO: handle dropping a course object onto the course end (and path start)
                
                //move the transition to the end of the course
                currentCourse.getTransitions().getTransitionType().remove(dragged);
                currentCourse.getTransitions().getTransitionType().add(dragged);
                
                if(startIndex >= 0){
                    
                    //update the tree to match the new state of the course
                    editCourseView.getCourseTree().loadTree(currentCourse);
                    
                } else {
    
                    //edit the new transition
                    displaySelectedTransition(dragged);     
                }

            }

        } else {
            // TODO: if null, add it to the end of the default branch
            
            AuthoredBranch displayedBranch = editCourseView.getTreeManager().getCurrentBranch();

            // no branch shown, add to end of course
            if (displayedBranch == null) {

                //move the transition to the end of the course
                currentCourse.getTransitions().getTransitionType().remove(dragged);
                currentCourse.getTransitions().getTransitionType().add(dragged);
                
                if(startIndex >= 0){
                    
                    //save the moved transition
                    saveCourse(filePath, true);
                    
                } else {

                    //edit the new transition
                    displaySelectedTransition(dragged);    
                }
            }
        }
    }

    /**
     * Updates all references to a concept name to reference a new concept name instead
     * 
     * @param oldName the old concept name
     * @param newName the new concept name
     */
    private void updateConceptReferences(String oldName, String newName) {
        
        boolean foundReferences = false; 
        
        //check the course for references to this concept. If any are found, update them to the new value
        if(currentCourse != null && currentCourse.getTransitions() != null){
            
            for(Serializable transition : currentCourse.getTransitions().getTransitionType()){
                
                if(transition instanceof PresentSurvey){
                    
                    PresentSurvey ps = (PresentSurvey) transition;
                    
                    if(ps.getSurveyChoice() != null && ps.getSurveyChoice() instanceof ConceptSurvey){
                        
                        ConceptSurvey cs = (ConceptSurvey) ps.getSurveyChoice();
                        
                        if(cs.getConceptQuestions() != null){
                            
                            /**
                             * If the concept questions have an extraneous concept that matches the new name
                             * and a concept that matches the old name, the extraneous concept needs to be 
                             * removed, otherwise the concept question list will contain duplicates
                             */
                            ConceptQuestions extraneousQuestion = null;
                            for(ConceptQuestions questions : cs.getConceptQuestions()){
                                // Check for an extraneous concept with the new name
                                if(questions.getName() != null && questions.getName().equals(newName)){
                                    extraneousQuestion = questions;
                                    break;
                                }
                            }
                            
                            Iterator<ConceptQuestions> iterator =  cs.getConceptQuestions().iterator();
                            while(iterator.hasNext()) {
                                ConceptQuestions questions = iterator.next();
                                if(questions.getName() != null && questions.getName().equals(oldName)){
                                    questions.setName(newName);
                                    
                                    foundReferences = true;
                                }
                                if(foundReferences && questions.equals(extraneousQuestion)) {
                                    iterator.remove();
                                }
                            }
                        }
                        
                    }
                    
                } else if (transition instanceof LessonMaterial) {
                    LessonMaterial lm = (LessonMaterial) transition;

                    if (lm.getLessonMaterialList() != null && lm.getLessonMaterialList().getMedia() != null) {

                        for (Media media : lm.getLessonMaterialList().getMedia()) {
                            if (media.getMediaTypeProperties() != null && media.getMediaTypeProperties() instanceof LtiProperties) {
                                LtiProperties ltiProp = (LtiProperties) media.getMediaTypeProperties();
                                LtiConcepts concepts = ltiProp.getLtiConcepts();
                                if (concepts != null && concepts.getConcepts().contains(oldName)) {
                                    foundReferences = true;
                                    
                                    int index = concepts.getConcepts().indexOf(oldName);
                                    concepts.getConcepts().remove(oldName);
                                    concepts.getConcepts().add(index, newName);
                                    
                                    break;
                                }
                            }
                        }
                    }
                } else if(transition instanceof MerrillsBranchPoint){
                    
                    MerrillsBranchPoint mbp = (MerrillsBranchPoint) transition;
                    
                    if(mbp.getConcepts() != null){
                        
                        List<String> concepts = mbp.getConcepts().getConcept();
                        if(concepts.contains(newName)) {
                            // Check to see if an extraneous concept with the new name exists
                            foundReferences = true;
                            concepts.remove(oldName);
                            
                        } else {
                            for(int index = 0; index < concepts.size(); index++){
                                
                                String conceptName = concepts.get(index);
                                
                                if(oldName.equalsIgnoreCase(conceptName)){
                                    //found old name
                                    logger.info("Updating concept '"+oldName+"' to '"+newName+"' in Adaptive Courseflow course object '"+mbp.getTransitionName()+"'.");
    
                                    concepts.set(index, newName);
                                    foundReferences = true;                                
                                    break;  //concepts are unique for a course, so there wont be another instance
                                }
                            }
                        }
                            
                        
                        logger.info("Current concepts for '"+mbp.getTransitionName()+"' are : "+mbp.getConcepts().getConcept());
                    }
                    
                    if(mbp.getQuadrants() != null){
                        
                        for(Serializable content : mbp.getQuadrants().getContent()){
                            
                            if(content instanceof Recall){
                                
                                Recall recall = (Recall) content;

                                if(recall.getPresentSurvey() != null){
                                    
                                    Recall.PresentSurvey ps = recall.getPresentSurvey();
                                    
                                    if(ps.getSurveyChoice() != null){
                                        
                                        Recall.PresentSurvey.ConceptSurvey cs = ps.getSurveyChoice();
                                        
                                        if(cs.getConceptQuestions() != null){
                                            Iterator<ConceptQuestions> iterator = cs.getConceptQuestions().iterator();
                                            
                                            if(foundReferences) {
                                                while(iterator.hasNext()) {
                                                    // Check for extraneous concepts that match the new name
                                                    ConceptQuestions questions = iterator.next();
                                                    if(questions.getName() != null && questions.getName().equals(newName)){
                                                        iterator.remove();
                                                    }
                                                }    
                                            }
                                            
                                            for(ConceptQuestions questions : cs.getConceptQuestions()){
                                                
                                                if(questions.getName() != null && questions.getName().equals(oldName)){
                                                    questions.setName(newName);
                                                    
                                                    foundReferences = true;
                                                }
                                            }
                                        }
                                        
                                    }
                                }
                                
                            } else if(content instanceof Practice){
                                
                                Practice practice = (Practice) content;
                                
                                if(practice.getPracticeConcepts() != null){
                                    
                                    List<String> concepts = practice.getPracticeConcepts().getCourseConcept();
                                    if(concepts.contains(newName)) {
                                        // Check to see if an extraneous concept with the new name exists
                                        foundReferences = true;
                                        concepts.remove(oldName);
                                        
                                    } else {
                                        for(int index = 0; index < concepts.size(); index++){
                                            
                                            String conceptName = concepts.get(index);
                                            
                                            if(oldName.equalsIgnoreCase(conceptName)){
                                                //found old name
                                                logger.info("Updating concept '"+oldName+"' to '"+newName+"' in the Practice part of an Adaptive Courseflow course object '"+mbp.getTransitionName()+"'.");
    
                                                concepts.set(index, newName);
                                                
                                                foundReferences = true;                             
                                                break;  //concepts are unique for a course, so there wont be another instance
                                            }
                                        }
                                    }
                                    
                                    logger.info("Current practice concepts for '"+mbp.getTransitionName()+"' are : "+practice.getPracticeConcepts().getCourseConcept());
                                }
                            }
                        }
                    }
                }
            }
        }
        
        if(foundReferences){
            
            WarningDialog.info("References updated", "All references to <b>" + oldName + "</b> in this course file "
                    + "have been automatically updated to reference <b>" + newName + "</b> instead. <br/><br/>"
                    + "Note that any Metadata files in this course folder referencing <b>" + oldName + "</b> "
                    + "will still need to be manually updated to reference " + newName + ".");
        }
    }

    /**
     * Updates all references to an LTI provider id to reference a new LTI provider id instead
     * 
     * @param oldId the old LTI provider id
     * @param newId the new LTI provider id
     */
    private void updateLtiProviderIdReferences(String oldId, String newId) {
        
        boolean foundReferences = false;

        // check the course for references to this concept. If any are found, update them to the new
        // value
        if (currentCourse != null && currentCourse.getTransitions() != null) {

            for (Serializable transition : currentCourse.getTransitions().getTransitionType()) {

                if (transition instanceof LessonMaterial) {
                    LessonMaterial lm = (LessonMaterial) transition;

                    if (lm.getLessonMaterialList() != null && lm.getLessonMaterialList().getMedia() != null) {

                        for (Media media : lm.getLessonMaterialList().getMedia()) {
                            if (media.getMediaTypeProperties() != null && media.getMediaTypeProperties() instanceof LtiProperties) {
                                LtiProperties ltiProp = (LtiProperties) media.getMediaTypeProperties();
                                if (ltiProp.getLtiIdentifier() != null && ltiProp.getLtiIdentifier().equalsIgnoreCase(oldId)) {
                                    foundReferences = true;

                                    ltiProp.setLtiIdentifier(newId);

                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }

        if (foundReferences && !newId.equalsIgnoreCase(oldId)) {
            
            WarningDialog.info("References Updated", "All references to <b>" + oldId + "</b> in this course file "
                    + "have been automatically updated to reference <b>" + newId + "</b> instead. <br/><br/>"
                    + "Note that any Metadata files in this course folder referencing <b>" + oldId + "</b> "
                    + "will still need to be manually updated to reference " + newId + ".");
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
        // this modal isn't visible so this isn't the target of the import
        if (!editCourseView.getGiftWrapDialog().isShowing()) {
            return;
        }

        importedTrainingApp = event.getTrainingApplication();
        editCourseView.getGiftWrapDialog().hide();
    }

    /**
     * A survey has changed in this course.
     * Update the survey context last modified date on the history portion of the course properties. 
     *
     * @param event the event
     */
    @EventHandler
    protected void onSurveysChangedEvent(SurveysChangedEvent event) {
        fetchCourseHistory();
    }
        
    /**
     * On custom form field focus event.
     *
     * @param event the event
     */
    @EventHandler
    protected void onCustomFormFieldFocusEvent(CustomFormFieldFocusEvent event) {
        
    }
    
    /**
     * On editor dirty event.
     *
     * @param event the event
     */
    @EventHandler
    protected void onEditorDirtyEvent(EditorDirtyEvent event) {
        //editCourseView.setNavigationEnabled(false);
        isCourseDirty = true;
    }
    
    /**
     * On file discard changes.
     */
    private void onFileDiscardChanges() {
        
        logger.info("onFileDiscardChanges");
        
                doGetCourseObject(URL.decodeQueryString(filePath));
                editCourseView.hideCourseObjectModal();
            
    }

    /**
     * On form field focus event.
     *
     * @param event the event
     */
    @EventHandler
    protected void onFormFieldFocusEvent(FormFieldFocusEvent event) {
    
    }

    /**
     * Performs logic whenever a course object is opened for editing
     * 
     * @param event an event indicating that a course object was opened for editing
     */
    @EventHandler
    protected void onCourseObjectOpenedForEditing(CourseObjectOpenedForEditingEvent event){
        
        if(event.getCourseObject() != null){
            
            editCourseView.getCourseTree().selectCourseObject(event.getCourseObject());
            
            //update the survey composer if a PresentSurvey object was opened
            if(event.getCourseObject() instanceof PresentSurvey) {
                SurveyEditorModal.onSurveyReferenceSelected(new PresentSurveyReference((PresentSurvey) event.getCourseObject()));
                
            } else {
                SurveyEditorModal.onSurveyReferenceSelected(null);
            }
        }
    }

    /**
     * Performs logic whenever a course object is done being edited
     * 
     * @param event an event indicating that a course object was finished being edited
     */
    @EventHandler
    protected void onCourseObjectDoneEditing(CourseObjectDoneEditingEvent event){
        
        editCourseView.getCourseTree().setSelectedNode(null, true);
    }
    
    /**
     * Performs logic whenever a course object has been renamed
     * 
     * @param event an event indicating that a course object was renamed
     */
    @EventHandler
    protected void onCourseObjectRenamed(CourseObjectRenamedEvent event){
        
        if(event.getCourseObject() != null){
            editCourseView.getCourseTree().loadTree(currentCourse);
            editCourseView.getCourseTree().selectCourseObject(event.getCourseObject());

            // update branch if a branch is shown
            if (editCourseView.getTreeManager().getCurrentBranch() != null) {
                editCourseView.getTreeManager().showCurrentBranchTree();
                AuthoredBranchTree authoredBranchTree = editCourseView.getTreeManager().getBranchTree(editCourseView.getTreeManager().getCurrentBranch());
                mil.arl.gift.tools.authoring.server.gat.client.view.course.authoredbranch.tree.TreeNode selected = null;
                for (Entry<mil.arl.gift.tools.authoring.server.gat.client.view.course.authoredbranch.tree.TreeNode, Serializable> entry : authoredBranchTree.nodeToCourseElement.entrySet()) {
                    if (entry.getValue().equals(event.getCourseObject())) {
                        selected = entry.getKey();
                        break;
                    }
                }
                authoredBranchTree.setSelectedNode(selected, false);
            }
        }
    }
    
    @EventHandler
    protected void onCourseObjectRedraw(CourseObjectRedrawEvent event){
        
        //when a course object needs to be redrawn, redraw the tree containing it and re-select it
        if(event.getCourseObject() != null){
            editCourseView.getCourseTree().loadTree(currentCourse);
            editCourseView.getCourseTree().selectCourseObject(event.getCourseObject());

            // update branch if a branch is shown
            if (editCourseView.getTreeManager().getCurrentBranch() != null) {
                editCourseView.getTreeManager().showCurrentBranchTree();
                AuthoredBranchTree authoredBranchTree = editCourseView.getTreeManager().getBranchTree(editCourseView.getTreeManager().getCurrentBranch());
                mil.arl.gift.tools.authoring.server.gat.client.view.course.authoredbranch.tree.TreeNode selected = null;
                for (Entry<mil.arl.gift.tools.authoring.server.gat.client.view.course.authoredbranch.tree.TreeNode, Serializable> entry : authoredBranchTree.nodeToCourseElement.entrySet()) {
                    if (entry.getValue().equals(event.getCourseObject())) {
                        selected = entry.getKey();
                        break;
                    }
                }
                authoredBranchTree.setSelectedNode(selected, false);
            }
        }
    }
    
    /**
     * Performs logic whenever a course object has been disabled or enabled
     * 
     * @param event
     *            an event indicating that a course object was disabled or
     *            enabled
     */
    @EventHandler
    protected void onCourseObjectDisabled(CourseObjectDisabledEvent event) {
        if (event.getCourseObject() != null) {
            editCourseView.getCourseTree().loadTree(currentCourse);
            editCourseView.getCourseTree().selectCourseObject(event.getCourseObject());
        }
    }

    /**
     * Handle the course concepts changed event.
     * 
     * @param event course concepts changed.
     */
    @EventHandler
    protected void onCourseConceptsChanged(@SuppressWarnings("unused") CourseConceptsChangedEvent event) {
        /* The course concepts may have changed, so notify the listening editor,
         * if there is one */
        notifyListenerEditorWhenConceptsChange();

        populateHeaderView();
    }

    /**
     * Performs logic whenever the LTI providers have changed
     * 
     * @param event an event indicating that the LTI provider list has changed
     */
    private void courseLtiProvidersChanged() {
        // only use the course authored LTI providers.
        if (currentCourse.getLtiProviders() != null) {
            List<LtiProvider> providerList = currentCourse.getLtiProviders().getLtiProvider();
            ltiProviderListDataProvider.getList().clear();
            ltiProviderListDataProvider.getList().addAll(providerList);
            ltiProviderListDataProvider.refresh();
            
            editCourseView.getHeaderView().getLTIConsumerPropertiesPanel().refresh(providerList);
            editCourseView.getHeaderView().showLtiProvidersListEmptyPanel(providerList.isEmpty());
            
            eventBus.fireEvent(new EditorDirtyEvent());
            eventBus.fireEvent(new CourseLtiProvidersChangedEvent(currentCourse));
        }
    }

    /**
     * Populates the header view's fields using the Course object currently being edited.
     */
    public void populateHeaderView() {
        
        if(currentCourse != null){
            
            editCourseView.getHeaderView().getNameInput().setValue(
                    currentCourse.getName() != null 
                            ? currentCourse.getName().trim() 
                            : ""
            );
            
            editCourseView.getCourseNameButton().setValue(
                    currentCourse.getName() != null 
                            ? currentCourse.getName().trim() 
                            : ""
            );
            
            editCourseView.getHeaderView().getDescriptionInput().setValue(
                    (currentCourse.getDescription() != null 
                            ? currentCourse.getDescription().trim() 
                            : ""), true
            );
            
            editCourseView.getHeaderView().getExcludeCheckBox().setValue(
                    currentCourse.getExclude() != null && currentCourse.getExclude().equals(BooleanEnum.TRUE)
            );
            
            conceptListDataProvider.getList().clear();
            
            Concepts concepts = currentCourse.getConcepts();
            if(concepts != null) {
                Serializable listOrHierarchy = concepts.getListOrHierarchy();
                if(listOrHierarchy instanceof Concepts.List) {
                    //Get the list of concepts from the scenario.
                    List<Concepts.List.Concept> conceptsInList = ((Concepts.List)listOrHierarchy).getConcept();
                    
                    //Add those concepts to the data provider that is linked to
                    //the concept list widget.
                    conceptListDataProvider.getList().clear();
                    conceptListDataProvider.getList().addAll(conceptsInList);
                    conceptListDataProvider.refresh();
                    
                    //update the editor for the concept list with the loaded concepts
                    editCourseView.getHeaderView().setConceptsList(conceptsInList);
                    
                } else {
                    //Get the hierarchy of concepts from the scenario.
                    ConceptNode conceptNode = ((Concepts.Hierarchy)listOrHierarchy).getConceptNode();
                    
                    editCourseView.getHeaderView().populatePropertiesHierarchy(conceptNode);
                    
                    if(conceptNode == null){
                        editCourseView.getHeaderView().showConceptHierarchyEmptyPanel(true);
                    
                    } else {
                        editCourseView.getHeaderView().showConceptHierarchyEmptyPanel(false);
                    }
                }
            }
            
            if(currentCourse.getImage() != null){
                editCourseView.getHeaderView().getCourseImageNameLabel().setText(currentCourse.getImage() + "\t");
                fetchCourseImage();                
                
            } else{
                editCourseView.getHeaderView().getCourseImageNameLabel().setText("No Image Selected (Default will be used).");
            }
            editCourseView.getHeaderView().setClearImageButtonVisible(currentCourse.getImage() == null ? false : true);
            editCourseView.getHeaderView().getPreviewTileIcon().setMarginLeft(10);
            editCourseView.getHeaderView().setPreviewTileIconVisible(currentCourse.getImage() == null ? false : true);
                
            List<LtiProvider> providerList;
            if (currentCourse.getLtiProviders() == null) {
                providerList = new ArrayList<LtiProvider>();
            } else {
                providerList = currentCourse.getLtiProviders().getLtiProvider();
            }
            editCourseView.getHeaderView().getLTIConsumerPropertiesPanel().init(providerList);
            editCourseView.getHeaderView().getLTIProviderPropertiesPanel().init(ltiCourseId);
            
            // Add the LTI providers to the data provider that is linked to
            // the LTI provider list widget.
            ltiProviderListDataProvider.getList().clear();
            ltiProviderListDataProvider.getList().addAll(providerList);

            editCourseView.getHeaderView().showLtiProvidersListEmptyPanel(providerList.isEmpty());

            //
            // Update the last successful course validation label 
            //
            String dateStr = currentCourse.getLastSuccessfulValidation();
            try{
                if(dateStr != null){
                    updateLastSuccessfulValidation(VALIDATION_DATE_FORMAT.parse(dateStr));
                }
            }catch(@SuppressWarnings("unused") Exception e){
                /* best effort */
                editCourseView.getHeaderView().getLastSuccessfulValidationLabel().setText(UNKNOWN_HISTORY_VALUE);
            }
            
            //
            // Update the other course history labels
            //
            fetchCourseHistory();
        }
    }
    
    /**
     * Leaves the provided concept hierarchy provided with unique concept names.  When a duplicate is found
     * the suffix "(1)" is added repeatedly until the concept name is unique.
     * 
     * @param conceptNode the concept hierarchy node to check for and update to unique concept names
     * @param uniqueSet the unique set of concept names in the entire course concept hierarchy
     */
    private static void removeDuplicateCourseConcepts(generated.course.ConceptNode conceptNode, Set<String> uniqueSet){
        
        String lowerCaseConcept = conceptNode.getName().toLowerCase().trim();
        if(uniqueSet.contains(lowerCaseConcept)){
            //duplicate - can't remove because this could have implications on the hierarchy, so rename instead
            
            String suffix = "(1)";
            while(true){
                
                //add the suffix and check for uniqueness
                lowerCaseConcept += suffix;
                
                if(!uniqueSet.contains(lowerCaseConcept)){
                    //adding the suffix to the current string makes it unique, set the unique value
                    conceptNode.setName(lowerCaseConcept);
                    break;
                }
            }
        }

        uniqueSet.add(lowerCaseConcept);
        
        if(conceptNode.getConceptNode() != null){
            //repeat for each child
            
            for(generated.course.ConceptNode childConceptNode : conceptNode.getConceptNode()){
                removeDuplicateCourseConcepts(childConceptNode, uniqueSet);
            }
        }
    }
    
    /**
     * Updates the last successful course validation label on the course properties panel.
     * 
     * @param date the date of the last successful course validation.  Can be null in which case the default
     * value will be shown in the label.
     */
    private void updateLastSuccessfulValidation(Date date){
        
        if(date != null){
            editCourseView.getHeaderView().getLastSuccessfulValidationLabel().setText(COURSE_HISTORY_DATE_FORMAT.format(date));
        }else{
            editCourseView.getHeaderView().getLastSuccessfulValidationLabel().setText(UNKNOWN_HISTORY_VALUE);
        }
    }
    
    /**
     * Updates the survey context last modified date on the course properties panel.
     * 
     * @param date the date of the last modification to the survey context.  Can be null in which case the default
     * value will be show in the label.
     */
    private void updateSurveyContextLastModified(Date date){
        
        if(date != null){
            editCourseView.getHeaderView().getSurveyContextLastModifiedLabel().setText(
                    COURSE_HISTORY_DATE_FORMAT.format(date));
        }else{
            editCourseView.getHeaderView().getSurveyContextLastModifiedLabel().setText(UNKNOWN_HISTORY_VALUE);
        }        
    }

    /**
     * Creates a copy of the transition name without causing conflicts.
     * 
     * @param transitionName The name of the transition being copied
     * @return The original transition name with a number in parenthesis, e.g. "Information (1)"
     */
    private String copyTransitionName(String transitionName) {
        
        int count = 1;
        String copiedName = transitionName + " (" + count + ")";
        
        // check for duplicates among all other course objects
        while (!GatClientUtility.isCourseObjectNameValid(copiedName, currentCourse, null)) {
            count++;
            copiedName = transitionName + " (" + count + ")";
        }
        
        return copiedName;
    }

    /**
     * Setup menu commands.
     */
    private void setupMenuCommands() {

        final Scheduler.ScheduledCommand fileSaveCmd = new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                saveCourse(filePath, true);
            }
        };
        editCourseView.setFileSaveCommand(fileSaveCmd);
        
        
        final Scheduler.ScheduledCommand unlockCourseCommand = new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                onUnlockCourse();
            }
        };
        editCourseView.setUnlockCourseCommand(unlockCourseCommand);


        Scheduler.ScheduledCommand saveDescriptionCmd = new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                editCourseView.getHeaderView().getDescriptionInput().setValue(
                        editCourseView.getEditorDescription(), true);
                fileSaveCmd.execute();
            }
        };
        editCourseView.setSaveDescriptionCommand(saveDescriptionCmd);
        
        Scheduler.ScheduledCommand fileDiscardChangesCmd = new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                onFileDiscardChanges();
            }
        };
        editCourseView.setFileDiscardChangesCommand(fileDiscardChangesCmd);    
                        
        editCourseView.setFileSaveAndValidateCommand(new Scheduler.ScheduledCommand() {
            
            @Override
            public void execute() {
                saveCourse(filePath, false, true, false);
            }
        });
        
        editCourseView.getShowUserActionListButton().addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                editCourseView.showUserAccessList();
                        } 
        });
    }
                        
    private void fetchCourseImage() {
        
        String userName = GatClientUtility.getUserName();
        final FetchContentAddress action = new FetchContentAddress(getCourseFolderPath(), currentCourse.getImage(), userName);
        
        dispatchService.execute(action, new AsyncCallback<FetchContentAddressResult>() {

            @Override
            public void onFailure(Throwable cause) {
                ErrorDetailsDialog dialog = new ErrorDetailsDialog(
                        "There was a problem retrieving the course tile image.", 
                        cause.getMessage(), 
                        DetailedException.getFullStackTrace(cause));
                        dialog.setDialogTitle("Failed to Retrieve Course Tile Image");
                        dialog.center();
            }

            @Override
            public void onSuccess(FetchContentAddressResult result) {
                
                if(result.isSuccess()) {
                    editCourseView.getHeaderView().getCourseTileImage().setUrl(result.getContentURL());
                    editCourseView.getHeaderView().getCourseTileImage().setWidth("310px");
                    editCourseView.getHeaderView().getCourseTileImage().setHeight("140px");
                } else {
                    editCourseView.getHeaderView().getCourseTileImage().setResource(GatClientBundle.INSTANCE.image_not_found());
                    editCourseView.getHeaderView().getCourseTileImage().setWidth("200px");
                    editCourseView.getHeaderView().getCourseTileImage().setHeight("200px");
                }
            }            
        });
    }
    
    /**
     * Requests course history information from the server and then updates the appropriate
     * components in the history part of the course properties panel. 
     */
    private void fetchCourseHistory(){
        
        logger.info("Fetching course history information");
        
        String userName = GatClientUtility.getUserName();
        
        if(currentCourse.getSurveyContext() != null){
        
            Integer surveyContextId = currentCourse.getSurveyContext().intValue();
            final FetchCourseHistory action = new FetchCourseHistory(userName, getCourseFolderPath(), surveyContextId);
            
            dispatchService.execute(action, new AsyncCallback<FetchCourseHistoryResult>() {            
                
                @Override
                public void onFailure(Throwable cause) {
                    ErrorDetailsDialog dialog = new ErrorDetailsDialog(
                            "There was a problem retrieving the course history.", 
                            cause.getMessage(), 
                            DetailedException.getFullStackTrace(cause));
                            dialog.setDialogTitle("Failed to Retrieve Course History");
                            dialog.center();
                }
                
                @Override
                public void onSuccess(FetchCourseHistoryResult result) {
                    
                    if(result.isSuccess()) {
                        
                        logger.info("Received course history information of "+result);
                        
                        if(result.getCourseFolderLastModifiedDate() != null){
                            editCourseView.getHeaderView().getLastModifiedLabel().setText(
                                    COURSE_HISTORY_DATE_FORMAT.format(result.getCourseFolderLastModifiedDate()));
                        }else{
                            editCourseView.getHeaderView().getLastModifiedLabel().setText(UNKNOWN_HISTORY_VALUE);
                        }
                        
                        updateSurveyContextLastModified(result.getSurveyContextLastModifiedDate());
    
                    } else {
                        
                        logger.info("Failed to retrieve course history information");
                        
                        editCourseView.getHeaderView().getLastModifiedLabel().setText(UNKNOWN_HISTORY_VALUE);
                        editCourseView.getHeaderView().getSurveyContextLastModifiedLabel().setText(UNKNOWN_HISTORY_VALUE);
                    }
                    
                }   
            });
        }
    }
    
    /**
     * Displays the set name dialog for a new transition,
     * 
     * @param transitionType The name of the transition type.
     * @param icon a path to an icon for the course object type (e.g. "images/VBS3.png")
     * @param exampleText additional information below the name textfield
     * @param handler The value change handler to execute when a name is set
     */
    private void showSetTransitionNameDialog(String transitionType, String icon, String exampleText, ValueChangeHandler<String> handler) {
        setTransitionNameDialog.setValue("");
        setTransitionNameDialog.setText(
                CourseElementUtil.getCourseObjectTypeImgTag(icon) + " New " + transitionType + " Name");
        setTransitionNameDialog.setInstructionsHtml("Enter a unique name that describes the " + transitionType + ":");
        setTransitionNameDialog.setAdditionalInfo(exampleText == null ? 
                null : "<span style=\"font-style: italic; font-size: 13px; color: gray\">" + exampleText + "</span>");
        
        if(setTransitionNameHandler != null) {
            setTransitionNameHandler.removeHandler();
        }
        setTransitionNameHandler = setTransitionNameDialog.addValueChangeHandler(handler);
        
        setTransitionNameDialog.center();
        
    }
    
    
    /**
     * Handler for when the course unlock button is pressed.  A course can be unlocked by the user
     * currently if the user has permissions to edit the course, but the course has dependencies on data collection.
     * If there are dependencies, then the user is warned that the course can be modified, but it may impact any
     * existing data collection results.
     * 
     */
    private void onUnlockCourse() {
        
        // Make sure the state is right to attempt to unlock.
        if (readOnly && userCanModify && hasDataCollectionDependencies) {
            
            StringBuilder sb = new StringBuilder();
            sb.append("This course has dependencies on data collection. One or more data sets have been created for this course in the ");
            sb.append(" My Research panel.");
            sb.append("<br/><br/>");
            sb.append("<b><i>If you unlock this course, any changes made could invalidate any data collection ");
            sb.append("results in each data set created for this course.</b></i>");
            sb.append("<br/><br/>");
            sb.append("Are you sure you want to unlock this course?");
            OkayCancelDialog.show("Confirm Unlock", sb.toString(), "Unlock Course", new OkayCancelCallback() {
                @Override
                public void okay() {
                    userBypassedDataCollectionWarning = true;
                    determineAndSetReadOnly();
                }
                
                @Override
                public void cancel() {
                     // Do nothing on cancel.
                }
            });
        }
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
            // TODO - Allow saving of courses in the PUBLIC folder in desktop mode by setting the flag to false.
            fileNewDialog = new DefaultGatFileSaveAsDialog(true);
        }
        
        fileNewDialog.setAllowNavigationToSubfolders(false);
        fileNewDialog.setAllowedFileExtensions(new String[]{AbstractSchemaHandler.COURSE_FILE_EXTENSION});

        fileNewDialog.setIntroMessageHTML(""
                +   "<div style='padding: 10px; font-weight: bold;'>"
                +       "Please name your course before authoring."
                +   "</div>");
        
        fileNewDialog.setFileNameLabelText("");     
        fileNewDialog.setConfirmButtonText("Create");
        fileNewDialog.setText("New Course");
        fileNewDialog.setCloseOnConfirm(false);
        fileNewDialog.setFoldersSelectable(true);
        fileNewDialog.setFileListVisible(false);
        
        handlerRegistrations.add(fileNewDialog.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                String path = getFileNewPath();
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
                
                //If the path is within the user's folder then the first
                //element will be the user name, the second will be the
                //course folder, and the third will be the file.
                int length = path.split(Constants.FORWARD_SLASH).length;
                if(length != 3) {
                    
                    WarningDialog.warning("Invalid path", "All courses must be created inside the user folder.");
                    
                    fileNewDialog.reallowConfirm();
                    return;
                }
                
                //The file dialog was intended so that exists is always false
                //but there seems to be a bug. Take a look at 
                //https://gifttutoring.org/issues/1812
                int index = path.lastIndexOf(Constants.FORWARD_SLASH);
                String courseFolder = path.substring(0, index);
                index = courseFolder.lastIndexOf(Constants.FORWARD_SLASH) + 1;
                courseFolder = courseFolder.substring(index);
                boolean exists = fileNewDialog.isFileOrFolderInCurrentDirectory(courseFolder);
                
                if(!exists || path.equals(filePath)) {
                    createNewFile(path);
                } else {
                    
                    WarningDialog.warning("Duplicate Course Name", "A course named '" + courseFolder + "' already exists."
                            + "<br/><b>Note</b>: course names are not case sensitive.<br/><br/>"
                            + "Please enter a different name for this course.", 
                            new ModalDialogCallback() {

                                @Override
                                public void onClose() {
                                    fileNewDialog.reallowConfirm();
                                }
                            }
                    );
                }
            }
        }));
        
        fileNewDialog.setCancelCallback(new CancelCallback() {
            
            @Override
            public void onCancel() {
                if(filePath == null) {
                    fileNewDialog.hide();
                    GatClientUtility.returnToDashboard();
                }
            }
        });
        
    }
    
    
    /* (non-Javadoc)
     * @see mil.arl.gift.tools.authoring.gat.client.view.CourseView.Presenter#start(com.google.gwt.user.client.ui.AcceptsOneWidget, mil.arl.gift.tools.authoring.gat.shared.id.CourseId)
     */
    @Override
    public void start(final AcceptsOneWidget containerWidget, HashMap<String, String> startParams) {        
        super.start();
        
        long msSinceLastStop = System.currentTimeMillis() - lastStopTimeMS;
        logger.info("start() called with params:\n" + startParams + "\nms since stop() = "+msSinceLastStop);
        
        // check whether the stop() was called recently and if so don't continue loading this presenter.
        // This is a hacked fix for the issue of the GAT entering read only for a course that you very recently
        // left the GAT and are now returning to the GAT for the same course.  The issue was that while the Dashboard
        // My courses page was being created a new course activity+presenter would be created which would cause a
        // lock to happen (within the same second of the stop() being called and its unlock course logic request to the
        // server).  This lock could then block the next GAT users on the course from getting write access to that course.
        if(msSinceLastStop < JUSTED_STOP_WINDOW_MS){
            logger.info("Ending start method because stop method was called recently.");
            return;
        }
        
        // Setup any start parameters that have been passed in via the url.
        DeploymentModeEnum mode = DeploymentModeEnum.DESKTOP;
        String modeParam = startParams.get(CoursePlace.PARAM_DEPLOYMODE);
        
        // Initialize the dialogs based on what mode the presenter is deployed in.
        initFileNewDialog(mode);       

        if (modeParam != null) {
            mode = DeploymentModeEnum.valueOf(modeParam);
        } else {
            logger.severe("Start mode could not be determined.  Defaulting to DESKTOP.");
        }
        
        final String scenarioId = GatClientUtility.getExternalScenarioId();
        
        /* 
         * Nick: Need to URL decode the path to prevent an issue in Firefox where URL encoded characters (namely spaces as %20s) aren't 
         * properly decoded before they get to this point
         */
        final String path = startParams.get(CoursePlace.PARAM_FILEPATH);
        
        if(path == null || path.isEmpty()) {
            
            if(scenarioId != null) {
                
                SharedResources.getInstance().getRpcService().createCourseForScenario(scenarioId, new AsyncCallback<GenericRpcResponse<ExternalScenarioImportResult>>() {

                    @Override
                    public void onFailure(Throwable thrown) {
                        DetailedExceptionSerializedWrapper e = new DetailedExceptionSerializedWrapper(new DetailedException(
                                "Failed to create course for external error. An unexpected error occured.", 
                                "A server error was thrown while fulfilling the request to create a course.", 
                                thrown
                        ));
                        ErrorDetailsDialog dialog = new ErrorDetailsDialog(e.getReason(), e.getDetails(),
                                e.getErrorStackTrace());
                        dialog.center();
                    }

                    @Override
                    public void onSuccess(GenericRpcResponse<ExternalScenarioImportResult> response) {
                        
                        if(!response.getWasSuccessful()) {
                            DetailedExceptionSerializedWrapper e = response.getException();
                            ErrorDetailsDialog dialog = new ErrorDetailsDialog(e.getReason(), e.getDetails(),
                                    e.getErrorStackTrace());
                            dialog.center();
                            
                        } else {
                            
                            /* A username fetched from SSO won't be in the URL, so gather from server request */
                            GatClientUtility.setResolvedUsername(response.getContent().getResolvedUsername());
                            
                            /* Load the course that was created */
                            startPart2(containerWidget, response.getContent().getImportedCoursePath());
                            logger.info("Start(): Setting read only to "+readOnly);
                            setReadOnly(readOnly);
                        }
                    }
                    
                });
                return;
                
            } else {
            
            startPart2(containerWidget, null);
            logger.info("Start(): Setting read only to "+readOnly);
            setReadOnly(readOnly);
            return;
        }
        }

        final String userName = GatClientUtility.getUserName();
        final String browserSessionKey = GatClientUtility.getBrowserSessionKey();
        
        AsyncCallback<LockFileResult> callback =  new AsyncCallback<LockFileResult>() {
            @Override
            public void onFailure(Throwable t) {

                logger.info("lockcourse onFailure - "+t);

                fileAlreadyLocked = true;  //not setting this will allow determineAndsetReadOnly() to change the read only flag to true
                startPart2(containerWidget, path);
                setReadOnly(true);
            }
            @Override
            public void onSuccess(LockFileResult result) {                
        
                //Handle a server side failure, treat as read-only
                if(!result.isSuccess()) {
                    StringBuilder sb = new StringBuilder("Failed to lock course (starting presenter): ")
                            .append(result.getErrorMsg()).append("\n")
                            .append(result.getErrorDetails()).append("\n")
                            .append(result.getErrorStackTrace());
                    logger.severe(sb.toString());

                    fileAlreadyLocked = true;  //not setting this will allow determineAndsetReadOnly() to change the read only flag to true
                    startPart2(containerWidget, path);
                    setReadOnly(true);
                    refreshData();
                    
                    return;
                }
                
                //Determine whether or not the user has secured the ability to write to the course
                boolean hasWritability = false;
                if(result.getCourseFileAccessDetails() != null) {
                    logger.info("lockcourse result -\n"+result);
                    List<CourseFileUserPermissionsDetails> accessList = new ArrayList<>(result.getCourseFileAccessDetails().getUsersPermissions().values());
                    editCourseView.setUserAccessList(accessList);
                    
                    hasWritability = result.getCourseFileAccessDetails().userHasWritePermission(userName, browserSessionKey);
                }
                
                //Run the GAT in edit or read-only mode depending on whether or not write permission was secured
                if(hasWritability) {                    
                    hasAcquiredCourseLock = true;
                    startPart2(containerWidget, path);
                    setReadOnly(false);
                    refreshData();
                } else {
                    fileAlreadyLocked = true; //not setting this will allow determineAndsetReadOnly() to change the read only flag to true
                    startPart2(containerWidget, path);
                    setReadOnly(true);
                    refreshData();
                }
            }
        };

        logger.info("lockCourse request start()");
        //Try to lock the course.
        LockCourse lockCourse = new LockCourse();
        lockCourse.setPath(path);
        lockCourse.setUserName(userName);
        lockCourse.setBrowserSessionKey(browserSessionKey);
        lockCourse.setInitialAcquisition(true);
        dispatchService.execute(lockCourse, callback);
    }
    
    private void startPart2(AcceptsOneWidget containerWidget, String path) {
        //Every minute remind the server that the file needs to 
        //be locked because we're still working on it.
        lockTimer.scheduleRepeating(60000);
        
        refreshTimer.scheduleRepeating(15000);
        
        editCourseView.initializeView();        
        setupView(containerWidget, editCourseView.asWidget());        
        eventRegistration = eventBinder.bindEventHandlers(this, eventBus);
        IFrameMessageHandlerChild.getInstance().sendMessage(new IFrameSimpleMessage(IFrameMessageType.GAT_LOADED));
        doGetCourseObject(path);
        
        //Link that data provider to the list.
        conceptListDataProvider.addDataDisplay(editCourseView.getHeaderView().getSimpleConceptList());
        
        //Link the data provider to the list
        HasData<LtiProvider> ltiProviderDisplay = editCourseView.getHeaderView().getLtiProviderList();
        ltiProviderListDataProvider.addDataDisplay(ltiProviderDisplay);
        
        //Setup the sort handler
        ListHandler<Concepts.List.Concept> sortHandler = new ListHandler<Concepts.List.Concept>(conceptListDataProvider.getList());
        editCourseView.getHeaderView().setSortHandlerForConceptList(sortHandler);

        exposeNativeFunctions();
    }

    /** 
     * This method gets called when we're jumping back to the dashboard (basically when the GAT is being unloaded/closing).
     * There should be no attempt at trying to communicate with the server via RPC or Dispatch because there is no guarantee
     * that the server will receive the message. Any logic in here should be client side only and should be short.  The browser
     * can terminate this thread at any point at its discretion.
     * 
     * @see mil.arl.gift.tools.authoring.gat.client.presenter.AbstractGatPresenter#stop()
     */
    @Override
    public void stop() { 
        
        lastStopTimeMS = System.currentTimeMillis(); 
        logger.info("CoursePresenter.stop() - time = "+lastStopTimeMS);
        
        //even know there is a chance the server won't receive this request, in the situations where it works
        //it will prevent having to wait for a lock timeout to occur.  Currently this case will only happen
        //for different GAT instances with write permissions that try to access the course after the user with
        //write access has left the GAT.
        unlockFile(true);
        
        super.stop();
        
        lockTimer.cancel();
        refreshTimer.cancel();
    }
    
    /**
     * Sets the file path and updates the GUI to reflect the
     * new path.
     * @param path The path to the course.xml
     */
    private void setFilePath(String path) {
        filePath = path;
        
        editCourseView.showReadOnlyLabel(readOnly);
        editCourseView.getCoursePathLabel().setText(getCourseFolderPath());
        
        //Let the file selection dialog know which folder files should be
        //uploaded/copied to.
        String courseFolderPath = getCourseFolderPath();
        exposeCoursePathToChildFrames(courseFolderPath);
        DefaultGatFileSelectionDialog.courseFolderPath = courseFolderPath;
        editCourseView.refreshMediaList();
        
        if(currentCourse != null){
            
            //keep track of old course names in case the author renames the course
            originalCourseName = currentCourse.getName();
        }
        
        /* Get a URL that can be used to reach files inside the course folder. This is needed to allow the survey
         * composer to reach media files in the course folder. */
        rpcService.getAssociatedCourseImage(GatClientUtility.getUserName(), courseFolderPath, new AsyncCallback<FetchContentAddressResult>() {
            
            @Override
            public void onSuccess(FetchContentAddressResult result) {
                exposeCourseFolderUrlToChildFrames(result.getContentURL());
            }
            
            @Override
            public void onFailure(Throwable caught) {
                logger.severe("Failed to get a URL for the course folder. This may affect previewing some course resources. " + caught);
            }
        });
    }
    
    
    /**
     * Exposes the given course path to any editor frames opened within this editor. This is used to allow sub-editors opened in
     * dialogs to locate the course folder where course object files should be saved to.
     * 
     * @param path the path to expose
     */
    private native void exposeCoursePathToChildFrames(String path)/*-{
        $wnd.editorBaseCoursePath = path;
    }-*/;
    
    /**
     * Exposes the given course folder URL to any editor frames opened within this editor. This is used to allow 
     * sub-editors opened in dialogs to reach files in the course folder, particularly for the survey composer
     * 
     * @param url the URL of the course folder.
     */
    private native void exposeCourseFolderUrlToChildFrames(String url)/*-{
        $wnd.editorBaseCourseUrl = url;
    }-*/;

    private native void closePreviewWindow() /*-{
        if($wnd.previewWindow != null) {
            $wnd.previewWindow.close();
        }
    }-*/;

    /**
     * Sets the visibility of the cancel button on the GIFT Wrap dialog modal
     * 
     * @param visible true to show the cancel button; false to hide it
     */
    private void setGIFTWrapDialogCancelButtonVisibility(final boolean visible) {
        // exit early case to protect from nulls
        if (editCourseView == null || editCourseView.getGiftWrapDialog() == null) {
            return;
        }

        editCourseView.getGiftWrapDialog().setCancelButtonVisible(visible);
    }

    /**
     * Imports the training application object from the specified path.
     * 
     * @param path The path to the training application object
     */
    private void importTrainingApp(final String path) {
        final ImportTrainingApplicationObject action = new ImportTrainingApplicationObject(
                GatClientUtility.getUserName(), currentCourse.getSurveyContext().intValue(), path,
                getCourseFolderPath(), GatClientUtility.getBrowserSessionKey());

        importTrainingApp(action);
    }

    /**
     * Uses the provided action to import the training application.
     * 
     * @param action the action containing the source and destination paths used to import the
     *        training application.
     */
    private void importTrainingApp(final ImportTrainingApplicationObject action) {
        BsLoadingDialogBox.display("Importing Course Object", "Please wait...");

        SharedResources.getInstance().getDispatchService().execute(action,
                new AsyncCallback<ImportTrainingApplicationObjectResult>() {
                    @Override
                    public void onFailure(Throwable t) {
                        BsLoadingDialogBox.remove();
                        ErrorDetailsDialog dialog = new ErrorDetailsDialog(
                                "Failed to import the external application because an error occurred on the server.",
                                t.getMessage(), null);
                        dialog.setText("Import Failed");
                        dialog.center();
                    }

                    @Override
                    public void onSuccess(ImportTrainingApplicationObjectResult result) {
                        BsLoadingDialogBox.remove();
                        if (result.isSuccess()) {
                            if (result.isFoundConflict()) {
                                FileTreeModel sourceModel = FileTreeModel
                                        .createFromRawPath(action.getRealTimeAssessmentFolder());
                                OkayCancelDialog.show("Conflict Found", "The real time assessment '"
                                        + sourceModel.getFileOrDirectoryName()
                                        + "' being imported already exists in the course '" + currentCourse.getName()
                                        + "'. If you choose to proceed with the import, the real time assessment will be given a unique name, but any conflicting support files will overwrite the existing ones (e.g. media files).<br/><br/>Do you wish to proceed?",
                                        "Proceed", new OkayCancelCallback() {
                                            @Override
                                            public void okay() {
                                                action.setOverwrite(true);
                                                importTrainingApp(action);
                                            }

                                            @Override
                                            public void cancel() {
                                                // do nothing. GIFT Wrap is still being shown.
                                            }
                                        });
                            } else {
                                eventBus.fireEvent(new TrainingApplicationImportEvent(
                                        result.getTaWrapper().getTrainingApplication()));
                            }
                        } else {
                            ErrorDetailsDialog dialog = new ErrorDetailsDialog(result.getErrorMsg(),
                                    result.getErrorDetails(), result.getErrorStackTrace());
                            dialog.setText("Import Failed");
                            dialog.center();
                        }
                    }
                });
    }
    
    /**
     * Notifies all listeners that the scenario file with the given path has been saved
     * 
     * @param filePath the workspace-relative path of the scenario file. Cannot be null.
     */
    public void onScenarioSaved(String filePath) {
        eventBus.fireEvent(new ScenarioSavedEvent(filePath));
    }
    
    /**
     * Loads this course's concepts and displays the editor used to modify them
     */
    private void showCourseConceptsEditor() {
        
        if(currentCourse != null) {
            editCourseView.showConceptsEditor(currentCourse.getConcepts());
        }
    }
    
    /**
     * Loads this course's concepts and displays the editor used to modify them
     */
    private void showCourseConceptsEditor(JavaScriptObject windowToNotify) {
        
        if(currentCourse != null) {
            this.editorToNotifyOnConceptsChange = windowToNotify;
            showCourseConceptsEditor();
        }
    }
    
    /**
     * Notifies the currently listening editor that the course's concepts have changed. This can
     * be useful for refreshing course concept data for editors in separate frames, such as the 
     * DKF editor.
     */
    private void notifyListenerEditorWhenConceptsChange() {
        notifyEditorWhenConceptsChange(editorToNotifyOnConceptsChange);
        editorToNotifyOnConceptsChange = null;
    }
    
    /**
     * Notifies the editor in the given window that the course's concepts have changed. This can
     * be useful for refreshing course concept data for editors in separate frames, such as the 
     * DKF editor.
     */
    private native void notifyEditorWhenConceptsChange(JavaScriptObject editorWnd)/*-{
        
        if(editorWnd != null){
            editorWnd.onCourseConceptsChanged();
        }
    }-*/;

    /**
     * Defines javascript methods
     */
    private native void exposeNativeFunctions()/*-{
    
        var that = this;
    
        $wnd.importTrainingApp = $entry(function(path){
            that.@mil.arl.gift.tools.authoring.server.gat.client.presenter.course.CoursePresenter::importTrainingApp(Ljava/lang/String;)(path);
        });

        $wnd.setGIFTWrapDialogCancelButtonVisibility = $entry(function(visible){
            that.@mil.arl.gift.tools.authoring.server.gat.client.presenter.course.CoursePresenter::setGIFTWrapDialogCancelButtonVisibility(Z)(visible);
        });
        
        $wnd.saveCourse = $entry(function(){
            that.@mil.arl.gift.tools.authoring.server.gat.client.presenter.course.CoursePresenter::saveCourse()();
        });
        
        $wnd.saveCourseAndNotify = $entry(function(){
            that.@mil.arl.gift.tools.authoring.server.gat.client.presenter.course.CoursePresenter::saveCourseAndNotify()();
        });
        
        $wnd.previewCourse = $entry(function(index){
            that.@mil.arl.gift.tools.authoring.server.gat.client.presenter.course.CoursePresenter::previewCourse(I)(index);
        });
        
        $wnd.closePreviewWindow = $entry(function(){
            that.@mil.arl.gift.tools.authoring.server.gat.client.presenter.course.CoursePresenter::closePreviewWindow()();
        });
                
        $wnd.getBaseCourseSurveyContextId = $entry(function(){
            return that.@mil.arl.gift.tools.authoring.server.gat.client.presenter.course.CoursePresenter::getBaseCourseSurveyContextId()();
        });
        
        $wnd.getBaseCourseConcepts = $entry(function(){
            return that.@mil.arl.gift.tools.authoring.server.gat.client.presenter.course.CoursePresenter::getBaseCourseConcepts()();
        });
        
        $wnd.getBaseLtiProviders = $entry(function(){
            return that.@mil.arl.gift.tools.authoring.server.gat.client.presenter.course.CoursePresenter::getBaseLtiProviders()();
        });
        
        $wnd.getBaseCourseName = $entry(function(){
            return that.@mil.arl.gift.tools.authoring.server.gat.client.presenter.course.CoursePresenter::getCourseName()();
        });
        
        $wnd.onScenarioSaved = $entry(function(path){
            that.@mil.arl.gift.tools.authoring.server.gat.client.presenter.course.CoursePresenter::onScenarioSaved(Ljava/lang/String;)(path);
        });
        
         $wnd.showCourseConceptsEditor = $entry(function(wnd){
            that.@mil.arl.gift.tools.authoring.server.gat.client.presenter.course.CoursePresenter::showCourseConceptsEditor(Lcom/google/gwt/core/client/JavaScriptObject;)(wnd);
        });
        
    }-*/;
    
    
    /**
     * Returns the workspace relative path to the course folder.
     * 
     * @return String - workspace relative path to the course folder.  An empty string is returned if the folder cannot be found.
     */
    private String getCourseFolderPath() {
        String courseFolderPath = "";
        if (filePath != null && !filePath.isEmpty()) {
            courseFolderPath = filePath.substring(0,  filePath.lastIndexOf(Constants.FORWARD_SLASH));
        }
        
        return courseFolderPath;
    }
    
    public void setReadOnly(boolean readOnly){
        
        GatClientUtility.setReadOnly(readOnly);
        logger.info("CoursePresenter::setReadOnly() readOnly = " + readOnly);
        this.readOnly = readOnly;
        IFrameMessageHandlerChild.getInstance().sendMessage(
                new IFrameSimpleMessage( readOnly ? IFrameMessageType.GAT_FILES_CLOSED : IFrameMessageType.GAT_FILES_OPEN ));
        editCourseView.setReadOnly(readOnly);
        editCourseView.getCourseTree().setReadOnly(readOnly);
        editCourseView.getHeaderView().setReadOnly(readOnly);
        
        
        // Update the tooltips for the header.
        HeaderView header = editCourseView.getHeaderView();
        
        // Tooltips are disabled by default.
        header.setLearnerTooltipVisibility(readOnly);
        header.setPedTooltipVisibility(readOnly);
        
        // Course image tooltip is visible if readonly is true.
        header.setCourseImageTooltipVisibility(readOnly);
        
        //Creates a new context menu for readOnly mode
        if(readOnly) {
            logger.info("Setting course object node menu with read-only choices: 'View'");
            transitionNodeContextMenu = new ContextMenu();
            transitionNodeContextMenu.getMenu().addItem(
                    "<i class='fa fa-edit' style='margin-right: 5px; font-size: 20px; vertical-align: middle;'></i>"
                +   "<span style='vertical-align: middle;'>"
                +       "View"
                +   "</span>", true, new  Scheduler.ScheduledCommand(){
    
                    @Override
                    public void execute() {
                        
                        mil.arl.gift.tools.authoring.server.gat.client.view.course.tree.TreeNode node = 
                                editCourseView.getCourseTree().getSelectedNode();
                        
                        Serializable courseObject = editCourseView.getCourseTree().getCourseObject(node);
                        
                        if(node != null){
                            displaySelectedTransition(courseObject);
                        }
                    
                        transitionNodeContextMenu.hide();
                    }
                    
                }
            );
            transitionAuthoredBranchNodeContextMenu = new ContextMenu();
            transitionAuthoredBranchNodeContextMenu.getMenu().addItem(
                    "<i class='fa fa-edit' style='margin-right: 5px; font-size: 20px; vertical-align: middle;'></i>"
                +   "<span style='vertical-align: middle;'>"
                +       "View"
                +   "</span>", true, new  Scheduler.ScheduledCommand(){
    
                    @Override
                    public void execute() {
                        
                        mil.arl.gift.tools.authoring.server.gat.client.view.course.authoredbranch.tree.TreeNode node = 
                                editCourseView.getTreeManager().getSelectedNode();
                        
                        Serializable courseObject = editCourseView.getTreeManager().getCourseObject(node);
                        
                        if(node != null){
                            displaySelectedTransition(courseObject);
                        }
                    
                        transitionAuthoredBranchNodeContextMenu.hide();
                    }
                    
                }
            );
        }else{
            logger.info("Setting course object node menu with choices: 'Edit', 'Delete', 'Copy'");
            transitionNodeContextMenu = new ContextMenu();
            transitionNodeContextMenu.getMenu().addItem(
                    "<i class='fa fa-edit' style='margin-right: 5px; font-size: 20px; vertical-align: middle;'></i>"
                +    "<span style='vertical-align: middle;'>"
                +         "Edit"
                +     "</span>", true, new  Scheduler.ScheduledCommand(){

                    @Override
                    public void execute() {
                        
                        mil.arl.gift.tools.authoring.server.gat.client.view.course.tree.TreeNode node = 
                                editCourseView.getCourseTree().getSelectedNode();
                        
                        Serializable courseObject = editCourseView.getCourseTree().getCourseObject(node);
                        
                        if(node != null){
                            displaySelectedTransition(courseObject);
                    }
                    
                        transitionNodeContextMenu.hide();
                }
                    
                }
            );
            
            if(!GatClientUtility.isRtaLessonLevel()) {
            
                transitionNodeContextMenu.getMenu().addItem(
                        "<i class='fa fa-trash' style='margin-right: 5px; font-size: 20px; vertical-align: middle;'></i>"
                    +    "<span style='vertical-align: middle;'>"
                    +         "Delete"
                    +     "</span>", true, new  Scheduler.ScheduledCommand(){
    
                        @Override
                        public void execute() {
                            
                            mil.arl.gift.tools.authoring.server.gat.client.view.course.tree.TreeNode node = 
                                    editCourseView.getCourseTree().getSelectedNode();
                                                
                            if(node != null){
                                transitionBeingDragged = editCourseView.getCourseTree().getCourseObject(node);
                                editCourseView.getCourseTree().deleteNode(node, null);
                            }
                            
                            transitionNodeContextMenu.hide();
                        }
                        
                    }
                );
                
                transitionNodeContextMenu.getMenu().addItem(
                        "<i class='fa fa-files-o' style='margin-right: 5px; font-size: 20px; vertical-align: middle;'></i>"
                    +    "<span style='vertical-align: middle;'>"
                    +         "Copy"
                    +     "</span>", true, new  Scheduler.ScheduledCommand(){
    
                        @Override
                        public void execute() {
                            
                            mil.arl.gift.tools.authoring.server.gat.client.view.course.tree.TreeNode node = 
                                    editCourseView.getCourseTree().getSelectedNode();
                            
                            try {
                                Serializable transition = editCourseView.getCourseTree().getCourseObject(node);
                                
                                // Copying Authored Branch is currently unsupported
                                if (transition instanceof AuthoredBranch) {
                                    WarningDialog.error("Unsupported Action", "Copying Authored Branch course objects is not currently supported.");
                                    transitionNodeContextMenu.hide();
                                    return;
                                }
                                
                                String sourceObjectName = CourseElementUtil.getTransitionName(transition);
                                String copyName = copyTransitionName(sourceObjectName);
                                
                                logger.info("Copying course object named '"+sourceObjectName+"' to new course object named '"+copyName+"'.");
                                final int index = currentCourse.getTransitions().getTransitionType().indexOf(transition) + 1;
                            
                                CourseElementUtil.copyCourseObject(transition, copyName, new CopyCourseObjectCallback() {
    
                                    @Override
                                    public void onCopy(Serializable copiedTransition) {
                                        currentCourse.getTransitions().getTransitionType().add(index, copiedTransition);
                                        transitionNodeContextMenu.hide();
                                    
                                        //save to update the view
                                        saveCourse(filePath, true);        
                                    }
                                
                                });
                                
                            } catch(Exception e) {
                                logger.log(Level.SEVERE, "There was a problem copying the course object.", e);
                                
                                //redraw the tree
                                editCourseView.getCourseTree().updateTree();
                            }
                        }
                        
                    }
                );
            }
            
            transitionAuthoredBranchNodeContextMenu = new ContextMenu();
            transitionAuthoredBranchNodeContextMenu.getMenu().addItem(
                    "<i class='fa fa-edit' style='margin-right: 5px; font-size: 20px; vertical-align: middle;'></i>"
                +    "<span style='vertical-align: middle;'>"
                +         "Edit"
                +     "</span>", true, new  Scheduler.ScheduledCommand(){

                    @Override
                    public void execute() {
                        
                        mil.arl.gift.tools.authoring.server.gat.client.view.course.authoredbranch.tree.TreeNode node = 
                                editCourseView.getTreeManager().getBranchTree(editCourseView.getTreeManager().getCurrentBranch()).getSelectedNode();
                        
                        Serializable courseObject = editCourseView.getTreeManager().getCourseObject(node);
                        
                        if(node != null){
                            displaySelectedTransition(courseObject);
                        }
                    
                        transitionAuthoredBranchNodeContextMenu.hide();
                    }
                    
                }
            );
            
            transitionAuthoredBranchNodeContextMenu.getMenu().addItem(
                    "<i class='fa fa-trash' style='margin-right: 5px; font-size: 20px; vertical-align: middle;'></i>"
                +    "<span style='vertical-align: middle;'>"
                +         "Delete"
                +     "</span>", true, new  Scheduler.ScheduledCommand(){

                    @Override
                    public void execute() {
                        
                        mil.arl.gift.tools.authoring.server.gat.client.view.course.authoredbranch.tree.TreeNode node = 
                                editCourseView.getTreeManager().getBranchTree(editCourseView.getTreeManager().getCurrentBranch()).getSelectedNode();
                        
                        if(node != null){
                            transitionBeingDragged = editCourseView.getTreeManager().getCourseObject(node);
                            editCourseView.getTreeManager().getBranchTree(editCourseView.getTreeManager().getCurrentBranch()).deleteNode(node, null);
                        }
                        
                        transitionAuthoredBranchNodeContextMenu.hide();
                    }
                    
                }
            );
            
            transitionAuthoredBranchNodeContextMenu.getMenu().addItem(
                    "<i class='fa fa-files-o' style='margin-right: 5px; font-size: 20px; vertical-align: middle;'></i>"
                +    "<span style='vertical-align: middle;'>"
                +         "Copy"
                +     "</span>", true, new  Scheduler.ScheduledCommand(){

                    @Override
                    public void execute() {
                        
                        mil.arl.gift.tools.authoring.server.gat.client.view.course.authoredbranch.tree.TreeNode node = 
                                editCourseView.getTreeManager().getBranchTree(editCourseView.getTreeManager().getCurrentBranch()).getSelectedNode();

                        try {
                            Serializable transition = editCourseView.getTreeManager().getCourseObject(node);

                            // Copying Authored Branch is currently unsupported
                            if (transition instanceof AuthoredBranch) {
                                WarningDialog.error("Unsupported Action", "Copying Authored Branch course objects is not currently supported.");
                                transitionAuthoredBranchNodeContextMenu.hide();
                                return;
                            }

                            String sourceObjectName = CourseElementUtil.getTransitionName(transition);
                            String copyName = copyTransitionName(sourceObjectName);
                            
                            logger.info("Copying course object named '"+sourceObjectName+"' to new course object named '"+copyName+"'.");
                            int foundIndex = -1;
                            Path foundPathStart = null;
                            
                            // Find the path start of the path that contains the course object
                            for (Path path : editCourseView.getTreeManager().getCurrentBranch().getPaths().getPath()) {
                                foundPathStart = path;
                                // Find the index of the course object in the path
                                for (int i=0; i<path.getCourseobjects().getAAROrAuthoredBranchOrEnd().size(); i++) {
                                    Serializable obj = path.getCourseobjects().getAAROrAuthoredBranchOrEnd().get(i);
                                    if (obj == transition) {
                                        foundIndex = i;
                                        break;
                                    }
                                }
                                
                                // Found the index
                                if (foundIndex != -1) {
                                    break;
                                }
                            }
                            
                            final int index = foundIndex + 1;
                            final Path pathStart = foundPathStart;
                        
                            CourseElementUtil.copyCourseObject(transition, copyName, new CopyCourseObjectCallback() {

                                @Override
                                public void onCopy(Serializable copiedTransition) {

                                    editCourseView.getTreeManager().getBranchTree(editCourseView.getTreeManager().getCurrentBranch()).addToPath(pathStart, copiedTransition, index);

                                    transitionAuthoredBranchNodeContextMenu.hide();
                                
                                    //save to update the view
                                    saveCourse(filePath, true);        

                                    editCourseView.getTreeManager().showBranchTree(editCourseView.getTreeManager().getCurrentBranch());
                                }
                            
                            });
                            
                        } catch(Exception e) {
                            logger.log(Level.SEVERE, "There was a problem copying the course object.", e);
                            
                            //redraw the tree
                            editCourseView.getCourseTree().updateTree();
                        }
                    }
                    
                }
            );
        }
    }

    
    /*
     * updates the disk space label and then updates the parent IFrame, currently the
     * GAT Dashboard. Should be called whenever the current course goes through any changes
     */
    private void refreshData(){
        logger.info("Course Presenter refreshData() called");
        updateDiskSpace();
    }
    
    /**
     * Updates the disk space label. 
     */
    private void updateDiskSpace() {
        GetRemainingDiskSpace action = new GetRemainingDiskSpace(GatClientUtility.getUserName());
    
        SharedResources.getInstance().getDispatchService().execute(action, new AsyncCallback<GetRemainingDiskSpaceResult>() {

            @Override
            public void onFailure(Throwable thrown) {
                editCourseView.getDiskSpaceLabel().setHTML("Remaining Space: <div style=\"color: #ae493a; display: inline;\">ERROR</div>");
                logger.fine("Caught exception while fetching disk space: " + thrown.getMessage());
            }

            @Override
            public void onSuccess(GetRemainingDiskSpaceResult result) {
                if(result.isSuccess()) {
                    editCourseView.getDiskSpaceLabel().setHTML("Remaining Space: " + result.getDiskSpace());
                } else {
                    editCourseView.getDiskSpaceLabel().setHTML("Remaining Space: <div style=\"color: #ae493a; display: inline;\">ERROR</div>");
                    logger.fine("Unable to retrieve disk space because: " + result.getErrorMsg());
                }
            }
            
            
        });
    }
    
    /**
     * Validates the specified file and displays a dialog containing validation 
     * errors to the client.
     * 
     * @param filePath The path of the file to validate.
     * @param userName The user validating the file.
     * @param busyNotification an optional notification for this method to hide when validation is complete
     */
    private void validateFile(String filePath, final String userName, final Notify busyNotification) {
        
        ValidateFile action = new ValidateFile();
        
        action.setRelativePath(filePath);
        action.setUserName(userName);
        
        BsLoadingDialogBox.display("Validating", "Validating Course, please wait...");
        
        dispatchService.execute(action, new AsyncCallback<ValidateFileResult>() {

            @Override
            public void onFailure(Throwable cause) {
                
                if(busyNotification != null){
                    busyNotification.hide();
                }
                
                BsLoadingDialogBox.remove();
                                
                ErrorDetailsDialog dialog = new ErrorDetailsDialog(
                        "Failed to validate the file.", 
                        cause.getMessage(), 
                        DetailedException.getFullStackTrace(cause));
                dialog.setDialogTitle("Validation Failed");
                dialog.center();
            }

            @Override
            public void onSuccess(ValidateFileResult result) {
                BsLoadingDialogBox.remove();
                
                logger.info("Validation call has returned from server.");
                
                if(result.isSuccess()) {
                    
                    Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                        
                        @Override
                        public void execute() {
                            
                            NotifySettings settings = NotifyUtil.generateDefaultSettings();
                            settings.setType(NotifyType.SUCCESS);
                            
                            Notify.notify("Validation Complete.", "No errors found.", IconType.CHECK_CIRCLE, settings);
                            
                            if(busyNotification != null){
                                busyNotification.hide();
                            }
                            
                            // update course tree, we need to remove the 'error' icons from the course objects.
                            if (editCourseView != null && editCourseView.getCourseTree() != null) {
                                // clear course tree validation map by passing in null
                                editCourseView.getCourseTree().setValidationResults(null);
                                editCourseView.getCourseTree().updateTree();
                            }
                        }
                    });
                    
                    
                } else {
                    
                    if(busyNotification != null){
                        busyNotification.hide();
                    }
                    
                    // update course tree.
                    if (editCourseView != null && editCourseView.getCourseTree() != null) {
                        editCourseView.getCourseTree().setValidationResults(result);
                        editCourseView.getCourseTree().updateTree();
                    }
                    
                    if (result.getCourseValidationResults() != null) {
                        logger.info("Creating course validation issue dialog.");
                        FileValidationDialog dialog = new FileValidationDialog(null, "There are validation issues with this course.", result.getCourseValidationResults(), false);
                        dialog.setText("Validation Errors");
                        dialog.center();
                    }
                    else {
                        FileValidationDialog dialog = new FileValidationDialog(null, "There are validation issues with this course.", null, false);
                        dialog.setText("Validation Errors");
                        dialog.center();
                    }
                } 
                
                updateLastSuccessfulValidation(result.getLastSuccessfulValidationDate());
            }            
        });
    }
    
    /**
     * Return the current course name.  
     * 
     * @return the name of the course.  Will be null if the course data is set.
     */
    private String getCourseName(){
        return currentCourse != null ? currentCourse.getName() : null;
    }
    
    /**
     * Gets summaries of all the references to the given concept 
     * 
     * @param concept the name of the concept
     * @return the reference summaries
     */
    private List<String> getConceptReferences(String concept){
        
        List<String> references = new ArrayList<String>();
        
        if(currentCourse != null && currentCourse.getTransitions() != null){
            
            for(Serializable transition : currentCourse.getTransitions().getTransitionType()){
                
                if(transition instanceof PresentSurvey){
                    
                    PresentSurvey ps = (PresentSurvey) transition;
                    
                    boolean foundReferences = false;
                    
                    if(ps.getSurveyChoice() != null && ps.getSurveyChoice() instanceof ConceptSurvey){
                        
                        ConceptSurvey cs = (ConceptSurvey) ps.getSurveyChoice();
                        
                        if(cs.getConceptQuestions() != null){
                            
                            for(ConceptQuestions questions : cs.getConceptQuestions()){
                                
                                if(questions.getName() != null && questions.getName().equals(concept)){
                                    foundReferences = true;
                                    break;
                                }
                            }
                        }                        
                    }
                    
                    if(foundReferences){
                        
                        StringBuilder sb = new StringBuilder();
                        
                        sb.append("A present survey transition ");
                        
                        if(ps.getTransitionName() != null && !ps.getTransitionName().isEmpty()){
                            sb.append(" named <b>");
                            sb.append(ps.getTransitionName());
                            sb.append("</b>");
                            
                        } else {
                            sb.append(" at index <b>");
                            sb.append(currentCourse.getTransitions().getTransitionType().indexOf(ps));
                            sb.append("</b>");
                        }
                        
                        references.add(sb.toString());
                    }
                } else if (transition instanceof LessonMaterial) {
                    LessonMaterial lm = (LessonMaterial) transition;
                    
                    boolean foundReferences = false;

                    if (lm.getLessonMaterialList() != null && lm.getLessonMaterialList().getMedia() != null) {

                        for (Media media : lm.getLessonMaterialList().getMedia()) {
                            if (media.getMediaTypeProperties() != null && media.getMediaTypeProperties() instanceof LtiProperties) {
                                LtiProperties ltiProp = (LtiProperties) media.getMediaTypeProperties();
                                if (ltiProp.getLtiConcepts() != null && ltiProp.getLtiConcepts().getConcepts().contains(concept)) {
                                    foundReferences = true;
                                    break;
                                }
                            }
                        }

                        if (foundReferences) {

                            StringBuilder sb = new StringBuilder();

                            sb.append("A lesson material transition ");

                            if (lm.getTransitionName() != null && !lm.getTransitionName().isEmpty()) {
                                sb.append(" named <b>");
                                sb.append(lm.getTransitionName());
                                sb.append("</b>");

                            } else {
                                sb.append(" at index <b>");
                                sb.append(currentCourse.getTransitions().getTransitionType().indexOf(lm));
                                sb.append("</b>");
                            }

                            references.add(sb.toString());
                        }
                    }
                } else if(transition instanceof MerrillsBranchPoint){
                    
                    MerrillsBranchPoint mbp = (MerrillsBranchPoint) transition;
                    
                    boolean foundReferences = false;
                    
                    if(mbp.getConcepts() != null){
                            
                        if(mbp.getConcepts().getConcept().indexOf(concept) >= 0){
                            
                            if(!foundReferences){
                                foundReferences = true;
                            }
                            
                        }
                    }
                    
                    if(mbp.getQuadrants() != null){
                        
                        for(Serializable content : mbp.getQuadrants().getContent()){
                            
                            if(content instanceof Recall){
                                
                                Recall recall = (Recall) content;

                                if(recall.getPresentSurvey() != null){
                                    
                                    Recall.PresentSurvey ps = recall.getPresentSurvey();
                                    
                                    if(ps.getSurveyChoice() != null){
                                        
                                        Recall.PresentSurvey.ConceptSurvey cs = ps.getSurveyChoice();
                                        
                                        if(cs.getConceptQuestions() != null){
                                            
                                            for(ConceptQuestions questions : cs.getConceptQuestions()){
                                                
                                                if(questions.getName() != null && questions.getName().equals(concept)){
                                                    
                                                    if(!foundReferences){
                                                        foundReferences = true;
                                                        break;
                                                    }
                                                }
                                            }
                                        }                                        
                                    }
                                }
                                
                            } else if(content instanceof Practice){
                                
                                Practice practice = (Practice) content;
                                
                                if(practice.getPracticeConcepts() != null){
                                    
                                    if(practice.getPracticeConcepts().getCourseConcept().indexOf(concept) >= 0){
                                        
                                        if(!foundReferences){
                                            foundReferences = true;
                                        }
                                        
                                    }
                                }
                            }
                            
                            if(foundReferences){
                                break;
                            }
                        }
                    }
                    
                    if(foundReferences){
                        
                        StringBuilder sb = new StringBuilder();
                        
                        sb.append("A Merrill's branch point");
                        
                        if(mbp.getTransitionName() != null && !mbp.getTransitionName().isEmpty()){
                            sb.append(" named <b>");
                            sb.append(mbp.getTransitionName());
                            sb.append("</b>");
                            
                        } else {
                            sb.append(" at index <b>");
                            sb.append(currentCourse.getTransitions().getTransitionType().indexOf(mbp));
                            sb.append("</b>");
                        }
                        
                        references.add(sb.toString());
                    }
                }
            }
        }
        
        return references;
    }
    
    /**
     * Gets summaries of all the references to the given LTI provider 
     * 
     * @param ltiProviderId the identifier of the LTI provider
     * @return the reference summaries
     */
    private List<String> getLtiProviderReferences(String ltiProviderId){
        
        List<String> references = new ArrayList<String>();
        
        if(currentCourse != null && currentCourse.getTransitions() != null){
            
            for(Serializable transition : currentCourse.getTransitions().getTransitionType()){
                
                if(transition instanceof LessonMaterial){
                    
                    LessonMaterial lm = (LessonMaterial) transition;
                    
                    boolean foundReferences = false;
                    
                    if(lm.getLessonMaterialList() != null && lm.getLessonMaterialList().getMedia() != null){
                        
                        for (Media media : lm.getLessonMaterialList().getMedia()) {
                            if (media.getMediaTypeProperties() != null && media.getMediaTypeProperties() instanceof LtiProperties) {
                                LtiProperties ltiProp = (LtiProperties) media.getMediaTypeProperties();
                                if (ltiProviderId.equals(ltiProp.getLtiIdentifier())){
                                    foundReferences = true;
                                    break;
                                }
                            }
                        }
                        
                        if(foundReferences){
                            
                            StringBuilder sb = new StringBuilder();
                            
                            sb.append("A lesson material transition ");
                            
                            if(lm.getTransitionName() != null && !lm.getTransitionName().isEmpty()){
                                sb.append(" named <b>");
                                sb.append(lm.getTransitionName());
                                sb.append("</b>");
                                
                            } else {
                                sb.append(" at index <b>");
                                sb.append(currentCourse.getTransitions().getTransitionType().indexOf(lm));
                                sb.append("</b>");
                            }
                            
                            references.add(sb.toString());
                        } 
                    }
                } 
            }
        }
        
        return references;
    }
    
    /**
     * Gets whether or not adding a concept with the specified name will cause a naming conflict or not
     * 
     * @param conceptToChange the concept whose name is being changed
     * @param conceptName the name to check
     * @return whether or not a name conflict will occur
     */
    private boolean doesConceptNameCauseConflict(Serializable conceptToChange, String conceptName){
        
        if(conceptName != null
                && currentCourse != null
                && currentCourse.getConcepts() != null
                && currentCourse.getConcepts().getListOrHierarchy() != null){
            
            if(currentCourse.getConcepts().getListOrHierarchy() instanceof Concepts.List){
                
                Concepts.List list = (generated.course.Concepts.List) currentCourse.getConcepts().getListOrHierarchy();
                
                for(Concept concept : list.getConcept()){
                    if((conceptToChange == null || !concept.equals(conceptToChange)) 
                            && concept.getName() != null 
                            && concept.getName().trim().equalsIgnoreCase(conceptName)){
                        return true;
                    }
                }
                
            } else if(currentCourse.getConcepts().getListOrHierarchy() instanceof Concepts.Hierarchy){
                
                Concepts.Hierarchy hierarchy = (Hierarchy) currentCourse.getConcepts().getListOrHierarchy();
                
                return doesConceptNameCauseConflict(conceptToChange, conceptName, hierarchy.getConceptNode());
            }
        }
        
        return false;
    }

    /**
     * Determines if the identifier name is conflicting with an existing one.
     * 
     * @param identifier the identifier to check.
     * @return whether or not an identifier conflict will occur
     */
    private boolean doesLtiProviderIdentifierCauseConflict(String identifier) {

        // cannot have a null identifier
        if (identifier == null) {
            return true;
        }

        String trimmedIdentifier = identifier.trim();
        
        // didn't change the identifier when editing
        if (ltiProviderBeingEdited != null && trimmedIdentifier.equalsIgnoreCase(ltiProviderBeingEdited.getIdentifier())) {
            return false;
        }

        List<LtiProvider> courseLtiProviders = GatClientUtility.getCourseLtiProviders();
        if (courseLtiProviders != null) {
            for (LtiProvider provider : courseLtiProviders) {
                if (provider.getIdentifier().equalsIgnoreCase(trimmedIdentifier)) {
                    return true;
                }
            }
        }
            
        return false;
    }

    /**
     * Gets whether or not adding a concept with the specified name will cause a naming conflict with the given concept node or its children
     * 
     * @param conceptToChange the concept whose name is being changed
     * @param conceptName the name to check
     * @param conceptNode the current concept node in a hierarchy
     * @return whether or not a name conflict will occur
     */
    private boolean doesConceptNameCauseConflict(Serializable conceptToChange, String conceptName,
            ConceptNode conceptNode) {
        
        if((conceptToChange == null || !conceptNode.equals(conceptToChange)) 
                && conceptNode.getName() != null 
                && conceptNode.getName().trim().equalsIgnoreCase(conceptName)){
            return true;
            
        } else if(conceptNode.getConceptNode() != null){
            
            for(ConceptNode child : conceptNode.getConceptNode()){
                
                if(doesConceptNameCauseConflict(conceptToChange, conceptName, child)){
                    return true;
                }
            }
        }
        
        return false;
    }
    
    private List<String> getConceptNamesUnderNode(ConceptNode node){
        
        List<String> conceptNames = new ArrayList<String>();
        
        if(node.getName() != null){
            conceptNames.add(node.getName());
        }
        
        if(node.getConceptNode() != null){
            
            for(ConceptNode child : node.getConceptNode()){
                conceptNames.addAll(getConceptNamesUnderNode(child));
            }
        }
            
        return conceptNames;
    }
    
    /**
     * Checks the current scenario to make sure there aren't any references to the current survey context. 
     * 
     * @param surveyContext the survey context to switch to once it has been verified that the current survey context is not being referenced anywhere
     * @return true if there are survey context references. False, otherwise.
     */
    private boolean hasSurveyContextReferences(){
                
        if(currentCourse != null){
            
            List<String> references = new ArrayList<String>();
            
            if(currentCourse.getTransitions() != null){
            
                for(Serializable transition : currentCourse.getTransitions().getTransitionType()){
                    
                    if(transition instanceof PresentSurvey){
                        
                        PresentSurvey survey = (PresentSurvey) transition;
                        
                        if(survey.getSurveyChoice() != null && survey.getSurveyChoice() instanceof String){
                            
                            StringBuilder sb = new StringBuilder();
                            sb.append("A survey transition named <b>");
                            sb.append(survey.getTransitionName() != null ? survey.getTransitionName() : "UNNAMED");
                            sb.append("</b> references the current survey context.");
                            
                            references.add(sb.toString());
                        }
                    }                    
                }
            }
            
            if(references.isEmpty()){                         
                return false;
                
            } else {
                
                StringBuilder sb = new StringBuilder();
                sb.append("The following problems were detected while attempting to change the survey context:<br>");
                sb.append("<ul>");
                
                for(String referenceDescription : references){
                    sb.append("<li style='text-align: left;'>");
                    sb.append(referenceDescription);
                    sb.append("</li>");
                }
                
                sb.append("</ul><br>");
                
                sb.append("Please remove these references before attempting to change the survey context again.");
                
                WarningDialog.error("Survey References", sb.toString());
                return true;
            }
            
        } else {
            return false;
        }
    }
    
    /**
     * Gets the survey context ID for the base course in a format that can be passed over JSNI. This is useful for passing
     * survey context information into sub-editors that lie in separate iframes.
     * 
     * @return a JSNI-compatible representation of the survey context ID
     */
    public String getBaseCourseSurveyContextId(){
        
        if(currentCourse != null && currentCourse.getSurveyContext() != null){
            return currentCourse.getSurveyContext().toString();
        }
        
        return null;
    }
    
    /**
     * Gets the concepts for the base course in a format that can be passed over JSNI. This is useful for passing
     * concept information into sub-editors that lie in separate iframes.
     * 
     * @return a JSNI-compatible representation of the concepts
     */
    public JsArrayString getBaseCourseConcepts(){    
        
        if(currentCourse != null && currentCourse.getConcepts() != null && currentCourse.getConcepts().getListOrHierarchy() != null){
            
            if(currentCourse.getConcepts().getListOrHierarchy() instanceof Concepts.List){
                
                JsArrayString concepts = JavaScriptObject.createArray().cast();
                
                Concepts.List conceptList = (generated.course.Concepts.List) currentCourse.getConcepts().getListOrHierarchy();
                
                for(Concept concept : conceptList.getConcept()){
                    concepts.push(concept.getName());
                }
                
                return concepts;
                
            } else if(currentCourse.getConcepts().getListOrHierarchy() instanceof Concepts.Hierarchy){
                
                JsArrayString concepts = JavaScriptObject.createArray().cast();
                
                Concepts.Hierarchy conceptHierarchy = (generated.course.Concepts.Hierarchy) currentCourse.getConcepts().getListOrHierarchy();
                
                if(conceptHierarchy.getConceptNode() != null){
                    
                    List<String> conceptNames = getConceptNamesUnderNode(conceptHierarchy.getConceptNode());
                    
                    for(String concept : conceptNames){
                        concepts.push(concept);
                    }
                }
                
                return concepts;
            }
        }
        
        return null;
    }
    
    /**
     * Gets the course authored LTI providers for the base course in a format that can be passed over JSNI. This
     * is useful for passing LTI provider information into sub-editors that lie in separate iframes.
     * 
     * @return a JSNI-compatible representation of the LTI provider ids
     */
    public JsArray<LtiProviderJSO> getBaseLtiProviders() {

        if (currentCourse != null && currentCourse.getLtiProviders() != null) {

            JsArray<LtiProviderJSO> providers = JavaScriptObject.createArray().cast();
            for (LtiProvider provider : currentCourse.getLtiProviders().getLtiProvider()) {
                LtiProviderJSO jso = JavaScriptObject.createObject().cast();
                BooleanEnum protectClientData = BooleanEnum.TRUE.equals(provider.getProtectClientData()) ? BooleanEnum.TRUE : BooleanEnum.FALSE;
                jso.setValues(provider.getIdentifier(), provider.getKey(), provider.getSharedSecret(), protectClientData.value());
                providers.push(jso);
            }

            return providers;
        }

        return null;
    }
    
    /**
     * Generates a new training application of the specified type
     * 
     * @param type the type of training application to generate
     * @return the generated training application
     */
    private TrainingApplication generateTrainingApp(TrainingApplicationEnum type){
        
        if(TrainingAppUtil.trainingAppToInteropClassNames.get(type) != null){
            
            TrainingApplication trainingApp = new TrainingApplication();
            
            List<String> interopImpls = TrainingAppUtil.trainingAppToInteropClassNames.get(type);
            
            if(trainingApp.getInterops() == null) {
                trainingApp.setInterops(new Interops());
            }
            
            trainingApp.getInterops().getInterop().clear(); 
            
            for(String implClassName : interopImpls){
                
                Interop interop = new Interop();
                
                if(implClassName != null){                                        
                    
                    interop.setInteropImpl(implClassName);
                    
                    Class<?> interopImplInputClass = TrainingAppUtil.interopClassNameToInputClass.get(implClassName);
                    
                    boolean foundInterop = true;
                    
                    if(interopImplInputClass != null){
                        
                        if(interopImplInputClass.equals(SimpleExampleTAInteropInputs.class)){
                            
                            SimpleExampleTAInteropInputs.LoadArgs loadArgs = new SimpleExampleTAInteropInputs.LoadArgs();
                            
                            SimpleExampleTAInteropInputs inputs = new SimpleExampleTAInteropInputs();
                            inputs.setLoadArgs(loadArgs);
                            
                            InteropInputs interopInputs = new InteropInputs();
                            interopInputs.setInteropInput(inputs);
                            
                            interop.setInteropInputs(interopInputs);                
                        
                        } else if(interopImplInputClass.equals(DETestbedInteropInputs.class)){
                            
                            DETestbedInteropInputs.LoadArgs loadArgs = new DETestbedInteropInputs.LoadArgs();
                            
                            DETestbedInteropInputs inputs = new DETestbedInteropInputs();
                            inputs.setLoadArgs(loadArgs);
                            
                            InteropInputs interopInputs = new InteropInputs();
                            interopInputs.setInteropInput(inputs);
                            
                            interop.setInteropInputs(interopInputs);            
                        
                        } else if(interopImplInputClass.equals(VBSInteropInputs.class)){
                            
                            VBSInteropInputs.LoadArgs loadArgs = new VBSInteropInputs.LoadArgs();
                            
                            VBSInteropInputs inputs = new VBSInteropInputs();
                            inputs.setLoadArgs(loadArgs);
                            
                            if(!GatClientUtility.isRtaLessonLevel()){
                                // default to showing scenario name as choice for new vbs course object
                                // when not in RTA mode
                                loadArgs.setScenarioName("");
                            }
                            
                            InteropInputs interopInputs = new InteropInputs();
                            interopInputs.setInteropInput(inputs);
                            
                            interop.setInteropInputs(interopInputs);
                        
                        } else if(interopImplInputClass.equals(TC3InteropInputs.class)){
                            
                            TC3InteropInputs.LoadArgs loadArgs = new TC3InteropInputs.LoadArgs();
                            
                            TC3InteropInputs inputs = new TC3InteropInputs();
                            inputs.setLoadArgs(loadArgs);
                            
                            InteropInputs interopInputs = new InteropInputs();
                            interopInputs.setInteropInput(inputs);
                            
                            interop.setInteropInputs(interopInputs);
                            
                        } else if(interopImplInputClass.equals(PowerPointInteropInputs.class)){
                                
                            PowerPointInteropInputs.LoadArgs loadArgs = new PowerPointInteropInputs.LoadArgs();
                            
                            PowerPointInteropInputs inputs = new PowerPointInteropInputs();
                            inputs.setLoadArgs(loadArgs);
                            
                            InteropInputs interopInputs = new InteropInputs();
                            interopInputs.setInteropInput(inputs);
                            
                            interop.setInteropInputs(interopInputs);
                        
                        } else if(interopImplInputClass.equals(DISInteropInputs.class)){
                            
                            DISInteropInputs.LoadArgs loadArgs = new DISInteropInputs.LoadArgs();
                            
                            DISInteropInputs inputs = new DISInteropInputs();
                            inputs.setLoadArgs(loadArgs);
                            
                            InteropInputs interopInputs = new InteropInputs();
                            interopInputs.setInteropInput(inputs);
                            
                            interop.setInteropInputs(interopInputs);
                            
                        } else if(interopImplInputClass.equals(VREngageInteropInputs.class)){
                            
                            VREngageInteropInputs.LoadArgs loadArgs = new VREngageInteropInputs.LoadArgs();
                            
                            VREngageInteropInputs inputs = new VREngageInteropInputs();
                            inputs.setLoadArgs(loadArgs);
                            
                            InteropInputs interopInputs = new InteropInputs();
                            interopInputs.setInteropInput(inputs);
                            
                            interop.setInteropInputs(interopInputs);
                            
                        } else if(interopImplInputClass.equals(UnityInteropInputs.class)){
                            
                            UnityInteropInputs.LoadArgs loadArgs = new UnityInteropInputs.LoadArgs();
                            
                            UnityInteropInputs inputs = new UnityInteropInputs();
                            inputs.setLoadArgs(loadArgs);
                            
                            InteropInputs interopInputs = new InteropInputs();
                            interopInputs.setInteropInput(inputs);
                            
                            interop.setInteropInputs(interopInputs);
                        
                        } else if(interopImplInputClass.equals(HAVENInteropInputs.class)){
                            
                            HAVENInteropInputs.LoadArgs loadArgs = new HAVENInteropInputs.LoadArgs();
                            
                            HAVENInteropInputs inputs = new HAVENInteropInputs();
                            inputs.setLoadArgs(loadArgs);
                            
                            InteropInputs interopInputs = new InteropInputs();
                            interopInputs.setInteropInput(inputs);
                            
                            interop.setInteropInputs(interopInputs);
                            
                        } else if(interopImplInputClass.equals(RIDEInteropInputs.class)){
                            
                            RIDEInteropInputs.LoadArgs loadArgs = new RIDEInteropInputs.LoadArgs();
                            
                            RIDEInteropInputs inputs = new RIDEInteropInputs();
                            inputs.setLoadArgs(loadArgs);
                            
                            InteropInputs interopInputs = new InteropInputs();
                            interopInputs.setInteropInput(inputs);
                            
                            interop.setInteropInputs(interopInputs);
                            
                        } else if(interopImplInputClass.equals(GenericLoadInteropInputs.class)){
                            
                            GenericLoadInteropInputs.LoadArgs loadArgs = new GenericLoadInteropInputs.LoadArgs();
                            
                            GenericLoadInteropInputs inputs = new GenericLoadInteropInputs();
                            inputs.setLoadArgs(loadArgs);
                            
                            InteropInputs interopInputs = new InteropInputs();
                            interopInputs.setInteropInput(inputs);
                            
                            interop.setInteropInputs(interopInputs);
                        
                        } else {                                
                            foundInterop = true;            
                        }
                        
                    } else {                        
                        foundInterop = false;
                    }
                    
                    if(!foundInterop){
                        
                        CustomInteropInputs.LoadArgs loadArgs = new CustomInteropInputs.LoadArgs();
                        
                        CustomInteropInputs inputs = new CustomInteropInputs();
                        inputs.setLoadArgs(loadArgs);
                        
                        InteropInputs interopInputs = new InteropInputs();
                        interopInputs.setInteropInput(inputs);
                        
                        interop.setInteropInputs(interopInputs);
                    }

                } else {
                    
                    WarningDialog.error("Missing interop class", "The interop implementation class \"" + implClassName + "\" was not found on the server.<br/><br/>"
                            + "This class is needed in order to correctly author the selected training application type.");
                    return null;
                }
                
                trainingApp.getInterops().getInterop().add(interop);
            }
            
            return trainingApp;
            
        } else {
            WarningDialog.error("Missing interop mapping", "No implementation mapping was found for '" + type.getDisplayName() + "'. It is "
                    + "possible that this training application type is new to GIFT and does not yet have an associated "
                    + "interface with which to author it.");
            return null;
        }
    }
    
    @EventHandler
    protected void onBranchSelected(BranchSelectedEvent event) {

        // save the current branch before it is possible replaced for possible action later in this method
        AuthoredBranch currBranch = editCourseView.getTreeManager().getCurrentBranch();
        
        editCourseView.getTreeManager().setCurrentBranch(event.getBranch());

        if(event.getBranch() != null){

            // set handlers if this is the first time the branch is being shown
            if (editCourseView.getTreeManager().getBranchTree(event.getBranch()) == null) {

                editCourseView.getTreeManager().showBranchTree(event.getBranch());

                editCourseView.getTreeManager().getBranchTree(event.getBranch()).setNodeClickFunction(new DatumFunction<Void>() {
                    
                    @Override
                    public Void apply(Element context, Value d, int index) {
                            
                        mil.arl.gift.tools.authoring.server.gat.client.view.course.authoredbranch.tree.TreeNode node = 
                                d.<mil.arl.gift.tools.authoring.server.gat.client.view.course.authoredbranch.tree.TreeNode> as();
                        
                        final TreeNodeEnum type = TreeNodeEnum.valueOf(node.getType());
                        final Serializable courseObject = editCourseView.getTreeManager().getCourseObject(node);
                        if(courseObject != null && type != null){
                            
                           if(TreeNodeEnum.TRANSITION.equals(type) || TreeNodeEnum.BRANCH_POINT.equals(type)){                                
                              displaySelectedTransition(courseObject); 
                           }
                           
                           else if(TreeNodeEnum.PATH_START.equals(type)){
                              displaySelectedTransition(editCourseView.getTreeManager().getCurrentBranch());
                           }
                        }
                
                        return null;
                    }
                });

                //enable dragging course tree nodes
                editCourseView.getTreeManager().getBranchTree(event.getBranch()).setNodeDragStartFunction(new DatumFunction<Void>() {
                    
                    @Override
                    public Void apply(Element context, Value d, int index) {
                        
                        if(readOnly){
                            return null;
                        }
                        
                        mil.arl.gift.tools.authoring.server.gat.client.view.course.authoredbranch.tree.TreeNode node = 
                                d.<mil.arl.gift.tools.authoring.server.gat.client.view.course.authoredbranch.tree.TreeNode> as();
                        
                        if(node != null){
                            
                            TreeNodeEnum type = TreeNodeEnum.valueOf(node.getType());
                            
                            if(type != null){
                                
                                if(TreeNodeEnum.TRANSITION.equals(type)){
                                    transitionBeingDragged = editCourseView.getTreeManager().getCourseObject(node);
                                }                       
                            }
                        }
                            
                        return null;
                    } 
                });
                        
                //select course tree nodes as course objects are dragged over them
                editCourseView.getTreeManager().getBranchTree(event.getBranch()).setNodeDragEnterFunction(new DatumFunction<Void>() {
                    
                    @Override
                    public Void apply(Element context, Value d, int index) {
                        
                        mil.arl.gift.tools.authoring.server.gat.client.view.course.authoredbranch.tree.TreeNode node = 
                                d.<mil.arl.gift.tools.authoring.server.gat.client.view.course.authoredbranch.tree.TreeNode> as();
                        
                        editCourseView.getTreeManager().setSelectedNode(node, true);
                        
                        return null;
                    }
                });
                
                //update the cursor to indicate that course objects can be dragged into course tree nodes
                editCourseView.getTreeManager().getBranchTree(event.getBranch()).setNodeDragOverFunction(new DatumFunction<Void>() {

                    @Override
                    public Void apply(Element context, Value d, int index) {
                            
                        //by default, the browser prevents dropping elements into other elements, so we need to disable this
                        D3.event().preventDefault();
                        
                        return null;
                    }
                });
                        
                //handle when the user drops a course object on top of a course tree node
                editCourseView.getTreeManager().getBranchTree(event.getBranch()).setNodeDropFunction(new DatumFunction<Void>() {
                    
                    @Override
                    public Void apply(Element context, Value d, int index) {
                        
                        D3.event().preventDefault(); //prevents Firefox from navigating away from the page when drag data is added
                        
                        final Serializable dragged = transitionBeingDragged;
                        transitionBeingDragged = null;
                        
                        dragCourseObject(dragged, d, true);
                        
                        return null;
                    }
                });
                
                //update the cursor to indicate that course objects can be dragged into course tree nodes
                editCourseView.getTreeManager().getBranchTree(event.getBranch()).setSVGDragOverFunction(new DatumFunction<Void>() {
                    
                    @Override
                    public Void apply(Element context, Value d, int index) {
                        
                        //by default, the browser prevents dropping elements into other elements, so we need to disable this
                        D3.event().preventDefault();
                        
                        return null;
                    }
                });
                        
                //handle when the user drops a course object on top of a course tree node
                editCourseView.getTreeManager().getBranchTree(event.getBranch()).setSVGDropFunction(new DatumFunction<Void>() {
                        
                    @Override
                    public Void apply(Element context, Value d, int index) {
                            
                        D3.event().preventDefault(); //prevents Firefox from navigating away from the page when drag data is added
                              
                        WarningDialog.alert("Create Course Object", 
                                "Please drag your course object to the path you want to add it to.");
                        
                        return null;
                    }
                });
        
                editCourseView.getTreeManager().getBranchTree(event.getBranch()).setNodeContextFunction(new DatumFunction<Void>() {

                    @Override
                    public Void apply(Element context, Value d, int index) {
                            
                        mil.arl.gift.tools.authoring.server.gat.client.view.course.authoredbranch.tree.TreeNode node = 
                                d.<mil.arl.gift.tools.authoring.server.gat.client.view.course.authoredbranch.tree.TreeNode> as();
                        
                        editCourseView.getTreeManager().setSelectedNode(node, true);
                        
                        TreeNodeEnum type = TreeNodeEnum.valueOf(node.getType());
                        
                        if(type != null){
                            
                            if(TreeNodeEnum.TRANSITION.equals(type)){                        
                                
                                transitionAuthoredBranchNodeContextMenu.setPopupPosition(
                                        D3.event().getClientX(),
                                        D3.event().getClientY()
                                );
                                transitionAuthoredBranchNodeContextMenu.show();
                            }
                        }            
                        
                        return null;
                    }
                });
                
                editCourseView.getTreeManager().getBranchTree(event.getBranch()).setDeleteNodeFunction(new DatumFunction<Void>() {

                    @Override
                    public Void apply(Element context, Value d, int index) {
                        
                        editCourseView.getEditorPanel().stopEditing(transitionBeingDragged, true, true);
                        
                        if(transitionBeingDragged instanceof AuthoredBranch) {
                            
                            /* If an authored branch is being deleted, then we need to make sure that we stop
                             * editing any course objects that are pinned inside of it */
                            AuthoredBranch currBranch = (AuthoredBranch) transitionBeingDragged;
                            
                            if(currBranch.getPaths() != null){
                                for(Path path : currBranch.getPaths().getPath()){
                                    
                                    if(path.getCourseobjects() != null){
                                        
                                        for(Serializable courseObject : path.getCourseobjects().getAAROrAuthoredBranchOrEnd()){
                                            editCourseView.getEditorPanel().stopEditing(courseObject, true, true);
                                        }
                                    }
                                }
                            }
                        }
                        
                        // Remove the transition
                        Serializable objToRemove = null;
                        for (Path path : editCourseView.getTreeManager().getCurrentBranch().getPaths().getPath()) {
                            for (Serializable obj : path.getCourseobjects().getAAROrAuthoredBranchOrEnd()) {
                                if (obj == transitionBeingDragged) {
                                    objToRemove = obj;
                                    break;
                                }
                                
                            }
                            if (objToRemove != null) {
                                path.getCourseobjects().getAAROrAuthoredBranchOrEnd().remove(objToRemove);
                                break;
                            }
                        }
                        
                        transitionBeingDragged = null;
                        saveCourse(filePath, true);
                        return null;
                    }
                    
                });
                
            } else {
                editCourseView.getTreeManager().showBranchTree(event.getBranch());
            }
            
        }else{
            
            // show the parent tree of the authored branch (tree) and deselect the authored branch course object in the parent tree
            editCourseView.getTreeManager().showBranchTree(null);
        }
        
        if(currBranch != null && currBranch != event.getBranch()){
            // when going from one branch to another branch (or no branch),
            // close any course object editor tabs from the authored branch (tree) that is being exited (pinned or not pinned)
            
            // the authored branch course object editor
            editCourseView.getEditorPanel().stopEditing(currBranch, true, true);
         
            if(currBranch.getPaths() != null){
                for(Path path : currBranch.getPaths().getPath()){
                    
                    if(path.getCourseobjects() != null){
                        
                        for(Serializable courseObject : path.getCourseobjects().getAAROrAuthoredBranchOrEnd()){
                            editCourseView.getEditorPanel().stopEditing(courseObject, true, true);
                        }
                    }
                }
            }
        }
    }
}
