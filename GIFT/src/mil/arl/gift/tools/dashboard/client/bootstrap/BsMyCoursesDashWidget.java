/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Container;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.NavTabs;
import org.gwtbootstrap3.client.ui.Navbar;
import org.gwtbootstrap3.client.ui.html.Paragraph;
import org.gwtbootstrap3.client.ui.html.Text;
import org.gwtbootstrap3.extras.select.client.ui.MultipleSelect;
import org.gwtbootstrap3.extras.select.client.ui.OptGroup;
import org.gwtbootstrap3.extras.select.client.ui.Option;
import org.gwtbootstrap3.extras.select.client.ui.Select;
import org.gwtbootstrap3.extras.select.client.ui.constants.SelectedTextFormat;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.DomainOption;
import mil.arl.gift.common.DomainOption.DomainOptionRecommendation;
import mil.arl.gift.common.course.CourseConceptsUtil;
import mil.arl.gift.common.enums.DeploymentModeEnum;
import mil.arl.gift.common.enums.DomainOptionRecommendationEnum;
import mil.arl.gift.common.gwt.client.GenericRpcResponse;
import mil.arl.gift.common.gwt.client.RpcResponse;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.AbstractBsWidget;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.BsLoadingDialogBox;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ModalDialogCallback;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ProgressBarListEntry;
import mil.arl.gift.common.gwt.shared.CourseValidationParams;
import mil.arl.gift.common.gwt.shared.DocumentUtil;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.io.CourseListFilter;
import mil.arl.gift.common.io.CourseListFilter.CourseSourceOption;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.DownloadableFileRef;
import mil.arl.gift.common.io.FileTreeModel;
import mil.arl.gift.common.io.LoadedProgressIndicator;
import mil.arl.gift.common.io.ProgressIndicator;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.common.util.StringUtils.Stringifier;
import mil.arl.gift.tools.dashboard.client.Dashboard;
import mil.arl.gift.tools.dashboard.client.DashboardService;
import mil.arl.gift.tools.dashboard.client.DashboardServiceAsync;
import mil.arl.gift.tools.dashboard.client.UiManager;
import mil.arl.gift.tools.dashboard.client.bootstrap.BsDialogWidget.DialogCallback;
import mil.arl.gift.tools.dashboard.client.bootstrap.BsDialogWidget.DialogType;
import mil.arl.gift.tools.dashboard.client.bootstrap.file.FileSelectionModal;
import mil.arl.gift.tools.dashboard.shared.ScreenEnum;
import mil.arl.gift.tools.dashboard.shared.rpcs.CorrectCoursePathsResult;
import mil.arl.gift.tools.dashboard.shared.rpcs.DeleteCourseResult;
import mil.arl.gift.tools.dashboard.shared.rpcs.ExportResponse;
import mil.arl.gift.tools.dashboard.shared.rpcs.GetCourseListResponse;
import mil.arl.gift.tools.dashboard.shared.rpcs.ImportCoursesResponse;
import mil.arl.gift.tools.dashboard.shared.rpcs.ProgressResponse;
import mil.arl.gift.tools.dashboard.shared.rpcs.ValidateCourseResponse;

/**
 * The BsMyCoursesDashWidget encapsulates the functionality required for the "my courses" panel 
 * in the cloud dashboard.  It contains a subheader (for filtering courses), plus a grid of selectable
 * courses that the user can take action on.
 *
 * @author nblomberg
 */
public class BsMyCoursesDashWidget extends AbstractBsWidget {

    private static Logger logger = Logger.getLogger(BsMyCoursesDashWidget.class.getName());
    
    private static BootstrapMyCoursesDashWidgetUiBinder uiBinder = GWT.create(BootstrapMyCoursesDashWidgetUiBinder.class);
    
    /** Whether or not to validate course paths. Should be true when the user first visits the Dashboard. */
    private static boolean checkCourseNames = true;
    
    private static final String NO_COURSE_SERVER_MESSAGE = "Courses could not be retrieved from the content management system.";
    private static final String NO_COURSE_MESSAGE_FILTER = "There are no courses to display with the selected course filter and/or search term.  Change the filter, search term, import some courses, or create a new one.";
    private static final String ERROR_FETCH_COURSES = "Error Retrieving Courses";
    private static final String ERROR_THROWABLE_MESSAGE = "A throwable error was caught trying to fetch the course list. Courses will not be displayed.";
    private static final String ERROR_RPC_FAILURE = "An rpc error occurred getting the course listing.  Courses will not be displayed.";

    /** Time (in ms) of how frequently the export progress should be polled. */
    private static final int POLL_EXPORT_PROGRESS_TIMER_MS = 200;
    
    /** Time (in ms) of how frequently the delete progress should be polled. */
    private static final int POLL_DELETE_PROGRESS_TIMER_MS = 200;
    
    /** Time (in ms) of how frequently the course list progress should be polled. */
    private static final int POLL_COURSE_LIST_PROGRESS_TIMER_MS = 200;
    
    /**
     * cookie for Dashboard Source filter selection
     */
    private static final String COOKIE_SELECTED_SOURCE = "Dashboard_SelectedSource"; //Name: Must conform to RFC 2965. Not allowed: = , ; white space. Also can't begin with $.
    private static final long ONE_YEAR = 1000 * 60 * 60 * 24 * 365; 
    
    /** the default selections to make on the course source type filter if no selection was made by the user in the past */
    private final List<String> DEFAULT_SOURCE_SELECTIONS;
        
    /**
     * Labels and progress percents for the take a course progress modal
     */
    private static final String READY_TASK_LABEL = "Ready";
    private static final String STARTING_TASK_LABEL = "Starting";
    private static final String LOADING_TASK_LABEL = "Loading";
    private static final String VALIDATING_TASK_LABEL = "Validating";
    private static final int LOADED_PERCENT = 100;
    private static final int VALIDATED_PERCENT = 50;
    private static final int NO_PERCENT = 0;
    
    /** A regular expression used to locate words and phrases in a search text expression */
    private static final String wordExpression =
        "-?\"[^\"]*\"" +    //double quotes around phrases(s)
        "|-?[A-Za-z0-9']+"  //single word
    ;

    /** A regular expression used to locate binary operators in a search text expression */
    private static final String binaryOperatorExpression = "(" + wordExpression + ")(\\s+(AND|OR)\\s+(" + wordExpression + "))+";
    
    private enum TakeCourseStateEnum{
        INACTIVE,
        VALIDATING,
        LOADING,
        CANCELING
    }
    
    private TakeCourseStateEnum currentTakeCourseState = TakeCourseStateEnum.INACTIVE;
    
    /** how often to check for course load progress updates */
    private static int CHECK_COURSE_LOAD_PROGRESS_DURATION = 250;
    
    /** 
     * contains the recent GWT RPC request for the take a course logic 
     * can be null if not currently going through the take a course logic 
     */
    private Request currentTakeACourseRequest = null;
    
    /** title of the error dialog when loading a course fails */
    private final String LOAD_COURSE_FAILURE = "Load Course Failure";
    
    /** 
     * parameters used to load a GIFT course (e.g runtime course id).  
     */
    private LoadCourseParameters loadCourseParameters;
    
    /** 
     * contains information about the course being started.
     * can be null if not currently going through the take a course logic
     */
    private DomainOption startingCourseData = null;
    
    /** A timer that is used to check the progress of the course load.  The timer can be cancelled if the user aborts the load course operation. */
    private Timer checkProgressTimer = null;
    
    /** This is the data model that represents the filter options that the user has chosen. It is sent to the server when requesting the course list. */
    private final CourseListFilter courseListFilter = new CourseListFilter();
    
    /**
     * The list of options for filtering a course.  These are visible to the user.
     * @author nblomberg
     *
     */
    public enum CourseFilterEnum {
        FILTER_RECOMMENDED("Recommended"),
        FILTER_REFRESHER("Refresher"),
        FILTER_OTHER("Other"),
        FILTER_INVALID("Invalid"),
        FILTER_NONE("Show All");
        
        private String displayName = null;
        
        private CourseFilterEnum(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    /**
     * The views for this widget
     * 
     * @author nroberts
     */
    public enum ViewEnum{
        DEFAULT,
        EXPORT,
        DELETE
    }
    
    /**
     * Create a remote service proxy to talk to the server-side dashboard service.
     */
    private final DashboardServiceAsync dashboardService = GWT
            .create(DashboardService.class);

    @UiField
    Container ctrlCourseList;
    
    @UiField
    Heading ctrlCourseMessage;
    
    @UiField
    Paragraph ctrlLoadPanel;
    
//    @UiField
//    BsLoadingIcon ctrlLoadIcon;
    
    @UiField
    MultipleSelect ctrlSourceOptions;
    
    @UiField
    Select ctrlFilterOptions;
    
    @UiField
    Container ctrlCourseDetails;
    
    @UiField
    Button ctrlCloseDetails;
    
    @UiField
    Container courseContainer;
    
    @UiField
    Paragraph courseDescription;
    
    @UiField
    Heading coursePath;
    
    @UiField
    NavTabs tabBar;
    
    @UiField
    AnchorListItem tabDesc;
    
    @UiField
    AnchorListItem tabReq;
    
    @UiField
    Container descriptionContents;
    
    @UiField
    Container requirementsContents;
    
    @UiField
    Button ctrlValidateAll;
    
    @UiField
    Button ctrlNewCourse;
    
    @UiField
    Container contentContainer;
    
    @UiField
    Button ctrlExportCourses;
    
    @UiField
    Button ctrlDeleteCourses;
    
    @UiField
    Button ctrlImportCourses;
    
    @UiField 
    Button ctrlCloseExport;
    
    @UiField
    Button ctrlExportSelectAll;
    
    @UiField
    Button ctrlExportSelectNone;
    
    @UiField
    Button ctrlStartExportCourses;
    
    @UiField 
    Button ctrlCloseDelete;
    
    @UiField
    Button ctrlDeleteSelectAll;
    
    @UiField
    Button ctrlDeleteSelectNone;
    
    @UiField
    Button ctrlStartDeleteCourses;
    
    DomainOption courseDetailData = null;
    
    @UiField
    Modal exportDialog;
    
    @UiField
    BsExportSummaryWidget exportSummary = new BsExportSummaryWidget();
    
    @UiField
    Button beginExportButton;
    
    @UiField
    Button cancelExportButton;
    
    @UiField
    Modal exportProgressDialog;
    
    @UiField
    ProgressBarListEntry subtaskProgress;
    
    @UiField
    ProgressBarListEntry overallProgress;
    
    @UiField
    Button cancelExportProgressButton;
    
    @UiField
    Text exportProgressDialogHeading;
    
    @UiField
    Modal takeACourseProgressDialog;
    
    @UiField
    ProgressBarListEntry takeACourseOverallProgress;
    
    @UiField
    Button cancelTakeACourseProgressButton;
    
    @UiField
    Text takeACourseProgressDialogHeading;
    
    @UiField
    Navbar exportNavBar;
    
    @UiField
    Modal deleteDialog;
    
    @UiField
    BsDeleteSummaryWidget deleteSummary = new BsDeleteSummaryWidget();
    
    @UiField
    Button beginDeleteButton;
    
    @UiField
    Button cancelDeleteButton;
    
    @UiField
    Modal deleteProgressDialog;
    
    @UiField
    ProgressBarListEntry deleteSubtaskProgress;
    
    @UiField
    ProgressBarListEntry deleteOverallProgress;
    
    @UiField
    Text deleteProgressDialogHeading;
    
    @UiField
    Navbar deleteNavBar;
    
    @UiField
    Modal courseListProgressDialog;
    
    @UiField
    ProgressBarListEntry courseListSubtaskProgress;
    
    @UiField
    ProgressBarListEntry courseListOverallProgress;
    
    @UiField
    Text courseListProgressDialogHeading;
    
    @UiField(provided = true)
    FileSelectionModal fileSelectionModal = new FileSelectionModal(Dashboard.IMPORT_SERVLET_URL);
    
    @UiField
    Modal importProgressDialog;
    
    @UiField
    ProgressBarListEntry importSubtaskProgress;
    
    @UiField
    ProgressBarListEntry importOverallProgress;
    
    @UiField
    Button cancelImportProgressButton;
    
    @UiField
    Text importProgressDialogHeading;
    
    @UiField
    Text overwriteDialogHeading;
    
    @UiField
    Modal overwriteFileDialog;
    
    @UiField
    BsDialogRenameWidget renameCourseDialog;
    
    @UiField
    HTML overwriteMessage;
    
    @UiField
    Button overwriteButton;
    
    @UiField
    Button noOverwriteButton;
    
    @UiField
    Button overwriteAllButton;
    
    /**
     * the textfield used to search for courses and filter the viewed courses
     */
    @UiField
    protected org.gwtbootstrap3.client.ui.TextBox coursesSearchBox;
    
    /**
     * the button to execute the search of courses
     */
    @UiField
    protected Button coursesSearchButton;
    
    /**
     *  The raw course map contains the complete map of each domain option to the ui widget that is represented that was retrieved from the server. 
     *  #4842 - this was a TreeMap but importing multiple courses seemed to not add to this map but sometimes replace items in the tree map
     *          with the put method.  
     */
    HashMap<DomainOption, BsCourseWidget> rawCourseMap = new HashMap<DomainOption, BsCourseWidget>();
    
    /** comparator used for sorting courses prior to filtering */
    private CourseComparator courseComparator = new CourseComparator();
    
    /** the current list of courses displayed based on 'filter options' and search term */
    List<DomainOption> displayedCourseList = new ArrayList<DomainOption>();
    
    ArrayList<BsCourseWidget> visibleCourseWidgets = new ArrayList<BsCourseWidget>();
    
    /** Used to resolve conflicting survey images contained in a course import */
    ArrayList<String> imagesToRemove = new ArrayList<String>();
    
    /**
     * Contains survey images from an attempted course import that conflict with
     * images in the 'surveyWebResources' directory
     */
    ArrayList<String> conflictingImages = new ArrayList<String>();
    
    /** A map of conflicting course paths to the new course names */
    Map<String, String> conflictingCourses = new HashMap<String, String>();
    
    /** A map of conflicting image files to their overwrite prompts detailing file size, modification date, etc. */
    Map<String, String> imageConflictPromptsMap = new HashMap<String, String>();
    
    /** A map of conflicting courses to their rename prompts. */
    Map<String, String> courseConflictPromptsMap = new HashMap<String, String>();
    
    String exportLocation;
    
    int currentFileConflict = 0;
    int currentCourseConflict = 0;
    
    /** A reference to the {@link GetCourseListResponse} that contains the up-converted courses */
    GetCourseListResponse upConvertedCourses;
    
    CourseFilterEnum selectedFilter = CourseFilterEnum.FILTER_NONE;
    
    boolean cancelledExport = false;
    
    boolean cancelledImport = false;
    
    /** Timer used to poll each child course widget to see if any are in the loading course state. */
    Timer pollCourseLoadTimer = null;
    
    /** Timer used to poll the server for export progress. */
    Timer pollExportProgressTimer = null;
    
    /** Timer used to poll the server for delete progress. */
    Timer pollDeleteProgressTimer = null;
    
    /** Timer used to poll the server for course list progress. */
    Timer pollCourseListProgressTimer = null;
    
    /** whether this widget is in debug mode */
    boolean debugModeRequested = false;
    
    interface BootstrapMyCoursesDashWidgetUiBinder extends UiBinder<Widget, BsMyCoursesDashWidget> {
    }
    
    
    // Click handler for the tab bar in the "Course Details" screen.  The tab bar consists of 
    // tabs such as "Description" and "Requirements".  More tabs may be added in the future.
    private ClickHandler tabClickHandler = new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {
            
            logger.fine("Tab bar was clicked.  Source is: " + event.getSource());
            
            Widget source = (Widget)(event.getSource());
            
            if (source != null) {
                Widget parent = source.getParent();
                logger.fine("Tab bar was clicked.  Parent is: " + parent);
                if (parent != null && 
                        parent instanceof AnchorListItem) {
                    selectAndPopulateTabBar((AnchorListItem) parent);
                }
            }            
        }
        
    };
    
