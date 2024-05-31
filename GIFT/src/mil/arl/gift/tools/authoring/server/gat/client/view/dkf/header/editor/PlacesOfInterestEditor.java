/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.header.editor;

import java.io.Serializable;

import generated.dkf.Area;
import generated.dkf.Path;
import generated.dkf.PlacesOfInterest;
import generated.dkf.Point;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.AbstractScenarioObjectEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.CreateScenarioObjectEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.DeleteScenarioObjectEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.RenameScenarioObjectEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.ScenarioEditorEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.validation.ScenarioValidationComposite;

/**
 * An editor that modifies a scenario's global places of interest
 */
public class PlacesOfInterestEditor extends AbstractScenarioObjectEditor<PlacesOfInterest> {

    /** The view being modified by this editor's presenter */
    PlacesOfInterestPanel poiPanel = new PlacesOfInterestPanel();

    /**
     * Instantiates a new places of interest editor.
     */
    public PlacesOfInterestEditor() {
    }

    @Override
    protected void editObject(PlacesOfInterest placesOfInterest) {
        if (placesOfInterest == null) {
            throw new IllegalArgumentException("The parameter 'placesOfInterest' cannot be null.");
        }
        
        poiPanel.edit(placesOfInterest);
        poiPanel.validateAll();
        
        setWidget(poiPanel);
    }
    
    @Override
    protected void processEvent(ScenarioEditorEvent event) {
        // create event
        if (event instanceof CreateScenarioObjectEvent) {
            CreateScenarioObjectEvent createEvent = (CreateScenarioObjectEvent) event;
            Serializable obj = createEvent.getScenarioObject();
            if (obj instanceof Point || obj instanceof Area || obj instanceof Path) {
                poiPanel.rebuildPlacesOfInterestTable();
            }

            // delete event
        } else if (event instanceof DeleteScenarioObjectEvent) {
            DeleteScenarioObjectEvent deleteEvent = (DeleteScenarioObjectEvent) event;
            Serializable obj = deleteEvent.getScenarioObject();
            if (obj instanceof Point || obj instanceof Area || obj instanceof Path) {
                poiPanel.rebuildPlacesOfInterestTable();
            }

            // rename event
        } else if (event instanceof RenameScenarioObjectEvent) {
            RenameScenarioObjectEvent renameEvent = (RenameScenarioObjectEvent) event;
            Serializable obj = renameEvent.getScenarioObject();
            if (obj instanceof Point || obj instanceof Area || obj instanceof Path) {
                poiPanel.refreshPlacesOfInterest(obj);
            }
        }
    }

    @Override
    public ScenarioValidationComposite getValidationCompositeChild() {
        return poiPanel;
    }
}