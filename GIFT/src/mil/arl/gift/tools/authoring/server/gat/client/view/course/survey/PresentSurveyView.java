/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.course.survey;

import java.math.BigInteger;
import java.util.List;

import org.gwtbootstrap3.client.ui.TextBox;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HasVisibility;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.view.client.HasData;

import generated.course.ConceptQuestions;
import generated.course.ConceptQuestions.AssessmentRules;
import generated.course.Course;
import mil.arl.gift.common.gwt.client.widgets.file.CancelCallback;
import mil.arl.gift.common.gwt.client.widgets.file.FileSelectionView;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.mbp.MbpView.CandidateConcept;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.SurveyPicker;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.SurveyPickerQuestionBank;

public interface PresentSurveyView extends IsWidget, IsSerializable {
    
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
    
    /** AutoTutor option text*/
    public static final String AUTOTUTOR_OPTION_TEXT = "AutoTutor Conversation";
    
    /** GIFT Survey option text*/
    public static final String GIFT_SURVEY_OPTION_TEXT = "GIFT Survey";

    /** Conversation Tree text*/
    public static final String CONVERSATION_TREE_OPTION_TEXT = "Conversation Tree";
    
    /**
     * Options for survey type list box
     * 
     * @author nroberts
     */
    public static enum SurveyTypeEditingMode{
        AUTOTUTOR,
        GIFT_SURVEY, 
        CONVERSATION_TREE,
        QUESTION_BANK;
    }
    
    /**
     * Editing modes for GIFT survty types
     * 
     * @author nroberts
     */
    public static enum GIFTSurveyTypeEditingMode{
        SURVEY_CONTEXT_SURVEY,
        KNOWLEDGE_ASSESSMENT_SURVEY;
    }
    
    /**
     * Editing modes for autotutor session types
     * 
     * @author apearigen
     */
    public static enum GIFTAutotutorSessionTypeEditingMode{
        SKO,
        DKF;
    }
    
    public static enum MandatoryBehaviorOptionChoice {
        ALWAYS,
        AFTER
    }

    /**
     * Sets the current survey type editing mode
     * 
     * @param mode the editing mode to set
     */
    void setSurveyTypeEditingMode(SurveyTypeEditingMode mode);
    
    /**
     * Gets the interface for the 'Concepts:' table
     * 
     * @return the interface for the 'Concepts:' table
     */
    HasData<CandidateConcept> getConceptCellTable();

    /**
     * Gets the interface for the 'Questions:' table
     * 
     * @return the interface for the 'Questions:' table
     */
    HasData<ConceptQuestions> getQuestionCellTable();

    /**
     * Sets the field updater for the checkboxes in the 'Concepts:' table
     * 
     * @param updater the field updater to assign
     */
    void setConceptSelectionColumnFieldUpdater(
            FieldUpdater<CandidateConcept, Boolean> updater);

    /**
     * Sets the field updater for the 'Easy' column in the 'Questions:' table
     * 
     * @param updater the field updater to assign
     */
    void setEasyColumnFieldUpdater(
            FieldUpdater<ConceptQuestions, String> updater);

    /**
     * Sets the field updater for the 'Medium' column in the 'Question:' table
     * 
     * @param updater the field updater to assign
     */
    void setMediumColumnFieldUpdater(
            FieldUpdater<ConceptQuestions, String> updater);

    /**
     * Sets the field updater for the 'Hard' column in the 'Questions:' table
     * 
     * @param updater the field updater to assign
     */
    void setHardColumnFieldUpdater(
            FieldUpdater<ConceptQuestions, String> updater);

    /**
     * Redraws the 'Questions:' table, updating the view with the latest data.
     */
    void redrawQuestionsCellTable();

    /**
     * Adds a slider to the view. The slider allows the user to adjust the novice, journeyman, and expert criteria.
     * 
     * @param conceptName The concept to associate the slider with
     * @return The created slider widget or null if nothing was created
     */
    KnowledgeAssessmentSlider appendSlider(String conceptName);
    
    /**
     * Adds a slider to the view. The slider is associated with an extraneous concept and can be removed but not edited.
     * 
     * @param conceptName The concept to associate the slider with
     * @param totalQuestions The total number of easy, medium, & hard questions
     * @param assessmentRules The concept question's assessment rules. Used to adjust the slider values.
     * @param removeCmd The command to execute when the extraneous concept is removed from the view.
     */
    void appendExtraneousSlider(String conceptName, int totalQuestions, AssessmentRules assessmentRules, ScheduledCommand removeCmd);

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
     * @return the slider widget or null if there is no slider associated  with this concept.
     */
    KnowledgeAssessmentSlider updateSlider(String conceptName, int totalQuestions, AssessmentRules assessmentRules);

    /**
     * Refreshes the list of knowledge assessment sliders
     * 
     * @param conceptList The list of selected concepts
     */
    void refreshSliderPanel(List<CandidateConcept> conceptList);
    
    /**
     * Undoes active changes to the given ConceptQuestions in the 'Questions:' table. This can be used to repopulate the table with the 
     * current state of the underlying data if they ever get out of sync (e.g. if an input field fails validation, preventing an update).
     * 
     * @param question the ConceptQuestions to undo changes on
     */
    void undoQuestionsTableChanges(ConceptQuestions question);

    /**
     * Gets the dialog used to select a DKF file
     * 
     * @return the dialog used to select a DKF file
     */
    HasValue<String> getFileSelectionDialog();

