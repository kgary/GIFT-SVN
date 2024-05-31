/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap;

import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.AnchorButton;
import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.Navbar;
import org.gwtbootstrap3.client.ui.NavbarBrand;
import org.gwtbootstrap3.client.ui.constants.Trigger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.enums.DeploymentModeEnum;
import mil.arl.gift.common.gwt.client.JsniUtility;
import mil.arl.gift.common.gwt.client.RpcResponse;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.AbstractBsWidget;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ManagedTooltip;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ModalDialogBox;
import mil.arl.gift.common.gwt.shared.ServerProperties;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.dashboard.client.Dashboard;
import mil.arl.gift.tools.dashboard.client.DashboardService;
import mil.arl.gift.tools.dashboard.client.DashboardServiceAsync;
import mil.arl.gift.tools.dashboard.client.UiManager;
import mil.arl.gift.tools.dashboard.client.bootstrap.BsDialogConfirmWidget.ConfirmationDialogCallback;
import mil.arl.gift.tools.dashboard.shared.ScreenEnum;

/**
 * The Bootstrap 'header' for the gift dashboard ui.  Currently this header contains
 * a NavBar with options to select at a high level for the dashboard.
 *
 * @author nblomberg
 */
public class BsHeaderWidget extends AbstractBsWidget implements ClickHandler {

    private static HeaderWidgetUiBinder uiBinder = GWT.create(HeaderWidgetUiBinder.class);

    private static Logger logger = Logger.getLogger(BsHeaderWidget.class.getName());
    /*
     * $TODO$ nblomberg
     * The user logged in state will hopefully be managed outside of the widget, but for now this will be
     * used to show the functionality of how sign in/out will work.
     */
    public enum UserLoginState {
        STATE_LOGGEDIN,
        STATE_LOGGEDOUT
    }
    
    private static final String SIGNOUT_TEXT = "Sign Out";
    private static final String NOTSIGNEDIN_TEXT = "Not Signed In";
    private static final String SIGNIN_TEXT = "Sign In";
    
    private static final String FORUM_URL = "https://gifttutoring.org/projects/gift/boards";
    private static final String DOCUMENTATION_URL = "https://gifttutoring.org/projects/gift/wiki/Documentation";
    private static final String RELEASE_NOTES = "https://gifttutoring.org/projects/gift/wiki/Release_Notes_";
    
    private static final String SIGNOUT_TITLE = "Sign Out";
    private static final String SIGNOUT_GATACTIVE_MESSAGE = "Are you sure you want to sign out? <br><br>  You will lose any unsaved changes to your course.";
    private static final String LEAVEGAT_CONFIRM_LABEL = "Sign Out";
    private static final String LEAVEGAT_DECLINE_LABEL = "Stay On Page";
    
    private static final String SELECTED_HEADER_STYLE = "headerSelected";
    private static final String SELECTED_SESSION_LIST_ITEM_STYLE = "sessionListItemSelectedColor";
    
    private ServerProperties props;
    
    /**
     * Create a remote service proxy to talk to the server-side Greeting service.
     */
    private final DashboardServiceAsync dashboardService = GWT
            .create(DashboardService.class);
    
    @UiField
    Navbar navBar;
    
    @UiField
    AnchorListItem ctrlMyCourses;
    
    @UiField
    AnchorListItem ctrlMyStats;
    
    @UiField
    AnchorListItem ctrlMyTools;
    
    @UiField
    AnchorListItem ctrlMyExperiments;
    
    @UiField
    AnchorButton ctrlGameMaster;
    
    @UiField
    AnchorListItem activeSessionsItem;
    
    @UiField
    AnchorListItem pastSessionsItem;
    
    /** A button used to access the sub-views within the web monitor */
    @UiField
    AnchorButton ctrlWebMonitor;
    
    /** A navigation button used to show the status view in the web monitor */
    @UiField
    AnchorListItem statusItem;
    
    /** A navigation button used to show the messages view in the web monitor */
    @UiField
    AnchorListItem messageItem;
    
    @UiField
    AnchorButton ctrlUserInfo;
    
    @UiField
    AnchorListItem ctrlUserAction; 
    
    @UiField
    Icon navbarMinimizeIcon;

    /** The tooltip for the {@link #minimizedPanel} */
    @UiField
    ManagedTooltip minimizedPanelTooltip;

    /** Panel to show when the main navigation bar is minimized */
    @UiField
    FlowPanel minimizedPanel;
    
