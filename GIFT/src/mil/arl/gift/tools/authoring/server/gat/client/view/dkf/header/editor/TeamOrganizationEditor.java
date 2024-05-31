/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.header.editor;

import generated.dkf.TeamOrganization;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.AbstractScenarioObjectEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.ScenarioEditorDirtyEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.ScenarioEditorEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.validation.ScenarioValidationComposite;

/**
 * An editor that modifies a scenario's global team organization
 */
public class TeamOrganizationEditor extends AbstractScenarioObjectEditor<TeamOrganization> {
    
    /** The panel that this editor uses to handle its UI components */
    private TeamOrganizationPanel panel = new TeamOrganizationPanel();
    
    /**
     * Instantiates a new team organization editor
     */
    public TeamOrganizationEditor() {
        setWidget(panel);
    }

    @Override
    protected void processEvent(ScenarioEditorEvent event) {
        
        if(event instanceof ScenarioEditorDirtyEvent) {
            panel.handleDirtyEvent((ScenarioEditorDirtyEvent) event);
        }
    }

    @Override
    public ScenarioValidationComposite getValidationCompositeChild() {
        return panel;
    }

    @Override
    protected void editObject(TeamOrganization courseObject) {
        panel.edit(courseObject);
    }

}
