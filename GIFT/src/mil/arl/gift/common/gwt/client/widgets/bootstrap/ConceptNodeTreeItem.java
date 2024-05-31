/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets.bootstrap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.IconStack;
import org.gwtbootstrap3.client.ui.Image;
import org.gwtbootstrap3.client.ui.RadioButton;
import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.extras.notify.client.constants.NotifyType;
import org.gwtbootstrap3.extras.notify.client.ui.Notify;
import org.gwtbootstrap3.extras.notify.client.ui.NotifySettings;

import com.github.gwtd3.api.Coords;
import com.github.gwtd3.api.D3;
import com.github.gwtd3.api.arrays.Array;
import com.github.gwtd3.api.behaviour.Drag.DragEventType;
import com.github.gwtd3.api.core.Selection;
import com.github.gwtd3.api.core.Value;
import com.github.gwtd3.api.functions.DatumFunction;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.VerticalAlign;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.CssResource.NotStrict;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TreeItem;

import generated.course.ConceptNode;
import mil.arl.gift.common.util.StringUtils;

/**
 * A generic tree item that represents a node in a concept hierarchy and allows the author
 * to interact with it
 * 
 * @author nroberts
 */
public class ConceptNodeTreeItem extends TreeItem {
    /** The logger for the class */
    private static Logger logger = Logger.getLogger(ConceptNodeTreeItem.class.getName());

    /** Interface to allow CSS file access */
    public interface Bundle extends ClientBundle {
        /** The instance of the bundle */
        public static final Bundle INSTANCE = GWT.create(Bundle.class);

        /**
         * The specific css resource
         * 
         * @return the css resource
         */
        @NotStrict
        @Source("TreeItemStyles.css")
        public MyResources css();
    }

    /** Interface to allow CSS style name access */
    protected interface MyResources extends CssResource {
        /**
         * The drag icon style
         * 
         * @return the style name for the drag icon
         */
        String teamOrganizationItem();
        String scenarioTreeItemIcon();
        String scenarioTreeItemName();
        String scenarioTreeItemDrag();
        String scenarioTreeItemDrop();
        String scenarioTreeItemDropBelow();
        String scenarioTreeItemDropBelowNoChildren();
        String scenarioTreeItemDropOver();
        String scenarioTreeItemNoDrop();
        String scenarioTreeItemButton();
        String teamOrganizationSortMatch();
        String teamOrganizationPlayableIcon();
        String teamOrganizationPlayableIconDisabled();
        String checkBox();
    }

    /** The CSS resource */
    protected static final MyResources CSS = Bundle.INSTANCE.css();
    
    /** The setting to use when displaying error notifications to the user **/
    private static final  NotifySettings NOTIFICATION_SETTINGS = NotifySettings.newSettings();
    
    static {
        /* Make sure the css style names are accessible */
        Bundle.INSTANCE.css().ensureInjected();
        
        NOTIFICATION_SETTINGS.setZIndex(5000);
        NOTIFICATION_SETTINGS.setType(NotifyType.DANGER);
    }

    /** The radio button group name to use when only one concept can be picked */
    private static final String CONCEPT_SELECTOR_GROUP = "ConceptSelectorGroup";
    
    
    /** The different modes that can be used to pick concepts within tree items and their children */
    public static enum PickMode{
        
        /** A mode specifying that no concepts should be pickable */
        NONE,
        
        /** A mode specifying that the author should only be able to pick a single concept */
        SINGLE,
        
        /** A mode specifying that the author should be able to pick multiple concepts */
        MULTIPLE
    }
    
    /** The tree item currently being dragged, if the user is currently dragging an item */
    private static ConceptNodeTreeItem itemBeingDragged = null;

    /** 
     * Whether or not {@link #itemBeingDragged} should be updated as drag events are received. D3 doesn't provide a way to
     * cancel dragging an element, so this boolean is used as a workaround to make sure that this object's dragging behavior
     * does not reset {@link #itemBeingDragged} if dragging is supposed to be disabled.
     */
    private static boolean isDraggingEnabled;

    /** The concept object that this tree item represents */
    protected ConceptNode object;

    /** The panel containing the tree item components */
    protected FlowPanel containerPanel = new FlowPanel();
    
    /** 
     * The check box used to pick this item's concept. 
     * <br/><br/>
     * If this item is set to is {@link PickMode#SINGLE} mode and represents a {@link ConceptNode}, 
     * then a {@link RadioButton} will be used to prevent the author from picking more than one item.
     * <br/><br/>
     * If this item is set to is {@link PickMode#MULTIPLE} mode, 
     * then a {@link CheckBox} will be used to allow the author to pick multiple items.
     */
    private CheckBox checkBox = null;
    
    /** The panel that the check box will be added to once it is created, if it is created at all */
    private SimplePanel checkBoxPanel = new SimplePanel();
    
