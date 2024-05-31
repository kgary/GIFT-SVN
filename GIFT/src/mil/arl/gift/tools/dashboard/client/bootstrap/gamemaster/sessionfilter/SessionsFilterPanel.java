/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.sessionfilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.LinkedGroup;
import org.gwtbootstrap3.client.ui.constants.IconType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.aar.LogMetadata;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.gwt.client.RpcResponse;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.KnowledgeSessionListWidget;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ProgressModalDialogBox;
import mil.arl.gift.common.io.LoadedProgressIndicator;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.dashboard.client.UiManager;
import mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.BsGameMasterPanel;
import mil.arl.gift.tools.dashboard.client.gamemaster.ActiveSessionProvider;
import mil.arl.gift.tools.dashboard.client.gamemaster.ActiveSessionProvider.ActiveSessionChangeHandler;
import mil.arl.gift.tools.dashboard.client.gamemaster.ActiveSessionProvider.RunState;

/**
 * A panel used to allow the user to filter which domain knowledge session data
 * should or should not be displayed in the Game Master interface
 *
 * @author nroberts
 */
public class SessionsFilterPanel extends Composite implements ActiveSessionChangeHandler {
    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(SessionsFilterPanel.class.getName());

    /** The UiBinder that combines the ui.xml with this java class */
    private static SessionsFilterPanelUiBinder uiBinder = GWT.create(SessionsFilterPanelUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface SessionsFilterPanelUiBinder extends UiBinder<Widget, SessionsFilterPanel> {
    }

    /** The active session provider */
    private final ActiveSessionProvider activeSessionProvider = ActiveSessionProvider.getInstance();

    /** A selection box used to select the type of filter to apply */
    @UiField(provided = true)
    protected ValueListBox<SessionFilterPropertyEnum> filterBox = new ValueListBox<>(
            new Renderer<SessionFilterPropertyEnum>() {
                @Override
                public String render(SessionFilterPropertyEnum object) {
                    return object.getName();
                }

                @Override
                public void render(SessionFilterPropertyEnum object, Appendable appendable) throws IOException {
                    appendable.append(render(object));
                }
            });

    /** Container panel for the session filter */
    @UiField
    protected FlowPanel sessionFilterContainer;

    /**
     * Deck panel to toggle between the {@link #filteredItemGroup} and
     * {@link #noSessionsLabel}
     */
    @UiField
    protected DeckPanel sessionDeckPanel;

    /** The label showing that no sessions are available */
    @UiField
    protected Heading noSessionsLabel;

    /** The list of sessions that the user can choose to filter upon */
    @UiField
    protected LinkedGroup filteredItemGroup;

    /** The widgets that are selected in the filter */
    private Set<SessionsFilterWidget> selectedFilterWidgets = new HashSet<>();

    /** The parent panel */
    private final BsGameMasterPanel parentPanel;
    
    /** used to show progress on updating the log index file on the server */
    private ProgressModalDialogBox logIndexUpdateProgressDialog = null;

    /**
     * Creates a new panel that filters domain knowledge session data for the
     * given display element
     *
     * @param parentPanel the parent panel
     */
    public SessionsFilterPanel(BsGameMasterPanel parentPanel) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(".ctor()");
        }

        if (parentPanel == null) {
            throw new IllegalArgumentException("The parameter 'parentPanel' cannot be null.");
        }

        initWidget(uiBinder.createAndBindUi(this));

        this.parentPanel = parentPanel;

        sessionDeckPanel.showWidget(sessionDeckPanel.getWidgetIndex(noSessionsLabel));

        List<SessionFilterPropertyEnum> acceptableFilters = new ArrayList<>();
        filterBox.setValue(SessionFilterPropertyEnum.COURSE_NAME);
        for (SessionFilterPropertyEnum propertyEnum : SessionFilterPropertyEnum.values()) {
            acceptableFilters.add(propertyEnum);
        }

        filterBox.setAcceptableValues(acceptableFilters);

        filterBox.addValueChangeHandler(new ValueChangeHandler<SessionFilterPropertyEnum>() {
            @Override
            public void onValueChange(ValueChangeEvent<SessionFilterPropertyEnum> event) {
                rebuildSessionFilter(false);
            }
        });

