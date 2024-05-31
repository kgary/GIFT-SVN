/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.aar;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.util.StringUtils;

/**
 * A summary of a scenario that was executed within a log. Includes the data
 * needed to extract the pertinent messages from the log as well as high level
 * metadata about the scenario.
 *
 * @author tflowers
 *
 */
public class LogMetadata implements Serializable {

    /** The version number used by the java serialization logic */
    private static final long serialVersionUID = 1L;

    /** The session which is described by the log */
    private AbstractKnowledgeSession session;

    /** The name of the file from which the log originates */
    private String logFile;

    /** The bounds of the messages to include from {@link #logFile}. */
    private LogSpan logSpan;

    /** The timestamp at which the first message was sent. */
    private long startTime;

    /** The timestamp at which the last message was sent. */
    private long endTime;
    
    /** usernames for those users that have marked this log as one of their favorites */
    private Set<String> usersFavorite = new HashSet<>();

    /** The name of the file from which the log patch originates */
    private String logPatchFile;

    /** The LOMs for the session's video files */
    private Set<VideoMetadata> videoFiles = new HashSet<>();
    
    /** The DKF file associated with the knowledge session from the log */
    private String dkf;

    /** Private constructor that makes this type GWT serializable. */
    private LogMetadata() {
    }

    /**
     * Constructs a {@link LogMetadata} object from a provided
     * {@link AbstractKnowledgeSession}, the name of a log file, and the message
     * bounds of the messages to include from that log file.
     *
     * @param session The {@link AbstractKnowledgeSession} whose execution is
     *        represented by this {@link LogMetadata} object. Can't be null.
     *        Must be {@link SessionType#PAST} type.
     * @param logFile The name of the log file that this {@link LogMetadata}
     *        object represents. Can't be null.
     * @param logSpan The bounds for the messages that comprise the session
     *        described by this {@link LogMetadata} object. Can't be null.
     * @param startTime The time in milliseconds at which the first message
     *        described by this {@link LogMetadata} was sent. Must be greater
     *        than or equal to zero.
     * @param endTime The time in milliseconds at which the last message
     *        described by this {@link LogMetadata} was sent. Must be greater
     *        than the provided start time.
     */
    public LogMetadata(AbstractKnowledgeSession session, String logFile, LogSpan logSpan, long startTime, long endTime) {
        this();
        setSession(session);
        setLogFile(logFile);
        setLogSpan(logSpan);
        setStartTime(startTime);
        setEndTime(endTime);
    }
    
    /**
     * Return the usernames for those users that have marked this log as one of their favorites
     * @return can be empty.
     */
    public Set<String> getUsersFavorite(){
        return usersFavorite;
    }
    
    /**
     * Add a user to the usernames for those users that have marked this log as one of their favorites
     * @param username if null or empty this method does nothing
     */
    public void addUserToFavorites(String username){
        
        if(StringUtils.isBlank(username)){
            return;
        }
        
        usersFavorite.add(username);
    }

    /**
     * Get the LOMs for the session's video files
     * 
     * @return the set of video file LOM types. Can't be null.
     */
    public Set<VideoMetadata> getVideoFiles() {
        return videoFiles;
    }

    /**
     * Getter for the session.
     *
     * @return The value of {@link #session}. Can't be null.
     */
    public AbstractKnowledgeSession getSession() {
        return session;
    }

    /**
     * Setter for the session.
     *
     * @param session The new value of {@link #session}.
     */
    private void setSession(AbstractKnowledgeSession session) {
        if (session == null) {
            throw new IllegalArgumentException("The parameter 'session' cannot be null.");
        } else if (!session.inPastSessionMode()) {
            /* Session mode must be set to past session mode */
            throw new IllegalArgumentException("The parameter 'session' must be a PAST type.");
        }

        this.session = session;
    }

    /**
     * Getter for the logFile.
     *
     * @return The value of {@link #logFile}. Can't be null. 
     * E.g. domainSession1277_uId1\domainSession1277_uId1_2021-09-30_13-45-10.protobuf.bin
     */
    public String getLogFile() {
        return logFile;
    }

    /**
     * Setter for the logFile.
     *
     * @param logFile The new value of {@link #logFile}. 
     * e.g. domainSession1277_uId1\domainSession1277_uId1_2021-09-30_13-45-10.protobuf.bin
     */
    private void setLogFile(String logFile) {
        if (logFile == null) {
            throw new IllegalArgumentException("The parameter 'logFile' cannot be null.");
        }

        this.logFile = logFile;
    }

