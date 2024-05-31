/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.transition;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.extras.select.client.ui.Option;
import org.gwtbootstrap3.extras.select.client.ui.Select;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.Actions.StateTransitions.StateTransition;
import generated.dkf.Concept;
import generated.dkf.LearnerStateTransitionEnum;
import generated.dkf.PerformanceNode;
import generated.dkf.Task;
import mil.arl.gift.common.enums.AbstractEnum;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;
import mil.arl.gift.common.gwt.client.util.ScenarioElementUtil;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.gwt.client.validation.WidgetValidationStatus;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared.Ribbon;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.transition.StateTransitionPanel.StateExpressionWrapper;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioValidatorUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.ScenarioEventUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemEditor;

/**
 * A widget used to add and edit state expressions.
 *
 * @author sharrison
 */
public class StateExpressionItemEditor extends ItemEditor<StateExpressionWrapper> {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(StateExpressionItemEditor.class.getName());

    /** The UiBinder that combines the ui.xml with this java class */
    private static StateExpressionItemEditorUiBinder uiBinder = GWT.create(StateExpressionItemEditorUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface StateExpressionItemEditorUiBinder extends UiBinder<Widget, StateExpressionItemEditor> {
    }

    /** Display string for a null previous or current property label */
    private static final String ANYTHING = "Anything";

    /**
     * The different types of state expressions
     *
     * @author sharrison
     */
    private enum ExpressionType {
        /** {@link Task} type */
        TASK,
        /** {@link Concept} type */
        CONCEPT,
        /** {@link LearnerStateTransitionEnum} type */
        LEARNER_STATE
    }

    /** The main deck that contains the ribbon and edit panel */
    @UiField
    protected DeckPanel mainDeck;

    /** The ribbon that allows the author to choose which state type to author */
    @UiField
    protected Ribbon choiceRibbon;

    /** The panel that gets displayed when a choice is selected in the {@link #choiceRibbon} */
    @UiField
    protected FlowPanel editPanel;

    /** The performance or learner state to use in the state expression */
    @UiField
    protected Select stateSelect;

    /** A dynamic label that changes based on what type is selected */
    @UiField
    protected InlineHTML typeLabel;
    
    /** contains 'of' when {@link #typeLabel} is 'state' and 'changes from' when {@link #typeLabel} is 'performance' */
    @UiField
    protected InlineHTML actionLabel;
    
    /** contains 'is' when {@link #typeLabel} is 'state' and 'to' when {@link #typeLabel} is 'performance' */
    @UiField
    protected InlineHTML toLabel;

    /** The panel containing the property dropdowns */
    @UiField
    protected FlowPanel propertyPanel;
    
    /** the panel containing the current property select widget (a subpanel of {@link #propertyPanel}*/
    @UiField
    protected FlowPanel currentPropertyPanel;

    /** The previous property dropdown */
    @UiField
    protected Select previousPropertySelect;

    /** The current property dropdown (e.g. learner state attributes, dkf task names, dkf concept names */
    @UiField
    protected Select currentPropertySelect;
    
    /** The select for course concepts, used when learner state attribute is selected in {@link #stateSelect} that is exclusive to course concepts */
    @UiField
    protected Select courseConceptSelect;

    /** The button to change the state type */
    @UiField
    protected Button changeTypeButton;

    /** The transition for which expressions are authored */
    private StateTransition stateTransition;

    /** The expression type that was selected by the author */
    private ExpressionType chosenExpressionType;

    /** The ribbon item for the {@link Task} type */
    private final Widget taskRibbonButton;

    /** The ribbon item for the {@link Concept} type */
    private final Widget conceptRibbonButton;

    /** The ribbon item for the {@link LearnerStateTransitionEnum} type */
    private final Widget learnerStateRibbonButton;

    /** A copy of the expression currently being edited */
    private StateExpressionWrapper selectedExpressionCopy;

    /** The container for showing validation messages for not having a state selected. */
    private final WidgetValidationStatus stateSelectedValidation;

    /**
     * The container for showing validation messages for having both the previous and current
     * property being null (anything).
     */
    private final WidgetValidationStatus propertyValidation;
    
    /**
     * container for showing validation messages when the state is a learner state, that learner state is a course concept 
     * based enumeration and the current state selector is on 'Anything'
     */
    private final WidgetValidationStatus currentStateValidationWhenCourseConcept;

    /**
     * Creates a new editor for modifying state expressions.
     */
    public StateExpressionItemEditor() {
        initWidget(uiBinder.createAndBindUi(this));

        stateSelectedValidation = new WidgetValidationStatus(stateSelect,
                "There must be a type selected for the criteria.");

        propertyValidation = new WidgetValidationStatus(propertyPanel,
                "Both the previous and current property values cannot be '" + ANYTHING + "'.");
        
        currentStateValidationWhenCourseConcept = new WidgetValidationStatus(currentPropertyPanel,
                "Select a value for the learner state attribute that is already set.");

        taskRibbonButton = choiceRibbon.addRibbonItem(ScenarioElementUtil.getIconFromType(Task.class), "Authored Tasks",
                "Select this to add a criteria using an authored Task.", new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        if (chosenExpressionType != ExpressionType.TASK) {
                            chosenExpressionType = ExpressionType.TASK;
                            updateTypeLabel();
                            updateStates(true);
                        }
                        showDeckWidget(mainDeck.getWidgetIndex(editPanel));
                        validateAll();
                    }
                });

