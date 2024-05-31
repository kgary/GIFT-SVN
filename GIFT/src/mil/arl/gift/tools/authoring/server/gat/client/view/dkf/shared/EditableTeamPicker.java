/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.AttachEvent.Handler;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;

import generated.dkf.Team;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.TeamObjectTreeItem.PickMode;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.TeamPicker;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.TeamTreeItem;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.TeamRenamedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.header.editor.EditableTeamTreeItem;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.ScenarioEventUtility;

/**
 * A widget used to pick team members from the scenario's team organization. This widget can also be used to create new
 * teams, edit existing teams, or remove existing teams.
 *
 * @author nroberts
 */
public class EditableTeamPicker extends TeamPicker {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(EditableTeamPicker.class.getName());

    /** An event binder used to allow this widget to receive global events */
    interface EditableTeamPickerEventBinder extends EventBinder<EditableTeamPicker> {
    }

    /** The event binder that handles global events for this widget */
    private static final EditableTeamPickerEventBinder eventBinder = GWT.create(EditableTeamPickerEventBinder.class);

    /** The event binder registration */
    private com.google.web.bindery.event.shared.HandlerRegistration eventBinderRegistration;

    /**
     * Creates a new team picker and initializes its event handling logic
     */
    public EditableTeamPicker() {
        this(false);
    }

    /**
     * Creates a new team picker, initializes its event handling logic, and optionally selects team members
     * for the author automatically if none are explicitly selected.
     * 
     * @param mustSelectTeamMember whether this picker should automatically select team members if the author
     * doesn't explicitly select any
     */
    public EditableTeamPicker(boolean mustSelectTeamMember) {
        this(mustSelectTeamMember, PickMode.MULTIPLE);
    }
    /**
     * Creates a new team picker, initializes its event handling logic, and optionally selects team members
     * for the author automatically if none are explicitly selected.
     * 
     * @param mustPickTeamMember whether this picker should automatically pick team members if the author
     * doesn't explicitly select any
     * @param pickMode the mode this picker should use to pick team members
     */
    public EditableTeamPicker(boolean mustPickTeamMember, PickMode pickMode) {
        this(mustPickTeamMember, false, pickMode);
    }

    /**
     * Creates a new team picker, initializes its event handling logic, and optionally selects team members
     * for the author automatically if none are explicitly selected.
     * 
     * @param mustPickTeamMember whether this picker should automatically pick team members if the author
     * doesn't explicitly select any
     * @param mustUseEntityMarkers whether the team members picked by this picker must use entity markers to
     * identify themselves in the training application. If true, validation errors will be shown if the author
     * picks a team member that does not use a entity marker.
     * @param pickMode the mode this picker should use to pick team members
     */
    public EditableTeamPicker(boolean mustPickTeamMember, boolean mustUseEntityMarkers, PickMode pickMode) {
        super(ScenarioClientUtility.getTeamOrganizationTeam(true), mustPickTeamMember, mustUseEntityMarkers,
                pickMode);
        
        logger.info("Creating EditableTeamPicker");
        
        setActive(true);

        nodeJumpButton.setVisible(true);

        eventBinderRegistration = eventBinder.bindEventHandlers(this, SharedResources.getInstance().getEventBus());

        addAttachHandler(new Handler() {
            @Override
            public void onAttachOrDetach(AttachEvent event) {
                final boolean attached = event.isAttached();
                final boolean hasHandler = eventBinderRegistration != null;

                /* add the event binder to this picker if it is attached and
                 * does not have the event binder already */
                if (attached && !hasHandler) {
                    eventBinderRegistration = eventBinder.bindEventHandlers(EditableTeamPicker.this,
                            SharedResources.getInstance().getEventBus());
                } else if (!attached && hasHandler) {
                    /* remove the event binder from this picker if it is
                     * detached */
                    eventBinderRegistration.removeHandler();
                    eventBinderRegistration = null;
                }
            }
        });

    }

    @Override
    public void setValue(List<String> value, boolean fireEvents) {
        super.setValue(value, fireEvents);
    }

    /**
     * Updates the team picker when one of the teams or team members it is referencing is renamed
     *
     * @param event an event indicating that a team or team member has been renamed
     */
    @EventHandler
    protected void onTeamRenamedEvent(TeamRenamedEvent event) {
        
        if(StringUtils.isNotBlank(event.getOldName())) {
            
            if (getValue() != null && getValue().contains(event.getOldName())) {
                
                // if the old team name is found in the team picker value, then update it to the
                // new team name
                List<String> newValue = new ArrayList<>(getValue());
                
                if(StringUtils.isBlank(event.getNewName())){
                    newValue.remove(event.getOldName());
                    
                } else {
                    newValue.set(newValue.indexOf(event.getOldName()), event.getNewName());
                }
                
                setValue(newValue);
                
            } else if (getDisplayedValue().contains(event.getOldName())) {
                
                // if the old team name is found in the team picker's DISPLAY value, then refresh the display
                refreshDisplayedValue(getValue());
                
                requestValidation(selectedTeamsEmptyValidation);
            }
        }
    }

    @Override
    public Team getTeam() {
        return ScenarioClientUtility.getTeamOrganizationTeam();
    }

    @Override
    protected void performNodeJump() {
        ScenarioEventUtility.fireJumpToTeamOrganization();
    }
    
    @Override
    protected TeamTreeItem createTeamTreeItem() {
        return new EditableTeamTreeItem(getTeam());
    }

    @Override
    protected void fireDirtyEvent(Serializable sourceObject) {
        ScenarioEventUtility.fireDirtyEditorEvent(sourceObject);
    }
}
