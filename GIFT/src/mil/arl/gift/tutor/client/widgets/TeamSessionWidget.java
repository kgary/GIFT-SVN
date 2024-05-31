/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.client.widgets;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Collapse;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.html.Paragraph;
import org.gwtbootstrap3.extras.animate.client.ui.Animate;
import org.gwtbootstrap3.extras.animate.client.ui.constants.Animation;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.course.dkf.session.KnowledgeSessionsReply;
import mil.arl.gift.common.course.dkf.session.SessionMember;
import mil.arl.gift.common.course.dkf.session.TeamKnowledgeSession;
import mil.arl.gift.common.course.dkf.team.AbstractTeamUnit;
import mil.arl.gift.common.course.dkf.team.Team;
import mil.arl.gift.common.course.dkf.team.TeamMember;
import mil.arl.gift.common.gwt.client.DetailedRpcResponse;
import mil.arl.gift.common.gwt.client.GenericRpcResponse;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.EditableLabel;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.KnowledgeSessionListWidget;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.KnowledgeSessionListWidget.SessionSelectedCallback;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ModalDialogBox;
import mil.arl.gift.common.gwt.shared.GetActiveKnowledgeSessionsResponse;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tutor.client.BrowserSession;
import mil.arl.gift.tutor.client.Document;
import mil.arl.gift.tutor.client.KnowledgeSessionListener;
import mil.arl.gift.tutor.client.SuccessFailCallback;
import mil.arl.gift.tutor.client.TutorUserInterfaceService;
import mil.arl.gift.tutor.client.TutorUserInterfaceServiceAsync;
import mil.arl.gift.tutor.client.widgets.UserSessionWidget.KickMemberCallback;
import mil.arl.gift.tutor.client.widgets.UserSessionWidget.UserType;
import mil.arl.gift.tutor.shared.WidgetInstance;
import mil.arl.gift.tutor.shared.data.AbstractKnowledgeSessionResponse;

/**
 * A widget used to handle selecting/joining/hosting team sessions prior to starting a training application such as VBS3.
 * 
 * @author nblomberg
 *
 */
public class TeamSessionWidget extends Composite implements KnowledgeSessionListener {

    interface TeamSessionWidgetUiBinder extends UiBinder<Widget, TeamSessionWidget> {
    }
        
    private static TeamSessionWidgetUiBinder uiBinder = GWT.create(TeamSessionWidgetUiBinder.class);
    
    /** Instance of the logger. */
    private static Logger logger = Logger.getLogger(TeamSessionWidget.class.getName());
    
    /** The connection to the TWS RPC service */
    private static final TutorUserInterfaceServiceAsync tutorUserInterfaceService = GWT.create(TutorUserInterfaceService.class);
    
    /** Indicates if a pending request is being sent to the server.  We only allow a single pending request at a time. */
    private boolean pendingRequest = false;
    
    /** The supported modes that the widget can display. */
    private static enum ScreenMode {
        SESSION_LIST,   // Displays the list of available sessions to join
        TEAM_LIST,      // Displays the list of roles that can be selected on a team (after a session is joined)
    }
    
    /** Header text for the host */
    private static final String HOST_HEADER_TEXT = "Hosting Team Session";
    
    /** Header text for a non-host */
    private static final String MEMBER_HEADER_TEXT = "Joining Team Session";
    
    /** Help text for the host */
    private static final String HOST_HELP_TEXT = "Select a role from the list below.<br/>"+
            "Once all players have selected a role, press the Start Session button to being the team session for all players.";
    
    /** Help text for a non-host */
    private static final String MEMBER_HELP_TEXT = "Select a role from the list below.<br/>"+
            "The host will begin the team session once everyone has choosen a role to play.";
    
    /** Notification reminder for the host. */
    private static final String HOST_NOTIFY_REMINDER = "When all joined users are assigned to roles, press the START SESSION button to begin.";
    
    /** The current mode for the UI Panels. */
    private ScreenMode screenMode = ScreenMode.SESSION_LIST;
    
    /** 
     * The data about the joined session. 
     * Will be null when the user has not joined a session yet.
     */
    TeamKnowledgeSession sessionJoined = null;
    
    /** The currently selected session from the list of available sessions. */
    AbstractKnowledgeSession selectedSession = null;
    
    /** The member data of the local user. */
    SessionMember localUser = null;
    
    /** Indicates if the learner is hosting the team session. */
    private boolean isHost = false;
    
    /** The domain id of the local user.  This is used so we can tell what domain session id belongs to the local user. */
    private Integer localDomainId = null;
    
    /** Mapping of domain session ids to the user session widgets for each user. */
    Map<Integer, UserSessionWidget> userSessionWidgets = new HashMap<Integer, UserSessionWidget>();
    
    /** The currently selected tree item. */
    TreeItem selectedTreeItem = null;
    
    /** To prevent spam, only send notifications after a set duration.  
     * This is to prevent the host from being spammed with notifications.
     */
    private static final int NOTIFY_HOST_DURATION = 30000;
    
    /** The last time the notification for the host to start the session was sent. */
    private Date lastNotifyHost = null;
    
    @UiField
    FlowPanel sessionPanel;
    
    @UiField
    FlowPanel teamPanel;
    
    @UiField
    Button refreshButton;
    
    @UiField 
    Button hostButton;
    
    @UiField
    Button joinButton;
    
    @UiField
    Button leaveSessionButton;
    
    @UiField
    Button startSessionButton;
        
    @UiField
    FlowPanel userList;
    
    @UiField
    Heading joinedHeader;
    
    @UiField
    EditableLabel sessionName;
    
    @UiField
    Label courseId;
    
    @UiField
    Tree teamTree;
    
    @UiField
    Heading sessionHeaderText;
    
    @UiField
    Paragraph helpText;
    
    /** Button used to collapse all the teams in the tree */
    @UiField
    protected Button collapseAllButton;
    
    /** Button used to expand all the teams in the tree */
    @UiField
    protected Button expandAllButton;
    
    @UiField
    protected Button hostHelpButton;
    
    @UiField
    protected Collapse hostHelpCollapse;

