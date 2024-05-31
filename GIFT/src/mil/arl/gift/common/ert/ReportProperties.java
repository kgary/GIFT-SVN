/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.ert;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mil.arl.gift.common.util.StringUtils;

/**
 * This class contains the properties of an event report to create.
 * 
 * @author jleonard
 */
public class ReportProperties implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/** default file name for the report, usually used when running the legacy ERT */
    public static final String DEFAULT_FILENAME = "GIFT.Report.csv";
    
    public static final String DEFAUL_SETTINGS_FILENAME = "ERT.ReportSettings";
    public static final String DEFAUL_SETTINGS_FILENAME_EXT = ".settings";
    
    /** default value for empty cells in the report (Note: SPSS prefers a period)*/
    public static final String DEFAULT_EMPTY_CELL = ".";
    
    /** the default setting for whether to use question text for the header of a survey response column */
    public static final boolean DEFAULT_USE_QUESTION_TEXT_FOR_HEADER = true;

    /** the unique ids of the event sources (e.g. domain session message log file) used to populate the report */
    private List<Integer> eventSourceIds = new ArrayList<Integer>();
    
    /** 
     * mapping of event types for the report and the display properties - currently just a flag 
     * indicating if the event type is selected to be included in the report 
     */
    private Map<EventType, EventColumnDisplay> eventTypeToDisplayProperty = new HashMap<EventType, EventColumnDisplay>();
    
    /** Map of column names to their properties */
    private Map<EventReportColumn, ColumnProperties> columnToPropertiesMap = new HashMap<EventReportColumn, ColumnProperties>();

    /** the column headers to create in the report */
    private List<EventReportColumn> reportColumns = new ArrayList<EventReportColumn>();
    
    /** the value to use for cells with no data */
    private String emptyCellValue;
    
    /** the file name to use to write the report too */
    private String filename;
    
    /** the location to place the report 
     *  Absolute path or relative to GIFT directory.
     */
    private String outputFolder;
    
    /** if columns lacking data should be excluded from the report */
    private boolean excludeDatalessColumns = false;
    
    /** if duplicate columns created during merge process should be moved to the end of the column list */
    private boolean relocateDuplicateColumns = true;
    
    /** the column whose values to sort rows by */
    private EventReportColumn sortByColumn = null;
    
    /** the column whose values to match and then merge one or more rows by */
    private EventReportColumn mergeByColumn = null;
    
    /** whether to use question text in the column header for survey questions */
    private boolean useSurveyQuestionTextForHeader = DEFAULT_USE_QUESTION_TEXT_FOR_HEADER;
    
    /** the username of the user creating the report */
    private String username;

    /**
     * Default Constructor
     *
     * Required by IsSerializable to exist and be public
     */
    public ReportProperties() {
    }
    
    /**
     * Class constructor - set attributes
     * 
     * @param username - username of the user creating the report, can't be null or empty.
     * @param eventSourceIds - unique ids of possible event sources, can't be empty
     * @param eventTypeOptions - all the events from the event sources
     * @param reportColumns - the columns to include in the report
     * @param emptyCellValue - the default empty cell value for suggestion to the user
     * @param defaultFileName - the default file name for suggestion to the user, can't be null
     */
    public ReportProperties(String username, List<Integer> eventSourceIds, List<EventType> eventTypeOptions, 
            List<EventReportColumn> reportColumns, String emptyCellValue, String defaultFileName) {
        
        if(eventSourceIds.isEmpty()){
            throw new IllegalArgumentException("There must be at least one event source");
        }else if(defaultFileName == null){
            throw new IllegalArgumentException("The default filename can't be null");
        }
        
        this.eventSourceIds.addAll(eventSourceIds);

        for(EventType eType : eventTypeOptions){
            addEventTypeOption(eType);
        }

        this.reportColumns.addAll(reportColumns);
        this.emptyCellValue = emptyCellValue;
        this.filename = defaultFileName;
        
        for(EventReportColumn reportColumn : this.reportColumns) {
            
            if(reportColumn.getProperties() != null) {
                
                ColumnProperties oldVal = columnToPropertiesMap.put(reportColumn, reportColumn.getProperties());
                
                if(oldVal != null) {
                    
                    throw new IllegalArgumentException("The report columns have non-unique headers");
                }
            }
        }
        
        setUserName(username);
    }
    
    /**
     * Set the username of the user creating the report.
     * @param username can't be null or empty.
     */
    private void setUserName(String username) {
        if(StringUtils.isBlank(username)) {
            throw new IllegalArgumentException("The username is null or empty");
        }
        this.username = username;
    }
    
    /**
     * Return the username of the user creating the report
     * @return won't be null or empty.
     */
    public String getUserName() {
        return username;
    }
    
    /**
     * Add the event type to the events being parsed and create a display configuration that
     * can be used to alter the display properties for columns of this event type.
     * 
     * @param eType the event type to add.  If the type is already known, the display properties
     * for that event type will be over-written.
     */
    public void addEventTypeOption(EventType eType){
        eventTypeToDisplayProperty.put(eType, new EventColumnDisplay(eType));
    }

    /**
     * Sets if columns with no data should be excluded from the report
     *
     * @param excludeDatalessColumns If columns with no data should be excluded
     * from the report
     */
    public void setExcludeDatalessColumns(boolean excludeDatalessColumns) {

        this.excludeDatalessColumns = excludeDatalessColumns;
    }
    
    /**
     * Sets if duplicate columns created during the merge process should be relocated to the
     * end of the list of columns in the ERT report output.  This is useful for being able
     * to easily ignore these columns in applications like Excel because they will all be located
     * as the columns at the end.  Duplicate columns are created in the merge process when two or
     * more columns for separate events are merged into the same row.
     * 
     * @param relocateDuplicateColumns if duplicate columns should be relocated
     */
    public void setRelocateDuplicateColumns(boolean relocateDuplicateColumns){
        this.relocateDuplicateColumns = relocateDuplicateColumns;
    }

    /**
     * Return whether or not columns with no data should be excluded from the report
     *
     * @return boolean Returns if columns with no data should be excluded from
     * the report
     */
    public boolean shouldExcludeDatalessColumns() {

        return excludeDatalessColumns;
    }
    
    /**
     * Return if duplicate columns created during the merge process should be relocated to the
     * end of the list of columns in the ERT report output.  This is useful for being able
     * to easily ignore these columns in applications like Excel because they will all be located
     * as the columns at the end.  Duplicate columns are created in the merge process when two or
     * more columns for separate events are merged into the same row.
     * 
     * @return boolean whether or not duplicate columns should be relocated
     */
    public boolean shouldRelocateDuplicateColumns(){
        return relocateDuplicateColumns;
    }

    /**
     * Set the column whose values to sort rows by
     * 
     * @param sortByColumn the column to sort by
     */
    public void setSortByColumn(EventReportColumn sortByColumn){
        this.sortByColumn = sortByColumn;        
    }
    
    /**
     * Return the column whose values to sort rows by
     * 
     * @return sortByColumn
     */
    public EventReportColumn getSortByColumn(){
        return sortByColumn;        
    }
    
    /**
     * Set the column whose values to match and then merge one or more rows by
     * 
     * @param mergeByColumn the column to merge by
     */
    public void setMergeByColumn(EventReportColumn mergeByColumn){
        this.mergeByColumn = mergeByColumn;
    }
    
    /**
     * Return the column whose values to match and then merge one or more rows by
     * 
     * @return mergeByColumn
     */
    public EventReportColumn getMergeByColumn(){
        return mergeByColumn;
    }
    
    /**
     * Set the list of event types to select by default for reporting
     * Note: these event type objects must be the same objects in the event type options
     * 
     * @param defaultSelectedEventTypes the event types to select by default
     */
    public void setDefaultSelectedEventTypes(Collection<EventType> defaultSelectedEventTypes){
        
        if(defaultSelectedEventTypes == null){
            throw new IllegalArgumentException("The default selected event types can't be null.");
        }
        
        for(EventType eTypeToSelect : defaultSelectedEventTypes){
            
            if(eventTypeToDisplayProperty.containsKey(eTypeToSelect)){
                setSelected(eTypeToSelect, true);
            }
        }
    }
    
    /**
     * Set the file name to use to write the report too.  This will over-write the default value
     * suggested by the server.
     * 
     * @param filename - can't be null or empty. E.g. GIFT.Report.csv, GIFT.Report.2022-04-12_09-19-51-mhoffman.zip
     */
    public void setFileName(String filename){
        if(StringUtils.isBlank(filename)) {
            throw new IllegalArgumentException("The filename is null or empty");
        }
        this.filename = filename;
    }
    
    /**
     * Return the full file name to write the report too.
     * 
     * @return the name of the file where the report is.
     */
    public String getFileName(){
        return filename;
    }
    
    /**
     * Return whether the provided event type is selected to be included in the report.
     * 
     * @param eventType the event type to check
     * @return boolean
     */
    public boolean isSelected(EventType eventType){
        return eventTypeToDisplayProperty.get(eventType).isEnabled();
    }
    
    /**
     * Check whether the column specified is one of the columns that will be shown in the
     * report defined by these properties.
     * 
     * @param columnToCheck the column to check if enabled
     * @return true if the column will be included in the report output.
     */
    public boolean isColumnEnabled(EventReportColumn columnToCheck){
        
        int index = reportColumns.indexOf(columnToCheck);
        if(index != -1){
            EventReportColumn foundColumn = reportColumns.get(index);
            return foundColumn.isEnabled();
        }
        
        return false;
    }
    
    /**
     * Set whether the event type is selected to be included in the report.
     * 
     * @param eventType the event type whose selected value is being set
     * @param isSelected whether the event type is selected for inclusion in the report
     */
    public void setSelected(EventType eventType, boolean isSelected){
        eventTypeToDisplayProperty.get(eventType).setEnabled(isSelected);
    }
    
    /**
     * Return the unique ids of the event sources used to populate the report
     * 
     * @return unique ids associated with sources (e.g. domain session message log files) to process
     */
    public List<Integer> getEventSourceIds() {
        return eventSourceIds;
    }

    /**
     * Return the container of event types for the report
     *  
     * @return the event type options for the report
     */
    public Set<EventType> getEventTypeOptions() {
        return eventTypeToDisplayProperty.keySet();
    }
    
    /**
     * Return the display object for the specified event type.
     * 
     * @param eventType - the event type to get display information for
     * @return the display information for the event and any columns
     */
    public EventColumnDisplay getEventTypeDisplay(EventType eventType){
        return eventTypeToDisplayProperty.get(eventType);
    }
    
    /**
     * Return the event type display map.
     * 
     * @return the event type to display mapping
     */
    public Map<EventType, EventColumnDisplay> getEventTypeToDisplay(){
        return eventTypeToDisplayProperty;
    }
    
    /**
     * Return the event type already stored for the event type provided.
     * 
     * @param eventTypeToMatch the event type to search for in this objects known
     * event types being configured and tracked.
     * @return the current event type stored in this class that matches the event type provided.  Null
     * if the event type is not known to this object because {@link #addEventTypeOption(EventType)} has
     * not been called for that event type.
     */
    public EventType getEventType(EventType eventTypeToMatch){
        
        for(EventType eventType : getEventTypeOptions()){
            
            if(eventTypeToMatch.equals(eventType)){
                return eventType;
            }
        }
        
        return null;
    }

    /**
     * Return the column headers to create in the report 
     * 
     * @return the columns to include in the report
     */
    public List<EventReportColumn> getReportColumns() {
        return reportColumns;
    }

    /**
     * Returns the map of columns to properties
     *
     * @return The map of columns to
     * properties
     */
    public Map<EventReportColumn, ColumnProperties> getColumnProperties() {

        return columnToPropertiesMap;
    }

    /**
     * Return the value to use for cells with no data 
     *  
     * @return the string to use for empty cells in the report output 
     */
    public String getEmptyCellValue() {
        return emptyCellValue;
    }
    
    /**
     * Set the value to use for cells with no data
     * 
     * @param emptyCellValue string to use for emtpy cells
     */
    public void setEmptyCellValue(String emptyCellValue) {
        this.emptyCellValue = emptyCellValue;
    }
    
    /**
     * Return the location where the report will be written.
     * 
     * @return absolute path or relative to GIFT folder.  Can be null
     * if default folder is desired.
     */
    public String getOutputFolder() {
        return outputFolder;
    }

    /**
     * Set the location where the report will be written.
     * 
     * @param outputFolder absolute path or relative to GIFT folder.  Can be null
     * if default folder is desired.
     */
    public void setOutputFolder(String outputFolder) {
        this.outputFolder = outputFolder;
    }

    /**
     * Return whether to use question text for the header of a survey response column
     * @return true if question text should be considered for column header
     */
    public boolean shouldUseSurveyQuestionTextForHeader() {
        return useSurveyQuestionTextForHeader;
    }

    /**
     * Set whether to use question text for the header of a survey response column
     * @param useSurveyQuestionTextForHeader the value to use
     */
    public void setUseSurveyQuestionTextForHeader(boolean useSurveyQuestionTextForHeader) {
        this.useSurveyQuestionTextForHeader = useSurveyQuestionTextForHeader;
    }

    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[ReportProperties: ");
        sb.append("username = ").append(username);
        
        sb.append(", eventSourceIds = {");
        for(Integer id : getEventSourceIds()){
            sb.append(id).append(", ");
        }
        sb.append("}");
        
        sb.append(", emptyCellValue = ").append(getEmptyCellValue());
        sb.append(", excludeDatalessColumns = ").append(shouldExcludeDatalessColumns());
        sb.append(", relocateDuplicateColumns = ").append(shouldRelocateDuplicateColumns());
        sb.append(", userSurveyQuestionTextForHeader = ").append(shouldUseSurveyQuestionTextForHeader());
        sb.append(", filename = ").append(getFileName());
        sb.append(", output folder = ").append(getOutputFolder());
        
        if(getMergeByColumn() != null){
            sb.append(", mergeColumn = ").append(getMergeByColumn());
        }
        
        if(getSortByColumn() != null){
            sb.append(", sortByColumn = ").append(getSortByColumn());
        }
        
        sb.append(",\neventTypeOptions = {");
        for(EventType type : getEventTypeOptions()){
            sb.append(type.toString()).append(" isSelected = ").append(eventTypeToDisplayProperty.get(type)).append(",\n");
        }
        sb.append("}");
        
        sb.append(", reportColumns = {");
        for(EventReportColumn column : getReportColumns()){
            sb.append(column == null ? "null" : column.toString()).append(", ");
        }
        sb.append("}");
        sb.append("]");
        return sb.toString();
    }
    
}
