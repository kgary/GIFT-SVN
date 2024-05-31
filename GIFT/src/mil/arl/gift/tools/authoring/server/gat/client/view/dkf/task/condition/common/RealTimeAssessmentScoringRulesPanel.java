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

import org.gwtbootstrap3.client.ui.Collapse;
import org.gwtbootstrap3.client.ui.PanelHeader;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.Condition;
import generated.dkf.RealTimeAssessmentRules;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.validation.ScenarioValidationComposite;

public class RealTimeAssessmentScoringRulesPanel extends ScenarioValidationComposite {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(RealTimeAssessmentScoringRulesPanel.class.getName());

    /** The UiBinder that combines the ui.xml with this java class */
    private static RealTimeAssessmentScoringRulesPanelUiBinder uiBinder = GWT
            .create(RealTimeAssessmentScoringRulesPanelUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface RealTimeAssessmentScoringRulesPanelUiBinder
            extends UiBinder<Widget, RealTimeAssessmentScoringRulesPanel> {
    }

    /** The header for {@link #customizeAssessmentCollapse} */
    @UiField
    protected PanelHeader customizeAssessmentCollapseHeader;

    /** The collapse panel to contain {@link #rtaRulesWidget} */
    @UiField
    protected Collapse customizeAssessmentCollapse;

    /** The widget to customize the real time assessment rules */
    @UiField
    protected RealTimeAssessmentScoringRulesWidget rtaRulesWidget;

    /**
     * Default Constructor
     */
    public RealTimeAssessmentScoringRulesPanel() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(".ctor()");
        }

        initWidget(uiBinder.createAndBindUi(this));

        customizeAssessmentCollapse.hide();
        customizeAssessmentCollapseHeader.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (customizeAssessmentCollapse.isShown()) {
                    customizeAssessmentCollapse.hide();
                } else {
                    customizeAssessmentCollapse.show();
                }
            }
        }, ClickEvent.getType());
    }

    /**
     * Populate the widget using the provided condition.
     * 
     * @param parentCondition the condition that contains the scoring rules used
     *        to populate this widget. Can't be null.
     */
    public void populateWidget(final Condition parentCondition) {
        // populate the scoring rules wrapper
        rtaRulesWidget.populateWidget(parentCondition);
        RealTimeAssessmentRules rtaRules = rtaRulesWidget.getRealTimeAssessmentRules(parentCondition);
        if (rtaRules != null) {
            customizeAssessmentCollapse.show();
        }
    }

    /**
     * Updates the read only mode based on the state of the widget.
     * 
     * @param isReadonly true to mark this widget as read only; false otherwise.
     */
    public void setReadonly(boolean isReadonly) {
        rtaRulesWidget.setReadOnly(isReadonly);
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
        childValidationComposites.add(rtaRulesWidget);
    }
}
