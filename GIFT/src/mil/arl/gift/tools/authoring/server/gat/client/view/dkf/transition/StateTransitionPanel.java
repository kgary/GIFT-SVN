/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.transition;

import static mil.arl.gift.common.gwt.client.SafeHtmlUtils.bold;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.constants.IconType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.TextOverflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.Actions.StateTransitions.StateTransition;
import generated.dkf.Actions.StateTransitions.StateTransition.LogicalExpression;
import generated.dkf.Actions.StateTransitions.StateTransition.StrategyChoices;
import generated.dkf.Concept;
import generated.dkf.LearnerStateTransitionEnum;
import generated.dkf.PerformanceNode;
import generated.dkf.Strategy;
import generated.dkf.StrategyRef;
import generated.dkf.Task;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;
import mil.arl.gift.common.gwt.client.SafeHtmlUtils;
import mil.arl.gift.common.gwt.client.util.StrategyActivityUtil;
import mil.arl.gift.common.gwt.client.util.StrategyActivityUtil.StrategyActivityIcon;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.gwt.client.validation.ValidationWidget;
import mil.arl.gift.common.gwt.client.validation.WidgetValidationStatus;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.EnforcedButton;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ItemAction;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.dialog.CreateConceptDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.CreateScenarioObjectEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.ScenarioEventUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.CreateListAction;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemField;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemListEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemListEditor.ListChangedCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ListChangedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ListChangedEvent.ListAction;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.validation.ScenarioValidationComposite;

/**
 * The panel for displaying, creating, and editing the criteria for executing strategies as well as
 * the list of strategies to execute when all of the criteria are met.
 *
 * @author sharrison
 */
public class StateTransitionPanel extends ScenarioValidationComposite {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(StateTransitionPanel.class.getName());

    /** The ui binder. */
    private static TransitionPanelUiBinder uiBinder = GWT.create(TransitionPanelUiBinder.class);

    /** The Interface TransitionPanelUiBinder */
    interface TransitionPanelUiBinder extends UiBinder<Widget, StateTransitionPanel> {
    }

    /** Interface to allow CSS style name access */
    interface Style extends CssResource {
        /**
         * The css for the displaying flex layout
         *
         * @return the css value
         */
        String flex();
    }

    /** The css Styles declared in the ui.xml */
    @UiField
    protected Style style;


    /** Display string for a null previous or current property label */
    private static final String DEFAULT_EXPRESSION_STATE = "Anything";

    /** Displays any validation errors on the page */
    @UiField(provided = true)
    protected ValidationWidget validations = new ValidationWidget(this);

    /** The item editor that is used by {@link #stateExpressionListEditor} */
    private final StateExpressionItemEditor stateExpressionItemEditor = new StateExpressionItemEditor();

    /** The list editor for the state expressions (execute when...) */
    @UiField(provided = true)
    protected ItemListEditor<StateExpressionWrapper> stateExpressionListEditor = new ItemListEditor<>(stateExpressionItemEditor);

    /** The item editor that is used by {@link #actionStrategyListEditor} */
    private final StrategyRefItemEditor strategyRefItemEditor = new StrategyRefItemEditor();

    /** The list editor for the strategies to execute when the criteria is met */
    @UiField(provided = true)
    protected ItemListEditor<StrategyRef> actionStrategyListEditor = new ItemListEditor<StrategyRef>(strategyRefItemEditor);

