/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

import generated.dkf.LessonMaterialList;
import generated.dkf.Media;
import generated.dkf.YoutubeVideoProperties;

import java.util.List;

/**
 * This class contains information about media that needs to be displayed by the Tutor during a training application scenario
 *
 * @author nroberts
 */
public class DisplayMidLessonMediaRequest {

    /** The list of media to display */
    private LessonMaterialList mediaList;
    
    /**
     * Constructor
     *
     * @param mediaList The list of media to display
     */
    public DisplayMidLessonMediaRequest(LessonMaterialList mediaList) {
    	this.mediaList = mediaList;
    }

    /**
     * Gets the list of media to be displayed
     *
     * @return List<Media> The list of media to be displayed.  Can be null.
     */
    public List<Media> getMediaList() {
    	return mediaList.getMedia();
    }
    
    /**
     * Return the lesson material to display.
     * 
     * @return can be null.
     */
    public LessonMaterialList getLessonMaterial() {
        return mediaList;
    }
    
    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[DisplayMidLessonMediaRequest: ");
        sb.append("Media List = {");

        if(getMediaList() != null) {
	        for (Media media : getMediaList()) {
	
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
	
	            } else {
	
	                sb.append("null");
	            }
	            
	            sb.append("},\n ");
	        }
        }
        sb.append("}");
        sb.append("]");

        return sb.toString();
    }
}
