/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.condition;

import static mil.arl.gift.common.gwt.client.SafeHtmlUtils.bold;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.EngageTargetsCondition;
import generated.dkf.Point;
import generated.dkf.TeamMemberRefs;
import generated.dkf.PointRef;
import mil.arl.gift.common.gwt.client.coordinate.CoordinateType;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.gwt.client.validation.WidgetValidationStatus;
import mil.arl.gift.common.gwt.client.widgets.DecimalNumberSpinner;
import mil.arl.gift.common.gwt.client.widgets.NumberSpinner;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ToggleButton;
import mil.arl.gift.common.util.StringUtils.Stringifier;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared.EditableTeamPicker;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioValidatorUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.CreateListAction;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemField;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemListEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ListChangedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemListEditor.EditCancelledCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemListEditor.ListChangedCallback;

/**
 * A {@link ConditionInputPanel} that is used to edit the
 * {@link EngageTargetsCondition} input.
 *
 * @author tflowers
 *
 */
public class EngageTargetsConditionEditor extends ConditionInputPanel<EngageTargetsCondition> {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(EngageTargetsConditionEditor.class.getName());

    /** The binder that binds this java class to a ui.xml */
    private static EngageTargetsConditionEditorUiBinder uiBinder = GWT
            .create(EngageTargetsConditionEditorUiBinder.class);

    /** The definition of the binder that binds a java class to a ui.xml */
    interface EngageTargetsConditionEditorUiBinder extends UiBinder<Widget, EngageTargetsConditionEditor> {
    }
    
    /**
     * Minimum writable value for
     * {@link EngageTargetsCondition#setWeaponConeAngle(BigDecimal)}
     */
    private static final int MIN_WEAPON_CONE_ANGLE = 1;

    /**
     * Maximum writable value for
     * {@link EngageTargetsCondition#setWeaponConeAngle(BigDecimal)}
     */
    private static final int MAX_WEAPON_CONE_ANGLE = 360;

    /**
     * The weapon cone angle value to use for a condition in which it is not specified.
     */
    private static final int DEFAULT_WEAPON_CONE_ANGLE = 30;
    
    /**
     * default amount of time for max time to detect to get above expectation
     */
    private static final BigDecimal DEFAULT_ABOVE_TIME = new BigDecimal("2.5");
    
    /**
     * default amount of time for max time to detect to get at expectation
     */
    private static final BigDecimal DEFAULT_AT_TIME = new BigDecimal("5.0");
    
    /**
     * the default max distance value for
     * {@link EngageTargetsCondition#setWeaponConeMaxDistance(BigInteger)}
     */
    private static final BigInteger DEFAULT_MAX_DISTANCE = BigInteger.valueOf(300);

    /** The picker that selects the team members to assess */
    @UiField(provided = true)
    protected EditableTeamPicker teamPicker = new EditableTeamPicker(false);
    
    /** The picker that selects the team members for targets */
    @UiField(provided = true)
    protected EditableTeamPicker targetTeamPicker = new EditableTeamPicker(false);
    
    /** The item list editor table for the target locations */
    @UiField(provided = true)
    protected ItemListEditor<PointRef> targetLocationListEditor;
    
    /** The control used to author the weapon cone angle threshold. */
    @UiField(provided = true)
    protected NumberSpinner weaponConeAngleSpinner = new NumberSpinner(DEFAULT_WEAPON_CONE_ANGLE, MIN_WEAPON_CONE_ANGLE, MAX_WEAPON_CONE_ANGLE);

    /** The checkbox used to optionally include a max distance threshold. */
    @UiField
    protected ToggleButton useMaxDistanceCheckBox;

    /** The control used to author the max distance of the condition. */
    @UiField(provided = true)
    protected NumberSpinner maxDistanceSpinner = new NumberSpinner(DEFAULT_MAX_DISTANCE.intValue(), 1, Integer.MAX_VALUE);
    
    /** The label for the max distance control */
    @UiField
    protected Widget maxDistanceLabel;
    
    /** used to input time in seconds for max time to get Above Expectation */
    @UiField(provided = true)
    protected DecimalNumberSpinner aboveExpectationTimeSpinner = new DecimalNumberSpinner(DEFAULT_ABOVE_TIME, BigDecimal.ZERO, new BigDecimal(Float.MAX_VALUE), new BigDecimal(0.1));
    
