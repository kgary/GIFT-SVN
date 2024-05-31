/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.strategy;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.extras.select.client.ui.OptGroup;
import org.gwtbootstrap3.extras.select.client.ui.Option;
import org.gwtbootstrap3.extras.select.client.ui.Select;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.ATRemoteSKO;
import generated.dkf.ATRemoteSKO.URL;
import generated.dkf.Assessments.Survey;
import generated.dkf.AutoTutorSKO;
import generated.dkf.Concept;
import generated.dkf.Conversation;
import generated.dkf.ConversationTreeFile;
import generated.dkf.PerformanceAssessment;
import generated.dkf.PerformanceAssessment.PerformanceNode;
import generated.dkf.Strategy;
import generated.dkf.StrategyHandler;
import generated.dkf.Task;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.gwt.client.validation.WidgetValidationStatus;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.EnforcedButton;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.enums.CourseObjectNames.CourseObjectName;
import mil.arl.gift.tools.authoring.server.gat.client.place.ConversationPlace;
import mil.arl.gift.tools.authoring.server.gat.client.place.DkfPlace;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.ta.RealTimeAssessmentPanel;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared.PerfNodeAssessmentSelectorWidget;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.DefaultGatFileSelectionDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.ScenarioEventUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.CourseObjectModal;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.DeleteRemoveCancelCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.DeleteRemoveCancelDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.ErrorDetailsDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.NewOrExistingFileDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.WarningDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.validation.ScenarioValidationComposite;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.DeleteWorkspaceFiles;

/**
 * An editor used to author performance assessments
 */
public class PerformanceAssessmentEditor extends ScenarioValidationComposite {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(PerformanceAssessmentEditor.class.getName());

    /** The content message for the delete dialog */
    private static final String DELETE_DIALOG_MSG = "Do you wish to <b>permanently delete</b> this conversation tree or simply remove this reference to prevent it from being used in this part of the course?<br><br>"
            + "Other course objects will be unable to use this conversation tree if it is deleted, which may cause validation issues if this conversation tree is being referenced in other parts of the course.";

    /** The ui binder. */
    private static PerformanceAssessmentEditorUiBinder uiBinder = GWT.create(PerformanceAssessmentEditorUiBinder.class);

    /**
     * The Interface PerformanceAssessmentEditorUiBinder.
     */
    interface PerformanceAssessmentEditorUiBinder extends UiBinder<Widget, PerformanceAssessmentEditor> {
    }

    /** The performance node input. */
    @UiField
    protected Select performanceNodeSelect;

