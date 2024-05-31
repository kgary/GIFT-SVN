/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.wrap.overlay;

import java.util.Set;

import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.extras.select.client.ui.Option;
import org.gwtbootstrap3.extras.select.client.ui.Select;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.Entrance;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.gwt.client.validation.ValidationWidget;
import mil.arl.gift.common.gwt.client.validation.WidgetValidationStatus;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemEditor;

/**
 * An item editor that modifies entrances of the EnterAreaCondition overlay in the wrap panel
 * 
 * @author nroberts
 */
public class EntranceItemEditor extends ItemEditor<EntranceShape> {

    private static EntranceItemEditorUiBinder uiBinder = GWT.create(EntranceItemEditorUiBinder.class);
    
    /** The text value for the placeholder option in the assessment dropdown list */
    private static final String NODE_PLACEHOLDER = "placeholder_value";

    interface EntranceItemEditorUiBinder extends UiBinder<Widget, EntranceItemEditor> {
    }
    
    /** Text box used to enter the entrance name*/
    @UiField
    protected TextBox nameBox;
    
    /** Label displaying the name of the start point */
    @UiField
    protected Label startLabel;
    
    /** Label displaying the name of the end point*/
    @UiField
    protected Label endLabel;
    
    /** The assessment dropdown */
    @UiField
    protected Select assessmentSelect;
    
    /** A validation widget used to display validation errors for this editor */
    @UiField(provided = true)
    protected ValidationWidget validations = new ValidationWidget(this);
    
    /** The name of the entrance being edited when it was originally loaded */
    private String originalName = null;
    
    /** Validation that displays an error when the author has not entered a unique name for this entrance */
    private WidgetValidationStatus nameValidation;
    
    /** The container for showing validation messages for not having an assessment set. */
    private WidgetValidationStatus assessmentValidationStatus;

    /**
     * Creates a new entrance item editor and sets up its event and validation handlers
     */
    public EntranceItemEditor() {
        initWidget(uiBinder.createAndBindUi(this));
        
        nameValidation = new WidgetValidationStatus(nameBox, "Please enter a unique name for this entrance.");
        assessmentValidationStatus = new WidgetValidationStatus(assessmentSelect, "An assessment must be selected.");
        
        nameBox.addValueChangeHandler(new ValueChangeHandler<String>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                requestValidation(nameValidation);
            }
        });
        
        Option placeholderOption = new Option();
        placeholderOption.setText("Select an assessment");
        placeholderOption.setValue(NODE_PLACEHOLDER);
        placeholderOption.setSelected(true);
        placeholderOption.setEnabled(false);
        placeholderOption.setHidden(true);
        assessmentSelect.add(placeholderOption);
        for (AssessmentLevelEnum assessmentLevel : AssessmentLevelEnum.VALUES()) {
            Option option = new Option();
            option.setText(assessmentLevel.getDisplayName());
            option.setValue(assessmentLevel.getName());
            assessmentSelect.add(option);
        }

        assessmentSelect.setValue(NODE_PLACEHOLDER);
        
        assessmentSelect.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                requestValidation(assessmentValidationStatus);
            }
        });
    }

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        validationStatuses.add(nameValidation);
        validationStatuses.add(assessmentValidationStatus);
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        
        if(nameValidation.equals(validationStatus)) {
            nameValidation.setValidity(isEnteredNameUnique(originalName, nameBox.getValue()));
            
        } else if (assessmentValidationStatus.equals(validationStatus)) {
            assessmentValidationStatus.setValidity(StringUtils.isNotBlank(assessmentSelect.getValue()));
        }
    }

    @Override
    protected boolean validate(EntranceShape obj) {
        final Entrance entrance = obj.getEntrance();
        if (entrance == null) {
            return false;
        }

        if (StringUtils.isBlank(entrance.getAssessment())) {
            return false;
        }

        String name = entrance.getName();
        if (StringUtils.isBlank(name)) {
            return false;
        }

        for (EntranceShape editorShape : getParentItemListEditor().getItems()) {
            /* Skip the provided shape */
            if (obj == editorShape) {
                continue;
            }

            if (editorShape.getEntrance() == null) {
                continue;
            } else if (StringUtils.equals(name, editorShape.getEntrance().getName())) {
                return false;
            }
        }

        return true;
    }

    @Override
    protected void populateEditor(EntranceShape obj) {
    
        //display validation errors in the validation widget inside this editor
        initValidationComposite(validations);
        
        Entrance entrance = obj.getEntrance();
        
        String name = entrance.getName();
        originalName = name;
        nameBox.setValue(name);
        
        startLabel.setText(entrance.getOutside() != null ? entrance.getOutside().getPoint() : "UNKNOWN");
        endLabel.setText(entrance.getInside() != null ? entrance.getInside().getPoint() : "UNKNOWN");
        
        assessmentSelect.setValue(StringUtils.isBlank(entrance.getAssessment()) ? NODE_PLACEHOLDER : entrance.getAssessment());
    }

    @Override
    protected void applyEdits(EntranceShape entranceShape) {
        
        if (entranceShape == null) {
            throw new IllegalArgumentException("The parameter 'entranceShape' cannot be null.");
        }
        
        entranceShape.getEntrance().setName(nameBox.getValue());
        
        entranceShape.getEntrance().setAssessment(assessmentSelect.getValue());
    }

    @Override
    protected void setReadonly(boolean isReadonly) {
        nameBox.setEnabled(!isReadonly);
        assessmentSelect.setEnabled(!isReadonly);
    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
        //no validation composite children
    }

    
    /**
     * Checks to see if the author has entered a unique name for the entrance being edited. To be considered unique,
     * the entered name must not be blank and must not match the name of any other entrance.
     * 
     * @param originalName the name this entrance had when it was loaded
     * @param name the currently edited name of this entrance
     * 
     * @return whether or not the entered name is unique
     */
    boolean isEnteredNameUnique(String originalName, String name) {
        
        String enteredName = name;
        
        if(StringUtils.isBlank(enteredName)) {
            return false;
        }
        
        for(EntranceShape shape : getParentItemListEditor().getItems()) {
            if (!StringUtils.equals(originalName, shape.getEntrance().getName()) 
                    && StringUtils.equals(enteredName, shape.getEntrance().getName())) {
                return false;
            }
        }
        
        return true;
    }
}
