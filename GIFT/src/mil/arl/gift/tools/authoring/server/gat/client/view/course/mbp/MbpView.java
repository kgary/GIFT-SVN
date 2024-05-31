/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.course.mbp;

import java.math.BigInteger;
import java.util.List;

import org.gwtbootstrap3.client.ui.Button;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.HasHTML;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HasVisibility;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.view.client.HasData;

import generated.course.ConceptQuestions;
import generated.course.ConceptQuestions.AssessmentRules;
import generated.metadata.Concept;
import mil.arl.gift.common.enums.MerrillQuadrantEnum;
import mil.arl.gift.common.enums.MetadataAttributeEnum;
import mil.arl.gift.common.gwt.client.widgets.BlockerPanel;
import mil.arl.gift.common.metadata.MetadataSearchResult.QuadrantResultSet;
import mil.arl.gift.common.metadata.MetadataWrapper;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.guidance.OptionalGuidanceCreator;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.survey.KnowledgeAssessmentSlider;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.survey.PresentSurveyView.MandatoryBehaviorOptionChoice;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.ta.TrainingAppInteropEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.SurveyPickerQuestionBank;
import mil.arl.gift.tools.authoring.server.gat.shared.model.course.PracticeApplicationObject;

public interface MbpView extends IsWidget, IsSerializable{
	
	/** List box choice for a simple reference data */
	static final String SIMPLE_REF_CHOICE = "Content File";
	
	/** List box choice for web resource data */
	static final String URL_CHOICE = "Web Resource";
	
	/**
	 * The Interface Presenter.
	 */
	public interface Presenter{

		/**
		 * Start.
		 */
		void start();
		
		/**
		 * Stop.
		 */
		void stop();
	}

	/**
	 * Return the HasValue interface for the component that allows the user
	 * to select the course concepts to teach in an adaptive courseflow course object.
	 * 
	 * @return the interface for getting the selected course concepts
	 */
	public HasValue<List<String>> getCourseConceptsSelectedConcepts();
	
	/**
     * Return the HasValue interface for the component that allows the user
     * to select the course concepts to teach in the practice phase of an 
     * adaptive courseflow course object.
     * 
     * @return the interface for getting the selected course concepts in a
     * practice phase
     */
	public HasValue<List<String>> getCourseConceptsSelectedPracticeConcepts();
	
	/**
	 * Set the complete list of course concepts available to be selected.
	 * 
	 * @param courseConcepts collection of course concept names.  Can be empty as an indication
	 * that there are no course concepts authored yet.  Can't be null.
	 * @param checkOnLearningConcepts collection of concept names used for the Rule/Example/Recall phases.  Can be empty as an indication
	 * that there are no check on learner concepts selected yet.   Can't be null.
	 * @param practiceConcepts collection of concept names used for Practice phase.  Can be empty as an indication that
	 * there are is no practice phase.   Can't be null.
	 */
	public void setCourseConcepts(List<String> courseConcepts, List<String> checkOnLearningConcepts, List<String> practiceConcepts);
	
	/**
	 * Return the HasVisiblity interface for the component that contains the component which
	 * allows the user to select the course concepts to teach in an adaptive courseflow course object.
	 * This is useful for hiding the course concept selection component (e.g. Read only mode).
	 * 
	 * @return the interface for setting the visibility of the parent to the course concept selection component
	 */
	public HasVisibility getSelectedConceptsPanel();
	
	/**
	 * Return the block panel used to block the removal of selected course concepts in the
	 * 'tagsinput' component.
	 * 
	 * @return the blocker panel that can be used to block the removal of selected course concepts (e.g. Read only mode)
	 */
	public BlockerPanel getSelectedConceptsTagBlockerPanel();
    
    /**
     * Return the HasVisiblity interface for the component that contains the component which
     * allows the user to select the course concepts to teach in practice in an adaptive courseflow course object.
     * This is useful for hiding the course concept selection component (e.g. Read only mode).
     * 
     * @return the interface for setting the visibility of the parent to the course concept selection component for practice
     */
    public HasVisibility getSelectedPracticeConceptsPanel();
    
