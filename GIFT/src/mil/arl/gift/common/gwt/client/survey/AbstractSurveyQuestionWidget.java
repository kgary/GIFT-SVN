/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.survey;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconSize;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.Placement;

import com.google.gwt.dom.client.MediaElement;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.event.dom.client.LoadedMetadataEvent;
import com.google.gwt.event.dom.client.LoadedMetadataHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.media.client.Audio;
import com.google.gwt.media.client.Video;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.survey.AbstractQuestion;
import mil.arl.gift.common.survey.AbstractQuestionResponse;
import mil.arl.gift.common.survey.AbstractQuestionResponseMetadata;
import mil.arl.gift.common.survey.AbstractSurveyQuestion;
import mil.arl.gift.common.survey.FillInTheBlankQuestion;
import mil.arl.gift.common.survey.FillInTheBlankSurveyQuestion;
import mil.arl.gift.common.survey.MatrixOfChoicesSurveyQuestion;
import mil.arl.gift.common.survey.MultipleChoiceSurveyQuestion;
import mil.arl.gift.common.survey.RatingScaleSurveyQuestion;
import mil.arl.gift.common.survey.SliderSurveyQuestion;
import mil.arl.gift.common.survey.SurveyProperties;

/**
 * An abstract class for representing survey question widgets
 *
 * @param <T> the type of survey question this widget represents
 * @author jleonard
 */
public abstract class AbstractSurveyQuestionWidget<T extends AbstractSurveyQuestion<? extends AbstractQuestion>> extends AbstractSurveyElementWidget<T> {

    /** The logger. */
    private static Logger logger = Logger.getLogger(AbstractSurveyQuestionWidget.class.getName());
    
    /** the paths to images used for survey response scoring results */
    private static final String MAX_POINTS_IMG_PATH = "surveyWebResources/images/check.png";
    private static final String MIN_POINTS_IMG_PATH = "surveyWebResources/images/cross.png";
    private static final String MIDDLE_POINTS_IMG_PATH = "surveyWebResources/images/star_green.png";
    
    private final FlowPanel containerPanel = new FlowPanel();

    private final InlineLabel statusLabel = new InlineLabel("");

    protected HTML questionHtml;
    
    protected FlowPanel questionPanel;
    
    /**
     * Constructor
     *
     * @param surveyProperties The properties of the survey this question is in
     * @param surveyQuestion The survey question to ask
     * @param questionNumber The number of the question on the page
     * @param isBeingEdited If the question is being edited
     */
    public AbstractSurveyQuestionWidget(SurveyProperties surveyProperties, T surveyQuestion, int questionNumber, boolean isBeingEdited) {
        super(surveyProperties, surveyQuestion, isBeingEdited);

        containerPanel.addStyleName(SurveyCssStyles.SURVEY_QUESTION_WIDGET_STYLE);
        
        if (surveyQuestion.getQuestion().getQuestionImagePosition() == 1 && surveyQuestion.getQuestion().getQuestionMedia() != null) {
            containerPanel.add(generateQuestionMedia());
        }

        if (surveyQuestion.getQuestion().getText() != null && !surveyQuestion.getQuestion().getText().isEmpty()) {
            
            questionPanel = new FlowPanel();
            
            if (!getSurveyProperties().getHideSurveyQuestionNumbers()) {
            	
            	StringBuilder sb = new StringBuilder();
            	sb.append("<div style='width: 100%; display: table;'><div style='display: table-row;'>")
            	.append("<div style='display: table-cell; padding-right: 5px;'>")
            	.append(questionNumber)
            	.append(".</div><div style='width: 100%; display: table-cell;'>")
            	.append(surveyQuestion.getQuestion().getText())
            	.append("</div></div></div>");

                questionHtml = new HTML(sb.toString(), true);

            } else {

                questionHtml = new HTML(surveyQuestion.getQuestion().getText(), true);
            }

            questionHtml.setStyleName(SurveyCssStyles.SURVEY_QUESTION_STYLE);
            
            questionPanel.add(questionHtml);
            
            if (surveyQuestion.getHelpString() != null && !surveyQuestion.getHelpString().isEmpty()) {
                
                FlowPanel helpContainer = new FlowPanel();
                
                helpContainer.addStyleName("questionHelpText");
                
                Image helpImage = new Image("surveyWebResources/images/question.png");
                
                helpImage.addStyleName("questionHelpImage");
                
                helpContainer.add(helpImage);
                
                Tooltip tooltip = new Tooltip(helpImage);
                tooltip.setContainer("body");
                tooltip.setTitle(surveyQuestion.getHelpString());
                tooltip.setTooltipInnerClassNames("tooltip-inner questionHelpImageTooltip");
                tooltip.setPlacement(Placement.BOTTOM);
                
                helpContainer.add(tooltip);
                                
                questionPanel.addStyleName("questionPanelWithHelp");
                
                questionPanel.add(helpContainer);
                
            }
                 
            containerPanel.add(questionPanel);

        } else if (isBeingEdited) {

            containerPanel.add(generateSmallErrorPanel("The question text has not been set."));

        } else {

            throw new NullPointerException("The question text has not been set.");
        }

        if (surveyQuestion.getQuestion().getQuestionImagePosition() == 0 && surveyQuestion.getQuestion().getQuestionMedia() != null) {
            containerPanel.add(generateQuestionMedia());
        }

        statusLabel.setHeight("0px");
        
        FlowPanel statusContainer = new FlowPanel();
        statusContainer.setStylePrimaryName(SurveyCssStyles.SURVEY_STATUS_CONTAINER);
        statusContainer.getElement().getStyle().setProperty("display", "none");
        statusContainer.add(statusLabel);
        
        containerPanel.add(statusContainer);
        
        addWidget(containerPanel);
    }
    
