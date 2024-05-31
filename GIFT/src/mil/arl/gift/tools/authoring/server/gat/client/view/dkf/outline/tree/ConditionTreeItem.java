/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.outline.tree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.constants.IconType;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.TreeItem;

import generated.dkf.AvoidLocationCondition;
import generated.dkf.Concept;
import generated.dkf.Condition;
import generated.dkf.Conditions;
import generated.dkf.Input;
import generated.dkf.LearnerAction;
import generated.dkf.LearnerActionEnumType;
import generated.dkf.PaceCountCondition;
import mil.arl.gift.common.course.InteropsInfo;
import mil.arl.gift.common.course.InteropsInfo.ConditionInfo;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.EditableInlineLabel;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.ScenarioEditorDirtyEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.ScenarioEventUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelDialog;

/**
 * A tree item that represents a condition in the scenario outline and allows the author to rename it, remove it, and edit it
 *
 * @author nroberts
 */
public class ConditionTreeItem extends ScenarioObjectTreeItem<Condition>{

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(ConditionTreeItem.class.getName());

    /**
     * Creates a new tree item that represents and modifies the given condition and validates it
     * against the given scenario
     *
     * @param condition the condition to represent and modify
     */
    public ConditionTreeItem(final Condition condition) {
        super(condition, true);

        if(logger.isLoggable(Level.FINE)){
            logger.fine("Constructing condition tree item for condition " + condition.toString());
        }

        final EditableInlineLabel nameLabel = getNameLabel();
        nameLabel.setEditingEnabled(false);

        if (condition.getConditionImpl() != null) {

            /* Set a temp name for the tree item until the display name is
             * retrieved */
            nameLabel.setValue("Loading...");

            if (ScenarioClientUtility.isConditionExcluded(condition) != null) {
                /* If the condition was excluded, try to find some useful name
                 * to display other than 'Unknown'. I've chosen to set the class
                 * name. */
                String[] split = condition.getConditionImpl().split("\\.");
                nameLabel.setValue(split[split.length - 1]);
            } else {
                ScenarioClientUtility.getConditionInfoForConditionImpl(condition.getConditionImpl(),
                        new AsyncCallback<InteropsInfo.ConditionInfo>() {

                            @Override
                            public void onSuccess(ConditionInfo conditionInfo) {

                                if (conditionInfo != null && conditionInfo.getDisplayName() != null
                                        && !conditionInfo.getDisplayName().isEmpty()) {
                                    nameLabel.setValue(conditionInfo.getDisplayName());
                                } else {
                                    logger.warning("The server could not find the condition display name for '"
                                            + condition.getConditionImpl() + "'.");
                                    nameLabel.setValue("Unknown");
                                }
                            }

                            @Override
                            public void onFailure(Throwable thrown) {
                                logger.log(Level.SEVERE,
                                        "The server had an problem retrieving the condition information for '"
                                                + condition.getConditionImpl() + "'.",
                                        thrown);
                                nameLabel.setValue("Unknown");
                            }
                        });
            }

        }else{
            nameLabel.setValue("Unknown");
        }


        if(logger.isLoggable(Level.FINE)){
            logger.fine("Initializing delete button logic");
        }

        // button to delete this condition
        addButton(IconType.TRASH, "Delete this condition", new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {

                if(logger.isLoggable(Level.FINE)){
                    logger.fine("Deleting condition " + condition.toString());
                }

                //stop propagation on the click event so that we don't re-select this item
                event.stopPropagation();

                final TreeItem parentItem = getParentItem();

                if(parentItem != null
                        && parentItem instanceof ConceptTreeItem) {

                    final Concept parentConcept = ((ConceptTreeItem) parentItem).getScenarioObject();

                    if(parentConcept.getConditionsOrConcepts() instanceof Conditions){

                        final Conditions conditions = (Conditions) parentConcept.getConditionsOrConcepts();

                        if (condition.getConditionImpl() != null) {

                            ScenarioClientUtility.getConditionInfoForConditionImpl(condition.getConditionImpl(), new AsyncCallback<InteropsInfo.ConditionInfo>() {

                                @Override
                                public void onSuccess(ConditionInfo conditionInfo) {

                                    if(conditionInfo != null && conditionInfo.getDisplayName() != null && !conditionInfo.getDisplayName().isEmpty()){
                                        remove("<b>"+conditionInfo.getDisplayName()+"</b>", condition, parentItem, conditions);
                                    }else{
                                        logger.warning("The server could not find the condition display name for '"+condition.getConditionImpl()+"'.");
                                        remove("this condition", condition, parentItem, conditions);
                                    }

                                }

                                @Override
                                public void onFailure(Throwable thrown) {
                                    logger.log(Level.SEVERE, "The server had an problem retrieving the condition information for '"+condition.getConditionImpl()+"'.", thrown);
                                    remove("this condition", condition, parentItem, conditions);
                                }
                            });
                        } else {
                            remove("this condition", condition, parentItem, conditions);
                        }
                    } else {
                        // should never happen
                        logger.warning(
                                "Somehow we are trying to delete a condition that has a concept parent with children of type other than Condition. Investigate how this happened.");
                    }
                } else {
                    // should never happen
                    logger.warning(
                            "Somehow we are trying to delete a condition that doesn't have a concept parent. Investigate how this happened.");
                }
            }

            private void remove(final String displayName, final Condition condition, final TreeItem parentItem, final Conditions conditions) {

                OkayCancelDialog.show(
                    "Delete Condition?",
                    "Are you sure you want to delete " + displayName + " from its parent concept?",
                    "Delete Condition",
                    new OkayCancelCallback() {

                        @Override
                        public void okay() {

                            if (logger.isLoggable(Level.FINE)) {
                                logger.fine("User confirmed delete");
                            }

                            /* Alert all interested parts of the Scenario
                             * editor that the task has been deleted through
                             * an event */
                            ScenarioEventUtility.fireDeleteScenarioObjectEvent(condition, ((ConceptTreeItem) parentItem).getScenarioObject());

                            /* allow the author to add concepts again after
                             * all the conditions are removed */
                            if (conditions.getCondition().isEmpty()) {
                                if (parentItem instanceof ConceptTreeItem) {

                                    Concept parentConcept = ((ConceptTreeItem) parentItem).getScenarioObject();
                                    parentConcept.setConditionsOrConcepts(null);

                                    ((ConceptTreeItem) parentItem).resetAddButtonTooltip();
                                }
                            }

                            /* If the condition is a PaceCountCondition, ask
                             * the user if they want to delete any
                             * unecessary learner actions */
                            Input input = condition.getInput();
                            if (input != null) {
                                if(input.getType() instanceof PaceCountCondition) {
                                    onDeletePaceCountCondition((PaceCountCondition) input.getType());
                                    
                                } else if(input.getType() instanceof AvoidLocationCondition) {
                                    ScenarioClientUtility.cleanUpAssessLocationLearnerAction();
                                }
                            }

                            if (logger.isLoggable(Level.FINE)) {
                                logger.fine("Finished deleting condition");
                            }
                        }

                        @Override
                        public void cancel() {

                            if(logger.isLoggable(Level.FINE)){
                                logger.fine("User cancelled delete");
                            }
                        }
                    }
                );
            }
        }, false);

