/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.outline.tree;

import generated.dkf.Scenario.EndTriggers;

/**
 * A {@link ScenarioObjectTreeItem} that represents a {@link EndTriggers} object
 * within a given {@link Scenario}.
 * 
 * @author tflowers
 *
 */
public class ScenarioEndTriggersTreeItem extends ScenarioObjectTreeItem<EndTriggers> {

    /**
     * Constructs a {@link ScenarioEndTriggersTreeItem} that represents the
     * provided {@link EndTriggers}.
     * 
     * @param endTriggers The {@link EndTriggers} that the
     *        {@link ScenarioEndTriggersTreeItem} represents. Can't be null.
     */
    public ScenarioEndTriggersTreeItem(EndTriggers endTriggers) {
        super(endTriggers);
        getNameLabel().setEditingEnabled(false);
    }
}