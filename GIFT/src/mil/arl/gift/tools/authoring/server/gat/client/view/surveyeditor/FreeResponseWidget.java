/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Container;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.gwt.HTMLPanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;

import mil.arl.gift.common.EnumerationNotFoundException;
import mil.arl.gift.common.enums.QuestionDifficultyEnum;
import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.common.enums.SurveyResponseTypeEnum;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.AbstractQuestionWidget;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.LoadSurveyException;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.SurveyEditMode;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.events.SurveyScoreValueChangedEvent;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.AbstractPropertySet;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.CommonPropertySet;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.DifficultyAndConceptsPropertySet;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.HiddenMFRAnswerWeightsPropertySet;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.QuestionImagePropertySet;
import mil.arl.gift.common.survey.AbstractQuestion;
import mil.arl.gift.common.survey.AbstractSurveyElement;
import mil.arl.gift.common.survey.AbstractSurveyQuestion;
import mil.arl.gift.common.survey.FillInTheBlankQuestion;
import mil.arl.gift.common.survey.FillInTheBlankSurveyQuestion;
import mil.arl.gift.common.survey.FreeResponseReplyWeights;
import mil.arl.gift.common.survey.SurveyItemProperties;
import mil.arl.gift.common.survey.score.AttributeScorerProperties;
import mil.arl.gift.common.survey.score.QuestionScorer;
import mil.arl.gift.common.survey.score.SurveyScorerUtil;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.events.FreeResponseScoringWidgetChangedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.props.AnswerFieldTextBoxPropertySet;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.props.DynamicResponseFieldPropertySet;

/**
 * The FreeResponseWidget allows the survey author to create a free response entry in the survey page.
 * 
 * @author nblomberg
 *
 */
public class FreeResponseWidget extends AbstractQuestionWidget {

    private static Logger logger = Logger.getLogger(FreeResponseWidget.class.getName());

    @UiField
    protected Container freeResponseContainer;

    @UiField
    protected Container freeResponseScoringContainer;

    @UiField
    protected Container freeResponseScoringContentsContainer;