    /**
     * Sets whether or not the file selection dialog should be visible
     * 
     * @param visible  whether or not the file selection dialog should be visible
     */
    void setFileSelectionDialogVisible(boolean visible);

    /**
     * Gets the checkbox used to set whether or not to use full screen
     * 
     * @return the checkbox used to set whether or not to use full screen
     */
    HasValue<Boolean> getFullScreenCheckBox();
    
    /**
     * Gets the checkbox used to set whether or not to use full screen
     * 
     * @return the checkbox used to set whether or not to use full screen
     */
    HasEnabled getFullScreenCheckBoxHasEnabled();
    
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
     * Gets the checkbox used to set whether or not to show responses in an AAR
     * 
     * @return the checkbox used to set whether or not to show responses in an
     *         AAR
     */
    HasValue<Boolean> getShowResponsesCheckBox();
    
    /**
     * Gets the checkbox used to set whether or not to show responses in an AAR
     * 
     * @return the checkbox used to set whether or not to show responses in an AAR
     */
    HasEnabled getShowResponsesCheckboxHasEnabled();
    
    /**
     * Set the visibility of the components responsible for the show responses element.
     * 
     * @param visible true if the UI elements should be visible for this property
     */
    void setShowResponsesVisibility(boolean visible);

    /**
     * Gets the checkbox used to set whether or not to use Knowledge Assesment results to influence course flow
     * 
     * @return the checkbox used to set whether or not to use Knowledge Assesment results to influence course flow
     */
    HasValue<Boolean> getUseResultsCheckBox();
    
    /**
     * Gets the checkbox used to set whether or not to use Knowledge Assesment results to influence course flow
     * 
     * @return the checkbox used to set whether or not to use Knowledge Assesment results to influence course flow
     */
    HasEnabled getUseResultsCheckboxHasEnabled();

    /**
     * Sets whether or not the survey options panel should be visible
     * 
     * @param visible whether or not the survey options panel should be visible
     */
    void setSurveyOptionsVisible(boolean visible);

    /**
     * Sets whether or not the knowledge assessment panel should be visible
     * 
     * @param visible whether or not the knowledge assessment panel should be visible
     */
    void setKnowledgeAssessmentOptionsVisible(boolean visible);

    /**
     * Gets the file selector used by the DKF file selection dialog
     * 
     * @return the file selector used by the DKF file selection dialog
     */
    FileSelectionView getFileSelector();

    /**
     * Sets the survey context ID to use to get the list of surveys. If no survey context ID is set, then no surveys will be loaded.
     * 
     * @param id the ID to use
     */
    void setSurveyContextId(BigInteger id);
    
    /**
     * Sets the survey transition name for the survey choice panel.
     * 
     * @param name - The transition name of the survey.
     */
    void setTransitionName(String name);

    /**
     * Shows the embedded dkf editor
     * 
     * @param coursePath The path to the current course
     * @param url The url to the dkf file
     */
    void showDkfModalEditor(String coursePath, String url);

    /**
     * Gets the button used to select a conversation tree file for the Conversation
     * 
     * @return  the button used to select a conversation tree file for the conversation
     */
    HasClickHandlers getSelectConversationTreeFileButton();

    /**
     * Hides the conversation tree file label and the select file button will be shown instead.
     */
    void hideConversationTreeFileLabel();

    /**
     * Shows the embedded conversation editor
     * 
     * @param coursePath The path to the current course
     * @param url The url to the conversation tree file
     * @param courseObjectName the name of the course object used to open the conversation tree file
     */
    void showConversationTreeModalEditor(String coursePath, String url, String courseObjectName);

    /**
     * Gets the remove Conversationg Tree File button
     * 
     * @return The remove Conversation Tree File button
     */
    HasClickHandlers getRemoveConversationTreeButton();

    /**
     * Gets the clickable conversation tree file image
     *  
     * @return the conversation tree file image
     */
    HasClickHandlers getEditConversationTreeButton();


    /**
     * Sets the intro text in the file selection dialog
     * 
     * @param msg
     */
    void setFileSelectionDialogIntroMessage(String msg);

    void setAutotutorTypeEditingMode(GIFTAutotutorSessionTypeEditingMode mode);

    HasClickHandlers getSelectDKFFileButton();

    HasClickHandlers getEditDkfButton();
    
    void showDkfFileLabel(String path);
    
    void hideDkfFileLabel();

    HasClickHandlers getRemoveDkfButton();
    
    /**
     * Notifies the view that the name box (for the survey) has been updated.
     * 
     * @param name - The name of the survey transition.
     */
    public void onNameBoxUpdated(String name);

    /**
     * Sets the course data for the survey transition. 
     * 
     * @param currentCourse - The current course object that the survey transition belongs to.
     */
    void setCourseData(Course currentCourse);
    
    /** 
     * Sets the cancel callback which is used if there was an error importing a dkf file. 
     * 
     * @param callback - The callback that will be signalled if there is an error and the modal dialog was cancelled.
     */
    void setCancelCallback(CancelCallback callback);

    SurveyPicker getSurveyPicker();

    void refreshView();

    SurveyPickerQuestionBank getSurveyPickerQuestionBank();
    
    /** 
     * Sets the conversation file tree label and displays the edit, delete, and copy buttons.
     * 
     * @param path - The path to the conversation file. If null, the select button will be shown instead.
     */
    void showConversationTreeFileLabel(String path);

    /**
     * Gets the input box used to enter an AutoTutor conversation URL
     * 
     * @return the AutoTutor conversation URL input box
     */
    TextBox getConversationUrlBox();

}
