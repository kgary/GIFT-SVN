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
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.Collapse;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.extras.select.client.ui.Option;
import org.gwtbootstrap3.extras.select.client.ui.Select;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.InstructionalIntervention;
import generated.dkf.MidLessonMedia;
import generated.dkf.Nvpair;
import generated.dkf.PerformanceAssessment;
import generated.dkf.ScenarioAdaptation;
import generated.dkf.Strategy;
import generated.dkf.StrategyHandler;
import generated.dkf.StrategyHandler.Params;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared.DkfNameValuePairEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared.Ribbon;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.strategy.AddFeedbackWidget.RibbonVisibilityChangedHandler;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.strategy.StrategyActivityEditor.StrategyActivityWrapper;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioValidatorUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.validation.ScenarioValidationComposite;

/**
 * An {@link ItemEditor} for an {@link ItemListEditor} that edits a strategy
 * activity returned by {@link Strategy#getStrategyActivities()}
 *
 * @author tflowers
 *
 */
public class StrategyActivityEditor extends ItemEditor<StrategyActivityWrapper> {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(StrategyActivityEditor.class.getName());

    /** The binder that combines this java class with the ui.xml */
    private static final StrategyActivityEditorUiBinder uiBinder = GWT.create(StrategyActivityEditorUiBinder.class);

    /** Defines the binder that combines the java class with the ui.xml */
    interface StrategyActivityEditorUiBinder extends UiBinder<Widget, StrategyActivityEditor> {
    }

    /**
     * Wraps a strategy activity to allow for a 'nullable' strategy activity.
     *
     * @author tflowers
     *
     */
    public static class StrategyActivityWrapper {

        /** The object that is being wrapped. */
        private Serializable activity = null;

        /**
         * Creates a wrapper without an activity to wrap.
         */
        public StrategyActivityWrapper() {

        }

        /**
         * Creates a wrapper around a given activity.
         *
         * @param activity The activity to wrap.  One of InstructionalIntervention, MidLessonMedia, PerformanceAssessment,
         * ScenarioAdaptation.
         */
        public StrategyActivityWrapper(Serializable activity) {
            this.activity = activity;
        }

        /**
         * Sets the wrapped activity.
         *
         * @param obj The new activity to wrap. Can be null.
         */
        public void setActivity(final Serializable obj) {
            this.activity = obj;
        }

        /**
         * Gets the wrapped activity.
         *
         * @return The wrapped activity. Can be null. One of InstructionalIntervention, MidLessonMedia, PerformanceAssessment,
         * ScenarioAdaptation.
         */
        public Serializable getActivity() {
            return activity;
        }

        @Override
        public String toString() {
            return new StringBuilder("[StrategyActivityWrapper: activity = ")
                    .append(getActivity())
                    .append("]").toString();
        }
    }

    /** Contains each of the strategy sub-editors */
    @UiField
    protected DeckPanel strategyTypeDeck;

    /** The ribbon that allows the user to choose the strategy type */
    @UiField
    protected Ribbon strategyTypeRibbon;

    @UiField
    protected Button changeTypeButton;

    /** The editor that allows creation of feedback for the learner */
    @UiField
    protected InstructionalInterventionEditor instructionalInterventionEditor;

    /** The editor that allows creation of media for the learner */
    @UiField
    protected MediaCollectionEditor mediaCollectionEditor;

    /** The editor that allows creation of scenario adaptations */
    @UiField
    protected ScenarioAdaptationEditor scenarioAdaptationEditor;

    /**
     * The editor that allows the user to include additional assessments for a
     * task/concept based on a survey or its conditions
     */
    @UiField(provided = true)
    protected PerformanceAssessmentEditor performanceAssessmentEditor = new PerformanceAssessmentEditor();

    /** The panel that contains the controls used amongst all editors */
    @UiField
    FlowPanel commonEditorControls;

    /** The button that toggles whether advanced options are shown */
    @UiField
    Button advancedOptionsButton;

    /** The collapseable panel containing the advanced options */
    @UiField
    Collapse advancedOptions;

    /**
     * The checkbox that indicates if the default strategy handler should be
     * used
     */
    @UiField
    protected CheckBox defaultStrategyHandlerCheckBox;

    /** The container for the different strategy handler panels */
    @UiField
    protected DeckPanel strategyHandlerDeck;

