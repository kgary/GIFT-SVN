/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.ert.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.time.FastDateFormat;
import org.apache.log4j.Level;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.DomainSession;
import mil.arl.gift.common.PackageUtil;
import mil.arl.gift.common.ert.EventReportColumn;
import mil.arl.gift.common.ert.EventType;
import mil.arl.gift.common.ert.GenerateReportStatus;
import mil.arl.gift.common.ert.ReportProperties;
import mil.arl.gift.common.logger.BookmarkReader;
import mil.arl.gift.sensor.writer.AbstractFileWriter;
import mil.arl.gift.tools.ert.shared.EventSourceTreeNode;
import mil.arl.gift.tools.ert.shared.EventSourcesTreeModel;
import mil.arl.gift.ums.logger.DomainSessionLogger;

/**
 * Gathers GIFT data files by domain session and then executes ERT report generation logic.
 *  
 * @author mhoffman
 *
 */
public class BatchReportUtil {
    
    static {
        //use ert log4j
        PropertyConfigurator.configureAndWatch(PackageUtil.getConfiguration() + File.separator + "tools" + File.separator + "ert" + File.separator + "log4j.properties");
    }
    
    /** instance of the logger */
    private static final Logger logger = LoggerFactory.getLogger(BatchReportUtil.class);
    
    private static final File DEFAULT_CONFIG = new File("config" + File.separator + "tools" + File.separator + "ert" + File.separator + "batchReportUtil.config");
    
    /** Date formatter used for batch report folder names. NOTE: null Locale and Timezone arguments make the formatter use the system defaults. */
    private static final FastDateFormat FolderNameFormat = FastDateFormat.getInstance("HH-mm-ss_MM-dd-yyyy", null, null);
    
    /**
     * Batch report configuration properties
     */
    private static final String DATA_FOLDER_PROPERTY = "dataFolder";
    private static final String PROPERTIES_FILE_PROPERTY = "propertiesFile";
    private static final String OUTPUT_FOLDER_PROPERTY = "reportOutputFolder";
    
    /** folder where the GIFT data resides */
    private File dataFolder = null; 
    
    /** the report configuration file */
    private File reportPropFile = null;
    
    /** folder where the ERT batch report output files will be created */
    private File outputFolder = null;
    
    /** 
     * flag used to indicate if the batch report util has been properly configured 
     * before attempting to group data sets and execute ERT report logic 
     */
    private boolean configured = false;
    
    /**
     * flag used to determine if messages should be printed to the console as well as being logged
     */
    private boolean consoleOutput = true;
    
    /**
     * Set attribute
     * 
     * @param consoleOutput flag used to determine if messages should be printed to the console as well as being logged
     */
    private BatchReportUtil(boolean consoleOutput){ 
        
        this.consoleOutput = consoleOutput;
    }
    
    /**
     * Writes the message to one or more output streams (e.g. console, log4j).
     * 
     * @param message the message to show
     * @param level the level of logging the message is associated with (e.g. Info)
     * @param t an exception to output.  Can be null.
     */
    private void outputStatus(String message, Level level, Throwable t){
        
        if(consoleOutput){
            System.out.println(message);
            
            if(t != null){
                t.printStackTrace();
            }
        }

        if(level == Level.ERROR){
            logger.error(message, t);
        }else if(level == Level.WARN){
            logger.warn(message, t);
        }else if(level == Level.INFO){
            logger.info(message, t);
        }else if(level == Level.DEBUG){
            logger.debug(message, t);
        }else if(level == Level.TRACE){
            logger.trace(message, t);
        }else{
            logger.warn(message, t);
        }

    }
    