    /**
     * Return a new image widget that contains the maximum points image.
     * 
     * @return a new image widget
     */
    protected Image createMaxWeightImage(){
        Image img = new Image(MAX_POINTS_IMG_PATH);
        img.setSize("16px", "16px");
        return img;
    }
    
    /**
     * Return a new image widget that contains the minimum points image.
     * 
     * @return a new image widget
     */
    protected Image createMinWeightImage(){
        Image img = new Image(MIN_POINTS_IMG_PATH);
        img.setSize("16px", "16px");
        return img;
    }
    
    /**
     * Return a new image widget that contains the not the min and not the max points image.
     * 
     * @return a new image widget
     */
    protected Image createMidWeightImage(){
        Image img = new Image(MIDDLE_POINTS_IMG_PATH);
        img.setSize("16px", "16px");
        return img;
    }

    /**
     * Add a widget for answering the question
     *
     * @param answerArea Widget for answering the question
     */
    protected final void addAnswerArea(Widget answerArea) {
        addAnswerArea(answerArea, false);
    }

    /**
     * Add a widget for answering the question
     *
     * @param answerArea Widget for answering the question
     * @param center If the answer area should be centered on the webpage
     */
    protected final void addAnswerArea(final Widget answerArea, boolean center) {
        answerArea.addStyleName(SurveyCssStyles.SURVEY_QUESTION_RESPONSE_STYLE);
        if (center) {
            answerArea.addAttachHandler(new AttachEvent.Handler() {
                @Override
                public void onAttachOrDetach(AttachEvent event) {
                    if (event.isAttached()) {
                        answerArea.setWidth(answerArea.getOffsetWidth() + "px");
                        answerArea.setStyleName(SurveyCssStyles.SURVEY_QUESTION_RESPONSE_CENTER_STYLE);
                    }
                }
            });
        }
        containerPanel.add(answerArea);
    }
    
    /**
     * Add a widget to the end of the survey question's panel that contains
     * information about the points earned for this question.
     * 
     * @param earnedPointsWidget
     */
    protected final void addEarnedPointsArea(final Widget earnedPointsWidget){
        
        if(earnedPointsWidget != null){
            containerPanel.add(earnedPointsWidget);
        }
    }

