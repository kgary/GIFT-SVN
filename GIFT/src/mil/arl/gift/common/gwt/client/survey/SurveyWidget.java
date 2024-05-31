/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.survey;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.HasCloseHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.HeadingSize;

import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.survey.CurrentQuestionAnsweredCallback;
import mil.arl.gift.common.survey.Survey;
import mil.arl.gift.common.survey.SurveyPage;
import mil.arl.gift.common.survey.SurveyPageResponse;
import mil.arl.gift.common.survey.SurveyPageResponseMetadata;
import mil.arl.gift.common.survey.SurveyResponse;
import mil.arl.gift.common.survey.SurveyResponseMetadata;

/**
 * A GWT Widget for displaying a survey
 *
 * @author jleonard
 */
public class SurveyWidget extends Composite implements HasCloseHandlers<SurveyResponse> {
    
    /** The logger. */
    private static Logger logger = Logger.getLogger(SurveyWidget.class.getName());

    private final ArrayList<SurveyPageWidget> surveyPageWidgets = new ArrayList<SurveyPageWidget>();

    private ListIterator<SurveyPageWidget> surveyPageIterator;

    private SurveyPageWidget currentSurveyPage = null;

    private SimplePanel surveyPagePanel = new SimplePanel();

    private final Survey survey;
    
    private final FlowPanel statusFooter = new FlowPanel();

    private final Button nextButton = new Button("Next");

    private final Date surveyStartTime = new Date();

    private final ScrollPanel scrollPanel;
    
    private final FlowPanel verticalPanel = new FlowPanel();

    private boolean previewMode;
    
    /** 
     * whether the current survey page continue button has been clicked already and resulted in showing a warning/error message
     * about missing question responses 
     */
    private boolean verificationClick = false;

    private TextBox bugTextBox = new TextBox();
    
    private SurveyCompletionStatus surveyCompletionStatus = SurveyCompletionStatus.NOT_SUBMITTED;
    
    /** Labels that are used for the aar button.  */
    private final String LABEL_NEXTPAGE = "Next Page";
    private final String LABEL_BACKTOSUMMARY = "Back to Summary";
    
    /**
     * Constructor, creates a widget for answering a survey
     *
     * @param survey The survey to display
     * @param previewMode If the survey is being previewed
     * @param isDebug if the survey widget should render in debug mode (e.g. color code scored answers)
     */
    public SurveyWidget(Survey survey, boolean previewMode, boolean isDebug) {
        this(survey, previewMode, isDebug, null);
    }
    