    /**
     * Update the inherited concepts that should be covered in the practice phase using the new
     * set of selected check on learner phase concepts collection provided.
     * 
     * @param checkOnLearningConcepts the current Rule/Example/Recall phase concepts to cover.  Can be empty.
     */
    public void updatePracticeConcepts(List<String> checkOnLearningConcepts);
	
	/**
	 * Gets the interface for the 'Questions:' table
	 * 
	 * @return the interface for the 'Questions:' table
	 */
	public HasData<ConceptQuestions> getQuestionCellTable();
	
	/**
	 * Sets the field updater for the checkboxes in the 'Concepts:' table
	 * 
	 * @param updater the field updater to assign
	 */
	public void setConceptSelectionColumnFieldUpdater(
			FieldUpdater<CandidateConcept, Boolean> updater);
		
	/**
	 * Sets the field updater for the 'Easy' column in the 'Questions:' table
	 * 
	 * @param updater the field updater to assign
	 */
	public void setEasyColumnFieldUpdater(
			FieldUpdater<ConceptQuestions, String> updater);

	/**
	 * Sets the field updater for the 'Medium' column in the 'Question:' table
	 * 
	 * @param updater the field updater to assign
	 */
	public void setMediumColumnFieldUpdater(
			FieldUpdater<ConceptQuestions, String> updater);

	/**
	 * Sets the field updater for the 'Hard' column in the 'Questions:' table
	 * 
	 * @param updater the field updater to assign
	 */
	public void setHardColumnFieldUpdater(
			FieldUpdater<ConceptQuestions, String> updater);

	/**
	 * Adds a slider to the view. The slider allows the user to adjust the novice, journeyman, and expert criteria.
	 * 
	 * @param conceptName The concept to associate the slider with
	 * @return The created slider widget or null if nothing was created
	 */
	KnowledgeAssessmentSlider appendSlider(String conceptName);

	/**
     * Adds a slider representing an extraneous concept to the view.
     * 
     * @param conceptName The name of the concept
     * @param totalQuestions The total number of questions available.
     * @param assessmentRules The assessment rules to update the slider with
	 * @param removeCmd The command to execute if the extraneous slider is removed from the view
     */
    void appendExtraneousSlider(final String conceptName, int totalQuestions, AssessmentRules assessmentRules, ScheduledCommand removeCmd);
	
	/**
	 * Removes a slider from the view.
	 * 
	 * @param conceptName The name of the concept associated with the slider.
	 */
	void removeSlider(String conceptName);

	/**
	 * Updates the slider.
	 * 
	 * @param conceptName The name of the concept associated with the slider.
	 * @param totalQuestions The total number of easy, medium, & hard questions
	 * @param assessmentRules The concept question's assessment rules. Used to adjust the slider values.
	 * @return The slider widget. Can be null if there is no slider widget for this concept.
	 */
	KnowledgeAssessmentSlider updateSlider(String conceptName, int totalQuestions, AssessmentRules assessmentRules);

	/**
	 * Refreshes the list of knowledge assessment sliders
	 * 
	 * @param conceptList The list of selected concepts
	 */
	void refreshSliderPanel(List<CandidateConcept> conceptList);
	
	/**
	 * Redraws the 'Questions:' table, updating the view with the latest data.
	 */
	public void redrawQuestionsCellTable();
	
	/**
	 * Undoes active changes to the given ConceptQuestions in the 'Questions:' table. This can be used to repopulate the table with the 
	 * current state of the underlying data if they ever get out of sync (e.g. if an input field fails validation, preventing an update).
	 * 
	 * @param question the ConceptQuestions to undo changes on
	 */
	public void undoQuestionsTableChanges(ConceptQuestions question);

	/**
	 * Displays the 'Add Content' dialog
	 */
	public void showAddContentDialog();
	