    /** The icon to be displayed in the tree item */
    private Icon typeIcon = new Icon();

    /** The image associated with this item (e.g. the military symbol associated with a training application entity) */
    private Image image = new Image();
    
    /** The name label */
    private EditableInlineLabel nameLabel = new EditableInlineLabel();
    
    /** The panel containing the tree item action buttons */
    private FlowPanel buttonPanel = new FlowPanel();
    
    /** A mapping of the buttons added to the tooltips created for them */
    protected HashMap<Icon, ManagedTooltip> buttonToTooltip = new HashMap<Icon, ManagedTooltip>();
    
    /** The mode that this item is currently using to allow the author to pick concepts*/
    protected PickMode pickMode = PickMode.NONE;

    /** A command to execute whenever the author changes the state of this item's check box*/
    protected Command onPickStateChangeCommand;

    /** Flag indicating if this tree item is valid */
    protected boolean isValid = true;

    /** 
     * Whether concepts represented by this item and its children should be able to be selected without selecting
     * their children, allowing users to pick concepts without their children.
     */
    private boolean parentPickEnabled = false;
    
    /** A panel used to hide the editor */
    protected SimplePanel emptyPanel = new SimplePanel();
    
    /** Icon used to indicate that this concept node is associated with an authoritative resource */
    private Icon resourceIcon = new Icon( IconType.UNLINK);

    /** Tooltip for the icon used to set whether a concept node is associated with an authoritative resource */
    protected Tooltip resourceTooltip;

    /** The icons to display to indicate if the concept is associated with an authoritative resource */
    protected final IconStack resourceIcons = new IconStack();
    
    /** The deck used to change the type of editr that is shown */
    protected DeckPanel typeDeck = new DeckPanel();
    
    /**
     * Creates an undraggable tree item representing the given object
     * 
     * @param object the object that the tree item should represent. Can't be null.
     */
    public ConceptNodeTreeItem(ConceptNode object){
        this(object, false);
    }
    
