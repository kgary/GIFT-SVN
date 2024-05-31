/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.util;

import mil.arl.gift.common.util.StringUtils;
import net.customware.gwt.dispatch.shared.Action;

/**
 * An action telling the server to parse a playback log
 * 
 * @author sharrison
 */
public class ParsePlaybackLog implements Action<ParsePlaybackLogResult> {

    /** The requesting user */
    private String username;

    /** The browser session key */
    private String browserSessionKey;

    /** The filename of the playback log to parse */
    private String filename;

    /**
     * Constructor
     */
    private ParsePlaybackLog() {
    }

    /**
     * Creates a request with populated parameters
     * 
     * @param username the name of the user who is making the request
     * @param browserSessionKey the unique identifier of the browser making the
     *        request
     * @param filename the playback log filename. Can't be null or empty.
     */
    public ParsePlaybackLog(String username, String browserSessionKey, String filename) {
        this();

        if (StringUtils.isBlank(filename)) {
            throw new IllegalArgumentException("The parameter 'filename' cannot be blank.");
        }

        this.username = username;
        this.browserSessionKey = browserSessionKey;
        this.filename = filename;
    }

    /**
     * Get the username of the requester.
     * 
     * @return the username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Gets the value of the unique identifier for the browser that is making
     * the request
     * 
     * @return the value of the browser session key.
     */
    public String getBrowserSessionKey() {
        return browserSessionKey;
    }

    /**
     * Get the filename value of the playback log.
     * 
     * @return the filename of the playback log. Will never be null.
     */
    public String getFilename() {
        return filename;
    }

    @Override
    public String toString() {

        StringBuffer sb = new StringBuffer();
        sb.append("[ParsePlaybackLog: ");
        sb.append("username = ").append(username);
        sb.append(", browserSessionKey = ").append(browserSessionKey);
        sb.append(", filename = ").append(filename);
        sb.append("]");

        return sb.toString();
    }
}