	/**
	 * Gets the 'Add Content' button for the Rule phase
	 * 
	 * @return the 'Add Content' button for the Rule phase
	 */
	public HasClickHandlers getAddRuleContentButton();
	
    /**
     * Gets the 'Add Content' button for the Rule phase
     * 
     * @return the 'Add Content' button for the Rule phase
     */
	HasVisibility getAddRuleContentButtonHasVisibility();
	
	/**
	 * Gets the 'Add Content' button for the Example phase
	 * 
	 * @return the 'Add Content' button for the Example phase
	 */
	public HasClickHandlers getAddExampleContentButton();
	
    /**
     * Gets the 'Add Content' button for the Example phase
     * 
     * @return the 'Add Content' button for the Example phase
     */
	HasVisibility getAddExampleContentButtonHasVisibility();

	/**
	 * Gets the 'Concepts:' table in the 'Add Content' dialog
	 * 
	 * @return the 'Concepts:' table
	 */
	public HasData<CandidateConcept> getContentConceptsTable();

	/**
	 * Sets a field updater to handle changes to the check boxes in the 'Concepts:' table in the 'Add Content' dialog
	 * 
	 * @param updater the updater
	 */
	public void setContentConceptSelectionColumnFieldUpdater(
			FieldUpdater<CandidateConcept, Boolean> updater);

	/**
	 * Gets the 'Attributes:' table in the 'Add Content' dialog
	 * 
	 * @return the 'Attributes:' table
	 */
	HasData<CandidateMetadataAttribute> getContentAttributesTable();

	/**
	 * Sets a field updater to handle changes to the check boxes in the 'Attributes:' table in the 'Add Content' dialog
	 * 
	 * @param updater the updater
	 */
	void setContentAttributeSelectionColumnFieldUpdater(
			FieldUpdater<CandidateMetadataAttribute, Boolean> updater);

	/**
	 * Gets the 'Add Application' button
	 * 
	 * @return the 'Add Application' button
	 */
	public HasClickHandlers getAddApplicationButton();
	
	/**
     * Gets the 'Add Application' button
     * 
     * @return the 'Add Application' button
     */
	HasVisibility getAddApplicationButtonHasVisibility();

	/**
	 * Hides the 'Add Content' dialog
	 */
	void hideAddContentDialog();
	
	/**
	 * Sets the title for the content dialog
	 * 
	 * @param text the text to use for the title
	 */
	void setContentDialogTitle(String text);
	
	/**
	 * Gets the check box used to show and hide the Practice Phase panel
	 * 
	 * @return the check box used to show and hide the Practice Phase panel
	 */
	HasValue<Boolean> getShowPracticePanelCheckBox();
	
	/**
     * Gets the check box used to show and hide the Practice Phase panel
     * 
     * @return the check box used to show and hide the Practice Phase panel
     */
	HasEnabled getShowPracticePanelCheckboxHasEnabled();

	/**
	 * Shows the 'Add Application' Dialog
	 */
	void showAddApplicationDialog();

	/**
	 * Hides the 'Add Application' Dialog
	 */
	void hideAddApplicationDialog();

    /**
     * Gets the training application interop editor fromthe Add Application Dialog
     * 
     * @return the {@link TrainingAppInteropEditor}
     */
    TrainingAppInteropEditor getAddApplicationInteropEditor();

	/**
	 * Sets the field updater for the checkboxes in the 'Concepts:' table in the 'Add Application' dialog
	 * 
	 * @param updater the updater
	 */
	void setApplicationConceptSelectionColumnFieldUpdater(
			FieldUpdater<CandidateConcept, Boolean> updater);

	/**
	 * Gets the interface for the 'Attributes:' table in the 'Add Application' dialog
	 * 
	 * @return the interface for the 'Attributes:' table in the 'Add Application' dialog
	 */
	HasData<CandidateMetadataAttribute> getApplicationAttributesTable();

