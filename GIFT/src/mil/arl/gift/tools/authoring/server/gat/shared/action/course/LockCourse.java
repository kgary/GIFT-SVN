/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.course;

import net.customware.gwt.dispatch.shared.Action;

/**
 * Action for locking a course.
 * 
 * @author elafave
 */
public class LockCourse implements Action<LockFileResult> {

    /**
     * The workspace relative path of the course being locked. e.g.
     * mhoffman/test/text.course.xml, Public/Hello World/hello world.course.xml
     */
    private String path;

    /** The user name. */
    private String userName;

    /** The identifier for the browser session */
    private String browserSessionKey;

    /**
     * The flag that specifies whether this is the first request to acquire the
     * lock or whether this is a request to renew the lock
     */
    private boolean initialAcquisition = false;

    /**
     * No-arg constructor. Needed for serialization.
     */
    public LockCourse() {
    }

    /**
     * Gets the workspace relative path of the course being locked. e.g.
     * mhoffman/test/text.course.xml, Public/Hello World/hello world.course.xml
     *
     * @return the path to the course of interest
     */
    public String getPath() {
        return path;
    }

    /**
     * Sets the workspace relative path of the course being locked. e.g.
     * mhoffman/test/text.course.xml, Public/Hello World/hello world.course.xml
     * 
     * @param path for the course of interest. Can't be null or empty.
     */
    public void setPath(String path) {

        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException("The path to the course to lock can't be null or empty.");
        }

        this.path = path;
    }

    /**
     * Gets the user name.
     * 
     * @return User name.
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Sets the user name.
     * 
     * @param userName User name.
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Gets the browser session key
     * 
     * @return the value of the browser session key, can be null
     */
    public String getBrowserSessionKey() {
        return browserSessionKey;
    }

    /**
     * Sets the browser session key
     * 
     * @param browserSessionKey the new value of the browser session key, can be null
     */
    public void setBrowserSessionKey(String browserSessionKey) {
        this.browserSessionKey = browserSessionKey;
    }

    /**
     * Gets the flag indicating whether this represents an initial acquisition
     * 
     * @return the value of the initialAcquisition flag
     */
    public boolean getInitialAcquistion() {
        return initialAcquisition;
    }

    /**
     * Sets the flag indicating whether this represents an initial acquisition
     * 
     * @param initialAcquisition the new value of the initialAcquisition flag
     */
    public void setInitialAcquisition(boolean initialAcquisition) {
        this.initialAcquisition = initialAcquisition;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[LockCourse: path=");
        builder.append(path);
        builder.append(", userName=");
        builder.append(userName);
        builder.append(", browserSessionKey=");
        builder.append(browserSessionKey);
        builder.append(", initialAcquisition=");
        builder.append(initialAcquisition);
        builder.append("]");
        return builder.toString();
    }
}