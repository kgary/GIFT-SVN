/*
* Copyright Dignitas Technologies, LLC
* This file and its contents are governed by one or more distribution and
* copyright statements as described in the LICENSE.txt file distributed with
* this work.
*/            
package mil.arl.gift.tools.dashboard.client.bootstrap.webmonitor.message;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SingleSelectionModel;

import mil.arl.gift.common.DomainSession;
import mil.arl.gift.common.UserSession;
import mil.arl.gift.common.gwt.client.GenericRpcResponse;
import mil.arl.gift.tools.dashboard.client.UiManager;
import mil.arl.gift.tools.dashboard.client.bootstrap.webmonitor.util.UsersStateProvider;
import mil.arl.gift.tools.dashboard.client.bootstrap.webmonitor.util.UsersStateProvider.UsersStatusChangeHandler;

/**
 * A panel used to display all of the domain sessions that are actively being run in GIFT.
 * This panel works in tandem with the rest of the web monitor messaging view in order to display
 * the message traffic for specific active sessions.
 * 
 * @author nroberts
 */
public class ActiveSessionsPanel extends Composite implements UsersStatusChangeHandler{

    private static ActiveSessionsPanelUiBinder uiBinder = GWT.create(ActiveSessionsPanelUiBinder.class);

    interface ActiveSessionsPanelUiBinder extends UiBinder<Widget, ActiveSessionsPanel> {
    }
    
    /** The main panel that houses the active user sessions table */
    @UiField
    protected FlowPanel mainPanel;
    
    /** The user sessions table that contains the active sessions */
    @UiField
    protected CellTable<UserSession> userSessionsTable = new CellTable<UserSession>();
    
    /** 
     * The active users data provider. Used in conjunction with the {@link userSessionsTable}, 
     * the table is updated with active users when a change of users is detected.
     */  
    private ListDataProvider<UserSession> activeSessionsDataProvider = new ListDataProvider<UserSession>(new ArrayList<UserSession>());

    /**
     * Creates a new panel for displaying the active sessions
     */
    public ActiveSessionsPanel() {
        initWidget(uiBinder.createAndBindUi(this));
        
        UsersStateProvider.getInstance().addHandler(this);
        initUserSessionsTable();  
    }
    
    /**
     * The read only user constant from common.properties to check is a user should be anonymized.
     */
    private final String READ_ONLY_USER = UiManager.getInstance().getReadOnlyUser();
    
    /**
     * The name GIFT will use to display a read only user in active sessions.
     */
    private final String ANONYMOUS_USER = "Anonymized";
    
    /**
     * Initializes the active user sessions table.
     */
    private void initUserSessionsTable() {
        userSessionsTable.setPageSize(Integer.MAX_VALUE);
        
        /* The column that displays the username of the active user session */
        Column<UserSession, SafeHtml> usernameColumn = new Column<UserSession, SafeHtml>(new SafeHtmlCell()) {
           @Override
           public SafeHtml getValue(UserSession userSession) {
               if (userSession.getUsername().equals(READ_ONLY_USER)) {
                   
                   /* Show italicized text to indicate that this isn't a real username */
                   return new SafeHtmlBuilder()
                           .appendHtmlConstant("<i>")
                           .appendEscaped(ANONYMOUS_USER)
                           .appendHtmlConstant("</i>")
                           .toSafeHtml();
               }
               
               return new SafeHtmlBuilder()
                       .appendEscaped(userSession.getUsername())
                       .toSafeHtml();
           }
        };
        
        usernameColumn.setSortable(true);

        /* The column that displays the user id of the active user session */
        Column<UserSession, String> userIdColumn = new Column<UserSession, String>(new TextCell()) {
           @Override
           public String getValue(UserSession userSession) {
               return String.valueOf(userSession.getUserId());
           }
        };
        
        userIdColumn.setSortable(true);
        
        /* The column that displays the user type of the active user session */
        Column<UserSession, String> userTypeColumn = new Column<UserSession, String>(new TextCell()) {
           @Override
           public String getValue(UserSession userSession) {
               return userSession.getSessionType().name();
           }
        };
        
        userTypeColumn.setSortable(true);
        
        /* 
         * The column that displays the domain session id of the active user. This assumes that the 
         * user session is of type domain session, which is warranted since the table populates once 
         * an active session is detected. 
         */
        Column<UserSession, String> sessionIdColumn = new Column<UserSession, String>(new TextCell()) {
           @Override
           public String getValue(UserSession userSession) {
               /* If this user session is a domain session, return the user id. */
               if (userSession instanceof DomainSession) {
                   return String.valueOf(((DomainSession) userSession).getDomainSessionId());
               } else {
                   /* No domain session is currently running. */
                   return null;
               }     
           }
        };
        
        sessionIdColumn.setSortable(true);
        
        userSessionsTable.addColumn(usernameColumn, "Username");
        userSessionsTable.setColumnWidth(usernameColumn, "25px");
        
        userSessionsTable.addColumn(userIdColumn, "User ID");
        userSessionsTable.setColumnWidth(userIdColumn, "25px");
        
        userSessionsTable.addColumn(userTypeColumn, "User Type");
        userSessionsTable.setColumnWidth(userTypeColumn, "25px");
        
        userSessionsTable.addColumn(sessionIdColumn, "Session ID");
        userSessionsTable.setColumnWidth(sessionIdColumn, "25px");
        
        userSessionsTable.setEmptyTableWidget(new HTML(""
                + "<span style='font-size: 12pt;'>"
                +   "There are currently no active user sessions to display."
                + "</span>"));
        
        /* Connect the user sessions table to the data provider and add all user sessions. */
        activeSessionsDataProvider.addDataDisplay(userSessionsTable);
        activeSessionsDataProvider.getList().addAll(initActiveSessions());
 
        final SingleSelectionModel<UserSession> selectionModel = new SingleSelectionModel<>();
        
        userSessionsTable.setSelectionModel(selectionModel);
        selectionModel.addSelectionChangeHandler(new Handler() {
            
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                
                UserSession selected = selectionModel.getSelectedObject();
                if(selected == null) {
                    return;
                    
                } else if(!(selected instanceof DomainSession)) {
                    
                    /* Deselect the selected item so it can be clicked on again */
                    selectionModel.setSelected(selected, false);
                    return;
                }
                selectionModel.setSelected(selected, false);
                final int domainSessionId = ((DomainSession) selected).getDomainSessionId();
                
                UiManager.getInstance().getDashboardService().watchDomainSession(
                        UiManager.getInstance().getUserName(), 
                        UiManager.getInstance().getSessionId(), 
                        domainSessionId, 
                        new AsyncCallback<GenericRpcResponse<Void>>() {
                    
                    @Override
                    public void onSuccess(GenericRpcResponse<Void> result) {
                        
                        if(!result.getWasSuccessful()) {
                            UiManager.getInstance().displayDetailedErrorDialog("Failed to Watch Domain Session", 
                                    result.getException());
                            
                            return;
                        }
                    }
                    
                    @Override
                    public void onFailure(Throwable caught) {
                        
                        UiManager.getInstance().displayDetailedErrorDialog("Failed to Watch Domain Session", 
                                "The selected domain session could not be watched because an unexpected error occured", 
                                "An exception was thrown while watching " + domainSessionId + ": " + caught, 
                                null, null);
                    }
                });
            }
        });  
        

