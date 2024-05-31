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

import generated.dkf.Scenario;

/**
 * A tree item that represents a scenario's miscellaneous properties in the scenario outline and
 * allows the author to edit them.
 * 
 * @author sharrison
 */
public class MiscellaneousTreeItem extends ScenarioObjectTreeItem<Scenario> {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(MiscellaneousTreeItem.class.getName());

    /**
     * Constructs a {@link MiscellaneousTreeItem}
     * 
     * @param scenario The {@link Scenario} that this {@link MiscellaneousTreeItem} represents.
     *        Can't be null.
     */
    public MiscellaneousTreeItem(Scenario scenario) {
        super(scenario);

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("MiscellaneousTreeItem(" + scenario + ")");
        }

        getNameLabel().setEditingEnabled(false);
    }
}