	/**
	 * Gets the interface for the 'Concepts:' table in the 'Add Application' dialog
	 * 
	 * @return the interface for the 'Concepts:' table in the 'Add Application' dialog
	 */
	HasData<CandidateConcept> getApplicationConceptsTable();

	/**
	 * Sets the field updater for the checkboxes in the 'Attributes:' table in the 'Add Application' dialog
	 * 
	 * @param updater the updater
	 */
	void setApplicationAttributeSelectionColumnFieldUpdater(
			FieldUpdater<CandidateMetadataAttribute, Boolean> updater);

	/**
	 * Sets the title of the 'Add Application' dialog
	 * 
	 * @param text the title
	 */
	void setApplicationDialogTitle(String text);

	/**
	 * Gets the 'Add' button from the 'Add Application' Dialog
	 * 
	 * @return the 'Add' button from the 'Add Application' Dialog
	 */
	HasClickHandlers getApplicationAddButton();

	/*
	 * Gets the CheckBox responsible for specifying whether or not to show
	 * the set recall allowed attempts property
	 */
	HasValue<Boolean> getShowRecallAllowedAttemptsCheckBox();
	
	/*
	 * Returns a reference to the show recall allowed attempts checkbox
	 * which allows for enabling and disabling the checkbox
	 */
	HasEnabled getShowRecallAllowedAttemptsCheckBoxHasEnabled();
	
	/*
	 * Specifies whether or not the recall allowed attempts spinner should be visible
	 * or not
	 */
	HasVisibility getRecallAllowedAttemptsPanel();
	
	/*
	 * Gets the spinner for specifying the number of allowed attempts
	 * in the recall phase of the Merill's Branch Point/Adaptive Courseflow
	 */
	HasValue<Integer> getRecallAllowedAttemptsSpinner();
	
	/*
     * Returns a reference to the recall allowed attempts spinner which allows for enabling 
     * or disabling the spinner
     */
	HasEnabled getRecallAllowedAttemptsSpinnerHasEnabled();
	
	/*
	 * Gets the CheckBox responsible for specifying whether or not to show
     * the set practice allowed attempts property
	 */
	HasValue<Boolean> getShowPracticeAllowedAttemptsCheckBox();
	
	/*
     * Returns a reference to the show practice allowed attempts checkbox
     * which allows for enabling and disabling the checkbox
     */
    HasEnabled getShowPracticeAllowedAttemptsCheckBoxHasEnabled();
	
	/*
	 * Specifies whether or not the practice allowed attempts spinner should be visible or not
	 */
	HasVisibility getPracticeAllowedAttemptsPanel();
	
	/*
	 * Gets the spinner for specifying the number of allowed attempts 
	 * in the practice phase of the Merill's Branch Point/Adaptive Courseflow
	 */
	HasValue<Integer> getPracticeAllowedAttemptsSpinner();
	
    /**
     * Gets the disabled input.
     *
     * @return the disabled input
     */
    HasValue<Boolean> getDisabledInput();
    
    /**
     * Gets the disabled input.
     *
     * @return the disabled input
     */
    HasEnabled getDisabledInputHasEnabled();  
    
    
    /**
     * Sets whether or not the mandatory controls are visible within the UI.
     * @param isVisible true if the mandatory controls should be visible, false 
     * if they should not be visible
     */
    void setMandatoryControlsVisibility(boolean isVisible);
    
    /**
     * Gets the checkbox used to set whether or not to mark the survey as mandatory
     * 
     * @return the checkbox used to set whether or not to mark the survey as mandatory
     */
    HasValue<Boolean> getMandatoryCheckBox();
    
    /**
     * Gets the checkbox used to set whether or not to mark the survey as mandatory
     * 
     * @return the checkbox used to set whether or not to mark the survey as mandatory
     */
    HasEnabled getMandatoryCheckBoxHasEnabled();
    
