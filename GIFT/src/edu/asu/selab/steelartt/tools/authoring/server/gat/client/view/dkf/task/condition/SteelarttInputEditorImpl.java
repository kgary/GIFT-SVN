package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.condition;

import java.util.Set;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.CheckBox;

import generated.dkf.BooleanEnum;
import generated.dkf.SteelarttConditionInput;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared.EditableTeamPicker;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;

public class SteelarttInputEditorImpl extends ConditionInputPanel<SteelarttConditionInput>{

    public SteelarttInputEditorImpl(){
        // Register value change handler for woundIdentifiedCheck
        woundIdentifiedCheck.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                // Convert the boolean value from the CheckBox into BooleanEnum
                getInput().setWoundIdentified(event.getValue() ? BooleanEnum.TRUE : BooleanEnum.FALSE);
                setDirty();
            }
        });
    }
    
    /** Team picker used to determine learner roles this condition should assess */
    @UiField(provided = true)
    protected EditableTeamPicker teamPicker = new EditableTeamPicker(ScenarioClientUtility.isLearnerIdRequiredByApplication());

    @UiField
    protected CheckBox woundIdentifiedCheck;

    @Override
    protected void setReadonly(boolean isReadonly) {
        teamPicker.setReadonly(isReadonly);
        woundIdentifiedCheck.setEnabled(!isReadonly);
    }

    @Override
    protected void onEdit() {
        // Initialize team picker as needed
        if(getInput().getTeamMemberRefs() != null) {
            teamPicker.setValue(getInput().getTeamMemberRefs().getTeamMemberRef());
        } else {
            teamPicker.setValue(null);
        }
        
        // Initialize the woundIdentified CheckBox using the BooleanEnum value
        BooleanEnum woundIdentified = getInput().getWoundIdentified();
        woundIdentifiedCheck.setValue(woundIdentified != null && woundIdentified == BooleanEnum.TRUE);
    }
    
    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
        // Add any composite validations if necessary
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        // Implement validations if required
    }

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        // Collect and return validation statuses if needed
    }
}
