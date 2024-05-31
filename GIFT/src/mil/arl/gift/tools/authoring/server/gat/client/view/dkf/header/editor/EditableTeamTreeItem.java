/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.header.editor;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.gwt.FlowPanel;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.TreeItem;

import generated.dkf.Team;
import generated.dkf.TeamMember;
import mil.arl.gift.common.enums.EchelonEnum;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.TeamMemberTreeItem;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.TeamObjectTreeItem;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.TeamTreeItem;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.ScenarioEditorDirtyEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
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
public class EditableTeamTreeItem extends TeamTreeItem{
    
    /** A context menu that lets the author pick whether they want to add a team or a team member */
    private ContextMenu addObjectMenu = new ContextMenu();
    
    /** Logger for the class */
    private static final Logger logger = Logger.getLogger(EditableTeamTreeItem.class.getName());
    
    /** A panel used to show the learner ID editor */
    private FlowPanel echelonPanel = new FlowPanel();
    
    /** The editor used to edit this team member's learner ID */
    private EchelonEditor echelonEditor;

    /** The tool tip for a team */
    private final String ADD_TEAM_AND_TEAM_MEMBER_TOOLTIP = "Add a sub-team or a learner role to this team";

    /**
     * Creates a new tree item that represents and modifies the given team
     * 
     * @param team the team to represent and modify
     */
    public EditableTeamTreeItem(final Team team) {
        super(team, !ScenarioClientUtility.isRootTeam(team) && !GatClientUtility.isReadOnly());
        
        echelonEditor = new EchelonEditor(team, new EchelonEditor.CancelCallback() {
            
            @Override
            public void onEditingCancelled(EchelonEnum backupCopy) {
                
                getTree().setSelectedItem(null, true);
                
                getTeamObject().setEchelon(backupCopy.getName());

                ScenarioEventUtility.fireDirtyEditorEvent(getTeamObject()); //re-validate when the learner ID changes
            }
        });

        if (GatClientUtility.isReadOnly()) {
            echelonEditor.setReadOnly(true);
        }

        getNameLabel().setEditingEnabled(!isReadOnly());

        if(logger.isLoggable(Level.FINE)){
            logger.fine("Initializing add button context menu");
        }
        
        final ScheduledCommand addTeamCommand = new ScheduledCommand() {
            
            @Override
            public void execute() {
                
                if(logger.isLoggable(Level.FINE)){
                    logger.fine("Adding sub-team to team " + team.toString());
                }
                
                addObjectMenu.hide();

                Team subTeam = ScenarioClientUtility.generateNewTeam();
                getTeamObject().getTeamOrTeamMember().add(subTeam);
                
                TeamTreeItem newTeamItem = new EditableTeamTreeItem(subTeam);
                newTeamItem.setPickMode(pickMode);
                newTeamItem.setOnPickStateChangeCommand(onPickStateChangeCommand);
                
                addItem(newTeamItem);
                
                setState(true);
                
                getTree().setSelectedItem(newTeamItem);
                
                if(Boolean.TRUE.equals(isPicked()) && getChildCount() == 1) {
                    
                    //if this team is selected and the first team unit is being added to it, automatically select that first unit
                    newTeamItem.getCheckBox().setValue(true, true);
                }
                
                //a new team was created, so update the map of global references
                ScenarioClientUtility.gatherTeamReferences();
                
                ScenarioEventUtility.fireDirtyEditorEvent(subTeam);
                
                if(logger.isLoggable(Level.FINE)){
                    logger.fine("Finished adding sub team");
                }
            }
        };
        
        addObjectMenu.getMenu().addItem(
                "<i class='fa fa-users scenarioTreeContextItem'></i>"
            +   "<span style='vertical-align: middle;'>"
            +       "Add sub-team"
            +   "</span>", true, addTeamCommand);
        
        final ScheduledCommand addTeamMemberCommand = new ScheduledCommand() {
            
            @Override
            public void execute() {

                if(logger.isLoggable(Level.FINE)){
                    logger.fine("Adding team member to team " + team.toString());
                }

                addObjectMenu.hide();

                TeamMember member = ScenarioClientUtility.generateNewTeamMember();
                getTeamObject().getTeamOrTeamMember().add(member);
                
                TeamMemberTreeItem newMemberItem = new EditableTeamMemberTreeItem(member);
                newMemberItem.setPickMode(pickMode);
                newMemberItem.setOnPickStateChangeCommand(onPickStateChangeCommand);
                
                addItem(newMemberItem);
                
                setState(true);
                
                getTree().setSelectedItem(newMemberItem);
                
                if(Boolean.TRUE.equals(isPicked()) && getChildCount() == 1) {
                    
                    //if this team is selected and the first team unit is being added to it, automatically select that first unit
                    newMemberItem.getCheckBox().setValue(true, true);
                }
                
                //a new team member was created, so update the map of global references
                ScenarioClientUtility.gatherTeamReferences();
                
                ScenarioEventUtility.fireDirtyEditorEvent(member);
                
                if(logger.isLoggable(Level.FINE)){
                    logger.fine("Finished adding team member");
                }
            }
        };
        
        addObjectMenu.getMenu().addItem(
                "<i class='fa fa-user scenarioTreeContextItem'></i>"
            +   "<span style='vertical-align: middle;'>"
            +       "Add learner role"
            +   "</span>", true, addTeamMemberCommand);
        
        if(logger.isLoggable(Level.FINE)){
            logger.fine("Initializing add button logic");
        }

        // button to add teams or team members
        Icon addButton = addButton(IconType.PLUS_CIRCLE, ADD_TEAM_AND_TEAM_MEMBER_TOOLTIP, new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                //stop propagation on the click event so that we don't re-select this item
                event.stopPropagation();
                
                addObjectMenu.showAtCurrentMousePosition(event);
            }
        });
        addButton.setVisible(!isReadOnly());
        
        if(logger.isLoggable(Level.FINE)){
            logger.fine("Initializing delete button logic");
        }
        
        if(!team.equals(ScenarioClientUtility.getTeamOrganization().getTeam())) {
            
            Icon cloneButton = addButton(IconType.CLONE, "Copy this team", new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    
                    if(getParentItem() != null && getParentItem().getTeamObject() instanceof Team) {
                    
                        Team clone = getTeamClone();
                        
                        //add the team member clone to this member's parent
                        Team parentTeam = ((Team) getParentItem().getTeamObject());
                        parentTeam.getTeamOrTeamMember().add(parentTeam.getTeamOrTeamMember().indexOf(team) + 1, clone);
                        
                        //create a new tree item for the cloned team member and place it after this item in the tree
                        getParentItem().insertItem(getParentItem().getChildIndex(EditableTeamTreeItem.this) + 1, new EditableTeamTreeItem(clone));
                    }
                }
                
            });
            cloneButton.setVisible(!isReadOnly());
            
            // button to delete this team
            Icon deleteButton = addButton(IconType.TRASH, "Delete this team", new ClickHandler() {
                
                @Override
                public void onClick(ClickEvent event) {
                    
                    if(logger.isLoggable(Level.FINE)){
                        logger.fine("Deleting team " + team.toString());
                    }
                    
                    //stop propagation on the click event so that we don't re-select this item
                    event.stopPropagation();
                    
                    final TreeItem parentItem = getParentItem();
                    
                    if(parentItem != null) {
                        
                        final Team parentTeam = ((TeamTreeItem) parentItem).getTeamObject();
                        
                        if(parentTeam != null){
                                
                            OkayCancelDialog.show(
                                "Delete Team?", 
                                "Are you sure you want to delete the team <b>" + ScenarioClientUtility.getScenarioObjectName(getTeamObject()) + "</b> from the team <b>" + parentTeam.getName() + "</b>?", 
                                "Delete Team", 
                                new OkayCancelCallback() {
                                    
                                    @Override
                                    public void okay() {
                                        
                                        if(logger.isLoggable(Level.FINE)){
                                            logger.fine("User confirmed delete");
                                        }
                                        
                                        final List<TeamReference> references = 
                                                ScenarioClientUtility.getReferencesToTeam(getTeamObject().getName());
                                        
                                        if(references == null || references.isEmpty()) {
                                            delete();
                                            
                                        } else {
                                            
                                            final TeamReferenceList referenceList = new TeamReferenceList();
                                            referenceList.getListEditor().replaceItems(references);
                                            referenceList.setOnJumpCommand(new Command() {
                                                
                                                @Override
                                                public void execute() {
                                                    
                                                    //close the current prompt if the author jumps to a condition
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
                                                    
                                                    //this team has references, so we need to ask the author if they want to remove these references first
                                                    OkayCancelDialog.show(
                                                            "Remove references?", 
                                                            refCount +" " + (refCount > 1 ? "triggers, conditions, and/or strategies are" : "trigger, condition, and/or strategy is") + " referencing this team. "
                                                            + "These references must be removed in order to delete this team."
                                                            + "<br/><br/>Do you want to remove these references and continue with the delete? "
                                                            + "<br/><br/>Triggers, conditions, and/or strategies that reference this team:", 
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

        echelonEditor.getElement().getStyle().setMarginTop(5, Unit.PX);
        
        echelonPanel.add(echelonEditor);
        
        echelonDeck.add(echelonPanel);
        
    }
    
    /**
     * Gets a deep clone of the team that this tree item represents
     * 
     * @return a deep clone of this item's team
     */
    public Team getTeamClone() {
        
        Team clone = new Team();
        
        if (echelonEditor.getValue() != null) {
            clone.setEchelon(echelonEditor.getValue().getName());
        }

        for(int i = 0; i < getChildCount(); i++) {
            
            TeamObjectTreeItem<?> child = (TeamObjectTreeItem<?>) getChild(i);
            
            if(child instanceof EditableTeamTreeItem) {
                
                //add a clone of the sub team as a child of this team's clone
                clone.getTeamOrTeamMember().add(((EditableTeamTreeItem) child).getTeamClone());
                
            } else if(child instanceof EditableTeamMemberTreeItem) {
                
                //add a clone of the team member as a child of this team's clone
                clone.getTeamOrTeamMember().add(((EditableTeamMemberTreeItem) child).getTeamMemberClone());
            }
        }
        
        String tentativeCloneName = getTeamObject().getName() + " - Copy";
        
        //ensure the team clone is given its own unique name
        int i = 1;
        String cloneName = tentativeCloneName;
        while(!ScenarioClientUtility.isTeamOrMemberNameValid(cloneName)) {
           cloneName = tentativeCloneName + " " + ++i;
        }
        
        clone.setName(cloneName);
        
        return clone;
    }
    
    /**
     * Deletes this tree item and its associated team member and removes all references to said team member
     */
    private void delete() {
        
        if(logger.isLoggable(Level.FINE)){
            logger.fine("Started deleting team");
        }
        
        final TreeItem parentItem = getParentItem();
        
        if(parentItem != null) {
            
            final Team parentTeam = ((TeamTreeItem) parentItem).getTeamObject();
            
            if(parentTeam != null){
        
                parentTeam.getTeamOrTeamMember().remove(getTeamObject());
                
                remove();
                
                // update references to this team whenever it is deleted
                ScenarioClientUtility.updateTeamReferences(getTeamObject().getName(), null);
                
                ScenarioEventUtility.fireDirtyEditorEvent(parentTeam);
            }
        }
        
        if(logger.isLoggable(Level.FINE)){
            logger.fine("Finished deleting team");
        }
    }
    
    @Override
    public void onDrop(TeamObjectTreeItem<? extends Serializable> dragged) {
        
        if(dragged != null) {
            
            if(dragged.getTeamObject() instanceof TeamMember) {
                
                TeamMember dragMember = (TeamMember) dragged.getTeamObject();
                
                //if a team member is dragged on top of this team, place the dragged team member inside this team
                Team fromParent = ((TeamTreeItem) dragged.getParentItem()).getTeamObject();
                
                //remove the dragged item from its original team
                fromParent.getTeamOrTeamMember().remove(dragMember);
                
                //add the dragged member to this team's list of members
                getTeamObject().getTeamOrTeamMember().add(0, dragMember);
                
                //remove the member's tree item from its parent and add it as a child of this team
                dragged.remove();
                insertItem(0, dragged);
                
                if(!getState()) {
                    setState(true);
                }
                
                //update the validation state of the objects affected by this move
                SharedResources.getInstance().getEventBus().fireEvent(new ScenarioEditorDirtyEvent(fromParent));
                
                if(!getTeamObject().equals(fromParent)) {
                    SharedResources.getInstance().getEventBus().fireEvent(new ScenarioEditorDirtyEvent(getTeamObject()));
                }
            
            } else if(dragged.getTeamObject() instanceof Team 
                    && !dragged.getTeamObject().equals(getTeamObject())
                    && getParentItem() != null) {
            
                Team dragTeam = (Team) dragged.getTeamObject();
                
                //if a team is dragged on top of this team, place the dragged team after this team
                Team fromParent = ((TeamTreeItem) dragged.getParentItem()).getTeamObject();
                Team toParent = ((TeamTreeItem) getParentItem()).getTeamObject();
                
                if(fromParent != null && toParent != null) {
                    
                    fromParent.getTeamOrTeamMember().remove(dragTeam);
                    
                    int dropIndex = toParent.getTeamOrTeamMember().indexOf(getTeamObject());
                        
                    toParent.getTeamOrTeamMember().add(dropIndex, dragTeam);
                    
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
    protected TeamTreeItem createTeamTreeItem(Team team) {
        return new EditableTeamTreeItem(team);
    }

    @Override
    protected TeamMemberTreeItem createTeamMemberTreeItem(TeamMember teamMember) {
        return new EditableTeamMemberTreeItem(teamMember);
    }

    @Override
    protected boolean isReadOnly() {
        return ScenarioClientUtility.isReadOnly();
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        
        updateVisibleEditor();
    }

    /**
     * Shows the echelon editor for this item if it is selected. If this item is not selected, the 
     * editor will be hidden.
     */
    private void updateVisibleEditor() {
        
        if(isSelected() && echelonDeck.getVisibleWidget() != echelonDeck.getWidgetIndex(echelonPanel)) {
            
            echelonDeck.showWidget(echelonDeck.getWidgetIndex(echelonPanel));
            
        } else {
            echelonDeck.showWidget(echelonDeck.getWidgetIndex(emptyPanel));
            echelonEditor.saveBackupValue();
        }
    }

    @Override
    protected boolean isObjectNameValid(Serializable object, String name) {
        return ScenarioClientUtility.isScenarioObjectNameValid(object, name);
    }

    @Override
    protected void updateTeamReferences(String oldName, String newName) {
        ScenarioClientUtility.updateTeamReferences(oldName, newName);
    }

    @Override
    protected void updateValidity() {
        isValid = ScenarioClientUtility.getValidationCache().isValid(object);
    }
}