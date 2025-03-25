package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.condition;


import java.util.List;
import java.util.Set;

import com.google.gwt.uibinder.client.UiField;
import generated.dkf.SteelarttConditionInput;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared.EditableTeamPicker;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;


public class SteelarttInputEditorImpl extends ConditionInputPanel<SteelarttConditionInput>{

    public SteelarttInputEditorImpl(){
        // NoArgsConstructor
    }
    /** Team picker used to determine learner roles this condition should assess */
    @UiField(provided = true)
    protected EditableTeamPicker teamPicker = new EditableTeamPicker(ScenarioClientUtility.isLearnerIdRequiredByApplication());

    @Override
    protected void setReadonly(boolean isReadonly) {
        teamPicker.setReadonly(isReadonly);
    }
    @Override
    protected void onEdit() {}
    
    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {}

    @Override
    public void validate(ValidationStatus validationStatus) {}

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {}
}