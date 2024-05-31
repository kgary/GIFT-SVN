/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.condition;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;

import generated.dkf.LearnerAction;
import generated.dkf.LearnerActionEnumType;
import generated.dkf.PaceCountCondition;
import mil.arl.gift.common.gwt.client.validation.ModelValidationStatus;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.gwt.client.validation.WidgetValidationStatus;
import mil.arl.gift.common.gwt.client.widgets.DecimalNumberSpinner;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.CreateScenarioObjectEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.DeleteScenarioObjectEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared.TeamMemberPicker;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.ScenarioEventUtility;

/**
 * An editor that modifies the inputs for {@link PaceCountCondition PaceCountConditions}
 *
 * @author nroberts
 */
public class PaceCountConditionEditorImpl extends ConditionInputPanel<PaceCountCondition>{

    /** The binder that combines the ui.xml with this java class */
    private static PaceCountConditionEditorImplUiBinder uiBinder = GWT
            .create(PaceCountConditionEditorImplUiBinder.class);

    /** The binder that registers EventHandlers in this class to an event bus */
    private static PaceCountConditionEditorImplEventBinder eventBinder = GWT.create(PaceCountConditionEditorImplEventBinder.class);

    /** The binder that combines the ui.xml with the java class */
    interface PaceCountConditionEditorImplUiBinder extends UiBinder<Widget, PaceCountConditionEditorImpl> {
    }

    /** The binder that registers EventHandlers with the event bus */
    interface PaceCountConditionEditorImplEventBinder extends EventBinder<PaceCountConditionEditorImpl> {
    }

    /** The expected travel distance text box. */
    @UiField (provided = true)
    protected DecimalNumberSpinner distanceTextBox = new DecimalNumberSpinner(BigDecimal.ZERO, BigDecimal.ZERO, null);

    /** The threshold distance text box. */
    @UiField (provided = true)
    protected DecimalNumberSpinner thresholdTextBox = new DecimalNumberSpinner(BigDecimal.ZERO, BigDecimal.ZERO, null);

    /** Team member picker used to determine the learner role that this condition should assess */
    @UiField(provided = true)
    protected TeamMemberPicker teamMemberPicker = new TeamMemberPicker(ScenarioClientUtility.isLearnerIdRequiredByApplication());

    /**
     * Validation container for when this editor doensn't have an expected
     * distance set
     */
    private final WidgetValidationStatus distanceEnteredValidation = new WidgetValidationStatus(distanceTextBox,
            "You must define a distance that the learner should travel. Please enter a distance.");

    /** Validation ensuring there are start and stop learner actions */
    private final ModelValidationStatus startAndStopLearnerAction = new ModelValidationStatus(
            "This condition requires both a Start Pace Count and Stop Pace Count condition") {
        @Override
        protected void fireJumpToEvent(Serializable modelObject) {
            ScenarioEventUtility.fireJumpToEvent(modelObject);
        }
    };

