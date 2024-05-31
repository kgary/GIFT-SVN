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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Container;
import org.gwtbootstrap3.client.ui.Image;
import org.gwtbootstrap3.client.ui.Popover;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimpleCheckBox;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.DomainOption;
import mil.arl.gift.common.DomainOption.DomainOptionRecommendation;
import mil.arl.gift.common.enums.DeploymentModeEnum;
import mil.arl.gift.common.enums.DomainOptionRecommendationEnum;
import mil.arl.gift.common.gwt.client.DetailedRpcResponse;
import mil.arl.gift.common.gwt.client.JsniUtility;
import mil.arl.gift.common.gwt.client.RpcResponse;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.AbstractBsWidget;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.BsLoadingIcon;
import mil.arl.gift.common.gwt.shared.CourseValidationParams;
import mil.arl.gift.common.gwt.shared.DocumentUtil;
import mil.arl.gift.common.io.CourseListFilter.CourseSourceOption;
import mil.arl.gift.tools.dashboard.client.Dashboard;
import mil.arl.gift.tools.dashboard.client.DashboardService;
import mil.arl.gift.tools.dashboard.client.DashboardServiceAsync;
import mil.arl.gift.tools.dashboard.client.UiManager;
import mil.arl.gift.tools.dashboard.client.bootstrap.BsDialogConfirmWidget.ConfirmationDialogCallback;
import mil.arl.gift.tools.dashboard.client.bootstrap.BsDialogWidget.DialogCallback;
import mil.arl.gift.tools.dashboard.client.bootstrap.BsDialogWidget.DialogType;
import mil.arl.gift.tools.dashboard.client.bootstrap.file.FileOperationProgressModal;
import mil.arl.gift.tools.dashboard.shared.ScreenEnum;
import mil.arl.gift.tools.dashboard.shared.rpcs.CopyCourseResult;
import mil.arl.gift.tools.dashboard.shared.rpcs.DeleteCourseResult;
import mil.arl.gift.tools.dashboard.shared.rpcs.ExportResponse;
import mil.arl.gift.tools.dashboard.shared.rpcs.ValidateCourseResponse;

/**
 * The BsCourseWidget class encapsulates the functionality needed for a single
 * course element/widget in the UI panel.  It contains the ability to hide/show
 * data on mouseover, plus icons/images of the course.
 *
 * @author nblomberg
 */
public class BsCourseWidget extends AbstractBsWidget {

    /** instance of the logger */
    private static Logger logger = Logger.getLogger(BsCourseWidget.class.getName());
    
    private static BootstrapCourseWidgetUiBinder uiBinder = GWT.create(BootstrapCourseWidgetUiBinder.class);
    
    /** background color used for my courses course tiles */
    public static String MY_COURSES_COLOR = null;
    
    /** background color used for showcase course tiles */
    public static String SHOWCASE_COURSES_COLOR = "#3841ef";  //blue-ish
    
    /** background color used for shared with me course tiles */
    public static String SHARED_WITH_ME_COURSES_COLOR = "#efb338";  //orang-ish
    
    /** background color used for no category course tiles */
    public static String NO_CATEGORY_COURSES_COLOR = "green";
    
    /** the style name that should be applied to course widgets when the widget needs some temp highlighting */
    private static final String BELOW_EXPECTATION_STYLE_NAME = "nodeBelowExpectationAssessment";
    
    /**
     * The CourseState represents the various states that this widget can be in at any given time.  
     * 
     * INITIAL - The initial state of the widget when creating it.
     * VALIDATING - The widget is in process of validating the course data with the server.
     * VALIDATED - The widget is validated its course data with the server.  This does not imply success or failure of the validation.  It just means validation is complete.
     * 
     * @author nblomberg
     *
     */
    private enum CourseState {
        INITIAL,
        VALIDATING,
        VALIDATED,
    }
    
    /**
    * Create a remote service proxy to talk to the server-side Greeting service.
    */
   private final DashboardServiceAsync dashboardService = GWT
           .create(DashboardService.class);
   
   @UiField
   Container courseWidgetContainer;
    
    @UiField
    Container ctrlCourseUpperPanel;
    
    
    @UiField
    Button startButton;
    

    @UiField
    HTML ctrlCourseName;
    
    @UiField 
    Image ctrlCourseImage;
    
    @UiField 
    Image ctrlCourseType;
    
    @UiField 
    Image ctrlCourseTypeOverlay;
    
    @UiField
    Button ctrlCourseInfo;
    
    @UiField 
    Popover ctrlCourseTypeReason;
    
    @UiField
    Button ctrlEditCourse;
    
    @UiField
    Button ctrlCopyCourse;
        
    @UiField
    Button ctrlDeleteCourse;
    
    @UiField
    Button ctrlShareCourse;
    
    /** button to export any course data for this course instance */
    @UiField
    Button exportCourseDataButton;
        
    /** button to jump into report generation */
    @UiField
    Button createReportButton;
        
    @UiField
    DeckPanel overlayDeck;
    
    @UiField
    Widget overlayPanel;
    
    @UiField
    FocusPanel selectOverlayPanel;
    
    @UiField
    SimpleCheckBox ctrlSelectCourse;
    
    @UiField
    BsLoadingIcon ctrlValidate;   
    
    
    // the course data that this widget represents.
    private DomainOption courseData;
    
    /** Whether or not this widget is selected */
    private boolean isSelected;
    
