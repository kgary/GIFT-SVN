/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.header.editor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.AvailableLearnerActions;
import generated.dkf.AvoidLocationCondition;
import generated.dkf.Concept;
import generated.dkf.Condition;
import generated.dkf.Conditions;
import generated.dkf.LearnerAction;
import generated.dkf.LearnerActionEnumType;
import generated.dkf.LearnerActionsList;
import generated.dkf.PaceCountCondition;
import generated.dkf.Strategy;
import mil.arl.gift.common.gwt.client.validation.ModelValidationStatus;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.gwt.client.validation.ValidationWidget;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ModalDialogBox;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.common.util.StringUtils.Stringifier;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.header.editor.LearnerActionEditor.LearnerActionType;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioValidatorUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.ScenarioEventUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.CreateListAction;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemField;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemListEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemListEditor.ListChangedCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ListChangedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ListChangedEvent.ListAction;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.validation.ScenarioValidationComposite;

/**
 * The Class LearnerActionsPanel.
 */
public class LearnerActionsPanel extends ScenarioValidationComposite {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(LearnerActionsPanel.class.getName());

    /** The UiBinder that combines the ui.xml with this java class */
    private static LearnerActionsPanelUiBinder uiBinder = GWT.create(LearnerActionsPanelUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface LearnerActionsPanelUiBinder extends UiBinder<Widget, LearnerActionsPanel> {
    }

    /** The learner actions data grid. */
    @UiField(provided = true)
    protected ItemListEditor<LearnerAction> learnerActionsDataGrid = new ItemListEditor<LearnerAction>(new LearnerActionEditor()) {
        
        @Override
        public void remove(final LearnerAction targetAction) {
            
            if(targetAction != null && targetAction.getType() == LearnerActionEnumType.ASSESS_MY_LOCATION) {
                
                //determine if any Avoid Location conditions would be adversely affected by the removal of this action
                LearnerActionsList learnerActions = ScenarioClientUtility.getLearnerActions();
                boolean hasOtherAssessLocationActions = false;

                if(learnerActions != null) {
                    for(LearnerAction action : learnerActions.getLearnerAction()) {
                        
                        if(!action.equals(targetAction) && LearnerActionEnumType.ASSESS_MY_LOCATION.equals(action.getType())){
                            hasOtherAssessLocationActions = true;
                            break;
                        }
                    }
                }
                
                if(hasOtherAssessLocationActions) {
                    
                    //if there are other 'Assess My Location' actions, then no need to clean up references
                    delete(targetAction);
                    
                } else {
                
                    //the last 'Assess My Location' action is being removed, so need to check if references need to be cleaned up
                    final List<AvoidLocationCondition> conditionsToCleanUp = new ArrayList<AvoidLocationCondition>();
                    for (Condition c : ScenarioClientUtility.getUnmodifiableConditionList()) {
    
                        if (c.getInput() != null 
                                && c.getInput().getType() instanceof AvoidLocationCondition) {
                            
                            AvoidLocationCondition alc = (AvoidLocationCondition) c.getInput().getType();
                            
                            if(alc.isRequireLearnerAction() != null && alc.isRequireLearnerAction()) {
                                conditionsToCleanUp.add((AvoidLocationCondition) c.getInput().getType());
                            }
                        }
                    }
                    
                    if(!conditionsToCleanUp.isEmpty()) {
                        
                        /* Construct the prompt to present to the user */
                        final ModalDialogBox referencesDialog = new ModalDialogBox();
                        referencesDialog.setGlassEnabled(true);
                        referencesDialog.getElement().getStyle().setProperty("max-width", "700px");
                        referencesDialog.setCloseable(true);
                        referencesDialog.getCloseButton().setText("Cancel");
                        
                        referencesDialog.setText("Delete References(s)?");
                        referencesDialog.setWidget(new HTML("One or more Avoid Location conditions still require this action "
                                + "in order to allow the learner to check their location.<br/><br/>Would you like GIFT to remove "
                                + "the references to this action from these conditions so that they no longer require the "
                                + "learner to check their location?"));
                        
                        Button actionReferencesButton = new Button("Delete Action and References", new ClickHandler() {
                            
                            @Override
                            public void onClick(ClickEvent event) {
                                
                                //remove references to the action
                                for (AvoidLocationCondition condition : conditionsToCleanUp) {
                                    condition.setRequireLearnerAction(null);
                                }
                                
                                //delete the action
                                delete(targetAction);
                                
                                referencesDialog.hide();
                            }
                        });
                        actionReferencesButton.setType(ButtonType.PRIMARY);
                        referencesDialog.setFooterWidget(actionReferencesButton);
                        
                        Button actionOnlyButton = new Button("Delete Action Only", new ClickHandler() {
                            
                            @Override
                            public void onClick(ClickEvent event) {
                                
                                //delete the action only
                                delete(targetAction);
                                
                                referencesDialog.hide();
                            }
                        });
                        actionOnlyButton.setType(ButtonType.PRIMARY);
                        referencesDialog.setFooterWidget(actionOnlyButton);
                
                        /* Schedule deferred allows the currently shown dialog to be hidden
                         * before reshowing it. */
                        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                
                            @Override
                            public void execute() {
                                referencesDialog.center();
                            }
                        });
                        
                    } else {
                        
                        //no conditions require this action, so simply delete it normally
                        delete(targetAction);
                    }
                }
                
            } else {
                
                //simply delete the action normally
                delete(targetAction);
            }
        }
        
