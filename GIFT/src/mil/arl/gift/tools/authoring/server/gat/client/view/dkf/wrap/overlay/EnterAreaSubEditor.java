/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.wrap.overlay;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.WhiteSpace;
import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.EnterAreaCondition;
import generated.dkf.Entrance;
import generated.dkf.Inside;
import generated.dkf.Outside;
import generated.dkf.Point;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.gwt.client.validation.ValidationWidget;
import mil.arl.gift.common.util.StringUtils.Stringifier;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.wrap.WrapPanel;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemField;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemFormatter;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemListEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemListEditor.ListChangedCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ListChangedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ListChangedEvent.ListAction;
import mil.arl.gift.tools.map.client.draw.PointShape;
import mil.arl.gift.tools.map.client.draw.PolylineShape;

/**
 * A sub-editor used to modify an EnterAreaCondition's list of entrances in the wrap overlay
 * 
 * @author nroberts
 */
public abstract class EnterAreaSubEditor extends Composite {

    private static EnterAreaSubEditorUiBinder uiBinder = GWT.create(EnterAreaSubEditorUiBinder.class);

    interface EnterAreaSubEditorUiBinder extends UiBinder<Widget, EnterAreaSubEditor> {
    }

    /** The entrance currently being edited*/
    protected EntranceShape entranceBeingEdited;
    
    /** The editor used to modify individual entrances in the list*/
    private EntranceItemEditor entranceItemEditor = new EntranceItemEditor() {
        
        @Override
        protected void populateEditor(final EntranceShape wrapper) {
            super.populateEditor(wrapper);
            
            if(entranceList.isEditing()) {
                
                setPoiEditingEnabled(false);
                
                entranceBeingEdited = wrapper;
                
                //center the map on the shape being edited
                WrapPanel.getInstance().getCurrentMap().centerView(false, wrapper.getMapShape());
            }
        }
        
        @Override
        protected void onCancel() {
            
            setPoiEditingEnabled(true);
            
            if(entranceBeingEdited != null) {
                
                if(!input.getEntrance().contains(entranceBeingEdited.getEntrance())) {
                    
                    //if the edited entrance is a temporary one created by the author ,
                    //remove it and its shape since its creation was cancelled
                    entranceBeingEdited.getMapShape().erase();
                    entranceToShape.remove(entranceBeingEdited.getEntrance());
                    
                } else {
                    
                    entranceBeingEdited.updateMapShape(); //revert the shape to its original appearance
                    entranceBeingEdited.getMapShape().draw();
                }
            }
            
            entranceBeingEdited = null;
            
            super.onCancel();
        }
    };
    
    @UiField(provided=true)
    protected ItemListEditor<EntranceShape> entranceList = new ItemListEditor<>(entranceItemEditor);
    
    /** The condition input containing the list of entrances*/
    private EnterAreaCondition input;
    
    /** A mapping from each entrance to the shape used to represent it on the map */
    private Map<Entrance, EntranceShape> entranceToShape = new HashMap<>();

    /** Whether or not the author should be able to edit entrances */
    private boolean entranceEditingEnabled = true;

    /** Whether or not this editor is read-only */
    private boolean isReadOnly;

