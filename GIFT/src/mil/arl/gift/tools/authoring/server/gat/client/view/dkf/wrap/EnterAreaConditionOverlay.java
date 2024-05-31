/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.wrap;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import com.google.gwt.dom.client.TableRowElement;
import generated.dkf.Condition;
import generated.dkf.EnterAreaCondition;
import generated.dkf.Point;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.header.editor.PlaceOfInterestConditionReference;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.header.editor.PlaceOfInterestReference;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.wrap.overlay.AbstractPlaceOfInterestShape;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.wrap.overlay.EnterAreaSubEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.wrap.overlay.PointOfInterestShape;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemFormatter;
import mil.arl.gift.tools.map.client.draw.AbstractMapShape;
import mil.arl.gift.tools.map.client.draw.PointShape;
import mil.arl.gift.tools.map.client.draw.ShapeDrawnCallback;

/**
 * An extension of the default places of interest overlay that is used to interact with an EnterAreaCondition and define the
 * entrances that the learner should enter
 * 
 * @author nroberts
 */
public class EnterAreaConditionOverlay extends PlacesOfInterestOverlay {

    /** The style name to apply to places of interest that cannot be selected for use with the condition */
    protected static final String EXCLUDED_ITEM_STYLE = "excludedOverlayItem";

    /** The style name to apply to places of interest that are available but not yet selected for use with the condition */
    protected static final String IGNORED_ITEM_STYLE = "ignoredOverlayItem";

    /** The comparator used to sort the list of places of interest for this condition type */
    private final Comparator<Serializable> ENTER_AREA_LIST_COMPARATOR = new Comparator<Serializable>() {

            @Override
            public int compare(Serializable o1, Serializable o2) {
               
                if(!(o2 instanceof Point)
                        || (doesConditionReferencePlace(o1) && !doesConditionReferencePlace(o2))) {
                    
                    //if the second entry doesn't represent a path or if the first entry is referenced while the 
                    //second isn't, then the first entry should be sorted before the second
                    return -1;
                } 
                
                return 1;
            }
        };

    /** The sub-editor used to edit the condition's list of entrances*/
    private EnterAreaSubEditor subEditor;

    /**
     * Creates a new overlay panel modifying the given condition and initializes the handlers for its controls
     * 
     * @param the condition to modify
     */
    public EnterAreaConditionOverlay(Condition condition) {
        
        super();
        
        if(condition == null) {
            throw new IllegalArgumentException("The condition to load into the overlay cannot be null.");
        }
        
        this.setCondition(condition);
        
        this.subEditor = new EnterAreaSubEditor((EnterAreaCondition) condition.getInput().getType()) {

            @Override
            public void refreshPlaceOfInterestReferences() {
                loadAndFilterPlacesOfInterest();
            }

            @Override
            public void setPoiEditingEnabled(boolean enabled) {
                
                if(!isReadOnly) {
                    setDrawingEnabled(enabled);
                    placesOfInterestList.setReadonly(!enabled);
                }
            }

            @Override
            public boolean arePointsVisible(Point start, Point end) {
                
                AbstractPlaceOfInterestShape<?, ?> startShape = poiToShape.get(start);
                AbstractPlaceOfInterestShape<?, ?> endShape = poiToShape.get(end);  
                
                return startShape != null && startShape.getMapShape().isDrawn()
                        && endShape != null && endShape.getMapShape().isDrawn();
            }
        };
        
        setSubEditor(subEditor);
        
        mainHeader.setText("Enter Area");
        
        setDrawPointInstructions(DEFAULT_DRAW_POINT_INSTRUCTIONS 
                + "<br/><br/>Once you've added a point, you can create an entrance using it by clicking another point while editing it.");
        
        placesOfInterestList.setItemFormatter(new ItemFormatter<AbstractPlaceOfInterestShape<?,?>>() {
            
            @Override
            public void format(AbstractPlaceOfInterestShape<?, ?> item, TableRowElement rowElement) {
                
                if(!(item.getPlaceOfInterest() instanceof Point)) {
                    
                    //paths should be visually distinguished from other place types since they are always ignored
                    rowElement.addClassName(EXCLUDED_ITEM_STYLE);
                    
                } else {
                    
                    rowElement.removeClassName(EXCLUDED_ITEM_STYLE);
                    
                    if(!doesConditionReferencePlace(item.getPlaceOfInterest())) {
                        
                        //places that aren't referenced by the condition should be visually distinguished
                        rowElement.addClassName(IGNORED_ITEM_STYLE);
                        
                    } else {
                        rowElement.removeClassName(IGNORED_ITEM_STYLE);
                    }
                }
                
                if(!isReadOnly && !drawingEnabled) {
                    rowElement.getStyle().setProperty("pointer-events", "none");
                    
                } else {
                    rowElement.getStyle().clearProperty("pointer-events");
                }
            }
        });
    }
    