    /** The button used to jump to the selected task/concept page */
    @UiField(provided = true)
    protected EnforcedButton nodeJumpButton = new EnforcedButton(IconType.EXTERNAL_LINK, "",
            "Navigates to the selected task or concept page", new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    BigInteger selectedValue = getSelectedNodeId();
                    if (selectedValue == null) {
                        return;
                    }

                    Serializable taskOrConcept = ScenarioClientUtility.getTaskOrConceptWithId(selectedValue);
                    if (taskOrConcept == null) {
                        return;
                    }

                    ScenarioEventUtility.fireJumpToEvent(taskOrConcept);
                }
            });

    /**
     * Deck panel that contains the layout for performance node assessments and conversation
     * assessments.
     */
    @UiField
    protected DeckPanel assessmentDeck;

    /** The performance node selection panel */
    @UiField
    protected FlowPanel performanceNodeAssessmentPanel;

    /** The panel containing the performance node select table */
    @UiField
    protected HTMLPanel performanceNodeSelectPanel;

    /** The start conversation selection panel */
    @UiField
    protected FlowPanel conversationAssessmentPanel;

    /** The panel containing the conversation widgets */
    @UiField
    protected DeckPanel conversationDeckPanel;

    /** The panel containing the tree to select a conversation */
    @UiField
    protected RealTimeAssessmentPanel conversationTreeSelectPanel;

    /** The panel to select the Auto Tutor assessment */
    @UiField
    protected FlowPanel autoTutorUrlPanel;

    /** The button to redirect the user to the auto tutor authoring tool */
    @UiField
    protected org.gwtbootstrap3.client.ui.Button autoTutorLinkButton;

    /** The text box to enter the auto tutor URL */
    @UiField
    protected TextBox autoTutorUrlBox;

    /** The select widget containing the conversation types */
    @UiField
    protected Select conversationTypeSelect;

    /** The dialog that displays the conversation tree */
    @UiField
    protected CourseObjectModal conversationTreeDialog;
    
    /** contains the survey picker used for mid lesson survey authoring */
    @UiField
    protected PerfNodeAssessmentSelectorWidget perfNodeAssessmentSelectorWidget;

    /**
     * The container for showing validation messages for the strategy not having selected a
     * {@link Task} or {@link Concept} when requesting an additional assessment.
     */
    private final WidgetValidationStatus additionalAssessmentValidationStatus;

    /**
     * The message to display for validation if the additional assessment does not have a task or
     * concept selected
     */
    private static final String ADDITIONAL_ASSESSMENT_ERR_MSG = "The strategy requires a task or concept to be selected when requesting an additional assessment.";

    /**
     * The container for showing validation messages for the strategy not having an auto tutor
     * conversation file selected.
     */
    private final WidgetValidationStatus autoTutorValidation;
    
    /** The error message to display if there is no AutoTutor URL */
    private static final String NO_AUTO_TUTOR_URL_MSG = "An AutoTutor URL must be specified for this strategy.";

    /** The error message to display if the AutoTutorURL is too short */
    private static final String AUTO_TUTOR_URL_LENGTH_MSG = "The AutoTutor URL must be at least 4 characters.";

    /**
     * The container for showing validation messages for the strategy not having a conversation tree
     * file selected.
     */
    private final WidgetValidationStatus conversationTreeValidation;

    /** The error message to display if the conversation tree file is not selected */
    private static final String CONVERSATION_ERR_MSG = "A conversation tree file must be specified for this strategy.";

    /** The file selection dialog */
    private DefaultGatFileSelectionDialog fileSelectionDialog = new DefaultGatFileSelectionDialog();

    /** The strategy that is being editing */
    private Strategy selectedStrategy;

    /** The conversation tree that is currently selected */
    private String currentConversationTree = null;

    /** The text value for the placeholder option in the performance node dropdown list */
    private static final String NODE_PLACEHOLDER = "placeholder_value";

    /** The option group for the tasks that will be put into the performance node select */
    private final OptGroup performanceNodeTaskGroup = new OptGroup();

    /** The option group for the concepts that will be put into the performance node select */
    private final OptGroup performanceNodeConceptGroup = new OptGroup();

    /** The different conversation types */
    private static enum ConversationType {
        /** Option for the auto tutor */
        AUTOTUTOR("AutoTutor Conversation"),
        /** Option for the conversation tree */
        CONVERSATION_TREE("Conversation Tree");

        /** The description of the conversation type */
        private String description;

        /**
         * Constructs the {@link ConversationType} with the given description.
         *
         * @param description The description to use with this {@link ConversationType}
         */
        private ConversationType(String description) {
            if (description == null) {
                throw new IllegalArgumentException("The parameter 'description' cannot be null.");
            }

            this.description = description;
        }

        /**
         * Returns the text description of the conversation type
         *
         * @return The description as a String, can't be null.
         */
        public String getDescription() {
            return description;
        }
    }

    /**
     * Instantiates a new performance assessment editor.
     */
    public PerformanceAssessmentEditor() {

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("PerformanceAssessmentEditor()");
        }

        initWidget(uiBinder.createAndBindUi(this));

        additionalAssessmentValidationStatus = new WidgetValidationStatus(performanceNodeSelectPanel,
                ADDITIONAL_ASSESSMENT_ERR_MSG);

        conversationTreeValidation = new WidgetValidationStatus(conversationTreeSelectPanel.getAddAssessmentButton(),
                CONVERSATION_ERR_MSG);

        autoTutorValidation = new WidgetValidationStatus(autoTutorUrlBox, NO_AUTO_TUTOR_URL_MSG);

        fileSelectionDialog.getFileSelector()
                .setAllowedFileExtensions(new String[] { AbstractSchemaHandler.CONVERSATION_TREE_FILE_EXTENSION });
        fileSelectionDialog.setBypassCopyLogicForFilesInCurrentCourseFolder(true);

        performanceNodeTaskGroup.setLabel("Tasks");
        performanceNodeConceptGroup.setLabel("Concepts");

        for (ConversationType type : ConversationType.values()) {
            Option option = new Option();
            option.setText(type.getDescription());
            option.setValue(type.name());
            conversationTypeSelect.add(option);
        }

        initConversationTreeHandlers();
        initAutoTutorHandlers();

        // default panel to show
        assessmentDeck.showWidget(assessmentDeck.getWidgetIndex(performanceNodeAssessmentPanel));

        // set default to conversation tree
        conversationTypeSelect.setValue(ConversationType.AUTOTUTOR.name());
        showConversationWidgetByType(ConversationType.AUTOTUTOR);

        //
        // customize the performance node assessment widget
        //
        perfNodeAssessmentSelectorWidget.setAdditionalAssessmentLabelVisibility(false);
        perfNodeAssessmentSelectorWidget.setAssessmentTypeSelectorVisiblity(false);
        
        loadSurveyContext(ScenarioClientUtility.getSurveyContextId());
    }
    
    /**
     * Loads the given survey context into this widget's survey assessment panel
     * 
     * @param surveyContextId the ID of the survey context to load
     */
    private void loadSurveyContext(BigInteger surveyContextId) {
        perfNodeAssessmentSelectorWidget.loadSurveyContext(surveyContextId);
    }

    /**
     * Handles the selection changes for the performance nodes.
     *
     * @param event the value change event containing the new selection
     */
    @UiHandler("performanceNodeSelect")
    protected void onPerformanceNodeSelectChanged(ValueChangeEvent<String> event) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("onPerformanceNodeSelectChanged(" + event.getValue() + ")");
        }

        if (isConversation()) {
            return;
        }
        
        BigInteger selectedValue = getSelectedNodeId();
        if (selectedValue != null) {
            final Serializable newTaskOrConcept = ScenarioClientUtility.getTaskOrConceptWithId(selectedValue);
            if (newTaskOrConcept != null) {
                
                final Serializable currTaskOrConcept = perfNodeAssessmentSelectorWidget.getTaskOrConcept();
                if(currTaskOrConcept == null){   
                    // this is the first task/concept selected in this instance, therefore nothing to ask the user
                    showAdditionalAssessments(newTaskOrConcept);
                }else{
                    // another task/concept was previously selected
                    
                    String currSurveyGIFTKey = perfNodeAssessmentSelectorWidget.getSurveyGIFTKey();
                    if(StringUtils.isNotBlank(currSurveyGIFTKey)){
                        // a survey was assigned to the previously selected task/concept
                        
                        String currNodeName = ScenarioClientUtility.getTaskOrConceptName(currTaskOrConcept);
                        String newNodeName = ScenarioClientUtility.getTaskOrConceptName(newTaskOrConcept);

                        Serializable assessment = null;
                        if (newTaskOrConcept instanceof Task) {
                            Task task = (Task) newTaskOrConcept;

                            if (task.getAssessments() != null && !task.getAssessments().getAssessmentTypes().isEmpty()) {
                                assessment = task.getAssessments().getAssessmentTypes().get(0);
                            }

                        } else if (newTaskOrConcept instanceof Concept) {
                            Concept concept = (Concept) newTaskOrConcept;

                            if (concept.getAssessments() != null && !concept.getAssessments().getAssessmentTypes().isEmpty()) {
                                assessment = concept.getAssessments().getAssessmentTypes().get(0);
                            }
                        }
                        
                        if(assessment == null){
                            // the new task/concept doesn't have additional assessment, nothing to over-write
                            String message = "Would you like to move the mid-lesson survey that is already attached to" + 
                                    " '"+currNodeName+"' to the selected item '"+newNodeName+"' which has no mid-lesson survey assigned?";
                            
                            OkayCancelDialog.show("Mid-lesson survey decision", 
                                    message,
                                    null,
                                    "Move the survey", "Don't move the survey", new OkayCancelCallback() {

                                        @Override
                                        public void okay() {
                                            // move the survey gift key to the new node and nullify the old node
                                            moveSurveyAssessment(currTaskOrConcept, newTaskOrConcept);
                                            showAdditionalAssessments(newTaskOrConcept);
                                        }

                                        @Override
                                        public void cancel() {
                                            // show a no-survey picked experience for the new task/concept node
                                            showAdditionalAssessments(newTaskOrConcept);
                                        }
                                    });
                        }else if(assessment instanceof Survey){
                            // the new task/concept has additional assessment in the form of a survey, ask
                            // if that survey should be over-written.
                            
                            String message = "Would you like to move the mid-lesson survey that is already attached to "+
                                    "'"+currNodeName+"' to the selected item '"+newNodeName+"' which already has a mid-lesson survey assigned?";
                            
                            OkayCancelDialog.show("Mid-lesson survey decision", 
                                    message,
                                    null,
                                    "Move and over-write the survey", "Keep the surveys where they are", new OkayCancelCallback() {

                                        @Override
                                        public void okay() {
                                            // move the survey gift key to the new node and nullify the old node
                                            moveSurveyAssessment(currTaskOrConcept, newTaskOrConcept);
                                            showAdditionalAssessments(newTaskOrConcept);
                                        }

                                        @Override
                                        public void cancel() {
                                            // show the survey already assigned to the newly selected task/concept
                                            showAdditionalAssessments(newTaskOrConcept);  
                                        }
                                    });
                            
                        }else{
                            // the new task/concept has a non-survey additional assessment, ask if that should
                            // be changed to survey type.
                        
                            String message = "Would you like to over-write the current assessment type choosen for "+
                                    "'"+newNodeName+"' with a mid-lesson survey?";
                            
                            OkayCancelDialog.show("Mid-lesson survey decision", 
                                    message,
                                    null,
                                    "Over-write with survey", "Cancel", new OkayCancelCallback() {

                                        @Override
                                        public void okay() {
                                            // show the survey already assigned to the newly selected task/concept
                                            showAdditionalAssessments(newTaskOrConcept);
                                        }

                                        @Override
                                        public void cancel() {
                                            //do nothing                                       
                                        }
                                    });
                        }
                        

                    }else{
                        // a survey was not-assigned to the previously selected task/concept, therefore
                        // nothing to ask of the user
                        showAdditionalAssessments(newTaskOrConcept);
                    }

                }
            }else{
                perfNodeAssessmentSelectorWidget.setVisible(false);
            }
        }else{
            perfNodeAssessmentSelectorWidget.setVisible(false);
        }

        requestValidation(additionalAssessmentValidationStatus);
    }
    
    /**
     * Show the mid-lesson survey picker for the provided task.
     * 
     * @param taskOrConcept the task/concept to show and edit the assessment object for in order to 
     * author a mid-lesson survey.
     */
    private void showAdditionalAssessments(Serializable taskOrConcept){
        
        // show the task/concept assessment panel
        perfNodeAssessmentSelectorWidget.setVisible(true);
        
        // make sure the assessment type is survey so the appropriate assessment type selection
        // is made in the assessment panel
        if(taskOrConcept != null){
            
            if (taskOrConcept instanceof Task) {
                Task task = (Task) taskOrConcept;

                if (task.getAssessments() == null){
                    task.setAssessments(new generated.dkf.Assessments());
                }

                if(task.getAssessments().getAssessmentTypes().isEmpty()) {
                    task.getAssessments().getAssessmentTypes().add(new generated.dkf.Assessments.Survey());
                }

            } else if (taskOrConcept instanceof Concept) {
                Concept concept = (Concept) taskOrConcept;
                
                if (concept.getAssessments() == null){
                    concept.setAssessments(new generated.dkf.Assessments());
                }

                if(concept.getAssessments().getAssessmentTypes().isEmpty()) {
                    concept.getAssessments().getAssessmentTypes().add(new generated.dkf.Assessments.Survey());
                }

            }
        }
        
        // populate the panel with this task/concept assessment object data model to edit
        perfNodeAssessmentSelectorWidget.showAdditionalAssessments(taskOrConcept);
    }
    
    /**
     * Moves the assessment object from the source task/concept to the destination task/concept.
     * 
     * @param sourceTaskOrConcept if it contains an assessment, the first assessment is removed and added
     * to the destination.  If null this method does nothing.  If this contains no assessments this method does nothing.
     * @param destTaskOrConcept where to add the removed assessment object.  If null this method does nothing.
     */
    private void moveSurveyAssessment(Serializable sourceTaskOrConcept, Serializable destTaskOrConcept){
        
        if(sourceTaskOrConcept == null || destTaskOrConcept == null){
            return;
        }
        
        Serializable sourceAssessment = null;
        
        // Remove it from the source task/concept
        if (sourceTaskOrConcept instanceof Task) {
            Task task = (Task) sourceTaskOrConcept;

            if (task.getAssessments() != null && !task.getAssessments().getAssessmentTypes().isEmpty()) {
                
                if(logger.isLoggable(Level.INFO)){
                    logger.info("removing additional assessment from task named "+task.getName());
                }
                sourceAssessment = task.getAssessments().getAssessmentTypes().remove(0);
            }

        } else if (sourceTaskOrConcept instanceof Concept) {
            Concept concept = (Concept) sourceTaskOrConcept;

            if (concept.getAssessments() != null && !concept.getAssessments().getAssessmentTypes().isEmpty()) {
                if(logger.isLoggable(Level.INFO)){
                    logger.info("removing additional assessment from concept named "+concept.getName());
                }
                sourceAssessment = concept.getAssessments().getAssessmentTypes().remove(0);                
            }
        }
        
        if(sourceAssessment != null){
            // add it to the destination task/concept
            
            if (destTaskOrConcept instanceof Task) {
                Task task = (Task) destTaskOrConcept;
                
                if(task.getAssessments() == null){
                    task.setAssessments(new generated.dkf.Assessments());
                }
                
                if(logger.isLoggable(Level.INFO)){
                    logger.info("adding removed additional assessment to task "+task.getName());
                }
                task.getAssessments().getAssessmentTypes().set(0, sourceAssessment);

            } else if (destTaskOrConcept instanceof Concept) {
                Concept concept = (Concept) destTaskOrConcept;
                
                if(concept.getAssessments() == null){
                    concept.setAssessments(new generated.dkf.Assessments());
                }

                if(logger.isLoggable(Level.INFO)){
                    logger.info("adding removed additional assessment to concept "+concept.getName());
                }
                concept.getAssessments().getAssessmentTypes().set(0, sourceAssessment);
            }
        }
    }

    /**
     * Handles the selection changes for the conversation types.
     *
     * @param event the value change event containing the new selection
     */
    @UiHandler("conversationTypeSelect")
    protected void onConversationTypeSelectChanged(ValueChangeEvent<String> event) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("onConversationTypeSelectChanged(" + event.getValue() + ")");
        }

        if (StringUtils.equals(event.getValue(), ConversationType.CONVERSATION_TREE.name())) {
            showConversationWidgetByType(ConversationType.CONVERSATION_TREE);
        } else if (StringUtils.equals(event.getValue(), ConversationType.AUTOTUTOR.name())) {
            showConversationWidgetByType(ConversationType.AUTOTUTOR);
        }

        requestValidationAndFireDirtyEvent(selectedStrategy, conversationTreeValidation, autoTutorValidation);
    }

    /**
     * Displays the performance node panel and hides the other performance assessment panels.
     */
    public void showPerformanceNodePanel() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("showPerformanceNodePanel()");
        }

        assessmentDeck.showWidget(assessmentDeck.getWidgetIndex(performanceNodeAssessmentPanel));
        requestValidation(additionalAssessmentValidationStatus, autoTutorValidation, conversationTreeValidation);
    }

    /**
     * Displays the conversation panel and hides the other performance assessment panels.
     */
    public void showConversationPanel() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("showConversationPanel()");
        }

        assessmentDeck.showWidget(assessmentDeck.getWidgetIndex(conversationAssessmentPanel));

        String selectedConversationType = conversationTypeSelect.getValue();
        for (ConversationType conversationType : ConversationType.values()) {
            if (selectedConversationType.equals(conversationType.name())) {
                showConversationWidgetByType(conversationType);
                break;
            }
        }
        requestValidation(additionalAssessmentValidationStatus, autoTutorValidation, conversationTreeValidation);
    }

    /**
     * Populate performance node choices.
     */
    public void populatePerformanceNodeChoices() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("populatePerformanceNodeChoices()");
        }

        performanceNodeTaskGroup.clear();
        performanceNodeConceptGroup.clear();

        /* TODO: [nice to have] create a tree structure here like in the outline
         * as opposed to the flat task/concept lists. */
        for (Task task : ScenarioClientUtility.getUnmodifiableTaskList()) {
            Option option = new Option();
            option.setText(task.getName());
            option.setValue(task.getNodeId().toString());
            option.setSelected(false);
            performanceNodeTaskGroup.add(option);
        }

        for (Concept concept : ScenarioClientUtility.getUnmodifiableConceptList()) {
            Option option = new Option();
            option.setText(concept.getName());
            option.setValue(concept.getNodeId().toString());
            option.setSelected(false);
            performanceNodeConceptGroup.add(option);
        }

        performanceNodeSelect.clear();

        Option placeholderOption = new Option();
        placeholderOption.setText("Select a task or concept");
        placeholderOption.setValue(NODE_PLACEHOLDER);
        placeholderOption.setSelected(true);
        placeholderOption.setEnabled(false);
        placeholderOption.setHidden(true);
        performanceNodeSelect.add(placeholderOption);

        if (performanceNodeTaskGroup.getWidgetCount() != 0) {
            performanceNodeSelect.add(performanceNodeTaskGroup);
        }

        if (performanceNodeConceptGroup.getWidgetCount() != 0) {
            performanceNodeSelect.add(performanceNodeConceptGroup);
        }

        performanceNodeSelect.setValue(NODE_PLACEHOLDER);
        performanceNodeSelect.render();
        performanceNodeSelect.refresh();
        
        // don't show until the performance node is set
        perfNodeAssessmentSelectorWidget.setVisible(false);
    }

    /**
     * Adds a new option to the list of tasks and concepts using the provided {@link Task} or
     * {@link Concept}.
     *
     * @param taskOrConcept the {@link Task} or {@link Concept} to add.
     */
    public void addPerformanceNode(Serializable taskOrConcept) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("addPerformanceNode(" + taskOrConcept + ")");
        }

        Option option = new Option();
        if (taskOrConcept instanceof Task) {
            Task task = (Task) taskOrConcept;
            option.setText(task.getName());
            option.setValue(task.getNodeId().toString());
            performanceNodeTaskGroup.add(option);
        } else if (taskOrConcept instanceof Concept) {
            Concept concept = (Concept) taskOrConcept;
            option.setText(concept.getName());
            option.setValue(concept.getNodeId().toString());
            performanceNodeConceptGroup.add(option);
        }

        performanceNodeSelect.render();
        performanceNodeSelect.refresh();
    }

    /**
     * Checks if the node id is selected in {@link #performanceNodeSelect}. If it is selected, set
     * the selected node to null.
     *
     * @param nodeId the node id to check against.
     */
    public void removePerformanceNode(BigInteger nodeId) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("removePerformanceNode(" + nodeId + ")");
        }

        if (nodeId == null) {
            return;
        }

        // if the node id is selected, set value to placeholder
        if (nodeId.equals(getSelectedNodeId())) {
            performanceNodeSelect.setValue(NODE_PLACEHOLDER, true);
        }

        // remove the performance node from the dropdown if in tasks
        for (int i = 0; i < performanceNodeTaskGroup.getWidgetCount(); i++) {
            Widget w = performanceNodeTaskGroup.getWidget(i);
            if (w instanceof Option) {
                Option option = (Option) w;
                if (StringUtils.equals(nodeId.toString(), option.getValue())) {
                    performanceNodeTaskGroup.remove(option);
                    performanceNodeSelect.render();
                    performanceNodeSelect.refresh();
                    return;
                }
            }
        }

        // remove the performance node from the dropdown if in concepts
        for (int i = 0; i < performanceNodeConceptGroup.getWidgetCount(); i++) {
            Widget w = performanceNodeConceptGroup.getWidget(i);
            if (w instanceof Option) {
                Option option = (Option) w;
                if (StringUtils.equals(nodeId.toString(), option.getValue())) {
                    performanceNodeConceptGroup.remove(option);
                    performanceNodeSelect.render();
                    performanceNodeSelect.refresh();
                    return;
                }
            }
        }
    }

    /**
     * Updates the dropdown of {@link Task tasks} and {@link Concept concepts} to reflect the name
     * change.
     *
     * @param nodeId the node id of the {@link Task} or {@link Concept} that was renamed. If null, nothing will change.
     * @param newName the new name. If blank, nothing will change.
     */
    public void handleTaskOrConceptRename(BigInteger nodeId, String newName) {
        if (logger.isLoggable(Level.FINE)) {
            List<Object> params = Arrays.<Object>asList(nodeId, newName);
            logger.fine("handleTaskOrConceptRename(" + StringUtils.join(", ", params) + ")");
        }

        if (nodeId == null || StringUtils.isBlank(newName)) {
            return;
        }

        for(Option option : performanceNodeSelect.getItems()) {
            if (StringUtils.equals(nodeId.toString(), option.getValue())) {
                option.setText(newName);
                performanceNodeSelect.refresh();
                break;
            }
        }
        
        if (nodeId != null) {
            Serializable taskOrConcept = ScenarioClientUtility.getTaskOrConceptWithId(nodeId);
            if (taskOrConcept != null) {
                perfNodeAssessmentSelectorWidget.onRename(taskOrConcept);
            }
        }
    }

    /**
     * Adds the handlers needed for the conversation tree.
     */
    private void initConversationTreeHandlers() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("initConversationTreeHandlers()");
        }

        // add button click handler
        conversationTreeSelectPanel.getAddAssessmentButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                NewOrExistingFileDialog.showCreateOrSelect("Real-Time Assessment", new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent createEvent) {

                        /* this is a temporary measure until we can figure out why the conversation
                         * prenter is not starting when accessed from the GIFT Wrap system tray */ 
                        if (GatClientUtility.isGIFTWrapMode()) {
                            WarningDialog.info("Unable To Create Conversation Tree",
                                    "GIFT Wrap does not support creating new conversation trees at this time. To create a conversation tree, please import this real time assessment into your course using the course creator and create the conversation tree by locating this panel again.");
                            return;
                        }

                        final String courseFolder = GatClientUtility.getBaseCourseFolderPath();
                        // Pass the course survey context id into the url via a parameter.
                        HashMap<String, String> paramMap = new HashMap<String, String>();
                        paramMap.put(DkfPlace.PARAM_GIFTWRAP, Boolean.toString(GatClientUtility.isGIFTWrapMode()));
                        final String url = GatClientUtility.createModalDialogUrlWithParams(courseFolder, "ConversationTree_",
                                AbstractSchemaHandler.CONVERSATION_TREE_FILE_EXTENSION, paramMap);

                        conversationTreeDialog.setCourseObjectUrl(CourseObjectName.CONVERSATION_TREE.getDisplayName(),
                                url);
                        conversationTreeDialog.setSaveButtonHandler(new ClickHandler() {
                            @Override
                            public void onClick(ClickEvent event) {
                                GatClientUtility.saveEmbeddedCourseObject();
                                String filename = GatClientUtility.getFilenameFromModalUrl(courseFolder, url,
                                        AbstractSchemaHandler.CONVERSATION_TREE_FILE_EXTENSION);
                                fileSelectionDialog.setValue(filename, true);
                                conversationTreeDialog.stopEditor();
                                CourseObjectModal.resetEmbeddedSaveObject();
                                requestValidationAndFireDirtyEvent(selectedStrategy, conversationTreeValidation);
                                
                                if(!GatClientUtility.isReadOnly()){
                                    GatClientUtility.saveCourseAndNotify(); // #4913 - now saving course in order to not loose recently added course objects that lead to this conversation being edited
                                }
                            }
                        });

                        conversationTreeDialog.show();
                    }

                }, new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent selectEvent) {
                        fileSelectionDialog.getFileSelector().setAllowedFileExtensions(
                                new String[] { AbstractSchemaHandler.CONVERSATION_TREE_FILE_EXTENSION });
                        fileSelectionDialog
                                .setIntroMessageHTML(DefaultGatFileSelectionDialog.CHOOSE_CONVERSATION_TREE_FILE_OBJECT);
                        setFileSelectionDialogVisible(true);
                    }
                });
            }
        });

        // edit button click handler
        conversationTreeSelectPanel.getEditButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                HashMap<String, String> paramMap = new HashMap<String, String>();
                paramMap.put(ConversationPlace.PARAM_READONLY, Boolean.toString(ScenarioClientUtility.isReadOnly()));

                final String courseFolder = GatClientUtility.getBaseCourseFolderPath();
                final String url = GatClientUtility.getModalDialogUrlWithParams(courseFolder, currentConversationTree, paramMap);

                conversationTreeDialog.setCourseObjectUrl(CourseObjectName.CONVERSATION_TREE.getDisplayName(), url);
                conversationTreeDialog.setSaveButtonHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        GatClientUtility.saveEmbeddedCourseObject();
                        String filename = GatClientUtility.getFilenameFromModalUrl(courseFolder, url,
                                AbstractSchemaHandler.CONVERSATION_TREE_FILE_EXTENSION);
                        fileSelectionDialog.setValue(filename, true);
                        conversationTreeDialog.stopEditor();
                        CourseObjectModal.resetEmbeddedSaveObject();
                        ScenarioEventUtility.fireDirtyEditorEvent();
                        
                        if(!GatClientUtility.isReadOnly()){
                            GatClientUtility.saveCourseAndNotify(); // #4913 - now saving course in order to not loose recently added course objects that lead to this conversation being edited
                        }
                    }
                });

                conversationTreeDialog.show();
            }
        });

        // delete button click handler
        conversationTreeSelectPanel.getDeleteButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                DeleteRemoveCancelDialog.show("Delete Conversation Tree", DELETE_DIALOG_MSG,
                        new DeleteRemoveCancelCallback() {

                            @Override
                            public void delete() {
                                String username = GatClientUtility.getUserName();
                                String browserSessionKey = GatClientUtility.getBrowserSessionKey();
                                List<String> filesToDelete = new ArrayList<String>();

                                final String filename = GatClientUtility.getBaseCourseFolderPath() + "/"
                                        + currentConversationTree;
                                filesToDelete.add(filename);

                                DeleteWorkspaceFiles action = new DeleteWorkspaceFiles(username, browserSessionKey,
                                        filesToDelete, true);

                                SharedResources.getInstance().getDispatchService().execute(action,
                                        new AsyncCallback<GatServiceResult>() {

                                            @Override
                                            public void onFailure(Throwable t) {
                                                ErrorDetailsDialog dialog = new ErrorDetailsDialog(
                                                        "Failed to delete the file.", t.getMessage(),
                                                        DetailedException.getFullStackTrace(t));
                                                dialog.setDialogTitle("Deletion Failed");
                                                dialog.center();
                                            }

                                            @Override
                                            public void onSuccess(GatServiceResult result) {
                                                if (result.isSuccess()) {
                                                    remove();
                                                    GatClientUtility.saveEmbeddedCourseObject();
                                                } else {
                                                    logger.warning("Was unable to delete the file: " + filename
                                                            + "\nError Message: " + result.getErrorMsg());
                                                }
                                            }

                                        });
                            }

                            @Override
                            public void remove() {
                                removeConversationTreeAssessment();
                                requestValidationAndFireDirtyEvent(selectedStrategy, conversationTreeValidation);
                            }

                            @Override
                            public void cancel() {
                            }
                        });
            }
        });

        // file selection value change handler
        fileSelectionDialog.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                if (event.getValue().endsWith(AbstractSchemaHandler.CONVERSATION_TREE_FILE_EXTENSION)) {
                    setConversationTreeFileAssessment(event.getValue());
                }

                requestValidationAndFireDirtyEvent(selectedStrategy, conversationTreeValidation, autoTutorValidation);
            }
        });
    }

    /**
     * Adds the handlers needed for the auto tutor.
     */
    private void initAutoTutorHandlers() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("initAutoTutorHandlers()");
        }

        autoTutorUrlBox.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("autoTutorUrlBox.onValueChange(" + event.getValue() + ")");
                }

                requestValidationAndFireDirtyEvent(selectedStrategy, autoTutorValidation);
            }
        });

        autoTutorLinkButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                GatClientUtility.openASATWindow();
            }
        });
    }

    /**
     * Removes the conversation tree assessment
     */
    private void removeConversationTreeAssessment() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("removeConversationTreeAssessment()");
        }

        currentConversationTree = null;
        conversationTreeSelectPanel.removeAssessment();
    }

    /**
     * Shows the correct conversation widget depending on the {@link ConversationType conversation
     * type} that is provided.
     *
     * @param type the {@link ConversationType conversation type} to display
     */
    private void showConversationWidgetByType(ConversationType type) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("showConversationWidgetByType(" + type + ")");
        }

        if (type == null) {
            throw new IllegalArgumentException("The parameter 'type' cannot be null.");
        }

        if (type == ConversationType.AUTOTUTOR) {
            conversationDeckPanel.showWidget(conversationDeckPanel.getWidgetIndex(autoTutorUrlPanel));
        } else {
            conversationDeckPanel.showWidget(conversationDeckPanel.getWidgetIndex(conversationTreeSelectPanel));
        }
    }

    /**
     * Sets the assessment path for the conversation tree file.
     *
     * @param path the path of the conversation tree file. If blank, the assessment will be removed.
     */
    private void setConversationTreeFileAssessment(String path) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("setConversationTreeFileAssessment(" + path + ")");
        }

        if (StringUtils.isNotBlank(path)) {
            currentConversationTree = path;
            conversationTreeSelectPanel.setAssessment(path);
        } else {
            removeConversationTreeAssessment();
        }
    }

    /**
     * Shows/Hides the file selection dialog.
     *
     * @param visible true to show the dialog; false to hide it.
     */
    public void setFileSelectionDialogVisible(boolean visible) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("setFileSelectionDialogVisible(" + visible + ")");
        }

        if (visible) {
            fileSelectionDialog.center();
        } else {
            fileSelectionDialog.hide();
        }
    }

    /**
     * Sets the {@link Strategy} being edited.
     *
     * @param strategy the {@link Strategy} being edited. Can't be null.
     */
    public void setStrategyBeingEdited(Strategy strategy) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("setStrategyBeingEdited(" + strategy + ")");
        }

        if (strategy == null) {
            throw new IllegalArgumentException("The parameter 'strategy' cannot be null.");
        }

        this.selectedStrategy = strategy;
    }

    /**
     * Resets the editor to be the state before the user interacted with it.
     */
    public void resetEditor() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("resetEditor()");
        }

        autoTutorUrlBox.clear();
        removeConversationTreeAssessment();

        /* Since the value of the auto tutor and conversation tree widgets have
         * changed, revalidate them */
        validateAll();
    }

    /**
     * Populates the UI with data from the provided {@link PerformanceAssessment}
     *
     * @param performanceAssessment the {@link PerformanceAssessment} to be edited. Can't be null.
     */
    public void populatePerformanceAssessment(PerformanceAssessment performanceAssessment) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("populatePerformanceAssessment(" + performanceAssessment + ")");
        }

        if (performanceAssessment == null) {
            throw new IllegalArgumentException("The parameter 'performanceAssessment' cannot be null.");
        }

        if (selectedStrategy == null) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Before the ");
            stringBuilder.append(PerformanceAssessmentEditor.class.getSimpleName());
            stringBuilder.append(" is populated, it must be provided with the strategy within which its activity is being edited.");
            throw new UnsupportedOperationException(stringBuilder.toString());
        }

        updateReadOnly();

        // set instance variables
        removeConversationTreeAssessment();

        // set default to conversation tree
        conversationTypeSelect.setValue(ConversationType.AUTOTUTOR.name());
        showConversationWidgetByType(ConversationType.AUTOTUTOR);

        if (performanceAssessment.getStrategyHandler() == null) {
            performanceAssessment.setStrategyHandler(new StrategyHandler());
        }

        Serializable assessmentType = performanceAssessment.getAssessmentType();
        if (assessmentType == null) {
            return;
        }

        if (assessmentType instanceof PerformanceNode) {
            PerformanceNode perfNode = (PerformanceNode) assessmentType;
            showPerformanceNodePanel();
            Serializable taskOrConcept = ScenarioClientUtility.getTaskOrConceptWithId(perfNode.getNodeId());
            if (taskOrConcept == null) {
                performanceNodeSelect.setValue(NODE_PLACEHOLDER);
                // ensure the dkf updates to reflect a null node
                ValueChangeEvent.fire(performanceNodeSelect, NODE_PLACEHOLDER);
                performanceNodeSelect.render();
                performanceNodeSelect.refresh();
                nodeJumpButton.setVisible(false);
            } else {
                performanceNodeSelect.setValue(perfNode.getNodeId().toString());
                performanceNodeSelect.render();
                performanceNodeSelect.refresh();
                nodeJumpButton.setVisible(true);
                
                showAdditionalAssessments(taskOrConcept);
            }
        } else if (assessmentType instanceof Conversation) {
            Conversation currentConversation = (Conversation) assessmentType;
            showConversationPanel();
            if (currentConversation.getType() instanceof ConversationTreeFile) {
                ConversationTreeFile currentConversationTreeFile = (ConversationTreeFile) currentConversation.getType();
                showConversationWidgetByType(ConversationType.CONVERSATION_TREE);
                setConversationTreeFileAssessment(currentConversationTreeFile.getName());
                conversationTypeSelect.setValue(ConversationType.CONVERSATION_TREE.name());
            } else if (currentConversation.getType() instanceof AutoTutorSKO) {
                AutoTutorSKO currentSko = (AutoTutorSKO) currentConversation.getType();
                showConversationWidgetByType(ConversationType.AUTOTUTOR);
                conversationTypeSelect.setValue(ConversationType.AUTOTUTOR.name());

                if (currentSko.getScript() instanceof ATRemoteSKO) {
                    ATRemoteSKO remoteSko = (ATRemoteSKO) currentSko.getScript();
                    String urlAddress = null;
                    if (remoteSko.getURL() != null) {
                        urlAddress = remoteSko.getURL().getAddress();
                    }
                    autoTutorUrlBox.setValue(urlAddress);
                } else {
                    autoTutorUrlBox.setValue(null);
                }
            }
        }
    }

    /**
     * Checks if the user has chosen a {@link ConversationTreeFile} for the
     * {@link PerformanceAssessment} and whether the specific file has been
     * chosen yet.
     *
     * @return True if the user has chosen a {@link ConversationTreeFile} type
     *         of {@link PerformanceAssessment} and a specific conversation tree
     *         ({@link #currentConversationTree}) is not blank.
     */
    private boolean isConversationSelected() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("isConversationSelected()");
        }

        if(isConversationTree()) {
            return StringUtils.isNotBlank(currentConversationTree);
        } else {
            return false;
        }
    }

    /**
     * Calculates the currently selected value of the
     * {@link #performanceNodeSelect}.
     *
     * @return The node id that is selected or null if none is selected.
     */
    private BigInteger getSelectedNodeId() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("getSelectedNodeId()");
        }

        if (!isPerformanceNodeAssessment()) {
            return null;
        }
        
        if(performanceNodeSelect.getSelectedItem() == null){
            return null;
        }

        String selectedValue = performanceNodeSelect.getSelectedItem().getValue();

        /* If the selectedValue is blank or the placeholder return null */
        if (StringUtils.isBlank(selectedValue) || StringUtils.equals(selectedValue, NODE_PLACEHOLDER)) {
            return null;
        }

        /* Return the BigInteger value that has been selected */
        return new BigInteger(selectedValue);
    }

    /**
     * Populates a provided {@link PerformanceAssessment} with the current state
     * of the {@link PerformanceAssessmentEditor}.
     *
     * @param perfAssessment The {@link PerformanceAssessment} to populate.
     *        Can't be null.
     */
    public void applyEdits(PerformanceAssessment perfAssessment) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("applyEdits(" + perfAssessment + ")");
        }

        if (perfAssessment == null) {
            throw new IllegalArgumentException("The parameter 'perfAssessment' cannot be null.");
        }

        /* Populate the assessement type */
        if (isPerformanceNodeAssessment()) {
            PerformanceNode perfNode = new PerformanceNode();
            perfNode.setNodeId(getSelectedNodeId());
            perfAssessment.setAssessmentType(perfNode);
        } else if (isConversation()) {
            Conversation conversation = new Conversation();
            perfAssessment.setAssessmentType(conversation);

            if (isAutoTutorConversation()) {
                AutoTutorSKO autoTutorSko = new AutoTutorSKO();

                /* Construct the remote SKO */
                ATRemoteSKO remoteSko = new ATRemoteSKO();
                remoteSko.setURL(new URL());
                remoteSko.getURL().setAddress(autoTutorUrlBox.getValue());
                autoTutorSko.setScript(remoteSko);

                conversation.setType(autoTutorSko);
            } else if (isConversationTree()) {
                ConversationTreeFile conversationTree = new ConversationTreeFile();
                conversationTree.setName(currentConversationTree);
                conversation.setType(conversationTree);
            }
        }
    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
        childValidationComposites.add(perfNodeAssessmentSelectorWidget);
    }

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        validationStatuses.add(additionalAssessmentValidationStatus);
        validationStatuses.add(autoTutorValidation);
        validationStatuses.add(conversationTreeValidation);
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("validate(" + validationStatus + ")");
        }

        /* Determine if performance assessment is for a performance node based
         * on the visible widget. */
        boolean isPerformanceNode = isPerformanceNodeAssessment();

        if (additionalAssessmentValidationStatus.equals(validationStatus)) {
            additionalAssessmentValidationStatus
                    .setValidity(!isPerformanceNode || getSelectedNodeId() != null);

        } else if (autoTutorValidation.equals(validationStatus)) {
            if (isPerformanceNode) {
                autoTutorValidation.setValid();
            } else {
                if (!isAutoTutorConversation()) {
                    autoTutorValidation.setValid();
                    return;
                }

                if (StringUtils.isBlank(autoTutorUrlBox.getValue())) {
                    autoTutorValidation.setErrorMessage(NO_AUTO_TUTOR_URL_MSG);
                    autoTutorValidation.setInvalid();
                } else if (autoTutorUrlBox.getValue().length() < 4) {
                    autoTutorValidation.setErrorMessage(AUTO_TUTOR_URL_LENGTH_MSG);
                    autoTutorValidation.setInvalid();
                } else {
                    autoTutorValidation.setValid();
                }
            }

        } else if (conversationTreeValidation.equals(validationStatus)) {
            if (isPerformanceNode) {
                conversationTreeValidation.setValid();
            } else {
                conversationTreeValidation.setValidity(!isConversationTree() || isConversationSelected());
            }
        }
    }

    /**
     * Updates the read only mode based on the state of the widget.
     */
    private void updateReadOnly() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("updateReadOnly()");
        }

        setReadonly(ScenarioClientUtility.isReadOnly());
    }

    /**
     * Determines if the {@link #performanceNodeAssessmentPanel} is visible.
     *
     * @return True if {@link #performanceNodeAssessmentPanel} is visible, false
     *         otherwise.
     */
    private boolean isPerformanceNodeAssessment() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("isPerformanceNodeAssessment()");
        }

        return assessmentDeck.getVisibleWidget() == assessmentDeck.getWidgetIndex(performanceNodeAssessmentPanel);
    }

    /**
     * Determines if the {@link #conversationAssessmentPanel} is visible.
     *
     * @return True if {@link #conversationAssessmentPanel} is visible, false
     *         otherwise.
     */
    private boolean isConversation() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("isConversation()");
        }

        return assessmentDeck.getVisibleWidget() == assessmentDeck.getWidgetIndex(conversationAssessmentPanel);
    }

    /**
     * Determines if the {@link #autoTutorUrlPanel} is visible.
     *
     * @return True if {@link #conversationAssessmentPanel} and
     *         {@link #autoTutorUrlPanel} are visible, false otherwise.
     */
    private boolean isAutoTutorConversation() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("isAutoTutorConversation()");
        }

        return isConversation()
                && conversationDeckPanel.getVisibleWidget() == conversationDeckPanel.getWidgetIndex(autoTutorUrlPanel);
    }

    /**
     * Determines if the {@link #conversationDeckPanel} is visible.
     *
     * @return True if {@link #conversationAssessmentPanel} and
     *         {@link #conversationDeckPanel} are visible, false otherwise.
     */

    private boolean isConversationTree() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("isConversationTree()");
        }

        return isConversation()
                && conversationDeckPanel.getVisibleWidget() == conversationDeckPanel.getWidgetIndex(conversationTreeSelectPanel);
    }

    /**
     * Sets the components to read only mode which prevents users from making changes.
     *
     * @param isReadonly True to prevent editing, false to allow editing.
     */
    public void setReadonly(boolean isReadonly) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("setReadonly(" + isReadonly + ")");
        }

        performanceNodeSelect.setEnabled(!isReadonly);
        conversationTypeSelect.setEnabled(!isReadonly);
        conversationTreeSelectPanel.setReadOnlyMode(isReadonly);
        autoTutorLinkButton.setEnabled(!isReadonly);
        autoTutorUrlBox.setEnabled(!isReadonly);
    }
}