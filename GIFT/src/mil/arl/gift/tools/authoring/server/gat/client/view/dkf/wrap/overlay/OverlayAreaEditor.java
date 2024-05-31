/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.wrap.overlay;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
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
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.AGL;
import generated.dkf.Area;
import generated.dkf.Coordinate;
import generated.dkf.GCC;
import generated.dkf.GDC;
import mil.arl.gift.common.gwt.client.coordinate.CoordinateType;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.gwt.client.validation.WidgetValidationStatus;
import mil.arl.gift.common.gwt.client.widgets.ColorBox;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.header.editor.CoordinateItemEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.header.editor.PlacesOfInterestPanel;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemField;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemListEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemListEditor.ListChangedCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ListChangedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.validation.ScenarioValidationComposite;
import mil.arl.gift.tools.map.client.draw.PolygonShape;

/**
 * An editor that modifies an {@link Area} for the places of interest overlay
 * 
 * @author nroberts
 */
public class OverlayAreaEditor extends ScenarioValidationComposite {

    private static OverlayAreaEditorUiBinder uiBinder = GWT.create(OverlayAreaEditorUiBinder.class);

    interface OverlayAreaEditorUiBinder extends UiBinder<Widget, OverlayAreaEditor> {
    }
    
    /** The icon representing the type of object being edited */
    @UiField(provided = true)
    protected Widget icon = PlacesOfInterestPanel.getPlaceIcon(Area.class);
    
    /** The place of interest name text box */
    @UiField
    protected TextBox nameBox;
    
    @UiField
    protected ColorBox colorBox;
    
    @UiField
    protected Button coordinatesButton;
    
    @UiField
    protected Collapse coordinatesCollapse;
    
    /** Validation that displays an error when the author has not entered a unique name for this place of interest */
    private WidgetValidationStatus nameValidation;
    
    /** Validation that displays an error when the author has not authored any coordinates */
    private WidgetValidationStatus coordsValidation;
    
    /** The name of the place of interest being edited when it was originally loaded */
    private String originalName = null;
    
    /** The editor used to edit items in the coordinate list */
    private CoordinateItemEditor coordinateItemEditor = new CoordinateItemEditor();
    
    @UiField(provided=true)
    protected ItemListEditor<Coordinate> coordinateList = new ItemListEditor<Coordinate>(coordinateItemEditor);
    
    /** The shape that will be updated as the author interacts with this editor */
    private PolygonShape<?> shape;

