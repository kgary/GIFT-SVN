/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.client.coursewidgets;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.gwtbootstrap3.client.ui.AnchorListItem;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.Widget;

import generated.course.LessonMaterialList.Assessment;
import generated.course.OverDwell.Duration.DurationPercent;
import generated.course.YoutubeVideoProperties;
import mil.arl.gift.common.gwt.client.RpcResponse;
import mil.arl.gift.common.gwt.shared.MediaHtml;
import mil.arl.gift.tutor.client.coursewidgets.CourseHeaderWidget.ContinueHandler;

/**
 * Widget that displays some media
 *
 * @author jleonard
 */
public class CollectionWidget extends Composite {
    
    private static CollectionWidgetUiBinder uiBinder = GWT.create(CollectionWidgetUiBinder.class);
    
    interface CollectionWidgetUiBinder extends UiBinder<Widget, CollectionWidget> {
    }

    @UiField
    protected DeckPanel deckPanel;
    
    @UiField(provided = true)
    protected ContentWidget contentWidget = new ContentWidget() {
        @Override
        public void buildOAuthUrl(String rawUrl, Serializable mediaTypeProperties, AsyncCallback<RpcResponse> callback) {
            buildLtiOAuthUrl(rawUrl, mediaTypeProperties, callback);
        }
    };
    
    @UiField
    protected SlideShowWidget slideShowWidget;
    
    private ArrayList<MediaAnchor> anchors = new ArrayList<MediaAnchor>();
    
    private int index = 0;
            
    private CourseHeaderWidget headerWidget;
    
    /** The time (in milliseconds) at which this widget started being displayed */
    private long startTime;
    
    /**
     * Creates a new widget displaying a collection of media items
     * 
     * @param mediaHtmlList the list of media items to display
     */
    public CollectionWidget(final List<MediaHtml> mediaHtmlList) {
        this(mediaHtmlList, null, null);
    }
    

    /**
     * Creates a new widget displaying a collection of media items and performs any necessary assessment logic based on
     * how long the learner spends viewing the displayed media
     * 
     * @param mediaHtmlList the list of media items to display
     * @param assessment the assessment logic to perform
     * @param dwellCallback a callback to be invoked if the learner spends too much time (overdwell) or too little time (underdwell)
     * viewing the displayed media
     */
    public CollectionWidget(final List<MediaHtml> mediaHtmlList, final Assessment assessment, final MediaDwellCallback dwellCallback) {
        
    	initWidget(uiBinder.createAndBindUi(this));
    	headerWidget = CourseHeaderWidget.getInstance();
    	
    	if (mediaHtmlList != null && !mediaHtmlList.isEmpty()) {
    			
            boolean first = true;
            for (final MediaHtml mediaHtml : mediaHtmlList) {

                final MediaAnchor mediaLink = new MediaAnchor(mediaHtml);
                anchors.add(mediaLink);
                mediaLink.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        anchors.get(index).setActive(false);
                        
                        index = anchors.indexOf(mediaLink);
                        showMedia(mediaHtml, mediaHtmlList);
                    }
                });
                
