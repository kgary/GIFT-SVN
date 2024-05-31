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
import java.util.List;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.html.Text;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ShowRangeEvent;
import com.google.gwt.event.logical.shared.ShowRangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DatePicker;

import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.common.enums.DeploymentModeEnum;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.ert.EventReportColumn;
import mil.arl.gift.common.ert.EventType;
import mil.arl.gift.common.ert.GenerateReportStatus;
import mil.arl.gift.common.ert.ReportProperties;
import mil.arl.gift.common.experiment.DataCollectionItem;
import mil.arl.gift.common.experiment.ExperimentUtil.DataSetType;
import mil.arl.gift.common.gwt.client.DetailedRpcResponse;
import mil.arl.gift.common.gwt.client.RpcResponse;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.TimePicker;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ProgressBarListEntry;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.DownloadableFileRef;
import mil.arl.gift.common.io.ProgressIndicator;
import mil.arl.gift.tools.dashboard.client.DashboardService;
import mil.arl.gift.tools.dashboard.client.DashboardServiceAsync;
import mil.arl.gift.tools.dashboard.client.UiManager;
import mil.arl.gift.tools.dashboard.client.bootstrap.BsDialogWidget.DialogCallback;
import mil.arl.gift.tools.dashboard.client.bootstrap.BsDialogWidget.DialogType;
import mil.arl.gift.tools.dashboard.shared.rpcs.GenerateReportStatusResponse;

/**
 * A dialog used to build published course reports
 * 
 * @author nroberts
 */
public class ExperimentBuildReportDialogWidget extends Composite {

    /** Instance of the logger. */
    private static Logger logger = Logger.getLogger(ExperimentBuildReportDialogWidget.class.getName());
    
    private static ExperimentBuildReportDialogWidgetUiBinder uiBinder = GWT
            .create(ExperimentBuildReportDialogWidgetUiBinder.class);

    interface ExperimentBuildReportDialogWidgetUiBinder extends
            UiBinder<Widget, ExperimentBuildReportDialogWidget> {
    }
   
    /** The tool tip text used when the button is disabled due to lack of participants **/
    private static final String NO_SUBJECTS_TOOL_TIP_TEXT = "Reports cannot be generated until at least one user has participated";
    
    /** The initial list of data types to show to the user. Should contain important GIFT events like survey responses, learner states, etc. */
    private static final List<DataTypeEntry> frequentlyReportedDataTypes = new ArrayList<DataTypeEntry>();
    
    /** The list of data types used by training applications */
    private static final List<DataTypeEntry> trainingApplicationDataTypes = new ArrayList<DataTypeEntry>();
    
    /** The list of remaining data types */
    private static final List<DataTypeEntry> otherDataTypes = new ArrayList<DataTypeEntry>();
    
    /** The list of system-level data types that should initially be hidden from end users */
    private static final List<DataTypeEntry> systemDataTypes = new ArrayList<DataTypeEntry>();
    
    /** Number of milliseconds in an hour, used for calculating time difference */
    private static final int MILLISECONDS_IN_HOUR = 3600000;
    
    /** Number of milliseconds in a minute, used for calculating time difference */
    private static final int MILLISECONDS_IN_MINUTE = 60000;
    
    /** Value used to cache last end date prior to change  */
    private Date lastEndDate;
    
    /** Value used to cache laststart date prior to change  */
    private Date lastStartDate;
    
    /** Value used to cache last start hours value  */
    private int lastStartHours;
    
    /** Value used to cache last start minutes value  */
    private int lastStartMinutes;
    
    /** Value used to cache last end hours value  */
    private int lastEndHours;
    
    /** Value used to cache last end minutes value  */
    private int lastEndMinutes;
    
    /** Value used to cache last start time AM/PM value */
    private String lastStartTimeSwitchLabel;
    
    /** Value used to cache last end time AM/PM value */
    private String lastEndTimeSwitchLabel;
    
    /** Tracks whether the last entry into a time field was a valid entry or not */
    private boolean timeError = false;
    
    private static final NumberFormat twoDigitFormat = NumberFormat.getFormat("00");
    
    /** System message types to hide from end users either due to sheer frequency (e.g. module status) or lack of relevance (e.g. kill module) */
    private static final List<MessageTypeEnum> SYSTEM_MESSAGE_TYPES = Arrays.asList(new MessageTypeEnum[]{
        MessageTypeEnum.ACK,
        MessageTypeEnum.NACK,
        MessageTypeEnum.PROCESSED_ACK,
        MessageTypeEnum.PROCESSED_NACK,
        MessageTypeEnum.GATEWAY_MODULE_STATUS,
        MessageTypeEnum.MODULE_STATUS,
        MessageTypeEnum.MODULE_ALLOCATION_REQUEST,
        MessageTypeEnum.MODULE_ALLOCATION_REPLY,
        MessageTypeEnum.ACTIVE_DOMAIN_SESSIONS_REQUEST,
        MessageTypeEnum.ACTIVE_USER_SESSIONS_REPLY,
        MessageTypeEnum.INIT_INTEROP_CONNECTIONS,
        MessageTypeEnum.DOMAIN_OPTIONS_REQUEST,
        MessageTypeEnum.DOMAIN_OPTIONS_REPLY,
        MessageTypeEnum.CONFIGURE_INTEROP_CONNECTIONS,
        MessageTypeEnum.KILL_MODULE,
        MessageTypeEnum.ACTIVE_DOMAIN_SESSIONS_REPLY,
        MessageTypeEnum.ACTIVE_USER_SESSIONS_REQUEST,
        MessageTypeEnum.DISPLAY_COURSE_INIT_INSTRUCTIONS_REQUEST,
        MessageTypeEnum.DOMAIN_SELECTION_REPLY,
        MessageTypeEnum.DOMAIN_SELECTION_REQUEST,
        MessageTypeEnum.DOMAIN_SESSION_START_TIME_REQUEST,
        MessageTypeEnum.DOMAIN_SESSION_START_TIME_REPLY,
        MessageTypeEnum.EXPERIMENT_COURSE_REQUEST,
        MessageTypeEnum.GET_EXPERIMENT_REQUEST,
        MessageTypeEnum.GET_EXPERIMENT_REPLY,
        MessageTypeEnum.LTI_GETUSER_REQUEST,
        MessageTypeEnum.LTI_GETUSER_REPLY,
        MessageTypeEnum.LOGIN_REQUEST,
        MessageTypeEnum.LOGIN_REPLY,
        MessageTypeEnum.LOGOUT_REQUEST,
        MessageTypeEnum.NEW_USER_REQUEST,
        MessageTypeEnum.SUBJECT_CREATED,
        MessageTypeEnum.USER_ID_REPLY,
        MessageTypeEnum.USER_ID_REQUEST,
        MessageTypeEnum.SURVEY_CHECK_REQUEST,
        MessageTypeEnum.SURVEY_CHECK_RESPONSE
    });
    
    static{
        
        //Populate the lists of data types. Since everything is final and static, we can use the same lists across all instances of this dialog.
        for(MessageTypeEnum messageType : MessageTypeEnum.VALUES()){
            
            if(!SYSTEM_MESSAGE_TYPES.contains(messageType)){
            
                if(messageType.equals(MessageTypeEnum.SUBMIT_SURVEY_RESULTS)){
                    frequentlyReportedDataTypes.add(new DataTypeEntry(messageType.getName(), "Survey responses", messageType.getDescription()));
                
                } else if(messageType.equals(MessageTypeEnum.PERFORMANCE_ASSESSMENT)){
                    frequentlyReportedDataTypes.add(new DataTypeEntry(messageType.getName(), "Performance assessments", messageType.getDescription()));
                    
                } else if(messageType.equals(MessageTypeEnum.LEARNER_STATE)){
                    frequentlyReportedDataTypes.add(new DataTypeEntry(messageType.getName(), "Learner states", messageType.getDescription()));
                    
                } else if(messageType.equals(MessageTypeEnum.PEDAGOGICAL_REQUEST)){
                    frequentlyReportedDataTypes.add(new DataTypeEntry(messageType.getName(), "Pedagogical requests", messageType.getDescription()));
                    
                } else if(messageType.equals(MessageTypeEnum.DISPLAY_FEEDBACK_TUTOR_REQUEST)){
                    frequentlyReportedDataTypes.add(new DataTypeEntry(messageType.getName(), "Show Feedback in Tutor", messageType.getDescription()));
                    
                } else if(messageType.equals(MessageTypeEnum.DISPLAY_FEEDBACK_GATEWAY_REQUEST)){
                    frequentlyReportedDataTypes.add(new DataTypeEntry(messageType.getName(), "Show Feedback in Training App", messageType.getDescription()));
                    
                } else if(messageType.equals(MessageTypeEnum.ENVIRONMENT_CONTROL)){
                    frequentlyReportedDataTypes.add(new DataTypeEntry(messageType.getName(), "Scenario Adaptation (Environment Control)", messageType.getDescription()));
                    
                } else if(MessageTypeEnum.TRAINING_APP_STATE_MESSAGE_TYPES.contains(messageType)){                    
                    trainingApplicationDataTypes.add(new DataTypeEntry(messageType.getName(), messageType.getDisplayName(), messageType.getDescription()));
                    
                } else {
                    otherDataTypes.add(new DataTypeEntry(messageType.getName(), messageType.getDisplayName(), messageType.getDescription()));
                }
            
            } else {
                
                systemDataTypes.add(new DataTypeEntry(messageType.getName(), messageType.getDisplayName(), messageType.getDescription()));
            }
        }    
        
        //sort the populated lists alphabetically by display name (or regular name if no display name is available)
        Comparator<DataTypeEntry> sorter = new Comparator<DataTypeEntry>() {

            @Override
            public int compare(DataTypeEntry o1, DataTypeEntry o2) {
                
                String firstName = o1.getDisplayName() != null ? o1.getDisplayName() : o1.getName();
                String secondName = o2.getDisplayName() != null ? o2.getDisplayName() : o2.getName();
                
                if(firstName == null){
                    return 1;
                } 
                
                if(secondName == null){
                    return -1;
                }
                
                return firstName.compareTo(secondName);
            }
        };
        
        Collections.sort(frequentlyReportedDataTypes, sorter);
        Collections.sort(trainingApplicationDataTypes, sorter);
        Collections.sort(otherDataTypes, sorter);
        Collections.sort(systemDataTypes, sorter);
    }
    
