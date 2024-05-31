/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.course;

import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;


/**
 * The result of a Fetch Content Address {@link FetchContentAddress}.
 * 
 * @author mhoffman
 *
 */
public class FetchContentAddressResult extends GatServiceResult{
	
	/** the URL of the course folder content */
	private String contentURL;
	
	/** 
	 * Whether or not the url violates the same origin policy constraint.
	 * Used to determine if can be displayed in an iframe 
	 */
	private boolean violatesSOP = false;
	
	/**
	 * Class constructor
	 * For serialization only.
	 */
	public FetchContentAddressResult() {
    }


    /**
     * The URL of the content requested.
     * 
     * @return the URL of the content requested in the course folder.  Will be null or empty string
     * if the content wasn't found.
     */
    public String getContentURL() {
        return contentURL;
    }

    /**
     * Set the content URL for the content requested.
     * 
     * @param contentURL the URL of the content. If null or empty string the content doesn't exist.
     */
    public void setContentURL(String contentURL) {
        this.contentURL = contentURL;
    }
    
    /**
     * Sets whether or not this url violates same origin policy checks
     *  
     * @param violatesSOP false if the url can does not violate SOP and can be displayed in an iframe, true otherwise.
     */
    public void setViolatesSOP(boolean violatesSOP) {
        this.violatesSOP = violatesSOP;
    }
    
    /**
     * Returns whether or not this url violates same origin policy checks
     * 
     * @return false if the url can does not violate SOP and can be displayed in an iframe, true otherwise.
     */
    public boolean violatesSOP() {
        return violatesSOP;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[FetchContentAddressResult: ");
        builder.append(", contentURL=");
        builder.append(contentURL);
        builder.append(", violatesSOP=");
        builder.append(violatesSOP);
        builder.append("]");
        return builder.toString();
    }
	
	
	
}