        if(logger.isLoggable(Level.FINE)){
            logger.fine("Finished constructing condition tree item");
        }
    }

    /**
     * When a {@link PaceCountCondition} has been deleted, check if there is
     * still: a start learner action, a stop learner action, and another
     * {@link PaceCountCondition}. If not then ask the users if they'd also like
     * to delete the remaining elements. Otherwise do nothing.
     *
     * @param condition The condition that was just deleted. Can't be null.
     */
    private void onDeletePaceCountCondition(PaceCountCondition condition) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("onDeletePaceCountCondition(" + condition + ")");
        }

        if (condition == null) {
            throw new IllegalArgumentException("The parameter 'condition' cannot be null.");
        }

        /* Check to see if this is the last pace count condition */
        boolean isLastPaceCountCondition = true;
        for (Condition c : ScenarioClientUtility.getUnmodifiableConditionList()) {
            /* Skip the current one since we are looking to see if there is
             * another */
            if (c == getScenarioObject()) {
                continue;
            }

            if (c.getInput() != null && c.getInput().getType() instanceof PaceCountCondition) {
                isLastPaceCountCondition = false;
                break;
            }
        }

        /* Check to see if there is a start and stop pace count learner
         * action */
        boolean hasStart = false, hasStop = false;
        for (LearnerAction learnerAction : ScenarioClientUtility.getUnmodifiableLearnerActionList()) {
            hasStart |= learnerAction.getType() == LearnerActionEnumType.START_PACE_COUNT;
            hasStop |= learnerAction.getType() == LearnerActionEnumType.END_PACE_COUNT;
        }

        /* If this isn't the last pace count, or the scenario has neither a
         * start or stop pace count learner action, there is nothing left to
         * do. */
        if (!isLastPaceCountCondition || !hasStart && !hasStop) {
            return;
        }

        /* Construct the prompt to present to the user */
        StringBuilder msgBuilder = new StringBuilder("There are no longer any Pace Count Conditions in the Scenario but there is still ");
        if (hasStart && hasStop) {
            msgBuilder.append("a Start Pace Count and an End Pace Count ");
        } else {
            msgBuilder.append(hasStart ? "a Start" : "an End Pace Count");
        }

        final String msg = msgBuilder.append(" learner action.<br/> Would you like to delete ")
                .append(hasStart && hasStop ? "these learner actions" : "this learner action")
                .append(" as well?").toString();

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

    @Override
    public void onDrop(ScenarioObjectTreeItem<? extends Serializable> dragged) {

        if(dragged != null
                && dragged.getScenarioObject() instanceof Condition
                && !dragged.getScenarioObject().equals(getScenarioObject())) {

            Condition dragCondition = (Condition) dragged.getScenarioObject();

            //if a condition is dragged on top of this condition, place the dragged condition after this condition
            Conditions fromConditions = null;
            Conditions toConditions = null;

            //get the list of conditions that the dragged item should be removed from
            Serializable fromParent = ((ScenarioObjectTreeItem<?>) dragged.getParentItem()).getScenarioObject();

            if(fromParent instanceof Concept) {

                Concept parentConcept = (Concept) fromParent;

                if(parentConcept.getConditionsOrConcepts() instanceof Conditions) {
                    fromConditions = (Conditions) parentConcept.getConditionsOrConcepts();
                }
            }

            //get the list of conditions that the dragged item should be moved to
            Serializable toParent = ((ScenarioObjectTreeItem<?>) getParentItem()).getScenarioObject();

            if(toParent instanceof Concept) {

                Concept parentConcept = (Concept) toParent;

                if(parentConcept.getConditionsOrConcepts() instanceof Conditions) {
                    toConditions = (Conditions) parentConcept.getConditionsOrConcepts();
                }
            }

            if(fromConditions != null && toConditions != null) {

                //remove the dragged item from its original list of conditions
                fromConditions.getCondition().remove(dragCondition);

                if(fromConditions.getCondition().isEmpty()) {

                  //if the list of conditions is empty, remove it
                    if(fromParent instanceof Concept) {
                        ((Concept) fromParent).setConditionsOrConcepts(null);
                    }
                }

                //insert the dragged item into the list of conditions it was dragged over
                int dropIndex = toConditions.getCondition().indexOf(getScenarioObject());

                toConditions.getCondition().add(dropIndex, dragCondition);

                boolean wasDragItemSelected = dragged.isSelected();

                dragged.remove();

                //move the tree items accordingly
                for(int index = 0; index < getParentItem().getChildCount(); index++) {

                    if(getParentItem().getChild(index).equals(this)) {

                        getParentItem().insertItem(index, dragged);

                        if(wasDragItemSelected) {

                            //if the dragged item was selected, we need to re-select it
                            dragged.setSelected(true);
                        }

                        break;
                    }
                }

                //update the validation state of the objects affected by this move
                SharedResources.getInstance().getEventBus().fireEvent(new ScenarioEditorDirtyEvent(fromParent));

                if(!toParent.equals(fromParent)) {
                    SharedResources.getInstance().getEventBus().fireEvent(new ScenarioEditorDirtyEvent(toParent));
                }
            }
        }
    }

    @Override
    public boolean allowDrop(ScenarioObjectTreeItem<?> otherItem) {
        return otherItem != null
                && otherItem.getScenarioObject() instanceof Condition
                && !otherItem.getScenarioObject().equals(getScenarioObject());
    }
}