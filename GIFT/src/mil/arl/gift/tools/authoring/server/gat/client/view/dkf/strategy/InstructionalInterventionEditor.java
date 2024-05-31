/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.strategy;

import java.math.BigDecimal;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.DelayAfterStrategy;
import generated.dkf.Feedback;
import generated.dkf.InstructionalIntervention;
import generated.dkf.StrategyStressCategory;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.strategy.AddFeedbackWidget.RibbonVisibilityChangedHandler;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.validation.ScenarioValidationComposite;

/**
 * An editor used to author instructional interventions
 */
public class InstructionalInterventionEditor extends ScenarioValidationComposite {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(InstructionalInterventionEditor.class.getName());

    /** The ui binder. */
    private static InstructionalInterventionEditorUiBinder uiBinder = GWT
            .create(InstructionalInterventionEditorUiBinder.class);

    /**
     * The Interface InstructionalInterventionEditorUiBinder.
     */
    interface InstructionalInterventionEditorUiBinder extends UiBinder<Widget, InstructionalInterventionEditor> {
    }

    /** The editor used to modify the feedback */
    @UiField
    protected AddFeedbackWidget feedbackEditor;

    /** The {@link InstructionalIntervention} being edited */
    private InstructionalIntervention currentInstructionalIntervention;

    /**
     * Instantiates a new instructional intervention editor.
     */
    public InstructionalInterventionEditor() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("InstructionalInterventionEditor()");
        }

        initWidget(uiBinder.createAndBindUi(this));
    }

    /**
     * Registers a handler to be invoked when the visibility of the
     * {@link #feedbackEditor}'s ribbon has changed.
     *
     * @param handler The {@link RibbonVisibilityChangedHandler} handler that
     *        should be invoked when the visibility of the
     *        {@link #feedbackEditor}'s ribbon has changed. Can't be null.
     */
    public void addRibbonVisibilityChangedHandler(RibbonVisibilityChangedHandler handler) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("addRibbonVisibilityChangedHandler(" + handler + ")");
        }

        feedbackEditor.addRibbonVisibilityChangedHandler(handler);
    }

    /**
     * Forwarding method to the
     * {@link AddFeedbackWidget#isFeedbackTypeSelected()} method.
     *
     * @return True if the user has selected a feedback type using the
     *         {@link #feedbackEditor}, false otherwise.
     */
    public boolean isFeedbackTypeSelected() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("isFeedbackTypeSelected()");
        }

        return feedbackEditor.isFeedbackTypeSelected();
    }

    /**
     * Resets the editor to reprompt the user for the type of {@link Feedback}.
     */
    public void resetEditor() {
        currentInstructionalIntervention = new InstructionalIntervention();
        currentInstructionalIntervention.setFeedback(new Feedback());
        feedbackEditor.populateEditor(currentInstructionalIntervention.getFeedback());
    }

    /**
     * Populates the UI with data from the {@link InstructionalIntervention} type being edited
     *
     * @param instructionalIntervention the type being edited
     */
    public void populateInstructionalIntervention(InstructionalIntervention instructionalIntervention) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("populateInstructionalIntervention(" + instructionalIntervention + ")");
        }

        currentInstructionalIntervention = instructionalIntervention == null ? new InstructionalIntervention()
                : instructionalIntervention;

        Feedback feedback = currentInstructionalIntervention.getFeedback();
        if (feedback == null) {
            feedback = new Feedback();
            currentInstructionalIntervention.setFeedback(feedback);
        }

        feedbackEditor.populateEditor(feedback);

        DelayAfterStrategy delayAfterStrategy = currentInstructionalIntervention.getDelayAfterStrategy();
        if (delayAfterStrategy != null && delayAfterStrategy.getDuration() != null) {
            feedbackEditor.setDelay(delayAfterStrategy.getDuration().intValue());
        }
        
        feedbackEditor.setStressCategory(currentInstructionalIntervention.getStressCategory());
    }

    /**
     * Gets the instructional intervention being edited
     *
     * @return the instructional intervention being edited
     */
    public InstructionalIntervention getInstructionalIntervention() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("getInstructionalIntervention()");
        }

        currentInstructionalIntervention.setFeedback(feedbackEditor.getFeedback());

        /* If a delay has been defined, save it to the instructional
         * intervention */
        Integer delayAmount = feedbackEditor.getDelay();
        if (delayAmount != null) {
            DelayAfterStrategy delay = new DelayAfterStrategy();
            delay.setDuration(new BigDecimal(delayAmount));
            currentInstructionalIntervention.setDelayAfterStrategy(delay);
        } else {
            currentInstructionalIntervention.setDelayAfterStrategy(null);
        }
        
        /* If a stress category was defined, save it to the instructional intervention */
        StrategyStressCategory category = feedbackEditor.getStressCategory();
        currentInstructionalIntervention.setStressCategory(category);

        return currentInstructionalIntervention;
    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
        childValidationComposites.add(feedbackEditor);
    }

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        /* There are no ValidationStatuses on this ValidationComposite. There
         * are only ValidationStatuses on its children. */
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        /* Since there are no ValidationStatuses on this ValidationComposite.
         * There is nothing that needs to be validated */
    }

    /**
     * Sets the components to read only mode which prevents users from making changes.
     *
     * @param isReadonly True to prevent editing, false to allow editing.
     */
    public void setReadonly(boolean isReadonly) {
        feedbackEditor.setReadonly(isReadonly);
    }
}