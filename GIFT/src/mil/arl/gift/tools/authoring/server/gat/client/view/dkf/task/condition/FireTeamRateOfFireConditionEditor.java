/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.condition;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.DescriptionData;
import org.gwtbootstrap3.extras.slider.client.ui.Range;
import org.gwtbootstrap3.extras.slider.client.ui.RangeSlider;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.FireTeamRateOfFireCondition;
import generated.dkf.TeamMemberRefs;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.gwt.client.validation.WidgetValidationStatus;
import mil.arl.gift.common.gwt.client.widgets.NumberSpinner;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared.EditableTeamPicker;

/**
 * A {@link ConditionInputPanel} that is used to edit the
 * {@link FireTeamRateOfFireCondition} input.
 *
 * @author tflowers
 *
 */
public class FireTeamRateOfFireConditionEditor extends ConditionInputPanel<FireTeamRateOfFireCondition> {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(FireTeamRateOfFireConditionEditor.class.getName());

    /** The binder that binds this java class to a ui.xml */
    private static FireTeamRateOfFireConditionEditorUiBinder uiBinder = GWT
            .create(FireTeamRateOfFireConditionEditorUiBinder.class);

    /** The definition of the binder that binds a java class to a ui.xml */
    interface FireTeamRateOfFireConditionEditorUiBinder extends UiBinder<Widget, FireTeamRateOfFireConditionEditor> {
    }
    
    /** Maximum value allowed. */
    private static final int MAX_ASSESSMENT_SLIDER_VALUE = 1;
    
    /** Minimum value allowed. */
    private static final int MIN_ASSESSMENT_SLIDER_VALUE = 0;
    
    /** Label to indicate that the assessment level will not be used. */
    private static final String UNUSED_LABEL = "Unused";


    /** The picker that selects the team members to assess */
    @UiField(provided = true)
    protected EditableTeamPicker teamPicker = new EditableTeamPicker(true);
    
    /** rounds per minute to fire during rapid fire interval */
    @UiField
    protected NumberSpinner roundsPerMinRapidFireSpinner;
    
    /** min seconds that rapid fire rate of fire should happen w/in rapidFireSecSpinner */
    @UiField
    protected NumberSpinner minRapidFireSecSpinner;
    
    /** window of time (seconds) that rapid fire rate of fire should happen w/in */
    @UiField
    protected NumberSpinner rapidFireSecSpinner;

    /** seconds until first assessment */
    @UiField
    protected NumberSpinner firstSecSpinner;
    
    /** seconds of each assessment after the first assessment */
    @UiField
    protected NumberSpinner subsequentSecSpinner;
    
    /** the assessment rules slider */
    @UiField
    protected RangeSlider assessmentSlider;
    
    /** text to show the current value of the below expectation assessment bounds */
    @UiField
    DescriptionData belowExpectationText;
    
    /** text to show the current value of the at expectation assessment bounds */
    @UiField
    DescriptionData atExpectationText;
    
    /** text to show the current value of the above expectation assessment bounds */
    @UiField
    DescriptionData aboveExpectationText;

    /** The validation object used to validate the team member picker value */
    private final WidgetValidationStatus teamMemberValidation = new WidgetValidationStatus(teamPicker.getTextBoxRef(),
            "At least one team member must be chosen.");
    
    /** the validation object used to validate the rapid fire window spinner */
    private final WidgetValidationStatus rapidFireSecValidation;

