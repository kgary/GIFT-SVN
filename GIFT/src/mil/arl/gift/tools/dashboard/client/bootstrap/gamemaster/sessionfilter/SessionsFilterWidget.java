/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.sessionfilter;

import static mil.arl.gift.common.gwt.client.SafeHtmlUtils.bold;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.LinkedGroupItem;
import org.gwtbootstrap3.client.ui.constants.IconType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.util.StringUtils;

/**
 * A widget to display a session and it's metadata for the filter panel.
 *
 * @author sharrison
 */
public class SessionsFilterWidget extends LinkedGroupItem implements Comparable<SessionsFilterWidget> {
    
    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(SessionsFilterWidget.class.getName());

    /** The UiBinder that combines the ui.xml with this java class */
    private static SessionsFilterWidgetUiBinder uiBinder = GWT.create(SessionsFilterWidgetUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface SessionsFilterWidgetUiBinder extends UiBinder<Widget, SessionsFilterWidget> {
    }

    /** The main flow panel that contains the contents of the widget */
    @UiField
    protected FlowPanel mainPanel;

    /** The icon used to collapse/expand the widget */
    @UiField
    protected Icon headerCollapseIcon;

    /** The label for the header */
    @UiField
    protected Label headerLabel;

    /** The deck panel showing the content */
    @UiField
    protected DeckPanel contentDeck;

    /** The panel to show when there is no content */
    @UiField
    protected SimplePanel noContentPanel;

    /** The panel containing the metadata for the session */
    @UiField
    protected FlowPanel dataPanel;

    /**
     * The filter value used to group knowledge sessions. Only sessions with
     * this same value can be grouped together.
     */
    private String filterValue;

    /** The filter property enum for this widget */
    private SessionFilterPropertyEnum filterProperty;

    /**
     * The collection of knowledge sessions that are represented by this
     * widget
     */
    private Set<AbstractKnowledgeSession> knowledgeSessions = new LinkedHashSet<>();

    /**
     * Constructor.
     *
     * @param knowledgeSession the knowledge session with which to populate this
     *        widget. Can't be null.
     * @param value the value to use as this widget's filter value. Only
     *        sessions with this same value can be grouped together. Can't be
     *        blank.
     * @param filterProperty the filter property enum for this widget. Can't be
     *        null.
     */
    public SessionsFilterWidget(AbstractKnowledgeSession knowledgeSession, String value,
            SessionFilterPropertyEnum filterProperty) {
        if (knowledgeSession == null) {
            throw new IllegalArgumentException("The parameter 'knowledgeSession' cannot be null.");
        } else if (StringUtils.isBlank(value)) {
            throw new IllegalArgumentException("The parameter 'value' cannot be blank.");
        } else if (filterProperty == null) {
            throw new IllegalArgumentException("The parameter 'filterProperty' cannot be null.");
        }

        /* Since this class is extending from LinkedGroupItem, it could not use
         * the typical composite initWidget method. The createAndBindUi method
         * will create the component (flow panel in this case) from the ui.xml
         * and 'add' inserts it into this LinkedGroupItem instance. */
        add(uiBinder.createAndBindUi(this));

        this.filterValue = value;
        this.filterProperty = filterProperty;

        headerLabel.setText(filterValue);

        addKnowledgeSession(knowledgeSession, value);

        /* Perform collapse/expand when icon is clicked */
        headerCollapseIcon.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                event.stopPropagation();
                final boolean showDataPanel = contentDeck.getVisibleWidget() != contentDeck.getWidgetIndex(dataPanel);
                final int widgetIndexToShow = contentDeck.getWidgetIndex(showDataPanel ? dataPanel : noContentPanel);
                contentDeck.showWidget(widgetIndexToShow);
                headerCollapseIcon
                        .setType(showDataPanel ? IconType.CHEVRON_CIRCLE_DOWN : IconType.CHEVRON_CIRCLE_RIGHT);
            }
        });
    }

    /**
     * Populate the metadata panel for the provided session.
     * 
     * @param knowledgeSession the session containing the metadata.
     */
    private void populateMetadataPanel(AbstractKnowledgeSession knowledgeSession) {
        switch (filterProperty) {
        case COURSE_NAME:
            /* Intentional fall-through */
        case EXPERIMENT_NAME:
            /* The widget can represent more than 1 session, so right now just
             * add 1 field in the panel for this session */
            addMetadataField("Session ID",
                    Integer.toString(knowledgeSession.getHostSessionMember().getDomainSessionId()));
            break;
        case SESSION_ID:
            /* The widget currently can only match 1 session to this widget's
             * filter value (session id), so we can add multiple metadata lines
             * into the panel */
            addMetadataField("Host", knowledgeSession.getHostSessionMember().getSessionMembership().getUsername());
            addMetadataField("Session", knowledgeSession.getNameOfSession());
            if (StringUtils.isNotBlank(knowledgeSession.getExperimentName())) {
                addMetadataField("Experiment", knowledgeSession.getExperimentName());
            }
            addMetadataField("Course", knowledgeSession.getCourseName());
            break;
        default:
            throw new UnsupportedOperationException(
                    "Found unsupported filter property '" + filterProperty.getName() + "'.");
        }
    }

    /**
     * Adds a metadata field into the {@link #dataPanel}.
     * 
     * @param name the name of the metadata. Can't be blank.
     * @param value the value of the metadata
     */
    private void addMetadataField(String name, String value) {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("The parameter 'name' cannot be blank.");
        }

        if (StringUtils.isBlank(value)) {
            value = "<none>";
        }

        SafeHtmlBuilder sb = new SafeHtmlBuilder();
        sb.append(bold(name)).append(bold(": ")).appendEscaped(value);

        FlowPanel panel = new FlowPanel();
        panel.add(new InlineHTML(sb.toSafeHtml()));

        dataPanel.add(panel);
    }

    /**
     * Attempts to add the knowledge session to the widget. Will only add if the
     * value matches the widget's filter value.
     * 
     * @param knowledgeSession the session to add if the value matches.
     * @param value the value to compare against the widget's filter value.
     * @return true if it was added; false otherwise.
     */
    public boolean addKnowledgeSession(AbstractKnowledgeSession knowledgeSession, String value) {
        if (knowledgeSession == null) {
            throw new IllegalArgumentException("The parameter 'knowledgeSession' cannot be null.");
        }

        if (!StringUtils.equalsIgnoreCase(value, filterValue)) {
            return false;
        }
        
        logger.fine("adding "+knowledgeSession.getDomainSessionLogFileName()+" log to filter with "+filterValue+" from session value of "+value+", course = "+knowledgeSession.getCourseName());

        knowledgeSessions.add(knowledgeSession);

        populateMetadataPanel(knowledgeSession);

        return true;
    }

    /**
     * Attempts to remove the knowledge session.
     * 
     * @param knowledgeSession the domain session to remove.
     * @return true if the domain session id existed in this widget and it was
     *         removed; false otherwise.
     */
    public boolean removeKnowledgeSession(AbstractKnowledgeSession knowledgeSession) {
        boolean removed = knowledgeSessions.remove(knowledgeSession);
        if (!removed) {
            return false;
        }

        /* Rebuild metadata panel */
        dataPanel.clear();
        for (AbstractKnowledgeSession session : knowledgeSessions) {
            populateMetadataPanel(session);
        }

        return true;
    }

    /**
     * Return the number of knowledge sessions being represented by this widget.
     * 
     * @return the number of represented sessions.
     */
    public int getSessionCount() {
        return knowledgeSessions.size();
    }

    /**
     * Return the knowledge sessions that belong to this widget.
     * 
     * @return the unmodifiable collection of knowledge sessions. Can't be null.
     */
    public Collection<AbstractKnowledgeSession> getSessions() {
        return Collections.unmodifiableCollection(knowledgeSessions);
    }

    @Override
    public void add(IsWidget child) {
        /* Only allow 1 child */
        clear();
        super.add(child);
    }

    @Override
    public void add(Widget child) {
        /* Only allow 1 child */
        clear();
        super.add(child);
    }

    @Override
    public int compareTo(SessionsFilterWidget other) {
        if (other == null) {
            return -1;
        }

        return filterValue.compareToIgnoreCase(other.filterValue);
    }
}