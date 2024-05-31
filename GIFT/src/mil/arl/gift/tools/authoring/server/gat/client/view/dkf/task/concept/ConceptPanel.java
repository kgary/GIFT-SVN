/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.concept;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.Actions.StateTransitions.StateTransition;
import generated.dkf.Concept;
import generated.dkf.Concepts;
import generated.dkf.Condition;
import generated.dkf.Conditions;
import generated.dkf.Strategy;
import mil.arl.gift.common.gwt.client.validation.ModelValidationStatus;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.gwt.client.validation.ValidationWidget;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.shared.AdditionalAssessmentWidget;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.shared.AssessmentRollupWidget;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.shared.CourseConceptDisplayWidget;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.shared.MiscAttributesWidget;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.shared.StateTransitionReferenceWidget;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.ScenarioEventUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.validation.ScenarioValidationComposite;

/**
 * The Class ConceptPanel.
 */
public class ConceptPanel extends ScenarioValidationComposite {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(ConceptPanel.class.getName());

    /** The UiBinder that combines the ui.xml with this java class */
    private static ConceptPanelUiBinder uiBinder = GWT.create(ConceptPanelUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface ConceptPanelUiBinder extends UiBinder<Widget, ConceptPanel> {
    }

    /**
     * The description of the table containing the state transitions that reference this concept
     */
    private static final String STATE_TRANSITION_TABLE_DESCRIPTION = "This concept"
            + " will be used to assess the learner's performance in its parent as they complete it. "
            + "The assessment for this concept will be determined based on the "
            + "assessments that the learner received for any of the conditions covered by this "
            + "concept. <br/><br/> Whenever the assessment for this concept changes, the "
            + "following state transitions will determine what actions should be taken.";

    /** Displays any validation errors on the page */
    @UiField(provided = true)
    protected ValidationWidget validations = new ValidationWidget(this);

    /** Displays any course concept data related to this task, if applicable */
    @UiField
    protected CourseConceptDisplayWidget courseConcept;

    /**
     * A control that shows each {@link StateTransition} that reference {@link #selectedConcept}
     */
    @UiField
    protected StateTransitionReferenceWidget referencedStateTransitions;

    /** The optional additional assessments for the concept */
    @UiField
    protected AdditionalAssessmentWidget additionalAssessments;
    
    /** The optional misc attributes for the concept */
    @UiField
    protected MiscAttributesWidget miscAttributes;
    
    /** An editor used to modify the concept's assessment rollup rules */
    @UiField
    protected AssessmentRollupWidget assessmentRollup;

    /** The concept the {@link ConceptPanel} is editing */
    private Concept selectedConcept;

    /** Message to be displayed when the {@link Task} has no children. */
    private static final String NO_CHILD_VALIDATION_MESSAGE = "This concept needs a condition that will assess the learner (or a sub-concept if you want additional hierarchy).";

    /** Message to be displayed when the {@link Concept} shares an externalSourceId with another. */
    private static final String DUPLICATE_EXTERNAL_SOURCE_MESSAGE = "This concept shares an external source with another condition, but its child conditions do not match. "
    		+ "For proper external export, make sure all concepts with the same external source ID have matching child conditions.";
    
    /** The container for showing validation messages for the concept not having children. */
    private final ModelValidationStatus childValidationStatus = new ModelValidationStatus(NO_CHILD_VALIDATION_MESSAGE) {
        @Override
        protected void fireJumpToEvent(Serializable modelObject) {
            ScenarioEventUtility.fireJumpToEvent(modelObject);
        }
    };
    
    /** The container for showing validation messages for duplicate Concept references to xTSP external sources */
    private final ModelValidationStatus xtspDuplicateReferenceValidationStatus = new ModelValidationStatus(DUPLICATE_EXTERNAL_SOURCE_MESSAGE) {
    	@Override
    	protected void fireJumpToEvent(Serializable modelObject) {
    		ScenarioEventUtility.fireJumpToEvent(modelObject);
    	}
    };
    
    /**
     * Instantiates a new dkf concept panel.
     */
    public ConceptPanel() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(".ctor()");
        }

        initWidget(uiBinder.createAndBindUi(this));

        referencedStateTransitions.setHelpText(STATE_TRANSITION_TABLE_DESCRIPTION);

