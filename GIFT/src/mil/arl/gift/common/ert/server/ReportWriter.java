/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.ert.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import javax.naming.OperationNotSupportedException;

import mil.arl.gift.common.PackageUtil;
import mil.arl.gift.common.ert.ColumnProperties;
import mil.arl.gift.common.ert.EventColumnDisplay;
import mil.arl.gift.common.ert.EventReportColumn;
import mil.arl.gift.common.ert.EventType;
import mil.arl.gift.common.ert.GenerateReportStatus;
import mil.arl.gift.common.ert.MinMaxProperty;
import mil.arl.gift.common.ert.ReportProperties;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.io.FileFinderUtil;
import mil.arl.gift.common.io.FileUtil;
import mil.arl.gift.common.io.ProgressIndicator;
import mil.arl.gift.common.io.TimeUtil;
import mil.arl.gift.common.io.ZipUtils;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvMapWriter;
import org.supercsv.prefs.CsvPreference;
import org.supercsv.util.CSVContext;

/**
 * Creates the output report file.  The contents are specified by a header and collection or rows.
 * 
 * @author mhoffman
 *
 */
public class ReportWriter {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(ReportWriter.class);
    
    /**
     * the default location where to write the report file(s) too - output folder 
     */
    public static final String DEFAULT_WRITE_LOCATION = PackageUtil.getOutput();    
    
    /** key labels for save/load operations of report properties attributes */
    private static final String EVENT_COLUMNS = "eventColumns";
    private static final String REPORT_COLUMNS = "reportColumns";
    private static final String EXCLUDE_DATALESS_COLUMNS = "excludeDatalessColumns";
    private static final String RELOCATE_DUPLICATE_COLUMNS = "relocateDuplicateColumns";
    private static final String MERGE_BY_COLUMN = "mergeByColumn";
    private static final String SORT_BY_COLUMN = "sortByColumn";
    private static final String EMPTY_CELL_VALUE = "emptyCellValue";
    private static final String REPORT_FILENAME = "reportFileName";

    private static final String SEMI_COLON = ";";
    protected static final String SPACE = " ";
    protected static final String UNDERSCORE = "_";
    protected static final String EMPTY = "";
    private static final String DUPLICATE_COLUMN_PREFIX = "(";
    private static final String DUPLICATE_COLUMN_SUFFIX = ")";
    
    /** 
     * the name of the empty column to use to separate duplicate columns that were relocated to
     * the end of the column list
     */
    private static final String DUPLICATE_COLUMN_SPACER = "DUPLICATE_COL_SPACER";

    /** There are four potential phases of report writing, so arbitrarily assume each one is 20 of the work
     *  The other 20 percent is in merging and sorting as defined below
     */
    private static final int PERCENT_CALCULATION_NUMERATOR = 20;
    
    /** Progress indicator fixed percent values */
    private static final int MERGE_ROW_PERCENT  = 10;
    private static final int SORT_ROW_PERCENT   = 10;
    
    /** 
     * contents of the header for the report
     * Note: the order of the entries correlate to the order in the report
     */
    private LinkedHashSet<EventReportColumn> headerColumns;
    
    /** whether to include the header label values in the report */
    private boolean writeHeader;
    
    /** the rows (i.e. contents) of the report */
    private List<Row> rows = new ArrayList<Row>(); 
    
    /** name of the file being written too */
    private String outputFilename;
    
    /** the value to put in for empty cells, i.e. cells with no data provided */
    private String emptyCellValue = ReportProperties.DEFAULT_EMPTY_CELL;
    
    /** (optional) properties of the report to use when writing output*/
    private ReportProperties reportProperties = null;
    
    /** an indicator of the current progress made in generating the event report */
    private GenerateReportStatus generateReportStatus = new GenerateReportStatus(new ProgressIndicator());

    /** write location -- if not set will default to DEFAULT_WRITE_LOCATION */
    private String writeLocation = null;
    
    /** the directory where this report is located */   
    private File reportDir = null;

    /**
     * Class constructor - set attributes needed to create a report output file.
     * 
     * @param outputFilename the file to write the report too
     * @param headerColumns - the first row of data, i.e. the column labels of the report.  Note: order matters as it pertains to the ordering of the columns in the repot
     * @param writeHeader - whether to include the header label values in the report
     */
    public ReportWriter(String outputFilename, LinkedHashSet<EventReportColumn> headerColumns, boolean writeHeader){
        
        if(outputFilename == null){
            throw new IllegalArgumentException("The output filename can't be null");
        }
        
        this.outputFilename = outputFilename;
        this.headerColumns = headerColumns;
        this.writeHeader = writeHeader;
    }
    
    /**
     * Class constructor - set attributes needed to create a report output file.
     * 
     * @param reportProperties - properties of the report
     * @param writeHeader - whether to include the header label values in the report
     */
    public ReportWriter(ReportProperties reportProperties, boolean writeHeader){
        this(reportProperties.getFileName(), new LinkedHashSet<>(reportProperties.getReportColumns()), writeHeader);

        this.reportProperties = reportProperties;
        
        setWriteLocation(reportProperties.getOutputFolder());
    }
    
