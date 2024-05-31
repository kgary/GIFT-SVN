/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets.bootstrap;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.IconStack;
import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.constants.Emphasis;
import org.gwtbootstrap3.client.ui.constants.IconType;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.SimplePanel;

import generated.dkf.LearnerId;
import generated.dkf.Team;
import generated.dkf.TeamMember;

/**
 * A tree item that represents a team member in the team organization outline
 * and allows the author to rename it, remove it, and edit it
 *
 * @author nroberts
 */
public class TeamMemberTreeItem extends TeamObjectTreeItem<TeamMember> {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(TeamMemberTreeItem.class.getName());

    /** The deck used to change the type of learner ID editor that is shown */
    protected DeckPanel typeDeck = new DeckPanel();

    /** A panel used to hide the learner ID editor */
    protected SimplePanel emptyPanel = new SimplePanel();

    /** Icon used to indicate that this team member is not playable */
    private Icon notPlayableIcon = new Icon(IconType.BAN);

    /** Tooltip for the icon used to set whether team member is playable */
    protected Tooltip playableTooltip;

    /** The icons to display to indicate if the learner is playable or not */
    protected final IconStack playableIcons = new IconStack();

    /**
     * Creates a new tree item that represents and modifies the given team
     * member
     *
     * @param teamMember the team member to represent and modify
     */
    public TeamMemberTreeItem(final TeamMember teamMember) {
        this(teamMember, false);
    }

    /**
     * Creates a new tree item that represents and modifies the given team
     * member
     *
     * @param teamMember the team member to represent and modify
     * @param draggable whether or not the tree item should be draggable
     */
    public TeamMemberTreeItem(final TeamMember teamMember, boolean draggable) {
        super(teamMember, draggable);

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Constructing team member tree item for team member " + teamMember.toString());
        }

        /* set up an interactive icon that the author can used to set whether
         * this team member is playable */
        playableIcons.addStyleName(CSS.teamOrganizationPlayableIcon());

        playableIcons.add(new Icon(IconType.GAMEPAD), false);

        notPlayableIcon.setEmphasis(Emphasis.DANGER);
        notPlayableIcon.setVisible(false);
        playableIcons.add(notPlayableIcon, true);

        playableTooltip = new Tooltip(playableIcons);
        playableTooltip.setIsHtml(true);
        playableTooltip.setContainer("body");

        containerPanel.insert(playableIcons, containerPanel.getWidgetIndex(getNameLabel()));

        updatePlayableIcon();

        /* set up the panel used to edit the member's learner ID */
        typeDeck.setAnimationEnabled(true);

        typeDeck.addDomHandler(new MouseDownHandler() {
            @Override
            public void onMouseDown(MouseDownEvent event) {
                /* clicking in the editor should not propagate to the tree */
                event.stopPropagation();
            }
        }, MouseDownEvent.getType());

        typeDeck.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                /* clicking in the editor should not propagate to the tree */
                event.stopPropagation();
            }
        }, ClickEvent.getType());

        typeDeck.add(emptyPanel);

        /* populate the learner ID data into the appropriate editor */
        if (teamMember.getLearnerId() == null) {
            teamMember.setLearnerId(new LearnerId());
        }

        containerPanel.add(typeDeck);

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Finished constructing team tree item");
        }
    }

    /**
     * Visually updates the icon used to control this team member's playability
     * so that it matches this team member's current playable state. Also
     * updates the icon's tooltip accordingly.
     */
    protected void updatePlayableIcon() {

        boolean playable = getTeamObject().isPlayable();

        notPlayableIcon.setVisible(!playable);
        playableTooltip.setTitle(playable ? getPlayableTooltip() : getUnplayableTooltip());
    }

    /**
     * Get the tooltip text for the playable icon
     * 
     * @return the playable icon tooltip text
     */
    protected String getPlayableTooltip() {
        return "This entity is playable.";
    }

    /**
     * Get the tooltip text for the unplayable icon
     * 
     * @return the unplayable icon tooltip text
     */
    protected String getUnplayableTooltip() {
        return "This entity is unplayable.";
    }

    @Override
    public boolean allowDrop(TeamObjectTreeItem<?> otherItem) {
        return otherItem != null
                && (otherItem.getTeamObject() instanceof TeamMember || otherItem.getTeamObject() instanceof Team)
                && !otherItem.getTeamObject().equals(getTeamObject());
    }

    @Override
    public void cleanUpTooltips() {
        super.cleanUpTooltips();

        playableTooltip.hide();
    }

    @Override
    protected boolean isReadOnly() {
        /* Always false */
        return false;
    }

    @Override
    protected boolean isObjectNameValid(Serializable object, String name) {
        /* Always true */
        return true;
    }

    @Override
    protected void updateTeamReferences(String oldName, String newName) {
        // do nothing
    }

    @Override
    public String getName() {
        return object.getName();
    }
}