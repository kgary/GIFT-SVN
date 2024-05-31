/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

import java.io.Serializable;

/**
 * Contains information about an HTML file that contains feedback to present to the learner.
 * 
 * @author mhoffman
 *
 */
public class DisplayHTMLFeedbackAction implements FeedbackAction, Serializable {
	
	private static final long serialVersionUID = 1L;
	
	/** 
	 * the URL of a domain hosted HTML file that contains feedback content 
	 * e.g. http://10.1.21.180:8885/COIN/badFeedback.html
	 */
	private String domainURL;
	
    /**
     * Default Constructor
     *
     * Required to be exist and be public for GWT compatibility
     */
    public DisplayHTMLFeedbackAction() {
        
    }
    
    /**
     * Constructor - set attribute(s)
     *
     * @param domainURL - URL of the HTML file hosted by the Domain module.
     */
    public DisplayHTMLFeedbackAction(String domainURL) {
        
    	if(domainURL == null){
    		throw new IllegalArgumentException("The domain URL can't be null.");
    	}
    	
    	this.domainURL = domainURL;
    }
    
    /**
     * Return the URL of a domain hosted HTML file that contains feedback content 
	 * e.g. http://10.1.21.180:8885/COIN/badFeedback.html
	 * 
     * @return String
     */
    public String getDomainURL(){
    	return domainURL;
    }

	@Override
	public boolean hasAudio() {
		return false;
	}

	@Override
	public String toString(){
		
		StringBuffer sb = new StringBuffer();
		sb.append("[DisplayHTMLFeedbackAction: ");
		sb.append("domainURL = ").append(domainURL);
		sb.append("]");
		
		return sb.toString();
	}
}
