/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.client.widgets;

import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;

import mil.arl.gift.common.gwt.client.RpcResponse;
import mil.arl.gift.common.survey.AbstractQuestionResponse;
import mil.arl.gift.common.survey.CurrentQuestionAnsweredCallback;
import mil.arl.gift.common.survey.SurveyResponse;
import mil.arl.gift.tutor.client.BrowserSession;
import mil.arl.gift.tutor.client.Document;
import mil.arl.gift.tutor.client.TutorUserWebInterface;
import mil.arl.gift.tutor.shared.CloseAction;
import mil.arl.gift.tutor.shared.SubmitAction;
import mil.arl.gift.tutor.shared.WidgetInstance;
import mil.arl.gift.tutor.shared.properties.SurveyWidgetProperties;
import mil.arl.gift.tutor.shared.properties.WidgetProperties;

/**
 * A Tutor Module widget for displaying a survey
 *
 * @author jleonard
 */
public class DisplaySurveyWidget extends TutorSurveyWidget implements IsRemovableTuiWidget{

    private final String webpageId;
    
    /** Whether or not this widget has notified the server that it is closed*/
    private boolean hasClosed = false;

    /**
     * Constructor
     *
     * @param instance The web page instance of the select domain page
     */
    public DisplaySurveyWidget(WidgetInstance instance) {
        super(SurveyWidgetProperties.getSurvey(instance.getWidgetProperties()), false, TutorUserWebInterface.isDebugMode());

        webpageId = instance.getWidgetId();

        this.addCloseHandler(new CloseHandler<SurveyResponse>() {
            @Override
            public void onClose(CloseEvent<SurveyResponse> result) {

                WidgetProperties updatedProperties = new WidgetProperties();
                SurveyWidgetProperties.setAnswers(updatedProperties, result.getTarget());

                BrowserSession.getInstance().sendActionToServer(new SubmitAction(webpageId, updatedProperties), new AsyncCallback<RpcResponse>() {
                    @Override
                    public void onFailure(Throwable caught) {

                        Document.getInstance().displayRPCError("Submitting survey results", caught);
                    }

                    @Override
                    public void onSuccess(RpcResponse result) {

                        
                        if (result == null) {
                            Document.getInstance().displayError("Closing 'Submitting survey results' page.", "The server returned a null result.");   
                            return;
                        }
                        
                        if (result.isSuccess()) {

                            BrowserSession.getInstance().sendActionToServer(new CloseAction(webpageId), new AsyncCallback<RpcResponse>() {
                                
                                @Override
                                public void onSuccess(RpcResponse result) {
                                    if (result == null || !result.isSuccess()) {
                                        Document.getInstance().displayError("Closing 'Submitting survey results' page", "Action failed on the server");
                                    }                                            
                                }
                                
                                @Override
                                public void onFailure(Throwable caught) {
                                    Document.getInstance().displayRPCError("Closing 'Submitting survey results' page", caught);                                    
                                }
                            });
                            
                            hasClosed = true;

                        } else {

                            Document.getInstance().displayError("Submitting survey results", "The action failed on the server.", result.getResponse());
                        }
                    }
                });
            }
        });
        
        // setup a callback used for notification that the learner answered a question through a widget
        this.setCurrentQuestionAnsweredCallback(new CurrentQuestionAnsweredCallback() {
            
            @Override
            public void questionAnswered(AbstractQuestionResponse response) {
                
                WidgetProperties updatedProperties = new WidgetProperties();
                SurveyWidgetProperties.setCurrentQuestionAnswers(updatedProperties, response);

                BrowserSession.getInstance().sendActionToServer(new SubmitAction(webpageId, updatedProperties), null);                
            }
        });
    }

    @Override
    public void onRemoval(boolean isGiftInvoked) {
        
        if(!hasClosed){
            
            if(isGiftInvoked) {
                
                /*
                 * If this widget is being explicitly removed by GIFT (i.e. not unloaded via refresh or detached 
                 * by clicking the browser's back button) and has not yet been closed, then close it to clean up 
                 * the survey's state on the survey in case another survey needs to be displayed afterward.
                 * 
                 * We don't want to close this widget if the user reloads the page or clicks their browser's back 
                 * button since the user may attempt to resume the course from this survey, which will fail 
                 * if the survey is closed
                 */
                BrowserSession.getInstance().sendActionToServer(new CloseAction(webpageId), new AsyncCallback<RpcResponse>() {
                    
                    @Override
                    public void onSuccess(RpcResponse result) {
                        if (result == null || !result.isSuccess()) {
                            Document.getInstance().displayError("Closing 'Submitting survey results' page", "Action failed on the server");
                        }                                            
                    }
                    
                    @Override
                    public void onFailure(Throwable caught) {
                        Document.getInstance().displayRPCError("Closing 'Submitting survey results' page", caught);                                    
                    }
                });
            }
            
            hasClosed = true;
        }
    }

}