    /**
     * The control that is used to choose between the types of mandatory
     * behavior
     * @return the control that is used to choose the mandatory behavior
     */
    HasValue<MandatoryBehaviorOptionChoice> getMandatoryBehaviorSelector();
    
    /**
     * A reference to the MandatoryBehaviorSelector that allows for
     * disabling and enabling of the control
     * @return the HasEnabled reference
     */
    HasEnabled getMandatoryBehaviorSelectorHasEnabled();
    
    /**
     * A reference to the MandatoryBehaviorSelector that allows for 
     * the hiding and showing of the control
     * @return the HasVisibility reference
     */
    HasVisibility getMandatoryBehaviorSelectorHasVisibility();
    
    /**
     * The control that is used to choose how long a previous learner
     * state should be considered for use when a FixedDecayMandatory behavior 
     * has been selected.
     * @return the control that is used to specify the number of days
     * that the learner state is to remain valid
     */
    HasValue<Integer> getLearnerStateShelfLife();

    /**
     * A reference to the LearnerStateShelfLife control that 
     * allows for disabling and enabling of the control
     * @return the HasEnabled reference
     */
    HasEnabled getLearnerStateShelfLifeHasEnabled();
    
    /**
     * A reference to the LearnerStateShelfLife control that 
     * allows for the hiding and showing of the control
     * @return the HasVisibility reference
     */
    HasVisibility getLearnerStateShelfLifeHasVisibility();
    
    /**
     * Sets whether or not the mbp options panel should be visible
     * 
     * @param visible whether or not the mbp options panel should be visible
     */
    void setMbpOptionsVisible(boolean visible);
	
	/*
	 * Returns a reference to the practice allowed attempts spinner which allows for enabling 
	 * or disabling the spinner
	 */
	HasEnabled getPracticeAllowedAttemptsSpinnerHasEnabled();
	
	/**
	 * Gets the 'Add' button from the 'Add Content' Dialog
	 * 
	 * @return the 'Add' button from the 'Add Content' Dialog
	 */
	HasClickHandlers getContentAddButton();
	

	/**
	 * Gets the text used to display validation errors in the 'Add Application' dialog
	 * 
	 * @return the text used to display validation errors in the 'Add Application' dialog
	 */
	HasHTML getApplicationValidationErrorText();

	/**
	 * Gets the HasEnabled interface for the 'Add' Button in the 'Add Application' dialog
	 * 
	 * @return the HasEnabled interface for the 'Add' Button in the 'Add Application' dialog
	 */
	HasEnabled getApplicationAddButtonEnabled();

	/**
	 * Gets the text used to display validation errors in the 'Add Application' dialog
	 * 
	 * @return the text used to display validation errors in the 'Add Application' dialog
	 */
	HasHTML getContentValidationErrorText();

	/**
	 * Gets the HasEnabled interface for the 'Add' Button in the 'Add Content' dialog
	 * 
	 * @return the HasEnabled interface for the 'Add' Button in the 'Add Content' dialog
	 */
	HasEnabled getContentAddButtonEnabled();

	/**
	 * Gets the HTML for the Rule phase's transitions warning message
	 * 
	 * @return the HTML for the Rule phase's transitions warning message
	 */
	HasHTML getRuleTransitionsWarning();

	/**
	 * Gets the HTML for the Example phase's transitions warning message
	 * 
	 * @return the HTML for the Example phase's transitions warning message
	 */
	HasHTML getExampleTransitionsWarning();

	/**
	 * Gets the HTML for the Recall phase's transitions warning message
	 * 
	 * @return the HTML for the Recall phase's transitions warning message
	 */	
	HasHTML getRecallTransitionsWarning();
	
	/**
	 * A representation of a concept available to be used in a Merrill's Branch Point. This class is only intended to be used in 
	 * displaying data to the user, not modifying the underlying concept.
	 * 
	 * @author nroberts
	 */
	public static class CandidateConcept{
		
		/** The name of the concept represented by this candidate */
		private String conceptName;
		
