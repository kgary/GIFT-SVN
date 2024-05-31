/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.condition;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.Condition;
import generated.dkf.HasMovedExcavatorComponentInput.Component;
import generated.dkf.HasMovedExcavatorComponentInput.Component.Bidirectional;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.gwt.client.validation.WidgetValidationStatus;
import mil.arl.gift.common.gwt.client.widgets.AngleBox;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.validation.ScenarioValidationComposite;

/**
 * An editor used to author excavator component directions
 * 
 * @author nroberts
 */
public class ExcavatorComponentDirectionEditor extends ScenarioValidationComposite {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(ExcavatorComponentDirectionEditor.class.getName());
    
    /** The UiBinder that combines the ui.xml with this java class */
    private static ExcavatorComponentDirectionEditorUiBinder uiBinder = GWT
            .create(ExcavatorComponentDirectionEditorUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface ExcavatorComponentDirectionEditorUiBinder extends UiBinder<Widget, ExcavatorComponentDirectionEditor> {
    }

    /** The deck panel that toggles between its child panels */
    @UiField
    protected DeckPanel directionDeck;

    /** The panel to be shown when the user selects any direction */
    @UiField
    protected Widget anyDirectionPanel;

    /** The panel to be shown when the user selects a specific direction */
    @UiField
    protected Widget anySpecificDirectionPanel;

    /** The text box to enter the angle for any direction */
    @UiField
    protected AngleBox anyAngleBox;

    /** The panel to be shown when the user selects either direction */
    @UiField
    protected Widget biDirectionPanel;

    /** The text box to enter the angle for forward direction */
    @UiField
    protected AngleBox forwardAngleBox;

    /** The text box to enter the angle for backward direction */
    @UiField
    protected AngleBox backwardAngleBox;
    
    /** Validation container for having an invalid angle for any direction */
    private final WidgetValidationStatus anyAngleValidation;
    
    /** Validation container for having an invalid angle for forward direction */
    private final WidgetValidationStatus forwardAngleValidation;
    
    /** Validation container for having an invalid angle for backward direction */
    private final WidgetValidationStatus backwardAngleValidation;

    /** The component being edited */
    private Component selectedComponent;
    
    /** The condition being edited */
    private Condition selectedCondition;

    /**
     * Constructor. Creates a new excavator component direction editor.
     */
    public ExcavatorComponentDirectionEditor() {
        initWidget(uiBinder.createAndBindUi(this));

        anyAngleValidation = new WidgetValidationStatus(anyAngleBox, "The 'any direction' angle must be 0 or greater. Please enter a valid angle.");
        forwardAngleValidation = new WidgetValidationStatus(forwardAngleBox, "The 'forward direction' angle must be 0 or greater. Please enter a valid angle.");
        backwardAngleValidation = new WidgetValidationStatus(backwardAngleBox, "The 'backward direction' angle must be 0 or greater. Please enter a valid angle.");
        
        anyAngleBox.addValueChangeHandler(new ValueChangeHandler<Double>() {
            @Override
            public void onValueChange(ValueChangeEvent<Double> event) {
                if (event.getValue() != null) {
                    selectedComponent.setDirectionType(event.getValue());
                } else {
                    if (selectedComponent.getDirectionType() == null
                            || !(selectedComponent.getDirectionType() instanceof Double)) {
                        selectedComponent.setDirectionType(Double.valueOf(0));
                    }

                    anyAngleBox.setValue((Double) selectedComponent.getDirectionType());
                }

                requestValidationAndFireDirtyEvent(selectedCondition, anyAngleValidation);
            }
        });

        forwardAngleBox.addValueChangeHandler(new ValueChangeHandler<Double>() {
            @Override
            public void onValueChange(ValueChangeEvent<Double> event) {
                if (selectedComponent.getDirectionType() == null
                        || !(selectedComponent.getDirectionType() instanceof Bidirectional)) {
                    selectedComponent.setDirectionType(new Bidirectional());
                }

                Bidirectional direction = (Bidirectional) selectedComponent.getDirectionType();
                if (event.getValue() != null) {
                    direction.setPositiveRotation(event.getValue());
                } else {
                    forwardAngleBox.setValue(direction.getPositiveRotation());
                }

                requestValidationAndFireDirtyEvent(selectedCondition, forwardAngleValidation);
            }
        });

        backwardAngleBox.addValueChangeHandler(new ValueChangeHandler<Double>() {
            @Override
            public void onValueChange(ValueChangeEvent<Double> event) {
                if (selectedComponent.getDirectionType() == null
                        || !(selectedComponent.getDirectionType() instanceof Bidirectional)) {
                    selectedComponent.setDirectionType(new Bidirectional());
                }

                Bidirectional direction = (Bidirectional) selectedComponent.getDirectionType();

                if (event.getValue() != null) {
                    direction.setNegativeRotation(event.getValue());
                } else {
                    backwardAngleBox.setValue(direction.getNegativeRotation());
                }

                requestValidationAndFireDirtyEvent(selectedCondition, backwardAngleValidation);
            }
        });
        
        
        directionDeck.showWidget(directionDeck.getWidgetIndex(anyDirectionPanel));
    }

    /**
     * Populates the editor.
     * 
     * @param component the {@link Component} containing the excavator angle data. Can't be null.
     * @param condition the condition that is referencing this editor. Can't be null.
     */
    public void edit(Component component, Condition condition) {
        if (logger.isLoggable(Level.FINE)) {
            StringBuilder sb = new StringBuilder("edit(");
            List<Object> params = Arrays.<Object>asList(component, condition);
            StringUtils.join(", ", params, sb);
            logger.fine(sb.append(")").toString());
        }

        if (component == null) {
            throw new IllegalArgumentException("The parameter 'component' cannot be null.");
        } else if (condition == null) {
            throw new IllegalArgumentException("The parameter 'condition' cannot be null.");
        }

        selectedComponent = component;
        selectedCondition = condition;

        if (component.getDirectionType() == null) {
            showAnyDirectionPanel();
        } else if (component.getDirectionType() instanceof Double) {
            showAnySpecificDirectionPanel();
            anyAngleBox.setValue((Double) component.getDirectionType());
        } else if (component.getDirectionType() instanceof Bidirectional) {
            showBiDirectionPanel();
            Bidirectional direction = (Bidirectional) component.getDirectionType();
            forwardAngleBox.setValue(direction.getPositiveRotation());
            backwardAngleBox.setValue(direction.getNegativeRotation());
        }
    }

    /** Show the 'any direction' panel */
    public void showAnyDirectionPanel() {
        directionDeck.showWidget(directionDeck.getWidgetIndex(anyDirectionPanel));
    }

    /** Show the 'any specific direction' panel */
    public void showAnySpecificDirectionPanel() {
        directionDeck.showWidget(directionDeck.getWidgetIndex(anySpecificDirectionPanel));
    }

    /** Show the 'bi direction' panel */
    public void showBiDirectionPanel() {
        directionDeck.showWidget(directionDeck.getWidgetIndex(biDirectionPanel));
    }

    /**
     * Retrieve the any angle box
     * 
     * @return the {@link HasValue} for the any angle box.
     */
    public HasValue<Double> getAnyAngleBox() {
        return anyAngleBox;
    }

    /**
     * Retrieve the forward angle box
     * 
     * @return the {@link HasValue} for the forward angle box.
     */
    public HasValue<Double> getForwardAngleBox() {
        return forwardAngleBox;
    }

    /**
     * Retrieve the backward angle box
     * 
     * @return the {@link HasValue} for the backward angle box.
     */
    public HasValue<Double> getBackwardAngleBox() {
        return backwardAngleBox;
    }

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        validationStatuses.add(anyAngleValidation);
        validationStatuses.add(forwardAngleValidation);
        validationStatuses.add(backwardAngleValidation);
    }

