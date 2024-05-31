/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.remote;

import java.io.IOException;

/**
 * Class to encapsulate a launch command sent from the Monitor (Remote Launch
 * Panel) to a workstation.
 *
 * @author cragusa
 */
public class LaunchCommand implements EncodableDecodable {

    /** The actual command */
    private String launchString;

    /** No-arg constructor, should only be called by the RemoteMessageUtil class */
    LaunchCommand() {
    }

    /**
     * Constructor with launch string included.
     *
     * @param launchString The launch string
     */
    public LaunchCommand(String launchString) {

        this.launchString = launchString;
    }

    /**
     * Gets the launch string.
     *
     * @return String The launch string
     */
    public String getLaunchString() {
        return launchString;
    }

    /**
     * Sets the launch string.
     *
     * @param launchString The launch string
     */
    void setLaunchString(String launchString) {
        this.launchString = launchString;
    }

    @Override
    public void decode(String string, char delimiter) throws IOException {

        if (string == null) {

            throw new IOException();
        }

        setLaunchString(string.trim());
    }

    @Override
    public void encode(StringBuffer buffer, char delimiter) throws IOException {

        if (buffer == null || launchString == null) {

            throw new IOException();
        }

        buffer.append(launchString);
    }

    @Override
    public RemoteMessageType getMessageType() {

        return RemoteMessageType.LAUNCH_COMMAND;
    }

    @Override
    public String toString() {

        return "LaunchCommand [launchString=" + launchString + "]";
    }
}