    /**
     * Creates a sub-editor modifying the given input for an EnterAreaCondition and sets up its event handlers
     * 
     * @param input the condition input to modify
     */
    public EnterAreaSubEditor(EnterAreaCondition input) {
        initWidget(uiBinder.createAndBindUi(this));
        
        if(input == null) {
            throw new IllegalArgumentException("The input to modify cannot be null");
        }
        
        this.input = input;
        
        entranceList.setPlaceholder("To create an entrance, click two existing points on the map that define where the learner should "
                + "enter and exit from.");
        entranceList.setRemoveItemDialogTitle("Delete Entrance?");
        entranceList.setRemoveItemStringifier(new Stringifier<EntranceShape>() {

            @Override
            public String stringify(EntranceShape item) {
                
                String name = item.getEntrance().getName();

                StringBuilder builder = new StringBuilder().append("<b>")
                        .append((name != null) ? name : "this entrance")
                        .append("</b>");

                return builder.toString();
            }
        });

        entranceList.setFields(buildListFields());

        entranceList.addListChangedCallback(new ListChangedCallback<EntranceShape>() {

            @Override
            public void listChanged(ListChangedEvent<EntranceShape> event) {

                if (ListAction.ADD.equals(event.getActionPerformed())) {

                    EntranceShape edited = event.getAffectedItems().get(0);

                    if (!EnterAreaSubEditor.this.input.getEntrance().contains(edited.getEntrance())) {

                        //if a new entrance item is added, we need to update the schema objects accordingly
                        EnterAreaSubEditor.this.input.getEntrance().add(edited.getEntrance());
                        
                        //this will likely change some place of interest references, so notify listeners accordingly
                        ScenarioClientUtility.gatherPlacesOfInterestReferences();
                        refreshPlaceOfInterestReferences();
                    }
                    
                } else if (ListAction.REMOVE.equals(event.getActionPerformed())) {

                    EntranceShape edited = event.getAffectedItems().get(0);

                    //if an entrance item is removed, we need to update the schema objects accordingly
                    EnterAreaSubEditor.this.input.getEntrance().remove(edited.getEntrance());
                    
                   //this will likely change some place of interest references, so notify listeners accordingly
                    ScenarioClientUtility.gatherPlacesOfInterestReferences();
                    refreshPlaceOfInterestReferences();
                }
                
                setPoiEditingEnabled(true);
                
                redrawEntranceList();
            }
        });
        
        entranceList.setItemFormatter(new ItemFormatter<EntranceShape>() {
            
            @Override
            public void format(EntranceShape item, TableRowElement rowElement) {
                
                if(!isReadOnly && !entranceEditingEnabled) {
                    rowElement.getStyle().setProperty("pointer-events", "none");
                    
                } else {
                    rowElement.getStyle().clearProperty("pointer-events");
                }
            }
        });
        
        //need to allow the list of entrances to validate, even though we don't show validation errors here
        entranceList.initValidationComposite(new ValidationWidget(entranceList));
    }
    
    /**
     * Builds the fields for the list of places of interest
     * 
     * @return the list of fields
     */
    protected List<ItemField<EntranceShape>> buildListFields() {
        
        List<ItemField<EntranceShape>> fields = new ArrayList<>();
        
        //render the name of each place of interest
        fields.add(new ItemField<EntranceShape>(null, "100%") {
            @Override
            public Widget getViewWidget(EntranceShape item) {
                
                FlowPanel panel = new FlowPanel();
                panel.getElement().getStyle().setPadding(1, Unit.PX);

                String outsideName = item.getEntrance().getOutside() != null 
                        ? item.getEntrance().getOutside().getPoint()
                        : "UNKNOWN";
                        
                String insideName = item.getEntrance().getInside() != null 
                        ? item.getEntrance().getInside().getPoint()
                        : "UNKNOWN";
                        
                SafeHtmlBuilder sb = new SafeHtmlBuilder();
                
                if(item.getEntrance().getName() != null) {
                    sb.appendHtmlConstant("<b>")
                    .appendEscaped(item.getEntrance().getName())
                    .appendHtmlConstant("</b><br/>");
                }
                        
                sb.appendHtmlConstant("<span style='font-style: italic;'>")
                .appendEscaped(outsideName)
                .appendHtmlConstant(" <i class='fa fa-long-arrow-right'></i> ")
                .appendEscaped(insideName)
                .appendHtmlConstant("</span>");
                
                InlineHTML label = new InlineHTML();
                label.setHTML(sb.toSafeHtml());
                
                panel.add(label);
                panel.getElement().getStyle().setWhiteSpace(WhiteSpace.NOWRAP);
                
                return panel;
            }
        });
        
        return fields;
    }

    /**
     * Loads the list of entrances from the underlying condition input and updates the shapes on the map accordingly
     */
    public void redrawEntranceList() {
        
        List<EntranceShape> entranceShapes = new ArrayList<>();
        
        Map<Entrance, EntranceShape> entranceToShapeToRemove = new HashMap<>();
        entranceToShapeToRemove.putAll(entranceToShape);
        entranceToShape.clear();
            
        for(Entrance entrance : input.getEntrance()) {
            
            EntranceShape shape = entranceToShapeToRemove.get(entrance);
            
            if(shape == null) {
                shape = new EntranceShape(entrance);
                
            } else {
                shape.updateMapShape();
            }
            
            final EntranceShape finalShape = shape;
            finalShape.getMapShape().setClickCommand(new Command() {
                
                @Override
                public void execute() {
                    
                    if(entranceList.isEditing()) {
                        entranceList.cancelEditing(); //cancel editing so we can start editing this shape
                    }
                    
                    entranceList.editExisting(finalShape);
                }
            });
            
            if(arePointsVisible(shape.getOutsidePoint(), shape.getInsidePoint())) {
                shape.getMapShape().draw(); 
                
            } else {
                shape.getMapShape().erase();
            }
            
            entranceToShape.put(entrance, shape);
            entranceShapes.add(shape);
        }
        
        for(Entrance entrance : entranceToShapeToRemove.keySet()) {
            
            if(!entranceToShape.containsKey(entrance)) {
                entranceToShapeToRemove.get(entrance).getMapShape().erase();
            }
        }
        
        entranceList.setItems(entranceShapes);
    }

