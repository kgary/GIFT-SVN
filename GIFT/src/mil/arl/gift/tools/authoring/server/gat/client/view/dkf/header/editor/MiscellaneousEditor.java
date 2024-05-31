/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.header.editor;

import generated.dkf.Scenario;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.AbstractScenarioObjectEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.ScenarioEditorEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.validation.ScenarioValidationComposite;

/**
 * An editor that modifies {@link Scenario} miscellaneous properties.
 * 
 * @author sharrison
 */
public class MiscellaneousEditor extends AbstractScenarioObjectEditor<Scenario> {

    /**
     * The view being modified by this editor's presenter
     */
    private MiscellaneousPanel miscellaneousPanel = new MiscellaneousPanel();

    @Override
    protected void editObject(Scenario scenario) {
        if (scenario == null) {
            throw new IllegalArgumentException("The parameter 'scenario' cannot be null.");
        }

        miscellaneousPanel.edit(scenario);
        miscellaneousPanel.validateAll();
        setWidget(miscellaneousPanel);
    }

    @Override
    protected void processEvent(ScenarioEditorEvent event) {
        // Doesn't respond to any of the events routed to it
    }

    @Override
    public ScenarioValidationComposite getValidationCompositeChild() {
        return miscellaneousPanel;
    }
}