    /**
     * Add a header column to the collection known to this writer.
     * Note: this will add the column to the report properties list
     * of report columns to show in the report file.
     * 
     * @param column if a duplicate it will not be added
     */
    public void addHeaderColumn(EventReportColumn column){
        headerColumns.add(column);
        
        // make sure the added header columns will be included in the report output
        // This is important when merging rows because the report columns collection
        // is checked.
        reportProperties.getReportColumns().add(column);
    }
    
    /**
     * Adds the header columns to the collection known to this writer.
     * Note: this will add the columns to the report properties list
     * of report columns to show in the report file.
     * 
     * @param columns duplicates will not be added
     */
    public void addHeaderColumns(Collection<EventReportColumn> columns){
        headerColumns.addAll(columns);
        
        // make sure the added header columns will be included in the report output
        // This is important when merging rows because the report columns collection
        // is checked.
        reportProperties.getReportColumns().addAll(columns);
    }
    
    /**
     * Set the value to put in for empty cells, i.e. cells with no data provided
     * 
     * @param value the string representing an empty cell
     */
    public void setEmptyCellValue(String value){
        emptyCellValue = value;
    }
    
    /**
     * Clear all the rows from the collection of rows this writer could write
     */
    public void clearRows(){
        rows.clear();
    }
    
    /**
     * Add a single row to the collection of rows for the report being created
     * 
     * @param row a new row for the report
     */
    public void addRow(Row row){
        
        if(row == null){
            throw new IllegalArgumentException("The row can't be null.");
        }
        rows.add(row);
    }
    
    /**
     * Add a collection of rows to the collection of rows for the report being created
     * 
     * @param rowsToAdd add a collection of rows to the report
     */
    public void addRows(Collection<Row> rowsToAdd){
        rows.addAll(rowsToAdd);
    }
    
    /**
     * Merge rows based on the values in the provided column.
     * 
     * Rules:
     *  - grab two rows (row 1 and 2) at a time (DONE)
     *  - if row 1 or 2 doesn't contain column, move on (DONE)
     *  - if rows contain different values for column, move on (DONE)
     *  - if merge column is not time column, remove all time columns from resulting row
     *  - if merge column is time column, remove other time columns from resulting row
     *  - add row 2 cells to resulting row (that weren't removed): (DONE)
     *         - if there is a same column name, start counter suffix.  If counter suffix column name, increment counter and repeat check.  When done add cell. (DONE)
     *         - if there is a different column name, just add it (DONE)
     *         
     * @param mergeByColumn - the column whose values to merge rows by
     */
    private void mergeRows(final EventReportColumn mergeByColumn){
        
        if(mergeByColumn != null){
        	
        	//sort rows before the merge to speed up the merging process
        	sortRows(mergeByColumn);
            
        	if(logger.isInfoEnabled()){
        	    logger.info("Merging rows based on values in "+mergeByColumn);
        	}
            
            //flag to indicate if the merge-by column is a time based column
            boolean isTimeBasedColumn = false;
            if(MessageLogEventSourceParser.timeColumns.contains(mergeByColumn)){
                if(logger.isInfoEnabled()){
                    logger.info("The merge-by column is a time based column, removing all other time related columns");
                }
                isTimeBasedColumn = true;
            }else{
                if(logger.isInfoEnabled()){
                    logger.info("The merge-by column is NOT a time based column, therefore removing all time related columns");
                }
            }
            
            int numRowsRemoved;
            Set<Integer> dsIds = new HashSet<>();  //contains the domain session ids merged into a single row 
            for(int i = 0; i < rows.size(); i++){
                //grab root row to merge other rows into
                Row row1 = rows.get(i);
                
                // reset for the current root row, row1, that other rows are being merged into
                Cell dsIdCell = null;
                dsIds.clear();
                
                //see if this row even has a cell with the merge column 
                Cell cell1 = null;
                for(Cell cell : row1.getCells()){
                    
                    // finding the merge-by column
                    if(cell1 == null && cell.getColumn().equals(mergeByColumn)){
                        cell1 = cell;
                    }
                    
                    // finding the domain session id column in order to concatenate domain session ids
                    if(dsIdCell == null && cell.getColumn().equals(EventReportColumn.DS_ID_COLUMN)){
                        dsIdCell = cell;
                        
                        if(dsIds.isEmpty()){
                            // need to populate the domain session ids already in this row
                            
                            String[] ids = cell.getValue().split(SEMI_COLON);
                            for(String id : ids){
                                dsIds.add(Integer.valueOf(id));    
                            }
                        }
                    }
                    
                    if(cell1 != null && dsIdCell != null){
                        break;
                    }
                }
                
                if(cell1 == null){
                    //nothing to do in this row
                    continue;
                }
                
                //reset
                numRowsRemoved = 0;
                
                for(int j = i+1; j < rows.size(); j++){
                    //grab row to merge into root row
                    Row row2 = rows.get(j);
                    
                    //find the cell with the merge column 
                    Cell cell2 = null;
                    for(Cell cell : row2.getCells()){
                        
                        if(cell.getColumn().equals(mergeByColumn)){
                            cell2 = cell;
                            break;
                        }
                    }
                    
                    //case where this row doesn't have the column in it
                    if(cell2 == null){
                        continue;
                    }
                    
                    //case where the values for the column are different - i.e. should only merge where values are the same
                    if(!cell1.getValue().equals(cell2.getValue())){
                        break; //since the rows were sorted, no need to check further
                    }                
                    
                    //
                    // Merge row 1 with row 2
                    //
                    for(Cell cell : row2.getCells()){
                        
                        if(!cell.getColumn().equals(mergeByColumn) && 
                                reportProperties.getReportColumns().contains(cell.getColumn()) &&
                                !MessageLogEventSourceParser.timeColumns.contains(cell.getColumn())){
                            //don't need to merge 'merge-by' column
                        	//add columns that are specified by the report properties (e.g. selected columns in the ERT)
                            //don't add time based columns (they should never be more than 1 instance of each time based column)
                            
                            if(MessageLogEventSourceParser.domainSessionColumns.contains(cell.getColumn())){
                                
                                if(EventReportColumn.DS_ID_COLUMN.equals(cell.getColumn())){
                                    // found a domain session id column in this row,
                                    
                                    if(dsIdCell == null){
                                        // the row1 didn't contain a domain session id cell, but row2 does
                                        dsIdCell = cell;
                                        row1.addCell(cell);
                                        dsIds.add(Integer.valueOf(cell.getValue()));
                                    }else if(!dsIds.contains(Integer.valueOf(cell.getValue()))){
                                        // concatenate ids
                                        dsIdCell.setValue(dsIdCell.getValue() + SEMI_COLON + cell.getValue());
                                        dsIds.add(Integer.valueOf(cell.getValue()));                                        
                                    }
                                }
                            }else{                            
                                row1.addCell(cell);
                            }
                        }
                    }
                    
                    //delete row2 so its not analyzed again
                    rows.remove(row2);
                    j--;  //keep j at same value for next iteration since collection size changed
                    numRowsRemoved++;

                }//end for                
                
                if(numRowsRemoved > 0){
                    //something was merged
                    
                    if(!isTimeBasedColumn){
                        //remove appropriate time related columns from the updated merged row (if any are still there) 
                        //because it doesn't make sense to show the timestamps for 1+ events that happened at 
                        //different times but are now in the same row
                        
                        for(int k = 0; k < row1.cells.size(); k++){
                            
                            if(MessageLogEventSourceParser.timeColumns.contains(row1.cells.get(k).getColumn())){
                                row1.cells.remove(k);
                                k--;  //for next loop
                            }
                        }
                    }
                    
                    if(i > numRowsRemoved){
                        //don't want to go below zero upon next iteration
                        i = i - numRowsRemoved - 1;
                    }else{
                        //start over at zero for next iteration
                        i = -1;
                    }
                }
                
            }//end for
        }//end if
    }
    
