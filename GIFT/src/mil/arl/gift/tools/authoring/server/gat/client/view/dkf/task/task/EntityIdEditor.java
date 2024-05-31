/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.task;

import java.io.Serializable;
import java.util.Set;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.constants.IconType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.EntityLocation.EntityId;
import generated.dkf.LearnerId;
import generated.dkf.StartLocation;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.header.editor.LearnerIdEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared.Ribbon;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared.TeamMemberPicker;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.validation.ScenarioValidationComposite;

/**
 * A widget that provides a user interface to allow the author to edit and {@link EntityId}.
 * 
 * @author nroberts
 */
public class EntityIdEditor extends ScenarioValidationComposite {

    /** The UiBinder that combines the ui.xml with this java class */
    private static EntityIdEditorUiBinder uiBinder = GWT.create(EntityIdEditorUiBinder.class);
    
    /** Team member picker used to pick a learner role to identify the entity */
    @UiField(provided = true)
    protected TeamMemberPicker teamMemberPicker = new TeamMemberPicker(ScenarioClientUtility.isLearnerIdRequiredByApplication());
    
    /** The deck panel used to switch between the sub-editors for each entity ID type*/
    @UiField
    protected DeckPanel mainDeck;
    
    /** A ribbon used to select the entity ID type */
    @UiField
    protected Ribbon typeRibbon;
    
    /** The editor used to modify the learner ID type */
    @UiField
    protected LearnerIdEditor learnerIdEditor;
    
    /** The panel containing the learner ID editor */
    @UiField
    protected FlowPanel learnerIdPanel;
    
    /** The entity ID currently being edited by this widget */
    private EntityId entityId;
    
    /** A button used to change the entity ID type after a type has been selected */
    @UiField
    protected Button changeTypeButton;

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface EntityIdEditorUiBinder extends UiBinder<Widget, EntityIdEditor> {
    }

