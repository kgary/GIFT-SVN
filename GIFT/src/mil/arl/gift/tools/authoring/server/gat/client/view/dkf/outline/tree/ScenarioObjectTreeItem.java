/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.outline.tree;

import java.io.Serializable;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.extras.notify.client.constants.NotifyType;
import org.gwtbootstrap3.extras.notify.client.ui.Notify;

import com.github.gwtd3.api.Coords;
import com.github.gwtd3.api.D3;
import com.github.gwtd3.api.arrays.Array;
import com.github.gwtd3.api.behaviour.Drag.DragEventType;
import com.github.gwtd3.api.core.Value;
import com.github.gwtd3.api.functions.DatumFunction;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.web.bindery.event.shared.binder.EventBinder;

import generated.dkf.Actions.StateTransitions.StateTransition;
import generated.dkf.Concept;
import generated.dkf.Condition;
import generated.dkf.Strategy;
import generated.dkf.Task;
import mil.arl.gift.common.gwt.client.util.ScenarioElementUtil;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.DragIcon;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.EditableInlineLabel;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.RenameScenarioObjectEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;

/**
 * A generic tree item that represents a scenario object in the scenario outline and allows the learner
 * to interact with it
 * 
 * @author nroberts
 * @param <T> the type of scenario object that this item represents
 */
public class ScenarioObjectTreeItem<T extends Serializable> extends TreeItem{

    /** The logger for the class */
    private static Logger logger = Logger.getLogger(ScenarioObjectTreeItem.class.getName());
    
    /** Interface for handling events. */
    /* The @SuppressWarnings exists so that we can specify the type
     * 'ScenarioObjectTreeItem' without providing a generic type. This was
     * causing a compilation issue. Do not remove @SuppressWarnings or specify a
     * type for ScenarioObjectTreeItem */
    @SuppressWarnings("rawtypes")
    interface WidgetEventBinder extends EventBinder<ScenarioObjectTreeItem> {
    }
    
    /** Create the instance of the event binder (binds the widget for events. */
    private static final WidgetEventBinder eventBinder = GWT.create(WidgetEventBinder.class);
    
    /** The tree item currently being dragged, if the user is currently dragging an item */
    private static ScenarioObjectTreeItem<? extends Serializable> itemBeingDragged = null;

    /** The scenario object that this tree item represents */
    private T object;

    /** The panel containing the tree item components */
    protected FlowPanel containerPanel = new FlowPanel();
    
    /** The icon to be displayed in the tree item */
    private Icon typeIcon = new Icon();
    
    /** The name label */
    final protected EditableInlineLabel nameLabel = new EditableInlineLabel();
    
    /** The panel containing the tree item action buttons */
    private FlowPanel buttonPanel = new FlowPanel();
    
    /** A mapping of the buttons added to the tooltips created for them */
    protected HashMap<Icon, Tooltip> buttonToTooltip = new HashMap<Icon, Tooltip>();
    
    /**
     * Creates an undraggable tree item representing the given object
     * 
     * @param object the object that the tree item should represent. Can't be null.
     */
    protected ScenarioObjectTreeItem(T object){
        this(object, false);
    }
    
