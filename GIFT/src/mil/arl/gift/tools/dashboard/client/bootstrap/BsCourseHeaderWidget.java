/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap;

import java.util.logging.Logger;

import mil.arl.gift.common.gwt.client.IFrameMessageHandlerParent;
import mil.arl.gift.common.gwt.client.RpcResponse;
import mil.arl.gift.common.gwt.client.iframe.IFrameMessageType;
import mil.arl.gift.common.gwt.client.iframe.messages.IFrameSimpleMessage;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.AbstractBsWidget;
import mil.arl.gift.common.gwt.shared.ServerProperties;
import mil.arl.gift.tools.dashboard.client.ChildIFrameConsts;
import mil.arl.gift.tools.dashboard.client.CourseEndingInterface;
import mil.arl.gift.tools.dashboard.client.Dashboard;
import mil.arl.gift.tools.dashboard.client.DashboardService;
import mil.arl.gift.tools.dashboard.client.DashboardServiceAsync;
import mil.arl.gift.tools.dashboard.client.UiManager;
import mil.arl.gift.tools.dashboard.client.UiManager.EndCourseReason;
import mil.arl.gift.tools.dashboard.client.bootstrap.BsDialogConfirmWidget.ConfirmationDialogCallback;
import mil.arl.gift.tools.dashboard.shared.ScreenEnum;

import org.gwtbootstrap3.client.ui.AnchorButton;
import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Navbar;
import org.gwtbootstrap3.client.ui.NavbarBrand;
import org.gwtbootstrap3.client.ui.html.Span;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

/**
 * The bootstrap course header widget (which is the navbar that is displayed when running
 * a course).  The navbar allows for user controls like (help, save course, stop course), along
 * with course information such as course name.  In the future, a progress bar may be added to 
 * show progress within a course.
 *
 * @author nblomberg
 */
public class BsCourseHeaderWidget extends AbstractBsWidget implements CourseEndingInterface {

    private static HeaderWidgetUiBinder uiBinder = GWT.create(HeaderWidgetUiBinder.class);

    private static Logger logger = Logger.getLogger(BsCourseHeaderWidget.class.getName());
    
    private final String SIGNOUT_TEXT = "Sign Out";  
    
    private final String SIGNOUT_TITLE = "Sign Out";
    private final String SIGNOUT_MESSAGE = "Are you sure you want to sign out?<br><br> You will lose any unsaved progress in the course.";
    private final String SIGNOUT_CONFIRM_LABEL = "Sign Out";
    private final String SIGNOUT_DECLINE_LABEL = "Resume Course";
    
    private final String STOPCOURSE_TITLE = "Quit Course";
    private final String STOPCOURSE_MESSAGE = "Are you sure you want to end the course?<br><br>  You will lose any unsaved progress in the course.";
    private final String STOPCOURSE_CONFIRM_LABEL = "End Course";
    private final String STOPCOURSE_DECLINE_LABEL = "Resume Course";    
    
    /** The domainId of the course that is being run */
    private String courseId;
    
    /**
     * Create a remote service proxy to talk to the server-side Greeting service.
     */
    private final DashboardServiceAsync dashboardService = GWT
            .create(DashboardService.class);
    
    @UiField
    Navbar navBar;
    
    @UiField
    Span txtCourseName;
    
    @UiField
    Button ctrlStop;
    
    @UiField
    AnchorButton ctrlUserInfo;
    
    @UiField
    AnchorListItem ctrlUserAction;

    /** where the system image will be added */
    @UiField
    NavbarBrand navBarHeader;
    
    interface HeaderWidgetUiBinder extends UiBinder<Widget, BsCourseHeaderWidget> {
    }
    
    /**
     * Callback used to handle with the 'stop course' button is pressed.
     */
    private final ConfirmationDialogCallback endCourseCallback = new ConfirmationDialogCallback() {

        @Override
        public void onDecline() {
            // Do nothing.
            
        }

        @Override
        public void onAccept() {
            
			// Start the process of ending the course (due to the user stopping the course).
        	UiManager.getInstance().onCourseEnding(EndCourseReason.USER_STOPPED);
        }
        
    };
    
    
    /**
     * Callback to handle when the signout button is pressed.
     */
    private final ConfirmationDialogCallback signOutCallback = new ConfirmationDialogCallback() {

        @Override
        public void onDecline() {
            // Do nothing.
            
        }

        @Override
        public void onAccept() {
            
            // Start the process of ending the course (due to the user signing out).
            UiManager.getInstance().onCourseEnding(EndCourseReason.USER_SIGNEDOUT);
        }
        
    };

    /**
     * Constructor
     */
    public BsCourseHeaderWidget(String userName, Object data) {
    	
       
    	
        initWidget(uiBinder.createAndBindUi(this));
        
        logger.info("data = " + data);
        if (data != null && data instanceof LoadCourseParameters) {
                        
            // $TODO$ It was changed so that the course runtime path (string) is used to start the course
            // rather than the domainOption data for the course.  Look at passing in a fuller domainOption data
            // object that contains information about the course such as the course name so it can be displayed in the
            // header.
            // private final String COURSE_LABEL = "Course: ";
            // txtCourseName.setText(COURSE_LABEL + courseObj.getDomainName());
            LoadCourseParameters loadParams = (LoadCourseParameters)data;
            courseId = loadParams.getCourseRuntimeId();
            
            logger.info("constructor called with course: " + courseId);
        } else {
            logger.warning("Unhandled init parameters passed into the screen.  Expected type of " + LoadCourseParameters.class.getName());
        }
        
        if(data instanceof LoadCourseParameters) {
            String courseName = ((LoadCourseParameters) data).getCourseDomainName();
        	txtCourseName.setText(courseName);
        	UiManager.getInstance().setCurrentCourseName(courseName);
        }
        
        ctrlUserInfo.setText(userName);
        ctrlUserAction.setText(SIGNOUT_TEXT);
        ctrlUserAction.addClickHandler(signOutHandler);
        
        ctrlStop.addClickHandler(stopCourseHandler);
        
        // set the system image
        Image headerImage = new Image();
        headerImage.setUrl(Dashboard.getInstance().getServerProperties().getPropertyValue(ServerProperties.SYSTEM_ICON_SMALL));
        headerImage.addStyleName("headerIconAdjustment");
        navBarHeader.add(headerImage);
        
    }
    