    /**
     * Creates a new area editor and initializes its input handlers and validation logic
     */
    public OverlayAreaEditor() {
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
                updateShapeToMatchEditor();
            }
        });
        
        coordinateItemEditor.getCoordinateEditor().setTypeLabelVisible(false);
        coordinateItemEditor.getCoordinateEditor().setChangeTypeButtonVisible(false);
        coordinateItemEditor.getElement().getStyle().setProperty("margin", "-10px -25px 0px");
        
        coordinateList.setPlaceholder("No points have been created.");
        coordinateList.setRemoveItemDialogTitle("Delete Point?");

        coordinateList.setFields(buildListFields());
        
        coordinateList.addListChangedCallback(new ListChangedCallback<Coordinate>() {
            
            @Override
            public void listChanged(ListChangedEvent<Coordinate> event) {
                
                coordinatesButton.setText(coordinateList.getItems().size() + " points");
                
                validateAll();
                
                updateShapeToMatchEditor();
            }
        });
        
        coordinatesButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                //change the appearance of the button used to display the coordinate summary whenever it is clicked
                setCoordinatesVisible(IconType.CARET_RIGHT.equals(coordinatesButton.getIcon()));
            }
        });
        
        coordsValidation = new WidgetValidationStatus(coordinateList, "There are fewer than 3 points defining this area. Please add a point.");
    }
    
    /**
     * Builds the fields for the list of coordinates
     * 
     * @return the list of fields
     */
    private List<ItemField<Coordinate>> buildListFields() {
        
        List<ItemField<Coordinate>> fields = new ArrayList<>();
       
        fields.add(new ItemField<Coordinate>(null, "100%") {
            @Override
            public Widget getViewWidget(Coordinate coordinate) {

                Serializable coordinateType;
                if (coordinate.getType() == null) {
                    GCC gcc = new GCC();
                    gcc.setX(BigDecimal.ZERO);
                    gcc.setY(BigDecimal.ZERO);
                    gcc.setZ(BigDecimal.ZERO);
                    coordinateType = gcc;
                }else {
                    coordinateType = coordinate.getType();
                }

                FlowPanel flowPanel = new FlowPanel();

                CoordinateType typeEnum = CoordinateType.getCoordinateTypeFromCoordinate(coordinateType);
                
                BigDecimal xValue;
                BigDecimal yValue;
                
                switch (typeEnum) {
                case GCC:
                    GCC gcc = (GCC) coordinateType;
                    xValue = gcc.getX();
                    yValue = gcc.getY();
                    break;
                case GDC:
                    GDC gdc = (GDC) coordinateType;
                    xValue = gdc.getLongitude();
                    yValue = gdc.getLatitude();
                    break;
                case AGL:
                    AGL agl = (AGL) coordinateType;
                    xValue = agl.getX();
                    yValue = agl.getY();
                    break;
                default:
                    throw new UnsupportedOperationException("The type '" + typeEnum + "' was unexpected.");
                }
                
                if (xValue == null) {
                    xValue = BigDecimal.ZERO;
                }
                
                if (yValue == null) {
                    yValue = BigDecimal.ZERO;
                }
                
                flowPanel.add(new HTML(typeEnum.buildXLabel(Float.valueOf(xValue.floatValue()))));
                flowPanel.add(new HTML(typeEnum.buildYLabel(Float.valueOf(yValue.floatValue()))));

                return flowPanel;
            }
        });

        return fields;
    }
    
    /**
     * Loads the given area shape into this editor and begins editing it
     * 
     * @param areaShape the area shape to edit
     */
    public void edit(AreaOfInterestShape areaShape) {
        
        Area area = areaShape.getPlaceOfInterest();
        shape = areaShape.getMapShape();
        
        String name = area.getName();
        originalName = name;
        nameBox.setValue(name);
        
        colorBox.setValue(area.getColorHexRGBA());
        
        List<Coordinate> coordinates = new ArrayList<>();
        
        for(Coordinate coord : area.getCoordinate()){
            coordinates.add(CoordinateItemEditor.deepCopy(coord));
        }
        
        coordinateList.setItems(coordinates);
        
        coordinatesButton.setText(coordinateList.getItems().size() + " points");
    }

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        validationStatuses.add(nameValidation);
        validationStatuses.add(coordsValidation);
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        if(nameValidation.equals(validationStatus)) {
            nameValidation.setValidity(OverlayPoiItemEditor.isEnteredNameUnique(originalName, nameBox.getValue()));
            
        } else if(coordsValidation.equals(validationStatus)) {
            coordsValidation.setValidity(coordinateList.getItems().size() >= 3);
        }
    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
        childValidationComposites.add(coordinateList);
    }

    /**
     * Sets whether or not this editor's input fields should be read-only
     * 
     * @param isReadonly whether or not fields should be read-only
     */
    public void setReadonly(boolean isReadonly) {
        nameBox.setEnabled(!isReadonly);
        colorBox.setEnabled(!isReadonly);
        coordinateList.setReadonly(isReadonly);
    }

    /**
     * Applies the edits made to this editor's fields to the given area shape
     * 
     * @param areaShape the area shape to which edits should be applied
     */
    public void applyEdits(AreaOfInterestShape areaShape) {
        
        if (areaShape == null) {
            throw new IllegalArgumentException("The parameter 'areaShape' cannot be null.");
        }
        
        Area area = areaShape.getPlaceOfInterest();
        
        area.setName(nameBox.getValue());
        area.setColorHexRGBA(colorBox.getValue());
        
        area.getCoordinate().clear();
        area.getCoordinate().addAll(coordinateList.getItems());
      
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
            
            AreaOfInterestShape pathShape = new AreaOfInterestShape(shape);
            applyEdits(pathShape);
            
            pathShape.updateMapShape();
            pathShape.getMapShape().draw();
        }
    }

}