    @UiField
    protected Modal reportDialog;
    
    @UiField
    protected Button createReportButton;
    
    @UiField
    protected Tooltip createReportButtonTooltip;
    
    @UiField
    protected HasText reportExperimentName;
    
    @UiField
    protected ComplexPanel frequentlyReportedDataTypeContainer;
    
    @UiField
    protected ComplexPanel trainingAppDataTypeContainer;
    
    @UiField
    protected ComplexPanel otherDataTypeContainer;
    
    @UiField
    protected CheckBox frequentlyReportedCheck;
    
    @UiField
    protected CheckBox trainingAppCheck;
    
    @UiField
    protected CheckBox otherCheck;
    
    @UiField
    protected CheckBox dateTimeCheck;
    
    @UiField
    protected Widget frequentlyReportedPanel;
    
    @UiField
    protected Widget trainingAppPanel;
    
    @UiField
    protected Widget otherPanel;
    
    @UiField
    protected Widget dateTimePanel;
    
    @UiField
    protected Button frSelectAll;
    
    @UiField
    protected Button frSelectNone;
    
    @UiField
    protected Button taSelectAll;
    
    @UiField
    protected Button taSelectNone;
    
    @UiField
    protected Button oSelectAll;
    
    @UiField
    protected Button oSelectNone;
    
    @UiField
    protected Anchor showSystemEventsAnchor;
    
    @UiField
    protected DeckPanel systemDeckPanel;
    
    @UiField
    protected Widget noSystemPanel;
    
    @UiField
    protected ComplexPanel systemDataTypeContainer;
    
    @UiField
    protected CheckBox mergeBySubjectCheck;
    
    @UiField
    protected Button cancelButton;
    
    
    @UiField
    protected Widget serverModeHelp;
    
    
    @UiField
    protected Modal exportReportProgressDialog;
    
    @UiField
    protected Text exportReportProgressDialogHeading;
    
    @UiField
    protected ProgressBarListEntry exportReportOverallProgress;
    
    @UiField
    protected ProgressBarListEntry exportReportSubtaskProgress;
    
    @UiField
    protected Button cancelExportReportProgressButton;
    
    @UiField
    protected TextBox startDateBox;
    
    @UiField
    protected TextBox endDateBox;
    
    @UiField
    protected DatePicker startDatePicker;
    
    @UiField
    protected DatePicker endDatePicker;
    
    @UiField
    protected TimePicker startTimePicker;
    
    @UiField
    protected TimePicker endTimePicker;
    
    private boolean cancelledExportReport = false;
    
    private boolean shouldPollForReportProgress;
    
    /**
     * Create a remote service proxy to talk to the server-side dashboard service.
     */
    private final DashboardServiceAsync dashboardService = GWT.create(DashboardService.class);
    
    /** The experiment for which the report will be geneated */
    private DataCollectionItem currentExperiment;
    
    /** designates whether current experiment is a course */
    private boolean isCourseTile;
    
