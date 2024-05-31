/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.header.editor;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.Concept;
import generated.dkf.LearnerAction;
import generated.dkf.LearnerActionReference;
import generated.dkf.Scenario;
import generated.dkf.Strategy;
import generated.dkf.StrategyApplied;
import generated.dkf.Scenario.EndTriggers.Trigger;
import generated.dkf.Task;
import generated.dkf.TaskEnded;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.gwt.client.validation.ValidationWidget;
import mil.arl.gift.common.util.StringUtils.Stringifier;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.ScenarioTriggerUtil;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.task.AddScenarioEndTriggerWidget;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.ScenarioEventUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.CreateListAction;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemField;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemListEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemListEditor.ListChangedCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ListChangedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.validation.ScenarioValidationComposite;

/**
 * The panel that displays the editor for modifying {@link Scenario.EndTriggers}.
 * 
 * @author tflowers
 */
public class ScenarioEndTriggersPanel extends ScenarioValidationComposite {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(ScenarioEndTriggersPanel.class.getName());

    /** The binder that combines this java class with the ui.xml */
    private static EndTriggersPanelUiBinder uiBinder = GWT.create(EndTriggersPanelUiBinder.class);

    /** Defines the binder that combines the java class with the ui.xml */
    interface EndTriggersPanelUiBinder extends UiBinder<Widget, ScenarioEndTriggersPanel> {
    }
    
    /** widget used to render the panel for authoring a scenario end trigger */
    AddScenarioEndTriggerWidget endTriggerWidget = new AddScenarioEndTriggerWidget();

    /** The editor that modifies the list of {@link Trigger triggers} */
    @UiField(provided = true)
    ItemListEditor<Trigger> endTriggersEditor = new ItemListEditor<>(endTriggerWidget);

    /** The panel showing validation errors */
    @UiField(provided = true)
    ValidationWidget validationWidget = new ValidationWidget(this);

    /**
     * Instantiates a new dkf scenario end trigger panel.
     */
    public ScenarioEndTriggersPanel() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(".ctor()");
        }

        initWidget(uiBinder.createAndBindUi(this));

        /* Adds the field to the editor */
        ItemField<Trigger> descriptionField = new ItemField<Trigger>("Description", null) {

            @Override
            public Widget getViewWidget(Trigger item) {
                return new HTML(ScenarioTriggerUtil.buildTriggerDescription(item));
            }
        };
        endTriggersEditor.setFields(Arrays.asList(descriptionField));

        /* Creates the add actions */
        endTriggersEditor.addCreateListAction("Click here to add a new stop event", new CreateListAction<Trigger>() {
            @Override
            public Trigger createDefaultItem() {
                Trigger trigger = new Trigger();
                trigger.setTriggerType(new TaskEnded());
                return trigger;
            }
        });

        endTriggersEditor.setRemoveItemStringifier(new Stringifier<Trigger>() {
            @Override
            public String stringify(Trigger obj) {
                return "this stop event";
            }
        });

        endTriggersEditor.addListChangedCallback(new ListChangedCallback<Trigger>() {
            @Override
            public void listChanged(ListChangedEvent<Trigger> event) {
                // need to revalidate outline
                ScenarioEventUtility.fireDirtyEditorEvent(ScenarioClientUtility.getScenario().getEndTriggers());
            }
        });

        setReadonly(ScenarioClientUtility.isReadOnly());

        // needs to be called last
        initValidationComposite(validationWidget);
    }
    
    /**
     * Populates the panel using the data within the given {@link Scenario#getEndTriggers()}.
     * 
     * @param endTriggers the data object that will be used to populate the panel. Can't be null.
     */
    public void edit(Scenario.EndTriggers endTriggers) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("edit(" + endTriggers + ")");
        }

        if (endTriggers == null) {
            throw new IllegalArgumentException("The 'endTriggers' parameter can't be null");
        }

        /* Binds the editor to directly edit the list of the end triggers that were passed in */
        endTriggersEditor.setItems(endTriggers.getTrigger());

        setReadonly(ScenarioClientUtility.isReadOnly());
    }

    /**
     * Refreshes the trigger tables for the renamed {@link Task}, {@link Concept}, or
     * {@link LearnerAction}
     * 
     * @param triggerItem the item that was renamed.
     * @param oldName the old name of the item.
     * @param newName the new name of the item.
     */
    public void handleTriggerItemRename(Serializable triggerItem, String oldName, String newName) {
        if (triggerItem == null) {
            throw new IllegalArgumentException("The parameter 'triggerItem' cannot be null.");
        } else if (!(triggerItem instanceof Task || triggerItem instanceof Concept
                || triggerItem instanceof LearnerAction || triggerItem instanceof Strategy)) {
            throw new IllegalArgumentException(
                    "The parameter 'triggerItem' must be of type Task, Concept, or Learner Action.");
        }

        boolean isLearnerAction = triggerItem instanceof LearnerAction;
        boolean isTaskOrConcept = triggerItem instanceof Task || triggerItem instanceof Concept;
        boolean isStrategy = triggerItem instanceof Strategy;

        BigInteger nodeIdToCheck = null;

        // get node id
        if (isTaskOrConcept) {
            nodeIdToCheck = triggerItem instanceof Task ? ((Task) triggerItem).getNodeId()
                    : ((Concept) triggerItem).getNodeId();
        }

        // apply rename to end triggers that match the node id
        for (Trigger trigger : endTriggersEditor.getItems()) {
            /* the editor can have multiple of the same trigger type so we can't break out */
            if (isLearnerAction) {
                LearnerActionReference learnerRef = ScenarioTriggerUtil.doesTriggerMatchLearnerActionName(trigger,
                        oldName);
                if (learnerRef != null) {
                    learnerRef.setName(newName);
                    endTriggersEditor.refresh(trigger);
                }
            }else if(isStrategy){
                StrategyApplied sApplied = ScenarioTriggerUtil.doesTriggerMatchStrategyName(trigger, oldName);
                if(sApplied != null){
                    sApplied.setStrategyName(newName);
                    endTriggersEditor.refresh(trigger);
                }
            } else if (ScenarioTriggerUtil.doesTriggerMatchNodeId(trigger, nodeIdToCheck)) {
                endTriggersEditor.refresh(trigger);
            }
        }
    }

    /**
     * Handles when a trigger item is deleted. Removes the deleted item from the trigger editor.
     */
    public void handleTriggerItemDeleted() {
        /* The deleted item was already removed from the lists by reference. Just rebuild the tables
         * to reflect the change. Do not need to force rebuild. */
        endTriggersEditor.redrawListEditor(false);
    }
    
    /**
     * A new strategy was added.  Notify trigger widget to update widgets.
     * 
     * @param strategy the strategy being added
     */
    public void addStrategy(Strategy strategy){
        endTriggerWidget.updateStrategyList();
    }

    /**
     * Revalidates the invalid list editor rows because they could contain the learner start
     * location and cause the row to become valid.
     */
    public void handleLearnerStartLocationUpdate() {
        endTriggersEditor.revalidateInvalidItems();
    }
    
    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        // no validation statuses
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        // nothing to validate
    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
        childValidationComposites.add(endTriggersEditor);
    }

    /**
     * Sets the components to read only mode which prevents users from making changes.
     * 
     * @param isReadonly True to prevent editing, false to allow editing.
     */
    public void setReadonly(boolean isReadonly) {
        endTriggersEditor.setReadonly(isReadonly);
    }
}