/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.wrap.overlay;

import java.util.Set;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Collapse;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.constants.IconType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.Coordinate;
import generated.dkf.Point;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.gwt.client.validation.WidgetValidationStatus;
import mil.arl.gift.common.gwt.client.widgets.ColorBox;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.header.editor.PlacesOfInterestPanel;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared.ScenarioCoordinateEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared.ScenarioCoordinateEditor.RibbonPanelChangeCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.validation.ScenarioValidationComposite;
import mil.arl.gift.tools.map.client.draw.PointShape;

/**
 * An editor that modifies a {@link Point} for the places of interest overlay
 * 
 * @author nroberts
 */
public class OverlayPointEditor extends ScenarioValidationComposite {

    private static OverlayPointEditorUiBinder uiBinder = GWT.create(OverlayPointEditorUiBinder.class);

    interface OverlayPointEditorUiBinder extends UiBinder<Widget, OverlayPointEditor> {
    }
    
    /** The icon representing the type of object being edited */
    @UiField(provided = true)
    protected Widget icon = PlacesOfInterestPanel.getPlaceIcon(Point.class);
    
    /** The place of interest name text box */
    @UiField
    protected TextBox nameBox;
    
    @UiField
    protected ColorBox colorBox;
    
    @UiField
    protected Button coordinatesButton;
    
    @UiField
    protected Collapse coordinatesCollapse;
    
    /** The place of interest coordinate */
    @UiField
    protected ScenarioCoordinateEditor coordinateEditor;
    
    /** Validation that displays an error when the author has not entered a unique name for this place of interest */
    private WidgetValidationStatus nameValidation;
    
    /** The name of the place of interest being edited when it was originally loaded */
    private String originalName = null;
    
    /** The coordinate currently being edited */
    private Coordinate currentCoordinate = null;
    
    /** The shape that will be updated as the author interacts with this editor */
    private PointShape<?> shape;

    /**
     * Creates a new point editor and initializes its input handlers and validation logic
     */
    public OverlayPointEditor() {
        initWidget(uiBinder.createAndBindUi(this));
        
        nameValidation = new WidgetValidationStatus(nameBox, "Please enter a unique name for this place of interest.");
        
        nameBox.addValueChangeHandler(new ValueChangeHandler<String>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                requestValidation(nameValidation);
                
                updateShapeToMatchEditor();
            }
        });
        
        colorBox.addValueChangeHandler(new ValueChangeHandler<String>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                requestValidation(nameValidation);
                
                updateShapeToMatchEditor();
            }
        });
        
        coordinateEditor.setTypeLabelVisible(false);
        coordinateEditor.setChangeTypeButtonVisible(false);
        
        coordinateEditor.addRibbonPanelChangeCallback(new RibbonPanelChangeCallback() {
            @Override
            public void ribbonVisible(boolean visible) {
                validateAll();
            }
        });
        
        coordinatesButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                //change the appearance of the button used to display the coordinate summary whenever it is clicked
                setCoordinatesVisible(IconType.CARET_RIGHT.equals(coordinatesButton.getIcon()));
            }
        });
    }
    
    /**
     * Loads the given point shape into this editor and begins editing it
     * 
     * @param pointShape the point shape to edit
     */
    public void edit(PointOfInterestShape pointShape) {
        
        Point point = pointShape.getPlaceOfInterest();
        shape = pointShape.getMapShape();
        
        String name = point.getName();
        originalName = name;
        nameBox.setValue(name);
        
        colorBox.setValue(point.getColorHexRGBA());
        
        if(point.getCoordinate() == null) {
            point.setCoordinate(new Coordinate());
        }
    
        currentCoordinate = point.getCoordinate();
        coordinateEditor.setCoordinate(currentCoordinate);
    }

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        validationStatuses.add(nameValidation);
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        if(nameValidation.equals(validationStatus)) {
            nameValidation.setValidity(OverlayPoiItemEditor.isEnteredNameUnique(originalName, nameBox.getValue()));
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
        colorBox.setEnabled(!isReadonly);
        coordinateEditor.setReadOnly(isReadonly);
    }

    /**
     * Applies the edits made to this editor's fields to the given point shape
     * 
     * @param pointShape the point shape to which edits should be applied
     */
    public void applyEdits(PointOfInterestShape pointShape) {
        
        if (pointShape == null) {
            throw new IllegalArgumentException("The parameter 'pointShape' cannot be null.");
        }
        
        Point point = pointShape.getPlaceOfInterest();
        
        coordinateEditor.updateCoordinate();
        
        point.setName(nameBox.getValue());
        point.setColorHexRGBA(colorBox.getValue());
        point.setCoordinate(currentCoordinate);
        
      
        if (nameBox.getValue() == null || originalName == null || !nameBox.getValue().equals(originalName)) {

            // update references to this place of interest whenever its name changes
            ScenarioClientUtility.updatePlaceOfInterestReferences(originalName, nameBox.getValue());
        }
    }
    
    /**
     * Sets whether or not this point's coordinates should be visible
     * 
     * @param visible whether the coordinates should be visible
     */
    public void setCoordinatesVisible(boolean visible) {
        
        if(visible) {
            
            coordinatesButton.setIcon(IconType.CARET_DOWN);
            
            coordinatesCollapse.show();
            
        } else {
            
            coordinatesButton.setIcon(IconType.CARET_RIGHT);
            coordinatesButton.toggle();
            
            coordinatesCollapse.hide();
        }
        
        coordinatesButton.setActive(visible);
    }
    
    /**
     * Updates and redraws the shape associated with this editor to reflect the editor's current values
     */
    private void updateShapeToMatchEditor() {
        
        if(shape != null) {
            
            PointOfInterestShape pointShape = new PointOfInterestShape(shape);
            applyEdits(pointShape);
            
            pointShape.updateMapShape();
            pointShape.getMapShape().draw();
        }
    }
}
