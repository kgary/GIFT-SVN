/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

/**
 * An abstract class for all events that are displayed in the AAR
 *
 * @author jleonard
 */
public abstract class AbstractAfterActionReviewEvent {
    
	private boolean showInAAR = true;
	
	/** flag used to indicate whether this review event was generated during an adaptive courseflow course object */
	private boolean adaptiveCourseflowEvent = false;
	
	private String courseObjectName;
	
	/**
	 * Constructor for creating an event that could be shown in a structure review course object.
     * 
     * @param courseObjectName the name of the course object where this event occurred.  Can't be null or empty.
	 */
	public AbstractAfterActionReviewEvent(String courseObjectName) {		
	       
        if(courseObjectName == null || courseObjectName.isEmpty()){
            throw new IllegalArgumentException("The course object name can't be null or empty.");
        }
        
        this.courseObjectName = courseObjectName;
	}
	
	/** 
	 * Constructor for creating an event that could be shown in a structure review course object.
	 * 
	 * @param courseObjectName the name of the course object where this event occurred.  Can't be null or empty.
	 * @param showInAAR - whether or not to show this event in the After Action Review 
	 */
	public AbstractAfterActionReviewEvent(String courseObjectName, boolean showInAAR) {
	    this(courseObjectName);

	    
	    this.showInAAR = showInAAR;
	}
	
	/**
	 * Returns the name of the course object where this event occurred.
	 * 
	 * @return the unique name of a course object.  Won't be null or empty.
	 */
	public String getCourseObjectName(){
	    return courseObjectName;
	}
	
	/** 
	 * Returns the showInAAR value of an AAR event
	 * @return boolean - true if the event should be shown in the AAR, false otherwise
	 */
	public boolean getShowInAAR() {
		return showInAAR;
	}
	
	/**
	 * Return  whether this review event was generated during an adaptive courseflow course object
	 * 
	 * @return default is false
	 */
	public boolean isAdaptiveCourseflowEvent() {
        return adaptiveCourseflowEvent;
    }

	/**
	 * Set  whether this review event was generated during an adaptive courseflow course object
	 * 
	 * @param adaptiveCourseflowEvent the value to use
	 */
    public void setAdaptiveCourseflowEvent(boolean adaptiveCourseflowEvent) {
        this.adaptiveCourseflowEvent = adaptiveCourseflowEvent;
    }

    @Override
	public String toString() {
		
		StringBuffer sb = new StringBuffer();
        sb.append("showInAAR = ").append(getShowInAAR());
        sb.append(", courseObjectName = ").append(getCourseObjectName());
        sb.append(", adaptiveCourseflowEvent = ").append(isAdaptiveCourseflowEvent());
        sb.append("]");

        return sb.toString();
	}
}