    /**
     * The placeholder widget to use when no handler panel is to be displayed
     */
    @UiField
    protected Widget noHandlerPanel;

    /** The widget to use when the user wants to choose an action handler */
    @UiField
    protected Widget strategyHandlerPanel;

    /** The select widget that contains the list of available action handlers */
    @UiField
    protected Select strategyHandlerSelect;

    /** The table that contains the handler's parameter key/value pairs */
    @UiField
    protected DkfNameValuePairEditor nvPairEditor;

    /** The current strategy handler being used by the strategies */
    private StrategyHandler currentStrategyHandler;

    /**
     * The default class for the strategy handler. Nick: I don't like
     * hard-coding this, but I just can't find a good way to get the default
     * strategy handler class on the client side
     */
    private final static String DEFAULT_STRATEGY_HANDLER_CLASS = "domain.knowledge.strategy.DefaultStrategyHandler";

    /**
     * Constructs an item editor for editing a strategy activity.
     */
    public StrategyActivityEditor() {
        initWidget(uiBinder.createAndBindUi(this));
        initRibbon();
        nvPairEditor.setOnChangeCommand(new Command() {
            @Override
            public void execute() {
                if (currentStrategyHandler == null) {
                    return;
                }

                List<Nvpair> items = nvPairEditor.getItems();
                if (items.isEmpty()) {
                    currentStrategyHandler.setParams(null);
                } else {
                    currentStrategyHandler.setParams(new Params());
                    currentStrategyHandler.getParams().getNvpair().addAll(items);
                }
            }
        });

        instructionalInterventionEditor.addRibbonVisibilityChangedHandler(new RibbonVisibilityChangedHandler() {

            @Override
            public void onVisibilityChanged(boolean ribbonIsShown) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("instructionalInterventionEditor.onVisibilityChanged(" + ribbonIsShown + ")");
                }

                setSaveButtonVisible(!ribbonIsShown);
            }
        });

        for (ValidationComposite child : getChildren()) {
            child.setActive(false);
        }
    }

    /**
     * Initializes the {@link #strategyTypeRibbon}.
     */
    private void initRibbon() {
        
        strategyTypeRibbon.setTileHeight(105);
        
        strategyTypeRibbon
                .addRibbonItem(IconType.COMMENT, "Feedback", "Deliver a message to the learner", new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        showInstructionalInterventionEditor();
                    }
                });
        
        if(!GatClientUtility.isRtaLessonLevel() && ScenarioClientUtility.canTrainingAppUseMidLessonMedia()){
            // If LessonLevel is set to RTA, then there is not GIFT UI to present media.
            
            strategyTypeRibbon
                .addRibbonItem(IconType.FILE, "Present Media", "Delivers a media resource to the learner", new ClickHandler() {

                    @Override
                    public void onClick(ClickEvent event) {
                        showMidLessonMediaEditor();
                    }
                });
        }

        if (ScenarioClientUtility.canTrainingAppUseModifyScenario()) {
            strategyTypeRibbon
                    .addRibbonItem(IconType.TREE, "Modify Scenario", "Adapts the scenario the learner is in", new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    showScenarioAdaptationEditor();
                }
            });
        }

        if(!GatClientUtility.isRtaLessonLevel() && ScenarioClientUtility.canTrainingAppUseMidLessonMedia()){
            // If LessonLevel is set to RTA, then there is not GIFT UI to present survey.
            strategyTypeRibbon
                .addRibbonItem(IconType.PENCIL_SQUARE, "Present Survey", "Assesses the learner with a survey", new ClickHandler() {

                    @Override
                    public void onClick(ClickEvent event) {
                        performanceAssessmentEditor.showPerformanceNodePanel();
                        showPerformanceAssessmentEditor();
                    }
                });
        }

        if(!GatClientUtility.isRtaLessonLevel() && ScenarioClientUtility.canTrainingAppUseMidLessonConversation()){
            // If LessonLevel is set to RTA, then there is not GIFT UI to present conversation.
            strategyTypeRibbon
                .addRibbonItem(IconType.COMMENTS, "Start Conversation", "Starts an AutoTutor or Conversation Tree conversation with the learner", new ClickHandler() {

                    @Override
                    public void onClick(ClickEvent event) {
                        performanceAssessmentEditor.showConversationPanel();
                        showPerformanceAssessmentEditor();
                    }
                });
        }

    }

    /**
     * Shows the ribbon when the user clicks the changeTypeButton.
     *
     * @param event The event containing details about the click. Can't be null.
     */
    @UiHandler("changeTypeButton")
    protected void onChangeActivityButtonClicked(ClickEvent event) {
        showStrategyTypeRibbon();
    }

    /**
     * Toggles the visibility of the {@link #advancedOptions} panel when the
     * {@link #advancedOptionsButton} is clicked.
     *
     * @param event The event containing details about the click. Can't be null.
     */
    @UiHandler("advancedOptionsButton")
    protected void onAdvancedOptionsButtonClicked(ClickEvent event) {
        if (advancedOptions.isShown()) {
            advancedOptions.hide();
        } else {
            advancedOptions.show();
        }
    }

    /**
     * Handles when the user checks or unchecks the
     * {@link #defaultStrategyHandlerCheckBox}.
     *
     * @param event The event that contains information indicating whether the
     *        checkbox was checked or unchecked. Can't be null.
     */
    @UiHandler("defaultStrategyHandlerCheckBox")
    protected void onDefaultStrategyHandlerCheckboxChanged(ValueChangeEvent<Boolean> event) {
        if (event.getValue()) {
            strategyHandlerDeck.showWidget(strategyHandlerDeck.getWidgetIndex(noHandlerPanel));
        } else {
            strategyHandlerDeck.showWidget(strategyHandlerDeck.getWidgetIndex(strategyHandlerPanel));
            if (strategyHandlerSelect.getItemCount() != 0) {
                strategyHandlerSelect.setValue(DEFAULT_STRATEGY_HANDLER_CLASS);
                strategyHandlerSelect.refresh();
            }
        }

        /* no matter if checked or unchecked, the handlers get reset to
         * default */
        currentStrategyHandler.setImpl(DEFAULT_STRATEGY_HANDLER_CLASS);
    }

    /**
     * An event handler for when the type of strategy is changed from the UI.
     *
     * @param event The event containing the string value of a given
     *        {@link StrategyActionType}.
     */
    @UiHandler("strategyHandlerSelect")
    protected void onStrategyHandlerSelectChanged(ValueChangeEvent<String> event) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("onStrategyHandlerSelectChanged(" + event.getValue() + ")");
        }

        currentStrategyHandler.setImpl(event.getValue());
    }

    /**
     * Adds a performance node as an option to the
     * {@link #performanceAssessmentEditor}.
     *
     * @param taskOrConcept The {@link Task} or {@link Concept} to add to the
     *        {@link #performanceAssessmentEditor}. Can be null.
     */
    public void addPerformanceNode(final Serializable taskOrConcept) {
        if (taskOrConcept == null) {
            throw new IllegalArgumentException("The parameter 'taskOrConcept' cannot be null.");
        }

        performanceAssessmentEditor.addPerformanceNode(taskOrConcept);
    }

    /**
     * Removes a performance node as an option from the
     * {@link #performanceAssessmentEditor}.
     *
     * @param nodeId The {@link BigInteger} id of the performance node to remove
     *        from the {@link #performanceAssessmentEditor}. Can be null.
     */
    public void removePerformanceNode(final BigInteger nodeId) {
        if (nodeId == null) {
            throw new IllegalArgumentException("The parameter 'nodeId' cannot be null.");
        }

        performanceAssessmentEditor.removePerformanceNode(nodeId);
    }

    /**
     * Updates a {@link Task} or {@link Concept} within the
     * {@link #performanceAssessmentEditor} that was renamed.
     *
     * @param nodeId The {@link BigInteger} id of the {@link Task} or
     *        {@link Concept} to updated. Can't be null.
     * @param newName The new name for the specified {@link Task} or
     *        {@link Concept} as a result of the rename operation. Can't be
     *        null.
     */
    public void handleTaskOrConceptRename(final BigInteger nodeId, final String newName) {
        if (nodeId == null) {
            throw new IllegalArgumentException("The parameter 'nodeId' cannot be null.");
        } else if (newName == null) {
            throw new IllegalArgumentException("The parameter 'newName' cannot be null.");
        }

        performanceAssessmentEditor.handleTaskOrConceptRename(nodeId, newName);
    }
    
    /**
     * Updates a {@link HighlightObjects} within the {@link #scenarioAdaptationEditor} 
     * that was renamed.
     * 
     * @param oldName the old name of the highlight object.  Can be null.
     * @param newName the new name for the highlight object.  Can't be null.
     */
    public void handleHighlightObjectRename(final String oldName, final String newName){        
        scenarioAdaptationEditor.updateHighlightNamesList(oldName, newName);
    }

    /**
     * Setter the strategy for which activities are being edited.
     *
     * @param strategy The strategy for which activities are being edited. Can't
     *        be null.
     */
    public void setStrategy(final Strategy strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException("The parameter 'strategy' cannot be null.");
        }

        /* update editors with new strategy */
        mediaCollectionEditor.setStrategyBeingEdited(strategy);
        performanceAssessmentEditor.setStrategyBeingEdited(strategy);

        /* make sure we populate the performance nodes before we update the
         * strategy type. */
        performanceAssessmentEditor.populatePerformanceNodeChoices();
    }

    /**
     * Sets the choices that are available through the
     * {@link #strategyHandlerSelect}.
     *
     * @param handlerClassNames The list of class names that can be chosen
     *        through the {@link #strategyHandlerSelect}. Null is treated as an
     *        empty list.
     */
    public void setAvailableStrategyHandlers(List<String> handlerClassNames) {
        strategyHandlerSelect.clear();

        if (handlerClassNames != null) {
            for (String className : handlerClassNames) {
                Option option = new Option();
                option.setText(className);
                option.setValue(className);
                strategyHandlerSelect.add(option);
            }
        }

        strategyHandlerSelect.refresh();
    }

    /**
     * Populates the UI with data from the current strategy handler being edited
     *
     * @param strategyHandler the strategy handler data used to populate the
     *        handler UI
     */
    private void populateStrategyHandler(StrategyHandler strategyHandler) {
        if (strategyHandler == null) {
            strategyHandler = new StrategyHandler();
        }

        currentStrategyHandler = strategyHandler;

        if (StringUtils.isBlank(strategyHandler.getImpl())) {
            strategyHandler.setImpl(DEFAULT_STRATEGY_HANDLER_CLASS);
        }

        if (strategyHandlerSelect.getItemCount() != 0) {
            strategyHandlerSelect.setValue(strategyHandler.getImpl());
            strategyHandlerSelect.refresh();
        }

        if (StringUtils.equals(strategyHandler.getImpl(), DEFAULT_STRATEGY_HANDLER_CLASS)) {
            defaultStrategyHandlerCheckBox.setValue(true);
            strategyHandlerDeck.showWidget(strategyHandlerDeck.getWidgetIndex(noHandlerPanel));
        } else {
            defaultStrategyHandlerCheckBox.setValue(false);
            strategyHandlerDeck.showWidget(strategyHandlerDeck.getWidgetIndex(strategyHandlerPanel));
        }

        List<Nvpair> params = new ArrayList<>();
        if (strategyHandler.getParams() != null) {
            params.addAll(strategyHandler.getParams().getNvpair());
        }

        nvPairEditor.setNameValueList(params);
    }

    /**
     * Enable validations for the provided editor while clearing and disabling
     * validations from the other children.
     *
     * @param editor the editor to allow validations
     */
    private void updateValidationForEditor(ScenarioValidationComposite editor) {
        for (ValidationComposite validationChild : getChildren()) {
            if (validationChild.equals(editor)) {
                // mark active before validating
                validationChild.setActive(true);
                validationChild.validateAll();
            } else {
                // clear validations before marking as deactivated
                validationChild.clearValidations();
                validationChild.setActive(false);
            }
        }
    }

    @Override
    public void getValidationStatuses(final Set<ValidationStatus> validationStatuses) {
        /* There are no ValidationStatuses on this ValidationComposite. There
         * are only ValidationStatuses on its children. */
    }

    @Override
    public void validate(final ValidationStatus validationStatus) {
        /* Since there are no ValidationStatuses on this ValidationComposite.
         * There is nothing that needs to be validated */
    }

    @Override
    protected boolean validate(StrategyActivityWrapper activityWrapper) {
        final Serializable activity = activityWrapper.getActivity();
        if (activity == null) {
            return false;
        }

        String errorMsg = ScenarioValidatorUtility.validateStrategyActivity(activity);
        return StringUtils.isBlank(errorMsg);
    }

    @Override
    protected void populateEditor(final StrategyActivityWrapper wrapper) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("populateEditor(" + wrapper + ")");
        }

        /* Verify that the wrapper is not null. */
        if (wrapper == null) {
            throw new IllegalArgumentException("The parameter 'wrapper' cannot be null.");
        }

        /* Determine the type of the activity and forward it to a more specific
         * method. If it is not of an expected type, throw an exception. */
        final Serializable obj = wrapper.getActivity();
        if (obj == null) {
            showStrategyTypeRibbon();
            populateStrategyHandler(new StrategyHandler());
        } else if (obj instanceof InstructionalIntervention) {
            populateEditor((InstructionalIntervention) obj);
        } else if (obj instanceof MidLessonMedia) {
            populateEditor((MidLessonMedia) obj);
        } else if (obj instanceof PerformanceAssessment) {
            populateEditor((PerformanceAssessment) obj);
        } else if (obj instanceof ScenarioAdaptation) {
            populateEditor((ScenarioAdaptation) obj);
        } else {
            final String unsupportedType = obj.getClass().getSimpleName();
            throw new IllegalArgumentException("An object of type '" + unsupportedType + "' is not editable.");
        }
    }

    /**
     * Populates and shows the {@link #instructionalInterventionEditor}.
     *
     * @param obj The {@link InstructionalIntervention} object with which to
     *        populate the editor.
     */
    private void populateEditor(InstructionalIntervention obj) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("populateEditor(" + obj + ")");
        }

        instructionalInterventionEditor.populateInstructionalIntervention(obj);
        populateStrategyHandler(obj.getStrategyHandler());
        showInstructionalInterventionEditor();
    }

    /**
     * Populates and shows the {@link #mediaCollectionEditor}.
     *
     * @param obj The {@link MidLessonMedia} object with which to populate the
     *        editor.
     */
    private void populateEditor(MidLessonMedia obj) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("populateEditor(" + obj + ")");
        }

        mediaCollectionEditor.populateMidLessonMedia(obj);
        populateStrategyHandler(obj.getStrategyHandler());
        showMidLessonMediaEditor();
    }

    /**
     * Populates and shows the {@link #performanceAssessmentEditor}.
     *
     * @param obj The {@link PerformanceAssessment} object with which to
     *        populate the editor.
     */
    private void populateEditor(PerformanceAssessment obj) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("populateEditor(" + obj + ")");
        }

        performanceAssessmentEditor.populatePerformanceAssessment(obj);
        populateStrategyHandler(obj.getStrategyHandler());
        showPerformanceAssessmentEditor();
    }

    /**
     * Populates and shows the {@link #scenarioAdaptationEditor}.
     *
     * @param obj The {@link ScenarioAdaptation} object with which to populate
     *        the editor.
     */
    private void populateEditor(ScenarioAdaptation obj) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("populateEditor(" + obj + ")");
        }

        scenarioAdaptationEditor.populateEditor(obj);
        populateStrategyHandler(obj.getStrategyHandler());
        showScenarioAdaptationEditor();
    }

    @Override
    protected void applyEdits(StrategyActivityWrapper wrapper) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("applyEdits(" + wrapper + ")");
        }

        int deckIndex = strategyTypeDeck.getVisibleWidget();
        Serializable activity = null;
        if (deckIndex == strategyTypeDeck.getWidgetIndex(strategyTypeRibbon)) {
            activity = null;
        } else if (deckIndex == strategyTypeDeck.getWidgetIndex(instructionalInterventionEditor)) {
            InstructionalIntervention instructionalIntervention = instructionalInterventionEditor.getInstructionalIntervention();
            instructionalIntervention.setStrategyHandler(currentStrategyHandler);
            activity = instructionalIntervention;
        } else if (deckIndex == strategyTypeDeck.getWidgetIndex(mediaCollectionEditor)) {
            MidLessonMedia midLessonMedia = new MidLessonMedia();
            midLessonMedia.setStrategyHandler(currentStrategyHandler);
            mediaCollectionEditor.applyEdits(midLessonMedia);
            activity = midLessonMedia;
        } else if (deckIndex == strategyTypeDeck.getWidgetIndex(scenarioAdaptationEditor)) {
            ScenarioAdaptation scenarioAdaptation = new ScenarioAdaptation();
            scenarioAdaptationEditor.applyEdits(scenarioAdaptation);
            scenarioAdaptation.setStrategyHandler(currentStrategyHandler);
            activity = scenarioAdaptation;
        } else if (deckIndex == strategyTypeDeck.getWidgetIndex(performanceAssessmentEditor)) {
            PerformanceAssessment perfAssess = new PerformanceAssessment();
            perfAssess.setStrategyHandler(currentStrategyHandler);
            performanceAssessmentEditor.applyEdits(perfAssess);
            activity = perfAssess;
        } else {
            throw new UnsupportedOperationException("An index of '" + deckIndex + "' was unhandled");
        }

        wrapper.setActivity(activity);
    }

    /**
     * Shows the {@link #strategyTypeRibbon} and hides all strategy editors.
     */
    private void showStrategyTypeRibbon() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("showStrategyTypeRibbon()");
        }

        showEditor(null);
        strategyTypeDeck.showWidget(strategyTypeDeck.getWidgetIndex(strategyTypeRibbon));
        commonEditorControls.setVisible(false);
        setSaveButtonVisible(false);

        /* Reset the editors. */
        instructionalInterventionEditor.resetEditor();
        mediaCollectionEditor.resetEditor();
        scenarioAdaptationEditor.resetEditor();
        performanceAssessmentEditor.resetEditor();
    }

    /**
     * Called to show a given editor.
     *
     * @param editor The editor to show. Null will not show an editor.
     */
    private void showEditor(ScenarioValidationComposite editor) {
        if (logger.isLoggable(Level.FINE)) {
            String editorName = editor != null ? editor.getClass().getSimpleName() : "null";
            logger.fine("showEditor(" + editorName + ")");
        }

        /* Reset the common editor controls */
        commonEditorControls.setVisible(true);
        advancedOptions.hide();

        for (ValidationComposite child : getChildren()) {
            child.clearValidations();
            child.setActive(false);
        }

        setSaveButtonVisible(true);

        if (editor != null) {
            editor.setActive(true);
            strategyTypeDeck.showWidget(strategyTypeDeck.getWidgetIndex(editor));
            updateValidationForEditor(editor);
        }
    }

    /**
     * Shows the {@link #instructionalInterventionEditor} and hides all other
     * strategy editors and the ribbon.
     */
    private void showInstructionalInterventionEditor() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("showInstructionalInterventionEditor()");
        }

        showEditor(instructionalInterventionEditor);
        setSaveButtonVisible(instructionalInterventionEditor.isFeedbackTypeSelected());
    }

    /**
     * Shows the {@link #mediaCollectionEditor} and hides all other strategy
     * editors and the ribbon.
     */
    private void showMidLessonMediaEditor() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("showMidLessonMediaEditor()");
        }

        showEditor(mediaCollectionEditor);
    }

    /**
     * Shows the {@link #performanceAssessmentEditor} and hides all other
     * strategy editors and the ribbon.
     */
    private void showPerformanceAssessmentEditor() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("showPerformanceAssessmentEditor()");
        }

        showEditor(performanceAssessmentEditor);
    }

    /**
     * Shows the {@link #scenarioAdaptationEditor} and hides all other strategy
     * editors and the ribbon.
     */
    private void showScenarioAdaptationEditor() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("showScenarioAdaptationEditor()");
        }

        showEditor(scenarioAdaptationEditor);
    }

    @Override
    protected void setReadonly(final boolean isReadonly) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("setReadonly(" + isReadonly + ")");
        }

        defaultStrategyHandlerCheckBox.setEnabled(!isReadonly);
        nvPairEditor.setEnabled(!isReadonly);
        instructionalInterventionEditor.setReadonly(isReadonly);
        scenarioAdaptationEditor.setReadonly(isReadonly);
        performanceAssessmentEditor.setReadonly(isReadonly);
        mediaCollectionEditor.setReadonly(isReadonly);
        changeTypeButton.setVisible(!isReadonly);
    }

    @Override
    public void addValidationCompositeChildren(final Set<ValidationComposite> childValidationComposites) {
        childValidationComposites.add(instructionalInterventionEditor);
        childValidationComposites.add(mediaCollectionEditor);
        childValidationComposites.add(performanceAssessmentEditor);
        childValidationComposites.add(scenarioAdaptationEditor);
    }
}