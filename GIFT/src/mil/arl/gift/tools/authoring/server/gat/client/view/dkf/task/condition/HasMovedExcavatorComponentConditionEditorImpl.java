/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.condition;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.ValueListBox;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.ExcavatorComponentEnum;
import generated.dkf.HasMovedExcavatorComponentInput;
import generated.dkf.HasMovedExcavatorComponentInput.Component;
import generated.dkf.HasMovedExcavatorComponentInput.Component.Bidirectional;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.gwt.client.validation.WidgetValidationStatus;
import mil.arl.gift.common.gwt.client.widgets.NumberSpinner;

/**
 * An editor used to author has moved excavator component conditions
 * 
 * @author nroberts
 */
public class HasMovedExcavatorComponentConditionEditorImpl
        extends ConditionInputPanel<HasMovedExcavatorComponentInput> {

    /** The logger for the class */
    private static final Logger logger = Logger
            .getLogger(HasMovedExcavatorComponentConditionEditorImpl.class.getName());

    private static HasMovedExcavatorComponentConditionEditorImplUiBinder uiBinder = GWT
            .create(HasMovedExcavatorComponentConditionEditorImplUiBinder.class);

    interface HasMovedExcavatorComponentConditionEditorImplUiBinder
            extends UiBinder<Widget, HasMovedExcavatorComponentConditionEditorImpl> {
    }

    private enum MoveEvaluationType {
        IN_ANY_DIRECTION("In any direction"),
        ANGLE_IN_ANY_DIRECTION("By an accumulating angle in any direction"),
        ANGLE_IN_BOTH_DIRECTIONS("By an accumulating angle forward and backward");

        private String displayName;

        private MoveEvaluationType(String displayName) {
            this.displayName = displayName;
        }

        public static void makeOptionsFor(ValueListBox<MoveEvaluationType> select) {
            select.setValue(IN_ANY_DIRECTION);
            select.setAcceptableValues(Arrays.asList(MoveEvaluationType.values()));
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /** Renders a {@link MoveEvaluationType} within a {@link ValueListBox} */
    private final Renderer<MoveEvaluationType> renderer = new Renderer<MoveEvaluationType>() {

        @Override
        public void render(MoveEvaluationType object, Appendable appendable) throws IOException {
            String displayName = object != null ? object.getDisplayName() : "None";
            appendable.append(displayName);
        }

        @Override
        public String render(MoveEvaluationType object) {
            return object != null ? object.getDisplayName() : "None";
        }
    };

    @UiField
    protected FlowPanel useComponentPanel;

    @UiField
    protected CheckBox useBucketCheck;

    @UiField
    protected DeckPanel bucketDeck;

    @UiField
    protected Widget noBucketEditor;

    @UiField(provided = true)
    protected ValueListBox<MoveEvaluationType> bucketSelect = new ValueListBox<>(renderer);

    @UiField
    protected ExcavatorComponentDirectionEditor bucketEditor;

    @UiField
    protected CheckBox useBoomCheck;

    @UiField
    protected DeckPanel boomDeck;

    @UiField
    protected Widget noBoomEditor;

    @UiField(provided = true)
    protected ValueListBox<MoveEvaluationType> boomSelect = new ValueListBox<>(renderer);

    @UiField
    protected ExcavatorComponentDirectionEditor boomEditor;

    @UiField
    protected CheckBox useArmCheck;

    @UiField
    protected DeckPanel armDeck;

    @UiField
    protected Widget noArmEditor;

    @UiField(provided = true)
    protected ValueListBox<MoveEvaluationType> armSelect = new ValueListBox<>(renderer);

    @UiField
    protected ExcavatorComponentDirectionEditor armEditor;

    @UiField
    protected CheckBox useSwingCheck;

    @UiField
    protected DeckPanel swingDeck;

    @UiField
    protected Widget noSwingEditor;

    @UiField(provided = true)
    protected ValueListBox<MoveEvaluationType> swingSelect = new ValueListBox<>(renderer);

    @UiField
    protected ExcavatorComponentDirectionEditor swingEditor;

    @UiField(provided = true)
    protected NumberSpinner maxAssessmentsBox = new NumberSpinner(0, 0, Integer.MAX_VALUE, 1);

    @UiField
    protected CheckBox maxAssessmentsCheckbox;

    private Component bucketComponent = null;
    private Component boomComponent = null;
    private Component armComponent = null;
    private Component swingComponent = null;

    /** Flag indicating if the panel is currently being populated (on edit) */
    private boolean isPopulating = false;

    /** Validation container for having no components selected (requires at least 1) */
    private final WidgetValidationStatus componentSelectedValidation;

    /**
     * Constructor
     */
    public HasMovedExcavatorComponentConditionEditorImpl() {
        initWidget(uiBinder.createAndBindUi(this));

        componentSelectedValidation = new WidgetValidationStatus(useComponentPanel,
                "At least one excavator component must be specified for this condition. Please select a component.");

        /* mark validation children as inactive until specifically enabled */
        bucketEditor.setActive(false);
        boomEditor.setActive(false);
        armEditor.setActive(false);
        swingEditor.setActive(false);

        bucketDeck.showWidget(bucketDeck.getWidgetIndex(noBucketEditor));
        boomDeck.showWidget(boomDeck.getWidgetIndex(noBoomEditor));
        armDeck.showWidget(armDeck.getWidgetIndex(noArmEditor));
        swingDeck.showWidget(swingDeck.getWidgetIndex(noSwingEditor));

        // Bucket Editor

        useBucketCheck.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {

                List<Component> removeList = new ArrayList<Component>();

                Iterator<Component> itr = getInput().getComponent().iterator();
                while (itr.hasNext()) {
                    Component component = itr.next();
                    if (component.getComponentType() != null) {
                        if (component.getComponentType().equals(ExcavatorComponentEnum.BUCKET)) {
                            removeList.add(component);
                        }
                    }
                }

                for (Component bucket : removeList) {
                    getInput().getComponent().remove(bucket);
                }

                if (event.getValue()) {
                    if (bucketComponent == null) {
                        Component component = new Component();
                        component.setComponentType(ExcavatorComponentEnum.BUCKET);
                        component.setDirectionType(null);
                        bucketComponent = component;
                    }

                    getInput().getComponent().add(bucketComponent);
                    setBucketEditorVisible(true);
                    populateBucketComponentEditor();
                } else {
                    setBucketEditorVisible(false);
                }

                requestValidationAndFireDirtyEvent(getCondition(), componentSelectedValidation);
            }
        });

        MoveEvaluationType.makeOptionsFor(bucketSelect);
        bucketSelect.addValueChangeHandler(new ValueChangeHandler<MoveEvaluationType>() {

            @Override
            public void onValueChange(ValueChangeEvent<MoveEvaluationType> event) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("bucketSelect.onValueChange(" + event.getValue() + ")");
                }

                if (bucketComponent == null) {
                    return;
                }

                switch (event.getValue()) {
                case ANGLE_IN_ANY_DIRECTION:
                    bucketComponent.setDirectionType(Double.valueOf(0));
                    bucketEditor.showAnySpecificDirectionPanel();
                    bucketEditor.getAnyAngleBox().setValue(Double.valueOf(0));
                    break;
                case ANGLE_IN_BOTH_DIRECTIONS:
                    Bidirectional direction = new Bidirectional();
                    direction.setPositiveRotation(Double.valueOf(0));
                    direction.setNegativeRotation(Double.valueOf(0));
                    bucketComponent.setDirectionType(direction);

                    bucketEditor.showBiDirectionPanel();
                    bucketEditor.getForwardAngleBox().setValue(direction.getPositiveRotation());
                    bucketEditor.getBackwardAngleBox().setValue(direction.getNegativeRotation());
                    break;
                case IN_ANY_DIRECTION:
                    bucketComponent.setDirectionType(null);
                    bucketEditor.showAnyDirectionPanel();
                    break;
                }
            }
        });

        // Boom editor
        useBoomCheck.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {

                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("useBoomCheck.onValueChange(" + event.getValue() + ")");
                }

                List<Component> removeList = new ArrayList<Component>();

                Iterator<Component> itr = getInput().getComponent().iterator();
                while (itr.hasNext()) {
                    Component component = itr.next();
                    if (component.getComponentType() != null) {
                        if (component.getComponentType().equals(ExcavatorComponentEnum.BOOM)) {
                            removeList.add(component);
                        }
                    }
                }

                for (Component boom : removeList) {
                    getInput().getComponent().remove(boom);
                }

                if (event.getValue()) {
                    if (boomComponent == null) {
                        Component component = new Component();
                        component.setComponentType(ExcavatorComponentEnum.BOOM);
                        component.setDirectionType(null);
                        boomComponent = component;
                    }

                    getInput().getComponent().add(boomComponent);
                    setBoomEditorVisible(true);
                    populateBoomComponentEditor();
                } else {
                    setBoomEditorVisible(false);
                }

                requestValidationAndFireDirtyEvent(getCondition(), componentSelectedValidation);
            }
        });

        MoveEvaluationType.makeOptionsFor(boomSelect);
        boomSelect.addValueChangeHandler(new ValueChangeHandler<MoveEvaluationType>() {

            @Override
            public void onValueChange(ValueChangeEvent<MoveEvaluationType> event) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("boomSelect.onValueChange(" + event.getValue() + ")");
                }

                if (boomComponent == null) {
                    return;
                }

                switch (event.getValue()) {
                case ANGLE_IN_ANY_DIRECTION:
                    boomComponent.setDirectionType(Double.valueOf(0));
                    boomEditor.showAnySpecificDirectionPanel();
                    boomEditor.getAnyAngleBox().setValue(Double.valueOf(0));
                    break;
                case ANGLE_IN_BOTH_DIRECTIONS:
                    Bidirectional direction = new Bidirectional();
                    direction.setPositiveRotation(Double.valueOf(0));
                    direction.setNegativeRotation(Double.valueOf(0));
                    boomComponent.setDirectionType(direction);
                    boomEditor.showBiDirectionPanel();
                    boomEditor.getForwardAngleBox().setValue(direction.getPositiveRotation());
                    boomEditor.getBackwardAngleBox().setValue(direction.getNegativeRotation());
                    break;
                case IN_ANY_DIRECTION:
                    boomComponent.setDirectionType(null);
                    boomEditor.showAnyDirectionPanel();
                    break;
                }
            }
        });

        // Arm editor
        useArmCheck.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {

                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("useArmCheck.onValueChange(" + event.getValue() + ")");
                }

                List<Component> removeList = new ArrayList<Component>();

                Iterator<Component> itr = getInput().getComponent().iterator();
                while (itr.hasNext()) {
                    Component component = itr.next();
                    if (component.getComponentType() != null) {
                        if (component.getComponentType().equals(ExcavatorComponentEnum.ARM)) {
                            removeList.add(component);
                        }
                    }
                }

                for (Component arm : removeList) {
                    getInput().getComponent().remove(arm);
                }

                if (event.getValue()) {
                    if (armComponent == null) {
                        Component component = new Component();
                        component.setComponentType(ExcavatorComponentEnum.ARM);
                        component.setDirectionType(null);
                        armComponent = component;
                    }

                    getInput().getComponent().add(armComponent);
                    setArmEditorVisible(true);
                    populateArmComponentEditor();
                } else {
                    setArmEditorVisible(false);
                }

                requestValidationAndFireDirtyEvent(getCondition(), componentSelectedValidation);
            }
        });

        MoveEvaluationType.makeOptionsFor(armSelect);
        armSelect.addValueChangeHandler(new ValueChangeHandler<MoveEvaluationType>() {

            @Override
            public void onValueChange(ValueChangeEvent<MoveEvaluationType> event) {

                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("armSelect.onValueChange(" + event.getValue() + ")");
                }

                if (armComponent == null) {
                    return;
                }

                switch (event.getValue()) {
                case ANGLE_IN_ANY_DIRECTION:
                    armComponent.setDirectionType(Double.valueOf(0));
                    armEditor.showAnySpecificDirectionPanel();
                    armEditor.getAnyAngleBox().setValue(Double.valueOf(0));
                    break;
                case ANGLE_IN_BOTH_DIRECTIONS:
                    Bidirectional direction = new Bidirectional();
                    direction.setPositiveRotation(Double.valueOf(0));
                    direction.setNegativeRotation(Double.valueOf(0));

                    armComponent.setDirectionType(direction);

                    armEditor.showBiDirectionPanel();
                    armEditor.getForwardAngleBox().setValue(direction.getPositiveRotation());
                    armEditor.getBackwardAngleBox().setValue(direction.getNegativeRotation());
                    break;
                case IN_ANY_DIRECTION:
                    armComponent.setDirectionType(null);
                    armEditor.showAnyDirectionPanel();
                    break;
                }
            }
        });

        // Swing editor

        useSwingCheck.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("useSwingCheck.onValueChange(" + event.getValue() + ")");
                }

                List<Component> removeList = new ArrayList<Component>();

                Iterator<Component> itr = getInput().getComponent().iterator();
                while (itr.hasNext()) {
                    Component component = itr.next();

                    if (component.getComponentType() != null) {
                        if (component.getComponentType().equals(ExcavatorComponentEnum.SWING)) {
                            removeList.add(component);
                        }
                    }
                }

                for (Component swing : removeList) {
                    getInput().getComponent().remove(swing);
                }

                if (event.getValue()) {
                    if (swingComponent == null) {
                        Component component = new Component();
                        component.setComponentType(ExcavatorComponentEnum.SWING);
                        component.setDirectionType(null);
                        swingComponent = component;
                    }

                    getInput().getComponent().add(swingComponent);
                    setSwingEditorVisible(true);
                    populateSwingComponentEditor();
                } else {
                    setSwingEditorVisible(false);
                }

                requestValidationAndFireDirtyEvent(getCondition(), componentSelectedValidation);
            }
        });

        MoveEvaluationType.makeOptionsFor(swingSelect);
        swingSelect.addValueChangeHandler(new ValueChangeHandler<MoveEvaluationType>() {

            @Override
            public void onValueChange(ValueChangeEvent<MoveEvaluationType> event) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("swingSelect.onValueChange(" + event.getValue() + ")");
                }

                if (swingComponent == null) {
                    return;
                }

                switch (event.getValue()) {
                case ANGLE_IN_ANY_DIRECTION:
                    swingComponent.setDirectionType(Double.valueOf(0));
                    swingEditor.showAnySpecificDirectionPanel();
                    swingEditor.getAnyAngleBox().setValue(Double.valueOf(0));
                    break;
                case ANGLE_IN_BOTH_DIRECTIONS:
                    Bidirectional direction = new Bidirectional();
                    direction.setPositiveRotation(Double.valueOf(0));
                    direction.setNegativeRotation(Double.valueOf(0));

                    swingComponent.setDirectionType(direction);

                    swingEditor.showBiDirectionPanel();
                    swingEditor.getForwardAngleBox().setValue(direction.getPositiveRotation());
                    swingEditor.getBackwardAngleBox().setValue(direction.getNegativeRotation());
                    break;
                case IN_ANY_DIRECTION:
                    swingComponent.setDirectionType(null);
                    swingEditor.showAnyDirectionPanel();
                    break;
                }
            }
        });

        maxAssessmentsBox.addValueChangeHandler(new ValueChangeHandler<Integer>() {

            @Override
            public void onValueChange(ValueChangeEvent<Integer> event) {

                if (event.getValue() == null) {
                    getInput().setMaxAssessments(null);
                } else {
                    try {
                        getInput().setMaxAssessments(BigInteger.valueOf(event.getValue()));
                    } catch (@SuppressWarnings("unused") NumberFormatException e) {
                        if (getInput().getMaxAssessments() != null) {
                            maxAssessmentsBox.setValue(getInput().getMaxAssessments().intValue());
                        } else {
                            maxAssessmentsBox.setValue(null);
                        }
                    }
                }
            }
        });

        maxAssessmentsCheckbox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                maxAssessmentsBox.setVisible(event.getValue());
                if (event.getValue()) {
                    BigInteger value = BigInteger.valueOf(maxAssessmentsBox.getValue());
                    getInput().setMaxAssessments(value);
                } else {
                    getInput().setMaxAssessments(null);
                }
            }
        });
    }

    /**
     * Sets the bucket editor visibility.
     * 
     * @param visible true to make visible; false to hide.
     */
    public void setBucketEditorVisible(boolean visible) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("setBucketEditorVisible(" + visible + ")");
        }

        bucketSelect.setVisible(visible);
        bucketEditor.setActive(visible);

        if (visible) {
            bucketDeck.showWidget(bucketDeck.getWidgetIndex(bucketEditor));

        } else {
            bucketEditor.clearValidations();
            bucketDeck.showWidget(bucketDeck.getWidgetIndex(noBucketEditor));
        }
    }

    /**
     * Sets the boom editor visibility.
     * 
     * @param visible true to make visible; false to hide.
     */
    public void setBoomEditorVisible(boolean visible) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("setBoomEditorVisible(" + visible + ")");
        }

        boomSelect.setVisible(visible);
        boomEditor.setActive(visible);

        if (visible) {
            boomDeck.showWidget(boomDeck.getWidgetIndex(boomEditor));

        } else {
            boomEditor.clearValidations();
            boomDeck.showWidget(boomDeck.getWidgetIndex(noBoomEditor));
        }
    }

    /**
     * Sets the arm editor visibility.
     * 
     * @param visible true to make visible; false to hide.
     */
    public void setArmEditorVisible(boolean visible) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("setArmEditorVisible(" + visible + ")");
        }

        armSelect.setVisible(visible);
        armEditor.setActive(visible);

        if (visible) {
            armDeck.showWidget(armDeck.getWidgetIndex(armEditor));
        } else {
            armEditor.clearValidations();
            armDeck.showWidget(armDeck.getWidgetIndex(noArmEditor));
        }
    }

    /**
     * Sets the swing editor visibility.
     * 
     * @param visible true to make visible; false to hide.
     */
    public void setSwingEditorVisible(boolean visible) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("setSwingEditorVisible(" + visible + ")");
        }

        swingSelect.setVisible(visible);
        swingEditor.setActive(visible);

        if (visible) {
            swingDeck.showWidget(swingDeck.getWidgetIndex(swingEditor));
        } else {
            swingEditor.clearValidations();
            swingDeck.showWidget(swingDeck.getWidgetIndex(noSwingEditor));
        }
    }

    @Override
    protected void onEdit() {
        isPopulating = true;
        if (getInput() != null) {

            bucketComponent = null;
            boomComponent = null;
            armComponent = null;
            swingComponent = null;

            for (Component component : getInput().getComponent()) {
                final ExcavatorComponentEnum componentType = component.getComponentType();
                if (componentType == null) {
                    continue;
                }

                MoveEvaluationType moveType = null;
                if (component.getDirectionType() == null) {
                    moveType = MoveEvaluationType.IN_ANY_DIRECTION;
                } else if (component.getDirectionType() instanceof Double) {
                    moveType = MoveEvaluationType.ANGLE_IN_ANY_DIRECTION;
                } else if (component.getDirectionType() instanceof Bidirectional) {
                    moveType = MoveEvaluationType.ANGLE_IN_BOTH_DIRECTIONS;
                }

                if (bucketComponent == null && componentType.equals(ExcavatorComponentEnum.BUCKET)) {
                    bucketComponent = component;
                    useBucketCheck.setValue(true);
                    bucketSelect.setValue(moveType);
                    setBucketEditorVisible(true);
                    populateBucketComponentEditor();
                }

                if (boomComponent == null && componentType.equals(ExcavatorComponentEnum.BOOM)) {
                    boomComponent = component;
                    useBoomCheck.setValue(true);
                    boomSelect.setValue(moveType);
                    setBoomEditorVisible(true);
                    populateBoomComponentEditor();
                }

                if (armComponent == null && componentType.equals(ExcavatorComponentEnum.ARM)) {
                    armComponent = component;
                    useArmCheck.setValue(true);
                    armSelect.setValue(moveType);
                    setArmEditorVisible(true);
                    populateArmComponentEditor();
                }

                if (swingComponent == null && componentType.equals(ExcavatorComponentEnum.SWING)) {
                    swingComponent = component;
                    useSwingCheck.setValue(true);
                    swingSelect.setValue(moveType);
                    setSwingEditorVisible(true);
                    populateSwingComponentEditor();
                }
            }

            if (bucketComponent == null) {

                // if no components exists, add one to make the condition valid
                if (boomComponent == null && armComponent == null && swingComponent == null) {

                    Component component = new Component();
                    component.setComponentType(ExcavatorComponentEnum.BUCKET);
                    component.setDirectionType(null);

                    bucketComponent = component;

                    getInput().getComponent().add(bucketComponent);

                    useBucketCheck.setValue(true);
                    setBucketEditorVisible(true);

                    populateBucketComponentEditor();

                } else {
                    useBucketCheck.setValue(false);
                    setBucketEditorVisible(false);
                }
            }

            if (boomComponent == null) {
                useBoomCheck.setValue(false);
                setBoomEditorVisible(false);
            }

            if (armComponent == null) {
                useArmCheck.setValue(false);
                setArmEditorVisible(false);
            }

            if (swingComponent == null) {
                useSwingCheck.setValue(false);
                setSwingEditorVisible(false);
            }

            /* Populates the max assessments controls */
            boolean hasMaxAssessments = getInput().getMaxAssessments() != null;
            maxAssessmentsCheckbox.setValue(hasMaxAssessments);
            maxAssessmentsBox.setVisible(hasMaxAssessments);
            if (hasMaxAssessments) {
                maxAssessmentsBox.setValue(getInput().getMaxAssessments().intValue());
            }
        }
        isPopulating = false;
    }

    /** Populates the bucket editor */
    private void populateBucketComponentEditor() {
        if (bucketComponent == null) {
            return;
        }

        bucketEditor.edit(bucketComponent, getCondition());

        if (!isPopulating) {
            bucketEditor.validateAllAndFireDirtyEvent(getCondition());
        }
    }

    /** Populates the boom editor */
    private void populateBoomComponentEditor() {
        if (boomComponent == null) {
            return;
        }

        boomEditor.edit(boomComponent, getCondition());

        if (!isPopulating) {
            boomEditor.validateAllAndFireDirtyEvent(getCondition());
        }
    }

    /** Populates the arm editor */
    private void populateArmComponentEditor() {
        if (armComponent == null) {
            return;
        }

        armEditor.edit(armComponent, getCondition());

        if (!isPopulating) {
            armEditor.validateAllAndFireDirtyEvent(getCondition());
        }
    }

    /** Populates the swing editor */
    private void populateSwingComponentEditor() {
        if (swingComponent == null) {
            return;
        }

        swingEditor.edit(swingComponent, getCondition());

        if (!isPopulating) {
            swingEditor.validateAllAndFireDirtyEvent(getCondition());
        }
    }

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        validationStatuses.add(componentSelectedValidation);
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        if (componentSelectedValidation.equals(validationStatus)) {
            componentSelectedValidation.setValidity(useBucketCheck.getValue() || useBoomCheck.getValue()
                    || useArmCheck.getValue() || useSwingCheck.getValue());
        }
    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
        childValidationComposites.add(bucketEditor);
        childValidationComposites.add(boomEditor);
        childValidationComposites.add(armEditor);
        childValidationComposites.add(swingEditor);
    }

    @Override
    protected void setReadonly(boolean isReadonly) {
        useBucketCheck.setEnabled(!isReadonly);
        bucketSelect.setEnabled(!isReadonly);
        bucketEditor.setReadonly(isReadonly);
        useBoomCheck.setEnabled(!isReadonly);
        boomSelect.setEnabled(!isReadonly);
        boomEditor.setReadonly(isReadonly);
        useArmCheck.setEnabled(!isReadonly);
        armSelect.setEnabled(!isReadonly);
        armEditor.setReadonly(isReadonly);
        useSwingCheck.setEnabled(!isReadonly);
        swingSelect.setEnabled(!isReadonly);
        swingEditor.setReadonly(isReadonly);
        maxAssessmentsBox.setEnabled(!isReadonly);
        maxAssessmentsCheckbox.setEnabled(!isReadonly);
    }
}