    /**
     * Configure the settings for the batch report utility by reading the configuration file properties.
     * 
     * @param batchConfigFile contains settings for the batch report utility.  The file must exist.
     * @throws Exception if there was a problem reading the file or setting the necessary properties.
     */
    private void configure(File batchConfigFile) throws Exception{
        
        if(batchConfigFile == null){
            throw new IllegalArgumentException("The batch configuration file can't be null.");
        }else if(!batchConfigFile.exists()){
            throw new IllegalArgumentException("The batch configuration file '"+batchConfigFile+"' doesn't exist.");
        }
        
        Properties properties = new Properties();
        try{
            properties.load(new FileInputStream(batchConfigFile));
        }catch(IOException e){
            throw new Exception("There was an issue reading the batch configuration file '"+batchConfigFile+"'.", e);
        }
        
        String dataFolderPath = properties.getProperty(DATA_FOLDER_PROPERTY);
        String reportPropFilePath = properties.getProperty(PROPERTIES_FILE_PROPERTY);
        String outputFolderPath = properties.getProperty(OUTPUT_FOLDER_PROPERTY);
        
        if(dataFolderPath == null || dataFolderPath.isEmpty()){
            throw new Exception("The data folder was not specified in the batch configuration file '"+batchConfigFile+"'.");
        }else if(reportPropFilePath == null || reportPropFilePath.isEmpty()){
            throw new Exception("The report properties file was not specified in the batch configuration file '"+batchConfigFile+"'.");
        }
        
        dataFolder = new File(dataFolderPath);
        if(!dataFolder.exists()){
            throw new Exception("The data folder '"+dataFolder+"' doesn't exist.");
        }else if(!dataFolder.isDirectory()){
            throw new Exception("The data folder '"+dataFolder+"' must be a directory.");
        }
        
        reportPropFile = new File(reportPropFilePath);
        if(!reportPropFile.exists()){
            throw new Exception("The report properties file '"+reportPropFile+"' doesn't exist.");
        }else if(!reportPropFile.isFile()){
            throw new Exception("The report properties file '"+reportPropFile+"' must be a file not a directory.");
        }
        
        if(outputFolderPath != null && !outputFolderPath.isEmpty()){
            outputFolder = new File(outputFolderPath);
            if(!outputFolder.exists()){
                outputFolder.mkdirs();
                outputStatus("Created the output directory of '"+outputFolder.getAbsolutePath()+"'.", Level.INFO, null);
            }else if(!outputFolder.isDirectory()){
                throw new Exception("The output folder '"+outputFolder+"' must be a directory.");
            }
        }
        
        configured = true;
    }
    