    /**
     * Creates a new entrance using the given start and end points, draws it on the map, and begins editing it
     * 
     * @param startPoint the new entrance's start point
     * @param endPoint the new entrance's end point
     */
    public void startEditingNewEntrance(Point startPoint, Point endPoint) {
        
        Entrance newEntrance = new Entrance();
        
        Outside outside = new Outside();
        outside.setPoint(startPoint.getName());
        outside.setProximity(BigDecimal.ONE);
        newEntrance.setOutside(outside);
        
        Inside inside = new Inside();
        inside.setPoint(endPoint.getName());
        inside.setProximity(BigDecimal.ONE);
        newEntrance.setInside(inside);
        
        EntranceShape shape = new EntranceShape(newEntrance);
        entranceToShape.put(newEntrance, shape);
        shape.getMapShape().draw();
        
        newEntrance.setAssessment(AssessmentLevelEnum.AT_EXPECTATION.getName());
        
        entranceList.editNewElement(shape);
    }
    
    /**
     * Sets whether or not the author should be allowed to edit entrances
     * 
     * @param enabled whether the author can edit entrances
     */
    public void setEntranceEditingEnabled(boolean enabled) {
        this.entranceEditingEnabled = enabled;
        entranceList.setReadonly(!enabled);
    }
    
    /**
     * Cleans up the map by removing any entrances that were rendered by this editor
     */
    public void cleanUpMap() {
        
        //erase all entrance shapes that were drawn on the map
        for(EntranceShape entranceShape : entranceToShape.values()) {
            entranceShape.getMapShape().erase();
        }
    }
    
    /**
     * Refreshes the list of places of interest in the overlay to account for reference changes made by this editor
     */
    abstract public void refreshPlaceOfInterestReferences();
    
    /**
     * Sets whether or not the author can edit places of interest
     * 
     * @param enabled whether the author can edit places of interest
     */
    abstract public void setPoiEditingEnabled(boolean enabled);
    
    /**
     * Gets whether or not both of the given points are visible
     * 
     * @param start the start point of an entrance
     * @param end the end point of an entrance
     * @return whether both points are visible
     */
    abstract public boolean arePointsVisible(Point start, Point end);

    /**
     * Updates the shapes of entrances referencing the given point so that they match the state of the given point shape
     * 
     * @param point the point whose referencing entrances should be updated
     * @param shape the point shape reflecting the point's currently rendered state
     */
    public void updateEntranceShapes(Point point, PointShape<?> shape) {
        
        if(point != null && point.getName() != null) {
            
            for(Entrance entrance : entranceToShape.keySet()) {
                
                if(entrance.getOutside() != null && point.getName().equals(entrance.getOutside().getPoint())) {
                    
                    PolylineShape<?> entranceMapShape = entranceToShape.get(entrance).getMapShape();
                    entranceMapShape.setColor(shape.getColor());
                    
                    //the outside point matches the reference, so update the entrance line's first vertex
                    entranceMapShape.getVertices().set(0, shape.getLocation());
                    
                    entranceMapShape.draw();
                    
                } else if(entrance.getInside() != null && point.getName().equals(entrance.getInside().getPoint())) {
                    
                    PolylineShape<?> entranceMapShape = entranceToShape.get(entrance).getMapShape();
                    
                    //the inside point matches the reference, so update the entrance line's last vertex
                    entranceMapShape.getVertices().set(1, shape.getLocation());
                    
                    entranceMapShape.draw();
                }
            }
        }
    }

    /**
     * Sets whether or not this widget is read-only
     * 
     * @param isReadOnly whether this widget is read-only
     */
    public void setReadOnly(boolean isReadOnly) {
        
        this.isReadOnly = isReadOnly;
        
        entranceList.setReadonly(isReadOnly);
    }
}
