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
 * This class contains information about text that needs to be displayed by the Tutor
 *
 * @author jleonard
 */
public class DisplayHtmlPageGuidanceTutorRequest extends AbstractDisplayGuidanceTutorRequest implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String url = null;
    
    private String message = null;
    
    /** Whether or not the url violates same origin policy or contains blocked content. */
    private boolean openInNewWindow;

    /**
     * Default constructor
     */
    public DisplayHtmlPageGuidanceTutorRequest() {
        super();
    }
    
    /**
     * Class constructor 
     * 
     * @param url the address of the HTML page to display
     * @param fullscreen whether the guidance content should be displayed in full screen on the tutor
     */
    public DisplayHtmlPageGuidanceTutorRequest(String url, boolean fullscreen) {
        super(fullscreen);

        this.url = url;
    }
    
    /**
    *
    * @param url The webpage to display
    * @param message Supplementary message to display alongside webpage.
    * @param fullscreen whether the guidance content should be displayed in full screen on the tutor
    */
   public DisplayHtmlPageGuidanceTutorRequest(String url, String message, boolean fullscreen) {
       super(fullscreen);

       this.url = url;
       this.message = message;
   }

    /**
     * Class constructor
     *
     * @param url The text to be displayed
     * @param fullscreen whether the guidance content should be displayed in full screen on the tutor
     * @param displayDuration The amount of time the text should be displayed
     * before being removed
     */
    public DisplayHtmlPageGuidanceTutorRequest(String url, boolean fullscreen, int displayDuration) {
        super(fullscreen, displayDuration);

        this.url = url;
    }
    
    /**
     * Class constructor
     *
     * @param url The text to be displayed
     * @param message Supplementary message to display alongside webpage.
     * @param fullscreen whether the guidance content should be displayed in full screen on the tutor
     * @param displayDuration The amount of time the text should be displayed
     * before being removed
     */
    public DisplayHtmlPageGuidanceTutorRequest(String url, String message, boolean fullscreen, int displayDuration) {
        super(fullscreen, displayDuration);

        this.url = url;
        this.message = message;
    }
    
    /**
     * Class constructor 
     *
     * @param url The text to be displayed
     * @param message Supplementary message to display alongside webpage.
     * @param fullscreen whether the guidance content should be displayed in full screen on the tutor
     * @param displayDuration The amount of time the text should be displayed
     * before being removed
     * @param openInNewWindow Whether or not the url should open in a new window
     */
    public DisplayHtmlPageGuidanceTutorRequest(String url, String message, boolean fullscreen, int displayDuration, boolean openInNewWindow) {
        super(fullscreen, displayDuration);

        this.url = url;
        this.message = message;       
        this.openInNewWindow = openInNewWindow;
        
    }

    /**
     * Returns the text to be displayed
     *
     * @return String The text to be displayed
     */
    public String getUrl() {
        return url;
    }
    
    /**
     * Gets the supplementary message to display.
     *  
     * @return String The supplementary message to display.
     */
    public String getMessage() {
    	return message;
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
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[DisplayHtmlPageGuidanceTutorRequest: ");
        sb.append(" url = ").append(url);
        sb.append(", message = ").append(message);
        sb.append(", shouldOpenInNewWindow = ").append(openInNewWindow);
        sb.append(", ").append(super.toString());
        sb.append("]");

        return sb.toString();
    }
}
