/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.extras.select.client.ui.Option;
import org.gwtbootstrap3.extras.select.client.ui.Select;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;

import generated.dkf.Assessments;
import generated.dkf.Concept;
import generated.dkf.Question;
import generated.dkf.Questions;
import generated.dkf.Reply;
import generated.dkf.Scenario;
import generated.dkf.Task;
import generated.dkf.Assessments.ConditionAssessment;
import generated.dkf.Assessments.Survey;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.gwt.client.validation.WidgetValidationStatus;
import mil.arl.gift.common.survey.AbstractQuestion;
import mil.arl.gift.common.survey.AbstractSurveyElement;
import mil.arl.gift.common.survey.AbstractSurveyQuestion;
import mil.arl.gift.common.survey.ListOption;
import mil.arl.gift.common.survey.MatrixOfChoicesQuestion;
import mil.arl.gift.common.survey.MultipleChoiceQuestion;
import mil.arl.gift.common.survey.RatingScaleQuestion;
import mil.arl.gift.common.survey.SurveyContextSurvey;
import mil.arl.gift.common.survey.SurveyPage;
import mil.arl.gift.common.survey.Survey.SurveyTypeEnum;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.GatClientBundle;
import mil.arl.gift.tools.authoring.server.gat.client.model.record.AssessmentRecord;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.ScenarioSurveyResources;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.ScenarioEventUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.SurveyPicker;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.cell.ExtendedSelectionCell;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.SelectQuestionDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.WarningDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.validation.ScenarioValidationComposite;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.FetchSurveyContextSurveysResult;

/**
 * This widget shows the performance node assessment authoring UI including the 
 * ability to author a mid lesson survey.
 * 
 * @author mhoffman
 *
 */
public class PerfNodeAssessmentSelectorWidget extends ScenarioValidationComposite {
    
    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(PerfNodeAssessmentSelectorWidget.class.getName());
    