    /**
     * Constructor, creates a widget for reviewing the answer to a page
     *
     * @param survey The survey to display
     * @param previewMode If the survey is being previewed
     * @param isDebug if the survey widget should render in debug mode (e.g. color code scored answers)
     * @param surveyResponseMetadata the optional response metadata to a survey represented by this widget
     */
    public SurveyWidget(Survey survey, final boolean previewMode, final boolean isDebug, final SurveyResponseMetadata surveyResponseMetadata) {

        if(isDebug){
            logger.info("Creating debug view of survey");
        }

        this.survey = survey;
        setPreviewModeEnabled(previewMode);

        final FlowPanel wrapper = new FlowPanel();
        verticalPanel.addStyleName("center surveyContentPanel");
        scrollPanel = new ScrollPanel(wrapper);
        scrollPanel.setHeight("100%");
        this.initWidget(scrollPanel);

        /*
         * There is an odd bug that is a result of some combination of text 
         * boxes, iFrames, and Internet Explorer. The text boxes would somehow
         * get into a state where clicking on it would be ignored thus
         * preventing the user from typing in it. I've found that putting a
         * text box in the highest GUI element, scheduling this delayed
         * command, and then making the text box invisible prevents the bug
         * from happening. I don't know why this works. - Ed LaFave
         */
        ScheduledCommand command = new ScheduledCommand() {
			@Override
			public void execute() {
				bugTextBox.setFocus(true);
				bugTextBox.setVisible(false);

				// Reset the scrollbar position since the page scrolls to the bottom when the textbox is focused.
				scrollPanel.setVerticalScrollPosition(0);
			}
		};
        Scheduler.get().scheduleDeferred(command);
        
        loadSurvey(isDebug, surveyResponseMetadata);
        
        if (!survey.getProperties().getHideSurveyName()) {
            
            FlowPanel surveyNamePanel = new FlowPanel();
            surveyNamePanel.addStyleName(SurveyCssStyles.SURVEY_NAME_HEADER_PANEL);
            Label surveyNameLabel = new Label(survey.getName());
            surveyNameLabel.addStyleName(SurveyCssStyles.SURVEY_NAME_STYLE);
            
            surveyNamePanel.add(surveyNameLabel);
            
            wrapper.add(surveyNamePanel);
        }
        
        wrapper.add(verticalPanel);
        
        verticalPanel.add(bugTextBox);
        
        verticalPanel.add(surveyPagePanel);
        
        HorizontalPanel footerPanel = new HorizontalPanel();
        
        statusFooter.getElement().getStyle().setDisplay(Style.Display.INLINE_BLOCK);
        statusFooter.getElement().getStyle().setProperty("marginTop", "15px");
        statusFooter.getElement().getStyle().setProperty("marginRight", "10px");
        statusFooter.getElement().getStyle().setProperty("float", "left");
        
        footerPanel.add(statusFooter);
        footerPanel.setCellWidth(statusFooter, "100%");
        
        FlowPanel continuePanel = new FlowPanel();
        
        continuePanel.add(nextButton);
        continuePanel.addStyleName(SurveyCssStyles.SURVEY_CONTINUE_BUTTON_STYLE);
        
        footerPanel.addStyleName(SurveyCssStyles.SURVEY_FOOTER_AREA);
        footerPanel.add(continuePanel);
        
        verticalPanel.add(footerPanel);
        
        nextButton.setType(ButtonType.PRIMARY);
        nextButton.addStyleName("continueButton");
        nextButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {

                if(SurveyWidget.this.previewMode){
                    // don't check if the survey page is complete
                 
                    displayNextPage();
                    
                    verificationClick = false;
                }else{
                    
                    // NOTE: calling isComplete() will cause warning/error messages to be shown on questions
                    //       if a question is missing an answer, something we don't currently want to do in preview mode
                    surveyCompletionStatus = currentSurveyPage.isComplete();
                    
                    switch (surveyCompletionStatus) {
                    case COMPLETE:
                        //the survey page was completed, allow learner to continue
                        
                        displayNextPage();
                        
                        verificationClick = false;
                        break;
                        
                    case MISSING_OPTIONAL:
                        //the survey page is missing optional question only answer(s)
                        
                        if(verificationClick){
                            //the learner was already warned about missing optional questions
                            //allow them to continue on the 2nd click of the survey page continue button
                            
                            displayNextPage();
                            
                            verificationClick = false;
                        }else{
                            //Warn the learner about the missing answers
                            
                            statusFooter.clear();
                            
                            //show different labels if there is one required question unanswered
                            Label status = new Label("Warning, one or more optional questions are unanswered. Press the button again to continue.");
                            status.addStyleName(SurveyCssStyles.SURVEY_WARNING_LABEL_STYLE);
                            
                            statusFooter.add(status);
                            scrollPanel.setVerticalScrollPosition(verticalPanel.getOffsetHeight() + 1);
                            
                            verificationClick = true; //don't warn a 2nd time, allow learner to continue to next page
                        }
                        
                        break;
                        
                    case MISSING_REQUIRED:
                        //always show the error message about missing required answers
                        
                        statusFooter.clear();
                            
                        Label status = new Label("One or more required questions are unanswered.");
                        status.addStyleName(SurveyCssStyles.SURVEY_ERROR_LABEL_STYLE);
                        
                        statusFooter.add(status);
                        scrollPanel.setVerticalScrollPosition(verticalPanel.getOffsetHeight() + 1);
                        
                        verificationClick = true;
                        
                        break;

                    default:
                        //if another enumeration was received, show a default message
                        
                        statusFooter.clear();
                        Label defaultStatus = new Label("There is a problem with one or more responses");
                        defaultStatus.addStyleName(SurveyCssStyles.SURVEY_ERROR_LABEL_STYLE);
                        statusFooter.add(defaultStatus);
                        scrollPanel.setVerticalScrollPosition(verticalPanel.getOffsetHeight() + 1);
                        break;
                    }                    
                    
                }
                
            }
        });
                
    }
    
    /**
     * Create survey page widgets for the survey.  This will also replace the current and upcoming
     * survey page widgets with new widgets.
     * 
     * @param isDebug if the survey widget should render in debug mode (e.g. color code scored answers)
     * @param surveyResponseMetadata the optional response metadata to a survey represented by this widget
     */
    public void loadSurvey(final boolean isDebug, final SurveyResponseMetadata surveyResponseMetadata){
            
        // Determine the index of the current page in the survey being shown to know
        // which survey page needs a new widget created for it.
        int startingIndex = 0;
        if(surveyPageIterator != null && surveyPageIterator.hasPrevious()){
            int nextPageToBuildIndex = surveyPageIterator.nextIndex();  //index of the next survey page to be rendered
            startingIndex = nextPageToBuildIndex - 1; //index of the current survey page widget needing to be replaced
        }

        for (int index = startingIndex; index < survey.getPages().size(); index++) {
            
            SurveyPage surveyPage = survey.getPages().get(index);
            
            if (surveyResponseMetadata != null) {
                
                for (SurveyPageResponseMetadata surveyPageResponseMetadata : surveyResponseMetadata.getSurveyPageResponses()) {
                    
                    if (surveyPage.getId() == surveyPageResponseMetadata.getSurveyPageId()) {

                        if(index < surveyPageWidgets.size()){
                            //replace an existing survey page widget
                            surveyPageWidgets.set(index, new SurveyPageWidget(survey.getProperties(), surveyPage, surveyPageResponseMetadata, isDebug));
                        }else{
                            //create a new survey page widget where one didn't exist before
                            surveyPageWidgets.add(new SurveyPageWidget(survey.getProperties(), surveyPage, surveyPageResponseMetadata, isDebug));
                        }
                    }
                }
                
            } else {
                
                if(index < surveyPageWidgets.size()){
                    //replace an existing survey page widget
                    surveyPageWidgets.set(index, new SurveyPageWidget(survey.getProperties(), surveyPage, isDebug));
                }else{
                    //create a new survey page widget where one didn't exist before
                    surveyPageWidgets.add(new SurveyPageWidget(survey.getProperties(), surveyPage, isDebug));
                }

            }
        }
        
        //start the iterator at the first widget that was created again
        surveyPageIterator = surveyPageWidgets.listIterator(startingIndex);    
        
        //render the survey page widget
        displayNextPage();
    }

    /**
     * Creates a flow panel (which can be used by the caller) that displays the survey widget element.
     * 
     * @param closeButton The handler for when the close button is pressed (to close the widget).
     * @return the widget displaying the results for the survey member
     */
    public FlowPanel displaySurveyResponseWidget(final ClickHandler closeButton) {
        
        // Do initialization work here 
        surveyPageIterator = surveyPageWidgets.listIterator();
    	verticalPanel.clear();

    	// Setup the main panel that the survey results will be shown in.
    	final FlowPanel mainPanel = new FlowPanel();
    	
    	// Create the header of the main panel.
    	FlowPanel headerPanel = new FlowPanel();
    	headerPanel.addStyleName("surveyAarHeaderPanel");
    	Heading header = new Heading(HeadingSize.H4, "Viewing Survey Results (use button at the bottom to close)");
    	header.addStyleName("surveyAarHeader");
    	headerPanel.add(header);
    	mainPanel.add(headerPanel);
    	
    	// Add the survey page panel with proper styling.
    	verticalPanel.add(surveyPagePanel);
        verticalPanel.removeStyleName("surveyContentPanel");
        verticalPanel.addStyleName("surveyContentPanelAar");

        // Setup the button on the bottom of the panel which will either advance the page, or close 
        // the survey.
        final Button continueButton = new Button();
        continueButton.addStyleName("continueButton");
        continueButton.setType(ButtonType.PRIMARY);
        continueButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				
				if (surveyPageIterator.hasNext()) {
					displayNextPage();
					
					if (surveyPageIterator.hasNext()) {
					    continueButton.setText(LABEL_NEXTPAGE);
			        } else {
			            continueButton.setText(LABEL_BACKTOSUMMARY);
			        }

				} else {
				    
				    closeButton.onClick(event);
				}
			}

        });
        
        // Create a footer panel with a continue button with proper styling.
        FlowPanel continuePanel = new FlowPanel();
        continuePanel.add(continueButton);
        continuePanel.addStyleName(SurveyCssStyles.SURVEY_CONTINUE_BUTTON_STYLE);
        
        HorizontalPanel footerPanel = new HorizontalPanel();
        
        statusFooter.getElement().getStyle().setDisplay(Style.Display.INLINE_BLOCK);
        statusFooter.getElement().getStyle().setProperty("marginTop", "15px");
        statusFooter.getElement().getStyle().setProperty("marginRight", "10px");
        statusFooter.getElement().getStyle().setProperty("float", "left");
        
        footerPanel.add(statusFooter);
        footerPanel.setCellWidth(statusFooter, "100%");
        
        footerPanel.addStyleName(SurveyCssStyles.SURVEY_FOOTER_AREA);
        footerPanel.add(continuePanel);
        verticalPanel.add(footerPanel);
        
        // Add the vertical panel.
        mainPanel.add(verticalPanel);
        
        // Display the first page of the survey (this loads the widgets/elements of the survey).
        displayNextPage();
        
        // Setup the butotn label properly based on if there is more pages to the survey.
        String buttonLabel;
        if (!surveyPageIterator.hasNext()) {
            
            buttonLabel = LABEL_BACKTOSUMMARY;
        } else {
            buttonLabel = LABEL_NEXTPAGE;
        }
        continueButton.setText(buttonLabel);
        
        return mainPanel;
    }
    
    /**
     * Set the callback used for notification of a survey question being answered.
     * 
     * @param questionAnsweredCallback can be null.
     */
    public void setCurrentQuestionAnsweredCallback(CurrentQuestionAnsweredCallback questionAnsweredCallback){
        
        for(SurveyPageWidget surveyPageWidget : surveyPageWidgets){
            surveyPageWidget.setCurrentQuestionAnsweredCallback(questionAnsweredCallback);
        }
    }

    /**
     * Displays the next page in the survey or submits the results if there are
     * no more to show
     */
    private void displayNextPage() {
        
        statusFooter.clear();
        
        if (surveyPageIterator.hasNext()) {

            if (previewMode || currentSurveyPage == null || currentSurveyPage.isComplete() != SurveyCompletionStatus.MISSING_REQUIRED) {

                if (currentSurveyPage != null) {

                    currentSurveyPage.onSurveyPageEnd();
                }

                SurveyPageWidget surveyPageWidget = surveyPageIterator.next();
                currentSurveyPage = surveyPageWidget;
                currentSurveyPage.onSurveyPageStart();
                surveyPagePanel.clear();
                surveyPagePanel.add(currentSurveyPage);
                
                logger.info("display survey page index "+(surveyPageIterator.nextIndex()-1));

                if (surveyPageIterator.nextIndex() == surveyPageWidgets.size()) {

                    if(previewMode){
                        nextButton.setText("Close Survey");
                    }else{
                        nextButton.setText("Complete Survey");
                    }

                } else {

                    nextButton.setText("Next Page");
                }
                
                scrollPanel.setVerticalScrollPosition(0);
                Window.scrollTo(0, 0);
            }

        } else {

            nextButton.setEnabled(false);
            currentSurveyPage.onSurveyPageEnd();
            submitSurvey();
        }
    }

    /**
     * Submits the survey results
     */
    private void submitSurvey() {

        final List<SurveyPageResponse> surveyResults = new ArrayList<SurveyPageResponse>();
        ListIterator<SurveyPageWidget> iterator = surveyPageWidgets.listIterator();
        boolean exceptionCaught = false;
        
        while (iterator.hasNext()) {

            SurveyPageWidget widget = iterator.next();

      
            try {

                SurveyPageResponse surveyPageResponse = widget.getResponse();
                surveyResults.add(surveyPageResponse);

            } catch (@SuppressWarnings("unused") MalformedAnswerException e) {

                if (!exceptionCaught) {

                    surveyPageIterator = iterator;
                    currentSurveyPage = widget;
                    surveyPagePanel.clear();
                    surveyPagePanel.add(currentSurveyPage);
                }

                exceptionCaught = true;
            }
        }
   
        if (previewMode || !exceptionCaught) {

            nextButton.setEnabled(false);
            
            SurveyResponse surveyResponse = new SurveyResponse();
            surveyResponse.setSurveyStartTime(surveyStartTime);
            surveyResponse.setSurveyEndTime(new Date());
            surveyResponse.setSurvey(survey);
            surveyResponse.getSurveyPageResponses().clear();
            surveyResponse.getSurveyPageResponses().addAll(surveyResults);
            
            CloseEvent.fire(SurveyWidget.this, surveyResponse);
        }
    }
    
    /**
     * Populate the correct widget component with the answers to survey questions provided by some other logic
     * external to the tutor.  For example, with a multiple choice question the choices provided in
     * the response should have their radio/checkbox components selected in the tutor client to indicate
     * the question was answered.
     *  
     * @param surveyResponse contains responses to this survey.  Can be null.  Can also have no actual responses as a no
     * response is a valid question response.
     */
    protected void applyExternalSurveyResponse(final SurveyResponse surveyResponse){
        
        if(surveyResponse == null){
            return;
        } else {
            if(survey.getId() != surveyResponse.getSurveyId()){
                //ERROR
                throw new DetailedException("Unable to apply the external survey response because the response is for a different survey.", 
                        "The current survey being presented has a survey id of "+survey.getId()+" and the survey response has a survey id of "+surveyResponse.getSurveyId()+".", null);
            }else{
                
                logger.info("Setting survey response from external source.");
                
                boolean applied;
                for(SurveyPageResponse surveyPageResponse : surveyResponse.getSurveyPageResponses()){
                    
                    applied = false;
                    for(SurveyPageWidget surveyPageWidget : surveyPageWidgets){
                        
                        if(surveyPageResponse.getSurveyPage().getId() == surveyPageWidget.getSurveyPageId()){
                            surveyPageWidget.applyExternalSurveyResponse(surveyPageResponse);
                            applied = true;
                            break;
                        }
                    }
                    
                    if(!applied){
                        //ERROR
                        throw new DetailedException("Failed to apply the external survey response for a survey page.", 
                                "Could not find the survey page widget associated with the survey page id "+surveyPageResponse.getSurveyPage().getId()+" from the survey response.", null);
                    }
                }
            }
        }
    }
    
    /**
     * Submit the answers for the current survey page.  This is used by logic external to the tutor 
     * such as when an external training application requests to submit the survey.  If this is the last
     * page then the survey is submitted, otherwise the next survey page is shown.
     */
    protected void applyExternalSurveySubmit(){
        
        logger.info("Submitting survey (page) from external source.");
        
        displayNextPage();
    }

    @Override
    public HandlerRegistration addCloseHandler(CloseHandler<SurveyResponse> handler) {

        return addHandler(handler, CloseEvent.getType());
    }
    
    /**
     * Sets whether or not this survey widget should behave like a preview for the user
     * 
     * @param enabled whether or not preview mode should be enabled
     */
    public void setPreviewModeEnabled(boolean enabled){
    	this.previewMode = enabled;
    }
}
