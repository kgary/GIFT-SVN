/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.wrap;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.TextDecoration;
import com.google.gwt.dom.client.Style.TextOverflow;
import com.google.gwt.dom.client.Style.VerticalAlign;
import com.google.gwt.dom.client.Style.WhiteSpace;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.Checkpoint;
import generated.dkf.CheckpointPaceCondition;
import generated.dkf.Condition;
import generated.dkf.Point;
import mil.arl.gift.common.gwt.client.widgets.ColorBox;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.header.editor.PlaceOfInterestConditionReference;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.header.editor.PlaceOfInterestReference;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.header.editor.PlacesOfInterestPanel;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.wrap.overlay.AbstractPlaceOfInterestShape;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.wrap.overlay.PointOfInterestShape;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemField;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemFormatter;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemListEditor.ListChangedCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ListChangedEvent.ListAction;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ListChangedEvent;

/**
 * An extension of the default places of interest overlay that is used to interact with an CheckpointPaceCondition and define the
 * checkpoints that the learner should reach
 * 
 * @author nroberts
 */
public class CheckpointPaceConditionOverlay extends PlacesOfInterestOverlay {
    
    /** The style name to apply to places of interest that can be selected for use with the condition */
    protected static final String DEFAULT_ITEM_STYLE = "defaultOverlayItem";

    /** The style name to apply to places of interest that cannot be selected for use with the condition */
    protected static final String EXCLUDED_ITEM_STYLE = "excludedOverlayItem";

    /** The style name to apply to places of interest that are available but not yet selected for use with the condition */
    protected static final String IGNORED_ITEM_STYLE = "ignoredOverlayItem";

    /** The comparator used to sort the list of places of interest for this condition type */
    private final Comparator<Serializable> CHECKPOINT_PACE_LIST_COMPARATOR = new Comparator<Serializable>() {

            @Override
            public int compare(Serializable o1, Serializable o2) {
                
                if(!(o2 instanceof Point)){
                    
                    //if the second entry does not represent a point, then the first entry should be sorted before the second
                    return -1;
                }
                
                boolean conditionReferences1 = doesConditionReferencePlace(o1);
                boolean conditionReferences2 = doesConditionReferencePlace(o2);
               
                if(conditionReferences1 && !conditionReferences2) {
                    
                    //if the first entry is referenced while the second isn't, then the first entry should be sorted before the second
                    return -1;
                    
                } else if(conditionReferences1 && conditionReferences2) {
                    
                    //if both points are referenced by this condition, sort them according to their order in the condition's list
                    String place1 = ScenarioClientUtility.getPlaceOfInterestName(o1);
                    String place2 = ScenarioClientUtility.getPlaceOfInterestName(o2);
                    
                    CheckpointPaceCondition input = (CheckpointPaceCondition) getCondition().getInput().getType();
                    for(Checkpoint checkpoint : input.getCheckpoint()) {
                        
                        //whichever point is found first will be sorted first
                        if(place1.equals(checkpoint.getPoint())) {
                            return -1;
                            
                        } else if(place2.equals(checkpoint.getPoint())) {
                            return 1;
                        }

                    }
                }
                
                return 1;
            }
        };