    private ViewEnum currentView = ViewEnum.DEFAULT;
    
    boolean shouldPollForExportProgress;
    
    boolean shouldPollForImportProgress;
    
    boolean shouldPollForDeleteProgress;
    
    boolean shouldPollForCourseListProgress;
    
    /**
     * Constructor
     */
    public BsMyCoursesDashWidget() {
        
        initWidget(uiBinder.createAndBindUi(this));
        
        UiManager.getInstance().fillToBottomOfViewport(contentContainer);
        
        List<String> sourceFilterProp = Dashboard.getInstance().getServerProperties().getCourseListFilter();
        if(CollectionUtils.isEmpty(sourceFilterProp)){
            // use a default list
            DEFAULT_SOURCE_SELECTIONS = Arrays.asList(CourseSourceOption.MY_COURSES.name(), CourseSourceOption.SHOWCASE_COURSES.name());
        }else{
            // convert the display names of the enums into the name values which is used by the multiple select widget
            Set<String> options = new HashSet<>(3);
            for (String value : sourceFilterProp) {

                if (StringUtils.isNotBlank(value)) {
                    
                    for(CourseSourceOption optionEnum : CourseSourceOption.values()){
                        if(value.equals(optionEnum.getDisplayName())){
                            options.add(optionEnum.name());
                            break;
                        }
                    }

                }
            }
            DEFAULT_SOURCE_SELECTIONS = new ArrayList<>(options);
        }
       
        if(logger.isLoggable(Level.FINE)){
            logger.fine("Getting course list for session id: " + UiManager.getInstance().getSessionId());
        }
        
        ctrlCourseMessage.setVisible(false);
        ctrlCourseDetails.setVisible(false);
        
        OptGroup filterOptionsGroup = new OptGroup();
        for(CourseFilterEnum filter : CourseFilterEnum.values()) {
            Option option = new Option();
            option.setText(filter.getDisplayName());
            option.setValue(filter.name());
            
            if(filter == selectedFilter){
                option.setSelected(true);
            }
            filterOptionsGroup.add(option);
        }
        
        ctrlFilterOptions.add(filterOptionsGroup);
        
        ctrlSourceOptions.addValueChangeHandler(new ValueChangeHandler<List<String>>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<List<String>> event) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("ctrlSourceOptions.onValueChange(" + event.getValue() + ")");
                }

                courseListFilter.getCourseSourceOptions().clear();
                StringBuilder selectedValues = new StringBuilder();
                for(String selected : event.getValue()) {
                    CourseSourceOption option = CourseSourceOption.valueOf(selected);
                    courseListFilter.getCourseSourceOptions().add(option);
                    
                    selectedValues.append(selected).append(Constants.COMMA);
                }
                
                //update cookie
                Date expires = new Date(System.currentTimeMillis() + ONE_YEAR);
                Cookies.setCookie(COOKIE_SELECTED_SOURCE+Constants.PERIOD+UiManager.getInstance().getUserName(), selectedValues.toString(), expires, null, Constants.FORWARD_SLASH, false);
                
