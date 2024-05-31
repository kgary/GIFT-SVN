/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.shared;

import com.google.gwt.user.client.rpc.IsSerializable;



/**
 * The PageLoadError object contains the items needed to display on a page load error screen.
 * 
 * @author nblomberg
 *
 */
public class PageLoadError implements IsSerializable {

   
    /** The title of the page load error that will be presented to the user. */
    private String errorTitle = "";
    /** The error description of the page load error that will be presented to th euser. */
    private String errorMessage = "";
    
    /**
     * Constructor
     * @param title - The title of the page load error that will be presented to the user.
     * @param message - A more detailed description of the error that will be presented to the user.
     */
    public PageLoadError(String title, String message) {
        errorTitle = title;
        errorMessage = message;
    }
    
    /**
     * Accessor to retrieve the error title.
     * @return String - The title of the error.
     */
    public String getErrorTitle() {
        return errorTitle;
    }
    
    /**
     * Accessor to retrieve the error message.
     * @return String - The description of the error message.
     */
    public String getErrorMessage() {
        return errorMessage;
    }
    

}