    /**
     * Creates a new overlay panel modifying the given condition and initializes the handlers for its controls
     * 
     * @param the condition to modify
     */
    public CheckpointPaceConditionOverlay(Condition condition) {
        
        super();
        
        if(condition == null) {
            throw new IllegalArgumentException("The condition to load into the overlay cannot be null.");
        }
        
        this.setCondition(condition);
        
        mainHeader.setText("Checkpoint Pace");
        
        setDrawPointInstructions("Click the location where you want to create a checkpoint for the learner to reach.");
        
        placesOfInterestList.setDraggable(true);
        placesOfInterestList.setItemFormatter(new ItemFormatter<AbstractPlaceOfInterestShape<?,?>>() {
            
            @Override
            public void format(AbstractPlaceOfInterestShape<?, ?> item, TableRowElement rowElement) {
                
                if(!(item.getPlaceOfInterest() instanceof Point)) {
                    
                    //non-points should be visually distinguished from points since they are always ignored
                    rowElement.removeClassName(DEFAULT_ITEM_STYLE);
                    rowElement.removeClassName(IGNORED_ITEM_STYLE);
                    rowElement.addClassName(EXCLUDED_ITEM_STYLE);
                    
                } else {
                    
                    rowElement.removeClassName(EXCLUDED_ITEM_STYLE);
                    
                    if(!doesConditionReferencePlace(item.getPlaceOfInterest())) {
                        
                        //places that aren't referenced by the condition should be visually distinguished
                        rowElement.removeClassName(DEFAULT_ITEM_STYLE);
                        rowElement.addClassName(IGNORED_ITEM_STYLE);
                        
                    } else {
                        rowElement.removeClassName(IGNORED_ITEM_STYLE);
                        rowElement.addClassName(DEFAULT_ITEM_STYLE);
                    }
                }
            }
        });
        
        placesOfInterestList.addListChangedCallback(new ListChangedCallback<AbstractPlaceOfInterestShape<?,?>>() {
            
            @Override
            public void listChanged(ListChangedEvent<AbstractPlaceOfInterestShape<?, ?>> event) {
                
                if(ListAction.REORDER.equals(event.getActionPerformed())){
                    
                    //determine which of the reordered places are in use by the condition and track the order of their names
                    final List<String> selectedPlaces = new ArrayList<>();
                    
                    for(AbstractPlaceOfInterestShape<?, ?> item : event.getAffectedItems()) {
                        
                        if(doesConditionReferencePlace(item.getPlaceOfInterest())) {
                            selectedPlaces.add(ScenarioClientUtility.getPlaceOfInterestName(item.getPlaceOfInterest()));
                        }
                    }
                    
                    CheckpointPaceCondition input = (CheckpointPaceCondition) getCondition().getInput().getType();
                    
                    //sort the collection's list of checkpoints so it matches the new name order
                    Collections.sort(input.getCheckpoint(), new Comparator<Checkpoint>() {

                        @Override
                        public int compare(Checkpoint o1, Checkpoint o2) {
                            return Integer.compare(selectedPlaces.indexOf(o1.getPoint()), selectedPlaces.indexOf(o2.getPoint()));
                        }
                        
                    });
                    
                    loadAndFilterPlacesOfInterest();
                }
            }
        });
    }
    
