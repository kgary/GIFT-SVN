/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.client.widgets;

import java.io.Serializable;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;

import generated.course.LessonMaterialList.Assessment;
import generated.course.LtiProperties;
import mil.arl.gift.common.gwt.client.RpcResponse;
import mil.arl.gift.common.gwt.shared.MediaHtml;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.tutor.client.BrowserSession;
import mil.arl.gift.tutor.client.Document;
import mil.arl.gift.tutor.client.coursewidgets.CollectionWidget;
import mil.arl.gift.tutor.client.coursewidgets.CollectionWidget.MediaDwellCallback;
import mil.arl.gift.tutor.client.coursewidgets.CourseHeaderWidget;
import mil.arl.gift.tutor.client.coursewidgets.CourseHeaderWidget.ContinueHandler;
import mil.arl.gift.tutor.shared.CloseAction;
import mil.arl.gift.tutor.shared.SubmitAction;
import mil.arl.gift.tutor.shared.WidgetInstance;
import mil.arl.gift.tutor.shared.properties.DisplayMediaCollectionWidgetProperties;
import mil.arl.gift.tutor.shared.properties.WidgetProperties;

/**
 * Widget that displays some media
 *
 * @author jleonard
 */
public class DisplayMediaCollectionWidget extends Composite {
    
    /**
     * Constructor
     *
     * @param instance The instance of the after action review widget
     */
    public DisplayMediaCollectionWidget(final WidgetInstance instance) {

    	List<MediaHtml> mediaHtmlList = null;
    	Assessment assessment = null;
    	WidgetProperties properties = instance.getWidgetProperties();
    	
    	if (properties != null) {
    		mediaHtmlList = DisplayMediaCollectionWidgetProperties.getMediaHtmlList(properties);
    		assessment = DisplayMediaCollectionWidgetProperties.getAssessment(properties);
    	}

    	CourseHeaderWidget.getInstance().setContinuePageId(instance.getWidgetId());
    	MediaDwellCallback mediaDwellCallback = new MediaDwellCallback() {
			
			@Override
			public void onUnderDwell(final ContinueHandler continueHandler) {
				
				if(instance != null){
				
					// prevent the usual Continue button logic, since we need to circumvent it
					continueHandler.setShouldStop(true);
					
					WidgetProperties updatedProperties = new WidgetProperties();
					DisplayMediaCollectionWidgetProperties.setUnderDwelled(updatedProperties, true);
					
					// notify the Domain that the learner has underdwelled
					BrowserSession.getInstance().sendActionToServer(new SubmitAction(instance.getWidgetId(), updatedProperties), new AsyncCallback<RpcResponse>() {
	                    @Override
	                    public void onFailure(Throwable caught) {
	                    	
	                    	continueHandler.setShouldStop(false);
	
	                        Document.getInstance().displayRPCError("Submitting underdwell results", caught);
	                    }
	
	                    @Override
	                    public void onSuccess(RpcResponse result) {
	                    	
	                    	continueHandler.setShouldStop(false);
	
	                        if (result.isSuccess()) {
	
	                            BrowserSession.getInstance().sendActionToServer(new CloseAction(instance.getWidgetId()), new AsyncCallback<RpcResponse>() {
	                                
	                                @Override
	                                public void onSuccess(RpcResponse result) {
	                                    if (result == null || !result.isSuccess()) {
	                                        Document.getInstance().displayError("Closing 'Submitting underdwell results' page", "Action failed on the server");
	                                    }                                            
	                                }
	                                
	                                @Override
	                                public void onFailure(Throwable caught) {
	                                    Document.getInstance().displayRPCError("Closing 'Submitting underdwell results' page", caught);                                    
	                                }
	                            });
	
	                        } else {
	
	                            Document.getInstance().displayError("Submitting underdwell results", "The action failed on the server.", result.getResponse());
	                        }
	                    }
	                });
				}
			}
			
			@Override
			public void onOverDwell() {
				
				if(instance != null){
				
					WidgetProperties updatedProperties = new WidgetProperties();
					DisplayMediaCollectionWidgetProperties.setOverDwelled(updatedProperties, true);
					
					BrowserSession.getInstance().sendActionToServer(new SubmitAction(instance.getWidgetId(), updatedProperties), new AsyncCallback<RpcResponse>() {
	                    @Override
	                    public void onFailure(Throwable caught) {
	
	                        Document.getInstance().displayRPCError("Submitting overdwell results", caught);
	                    }
	
	                    @Override
	                    public void onSuccess(RpcResponse result) {
	
	                        if (result.isSuccess()) {
	
	                            BrowserSession.getInstance().sendActionToServer(new CloseAction(instance.getWidgetId()), new AsyncCallback<RpcResponse>() {
	                                
	                                @Override
	                                public void onSuccess(RpcResponse result) {
	                                    if (result == null || !result.isSuccess()) {
	                                        Document.getInstance().displayError("Closing 'Submitting overdwell results' page", "Action failed on the server");
	                                    }                                            
	                                }
	                                
	                                @Override
	                                public void onFailure(Throwable caught) {
	                                    Document.getInstance().displayRPCError("Closing 'Submitting overdwell results' page", caught);                                    
	                                }
	                            });
	
	                        } else {
	
	                            Document.getInstance().displayError("Submitting overdwell results", "The action failed on the server.", result.getResponse());
	                        }
	                    }
	                });
				}
			}
		};
		
		CollectionWidget widget = new CollectionWidget(mediaHtmlList, assessment, mediaDwellCallback) {
		    @Override
		    public void buildLtiOAuthUrl(String rawUrl, Serializable mediaTypeProperties, AsyncCallback<RpcResponse> callback) {
		        buildOAuthUrl(rawUrl, mediaTypeProperties, callback);
		    }
		};
    	
    	initWidget(widget);
    }
    
    /**
     * Builds the encrypted OAuth URL that will be used to send the request to the LTI provider.
     * 
     * @param rawUrl the raw media url before it has been protected by OAuth.
     * @param mediaTypeProperties The MediaTypeProperties associated with the content.
     * @param callback the callback used to handle the response or catch any failures.
     */
    public void buildOAuthUrl(String rawUrl, Serializable mediaTypeProperties, AsyncCallback<RpcResponse> callback) {
        if (mediaTypeProperties != null && mediaTypeProperties instanceof LtiProperties) {
            LtiProperties ltiProperties = (LtiProperties) mediaTypeProperties;
            BrowserSession.getInstance().buildOAuthLtiUrl(rawUrl, ltiProperties, callback);
        } else {
            callback.onFailure(new DetailedException("Building LTI encoded url failed",
                    "Exception occurred while trying to build the encoded LTI provider url", null));
        }
    }
}