    private static WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);

	interface WidgetUiBinder extends
			UiBinder<Widget, FreeResponseWidget> {
    }

    /** Interface for handling events. */
    interface FreeResponseWidgetEventBinder extends EventBinder<FreeResponseWidget> {
    }

    /** Create the instance of the event binder (binds the widget for events. */
    private static final FreeResponseWidgetEventBinder eventBinder = GWT.create(FreeResponseWidgetEventBinder.class);

    /** The UiStyle from the .ui.xml file. */
    interface UiStyle extends CssResource {
        public String textBoxStyle();

        public String labelStyle();
    }

    @UiField
    UiStyle style;

    /** The answer weights corresponding to the response field row option lists */
    private FreeResponseReplyWeights answerWeights;

    /**
     * Flag indicating if the survey containing this widget is a question bank
     */
    private final boolean isInQuestionBank;

    /**
     * List of attributes for this question to be scored on
     */
    private Set<AttributeScorerProperties> scoringAttributes;

    /** Keeps track of the order of the free response scoring widgets */
    private Map<FreeResponseScoringWidget, Integer> widgetToIndexMap = new HashMap<FreeResponseScoringWidget, Integer>();

    /**
     * Constructor
     * 
     * @param mode the current survey editor mode
     * @param isScored true if the free response widget is being scored; false
     *        otherwise
     * @param isInQuestionBank true if the parent survey is a question bank;
     *        false otherwise
     */
    public FreeResponseWidget(SurveyEditMode mode, boolean isScored, boolean isInQuestionBank) {
        super(mode, isScored);
        if (logger.isLoggable(Level.FINE)) {
            StringBuilder sb = new StringBuilder("FreeResponseWidget(");
            List<Object> params = Arrays.<Object>asList(mode, isScored, isInQuestionBank);
            StringUtils.join(", ", params, sb);
            logger.fine(sb.append(")").toString());
        }

        initWidget(uiBinder.createAndBindUi(this));
        this.isInQuestionBank = isInQuestionBank;
        eventBinder.bindEventHandlers(this, SharedResources.getInstance().getEventBus());
        onEditorModeChanged(mode);
    }

    @Override
    protected void addCustomPropertySets() {

        AnswerFieldTextBoxPropertySet ansFieldPropSet = new AnswerFieldTextBoxPropertySet();
        addPropertySet(ansFieldPropSet);

        DynamicResponseFieldPropertySet dynRespFieldPropSet = new DynamicResponseFieldPropertySet();
        addPropertySet(dynRespFieldPropSet);

        HiddenMFRAnswerWeightsPropertySet answerWeightsSet = new HiddenMFRAnswerWeightsPropertySet();
        addPropertySet(answerWeightsSet);
        
        DifficultyAndConceptsPropertySet diffAndConceptSet = new DifficultyAndConceptsPropertySet();
        addPropertySet(diffAndConceptSet);

        answerWeights = (FreeResponseReplyWeights) answerWeightsSet.getPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS);
        scoringAttributes = ((QuestionScorer) answerWeightsSet.getPropertyValue(SurveyPropertyKeyEnum.SCORERS)).getAttributeScorers();
    }

    @Override
    public void onPropertySetChange(AbstractPropertySet propSet) {

        super.onPropertySetChange(propSet);

        if (propSet instanceof DynamicResponseFieldPropertySet) {
            DynamicResponseFieldPropertySet dynRespFieldProps = (DynamicResponseFieldPropertySet) propSet;
            List<String> types = dynRespFieldProps.getResponseFieldTypes();
            List<String> labels = dynRespFieldProps.getResponseFieldLabels();
            List<String> leftAligned = dynRespFieldProps.getResponseFieldLeftAligned();
            Integer perLine = dynRespFieldProps.getResponsesPerLine();

            initContainer(perLine, types, labels, leftAligned);
        }
    }

    /**
     * Initializes the container that holds the response field widgets. Each line will have
     * "responsesPerLine" number of widgets on it. Each widget will contain a text field and
     * optionally a label.
     * 
     * @param responsesPerLine the number of widgets per line. Can't be null. Must be positive.
     * @param responseTypes the type of allow characters in the text field. Uses values from
     *            {@link SurveyResponseTypeEnum}. Can't be null.
     * @param responseLabels the optional labels for the response types. No label is an empty
     *            string. Can be null, will default to no labels.
     * @param responseLeftAligned if the label is left aligned or right aligned.
     */
    private void initContainer(Integer responsesPerLine, List<String> responseTypes, List<String> responseLabels, List<String> responseLeftAligned) {

        if (responsesPerLine == null || responsesPerLine < 1) {
            throw new IllegalArgumentException("The Free Response Widget's 'responsesPerLine' must be a positive number.");
        } else if (responseTypes == null) {
            throw new IllegalArgumentException("The Free Response Widget's 'responseTypes' cannot be null.");
        }

        // start fresh
        freeResponseContainer.clear();

        int createdWidgets = 0;
        int numResponses = responseTypes.size();

        StringBuilder strBuilder = new StringBuilder();

        // keep looping until we have created all the widgets
        while (numResponses > createdWidgets) {
            int numRemaining = numResponses - createdWidgets;

            strBuilder.append("<div>");

            // loop for the number of responses that should be on each line
            for (int i = 0; i < responsesPerLine; i++) {

                // reached the number of responses but it didn't fill the whole "responses per
                // line" parameter, so end early.
                if (createdWidgets >= numResponses) {
                    break;
                }

                // Calculate column size. If the whole "responses per line" is filled, split by
                // value evenly; otherwise, split by the number remaining evenly.
                int colSize;
                if (numRemaining < responsesPerLine) {
                    colSize = 100 / numRemaining;
                } else {
                    colSize = 100 / responsesPerLine;
                }

                strBuilder.append("<div style='display:inline-block;width:").append(colSize)
                        .append("%;padding-right:15px;'> <table style='width:100%'><tr style='width:100%'>");

                // response field text box
                final TextBox textBox = new TextBox();
                textBox.setStyleName(style.textBoxStyle(), true);

                textBox.setPlaceholder("Response Field " + (createdWidgets+1));
                textBox.setEnabled(false);

                boolean leftAligned = false;
                if (responseLeftAligned != null && createdWidgets < responseLeftAligned.size()) {
                    leftAligned = Boolean.parseBoolean(responseLeftAligned.get(createdWidgets));
                }
                
                // Add text box before label if label is not left aligned
                if (!leftAligned) {
                    strBuilder.append("<td style='width:100%'>").append(textBox.toString()).append("</td>");
                }

                String label = null;
                if (responseLabels != null && createdWidgets < responseLabels.size()) {
                    label = responseLabels.get(createdWidgets);
                }
                
                // if the label exists, create it next to the text box
                if (label != null && !label.trim().isEmpty()) {
                    Label labelWidget = new Label(label);
                    labelWidget.setWordWrap(false);
                    labelWidget.setStyleName(style.labelStyle(), true);

                    String padding_direction = leftAligned ? "padding-right" : "padding-left";
                    strBuilder.append("<td style='").append(padding_direction).append(":5px;'>").append(labelWidget.toString()).append("</td>");
                }
                
                // Add text box after label if label is left aligned
                if (leftAligned) {
                    strBuilder.append("<td style='width:100%'>").append(textBox.toString()).append("</td>");
                }

                strBuilder.append("</tr></table></div>");
                
                createdWidgets++;
            }

            strBuilder.append("</div>");
        }

        freeResponseContainer.add(new HTMLPanel(SafeHtmlUtils.fromTrustedString(strBuilder.toString())));

        // update scoring container
        initScoringContainer(responseTypes);
    }

    /**
     * Initializes the container that holds the response field widgets. Each line will have
     * "responsesPerLine" number of widgets on it. Each widget will contain a text field and
     * optionally a label.
     * 
     * @param responseTypes the type of allow characters in the text field. Uses values from
     *            {@link SurveyResponseTypeEnum}. Can't be null.
     */
    private void initScoringContainer(List<String> responseTypes) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("initScoringContainer(" + responseTypes + ")");
        }

        if (responseTypes == null) {
            throw new IllegalArgumentException("The Free Response Widget's 'responseTypes' cannot be null.");
        }

        // start fresh
        freeResponseScoringContentsContainer.clear();
        widgetToIndexMap.clear();

        List<List<List<Double>>> replyWeights = answerWeights.getReplyWeights();
        
        // remove extraneous answer weights (happens when the property set removes a field)
        while (replyWeights.size() > responseTypes.size()) {
            replyWeights.remove(replyWeights.size()-1);
        }
        
        // create scoring for each response field
        for (int i = 0; i < responseTypes.size(); i++) {
                
            SurveyResponseTypeEnum responseType;
            try {
                responseType = SurveyResponseTypeEnum.valueOf(responseTypes.get(i));
            } catch (@SuppressWarnings("unused") EnumerationNotFoundException e) {
                // the FreeResponseScoringWidget will handle a null response type
                responseType = null;
            }

            
            
            FreeResponseScoringWidget scoringWidget;
            if (i < replyWeights.size() && !replyWeights.get(i).isEmpty()) {
                
                // if type was changed to free-text, set scoring weights to empty.
                if (SurveyResponseTypeEnum.FREE_TEXT.equals(responseType)) {
                    replyWeights.get(i).clear();
                    replyWeights.get(i).add(new ArrayList<Double>());
                }
                
                // create from existing (1 to many)
                scoringWidget = new FreeResponseScoringWidget("Response Field " + (i + 1), responseType, replyWeights.get(i), isInQuestionBank());
            } else {
                // create new default
                scoringWidget = new FreeResponseScoringWidget("Response Field " + (i + 1), responseType, null, isInQuestionBank());
                answerWeights.addNew(scoringWidget.updateScores());
            }

            widgetToIndexMap.put(scoringWidget, i);
            freeResponseScoringContentsContainer.add(scoringWidget);
        }

        // update the total points (this also saves the scores into the properties)
        updateTotalQuestionFlag();
    }

    /**
     * Received a notification that the scoring values changed. Perform update.
     * 
     * @param event the event that was triggered. Contains the modified widget.
     */
    @EventHandler
    protected void onFreeResponseScoringWidgetChangedEvent(FreeResponseScoringWidgetChangedEvent event) {
        FreeResponseScoringWidget widget = event.getWidget();

        // If we can find the widget, peform update. Otherwise, skip because it doesn't exist
        // anymore (should never happen).
        if (widgetToIndexMap.containsKey(widget)) {
            Integer index = widgetToIndexMap.get(widget);

            if (index + 1 > answerWeights.getReplyWeights().size()) {
                // new widget, so add weighted values
                answerWeights.getReplyWeights().add(widget.updateScores());
            } else {
                // existing widget, replace weighted values
                answerWeights.setWeightValue(index, widget.updateScores());
            }

            // perform update
            updateTotalQuestionFlag();
        }
    }

    @Override
    public void initializeWidget() {
        // Nothing to do here as widgets are created in the binder.
        
    }

    @Override
    public void refresh() {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("refresh() called: " + getEditMode());
        }

        if (getEditMode() == SurveyEditMode.WritingMode) {
            freeResponseScoringContainer.setVisible(false);
            questionHtml.setEditable(true);
        } else if (getEditMode() == SurveyEditMode.ScoringMode) {
            freeResponseScoringContainer.setVisible(true);
            questionHtml.setEditable(false);
        } else {
            logger.severe("Unsupported mode: " + getEditMode());
        }
    }

    @Override
    public void onEditorModeChanged(SurveyEditMode mode) {
        super.onEditorModeChanged(mode);
        setEditMode(mode);
        if (mode == SurveyEditMode.ScoringMode) {
            questionHtml.setEditable(false);
        } else {
            questionHtml.setEditable(true);
        }
    }

    @Override
    public Double getPossibleTotalPoints() {
        Double totalPoints = 0.0;

        try {
            totalPoints = SurveyScorerUtil.getHighestScoreFreeResponse(answerWeights.getReplyWeights());
        } catch (IllegalArgumentException e) {
            logger.severe("getPossibleTotalPoints() Caught exception: " + e.getMessage() + "  The points will be set to 0.0 for this question.");
        }
        return totalPoints;
    }

    @Override
    protected void setIsScoredType() {
        isScoredType = true;
    }



    @Override
    public void load(AbstractSurveyElement element) throws LoadSurveyException {

        if (element instanceof AbstractSurveyQuestion) {
            @SuppressWarnings("unchecked")
            AbstractSurveyQuestion<? extends AbstractQuestion> surveyQuestion = (AbstractSurveyQuestion<? extends AbstractQuestion>) element;

            if (surveyQuestion instanceof FillInTheBlankSurveyQuestion) {

                FillInTheBlankSurveyQuestion question = (FillInTheBlankSurveyQuestion) surveyQuestion;
                SurveyItemProperties properties = question.getProperties();

                FillInTheBlankQuestion fillInTheBlankQuestion = question.getQuestion();

                if (logger.isLoggable(Level.INFO)) {
                    logger.info("question text: " + fillInTheBlankQuestion.getText());
                }

                questionHtml.setValue(fillInTheBlankQuestion.getText());

                // Load the common properties (should happen in the base class).
                debugPrintQuestionProperties(properties);

                // Load the slider range properties (if any)
                AbstractPropertySet propSet = getPropertySetByType(AnswerFieldTextBoxPropertySet.class.getName());
                if (propSet != null) {
                    AnswerFieldTextBoxPropertySet answerFieldProps = (AnswerFieldTextBoxPropertySet) propSet;
                    answerFieldProps.load(properties);
                    answerFieldProps.load(fillInTheBlankQuestion.getProperties());
                }

                // Load the Dynamic Response Field Properties
                propSet = getPropertySetByType(DynamicResponseFieldPropertySet.class.getName());
                if (propSet != null) {
                    DynamicResponseFieldPropertySet dynRespFieldPropSet = (DynamicResponseFieldPropertySet) propSet;
                    dynRespFieldPropSet.load(properties);
                    dynRespFieldPropSet.load(fillInTheBlankQuestion.getProperties());
                }

                // Load the Common Properties
                propSet = getPropertySetByType(CommonPropertySet.class.getName());
                if (propSet != null) {
                    CommonPropertySet commonProps = (CommonPropertySet) propSet;
                    commonProps.load(properties);
                    commonProps.load(fillInTheBlankQuestion.getProperties());
                    commonProps.setSurveyQuestion(surveyQuestion);
                }

                // Load the Image Display Properties
                propSet = getPropertySetByType(QuestionImagePropertySet.class.getName());
                if (propSet != null) {
                    QuestionImagePropertySet imageProps = (QuestionImagePropertySet) propSet;
                    imageProps.load(properties);
                    imageProps.load(fillInTheBlankQuestion.getProperties());
                }

                // Load the answer weights Properties
                propSet = getPropertySetByType(HiddenMFRAnswerWeightsPropertySet.class.getName());
                if (propSet != null) {
                    HiddenMFRAnswerWeightsPropertySet answerWeightsProps = (HiddenMFRAnswerWeightsPropertySet) propSet;
                    answerWeightsProps.load(properties);
                    answerWeightsProps.load(fillInTheBlankQuestion.getProperties());
                    answerWeights = (FreeResponseReplyWeights) answerWeightsProps.getPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS);
                    scoringAttributes = ((QuestionScorer) answerWeightsProps.getPropertyValue(SurveyPropertyKeyEnum.SCORERS)).getAttributeScorers();
                }
                
                // Load the difficulty and concepts Properties
                propSet = getPropertySetByType(DifficultyAndConceptsPropertySet.class.getName());
                if (propSet != null) {
                    DifficultyAndConceptsPropertySet diffAndConceptsSet = (DifficultyAndConceptsPropertySet) propSet;
                    diffAndConceptsSet.load(properties);
                    diffAndConceptsSet.load(fillInTheBlankQuestion.getProperties());
                }

                // This should be called after all property sets have been loaded for the
                // abstractsurveyelement.
                addUnsupportedProperties(fillInTheBlankQuestion.getProperties(), properties);
                onLoadNotifyPropertySetChanges();

                // print the slider question properties
                debugPrintQuestionProperties(fillInTheBlankQuestion.getProperties());

                refresh();
            } else {
                throw new LoadSurveyException("Trying to load a FreeResponse widget, but encountered non free response data from the database.", null);
            }
        } else {
            throw new LoadSurveyException("Expected AbstractSurveyElement of type AbstractSurveyQuestion, but found: " + element.getClass().getName(), null);
        }

    }

    /**
     * Updates the QuestionScorer totalQuestion flag based on the current answerweights for the
     * question. This method should be called anytime a score event is fired and or anytime an event
     * occurs that could change the weights for the question (such as during the load process).
     */
    private void updateTotalQuestionFlag() {

        AbstractPropertySet propSet = getPropertySetByType(HiddenMFRAnswerWeightsPropertySet.class.getName());
        HiddenMFRAnswerWeightsPropertySet answerWeightsProps = (HiddenMFRAnswerWeightsPropertySet) propSet;
        answerWeightsProps.getProperties().setPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS, answerWeights);
        updateTotalQuestionFlag(answerWeightsProps.getProperties());
        SharedResources.getInstance().getEventBus().fireEvent(new SurveyScoreValueChangedEvent());
    }

    /**
     * Checks if this widget is contained within a question bank
     * 
     * @return true if it is; false otherwise.
     */
    public boolean isInQuestionBank() {
        return isInQuestionBank;
    }

    /**
     * Debug print function used to print the properties for the question. Only prints if the logger
     * level is at Info or below.
     * 
     * @param properties the survey item properties to log.
     */
    public void debugPrintQuestionProperties(SurveyItemProperties properties) {

        if (logger.isLoggable(Level.INFO) && properties != null) {
            // DEBUG PRINT THE properties
            logger.info("Properties size = " + properties.getPropertyCount());

            for (SurveyPropertyKeyEnum key : properties.getKeys()) {
                logger.info("Key name = " + key + "\nKey value = " + properties.getPropertyValue(key));
            }
        }
    }
    
    @Override
    public void setDifficulty(QuestionDifficultyEnum difficulty) {
        getPropertySetByType(DifficultyAndConceptsPropertySet.class.getName()).getProperties().setPropertyValue(SurveyPropertyKeyEnum.QUESTION_DIFFICULTY, difficulty.getDisplayName());
    }
    
    @Override
    public String getDifficulty(){
        return (String) getPropertySetByType(DifficultyAndConceptsPropertySet.class.getName()).getProperties().getPropertyValue(SurveyPropertyKeyEnum.QUESTION_DIFFICULTY);
    }
    
    @Override
    public void setConcepts(ArrayList<String> concepts){
        getPropertySetByType(DifficultyAndConceptsPropertySet.class.getName()).getProperties().setPropertyValue(SurveyPropertyKeyEnum.ASSOCIATED_CONCEPTS, SurveyItemProperties.encodeListString(concepts));
    }
    
    @Override
    public List<String> getConcepts(){
        return SurveyItemProperties.decodeListString((String) getPropertySetByType(DifficultyAndConceptsPropertySet.class.getName()).getProperties().getPropertyValue(SurveyPropertyKeyEnum.ASSOCIATED_CONCEPTS));
    }

    @Override
    public void setScorerProperty(Set<AttributeScorerProperties> attributes) {
        scoringAttributes = attributes;
        ((QuestionScorer) getPropertySetByType(HiddenMFRAnswerWeightsPropertySet.class.getName()).getPropertyValue(SurveyPropertyKeyEnum.SCORERS))
                .setAttributeScorers(scoringAttributes);
        if (logger.isLoggable(Level.INFO)) {
            logger.info("set scoring attributes = " + ((QuestionScorer) getPropertySetByType(HiddenMFRAnswerWeightsPropertySet.class.getName())
                    .getPropertyValue(SurveyPropertyKeyEnum.SCORERS)).getAttributeScorers());
        }
    }
    
    /**
     * Returns the current list of scoring attributes as a string list so the 
     * question container's multiselect box can be populated on load
     * 
     * @return the string list of the attributes
     */
    @Override
    public List<String> getScoringAttributesAsStringList(){
        List<String> stringList = new ArrayList<String>();
        for(AttributeScorerProperties attribute : scoringAttributes){
            stringList.add(attribute.getAttributeType().getName());
        }
        return stringList;
    }

    @Override
    public void setReadOnlyMode(boolean readOnly) {
        this.isReadOnly = readOnly;

        questionHtml.setEditable(!readOnly);
        if (readOnly) {
            questionHtml.setPlaceholder(READ_ONLY_MODE_PLACEHOLDER);
            questionHtml.setTooltip(READ_ONLY_MODE_TOOLTIP);
        }
    }



    @Override
    public void setPlaceholderResponseVisible(boolean visible) {
        // Nothing to do
    }
}