    /**
     * Getter for the logSpan.
     *
     * @return The value of {@link #logSpan}. Can't be null.
     */
    public LogSpan getLogSpan() {
        return logSpan;
    }

    /**
     * Setter for the logSpan.
     *
     * @param logSpan The new value of {@link #logSpan}. Can't be null.
     */
    private void setLogSpan(LogSpan logSpan) {
        if (logSpan == null) {
            throw new IllegalArgumentException("The parameter 'logSpan' cannot be null.");
        }

        this.logSpan = logSpan;
    }

    /**
     * Getter for the timestamp at which the first message was sent.
     *
     * @return The value of {@link #startTime}. Must be greater than or equal to
     *         zero.
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * Setter for the timestamp at which the first message was sent.
     *
     * @param startTime The new value of {@link #startTime}. Must be greater
     *        than or equal to zero.
     */
    private void setStartTime(long startTime) {
        if (startTime < 0) {
            throw new IllegalArgumentException("The value of 'startTime' must be greater than or equal to zero.");
        }
        this.startTime = startTime;
    }

    /**
     * Getter for the timestamp at which the last message was sent.
     *
     * @return The value of {@link #endTime}.
     */
    public long getEndTime() {
        return endTime;
    }

    /**
     * Setter for the timestamp at which the last message was sent.
     *
     * @param endTime The new value of {@link #endTime}. Must be greater than or
     *        equal to {@link #getStartTime()}.
     */
    public void setEndTime(long endTime) {
        if (endTime < startTime) {
            throw new IllegalArgumentException("The value of 'endTime' must be greater than or equal to 'startTime'.");
        }

        this.endTime = endTime;
    }

    /**
     * A convenience method for calculating the duration of the described DKF
     * session based on the values of {@link #getStartTime()} and
     * {@link #getEndTime()}.
     *
     * @return The duration of the DKF session measured in milliseconds. Must be positive.
     */
    public long getDuration() {
        return endTime - startTime;
    }

    /**
     * Set the log patch file path.
     * 
     * @param logPatchFile the log patch file path. Can be null.
     */
    public void setLogPatchFile(String logPatchFile) {
        this.logPatchFile = logPatchFile;
    }

    /**
     * Get the patch file path for this log
     * 
     * @return the log file patch path. Can be null if a patch doesn't exist.
     */
    public String getLogPatchFile() {
        return logPatchFile;
    }

    /**
     * Checks if a patch file exists for this log metadata.
     * 
     * @return true if a patch file exists; false otherwise.
     */
    public boolean hasLogPatchFile() {
        return StringUtils.isNotBlank(logPatchFile);
    }
    
    /**
     * Gets the DKF file associated with the knowledge session from the log
     * 
     * @return the DKF file name. Can be null for older logs.
     */
    public String getDkf() {
        return dkf;
    }
    
    /**
     * Sets the DKF file associated with the knowledge session from the log
     * 
     * @param dkf the DKF file name. Can be null for older logs.
     */
    public void setDkf(String dkf) {
        
        /* If the DKF file path was recorded to the log on a Windows machine, it may use Windows file separators, which can cause an issue 
         * if the log is then played back in Linux. A precaution, we should replace these with generic Java file separators so that they
         * are platform agnostic. */
        this.dkf = dkf != null
                ? dkf.replace("\\", "/")
                : null;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((logFile == null) ? 0 : logFile.hashCode());
        result = prime * result + (int) (startTime ^ (startTime >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof LogMetadata)) {
            return false;
        }
        LogMetadata other = (LogMetadata) obj;
        if (logFile == null) {
            if (other.logFile != null) {
                return false;
            }
        } else if (!logFile.equals(other.logFile)) {
            return false;
        }
        if (startTime != other.startTime) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[LogMetadata: session = ");
        builder.append(session);
        builder.append(", logFile = ");
        builder.append(logFile);
        builder.append(", logSpan = ");
        builder.append(logSpan);
        builder.append(", startTime = ");
        builder.append(startTime);
        builder.append(", endTime = ");
        builder.append(endTime);
        builder.append(", usersFavorite = ");
        builder.append(usersFavorite);
        builder.append(", video size = ");
        builder.append(videoFiles.size());
        builder.append(", dkf = ");
        builder.append(dkf);
        builder.append("]");
        return builder.toString();
    }
    
    
}