    /**
     * Sort the order of the rows to be written 
     * 
     * @param column - the column whose values to sort by
     */
    private void sortRows(final EventReportColumn column){
        
        if(column != null){
            
            if(logger.isInfoEnabled()){
                logger.info("Sorting rows based on values in "+column);
            }
        
            Collections.sort(rows, new Comparator<Row>(){

                @Override
                public int compare(Row row1, Row row2) {
                    
                    //find the cells whose values need to be sorted by
                    Cell cell1 = null, cell2 = null;
                    
                    for(Cell cell : row1.getCells()){
                        
                        if(cell.getColumn().equals(column)){
                            //found cell to sort by
                            cell1 = cell;
                            break;
                        }
                    }
                    
                    for(Cell cell : row2.getCells()){
                        
                        if(cell.getColumn().equals(column)){
                            //found cell to sort by
                            cell2 = cell;
                            break;
                        }
                    }
                    
                    if(cell1 != null && cell2 != null){
                        //sort
                        
                        if(isNumeric(cell1.getValue()) && isNumeric(cell2.getValue())){
                            
                            double value1 = Double.parseDouble(cell1.getValue());
                            double value2 = Double.parseDouble(cell2.getValue());
                            if(value1 < value2){
                                return -1;
                            }else if(value1 == value2){
                                return 0;
                            }else{
                                return 1;
                            }
                            
                        }else{
                            return cell1.getValue().compareTo(cell2.getValue());
                        }
                        
                    }else if(cell2 != null){
                        //row 2 doesn't have the column, place row 1 ahead of it. 
                        return -1;                        
                    }else{
                        //row 1 doesn't have the column, place row 2 ahead of it.
                        return 1;
                    }

                }
                
            });
        }
    }
    
    /**
     * Return whether the string is a number or not.
     * 
     * @param str - the string to check
     * @return boolean - true if the string is a number
     */
    private static boolean isNumeric(String str){        
      return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }
    
    /**
     * This method will test that the file specified by the report is write-able by attempting to write
     * the header values that will be used when the actual report is generated. If this test fails an exception
     * will be thrown which should indicate that the full report should NOT be created at this time.
     * 
     * @throws Exception if there was a severe problem during the write test
     */
    public void writeTest() throws Exception{
        
        if(logger.isInfoEnabled()){
            logger.info("Starting to perform a test write to report file: "+outputFilename);
        }

        createReportDirectory();
        ICsvMapWriter writer = new CsvMapWriter(new FileWriter(reportDir + File.separator + outputFilename), CsvPreference.EXCEL_PREFERENCE);

        try{

            //convert header columns into labels to be used
            List<String> header = new ArrayList<String>(headerColumns.size());
            Iterator<EventReportColumn> headerItr = headerColumns.iterator();
            while(headerItr.hasNext()){
                
                EventReportColumn column = headerItr.next();
                
                //replace spaces with underscores for SPSS compliant column header
                if(logger.isInfoEnabled()){
                    logger.info("Replacing spaces w/ underscores for header: "+column);
                }
                if(column.isEnabled()){
                    header.add(column.getDisplayName().replace(SPACE, UNDERSCORE));
                }
            }
            
            writer.writeHeader(header.toArray(new String[header.size()]));       
        
        }finally {
            writer.close();
        }
    }
    