    /**
     * Creates a new widget that can edit Entity IDs
     */
    public EntityIdEditor() {
        initWidget(uiBinder.createAndBindUi(this));
        
        typeRibbon.addRibbonItem(
                IconType.USER, 
                "Learner Entity", 
                "Track a learner-controlled entity in the training application", 
                new ClickHandler() {
                    
                    @Override
                    public void onClick(ClickEvent event) {
                        
                        EntityId.TeamMemberRef memberRef = new EntityId.TeamMemberRef();
                        
                        entityId.setTeamMemberRefOrLearnerId(memberRef);
                        populateMemberRef(memberRef);
                    }
                });
        
        if(ScenarioClientUtility.getScenario().getResources().getSourcePath() == null) {
			typeRibbon.addRibbonItem(IconType.USER_SECRET, "Other Entity",
					"Use a non-learner entity in the training application", new ClickHandler() {

						@Override
						public void onClick(ClickEvent event) {

							LearnerId learnerId = new LearnerId();

							entityId.setTeamMemberRefOrLearnerId(learnerId);
							populateLearnerId(learnerId);
						}
					});
        }
        
        changeTypeButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                entityId.setTeamMemberRefOrLearnerId(null);
                mainDeck.showWidget(mainDeck.getWidgetIndex(typeRibbon));
                changeTypeButton.setVisible(false);
                
                teamMemberPicker.setActive(false);
            }
        });
        
        teamMemberPicker.addValueChangeHandler(new ValueChangeHandler<String>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                
                String selectedRole = event.getValue();

                if(!(entityId.getTeamMemberRefOrLearnerId() instanceof EntityId.TeamMemberRef)) {
                    entityId.setTeamMemberRefOrLearnerId(new EntityId.TeamMemberRef());
                }
                
                EntityId.TeamMemberRef memberRef = (EntityId.TeamMemberRef) entityId.getTeamMemberRefOrLearnerId();
                
                //update the backing data model with the referenced team member name
                if(StringUtils.isNotBlank(selectedRole)) {
                    memberRef.setValue(selectedRole);
                    
                } else {
                    memberRef.setValue(null);
                }
            }
        });
        
        //show the type ribbon by default
        mainDeck.showWidget(mainDeck.getWidgetIndex(typeRibbon));
        changeTypeButton.setVisible(false);
    }
    
    /**
     * Return whether the ribbon is currently shown in this editor that allows the author to
     * choose the type of entity identification (e.g. learner or entity id)
     * @return true if the ribbon is shown (i.e. a choice was not made yet), or false if a choice/object is provided
     */
    public boolean isTypeRibbonShown() {
        return mainDeck.getVisibleWidget() == mainDeck.getWidgetIndex(typeRibbon);
    }
    
    /**
     * Return the ribbon used to show the choices of entity identification (e.g. learner or entity id)
     * @return the ribbon widget shown in this editor
     */
    public Ribbon getTypeRibbon() {
        return typeRibbon;
    }

    /**
     * Loads the given entity ID into this editor so that the author can modify it using the provided user
     * interface controls
     * 
     * @param entityId the entity ID to be edited
     */
    public void edit(EntityId entityId) {
        
        if(entityId == null) {
            entityId = new EntityId();
        }

        /* Create a new entity id instance to be edited since we don't want to
         * modify the original */
        this.entityId = new EntityId();

        if (entityId.getTeamMemberRefOrLearnerId() instanceof EntityId.TeamMemberRef) {
            /* Copy team member ref */
            EntityId.TeamMemberRef sourceMember = (EntityId.TeamMemberRef) entityId.getTeamMemberRefOrLearnerId();
            EntityId.TeamMemberRef newMember = new EntityId.TeamMemberRef();

            newMember.setValue(sourceMember.getValue());

            this.entityId.setTeamMemberRefOrLearnerId(newMember);
            populateMemberRef(newMember);

        } else if (entityId.getTeamMemberRefOrLearnerId() instanceof LearnerId) {
            /* Copy learner id */
            LearnerId sourceLearnerId = (LearnerId) entityId.getTeamMemberRefOrLearnerId();
            LearnerId newLearnerId = new LearnerId();

            final Serializable type = sourceLearnerId.getType();
            if (type instanceof StartLocation) {
                StartLocation sourceLocation = (StartLocation) type;
                StartLocation newLocation = new StartLocation();
                newLocation.setCoordinate(sourceLocation.getCoordinate());
                newLearnerId.setType(newLocation);
            } else if (type instanceof String) {
                String typeString = (String) type;
                newLearnerId.setType(typeString);
            }

            this.entityId.setTeamMemberRefOrLearnerId(newLearnerId);
            populateLearnerId(newLearnerId);

        } else {
            
            mainDeck.showWidget(mainDeck.getWidgetIndex(typeRibbon));
            changeTypeButton.setVisible(false);
            
            teamMemberPicker.setActive(false);
        }
    }
    
    /**
     * Loads the given team member reference into the editor and shows the team member reference editor to the author
     * 
     * @param location the team member reference to load. Cannot be null.
     */
    private void populateMemberRef(EntityId.TeamMemberRef location) {
        
        String memberRef = location.getValue();
        
        //the current training app requires a team member learner ID, so load any that are found
        teamMemberPicker.setValue(memberRef);
        
        //update the backing value in case the picker automatically provides a new value
        location.setValue(teamMemberPicker.getValue());
        
        teamMemberPicker.setValue(location.getValue());
        
        mainDeck.showWidget(mainDeck.getWidgetIndex(teamMemberPicker));
        changeTypeButton.setVisible(true);
        
        teamMemberPicker.setActive(true);
    }
    
    /**
     * Loads the given learner ID into the editor and shows the learner ID editor to the author
     * 
     * @param learnerId the learner ID name to load
     */
    private void populateLearnerId(LearnerId learnerId) {
        
        learnerIdEditor.edit(learnerId);
        
        mainDeck.showWidget(mainDeck.getWidgetIndex(learnerIdPanel));
        changeTypeButton.setVisible(true);
        
        teamMemberPicker.setActive(false);
    }
    
    /**
     * Updates the components in the editor based on the provided read-only flag.
     * 
     * @param isReadonly true to set the components as read-only.
     */
    public void setReadOnly(boolean isReadonly) {
        typeRibbon.setReadonly(isReadonly);
        teamMemberPicker.setReadonly(isReadonly);
        learnerIdEditor.setReadOnly(isReadonly);
        changeTypeButton.setEnabled(!isReadonly);
    }
    
    /**
     * Gets the entity ID currently being edited
     * 
     * @return the entity ID being edited
     */
    public EntityId getEntityId() {
        return entityId;
    }

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        //nothing to validate
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        //nothing to validate
    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
        childValidationComposites.add(teamMemberPicker);
    }
}