    @Override
    public void validate(ValidationStatus validationStatus) {

        if (anyAngleValidation.equals(validationStatus)) {
            if (directionDeck.getVisibleWidget() != directionDeck.getWidgetIndex(anySpecificDirectionPanel)) {
                anyAngleValidation.setValid();
                return;
            }

            final Double anyAngle = anyAngleBox.getValue();
            anyAngleValidation.setValidity(anyAngle != null && Double.compare(anyAngle, 0) >= 0);
        } else if (forwardAngleValidation.equals(validationStatus)) {
            if (directionDeck.getVisibleWidget() != directionDeck.getWidgetIndex(biDirectionPanel)) {
                forwardAngleValidation.setValid();
                return;
            }

            final Double forwardAngle = forwardAngleBox.getValue();
            forwardAngleValidation.setValidity(forwardAngle != null && Double.compare(forwardAngle, 0) >= 0);
        } else if (backwardAngleValidation.equals(validationStatus)) {
            if (directionDeck.getVisibleWidget() != directionDeck.getWidgetIndex(biDirectionPanel)) {
                backwardAngleValidation.setValid();
                return;
            }

            final Double backwardAngle = backwardAngleBox.getValue();
            backwardAngleValidation.setValidity(backwardAngle != null && Double.compare(backwardAngle, 0) >= 0);
        }
    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
        // no validation composite children
    }

    /**
     * Sets the components to read only mode which prevents users from making changes.
     * 
     * @param isReadonly True to prevent editing, false to allow editing.
     */
    public void setReadonly(boolean isReadonly) {
        anyAngleBox.setReadonly(isReadonly);
        forwardAngleBox.setReadonly(isReadonly);
        backwardAngleBox.setReadonly(isReadonly);
    }
}