    /**
     * Get the answer for this survey question
     *
     * @param validate checks if the number of responses equals the expected number of results and
     *            for missing required fields. This is typically true when submitting a survey.
     * @return QuestionResponse The response for the survey question
     * @throws MalformedAnswerException If the question was not properly answered and the validate
     *             flag is true.
     */
    public abstract AbstractQuestionResponse getAnswer(boolean validate) throws MalformedAnswerException;
    
    /**
     * Populate the correct widget component with the answer details provided by some other logic
     * external to the tutor.  For example, with a multiple choice question the choices provided in
     * the response should have their radio/checkbox components selected in the tutor client to indicate
     * the question was answered.
     *  
     * @param questionResponse contains a response to this question.  Can be null.  Can also have no actual responses as a no
     * response is a valid question response.
     */
    public abstract void setExternalQuestionResponse(AbstractQuestionResponse questionResponse);

    /**
     * Hide the error text
     */
    public final void resetStatus() {
        statusLabel.removeStyleName(SurveyCssStyles.SURVEY_ERROR_LABEL_STYLE);
        statusLabel.removeStyleName(SurveyCssStyles.SURVEY_WARNING_LABEL_STYLE);
        statusLabel.setText("");
        statusLabel.setHeight("0px");
        statusLabel.getParent().getElement().getStyle().setProperty("display", "none");
    }

    /**
     * Display an error with the survey question / answer
     *
     * @param error The error to display
     */
    public final void displayError(String error) {
        statusLabel.setStylePrimaryName(SurveyCssStyles.SURVEY_ERROR_LABEL_STYLE);
        statusLabel.setText("Error: " + error);
        statusLabel.getElement().getStyle().clearHeight();
        statusLabel.getParent().getElement().getStyle().clearProperty("display");
    }
    
    public final void displayWarning(String warning) {
        statusLabel.setStylePrimaryName(SurveyCssStyles.SURVEY_WARNING_LABEL_STYLE);
        statusLabel.setText("Warning: " + warning);
        statusLabel.getElement().getStyle().clearHeight();
        statusLabel.getParent().getElement().getStyle().clearProperty("display");
    }

    /**
     * Generates the media for this survey element
     *
     * @return Widget the generated media. Will not be null. 
     */
    private Widget generateQuestionMedia() {
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("Generating question media panel");
        }
        
        final Integer mediaWidth = getSurveyElement().getQuestion().getQuestionImageWidth();
        final String mediaLocation = getSurveyElement().getQuestion().getQuestionMedia();
        boolean isLegacyImage = !getSurveyElement().getQuestion().getProperties().hasProperty(SurveyPropertyKeyEnum.QUESTION_MEDIA_TYPE_KEY);
        
        final Widget media;
        