    /**
     * Creates a tree item representing the given object
     * 
     * @param object the object that the tree item should represent. Can't be null.
     * @param draggable whether or not the tree item should be draggable
     */
    protected ConceptNodeTreeItem(ConceptNode object, final boolean draggable){
        
        if (object == null) {
            throw new IllegalArgumentException("The parameter 'object' cannot be null.");
        }

        this.object = object;

        // remove any vertical padding around this item, since this creates gaps when dragging items
        getElement().getStyle().setPaddingTop(0, Unit.PX);
        getElement().getStyle().setPaddingBottom(0, Unit.PX);
        getElement().getStyle().setPaddingLeft(16, Unit.PX);
        getElement().getStyle().setFontWeight(FontWeight.NORMAL);
        getElement().getStyle().setColor("black");
        
        getElement().addClassName(CSS.teamOrganizationItem());
        
        //provide an optional check box that the author can use to mark this item
        checkBoxPanel.getElement().getStyle().setDisplay(Display.INLINE_BLOCK);
        checkBoxPanel.getElement().getStyle().setVerticalAlign(VerticalAlign.MIDDLE);
        
        containerPanel.add(checkBoxPanel);

        if (draggable) {
            // add an icon to indicate that concept tree items can be dragged
            DragIcon dragIcon = new DragIcon();
            @SuppressWarnings("unused")
            ManagedTooltip dragIconTooltip = new ManagedTooltip(dragIcon, "Drag to reorder");
            dragIcon.getElement().getStyle().setMarginRight(5, Unit.PX);

            containerPanel.add(dragIcon);
        }

        /* use an icon to indicate this object's type. Also performs validation on this object and
         * adds/updates the cache if it's not clean */
        updateIcon();
        containerPanel.add(typeIcon);
        typeIcon.addStyleName(CSS.scenarioTreeItemIcon());

        // use an editable label to display and modify this object's name
        nameLabel.setValue(getName());
        nameLabel.addStyleName(CSS.scenarioTreeItemName());
        nameLabel.setTooltipContainer("body");  //make sure the tooltip isn't hidden behind the tabs
        nameLabel.setEditingEnabled(false);

        nameLabel.addValueChangeHandler(new ValueChangeHandler<String>() {

            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("onValueChangeHandler");
                }

                ConceptNode object = getObject();
                
                String name = object.getName();
                String newName = event.getValue().trim();
                
                
                if (StringUtils.equals(name, newName)) {
                    // No change
                    return;
                } else if(StringUtils.isBlank(newName)) {
                    
                    Notify.notify("The name must contain at least one visible character.", NOTIFICATION_SETTINGS);
                    nameLabel.setValue(name);
                    
                    return;
                    
                } else if (!isObjectNameValid(object, newName)) {

                    Notify.notify("Another concept is already named '" + newName +"'.", NOTIFICATION_SETTINGS);
                    nameLabel.setValue(name);

                    return;
                }
                
                String oldName = object.getName();
                object.setName(newName);
                
                nameLabel.setValue(newName);
                onRename();
                
                // update references to this concept whenever its name changes
                updateConceptReferences(oldName, newName);
            }

        });

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
        
        containerPanel.add(nameLabel);

        image.getElement().getStyle().setProperty("margin", "-5px 0px");
        image.getElement().getStyle().setProperty("maxHeight", "28px");
        containerPanel.add(image);
        
        //add the panel where additional buttons will get added
        buttonPanel.getElement().getStyle().setDisplay(Display.INLINE_BLOCK);
        containerPanel.add(buttonPanel);

        setWidget(containerPanel);

        Selection elementSelection = D3.select(getElement())
        .data(Array.fromJavaArray(new Coords[]{Coords.create(0, 0)})); //define data containing the drag origin (i.e. top left coordinates)
        
        if(draggable) {
            
            //use D3's drag behavior to allow concept tree items to be dragged (similar to nodes in the course tree)
            
            elementSelection.call(D3.behavior().drag()
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
                        
                        if(itemBeingDragged == null && isDraggingEnabled) {
                            
                            //start dragging a new item
                            itemBeingDragged = ConceptNodeTreeItem.this;
                        }
                        
                        if(Math.abs(coord.x()) > 2 || Math.abs(coord.y()) > 2) {
                            
                            //start dragging once the mouse moves a certain amount
                            if(StringUtils.isBlank(context.getStyle().getProperty("transform"))) {
                                
                                //apply the drag styling the first time this item's position is changed
                                containerPanel.getElement().addClassName(CSS.scenarioTreeItemDrag());
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
                        isDraggingEnabled = false;
                        containerPanel.getElement().removeClassName(CSS.scenarioTreeItemDrag());
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
                     * Note that adding more <img> elements to concept object tree items in the future may break this logic.
                     */
                    EventTarget target = D3.event().getEventTarget();
                    
                    if(Element.is(target) && "img".equals(((Element) target.cast()).getTagName().toLowerCase())) {
                        setState(!getState(), true);
                    }
                    
                    isDraggingEnabled = true; //allow this item to be dragged
                    
                    return null;
                }
            }).on("click", new DatumFunction<Void>() {
                
                @Override
                public Void apply(Element context, Value d, int index) {
                    
                    D3.event().stopPropagation();
                    
                    //select this item on click
                    if(!ConceptNodeTreeItem.this.equals(getTree().getSelectedItem())) {
                        getTree().setSelectedItem(ConceptNodeTreeItem.this, true);
                    }
                    
                    return null;
                }
            });
        }
        
        elementSelection.on("mouseover", new DatumFunction<Void>() {
            
            @Override
            public Void apply(Element context, Value d, int index) {
                
                D3.event().stopPropagation();
                
                if(itemBeingDragged != null) {
                    
                    getElement().addClassName(CSS.scenarioTreeItemDrop());
                    
                    if(shouldDropBelow(itemBeingDragged)) {
                        
                        if(getChildCount() > 0) {
                            getElement().addClassName(CSS.scenarioTreeItemDropBelow());
                            
                        } else {
                            getElement().addClassName(CSS.scenarioTreeItemDropBelowNoChildren());
                        }
                        
                    } else {
                        getElement().addClassName(CSS.scenarioTreeItemDropOver());
                    }
                    
                    if(!allowDrop(itemBeingDragged)){
                        getElement().addClassName(CSS.scenarioTreeItemNoDrop());
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
                    getElement().removeClassName(CSS.scenarioTreeItemDrop());
                    getElement().removeClassName(CSS.scenarioTreeItemDropBelow());
                    getElement().removeClassName(CSS.scenarioTreeItemDropBelowNoChildren());
                    getElement().removeClassName(CSS.scenarioTreeItemDropOver());
                    getElement().removeClassName(CSS.scenarioTreeItemNoDrop());
                }
                
                return null;
            }
        }).on("mouseup", new DatumFunction<Void>() {
            
            @Override
            public Void apply(Element context, Value d, int index) {
                
                if(itemBeingDragged != null) {
                    
                    //remove all classes that add drop styling
                    getElement().removeClassName(CSS.scenarioTreeItemDrop());
                    getElement().removeClassName(CSS.scenarioTreeItemDropBelow());
                    getElement().removeClassName(CSS.scenarioTreeItemDropBelowNoChildren());
                    getElement().removeClassName(CSS.scenarioTreeItemDropOver());
                    getElement().removeClassName(CSS.scenarioTreeItemNoDrop());
                    
                    onDrop(itemBeingDragged);
                    
                    //need to ensure that the concept outline doesn't handle dropping this item
                    itemBeingDragged = null;
                }
                
                isDraggingEnabled = false;
                
                return null;
            }
        });
        
        for (Serializable childNode : object.getConceptNode()) {
            addItem(createConceptNodeTreeItem((ConceptNode) childNode));
        }

        setState(true);
        
        resourceIcons.addStyleName(CSS.teamOrganizationPlayableIcon());

        resourceIcons.add(resourceIcon, false);

        resourceTooltip = new Tooltip(resourceIcons);
        resourceTooltip.setIsHtml(true);
        resourceTooltip.setContainer("body");

        containerPanel.insert(resourceIcons, containerPanel.getWidgetIndex(getNameLabel()));
        
        updateResourceIcon();
        
        /* set up the panel used to edit the concept's associated resources */
        typeDeck.setAnimationEnabled(true);

        typeDeck.addDomHandler(new MouseDownHandler() {
            @Override
            public void onMouseDown(MouseDownEvent event) {
                /* clicking in the editor should not propagate to the tree */
                event.stopPropagation();
            }
        }, MouseDownEvent.getType());

        typeDeck.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                /* clicking in the editor should not propagate to the tree */
                event.stopPropagation();
            }
        }, ClickEvent.getType());

        typeDeck.add(emptyPanel);

        containerPanel.add(typeDeck);
    }
    
    /**
     * Creates a new {@link ConceptNodeTreeItem}
     * 
     * @param childNode the child node to represent and modify
     * @return the newly created {@link ConceptNodeTreeItem}
     */
    protected ConceptNodeTreeItem createConceptNodeTreeItem(ConceptNode childNode) {
        return new ConceptNodeTreeItem(childNode);
    }

    /**
     * If this item is pickable and has any children, updates this item's picked state to match its children's. If at least 
     * one child is not picked, this item will also not be picked. If all of this item's children are picked, this item
     * will also be picked.
     */
    protected void updatePickedStateToMatchChildren() {

        int childCount = getChildCount();
        if (!isPickable() || childCount <= 0) {
            /* Nothing to update */
            return;
        }

        int numChecked = 0;
        int numIndeterminate = 0;
        for (int i = 0; i < childCount; i++) {

            ConceptNodeTreeItem child = (ConceptNodeTreeItem) getChild(i);

            Boolean childChecked = child.isPicked();
            if (childChecked == null) {
                /* If a child is indeterminate, then the parent is
                 * indeterminate. No need to keep checking children. */
                numIndeterminate++;
                break;
            } else if (childChecked) {
                /* Increment number of children checked */
                numChecked++;
            } else if (numChecked > 0) {
                /* Found checked children and unchecked children. Parent is
                 * indeterminate. */
                break;
            }
        }

        final Boolean previousValue = isPicked();
        if (childCount == numChecked) {
            /* This item's children are all checked, so check this item too */
            checkBox.setValue(true);
        } else if (numIndeterminate == 0 && numChecked == 0) {
            
            if(parentPickEnabled) {
                
                if(checkBox.getValue() == null && checkBox instanceof ThreeStateCheckbox) {
                    
                    /* If this item is picked but not any children, check this item rather than leaving it indeterminate */
                    checkBox.setValue(true);
                }
                
            } else {
                
                /* This item's children are all unchecked, so uncheck this item too */
                checkBox.setValue(false);
            }
            
        } else {
            /* This item's children are both checked and unchecked. Mark this
             * checkbox as indeterminate if possible; uncheck if not
             * possible. */
            if (checkBox instanceof ThreeStateCheckbox) {
                checkBox.setValue(null);
            } else {
                checkBox.setValue(false);
            }
        }
        
        refreshCheckBoxStyling();

        boolean stateChanged = previousValue != isPicked();
        if (stateChanged && getParentItem() != null) {
            getParentItem().updatePickedStateToMatchChildren();
        }
    }

    /**
     * Gets the concept object that this tree item represents. Can't be null.
     * 
     * @return the concept object that this tree item represents
     */
    public ConceptNode getObject() {
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
     * @return the created button
     */
    public Icon addButton(IconType buttonIcon, String tooltipText, ClickHandler clickHandler) {
        
        Icon button = new Icon(buttonIcon);
        button.addStyleName(CSS.scenarioTreeItemButton());
        
        if(clickHandler != null) {
            button.addClickHandler(clickHandler);
        }

        ManagedTooltip tooltip = new ManagedTooltip(button, tooltipText);

        buttonToTooltip.put(button, tooltip);
        buttonPanel.add(tooltip);

        return button;
    }
    
    /**
     * Updates the tooltip text for a button
     * 
     * @param button the button for which the tooltip's text should be updated
     * @param tooltipText the text the tooltip should be updated to
     */
    public void updateButtonTooltip(Icon button, String tooltipText) {
        ManagedTooltip tooltip = buttonToTooltip.get(button);
        if (tooltip != null) {
            tooltip.setTitle(tooltipText);
        } else {
            logger.warning("Failed to updated tooltip text because could not find tooltip for the specified button.");
        }
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
        updateValidity();
        if (isValid) {
            setIconType(IconType.LIGHTBULB_O, null);
        } else {
            setIconType(IconType.EXCLAMATION_TRIANGLE, "red");
        }
    }

    /**
     * Checks if the {@link #object} is valid.
     */
    protected void updateValidity() {
        /* We aren't performing validation at this level */
        isValid = true;
    }

    /**
     * Return the flag indicating if this tree item is valid
     * 
     * @return true if the tree item is valid; false otherwise.
     */
    public boolean isValid() {
        return isValid;
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
    
    /**
     * Sets the URL of the image associated with this item (e.g. the military symbol associated with a training application entity)
     * 
     * @param url the url of the image. Can be null if no image should be shown next to this item.
     */
    public void setImageUrl(String url) {
        image.setUrl(url);
    }
    
    /**
     * Gets the URL of the image associated with this item (e.g. the military symbol associated with a training application entity)
     * 
     * @return the url of the image. Can be null if no image is being shown next to this item.
     */
    public String getImageUrl() {
        return image.getUrl();
    }
    
    @Override
    public ConceptNodeTreeItem getParentItem() {
        TreeItem parentItem = super.getParentItem();
        if (parentItem == null) {
            return null;
        } else if (!(parentItem instanceof ConceptNodeTreeItem)) {
            logger.severe("Trying to get the parent item that is not of type '"
                    + ConceptNodeTreeItem.class.getSimpleName() + "'");
            return null;
        }

        return (ConceptNodeTreeItem) parentItem;
    }
    
    /**
     * Handles when another concept tree item is dropped on top of this one. This method will not do anything by default, but
     * classes extending this method may use it to handle when specific tree items are dragged.
     * 
     * @param dragged the concept item that was dragged and dropped on this item
     */
    protected void onDrop(ConceptNodeTreeItem dragged) {
        //by default, don't do anything when concept objects are dropped on top of each other
    }
    
    /**
     * Gets the tree item that is currently being dragged, if applicable
     * 
     * @return the tree item being dragged
     */
    public static ConceptNodeTreeItem getItemBeingDragged() {
        return itemBeingDragged;
    }
    
    /**
     * Gets whether or not this tree item should allow the given tree item to be dropped on it
     * 
     * @param otherItem the item to drop
     * @return whether or not this item allows the given item to be dropped on it
     */
    public boolean allowDrop(ConceptNodeTreeItem otherItem) {
        return otherItem != null && getObject().equals(otherItem.getObject());
    }
    
    /**
     * Gets whether or not the given tree item should be placed below this one if it is dropped
     * 
     * @param otherItem the item to drop
     * @return whether or not the given tree item should be placed below this one
     */
    public boolean shouldDropBelow(ConceptNodeTreeItem otherItem) {
        
        if (otherItem == null) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Sets whether or not the author should be able to check and uncheck this tree item and its children via check boxes
     * 
     * @param pickMode the mode that can be used to pick concepts within tree items and their children
     */
    public void setPickMode(ConceptNodeTreeItem.PickMode pickMode) {
        
        if(!PickMode.NONE.equals(pickMode)) {
            
            boolean isNewCheckBox = false;
            
            if(PickMode.SINGLE.equals(pickMode) && object.getConceptNode().isEmpty() && !(checkBox instanceof RadioButton)) {
                
                //provide a radio button that the author can use to pick only one concept item
                checkBox = new RadioButton(CONCEPT_SELECTOR_GROUP);
                checkBox.addStyleName(CSS.checkBox());
                isNewCheckBox = true;
                
            } else if(PickMode.MULTIPLE.equals(pickMode) && (checkBox == null || checkBox instanceof RadioButton)) {
                
                //provide a check box that the author can use to pick this item
                checkBox = new ThreeStateCheckbox();
                checkBox.addStyleName(CSS.checkBox());
                isNewCheckBox = true;
            }
            
            if(isNewCheckBox) {
                
                checkBox.addMouseDownHandler(new MouseDownHandler() {
                    
                    @Override
                    public void onMouseDown(MouseDownEvent event) {
                        
                        //need to set value on mouse down so that the value can change as the user clicks off the check box
                        event.preventDefault();
                        event.stopPropagation();
                        
                        //manually update the value rather than relying on the default DOM logic that's fired on click (which fires too late)
                        togglePicked();
                    }
                });
                
                checkBox.addClickHandler(new ClickHandler() {
                    
                    @Override
                    public void onClick(ClickEvent event) {
                        
                        event.preventDefault();  //already changing check box state on mouse down, so don't do it on click
                        event.stopPropagation(); //allow tree items to be checked without being selected
                    }
                });
            }
        } 
        
        checkBoxPanel.setWidget(checkBox);
        
        this.pickMode = pickMode;
        
        //update the pick mode of this item's children
        for(int i = 0; i < getChildCount(); i++) {
            ((ConceptNodeTreeItem) getChild(i)).setPickMode(pickMode);
        }
    }
    
    /**
     * Toggles whether or not this item is currently picked, as if the user clicked on this
     * concept object's check box.
     */
    public void togglePicked() {
        
        boolean oldValue = checkBox.getValue() != null ? checkBox.getValue() : false;
        boolean newValue = false;
        boolean pickParentOnly = false;
        
        if((parentPickEnabled && !(object.getConceptNode().isEmpty()))) {
            
            if(!oldValue) {
                
                //if this concept is not picked, pick it but not its children
                pickParentOnly = true;
                newValue = !oldValue;
                
            } else {
                
                boolean hasAnyPickedImmediateChildren = false;
                
                for(int i = 0; i < getChildCount(); i++) {
                    
                    ConceptNodeTreeItem child = (ConceptNodeTreeItem) getChild(i);
                    if(!Boolean.FALSE.equals(child.isPicked())) {
                        hasAnyPickedImmediateChildren = true;
                        break;
                    }
                }
                
                if(hasAnyPickedImmediateChildren) {
                    
                    //if any of this concept's immediate children are picked, unpick this concept
                    newValue = !oldValue;
                    
                } else {
                    
                    //if this concept is picked without its children, pick its children
                    newValue = true;
                }
            }
            
        } else {
            
            //toggle the current picked status normally
            newValue = !oldValue;
            pickParentOnly = false;
        }
        
        checkBox.setValue(newValue);
        
        if(isPickable()) {
                
            if(parentPickEnabled && pickParentOnly) {
                
                //unpick this concept's children if only the concept itself should be picked
                setChildrenPicked(false);
                
            } else {
                
                //otherwise, pick/unpick this concept's children normally
                setChildrenPicked(newValue);
            }
            
            //if the author unchecks this item, need to make sure its parents are unchecked too
            ConceptNodeTreeItem parent = ConceptNodeTreeItem.this.getParentItem();
            
            if(parent != null) {
                parent.updatePickedStateToMatchChildren();
            }
            
            if(onPickStateChangeCommand != null) {
                onPickStateChangeCommand.execute();
            }
        }
        
        refreshCheckBoxStyling();
    }
    
    /**
     * Recursively navigates through this tree item's children and updates them to match the given pick state
     * 
     * @param picked whether or not this tree item's children should be picked
     */
    private void setChildrenPicked(boolean picked) {
        
        for(int i = 0; i < getChildCount(); i++) {
            
            ConceptNodeTreeItem item = (ConceptNodeTreeItem) getChild(i);
            
            if(item.isPickable()) {
                item.checkBox.setValue(picked);
            }
            
            if(!picked || !parentPickEnabled) {
                
                //only propagate this item's pick state to its children if it is not picked or parent picking is disabled
                item.setChildrenPicked(picked);
            }
            
            item.refreshCheckBoxStyling();
        }
    }
    
    /**
     * Sets a command to be executed when this item or its children is picked or unpicked by the author
     * 
     * @param onPickStateChange the command to execute
     */
    public void setOnPickStateChangeCommand(Command onPickStateChange) {
        this.onPickStateChangeCommand = onPickStateChange;
        
        for(int i = 0; i < getChildCount(); i++) {
            
            ConceptNodeTreeItem item = (ConceptNodeTreeItem) getChild(i);
            item.setOnPickStateChangeCommand(onPickStateChange);
        }
    }
    
    /**
     * Gets the names of the top-most concepts that have been picked. If a parent concept is picked, its children's
     * names will be excluded from the list.
     * 
     * @return the names of the top-most concepts that have been picked
     */
    public List<String> getPickedObjectNames() {
        
        List<String> checkedNames = new ArrayList<>();
        
        if(Boolean.TRUE.equals(isPicked())) {
            checkedNames.add(nameLabel.getValue());
            return checkedNames;
        }
        
        for(int i = 0; i < getChildCount(); i++) {
            checkedNames.addAll(((ConceptNodeTreeItem) getChild(i)).getPickedObjectNames());
        }
        
        return checkedNames;
    }

    /**
     * Sets the names of the top-most concepts that should be checked. If one of the provided names belongs to
     * a parent concept with children, all of its children will be selected automatically.
     * 
     * @param teamNames the names of the concepts that should be checked
     */
    public void setPickedObjectNames(Collection<String> objectNames) {

        boolean hasNameToCheck = objectNames != null
                && objectNames.contains(getObject().getName());

        if (isPickable()) {
            checkBox.setValue(hasNameToCheck);

            /* Update parent to reflect the checkbox change*/
            if (getParentItem() != null) {
                getParentItem().updatePickedStateToMatchChildren();
            }
        }

        if (hasNameToCheck) {
            setChildrenPicked(true);
        } else if (getChildCount() > 0) {
            for (int i = 0; i < getChildCount(); i++) {
                ConceptNodeTreeItem item = (ConceptNodeTreeItem) getChild(i);
                item.setPickedObjectNames(objectNames);
            }

            /* Children may have changed check state, so check if an update is
             * needed for this item */
            updatePickedStateToMatchChildren();
        }
    }

    /**
     * Gets the names of all the concepts that have been picked.
     * 
     * @return the names of the concepts that have been picked.
     */
    public List<String> getAllPickedObjectNames() {

        List<String> checkedNames = new ArrayList<>();

        if (Boolean.TRUE.equals(isPicked()) && object.getConceptNode().isEmpty()) {
            checkedNames.add(nameLabel.getValue());
        }

        for (int i = 0; i < getChildCount(); i++) {
            final ConceptNodeTreeItem treeItem = (ConceptNodeTreeItem) getChild(i);
            checkedNames.addAll(treeItem.getAllPickedObjectNames());
        }

        return checkedNames;
    }

    /**
     * Gets the names of all the leaf concepts whether they are picked or not.
     * 
     * @return the names of the leaf concepts.
     */
    public List<String> getAllLeafObjectNames() {
        List<String> names = new ArrayList<>();

        if (object.getConceptNode().isEmpty()) {
            names.add(nameLabel.getValue());
        }

        for (int i = 0; i < getChildCount(); i++) {
            final ConceptNodeTreeItem treeItem = (ConceptNodeTreeItem) getChild(i);
            names.addAll(treeItem.getAllLeafObjectNames());
        }

        return names;
    }

    /**
     * Checks if the tree item is picked or contains a picked item.
     * 
     * @return true if this tree item or a descendant is picked; false
     *         otherwise.
     */
    public boolean containsPicked() {
        /* Contains picked if the state is true or indeterminate (null) */
        if (!Boolean.FALSE.equals(isPicked())) {
            return true;
        }

        for (int i = 0; i < getChildCount(); i++) {
            final ConceptNodeTreeItem treeItem = (ConceptNodeTreeItem) getChild(i);
            if (treeItem.containsPicked()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Gets whether or not this item is currently picked
     * 
     * @return whether this item is currently picked. Can be null if in
     *         indeterminate state. Returns false if it is not pickable in the
     *         first place.
     */
    public Boolean isPicked() {
        
        if(isPickable()) {
            return checkBox.getValue();
        }

        return false;
    }
    
    /**
     * Gets whether or not this item can be picked by the author
     * 
     * @return whether this item can be picked by the author
     */
    public boolean isPickable() {
        return checkBox != null;
    }
    
    /**
     * Executes logic whenever the user uses this item to rename its represented concept object. By default, this method will do
     * nothing, but subclasses can override this method to implement their own logic.
     */
    protected void onRename() {
        //do nothing by default
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("[ConceptNodeTreeItem: ")
                .append("object = ").append(getObject())
                .append("]").toString();
    }
    
    /**
     * If this concept object contains the specified text, modifies its styling to indicate such. If this concept object has any children 
     * containing the text, then their styling will also be modified, and they will be sorted so that children containing 
     * the text are moved to the top
     * 
     * @param text the text to search for
     * @return whether or not this concept object or any of its children contain the selected text
     */
    public boolean sortByText(String text) {
        
        //determine search terms in the filter based on whitespace
        String[] searchTerms = null;
        
        if(text != null) {
            searchTerms = text.split("\\s+");
        }
        
        return sortBySearchTerms(searchTerms);
    }
    
    /**
     * If this concept object contains the specified search terms, modifies its styling to indicate such. If this concept object has any children 
     * containing the search terms, then their styling will also be modified, and they will be sorted so that children containing 
     * the search terms are moved to the top
     * 
     * @param searchTerms the search terms (i.e. whitespace separated strings of text) to search for
     * @return whether or not this concept object or any of its children contain the selected text
     */
    private boolean sortBySearchTerms(String[] searchTerms) {
        
        boolean containsText = false;
        
        if(searchTerms != null) {
            
            boolean missingTerm = false;
            
            String objectName = getNameLabel().getValue();
            
            //check if each concept object contains the necessary search terms
            for(int i = 0; i < searchTerms.length; i++) {
                
                if(objectName == null || !objectName.toLowerCase().contains(searchTerms[i])) {
                    missingTerm = true;
                    break;
                }
            }
            
            if(!missingTerm) {
                
                getElement().addClassName(CSS.teamOrganizationSortMatch());
                containsText = true;
                
            } else {
                getElement().removeClassName(CSS.teamOrganizationSortMatch());
            }
            
            for(int i = 0; i < getChildCount(); i++) {
                
                ConceptNodeTreeItem child = (ConceptNodeTreeItem) getChild(i);
                
                if(child.sortBySearchTerms(searchTerms)) {
                    containsText = true;
                }
            }
            
            setState(containsText);
        }
        
        return containsText;
    }
    
    /**
     * Stops dragging the concept object currently being dragged, if there is such an object. If no concept object is 
     * being dragged, this method will do nothing.
     */
    public static void cancelDragging() {
        
        itemBeingDragged = null;
        isDraggingEnabled = false;
    }
    
    /**
     * Hides all of the tooltips associated with this tree item and its children. Useful when this item's child elements cannot 
     * fire mouse out events, such as when this item is not attached to the DOM.
     */
    public void cleanUpTooltips() {
        
        for(Tooltip tooltip : buttonToTooltip.values()) {
            tooltip.hide();
        }
        
        for(int i = 0; i < getChildCount(); i++) {
            ((ConceptNodeTreeItem) getChild(i)).cleanUpTooltips();
        }
    }

    /**
     * Retrieve the check box used to pick this item's concept.
     * 
     * @return the check box
     */
    public CheckBox getCheckBox() {
        return checkBox;
    }

    /**
     * Specifies whether or not the tree item is in read-only mode.
     *
     * @return True indicates that the tree item is in read-only mode and
     *         therefore, no editing should be allowed on any of the widgets.
     *         False indicates that the tree item is not in read-only mode and
     *         therefore normal editing should be allowed through the widgets.
     */
    protected boolean isReadOnly() {
        /* Always false */
        return false;
    }
    
    /**
     * Gets whether or not adding an object with the specified name will cause a
     * naming conflict
     *
     * @param object the Serializable object to cast
     * @param name the name to check
     * @return whether or not a name conflict will occur
     */
    protected boolean isObjectNameValid(ConceptNode object, String name) {
        /* Always true */
        return true;
    }
    
    /**
     * Updates all references to the concept with the old name to
     * use the new name instead
     *
     * @param oldName the old concept name to replace
     * @param newName the new concept name to use
     */
    protected void updateConceptReferences(String oldName, String newName) {
        // do nothing
    }
    
    /**
     * Sets whether the user should be able to pick concepts in this hierarchy without automatically
     * picking their children.
     * 
     * @param enabled whether parent concept picking should be enabled. Defaults to false;
     */
    public void setParenPickEnabled(boolean enabled) {
        this.parentPickEnabled = enabled;
        
        for(int i = 0; i < getChildCount(); i++) {
            ((ConceptNodeTreeItem) getChild(i)).setParenPickEnabled(enabled);
        }
    }
    
    /**
     * Gets the name of this concept's name in the concept hierarchy
     * 
     * @return the concept's name. Cannot be null.
     */
    public String getName() {
        return object.getName();
    }
    
    /**
     * Calculates the current state of this item's checkbox and modifies its styling when necessary
     * to indicate states other than the default checkbox states. This is needed to visually distinguish when
     * a concept is selected without its children when parent picking is enabled.
     */
    private void refreshCheckBoxStyling() {
        
        if(isPickable() && !(object.getConceptNode().isEmpty()) && parentPickEnabled) {
            
            boolean hasPickedImmediateChild = false;
            
            for(int i = 0; i < getChildCount(); i++) {
                ConceptNodeTreeItem child = (ConceptNodeTreeItem) getChild(i);
                if(!Boolean.FALSE.equals(child.isPicked())) {
                    hasPickedImmediateChild = true;
                    break;
                }
            }

            if(Boolean.TRUE.equals(isPicked()) && !hasPickedImmediateChild) {
                
                //a concept item is selected without any selected children, so apply styling to distinguish it
                checkBox.getElement().getStyle().setProperty("filter", "invert(1)");
                
            } else {
                
                //otherwise, remove the distinguishing styling
                checkBox.getElement().getStyle().clearProperty("filter");
            }
        }
    }
    
    /**
     * Visually updates the icon used to control this concept's association with a resource
     * so that it matches this concept's current playable state. Also
     * updates the icon's tooltip accordingly.
     */
    protected void updateResourceIcon() {

        boolean usesResource = getObject().getAuthoritativeResource() != null;

        resourceIcon.setType(usesResource ? IconType.LINK : IconType.UNLINK);
        resourceTooltip.setTitle(usesResource 
                ? "This concept is associated with an authoratitive resource" 
                : "Click to associate this concept with an authoritative resource");
    }
}