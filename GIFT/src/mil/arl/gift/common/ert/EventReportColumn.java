/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.ert;

import java.io.Serializable;

/**
 * This class represents an ERT column in the output report file.  It is a header
 * for information/content of events.
 * 
 * @author jleonard
 */
public class EventReportColumn implements Serializable, Comparable<EventReportColumn> {

	private static final long serialVersionUID = 1L;

	/** the column name */
    private String columnName;
    
    /** the display name of the column */
    private String displayName;
    
    /** whether this column should be in the report */
    private boolean enabled = true;
    
    private ColumnProperties columnProperties;
    
    /** 
	 * column labels
	 * Note: SPSS requires that the first row contain the column headers and the text contain no spaces, use underscores. 
	 */
	public static final String TIME_COL_NAME = "Time";
	public static final String USERNAME_COL_DISPLAY_NAME = "Username";
	public static final String USERNAME_COL_NAME = "Username";
	public static final String USER_ID_COL_DISPLAY_NAME = "User Id";
	public static final String USER_ID_COL_NAME = "User_ID";
	public static final String DS_ID_COL_DISPLAY_NAME = "Domain Session Id";
	public static final String DS_ID_COL_NAME = "ds_ID";
	public static final String DS_TIME_COL_DISPLAY_NAME = "Domain Session Time";
	public static final String DS_TIME_COL_NAME = "DS_Time";
	
	/** display name for the elapsed DKF time column */
	public static final String DKF_TIME_COL_DISPLAY_NAME = "Real Time Assessment Time";
	
	/** internal name for the elapsed DKF time column */
	public static final String DKF_TIME_COL_NAME = "DKF_Time";
	
	public static final String CONTENT_COL_NAME = "Content";
	public static final String EVENT_TYPE_COL_DISPLAY_NAME = "Event Type";
	public static final String EVENT_TYPE_COL_NAME = "Event_Type";
	public static final String PLATFORM_COL_NAME = "Platform";
    public static final String VERSION_COL_NAME = "Version";
    public static final String SCREEN_WIDTH_COL_DISPLAY_NAME = "Screen Width";
    public static final String SCREEN_WIDTH_COL_NAME = "Screen_W";
    public static final String SCREEN_HEIGHT_COL_DISPLAY_NAME = "Screen Height";
    public static final String SCREEN_HEIGHT_COL_NAME = "Screen_H";
    public static final String PARTICIPANT_ID_COL_NAME = "Participant ID";
    public static final String ATTEMPT_COL_NAME = "Attempt";
    public static final String COURSE_ATTEMPT_COL_NAME = "CourseAttempt";
	
	public static final EventReportColumn DS_TIME_COLUMN = new EventReportColumn(DS_TIME_COL_DISPLAY_NAME, DS_TIME_COL_NAME, new TimeProperties());
	public static final EventReportColumn EVENT_TYPE_COLUMN = new EventReportColumn(EVENT_TYPE_COL_DISPLAY_NAME, EVENT_TYPE_COL_NAME);
	
	/** column for elapsed DKF time */
	public static final EventReportColumn DKF_TIME_COLUMN = new EventReportColumn(DKF_TIME_COL_DISPLAY_NAME, DKF_TIME_COL_NAME, new TimeProperties());

	public static final EventReportColumn CONTENT_COLUMN = new EventReportColumn(CONTENT_COL_NAME, CONTENT_COL_NAME);
	public static final EventReportColumn DS_ID_COLUMN = new EventReportColumn(DS_ID_COL_DISPLAY_NAME, DS_ID_COL_NAME);
	public static final EventReportColumn USERNAME_COLUMN = new EventReportColumn(USERNAME_COL_DISPLAY_NAME, USERNAME_COL_NAME);
	public static final EventReportColumn USER_ID_COLUMN = new EventReportColumn(USER_ID_COL_DISPLAY_NAME, USER_ID_COL_NAME);
	public static final EventReportColumn TIME_COLUMN = new EventReportColumn(TIME_COL_NAME, TIME_COL_NAME, new TimeProperties());
	public static final EventReportColumn PLATFORM_COLUMN = new EventReportColumn(PLATFORM_COL_NAME, PLATFORM_COL_NAME);
	public static final EventReportColumn VERSION_COLUMN = new EventReportColumn(VERSION_COL_NAME, VERSION_COL_NAME);
	public static final EventReportColumn SCREEN_WIDTH_COLUMN = new EventReportColumn(SCREEN_WIDTH_COL_DISPLAY_NAME, SCREEN_WIDTH_COL_NAME);
	public static final EventReportColumn SCREEN_HEIGHT_COLUMN = new EventReportColumn(SCREEN_HEIGHT_COL_DISPLAY_NAME, SCREEN_HEIGHT_COL_NAME);
    public static final EventReportColumn ATTEMPT_COL = new EventReportColumn(ATTEMPT_COL_NAME, ATTEMPT_COL_NAME);
    public static final EventReportColumn COURSE_ATTEMPT_COL = new EventReportColumn(COURSE_ATTEMPT_COL_NAME, COURSE_ATTEMPT_COL_NAME);
    public static final EventReportColumn PARTICIPANT_ID_COL = new EventReportColumn(PARTICIPANT_ID_COL_NAME, PARTICIPANT_ID_COL_NAME);
	
