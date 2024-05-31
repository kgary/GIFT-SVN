/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.shared;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Collapse;
import org.gwtbootstrap3.client.ui.PanelHeader;
import org.gwtbootstrap3.client.ui.constants.IconType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.Actions.StateTransitions.StateTransition;
import generated.dkf.Actions.StateTransitions.StateTransition.LogicalExpression;
import generated.dkf.Concept;
import generated.dkf.PerformanceNode;
import generated.dkf.Strategy;
import generated.dkf.StrategyRef;
import generated.dkf.Task;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.EnforcedButton;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.GenericListEditor;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.HelpLink;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ItemAction;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.common.util.StringUtils.Stringifier;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.ScenarioEventUtility;

/**
 * The widget that displays a list of state transitions that reference a specific instructional strategy within the DKF.
 * 
 * @author tflowers
 *
 */
public class StateTransitionReferenceWidget extends Composite {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(StateTransitionReferenceWidget.class.getName());

    /** The UiBinder that combines the ui.xml with this java class */
    private static StateTransitionReferenceWidgetUiBinder uiBinder = GWT
            .create(StateTransitionReferenceWidgetUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface StateTransitionReferenceWidgetUiBinder
            extends UiBinder<Widget, StateTransitionReferenceWidget> {
    }

    /**
     * Handles when the user clicks the 'Create Listener' button by providing
     * the UI for creating a new transition listener
     */
    private final ClickHandler createListenerHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent e) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("createListenerHandler.onClick(" + e + ")");
            }

            StateTransition transition = ScenarioClientUtility.generateNewStateTransition();

            if (parentScenarioObject instanceof Task || parentScenarioObject instanceof Concept) {
                LogicalExpression expression = new LogicalExpression();

                String name;
                BigInteger id;
                if (parentScenarioObject instanceof Task) {
                    Task task = (Task) parentScenarioObject;
                    name = task.getName();
                    id = task.getNodeId();
                } else {
                    Concept concept = (Concept) parentScenarioObject;
                    name = concept.getName();
                    id = concept.getNodeId();
                }

                PerformanceNode perfNode = new PerformanceNode();
                perfNode.setName(name);
                perfNode.setNodeId(id);

                expression.getStateType().add(perfNode);
                transition.setLogicalExpression(expression);
            } else if (parentScenarioObject instanceof Strategy) {
                Strategy strategy = (Strategy) parentScenarioObject;
                StrategyRef stratRef = new StrategyRef();
                stratRef.setName(strategy.getName());
                transition.getStrategyChoices().getStrategies().add(stratRef);
            } else {
                if (parentScenarioObject == null) {
                    throw new UnsupportedOperationException("The 'parentScenarioObject' can not be null.");
                } else {
                    throw new UnsupportedOperationException(
                            "The type " + parentScenarioObject.getClass().getName()
                                    + " for 'parentScenarioObject' has not yet been implemented.");
                }
            }

            ScenarioEventUtility.fireCreateScenarioObjectEvent(transition);
        }
    };

    /** The collapse that contains {@link #listEditor} */
    @UiField
    protected Collapse collapse;

    /** The header/title for the control */
    @UiField
    protected PanelHeader panelHeader;

    /** The link to display the help text */
    @UiField
    protected HelpLink helpLink;
    
    /** The text to be displayed in the help dialog */
    @UiField
    protected HTML helpText;
    
    /**
     * The control that displays the each of the elements contained within this
     * control
     */
    @UiField(provided = true)
    protected GenericListEditor<StateTransition> listEditor = new GenericListEditor<StateTransition>(new Stringifier<StateTransition>() {
        @Override
        public String stringify(StateTransition stateTransition) {
            return stateTransition.getName();
        }
    });

    /**
     * The button provided to the user allowing for the creation of a new
     * transition listener.
     */
    protected EnforcedButton createListenerButton = new EnforcedButton(IconType.LINK, "Create State Transition",
            "Creates a new state transition", createListenerHandler);
    
    /** Action to jump to the selected state transition page. This will be visible for each item in the {@link GenericListEditor} table. */
    protected ItemAction<StateTransition> jumpToAction = new ItemAction<StateTransition>() {
        
        @Override
        public boolean isEnabled(StateTransition item) {
            return true;
        }
        
        @Override
        public String getTooltip(StateTransition item) {
            return "Click to navigate to this state transition";
        }
        
        @Override
        public IconType getIconType(StateTransition item) {
            return IconType.EXTERNAL_LINK;
        }
        
        @Override
        public void execute(StateTransition item) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("jumpToAction.execute(" + item + ")");
            }
            
            ScenarioEventUtility.fireJumpToEvent(item);
        }
    };

    /**
     * The scenario object for which the referencing
     * {@link generated.course.Actions.StateTransitions.StateTransition state
     * transitions} are being shown
     */
    private Serializable parentScenarioObject = null;

    /**
     * Default constructor for the {@link StateTransitionReferenceWidget}
     */
    public StateTransitionReferenceWidget() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(".ctor()");
        }

        initWidget(uiBinder.createAndBindUi(this));
 
        // Populate the list widget
        listEditor.setRowAction(jumpToAction);
        listEditor.setBottomButtons(Arrays.asList(createListenerButton));

        // Populate the collapsable sections with a randomly generate id
        String id = Document.get().createUniqueId();
        panelHeader.setDataTarget("#" + id);
        collapse.setId(id);
        
        helpLink.setVisible(false);

        updateReadOnly();
    }

    /**
     * Adds the provided {@link StateTransition} to the UI. A call to refresh is
     * not necessary.
     * 
     * @param transition The transition to add to the list. Can't be null.
     */
    public void add(StateTransition transition) {
        if (transition == null) {
            throw new IllegalArgumentException("The parameter 'transition' cannot be null.");
        }

        listEditor.addItem(transition);
    }

    /**
     * Removes the provided {@link StateTransition} from the UI. A call to
     * refresh is not necessary.
     * 
     * @param transition The transition to remove from the list. Can't be null.
     */
    public void remove(StateTransition transition) {
        if (transition == null) {
            throw new IllegalArgumentException("The parameter 'transition' cannot be null.");
        }

        listEditor.removeItem(transition);
    }

    /**
     * Updates the UI to reflect any mutations to the underlying
     * {@link StateTransition state transitions} contained within this widget
     */
    public void refresh() {
        listEditor.refresh();
    }

    /**
     * Sets the label/description for the table editor.
     * 
     * @param html The HTML as a {@link String} to display as the description.
     *        The value can be null.
     */
    public void setTableLabel(String html) {
        listEditor.setTableLabel(html);
    }
    
    /**
     * Sets the help text for the table editor.
     * @param html the HTML as a {@link String} to display as the help text. The value can be null.
     */
    public void setHelpText(String html) {
        boolean blank = StringUtils.isBlank(html);
        helpLink.setVisible(!blank);
        helpText.setHTML(blank ? null : html);
    }

    /**
     * Shows each {@link StateTransition} that references the given
     * {@link Concept}
     * 
     * @param concept The {@link Concept} for which to find references.
     */
    public void showTransitions(Concept concept) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("showTransitions(" + concept + ")");
        }

        parentScenarioObject = concept;
        HashSet<StateTransition> transitions = ScenarioClientUtility.getStateTransitionsThatReferenceNodeId(concept.getNodeId());
        listEditor.replaceItems(transitions);

        updateReadOnly();
    }

    /**
     * Shows each {@link StateTransition} that references the given {@link Task}
     * 
     * @param task The {@link Task} for which to find references.
     */
    public void showTransitions(Task task) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("showTransitions(" + task + ")");
        }

        parentScenarioObject = task;
        HashSet<StateTransition> transitions = ScenarioClientUtility.getStateTransitionsThatReferenceNodeId(task.getNodeId());
        listEditor.replaceItems(transitions);

        updateReadOnly();
    }

    /**
     * Shows each {@link StateTransition} that references the given
     * {@link Strategy}
     * 
     * @param strategy The {@link Strategy} for which to find references.
     */
    public void showTransitions(Strategy strategy) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("showTransitions(" + strategy + ")");
        }
        
        parentScenarioObject = strategy;
        HashSet<StateTransition> transitions = strategy == null ? new HashSet<StateTransition>()
                : ScenarioClientUtility.getStateTransitionsThatReferenceStrategy(strategy.getName());
        listEditor.replaceItems(transitions);

        updateReadOnly();
    }

    /**
     * Updates the read only mode based on the state of the widget.
     */
    private void updateReadOnly() {
        boolean isReadOnly = parentScenarioObject == null || ScenarioClientUtility.isReadOnly();
        setReadonly(isReadOnly);
    }

    /**
     * Sets the read only mode of the widget
     * 
     * @param isReadOnly If true, disable all widgets that allow editing. If
     *        false, enable all widgets that allow editing.
     */
    private void setReadonly(boolean isReadOnly) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("setReadonly(" + isReadOnly + ")");
        }

        createListenerButton.setEnabled(!isReadOnly);
    }
}