        conceptRibbonButton = choiceRibbon.addRibbonItem(ScenarioElementUtil.getIconFromType(Concept.class),
                "Authored Concepts", "Select this to add a criteria using an authored Concept.", new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        if (chosenExpressionType != ExpressionType.CONCEPT) {
                            chosenExpressionType = ExpressionType.CONCEPT;
                            updateTypeLabel();
                            updateStates(true);
                        }
                        showDeckWidget(mainDeck.getWidgetIndex(editPanel));
                        validateAll();
                    }
                });

        learnerStateRibbonButton = choiceRibbon.addRibbonItem(IconType.LEANPUB, "Possible Learner States",
                "Select this to add a criteria using a possible learner state.", new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        if (chosenExpressionType != ExpressionType.LEARNER_STATE) {
                            chosenExpressionType = ExpressionType.LEARNER_STATE;
                            updateTypeLabel();
                            updateStates(true);
                        }
                        showDeckWidget(mainDeck.getWidgetIndex(editPanel));
                        validateAll();
                    }
                });

        changeTypeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                updateStates(false);
                showDeckWidget(mainDeck.getWidgetIndex(choiceRibbon));
                validateAll();
            }
        });

        stateSelect.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                populatePreviousAndCurrentStates();
                updateLabelsBasedOnType();
                requestValidation(currentStateValidationWhenCourseConcept, stateSelectedValidation, propertyValidation);
            }
        });
        
        courseConceptSelect.addValueChangeHandler(new ValueChangeHandler<String>() {

            @Override
            public void onValueChange(ValueChangeEvent<String> arg0) {
                requestValidation(currentStateValidationWhenCourseConcept, propertyValidation);
            }
        });

        ValueChangeHandler<String> propertyHandler = new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                validateAll();
            }
        };
        previousPropertySelect.addValueChangeHandler(propertyHandler);
        currentPropertySelect.addValueChangeHandler(propertyHandler);
    }
    
    /**
     * Configure labels and other widgets depending on the type of state attribute.
     * E.g. if learner state attribute is exclusive to concepts (e.g. Knowledge) is selected then reconfigure
     * the UI to not show transition from one state value to another -</br>
     * "If the learner's state on 'Knowledge' of 'map-reading' is 'Expert'"</br>
     * versus "If the learner's performance on 'map-reading' changes from 'Anything' to 'Below Expectation'"
     */
    private void updateLabelsBasedOnType() {
        
        boolean isConceptLearnerState = false;
        if(ExpressionType.LEARNER_STATE.equals(chosenExpressionType) && stateSelect.getSelectedItem() != null) {
            LearnerStateAttributeNameEnum learnerState = LearnerStateAttributeNameEnum
                    .valueOf(stateSelect.getSelectedItem().getValue());
            
            if(learnerState.isExclusiveToConcepts()) {
                isConceptLearnerState = true;
            }
        }
        
        actionLabel.setText(isConceptLearnerState ? "of" : "changes from");
        toLabel.setText(isConceptLearnerState ? "is" : "to");
        previousPropertySelect.setVisible(!isConceptLearnerState);
        courseConceptSelect.setVisible(isConceptLearnerState);
    }

    @Override
    protected void populateEditor(StateExpressionWrapper obj) {
        selectedExpressionCopy = new StateExpressionWrapper();
        chosenExpressionType = null;
        previousPropertySelect.clear();
        currentPropertySelect.clear();

        if (obj.getStateExpression() instanceof LearnerStateTransitionEnum) {
            LearnerStateTransitionEnum transition = (LearnerStateTransitionEnum) obj.getStateExpression();
            selectedExpressionCopy.setStateExpression(copyLearnerStateTransitionEnum(transition));
            chosenExpressionType = ExpressionType.LEARNER_STATE;

            updateTypeLabel();
            updateStates(true);

            stateSelect.setValue(transition.getAttribute());
            courseConceptSelect.setValue(transition.getConcept());
            populatePreviousAndCurrentStates();
            updateLabelsBasedOnType();

            previousPropertySelect.setValue(transition.getPrevious() == null ? ANYTHING : transition.getPrevious());
            currentPropertySelect.setValue(transition.getCurrent() == null ? ANYTHING : transition.getCurrent());

            showDeckWidget(mainDeck.getWidgetIndex(editPanel));
        } else if (obj.getStateExpression() instanceof PerformanceNode) {
            PerformanceNode perfNode = (PerformanceNode) obj.getStateExpression();
            selectedExpressionCopy.setStateExpression(copyPerformanceNode(perfNode));

            if (ScenarioClientUtility.getTaskWithId(perfNode.getNodeId()) != null) {
                chosenExpressionType = ExpressionType.TASK;
            } else if (ScenarioClientUtility.getConceptWithId(perfNode.getNodeId()) != null) {
                chosenExpressionType = ExpressionType.CONCEPT;
            } else {
                throw new UnsupportedOperationException(
                        "Trying to load StateExpressionItemEditor with unknown node id '" + perfNode.getNodeId() + "'");
            }

            updateTypeLabel();
            updateStates(true);

            stateSelect.setValue(perfNode.getNodeId().toString());

            previousPropertySelect.setValue(perfNode.getPrevious() == null ? ANYTHING : perfNode.getPrevious());
            currentPropertySelect.setValue(perfNode.getCurrent() == null ? ANYTHING : perfNode.getCurrent());

            showDeckWidget(mainDeck.getWidgetIndex(editPanel));
        } else {
            updateStates(false);
            showDeckWidget(mainDeck.getWidgetIndex(choiceRibbon));
        }
    }

    @Override
    protected void applyEdits(StateExpressionWrapper obj) {
        /* Variables for capturing before and after values of the referenced
         * state expression. */
        Serializable oldExpression = obj.getStateExpression();
        Serializable newExpression = null;

        String selectedState = stateSelect.getSelectedItem().getValue();

        String previousPropValue = StringUtils.equals(previousPropertySelect.getSelectedItem().getValue(), ANYTHING)
                ? null
                : previousPropertySelect.getSelectedItem().getValue();
        String currentPropValue = StringUtils.equals(currentPropertySelect.getSelectedItem().getValue(), ANYTHING)
                ? null
                : currentPropertySelect.getSelectedItem().getValue();

        if (chosenExpressionType == ExpressionType.LEARNER_STATE) {
            LearnerStateTransitionEnum transition = new LearnerStateTransitionEnum();

            // populate the LearnerStateTransitionEnum with the selected values
            transition.setAttribute(selectedState);
            transition.setPrevious(previousPropValue);
            transition.setCurrent(currentPropValue);
            transition.setConcept(courseConceptSelect.getSelectedItem().getValue());

            newExpression = transition;
            obj.setStateExpression(transition);
        } else {
            PerformanceNode perfNode = new PerformanceNode();

            // populate the PerformanceNode with the selected values
            perfNode.setName(stateSelect.getSelectedItem().getText());
            perfNode.setNodeId(new BigInteger(selectedState));
            perfNode.setPrevious(previousPropValue);
            perfNode.setCurrent(currentPropValue);

            newExpression = perfNode;
            obj.setStateExpression(perfNode);
        }

        /* Fire an event that alerts subscribers the change in elements being
         * referenced by the state transition */
        if (oldExpression != null || newExpression != null) {
            ScenarioEventUtility.fireReferencesChangedEvent(getStateTransition(), oldExpression, newExpression);
        }
    }

    /**
     * Shows the provided deck widget and shows/hides the save button accordingly.
     *
     * @param deckWidget the deck widget index.
     */
    private void showDeckWidget(int deckWidget) {
        mainDeck.showWidget(deckWidget);
        setSaveButtonVisible(deckWidget != mainDeck.getWidgetIndex(choiceRibbon));
    }

    /**
     * Builds the ribbon with the provided options.
     *
     * @param expressionTypes the set of expression types to add to the ribbon
     */
    private void refreshRibbon(Set<ExpressionType> expressionTypes) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("refreshRibbon(" + expressionTypes + ")");
        }

        // if null we want to hide everything so use empty list
        if (expressionTypes == null) {
            expressionTypes = new HashSet<>();
        }

        taskRibbonButton.getElement().getStyle()
                .setDisplay(expressionTypes.contains(ExpressionType.TASK) ? Display.INLINE_BLOCK : Display.NONE);

        conceptRibbonButton.getElement().getStyle()
                .setDisplay(expressionTypes.contains(ExpressionType.CONCEPT) ? Display.INLINE_BLOCK : Display.NONE);

        learnerStateRibbonButton.getElement().getStyle().setDisplay(
                expressionTypes.contains(ExpressionType.LEARNER_STATE) ? Display.INLINE_BLOCK : Display.NONE);
    }

    /**
     * Populates the state select dropdown with the choices that are not currently selected.
     *
     * @param populateDropdowns true to populate the state dropdowns; false to only update the
     *        choice ribbon.
     */
    private void updateStates(boolean populateDropdowns) {

        List<Option> stateSelectOptions = new ArrayList<Option>();
        Set<ExpressionType> populatedTypes = new HashSet<ExpressionType>();

        boolean isTask = chosenExpressionType == ExpressionType.TASK;
        for (Task task : ScenarioClientUtility.getUnmodifiableTaskList()) {
            if (doesTaskOrConceptExistInListItems(task.getNodeId())) {
                continue;
            }

            // a task option exists, put into set
            populatedTypes.add(ExpressionType.TASK);

            Option option = new Option();
            option.setText(task.getName());
            option.setValue(task.getNodeId().toString());

            // this is the type we want options for, put into dropdown
            if (isTask) {
                stateSelectOptions.add(option);
            } else {
                // we know there is atleast 1 task, don't need to keep checking
                break;
            }
        }

        boolean isConcept = chosenExpressionType == ExpressionType.CONCEPT;
        for (Concept concept : ScenarioClientUtility.getUnmodifiableConceptList()) {
            if (doesTaskOrConceptExistInListItems(concept.getNodeId())) {
                continue;
            }

            // a concept option exists, put into set
            populatedTypes.add(ExpressionType.CONCEPT);

            Option option = new Option();
            option.setText(concept.getName());
            option.setValue(concept.getNodeId().toString());

            // this is the type we want options for, put into dropdown
            if (isConcept) {
                stateSelectOptions.add(option);
            } else {
                // we know there is atleast 1 concept, don't need to keep checking
                break;
            }
        }

        boolean isLearnerState = chosenExpressionType == ExpressionType.LEARNER_STATE;
        List<LearnerStateAttributeNameEnum> learnerStates = new ArrayList<LearnerStateAttributeNameEnum>();
        learnerStates.addAll(LearnerStateAttributeNameEnum.VALUES());
        if (isLearnerState) {
            Collections.sort(learnerStates, new Comparator<LearnerStateAttributeNameEnum>() {
                @Override
                public int compare(LearnerStateAttributeNameEnum o1, LearnerStateAttributeNameEnum o2) {
                    return o1.getDisplayName().compareTo(o2.getDisplayName());
                }
            });
        }
        for (LearnerStateAttributeNameEnum learnerState : learnerStates) {
            if (doesLearnerStateExistInListItems(learnerState)) {
                continue;
            }

            // a learner state option exists, put into set
            populatedTypes.add(ExpressionType.LEARNER_STATE);

            Option option = new Option();
            option.setText(learnerState.getDisplayName());
            option.setValue(learnerState.getName());

            // this is the type we want options for, put into dropdown
            if (isLearnerState) {
                stateSelectOptions.add(option);
            } else {
                // we know there is atleast 1 learner state, don't need to keep checking
                break;
            }
        }
        
        // populate course concepts selector (if not already populated)
        if(courseConceptSelect.getItemCount() == 0) {
            
            List<String> courseConcepts = GatClientUtility.getBaseCourseConcepts();
            if(courseConcepts != null) {
                for(String courseConcept : courseConcepts) {
                    
                    Option option = new Option();
                    option.setText(courseConcept);
                    option.setValue(courseConcept);
                    
                    courseConceptSelect.add(option);
                }
            }
            
        }

        refreshRibbon(populatedTypes);

        if (populateDropdowns) {
            stateSelect.clear();
            for (Option option : stateSelectOptions) {
                stateSelect.add(option);
            }
            stateSelect.refresh();
            populatePreviousAndCurrentStates();
            updateLabelsBasedOnType();
        }
    }

    /**
     * Checks if the provided {@link Task} or {@link Concept} node id is already in the list of
     * existing criteria.
     *
     * @param nodeId the node id to check.
     * @return true if the {@link Task} or {@link Concept} is already in use; false otherwise.
     */
    private boolean doesTaskOrConceptExistInListItems(BigInteger nodeId) {
        if (selectedExpressionCopy.getStateExpression() instanceof PerformanceNode) {
            PerformanceNode perfNode = (PerformanceNode) selectedExpressionCopy.getStateExpression();
            // ignore the currently edited performance node
            if (perfNode.getNodeId().equals(nodeId)) {
                return false;
            }
        }

        for (StateExpressionWrapper wrapper : getParentItemListEditor().getItems()) {
            if (!(wrapper.getStateExpression() instanceof PerformanceNode)) {
                continue;
            }

            PerformanceNode perfNode = (PerformanceNode) wrapper.getStateExpression();
            if (perfNode.getNodeId().equals(nodeId)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if the provided {@link LearnerStateAttributeNameEnum} is already in the list of
     * existing criteria.
     *
     * @param learnerState the {@link LearnerStateAttributeNameEnum} to check.
     * @return true if the {@link LearnerStateAttributeNameEnum} is already in use; false otherwise.
     */
    private boolean doesLearnerStateExistInListItems(LearnerStateAttributeNameEnum learnerState) {
        if (selectedExpressionCopy.getStateExpression() instanceof LearnerStateTransitionEnum) {
            LearnerStateTransitionEnum transition = (LearnerStateTransitionEnum) selectedExpressionCopy
                    .getStateExpression();
            // ignore the currently edited learner state
            if (StringUtils.equals(transition.getAttribute(), learnerState.getName())) {
                return false;
            }
        }

        for (StateExpressionWrapper wrapper : getParentItemListEditor().getItems()) {
            if (wrapper.equals(selectedExpressionCopy)
                    || !(wrapper.getStateExpression() instanceof LearnerStateTransitionEnum)) {
                continue;
            }

            LearnerStateTransitionEnum stateTransition = (LearnerStateTransitionEnum) wrapper.getStateExpression();
            if (StringUtils.equals(stateTransition.getAttribute(), learnerState.getName())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Uses the selected expression type and sub-type to populate the previous and current state
     * lists.
     * @throws UnsupportedOperationException if the expression type is unknown
     */
    private void populatePreviousAndCurrentStates() {
        previousPropertySelect.clear();
        currentPropertySelect.clear();

        ArrayList<AbstractEnum> previousValues = new ArrayList<AbstractEnum>();
        ArrayList<AbstractEnum> currentValues = new ArrayList<AbstractEnum>();

        switch (chosenExpressionType) {
        case LEARNER_STATE:
            LearnerStateAttributeNameEnum learnerState = LearnerStateAttributeNameEnum
                    .valueOf(stateSelect.getSelectedItem().getValue());

            // load previous and current list values
            for (AbstractEnum abstractEnum : learnerState.getAttributeValues()) {
                previousValues.add(abstractEnum);
                currentValues.add(abstractEnum);
            }

            break;
        case TASK:
            // fall-through
        case CONCEPT:
            previousValues.addAll(AssessmentLevelEnum.VALUES());
            currentValues.addAll(AssessmentLevelEnum.VALUES());

            break;
        default:
            throw new UnsupportedOperationException(
                    "Found an unknown type ['" + chosenExpressionType + "'] in populatePreviousAndCurrentStates().");
        }

        // Set previous values
        for (AbstractEnum previous : previousValues) {
            Option option = new Option();
            option.setText(previous.getDisplayName());
            option.setValue(previous.getName());
            previousPropertySelect.add(option);
        }
        Option anythingOptionPrevious = new Option();
        anythingOptionPrevious.setText(ANYTHING);
        anythingOptionPrevious.setValue(ANYTHING);
        previousPropertySelect.add(anythingOptionPrevious);

        // Set current values
        for (AbstractEnum current : currentValues) {
            Option option = new Option();
            option.setText(current.getDisplayName());
            option.setValue(current.getName());
            currentPropertySelect.add(option);
        }
        Option anythingOptionCurrent = new Option();
        anythingOptionCurrent.setText(ANYTHING);
        anythingOptionCurrent.setValue(ANYTHING);
        currentPropertySelect.add(anythingOptionCurrent);

        // set default values
        previousPropertySelect.setValue(ANYTHING);
        currentPropertySelect.setValue(ANYTHING);

        previousPropertySelect.refresh();
        currentPropertySelect.refresh();
    }

    /**
     * Builds the text label used for the given expression type.
     */
    private void updateTypeLabel() {
        typeLabel.setHTML(chosenExpressionType == ExpressionType.LEARNER_STATE ? "state" : "performance");
    }

    /**
     * Copies a {@link LearnerStateTransitionEnum}.
     *
     * @param toCopy the {@link LearnerStateTransitionEnum} to copy.
     * @return the copy of the {@link LearnerStateTransitionEnum}.
     */
    private LearnerStateTransitionEnum copyLearnerStateTransitionEnum(LearnerStateTransitionEnum toCopy) {
        LearnerStateTransitionEnum transition = new LearnerStateTransitionEnum();
        transition.setAttribute(toCopy.getAttribute());
        transition.setPrevious(toCopy.getPrevious());
        transition.setCurrent(toCopy.getCurrent());
        return transition;
    }

    /**
     * Copies a {@link PerformanceNode}.
     *
     * @param toCopy the {@link PerformanceNode} to copy.
     * @return the copy of the {@link PerformanceNode}.
     */
    private PerformanceNode copyPerformanceNode(PerformanceNode toCopy) {
        PerformanceNode perfNode = new PerformanceNode();
        perfNode.setName(toCopy.getName());
        perfNode.setNodeId(toCopy.getNodeId());
        perfNode.setPrevious(toCopy.getPrevious());
        perfNode.setCurrent(toCopy.getCurrent());
        return perfNode;
    }

    /**
     * Getter for the state transition for which state expressions are being
     * authored.
     *
     * @return The {@link StateTransition} for which state expressions are being
     *         authored. Can be null.
     */
    private StateTransition getStateTransition() {
        return stateTransition;
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

        this.stateTransition = stateTransition;
    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
        // no validation composite children
    }

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        validationStatuses.add(stateSelectedValidation);
        validationStatuses.add(propertyValidation);
        validationStatuses.add(currentStateValidationWhenCourseConcept);
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        if (mainDeck.getVisibleWidget() == mainDeck.getWidgetIndex(choiceRibbon)) {
            validationStatus.setValid();
            return;
        }

        if (stateSelectedValidation.equals(validationStatus)) {
            stateSelectedValidation.setValidity(stateSelect.getSelectedItem() != null);
        } else if (propertyValidation.equals(validationStatus)) {
            // check if both previous and current selectors are on the default of ANYTHING
            boolean currentIsAnything = StringUtils.equals(currentPropertySelect.getValue(), ANYTHING);
            boolean previousIsAnything = StringUtils.equals(previousPropertySelect.getValue(), ANYTHING);
            
            // ignore this validation check when a learner state attribute that is course concept based is used since
            // the previous select widget is not used
            boolean isCourseConceptLearnerState = courseConceptSelect.isVisible();
            propertyValidation.setValidity(isCourseConceptLearnerState || !previousIsAnything || !currentIsAnything);
        }else if(currentStateValidationWhenCourseConcept.equals(validationStatus)) {
            
            // the previous select widget is not considered (because its hidden when courseConceptSelect is visible)
            boolean currentIsAnything = StringUtils.equals(currentPropertySelect.getValue(), ANYTHING);
            boolean isCourseConceptLearnerState = courseConceptSelect.isVisible();
            currentStateValidationWhenCourseConcept.setValidity(!isCourseConceptLearnerState || (isCourseConceptLearnerState && !currentIsAnything));
        }
    }

    @Override
    protected boolean validate(StateExpressionWrapper wrapper) {
        Serializable stateExp = wrapper.getStateExpression();
        if (stateExp == null) {
            return false;
        }

        String errorMsg;
        if (stateExp instanceof LearnerStateTransitionEnum) {
            LearnerStateTransitionEnum learnerState = (LearnerStateTransitionEnum) stateExp;
            errorMsg = ScenarioValidatorUtility.validateLearnerStateTransitionEnum(learnerState);
        } else if (stateExp instanceof PerformanceNode) {
            PerformanceNode node = (PerformanceNode) stateExp;
            errorMsg = ScenarioValidatorUtility.validatePerformanceNode(node);
        } else {
            return false;
        }

        return StringUtils.isBlank(errorMsg);
    }

    @Override
    public void setReadonly(boolean isReadonly) {
        choiceRibbon.setReadonly(isReadonly);
        stateSelect.setEnabled(!isReadonly);
        courseConceptSelect.setEnabled(!isReadonly);
        previousPropertySelect.setEnabled(!isReadonly);
        currentPropertySelect.setEnabled(!isReadonly);
        setSaveButtonVisible(!isReadonly);
        changeTypeButton.setVisible(!isReadonly);
    }
}
