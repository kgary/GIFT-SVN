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

import org.gwtbootstrap3.client.ui.constants.IconType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.AGL;
import generated.dkf.Coordinate;
import generated.dkf.GCC;
import generated.dkf.GDC;
import generated.dkf.PlacesOfInterest;
import generated.dkf.Point;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared.PlaceOfInterestPicker;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared.Ribbon;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared.ScenarioCoordinateEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared.ScenarioCoordinateEditor.RibbonPanelChangeCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioValidatorUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemEditor;

/**
 * An {@link ItemEditor} used to modify {@link Coordinate coordinates}. This widget basically acts as a {@link ItemEditor} wrapper
 * around a {@link ScenarioCoordinateEditor}.
 * 
 * @author nroberts
 */
public class CoordinateItemEditor extends ItemEditor<Coordinate> {

    private static CoordinateItemEditorUiBinder uiBinder = GWT.create(CoordinateItemEditorUiBinder.class);

    interface CoordinateItemEditorUiBinder extends UiBinder<Widget, CoordinateItemEditor> {
    }
    
    /** The inner coordinate editor*/
    @UiField
    protected ScenarioCoordinateEditor coordinateEditor;
    
    /** A picker used to pick a point of interest to copy a coordinate from */
    @UiField(provided=true)
    protected PlaceOfInterestPicker pointPicker = new PlaceOfInterestPicker(Point.class) {
        
        @Override
        public boolean isActive() {
            
            //need to ensure this picker isn't validated, since doing so can potentially cause an infinite loop
            //(since the Path and Area editors inside the drop down use the same picker)
            return false;
        }
    };
    
    /** A ribbon used to allow the author to pick whether to copy an existing coordinate or create a new one */
    @UiField
    protected Ribbon newOrExistingRibbon;
    
    /** A deck panel used to switch between the editors for new or existing coordinates */
    @UiField
    protected DeckPanel newOrExistingDeck;