    /**
     * Search for and group the GIFT data by domain session.  Then execute an ERT report
     * for each domain session.
     *  
     * @throws Exception if there was a problem generating a report.
     */
    private void process() throws Exception{
        
        if(!configured){
            throw new Exception("Unable to run the batch report process because configure hasn't been called yet.");
        }   
        
        outputStatus("starting at : "+Calendar.getInstance().getTime(), Level.INFO, null);

        
        outputStatus("START: searching for GIFT data files...\n", Level.INFO, null);
        ErtRpcServiceImpl ertService = new ErtRpcServiceImpl(dataFolder);
        outputStatus("FINISHED: searching for GIFT data files\n", Level.INFO, null);
        
        EventSourcesTreeModel eventSourcesTreeModel = ertService.getEventSources(false);
        Map<Integer, DomainSessionData> dsIdToData = new HashMap<Integer, DomainSessionData>();
        outputStatus("EventSourceTree: " + eventSourcesTreeModel.getNode(0).getChildren().toString(),Level.INFO,null);
        
        outputStatus("START: grouping data files by domain session...\n", Level.INFO, null);
        groupData(eventSourcesTreeModel.getRootNodes(), dsIdToData, ertService);
        outputStatus("FINISHED: grouping data files by domain session...\n", Level.INFO, null);
        
        outputStatus("dsIdToData: " + dsIdToData.toString(),Level.INFO,null);
        
        if(dsIdToData.isEmpty()){
            throw new Exception("Failed to find any GIFT domain session message log files or sensor writer files under '"+dataFolder+"'.");
        }
        
        //
        // create directory to place reports
        //
        File batchReportDirectory = null;
        String newFolderName = FolderNameFormat.format(Calendar.getInstance().getTime());
        if(outputFolder == null){
            //use default - relative to GIFT/output
            batchReportDirectory = new File("output" + File.separator + newFolderName);
        }else{
            batchReportDirectory = new File(outputFolder.getAbsolutePath() + File.separator + newFolderName);
        }
        
        batchReportDirectory.mkdir();
        
        outputStatus("START: creating ERT report(s) in "+batchReportDirectory+"\n", Level.INFO, null);

        //run report for each domain session
        for(DomainSessionData data : dsIdToData.values()){
            
            //set selected event sources
            List<Integer> eventSourceIds = new ArrayList<Integer>();
            
            if(data.msgLogFileNodeId != -1){
                eventSourceIds.add(data.msgLogFileNodeId);
            }
            
            eventSourceIds.addAll(data.sensorFileNodeIds);
            
            if(data.bookmarkNodeId != -1){
                eventSourceIds.add(data.bookmarkNodeId);
            }
            
            if(eventSourceIds.isEmpty()){
                //ERROR
                outputStatus("ERROR: there are no data files for "+data, Level.ERROR, null);
            }
            
            //populate the report properties with columns found in the event source(s)
            ReportProperties eventSourcesReportProperties = ertService.selectEventSource(eventSourceIds, null);    
            
            //Add the event sources event type columns to the report header
            //Later these columns can be removed if disabled in the ERT settings file
//            for(EventType type : eventSourcesReportProperties.getEventTypeToDisplay().keySet()){
//                
//                EventColumnDisplay columnsDisplay = eventSourcesReportProperties.getEventTypeToDisplay().get(type);
//                if(columnsDisplay.isEnabled()){
//                    
//                    //should be all of them
//                    List<EventReportColumn> columns = columnsDisplay.getSelectedColumns();
//                    
//                    //add to header
//                    eventSourcesReportProperties.getReportColumns().addAll(columns);
//                }
//                
//            }
            
            
//            List<EventReportColumn> sourcesEventReportColumns = new ArrayList<EventReportColumn>(eventSourcesReportProperties.getReportColumns());
            
            //populate the report properties with settings in the saved ERT settings file
            ReportProperties settingsFileReportProperties;
            try{
                settingsFileReportProperties = ertService.loadReportProperties(reportPropFile.getAbsolutePath(), eventSourcesReportProperties);
            }catch(Exception e){
                throw new Exception("There was a problem loading the ERT report properties found in '"+reportPropFile+"'.", e);
            } 
            
            /////////////////////////////////////////////
            
            ReportProperties reportProperties = settingsFileReportProperties;
//            
//            for (EventType eventType : reportProperties.getEventTypeOptions()) {
//                
//                List<EventReportColumn> selectedColumns = reportProperties.getEventTypeDisplay(eventType).getSelectedColumns();
//                java.util.Collections.sort(selectedColumns);
//                reportProperties.getReportColumns().addAll(selectedColumns);
//            }
            
            /////////////////////////////////////////////
            
//            ReportProperties reportProperties = null;
//            
//            //removes all null entries from the list
//            eventSourcesReportProperties.getReportColumns().removeAll(Collections.singletonList(null));
//
//            //copy of current event sources default columns (enabled or not)
//            List<EventReportColumn> reportColumns = new ArrayList<EventReportColumn>(sourcesEventReportColumns);
//
//            //going to rebuild this by merging what is currently available for the event sources
//            //and the properties being loaded
//            reportProperties = null;
//
//            //removes all null entries from the list
//            settingsFileReportProperties.getReportColumns().removeAll(Collections.singletonList(null));
//            
//            reportProperties = settingsFileReportProperties;            
//            sourcesEventReportColumns = new ArrayList<EventReportColumn>(reportProperties.getReportColumns());
//
//            //contains the default columns from the settings file
//            List<EventReportColumn> newReportColumns = new ArrayList<EventReportColumn>(sourcesEventReportColumns);
//
//            //remove default columns that came from the settings file if they aren't available
//            //in the selected and parsed event sources, leaving a merged list of only default
//            //columns that are in the event sources and are in the settings file
//            newReportColumns.retainAll(reportColumns);
//
//            //create collection of default columns that aren't in the settings file but from event sources
//            List<EventReportColumn> oldReportColumns = new ArrayList<EventReportColumn>(reportColumns);
//            oldReportColumns.removeAll(newReportColumns);
//
//            sourcesEventReportColumns.clear();
//
//            //merged default columns
//            sourcesEventReportColumns.addAll(newReportColumns);
//
//            //default columns from event sources
//            sourcesEventReportColumns.addAll(oldReportColumns);
//
//            reportProperties.getReportColumns().clear();
//
//            //add only the merged default columns
//            reportProperties.getReportColumns().addAll(newReportColumns);

            //fill other positions with null to indicate not enabled for those default columns
            //that are in the event sources but not in the settings
//            while (reportProperties.getReportColumns().size() < sourcesEventReportColumns.size()) {
//
//                reportProperties.getReportColumns().add(null);
//            }
            
            // add the event columns (e.g. accelerometer_x) to the default report columns (e.g. domain session time)
            // based on the events found in the event sources merged with the settings file loaded
            for (EventType eventType : reportProperties.getEventTypeOptions()) {
                
                List<EventReportColumn> selectedColumns = reportProperties.getEventTypeDisplay(eventType).getSelectedColumns();
                java.util.Collections.sort(selectedColumns);
                reportProperties.getReportColumns().addAll(selectedColumns);
                
                List<EventReportColumn> notSelectedColumns = reportProperties.getEventTypeDisplay(eventType).getNotSelectedColumns();
                reportProperties.getReportColumns().removeAll(notSelectedColumns);
            }

            
            
            //////////////////////////////////////////////
            
//            List<EventType> eventsToAdd = new ArrayList<EventType>();
//            for(EventType eventType : batchReportProperties.getEventTypeOptions()){
//                
//                EventColumnDisplay displaySettings = batchReportProperties.getEventTypeDisplay(eventType);
//                if(displaySettings.isEnabled()){
//                    eventsToAdd.add(eventType);
//                }
//            }
//            
//            batchReportProperties.setDefaultSelectedEventTypes(eventsToAdd);
            
            //name report after user id and domain session
            reportProperties.setFileName("ERT_uId_"+data.userId+"_dsId_"+data.domainSessionId+".batchReport.csv");
            
            //place new report file in specific output folder
            reportProperties.setOutputFolder(batchReportDirectory.getAbsolutePath());
            
            //kick off report generation thread, i.e. this will return the calling thread quickly
            ertService.generateEventReport(reportProperties);            

            //
            // Monitor the status of the report process
            //
            GenerateReportStatus reportStatus = ertService.getProgressIndicator();
            outputStatus("\n"+data+" Progress %: ", Level.INFO, null);
            int lastPercent = -1, nowPercent;
            while(!reportStatus.isFinished() && reportStatus.getException() == null){
                
                nowPercent = reportStatus.getProgress().getPercentComplete();
                if(nowPercent != lastPercent){
                    System.out.print(reportStatus.getProgress().getPercentComplete() + " ");  
                    lastPercent = nowPercent;                
                }
                
                try{
                    Thread.sleep(500);
                }catch(@SuppressWarnings("unused") Exception e){}
            }
            
            outputStatus(reportStatus.getProgress().getPercentComplete() + " ", Level.INFO, null);  
            
            if(reportStatus.getException() != null){
                outputStatus("\nERROR: failed to create report for user "+data.userId+" session "+data.domainSessionId+".", Level.ERROR, null);
            }else{            
                outputStatus("\nCreated report file for user "+data.userId+" session "+data.domainSessionId+" : "+reportProperties.getFileName(), Level.ERROR, null);
            }
        }//end for
        
        outputStatus("Finished creating reports.", Level.INFO, null);
    }
    
