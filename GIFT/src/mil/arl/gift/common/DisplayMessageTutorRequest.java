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

import java.io.Serializable;

/**
 * This class contains information about text that needs to be displayed by the Tutor
 *
 * @author jleonard
 */
public class DisplayMessageTutorRequest extends AbstractDisplayContentTutorRequest implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** This request is used by the tutor module to clear the current transition. 
     * Used to present the continue button in an autotutor session that occurs after a training application. */
    public static final DisplayMessageTutorRequest EMPTY_REQUEST = new DisplayMessageTutorRequest();
    
    private static final String DEFAULT_TITLE = "Information";
    
    private Guidance guidance = null;
    
    /**
     * Default constructor
     */
    public DisplayMessageTutorRequest() {
    	super();
    }
    
    public DisplayMessageTutorRequest(Guidance guidance, int displayDuration, boolean whileTrainingAppLoads) {
    	super(displayDuration, whileTrainingAppLoads);
        this.guidance = guidance;
    }
    
    public DisplayMessageTutorRequest(Guidance guidance) {
    	super();
        this.guidance = guidance;
    }
    
    public static DisplayMessageTutorRequest createTextRequest(String title, String message) {
    	 
         generated.course.Guidance guidance = new generated.course.Guidance();
         generated.course.Guidance.Message msg = new generated.course.Guidance.Message();
         if(title == null) {
        	 title = DEFAULT_TITLE;
         }
         msg.setContent(message);
         guidance.setGuidanceChoice(msg);
         guidance.setTransitionName(title);
         guidance.setFullScreen(BooleanEnum.TRUE);
         
         DisplayMessageTutorRequest request = new DisplayMessageTutorRequest();
         request.setGuidance(guidance);
         return request;
    }
    
    public Guidance getGuidance() {
    	return guidance;
    }
    
    public void setGuidance(Guidance guidance) {
    	this.guidance = guidance;
    }
    
    @Override
	public boolean isFullscreen() {
    	if(guidance == null) {
    		return true;
    	} else {
    		return guidance.getFullScreen() == null || guidance.getFullScreen() == BooleanEnum.TRUE;
    		
    	}
    	
    }
    
    @Override
	public String getMessage() {
    	if(guidance != null && guidance.getGuidanceChoice() instanceof Guidance.Message) {
    		 return ((Guidance.Message) guidance.getGuidanceChoice()).getContent();
    	}
    	
    	return null;
    }
    
    @Override
    public String getTitle() {
    	if(guidance != null) {
    		return guidance.getTransitionName();
    	} 
    	return null;
    }
    
    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[DisplayMessageTutorRequest: ");
        
        if(getTitle() != null){
            sb.append(" title = ").append(getTitle());
        }
        
        if(getMessage() != null){
            sb.append(", message = ").append(getMessage());
        }
        
        if(guidance != null){
            
            if(guidance.getGuidanceChoice() instanceof Guidance.URL){
                sb.append(", URL = ").append(((Guidance.URL)guidance.getGuidanceChoice()).getAddress());
            }else if(guidance.getGuidanceChoice() instanceof Guidance.File){
                sb.append(", File = ").append(((Guidance.File)guidance.getGuidanceChoice()).getHTML());                
            }
        }
        
        sb.append(", ").append(super.toString());
        sb.append("]");

        return sb.toString();
    }
}