		/** Whether or not the concept represented by this candidate has been selected for use in the Merrill's Branch Point */
		private boolean isChosen = false;
		
		/** Whether or not the concept should remain in the practice concepts table after being deselected from the MBP concept table. */
		private boolean remainSelected = false;
		
		/**
		 * Creates a candidate for the specified concept.
		 * 
		 * @param conceptName the name of the concept this candidate represents.  Can't be null or empty.
		 * @param isChosen whether or not the candidate has been selected for use in the Merrill's Branch Point
		 */
		public CandidateConcept(String conceptName, boolean isChosen){
			this.setChosen(isChosen);
			
			if(StringUtils.isBlank(conceptName)){
			    throw new IllegalArgumentException("The concept name can't be null or empty.");
			}
			this.conceptName = conceptName;
		}

		/**
		 * Gets whether or not the concept represented by this candidate has been chosen
		 * 
		 * @return  whether or not the concept represented by this candidate has been chosen
		 */
		public boolean isChosen() {
			return isChosen;
		}

		/**
		 * Sets whether or not the concept represented by this candidate has been chosen
		 * 
		 * @param isChosen whether or not the concept represented by this candidate has been chosen
		 */
		public void setChosen(boolean isChosen) {
			this.isChosen = isChosen;
		}

		/**
		 * Gets the name of the concept represented by this candidate
		 * 
		 * @return the name of the concept represented by this candidate
		 */
		public String getConceptName() {
			return conceptName;
		}
		
		/**
		 * Gets whether or not the concept should remain selected in the practice concept table 
		 * after it has been deselected from the mbp concepts table.
		 * 
		 * @return true if the concept should remain selected, false otherwise. 
		 */
		public boolean getRemainSelected() {
			return remainSelected;
		}
		
		/**
		 * Sets whether or not the concept should remain selected in the practice concept table 
		 * after it has been deselected from the mbp concepts table.
		 * 
		 * @param remainSelected whether or not the concept should remain selected. 
		 */
		public void setRemainSelected(boolean remainSelected) {
			this.remainSelected = remainSelected;
		}
		
		@Override
		public boolean equals(Object obj){
		    
		    if(obj instanceof CandidateConcept){
		        CandidateConcept otherCandidate = (CandidateConcept)obj;
		        return conceptName.equalsIgnoreCase(otherCandidate.getConceptName());
		    }
		    
		    return false;
		}
		