    /**
     * Creates a tree item representing the given object
     * 
     * @param object the object that the tree item should represent. Can't be null.
     * @param draggable whether or not the tree item should be draggable
     */
    protected ScenarioObjectTreeItem(T object, final boolean draggable){
        
        if (object == null) {
            throw new IllegalArgumentException("The parameter 'object' cannot be null.");
        }

        eventBinder.bindEventHandlers(ScenarioObjectTreeItem.this, SharedResources.getInstance().getEventBus());

        this.object = object;

        // remove any vertical padding around this item, since this creates gaps when dragging items
        getElement().getStyle().setPaddingTop(0, Unit.PX);
        getElement().getStyle().setPaddingBottom(0, Unit.PX);
        getElement().getStyle().setPaddingLeft(16, Unit.PX);

        if (draggable) {

            // add an icon to indicate that scenario tree items can be dragged
            DragIcon dragIcon = new DragIcon();
            dragIcon.getElement().getStyle().setMarginRight(5, Unit.PX);
            dragIcon.getElement().setTitle("Drag to reorder");

            containerPanel.add(dragIcon);
        }

        /* use an icon to indicate this object's type. Also performs validation on this object and
         * adds/updates the cache if it's not clean */
        updateIcon();
        containerPanel.add(typeIcon);
        typeIcon.addStyleName("scenarioTreeItemIcon");

        // use an editable label to display and modify this object's name
        nameLabel.setValue(ScenarioElementUtil.getObjectName(object));
        nameLabel.addStyleName("scenarioTreeItemName");
        nameLabel.setTooltipContainer("body");  //make sure the tooltip isn't hidden behind the tabs
        nameLabel.setEditingEnabled(!ScenarioClientUtility.isReadOnly());

        nameLabel.addDomHandler(new MouseDownHandler() {
            @Override
            public void onMouseDown(MouseDownEvent event) {
                if (isSelected()) {

                    /* if this tree item is already selected, avoid selecting it again whenever the
                     * user clicks its text, since that interferes with the renaming box */
                    event.stopPropagation();
                }
            }
        }, MouseDownEvent.getType());
        
        nameLabel.addDomHandler(new KeyDownHandler() {
            
            @Override
            public void onKeyDown(KeyDownEvent event) {
                
                if(isSelected()) {
                    
                    //prevent arrow keys from changing the selection in the outline when editing names
                    event.stopPropagation();
                }
            }
        }, KeyDownEvent.getType());
        
        /* IE 11 was causing problems with the click event because it would persist to the tree and
         * lose focus from the name label (therefore immediately triggering the value change event
         * with no changes). We need to stop halt the event here. */
        nameLabel.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (isSelected()) {
                    event.stopPropagation();
                }
            }
        });
        
      nameLabel.addValueChangeHandler(new ValueChangeHandler<String>() {

            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
            	String newName = event.getValue().trim();
            	setNameIfAllowed(newName);
            }
      }); 
      
        containerPanel.add(nameLabel);
        
        //add the panel where additional buttons will get added
        buttonPanel.getElement().getStyle().setDisplay(Display.INLINE_BLOCK);
        containerPanel.add(buttonPanel);
        
        setWidget(containerPanel);
        
        if(draggable) {
            
            //use D3's drag behavior to allow scenario tree items to be dragged (similar to nodes in the course tree)
            D3.select(getElement())
            .data(Array.fromJavaArray(new Coords[]{Coords.create(0, 0)})) //define data containing the drag origin (i.e. top left coordinates)
            .call(D3.behavior().drag()
                .origin(new DatumFunction<Coords>() {
                
                    @Override
                    public Coords apply(Element context, Value d, int index) {
                        
                        //get the drag origin
                        return d.asCoords();
                    }
                })
                .on(DragEventType.DRAG, new DatumFunction<Void>() {
                    
                    @Override
                    public Void apply(final Element context, Value d, int index) {
                            
                        Coords coord = d.as();
                        
                        //get the mouse coordinates relative to the drag origin (i.e. the top left corner of the item being dragged)
                        coord.x(D3.eventAsCoords().x());
                        coord.y(D3.eventAsCoords().y());
                        
                        if(itemBeingDragged == null) {
                            
                            //start dragging a new item
                            itemBeingDragged = ScenarioObjectTreeItem.this;
                        }
                        
                        if(Math.abs(coord.x()) > 2 || Math.abs(coord.y()) > 2) {
                            
                            //start dragging once the mouse moves a certain amount
                            if(StringUtils.isBlank(context.getStyle().getProperty("transform"))) {
                                
                                //apply the drag styling the first time this item's position is changed
                                containerPanel.getElement().addClassName("scenarioTreeItemDrag");
                            }
                            
                            containerPanel.getElement().getStyle().setProperty("transform", "translate(" + ((int) coord.x()) + "px ," + ((int) coord.y()) + "px)");
                        }
                        
                        return null;
                    }
                }).on(DragEventType.DRAGEND, new DatumFunction<Void>() {
                    
                    @Override
                    public Void apply(Element context, Value d, int index) {
                        
                        //reset the item's coordinate data
                        Coords coord = d.as();
                        
                        coord.x(0);
                        coord.y(0);
                        
                        //stop dragging the item
                        itemBeingDragged = null;
                        containerPanel.getElement().removeClassName("scenarioTreeItemDrag");
                        containerPanel.getElement().getStyle().clearProperty("transform");
                        
                        return null;
                    }
                })
            ).on("mousedown", new DatumFunction<Void>() {
                
                @Override
                public Void apply(Element context, Value d, int index) {
                    
                    //need to stop propagation so we don't start dragging the parent item too
                    D3.event().stopPropagation();
                    
                    /* 
                     * Stopping propagation, however, also causes the expand/collapse icon to no longer respond to mouse events, since its
                     * event listener is actually further up the DOM structure. Because of this, we need to detect when the <img> element
                     * containing the expand/collapse icon is clicked on and perform the same expanding and collapsing behavior.
                     * 
                     * Note that adding more <img> elements to scenario object tree items in the future may break this logic.
                     */
                    EventTarget target = D3.event().getEventTarget();
                    
                    if(Element.is(target) && "img".equals(((Element) target.cast()).getTagName().toLowerCase())) {
                        setState(!getState(), true);
                    }
                    
                    return null;
                }
            }).on("click", new DatumFunction<Void>() {
                
                @Override
                public Void apply(Element context, Value d, int index) {
                    
                    D3.event().stopPropagation();
                    
                    //select this item on click
                    getTree().setSelectedItem(ScenarioObjectTreeItem.this, true);
                    
                    return null;
                }
            }).on("mouseover", new DatumFunction<Void>() {
                
                @Override
                public Void apply(Element context, Value d, int index) {
                    
                    D3.event().stopPropagation();
                    
                    if(itemBeingDragged != null) {
                        
                        getElement().addClassName("scenarioTreeItemDrop");
                        
                        if(shouldDropBelow(itemBeingDragged)) {
                            
                            if(getChildCount() > 0) {
                                getElement().addClassName("scenarioTreeItemDropBelow");
                                
                            } else {
                                getElement().addClassName("scenarioTreeItemDropBelowNoChildren");
                            }
                            
                        } else {
                            getElement().addClassName("scenarioTreeItemDropOver");
                        }
                        
                        if(!allowDrop(itemBeingDragged)){
                            getElement().addClassName("scenarioTreeItemNoDrop");
                        }
                    }
                    
                    return null;
                }
            }).on("mouseout", new DatumFunction<Void>() {
                
                @Override
                public Void apply(Element context, Value d, int index) {
                    
                    D3.event().stopPropagation();
                    
                    if(itemBeingDragged != null) {
                        
                        //remove all classes that add drop styling
                        getElement().removeAttribute("class");
                    }
                    
                    return null;
                }
            }).on("mouseup", new DatumFunction<Void>() {
                
                @Override
                public Void apply(Element context, Value d, int index) {
                    
                    if(itemBeingDragged != null) {
                        
                        //remove all classes that add drop styling
                        getElement().removeAttribute("class");
                        
                        onDrop(itemBeingDragged);
                        
                        //need to ensure that the scenario outline doesn't handle dropping this item
                        itemBeingDragged = null;
                    }
                    
                    return null;
                }
            });
        }
    }
    
    /**
     * Gets the scenario object that this tree item represents. Can't be null.
     * 
     * @return the scenario object that this tree item represents
     */
    public T getScenarioObject() {
        return object;
    }
    
    /**
     * Gets the label used to display and edit this object's name
     * 
     * @return the label used to display and edit this object's name
     */
    public EditableInlineLabel getNameLabel() {
        return nameLabel;
    }
    
    /**
     * Creates an icon button that uses the given icon type, displays the given tooltip text, and optionally handles click events 
     * using the given handler and then adds that button to this tree item.
     * 
     * @param buttonIcon the type of icon the button should use
     * @param tooltipText the text that this button's tooltip should use
     * @param clickHandler an optional click handler that can be used to listen for click events on this button
     * @param showOnReadOnly true if the button should be visible even if the user is in read-only mode; false otherwise.
     * @return the created button
     */
    public Icon addButton(IconType buttonIcon, String tooltipText, ClickHandler clickHandler, boolean showOnReadOnly) {
        
        Icon button = new Icon(buttonIcon);
        button.addStyleName("scenarioTreeItemButton");
        
        if(clickHandler != null) {
            button.addClickHandler(clickHandler);
        }
        
        Tooltip addConceptTooltip = new Tooltip(button);
        addConceptTooltip.setTitle(tooltipText);
        addConceptTooltip.setContainer("body"); //prevents cutoff from scroll area
        
        buttonToTooltip.put(button, addConceptTooltip);
        buttonPanel.add(addConceptTooltip);
        
        if (!showOnReadOnly && ScenarioClientUtility.isReadOnly()) {
            button.setVisible(false);
        }
        
        /* If source path contains an imported file and the button does not add conditions, set the button to non-visible. */
        if (ScenarioClientUtility.getScenario().getResources().getSourcePath() != null && !(object instanceof Condition || object instanceof Strategy)) {
        	button.setVisible(false);
        }
        
        return button;
    }
    
    /**
     * Updates the tooltip text for a button
     * 
     * @param button the button for which the tooltip's text should be updated
     * @param tooltipText the text the tooltip should be updated to
     */
    public void updateButtonTooltip(Icon button, String tooltipText) {
        Tooltip tooltip = buttonToTooltip.get(button);
        if (tooltip != null) {
            tooltip.setTitle(tooltipText);
        } else {
            logger.warning("Failed to updated tooltip text because could not find tooltip for the specified button.");
        }
    }
    
    /**
     * Checks if the scenario object being edited can be changed to the given name, and if it can,
     * updates the scenario object to use the new name. If changing the name would have potentially
     * unwanted side effects, the user may be prompted to confirm or cancel the name change. If the
     * name change is prevented, then the UI elements used to change the name will be reverted to
     * show the existing name and an informative message may be shown to the user.
     *
     * @param newName the new name to give the scenario object being edited. If null, the name
     * change will be cancelled and a message will be displayed to the user.
     */
    public void setNameIfAllowed(String newName) {

            
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("onValueChangeHandler");
            }
            
           Serializable scenarioObject = getScenarioObject();
            
             String name = ScenarioClientUtility.getScenarioObjectName(scenarioObject);
            
            if (StringUtils.equals(name, newName)) {
                // No change
                return;
            } else if(StringUtils.isBlank(newName)) {
                
                Notify.notify("The name must contain at least one visible character.", NotifyType.DANGER);
                nameLabel.setValue(name);
                
                return;
            } else if (!ScenarioClientUtility.isScenarioObjectNameValid(scenarioObject, newName)) {

                String warning = "";
                if(scenarioObject instanceof Task || scenarioObject instanceof Concept){
                    warning = "Another Task or Concept is already named '" + newName +"'.";
                } else if(scenarioObject instanceof StateTransition){
                    warning = "Another State Transition is already named '" + newName +"'.";
                } else if(scenarioObject instanceof Strategy){
                    warning = "Another Strategy is already named '" + newName +"'.";
                }
                Notify.notify(warning, NotifyType.DANGER);
                nameLabel.setValue(name);

                return;
            }
            
            String oldName = ScenarioClientUtility.getScenarioObjectName(scenarioObject);
            ScenarioClientUtility.setScenarioObjectName(scenarioObject, newName);
            
            nameLabel.setValue(newName);

            SharedResources.getInstance().getEventBus()
                    .fireEvent(new RenameScenarioObjectEvent(scenarioObject, oldName, newName)); 
  }  
    
    @Override
    public void setState(boolean open, boolean fireEvents){
        super.setState(open, fireEvents);
        
        // when collapsing a node, collapse all descendants so that the user doesn't
        // have to collapse each descendant manually.  The goal is to have GIFT show the direct
        // descendants of a selected node automatically (done elsewhere) but not the rest
        // of the descendants if the selected node was previously manually collapsed. 
        if(!open){
            for(int index = 0; index < getChildCount(); index ++){
                TreeItem child = getChild(index);
                child.setState(open, fireEvents);
            }
        }
    }
    
    /**
     * Updates the icon for the tree item. An additional feature of this method is that if the
     * validation cache is dirty for this object, validation will be performed and updated in the
     * cache.
     */
    public void updateIcon() {
        if (ScenarioClientUtility.getValidationCache().isValid(object)) {
            setIconType(ScenarioElementUtil.getTypeIcon(object), null);
        } else {
            setIconType(IconType.EXCLAMATION_TRIANGLE, "red");
        }
    }
    
    /**
     * Sets the icon type for the tree item.
     * 
     * @param iconType the {@link IconType}
     * @param color the color to set the icon. If null, the color property will be cleared.
     */
    private void setIconType(IconType iconType, String color) {

        typeIcon.setType(iconType);

        // if not given a specific color, set the property to empty string
        typeIcon.setColor(StringUtils.isNotBlank(color) ? color : "");
    }
    
    @Override
    public ScenarioObjectTreeItem<?> getParentItem() {
        TreeItem parentItem = super.getParentItem();
        if (parentItem == null) {
            return null;
        } else if (!(parentItem instanceof ScenarioObjectTreeItem<?>)) {
            logger.severe("Trying to get the parent item that is not of type '"
                    + ScenarioObjectTreeItem.class.getSimpleName() + "'");
            return null;
        }

        return (ScenarioObjectTreeItem<?>) parentItem;
    }
    
    /**
     * Handles when another scenario tree item is dropped on top of this one. This method will not do anything by default, but
     * classes extending this method may use it to handle when specific tree items are dragged.
     * 
     * @param dragged the scenario item that was dragged and dropped on this item
     */
    protected void onDrop(ScenarioObjectTreeItem<? extends Serializable> dragged) {
        //by default, don't do anything when scenario objects are dropped on top of each other
    }
    
    /**
     * Gets the tree item that is currently being dragged, if applicable
     * 
     * @return the tree item being dragged
     */
    public static ScenarioObjectTreeItem<? extends Serializable> getItemBeingDragged() {
        return itemBeingDragged;
    }
    
    /**
     * Gets whether or not this tree item should allow the given tree item to be dropped on it
     * 
     * @param otherItem the item to drop
     * @return whether or not this item allows the given item to be dropped on it
     */
    public boolean allowDrop(ScenarioObjectTreeItem<?> otherItem) {
        return true;
    }
    
    /**
     * Gets whether or not the given tree item should be placed below this one if it is dropped
     * 
     * @param otherItem the item to drop
     * @return whether or not the given tree item should be placed below this one
     */
    public boolean shouldDropBelow(ScenarioObjectTreeItem<?> otherItem) {
        return false;
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("[ScenarioObject: ")
                .append("object = ").append(getScenarioObject())
                .append("]").toString();
    }
}