/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.survey;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.event.dom.client.ContextMenuHandler;
import com.google.gwt.event.dom.client.DropEvent;
import com.google.gwt.event.dom.client.DropHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;

import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.VerticalResizeTextArea;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.survey.AbstractQuestionResponse;
import mil.arl.gift.common.survey.AbstractQuestionResponseMetadata;
import mil.arl.gift.common.survey.FillInTheBlankSurveyQuestion;
import mil.arl.gift.common.survey.QuestionResponseElement;
import mil.arl.gift.common.survey.SurveyProperties;

/**
 * A widget for a summarize passage survey question
 * <br/><br/>
 * This question widget is somewhat unique in that it doesn't actually have a unique question type assigned to it and, instead,
 * acts as a modification of {@link FreeResponseQuestionWidget} that modifies its appearance heavily based on a modified set of
 * survey item properties that are documented below
 * <br/>
 * <ul>
 * 		<li>
 * 			{@link SurveyPropertyKeyEnum#IS_REMEDIATION_CONTENT} - Used to identify {@link mil.arl.gift.common.survey.FillInTheBlankQuestion 
 * 			FillInTheBlankQuestions} that should be presented using a modified version of {@link FreeResponseQuestionWidget}, like
 * 			{@link HighlightPassageQuestionWidget} or {@link SummarizePassageQuestionWidget}
 * 		</li>
 * 		<br/>
 * 		<li>
 * 			{@link SurveyPropertyKeyEnum#IS_ANSWER_FIELD_TEXT_BOX_KEY} - Used to determine whether or not a modified FillIntheBlankQuestion
 * 			is a Summarize Passage question that should go to {@link SummarizePassageQuestionWidget}
 * 		</li>
 * 		<br/>
 * 		<li>
 * 			{@link SurveyPropertyKeyEnum#INSTRUCTION_TEXT} - Used to save instructions for highlight and summarize questions 
 * 		</li>
 * 		<br/>
 * 		<li>
 * 			{@link SurveyPropertyKeyEnum#CORRECT_ANSWER} - Used to save the ideal highlighting/summary for highlight and summarize questions 
 * 		</li>
 * </ul>
 * 
 * @author nroberts
 */
public class SummarizePassageQuestionWidget extends AbstractSurveyQuestionWidget<FillInTheBlankSurveyQuestion> {
    
    private Date timeAnswered;
    
    /** Whether or not the user has submitted their highlighting */
    boolean isSubmitted = false;
    
    /** Text area used for the learner's summary. */
    private VerticalResizeTextArea summaryArea;
    
    private static final int MIN_ANSWER_LENGTH = 20;