                if(first) {
                    mediaLink.setActive(true);
                    headerWidget.setHeaderTitle(mediaHtml.getName());
                    if(mediaHtml.isSlideShow()) {
                        slideShowWidget.setProperties(mediaHtml.getProperties());
                        deckPanel.showWidget(deckPanel.getWidgetIndex(slideShowWidget));
                        
                    } else {
                        if(mediaHtml.isSameOriginPolicyViolator()) {
                            contentWidget.setLinkUrl(mediaHtml.getUri(), mediaHtml.getName());
                        } else {
                            contentWidget.setContentUrl(mediaHtml.getUri(), mediaHtml.getProperties());
                            
                            if(mediaHtml.getProperties() != null 
                                    && mediaHtml.getProperties() instanceof YoutubeVideoProperties
                                    && mediaHtmlList.size() == 1
                                    && assessment != null){
                                
                                // if a single YouTube video with an assessment is being displayed, we need
                                // to track how long the learner spends on this page so we can detect
                                // their underdwell/overdwell status
                                
                                if(assessment.getOverDwell() != null
                                        && assessment.getOverDwell().getDuration() != null
                                        && assessment.getOverDwell().getDuration().getType() != null){
                                    
                                    Integer overDwellSeconds = null;
                                    
                                    if(assessment.getOverDwell().getDuration().getType() instanceof BigInteger){
                                        
                                        //use the provided duration
                                        overDwellSeconds = ((BigInteger) assessment.getOverDwell().getDuration().getType()).intValue();
                                    
                                    } if(assessment.getOverDwell().getDuration().getType() instanceof DurationPercent){
                                        
                                        DurationPercent durationPercent = (DurationPercent) assessment.getOverDwell().getDuration().getType();
                                        
                                        if(durationPercent.getTime() != null){
                                            
                                            if(durationPercent.getPercent() != null){
                                                
                                                //multiply the duration with the percentage
                                                overDwellSeconds = (int) (durationPercent.getTime().longValue() * (durationPercent.getPercent().doubleValue()/100));
                                                
                                            } else {
                                                
                                                //assume 100% duration
                                                overDwellSeconds = durationPercent.getTime().intValue();
                                            }
                                        }
                                    }
                                    
                                    if(overDwellSeconds != null){
                                        
                                        // schedule a timer to notify the Domain when the learner overdwells
                                        Timer overDwellTimer = new Timer() {
                                            
                                            @Override
                                            public void run() {
                                                
                                                if(dwellCallback != null){
                                                    dwellCallback.onOverDwell();
                                                }
                                            }
                                        };
                                        
                                        overDwellTimer.schedule(overDwellSeconds * 1000);
                                    }
                                }
                            }
                        
                        }
                        
                        deckPanel.showWidget(deckPanel.getWidgetIndex(contentWidget));
                    }
                    first = false;
                }
                
