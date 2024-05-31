/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.transition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.gwtbootstrap3.client.ui.ValueListBox;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.Actions.InstructionalStrategies;
import generated.dkf.Actions.StateTransitions.StateTransition;
import generated.dkf.Strategy;
import generated.dkf.StrategyRef;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.gwt.client.validation.WidgetValidationStatus;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.ScenarioEventUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemEditor;

/**
 * An {@link ItemEditor} used to modify {@link StrategyRef strategy references} in a list.
 *
 * @author nroberts
 */
public class StrategyRefItemEditor extends ItemEditor<StrategyRef> {

    /** The ui binder. */
    private static StrategyRefItemEditorUiBinder uiBinder = GWT.create(StrategyRefItemEditorUiBinder.class);

    /**
     * The Interface StrategyRefItemEditorUiBinder.
     */
    interface StrategyRefItemEditorUiBinder extends UiBinder<Widget, StrategyRefItemEditor> {
    }

    /** The ValueListBox containing the names of available strategies */
    @UiField(provided = true)
    protected ValueListBox<String> nameBox = new ValueListBox<>(new Renderer<String>() {

        @Override
        public String render(String object) {
            return object != null ? object : "NONE AVAILABLE";
        }

        @Override
        public void render(String object, Appendable appendable) throws IOException {
            appendable.append(render(object));
        }
    });

    /** The panel for activity icons */
    @UiField
    protected FlowPanel activitiesPanel;

    /** The container for activities */
    @UiField
    protected Widget activitiesContainer;

    /** The state transition that the strategy reference being edited is part of*/
    private StateTransition stateTransition;

    /** Validation status that will show an error if there are no strategies available to choose from*/
    private WidgetValidationStatus strategyRefValidationStatus;

    /**
     * Creates a new strategy reference editor and initializes its validation and input handlers.
     */
    public StrategyRefItemEditor() {
        initWidget(uiBinder.createAndBindUi(this));

        strategyRefValidationStatus = new WidgetValidationStatus(nameBox,
                "There are no more strategies available to pick from. Please create a strategy.");

        nameBox.addValueChangeHandler(new ValueChangeHandler<String>() {

            @Override
            public void onValueChange(ValueChangeEvent<String> event) {

                if(event.getValue() != null) {

                    //update the list of activities
                    updateActivities(event.getValue());
                }
            }
        });
    }

    /**
     * Visually updates the list of activities that will be invoked by the strategy with the given name
     *
     * @param refName the name of the strategy to reference
     */
    protected void updateActivities(String refName) {

        activitiesPanel.clear();

        List<FlowPanel> activityIcons = StateTransitionPanel.getActivityIcons(refName);

        for(FlowPanel icon : activityIcons) {
            activitiesPanel.add(icon);
        }

        activitiesContainer.setVisible(!activityIcons.isEmpty());
    }

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        validationStatuses.add(strategyRefValidationStatus);
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        if (strategyRefValidationStatus.equals(validationStatus)) {
            strategyRefValidationStatus.setValidity(nameBox.getValue() != null);
        }
    }

    @Override
    protected boolean validate(StrategyRef strategyRef) {
        return StringUtils.isNotBlank(strategyRef.getName());
    }

    /**
     * Gets the list of available strategy names that have not been selected by the current state transition.
     *
     * @return the list of strategy names that are available. Cannot be null.
     */
    private List<String> getAvailableStrategyNames() {

        List<String> availableStrategies = new ArrayList<>();

        InstructionalStrategies strategies = ScenarioClientUtility.getStrategies();

        if (strategies != null) {
            for (Strategy strategy : strategies.getStrategy()) {
                String strategyName = strategy.getName();
                if (StringUtils.isNotBlank(strategyName) && !availableStrategies.contains(strategyName)) {
                    // only add strategy names that are not blank and have not already been added
                    availableStrategies.add(strategyName);
                }
            }

        }

        return availableStrategies;
    }

    @Override
    protected void populateEditor(StrategyRef obj) {

        //update the box used to select the strategy name
        nameBox.setValue(obj.getName(), true);

        updateStrategyList();
    }

    @Override
    protected void applyEdits(StrategyRef obj) {

        String oldName = obj.getName();
        String newName = nameBox.getValue();

        obj.setName(nameBox.getValue());

        if (oldName != null || newName != null) {
            ScenarioEventUtility.fireReferencesChangedEvent(stateTransition, null, obj);
        }
    }

    @Override
    protected void setReadonly(boolean isReadonly) {
        nameBox.setEnabled(!isReadonly);
        setSaveButtonVisible(!isReadonly);
    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
        //No children to show validation status for
    }

    /**
     * Updates the available strategy names that the author can select from
     */
    public void updateStrategyList() {
        updateStrategyList(null, null);
    }

    /**
     * Updates the available strategy names that the author can select from and, if necessary, replaces the old
     * name of a renamed strategy.
     *
     * @param oldName the old name to update
     * @param newName the new value to update the name with
     */
    public void updateStrategyList(String oldName, String newName) {

        String selectedName = nameBox.getValue();

        if(oldName != null && oldName.equals(selectedName)) {
            selectedName = newName;
            nameBox.setValue(selectedName);
        }

        List<String> strategyNames = getAvailableStrategyNames();

        if(!strategyNames.isEmpty()) {

            //if a rename is occurring, make sure to replace the old name with the new one
            ListIterator<String> itr = strategyNames.listIterator();

            if (StringUtils.isNotBlank(oldName)) {
                while (itr.hasNext()) {
                    String strategyName = itr.next();
                    if (StringUtils.equals(oldName, strategyName)) {
                        itr.set(newName);
                    }
                }
            }

            if(selectedName != null && strategyNames.contains(selectedName)) {
                //if a name was already selected and still exists in the updated list, reselect it
                nameBox.setValue(selectedName);

            } else {
                //otherwise, select the first name available
                nameBox.setValue(strategyNames.get(0));
            }

        } else {
            //an error message will inform the author that there are no other strategies to select
            nameBox.setValue(selectedName);
        }

        //update the UI with the new list of strategy names
        nameBox.setAcceptableValues(strategyNames);

        requestValidation(strategyRefValidationStatus);

        updateActivities(nameBox.getValue());
    }

    /**
     * Setter for {@link #stateTransition}.
     *
     * @param stateTransition The {@link StateTransition} for which each
     *        {@link StateExpressionWrapper} is being authored. Can't be null.
     */
    public void setStateTransition(StateTransition stateTransition) {
        if (stateTransition == null) {
            throw new IllegalArgumentException("The parameter 'stateTransition' cannot be null.");
        }

        /** The transition for which expressions are authored */
        this.stateTransition = stateTransition;
    }
}
