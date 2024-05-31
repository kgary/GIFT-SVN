/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

import generated.course.LessonMaterial;
import generated.course.Media;
import generated.course.YoutubeVideoProperties;
import mil.arl.gift.common.util.StringUtils;

import java.util.List;

/**
 * This class contains information about media that needs to be displayed by the Tutor
 *
 * @author jleonard
 */
public class DisplayMediaCollectionRequest {

    /**  The list of media to display */
    private LessonMaterial lessonMaterial;
    
    /**  a reference to the content to be displayed that is unique to this course.*/
    private String contentReference;
    
    /**
     * Constructor
     *
     * @param lessonMaterialTransition The list of media to display. Can be null.
     * @param contentReference a reference to the content to be displayed that is unique to this course.  Can't be null or empty.
     */
    public DisplayMediaCollectionRequest(LessonMaterial lessonMaterialTransition, String contentReference) {
        
        if(StringUtils.isBlank(contentReference)){
            throw new IllegalArgumentException("The content reference can't be null or empty.");
        }
        
        this.contentReference = contentReference;
    	lessonMaterial = lessonMaterialTransition;
    }
    
    /**
     * Return the lesson material to display.
     * 
     * @return can be null.
     */
    public LessonMaterial getLessonMaterial() {
    	return lessonMaterial;
    }

    /**
     * Gets the list of media to be displayed
     *
     * @return List<Media> The list of media to be displayed.  Can be null.
     */
    public List<Media> getMediaList() {
    	if(lessonMaterial != null && lessonMaterial.getLessonMaterialList() != null) {
    		return lessonMaterial.getLessonMaterialList().getMedia();
    	}
    	return null;
    }
    
    /**
     * Return a reference to the content to be displayed that is unique to this course.
     * @return won't be null
     */
    public String getContentReference(){
        return contentReference;
    }
    
    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[DisplayMediaCollectionRequest: ");
        sb.append("contentRef = ").append(getContentReference());
        sb.append(", Media List = {");

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
	            
	            sb.append("}, ");
	        }
        }
        sb.append("}");
        sb.append("]");

        return sb.toString();
    }
}
