/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared;

import java.io.Serializable;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconType;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.condition.ConditionInputPanel;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.wrap.WrapPanel;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelDialog;

/**
 * A button used to show the wrap overlay panel. Callers can override this class's {@link #getOriginalPlaceToWrap()} and/or
 * {@link #getUnappliedPlaceChanges()} methods to automatically begin editing places of interest when the wrap overlay
 * is shown.
 * 
 * @author nroberts
 */
public class WrapButton extends Button {
    
    /** The continue button for the unsaved changes dialog */
    private Button continueButton = new Button("Continue");

    /**
     * Creates a new button to show the wrap overlay panel. If the current training application is not supported by the overlay,
     * then this button will be hidden from the author.
     */
    public WrapButton() {
        this(true);
    }

    /**
     * Creates a new button to show the wrap overlay panel. If the current training application is not supported by the overlay,
     * then this button will be hidden from the author.
     * 
     * @param automaticallyAddPoints whether to automatically add the points authored to the condition being edited
     */
    public WrapButton(final boolean automaticallyAddPoints) {
        
        if(WrapPanel.isCurrentTrainingAppSupported()) {
            
            setText("Edit on Map");
            
            setType(ButtonType.PRIMARY);
            setIcon(IconType.MAP);
            addStyleName("wrapButton");
            
            setVisible(true);
            
            continueButton.setType(ButtonType.PRIMARY);
            
            addClickHandler(new ClickHandler() {
                
                @Override
                public void onClick(ClickEvent event) {
                    if (ConditionInputPanel.isDirty()) {
                        // show message warning of unsaved changes
                        OkayCancelDialog.show("Unsaved Changes",
                                "You have unsaved changes. If you continue to the Map Editor, you may lose them.<br/><br/>"+
                                        "If you wish to save your authored values then select the Set button on the editor "+
                                        "before attempting to jump to the map editor.<br/><br/>Would you like to continue to the map editor now?",
                                "Continue", new OkayCancelCallback() {
                                    
                                    @Override
                                    public void okay() {
                                        WrapPanel.editOnMap(getOriginalPlaceToWrap(), getUnappliedPlaceChanges(), automaticallyAddPoints);
                                        
                                    }
                                    
                                    @Override
                                    public void cancel() {
                                        // nothing to do
                                    }
                                });

                    } else {
                        WrapPanel.editOnMap(getOriginalPlaceToWrap(), getUnappliedPlaceChanges(), automaticallyAddPoints);
                    }
                }
            });
            
        } else {
            setVisible(false);
        }
    }
    
    /**
     * Gets the place of interest that should be opened for editing when this button is clicked on. When the overlay is shown,
     * it will try to find this place of interest in its list and reuse its existing editor so that any changes are applied to
     * the same object.
     * <br/><br/>
     * By default, no place of interest will be opened for editing when the overlay is shown, unless this method is explicitly
     * overridden.
     * 
     * @return the original place of interest to open for editing in the wrap overlay
     */
    public Serializable getOriginalPlaceToWrap() {
        return null;
    }
    
    /**
     * Gets a place of interest representing changes made in an active editor that have not yet been saved to an existing place 
     * of interest. When the overlay is shown, it will update the appropriate editor with the unsaved changes so that the author 
     * can continue authoring where they left off.
     * <br/><br/>
     * If the place of interest is returned by {@link #getOriginalPlaceToWrap()} is found in the overlay's list of places
     * of interest, then the unsaved changes will be applied to that place of interest's editor.
     * <br/><br/>
     * If If the place of interest is returned by {@link #getOriginalPlaceToWrap()} is NOT found in the overlay's list of places
     * of interest, then the unsaved changes will be used to start editing a new place of interest.
     * <br/><br/>
     * By default, no place changes will be applied to the appropriate editor when the overlay is shown, unless this method is
     * explicitly overriden.
     * 
     * @return a place of interest representing changes to apply to the appropriate editor in the overlay
     */
    public Serializable getUnappliedPlaceChanges() {
        return null;
    }
}
