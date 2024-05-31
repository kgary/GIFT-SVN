/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.constants.IconSize;
import org.gwtbootstrap3.client.ui.constants.IconType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.dom.client.DataTransfer;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DragOverEvent;
import com.google.gwt.event.dom.client.DragOverHandler;
import com.google.gwt.event.dom.client.DropEvent;
import com.google.gwt.event.dom.client.DropHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.TimeZone;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;

import mil.arl.gift.common.enums.AbstractEnum;
import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;
import mil.arl.gift.common.enums.LowMediumHighLevelEnum;
import mil.arl.gift.common.enums.OperatorEnum;
import mil.arl.gift.common.enums.SurveyElementTypeEnum;
import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.common.gwt.client.widgets.DynamicHeaderScrollPanel;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.BsLoadingDialogBox;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ModalDialogBox;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ModalDialogCallback;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.AbstractQuestionWidget;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.LoadSurveyException;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.SaveSurveyException;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.SurveyEditMode;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.events.ResponsesChangedEvent;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.events.SurveyScoreValueChangedEvent;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.LoadedProgressIndicator;
import mil.arl.gift.common.survey.AbstractQuestion;
import mil.arl.gift.common.survey.AbstractSurveyElement;
import mil.arl.gift.common.survey.AbstractSurveyQuestion;
import mil.arl.gift.common.survey.Constants;
import mil.arl.gift.common.survey.FillInTheBlankSurveyQuestion;
import mil.arl.gift.common.survey.ListOption;
import mil.arl.gift.common.survey.MatrixOfChoicesSurveyQuestion;
import mil.arl.gift.common.survey.MultipleChoiceSurveyQuestion;
import mil.arl.gift.common.survey.OptionList;
import mil.arl.gift.common.survey.RatingScaleSurveyQuestion;
import mil.arl.gift.common.survey.SliderSurveyQuestion;
import mil.arl.gift.common.survey.Survey;
import mil.arl.gift.common.survey.Survey.SurveyTypeEnum;
import mil.arl.gift.common.survey.SurveyContextSurvey;
import mil.arl.gift.common.survey.SurveyItemProperties;
import mil.arl.gift.common.survey.SurveyPage;
import mil.arl.gift.common.survey.SurveyProperties;
import mil.arl.gift.common.survey.TextSurveyElement;
import mil.arl.gift.common.survey.score.AttributeScorerProperties;
import mil.arl.gift.common.survey.score.ReturnValueCondition;
import mil.arl.gift.common.survey.score.SurveyScorer;
import mil.arl.gift.common.survey.score.TotalScorer;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.survey.SurveyRpcService;
import mil.arl.gift.tools.authoring.server.gat.client.survey.SurveyRpcServiceAsync;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.AbstractSelectSurveyItemWidget.SurveyItemType;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.AddSurveyDialog.SurveyDialogOption;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.CopySurveyItemDialog.CopySurveyItemCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.SurveyPageWidget.MoveOrderEnum;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.SurveyPageWidget.SurveyPageEditModeChanged;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.events.AssociatedConceptsChangedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.events.ClearFilterEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.events.DifficultyChangedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.events.FilterChangeEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.events.SurveyAddNewItemEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.events.SurveyAddPageEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.events.SurveyAddSurveyItemEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.events.SurveyAddSurveyItemEvent.InsertOrder;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.events.SurveyChangeEditMode;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.events.SurveyCloseEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.events.SurveyCopyQuestionEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.events.SurveyDeletePageEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.events.SurveyDeleteQuestionEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.events.SurveyFilterEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.events.SurveyImportEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.events.SurveyMovePageEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.events.SurveyMoveQuestionEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.events.SurveyPreviewEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.events.SurveySaveEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.events.SurveyScrollToElementEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.events.SurveySelectPageEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.events.SurveySelectQuestionEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.filter.QuestionFilter;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.filter.QuestionFilterWidget;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.ErrorDetailsDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.WarningDialog;
import mil.arl.gift.tools.authoring.server.gat.shared.GwtSurveySystemProperties;
import mil.arl.gift.tools.authoring.server.gat.shared.ListQuery;
import mil.arl.gift.tools.authoring.server.gat.shared.ListQueryResponse;
import mil.arl.gift.tools.authoring.server.gat.shared.OptionListQueryData;
import mil.arl.gift.tools.authoring.server.gat.shared.action.surveyeditor.ImportQsf;
import mil.arl.gift.tools.authoring.server.gat.shared.action.surveyeditor.QualtricsImportResult;


/**
 * The survey editor panel encapsulates the main panel for survey editing.  
 * It consists of a general header at the top, a workspace area in the center
 * and a properties panel on the right hand side.  
 * 
 * @author nblomberg
 *
 */
public class SurveyEditorPanel extends Composite implements SurveyImageInterface  {

    /** The logger for the class */
    private static Logger logger = Logger.getLogger(SurveyEditorPanel.class.getName());
    
    /** The UI Binder for the class */
    private static SurveyEditorPanelUiBinder uiBinder = GWT.create(SurveyEditorPanelUiBinder.class);

    
    /** The interface for the UI Binder */
    interface SurveyEditorPanelUiBinder extends
            UiBinder<Widget, SurveyEditorPanel> {
    }
    
    /** The container for the filter. Currently the filter is only used for question banks. */
    @UiField
    FlowPanel filterContainer;

    /** The container for the survey pages and the load more button. Currently the load more button is only used for question banks. */
    @UiField
    FlowPanel workspaceWidget;
    
    /** The container for the property panel. */
    @UiField
    FlowPanel propertiesWidget;
    
    /** The container for the header widgets including the survey name, mode buttons, and survey action buttons. */
    @UiField
    SurveyEditorHeaderWidget surveyHeader;
    
    /** The container for the header, footer, workspace, and property widgets. */
    @UiField
    DynamicHeaderScrollPanel mainContainer;
        
    /** The container for the loading page */
    @UiField
    FlowPanel loadingPanel;
    
    /** The footer widget of buttons to add new survey items. */
    @UiField
    FooterWidget footerWidget;
    
    /** Interface for handling events. */
    interface WidgetEventBinder extends EventBinder<SurveyEditorPanel> {
    }
    
    /** Create the instance of the event binder (binds the widget for events. */
    private static final WidgetEventBinder eventBinder = GWT
            .create(WidgetEventBinder.class);    
  
    /** The scoring logic header for knowledge assessment (static) surveys.  (Kas) */
    private KasScoringLogicWidget kasScoringLogicWidget = new KasScoringLogicWidget();
    
    /** The scoring logic header for collect user info (scored) surveys.  (CuiS) */
    private ScoringLogicWidgetCuiS scoringLogicWidgetCuiS = new ScoringLogicWidgetCuiS();
    
    /** The widget used to filter the questions shown by paging logic */
    private QuestionFilterWidget filterWidget;
    
    /** Container that will hold each survey page widget. */
    private FlowPanel surveyPageContainer = new FlowPanel();
    
    /** Button used to load more questions if paging logic is being used */
    private Button loadMoreQuestions = new Button();
    
    /** Dialog where the user can drop qualtrics export files for importing */
    private ModalDialogBox dndImportModal;
    
    /** The current page index which is used to give each page widget a unique identifier. */
    private int currentPageIndex = 0;
    
    /** Modal dialog used to display progress of the survey elements while the edit mode is changed. */
    private static SurveyElementProgressModal editorModeProgressModal;
        
    /** The selected survey page widget (if any survey page is selected).  Null if there is no survey page selected. */
    private SurveyPageWidget selectedPage = null;
    
    /** The panel that is used to display the properties of the selected item. */
    private PropertiesPanelWidget propertiesPanel;
    
    /** The current survey type that is being authored in the editor. */
    private SurveyDialogOption surveyType = SurveyDialogOption.COLLECTINFO_NOTSCORED;
    
    /** The current mode that the editor is in (writing or scoring mode). */
    private SurveyEditMode editorMode = SurveyEditMode.WritingMode;
    
    /** The set of global survey resources (i.e. survey context, concepts, etc.) that should be referred to while editing surveys */
    private AbstractSurveyResources surveyResources = null;
    
    /** Provides an interface to notify when a survey panel is closed */
    private CloseableInterface closeableInterface = null;
    
    /** An instance of the survey data that is loaded by the Survey Editor. */
    private Survey loadedSurvey = null;
    
    /** The survey context that the survey will be saved to. */
    private SurveyContextSurvey surveyContextSurvey = null;
    
    /** The survey transition name */
    private String surveyTransitionName = null;
    
    /** The survey context key for the survey transition */
    private String surveyKey = null;
    
    /** The id of the survey context */
    private int surveyContextId;
    
    /** Callback that is used after the survey context has been assigned / saved to the database. */
    private SelectSurveyContextCallback contextCallback;

    /** A delay that's used to wait for the modal dialog to fade in. */
    private int MODAL_FADEIN_DELAY_MS = 1500;
    
    /** A cached survey save event object that is valid for the duration of the save process. */
    SurveySaveEvent cachedSaveEvent = null;

    /** Boolean to indicate if the survey was converted by having the survey type added to it upon loading.  This should only happen for
     * legacy surveys authored in the old SAS (Survey Authoring System) tool.
     */
    private boolean wasConverted = false;
    
    /** The list of images to be displayed in surveys */
    private List<String> surveyImageList = null;
    
    /**
     * RPC service that is used to retrieve the surveys from the database
     */
    private final SurveyRpcServiceAsync rpcService = GWT.create(SurveyRpcService.class);
    
    
    /** True if the survey editor is read-only, false otherwise. */
    private boolean isReadOnly = false;
    
    /** True during the initial load of the SurveyEditorPanel, false otherwise. */
    private boolean isLoading = false;
    
    /** Manager class that is used to query and determine permissions for the survey editor. */
    private SurveyPermissionsManager permissionsManager = new SurveyPermissionsManager();
    
    /** Amount of time to yield back to the browser thread during loading of a survey (in milliseconds).  */
    /** 5 ms delay should be enough to prevent the unresponsive script error in the browser. */
    private static final int LOAD_SURVEY_SCHEDULER_DELAY_MS = 5;
    
    /** Title for the loading dialog when checking dependencies */
    private static final String CHECK_DEPENDENCIES_TITLE = "Checking Dependencies";
    
    /** Dialog message for the loading dialog when checking dependencies */
    private static final String CHECK_DEPENDENCIES_MESSAGE = "Please wait...checking the dependencies for the survey.";
    
    /**
     * The following variables are used by the scheduler during the load survey process to keep track of which
     * element is currently being loaded.
     */
    /** The current AbstractSurveyElement iterator for the load process. */
    private Iterator<AbstractSurveyElement> elementIter = null;
    /** The current SurveyPage that is being loaded during the load process. */
    private SurveyPage currentPageLoad = null;
    /** The current SurveyPageWidget that is being loaded during the load process. */
    private SurveyPageWidget currentPageWidgetLoad = null;
    /** The current SurveyPage iterator for the load process. */
    private Iterator<SurveyPage> pageIter = null;
    /** The current index of the element of the page that is being loaded. */
    private int currentElementLoadIndex = 0;
    /** The current index of the page that is being loaded. */
    private int currentPageLoadIndex = 0;
    /** The current total elements in the page. */
    private int totalElementLoadCount = 0;
    
    /** 
     * The current index of the element on the survey page container. 
     * Used for progress reporting when the user switches editing modes.
     */
    private int currentWidgetIndex = 0;

    /** The number of questions that have already been lazy loaded by the user*/
    private int indexOfLastQuestionLoaded = 0;

    /** The total number of questions that have been loaded into the page widget */
    private int totalQuestionsLoaded = 0;
    
    /** The number of questions to load at a time for the user driven lazy loading logic*/
    private int pageSize = 10;
    
    /** The number of questions that apply to the current filter */
    private int filteredQuestions = 0;
    
    /** Maps each QuestionContainerWidget's id to the its corresponding AbstractSurveyElement within the loadedSurvey */
    private Map<SurveyWidgetId, AbstractSurveyElement> questionIdToDataModel = new HashMap<>();
    
    /**
     * Callback to execute when the edit mode has been switched for all widgets on a page.
     * Used for progress reporting when the user switches editing modes
     */
    private SurveyPageEditModeChanged pageChangedCallback = new SurveyPageEditModeChanged() {

        @Override
        public void onEditModeChangeComplete() {
            
            // When the edit mode has been changed for one page, proceed to change the next page.
            changeNextSurveyPageEditMode(editorMode);
        }
        
    };
    
    /** Whether or not this editor is using selection mode (i.e. letting users pick survey items) */
    private boolean isSelectionMode = false;

    /** the upload Qualtrics dialog help */
    private Anchor helpAnchor;
    
    /** The type of survey this survey's contents are being copied into */
    private SurveyDialogOption copyTargetType = null;
    
    /** The event registration this editor is using to handle survey events */
    private HandlerRegistration eventRegistration = null;
    
    /** whether the survey being loaded is the original survey and not a copy of an existing survey or a new survey.*/
    private boolean usingOriginalSurvey = false;
    
    /**
     * Creates a new dialog for selecting a survey context survey 
     */
    public SurveyEditorPanel() {
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("constructor()");
        }
        
        initWidget(uiBinder.createAndBindUi(this));
        
        editorModeProgressModal = new SurveyElementProgressModal();
        
        setSurveyEventHandlingEnabled(true);
        
        filterContainer.clear();
        workspaceWidget.clear();
        
        propertiesWidget.clear();
        
        surveyPageContainer.setWidth("100%");
        
        FlowPanel dndPanel = new FlowPanel();
        Icon dndIcon = new Icon(IconType.CLOUD_UPLOAD);
        Label dndLabel = new Label("Drop .qsf file here");
        Icon helpIcon = new Icon(IconType.QUESTION_CIRCLE);
        helpAnchor = new Anchor();
        
        dndPanel.add(dndLabel);
        dndPanel.add(dndIcon);
        dndIcon.addStyleName("dndIcon");
        dndPanel.addStyleName("dndPanel");
        dndIcon.setSize(IconSize.TIMES3);
        helpIcon.setSize(IconSize.TIMES2);
        helpAnchor.getElement().getStyle().setMargin(5, Unit.PX);
        helpAnchor.getElement().getStyle().setFloat(Style.Float.LEFT);
        helpAnchor.setHTML(helpIcon.getElement().getString());
        helpAnchor.setTarget("_blank");
        //Note: set the link else-where in this class, after checking if survey is editable
        
        dndImportModal = new ModalDialogBox();
        dndImportModal.setCloseable(true);
        dndImportModal.setFooterWidget(helpAnchor);
        dndImportModal.setGlassEnabled(true);
        dndImportModal.getCloseButton().setText("Cancel");
        dndImportModal.setText("Import survey from .qsf");
        dndImportModal.setWidget(dndPanel);
        
        dndImportModal.addDomHandler(new DragOverHandler() {
            
            @Override
            public void onDragOver(DragOverEvent event) {
                // Stop the browser from immediately trying to load the file so we can handle it
                event.preventDefault(); 
            }
        }, DragOverEvent.getType());
        
        dndImportModal.addDomHandler(new DropHandler() {
            
            @Override
            public void onDrop(DropEvent event) {
                
                // Stop the browser from immediately trying to load the file so we can handle it
                event.preventDefault(); 
                    
                if(event.getDataTransfer() != null){
                    
                    // Process the dragged content so that files dragged onto this widget can be loaded as surveys
                    processDroppedFile(event.getNativeEvent(), event.getDataTransfer());
                }
            }
            
        }, DropEvent.getType());
        
        loadMoreQuestions.setIcon(IconType.COG);
        loadMoreQuestions.setText("Load More...");
        loadMoreQuestions.setWidth("100%");
        loadMoreQuestions.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                countFilteredQuestions();
                
                // Finds the first (and only) surveyPageWidget
                SurveyPageWidget surveyPageWidget = null;
                for (Widget w : surveyPageContainer) {
                    if (w instanceof SurveyPageWidget) {
                        surveyPageWidget = (SurveyPageWidget) w;
                        break;
                    }
                }
                
