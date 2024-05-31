/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.condition.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.Collapse;
import org.gwtbootstrap3.client.ui.Panel;
import org.gwtbootstrap3.client.ui.PanelHeader;
import org.gwtbootstrap3.extras.select.client.ui.Option;
import org.gwtbootstrap3.extras.select.client.ui.Select;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.Condition;
import generated.dkf.Default;
import mil.arl.gift.common.course.InteropsInfo;
import mil.arl.gift.common.course.InteropsInfo.ConditionInfo;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.CourseConceptUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared.ConditionDescriptor;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.condition.ConditionInputPanel;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.condition.NoInputsConditionEditorImpl;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.condition.common.ScoringRuleWidget.ScoringRuleType;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.validation.ScenarioValidationComposite;

/**
 * A wrapper for the {@link ConditionInputPanel condition editors} to include common components.
 * 
 * @author sharrison
 */
public class ConditionEditorWrapper extends ScenarioValidationComposite {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(ConditionEditorWrapper.class.getName());

    /** The UiBinder that combines the ui.xml with this java class */
    private static ConditionEditorWrapperUiBinder uiBinder = GWT.create(ConditionEditorWrapperUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface ConditionEditorWrapperUiBinder extends UiBinder<Widget, ConditionEditorWrapper> {
    }

    /** The descriptor widget for the condition */
    @UiField
    protected ConditionDescriptor descriptor;

    /** The collapse panel header */
    @UiField
    protected PanelHeader realTimePanelHeader;

    /** The collapse panel for the condition panel */
    @UiField
    protected Collapse realTimePanelCollapse;

    /** The condition input panel that is being edited */
    @UiField
    protected SimplePanel conditionPanel;

    /** The list of scoring rules for the overall assessment. */
    @UiField
    protected OverallAssessmentScoringRulesWidget scoringRulesWidget;

    /** CheckBox for setting a default value for the learner's performance assessment */
    @UiField
    protected CheckBox defaultAssessmentCheckBox;

    /** Dropdown for selecting the default value for the learner's performance assessment */
    @UiField
    protected Select defaultAssessmentSelect;

    /** The collapse panel header */
    @UiField
    protected PanelHeader overallPanelHeader;

    /** The collapse panel for the overall assessments */
    @UiField
    protected Collapse overallCollapse;

    /** The {@link Condition} we are currently editing */
    private Condition selectedCondition;

    /** The collapse panel header */
    @UiField
    protected PanelHeader advancedPanelHeader;

    /** The collapse panel for the advanced panel */
    @UiField
    protected Collapse advancedPanelCollapse;
    
    /** the panel containing the overall assessment components */
    @UiField
    protected Panel overallAssessmentPanel;
    
    /** flag used to indicate whether the server has indicated that this condition type supports overall assessments */
    private boolean conditionAllowsOverallAssessments = false;
    
    /** flag used to indicate whether the server was queried yet on if this condition type supports overall assessments */
    private boolean haveQueriedOnCondition = false;

    /**
     * Constructor.
     * 
     * @param backButtonClickHandler - the click handler to attach to the back button. Can't be null.
     */
    public ConditionEditorWrapper(ClickHandler backButtonClickHandler) {
        if (backButtonClickHandler == null) {
            throw new IllegalArgumentException("The parameter 'backButtonClickHandler' cannot be null.");
        }

        initWidget(uiBinder.createAndBindUi(this));

        realTimePanelHeader.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (realTimePanelCollapse.isShown()) {
                    realTimePanelCollapse.hide();
                } else {
                    realTimePanelCollapse.show();
                }
            }
        }, ClickEvent.getType());

        overallPanelHeader.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (overallCollapse.isShown()) {
                    overallCollapse.hide();
                } else {
                    overallCollapse.show();
                }
            }
        }, ClickEvent.getType());

        advancedPanelHeader.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (advancedPanelCollapse.isShown()) {
                    advancedPanelCollapse.hide();
                } else {
                    advancedPanelCollapse.show();
                }
            }
        }, ClickEvent.getType());

        descriptor.addBackButtonClickHandler(backButtonClickHandler);

        defaultAssessmentCheckBox.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                boolean checked = defaultAssessmentCheckBox.getValue();
                defaultAssessmentSelect.setEnabled(checked);
                if (checked) {
                    selectedCondition.setDefault(new Default());
                    selectedCondition.getDefault().setAssessment(defaultAssessmentSelect.getValue());
                } else {
                    selectedCondition.setDefault(null);
                }
            }
        });

        // load the assessment options
        for (AssessmentLevelEnum assessmentEnum : AssessmentLevelEnum.VALUES()) {
            Option option = new Option();
            option.setText(assessmentEnum.getDisplayName());
            option.setValue(assessmentEnum.getName());
            defaultAssessmentSelect.add(option);
        }

        defaultAssessmentSelect.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                if (defaultAssessmentCheckBox.getValue()) {
                    selectedCondition.getDefault().setAssessment(event.getValue());
                }
            }
        });

        setReadonly(ScenarioClientUtility.isReadOnly());
    }

    /**
     * Populates the panel using the data within the given {@link Condition}.
     * 
     * @param condition the data object that will be used to populate the panel. Can't be null.
     */
    public void edit(final Condition condition) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("edit(" + condition + ")");
        }

        if (condition == null) {
            throw new IllegalArgumentException("The 'condition' parameter can't be null");
        }
        
        this.selectedCondition = condition;

        String excludedReason = ScenarioClientUtility.isConditionExcluded(condition);
        if (excludedReason != null) {
            /* If the condition is excluded, put the reason in the
             * descriptor. */
            descriptor.setConditionName(excludedReason);
        } else {
            ScenarioClientUtility.getConditionInfoForConditionImpl(condition.getConditionImpl(),
                    new AsyncCallback<InteropsInfo.ConditionInfo>() {
                        @Override
                        public void onSuccess(ConditionInfo result) {
                            descriptor.setConditionName(result.getDisplayName());
                            descriptor.setFullConditionDescription(
                                    SafeHtmlUtils.fromTrustedString(result.getConditionDesc()));
                        }

                        @Override
                        public void onFailure(Throwable caught) {
                            descriptor.setConditionName("There was a problem retrieving the Condition's information");
                        }
                    });
        }
        
        if (condition.getScoring() != null) {
            if (!condition.getScoring().getType().isEmpty()) {
                // auto expand the overall assessment panel since there is something authored
                overallCollapse.show();
            }
        }
        
        refreshOverallAssessmentPanel();

        boolean isAssessmentSelected = condition.getDefault() != null
                && StringUtils.isNotBlank(condition.getDefault().getAssessment());
        defaultAssessmentCheckBox.setValue(isAssessmentSelected);

        defaultAssessmentSelect.setEnabled(isAssessmentSelected);
        defaultAssessmentSelect.setValue(isAssessmentSelected ? condition.getDefault().getAssessment()
                : AssessmentLevelEnum.BELOW_EXPECTATION.getName());

        scoringRulesWidget.populateWidget(condition);

        // child was modified. Notify super.
        childListModified();
    }
    
    /**
     * Check if the overall assessment panel should be drawn or not based on: <br/>
     * 1. HIDE = if this DKF is for remediation, than there is no overall assessments
     * 2. SHOW = if the parent concept to this condition is a course concept, only course concepts have overall assessments
     * 3. HIDE = if the condition doesn't support authoring overall assessments, because the condition class doesn't have logic for them
     */
    public void refreshOverallAssessmentPanel(){
        
        if(ScenarioClientUtility.isRemediation()) {
            
            // do not load the editing interface for overall assessment if this DKF is being used for interactive remediation content
            overallAssessmentPanel.setVisible(false);
            
        } else {
            
            if(CourseConceptUtility.hasCourseConceptAncestor(selectedCondition)){
                // this condition is for a course concept
            
                if(!haveQueriedOnCondition){
                    // have not queried the server to determine if the condition even supports overall assessments
                    
                    // show only the overall assessment types that this condition can populate, if none
                    // than hide the overall assessment panel.
                    ScenarioClientUtility.getConditionsOverallAssessmentTypes(
                            new AsyncCallback<Map<String, Set<String>>>() {
            
                                @Override
                                public void onSuccess(Map<String, Set<String>> overallAssessmentTypesConditionsMap) {
                                    
                                    haveQueriedOnCondition = true;
            
                                    if (overallAssessmentTypesConditionsMap != null && 
                                            !overallAssessmentTypesConditionsMap.isEmpty()) {
                                        
                                        List<ScoringRuleType> ruleTypes = new ArrayList<>(3);
                                        String conditionImpl = selectedCondition.getConditionImpl();
                                        Set<String> overallAssessmentTypes = overallAssessmentTypesConditionsMap.get(conditionImpl);
                                        if(overallAssessmentTypes != null){
                                            
                                            for(String overallAssessmentTypeClazz : overallAssessmentTypes){
                                                
                                                if(overallAssessmentTypeClazz.equals(generated.dkf.Count.class.getCanonicalName())){
                                                    ruleTypes.add(ScoringRuleType.COUNT);
                                                }else if(overallAssessmentTypeClazz.equals(generated.dkf.ViolationTime.class.getCanonicalName())){
                                                    ruleTypes.add(ScoringRuleType.VIOLATION_TIME);
                                                }else if(overallAssessmentTypeClazz.equals(generated.dkf.CompletionTime.class.getCanonicalName())){
                                                    ruleTypes.add(ScoringRuleType.COMPLETION_TIME);
                                                }
                                            }
                                        }
            
                                        if (logger.isLoggable(Level.INFO)) {
                                            logger.info(
                                                    "Updating the overall assessment widget based on the types available to the condition '"
                                                            + conditionImpl + "' for the types "+ruleTypes+".");
                                        }
            
                                        // update widget 
                                        overallAssessmentPanel.setVisible(!ruleTypes.isEmpty());
                                        conditionAllowsOverallAssessments = !ruleTypes.isEmpty();
                                    }else{
                                        conditionAllowsOverallAssessments = false;
                                    }
                                }
            
                                @Override
                                public void onFailure(Throwable thrown) {
                                    logger.log(Level.SEVERE,
                                            "The server failed to retrieve the conditions overall assessment types.",
                                            thrown);
                                }
                            });
                }else{
                    logger.info("Condition "+selectedCondition.getConditionImpl()+" is under a course concept ancestor, setting overall assessment panel visiblity to previous value of "+conditionAllowsOverallAssessments);

                    // use the previously queried flag 
                    overallAssessmentPanel.setVisible(conditionAllowsOverallAssessments);
                }
            }else{                
                logger.info("Condition "+selectedCondition.getConditionImpl()+" is not under a course concept ancestor, hiding overall assessment panel");
                
                // this condition is not for a course concept, therefore don't allow overall assessments to be defined
                overallAssessmentPanel.setVisible(false);
            }
        }
    }
    
    /**
     * Sets the condition panel to be displayed to the user as the content of this wrapper.
     * 
     * @param conditionPanel The condition panel. Can't be null. Must be of type
     *        {@link ConditionInputPanel} or {@link NoInputsConditionEditorImpl}.
     * @throws UnsupportedOperationException if the condition panel type is unknown
     */
    public void setConditionPanel(Widget conditionPanel) {
        if (logger.isLoggable(Level.FINE)) {
            // don't want to log conditionPanel. Floods the log.
            logger.fine("setConditionPanel()");
        }

        if (conditionPanel == null) {
            throw new IllegalArgumentException("The parameter 'conditionPanel' cannot be null.");
        } else if (!(conditionPanel instanceof ConditionInputPanel
                || conditionPanel instanceof NoInputsConditionEditorImpl)) {
            throw new UnsupportedOperationException(
                    "The parameter 'conditionPanel' must be of type '" + ConditionInputPanel.class.getSimpleName()
                            + "' or '" + NoInputsConditionEditorImpl.class.getSimpleName() + "'");
        }

        this.conditionPanel.clear();
        this.conditionPanel.setWidget(conditionPanel);
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
        childValidationComposites.add(scoringRulesWidget);

        Widget conditionWidget = conditionPanel.getWidget();
        if (conditionWidget != null && conditionWidget instanceof ScenarioValidationComposite) {
            childValidationComposites.add((ScenarioValidationComposite) conditionWidget);
        }
    }

    /**
     * Sets the components to read only mode which prevents users from making changes.
     * 
     * @param isReadonly True to prevent editing, false to allow editing.
     */
    public void setReadonly(boolean isReadonly) {
        descriptor.setReadonly(isReadonly);
        scoringRulesWidget.setReadOnly(isReadonly);
        defaultAssessmentCheckBox.setEnabled(!isReadonly);
        defaultAssessmentSelect.setEnabled(!isReadonly);
    }
}