    /** An instance of the myCourses widget.  This is used so that the widget can communicate back
     *  to the my courses panel when it is done async loading (so the panel can update the details)
     */
    private BsMyCoursesDashWidget myCoursesWidget = null;
  
    /** A gwt Request object that is used to cancel the validateCourseData rpc */
    private Request validateCourseRequest = null;
    
    /** The state of the course widget */
    private CourseState courseState = CourseState.INITIAL;
    
    
    interface BootstrapCourseWidgetUiBinder extends UiBinder<Widget, BsCourseWidget> {
    }
    

    /**
     * Constructor
     */
    private BsCourseWidget() {
        
        /*
         * nblomberg 4/17/15 
         * We are no longer using sink events here, since we have put the mouseover event to be a 'hover' event in native css.  
         * Keeping the sink event code here in the meantime, in case we need to bring it back.  This is an example of how to 
         * do fast mouse events using native events.
         *  
        // Using sink events here for MUCH faster processing.  If we just added
        // domeventhandlers here, the highlighting would not track properly and the mouseout event would not always get triggered if the mouse was moving fast.
        // See:  http://stackoverflow.com/questions/10626429/onmouseout-event-not-triggered-when-moving-mouse-fast-gwt-all-browsers
        // We are no longer using sink events here, since we have put the mouseover event to be a 'hover' event in native css.  

        // sinkEvents(Event.ONMOUSEOVER | Event.ONMOUSEOUT);
        */
        
        initWidget(uiBinder.createAndBindUi(this));
        
        ctrlCourseUpperPanel.setVisible(true);

        startButton.addClickHandler(startCourseHandler);
                
        ctrlCourseTypeOverlay.addClickHandler(showValidationMessageHandler);

        overlayDeck.showWidget(overlayDeck.getWidgetIndex(overlayPanel));
        
        // If LessonLevel is set to RTA, then the widgets should be hidden.
        if(UiManager.getInstance().isRtaLessonLevel()){
            startButton.setVisible(false);
        }
        
        selectOverlayPanel.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                setSelected(!isSelected);
            }
        });  
        
        ctrlCourseInfo.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
               JsniUtility.trackEvent( "Open Course Info" );
               UiManager.getInstance().showCourseDetails(courseData);
                
            }           
            
        });
        
        // Handler for the edit course button.
        ctrlEditCourse.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if (myCoursesWidget != null) {
                    myCoursesWidget.cancelPendingRpcs();
                }else if(!courseData.isDomainIdReadable()){
                    // prevent accessing the course creator if the user hacked the dom
                    return;
                }
                JsniUtility.trackEvent( "Open Edit Course" );
                UiManager.getInstance().showEditCourse(courseData);
            }
            
        });
        
        ctrlCopyCourse.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                
                if(!courseData.isDomainIdReadable()){
                    // prevent copying a course if the user hacked the dom
                    return;
                }
                JsniUtility.trackEvent( "Open Copy Course" );
                copyCourse("Please enter a new name for the course <b>" + courseData.getDomainName() + "</b>");                
            }
            
        });
        
        ctrlDeleteCourse.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                
                if(!courseData.isDomainIdReadable()){
                    // prevent deleting a course if the user hacked the dom
                    return;
                }
                
                JsniUtility.trackEvent( "Open Delete Course" );
                StringBuilder message = new StringBuilder();
                message.append("The following course has been chosen for deletion:");
                message.append("<br/>");
                message.append("<ul>");
                message.append("<li>");
                message.append(courseData.getDomainName());
                message.append("</li>");
                message.append("</ul>");
                message.append("Deleting this course will also delete any files associated with it, which could potentially invalidate any ");
                message.append("courses that reference such files or cause problems during course execution.");
                message.append("<br/>");
                message.append("<br/>");
                message.append("<b><i>If there has been any published courses created (from the Publish Courses Panel) and any data collected ");
                message.append("for this course, the published courses will not be deleted, but users will no longer ");
                message.append("be able to take the course associated with the published courses.</i></b>");
                message.append("<br/>");
                message.append("<br/>");
                message.append("If you wish to delete this course without causing problems, you should make sure that no other ");
                message.append("files reference the files inside of it. ");
                message.append("This course should not be open in the Course Creator when deleting, otherwise the course will be recreated when saved.");
                message.append("<br/>");
                message.append("<br/>");
                message.append("Are you sure you still want to delete this course?");
                message.append("<br/>");
                message.append("</div>");

                UiManager.getInstance().displayConfirmDialog("Delete Course", message.toString(), "Yes, delete this course", "Cancel", new ConfirmationDialogCallback() {

                    @Override
                    public void onDecline() {
                        // Nothing to do
                    }

                    @Override
                    public void onAccept() {
                        JsniUtility.trackEvent( "Confirm Delete Course" );
                        deleteCourse(false, false);
                    }
                });                
            }            
        });    
        
        ctrlShareCourse.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                
                if(!isSharable()){
                    // prevent sharing a course if the user hacked the dom
                    return;
                }
                JsniUtility.trackEvent( "Share Course" );
                shareCourse();                
            }
            
        });
        
        createReportButton.addClickHandler(new ClickHandler(){
            
            @Override
            public void onClick(ClickEvent event) {
                
                if(hasPublishCourse()){
                    // jump to the publish course page and have it open the published courses that reference this course
                    UiManager.getInstance().displayScreen(ScreenEnum.MY_RESEARCH, courseData);
                }
            }
        });
        
        exportCourseDataButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                if(!isOwner()){
                    // prevent export if the user hacked the dom
                    return;
                }
                
                JsniUtility.trackEvent( "Export Course Data" );
                exportCourseData();
            }
            
        });
    }
    
    /**
     * Starts the server request to export the course data, starts monitoring the progress and shows a error or completion
     * dialog at the end.
     */
    private void exportCourseData(){
        
        final FileOperationProgressModal exportCourseDataProgressModal = FileOperationProgressModal.getCourseDataExportProgressModal();
        
        dashboardService.exportCourseData(UiManager.getInstance().getUserName(), courseData, new AsyncCallback<ExportResponse>() {
            
            @Override
            public void onSuccess(final ExportResponse exportResponse) {

                if(!exportResponse.isSuccess()) {
                    
                    // close the progress modal
                    exportCourseDataProgressModal.stopPollForProgress(!exportResponse.isSuccess());
                    
                    UiManager.getInstance().displayDetailedErrorDialog(
                            "Failed to Export Course Data", 
                            "An error occurred while exporting the course data: " + exportResponse.getResponse() 
                            + "<br/><br/><b>Course Name:</b> " +  courseData.getDomainName()
                            + "<br/><b>Course Path:</b> " + courseData.getDomainId(), 
                            exportResponse.getAdditionalInformation(), 
                            exportResponse.getErrorStackTrace(),
                            courseData.getDomainName());
                        
                }else{
                    
                    if(exportCourseDataProgressModal.isVisible()) {
                        // close the progress modal
                        exportCourseDataProgressModal.stopPollForProgress(false);
                    }
                    
                    if(exportResponse.getExportResult() != null){
                        Window.open(exportResponse.getExportResult().getDownloadUrl(),"","");
                        
                        UiManager.getInstance().displayDialog(DialogType.DIALOG_INFO, "Export Successful!", ""
                                + "Your export has been successfully created.<br/>"
                                + "<br/>"
                                + "If your download doesn't start in a few seconds please, "
                                + "<a href='" + exportResponse.getExportResult().getDownloadUrl() + "' target='_self'>click here</a> "
                                + "to start the download."
                                
                                , new DialogCallback() {
                                    
                                    @Override
                                    public void onAccept() {
                                        
                                        dashboardService.deleteExportFile(exportResponse.getExportResult(), new AsyncCallback<RpcResponse>(){
    
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
                    }else{
                        // let the user know there is nothing to download
                        UiManager.getInstance().displayDialog(DialogType.DIALOG_INFO, "Nothing to Export", 
                                "No course data was found for the course '"+courseData.getDomainName()+"'.<br/><br/>"+
                                        "Has there been at least one attempt at this course yet?<br/><i>(currently only exports attempts made after April 23, 2021)</i>", null);
                    }
                }
            }
            
            @Override
            public void onFailure(Throwable t) {
                exportCourseDataProgressModal.stopPollForProgress(true);
            }
        });
        
        exportCourseDataProgressModal.startPollForProgress();
    }
    
    /**
     * Return whether this course is sharable.  
     * 
     * @return true if the user is the owner of the course and GIFT is in server mode. 
     */
    private boolean isSharable(){
        return isOwner() && 
                Dashboard.getInstance().getServerProperties().getDeploymentMode() == DeploymentModeEnum.SERVER;
    }
    
    /**
     * Return whether this course is owned by this user.
     * 
     * @return true if the user is the owner of the course
     */
    private boolean isOwner(){
        return courseData != null && courseData.isOwner();
    }
    
    /**
     * For handling when the start course button is pressed
     */
    private final ClickHandler startCourseHandler = new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {
            
            JsniUtility.trackEvent( "Start Course" );
            try{
                startTakeACourse();
            }catch(Throwable t){
                logger.log(Level.SEVERE, "Failed to start the course", t);
                UiManager.getInstance().displayErrorDialog("Failed to start the course", 
                        "There was an error when attempting to start - "+t, null);
            }
        }
    };
    
    /**
     * For handling when the invalid course icon is clicked.
     */
    private final ClickHandler showValidationMessageHandler = new ClickHandler() {
        
        @Override
        public void onClick(ClickEvent event) {
            
            JsniUtility.trackEvent( "Open Invalid Course Info" );
            if(courseData != null 
                    && courseData.getDomainOptionRecommendation() != null 
                    && courseData.getDomainOptionRecommendation().getDomainOptionRecommendationEnum() != null){
                
                DomainOptionRecommendation recommendation = courseData.getDomainOptionRecommendation();
                if(recommendation.getDomainOptionRecommendationEnum().isUnavailableType()){
                
                    UiManager.getInstance().displayDetailedErrorDialog(
                            "Course Unavailable", 
                            recommendation.getReason(), 
                            recommendation.getDetails(), 
                            null,
                            courseData.getDomainName());
                    
                }else if(recommendation.getDomainOptionRecommendationEnum() == DomainOptionRecommendationEnum.AVAILABLE_WITH_WARNING){
                    
                    UiManager.getInstance().displayDetailedErrorDialog(
                            "Course with Missing Resources", 
                            recommendation.getReason(), 
                            recommendation.getDetails(), 
                            null,
                            courseData.getDomainName());
                }
                
                
            }
        }
    };
    
    /**
     * Constructor for the widget.  This takes in a coursename
     * and calls the base private constructor.  This is the constructor
     * that should be used when creating the BsCourseWidget object.
     * 
     * @param data - The course data (domain option) that the ui element represents.  Cannot be null. 
     * @param parent - The parent widget that contains the course widget (can be null if using the widget on a different panel other than the my courses panel).
     */
    public BsCourseWidget(DomainOption data, BsMyCoursesDashWidget parent) {
        this();       
        
        myCoursesWidget = parent;
        
        updateCourseData(data);  
        
        logger.fine("finished creating course widget for "+data.getDomainId());
    }
    
    /**
     * Apply some temporary highlighting to this course widget instance.  Temporary comes in because
     * a refresh of course widgets currently causes a recreation and this style would not be applied by default.
     */
    public void applyTempHighlight(){
        courseWidgetContainer.addStyleName(BELOW_EXPECTATION_STYLE_NAME);
    }
    
    
    /**
     * Cancels any pending rpcs and does cleanup on other items like the timer.  This should be done
     * if the user cancels manually, or if the parent widget is removed from the dom (like transitioning to another screen).
     */
    public void cancelPendingRpcs() {
        
        if (courseData != null) {
            logger.info("cancelPendingRpcs called for widget: " + courseData.getDomainId());
        }

        // Cancel the course validation.
        if (validateCourseRequest != null) {
            logger.info("Cancelling the course validation request.");
            validateCourseRequest.cancel();
            validateCourseRequest = null;
        }
    }
    
    /**
     * Return the color to use for a particular course source option type.  This can be used
     * to color the course tile in some way to visually differentiate the courses based on the source
     * (i.e. workspace).
     * @param courseSourceOptionType the enumerated type to get the color for.
     * @return a color for that enumerated type.
     */
    public static String getCourseSourceOptionTypeColor(CourseSourceOption courseSourceOptionType){
        String bgColor;
        
        if(courseSourceOptionType != null){
            switch(courseSourceOptionType){
            case SHOWCASE_COURSES:
                bgColor = SHOWCASE_COURSES_COLOR;
                break;
            case SHARED_COURSES:
                bgColor = SHARED_WITH_ME_COURSES_COLOR;
                break;
            case MY_COURSES:
                bgColor = MY_COURSES_COLOR;
                break;
            default:
                bgColor = NO_CATEGORY_COURSES_COLOR;
                break;
            }
        }else{
            bgColor = NO_CATEGORY_COURSES_COLOR;
        }
        
        return bgColor;
    }

    /**
     * Helper function to update the course data for the widget.  
     * 
     * When new course data is passed in, the widget can refresh/repopulate
     * the data for the ui as needed.
     * 
     * @param data - The new course data (domain option) that the widget represents.  Cannot be null.
     */
    public void updateCourseData(DomainOption data) {
         
        courseData = data;
        
        ctrlCourseName.setText(data.getDomainName());
        
        String bgColor = getCourseSourceOptionTypeColor(data.getCourseSourceOptionType());
        
        if(bgColor != null){
            courseWidgetContainer.getElement().getStyle().setBackgroundColor(bgColor);
        }
        
        // Fill in the image, use default image if null.
        String imageUrl = data.getImageURL();
        if (imageUrl == null || imageUrl.isEmpty()) {
            imageUrl = DomainOption.COURSE_DEFAULT_IMAGE;
        }
        logger.info("For course (" + data.getDomainName() + "), the image is: " + imageUrl);
        
        final String finalImageUrl = imageUrl;
        Image.prefetch(finalImageUrl);
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            @Override
            public void execute() {
                ctrlCourseImage.setUrl(finalImageUrl);
            }
            
        });
        
        // set all but the default image to 140px height to avoid parts of the image being cut off. The
        // default image is sized to be cut off at the bottom some on course tiles, best to avoid sizing
        if(!ctrlCourseImage.getUrl().endsWith(DomainOption.COURSE_DEFAULT_IMAGE)){
            ctrlCourseImage.setHeight("140px");
        }
        if(ctrlCourseImage.getUrl().endsWith(DomainOption.FILE_NOT_FOUND_IMAGE)){
            ctrlCourseImage.setWidth("140px");
        }
        
        startButton.setEnabled(true);
        
        // populate the course type icon & reason.
        ctrlCourseType.setVisible(false);
        ctrlCourseTypeOverlay.setVisible(false);
        ctrlCourseTypeReason.setIsHtml(false);
        ctrlCourseTypeReason.setTitle("");
        ctrlCourseTypeReason.setContent("");
        

        String recommendationTitle = "";
        String recommendationMessage = "";
        
        DomainOption.DomainOptionRecommendation recommendation = data.getDomainOptionRecommendation();
        logger.fine("Recommendation = " + recommendation);
        if (recommendation != null) {
            
            DomainOptionRecommendationEnum recommendationType = recommendation.getDomainOptionRecommendationEnum();
            if(recommendationType.compareTo(DomainOptionRecommendationEnum.RECOMMENDED) == 0){
                ctrlCourseType.setUrl(DomainOption.COURSE_TYPE_RECOMMENDED);
                ctrlCourseTypeOverlay.setUrl(DomainOption.COURSE_TYPE_RECOMMENDED);
                ctrlCourseType.setVisible(true);
                ctrlCourseTypeOverlay.setVisible(true);
                recommendationTitle = "Recommended";
                recommendationMessage = recommendation.getReason();
                
                ctrlCourseTypeOverlay.removeStyleName("clickable");

                
            }else if(recommendationType.compareTo(DomainOptionRecommendationEnum.NOT_RECOMMENDED) == 0 ){
                recommendationTitle = "Refresher Course";
                recommendationMessage = recommendation.getReason();
                ctrlCourseType.setUrl(DomainOption.COURSE_TYPE_REFRESHER);
                ctrlCourseTypeOverlay.setUrl(DomainOption.COURSE_TYPE_REFRESHER);
                ctrlCourseType.setVisible(true);
                ctrlCourseTypeOverlay.setVisible(true);
                
                ctrlCourseTypeOverlay.removeStyleName("clickable");

                
            }else if(recommendationType.compareTo(DomainOptionRecommendationEnum.AVAILABLE_WITH_WARNING) == 0 ){
                recommendationTitle = "Course w/ Missing Resource(s)";
                recommendationMessage = "<html>"+recommendation.getReason()+"<br/>Please click to view "
                        + "a report of the problem encountered. </html>";
                ctrlCourseTypeReason.setIsHtml(true);
                
                ctrlCourseType.setUrl(DomainOption.COURSE_TYPE_WARNING);
                ctrlCourseTypeOverlay.setUrl(DomainOption.COURSE_TYPE_WARNING);
                ctrlCourseType.setVisible(true);
                ctrlCourseTypeOverlay.setVisible(true);
                
                ctrlCourseTypeOverlay.removeStyleName("clickable");
                
            }else{
                recommendationTitle = "Unavailable Course";
                recommendationMessage = "<html>This course is currently not available because a problem occured during validation. <br/>Please click to view "
                        + "a report of the problem encountered. </html>";
                ctrlCourseTypeReason.setIsHtml(true);
                
                ctrlCourseType.setUrl(DomainOption.COURSE_TYPE_INVALID);
                ctrlCourseTypeOverlay.setUrl(DomainOption.COURSE_TYPE_INVALID);
                ctrlCourseType.setVisible(true);
                ctrlCourseTypeOverlay.setVisible(true);
                
                ctrlCourseTypeOverlay.addStyleName("clickable");
                
                
                // if the course is invalid we don't want the user to be allowed to start the course.
                startButton.setEnabled(false);
            }
            
            if (recommendationTitle != "" && recommendationMessage != "") {
                ctrlCourseTypeReason.setTitle(recommendationTitle);
                ctrlCourseTypeReason.setContent(recommendationMessage);
            }
        }
        
        // manage course tile buttons based on user permission
        ctrlDeleteCourse.setVisible(courseData.isOwner() && courseData.isDomainIdWritable());        
        ctrlEditCourse.setVisible(courseData.isDomainIdReadable());
        ctrlCopyCourse.setVisible(courseData.isDomainIdReadable());
        exportCourseDataButton.setVisible(courseData.isOwner());

        // course sharing only allowed in server mode and user must be the owner of the course
        ctrlShareCourse.setVisible(isSharable());
        
        // only show the button if this user has access to at least one published course instance that references this course
        createReportButton.setVisible(hasPublishCourse());
    }
    
    /**
     * Return whether this course is referenced in a publish course that this user has access too.
     * @return true if this course is referenced in an accessible publish course
     */
    public boolean hasPublishCourse(){
        return courseData != null && courseData.hasAccessiblePublishCourse();
    }
    
    /**
     * Whether the current dashboard user can edit this course.
     * 
     * @return true if the user is the owner and has write access.
     */
    public boolean isModifiable(){
        return courseData != null && courseData.isOwner() && courseData.isDomainIdWritable();
    }
    
    /**
     * Whether the current dashboard user can read this course contents (e.g. open in course creator)
     * @return true if the user has read access to the course.
     */
    public boolean isReadable(){
        return courseData != null && courseData.isDomainIdReadable();
    }
    
    /**
     * Remove this course widget from the list of course widgets
     */
    private void removeCourseWidget(){
        myCoursesWidget.removeCourse(this);
    }
 
    /**
     * Attempts to delete the current course data
     * 
     * @param deleteResponses Whether or not to delete the survey responses. If set to false, the user will not be able to continue
     * deleting the course if the survey context contains survey responses. 
     * @param skipSurveyResources Whether or not to skip deleting the course's survey context.
     */
    private void deleteCourse(boolean deleteResponses, boolean skipSurveyResources) {


        final FileOperationProgressModal deleteProgressModal = FileOperationProgressModal.getDeleteProgressModal();

        dashboardService.deleteCourses(UiManager.getInstance().getSessionId(), Arrays.asList(courseData), deleteResponses, skipSurveyResources, new AsyncCallback<DeleteCourseResult>(){

            @Override
            public void onFailure(Throwable caught) {
                deleteProgressModal.stopPollForProgress(true);
            }

            @Override
            public void onSuccess(final DeleteCourseResult result) {

                if(!result.isSuccess()) {
                    
                    if(result.hadSurveyResponses()) {
                        
                        deleteProgressModal.stopPollForProgress(true, new ScheduledCommand() {

                            @Override
                            public void execute() {
                                
                                UiManager.getInstance().displayConfirmDialog(
                                        "Delete Survey Responses?",
                                        "There are survey responses for this course which will need to be deleted as well."
                                        + "<br/><br/>"
                                        + "Do you want to delete these responses and continue deleting the course?",
                                        "Yes, Delete Responses", 
                                        "Cancel",
                                        new ConfirmationDialogCallback() {
                                            
                                            @Override
                                            public void onDecline() {
                                                // nothing to do
                                            }

                                            @Override
                                            public void onAccept() {
                                                deleteCourse(true, false);
                                            }
                                            
                                        });
                            }
                            
                        });
                        
                        } else if (result.deleteSurveyFailed()){
                            
                            surveyDeleteFailed(result);
                                                        
                        } else {
                            
                            deleteProgressModal.stopPollForProgress(!result.isSuccess());
                            
                            if(result.getUserSessionId() == null){
                                UiManager.getInstance().displayScreen(ScreenEnum.LOGIN);
                                UiManager.getInstance().displayInfoDialog("Login Again", result.getResponse());
                            }else{

                                UiManager.getInstance().displayDetailedErrorDialog(
                                        "Failed to Delete Course", 
                                        "An error occurred while deleting the course: " + result.getResponse() 
                                        + "<br/><br/><b>Course Name:</b> " +  courseData.getDomainName()
                                        + "<br/><b>Course Path:</b> " + courseData.getDomainId(), 
                                        result.getAdditionalInformation(), 
                                        result.getErrorStackTrace(),
                                        courseData.getDomainName());
                            }
                            

                        }
                    
                } else {
                    if(deleteProgressModal.isVisible()) {
                        deleteProgressModal.stopPollForProgress(false);
                    }

                    removeCourseWidget();
                }

            }
            
            private void surveyDeleteFailed(final DeleteCourseResult result){
                
                deleteProgressModal.stopPollForProgress(true, new ScheduledCommand() { 
                    
                    @Override
                    public void execute() {
                        
                        String reason = (result.getResponse() != null ? result.getResponse() : " You do not have permission to delete "
                                + "this course's surveys.");
                        
                        String advancedDescription = new String();
                        if(result.getAdditionalInformation() != null){
                            advancedDescription = result.getAdditionalInformation();
                        }else if(result.getErrorStackTrace() != null && !result.getErrorStackTrace().isEmpty()){
                            
                            //for now just grab the first 5 lines since this dialog isn't setup for showing a full stack trace 
                            for(int index = 0; 
                                    index < 5 && index < result.getErrorStackTrace().size(); index++){
                                
                                String line = result.getErrorStackTrace().get(index);
                                if(index > 0){
                                    advancedDescription += "&nbsp;&nbsp;&nbsp;&nbsp;"; //tab
                                }
                                advancedDescription += line + "</br>";
                            }
                        }else{
                            advancedDescription = "not provided";
                        }
                    
                        UiManager.getInstance().displayConfirmDialog(
                            "Failed to delete course",
                            "The course's surveys could not be deleted.<br/><br/><b>Reason</b>: "
                            + reason
                            + "</br></br><i>Advanced Description</i>:</br>"
                            + advancedDescription
                            + "<br/><br/>"
                            + "Do you still want to delete this course without deleting its surveys?",
                            "Yes, Continue Delete", 
                            "Cancel",
                            new ConfirmationDialogCallback() {
                                
                                @Override
                                public void onDecline() {
                                    deleteProgressModal.stopPollForProgress(true);
                                }

                                @Override
                                public void onAccept() {
                                    deleteCourse(false, true);
                                }
                                
                            });
                    }
                    
                });
            }

        });

        deleteProgressModal.startPollForProgress();
    
    }
    
    /**
     * Attempts to share the course with another user.
     */
    private void shareCourse() {
        UiManager.getInstance().displayShareDialog(courseData);
    }
    
    /**
     * Attempts to copy the current courseData.
     * 
     * @param renameMsg The message that prompts the user to enter a new name for the course.
     */
    private void copyCourse(String renameMsg) {
        
        final DomainOption courseDataFinal = courseData;
        UiManager.getInstance().displayRenameDialog(
                "Enter a new name", 
                renameMsg, 
                courseDataFinal.getDomainName() + " - Copy", 
                "Copy",
                new ValueChangeHandler<String>() {

                    @Override
                    public void onValueChange(final ValueChangeEvent<String> event) {
                        logger.info("renameDialog::onValueChange() " + event.getValue());
                        
                        final FileOperationProgressModal copyProgressModal = FileOperationProgressModal.getCopyProgressModal();
                        
                        //Checks for illegal characters in the name of the file.
                        final String validationMsg = DocumentUtil.validateFileName(event.getValue());
                        if(validationMsg != null){
                            copyProgressModal.stopPollForProgress(true, new ScheduledCommand() {

                                @Override
                                public void execute() {
                                    copyCourse(validationMsg  + "<br/><br/>Please enter a new name.");
                                }
                                
                            });
                            return;
                        }
                        
                        copyProgressModal.setCopyCallbackHandler(new AsyncCallback<CopyCourseResult>() {

                            @Override
                            public void onFailure(Throwable caught) {
                                
                                final String caughtStr = caught.toString();
                                logger.info("onFailure: " + caughtStr);
                                copyProgressModal.stopPollForProgress(true,  new ScheduledCommand() {

                                    @Override
                                    public void execute() {
                                        UiManager.getInstance().displayDetailedErrorDialog("Copy Course Failed", 
                                        "A server error occurred while copying the course."
                                        + "<b>Course Name:</b> " + courseData.getDomainName()
                                        + "<br/><b>Course Path:</b> " + courseData.getDomainId(),
                                        caughtStr, null, courseData.getDomainName());
                                    }
                                });
                                
                            }

                            @Override
                            public void onSuccess(CopyCourseResult result) {
                                
                                logger.info("onSuccess() result: " + result);
                                if(result.isSuccess()) {
                                    copyProgressModal.stopPollForProgress(false);
                                    
                                    // Delay the refresh for one event loop.
                                    Scheduler.get().scheduleDeferred(new ScheduledCommand() {

                                        @Override
                                        public void execute() {
                                            
                                            logger.info("Refreshing course list");
                                            myCoursesWidget.refreshCourseList(null);
                                            
                                        }
                                       
                                    });
                                    
                                    
                                } else if(result.courseAlreadyExists()) {
                                    
                                    logger.info("Course already exists");
                                    copyProgressModal.stopPollForProgress(true, new ScheduledCommand() {

                                        @Override
                                        public void execute() {
                                            copyCourse("A course with the name \"" + event.getValue() + "\" already exists. "
                                                    + "<br/><b>Note</b>: course names are not case sensitive.<br/><br/>"
                                                    + "Please enter a new name for the course.");      
                                        }
                                        
                                    });
                                    
                                } else {
                                    
                                    copyProgressModal.stopPollForProgress(true);
                                    
                                    if(result.getUserSessionId() == null){
                                        UiManager.getInstance().displayScreen(ScreenEnum.LOGIN);
                                        UiManager.getInstance().displayInfoDialog("Login Again", result.getResponse());
                                    }else{

                                        UiManager.getInstance().displayDetailedErrorDialog(
                                                "Failed to Copy Course", 
                                                "An error occurred while copying the course."
                                                + "<br/>Result = "+ result.getResponse() 
                                                + "<br/>Message = "+ result.getErrorMessage()
                                                + "<br/><br/><b>Course Name:</b> " +  courseDataFinal.getDomainName()
                                                + "<br/><b>Course Path:</b> " + courseDataFinal.getDomainId(), 
                                                result.getErrorDetails(), 
                                                result.getErrorStackTrace(),
                                                courseDataFinal.getDomainName());
                                    }

                                }
                                
                            }
                            
                        });
                        
                        dashboardService.copyCourse(UiManager.getInstance().getSessionId(), event.getValue(), courseDataFinal, new ArrayList<DomainOption> (myCoursesWidget.rawCourseMap.keySet()), new AsyncCallback<DetailedRpcResponse>() {

                            @Override
                            public void onFailure(final Throwable caught) {
                                copyProgressModal.stopPollForProgress(true,  new ScheduledCommand() {

                                    @Override
                                    public void execute() {
                                        UiManager.getInstance().displayDetailedErrorDialog("Copy Course Failed", 
                                        "A server error occurred while copying the course.", caught.toString(), null, courseDataFinal.getDomainName());
                                    }
                                });
                            }

                            @Override
                            public void onSuccess(DetailedRpcResponse result) {
                                if(result.isSuccess()) {
                                    logger.info("start polling for progress. " + event);
                                    copyProgressModal.startPollForProgress();
                                } else {

                                    copyProgressModal.stopPollForProgress(true);
                                    
                                    if(result.getUserSessionId() == null){
                                        UiManager.getInstance().displayScreen(ScreenEnum.LOGIN);
                                        UiManager.getInstance().displayInfoDialog("Login Again", result.getResponse());
                                    }else{

                                        UiManager.getInstance().displayDetailedErrorDialog(
                                                "Failed to Start Copying Course", 
                                                "An error occurred while requesting to copy the course."
                                                + "<br/>Message = "+ result.getErrorMessage()
                                                + "<br/><br/><b>Course Name:</b> " +  courseDataFinal.getDomainName()
                                                + "<br/><b>Course Path:</b> " + courseDataFinal.getDomainId() 
                                                + "<br/>Result = "+ result.getResponse(),
                                                result.getErrorDetails(), 
                                                result.getErrorStackTrace(),
                                                courseDataFinal.getDomainName());
                                    }

                                }
                                
                            }
                            
                        });

                        logger.info("copy course started: " + event);
                    }
                    
                });
    }
 
    /**
     * Used to set the visibility of the course info/details button.
     * 
     * @param visible - True to show the button, false to hide it.
     */
    public void setInfoButtonVisibility(boolean visible) {
        ctrlCourseInfo.setVisible(false);
    }
    
    
    /*
     * nblomberg 4/17/15 
     * We are no longer using sink events here, since we have put the mouseover event to be a 'hover' event in native css.  
     * Keeping the sink event code here in the meantime, in case we need to bring it back.  This is an example of how to 
     * do fast mouse events using native events.
     *  
    @Override
    public void onBrowserEvent(Event event) {
        super.onBrowserEvent(event);
        
        switch(event.getTypeInt()) {
        case Event.ONMOUSEOUT:
            ctrlCourseUpperPanel.setVisible(false);
            ctrlCourseLowerPanel.setVisible(false);    
            logger.fine("BrowserEvent occurred: " + event.getType());
            break;
        case Event.ONMOUSEOVER:
            ctrlCourseUpperPanel.setVisible(true);
            ctrlCourseLowerPanel.setVisible(true);
            logger.fine("BrowserEvent occurred: " + event.getType());
            break;
        }
    }
    */
    
    /**
     * Sets whether or not this course widget should be capable of being selected
     * 
     * @param selectable whether or not this course widget should be capable of being selected
     */
    public void setSelectable(boolean selectable){
        
        if(selectable){
            overlayDeck.showWidget(overlayDeck.getWidgetIndex(selectOverlayPanel));
            
        } else {
            overlayDeck.showWidget(overlayDeck.getWidgetIndex(overlayPanel));
        }
    }

    /**
     * Gets whether or not this widget is selected
     * 
     * @return whether or not this widget is selected
     */
    public boolean isSelected() {
        return isSelected;
    }

    /**
     * Sets whether or not this widget is selected and updates the widget's style accordingly.<br/>
     * Note: this method won't do anything if this widget isn't showing the select panel.
     * 
     * @param isSelected whether or not this widget is selected
     */
    public void setSelected(boolean isSelected) {
        
        boolean isSelectable = overlayDeck.getVisibleWidget() == overlayDeck.getWidgetIndex(selectOverlayPanel);
        if(!isSelectable){
            return;
        }
        
        ctrlSelectCourse.setValue(isSelected);
        
        if(isSelected){
            selectOverlayPanel.addStyleName("courseDummySelected");
            
        } else {
            selectOverlayPanel.removeStyleName("courseDummySelected");
        }
        
        this.isSelected = isSelected;
    }
    
    /**
     * Accessor to get the course data associated with this course widget.
     * @return DomainOption - The domainoption (course data) for this widget.
     */
    public DomainOption getCourseData(){
        return courseData;
    }    
    
    /**
     * Accessor to return the course state of this widget.
     * 
     * @return CourseState - The current course state of the widget.
     */
    public CourseState getCourseState() {
       return courseState;
    }  
    
    /**
     * Handles state changes for this widget.  Primarily used to update
     * the visual state of the ui elements for the various states that the widget
     * can be in.
     * 
     * @param state - The new course state of the widget (cannot be null).
     */
    private void changeState(CourseState state) {
        
        if (courseData != null) {
            logger.info("Changing state of " + courseData.getDomainId() + " to state: " + state);
        } else {
            logger.info("Changing state of " + this + " to state: " + state);
        }
        
        courseState = state;
        switch (state) {

        case VALIDATING: 
            // Disable buttons that should not be enabled while the validation is in progress.
            ctrlValidate.startLoading();
            startButton.setEnabled(false);
            ctrlCourseInfo.setEnabled(false);
            break;
        case VALIDATED:
            // Validated here doesn't meant that validation was successful, only that the course
            // has been validated and has either succeeded or failed.
            startButton.setEnabled(true);
            startButton.setIcon(null);
            ctrlValidate.stopLoading();
            ctrlCourseInfo.setEnabled(true);            
            //$FALL-THROUGH$
        case INITIAL:
            // intentional fall-through
        default:
            // Setup initial defaults for the controls.
            break;
        
        }
    }
    
    /**
     * Start the logic to take the course represented by this widget.
     * @throws Exception if the course data is null
     */
    public void startTakeACourse() throws Exception{
        
        if(logger.isLoggable(Level.INFO)){
            logger.info("Requesting to start course: " + courseData);
        }
        
        UiManager.getInstance().takeACourse(courseData);
    }
    
    
    /**
     * Start the course validation.  This makes an asynchronous call to the server
     * to check if the course is valid.  During the time of the async call we want to 
     * display a loading icon and disable some of the controls until the course can
     * be properly validated.
     */
    public void startValidation(CourseValidationParams courseValidationParams) {
        
        // Change the state to validing state (in progress of validation).
        changeState(CourseState.VALIDATING);

        validateCourseRequest = dashboardService.validateCourseData(UiManager.getInstance().getSessionId(), courseData, courseValidationParams, new AsyncCallback<ValidateCourseResponse>() {

            
            @Override
            public void onFailure(Throwable t) {
                
                changeState(CourseState.VALIDATED);
                logger.severe("Unable to validate course: " + t.getMessage());
            }

            @Override
            public void onSuccess(ValidateCourseResponse result) {
                
                changeState(CourseState.VALIDATED);
                if (result.isSuccess()) {
                    
                    onValidationSuccess(result.getCourse());

                } else {
                    logger.warning("Course was not able to be validated: " + result.getResponse());
                }
                
            }
            
        });
        
    }
    
    /**
     * Handles the logic for when a course is successfully validated.
     * 
     * @param newCourseData - The new course data received from the server (cannot be null).
     */
    private void onValidationSuccess(DomainOption newCourseData) {
        DomainOption oldCourseData = courseData;
        // We should only get one result back from the server.
        updateCourseData(newCourseData);

        // signal to the my courses panel that this widget has been validated.
        // this is optional if the my courses panel has been specified as a parent
        // of this widget.
        if (myCoursesWidget != null) {
            myCoursesWidget.onValidationSuccess(oldCourseData,  courseData, this);
        }
    }

    /**
     * Set the visibility on buttons used to interact with the course (e.g. start course, copy course).
     * 
     * @param isVisible - true to show the buttons, false to hide.
     */
    public void setCourseActionButtonsVisibility(boolean isVisible) {
        
        ctrlCourseInfo.setVisible(false);
        startButton.setVisible(isVisible);
        ctrlCopyCourse.setVisible(false);
        ctrlDeleteCourse.setVisible(false);
        ctrlShareCourse.setVisible(false);  
    }

}