    /** where to place the system image */
    @UiField
    NavbarBrand navBarHeader;
    
    /** the system image to add to the nav bar header */
    Image headerImage = null;

    /**
     * The timer to show the {@link #minimizedPanelTooltip} for a few seconds
     */
    private final Timer minimizeTooltipTimer = new Timer() {
        @Override
        public void run() {
            minimizedPanelTooltip.setTrigger(Trigger.HOVER);
            minimizedPanelTooltip.recreate();
            minimizedPanelTooltip.hide();
        }
    };

    /**
     * The click event handler for the minimize navbar icon
     * 
     * @param event the click event
     */
    @UiHandler("navbarMinimizeIcon")
    void onClickNavbarMinimizeBtn(ClickEvent event) {
        event.stopPropagation();

        /* Hide the nav bar and show the minimized panel */
        navBar.setVisible(false);
        minimizedPanel.setVisible(true);

        /* Show the tooltip for 2 seconds */
        minimizedPanelTooltip.show();
        minimizeTooltipTimer.schedule(2000);
    }

    @UiHandler("forumsItem")
    void onClickForumsItem(ClickEvent event) {
        JsniUtility.trackEvent( "Open Forums" );
        Window.open(FORUM_URL, "_blank", "");        
    }
    
    @UiHandler("documentationItem")
    void onClickDocumentationItem(ClickEvent event) {  
        
        JsniUtility.trackEvent( "Open Documentation" );
        if(props == null){
            props = Dashboard.getInstance().getServerProperties();  
        }
        
        if(props != null && props.getDocumentationToken() != null && !props.getDocumentationToken().isEmpty()){
            Window.open(DOCUMENTATION_URL + "_" + props.getDocumentationToken(), "_blank", ""); 
        }else{
            Window.open(DOCUMENTATION_URL, "_blank", "");    
        }
    }
    
    @UiHandler("aboutItem")
    void onClickAboutItem(ClickEvent event) {  
        
        JsniUtility.trackEvent( "Open About GIFT" );
        if(props == null){
            props = Dashboard.getInstance().getServerProperties();  
        }
        
        String version = "<i>error</i>";
        String date = "<i>error</i>";
        String buildDate = "<i>error</i>";
        String buildLocation = "<i>error</i>";
        String releaseNotesLink = "</i>error</i>";
        if(props != null){
            
            if(StringUtils.isNotBlank(props.getVersionName())){
                version = props.getVersionName();
            }
            
            if(StringUtils.isNotBlank(props.getVersionDate())){
                date = props.getVersionDate();
            }
            
            if(StringUtils.isNotBlank(props.getDocumentationToken())){
                releaseNotesLink = "<a href=\""+RELEASE_NOTES + props.getDocumentationToken()+"\" target=\"_blank\">Release Notes</a>";
            }
            
            if(StringUtils.isNotBlank(props.getBuildDate())){
                buildDate = props.getBuildDate();
            }
            
            if(StringUtils.isNotBlank(props.getBuildLocation())){
                
                if(props.isServerMode()){
                    // don't show server path 
                    buildLocation = "<i>server</i>";
                }else{
                    buildLocation = props.getBuildLocation();
                }
            }
            
        }
        
        ModalDialogBox aboutDialog = new ModalDialogBox();
        aboutDialog.setCloseable(true);
        aboutDialog.setGlassEnabled(true);
        aboutDialog.setText("About");
        aboutDialog.setWidget(new HTML("<img src=\""+Dashboard.getInstance().getServerProperties().getPropertyValue(ServerProperties.LOGO)+"\"/><br/><br/>"+
                "<table><tr><td>Version</td><td>"+version+"</td></tr>"+
                "<tr><td>Date</td><td>"+date+"</td></tr>"+
                "<tr><td>Path</td><td>"+buildLocation+"</td></tr>"+
                "<tr><td style=\"padding-right: 25px;\">Build Date</td><td>"+buildDate+"</td></tr></table><br/>"+
                releaseNotesLink));
        aboutDialog.center();
        aboutDialog.getElement().getStyle().setTop(76, Unit.PX);  // the default top after 'center' pushes this dialog to the center of the course tiles, this moves it higher
        aboutDialog.show();
    }
    
    
    /** The registration of the click handler for the sign out button. This is used so we can ensure we only have one click handler for the sign out button */
    private HandlerRegistration clickHandlerRegistration = null; 

