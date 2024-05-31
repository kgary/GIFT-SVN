/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.header.editor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.constants.IconSize;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.AGL;
import generated.dkf.Area;
import generated.dkf.Coordinate;
import generated.dkf.GCC;
import generated.dkf.GDC;
import mil.arl.gift.common.enums.TrainingApplicationEnum;
import mil.arl.gift.common.gwt.client.coordinate.CoordinateType;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.gwt.client.validation.WidgetValidationStatus;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.BubbleLabel;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared.WrapButton;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.CreateListAction;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemField;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemListEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemListEditor.ListChangedCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ListChangedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.validation.ScenarioValidationComposite;

/**
 * An editor that modifies an {@link Area}
 * 
 * @author nroberts
 */
public class AreaEditor extends ScenarioValidationComposite {
    
    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(AreaEditor.class.getName());

    private static AreaEditorUiBinder uiBinder = GWT.create(AreaEditorUiBinder.class);

    interface AreaEditorUiBinder extends UiBinder<Widget, AreaEditor> {
    }
    
    /** The icon representing the type of object being edited */
    @UiField(provided = true)
    protected Widget icon = PlacesOfInterestPanel.getPlaceIcon(Area.class);
    
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
            
            //apply this editor's edits to a dummy place of interest so we can load them into the wrap panel
            Area editedPlace = new Area();
            editedPlace.setColorHexRGBA(originalPlace.getColorHexRGBA());
            
            applyEdits(editedPlace, false);
            
