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

import generated.dkf.PlacesOfInterest;

/**
 * A {@link TreeItem} that represents {@link PlacesOfInterest} within a
 * {@link Scenario}.
 * 
 * @author tflowers
 *
 */
public class WaypointsTreeItem extends ScenarioObjectTreeItem<PlacesOfInterest> {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(WaypointsTreeItem.class.getName());

    /**
     * Constructs a {@link WaypointsTreeItem}
     * 
     * @param placesOfInterest The {@link PlacesOfInterest} that this {@link WaypointsTreeItem}
     *        represents. Can't be null.
     */
    public WaypointsTreeItem(PlacesOfInterest placesOfInterest) {
        super(placesOfInterest);

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("WaypointTreeItem(" + placesOfInterest + ")");
        }

        getNameLabel().setEditingEnabled(false);
    }
}