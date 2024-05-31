/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.condition;

import java.math.BigDecimal;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.SpacingCondition.SpacingPair;
import generated.dkf.SpacingCondition.SpacingPair.Acceptable;
import generated.dkf.SpacingCondition.SpacingPair.FirstObject;
import generated.dkf.SpacingCondition.SpacingPair.Ideal;
import generated.dkf.SpacingCondition.SpacingPair.SecondObject;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.gwt.client.validation.WidgetValidationStatus;
import mil.arl.gift.common.gwt.client.widgets.DecimalNumberSpinner;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared.TeamMemberPicker;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioValidatorUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemEditor;

/**
 * A {@link ItemEditor} that allows the author to edit a {@link SpacingPair} 
 * 
 * @author nroberts
 */
public class SpacingPairItemEditor extends ItemEditor<SpacingPair> {

    private static SpacingPairItemEditorUiBinder uiBinder = GWT.create(SpacingPairItemEditorUiBinder.class);

    interface SpacingPairItemEditorUiBinder extends UiBinder<Widget, SpacingPairItemEditor> {
    }
    
    /** Picker used to select the first entity that should maintain spacing */
    @UiField(provided = true)
    protected TeamMemberPicker firstObjectPicker = new TeamMemberPicker(true);
    
    /** Picker used to select the second entity that should maintain spacing */
    @UiField(provided = true)
    protected TeamMemberPicker secondObjectPicker = new TeamMemberPicker(true);
    
    /** Input box used to specify the minimum ideal spacing */
    @UiField(provided = true)
    protected DecimalNumberSpinner idealMinBox = new DecimalNumberSpinner(BigDecimal.ZERO, BigDecimal.ZERO, null);
    
    /** Input box used to specify the maximum ideal spacing */
    @UiField(provided = true)
    protected DecimalNumberSpinner idealMaxBox = new DecimalNumberSpinner(BigDecimal.ZERO, BigDecimal.ZERO, null);
    
    /** Input box used to specify the minimum acceptable spacing */
    @UiField(provided = true)
    protected DecimalNumberSpinner acceptedMinBox = new DecimalNumberSpinner(BigDecimal.ZERO, BigDecimal.ZERO, null);
    
    /** Input box used to specify the maximum acceptable spacing */
    @UiField(provided = true)
    protected DecimalNumberSpinner acceptedMaxBox = new DecimalNumberSpinner(BigDecimal.ZERO, BigDecimal.ZERO, null);
    
    /** Validation that displays an error if no ideal min value is provided */
    private final WidgetValidationStatus noIdealMinValidation;
    
    /** Validation that displays an error if no ideal max value is provided */
    private final WidgetValidationStatus noIdealMaxValidation;
    
    /** Validation that displays an error if no acceptable min value is provided */
    private final WidgetValidationStatus noAcceptedMinValidation;
    
    /** Validation that displays an error if no acceptable max value is provided */
    private final WidgetValidationStatus noAcceptedMaxValidation;
    
    /** Validation that displays an error if the acceptable max value is less than the ideal max */
    private final WidgetValidationStatus lesserAcceptedMaxValidation;
    
    /** Validation that displays an error if the acceptable min value is greater than the ideal min */
    private final WidgetValidationStatus greaterAcceptedMinValidation;
    
    /** Validation that displays an error if the acceptable min value is greater than the acceptable max */
    private final WidgetValidationStatus acceptedMinGreaterThanMaxValidation;
    
    /** Validation that displays an error if the ideal min value is greater than the ideal max */
    private final WidgetValidationStatus idealMinGreaterThanMaxValidation;
    
    /** Validation that displays an error if the author has picked the same team member for the first and second entity */
    private final WidgetValidationStatus duplicateTeamMemberValidation;
    