    /**
     * Creates a new scenario item editor and initializes its input handlers
     */
    public CoordinateItemEditor() {
        initWidget(uiBinder.createAndBindUi(this));
        
        coordinateEditor.addRibbonPanelChangeCallback(new RibbonPanelChangeCallback() {
            @Override
            public void ribbonVisible(boolean visible) {
                validateAll();
            }
        });
        
        newOrExistingRibbon.setTileWidth(104);
        newOrExistingRibbon.setTileHeight(100);
        
        newOrExistingRibbon.addRibbonItem(IconType.MAP_MARKER, "Existing Point", "Copy the coordinate of an existing point", new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                //allow the author to select a point of interest to copy a coordinate from
                newOrExistingDeck.showWidget(newOrExistingDeck.getWidgetIndex(pointPicker));
                newOrExistingDeck.setVisible(true);
                newOrExistingRibbon.setVisible(false);
                
                setSaveButtonVisible(false);
            }
        });
        
        newOrExistingRibbon.addRibbonItem(IconType.BULLSEYE, "Coordinate", "Define a new coordinate", new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                //allow the author to create a new coordinate
                newOrExistingDeck.showWidget(newOrExistingDeck.getWidgetIndex(coordinateEditor));
                newOrExistingDeck.setVisible(true);
                newOrExistingRibbon.setVisible(false);
                
                setSaveButtonVisible(!ScenarioClientUtility.isReadOnly());
            }
        });
        
        pointPicker.setJumpEnabled(false);
        pointPicker.setPoiEditingEnabled(false);
        pointPicker.addValueChangeHandler(new ValueChangeHandler<String>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                
                if(StringUtils.isNotBlank(event.getValue())) {
                    
                    Serializable placeOfInterest = ScenarioClientUtility.getPlaceOfInterestWithName(event.getValue());
                    
                    if(placeOfInterest != null && placeOfInterest instanceof Point) {
                        
                        Point point = (Point) placeOfInterest;
                        
                        if(point.getCoordinate() != null && point.getCoordinate().getType() != null) {
                            
                            //copy the coordinate of the selected point into the coordinate editor
                            coordinateEditor.populateEditor(point.getCoordinate().getType());
                        }
                    }
                    
                    //switch to the coordinate editor to show the copied coordinate values
                    newOrExistingDeck.showWidget(newOrExistingDeck.getWidgetIndex(coordinateEditor));
                    
                    setSaveButtonVisible(!ScenarioClientUtility.isReadOnly());
                }
            }
        });
    }

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        //Nothing to validate
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        //Nothing to validate
    }

    @Override
    protected boolean validate(Coordinate coordinate) {
        String errorMsg = ScenarioValidatorUtility.validateCoordinate(coordinate);
        return StringUtils.isBlank(errorMsg);
    }

    @Override
    protected void populateEditor(Coordinate obj) {
        
        boolean hasAuthoredValue = false;
        
        if(GatClientUtility.isReadOnly() || !arePointsOfInterestAvailable()) {
            
            //don't allow the author to copy coordinates from existing points if the editor is read only or no points are available
            hasAuthoredValue = true;
            
        } else {
            
            //only allow the author copy coordinates from existing points if the current coordinate has no values
            if(obj != null && obj.getType() != null) {
                
                if(obj.getType() instanceof GCC) {
                    
                    GCC type = (GCC) obj.getType();
                    hasAuthoredValue = type.getX() != null || type.getY() != null || type.getZ() != null;
                    
                } else if(obj.getType() instanceof GDC) {
                    
                    GDC type = (GDC) obj.getType();
                    hasAuthoredValue = type.getLongitude() != null || type.getLatitude() != null || type.getElevation() != null;
                    
                } else if(obj.getType() instanceof AGL) {
                    
                    AGL type = (AGL) obj.getType();
                    hasAuthoredValue = type.getX() != null || type.getY() != null || type.getElevation() != null;
                }
            }
        }
        
        coordinateEditor.setCoordinate(obj);
        pointPicker.setValue(null); //reset the picker used to copy points of interest
        
        if(!hasAuthoredValue) {
            
            //show a ribbon to let the author copy an existing point or create a new coordinate
            newOrExistingDeck.setVisible(false);
            newOrExistingRibbon.setVisible(true);
            
            setSaveButtonVisible(false);
            
        } else {
            
            //start editing the existing coordinate
            newOrExistingDeck.showWidget(newOrExistingDeck.getWidgetIndex(coordinateEditor));
            newOrExistingDeck.setVisible(true);
            newOrExistingRibbon.setVisible(false);
            
            setSaveButtonVisible(!ScenarioClientUtility.isReadOnly());
        }
    }

    @Override
    protected void applyEdits(Coordinate obj) {
        
        if(coordinateEditor.isTypeSelected()) {
            coordinateEditor.updateCoordinate();
        }
    }

    @Override
    protected void setReadonly(boolean isReadonly) {
        coordinateEditor.setReadOnly(isReadonly);
        pointPicker.setReadonly(isReadonly);
    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
        //Nothing to validate
    }

    /** 
     * Gets the inner editor that's actually used to modify the coordinate values
     */
    public ScenarioCoordinateEditor getCoordinateEditor() {
        return coordinateEditor;
    }
    
    /**
     * Creates a deep copy of the given coordinate, with all new objects set to the same values
     * 
     * @param original the original coordinate
     * @return the created copy of the coordinate
     */
    public static Coordinate deepCopy(Coordinate original) {
        
        Coordinate copy = new Coordinate();
        
        if(original.getType() instanceof GCC) {
            
            GCC originalType = (GCC) original.getType();
            GCC copyType = new GCC();
            
            copyType.setX(originalType.getX());
            copyType.setY(originalType.getY());
            copyType.setZ(originalType.getZ());
            
            copy.setType(copyType);
            
        } else if(original.getType() instanceof GDC) {
            
            GDC originalType = (GDC) original.getType();
            GDC copyType = new GDC();
            
            copyType.setLatitude(originalType.getLatitude());
            copyType.setLongitude(originalType.getLongitude());
            copyType.setElevation(originalType.getElevation());
            
            copy.setType(copyType);
            
        } else if(original.getType() instanceof AGL) {
            
            AGL originalType = (AGL) original.getType();
            AGL copyType = new AGL();
            
            copyType.setX(originalType.getX());
            copyType.setY(originalType.getY());
            copyType.setElevation(originalType.getElevation());
            
            copy.setType(copyType);
        }
        
        return copy;
    }
    
    /**
     * Checks whether or not the global list of places of interest contains any points that coordinates can be
     * copied from.
     * 
     * @return whether any points of interest are available to copy coordinates from
     */
    private boolean arePointsOfInterestAvailable() {
        
        PlacesOfInterest pois = ScenarioClientUtility.getPlacesOfInterest();
        
        if(pois != null) {
            for(Serializable poi : pois.getPointOrPathOrArea()){
                
                if(poi instanceof Point) {
                    return true;
                }
            }
        }
        
        return false;
    }
}
