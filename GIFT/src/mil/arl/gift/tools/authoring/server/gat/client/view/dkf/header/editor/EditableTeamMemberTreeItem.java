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
import org.gwtbootstrap3.client.ui.constants.Trigger;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TreeItem;

import generated.dkf.LearnerId;
import generated.dkf.Team;
import generated.dkf.TeamMember;
import mil.arl.gift.common.enums.TrainingApplicationEnum;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.TeamMemberTreeItem;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.TeamObjectTreeItem;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.TeamTreeItem;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.ScenarioEditorDirtyEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.ScenarioEventUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelDialog;

/**
 * A tree item that represents a team member in the team organization outline and allows the author to rename it, remove it, and edit it
 *
 * @author nroberts
 */
public class EditableTeamMemberTreeItem extends TeamMemberTreeItem {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(EditableTeamMemberTreeItem.class.getName());
    
    /** A panel used to show the learner ID editor */
    private FlowPanel learnerIdPanel = new FlowPanel();
    
    /** The editor used to edit this team member's learner ID */
    private LearnerIdEditor learnerIdEditor = new LearnerIdEditor(new LearnerIdEditor.CancelCallback() {
        
        @Override
        public void onEditingCancelled(LearnerId backupCopy) {
            
            getTree().setSelectedItem(null, true);
           
            //since editing the learner ID was cancelled, need to revert the data model to the backup copy
            getTeamObject().setLearnerId(backupCopy);
            
            ScenarioEventUtility.fireDirtyEditorEvent(getTeamObject()); //re-validate when the learner ID changes
        }
    });

