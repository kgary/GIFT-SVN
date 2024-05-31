/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap;

import static mil.arl.gift.common.util.StringUtils.isNotBlank;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.RadioButton;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.html.Paragraph;
import org.gwtbootstrap3.client.ui.html.Text;
import org.gwtbootstrap3.extras.select.client.ui.OptGroup;
import org.gwtbootstrap3.extras.select.client.ui.Option;
import org.gwtbootstrap3.extras.select.client.ui.Select;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SingleSelectionModel;

import mil.arl.gift.common.DomainOption;
import mil.arl.gift.common.enums.DeploymentModeEnum;
import mil.arl.gift.common.experiment.CourseCollection;
import mil.arl.gift.common.experiment.DataCollectionItem;
import mil.arl.gift.common.experiment.DataCollectionItem.DataCollectionPermission;
import mil.arl.gift.common.experiment.ExperimentUtil.DataCollectionUserRole;
import mil.arl.gift.common.experiment.ExperimentUtil.DataSetType;
import mil.arl.gift.common.experiment.ExperimentUtil.ExperimentStatus;
import mil.arl.gift.common.gwt.client.DetailedRpcResponse;
import mil.arl.gift.common.gwt.client.GenericRpcResponse;
import mil.arl.gift.common.gwt.client.RpcResponse;
import mil.arl.gift.common.gwt.client.widgets.DynamicHeaderScrollPanel;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.AbstractBsWidget;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.BsLoadingIcon;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.HelpLink;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ManagedTooltip;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ProgressBarListEntry;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.io.DownloadableFileRef;
import mil.arl.gift.common.io.ProgressIndicator;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.dashboard.client.Dashboard;
import mil.arl.gift.tools.dashboard.client.DashboardService;
import mil.arl.gift.tools.dashboard.client.DashboardServiceAsync;
import mil.arl.gift.tools.dashboard.client.UiManager;
import mil.arl.gift.tools.dashboard.client.bootstrap.BsDialogConfirmWidget.ConfirmationDialogCallback;
import mil.arl.gift.tools.dashboard.client.bootstrap.BsDialogWidget.DialogCallback;
import mil.arl.gift.tools.dashboard.client.bootstrap.BsDialogWidget.DialogType;
import mil.arl.gift.tools.dashboard.client.bootstrap.ExperimentToolWidget.CourseEntry.COURSE_STATE;
import mil.arl.gift.tools.dashboard.shared.rpcs.ExperimentListResponse;
import mil.arl.gift.tools.dashboard.shared.rpcs.ExperimentResponse;
import mil.arl.gift.tools.dashboard.shared.rpcs.ExportResponse;
import mil.arl.gift.tools.dashboard.shared.rpcs.GetCourseListResponse;
import mil.arl.gift.tools.dashboard.shared.rpcs.ProgressResponse;
import mil.arl.gift.tools.dashboard.shared.rpcs.coursecollection.AddCourseAction;

/**
 * A widget that allows users to create new published courses, view all of their existing published courses, and edit their published courses
 *
 * @author nroberts
 */
public class ExperimentToolWidget extends AbstractBsWidget  {

    /** The logger for the class */
    private static Logger logger = Logger.getLogger(ExperimentToolWidget.class.getName());

    private static ExperimentToolWidgetUiBinder uiBinder = GWT
            .create(ExperimentToolWidgetUiBinder.class);

    private final String NO_COURSE_MESSAGE = "There are no courses to display.  Please add some courses or create a new one.";
    
    /** message to show on the panel when there are no published courses available to the user (AND not filters are applied) */
    private final String NO_PUBLISHED_COURSES_NO_FILTER_MESSAGE = "There are no published courses to display.  Please create a new one.";
    
    /** message to show on the panel when there are no published courses available to the user AND a filter is being applied */
    private final String NO_PUBLISHED_COURSES_WITH_FILTER_MESSAGE = "With the search term and/or filter applied, there are no published courses to display.";

    private final String ERROR_FETCH_COURSES = "Error Retrieving Courses";
    private final String ERROR_THROWABLE_MESSAGE = "A throwable error was caught trying to fetch the course list. Courses will not be displayed.";
    private final String ERROR_RPC_FAILURE = "An rpc error occurred getting the course listing.  Courses will not be displayed.";

    private final String ERROR_FETCH_EXPERIMENTS = "Error Retrieving Published Courses";
    private final String ERROR_EXPERIMENTS_THROWABLE_MESSAGE = "A throwable error was caught trying to fetch the published courses list. Published courses will not be displayed.";
    private final String ERROR_EXPERIMENTS_RPC_FAILURE = "An rpc error occurred getting the published courses listing.  Published courses will not be displayed.";

    /** The label to display to the user when creating an Experiment or LTI */
    private final String EXPERIMENT_NAME_LABEL = "Published Course Name:";

    /** The label to display to the user when creating a Collection */
    private final String COLLECTION_NAME_LABEL = "Published Collection Name:";

    interface ExperimentToolWidgetUiBinder extends
            UiBinder<Widget, ExperimentToolWidget> {
    }

    /**
     * Create a remote service proxy to talk to the server-side dashboard service.
     */
    private final DashboardServiceAsync dashboardService = GWT
            .create(DashboardService.class);
    
    @UiField
    protected Widget mainContainer;

    @UiField
    protected Modal createDialog;

    @UiField
    protected DynamicHeaderScrollPanel createDialogScrollPanel;

    @UiField
    protected Button createButton;

    @UiField
    protected FocusPanel createExperimentPanel;

    @UiField
    protected Button createCourseButton;

    @UiField
    protected FocusPanel createCoursePanel;

    @UiField
    protected Paragraph ctrlLoadPanel;

    @UiField
    protected BsLoadingIcon ctrlLoadIcon;

    @UiField
    protected Heading ctrlCourseMessage;

    @UiField
    protected Button cancelCreateButton;

    @UiField
    protected Button createExperimentButton;

    @UiField
    protected FlowPanel parentCollectionPanel;

    @UiField
    protected Label parentCollectionLabel;

    @UiField
    protected HTML experimentNameLabel;

    @UiField
    protected TextBox createNameTextBox;

    /** tooltip attached to the create name textbox */
    @UiField
    protected ManagedTooltip createNameTextBoxTooltip;

    @UiField
    protected TextArea createDescriptionTextBox;

    @UiField
    protected Modal createProgressDialog;

    @UiField
    protected Text createProgressDialogHeading;

    @UiField
    protected ProgressBarListEntry createOverallProgress;

    @UiField
    protected Button cancelCreateProgressButton;

    @UiField
    protected Button cancelExportCourseProgressButton;

    @UiField
    protected Button cancelExportRawDataProgressButton;

    @UiField
    protected Heading ctrlExperimentMessage;

    @UiField
    protected Paragraph ctrlLoadExperimentPanel;

    @UiField
    protected BsLoadingIcon ctrlLoadExperimentIcon;

    @UiField
    protected FlowPanel experimentListPanel;


    @UiField
    protected Modal deleteProgressDialog;

    @UiField
    protected Text deleteProgressDialogHeading;

    @UiField
    protected ProgressBarListEntry deleteOverallProgress;


    @UiField
    protected Modal exportCourseProgressDialog;

    @UiField
    protected Text exportCourseProgressDialogHeading;

    @UiField
    protected ProgressBarListEntry exportCourseOverallProgress;

    @UiField
    protected ProgressBarListEntry exportCourseSubtaskProgress;


    @UiField
    protected Modal exportRawDataProgressDialog;

    @UiField
    protected Text exportRawDataProgressDialogHeading;

    @UiField
    protected ProgressBarListEntry exportRawDataOverallProgress;

    @UiField
    protected ProgressBarListEntry exportRawDataSubtaskProgress;


    @UiField
    protected Modal editDialog;

    @UiField
    protected TextBox editNameTextBox;

    /** tooltip attached to the editNameTextBox */
    @UiField
    protected ManagedTooltip editNameTextBoxTooltip;

    @UiField
    protected TextArea editDescriptionTextBox;

    @UiField
    protected Button saveEditButton;


    @UiField
    protected Modal startExportCourseDialog;

    @UiField
    protected ExperimentCourseExportSummaryWidget startCourseExportSummary;

    @UiField
    protected Button confirmStartCourseExportButton;
    
    
    /**
     * Used to convert binary data to human readable format
     * under published courses.
     */
    @UiField
    protected CheckBox convertLogCheckBox;

    @UiField
    protected Modal startExportRawDataDialog;

    @UiField
    protected ExperimentRawDataExportSummaryWidget startRawDataExportSummary;

    @UiField
    protected Button confirmStartRawDataExportButton;

    @UiField
    protected ExperimentBuildReportDialogWidget reportDialog;

    /** The {@link RadioButton} used to select {@link DataSetType#EXPERIMENT} */
    @UiField
    protected RadioButton experimentTypeButton;

    /** The {@link RadioButton} used to select {@link DataSetType#COLLECTION} */
    @UiField
    protected RadioButton collectionTypeButton;

    /** The {@link RadioButton} used to select {@link DataSetType#LTI} */
    @UiField
    protected RadioButton ltiTypeButton;

    @UiField
    protected HTMLPanel experimentNote;

    @UiField
    protected HTMLPanel collectionNote;

    @UiField
    protected HTMLPanel ltiNote;

    /**
     * The panel that contains the controls used to search the available
     * courses.
     */
    @UiField
    protected FlowPanel searchPanel;

    /**
     * The textfield used to search for courses and filter the viewed courses when creating a new published course
     */
    @UiField
    protected org.gwtbootstrap3.client.ui.TextBox courseSearchBox;

    /**
     * The button to execute the search of courses
     */
    @UiField
    protected Button courseSearchButton;
    
    /**
     * published courses sort by select widget
     */
    @UiField
    protected Select ctrlSortOptions;
    
    /**
     * published courses filter by select widget
     */
    @UiField
    protected Select ctrlFilterOptions;
    
    /**
     * the textfield used to search for published courses and filter the viewed published course
     */
    @UiField
    protected org.gwtbootstrap3.client.ui.TextBox publishedCoursesSearchBox;

    /**
     * the button to execute the search of published courses
     */
    @UiField
    protected Button publishedCoursesSearchButton;

    /** The panel that contains the {@link #userCourseTable}. */
    @UiField
    protected FlowPanel courseListPanel;

    @UiField
    protected HelpLink helpLink;
    
    /**
     * the help button for the published courses search field
     */
    @UiField
    protected HelpLink publishedCoursesSearchHelpLink;

    /** The un-filtered list of user courses requested from the server */
    private List<CourseEntry> userCourses = new ArrayList<CourseEntry>();

    /**
     * TODO: update comment when ID changes to type
     * The table used to display the courses that are retrieved from the server
     * Has columns for the name of the course
     */
    @UiField(provided = true)
    protected CellTable<CourseEntry> userCourseTable = new CellTable<CourseEntry>();

    /**
     * Used to select only 1 course at a time from the list
     */
    private SingleSelectionModel<CourseEntry> courseTableSelectionModel = new SingleSelectionModel<CourseEntry>();

    /**
     * The parent collection widget of the newly published course. Will be null
     * if the published course has no parent collection.
     */
    private CourseCollectionWidget parentCollectionWidget = null;

    /** the one and only selected course */
    private CourseEntry selectedCourse = null;
    
    /** the full set of published course widgets created on the first call to the server */
    private List<Widget> publishedCoursesRetrievedList = new ArrayList<>();

   /** A regular expression used to locate words and phrases in a search text expression */
   private static final String wordExpression =
       "-?\"[^\"]*\"" +    //double quotes around phrases(s)
       "|-?[A-Za-z0-9']+"  //single word
   ;

   /** A regular expression used to locate binary operators in a search text expression */
   private static final String binaryOperatorExpression = "(" + wordExpression + ")(\\s+(AND|OR)\\s+(" + wordExpression + "))+";

    /** The text shown to the user when no courses are found on the server */
    private static final String NO_USER_COURSES_TEXT = "<span style='font-size: 12pt;'>"
            +   "You don't have access to any courses yet.  Create or Import a course in order to publish a course."
            + "</span>";

    /** The text shown to the user when no courses match their entered search query */
    private static final String NO_SEARCH_MATCHES_TEXT = "<span style='font-size: 12pt;'>"
            +   "No courses matching the given search query were found."
            + "</span>";

    /** The widget shown when the user course table is empty */
    private HTML userCourseEmptyTableWidget = new HTML(""
            + NO_USER_COURSES_TEXT);

    /**
     * Comparator for the course name column of the course table
     */
    private Comparator<CourseEntry> nameComparator = new Comparator<CourseEntry>() {

        @Override
        public int compare(CourseEntry o1, CourseEntry o2) {

            if(o1 == null && o2 == null){
                return 0;

            } else if(o1 == null){
                return -1;

            } else if(o2 == null){
                return 1;
            }

            return o1.getCourse().getDomainName().compareToIgnoreCase(o2.getCourse().getDomainName());
        }
    };

    /**
     * Comparator for the course workspace column of the course table
     */
    private Comparator<CourseEntry> workspaceComparator = new Comparator<CourseEntry>() {

        @Override
        public int compare(CourseEntry o1, CourseEntry o2) {

            if(o1 == null && o2 == null){
                return 0;

            } else if(o1 == null){
                return -1;

            } else if(o2 == null){
                return 1;
            }

            String workspace1;
            String path1 = o1.getCourse().getDomainId();
            int index1 = path1.indexOf(Constants.FORWARD_SLASH);
            if(index1 != -1){
                workspace1 = path1.substring(0, index1);
            }else{
                return -1;
            }

            String workspace2;
            String path2 = o2.getCourse().getDomainId();
            int index2 = path2.indexOf(Constants.FORWARD_SLASH);
            if(index2 != -1){
                workspace2 = path2.substring(0, index2);
            }else{
                return 1;
            }

            return workspace1.compareToIgnoreCase(workspace2);
        }
    };

    /**
     * Provides the data to the course list
     */
    private ListDataProvider<CourseEntry> userCoursesTableDataProvider = new ListDataProvider<CourseEntry>();

    /**
     * The course name column of the course table
     */
    private Column<CourseEntry, String> nameColumn = new Column<CourseEntry,String>(new TextCell()){

        @Override
        public String getValue(CourseEntry object) {

            if(object == null){
                return "";

            } else {
                return object.getCourse().getDomainName();

            }
        }

    };