    /** 
     * Creates a new dialog for creating an experiment report and attaches all its handlers.
     */
    @SuppressWarnings("deprecation")
    public ExperimentBuildReportDialogWidget() {
        initWidget(uiBinder.createAndBindUi(this));
        
        frequentlyReportedCheck.setValue(true);
        
        frequentlyReportedCheck.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                
                frequentlyReportedPanel.setVisible(event.getValue());
            }
        });
        
        trainingAppCheck.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                
                trainingAppPanel.setVisible(event.getValue());
            }
        });
        
        otherCheck.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                
                otherPanel.setVisible(event.getValue());
            }
        });
        
        dateTimeCheck.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                
                dateTimePanel.setVisible(event.getValue());
            }
        });
        
        startDateBox.setReadOnly(true);
        endDateBox.setReadOnly(true);
        startDatePicker.setYearAndMonthDropdownVisible(true);
        endDatePicker.setYearAndMonthDropdownVisible(true);
        
        startDateBox.addClickHandler(new ClickHandler(){
            @Override
            public void onClick(ClickEvent event) {
                startDatePicker.setVisible(!startDatePicker.isVisible());
                
            }
        });
        
        endDateBox.addClickHandler(new ClickHandler(){
            @Override
            public void onClick(ClickEvent event) {
                endDatePicker.setVisible(!endDatePicker.isVisible());
                
            }
        });

        startDatePicker.addValueChangeHandler(new ValueChangeHandler<Date>() {
            @Override
            public void onValueChange(ValueChangeEvent<Date> vce) {
                startDateBox.setText(startDatePicker.getValue().toLocaleString().substring(0, startDatePicker.getValue().toLocaleString().indexOf(',')));
                if(startDateBox.getText().equals(endDateBox.getText())) {
                    long startTime = getStartTime();
                    long endTime = getEndTime();
                    if(startTime > endTime) {
                        UiManager.getInstance().displayErrorDialog(
                                "Start Time Invalid", 
                                "The start time " + 
                                startTimePicker.getValueAsText() + 
                                " can't be after the end time " + 
                                endTimePicker.getValueAsText() +
                                " when the dates are the same.", 
                                null);
                        adjustStartTime();
                    }
                } else if(currentExperiment.getPublishedDate() != null && 
                        ((startDatePicker.getValue().getYear() < currentExperiment.getPublishedDate().getYear()) || 
                        (startDatePicker.getValue().getYear() == currentExperiment.getPublishedDate().getYear() &&
                        startDatePicker.getValue().getMonth() < currentExperiment.getPublishedDate().getMonth()) ||
                        (startDatePicker.getValue().getYear() == currentExperiment.getPublishedDate().getYear() &&
                        startDatePicker.getValue().getMonth() == currentExperiment.getPublishedDate().getMonth() &&
                        startDatePicker.getValue().getDate() < currentExperiment.getPublishedDate().getDate()))){
                    UiManager.getInstance().displayErrorDialog(
                            "Start Date Invalid", 
                            "The start date " + 
                            startDateBox.getText() + 
                            " can't be before the course published date " + 
                            currentExperiment.getPublishedDate().toLocaleString().substring(0, startDatePicker.getValue().toLocaleString().indexOf(',') + 1), 
                            null);
                    adjustStartDate();
                    adjustStartTime();
                    } else if(compareDbTimes()) {
                                            UiManager.getInstance().displayErrorDialog(
                                                "Start Time Invalid", 
                                                "The start time " + startTimePicker.getValueAsText() 
                                                + " can't be before the course creation time " 
                                                + currentExperiment.getPublishedDate().toLocaleString().substring(currentExperiment.getPublishedDate().toLocaleString().indexOf(',') + 2)
                                                + " when the start date is the course creation date.", 
                                                null);
                                            adjustStartTime();
                    } else if(endDatePicker.getValue().compareTo(startDatePicker.getValue()) < 0) {
                        UiManager.getInstance().displayErrorDialog(
                                "Start Date Invalid", 
                                "The start date " + startDateBox.getText() + " can't be after the end date " + endDateBox.getText(), 
                                null);
                        adjustStartDate();
                        if(startDateBox.getText().equals(endDateBox.getText()) && (startTimePicker.getHours() > new Date().getHours() ||
                                (startTimePicker.getHours() == new Date().getHours() && startTimePicker.getMinutes() > new Date().getMinutes()))) {
                                    adjustStartTime();
                                    }
                                }
                    lastStartDate = startDatePicker.getValue();
                    startTimePicker.getHoursBox().setText(twoDigitFormat.format(startTimePicker.getHours()));
                    startTimePicker.getMinutesBox().setText(twoDigitFormat.format(startTimePicker.getMinutes()));
                    endTimePicker.getHoursBox().setText(twoDigitFormat.format(endTimePicker.getHours()));
                    endTimePicker.getMinutesBox().setText(twoDigitFormat.format(endTimePicker.getMinutes()));
                    startDatePicker.setCurrentMonth(startDatePicker.getValue());

                    startDatePicker.addShowRangeHandlerAndFire(new ShowRangeHandler<Date>() {

            			@Override
            			public void onShowRange(ShowRangeEvent<Date> event) {
            				disableStartDateRange();
            }
                    	
        });
            }
        });
        
        endDatePicker.addValueChangeHandler(new ValueChangeHandler<Date>() {
            @Override
            public void onValueChange(ValueChangeEvent<Date> vce) {
                endDateBox.setText(endDatePicker.getValue().toLocaleString().substring(0, endDatePicker.getValue().toLocaleString().indexOf(',')));
                if(compareTimes()) {
                    UiManager.getInstance().displayErrorDialog(
                            "Start Time Invalid", 
                            "The start time " + 
                            startTimePicker.getValueAsText() + 
                            " can't be after the end time " + 
                            endTimePicker.getValueAsText() +
                            " when the dates are the same.", 
                            null);
                    adjustEndTime();
                }  else if(endDatePicker.getValue().compareTo(startDatePicker.getValue()) < 0 &&
                        (currentExperiment.getPublishedDate() != null && startDatePicker.getValue().compareTo(currentExperiment.getPublishedDate()) < 0)) {
                    UiManager.getInstance().displayErrorDialog(
                            "End Date Invalid", 
                            "The end date " + 
                            endDateBox.getText() + 
                            " can't be before the start date " + 
                            startDateBox.getText(), 
                            null);
                    adjustEndDate();
                    }
                else if(endDatePicker.getValue().getYear() < startDatePicker.getValue().getYear() ||
                        (endDatePicker.getValue().getYear() == startDatePicker.getValue().getYear() &&
                                endDatePicker.getValue().getMonth() < startDatePicker.getValue().getMonth()) ||
                        (endDatePicker.getValue().getYear() == startDatePicker.getValue().getYear() &&
                                endDatePicker.getValue().getMonth() == startDatePicker.getValue().getMonth() &&
                                endDatePicker.getValue().getDate() < startDatePicker.getValue().getDate())) {
                    UiManager.getInstance().displayErrorDialog(
                            "End Date Invalid", 
                            "The end date " + 
                            endDateBox.getText() + 
                            " can't be before the start date " + 
                            startDateBox.getText(), 
                            null);
                    adjustEndDate();
                }
                startDatePicker.setCurrentMonth(startDatePicker.getValue());
                lastEndDate = endDatePicker.getValue();
                startTimePicker.getHoursBox().setText(twoDigitFormat.format(startTimePicker.getHours()));
                startTimePicker.getMinutesBox().setText(twoDigitFormat.format(startTimePicker.getMinutes()));
                endTimePicker.getHoursBox().setText(twoDigitFormat.format(endTimePicker.getHours()));
                endTimePicker.getMinutesBox().setText(twoDigitFormat.format(endTimePicker.getMinutes()));
                endDatePicker.setCurrentMonth(endDatePicker.getValue());
                                
                endDatePicker.addShowRangeHandlerAndFire(new ShowRangeHandler<Date>() {

        			@Override
        			public void onShowRange(ShowRangeEvent<Date> event) {
        				disableEndDateRange();
            }
                	
        });
            }
        });
        
                
        startTimePicker.getHoursBox().addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> vce) {
                if(StringUtils.isBlank(startTimePicker.getHoursBox().getText())) {
                    UiManager.getInstance().displayErrorDialog(
                            "Start Time Invalid", 
                            "The start time must have a value for hours.", 
                            null);
                    timeError = true;
                    adjustStartTime();
                } else if(compareDbTimes()) {
                                        UiManager.getInstance().displayErrorDialog(
                                            "Start Time Invalid", 
                                            "The start time " + startTimePicker.getValueAsText() 
                                            + " can't be before the course creation time " 
                                            + currentExperiment.getPublishedDate().toLocaleString().substring(currentExperiment.getPublishedDate().toLocaleString().indexOf(',') + 2)
                                            + " when the start date is the course creation date.", 
                                            null);
                                        timeError = true;
                                        adjustStartTime();
                } else if(compareTimes()) {
                    UiManager.getInstance().displayErrorDialog(
                            "Start Time Invalid", 
                            "The start time " + startTimePicker.getValueAsText() + " can't be after the end time " +
                             endTimePicker.getValueAsText() + " when the dates are the same.", 
                            null);
                    timeError = true;
                    adjustStartTime();
                } else {
                    timeError = false;
                }
                startTimePicker.getHoursBox().setText(twoDigitFormat.format(startTimePicker.getHours()));
                startTimePicker.getMinutesBox().setText(twoDigitFormat.format(startTimePicker.getMinutes()));
                lastStartHours = startTimePicker.getHours();
            }
        });
        
        startTimePicker.getMinutesBox().addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> vce) {
                if(StringUtils.isBlank(startTimePicker.getMinutesBox().getText())) {
                    UiManager.getInstance().displayErrorDialog(
                            "Start Time Invalid", 
                            "The start time must have a value for minutess.", 
                            null);
                    timeError = true;
                    adjustStartTime();
                } else if(compareDbTimes()) {
                                        UiManager.getInstance().displayErrorDialog(
                                            "Start Time Invalid", 
                                            "The start time " + startTimePicker.getValueAsText() 
                                            + " can't be before the course creation time " 
                                            + currentExperiment.getPublishedDate().toLocaleString().substring(currentExperiment.getPublishedDate().toLocaleString().indexOf(',') + 2)
                                            + " when the start date is the course creation date.", 
                                            null);
                                        timeError = true;
                                        adjustStartTime();
                } else if(compareTimes()) {
                    UiManager.getInstance().displayErrorDialog(
                            "Start Time Invalid", 
                            "The start time " + startTimePicker.getValueAsText() + " can't be after the end time " +
                             endTimePicker.getValueAsText() + " when the dates are the same.", 
                            null);
                    timeError = true;
                    adjustStartTime();
                } else {
                    timeError = false;
                }
                startTimePicker.getHoursBox().setText(twoDigitFormat.format(startTimePicker.getHours()));
                startTimePicker.getMinutesBox().setText(twoDigitFormat.format(startTimePicker.getMinutes()));
                lastStartMinutes = startTimePicker.getMinutes();
            }
        });
        
        startTimePicker.getHoursUpButton().addClickHandler(new ClickHandler(){
            @Override
            public void onClick(ClickEvent ce) {
                if(compareTimes()) {
                    UiManager.getInstance().displayErrorDialog(
                            "Start Time Invalid", 
                            "The start time " + startTimePicker.getValueAsText() 
                            + " can't be after the end time " + endTimePicker.getValueAsText()
                            +   " when the dates are the same.", 
                            null);
                    adjustStartTime();
                }
                startTimePicker.getHoursBox().setText(twoDigitFormat.format(startTimePicker.getHours()));
                startTimePicker.getMinutesBox().setText(twoDigitFormat.format(startTimePicker.getMinutes()));
                lastStartHours = startTimePicker.getHours();
            }
        });
        
        startTimePicker.getMinutesUpButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent ce) {
                 if(compareTimes()) {
                            UiManager.getInstance().displayErrorDialog(
                                    "Start Time Invalid", 
                                    "The start time " + startTimePicker.getValueAsText() + " can't be after the end time " +
                                    endTimePicker.getValueAsText() + " when the dates are the same.", 
                                    null);
                            adjustStartTime();
                }
                 startTimePicker.getHoursBox().setText(twoDigitFormat.format(startTimePicker.getHours()));
                 startTimePicker.getMinutesBox().setText(twoDigitFormat.format(startTimePicker.getMinutes()));
                 lastStartMinutes = startTimePicker.getMinutes();
            }
        });
        
        startTimePicker.getHoursDownButton().addClickHandler(new ClickHandler(){
            @Override
            public void onClick(ClickEvent ce) {
                if(compareDbTimes()) {
                                    UiManager.getInstance().displayErrorDialog(
                                            "Start Time Invalid", 
                                            "The start time " + startTimePicker.getValueAsText() 
                                            + " can't be before the course creation time " 
                                            + currentExperiment.getPublishedDate().toLocaleString().substring(currentExperiment.getPublishedDate().toLocaleString().indexOf(',') + 2)
                                            + " when the start date is the course creation date.", 
                                            null);
                                    adjustStartTime();
                } else if(compareTimes()) {
                    UiManager.getInstance().displayErrorDialog(
                            "Start Time Invalid", 
                            "The start time " + startTimePicker.getValueAsText() 
                            + " can't be after the end time " + endTimePicker.getValueAsText()
                            +   " when the dates are the same.", 
                            null);
                    adjustStartTime();
                }
                startTimePicker.getHoursBox().setText(twoDigitFormat.format(startTimePicker.getHours()));
                startTimePicker.getMinutesBox().setText(twoDigitFormat.format(startTimePicker.getMinutes()));
                lastStartHours = startTimePicker.getHours();
            }
        });
        
        startTimePicker.getMinutesDownButton().addClickHandler(new ClickHandler(){
            @Override
            public void onClick(ClickEvent ce) {
                if(compareDbTimes()) {
                                        UiManager.getInstance().displayErrorDialog(
                                                "Start Time Invalid", 
                                                "The start time " + startTimePicker.getValueAsText() 
                                                + " can't be before the course creation time " 
                                            + currentExperiment.getPublishedDate().toLocaleString().substring(currentExperiment.getPublishedDate().toLocaleString().indexOf(',') + 2)
                                            + " when the start date is the course creation date.", 
                                                null);
                                        adjustStartTime();
                } else if(compareTimes()) {
                    UiManager.getInstance().displayErrorDialog(
                            "Start Time Invalid", 
                            "The start time " + startTimePicker.getValueAsText() 
                            + " can't be after the end time " + endTimePicker.getValueAsText()
                            +   " when the dates are the same.", 
                            null);
                    adjustStartTime();
                }
                startTimePicker.getHoursBox().setText(twoDigitFormat.format(startTimePicker.getHours()));
                startTimePicker.getMinutesBox().setText(twoDigitFormat.format(startTimePicker.getMinutes()));
                lastStartMinutes = startTimePicker.getMinutes();
            }
        });
        
        startTimePicker.getSwitch().addClickHandler(new ClickHandler(){
            @Override
            public void onClick(ClickEvent ce) {
                if(compareDbTimes()) {
                    UiManager.getInstance().displayErrorDialog(
                            "Start Time Invalid", 
                                        "The start time " + startTimePicker.getValueAsText() 
                                        + " can't be before the course creation time " 
                                        + currentExperiment.getPublishedDate().toLocaleString().substring(currentExperiment.getPublishedDate().toLocaleString().indexOf(',') + 2)
                                        + " when the start date is the course creation date.", 
                                        null);
                                    adjustStartTime();
                } else if(compareTimes()) {
                    UiManager.getInstance().displayErrorDialog(
                            "Start Time Invalid", 
                            "The start time " + startTimePicker.getValueAsText() + " can't be after the end time " +
                            endTimePicker.getValueAsText() + " when the dates are the same.",  
                            null);
                    adjustStartTime();
                } 
                startTimePicker.getHoursBox().setText(twoDigitFormat.format(startTimePicker.getHours()));
                startTimePicker.getMinutesBox().setText(twoDigitFormat.format(startTimePicker.getMinutes()));
                lastStartTimeSwitchLabel = startTimePicker.getSwitch().getText();
            }
        });
        
        endTimePicker.getHoursBox().addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> vce) {
                if(StringUtils.isBlank(endTimePicker.getHoursBox().getText())) {
                    UiManager.getInstance().displayErrorDialog(
                            "End Time Invalid", 
                            "The end time must have a value for hours.", 
                            null);
                    timeError = true;
                    adjustEndTime();
                } else if(compareTimes()) {
                    UiManager.getInstance().displayErrorDialog(
                            "End Time Invalid", 
                            "The end time " + endTimePicker.getValueAsText() + " can't be before the start time " +
                            startTimePicker.getValueAsText() + " when the dates are the same.",   
                            null);
                    timeError = true;
                    adjustEndTime();
                } else if(startDateBox.getText().equals(endDateBox.getText()) && (endTimePicker.getHours() > 12 || endTimePicker.getMinutes() > 59)) {
                    timeError = true;
                    adjustEndTime();
                } else {
                    timeError = false;
                }
                endTimePicker.getHoursBox().setText(twoDigitFormat.format(endTimePicker.getHours()));
                endTimePicker.getMinutesBox().setText(twoDigitFormat.format(endTimePicker.getMinutes()));
                lastEndHours = endTimePicker.getHours();
            }
        });
        
        endTimePicker.getMinutesBox().addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> vce) {
                if(StringUtils.isBlank(endTimePicker.getMinutesBox().getText())) {
                    UiManager.getInstance().displayErrorDialog(
                            "End Time Invalid", 
                            "The end time must have a value for minutes.", 
                            null);
                    timeError = true;
                    adjustEndTime();
                } else if(compareTimes()) {
                    UiManager.getInstance().displayErrorDialog(
                            "End Time Invalid", 
                            "The end time " + endTimePicker.getValueAsText() + " can't be before the start time " +
                            startTimePicker.getValueAsText() + " when the dates are the same.",   
                            null);
                    timeError = true;
                    adjustEndTime();
                } else if(startDateBox.getText().equals(endDateBox.getText()) && (endTimePicker.getHours() > 12 || endTimePicker.getMinutes() > 59)) {
                    timeError = true;
                    adjustEndTime();
                } else {
                    timeError = false;
                }
                endTimePicker.getHoursBox().setText(twoDigitFormat.format(endTimePicker.getHours()));
                endTimePicker.getMinutesBox().setText(twoDigitFormat.format(endTimePicker.getMinutes()));
                lastEndMinutes = endTimePicker.getMinutes();
            }
        });
        
        endTimePicker.getHoursDownButton().addClickHandler(new ClickHandler(){
            @Override
            public void onClick(ClickEvent ce) {
                if(compareTimes()) {
                    UiManager.getInstance().displayErrorDialog(
                            "End Time Invalid", 
                            "The start time " + startTimePicker.getValueAsText() + " can't be after the end time " +
                            endTimePicker.getValueAsText() + " when the dates are the same.", 
                            null);
                    adjustEndTime();
                }
                endTimePicker.getHoursBox().setText(twoDigitFormat.format(endTimePicker.getHours()));
                endTimePicker.getMinutesBox().setText(twoDigitFormat.format(endTimePicker.getMinutes()));
                lastEndHours = endTimePicker.getHours();
            }
        });
        
        endTimePicker.getHoursUpButton().addClickHandler(new ClickHandler(){
            @Override
            public void onClick(ClickEvent ce) {
                if(compareTimes()) {
                    UiManager.getInstance().displayErrorDialog(
                            "End Time Invalid", 
                            "The start time " + startTimePicker.getValueAsText() + " can't be after the end time " +
                            endTimePicker.getValueAsText() + " when the dates are the same.", 
                            null);
                    adjustEndTime();
                }
                endTimePicker.getHoursBox().setText(twoDigitFormat.format(endTimePicker.getHours()));
                endTimePicker.getMinutesBox().setText(twoDigitFormat.format(endTimePicker.getMinutes()));
                lastEndHours = endTimePicker.getHours();
            }
        });
        
        endTimePicker.getMinutesUpButton().addClickHandler(new ClickHandler(){
            @Override
            public void onClick(ClickEvent ce) {
                if(compareTimes()) {
                    UiManager.getInstance().displayErrorDialog(
                            "End Time Invalid", 
                            "The start time " + startTimePicker.getValueAsText() + " can't be after the end time " +
                            endTimePicker.getValueAsText() + " when the dates are the same.", 
                            null);
                    adjustEndTime();
                }
                endTimePicker.getHoursBox().setText(twoDigitFormat.format(endTimePicker.getHours()));
                endTimePicker.getMinutesBox().setText(twoDigitFormat.format(endTimePicker.getMinutes()));
                lastEndMinutes = endTimePicker.getMinutes();
            }
        });
        
        endTimePicker.getMinutesDownButton().addClickHandler(new ClickHandler(){
            @Override
            public void onClick(ClickEvent ce) {
                if(compareTimes()) {
                            UiManager.getInstance().displayErrorDialog(
                                    "End Time Invalid", 
                                    "The start time " + startTimePicker.getValueAsText() + " can't be after the end time " +
                                    endTimePicker.getValueAsText() + " when the dates are the same.", 
                                    null);
                            adjustEndTime();
                }
                endTimePicker.getHoursBox().setText(twoDigitFormat.format(endTimePicker.getHours()));
                endTimePicker.getMinutesBox().setText(twoDigitFormat.format(endTimePicker.getMinutes()));
                lastEndMinutes = endTimePicker.getMinutes();
            }
        });
        
        endTimePicker.getSwitch().addClickHandler(new ClickHandler(){
            @Override
            public void onClick(ClickEvent ce) {
                if(compareTimes()) {
                    UiManager.getInstance().displayErrorDialog(
                            "End Time Invalid", 
                            "The end time " + endTimePicker.getValueAsText() + " can't be before the start time " +
                            startTimePicker.getValueAsText() + " when the dates are the same.",    
                            null);
                    adjustEndTime();
                    }
                endTimePicker.getHoursBox().setText(twoDigitFormat.format(endTimePicker.getHours()));
                endTimePicker.getMinutesBox().setText(twoDigitFormat.format(endTimePicker.getMinutes()));
                lastEndTimeSwitchLabel = endTimePicker.getSwitch().getText();
                }
            });

        //populate the UI with the data types to select from
        for(DataTypeEntry type : frequentlyReportedDataTypes){
            frequentlyReportedDataTypeContainer.add(new ReportDataTypeWidget(type));
        }
        
        for(DataTypeEntry type : trainingApplicationDataTypes){        
            trainingAppDataTypeContainer.add(new ReportDataTypeWidget(type));
        }
        
        for(DataTypeEntry type : otherDataTypes){            
            otherDataTypeContainer.add(new ReportDataTypeWidget(type));
        }
        
        for(DataTypeEntry type : systemDataTypes){                        
            systemDataTypeContainer.add(new ReportDataTypeWidget(type));
        }
        
        createReportButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                if(!timeError) {
                                
                //gather report properties from the UI
                List<EventType> eventsToReport = new ArrayList<EventType>();
                
                if(frequentlyReportedCheck.getValue()){
                    
                    for(DataTypeEntry dataType : getAllSelectedEntries(frequentlyReportedDataTypeContainer)){
                        eventsToReport.add(new EventType(dataType.getName(), dataType.getDisplayName(), dataType.getDescription()));
                    }
                }
                
                if(trainingAppCheck.getValue()){
                    
                    for(DataTypeEntry dataType : getAllSelectedEntries(trainingAppDataTypeContainer)){
                        eventsToReport.add(new EventType(dataType.getName(), dataType.getDisplayName(), dataType.getDescription()));
                    }
                }

                if(otherCheck.getValue()){
                    
                    for(DataTypeEntry dataType : getAllSelectedEntries(otherDataTypeContainer)){
                        eventsToReport.add(new EventType(dataType.getName(), dataType.getDisplayName(), dataType.getDescription()));
                    }
                }
                                
                if(!dateTimeCheck.getValue()) {
                    EventReportColumn.TIME_COLUMN.setTimeConstraints(null, null);
                } else {
                    if(startDateBox.getText().equals(endDateBox.getText()) && getStartTime() == getEndTime()) {
                        UiManager.getInstance().displayErrorDialog(
                                "Start and End Time are the Same", 
                                "Please change one of these values.", 
                                null);
                            return;                    
                        } else {
                        EventReportColumn.TIME_COLUMN.setTimeConstraints((startDatePicker.getValue().getTime() - getStartDbTime()) + getStartTime(), (endDatePicker.getValue().getTime() - getEndDifference()) + getEndTime());
  
                    }
                }
                
                if(systemDataTypeContainer.isVisible()){
                    
                    for(DataTypeEntry dataType : getAllSelectedEntries(systemDataTypeContainer)){
                        eventsToReport.add(new EventType(dataType.getName(), dataType.getDisplayName(), dataType.getDescription()));
                    }
                }                
                
                if(eventsToReport.isEmpty()){
                    
                    UiManager.getInstance().displayErrorDialog(
                            "No Event Types Selected", 
                            "Please select at least one type of event to be included in the report.", 
                            null);
                    
                    return;
                }
                
                boolean mergeBySubject = mergeBySubjectCheck.getValue();
                
                //create the report properties
                //dummy value because ReportProperties errors on empty sourceID.  Files will be added by server later
                List<Integer> sources = new ArrayList<Integer>();
                sources.add(1);
                
                ArrayList<EventReportColumn> reportColumns = new ArrayList<EventReportColumn>();
                
                if(isCourseTile && !mergeBySubject) {
                    reportColumns.add(EventReportColumn.COURSE_ATTEMPT_COL);
                }
                reportColumns.add(EventReportColumn.TIME_COLUMN);
                reportColumns.add(EventReportColumn.EVENT_TYPE_COLUMN);
                reportColumns.add(EventReportColumn.USER_ID_COLUMN);
                reportColumns.add(EventReportColumn.USERNAME_COLUMN);
                reportColumns.add(EventReportColumn.CONTENT_COLUMN);
                reportColumns.add(EventReportColumn.DS_ID_COLUMN);
                reportColumns.add(EventReportColumn.DS_TIME_COLUMN);
                reportColumns.add(EventReportColumn.DKF_TIME_COLUMN);
                                
                ReportProperties properties = new ReportProperties(
                        UiManager.getInstance().getUserName(),
                        sources, 
                        eventsToReport, 
                        reportColumns, 
                        ReportProperties.DEFAULT_EMPTY_CELL, 
                        ReportProperties.DEFAULT_FILENAME);

                //enable all eventTypes listed
                for (EventType eventType : eventsToReport) {
                    properties.setSelected(eventType, true);
                }
                
                if(mergeBySubject){
                    properties.setMergeByColumn(EventReportColumn.USER_ID_COLUMN);
                }
                
                cancelExportReportProgressButton.setEnabled(true);
                    
                cancelledExportReport = false;
                
                exportReportProgressDialogHeading.setText("Generating Report...");
                exportReportOverallProgress.updateProgress(new ProgressIndicator(0, "Initializing"));
                exportReportSubtaskProgress.updateProgress(new ProgressIndicator(0, "Initializing"));
                
                //generate the report
                dashboardService.exportExperimentReport(UiManager.getInstance().getUserName(), currentExperiment.getId(), properties, new AsyncCallback<DetailedRpcResponse>() {
                    
                    @Override
                    public void onSuccess(DetailedRpcResponse response) {
                
                        if(response.isSuccess()){
                            
                            //begin polling for progress
                            shouldPollForReportProgress = true;
                            
                            maybePollForReportProgress();    
                            
                        } else {
                            
                            exportReportProgressDialog.hide();        
                            
                            if(response.getErrorMessage() == null){
                                UiManager.getInstance().displayErrorDialog("Export Report Failed", "An error occurred while generating the report.", null);
                                
                            } else {
                                
                                if(response.getErrorDetails() == null){
                                    UiManager.getInstance().displayErrorDialog("Export Report Failed", "An error occurred while generating the report: " + response.getErrorMessage(), null);
                                    
                                } else {
                                    UiManager.getInstance().displayDetailedErrorDialog(
                                            "Export Report Failed", 
                                            "An error occurred while generating the report: " + response.getErrorMessage(), 
                                            response.getErrorDetails(), 
											response.getErrorStackTrace(),
											null);
                                }
                            }
                        }
                    }
                    
                    @Override
                    public void onFailure(Throwable thrown) {
                        
                        shouldPollForReportProgress = false;
                        exportReportProgressDialog.hide();
                        
                        if(!cancelledExportReport){
                        
                            UiManager.getInstance().displayErrorDialog("Export Report Failed", "An error occurred while generating the report: " + thrown.toString() +".", null);
                    
                        } else {                                
                            //export was cancelled, so no need to do anything except hide the export progress dialog
                            
                            cancelledExportReport = false;
                        }
                    }
                });
                
                exportReportProgressDialog.show();                
                } else {
                    timeError = false;
            }
            }
        });
        
        cancelExportReportProgressButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                cancelExportReportProgressButton.setEnabled(false);
                
                cancelledExportReport = true;
                
                exportReportProgressDialogHeading.setText("Cancelling Report Generation...");
                
                dashboardService.cancelExportExperimentReport(UiManager.getInstance().getUserName(), new AsyncCallback<RpcResponse>() {

                    @Override
                    public void onFailure(Throwable e) {
                        UiManager.getInstance().displayErrorDialog("Cancel Report Generation Failed", "An error occurred while cancelling the raw data export: " + e.toString() +".", null);
                    }

                    @Override
                    public void onSuccess(RpcResponse response) {
                        
                        if(response.isSuccess()){
                            
                            exportReportProgressDialog.hide();
                            shouldPollForReportProgress = false;
                            
                        } else {
                            if(response.getResponse() == null){
                                UiManager.getInstance().displayErrorDialog("Cancel Report Generation Failed", "An error occurred while cancelling the raw data export.", null);
                                
                            } else {
                                UiManager.getInstance().displayErrorDialog("Cancel Report Generation Failed", "An error occurred while cancelling the raw data export: " + response.getResponse(), null);
                            }
                        }
                    }
                    
                });
            }
        });
        
        frSelectAll.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                setAllEntriesSelected(frequentlyReportedDataTypeContainer, true);
            }
        });
        
        frSelectNone.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                setAllEntriesSelected(frequentlyReportedDataTypeContainer, false);
            }
        });
        
        taSelectAll.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                setAllEntriesSelected(trainingAppDataTypeContainer, true);
            }
        });
        
        taSelectNone.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                setAllEntriesSelected(trainingAppDataTypeContainer, false);
            }
        });
        
        oSelectAll.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                setAllEntriesSelected(otherDataTypeContainer, true);
                
                if(systemDataTypeContainer.isVisible()){
                    setAllEntriesSelected(systemDataTypeContainer, true);
                }
            }
        });

        oSelectNone.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                setAllEntriesSelected(otherDataTypeContainer, false);
                
                if(systemDataTypeContainer.isVisible()){
                    setAllEntriesSelected(systemDataTypeContainer, false);
                }
            }
        });
        
        systemDeckPanel.showWidget(systemDeckPanel.getWidgetIndex(noSystemPanel));
        
        showSystemEventsAnchor.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                if(systemDataTypeContainer.isVisible()){
                    
                    systemDeckPanel.showWidget(systemDeckPanel.getWidgetIndex(noSystemPanel));
                    showSystemEventsAnchor.setText("Show system events");
                    
                } else {
                    
                    systemDeckPanel.showWidget(systemDeckPanel.getWidgetIndex(systemDataTypeContainer));
                    showSystemEventsAnchor.setText("Hide system events");
                }
            }
        });
        
        cancelButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent arg0) {
                hide();
            }
        });
        
        if(DeploymentModeEnum.SERVER.equals(UiManager.getInstance().getDeploymentMode())){
            serverModeHelp.setVisible(true);
        }
    }
    
    /**
     * Selects or deselects all the data types in the given widget container
     * 
     * @param entryContainer the container holding the data types
     * @param selected whether to select or delselect all the data types
     */
    private void setAllEntriesSelected(ComplexPanel entryContainer, boolean selected){
        
        for(int i = 0; i < entryContainer.getWidgetCount(); i++){
            
            Widget widget = entryContainer.getWidget(i);
            
            if(widget instanceof ReportDataTypeWidget){
                ((ReportDataTypeWidget) widget).setValue(selected);
            }
        }
    }
    
    /**
     * Gets all the selected data types in the given widget container
     * 
     * @param entryContainer the container holding the data types
     * @return the selected data types
     */
    private List<DataTypeEntry> getAllSelectedEntries(ComplexPanel entryContainer){
        
        List<DataTypeEntry> selectedEntries = new ArrayList<DataTypeEntry>();
        
        for(int i = 0; i < entryContainer.getWidgetCount(); i++){
            
            Widget widget = entryContainer.getWidget(i);
            
            if(widget instanceof ReportDataTypeWidget && ((ReportDataTypeWidget) widget).getValue()){
                selectedEntries.add(((ReportDataTypeWidget) widget).getDataTypeEntry());
            }
        }
        
        return selectedEntries;
    }

    /**
     * Shows the dialog and updates its experiment
     * 
     * @param experiment
     */
    @SuppressWarnings("deprecation")
    public void show(DataCollectionItem experiment){
        
        if(experiment == null){
            throw new IllegalArgumentException("The published course to generate a report for cannot be null");
        }
        
        currentExperiment = experiment;
        
        reportExperimentName.setText(currentExperiment.getName() != null ? currentExperiment.getName() : "this published course");    
        
        //Enables or disables the button based on the number of subjects
        boolean hasData = false;
        if (currentExperiment.isDataSetType(DataSetType.EXPERIMENT) || currentExperiment.isDataSetType(DataSetType.COURSE_DATA)) {
            hasData = currentExperiment.getSubjectSize() != 0;
            if(currentExperiment.isDataSetType(DataSetType.COURSE_DATA)) {
                isCourseTile = true;
            } else {
                isCourseTile = false;
            }
        } else if (currentExperiment.isDataSetType(DataSetType.LTI)) {
            hasData = currentExperiment.getLtiResultSize() != 0;
        } else {
            logger.severe("Unsupported published course type encountered when showing the build report dialog: " + experiment);
        }
        
        if(!hasData) {
            createReportButton.setEnabled(false);
            createReportButtonTooltip.setTitle(NO_SUBJECTS_TOOL_TIP_TEXT);
        } else {
            createReportButton.setEnabled(true);
            createReportButtonTooltip.setTitle("");
        }
        
        endDatePicker.setValue(new Date());
        lastEndDate = endDatePicker.getValue();
        endDateBox.setText(new Date().toLocaleString().substring(0, new Date().toLocaleString().indexOf(',')));
        endDatePicker.setCurrentMonth(endDatePicker.getValue());
        
        if(new Date().getHours() > 12) {
            endTimePicker.setHours(new Date().getHours() - 12);
            endTimePicker.getSwitch().setText("PM");
        } else if(new Date().getHours() == 12) {
            endTimePicker.setHours(new Date().getHours());
            endTimePicker.getSwitch().setText("PM");
        } else if(new Date().getHours() == 0) {
            endTimePicker.setHours(12);
            startTimePicker.getSwitch().setText("AM");
        } else {
            endTimePicker.setHours(new Date().getHours());  
            startTimePicker.getSwitch().setText("AM");
        }
        endTimePicker.setMinutes(new Date().getMinutes());
        endTimePicker.getHoursBox().setText(twoDigitFormat.format(endTimePicker.getHours()));
        endTimePicker.getMinutesBox().setText(twoDigitFormat.format(endTimePicker.getMinutes()));
        lastEndHours = endTimePicker.getHours();
        lastEndMinutes = endTimePicker.getMinutes();
        lastEndTimeSwitchLabel = endTimePicker.getSwitch().getText();
        
        endDatePicker.addShowRangeHandlerAndFire(new ShowRangeHandler<Date>() {

			@Override
			public void onShowRange(ShowRangeEvent<Date> event) {
				disableEndDateRange();
			}
        	
        });
        
        if(currentExperiment.getPublishedDate() != null) {
            startDatePicker.setValue(currentExperiment.getPublishedDate());
            startDateBox.setText(startDatePicker.getValue().toLocaleString().substring(0, startDatePicker.getValue().toLocaleString().indexOf(',')));
            startDatePicker.setCurrentMonth(startDatePicker.getValue());

            if(currentExperiment.getPublishedDate().getHours() > 12) {
                startTimePicker.setHours(currentExperiment.getPublishedDate().getHours() - 12);
                startTimePicker.getSwitch().setText("PM");
            } else if(currentExperiment.getPublishedDate().getHours() == 12) {
                startTimePicker.setHours(currentExperiment.getPublishedDate().getHours());
                startTimePicker.getSwitch().setText("PM");
            } else if(currentExperiment.getPublishedDate().getHours() == 0) {
                startTimePicker.setHours(12);
                startTimePicker.getSwitch().setText("AM");
            } else {
                startTimePicker.setHours(currentExperiment.getPublishedDate().getHours());
                startTimePicker.getSwitch().setText("AM");
            }
            startTimePicker.setMinutes(currentExperiment.getPublishedDate().getMinutes());
        } else {
            startDatePicker.setValue(new Date());
            startDateBox.setText(startDatePicker.getValue().toLocaleString().substring(0, startDatePicker.getValue().toLocaleString().indexOf(',')));
            startDatePicker.setCurrentMonth(startDatePicker.getValue());

            if(new Date().getHours() > 12) {
                startTimePicker.setHours(new Date().getHours() - 12);
                startTimePicker.getSwitch().setText("PM");
            } else if(new Date().getHours() == 12) {
                startTimePicker.setHours(new Date().getHours());
                startTimePicker.getSwitch().setText("PM");
            } else if(new Date().getHours() == 0) {
                startTimePicker.setHours(12);
                startTimePicker.getSwitch().setText("AM");
            } else {
                startTimePicker.setHours(new Date().getHours());  
                startTimePicker.getSwitch().setText("AM");
            }
            startTimePicker.setMinutes(new Date().getMinutes());
        }
        startTimePicker.getHoursBox().setText(twoDigitFormat.format(startTimePicker.getHours()));
        startTimePicker.getMinutesBox().setText(twoDigitFormat.format(startTimePicker.getMinutes()));
        
        lastStartDate = startDatePicker.getValue();
        lastStartHours = startTimePicker.getHours();
        lastStartMinutes = startTimePicker.getMinutes();
        lastStartTimeSwitchLabel = startTimePicker.getSwitch().getText();
        
        startDatePicker.addShowRangeHandlerAndFire(new ShowRangeHandler<Date>() {

			@Override
			public void onShowRange(ShowRangeEvent<Date> event) {
				disableStartDateRange();
			}
        	
        });
        
        reportDialog.show();
    }
    
    /**
     * Hides the dialog and resets its fields
     */
    public void hide(){
        
        reportDialog.hide();
        
        setAllEntriesSelected(frequentlyReportedDataTypeContainer, false);
        setAllEntriesSelected(trainingAppDataTypeContainer, false);
        setAllEntriesSelected(otherDataTypeContainer, false);
        setAllEntriesSelected(systemDataTypeContainer, false);
        
        frequentlyReportedCheck.setValue(true, true);
        trainingAppCheck.setValue(false, true);
        otherCheck.setValue(false, true);
        dateTimeCheck.setValue(false, true);
        
        startDatePicker.setValue(new Date());
        lastStartDate = null;
        endDatePicker.setValue(new Date());
        lastEndDate = null;
        startDatePicker.setCurrentMonth(startDatePicker.getValue());
        endDatePicker.setCurrentMonth(endDatePicker.getValue());
        
        startTimePicker.setHours(0);
        startTimePicker.setMinutes(0);
        endTimePicker.setHours(0);
        endTimePicker.setMinutes(0);
        startDateBox.setText("");
        endDateBox.setText("");
        
        startDatePicker.setVisible(false);
        endDatePicker.setVisible(false);
        
        systemDeckPanel.showWidget(systemDeckPanel.getWidgetIndex(noSystemPanel));        
        
        mergeBySubjectCheck.setValue(false);
    }
    
    /**
     * An abstract representation of a data type that can be displayed in a report dialog
     * 
     * @author nroberts
     */
    public static class DataTypeEntry{
        
        /** A unique name for the data type */
        private String name;
        
        /** The name for this data type that will be displayed back to the user */
        private String displayName;
        
        /** A description for this data type */
        private String description;
        
        /** 
         * Creates a new data type entry
         */
        public DataTypeEntry(String name, String displayName, String description){
            this.name = name;
            this.displayName = displayName;
            this.description = description;
        }

        /**
         * Gets this data type's name
         * 
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * Gets this data type's display name
         * 
         * @return the displayName
         */
        public String getDisplayName() {
            return displayName;
        }

        /**
         * Gets this data type's description
         * 
         * @return the description
         */
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * Polls the server for the progress of the current report generation until the user cancels
     */
    private void maybePollForReportProgress(){
        
        if(shouldPollForReportProgress){
        
            dashboardService.getExportExperimentReportProgress(UiManager.getInstance().getUserName(), new AsyncCallback<GenerateReportStatusResponse>() {
    
                @Override
                public void onFailure(Throwable e) {
    
                    maybePollForReportProgress();                    
                }
    
                @Override
                public void onSuccess(GenerateReportStatusResponse response) {
                    
                    if(response.getStatus() != null){
                        
                        GenerateReportStatus status = response.getStatus();
                        
                        if(status.getReportResult() != null || status.getException() != null){
                            
                            shouldPollForReportProgress = false;
                            
                            if(!cancelledExportReport){
                                
                                if(status.getException() != null){
                                
                                    exportReportProgressDialog.hide();
                                    
                                    DetailedException e = status.getException();
                                
                                    UiManager.getInstance().displayDetailedErrorDialog(
                                            "Report Generation Failed", 
                                            e.getReason(), 
                                            e.getDetails(), 
											e.getErrorStackTrace(),
											null);
                                
                                } else if(status.getReportResult() != null){
                                    
                                    final DownloadableFileRef result = status.getReportResult();            
                                    
                                    exportReportProgressDialog.hide();        
                                    
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
                                
                                } 
                                
                                dashboardService.cancelExportExperimentReport(UiManager.getInstance().getUserName(), new AsyncCallback<RpcResponse>() {
                                    
                                    @Override
                                    public void onSuccess(RpcResponse response) {
                                        //Nothing to do
                                    }
                                    
                                    @Override
                                    public void onFailure(Throwable thrown) {
                                        /* 
                                         * Do Nothing. Cleaning up after a report is handled silently, therefore errors 
                                         * should be handled on the server.
                                         */        
                                    }
                                });
                                
                            } else {                                
                                
                                exportReportProgressDialog.hide();
                                
                                cancelledExportReport = false;
                            }
                            
                        } else if(status.getProgress() != null){
                            
                            ProgressIndicator progress = status.getProgress();
                            exportReportOverallProgress.updateProgress(progress);
                            

                            if(progress.getSubtaskProcessIndicator() != null){
                                exportReportSubtaskProgress.updateProgress(progress.getSubtaskProcessIndicator());
                            }
                        }
                    }
    
                    maybePollForReportProgress();                    
                }
            });
            
        }
    }
    
    /**
     * Get the start time stored in the db
     * @return the time, in milliseconds, stored in the db for the published course
     */
    @SuppressWarnings("deprecation")
    private long getStartDbTime() {
        return (startDatePicker.getValue().getHours() * MILLISECONDS_IN_HOUR) + (startDatePicker.getValue().getMinutes() * MILLISECONDS_IN_MINUTE) + (startDatePicker.getValue().getSeconds() * 1000);
    }
    
    /** 
     * Get the end time stored
     * @return the time, in milliseconds, stored in this date object
     */
    @SuppressWarnings("deprecation")
    private long getEndDifference() {
        return (endDatePicker.getValue().getHours() * MILLISECONDS_IN_HOUR) + (endDatePicker.getValue().getMinutes() * MILLISECONDS_IN_MINUTE) + (endDatePicker.getValue().getSeconds() * 1000);
    }
    
    /** 
     * Get the start time in epoch form
     * @return the time, in milliseconds, stored in the start date and start time pickers
     */
    private long getStartTime() {
        if(!startTimePicker.isAM() && startTimePicker.getHours() != 12) {
            return ((startTimePicker.getHours() + 12) * MILLISECONDS_IN_HOUR) + (startTimePicker.getMinutes() * MILLISECONDS_IN_MINUTE);
        } else if(startTimePicker.isAM() && startTimePicker.getHours() == 12) {
            return (startTimePicker.getMinutes() * MILLISECONDS_IN_MINUTE);
        } else {
            return (startTimePicker.getHours() * MILLISECONDS_IN_HOUR) + (startTimePicker.getMinutes() * MILLISECONDS_IN_MINUTE);
        }
    }
    
    /** 
     * Get the end time in epoch form
     * @return the time, in milliseconds, stored in the end date and end time pickers
     */
    private long getEndTime() {
        if(!endTimePicker.isAM() && endTimePicker.getHours() != 12) {
            return ((endTimePicker.getHours() + 12) * MILLISECONDS_IN_HOUR) + (endTimePicker.getMinutes() * MILLISECONDS_IN_MINUTE);
        } else if(endTimePicker.isAM() && endTimePicker.getHours() == 12) {
            return (endTimePicker.getMinutes() * MILLISECONDS_IN_MINUTE);
        } else {
            return (endTimePicker.getHours() * MILLISECONDS_IN_HOUR) + (endTimePicker.getMinutes() * MILLISECONDS_IN_MINUTE);
        }
    }
    
    /** 
     * Get the course creation time in epoch form
     * @return the time, in milliseconds, of the course's creation
     */
    @SuppressWarnings("deprecation")
    private long getCourseCreationTime() {
        if(currentExperiment.getPublishedDate() != null) {
            Date date = currentExperiment.getPublishedDate();
            if(date.toString().contains("PM") && date.getHours() != 12) {
                return ((date.getHours() + 12) * MILLISECONDS_IN_HOUR) + (date.getMinutes() * MILLISECONDS_IN_MINUTE);
            } else if(date.toString().contains("AM") && date.getHours() == 12) {
                return (date.getMinutes() * MILLISECONDS_IN_MINUTE);
            } else {
                return (date.getHours() * MILLISECONDS_IN_HOUR) + (date.getMinutes() * MILLISECONDS_IN_MINUTE);
}
        } else {
            return 0;
        }
    }
    
    /**
     * Reset start time picker to valid value
     */
    @SuppressWarnings("deprecation")
    private void adjustStartTime() {
        startTimePicker.setHours(lastStartHours);
        startTimePicker.setMinutes(lastStartMinutes);
        startTimePicker.getSwitch().setText(lastStartTimeSwitchLabel);
        if(currentExperiment.getPublishedDate() != null &&
                startDatePicker.getValue().getYear() == currentExperiment.getPublishedDate().getYear() &&
                startDatePicker.getValue().getMonth() == currentExperiment.getPublishedDate().getMonth() &&
                startDatePicker.getValue().getDate() == currentExperiment.getPublishedDate().getDate() && 
                getStartTime() <= getCourseCreationTime()) {
            int hours = currentExperiment.getPublishedDate().getHours();
            if(hours == 0) {
                hours = 12;
                startTimePicker.getSwitch().setText("AM");
            } else if(hours == 12) {
                startTimePicker.getSwitch().setText("PM");
            } else if(hours > 12) {
                hours -= 12;
                startTimePicker.getSwitch().setText("PM");
            } else {
                startTimePicker.getSwitch().setText("AM");
}
            startTimePicker.setHours(hours);
            startTimePicker.setMinutes(currentExperiment.getPublishedDate().getMinutes());
            lastStartHours = startTimePicker.getHours();
            lastStartMinutes = startTimePicker.getMinutes();
            lastStartTimeSwitchLabel = startTimePicker.getSwitch().getText();
        } else if(getStartTime() > getEndTime()) {
            if(startDatePicker.getValue().getDate() == (currentExperiment.getPublishedDate().getDate())) {
            startTimePicker.setHours(currentExperiment.getPublishedDate().getHours());
            startTimePicker.setMinutes(currentExperiment.getPublishedDate().getMinutes());
            startTimePicker.getSwitch().setText(endTimePicker.getSwitch().getText());
            }
            else {
                startTimePicker.setHours(endTimePicker.getHours());
                startTimePicker.setMinutes(endTimePicker.getMinutes());
                startTimePicker.getSwitch().setText(endTimePicker.getSwitch().getText());
            }
            
            lastStartHours = startTimePicker.getHours();
            lastStartMinutes = startTimePicker.getMinutes();
            lastStartTimeSwitchLabel = startTimePicker.getSwitch().getText();
        }
    }
    
    /**
     * Reset end time picker to valid value
     */
    private void adjustEndTime() {
        endTimePicker.setHours(lastEndHours);
        endTimePicker.setMinutes(lastEndMinutes);
        endTimePicker.getSwitch().setText(lastEndTimeSwitchLabel);
        if(getStartTime() > getEndTime()) {
            endTimePicker.setHours(startTimePicker.getHours());
            endTimePicker.setMinutes(startTimePicker.getMinutes());
            endTimePicker.getSwitch().setText(startTimePicker.getSwitch().getText());
            lastEndHours = endTimePicker.getHours();
            lastEndMinutes = endTimePicker.getMinutes();
            lastEndTimeSwitchLabel = endTimePicker.getSwitch().getText();
        }
    }
    
    /**
     * Reset start date picker to valid value
     */
    @SuppressWarnings("deprecation")
    private void adjustStartDate() {
        startDatePicker.setValue(lastStartDate);
        startDateBox.setText(startDatePicker.getValue().toLocaleString().substring(0, startDatePicker.getValue().toLocaleString().indexOf(',')));
        startDatePicker.setCurrentMonth(startDatePicker.getValue());
    }
    
    /**
     * Reset end date picker to valid value
     */
    @SuppressWarnings("deprecation")
    private void adjustEndDate() {
        endDatePicker.setValue(lastEndDate);
        endDateBox.setText(endDatePicker.getValue().toLocaleString().substring(0, endDatePicker.getValue().toLocaleString().indexOf(',')));
        endDatePicker.setCurrentMonth(endDatePicker.getValue());
    }
    
    /**
     * Compares the start and end times. Will return false if the start and end dates aren't the same.
     * @return A boolean value denoting if the start and end dates are the same and if the start time
     * is after the end time.
     */
    private boolean compareTimes() {
    	return startDateBox.getText().equals(endDateBox.getText()) && getStartTime() > getEndTime();
}
    
    /**
     * Compares the start time with the course creation time.
     * Will return false if the current course doesn't have a published date or if the start date isn't
     * equal to the course published date.
     * @return A boolean value denoting if the start and course creation dates are the same 
     * and if the start time is before the course creation time.
     */
    @SuppressWarnings("deprecation")
	private boolean compareDbTimes() {
    	return currentExperiment.getPublishedDate() != null && 
                startDatePicker.getValue().getYear() == currentExperiment.getPublishedDate().getYear() &&
                startDatePicker.getValue().getMonth() == currentExperiment.getPublishedDate().getMonth() &&
                startDatePicker.getValue().getDate() == currentExperiment.getPublishedDate().getDate() && 
                getStartTime() < getCourseCreationTime();
    }
    
    /**
     * Disables the dates on the start date calendar before and after the current month
     */
    @SuppressWarnings("deprecation")
	private void disableStartDateRange() {
		Date firstDate = startDatePicker.getFirstDate();
		if(firstDate.getMonth() + 1 == startDatePicker.getValue().getMonth() && firstDate.getYear() == startDatePicker.getValue().getYear()) {
			while(firstDate.before(new Date(startDatePicker.getValue().getYear(), startDatePicker.getValue().getMonth(), 1))) {
				startDatePicker.setTransientEnabledOnDates(false, firstDate);
				firstDate = new Date(firstDate.getTime() + 24 * 60 * 60 * 1000);
			}
		} else if(firstDate.getMonth() == 11) {
			Date newYear = new Date(firstDate.getYear() + 1, 0, 1);
			while(firstDate.before(newYear)) {
				startDatePicker.setTransientEnabledOnDates(false, firstDate);
				firstDate = new Date(firstDate.getTime() + 24 * 60 * 60 * 1000);
			}
		} else {
			while(firstDate.before(new Date(firstDate.getYear(), startDatePicker.getCurrentMonth().getMonth(), 1))) {
				startDatePicker.setTransientEnabledOnDates(false, firstDate);
				firstDate = new Date(firstDate.getTime() + 24 * 60 * 60 * 1000);
			}
		}
		firstDate = new Date(startDatePicker.getCurrentMonth().getYear(), startDatePicker.getCurrentMonth().getMonth() + 1, 1);
		Date lastDate = new Date(startDatePicker.getLastDate().getTime() + 24 * 60 * 60 * 1000);
		while(firstDate.before(lastDate)) {
			startDatePicker.setTransientEnabledOnDates(false, firstDate);
			firstDate = new Date(firstDate.getTime() + 24 * 60 * 60 * 1000);
		}
    }
    
    /**
     * Disables the dates on the end date calendar before the current month
     */
    @SuppressWarnings("deprecation")
	private void disableEndDateRange() {
    	Date firstDate = endDatePicker.getFirstDate();
		if(firstDate.getMonth() + 1 == endDatePicker.getValue().getMonth() && firstDate.getYear() == endDatePicker.getValue().getYear()) {
			while(firstDate.before(new Date(endDatePicker.getValue().getYear(), endDatePicker.getValue().getMonth(), 1))) {
				endDatePicker.setTransientEnabledOnDates(false, firstDate);
				firstDate = new Date(firstDate.getTime() + 24 * 60 * 60 * 1000);
			}
		} else if(firstDate.getMonth() == 11) {
			Date newYear = new Date(firstDate.getYear() + 1, 0, 1);
			while(firstDate.before(newYear)) {
				endDatePicker.setTransientEnabledOnDates(false, firstDate);
				firstDate = new Date(firstDate.getTime() + 24 * 60 * 60 * 1000);
			}
		} else {
			while(firstDate.before(new Date(firstDate.getYear(), endDatePicker.getCurrentMonth().getMonth(), 1))) {
				endDatePicker.setTransientEnabledOnDates(false, firstDate);
				firstDate = new Date(firstDate.getTime() + 24 * 60 * 60 * 1000);
			}
		}
		firstDate = new Date(endDatePicker.getCurrentMonth().getYear(), endDatePicker.getCurrentMonth().getMonth() + 1, 1);
		Date lastDate = new Date(endDatePicker.getLastDate().getTime() + 24 * 60 * 60 * 1000);
		while(firstDate.before(lastDate)) {
			endDatePicker.setTransientEnabledOnDates(false, firstDate);
			firstDate = new Date(firstDate.getTime() + 24 * 60 * 60 * 1000);
		}
    }
}