    /** The button used to create a task to be used by this transition */
    private EnforcedButton createTask = new EnforcedButton(IconType.EXTERNAL_LINK, "Create task",
            "Navigate to the page to create a new task", new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {

                    Task task = ScenarioClientUtility.generateNewTask();

                    PerformanceNode nodeToAdd = new PerformanceNode();
                    nodeToAdd.setName(task.getName());
                    nodeToAdd.setNodeId(task.getNodeId());

                    // add the item if it isn't in the list
                    if (!selectedTransition.getLogicalExpression().getStateType().contains(nodeToAdd)) {
                        selectedTransition.getLogicalExpression().getStateType().add(nodeToAdd);
                    }

                    ScenarioEventUtility.fireCreateScenarioObjectEvent(task);

                    stateExpressionListEditor.add(new StateExpressionWrapper(nodeToAdd));
                }
            });

    /** The button used to create a concept to be used by this transition */
    private EnforcedButton createConcept = new EnforcedButton(IconType.EXTERNAL_LINK, "Create concept",
            "Navigates to the page to create a new concept", new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    final CreateConceptDialog createConceptDialog = new CreateConceptDialog();
                    createConceptDialog.addValueChangeHandler(new ValueChangeHandler<CreateScenarioObjectEvent>() {
                        @Override
                        public void onValueChange(ValueChangeEvent<CreateScenarioObjectEvent> event) {
                            CreateScenarioObjectEvent createConceptEvent = event.getValue();

                            Concept concept = (Concept) createConceptEvent.getScenarioObject();
                            PerformanceNode nodeToAdd = new PerformanceNode();
                            nodeToAdd.setName(concept.getName());
                            nodeToAdd.setNodeId(concept.getNodeId());

                            // add the item if it isn't in the list
                            if (!selectedTransition.getLogicalExpression().getStateType().contains(nodeToAdd)) {
                                selectedTransition.getLogicalExpression().getStateType().add(nodeToAdd);
                            }

                            SharedResources.getInstance().getEventBus().fireEvent(createConceptEvent);

                            stateExpressionListEditor.add(new StateExpressionWrapper(nodeToAdd));

                            createConceptDialog.hide();
                            createConceptDialog.removeFromParent();
                        }
                    });

                    createConceptDialog.center();
                }
            });

    /** The button used to create a strategy referenced by this transition */
    private EnforcedButton createStrategy = new EnforcedButton(IconType.EXTERNAL_LINK, "Create Strategy",
            "Navigates to the page to create a new strategy", new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    Strategy strategy = ScenarioClientUtility.generateNewStrategy();

                    StrategyRef strategyRef = new StrategyRef();
                    strategyRef.setName(strategy.getName());
                    addStrategyToExecute(strategyRef);

                    requestValidationAndFireDirtyEvent(selectedTransition);
                    ScenarioEventUtility.fireCreateScenarioObjectEvent(strategy);
                }
            });

    /** The currently selected state transition. */
    private StateTransition selectedTransition;

    /**
     * Comparator used to sort performance nodes and learner states. Sorted so that PerformanceNodes
     * are on top.
     */
    private final Comparator<Serializable> variableListComparator = new Comparator<Serializable>() {

        @Override
        public int compare(Serializable o1, Serializable o2) {

            // sort Performance Nodes above LearnerStateTransitionEnums
            if (o1 instanceof PerformanceNode && o2 instanceof LearnerStateTransitionEnum) {
                return -1;

                // sort LearnerStateTransitionEnums below Performance Nodes
            } else if (o1 instanceof LearnerStateTransitionEnum && o2 instanceof PerformanceNode) {
                return 1;

                // sort performance nodes by name
            } else if (o1 instanceof PerformanceNode && o2 instanceof PerformanceNode) {
                PerformanceNode node1 = (PerformanceNode) o1;
                PerformanceNode node2 = (PerformanceNode) o2;
                return node1.getName().compareTo(node2.getName());

                // sort learner state transitions by attribute
            } else if (o1 instanceof LearnerStateTransitionEnum && o2 instanceof LearnerStateTransitionEnum) {
                LearnerStateTransitionEnum transition1 = (LearnerStateTransitionEnum) o1;
                LearnerStateTransitionEnum transition2 = (LearnerStateTransitionEnum) o2;
                return transition1.getAttribute().compareTo(transition2.getAttribute());
            }

            return 0;
        }
    };

    /**
     * The container for showing validation messages for the state transition not having any
     * criteria set.
     */
    private final WidgetValidationStatus criteriaValidationStatus;

    /**
     * The container for showing validation messages for the state transition not having any actions
     * set.
     */
    private final WidgetValidationStatus actionValidationStatus;

    /**
     * Instantiates a new dkf transition panel.
     */
    public StateTransitionPanel() {
        initWidget(uiBinder.createAndBindUi(this));

        stateExpressionListEditor.setFields(Arrays.asList(buildStateExpressionItemField()));
        stateExpressionListEditor.setActions(buildStateExpressionActions());
        Widget addStateExpressionButton = stateExpressionListEditor.addCreateListAction(
                "Click here to add a new criteria", new CreateListAction<StateExpressionWrapper>() {
            @Override
                    public StateExpressionWrapper createDefaultItem() {
                        return new StateExpressionWrapper();
                    }
                });

        stateExpressionListEditor.setTopButtons(Arrays.asList(createTask, createConcept));
        stateExpressionListEditor.addListChangedCallback(new ListChangedCallback<StateExpressionWrapper>() {
            @Override
            public void listChanged(ListChangedEvent<StateExpressionWrapper> event) {

                selectedTransition.getLogicalExpression().getStateType().clear();
                for (StateExpressionWrapper wrapper : stateExpressionListEditor.getItems()) {
                    selectedTransition.getLogicalExpression().getStateType().add(wrapper.getStateExpression());
                }

                if (event.getActionPerformed() == ListAction.REMOVE) {
                    final Serializable oldValue = event.getAffectedItems().get(0).getStateExpression();
                    if (oldValue != null) {
                        ScenarioEventUtility.fireReferencesChangedEvent(selectedTransition, oldValue, null);
                    }
                }

                requestValidationAndFireDirtyEvent(selectedTransition, criteriaValidationStatus);
            }
        });

        criteriaValidationStatus = new WidgetValidationStatus(addStateExpressionButton,
                "The state transition requires that at least one criteria is created. Please create a criteria.");


        actionStrategyListEditor.setFields(buildStrategyFields());
        actionStrategyListEditor.setActions(buildStrategyActions());
        actionStrategyListEditor.setDraggable(true);

        Widget addStrategyRefButton = actionStrategyListEditor.addCreateListAction("Click here to add a new strategy reference", new CreateListAction<StrategyRef>() {

            @Override
            public StrategyRef createDefaultItem() {
                return new StrategyRef();
            }
        });

        actionStrategyListEditor.setTopButtons(Arrays.asList(createStrategy));

        actionStrategyListEditor.addListChangedCallback(new ListChangedCallback<StrategyRef>() {

            @Override
            public void listChanged(ListChangedEvent<StrategyRef> event) {
                /* need to redraw table because the last element in the table has a '+' in the
                 * label. That last element might have just changed been moved, removed, or
                 * added. */
                actionStrategyListEditor.redrawListEditor(true);

                if(event.getActionPerformed() == ListAction.ADD) {

                    final Serializable newValue = event.getAffectedItems().get(0);
                    if (newValue != null) {
                        ScenarioEventUtility.fireReferencesChangedEvent(selectedTransition, null, newValue);
                    }

                } else if (event.getActionPerformed() == ListAction.REMOVE) {

                    final Serializable oldValue = event.getAffectedItems().get(0);
                    if (oldValue != null) {
                        ScenarioEventUtility.fireReferencesChangedEvent(selectedTransition, oldValue, null);
                    }
                }

                requestValidationAndFireDirtyEvent(selectedTransition, actionValidationStatus);
            }
        });

        actionValidationStatus = new WidgetValidationStatus(addStrategyRefButton,
                "The state transition requires that at least one strategy is applied. Please add a strategy to apply.");

        updateReadOnly();

        // needs to be called last
        initValidationComposite(validations);
    }

    /**
     * Builds the item field for the state expression table.
     *
     * @return the {@link ItemField} for the state expression table.
     */
    private ItemField<StateExpressionWrapper> buildStateExpressionItemField() {
        return new ItemField<StateExpressionWrapper>(null, "100%") {
            @Override
            public Widget getViewWidget(StateExpressionWrapper item) {

                SafeHtmlBuilder sb = new SafeHtmlBuilder();
                if (item.getStateExpression() instanceof LearnerStateTransitionEnum) {
                    LearnerStateTransitionEnum keyState = (LearnerStateTransitionEnum) item.getStateExpression();

                    boolean isLSExclusiveToConcepts = false;
                    if (keyState.getAttribute() != null) {
                        try {
                            LearnerStateAttributeNameEnum lsAttrName = LearnerStateAttributeNameEnum.valueOf(keyState.getAttribute());
                            isLSExclusiveToConcepts = lsAttrName.isExclusiveToConcepts();
                        }catch(@SuppressWarnings("unused") Exception e) {
                            logger.warning("Found unhandled learner state attribute name reference of '"+keyState.getAttribute()+"'.");
                        }
                        sb.append(bold(keyState.getAttribute()));                       
                    }

                    if(isLSExclusiveToConcepts) {
                        sb.appendHtmlConstant(" of ")
                            .append(bold(keyState.getConcept() == null ? "course concept not set" : keyState.getConcept()))
                            .appendHtmlConstant(" is ")
                            .append(bold(keyState.getCurrent() == null ? DEFAULT_EXPRESSION_STATE : keyState.getCurrent()));

                    }else {
                        sb.appendHtmlConstant(" changes from ")
                                .append(bold(keyState.getPrevious() == null ? DEFAULT_EXPRESSION_STATE : keyState.getPrevious()))
                                .appendHtmlConstant(" to ")
                                .append(bold(keyState.getCurrent() == null ? DEFAULT_EXPRESSION_STATE : keyState.getCurrent()));
                    }

                } else if (item.getStateExpression() instanceof PerformanceNode) {
                    PerformanceNode keyNode = (PerformanceNode) item.getStateExpression();

                    if (keyNode.getName() != null) {
                        sb.append(bold(keyNode.getName()));
                        }

                    String previousLabel = keyNode.getPrevious();
                    if (previousLabel == null) {
                        previousLabel = DEFAULT_EXPRESSION_STATE;
                    } else {
                        // get display name
                        try {
                            AssessmentLevelEnum assessmentLevel = AssessmentLevelEnum.valueOf(previousLabel);
                            previousLabel = assessmentLevel.getDisplayName();
                        } catch (@SuppressWarnings("unused") Exception e) {
                            logger.warning(
                                    "Unable to find the appropriate previous performance assessment level enumeration for '"
                                            + previousLabel + "'.");
                        }
                    }

                    String currentLabel = keyNode.getCurrent();
                    if (currentLabel == null) {
                        currentLabel = DEFAULT_EXPRESSION_STATE;
                    } else {
                        // get display name
                        try {
                            AssessmentLevelEnum assessmentLevel = AssessmentLevelEnum.valueOf(currentLabel);
                            currentLabel = assessmentLevel.getDisplayName();
                        } catch (@SuppressWarnings("unused") Exception e) {
                            logger.warning(
                                    "Unable to find the appropriate current performance assessment level enumeration for '"
                                            + currentLabel + "'.");
                        }
                    }

                    sb.appendHtmlConstant("'s performance assessment level changes from ").append(bold(previousLabel))
                            .appendHtmlConstant(" to ").append(bold(currentLabel));
                }

                return new HTML(sb.toSafeHtml());
            }
        };
    }

    /**
     * Builds the column actions for the state expressions.
     *
     * @return the list of state expression item actions.
     */
    private List<ItemAction<StateExpressionWrapper>> buildStateExpressionActions() {
        // jump action icon
        ItemAction<StateExpressionWrapper> jumpToConcept = new ItemAction<StateExpressionWrapper>() {

            @Override
            public boolean isEnabled(StateExpressionWrapper item) {
                return item.getStateExpression() instanceof PerformanceNode;
            }

            @Override
            public void execute(StateExpressionWrapper item) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("jumpToConcept.execute(" + item + ")");
                }

                // the only type that should have the 'jump to' button enabled is PerformanceNode
                PerformanceNode performanceNode = (PerformanceNode) item.getStateExpression();
                Serializable taskOrConcept = ScenarioClientUtility.getTaskOrConceptWithId(performanceNode.getNodeId());
                if (taskOrConcept == null) {
                    logger.warning("Failed to find the task/concept with name: '" + performanceNode.getName() + "'.");
                } else {
                    ScenarioEventUtility.fireJumpToEvent(taskOrConcept);
                }
            }

            @Override
            public String getTooltip(StateExpressionWrapper item) {
                return "Navigate to this concept";
            }

            @Override
            public IconType getIconType(StateExpressionWrapper item) {
                return IconType.EXTERNAL_LINK;
            }
        };

        return Arrays.asList(jumpToConcept);
    }

    /**
     * Builds the column fields for the strategies.
     *
     * @return the list of strategy item fieldss.
     */
    private List<ItemField<StrategyRef>> buildStrategyFields(){

        List<ItemField<StrategyRef>> fields = new ArrayList<>();

        //add a field to display the names of strategies
        fields.add(new ItemField<StrategyRef>() {

            @Override
            public Widget getViewWidget(StrategyRef item) {

                SafeHtmlBuilder htmlBuilder = new SafeHtmlBuilder();
                if (item != null) {
                    if (selectedTransition != null && selectedTransition.getStrategyChoices() != null) {
                        List<StrategyRef> strategies = selectedTransition.getStrategyChoices().getStrategies();
                        int itemIndex = strategies.indexOf(item);
                        htmlBuilder.append(++itemIndex);

                        /* add '+' if it's the last item in the list to show that it will continue
                         * to be executed each time the state transition is evaluated to true */
                        if (itemIndex == strategies.size()) {
                            htmlBuilder.appendEscaped("+");
                        }

                        htmlBuilder.appendEscaped(". ");
                    }

                    String refName = item.getName();
                    if (StringUtils.isNotBlank(refName)) {
                        htmlBuilder.append(SafeHtmlUtils.bold(refName));
                    } else {
                        htmlBuilder.append(SafeHtmlUtils.color("malformed strategy reference", "red"));
                    }
                } else {
                    htmlBuilder.append(SafeHtmlUtils.color("unrecognized type", "red"));
                }

                HTML html = new HTML(htmlBuilder.toSafeHtml());
                html.setWidth("auto");
                html.getElement().getStyle().setOverflow(Overflow.HIDDEN);
                html.getElement().getStyle().setProperty("whiteSpace", "nowrap");
                html.getElement().getStyle().setProperty("maxWidth", "400px");
                html.getElement().getStyle().setTextOverflow(TextOverflow.ELLIPSIS);

                return html;
            }
        });

        //add a field to display icons indicating the activities a strategy will invoke
        fields.add(new ItemField<StrategyRef>(null, "100%") {

            @Override
            public Widget getViewWidget(StrategyRef item) {

                FlowPanel panel = new FlowPanel();
                panel.setStyleName(style.flex());

                for(FlowPanel icon : getActivityIcons(item.getName())) {
                    panel.add(icon);
                }
                
                if(panel.getWidgetCount() < 1) {
                    panel.getElement().getStyle().setPadding(10, Unit.PX);
                }

                return panel;
            }
        });

        return fields;
    }

    /**
     * Builds the column actions for the strategies.
     *
     * @return the list of strategy item actions.
     */
    private List<ItemAction<StrategyRef>> buildStrategyActions() {

        ItemAction<StrategyRef> jumpTo = new ItemAction<StrategyRef>() {

            @Override
            public boolean isEnabled(StrategyRef item) {
                return true;
            }

            @Override
            public void execute(StrategyRef item) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("jumpToStrategy.execute(" + item + ")");
                }

                // the only type that should have the 'jump to' button enabled is StrategyRef
                String strategyName = item.getName();
                Strategy strategy = ScenarioClientUtility.getStrategyWithName(strategyName);

                if (strategy == null) {
                    logger.warning("Failed to find the strategy with name: '" + strategyName + "'.");
                } else {
                    ScenarioEventUtility.fireJumpToEvent(strategy);
                }
            }

            @Override
            public String getTooltip(StrategyRef item) {
                return "Click to navigate to this strategy";
            }

            @Override
            public IconType getIconType(StrategyRef item) {
                return IconType.EXTERNAL_LINK;
            }
        };

        return Arrays.asList(jumpTo);
    }

    /**
     * Updates the {@link StateTransition} that this panel should be editing.
     *
     * @param transition The {@link StateTransition} to edit. The value can't be null
     */
    public void edit(StateTransition transition) {

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("edit(" + transition + ")");
        }

        if (transition == null) {
            throw new IllegalArgumentException("Cannot edit a null transition.");
        }

        stateExpressionItemEditor.setStateTransition(transition);
        strategyRefItemEditor.setStateTransition(transition);

        // prevent null pointers for the transition's logical expression
        if (transition.getLogicalExpression() == null) {
            transition.setLogicalExpression(new LogicalExpression());
        }

        // prevent null pointers for the transition's strategy choices
        if (transition.getStrategyChoices() == null) {
            transition.setStrategyChoices(new StrategyChoices());
        }

        selectedTransition = transition;

        // Load state expression table data
        List<Serializable> stateTypeList = new ArrayList<Serializable>();
        stateTypeList.addAll(transition.getLogicalExpression().getStateType());

        // group performance nodes and learner states in the table
        Collections.sort(stateTypeList, variableListComparator);

        List<StateExpressionWrapper> wrappers = new ArrayList<StateExpressionWrapper>();

        /* wrap the state expression schema objects in a helper class so that we can switch between
         * LearnerStateTransitionEnum and PerformanceNode */
        for (Serializable type : stateTypeList) {
            wrappers.add(new StateExpressionWrapper(type));
        }

        stateExpressionListEditor.setItems(wrappers);

        // Load action strategy table data
        actionStrategyListEditor.setItems(transition.getStrategyChoices().getStrategies());

        updateReadOnly();
    }

    /**
     * Updates the dropdown of {@link Task tasks} to reflect the name change.
     *
     * @param task the {@link Task} that was renamed. If null or if its {@link Task#getNodeId() node
     *        id} is null, nothing will change.
     * @param oldName the old name of the {@link Task} before it was renamed. If blank, nothing will
     *        change.
     * @param newName the new name. If blank, nothing will change.
     */
    public void handleTaskRename(Task task, String oldName, String newName) {
        if (task == null || task.getNodeId() == null || StringUtils.isBlank(oldName) || StringUtils.isBlank(newName)) {
            return;
        }

        // if node id is found, update in table
        for (StateExpressionWrapper wrapper : stateExpressionListEditor.getItems()) {
            if (wrapper.getStateExpression() instanceof PerformanceNode) {
                PerformanceNode itemNode = (PerformanceNode) wrapper.getStateExpression();
                if (itemNode.getNodeId().equals(task.getNodeId())) {
                    itemNode.setName(newName);
                    stateExpressionListEditor.refresh(wrapper);
                }
            }
        }
    }

    /**
     * Updates the dropdown of {@link Concept concepts} to reflect the name change.
     *
     * @param concept the {@link Concept} that was renamed. If null or if its
     *        {@link Concept#getNodeId() node id} is null, nothing will change.
     * @param oldName the old name of the {@link Concept} before it was renamed. If blank, nothing
     *        will change.
     * @param newName the new name. If blank, nothing will change.
     */
    public void handleConceptRename(Concept concept, String oldName, String newName) {
        if (concept == null || concept.getNodeId() == null || StringUtils.isBlank(oldName)
                || StringUtils.isBlank(newName)) {
            return;
        }

        // if node id is found, update in table
        for (StateExpressionWrapper wrapper : stateExpressionListEditor.getItems()) {
            if (wrapper.getStateExpression() instanceof PerformanceNode) {
                PerformanceNode itemNode = (PerformanceNode) wrapper.getStateExpression();
                if (itemNode.getNodeId().equals(concept.getNodeId())) {
                    itemNode.setName(newName);
                    stateExpressionListEditor.refresh(wrapper);
                }
            }
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

        // if old name is found, update in table
        for (Serializable item : actionStrategyListEditor.getItems()) {
            if (item instanceof StrategyRef) {
                StrategyRef itemRef = (StrategyRef) item;
                if (StringUtils.equals(itemRef.getName(), oldName)) {

                    itemRef.setName(newName);

                    actionStrategyListEditor.refresh(itemRef);
                    strategyRefItemEditor.updateStrategyList(oldName, newName);

                    validateAllAndFireDirtyEvent(selectedTransition);
                    return;
                }
            }
        }
    }

    /**
     * Removes the provided {@link Task} from the list of selected criteria.
     *
     * @param task The {@link Task} to remove. Nothing will change if null or if the
     *        {@link Task#getNodeId() task's node id} is null.
     */
    public void removeReferencedTask(Task task) {
        if (task == null || task.getNodeId() == null) {
            return;
        }

        removeReferencedPerformanceNode(task.getNodeId());
    }

    /**
     * Removes the provided {@link Concept} from the list of selected criteria.
     *
     * @param concept The {@link Concept} to remove. Nothing will change if null or if the
     *        {@link Concept#getNodeId() concept's node id} is null.
     */
    public void removeReferencedConcept(Concept concept) {
        if (concept == null || concept.getNodeId() == null) {
            return;
        }

        removeReferencedPerformanceNode(concept.getNodeId());
    }

    /**
     * Removes the {@link Task} or {@link Concept} with the provided node id from the list of
     * selected criteria. If the node id is not selected, then it will be removed from the available
     * choices within the {@link #stateExpressionDialog}. If present, it will also be removed from
     * the backing data object. The UI will be updated to reflect these changes.
     *
     * @param referencedNodeId the node id that references the {@link Task} or {@link Concept}.
     */
    private void removeReferencedPerformanceNode(BigInteger referencedNodeId) {
        /* Remove the task reference from the transition's list of criteria */
        for (StateExpressionWrapper wrapper : stateExpressionListEditor.getItems()) {
            if (wrapper.getStateExpression() instanceof PerformanceNode) {
                PerformanceNode perfNode = (PerformanceNode) wrapper.getStateExpression();
                if (perfNode.getNodeId().equals(referencedNodeId)) {
                    stateExpressionListEditor.remove(wrapper);
                    requestValidationAndFireDirtyEvent(selectedTransition, criteriaValidationStatus);
                    return;
                }
            }
        }
    }

    /**
     * Removes the provided {@link Strategy} from the list of available strategies within the
     * {@link #actionStrategyDialog}, removes it from {@link #selectedTransition}'s instructional
     * strategies, and removes it from the UI displaying {@link #selectedTransition}'s instructional
     * strategies.
     *
     * @param strategy The {@link Strategy} to remove from the UI.
     */
    public void removeReferencedStrategy(Strategy strategy) {
        /* Remove the strategy reference from the transition's list of strategies */
        for (Serializable strategyOrDelay : actionStrategyListEditor.getItems()) {
            if (strategyOrDelay instanceof StrategyRef) {
                StrategyRef strategyRef = (StrategyRef) strategyOrDelay;
                if (StringUtils.equals(strategyRef.getName(), strategy.getName())) {
                    actionStrategyListEditor.remove(strategyRef);
                    return;
                }
            }
        }

        actionStrategyListEditor.redrawListEditor(true);
        strategyRefItemEditor.updateStrategyList();

        validateAllAndFireDirtyEvent(selectedTransition);
    }

    /**
     * Adds the provided item to the correct list of available choices.
     *
     * @param item The item to add to the available choices list in the UI.
     */
    public void handleNewItem(Serializable item) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("handleNewItem(" + item + ")");
        }

        if(item instanceof Strategy) {

            actionStrategyListEditor.redrawListEditor(false);
            strategyRefItemEditor.updateStrategyList();

            validateAllAndFireDirtyEvent(selectedTransition);
        }
    }

    /**
     * Adds a strategy to the strategies-to-execute list.
     *
     * @param strategy the strategy to execute
     * @param index the location to insert the strategy. If null or out-of-bounds, the strategy will
     *        be appended.
     */
    private void addStrategyToExecute(StrategyRef strategy) {

        if (selectedTransition.getStrategyChoices() == null) {
            selectedTransition.setStrategyChoices(new StrategyChoices());
        }

        StrategyChoices strategies = selectedTransition.getStrategyChoices();

        // add the item if it isn't in the list
        if (!strategies.getStrategies().contains(strategy)) {
            strategies.getStrategies().add(strategy);
        }

        actionStrategyListEditor.add(strategy);

    }

    /**
     * Moves the strategy in the data table based on the distance (number of rows).
     *
     * @param strategy the strategy to move.
     * @param distance the number of rows to move. Negative distance is moving up; Positive distance
     *        is moving down.
     */
    @SuppressWarnings("unused")
    private void move(StrategyRef strategy, int distance) {

        List<StrategyRef> refs = selectedTransition.getStrategyChoices().getStrategies();

        int currentIndex = refs.indexOf(strategy);

        if (currentIndex == -1) {
            throw new IllegalArgumentException("Failed moving strategy due to the strategy not existing in the list.");
        }

        int newIndex = currentIndex + distance;
        if (newIndex < 0 || newIndex >= refs.size()) {
            throw new IllegalArgumentException(
                    "Failed moving strategy due to invalid list location [" + newIndex + "].");
        }

        StrategyRef refAtIndex = refs.get(newIndex);
        refs.remove(strategy);

        if(newIndex > currentIndex) {
            refs.add(refs.indexOf(refAtIndex) + 1, strategy);

        } else {
            refs.add(refs.indexOf(refAtIndex), strategy);
        }

        actionStrategyListEditor.redrawListEditor(true);
    }

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        validationStatuses.add(criteriaValidationStatus);
        validationStatuses.add(actionValidationStatus);
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        int listSize;
        if (criteriaValidationStatus.equals(validationStatus)) {
            listSize = stateExpressionListEditor.size();
            criteriaValidationStatus.setValidity(listSize > 0);
        } else if (actionValidationStatus.equals(validationStatus)) {
            listSize = actionStrategyListEditor.size();
            actionValidationStatus.setValidity(listSize > 0);
        }
    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
        childValidationComposites.add(stateExpressionListEditor);
        childValidationComposites.add(actionStrategyListEditor);
    }

    /**
     * A helper class that wraps {@link LearnerStateTransitionEnum} and {@link PerformanceNode}
     * schema objects for the state transition's expression editor. This class is used to allow the
     * state expression table to more easily handle both possible types for the schema object's
     * expression list.
     *
     * @author sharrison
     */
    public static class StateExpressionWrapper {

        /** The state expression schema object beeing wrapped */
        private Serializable stateExpression;

        /**
         * Creates a wrapper that does not yet wrap a state expression schema object
         */
        public StateExpressionWrapper() {
        }

        /**
         * Creates a wrapper wrapping the given state expression schema object
         *
         * @param stateExpression the schema object to wrap
         */
        public StateExpressionWrapper(Serializable stateExpression) {
            this.stateExpression = stateExpression;
        }

        /**
         * Sets the state expression schema object this wrapper should wrap
         *
         * @param stateExpression the schema object to wrap
         */
        public void setStateExpression(Serializable stateExpression) {
            this.stateExpression = stateExpression;
        }

        /**
         * Gets the state expression schema object being wrapped
         *
         * @return the schema object being wrapped
         */
        public Serializable getStateExpression() {
            return stateExpression;
        }
    }

    /**
     * Updates the read-only state of the widget based on the state contained within the widget and
     * the readonly flag set in {@link ScenarioClientUtility}
     */
    private void updateReadOnly() {
        boolean isReadOnly = ScenarioClientUtility.isReadOnly() || selectedTransition == null;
        setReadOnly(isReadOnly);
    }

    /**
     * Sets the usability of all the widgets that are used to edit the underlying
     * {@link StateTransition}
     *
     * @param isReadOnly If true, disable all the widgets used to edit. If false, enable all the
     *        widgets used to edit.
     */
    private void setReadOnly(boolean isReadOnly) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("setReadOnly(" + isReadOnly + ")");
        }

        // Disable the action buttons
        createTask.setEnabled(!isReadOnly);
        createConcept.setEnabled(!isReadOnly);
        createStrategy.setEnabled(!isReadOnly);

        // Disable the two item list editors
        this.actionStrategyListEditor.setReadonly(isReadOnly);
        this.stateExpressionListEditor.setReadonly(isReadOnly);
    }

    /**
     * Gets a list of icons representing what activities the strategy with the given name will invoke
     *
     * @param refName the name of the strategy being referenced
     * @return the activities that the referenced strategy will invoke
     */
    static List<FlowPanel> getActivityIcons(String refName) {

        Strategy strategy = ScenarioClientUtility.getStrategyWithName(refName);

        List<StrategyActivityIcon> icons = new ArrayList<>();
        List<FlowPanel> iconContainer = new ArrayList<>();

        if(strategy != null) {

            iconContainer = StrategyActivityUtil.countActivitiesAndCreateIcons(icons, strategy);
            for (StrategyActivityIcon icon : icons) {
                icon.applyStateTransitionPanelStyle();
            }
        }

        return iconContainer;
    }
}