                headerWidget.addDropDownLink(mediaLink);
            }
            
            if(mediaHtmlList.size() > 1) {
                headerWidget.showPreviousButton(new ClickHandler() {

                    @Override
                    public void onClick(ClickEvent arg0) {
                        anchors.get(index).setActive(false);
                        
                        index -= 1;
                        MediaHtml media = mediaHtmlList.get(index);
                        showMedia(media, mediaHtmlList);
                    }
                    
                }, "previous media content");
                headerWidget.showNextButton(new ClickHandler() {

                    @Override
                    public void onClick(ClickEvent arg0) {
                        anchors.get(index).setActive(false);
                        
                        index += 1;
                        MediaHtml media = mediaHtmlList.get(index);
                        showMedia(media, mediaHtmlList);
                    }
                    
                }, "next media content");
                
            }
            
        } else {
            contentWidget.setMessageHTML("There is no lesson material to display");
        }
    	
        //start tracking the time the learner spends on this widget
        startTime = System.currentTimeMillis();

    		headerWidget.setContinueButtonCallback(new ContinueHandler() {
    			@Override
    			public void onClick() {
    				// Clear the iframe to prevent PDFs from causing lag in the browser when the continue button is clicked
    				if(deckPanel.getVisibleWidget() == deckPanel.getWidgetIndex(contentWidget)) {
    					contentWidget.clearContent();
    				}
    				
    				if(assessment != null 
                            && assessment.getUnderDwell() != null 
                            && assessment.getUnderDwell().getDuration() != null){   
                    
                        // check to see if the learner underdwelled on this widget
                        long underDwellMillis = assessment.getUnderDwell().getDuration().longValue() * 1000;
                        
                        if(System.currentTimeMillis() - startTime < underDwellMillis){
                            
                            if(dwellCallback != null){
                                
                                dwellCallback.onUnderDwell(this);
                            }
                        }
                    }   
                    
                    Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                        
                        @Override
                        public void execute() {
                            
                            //remove this callback so it isn't called again accidentally
                            headerWidget.setContinueButtonCallback(null);
                        }
                    });
    			}
    			
    			@Override
    			public void onFailure() {
    				showMedia(anchors.get(index).getMedia(), mediaHtmlList);
    			}
    		});
    }
    
    private void showMedia(MediaHtml mediaHtml, List<MediaHtml> mediaHtmlList) {
    	
    	contentWidget.clearContent();
    	
    	if(index + 1 < mediaHtmlList.size()){
    		headerWidget.setNextButtonEnabled(true);
		} else {
			headerWidget.setNextButtonEnabled(false);				
		}
    	
    	if(index > 0 && index < mediaHtmlList.size()) {
    		headerWidget.setPreviousButtonEnabled(true);
		} else {
			headerWidget.setPreviousButtonEnabled(false);
		}
    	
    	anchors.get(index).setActive(true);
		
    	headerWidget.setHeaderTitle(mediaHtml.getName());
		
		if(mediaHtml.isSlideShow()) {
			slideShowWidget.setProperties(mediaHtml.getProperties());
			deckPanel.showWidget(deckPanel.getWidgetIndex(slideShowWidget));
			
		} else {
			
			if (mediaHtml.isSameOriginPolicyViolator()) {
				/*
				 * Open the URL in a separate browser.
				 */
				String options = "menubar=no,location=no,resizable=yes,scrollbars=yes,status=no";
				Window.open(mediaHtml.getUri(), "_blank", options);
				contentWidget.setLinkUrl(mediaHtml.getUri(), mediaHtml.getName());

			} else {
				contentWidget.setContentUrl(mediaHtml.getUri(), mediaHtml.getProperties());
			}
			
			deckPanel.showWidget(deckPanel.getWidgetIndex(contentWidget));
		}
    }

    private class MediaAnchor extends AnchorListItem {
    	private boolean showInfo = false;
    	private MediaHtml media;
    	
    	public MediaAnchor(MediaHtml media) {
    		super(media.getName());
    		this.media = media;
    		if(media.getMessage() != null && !media.getMessage().isEmpty()) {
    			showInfo = true;
    		}
    	}
    	
    	public MediaHtml getMedia() {
    		return media;
    	}
    	
    	@Override
    	public void setActive(boolean active) {
    		super.setActive(active);
    		if(active) {
    			if(showInfo) {
    				
    				headerWidget.setInfoMessage(media.getMessage(), true);
    				showInfo = false;
    				
    			} else {
    				
    				headerWidget.setInfoMessage(media.getMessage(), false);
    			}
    		}
    	}
    }
    
    /**
     * A callback that executes logic whenever the learner spends too much time (overdwell) or too little time (underdwell)
     * viewing displayed media items
     * 
     * @author nroberts
     */
    public static interface MediaDwellCallback{
        
        /**
         * Handles when the learner spends too much time viewing media items (overdwell)
         */
        public void onOverDwell();
        
        /**
         * Handles when the learner spends too little time viewing media items (underdwell)
         * 
         * @param handler the handler for the continue button used to proceed with the course. This is used to prevent
         * the course from proceeding normally if an underdwell occurrs, allowing the Domain to redirect the course elsewhere
         */
        public void onUnderDwell(ContinueHandler handler);
    }
    
    /**
     * Builds the encrypted OAuth URL that will be used to send the request to the LTI provider.
     * 
     * @param rawUrl the raw media url before it has been protected by OAuth.
     * @param mediaTypeProperties The MediaTypeProperties associated with the content.
     * @param callback the callback used to handle the response or catch any failures.
     */
    public void buildLtiOAuthUrl(String rawUrl, Serializable mediaTypeProperties, AsyncCallback<RpcResponse> callback) {
        // This is handled by an overwritten method when the constructor is initialized.
        // The reason for this is we need to get to a spot where we can access BrowserSession.getInstance()
    }
}
