/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.condition.common;

import static mil.arl.gift.common.gwt.client.SafeHtmlUtils.bold;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.Image;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.constants.IconSize;
import org.gwtbootstrap3.client.ui.constants.IconType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.CompletionTime;
import generated.dkf.Condition;
import generated.dkf.Count;
import generated.dkf.Evaluator;
import generated.dkf.Evaluators;
import generated.dkf.UnitsEnumType;
import generated.dkf.ViolationTime;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.enums.OperatorEnum;
import mil.arl.gift.common.gwt.client.event.InputEvent;
import mil.arl.gift.common.gwt.client.event.InputHandler;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.gwt.client.validation.WidgetValidationStatus;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.FormattedTimeBox;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.HelpLink;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.ScenarioEventUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.CreateListAction;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemField;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemListEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemListEditor.ListChangedCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ListChangedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ListChangedEvent.ListAction;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.validation.ScenarioValidationComposite;

/**
 * The widget that displays the an optional scoring rule for the condition.
 * 
 * @author sharrison
 */
public class ScoringRuleWidget extends ScenarioValidationComposite {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(ScoringRuleWidget.class.getName());

    /** The UiBinder that combines the ui.xml with this java class */
    private static ScoringRuleWidgetUiBinder uiBinder = GWT.create(ScoringRuleWidgetUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface ScoringRuleWidgetUiBinder extends UiBinder<Widget, ScoringRuleWidget> {
    }

    /** The check box that determines if the rule evaluators should be shown or not */
    @UiField
    protected CheckBox ruleCheckBox;

    /** The icon and tooltip for the scoring rule type */
    @UiField
    protected SimplePanel checkBoxIconPanel;

    /** The panel containing the check box label (HTML or textbox) */
    @UiField
    protected InlineHTML typeLabel;

    /** The panel containing the check box label (HTML or textbox) */
    @UiField
    protected TextBox nameTextBox;

    /** The help link for explaining why the assessment name is important */
    @UiField
    protected HelpLink nameHelpLink;

    /** The panel that contains the rule data */
    @UiField
    protected FlowPanel contentPanel;

    /** The list of rule evaluators */
    @UiField(provided = true)
    protected ItemListEditor<Evaluator> evaluatorItemListEditor;

    /** The container for showing validation messages for not having a rule name. */
    private final WidgetValidationStatus ruleNameValidationStatus;

    /** The container for showing validation messages for not having at least one evaluator. */
    private final WidgetValidationStatus evaluatorSizeValidationStatus;

    /** The condition that is being scored */
    private Condition parentCondition;

    /** The callback to perform add/remove operations on the scoring rule's parent container */
    private ScoringRuleCallback scoringRuleCallback;

    /**
     * The scoring rule found in {@link #parentCondition} of the type {@link #widgetScoringRuleType}
     */
    private Serializable selectedScoringRule;

    /** The rule type that this widget is representing */
    private ScoringRuleType widgetScoringRuleType;

    /**
     * Default constructor
     * 
     * @param scoringRuleType the scoring rule type that is represented in this widget
     */
    public ScoringRuleWidget(final ScoringRuleType scoringRuleType) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(".ctor()");
        }

        if (scoringRuleType == null) {
            throw new IllegalArgumentException("The parameter 'ruleType' cannot be null.");
        }

        // set class member before ui init
        evaluatorItemListEditor = new ItemListEditor<Evaluator>(new EvaluatorItemEditor(scoringRuleType));

        // init ui
        initWidget(uiBinder.createAndBindUi(this));
        
        this.widgetScoringRuleType = scoringRuleType;