    interface HeaderWidgetUiBinder extends UiBinder<Widget, BsHeaderWidget> {
    }

    /**
     * Constructor
     */
    public BsHeaderWidget() {    	
        
        initWidget(uiBinder.createAndBindUi(this));
        
        updateUserState("", UserLoginState.STATE_LOGGEDOUT);
        
        ctrlMyCourses.setActive(true);
        ctrlMyCourses.addClickHandler(this);
        ctrlMyStats.addClickHandler(this);
        ctrlMyTools.addClickHandler(this);
        ctrlMyExperiments.addClickHandler(this);
        activeSessionsItem.addClickHandler(this);
        pastSessionsItem.addClickHandler(this);
        statusItem.addClickHandler(this);
        messageItem.addClickHandler(this);

        /* Add a click handler to the minimize panel to expand the navbar */
        minimizedPanel.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                event.stopPropagation();
                navBar.setVisible(true);
                minimizedPanel.setVisible(false);

                /* If the timer is still running, cancel it and hide the
                 * tooltip */
                if (minimizeTooltipTimer.isRunning()) {
                    minimizeTooltipTimer.cancel();
                    minimizedPanelTooltip.hide();
                }

                /* Set the trigger back to Manual for the next time the navbar
                 * is collapsed */
                minimizedPanelTooltip.setTrigger(Trigger.MANUAL);
                minimizedPanelTooltip.recreate();
            }
        }, ClickEvent.getType());
    }
    
    /**
     * Set the nav bar header icon to the system icon
     * @param systemIconUrl the URL accessible by the dashboard for the system icon.  Shouldn't be null or empty.
     */
    private void setSystemIcon(String systemIconUrl){
        
        if(headerImage == null){
            headerImage = new Image();
            headerImage.addStyleName("headerIconAdjustment");
            navBarHeader.add(headerImage);            
        }

        headerImage.setUrl(systemIconUrl);
    }
    
    /**
     * Set the minimize icon visibility
     * @param boolean the value to indicate whether the minimize icon should be visible or not
     */
    public void setMinimizeIconVisible(boolean bool) {
        navbarMinimizeIcon.setVisible(bool);
    }
    
    /*
     * Handler for the sign out button on the navbar.
     * 
     */
    private final ClickHandler signOutHandler = new ClickHandler() {

        // Setup callback for logging out.
        final AsyncCallback<RpcResponse> responseCallback = new AsyncCallback<RpcResponse>() {
            @Override
            public void onFailure(Throwable caught) {
                
            }

            @Override
            public void onSuccess(RpcResponse result) {
                
                UiManager.getInstance().displayScreen(ScreenEnum.LOGIN);
                
                updateUserState("", UserLoginState.STATE_LOGGEDOUT);
                
                /* Reload in case HTTP session information has changed */
                Window.Location.reload();
            }
        };
        
        
        @Override
        public void onClick(ClickEvent event) {
            
            
            logger.fine("Sign Out Menu option clicked: " + event);
            if (UiManager.getInstance().isGatAuthoringActive()) {
                
                ConfirmationDialogCallback callback = new ConfirmationDialogCallback() {

                    @Override
                    public void onDecline() {
                        // do nothing except track analytics
                        JsniUtility.trackEvent( "Cancelled Logout" );
                    }

                    @Override
                    public void onAccept() {
                        JsniUtility.changePage( "Logged Out","Log In" );
                        dashboardService.logoutUser(responseCallback);
                    }
                    
                };
                
                UiManager.getInstance().displayConfirmDialog(SIGNOUT_TITLE, SIGNOUT_GATACTIVE_MESSAGE, LEAVEGAT_CONFIRM_LABEL, LEAVEGAT_DECLINE_LABEL, callback);
            } else {
                dashboardService.logoutUser(responseCallback);
                UiManager.getInstance().displayScreen(ScreenEnum.LOGIN);
            }
            
        }
    };
    
    
    /*
     * Handler for the sign in button on the ui container.
     */
    private final ClickHandler signInHandler = new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {
            
            // $TODO$ nblomberg -- We no longer support a 'logged out' state on the navbar.
        }
        
    };
    
    
    /**
     * Sets the highlighted NavBar item (MyCourses, MyStats, MyTools) in the
     * navbar.  The default is MyCourses.
     * @param screen - The enum that matches the (MyCourses, MyStats, MyTools option).
     */
    public void setSelectedNavBarItem(ScreenEnum screen) {
        
        // Default to MyCourses.
        // Only one option should be 'active' at a time.
        ctrlMyCourses.setActive(true);
        ctrlMyStats.setActive(false);
        ctrlMyTools.setActive(false);
        ctrlMyExperiments.setActive(false);
        ctrlGameMaster.removeStyleName(SELECTED_HEADER_STYLE);
        ctrlWebMonitor.removeStyleName(SELECTED_HEADER_STYLE);
        activeSessionsItem.removeStyleName(SELECTED_SESSION_LIST_ITEM_STYLE);
        pastSessionsItem.removeStyleName(SELECTED_SESSION_LIST_ITEM_STYLE);
        statusItem.removeStyleName(SELECTED_SESSION_LIST_ITEM_STYLE);
        messageItem.removeStyleName(SELECTED_SESSION_LIST_ITEM_STYLE);
        
        if (screen == ScreenEnum.LEARNER_PROFILE) {
            ctrlMyCourses.setActive(false);
            ctrlMyStats.setActive(true);
       
        } else if (screen == ScreenEnum.COURSE_CREATOR) {
            ctrlMyCourses.setActive(false);
            ctrlMyTools.setActive(true);
            
        } else if (screen == ScreenEnum.MY_RESEARCH) {
            ctrlMyCourses.setActive(false);
            ctrlMyExperiments.setActive(true);
        } else if (screen == ScreenEnum.GAME_MASTER_ACTIVE) {
            ctrlMyCourses.setActive(false);
            ctrlGameMaster.addStyleName(SELECTED_HEADER_STYLE);
            activeSessionsItem.addStyleName(SELECTED_SESSION_LIST_ITEM_STYLE);
        }  else if (screen == ScreenEnum.GAME_MASTER_PAST) {
            ctrlMyCourses.setActive(false);
            ctrlGameMaster.addStyleName(SELECTED_HEADER_STYLE);
            pastSessionsItem.addStyleName(SELECTED_SESSION_LIST_ITEM_STYLE);
            
        } else if (screen == ScreenEnum.WEB_MONITOR_STATUS) {
            
            ctrlMyCourses.setActive(false);
            ctrlWebMonitor.addStyleName(SELECTED_HEADER_STYLE);
            statusItem.addStyleName(SELECTED_SESSION_LIST_ITEM_STYLE);
            
        }  else if (screen == ScreenEnum.WEB_MONITOR_MESSAGE) {
            
            ctrlMyCourses.setActive(false);
            ctrlWebMonitor.addStyleName(SELECTED_HEADER_STYLE);
            messageItem.addStyleName(SELECTED_SESSION_LIST_ITEM_STYLE);
        }
    }
    
    /*
     * Update the ui element in the navbar that shows the user name.
     */
    public void updateUserState(String userName, UserLoginState state) {
        
        // We need to remove any previous handler here, otherwise, the 'addclickhandler'
        // continues to add, resulting in multiple click events being handled, which is 
        // not what we want.
        if (clickHandlerRegistration != null) {
            clickHandlerRegistration.removeHandler();
        }
        
        
        if (state == UserLoginState.STATE_LOGGEDIN) {
            ctrlUserInfo.setText(userName);
            ctrlUserAction.setText(SIGNOUT_TEXT);
            clickHandlerRegistration = ctrlUserAction.addClickHandler(signOutHandler);
        }
        else {
            ctrlUserInfo.setText(NOTSIGNEDIN_TEXT);
            ctrlUserAction.setText(SIGNIN_TEXT);
            clickHandlerRegistration = ctrlUserAction.addClickHandler(signInHandler);
            
            
        }
    }
    
    /**
     * Update the dashboard header based on the current configuration (i.e. deployment mode, debug mode)
     */
    public void updateHeaderConfiguration(){
                
        // If LessonLevel is set to RTA, then the widgets should be hidden.
        if (UiManager.getInstance().isRtaLessonLevel()){
            ctrlMyStats.setVisible(false);
            ctrlMyExperiments.setVisible(false);
            ctrlMyCourses.setText("Real Time Assessments");
        }
        
        /* hide game master option in server mode (unless debug mode is
         * enabled); make sure to re-enable the game master UI when switching
         * from server-noDebug to server-debug configuration */
        boolean showGameMaster = !DeploymentModeEnum.SERVER.equals(UiManager.getInstance().getDeploymentMode())
                || UiManager.getInstance().isDebugMode();
        ctrlGameMaster.setVisible(showGameMaster);
        ctrlGameMaster.setEnabled(showGameMaster);
        activeSessionsItem.setEnabled(showGameMaster);
        pastSessionsItem.setEnabled(showGameMaster);
        
        /* Web monitor components should also be shown/hidden under the same circumstances as game master*/
        ctrlWebMonitor.setVisible(showGameMaster);
        ctrlWebMonitor.setEnabled(showGameMaster);
        statusItem.setEnabled(showGameMaster);
        messageItem.setEnabled(showGameMaster);

        if(Dashboard.getInstance().getServerProperties() != null){
            setSystemIcon(Dashboard.getInstance().getServerProperties().getPropertyValue(ServerProperties.SYSTEM_ICON_SMALL));
        }
    }
    
    /**
     * Disables the navigation tabs. This will prevent the user from navigating away from the current page.
     */
    public void disable() {
        setEnabled(false);
    }
    
    /**
     * Enables the navigation tabs
     */
    public void enable() {
        setEnabled(true);
    }
    
    /**
     * Sets whether navigation tabs should be enabled
     * 
     * @param enabled whether the tabs should be enabled or disabled
     */
    private void setEnabled(boolean enabled) {
        ctrlMyStats.setEnabled(enabled);
        ctrlMyTools.setEnabled(enabled);
        ctrlMyCourses.setEnabled(enabled);
        ctrlMyExperiments.setEnabled(enabled);
        ctrlGameMaster.setEnabled(enabled);
        activeSessionsItem.setEnabled(enabled);
        pastSessionsItem.setEnabled(enabled);
        ctrlWebMonitor.setEnabled(enabled);
        statusItem.setEnabled(enabled);
        messageItem.setEnabled(enabled);
    }

    @Override
    public void onClick(ClickEvent event) {
        
        Widget source = (Widget)event.getSource();

        // We need to the the source parent here for it to match (not sure why, but that's what event source is coming in as).
        Widget parent = source.getParent();
        
        logger.fine("Navbar header was clicked " + parent);
        
        
        if (parent == ctrlMyCourses) {
            
            logger.fine("My Courses selected.");
            JsniUtility.changePage( "My Courses Selected","My Courses" );
            // Don't navigate to a new page if we're already on the same page.
            if (!ctrlMyCourses.isActive()) {
                
                
                if (UiManager.getInstance().isGatAuthoringActive()) {
                    UiManager.getInstance().showLeaveGatDialog(ScreenEnum.MYCOURSES);
                } else {
                    resetNavItemActiveStates();
                    ctrlMyCourses.setActive(true);
                    
                    UiManager.getInstance().displayScreen(ScreenEnum.MYCOURSES);
                }
   
            } else {
            
                // If we're on the my courses tab already, but the course details screen is open, then
                // close the course details screen.
                if (UiManager.getInstance().isCourseDetailsVisible()) {
                    UiManager.getInstance().closeCourseDetails();
                }
            }
            
            
        } else if (parent == ctrlMyStats) {
            logger.fine("My Stats selected.");
            JsniUtility.changePage( "My Stats Selected","My Stats" );
            if (!ctrlMyStats.isActive()) {
                
                if (UiManager.getInstance().isGatAuthoringActive()) {
                    UiManager.getInstance().showLeaveGatDialog(ScreenEnum.LEARNER_PROFILE);
                } else {
                    resetNavItemActiveStates();
                    ctrlMyStats.setActive(true);

                    UiManager.getInstance().displayScreen(ScreenEnum.LEARNER_PROFILE);
                }
            }
            
        } else if (parent == ctrlMyTools) {
            
            logger.fine("Course Creator selected.");
            JsniUtility.changePage( "Course Creator Selected","Course Creator" );
            if (!ctrlMyTools.isActive()) {
                
                resetNavItemActiveStates();
                ctrlMyTools.setActive(true);
                
                UiManager.getInstance().displayScreen(ScreenEnum.COURSE_CREATOR, null);
            }
        
        } else if (parent == ctrlMyExperiments) {
            
            logger.fine("My Tools selected.");
            JsniUtility.changePage( "My Tools Selected","My Tools" );
            if (!ctrlMyExperiments.isActive()) {
                            
              if (UiManager.getInstance().isGatAuthoringActive()) {
                  UiManager.getInstance().showLeaveGatDialog(ScreenEnum.MY_RESEARCH);
              } else {
                    resetNavItemActiveStates();
                    ctrlMyExperiments.setActive(true);
                    
                    UiManager.getInstance().displayScreen(ScreenEnum.MY_RESEARCH);
              }
            }
        } else if (parent == activeSessionsItem) {
            /* Don't check if the active session page is already active because
             * if they select it again, we want to go back to the list of
             * sessions */

            logger.fine("Active Sessions Game Master selected.");
            JsniUtility.changePage("Active Sessions Game Master Selected", "Active Sessions Game Master");
            if (UiManager.getInstance().isGatAuthoringActive()) {
                UiManager.getInstance().showLeaveGatDialog(ScreenEnum.GAME_MASTER_ACTIVE);
            } else {
                resetNavItemActiveStates();
                ctrlGameMaster.addStyleName(SELECTED_HEADER_STYLE);
                activeSessionsItem.addStyleName(SELECTED_SESSION_LIST_ITEM_STYLE);

                UiManager.getInstance().displayScreen(ScreenEnum.GAME_MASTER_ACTIVE);
            }
        } else if (parent == pastSessionsItem) {
            /* Don't check if the past session page is already active because if
             * they select it again, we want to go back to the list of
             * sessions */

            logger.fine("Past Sessions Game Master selected.");
            JsniUtility.changePage("Past Sessions Game Master Selected", "Past Sessions Game Master");
            if (UiManager.getInstance().isGatAuthoringActive()) {
                UiManager.getInstance().showLeaveGatDialog(ScreenEnum.GAME_MASTER_PAST);
            } else {
                resetNavItemActiveStates();
                ctrlGameMaster.addStyleName(SELECTED_HEADER_STYLE);
                pastSessionsItem.addStyleName(SELECTED_SESSION_LIST_ITEM_STYLE);

                UiManager.getInstance().displayScreen(ScreenEnum.GAME_MASTER_PAST);
            }
            
        } else if (parent == statusItem) {
            
            logger.fine("Web Monitor Status selected.");
            JsniUtility.changePage("Web Monitor Status Selected", "Web Monitor Status");
            if (UiManager.getInstance().isGatAuthoringActive()) {
                UiManager.getInstance().showLeaveGatDialog(ScreenEnum.WEB_MONITOR_STATUS);
            } else {
                resetNavItemActiveStates();
                ctrlWebMonitor.addStyleName(SELECTED_HEADER_STYLE);
                statusItem.addStyleName(SELECTED_SESSION_LIST_ITEM_STYLE);

                UiManager.getInstance().displayScreen(ScreenEnum.WEB_MONITOR_STATUS);
            }
            
        } else if (parent == messageItem) {

            logger.fine("Web Monitor Messages selected.");
            JsniUtility.changePage("Web Monitor Messages Selected", "Web Monitor Messages");
            if (UiManager.getInstance().isGatAuthoringActive()) {
                UiManager.getInstance().showLeaveGatDialog(ScreenEnum.GAME_MASTER_PAST);
            } else {
                resetNavItemActiveStates();
                ctrlWebMonitor.addStyleName(SELECTED_HEADER_STYLE);
                messageItem.addStyleName(SELECTED_SESSION_LIST_ITEM_STYLE);

                UiManager.getInstance().displayScreen(ScreenEnum.WEB_MONITOR_MESSAGE);
            }
        }
        
        
    }
   
    /**
     * Resets all navigation buttons to their default inactive state. 
     * Useful for switching between button states.
     */
    private void resetNavItemActiveStates() {
        ctrlMyCourses.setActive(false);
        ctrlMyStats.setActive(false);
        ctrlMyTools.setActive(false);
        ctrlMyExperiments.setActive(false);
        ctrlGameMaster.removeStyleName(SELECTED_HEADER_STYLE);
        activeSessionsItem.removeStyleName(SELECTED_SESSION_LIST_ITEM_STYLE);
        pastSessionsItem.removeStyleName(SELECTED_SESSION_LIST_ITEM_STYLE);
        
        ctrlWebMonitor.removeStyleName(SELECTED_HEADER_STYLE);
        statusItem.removeStyleName(SELECTED_SESSION_LIST_ITEM_STYLE);
        messageItem.removeStyleName(SELECTED_SESSION_LIST_ITEM_STYLE);
    }
}
