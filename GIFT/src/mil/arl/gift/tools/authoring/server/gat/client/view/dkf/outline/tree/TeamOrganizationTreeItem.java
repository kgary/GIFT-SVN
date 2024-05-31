/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.outline.tree;

import java.util.logging.Level;
import java.util.logging.Logger;

import generated.dkf.TeamOrganization;

/**
 * A {@link TreeItem} that represents {@link TeamOrganization} within a
 * {@link Scenario}.
 * 
 * @author tflowers
 *
 */
public class TeamOrganizationTreeItem extends ScenarioObjectTreeItem<TeamOrganization> {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(TeamOrganizationTreeItem.class.getName());

    /**
     * Constructs a {@link TeamOrganizationTreeItem}
     * 
     * @param organization The {@link TeamOrganization} that this {@link TeamOrganizationTreeItem}
     *        represents. Can't be null.
     */
    public TeamOrganizationTreeItem(TeamOrganization organization) {
        super(organization);

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("TeamOrganizationTreeItem(" + organization + ")");
        }

        getNameLabel().setEditingEnabled(false);
    }
}