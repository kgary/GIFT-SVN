/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor;

import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;

/**
 * Singleton class that provides access to shared resources.
 *
 * @author nroberts
 */
public class SurveyEditorResources {
	
	/** The Constant INSTANCE. */
	private static final SurveyEditorResources INSTANCE =  new SurveyEditorResources();

	/** The event bus. */
	private EventBus eventBus = new SimpleEventBus();

	/** 
	 * The URL where hosted media files in a course folder can be retrieved from. 
	 * (e.g. http://localhost:8885/workspace/username/My Course)
	 */
    private String hostFolderUrl;

	/**
	 * Instantiates a new shared resources.
	 */
	private SurveyEditorResources() {
	}
	
	/**
	 * Gets the single instance of SharedResources.
	 *
	 * @return single instance of SharedResources
	 */
	public static SurveyEditorResources getInstance() {		
		return INSTANCE;
	}
	
	/**
	 * Retrieve the EventBus.
	 *
	 * @return the EventBus
	 */
	public EventBus getEventBus() {
		return  eventBus;
	}
	
	/**
	 * Sets the event bus. This can be used to override the default event bus with another one.
	 *
	 * @param eventBus the EventBus
	 */
	public void setEventBus(EventBus eventBus) {		
		this.eventBus = eventBus;
	}
	
	/**
	 * Sets the URL where hosted media files in a course folder can be retrieved from.
	 * (e.g. http://localhost:8885/workspace/username/My Course)
	 * 
	 * @param hostFolderUrl the URL of hosted media files. Cannot be null.
	 */
	public void setHostFolderUrl(String hostFolderUrl) {
	    
	    if(hostFolderUrl == null) {
	        throw new IllegalArgumentException("The host folder URL cannot be null.");
	    }
	    
	    this.hostFolderUrl = hostFolderUrl;
	}

	/**
	 * Gets the URL where hosted media files in a course folder can be retrieved from.
	 * (e.g. http://localhost:8885/workspace/username/My Course)
	 * 
	 * @return the URL of hosted media files. Will only be null if not yet assigned.
	 */
    public String getHostFolderUrl() {
        return hostFolderUrl;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[SurveyEditorResources: eventBus = ");
        sb.append(getEventBus());
        sb.append(", hostFolderUrl = ");
        sb.append(getHostFolderUrl());
        sb.append("]");
        return sb.toString();
    }
}
