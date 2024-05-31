/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.ert.event;

import java.util.ArrayList;
import java.util.List;

import mil.arl.gift.common.ert.EventReportColumn;
import mil.arl.gift.common.ert.server.Cell;
import mil.arl.gift.common.ert.server.MessageLogEventSourceParser;
import mil.arl.gift.common.logger.DomainSessionMessageEntry;


/**
 * The domain session event representation for events that will be used in an event report.
 * This class stores the content/information on a domain session event for a row in the report.
 * 
 * @author mhoffman
 *
 */
public class DomainSessionEvent extends AbstractEvent {

    /** the elapsed domain session time in seconds for when the message was created*/
    private double ds_time;
    
    /** the elapsed time in seconds since the start of a DKF (null if not in a DKF) */
    private Double dkf_time;
    
    /** the elapsed domain session time in seconds for when the message was written to the message log */
    private double writeTime;
    
    /** unique id corresponding to running a course */
    private int domainSessionId;

    /** The user id associated with the domain session message */
    private int userId;
    
    /** (optional) participant id from an experiment where the user is anonymous */
    private Integer participantId = null;

    /** The username of the user associated with this event */
    private String username;

    /** (optional) generic information about the event */
    private String content = null;
    
    /** (optional) generic information about why the event occurred */
    private String reason = null;

    /** contains columns specific to this event */
    private List<EventReportColumn> columns = new ArrayList<EventReportColumn>();
    
    /** cell containing the domain session event time value */
    private Cell ds_timeCell = null;
    
    /** cell containing the course attempt int value */
    private Cell courseAttemptCell = null;
    
    /**
     * Class constructor - set attributes.  This constructor uses a generic column for the content provided
     * 
     * @param name - name of the event
     * @param time - epoch time of the event
     * @param ds_time - elapsed domain session time of the event
     * @param userId - the user id associated with the domain session message
     * @param domainSessionId - the domain session id associated with the domain session message
     * @param content - content of the event.  If null, the content cell will not be added for this event instance.
     */
    public DomainSessionEvent(String name, long time, double ds_time, int userId, int domainSessionId, String content){
        super(name, time);       
        
        setContent(content);
        setDomainSessionTime(ds_time);
        setUserId(userId);
        setDomainSessionId(domainSessionId);
    }
    
    /**
     * Class constructor - set attribute(s)
     * 
     * @param name - name of the event
     * @param time - epoch time of the event
     * @param domainSessionMessageEntry - general info about the domain session message
     * @param content - content of the event. If null, the content cell will not be added for this event instance.
     */
    public DomainSessionEvent(String name, long time, DomainSessionMessageEntry domainSessionMessageEntry, String content){
        super(name, time);
        
        setDomainSessionTime(domainSessionMessageEntry.getElapsedDSTime());
        setUserId(domainSessionMessageEntry.getUserId());
        setDomainSessionId(domainSessionMessageEntry.getDomainSessionId());
        setUsername(domainSessionMessageEntry.getUsername());
        setWriteTime(domainSessionMessageEntry.getWriteTime());
        setContent(content);     
    }
    
    /**
     * Class constructor - set attribute(s)
     * 
     * @param name - name of the event
     * @param ds_time - elapsed domain session time of the event
     * @param userId - the user id associated with the domain session message
     * @param domainSessionId - the domain session id associated with the domain session message
     */
    public DomainSessionEvent(String name, double ds_time, int userId, int domainSessionId){
        super(name);
        
        setDomainSessionTime(ds_time);
        setUserId(userId);
        setDomainSessionId(domainSessionId);
    }
    
    /**
     * Class constructor - set attribute(s).
     * Note: this constructor should only be used when the user and domain session id's are not known, but
     * it is a domain session event regardless.
     * 
     * @param name - name of the event
     * @param time - epoch time of the event
     * @param ds_time - elapsed domain session time of the event
     * @param content - content of the event
     */
    public DomainSessionEvent(String name, long time, double ds_time, String content){
        super(name, time);
        
        setDomainSessionTime(ds_time);        
        setContent(content);
    }
    
    /**
     * Set the elapsed time in seconds since the start of a DKF (null if not in a DKF) 
     * @param elapsedDKFTime can be null if this event is not during a DKF
     */
    public void setElapsedDKFTime(Double elapsedDKFTime){
        this.dkf_time = elapsedDKFTime;
        if(dkf_time != null){
            cells.add(new Cell(String.valueOf(dkf_time), EventReportColumn.DKF_TIME_COLUMN));
        }       
    }