        if(Constants.isVideoFile(mediaLocation)) {
            
            if(logger.isLoggable(Level.INFO)) {
                logger.info("Generating question video");
            }
            
            /* Display a video */
            final Video video = Video.createIfSupported();
            video.setPreload(MediaElement.PRELOAD_AUTO);
            video.setControls(true);
            
            media = video;

            video.addLoadedMetadataHandler(new LoadedMetadataHandler() {
                
                @Override
                public void onLoadedMetadata(LoadedMetadataEvent event) {
                    resizeMedia(mediaWidth, media);
                }
            });
            
            video.setSrc(mediaLocation);
            
        } else if(Constants.isAudioFile(mediaLocation)) {
            
            if(logger.isLoggable(Level.INFO)) {
                logger.info("Generating question audio");
            }
            
            /* Display an audio file */
            final Audio audio = Audio.createIfSupported();
            audio.setPreload(MediaElement.PRELOAD_AUTO);
            audio.setControls(true);
            
            media = audio;
            
            audio.addLoadedMetadataHandler(new LoadedMetadataHandler() {
                
                @Override
                public void onLoadedMetadata(LoadedMetadataEvent event) {
                    resizeMedia(mediaWidth, media);
                }
            });
            
            audio.setSrc(mediaLocation);
            
        } else if(isLegacyImage || Constants.isImageFile(mediaLocation)){
            
            if(logger.isLoggable(Level.INFO)) {
                logger.info("Generating question image");
            }
            
            /* Display either a legacy image or an image file */
            final Image image = new Image(mediaLocation);
            
            media = image;

            image.addLoadHandler(new LoadHandler() {
                @Override
                public void onLoad(LoadEvent event) {
                    resizeMedia(mediaWidth, media);
                }
            });
            
        } else {
            
            if(logger.isLoggable(Level.INFO)) {
                logger.info("Generating question unknown media");
            }
            
            /* Display a button that can be used to view a media file with an unknown type */
            final Button button = new Button("Click to View");
            button.setType(ButtonType.PRIMARY);
            button.setIcon(IconType.FILE);
            button.setIconSize(IconSize.TIMES2);
            button.getElement().getStyle().setProperty("padding", "10px 80px");
            button.getElement().getStyle().setProperty("display", "flex");
            button.getElement().getStyle().setProperty("flexDirection", "column");
            button.getElement().getStyle().setProperty("alignItems", "center");
            button.getElement().getStyle().setProperty("fontSize", "20px");
            
            final Tooltip tooltip = new Tooltip("Click to show this media in a new window");
            tooltip.setWidget(button);
            
            media = button;
            
            button.addMouseDownHandler(new MouseDownHandler() {
                
                @Override
                public void onMouseDown(MouseDownEvent event) {
                    event.stopPropagation();
                }
            });
            
            button.addClickHandler(new ClickHandler() {
                
                @Override
                public void onClick(ClickEvent event) {
                    tooltip.hide();
                    String options = "menubar=no,location=no,resizable=yes,scrollbars=yes,status=no";
                    Window.open(
                            mediaLocation, 
                            "_blank", 
                            options);
                }
            });
            
        }
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("Question media generated.");
        }
        
        media.getElement().getStyle().setProperty("margin", "10px auto");
        media.getElement().getStyle().setProperty("display", "flex");
        media.getElement().getStyle().setProperty("maxWidth", "100%");

        FlowPanel mediaContainer = new FlowPanel();
        mediaContainer.add(media);
        
        mediaContainer.getElement().getStyle().setOverflowX(Overflow.AUTO);
        mediaContainer.getElement().getStyle().setOverflowY(Overflow.VISIBLE);
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("Finished generating media panel");
        }

        return mediaContainer;
    }
    
    /**
     * Resizes the media associated with this question to reflect the provided size
     * 
     * @param size the size that the rendered media should be shown with (as a percentage
     * of its original size)
     * @param media the media to resize. If null, no resizing will be performed.
     */
    private void resizeMedia(Integer size, Widget media) {
        if(media == null) {
            return;
        }
        
        float widthPercentage = (size / 100f);

        if (widthPercentage == 0) {

            widthPercentage = 1f;
        }
        
        int width;
        
        /* Attempt to get the natural width of the media being displayed. The property
         * that needs to be looked at to get this will vary slightly depending on
         * what type of HTML element is used */
        String widthProp = media instanceof Video ? "videoWidth" : "naturalWidth";
        String widthAttr = media.getElement().getPropertyString(widthProp);
        try {
            width = Integer.valueOf(widthAttr);
            
        } catch(@SuppressWarnings("unused") NumberFormatException e) {
            
            /* The element is likely still loading, so the natural width is not yet known.
             * If this happens, fall back on the offset width (i.e. the rendered width) */
            width = media.getOffsetWidth();
        }
        
        media.getElement().setAttribute("width", width * widthPercentage + "px");
        media.getElement().setAttribute("height", (widthPercentage * 100) + "%");
    }

    /**
     * Creates a widget for answering a given survey question
     *
     * @param surveyProperties The properties of the survey the question is in
     * @param surveyQuestion The survey question a widget is needed for
     * @param questionNumber The number of the question on the survey page
     * @param isBeingEdited If the question is being edited
     * @param isDebug if the survey widget should render in debug mode (e.g. color code scored answers)
     * @return AbstractSurveyQuestionWidget The widget for answering the survey
     * question
     */
    public static AbstractSurveyQuestionWidget<? extends AbstractSurveyQuestion<? extends AbstractQuestion>> 
        createQuestionWidget(SurveyProperties surveyProperties, 
                AbstractSurveyQuestion<? extends AbstractQuestion> surveyQuestion, int questionNumber, boolean isBeingEdited, boolean isDebug) {

        if (surveyQuestion != null && surveyQuestion.getQuestion() != null) {

            try {

                AbstractSurveyQuestionWidget<? extends AbstractSurveyQuestion<? extends AbstractQuestion>> questionWidget = null;

                if (surveyQuestion instanceof FillInTheBlankSurveyQuestion) {

                    FillInTheBlankQuestion question = (FillInTheBlankQuestion) surveyQuestion.getQuestion();
                    
                    if(question.getProperties().hasProperty(SurveyPropertyKeyEnum.IS_REMEDIATION_CONTENT)
                            && question.getProperties().getBooleanPropertyValue(SurveyPropertyKeyEnum.IS_REMEDIATION_CONTENT)){
                        
                        //regular FillInTheBlankQuestions don't support multi-select, so this must be a special case
                        
                        if(question.getProperties().hasProperty(SurveyPropertyKeyEnum.IS_ANSWER_FIELD_TEXT_BOX_KEY)
                                && question.getProperties().getBooleanPropertyValue(SurveyPropertyKeyEnum.IS_ANSWER_FIELD_TEXT_BOX_KEY)){
                            
                            questionWidget = new SummarizePassageQuestionWidget(surveyProperties, (FillInTheBlankSurveyQuestion) surveyQuestion, questionNumber, isBeingEdited);
                            
                        } else{
                            
                            questionWidget = new HighlightPassageQuestionWidget(surveyProperties, (FillInTheBlankSurveyQuestion) surveyQuestion, questionNumber, isBeingEdited);
                        }
                        
                    } else {

                        questionWidget = new FreeResponseQuestionWidget(surveyProperties, (FillInTheBlankSurveyQuestion) surveyQuestion, questionNumber, isBeingEdited);
                    }
                } else if (surveyQuestion instanceof MultipleChoiceSurveyQuestion) {

                    questionWidget = new MultipleChoiceQuestionWidget(surveyProperties, (MultipleChoiceSurveyQuestion) surveyQuestion, questionNumber, isBeingEdited, false, isDebug);

                } else if (surveyQuestion instanceof RatingScaleSurveyQuestion) {

                    questionWidget = new RatingScaleQuestionWidget(surveyProperties, (RatingScaleSurveyQuestion) surveyQuestion, questionNumber, isBeingEdited, isDebug);

                } else if (surveyQuestion instanceof MatrixOfChoicesSurveyQuestion) {

                    questionWidget = 
                            new MatrixOfChoicesQuestionWidget(surveyProperties, (MatrixOfChoicesSurveyQuestion) surveyQuestion, questionNumber, isBeingEdited, isDebug);

                } else if (surveyQuestion instanceof SliderSurveyQuestion) {

                    questionWidget = new SliderQuestionWidget(surveyProperties, (SliderSurveyQuestion) surveyQuestion, questionNumber, isBeingEdited);

                } else {

                    throw new IllegalArgumentException("Unable to create a survey question widget for survey question of type " + surveyQuestion.getClass());
                }

                return questionWidget;

            } catch (Exception e) {

                logger.severe("ERROR: Caught an exception while constructing a survey question widget for question " + surveyQuestion.getQuestion().getQuestionId() + ", error: " + e.getMessage());
                return null;
            }

        } else {

            logger.severe("ERROR: Caught an exception while constructing a survey question widget: Question is null");
            return null;
        }
    }

    /**
     * Creates a widget for answering a given survey question
     *
     * @param surveyProperties The properties of the survey the question is in
     * @param question The survey question a widget is needed for
     * @param questionNumber If the question is being edited
     * @param isDebug if the survey widget should render in debug mode (e.g. color code scored answers)
     * @return AbstractSurveyQuestionWidget The widget for answering the survey
     * question
     */
    public static AbstractSurveyQuestionWidget<? extends AbstractSurveyQuestion<? extends AbstractQuestion>> createQuestionWidget(SurveyProperties surveyProperties, AbstractSurveyQuestion<? extends AbstractQuestion> question, int questionNumber, boolean isDebug) {

        return createQuestionWidget(surveyProperties, question, questionNumber, false, isDebug);
    }

    /**
     * Creates a widget for reviewing the answer to a given survey question
     *
     * @param surveyProperties The properties of the survey the question is in
     * @param surveyQuestion The survey question a widget is needed for
     * @param questionNumber If the question is being edited
     * @param response The response to the survey question
     * @return AbstractSurveyQuestionWidget The widget for answering the survey
     * question
     */
    public static AbstractSurveyQuestionWidget<? extends AbstractSurveyQuestion<? extends AbstractQuestion>> createQuestionWidget(SurveyProperties surveyProperties, AbstractSurveyQuestion<? extends AbstractQuestion> surveyQuestion, 
            int questionNumber, AbstractQuestionResponseMetadata responseMetadata) {

        if (surveyQuestion != null && surveyQuestion.getQuestion() != null) {

            try {

                AbstractSurveyQuestionWidget<? extends AbstractSurveyQuestion<? extends AbstractQuestion>> questionWidget = null;

                if (surveyQuestion instanceof FillInTheBlankSurveyQuestion) {

                    FillInTheBlankQuestion question = (FillInTheBlankQuestion) surveyQuestion.getQuestion();
                    
                    if(question.getProperties().hasProperty(SurveyPropertyKeyEnum.IS_REMEDIATION_CONTENT)
                            && question.getProperties().getBooleanPropertyValue(SurveyPropertyKeyEnum.IS_REMEDIATION_CONTENT)){
                        
                        //regular FillInTheBlankQuestions don't support multi-select, so this must be a special case
                        
                        if(question.getProperties().getBooleanPropertyValue(SurveyPropertyKeyEnum.IS_ANSWER_FIELD_TEXT_BOX_KEY)){
                            
                            questionWidget = new SummarizePassageQuestionWidget(surveyProperties, (FillInTheBlankSurveyQuestion) surveyQuestion, questionNumber, responseMetadata);
                            
                        } else{
                            
                            questionWidget = new HighlightPassageQuestionWidget(surveyProperties, (FillInTheBlankSurveyQuestion) surveyQuestion, questionNumber, responseMetadata);
                        }
                        
                    } else {

                        questionWidget = new FreeResponseQuestionWidget(surveyProperties, (FillInTheBlankSurveyQuestion) surveyQuestion, questionNumber, responseMetadata);
                    }
                } else if (surveyQuestion instanceof MultipleChoiceSurveyQuestion) {

                    logger.info("Constructing MultipleChoiceQuestionWidget");
                    questionWidget = new MultipleChoiceQuestionWidget(surveyProperties, (MultipleChoiceSurveyQuestion) surveyQuestion, questionNumber, responseMetadata);

                } else if (surveyQuestion instanceof RatingScaleSurveyQuestion) {

                    questionWidget = new RatingScaleQuestionWidget(surveyProperties, (RatingScaleSurveyQuestion) surveyQuestion, questionNumber, responseMetadata);

                } else if (surveyQuestion instanceof MatrixOfChoicesSurveyQuestion) {

                    questionWidget = new MatrixOfChoicesQuestionWidget(surveyProperties, (MatrixOfChoicesSurveyQuestion) surveyQuestion, questionNumber, responseMetadata);

                } else if (surveyQuestion instanceof SliderSurveyQuestion) {

                    questionWidget = new SliderQuestionWidget(surveyProperties, (SliderSurveyQuestion) surveyQuestion, questionNumber, responseMetadata);

                } else {

                    throw new IllegalArgumentException("Unable to create a survey question widget for survey question of type " + surveyQuestion.getClass());
                }

                return questionWidget;

            } catch (Exception e) {

                logger.log(Level.SEVERE, 
                        "Caught an exception while constructing a survey question widget of type "+surveyQuestion.getClass()+" for question with id " + surveyQuestion.getQuestion().getQuestionId() + ".  The error reads: "+e.getMessage(),
                        e);
                return null;
            }

        } else {

            logger.severe("Caught an exception while constructing a survey question widget: Question is null");
            return null;
        }
    }
}