                refreshCourseList(null);
            }
        });
        
        OptGroup sourceOptionsGroup = new OptGroup();
        for(CourseSourceOption source : CourseSourceOption.values()) {
            
            if(source == CourseSourceOption.SHARED_COURSES &&
                    Dashboard.getInstance().getServerProperties().getDeploymentMode() == DeploymentModeEnum.DESKTOP){
                // course sharing not supported in Desktop mode
                continue;
            }
            
            Option option = new Option();
            option.setText(source.getDisplayName());
            option.getElement().getStyle().setFontWeight(FontWeight.BOLD);
            option.setValue(source.name());
            option.setColor(BsCourseWidget.getCourseSourceOptionTypeColor(source));
            sourceOptionsGroup.add(option);
        }
        
        ctrlSourceOptions.add(sourceOptionsGroup);
        ctrlSourceOptions.setSelectedTextFormat(SelectedTextFormat.COUNT);
        ctrlSourceOptions.refresh();
        
        //
        // Attempt to load previously saved Source enums from cookie (username specific)
        //
        String selectedSourceCookie = Cookies.getCookie(COOKIE_SELECTED_SOURCE+Constants.PERIOD+UiManager.getInstance().getUserName());
        if (selectedSourceCookie != null) {
            // attempt to apply user's saved selections from cookie
            
            if(selectedSourceCookie.isEmpty()){
                //handle case where no source enums are checked
                ValueChangeEvent.fire(ctrlSourceOptions, new ArrayList<String>());
            }else{
                
                String[] values = selectedSourceCookie.split(Constants.COMMA);
                List<String> options = new ArrayList<>(3);
                for (String value : values) {
    
                    if (value != null && !value.isEmpty()) {
                        try {
                            CourseSourceOption option = CourseSourceOption.valueOf(value);
                            if (option != null) {
                                options.add(option.name());
                            }
                        } catch (@SuppressWarnings("unused") Exception e) {
                            /* ignore, what else could we do? */
                        }
                    }
                }
                
                if (options.isEmpty()) {
                    /* the cookie contains a non empty value but could not determine
                     * which enums to select, apply default selection of 'My
                     * Courses' */
                    ctrlSourceOptions.setValue(DEFAULT_SOURCE_SELECTIONS, true);
                } else {
                    /* apply selections found in cookie */
                    ctrlSourceOptions.setValue(options, true);
                }
            }

        }else{
            //cookie was never saved, apply default selection of 'My Courses'
            ctrlSourceOptions.setValue(DEFAULT_SOURCE_SELECTIONS, true);
        }
        
        // Disable subheader controls until course loading is completed.
        updateSubHeaderControls(false);
        
        // Setup the tab bar.
        tabDesc.addClickHandler(tabClickHandler);
        tabReq.addClickHandler(tabClickHandler);
        tabReq.setActive(false);
        tabDesc.setActive(true);
        descriptionContents.setVisible(true);
        requirementsContents.setVisible(false);
        
        
        ctrlFilterOptions.addValueChangeHandler(new ValueChangeHandler<String>() {
        
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
        
                // $TODO$ nblomberg
                // We could optimize this if needed by only updating if the index changes.
                selectedFilter = CourseFilterEnum.valueOf(event.getValue());
                filterSearchCourses();
            }
        });
        
        coursesSearchBox.addKeyUpHandler(new KeyUpHandler() {

            @Override
            public void onKeyUp(KeyUpEvent event) {
                
                if(event.getNativeKeyCode() == KeyCodes.KEY_ENTER){
                    filterSearchCourses();
                }else if(StringUtils.isBlank(coursesSearchBox.getValue())){
                    filterSearchCourses();
                }
            }
        });

        coursesSearchButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {                
                filterSearchCourses();                
            }
        });
        
        fileSelectionModal.getFileSelector().setAllowedFileExtensions(new String[]{".zip"});
        fileSelectionModal.setOptionalInstructionsText("Please select a GIFT export (.zip) that contains courses you would like to import into your workspace.");
        
        // Click handler for the "Close" button the course details page.
        ctrlCloseDetails.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                
                // Close the course details panel.
                closeCourseDetails();
            }
        });
        
        ctrlValidateAll.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent arg0) {
                
                if(!debugModeRequested){
                    return;
                }

                CourseValidationParams params = new CourseValidationParams();
                params.setForceCourseLogicValidation(true);
                params.setForceSurveyValidation(true);
                for(DomainOption domainOption : rawCourseMap.keySet()){
                    
                    BsCourseWidget widget = rawCourseMap.get(domainOption);
                    widget.startValidation(params);
                }
            }
        });
        
        
        // Handler for the new course button.
        ctrlNewCourse.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                cancelPendingRpcs();
                UiManager.getInstance().showEditCourse(null);                
            }
            
        });
        
        ctrlExportCourses.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                if(currentView.equals(ViewEnum.EXPORT)){
                    setView(ViewEnum.DEFAULT);
                    
                } else {
                    setView(ViewEnum.EXPORT);
                }
            }
        });
        
        ctrlCloseExport.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                setView(ViewEnum.DEFAULT);
            }
        });
        
        ctrlExportSelectAll.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                selectAllVisibleCourses();
            }
        });
        
        ctrlExportSelectNone.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                selectNoVisibleCourses();
            }
        });

        ctrlStartExportCourses.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                List<DomainOption> selectedCourses = getSelectedCourses();
                
                if(selectedCourses != null && !selectedCourses.isEmpty()){
                    showExportSummaryDialog(selectedCourses);
                } else {
                    UiManager.getInstance().displayErrorDialog("No Courses Selected", "Please select the courses you wish to export.", null);
                }
            }
        });
        
        cancelExportButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                exportDialog.hide();
            }
        });
        
        beginExportButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                List<DomainOption> selectedCourses = getSelectedCourses();
                
                cancelExportProgressButton.setEnabled(true);
                    
                cancelledExport = false;
                
                ctrlStartExportCourses.setEnabled(false);
                
                exportProgressDialogHeading.setText("Creating Export...");
                
                dashboardService.exportCourses(UiManager.getInstance().getUserName(), selectedCourses, new AsyncCallback<ExportResponse>() {
                    
                    @Override
                    public void onSuccess(final ExportResponse response) {
                        
                        shouldPollForExportProgress = false;
                        
                        ctrlStartExportCourses.setEnabled(true);
                        
                        if(!cancelledExport){
                        
                            if(response.isSuccess()){                                                               
                                //export completely finished, so need to give the client the file.
                                
                                setView(ViewEnum.DEFAULT);
                                
                                exportProgressDialog.hide();
                                
                                final DownloadableFileRef result = response.getExportResult();
                                
                                Window.open(result.getDownloadUrl(),"","");
                                
                                UiManager.getInstance().displayDialog(DialogType.DIALOG_INFO, "Export Successful!", ""
                                        + "Your export has been successfully created.<br/>"
                                        + "<br/>"
                                        + "If your download doesn't start in a few seconds please, "
                                        + "<a href='" + result.getDownloadUrl() + "' target='_self'>click here</a> "
                                        + "to start the download."
                                        
                                        , new DialogCallback() {
                                            
                                            @Override
                                            public void onAccept() {
                                                
                                                dashboardService.deleteExportFile(result, new AsyncCallback<RpcResponse>(){

                                                    @Override
                                                    public void onFailure(
                                                            Throwable e) {
                                                        
                                                        /* 
                                                         * Do Nothing. File deletion is handled silently, therefore errors 
                                                         * should be handled on the server.
                                                         */                                                     
                                                    }

                                                    @Override
                                                    public void onSuccess(
                                                            RpcResponse response) {
                                                        
                                                        //Do Nothing. File deletion is handled silently, so nothing should happen here.
                                                    }
                                                    
                                                });
                                            }
                                        }
                                );
                            
                            } else {
                                
                                exportProgressDialog.hide();
                                
                                UiManager.getInstance().displayDetailedErrorDialog("Export Failed", 
                                        response.getResponse(), response.getErrorDetails(), response.getErrorStackTrace(), null);
                            }
                            
                        } else {                                
                            //export was cancelled, so no need to do anything except hide the export progress dialog
                            
                            exportProgressDialog.hide();
                            
                            cancelledExport = false;
                        }
                    }
                    
                    @Override
                    public void onFailure(Throwable e) {
                        
                        shouldPollForExportProgress = false;
                        
                        ctrlStartExportCourses.setEnabled(true);
                        
                        if(!cancelledExport){
                        
                            UiManager.getInstance().displayErrorDialog("Export Failed", "An error occurred during the export: " + e.toString() +".", null);
                    
                        } else {                                
                            //export was cancelled, so no need to do anything except hide the export progress dialog
                            
                            exportProgressDialog.hide();
                            
                            cancelledExport = false;
                        }
                    }
                });
                
                exportProgressDialog.show();
                
                shouldPollForExportProgress = true;
                
                scheduleExportProgressTimer();
            }
        });
        
        cancelExportProgressButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                cancelExportProgressButton.setEnabled(false);
                
                cancelledExport = true;
                
                exportProgressDialogHeading.setText("Cancelling Export...");
                
                dashboardService.cancelExport(UiManager.getInstance().getUserName(), new AsyncCallback<RpcResponse>() {

                    @Override
                    public void onFailure(Throwable e) {
                        UiManager.getInstance().displayErrorDialog("Cancel Export Failed", "An error occurred while cancelling the export: " + e.toString() +".", null);
                    }

                    @Override
                    public void onSuccess(RpcResponse response) {
                        
                        if(response.isSuccess()){
                            shouldPollForExportProgress = false;
                            
                        } else {
                            if(response.getResponse() == null){
                                UiManager.getInstance().displayErrorDialog("Cancel Export Failed", "An error occurred while cancelling the export.", null);
                                
                            } else {
                                UiManager.getInstance().displayErrorDialog("Cancel Export Failed", "An error occurred while cancelling the export: " + response.getResponse(), null);
                            }
                        }
                    }
                    
                });
            }
        });
        
        cancelTakeACourseProgressButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent arg0) {
                
                currentTakeCourseState = TakeCourseStateEnum.CANCELING;
                
                //cancel RPCs
                cancelCourseLoading(startingCourseData);                

                //re-enable the my courses page
                // Note: do this after cancelCourseLoading because finishedStartingCourse method will remove
                // the progress indicator object which cancelCourseLoading method is using on the server side.
                finishedStartingCourse();
            }
        });
        
        ctrlDeleteCourses.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                if(currentView.equals(ViewEnum.DELETE)){
                    setView(ViewEnum.DEFAULT);
                    
                } else {
                    setView(ViewEnum.DELETE);
                }
            }
        });
        
        ctrlCloseDelete.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                setView(ViewEnum.DEFAULT);
            }
        });
        
        ctrlDeleteSelectAll.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                selectAllVisibleCourses();
            }
        });
        
        ctrlDeleteSelectNone.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                selectNoVisibleCourses();
            }
        });

        ctrlStartDeleteCourses.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                List<DomainOption> selectedCourses = getSelectedCourses();
                
                if(selectedCourses != null && !selectedCourses.isEmpty()){
                    showDeleteSummaryDialog(selectedCourses);
                } else {
                    UiManager.getInstance().displayErrorDialog("No Courses Selected", "Please select the courses you wish to delete.", null);
                }
            }
        });
        
        cancelDeleteButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                deleteDialog.hide();
            }
        });
        
        beginDeleteButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                final List<DomainOption> selectedCourses = getSelectedCourses();
                
                ctrlStartDeleteCourses.setEnabled(false);
                
                deleteProgressDialogHeading.setText("Deleting course(s)...");
                
                dashboardService.deleteCourses(UiManager.getInstance().getSessionId(), selectedCourses, true, false, new AsyncCallback<DeleteCourseResult>() {
                    
                    @Override
                    public void onSuccess(final DeleteCourseResult response) {
                        
                        shouldPollForDeleteProgress = false;
                        
                        ctrlStartDeleteCourses.setEnabled(true);
                        
                        if(response.isSuccess()){                                                               
                            //delete completely finished
                            
                            setView(ViewEnum.DEFAULT);
                            
                            deleteProgressDialog.hide();
                        
                        } else {
                            
                            deleteProgressDialog.hide();
                            
                            if(response.getCourseWithIssue() != null){
                                UiManager.getInstance().displayDetailedErrorDialog("Delete Failed", 
                                        "There was a problem while trying to delete '"+response.getCourseWithIssue().getDomainName()+"' because "+response.getResponse()+
                                            ".</br></br>Some or all of the other selected courses may still have been deleted for you.", 
                                        response.getResponse(), 
                                        response.getErrorStackTrace(), null);
                            }else{
                                UiManager.getInstance().displayDetailedErrorDialog("Delete Failed", 
                                        response.getResponse(), 
                                        response.getResponse(), 
                                        response.getErrorStackTrace(), null);
                            }
                        }
                        
                        if(!selectedCourses.isEmpty()){
                            
                            for(DomainOption domainOption : selectedCourses){
                                
                                BsCourseWidget courseWidget = rawCourseMap.get(domainOption);
                                if(courseWidget != null){
                                    removeCourse(courseWidget);
                                }
                            }
                        }
                    }
                    
                    @Override
                    public void onFailure(Throwable e) {
                        
                        shouldPollForDeleteProgress = false;
                        
                        ctrlStartDeleteCourses.setEnabled(true);
                        
                        UiManager.getInstance().displayErrorDialog("Delete Failed", 
                                "There was a problem while trying to delete a course: " + e.toString() +
                                    ".</br></br>Some or all of the other selected courses may still have been deleted for you.", 
                                null);
                    }
                });
                
                deleteProgressDialog.show();
                
                shouldPollForDeleteProgress = true;
                
                scheduleDeleteProgressTimer();
            }
        });
        
        ctrlImportCourses.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                setView(ViewEnum.DEFAULT);
                
                fileSelectionModal.show();
            }
        });
        
        fileSelectionModal.addValueChangeHandler(new ValueChangeHandler<String>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                
                exportLocation = event.getValue();
                
                fileSelectionModal.hide();
                
                cancelImportProgressButton.setEnabled(true);
                
                cancelledImport = false;
                
                importProgressDialogHeading.setText("Creating Import...");
                
                dashboardService.checkForImportConflicts(UiManager.getInstance().getUserName(), exportLocation, new AsyncCallback<ImportCoursesResponse>() {
                    
                    @Override
                    public void onSuccess(final ImportCoursesResponse response) {
                        
                        if(response.isSuccess()){
                            if(response.hasConflicts()){
                                
                                importProgressDialog.hide();
                                
                                // Reset all conflict data
                                currentFileConflict = 0;
                                currentCourseConflict = 0;
                                
                                imagesToRemove.clear();
                                conflictingImages.clear();
                                conflictingCourses.clear();
                                imageConflictPromptsMap.clear();
                                courseConflictPromptsMap.clear();
                                
                                // Populate the conflict data
                                imageConflictPromptsMap.putAll(response.getImageOverwritePrompts());
                                conflictingImages.addAll(imageConflictPromptsMap.keySet());
                                courseConflictPromptsMap.putAll(response.getCourseRenamePrompts());
                                
                                if(!courseConflictPromptsMap.isEmpty()) {
                                    handleImportedCourseConflicts();
                                    
                                } else if(!imageConflictPromptsMap.isEmpty()) {
                                    handleImportedImageConflicts();
                                }
                            }
                        }else{
                            
                            //TODO: use DetailedExceptionWrapper here and pass to handleFailedImport to it can use a better dialog for displaying errors
                            Exception exception = new Exception(response.getResponse() +" because "+response.getAdditionalInformation());
                            handleFailedImport(exception);
                        }
                    }
                    
                    @Override
                    public void onFailure(Throwable e) {
                        handleFailedImport(e);
                    }
                });
                
                importProgressDialog.show();
                
                shouldPollForImportProgress = true;
                
                maybePollForImportProgress();
            }
        });
        
        cancelImportProgressButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                cancelImportProgressButton.setEnabled(false);
                
                cancelledImport = true;
                
                importProgressDialogHeading.setText("Cancelling Import...");
                
                dashboardService.cancelImport(UiManager.getInstance().getUserName(), new AsyncCallback<RpcResponse>() {

                    @Override
                    public void onFailure(Throwable e) {
                        
                        UiManager.getInstance().displayErrorDialog("Cancel Import Failed", "An error occurred while cancelling the import: " + e.toString() +".", null);
                        
                        cancelImportProgressButton.setEnabled(true);
                        importProgressDialog.hide();
                    }

                    @Override
                    public void onSuccess(RpcResponse response) {
                        
                        if(!response.isSuccess()){
                            
                            if(response.getResponse() == null){
                                UiManager.getInstance().displayErrorDialog("Cancel Import Failed", "An error occurred while cancelling the import.", null);
                                
                            } else {
                                UiManager.getInstance().displayErrorDialog("Cancel Import Failed", "An error occurred while cancelling the import: " + response.getResponse(), null);
                            }
                            
                            cancelImportProgressButton.setEnabled(true);
                            importProgressDialog.hide();
                        }
                    }
                    
                });
            }
        });
        
        overwriteButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {
                showNextConflict(true, false);
            }
            
        });
        
        overwriteAllButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {
                showNextConflict(true, true);
            }
        
        });
        
        noOverwriteButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {
                showNextConflict(false, false);
            }
            
        });

        // only show in debug mode
        ctrlValidateAll.setVisible(UiManager.getInstance().isDebugMode());        
    }
    
    /**
     * First filters the retrieved list of courses from the server bsead on the source type(s) selected and then
     * applies any search term provided.
     */
    private void filterSearchCourses(){
        
        /** changes the displayedCourseList list based on the currently selected filter on the course list retrieve from 
         * the server based on the source types selected */
        filterCourses(selectedFilter);
        
        /** further changes the displayCourseList list from filterCourses above */
        applyCoursesSearchFilter();
        
        if (!displayedCourseList.isEmpty()) {
          // Populate the course list based on the new array.
          populateCourseList(displayedCourseList);
        } else {
            // Display a message stating that the user has no courses for the selected filters.
            logger.fine("no courses to display based on search term: '"+coursesSearchBox.getValue()+"' and filter '" + selectedFilter + "'.");
            ctrlCourseMessage.setText(NO_COURSE_MESSAGE_FILTER);
            ctrlCourseMessage.setVisible(true);
        }
    }
    
    /**
     * Apply the current courses search term filter on the current domains being viewed. If the current search term
     * is blank, this method doesn't remove (or add back) any courses.
     * @return true if there is a search term and it was applied, false if there was no search term.
     */
    private boolean applyCoursesSearchFilter(){
        
        String searchTerm = coursesSearchBox.getValue();    
        if(StringUtils.isNotBlank(searchTerm)){
            
            logger.info("Filtering displayed course list size of "+displayedCourseList.size()+" using search term = '"+ searchTerm +"'.");
            
            // get the filtered list that is a subset of the current list of domains 
            displayedCourseList = filterCoursesBySearchStr(searchTerm, displayedCourseList);
            
            return true;
        }else{
            return false;
        }        

    }
    
    /**
     * Filter the course widget list provided by using the filter expression as the search
     * term.
     * 
     * @param filterExpression the expression used to filter courses. If null or empty
     *        the list provided is not reduced in size.
     * @param toFilter the list to filter. If null or empty, an empty list will
     *        be returned.
     * @return the filtered list. Can't be null but can be empty.
     */
    public static List<DomainOption> filterCoursesBySearchStr(final String filterExpression, List<DomainOption> toFilter) {
        List<DomainOption> result = new ArrayList<>();
        if (toFilter == null || toFilter.isEmpty()) {
            return result;
        }
        if (StringUtils.isBlank(filterExpression)) {
            result.addAll(toFilter);
            return result;
        }
        final List<DomainOption> toFilterCopy = new ArrayList<>(toFilter);
        final RegExp searchTermExp = RegExp.compile(binaryOperatorExpression + "|" + wordExpression, "gm");
        /* Parse the filter expression to get the list of search terms */
        final List<String> searchTerms = new ArrayList<String>();
        for (MatchResult matcher = searchTermExp.exec(filterExpression); matcher != null; matcher = searchTermExp
                .exec(filterExpression)) {
            searchTerms.add(matcher.getGroup(0));
        }
        for (String currentTerm : searchTerms) {
            if (StringUtils.isBlank(currentTerm) || (currentTerm.equals("AND") && searchTerms.size() == 1) || (currentTerm.equals("OR") && searchTerms.size() == 1)) {
                continue;
            }
            currentTerm = currentTerm.trim();
            /* If a term matches the regular expression for a binary operator
             * chain, perform the binary operations specified and add the
             * resulting rows to the result */
            if (currentTerm.matches(binaryOperatorExpression)) {
                /* Parse the binary operator chain for its operands */
                final List<String> operands = Arrays.asList(currentTerm.split("\\s+AND\\s+|\\s+OR\\s+"));
                /* Parse the binary operator chain for its operators */
                for (String operand : operands) {
                    currentTerm = currentTerm.replaceAll(operand, "");
                }
                currentTerm = currentTerm.trim();
                final List<String> operators = Arrays.asList(currentTerm.split("\\s+"));
                /* For each operand, perform the next binary operation specified
                 * using result of the previous binary operation and the operand
                 * itself */
                final List<DomainOption> binaryOpResult = new ArrayList<>();
                for (String operand : operands) {
                    final int j = operands.indexOf(operand);
                    if (operands.indexOf(operand) == 0) {
                        binaryOpResult.addAll(filterCoursesBySearchStr(operand, toFilterCopy));
                    } else if (operators.get(j - 1).matches("AND")) {
                        binaryOpResult.retainAll(filterCoursesBySearchStr(operand, toFilterCopy));
                    } else if (operators.get(j - 1).matches("OR")) {
                        binaryOpResult.addAll(filterCoursesBySearchStr(operand, toFilterCopy));
                    }
                }
                /* Add what items remain to the result */
                result.addAll(binaryOpResult);
            } else if (currentTerm.startsWith("-")) {
                /* If a term starts with a '-', then all rows captured by
                 * searching for the remainder of the term will be removed from
                 * the result */
                /* If there are already items in the result, search for the
                 * remainder of the search term and remove all items found in
                 * the search */
                if (!result.isEmpty()) {
                    result.removeAll(filterCoursesBySearchStr(currentTerm.substring(1), toFilterCopy));
                    /* Otherwise, add all the items in the table to the result,
                     * search for the remainder of the search term, and remove
                     * all items found in the search */
                } else {
                    result.addAll(toFilterCopy);
                    result.removeAll(filterCoursesBySearchStr(currentTerm.substring(1), toFilterCopy));
                }
            } else {
                /* It is still possible for an AND/OR keyword to make it here
                 * if it is the final term of a search query. If it does make 
                 * it to this point, skip over the currentTerm.
                 */
                if(currentTerm.equals("AND") || currentTerm.equals("OR")) {
                    continue;
                }
                /* Otherwise, treat the term as a single phrase */
                /* If the term begins and ends with quotes, remove the quotes
                 * before evaluating the term */
                boolean exactMatch = false;
                if (currentTerm.startsWith("\"") && currentTerm.endsWith("\"")) {
                    exactMatch = true;
                    currentTerm = currentTerm.substring(1, currentTerm.length() - 1);
                }
                /* Find all the published items containing the course's text
                 * (ignoring case) #5015 - ignore case so user doesn't have to worry about matching*/
                for (DomainOption course : toFilterCopy) {
                    final String courseName = course.getDomainName();
                    if ((exactMatch && courseName.contains(currentTerm))
                            || (!exactMatch && courseName.toLowerCase().contains(currentTerm.toLowerCase()))) {
                        if (logger.isLoggable(Level.INFO)) {
                            logger.info("By item name -> " + courseName);
                        }
                        result.add(course);
                        continue;
                    }
                    
                    final String courseDesc = course.getDescription();
                    if (courseDesc != null && ((exactMatch && courseDesc.contains(currentTerm))
                            || (!exactMatch && courseDesc.toLowerCase().contains(currentTerm.toLowerCase())))) {
                        if (logger.isLoggable(Level.INFO)) {
                            logger.info("By item description -> " + courseDesc);
                        }
                        result.add(course);
                        continue;
                    }
                    
                  final generated.course.Concepts courseConcepts = course.getConcepts();
                  if(courseConcepts != null){
                      
                      boolean added = false;
                      List<String> courseConceptsList = CourseConceptsUtil.getConceptNameList(courseConcepts);
                      for(String courseConcept : courseConceptsList){
                          
                          if ((exactMatch && courseConcept.contains(currentTerm))
                                  || (!exactMatch && courseConcept.toLowerCase().contains(currentTerm.toLowerCase()))) {
                              logger.info("By course concept -> "+ course.getDomainName());
                              added = result.add(course);
                              break;
                          }
                      }
                      
                      if(added){
                          continue;
                      }
                  }
                }
            }
        }
        return result;
    }
    
    /**
     * Shows the initial image conflicts dialog.
     */
    private void handleImportedImageConflicts() {
        overwriteMessage.setHTML(imageConflictPromptsMap.get(conflictingImages.get(currentFileConflict)));
        overwriteDialogHeading.setText("A File Already Exists (1 of " + conflictingImages.size() + ")");
        
        overwriteFileDialog.show();
    }
    
    /**
     * This method takes care of any course conflicts. The user is prevented from rename courses with 
     * course names that already exist. They are also prevented from using the same name more than once
     * if there are multiple courses to be imported. 
     * <br>
     * Once all rename prompts are complete, image conflicts will automatically be presented if available.
     */
    private void handleImportedCourseConflicts() {

        final List<String> conflictingCourseNames = new ArrayList<String>();
        conflictingCourseNames.addAll(courseConflictPromptsMap.keySet());
        
        // Retrieve the names of courses in the user's workspace. This will be used later
        // to prevent renaming with an existing course name
        final List<String> courseNames = new ArrayList<String>();
        for(DomainOption course : rawCourseMap.keySet()) {
            if(course.getDomainId().startsWith(UiManager.getInstance().getUserName())
                || course.getDomainId().startsWith("/" + UiManager.getInstance().getUserName())) {
                courseNames.add(course.getDomainName().toLowerCase());
            }
        }
        
        renameCourseDialog.setData(
            "A Course Already Exists (1 of " + courseConflictPromptsMap.size() + ")", 
            courseConflictPromptsMap.get(conflictingCourseNames.get(0)), 
            "Rename Course", 
            new ValueChangeHandler<String>() {
                
                @Override
                public void onValueChange(ValueChangeEvent<String> event) {
                    // This method is fired when the user clicks the 'Rename Course' button 
                    
                    String validationMsg = DocumentUtil.validateFileName(event.getValue());
                    if(validationMsg != null){
                        // If new name contains invalid characters, prompt them to enter a new name
                        renameCourseDialog.showInvalidMessage(validationMsg  + "<br/><br/>Please enter a new name.");
                        
                    }else if(courseNames.contains(event.getValue().toLowerCase())) {
                        // If a course in the user's workspace already has the target name, prompt them to enter a new name
                        renameCourseDialog.showInvalidMessage("A course with this name already exists. Please enter a different name.");

                    } else if(!conflictingCourses.containsValue(event.getValue())) {
                        // If the user hasn't already renamed a previous conflicting course with the same name:
                        
                        // Add the new name to the map
                        conflictingCourses.put(conflictingCourseNames.get(currentCourseConflict), event.getValue());

                        // Increment the current conflict index and check if all conflicts have been resolved
                        currentCourseConflict += 1;
                        if(currentCourseConflict == courseConflictPromptsMap.size()) {
                            renameCourseDialog.hide();
                            
                            if(!imageConflictPromptsMap.isEmpty()) {
                                // Handle any image conflicts if available
                                handleImportedImageConflicts();
                                
                            } else {
                                // Proceed with the reset of the import
                                
                                importProgressDialog.show();
                                dashboardService.importCourses(UiManager.getInstance().getUserName(), exportLocation, conflictingImages, conflictingCourses, new AsyncCallback<ImportCoursesResponse>() {
                                    
                                    @Override
                                    public void onFailure(Throwable e) {
                                        handleFailedImport(e);
                                    }

                                    @Override
                                    public void onSuccess(ImportCoursesResponse response) {
                                        //Nothing to do. The results of the import will be handled asynchronously by the polling logic.                             
                                    }                           
                                });         
                            }
                            
                        } else {
                            // There are more conflicts to resolve, update the dialog message
                            renameCourseDialog.changeText(
                                    "A Course Already Exists (" + currentCourseConflict + " of" + courseConflictPromptsMap.size() + ")",
                                    courseConflictPromptsMap.get(conflictingCourseNames.get(currentCourseConflict)));
                        }

                    } else {
                        // The user already renamed a previous conflicting course with the same name, prompt them to enter a new name
                        renameCourseDialog.showInvalidMessage("This name is being used for another course in this import. Please enter a different name.");
                    }
                }
            });
        renameCourseDialog.show();

    }
   
    
    /**
     * Action to take when an import fails.
     * 
     * @param exception the exception thrown during the import
     */
    private void handleFailedImport(Throwable exception) {
        
        cancelImportProgressButton.setEnabled(true);
        importProgressDialog.hide();
        
        shouldPollForImportProgress = false;
        
        if(!cancelledImport){
        
            UiManager.getInstance().displayErrorDialog("Import Failed", "An error occurred during the import: " + exception.toString() +".", null);
    
        } else {                                
            //import was cancelled, so no need to do anything except hide the import progress dialog
            
            cancelledImport = false;
        }
    }
    
    /**
     * Action to take when an import is complete
     * 
     * @param response the server response to the course import ending, either successfully or not.
     * If successful the response should have a list of the courses that were imported.
     */
    private void completeImport(LoadedProgressIndicator<List<DomainOption>> response) {
        
        cancelImportProgressButton.setEnabled(true);
        importProgressDialog.hide();
        
        shouldPollForImportProgress = false;
        
        if(!cancelledImport){
            
            if(response.getException() == null){
                
                //import completely finished with no errors, so need to refresh the course list
                
                setView(ViewEnum.DEFAULT);
                
                List<DomainOption> courses = response.getPayload();
                if(courses != null && !courses.isEmpty()){                    
                    //create the course tiles for the newly imported courses
                    
                    createCourseWidgets(courses, false, true);
                    
                    filterSearchCourses();
                    
                    StringBuilder messageSb = new StringBuilder("The following courses were imported into your workspace.<br/>"
                            + "<ul>");
                    for(DomainOption newCourse: courses){
                        messageSb.append("<li>");
                        messageSb.append(newCourse.getDomainName());
                        messageSb.append("</li>");
                    }
                    messageSb.append("</ul><br/><br/><i>These courses are currently highlighted in an red animated surrounding shadow.</i>");
                    
                    UiManager.getInstance().displayInfoDialog("Import Successful", messageSb.toString());

                }else{
                
                    refreshCourseList(new AsyncCallback<Boolean>() {
    
                        @Override
                        public void onFailure(Throwable caught) {
                            //nothing to do, should be handled in refreshCourseList method
                        }
    
                        @Override
                        public void onSuccess(Boolean refreshedCourseList) {
                            
                            //only show a dialog if a dialog wasn't shown in the refreshCourseList method,
                            //otherwise competing dialogs could leave the webpage in a bad state, e.g. glass over entire page leaving it un-clickable
                            if(refreshedCourseList != null && refreshedCourseList){
                                UiManager.getInstance().displayInfoDialog("Import Successful", "Your courses have been successfully imported.");
                            }
                        }
                    });
                }
                
                
            } else {
                
                UiManager.getInstance().displayDetailedErrorDialog(
                        "Import Failed", 
                        response.getException().getReason(), 
                        response.getException().getDetails(), 
                        response.getException().getErrorStackTrace(),
                        null
                );
            }
            
        } else {                                
            //import was cancelled, so no need to do anything except hide the import progress dialog
            
            cancelledImport = false;
        }
    }
    
    /**
     * Displays information about subsequent file conflicts. If there are no more conflicts,
     * the import will restart.
     * 
     * @param overwrite - whether or not to overwrite the current file
     * @param overwriteAll - whether or not to overwrite all files
     */
    private void showNextConflict(boolean overwrite, boolean overwriteAll) {
        
        if(currentFileConflict <= conflictingImages.size() && !overwriteAll) {
            if(!overwrite) {
                imagesToRemove.add(conflictingImages.get(currentFileConflict));
            }
            
            currentFileConflict++;
        }
        
        if(currentFileConflict == conflictingImages.size() || overwriteAll) {
            
            conflictingImages.removeAll(imagesToRemove);            
            overwriteFileDialog.hide();
            importProgressDialog.show();
            
            dashboardService.importCourses(UiManager.getInstance().getUserName(), exportLocation, conflictingImages, conflictingCourses, new AsyncCallback<ImportCoursesResponse>() {
                
                @Override
                public void onFailure(Throwable e) {
                    handleFailedImport(e);
                }

                @Override
                public void onSuccess(ImportCoursesResponse response) {
                    //Nothing to do. The results of the import will be handled asynchronously by the polling logic.                             
                }                           
            });         
        } else {
            overwriteDialogHeading.setText("A File Already Exists (" + (currentFileConflict + 1) + " of " + conflictingImages.size() + ")");
            overwriteMessage.setHTML(imageConflictPromptsMap.get(conflictingImages.get(currentFileConflict)));
        }
        
    }
    
    /**
     * Selects and populates the tab bar based on a selected anchor list item.
     * 
     * @param source - The Anchor list item that was selected.  Cannot be null.
     */
    private void selectAndPopulateTabBar(AnchorListItem source) {
        
        if (source.equals(tabDesc)) {
            
            tabReq.setActive(false);
            tabDesc.setActive(true);
            descriptionContents.setVisible(true);
            requirementsContents.setVisible(false);

        } else if (source.equals(tabReq)) {
            
            tabDesc.setActive(false);
            tabReq.setActive(true);
            descriptionContents.setVisible(false);
            requirementsContents.setVisible(true);
        }        
    }


    /**
     * Filters the displayed courses based on the filter that is chosen.
     * 
     * @param filterOption - The filter that should be applied to the displayed courses.
     */
    private void filterCourses(CourseFilterEnum filterOption) {
        
        // Default the course message to be hidden.
        ctrlCourseMessage.setVisible(false);
        
        // Clear out the panel of any previous elements.
        ctrlCourseList.clear();
        
        // Reset the array of the courses we will display.
        displayedCourseList.clear();
        
        logger.info("Applying "+filterOption+" on "+rawCourseMap.size());
        
        // since no longer using TreeMap due to key collisions removing elements, 
        // sort the courses prior to filtering each time
        List<DomainOption> courses = new ArrayList<>(rawCourseMap.keySet());
        Collections.sort(courses, courseComparator);
        
        // Iterate over the raw list of courses we got from the server.
        for (DomainOption courseData : courses) {

            // Check to see if the filter applies to this domain item.
            if (doesDomainOptionMatchCourseFilter(courseData, filterOption)) {
                displayedCourseList.add(courseData);
            }else{
                logger.fine("not displaying "+courseData);
            }

        }
        
        logger.fine("Resulting displayed course list size = "+displayedCourseList.size());
        
    }
    
    /**
     * Check to see if a domain option should be displayed based on a filter that the user has selected.
     * 
     * @param option - the domain option (course data) to validate.
     * @param filter - the filter that should be checked to see if the course should be displayed.
     * @return
     */
    private boolean doesDomainOptionMatchCourseFilter(DomainOption option, CourseFilterEnum filter) {
        boolean success = false;
        
        // If the filter is NONE then the course should be displayed.
        if (filter == CourseFilterEnum.FILTER_NONE) {
            success = true;
        } else {
        
            DomainOptionRecommendation rec = option.getDomainOptionRecommendation();
            
            if (rec != null) {
                DomainOptionRecommendationEnum recType = rec.getDomainOptionRecommendationEnum();
                              
                if(recType.compareTo(DomainOptionRecommendationEnum.RECOMMENDED) == 0){
                    
                    if (filter == CourseFilterEnum.FILTER_RECOMMENDED) {
                        success = true;
                    }
                    
                }else if(recType.compareTo(DomainOptionRecommendationEnum.NOT_RECOMMENDED) == 0 ){
                    
                    if (filter == CourseFilterEnum.FILTER_REFRESHER) {
                        success = true;
                    }
                    
                }else{ 
                    
                    // Everything else is treated as an INVALID course.
                    if (filter == CourseFilterEnum.FILTER_INVALID) {
                        success = true;
                    }
                   
                }
            } else {
                // If there is no domainoptionrecommendation, the course is listed as an OTHER type.
                if (filter == CourseFilterEnum.FILTER_OTHER){
                    success = true;
                }
            }
        }     

        return success;
    }

    

    
    /**
     * Populates the ui with the course list based on an array of domain options.  This creates
     * a grid element based on twitter bootstrap grid layout.  Currently 4 course widgets are displayed
     * per row.
     * 
     * @param courseList - The array of domain options to display (should not be null).
     */
    private void populateCourseList(List<DomainOption> courseList) {

        logger.fine("populating course list with size: " + courseList.size());
        

        // Add everything in a single row, specify styling for small & medium resolutions.
        // This works as long as the height of the elements do not change from one row to another, as
        // well as not having different number of items on each row.  This will keep elements in multiple rows
        // of 12 units each.  For Extra Small devices, we should have a single column, for small devices, we
        // should have 3 items per row (each taking 4 grid units), and for medium+ devices, 
        // there should be 4 items per row (each taking 3 grid units).  
        
        // Default the course message to be hidden.
        ctrlCourseMessage.setVisible(false);
        
        // Clear out the panel of any previous elements.
        ctrlCourseList.clear();
        
        visibleCourseWidgets.clear();
       
        for (int x = 0; x < courseList.size(); x++) {

            DomainOption courseItem = courseList.get(x);
            BsCourseWidget course = rawCourseMap.get(courseItem);
            
            if(currentView.equals(ViewEnum.EXPORT) || currentView.equals(ViewEnum.DELETE)){
                course.setSelectable(true);
            }
            
            visibleCourseWidgets.add(course);
            placeCourseWidgetIntoSection(course);
        }
            
    }
    
    /**
     * Adds a created course widget into the collection of course 
     * widgets to be displayed
     * @param course the course tile to add to the course list
     */
    private void placeCourseWidgetIntoSection(BsCourseWidget course) {
        //If the course is null don't try to add it
        if(course == null) {
            return;
        }
        
        ctrlCourseList.add(course);
    }
    
    /**
     * Kills any pending operations of any course widgets
     */
    public void cancelPendingRpcs() {
        for (BsCourseWidget courseWidget : rawCourseMap.values()) {
            courseWidget.cancelPendingRpcs();
        }
        
        // This will abort any currently outstanding rpc request related to starting the process of taking a course
        if (currentTakeACourseRequest != null) {
            logger.info("Cancelling currently outstanding rpc request related to starting the process of taking a course.");
            currentTakeACourseRequest.cancel();
            currentTakeACourseRequest = null;
        }
        
        // Cancel the timer to check for updates.
        if (checkProgressTimer != null) {
            logger.info("Cancelling checkprogress timer.");
            checkProgressTimer.cancel();
            checkProgressTimer = null;
        }

    }
     
    /**
     * Start the logic to take a course.
     * 
     * @param courseData the course data that will be used to find the course and start the course.  Cannot be null.
     */
    public void takeACourse(final DomainOption courseData){
        
        currentTakeCourseState = TakeCourseStateEnum.INACTIVE;
        startingCourseData = courseData;
        
        //show progress dialog
        takeACourseProgressDialog.show();
        
        //first step is to validate the course on the server
        startValidation(courseData, null);
    }
    
    /**
     * Triggers a server request to load the specified course.  Since the course
     * files may no longer reside on the user's machine, it is possible they may need
     * to be downloaded first.  This function should be called before any request to 
     * start the course is made.
     */
    public void requestLoadCourse(final DomainOption courseData) {
        
        //start at the validation percent ending value
        takeACourseOverallProgress.updateProgress(LOADING_TASK_LABEL, VALIDATED_PERCENT);
        
        currentTakeCourseState = TakeCourseStateEnum.LOADING;
        
        // Note we are caching off the Request object here so that we can abort this rpc if the user wants to.  Instead of void, the rpc can return a
        // Result object which can be cancelled via Request.cancel() method.
        currentTakeACourseRequest = dashboardService.loadCourse(UiManager.getInstance().getSessionId(), courseData, new AsyncCallback<RpcResponse>() {

            @Override
            public void onFailure(Throwable t) {
                
                logger.severe("Unable to load the course: " + courseData + ".  Caught a throwable error: " + t.getMessage());
                
                finishedStartingCourse();
                
                UiManager.getInstance().displayDetailedErrorDialog(
                        LOAD_COURSE_FAILURE, 
                        "A server error occurred while loading the course.<br/><br/>"
                        + "<b>Course Name:</b> " + courseData.getDomainName()
                        + "<br/><b>Course Path:</b> " + courseData.getDomainId(), 
                        "A server error occurred:<br/>" + t.toString(), 
                        DetailedException.getFullStackTrace(t),
                        courseData.getDomainName());
            }

            @Override
            public void onSuccess(RpcResponse result) {
                
                if(currentTakeCourseState != TakeCourseStateEnum.LOADING){
                    return;
                }

                if (result.isSuccess()) {
                                        
                    String courseRuntimeId = result.getResponse();
                    loadCourseParameters = new LoadCourseParameters(courseRuntimeId, courseData.getDomainId(), courseData.getDomainName());
                    
                    if(logger.isLoggable(Level.INFO)){
                        logger.info("Load course returned success.  Runtime course id = " + courseRuntimeId);
                    }
                    
                    takeACourseOverallProgress.updateProgress(STARTING_TASK_LABEL, LOADED_PERCENT);
                    startLoadedCourse();
                    
                } else {
                    String errorMsg = "Unable to load the course: " + courseData + ".  Server returned an rpc error: " + result.getResponse() + "\n" + result.getAdditionalInformation();
                    logger.severe(errorMsg);
                    
                    finishedStartingCourse();
                    
                    if(result.getUserSessionId() == null){
                        UiManager.getInstance().displayScreen(ScreenEnum.LOGIN);
                        UiManager.getInstance().displayInfoDialog("Login Again", result.getResponse());
                    }else{
                  
                        UiManager.getInstance().displayDetailedErrorDialog(
                                LOAD_COURSE_FAILURE, 
                                "An error occurred while loading the course: " + result.getResponse() 
                                + "<br/><br/><b>Course Name:</b> " +  courseData.getDomainName()
                                + "<br/><b>Course Path:</b> " + courseData.getDomainId(), 
                                result.getAdditionalInformation(), 
                                result.getErrorStackTrace(),
                                courseData.getDomainName());
                    }
                    
                }
            }                

        });
        
        checkCourseLoadProgress(courseData);
        
    }
    
    /**
     * Start the course validation.  This makes an asynchronous call to the server
     * to check if the course is valid.  During the time of the async call we want to 
     * display a loading icon and disable some of the controls until the course can
     * be properly validated.
     */
    public void startValidation(final DomainOption courseData, CourseValidationParams courseValidationParams) {
        
        takeACourseOverallProgress.updateProgress(VALIDATING_TASK_LABEL, NO_PERCENT);
        
        currentTakeCourseState = TakeCourseStateEnum.VALIDATING;

        currentTakeACourseRequest = dashboardService.validateCourseData(UiManager.getInstance().getSessionId(), courseData, courseValidationParams, new AsyncCallback<ValidateCourseResponse>() {
            
            @Override
            public void onFailure(Throwable t) {                
                logger.severe("Unable to validate course: " + t.getMessage());
                
                finishedStartingCourse();
                
                if(t instanceof DetailedException){
                    DetailedException detailedException = (DetailedException)t;
                    UiManager.getInstance().displayDetailedErrorDialog(
                            "Failed to Start Course", 
                            detailedException.getReason(), 
                            detailedException.getDetails(), 
                            detailedException.getErrorStackTrace(),
                            courseData.getDomainName());
                }else{
                    UiManager.getInstance().displayDetailedErrorDialog(
                            "Failed to Start Course", 
                            "The course could not be started", 
                            "Course was not able to be validated because the server returned a response of \"" + t.toString() + "\"",                             null,
                            courseData.getDomainName());
                }
            }

            @Override
            public void onSuccess(ValidateCourseResponse result) {
                
                if(currentTakeCourseState != TakeCourseStateEnum.VALIDATING){
                    return;
                }
                
                if (result.isSuccess()) {
                        
                    //check validation response
                    DomainOption validationData = result.getCourse();
                    DomainOption.DomainOptionRecommendation recommendation = validationData.getDomainOptionRecommendation();
                    
                    //when validation fails, show reason
                    if(recommendation != null){
                        
                        //make sure course tile is up to date
                        BsCourseWidget courseWidget = rawCourseMap.get(validationData);
                        if(courseWidget != null){
                            courseWidget.updateCourseData(validationData);
                        }
                        
                        DomainOptionRecommendationEnum recommendationType = recommendation.getDomainOptionRecommendationEnum();
                        
                        if(logger.isLoggable(Level.FINE)){
                            logger.fine("Recommendation Type = " + recommendationType);
                        }
                        
                        if(recommendationType.isUnavailableType()){
                            //show dialog with information
                            
                            finishedStartingCourse();
                            
                            UiManager.getInstance().displayDetailedErrorDialog(
                                    "Failed to Start Course", 
                                    recommendation.getReason(), 
                                    recommendation.getDetails(), 
                                    null,
                                    courseData.getDomainName());
                            
                            return;
                        }
                    }
                    
                    if(logger.isLoggable(Level.INFO)){
                        logger.info("Successfully validated course.  Requesting the course to be loaded.");
                    }
                    
                    //continue with loading course
                    requestLoadCourse(courseData);                        
                    
                } else {
                    
                    logger.warning("Course was not able to be validated: " + result.getResponse());
                    
                    finishedStartingCourse();
                    
                    if(result.getUserSessionId() == null){
                        UiManager.getInstance().displayScreen(ScreenEnum.LOGIN);
                        UiManager.getInstance().displayInfoDialog("Login Again", result.getResponse() + "\nHave you been idle for a long time?");
                    }else{
                  
                        UiManager.getInstance().displayDetailedErrorDialog(
                                "Course Unavailable", 
                                "The course could not be started", 
                                "Course was not able to be validated because the server returned a response of \"" + result.getResponse() + "\"", 
                                null,
                                courseData.getDomainName());
                    }
                }
                
            }
            
        });
        
        checkCourseValidationProgress(courseData);
        
    }
    
    /**
     * Resets the My Courses dashboard page so that another course could
     * be taken.
     */
    private void finishedStartingCourse(){    
        
        logger.info("finishedStartingCourse()");
        
        // notify server to cleanup progress indicator
        dashboardService.cleanupLoadCourseIndicator(UiManager.getInstance().getSessionId(), startingCourseData,
                new AsyncCallback<GenericRpcResponse<Void>>() {
                    @Override
                    public void onSuccess(GenericRpcResponse<Void> response) {
                        // do nothing
                    }
        
                    @Override
                    public void onFailure(Throwable t) {
                        // do nothing, this is best effort
                    }
                });
        
        currentTakeCourseState = TakeCourseStateEnum.INACTIVE;
        startingCourseData = null;
        takeACourseProgressDialog.hide();

    }
    
    /**
     * Cancel the course loading operations.  This allows the user to cancel and either retry, or try to start other courses.
     */
    private void cancelCourseLoading(final DomainOption courseData) {

        cancelPendingRpcs();
        
        if(courseData == null){
            return;
        }        
        
        // Fire off a cancel load course to the server to let the server know we cancelled this load.  We don't care about the return case here.
        dashboardService.cancelLoadCourse(UiManager.getInstance().getSessionId(), courseData.getDomainId(), new AsyncCallback<RpcResponse>() {

            @Override
            public void onFailure(Throwable t) {
                logger.severe("CancelLoadCourse rpc caught a throwable error: " + t.getMessage());                
            }

            @Override
            public void onSuccess(RpcResponse result) {
                if (!result.isSuccess()) {
                    logger.severe("CancelLoadCoruse rpc returned failure: " + result);
                }                
            }            
        });        
    }
    
    /**
     * Starts the loaded course by using the course runtime id.  
     */
    private void startLoadedCourse() {
        
        if(logger.isLoggable(Level.INFO)){
            logger.info("Requesting to start course: " + loadCourseParameters);
        }

        UiManager.getInstance().displayScreen(ScreenEnum.COURSE_RUNTIME, loadCourseParameters );
        
        finishedStartingCourse();
    }
    
    /**
     * Schedule a task to run in the near future that will query the server for the 
     * current progress of the course validation operation being done on the server.  This progress
     * update will then be shown on the currently shown progress bar.  If the validation operation is still
     * on going, this method will recursively call itself. 
     * 
     * @param courseData contains information about the course being validated and is needed to lookup the 
     * validation progress for that course.
     */
    private void checkCourseValidationProgress(final DomainOption courseData) {
        
        if(logger.isLoggable(Level.FINE)){
            logger.fine("Checking validation progress for course: " + courseData.getDomainId());
        }
        
        checkProgressTimer = new Timer() {
            @Override
            public void run() {
                currentTakeACourseRequest = dashboardService.getValidateCourseProgress(UiManager.getInstance().getSessionId(), courseData.getDomainId(), new AsyncCallback<ProgressResponse>() {

                    @Override
                    public void onFailure(Throwable t) {
                        logger.severe("Error caught with getting validating course progress: " + t.getMessage());
                        
                        finishedStartingCourse();
                    }

                    @Override
                    public void onSuccess(ProgressResponse result) {
                        
                        if(currentTakeCourseState != TakeCourseStateEnum.VALIDATING){
                            return;
                        }
          
                        if (result.isSuccess()) {

                            ProgressIndicator progress = result.getProgress();
                            
                            if(logger.isLoggable(Level.FINE)){
                                logger.fine("Progress is: " + progress + "");
                            }
                            
                            if (progress != null /*&& getCourseState() != CourseState.LOADED*/) {
                                
                                //logger.warning("progress = "+((int)(progress.getPercentComplete() * (VALIDATED_PERCENT / 100.0))));
                                takeACourseOverallProgress.updateProgress(VALIDATING_TASK_LABEL, (int)(progress.getPercentComplete() * (VALIDATED_PERCENT / 100.0)));
                                
                                if (progress.getPercentComplete() < 100) {
                                    
                                    // If we're still getting updates, schedule another check to the server.
                                    checkCourseValidationProgress(courseData);
                                } 
                            }
                        } else {
                            logger.severe("Rpc error occurred while calling getCourseValidationProgress. Error is: " + result.getResponse());
                            
                            finishedStartingCourse();
                        }
                        
                    }
                    
                });
            }
          };
        
          // Check the progress again.
          checkProgressTimer.schedule(CHECK_COURSE_LOAD_PROGRESS_DURATION);
    }
    
    /**
     * Schedule a task to run in the near future that will query the server for the 
     * current progress of the course load operation being done on the server.  This progress
     * update will then be shown on the currently shown progress bar.  If the load operation is still
     * on going, this method will recursively call itself. 
     * 
     * @param courseData contains information about the course being loaded and is needed to lookup the 
     * load progress for that course.
     */
    private void checkCourseLoadProgress(final DomainOption courseData) {
        
        if(logger.isLoggable(Level.FINE)){
            logger.fine("Checking load progress for course: " + courseData.getDomainId());
        }
        
        checkProgressTimer = new Timer() {
            @Override
            public void run() {
                currentTakeACourseRequest = dashboardService.getLoadCourseProgress(UiManager.getInstance().getSessionId(), courseData.getDomainId(), new AsyncCallback<ProgressResponse>() {

                    @Override
                    public void onFailure(Throwable t) {
                        logger.severe("Error caught with getting load course progress: " + t.getMessage());
                        
                        finishedStartingCourse();
                    }

                    @Override
                    public void onSuccess(ProgressResponse result) {
                        
                        if(currentTakeCourseState != TakeCourseStateEnum.LOADING){
                            return;
                        }
          
                        if (result.isSuccess()) {

                            ProgressIndicator progress = result.getProgress();
                            
                            if(logger.isLoggable(Level.FINE)){
                                logger.fine("Progress is: " + progress + "");
                            }
                            
                            if (progress != null /*&& getCourseState() != CourseState.LOADED*/) {
                                
                                //starting percent for loading is VALIDATED_PERCENT (50%)
                                //then add a % of the remaining progress (50%)
                                //logger.warning("progress = "+(VALIDATED_PERCENT + (int)(progress.getPercentComplete() * ((LOADED_PERCENT - VALIDATED_PERCENT) / 100.0))));
                                takeACourseOverallProgress.updateProgress(LOADING_TASK_LABEL, VALIDATED_PERCENT + (int)(progress.getPercentComplete() * ((LOADED_PERCENT - VALIDATED_PERCENT) / 100.0)));
                                
                                if (progress.getPercentComplete() < 100) {
                                    
                                    // If we're still getting updates, schedule another check to the server.
                                    checkCourseLoadProgress(courseData);
                                } 
                            }
                        } else {
                            logger.severe("Rpc error occurred while calling getLoadCourseProgress. Error is: " + result.getResponse());
                            
                            finishedStartingCourse();
                        }
                        
                    }
                    
                });
            }
          };
        
          // Check the progress again.
          checkProgressTimer.schedule(CHECK_COURSE_LOAD_PROGRESS_DURATION);
    }


    /**
     * Display the course details panel for a specified course.
     * 
     * @param courseData - The course data to display the details for.  Should not be null.
     */
    public void displayCourseDetails(DomainOption courseData) {
        ctrlCourseList.setVisible(false);
        ctrlCourseDetails.setVisible(true);        
        
        // Cache off the course that the user is viewing.  This is used
        // when the start button is pressed, so we can know what course
        // the user is currently viewing in the details panel.
        courseDetailData = courseData;

        // For some reason, we cannot link in the BsCourseWidget from the UiBinder because
        // the popover doesn't always display.  Even when calling popover.reconfigure() the popover
        // only seems to display every other time.
        // For now we will dynamically create the widget which seems to workaround the issue.
        // 
        // Set the course container with the new course widget.
        courseContainer.clear();
        
        // This widget should already be validated so we don't need to have a notification back so
        // we pass in null for the parent argument.
        BsCourseWidget courseWidget = new BsCourseWidget(courseData, null);
        
        // Hide buttons we don't want the user to have access to on this widget.
        courseWidget.setCourseActionButtonsVisibility(false);
        
        courseWidget.setWidth("100%");
        
        courseContainer.add(courseWidget);

        coursePath.setText(courseData.getDomainId());
        courseDescription.setHTML(courseData.getDescription());
        
        // Default the tab bar.
        tabReq.setActive(false);
        tabDesc.setActive(true);
        descriptionContents.setVisible(true);
        requirementsContents.setVisible(false);
        
        // Disable the subheader controls()
        updateSubHeaderControls(false);
    }
    
    
    /**
     * Updates any controls on the subheader to be enabled or disabled.
     * 
     * @param enabled - True to enable all the controls, false otherwise.
     */
    private void updateSubHeaderControls(boolean enabled) {
       
        ctrlSourceOptions.setEnabled(enabled);
        ctrlFilterOptions.setEnabled(enabled);
        ctrlNewCourse.setEnabled(enabled);
        ctrlExportCourses.setEnabled(enabled);
        ctrlImportCourses.setEnabled(enabled);
        ctrlDeleteCourses.setEnabled(enabled);
    }
    
    
    /**
     * Helper function to determine if a course is invalid.
     * $TODO$ nblomberg
     * This could be moved to a utility class.
     * 
     * @param data - The domain option data to determine if it is considered invalid.
     * @return - true if the course is considered invalid, false otherwise.
     */
    public boolean isCourseInvalid(DomainOption data) {
        
        boolean invalidCourse = false;
        DomainOption.DomainOptionRecommendation recommendation = data.getDomainOptionRecommendation();
        if (recommendation != null) {
            
            DomainOptionRecommendationEnum recommendationType = recommendation.getDomainOptionRecommendationEnum();
            invalidCourse = recommendationType.isUnavailableType();           
        }
        
        return invalidCourse;
    }

    
    /**
     * Accessor to determine if the course details panel is open and visible.
     * 
     * @return - True if the course details panel is visible/open, false otherwise.
     */
    public boolean isCourseDetailsVisible() {
        return ctrlCourseDetails.isVisible();
    }

    /**
     * Closes the course details panel.
     */
    public void closeCourseDetails() {
        ctrlCourseList.setVisible(true);
        ctrlCourseDetails.setVisible(false);
        
        // Re-enable the subheader controls
        updateSubHeaderControls(true);
        
    }
    
    /**
     * Sets the current view, updating UI components accordingly
     * 
     * @param view the new view
     */
    private void setView(ViewEnum view){
        
        currentView = view;
            
        exportNavBar.setVisible(view.equals(ViewEnum.EXPORT));  
        deleteNavBar.setVisible(view.equals(ViewEnum.DELETE));
        
        for(BsCourseWidget course : visibleCourseWidgets){
            
            // to Export the user needs at least read access
            // to Delete the user needs ownership and write access
            if((view.equals(ViewEnum.EXPORT) && course.isReadable()) || 
                    (view.equals(ViewEnum.DELETE) && course.isModifiable())){           
                course.setSelectable(true);
                course.setSelected(false);
                
            } else {
                // this course can not be selected for the current view (e.g. course export, course delete)
                course.setSelectable(false);
                course.setSelected(false);
            }
        }
    }
    
    /**
     * Gets the list of selected courses
     * 
     * @return the of selected courses
     */
    private List<DomainOption> getSelectedCourses(){
        
        List<DomainOption> selectedCourses = new ArrayList<DomainOption>();
        
        for(BsCourseWidget widget : visibleCourseWidgets){
            if(widget.isSelected()){
                selectedCourses.add(widget.getCourseData());
            }
        }
        
        return selectedCourses;
    }
    
    /**
     * Selects all courses currently visible on screen
     */
    private void selectAllVisibleCourses(){
        
        for(BsCourseWidget widget : visibleCourseWidgets){
            widget.setSelected(true);
        }
    }
    
    /**
     * Deselects all courses currently visible on screen
     */
    private void selectNoVisibleCourses(){
        
        for(BsCourseWidget widget : visibleCourseWidgets){
            widget.setSelected(false);
        }
    }
    
    /**
     * Shows the export summary dialog populated with the specified list of courses to export
     * 
     * @param selectedCourses the list of courses to show in the summary
     */
    private void showExportSummaryDialog(List<DomainOption> selectedCourses) {
        
        exportSummary.buildCoursesToExportSummary(selectedCourses);
        
        exportDialog.show();
    }
    
    /**
     * Shows the delete summary dialog populated with the specified list of courses to delete
     * 
     * @param selectedCourses the list of courses to show in the summary
     */
    private void showDeleteSummaryDialog(List<DomainOption> selectedCourses) {
        
        deleteSummary.buildCoursesToDeleteSummary(selectedCourses);
        
        deleteDialog.show();
    }
    
    /**
     * Schedules a repeating timer which polls the server for export progress 
     * until the operation completes.
     */
    private void scheduleExportProgressTimer() {
        // Setup the timer to check for any pending course loads.
        pollExportProgressTimer = new Timer() {

            @Override
            public void run() {
                
                if(shouldPollForExportProgress){
                    
                    dashboardService.getExportProgress(UiManager.getInstance().getUserName(), new AsyncCallback<ProgressResponse>() {
            
                        @Override
                        public void onFailure(Throwable e) {
                            // Nothing to do
                        }
            
                        @Override
                        public void onSuccess(ProgressResponse response) {
                            
                            if(response.isSuccess()){
                                
                                ProgressIndicator progress = response.getProgress();
                                overallProgress.updateProgress(progress);
                                
                                if(progress.getSubtaskProcessIndicator() != null){
                                    subtaskProgress.updateProgress(progress.getSubtaskProcessIndicator());
                                }
                            }
                            
                        }
                    });
                    
                } else {
                    pollExportProgressTimer.cancel();
                }
                
            }
            
        };

        pollExportProgressTimer.scheduleRepeating(POLL_EXPORT_PROGRESS_TIMER_MS);
    }
    
    /**
     * Schedules a repeating timer which polls the server for delete course progress 
     * until the operation completes.
     */
    private void scheduleDeleteProgressTimer() {
        // Setup the timer to check for any pending course delete.
        pollDeleteProgressTimer = new Timer() {

            @Override
            public void run() {
                
                if(shouldPollForDeleteProgress){
                    
                    dashboardService.getDeleteProgress(UiManager.getInstance().getSessionId(), new AsyncCallback<ProgressResponse>() {
            
                        @Override
                        public void onFailure(Throwable e) {
                            // Nothing to do
                        }
            
                        @Override
                        public void onSuccess(ProgressResponse response) {
                            
                            if(response.isSuccess()){
                                
                                ProgressIndicator progress = response.getProgress();
                                deleteOverallProgress.updateProgress(progress);
                                
                                if(progress.getSubtaskProcessIndicator() != null){
                                    deleteSubtaskProgress.updateProgress(progress.getSubtaskProcessIndicator());
                                }
                            }
                            
                        }
                    });
                    
                } else {
                    pollDeleteProgressTimer.cancel();
                }
                
            }
            
        };

        pollDeleteProgressTimer.scheduleRepeating(POLL_DELETE_PROGRESS_TIMER_MS);
    }
    
    /**
     * Schedules a repeating timer which polls the server for get course list progress 
     * until the operation completes.
     */
    private void scheduleGetCourseListProgressTimer() {
        // Setup the timer to check for any pending get course list request.
        pollCourseListProgressTimer = new Timer() {

            @Override
            public void run() {
                
                if(shouldPollForCourseListProgress){
                    
                    dashboardService.getCourseListProgress(UiManager.getInstance().getSessionId(), new AsyncCallback<ProgressResponse>() {
            
                        @Override
                        public void onFailure(Throwable e) {
                            // Nothing to do
                        }
            
                        @Override
                        public void onSuccess(ProgressResponse response) {
                            
                            if(response.isSuccess()){
                                
                                ProgressIndicator progress = response.getProgress();
                                courseListOverallProgress.updateProgress(progress);
                                
                                if(progress.getSubtaskProcessIndicator() != null){
                                    courseListSubtaskProgress.updateProgress(progress.getSubtaskProcessIndicator());
                                }
                            }
                            
                        }
                    });
                    
                } else {
                    pollCourseListProgressTimer.cancel();
                }
                
            }
            
        };

        pollCourseListProgressTimer.scheduleRepeating(POLL_COURSE_LIST_PROGRESS_TIMER_MS);
    }
    
    /**
     * Creates the course widgets based on the list of domain options and keeps
     * a map of each domain option to the widget that is represented. This map
     * is used for filtering and is kept so that we don't need to recreate the
     * course widgets each time the filter is changed.
     * 
     * @param courseList - The list of course to create course tiles for (cannot be null).
     * @param replaceExisting - whether this list is replacing the previous list of courses.  Meaning
     * the previous course tiles should be replaced with a new list of course tiles.
     * @param tempHighlight - true if the new course widget(s) should have some form of temporary highlighting to draw
     * attention to these widgets.
     */
    private void createCourseWidgets(List<DomainOption> courseList, boolean replaceExisting, boolean tempHighlight) {

        if(replaceExisting){
            //Empty the map since we are about to rebuild it
            rawCourseMap.clear();
        }
        
        for (DomainOption courseItem : courseList) {
            
            if(courseItem == null){
                continue;
            }

            logger.fine("Creating course widget for "+courseItem.getDomainId());
            
            try{
                BsCourseWidget course = new BsCourseWidget(courseItem, this);
                if(tempHighlight){
                    course.applyTempHighlight();
                }
    
                // Keep a map of the domain option to the widget that it represents.
                rawCourseMap.put(courseItem, course);
            }catch(Exception e){
                logger.log(Level.SEVERE, "Failed to create course widget for "+courseItem.getDomainId(), e);
            }

        }

    }
    
    /**
     * Polls the server for the progress status of an ongoing import operation, assuming one is available.  
     * <br/><br/>
     * If an ongoing import operation is found on the server end, then this method will continually update the UI elements used 
     * to show the import progress to reflect the current state of the operation on the server. If the import then completes, this 
     * method will then perform the final UI logic needed to either display an error messages, in the case of a failure, or 
     * refresh the list of courses, in the case of a successful completion.
     * 
     * If no import operations are currently underway, then this method won't do anything.
     */
    private void maybePollForImportProgress(){
        
        if(shouldPollForImportProgress){
        
            dashboardService.getImportProgress(UiManager.getInstance().getUserName(), new AsyncCallback<LoadedProgressIndicator<List<DomainOption>>>() {

                @Override
                public void onFailure(Throwable e) {
    
                    maybePollForImportProgress();                   
                }
        
                    @Override
                public void onSuccess(LoadedProgressIndicator<List<DomainOption>> response) {
                        
                    if(response.isComplete()){
                            
                        //if the import has finished, deal with the completed import
                        completeImport(response);
                            
                    } else { 
                        
                        //otherwise, check to see if there is progress information
                        importOverallProgress.updateProgress(response);
                        
                        if(response.getSubtaskProcessIndicator() != null){
                            importSubtaskProgress.updateProgress(response.getSubtaskProcessIndicator());
                        }
                        
                        //schedule another poll for progress 1 second from now
                        Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {
                            
                            @Override
                            public boolean execute() {
                                
                                maybePollForImportProgress();
                                
                                return false;
                            }
                            
                        }, 1000);
                    }
                    }
                });
              }
    }
    
    /**
     * Notification from a child course widget that it has been successfully validated.  This means that
     * the domain option data for the child widget has changed and the my courses panel needs to be updated
     * with the new domain option data.  
     * 
     * @param oldCourseData - the old domain option data for the widget (cannot be null).
     * @param newCourseData - the new domain option data (validated) for the course widget (cannot be null).
     * @param courseWidget - the course widget that has been validated (cannot be null).
     */
    public void onValidationSuccess(DomainOption oldCourseData, DomainOption newCourseData, BsCourseWidget courseWidget) {
        
        // Update our raw course map with the new domain option data.
        if (oldCourseData != null && newCourseData != null && courseWidget != null) {
            rawCourseMap.remove(oldCourseData);
            rawCourseMap.put(newCourseData, courseWidget);
        }

    }
    
    /**
     * Comparator class used to sort the course tiles by:
     * 
     * 1. Course source option type
     * 1.1. My courses
     * 1.2. Shared courses
     * 1.3. showcase courses
     * 
     * 2. alphabetically by course name
     * 
     * @author nblomberg
     *
     */
    private class CourseComparator implements Comparator<DomainOption> {

        @Override
        public int compare(DomainOption d1, DomainOption d2) {
            
            if(d1.getCourseSourceOptionType() == null){
                
                if(d2.getCourseSourceOptionType() != null){
                    // d2 before d1
                    return 1;
                }
            }else if(d1.getCourseSourceOptionType() == d2.getCourseSourceOptionType()){
                // same source option type, compare by name
                
                return d1.getDomainName().toLowerCase().compareTo(d2.getDomainName().toLowerCase());
            }else if(d1.getCourseSourceOptionType() == CourseSourceOption.MY_COURSES){
                // d1 is my courses which has highest precedence
                return -1;
            }else if(d2.getCourseSourceOptionType() == CourseSourceOption.MY_COURSES){
                // d2 is my courses which has highest precedence
                return 1;
            }else if(d1.getCourseSourceOptionType() == CourseSourceOption.SHARED_COURSES){
                // d1 is shared with me which is 2nd highest precedence
                return -1;
            }else if(d2.getCourseSourceOptionType() == CourseSourceOption.SHARED_COURSES){
                // d2 is shared with me which is 2nd highest precedence
                return 1;
            }
            
            // no ordering between d1 and d2
            return 0;
        }
        
    }
    
    /**
     * Called when the widget is detached from the dom (like when transitioning to a new screen).
     * This gives the ability to do cleanup on the widget (or child widgets) so things like
     * the repeatable timer can be cancelled properly.
     */
    @Override
    protected void onDetach() {
        logger.info("onDetach() called");
        
        
        cleanupWidget();
        
        
        // Calling on parent onDetach per gwt docs.  This must be called to prevent other bugs/memory leaks.
        super.onDetach();
        
    }
    
    
    /** 
     * Performs necessary cleanup for the widget.
     */
    private void cleanupWidget() {
        if (pollCourseLoadTimer != null) {
            logger.info("Stopping the pollCourseLoadTimer.");
            pollCourseLoadTimer.cancel();
            pollCourseLoadTimer = null;
        }
        
        // Kill any pending operations of any course widgets
        cancelPendingRpcs();        
    }
    
    @Override
    public void onPreDetach() {
        logger.info("onPreDetach() called");
        
        cleanupWidget();
    }
    
    /**
     * Remove the course widget from the list of course widgets.
     * 
     * @param courseToRemove the course widget to remove
     */
    public void removeCourse(BsCourseWidget courseToRemove){

        if(courseToRemove == null){
            return;
        }
        
        rawCourseMap.remove(courseToRemove.getCourseData());
        visibleCourseWidgets.remove(courseToRemove);
        ctrlCourseList.remove(courseToRemove);
        displayedCourseList.remove(courseToRemove.getCourseData());

    }
    
    /**
     * Updates the user interface with the latest list of courses from the server
     * 
     * @param callback - used to notify the caller on whether the course list was successfully refreshed.</br>
     * onFailure - a failure message will be logged and shown to the user in this method already, no need to show it again.
     * onSuccess - true will be returned if the server returned true for retrieving the course list and the course list was refreshed.
     *             false will be returned if the server returned false for retrieving the course list and the course list was NOT refreshed.
     *             False normally means a dialog or message was shown to the user and therefore the callback shouldn't show a message as well.
     */
    public void refreshCourseList(final AsyncCallback<Boolean> callback){
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("refreshCourseList("+(callback == null ? "no callback" : "callback")+")");
        }
        
        ctrlCourseMessage.setVisible(false);
        ctrlSourceOptions.setEnabled(false);
        ctrlCourseList.setVisible(false);
        ctrlLoadPanel.setVisible(true);
