/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets.bootstrap;

import static mil.arl.gift.common.gwt.client.SafeHtmlUtils.bold;

import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.LinkedGroup;
import org.gwtbootstrap3.client.ui.LinkedGroupItem;
import org.gwtbootstrap3.client.ui.LinkedGroupItemText;
import org.gwtbootstrap3.client.ui.constants.HeadingSize;
import org.gwtbootstrap3.client.ui.constants.IconType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.aar.LogMetadata;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.course.dkf.session.SessionMember;
import mil.arl.gift.common.course.dkf.session.TeamKnowledgeSession;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.util.BiDirectionalHashMap;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.common.util.StringUtils;

/**
 * Widget containing the knowledge session list.
 *
 * @author sharrison
 */
public class KnowledgeSessionListWidget extends AbstractBsWidget {
    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(KnowledgeSessionListWidget.class.getName());

    /** The UiBinder that combines the ui.xml with this java class */
    private static KnowledgeSessionListWidgetUiBinder uiBinder = GWT.create(KnowledgeSessionListWidgetUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface KnowledgeSessionListWidgetUiBinder extends UiBinder<Widget, KnowledgeSessionListWidget> {
    }

    /** The deck panel containing the different widgets that can be displayed */
    @UiField
    protected DeckPanel deckPanel;

    /** The list of knowledge sessions */
    @UiField
    protected LinkedGroup sessionList;

    /** A label indicating that no sessions could be found */
    @UiField
    protected Heading noSessionsLabel;

    /**
     * Date format used to show the full date (i.e. [HOUR]:[MINUTE] [MONTH] [DAY], [YEAR])
     */
    private static DateTimeFormat FULL_DATE_FORMAT = DateTimeFormat.getFormat("HH:mm MMMM d, yyyy");

    /** The session details: Host username */
    private static final SafeHtml HOST_USERNAME = bold("Host: ");

    /** The session details: time info for session (start and end - if provided) */
    private static final SafeHtml TIME = bold("Time: ");

    /** The session details: experiment name (if provided) */
    private static final SafeHtml EXPERIMENT_NAME = bold("Experiment: ");

    /** The session details: numeric id of the participant (if provided) */
    private static final SafeHtml PARTICIPANT_ID = bold("Participant ID: ");

    /** The session details: duration of the session (if end time is provided) */
    private static final SafeHtml DURATION = bold("Duration: ");

    /** The session details: the name of the domain session log file (if provided) */
    private static final SafeHtml LOG_FILENAME = bold("Log: ");

    /** The session details: Course ID parameter */
    private static final SafeHtml SESSION_COURSE_ID = bold("Course ID: ");

    /** The session details: Team participants parameter */
    private static final SafeHtml SESSION_PARTICIPANTS = bold("Team Roster: ");

    /** The session details: Host session ID parameter */
    private static final SafeHtml SESSION_HOST_SESSION_ID = bold("Host session ID: ");

    /** tooltip text instructing the user to select a session from the list */
    private static final String SELECT_SESSION_TOOLTIP = "Select to start monitoring this session";

    /** Maps the created list item UI widget with the backing session data */
    private BiDirectionalHashMap<AbstractKnowledgeSession, LinkedGroupItem> sessionListItemMap = new BiDirectionalHashMap<>();

    /** The tooltip to show on the list item */
    private String listItemTooltip = null;

    /**
     * The callback to be executed whenever a user selects an
     * {@link AbstractKnowledgeSession}
     */
    private SessionSelectedCallback selectedSessionCallback;

    /**
     * Constructor required for GWT serialization
     */
    private KnowledgeSessionListWidget() {
    }

    /**
     * Constructor. Uses a default list item tooltip.
     *
     * @param selectedSessionCallback the callback to be executed whenever a
     *        user selects an {@link AbstractKnowledgeSession}
     */
    public KnowledgeSessionListWidget(SessionSelectedCallback selectedSessionCallback) {
        this(SELECT_SESSION_TOOLTIP, selectedSessionCallback);
    }

    /**
     * Constructor
     *
     * @param listItemTooltip the tooltip for the list items. If null, no
     *        tooltip will be shown.
     * @param selectedSessionCallback the callback to be executed whenever a
     *        user selects an {@link AbstractKnowledgeSession}
     */
    public KnowledgeSessionListWidget(String listItemTooltip, SessionSelectedCallback selectedSessionCallback) {
        this();

        if (selectedSessionCallback == null) {
            throw new IllegalArgumentException("The parameter 'selectedSessionCallback' cannot be null.");
        }

        initWidget(uiBinder.createAndBindUi(this));

        this.listItemTooltip = listItemTooltip;
        this.selectedSessionCallback = selectedSessionCallback;

        deckPanel.showWidget(deckPanel.getWidgetIndex(noSessionsLabel));
    }

    /**
     * Populate this widget with the provided knowledge sessions.
     *
     * @param knowledgeSessions the sessions to populate this widget
     */
    public void populateWidget(Collection<AbstractKnowledgeSession> knowledgeSessions) {
        sessionList.clear();
        sessionListItemMap.clear();

        if (CollectionUtils.isEmpty(knowledgeSessions)) {
            deckPanel.showWidget(deckPanel.getWidgetIndex(noSessionsLabel));
            return;
        }

        for (AbstractKnowledgeSession session : knowledgeSessions) {
            createListItem(session);
        }
    }

    /**
     * Create the knowledge session list item from a log metadata.
     *
     * @param logMetadata contains information about a domain session log file for past session playback
     * @param favIcon the icon to use for enabling/disabling favorite setting on this log file
     */
    private void createListItem(LogMetadata logMetadata, Icon favIcon){

        AbstractKnowledgeSession session = logMetadata.getSession();
        boolean isTeam = session instanceof TeamKnowledgeSession;
        Heading header = new Heading(HeadingSize.H4, session.getNameOfSession());
        Icon icon = new Icon(isTeam ? IconType.USERS : IconType.USER);
        icon.setPaddingRight(10);
        int iconIndex = 0;
        header.insert(icon, iconIndex++);
        
        if(!logMetadata.getVideoFiles().isEmpty()){
            // show an icon that indicates this session has one or more video files linked to it
            Icon videoIcon = new Icon(IconType.VIDEO_CAMERA);
            ManagedTooltip.attachTooltip(videoIcon, "Contains video file(s)");
            videoIcon.setPaddingRight(10);
            header.insert(videoIcon, iconIndex++);
        }
        
        if(logMetadata.getSession().getObserverControls() != null &&
                StringUtils.isNotBlank(logMetadata.getSession().getObserverControls().getCapturedAudioPath())){
            // show an icon that indicate this session has an audio file linked to it
            Icon audioIcon = new Icon(IconType.FILE_AUDIO_O);
            ManagedTooltip.attachTooltip(audioIcon, "Contains audio file");
            audioIcon.setPaddingRight(10);
            header.insert(audioIcon, iconIndex++);
        }

        if (logMetadata.hasLogPatchFile()) {
            Icon patchIcon = new Icon(IconType.PAPERCLIP);
            ManagedTooltip.attachTooltip(patchIcon, "Patch file applied");
            patchIcon.setPaddingRight(10);
            header.insert(patchIcon, iconIndex++);
        }

        if(favIcon != null){
            header.add(favIcon);
        }

        createListItem(session, header, null);
    }

    /**
     * Create the knowledge session list item from an active knowledge session.
     *
     * @param session the knowledge session to use to populate the list item.
     */
    private void createListItem(final AbstractKnowledgeSession session){

        boolean isTeam = session instanceof TeamKnowledgeSession;
        Heading header = new Heading(HeadingSize.H3, session.getNameOfSession());
        Icon icon = new Icon(isTeam ? IconType.USERS : IconType.USER);
        icon.setPaddingRight(10);
        header.insert(icon, 0);

        createListItem(session, header, AbstractKnowledgeSession.defaultSessionComparator);
    }

    /**
     * Create a new UI item with the data from the provided session.
     *
     * @param session the knowledge session to use to populate the list item.
     * @param header the header to use for the knowledge session list item.  This allows customization of the header
     * based on the type of knowledge session (e.g. active vs past session)
     * @param sessionComparator used to sort the sessions on the client.  Can be null if the server side sorting, if any,
     * suffices.
     */
    private void createListItem(final AbstractKnowledgeSession session, final Heading header, Comparator<AbstractKnowledgeSession> sessionComparator) {

        final LinkedGroupItemText text = new LinkedGroupItemText();
        final SafeHtmlBuilder sb = new SafeHtmlBuilder();

        final SessionMember hostSessionMember = session.getHostSessionMember();

        if(session.getSessionStartTime() > 0){
            Date start = new Date(session.getSessionStartTime());
            sb.append(TIME).appendHtmlConstant(FULL_DATE_FORMAT.format(start));
            if(session.getSessionEndTime() > session.getSessionStartTime()){

                // concatenate end time
                sb.appendHtmlConstant(Constants.SPACE).appendHtmlConstant("-").appendHtmlConstant(Constants.SPACE)
                    .appendHtmlConstant(FULL_DATE_FORMAT.format(new Date(session.getSessionEndTime())));

                // concatenate duration
                String duration = FormattedTimeBox.getDisplayText((int)(session.getSessionEndTime() - session.getSessionStartTime())/1000, true);
                sb.appendHtmlConstant(Constants.SPACE).appendHtmlConstant(Constants.SPACE).append(DURATION).appendHtmlConstant(duration);
            }
            sb.appendHtmlConstant("<br/>");
        }

        if(session.getExperimentName() != null){
            final String participantId = String.valueOf(hostSessionMember.getUserSession().getUserId());
            sb.append(EXPERIMENT_NAME).appendHtmlConstant(session.getExperimentName()).appendHtmlConstant("<br/>");
            sb.append(PARTICIPANT_ID).appendHtmlConstant(participantId).appendHtmlConstant("<br/>");
        } else {
            sb.append(HOST_USERNAME).appendHtmlConstant(hostSessionMember.getSessionMembership().getUsername()).appendHtmlConstant("<br/>");
        }

        sb.append(SESSION_COURSE_ID).appendHtmlConstant(session.getCourseRuntimeId());
        sb.appendHtmlConstant("<br/>");
        if (session instanceof TeamKnowledgeSession) {
            TeamKnowledgeSession teamSession = (TeamKnowledgeSession) session;
            //Note: add 1 to include the host in the count since the host is not currently in the list of joined members
            sb.append(SESSION_PARTICIPANTS)
            .appendHtmlConstant(Integer.toString(teamSession.getJoinedMembers().size()+1))
            .appendHtmlConstant(" out of ")
            .appendHtmlConstant(Integer.toString(teamSession.getTotalPossibleTeamMembers()));
            sb.appendHtmlConstant("<br/>");
        }
        sb.append(SESSION_HOST_SESSION_ID)
                .appendHtmlConstant(Integer.toString(hostSessionMember.getDomainSessionId()));

        if(session.getDomainSessionLogFileName() != null){
            sb.appendHtmlConstant("<br/>");
            sb.append(LOG_FILENAME).appendHtmlConstant(session.getDomainSessionLogFileName());
        }

        text.setHTML(sb.toSafeHtml().asString());
        text.getElement().getStyle().setFontSize(12, Unit.PX);

        final LinkedGroupItem groupItem = new LinkedGroupItem();
        groupItem.add(header);
        groupItem.add(text);

        groupItem.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {

                setSelectedItem(groupItem);
                selectedSessionCallback.sessionSelected(session);
            }
        }, ClickEvent.getType());