    /**
     * Constructor, creates a widget for answering ahighlight passage question
     *
     * @param surveyProperties The properties of the survey this question is in
     * @param surveyQuestion The survey question to ask
     * @param questionNumber The number of the question on the page
     * @param isBeingEdited If this widget is for a question being modified
     */
    public SummarizePassageQuestionWidget(SurveyProperties surveyProperties, final FillInTheBlankSurveyQuestion surveyQuestion, int questionNumber, boolean isBeingEdited) {
        super(surveyProperties, surveyQuestion, questionNumber, isBeingEdited);  
        
        if(surveyQuestion.getQuestion().getProperties().hasProperty(SurveyPropertyKeyEnum.INSTRUCTION_TEXT)){
            HorizontalPanel hPanel = new HorizontalPanel();
            
            Image image = new Image("images/instructions-36.png");
            image.getElement().getStyle().setMarginRight(10, Unit.PX);
            hPanel.add(image);
            hPanel.setCellVerticalAlignment(image, HorizontalPanel.ALIGN_MIDDLE);
            
            HTML instructionsHtml = new HTML((String) surveyQuestion.getQuestion().getProperties().getPropertyValue(SurveyPropertyKeyEnum.INSTRUCTION_TEXT));
            instructionsHtml.setStyleName(SurveyCssStyles.SURVEY_QUESTION_STYLE);
            instructionsHtml.getElement().getStyle().setPaddingBottom(0, Unit.PX);
            instructionsHtml.getElement().getStyle().setMarginBottom(0, Unit.PX);
            instructionsHtml.addStyleName(SurveyCssStyles.SURVEY_QUESTION_CHILD_NO_MARGIN);
            hPanel.add(instructionsHtml);
            hPanel.setCellVerticalAlignment(instructionsHtml, HorizontalPanel.ALIGN_MIDDLE);
            
            questionPanel.insert(hPanel, questionPanel.getWidgetIndex(questionHtml));
            questionPanel.insert(new HTML("<hr style='border-color: rgb(200, 200, 200);'/>"), questionPanel.getWidgetIndex(questionHtml));
        }
        
        summaryArea = new VerticalResizeTextArea();
        summaryArea.setWidth("100%");
        summaryArea.setPlaceholder("Enter your summary here");
        // Hides the toolbar
        summaryArea.setShowToolbar(false);
        
        final Button submitButton = new Button("Submit Summary");
        submitButton.setIcon(IconType.CHECK_CIRCLE);
        submitButton.setType(ButtonType.SUCCESS);      
        submitButton.setEnabled(false);
        submitButton.setVisible(false);
        submitButton.getElement().getStyle().setProperty("marginTop", "10px");
        
        summaryArea.addKeyDownHandler(new KeyDownHandler() {
			
			@Override
			public void onKeyDown(KeyDownEvent event) {
				
				if(event.isControlKeyDown() && event.getNativeKeyCode() == KeyCodes.KEY_V){
					
					//prevent learners from using Ctrl + V to paste text into the summary area
					event.preventDefault();
					event.stopPropagation();
				}
				
				Scheduler.get().scheduleDeferred(new ScheduledCommand() {
					
					@Override
					public void execute() {
						
						if(summaryArea.getValue() != null && summaryArea.getValueLength() > MIN_ANSWER_LENGTH){
							
							submitButton.setEnabled(true);
							submitButton.setVisible(true);
							
						} else {
							
							submitButton.setEnabled(false);
							submitButton.setVisible(false);
						}
					}
				});
			}
		});
        
        summaryArea.addDomHandler(new ContextMenuHandler() {
			
			@Override
			public void onContextMenu(ContextMenuEvent event) {
				
				//prevent learners from using the context menu to paste text into the summary area
				event.preventDefault();
				event.stopPropagation();
			}
			
		}, ContextMenuEvent.getType());
        
        summaryArea.addDropHandler(new DropHandler() {
			
			@Override
			public void onDrop(DropEvent event) {
				
				//prevent learners from dragging text into the summary area
				event.preventDefault();
				event.stopPropagation();
			}
		});
        
        summaryArea.addValueChangeHandler(new ValueChangeHandler<String>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				
				if(summaryArea.getValue() != null && summaryArea.getValueLength() > MIN_ANSWER_LENGTH){

                    submitButton.setEnabled(true);
                    submitButton.setVisible(true);

                } else {

                    submitButton.setEnabled(false);
                    submitButton.setVisible(false);
                }
				
				timeAnswered = new Date();
			}
		});
        
        final FlowPanel idealAnswerArea = new FlowPanel();
        idealAnswerArea.setVisible(false);
        
        submitButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent arg0) {
				
				isSubmitted = true;
				
				// remove any existing error/warning messages.
                resetStatus();
				
				idealAnswerArea.setVisible(true);
				idealAnswerArea.getElement().getStyle().setProperty("display", "block");
				
				summaryArea.setEnabled(false);
				submitButton.setEnabled(false);
				submitButton.setVisible(false);
			}
		});
        
        FlowPanel answerArea = new FlowPanel();
        answerArea.add(new HTML("<hr style='border-color: rgb(200, 200, 200);'/>"));
        answerArea.add(summaryArea);
        answerArea.add(submitButton);
        
        addAnswerArea(answerArea);
        
        answerArea.getElement().getStyle().setProperty("display", "block");
        
        if (surveyQuestion.getQuestion().getProperties().hasProperty(SurveyPropertyKeyEnum.CORRECT_ANSWER)) {
            idealAnswerArea.add(new HTML("<hr style='border-color: rgb(200, 200, 200);'/>"));

            HorizontalPanel hPanel = new HorizontalPanel();
            
            Image image = new Image("images/pencil_professor-64.png");
            image.getElement().getStyle().setMarginRight(10, Unit.PX);
            hPanel.add(image);
            hPanel.setCellVerticalAlignment(image, HorizontalPanel.ALIGN_MIDDLE);
            
            HTML html = new HTML("<div style='font-size: 24px; font-weight=bold;'>Expert Summary</div>");
            hPanel.add(html);
            hPanel.setCellVerticalAlignment(html, HorizontalPanel.ALIGN_MIDDLE);
            
            idealAnswerArea.add(hPanel);
            idealAnswerArea.add(new HTML("<div style='margin-bottom: 5px; margin-top: 10px;'>"
                    + "To help you evaluate your understanding, here is an example summary generated by an expert:" + "</div>"));

            HTML correctSummary = new HTML();
            correctSummary.setHTML((String) surveyQuestion.getQuestion().getProperties().getPropertyValue(SurveyPropertyKeyEnum.CORRECT_ANSWER));
            correctSummary.addStyleName(SurveyCssStyles.SURVEY_CORRECT_ANSWER_BLOCK);
            correctSummary.getElement().getStyle().setPadding(5, Unit.PX);
            idealAnswerArea.add(correctSummary);
        }

        addAnswerArea(idealAnswerArea);
    }

    /**
     * Constructor, creates a widget for reviewing the response to a highlight passage question
     *
     * @param surveyProperties The properties of the survey this question is in
     * @param surveyQuestion The survey question to ask
     * @param questionNumber The number of the question on the page
     * @param response The response to this question
     */
    public SummarizePassageQuestionWidget(SurveyProperties surveyProperties, FillInTheBlankSurveyQuestion surveyQuestion, int questionNumber, AbstractQuestionResponseMetadata responseMetadata) {
        this(surveyProperties, surveyQuestion, questionNumber, true);

        if (!responseMetadata.getResponses().isEmpty()) {

            if (responseMetadata.getResponses().size() > 1) {

                GWT.log("Warning: Got multiple responses back for a fill in the blank question. Expecting only 1, using the first response by default.");
            }
            
            summaryArea.setValue(responseMetadata.getResponses().get(0).getText());
        }
    }

	@Override
    public AbstractQuestionResponse getAnswer(boolean validate) throws MalformedAnswerException {
    	
        if (summaryArea.getValue() == null || summaryArea.getValueLength() < MIN_ANSWER_LENGTH) {

            throw new MalformedAnswerException("Please write a longer summary.", getSurveyElement().getIsRequired());
        }
    	
    	if(!isSubmitted){
    		throw new MalformedAnswerException(
    				"The entered summary has not been submitted for this question. ", 
    				getSurveyElement().getIsRequired()
    		);
    	}
    	
    	List<QuestionResponseElement> responses = new ArrayList<QuestionResponseElement>();
    	responses.add(new QuestionResponseElement(summaryArea.getValue(), timeAnswered));

        return AbstractQuestionResponse.createResponse(getSurveyElement(), responses);
    }

    
    @Override
    public void setExternalQuestionResponse(AbstractQuestionResponse questionResponse) {

        //currently not supported
        throw new DetailedException("Unable to apply an external question response to a slider question type.", "This logic has not been implemented yet.", null);
    }
    
}