    /**
     * Creates the directory where this report will be created, if the directory
     * hasn't been created already.
     */
    private void createReportDirectory() {
     
        if(reportDir == null) {
            // create unique directory to place files in
            String location = (writeLocation != null) ? writeLocation : DEFAULT_WRITE_LOCATION;
            reportDir = new File(location + File.separator + UUID.randomUUID());
            reportDir.mkdir();
        }
    }
    
    /**
     * Create the output file for this report
     * 
     * @return the file created that should contain the report, e.g.  
     * @throws Exception if there was a problem writing the report
     */
    public String write() throws Exception{
        
        if(logger.isInfoEnabled()){
            logger.info("Starting to create report file: "+outputFilename);
        }

        generateReportStatus.getProgress().setPercentComplete(0);
        
        createReportDirectory();
        
        //flag used to indicate whether a duplicate column was created during merging and therefore
        //some information should be presented to the user after the report is finished
        boolean createdDuplicateColumns = false;
        
        List<File> filesToZip = new ArrayList<>();
        
        // create report file
        File reportFile = new File(reportDir + File.separator + outputFilename);
        createdDuplicateColumns = writeReportFile(reportFile);
        filesToZip.add(reportFile);
        
        // create report properties files
        File reportPropFile = new File(reportDir + File.separator + "ReportProperties.settings");
        saveReportProperties(reportPropFile, reportProperties);
        filesToZip.add(reportPropFile);

        // zip it up
        File zipFile = new File(reportDir.getParentFile() + File.separator + "GIFT.Report."+TimeUtil.formatTimeLogFilename(System.currentTimeMillis())+"-"+reportProperties.getUserName()+".zip");
        ZipUtils.zipFiles(filesToZip, zipFile);
        
        // provide the relative path from the hosted folder, the root location
        reportProperties.setFileName(FileFinderUtil.getRelativePath(reportDir.getParentFile(), zipFile));
        
        // delete files that were put in the zip for cleanup purposes (the zip will be deleted by other logic)
        // Best effort.
        try{
            FileUtil.deleteDirectory(reportDir);
        }catch(Throwable t) {
            logger.error("Failed to delete report directory ("+reportDir+") where the report zip was created ("+zipFile+").", t);
        }

        
        //
        // Add finished message details
        //
        StringBuffer sb = new StringBuffer();
        if(createdDuplicateColumns){
            sb.append("> Additional columns were added during the merge process and are identified by the suffix of '").append(DUPLICATE_COLUMN_PREFIX).append("#").append(DUPLICATE_COLUMN_SUFFIX).append("' in the column name.");
            if(reportProperties.shouldRelocateDuplicateColumns()){
                sb.append(".  These duplicate columns have been moved to the end of the column list and are separated from the rest of the columns by an empty column with the name of ").append(DUPLICATE_COLUMN_SPACER).append(".");
            }
        }

        generateReportStatus.getProgress().setPercentComplete(100);
        generateReportStatus.setFinishedAdditionalDetails(sb.toString());
        
        if(logger.isInfoEnabled()){
            logger.info("Finished report file");
        }
        
        return reportProperties.getFileName();
    }
    
