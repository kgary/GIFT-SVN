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

import generated.dkf.AvailableLearnerActions;

/**
 * The {@link ScenarioObjectTreeItem} for the {@link AvailableLearnerActions}
 * within the {@link Scenario}.
 * 
 * @author tflowers
 *
 */
public class LearnerActionsTreeItem extends ScenarioObjectTreeItem<AvailableLearnerActions> {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(LearnerActionsTreeItem.class.getName());

    /**
     * Constructs a {@link LearnerActionsTreeItem}
     * 
     * @param learnerActions The {@link AvailableLearnerActions} that this
     *        {@link LearnerActionsTreeItem} represents. Can't be null.
     */
    public LearnerActionsTreeItem(AvailableLearnerActions learnerActions) {
        super(learnerActions);
        
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("LearnerActionsTreeItem(" + learnerActions + ")");
        }

        getNameLabel().setEditingEnabled(false);
    }
}