    /**
     * Default Constructor
     *
     * Required by IsSerializable to exist and be public
     */
    public EventReportColumn() {
    }
    
    /**
     * Class constructor - set attributes
     *
     * @param columnName - the name of the column that will be used to display
     *        to the user and will also be written out, possibly to disk
     */
    public EventReportColumn(String columnName) {
        
        this(columnName, columnName, null);
    }

    /**
     * Class constructor - set attributes
     * 
     * @param displayName - the display name of the column
     * @param columnName - the column name that will be written out, possibly to disk
     * @param columnProperties The properties of this column 
     */
    public EventReportColumn(String displayName, String columnName, ColumnProperties columnProperties) {
        
        if(columnName == null){
            throw new IllegalArgumentException("The column name can't be null");
        }
        
        this.columnName = columnName;
        
        if(displayName == null){
            throw new IllegalArgumentException("The display name can't be null");
        }
        
        this.displayName = displayName;
        
        this.columnProperties = columnProperties;
    }

    /**
     * Class constructor - set attributes
     *
     * @param displayName - the display name of the column
     * @param columnName - the column name that will be written out, possibly to
     * disk
     */
    public EventReportColumn(String displayName, String columnName) {

        this(displayName, columnName, null);
    }
    
    /**
     * Return the name of the column
     * 
     * @return String
     */
    public String getColumnName() {
        return columnName;
    }
    
    /**
     * Return the display name of the column
     * 
     * @return String
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the properties for this column
     *
     * @return ColumnProperties The properties of this column
     */
    public ColumnProperties getProperties() {

        return columnProperties;
    }
    
    /**
     * Sets time constraints for filtering out events before/after those constraints. Column properties must have {@link TimeProperties}. If not, this method does nothing.
     * @param beginTime the times that before which should be filtered out. Can be null.
     * @param endTime the times that after which should be filtered out. Can be null.
     */
    public void setTimeConstraints(Long beginTime, Long endTime) {
        if(columnProperties instanceof TimeProperties) {
            ((TimeProperties) columnProperties).setFilterBeforeTime(beginTime);
            ((TimeProperties) columnProperties).setFilterAfterTime(endTime);
        }
    }
    
    /**
     * Return whether this column should be in the report
     * @return the default is true
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Set whether this column should be in the report
     * 
     * @param enabled should this column be in the report
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public int compareTo(EventReportColumn other) {             
        return this.getColumnName().compareTo(other.getColumnName());
    }
    
    @Override
    public boolean equals(Object otherEventReportColumn){
        
        boolean equals = false;
        if(otherEventReportColumn instanceof EventReportColumn &&
                ((EventReportColumn)otherEventReportColumn).getColumnName().equals(this.getColumnName())){
            equals = true;
        }
        
        return equals;
    }
    
    @Override
    public int hashCode() {
        int hash = 1;
        hash = hash * 31 + getColumnName().hashCode();        
        return hash;
    }
    
    @Override
    public String toString(){
        
        StringBuilder sb = new StringBuilder();
        sb.append("[EventReportColumn: ");
        sb.append("name = ").append(getColumnName());
        sb.append(", displayName = ").append(getDisplayName()); 
        sb.append(", enabled = ").append(isEnabled());
        sb.append("]");
        return sb.toString();
    }
}