        /**
         * Deletes the given action regardless of any references it may have
         * 
         * @param action the action to delete
         */
        private void delete(LearnerAction action) {
            super.remove(action);
        }
    };

    /**
     * The widget that displays validation errors for the {@link AvailableLearnerActions}
     */
    @UiField(provided = true)
    protected final ValidationWidget validationWidget = new ValidationWidget(this);

    /** The display name column. */
    private ItemField<LearnerAction> displayNameColumn = new ItemField<LearnerAction>() {

        @Override
        public Widget getViewWidget(LearnerAction learnerAction) {
            if (learnerAction != null && learnerAction.getDisplayName() != null) {
                return new HTML(learnerAction.getDisplayName());
            }

            return new HTML("");
        }
    };

    /** The type column. */
    private final ItemField<LearnerAction> typeColumn = new ItemField<LearnerAction>() {

        @Override
        public Widget getViewWidget(LearnerAction learnerAction) {
            if (learnerAction != null && learnerAction.getType() != null) {
                LearnerActionType typeFromAction = LearnerActionType.getTypeFromAction(learnerAction);
                String typeName = typeFromAction != null ? typeFromAction.getDisplayName() : "";
                return new HTML(typeName);
            }

            return new HTML("");
        }
    };
    
    /** The additional info column. */
    private final ItemField<LearnerAction> additionalInfoColumn = new ItemField<LearnerAction>() {

        @Override
        public Widget getViewWidget(LearnerAction learnerAction) {
            if (learnerAction != null && learnerAction.getType() != null) {
                LearnerActionType typeFromAction = LearnerActionType.getTypeFromAction(learnerAction);
                if(typeFromAction.equals(LearnerActionType.APPLY_STRATEGY)){
                    Serializable actionParams = learnerAction.getLearnerActionParams();
                    if(actionParams instanceof generated.dkf.LearnerAction.StrategyReference){
                        generated.dkf.LearnerAction.StrategyReference strategyRef = (generated.dkf.LearnerAction.StrategyReference)actionParams;
                        if(StringUtils.isNotBlank(strategyRef.getName())){
                            return new HTML("strategy: "+strategyRef.getName());
                        }
                    }
                }
            }

            return new HTML("");
        }
    };
    
    /** The jump to column. */
    private final ItemField<LearnerAction> jumpToColumn = new ItemField<LearnerAction>() {

        @Override
        public Widget getViewWidget(LearnerAction learnerAction) {
            if (learnerAction != null && learnerAction.getType() != null) {
                LearnerActionType typeFromAction = LearnerActionType.getTypeFromAction(learnerAction);
                if(typeFromAction.equals(LearnerActionType.APPLY_STRATEGY)){
                    Serializable actionParams = learnerAction.getLearnerActionParams();
                    if(actionParams instanceof generated.dkf.LearnerAction.StrategyReference){
                        final generated.dkf.LearnerAction.StrategyReference strategyRef = (generated.dkf.LearnerAction.StrategyReference)actionParams;
                        if(StringUtils.isNotBlank(strategyRef.getName())){
                            Icon icon = new Icon(IconType.EXTERNAL_LINK);
                            icon.addClickHandler(new ClickHandler() {
                                
                                @Override
                                public void onClick(ClickEvent event) {

                                    Strategy strategy = ScenarioClientUtility.getStrategyWithName(strategyRef.getName());
                                    if(strategy != null){
                                        ScenarioEventUtility.fireJumpToEvent(strategy);
                                    }

                                }
                            });
                            icon.setTitle("Jump to strategy");
                            icon.getElement().getStyle().setCursor(com.google.gwt.dom.client.Style.Cursor.POINTER);
                            return icon;
                        }
                    }
                }
            }

            return new HTML("");
        }
    };

    /** The {@link AvailableLearnerActions} that are currently being edited. */
    private AvailableLearnerActions learnerActions;

    /** The list of {@link LearnerActionsList} that may be included in the data model */
    private LearnerActionsList learnerActionsList;

    /** The status that validates pace count actions a present and necessary */
    private final ModelValidationStatus paceCountStatus = new ModelValidationStatus("The pace count learner actions are unnecessary") {
        @Override
        protected void fireJumpToEvent(Serializable modelObject) {
            ScenarioEventUtility.fireJumpToEvent(modelObject);
        }
    };

    /**
     * Instantiates a new learner actions editor.
     */
    public LearnerActionsPanel() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(".ctor()");
        }

        initWidget(uiBinder.createAndBindUi(this));

        initLearnerActionsTable();

        setReadonly(ScenarioClientUtility.isReadOnly());

        // needs to be called last
        initValidationComposite(validationWidget);
    }

    /**
     * Inits the learner action data grid.
     */
    private void initLearnerActionsTable() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("initLearnerActionsTable()");
        }

        learnerActionsDataGrid.addListChangedCallback(new ListChangedCallback<LearnerAction>() {
            @Override
            public void listChanged(ListChangedEvent<LearnerAction> event) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("learnerActionsDataGrid.listChanged(" + event + ")");
                }

                LearnerAction affectedAction = event.getAffectedItems().get(0);
                if (event.getActionPerformed() == ListAction.REMOVE) {
                    ScenarioEventUtility.fireDeleteScenarioObjectEvent(affectedAction, null);

                    /* If the learner action is a start or end pace count
                     * action, additional logic needs to be run since there is
                     * validation logic that depends on both a start and end
                     * pace count learner action being present as well as a
                     * PaceCountCondition. */
                    if (affectedAction.getType() == LearnerActionEnumType.START_PACE_COUNT
                            || affectedAction.getType() == LearnerActionEnumType.END_PACE_COUNT) {
                        onDeletePaceCountAction(affectedAction);
                        
                    }
                    
                } else if (event.getActionPerformed() == ListAction.ADD) {
                    ScenarioEventUtility.fireCreateScenarioObjectEvent(affectedAction);
                    if (ScenarioClientUtility.getUnmodifiableLearnerActionList().isEmpty()) {
                        learnerActions.setLearnerActionsList(learnerActionsList);
                    }
                }

                requestValidationAndFireDirtyEvent(learnerActions, paceCountStatus);
            }
        });

        learnerActionsDataGrid.addCreateListAction("Create Learner Action", new CreateListAction<LearnerAction>() {

            @Override
            public LearnerAction createDefaultItem() {

                LearnerAction newLearnerAction = new LearnerAction();
                newLearnerAction.setDisplayName(null);
                newLearnerAction.setType(null);

                return newLearnerAction;
            }
        });

        learnerActionsDataGrid.setRemoveItemDialogTitle("Delete Learner Action");
        learnerActionsDataGrid.setRemoveItemStringifier(new Stringifier<LearnerAction>() {

            @Override
            public String stringify(LearnerAction obj) {
                return obj != null ? obj.getDisplayName() : "this learner action";
            }
        });

        String placeholder = "No learner actions have been created; therefore, no action buttons will be presented to the learner.";
        learnerActionsDataGrid.setPlaceholder(placeholder);

        learnerActionsDataGrid.setFields(Arrays.asList(displayNameColumn, typeColumn, additionalInfoColumn, jumpToColumn));
    }

    /**
     * When either a start pace count or stop pace count learner action has been
     * deleted, check if there is still: a start learner action, a stop learner
     * action, and a {@link PaceCountCondition}. If not then ask the users if
     * they'd also like to delete the remaining elements. Otherwise do nothing.
     *
     * @param deletedAction The learner action that was just deleted. Can't be
     *        null.
     */
    private void onDeletePaceCountAction(LearnerAction deletedAction) {
        if (deletedAction == null) {
            throw new IllegalArgumentException("The parameter 'deletedAction' cannot be null.");
        }

        boolean hasStart = false, hasEnd = false;
        for (LearnerAction action : ScenarioClientUtility.getUnmodifiableLearnerActionList()) {
            /* Ignore the deleted action if it is still in the list */
            if (action == deletedAction) {
                continue;
            }

            hasStart |= action.getType() == LearnerActionEnumType.START_PACE_COUNT;
            hasEnd |= action.getType() == LearnerActionEnumType.END_PACE_COUNT;
        }

        boolean hasPaceCountCondition = false;
        for (Condition condition : ScenarioClientUtility.getUnmodifiableConditionList()) {
            if (condition.getInput() != null && condition.getInput().getType() instanceof PaceCountCondition) {
                hasPaceCountCondition = true;
                break;
            }
        }

        /* If there is no invalid state, no need to prompt for delete */
        if (hasPaceCountCondition && hasStart && hasEnd || !hasPaceCountCondition && !hasStart && !hasEnd) {
            return;
        }

        /* Construct the prompt to present to the user */
        final String msg = new StringBuilder("There is no longer ")
                .append(deletedAction.getType() == LearnerActionEnumType.START_PACE_COUNT ? "a Start Pace Count" : "an End Pace Count")
                .append(" in the Scenario but there is still ")
                .append(hasStart ? "a Start Pace Count" : "")
                .append(hasEnd ? "an End Pace Count" : "")
                .append(hasStart || hasEnd ? " learner action" : "")
                .append(hasPaceCountCondition ? " and a Pace Count condition" : "")
                .append(".<br/>Would you like to delete the remaining items?")
                .toString();

        final String title = "Delete Learner Action(s)?";

        /* Schedule deferred allows the currently shown dialog to be hidden
         * before reshowing it. */
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            @Override
            public void execute() {
                OkayCancelDialog.show(title, msg, null, "Delete", "Keep", new OkayCancelCallback() {

                    @Override
                    public void okay() {
                        /* Delete all the relevant learner actions */
                        Collection<LearnerAction> actionsToDelete = new ArrayList<>();
                        for (LearnerAction action : ScenarioClientUtility.getUnmodifiableLearnerActionList()) {
                            LearnerActionEnumType type = action.getType();
                            if (type == LearnerActionEnumType.END_PACE_COUNT
                                    || type == LearnerActionEnumType.START_PACE_COUNT) {
                                actionsToDelete.add(action);
                            }
                        }

                        for (LearnerAction action : actionsToDelete) {
                            ScenarioEventUtility.fireDeleteScenarioObjectEvent(action, null);
                            ScenarioEventUtility.fireDirtyEditorEvent(learnerActions);
                        }

                        Map<Concept, Collection<Condition>> conditionsToDelete = new HashMap<>();
                        for (Concept concept : ScenarioClientUtility.getUnmodifiableConceptList()) {
                            
                            Serializable conditionOrConcept = concept.getConditionsOrConcepts();
                            if(conditionOrConcept instanceof Conditions){
                                // Because the getUnmodifiableConceptList returns all concepts under all tasks
                                // including subconcepts, we can ignore concepts that are subconcepts
                                
                                for(Condition condition : ((Conditions)conditionOrConcept).getCondition()){
                                    if (condition.getInput() != null
                                            && condition.getInput().getType() instanceof PaceCountCondition) {
                                        
                                        Collection<Condition> conditionList = conditionsToDelete.get(concept);
                                        if(conditionList == null){
                                            conditionList = new ArrayList<>();
                                        }
                                        conditionList.add(condition);
                                        conditionsToDelete.put(concept, conditionList);
                                    }

                                }
                            }

                        }

                        for (Concept concept : conditionsToDelete.keySet()) {
                            Collection<Condition> conditions = conditionsToDelete.get(concept);
                            for(Condition condition : conditions){
                                ScenarioEventUtility.fireDeleteScenarioObjectEvent(condition, concept);
                                ScenarioEventUtility.fireDirtyEditorEvent(learnerActions);
                            }
                        }
                    }

                    @Override
                    public void cancel() {
                        // Nothing to do
                    }
                });
            }
        });
    }

    /**
     * Populates the panel using the data within the given {@link AvailableLearnerActions}.
     *
     * @param learnerActions the data object that will be used to populate the panel. Can't be null.
     */
    public void edit(AvailableLearnerActions learnerActions) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("edit(" + learnerActions + ")");
        }

        if (learnerActions == null) {
            throw new IllegalArgumentException("The 'learnerActions' parameter can't be null");
        }

        /* Holds references or creates instances of the objects to edit */
        this.learnerActions = learnerActions;

        if (learnerActions.getLearnerActionsList() != null) {
            learnerActionsList = learnerActions.getLearnerActionsList();
        } else {
            learnerActionsList = new LearnerActionsList();
            learnerActions.setLearnerActionsList(learnerActionsList);
        }

        // initialize Learner Action list display
        learnerActionsDataGrid.setItems(learnerActionsList.getLearnerAction());

        setReadonly(ScenarioClientUtility.isReadOnly());
    }
    
    /**
     * Opens the item list editor for editing the specified learner action
     * @param learnerAction the learner action to open the editor for
     */
    public void edit(LearnerAction learnerAction){
        learnerActionsDataGrid.editExisting(learnerAction);
    }

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        validationStatuses.add(paceCountStatus);
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("validate(" + validationStatus + ")");
        }

        if (paceCountStatus.equals(validationStatus)) {
            
            if(learnerActions != null && learnerActions.getLearnerActionsList() != null){
                String msg = ScenarioValidatorUtility.validatePaceCountLearnerActionDependency(
                        learnerActions.getLearnerActionsList().getLearnerAction(), true);
                if (msg != null) {
                    validationStatus.setErrorMessage(msg);
                    validationStatus.setInvalid();
                } else {
                    validationStatus.setValid();
                }
            }else{
                validationStatus.setValid();
            }
        }
    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
        childValidationComposites.add(learnerActionsDataGrid);
    }

    /**
     * Rebuilds the learner action table
     */
    public void rebuildLearnerActionTable() {
        learnerActionsDataGrid.redrawListEditor(false);
    }

    /**
     * Refreshes the row containing the provided learner action.
     *
     * @param learnerAction the learner action
     */
    public void refreshLearnerAction(LearnerAction learnerAction) {
        learnerActionsDataGrid.refresh(learnerAction);
    }
    
    /**
     * Removes the provided {@link Strategy} from the list of available strategies within the
     * {@link #learnerActionsDataGrid}
     *
     * @param strategy The {@link Strategy} to remove from the UI.
     */
    public void removeReferencedStrategy(Strategy strategy) {
        
        boolean changeMade = false;
        /* Remove the strategy reference from the learner action's list of strategies */
        for (LearnerAction learnerAction : learnerActionsDataGrid.getItems()) {
            Serializable actionParams = learnerAction.getLearnerActionParams();
            if (actionParams instanceof generated.dkf.LearnerAction.StrategyReference) {
                generated.dkf.LearnerAction.StrategyReference strategyRef = (generated.dkf.LearnerAction.StrategyReference)actionParams;
                if (StringUtils.equals(strategyRef.getName(), strategy.getName())) {
                    changeMade = true;
                    strategyRef.setName(null);  //TODO - currently this will set the referenced strategy to the first in the list, need better UX
                    learnerActionsDataGrid.refresh(learnerAction);
                }
            }
        }

        if(changeMade){
            ScenarioEventUtility.fireDirtyEditorEvent(learnerActions);
            
            learnerActionsDataGrid.redrawListEditor(true);
        }

    }
    
    /**
     * Updates the dropdown of {@link Strategy strategies} to reflect the name change.
     *
     * @param oldName the old name of the {@link Strategy} before it was renamed. If null, nothing
     *        will change.
     * @param newName the new name of the {@link Strategy}. If blank, nothing will change.
     */
    public void handleStrategyRename(String oldName, String newName) {
        if (StringUtils.isBlank(oldName) || StringUtils.isBlank(newName)) {
            return;
        }
        
        boolean changeMade = false;

        // if old name is found, update in table
        for (LearnerAction learnerAction : learnerActionsDataGrid.getItems()) {
            Serializable actionParams = learnerAction.getLearnerActionParams();
            if (actionParams instanceof generated.dkf.LearnerAction.StrategyReference) {
                generated.dkf.LearnerAction.StrategyReference strategyRef = (generated.dkf.LearnerAction.StrategyReference)actionParams;
                if (StringUtils.equals(strategyRef.getName(), oldName)) {
                    
                    changeMade = true;

                    // updates the data model
                    strategyRef.setName(newName);

                    learnerActionsDataGrid.refresh(learnerAction);
                }
            }
        }
        
        if(changeMade){
            ScenarioEventUtility.fireDirtyEditorEvent(learnerActions);
            
            learnerActionsDataGrid.redrawListEditor(true);
        }
    }

    /**
     * Sets the sub-components to read only mode which prevents users from making changes.
     *
     * @param isReadonly True to prevent editing, false to allow editing.
     */
    public void setReadonly(boolean isReadonly) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("setReadonly(" + isReadonly + ")");
        }

        learnerActionsDataGrid.setReadonly(isReadonly);
    }

}