    /**
     * Populate the provided file with the events and columns specified by the report properties.
     * @param reportFile the file to populate, can't be null, doesn't have to exist yet but should be able to create it.
     * @return true if the report file contains duplicated columns, i.e. columns that couldn't be merged
     * @throws IOException if there was a problem writing the file
     * @throws OperationNotSupportedException if there was a problem with any of the columns/rows
     */
    private boolean writeReportFile(File reportFile) throws IOException, OperationNotSupportedException {
        
        FileWriter fileWriter = new FileWriter(reportFile, false);
        ICsvMapWriter writer = new CsvMapWriter(fileWriter, CsvPreference.EXCEL_PREFERENCE);
        
        boolean createdDuplicateColumns = false;
        
        try {
            
            //convert header columns into labels to be used
            List<String> header = new ArrayList<String>(headerColumns.size());
            Iterator<EventReportColumn> headerItr = headerColumns.iterator();
            while(headerItr.hasNext()){
                
                EventReportColumn column = headerItr.next();
                
                //replace spaces with underscores for SPSS compliant column header
                if(logger.isInfoEnabled()){
                    logger.info("Replacing spaces w/ underscores for header: "+column);
                }
                if(column.isEnabled()){
                    header.add(column.getDisplayName().replace(SPACE, UNDERSCORE));
                }
            }

            //Calculate an arbitrary percent to use to increase the progress by as each row is handled
            //float percentProgressPerRow = PERCENT_CALCULATION_NUMERATOR/(float) rows.size();

            if (reportProperties != null) {

                //
                // Remove any rows that violate rules of column properties
                //
                boolean removeRow;

                // Update progress bar from 0-PERCENT_CALCULATIONG_NUMERATOR within this section using below description
                generateReportStatus.getProgress().setTaskDescription("Removing rows that violate any filters you provided.");
                int rowsProcessed = 0;
                int rowTotal = rows.size();

                int numRowsRemoved = 0;

                for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
                    rowsProcessed++;

                    Row row = rows.get(rowIndex);
                    removeRow = false;

                    for (Cell cell : row.getCells()) {

                        ColumnProperties properties = reportProperties.getColumnProperties().get(cell.getColumn());

                        if (properties != null) {

                            if (properties instanceof MinMaxProperty) {

                                MinMaxProperty minMaxProperty = (MinMaxProperty) properties;
                                Long min = minMaxProperty.getMinimum();
                                Long max = minMaxProperty.getMaximum();
                                
                                Long value;
                                
                                if(cell.getValue().contains(Constants.PERIOD)) {
                                    
                                    Double val = Double.parseDouble(cell.getValue()) * 1000;
                                    
                                    value = val.longValue();
                                    
                                } else {
                                    
                                    value = Long.parseLong(cell.getValue());                                    
                                }
                                
                                if (min != null && value < min) {
                                    //value is less than min
                                    removeRow = true;
                                    break;
                                } else if (max != null && value > max) {
                                    //value is greater than max
                                    removeRow = true;
                                    break;
                                }

                            } else {
                                throw new OperationNotSupportedException("Found unhandled column property of " + properties + " for cell of " + cell);
                            }
                        }
                    }

                    if (removeRow) {
                        rows.remove(rowIndex);
                        numRowsRemoved++;
                        rowIndex--;
                    }
                    generateReportStatus.getProgress().setPercentComplete((PERCENT_CALCULATION_NUMERATOR * rowsProcessed)/rowTotal);
                }

                if(logger.isInfoEnabled()){
                    logger.info("Removed " + numRowsRemoved + " row(s) from the report based on default column properties.");
                }

                //merge rows based on user provided choice
                generateReportStatus.getProgress().setTaskDescription("Merging rows");
                
                // when the merge by column is user id and there are participant ids found in the data set
                // use participant id as the merge by column instead of user id since the user ids are most likely
                // auto generated.  Participant ids allows a users multiple attempts to be merged into a single row.
                if(reportProperties.getMergeByColumn() != null && reportProperties.getMergeByColumn().equals(EventReportColumn.USER_ID_COLUMN) &&
                        reportProperties.isColumnEnabled(EventReportColumn.PARTICIPANT_ID_COL)){
                    reportProperties.setMergeByColumn(EventReportColumn.PARTICIPANT_ID_COL);
                }
                mergeRows(reportProperties.getMergeByColumn());
                generateReportStatus.getProgress().setPercentComplete(PERCENT_CALCULATION_NUMERATOR+MERGE_ROW_PERCENT);


                //sort rows based on user provided choice
                generateReportStatus.getProgress().setTaskDescription("Sorting rows");
                sortRows(reportProperties.getSortByColumn());
                generateReportStatus.getProgress().setPercentComplete(PERCENT_CALCULATION_NUMERATOR+MERGE_ROW_PERCENT+SORT_ROW_PERCENT);
            }

            //
            // pre-process merge to handle column name collisions and to write the full header before any data rows
            //
            HashMap<String, ? super Object> colValueChecker = new HashMap<String, Object>();
            HashMap<Cell, String> cellToColumn = new HashMap<Cell, String>();

            //Calculate an arbitrary percent to use to increase the progress by as each row is handled
            //percentProgressPerRow = PERCENT_CALCULATION_NUMERATOR/(float) rows.size();

            // go update progress from progressStart to progresStart + PERCENT_CALCULATION_NUMERATOR in next section
            int progresStart = PERCENT_CALCULATION_NUMERATOR+MERGE_ROW_PERCENT+SORT_ROW_PERCENT;
            generateReportStatus.getProgress().setTaskDescription("Handling cell collisions");
            generateReportStatus.getProgress().setPercentComplete(progresStart);
            int rowsProcessed = 0;
            int rowTotal = rows.size();
            for(Row row : rows){
                rowsProcessed++;

                for(Cell cell : row.getCells()){

                    String column = cell.getColumn().getDisplayName().replace(SPACE, UNDERSCORE);
                    if(colValueChecker.containsKey(column)){
                        
                        String newColName;
                        int suffixCnt = 2;
                        while(true){
                            
                            newColName = column + DUPLICATE_COLUMN_PREFIX + suffixCnt+ DUPLICATE_COLUMN_SUFFIX;
                            if(!colValueChecker.containsKey(newColName)){
                                break;
                            }
                            
                            suffixCnt++;
                        }
                        
                        if(header.contains(newColName)){
                            //a previous row already added this column name, so just use it
                            
                            //make sure to update the column value checker in case there is another instance of this column name
                            colValueChecker.put(newColName, cell.getValue());
                            
                            cellToColumn.put(cell, newColName);
                            continue;
                        }
                        
                        //find collision column index to insert after
                        boolean added = false;
                        for(int i = 0; i < header.size(); i++){
                            
                            if(header.get(i).equals(column)){
                                //found index to insert after
                                
                                if((i+(suffixCnt-1)) >= header.size()){
                                    //add to end of column list
                                    header.add(newColName);
                                }else{
                                    //insert after collision column plus the suffix offset
                                    header.add(i+suffixCnt-1, newColName);
                                }
                                colValueChecker.put(newColName, cell.getValue());
                                cellToColumn.put(cell, newColName);
                                added = true;
                                break;
                            }
                        }
                        
                        if(!added){
                            throw new OperationNotSupportedException("Unable to merge two cells trying to be placed in the same column in the same row.  The column name is "+column+".");
                        }
                        
                        createdDuplicateColumns = true;
                        
                    }else{
                        colValueChecker.put(column, cell.getValue());
                        cellToColumn.put(cell, column);
                    }
                    
                    
                }//end inner for
                
                //reset for next row
                colValueChecker.clear();

                generateReportStatus.getProgress().setPercentComplete(progresStart+(PERCENT_CALCULATION_NUMERATOR * rowsProcessed)/rowTotal);
            }//end outer for
            progresStart+= PERCENT_CALCULATION_NUMERATOR;
            generateReportStatus.getProgress().setPercentComplete(progresStart);

            //
            // Move duplicate columns created during merge to end of original columns
            // Note: this is done after the above column rename logic in order to keep the duplicate
            //       columns together and in the same order as the collision columns appear in the header
            //
            if(reportProperties.shouldRelocateDuplicateColumns()){
                
                List<String> relocatedHeader = new ArrayList<String>();
                for(int index = 0; index < header.size(); index++){
                    
                    String colName = header.get(index);
                    
                    if(isDuplicateColumn(colName)){
                        //found duplicate column at this index, move it to the end
                        
                        relocatedHeader.add(header.remove(index));
                        index--;
                    }
                }
                
                //update header reference
                header.addAll(relocatedHeader);
            }
            
            //
            // Remove columns with no values
            //
            //TODO: if the report writing becomes slow, try to cache the columns with data in a previous report writing step so we don't have to loop so much here.
            
            if(reportProperties.shouldExcludeDatalessColumns()){
                boolean empty;

                generateReportStatus.getProgress().setTaskDescription("Removing dataless columns");
                //float percentProgressPerHeader = PERCENT_CALCULATION_NUMERATOR/(float) header.size();
                
                //to improve performance keep list of other headers found to have data while dealing with the 
                //current header
                Set<String> otherColumnsWithData = new HashSet<String>();



                int headersProcessed = 0;
                int headerTotal = header.size();
                for(int headerIndex = 0; headerIndex < header.size(); headerIndex++){
                    headersProcessed++;

                    generateReportStatus.getProgress().setPercentComplete(progresStart+(PERCENT_CALCULATION_NUMERATOR * headersProcessed)/headerTotal);
                    
                    String columnName = header.get(headerIndex);
                    
                    //check if column was already found to have at least one cell with data
                    empty = !otherColumnsWithData.contains(columnName);
                    
                    if(empty){
                        //check every cell until you find one in the current column being checked
                        
                        for(String cellColumnName : cellToColumn.values()){
                            
                            if(columnName.equals(cellColumnName)){
                                empty = false;
                                break;
                            }else if(!otherColumnsWithData.contains(cellColumnName)){
                                //add to list for use in a future iteration of the outer for loop
                                otherColumnsWithData.add(cellColumnName);
                            }
                        }
                        
                        //if still unable to find a single cell with a value under that column header, remove that column
                        if(empty){
                            header.remove(headerIndex);
                            if(logger.isDebugEnabled()){
                                logger.debug("Removed column with header: " + columnName);
                            }
                            headerIndex--;
                        }
                    }
                }
            }

            progresStart+= PERCENT_CALCULATION_NUMERATOR;
            generateReportStatus.getProgress().setPercentComplete(progresStart);

            if(reportProperties.shouldRelocateDuplicateColumns() && createdDuplicateColumns){
                //separate the duplicate columns that were moved to the end of the column list with
                //an empty column to clearly identify those columns from the rest of the columns
                
                for(int index = 0; index < header.size(); index++){
                    
                    String columnName = header.get(index);
                    if(isDuplicateColumn(columnName)){
                        header.add(index, DUPLICATE_COLUMN_SPACER);
                        break;
                    }
                }
                
            }
            
            if(writeHeader){
                if(header.isEmpty()){
                    logger.warn("There are no columns to print based on the report settings.");
                    return createdDuplicateColumns;
                } else {
                    writer.writeHeader(header.toArray(new String[header.size()]));
                }
            }
            
            //for now... convert all null or empty string entries to empty cell value
            //Note: the processor length must match the header length, therefore if more than one processor is needed
            //      you will need to create a custom processor instance
            List<CellProcessor> processor = new ArrayList<CellProcessor>(header.size());
            CellProcessor cellProcessor = new CellProcessor() {
                
                @Override
                public Object execute(Object value, CSVContext context) {
                    
                    if(value == null || value.equals(EMPTY)){
                        return emptyCellValue;
                    }else if(value instanceof String && ((String)value).contains(Constants.NEWLINE)){
                        //don't want new lines used for readability in user interfaces to be applied
                        //to the report file being created
                        return ((String)value).replace(Constants.NEWLINE, "");
                    }else{
                        return value;
                    }
                }
            };
            
            for(int i = 0; i < header.size(); i++){
                processor.add(cellProcessor);
            }
            
            //gather and write each row of data
            HashMap<String, ? super Object> data = new HashMap<String, Object>();
            generateReportStatus.getProgress().setTaskDescription("Writing data to the report file.");

            rowsProcessed = 0;
            rowTotal = rows.size();
            for(Row row : rows){
                for(Cell cell : row.getCells()){
                    
                    if(!cellToColumn.containsKey(cell)){
                        logger.error("Unable to find the appropriate column for cell = "+cell);
                    }else{
                        data.put(cellToColumn.get(cell), cell.getValue());
                    }

                }//end inner for
                
                try{
                    writer.write(data, header.toArray(new String[header.size()]), processor.toArray(new CellProcessor[processor.size()]));
                }catch(IOException ioe){
                    //treat IO exception as a critical error, stop writing 
                    logger.error("There was an exception while writing report for data = "+data+" and header = "+header+". Terminating report creation.", ioe);
                    return createdDuplicateColumns;
                }catch(Exception e){
                    //possibly something wrong with the current data collection, continue writing
                    logger.error("There was an exception while writing report for data = "+data+" and header = "+header+". Continuing report creation.", e);
                }
                
                //reset for the next row of data
                data.clear();
                generateReportStatus.getProgress().setPercentComplete(progresStart+(PERCENT_CALCULATION_NUMERATOR * rowsProcessed)/rowTotal);
            }//end for    

        } finally {
            writer.close();
            fileWriter.close();
        }
        