//        ctrlLoadIcon.startLoading();
        
        //set to 0 percent so the next time the dialog is shown
        //the progress bar doesn't go from 100 to 0 when visible
        takeACourseOverallProgress.updateProgress(READY_TASK_LABEL, NO_PERCENT);  
        
        courseListProgressDialogHeading.setText("Retrieving courses...");
        
        dashboardService.getCourseList(UiManager.getInstance().getSessionId(), checkCourseNames, false, courseListFilter, new AsyncCallback<GetCourseListResponse>() {

            @Override
            public void onFailure(Throwable t) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Rpc throwable exception was caught: " + t.getMessage());
                }
                
                shouldPollForCourseListProgress = false;

                UiManager.getInstance().displayErrorDialog(ERROR_FETCH_COURSES, ERROR_THROWABLE_MESSAGE, null);
                ctrlCourseMessage.setText(createNoCoursesMessage());
                ctrlCourseMessage.setVisible(true);
                
                updateSubHeaderControls(true);
                
                ctrlLoadPanel.setVisible(false);
//                ctrlLoadIcon.stopLoading();
                ctrlCourseList.setVisible(true);
                
                if(callback != null){
                    callback.onFailure(t);
                }
            }

            @Override
            public void onSuccess(final GetCourseListResponse courseListResult) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("onSuccess(" + courseListResult + ")");
                }

                shouldPollForCourseListProgress = false;
                courseListProgressDialog.hide();
                
                checkCourseNames = false;
                
                Boolean returnVal = Boolean.TRUE;
                
                if (courseListResult != null && courseListResult.isSuccess()) {
                    logger.fine("result is success");
                    
                    if (courseListResult.getCourseList() != null && !courseListResult.getCourseList().isEmpty()) {
                                      
                        logger.fine("getCourseList rpc returned " + courseListResult.getCourseList().size() + " courses.");
                        
                        // Create all the course widgets based on the raw list.
                        createCourseWidgets(courseListResult.getCourseList(), true, false);
                        
                        logger.fine("finished created course widgets");
                        
                        filterSearchCourses();
                        
                        logger.fine("finished applying course filer");
                        
                        // Enable the filter option list box (only if there is a list of courses to filter on).
                        ctrlFilterOptions.setEnabled(true);

                        if(courseListResult.hasInvalidPaths()) {
                            if (logger.isLoggable(Level.FINE)) {
                                logger.fine("Course has invalid paths. Correcting paths...");
                            }

                            BsLoadingDialogBox.display("Correcting Course Paths", "There are courses in your workspace with naming conventions "
                                    + "that are no longer supported. Please wait while your courses are updated.");
                            
                            dashboardService.correctCoursePaths(UiManager.getInstance().getSessionId(), courseListResult.getCourseList(), new AsyncCallback<CorrectCoursePathsResult> (){

                                @Override
                                public void onFailure(Throwable caught) {
                                    if (logger.isLoggable(Level.FINE)) {
                                        logger.fine("correctCoursePaths.onFailure()");
                                    }
                                    
                                    BsLoadingDialogBox.remove();
                                    
                                    UiManager.getInstance().displayDetailedErrorDialog(
                                            "Course Path Correction Failed",
                                            "Failed to correct course paths. An error ocurred on the server.", 
                                            caught.toString(),
                                            DetailedException.getFullStackTrace(caught),
                                            null,
                                            new ModalDialogCallback() {

                                                @Override
                                                public void onClose() {
                                                    displayUpconvertedFiles(courseListResult);
                                                }
                                            }
                                    );
                                }

                                @Override
                                public void onSuccess(CorrectCoursePathsResult correctPathsResult) {
                                    if (logger.isLoggable(Level.FINE)) {
                                        logger.fine("correctCoursePaths.onSuccess()");
                                    }
                                    
                                    BsLoadingDialogBox.remove();
                                    
                                    /* Save the reference to this courseListResult to display upconverted
                                     * files later since we are going to make another call to refreshCourseList */
                                    upConvertedCourses = courseListResult;
                                    
                                    if(correctPathsResult.hasSummary()){
                                        
                                        UiManager.getInstance().displayInfoDialog(
                                                "Corrected Course Paths", 
                                                correctPathsResult.getSummary(), new DialogCallback() {
                                    
                                                    @Override
                                                    public void onAccept() {
                                                        rawCourseMap.clear();
                                                        refreshCourseList(callback);
                                                    }
                                                });
                                    } else {
                                        rawCourseMap.clear();
                                        refreshCourseList(callback);
                                    }

                                }
                            });
                        } else {
                            // if all the courses are valid, display the ones that were upconverted, if any
                            displayUpconvertedFiles(upConvertedCourses == null ? courseListResult : upConvertedCourses);
                        }
                        
                    } else {
                        ctrlCourseList.clear();
                        ctrlCourseMessage.setText(createNoCoursesMessage());
                        ctrlCourseMessage.setVisible(true);
                    }
                    
                } else {
                    
                    returnVal = Boolean.FALSE;
                                        
                    if(courseListResult != null) {
                        
                        logger.fine("Rpc result for getCourseList is a failure - " + courseListResult.getAdditionalInformation());
                        
                        if(courseListResult.getUserSessionId() == null){
                            UiManager.getInstance().displayScreen(ScreenEnum.LOGIN);
                            UiManager.getInstance().displayInfoDialog("Login Again", courseListResult.getResponse() + "\nHave you been idle for a long time?");
                        }else if(Dashboard.getInstance().getServerProperties().getDeploymentMode() == DeploymentModeEnum.SERVER){
                            //display generic no courses server message mentioning content management system

                            UiManager.getInstance().displayErrorDialog(ERROR_FETCH_COURSES, courseListResult.getResponse(), null);
                            ctrlCourseMessage.setText(NO_COURSE_SERVER_MESSAGE);
                            ctrlCourseMessage.setVisible(true);
                        }else{
                            //in desktop mode, not an expired user usersession
                            
                            UiManager.getInstance().displayErrorDialog(ERROR_FETCH_COURSES, courseListResult.getResponse(), null);
                            ctrlCourseMessage.setText(createNoCoursesMessage());
                            ctrlCourseMessage.setVisible(true);
                        }
                         
                    } else {
                        
                        logger.fine("Rpc result is failure - no additional information available");

                        UiManager.getInstance().displayErrorDialog(ERROR_FETCH_COURSES, ERROR_RPC_FAILURE, null);
                        ctrlCourseMessage.setText(createNoCoursesMessage());
                        ctrlCourseMessage.setVisible(true);
                    }
                }
                
                updateSubHeaderControls(true);
                
                ctrlLoadPanel.setVisible(false);
