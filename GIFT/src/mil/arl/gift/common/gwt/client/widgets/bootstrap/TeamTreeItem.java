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

import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.SimplePanel;

import generated.dkf.Team;
import generated.dkf.TeamMember;

/**
 * A tree item that represents a team in the team organization outline and
 * allows the author to rename it, remove it, edit it, and add teams or team
 * members to it.
 * 
 * @author nroberts
 */
public class TeamTreeItem extends TeamObjectTreeItem<Team> {

    /** Logger for the class */
    private static final Logger logger = Logger.getLogger(TeamTreeItem.class.getName());

    /** The deck used to change the type of echelon for the team that is shown */
    protected DeckPanel echelonDeck = new DeckPanel();

    /** A panel used to hide the learner ID editor */
    protected SimplePanel emptyPanel = new SimplePanel();

    /**
     * Creates a new tree item that represents and modifies the given team
     * 
     * @param team the team to represent and modify
     */
    public TeamTreeItem(final Team team) {
        this(team, false);
    }

    /**
     * Creates a new tree item that represents and modifies the given team
     * 
     * @param team the team to represent and modify
     * @param draggable whether or not the tree item should be draggable
     */
    public TeamTreeItem(final Team team, boolean draggable) {
        super(team, draggable);

        getElement().getStyle().setFontWeight(FontWeight.BOLD);

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Constructing team tree item for team " + team.toString());
        }

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Populating child elements");
        }

        for (Serializable childUnit : team.getTeamOrTeamMember()) {
            if (childUnit instanceof Team) {
                addItem(createTeamTreeItem((Team) childUnit));
            } else if (childUnit instanceof TeamMember) {
                addItem(createTeamMemberTreeItem((TeamMember) childUnit));
            }
        }

        setState(true);

        echelonDeck.setAnimationEnabled(true);

        echelonDeck.addDomHandler(new MouseDownHandler() {
            @Override
            public void onMouseDown(MouseDownEvent event) {
                /* clicking in the editor should not propagate to the tree */
                event.stopPropagation();
            }
        }, MouseDownEvent.getType());

        echelonDeck.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                /* clicking in the editor should not propagate to the tree */
                event.stopPropagation();
            }
        }, ClickEvent.getType());

        echelonDeck.add(emptyPanel);

        containerPanel.add(echelonDeck);

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Finished constructing team tree item");
        }
    }

    /**
     * Creates a new {@link TeamTreeItem}
     * 
     * @param team the team to represent and modify
     * @return the newly created {@link TeamTreeItem}
     */
    protected TeamTreeItem createTeamTreeItem(Team team) {
        return new TeamTreeItem(team);
    }

    /**
     * Creates a new {@link TeamMemberTreeItem}
     * 
     * @param teamMember the team member to represent and modify
     * @return the newly created {@link TeamMemberTreeItem}
     */
    protected TeamMemberTreeItem createTeamMemberTreeItem(TeamMember teamMember) {
        return new TeamMemberTreeItem(teamMember);
    }

    @Override
    public boolean allowDrop(TeamObjectTreeItem<?> otherItem) {

        if (otherItem == null) {
            return false;
        }

        Serializable object = otherItem.getTeamObject();

        // allow other teams and team members to be dropped onto this team
        return (object instanceof Team && !otherItem.getTeamObject().equals(getTeamObject()))
                || object instanceof TeamMember;
    }

    @Override
    public boolean shouldDropBelow(TeamObjectTreeItem<?> otherItem) {

        if (otherItem == null) {
            return false;
        }

        Serializable object = otherItem.getTeamObject();

        // allow other teams and team members to be dropped onto this team
        return object instanceof TeamMember;
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