            return editedPlace;
        }
    };
    
    /** Validation that displays an error when the author has not entered a unique name for this place of interest */
    private WidgetValidationStatus nameValidation;
    
    /** Validation that displays an error when the author has not authored any coordinates */
    private WidgetValidationStatus coordsValidation;
    
    /** The name of the place of interest being edited when it was originally loaded */
    private String originalName = null;
    
    @UiField(provided=true)
    protected ItemListEditor<Coordinate> coordinateList;
    
    /** The original place of interest loaded into this editor*/
    private Area originalPlace = null;
    
    /** the coordinate editor instance for editing points in an area */
    final private CoordinateItemEditor coordinateItemEditor;

    /**
     * Creates a new point editor and initializes its input handlers and validation logic
     */
    public AreaEditor() {
        
        //must be called before initwidget
        coordinateItemEditor = new CoordinateItemEditor();
        
        TrainingApplicationEnum taType = ScenarioClientUtility.getTrainingAppType();
        if(taType != TrainingApplicationEnum.UNITY_EMBEDDED){
            //AGL is used by Unity and VBS.  For VBS, only Identify PoIs condition uses AGL.  That condition only consumes points.
            logger.info("Hiding AGL coordinate type for area editor.");
            coordinateItemEditor.getCoordinateEditor().setDisallowedTypes(CoordinateType.AGL); 
        }
        
        coordinateList = new ItemListEditor<Coordinate>(coordinateItemEditor);
        
        initWidget(uiBinder.createAndBindUi(this));
        
        nameValidation = new WidgetValidationStatus(nameBox, "Please enter a unique name for this place of interest.");
        
        nameBox.addValueChangeHandler(new ValueChangeHandler<String>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                requestValidation(nameValidation);
            }
        });        

        coordinateList.setPlaceholder("No points have been created.");
        coordinateList.setRemoveItemDialogTitle("Delete Point?");

        coordinateList.setFields(buildListFields());
        
        coordinateList.addListChangedCallback(new ListChangedCallback<Coordinate>() {
            
            @Override
            public void listChanged(ListChangedEvent<Coordinate> event) {
                validateAll();
            }
        });
        
        Widget addCoordWidget = coordinateList.addCreateListAction("Click here to add a new point", new CreateListAction<Coordinate>() {

            @Override
            public Coordinate createDefaultItem() {
                
                Coordinate newCoordinate = new Coordinate();
                
                if(!coordinateList.getItems().isEmpty()) {
                    
                    Coordinate firstCoordinate = coordinateList.getItems().get(0);
                    
                    if(firstCoordinate.getType() instanceof GCC) {
                        newCoordinate.setType(new GCC());
                        
                    } else if(firstCoordinate.getType() instanceof GDC) {
                        newCoordinate.setType(new GDC());
                        
                    } else if(firstCoordinate.getType() instanceof AGL) {
                        newCoordinate.setType(new AGL());
                    }
                }
                
                return newCoordinate;
            }
        });
        
        coordsValidation = new WidgetValidationStatus(addCoordWidget, "There are fewer than 3 points defining this area. Please add a point.");
    }
    
    /**
     * Sets the disallowed types to be hidden in the coordinate editor.
     * 
     * @param disallowedTypes the {@link CoordinateType types} to be hidden.  Can be empty/null
     * to hide nothing.
     */
    public void setDisallowedTypes(CoordinateType... disallowedTypes) {
        coordinateItemEditor.getCoordinateEditor().setDisallowedTypes(disallowedTypes);
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
                BigDecimal zValue;
                
                switch (typeEnum) {
                case GCC:
                    GCC gcc = (GCC) coordinateType;
                    xValue = gcc.getX();
                    yValue = gcc.getY();
                    zValue = gcc.getZ();
                    break;
                case GDC:
                    GDC gdc = (GDC) coordinateType;
                    xValue = gdc.getLongitude();
                    yValue = gdc.getLatitude();
                    zValue = gdc.getElevation();
                    break;
                case AGL:
                    AGL agl = (AGL) coordinateType;
                    xValue = agl.getX();
                    yValue = agl.getY();
                    zValue = agl.getElevation();
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
                
                if (zValue == null) {
                    zValue = BigDecimal.ZERO;
                }

                Icon icon = new Icon(typeEnum.getIconType());
                icon.setSize(IconSize.LARGE);
                flowPanel.add(icon);
                
                InlineHTML htmlLabel = new InlineHTML(typeEnum.name());
                htmlLabel.getElement().getStyle().setPaddingLeft(5, Unit.PX);
                htmlLabel.getElement().getStyle().setPaddingRight(5, Unit.PX);
                flowPanel.add(htmlLabel);
                
                flowPanel.add(new BubbleLabel(typeEnum.buildXLabel(Float.valueOf(xValue.floatValue()))));
                flowPanel.add(new BubbleLabel(typeEnum.buildYLabel(Float.valueOf(yValue.floatValue()))));
                flowPanel.add(new BubbleLabel(typeEnum.buildZLabel(Float.valueOf(zValue.floatValue()))));

                return flowPanel;
            }
        });

        return fields;
    }
    
    /**
     * Loads the given area into this editor and begins editing it
     * 
     * @param area the area to edit
     */
    public void edit(Area area) {
        
        originalPlace = area;
        
        String name = ScenarioClientUtility.getPlaceOfInterestName(area);
        originalName = name;
        nameBox.setValue(name);
        
        List<Coordinate> coordinates = new ArrayList<>();
        
        for(Coordinate coord : area.getCoordinate()){
            coordinates.add(CoordinateItemEditor.deepCopy(coord));
        }
        
        coordinateList.setItems(coordinates);
    }

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        validationStatuses.add(nameValidation);
        validationStatuses.add(coordsValidation);
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        if(nameValidation.equals(validationStatus)) {
            nameValidation.setValidity(PlaceOfInterestItemEditor.isEnteredNameUnique(originalName, nameBox.getValue()));
            
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
        coordinateList.setReadonly(isReadonly);
    }

    /**
     * Applies the edits made to this editor's fields to the given area object
     * 
     * @param area the area to which edits should be applied
     */
    public void applyEdits(Area area) {
        applyEdits(area, true);
    }
    
    /**
     * Applies the edits made to this editor's fields to the given area object and optionally updates its name references
     * 
     * @param area the area to which edits should be applied
     * @param updateNameReferences whether or not to update the references to the area's original name
     */
    private void applyEdits(Area area, boolean updateNameReferences) {
        
        if (area == null) {
            throw new IllegalArgumentException("The parameter 'area' cannot be null.");
        }
        
        area.setName(nameBox.getValue());
        
        area.getCoordinate().clear();
        area.getCoordinate().addAll(coordinateList.getItems());
      
        if(updateNameReferences) {
            if (nameBox.getValue() == null || originalName == null || !nameBox.getValue().equals(originalName)) {
    
                // update references to this place of interest whenever its name changes
                ScenarioClientUtility.updatePlaceOfInterestReferences(originalName, nameBox.getValue());
            }
        }
    }

}
