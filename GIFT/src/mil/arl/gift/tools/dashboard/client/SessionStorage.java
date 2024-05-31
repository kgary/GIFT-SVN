/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client;



import java.util.logging.Logger;

import com.google.gwt.storage.client.Storage;

/**
 * Helper class for using gwt html5 session storage.  Docs are provided here on usage
 * http://www.gwtproject.org/doc/latest/DevGuideHtml5Storage.html
 * 
 * @author nblomberg
 *
 */
public class SessionStorage {
   
    /** logger which can be used to log to the browser console window */
    private static Logger logger = Logger.getLogger(SessionStorage.class.getName());

    /** Token/key value used to store the dashboard session id. */
    public static final String DASHBOARD_SESSIONID_TOKEN = "dashboardSessionId";

    /**
     * Constructor - private
     */
    private SessionStorage() {

    }
    
    /**
     * Gets an item from session storage based on the key / token value
     * 
     * @param token The token / key to lookup
     * @return The value of the token/key if found (null if the token doesn't exist).
     */
    public static String getItem(String token) {
        
        String value = null;
        Storage storage = Storage.getSessionStorageIfSupported();
        if (storage != null) {
            value = storage.getItem(token);
        } else {
            logger.severe("Unable to get an item from storage.  Trying to access HTML5 session storage and it is not supported.");
        }
        
        return value;
    }
    
    /**
     * Puts an item into session storage based on the token.
     * 
     * @param token The token that the item will be stored in
     * @param value The value of the token
     */
    public static void putItem(String token, String value) {
        Storage storage = Storage.getSessionStorageIfSupported();
        if (storage != null) {
            storage.setItem(token, value);
        } else {
            logger.severe("Unable to put an item into storage.  Trying to access HTML5 session storage and it is not supported.");
        }
    }
    
    /**
     * Removes an item from session storage based on the token
     * If the token is found in session storage, it is removed.
     * 
     * @param token the token to lookup. 
     */
    public static void removeItem(String token) {
        Storage storage = Storage.getSessionStorageIfSupported();
        if (storage != null) {
            storage.removeItem(token);
        } else {
            logger.severe("Unable to remove item from storage.  Trying to access HTML5 session storage and it is not supported.");
        }
    }
    
   

}