    /**
     * The course workspace column of the course table
     */
    private Column<CourseEntry, String> workspaceColumn = new Column<CourseEntry,String>(new TextCell()){

        @Override
        public String getValue(CourseEntry object) {

            if(object == null){
                return "";

            } else {

                String path = object.getCourse().getDomainId();
                int index = path.indexOf(Constants.FORWARD_SLASH);
                if(index != -1){
                    return path.substring(0, index);
                }else{
                    return "unknown";
                }

            }
        }

    };

    /**
     * The status column of the course table to show whether the course can be selected
     */
    protected Column<CourseEntry, SafeHtml> statusColumn = new Column<CourseEntry, SafeHtml>(new SafeHtmlCell()){

        @Override
        public SafeHtml getValue(CourseEntry courseEntry) {

            if(courseEntry == null){
                return SafeHtmlUtils.fromTrustedString("");
            }

            Icon icon;
            switch(courseEntry.getState()){
            case READ_ONLY_LOCKED:
                icon = new Icon(IconType.LOCK);
                icon.setTitle("Read Only (try publishing a copy of this course)");
                icon.getElement().getStyle().setCursor(Cursor.POINTER);
                icon.getElement().getStyle().setMargin(-10, Unit.PX);
                icon.getElement().getStyle().setFontSize(20, Unit.PX);
                return SafeHtmlUtils.fromTrustedString(icon.toString());
            case UNAVAILABLE:
                Image image = new Image("images/Unavailable.png");
                image.setTitle("Unavailable (Check course for issues)");
                image.getElement().getStyle().setCursor(Cursor.POINTER);
                image.getElement().getStyle().setMargin(-10, Unit.PX);
                image.getElement().getStyle().setFontSize(20, Unit.PX);
                return SafeHtmlUtils.fromTrustedString(image.toString());
            default:
                return SafeHtmlUtils.fromTrustedString("");
            }
       }

    };

    private boolean cancelledCreate = false;

    private boolean shouldPollForCreateProgress;

    private boolean shouldPollForDeleteProgress;


    private boolean cancelledExportCourse = false;

    private boolean shouldPollForExportCourseProgress;


    private boolean cancelledExportRawData = false;

    private boolean shouldPollForExportRawDataProgress;

    /** Command that should be executed when starting to export an experiment course*/
    private Command startExportExperimentCourseCommand = null;

    /** Command that should be executed when starting to export raw subject data*/
    private Command startExportExperimentRawDataCommand = null;

    /** Command that should be executed when saving experiment edits*/
    private Command saveExperimentEditsCommand = null;
    
    /**
     * The list of options for sorting published courses.  
     */
    public enum PublishedCourseSortEnum {
        SORT_NAME_ALPHABETICAL("Alphabetical"),
        SORT_NAME_REVERSE_ALPHABETICAL("Reverse Alphabetical"),
        SORT_ATTEMPTS_DESCENDING("Most Attempts"),
        SORT_ATTEMPTS_ASCENDING("Least Attempts"),
        SORT_LAST_ATTEMPT_DESCENDING("Recent Attempt"),
        SORT_LAST_ATTEMPT_ASCENDING("Oldest Attempt");
        
        private String displayName = null;
        
        private PublishedCourseSortEnum(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    /** the current published course sort choice */
    PublishedCourseSortEnum selectedSort = PublishedCourseSortEnum.SORT_NAME_ALPHABETICAL;
    
    /**
     * The list of options for filtering published courses.  
     */
    public enum PublishedCourseFilterEnum {
        FILTER_COURSE_TILE("Course Tile"),
        FILTER_EXPERIMENT("Experiment"),
        FILTER_LTI("LTI"),
        FLITER_ACTIVE("Active"),
        FILTER_PAUSED("Paused"),
        FILTER_NONE("None"),
        FILTER_SHARED_WITH_ME("Share with me"),
        FILTER_SHARED_WITH_OTHERS("Shared with others");
        
        private String displayName = null;
        
        private PublishedCourseFilterEnum(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    /** the current published course filter choice */
    PublishedCourseFilterEnum selectedFilter = PublishedCourseFilterEnum.FILTER_NONE;

    /**
     * cached request of the course to use to show the published courses that reference that course
     * when going to the publish course page from another page.  The publish course list is requested
     * from the server asynchronously. 
     */
    private DomainOption courseToExpose = null;
    
    /**
     * cached request of the data set type to ignore when wanting to automatically show published courses
     * that reference a specific course (see {@link #courseToExpose}).
     */
    private DataSetType courseDataSetTypeExposeFilter = null;

    /**
     * Creates a new experiment tool widget and automatically fetches the list of experiments
     */
    public ExperimentToolWidget() {

        initWidget(uiBinder.createAndBindUi(this));

        //don't allow the publish course button to be available until the appropriate inputs are provided
        experimentTypeButton.setActive(true);  // default to experiment type, do this before calling shouldEnablePublishCourseButton()
        createExperimentButton.setEnabled(shouldEnablePublishCourseButton());

        UiManager.getInstance().fillToBottomOfViewport(mainContainer);

        userCoursesTableDataProvider.addDataDisplay(userCourseTable);

        userCourseTable.setPageSize(Integer.MAX_VALUE);

        userCourseTable.addColumn(nameColumn, SafeHtmlUtils.fromTrustedString(
                "<p style=\"padding-left:5px;\"> Course Name"
                + "<i class='fa fa-sort' style='margin-left: 5px;'/> </p>"
        ));
        userCourseTable.setColumnWidth(nameColumn, "100%");

        userCourseTable.addColumn(workspaceColumn, SafeHtmlUtils.fromTrustedString(
                "<p style=\"padding-left:5px;\"> Workspace"
                + "<i class='fa fa-sort' style='margin-left: 5px;'/> </p>"
        ));
        userCourseTable.setColumnWidth(workspaceColumn, "200px");

        statusColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        userCourseTable.addColumn(statusColumn);
        userCourseTable.setColumnWidth(statusColumn, "100px");

        userCourseTable.setPageSize(Integer.MAX_VALUE);

        userCourseTable.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.DISABLED);

        userCourseTable.setSelectionModel(courseTableSelectionModel);
        userCourseTable.setEmptyTableWidget(userCourseEmptyTableWidget);

        ListHandler<CourseEntry> userCoursesSortHandler = new ListHandler<>(userCoursesTableDataProvider.getList());
        userCoursesSortHandler.setComparator(nameColumn, nameComparator);
        userCoursesSortHandler.setComparator(workspaceColumn, workspaceComparator);
        userCourseTable.addColumnSortHandler(userCoursesSortHandler);
        
        // Build the sort options
        // by name
        OptGroup nameSortOptionGroup = new OptGroup();
        nameSortOptionGroup.setLabel("Name");
        Option alphabeticalOption = new Option();
        alphabeticalOption.setText(PublishedCourseSortEnum.SORT_NAME_ALPHABETICAL.getDisplayName());
        alphabeticalOption.setValue(PublishedCourseSortEnum.SORT_NAME_ALPHABETICAL.name());
        alphabeticalOption.setSelected(true);
        nameSortOptionGroup.add(alphabeticalOption);
        Option reverseAlphabeticalOption = new Option();
        reverseAlphabeticalOption.setText(PublishedCourseSortEnum.SORT_NAME_REVERSE_ALPHABETICAL.getDisplayName());
        reverseAlphabeticalOption.setValue(PublishedCourseSortEnum.SORT_NAME_REVERSE_ALPHABETICAL.name());
        nameSortOptionGroup.add(reverseAlphabeticalOption);
        ctrlSortOptions.add(nameSortOptionGroup);
        // by attempts count
        OptGroup attemptsSortOptionGroup = new OptGroup();
        attemptsSortOptionGroup.setLabel("Number of Attempts");
        Option attemptsDescendingOption = new Option();
        attemptsDescendingOption.setText(PublishedCourseSortEnum.SORT_ATTEMPTS_DESCENDING.getDisplayName());
        attemptsDescendingOption.setValue(PublishedCourseSortEnum.SORT_ATTEMPTS_DESCENDING.name());
        attemptsSortOptionGroup.add(attemptsDescendingOption);
        Option attemptsAscendingOption = new Option();
        attemptsAscendingOption.setText(PublishedCourseSortEnum.SORT_ATTEMPTS_ASCENDING.getDisplayName());
        attemptsAscendingOption.setValue(PublishedCourseSortEnum.SORT_ATTEMPTS_ASCENDING.name());
        attemptsSortOptionGroup.add(attemptsAscendingOption);
        ctrlSortOptions.add(attemptsSortOptionGroup);
        // by last attempt
        OptGroup lastAttemptsSortOptionGroup = new OptGroup();
        lastAttemptsSortOptionGroup.setLabel("Last Attempt");
        Option lastAttemptsDescendingOption = new Option();
        lastAttemptsDescendingOption.setText(PublishedCourseSortEnum.SORT_LAST_ATTEMPT_DESCENDING.getDisplayName());
        lastAttemptsDescendingOption.setValue(PublishedCourseSortEnum.SORT_LAST_ATTEMPT_DESCENDING.name());
        lastAttemptsSortOptionGroup.add(lastAttemptsDescendingOption);
        Option lastAttemptsAscendingOption = new Option();
        lastAttemptsAscendingOption.setText(PublishedCourseSortEnum.SORT_LAST_ATTEMPT_ASCENDING.getDisplayName());
        lastAttemptsAscendingOption.setValue(PublishedCourseSortEnum.SORT_LAST_ATTEMPT_ASCENDING.name());
        lastAttemptsSortOptionGroup.add(lastAttemptsAscendingOption);
        ctrlSortOptions.add(lastAttemptsSortOptionGroup);
        
        ctrlSortOptions.refresh();
        
        ctrlSortOptions.addValueChangeHandler(new ValueChangeHandler<String>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                logger.info("Changing published courses sort type to "+event.getValue());
                selectedSort = PublishedCourseSortEnum.valueOf(event.getValue());
                applyPublishedCourseSort();  // only call sort, not removing any entries with filters
            }
        });
        
        // Build the filter options
        // by none
        Option noneOption = new Option();
        noneOption.setText(PublishedCourseFilterEnum.FILTER_NONE.getDisplayName());
        noneOption.setValue(PublishedCourseFilterEnum.FILTER_NONE.name());
        noneOption.setSelected(true);
        ctrlFilterOptions.add(noneOption);
        // by type
        OptGroup typeFilterOptionGroup = new OptGroup();
        typeFilterOptionGroup.setLabel("Type");
        Option courseTileOption = new Option();
        courseTileOption.setText(PublishedCourseFilterEnum.FILTER_COURSE_TILE.getDisplayName());
        courseTileOption.setValue(PublishedCourseFilterEnum.FILTER_COURSE_TILE.name());
        typeFilterOptionGroup.add(courseTileOption);
        Option experimentOption = new Option();
        experimentOption.setText(PublishedCourseFilterEnum.FILTER_EXPERIMENT.getDisplayName());
        experimentOption.setValue(PublishedCourseFilterEnum.FILTER_EXPERIMENT.name());
        typeFilterOptionGroup.add(experimentOption);
        Option ltiOption = new Option();
        ltiOption.setText(PublishedCourseFilterEnum.FILTER_LTI.getDisplayName());
        ltiOption.setValue(PublishedCourseFilterEnum.FILTER_LTI.name());
        typeFilterOptionGroup.add(ltiOption);
        ctrlFilterOptions.add(typeFilterOptionGroup);
        // by shared
        OptGroup sharedGroup = new OptGroup();
        sharedGroup.setLabel("Sharing");
        Option sharedWithMeOption = new Option();
        sharedWithMeOption.setText(PublishedCourseFilterEnum.FILTER_SHARED_WITH_ME.getDisplayName());
        sharedWithMeOption.setValue(PublishedCourseFilterEnum.FILTER_SHARED_WITH_ME.name());
        sharedGroup.add(sharedWithMeOption);
        Option sharedOption = new Option();
        sharedOption.setText(PublishedCourseFilterEnum.FILTER_SHARED_WITH_OTHERS.getDisplayName());
        sharedOption.setValue(PublishedCourseFilterEnum.FILTER_SHARED_WITH_OTHERS.name());
        sharedGroup.add(sharedOption);
        ctrlFilterOptions.add(sharedGroup);
        // by status
        OptGroup statusGroup = new OptGroup();
        statusGroup.setLabel("Status");
        Option activeOption = new Option();
        activeOption.setText(PublishedCourseFilterEnum.FLITER_ACTIVE.getDisplayName());
        activeOption.setValue(PublishedCourseFilterEnum.FLITER_ACTIVE.name());
        statusGroup.add(activeOption);
        Option pausedOption = new Option();
        pausedOption.setText(PublishedCourseFilterEnum.FILTER_PAUSED.getDisplayName());
        pausedOption.setValue(PublishedCourseFilterEnum.FILTER_PAUSED.name());
        statusGroup.add(pausedOption);
        ctrlFilterOptions.add(statusGroup);
        
        ctrlFilterOptions.refresh();
        
        ctrlFilterOptions.addValueChangeHandler(new ValueChangeHandler<String>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                logger.info("Changing published courses filter type to "+event.getValue());
                selectedFilter = PublishedCourseFilterEnum.valueOf(event.getValue());
                filterSearchSortPublishedCourses();
            }
        });

