/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.condition.common;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.extras.select.client.ui.Option;
import org.gwtbootstrap3.extras.select.client.ui.Select;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.Evaluator;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.enums.OperatorEnum;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.gwt.client.validation.WidgetValidationStatus;
import mil.arl.gift.common.gwt.client.widgets.NumberSpinner;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.FormattedTimeBox;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.condition.common.ScoringRuleWidget.ScoringRuleType;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioValidatorUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemEditor;

/**
 * An {@link ItemEditor} used to edit scoring rule evaluators.
 * 
 * @author sharrison
 */
public class EvaluatorItemEditor extends ItemEditor<Evaluator> {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(EvaluatorItemEditor.class.getName());

    /** The UiBinder that combines the ui.xml with this java class */
    private static EvaluatorItemEditorUiBinder uiBinder = GWT.create(EvaluatorItemEditorUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface EvaluatorItemEditorUiBinder extends UiBinder<Widget, EvaluatorItemEditor> {
    }

    /** The assessment select */
    @UiField
    protected Select assessmentSelect;

    /** The label between the assessment and operator */
    @UiField
    protected InlineHTML firstLabel;

    /** The operator select */
    @UiField
    protected Select operatorSelect;

    /**
     * The number spinner if we are evaluating a scoring rule associated with numbers (e.g. count)
     */
    @UiField(provided = true)
    protected NumberSpinner countSpinner = new NumberSpinner(0, 0, Integer.MAX_VALUE);

    /**
     * The time box if we are evaluating a scoring rule associated with time (e.g. completion or
     * violation time)
     */
    @UiField
    protected FormattedTimeBox timeBox;

    /** The label at the end to specify units */
    @UiField
    protected InlineHTML secondLabel;

    /** Flag to indicate if this item editor is editing a {@link Count} scoring rule evaluator */
    private boolean isCount = false;

    /** The container for showing validation messages for not having an assessment selected. */
    private final WidgetValidationStatus assessmentValidationStatus;

    /** The container for showing validation messages for not having an operator selected. */
    private final WidgetValidationStatus operatorValidationStatus;

    /**
     * The container for showing validation messages for not having a valid value in the count
     * spinner.
     */
    private final WidgetValidationStatus countValidationStatus;

    /**
     * The container for showing validation messages for not having a valid value in the time box.
     */
    private final WidgetValidationStatus timeValidationStatus;

    /**
     * Constructor.
     * 
     * @param scoringRuleType the scoring rule type that is being modified in this editor.
     */
    public EvaluatorItemEditor(ScoringRuleType scoringRuleType) {
        if (scoringRuleType == null) {
            throw new IllegalArgumentException("The parameter 'scoringRuleType' cannot be null.");
        }

        initWidget(uiBinder.createAndBindUi(this));

        assessmentValidationStatus = new WidgetValidationStatus(assessmentSelect,
                "The assessment is missing from the evaluator. Please add an assessment type.");
        operatorValidationStatus = new WidgetValidationStatus(operatorSelect,
                "The operator is missing from the evaluator. Please add an operator.");
        countValidationStatus = new WidgetValidationStatus(countSpinner, "Please select a valid number.");
        timeValidationStatus = new WidgetValidationStatus(timeBox, "Please enter a valid time value.");

        isCount = ScoringRuleType.COUNT == scoringRuleType;

        if (isCount) {
            countSpinner.setVisible(true);
            secondLabel.setVisible(true);
        } else {
            timeBox.setVisible(true);
        }

        for (AssessmentLevelEnum assessmentEnum : AssessmentLevelEnum.VALUES()) {
            Option option = new Option();
            option.setText(assessmentEnum.getDisplayName());
            option.setValue(assessmentEnum.getName());
            assessmentSelect.add(option);
        }

        for (OperatorEnum operatorEnum : OperatorEnum.VALUES()) {
            Option option = new Option();
            option.setText(convertOperatorString(operatorEnum, isCount));
            option.setValue(operatorEnum.getName());
            operatorSelect.add(option);
        }

        // add value change handlers to request validation on change
        assessmentSelect.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> arg0) {
                requestValidation(assessmentValidationStatus);
            }
        });

        operatorSelect.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> arg0) {
                requestValidation(operatorValidationStatus);
            }
        });

        countSpinner.addValueChangeHandler(new ValueChangeHandler<Integer>() {
            @Override
            public void onValueChange(ValueChangeEvent<Integer> arg0) {
                requestValidation(countValidationStatus);
            }
        });

        timeBox.addValueChangeHandler(new ValueChangeHandler<Integer>() {
            @Override
            public void onValueChange(ValueChangeEvent<Integer> arg0) {
                requestValidation(timeValidationStatus);
            }
        });
    }

    /**
     * Converts the {@link OperatorEnum} into a user-friendly string for display
     * 
     * @param operator the {@link OperatorEnum}
     * @param isCount flag indicating if the operator is coming from a {@link Count} scoring rule.
     * @return the user-friendly display string
     * @throws UnsupportedOperationException if the operator type is unknown
     */
    public static String convertOperatorString(OperatorEnum operator, boolean isCount) {

        if (OperatorEnum.EQUALS.equals(operator)) {
            return "exactly";
        } else if (OperatorEnum.LT.equals(operator)) {
            return isCount ? "fewer than" : operator.getDisplayName().toLowerCase();
        } else if (OperatorEnum.GT.equals(operator)) {
            return "more than";
        } else if (OperatorEnum.LTE.equals(operator)) {
            return isCount ? "fewer than or exactly" : "less than or exactly";
        } else if (OperatorEnum.GTE.equals(operator)) {
            return "more than or exactly";
        } else {
            throw new UnsupportedOperationException("Found unexpected operator type '" + operator + "'");
        }
    }

    @Override
    protected void populateEditor(Evaluator obj) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("populateEditor(" + obj + ")");
        }

        // set assessment
        AssessmentLevelEnum assessment = StringUtils.isBlank(obj.getAssessment())
                ? AssessmentLevelEnum.BELOW_EXPECTATION
                : AssessmentLevelEnum.valueOf(obj.getAssessment());
        assessmentSelect.setValue(assessment.getName());

        // set operator
        OperatorEnum operator = StringUtils.isBlank(obj.getOperator()) ? OperatorEnum.EQUALS
                : OperatorEnum.valueOf(obj.getOperator());
        operatorSelect.setValue(operator.getName());

        // set first label text
        SafeHtmlBuilder sb = new SafeHtmlBuilder();
        sb.appendHtmlConstant(" when this condition is assessed ");
        if (!isCount) {
            sb.appendHtmlConstant("for ");
        }
        firstLabel.setHTML(sb.toSafeHtml());

        // set the count spinner or time box
        Integer itemValue;
        try {
            // if a time type, value will be in the form HH:mm:ss
            String objValue = obj.getValue();
            itemValue = isCount ? Integer.valueOf(objValue) : FormattedTimeBox.getTimeFromString(objValue);
        } catch (@SuppressWarnings("unused") Exception e) {
            itemValue = 0;
        }

        countSpinner.setValue(isCount ? itemValue : null);
        timeBox.setValue(isCount ? null : itemValue);
    }

    @Override
    protected void applyEdits(Evaluator obj) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("applyEdits(" + obj + ")");
        }

        obj.setAssessment(assessmentSelect.getValue());
        obj.setOperator(operatorSelect.getValue());
        // time box needs to return the value in 'HH:mm:ss' format (e.g. getValueAsText)
        obj.setValue(isCount ? countSpinner.getValue().toString() : timeBox.getValueAsText());
    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
        // no validation composite children
    }

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        validationStatuses.add(assessmentValidationStatus);
        validationStatuses.add(operatorValidationStatus);
        validationStatuses.add(countValidationStatus);
        validationStatuses.add(timeValidationStatus);
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        if (assessmentValidationStatus.equals(validationStatus)) {
            assessmentValidationStatus.setValidity(StringUtils.isNotBlank(assessmentSelect.getValue()));
        } else if (operatorValidationStatus.equals(validationStatus)) {
            operatorValidationStatus.setValidity(StringUtils.isNotBlank(operatorSelect.getValue()));
        } else if (countValidationStatus.equals(validationStatus)) {
            /* if the scoring rule is not Count, then this widget is not visible and therefore
             * should always be valid */
            countValidationStatus.setValidity(!isCount || countSpinner.getValue() != null);
        } else if (timeValidationStatus.equals(validationStatus)) {
            /* if the scoring rule is Count, then this widget is not visible and therefore should
             * always be valid */
            timeValidationStatus.setValidity(isCount || timeBox.getValue() != null);
        }
    }

    @Override
    protected boolean validate(Evaluator evaluator) {
        String errorMsg = ScenarioValidatorUtility.validateEvaluator(evaluator);
        return StringUtils.isBlank(errorMsg);
    }

    @Override
    public void setReadonly(boolean isReadonly) {
        assessmentSelect.setEnabled(!isReadonly);
        operatorSelect.setEnabled(!isReadonly);
        countSpinner.setEnabled(!isReadonly);
        timeBox.setEnabled(!isReadonly);
    }
}