    /**
     * Creates a new tree item that represents and modifies the given team member
     *
     * @param teamMember the team member to represent and modify
     */
    public EditableTeamMemberTreeItem(final TeamMember teamMember) {
        super(teamMember, !GatClientUtility.isReadOnly());

        getNameLabel().setEditingEnabled(!isReadOnly());
        
        /* Disables the button panel if the course was imported with an xTSP */
        if(ScenarioClientUtility.getScenario().getResources().getSourcePath() != null) {
        	super.buttonPanel.setVisible(false);
        	learnerIdEditor.setReadOnly(true);
        }
        
        if (GatClientUtility.isReadOnly()) {
            learnerIdEditor.setReadOnly(true);
            playableIcons.removeStyleName(CSS.teamOrganizationPlayableIcon());
            playableIcons.addStyleName(CSS.teamOrganizationPlayableIconDisabled());
            playableTooltip.setTrigger(Trigger.MANUAL);
        }
        
        playableIcons.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {

                if (GatClientUtility.isReadOnly()) {
                    return;
                }

                teamMember.setPlayable(!teamMember.isPlayable());
                
                updatePlayableIcon();
            }
            
        }, ClickEvent.getType());
        
        learnerIdEditor.getElement().getStyle().setMarginTop(5, Unit.PX);
        learnerIdEditor.setLearnerIdChangedCommand(new Command() {
            
            @Override
            public void execute() {
                
                // update the data model of objects that have this entity marking value but whose editors aren't open right now
                if(teamMember.getLearnerId() != null &&
                        teamMember.getLearnerId().getType() instanceof String){
                    ScenarioClientUtility.updateTeamMemberEntityMarkingReferences(teamMember.getName(), (String)teamMember.getLearnerId().getType());
                }
                
                ScenarioEventUtility.fireDirtyEditorEvent(teamMember); //re-validate when the learner ID changes
            }
        });
        
        learnerIdPanel.add(learnerIdEditor);
        
        typeDeck.add(learnerIdPanel);
        
        learnerIdEditor.edit(teamMember.getLearnerId());
        
        onRename();
        
        updateVisibleEditor();
        
        Icon addButton = addButton(IconType.CLONE, "Copy this learner role", new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                
                if(getParentItem() != null && getParentItem().getTeamObject() instanceof Team) {
                
                    TeamMember clone = getTeamMemberClone();
                    
                    //add the team member clone to this member's parent
                    Team parentTeam = ((Team) getParentItem().getTeamObject());
                    parentTeam.getTeamOrTeamMember().add(parentTeam.getTeamOrTeamMember().indexOf(teamMember) + 1, clone);
                    
                    //create a new tree item for the cloned team member and place it after this item in the tree
                    getParentItem().insertItem(getParentItem().getChildIndex(EditableTeamMemberTreeItem.this) + 1, new EditableTeamMemberTreeItem(clone));
                }
            }
            
        });
        addButton.setVisible(!isReadOnly());

        // button to delete this team member
        Icon deleteButton = addButton(IconType.TRASH, "Delete this learner role", new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                if(logger.isLoggable(Level.FINE)){
                    logger.fine("Deleting team member" + teamMember.toString());
                }
                
                //stop propagation on the click event so that we don't re-select this item
                event.stopPropagation();
                
                String parentName = null;
                TeamObjectTreeItem<?> parentItem = getParentItem();
                if(parentItem != null){
                    parentName = ScenarioClientUtility.getScenarioObjectName(parentItem.getTeamObject());
                }
                       
                OkayCancelDialog.show(
                    "Delete Learner Role?", 
                    "Are you sure you want to delete the team member <b>" + ScenarioClientUtility.getScenarioObjectName(getTeamObject()) + "</b>" + 
                            (parentName != null ? " from the team <b>" + parentName + "</b>" : "") + "?", 
                    "Delete Learner Role", 
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
                                                refCount +" " + (refCount > 1 ? "triggers, conditions and/or strategies are" : "trigger, condition, or strategy is") + " referencing this learner. "
                                                + "These references must be removed in order to delete this learner."
                                                + "<br/><br/>Do you want to remove these references and continue with the delete? "
                                                + "<br/><br/>Triggers, conditions, and/or strategies that reference this learner:", 
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
        });
        deleteButton.setVisible(!isReadOnly());

        if(logger.isLoggable(Level.FINE)){
            logger.fine("Finished constructing team tree item");
        }
    }

    /**
     * Gets a deep clone of the team member that this tree item represents
     * 
     * @return a deep clone of this item's team member
     */
    public TeamMember getTeamMemberClone() {
        
        TeamMember clone = new TeamMember();
        clone.setPlayable(getTeamObject().isPlayable());
        clone.setLearnerId(learnerIdEditor.getLearnerIdCopy());
        
        String tentativeCloneName = getTeamObject().getName() + " - Copy";
        
        //ensure the team member clone is given its own unique name
        int i = 1;
        String cloneName = tentativeCloneName;
        while(!ScenarioClientUtility.isTeamOrMemberNameValid(cloneName)) {
           cloneName = tentativeCloneName + " " + ++i;
        }
        
        clone.setName(cloneName);
        
        return clone;
    }

    @Override
    protected String getPlayableTooltip() {
        return "This entity is playable.<br/>Click to make it unplayable.";
    }

    @Override
    protected String getUnplayableTooltip() {
        return "This entity is unplayable.<br/>Click to make it playable.";
    }

    /**
     * Deletes this tree item and its associated team member and removes all references to said team member
     */
    private void delete() {
        
        if(logger.isLoggable(Level.FINE)){
            logger.fine("Started deleting team member");
        }
        
        final TreeItem parentItem = getParentItem();
        
        if(parentItem != null) {
            
            final Team parentTeam = ((TeamTreeItem) parentItem).getTeamObject();
            
            if(parentTeam != null){
        
                parentTeam.getTeamOrTeamMember().remove(getTeamObject());
                
                remove();
                
                // update references to this team member whenever it is deleted
                ScenarioClientUtility.updateTeamReferences(getTeamObject().getName(), null);
                
                ScenarioEventUtility.fireDirtyEditorEvent(parentTeam);
            }
        }
        
        if(logger.isLoggable(Level.FINE)){
            logger.fine("Finished deleting team member");
        }
    }
    
    @Override
    public void onDrop(TeamObjectTreeItem<? extends Serializable> dragged) {

        if(dragged != null
                && (dragged.getTeamObject() instanceof TeamMember || dragged.getTeamObject() instanceof Team)
                && !dragged.getTeamObject().equals(getTeamObject())) {

            Serializable dragUnit = dragged.getTeamObject();

            //if a team object is dragged on top of this team member, place the dragged object after this member
            Team fromParent = ((TeamTreeItem) dragged.getParentItem()).getTeamObject();
            Team toParent = ((TeamTreeItem) getParentItem()).getTeamObject();

            if(fromParent != null && toParent != null) {
                
                fromParent.getTeamOrTeamMember().remove(dragUnit);
                
                int dropIndex = toParent.getTeamOrTeamMember().indexOf(getTeamObject());
                    
                toParent.getTeamOrTeamMember().add(dropIndex, dragUnit);
                
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
    
    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        
        updateVisibleEditor();
    }

    /**
     * Shows the appropriate editor for this item's learner ID if this item is selected. If this item is not selected, the 
     * editor will be hidden.
     */
    private void updateVisibleEditor() {
        
        if(isSelected() && typeDeck.getVisibleWidget() != typeDeck.getWidgetIndex(learnerIdPanel)) {
            
            typeDeck.showWidget(typeDeck.getWidgetIndex(learnerIdPanel));
            
            //reload the learner ID editor
            learnerIdEditor.edit(getTeamObject().getLearnerId());
            
        } else {
            typeDeck.showWidget(typeDeck.getWidgetIndex(emptyPanel));
        }
    }
    
    @Override
    protected void onRename() {
        
        TrainingApplicationEnum taType = ScenarioClientUtility.getTrainingAppType();
        StringBuilder labelBuilder = new StringBuilder();
        labelBuilder.append("<html>The unique name of the entity being played by ").append(getTeamObject().getName())
            .append(" in ").append(taType.getDisplayName()).append(":");

        // DIS protocol only allows 10 character limit for entity markings (URN markings), the rest is truncated.
        if(taType.equals(TrainingApplicationEnum.VBS) || taType.equals(TrainingApplicationEnum.VR_ENGAGE)){
            labelBuilder.append("<br/><font color=\"blue\">10 character limit for DIS protocol</font>");
            
            learnerIdEditor.setEntityMarkerCharacterLimit(10);
        }
            
        labelBuilder.append("</html>");
        learnerIdEditor.setEntityMarkerLabel(labelBuilder.toString());
    }

    @Override
    protected boolean isReadOnly() {
        return ScenarioClientUtility.isReadOnly();
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