        ClickHandler headerClickHandler = new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Boolean selected = ruleCheckBox.getValue();
                ruleCheckBox.setValue(!selected, true);
            }
        };

        checkBoxIconPanel.setWidget(widgetScoringRuleType.getTooltipIcon());
        checkBoxIconPanel.sinkEvents(Event.ONCLICK);
        checkBoxIconPanel.addHandler(headerClickHandler, ClickEvent.getType());
        typeLabel.addClickHandler(headerClickHandler);
        
        ruleCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                final Boolean selected = event.getValue();

                contentPanel.setVisible(selected);

                nameHelpLink.setVisible(selected);
                nameTextBox.setVisible(selected);
                typeLabel.setVisible(!selected);
                if (selected) {
                    // revert to default
                    if (StringUtils.isBlank(nameTextBox.getText())) {
                        nameTextBox.setValue(widgetScoringRuleType.getLabel().asString());
                    }
                    scoringRuleCallback.onAdd(selectedScoringRule);
                } else {
                    scoringRuleCallback.onRemove(selectedScoringRule);
                }

                validateAllAndFireDirtyEvent(parentCondition);
            }
        });

        typeLabel.setHTML(widgetScoringRuleType.getLabel());
        nameTextBox.addDomHandler(new InputHandler() {
            @Override
            public void onInput(InputEvent event) {
                final String name = nameTextBox.getText();
                switch (widgetScoringRuleType) {
                case COUNT:
                    Count count = (Count) selectedScoringRule;
                    count.setName(name);
                    break;
                case COMPLETION_TIME:
                    CompletionTime completionTime = (CompletionTime) selectedScoringRule;
                    completionTime.setName(name);
                    break;
                case VIOLATION_TIME:
                    ViolationTime violationTime = (ViolationTime) selectedScoringRule;
                    violationTime.setName(name);
                    break;
                default:
                    throw new UnsupportedOperationException(
                            "Found unexpected rule type '" + widgetScoringRuleType + "'");
                }

                typeLabel.setHTML(StringUtils.isBlank(name) ? widgetScoringRuleType.getLabel().asString() : name);

                requestValidationAndFireDirtyEvent(parentCondition, ruleNameValidationStatus);
            }
        }, InputEvent.getType());

        evaluatorItemListEditor.setFields(buildScoringRuleItemFields());
        Widget addButton = evaluatorItemListEditor.addCreateListAction("Click here to add a new evaluator",
                new CreateListAction<Evaluator>() {
                    @Override
                    public Evaluator createDefaultItem() {
                        return new Evaluator();
                    }
                });
        evaluatorItemListEditor.addListChangedCallback(new ListChangedCallback<Evaluator>() {
            @Override
            public void listChanged(ListChangedEvent<Evaluator> event) {
                ListAction action = event.getActionPerformed();
                if (action == ListAction.ADD || action == ListAction.REMOVE) {
                    requestValidationAndFireDirtyEvent(parentCondition, evaluatorSizeValidationStatus);
                } else {
                    // list changed but isn't needed for validation
                    ScenarioEventUtility.fireDirtyEditorEvent();
                }
            }
        });

        ruleNameValidationStatus = new WidgetValidationStatus(nameTextBox,
                "A scoring rule is selected but doesn't have a name. Please enter a name for the scoring rule.");

        evaluatorSizeValidationStatus = new WidgetValidationStatus(addButton,
                "A scoring rule is selected but doesn't contain any evaulators. Please add a new evaluator.");
    }

    /**
     * Builds the item fields for the scoring rule table.
     * 
     * @return the {@link ItemField item field columns} for the scoring rule table.
     * @throws UnsupportedOperationException if the rule type is unknown
     */
    private List<ItemField<Evaluator>> buildScoringRuleItemFields() {

        ItemField<Evaluator> ruleDisplayField = new ItemField<Evaluator>(null, "100%") {
            @Override
            public Widget getViewWidget(Evaluator evaluator) {
                AssessmentLevelEnum assessment = StringUtils.isBlank(evaluator.getAssessment())
                        ? AssessmentLevelEnum.BELOW_EXPECTATION
                        : AssessmentLevelEnum.valueOf(evaluator.getAssessment());
                OperatorEnum operator = StringUtils.isBlank(evaluator.getOperator()) ? OperatorEnum.EQUALS
                        : OperatorEnum.valueOf(evaluator.getOperator());

                boolean isCount = widgetScoringRuleType == ScoringRuleType.COUNT;

                SafeHtmlBuilder sb = new SafeHtmlBuilder();
                sb.append(bold(assessment.getDisplayName()));
                sb.appendHtmlConstant(" when this condition is assessed ");
                if (!isCount) {
                    sb.appendHtmlConstant("for ");
                }
                sb.append(bold(EvaluatorItemEditor.convertOperatorString(operator, isCount)));
                sb.appendHtmlConstant(" ");
                String value = evaluator.getValue() == null ? "[not defined]" : evaluator.getValue();
                if (isCount) {
                    sb.append(bold(value));
                    sb.appendHtmlConstant(" times");
                } else {
                    try {
                        // value is in form HH:mm:ss
                        int timeInSeconds = FormattedTimeBox.getTimeFromString(value);
                        sb.append(bold(FormattedTimeBox.getDisplayText(timeInSeconds)));
                    } catch (@SuppressWarnings("unused") NumberFormatException nfe) {
                        // print it out raw
                        sb.appendEscaped(value);
                    }
                }
                sb.appendHtmlConstant(".");

                return new HTML(sb.toSafeHtml());
            }
        };

        return Arrays.asList(ruleDisplayField);
    }

    /**
     * Populate the widget using the provided condition's scoring.
     * 
     * @param scoringRuleToEdit the scoring rule to edit. If null, a new one will be created.
     * @param parentCondition the condition that contains the scoring object used to populate this
     *        widget. Can't be null.
     * @param callback the callback to perform add/remove operations on the scoring rule. Can't be
     *        null.
     */
    public void populateWidget(Serializable scoringRuleToEdit, Condition parentCondition,
            ScoringRuleCallback callback) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("populateWidget(" + scoringRuleToEdit + ")");
        }

        if (parentCondition == null) {
            throw new IllegalArgumentException("The parameter 'condition' cannot be null.");
        } else if (callback == null) {
            throw new IllegalArgumentException("The parameter 'callback' cannot be null.");
        }

        this.parentCondition = parentCondition;
        this.scoringRuleCallback = callback;

        /* If the provided scoring rule doesn't exist, create a new one. */
        final boolean originalExists = scoringRuleToEdit != null;
        selectedScoringRule = originalExists ? scoringRuleToEdit : createNewScoringRule();

        switch (widgetScoringRuleType) {
        case COUNT:
            populateWidget((Count) selectedScoringRule);
            break;
        case COMPLETION_TIME:
            populateWidget((CompletionTime) selectedScoringRule);
            break;
        case VIOLATION_TIME:
            populateWidget((ViolationTime) selectedScoringRule);
            break;
        default:
            throw new UnsupportedOperationException(
                    "The rule type '" + widgetScoringRuleType + "' is unknown for populateWidget().");
        }

        ruleCheckBox.setValue(originalExists);
        nameTextBox.setVisible(originalExists);
        typeLabel.setVisible(!originalExists);
        nameHelpLink.setVisible(originalExists);
        contentPanel.setVisible(originalExists);
    }

    /**
     * Populate the widget using a {@link Count} scoring rule.
     * 
     * @param count the scoring rule
     */
    private void populateWidget(Count count) {
        final String countName = count.getName();
        nameTextBox.setValue(countName);
        typeLabel.setHTML(StringUtils.isBlank(countName) ? widgetScoringRuleType.getLabel().asString() : countName);

        if (count.getEvaluators() == null) {
            count.setEvaluators(new Evaluators());
        }

        evaluatorItemListEditor.setItems(count.getEvaluators().getEvaluator());
    }

    /**
     * Populate the widget using a {@link CompletionTime} scoring rule.
     * 
     * @param time the scoring rule
     */
    private void populateWidget(CompletionTime time) {
        final String timeName = time.getName();
        nameTextBox.setValue(timeName);
        typeLabel.setHTML(StringUtils.isBlank(timeName) ? widgetScoringRuleType.getLabel().asString() : timeName);

        if (time.getEvaluators() == null) {
            time.setEvaluators(new Evaluators());
        }

        evaluatorItemListEditor.setItems(time.getEvaluators().getEvaluator());
    }

    /**
     * Populate the widget using a {@link ViolationTime} scoring rule.
     * 
     * @param time the scoring rule
     */
    private void populateWidget(ViolationTime time) {
        final String timeName = time.getName();
        nameTextBox.setValue(timeName);
        typeLabel.setHTML(StringUtils.isBlank(timeName) ? widgetScoringRuleType.getLabel().asString() : timeName);

        if (time.getEvaluators() == null) {
            time.setEvaluators(new Evaluators());
        }

        evaluatorItemListEditor.setItems(time.getEvaluators().getEvaluator());
    }

    /**
     * Create a new scoring rule based on the {@link #widgetScoringRuleType}
     * 
     * @return the new scoring rule
     */
    private Serializable createNewScoringRule() {
        switch (widgetScoringRuleType) {
        case COUNT:
            Count count = new Count();
            count.setName("Violation Count");
            count.setUnits(UnitsEnumType.COUNT);
            count.setEvaluators(new Evaluators());
            return count;
        case COMPLETION_TIME:
            CompletionTime completionTime = new CompletionTime();
            completionTime.setName("Completion Time");
            completionTime.setUnits(UnitsEnumType.HH_MM_SS);
            completionTime.setEvaluators(new Evaluators());
            return completionTime;
        case VIOLATION_TIME:
            ViolationTime violationTime = new ViolationTime();
            violationTime.setName("Violation Time");
            violationTime.setUnits(UnitsEnumType.HH_MM_SS);
            violationTime.setEvaluators(new Evaluators());
            return violationTime;
        default:
            throw new UnsupportedOperationException("Found unexpected rule type '" + widgetScoringRuleType + "'");
        }
    }

    /**
     * Retrieves the scoring rule type that is represented in this widget
     * 
     * @return the {@link ScoringRuleType}
     */
    public ScoringRuleType getType() {
        return widgetScoringRuleType;
    }

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        validationStatuses.add(ruleNameValidationStatus);
        validationStatuses.add(evaluatorSizeValidationStatus);
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        int evalItemSize;
        // if the check box is not selected, there can be no invalid widgets
        if (!ruleCheckBox.getValue()) {
            validationStatus.setValid();
            return;
        }

        if (ruleNameValidationStatus.equals(validationStatus)) {
            ruleNameValidationStatus.setValidity(StringUtils.isNotBlank(nameTextBox.getValue()));
        } else if (evaluatorSizeValidationStatus.equals(validationStatus)) {
            evalItemSize = evaluatorItemListEditor.size();
            evaluatorSizeValidationStatus.setValidity(evalItemSize > 0);
        }
    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
        childValidationComposites.add(evaluatorItemListEditor);
    }

    /**
     * Updates the read only mode based on the state of the widget.
     * 
     * @param readOnly true to mark this widget as read only; false otherwise.
     */
    public void setReadOnly(boolean readOnly) {
        ruleCheckBox.setEnabled(!readOnly);
        nameTextBox.setEnabled(!readOnly);
        evaluatorItemListEditor.setReadonly(readOnly);
    }

    /** The different types of Scoring Rules */
    public enum ScoringRuleType {
        /** count rule */
        COUNT("Violation Count", Count.class),
        /** completion time rule */
        COMPLETION_TIME("Completion Time", CompletionTime.class),
        /** violation time rule */
        VIOLATION_TIME("Violation Time", ViolationTime.class);

        /** The default label for the scoring rule */
        private SafeHtml label;

        /** The class that the enum is representing */
        private Class<?> clazz;

        /**
         * Constructor.
         * 
         * @param label the default label of the scoring rule type to be display to the user
         * @param clazz the class that the enum is representing
         */
        private ScoringRuleType(String label, Class<?> clazz) {
            this.label = SafeHtmlUtils.fromTrustedString(label);
            this.clazz = clazz;
        }

        /**
         * The tooltip icon used to indicate the scoring rule type
         * 
         * @return the visual representation of the enum
         */
        public Tooltip getTooltipIcon() {

            switch (this) {
            case COUNT:
                Icon countIcon = new Icon(IconType.HAND_PEACE_O);
                countIcon.setSize(IconSize.LARGE);
                return new Tooltip(countIcon, "Number of Events");
            case COMPLETION_TIME:
                Icon completionTimeIcon = new Icon(IconType.CLOCK_O);
                completionTimeIcon.setSize(IconSize.LARGE);
                return new Tooltip(completionTimeIcon, "Time to Complete");
            case VIOLATION_TIME:
                Image image = new Image("images/totalTime.png");
                image.setHeight("25px");
                image.setWidth("30px");
                return new Tooltip(image, "Events Total Time");
            default:
                // unhandled enum case
                Icon unknownIcon = new Icon(IconType.QUESTION);
                unknownIcon.setSize(IconSize.LARGE);
                unknownIcon.setColor("red");
                return new Tooltip(unknownIcon, "Unknown scoring rule type");
            }
        }

        /**
         * Checks if the provided item is the same class type as the enum is representing.
         * 
         * @param scoringRule the item to check
         * @return true if the item is the correct type; false otherwise
         */
        public boolean isCorrectType(Serializable scoringRule) {
            return StringUtils.equals(clazz.getName(), scoringRule.getClass().getName());
        }

        /**
         * The default label of the scoring rule.
         * 
         * @return the default label of the scoring rule to be display to the user
         */
        public SafeHtml getLabel() {
            return label;
        }
    }

    /**
     * Interface defining a callback for when a scoring rule changes.
     * 
     * @author sharrison
     */
    public interface ScoringRuleCallback {

        /**
         * Invokes logic to remove the provided scoring rule.
         * 
         * @param scoringRule the rule to remove
         */
        public void onRemove(Serializable scoringRule);

        /**
         * Invokes logic to add the provided scoring rule.
         * 
         * @param scoringRule the rule to add
         */
        public void onAdd(Serializable scoringRule);
    }
}
