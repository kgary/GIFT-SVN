/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.client.widgets;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.DisplayMediaTutorRequest;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ProgressBarListEntry;
import mil.arl.gift.common.io.ProgressIndicator;
import mil.arl.gift.tutor.client.coursewidgets.ContentWidget;
import mil.arl.gift.tutor.client.coursewidgets.CourseHeaderWidget;
import mil.arl.gift.tutor.client.coursewidgets.SlideShowWidget;
import mil.arl.gift.tutor.client.coursewidgets.CourseHeaderWidget.ContinueHandler;
import mil.arl.gift.tutor.shared.WidgetInstance;
import mil.arl.gift.tutor.shared.WidgetTypeEnum;
import mil.arl.gift.tutor.shared.properties.DisplayMediaWidgetProperties;
import mil.arl.gift.tutor.shared.properties.WidgetProperties;

/**
 * A widget that displays media (URLs, files) to the user
 *
 * @author jleonard
 */
public class DisplayMediaWidget extends Composite implements IsUpdateableWidget {

	private static DisplayMediaWidgetUiBinder uiBinder = GWT.create(DisplayMediaWidgetUiBinder.class);
	
	private static Logger logger = Logger.getLogger(DisplayMediaWidget.class.getName());
	
	@UiField
	protected DeckPanel deckPanel;
	
    @UiField
    protected ContentWidget contentWidget;
    
    @UiField
    protected SlideShowWidget slideShowWidget;
    
    @UiField
    protected ProgressBarListEntry progressBar;
    
    /** A container element used to show/hide the progress bar as needed */
    @UiField
    protected Widget progressBarOverlay;
	
    private ProgressIndicator progressIndicator = null;
    
    interface DisplayMediaWidgetUiBinder extends UiBinder<Widget, DisplayMediaWidget> {
    }
    
    /**
     * Constructor
     *
     * @param instance The instance of the display text widget
     */
    public DisplayMediaWidget(WidgetInstance instance) {
    	initWidget(uiBinder.createAndBindUi(this));
        init(instance);
    }
    
    private void init(WidgetInstance instance) {

    	deckPanel.showWidget(deckPanel.getWidgetIndex(contentWidget));
    	WidgetProperties properties = instance.getWidgetProperties();
    	
    	if(logger.isLoggable(Level.INFO)){
    	    logger.info("Creating Display Media Widget from:\n"+properties);
    	}
    	
    	if (properties != null) {

    		final DisplayMediaTutorRequest parameters = DisplayMediaWidgetProperties.getParameters(properties);	
    		
    		if (parameters.shouldOpenInNewWindow()) {
    			/* Certain web servers that host external web pages have X-Frame-Options (XFO) of DENY or SAMEORIGIN. 
        		 * When these option are set, the browser cannot display that web page in an IFrame.  The following code
        		 * indicates that the the objects URI has violated the SOP.  When clicked the link will launch another
        		 * instance of a tab or browser to display the URL.
        		 */    			
    			contentWidget.setLinkUrl(parameters.getUrl(), parameters.getTitle());

    		} else if(DisplayMediaWidgetProperties.isSlideShowItem(properties)) {
    			// If this is a slide show, initialize and display the dedicated slide show widget
    			
    			slideShowWidget.setProperties(parameters.getMedia().getMediaTypeProperties());
    			deckPanel.showWidget(deckPanel.getWidgetIndex(slideShowWidget));
    			
    		} else {
    			// Show web pages that can be loaded in an IFrame
    			contentWidget.setContentUrl(parameters.getUrl(), parameters.getMediaTypeProperties());
    			
    			Integer progress = DisplayMediaWidgetProperties.getLoadProgress(properties);
    			if(progress != null) {
    				initProgressBar(progress);
    			}
    		}
    		
    		if(parameters.getMessage() != null && !parameters.getMessage().isEmpty()) {
    			// Display the supplementary message
    			CourseHeaderWidget.getInstance().setInfoMessage(parameters.getMessage(), true);
    		}
    		
	    	if (DisplayMediaWidgetProperties.getHasContinueButton(properties)) {
	    		// Display the header bar and the title
				CourseHeaderWidget.getInstance().setHeaderTitle(parameters.getTitle());
				CourseHeaderWidget.getInstance().setContinueButtonEnabled(slideShowWidget.getContinueButtonEnabled());
				CourseHeaderWidget.getInstance().setContinuePageId(instance.getWidgetId());
				CourseHeaderWidget.getInstance().setContinueButtonCallback(new ContinueHandler() {
	    			@Override
	    			public void onClick() {
	    				contentWidget.clearContent();
	    			}
	    			
	    			@Override
	    			public void onFailure() {
	    				// Clear the iframe to prevent PDFs from causing lag in the browser when the continue button is clicked
	    				contentWidget.setContentUrl(parameters.getUrl(), parameters.getMediaTypeProperties());
	    			}
	    		});
			}
    	}
    }

    private void initProgressBar(int progress) {
    	
    	progressIndicator = new ProgressIndicator();
    	
    	progressIndicator.setTaskDescription("Downloading content");
		progressIndicator.setPercentComplete(progress);
		progressBar.updateProgress(progressIndicator);
    }
    
	@Override
	public void update(WidgetInstance widgetInstance) {
		
		if(widgetInstance != null && widgetInstance.getWidgetProperties() != null) {
			Integer progress = DisplayMediaWidgetProperties.getLoadProgress(widgetInstance.getWidgetProperties());
			
			if(progress != null) {
				
				if(progressIndicator == null) {
					initProgressBar(progress);				
				}
				
				progressIndicator.setPercentComplete(progress);
				progressBar.updateProgress(progressIndicator);
				
				progressBarOverlay.setVisible(progress > 0);
				
				return;
			}
		}

		// create a regular guidance page if there is no load progress property
		init(widgetInstance);		
	}
	
	@Override
	public WidgetTypeEnum getWidgetType() {
		return WidgetTypeEnum.MEDIA_WIDGET;
	}
}
