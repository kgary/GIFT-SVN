/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.header.editor;

import java.io.Serializable;
import java.util.Set;

import org.gwtbootstrap3.client.ui.TextBox;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.Coordinate;
import generated.dkf.Point;
import mil.arl.gift.common.gwt.client.coordinate.CoordinateType;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.gwt.client.validation.WidgetValidationStatus;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared.ScenarioCoordinateEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared.ScenarioCoordinateEditor.RibbonPanelChangeCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared.WrapButton;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.validation.ScenarioValidationComposite;

/**
 * An editor that modifies a {@link Point}
 * 
 * @author nroberts
 */
public class PointEditor extends ScenarioValidationComposite {

    private static PointEditorUiBinder uiBinder = GWT.create(PointEditorUiBinder.class);

    interface PointEditorUiBinder extends UiBinder<Widget, PointEditor> {
    }
    
    /** The icon representing the type of object being edited */
    @UiField(provided = true)
    protected Widget icon = PlacesOfInterestPanel.getPlaceIcon(Point.class);
    
    /** The place of interest name text box */
    @UiField
    protected TextBox nameBox;
    
    @UiField(provided = true)
    protected WrapButton wrapButton = new WrapButton() {
        
        @Override
        public Serializable getOriginalPlaceToWrap() {
            return originalPlace;
        }
        
        @Override
        public Serializable getUnappliedPlaceChanges() {
            
            Point editedPlace = new Point();
            editedPlace.setColorHexRGBA(originalPlace.getColorHexRGBA());
            
            applyEdits(editedPlace, false);
            
            return editedPlace;
        }
    };
    
    /** The place of interest coordinate */
    @UiField
    protected ScenarioCoordinateEditor coordinateEditor;
    
    /** Validation that displays an error when the author has not entered a unique name for this place of interest */
    private WidgetValidationStatus nameValidation;
    
    /** Validation that displays an error when the author has not selected a coordinate type for this place of interest */
    private WidgetValidationStatus coordinateTypeValidation;
    
    /** The name of the place of interest being edited when it was originally loaded */
    private String originalName = null;
    
    /** The coordinate currently being edited */
    private Coordinate currentCoordinate = null;
    
    /** The original place of interest loaded into this editor*/
    private Point originalPlace = null;

    /**
     * Creates a new point editor and initializes its input handlers and validation logic
     */
    public PointEditor() {
        initWidget(uiBinder.createAndBindUi(this));
        
        nameValidation = new WidgetValidationStatus(nameBox, "Please enter a unique name for this place of interest.");
        coordinateTypeValidation = new WidgetValidationStatus(coordinateEditor, "Please enter a coordinate for this place of interest.");
        
        nameBox.addValueChangeHandler(new ValueChangeHandler<String>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                requestValidation(nameValidation);
            }
        });
        
        coordinateEditor.addRibbonPanelChangeCallback(new RibbonPanelChangeCallback() {
            @Override
            public void ribbonVisible(boolean visible) {
                validateAll();
            }
        });
    }
    
    /**
     * Sets the disallowed types to be hidden in the coordinate editor.
     * 
     * @param disallowedTypes the {@link CoordinateType types} to be hidden.  Can be empty/null
     * to hide nothing.
     */
    public void setDisallowedTypes(CoordinateType... disallowedTypes) {
        coordinateEditor.setDisallowedTypes(disallowedTypes);
    }
    
    /**
     * Loads the given point into this editor and begins editing it
     * 
     * @param point the point to edit
     */
    public void edit(Point point) {
        
        originalPlace = point;
        
        String name = ScenarioClientUtility.getPlaceOfInterestName(point);
        originalName = name;
        nameBox.setValue(name);
        
        if(point.getCoordinate() == null) {
            point.setCoordinate(new Coordinate());
        }
    
        currentCoordinate = point.getCoordinate();
        coordinateEditor.setCoordinate(currentCoordinate);
    }

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        validationStatuses.add(nameValidation);
        validationStatuses.add(coordinateTypeValidation);
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        if(nameValidation.equals(validationStatus)) {
            nameValidation.setValidity(PlaceOfInterestItemEditor.isEnteredNameUnique(originalName, nameBox.getValue()));
        }else if(coordinateTypeValidation.equals(validationStatus)){
            coordinateTypeValidation.setValidity(coordinateEditor.isTypeSelected());
        }
    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
        // no validation composite children
    }
    
    /**
     * Sets whether or not this editor's input fields should be read-only
     * 
     * @param isReadonly whether or not fields should be read-only
     */
    public void setReadonly(boolean isReadonly) {
        nameBox.setEnabled(!isReadonly);
        coordinateEditor.setReadOnly(isReadonly);
    }

    /**
     * Applies the edits made to this editor's fields to the given point object
     * 
     * @param point the point to which edits should be applied
     */
    public void applyEdits(Point point) {
        applyEdits(point, true);
    }

    /**
     * Applies the edits made to this editor's fields to the given point object and optionally updates its name references
     * 
     * @param point the point to which edits should be applied
     * @param updateNameReferences whether or not to update the references to the point's original name
     */
    private void applyEdits(Point point, boolean updateNameReferences) {
        
        if (point == null) {
            throw new IllegalArgumentException("The parameter 'point' cannot be null.");
        }
        
        point.setName(nameBox.getValue());
        
        if(coordinateEditor.isTypeSelected()) {
            coordinateEditor.updateCoordinate();
        }
        
        point.setCoordinate(currentCoordinate);
        
        if(updateNameReferences) {
            if (nameBox.getValue() == null || originalName == null || !nameBox.getValue().equals(originalName)) {
    
                // update references to this place of interest whenever its name changes
                ScenarioClientUtility.updatePlaceOfInterestReferences(originalName, nameBox.getValue());
            }
        }
    }
}