        /**
         * List handler to connect sorting to lists
         */
        ListHandler<UserSession> columnSortHandler = new ListHandler<UserSession>(activeSessionsDataProvider.getList());
        
        /**
         * comparator for sessionId column
         */
        columnSortHandler.setComparator(sessionIdColumn, new Comparator<UserSession>() {
            
            @Override
            public int compare(UserSession o1, UserSession o2) {
                if (o1 == o2) {
                    return 0;
                }
                
                if (((DomainSession)o1).getDomainSessionId() > ((DomainSession)o2).getDomainSessionId()) {
                    return 1;
                }
                return -1;
            }
           
        });

        /**
         * comparator for userType column
         */
        columnSortHandler.setComparator(userTypeColumn, new Comparator<UserSession>() {

            @Override
            public int compare(UserSession o1, UserSession o2) {
                if (o1 == o2) {
                    return 0;
                }
                
                if (o1 != null) {
                    return (o2 != null) ? o1.getSessionType().compareTo(o2.getSessionType()) : 1;
                }
                return -1;
            }
           
        });
        
        /**
         * comparator for userId column
         */
        columnSortHandler.setComparator(userIdColumn, new Comparator<UserSession>() {

            @Override
            public int compare(UserSession o1, UserSession o2) {
                if (o1 == o2) {
                    return 0;
                }
                
                if (o1.getUserId() > o2.getUserId()) {
                    
                    return 1;
                }
                return -1;
            }
           
        });
        
        /**
         * comparator for username column
         */
        columnSortHandler.setComparator(usernameColumn, new Comparator<UserSession>() {

            @Override
            public int compare(UserSession o1, UserSession o2) {
                if (o1 == o2) {
                    return 0;
                }
                
                if (o1 != null) {
                    return (o2 != null) ? o1.getUsername().compareTo(o2.getUsername()) : 1;
                }
                return -1;
            }
           
        });
        
        /**
         * add the sort handler to the cell table
         */
        userSessionsTable.addColumnSortHandler(columnSortHandler);
        /**
         * initial sort by sessionID
         */
        userSessionsTable.getColumnSortList().push(sessionIdColumn);  
    }

    /**
     * Initializes the list of active user and domain sessions once the active sessions panel loads. This
     * ensures that if an active session is running and the page reloads that the session will correctly be 
     * populated by the data provider.
     * 
     * @return the list of active user sessions
     */
    private List<UserSession> initActiveSessions() {
        List<UserSession> sessions = new ArrayList<>();
        
        /* Grab the list of active user and domain sessions. */
        Map<Integer, UserSession> userSessions = UsersStateProvider.getInstance().getUserSessions();
        Map<Integer, DomainSession> domainSessions = UsersStateProvider.getInstance().getActiveDomainSessions();
        
        /* 
         * For each active user session, check if there is a corresponding domain session, and add to 
         * the active session list if available. 
         */
        for (Integer session : userSessions.keySet()) {
            DomainSession activeDS = domainSessions.get(session);
            
            if (activeDS != null) {
                sessions.add(activeDS);
            }
        }
        
        return sessions;
    }

    @Override
    public void onUsersChanged(Map<Integer, UserSession> userSessions, Map<Integer, DomainSession> domainSessions) {        
        /* Clear list to get ready to process updates. */
        activeSessionsDataProvider.getList().clear();
        
        /* 
         * For each active user session, check if there is a corresponding domain session, and add to 
         * the active session list if available. 
         */
        for (Integer session : userSessions.keySet()) {
            DomainSession activeDS = domainSessions.get(session);
            
            if (activeDS != null) {
                activeSessionsDataProvider.getList().add(activeDS);
            }
        }        

        /* Set the row data for the cell table with the collection of active user and domain sessions. */
        activeSessionsDataProvider.refresh();
    }
}