    /**
     * Recursively traverses the event sources tree (which represents the file structure under the GIFT data directory provided) to determine
     * the type of each GIFT data file found.  Once a file is determined to be from a GIFT domain session it is paired with
     * other GIFT data from that domain session.
     *  
     * @param eventSourcesTreeNodes contains references to the files found under the data directory provided
     * @param dsIdToData mapping of domain session id to the data for that session, populated by this method
     * @param ertService used to retrieve the file details for an event source tree node
     */
    private void groupData(List<EventSourceTreeNode> eventSourcesTreeNodes, Map<Integer, DomainSessionData> dsIdToData, ErtRpcServiceImpl ertService){
        
        for(EventSourceTreeNode node : eventSourcesTreeNodes){
            
            if(node.isFolder()){
                groupData(node.getChildren(), dsIdToData, ertService);
            }else{
                //check file type
                
                File file = ertService.getFileForNode(node.getNodeId());
                if(EventSourceUtil.isMessageLog(file)){
                    //found a message log file
                    
                    DomainSession dSession = DomainSessionLogger.populateIdsFromFileName(file);
                    
                    if(dSession == null){
                        outputStatus("ERROR: unable to determine the user id and domain session id from the message log file '"+file+"' therefore it will be skipped.", Level.ERROR, null);
                        continue;
                    }
                    
                    DomainSessionData data = dsIdToData.get(dSession.getDomainSessionId());
                    if(data == null){
                        data = new DomainSessionData();
                        data.domainSessionId = dSession.getDomainSessionId();
                        data.userId = dSession.getUserId();
                        dsIdToData.put(dSession.getDomainSessionId(), data);
                    }else if(data.msgLogFileNodeId != -1){
                        //ERROR
                        outputStatus("ERROR: found a second domain session message log file for session "+dSession.getDomainSessionId()+" of user "+dSession.getUserId()+" in the message log file '"+file+"' therefore this file will be skipped.", Level.ERROR, null);
                        continue;
                    }
                    
                    outputStatus("User "+dSession.getUserId()+": found domain session message log file of "+file.getName(), Level.INFO, null);
                    data.msgLogFileNodeId = node.getNodeId();
                    

                }else if(EventSourceUtil.isSensorWriter(file)){
                    //found a sensor data file
                    
                    DomainSession dSession = AbstractFileWriter.populateIdsFromFileName(file);
                    
                    if(dSession == null){
                        outputStatus("ERROR: unable to determine the user id and domain session id from the sensor data file '"+file+"' therefore it will be skipped.", Level.ERROR, null);
                        continue;
                    }
                    
                    DomainSessionData data = dsIdToData.get(dSession.getDomainSessionId());
                    if(data == null){
                        data = new DomainSessionData();
                        data.domainSessionId = dSession.getDomainSessionId();
                        data.userId = dSession.getUserId();
                        dsIdToData.put(dSession.getDomainSessionId(), data);
                    }
                    
                    outputStatus("User "+dSession.getUserId()+": found sensor data file of "+file.getName(), Level.INFO, null);
                    data.sensorFileNodeIds.add(node.getNodeId());
                    
                }else if(EventSourceUtil.isBookmarkLog(file)){
                    
                    DomainSession dSession = BookmarkReader.parseFilename(file);                   
                    
                    if(dSession == null){
                        outputStatus("ERROR: unable to determine the user id and domain session id from the Bookmark log file '"+file+"' therefore it will be skipped.", Level.ERROR, null);
                        continue;
                    }
                    
                    DomainSessionData data = dsIdToData.get(dSession.getDomainSessionId());
                    if(data == null){
                        data = new DomainSessionData();
                        data.domainSessionId = dSession.getDomainSessionId();
                        data.userId = dSession.getUserId();
                        dsIdToData.put(dSession.getDomainSessionId(), data);
                    }
                    
                    outputStatus("User "+dSession.getUserId()+": found bookmark log file of "+file.getName(), Level.INFO, null);
                    data.bookmarkNodeId = node.getNodeId();
                    
                    
                }
            }
        }
        
    }
    