//                ctrlLoadIcon.stopLoading();
                ctrlCourseList.setVisible(true);
                
                if(callback != null){
                    callback.onSuccess(returnVal);
                }
            }
        });
        
        courseListProgressDialog.show();
        
        shouldPollForCourseListProgress = true;
        
        scheduleGetCourseListProgressTimer();
    }
    
    /**
     * Display the files that were upconverted in a dialog to the user
     * 
     * @param courseListResult - the result of the getCourseList that contains the upconverted files.
     * Can't be null.
     */
    private void displayUpconvertedFiles(GetCourseListResponse courseListResult) {
        
        if (courseListResult == null) {
            throw new IllegalArgumentException("The parameter 'courseListResult' cannot be null.");
        }

        // check if any courses were upconverted
        boolean hasUpconvertedCourses = !courseListResult.getUpconvertedCourses().isEmpty();

        if (hasUpconvertedCourses) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("displayUpconvertedFiles()");
            }
            
            StringBuilder sb = new StringBuilder();
            sb.append("Some of the courses you've loaded were created with an old version of GIFT, ");
            sb.append("but we were able to update them for you. These courses have already been saved.");
            sb.append("<br><br>The following courses were updated:");
            sb.append("<br><ul>");

            // we only want to display the course name, not the file path.
            for (String updatedCoursePath : courseListResult.getUpconvertedCourses()) {
                String correctedCoursePath = FileTreeModel.correctFilePath(updatedCoursePath);
                for (DomainOption course : courseListResult.getCourseList()) {
                    if (correctedCoursePath.endsWith(course.getDomainId())) {
                        sb.append("<li>").append(course.getDomainName()).append("</li>");
                        break;
                    }
                }
            }

            sb.append("</ul>");
            UiManager.getInstance().displayInfoDialog("Updated Files", sb.toString());
        }
            
        // We don't need to save this any more now that we've displayed the list
        upConvertedCourses = null;
    }
    
    @Override
    public void onUnload() {
        logger.info("BsMyCoursesDashWidget onUnload() called");
        super.onUnload();
    }
    
    /**
     * Responsible for generating the String that should be displayed if 
     * there are no course widgets to show. The message is dynamically 
     * generated based on the current state of the courseListFilter so that 
     * the instructions given to the user for displaying courses to show 
     * are relevant.
     * @return the message to display. 
     */
    private String createNoCoursesMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("The list is empty because ");
        
        int filterOptionCount = courseListFilter.getCourseSourceOptions().size();
        Stringifier<CourseSourceOption> explanationStringifier = new Stringifier<CourseSourceOption>() {
            @Override
            public String stringify(CourseSourceOption option) {
                switch(option) {
                case MY_COURSES:
                    return "you have no personal courses";
                case SHARED_COURSES:
                    return "nothing has been shared with you";
                case SHOWCASE_COURSES:
                    return "the showcase is empty";
                }
                
                return "there are no courses available";
            }
        };
        
        if(filterOptionCount > 0){
            StringUtils.join(
                    ", ", 
                    courseListFilter.getCourseSourceOptions().iterator(), 
                    explanationStringifier, 
                    sb);
            
            if(filterOptionCount > 1) {            
                int lastComma = sb.lastIndexOf(",");
                sb.replace(lastComma, lastComma + 1, ", or");
            }
        }else{
            sb.append("there are no courses available");
        }
        
        sb.append(". Try ");
        
        Stringifier<CourseSourceOption> courseOfActionStringifier = new Stringifier<CourseSourceOption>() {
            
            @Override
            public String stringify(CourseSourceOption option) {
                switch(option) {
                case MY_COURSES:
                    return "creating a course of your own";
                case SHARED_COURSES:
                    return "having someone share a course with you";
                case SHOWCASE_COURSES:
                    return "have your system administrator add courses to the showcase courses folder";
                }
                
                return "creating a course of your own";
            }
        };
        
        if(filterOptionCount > 0){
            StringUtils.join(
                    ", ", 
                    courseListFilter.getCourseSourceOptions().iterator(),
                    courseOfActionStringifier,
                    sb);
            
            if(filterOptionCount > 1) {            
                int lastComma = sb.lastIndexOf(",");
                sb.replace(lastComma, lastComma + 1, ", or");
            }
        }else{
            sb.append("creating a course of your own");
        }
        
        sb.append(".");
        return sb.toString();
    }
}