		@Override
		public int hashCode(){
		    
		    return 7 * conceptName.hashCode();
		}

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("[CandidateConcept: conceptName=");
            builder.append(conceptName);
            builder.append(", isChosen=");
            builder.append(isChosen);
            builder.append(", remainSelected=");
            builder.append(remainSelected);
            builder.append("]");
            return builder.toString();
        }	
		
	}
	
	/**
	 * A representation of a metadata attribute to be used in course content. This class is only intended to be used in 
	 * displaying data to the user, not modifying the underlying metadata.
	 * 
	 * @author nroberts
	 */
	public static class CandidateMetadataAttribute{
		
		/** The parent concept to which the attribute represented by this candidate belongs */
		private Concept parentConcept;
		
		/** The attribute represented by this candidate */
		private MetadataAttributeEnum attribute;
		
		/** Whether or not attribute represented by this candidate has been selected */
		private boolean isChosen = false;
		
		/**
		 * Creates a candidate for the specified attribute.
		 * 
		 * @param parentConcept the parent concept to which the attribute represented by this candidate belongs
		 * @param attribute the name of the attribute this candidate represents
		 * @param isChosen whether or not the attribute has been selected
		 */
		public CandidateMetadataAttribute(Concept parentConcept, MetadataAttributeEnum attribute, boolean isChosen){
			this.setChosen(isChosen);
			this.attribute = attribute;
			this.parentConcept = parentConcept;
		}

		/**
		 * Gets whether or not the attribute represented by this candidate has been chosen
		 * 
		 * @return  whether or not the attribute represented by this candidate has been chosen
		 */
		public boolean isChosen() {
			return isChosen;
		}

		/**
		 * Sets whether or not the attribute represented by this candidate has been chosen
		 * 
		 * @param isChosen whether or not the attribute represented by this candidate has been chosen
		 */
		public void setChosen(boolean isChosen) {
			this.isChosen = isChosen;
		}

		/**
		 * Gets the name of the attribute represented by this candidate
		 * 
		 * @return the name of the attribute represented by this candidate
		 */
		public MetadataAttributeEnum getAttribute() {
			return attribute;
		}
		
		/** 
		 * Gets the parent concept to which the attribute represented by this candidate belongs
		 * 
		 * @return the parent concept to which the attribute represented by this candidate belongs
		 */
		public Concept getParentConcept(){
			return parentConcept;
		}
		
	}
	
	/**
	 * Interface for handling successful file uploads
	 * 
	 * @author nroberts
	 */
	public static interface FileUploadCallback{
		
		public void onSuccess(String filename);
	}

	/**
	 * Sets whether or not the rule phase warning message is visible
	 * 
	 * @param visible whether or not the rule phase warning message is visible
	 */
	void showRuleWarning(boolean visible);

	/**
	 * Sets whether or not the example phase warning message is visible
	 * 
	 * @param visible whether or not the example phase warning message is visible
	 */
	void showExampleWarning(boolean visible);

	/**
	 * Sets whether or not the recall phase warning message is visible
	 * 
	 * @param visible whether or not the recall phase warning message is visible
	 */
	void showRecallWarning(boolean visible);

	/**
	 * Shows or hides the Practice Phase panel
	 *  
	 * @param visible whether or not the Practice Phase panel should be visible
	 */
	void setPracticePanelVisible(boolean visible);

	/**
	 * Sets the list of rule files
	 * 
     * @param metadataFiles the mapping from each metadata file name, including workspace path, 
     * to an object containing information about that metadata.  Can be null or empty.
	 */
	void setRuleFiles(QuadrantResultSet metadataFiles);
	
	/**
	 * Add the metadata reference to the content list associated with the provided adaptive courseflow phase.</br>
	 * Note: this doesn't handle Practice phase.
	 * 
	 * @param metadataWrapper contains information about the metadata that was successfully just created on the server.  If null,
	 * this method does nothing.
	 * @param phase used to add the metadata reference to the appropriate content list in the panel.  Can't be null.
	 */
	void addContentFile(MetadataWrapper metadataWrapper, MerrillQuadrantEnum phase);

	HasClickHandlers getRuleRefreshButton();
	
	/**
	 * Set whether the rule files panel content list is being loaded.
	 * 
	 * @param loadingContentList true if the content list is currently being loaded into view
	 */
	void setRuleFilePanelLoading(boolean loadingContentList);

	/**
     * Sets the list of example files
     * 
     * @param metadataFiles a mapping from each metadata file name, including workspace path, 
     * to an object containing information about that metadata.  Can be null or empty.
     */
	void setExampleFiles(QuadrantResultSet metadataFiles);

	HasClickHandlers getExampleRefreshButton();

    /**
     * Set whether the example files panel content list is being loaded.
     * 
     * @param loadingContentList true if the content list is currently being loaded into view
     */
    void setExampleFilePanelLoading(boolean loadingContentList);

	TrainingAppInteropEditor getPracticeInteropEditor();

	/**
	 * Sets the path to the course folder.
	 * 
	 * @param coursePath The path to the course folder
	 */
	void setCourseFolderPath(String coursePath);
	
	/**
	 * Accessor to get the survey picker question bank widget.
	 * 
	 * @return SurveyPickerQuestionBank - The survey picker question bank widget.
	 */
	SurveyPickerQuestionBank getSurveyPickerQuestionBank();

	/**
	 * Called when the name box has been updated (contains the name of the mbp transition).
	 * 
	 * @param name - The name of the mbp transition.
	 */
	void onNameBoxUpdated(String name);

	HasClickHandlers getPracticeRefreshButton();

    /**
     * Set whether the practice applications panel content list is being loaded.
     * 
     * @param loadingContentList true if the content list is currently being loaded into view
     */
    void setPracticeApplicationsPanelLoading(boolean loadingContentList);

	/**
     * Sets the list of practice files
     * 
     * @param practiceApplications contains the list of practice applications for the selected course concepts.  
     * These are the required concepts for the adaptive courseflow course object.  Can be null or empty.
     */
	void setPracticeApplications(List<PracticeApplicationObject> practiceApplications);
	
	/**
	 * Remove the practice application content table row associated with the given metadata file path.
	 * 
	 * @param metadataFilePath the course folder path to a metadata file of which to remove the associated row
	 * in the practice application content table.  Can't be null or empty string.
	 * @return true if the row was removed, false if a row wasn't removed
	 */
	boolean removePracticeApplication(String metadataFilePath);

	Column<PracticeApplicationObject, String> getPracticeApplicationDeleteColumn();
	
	void setRefreshMetadataCommand(Command command);
	
	/**
     * Sets the survey context id that the course is using.
     * 
     * @param surveyContextId The surveyContextId of the current course.
     */
	public void setCourseSurveyContextId(BigInteger surveyContextId);

	OptionalGuidanceCreator getRuleGuidanceCreator();

	OptionalGuidanceCreator getExampleGuidanceCreator();

	OptionalGuidanceCreator getRecallGuidanceCreator();

	OptionalGuidanceCreator getPracticeGuidanceCreator();

	Button getChangePracticeApplicationButton();

	ContentReferenceEditor getContentReferenceEditor();

    /**
     * Set whether the remediation files panel content list is being loaded.
     * 
     * @param loadingContentList true if the content list is currently being loaded into view
     */
    void setRemediationFilePanelLoading(boolean loadingContentList);

	HasClickHandlers getRemediationRefreshButton();

	/**
	 * Sets the remediation content files.
	 * 
     * @param metadataFileNameToWrapper a mapping from each metadata file name, including workspace path, 
     * to an object containing information about that metadata.  Can be null or empty.
     */
	void setRemediationFiles(QuadrantResultSet metadataFiles);

	HasVisibility getAddRemediationContentButtonHasVisibility();

	
	HasClickHandlers getAddRemediationContentButton();	

	void showAddRemediationDialog();

	void hideAddRemediationDialog();

	HasData<CandidateConcept> getRemediationConceptsTable();

	void setRemediationConceptSelectionColumnFieldUpdater(FieldUpdater<CandidateConcept, Boolean> updater);

	HasClickHandlers getRemediationAddButton();

	HasEnabled getRemediationAddButtonEnabled();

	HasHTML getRemediationValidationErrorText();
	
	ContentReferenceEditor getRemediationContentReferenceEditor();

	HasData<CandidateMetadataAttribute> getRemediationAttributesTable();

	void setRemediationAttributeSelectionColumnFieldUpdater(FieldUpdater<CandidateMetadataAttribute, Boolean> updater);
	
	HasValue<Boolean> getExcludeRuleExampleContentCheckBox();
	
	/**
     * Returns a reference to the remediation exclude rule/example content CheckBox which allows for enabling 
     * or disabling the spinner
     */
    HasEnabled  getExcludeRuleExampleContentCheckBoxHasEnabled();

    /**
     * Updates the rule/example/remediation content that the author is currently authoring to match the given selection
     * state of the given concept.
     * 
     * Should ideally be fired whenever the user explictly selects or deselects a concept while 
     * authoring a rule/example/remediation content.
     * 
     * @param concept the concept to update in the rule/example/remediation content that the author is currently authoring
     * @param selected the selection state of said concept
     */
    void updateContentOnConceptChange(String concept, boolean selected);
}