    /**
     * Inner class used to wrap around various attributes collected
     * for a users domain session.  These attributes are essential when
     * executing the ERT report generation logic.
     * 
     * @author mhoffman
     *
     */
    private static class DomainSessionData{
        
        /** 
         * the event node id of a user's domain session message log file to be used in the ERT report.
         * The node id is assigned when building the node tree of all GIFT data files found in the data folder
         */
        public int msgLogFileNodeId = -1;      
        
        /**
         * a collection of event node ids of a user's domain session sensor data files to be used in the ERT report.
         * The node id is assigned when building the node tree of all GIFT data files found in the data folder
         */
        public List<Integer> sensorFileNodeIds = new ArrayList<Integer>();
        
        public int bookmarkNodeId = -1;
        
        /**
         * domain session information
         */
        public int userId;
        public int domainSessionId;
        
        @Override
        public String toString(){
            return "[user "+userId+" : session "+domainSessionId+"]";
        }
    }

    /**
     * Run the batch report logic.
     * 
     * @param args a single, optional, parameter can be provided which is the path to the 
     * batch report configuration file.  If the parameter is not provided the default configuration file
     * is used ({@link BatchReportUtil#DEFAULT_CONFIG}})
     * @throws Exception if there was a problem during the batch report logic
     */
    public static void main(String[] args) throws Exception {
        
        File batchConfigFile;
        if(args == null || args.length == 0){
            System.out.println("Using the default ERT batch report configuration file of '"+DEFAULT_CONFIG+"'.");
            batchConfigFile = DEFAULT_CONFIG;
        }else{        
            batchConfigFile = new File(args[0]);
            System.out.println("Using the provided ERT batch report configuration file of '"+batchConfigFile+"'.");
        }
              
        try{
            BatchReportUtil batch = new BatchReportUtil(true);
            batch.configure(batchConfigFile);
            batch.process();
        
            System.out.println("\n\nDONE.");
            
            try {
                BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
                String input = null;
                do {
                    System.out.print("\nPress Enter to close.\n");
                    input = inputReader.readLine();
    
                } while (input != null && input.length() != 0);
                
            } catch (Exception e) {
                System.err.println("Caught exception while reading input: \n");
                e.printStackTrace();
            }
        }catch(Throwable t){
            t.printStackTrace();
            
            System.out.println("\n\nOops, something went wrong.");
            System.exit(1);
        }
    }
}
