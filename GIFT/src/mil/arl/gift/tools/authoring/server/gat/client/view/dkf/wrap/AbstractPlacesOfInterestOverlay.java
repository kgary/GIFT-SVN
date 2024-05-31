/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.wrap;

import java.io.Serializable;

import com.google.gwt.user.client.ui.Composite;

import generated.dkf.Condition;

/**
 * An abstract overlay used to interact with a scenario's places of interest through the wrap panel. This class represents common
 * functionality shared between the different overlays used to modify places of interest in various ways.
 * 
 * @author nroberts
 *
 */
public abstract class AbstractPlacesOfInterestOverlay extends Composite{
    
    /** The place of interest to open for editing once the map is ready */
    protected Serializable placeToEditWhenReady;

    /** 
     * An optional edited version of the place of interest to open for editing once the map is ready. Used to update the 
     * appropriate editor if the author has made temporary changes that have not yet been pushed to the original place of interest.
     */
    protected Serializable editedPlaceToEditWhenReady;
    
    /** The condition that is currently loaded and should be modified by this overlay*/
    private Condition condition;

    /**
     * Updates the rendered list of places of interest to match the underlying data
     */
    abstract public void refreshPlacesOfInterest();
    
    /**
     * Prepares the editor so that it can begin editing the given place of interest immediately once the map is ready
     * 
     * @param originalPlace the original place of interest to edit
     * @param edittedPlace a copy containing any temporary changes that the author has not yet pushed to the original 
     * place of interest. Used to update the editor with the temporary changes.
     */
    void prepareForEditing(Serializable originalPlace, Serializable edittedPlace) {
        this.placeToEditWhenReady = originalPlace;
        this.editedPlaceToEditWhenReady = edittedPlace;
    }
    
    /**
     * Sets whether or not the layers panel should be visible
     * 
     * @param visible whether the layers panel should be visible
     */
    abstract public void setLayersPanelVisible(boolean visible);
    
    /**
     * Gets whether or not the layers panel is visible
     * 
     * @return whether the panel is visible
     */
    abstract public boolean isLayersPanelVisible();
    
    /**
     * Cleans up the map by removing any visual elements that were added by this overlay
     */
    abstract public void cleanUpMap();

    /**
     * Resets the map's zoom level and view position so that it fits all of the places of interest currently
     * rendered by this overlay.
     */
    abstract public void resetZoomToFitPlaces();

    /**
     * Sets whether or not this widget should be read-only
     * 
     * @param isReadOnly whether this widget should be read-only
     */
    abstract public void setReadOnly(boolean isReadOnly);
    
    /**
     * Gets the condition that this overlay is modifying, if applicable
     *
     * @return the condition being modified
     */
    public Condition getCondition() {
        return condition;
    }

    /**
     * Sets the condition that this overlay should modify
     *
     * @param condition the condition to modify
     */
    public void setCondition(Condition condition) {
        this.condition = condition;
    }
}