        /* Subscribe to the data providers */
        subscribe();
    }
    
    @Override
    protected void onAttach() {
        super.onAttach();
        
        /* Check if any active knowledge sessions are already running and need to be added */
        Set<AbstractKnowledgeSession> existingSessions = ActiveSessionProvider.getInstance().getActiveKnowledgeSessions();
        if(existingSessions != null) {
            for(AbstractKnowledgeSession existingSession : existingSessions) {
                sessionAdded(existingSession);
            }
        }
    }

    /**
     * Subscribe to the providers that this widget cares about.
     */
    private void subscribe() {
        /* Subscribe to the list of active knowledge sessions. */
        activeSessionProvider.addManagedHandler(this);
    }

    /**
     * Create a new session filter item or group it with an existing widget if
     * possible. Does not handle session registration.
     *
     * @param session the session to create the filter item for.
     * @return the filter widget that the session belongs to; can be null if the
     *         session does not meet the filter criteria.
     */
    private SessionsFilterWidget createNewFilterItem(final AbstractKnowledgeSession session) {
        /* If it is not a playback when in playback mode, then do not add a
         * filter item to the list */
        final boolean isPlaybackMode = parentPanel.isSessionListInPlaybackMode();
        final boolean isPlaybackSession = session.inPastSessionMode();
        if (isPlaybackMode && !isPlaybackSession) {
            return null;
        }

        final SessionFilterPropertyEnum selectedFilterProperty = filterBox.getValue();
        String value = SessionFilterPropertyEnum.getFilterPropertyValue(selectedFilterProperty, session);

        /* Check if the session matches the filter property */
        if (StringUtils.isBlank(value)) {
            /* Doesn't match filter */
            return null;
        }

        for (SessionsFilterWidget widget : getWidgetsInFilter()) {
            /* See if any existing widgets have filter criteria that the session
             * complies with. If yes, it can be grouped with that widget instead
             * of creating a new one. */
            if (widget.addKnowledgeSession(session, value)) {
                return widget;
            }
        }

        /* Create new widget */
        final SessionsFilterWidget newFilterItem = new SessionsFilterWidget(session, value, selectedFilterProperty);

        /* Toggle active/inactive when clicked */
        newFilterItem.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                /* Toggle filter's 'active' value for session */
                boolean makeActive = !newFilterItem.isActive();
                if (makeActive) {
                    setFilterItemActive(newFilterItem);
                } else {
                    setFilterItemInactive(newFilterItem);
                }
            }
        });

        /* This is a sorted list, so determine where to insert */
        int insertIndex;
        for (insertIndex = 0; insertIndex < filteredItemGroup.getWidgetCount(); insertIndex++) {
            Widget w = filteredItemGroup.getWidget(insertIndex);
            if (w instanceof SessionsFilterWidget) {
                SessionsFilterWidget listWidget = (SessionsFilterWidget) w;
                if (newFilterItem.compareTo(listWidget) < 0) {
                    break;
                }
            }
        }

        /* Add the new filter item to the group */
        filteredItemGroup.insert(newFilterItem, insertIndex);

        showSessionWidget(filteredItemGroup);

        return newFilterItem;
    }

    /**
     * Show a widget in the {@link #sessionDeckPanel}.
     *
     * @param widget the widget to show. Must be {@link #noSessionsLabel} or
     *        {@link #filteredItemGroup}. Can't be null.
     */
    private void showSessionWidget(Widget widget) {
        if (widget == null) {
            throw new IllegalArgumentException("The parameter 'widget' cannot be null.");
        }

        final int widgetIndex = sessionDeckPanel.getWidgetIndex(widget);
        if (widgetIndex == -1) {
            throw new IllegalArgumentException("The paramter 'widget' is not a child of sessionDeckPanel.");
        } else if (widgetIndex == sessionDeckPanel.getVisibleWidget()) {
            /* The desired widget is already visible */
            return;
        }

        sessionDeckPanel.showWidget(widgetIndex);
    }

    /**
     * Set the the provided filter widget as active.
     *
     * @param filterWidget the filter widget to update. Nothing will happen if
     *        null.
     */
    private void setFilterItemActive(final SessionsFilterWidget filterWidget) {
        if (filterWidget == null || filterWidget.isActive()) {
            return;
        }

        /* Set/Remove the active style */
        filterWidget.setActive(true);

        /* Add to the selected list */
        selectedFilterWidgets.add(filterWidget);

        final boolean isPlaybackMode = parentPanel.isSessionListInPlaybackMode();

        /* If going from 0 -> 1 selected item, make sure to handle all other
         * items since the expected behavior is that no selection means
         * everything is shown. */
        if (selectedFilterWidgets.size() == 1) {

            final KnowledgeSessionListWidget listWidget = isPlaybackMode ? parentPanel.getAarSessionListWidget()
                    : parentPanel.getActiveSessionListWidget();

            for (SessionsFilterWidget item : getWidgetsInFilter()) {
                /* Remove the filter item that was just selected as we want to
                 * keep that registered */
                if (item == filterWidget) {
                    continue;
                }

                for (AbstractKnowledgeSession session : item.getSessions()) {
                    listWidget.removeSession(session);
                }
            }
        } else {
            final String username = UiManager.getInstance().getUserName();
            for (AbstractKnowledgeSession session : filterWidget.getSessions()) {
                if (isPlaybackMode) {
                    LogMetadata logMetadata = parentPanel.getPlaybackLogSessions().get(session);
                    Icon favIcon = createFavoriteIcon(logMetadata, username);
                    parentPanel.getAarSessionListWidget().addSession(logMetadata, favIcon);
                } else {
                    parentPanel.getActiveSessionListWidget().addSession(session);
                }
            }
        }
    }

    /**
     * Set the the provided filter widget as inactive.
     *
     * @param filterWidget the filter widget to update. Nothing will happen if
     *        null.
     */
    private void setFilterItemInactive(final SessionsFilterWidget filterWidget) {
        if (filterWidget == null || !filterWidget.isActive()) {
            return;
        }

        /* Set/Remove the active style */
        filterWidget.setActive(false);

        /* Remove from the selected list */
        selectedFilterWidgets.remove(filterWidget);

        final boolean isPlaybackMode = parentPanel.isSessionListInPlaybackMode();

        /* If going from 1 -> 0 selected items, make sure to handle all items
         * since the expected behavior is that no selection means everything is
         * shown and registered. */
        if (selectedFilterWidgets.isEmpty()) {

            final String username = UiManager.getInstance().getUserName();
            for (SessionsFilterWidget item : getWidgetsInFilter()) {
                for (AbstractKnowledgeSession session : item.getSessions()) {
                    if (isPlaybackMode) {
                        LogMetadata logMetadata = parentPanel.getPlaybackLogSessions().get(session);
                        Icon favIcon = createFavoriteIcon(logMetadata, username);
                        parentPanel.getAarSessionListWidget().addSession(logMetadata, favIcon);
                    } else {
                        parentPanel.getActiveSessionListWidget().addSession(session);
                    }
                }
            }
        } else {
            final KnowledgeSessionListWidget listWidget = isPlaybackMode ? parentPanel.getAarSessionListWidget()
                    : parentPanel.getActiveSessionListWidget();

            for (AbstractKnowledgeSession session : filterWidget.getSessions()) {
                listWidget.removeSession(session);
            }
        }
    }

    /**
     * Asks the server for the latest progress on updating the log index. The progress
     * is displayed to the user on the progress bar.  When the server indicates the request is
     * completed, the progress dialog is closed.  If the load operation is still
     * on going, this method will recursively call itself.
     */
    private void checkUpdateLogIndexLoadProgress() {

        if(logger.isLoggable(Level.FINE)){
            logger.fine("Checking update log index progress for past session logs");
        }        

        UiManager.getInstance().getDashboardService().getUpdateLogIndexProgress(UiManager.getInstance().getSessionId(),
                new AsyncCallback<LoadedProgressIndicator<Void>>() {

            @Override
            public void onFailure(Throwable t) {
                logger.severe("Error caught with getting update log index progress: " + t.getMessage());

                UiManager.getInstance().displayErrorDialog("Failed to update log index", "There was a server side error of\n"+t.getMessage(), null);

                logIndexUpdateProgressDialog.hide();
            }

            @Override
            public void onSuccess(LoadedProgressIndicator<Void> loadProgressResponse) {                
                
                if(logIndexUpdateProgressDialog == null){
                    logIndexUpdateProgressDialog = new ProgressModalDialogBox("Updating Property", loadProgressResponse, false);
                }
                
                logIndexUpdateProgressDialog.updateProgress(loadProgressResponse);

                if(loadProgressResponse.getException() != null){

                    UiManager.getInstance().displayDetailedErrorDialog("Failed to update log index",
                            loadProgressResponse.getException().getReason(), loadProgressResponse.getException().getDetails(),
                            loadProgressResponse.getException().getErrorStackTrace(), null);
                    
                    logIndexUpdateProgressDialog.hide();

                }else if(loadProgressResponse.isComplete()){
                    if(logger.isLoggable(Level.INFO)){
                        logger.info("Update log index request load progress has completed");
                    }

                } else {
                    if(logger.isLoggable(Level.INFO)){
                        logger.info("Update log index request load progress continues.."+loadProgressResponse);
                    }

                    //schedule another poll for progress 1 second from now
                    Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {

                        @Override
                        public boolean execute() {

                            checkUpdateLogIndexLoadProgress();

                            return false;
                        }

                    }, 1000);
                }
            }

        });

    }

    /**
     * Create an icon to use to enable/disable favorite setting for a single log metadata in the knowledge
     * sessions list.
     * 
     * @param logMetadata contains information about a domain session log file for past session playback
     * @param username the current game master user, used to check whether this log metadata is a favorite
     * of this user or not.
     * @return a new Icon representing the users setting of favorites
     */
    private Icon createFavoriteIcon(final LogMetadata logMetadata, final String username){
        
        final Icon favIcon = new Icon(logMetadata.getUsersFavorite().contains(username) ? IconType.STAR : IconType.STAR_O);
        if(favIcon.getType() == IconType.STAR){
            favIcon.setColor("yellow");
            favIcon.getElement().getStyle().setProperty("webkitTextStrokeColor", "black");
            favIcon.getElement().getStyle().setProperty("webkitTextStrokeWidth", "2px");
        }
        
        favIcon.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent clickEvent) {
                
                if(favIcon.getType() == IconType.STAR){
                    favIcon.setType(IconType.STAR_O);
                    favIcon.setColor("black");
                    favIcon.getElement().getStyle().clearProperty("webkitTextStrokeColor");
                    favIcon.getElement().getStyle().clearProperty("webkitTextStrokeWidth");
                    logMetadata.getUsersFavorite().remove(username);
                }else{
                    favIcon.setType(IconType.STAR);
                    favIcon.setColor("yellow");
                    favIcon.getElement().getStyle().setProperty("webkitTextStrokeColor", "black");
                    favIcon.getElement().getStyle().setProperty("webkitTextStrokeWidth", "2px");
                    logMetadata.getUsersFavorite().add(username);
                }
                
                // communicate to server that the log index file needs to be updated
                UiManager.getInstance().getDashboardService().updateLogIndex(UiManager.getInstance().getSessionId(), 
                        logMetadata, new AsyncCallback<RpcResponse>() {
                    
                    @Override
                    public void onSuccess(RpcResponse response) {
                        
                        if(response.isSuccess()){
                            checkUpdateLogIndexLoadProgress();
                        }else{

                            UiManager.getInstance().displayDetailedErrorDialog("Failed to update log index",
                                        response.getResponse(), response.getAdditionalInformation(),
                                        response.getErrorStackTrace(), null);
                        }                        
                    }
                    
                    @Override
                    public void onFailure(Throwable throwable) {

                        logger.severe("Error caught when requesting that the server update the past session log index for "+logMetadata+"\n" + throwable.getMessage());

                        UiManager.getInstance().displayErrorDialog("Failed to update the log index", "There was a server side error of\n"+throwable.getMessage(), null);
                    }
                });
                
                // to prevent this session from being started
                clickEvent.stopPropagation();
            }
        });
        favIcon.setPaddingLeft(10);
        
        return favIcon;
    }

    /**
     * Return the widgets that are currently being shown in the filter.
     *
     * @return the set of widgets being shown.
     */
    public Set<SessionsFilterWidget> getWidgetsInFilter() {
        Set<SessionsFilterWidget> inFilter = new HashSet<>();
        for (int i = 0; i < filteredItemGroup.getWidgetCount(); i++) {
            Widget w = filteredItemGroup.getWidget(i);
            if (w instanceof SessionsFilterWidget) {
                SessionsFilterWidget fWidget = (SessionsFilterWidget) w;
                inFilter.add(fWidget);
            }
        }

        return inFilter;
    }

    /**
     * Does a hard refresh on the filter. Completely clears the filter then
     * rebuilds it with the appropriate sessions. Will call register/deregister
     * for the sessions accordingly.
     * 
     * @param resetFilterProperty true to reset the filter property back to the
     *        default ({@link SessionFilterPropertyEnum#COURSE_NAME}).
     */
    public void rebuildSessionFilter(boolean resetFilterProperty) {
        /* Reset UI and backing data models */
        filteredItemGroup.clear();
        selectedFilterWidgets.clear();

        if (resetFilterProperty) {
            filterBox.setValue(SessionFilterPropertyEnum.COURSE_NAME);
        }

        refreshListItems();

        /* Show no session label if none were created */
        if (filteredItemGroup.getWidgetCount() == 0) {
            showSessionWidget(noSessionsLabel);
        }
    }

    /**
     * Refreshes the list items. Removes and rebuilds all items.
     */
    public void refreshListItems() {
        if (parentPanel.isSessionListInPlaybackMode()) {
            final KnowledgeSessionListWidget aarSessionListWidget = parentPanel.getAarSessionListWidget();
            aarSessionListWidget.clearSessions();
            for (AbstractKnowledgeSession session : parentPanel.getPlaybackLogSessions().keySet()) {
                SessionsFilterWidget widget = createNewFilterItem(session);
                if (widget != null) {
                    String username = UiManager.getInstance().getUserName();
                    LogMetadata logMetadata = parentPanel.getPlaybackLogSessions().get(session);
                    Icon favIcon = createFavoriteIcon(logMetadata, username);
                    aarSessionListWidget.addSession(logMetadata, favIcon);
                }
            }
        } else {
            final KnowledgeSessionListWidget activeSessionListWidget = parentPanel.getActiveSessionListWidget();
            activeSessionListWidget.clearSessions();
            for (AbstractKnowledgeSession session : activeSessionProvider.getActiveKnowledgeSessions()) {
                /* Skip any sessions that are active because of a playback
                 * currently running */
                if (session.inPastSessionMode()) {
                    continue;
                }

                SessionsFilterWidget widget = createNewFilterItem(session);
                if (widget != null) {
                    activeSessionListWidget.addSession(session);
                }
            }
        }
    }

    @Override
    public void sessionAdded(AbstractKnowledgeSession knowledgeSession) {
        SessionsFilterWidget widget = createNewFilterItem(knowledgeSession);
        if (widget == null) {
            return;
        }

        if (parentPanel.isSessionListInPlaybackMode()) {
            String username = UiManager.getInstance().getUserName();
            LogMetadata logMetadata = parentPanel.getPlaybackLogSessions().get(knowledgeSession);
            Icon favIcon = createFavoriteIcon(logMetadata, username);
            parentPanel.getAarSessionListWidget().addSession(logMetadata, favIcon);
        } else {
            parentPanel.getActiveSessionListWidget().addSession(knowledgeSession);
        }
    }

    @Override
    public void sessionEnded(AbstractKnowledgeSession knowledgeSession) {
        if (knowledgeSession == null) {
            return;
        }
        
        if (!parentPanel.isSessionListInPlaybackMode() && !knowledgeSession.inPastSessionMode()) {
            
            //if active sessions are being shown and this session has ended, it needs to be removed
            parentPanel.getActiveSessionListWidget().removeSession(knowledgeSession);
        }

        final int domainSessionId = knowledgeSession.getHostSessionMember().getDomainSessionId();
        if (RunState.PLAYBACK_ENDED.equals(activeSessionProvider.getRunState(domainSessionId))) {
            /* Playback has ended, but not been terminated, so we don't want to
             * remove the filter yet */
            return;
        }

        /* See if the domain session id is in any of the widgets */
        SessionsFilterWidget parentWidget = null;
        for (SessionsFilterWidget widget : getWidgetsInFilter()) {
            /* Removes the session from the widget if it exists */
            final boolean removedFromWidget = widget.removeKnowledgeSession(knowledgeSession);
            if (removedFromWidget) {
                parentWidget = widget;
                break;
            }
        }

        /* Session did not belong to any of the filter widgets */
        if (parentWidget == null) {
            return;
        }

        /* If the widget has no more sessions, remove it from the UI */
        if (parentWidget.getSessionCount() == 0) {
            /* Mark filter item as inactive before removing it so it can be
             * properly handled */
            setFilterItemInactive(parentWidget);
            filteredItemGroup.remove(parentWidget);

            /* Show no session label if removing the last item */
            if (filteredItemGroup.getWidgetCount() == 0) {
                showSessionWidget(noSessionsLabel);
            }
        }
    }
}