    /**
     * Creates a new item editor that can edit a spacing pair and initializes its event and validation handlers
     */
    public SpacingPairItemEditor() {
        initWidget(uiBinder.createAndBindUi(this));
        
        firstObjectPicker.addValueChangeHandler(new ValueChangeHandler<String>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                validateAll();
            }
        });
        
        secondObjectPicker.addValueChangeHandler(new ValueChangeHandler<String>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                validateAll();
            }
        });
        
        idealMinBox.addValueChangeHandler(new ValueChangeHandler<BigDecimal>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<BigDecimal> event) {
                validateAll();
            }
        });
        
        idealMaxBox.addValueChangeHandler(new ValueChangeHandler<BigDecimal>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<BigDecimal> event) {
                validateAll();
            }
        });
        
        acceptedMinBox.addValueChangeHandler(new ValueChangeHandler<BigDecimal>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<BigDecimal> event) {
                validateAll();
            }
        });
        
        acceptedMaxBox.addValueChangeHandler(new ValueChangeHandler<BigDecimal>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<BigDecimal> event) {
                validateAll();
            }
        });
        
        noIdealMinValidation = new WidgetValidationStatus(idealMinBox, 
                "Please specify the ideal minimum distance these entities should maintain.");
        
        noIdealMaxValidation = new WidgetValidationStatus(idealMaxBox, 
                "Please specify the ideal maximum distance these entities should maintain.");
        
        noAcceptedMinValidation = new WidgetValidationStatus(acceptedMinBox, 
                "Please specify the acceptable minimum distance these entities should maintain.");
        
        noAcceptedMaxValidation = new WidgetValidationStatus(acceptedMaxBox, 
                "Please specify the acceptible maximum distance these entities should maintain.");
        
        lesserAcceptedMaxValidation = new WidgetValidationStatus(acceptedMaxBox, 
                "The acceptable maximum distance must be greater than the ideal maximum distance.");
        
        greaterAcceptedMinValidation = new WidgetValidationStatus(acceptedMinBox, 
                "The acceptable minimum distance must be less than than the ideal minimum distance.");
        
        acceptedMinGreaterThanMaxValidation = new WidgetValidationStatus(acceptedMinBox, 
                "The acceptable minimum distance must be less than the acceptable maximum distance.");
        
        idealMinGreaterThanMaxValidation = new WidgetValidationStatus(idealMinBox, 
                "The ideal minimum distance must be less than than the ideal maximum distance.");
        
        duplicateTeamMemberValidation = new WidgetValidationStatus(secondObjectPicker, 
                "An entity cannot maintain spacing from itself. Please pick two different entities to maintain spacing..");
    }

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        validationStatuses.add(noIdealMinValidation);
        validationStatuses.add(noIdealMaxValidation);
        validationStatuses.add(noAcceptedMinValidation);
        validationStatuses.add(noAcceptedMaxValidation);
        validationStatuses.add(lesserAcceptedMaxValidation);
        validationStatuses.add(greaterAcceptedMinValidation);
        validationStatuses.add(acceptedMinGreaterThanMaxValidation);
        validationStatuses.add(idealMinGreaterThanMaxValidation);
        validationStatuses.add(duplicateTeamMemberValidation);
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        
        if (noIdealMinValidation.equals(validationStatus)) {
            validationStatus.setValidity(idealMinBox.getValue() != null && !BigDecimal.ZERO.equals(idealMinBox.getValue()));
            
        } else if (noIdealMaxValidation.equals(validationStatus)) {
            validationStatus.setValidity(idealMaxBox.getValue() != null && !BigDecimal.ZERO.equals(idealMaxBox.getValue()));
            
        } else if (noAcceptedMinValidation.equals(validationStatus)) {
            validationStatus.setValidity(acceptedMinBox.getValue() != null && !BigDecimal.ZERO.equals(acceptedMinBox.getValue()));
            
        } else if (noAcceptedMaxValidation.equals(validationStatus)) {
            validationStatus.setValidity(acceptedMaxBox.getValue() != null && !BigDecimal.ZERO.equals(acceptedMaxBox.getValue()));
            
        } else if (lesserAcceptedMaxValidation.equals(validationStatus)) {
            validationStatus.setValidity(BigDecimal.ZERO.equals(acceptedMaxBox.getValue())
                   || BigDecimal.ZERO.equals(idealMaxBox.getValue())
                   || acceptedMaxBox.getValue().compareTo(idealMaxBox.getValue()) == 1);
            
        } else if (greaterAcceptedMinValidation.equals(validationStatus)) {
            validationStatus.setValidity(BigDecimal.ZERO.equals(acceptedMinBox.getValue())
                    || BigDecimal.ZERO.equals(idealMinBox.getValue())
                    || acceptedMinBox.getValue().compareTo(idealMinBox.getValue()) == -1);
            
        } else if (acceptedMinGreaterThanMaxValidation.equals(validationStatus)) {
            validationStatus.setValidity(BigDecimal.ZERO.equals(acceptedMinBox.getValue())
                    || BigDecimal.ZERO.equals(acceptedMaxBox.getValue())
                    || acceptedMinBox.getValue().compareTo(acceptedMaxBox.getValue()) == -1);
             
         } else if (idealMinGreaterThanMaxValidation.equals(validationStatus)) {
             validationStatus.setValidity(BigDecimal.ZERO.equals(idealMinBox.getValue())
                     || BigDecimal.ZERO.equals(idealMaxBox.getValue())
                     || idealMinBox.getValue().compareTo(idealMaxBox.getValue()) == -1);
              
          } else if (duplicateTeamMemberValidation.equals(validationStatus)) {
            validationStatus.setValidity(!StringUtils.equals(firstObjectPicker.getValue(), secondObjectPicker.getValue()));
        }
    }

    @Override
    protected boolean validate(SpacingPair spacingPair) {
        String errorMsg = ScenarioValidatorUtility.validateSpacingPair(spacingPair);
        return StringUtils.isBlank(errorMsg);
    }

    @Override
    protected void populateEditor(SpacingPair obj) {
        
        if(obj.getFirstObject() != null 
                && obj.getFirstObject().getTeamMemberRef() != null) {
            
            firstObjectPicker.setValue(obj.getFirstObject().getTeamMemberRef());
            
        } else {
            firstObjectPicker.setValue(null);
        }
        
        if(obj.getSecondObject() != null 
                && obj.getSecondObject().getTeamMemberRef() != null) {
            
            secondObjectPicker.setValue(obj.getSecondObject().getTeamMemberRef());
            
        } else {
            secondObjectPicker.setValue(null);
        }
        
        BigDecimal idealMin = null;
        BigDecimal idealMax = null;
        
        if(obj.getIdeal() != null){
            idealMin = obj.getIdeal().getIdealMinSpacing();
            idealMax = obj.getIdeal().getIdealMaxSpacing();
        }
        
        idealMinBox.setValue(idealMin);
        idealMaxBox.setValue(idealMax);
        
        BigDecimal acceptedMin = null;
        BigDecimal acceptedMax = null;
        
        if(obj.getAcceptable() != null){
            acceptedMin = obj.getAcceptable().getAcceptableMinSpacing();
            acceptedMax = obj.getAcceptable().getAcceptableMaxSpacing();
        }
        
        acceptedMinBox.setValue(acceptedMin);
        acceptedMaxBox.setValue(acceptedMax);
    }

    @Override
    protected void applyEdits(SpacingPair obj) {
        
        if(obj.getFirstObject() == null) {
            obj.setFirstObject(new FirstObject());
        }
        
        obj.getFirstObject().setTeamMemberRef(firstObjectPicker.getValue());
        
        if(obj.getSecondObject() == null) {
            obj.setSecondObject(new SecondObject());
        }
        
        obj.getSecondObject().setTeamMemberRef(secondObjectPicker.getValue());
        
        if(obj.getIdeal() == null) {
            obj.setIdeal(new Ideal());
        }
        
        obj.getIdeal().setIdealMinSpacing(idealMinBox.getValue());
        obj.getIdeal().setIdealMaxSpacing(idealMaxBox.getValue());
        
        if(obj.getAcceptable() == null) {
            obj.setAcceptable(new Acceptable());
        }
        
        obj.getAcceptable().setAcceptableMinSpacing(acceptedMinBox.getValue());
        obj.getAcceptable().setAcceptableMaxSpacing(acceptedMaxBox.getValue());
    }

    @Override
    protected void setReadonly(boolean isReadonly) {
        firstObjectPicker.setReadonly(isReadonly);
        secondObjectPicker.setReadonly(isReadonly);
        idealMinBox.setEnabled(!isReadonly);
        idealMaxBox.setEnabled(!isReadonly);
        acceptedMinBox.setEnabled(!isReadonly);
        acceptedMaxBox.setEnabled(!isReadonly);
    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
        childValidationComposites.add(firstObjectPicker);
        childValidationComposites.add(secondObjectPicker);
    }
}