        courseTableSelectionModel.addSelectionChangeHandler(new Handler(){

            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                selectedCourse = courseTableSelectionModel.getSelectedObject();

                //enable publishing if all required inputs are provided
                createExperimentButton.setEnabled(shouldEnablePublishCourseButton());
            }

        });

        nameColumn.setSortable(true);
        workspaceColumn.setSortable(true);
        statusColumn.setSortable(false);

        experimentNameLabel.setText(EXPERIMENT_NAME_LABEL);
        experimentTypeButton.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("experimentTypeButton.onValueChange(" + event.toDebugString() + ")");
                }

                if (event.getValue()) {
                    handleDataSetTypeChange(DataSetType.EXPERIMENT);
                }
            }

        });

        collectionTypeButton.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("collectionTypeButton.onValueChange(" + event.toDebugString() + ")");
                }

                if (event.getValue()) {
                    handleDataSetTypeChange(DataSetType.COLLECTION);
                }
            }

        });

        ltiTypeButton.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("ltiTypeButton.onValueChange(" + event.toDebugString() + ")");
                }

                if (event.getValue()) {
                    handleDataSetTypeChange(DataSetType.LTI);
                }
            }

        });

        createButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                event.stopPropagation();
                showCreateDialog(null);
            }
        });

        createExperimentPanel.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                createButton.click();
            }
        });

        createNameTextBox.addKeyUpHandler(new KeyUpHandler() {

            @Override
            public void onKeyUp(KeyUpEvent arg0) {

                createExperimentButton.setEnabled(shouldEnablePublishCourseButton());
            }
        });

        cancelCreateButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                hideCreateDialog();
            }
        });

        createExperimentButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {

                // get experiment details
                final String name = createNameTextBox.getValue();
                if(StringUtils.isBlank(name)){
                    UiManager.getInstance().displayErrorDialog("No Name Specified", "Please provide a name.", null);
                    createExperimentButton.setEnabled(false);
                    return;
                }

                final String description = createDescriptionTextBox.getValue();

                if (StringUtils.equals(getSelectedType(), DataSetType.COLLECTION.name())) {
                    createExperimentCollection(name, description);
                    /* TODO: CHUCK should call refresh instead of manually
                     * setting visibility here once collection ownership is a
                     * thing */
                    ctrlCourseMessage.setVisible(false);
                    experimentListPanel.setVisible(true);
                    return;
                }

                if(selectedCourse == null){
                    UiManager.getInstance().displayErrorDialog("No Course Selected", "At least one course must be selected.", null);
                    createExperimentButton.setEnabled(false);
                    return;
                }

                //create the experiment, showing a progress dialog while the creation takes place

                cancelCreateProgressButton.setEnabled(true);

                cancelledCreate = false;

                createExperimentButton.setEnabled(false);

                createProgressDialogHeading.setText("Publishing course...");
                createOverallProgress.updateProgress(new ProgressIndicator(0, "Initializing"));

                String dataSetType = getSelectedType();

                /* Add to parent collection if one exists */
                final String username = UiManager.getInstance().getUserName();
                if (parentCollectionWidget != null) {
                    final DataSetType dataSetTypeEnum = DataSetType.valueOf(dataSetType);
                    if (DataSetType.EXPERIMENT != dataSetTypeEnum) {
                        throw new UnsupportedOperationException(
                                "The type '" + dataSetType + "' is not supported by collections.");
                    }

                    DataCollectionItem newExperiment = new DataCollectionItem();
                    newExperiment.setName(name);
                    newExperiment.setDescription(description);
                    newExperiment.setDataSetType(dataSetTypeEnum);
                    newExperiment.setAuthorUsername(username);
                    newExperiment.setSourceCourseId(selectedCourse.getCourse().getDomainId());

                    final CourseCollection parentCollection = parentCollectionWidget.getCollection();
                    dashboardService.updateCourseCollection(new AddCourseAction(username,
                            parentCollection.getId(), newExperiment),
                            new AsyncCallback<GenericRpcResponse<DataCollectionItem>>() {
                                @Override
                                public void onSuccess(GenericRpcResponse<DataCollectionItem> response) {
                                    final DataCollectionItem content = response.getContent();
                                    if (content == null) {
                                        if (response.getException() != null) {
                                            UiManager.getInstance().displayDetailedErrorDialog(
                                                    "Failed to Add Published Course", response.getException());
                                        } else {
                                            UiManager.getInstance().displayErrorDialog("Failed to Add Published Course",
                                                    "The collection was not able to add the published course.", null);
                                        }
                                    } else {

                                        /* Add experiment to client-side collection */
                                        parentCollection.getCourses().add(content);

                                        shouldPollForCreateProgress = false;
                                        createExperimentButton.setEnabled(shouldEnablePublishCourseButton());

                                        if (!cancelledCreate) {
                                            onExperimentAddedSuccessfully(content);
                                        } else {
                                            cancelledCreate = false;
                                        }
                                    }

                                    /* creation was cancelled, so no need to
                                     * do anything except hide the creation
                                     * progress dialog */
                                    createProgressDialog.hide();
                                    cleanupCreateExperimentProgressIndicator();
                                }

                                @Override
                                public void onFailure(Throwable caught) {
                                    shouldPollForCreateProgress = false;
                                    createExperimentButton.setEnabled(shouldEnablePublishCourseButton());

                                    if (!cancelledCreate) {
                                        UiManager.getInstance().displayErrorDialog(
                                                "Failed to Add Published Course",
                                                "An error occurred while publishing the course and adding it to the collection. Message: "
                                                        + caught.getMessage(),
                                                null);
                                    } else {
                                        cancelledCreate = false;
                                    }

                                    /* creation was cancelled, so no need to
                                     * do anything except hide the creation
                                     * progress dialog */
                                    createProgressDialog.hide();
                                    cleanupCreateExperimentProgressIndicator();
                                }
                            });
                }
                else {
                    /* Create the experiment with no parent collection */
                    dashboardService.createExperiment(name, description, username, selectedCourse.getCourse().getDomainId(), dataSetType, new AsyncCallback<ExperimentResponse>() {

                        @Override
                        public void onSuccess(final ExperimentResponse response) {

                            shouldPollForCreateProgress = false;

                            createExperimentButton.setEnabled(shouldEnablePublishCourseButton());

                            if(!cancelledCreate){

                                if(response.isSuccess() && response.getExperiment() != null){
                                    //create completely finished, so need to give the client the experiment's URL.
                                    createProgressDialog.hide();

                                    final DataCollectionItem experiment = response.getExperiment();
                                    onExperimentAddedSuccessfully(experiment);

                                } else {

                                    createProgressDialog.hide();

                                    if(response.getErrorMessage() == null){
                                        UiManager.getInstance().displayErrorDialog("Publish Course Failed", "An error occurred while publishing the course.", null);

                                    } else {

                                        if(response.getErrorDetails() == null){
                                            UiManager.getInstance().displayErrorDialog("Publish Course Failed", "An error occurred while publishing the course: " + response.getErrorMessage(), null);

                                        } else {
                                            UiManager.getInstance().displayDetailedErrorDialog(
                                                    "Publish Course Failed",
                                                    "A problem was encountered while publishing the course: " + response.getErrorMessage(),
                                                    response.getErrorDetails(),
    												response.getErrorStackTrace(),
    												null);
                                        }
                                    }
                                }

                            } else {
                                //creation was cancelled, so no need to do anything except hide the creation progress dialog

                                createProgressDialog.hide();

                                cancelledCreate = false;
                            }

                            cleanupCreateExperimentProgressIndicator();
                        }

                        @Override
                        public void onFailure(Throwable e) {

                            shouldPollForCreateProgress = false;

                            createExperimentButton.setEnabled(shouldEnablePublishCourseButton());

                            if(!cancelledCreate){

                                UiManager.getInstance().displayErrorDialog("Publish Course Failed", "An error occurred while publishing the course: " + e.toString() +".", null);

                            } else {
                                //creation was cancelled, so no need to do anything except hide the creation progress dialog

                                createProgressDialog.hide();

                                cancelledCreate = false;
                            }

                            cleanupCreateExperimentProgressIndicator();
                        }
                    });
                }

                createProgressDialog.show();

                shouldPollForCreateProgress = true;

                maybePollForCreateProgress();
            }
        });

        cancelCreateProgressButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {

                //cancel creating the experiment

                cancelCreateProgressButton.setEnabled(false);

                cancelledCreate = true;

                createProgressDialogHeading.setText("Cancelling Publish Course...");

                dashboardService.cancelExport(UiManager.getInstance().getUserName(), new AsyncCallback<RpcResponse>() {

                    @Override
                    public void onFailure(Throwable e) {
                        UiManager.getInstance().displayErrorDialog("Cancel Publish Course Failed", "An error occurred while cancelling publish course: " + e.toString() +".", null);
                    }

                    @Override
                    public void onSuccess(RpcResponse response) {

                        if(response.isSuccess()){
                            shouldPollForCreateProgress = false;

                        } else {
                            if(response.getResponse() == null){
                                UiManager.getInstance().displayErrorDialog("Cancel Publish Course Failed", "An error occurred while cancelling publish course.", null);

                            } else {
                                UiManager.getInstance().displayErrorDialog("Cancel Publish Course Failed", "An error occurred while cancelling publish course: " + response.getResponse(), null);
                            }
                        }
                    }

                });
            }
        });

        createCourseButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                event.stopPropagation();
                UiManager.getInstance().displayConfirmDialog(
                        "Leave Publish Courses",
                        "In order to create a new course, you'll need to leave this page and enter the Course Authoring Tool.<br/>"
                        + "<br/>"
                        + "Are you sure you want to leave this page to enter the Course Authoring Tool?",
                        "Yes, Leave Page",
                        "No, Stay on Page", new ConfirmationDialogCallback() {

                    @Override
                    public void onDecline() {
                        //Nothing to do
                    }

                    @Override
                    public void onAccept() {
                        UiManager.getInstance().showEditCourse(null);
                    }
                });
            }
        });

        createCoursePanel.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                createCourseButton.click();
            }
        });

        confirmStartCourseExportButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {

                if(startExportExperimentCourseCommand != null){
                    startExportExperimentCourseCommand.execute();
                }
            }
        });

        confirmStartRawDataExportButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {

                if(startExportExperimentRawDataCommand != null){
                    startExportExperimentRawDataCommand.execute();
                }
            }
        });

        saveEditButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {

                if(saveExperimentEditsCommand != null){
                    saveExperimentEditsCommand.execute();
                }
            }
        });

        cancelExportCourseProgressButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {

                cancelExportCourseProgressButton.setEnabled(false);

                cancelledExportCourse = true;

                exportCourseProgressDialogHeading.setText("Cancelling Course Export...");

                dashboardService.cancelExport(UiManager.getInstance().getUserName(), new AsyncCallback<RpcResponse>() {

                    @Override
                    public void onFailure(Throwable e) {
                        UiManager.getInstance().displayErrorDialog("Cancel Course Export Failed", "An error occurred while cancelling the course export: " + e.toString() +".", null);
                    }

                    @Override
                    public void onSuccess(RpcResponse response) {

                        if(response.isSuccess()){
                            shouldPollForExportCourseProgress = false;

                        } else {
                            if(response.getResponse() == null){
                                UiManager.getInstance().displayErrorDialog("Cancel Course Export Failed", "An error occurred while cancelling the course export.", null);

                            } else {
                                UiManager.getInstance().displayErrorDialog("Cancel Course Export Failed", "An error occurred while cancelling the course export: " + response.getResponse(), null);
                            }
                        }
                    }

                });
            }
        });

        cancelExportRawDataProgressButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {

                cancelExportRawDataProgressButton.setEnabled(false);

                cancelledExportRawData = true;

                exportRawDataProgressDialogHeading.setText("Cancelling Raw Data Export...");

                dashboardService.cancelExport(UiManager.getInstance().getUserName(), new AsyncCallback<RpcResponse>() {

                    @Override
                    public void onFailure(Throwable e) {
                        UiManager.getInstance().displayErrorDialog("Cancel Raw Data Failed", "An error occurred while cancelling the raw data export: " + e.toString() +".", null);
                    }

                    @Override
                    public void onSuccess(RpcResponse response) {

                        if(response.isSuccess()){
                            shouldPollForExportRawDataProgress = false;

                        } else {
                            if(response.getResponse() == null){
                                UiManager.getInstance().displayErrorDialog("Cancel Raw Data Failed", "An error occurred while cancelling the raw data export.", null);

                            } else {
                                UiManager.getInstance().displayErrorDialog("Cancel Raw Data Failed", "An error occurred while cancelling the raw data export: " + response.getResponse(), null);
                            }
                        }
                    }

                });
            }
        });

        courseSearchBox.addKeyUpHandler(new KeyUpHandler() {

            @Override
            public void onKeyUp(KeyUpEvent event) {

                userCoursesTableDataProvider.getList().clear();
                userCoursesTableDataProvider.getList().addAll(applyCourseSearchFilter(userCourses));
                userCoursesTableDataProvider.refresh();

                if(StringUtils.isBlank(courseSearchBox.getText())){
                    userCourseEmptyTableWidget.setHTML(NO_USER_COURSES_TEXT);
                } else {
                    userCourseEmptyTableWidget.setHTML(NO_SEARCH_MATCHES_TEXT);
                }
            }
        });

        courseSearchButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {

                userCoursesTableDataProvider.getList().clear();
                userCoursesTableDataProvider.getList().addAll(applyCourseSearchFilter(userCourses));
                userCoursesTableDataProvider.refresh();

                if(StringUtils.isBlank(courseSearchBox.getText())){
                    userCourseEmptyTableWidget.setHTML(NO_USER_COURSES_TEXT);
                } else {
                    userCourseEmptyTableWidget.setHTML(NO_SEARCH_MATCHES_TEXT);
                }
            }
        });
        
        publishedCoursesSearchBox.addKeyUpHandler(new KeyUpHandler() {

            @Override
            public void onKeyUp(KeyUpEvent event) {
                
                if(event.getNativeKeyCode() == KeyCodes.KEY_ENTER){
                    filterSearchSortPublishedCourses();
                }else if(StringUtils.isBlank(publishedCoursesSearchBox.getValue())){
                    filterSearchSortPublishedCourses();
                }
            }
        });

        publishedCoursesSearchButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {                
                filterSearchSortPublishedCourses();                
            }
        });

        //get the list of experiments for the first time
        refreshExperimentList();

        experimentNote.setVisible(true);
        collectionNote.setVisible(false);
        ltiNote.setVisible(false);
    }
    
    /**
     * Show any published courses that reference the course specified. This currently expand the panel(s) for any
     * matching published courses found.  If the publish course list is still being retrieved from the server
     * the parameters will be saved and this method will be called again once the server returns that list.
     * @param domainOption the course to search for in the published courses.  If null this method does nothing.
     * @param dataSetType optional data set type to ignore when searching published course types.  E.g. ignore
     * experiment published courses.
     */
    public void expandPublishCourses(DomainOption domainOption, DataSetType dataSetType){
        
        this.courseToExpose = domainOption;
        this.courseDataSetTypeExposeFilter = dataSetType;
        
        if(ctrlLoadExperimentIcon.isLoading()){
            return;
        }else if(courseToExpose == null){
            return;
        }
        
        for(Widget widget : publishedCoursesRetrievedList){
            
            if(widget instanceof ExperimentWidget){
                
                ExperimentWidget experimentWidget = (ExperimentWidget)widget;
                experimentWidget.expandPublishCourses(domainOption, dataSetType);
                
            }else if(widget instanceof CourseCollectionWidget){
                
                CourseCollectionWidget collectionWidget = (CourseCollectionWidget)widget;
                collectionWidget.expandPublishCourses(domainOption, dataSetType);
            }
        }
    }
    
    /**
     * Performs the following actions on the published courses panel:
     * 1. removes all the current published courses widget
     * 2. applies the current published courses filter on the list retrieved from the server
     * 3. applies the current search term on the filtered list from 2.  If no search term, then nothing is removed.
     * 4. applies the current sort type on the list from 3.
     */
    private void filterSearchSortPublishedCourses(){ 
        logger.info("starting filtering, searching and sorting now... [currently showing "+experimentListPanel.getWidgetCount()+" of "+publishedCoursesRetrievedList.size()+" published courses]");
        applyPublishedCoursesFilter();
        logger.info("after filtering... [currently showing "+experimentListPanel.getWidgetCount()+" of "+publishedCoursesRetrievedList.size()+" published courses]");
        applyPublishedCoursesSearchFilter();
        logger.info("starting search term... [currently showing "+experimentListPanel.getWidgetCount()+" of "+publishedCoursesRetrievedList.size()+" published courses]");
        applyPublishedCourseSort();
        logger.info("finished filtering, searching and sorting.");
        
        if(experimentListPanel.getWidgetCount() == 0){
            logger.info("After filtering/searching, there are no published courses to show.");
            showNoPublishedCoursesMessage(NO_PUBLISHED_COURSES_WITH_FILTER_MESSAGE);
        }else{
            ctrlExperimentMessage.setVisible(false); // to hide the no published courses message
            ctrlLoadExperimentPanel.setVisible(false);  // to hide the loading published courses panel
            experimentListPanel.setVisible(true); // to show the list of published courses
        }
    }

    /**
     * Perform action once the experiment is added successfully.
     *
     * @param experimentAdded the new experiment
     */
    private void onExperimentAddedSuccessfully(DataCollectionItem experimentAdded) {
        if (experimentAdded.isDataSetType(DataSetType.EXPERIMENT)) {

            showExperimentCreatedDialog(experimentAdded);

        } else if (!experimentAdded.isDataSetType(DataSetType.COLLECTION)) {

            /* If not an EXPERIMENT or Collection type, simply Go back to the publish course page */
            hideCreateDialog();
            refreshExperimentList(experimentAdded.getId());
            
        } else {
            logger.severe("Unsupported publish course type encountered on the createDataSet() response: " + experimentAdded);
        }
    }

    private void createExperimentCollection(final String name, final String description) {
        hideCreateDialog();
        dashboardService.createCourseCollection(UiManager.getInstance().getUserName(), name, description,
                new AsyncCallback<CourseCollection>() {

            @Override
            public void onSuccess(CourseCollection collection) {
                CourseCollectionWidget collectionWidget = new CourseCollectionWidget(collection, ExperimentToolWidget.this);
                experimentListPanel.add(collectionWidget);
            }

            @Override
            public void onFailure(Throwable caught) {
                logger.log(Level.SEVERE, "There was a problem creating a new CourseCollection", caught);
                UiManager.getInstance().displayErrorDialog(
                        "Create Course Collection Error",
                        "There was a problem creating the '" + name + "' course collection",
                        null);
            }
        });
    }

    /**
     * Notify the server that the data collection raw data export progress indicator is no longer needed for the
     * user.
     */
    private void cleanupRawDataExportProgressIndicator(){
        dashboardService.cleanupRawDataExportProgressIndicator(UiManager.getInstance().getUserName(), new AsyncCallback<GenericRpcResponse<Void>>() {

            @Override
            public void onFailure(Throwable arg0) {
                //don't care, best effort
            }

            @Override
            public void onSuccess(GenericRpcResponse<Void> arg0) {
                //don't care, best effort
            }
        });

    }

    /**
     * Notify the server that the create data collection item progress indicator is no longer needed for the user
     */
    private void cleanupCreateExperimentProgressIndicator(){
        dashboardService.cleanupCreateDataCollectionItemProgressIndicator(UiManager.getInstance().getUserName(), new AsyncCallback<GenericRpcResponse<Void>>() {

            @Override
            public void onFailure(Throwable arg0) {
                //don't care, best effort
            }

            @Override
            public void onSuccess(GenericRpcResponse<Void> arg0) {
                //don't care, best effort
            }
        });

    }

    /**
     * Notify the server that the export data collection course progress indicator is no longer needed for the user
     */
    private void cleanupCourseExportProgressIndicator(){
        dashboardService.cleanupCourseExportProgressIndicator(UiManager.getInstance().getUserName(), new AsyncCallback<GenericRpcResponse<Void>>() {

            @Override
            public void onFailure(Throwable arg0) {
                //don't care, best effort
            }

            @Override
            public void onSuccess(GenericRpcResponse<Void> arg0) {
                //don't care, best effort
            }
        });

    }

    /**
     * Return whether the publish course button should be enabled or disabled.
     *
     * @return true if the button should be enabled because the required inputs have been provided
     */
    private boolean shouldEnablePublishCourseButton(){
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("shouldEnablePublishCourseButton()");
        }

        final String name = createNameTextBox.getValue();

        final boolean isCollection = StringUtils.equals(getSelectedType(), DataSetType.COLLECTION.name());
        final boolean availableCourseIsSelected = selectedCourse != null && selectedCourse.getState() == COURSE_STATE.AVAILABLE;
        return isNotBlank(name) && (availableCourseIsSelected || isCollection);
    }
    
    /**
     * Apply the current published courses search filter on the current widgets by only showing the
     * published course widgets that satisfy the search term. If the current search term
     * is blank, this method doesn't remove (or add back) any published courses.
     * @return true if there is a search term and it was applied, false if there was no search term.
     */
    private boolean applyPublishedCoursesSearchFilter(){
        
        String searchTerm = publishedCoursesSearchBox.getValue();
        
        if(StringUtils.isNotBlank(searchTerm)){
            // first get all the widgets currently being sorted, then remove all the widgets from the panel
            List<Widget> toFilter = new ArrayList<>();
            Iterator<Widget> widgetsItr = experimentListPanel.iterator();
            while(widgetsItr.hasNext()){
                toFilter.add(widgetsItr.next());
                widgetsItr.remove();
            }
            
            // get the filtered list that is a subset of the current list of widgets 
            List<Widget> appliedList = filterPublishedCoursesBySearchStr(searchTerm, toFilter);
            
            // add the widgets back in sorted order
            for(Widget widget : appliedList){
                experimentListPanel.add(widget);
            }
            
            return true;
        }else{
            return false;
        }        

    }
    
    /**
     * Apply the currently selected published course sort algorithm on the list of published courses
     * currently shown.
     */
    private void applyPublishedCourseSort(){
        sortPublishedCourses(selectedSort);
    }

    /**
     * Applies the search filter currently entered by the user to the given list of courses and returns a copy of the result
     *
     * @param courseList the list that should be filtered
     * @return a copy of the given list with the appropriate course filtering applied to it
     */
    private List<CourseEntry> applyCourseSearchFilter(List<CourseEntry> courseList){
        return filterCoursesByText(courseSearchBox.getValue(), courseList);
    }

    /**
     * Clear the search box text in order to reset the search filter.
     * This should normally only be done when the dialog is not visible in order to not
     * change the search results from what the author has manually entered as a search filter.
     */
    public void clearCourseSearchFilter(){
        courseSearchBox.clear();
    }
    
    /**
     * Filters the given list of courses using the given filter expression.
     * 
     * @param filterExpression the expression used to filter. If null or empty
     *        the list provided is not reduced in size.
     * @param toFilter the list of courses to filter. If null or empty, an empty list will
     *        be returned.
     * @return the filtered list. Can't be null but can be empty.
     */
    public static List<CourseEntry> filterCoursesByText(final String filterExpression, List<CourseEntry> toFilter) {
        List<CourseEntry> result = new ArrayList<>();
        if (toFilter == null || toFilter.isEmpty()) {
            return result;
        }
        if (StringUtils.isBlank(filterExpression)) {
            result.addAll(toFilter);
            return result;
        }
        final List<CourseEntry> toFilterCopy = new ArrayList<>(toFilter);
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
                final List<CourseEntry> binaryOpResult = new ArrayList<>();
                for (String operand : operands) {
                    final int j = operands.indexOf(operand);
                    if (operands.indexOf(operand) == 0) {
                        binaryOpResult.addAll(filterCoursesByText(operand, toFilterCopy));
                    } else if (operators.get(j - 1).matches("AND")) {
                        binaryOpResult.addAll(filterCoursesByText(operand, toFilterCopy));
                    } else if (operators.get(j - 1).matches("OR")) {
                        binaryOpResult.addAll(filterCoursesByText(operand, toFilterCopy));
                    }
                }
                /* Add what items remain to the result, ensuring duplicates aren't added */
                for(CourseEntry course : binaryOpResult) {
                    if(!result.contains(course)) {
                        result.add(course);
                    }
                }
            } else if (currentTerm.startsWith("-")) {
                /* If a term starts with a '-', then all rows captured by
                 * searching for the remainder of the term will be removed from
                 * the result */
                /* If there are already items in the result, search for the
                 * remainder of the search term and remove all items found in
                 * the search */
                if (!result.isEmpty()) {
                    result.removeAll(filterCoursesByText(currentTerm.substring(1), toFilterCopy));
                    /* Otherwise, add all the items in the table to the result,
                     * search for the remainder of the search term, and remove
                     * all items found in the search */
                } else {
                    result.addAll(toFilterCopy);
                    result.removeAll(filterCoursesByText(currentTerm.substring(1), toFilterCopy));
                }
            } else {
                /* It is still possible for an AND/OR keyword to make it here
                 * If it is the final term of a search query. If it does make 
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
                for (CourseEntry course : toFilterCopy) {
                    final String courseName = course.getCourse().getDomainName();
                    if (!result.contains(course) && ((exactMatch && courseName.contains(currentTerm))
                            || (!exactMatch && courseName.toLowerCase().contains(currentTerm.toLowerCase())))) {
                        if (logger.isLoggable(Level.INFO)) {
                            logger.info("By item name -> " + courseName);
                        }
                        result.add(course);
                        continue;
                    }
                }
            }
        }
        return result;
    }

    /**
     * Update the UI based on the provided data set type.
     *
     * @param dataSetType the selected publish course type
     */
    private void handleDataSetTypeChange(DataSetType dataSetType){

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("handleDataSetTypeChange(" + dataSetType + ")");
        }

        /* Ensure the correct button is active */
        experimentTypeButton.setActive(dataSetType == DataSetType.EXPERIMENT);
        collectionTypeButton.setActive(dataSetType == DataSetType.COLLECTION);
        ltiTypeButton.setActive(dataSetType == DataSetType.LTI);

        experimentNameLabel
                .setText(dataSetType == DataSetType.COLLECTION ? COLLECTION_NAME_LABEL : EXPERIMENT_NAME_LABEL);

        // only 1 course data type per course, therefore course name is publish course name
        createNameTextBox.setEnabled(!DataSetType.COURSE_DATA.equals(dataSetType));
        createNameTextBoxTooltip.setTitle(DataSetType.COURSE_DATA.equals(dataSetType) ? "The name will be filled in automatically once you select a course." : "");

        ltiNote.setVisible(dataSetType == DataSetType.LTI);
        collectionNote.setVisible(dataSetType == DataSetType.COLLECTION);
        experimentNote.setVisible(dataSetType == DataSetType.EXPERIMENT);
        searchPanel.setVisible(dataSetType != DataSetType.COLLECTION);
        courseListPanel.setVisible(dataSetType != DataSetType.COLLECTION);

        // Update each course widget with the current published course selection.
        for(CourseEntry courseEntry : userCourses){
            courseEntry.setDataSetType(dataSetType);
            refreshCourseEntryStatus(courseEntry);
        }

        userCoursesTableDataProvider.refresh();
        createExperimentButton.setEnabled(shouldEnablePublishCourseButton());
    }

    /**
     * Return the name of the selected publish course type.
     *
     * @return the name of the currently selected publish course type. Can return null.
     */
    private String getSelectedType(){

        String toRet = null;
        if(experimentTypeButton.isActive()){
            toRet = DataSetType.EXPERIMENT.name();
        } else if (collectionTypeButton.isActive()) {
            toRet = DataSetType.COLLECTION.name();
        } else if(ltiTypeButton.isActive()) {
            toRet = DataSetType.LTI.name();
        } else{
            logger.severe("found unhandled situation, was a new published course type button added.  See ExperimentToolWidget.getSelectedType()");
        }

        logger.fine("getSelectedType() -> " + toRet);
        return toRet;
    }

    /**
     * Displays the dialog for when an experiment has been successfully created.
     * @param experiment The experiment object details.
     */
    private void showExperimentCreatedDialog(final DataCollectionItem experiment) {
        // Show the experiment
        final String experimentUrl = experiment.getUrl();

        StringBuilder sb = new StringBuilder();

        sb.append("<label style='color: black; font-weight: bold; display: inline;'>").append(experiment.getName()).append("</label> has been created.<br/>")
                .append("<br/>")
                .append("Participants can access this experiment using the URL provided below. <br/>")
                .append("<br/>")
                .append("<div style='color: black; padding: 10px; border-radius: 10px; background-color: lightBlue; ")
                .append("background-image: linear-gradient(rgb(230, 230, 255), rgb(245,245,255) 10%, lightBlue); box-shadow: 3px 3px 5px rgba(0,0,0,0.5);'>")
                .append("<b>Experiment URL (read-only):</b></br>")
                .append("<div style='padding: 0px 20px;'>")
                .append("<input type='text' readonly value='").append(experimentUrl).append("' onClick='this.select();' ")
                .append("style='width: 100%; border-radius: 5px; margin-top: 5px; padding: 5px;'/>")
                .append("</div></div>")
                .append("<br/>")
                .append("This URL can be retrieved again at any time from the Publish Courses page.");

        if(UiManager.getInstance().getUseCloudLoginPage()){

            sb.append("<br/><br/><b>Note:</b> Any data gathered by this experiment will be backed up for 30 days ")
                    .append("after it is deleted.");
        }

        UiManager.getInstance().displayDialog(
                DialogType.DIALOG_INFO,
                "Experiment Created!",
                sb.toString(),
                new DialogCallback() {

                    @Override
                    public void onAccept() {

                        if (parentCollectionWidget != null) {
                            parentCollectionWidget.refreshUi();
                        } else {
                            /* Refresh the entire list */
                            refreshExperimentList(experiment.getId());
                        }

                        hideCreateDialog();
                    }
                }
        );
    }

    /**
     * Hides the create dialog
     */
    public void hideCreateDialog() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("hideCreateDialog()");
        }

        /* Reset the dialog */
        ValueChangeEvent.fire(experimentTypeButton, true);
        createNameTextBox.setValue(null);
        createDescriptionTextBox.setValue(null);

        /* Before hiding the dialog, scroll the dialog to the top for the next
         * time it is opened. */
        createDialogScrollPanel.scrollToTop();

        /* Hide the modal */
        createDialog.hide();
    }

    /**
     * Resets the data on the "Create Experiment" dialog and fetches the list of
     * courses
     *
     * @param courseCollection the collection that the experiment is being
     *        created for; null if a collection child is not the one being
     *        published.
     */
    public void showCreateDialog(final CourseCollectionWidget courseCollection){
        /* The dialog values are reset on close */

        userCourseTable.setVisible(false);
        ctrlLoadPanel.setVisible(true);
        ctrlLoadIcon.startLoading();

        ctrlCourseMessage.setVisible(false);

        this.parentCollectionWidget = courseCollection;

        /* Show the collection button if there is no parent collection */
        final boolean hasParentCollection = courseCollection != null;
        collectionTypeButton.setEnabled(!hasParentCollection);
        collectionTypeButton.setVisible(!hasParentCollection);
        ltiTypeButton.setEnabled(!hasParentCollection);
        ltiTypeButton.setVisible(!hasParentCollection);
        if (hasParentCollection) {
            experimentTypeButton.addStyleName("publishTypeButtonAlone");
        } else {
            experimentTypeButton.removeStyleName("publishTypeButtonAlone");
        }

        /* Show collection panel with the collection name label if there is a
         * parent collection */
        parentCollectionPanel.setVisible(hasParentCollection);
        parentCollectionLabel
                .setText(courseCollection != null ? courseCollection.getCollection().getName() : Constants.EMPTY);

        dashboardService.getCourseList(UiManager.getInstance().getSessionId(), false, true, null, new AsyncCallback<GetCourseListResponse>() {

            @Override
            public void onFailure(Throwable t) {

                logger.fine("Rpc throwable exception was caught: " + t.getMessage());

                UiManager.getInstance().displayErrorDialog(ERROR_FETCH_COURSES, ERROR_THROWABLE_MESSAGE, null);
                ctrlCourseMessage.setText(NO_COURSE_MESSAGE);
                ctrlCourseMessage.setVisible(true);

                 ctrlLoadPanel.setVisible(false);
                 ctrlLoadIcon.stopLoading();
            }

            @Override
            public void onSuccess(GetCourseListResponse result) {

                 if (result != null && result.isSuccess()) {
                     logger.fine("result is success");

                     if (result.getCourseList() != null && !result.getCourseList().isEmpty()) {

                         logger.fine("getCourseList rpc returned " + result.getCourseList().size() + " courses.");

                         userCourseTable.setVisible(true);

                         userCourses.clear();
                         for(DomainOption course : result.getCourseList()){

                             String dataSetTypeStr = getSelectedType();
                             DataSetType dataSetType = DataSetType.valueOf(dataSetTypeStr);
                             CourseEntry entry = new CourseEntry(course, dataSetType);
                             refreshCourseEntryStatus(entry);
                             userCourses.add(entry);
                         }

                         userCoursesTableDataProvider.getList().clear();
                         userCoursesTableDataProvider.getList().addAll(applyCourseSearchFilter(userCourses));
                         userCoursesTableDataProvider.refresh();

                     } else {
                         ctrlCourseMessage.setText(NO_COURSE_MESSAGE);
                         ctrlCourseMessage.setVisible(true);
                     }

                 } else {
                     logger.fine("Rpc result is failure -" + result);

                     if(result != null && Dashboard.getInstance().getServerProperties().getDeploymentMode() == DeploymentModeEnum.SERVER) {
                         UiManager.getInstance().displayErrorDialog(ERROR_FETCH_COURSES, result.getResponse(), null);
                         ctrlCourseMessage.setText(NO_COURSE_MESSAGE);
                         ctrlCourseMessage.setVisible(true);

                     } else {
                         UiManager.getInstance().displayErrorDialog(ERROR_FETCH_COURSES, ERROR_RPC_FAILURE, null);
                         ctrlCourseMessage.setText(NO_COURSE_MESSAGE);
                         ctrlCourseMessage.setVisible(true);
                     }
                 }

                 ctrlLoadPanel.setVisible(false);
                 ctrlLoadIcon.stopLoading();
            }
        });

        createDialog.show();
    }

    /**
     * Polls the server for the progress of the current experiment creation until the user cancels
     */
    private void maybePollForCreateProgress(){

        if(shouldPollForCreateProgress){

            dashboardService.getCreateExperimentProgress(UiManager.getInstance().getUserName(), new AsyncCallback<ProgressResponse>() {

                @Override
                public void onFailure(Throwable e) {

                    maybePollForCreateProgress();
                }

                @Override
                public void onSuccess(ProgressResponse response) {

                    if(response.isSuccess()){

                        ProgressIndicator progress = response.getProgress();
                        createOverallProgress.updateProgress(progress);
                    }

                    maybePollForCreateProgress();
                }
            });

        }
    }

    /**
     * Refreshes the list of experiments with the most up-to-date data from the server
     */
    private void refreshExperimentList(){

        refreshExperimentList(null);
    }

    /**
     * Refreshes the list of experiments with the most up-to-date data from the
     * server and automatically opens the given experiment's widget
     *
     * @param experimentToOpen The id of the experiment to open.
     */
    private void refreshExperimentList(final String experimentToOpen){

        final ExperimentToolWidget thisWidget = this;
        ctrlLoadExperimentPanel.setVisible(true);
        ctrlLoadExperimentIcon.startLoading();

        ctrlExperimentMessage.setVisible(false);
        experimentListPanel.setVisible(false);

        final String username = UiManager.getInstance().getUserName();
        dashboardService.getExperiments(username, new AsyncCallback<ExperimentListResponse>() {

            @Override
            public void onSuccess(final ExperimentListResponse response) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("dashboardService.getExperiments.onSuccess(" + response + ")");
                }

                final UiManager uiManager = UiManager.getInstance();
                if (!response.isSuccess()) {
                    logger.fine("Rpc result is failure" + response.getAdditionalInformation());
                    uiManager.displayErrorDialog(ERROR_FETCH_EXPERIMENTS, ERROR_EXPERIMENTS_RPC_FAILURE, null);
                    ctrlCourseMessage.setText(NO_PUBLISHED_COURSES_NO_FILTER_MESSAGE);
                    ctrlCourseMessage.setVisible(true);
                    return;
                }

                dashboardService.getCourseCollectionsByUser(username, new AsyncCallback<Collection<CourseCollection>>() {

                    @Override
                    public void onSuccess(Collection<CourseCollection> courseCollections) {
                        if (logger.isLoggable(Level.FINE)) {
                            logger.fine("dashboardService.getCourseCollectionByAuthor.onSuccess(" + courseCollections + ")");
                        }

                        for (CourseCollection collection : courseCollections) {
                            collection.removeCoursesByVisibility(uiManager.getUserName());
                        }

                        ctrlLoadExperimentPanel.setVisible(false);
                        ctrlLoadExperimentIcon.stopLoading();
                        publishedCoursesRetrievedList.clear();

                        final ArrayList<DataCollectionItem> experiments = response.getExperiments();
                        if (experiments != null) {

                            /* Filter out the grouped experiments to
                             * prevent experiments/courses from being
                             * listed twice. */
                            filterOutGroupedExperiments(courseCollections, experiments);

                            /* Clear out any existing experiments */
                            experimentListPanel.clear();

                            /* Build the widgets for the experiments
                             * that aren't grouped (if any). */
                            final boolean hasExperiments = !experiments.isEmpty();
                            if(hasExperiments){
                                buildExperimentWidgets(experiments);
                            } else {
                                ctrlCourseMessage.setText(NO_PUBLISHED_COURSES_NO_FILTER_MESSAGE);
                                ctrlCourseMessage.setVisible(true);
                            }

                            /* Build the widgets for the course
                             * collections */
                            final boolean hasCollections = !courseCollections.isEmpty();
                            if (hasCollections) {
                                buildCourseCollectionWidgets(courseCollections);
                            } else {
                                // TODO CHUCK: Handle a lack of experiment collections
                            }

                            if (hasExperiments || hasCollections) {
                                
                                expandPublishCourses(courseToExpose, courseDataSetTypeExposeFilter);
                                applyPublishedCourseSort();  // default sort upon retrieval from server
                                experimentListPanel.setVisible(true);
                            }

                        }
                    }

                    @Override
                    public void onFailure(Throwable thrown) {
                        UiManager.getInstance().displayErrorDialog(ERROR_FETCH_EXPERIMENTS, ERROR_EXPERIMENTS_THROWABLE_MESSAGE, null);
                        showNoPublishedCoursesMessage(NO_PUBLISHED_COURSES_NO_FILTER_MESSAGE);

                        publishedCoursesRetrievedList.clear();
                    }

                    private void filterOutGroupedExperiments(Collection<CourseCollection> courseCollections, Collection<DataCollectionItem> experiments) {
                        if (logger.isLoggable(Level.FINE)) {
                            List<Object> params = Arrays.<Object>asList(courseCollections, experiments);
                            logger.fine("filterOutGroupedExperiments(" + StringUtils.join(", ", params) + ")");
                        }

                        for (CourseCollection courseCollection : courseCollections) {
                            for (DataCollectionItem collectedCourse : courseCollection.getCourses()) {
                                Iterator<DataCollectionItem> experimentIter = experiments.iterator();
                                while (experimentIter.hasNext()) {
                                    DataCollectionItem experiment = experimentIter.next();
                                    if (experiment.getId().equals(collectedCourse.getId())) {
                                        experimentIter.remove();
                                    }
                                }
                            }
                        }
                    }

                    private void buildExperimentWidgets(Collection<DataCollectionItem> experiments) {
                        if (logger.isLoggable(Level.FINE)) {
                            logger.fine("buildExperimentWidgets(" + experiments + ")");
                        }

                        ExperimentWidget widgetToScrollTo = null;

                        for (DataCollectionItem experiment : experiments) {

                            ExperimentWidget experimentWidget = new ExperimentWidget(experiment, thisWidget);

                            if (experimentToOpen != null && experiment.getId() != null
                                    && experiment.getId().equals(experimentToOpen)) {
                                widgetToScrollTo = experimentWidget;
                            }

                            experimentListPanel.add(experimentWidget);
                            publishedCoursesRetrievedList.add(experimentWidget);
                        }

                        if (widgetToScrollTo != null) {
                            widgetToScrollTo.getElement().scrollIntoView();
                            widgetToScrollTo.setContentVisible(true);
                        }
                    }

                    private void buildCourseCollectionWidgets(Collection<CourseCollection> courseCollections) {
                        if (logger.isLoggable(Level.FINE)) {
                            logger.fine("buildCourseCollectionWidgets(" + courseCollections + ")");
                        }

                        for (CourseCollection courseCollection : courseCollections) {
                            CourseCollectionWidget collectionWidget = new CourseCollectionWidget(courseCollection, ExperimentToolWidget.this);
                            experimentListPanel.add(collectionWidget);
                            publishedCoursesRetrievedList.add(collectionWidget);
                        }
                    }
                });

            }

            @Override
            public void onFailure(Throwable thrown) {

                UiManager.getInstance().displayErrorDialog(ERROR_FETCH_EXPERIMENTS, ERROR_EXPERIMENTS_THROWABLE_MESSAGE, null);
                showNoPublishedCoursesMessage(NO_PUBLISHED_COURSES_NO_FILTER_MESSAGE);
            }
        });
    }
    
    /**
     * Shows the text provided on the panel where published courses list is shown instead of
     * the list of widgets.  This is usually used when there are no published courses to show.
     * 
     * @param textToShow should be useful to the current reason why there are no published courses to show.
     */
    private void showNoPublishedCoursesMessage(String textToShow){
        
        ctrlExperimentMessage.setText(textToShow);
        ctrlExperimentMessage.setVisible(true);

        ctrlLoadExperimentPanel.setVisible(false);
        ctrlLoadExperimentIcon.stopLoading();
    }
    
    /**
     * Filter the published course widget list provided by using the filter expression as the search term.
     * 
     * @param filterExpression the expression used to filter published courses.  If null or empty the
     * list provided is not reduced in size.
     * @param toFilter the list of published course widgets to filter
     * @return the filtered list of published course widget.  Can be null but empty.
     */
    public List<Widget> filterPublishedCoursesBySearchStr(final String filterExpression, List<Widget> toFilter) {
        List<Widget> result = new ArrayList<>();
        if (toFilter == null || toFilter.isEmpty()) {
            return result;
        }
        if (StringUtils.isBlank(filterExpression)) {
            result.addAll(toFilter);
            return result;
        }
        final List<Widget> toFilterCopy = new ArrayList<>(toFilter);
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
                final List<Widget> binaryOpResult = new ArrayList<>();
                for (String operand : operands) {
                    final int j = operands.indexOf(operand);
                    if (operands.indexOf(operand) == 0) {
                        binaryOpResult.addAll(filterPublishedCoursesBySearchStr(operand, toFilterCopy));
                    } else if (operators.get(j - 1).matches("AND")) {
                        binaryOpResult.retainAll(filterPublishedCoursesBySearchStr(operand, toFilterCopy));
                    } else if (operators.get(j - 1).matches("OR")) {
                        binaryOpResult.addAll(filterPublishedCoursesBySearchStr(operand, toFilterCopy));
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
                    result.removeAll(filterPublishedCoursesBySearchStr(currentTerm.substring(1), toFilterCopy));
                    /* Otherwise, add all the items in the table to the result,
                     * search for the remainder of the search term, and remove
                     * all items found in the search */
                } else {
                    result.addAll(toFilterCopy);
                    result.removeAll(filterPublishedCoursesBySearchStr(currentTerm.substring(1), toFilterCopy));
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
              //find all the published courses containing the term's text (ignoring case)
                for(Widget widget : toFilterCopy){

                    String publishedCourseName = getPublishedCourseWidgetName(widget);
                    if(filterByPublishedCourseWidgetName(widget, currentTerm, exactMatch)){
                        logger.info("By published course name = "+publishedCourseName);
                        result.add(widget);
                        continue;
                    }
                    
                    if(filterByPublishedCourseWidgetDesc(widget, currentTerm, exactMatch)){
                        logger.info("By published course desc = "+publishedCourseName);
                        result.add(widget);
                        continue;
                    }
                    
                    if(filterByPublishedCourseWidgetId(widget, currentTerm, exactMatch)){
                        logger.info("By published course id = "+publishedCourseName);
                        result.add(widget);
                        continue;
                    }
                    
                    if(filterByPublishedCourseWidgetCourseName(widget, currentTerm, exactMatch)){
                        logger.info("By published course's one course name -> "+publishedCourseName);
                        result.add(widget);
                        continue;
                    }

                }
            }
        }
        return result;
    }
    
    
    /**
     * Filter the set of published courses retrieved by the server based on the currently
     * selected filter.
     */
    private void applyPublishedCoursesFilter(){
        filterPublishedCourses(selectedFilter);
    }
    
    /**
     * Filter the published courses retrieved by the server by the filter option specified.
     * 
     * @param filterOption the enumerated filter option to apply to the published courses
     */
    private void filterPublishedCourses(PublishedCourseFilterEnum filterOption){
        
        switch(filterOption){
        
        case FILTER_COURSE_TILE:
            filterForDataSetType(DataSetType.COURSE_DATA);
            break;
        case FILTER_EXPERIMENT:
            filterForDataSetType(DataSetType.EXPERIMENT);
            break;
        case FILTER_LTI:
            filterForDataSetType(DataSetType.LTI);
            break;
        case FLITER_ACTIVE:
            filterForStatus(ExperimentStatus.RUNNING);
            break;
        case FILTER_PAUSED:
            filterForStatus(ExperimentStatus.PAUSED);
            break;
        case FILTER_SHARED_WITH_ME:
            filterForShared(true);
            break;
        case FILTER_SHARED_WITH_OTHERS:
            filterForShared(false);
            break;
        case FILTER_NONE:
            //fall through
        default:
            // put all published course back
            experimentListPanel.clear();
            for(Widget widget : publishedCoursesRetrievedList){
                experimentListPanel.add(widget);
            }
            break;
        }
    }
    
    /**
     * Order the published courses that are currently shown by the sorting algorithm specified.
     * 
     * @param sortOption the enumerated option to sort by
     */
    private void sortPublishedCourses(PublishedCourseSortEnum sortOption){
        
        switch(sortOption){
        
        case SORT_NAME_ALPHABETICAL:
            sortPublishedCoursesByName(true);
            break;
        case SORT_NAME_REVERSE_ALPHABETICAL:
            sortPublishedCoursesByName(false);
            break;
        case SORT_ATTEMPTS_DESCENDING:
            sortPublishedCoursesByAttempts(true);
            break;
        case SORT_ATTEMPTS_ASCENDING:
            sortPublishedCoursesByAttempts(false);
            break;
        case SORT_LAST_ATTEMPT_DESCENDING:
            sortPublishedCoursesByLastAttempt(true);
            break;
        case SORT_LAST_ATTEMPT_ASCENDING:
            sortPublishedCoursesByLastAttempt(false);
            break;
        default:
            sortPublishedCoursesByName(true);
        }
    }
    
    /**
     * Sorts the published courses panel by comparing the name of each top level item (LTI, Experiment, Collection).
     * @param alphabetical true if the name sort is alphabetical, false for reverse alphabetical
     */
    public void sortPublishedCoursesByName(final boolean alphabetical){
        
        // first remove all the widgets from the panel
        List<Widget> panelWidgets = new ArrayList<>();
        Iterator<Widget> panelWidgetsItr = experimentListPanel.iterator();
        while(panelWidgetsItr.hasNext()){
            panelWidgets.add(panelWidgetsItr.next());
            panelWidgetsItr.remove();
        }

        // sort the local list by comparing the name of each top level item (LTI, Experiment, Collection)
        Collections.sort(panelWidgets, new Comparator<Widget>() {

            @Override
            public int compare(Widget o1, Widget o2) {

                String o1Name = getPublishedCourseWidgetName(o1), o2Name = getPublishedCourseWidgetName(o2);
                if(o1Name == null){
                    // worst case, push object 1 to end of list
                    return 1;
                }else if(o2Name == null){
                    // worst case, push object 2 to end of list
                    return 1;
                }
                
                if(alphabetical){
                    return o1Name.compareToIgnoreCase(o2Name);
                }else{
                    return o2Name.compareToIgnoreCase(o1Name);
                }
            }

        });
        
        // add the widgets back in sorted order
        for(Widget widget : panelWidgets){
            experimentListPanel.add(widget);
        }
    }
    
    /**
     * Sorts the published courses panel by comparing the number of attempts of each top level item (LTI, Experiment, Collection).
     * @param descending true if the attempts sort is highest first, false for lowest number first
     */
    public void sortPublishedCoursesByAttempts(final boolean descending){
        
        // first remove all the widgets from the panel
        List<Widget> panelWidgets = new ArrayList<>();
        Iterator<Widget> panelWidgetsItr = experimentListPanel.iterator();
        while(panelWidgetsItr.hasNext()){
            panelWidgets.add(panelWidgetsItr.next());
            panelWidgetsItr.remove();
        }

        // sort the local list by comparing the attempt count of each top level item (LTI, Experiment, Collection)
        Collections.sort(panelWidgets, new Comparator<Widget>() {

            @Override
            public int compare(Widget o1, Widget o2) {

                long o1Attempts = getPublishedCourseWidgetMaxNumOfAttempts(o1), 
                        o2Attempts = getPublishedCourseWidgetMaxNumOfAttempts(o2);
                
                if(!descending){
                    return Long.compare(o1Attempts, o2Attempts);
                }else{
                    return Long.compare(o2Attempts, o1Attempts);
                }
            }

        });
        
        // add the widgets back in sorted order
        for(Widget widget : panelWidgets){
            experimentListPanel.add(widget);
        }
    }
    
    /**
     * Sorts the published courses panel by comparing the last attempts of each top level item (LTI, Experiment, Collection).
     * @param descending true if the last attempt sort is most recent first, false for oldest first
     */
    public void sortPublishedCoursesByLastAttempt(final boolean descending){
        
        // first remove all the widgets from the panel
        List<Widget> panelWidgets = new ArrayList<>();
        Iterator<Widget> panelWidgetsItr = experimentListPanel.iterator();
        while(panelWidgetsItr.hasNext()){
            panelWidgets.add(panelWidgetsItr.next());
            panelWidgetsItr.remove();
        }

        // sort the local list by comparing the last attempt date of each top level item (LTI, Experiment, Collection)
        Collections.sort(panelWidgets, new Comparator<Widget>() {

            @Override
            public int compare(Widget o1, Widget o2) {

                Date o1LastAttempt = getPublishedCourseWidgetRecentAttempt(o1), 
                        o2LastAttempt = getPublishedCourseWidgetRecentAttempt(o2);
                
                if(o1LastAttempt == null){
                    // worst case, push object 1 to end of list
                    return 1;
                }else if(o2LastAttempt == null){
                    // worst case, push object 2 to end of list
                    return -1;
                }
                
                if(descending){
                    return o2LastAttempt.compareTo(o1LastAttempt);
                }else{
                    return o1LastAttempt.compareTo(o2LastAttempt);
                }
            }

        });
        
        // add the widgets back in sorted order
        for(Widget widget : panelWidgets){
            experimentListPanel.add(widget);
        }
    }
    
    /**
     * Return the name of the published course for the published course widget
     * @param widget the published course widget to get the name for
     * @return the published course name
     */
    private String getPublishedCourseWidgetName(Widget widget){
        
        if(widget instanceof ExperimentWidget){
            ExperimentWidget experimentWidget = (ExperimentWidget)widget;
            DataCollectionItem widgetDataCollectionItem = experimentWidget.getExperiment();
            if(widgetDataCollectionItem != null){
                return widgetDataCollectionItem.getName();
            }
        }else if(widget instanceof CourseCollectionWidget){
            
            CourseCollectionWidget courseCollectionWidget = (CourseCollectionWidget)widget;
            CourseCollection courseCollection = courseCollectionWidget.getCollection();
            return courseCollection.getName();
        }
        
        return null;
    }
    
    /**
     * Return true if the course name of the published course contains the search term
     * @param widget the published course widget to get the course name for and compare it to the search term.
     * IF this is a collection it will search the child published courses.
     * @return true if the search term is in the published course's course name
     */
    private boolean filterByPublishedCourseWidgetCourseName(Widget widget, String searchTerm, boolean exactMatch){
        
        if(widget instanceof ExperimentWidget){
            ExperimentWidget experimentWidget = (ExperimentWidget)widget;
            DataCollectionItem widgetDataCollectionItem = experimentWidget.getExperiment();
            if(widgetDataCollectionItem != null){
                String courseId = widgetDataCollectionItem.getSourceCourseId();
                String courseFolder = widgetDataCollectionItem.getCourseFolder();
                return ((exactMatch && ((courseId != null && courseId.contains(searchTerm)) || 
                        (courseFolder != null && courseFolder.contains(searchTerm)))) ||
                        (!exactMatch && ((courseId != null && courseId.toLowerCase().contains(searchTerm.toLowerCase())) || 
                        (courseFolder != null && courseFolder.toLowerCase().contains(searchTerm.toLowerCase())))));
            }
        }else if(widget instanceof CourseCollectionWidget){
            
            CourseCollectionWidget courseCollectionWidget = (CourseCollectionWidget)widget;
            CourseCollection courseCollection = courseCollectionWidget.getCollection();
            for(DataCollectionItem publishedCourse : courseCollection.getCourses()){
                
                String courseId = publishedCourse.getSourceCourseId();
                String courseFolder = publishedCourse.getCourseFolder();
                if((exactMatch && ((courseId != null && courseId.contains(searchTerm)) || 
                        (courseFolder != null && courseFolder.contains(searchTerm)))) ||
                        (!exactMatch && ((courseId != null && courseId.toLowerCase().contains(searchTerm.toLowerCase())) || 
                        (courseFolder != null && courseFolder.toLowerCase().contains(searchTerm.toLowerCase()))))){
                    return true;
                }
            }
        }
        
        return false;
    }
     
    /**
     * Return true if the published course id of the published course contains the search term
     * @param widget the published course widget to get the published course id for and compare it to the search term.
     * If this is a collection it will search the child published courses as well.
     * @return true if the search term is in the published course id
     */
    private boolean filterByPublishedCourseWidgetId(Widget widget, String searchTerm, boolean exactMatch){
        
        if(widget instanceof ExperimentWidget){
            ExperimentWidget experimentWidget = (ExperimentWidget)widget;
            DataCollectionItem widgetDataCollectionItem = experimentWidget.getExperiment();
            if(widgetDataCollectionItem != null){
                String id = widgetDataCollectionItem.getId();
                if(id != null) {
                    return((exactMatch && id.contains(searchTerm)) || (!exactMatch && id.toLowerCase().contains(searchTerm.toLowerCase())));
                } else {
                    return false;
                }

            }
        }else if(widget instanceof CourseCollectionWidget){
            
            CourseCollectionWidget courseCollectionWidget = (CourseCollectionWidget)widget;
            CourseCollection courseCollection = courseCollectionWidget.getCollection();
            String collectionId = courseCollection.getId();
            if(collectionId != null && ((exactMatch && collectionId.contains(searchTerm)) 
                    || (!exactMatch && collectionId.toLowerCase().contains(searchTerm.toLowerCase())))){
                return true;
            }

            
            // check child published courses
            for(DataCollectionItem publishedCourse : courseCollection.getCourses()){
                
                String childId = publishedCourse.getId();
                if(childId != null && ((exactMatch && childId.contains(searchTerm))
                        || (!exactMatch && childId.toLowerCase().contains(searchTerm.toLowerCase())))){
                    return true;
                }

            }
        }
        
        return false;
    }
    
    /**
     * Filters the set of published courses retrieved from the server so that the type specified and
     * collections with the type specified are shown.
     * @param dataSetType can be experiment or LTI
     */
    private void filterForDataSetType(DataSetType dataSetType){
        
        if(dataSetType == null || dataSetType.equals(DataSetType.COLLECTION)){
            return;
        }
        
        // first remove all the widgets from the panel
        List<Widget> panelWidgets = new ArrayList<>(publishedCoursesRetrievedList);
        experimentListPanel.clear();
        
        Iterator<Widget> panelWidgetsItr = panelWidgets.iterator();
        while(panelWidgetsItr.hasNext()){
            
            Widget candidateWidget = panelWidgetsItr.next();
            if(candidateWidget instanceof ExperimentWidget){
                ExperimentWidget experimentWidget = (ExperimentWidget)candidateWidget;
                DataCollectionItem widgetDataCollectionItem = experimentWidget.getExperiment();
                if(widgetDataCollectionItem != null){
                    
                    if(!widgetDataCollectionItem.isDataSetType(dataSetType)){
                        panelWidgetsItr.remove();
                        continue;
                    }
                }
                
            }else if(candidateWidget instanceof CourseCollectionWidget){
                
                CourseCollectionWidget courseCollectionWidget = (CourseCollectionWidget)candidateWidget;
                CourseCollection courseCollection = courseCollectionWidget.getCollection();
                
                if(courseCollection.getCourses().isEmpty()){
                    panelWidgetsItr.remove();
                    continue;
                }
                
                // check child published courses
                boolean foundOne = false;
                for(DataCollectionItem publishedCourse : courseCollection.getCourses()){
                    
                    if(publishedCourse.isDataSetType(dataSetType)){
                        foundOne = true;
                        break;
                    }
                }
                
                if(!foundOne){
                    panelWidgetsItr.remove();
                    continue;
                }
            }
                    
        } // end for
        
        // add the widgets back
        for(Widget widget : panelWidgets){
            experimentListPanel.add(widget);
        }
    }
    
    /**
     * Filters the set of published courses retrieved from the server based on the shared settings, i.e.
     * is this shared with the user or is the user sharing it with others.
     * @param sharedWithMeFilter true to keep items that are shared with the user, false to keep items that the user is sharing
     * with others
     */
    private void filterForShared(boolean sharedWithMeFilter) {
        
        // first remove all the widgets from the panel
        List<Widget> panelWidgets = new ArrayList<>(publishedCoursesRetrievedList);
        experimentListPanel.clear();
        
        Iterator<Widget> panelWidgetsItr = panelWidgets.iterator();
        while(panelWidgetsItr.hasNext()){
            
            Widget candidateWidget = panelWidgetsItr.next();
            if(candidateWidget instanceof ExperimentWidget){
                ExperimentWidget experimentWidget = (ExperimentWidget)candidateWidget;
                DataCollectionItem widgetDataCollectionItem = experimentWidget.getExperiment();
                if(widgetDataCollectionItem != null){
                    
                    if(shouldRemoveBasedOnSharedFilter(widgetDataCollectionItem, sharedWithMeFilter)) {
                        panelWidgetsItr.remove();
                        continue;
                    }
                }
                
            }else if(candidateWidget instanceof CourseCollectionWidget){
                
                CourseCollectionWidget courseCollectionWidget = (CourseCollectionWidget)candidateWidget;
                CourseCollection courseCollection = courseCollectionWidget.getCollection();
                
                if(courseCollection.getCourses().isEmpty()){
                    panelWidgetsItr.remove();
                    continue;
                }
                
                // check child published courses
                boolean foundOneToKeep = false;
                for(DataCollectionItem publishedCourse : courseCollection.getCourses()){
                    
                    if(!shouldRemoveBasedOnSharedFilter(publishedCourse, sharedWithMeFilter)) {
                        foundOneToKeep = true;
                        break;
                    }

                }
                
                if(!foundOneToKeep){
                    panelWidgetsItr.remove();
                    continue;
                }
            }
        }
        
        // add the widgets back
        for(Widget widget : panelWidgets){
            experimentListPanel.add(widget);
        }
    }
    
    /**
     * Return whether the item provided should be removed from this widget based on filtering logic regarding
     * sharing permissions.
     * @param item the published course item to check to see if it should be filtered out
     * @param sharedWithMeFilter true to keep items that are shared with the user, false to keep items that the user is sharing
     * with others
     * @return true if this item should be removed from the list of published courses currently shown in this widget
     */
    private boolean shouldRemoveBasedOnSharedFilter(DataCollectionItem item, boolean sharedWithMeFilter) {
        
        DataCollectionUserRole role = item.getPermissionForUser(UiManager.getInstance().getUserName());
        boolean ownedByUser = DataCollectionUserRole.OWNER.equals(role);
        boolean managedByUser = DataCollectionUserRole.MANAGER.equals(role);
        
        // someone other that this user has permission to this item
        boolean sharedWithOthers = false;
        // someone other than the owner and this user has permissions to this item
        boolean sharedWithMoreThanMeAndOwner = false;
        if(item.getPermissions() != null) {

            for(DataCollectionPermission permissionEntry : item.getPermissions()) {
                
                if(!UiManager.getInstance().getUserName().equalsIgnoreCase(permissionEntry.getUsername())) {
                    // found permissions for someone other than this user
                    sharedWithOthers = true;
                }
                
                if(sharedWithOthers && 
                        !item.getAuthorUsername().equalsIgnoreCase(permissionEntry.getUsername()) && 
                        !UiManager.getInstance().getUserName().equalsIgnoreCase(permissionEntry.getUsername())) {
                    // found permissions for someone other than this user and other than the owner
                    sharedWithMoreThanMeAndOwner = true;
                }
                
                if(sharedWithOthers && sharedWithMoreThanMeAndOwner) {
                    // short-circuit - no need to keep looking                                
                    break;
                }
            }
        }
        
        if(sharedWithMeFilter && ownedByUser){
            // want to show items shared with the user, not owned by the user - remove owned items
            return true;
        }else if(!sharedWithMeFilter && 
                ((!ownedByUser && !managedByUser) || (ownedByUser && !sharedWithOthers) || (managedByUser && !sharedWithMoreThanMeAndOwner))) {
            // want to show items the user is sharing which they must own or be a manager 
            // -> remove not owned or managed items (because the user cant share it then) OR 
            // -> remove items owned but not shared with anyone
            // -> remove items managed but not shared with anyone else but the owner and this user
            return true;
        }
        
        return false;
    }
    
    
    /**
     * Filters the set of published courses retrieved from the server so that the status specified and
     * collections with the status specified are shown.
     * @param status the status to show
     */
    private void filterForStatus(ExperimentStatus status){
        
        // first remove all the widgets from the panel
        List<Widget> panelWidgets = new ArrayList<>(publishedCoursesRetrievedList);
        experimentListPanel.clear();
        
        Iterator<Widget> panelWidgetsItr = panelWidgets.iterator();
        while(panelWidgetsItr.hasNext()){
            
            Widget candidateWidget = panelWidgetsItr.next();
            if(candidateWidget instanceof ExperimentWidget){
                ExperimentWidget experimentWidget = (ExperimentWidget)candidateWidget;
                DataCollectionItem widgetDataCollectionItem = experimentWidget.getExperiment();
                if(widgetDataCollectionItem != null){
                    
                    if(widgetDataCollectionItem.getStatus() != status){
                        panelWidgetsItr.remove();
                        continue;
                    }
                }
                
            }else if(candidateWidget instanceof CourseCollectionWidget){
                
                CourseCollectionWidget courseCollectionWidget = (CourseCollectionWidget)candidateWidget;
                CourseCollection courseCollection = courseCollectionWidget.getCollection();
                
                if(courseCollection.getCourses().isEmpty()){
                    panelWidgetsItr.remove();
                    continue;
                }
                
                // check child published courses
                boolean foundOne = false;
                for(DataCollectionItem publishedCourse : courseCollection.getCourses()){
                    
                    if(publishedCourse.getStatus() != status){
                        foundOne = true;
                        break;
                    }
                }
                
                if(!foundOne){
                    panelWidgetsItr.remove();
                    continue;
                }
            }
                    
        } // end for
        
        // add the widgets back
        for(Widget widget : panelWidgets){
            experimentListPanel.add(widget);
        }
    }
    
    /**
     * Return true if the published course description of the published course contains the search term
     * @param widget the published course widget to get the published course description for and compare it to the search term.
     * If this is a collection it will search the child published courses as well.
     * @param exactMatch boolean indicating whether exactMatch is active
     * @return true if the search term is in the published course description
     */
    private boolean filterByPublishedCourseWidgetDesc(Widget widget, String searchTerm, boolean exactMatch){
        
        if(widget instanceof ExperimentWidget){
            ExperimentWidget experimentWidget = (ExperimentWidget)widget;
            DataCollectionItem widgetDataCollectionItem = experimentWidget.getExperiment();
            if(widgetDataCollectionItem != null){
                String desc = widgetDataCollectionItem.getDescription();
                if(desc != null) {
                    return((exactMatch && desc.contains(searchTerm)) || (!exactMatch && desc.toLowerCase().contains(searchTerm.toLowerCase())));
                } else {
                    return false;
                }
            }
        }else if(widget instanceof CourseCollectionWidget){
            
            CourseCollectionWidget courseCollectionWidget = (CourseCollectionWidget)widget;
            CourseCollection courseCollection = courseCollectionWidget.getCollection();
            String collectionDesc = courseCollection.getDescription();
            if(collectionDesc != null && ((exactMatch && collectionDesc.contains(searchTerm)) 
                    || (!exactMatch && collectionDesc.toLowerCase().contains(searchTerm.toLowerCase())))){
                return true;
            }
            
            // check child published courses
            for(DataCollectionItem publishedCourse : courseCollection.getCourses()){
                
                String childDesc = publishedCourse.getDescription();
                if(childDesc != null && ((exactMatch && childDesc.contains(searchTerm))
                        || (!exactMatch && childDesc.toLowerCase().contains(searchTerm.toLowerCase())))){
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Return true if the published course name of the published course contains the search term
     * @param widget the published course widget to get the published course name for and compare it to the search term.
     * If this is a collection it will search the child published courses as well.
     * @param exactMatch boolean indicating whether exactMatch is active
     * @return true if the search term is in the published course name
     */
    private boolean filterByPublishedCourseWidgetName(Widget widget, String searchTerm, boolean exactMatch){
        
        if(widget instanceof ExperimentWidget){
            ExperimentWidget experimentWidget = (ExperimentWidget)widget;
            DataCollectionItem widgetDataCollectionItem = experimentWidget.getExperiment();
            if(widgetDataCollectionItem != null){
                String name = widgetDataCollectionItem.getName();
                if(name != null) {
                    return((exactMatch && name.contains(searchTerm)) || (!exactMatch && name.toLowerCase().contains(searchTerm.toLowerCase())));
                } else {
                    return false;
                }
            }
        }else if(widget instanceof CourseCollectionWidget){
            
            CourseCollectionWidget courseCollectionWidget = (CourseCollectionWidget)widget;
            CourseCollection courseCollection = courseCollectionWidget.getCollection();
            String collectionName = courseCollection.getName();
            if(collectionName != null && ((exactMatch && collectionName.contains(searchTerm)) 
                    || (!exactMatch && collectionName.toLowerCase().contains(searchTerm.toLowerCase())))){
                return true;
            }
            
            // check child published courses
            for(DataCollectionItem publishedCourse : courseCollection.getCourses()){
                
                String childName = publishedCourse.getName();
                if(childName != null && ((exactMatch && childName.contains(searchTerm))
                        || (!exactMatch && childName.toLowerCase().contains(searchTerm.toLowerCase())))){
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Return the number of attempts for the published course for the published course widget
     * @param widget the published course widget to get the number of attempts for
     * @return the published course number of attempts
     */
    private long getPublishedCourseWidgetMaxNumOfAttempts(Widget widget){
        
        if(widget instanceof ExperimentWidget){
            ExperimentWidget experimentWidget = (ExperimentWidget)widget;
            DataCollectionItem widgetDataCollectionItem = experimentWidget.getExperiment();
            if(widgetDataCollectionItem != null){
                
                if(widgetDataCollectionItem.getDataSetType() == DataSetType.EXPERIMENT){
                    return widgetDataCollectionItem.getSubjectSize();
                }else if(widgetDataCollectionItem.getDataSetType() == DataSetType.LTI){
                    return widgetDataCollectionItem.getLtiResultSize();
                }
            }
            
        }else if(widget instanceof CourseCollectionWidget){
            
            CourseCollectionWidget courseCollectionWidget = (CourseCollectionWidget)widget;
            CourseCollection courseCollection = courseCollectionWidget.getCollection();
            
            long max = Long.MIN_VALUE;
            for(DataCollectionItem publishedCourse : courseCollection.getCourses()){
                
                long size = 0;
                if(publishedCourse.getDataSetType() == DataSetType.EXPERIMENT){
                    size = publishedCourse.getSubjectSize();
                }else if(publishedCourse.getDataSetType() == DataSetType.LTI){
                    size = publishedCourse.getLtiResultSize();
                }
                
                if(size > max){
                    max = size;
                }
            }
            
            return max;
        }        

        return 0;
    }
    
    /**
     * Return the date of the last attempt of the published course for the published course widget
     * @param widget the published course widget to get the last attempt date for
     * @return the published course's last attempt date
     */
    private Date getPublishedCourseWidgetRecentAttempt(Widget widget){
        
        if(widget instanceof ExperimentWidget){
            ExperimentWidget experimentWidget = (ExperimentWidget)widget;
            DataCollectionItem widgetDataCollectionItem = experimentWidget.getExperiment();
            if(widgetDataCollectionItem != null){
                
                if(widgetDataCollectionItem.getDataSetType() == DataSetType.EXPERIMENT){
                    return widgetDataCollectionItem.getSubjectLastAttemptedDate();
                }else if(widgetDataCollectionItem.getDataSetType() == DataSetType.LTI){
                    return widgetDataCollectionItem.getLtiResultLastAttemptedDate();
                }
            }
            
        }else if(widget instanceof CourseCollectionWidget){
            
            CourseCollectionWidget courseCollectionWidget = (CourseCollectionWidget)widget;
            CourseCollection courseCollection = courseCollectionWidget.getCollection();
            
            Date newest = null;
            for(DataCollectionItem publishedCourse : courseCollection.getCourses()){
                
                Date date = null;
                if(publishedCourse.getDataSetType() == DataSetType.EXPERIMENT){
                    date = publishedCourse.getSubjectLastAttemptedDate();
                }else if(publishedCourse.getDataSetType() == DataSetType.LTI){
                    date = publishedCourse.getLtiResultLastAttemptedDate();
                }
                
                if(newest == null || (date != null && date.after(newest))){
                    newest = date;
                }
            }
            
            return newest;
        }
        
        return null;
    }

    /**
     * Shows the edit dialog for a specified experiment.
     *
     * @param experiment The experiment to show the edit dialog for.
     * @param childWidget The child experiment widget.
     */
    public void showEditDialog(final DataCollectionItem experiment, final ExperimentWidget childWidget) {
        editNameTextBox.setValue(experiment.getName());

        // 'course data' published course name comes from the course name
        editNameTextBox.setEnabled(!experiment.getDataSetType().equals(DataSetType.COURSE_DATA));
        editNameTextBoxTooltip.setTitle(DataSetType.COURSE_DATA.equals(experiment.getDataSetType()) ? "The name is managed by the course." : "");

        editDescriptionTextBox.setValue(experiment.getDescription());

        editDialog.show();

        saveExperimentEditsCommand = new Command() {

            @Override
            public void execute() {

                final String name = editNameTextBox.getValue();

                final String description = editDescriptionTextBox.getValue();

                if(name == null || name.isEmpty()){
                    UiManager.getInstance().displayErrorDialog("No Name Specified", "Please provide a name.", null);
                    return;
                }

                dashboardService.updateExperiment(UiManager.getInstance().getUserName(), experiment.getId(), name, description, new AsyncCallback<ExperimentResponse>() {

                    @Override
                    public void onSuccess(ExperimentResponse response) {

                        if(response.isSuccess() && response.getExperiment() != null){

                            childWidget.setExperiment(response.getExperiment());

                            editDialog.hide();

                        } else {

                            createProgressDialog.hide();

                            if(response.getErrorMessage() == null){
                                UiManager.getInstance().displayErrorDialog("Update Published Course Failed", "An error occurred while updating the published course.", null);

                            } else {

                                if(response.getErrorDetails() == null){
                                    UiManager.getInstance().displayErrorDialog("Update Published Course Failed", "An error occurred while updating the published course: " + response.getErrorMessage(), null);

                                } else {
                                    UiManager.getInstance().displayDetailedErrorDialog(
                                            "Update Published Course Failed",
                                            "An error occurred while updating the published course: " + response.getErrorMessage(),
                                            response.getErrorDetails(),
														response.getErrorStackTrace(),
		                                                null);
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(Throwable thrown) {
                        UiManager.getInstance().displayErrorDialog("Update Published Course Failed", "An error occurred while updating the published course: " + thrown.toString() +".", null);
                    }
                });
            }
        };
    }

    /**
     * Shows the delete dialog for a given experiment.
     *
     * @param experiment The experiment to show the dialog for.
     * @param childWidget The child widget that is used to display the experiment data.
     */
    public void showDeleteDialog(final DataCollectionItem experiment, final ExperimentWidget childWidget) {
        deleteProgressDialogHeading.setText("Deleting Published Course...");
        deleteOverallProgress.updateProgress(new ProgressIndicator(0, "Initializing"));

        dashboardService.deleteExperiment(UiManager.getInstance().getUserName(), experiment.getId(), new AsyncCallback<DetailedRpcResponse>() {

            @Override
            public void onSuccess(final DetailedRpcResponse response) {

                shouldPollForDeleteProgress = false;

                if(response.isSuccess() ){

                    refreshExperimentList();

                    UiManager.getInstance().displayInfoDialog(
                            "Deleted Published Course",
                            (experiment.getName() != null ? "<b>" + experiment.getName() + "</b>" : "The published course") + ""
                                    + " has been successfully deleted."
                            );

                    deleteProgressDialog.hide();

                } else {

                    deleteProgressDialog.hide();

                    if(response.getErrorMessage() == null){
                        UiManager.getInstance().displayErrorDialog("Delete Published Course Failed", "An error occurred while deleting the published course.", null);

                    } else {

                        if(response.getErrorDetails() == null){
                            UiManager.getInstance().displayErrorDialog("Delete Published Course Failed", "An error occurred while deleting the published course: " + response.getErrorMessage(), null);

                        } else {
                            UiManager.getInstance().displayDetailedErrorDialog(
                                    "Delete Published Course Failed",
                                    "An error occurred while deleting the published course: " + response.getErrorMessage(),
                                    response.getErrorDetails(),
                                    response.getErrorStackTrace(),
                                    null);
                        }
                    }

                }

                cleanupDeleteDataCollectionItemProgressIndicator();
            }

            @Override
            public void onFailure(Throwable e) {

                shouldPollForDeleteProgress = false;

                UiManager.getInstance().displayErrorDialog("Delete Published Course Failed", "An error occurred while deleting the published course: " + e.toString() +".", null);

                cleanupDeleteDataCollectionItemProgressIndicator();
           }
        });

        deleteProgressDialog.show();

        shouldPollForDeleteProgress = true;

        maybePollForDeleteProgress();
    }

    /**
     * Notify the server that the delete data collection item progress indicator is no longer needed for the user
     */
    private void cleanupDeleteDataCollectionItemProgressIndicator(){

        dashboardService.cleanupDeleteDataCollectionItemProgressIndicator(
                UiManager.getInstance().getUserName(), new AsyncCallback<GenericRpcResponse<Void>>() {

            @Override
            public void onSuccess(GenericRpcResponse<Void> arg0) {
                //don't care
            }

            @Override
            public void onFailure(Throwable arg0) {
                //don't care, best effort
            }
        });
    }

    /**
     * Shows the start export course dialog for the specified experiment.
     *
     * @param experiment The experiment to show the dialog for.
     * @param childWidget The child widget that is displaying the experiment data.
     */
    public void showStartExportCourseDialog(final DataCollectionItem experiment, final ExperimentWidget childWidget) {
        startCourseExportSummary.setExperiment(experiment);

        startExportExperimentCourseCommand = new Command(){

            @Override
            public void execute() {

                startExportCourseDialog.hide();

                cancelExportCourseProgressButton.setEnabled(true);

                cancelledExportCourse = false;

                exportCourseProgressDialogHeading.setText("Exporting Course...");
                exportCourseOverallProgress.updateProgress(new ProgressIndicator(0, "Initializing"));
                exportCourseSubtaskProgress.updateProgress(new ProgressIndicator(0, "Initializing"));

                dashboardService.exportExperimentCourse(UiManager.getInstance().getUserName(), experiment.getId(), new AsyncCallback<ExportResponse>() {

                    @Override
                    public void onSuccess(final ExportResponse response) {

                        shouldPollForExportCourseProgress = false;

                        if(!cancelledExportCourse){

                            if(response.isSuccess() && response.getExportResult() != null){

                                final DownloadableFileRef result = response.getExportResult();

                                exportCourseProgressDialog.hide();

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

                                exportCourseProgressDialog.hide();

                                if(response.getErrorMessage() == null){
                                    UiManager.getInstance().displayErrorDialog("Export Course Failed", "An error occurred while exporting the course.", null);

                                } else {

                                    if(response.getErrorDetails() == null){
                                        UiManager.getInstance().displayErrorDialog("Export Course Failed", "An error occurred while exporting the course: " + response.getErrorMessage(), null);

                                    } else {
                                        UiManager.getInstance().displayDetailedErrorDialog(
                                                "Export Course Failed",
                                                "An error occurred while exporting the course: " + response.getErrorMessage(),
                                                response.getErrorDetails(),
															response.getErrorStackTrace(),
			                                                null);
                                    }
                                }
                            }

                        } else {

                            exportCourseProgressDialog.hide();

                            cancelledExportCourse = false;
                        }

                        cleanupCourseExportProgressIndicator();
                    }

                    @Override
                    public void onFailure(Throwable e) {

                        shouldPollForExportCourseProgress = false;

                        if(!cancelledExportCourse){

                            UiManager.getInstance().displayErrorDialog("Export Course Failed", "An error occurred while exporting the course: " + e.toString() +".", null);

                        } else {
                            //export was cancelled, so no need to do anything except hide the export progress dialog

                            exportCourseProgressDialog.hide();

                            cancelledExportCourse = false;
                        }

                        cleanupCourseExportProgressIndicator();
                    }
                });

                exportCourseProgressDialog.show();

                shouldPollForExportCourseProgress = true;

                maybePollForExportCourseProgress();
            }
        };

        startExportCourseDialog.show();
    }

    /**
     * Shows the start Export Subject data dialog for a given experiment.
     *
     * @param experiment The object containing the experiment data.
     * @param childWidget The child widget that is showing the experiment data.
     */
    public void showStartExportSubjectDialog(final DataCollectionItem experiment, final ExperimentWidget childWidget) {
        startRawDataExportSummary.setExperiment(experiment);

        startExportExperimentRawDataCommand = new Command(){

            @Override
            public void execute() {

                startExportRawDataDialog.hide();

                cancelExportRawDataProgressButton.setEnabled(true);

                cancelledExportRawData = false;

                exportRawDataProgressDialogHeading.setText("Exporting Raw Data...");
                exportRawDataOverallProgress.updateProgress(new ProgressIndicator(0, "Initializing"));
                exportRawDataSubtaskProgress.updateProgress(new ProgressIndicator(0, "Initializing"));

                final boolean exportConvertedBinaryLogs = Boolean.TRUE.equals(convertLogCheckBox.getValue());
                dashboardService.exportExperimentRawData(UiManager.getInstance().getUserName(), experiment.getId(), exportConvertedBinaryLogs, new AsyncCallback<ExportResponse>() {

                    @Override
                    public void onSuccess(final ExportResponse response) {

                        shouldPollForExportRawDataProgress = false;

                        if(!cancelledExportRawData){

                            if(response.isSuccess() && response.getExportResult() != null){

                                final DownloadableFileRef result = response.getExportResult();

                                exportRawDataProgressDialog.hide();

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

                                exportRawDataProgressDialog.hide();

                                if(response.getErrorMessage() == null){
                                    UiManager.getInstance().displayErrorDialog("Export Raw Data Failed", "An error occurred while exporting the raw participants data.", null);

                                } else {

                                    if(response.getErrorDetails() == null){
                                        UiManager.getInstance().displayErrorDialog("Export Raw Data Failed", "An error occurred while exporting the raw participants data: " + response.getErrorMessage(), null);

                                    } else {
                                        UiManager.getInstance().displayDetailedErrorDialog(
                                                "Export Raw Data Failed",
                                                "An error occurred while exporting the raw participant data: " + response.getErrorMessage(),
                                                response.getErrorDetails(),
															response.getErrorStackTrace(),
			                                                null);
                                    }
                                }
                            }

                        } else {

                            exportRawDataProgressDialog.hide();

                            cancelledExportRawData = false;
                        }

                        cleanupRawDataExportProgressIndicator();
                    }

                    @Override
                    public void onFailure(Throwable e) {

                        shouldPollForExportRawDataProgress = false;

                        if(!cancelledExportRawData){

                            UiManager.getInstance().displayErrorDialog("Export Raw Data Failed", "An error occurred while exporting the raw participants data: " + e.toString() +".", null);

                        } else {
                            //export was cancelled, so no need to do anything except hide the export progress dialog

                            exportRawDataProgressDialog.hide();

                            cancelledExportRawData = false;
                        }

                        cleanupRawDataExportProgressIndicator();
                    }
                });

                exportRawDataProgressDialog.show();

                shouldPollForExportRawDataProgress = true;

                maybePollForExportRawDataProgress();
            }
        };

        startExportRawDataDialog.show();
    }

    /**
     * Shows the build report dialog for the specified experiment.
     *
     * @param experiment The experiment to show the dialog for.
     */
    public void showBuildReportDialog(final DataCollectionItem experiment) {
        reportDialog.show(experiment);
    }

    /**
     * Polls the server for the progress of the current experiment deletion until the user cancels
     */
    private void maybePollForDeleteProgress(){

        if(shouldPollForDeleteProgress){

            dashboardService.getDeleteExperimentProgress(UiManager.getInstance().getUserName(), new AsyncCallback<ProgressResponse>() {

                @Override
                public void onFailure(Throwable e) {

                    maybePollForDeleteProgress();
                }

                @Override
                public void onSuccess(ProgressResponse response) {

                    if(response.isSuccess()){

                        ProgressIndicator progress = response.getProgress();
                        deleteOverallProgress.updateProgress(progress);

                    }

                    maybePollForDeleteProgress();
                }
            });


        }
    }

    /**
     * Polls the server for the progress of the current experiment course export until the user cancels
     */
    private void maybePollForExportCourseProgress(){

        if(shouldPollForExportCourseProgress){

            dashboardService.getExportExperimentCourseProgress(UiManager.getInstance().getUserName(), new AsyncCallback<ProgressResponse>() {

                @Override
                public void onFailure(Throwable e) {

                    maybePollForExportCourseProgress();
                }

                @Override
                public void onSuccess(ProgressResponse response) {

                    if(response.isSuccess()){

                        ProgressIndicator progress = response.getProgress();
                        exportCourseOverallProgress.updateProgress(progress);

                        if(progress.getSubtaskProcessIndicator() != null){
                            exportCourseSubtaskProgress.updateProgress(progress.getSubtaskProcessIndicator());
                        }
                    }

                    maybePollForExportCourseProgress();
                }
            });

        }
    }

    /**
     * Polls the server for the progress of the current experiment raw data export until the user cancels
     */
    private void maybePollForExportRawDataProgress(){

        if(shouldPollForExportRawDataProgress){

            dashboardService.getExportExperimentRawDataProgress(UiManager.getInstance().getUserName(), new AsyncCallback<ProgressResponse>() {


                @Override
                public void onFailure(Throwable e) {

                    maybePollForExportRawDataProgress();
                }

                @Override
                public void onSuccess(ProgressResponse response) {

                    if(response.isSuccess()){

                        ProgressIndicator progress = response.getProgress();
                        exportRawDataOverallProgress.updateProgress(progress);


                        if(progress.getSubtaskProcessIndicator() != null){
                            exportRawDataSubtaskProgress.updateProgress(progress.getSubtaskProcessIndicator());

                        }
                    }

                    maybePollForExportRawDataProgress();
                }
            });

        }
    }

    /**
     * Set the state of the provided course entry by analyzing the course entry against the current
     * publish course type selected on this dialog.
     *
     * @param courseEntry the course entry in the course table to update the state for
     */
    private void refreshCourseEntryStatus(CourseEntry courseEntry) {

        if (courseEntry == null) {
            logger.severe("refreshCourseEntryStatus() 'courseEntry' cannot be null.");
            return;
        }

        if(logger.isLoggable(Level.INFO)){
            logger.info("refreshCourseEntryStatus() called for course: " + courseEntry.getCourse().getDomainId());
        }

        DomainOption validatedCourse = courseEntry.getCourse();
        DataSetType dataSetType = courseEntry.getDataSetType();

        switch(dataSetType){
        case COURSE_DATA:
            // purposeful fall through
            //$FALL-THROUGH$
        case LTI:
            // purposeful fall through
            //$FALL-THROUGH$
        case EXPERIMENT:
            if (!validatedCourse.isDomainIdWritable()) {
                // course must be write-able for LTI
                courseEntry.setState(COURSE_STATE.READ_ONLY_LOCKED);
                break;
            }

            if(validatedCourse.getDomainOptionRecommendation() != null
                && validatedCourse.getDomainOptionRecommendation().getDomainOptionRecommendationEnum() != null
                && validatedCourse.getDomainOptionRecommendation().getDomainOptionRecommendationEnum().isUnavailableType()){
                courseEntry.setState(COURSE_STATE.UNAVAILABLE);
                break;
            }

            courseEntry.setState(COURSE_STATE.AVAILABLE);
            break;
		default:
			break;
        }

    }

    /**
     * Wrapper around a domain option in order to keep track of publish course types and
     * whether the course is available for selection.
     *
     * @author mhoffman
     *
     */
    public static class CourseEntry{

        /** The Domain Option that belongs to this widget. */
        private DomainOption course;

        /** The currently selected published course type. */
        private DataSetType dataSetType;

        private COURSE_STATE state;

        public enum COURSE_STATE{
            AVAILABLE,
            UNAVAILABLE,
            READ_ONLY_LOCKED,
        }

        /**
         * Set attributes.
         *
         * @param course contains information about the course.  can't be null
         * @param dataSetType the current type of publishing selected on this panel.  can't be null.
         */
        public CourseEntry(DomainOption course, DataSetType dataSetType){

            setCourse(course);
            setDataSetType(dataSetType);
        }


        public DomainOption getCourse() {
            return course;
        }

        private void setCourse(DomainOption course) {

            if(course == null){
                throw new IllegalArgumentException("The course can't be null.");
            }
            this.course = course;
        }

        public DataSetType getDataSetType() {
            return dataSetType;
        }

        public void setDataSetType(DataSetType dataSetType) {

            if(dataSetType == null){
                throw new IllegalArgumentException("The data set can't be null.");
            }
            this.dataSetType = dataSetType;
        }

        public COURSE_STATE getState() {
            return state;
        }

        public void setState(COURSE_STATE state) {
            this.state = state;
        }


        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("[CourseEntry: course=");
            builder.append(course);
            builder.append(", dataSetType=");
            builder.append(dataSetType);
            builder.append(", state=");
            builder.append(state);
            builder.append("]");
            return builder.toString();
        }
    }
}