        return createdDuplicateColumns;
    }
    
    /**
     * Return whether the column name provided represents a duplicate column name created
     * during the merge process to handle an event collision being represented on the same row.
     * 
     * @param columnName the column name to check
     * @return boolean whether or not the column name represents a duplicate column 
     */
    private boolean isDuplicateColumn(String columnName){
        
        //simple check, hopefully don't need something more sophisticated than this
        if(columnName != null && columnName.endsWith(DUPLICATE_COLUMN_SUFFIX) && columnName.contains(DUPLICATE_COLUMN_PREFIX)){
            return true;
        }
        
       return false;
    }
    
    /**
     * Sets the progress indicator of this report writer
     * 
     * @param progInd The progress indicator to which this report writer should be set to use.
     */
    public void setProgressIndicator(GenerateReportStatus progInd){
        generateReportStatus = progInd;
    }

    /**
     * sets the file write location
     *
     * @param writeLocation The location (directory) to which the report file should be written
     *
     */
    public void setWriteLocation(String writeLocation) {this.writeLocation = writeLocation;}
    
    /**
     * Save the report properties to the file specified.  
     * 
     * @param file - the file to write content too, e.g. E:\work\GIFT\ARL SVN\trunk\GIFT\..\Domain\Exports\fc84feca-ab3b-4f16-b5b8-3d1e0bf13a67\ReportProperties.settings
     * @param reportProperties The properties of the report to save
     * @throws Exception - can throw various I/O exceptions during file write operations
     */
    @SuppressWarnings("unchecked")
    public static void saveReportProperties(File file, ReportProperties reportProperties) throws Exception{
        
        Properties prop = new Properties();
        
        Set<EventType> eventTypes = reportProperties.getEventTypeOptions();
        
        //
        //Report columns - i.e. the default columns (plus ordering)
        //
        List<EventReportColumn> reportColumns = reportProperties.getReportColumns();
        reportColumns.removeAll(Collections.singletonList(null));
        JSONArray reportColArray = new JSONArray();
        for(EventReportColumn col : reportColumns){
            
            JSONObject colObj = new JSONObject();
            EventReportColumnCodec.encode(colObj, col);
            
            reportColArray.add(colObj);
        }
        
        prop.setProperty(REPORT_COLUMNS, reportColArray.toString());
        
        //
        // Event columns
        //
        JSONArray eventsArray = new JSONArray();
        for(EventType eType : eventTypes){
            
            EventColumnDisplay display = reportProperties.getEventTypeDisplay(eType);
            JSONObject eventObj = new JSONObject();
            EventColumnDisplayCodec.encode(eventObj, display);
            
            eventsArray.add(eventObj);
        }

        prop.setProperty(EVENT_COLUMNS, eventsArray.toString());

        prop.setProperty(EXCLUDE_DATALESS_COLUMNS, Boolean.toString(reportProperties.shouldExcludeDatalessColumns()));
        
        prop.setProperty(RELOCATE_DUPLICATE_COLUMNS,  Boolean.toString(reportProperties.shouldRelocateDuplicateColumns()));
        
        prop.setProperty(REPORT_FILENAME, reportProperties.getFileName());
        
        //Merge-by column (optional) 
        if(reportProperties.getMergeByColumn() != null){
            JSONObject mergeObj = new JSONObject();
            EventReportColumnCodec.encode(mergeObj, reportProperties.getMergeByColumn());
            prop.setProperty(MERGE_BY_COLUMN, mergeObj.toString());
        }
        
        //Sort-by column (optional)
        if(reportProperties.getSortByColumn() != null){
            JSONObject sortObj = new JSONObject();
            EventReportColumnCodec.encode(sortObj, reportProperties.getSortByColumn());
            prop.setProperty(SORT_BY_COLUMN, sortObj.toString());
        }
        
        //empty cell 
        prop.setProperty(EMPTY_CELL_VALUE, reportProperties.getEmptyCellValue());
        
        FileOutputStream out = new FileOutputStream(file);
        prop.store(out, "This file contains ERT report settings.");
        out.close();
    }
    
    
    /**
     * Load the report properties from the file specified.
     * 
     * @param propertyFile - the file to load properties from.
     * @param reportProperties The properties of the current file loaded
     * @return ReportProperties The new properties of report to generate
     * @throws Exception - can throw various I/O exceptions during file read operations
     */
    public static ReportProperties loadReportProperties(File propertyFile, ReportProperties reportProperties) throws Exception{
        
        if(logger.isInfoEnabled()) {
            logger.info("Starting to load settings from "+propertyFile);
        }
        
        FileInputStream input = new FileInputStream(propertyFile);
        Properties prop = new Properties();
        prop.load(input);
        input.close();
        
        //
        // Report columns
        //
        List<EventReportColumn> cols = reportProperties.getReportColumns();
        cols.clear(); //only report those that were saved
        
        String rawReportColumnsStr = prop.getProperty(REPORT_COLUMNS);
        JSONArray rootReportColArray = (JSONArray) new JSONParser().parse(rawReportColumnsStr);
        for(Object obj : rootReportColArray){
            JSONObject jObj = (JSONObject)obj;
            EventReportColumn col = EventReportColumnCodec.decode(jObj);
            cols.add(col);
        }
        
        reportProperties.getColumnProperties().clear();

        for (EventReportColumn reportColumn : cols) {

            if (reportColumn.getProperties() != null) {

                ColumnProperties oldVal = reportProperties.getColumnProperties().put(reportColumn, reportColumn.getProperties());

                if (oldVal != null) {

                    throw new IllegalArgumentException("The report columns have non-unique headers");
                }
            }
        }
        
        //
        //Event types and columns
        //
        Map<EventType, EventColumnDisplay> newEventMap = new HashMap<EventType, EventColumnDisplay>();
        Map<EventType, EventColumnDisplay> origEventMap = reportProperties.getEventTypeToDisplay();
        String rawEventColumnsStr = prop.getProperty(EVENT_COLUMNS);
        JSONArray rootEventColArray = (JSONArray) new JSONParser().parse(rawEventColumnsStr);
        for(Object obj : rootEventColArray){
            JSONObject jObj = (JSONObject)obj;
            EventColumnDisplay colDisplay = EventColumnDisplayCodec.decode(jObj);
            
            //brute force search in case the event ids are different
            boolean updated = false;
            for(EventType eType : origEventMap.keySet()){
                
                if(eType.getName().equals(colDisplay.getEventType().getName())){
                    //set display settings
                    newEventMap.put(eType, colDisplay);
                    updated = true;
                    break;
                }
            }
            
            if(!updated){
                logger.error("Unable to update/merge display settings for "+colDisplay);
            }
        }
        
        // Update the report properties event type display properties which could have been
        // changed because the event was in the settings file or if not in the settings file
        // the event display settings need to be changed to disabled so the event isn't included
        // automatically in the report
        for(EventType origEvent : origEventMap.keySet()){
            
            if(newEventMap.containsKey(origEvent)){
                //updated
                origEventMap.put(origEvent, newEventMap.get(origEvent));
            }else{
                //not updated, therefore disable
                EventColumnDisplay display = origEventMap.get(origEvent);
                display.setEnabled(false);
            }
        }
     
        
        //empty cell
        String emptyCellValue = prop.getProperty(EMPTY_CELL_VALUE);
        reportProperties.setEmptyCellValue(emptyCellValue);
        
        //report filename
        String reportFilename = prop.getProperty(REPORT_FILENAME);
        reportProperties.setFileName(reportFilename);
   
        if (prop.getProperty(EXCLUDE_DATALESS_COLUMNS) != null) {
            
            String stringValue = prop.getProperty(EXCLUDE_DATALESS_COLUMNS);
            
            reportProperties.setExcludeDatalessColumns(Boolean.parseBoolean(stringValue));
        }
        
        if(prop.getProperty(RELOCATE_DUPLICATE_COLUMNS) != null){
            
            String stringValue = prop.getProperty(RELOCATE_DUPLICATE_COLUMNS);
            
            reportProperties.setRelocateDuplicateColumns(Boolean.parseBoolean(stringValue));
        }
        
        //Merge-by column
        if(prop.getProperty(MERGE_BY_COLUMN) != null){
            JSONObject mergeObj = (JSONObject) new JSONParser().parse(prop.getProperty(MERGE_BY_COLUMN));
            EventReportColumn col = EventReportColumnCodec.decode(mergeObj);
            reportProperties.setMergeByColumn(col);
        }
        
        //Sort-by column
        if(prop.getProperty(SORT_BY_COLUMN) != null){
            JSONObject sortObj = (JSONObject) new JSONParser().parse(prop.getProperty(SORT_BY_COLUMN));
            EventReportColumn col = EventReportColumnCodec.decode(sortObj);
            reportProperties.setSortByColumn(col);
        }
        
        if(logger.isDebugEnabled()) {
            logger.debug("After loading settings from "+propertyFile+", the report properties looks like = "+reportProperties);
        }

        return reportProperties;
    }
}
