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
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.shared.event.HiddenEvent;
import org.gwtbootstrap3.client.shared.event.HiddenHandler;
import org.gwtbootstrap3.client.shared.event.ShownEvent;
import org.gwtbootstrap3.client.shared.event.ShownHandler;
import org.gwtbootstrap3.client.ui.Collapse;
import org.gwtbootstrap3.client.ui.PanelHeader;
import org.gwtbootstrap3.client.ui.constants.IconType;


import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Widget;


import generated.dkf.Assessments.ConditionAssessment;
import generated.dkf.Assessments.Survey;
import generated.dkf.Concept;

import generated.dkf.Strategy;
import generated.dkf.StrategyRef;
import generated.dkf.Task;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.EnforcedButton;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.GenericListEditor;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ItemAction;
import mil.arl.gift.common.util.StringUtils.Stringifier;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared.PerfNodeAssessmentSelectorWidget;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.ScenarioEventUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.validation.ScenarioValidationComposite;

/**
 * The widget that displays the optional additional assessments for tasks and concepts.
 * 
 * @author sharrison
 */
public class AdditionalAssessmentWidget extends ScenarioValidationComposite {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(AdditionalAssessmentWidget.class.getName());

    /** The UiBinder that combines the ui.xml with this java class */
    private static AdditionalAssessmentWidgetUiBinder uiBinder = GWT.create(AdditionalAssessmentWidgetUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface AdditionalAssessmentWidgetUiBinder extends UiBinder<Widget, AdditionalAssessmentWidget> {
    }

    /** The header/title for the control */
    @UiField
    protected PanelHeader panelHeader;

    /** The collapse that contains the body of the widget */
    @UiField
    protected Collapse collapse;
    
    @UiField
    protected PerfNodeAssessmentSelectorWidget perfNodeAssessmentSelectorWidget;

    /** The list editor for the actions that reference the task/concept */
    @UiField(provided = true)
    protected GenericListEditor<Strategy> strategyRefListEditor = new GenericListEditor<Strategy>(
            new Stringifier<Strategy>() {
                @Override
                public String stringify(Strategy strategy) {
                    return strategy.getName();
                }
            });

    /** The button used to create a strategy referenced by this transition */
    private EnforcedButton createStrategyButton = new EnforcedButton(IconType.EXTERNAL_LINK, "Create Strategy",
            "Navigate to the page to create a new strategy", new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    Strategy strategy = ScenarioClientUtility.generateNewStrategy();

                    StrategyRef strategyRef = new StrategyRef();
                    strategyRef.setName(strategy.getName());

                    strategyRefListEditor.addItem(strategy);

                    ScenarioEventUtility.fireCreateScenarioObjectEvent(strategy);
                }
            });

    /**
     * Action to jump to the selected strategy page. This will be visible for each item in the
     * {@link GenericListEditor} table.
     */
    protected ItemAction<Strategy> jumpToAction = new ItemAction<Strategy>() {

        @Override
        public boolean isEnabled(Strategy item) {
            return true;
        }

        @Override
        public String getTooltip(Strategy item) {
            return "Click to navigate to this strategy";
        }

        @Override
        public IconType getIconType(Strategy item) {
            return IconType.EXTERNAL_LINK;
        }

        @Override
        public void execute(Strategy item) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("jumpToAction.execute(" + item + ")");
            }

            ScenarioEventUtility.fireJumpToEvent(item);
        }
    };

    /** The read-only flag */
    private boolean isReadOnly = false;

    /**
     * Default constructor
     */
    public AdditionalAssessmentWidget() {

        if (logger.isLoggable(Level.FINE)) {
            logger.fine(".ctor()");
        }
        
        initWidget(uiBinder.createAndBindUi(this));

        // Populate the list widget
        strategyRefListEditor.setRowAction(jumpToAction);
        strategyRefListEditor.setBottomButtons(Arrays.asList(createStrategyButton));

        // Populate the collapsible sections with a randomly generate id
        String id = Document.get().createUniqueId();
        panelHeader.setDataTarget("#" + id);
        collapse.setId(id);
    }

    /**
     * Loads the given survey context into this widget's survey assessment panel
     * 
     * @param surveyContextId the ID of the survey context to load
     */
    public void loadSurveyContext(BigInteger surveyContextId) {
        perfNodeAssessmentSelectorWidget.loadSurveyContext(surveyContextId);
    }

    /**
     * Perform actions when {@link Task} or {@link Concept} is renamed.
     * 
     * @param taskOrConcept the {@link Task} or {@link Concept} that was renamed. Can't be null.
     */
    public void onRename(Serializable taskOrConcept) {
        if (taskOrConcept == null) {
            throw new IllegalArgumentException("The parameter 'taskOrConcept' cannot be null.");
        } else if (!(taskOrConcept instanceof Task || taskOrConcept instanceof Concept)) {
            throw new IllegalArgumentException("The parameter 'taskOrConcept' must be of type 'Task' or 'Concept'.");
        }
        
        perfNodeAssessmentSelectorWidget.onRename(taskOrConcept);
    }

    /**
     * Populates the survey question panel with the data provided in the survey assessment.
     * 
     * @param taskOrConcept the {@link Task} or {@link Concept} that we are adding additional
     *        assessments for. Can't be null.
     */
    public void showAdditionalAssessments(Serializable taskOrConcept) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("populateAdditionalAssessments(" + taskOrConcept + ")");
        }
        
        // If LessonLevel is set to RTA, then hide the additional assessments, since they need the TUI
        if(GatClientUtility.isRtaLessonLevel()){
            panelHeader.setVisible(false);            
        }
        
        perfNodeAssessmentSelectorWidget.showAdditionalAssessments(taskOrConcept);

        Serializable currentTaskOrConcept = perfNodeAssessmentSelectorWidget.getTaskOrConcept();
        updateReadOnly();

        Serializable assessment = null;

        if (currentTaskOrConcept instanceof Task) {

            Task task = (Task) currentTaskOrConcept;

            if (task.getAssessments() != null && !task.getAssessments().getAssessmentTypes().isEmpty()) {
                assessment = task.getAssessments().getAssessmentTypes().get(0);
            }

        } else if (currentTaskOrConcept instanceof Concept) {

            Concept concept = (Concept) currentTaskOrConcept;

            if (concept.getAssessments() != null && !concept.getAssessments().getAssessmentTypes().isEmpty()) {
                assessment = concept.getAssessments().getAssessmentTypes().get(0);
            }
        }

        if (assessment instanceof Survey) {
            collapse.show(); // auto expand the panel when there is authored content to show
        } else if (assessment instanceof ConditionAssessment) {
            collapse.show(); // auto expand the panel when there is authored content to show
            updateActionReferences();
        } else {
            updateActionReferences();
        }
    }

    /**
     * Adds the provided {@link Strategy} to the UI. A call to refresh is
     * not necessary.
     * 
     * @param strategy The strategy to add to the list. Can't be null.
     */
    public void add(Strategy strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException("The parameter 'strategy' cannot be null.");
        }

        strategyRefListEditor.addItem(strategy);
    }

    /**
     * Removes the provided {@link Strategy} from the UI. A call to
     * refresh is not necessary.
     * 
     * @param strategy The strategy to remove from the list. Can't be null.
     */
    public void remove(Strategy strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException("The parameter 'strategy' cannot be null.");
        }

        strategyRefListEditor.removeItem(strategy);
    }
    
    /**
     * Updates the list of referenced actions with the current {@link Task} or {@link Concept}.
     */
    private void updateActionReferences() {
        Serializable currentTaskOrConcept = perfNodeAssessmentSelectorWidget.getTaskOrConcept();
        if (currentTaskOrConcept instanceof Task) {
            strategyRefListEditor.replaceItems(
                    ScenarioClientUtility.getStrategiesThatReferenceNodeId(((Task) currentTaskOrConcept).getNodeId()));
        } else if (currentTaskOrConcept instanceof Concept) {
            strategyRefListEditor.replaceItems(ScenarioClientUtility
                    .getStrategiesThatReferenceNodeId(((Concept) currentTaskOrConcept).getNodeId()));
        }
    }

    /**
     * Refreshes the editor and redraws the items in the list.
     */
    public void refreshActionReferences() {
        strategyRefListEditor.refresh();
    }

    /**
     * Removes the provided {@link Strategy} from the reference table.
     * 
     * @param strategy the {@link Strategy} to remove.
     */
    public void removeAction(Strategy strategy) {
        strategyRefListEditor.removeItem(strategy);
    }



    /**
     * Shows or Hides the collapse panel within the widget.
     * 
     * @param expand true to show the contents of the collapse panel; false to hide them.
     * @param command the command to execute when the animation is complete.
     */
    public void toggleCollapsePanel(boolean expand, final Command command) {

        if (expand) {
            if (collapse.isShown()) {
                command.execute();
            }

            /* need to have an 'on complete' handler because collapse.show() returns before
             * completing */
            final HandlerRegistration[] registrations = new HandlerRegistration[1];
            registrations[0] = collapse.addShownHandler(new ShownHandler() {

                @Override
                public void onShown(ShownEvent event) {
                    registrations[0].removeHandler();
                    command.execute();
                }
            });

            collapse.show();
        } else {
            if (collapse.isHidden()) {
                command.execute();
            }

            /* need to have an 'on complete' handler because collapse.show() returns before
             * completing */
            final HandlerRegistration[] registrations = new HandlerRegistration[1];
            registrations[0] = collapse.addHiddenHandler(new HiddenHandler() {

                @Override
                public void onHidden(HiddenEvent event) {
                    registrations[0].removeHandler();
                    command.execute();
                }
            });

            collapse.hide();
        }
    }

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
        childValidationComposites.add(perfNodeAssessmentSelectorWidget);
    }

    /**
     * Updates the read only mode based on the state of the widget.
     */
    private void updateReadOnly() {
        isReadOnly = ScenarioClientUtility.isReadOnly();
        createStrategyButton.setEnabled(!isReadOnly);
    }
}