    /** The UiBinder that combines the ui.xml with this java class */
    private static PerfNodeSurveyAssessmentWidgetUiBinder uiBinder = GWT.create(PerfNodeSurveyAssessmentWidgetUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface PerfNodeSurveyAssessmentWidgetUiBinder extends UiBinder<Widget, PerfNodeAssessmentSelectorWidget> {
    }
    
    /**
     * The Interface GetValue.
     *
     * @param <C> the generic type
     */
    private static interface GetValue<C> {

        /**
         * Gets the value.
         *
         * @param record the record
         * @return the value
         */
        C getValue(AssessmentRecord record);
    }
    
    /**
     * The select widget that contains the different assessment types that the task/concept can
     * perform
     */
    @UiField
    protected Select assessmentTypeSelect;
    
    @UiField
    protected FlowPanel assessmentTypeSelectPanel;
    
    /** The panel that contains the survey selection */
    @UiField
    protected FlowPanel surveyAssessmentPanel;

    /** A picker widget that allows the author to either create a survey to use or select an existing one */
    @UiField
    protected SurveyPicker surveyPicker;
    
    @UiField
    protected HTML additionalAssessmentLabel;

    /**
     * The panel that contains the survey questions to be assessed. This is only here for LEGACY
     * purposes.
     */
    @UiField
    protected FlowPanel surveyQuestionAssessmentPanel;

    /** The button to add a survey question assessment */
    @UiField(provided = true)
    protected Image addQuestionButton = new Image(GatClientBundle.INSTANCE.add_image());

    /** The table that contains the survey question assessment records */
    @UiField
    protected CellTable<AssessmentRecord> questionAssessmentDataDisplay;

    /** The button to delete a survey question assessment */
    @UiField
    protected com.google.gwt.user.client.ui.Button deleteQuestionButton;
    
    /** Help label for explaining about how notifying conditions works */
    @UiField
    protected Label conditionAssessmentHelp;
    
    /** The dialog to select a survey question to be assessed */
    private final SelectQuestionDialog selectQuestionDialog = new SelectQuestionDialog();

    /** The question assessment data provider. */
    private final ListDataProvider<AssessmentRecord> questionAssessmentDataProvider = new ListDataProvider<AssessmentRecord>();

    /** The question assessment selection model. */
    private final MultiSelectionModel<AssessmentRecord> questionAssessmentSelectionModel = new MultiSelectionModel<AssessmentRecord>();
    
    /** The container for showing validation messages for the survey picker. */
    private final WidgetValidationStatus surveyPickerValidationStatus;
    
    /**
     * Flag to indicate if the page is currently being initialized. We want to disable some events
     * on init.
     */
    private boolean performingSetup = false;
    
    /** Defines the type of additional assessments to author */
    private static enum AdditionalAssessmentType {
        /** Option to have no additional assessment */
        DO_NOTHING("do nothing"),
        /** Option to have an additional assessment using a survey */
        SURVEY("present a knowledge assessment survey"),
        /** Option to have an additional assessment using its conditions */
        CONDITIONS("notify conditions about the request");

        /** The description of the assessment type */
        String description;

        /**
         * Constructs the {@link AdditionalAssessmentType} with the given description.
         * 
         * @param description The description to use with this {@link AdditionalAssessmentType}
         */
        private AdditionalAssessmentType(String description) {
            if (description == null) {
                throw new IllegalArgumentException("The parameter 'description' cannot be null.");
            }

            this.description = description;
        }

        /**
         * Returns the text description of the assessment type
         * 
         * @return The description as a String, can't be null.
         */
        public String getDescription() {
            return description;
        }
    }
    
    /** The {@link Task} or {@link Concept} that the assessment is being created for */
    private Serializable currentTaskOrConcept;

    /**
     * This is to maintain the legacy survey question assessments. This should only be non-null if
     * we are editing a task/concept that already contains a survey with question assessments.
     */
    private String legacySurveyWithQuestions = null;

    /** The read-only flag */
    private boolean isReadOnly = false;
    
    public PerfNodeAssessmentSelectorWidget(){
        
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(".ctor()");
        }

        initWidget(uiBinder.createAndBindUi(this));
        
        // hide by default
        conditionAssessmentHelp.setVisible(false);
        
        surveyPickerValidationStatus = new WidgetValidationStatus(surveyPicker.getPanelWidget(),
                "You must select a survey to be assessed or change assessment type.");
        
        for (AdditionalAssessmentType assessmentType : AdditionalAssessmentType.values()) {

            // MH 9.13.18 - not adding 'conditions' as there has yet to be a single implementation
            // of a condition class providing another or different assessment on request.
            if (assessmentType == AdditionalAssessmentType.CONDITIONS) {
                continue;
            }

            Option option = new Option();
            option.setText(assessmentType.getDescription());
            option.setValue(assessmentType.name());
            assessmentTypeSelect.add(option);
        }
        
        surveyPicker.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                requestValidationAndFireDirtyEvent(currentTaskOrConcept, surveyPickerValidationStatus);
            }
        });

        initSurveyQuestionPanel();
    }
    
    /**
     * Set the visibility of the assessment explanation label.
     * 
     * @param visible true if the label should be shown.
     */
    public void setAdditionalAssessmentLabelVisibility(boolean visible){
        additionalAssessmentLabel.setVisible(visible);
    }
    
    /**
     * Set the visibility of the assessment type select component.  This component
     * contains choices like 'survey' and 'do nothing'.
     * @param visible true if the assessment type component should be visible.
     */
    public void setAssessmentTypeSelectorVisiblity(boolean visible){
        // the Select widget setVisible doesn't hide the component, therefore
        // it is wrapped in a panel that is then hidden.
        assessmentTypeSelectPanel.setVisible(visible);
    }
    
    /**
     * Return the current gift survey key of the survey associated with the survey picker.
     * 
     * @return can be null or empty string if there is no survey picked yet.
     */
    public String getSurveyGIFTKey(){
        return surveyPicker.getValue();
    }
    
    /**
     * Handles the selection changes for the additional assessment types. This will change which
     * panels are visible to the user.
     * 
     * @param event the value change event containing the new selection
     * @throws UnsupportedOperationException if the assessment type is unknown
     */
    @UiHandler("assessmentTypeSelect")
    protected void onAssessmentTypeSelectChanged(ValueChangeEvent<String> event) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("onAssessmentTypeSelectChanged.onValueChange(" + event.toDebugString() + ")");
        }

        AdditionalAssessmentType assessmentType = AdditionalAssessmentType.valueOf(event.getValue());
        switch (assessmentType) {
        case DO_NOTHING:
            if(logger.isLoggable(Level.INFO)){
                logger.info("Showing assessment panel for "+AdditionalAssessmentType.DO_NOTHING+" assessment type.");
            }
            surveyAssessmentPanel.setVisible(false);
            surveyQuestionAssessmentPanel.setVisible(false);
            conditionAssessmentHelp.setVisible(false);
            break;
        case CONDITIONS:
            if(logger.isLoggable(Level.INFO)){
                logger.info("Showing assessment panel for "+AdditionalAssessmentType.CONDITIONS+" assessment type.");
            }
            surveyAssessmentPanel.setVisible(false);
            surveyQuestionAssessmentPanel.setVisible(false);
            conditionAssessmentHelp.setVisible(true);
            break;
        case SURVEY:
            if(logger.isLoggable(Level.INFO)){
                logger.info("Showing assessment panel for "+AdditionalAssessmentType.SURVEY+" assessment type.");
            }
            surveyAssessmentPanel.setVisible(true);
            surveyQuestionAssessmentPanel.setVisible(isSurveyLegacyWithQuestions(surveyPicker.getValue()));
            conditionAssessmentHelp.setVisible(false);
            break;
        default:
            throw new UnsupportedOperationException(
                    "Found an unknown assessment type: '" + assessmentType.getDescription() + "'.");
        }
        
        updateTaskOrConceptAssessment(assessmentType);
    }
    
    /**
     * Updates the {@link Task} or {@link Concept} with the correct assessment based on the
     * populated panel fields. This should be called whenever anything changes within the additional
     * assessment panel.
     * 
     * @param assessmentType the {@link AdditionalAssessmentType} specifying which type of
     *        assessment to build
     * @throws UnsupportedOperationException if the assessment type is unknown
     */
    public void updateTaskOrConceptAssessment(AdditionalAssessmentType assessmentType) {
        
        if(logger.isLoggable(Level.INFO)){
            logger.info("updateTaskOrConceptAssessment ( "+assessmentType+" )");
        }

        // clear out previous task/concept's assessments.
        clearTaskOrConceptAssessment();

        // build assessment
        Serializable surveyOrConditionAssessment = null;
        switch (assessmentType) {
        case DO_NOTHING:
            // if do nothing is chosen, then no need to populate the assessment
            break;
        case SURVEY:
            Survey survey = new Survey();
            if (surveyPicker.getValue() != null) {
                survey.setGIFTSurveyKey(surveyPicker.getValue());
                
                if(logger.isLoggable(Level.INFO)){
                    logger.info("created new Survey object for task/concept and set the gift survey key to "+surveyPicker.getValue()+
                            " which was found in the survey picker.  SurveyQuestionAssessmentPanel visible = "+surveyQuestionAssessmentPanel.isVisible()+
                            ", "+questionAssessmentDataProvider.getList().size()+" questions in data provider.");
                }

                if (surveyQuestionAssessmentPanel.isVisible()) {
                    survey.setQuestions(new Questions());
                    
                    if(logger.isLoggable(Level.INFO)){
                        logger.info("Adding "+questionAssessmentDataProvider.getList().size()+" questions to question assessment panel.");
                    }
                    for (AssessmentRecord record : questionAssessmentDataProvider.getList()) {
                        if (record.getQuestionOrReply() instanceof Question) {
                            survey.getQuestions().getQuestion().add((Question) record.getQuestionOrReply());
                        }
                    }
                }
            }

            surveyOrConditionAssessment = survey;
            break;
        case CONDITIONS:
            surveyOrConditionAssessment = new ConditionAssessment();
            break;
        default:
            throw new UnsupportedOperationException(
                    "Found an unknown assessment type: '" + assessmentType.getDescription() + "'.");
        }

        // update new task/concept's assessments.
        if (surveyOrConditionAssessment != null) {
            if (currentTaskOrConcept instanceof Task) {
                Task task = (Task) currentTaskOrConcept;
                if (task.getAssessments() == null) {
                    task.setAssessments(new Assessments());
                }

                if(logger.isLoggable(Level.INFO)){
                    logger.info("adding additional assessment to task "+task.getName()+" of type "+surveyOrConditionAssessment);
                }
                task.getAssessments().getAssessmentTypes().add(surveyOrConditionAssessment);
            } else if (currentTaskOrConcept instanceof Concept) {
                Concept concept = (Concept) currentTaskOrConcept;
                if (concept.getAssessments() == null) {
                    concept.setAssessments(new Assessments());
                }

                if(logger.isLoggable(Level.INFO)){
                    logger.info("adding additional assessment to concept "+concept.getName()+" of type "+surveyOrConditionAssessment);
                }
                concept.getAssessments().getAssessmentTypes().add(surveyOrConditionAssessment);
            }
        }

        // do not fire dirty event if we are populating the page
        if (performingSetup) {
            requestValidation(surveyPickerValidationStatus);
        }else {
            if(logger.isLoggable(Level.INFO)){
                logger.info("updateTaskOrConceptAssessment calling requestValidationAndFireDirtyEvent on "+currentTaskOrConcept);
            }
            requestValidationAndFireDirtyEvent(currentTaskOrConcept, surveyPickerValidationStatus);
        }
    }
    
    /**
     * Return the current task/concept node selected to have an additional assessment (e.g. mid lesson survey)
     * assigned to it.
     * 
     * @return can be null if a task/concept has not been selected/set yet.
     */
    public Serializable getTaskOrConcept(){
        return currentTaskOrConcept;
    }
    
    /**
     * Checks if the provided survey key is a legacy survey with questions to assess.
     * 
     * @param giftSurveyKey the GIFT survey key to check
     * @return true if the provided GIFT survey key matches a legacy survey with questions to
     *         assess.
     */
    private boolean isSurveyLegacyWithQuestions(String giftSurveyKey) {
        return StringUtils.isNotBlank(giftSurveyKey) && StringUtils.equals(legacySurveyWithQuestions, giftSurveyKey);
    }
    
    /**
     * Populates the survey question panel with the data provided in the survey assessment.
     * 
     * @param surveyAssessment the survey assessment used to populate the panel fields.
     * @param giftSurveys the list of available GIFT course surveys.
     */
    private void populateSurveyQuestionPanel(Survey surveyAssessment, List<SurveyContextSurvey> giftSurveys) {
        
        if(logger.isLoggable(Level.INFO)){
            logger.info("populateSurveyQuestionPanel ( " +surveyAssessment.getGIFTSurveyKey()+" , "+giftSurveys+" )");
        }
        if (surveyAssessment == null) {
            throw new IllegalArgumentException("The parameter 'surveyAssessment' cannot be null.");
        }

        questionAssessmentDataProvider.getList().clear();

        if (surveyAssessment.getQuestions() == null || surveyAssessment.getQuestions().getQuestion().isEmpty()) {
            if(logger.isLoggable(Level.INFO)){
                logger.info("cleared question assessment data/panel and the survey assessment has no questions.");
            }
            questionAssessmentDataProvider.refresh();
            return;
        }

        legacySurveyWithQuestions = surveyAssessment.getGIFTSurveyKey();
        SurveyContextSurvey foundSurvey = null;
        for (SurveyContextSurvey giftSurveyContextSurvey : giftSurveys) {
            if (StringUtils.equals(surveyAssessment.getGIFTSurveyKey(), giftSurveyContextSurvey.getKey())) {
                foundSurvey = giftSurveyContextSurvey;
                break;
            }
        }
        
        if(logger.isLoggable(Level.INFO)){
            logger.info("populating question table with "+surveyAssessment.getQuestions().getQuestion().size()+" questions.");
        }

        for (Question question : surveyAssessment.getQuestions().getQuestion()) {
            AbstractQuestion foundQuestion = null;
            if (foundSurvey != null && foundSurvey.getSurvey() != null && question.getKey() != null) {
                for (SurveyPage page : foundSurvey.getSurvey().getPages()) {
                    for (AbstractSurveyElement element : page.getElements()) {
                        if (element instanceof AbstractSurveyQuestion<?>) {

                            AbstractQuestion giftQuestion = ((AbstractSurveyQuestion<?>) element).getQuestion();
                            if (question.getKey().intValue() == element.getId()) {
                                AssessmentRecord record = new AssessmentRecord(question, giftQuestion.getText());
                                questionAssessmentDataProvider.getList().add(record);

                                foundQuestion = giftQuestion;
                                break;
                            }
                        }
                    }

                    if (foundQuestion != null) {
                        break;
                    }
                }
            }

            List<ListOption> listOptionList = new ArrayList<ListOption>();
            if (foundQuestion == null) {

                SafeHtmlBuilder sb = new SafeHtmlBuilder();
                sb.appendHtmlConstant("<div class='warningLabel'><u>Invalid</u><br/>Not present in database.</div>");

                AssessmentRecord record = new AssessmentRecord(question,
                        "Question ID = " + (question.getKey() != null ? question.getKey().toString() : "None"),
                        sb.toSafeHtml());

                questionAssessmentDataProvider.getList().add(record);
            } else {
                if (foundQuestion instanceof MultipleChoiceQuestion) {
                    listOptionList
                            .addAll(((MultipleChoiceQuestion) foundQuestion).getReplyOptionSet().getListOptions());
                } else if (foundQuestion instanceof MatrixOfChoicesQuestion) {
                    listOptionList
                            .addAll(((MatrixOfChoicesQuestion) foundQuestion).getColumnOptions().getListOptions());
                    listOptionList.addAll(((MatrixOfChoicesQuestion) foundQuestion).getRowOptions().getListOptions());
                } else if (foundQuestion instanceof RatingScaleQuestion) {
                    listOptionList.addAll(((RatingScaleQuestion) foundQuestion).getReplyOptionSet().getListOptions());
                }
                
                if(logger.isLoggable(Level.INFO)){
                    logger.info("populating question entry in table with "+question.getReply().size()+" replies that are assessed");
                }

                for (Reply reply : question.getReply()) {
                    boolean foundReply = false;

                    if (reply.getKey() != null) {
                        for (ListOption listOption : listOptionList) {
                            if (reply.getKey().intValue() == listOption.getId()) {
                                AssessmentRecord replyRecord = new AssessmentRecord(reply, listOption.getText());
                                questionAssessmentDataProvider.getList().add(replyRecord);
                                foundReply = true;
                                break;
                            }
                        }
                    }

                    if (!foundReply) {
                        SafeHtmlBuilder sb = new SafeHtmlBuilder();
                        sb.appendHtmlConstant(
                                "<div class='warningLabel'><u>Invalid</u><br/>Not present in database.</div>");

                        AssessmentRecord replyRecord = new AssessmentRecord(reply,
                                "Reply ID = " + (reply.getKey() != null ? reply.getKey().toString() : "None"),
                                sb.toSafeHtml());

                        questionAssessmentDataProvider.getList().add(replyRecord);
                    }
                }
            }
        }
        
        if(logger.isLoggable(Level.INFO)){
            logger.info("refreshing question assessment data provider now");
        }

        questionAssessmentDataProvider.refresh();
    }

    /**
     * Adds a question to a survey assessment.
     * 
     * @param surveyQuestion the question to add.
     */
    private void addQuestion(AbstractSurveyQuestion<?> surveyQuestion) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("addQuestion(" + surveyQuestion + ")");
        }

        if (surveyQuestion == null) {
            throw new IllegalArgumentException("The parameter 'surveyQuestion' cannot be null.");
        } else if (surveyQuestion.getQuestion() == null) {
            throw new IllegalArgumentException("The question within parameter 'surveyQuestion' cannot be null.");
        }

        final Serializable assessment;

        // exit cases
        if (currentTaskOrConcept == null) {
            return;
        } else if (currentTaskOrConcept instanceof Task) {
            Task task = (Task) currentTaskOrConcept;
            if (task.getAssessments() == null || task.getAssessments().getAssessmentTypes().isEmpty()) {
                return;
            }
            assessment = task.getAssessments().getAssessmentTypes().get(0);
        } else if (currentTaskOrConcept instanceof Concept) {
            Concept concept = (Concept) currentTaskOrConcept;
            if (concept.getAssessments() == null || concept.getAssessments().getAssessmentTypes().isEmpty()) {
                return;
            }
            assessment = concept.getAssessments().getAssessmentTypes().get(0);
        } else {
            return;
        }

        if (assessment instanceof Survey) {

            Survey survey = (Survey) assessment;
            AbstractQuestion question = surveyQuestion.getQuestion();

            if (question instanceof MultipleChoiceQuestion || question instanceof MatrixOfChoicesQuestion
                    || question instanceof RatingScaleQuestion) {

                Question addQuestion = new Question();
                addQuestion.setKey(BigInteger.valueOf(surveyQuestion.getId()));

                if (survey.getQuestions() == null) {
                    survey.setQuestions(new Questions());
                }

                ((Survey) assessment).getQuestions().getQuestion().add(addQuestion);

                AssessmentRecord record = new AssessmentRecord(addQuestion, question.getText());
                questionAssessmentDataProvider.getList().add(record);

                List<ListOption> listOptionList = new ArrayList<ListOption>();

                if (question instanceof MultipleChoiceQuestion) {
                    listOptionList.addAll(((MultipleChoiceQuestion) question).getReplyOptionSet().getListOptions());
                } else if (question instanceof MatrixOfChoicesQuestion) {
                    listOptionList.addAll(((MatrixOfChoicesQuestion) question).getColumnOptions().getListOptions());
                    listOptionList.addAll(((MatrixOfChoicesQuestion) question).getRowOptions().getListOptions());
                } else if (question instanceof RatingScaleQuestion) {
                    listOptionList.addAll(((RatingScaleQuestion) question).getReplyOptionSet().getListOptions());
                }

                for (ListOption listOption : listOptionList) {

                    Reply reply = new Reply();

                    reply.setKey(BigInteger.valueOf(listOption.getId()));
                    reply.setResult(AssessmentLevelEnum.VALUES().get(0).getDisplayName());

                    addQuestion.getReply().add(reply);

                    AssessmentRecord replyRecord = new AssessmentRecord(reply, listOption.getText());
                    questionAssessmentDataProvider.getList().add(replyRecord);
                }

                questionAssessmentDataProvider.refresh();
            }
        } else {
            logger.warning("Tried to add question when assessment is not of type 'Survey'.");
        }
    }
    
    /**
     * Removes the assessments from the {@link Task} or {@link Concept}.
     */
    private void clearTaskOrConceptAssessment() {
        
        if(logger.isLoggable(Level.INFO)){
            logger.info("clearTaskOrConceptAssessment on "+currentTaskOrConcept);
        }
        
        if (currentTaskOrConcept instanceof Task) {
            Task task = (Task) currentTaskOrConcept;
            if (task.getAssessments() != null) {
                task.getAssessments().getAssessmentTypes().clear();
            }
        } else if (currentTaskOrConcept instanceof Concept) {
            Concept concept = (Concept) currentTaskOrConcept;
            if (concept.getAssessments() != null) {
                concept.getAssessments().getAssessmentTypes().clear();
            }
        }
    }
    
    /**
     * Finishes showing the additional assessments. This is broken out into a separate method so
     * that the common code can be called when the assessment type is finished loading its data.
     * (e.g. Survey performs an async call, so we need to wait until it's complete)
     * 
     * @param assessmentType the assessment type to show
     */
    private void finishShowAdditionalAssessments(AdditionalAssessmentType assessmentType) {

        if(logger.isLoggable(Level.INFO)){
            logger.info("finishShowAdditionalAssessments ( "+assessmentType+" )");
        }
        
        assessmentTypeSelect.setValue(assessmentType.name());
        // force value change event to fire with new value
        ValueChangeEvent.fire(assessmentTypeSelect, assessmentType.name());

        updateTaskOrConceptAssessment(assessmentType);

        // finished populating the page
        performingSetup = false;
        
        if(logger.isLoggable(Level.INFO)){
            logger.info("finishShowAdditionalAssessments - performingSetup now false");
        }
    }
    
    /**
     * Loads the given survey context into this widget's survey assessment panel
     * 
     * @param surveyContextId the ID of the survey context to load
     */
    public void loadSurveyContext(BigInteger surveyContextId) {
        surveyPicker.setSurveyContextId(surveyContextId);
    }

    /**
     * Initializes the survey question panel
     */
    private void initSurveyQuestionPanel() {

        // limit authors to only use knowledge assessment surveys for survey assessments
        surveyPicker.setTargetSurveyType(SurveyTypeEnum.ASSESSLEARNER_STATIC);
        surveyPicker.addValueChangeHandler(new ValueChangeHandler<String>() {

            @Override
            public void onValueChange(ValueChangeEvent<String> event) {

                selectQuestionDialog.setSurveyGIFTKey(event.getValue());
                surveyQuestionAssessmentPanel.setVisible(isSurveyLegacyWithQuestions(event.getValue()));

                updateTaskOrConceptAssessment(AdditionalAssessmentType.SURVEY);
            }
        });

        addQuestionButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                // do nothing on click if read-only
                if (isReadOnly) {
                    return;
                }

                Scenario scenario = ScenarioClientUtility.getScenario();
                if (scenario != null && scenario.getResources() != null) {
                    selectQuestionDialog.setSurveyContextId(scenario.getResources().getSurveyContext());
                } else {
                    selectQuestionDialog.setSurveyContextId(null);
                }

                selectQuestionDialog.setValue(null);
                selectQuestionDialog.center();
            }
        });

        selectQuestionDialog.addValueChangeHandler(new ValueChangeHandler<AbstractSurveyQuestion<?>>() {
            @Override
            public void onValueChange(ValueChangeEvent<AbstractSurveyQuestion<?>> event) {
                if (event.getValue() == null) {
                    WarningDialog.error("Selection missiong", "Please select a question to add.");
                }

                addQuestion(event.getValue());
                selectQuestionDialog.hide();

                // fire event but validation isn't required
                ScenarioEventUtility.fireDirtyEditorEvent();
            }
        });

        deleteQuestionButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent arg0) {
                deleteSelectedQuestions();

                // fire event but validation isn't required
                ScenarioEventUtility.fireDirtyEditorEvent();
            }
        });

        TextCell questionTextCell = new TextCell() {
            @Override
            public void render(Context context, SafeHtml value, SafeHtmlBuilder sb) {
                if (((AssessmentRecord) context.getKey()).getQuestionOrReply() instanceof Question) {
                    super.render(context, value, sb);
                } else {
                    // render nothing for this row
                }
            }
        };

        TextCell replyTextCell = new TextCell() {
            @Override
            public void render(Context context, SafeHtml value, SafeHtmlBuilder sb) {
                if (((AssessmentRecord) context.getKey()).getQuestionOrReply() instanceof Reply) {
                    super.render(context, value, sb);
                } else {
                    // render nothing for this row
                }
            }
        };

        ExtendedSelectionCell assessmentSelectionCell = new ExtendedSelectionCell() {
            @Override
            public void render(Context context, String value, SafeHtmlBuilder sb) {
                if (((AssessmentRecord) context.getKey()).getQuestionOrReply() instanceof Reply) {
                    super.render(context, value, sb);
                } else {
                    // render nothing for this row
                }
            }
        };

        Column<AssessmentRecord, String> questionTextColumn = createColumn(questionTextCell, new GetValue<String>() {
            @Override
            public String getValue(AssessmentRecord record) {
                if (record.getQuestionOrReply() instanceof Question) {
                    return record.getDisplayText();
                }

                return "";
            }
        }, null // values in this column cannot be edited
        );

        questionAssessmentDataDisplay.addColumn(questionTextColumn, "Question");
        questionAssessmentDataDisplay.setColumnWidth(questionTextColumn, "50%");
        questionAssessmentDataDisplay.setEmptyTableWidget(
                new HTML("" + "<span style='font-size: 12pt;'>" + "No questions have been added; therefore, no "
                        + "questions assessments will be performed." + "</span>"));

        Column<AssessmentRecord, String> replyTextColumn = createColumn(replyTextCell, new GetValue<String>() {
            @Override
            public String getValue(AssessmentRecord record) {
                if (record.getQuestionOrReply() instanceof Reply) {
                    return record.getDisplayText();
                }

                return "";
            }
        }, null // values in this column cannot be edited
        );

        questionAssessmentDataDisplay.addColumn(replyTextColumn, "Reply");
        questionAssessmentDataDisplay.setColumnWidth(replyTextColumn, "25%");

        List<String> assessmentChoices = new ArrayList<String>();

        for (AssessmentLevelEnum assessmentLevelEnum : AssessmentLevelEnum.VALUES()) {
            assessmentChoices.add(assessmentLevelEnum.getDisplayName());
        }

        assessmentSelectionCell.setDefaultOptions(assessmentChoices);

        Column<AssessmentRecord, String> assessmentSelectionColumn = createColumn(assessmentSelectionCell,
                new GetValue<String>() {
                    @Override
                    public String getValue(AssessmentRecord record) {
                        if (record.getQuestionOrReply() instanceof Reply) {
                            return AssessmentLevelEnum.valueOf(((Reply) record.getQuestionOrReply()).getResult())
                                    .getDisplayName();
                        }

                        return "";
                    }
                }, new FieldUpdater<AssessmentRecord, String>() {
                    @Override
                    public void update(int index, AssessmentRecord record, String value) {
                        if (record.getQuestionOrReply() instanceof Reply) {
                            Reply reply = (Reply) record.getQuestionOrReply();
                            reply.setResult(value);
                        }
                    }
                });

        questionAssessmentDataDisplay.addColumn(assessmentSelectionColumn, "Assessment");
        questionAssessmentDataDisplay.setColumnWidth(assessmentSelectionColumn, "25%");

        Column<AssessmentRecord, SafeHtml> statusColumn = new Column<AssessmentRecord, SafeHtml>(new SafeHtmlCell()) {
            @Override
            public SafeHtml getValue(AssessmentRecord record) {
                return record.getStatusHtml();
            }
        };

        deleteQuestionButton.setEnabled(false);

        questionAssessmentDataDisplay.setPageSize(Integer.MAX_VALUE);
        questionAssessmentDataDisplay.addColumn(statusColumn, "Status");
        questionAssessmentDataDisplay.redraw();
        questionAssessmentDataDisplay.setSelectionModel(questionAssessmentSelectionModel);

        questionAssessmentDataProvider.addDataDisplay(questionAssessmentDataDisplay);
        questionAssessmentSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                for (AssessmentRecord record : questionAssessmentSelectionModel.getSelectedSet()) {

                    // Select the question above the selected reply
                    if (record.getQuestionOrReply() instanceof Reply) {
                        for (int i = questionAssessmentDataProvider.getList().indexOf(record) - 1; i >= 0; i--) {
                            AssessmentRecord nextRecord = questionAssessmentDataProvider.getList().get(i);
                            if (nextRecord.getQuestionOrReply() instanceof Question) {
                                record = nextRecord;
                                questionAssessmentSelectionModel.setSelected(record, true);

                                break;
                            }
                        }
                    }

                    // Select all replies below the question
                    for (int i = questionAssessmentDataProvider.getList().indexOf(record)
                            + 1; i < questionAssessmentDataProvider.getList().size(); i++) {

                        AssessmentRecord nextRecord = questionAssessmentDataProvider.getList().get(i);
                        if (nextRecord.getQuestionOrReply() instanceof Question) {
                            break;
                        }

                        questionAssessmentSelectionModel.setSelected(nextRecord, true);
                    }
                }

                deleteQuestionButton.setEnabled(true);
            }
        });
    }

    /**
     * Creates the column.
     *
     * @param <C> the generic type
     * @param cell the cell
     * @param getter the getter
     * @param fieldUpdater the field updater
     * @return the column
     */
    private <C> Column<AssessmentRecord, C> createColumn(Cell<C> cell, final GetValue<C> getter,
            FieldUpdater<AssessmentRecord, C> fieldUpdater) {

        Column<AssessmentRecord, C> column = new Column<AssessmentRecord, C>(cell) {
            @Override
            public C getValue(AssessmentRecord object) {
                return getter.getValue(object);
            }
        };

        if (fieldUpdater != null) {
            column.setFieldUpdater(fieldUpdater);
        }

        return column;
    }
    
    /**
     * Perform actions when {@link Task} or {@link Concept} is renamed.
     * 
     * @param taskOrConcept the {@link Task} or {@link Concept} that was renamed. Can't be null.
     */
    public void onRename(Serializable taskOrConcept) {
        if (taskOrConcept == null) {
            throw new IllegalArgumentException("The parameter 'taskOrConcept' cannot be null.");
        } else if (!(taskOrConcept instanceof Task || taskOrConcept instanceof Concept)) {
            throw new IllegalArgumentException("The parameter 'taskOrConcept' must be of type 'Task' or 'Concept'.");
        }

        if (currentTaskOrConcept instanceof Task) {
            Task task = (Task) currentTaskOrConcept;
            surveyPicker.setTransitionName(task.getName());
        } else if (currentTaskOrConcept instanceof Concept) {
            Concept concept = (Concept) currentTaskOrConcept;
            surveyPicker.setTransitionName(concept.getName());
        }
    }
    
    /**
     * Populates the survey question panel with the data provided in the survey assessment.
     * 
     * @param taskOrConcept the {@link Task} or {@link Concept} that we are adding additional
     *        assessments for. Can't be null.
     */
    public void showAdditionalAssessments(Serializable taskOrConcept) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("showAdditionalAssessments(" + taskOrConcept + ")");
        }
        
        // starting to populate the page
        performingSetup = true;

        // set the initial state of the survey picker using the current scenario
        surveyPicker.setSurveyResources(new ScenarioSurveyResources(ScenarioClientUtility.getScenario()));
        surveyPicker.updateDisplay();

        if (taskOrConcept == null) {
            throw new IllegalArgumentException("The parameter 'taskOrConcept' cannot be null.");
        } else if (!(taskOrConcept instanceof Task || taskOrConcept instanceof Concept)) {
            throw new IllegalArgumentException("The parameter 'taskOrConcept' must be of type 'Task' or 'Concept'.");
        }

        currentTaskOrConcept = taskOrConcept;
        updateReadOnly();

        Serializable assessment = null;

        if (currentTaskOrConcept instanceof Task) {

            Task task = (Task) currentTaskOrConcept;

            if (task.getAssessments() != null && !task.getAssessments().getAssessmentTypes().isEmpty()) {
                assessment = task.getAssessments().getAssessmentTypes().get(0);
            }else{
                surveyPicker.setValue(null);
            }
            surveyPicker.setTransitionName(task.getName());

        } else if (currentTaskOrConcept instanceof Concept) {

            Concept concept = (Concept) currentTaskOrConcept;

            if (concept.getAssessments() != null && !concept.getAssessments().getAssessmentTypes().isEmpty()) {
                assessment = concept.getAssessments().getAssessmentTypes().get(0);
            }else{
                surveyPicker.setValue(null);
            }
            surveyPicker.setTransitionName(concept.getName());
        }

        final AdditionalAssessmentType assessmentType;
        if (assessment instanceof Survey) {
            assessmentType = AdditionalAssessmentType.SURVEY;
            final Survey surveyAssessment = (Survey) assessment;
            surveyPicker.setValue(surveyAssessment.getGIFTSurveyKey());
            if(logger.isLoggable(Level.INFO)){
                String questionSize = surveyAssessment.getQuestions() != null ? String.valueOf(surveyAssessment.getQuestions().getQuestion().size()) : "-1";
                logger.info("set assessment type to "+AdditionalAssessmentType.SURVEY+" and survey picker value to "+surveyPicker.getValue()+
                        ".  Question assessments size = "+questionSize);
            }

            selectQuestionDialog.setSurveyGIFTKey(surveyAssessment.getGIFTSurveyKey());
            ScenarioClientUtility.getCourseSurveys(new AsyncCallback<FetchSurveyContextSurveysResult>() {
                @Override
                public void onSuccess(FetchSurveyContextSurveysResult result) {
                    populateSurveyQuestionPanel(surveyAssessment, result.getSurveys());
                    finishShowAdditionalAssessments(assessmentType);
                }

                @Override
                public void onFailure(Throwable caught) {
                    // do nothing
                    finishShowAdditionalAssessments(assessmentType);
                }
            });

        } else if (assessment instanceof ConditionAssessment) {
            assessmentType = AdditionalAssessmentType.CONDITIONS;
            finishShowAdditionalAssessments(assessmentType);
        } else {
            assessmentType = AdditionalAssessmentType.DO_NOTHING;
            finishShowAdditionalAssessments(assessmentType);
        }

    }

    /**
     * Deletes all the selected questions from the survey assessment.
     */
    private void deleteSelectedQuestions() {

        final Serializable assessment;

        // exit cases
        if (currentTaskOrConcept == null) {
            return;
        } else if (currentTaskOrConcept instanceof Task) {
            Task task = (Task) currentTaskOrConcept;
            if (task.getAssessments() == null || task.getAssessments().getAssessmentTypes().isEmpty()) {
                return;
            }
            assessment = task.getAssessments().getAssessmentTypes().get(0);
        } else if (currentTaskOrConcept instanceof Concept) {
            Concept concept = (Concept) currentTaskOrConcept;
            if (concept.getAssessments() == null || concept.getAssessments().getAssessmentTypes().isEmpty()) {
                return;
            }
            assessment = concept.getAssessments().getAssessmentTypes().get(0);
        } else {
            return;
        }

        if (assessment instanceof Survey) {

            final List<Question> selectedQuestions = new ArrayList<Question>();
            final List<AssessmentRecord> selectedRecords = new ArrayList<AssessmentRecord>();

            for (AssessmentRecord record : questionAssessmentSelectionModel.getSelectedSet()) {

                if (record.getQuestionOrReply() instanceof Question) {
                    selectedQuestions.add((Question) record.getQuestionOrReply());
                }

                selectedRecords.add(record);
            }

            boolean isSingular = selectedQuestions.size() == 1;

            String dialogMsg = "Are you sure you want to remove the selected question" + (isSingular ? "" : "s")
                    + " from this survey assessment?";

            OkayCancelDialog.show("Delete " + (isSingular ? "Question" : "Questions") + "?", dialogMsg,
                    "Yes, delete " + (isSingular ? " this question" : " these questions"), new OkayCancelCallback() {

                        @Override
                        public void okay() {
                            ((Survey) assessment).getQuestions().getQuestion().removeAll(selectedQuestions);
                            questionAssessmentDataProvider.getList().removeAll(selectedRecords);
                            questionAssessmentDataProvider.refresh();
                            deleteQuestionButton.setEnabled(!questionAssessmentDataProvider.getList().isEmpty());
                        }

                        @Override
                        public void cancel() {
                            // Nothing to do
                        }
                    });
        }
    }
    

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        validationStatuses.add(surveyPickerValidationStatus);
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        if (surveyPickerValidationStatus.equals(validationStatus)) {
            boolean surveyAssessmentType = assessmentTypeSelect.getSelectedItem() != null && StringUtils
                    .equals(assessmentTypeSelect.getSelectedItem().getValue(), AdditionalAssessmentType.SURVEY.name());
            boolean surveySelected = StringUtils.isNotBlank(surveyPicker.getValue());

            if (surveyAssessmentType && !surveySelected) {
                surveyPickerValidationStatus.setInvalid();
            } else {
                surveyPickerValidationStatus.setValid();
            }
        }

    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
        // no validation composite children
    }

    /**
     * Updates the read only mode based on the state of the widget.
     */
    private void updateReadOnly() {
        isReadOnly = ScenarioClientUtility.isReadOnly();

        assessmentTypeSelect.setEnabled(!isReadOnly);
        addQuestionButton.getElement().getStyle().setOpacity(isReadOnly ? 0.5f : 1.0f);
        deleteQuestionButton.setEnabled(!isReadOnly);
    }
}
