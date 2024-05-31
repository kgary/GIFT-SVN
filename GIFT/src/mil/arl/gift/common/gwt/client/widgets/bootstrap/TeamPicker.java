/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets.bootstrap;

import static mil.arl.gift.common.util.StringUtils.join;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Collapse;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.extras.tagsinput.client.ui.MVTagsInput;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.HasDirection.Direction;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.Team;
import generated.dkf.TeamMember;
import mil.arl.gift.common.gwt.client.event.InputEvent;
import mil.arl.gift.common.gwt.client.event.InputHandler;
import mil.arl.gift.common.gwt.client.util.TeamsUtil;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.gwt.client.validation.WidgetValidationStatus;
import mil.arl.gift.common.gwt.client.widgets.BlockerPanel;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.TeamObjectTreeItem.PickMode;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.common.util.StringUtils;

/**
 * A widget used to pick team members from the team organization.
 *
 * @author nroberts
 */
public class TeamPicker extends ValidationComposite implements HasValue<List<String>> {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(TeamPicker.class.getName());

    /** The UiBinder that combines the ui.xml with this java class */
    private static TeamPickerUiBinder uiBinder = GWT.create(TeamPickerUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface TeamPickerUiBinder extends UiBinder<Widget, TeamPicker> {
    }

    /** A text box used to search for team members to select */
    @UiField
    protected TextBox teamSelect;
    
    /** The tags showing the selected teams or team members */
    @UiField
    protected MVTagsInput teamTags;
    
    /** Used to block the removal of team tags when read only */
    @UiField
    protected BlockerPanel teamTagPanelBlocker;

    /** The list-filtering command that is scheduled to be executed after keystrokes in the selection box*/
    private ScheduledCommand scheduledFilter = null;

    /** The button used to jump to the selected task/concept page */
    @UiField(provided = true)
    protected EnforcedButton nodeJumpButton = new EnforcedButton(IconType.EXTERNAL_LINK, "", "Navigates to the team organization page",
            new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    performNodeJump();

                    //hide the team selector so that it doesn't block the editor that the author jumps to
                    TeamSelectionPanel.hideSelector();
                }
            });

    /** The collapse containing a panel for selecting a {@link Point} */
    @UiField
    protected Collapse selectorPanel;

    /** The label that is used to annotate the picker field */
    @UiField
    protected HTML label;
    
    /** The picker input panel */
    @UiField
    protected Widget pickerInputPanel;

    /** Validation container for when the {@link TeamPicker} has invalid teams selected. */
    protected final WidgetValidationStatus teamSelectedValidation;
    
    /** Validation container for when the {@link TeamPicker} has teams selected that do not have a single child team member. */
    protected final WidgetValidationStatus selectedTeamsEmptyValidation;
    
    /** Validation container for when no team members have been selected and no team members are available */
    protected final WidgetValidationStatus teamMemberAvailableValidation;
    
    /** Validation container for when entity markers are required but no team members have any defined */
    protected final WidgetValidationStatus noEntityMarkersValidation;
    
    /** Validation container for when entity markers are required but at least ons selected team member doesn't use one */
    protected final WidgetValidationStatus entityMarkerMissingValidation;

    /** Validation container for when a team member does not exist in the team organization but set into this widget */
    protected final WidgetValidationStatus noSuchTeamMemberValidation;

    /**
     * Whether or not changes made to the tags input should be handled by this widget. The tags input doesn't provide a way to set
     * its value without firing a {@link ValueChangeEvent}, so this flag is used as a workaround for operations where we don't 
     * want to fire such an event, such as loading.
     */
    private boolean shouldHandleTagValueChange;
    
    /** 
     * The current value of this widget (i.e. the selected team names). This needs to maintained separately from the tags 
     * input so that it can still be read and modified even when the tags input isn't attached.
     */
    private List<String> value;

    /** 
     * Whether this picker's value MUST remain a non-null, non-empty list of team member names if at all possible. This 
     * effectively prevents the author from not selecting any team members, since if the author does not explicitly pick
     * anything, a default value will be provided for them. If they have not selected anything, then the root team will
     * be used as the default. If they made a valid selection earlier, then the last valid selection will be used as the
     * default.
     * <br/><br/>
     * Note that this picker's value can still be null or empty if there are absolutely no team members to pick from,
     * which will cause a validation error to be shown to the author indicating that they need to add a team member.
     */
    private boolean mustPickTeamMember;

    /** The mode that this picker should use to pick team members */
    private PickMode pickMode = PickMode.MULTIPLE;

    /** Whether the selected team members must identify themselves in the training app using entity markers */
    private boolean mustUseEntityMarkers;

    /**
     * Maintaining a collection of the selected values. This is needed because
     * {@link #teamTags} only performs operations (add, removeAll, etc...) if
     * the widget is attached, so these will not work when populating and
     * validating externally. Using a set because {@link #teamTags} does not
     * allow duplicates and this will replicate that behavior
     */
    private Set<String> displayedValues = new HashSet<>();
    
    /** The organizational team */
    private final Team orgTeam;
    
    /**
     * Creates a new team picker and initializes its event handling logic
     * 
     * @param team the organizational team. Can't be null.
     */
    public TeamPicker(Team team) {
        this(team, false);
    }
    
    /**
     * Creates a new team picker, initializes its event handling logic, and optionally selects team members
     * for the author automatically if none are explicitly selected.
     * 
     * @param team the organizational team. Can't be null.
     * @param mustSelectTeamMember whether this picker should automatically select team members if the author
     * doesn't explicitly select any
     */
    public TeamPicker(Team team, boolean mustSelectTeamMember) {
        this(team, mustSelectTeamMember, PickMode.MULTIPLE);
    }
    /**
     * Creates a new team picker, initializes its event handling logic, and optionally selects team members
     * for the author automatically if none are explicitly selected.
     * 
     * @param team the organizational team. Can't be null.
     * @param mustPickTeamMember whether this picker should automatically pick team members if the author
     * doesn't explicitly select any
     * @param pickMode the mode this picker should use to pick team members
     */
    public TeamPicker(Team team, boolean mustPickTeamMember, PickMode pickMode) {
        this(team, mustPickTeamMember, false, pickMode);
    }

    /**
     * Creates a new team picker, initializes its event handling logic, and optionally selects team members
     * for the author automatically if none are explicitly selected.
     * 
     * @param team the organizational team. Can't be null.
     * @param mustPickTeamMember whether this picker should automatically pick team members if the author
     * doesn't explicitly select any
     * @param mustUseEntityMarkers whether the team members picked by this picker must use entity markers to
     * identify themselves in the training application. If true, validation errors will be shown if the author
     * picks a team member that does not use a entity marker.
     * @param pickMode the mode this picker should use to pick team members
     */
    public TeamPicker(Team team, boolean mustPickTeamMember, boolean mustUseEntityMarkers, PickMode pickMode) {
        if (team == null) {
            throw new IllegalArgumentException("The parameter 'team' cannot be null.");
        }

        if(pickMode == null) {
            pickMode = PickMode.MULTIPLE;
        }

        initWidget(uiBinder.createAndBindUi(this));
        setActive(false);
        
        
        this.orgTeam = team;
        
        teamSelectedValidation = new WidgetValidationStatus(pickerInputPanel,
                "One or more of the selected team members were not found in the team organization. "
                + "Please re-select team members from the team organization.");
        
        selectedTeamsEmptyValidation = new WidgetValidationStatus(pickerInputPanel,
                "No team members have been selected. Please select at least one team member.");
        
        teamMemberAvailableValidation = new WidgetValidationStatus(pickerInputPanel,
                "No team members are available. Please add at least one team member corresponding "
                + "to an actor in the training application. ");
        
        noEntityMarkersValidation = new WidgetValidationStatus(pickerInputPanel,
                "No team members have entity markers. Please add at least one team member that uses an entity "
                + "marker to identify itself in the training application.");
        
        entityMarkerMissingValidation = new WidgetValidationStatus(pickerInputPanel,
                "One or more of the selected team members do not use entity markers. Please "
                + "select a team member that identifies itself in the training application with an entity marker.");

        noSuchTeamMemberValidation = new WidgetValidationStatus(pickerInputPanel,
                "One or more team members were set into the team picker but do not exist in the team organization.");

        this.mustPickTeamMember = mustPickTeamMember;
        this.mustUseEntityMarkers = mustUseEntityMarkers;
        this.pickMode = pickMode;

        teamSelect.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                /* show the team selection dropdown whenever the search box is
                 * selected */
                TeamSelectionPanel.showSelector(TeamPicker.this);
            }
        });

        teamSelect.addDomHandler(new InputHandler() {

            @Override
            public void onInput(InputEvent event) {

                if (scheduledFilter == null) {

                    // Schedule a filter operation for the list. We don't want
                    // to perform the filter operation immediately because
                    // it can cause some slight input lag if the user presses
                    // several keys in quick succession or holds a key down.
                    scheduledFilter = new ScheduledCommand() {

                        @Override
                        public void execute() {

                            // update the filter for the selection dropdown
                            TeamSelectionPanel.loadAndFilterTeams(TeamPicker.this);

                            // allow the filter operation to be applied again,
                            // since it is finished
                            scheduledFilter = null;
                        }
                    };

                    Scheduler.get().scheduleDeferred(scheduledFilter);
                }
            }

        }, InputEvent.getType());
        teamSelect.addFocusHandler(new FocusHandler() {

            @Override
            public void onFocus(FocusEvent event) {

                // select all of the search box's text when it gains focus so
                // that it's easier for the author to clear out
                teamSelect.selectAll();
            }

        });

        /* If the value changes, remove the validation for setting an invalid
         * team member */
        addValueChangeHandler(new ValueChangeHandler<List<String>>() {
            @Override
            public void onValueChange(ValueChangeEvent<List<String>> event) {
                noSuchTeamMemberValidation.setValid();
            }
        });
    }

    /**
     * Handles the selection changes for the team and team member tags.
     *
     * @param event the value change event containing the new selection
     */
    @UiHandler("teamTags")
    protected void onTeamTagsChanged(ValueChangeEvent<List<String>> event) {
        
        if(shouldHandleTagValueChange) {
            
            List<String> tagsValue = teamTags.getValue();
            
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("onTeamTagsChanged.onValueChange(" + tagsValue + ")");
            }
    
            //the author has chosen valid teams, so update this widget's value
            setValue(tagsValue, true);
        }
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<List<String>> handler) {
        return this.addHandler(handler, ValueChangeEvent.getType());
    }

    @Override
    public List<String> getValue() {
        return this.value;
    }

    @Override
    public void setValue(List<String> value) {
        setValue(value, false);
    }

    @Override
    public void setValue(List<String> value, boolean fireEvents) {
        if (logger.isLoggable(Level.FINE)) {
            StringBuilder sb = new StringBuilder("setValue(");
            List<Object> params = Arrays.<Object>asList(value, fireEvents);
            join(", ", params, sb);
            logger.fine(sb.append(")").toString());
        }

        /* Ensure no nulls in the list. I've seen MVInputTags do this before. */
        while (value != null && value.contains(null)) {
            value.remove(null);
        }

        /* Check if we are trying to set a team member that is not part of the
         * team org */
        if (value != null) {
            Set<String> invalidMembers = TeamsUtil.findInvalidMembers(value, orgTeam);

            if (CollectionUtils.isEmpty(invalidMembers)) {
                noSuchTeamMemberValidation.setValid();
            } else {
                noSuchTeamMemberValidation.setErrorMessage("The team members {" + join(", ", invalidMembers)
                        + "} were set into the team picker but do not exist in the team organization. Add the member into the team organization or reselect the desired members.");
                noSuchTeamMemberValidation.setInvalid();
            }
        }

        boolean changedToDefault = false;
        List<String> newValue;
        if(PickMode.SINGLE.equals(pickMode)) {
            
            newValue = TeamsUtil.getTeamMemberNames(value, getTeam(), false);
            if(CollectionUtils.isNotEmpty(newValue)) {
                
                //only one team member can be picked, so remove all but the first
                String firstValue = newValue.get(0);
                
                newValue.clear();
                newValue.add(firstValue);
                
            } else {
                newValue = null;
            }
            
            if(mustPickTeamMember && newValue == null && !fireEvents) {
                //the value is null but a null value is not allowed, so use a default value if possible
                //(don't do this if the author has explicitly removed items, i.e. fireEvents is true)
                newValue = TeamsUtil.getTeamMemberNames(getValue(), getTeam(), false);
                
                if(CollectionUtils.isNotEmpty(newValue)) {
                    
                    //the current value is not empty, so simply prevent it from changing to an empty value
                    String firstValue = newValue.get(0);
                    
                    newValue.clear();
                    newValue.add(firstValue);
                    changedToDefault = true;
                } else {
                
                    //the current value is also empty, so change it to use an arbitrary team member
                    String defaultTeamMember = TeamsUtil.getAnyTeamMemberName(getTeam());
                    
                    if(defaultTeamMember != null) {
                        
                        newValue = new ArrayList<>();
                        newValue.add(defaultTeamMember);
                        changedToDefault = true;
                    }
                }
            }
            
        } else {
            
            newValue = TeamsUtil.getTopMostTeamNames(value, getTeam()); //use top-most names to remove invalid team names
            if (mustPickTeamMember && CollectionUtils.isEmpty(newValue) && !fireEvents) {
                //the new value is empty but empty values are not allowed, so use a default value if possible
                //(don't do this if the author has explicitly removed items, i.e. fireEvents is true)
                newValue = TeamsUtil.getTopMostTeamNames(this.value, getTeam());
                
                if(CollectionUtils.isNotEmpty(newValue)) {
                    changedToDefault = true;
                } else {
                    
                    //the current value is also empty, so change it to use the scenario's default team
                    Team defaultTeam = getTeam();
                    
                    List<String> defaultList = new ArrayList<>();
                    defaultList.add(defaultTeam.getName());
                    
                    newValue = TeamsUtil.getTopMostTeamNames(defaultList, getTeam());
                    changedToDefault = true;
                }
            }
        }

        /* Since we are manually updating the UI to be valid, force the
         * validation widget to be valid as well. ValidateAll() is called later
         * but we need to explicitly fire the change callback beforehand for
         * this validation widget since we are changing the data model before it
         * was able to be validated on the original data. */
        if (changedToDefault) {
            selectedTeamsEmptyValidation.setValidity(true, true);
        }

        refreshDisplayedValue(newValue);

        this.value = TeamsUtil.getTeamMemberNames(newValue, getTeam(), false);

        if(getSelectorPanel().isShown()) {
            
            //update the filter for the selection dropdown if it is visible
            TeamSelectionPanel.loadAndFilterTeams(TeamPicker.this);
        }
        
        validateAll();

        if (fireEvents || changedToDefault) {
            ValueChangeEvent.fire(this, getValue());
        }
    }

    /**
     * This does the same as {@link #getValue()} except doesn't return any
     * top-level team names. Instead it will contain the complete list of all
     * selected team members.
     * 
     * @return the entire set of selected team members. No team names are
     *         included. Will never be null.
     */
    public Set<String> getSelectedTeamMembers(){
        Set<String> selectedMembers = new HashSet<>();
        
        for (String value : getValue()) {
            Serializable entity = TeamsUtil.getTeamOrgEntityWithName(value, orgTeam, true);
            if (entity instanceof Team) {
                Team team = (Team) entity;
                selectedMembers.addAll(TeamsUtil.getTeamMembersFromTeam(team));
            } else if (entity instanceof TeamMember) {
                TeamMember member = (TeamMember) entity;
                selectedMembers.add(member.getName());
            }
        }
        
        return selectedMembers;
    }

    /**
     * Refreshes this widget's displayed value to match its underlying true value. Visually, this will update this widget's
     * tags to reflect the team members that are selected internally.
     * 
     * @param value the value to display
     */
    protected void refreshDisplayedValue(List<String> value) {
        if (logger.isLoggable(Level.FINE)) {
            StringBuilder sb = new StringBuilder("refreshDisplayedValue(");
            List<Object> params = Arrays.<Object>asList(value);
            join(", ", params, sb);
            logger.fine(sb.append(")").toString());
        }

        shouldHandleTagValueChange = false;
        teamTags.removeAll();
        displayedValues.clear();
        
        /* if teams can be picked, show top-most names to reduce clutter
         * otherwise, show the raw values */
        List<String> valuesToAdd = !PickMode.SINGLE.equals(pickMode) ?TeamsUtil.getTopMostTeamNames(value, getTeam())
                : value;
        if (valuesToAdd != null && !valuesToAdd.isEmpty()) {
            teamTags.add(valuesToAdd);
            displayedValues.addAll(valuesToAdd);
        }

        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            
            @Override
            public void execute() {
                
                //allow the event loop to finish before handling changes to the tags input again, otherwise it may
                //start handling changes too early and fire a value change event when unintended
                shouldHandleTagValueChange = true;
            }
        });
    }

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        validationStatuses.add(teamSelectedValidation);
        validationStatuses.add(selectedTeamsEmptyValidation);
        validationStatuses.add(teamMemberAvailableValidation);
        validationStatuses.add(noEntityMarkersValidation);
        validationStatuses.add(entityMarkerMissingValidation);
        validationStatuses.add(noSuchTeamMemberValidation);
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        if (teamSelectedValidation.equals(validationStatus)) {
            teamSelectedValidation.setValidity(
                    getValue() == null 
                    || getValue().isEmpty()
                    || !TeamsUtil.getTopMostTeamNames(getValue(), getTeam()).isEmpty());
            
        } else if (selectedTeamsEmptyValidation.equals(validationStatus)) {
            
            //if the underlying value is empty and either empty values aren't allowed or the team tags aren't empty, 
            //then team members must be selected
            selectedTeamsEmptyValidation.setValidity(
                    (getValue() != null && !getValue().isEmpty())
                    || (!mustPickTeamMember && displayedValues.isEmpty()));
            
        } else if (teamMemberAvailableValidation.equals(validationStatus)) {
            
            //if the underlying value is empty and there are no team members available, then the author can't actually pick anything
            teamMemberAvailableValidation.setValidity(
                    (getValue() != null && !getValue().isEmpty())
                    || TeamsUtil.getAnyTeamMemberName(getTeam()) != null);
            
        } else if (noEntityMarkersValidation.equals(validationStatus)) {
            
            noEntityMarkersValidation.setValidity(
                    !mustUseEntityMarkers || TeamsUtil.hasTeamMemberWithMarker(getTeam()));
            
        } else if(entityMarkerMissingValidation.equals(validationStatus)) {
            
            if(mustUseEntityMarkers 
                    && getValue() != null 
                    && !getValue().isEmpty()) {
                
                List<TeamMember> members = TeamsUtil.getTeamMembersWithNames(getValue(), getTeam());
                
                boolean isMissingEntityMarker = false;
                
                if(members != null) {
                    
                    for(TeamMember member : members) {
                        
                        if(member.getLearnerId() == null || !(member.getLearnerId().getType() instanceof String)) {
                            isMissingEntityMarker = true;
                            break;
                        }
                    }
                    
                } else {
                    isMissingEntityMarker = true;
                }
                
                entityMarkerMissingValidation.setValidity(!isMissingEntityMarker);
                
            } else {
                entityMarkerMissingValidation.setValid();
            }
        } else if (noSuchTeamMemberValidation.equals(validationStatus)) {
            /* This validation does not get assessed here. This is a special
             * case that will validate on setValue() and on value change. */
        }
    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
        //no child validation
    }

    /**
     * Sets the components to read only mode which prevents users from making changes.
     *
     * @param isReadonly True to prevent editing, false to allow editing.
     */
    public void setReadonly(boolean isReadonly) {
        teamSelect.setEnabled(!isReadonly);
        if(isReadonly){
            teamTagPanelBlocker.block();  // block click events to prevent removing tags
        }else{
            teamTagPanelBlocker.unblock();
        }
    }

    /**
     * Gets a {@link Widget} reference to the {@link #teamSelect} textbox. This
     * method is used to expose the {@link Widget} for purpose of validation
     * highlighting.
     *
     * @return The {@link Widget} reference to {@link #teamSelect}. Can't be
     *         null.
     */
    public Widget getTextBoxRef() {
        return pickerInputPanel;
    }

    /**
     * Gets the collapseable panel that the selector should be placed in when it is shown
     *
     * @return the panel where the selector should be placed
     */
    Collapse getSelectorPanel() {
        return selectorPanel;
    }

    /**
     * Sets the label text for this picker
     *
     * @param text the plain text to use as the label
     */
    public void setLabel(String text) {
        label.setText(text);
        label.setVisible(StringUtils.isNotBlank(text));
    }

    /**
     * Sets the label text for this picker and renders it as HTML
     *
     * @param html the HTML to use as the label
     */
    public void setLabel(SafeHtml html) {
        if (html == null) {
            setLabel("");
            return;
        }

        label.setHTML(html, Direction.LTR);
        label.setVisible(StringUtils.isNotBlank(html.asString()));
    }

    /**
     * Gets this widget's <i>displayed</i> value, i.e. the strings currently being shown by its visible tags. 
     * Unlike {@link #getValue()}, this method will return only the top-most team and team member names that encompass 
     * all of the team member names that would normally be returned by {@link #getValue()}. Since the number of 
     * top-most team and team member names will generally be less than the total number of team member names, this
     * reduces visual clutter by cutting down on the number of names that are rendered
     * 
     * @return the displayed value of this widget, i.e. the names of the top-level teams and team members represented
     * by this widget's tags. Can never be null.
     */
    public Collection<String> getDisplayedValue(){
        return Collections.unmodifiableCollection(displayedValues);
    }
    
    /**
     * Gets the mode that this widget is using to allow the author to pick team members
     * 
     * @return the mode used to pick team members
     */
    public PickMode getPickMode() {
        return pickMode;
    }

    /**
     * Gets the text box used to search for team or team member names
     * 
     * @return the search box
     */
    public TextBox getSearchBox() {
        return teamSelect;
    }

    @Override
    protected void fireDirtyEvent(Serializable sourceObject) {
        // nothing to fire
    }

    /**
     * Get the organization team
     * 
     * @return the team. Will never be null.
     */
    public Team getTeam() {
        return orgTeam;
    }

    /**
     * Perform the action upon a node jump
     */
    protected void performNodeJump() {
        // do nothing
    }

    /**
     * Create a new team tree item with the team from this picker
     * 
     * @return the new team tree item
     */
    protected TeamTreeItem createTeamTreeItem() {
        return new TeamTreeItem(getTeam());
    }
}