                RepeatingCommand rc = createPageBuilderCommand(
                        loadedSurvey.getPages().get(0), 
                        surveyPageWidget, 
                        indexOfLastQuestionLoaded + 1, 
                        pageSize,
                        new AsyncCallback<Boolean>(){

                            @Override
                            public void onFailure(Throwable arg0) {
                                /** Nothing to do */
                            }

                            @Override
                            public void onSuccess(Boolean success) {
                                displayLoadMoreButton();
                            }
                            
                        });
                Scheduler.get().scheduleFixedDelay(rc, LOAD_SURVEY_SCHEDULER_DELAY_MS);
            }
        });

        displayLoadMoreButton();
    }
    
    
    
    /**
     * Processes a file that has been dropped onto this widget to determine if a survey can be
     * loaded based on its contents
     * 
     * @param e the native event.
     * @param dt the file dropped data transfer object.
     */
    private native void processDroppedFile(NativeEvent e, DataTransfer dt)/*-{
        
        //Nick: This code is modified from an example found at
        //http://www.htmlgoodies.com/html5/javascript/drag-files-into-the-browser-from-the-desktop-HTML5.html#fbid=ynG6653DJ5u
        
        //a reference to the native GWT object used to invoke this method
        var that = this;

        //an event used to add native event handlers
        function addEventHandler(obj, evt, handler) {
            if (obj.addEventListener) {
                // W3C method
                obj.addEventListener(evt, handler, false);
            } else if (obj.attachEvent) {
                // IE method.
                obj.attachEvent('on' + evt, handler);
            } else {
                // Old school method.
                obj['on' + evt] = handler;
            }
        }

        //iterate through all the files that were dragged onto the page
        var files = dt.files;
        for (var i = 0; i < files.length; i++) {
            
            var file = files[i];
            var reader = new FileReader();

            //attach a handler to perform logic once each file has been read
            addEventHandler(
                    reader,
                    'loadend',
                    function(e, file) {
                        
                        var plainText = this.result;    
                        
                        //if the file was successfully loaded, parse its contents to try to form a survey
                        that.@mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.SurveyEditorPanel::importSurvey(Ljava/lang/String;)(plainText);
                        
                    }
            );

            //read each file
            reader.readAsText(file);
        }

    }-*/;

    /**
     * Updates the survey image list with the current values on the server.
     * An optional callback can be used to signal when the result has been retrieved.
     * 
     * @param callback - Callback used to signal when the images have been retrieved.
     */
    private void updateSurveyImageList(final AsyncCallback<String> callback) {
        
        if(logger.isLoggable(Level.FINE)) {
            logger.fine("updateSurveyImageList(" + callback + ")");
        }
        
        rpcService.getSurveyImages(new AsyncCallback<List<String>>() {

            @Override
            public void onFailure(Throwable t) {
                
                if(logger.isLoggable(Level.SEVERE)) {
                    logger.log(Level.SEVERE,"Failed to get survey images: " + t.getMessage(), t);
                }
                
                if (callback != null) {
                    callback.onFailure(t);
                }
                
            }

            @Override
            public void onSuccess(List<String> result) {
                
                if (result != null && !result.isEmpty()) {
                    
                    if(logger.isLoggable(Level.INFO)) {
                        logger.info("Retrieved survey images successfully.");
                    }
                    
                    surveyImageList = result;
                    
                } else if(logger.isLoggable(Level.SEVERE)) {
                    logger.severe("Unable to get survey images.  Server returned a null or empty list.");
                }
                
                if (callback != null) {
                    callback.onSuccess("");
                }
            }
            
        });
    }
    
    /**
     * Used to fetch the option lists from the server (these are the existing
     * answer sets that users can select from). This also creates the properties
     * panel with the retrieved option list values. This should only be called
     * once during initialization of the survey editor panel.
     * 
     * @param callback Used to signal when the results have been retrieved. A
     *        null value can be used in order to indicate no callback should be
     *        executed.
     * 
     */
    private void getOptionLists(final AsyncCallback<String> callback) {
        
        if(logger.isLoggable(Level.FINE)) {
            logger.fine("getOptionLists(" + callback + ")");
        }
        
        ListQuery<OptionListQueryData> query = new ListQuery<OptionListQueryData>();
        
        OptionListQueryData queryData = new OptionListQueryData(true, GatClientUtility.getUserName());
        
        query.setQueryData(queryData);
        
        rpcService.getOptionLists(query, new AsyncCallback<ListQueryResponse<OptionList>>(){

            @Override
            public void onFailure(Throwable caught) {
                
                if(logger.isLoggable(Level.WARNING)) {
                    logger.warning("Failed to get answer sets");
                }
                
                if (callback != null) {
                    callback.onFailure(caught);
                }
            }

            @Override
            public void onSuccess(ListQueryResponse<OptionList> result) {
                
                if(logger.isLoggable(Level.INFO)) {
                    logger.info("Got the answer sets with a total of " + result.getList().size() + " sets, creating the properties panel");
                }
                
                createPropertiesPanel(result);
                
                if (callback != null) {
                    callback.onSuccess("");
                }
            }
        });
    }
    
    /**
     * Creates the properties panel based on the existing option lists from the server.
     * 
     * @param response - list of option sets from the server.
     */
    private void createPropertiesPanel(ListQueryResponse<OptionList> response) {
        
        if(logger.isLoggable(Level.FINE)) {
            logger.fine("createPropertiesPanel(" + response + ")");
        }
        
        propertiesWidget.clear();

        if (response != null) {
            propertiesPanel = new PropertiesPanelWidget(this, response.getList());
            propertiesWidget.add(propertiesPanel);
        } else {
            propertiesPanel = new PropertiesPanelWidget(this);
            propertiesWidget.add(propertiesPanel);
        }
        
        propertiesPanel.setReadOnlyMode(isReadOnly());
    }

    /**
     * Adds a new survey page widget to the workspace.
     */
    public void addNewSurveyPage() {
        
        if(logger.isLoggable(Level.FINE)) {
            logger.fine("addNewSurveyPage()");
        }
        
        editorModeProgressModal.addPage();
        
        SurveyPageWidget pageWidget = createNewSurveyPage();
        selectSurveyPage(pageWidget.getWidgetId());
    }
    
    /**
     * Handles the event when adding a survey page.
     * 
     * @param event the {@link SurveyAddPageEvent}
     */
    @EventHandler
    protected void onSurveyAddPageEvent(SurveyAddPageEvent event) {
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("onSurveyAddPageEvent: " + event);
        }
        
        if(editorMode == SurveyEditMode.WritingMode && !isReadOnly){
            addNewSurveyPage();
        }
    }
    
    /**
     * Handles the event when deleting a survey page.
     * 
     * @param event the {@link SurveyDeletePageEvent}
     */
    @EventHandler
    protected void onSurveyDeletePageEvent(SurveyDeletePageEvent event) {
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("onSurveyDeletePageEvent: " + event);
        }
        
        deleteSurveyPage(event.getWidgetId());
    }
    
    /**
     * Handles the event when moving a survey page.
     * 
     * @param event the {@link SurveyMovePageEvent}
     */
    @EventHandler
    protected void onSurveyMovePageEvent(final SurveyMovePageEvent event) {
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("onSurveyMovePageEvent: " + event);
        }

        MoveOrderEnum moveOrder = event.getPageOrder();
        SurveyWidgetId pageId = event.getWidgetId();
        boolean isFirstPage = isFirstPage(pageId);
        boolean isLastPage = isLastPage(pageId);
        
        // Optimization to not move the page if it is already first (and trying to be moved up)
        // and if it it not last (and trying to be moved down).
        if (!((isFirstPage && moveOrder == MoveOrderEnum.ORDER_UP) ||
             (isLastPage && moveOrder == MoveOrderEnum.ORDER_DOWN))) {
            
            
            showLoadingDialog("Moving Survey Page", "The survey page is being moved...please wait.");
            Scheduler.get().scheduleDeferred(new Command() {

                @Override
                public void execute() {
                    moveSurveyPage(event.getWidgetId(), event.getPageOrder());
                    hideLoadingDialog();
                }
                
            });
        }
    }
    
    /**
     * Handles the event when selecting a survey page.
     * 
     * @param event the {@link SurveySelectPageEvent}
     */
    @EventHandler 
    protected void onSurveySelectPageEvent(SurveySelectPageEvent event) {
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("onSurveySelectPageEvent: " + event);
        }
        
        selectSurveyPage(event.getPageId());
    }
    
    /**
     * Handles the event when 'add survey item' button was clicked.
     * 
     * @param event the {@link SurveyAddSurveyItemEvent}
     */
    @EventHandler 
    protected void onSurveyAddSurveyItemEvent(SurveyAddSurveyItemEvent event) {
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("onSurveyAddSurveyItemEvent: " + event);
        }
        
        if(editorMode == SurveyEditMode.WritingMode && !isReadOnly){
            showAddSurveyItemWidget(event.getPageId(), event.getQuestionId(), event.getInsertOrder());
        }
    }
    
    /**
     * Handles the event when a survey item response changed.
     * 
     * @param event the {@link ResponsesChangedEvent}
     */
    @EventHandler
    protected void onResponsesChangedEvent(ResponsesChangedEvent event) {
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("onResponsesChangedEvent: " + event);
        }
         
         if(editorMode == SurveyEditMode.WritingMode && !isReadOnly 
                 && propertiesPanel.getMultiSelectWidget() != null) {
            propertiesPanel.getMultiSelectWidget().onResponseChanged(event.getTotalResponses());
         }
    }
    
    /**
     * Handles the event when a adding a new survey item.
     * 
     * @param event the {@link SurveyAddNewItemEvent}
     */
    @EventHandler 
    protected void onSurveyAddNewItemEvent(final SurveyAddNewItemEvent event) {
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("onSurveyAddNewItemEvent: " + event);
        }
        
        if(editorMode == SurveyEditMode.WritingMode && !isReadOnly){
            
            if (event.getItemType() == SurveyItemType.COPY_EXISTING_ITEM) {
                
                //Temporarily disable event handling so that survey events directed at the copy 
                //dialog don't get intercepted by this widget.
                setSurveyEventHandlingEnabled(false);
                
                final CopySurveyItemDialog dialog = new CopySurveyItemDialog();
                dialog.addCloseHandler(new CloseHandler<PopupPanel>() {
                    
                    @Override
                    public void onClose(CloseEvent<PopupPanel> event) {
                        
                        //Re-enable event handling so that survey events are once again handled by this widget
                        setSurveyEventHandlingEnabled(true);
                    }
                });
                dialog.showCopy(this, new CopySurveyItemCallback() {
                        
                    @Override
                    public void onItemsSelected(List<QuestionContainerWidget> items) {          
              
                        dialog.hide();
                    
                        copySurveyQuestions(event.getParentPage().getWidgetId(), null, items);      
                        
                        event.getParentPage().hideSelectSurveyItemWidget();                     
                        event.getParentPage().updateQuestionNumbers();
                    }
                });
                
            } else {
            
                if (event.getParentPage() != null) {
                    SurveyPageWidget surveyPage = event.getParentPage();
                    surveyPage.onNewSurveyItem(event.getItemType());
                }
            }
        }
        
    }
    
    /**
     * Handles the event when moving a question.
     * 
     * @param event the {@link SurveyMoveQuestionEvent}
     */
    @EventHandler
    protected void onSurveyMoveQuestionEvent(SurveyMoveQuestionEvent event) {
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("onSurveyMoveQuestionEvent: " + event);
        }

        moveSurveyQuestion(event.getPageWidgetId(), event.getQuestionId(), event.getPageOrder());
    }
    
    /**
     * Handles the event when copying a question.
     * 
     * @param event the {@link SurveyCopyQuestionEvent}
     */
    @EventHandler
    protected void onSurveyCopyQuestionEvent(SurveyCopyQuestionEvent event) {
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("onSurveyCopyQuestionEvent: " + event);
        }

        if(editorMode == SurveyEditMode.WritingMode && !isReadOnly){
            copySelectedSurveyQuestions(event.getPageWidgetId(), event.getQuestionId());
        }
    }
    
    /**
     * Handles the event when deleting a question.
     * 
     * @param event the {@link SurveyDeleteQuestionEvent}
     */
    @EventHandler
    protected void onSurveyDeleteQuestionEvent(SurveyDeleteQuestionEvent event) {
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("onSurveyDeleteQuestionEvent: " + event);
        }

        deleteSurveyQuestion(event.getPageWidgetId(), event.getQuestionId());
    }
    
    /**
     * Handles the event when changing a question's associated concepts.
     * 
     * @param event the {@link AssociatedConceptsChangedEvent}
     */
    @EventHandler
    protected void onAssociatedConceptsChangedEvent(AssociatedConceptsChangedEvent event) {
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("onAssociatedConceptsChangedEvent(" + event + ")");
        }
        
        //Only allows execution for question banks currently
        if(!surveyType.equals(SurveyDialogOption.ASSESSLEARNER_QUESTIONBANK)) {
            return;
        }
        
        AbstractSurveyElement changedElement = questionIdToDataModel.get(event.getWidgetId());
        if(changedElement == null) {
            saveCurrentlyVisibleQuestionsToDataModel();
            changedElement = questionIdToDataModel.get(event.getWidgetId());
        }
        
        if(changedElement instanceof AbstractSurveyQuestion<?>) {
            AbstractSurveyQuestion<?> surveyQuestion = (AbstractSurveyQuestion<?>) changedElement;
            String conceptListString = SurveyItemProperties.encodeListString(event.getConcepts());
            surveyQuestion.getQuestion().getProperties().setPropertyValue(SurveyPropertyKeyEnum.ASSOCIATED_CONCEPTS, conceptListString);
        } else if(!isLoading) {
            //#3530 - when the question bank survey is loading, existing questions that are tagged with concepts
            //        will not be saved to the data model at this point (i.e. the changedElement will be null)
            if(logger.isLoggable(Level.WARNING)) {
                logger.warning("Unable to get reference to backing data model for the question whose associated concepts have changed");
            }
        }
    }
    
    /**
     * Handles the event when changing a question's difficulty.
     * 
     * @param event the {@link DifficultyChangedEvent}
     */
    @EventHandler
    protected void onDifficultyChangedEvent(DifficultyChangedEvent event) {
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("onDifficultyChangedEvent: " + event);
        }
        
        //Only allows execution for question banks currently
        if(!surveyType.equals(SurveyDialogOption.ASSESSLEARNER_QUESTIONBANK)) {
            return;
        }
        
        AbstractSurveyElement changedElement = questionIdToDataModel.get(event.getWidgetId());
        if(changedElement == null) {
            saveCurrentlyVisibleQuestionsToDataModel();
            changedElement = questionIdToDataModel.get(event.getWidgetId());
        }
        
        if(changedElement instanceof AbstractSurveyQuestion<?>) {
            AbstractSurveyQuestion<?> surveyQuestion = (AbstractSurveyQuestion<?>) changedElement;
            surveyQuestion.getQuestion().getProperties().setPropertyValue(SurveyPropertyKeyEnum.QUESTION_DIFFICULTY, event.getDifficulty());
        
        } else if(logger.isLoggable(Level.WARNING)) {
            logger.warning("Unable to get reference to backing data model for the question whose difficulty has changed");
        }
    }
    
    /**
     * Handles the event when selecting a question.
     * 
     * @param event the {@link SurveySelectQuestionEvent}
     */
    @EventHandler
    void onSurveySelectQuestionEvent(SurveySelectQuestionEvent event) {
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("onSurveySelectQuestionEvent: " + event);
        }

        selectSurveyQuestion(event.getPageWidgetId(), event.getQuestionId(), event.shouldKeepPreviousSelection());
    }
    
    /**
     * Handles the event when we want to programmatically scroll to a specific element.
     * 
     * @param event the {@link SurveyScrollToElementEvent}
     */
    @EventHandler
    protected void onSurveyScrollToElementEvent(SurveyScrollToElementEvent event) {
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("onSurveyScrollToElementEvent: " + event.getElementId());
        }

        if(!isLoading) {
            scrollToElement(event.getElementId());
        }
    }
    
    /**
     * Handles the event when the survey changed modes.
     * 
     * @param event the {@link SurveyChangeEditMode}
     */
    @EventHandler
    void onSurveyChangeEditModeEvent(SurveyChangeEditMode event) {
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("onSurveyChangeEditModeEvent: " + event);
        }

        onEditorModeChanged(event.getEditMode());
    }
    
    /**
     * Handles the event when the question score changed values.
     * 
     * @param event the {@link SurveyScoreValueChangedEvent}
     */
    @EventHandler
    protected void onSurveyScoreValueChangedEvent(SurveyScoreValueChangedEvent event) {
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("onSurveyScoreValueChangedEvent: " + event);
        }

        onScoreValueChanged();
    }
    
    /**
     * Handles the event when the filter was modified.
     * 
     * @param event the {@link FilterChangeEvent}
     */
    @EventHandler
    protected void onFilterChangeEvent(FilterChangeEvent event) {
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("onApplyFilterEvent: " + event);
        }
        
        //Save changes made to page
        saveCurrentlyVisibleQuestionsToDataModel();
        
        questionIdToDataModel.clear();
        surveyPageContainer.clear();
        deselectAllSurveyQuestions();
        
        SurveyPageWidget newPage = createNewSurveyPage();
        selectSurveyPage(newPage.getWidgetId());
        
        countFilteredQuestions();

        totalQuestionsLoaded = 0;
        indexOfLastQuestionLoaded = 0;
        RepeatingCommand rc = createPageBuilderCommand(
                loadedSurvey.getPages().get(0), 
                newPage, 
                0, 
                pageSize,
                new AsyncCallback<Boolean>() {
                    
                    @Override
                    public void onSuccess(Boolean arg0) {
                        displayLoadMoreButton();
                    }
                    
                    @Override
                    public void onFailure(Throwable arg0) {
                        /** Nothing to do */
                    }
                });
        Scheduler.get().scheduleFixedDelay(rc, LOAD_SURVEY_SCHEDULER_DELAY_MS);
    }
    
    /**
     * Handles the event when the filter is cleared.
     * 
     * @param event the {@link ClearFilterEvent}
     */
    @EventHandler
    protected void onClearFilterEvent(ClearFilterEvent event) {
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("onClearFilterEvent: " + event);
        }
        
        surveyPageContainer.clear();

        SurveyPageWidget pageWidget = createNewSurveyPage();
        selectSurveyPage(pageWidget.getWidgetId());
        
        indexOfLastQuestionLoaded = 0;
        RepeatingCommand rc = createPageBuilderCommand(
                loadedSurvey.getPages().get(0), 
                pageWidget, 
                0, 
                pageSize, 
                null);
        Scheduler.get().scheduleFixedDelay(rc, LOAD_SURVEY_SCHEDULER_DELAY_MS);
    }
    
    /**
     * Handles the event when the filter button was clicked.
     * 
     * @param event the {@link SurveyFilterEvent}
     */
    @EventHandler
    protected void onSurveyFilterEvent(SurveyFilterEvent event) {
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("onSurveyFilterEvent: " + event);
        }
        
        filterWidget.setVisible(!filterWidget.isVisible());        
    }
    
    /**
     * Handles the event when we want to save the survey.
     * 
     * @param event the {@link SurveySaveEvent}
     */
    @EventHandler
    protected void onSurveySaveEvent(SurveySaveEvent event) {
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("onSurveySaveEvent: " + event);
        }
        
        cachedSaveEvent = event;
        onSaveSurvey();
    }
    
    /**
     * Handles the event when we want to import a survey.
     * 
     * @param event the {@link SurveyImportEvent}
     */
    @EventHandler
    protected void onSurveyImportEvent(SurveyImportEvent event) {
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("onSurveyImportEvent: " + event);
        }
        
        dndImportModal.center();
    }

    /**
     * Handles the event when the preview button was clicked.
     * 
     * @param event the {@link SurveyPreviewEvent}
     */
    @EventHandler
    protected void onSurveyPreviewEvent(SurveyPreviewEvent event) {
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("onSurveyPreviewEvent: " + event);
        }

        onPreviewSurvey();
    }
    
    /**
     * Updates listeners whenever a survey references is selected by the author
     *
     * @param reference the reference that was selected
     */
    public void onSurveyReferenceSelected(AbstractSurveyReference reference) {    

        if(logger.isLoggable(Level.INFO)) {
            logger.info("onSurveyReferenceSelected: " + reference);
        }

        surveyHeader.onSurveyReferenceSelected(reference);
    }

    /**
     * Handler for when the score value has changed (and the score totals should be 
     * recomputed).
     */
    private void onScoreValueChanged() {            
        
        // Update the scoring header if it's being displayed.
        if (surveyType == SurveyDialogOption.ASSESSLEARNER_STATIC) {           

            // Iterate through each page (and each item on each page) to get 
            // the possible total points available on the survey.
            Double totalPoints = 0.0;
            
            for (Widget widget : surveyPageContainer) {
                if (widget instanceof SurveyPageWidget) {
                    SurveyPageWidget pageWidget = (SurveyPageWidget) widget;
                    totalPoints += pageWidget.getTotalPoints();
                }
            }
            
            kasScoringLogicWidget.setPossibleTotalPoints(totalPoints);
            // refreshing won't update the scores until the widget is marked as initialized.
            kasScoringLogicWidget.refresh(false);            
        } else if (surveyType == SurveyDialogOption.COLLECTINFO_SCORED) {
            Double totalPoints = 0.0;
            HashMap<LearnerStateAttributeNameEnum, Double> attributeScoreMap = new HashMap<LearnerStateAttributeNameEnum, Double>();
            for (Widget widget : surveyPageContainer) {
                if (widget instanceof SurveyPageWidget) {
                    SurveyPageWidget pageWidget = (SurveyPageWidget)widget;
                    pageWidget.computeTotalPointsPerAttribute(attributeScoreMap);
                    totalPoints += pageWidget.getTotalPoints();
                }
            }
            
            if(logger.isLoggable(Level.INFO)) {
                logger.info("Attribute Score Map size = " + attributeScoreMap.size());
            }
            
            scoringLogicWidgetCuiS.setPossibleTotalPoints(totalPoints);
            scoringLogicWidgetCuiS.setTotalPointsPerAttribute(attributeScoreMap);
            
            // Go through and get totals for each attribute type.
            scoringLogicWidgetCuiS.refresh();
        }
    }

    /**
     * Called when the survey editor mode has changed (writing mode to scoring mode).
     * 
     * @param editMode - The new editor mode (writing or scoring mode).
     */
    private void onEditorModeChanged(SurveyEditMode editMode) {
        
        editorMode = editMode;
        currentWidgetIndex = 0;
        editorModeProgressModal.reset();
        editorModeProgressModal.setPages(surveyPageContainer.getWidgetCount());

        /* 
         * If the survey panel has been initialized and the user has loaded more than one page,
         * show the editor mode progress dialog
         */
                
        if(surveyPageContainer.getWidgetCount() > 0 && editorModeProgressModal.numPages > 0) {
            editorModeProgressModal.center();
        }
        
        surveyHeader.setMode(editMode);
        footerWidget.setMode(editMode);

        changeNextSurveyPageEditMode(editMode);
    }
    
    /**
     * Changes the edit mode for the next survey page. Does nothing if there are no pages,
     * or if all pages have been updated.
     *  
     * @param editMode The edit mode
     */
    private void changeNextSurveyPageEditMode(SurveyEditMode editMode) {
        
        while(currentWidgetIndex < surveyPageContainer.getWidgetCount()) {

            // Update the ui components based on the mode that is being displayed.      

            Widget widget = surveyPageContainer.getWidget(currentWidgetIndex);
            currentWidgetIndex += 1;
            
            if (widget instanceof SurveyPageWidget) {
                
                SurveyPageWidget pageWidget = (SurveyPageWidget)widget;
                pageWidget.onEditorModeChanged(editorMode, pageChangedCallback);
                
                /*
                 * Break the while loop to allow the progress dialog to update accurately for
                 * the current page. The pageChangedCallback will handle the following pages.
                 */
                break;
            }
        }
        
    }
    
    
    /**
     * Updates the percent complete of the edit mode progress dialog.
     * 
     * @param pageNumber The current page
     * @param current The current survey element being updated
     * @param total The total number of survey elements on the current page.
     */
    public static void updateEditModeProgress(int pageNumber, int current, int total) {
        editorModeProgressModal.setProgress(pageNumber, current, total);
    }
        
    /** 
     * Scrolls the document to a specified html element based on the element's html id.  
     * 
     * @param elementId - The html id of the element to be scrolled to.
     */
    private void scrollToElement(final String elementId) {
        
        if (elementId != null && !elementId.isEmpty()) {
            Scheduler.get().scheduleDeferred(new Command() {

                @Override
                public void execute() {
                    
                    if (Document.get().getElementById(elementId) != null) {
                        Document.get().getElementById(elementId).scrollIntoView();
                        
                    } else if(logger.isLoggable(Level.SEVERE)) {
                        logger.severe("scrollToElement() called with element id: " + elementId + ", but the element could not be found.");
                    }
                }
                
            });
        } 
    }

    /**
     * Selects a question on a specified page.
     * 
     * @param pageWidgetId - The widget id of the page containing the question.
     * @param questionId - The widget id of the question to be selected.
     * @param keepPreviousSelection - Whether or not to allow previously selected questions to remain selected
     */
    private void selectSurveyQuestion(SurveyWidgetId pageWidgetId, SurveyWidgetId questionId, boolean keepPreviousSelection) {
        
        for (Widget widget : surveyPageContainer) {
            if (widget instanceof SurveyPageWidget) {
                SurveyPageWidget pageWidget = (SurveyPageWidget)widget;
                if (pageWidgetId.equals(pageWidget.getWidgetId())) {
                    
                    boolean selectingAgain = pageWidget.selectQuestion(questionId, keepPreviousSelection);
                    if (!selectingAgain) {
                    scrollToElement(questionId.getWidgetId());
                    }
                    
                    selectSurveyPage(pageWidgetId, keepPreviousSelection);
                    
                } else {
                    
                    if(!keepPreviousSelection){
                        deSelectPage(pageWidget);
                    }
                }
            }
        }
    }
    
    /**
     * Selects All of the survey questions in all of the available pages
     */
    void selectAllSurveyQuestions() {
        
        for (Widget widget : surveyPageContainer) {
            if (widget instanceof SurveyPageWidget) {
                SurveyPageWidget pageWidget = (SurveyPageWidget)widget;              
                    
                for(QuestionContainerWidget qWidget : pageWidget.getQuestionWidgets()){
                    
                    if(!qWidget.isSelected()){
                        //ignore if already selected
                        
                        if(isSelectionMode 
                                && copyTargetType != null 
                                && copyTargetType.equals(SurveyDialogOption.ASSESSLEARNER_QUESTIONBANK)){
                            //question bank surveys can only support copying questions of certain types
                            
                            Widget questionWidget = qWidget.getQuestionWidget();
                            
                            boolean supportedByQuestionBank = false;
                            if (questionWidget instanceof AbstractQuestionWidget) {
                                SurveyItemType surveyType = SurveyWidgetFactory.getQuestionWidgetType((AbstractQuestionWidget) questionWidget);
                                supportedByQuestionBank = SurveyWidgetFactory.isSupportedByQuestionBank(surveyType);
                            }

                            if (!supportedByQuestionBank) {
                                // shouldn't select this question because its not a supported
                                // question type for question bank survey type
                                continue;
                            }
                        }
                        
                        pageWidget.selectQuestion(qWidget.getWidgetId(), true);
                    }
                }

            }
        }
    }
    
    /**
     * Deselects all of the survey questions in all of the available pages
     */
    void deselectAllSurveyQuestions() {
        
        for (Widget widget : surveyPageContainer) {
            if (widget instanceof SurveyPageWidget) {
                SurveyPageWidget pageWidget = (SurveyPageWidget)widget;              
                    
                for(QuestionContainerWidget qWidget : pageWidget.getQuestionWidgets()){
                    
                    if(qWidget.isSelected()){                       
                        pageWidget.selectQuestion(qWidget.getWidgetId(), true);
                    }
                }

            }
        }
    }
    
    /**
     * Deselects the the page.
     * 
     * @param pageWidget - The page to deselect.
     */
    private void deSelectPage(SurveyPageWidget pageWidget) {
        if (pageWidget != null) {
            pageWidget.setSelected(false);
            pageWidget.selectQuestion(null, false);
            List<QuestionContainerWidget> questions = pageWidget.getQuestionWidgets();
            for (QuestionContainerWidget question : questions) {
                question.setSelected(false);
            }
        }
    }


    /**
     * Updates the properties panel widget with a selected widget.
     * 
     * @param pageWidget - The page that has a selected item.  Null means that the properties panel should be cleared.
     */
    private void updatePropertiesPanel(SurveyPageWidget pageWidget) {

        if (pageWidget != null) {
            AbstractQuestionWidget selectedWidget = pageWidget.getLastSelectedQuestionWidget();
            
            propertiesPanel.displayProperties(selectedWidget);
        } else {
            // The page has been deleted, so there is nothing selected.  Clear out the properties panel.
            propertiesPanel.displayProperties(null);
        }
        
        
    }


    /**
     * Deletes the survey question.
     * 
     * @param pageWidgetId - The page widget id containing the question.
     * @param questionId - The widget id of the question to be deleted.
     */
    private void deleteSurveyQuestion(SurveyWidgetId pageWidgetId, SurveyWidgetId questionId) {
        
        for (int x=0; x < surveyPageContainer.getWidgetCount(); x++) {
            Widget widget = surveyPageContainer.getWidget(x);
            if (widget instanceof SurveyPageWidget) {
                SurveyPageWidget pageWidget = (SurveyPageWidget)widget;
                if (pageWidgetId.equals(pageWidget.getWidgetId())) {
                    //Delete the survey question from the data model if paging logic is being used
                    if(surveyType == SurveyDialogOption.ASSESSLEARNER_QUESTIONBANK) {
                        if (loadedSurvey.getPageCount() > 0) {
                            AbstractSurveyElement elementToDel = questionIdToDataModel.get(questionId);
                            if(elementToDel != null) {
                                // Cannot use list.remove() here because the element's .equals won't
                                // match. Manually search through the list and compare element IDs.
                                // Remove the element from the survey element list and the
                                // questionIdToDataModel.
                                Iterator<AbstractSurveyElement> itr = loadedSurvey.getPages().get(x).getElements().iterator();
                                while (itr.hasNext()) {
                                    if (itr.next().getWidgetId().equals(elementToDel.getWidgetId())) {
                                        itr.remove();
                                        questionIdToDataModel.remove(questionId);        
                                        break;
                            }
                        }
                    }
                        }
                    }
                    
                    //Delete the survey question from the UI
                    pageWidget.deleteQuestion(questionId);
                    updatePropertiesPanel(pageWidget);
                    SharedResources.getInstance().getEventBus().fireEvent(new SurveyScoreValueChangedEvent());
                    break;
                }
            }
        }
        
    }


    /**
     * Moves the survey question up or down in order.
     * 
     * @param pageWidgetId The page widget id containing the question.
     * @param questionId - The widget id of the question to be moved.
     * @param pageOrder - The order the question should be moved (up or down).
     */
    private void moveSurveyQuestion(SurveyWidgetId pageWidgetId, SurveyWidgetId questionId, MoveOrderEnum pageOrder) {
        
        for (Widget widget : surveyPageContainer) {
            if (widget instanceof SurveyPageWidget) {
                SurveyPageWidget pageWidget = (SurveyPageWidget)widget;
                if (pageWidgetId.equals(pageWidget.getWidgetId())) {
                    pageWidget.moveSurveyQuestion(questionId, pageOrder);
                    break;
                }
            }
        }
    }

    /**
     * Copies all of the currently selected questions to the location of the question with the specified widget ID
     * 
     * @param pageWidgetId The page widget id containing the question.
     * @param questionId - The widget id of the question where the copies should be placed
     */
    private void copySelectedSurveyQuestions(SurveyWidgetId pageWidgetId, SurveyWidgetId questionId) {
        
        List<QuestionContainerWidget> selectedQuestions = getSelectedQuestionWidgets();
        
        copySurveyQuestions(pageWidgetId, questionId, selectedQuestions);
    }
            
    /**
     * Copies the contents of all of the given question widgets to the location of the question with the specified widget ID
     * 
     * @param pageWidgetId The page widget id containing the question.
     * @param questionId - The widget id of the question where the copies should be placed. Can be null if the selected
     * questions should be copied to the page widget's current insertion index
     * @param selectedWidgets the widgets to copy content from
     */
    private void copySurveyQuestions(SurveyWidgetId pageWidgetId, SurveyWidgetId questionId, List<QuestionContainerWidget> selectedWidgets) {
        
        SurveyPageWidget targetPage = null;

        // If we are coming back to a question bank, set the page number back to 1
        if (surveyType == SurveyDialogOption.ASSESSLEARNER_QUESTIONBANK) {
            // Set the page count to 1 since we only have one page for question banks
            editorModeProgressModal.setPages(1);
        }
        
        //iterate through each page to get its selected question widgets
        for (Widget widget : surveyPageContainer) {
            if (widget instanceof SurveyPageWidget) {
                
                SurveyPageWidget pageWidget = (SurveyPageWidget)widget;                
                
                if (pageWidgetId.equals(pageWidget.getWidgetId())) {
                    targetPage = pageWidget;
                    break;
                }
            }
        }
        
        if(targetPage == null){
            
            hideLoadingDialog();
            
            ErrorDetailsDialog dialog = new ErrorDetailsDialog("Failed to copy the survey item(s) to the survey.", "Failed to find the survey page widget with id "+pageWidgetId+".", null);
            dialog.setTitle("Survey Item Copy Error");
            dialog.center();
            
            clearCachedSaveEvent();
            return;
        }
        
        if(questionId == null){
            
            if(InsertOrder.ORDER_BEFORE.equals(targetPage.getInsertOrder())
                    || targetPage.getInsertQuestionId() == null){
                
                //if the question widgets are being copied to the location before the page's current insertion index, then
                //we need to insert the widgets in reverse order, otherwise they won't maintain their ordering
                Collections.reverse(selectedWidgets);
            }
        }
        
        if(!selectedWidgets.isEmpty()){
            
            boolean isScored = !getSurveyType().equals(SurveyDialogOption.COLLECTINFO_NOTSCORED);
                    
            for(QuestionContainerWidget questionWidget : selectedWidgets){      
                
                //copy the contents of each question widget by saving them to an arbitrary list
                List<AbstractSurveyElement> savedElements = new ArrayList<AbstractSurveyElement>();
                try {
                    questionWidget.save(savedElements, loadedSurvey.getEditableToUserNames(), loadedSurvey.getVisibleToUserNames());
                    
                    // loop through the elements and replace the list options property value (which
                    // contains the reference to the original question's list option) with a new
                    // instance.
                    for (AbstractSurveyElement element : savedElements) {
                        if (element instanceof AbstractSurveyQuestion) {
                            SurveyItemProperties qProperties = ((AbstractSurveyQuestion<?>) element).getQuestion().getProperties();
                            List<SurveyPropertyKeyEnum> optionListKeys = new ArrayList<SurveyPropertyKeyEnum>();
                            
                            if (qProperties.hasProperty(SurveyPropertyKeyEnum.REPLY_OPTION_SET_KEY)) {
                                optionListKeys.add(SurveyPropertyKeyEnum.REPLY_OPTION_SET_KEY);
                            }
                            if (qProperties.hasProperty(SurveyPropertyKeyEnum.COLUMN_OPTIONS_KEY)) {
                                optionListKeys.add(SurveyPropertyKeyEnum.COLUMN_OPTIONS_KEY);
                            }
                            if (qProperties.hasProperty(SurveyPropertyKeyEnum.ROW_OPTIONS_KEY)) {
                                optionListKeys.add(SurveyPropertyKeyEnum.ROW_OPTIONS_KEY);
                            }
                            
                            for (SurveyPropertyKeyEnum key : optionListKeys) {
                                OptionList optionList = (OptionList) qProperties.getPropertyValue(key);

                                // We do not want to copy the option list for this element if it is a shared option list.
                                // These are uneditable in the GAT and SHOULD reference the same option list object in the database.
                                if (optionList != null && !optionList.getIsShared()) {
                                    List<ListOption> newOptions = new ArrayList<ListOption>();

                                    if (optionList.getListOptions() != null) {
                                        // copy the list options
                                        for (ListOption option : optionList.getListOptions()) {
                                            ListOption listOption = new ListOption(0, option.getText(), 0);
                                            listOption.setSortKey(option.getSortKey());
                                            newOptions.add(listOption);
                                        }
                                    }
                                    
                                    OptionList newOptionList = new OptionList(0, optionList.getName(), optionList.getIsShared(), newOptions,
                                            optionList.getVisibleToUserNames(), optionList.getEditableToUserNames());

                                    // set new options list into property set
                                    qProperties.setPropertyValue(key, newOptionList);
                                }
                            }
                        }
                    }
                } catch (SaveSurveyException e) {
                    
                    if(questionWidget.getWidgetId() != null){
                        
                        if(logger.isLoggable(Level.WARNING)) {
                            logger.log(Level.WARNING, "Failed to copy the content of a question widget with an ID of " 
                                    + questionWidget.getWidgetId().getWidgetId(), e
                            );
                        }
                        
                    } else if(logger.isLoggable(Level.WARNING)) {
                        logger.log(Level.WARNING, "Failed to copy the content of an unknown question widget.", e);
                    }
                }
                
                if(!savedElements.isEmpty() 
                        && questionWidget.getQuestionWidget() != null
                        && questionWidget.getQuestionWidget() instanceof AbstractQuestionWidget){
                
                    //create a new instance of the original question widget's type
                    SurveyItemType type = SurveyWidgetFactory.getQuestionWidgetType(
                            (AbstractQuestionWidget) questionWidget.getQuestionWidget()
                    );
                    
                    //populate that new instance with the copied question data
                    boolean isQuestionBank = getSurveyType() == SurveyDialogOption.ASSESSLEARNER_QUESTIONBANK;
                    AbstractQuestionWidget copyWidget = SurveyWidgetFactory.createSurveyWidget(type, editorMode, isScored, isSelectionMode, isQuestionBank);
                    copyWidget.setReadOnlyMode(isReadOnly());
                    
                    try {
                        copyWidget.load(savedElements.get(0));
                    } catch (LoadSurveyException e) {

                        if(questionWidget.getWidgetId() != null){
                            
                            if(logger.isLoggable(Level.WARNING)) {
                                logger.log(Level.WARNING, "Failed to load copied content from a question widget with an ID of " 
                                        + questionWidget.getWidgetId().getWidgetId(), e
                                );
                            }
                            
                        } else if(logger.isLoggable(Level.WARNING)) {
                            logger.log(Level.WARNING, "Failed to load copied content from an unknown question widget.", e);
                        }
                        
                        continue;
                    }
                    
                    if(questionId != null){
                        targetPage.addWidgetToSurveyPage(copyWidget, true, questionId, InsertOrder.ORDER_AFTER);
                        
                    } else {
                        targetPage.addWidgetToSurveyPage(copyWidget, true);
                    }
                }
            }
        }
    }

    /**
     * Gets the question widgets selected across every page contained by this widget
     * 
     * @return the selected question widgets
     */
    List<QuestionContainerWidget> getSelectedQuestionWidgets(){

        List<QuestionContainerWidget> selectedWidgets = new ArrayList<QuestionContainerWidget>();
        
        //iterate through each page to get its selected question widgets
        for (Widget widget : surveyPageContainer) {
            if (widget instanceof SurveyPageWidget) {
                
                SurveyPageWidget pageWidget = (SurveyPageWidget)widget;                
                List<QuestionContainerWidget> selected = pageWidget.getSelectedQuestionWidgets(true);
                
                if(selected != null){
                    
                    //get each page's selected question widgets
                    for(QuestionContainerWidget qWidget : selected){                    
                        selectedWidgets.add(0, qWidget);
                    }
                }
            }
        }
        
        return selectedWidgets;
    }


    /**
     * There should only be one 'add survey item widget' opened at a time.
     * This function is called when the widget should be shown.
     * @param pWidgetId - The page widget id that the 'add survey item' widget will be shown for.  Null means that the 'add survey item' widget
     *                    will be shown on the selected page.
     * @param qWidgetId - The question widget id that the 'add survey item' widget will be shown (before or after).  Null means that the 
     *                    'add survey item' widget will be shown at the end of the page.
     * @param order - The order (before or after) that the 'add survey item' widget will be shown based on the specified qWidgetId.
     */
    private void showAddSurveyItemWidget(SurveyWidgetId pWidgetId, SurveyWidgetId qWidgetId, InsertOrder order) {
        
        
        if (pWidgetId == null && qWidgetId == null) {
            // If there is no page widget id passed in, then the survey element is added to the
            // end of the selected page.
            
            //When the selected page has not been set in a question bank survey, select the one and only page
            //for the author (since survey page authoring is hidden for question bank authoring)
            if (selectedPage == null && surveyType == SurveyDialogOption.ASSESSLEARNER_QUESTIONBANK && surveyPageContainer.getWidgetCount() > 0) {
                for (Widget widget : surveyPageContainer) {
                    if (widget instanceof SurveyPageWidget) {
                        selectedPage = (SurveyPageWidget) widget;
                        break;
                    }
                }
            }
            
            if(selectedPage == null){
                //show a message to the user to display a page first                 
                WarningDialog.info("Select a page", "Please select or create a survey page first, then add survey item(s) to that page.");
            }else{            
                selectedPage.showSelectSurveyItemWidget(null, order);
            }
        } else {
            for (Widget widget : surveyPageContainer) {
                if (widget instanceof SurveyPageWidget) {
                    SurveyPageWidget pageWidget = (SurveyPageWidget)widget;
                    if (pageWidget.getWidgetId().equals(pWidgetId)) {
                        selectedPage.showSelectSurveyItemWidget(qWidgetId, order);
                        break;
                    }
                }
            }
        }
       
        
    }


    /**
     * Selects a survey page based on the page widget id.
     * 
     * @param wId - The widget id of the page to be selected.
     */
    private void selectSurveyPage(SurveyWidgetId wId) {
        
        selectSurveyPage(wId, false);
    }
    
    /**
     * Selects a survey page base don the page widget id.
     * 
     * @param wId - The widget id of the page to be selected.
     * @param keepPreviousSelection - Whether or not to allow previously selected pages to remain selected
     */
    private void selectSurveyPage(SurveyWidgetId wId, boolean keepPreviousSelection) {
        for (Widget widget : surveyPageContainer) {
            if (widget instanceof SurveyPageWidget) {
                SurveyPageWidget pageWidget = (SurveyPageWidget)widget;
                
                if (wId.equals(pageWidget.getWidgetId())) {
                    pageWidget.setSelected(true);
                    
                    selectedPage = pageWidget;
                    
                    updatePropertiesPanel(pageWidget);
                    
                    if(logger.isLoggable(Level.INFO)) {
                        logger.info("selected page updated: " + selectedPage.getWidgetId());
                    }
                    
                } else {
                    
                    if(!keepPreviousSelection){
                        deSelectPage(pageWidget);
                    }
                }
                
            }
        }
    }
    
    
    /**
     * Determines if the page widget id is the first page.
     * 
     * @param wId The page widget id.
     * @return True if the page is the first page, false otherwise.
     */
    private boolean isFirstPage(SurveyWidgetId wId) {
        boolean isFirst = false;
        

        if (surveyPageContainer.getWidgetCount() > 0) {
            int firstIndex = 0;
            Widget widget = surveyPageContainer.getWidget(firstIndex);
            if (widget != null && widget instanceof SurveyPageWidget) {
                SurveyPageWidget pageWidget = (SurveyPageWidget)widget;
                if (wId.equals(pageWidget.getWidgetId())) {
                    
                    isFirst = true;
                }
            }
        }   
        
        return isFirst;
    }
    
    /**
     * Determines if the page widget id is the last page.
     * 
     * @param wId The page widget id.
     * @return True if the page is the last page, false otherwise.
     */
    private boolean isLastPage(SurveyWidgetId wId) {
        boolean isLast = false;
        

        if (surveyPageContainer.getWidgetCount() > 0) {
            int lastIndex = surveyPageContainer.getWidgetCount() - 1;
            Widget widget = surveyPageContainer.getWidget(lastIndex);
            if (widget != null && widget instanceof SurveyPageWidget) {
                SurveyPageWidget pageWidget = (SurveyPageWidget)widget;
                if (wId.equals(pageWidget.getWidgetId())) {
                    isLast = true;
                }
            }
        }   
        
        return isLast;
    }



    /**
     * Moves a survey page based on the pageId up or down in order.
     * 
     * @param wId the widget id of the page to be moved.
     * @param pageOrder The order to move the page by one (up or down).
     */
    private void moveSurveyPage(SurveyWidgetId wId, MoveOrderEnum pageOrder) {

        
        // TODO - This move could be an expensive operation since the entire page
        // needs to be removed and reinserted into the DOM.  If performance becomes an issue
        // it may be worthwhile to see if this could be improved, or some type of async operation
        // would need to be displayed to the user if the move takes awhile.
        int lastIndex = surveyPageContainer.getWidgetCount() - 1;
        for (int x = 0; x < surveyPageContainer.getWidgetCount(); x++) {
            Widget widget = surveyPageContainer.getWidget(x);
            if (widget instanceof SurveyPageWidget) {
                SurveyPageWidget pageWidget = (SurveyPageWidget)widget;
                if (wId.equals(pageWidget.getWidgetId())) {
                    
                    if ((x == 0 && pageOrder == MoveOrderEnum.ORDER_UP) || 
                       (x == lastIndex && pageOrder == MoveOrderEnum.ORDER_DOWN)) {
                        break;
                    }
                    
                    
                    surveyPageContainer.remove(pageWidget);
                    int widgetCount = surveyPageContainer.getWidgetCount();
                    
                    if (pageOrder == MoveOrderEnum.ORDER_UP) {
                        
                        int moveIndex = x - 1;
                        
                        if (moveIndex < 0) {
                            moveIndex = 0;
                        }
                        // Move the question up
                        surveyPageContainer.insert(pageWidget, moveIndex);
                        
                        //Update the page titles
                        pageWidget.setPageNumber(moveIndex + 1);
                        
                        SurveyPageWidget otherPage = (SurveyPageWidget) surveyPageContainer.getWidget(x);
                        if(otherPage != null) {
                            otherPage.setPageNumber(x + 1);
                        }
                    } else {
                        
                        int moveIndex = x + 1;
                        if (moveIndex >= widgetCount) {
                            surveyPageContainer.add(pageWidget); 
                            moveIndex = surveyPageContainer.getWidgetCount() - 1;
                        } else {
                            surveyPageContainer.insert(pageWidget,  moveIndex);
                        }    
                        
                        //Update the page titles
                        pageWidget.setPageNumber(moveIndex + 1);
                        SurveyPageWidget otherPage = (SurveyPageWidget) surveyPageContainer.getWidget(x);
                        if(otherPage != null) {
                            otherPage.setPageNumber(x + 1);
                        }
                        
                    }
                    
                    // Some widgets such as the slider bar widgets need to be refreshed when
                    // they are reattached to the DOM.  This method allows any widgets that need
                    // to be refreshed the opportunity to do so.
                    pageWidget.refreshContentsOnReattach();
                    break;
                }
            }
        } 
        
        
        
    }

    /**
     * Deletes a survey page from the workspace based on the pageId.
     * 
     * @param widgetId The widget id of the page to be deleted.
     */
    private void deleteSurveyPage(SurveyWidgetId widgetId) {
        
        if(logger.isLoggable(Level.WARNING)) {
            logger.warning("deleteSurveyPage: Looking for widget '" + widgetId.getWidgetId() + "'");
        }
        
        //Used to determine when to begin updating titles
        boolean itemFound = false;
        
        for (int x = 0; x < surveyPageContainer.getWidgetCount(); x++) {
            Widget widget = surveyPageContainer.getWidget(x);
            if (widget instanceof SurveyPageWidget) {
                SurveyPageWidget pageWidget = (SurveyPageWidget)widget;
                
                if (!itemFound && widgetId.equals(pageWidget.getWidgetId())) {
                    // Page widget's survey page can be null if the survey page was added and deleted before it was saved
                    if (pageWidget.getSurveyPage() != null && !loadedSurvey.getPages().remove(pageWidget.getSurveyPage())) {
                        
                        if(logger.isLoggable(Level.SEVERE)) {
                            logger.severe("Failed to remove SurveyPage " + pageWidget.getSurveyPage().getId() + " (" + pageWidget.getSurveyPage().getSurveyId() + ")" + " for PageWidget " + pageWidget.getWidgetId() + " from loaded survey.");
                        }
                    }

                    surveyPageContainer.remove(pageWidget);

                    if (selectedPage != null && selectedPage.getWidgetId().equals(widgetId)) {
                        selectedPage = null;
                        updatePropertiesPanel(null);
                    }
                    SharedResources.getInstance().getEventBus().fireEvent(new SurveyScoreValueChangedEvent());
                    
                    //Marks that the item in question was found
                    itemFound = true;
                    editorModeProgressModal.deletePage();
                    
                    //Decrements x so that the next item isn't skipped
                    x--;
                }
                
                else if(itemFound) {
                    //Offsets each of the following pages titles
                    pageWidget.setPageNumber(x + 1);
                }
            }
        }
    }


    /**
     * Sets the type of survey that will be authored for the survey editor panel.
     * 
     * @param type - The type of survey that will be authored for the survey editor panel.
     */
    public void setSurveyType(SurveyDialogOption type) {
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("Setting survey type to: " + type);
        }
        
        surveyType = type;
    }
    
    /**
     * Gets the type of survey that is being authored by the survey editor panel.
     * 
     * @return SurveyDialogOption - The type of survey that is being authored by the editor panel.
     */
    public SurveyDialogOption getSurveyType() {
        return surveyType;
    }
    
    /**
     * Resets the panel and re-initializes it based on the current mode.
     * 
     * @param isNewSurvey true if the survey we are resetting is new; false if it was pre-existing.
     */
    private void resetPanel(boolean isNewSurvey) {
        
        currentPageIndex = 0;
        selectedPage = null;
        
        updatePropertiesPanel(null);

        filterContainer.clear();
        workspaceWidget.clear();
        surveyPageContainer.clear();
        
        // This is to prevent the scrollbar from appearing for the question container
        // widget if the questions don't flow past the height of the container. Since
        // the height value is set on the DynamicHeaderScroll, we change it by accessing
        // it as the parent of workspace widget.
        workspaceWidget.getParent().getElement().getStyle().clearHeight();
        
        if (surveyType == SurveyDialogOption.ASSESSLEARNER_STATIC) {
            workspaceWidget.add(kasScoringLogicWidget);
            // show the scoring logic widget.
            kasScoringLogicWidget.setVisible(true);
            kasScoringLogicWidget.initialize(loadedSurvey, isNewSurvey);
            kasScoringLogicWidget.refresh(false);
           
        } else if (surveyType == SurveyDialogOption.COLLECTINFO_SCORED) {
            workspaceWidget.add(scoringLogicWidgetCuiS);
            // show the scoring logic widget.
            scoringLogicWidgetCuiS.setVisible(true);
            scoringLogicWidgetCuiS.initialize(loadedSurvey, isNewSurvey, isReadOnly);
            scoringLogicWidgetCuiS.refresh();
            
        }
        
        if(surveyType == SurveyDialogOption.ASSESSLEARNER_QUESTIONBANK) {
            List<String> conceptNames = surveyResources != null ? surveyResources.getConcepts() : null;
            
            filterWidget = new QuestionFilterWidget(conceptNames);
            filterWidget.setVisible(false);
            filterContainer.add(filterWidget);
            workspaceWidget.add(surveyPageContainer);
            workspaceWidget.add(loadMoreQuestions);
        } else {
            workspaceWidget.add(surveyPageContainer);
        }
        
    }
    
    /**
     * Initializes the panel to author a specified type of survey.
     * 
     * @param type - The type of survey that will be authored.
     * @param resources - The global set of survey resources (e.g. survey context, concepts, etc.) that this widget should use
     * @param transitionName - The default transition name for the survey. 
     * @param surveyContextKey - The surveyContextKey assigned for the transition (if any), this can be null or empty.
     * @param callback - The callback for when the survey context has been selected/saved in the survey editor.
     */
    public void initializePanel(final SurveyDialogOption type, final AbstractSurveyResources resources, String transitionName, String surveyContextKey,
                                SelectSurveyContextCallback callback) {
        
        if(logger.isLoggable(Level.FINE)) {
            logger.fine("initializePanel(" + type + ", " + resources + ", " + transitionName + ", " + callback);
        }
        
        wasConverted = false;
        contextCallback = callback;
        surveyTransitionName = transitionName;
        surveyContextSurvey = null; //so it will be created for this new survey upon saving
        surveyKey = surveyContextKey;
        loadedSurvey = new Survey();
        loadedSurvey.setProperties(new SurveyProperties());
        
        editorModeProgressModal = new SurveyElementProgressModal();
        
        // Default survey properties.
        defaultSurveyProperties(loadedSurvey.getProperties(), type);
        
        //if survey is an assess learner knowledge, then go ahead and add the single knowledge scoring attribute
        if(type.equals(SurveyDialogOption.ASSESSLEARNER_STATIC)){
            loadedSurvey.getScorerModelForNewSurvey().getAttributeScorers().add(
                    new AttributeScorerProperties(LearnerStateAttributeNameEnum.KNOWLEDGE, createReturnValue(LearnerStateAttributeNameEnum.KNOWLEDGE)));
        }

        boolean useWildcard = StringUtils.equals(GatClientUtility.getUserName(), GatClientUtility.GIFT_WRAP_DESKTOP_USER);
        loadedSurvey.getVisibleToUserNames().add(useWildcard ? Constants.VISIBILITY_WILDCARD : GatClientUtility.getUserName());
        loadedSurvey.getEditableToUserNames().add(useWildcard ? Constants.EDITABLE_WILDCARD : GatClientUtility.getUserName());

        // User is loading a new survey, show the loading page while the survey data (like survey context is loaded).
        showLoadingPanel();
        showLoadingDialog("Loading the Survey", "Please wait...loading the survey.");
        
        // Default to writing mode for every survey type.
        setMode(SurveyEditMode.WritingMode);
        
        // Sets the survey type.
        setSurveyType(type);
        
        setSurveyResources(resources);

        if(logger.isLoggable(Level.INFO)) {
            logger.info("fetching survey context");
        }
        
        // The rpc load sequence is as follows:
        //  fetch survey images rpc
        //  fetch option lists (answer sets) rpc
        // Those are called sequentially and once all have succeeded the editor loads.
        // If any fails, then the user is displayed an error message and the editor is closed.
        
        if(surveyResources != null && surveyResources.getSurveyContextId() > 0){
            
            if(logger.isLoggable(Level.INFO)) {
                logger.info("using survey context id of "+surveyContextId);
            }
            
            surveyContextId = surveyResources.getSurveyContextId();
        }else{
            hideLoadingDialog();
            WarningDialog.error("Unable to load the survey editor.", "The survey context id has not been set", 
                    new ModalDialogCallback() {

                        @Override
                        public void onClose() {
                            if (closeableInterface != null) {
                                closeableInterface.close();
                            }
                        }
            });
            return;
        }
                    

        // Fetch survey images & option lists.
        updateSurveyImageList(new AsyncCallback<String>() {

            @Override
            public void onFailure(Throwable t) {
                displayCriticalLoadError("Unable to load the survey editor.  There was a critical error fetching the server images: " + t.getMessage());
            }

            @Override
            public void onSuccess(String result) {

                getOptionLists(new AsyncCallback<String>() {

                    @Override
                    public void onFailure(Throwable t) {                        
                        displayCriticalLoadError("Unable to load the survey editor.  There was a critical error fetching answer sets from the server: " + t.getMessage());
                    }

                    @Override
                    public void onSuccess(String result) {
                        
                        // Remove the loading dialog and allow other dialogs to appear if needed.
                        hideLoadingDialog();
                        // Success, finish setting up the editor and hide the loading panel.
                        finishNewPanelInitialization(type);
                        hideLoadingPanel();
                    }
                });
            }
        });

        setReadOnlyMode(GatClientUtility.isReadOnly());
    }
    
    /**
     * Finishes the initialization of the survey editor panel for a new survey.  This should be called
     * after the survey context, survey images, and survey option sets are retrieved from the server.
     * 
     * @param type - The type of survey that will be authored.
     * @param courseData - The course object data associated with the survey.
     */
    private void finishNewPanelInitialization(SurveyDialogOption type) {
        
        // The mode is being switched, so clear out any previous data and rebuild the panel.
        resetPanel(true);
        
        permissionsManager.initialize();
        
        
        String headerSurveyName = surveyTransitionName;
        
        if (type == SurveyDialogOption.ASSESSLEARNER_QUESTIONBANK) {
            headerSurveyName = GatClientUtility.getCourseName() + GwtSurveySystemProperties.KNOWLEDGE_ASSESSMENT_QBANK_GIFT_SURVEY_NAME_POST_FIX;
            loadedSurvey.setName(headerSurveyName);
            loadedSurvey.getProperties().setBooleanPropertyValue(SurveyPropertyKeyEnum.UNPRESENTABLE, true);
        }

        surveyHeader.initializeWidget(type, headerSurveyName);
        footerWidget.initializeWidget(type, isReadOnly);
        addNewSurveyPage();
        
        acquireSurveyLocks(new AsyncCallback<Boolean>() {

            @Override
            public void onFailure(Throwable arg0) {
                // Nothing to do here.  The failure is not thrown from the acquireSurveyLock method.
                WarningDialog.error("Failed to acquire survey lock", "Server failure occurred while attempting to aquire a lock on the survey.  You will not be able to edit the survey.");
                setReadOnlyMode(true);
            }

            @Override
            public void onSuccess(Boolean result) {
                
                if (result) {
                    // don't need to check dependencies in gift wrap mode
                    if (GatClientUtility.isGIFTWrapMode()) {
                        return;
                    }
                    
                    if(logger.isLoggable(Level.INFO)) {
                        logger.info("Checking permissions for existing survey.");
                    }
                    
                    showLoadingDialog(CHECK_DEPENDENCIES_TITLE, CHECK_DEPENDENCIES_MESSAGE);
                    checkSurveyPermissions(false, null);  //don't need a callback because there is nothing to do after this, unlike loading a survey
                                                          //which needs to handle the scoring details after the survey is loaded
                    
                } else {
                    // nothing to do here.  Readonly method will be set internally in the acquire lock method.
                    
                    WarningDialog.alert("Failed to acquire survey lock", "Unable to obtain a lock to the survey.  Unfortunately somebody else currently possesses the lock. Try again later. You will not be able to edit the survey.");
                    setReadOnlyMode(true);
                }
                
            }
            
        });
        
    }
    
    /**
     * Asynchronous method to acquire the locks for the survey.
     * 
     * @param callback Callback used to signal when the rpc has completed.  True is returned for a successful lock.  False otherwise.
     */
    private void acquireSurveyLocks(final AsyncCallback<Boolean> callback) {
        
        if(logger.isLoggable(Level.FINE)) {
            logger.fine("acquireSurveyLocks(" + callback + ")");
        }

        permissionsManager.acquireSurveyLocks(surveyContextId, loadedSurvey, new AsyncCallback<Boolean>() {

            @Override
            public void onFailure(Throwable t) {
                
                if(logger.isLoggable(Level.SEVERE)) {
                    logger.log(Level.SEVERE, "acquireSurveyLocks() returned failure: " + t.getMessage(), t);
                }
                
                callback.onFailure(t);
            }

            @Override
            public void onSuccess(Boolean lockSuccess) {
                
                if(logger.isLoggable(Level.INFO)) {
                    logger.info("acquireSurveyLocks() returned: " + lockSuccess);
                }
                
                callback.onSuccess(lockSuccess);
            }
        });
    }
    
    /**
     * Checks the permissions for the survey (either a new survey or an existing survey).  This is expected
     * to be done once during the initial load of the survey.
     * 
     * @param isExistingSurvey True if the survey is an existing survey, false if the survey is a new survey (not yet in the survey database).
     * @param callback used to notify success of failure of retrieving the survey permissions from the server and setting the read only
     * flag in this class.
     */
    private void checkSurveyPermissions(final boolean isExistingSurvey, final AsyncCallback<Boolean> callback) {
        
        if(logger.isLoggable(Level.FINE)) {
            logger.fine("checkSurveyPermissions(" + isExistingSurvey + ", " + callback + ")");
        }

        AsyncCallback<Boolean> surveyEditableCheckCallback = new AsyncCallback<Boolean>() {

            @Override
            public void onFailure(Throwable throwable) {
                callback.onFailure(throwable);
            }

            @Override
            public void onSuccess(Boolean isSurveyEditable) {

                if (isSurveyEditable) {
                    
                    // check survey dependencies.
                    permissionsManager.getSurveyDependencies(surveyContextId, loadedSurvey, new AsyncCallback<String>() {

                        @Override
                        public void onFailure(Throwable t) {
                            
                            try{
                                if(logger.isLoggable(Level.SEVERE)) {
                                    logger.log(Level.SEVERE,"getSurveyDependencies() returned failure: " + t.getMessage(), t);
                                }
                                
                                // goto readonly mode.
                                hideLoadingDialog();
                                WarningDialog.error("Failed to check survey dependencies", "Server failure occurred while getting the survey dependencies.  You will not be able to edit the survey.");
                                setReadOnlyMode(true);
                                propertiesPanel.setReadOnlyMode(true);
                                permissionsManager.releaseLocksIgnoreCallback();
                                
                                if(callback != null) {
                                    // The lock for this survey has already been acquired
                                    callback.onFailure(new Exception("Failed to set read only on survey editor.", t));
                                }
                            }catch(Exception e){
                                
                                if(logger.isLoggable(Level.SEVERE)) {
                                    logger.log(Level.SEVERE, "Failed to set read only on survey editor after failing to retrieve survey dependencies.", e);
                                }
                                
                                if(callback != null) {
                                    // The lock for this survey has already been acquired
                                    callback.onFailure(new Exception("Failed to set read only on survey editor.", e));
                                }
                            }
                            
                        }

                        @Override
                        public void onSuccess(String result) {
                            if (result != null && result.equals(SurveyPermissionsManager.NO_DEPENDENCIES)) {
                                
                                if(logger.isLoggable(Level.INFO)) {
                                    logger.info("getSurveyDependencies() found no dependencies.");
                                }
                                
                                hideLoadingDialog();
                                
                                onEditableSurveyLoadComplete(isExistingSurvey);
                                
                                if(callback != null) {
                                    callback.onSuccess(true);
                                }
                                
                            } else {
                                
                                if(logger.isLoggable(Level.INFO)) {
                                    logger.info("getSurveyDependencies() found dependencies.");
                                }
                                
                                if (result != null && result.equals(SurveyPermissionsManager.HAS_RESPONSES_MESSAGE)) {
                                    
                                    hideLoadingDialog();
                                    // If the server has any scored responses, do not allow the user to edit the survey once it has been scored.
                                    OkayCancelDialog.show("Warning", result, "Delete Survey Responses", new OkayCancelCallback() {

                                        @Override
                                        public void okay() {
                                            rpcService.deleteSurveyResponses(surveyContextId, loadedSurvey, GatClientUtility.getUserName(), new AsyncCallback<Boolean>(){

                                                @Override
                                                public void onFailure(Throwable t) {
                                                    
                                                    try{
                                                        if(logger.isLoggable(Level.INFO)) {
                                                            logger.log(Level.SEVERE,"Server failure occurred deleting the survey responses: " + t.getMessage(), t);
                                                        }
                                                        
                                                        hideLoadingDialog();
                                                        WarningDialog.alert("Failed to delete survey responses", "Server failure occurred deleting the survey responses: " + t.getMessage());
                                                        setReadOnlyMode(true);
                                                        propertiesPanel.setReadOnlyMode(true);
                                                        permissionsManager.releaseLocksIgnoreCallback();
                                                        if(callback != null) {
                                                            // The lock for this survey has already been acquired
                                                            callback.onFailure(new Exception("Failed to set read only on survey editor after failing to delete survey responses.", t));
                                                        }
                                                    }catch(Exception e){
                                                        
                                                        if(logger.isLoggable(Level.INFO)) {
                                                            logger.log(Level.SEVERE, "Failed to set read only on survey editor after failing to delete survey responses.", e);
                                                        }
                                                        
                                                        if(callback != null) {
                                                            // The lock for this survey has already been acquired
                                                            callback.onFailure(new Exception("Failed to set read only on survey editor after failing to delete survey responses.", e));
                                                        }
                                                    }
                                                }

                                                @Override
                                                public void onSuccess(Boolean result) {
                                                    
                                                    if(logger.isLoggable(Level.INFO)) {
                                                        logger.info("deleteSurveyResponses returned: " + result);
                                                    }
                                                    
                                                    hideLoadingDialog();
                                                    if (result != null && result) {
                                                        // Survey response deletion was successful, we need to recheck to see if there
                                                        // are any other dependencies.  This is a recursive call.
                                                        showLoadingDialog(CHECK_DEPENDENCIES_TITLE, CHECK_DEPENDENCIES_MESSAGE);
                                                        checkSurveyPermissions(isExistingSurvey, callback);
                                                    } else {
                                                        
                                                        if(logger.isLoggable(Level.INFO)) {
                                                            logger.severe("Server was not successful in deleting the survey responses. Result is: " + result);
                                                        }
                                                        
                                                        WarningDialog.alert("Failed to delete survey responses", "Server was not successful in deleting the survey resonses.  Result returned was: " + result);
                                                        setReadOnlyMode(true);
                                                        propertiesPanel.setReadOnlyMode(true);
                                                        permissionsManager.releaseLocksIgnoreCallback();
                                                        
                                                        if(callback != null) {
                                                            // The lock for this survey has already been acquired
                                                            callback.onFailure(new Exception("Failed to set read only on survey editor because the survey response could not be deleted."));
                                                        }
                                                    }
                                                    
                                                }
                                                
                                            });
                                        }

                                        @Override
                                        public void cancel() {
                                            hideLoadingDialog();
                                            setReadOnlyMode(true); 
                                            propertiesPanel.setReadOnlyMode(true);
                                            permissionsManager.releaseLocksIgnoreCallback();
                                            
                                            if(callback != null) {
                                                callback.onSuccess(true);
                                            }
                                        }
                                        
                                    });
                                } else {
                                    
                                    hideLoadingDialog();
                                    // If the server has any dependencies, then bring up a dialog to allow the user to edit the survey.
                                    String message = "There are dependencies on this survey: <br/>" + result + "<b>Are you sure you want to edit this survey context?</b>";
                                    
                                    OkayCancelDialog.show("Enable Editing?", message, "Enable Editing", new OkayCancelCallback() {

                                        @Override
                                        public void okay() {
                                            
                                            onEditableSurveyLoadComplete(isExistingSurvey);
                                            
                                            if(callback != null) {
                                                callback.onSuccess(true);
                                            }
                                        }

                                        @Override
                                        public void cancel() {
                                            setReadOnlyMode(true);
                                            propertiesPanel.setReadOnlyMode(true);
                                            permissionsManager.releaseLocksIgnoreCallback();
                                            
                                            if(callback != null) {
                                                callback.onSuccess(true);
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    });
                    
                    //set the URL for the Qualtrics help
                    //Note: doing this outside of the constructor so that the server properties are available,
                    //      also the Qualtrics upload dialog is not available in read only mode anyway so this if is a good place.
                    try{
                        String versionName = GatClientUtility.getServerProperties().getDocumentationToken();
                        helpAnchor.setHref("https://gifttutoring.org/projects/gift/wiki/Authoring_Guide_"+versionName+"#Qualtric-Surveys");
                    }catch(Exception e){
                        
                        if(logger.isLoggable(Level.INFO)) {
                            logger.log(Level.SEVERE, "Failed to retrieve the GIFT version name from properties, using default wiki page URL for Qualtric Survey help link.", e);
                        }
                        
                        helpAnchor.setHref("https://gifttutoring.org/projects/gift/wiki/Documentation");
                    }
                } else {
                    
                    if(logger.isLoggable(Level.INFO)) {
                        logger.info("User does not have permissions to edit the survey.");
                    }
                    
                    hideLoadingDialog();
                    WarningDialog.alert("Read Only Survey", "You do not have the proper permissions for this survey.  You will not be able to save any changes. ");
                    setReadOnlyMode(true);
                    propertiesPanel.setReadOnlyMode(true);
                    permissionsManager.releaseLocksIgnoreCallback();
                    
                    if(callback != null) {
                        callback.onSuccess(true);
                    }
                }
                
                if(isExistingSurvey){
                    //need special logic to creating a survey from an existing survey and author
                    //selected to use the original survey instead of making a copy
                    handleUseOriginalSurvey();
                }
            }
        };
        
        if(isExistingSurvey){
            permissionsManager.isSurveyEditable(surveyContextId, loadedSurvey, surveyEditableCheckCallback);
        }else{
            //this is a new survey which hasn't been saved, nor does it have a survey id which is needed
            //to check permissions on the server
            surveyEditableCheckCallback.onSuccess(true);
        }
    }
    
    /**
     * Called when a survey has been checked for dependencies and permissions, and has been found to be
     * editable by the user.  This does final checks and notifications of important alerts that the user may need to see.
     * It also sets the mode for the survey editor to be in editable mode.
     * 
     * @param isExistingSurvey True if the survey being loaded is an existing survey, false otherwise.
     */
    private void onEditableSurveyLoadComplete(final boolean isExistingSurvey) {
        
        // This check only needs to happen for existing surveys.
        if (isExistingSurvey) {
            checkQuestionBankSurvey();
        }

        // set to edit mode if the course is not in readonly mode
        if (!GatClientUtility.isReadOnly()) {
            setReadOnlyMode(false);
        }
    }
    
    /** 
     * Sets the readonly mode for the survey editor panel.
     * 
     * @param readOnly True if the survey editor is set to readonly, false if it should be set to be editable.
     */
    private void setReadOnlyMode(boolean readOnly) {
        
        if(logger.isLoggable(Level.FINE)) {
            logger.fine("setReadOnlyMode(" + readOnly + ")");
        }
        
        this.isReadOnly = readOnly;
        
        // Update the survey header to disable the save and display 'readonly'.
        surveyHeader.setReadOnlyMode(readOnly);

        // Update the survey footer to hide the Add Survey Item/Page buttons
        footerWidget.setReadOnlyMode(readOnly);
        
        // Update the scoring logic widget to hide Add Logic Rule button and disabled scoring attributes
        scoringLogicWidgetCuiS.setReadOnlyMode(readOnly);
        
        // set each page as read only
        for (Widget w : surveyPageContainer) {
            if (w instanceof SurveyPageWidget) {
                SurveyPageWidget pageWidget = (SurveyPageWidget) w;
                pageWidget.setReadOnlyMode(readOnly);
            }
        }
    }
    
    /**
     * Accessor to get if the survey editor is in readonly mode.
     * 
     * @return True if the survey editor is in readonly mode, false otherwise.
     */
    public boolean isReadOnly() {
        
        if(logger.isLoggable(Level.FINE)) {
            logger.fine("isReadOnly(): " + this.isReadOnly);
        }
        
        return this.isReadOnly;
    }

    /**
     * Finishes the initialization of the survey editor panel for an existing survey.  This should be called
     * after the survey context, survey images, and survey option sets are retrieved from the server.
     * 
     * @param survey - The survey object data (should not be null).
     */
    private void finishExistingPanelInitialization(final Survey survey) {
        // Reset the panel (clear out everything)
        resetPanel(false);
        
        permissionsManager.initialize();
        
        surveyHeader.initializeWidget(getSurveyType(), survey, isReadOnly);
        footerWidget.initializeWidget(getSurveyType(), isReadOnly);
        
        loadExistingSurvey(survey);
    }
    
    /**
     * Displays a critical message during the loading process of the survey and forces
     * the survey editor to close.
     * 
     * @param errorMessage - The error message to display.  Once the dialog is confirmed, the editor is closed.
     */
    private void displayCriticalLoadError(String errorMessage) {
        hideLoadingDialog();
        
        WarningDialog.error("Failed to load", errorMessage, new ModalDialogCallback() {

            @Override
            public void onClose() {
                if (closeableInterface != null) {
                    closeableInterface.close();
                }
                
            }
            
        });
    }
    
    /**
     * Displays the loading panel (and hides the main panel).
     */
    private void showLoadingPanel() {
        loadingPanel.setVisible(true);
        mainContainer.setVisible(false);
    }
    
    /**
     * Display the loading dialog to the user.
     * @param title the title of the dialog.
     * @param message the message that will be displayed to the user in the dialog.
     */
    private void showLoadingDialog(String title, String message) {
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("showing the loading dialog box with title: " + title);
        }
        
        BsLoadingDialogBox.display(title, message);
    }
    
    /**
     * Hides the loading dialog.
     */
    private void hideLoadingDialog() {
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("hiding loading dialog box.");
        }
        
        BsLoadingDialogBox.remove();
    }

    /**
     * Sets the default survey properties (used for new survey creation).
     * 
     * @param properties - The SurveyProperties object to set the properties for.
     * @param type - The type of survey that is being edited.
     */
    private void defaultSurveyProperties(SurveyProperties properties, SurveyDialogOption type) {
        properties.setCompleteSurveyButtonLabel("");
        properties.setHideSurveyName(false);
        properties.setHideSurveyPageNumbers(false);
        properties.setHideSurveyQuestionNumbers(false);
        properties.setCanGoBackPages(true);
        properties.setNextPageButtonLabel("");
        properties.setSurveyType(SurveyTypeEnum.valueOf(type.toString()));
        properties.setSurveyScorer(new SurveyScorer(new TotalScorer(new ArrayList<AttributeScorerProperties>()), new ArrayList<AttributeScorerProperties>()));
    }

    /**
     * Initialize the Survey Editor panel with an existing survey object.
     * This should be called when the user is loading an existing survey.
     * 
     * @param resources - The global set of survey resources (e.g. survey context, concepts, etc.) that this widget should use
     * @param survey - The survey object data (should not be null).
     * @param transitionName - The default transition name for the survey. 
     * @param surveyContextKey - The surveyContextKey assigned for the transition (if any), this can be null or empty.
     * @param callback - The callback for when the survey context has been selected/saved in the survey editor.
     * @param useOriginal - whether the survey being loaded is the original survey and not a copy of an existing survey or a new survey.
     */
    public void initializePanel(final AbstractSurveyResources resources, final Survey survey, String transitionName, String surveyContextKey,
                                SelectSurveyContextCallback callback, boolean useOriginal) {
        
        if(logger.isLoggable(Level.FINE)) {
            logger.fine("initializePanel(" + resources + ", " + survey + ", " + transitionName + ", " + surveyContextKey
                    + ", " + callback + ", " + useOriginal + ")");
        }
        
        wasConverted = false;
        contextCallback = callback;
        loadedSurvey = survey;
        surveyTransitionName = transitionName;
        surveyContextSurvey = null; //so it will be created for this new survey upon saving
        surveyKey = surveyContextKey;
        usingOriginalSurvey = useOriginal;
        
        // User is loading an existing survey, so display a loading message.
        showLoadingPanel();
        showLoadingDialog("Loading the Survey", "Please wait...loading the survey.");
        
        // Default to writing mode for every survey type.
        setMode(SurveyEditMode.WritingMode);
        
        loadSurveyProperties(survey.getProperties());

        setSurveyResources(resources);
        
        

        // Start by loading the survey context.  
        // The rpc load sequence is as follows:
        //  fetch survey images rpc
        //  fetch option lists (answer sets) rpc
        //  load the existing survey data.
        // Those are called sequentially and once all have succeeded the editor loads.
        // If any fails, then the user is displayed an error message and the editor is closed.
        // Once the survey context is loaded, then load the existing survey object (questions, properties, etc.).
        
        if(surveyResources != null && surveyResources.getSurveyContextId() > 0){
            
            if(logger.isLoggable(Level.INFO)) {
                logger.info("using survey context id of "+surveyContextId);
            }
            
            surveyContextId = surveyResources.getSurveyContextId();
            
        }else{
            hideLoadingDialog();
            WarningDialog.error("Unable to load the survey editor.", "The survey context id has not been set", 
                    new ModalDialogCallback() {

                        @Override
                        public void onClose() {
                            if (closeableInterface != null) {
                                closeableInterface.close();
                            }
                        }
            });
            return;
        }

                    
        // Fetch survey images & option lists.
        updateSurveyImageList(new AsyncCallback<String>() {

            @Override
            public void onFailure(Throwable t) {
                displayCriticalLoadError("Unable to load the survey editor.  There was a critical error fetching the server images: " + t.getMessage());
            }

            @Override
            public void onSuccess(String result) {
                
                
                getOptionLists(new AsyncCallback<String>() {

                    @Override
                    public void onFailure(Throwable t) {
                        
                        displayCriticalLoadError("Unable to load the survey editor.  There was a critical error fetching answer sets from the server: " + t.getMessage());
                    }

                    @Override
                    public void onSuccess(String result) {

                        // finish initializing the panel once the option lists and survey images are retrieved from the server.
                        finishExistingPanelInitialization(survey);
                    }
                });
            }
        });        
        
        setReadOnlyMode(GatClientUtility.isReadOnly());
    }

    /**
     * Initialize the Survey Editor panel with an existing survey object so that the user can copysurvey items from it.
     * This should be called when the user needs to copy items from a survey.
     * <br/><br/>
     * Initializing the Survey Editor panel via this method will essentially make it read-only by default and will
     * prevent it from executing logic that isn't needed while selecting survey items, such as loading survey context
     * or course data.
     * 
     * @param survey - The survey object data (should not be null).
     * @param targetSurveyType - Optional. The type of survey being copied into.
     */
    public void initializePanelForCopy(final Survey survey, final SurveyDialogOption targetSurveyType) {
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("initializePanelForSelection() called with existing survey data.");
        }
        
        //enable selection mode
        isSelectionMode = true;
        copyTargetType = targetSurveyType;
        
        //update the editor header to accommodate selection mode
        surveyHeader.setSelectionModeEnabled(isSelectionMode);
        surveyHeader.setSurveyName(survey.getName());
        
        kasScoringLogicWidget.setReadOnlyMode(true);
        scoringLogicWidgetCuiS.setReadOnlyMode(true);
        
        footerWidget.setVisible(false);
        
        wasConverted = false;
        loadedSurvey = survey;
        
        // User is loading an existing survey, so display a loading message.
        showLoadingPanel();
        showLoadingDialog("Loading the Survey", "Please wait...loading the survey.");
        
        // Default to writing mode for every survey type.
        setMode(SurveyEditMode.WritingMode);
        
        loadSurveyProperties(survey.getProperties());
        
        //make this editor read-only so users can only select survey items
        setReadOnlyMode(true);

        // Fetch survey images & option lists.
        updateSurveyImageList(new AsyncCallback<String>() {

            @Override
            public void onFailure(Throwable t) {
                displayCriticalLoadError("Unable to load the survey editor.  There was a critical error fetching the server images: " + t.getMessage());
            }

            @Override
            public void onSuccess(String result) {
                
                
                getOptionLists(new AsyncCallback<String>() {

                    @Override
                    public void onFailure(Throwable t) {
                        
                        displayCriticalLoadError("Unable to load the survey editor.  There was a critical error fetching answer sets from the server: " + t.getMessage());
                    }

                    @Override
                    public void onSuccess(String result) {

                        // finish initializing the panel once the option lists and survey images are retrieved from the server.
                        finishExistingPanelInitialization(survey);    
                    }
                    
                });
                
            }
            
        });
    }


    /**
     * Loads an existing survey into the survey editor.
     * 
     * @param survey - The survey object from the database.
     */
    private void loadExistingSurvey(final Survey survey) {
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("load existing survey: " + survey.getId() + " " + survey.getName());
        }
        
        // Clear any previous widget ids from the survey elements.
        // Since the widgets are created fresh each time the page is loaded, we do not want to keep
        // any references to old widgets. The elements will be loaded with the new widget ids later
        // on, when the widgets get created.
        for(SurveyPage page : survey.getPages()) {
            for (AbstractSurveyElement element : page.getElements()) {
                element.setWidgetId(null);
            }
        }
        
        // Perform question bank cleanup. A question bank should only contain one survey page.
        if (SurveyDialogOption.ASSESSLEARNER_QUESTIONBANK.equals(surveyType) && survey.getPageCount() != 1) {
            
            consolidatePageList(survey.getPages(), true);

            SurveyPage consolidatedPage = survey.getPages().get(0);

            if (consolidatedPage != null) {
                survey.getPages().clear();
                survey.getPages().add(consolidatedPage);
            }
        }
        
        //Puts the editor panel into the loading state
        isLoading = true;
        
        // Construct the pages for the survey.
        debugPrintSurveyProperties(survey.getProperties());
        
        // Callback for survey panels that have scoring rules/panels, we have to
        // load the panel AFTER the questions have been loaded and the possible
        // total score is calculated.
        final AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {

            @Override
            public void onFailure(Throwable caught) {
                // The failure case is already taken care of in the loadPagesIncremental() method
            }

            @Override
            public void onSuccess(Boolean success) {

                if (success) {
                    if (surveyType.equals(SurveyDialogOption.ASSESSLEARNER_STATIC)) {
                        kasScoringLogicWidget.load(survey);
                    } else if (surveyType.equals(SurveyDialogOption.COLLECTINFO_SCORED)) {
                        scoringLogicWidgetCuiS.load(survey);
                    } else if (surveyType.equals(SurveyDialogOption.ASSESSLEARNER_QUESTIONBANK)) {
                        surveyLoadCompletedSuccessfully(new AsyncCallback<Boolean>() {

                            @Override
                            public void onFailure(Throwable arg0) {
                                // The failure case is already taken care of in the loadPagesIncremental() method
                            }

                            @Override
                            public void onSuccess(Boolean arg0) {
                                displayLoadMoreButton();
                            }}, false);
                    }
                } else {
                    // The failure case is already taken care of in the loadPagesIncremental() method
                }
            }

        };
        
        Timer timer = new Timer() {

            @Override
            public void run() {
                loadPagesIncremental(survey.getPages(), callback, true);
            }
            
        };
        
        // This allows time for the modal dialog to fade in.
        timer.schedule(MODAL_FADEIN_DELAY_MS);
    }
    
    
    /**
     * Handler for when the survey is being saved.
     */
    private void onSaveSurvey() {
        
        
        try {
            
            // Check if survey name is empty 
            String name = surveyHeader.getSurveyName();
            if (name.isEmpty()) {
                WarningDialog.error("Survey Name Needed", "Please enter a survey name before saving.");
                return;
            }
            
            showLoadingDialog("Saving Survey", "Please wait while the survey is being saved.");
            
            
            saveSurveyPagesToDataModel();
            
            // save the survey
            saveSurvey();
        
        } catch (SaveSurveyException e) {
            hideLoadingDialog();
            
            ErrorDetailsDialog dialog = new ErrorDetailsDialog("Failed to save survey to the database.", e.getMessage(), DetailedException.getFullStackTrace(e));
            dialog.setTitle("Survey Database Error");
            dialog.center();
            
            clearCachedSaveEvent();
        }

    }
    
    /**
     * Pushes the changes in all of the currently displayed survey pages to their underlying data models
     * 
     * @throws SaveSurveyException if an exception occurs while saving the changes
     */
    private void saveSurveyPagesToDataModel() throws SaveSurveyException {
        
        if(!surveyType.equals(SurveyDialogOption.ASSESSLEARNER_QUESTIONBANK)) {
            
            // Clear out old page data and rebuild.
            loadedSurvey.getPages().clear();
            
            // Save survey name.
            surveyHeader.saveSurvey(loadedSurvey);
            
            // Save the survey properties.
            saveSurveyProperties(loadedSurvey.getProperties());

            // Save the survey page data along with the survey properties.
            for (int x = 0; x < surveyPageContainer.getWidgetCount(); x++) {
                Widget widget = surveyPageContainer.getWidget(x);
                if (widget instanceof SurveyPageWidget) {
                    SurveyPageWidget pageWidget = (SurveyPageWidget)widget;
                    
                    pageWidget.saveSurvey(loadedSurvey);
                }
            }
        } else {
            saveCurrentlyVisibleQuestionsToDataModel();
        }
    }



    /**
     * Handler for when a survey is being imported
     * 
     * @param content The contents of a qsf export
     */
    private void importSurvey(String content) {
        
        try {
            dndImportModal.hide();

            showLoadingDialog("Importing Survey", "Please wait while your survey is imported...");
            
            ImportQsf action = new ImportQsf(GatClientUtility.getUserName(), content);
            SharedResources.getInstance().getDispatchService().execute(action, new AsyncCallback<QualtricsImportResult>() {

                @Override
                public void onFailure(Throwable caught) {
                    hideLoadingDialog();
                    ErrorDetailsDialog errorDialog = new ErrorDetailsDialog("A server error occurred while importing the survey.", "Failed to import the survey because " + caught.toString(), null);
                    errorDialog.setText("Import Failed");
                    errorDialog.center();
                }

                @Override
                public void onSuccess(final QualtricsImportResult result) {
                    
                    if(result.isSuccess()) {                        
                        boolean isQuestionBank = (surveyType == SurveyDialogOption.ASSESSLEARNER_QUESTIONBANK);

                        if (loadedSurvey.getPages() == null) {
                            loadedSurvey.setPages(new ArrayList<SurveyPage>());
                        }
                        
                        if (result.getSurveyPages() != null && !result.getSurveyPages().isEmpty()) {
                            
                            // initialize to default
                            int maxPageSize = pageSize;
                            if (isQuestionBank) {

                                // need to push the existing changes to the data model before we
                                // import
                                saveCurrentlyVisibleQuestionsToDataModel();

                                // Consolidates the survey pages from the result into a single page.
                                // The consolidated pages will only contain elements that are
                                // supported by the question bank.
                                consolidatePageList(result.getSurveyPages(), isQuestionBank);

                                SurveyPage consolidatedPage = result.getSurveyPages().get(0);

                                if (loadedSurvey.getPageCount() > 0) {
                                    // insert the consolidated results into the beginning of the
                                    // question bank page.
                                    loadedSurvey.getPages().get(0).getElements().addAll(0, consolidatedPage.getElements());
                                } else {
                                    // no existing pages, so add the consolidated page to the
                                    // question bank
                                    loadedSurvey.getPages().add(consolidatedPage);
                                }

                                // set the max page size to be what was already visible plus the
                                // imported results.
                                maxPageSize = selectedPage.getQuestionCount() + consolidatedPage.getElements().size();
                            }
                            
                            // Load the pages depending on if it is a question bank or not:
                            
                            /* 1. QUESTION BANK: We want to load the entire survey (populated above
                             * with the imported survey) because questions banks only have 1 survey
                             * page so that page is cleared out and repopulated with the updated survey. */

                            /* 2. NOT A QUESTION BANK: We want to append the imported survey pages
                             * to our existing survey because it allows for multiple survey pages.
                             * Since we can have many survey pages, we do not need to clear the
                             * existing data before appending the new imported data. */
                            loadPagesIncremental(isQuestionBank ? loadedSurvey.getPages() : result.getSurveyPages(), new AsyncCallback<Boolean>() {

                                @Override
                                public void onFailure(Throwable caught) {
                                    // The failure case is already taken care of in the loadPagesIncremental() method
                                }

                                @Override
                                public void onSuccess(Boolean success) {

                                    if (success) {
                                        if (!result.getFailedItems().isEmpty()) {
                                            StringBuilder sb = new StringBuilder();
                                            for (String item : result.getFailedItems()) {
                                                sb.append("<li>").append(item).append("</li>");
                                            }
                                            WarningDialog.warning("Unsupported Survey Items",
                                                    "<div style=\"max-height: 400px; overflow: auto; padding-top: 20px; margin: -21px -17px -21px -10px;\">"
                                                            + "The following survey items could not be imported:<br/><ul>" + sb.toString()
                                                            + "</ul></div>");
                                        }
                                        displayLoadMoreButton();
                                    } else {
                                        // The failure case is already taken care of in the loadPagesIncremental() method
                                    }
                                }

                            }, false, maxPageSize);

                        } else {
                            // hide the loading dialog because there is nothing to import
                            hideLoadingDialog();
                        }
                    } else {
                        hideLoadingDialog();
                        ErrorDetailsDialog errorDialog = new ErrorDetailsDialog(result.getErrorMsg(), result.getErrorDetails(), result.getErrorStackTrace());
                        errorDialog.setText("Import Failed");
                        errorDialog.center();
                    }
                }
                
            });
        } catch (Exception e) {
            hideLoadingDialog();
            ErrorDetailsDialog errorDialog = new ErrorDetailsDialog("An error occurred while importing the survey.", "Failed to import the survey because " + e.toString(), null);
            errorDialog.setText("Import Failed");
            errorDialog.center();
        }
            
    }
    /**
     * Handler for when the survey is being previewed
     */
    private void onPreviewSurvey() {
        
         try {
        
            saveSurveyPagesToDataModel();
            
            surveyHeader.previewSurvey(loadedSurvey);
            
         } catch (SaveSurveyException e) {
             hideLoadingDialog();
             
             ErrorDetailsDialog dialog = new ErrorDetailsDialog(
                    "Failed to preview survey. A problem occurred while gathering this survey's resources. ", 
                    e.getMessage(), 
                    DetailedException.getFullStackTrace(e)
             );
             dialog.setTitle("Survey Preview Error");
             dialog.center();
             
             clearCachedSaveEvent();
         }

    }
    
    /**
     * Load the survey properties into the editor. 
     * The other properties should be displayed appropriately in the survey property dialog and should be updated
     * properly in the dialog.
     * 
     * @param props - The survey properties that will be loaded.
     */
    private void loadSurveyProperties(SurveyProperties props) {
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("Loading survey properties");
        }
        
        // load the survey type.
        SurveyTypeEnum type = props.getSurveyType();
        if(type == null){
            //this is most likely because the type was not set because it was authored before survey types were created
            setSurveyType(determineSurveyType(props));
            props.setSurveyType(determineSurveyType(getSurveyType()));
            // Signal that the type was added to the survey so that the user can be notified later if needed.
            wasConverted = true;
            
            if(logger.isLoggable(Level.INFO)) {
                logger.info("The survey being loaded doesn't have the survey type property. Based on a check the survey was determined to be " + surveyType.name()+" type.");
            }
            
        }else{

            switch(type){
            
            case COLLECTINFO_SCORED:
                setSurveyType(SurveyDialogOption.COLLECTINFO_SCORED);
                break;
            case COLLECTINFO_NOTSCORED:
                setSurveyType(SurveyDialogOption.COLLECTINFO_NOTSCORED);
                break;
            case ASSESSLEARNER_STATIC:
                setSurveyType(SurveyDialogOption.ASSESSLEARNER_STATIC);
                break;
            case ASSESSLEARNER_QUESTIONBANK:
                setSurveyType(SurveyDialogOption.ASSESSLEARNER_QUESTIONBANK);
                break;
            default:
                setSurveyType(determineSurveyType(props));
                props.setSurveyType(determineSurveyType(getSurveyType()));
                // Signal that the type was added to the survey so that the user can be notified later if needed.
                wasConverted = true;
                
                if(logger.isLoggable(Level.INFO)) {
                    logger.info("The survey being loaded has an unknown survey type.  Based on a check the survey was determined to be " + surveyType.name() +" type.");
                }
            }
        }
        
    }
    
    /**
     * Retrieve the enumerated survey type from the dialog option.
     * 
     * @param surveyDialogOption the current survey type dialog option selected
     * @return the enumerated survey type based on that dialog option
     */
    private SurveyTypeEnum determineSurveyType(SurveyDialogOption surveyDialogOption){
        
        switch(surveyDialogOption){
        
        case ASSESSLEARNER_QUESTIONBANK:
            return SurveyTypeEnum.ASSESSLEARNER_QUESTIONBANK;
        case ASSESSLEARNER_STATIC:
            return SurveyTypeEnum.ASSESSLEARNER_STATIC;
        case COLLECTINFO_NOTSCORED:
            return SurveyTypeEnum.COLLECTINFO_NOTSCORED;
        case COLLECTINFO_SCORED:
            return SurveyTypeEnum.COLLECTINFO_SCORED;
        default:
            return SurveyTypeEnum.COLLECTINFO_NOTSCORED;
        }
    }
    
    
    /**
     * Converts a survey type to a string that can be displayed in the UI.
     * 
     * @param type The type of survey to get the label for.
     * @return The label for the survey based on the type if valid.  If the survey type is not valid and empty string is returned.
     */
    private String convertSurveyTypeToString(SurveyDialogOption type) {
        String typeString = "";
        if(getSurveyType().equals(SurveyDialogOption.COLLECTINFO_NOTSCORED)){
            typeString = "Collect Learer Information (Not Actionable)";
        } else if(getSurveyType().equals(SurveyDialogOption.COLLECTINFO_SCORED)){
            typeString = "Collect Learer Information (Actionable)";
        } else if (getSurveyType().equals(SurveyDialogOption.ASSESSLEARNER_STATIC)){
            typeString = "Assess Learner Knowledge";
        } else if(getSurveyType().equals(SurveyDialogOption.ASSESSLEARNER_QUESTIONBANK)){
            typeString = "Knowledge Assessment Question Bank";
        }
        
        return typeString;
    }
    
    /**
     * Will try to determine what type of survey is being loaded when the property
     * is not explicitly set for the survey (i.e. created using old SAS)
     * 
     * @param props the properties to determine the type with
     * 
     * @return the new survey type
     */
    private SurveyDialogOption determineSurveyType(SurveyProperties props){
        
        if(loadedSurvey.getName().endsWith("- Knowledge Assessment Question Bank")){
            return SurveyDialogOption.ASSESSLEARNER_QUESTIONBANK;
        }
        
        if(props.hasProperty(SurveyPropertyKeyEnum.SCORERS)){
            if(props.getSurveyScorer().getAttributeScorers().size() == 1){
                
                Iterator<AttributeScorerProperties> itr = props.getSurveyScorer().getAttributeScorers().iterator();
                if(itr.next().getAttributeType().equals(LearnerStateAttributeNameEnum.KNOWLEDGE)){
                    return SurveyDialogOption.ASSESSLEARNER_STATIC;
                }
            }
            return SurveyDialogOption.COLLECTINFO_SCORED;
        }
        
        return SurveyDialogOption.COLLECTINFO_NOTSCORED;
    }

    /**
     * Saves the survey properties to the survey properties survey.
     * 
     * @param props - The survey properties object to be updated.
     */
    private void saveSurveyProperties(SurveyProperties props) {
        // Nothing to do here at the moment since the properties are already updated via the editor
        // and when the survey is saved.
    }

    /**
     * Save the survey by export or saving to the database.
     */
    private void saveSurvey() {
        if (GatClientUtility.isGIFTWrapMode()) {
            exportSurvey();
        } else {
            // This call will both save a new survey, or update an existing one to the database.
            // New items (surveys, survey pages, questions) have ids of 0.
            // Existing items will be loaded with their existing database ids so that
            // the existing ids will be updated to the database.
            saveSurveyToDb();
        }
    }

    /**
     * Asynchronous call to the server to save the survey to an export file.
     */
    private void exportSurvey() {

        String surveyContextKey = surveyKey;
        // new survey
        if (StringUtils.isBlank(surveyContextKey)) {
            surveyContextKey = surveyTransitionName;
        }

        surveyContextSurvey = new SurveyContextSurvey(surveyContextId, surveyContextKey, loadedSurvey);

        rpcService.surveyEditorExportSurveyContextSurvey(surveyContextSurvey,
                GatClientUtility.getBaseCourseFolderPath(), GatClientUtility.getUserName(),
                new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable t) {
                        hideLoadingDialog();
                        WarningDialog.error("Failed to update course survey",
                                "Failure occurred when saving the survey context to the database: " + t.getMessage());
                        clearCachedSaveEvent();
                    }

                    @Override
                    public void onSuccess(Void result) {
                        contextCallback.onSurveyContextSelected(surveyContextSurvey.getKey(), surveyContextId,
                                loadedSurvey);
                        hideLoadingDialog();

                        if (cachedSaveEvent != null && cachedSaveEvent.getCloseAfterSave()) {
                            if (logger.isLoggable(Level.INFO)) {
                                logger.info(
                                        "Save was successful, closing survey editor because user requested to close after save.");
                            }
                            SharedResources.getInstance().getEventBus().fireEvent(new SurveyCloseEvent());
                        }

                        clearCachedSaveEvent();
                    }
                });
    }

    /**
     * Asynchronous call to the server to save the survey to the database.
     */
    private void saveSurveyToDb() {
        
        /* Keep track of the last course folder that was used to modify the survey. This will be referenced when copying media files */
        loadedSurvey.getProperties().setPropertyValue(SurveyPropertyKeyEnum.MEDIA_FILE_SOURCE, GatClientUtility.getBaseCourseFolderPath());
        
        rpcService.surveyEditorSaveSurveyAsync(loadedSurvey, surveyContextId, GatClientUtility.getUserName(), new AsyncCallback<Void>() {
 
            @Override
            public void onFailure(Throwable t) {
                
                if(logger.isLoggable(Level.INFO)) {
                    logger.log(Level.SEVERE,"onFailure() occurred when saving the survey: " + t.getMessage(), t); 
                }
                
                hideLoadingDialog();
                WarningDialog.error("Failed to save survey", "An error occurred when saving the survey: " + t.getMessage());
                
                clearCachedSaveEvent();
            }

            @Override
            public void onSuccess(Void result) {

                pollForSaveProgress();
            }  
        });
    }
    
    /**
     * Polls the server for the status of the current save operation until that operation has completed, either successfully or
     * unsuccessfully.
     */
    private void pollForSaveProgress() {
        
        rpcService.getSaveSurveyStatus(GatClientUtility.getUserName(), new AsyncCallback<LoadedProgressIndicator<Survey>>() {
            
            @Override
            public void onSuccess(LoadedProgressIndicator<Survey> result) {
                
                if (result != null) {
                    
                    if(result.isComplete()){
                        
                        if(logger.isLoggable(Level.INFO)) {
                            logger.info("Successfully saved the survey!");
                        }
                        
                        boolean savedNewSurvey = false;
                        if (loadedSurvey != null && loadedSurvey.getId() == 0) {
                            savedNewSurvey = true;
                        }
                        
                        loadedSurvey = result.getPayload();
                        
                        
                        if (loadedSurvey != null) {
                            // The ids that are saved on the server need to be kept in sync with the client ids.
                            // After a save (especially for a new item), the db id could be new, so each survey widget
                            // (pages and question elements) need to have the ids updated.
                            // Updating the database ids directly is faster than rebuilding the UI with the survey object.
                            Iterator<SurveyPage> pageIter = loadedSurvey.getPages().iterator();
                            for (Widget widget : surveyPageContainer) {
                                if (widget instanceof SurveyPageWidget && pageIter.hasNext()) {
                                    SurveyPage surveyPage = pageIter.next();
                                    SurveyPageWidget pageWidget = (SurveyPageWidget)widget;
                                    
                                    pageWidget.updateSurveyDbIds(surveyPage, questionIdToDataModel);
                                    
                                }
                            }
                            
                            
                            // Refresh the survey header properties.
                            surveyHeader.setSurveyProperties(loadedSurvey.getProperties());
                            
                            // Reinitialize any scoring logic headers with the new loadedSurvey object.
                            if (surveyType == SurveyDialogOption.ASSESSLEARNER_STATIC) {
                                kasScoringLogicWidget.initialize(loadedSurvey, false);
                                kasScoringLogicWidget.refresh(false);
                               
                            } else if (surveyType == SurveyDialogOption.COLLECTINFO_SCORED) {
                                scoringLogicWidgetCuiS.initialize(loadedSurvey, false, isReadOnly);
                                scoringLogicWidgetCuiS.load(loadedSurvey);
                                scoringLogicWidgetCuiS.refresh();
                                
                            }
                            
                        }
                        
                        if(logger.isLoggable(Level.INFO)) {
                            logger.info("saved new survey = " + savedNewSurvey);
                        }
                        
                        if (savedNewSurvey) {
                            
                            
                            permissionsManager.acquireSurveyOnlyLock(loadedSurvey, new AsyncCallback<Boolean>() {

                                @Override
                                public void onFailure(Throwable t) {
                                    
                                    if(logger.isLoggable(Level.INFO)) {
                                        logger.log(Level.SEVERE,"Unable to acquire a lock for the newly saved survey: " + t.getMessage(), t);
                                    }
                                    
                                    saveSurveyContextToDb();
                                }

                                @Override
                                public void onSuccess(Boolean result) {
                                    
                                    if (result != null && result) {
                                        
                                        if(logger.isLoggable(Level.INFO)) {
                                            logger.info("Successfully acquired a lock for the newly saved survey with id: " + loadedSurvey.getId());
                                        }
                                        
                                    } else if(logger.isLoggable(Level.SEVERE)) {
                                        logger.severe("Unable to acquire a lock for the newly saved survey with id: " + loadedSurvey.getId());
                                    }
                                    saveSurveyContextToDb();
                                }
                                
                            });
                            
                        } else {
                            saveSurveyContextToDb();
                        }
                        
                    } else if(result.getException() != null){
                        //failed to save
                        
                        clearCachedSaveEvent();
                        
                        if(logger.isLoggable(Level.SEVERE)) {
                            logger.severe("Failed to save the survey.\n"+ result.getException());
                        }
                        
                        hideLoadingDialog();
                        
                        ErrorDetailsDialog dialog = new ErrorDetailsDialog(
                                result.getException().getReason(), 
                                result.getException().getDetails(), 
                                result.getException().getErrorStackTrace()
                        );
                        
                        dialog.setDialogTitle("Save Failed");
                        dialog.center();
                    
                    } else {
                        
                        //schedule another poll for progress 1 second from now
                        Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {
                            
                            @Override
                            public boolean execute() {
                                
                                pollForSaveProgress();
                                
                                return false;
                            }
                            
                        }, 1000);
                        
                    }
                    
                } else {
                    
                    clearCachedSaveEvent();
                    
                    if(logger.isLoggable(Level.SEVERE)) {
                        logger.severe("Survey result is null after saving the survey.");
                    }
                    
                    hideLoadingDialog();
                    
                    WarningDialog.error("Unexpected save response", "Encountered unexpected null for the survey result after saving the survey to the database.");

                }
            }
            
            @Override
            public void onFailure(Throwable caught) {
                
                //failed to save
                
                clearCachedSaveEvent();
                
                if(logger.isLoggable(Level.SEVERE)) {
                    logger.log(Level.SEVERE,"Failed to save the survey.", caught);
                }
                
                hideLoadingDialog();
                
                ErrorDetailsDialog dialog = new ErrorDetailsDialog(
                        "Failed to save the survey due to an unexpected error on the server.", 
                        caught.toString(), 
                        DetailedException.getFullStackTrace(caught)
                );
                
                dialog.setDialogTitle("Save Failed");
                dialog.center();
            }
        });
    }



    /**
     * Used to save the survey context to the database.  This maps a unique context key to the survey context.
     */
    private void saveSurveyContextToDb() {
        
        if(surveyContextSurvey == null){
            //create the survey context survey 
            
            //the survey key was not provided upon init, nor has it been created below in a past save
            if(surveyKey == null){
                
                //build the gift key for the survey context survey
                if (this.getSurveyType() == SurveyDialogOption.ASSESSLEARNER_QUESTIONBANK) {
                    // Only 1 question bank is supported for any survey context.  The context key for the question bank
                    // is the same.
                    surveyKey = GwtSurveySystemProperties.KNOWLEDGE_ASSESSMENT_QBANK_GIFT_KEY;                
                } else {
                    // generate a new survey context key
                    
                    Date date = new Date();
                    DateTimeFormat dtFormat = DateTimeFormat.getFormat("yyyyMMdd_HHmmss");
                    String dateStr = dtFormat.format(date, TimeZone.createTimeZone(0));
                    String keyName = surveyTransitionName + "_" + dateStr;
                    
                    surveyKey = keyName;
                }
            }
            
            surveyContextSurvey = new SurveyContextSurvey(surveyContextId, surveyKey, loadedSurvey);
        }else{
            surveyKey = surveyContextSurvey.getKey();
        }
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("Calling server to Insert/Update survey context survey with "+surveyContextSurvey);
        }

        //insert/update the survey context survey in the survey db
        rpcService.updateSurveyContextSurvey(surveyContextSurvey, new AsyncCallback<Boolean>() {

            @Override
            public void onFailure(Throwable t) {
                
                if(logger.isLoggable(Level.SEVERE)) {
                    logger.log(Level.SEVERE,"Failure occurred when saving the survey context to the database: " + t.getMessage(), t);
                }
                
                hideLoadingDialog();
                WarningDialog.error("Failed to update course survey", "Failure occurred when saving the survey context to the database: " + t.getMessage());
                clearCachedSaveEvent();
            }

            @Override
            public void onSuccess(Boolean result) {
                
                if (result) {
                    if(logger.isLoggable(Level.INFO)){
                        logger.info("UpdateSurveyContext returned success.  GIFT key = "+surveyContextSurvey.getKey()+", surveyContextId = "+surveyContextId);
                    }
                    
                    contextCallback.onSurveyContextSelected(surveyContextSurvey.getKey(), surveyContextId, loadedSurvey);
                    hideLoadingDialog();
                    
                    
                    if (cachedSaveEvent != null && cachedSaveEvent.getCloseAfterSave()) {
                        if(logger.isLoggable(Level.INFO)){
                            logger.info("Save was successful, closing survey editor because user requested to close after save.");
                        }
                        SharedResources.getInstance().getEventBus().fireEvent(new SurveyCloseEvent());
                    } 
                    
                    clearCachedSaveEvent();
                    
                    if(!GatClientUtility.isReadOnly()){
                        GatClientUtility.saveCourseAndNotify(); // #4913 - now saving course in order to not loose recently added course objects that lead to this survey being edited
                    }
                    
                } else {
                    
                    if(logger.isLoggable(Level.SEVERE)) {
                        logger.severe("UpdateSurveyContext was not successful when saving the survey context to the database.");
                    }
                    
                    hideLoadingDialog();
                    WarningDialog.error("Failed to update course survey", "The server returned that the save was not successful.");
                    clearCachedSaveEvent();
                }
                
            }
            
        });        
        
    }
    

    /**
     * Clears the cached survey save event.
     */
    private void clearCachedSaveEvent() {
        cachedSaveEvent = null;
    }

    /**
     * Debug function used to print the properties for a survey object from the database.
     * 
     * @param properties - The properties of the survey database object.
     */
    private void debugPrintSurveyProperties(SurveyProperties properties) {
        if (properties != null && logger.isLoggable(Level.INFO)) {
            
            StringBuilder sb = new StringBuilder();
            sb.append("Survey Properties size = ").append(properties.getPropertyCount());
            
            sb.append("\nkeys = {");
            for (SurveyPropertyKeyEnum key : properties.getKeys()) {
                sb.append(key).append("\n");
            }
            sb.append("}");
            logger.info(sb.toString());
        } 
        
    }

    /** 
     * Sets the mode for the survey editor.  
     * 
     * @param mode - The edit mode for the survey panel.
     */
    public void setMode(SurveyEditMode mode) {
        
        if(logger.isLoggable(Level.FINE)) {
            logger.fine("setMode(" + mode + ")");
        }
        
        onEditorModeChanged(mode);
    }

    /**
     * Sets the global survey resources (i.e. survey context, concepts, etc.) that should be used by this widget for authoring surveys
     * 
     * @param resources the survey resources
     */
    private void setSurveyResources(AbstractSurveyResources resources) {
        
        if(logger.isLoggable(Level.FINE)) {
            logger.fine("setSurveyResources(" + resources + ")");
        }
        
        surveyResources = resources;
    }


    /**
     * Creates a new survey page widget and performs the necessary initialization of the widget.
     * 
     * @return - SurveyPageWidget - The page widget that was created.
     */
    private SurveyPageWidget createNewSurveyPage() {
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("create new survey page");
        }
        
        SurveyPageWidget pageWidget = new SurveyPageWidget(editorMode, surveyType);
        pageWidget.setReadOnlyMode(isSelectionMode || isReadOnly);
        currentPageIndex++;
        
        pageWidget.initialize(surveyPageContainer.getWidgetCount() + 1, currentPageIndex, surveyResources);
        
        surveyPageContainer.add(pageWidget);
        
        return pageWidget;
    }
    
    /**
     * Hides the loading panel and displays the main panel.
     */
    private void hideLoadingPanel() {
        // Load succeeded - Show the main container.
        loadingPanel.setVisible(false);
        mainContainer.setVisible(true);
    }
    
    /**
     * Loads the list of survey pages into the survey editor panel incrementally
     * so that the Java main event loop is not locked up.
     * 
     * @param pageList - The list of SurveyPage objects from the survey
     *            database.
     * @param loadCompleteCallback - The callback to execute when the survey
     *            pages are finished loading. Can be null.
     * @param needsWritePermissionsCheck whether a permissions check should be performed after the survey is loaded to 
     * determine whether the survey panel should be in read only mode or not.  You may not want to perform this
     * check if the load logic is importing questions into an existing survey.
     */
   public void loadPagesIncremental(List<SurveyPage> pageList, final AsyncCallback<Boolean> loadCompleteCallback, boolean needsWritePermissionsCheck) {
       loadPagesIncremental(pageList, loadCompleteCallback, needsWritePermissionsCheck, pageSize);
    }
    
   /**
    * Loads the list of survey pages into the survey editor panel incrementally
    * so that the Java main event loop is not locked up.
    * 
    * @param pageList - The list of SurveyPage objects from the survey database.
    * @param loadCompleteCallback - The callback to execute when the survey pages are finished loading. Can be null.
    * @param needsWritePermissionsCheck whether a permissions check should be performed after the survey is loaded to 
    * determine whether the survey panel should be in read only mode or not.  You may not want to perform this
    * check if the load logic is importing questions into an existing survey.
    * @param maxPageSize the maximum number of question allowed on a single page.
    */
    public void loadPagesIncremental(List<SurveyPage> pageList, final AsyncCallback<Boolean> loadCompleteCallback,
            final boolean needsWritePermissionsCheck, final int maxPageSize) {
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("loadPagesIncremental. pageList.size: " + pageList.size());
        }
        
        pageIter = pageList.iterator();

        // Reset the load counters.
        final int numPages = pageList.size();
        editorModeProgressModal.setPages(numPages);
        currentElementLoadIndex = 0;
        currentPageLoadIndex = 0;
        totalElementLoadCount = 0;
        
        indexOfLastQuestionLoaded = 0;
        
        RepeatingCommand rc = null;
            
        if (surveyType == SurveyDialogOption.ASSESSLEARNER_QUESTIONBANK) {
            // Set the page count to 1 since we only have one page for question banks
            editorModeProgressModal.setPages(1);

            // we are going to be rebuilding the question bank widgets, clear the container and
            // reset the loaded questions counter
            surveyPageContainer.clear();
            totalQuestionsLoaded = 0;
            
            SurveyPageWidget pageWidget = createNewSurveyPage();
            selectSurveyPage(pageWidget.getWidgetId());
            
            countFilteredQuestions();

            rc = createPageBuilderCommand(
            pageList.get(0), 
            pageWidget, 
            0, 
            maxPageSize, 
            loadCompleteCallback);
        } else {
            rc = createPageBuilderCommandForAllQuestions(numPages, loadCompleteCallback, needsWritePermissionsCheck);
        }
        
        Scheduler.get().scheduleFixedDelay(rc, LOAD_SURVEY_SCHEDULER_DELAY_MS);
    }
   
    /**
     * Consolidates the elements from the list of survey pages into the first survey page.
     * 
     * @param pageList the list of survey pages to consolidate. If this is null or empty, nothing
     *            will be updated.
     * @param isQuestionBank true if the survey type is of QuestionBank. If true, the page elements
     *            that are not supported by the question bank will be removed.
     */
   private void consolidatePageList(List<SurveyPage> pageList, boolean isQuestionBank) {
        if (pageList == null || pageList.isEmpty()) {
            return;
        }

        Iterator<SurveyPage> pageItr = pageList.iterator();
        
        // if question bank, process first page separately (remove elements that aren't supported).
        // if not question bank, we don't need to process the first page.
        SurveyPage firstPage = pageItr.next();
        if (isQuestionBank) {
            Iterator<AbstractSurveyElement> elementItr = firstPage.getElements().iterator();
            while (elementItr.hasNext()) {
                SurveyItemType surveyType = SurveyWidgetFactory.getQuestionWidgetType(elementItr.next());
                if (!SurveyWidgetFactory.isSupportedByQuestionBank(surveyType)) {
                    elementItr.remove();
                }
            }
        }

        // process the rest of the pages after the first
        while (pageItr.hasNext()) {
            Iterator<AbstractSurveyElement> elementItr = pageItr.next().getElements().iterator();
            while (elementItr.hasNext()) {
                AbstractSurveyElement element = elementItr.next();

                // if question bank, we need to filter out invalid elements
                if (!isQuestionBank || SurveyWidgetFactory.isSupportedByQuestionBank(SurveyWidgetFactory.getQuestionWidgetType(element))) {
                    // add element to the first page and set it's page id to the first page's id
                    element.setSurveyPageId(firstPage.getId());
                    firstPage.getElements().add(element);
                }
            }

            // now that we have copied all relevant elements to the first page, remove this page.
            pageItr.remove();
        }
    }
    
    /**
     * Saves the questions loaded in the survey page to the data model
     */
    private void saveCurrentlyVisibleQuestionsToDataModel() {
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("Saving currently visible questions to data model.");
        }
        
        try {
            if (loadedSurvey == null) {
                throw new UnsupportedOperationException("Must have a loaded survey in order to save survey questions.");
            }
            
            //Save the current UI state to data models
            Survey tempSurvey = new Survey();
            
            //Finds the first (and only) surveyPageWidget
            SurveyPageWidget surveyPageWidget = null;
            for(Widget w : surveyPageContainer) {
                if(w instanceof SurveyPageWidget) {
                    surveyPageWidget = (SurveyPageWidget) w;
                    break;
                }
            }
            
            //Guard statement to ensure a surveyPage was found
            if(surveyPageWidget == null) {
                
                if(logger.isLoggable(Level.INFO)) {
                    logger.warning("The surveyPageContainer does not contain a SurveyPageWidget whose questions to save");
                }
                
                return;
            }
            
            selectedPage = surveyPageWidget;
            
            if (loadedSurvey.getPages().size() == 0) {
                SurveyPage surveyPage = new SurveyPage();
                surveyPage.setId(0);
                surveyPage.setName("");
                loadedSurvey.getPages().add(surveyPage);
            }
            
            SurveyPage pageBeingEdited = loadedSurvey.getPages().get(0);
            
            //Gets the elements from the survey
            selectedPage.saveSurvey(tempSurvey);
            List<AbstractSurveyElement> newElements = tempSurvey.getPages().get(0).getElements();
            
            //Replaces the old question data models with the newly created ones
            for(int i = 0; i < selectedPage.getQuestionWidgets().size(); i++) {
                
                //Get references to the question widget and the appropriate data models
                QuestionContainerWidget question = selectedPage.getQuestionWidgets().get(i);
                AbstractSurveyElement oldElement = questionIdToDataModel.get(question.getWidgetId());
                AbstractSurveyElement newElement = newElements.get(i);
                
                //If there is no entry in the map, the element is new and should be inserted at the end of the list
                if(oldElement != null) {
                    
                    int indexOfExistingElement = -1;
                    // Cannot use list.indexOf() here because the element's .equals won't match.
                    // Manually search through the list and compare element IDs.
                    for (int elemIndex = 0; elemIndex < pageBeingEdited.getElements().size(); elemIndex++) {
                        if (pageBeingEdited.getElements().get(elemIndex).getWidgetId() != null
                                && pageBeingEdited.getElements().get(elemIndex).getWidgetId().equals(oldElement.getWidgetId())) {
                            indexOfExistingElement = elemIndex;
                            break;
                        }
                    }
                    
                    if (indexOfExistingElement != -1) {
                        pageBeingEdited.getElements().remove(indexOfExistingElement);
                        pageBeingEdited.getElements().add(indexOfExistingElement, newElement);
                    } else {
                        pageBeingEdited.getElements().add(newElement);
                    }
                } else {
                    pageBeingEdited.getElements().add(newElement);
                }
                                
                //Update the map entry
                questionIdToDataModel.put(question.getWidgetId(), newElement);
            }
            
        } catch(Throwable surveyEx) {
            
            if(logger.isLoggable(Level.SEVERE)) {
                logger.log(Level.SEVERE,surveyEx.toString(), surveyEx);
            }
        }
    }
    
    /**
     * Creates the repeating command that builds a page by loading each question from the surveyPage to it
     * 
     * @param surveyPage - The survey page containing the questions
     * @param pageWidget - The page widget to load the questions to
     * @param startIndex - The current index of the next question to load into the survey editor
     * @param questionCount - The max number of questions to load at a time
     * @param loadCompleteCallback - The callback to run when the last question has been loaded. Can be null.
     * @return the repeating command that builds the page
     */
    private RepeatingCommand createPageBuilderCommand(final SurveyPage surveyPage, final SurveyPageWidget pageWidget, final int startIndex, final int questionCount, final AsyncCallback<Boolean> loadCompleteCallback) {
       
        if(logger.isLoggable(Level.INFO)) {
            logger.info("createPageBuilderCommand()");
        }

        if(surveyPage == null) {
            throw new IllegalArgumentException("The value for surveyPage cannot be null");
        }
        
        if(pageWidget == null) {
            throw new IllegalArgumentException("The value for pageWidget cannot be null");
        }
        
        final QuestionFilter filter = filterWidget.getFilter();
        BsLoadingDialogBox.display("Loading Elements", "");
        return new RepeatingCommand() {
            
            /** The current index of the next question to load into the survey editor */
            private int currentQuestionIndex = startIndex;
            
            /** The number of questions that have been loaded into the survey editor */
            private int questionsLoaded = 0;
            
            /** The number of questions left to load; defaults to the question count limit */
            int questionsToLoad = questionCount;
            
            @Override
            public boolean execute() {
                
                if(logger.isLoggable(Level.INFO)) {
                    logger.info("createPageBuilderCommand.execute()");
                }
                
                try {
                    
                    boolean shouldExecuteAgain = false;

                    if (filterWidget.isFilterActive()) { // If the filter is active...
                        // Check if there are less than 10 questions left to load
                        if (questionsLoaded % questionCount == 0 && filteredQuestions - totalQuestionsLoaded < questionCount) {
                            questionsToLoad = filteredQuestions - totalQuestionsLoaded;
                        }
                        //Check to make sure the index is in bounds and that the desired page size has not already been loaded
                        shouldExecuteAgain = questionsLoaded < surveyPage.getElements().size()
                                && questionsLoaded < questionsToLoad;
                    } else if (currentQuestionIndex % questionCount == 0 && surveyPage.getElements().size() - currentQuestionIndex < questionCount) {
                        // If there are fewer than questionCount questions to load...
                        questionsToLoad = surveyPage.getElements().size() - currentQuestionIndex;
                        // Check to make sure the index is in bounds and that the desired page size has not already been loaded
                        shouldExecuteAgain = currentQuestionIndex < surveyPage.getElements().size()
                                && questionsLoaded < questionsToLoad;
                    } else { // Otherwise we need to load questionCount more questions
                        // Check to make sure the index is in bounds and that the desired page size has not already been loaded
                        shouldExecuteAgain = currentQuestionIndex < surveyPage.getElements().size()
                                && questionsLoaded < questionsToLoad;
                    }
                    
                    if(!shouldExecuteAgain) {
                        hideLoadingDialog();
                        if(loadCompleteCallback != null) {
                            loadCompleteCallback.onSuccess(true);
                        }
                        return false;
                    }
                    
                    //Specify the element that is being loaded
                    String dialogMessage = "Please wait...loading Element " + (questionsLoaded + 1) + " of " 
                            + questionsToLoad
                            + "<br/><br/>Refresh the browser if you wish to cancel.";
                    BsLoadingDialogBox.updateMessage(dialogMessage);
                    
                    //Determines whether to move to the next question or the next page
                    AbstractSurveyElement element = surveyPage.getElements().get(currentQuestionIndex++);
                    if(filter == null || filter.matches(element)) {
                        loadSurveyElementForPage(surveyPage, pageWidget, element);
                        questionsLoaded++;
                        totalQuestionsLoaded++;
                    }

                    indexOfLastQuestionLoaded = currentQuestionIndex - 1;
                    return true;
                } catch(LoadSurveyException loadSurveyEx) {
                    if(loadCompleteCallback != null) {
                        loadCompleteCallback.onFailure(loadSurveyEx);
                    }
                    return false;
                }
            }
        };
    }
    
    /**
     * Creates the repeating command that builds a page by loading each question from the surveyPage
     * to it. This method is called for all non question bank survey types.
     * 
     * @param numPages the total number of survey pages to load
     * @param loadCompleteCallback the callback to run when the last question has been loaded. Can
     *            be null.
     * @param needsWritePermissionsCheck check whether a permissions check should be performed after
     *            the survey is loaded to determine whether the survey panel should be in read only
     *            mode or not. You may not want to perform this check if the load logic is importing
     *            questions into an existing survey.
     * @return the repeating command that builds the page
     */
    public RepeatingCommand createPageBuilderCommandForAllQuestions(final int numPages, 
            final AsyncCallback<Boolean> loadCompleteCallback, final boolean needsWritePermissionsCheck) {
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("createPageBuilderCommandForAllQuestions()");
        }

        return new RepeatingCommand() {

            @Override
            public boolean execute() {
                
                if(logger.isLoggable(Level.INFO)) {
                    logger.info("RepeatingCommand execute() called.");
                }
                
                boolean shouldExecuteAgain = false;
                try {
                    
                    boolean loadedElementForPage = false;
                    // Check to continue loading elements in a page first.
                    if (elementIter != null && elementIter.hasNext()) {
                        AbstractSurveyElement element = elementIter.next();
                        currentElementLoadIndex++;
                        loadSurveyElementForPage(currentPageLoad, currentPageWidgetLoad, element);
                        
                        
                        // If there is only one page, don't display the page count.
                        StringBuilder sb = new StringBuilder();
                        sb.append("Please wait...loading");
                        if (numPages > 1) {
                            sb.append(" Page ").append(currentPageLoadIndex).append(" of ").append(numPages).append(" and");
                        } 
                        
                        sb.append(" Element ").append(currentElementLoadIndex).append(" of ").append(totalElementLoadCount).append(".");
                        sb.append("<br><br>Refresh the browser page if you wish to cancel loading.");
                        
                        
                        BsLoadingDialogBox.updateMessage(sb.toString());
                        loadedElementForPage = true;
                        shouldExecuteAgain = true;
                    }else if(currentElementLoadIndex == 0){
                        //a survey page with no elements (e.g. NASA-TLX 2 survey page 2)
                        loadSurveyElementForPage(currentPageLoad, currentPageWidgetLoad, null);
                    }
                    
                    
                    if (!loadedElementForPage && pageIter.hasNext()) {
                        
                        currentPageLoad = pageIter.next();
                        
                        if (currentPageLoad.getElements() != null) {
                            totalElementLoadCount = currentPageLoad.getElements().size();
                        } else {
                            totalElementLoadCount = 0;
                        }
                        
                        currentElementLoadIndex = 0;
                        currentPageLoadIndex++;
                        currentPageWidgetLoad = createNewSurveyPage();

                        deSelectPage(currentPageWidgetLoad);

                        elementIter = currentPageLoad.getElements().iterator();
                        
                        // If there is only one page, don't display the page count.
                        StringBuilder sb = new StringBuilder();
                        sb.append("Please wait...loading");
                        if (numPages > 1) {
                            sb.append(" Page ").append(currentPageLoadIndex).append(" of ").append(numPages).append(".");
                        } else {
                            sb.append(" the survey.");
                            
                        }

                        sb.append("<br><br>Refresh the browser page if you wish to cancel loading.");
                        
                        BsLoadingDialogBox.updateMessage(sb.toString());
                        
                        selectSurveyPage(currentPageWidgetLoad.getWidgetId());
                        shouldExecuteAgain = true;
                    } else if (!loadedElementForPage) {
                        
                        if(logger.isLoggable(Level.INFO)) {
                            logger.info("loadPagesIncremental() successfully completed.");
                        }
                        
                        surveyLoadCompletedSuccessfully(loadCompleteCallback, needsWritePermissionsCheck);
                    }
                } catch (LoadSurveyException e) {
                    
                    if(logger.isLoggable(Level.SEVERE)) {
                        logger.log(Level.SEVERE,"There was an error loading the survey with reason: " + e.getMessage(), e);
                    }
                    
                    hideLoadingDialog();
                    
                    WarningDialog.error("Failed to load survey", "There was an error on the client when loading the survey:\n" + e.getMessage(), new ModalDialogCallback() {

                        @Override
                        public void onClose() {
                            if (closeableInterface != null) {
                                closeableInterface.close();
                            }
                            if(loadCompleteCallback != null) {
                                loadCompleteCallback.onSuccess(false);
                            }
                        }
                        
                    });
                } catch (Throwable t){
                    
                    if(logger.isLoggable(Level.SEVERE)) {
                        logger.log(Level.SEVERE,"There was an error loading the survey with reason: " + t.toString(), t);
                    }
                    
                    hideLoadingDialog();
                    
                    WarningDialog.error("Failed to load survey", "There was an error on the client when loading the survey:\n" + t.toString(), new ModalDialogCallback() {

                        @Override
                        public void onClose() {
                            if (closeableInterface != null) {
                                closeableInterface.close();
                            }
                            if(loadCompleteCallback != null) {
                                loadCompleteCallback.onSuccess(false);
                            }
                        }
                        
                    });
                }

                return shouldExecuteAgain;
            }
        };
    }
    
    /**
     * Load handler to load the survey page widget with the page data from the survey database.
     * 
     * @param page the SurveyPage database data that will be loaded into the SurveyPageWidget UI.
     * @param pageWidget the page widget that the element will be added to.
     * @param element the survey element that will be loaded into the page widget.  Can be null if the survey page
     * contains no elements.
     * @throws LoadSurveyException will be thrown if the survey element fails to load.
     */
    public void loadSurveyElementForPage(SurveyPage page, SurveyPageWidget pageWidget, AbstractSurveyElement element) throws LoadSurveyException {

        SurveyEditMode editMode = editorMode;
        if (page != null) {
            
            try{
                logger.info("load survey element for page '"+page.getName()+"'.");
                pageWidget.setPageName(page.getName());
                
                pageWidget.setPageDbId(page.getId());
                
                // DEBUG PRINT THE properties
                pageWidget.debugPrintPageProperties(page.getProperties());
                
                if(logger.isLoggable(Level.INFO)) {
                    logger.info("Page number of elements: " + page.getElements().size());
                }
    
                if(element != null){
                    boolean isScored = !getSurveyType().equals(SurveyDialogOption.COLLECTINFO_NOTSCORED);
                    boolean isQuestionBank = getSurveyType().equals(SurveyDialogOption.ASSESSLEARNER_QUESTIONBANK);
                    boolean readOnly = isSelectionMode || isReadOnly;
    
                    SurveyElementTypeEnum type = element.getSurveyElementType();
                    if (type == SurveyElementTypeEnum.QUESTION_ELEMENT) {
                        
                        Widget widget = null;
                        
                        // Dig deeper in terms of the type of survey.
                        @SuppressWarnings("unchecked")
                        AbstractSurveyQuestion<? extends AbstractQuestion> surveyQuestion = (AbstractSurveyQuestion<? extends AbstractQuestion>)element;
        
                            if(logger.isLoggable(Level.INFO)) {
                                logger.info("surveyQuestion = " + surveyQuestion.getClass().getName());
                            }
                            
                            if (surveyQuestion instanceof FillInTheBlankSurveyQuestion) {
                                
                                if(surveyQuestion.getQuestion().getProperties().getBooleanPropertyValue(SurveyPropertyKeyEnum.IS_ANSWER_FIELD_TEXT_BOX_KEY) != null && 
                                        !surveyQuestion.getQuestion().getProperties().getBooleanPropertyValue(SurveyPropertyKeyEnum.IS_ANSWER_FIELD_TEXT_BOX_KEY)){
                                    
                                    if(logger.isLoggable(Level.INFO)) {
                                        logger.info("Creating an essay widget");
                                    }
                                    
                                    widget = SurveyWidgetFactory.createSurveyWidget(SurveyItemType.ESSAY, editMode, isScored, readOnly, isQuestionBank);
                                    
                                } else {
                                    
                                    if(logger.isLoggable(Level.INFO)) {
                                        logger.info("Creating a free response widget");
                                    }
                                    
                                    surveyQuestion.getQuestion().getProperties().setBooleanPropertyValue(SurveyPropertyKeyEnum.IS_ANSWER_FIELD_TEXT_BOX_KEY, true);
                                    widget = SurveyWidgetFactory.createSurveyWidget(SurveyItemType.FREE_RESPONSE, editMode, isScored, readOnly, isQuestionBank);
                                }
            
                            } else if (surveyQuestion instanceof MultipleChoiceSurveyQuestion) {
            
                                
                                // MultipleChoiceSurveyQuestion could be a TRUE/FALSE or normal Multiple Choice widget.
                                OptionList optionList = null;
                                if(surveyQuestion.getQuestion().getProperties().getPropertyValue(SurveyPropertyKeyEnum.REPLY_OPTION_SET_KEY) instanceof OptionList){
                                    optionList = (OptionList) surveyQuestion.getQuestion().getProperties().getPropertyValue(SurveyPropertyKeyEnum.REPLY_OPTION_SET_KEY);
                                }
                                if(optionList != null && optionList.getName().equals("True/False")){
                                    widget = SurveyWidgetFactory.createSurveyWidget(SurveyItemType.TRUE_FALSE, editMode, isScored, readOnly, isQuestionBank);
                                } else {
                                    widget = SurveyWidgetFactory.createSurveyWidget(SurveyItemType.MULTIPLE_CHOICE, editMode, isScored, readOnly, isQuestionBank);
                                }
            
                            } else if (surveyQuestion instanceof RatingScaleSurveyQuestion) {
            
                                widget = SurveyWidgetFactory.createSurveyWidget(SurveyItemType.RATING_SCALE, editMode, isScored, readOnly, isQuestionBank);
            
                            } else if (surveyQuestion instanceof MatrixOfChoicesSurveyQuestion) {
            
                                widget = SurveyWidgetFactory.createSurveyWidget(SurveyItemType.MATRIX_OF_CHOICES, editMode, isScored, readOnly, isQuestionBank);
            
                            } else if (surveyQuestion instanceof SliderSurveyQuestion) {
        
                                widget = SurveyWidgetFactory.createSurveyWidget(SurveyItemType.SLIDER_BAR, editMode, isScored, readOnly, isQuestionBank);
                            
                            } else if(logger.isLoggable(Level.SEVERE)) {
                                logger.severe("Unsupported survey type encountered: " + surveyQuestion.getClass().getName());
                            }
                        
                        
                        // Load the widget with the properties from the survey question data.
                        if (widget != null && widget instanceof AbstractQuestionWidget) {
                            AbstractQuestionWidget absWidget = (AbstractQuestionWidget) widget;
                            absWidget.setReadOnlyMode(isReadOnly);
                            
                            if (surveyQuestion.getQuestion() != null) {
    
                                replaceOutdatedSharedOptionLists(surveyQuestion.getQuestion());
    
                                absWidget.setAbstractQuestionDbId(surveyQuestion.getQuestion().getQuestionId());
                                
                                // Load any previously existing categories for this question.
                                // This is not exposed to the user yet, so this is only for legacy questions from the SAS.
                                absWidget.loadQuestionCategories(surveyQuestion.getQuestion().getCategories());
                            }
                            
                            absWidget.setAbstractSurveyQuestionDbId(surveyQuestion.getId());
                            absWidget.load(surveyQuestion);
                            SurveyWidgetId qId = pageWidget.addWidgetToSurveyPage(absWidget, false, false);
                            surveyQuestion.setWidgetId(qId.getWidgetId());
                            questionIdToDataModel.put(qId, surveyQuestion);
                        }
                    
                    
                    } else if (type == SurveyElementTypeEnum.TEXT_ELEMENT) {
                        
                        TextSurveyElement textElement = (TextSurveyElement)element;
                        
                        Widget widget = null;
                        widget = SurveyWidgetFactory.createSurveyWidget(SurveyItemType.INFORMATIVE_TEXT, editMode, isScored, readOnly, isQuestionBank);
                        // Load the widget with the properties from the survey question data.
                        if (widget != null && widget instanceof AbstractQuestionWidget) {
                            AbstractQuestionWidget absWidget = (AbstractQuestionWidget) widget;
                            
                            absWidget.setAbstractSurveyQuestionDbId(textElement.getId());
                            absWidget.load(textElement);
                            SurveyWidgetId qId = pageWidget.addWidgetToSurveyPage(widget, false, false);
                            textElement.setWidgetId(qId.getWidgetId());
                            questionIdToDataModel.put(qId, textElement);
                        }
                    }
                }
                
                deSelectPage(pageWidget);
            }catch(Throwable t){
                throw new LoadSurveyException("Failed to load survey element (id: "+ (element != null ? element.getId() : "null") +") on survey page named '"+page.getName()+"' (id: "+page.getId()+") because "+t.toString(), t);
            }
        } else if(logger.isLoggable(Level.SEVERE)) {
            logger.severe("Encountered a null page when trying to load the survey page.");
        }
    }
    
    /**
     * This method checks if the given survey question contains any shared option lists. If it does,
     * then it compares the shared option list with our set of 'core' shared lists. If any of the
     * question's shared option lists have the same name as a core shared list, but different ids,
     * then that means it is pointing to the incorrect database entry. This method will update the
     * question to point to the correct core shared list. This can happen if we import a question
     * from a very old survey that is referencing an outdated database entry.
     * 
     * @param surveyQuestion the survey question to update.
     */
    private void replaceOutdatedSharedOptionLists(AbstractQuestion surveyQuestion) {

        // exit method if the question is null or the shared answer set is null.
        if (surveyQuestion == null || propertiesPanel == null || propertiesPanel.sharedAnswerSets == null) {
            return;
        }

        SurveyItemProperties properties = surveyQuestion.getProperties();

        for (SurveyPropertyKeyEnum key : properties.getKeys()) {
            Serializable propValue = properties.getPropertyValue(key);

            // find all the question's option lists (from it's properties) and see if it is a shared option list.
            if (propValue instanceof OptionList && ((OptionList) propValue).getIsShared()) {

                OptionList optList = (OptionList) propValue;
                for (OptionList sharedList : propertiesPanel.sharedAnswerSets) {
                    // if the option list has the same name and id, no action is performed.
                    // if the option list has the same name but different id, then set the question's properties to reference the 'official' shared option list.
                    if (StringUtils.equals(optList.getName(), sharedList.getName())) {
                        if (optList.getId() != sharedList.getId()) {
                            surveyQuestion.getProperties().setPropertyValue(key, sharedList);
                        }
                        break;
                    }
                }
            }
        }
    }
    
    /**
     * Called when the survey load process has successfully completed.
     * 
     * @param loadCompleteCallback the callback to execute after the survey locks are acquired
     * @param needsWritePermissionsCheck whether a permissions check should be performed after the survey is loaded to 
     * determine whether the survey panel should be in read only mode or not.  You may not want to perform this
     * check if the load logic is importing questions into an existing survey.
     */
    private void surveyLoadCompletedSuccessfully(final AsyncCallback<Boolean> loadCompleteCallback, 
            final boolean needsWritePermissionsCheck) {
        // Signal the event to calculate the scores of the survey.
        SharedResources.getInstance().getEventBus().fireEvent(new SurveyScoreValueChangedEvent());
        
        // Widgets are done loading
        isLoading = false;

        // Load succeeded - Show the main container.
        hideLoadingPanel();
        hideLoadingDialog();
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("Survey load completed successfully: isReadOnly = "+isReadOnly+", needsWritePermissionsCheck = "+needsWritePermissionsCheck);
        }
        
        if(!isReadOnly && needsWritePermissionsCheck){
            //don't check for survey write ability if the survey is deemed read only already
            
            // Ensure that the user can acquire the survey locks.
            showLoadingDialog("Acquiring Survey Locks", "Please wait...acquiring locks for the survey.");
            acquireSurveyLocks(new AsyncCallback<Boolean>() {

                @Override
                public void onFailure(Throwable arg0) {

                    try{
                        hideLoadingDialog();
                        WarningDialog.error("Failed to acquire survey lock", "Server failure occurred while attempting to aquire a lock on the survey.  You will not be able to edit the survey.");
                        setReadOnlyMode(true);
                       
                        if(loadCompleteCallback != null) {
                            // The lock for this survey has already been acquired
                            loadCompleteCallback.onSuccess(true);
                        }
                    }catch(Exception e){
                        
                        if(logger.isLoggable(Level.INFO)) {
                            logger.log(Level.INFO, "Failed to set read only on survey editor after failing to acquire survey lock.", arg0);
                        }
                        
                        if(loadCompleteCallback != null) {
                            // The lock for this survey has already been acquired
                            loadCompleteCallback.onFailure(new Exception("Failed to set read only on survey editor.", e));
                        }
                    }
                }

                @Override
                public void onSuccess(Boolean result) {
                    
                    if (result) {
                        hideLoadingDialog();

                        // don't need to check dependencies in gift wrap mode
                        if (GatClientUtility.isGIFTWrapMode()) {
                            return;
                        }

                        // Locks have been acquired, so continue checking permissions.
                        if(logger.isLoggable(Level.INFO)) {
                            logger.info("Checking permissions for existing survey.");
                        }
                        
                        showLoadingDialog(CHECK_DEPENDENCIES_TITLE, CHECK_DEPENDENCIES_MESSAGE);
                        checkSurveyPermissions(true, loadCompleteCallback);                        
                    } else {
                        // nothing to do here.  Readonly method will be set internally in the acquire lock method.
                        
                        try{
                            hideLoadingDialog();
                            WarningDialog.alert("Unable to obtain a lock to the survey", 
                                    "Unfortunately the lock for this survey couldn't be acquired.  The most likely reason is that somebody else currently possesses the lock or there was an error on the server.<br/>"+
                                    "Please try to edit this survey again later.<br/><br/>You will not be able to edit the survey.");
                            setReadOnlyMode(true);
                        }catch(Exception e){
                            
                            if(logger.isLoggable(Level.SEVERE)) {
                                logger.log(Level.SEVERE, "Failed to set read only on survey editor after determining that you can't obtain a lock to the survey.");
                            }
                            
                            if(loadCompleteCallback != null) {
                                // The lock for this survey has already been acquired
                                loadCompleteCallback.onFailure(new Exception("Failed to set read only on survey editor.", e));
                            }
                        }
                        
                    }
                    
                }
                
            });
        
            if(isSelectionMode 
                    && copyTargetType != null 
                    && copyTargetType.equals(SurveyDialogOption.ASSESSLEARNER_QUESTIONBANK)){
                disableInappropriateQBankQuestions();
            }
        }else{
                        
            if(isSelectionMode 
                    && copyTargetType != null 
                    && copyTargetType.equals(SurveyDialogOption.ASSESSLEARNER_QUESTIONBANK)){
                disableInappropriateQBankQuestions();
            }
        
            if(loadCompleteCallback != null) {
                // The lock for this survey has already been acquired
                loadCompleteCallback.onSuccess(true);
            } 
        }
        
    }
    
    /**
     * Disable any questions that are not able to be in a question bank survey type.
     * This is used to prevent copying questions from other survey types that aren't supported
     * in question banks (e.g. free response question)
     */
    private void disableInappropriateQBankQuestions(){        
        
        //prevent certain question types from being selected when copying items to a question bank survey
        for (Widget widget : surveyPageContainer) {
            if (widget instanceof SurveyPageWidget) {
                 
                 SurveyPageWidget pageWidget = (SurveyPageWidget)widget;
                
                 for(QuestionContainerWidget question : pageWidget.getQuestionWidgets()){
                     
                     Widget questionWidget = question.getQuestionWidget();
                     
                    boolean supportedByQuestionBank = false;
                    if (questionWidget instanceof AbstractQuestionWidget) {
                        SurveyItemType surveyType = SurveyWidgetFactory.getQuestionWidgetType((AbstractQuestionWidget) questionWidget);
                        supportedByQuestionBank = SurveyWidgetFactory.isSupportedByQuestionBank(surveyType);
                    }

                    if (!supportedByQuestionBank) {
                        // can't copy this question because it's not a supported question type for
                        // question bank survey type

                        question.setEnabled(false);
                        question.setTitle("This survey item cannot be copied because it is invalid for Question Bank surveys.");
                    }
                 }
             }
        }
    }
    
    /**
     * Check if the author selected to 'use original' survey for the survey being loaded and that survey is read only.
     * If it is than the user won't be initiate a save survey to course because its read only, so it has to be done for them.   
     */
    private void handleUseOriginalSurvey(){
        
        if(isReadOnly() && this.usingOriginalSurvey){
            saveSurveyContextToDb();
        }
    }
    
    
    /**
     * Performs a check (only for a question bank survey) to determine if the question bank
     * is shared across other course objects in the course.  If it is shared, the user is presented with
     * a dialog to inform them that the survey can be edited, but will affect the other places in the course
     * where the survey is used.
     */
    private void checkQuestionBankSurvey() {

        if (this.getSurveyType() == SurveyDialogOption.ASSESSLEARNER_QUESTIONBANK 
                && surveyResources != null) {
            
            boolean hasSharedQuestionBank = surveyResources.hasSharedQuestionBank();
            if (hasSharedQuestionBank) {
                WarningDialog.warning("Shared Question Bank", "This Question Bank survey is shared with other course objects.  <b><i>Any changes that are saved " + 
                                      "to this survey will affect those course objects where the Question Bank is used.</b></i>");
            }
            
        }
    }
    
    /**
     * Creates default return values when a new attribute is added, all with values = 0
     * 
     * @return the list of default return value conditions using >= 0.
     */
    public static List<ReturnValueCondition> createDefaultReturnValue(){
        List<ReturnValueCondition> conditions = new ArrayList<ReturnValueCondition>();
        conditions.add(new ReturnValueCondition(OperatorEnum.GTE, 0, LowMediumHighLevelEnum.HIGH));
        conditions.add(new ReturnValueCondition(OperatorEnum.GTE, 0, LowMediumHighLevelEnum.MEDIUM));
        conditions.add(new ReturnValueCondition(OperatorEnum.GTE, 0, LowMediumHighLevelEnum.LOW));
        
        return conditions;
    }
    
    /**
     * Returns an array of return value conditions with all values set to 0. The return values are based on
     * the learner state enums associated with the given attribute, excluding the Unknown' learner state since
     * the current attribute scoring interface doesn't support it.
     * 
     * @param learnerStateAttribute The learner state attribute to create return value conditions for.
     * @return A list of return value conditions.
     */
    public static List<ReturnValueCondition> createReturnValue(LearnerStateAttributeNameEnum learnerStateAttribute){
        List<ReturnValueCondition> conditions = new ArrayList<ReturnValueCondition>();
        
        if(learnerStateAttribute != null) {
            for(AbstractEnum value : learnerStateAttribute.getAttributeAuthoredValues()) {
                conditions.add(0, new ReturnValueCondition(OperatorEnum.GTE, 0, value));
            }       
            return conditions;          
        } 
        
        return createDefaultReturnValue();
    }

    /**
     * Set an interface to notify when a survey panel is closed
     * 
     * @param closeable the interface to set.
     */
    public void setCloseableInterface(CloseableInterface closeable) {
        closeableInterface = closeable;
    }

    @Override
    public List<String> getSurveyImageList() {
        return surveyImageList;
    }

    /**
     * Asynchronous method used to shutdown the panel.  This should be called when the panel is closed.
     * The caller is expected to wait for the callback before proceeding with the shutdown of the panel.
     * Any shutdown logic is performed such as canceling or releasing survey locks.
     * 
     * @param modalCallback Callback used to signal when the shutdown process has completed.
     */
    public void shutdownPanel(final AsyncCallback<Boolean> modalCallback) {

        showLoadingDialog("Closing Survey", "Closing the survey, please wait.");
        // Clear out any widgets when the editor is unloaded.
        workspaceWidget.clear();
        surveyPageContainer.clear();
        loadedSurvey = null;
        
        if (permissionsManager.hasLocks()) {
            permissionsManager.releaseSurveyLocks(new AsyncCallback<Boolean>() {

                @Override
                public void onFailure(Throwable t) {
                    
                    if(logger.isLoggable(Level.SEVERE)) {
                        logger.log(Level.SEVERE,"releaseSurveyLocks() returned failure: " + t.getMessage(), t);
                    }
                    
                    hideLoadingDialog();
                    // Signal back to the modal to continue shutting down.  There is nothing to do here if there is a failure
                    // in releasing the locks.
                    modalCallback.onSuccess(true);
                    
                }

                @Override
                public void onSuccess(Boolean result) {
                    
                    if(logger.isLoggable(Level.INFO)) {
                        logger.info("releaseSurveyLocks() returned result: " + result);
                    }
                    
                    hideLoadingDialog();
                    // Signal back to the modal to continue shutting down regardless of the result.
                    modalCallback.onSuccess(true);
                    
                }
                
            });
        } else {
            // Signal back to the modal to continue shutting down.
            hideLoadingDialog();
            modalCallback.onSuccess(true);
        }
        
    }
    
    /**
     * Sets whether or not this widget should handle survey events
     * 
     * @param enabled whether or not this widget should handle survey events
     */
    void setSurveyEventHandlingEnabled(boolean enabled){
        
        if(enabled){
            
            if (eventRegistration == null) {

                eventRegistration = eventBinder.bindEventHandlers(SurveyEditorPanel.this,
                        SharedResources.getInstance().getEventBus());
            }

        } else {

            if (eventRegistration != null) {

                eventRegistration.removeHandler();
                eventRegistration = null;
            }
        }
    }
    
    /**
     * Counts the total number of filtered questions
     * 
     * @return the number of filtered questions
     */
    int countFilteredQuestions() {
       
        if (loadedSurvey != null && !loadedSurvey.getPages().isEmpty() && filterWidget != null) {

            final QuestionFilter filter = filterWidget.getFilter();

            if (filter != null) {
                filteredQuestions = 0;
                // Count the number of questions that apply to the filter
                for (AbstractSurveyElement element : loadedSurvey.getPages().get(0).getElements()) {
                    if (filter.matches(element)) {
                        filteredQuestions++;
                    }
                }
            }
        }
        
        return filteredQuestions;
    }

    /**
     * Check to see if we need to display the Load More Questions button
     * by counting the total number of questions in the filter
     */
    void displayLoadMoreButton() {

        if (surveyType == SurveyDialogOption.ASSESSLEARNER_QUESTIONBANK) {
            // When the selected page has not been set in a question bank survey, select the one
            // and only page for the author (since survey page authoring is hidden for question
            // bank authoring)
            if (selectedPage == null && surveyPageContainer.getWidgetCount() > 0) {
                Widget widget = surveyPageContainer.getWidget(0);
                if (widget instanceof SurveyPageWidget) {
                    SurveyPageWidget pageWidget = (SurveyPageWidget) widget;
                    selectedPage = pageWidget;
                }
            }
            
            if (loadedSurvey != null && !loadedSurvey.getPages().isEmpty() && filterWidget != null && selectedPage != null) {

                final QuestionFilter filter = filterWidget.getFilter();

                if (filter != null) {
                    filteredQuestions = 0;
                    // Count the number of questions that apply to the filter
                    for (AbstractSurveyElement element : loadedSurvey.getPages().get(0).getElements()) {
                        if (filter.matches(element)) {
                            filteredQuestions++;
                        }
                    }
                    
                    loadMoreQuestions.setVisible(selectedPage.getQuestionWidgets().size() < filteredQuestions);
                } else {
                    loadMoreQuestions.setVisible(selectedPage.getQuestionWidgets().size() < loadedSurvey.getPages().get(0).getElements().size());
                }
            } else {
                loadMoreQuestions.setVisible(false);
            }
        } else {
            loadMoreQuestions.setVisible(false);
        }
    }
    
    /**
     * A progress modal to indicate the status of survey elements while the edit mode is being changed
     */
    private class SurveyElementProgressModal extends ModalDialogBox {

        /** The total number of pages in this survey */
        private int numPages = 0;
        
        /** The description of the current task */
        private Label taskDescription = new Label("Loading survey elements...");
        
        /** A div with a green background to represent progress */
        private FlowPanel progressBar  = new FlowPanel();
        
        
        /** Timer used to close the dialog politely. */
        private Timer closeTimer = new Timer() {

            @Override
            public void run() {
                hide();
            }
            
        };
        
        /**
         * Constructor
         */
        public SurveyElementProgressModal() {
            
            setGlassEnabled(true);
            setHtml("Switching Modes");
            
            FlowPanel mainPanel = new FlowPanel();
            FlowPanel progressWrapper = new FlowPanel();
            
            mainPanel.add(taskDescription);
            mainPanel.add(progressWrapper);
            
            progressBar.getElement().getStyle().setProperty("background", "rgb(80, 175, 80)");
            progressBar.setWidth("0px");
            progressBar.setHeight("20px");
            
            progressWrapper.add(progressBar);
            progressWrapper.setWidth("100%");
            progressWrapper.getElement().getStyle().setProperty("background", "#e5e5e5");
            
            setWidget(mainPanel);
            setWidth("400px");
        }
        
        /**
         * Resets the progress indicator and task description
         */
        public void reset() {
            taskDescription.setText("Loading survey elements...");
            progressBar.setWidth("0px");
        }
        
        /**
         * Increments the total number of survey pages
         */
        public void addPage() {
            numPages += 1;
        }
        
        /**
         * Decrements the total number of survey pages
         */
        public void deletePage() {
            numPages -= 1;
        }
                 
        /**
         * Sets the total number of survey pages
         * 
         * @param totalPages The total number of pages in this survey
         */
        public void setPages(int totalPages) {
            numPages = totalPages;
        }
        
        /**
         * Increases progress to the percent of current/total
         *  
         * @param currentPage the page number of the current page.
         * @param current The current number of survey elements loaded on the current page.  This should NOT be greater
         * than the total.  If it is the progress text will update with the counts but the progress bar will remain at 100%.
         * @param total The total number of survey elements on the current page.  Should be greater than zero.
         */
        public void setProgress(int currentPage, final int current, final int total) {
            
            int percent = 0;
            
            if(current == 0 && total == 0) {
                taskDescription.setText("Loading page " + currentPage + " of " + numPages);
                percent = 100;
                
            }else if(total <= 0){
                percent = 0;
                
            } else {    
                taskDescription.setText("Page " + currentPage + " of " + numPages + ": Loading survey element " + current + " of " + total);
                 percent = ((int) ((current * 100.0) / total));
                 
                 //in case the current value is greater than total
                 if(percent > 100){
                     percent = 100;
                 }
            }
            
            progressBar.setWidth(percent + "%");
            
            if(numPages == currentPage && current == total) {
                closeTimer.schedule(500);
            }
        }
    }
}