    /*
     * Handler for the sign out button on the navbar.
     * 
     */
    private final ClickHandler signOutHandler = new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {
            // Confirm with the user about ending the course before it's done.
            UiManager.getInstance().displayConfirmDialog(SIGNOUT_TITLE, SIGNOUT_MESSAGE, SIGNOUT_CONFIRM_LABEL, SIGNOUT_DECLINE_LABEL, signOutCallback);
        }
    };
    
    /**
     * Handler for the stop course button on the navbar.
     */
    private final ClickHandler stopCourseHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent clickEvent) {

            showStopCourseDialog();
        }
    };
    
    
    /**
     * Accessor to show the stop course dialog.  
     */
    public void showStopCourseDialog() {
        UiManager.getInstance().displayConfirmDialog(STOPCOURSE_TITLE, STOPCOURSE_MESSAGE, STOPCOURSE_CONFIRM_LABEL, STOPCOURSE_DECLINE_LABEL, endCourseCallback);
    }


    @Override
    public void handleCourseEnding() {
        
        logger.info("Contacting TUI with Stop Course message");
        
        // The stop course message is sent to the TUI and then we wait for the "END COURSE" message response.
        // Even though the messages are 'one-way' we need to have an 'asyncronous' wait/load here to allow 
        // the TUI time to shutdown properly.  Once the TUI is closed properly then we can proceed.  This prevents
        // issues like the dashboard trying to delete course files while the TUI still has them loaded
        // (see ticket:  https://gifttutoring.org/issues/1769)
        IFrameSimpleMessage msg = new IFrameSimpleMessage(IFrameMessageType.STOP_COURSE);
        IFrameMessageHandlerParent.getInstance().sendMessage(msg, ChildIFrameConsts.TUI_IFRAME_KEY);
        
        ctrlUserAction.setEnabled(false);
        ctrlStop.setEnabled(false);
        
    }


    @Override
    public void handleCourseEndingTimerExpired(EndCourseReason reason) {
        courseEnded(reason);
    }


    @Override
    public void handleCourseEnded(EndCourseReason reason) {
        courseEnded(reason);
    }
    
    /**
     * Perform cleanup that is needed when the course has ended.  
     * 
     * @param reason - the reason that the course was ended.
     */
    private void courseEnded(final EndCourseReason reason) {
        
        logger.fine("Sending endcourse rpc for course: " + courseId);
        dashboardService.endCourse(UiManager.getInstance().getSessionId(), courseId, new AsyncCallback<RpcResponse>() {

            @Override
            public void onFailure(Throwable t) {
                logger.severe("Failure while executing the endCourse rpc: " + t.getMessage());
                transitionToNextScreen(reason);
                
            }

            @Override
            public void onSuccess(RpcResponse result) {
                if (result.isSuccess()) {
                    logger.fine("endCourse rpc returned success.");
                } else {
                    logger.fine("endCourse rpc returned failure: " + result.getResponse());
                }
                
                transitionToNextScreen(reason);
            } 
        });
    }
    
    /**
     * Transition to the next screen based on the reason that the course was ended.  If the user
     * signed out, then the next screen should be the login screen.  If the user manually stopped,
     * then the user should be taken back to the my courses screen.
     * 
     * @param reason - the reason the course was ended.
     */
    private void transitionToNextScreen(EndCourseReason reason) {
        if (reason == EndCourseReason.USER_SIGNEDOUT) {
            logoutUser();
        } else {
            displayMyCoursesScreen();
        }
    }
    
    /**
     * Signal to the UIManager that the mycourses screen should be displayed.
     * 
     */
    private void displayMyCoursesScreen() {
        logger.info("displayMyCoursesScreen() transitioning to mycourses screen.");
        UiManager.getInstance().displayScreen(ScreenEnum.MYCOURSES);
    }
    
    /**
     * Log the user out (if they signed out via the 'sign out' button).
     */
    private void logoutUser() {
        logger.info("logoutUser() transitioning to login screen.");
        // Setup callback for logging out.
        final AsyncCallback<RpcResponse> responseCallback = new AsyncCallback<RpcResponse>() {
               @Override
               public void onFailure(Throwable caught) {
                   logger.info("rpc failure caught: transitioning to login screen.");
                   UiManager.getInstance().displayScreen(ScreenEnum.LOGIN);
               }

               @Override
               public void onSuccess(RpcResponse result) {
                   logger.info("rpc response returned: transitioning to login screen.");
                   UiManager.getInstance().displayScreen(ScreenEnum.LOGIN);
               }
        };
        
        dashboardService.logoutUser(responseCallback);
    }

}