    /**
     * Builds an unpopulated {@link FireTeamRateOfFireCondition}.
     */
    public FireTeamRateOfFireConditionEditor() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("FireTeamRateOfFireConditionEditor()");
        }

        initWidget(uiBinder.createAndBindUi(this));
        
        rapidFireSecValidation = new WidgetValidationStatus(rapidFireSecSpinner, 
                "The Rapid Fire interval must be greater than or equal to the 'maintain for at least' value.");
        
        roundsPerMinRapidFireSpinner.setMinValue(1);
        minRapidFireSecSpinner.setMinValue(1);
        rapidFireSecSpinner.setMinValue(1);
        firstSecSpinner.setMinValue(1);
        subsequentSecSpinner.setMinValue(1);
        
        roundsPerMinRapidFireSpinner.addValueChangeHandler(new ValueChangeHandler<Integer>() {

            @Override
            public void onValueChange(ValueChangeEvent<Integer> event) {
                validateAll();                
            }
        });
        
        minRapidFireSecSpinner.addValueChangeHandler(new ValueChangeHandler<Integer>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<Integer> event) {
                validateAll();
            }
        });
        
        rapidFireSecSpinner.addValueChangeHandler(new ValueChangeHandler<Integer>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<Integer> event) {
                validateAll();
            }
        });
        
        firstSecSpinner.addValueChangeHandler(new ValueChangeHandler<Integer>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<Integer> event) {
                validateAll();
            }
        });
        
        subsequentSecSpinner.addValueChangeHandler(new ValueChangeHandler<Integer>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<Integer> event) {
                validateAll();
            }
        });
        
        updateAssessmentTableText(assessmentSlider.getValue());
    }

    /**
     * Handles when the value of the {@link #teamPicker} changes.
     *
     * @param event The event containing the updated value of
     *        {@link #teamPicker}. Can't be null.
     */
    @UiHandler("teamPicker")
    protected void onTeamMembersChanged(ValueChangeEvent<List<String>> event) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("onTeamMembersChanged(" + event.toDebugString() + ")");
        }

        final List<String> teamMemberRef = getInput().getTeamMemberRefs().getTeamMemberRef();
        teamMemberRef.clear();
        teamMemberRef.addAll(event.getValue());

        requestValidationAndFireDirtyEvent(getCondition(), teamMemberValidation);
    }
    
    /**
     * Handles when the sustained rate of fire TG ratio slider is changed.
     * 
     * @param event the change event
     */
    @UiHandler("assessmentSlider")
    void onAssessmentSliderRangeChange(ValueChangeEvent<Range> event) {
        logger.info("assessmentSlider: change, value = " + event.getValue());

        // update data model
        Range range = event.getValue();
        getInput().setBelowExpectationUpperBound(new BigDecimal(range.getMinValue()));
        getInput().setAtExpectationUpperBound(new BigDecimal(range.getMaxValue()));
        
        updateAssessmentTableText(event.getValue());
    }
    
    /**
     * Handles the widget change events for the amount of time until the first sustained rate of fire assessment happens.
     * 
     * @param event the change event
     */
    @UiHandler("firstSecSpinner")
    void onFirstSecSpinnerValueChange(ValueChangeEvent<Integer> event) {
        logger.info("firstSecSpinner: change, value = " + event.getValue());

        // update data model
        Integer value = event.getValue();
        getInput().setSecUntilFirstAssessment(BigInteger.valueOf(value));  
        
        fireDirtyEvent(getCondition());
    }
    
    /**
     * Handles the widget change events for the rounds per minute that should be fired during rapid fire rate of fire
     * 
     * @param event the change event
     */
    @UiHandler("roundsPerMinRapidFireSpinner")
    void onRoundsPerMinRapidFireSpinnerValueChange(ValueChangeEvent<Integer> event) {
        logger.info("roundsPerMinRapidFireSpinner: change, value = " + event.getValue());

        // update data model
        Integer value = event.getValue();
        getInput().setRapidFireRoundsPerMinute(BigInteger.valueOf(value));  
        
        fireDirtyEvent(getCondition());
    }
    
    /**
     * Handles the widget change events for the window of time (seconds) that rapid fire rate of fire should happen w/in
     * 
     * @param event the change event
     */
    @UiHandler("rapidFireSecSpinner")
    void onRapidFireSecSpinnerValueChange(ValueChangeEvent<Integer> event) {
        logger.info("rapidFireSecSpinner: change, value = " + event.getValue());

        // update data model
        Integer value = event.getValue();
        getInput().setRapidFireInterval(BigDecimal.valueOf(value));  
        
        requestValidationAndFireDirtyEvent(getCondition(), rapidFireSecValidation);
    }
    
    /**
     * Handles the widget change events for the min seconds that rapid fire rate of fire should happen w/in rapidFireSecSpinner
     * 
     * @param event the change event
     */
    @UiHandler("minRapidFireSecSpinner")
    void onMinRapidFireSecSpinnerValueChange(ValueChangeEvent<Integer> event) {
        logger.info("minRapidFireSecSpinner: change, value = " + event.getValue());

        // update data model
        Integer value = event.getValue();
        getInput().setMinRapidFireInterval(BigDecimal.valueOf(value));  
        
        requestValidationAndFireDirtyEvent(getCondition(), rapidFireSecValidation);
    }
    
    /**
     * Handles the widget change events for the amount of time until subsequent sustained rate of fire
     * assessment happen, again and again.
     * @param event the change event
     */
    @UiHandler("subsequentSecSpinner")
    void onSubsequentSecSpinnerValueChange(ValueChangeEvent<Integer> event) {
        logger.info("subsequentSecSpinner: change, value = " + event.getValue());

        // update data model
        Integer value = event.getValue();
        getInput().setSubsequentAssessmentInterval(BigInteger.valueOf(value));      
        
        fireDirtyEvent(getCondition());
    }


    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("getValidationStatuses(" + validationStatuses + ")");
        }

        validationStatuses.add(teamMemberValidation);
        validationStatuses.add(rapidFireSecValidation);
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("validate(" + validationStatus + ")");
        }

        if (teamMemberValidation.equals(validationStatus)) {
            final int teamRefCount = getInput().getTeamMemberRefs().getTeamMemberRef().size();
            validationStatus.setValidity(teamRefCount >= 1);
        }else if(rapidFireSecValidation.equals(validationStatus)) {
            BigDecimal min = getInput().getMinRapidFireInterval();
            BigDecimal window = getInput().getRapidFireInterval();
            boolean valid = true;
            if(min != null && window != null) {
                // window must be >= min value
                valid = window.compareTo(min) >= 0;
            }
            validationStatus.setValidity(valid);
        }
    }

    @Override
    protected void onEdit() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("onEdit()");
        }

        /* Ensure that there is a value for the assessed teams. */
        TeamMemberRefs teamMemberRefs = getInput().getTeamMemberRefs();
        if (teamMemberRefs == null) {
            teamMemberRefs = new TeamMemberRefs();
            getInput().setTeamMemberRefs(teamMemberRefs);
        }

        teamPicker.setValue(teamMemberRefs.getTeamMemberRef());
        
        if(getInput().getRapidFireRoundsPerMinute() != null) {
            roundsPerMinRapidFireSpinner.setValue(getInput().getRapidFireRoundsPerMinute().intValue());
        }else {
            roundsPerMinRapidFireSpinner.setValue(300); // default - from dkf.xsd
        }
        
        if(getInput().getRapidFireInterval() != null){
            rapidFireSecSpinner.setValue(getInput().getRapidFireInterval().intValue());
        }else{
            rapidFireSecSpinner.setValue(60);  //default - from dkf.xsd
        }
        
        if(getInput().getMinRapidFireInterval() != null){
            minRapidFireSecSpinner.setValue(getInput().getMinRapidFireInterval().intValue());
        }else{
            minRapidFireSecSpinner.setValue(30);  //default - from dkf.xsd
        }
        
        if(getInput().getSecUntilFirstAssessment() != null){
            firstSecSpinner.setValue(getInput().getSecUntilFirstAssessment().intValue());
        }else{
            firstSecSpinner.setValue(30);  //default - from dkf.xsd
        }
        
        if(getInput().getSubsequentAssessmentInterval() != null){
            subsequentSecSpinner.setValue(getInput().getSubsequentAssessmentInterval().intValue());
        }else{
            subsequentSecSpinner.setValue(5); // default - from dkf.xsd
        }
        
        // required - set default
        if(getInput().getBelowExpectationUpperBound() == null){
            getInput().setBelowExpectationUpperBound(new BigDecimal(0.2));
        }
        
        // required - set default
        if(getInput().getAtExpectationUpperBound() == null){
            getInput().setAtExpectationUpperBound(new BigDecimal(0.35));
        }
        
        assessmentSlider.setValue(new Range(getInput().getBelowExpectationUpperBound().doubleValue(), getInput().getAtExpectationUpperBound().doubleValue()));
        updateAssessmentTableText(assessmentSlider.getValue());
    }
    
    /**
     * Updates the assessment table text based on the range value.
     * 
     * @param range - The range value of the assessment slider. 
     */
    private void updateAssessmentTableText(Range range) {
        
        // update below expectation text
        if(range.getMinValue() > MIN_ASSESSMENT_SLIDER_VALUE){
            belowExpectationText.setText("TG ratio \u003C " + range.getMinValue() );
        }else{
            belowExpectationText.setText(UNUSED_LABEL);
        }
        
        // update at expectation text
        if (range.getMinValue() != range.getMaxValue()) {            
            atExpectationText.setText(range.getMinValue() + " \u2264 TG ratio \u003C " + range.getMaxValue());
        } else {
            atExpectationText.setText(UNUSED_LABEL);
        }
        
        // update above expectation text
        if (range.getMaxValue() != MAX_ASSESSMENT_SLIDER_VALUE) {
            aboveExpectationText.setText(range.getMaxValue() + " \u2264 TG ratio \u2264 1");
        } else {
            aboveExpectationText.setText(UNUSED_LABEL);
        }
        
    }

    @Override
    protected void setReadonly(boolean isReadonly) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("setReadonly(" + isReadonly + ")");
        }

        teamPicker.setReadonly(isReadonly);
        assessmentSlider.setEnabled(!isReadonly);
        firstSecSpinner.setEnabled(!isReadonly);
        subsequentSecSpinner.setEnabled(!isReadonly);
        minRapidFireSecSpinner.setEnabled(!isReadonly);
        rapidFireSecSpinner.setEnabled(!isReadonly);
        roundsPerMinRapidFireSpinner.setEnabled(!isReadonly);
    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("addValidationCompositeChildren(" + childValidationComposites + ")");
        }

        childValidationComposites.add(teamPicker);
    }
}
