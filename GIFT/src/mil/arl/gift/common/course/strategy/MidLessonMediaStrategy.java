/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.course.strategy;

import generated.dkf.LessonMaterialList;
import generated.dkf.Media;
import generated.dkf.MidLessonMedia;
import generated.dkf.YoutubeVideoProperties;
import mil.arl.gift.common.course.dkf.strategy.AbstractDKFStrategy;

/**
 * This class contains information on a mid-lesson media strategy.
 * 
 * @author nroberts
 *
 */
public class MidLessonMediaStrategy extends AbstractDKFStrategy {
    
    /** the list of media items this strategy should present */
    private LessonMaterialList media;
    
    /**
     * Class constructor - set attributes
     * 
     * @param name unique name of a strategy
     * @param midLessonMedia - dkf.xsd generated class instance. Cannot be null or empty.
     */
    public MidLessonMediaStrategy(String name, MidLessonMedia midLessonMedia){
        super(name, midLessonMedia.getStrategyHandler());
        
        if(midLessonMedia == null 
                || midLessonMedia.getLessonMaterialList() == null 
                || midLessonMedia.getLessonMaterialList().getMedia().isEmpty()) {
            
            throw new IllegalArgumentException("The list of media to present cannot be null or empty.");
        }
        
        this.media = midLessonMedia.getLessonMaterialList();
        
        if(midLessonMedia.getDelayAfterStrategy() != null && midLessonMedia.getDelayAfterStrategy().getDuration() != null){
            this.setDelayAfterStrategy(midLessonMedia.getDelayAfterStrategy().getDuration().floatValue());
        }
    }
    
    /**
     * Return the list of media items this strategy should present. The returned list cannot be null or empty.
     * 
     * @return the list of media items to present
     */
    public LessonMaterialList getMediaList(){
        return media;
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[MidLessonMediaStrategy: ");
        sb.append(super.toString());
        
        sb.append(", media = {");

        if(getMediaList() != null && this.media.getMedia() != null) {
            for (Media media : this.media.getMedia()) {
    
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