    /** used to input time in seconds for max time to get At Expectation */
    @UiField(provided = true)
    protected DecimalNumberSpinner atExpectationTimeSpinner = new DecimalNumberSpinner(DEFAULT_AT_TIME, BigDecimal.ZERO, new BigDecimal(Float.MAX_VALUE), new BigDecimal(0.1));

    /** The validation object used to validate the 
     *  {@link #teamPicker}
     */
    private final WidgetValidationStatus teamMemberValidation = new WidgetValidationStatus(teamPicker.getTextBoxRef(),
            "At least one team member must be chosen.");
    
    /**
     * The validation object used to validate constraints on the
     * {@link #weaponConeAngleSpinner}.
     */
    private final WidgetValidationStatus weaponConeAngleValidation = new WidgetValidationStatus(weaponConeAngleSpinner,
            "The weapon cone angle must be greater than or equal to zero.");

    /**
     * The validation object used to validate constraints on the
     * {@link #maxDistanceSpinner}.
     */
    private final WidgetValidationStatus maxDistanceValidation = new WidgetValidationStatus(maxDistanceSpinner,
            "The distance must be greater than or equal to zero.");
    
    /**
     * The validation object used to validate constraints on the
     * {@link #targetLocationListEditor}.
     */
    private final WidgetValidationStatus targetLocationValidationStatus;
    
    /**
     * The validation object used to validate constraints on the
     * {@link #targetMemberListEditor}.
     */
    private final WidgetValidationStatus targetMemberValidationStatus = new WidgetValidationStatus(targetTeamPicker,
            "At least one team organization member or location target must be specified.");
    
    /**
     * The validation object used to validate constraints on the
     * {@link #AboveExpectationTimeTextBox}.
     */
    private final WidgetValidationStatus aboveTimeValidation = new WidgetValidationStatus(aboveExpectationTimeSpinner,
            "The Above Expectation time must be greater than zero and less than the At Expectation time.");
    
    /**
     * The validation object used to validate constraints on the
     * {@link #AtExpectationTimeTextBox}.
     */
    private final WidgetValidationStatus atTimeValidation = new WidgetValidationStatus(atExpectationTimeSpinner,
            "The At Expectation time must be greater than zero and greater than the Above Expectation time.");