    @UiHandler("refreshButton")
    void onClickRefreshButton(ClickEvent event) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("refresh button clicked.");
        }
        
        setSelectedSession(null);
        
        fetchActiveKnowledgeSessions();
    }
    
    @UiHandler("joinButton")
    void onClickJoinButton(ClickEvent event) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("join button clicked.");
        }
        joinSessionRequest();
    }
    
    @UiHandler("hostButton") 
    void onClickHostButton(ClickEvent event) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("host button clicked.");
        }
        hostTeamSession();
    }
    
    @UiHandler("leaveSessionButton") 
    void onClickLeaveSessionButton(ClickEvent event) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("leave session button clicked.");
        }
        leaveSession();
    }
    
    @UiHandler("startSessionButton") 
    void onClickStartSessionButton(ClickEvent event) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("start session button clicked.");
        }
        
        startTeamSession();

    }
    
    @UiHandler("teamTree")
    void onTreeItemSelected(SelectionEvent<TreeItem> item) {

        if (item != null && item.getSelectedItem() != null) {
            final TreeItem selectedItem = item.getSelectedItem();
            
            if (selectedItem.getWidget() != null && selectedItem.getWidget() instanceof TeamTreeWidgetItem) {
                TeamTreeWidgetItem teamTreeItem = (TeamTreeWidgetItem)(selectedItem.getWidget());
                if (teamTreeItem.isTeamMember()) {
                    if (logger.isLoggable(Level.INFO)) {
                        logger.info("Selected team member: " + teamTreeItem.getItemName());
                    }
                    
                    if (selectedTreeItem != selectedItem) {
                        
                        if(teamTreeItem.isAvailable()) {
                            
                            // Only select if it's not already selected.
                            selectTeamRole(teamTreeItem.getItemName(), new SuccessFailCallback() {
    
                                @Override
                                public void onSuccess() {
                                    // Update the internally selected item.
                                    selectedTreeItem = selectedItem;
                                }
    
                                @Override
                                public void onFailure() {
                                    // Keep the original selection.
                                    teamTree.setSelectedItem(selectedTreeItem, false);
                                }
                                
                            });
                        
                        } else {
                            // Keep the original selection.
                            teamTree.setSelectedItem(selectedTreeItem, false);
                        }
                        
                    } else {
                        // If the user selects the same item again (simply unassign the user from the role.
                        unassignTeamRole(teamTreeItem.getItemName(), new SuccessFailCallback() {

                            @Override
                            public void onSuccess() {
                                selectedTreeItem = null;
                                teamTree.setSelectedItem(null, false);
                                
                            }

                            @Override
                            public void onFailure() {
                                teamTree.setSelectedItem(selectedTreeItem, false);
                                
                            }
                            
                        });
                    }
                        
                } else {
                    
                    // Toggle the team widget open or closed.
                    // This is a workaround for a gwt bug where the tree item double-fires the selection handler
                    // and is discussed in this thread:  
                    //  https://github.com/gwtproject/gwt/issues/3665
                    // The workaround was taken from this thread and selects the parent item prior to updating
                    // the state of the node to avoid a double selection firing event.  After the state is updated
                    // of the node, then the selected item is returned to it's proper value.
                    // Without this workaround, the call to setState() fires the selection event twice and makes it difficult to
                    // properly toggle the tree item to open or closed.
                    TreeItem parent = selectedItem.getParentItem();
                    selectedItem.getTree().setSelectedItem(parent, false); // null is ok
                    if(parent != null)
                        parent.setSelected(false);  // not compulsory
                    selectedItem.setState(!selectedItem.getState(), false);
                    
                    // Set the selected item back to its' proper value.
                    teamTree.setSelectedItem(selectedTreeItem, false);
                    
                    
                }
            } 
        }
    }

    /** The widget containing the list of active knowledge sessions */
    @UiField(provided = true)
    protected KnowledgeSessionListWidget sessionListWidget = new KnowledgeSessionListWidget(
            new SessionSelectedCallback() {
                @Override
                protected void sessionSelected(AbstractKnowledgeSession knowledgeSession) {
                    if (logger.isLoggable(Level.INFO)) {
                        logger.info("session Selected: " + knowledgeSession);
                    }
                    
                    setSelectedSession(knowledgeSession);
                }
            });

    /** A mapping from each team member role to the item used to represent it in the team tree */
    private HashMap<String, TeamTreeWidgetItem> roleToTreeItem = new HashMap<>();

    /** 
     * The old name of the session before the host started editing it. This value is only populated and used while
     * the host is editing the session name.
     */
    private String oldSessionName;
    
    /**
     * Class constructor
     * 
     * @param widgetInstance Instance of the widget
     */
    public TeamSessionWidget(WidgetInstance instance) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("TeamSessionWidget()");
        }

        initWidget(uiBinder.createAndBindUi(this));
        
        userList.clear();
        
        joinedHeader.setText("");
        
        // Default host button style.
        startSessionButton.setEnabled(false);
        
        BrowserSession.getInstance().setKnowledgeSessionListener(this);

        // don't show the host button until the server checks whether this client can host
        hostButton.setVisible(false);
        hostHelpButton.setVisible(false);
        
        updateSessionListWidget(null);
        
        collapseAllButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                for(int i = 0; i < teamTree.getItemCount(); i++) {
                    collapseAll(teamTree.getItem(i));
                }
            }

            /**
             * Collapses the given tree item and its children
             * 
             * @param item the tree item to collapse
             */
            private void collapseAll(TreeItem item) {
                item.setState(false);
                
                for(int i = 0; i < item.getChildCount(); i++) {
                    collapseAll(item.getChild(i));
                }
            }
        });
        
        expandAllButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                for(int i = 0; i < teamTree.getItemCount(); i++) {
                    expandAll(teamTree.getItem(i));
                }
            }

            /**
             * Expands the given tree item and its children
             * 
             * @param item the tree item to expand
             */
            private void expandAll(TreeItem item) {
                item.setState(true);
                
                for(int i = 0; i < item.getChildCount(); i++) {
                    expandAll(item.getChild(i));
                }
            }
        });
        
        sessionName.addValueChangeHandler(new ValueChangeHandler<String>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                changeSessionName(event.getValue());
            }
        });
        
        sessionName.addMouseDownHandler(new MouseDownHandler() {
            
            @Override
            public void onMouseDown(MouseDownEvent event) {
                
                //if the host begins editing the session name, save the original name in case a revert is needed
                oldSessionName = sessionName.getValue();
            }
        });
        
        hostHelpButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                hostHelpCollapse.toggle();
            }
        });
        
        setMode(ScreenMode.SESSION_LIST);
        updatePanels();
        
        // Kick off a request to fetch any active knowledge sessions.
        this.fetchCurrentDomainIdAndSessions();
    }
    
    
    /**
     * Sets the selected knowledge session.  Can pass in null to indicate there is no selection.
     * 
     * @param knowledgeSession The knowledge session that is selected.  Can be null, which means no selection.
     */
    private void setSelectedSession(AbstractKnowledgeSession knowledgeSession) {

        selectedSession = knowledgeSession;

        
        if (selectedSession != null) {
            joinButton.setEnabled(true);
            sessionListWidget.setSelectedSessionBySessionName(selectedSession.getNameOfSession());
        } else {
            sessionListWidget.clearSelection();
            joinButton.setEnabled(false);
        }
    }
    
    /**
     * Sets the current mode of the widget.
     * 
     * @param mode The mode that the widget will be set to.
     */
    private void setMode(ScreenMode mode) {
        this.screenMode = mode;
    }
    
    /**
     * Get the current mode of the widget.
     * @return The current mode of the widget.
     */
    private ScreenMode getMode() {
        return this.screenMode;
    }
    
    /**
     * Update the panels based on the current mode.
     */
    private void updatePanels() {
        
        ScreenMode mode = getMode();
        if (mode == ScreenMode.SESSION_LIST) {
            sessionPanel.setVisible(true);
            teamPanel.setVisible(false);
            hostButton.setEnabled(true);
        } else {
            sessionPanel.setVisible(false);
            teamPanel.setVisible(true);
            
            // Only allow the host to start the session.
            if (isHost) {
                sessionHeaderText.setText(HOST_HEADER_TEXT);
                helpText.setHTML(HOST_HELP_TEXT);
                startSessionButton.setEnabled(true);
                sessionName.setEditingEnabled(true);
                startSessionButton.setType(ButtonType.SUCCESS);
                startSessionButton.setText("Start Session");
                
            } else {
                sessionHeaderText.setText(MEMBER_HEADER_TEXT);
                helpText.setHTML(MEMBER_HELP_TEXT);
                startSessionButton.setEnabled(false);
                sessionName.setEditingEnabled(false);
                hostHelpButton.setVisible(false);
                startSessionButton.setType(ButtonType.DEFAULT);
                startSessionButton.setText("Waiting on Host...");
            }
        }

    }
    
    /**
     * Sends the request to leave the team session.
     */
    private void leaveSession() {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("leaveSession()");
        }
        if (!pendingRequest) {
            pendingRequest = true;
            if (logger.isLoggable(Level.INFO)) {
                logger.info("leaveTeamSessionRequest() sending request to server.");
            }
            tutorUserInterfaceService.leaveTeamSessionRequest(BrowserSession.getInstance().getBrowserSessionKey(), 
                    sessionJoined.getNameOfSession(),
                    sessionJoined.getHostSessionMember().getDomainSessionId(), sessionJoined.getHostSessionMember().getSessionMembership().getUsername(), 
                    new AsyncCallback<GetActiveKnowledgeSessionsResponse>() {
                @Override
                public void onSuccess(GetActiveKnowledgeSessionsResponse response) {
                    
                    if (response.isSuccess()) {
                        
                    
                        if (logger.isLoggable(Level.INFO)) {
                            logger.info("leaveTeamSessionRequest() success.");
                        }
                        
                        // After leaving a session, clear out any previous selection prior to updating the list.
                        setSelectedSession(null);
                        
                        updateSessionListWidget(response.getKnowledgeSessions());
                        
                        // Default the role selection panel to visible.
                        sessionJoined = null;
                        isHost = false;
                        
                        // Switch back to the session list.
                        setMode(ScreenMode.SESSION_LIST);
                        hostButton.setVisible(response.getKnowledgeSessions().canHost());
                        hostHelpButton.setVisible(response.getKnowledgeSessions().canHost());
                    } else {
                        logger.severe("leaveTeamSessionRequest() response was not successful.");
                        Document.getInstance().displayDialogInDashboard("Leave Session Failed.", 
                                "Could not leave the team session.  Reason: " + response.getResponse(), null);
                        updateSessionListWidget(null);
                        hostButton.setVisible(false);
                        hostHelpButton.setVisible(false);
                    }
                    
                    pendingRequest = false;
                    updatePanels();
                }

                @Override
                public void onFailure(Throwable t) {

                    logger.severe("leaveTeamSessionRequest() failure: " + t.getMessage());
                    updateSessionListWidget(null);
                    pendingRequest = false;
                    hostButton.setVisible(false);
                    hostHelpButton.setVisible(false);
                }
            });
        }
    }
    
    /**
     * Fetch the current domain session id of the user and then fetch the current knowledge sessions.
     */
    private void fetchCurrentDomainIdAndSessions() {
        tutorUserInterfaceService.getCurrentDomainId(BrowserSession.getInstance().getBrowserSessionKey(),
                new AsyncCallback<GenericRpcResponse<Integer>>() {

                    @Override
                    public void onFailure(Throwable t) {
                        logger.severe("Unable to fetch current domain id of local user: " + t);
                     
                    }

                    @Override
                    public void onSuccess(GenericRpcResponse<Integer> response) {
                        
                        if (response != null && response.getWasSuccessful()) {
                            localDomainId = response.getContent();
                            fetchActiveKnowledgeSessions();
                        } else {
                            logger.severe("Unable to fetch current domain id of local user, response failed: " + response);
                        }
                       
                    }
        });
    }
    
    /**
     * Fetches the list of active knowledge sessions.
     */
    private void fetchActiveKnowledgeSessions() {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("fetchActiveKnowledgeSessions()");
        }
        if (!pendingRequest) {
            pendingRequest = true;
            if (logger.isLoggable(Level.INFO)) {
                logger.info("fetchActiveKnowledgeSessions() sending request to server.");
            }
            
            tutorUserInterfaceService.fetchActiveKnowledgeSessions(BrowserSession.getInstance().getBrowserSessionKey(), new AsyncCallback<GetActiveKnowledgeSessionsResponse>() {
                @Override
                public void onSuccess(GetActiveKnowledgeSessionsResponse response) {
                    if (logger.isLoggable(Level.INFO)) {
                        logger.info("fetchActiveKnowledgeSessions() success.");
                    }
                    
                    updateSessionListWidget(response.isSuccess() ? response.getKnowledgeSessions() : null);
                    pendingRequest = false;
                    
                    hostButton.setVisible(response.getKnowledgeSessions().canHost());
                    hostHelpButton.setVisible(response.getKnowledgeSessions().canHost());
                }

                @Override
                public void onFailure(Throwable t) {
                    logger.severe("fetchActiveKnowledgeSessions() failure: " + t.getMessage());

                    updateSessionListWidget(null);
                    pendingRequest = false;
                    hostButton.setVisible(false);
                    hostHelpButton.setVisible(false);
                }
            });
        }
        
    }
    
    /**
     * Send the request to host a team session.
     */
    private void hostTeamSession() {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("hostTeamSession()");
        }
        if (!pendingRequest) {
            tutorUserInterfaceService.hostTeamSessionRequest(BrowserSession.getInstance().getBrowserSessionKey(), new AsyncCallback<AbstractKnowledgeSessionResponse>() {
                @Override
                public void onSuccess(AbstractKnowledgeSessionResponse response) {
                    if (logger.isLoggable(Level.INFO)) {
                        logger.info("hostTeamSessionRequest() success.");
                    }
                    
                    pendingRequest = false;
                    
                    if (response.isSuccess()) {
                        
                        AbstractKnowledgeSession session = response.getKnowledgeSession();
                        
                        if (session != null) {

                                    
                            if (session instanceof TeamKnowledgeSession) {
                                
                                hostButton.setEnabled(false);
                                isHost = true;
                                setMode(ScreenMode.TEAM_LIST);
                                
                                refreshUserSessionWidgets((TeamKnowledgeSession)session);
                            } else {
                                logger.severe("hostTeamSessionRequest() returned an invalid session of type; " + session.getClass());
                                Document.getInstance().displayDialogInDashboard("Host Session Failed.", 
                                        "The server returned an invalid session of type; " + session.getClass(), null);
                            }
                            
                        } else {
                            logger.severe("hostTeamSessionRequest() returned a null session.");
                            Document.getInstance().displayDialogInDashboard("Host Session Failed.", "The server returned a null session.", null);
                        }
                        
                       
                    } else {
                        logger.severe("hostTeamSessionRequest() returned failure: " + response);
                        Document.getInstance().displayDialogInDashboard("Host Session Failed.", response.getResponse(), null);
                        
                        // Stay on the session list.
                    }
                   
                    // refresh the ui.
                    updatePanels();
                }

                

                @Override
                public void onFailure(Throwable t) {
                    
                    logger.severe("hostTeamSessionRequest() failure: " + t.getMessage());
                    updateSessionListWidget(null);
                    pendingRequest = false;
                    hostButton.setEnabled(true);
                    isHost = false;
                }
            });
        }
        
    }
    
    /**
     * Updates the team structure.  This structure should not change once the session is hosted.
     * 
     */
    private void rebuildTeamStructure() {

        
        if (sessionJoined != null) {
            rebuildTeamTree(sessionJoined.getTeamStructure());
        }
    }
    
    /**
     * Gets the assigned role name of the local user.  
     * 
     * @return The assigned role name of the user (or empty string if not assigned).
     */
    public String getAssignedRoleName() {
        String roleName = "";
        if (isHost) {
            if (sessionJoined.getHostSessionMember().getSessionMembership().getTeamMember() != null) {
                roleName = sessionJoined.getHostSessionMember().getSessionMembership().getTeamMember().getName();
            } else {
                if (sessionJoined != null) {
                    // populate the list of users.
                    for (SessionMember member : sessionJoined.getJoinedMembers().values()) {
                        if (member.getDomainSessionId() == localDomainId) {
                            if (member.getSessionMembership().getTeamMember() != null) {
                                roleName = member.getSessionMembership().getTeamMember().getName();
                                break;
                            }
                        }
                    }
                }
            }
        }
        
        return roleName;
    }
    
    
    /**
     * Requests to assign the user to a specific role on a team.
     * 
     * @param roleName The name of the role to be assigned to.
     * @param callback The callback used to indicate if the rpc response was successful or not. Cannot be null.
     */
    public void selectTeamRole(String roleName, final SuccessFailCallback callback) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("selectTeamRole()");
        }
        
        if (!pendingRequest) {
            
            final String roleToUse = roleName;
            tutorUserInterfaceService.selectTeamMemberRole(BrowserSession.getInstance().getBrowserSessionKey(), roleToUse, 
                    sessionJoined.getHostSessionMember().getDomainSessionId(),
                    new AsyncCallback<GetActiveKnowledgeSessionsResponse>() {
                @Override
                public void onSuccess(GetActiveKnowledgeSessionsResponse response) {
                    if (logger.isLoggable(Level.INFO)) {
                        logger.info("selectTeamRole() success.");
                    }
                    
                    pendingRequest = false;
                    
                    if (response.isSuccess()) {
                        // Assignment success.
                        // The server will send an KnowledgeSessionsUpdated message.  Do nothing for now.
                        callback.onSuccess();
                        
                        
                    } else {
                        
                        // This gets displayed as a notification, so no need to log an error here since it is not
                        // a fatal error.
                        if (logger.isLoggable(Level.INFO)) {
                            logger.info("selectTeamRole() returned failure: " + response);
                        }
                        
                        callback.onFailure();

                        // Display a notification in the dashboard.
                        Document.getInstance().displayNotificationInDashboard(response.getResponse(), 
                                IconType.EXCLAMATION_CIRCLE.getCssName());
                        
                    }
                   
                    // refresh the ui.
                    updatePanels();
                }

                @Override
                public void onFailure(Throwable t) {
                    
                    logger.severe("assignTeamMember() failure: " + t.getMessage());
                    callback.onFailure();
                    updateSessionListWidget(null);
                    pendingRequest = false;
                    hostButton.setEnabled(true);
                    isHost = false;
                }
            }); 
        }
    }
    
    /**
     * Unassigns the user from the team role.
     * 
     * @param roleName The role name to be unassigned from
     * @param callback The callback used to indicate if the rpc response was successful or not. Cannot be null.
     */
    public void unassignTeamRole(String roleName, final SuccessFailCallback callback) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("unassignTeamRole()");
        }
        
        if (!pendingRequest) {
            
            final String roleToUse = roleName;
            tutorUserInterfaceService.unassignTeamMemberRole(BrowserSession.getInstance().getBrowserSessionKey(), roleToUse, 
                    sessionJoined.getHostSessionMember().getDomainSessionId(),
                    new AsyncCallback<GetActiveKnowledgeSessionsResponse>() {
                @Override
                public void onSuccess(GetActiveKnowledgeSessionsResponse response) {
                    if (logger.isLoggable(Level.INFO)) {
                        logger.info("unassignTeamRole() success.");
                    }
                    
                    pendingRequest = false;
                    
                    if (response.isSuccess()) {
                        // Assignment success.
                        // The server will send an KnowledgeSessionsUpdated message.  Do nothing for now.
                        callback.onSuccess();
                        
                    } else {
                        logger.severe("unassignTeamRole() returned failure: " + response);
                        callback.onFailure();
                        Document.getInstance().displayDialogInDashboard("Unassign Team Role Failed", response.getResponse(), null);
                        
                    }
                   
                    // refresh the ui.
                    updatePanels();
                }

                @Override
                public void onFailure(Throwable t) {
                    
                    logger.severe("unassignTeamRole() failure: " + t.getMessage());
                    callback.onFailure();
                    updateSessionListWidget(null);
                    pendingRequest = false;
                    hostButton.setEnabled(true);
                    isHost = false;
                }
            }); 
        }
        
    }
    

    /**
     * Send the request to start a team session.
     */
    private void startTeamSession() {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("startTeamSession()");
        }
        if (!pendingRequest) {
            
            // prevent multiple clicks and stop the animation that is on the button
            startSessionButton.setText("Starting ...");
            startSessionButton.setEnabled(false);
            Animate.stopAnimation(startSessionButton, Animation.PULSE.getCssName());
            
            tutorUserInterfaceService.startTeamSessionRequest(BrowserSession.getInstance().getBrowserSessionKey(), new AsyncCallback<DetailedRpcResponse>() {
                @Override
                public void onSuccess(DetailedRpcResponse response) {
                    if (logger.isLoggable(Level.INFO)) {
                        logger.info("startTeamSessionRequest() success.");
                    }
                    
                    pendingRequest = false;
                    
                    if (response.isSuccess()) {
                        // Nothing to do here, the domain module will start transitioning into the training app.
                        // Keep the button locked so the user can't try to start again.
                        startSessionButton.setEnabled(false);
                        sessionName.setEditingEnabled(false);
                        
                    } else {
                        
                        if (logger.isLoggable(Level.INFO)) {
                            // Not a fatal error, so just log as info.
                            logger.info("startTeamSessionRequest() returned failure: " + response); 
                        }
                        Document.getInstance().displayNotificationInDashboard(response.getResponse(), IconType.EXCLAMATION_CIRCLE.getCssName());
                        
                        startSessionButton.setEnabled(true);
                        
                        // Stay on the session list.
                    }

                    // refresh the ui.
                    updatePanels();
                }

                @Override
                public void onFailure(Throwable t) {
                    
                    logger.severe("startTeamSessionRequest() failure: " + t.getMessage());
                    updateSessionListWidget(null);
                    pendingRequest = false;
                    hostButton.setEnabled(true);
                    startSessionButton.setEnabled(true);
                    isHost = false;
                }
            });
        }
        
    }
    
    /**
     * Attempts to get the course name from a full course id from the server.
     * If this is not successful, the original full course id is returned.
     * 
     * @param fullCourseId the course id to get the name from
     * @return The course name that is contained within the course id.  If no conversion is done, 
     * the full course id is returned.
     */
    private String getCourseNameFromId(String fullCourseId) {
        
        // Default to the full course id
        String finalCourseId = fullCourseId;
        if(fullCourseId.contains(Constants.FORWARD_SLASH)){
            int courseFolderEnd = fullCourseId.lastIndexOf(Constants.FORWARD_SLASH);
            String coursePath = fullCourseId.substring(0, courseFolderEnd);
            int courseFolderStart = coursePath.lastIndexOf(Constants.FORWARD_SLASH);
            if(courseFolderStart != -1){
                finalCourseId = coursePath.substring(courseFolderStart+1);
            }else{
                finalCourseId = coursePath;
            }
        } 
        
        return finalCourseId;
    }
    
    /**
     * Rebuilds the UI panel based on the latest session data.  The host is identified so it is clear for other
     * members who the host is. This should be used mainly on initial screen load.  Calling this all the time will
     * cause the DOM elements to be rebuilt and can cause screen flickering if the user has widgets opened.
     */
    private void rebuildUserSessionWidgets() {


        userList.clear();
        userSessionWidgets.clear();
        logger.info("rebuildUserSessionWidgets()\n"+sessionJoined);
        if (sessionJoined != null) {
            
            // Populate the session name
            sessionName.setValue(sessionJoined.getNameOfSession());
            // Populate the course id - only show the root name if possible.
            String finalCourseId = getCourseNameFromId(sessionJoined.getCourseSourceId());
            courseId.setText(finalCourseId);
            // populate the list of users.
            boolean isLocalUser = false;
            int hostDomainId = sessionJoined.getHostSessionMember().getDomainSessionId();
            // Add the host            
            if (sessionJoined.getHostSessionMember().getDomainSessionId() == localDomainId) {
                isLocalUser = true;
                localUser = sessionJoined.getHostSessionMember();
            }
            
            UserSessionWidget hostWidget = new UserSessionWidget(sessionJoined.getHostSessionMember(), UserType.HOST, isLocalUser, isHost);
            userSessionWidgets.put(hostDomainId, hostWidget);
            userList.add(hostWidget);

            // Add any additional members.
            for (SessionMember member : sessionJoined.getJoinedMembers().values()) {
                
                isLocalUser = false;
                if (member.getDomainSessionId() == localDomainId) {
                    isLocalUser = true;
                    localUser = member;
                }
                
                UserSessionWidget memberWidget = new UserSessionWidget(member, UserType.USER, isLocalUser, isHost, new KickMemberCallback() {
                    
                    @Override
                    public void kickMember(SessionMember member) {
                        kickFromSession(member);
                    }
                });
                userSessionWidgets.put(member.getDomainSessionId(), memberWidget);
                userList.add(memberWidget);
            }    
            
            updateJoinedLabel(sessionJoined);
        }
       
        
        // On a full rebuild, show the selection panel.
        rebuildTeamStructure();
        updateTeamTreeSelection();
    }
    
    /**
     * Sends the request to kick the given user from the team session.
     */
    private void kickFromSession(final SessionMember member) {
        
        if(member != null && isHost) {
            
            //only kick the given user if the host is sending the request
            if (logger.isLoggable(Level.INFO)) {
                logger.info("kickFromSession(" + member + ")");
            }
            
            final ModalDialogBox confirmDialog = new ModalDialogBox();
            confirmDialog.setGlassEnabled(true);
            confirmDialog.setCloseable(true);
            confirmDialog.getCloseButton().setText("No");
            confirmDialog.setHtml("Kick from Session?");
            confirmDialog.setWidget(new HTML("Are you sure you want to kick <b>" + member.getSessionMembership().getUsername() +"</b> from this team session?"));
            confirmDialog.setFooterWidget(new Button("Yes", new ClickHandler() {
                
                @Override
                public void onClick(ClickEvent event) {
                    
                    confirmDialog.hide();
                    
                    if (!pendingRequest) {
                        pendingRequest = true;
                        if (logger.isLoggable(Level.INFO)) {
                            logger.info("kickFromTeamSessionRequest() sending request to server.");
                        }
                        
                        tutorUserInterfaceService.kickFromTeamSessionRequest(BrowserSession.getInstance().getBrowserSessionKey(), 
                                sessionJoined.getHostSessionMember().getDomainSessionId(), 
                                member,
                                new AsyncCallback<GetActiveKnowledgeSessionsResponse>() {
                            @Override
                            public void onSuccess(GetActiveKnowledgeSessionsResponse response) {
                                
                                if (!response.isSuccess()) {
                                    logger.severe("kickFromTeamSessionRequest() response was not successful.");
                                }
                                
                                pendingRequest = false;
                                updatePanels();
                            }

                            @Override
                            public void onFailure(Throwable t) {
                                logger.severe("kickFromTeamSessionRequest() failure: " + t.getMessage());
                                pendingRequest = false;
                            }
                        });
                    }
                }
            }));
            
            confirmDialog.center();
            confirmDialog.getElement().getStyle().setProperty("top", "20%"); //move dialog up slightly
        }
    }
    
    /**
     * Sends the request to change the name of the team session.
     */
    private void changeSessionName(final String sessionName) {
        
        if(StringUtils.isNotBlank(sessionName) 
                && !sessionName.equals(oldSessionName)
                && isHost) {
            
            //only change the session name if the host is sending the request, the new name is not empty, and the new name is different
            if (logger.isLoggable(Level.INFO)) {
                logger.info("changeSessionName(" + sessionName+ ")");
            }
            
            final ModalDialogBox confirmDialog = new ModalDialogBox();
            confirmDialog.setGlassEnabled(true);
            confirmDialog.setCloseable(true);
            confirmDialog.getCloseButton().setText("No");
            confirmDialog.setHtml("Change Session Name?");
            confirmDialog.setWidget(new HTML("Are you sure you want to change the name of this team session to <b>" + sessionName +"</b>?"));
            confirmDialog.setFooterWidget(new Button("Yes", new ClickHandler() {
                
                @Override
                public void onClick(ClickEvent event) {
                    
                    confirmDialog.hide();
                    
                    if (!pendingRequest) {
                        pendingRequest = true;
                        if (logger.isLoggable(Level.INFO)) {
                            logger.info("changeTeamSessionNameRequest() sending request to server.");
                        }
                        
                        tutorUserInterfaceService.changeTeamSessionNameRequest(BrowserSession.getInstance().getBrowserSessionKey(), 
                                sessionJoined.getHostSessionMember().getDomainSessionId(), 
                                sessionName,
                                new AsyncCallback<GetActiveKnowledgeSessionsResponse>() {
                            @Override
                            public void onSuccess(GetActiveKnowledgeSessionsResponse response) {
                                
                                if (!response.isSuccess()) {
                                    logger.severe("changeTeamSessionNameRequest() response was not successful.");
                                }
                                
                                pendingRequest = false;
                                updatePanels();
                            }

                            @Override
                            public void onFailure(Throwable t) {
                                logger.severe("changeTeamSessionNameRequest() failure: " + t.getMessage());
                                pendingRequest = false;
                            }
                        });
                    }
                }
            }));
            confirmDialog.getCloseButton().addClickHandler(new ClickHandler() {
                
                @Override
                public void onClick(ClickEvent event) {
                    
                    //changing the session name was denied, so revert to the old name
                    TeamSessionWidget.this.sessionName.setValue(oldSessionName);
                    oldSessionName = null;
                }
            });
            
            confirmDialog.center();
            confirmDialog.getElement().getStyle().setProperty("top", "20%"); //move dialog up slightly
            
        } else {
            
            //changing the session name was denied, so revert to the old name
            this.sessionName.setValue(oldSessionName);
            oldSessionName = null;
        }
    }

    /**
     * Updates the text on the User Joined label to show how many current users out of the total possible
     * there are.
     * 
     * @param sessionData The data containing the session information.  Cannot be null.
     */
    private void updateJoinedLabel(TeamKnowledgeSession sessionData) {
        // Get current members joined (add one for host).
        int currentJoined = sessionData.getJoinedMembers().size() + 1;
        
        // Set the header.  Update user label based on plural users.
        String userLabel = " User (";
        if (currentJoined > 1) {
            userLabel = " Users (";
        }
        joinedHeader.setText(currentJoined + userLabel + sessionData.getTotalPossibleTeamMembers() + " Max)");
    }
    
    /**
     * Request to join a session.
     */
    private void joinSessionRequest() {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("joinSessionRequest");
        }
        
        if (!pendingRequest) {
            //sessionListWidget.
            tutorUserInterfaceService.joinSessionRequest(BrowserSession.getInstance().getBrowserSessionKey(), selectedSession, 
                    new AsyncCallback<AbstractKnowledgeSessionResponse>() {
                @Override
                public void onSuccess(AbstractKnowledgeSessionResponse response) {
                    
                    pendingRequest = false;
                    if (response.isSuccess()) {
                        if (logger.isLoggable(Level.INFO)) {
                            logger.info("joinSessionRequest() success.");
                        }
                        
                        
                        if (response.getKnowledgeSession() != null) {
                            
                            AbstractKnowledgeSession session = response.getKnowledgeSession();
                            if (session instanceof TeamKnowledgeSession) {
                                hostButton.setEnabled(false);
                                isHost = false;
                                setMode(ScreenMode.TEAM_LIST);
                                refreshUserSessionWidgets((TeamKnowledgeSession)session);

                                // refresh the ui.
                                updatePanels();
                            } else {
                                String errorMsg = "Could not join the team session, the server returned an invalid session type of: " + session.getClass();
                                logger.severe(errorMsg);
                                Document.getInstance().displayDialogInDashboard("Join Session Failure", errorMsg, null);
                            }
                           
                        } else {
                            String errorMsg = "Could not join the team session, the server returned a null session.";
                            logger.severe(errorMsg);
                            Document.getInstance().displayDialogInDashboard("Join Session Failure", errorMsg, null);
                        }
                        
                    } else {
                        String errorMsg = response.getResponse();
                        logger.severe(errorMsg);
                        Document.getInstance().displayDialogInDashboard("Join Session Failure", errorMsg, null);
                    }
                    
                }

                @Override
                public void onFailure(Throwable t) {
                    
                    logger.severe("joinSessionRequest() failure: " + t.getMessage());
                    
                    updateSessionListWidget(null);
                    pendingRequest = false;
                    isHost = false;
                }
            });
        }
    }

    @Override
    public void onKnowledgeSessionUpdated(KnowledgeSessionsReply knowledgeSessionsReply) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("onKnowledgeSessionUpdated");
        }
        
        updateSessionListWidget(knowledgeSessionsReply);

        if (sessionJoined != null) {
            
            boolean foundSession = false;
            
            if (knowledgeSessionsReply != null) {
                for (AbstractKnowledgeSession session : knowledgeSessionsReply.getKnowledgeSessionMap().values()) {
                    if (sessionJoined.getHostSessionMember().getDomainSessionId() == session.getHostSessionMember().getDomainSessionId()) {
                        foundSession = true;

                        if (session instanceof TeamKnowledgeSession) {
                            TeamKnowledgeSession teamSession = (TeamKnowledgeSession)session;
                            refreshUserSessionWidgets(teamSession);
                            
                            if (isHost) {
                                updateHostButton(teamSession);
                            }
                            break;
                        }
                    }
                }
            }
            
            if (!foundSession && !isHost) {
                // The session we joined is no longer available and we've been kicked out.          
                sessionJoined = null;
                isHost = false;
                setMode(ScreenMode.SESSION_LIST);
                // Default the role selection panel to true if it gets opened again.
                Document.getInstance().displayDialogInDashboard("Removed From Session", "The session is no longer available.  The host may have closed the session.  Try and join or host a new session.", null);
            }
        }
        
        
        
        // refresh the ui.
        updatePanels();
    }
    
    /**
     * Updates the host "start session" button based on the latest team session status.
     * If all the team members are assigned, then the button style is changed to better
     * show that the session can be started.  Otherwise the default style is applied. 
     * 
     * @param teamSession The team session data holding the latest status of the session.  Cannot be null.
     */
    private void updateHostButton(TeamKnowledgeSession teamSession) {
        
        boolean allAssigned = true;
        
        if (teamSession.getHostSessionMember().getSessionMembership().getTeamMember() == null) {
            allAssigned = false;
        }
        
        if (teamSession.getJoinedMembers() != null) {
            for (SessionMember member : teamSession.getJoinedMembers().values()) {
                if (member.getSessionMembership().getTeamMember() == null) {
                    allAssigned = false;
                    break;
                }
            }
        }
        
        if (allAssigned) {
            startSessionButton.setEnabled(true);
            Animate.animate(startSessionButton, Animation.PULSE, -1);
            
            // Send a notification to the host as well if we haven't sent one for awhile.
            Date now = new Date();
            if (lastNotifyHost == null || (now.getTime() - lastNotifyHost.getTime() > NOTIFY_HOST_DURATION)) {
                Document.getInstance().displayNotificationInDashboard(HOST_NOTIFY_REMINDER, IconType.EXCLAMATION_CIRCLE.getCssName());
                
                lastNotifyHost = now;
            }
            
        } else {
            startSessionButton.setEnabled(false);
            Animate.stopAnimation(startSessionButton, Animation.PULSE.getCssName());
        }

        
    }

    /**
     * Update the session list widget based on the knowledge session reply data.  Null can be passed in
     * to show an empty list.
     * 
     * @param knowledgeSessionsReply The list of session data to populate the list with.  Can be null to show an empty list.
     */
    private void updateSessionListWidget(KnowledgeSessionsReply knowledgeSessionsReply) {
        // Populate the session list widget. This rebuilds the widget.
        Collection<AbstractKnowledgeSession> sessions = null;
        if (knowledgeSessionsReply != null && knowledgeSessionsReply.getKnowledgeSessionMap() != null) {
            sessions = knowledgeSessionsReply.getKnowledgeSessionMap().values();
        }

        sessionListWidget.populateWidget(sessions);

        boolean foundOldSelection = false;
        // Check to see if the selected session is still available.
        if (knowledgeSessionsReply != null ) {

            if (knowledgeSessionsReply.getKnowledgeSessionMap() != null && !knowledgeSessionsReply.getKnowledgeSessionMap().isEmpty()) {
                Collection<AbstractKnowledgeSession> sessionList = knowledgeSessionsReply.getKnowledgeSessionMap().values();
                for (AbstractKnowledgeSession session: sessionList) {

                    int selectedSessionId = -1;
                    if (selectedSession != null && selectedSession.getHostSessionMember() != null) {
                        selectedSessionId = selectedSession.getHostSessionMember().getDomainSessionId();
                    }
                    
                    // Compare based on the session ids
                    if (session.getHostSessionMember() != null && session.getHostSessionMember().getDomainSessionId() == selectedSessionId) {
                        foundOldSelection = true;
                        // The selection still exists, so keep it selected
                        setSelectedSession(selectedSession);
                        break;
                    }
                }
            }
            
        }
        
        if (!foundOldSelection) {
         // Cannot find the selected session, so clear it out.
            setSelectedSession(null);
        }
        
    }
    
    /**
     * Refreshes the current list of user session widgets based on the session data.
     * This should be the main update / refresh method for the UI since it will not rebuild the DOM elements, but instead
     * try to determine what has changed and update based on the changes.
     * 
     * @param session The session data to update the widgets from.  Should not be null.
     */
    private void refreshUserSessionWidgets(TeamKnowledgeSession session) {
        
        if(sessionJoined != null) {
            
            //need to determine if this user is still a part of this session
            boolean doesSessionContainLocalUser = false;
            
            if(session.getHostSessionMember() != null && localDomainId.equals(session.getHostSessionMember().getDomainSessionId())) {
                doesSessionContainLocalUser = true;
                
            } else {
                
                for(Integer domainSessionId : session.getJoinedMembers().keySet()) {
                    if(localDomainId.equals(domainSessionId)) {
                        doesSessionContainLocalUser = true;
                        break;
                    }
                }
            }
        
            if(!doesSessionContainLocalUser) {
                
                //return to the session selection screen as if this user left the session
                setSelectedSession(null);
                
                // Default the role selection panel to visible.
                sessionJoined = null;
                isHost = false;
                
                // Switch back to the session list.
                setMode(ScreenMode.SESSION_LIST);
                
                return;
            }
        }
        
        boolean updatingExistingWidgets = true;
        
        // Look through the session data to see what has changed.
        if (sessionJoined == null && session != null) {
            
            // Update everything
            sessionJoined = session;
            rebuildUserSessionWidgets();
            
            //widgets were totally cleared, so no existing widgets need to refresh
            updatingExistingWidgets = false;
        } 
                
        //track the names of any team member roles that have already been chosen by users
        HashMap<String, String> roleToAssignedUser = new HashMap<String, String>();

        // Compare the incoming session data with the old.
        // Check existing members.
        // Check Host
        SessionMember hostMember = session.getHostSessionMember();

        if(updatingExistingWidgets) {
            
            //host may have changed the session name, so make sure the displayed name is up to date
            sessionName.setValue(session.getNameOfSession());
            
            UserSessionWidget hostWidget = userSessionWidgets.get(hostMember.getDomainSessionId());
            
            if (hostWidget != null) {
                // Update the existing host widget.
                hostWidget.updateWidget(hostMember, UserType.HOST, isHost);
            } else {
                // add a new host widget.
                boolean isLocalUser = false;
                int hostDomainId = sessionJoined.getHostSessionMember().getDomainSessionId();
                // Add the host            
                if (sessionJoined.getHostSessionMember().getDomainSessionId() == localDomainId) {
                    isLocalUser = true;
                    localUser = sessionJoined.getHostSessionMember();
                }
                
                UserSessionWidget newHostWidget = new UserSessionWidget(sessionJoined.getHostSessionMember(), UserType.HOST, isLocalUser, isHost);
                userSessionWidgets.put(hostDomainId, hostWidget);
                userList.add(newHostWidget);
            }
        }
        
        if(hostMember.getSessionMembership().getTeamMember() != null) {
            roleToAssignedUser.put(hostMember.getSessionMembership().getTeamMember().getName(), hostMember.getSessionMembership().getUsername());
        }
            
        // Check Members
        for (SessionMember member : session.getJoinedMembers().values()) {
            
            if(updatingExistingWidgets) {
                
                UserSessionWidget memberWidget = userSessionWidgets.get(member.getDomainSessionId());
                if (memberWidget != null) {
                    memberWidget.updateWidget(member, UserType.USER, isHost);
                } else {
                    boolean isLocalUser = false;
                    if (member.getDomainSessionId() == localDomainId) {
                        isLocalUser = true;
                        localUser = member;
                    }
                    UserSessionWidget newMemberWidget = new UserSessionWidget(member, UserType.USER, isLocalUser, isHost, new KickMemberCallback() {
                        
                        @Override
                        public void kickMember(SessionMember member) {
                            kickFromSession(member);
                        }
                    });
                    userSessionWidgets.put(member.getDomainSessionId(), newMemberWidget);
                    userList.add(newMemberWidget);
                }
            }
            
            if(member.getSessionMembership().getTeamMember() != null) {
                roleToAssignedUser.put(member.getSessionMembership().getTeamMember().getName(), member.getSessionMembership().getUsername());
            }
        }
        
        if(updatingExistingWidgets) {
            
            // Update the UI if users left.
            for (SessionMember member : sessionJoined.getJoinedMembers().values()) {
                
                int domainId = member.getDomainSessionId();
                if (session.getJoinedMembers().get(domainId) == null) {
                    // Remove the user widget.
                    removeUserWidget(domainId);
                }
            }
            
            updateJoinedLabel(session);
            
            sessionJoined = session;
        }
        
        // Update the team tree so that all of its team member items visually indicate whether they are available for users to pick
        HashMap<String, TeamTreeWidgetItem> availableRoles = new HashMap<>(roleToTreeItem);
        
        for(String role : roleToAssignedUser.keySet()) {
            
            TeamTreeWidgetItem item = availableRoles.get(role);
            if(item != null) {
                item.onAssignedUserChanged(roleToAssignedUser.get(role));
                availableRoles.remove(role);
            }
        }
        
        for(TeamTreeWidgetItem item : availableRoles.values()) {
            item.onAssignedUserChanged(null);
        }
    }
    
    /**
     * Removes the user widget based on the domain id
     * 
     * @param domainId The domain id to remove from the list of users.
     */
    private void removeUserWidget(int domainId) {
        
        Iterator<Widget> iter = userList.iterator();
        
        while(iter.hasNext()) {
            Widget widget = iter.next();
            if (widget instanceof UserSessionWidget) {
                UserSessionWidget userWidget = (UserSessionWidget)widget;
                if (userWidget.getSessionMemberData().getDomainSessionId() == domainId) {
                    userSessionWidgets.remove(domainId);
                    userList.remove(userWidget);
                    break;
                }
            }
        }
        
    }

    @Override 
    protected void onUnload() {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("onUnload()");
        }
                
        // Clear out the knowledge session listener.
        BrowserSession.getInstance().setKnowledgeSessionListener(null);
    }
    
    /**
     * Updates the selection in the team tree
     */
    public void updateTeamTreeSelection() {

        if (localUser != null && localUser.getSessionMembership().getTeamMember() != null) {
            selectTreeItemByRoleName(localUser.getSessionMembership().getTeamMember().getName());
        } else {
            teamTree.setSelectedItem(null, false);
        }        
    }    
    
    /**
     * Searches the team tree for the role name and selects the item if found.
     * 
     * @param roleName The role name to select
     */
    private void selectTreeItemByRoleName(String roleName) {
        
        for (int x = 0; x < teamTree.getItemCount(); x++) {
            TreeItem subItem = teamTree.getItem(x);
            
            boolean foundItem = traverseTeamTreeByItem(subItem, roleName);
            if (foundItem) {
                return;
            }
        }
    }
    
    /**
     * Recursively traverses the tree item to find and select the item based on
     * the role name.
     * 
     * @param treeItem The tree item to traverse
     * @param roleName The rolename to find and select
     * @return True if found, false otherwise.
     */
    private boolean traverseTeamTreeByItem(TreeItem treeItem, String roleName) {

        
       boolean foundItem = false;
       Widget widget = treeItem.getWidget();
       if (widget != null && widget instanceof TeamTreeWidgetItem) {
           TeamTreeWidgetItem treeItemWidget = (TeamTreeWidgetItem)widget;
           if (treeItemWidget.getItemName().compareTo(roleName) == 0) {
               teamTree.setSelectedItem(treeItem, false);
               return true;
           }
       }
           
       if (treeItem.getChildCount() > 0) {
           
           for (int x = 0; x < treeItem.getChildCount(); x++) {
               TreeItem subItem = treeItem.getChild(x);
               foundItem = traverseTeamTreeByItem(subItem, roleName);
               if (foundItem) {
                   return true;
               }
           }
       }
           
        return foundItem;
    }
    
    
    /**
     * Rebuilds the team tree based on the team data.
     * 
     * @param teamData Team data used to build the team tree.
     */
    public void rebuildTeamTree(Team teamData) {
        teamTree.clear();
        roleToTreeItem.clear();
        TreeItem root = new TreeItem();
        populateTeamList(teamData, root);
        // Default to open until you see the first selected team member - needs to be done after the item is populated.
        depthFirstExpose(root);
        teamTree.addItem(root);       
    }
    
    /**
     * Recursively expand the team tree until a team member is exposed.
     * 
     * @param parent the node to expand in the tree
     * @return true if a team member is now visible because a parent tree item that is a team was expanded.
     */
    private boolean depthFirstExpose(TreeItem parent){
        
        // expand the team parent tree item
        parent.setState(true);
        
        for(int index = 0; index < parent.getChildCount(); index++){
            
            TreeItem child = parent.getChild(index);
            if(child.getWidget() instanceof TeamTreeWidgetItem){
                TeamTreeWidgetItem teamTreeItem = (TeamTreeWidgetItem)(child.getWidget());
                if (teamTreeItem.isTeamMember()) {
                    //done
                    return true;
                }else{
                    if(depthFirstExpose(child)){
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    /**
     * Recursive method to populate the team tree with new widgets based on the
     * data of the team structure.
     * 
     * @param team The data representing the team structure
     * @param tree The tree item that currently is being iterated through
     */
    private void populateTeamList(Team team, TreeItem tree) {
        
        TeamTreeWidgetItem treeItemWidget = new TeamTreeWidgetItem(IconType.USERS, team.getName());
        tree.setWidget(treeItemWidget);

        for (AbstractTeamUnit unit : team.getUnits()) {
            if (unit instanceof Team) {
                Team subTeam = (Team)unit;
                if(subTeam.hasPlayableTeamMember()){
                    TreeItem treeItem = new TreeItem();
                    populateTeamList(subTeam, treeItem);
                    tree.setState(false);
                    tree.addItem(treeItem);
                }
            } else if(unit instanceof TeamMember<?>) {
                TeamMember<?> teamMember = (TeamMember<?>)unit;
                if(teamMember.isPlayable()){
                    
                    final TeamTreeWidgetItem userItem = new TeamTreeWidgetItem(IconType.USER, unit.getName());
                    TreeItem item = new TreeItem(userItem) {
                        
                        @Override
                        public void setSelected(boolean selected) {
                            super.setSelected(selected);
                            
                            //visually update this tree item when it's selection state changes
                            userItem.onSelectedStateChanged(selected);
                        }
                    };
                    
                    tree.addItem(item);
                    roleToTreeItem.put(unit.getName(), userItem);
                }
            }
        }
    }

   
}
