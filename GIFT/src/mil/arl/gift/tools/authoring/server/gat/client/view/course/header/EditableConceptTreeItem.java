/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.course.header;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.Trigger;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TreeItem;

import generated.course.AuthoritativeResource;
import generated.course.ConceptNode;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ConceptNodeTreeItem;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.CourseConceptUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.ScenarioEditorDirtyEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.header.editor.TeamReference;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.header.editor.TeamReferenceList;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.ContextMenu;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.ScenarioEventUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelDialog;

/**
 * A tree item that represents a team in the team organization outline and allows the author to rename it, remove it, edit it,
 * and add teams or team members to it.
 * 
 * @author nroberts
 */
public class EditableConceptTreeItem extends ConceptNodeTreeItem{
    
    /** A context menu that lets the author pick whether they want to add a team or a team member */
    private ContextMenu addObjectMenu = new ContextMenu();
    
    /** Logger for the class */
    private static final Logger logger = Logger.getLogger(EditableConceptTreeItem.class.getName());

    /** The tool tip for a team */
    private final String ADD_CONCEPT_TOOLTIP = "Add a sub-concept to this concept";
    
    /** A panel used to show the resource editor */
    private FlowPanel resourcePanel = new FlowPanel();
    
    /** The editor used to edit this concept's resource */
    private AuthoritativeResourceEditor resourceEditor = new AuthoritativeResourceEditor();