    @Override
    protected List<ItemField<AbstractPlaceOfInterestShape<?, ?>>> buildListFields() {
        
        List<ItemField<AbstractPlaceOfInterestShape<?, ?>>> fields = new ArrayList<>();
        
        fields.add(new ItemField<AbstractPlaceOfInterestShape<?,?>>() {
            
            @Override
            public Widget getViewWidget(final AbstractPlaceOfInterestShape<?, ?> item) {
                
                if(doesConditionReferencePlace(item.getPlaceOfInterest())) {
                    
                    //create a button that allows the author to remove this place's reference from the condition
                    Button button = new Button();
                    button.setText("Using");
                    button.setType(ButtonType.PRIMARY);
                    button.getElement().setAttribute("style", "padding: 3px 10px; border-radius: 20px; margin: 0px -8px;");
                    button.setEnabled(!isReadOnly);
                    
                    final Tooltip tooltip = new Tooltip(button, "Click to stop using this point");
                    tooltip.setContainer("body");
                    
                    button.addMouseDownHandler(new MouseDownHandler() {
                        
                        @Override
                        public void onMouseDown(MouseDownEvent event) {
                            event.stopPropagation();
                            
                            togglePlaceToReach(item.getPlaceOfInterest());
                            
                            loadAndFilterPlacesOfInterest();
                            
                            if(isLayersPanelVisible()) {
                                refreshLayers();
                            }
                            
                            tooltip.hide();
                        }
                    });
                    
                    button.addAttachHandler(new AttachEvent.Handler() {
                        
                        @Override
                        public void onAttachOrDetach(AttachEvent event) {
                            if(!event.isAttached()) {
                                tooltip.hide(); //hide the tooltip when the button is removed
                            }
                        }
                    });
                    
                    return button;
                    
                } else if(item instanceof PointOfInterestShape){
                    
                   //create a button that allows the author to add this place's reference from the condition
                    Button button = new Button();
                    button.setText("Available");
                    button.getElement().setAttribute("style", "padding: 3px 4px; border-radius: 20px; margin: 0px -8px;"
                            + "background-color: rgb(125,125,125); border: 1px solid rgb(100,100,100);");
                    button.setEnabled(!isReadOnly);
                    
                    final Tooltip tooltip = new Tooltip(button, "Click to use this point");
                    tooltip.setContainer("body");
                    
                    button.addMouseDownHandler(new MouseDownHandler() {
                        
                        @Override
                        public void onMouseDown(MouseDownEvent event) {
                            event.stopPropagation();
                            
                            togglePlaceToReach(item.getPlaceOfInterest());
                            
                            loadAndFilterPlacesOfInterest();
                            
                            if(isLayersPanelVisible()) {
                                refreshLayers();
                            }
                            
                            tooltip.hide();
                        }
                    }); 
                    
                    button.addAttachHandler(new AttachEvent.Handler() {
                        
                        @Override
                        public void onAttachOrDetach(AttachEvent event) {
                            if(!event.isAttached()) {
                                tooltip.hide(); //hide the tooltip when the button is removed
                            }
                        }
                    });
                    
                    return button;
                    
                } else {
                    
                    //return an empty HTML element, since we don't need a button here
                    return new HTML();
                }
            }
        });
        
        fields.add(new ItemField<AbstractPlaceOfInterestShape<?, ?>>() {
            @Override
            public Widget getViewWidget(AbstractPlaceOfInterestShape<?, ?> wrapper) {
                
                Serializable item = wrapper.getPlaceOfInterest();
                
                return PlacesOfInterestPanel.getPlaceIcon(item.getClass());
            }
        });
        
        //render the name of each place of interest
        fields.add(new ItemField<AbstractPlaceOfInterestShape<?, ?>>(null, "100%") {
            @Override
            public Widget getViewWidget(AbstractPlaceOfInterestShape<?, ?> wrapper) {
                
                Serializable item = wrapper.getPlaceOfInterest();

                String name = ScenarioClientUtility.getPlaceOfInterestName(item);
                
                InlineLabel label = new InlineLabel(name);
                label.setTitle(name);
                label.setWidth("150px");
                label.getElement().getStyle().setDisplay(Display.INLINE_BLOCK);
                label.getElement().getStyle().setWhiteSpace(WhiteSpace.NOWRAP);
                label.getElement().getStyle().setOverflow(Overflow.HIDDEN);
                label.getElement().getStyle().setTextOverflow(TextOverflow.ELLIPSIS);
                label.getElement().getStyle().setVerticalAlign(VerticalAlign.MIDDLE);
                
                if (!placesOfInterestMatchingTerms.isEmpty() && placesOfInterestMatchingTerms.contains(item)) {
                    label.getElement().getStyle().setFontWeight(FontWeight.BOLD);
                    label.getElement().getStyle().setTextDecoration(TextDecoration.UNDERLINE);
                }
                
                return label;
            }
        });
        
        //render the color of each place of interest
        fields.add(new ItemField<AbstractPlaceOfInterestShape<?, ?>>() {
            @Override
            public Widget getViewWidget(AbstractPlaceOfInterestShape<?, ?> wrapper) {
                
                final Serializable item = wrapper.getPlaceOfInterest();

                String color = ScenarioClientUtility.getPlaceOfInterestColor(item);
                if(color == null) {
                    color = "#000000";
                }
                
                ColorBox colorPicker = new ColorBox();
                colorPicker.addStyleName("colorDisplay");
                colorPicker.setValue(color);
                
                if(isReadOnly) {
                    colorPicker.getElement().getStyle().setProperty("pointer-events", "none");
                }
                
                final Tooltip tooltip = new Tooltip(colorPicker, "Change color");
                tooltip.setContainer("body");
                
                colorPicker.addValueChangeHandler(new ValueChangeHandler<String>() {
                    
                    @Override
                    public void onValueChange(ValueChangeEvent<String> event) {
                        
                        //update the item's underlying color when the author selects a new one.
                        String color = event.getValue();
                        
                        AbstractPlaceOfInterestShape<?, ?> poiShape = poiToShape.get(item);
                        if(poiShape != null) {
                            poiShape.getMapShape().setColor(color);
                            poiShape.getMapShape().draw();
                            poiShape.updatePlaceOfInterest();
                        }
                    }
                });
                
                colorPicker.addClickHandler(new ClickHandler() {
                    
                    @Override
                    public void onClick(ClickEvent event) {
                        event.stopPropagation(); //prevent list element from handling the click inadvertently
                        tooltip.hide();
                    }
                });
                
                colorPicker.addMouseDownHandler(new MouseDownHandler() {
                    
                    @Override
                    public void onMouseDown(MouseDownEvent event) {
                        event.stopPropagation(); //prevent list element from handling the click inadvertently
                        tooltip.hide();
                    }
                });
                
                return colorPicker;
            }
        });

        return fields;
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
    
    /**
     * Toggles whether or not the learner should reach the given place of interest. 
     * <br/><br/>
     * If the place of interest is referenced by the condition as a place the learner should reach, this method 
     * will remove the reference so that the place will be ignored by the condition.
     * <br/><br/>
     * If the place of interest is NOT referenced by the condition, this method will add reference so that it is not 
     * ignored by the condition, meaning that the learner should reach it.
     * 
     * @param placeOfInterest
     */
    private void togglePlaceToReach(Serializable placeOfInterest) {
        
        String placeName = ScenarioClientUtility.getPlaceOfInterestName(placeOfInterest);
        CheckpointPaceCondition input = (CheckpointPaceCondition) getCondition().getInput().getType();
        
        boolean placeRefRemoved = false;
        
        Iterator<Checkpoint> checkpointItr = input.getCheckpoint().iterator();
        while(checkpointItr.hasNext()) {
            
            Checkpoint ref = checkpointItr.next();
            
            if(placeName.equals(ref.getPoint())) {
                
                //a reference to the place was found, so remove it
                placeRefRemoved = true;
                checkpointItr.remove();
            }
        }
        
        if(!placeRefRemoved) {
            
            //no references were found to this place, so add a new reference
            if(placeOfInterest instanceof Point) {
                
                Checkpoint ref = new Checkpoint();
                ref.setPoint(placeName);
                ref.setAtTime("00:00:01");
                ref.setWindowOfTime(BigDecimal.valueOf(0.0));
                
                input.getCheckpoint().add(ref);
                
            }
        }
        
        //a reference was either added or removed, so update the global map of references
        ScenarioClientUtility.gatherPlacesOfInterestReferences();
    }
    
    @Override
    protected List<Class<?>> getPreferredTypes() {
        return Arrays.asList(new Class<?>[] {Point.class});
    }
    
    @Override
    protected void onPlaceOfInterestAdded(Serializable placeOfInterest) {
        
        if(placeOfInterest instanceof Point) {
            
            //automatically reach the new place of interest
            togglePlaceToReach(placeOfInterest);
        }
    }
    
    @Override
    protected Comparator<Serializable> getListSortComparator() {
        return CHECKPOINT_PACE_LIST_COMPARATOR;
    }
}
