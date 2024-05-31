/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client;

import com.google.gwt.user.client.Cookies;
import java.util.Date;

/**
 * Contains the current browser properties
 *
 * @author jleonard
 */
public class BrowserProperties {

    /**
     * Default browser cookie property TTL
     */
    private final static long COOKIE_DURATION = 1000 * 60 * 60 * 24 * 2;
    /**
     * Property keys
     */
    private final static String USER_SESSION_KEY_COOKIE_KEY = "userSessionKey";
    /**
     * The singleton instance
     */
    private static BrowserProperties instance = null;

    private String userSessionKey = null;

    /**
     * Gets the singleton instance of the browser properties
     *
     * @return BrowserProperties The instance of browser properties
     */
    public static BrowserProperties getInstance() {

        if (instance == null) {

            synchronized (BrowserProperties.class) {

                instance = new BrowserProperties();
            }
        }

        return instance;
    }

    /**
     * Constructor
     */
    private BrowserProperties() {
        userSessionKey = Cookies.getCookie(USER_SESSION_KEY_COOKIE_KEY);
    }

    /**
     * Gets the user session key in the cookie
     *
     * @return String The user session key in the cookie
     */
    public String getUserSessionKey() {
        return userSessionKey;
    }

    /**
     * Sets the user session key
     *
     * @param userSessionKey The active user session key
     */
    public void setUserSessionKey(String userSessionKey) {
        this.userSessionKey = userSessionKey;
        if (userSessionKey != null) {
            writeProperty(USER_SESSION_KEY_COOKIE_KEY, userSessionKey);
        } else {
            removeProperty(USER_SESSION_KEY_COOKIE_KEY);
        }
    }

    /**
     * Writes a property to the cookie
     *
     * @param key The key of the property
     * @param value The value of the property
     */
    private void writeProperty(String key, String value) {
        Date expires = new Date(System.currentTimeMillis() + COOKIE_DURATION);
        Cookies.setCookie(key, value, expires, null, "/", false);
    }

    /**
     * Removes a property from the cookie
     *
     * @param key The key of the property
     */
    private void removeProperty(String key) {
        Cookies.removeCookie(key);
    }
}