    @Override
    public void loadAndFilterPlacesOfInterest() {
        
        //remove draw callbacks from rendered shapes to avoid redundantly redrawing entrances, since they will be redrawn anyway
        for(Serializable placeOfInterest : poiToShape.keySet()) {
            
            final AbstractPlaceOfInterestShape<?, ?> placeShape = poiToShape.get(placeOfInterest);
            placeShape.getMapShape().setDrawCallback(null);
        }
        
        //gather places of interest from the scenario and render them on the map
        super.loadAndFilterPlacesOfInterest();
        
        //gather entrances from the condition and render them on the map
        subEditor.redrawEntranceList();
        
        //re-add draw callbacks to automatically update entrances when their associated points are redrawn
        for(Serializable placeOfInterest : poiToShape.keySet()) {
            
            final AbstractPlaceOfInterestShape<?, ?> placeShape = poiToShape.get(placeOfInterest);
            if(placeShape instanceof PointOfInterestShape) {
                
                placeShape.getMapShape().setDrawCallback(new ShapeDrawnCallback() {
                    
                    @Override
                    public void onShapeDrawn(AbstractMapShape<?> shape) {
                        
                        PointOfInterestShape pointShape = (PointOfInterestShape) placeShape;
                        Point point = pointShape.getPlaceOfInterest();
                        
                        //see if there are entrances using this point and update them if there are
                        subEditor.updateEntranceShapes(point, (PointShape<?>) shape);
                    }
                });
            }
        }
    }
    
    @Override
    protected void updateRenderedLayers() {
        super.updateRenderedLayers();
        
        //redraw entrances in case start and end points were hidden
        subEditor.redrawEntranceList();
    }
    
    /**
     * Gets whether or not the loaded condition contains a reference to the given place of interest
     * 
     * @param placeOfInterest the place of interest to check for references to
     * @return whether the condition has any references to the place of interest
     */
    private boolean doesConditionReferencePlace(Serializable placeOfInterest) {
        
        List<PlaceOfInterestReference> references = ScenarioClientUtility.getReferencesTo(placeOfInterest);
        
        if(references != null) {
            for(PlaceOfInterestReference reference : references) {
                
                if(reference instanceof PlaceOfInterestConditionReference &&
                        getCondition().equals(((PlaceOfInterestConditionReference)reference).getCondition())){
                    return true;
                }
            }
        }
        
        return false;
    }
    
    @Override
    protected List<Class<?>> getPreferredTypes() {
        return Arrays.asList(new Class<?>[] {Point.class});
    }
    
    @Override
    protected Comparator<Serializable> getListSortComparator() {
        return ENTER_AREA_LIST_COMPARATOR;
    }
    
    @Override
    protected void onPlaceShapeClicked(AbstractPlaceOfInterestShape<?, ?> placeShape) {
        
        if(!isReadOnly 
                && placesOfInterestList.isEditing() 
                && placeShape != null 
                && !placeShape.equals(poiBeingEdited)
                && placeShape.getPlaceOfInterest() instanceof Point 
                && poiBeingEdited.getPlaceOfInterest() instanceof Point
                && ScenarioClientUtility.getPlacesOfInterest() != null
                && ScenarioClientUtility.getPlacesOfInterest().getPointOrPathOrArea().contains(poiBeingEdited.getPlaceOfInterest())) {
                
                //if the author is editing an existing point and clicks on a second point, create an entrance linking both points
                Point startPoint = (Point) poiBeingEdited.getPlaceOfInterest();
                Point endPoint = (Point) placeShape.getPlaceOfInterest();
                
                placesOfInterestList.cancelEditing();
                
                subEditor.startEditingNewEntrance(startPoint, endPoint);
                
        } else {
            super.onPlaceShapeClicked(placeShape);
        }
    }
    
    @Override
    protected void onEditingStateChanged(boolean isEditing) {
        super.onEditingStateChanged(isEditing);
        
        subEditor.setEntranceEditingEnabled(!isEditing);
    }
    
    @Override
    public void cleanUpMap() {
        super.cleanUpMap();
        
        //clean up any entrances that were rendered
        subEditor.cleanUpMap();
    }
    
    @Override
    public void setReadOnly(boolean isReadOnly) {
        super.setReadOnly(isReadOnly);
        
        subEditor.setReadOnly(isReadOnly);
    }
}
