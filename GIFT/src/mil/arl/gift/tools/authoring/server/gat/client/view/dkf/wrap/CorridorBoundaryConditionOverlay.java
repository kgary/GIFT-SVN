/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.wrap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
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
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.Area;
import generated.dkf.Condition;
import generated.dkf.CorridorBoundaryCondition;
import generated.dkf.Path;
import generated.dkf.PathRef;
import generated.dkf.Point;
import mil.arl.gift.common.gwt.client.widgets.ColorBox;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.header.editor.PlaceOfInterestConditionReference;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.header.editor.PlaceOfInterestReference;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.header.editor.PlacesOfInterestPanel;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.wrap.overlay.AbstractPlaceOfInterestShape;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.wrap.overlay.PathOfInterestShape;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemField;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemFormatter;

/**
 * An extension of the default places of interest overlay that is used to interact with an CorridorBoundaryCondition and define the
 * places that the learner should follow
 * 
 * @author nroberts
 */
public class CorridorBoundaryConditionOverlay extends PlacesOfInterestOverlay {

    /** The style name to apply to places of interest that cannot be selected for use with the condition */
    protected static final String EXCLUDED_ITEM_STYLE = "excludedOverlayItem";

    /** The style name to apply to places of interest that are available but not yet selected for use with the condition */
    protected static final String IGNORED_ITEM_STYLE = "ignoredOverlayItem";

    /** The comparator used to sort the list of places of interest for this condition type */
    private final Comparator<Serializable> CORRIDOR_BOUNDARY_LIST_COMPARATOR = new Comparator<Serializable>() {

            @Override
            public int compare(Serializable o1, Serializable o2) {
               
                if(!(o2 instanceof Path)
                        || (doesConditionReferencePlace(o1) && !doesConditionReferencePlace(o2))) {
                    
                    //if the second entry doesn't represent a path or if the first entry is referenced while the 
                    //second isn't, then the first entry should be sorted before the second
                    return -1;
                } 
                
                return 1;
            }
        };

    /**
     * Creates a new overlay panel modifying the given condition and initializes the handlers for its controls
     * 
     * @param the condition to modify
     */
    public CorridorBoundaryConditionOverlay(Condition condition) {
        
        super();
        
        if(condition == null) {
            throw new IllegalArgumentException("The condition to load into the overlay cannot be null.");
        }
        
        this.setCondition(condition);
        
        mainHeader.setText("Follow Path");
        
        setDrawPathInstructions("Click at least 2 locations defining the corridor that the learner should follow."
                + "<br/><br/>To complete the path, double-click its last location.");
        
        placesOfInterestList.setItemFormatter(new ItemFormatter<AbstractPlaceOfInterestShape<?,?>>() {
            
            @Override
            public void format(AbstractPlaceOfInterestShape<?, ?> item, TableRowElement rowElement) {
                
                if(item.getPlaceOfInterest() instanceof Point || item.getPlaceOfInterest() instanceof Area) {
                    
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
                    
                    final Tooltip tooltip = new Tooltip(button, "Click to stop using this place of interest");
                    tooltip.setContainer("body");
                    
                    button.addMouseDownHandler(new MouseDownHandler() {
                        
                        @Override
                        public void onMouseDown(MouseDownEvent event) {
                            event.stopPropagation();
                            
                            togglePlaceToFollow(item.getPlaceOfInterest());
                            
                            loadAndFilterPlacesOfInterest();
                            
                            if(isLayersPanelVisible()) {
                                refreshLayers();
                            }
                            
                            tooltip.hide();
                        }
                    });
                    
                    return button;
                    
                } else if(item instanceof PathOfInterestShape){
                    
                   //create a button that allows the author to add this place's reference from the condition
                    Button button = new Button();
                    button.setText("Available");
                    button.getElement().setAttribute("style", "padding: 3px 4px; border-radius: 20px; margin: 0px -8px;"
                            + "background-color: rgb(125,125,125); border: 1px solid rgb(100,100,100);");
                    button.setEnabled(!isReadOnly);
                    
                    final Tooltip tooltip = new Tooltip(button, "Click to use this place of interest");
                    tooltip.setContainer("body");
                    
                    button.addMouseDownHandler(new MouseDownHandler() {
                        
                        @Override
                        public void onMouseDown(MouseDownEvent event) {
                            event.stopPropagation();
                            
                            togglePlaceToFollow(item.getPlaceOfInterest());
                            
                            loadAndFilterPlacesOfInterest();
                            
                            if(isLayersPanelVisible()) {
                                refreshLayers();
                            }
                            
                            tooltip.hide();
                        }
                    }); 
                    
                    return button;
                    
                } else {
                    
                    //return an empty HTML element, since we don't need a button here
                    return new HTML();
                }
            }
        });
        
        //render the name of each place of interest
        fields.add(new ItemField<AbstractPlaceOfInterestShape<?, ?>>(null, "100%") {
            @Override
            public Widget getViewWidget(AbstractPlaceOfInterestShape<?, ?> wrapper) {
                
                FlowPanel panel = new FlowPanel();
                
                Serializable item = wrapper.getPlaceOfInterest();
                
                Widget iconWidget = PlacesOfInterestPanel.getPlaceIcon(item.getClass());
                
                if(iconWidget != null) {
                    panel.add(iconWidget);
                }

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
                
                panel.add(label);
                panel.getElement().getStyle().setWhiteSpace(WhiteSpace.NOWRAP);
                
                return panel;
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
     * Toggles whether or not the learner should follow the given place of interest. 
     * <br/><br/>
     * If the place of interest is referenced by the condition as a place the learner should follow, this method 
     * will remove the reference so that the place will be ignored by the condition.
     * <br/><br/>
     * If the place of interest is NOT referenced by the condition, this method will add reference so that it is not 
     * ignored by the condition, meaning that the learner should follow it.
     * 
     * @param placeOfInterest
     */
    private void togglePlaceToFollow(Serializable placeOfInterest) {
        
        String placeName = ScenarioClientUtility.getPlaceOfInterestName(placeOfInterest);
        CorridorBoundaryCondition input = (CorridorBoundaryCondition) getCondition().getInput().getType();
        
        if(input.getPathRef() == null) {
            input.setPathRef(new PathRef());
        }
        
        if(!placeName.equals(input.getPathRef().getValue())) {
            input.getPathRef().setValue(placeName);
            
        } else {
            input.getPathRef().setValue(null);
        }
        
        //a reference was either added or removed, so update the global map of references
        ScenarioClientUtility.gatherPlacesOfInterestReferences();
    }
    
    @Override
    protected List<Class<?>> getPreferredTypes() {
        return Arrays.asList(new Class<?>[] {Path.class});
    }
    
    @Override
    protected void onPlaceOfInterestAdded(Serializable placeOfInterest) {
        
        if(placeOfInterest instanceof Path) {
            
            //automatically follow the new place of interest
            togglePlaceToFollow(placeOfInterest);
        }
    }
    
    @Override
    protected Comparator<Serializable> getListSortComparator() {
        return CORRIDOR_BOUNDARY_LIST_COMPARATOR;
    }
}