    /**
     * Set the value for the content and create a cell for it.
     * 
     * @param content the content of the session event. If null, the content cell will not be added for this event instance.
     */
    private void setContent(String content) {
        this.content = content;
        if(this.content != null){
            cells.add(new Cell(content, EventReportColumn.CONTENT_COLUMN));
        }
    }
    
    /**
     * Set the value for the course attempt tracker and create a cell for it.
     * 
     * @param courseAttemptt the course attempt for this event.
     */
    public void setCourseAttempt(int courseAttempt) {
        if(courseAttemptCell == null){
            courseAttemptCell = new Cell(String.valueOf(courseAttempt), EventReportColumn.COURSE_ATTEMPT_COL);
            cells.add(courseAttemptCell);
        }       
        
        courseAttemptCell.setValue(String.valueOf(courseAttempt));
    }

    /**
     * Set the value for the domain session id and create a cell for it.
     * 
     * @param domainSessionId the value of the domain session id.
     */
    private void setDomainSessionId(int domainSessionId) {
        this.domainSessionId = domainSessionId;
        cells.add(new Cell(String.valueOf(domainSessionId), EventReportColumn.DS_ID_COLUMN));
    }
    
    /**
     * Set the value for the user id and create a cell for it.
     * 
     * @param userId the user id.
     */
    private void setUserId(int userId) {
        this.userId = userId;
        cells.add(new Cell(String.valueOf(userId), EventReportColumn.USER_ID_COLUMN));
    }

    /**
     * Set the elapsed domain session time for when the message was written to the message log
     * 
     * @param writeTime the elapsed domain session time for when the message was written to the message log
     */
    public void setWriteTime(double writeTime){
        this.writeTime = writeTime;
        cells.add(new Cell(String.valueOf(writeTime), MessageLogEventSourceParser.DS_WRITE_TIME_COLUMN)); 
    }   
    
    /**
     * Set the username of the user associated with this event.
     * 
     * @param username - the GIFT username for the user
     */
    public void setUsername(String username){
        this.username = username;
        cells.add(new Cell(username, EventReportColumn.USERNAME_COLUMN)); 
    }
    
    /**
     * Return the elapsed domain session time this event happened.
     * 
     * @return elapsed domain session time of this event
     */
    protected double getDomainSessionTime(){
        return ds_time;
    }
    
    /**
     * Set the elapsed domain session time this event happened.
     * 
     * @param ds_time elapsed domain session time of this event
     */
    public void setDomainSessionTime(double ds_time){
            
        this.ds_time = ds_time;
        if(ds_timeCell == null){
            ds_timeCell = new Cell(String.valueOf(ds_time), EventReportColumn.DS_TIME_COLUMN);
            cells.add(ds_timeCell);
        }       
        
        ds_timeCell.setValue(String.valueOf(ds_time));
        
    }
    
    /**
     * Return the unique domain session id that this event was captured in.
     * 
     * @return domain session id for the session this event happened in
     */
    public int getDomainSessionId(){
        return domainSessionId;
    }
    
    /**
     * Return the unique user id for the user that caused this event.
     * 
     * @return user id for the user that caused this event
     */
    public int getUserId(){
        return userId;
    }
    
    /**
     * Return the participant id from an experiment where the user is anonymous
     * 
     * @return can be null if not set.  Null participant id and a populated username 
     * is usually an indicator that this is not an experiment delivered domain session.
     */
    public Integer getParticipantId() {
        return participantId;
    }

    /**
     * Set the participant id from an experiment where the user is anonymous
     * @param participantId can be null 
     */
    public void setParticipantId(Integer participantId) {
        this.participantId = participantId;
        if(participantId != null){
            cells.add(new Cell(String.valueOf(participantId), EventReportColumn.PARTICIPANT_ID_COL));
        }
    }

    @Override
    public List<EventReportColumn> getColumns(){
        return columns;
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[DomainSessionEvent: ");
        sb.append(super.toString());
        sb.append(", ds time = ").append(ds_time);
        sb.append(", write time = ").append(writeTime);
        sb.append(", user id = ").append(userId);
        if(participantId != null){
            sb.append(", participant id = ").append(participantId);
        }
        sb.append(", username = ").append(username);
        sb.append(", domain session id = ").append(domainSessionId);

        if (content != null) {
            sb.append(", content = ").append(content);
        }

        if (reason != null) {
            sb.append(", reason = ").append(reason);
        }

        sb.append("]");
        return sb.toString();
    }
}
