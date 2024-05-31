/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

import generated.course.BooleanEnum;
import generated.course.Guidance;
import generated.course.Media;
import generated.course.YoutubeVideoProperties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This class contains information about text that needs to be displayed by the Tutor
 *
 * @author jleonard
 */
public class DisplayMediaTutorRequest extends AbstractDisplayContentTutorRequest implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Guidance guidance = null;
    
    private Media media = null;
    
    private List<Media> mediaList = null;
    
    /** Whether or not the url violates same origin policy or contains blocked content. */
    private boolean openInNewWindow;
    
    /**
     * Default constructor
     */
    public DisplayMediaTutorRequest() {
    	super();
    }
    
    /**
     * For legacy messages only.
     *
     * @param mediaList The list of media to display
     */
    public DisplayMediaTutorRequest(List<Media> mediaList) {
        this.mediaList = new ArrayList<Media>();
        mediaList.addAll(mediaList);
    }

    /**
     * Constructor
     * 
     * @param guidance The serializable guidance course object
     * @param displayDuration The display duration
     * @param whileTrainingAppLoads whether this course object should be presented while a training app loads
     */
    public DisplayMediaTutorRequest(Guidance guidance, int displayDuration, boolean whileTrainingAppLoads) {
    	super(displayDuration, whileTrainingAppLoads);
        this.guidance = guidance;
    }

    /**
     * Constructor 
     * 
     * @param transition A serialized Guidance or Media course object
     */
    public DisplayMediaTutorRequest(Serializable transition) {
    	super();
    	if(transition instanceof Guidance) {
    		this.guidance = (Guidance) transition;
    	} else {
    		this.media = (Media) transition;
    	}
    }
        
    /**
     * Gets the serialized Guidance course object
     * 
     * @return The guidance course object
     */
    public Guidance getGuidance() {
    	return guidance;
    }
    
    /**
     * Sets the serialized Guidance course object
     * 
     * @param guidance The Guidance course object
     */
    public void setGuidance(Guidance guidance) {
    	this.guidance = guidance;
    }
    
    /**
     * Gets the serialized Media course object
     * 
     * @return The Media course object
     */
    public Media getMedia() {
    	return media;
    }
    
    /**
     * Sets the serialized Media course object
     * 
     * @param guidance The Media course object
     */
    public void setMedia(Media media) {
    	this.media = media;
    }
    
    /**
     * Gets the available media type properties
     * 
     * @return The serialized media type properties. Can be null.
     */
    public Serializable getMediaTypeProperties() {
    	if(media != null) {
    		return media.getMediaTypeProperties();
    	}
    	return null;
    }
    
    /**
     * For legacy messages only.
     * 
     * @return The list of media
     */
    public List<Media> getMediaList() {
    	return mediaList;
    }
    
    /**
     * Gets whether or not the url should open in a new window. This is true if
     * the url violates the same origin policy or if it contains blocked content.
     * Content is blocked if the page is served over HTTPS but requests an HTTP
     * resource.
     * 
     * @return true if the content should open in a new window, false otherwise.
     */
    public boolean shouldOpenInNewWindow() {
        return openInNewWindow;
    }
    
    /**
     * Sets whether or not the url should open in a new window. This is true if
     * the url violates the same origin policy or if it contains blocked content.
     * Content is blocked if the page is served over HTTPS but requests an HTTP
     * resource.
     * 
     * @param openInNewWindow whether or not the content should open in a new window.
     */
    public void setShouldOpenInNewWindow(boolean openInNewWindow) {
        this.openInNewWindow = openInNewWindow;
    }
    
    @Override
	public boolean isFullscreen() {
    	if(guidance == null || guidance.getFullScreen() == null) {
    		return true;
    	}
    	
    	return guidance.getFullScreen() == BooleanEnum.TRUE;    	
    }
    
    @Override
	public String getMessage() {
    	if(guidance != null) {
	    	if(guidance.getGuidanceChoice() instanceof Guidance.URL) {
	    		 return ((Guidance.URL) guidance.getGuidanceChoice()).getMessage();
	    		 
	    	} else if(guidance.getGuidanceChoice() instanceof Guidance.File) {
	    		return ((Guidance.File) guidance.getGuidanceChoice()).getMessage();
	    	}
	    	
    	} else if(media != null) {
    		return media.getMessage();
    	}
    	
    	return null;
    }
    
    @Override
	public String getTitle() {
    	if(guidance != null) {
	    	return guidance.getTransitionName();
    	} else if(media != null) {
    		return media.getName();
    	}
    	return null;
    }
    
    public String getUrl() {
    	if(guidance != null) {
	    	if(guidance.getGuidanceChoice() instanceof Guidance.URL) {
	    		 return ((Guidance.URL) guidance.getGuidanceChoice()).getAddress();
	    		 
	    	} else if(guidance.getGuidanceChoice() instanceof Guidance.File) {
	    		return ((Guidance.File) guidance.getGuidanceChoice()).getHTML();
	    	}
	    	
    	} else if(media != null) {
    		return media.getUri();
    	}
    	
    	return null;
    }
        
    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[DisplayMediaTutorRequest: ");
        sb.append("display duration = ").append(getDisplayDuration()).append(" ms");
        sb.append(", whileTrainingAppLoads = ").append(isWhileTrainingAppLoads());
        sb.append(", title = ").append(getTitle());
        sb.append(", message = ").append(getMessage());
        sb.append(", url = ").append(getUrl());
        if(mediaList != null) {
	        for (Media media : mediaList) {
	
	            sb.append("{");
	
	            if (media != null) {
	
	                sb.append("Name = ").append(media.getName());
	                sb.append(", URI = ").append(media.getUri());
	                sb.append(", Properties Class = {");
	
	                if (media.getMediaTypeProperties() != null) {
	
	                    sb.append("type = ").append(media.getMediaTypeProperties().getClass().getCanonicalName());
	                    
	                    //
	                    // Gather media type class attributes
	                    //
	                    if(media.getMediaTypeProperties() instanceof YoutubeVideoProperties){
	                        YoutubeVideoProperties uTubeProps = (YoutubeVideoProperties)media.getMediaTypeProperties();
	                        
	                        //optional
	                        if(uTubeProps.getAllowFullScreen() != null){
	                            sb.append(", allowFullScreen = ").append(uTubeProps.getAllowFullScreen().value());
	                        }
	                        
	                        //optional
	                        if(uTubeProps.getSize() != null){
	                            sb.append(", size = { width = ").append(uTubeProps.getSize().getWidth());
	                            sb.append(", height = ").append(uTubeProps.getSize().getHeight()).append("}");
	                        }
	                    }
	
	                } else {	
	                	
	                    sb.append("null");
	                }
	                
	                sb.append("}");	
	            }	        
	            
	            sb.append("}, ");
	            
	        }
	        
	        sb.append("}");
        }
        sb.append("]");

        return sb.toString();
    }
}