    /**
     * Creates a new tree item that represents and modifies the given team
     * 
     * @param team the team to represent and modify
     */
    public EditableConceptTreeItem(final ConceptNode concept) {
        super(concept, !CourseConceptUtility.isRootConcept(concept) && !GatClientUtility.isReadOnly());
        
        getNameLabel().setEditingEnabled(!isReadOnly());
        
        if(logger.isLoggable(Level.FINE)){
            logger.fine("Initializing add button logic");
        }
        
        if (GatClientUtility.isReadOnly()) {
            resourceEditor.setReadOnly(true);
            resourceIcons.removeStyleName(CSS.teamOrganizationPlayableIcon());
            resourceIcons.addStyleName(CSS.teamOrganizationPlayableIconDisabled());
            resourceTooltip.setTrigger(Trigger.MANUAL);
        }
        
        //initialize the editor used to modify this concept's authoritative resource
        resourceIcons.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {

                if (GatClientUtility.isReadOnly()) {
                    return;
                }
                
                AuthoritativeResource newResource = null;
                if(concept.getAuthoritativeResource() == null){
                    newResource = new AuthoritativeResource();
                    newResource.setId("");  // need to set the id to something in order to have the required id element appear in the course.xml
                                            // if not then PublishLessonScore.getConceptsAsXMLString(publishLessonScore.getConcepts().getConceptNode())); fails
                }
                concept.setAuthoritativeResource(newResource);
                
                updateResourceIcon();
                
                updateVisibleEditor();
            }
            
        }, ClickEvent.getType());
        
        resourceEditor.getElement().getStyle().setMarginTop(5, Unit.PX);
        resourceEditor.setResourceChangedCommand(new Command() {
            
            @Override
            public void execute() {
                ScenarioEventUtility.fireDirtyEditorEvent(concept); //re-validate when the resource changes
            }
        });
        
        resourcePanel.getElement().getStyle().setProperty("maxWidth", "500px");
        resourcePanel.add(resourceEditor);
        
        typeDeck.add(resourcePanel);
        
        resourceEditor.edit(concept);
        
        onRename();
        
        updateVisibleEditor();

        // button to add teams or team members
        Icon addButton = addButton(IconType.PLUS_CIRCLE, ADD_CONCEPT_TOOLTIP, new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                //stop propagation on the click event so that we don't re-select this item
                event.stopPropagation();
                
                if(logger.isLoggable(Level.FINE)){
                    logger.fine("Adding sub-team to team " + concept.toString());
                }
                
                addObjectMenu.hide();

                ConceptNode subNode = CourseConceptUtility.generateNewConcept();
                getObject().getConceptNode().add(subNode);
                
                ConceptNodeTreeItem newNodeItem = new EditableConceptTreeItem(subNode);
                newNodeItem.setPickMode(pickMode);
                newNodeItem.setOnPickStateChangeCommand(onPickStateChangeCommand);
                
                addItem(newNodeItem);
                
                setState(true);
                
                getTree().setSelectedItem(newNodeItem);
                
                if(Boolean.TRUE.equals(isPicked()) && getChildCount() == 1) {
                    
                    //if this node is selected and the first node is being added to it, automatically select that first unit
                    newNodeItem.getCheckBox().setValue(true, true);
                }
                
                //a new concept was created, so update the map of global references
                CourseConceptUtility.gatherConceptReferences();
                
                ScenarioEventUtility.fireDirtyEditorEvent(subNode);
                
                if(logger.isLoggable(Level.FINE)){
                    logger.fine("Finished adding sub concept");
                }
            }
        });
        addButton.setVisible(!isReadOnly());
        
        if(logger.isLoggable(Level.FINE)){
            logger.fine("Initializing delete button logic");
        }
        
        if(!concept.equals(CourseConceptUtility.getRootConcept())) {
            
            Icon cloneButton = addButton(IconType.CLONE, "Copy this concept", new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    
                    if(getParentItem() != null) {
                    
                        ConceptNode clone = getConceptClone();
                        
                        //add the node clone to this node's parent
                        ConceptNode parentConcept = getParentItem().getObject();
                        parentConcept.getConceptNode().add(parentConcept.getConceptNode().indexOf(concept) + 1, clone);
                        
                        //create a new tree item for the cloned node and place it after this item in the tree
                        getParentItem().insertItem(getParentItem().getChildIndex(EditableConceptTreeItem.this) + 1, new EditableConceptTreeItem(clone));
                    }
                }
                
            });
            cloneButton.setVisible(!isReadOnly());
            
            // button to delete this concept
            Icon deleteButton = addButton(IconType.TRASH, "Delete this concept", new ClickHandler() {
                
                @Override
                public void onClick(ClickEvent event) {
                    
                    if(logger.isLoggable(Level.FINE)){
                        logger.fine("Deleting concept " + concept.toString());
                    }
                    
                    //stop propagation on the click event so that we don't re-select this item
                    event.stopPropagation();
                    
                    final TreeItem parentItem = getParentItem();
                    
                    if(parentItem != null) {
                        
                        final ConceptNode parentConcept = ((ConceptNodeTreeItem) parentItem).getObject();
                        
                        if(parentConcept != null){
                                
                            OkayCancelDialog.show(
                                "Delete Concept?", 
                                "Are you sure you want to delete the concept <b>" + getObject().getName() + "</b> from the concept <b>" + parentConcept.getName() + "</b>?", 
                                "Delete Concept", 
                                new OkayCancelCallback() {
                                    
                                    @Override
                                    public void okay() {
                                        if(logger.isLoggable(Level.FINE)){
                                            logger.fine("User confirmed delete");
                                        }
                                        
                                        final List<TeamReference> references = 
                                                CourseConceptUtility.getReferencesToConcept(getObject().getName());
                                        
                                        if(references == null || references.isEmpty()) {
                                            delete();
                                            
                                        } else {
                                            
                                            final TeamReferenceList referenceList = new TeamReferenceList();
                                            referenceList.getListEditor().replaceItems(references);
                                            referenceList.setOnJumpCommand(new Command() {
                                                
                                                @Override
                                                public void execute() {
                                                    
                                                    //close the current prompt if the author jumps to a course object
                                                    OkayCancelDialog.cancel();
                                                }
                                            });
                                            
                                            //defer the next prompt so that the previous one can close and so the reference list can be populated
                                            Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                                                
                                                @Override
                                                public void execute() {
                                                    
                                                    int refCount = 0;
                                                    
                                                    for(TeamReference reference : references) {
                                                        refCount += reference.getReferenceCount();
                                                    }
                                                    
                                                    //this concept has references, so we need to ask the author if they want to remove these references first
                                                    OkayCancelDialog.show(
                                                            "Remove references?", 
                                                            refCount +" " + (refCount > 1 ? "course objects are" : "course object is") + " referencing this concept. "
                                                            + "These references must be removed in order to delete this concept."
                                                            + "<br/><br/>Do you want to remove these references and continue with the delete? "
                                                            + "<br/><br/>Course objects that reference this concept:", 
                                                            referenceList,
                                                            "Remove References and Continue Delete", 
                                                            new OkayCancelCallback() {
                                                                
                                                                @Override
                                                                public void okay() {
                                                                    delete();
                                                                }
                                                                
                                                                @Override
                                                                public void cancel() {
                                                                    
                                                                    if(logger.isLoggable(Level.FINE)){
                                                                        logger.fine("User cancelled removing references");
                                                                    }
                                                                }
                                                            }
                                                    ); 
                                                }
                                            });
                                        }
                                    }
                                    
                                    @Override
                                    public void cancel() {
                                        
                                        if(logger.isLoggable(Level.FINE)){
                                            logger.fine("User cancelled delete");
                                        }
                                    }
                                }
                            ); 
                        }
                    }
                }
            });
            deleteButton.setVisible(!isReadOnly());
        }
        
    }
    
    /**
     * Gets a deep clone of the concept that this tree item represents
     * 
     * @return a deep clone of this item's concept
     */
    public ConceptNode getConceptClone() {
        
        ConceptNode clone = new ConceptNode();

        for(int i = 0; i < getChildCount(); i++) {
            
            ConceptNodeTreeItem child = (ConceptNodeTreeItem) getChild(i);
            
            if(child instanceof EditableConceptTreeItem) {
                
                //add a clone of the sub concept as a child of this concept's clone
                clone.getConceptNode().add(((EditableConceptTreeItem) child).getConceptClone());  
            }
        }
        
        String tentativeCloneName = getObject().getName() + " - Copy";
        
        //ensure the concept clone is given its own unique name
        int i = 1;
        String cloneName = tentativeCloneName;
        while(!CourseConceptUtility.isConceptNameValid(cloneName)) {
           cloneName = tentativeCloneName + " " + ++i;
        }
        
        clone.setName(cloneName);
        
        return clone;
    }
    
    /**
     * Deletes this tree item and its associated concept and removes all references to said concept
     */
    private void delete() {
        
        if(logger.isLoggable(Level.FINE)){
            logger.fine("Started deleting concept");
        }
        
        final TreeItem parentItem = getParentItem();
        
        if(parentItem != null) {
            
            final ConceptNode parentConcept = ((ConceptNodeTreeItem) parentItem).getObject();
            
            if(parentConcept != null){
        
                parentConcept.getConceptNode().remove(getObject());
                
                remove();
                
                // update references to this concept whenever it is deleted
                CourseConceptUtility.updateConceptReferences(getObject().getName(), null);
                
                ScenarioEventUtility.fireDirtyEditorEvent(parentConcept);
            }
            
            parentItem.setSelected(true);
        }
        
        if(logger.isLoggable(Level.FINE)){
            logger.fine("Finished deleting concept");
        }
    }
    
    @Override
    public void onDrop(ConceptNodeTreeItem dragged) {
        
        if(dragged != null) {
            
            if(!dragged.getObject().equals(getObject()) && getParentItem() != null) {
            
                ConceptNode dragNode = dragged.getObject();
                
                //if a concept is dragged on top of this concept, place the dragged concept after this concept
                ConceptNode fromParent = dragged.getParentItem().getObject();
                ConceptNode toParent = getParentItem().getObject();
                
                if(fromParent != null && toParent != null) {
                    
                    fromParent.getConceptNode().remove(dragNode);
                    
                    int dropIndex = toParent.getConceptNode().indexOf(getObject());
                        
                    toParent.getConceptNode().add(dropIndex, dragNode);
                    
                    boolean wasDragItemSelected = dragged.isSelected();
                    
                    dragged.remove();
                    
                    //move the tree items accordingly
                    for(int index = 0; index < getParentItem().getChildCount(); index++) {
                        
                        if(getParentItem().getChild(index).equals(this)) {
                            
                            getParentItem().insertItem(index, dragged);
                            
                            if(wasDragItemSelected) {
                                
                                //if the dragged item was selected, we need to re-select it
                                dragged.setSelected(true);
                            }
                            
                            break;
                        }  
                    }
                }
                
                //update the validation state of the objects affected by this move
                SharedResources.getInstance().getEventBus().fireEvent(new ScenarioEditorDirtyEvent(fromParent));
                
                if(toParent != null && !toParent.equals(fromParent)) {
                    SharedResources.getInstance().getEventBus().fireEvent(new ScenarioEditorDirtyEvent(toParent));
                }
            }
        }
    }

    @Override
    protected ConceptNodeTreeItem createConceptNodeTreeItem(ConceptNode conceptNode) {
        return new EditableConceptTreeItem(conceptNode);
    }

    @Override
    protected boolean isReadOnly() {
        return GatClientUtility.isReadOnly();
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        
        updateVisibleEditor();
    }

    /**
     * Shows the appropriate editor for this item's athoritative resource if this item is selected. If this item is not selected, the 
     * editor will be hidden.
     */
    private void updateVisibleEditor() {
        
        if(isSelected() 
                && typeDeck.getVisibleWidget() != typeDeck.getWidgetIndex(resourcePanel) 
                && getObject().getAuthoritativeResource() != null) {
            
            typeDeck.showWidget(typeDeck.getWidgetIndex(resourcePanel));
            
            //reload the resouce editor
            resourceEditor.edit(getObject());
            resourceEditor.refreshDisplayedData();
            
        } else {
            typeDeck.showWidget(typeDeck.getWidgetIndex(emptyPanel));
        }
    }

    @Override
    protected boolean isObjectNameValid(ConceptNode object, String name) {
        return CourseConceptUtility.isConceptNameValid(name);
    }

    @Override
    protected void updateConceptReferences(String oldName, String newName) {
        CourseConceptUtility.updateConceptReferences(oldName, newName);
    }

    @Override
    protected void updateValidity() {
        isValid = true;
    }
}