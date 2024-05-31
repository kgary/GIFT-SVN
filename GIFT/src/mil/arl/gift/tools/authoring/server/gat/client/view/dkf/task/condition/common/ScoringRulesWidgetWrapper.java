/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.condition.common;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.Condition;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.condition.common.ScoringRuleWidget.ScoringRuleType;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.validation.ScenarioValidationComposite;

/**
 * The widget that displays the optional scoring rules for the condition.
 * 
 * @author sharrison
 */
public abstract class ScoringRulesWidgetWrapper extends ScenarioValidationComposite {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(ScoringRulesWidgetWrapper.class.getName());

    /** The UiBinder that combines the ui.xml with this java class */
    private static ScoringRulesWidgetWrapperUiBinder uiBinder = GWT.create(ScoringRulesWidgetWrapperUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface ScoringRulesWidgetWrapperUiBinder extends UiBinder<Widget, ScoringRulesWidgetWrapper> {
    }

    /** The count scoring rule widget */
    @UiField(provided = true)
    protected ScoringRuleWidget countRuleWidget = new ScoringRuleWidget(ScoringRuleType.COUNT);

    /** The completion time scoring rule widget */
    @UiField(provided = true)
    protected ScoringRuleWidget completionTimeRuleWidget = new ScoringRuleWidget(ScoringRuleType.COMPLETION_TIME);

    /** The violation time scoring rule widget */
    @UiField(provided = true)
    protected ScoringRuleWidget violationTimeRuleWidget = new ScoringRuleWidget(ScoringRuleType.VIOLATION_TIME);

    /** The set of widgets being used */
    protected Set<ScoringRuleWidget> widgetsInUse = new HashSet<>();

    /**
     * Constructor
     * 
     * @param ruleTypes the {@link ScoringRuleType scoring rule types} to display in the panel.
     */
    public ScoringRulesWidgetWrapper(ScoringRuleType... ruleTypes) {
        this(ruleTypes == null ? null : Arrays.asList(ruleTypes));
    }

    /**
     * Constructor
     * 
     * @param ruleTypes the {@link ScoringRuleType scoring rule types} to display in the panel.
     */
    public ScoringRulesWidgetWrapper(List<ScoringRuleType> ruleTypes) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(".ctor()");
        }

        initWidget(uiBinder.createAndBindUi(this));

        if (ruleTypes == null) {
            throw new IllegalArgumentException("The parameter 'allowedRuleTypes' cannot be null.");
        } else if (ruleTypes.isEmpty()) {
            throw new IllegalArgumentException(
                    "The parameter 'allowedRuleTypes' must contain at least one allowed rule type.");
        }

        /* Set rule widgets an inactive for validation until they are added to the panel */
        for (ValidationComposite validationChildren : getChildren()) {
            validationChildren.setActive(false);
        }

        enableScoringRuleWidgets(ruleTypes);
    }
    
    /**
     * Set the visibility of the overall assessment panel components based
     * on the scoring rule types applicable to the condition being authored for this panel instance.
     * @param ruleTypes zero or more scoring rule types that correspond to the UI components to show.
     * Can't be null.
     */
    public void enableScoringRuleWidgets(List<ScoringRuleType> ruleTypes){
        
        // turn everything invisible first
        widgetsInUse.clear();
        countRuleWidget.setVisible(false);
        completionTimeRuleWidget.setVisible(false);
        violationTimeRuleWidget.setVisible(false);
        
        // ensure unique
        HashSet<ScoringRuleType> uniqueTypes = new HashSet<>(ruleTypes);
        for (ScoringRuleType type : uniqueTypes) {
            switch (type) {
            case COUNT:
                countRuleWidget.setVisible(true);

                /* if this is the only type being shown, remove bottom margin */
                if (uniqueTypes.size() == 1) {
                    countRuleWidget.getElement().getStyle().setMarginBottom(0, Unit.PX);
                }

                widgetsInUse.add(countRuleWidget);
                continue;
            case COMPLETION_TIME:
                completionTimeRuleWidget.setVisible(true);

                /* if this is the only type being shown or the last type being shown, remove bottom margin */
                if (uniqueTypes.size() == 1 || !uniqueTypes.contains(ScoringRuleType.VIOLATION_TIME)) {
                    completionTimeRuleWidget.getElement().getStyle().setMarginBottom(0, Unit.PX);
                }

                widgetsInUse.add(completionTimeRuleWidget);
                continue;
            case VIOLATION_TIME:
                violationTimeRuleWidget.setVisible(true);
                widgetsInUse.add(violationTimeRuleWidget);
                continue;
            default:
                throw new IllegalArgumentException(
                        "The parameter 'allowedRuleTypes' contains an unknown scoring rule type '" + type + "'.");
            }
        }

        /* Mark the widgets that are being used as active for validation */
        for (ScoringRuleWidget widget : widgetsInUse) {
            widget.setActive(true);
        }
    }

    /**
     * Populate the widget using the provided condition.
     * 
     * @param parentCondition the condition that contains the scoring rules used to populate this
     *        widget. Can't be null.
     */
    public abstract void populateWidget(final Condition parentCondition);

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
        childValidationComposites.add(countRuleWidget);
        childValidationComposites.add(completionTimeRuleWidget);
        childValidationComposites.add(violationTimeRuleWidget);
    }

    /**
     * Updates the read only mode based on the state of the widget.
     * 
     * @param readOnly true to mark this widget as read only; false otherwise.
     */
    public void setReadOnly(boolean readOnly) {
        for (ScoringRuleWidget widget : widgetsInUse) {
            widget.setReadOnly(readOnly);
        }
    }
}