    /**
     * Creates an new unpopulated instance of a
     * {@link PaceCountConditionEditorImpl}.
     */
    public PaceCountConditionEditorImpl() {
        initWidget(uiBinder.createAndBindUi(this));
        eventBinder.bindEventHandlers(this, SharedResources.getInstance().getEventBus());

        startAndStopLearnerAction.setModelObject(ScenarioClientUtility.getAvailableLearnerActions());

        distanceTextBox.addValueChangeHandler(new ValueChangeHandler<BigDecimal>(){
            @Override
            public void onValueChange(ValueChangeEvent<BigDecimal> event) {

                BigDecimal distance = event.getValue();

                if (distance != null) {
                    getInput().setExpectedDistance(distance.doubleValue());

                    ScenarioEventUtility.fireDirtyEditorEvent(getCondition());
                }

                requestValidation(distanceEnteredValidation);
            }
        });

        thresholdTextBox.addValueChangeHandler(new ValueChangeHandler<BigDecimal>(){
            @Override
            public void onValueChange(ValueChangeEvent<BigDecimal> event) {

                BigDecimal threshold = event.getValue();

                if (threshold != null && threshold.doubleValue() >= 0) {
                    getInput().setDistanceThreshold(threshold.doubleValue());

                } else {
                    getInput().setDistanceThreshold(null);
                }
            }
        });
        
        teamMemberPicker.addValueChangeHandler(new ValueChangeHandler<String>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                
                String selectedRole = event.getValue();
                
                //update the backing data model with the referenced team member name
                if(StringUtils.isNotBlank(selectedRole)) {
                    getInput().setTeamMemberRef(selectedRole);
                    
                } else {
                    getInput().setTeamMemberRef(null);
                }
                
                //this condition's team references may have changed, so update the global reference map
                ScenarioClientUtility.gatherTeamReferences();

                /* Validate picker after it changed */
                teamMemberPicker.validateAllAndFireDirtyEvent(getCondition());
            }
        });
    }

    /**
     * Requests revalidation when a new scenario object is created.
     *
     * @param event The event containing the object that was created.
     */
    @EventHandler
    protected void onScenarioObjectCreated(CreateScenarioObjectEvent event) {
        if (event.getScenarioObject() instanceof LearnerAction) {
            requestValidation(startAndStopLearnerAction);
            ScenarioEventUtility.fireDirtyEditorEvent(getCondition());
        }
    }

    /**
     * Requests revalidation when a scenario object has been deleted.
     *
     * @param event The event containing the object that was deleted.
     */
    @EventHandler
    protected void onScenarioObjectDeleted(DeleteScenarioObjectEvent event) {
        if (event.getScenarioObject() instanceof LearnerAction) {
            requestValidation(startAndStopLearnerAction);
            ScenarioEventUtility.fireDirtyEditorEvent(getCondition());
        }
    }

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        validationStatuses.add(distanceEnteredValidation);
        validationStatuses.add(startAndStopLearnerAction);
    }

    @Override
    public void validate(ValidationStatus validationStatus) {

        if (distanceEnteredValidation.equals(validationStatus)) {
            distanceEnteredValidation.setValidity(distanceTextBox.getValue() != null && distanceTextBox.getValue().doubleValue() > 0);
        } else if (startAndStopLearnerAction.equals(validationStatus)) {
            if (ScenarioClientUtility.getLearnerActions() != null) {
                boolean hasStart = false, hasStop = false;
                for (LearnerAction learnerAction : ScenarioClientUtility.getLearnerActions().getLearnerAction()) {
                    hasStart |= learnerAction.getType() == LearnerActionEnumType.START_PACE_COUNT;
                    hasStop |= learnerAction.getType() == LearnerActionEnumType.END_PACE_COUNT;
                }

                validationStatus.setValidity(hasStart && hasStop);
            } else {
                validationStatus.setInvalid();
            }
        }
    }

    @Override
    protected void onEdit() {

        distanceTextBox.setValue(BigDecimal.valueOf(getInput().getExpectedDistance()));

        if(getInput().getDistanceThreshold() != null) {
            thresholdTextBox.setValue(BigDecimal.valueOf(getInput().getDistanceThreshold()));
        } else {
            thresholdTextBox.setValue(null);
        }
        
        if(ScenarioClientUtility.isLearnerIdRequiredByApplication()) {
            
            String memberRef = getInput().getTeamMemberRef();
            
            //the current training app requires a team member learner ID, so load any that are found
            teamMemberPicker.setValue(memberRef);
            
            //update the backing value in case the picker automatically provides a new value
            getInput().setTeamMemberRef(teamMemberPicker.getValue());
            
        } else {
            
            if(getInput().getTeamMemberRef() != null) {
                getInput().setTeamMemberRef(null);
            }
            
            teamMemberPicker.setValue(null);
        }
    }

    @Override
    protected void setReadonly(boolean isReadonly) {
        distanceTextBox.setEnabled(!isReadonly);
        thresholdTextBox.setEnabled(!isReadonly);
        teamMemberPicker.setReadonly(isReadonly);
    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
        childValidationComposites.add(teamMemberPicker);
    }
}