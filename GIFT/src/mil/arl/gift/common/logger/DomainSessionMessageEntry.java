/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.logger;

import mil.arl.gift.common.UserSession;
import mil.arl.gift.net.api.message.DomainSessionMessage;
import mil.arl.gift.net.api.message.DomainSessionMessageInterface;
import mil.arl.gift.net.api.message.Message;

/**
 * This class represents a domain session message entry in the domain session message log file.  It contains
 * extra information about the entry such as the elapsed domain session time for when the message was created and
 * when it was written to the file.
 *
 * @author mhoffman
 *
 */
public class DomainSessionMessageEntry extends Message implements DomainSessionMessageInterface {

    /** the elapsed domain session time for when the message was created  */
    private final double elapsedDSTime;

    /** the elapsed domain session time for when the message was written to the message log */
    private final double writeTime;

    /** information about the user session (including the unique user id of the learner) the message is associated with */    
    private UserSession userSession; 

    /** unique id of the domain session among all course executions in a gift instance */
    private final int domainSessionId;
    
    /** The unique ID of the service being used to play back this session, if this session is being played back*/
    private String playbackId;

    /**
     * Class constructor - set attributes
     *
     * @param eventSourceId - the event id created by the sender
     * @param domainSessionId - the domain session id associated with the domain
     *        session message
     * @param userSession - The {@link UserSession} that contains the properties
     *        of the mobile app associated with the domain session message, if
     *        applicable
     * @param elapsedDSTime - amount of time elapsed from the start of the
     *        domain session when this message was created
     * @param writeTime - the time at which this message was written to the
     *        message log file
     * @param message - the contents of the message
     */
    public DomainSessionMessageEntry(int eventSourceId, int domainSessionId, UserSession userSession,
            double elapsedDSTime, double writeTime, Message message) {
        super(message.getMessageType(), message.getSequenceNumber(),eventSourceId, message.getTimeStamp(), message.getSenderModuleName(), message.getSenderAddress(),
                message.getSenderModuleType(), message.getDestinationQueueName(), message.getPayload(), message.needsHandlingResponse());

        if (userSession == null) {
            throw new IllegalArgumentException("The parameter 'userSession' cannot be null.");
        }

        this.elapsedDSTime = elapsedDSTime;
        this.writeTime = writeTime;
        this.userSession = userSession;
        this.domainSessionId = domainSessionId;

        setReplyToSequenceNumber(message.getReplyToSequenceNumber());
    }

    /**
     * Creates a shallow copy of this {@link DomainSessionMessageEntry}.
     *
     * @return The newly created copy of the {@link DomainSessionMessageEntry}.
     */
    private DomainSessionMessageEntry shallowCopy() {
        return new DomainSessionMessageEntry(getSourceEventId(), domainSessionId, userSession, elapsedDSTime, writeTime,
                this);
    }

    /**
     * Return the elapsed domain session time for when the message was created
     *
     * @return double
     */
    public double getElapsedDSTime(){
        return elapsedDSTime;
    }

    /**
     * Return the elapsed domain session time for when the message was written
     * to the message log
     *
     * @return double
     */
    public double getWriteTime(){
        return writeTime;
    }

    @Override
    public UserSession getUserSession() {
        return userSession;
    }

    @Override
    public int getUserId() {
        return userSession.getUserId();
    }

    @Override
    public String getExperimentId() {
        return userSession.getExperimentId();
    }

    @Override
    public String getUsername() {
        return userSession.getUsername();
    }

    /**
     * Creates a shallow copy of this {@link DomainSessionMessageEntry} that
     * differs only in the value of its username field.
     *
     * @param username The new {@link String} value to use for the username.
     * @return The newly created {@link DomainSessionMessageEntry}. Can't be
     *         null.
     */
    public DomainSessionMessageEntry replaceUsername(String username) {
        DomainSessionMessageEntry toRet = shallowCopy();
        toRet.getUserSession().setUsername(username);
        return toRet;
    }

    @Override
    public int getDomainSessionId(){
        return domainSessionId;
    }

    /**
     * Gets the unique ID of the service being used to play back this session, if this session is being played back
     * 
     * @return the ID of this session's playback service. Can be null.
     */
    public String getPlaybackId() {
        return playbackId;
    }

    /**
     * Sets the unique ID of the service being used to play back this session
     * 
     * @param playbackId the ID of this session's playback service. Can be null.
     */
    public void setPlaybackId(String playbackId) {
        this.playbackId = playbackId;
    }

    /**
     * Parse the time stamps contained in the domain session message entry from
     * the message log file and return a new instance of this class.
     *
     * @param rawTimestamps - contains the time stamps to separate
     * @param message - the decoded message from the domain session message
     *        entry in the log file
     * @return DomainSessionMessageEntry - new instance
     */
    public static DomainSessionMessageEntry parseMessageEntry(String rawTimestamps, DomainSessionMessage message) {

        String[] timestamps = rawTimestamps.split(" ");

        DomainSessionMessageEntry entry = null;
        if(timestamps.length == 2){
            entry = new DomainSessionMessageEntry(message.getSourceEventId(),
                    message.getDomainSessionId(),
                    message.getUserSession(), 
                    Double.parseDouble(timestamps[0]),
                    Double.parseDouble(timestamps[1]),
                    message);
        }

        return entry;
    }
}
