/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.survey;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.common.gwt.client.JsniUtility;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.survey.AbstractQuestionResponse;
import mil.arl.gift.common.survey.AbstractQuestionResponseMetadata;
import mil.arl.gift.common.survey.FillInTheBlankSurveyQuestion;
import mil.arl.gift.common.survey.QuestionResponseElement;
import mil.arl.gift.common.survey.SurveyProperties;

/**
 * A widget for a highlight passage survey question.
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
public class HighlightPassageQuestionWidget extends AbstractSurveyQuestionWidget<FillInTheBlankSurveyQuestion> {
    
    /** A list of the selection handlers that should be notified when the user selects text in the document */
    private static List<Command> selectionHandlers = new ArrayList<>();
    
    static {
        
        //listen for mouse up events anywhere in the DOM in case the user is selecting text
        Event.addNativePreviewHandler(new NativePreviewHandler() {

            @Override
            public void onPreviewNativeEvent(NativePreviewEvent event) {

                if (event.isCanceled()) {
                    return;
                }

                // If the event targets the popup or the partner, consume it
                Event nativeEvent = Event.as(event.getNativeEvent());

                // Switch on the event type
                int type = nativeEvent.getTypeInt();
                switch (type) {

                case Event.ONMOUSEUP:
                    // Don't eat events if event capture is enabled, as this can
                    // interfere with dialog dragging, for example.
                    if (DOM.getCaptureElement() != null) {
                        event.consume();
                        return;
                    }

                    //notify listeners when a mouse up event is detected
                    for(Command handler : selectionHandlers) {
                        handler.execute();
                    }
                    
                    break;
                }
            }
        });
    }
    
    /** The time that the author answered this widget's question */
    private Date timeAnswered;
    
    /** The original text passage for the user to highlight */
    private String originalPassage;
    
    /** Whether or not the user has submitted their highlighting */
    boolean isSubmitted = false;

    /** A command used to handle executing logic when the user selects text in the document */
    private Command selectionHandler;

    /**
     * Constructor, creates a widget for answering ahighlight passage question
     *
     * @param surveyProperties The properties of the survey this question is in
     * @param surveyQuestion The survey question to ask
     * @param questionNumber The number of the question on the page
     * @param isBeingEdited If this widget is for a question being modified
     */
    public HighlightPassageQuestionWidget(SurveyProperties surveyProperties, final FillInTheBlankSurveyQuestion surveyQuestion, int questionNumber, boolean isBeingEdited) {
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
        
        originalPassage = questionHtml.getHTML();
        
        final Button highlightResetButton = new Button("Undo Highlighting");
        highlightResetButton.setIcon(IconType.UNDO);
        highlightResetButton.setType(ButtonType.PRIMARY);
        highlightResetButton.getElement().getStyle().setProperty("margin-right", "5px");
        highlightResetButton.setEnabled(false);
        highlightResetButton.setVisible(false);
        
        final Button submitButton = new Button("Submit Highlighting");
        submitButton.setIcon(IconType.CHECK_CIRCLE);
        submitButton.setType(ButtonType.SUCCESS);      
        submitButton.setEnabled(false);
        submitButton.setVisible(false);
        
        highlightResetButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent arg0) {
				
				JsniUtility.clearBrowserSelection();
				
				questionHtml.setHTML(originalPassage);
				
				highlightResetButton.setEnabled(false);
				highlightResetButton.setVisible(false);
				submitButton.setVisible(false);
				submitButton.setEnabled(false);
			}
		});
        
        final FlowPanel idealAnswerArea = new FlowPanel();
        
        submitButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent arg0) {
				
				isSubmitted = true;
				
				// remove any existing error/warning messages.
				resetStatus();
				
				idealAnswerArea.setVisible(true);
				idealAnswerArea.getElement().getStyle().setProperty("display", "block");
				
				highlightResetButton.setEnabled(false);
				highlightResetButton.setVisible(false);
				submitButton.setVisible(false);
				submitButton.setEnabled(false);
			}
		});
        
        FlowPanel answerArea = new FlowPanel();
        answerArea.add(highlightResetButton);
        answerArea.add(submitButton);
        
        answerArea.getElement().getStyle().setProperty("display", "block");
        
        addAnswerArea(answerArea);

        idealAnswerArea.setVisible(false);
        
        if(surveyQuestion.getQuestion().getProperties().hasProperty(SurveyPropertyKeyEnum.CORRECT_ANSWER)){
            idealAnswerArea.add(new HTML("<hr style='border-color: rgb(200, 200, 200);'/>"));

            HorizontalPanel hPanel = new HorizontalPanel();
            Image image = new Image("images/pencil_professor-64.png");
            image.getElement().getStyle().setMarginRight(10, Unit.PX);
            hPanel.add(image);
            hPanel.setCellVerticalAlignment(image, HorizontalPanel.ALIGN_MIDDLE);
            
            HTML html = new HTML("<div style='font-size: 24px; font-weight=bold;'>Expert Highlighting</div>");
            hPanel.add(html);
            hPanel.setCellVerticalAlignment(html, HorizontalPanel.ALIGN_MIDDLE);
            
            idealAnswerArea.add(hPanel);
            idealAnswerArea.add(new HTML("<div style='margin-bottom: 5px; margin-top: 10px;'>"
                    + "To help you evaluate your understanding, here is some example highlighting generated by an expert:" + "</div>"));

            HTML correctSummary = new HTML();
            correctSummary.setHTML((String) surveyQuestion.getQuestion().getProperties().getPropertyValue(SurveyPropertyKeyEnum.CORRECT_ANSWER));
            correctSummary.addStyleName(SurveyCssStyles.SURVEY_CORRECT_ANSWER_BLOCK);
            correctSummary.getElement().getStyle().setPadding(5, Unit.PX);
            idealAnswerArea.add(correctSummary);
        }                  
                
        addAnswerArea(idealAnswerArea);
        
        selectionHandler = new Command() {
            
            @Override
            public void execute() {
                
                if(!isSubmitted){
                    
                    highlightSelection(questionHtml.getElement());
                        
                    if(!originalPassage.equals(questionHtml.getHTML())){
                        
                        timeAnswered = new Date();
                        
                        highlightResetButton.setEnabled(true);
                        highlightResetButton.setVisible(true);
                        submitButton.setVisible(true);
                        submitButton.setEnabled(true);
                        
                    } else {
                        highlightResetButton.setEnabled(false);
                        highlightResetButton.setVisible(false);
                        submitButton.setVisible(false);
                        submitButton.setEnabled(false);
                    }
                }
            }
        };
    }
    
    @Override
    protected void onLoad() {
        super.onLoad();
        
        //listen for when the user selects text in the document as long as this widget is loaded
        selectionHandlers.add(selectionHandler);
    }
    
    @Override
    protected void onUnload() {
        super.onUnload();
        
        //if this widget is unloaded, stop listening to user text selection
        selectionHandlers.remove(selectionHandler);
    }

    /**
     * Constructor, creates a widget for reviewing the response to a highlight passage question
     *
     * @param surveyProperties The properties of the survey this question is in
     * @param surveyQuestion The survey question to ask
     * @param questionNumber The number of the question on the page
     * @param responseMetadata the response to this question
     */
    public HighlightPassageQuestionWidget(SurveyProperties surveyProperties, FillInTheBlankSurveyQuestion surveyQuestion, int questionNumber, AbstractQuestionResponseMetadata responseMetadata) {
        this(surveyProperties, surveyQuestion, questionNumber, true);

        if (!responseMetadata.getResponses().isEmpty()) {

            if (responseMetadata.getResponses().size() > 1) {

                GWT.log("Warning: Got multiple responses back for a fill in the blank question. Expecting only 1, using the first response by default.");
            }
            
            questionHtml.setHTML(responseMetadata.getResponses().get(0).getText());
            
            addCorrectAnswerPanel(surveyQuestion);
        }
    }

    /**
     * Adds a panel displaying the correct answer of the question this widget represents
     * 
	 * @param surveyQuestion the question this widget represent
	 */
	private void addCorrectAnswerPanel(FillInTheBlankSurveyQuestion surveyQuestion) {		 
         
         if(surveyQuestion.getQuestion().getProperties().hasProperty(SurveyPropertyKeyEnum.CORRECT_ANSWER)){
         	
            FlowPanel idealAnswerArea = new FlowPanel();
            idealAnswerArea.getElement().getStyle().setProperty("display", "block");
            
            FlowPanel containerPanel = new FlowPanel();
            containerPanel.addStyleName(SurveyCssStyles.SURVEY_QUESTION_STYLE);
            
            containerPanel.add(new HTML("<hr style='border-color: rgb(200, 200, 200);'/><b>Ideal Answer:</b>"));
            containerPanel.add(new HTML(
            		(String) surveyQuestion.getQuestion().getProperties().getPropertyValue(SurveyPropertyKeyEnum.CORRECT_ANSWER)
            ));
            
            idealAnswerArea.add(containerPanel);
            
            addAnswerArea(idealAnswerArea);
         }
	}

	@Override
    public AbstractQuestionResponse getAnswer(boolean validate) throws MalformedAnswerException {
    	
    	String answerText = questionHtml.getHTML();

        if (originalPassage.equals(answerText)) {

            throw new MalformedAnswerException("No highlighting has been applied to the given passage.", getSurveyElement().getIsRequired());
        }
    	
    	if(!isSubmitted){
    		throw new MalformedAnswerException("Highlighting has not been submitted for this question. ", getSurveyElement().getIsRequired());
    	}
    	
        return AbstractQuestionResponse.createResponse(getSurveyElement(), Collections.singletonList(new QuestionResponseElement(answerText, timeAnswered)));
    }
	    
    @Override
    public void setExternalQuestionResponse(AbstractQuestionResponse questionResponse) {

        //currently not supported
        throw new DetailedException("Unable to apply an external question response to a slider question type.", "This logic has not been implemented yet.", null);
    }
    
    /**
	 * Highlights any currently selected text inside the given element, changing the text's background color and returning
	 * a string of HTML representing the selected text. If no text is currently selected in the browser window or if
	 * the selected text is less than 1 character long, this method will simply return null.
	 * 
	 * @param target the element within which to highlight the current selection, if one exists
	 * @return String a string containing the raw HTML that was highlighted
	 */
	protected native String highlightSelection(Element target)/*-{
		
		if($wnd.getSelection){
			
			// Nick: The following two functions are based on a solution for determining whether or not the window's
			// selected text is contained within a certain DOM element. This solution was found at
			// http://stackoverflow.com/questions/8339857/how-to-know-if-selected-text-is-inside-a-specific-div
			
			//checks to see if the specified container node is or contains the given node
			function isOrContains(node, container) {
			    while (node) {
			        if (node === container) {
			            return true;
			        }
			        node = node.parentNode;
			    }
			    return false;
			}
			
			//checks to see if the given element contains the browser's currently selected text
			function elementContainsSelection(el) {
			    var sel;
			    if ($wnd.getSelection) {
			        sel = $wnd.getSelection();
			        if (sel.rangeCount > 0) {
			            for (var i = 0; i < sel.rangeCount; ++i) {
			                if (!isOrContains(sel.getRangeAt(i).commonAncestorContainer, el)) {
			                    return false;
			                }
			            }
			            return true;
			        }
			    }
			    return false;
			}
			
			//determine if the target element contains the current selection
			if(elementContainsSelection(target)){				  

				var selection = $wnd.getSelection();
								
		        if (selection.rangeCount > 0) {
		        	
		          range = selection.getRangeAt(0);
		          
		          //determine if the current selection has 1 or more characters
		          if(!range.collapsed){
		          	
		          	// Nick: The following two functions are based on a solution for highlighting text found at 
		          	// http://stackoverflow.com/questions/2582831/how-can-i-highlight-the-text-of-the-dom-range-object
		          	
		          	//briefly turns on the browser's document editing mode and applies highlighting to the currently selected range
		          	function makeEditableAndHighlight(colour) {
					    var range, sel = $wnd.getSelection();
					    if (sel.rangeCount && sel.getRangeAt) {
					        range = sel.getRangeAt(0);
					    }
					    $doc.designMode = "on";
					    if (range) {
					        sel.removeAllRanges();
					        sel.addRange(range);
					    }
					    // Use HiliteColor since some browsers apply BackColor to the whole block
					    if (!$doc.execCommand("HiliteColor", false, colour)) {
					        $doc.execCommand("BackColor", false, colour);
					    }
					    $doc.designMode = "off";
					};
					
					//highlights the currently selected text with the given color
					function highlight(colour) {
					    var range;
					    if ($wnd.getSelection) {
					        // IE9 and non-IE
					        try {
					            if (!$doc.execCommand("BackColor", false, colour)) {
					                makeEditableAndHighlight(colour);
					            }
					        } catch (ex) {
					            makeEditableAndHighlight(colour)
					        }
					    } else if ($doc.selection && $doc.selection.createRange) {
					        // IE <= 8 case
					        range = $doc.selection.createRange();
					        range.execCommand("BackColor", false, colour);
					    }
					};
		
					//highlight the currently selected text yellow
					highlight("yellow");
					
					//get the text that was selected as a string of HTML
			        var clonedSelection = range.cloneContents();
			        var div = $doc.createElement('div');
			        div.appendChild(clonedSelection);
			        
			        return div.innerHTML;
		          }
		        }
			}
		}
		
		return null;
		
	}-*/;

}