        // needs to be called last
        initValidationComposite(validations);
    }

    /**
     * Updates the widget to begin editing the passed in concept
     * 
     * @param concept The concept to edit, can't be null.
     */
    public void edit(Concept concept) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("edit(" + concept + ")");
        }

        if (concept == null) {
            throw new IllegalArgumentException("The parameter 'concept' can not be null");
        }

        // load the current survey context into the survey assessment area
        additionalAssessments.loadSurveyContext(ScenarioClientUtility.getSurveyContextId());

        this.selectedConcept = concept;

        referencedStateTransitions.showTransitions(concept);
        additionalAssessments.showAdditionalAssessments(concept);
        courseConcept.load(concept);
        
        miscAttributes.edit(concept);
        assessmentRollup.edit(concept);
    }

    /**
     * Sets the the local {@link Concept} with the new name.
     * 
     * @param newConceptName the new name.
     */
    public void onRename(String newConceptName) {
        selectedConcept.setName(newConceptName);
        additionalAssessments.onRename(selectedConcept);
    }

    /**
     * Returns the node id of the {@link Concept} being edited in this panel
     * 
     * @return the node id of the edited {@link Concept}. Will return null if no {@link Concept} is
     *         being edited.
     */
    public BigInteger getSelectedConceptNodeId() {
        return selectedConcept == null ? null : selectedConcept.getNodeId();
    }

    /**
     * Refreshes the referenced state transitions table and redraws the items in the list.
     */
    public void refreshReferencedStateTransitions() {
        referencedStateTransitions.refresh();
    }

    /**
     * Updates the list of referenced state transitions with the selected task.
     * 
     * @param transition the transition to add or remove from the references table.
     * @param add true to add the transition to the refrences table; false to remove it.
     */
    public void updateReferencedStateTransitions(StateTransition transition, boolean add) {
        if (add) {
            referencedStateTransitions.add(transition);
        } else {
            referencedStateTransitions.remove(transition);
        }
    }

    /**
     * Refreshes the referenced action table and redraws the items in the list.
     */
    public void refreshReferencedActions() {
        additionalAssessments.refreshActionReferences();
    }

    /**
     * Updates the list of referenced strategies with the selected task.
     * 
     * @param strategy the strategy to add or remove from the references table.
     * @param add true to add the strategy to the refrences table; false to remove it.
     */
    public void updateReferencedActions(Strategy strategy, boolean add) {
        if (add) {
            additionalAssessments.add(strategy);
        } else {
            additionalAssessments.remove(strategy);
        }
    }

    /**
     * Handles the deletion of a certain {@link Strategy} by removing it from
     * {@link #additionalAssessments} if it was contained there.
     * 
     * @param strategy The {@link Strategy} to remove if it is found within
     *        {@link #additionalAssessments}. If it isn't, nothing happens.
     */
    public void removeAction(Strategy strategy) {
        additionalAssessments.removeAction(strategy);
    }

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        validationStatuses.add(childValidationStatus);
        
        if(ScenarioClientUtility.getScenario().getResources() != null
                && ScenarioClientUtility.getScenario().getResources().getSourcePath() != null) {
            
            /* If this DKF is based on an XTSP, need additional validation for duplicates */
            validationStatuses.add(xtspDuplicateReferenceValidationStatus);
        }
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        if (childValidationStatus.equals(validationStatus)) {
            // must have a child
            Serializable children = selectedConcept.getConditionsOrConcepts();
            if (children == null || (children instanceof Conditions && ((Conditions) children).getCondition().isEmpty())
                    || (children instanceof Concepts && ((Concepts) children).getConcept().isEmpty())) {
                childValidationStatus.setModelObject(null);
                childValidationStatus.setErrorMessage(NO_CHILD_VALIDATION_MESSAGE);
                childValidationStatus.setAdditionalInstructions(buildNoChildValidationAdditionalInformation());
                childValidationStatus.setInvalid();
            } else if (children instanceof Concepts) {
                Concepts concepts = (Concepts) children;
                Concept invalidConcept = null;
                for (Concept concept : concepts.getConcept()) {
                    if (!ScenarioClientUtility.getValidationCache().isValid(concept)) {
                        invalidConcept = concept;
                        break;
                    }
                }

                if (invalidConcept == null) {
                    childValidationStatus.setValid();
                } else {
                    childValidationStatus.setModelObject(invalidConcept);
                    childValidationStatus.setErrorMessage("The child concept '" + invalidConcept.getName()
                            + "' is invalid. Please resolve the issues within the concept.");
                    childValidationStatus.setAdditionalInstructions(null);
                    childValidationStatus.setInvalid();
                }
            } else if (children instanceof Conditions) {
                Conditions conditions = (Conditions) children;
                Condition invalidCondition = null;
                for (Condition condition : conditions.getCondition()) {
                    if (!ScenarioClientUtility.getValidationCache().isValid(condition)) {
                        invalidCondition = condition;
                        break;
                    }
                }

                if (invalidCondition == null) {
                    childValidationStatus.setValid();
                } else {
                    childValidationStatus.setModelObject(invalidCondition);
                    childValidationStatus.setErrorMessage(
                            "A child condition is invalid. Please resolve the issues within the condition.");
                    childValidationStatus.setAdditionalInstructions(null);
                    childValidationStatus.setInvalid();
                }
            }
        } else if (xtspDuplicateReferenceValidationStatus.equals(validationStatus)) {
        	ScenarioClientUtility.setConceptPanelWarningInCaseOfConflictingExternalSourceDuplicates(selectedConcept, xtspDuplicateReferenceValidationStatus, this);        	
        }
    }
    
    /**
     * A method to allow an source outside this class to call requestValidation. This is a special case,
     * as this class includes a validation check which involves a server call, and thus needs to request
     * validation via an asynchronous callback.
     * @param status The ValidationStatus to send to the requestValidation call.
     */
    public void requestValidationExternally(ValidationStatus status) {
    	requestValidation(status);
    }
    
    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
        childValidationComposites.add(additionalAssessments);
        childValidationComposites.add(assessmentRollup);
    }

    /**
     * Builds the additional information string for the {@link #childValidationStatus}.
     * 
     * @return the string to display to the user to help with adding a {@link Condition} for this {@link Concept}.
     */
    private String buildNoChildValidationAdditionalInformation() {
        StringBuilder sb = new StringBuilder("To add a Condition, click the '+' button next to the Concept");
        if (selectedConcept != null) {
            sb.append(" '").append(selectedConcept.getName()).append("'");
        }
        sb.append(" in the Task list.");
        return sb.toString();
    }
}