        if (StringUtils.isNotBlank(listItemTooltip)) {
            /* Just need to call the constructor to have the tooltip show on the
             * panel that contains the widgets. Don't need to actually use the
             * ManagedTooltip. */
            ManagedTooltip.attachTooltip(groupItem, listItemTooltip);
        }

        sessionListItemMap.put(session, groupItem);
        showSession(session, sessionComparator);
        deckPanel.showWidget(deckPanel.getWidgetIndex(sessionList));
    }

    /**
     * Sets the selected item in the session list to active. This will clear out
     * any previous selection.
     *
     * @param selectedItem The item to be selected. If null, the selections will
     *        be cleared.
     */
    private void setSelectedItem(LinkedGroupItem selectedItem) {
        clearSelection();
        if (selectedItem != null) {
            selectedItem.setActive(true);
        }
    }

    /**
     * Sets the selected item in the session list to active. This will clear out
     * any previous selection.
     *
     * @param selectedItem The item to be selected. If the item is not in the
     *        list, the selections will be cleared.
     */
    public void setSelectedItem(AbstractKnowledgeSession selectedItem) {
        LinkedGroupItem groupItem = sessionListItemMap.getFromKey(selectedItem);
        setSelectedItem(groupItem);
    }

    /**
     * Sets the selected item in the list by session name.
     *
     * @param sessionName - Name of the session (cannot be null).
     */
    public void setSelectedSessionBySessionName(String sessionName) {
        if (sessionName == null || sessionName.isEmpty()) {
            return;
        }

        for (int i = 0; i < sessionList.getWidgetCount(); i++) {
            Widget w = sessionList.getWidget(i);
            if (w instanceof LinkedGroupItem) {
                LinkedGroupItem groupItem = (LinkedGroupItem) w;

                // Iterate through the linked group item widgets to find the header that contains the name.
                for (int j=0; j < groupItem.getWidgetCount(); j++) {
                    Widget h = groupItem.getWidget(j);
                    if (h instanceof Heading) {
                        Heading heading = (Heading)h;
                        if (sessionName.compareTo(heading.getText()) == 0) {
                            groupItem.setActive(true);
                            return;
                        }
                    }
                }

            }
        }
    }

    /**
     * Clears any selected items.
     */
    public void clearSelection() {
        for (int i = 0; i < sessionList.getWidgetCount(); i++) {
            Widget w = sessionList.getWidget(i);
            if (w instanceof LinkedGroupItem) {
                LinkedGroupItem groupItem = (LinkedGroupItem) w;
                groupItem.setActive(false);
            }
        }
    }

    /**
     * Retrieve the selected item.
     *
     * @return the selected item. Can be null if nothing is selected.
     */
    public AbstractKnowledgeSession getSelection() {
        for (int i = 0; i < sessionList.getWidgetCount(); i++) {
            Widget w = sessionList.getWidget(i);
            if (w instanceof LinkedGroupItem) {
                LinkedGroupItem groupItem = (LinkedGroupItem) w;
                if (groupItem.isActive()) {
                    return sessionListItemMap.getFromValue(groupItem);
                }
            }
        }

        return null;
    }

    /**
     * Adds the provided knowledge session to the list if it doesn't already
     * exist.
     *
     * @param knowledgeSession the knowledge session to add.
     */
    public void addSession(AbstractKnowledgeSession knowledgeSession) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("addSession(" + knowledgeSession + ")");
        }

        /* This session is null or already known */
        if (knowledgeSession == null || sessionListItemMap.containsKey(knowledgeSession)) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("addSession() - did not create the list item.");
            }

            return;
        }

        createListItem(knowledgeSession);
    }

    /**
     * Adds the provided knowledge session to the list if it doesn't already
     * exist.
     *
     * @param logMetadata contains information about a domain session log file for past session playback
     * @param favIcon the icon to use for enabling/disabling favorite setting on this log file
     */
    public void addSession(LogMetadata logMetadata, Icon favIcon) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("addSession(" + logMetadata + ")");
        }

        AbstractKnowledgeSession knowledgeSession = logMetadata.getSession();

        /* This session is null or already known */
        if (knowledgeSession == null || sessionListItemMap.containsKey(knowledgeSession)) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("addSession() - did not create the list item.");
            }

            return;
        }

        createListItem(logMetadata, favIcon);
    }

    /**
     * Remove the provided knowledge session from the list.
     *
     * @param knowledgeSession the knowledge session to remove.
     */
    public void removeSession(AbstractKnowledgeSession knowledgeSession) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("removeSession(" + knowledgeSession + ")");
        }

        if (knowledgeSession == null) {
            return;
        }

        /* Remove the list item */
        hideSession(knowledgeSession);
        sessionListItemMap.removeKey(knowledgeSession);

        final Widget widgetToShow = sessionList.getWidgetCount() == 0 ? noSessionsLabel : sessionList;
        deckPanel.showWidget(deckPanel.getWidgetIndex(widgetToShow));
    }

    /**
     * Shows the session in the list.
     *
     * @param knowledgeSession the session to show.
     * @param sessionComparator used to sort the sessions on the client.  Can be null if the server side sorting, if any,
     * suffices.
     */
    public void showSession(AbstractKnowledgeSession knowledgeSession, Comparator<AbstractKnowledgeSession> sessionComparator) {
        LinkedGroupItem groupItem = sessionListItemMap.getFromKey(knowledgeSession);
        if (groupItem == null) {
            /* Widget does not exist for this session */
            return;
        }

        /* This is a sorted list, so determine where to insert */
        boolean inserted = false;
        if(sessionComparator != null){
            for (int i = 0; i < sessionList.getWidgetCount(); i++) {
                final LinkedGroupItem listItem = (LinkedGroupItem) sessionList.getWidget(i);
                final AbstractKnowledgeSession listSession = sessionListItemMap.getFromValue(listItem);
                if (sessionComparator.compare(knowledgeSession, listSession) < 0) {
                    sessionList.insert(groupItem, i);
                    inserted = true;
                    break;
                }
            }
        }

        /* Wasn't inserted, so add it to the end of the list. */
        if (!inserted) {
            sessionList.add(groupItem);
        }
    }

    /**
     * Hides the session in the list.
     *
     * @param knowledgeSession the session to hide.
     */
    public void hideSession(AbstractKnowledgeSession knowledgeSession) {
        LinkedGroupItem groupItem = sessionListItemMap.getFromKey(knowledgeSession);
        if (groupItem == null) {
            /* Widget does not exist for this session */
            return;
        }

        sessionList.remove(groupItem);
    }

    /**
     * Clear/hide all sessions known to this widget.
     */
    public void clearSessions(){

        for(AbstractKnowledgeSession knowledgeSession : sessionListItemMap.keySet()){
            hideSession(knowledgeSession);
        }

        sessionListItemMap.clear();
    }

    /**
     * Set the text to be displayed if the list is empty.
     *
     * @param text the text to display to the user in the case of an empty list.
     */
    public void setEmptyListText(String text) {
        noSessionsLabel.setText(text);
    }

    /**
     * Scrolls to the top of the list of sessions.
     */
    public void scrollToTop() {
        deckPanel.getElement().scrollIntoView();
    }

    /**
     * Callback for when an {@link AbstractKnowledgeSession} is selected.
     *
     * @author sharrison
     */
    public static abstract class SessionSelectedCallback {
        /**
         * Sends notification that a session was selected.
         *
         * @param knowledgeSession the selected session.
         */
        protected abstract void sessionSelected(AbstractKnowledgeSession knowledgeSession);
    }
}