    /**
     * Builds an unpopulated {@link EngageTargetsCondition}.
     */
    public EngageTargetsConditionEditor() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("EngageTargetsConditionEditor()");
        }
        
        WaypointRefItemEditor targetWaypointEditor = new WaypointRefItemEditor(this);
        CoordinateType[] disallowedTypes = ScenarioClientUtility.getDisallowedCoordinateTypes(generated.dkf.EngageTargetsCondition.class);
        targetWaypointEditor.setDisallowedTypes(disallowedTypes);
        targetLocationListEditor = new ItemListEditor<PointRef>(targetWaypointEditor);

        initWidget(uiBinder.createAndBindUi(this));
        
        targetLocationListEditor.setTableLabel("Locations to engage:");
        targetLocationListEditor.setPlaceholder("No locations have been set.");
        targetLocationListEditor.setFields(buildLocationItemFields());
        targetLocationListEditor.setDraggable(true);
        targetLocationListEditor.addListChangedCallback(new ListChangedCallback<PointRef>() {
            @Override
            public void listChanged(ListChangedEvent<PointRef> event) {
                
                // update data model
                if(getInput().getTargetsToEngage() == null){
                    getInput().setTargetsToEngage(new generated.dkf.EngageTargetsCondition.TargetsToEngage());
                }
                
                List<Serializable> targetsToEngage = getInput().getTargetsToEngage().getTeamMemberRefOrPointRef();

                switch(event.getActionPerformed()){
                case ADD:
                    targetsToEngage.addAll(event.getAffectedItems());
                    break;
                case REMOVE:
                    targetsToEngage.removeAll(event.getAffectedItems());
                    break;
                case REORDER:
                    targetsToEngage.clear();
                    targetsToEngage.addAll(event.getAffectedItems());
                    break;
				default:
					break;
                }
                
                setClean();
                requestValidationAndFireDirtyEvent(getCondition(), targetMemberValidationStatus, targetLocationValidationStatus);
                
                //a place of interest reference may have been changed, so update the global list of references
                ScenarioClientUtility.gatherPlacesOfInterestReferences();
            }
        });
        targetLocationListEditor.addEditCancelledCallback(new EditCancelledCallback() {
            @Override
            public void editCancelled() {
                setClean();
            }
        });
        targetLocationListEditor.setRemoveItemDialogTitle("Remove Target Location");
        targetLocationListEditor.setRemoveItemStringifier(new Stringifier<PointRef>() {
            @Override
            public String stringify(PointRef obj) {
                return "the point "+bold(obj.getValue()).asString()+" as a target (does not delete the underlying place of interest)";
            }
        });
        Widget locationAddAction = targetLocationListEditor.addCreateListAction("Click here to add a new target location",
                buildLocationCreateAction());
        
        targetLocationValidationStatus = new WidgetValidationStatus(locationAddAction,
                "At least one team organization member or location target must be specified.");
    }
    
    /**
     * Builds the item fields for the location table.
     * 
     * @return the {@link ItemField item field columns} for the location table.
     */
    private List<ItemField<PointRef>> buildLocationItemFields() {

        ItemField<PointRef> waypointField = new ItemField<PointRef>(null, "100%") {
            @Override
            public Widget getViewWidget(PointRef item) {
                return new HTML(item.getValue());
            }
        };

        return Arrays.asList(waypointField);
    }
    
    /**
     * Builds the create action for the location table.
     * 
     * @return the {@link CreateListAction} for the location table.
     */
    private CreateListAction<PointRef> buildLocationCreateAction() {
        return new CreateListAction<PointRef>() {

            @Override
            public PointRef createDefaultItem() {
                
                setDirty();
                
                List<Serializable> placesOfInterest = ScenarioClientUtility.getUnmodifiablePlacesOfInterestList();

                PointRef pointRef = new PointRef();

                if (!placesOfInterest.isEmpty()) {
                    //artificially pick a point
                    for(Serializable placeOfInterest : placesOfInterest){
                        
                        if(placeOfInterest instanceof Point){
                            Point point = (Point) placeOfInterest;
                            if(!ScenarioValidatorUtility.containsDisallowedCoordinate(
                                    point, ScenarioClientUtility.getDisallowedCoordinateTypes(generated.dkf.IdentifyPOIsCondition.class))){
                                pointRef.setValue(point.getName());
                                break;
                            }
                        }
                    }
                    
                }

                return pointRef;
            }
        };
    }

    /**
     * Handles when the value of the {@link #teamPicker} changes.
     *
     * @param event The event containing the updated value of
     *        {@link #teamPicker}. Can't be null.
     */
    @UiHandler("teamPicker")
    protected void onTeamMembersChanged(ValueChangeEvent<List<String>> event) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("onTeamMembersChanged(" + event.toDebugString() + ")");
        }

        final List<String> teamMemberRef = getInput().getTeamMemberRefs().getTeamMemberRef();
        teamMemberRef.clear();
        teamMemberRef.addAll(event.getValue());

        requestValidationAndFireDirtyEvent(getCondition(), teamMemberValidation);
    }
    
    /**
     * Handles when the value of the {@link #targetTeamPicker} changes.
     *
     * @param event The event containing the updated value of
     *        {@link #targetTeamPicker}. Can't be null.
     */
    @UiHandler("targetTeamPicker")
    protected void onTargetTeamMembersChanged(ValueChangeEvent<List<String>> event) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("onTargetTeamMembersChanged(" + event.toDebugString() + ")");
        }

        if(getInput().getTargetsToEngage() == null){
            getInput().setTargetsToEngage(new generated.dkf.EngageTargetsCondition.TargetsToEngage());
        }
        
        final List<Serializable> targetsToEngage = getInput().getTargetsToEngage().getTeamMemberRefOrPointRef();
        // update data model to match widget list
        List<String> newItems = new ArrayList<>(event.getValue());
        Iterator<Serializable> targetsToEngageItr = targetsToEngage.iterator();
        while(targetsToEngageItr.hasNext()){
            
            Serializable target = targetsToEngageItr.next();
            if(target instanceof String){
            
                int indexOfItem = newItems.indexOf(target);
                if(indexOfItem != -1){
                    // the item is still in the widget, it isn't a new item 
                    // but one the data model knows about already
                    newItems.remove(indexOfItem);
                }else{
                    // the item isn't in the widget anymore, remove it from data model
                    targetsToEngageItr.remove();
                }
            }
        }
        
        // add new items to data model
        targetsToEngage.addAll(newItems);

        requestValidationAndFireDirtyEvent(getCondition(), targetMemberValidationStatus, targetLocationValidationStatus);
    }
    
    /**
     * Handles when the value of the {@link #weaponConeAngleSpinner} changes.
     *
     * @param event The event containing the updated value of
     *        {@link #weaponConeAngleSpinner}. Can't be null.
     */
    @UiHandler("weaponConeAngleSpinner")
    protected void onWeaponCondeAngleChanged(ValueChangeEvent<Integer> event) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("onWeaponCondeAngleChanged(" + event.toDebugString() + ")");
        }

        getInput().setWeaponConeAngle(event.getValue());
        requestValidationAndFireDirtyEvent(getCondition(), weaponConeAngleValidation);
    }
    
    /**
     * Handles when the value of the {@link #aboveExpectationTimeSpinner} changes.
     *
     * @param event The event containing the updated value of
     *        {@link #aboveExpectationTimeSpinner}. Can't be null.
     */
    @UiHandler("aboveExpectationTimeSpinner")
    protected void onAboveExpectationTimeSpinnerChanged(ValueChangeEvent<BigDecimal> event) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("onAboveExpectationTimeSpinnerChanged(" + event.toDebugString() + ")");
        }

        getInput().setAboveExpectationUpperBound(event.getValue());
        requestValidationAndFireDirtyEvent(getCondition(), aboveTimeValidation);
    }
    
    /**
     * Handles when the value of the {@link #atExpectationTimeSpinner} changes.
     *
     * @param event The event containing the updated value of
     *        {@link #atExpectationTimeSpinner}. Can't be null.
     */
    @UiHandler("atExpectationTimeSpinner")
    protected void onAtExpectationTimeSpinnerChanged(ValueChangeEvent<BigDecimal> event) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("onAtExpectationTimeSpinnerChanged(" + event.toDebugString() + ")");
        }

        getInput().setAtExpectationUpperBound(event.getValue());
        requestValidationAndFireDirtyEvent(getCondition(), atTimeValidation);
    }

    /**
     * Handles when the value of the {@link #useMaxDistanceCheckBox} changes.
     *
     * @param event The event containing the updated value of
     *        {@link #useMaxDistanceCheckBox}. Can't be null.
     */
    @UiHandler("useMaxDistanceCheckBox")
    protected void onUseMaxDistanceChanged(ValueChangeEvent<Boolean> event) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("onUseMaxDistanceChanged(" + event.toDebugString() + ")");
        }

        final boolean isChecked = Boolean.TRUE.equals(event.getValue());
        if (isChecked) {
            logger.info("Setting weapon cone max distance to "+maxDistanceSpinner.getValue());
            getInput().setWeaponConeMaxDistance(BigInteger.valueOf(maxDistanceSpinner.getValue()));
        } else {
            getInput().setWeaponConeMaxDistance(null);
        }

        requestValidationAndFireDirtyEvent(getCondition(), maxDistanceValidation);
    }

    /**
     * Handles when the value of the {@link #maxDistanceSpinner} changes.
     *
     * @param event The event containing the updated value of
     *        {@link #maxDistanceSpinner}. Can't be null.
     */
    @UiHandler("maxDistanceSpinner")
    protected void onMaxDistanceChanged(ValueChangeEvent<Integer> event) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("onMaxDistanceChanged(" + event.toDebugString() + ")");
        }

        getInput().setWeaponConeMaxDistance(BigInteger.valueOf(event.getValue()));
        requestValidationAndFireDirtyEvent(getCondition(), maxDistanceValidation);
    }


    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("getValidationStatuses(" + validationStatuses + ")");
        }

        validationStatuses.add(teamMemberValidation);
        validationStatuses.add(maxDistanceValidation);
        validationStatuses.add(weaponConeAngleValidation);
        validationStatuses.add(targetLocationValidationStatus);
        validationStatuses.add(targetMemberValidationStatus);
        validationStatuses.add(aboveTimeValidation);
        validationStatuses.add(atTimeValidation);
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("validate(" + validationStatus + ")");
        }

        if (teamMemberValidation.equals(validationStatus)) {
            final int teamRefCount = getInput().getTeamMemberRefs().getTeamMemberRef().size();
            validationStatus.setValidity(teamRefCount >= 1);
        } else if (maxDistanceValidation.equals(validationStatus)) {
            final BigInteger maxDistance = getInput().getWeaponConeMaxDistance();
            validationStatus.setValidity(maxDistance == null || maxDistance.intValue() > 0);
        } else if (weaponConeAngleValidation.equals(validationStatus)) {
            final Integer weaponConeAngle = getInput().getWeaponConeAngle();
            validationStatus.setValidity(weaponConeAngle != null && weaponConeAngle >= 1 && weaponConeAngle <= 360);
        } else if(targetLocationValidationStatus.equals(validationStatus) ||
                targetMemberValidationStatus.equals(validationStatus)){
            boolean hasTarget = !targetLocationListEditor.getItems().isEmpty() || !targetTeamPicker.getValue().isEmpty();
            validationStatus.setValidity(hasTarget);
        } else if (aboveTimeValidation.equals(validationStatus)) {
            final BigDecimal aboveTime = getInput().getAboveExpectationUpperBound();
            final BigDecimal atTime = getInput().getAtExpectationUpperBound();
            validationStatus.setValidity(aboveTime.doubleValue() > 0 && aboveTime.doubleValue() < atTime.doubleValue());
        } else if (atTimeValidation.equals(validationStatus)) {
            final BigDecimal aboveTime = getInput().getAboveExpectationUpperBound();
            final BigDecimal atTime = getInput().getAtExpectationUpperBound();
            validationStatus.setValidity(atTime.doubleValue() > 0 && aboveTime.doubleValue() < atTime.doubleValue());
        }
    }

    @Override
    protected void onEdit() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("onEdit()");
        }

        /* Ensure that there is a value for the assessed teams. */
        TeamMemberRefs teamMemberRefs = getInput().getTeamMemberRefs();
        if (teamMemberRefs == null) {
            teamMemberRefs = new TeamMemberRefs();
            getInput().setTeamMemberRefs(teamMemberRefs);
        }

        teamPicker.setValue(teamMemberRefs.getTeamMemberRef());
        
        if(getInput().getTargetsToEngage() == null){
            getInput().setTargetsToEngage(new generated.dkf.EngageTargetsCondition.TargetsToEngage());
        }
        
        List<String> targetTeamMembers = new ArrayList<>();
        List<PointRef> targetLocations = new ArrayList<>();
        for(Serializable target : getInput().getTargetsToEngage().getTeamMemberRefOrPointRef()){
            if(target instanceof String){
                targetTeamMembers.add((String) target);
            }else if(target instanceof PointRef){
                targetLocations.add((PointRef) target);
            }
        }
        targetTeamPicker.setValue(targetTeamMembers);
        targetLocationListEditor.setItems(targetLocations);

        BigInteger maxDistance = getInput().getWeaponConeMaxDistance();
        useMaxDistanceCheckBox.setValue(maxDistance != null);
        if (maxDistance != null) {
            maxDistanceSpinner.setValue(maxDistance.intValue());
        }
        
        Integer weaponConeMaxAngle = getInput().getWeaponConeAngle(); 
        if(weaponConeMaxAngle == 0){
            // default for integer is 0, which probably means the author never specified,
            // set current value to default value that is used when rendering widget
            weaponConeMaxAngle = DEFAULT_WEAPON_CONE_ANGLE;
        }
        weaponConeAngleSpinner.setValue(weaponConeMaxAngle);
        
        BigDecimal aboveTime = getInput().getAboveExpectationUpperBound();
        if(aboveTime == null){
            aboveTime = DEFAULT_ABOVE_TIME;
            getInput().setAboveExpectationUpperBound(aboveTime);
        }
        
        aboveExpectationTimeSpinner.setValue(aboveTime);
        
        BigDecimal atTime = getInput().getAtExpectationUpperBound();
        if(atTime == null){
            atTime = DEFAULT_AT_TIME;
            getInput().setAtExpectationUpperBound(atTime);
        }
        
        atExpectationTimeSpinner.setValue(atTime);
    }

    @Override
    protected void setReadonly(boolean isReadonly) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("setReadonly(" + isReadonly + ")");
        }

        teamPicker.setReadonly(isReadonly);
        useMaxDistanceCheckBox.setEnabled(!isReadonly);
        maxDistanceSpinner.setEnabled(!isReadonly);
        weaponConeAngleSpinner.setEnabled(!isReadonly);
        targetTeamPicker.setReadonly(isReadonly);
        targetLocationListEditor.setReadonly(isReadonly);
        aboveExpectationTimeSpinner.setEnabled(!isReadonly);
        atExpectationTimeSpinner.setEnabled(!isReadonly);
    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("addValidationCompositeChildren(" + childValidationComposites + ")");
        }

        childValidationComposites.add(teamPicker);
        childValidationComposites.add(targetLocationListEditor);
        childValidationComposites.add(targetTeamPicker);
    }
}
