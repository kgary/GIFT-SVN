/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.ert.server;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.PackageUtil;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.ert.ReportProperties;
import mil.arl.gift.common.ert.event.AbstractEvent;
import mil.arl.gift.common.ert.event.DefaultEvent;
import mil.arl.gift.common.ert.event.DomainSessionEvent;
import mil.arl.gift.common.ert.server.MessageLogEventSourceParser;
import mil.arl.gift.common.ert.server.ReportWriter;
import mil.arl.gift.common.io.DesktopFolderProxy;
import mil.arl.gift.common.io.EventSourceFileFilter;
import mil.arl.gift.common.io.FileFinderUtil;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.common.logger.DomainSessionMessageEntry;
import mil.arl.gift.common.logger.MessageLogReader;
import mil.arl.gift.net.api.message.Message;
import mil.arl.gift.tools.ert.shared.EventSourceTreeNode;
import mil.arl.gift.tools.ert.shared.EventSourcesTreeModel;

/**
 * This is the server side for the Event Report Tool (ERT).  The ERT is used to create output file(s) for reports that contain
 * GIFT events (e.g. Learner State message).
 * 
 * @author mhoffman
 *
 */
public class EventReportServer {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(EventReportServer.class);    

    private static final String DOMAIN_SESSIONS_DIR = PackageUtil.getDomainSessions();
    private static final String[] DEFAULT_EVENT_ROOT_DIRS = new String[]{DOMAIN_SESSIONS_DIR};
    
    public static final String DEFAULT_SETTINGS_ROOT_DIR = PackageUtil.getOutput();
    
    /** map of unique event source node id to the file representing that source */
    private Map<Integer, File> nodeIdToFileName = new HashMap<Integer, File>();
    
    /** the tree model contains the source nodes to be displayed to the user */
    private EventSourcesTreeModel treeModel = new EventSourcesTreeModel();
    
    /**
     * Class constructor
     */
    public EventReportServer(){
        
    }
    
    /**
     * Return the tree model contains the source nodes to be displayed to the user
     * 
     * @return EventSourcesTreeModel
     */
    public EventSourcesTreeModel getTreeModel(){
        return treeModel;
    }
    
    /**
     * Return the file associated with the event source node id provided.
     * 
     * @param nodeId unique event source node id
     * @return File
     */
    public File getFileForNode(int nodeId){
        return nodeIdToFileName.get(nodeId);
    }
    
    /**
     * Populate the tree model with the list of event source files available
     * 
     * @param rootDirectories - root directories to recursively search for event files in
     */
    public void findEventFiles(String[] rootDirectories){
        
        reset();
        
        for(String rootDirectory : rootDirectories){
            
            File file = new File(rootDirectory);
            EventSourceTreeNode node = createTreeNode(rootDirectory, file, true, true);          
            
            getEventSourceFiles(file, node);            
        }
        
        logger.info("Found "+nodeIdToFileName.size()+" file (+folder) entries");

    }
    
    /**
     * Populate the tree model with the list of event source files available
     * 
     * @param rootDirectories - root directories to recursively search for event files in.  If 
     * null the default will be used.
     */
    public void findEventFiles(File[] rootDirectories){
        
        reset();
        
        if(rootDirectories == null){
            findEventFiles();
            return;
        }
        
        for(File folder : rootDirectories){
            
            EventSourceTreeNode node = createTreeNode(folder.getName(), folder, true, true);          
            
            getEventSourceFiles(folder, node);            
        }
        
        logger.info("Found "+nodeIdToFileName.size()+" file (+folder) entries");

    }

    /**
     * Populate the tree model using the default list of event source files
     */
    public void findEventFiles() {
        findEventFiles(DEFAULT_EVENT_ROOT_DIRS);
    }

    /**
     * Find the list of report settings files in the default directory
     *
     * @return String[] The list of report setting files
     * @throws IOException 
     */
    public String[] findSettingsFiles() throws IOException{
        return findSettingsFiles(DEFAULT_SETTINGS_ROOT_DIR);
    }
    
    /**
     * Find the list of report settings files in the specified directory
     * 
     * @param rootDirectoryName - directory to start searching for report settings files
     * @return String[] - file names of report setting files found
     * @throws IOException 
     */
    public String[] findSettingsFiles(String rootDirectoryName) throws IOException{
        
        File rootDirectory = new File(rootDirectoryName);
        if(!rootDirectory.exists()){
            throw new IllegalArgumentException("The root directory "+rootDirectoryName+" was not found.");
        }
        
        List<FileProxy> files = new ArrayList<FileProxy>();
        FileFinderUtil.getFilesByExtension(new DesktopFolderProxy(rootDirectory), files, ReportProperties.DEFAUL_SETTINGS_FILENAME_EXT);
        String[] filenames = new String[files.size()];
        for(int i = 0; i < files.size(); i++){
            FileProxy file = files.get(i);
            filenames[i] = FileFinderUtil.getRelativePath(rootDirectory, new File(file.getFileId()));
        }
        
        logger.info("Found "+filenames.length+" setting files");
        
        return filenames;
    }

    /**
     * Create a new tree node using the provided parameters.  Also add the node to the tree model and
     * the map of all nodes.
     * 
     * @param name - the name of the node
     * @param file - the file containing the events for this node
     * @param isFolder - whether this node represents a folder file
     * @param isRoot - whether this node is a root node (i.e. the node has children)
     * @return EventSourceTreeNode - the new node
     */
    private EventSourceTreeNode createTreeNode(String name, File file, boolean isFolder, boolean isRoot){
        
        EventSourceTreeNode node = new EventSourceTreeNode(EventSourcesTreeModel.getNextNodeId(), name, isFolder);
        treeModel.addNode(node, isRoot);
        nodeIdToFileName.put(node.getNodeId(), file);
        
        return node;
    }
    
    /**
     * Reset the ERT server knowledge of the files available for analysis
     */
    private void reset(){
        
        logger.info("Resetting event report server");
        treeModel.reset();
        nodeIdToFileName.clear();
    }
    
    /**
     * Populate the root node with the list of Event Source files in the specified directory,
     * recursively calling itself for all directories
     * 
     * @param dir The directory to search
     * @param root The parent node to populate with children for each found event source file
     */
    private void getEventSourceFiles(File dir, EventSourceTreeNode root) {
        
        final File[] children = dir.listFiles(new EventSourceFileFilter());
        if (children != null) {
            for (File child : children) {
                
                EventSourceTreeNode childNode;
                if(child.isFile()) {
                    childNode = createTreeNode(child.getName(), child, false, false);

                }else{
                    childNode = createTreeNode(child.getName(), child, true, false);
                    getEventSourceFiles(child, childNode);
                }
                
                root.addChild(childNode);
            }
        }
    }
    
    /**
     * Note: This method is used for testing.
     * Parse the contents of an event file represented by the tree node.  If the file is a directory
     * recursively search for files to parse.
     * 
     * @param node contains the event source information for an event file
     * @param ert the server side instance of the ERT requesting the parsing of the event file
     * @param reader responsible for parsing events from the file
     * @throws Exception if there was a severe problem parsing the event file
     */
    @SuppressWarnings("unused")
    private static void parseFile(EventSourceTreeNode node, EventReportServer ert, MessageLogReader reader) throws Exception{
        
        File file = ert.getFileForNode(node.getNodeId());
        if(file.isDirectory()){
            
            for(EventSourceTreeNode child : node.getChildren()){
                parseFile(child, ert, reader);
            }
            
        }else{
        
            reader.parseLog(new FileProxy(new File(file.getAbsolutePath())));
            
            System.out.println("Finished parsing file: "+file.getName());
            
            String outputFilename = "test.EventReportTool."+file.getName()+".out.csv";
            ReportWriter writer;
            if(reader.isDomainSessionLog()){
                writer = new ReportWriter(outputFilename, new LinkedHashSet<>(MessageLogEventSourceParser.domainSessionColumns), true);
            }else{
                writer = new ReportWriter(outputFilename, new LinkedHashSet<>(MessageLogEventSourceParser.systemColumns), true);
            }
            
            //create report using all messages in the file
            Set<MessageTypeEnum> types = reader.getTypesOfMessages();
            for(MessageTypeEnum type : types){
                
                for (Message message : reader.getMessagesByType(type)) {

                    AbstractEvent event;
                    String payload = message.getPayload() != null ? message.getPayload().toString() : null;
                    if (message instanceof DomainSessionMessageEntry) {
                        event = new DomainSessionEvent(message.getMessageType().toString(), message.getTimeStamp(),
                                ((DomainSessionMessageEntry) message), payload);

                    } else {
                        event = new DefaultEvent(message.getMessageType().toString(), message.getTimeStamp(), payload);
                    }
                    writer.addRow(event.toRow());
                }
            }
            
            try{
                writer.write();
                System.out.println("Finished creating report file: "+outputFilename);
            }catch(Exception e){
                System.out.println("Caught exception while writing report");
                e.printStackTrace();
            }
        }
    }
    
    

    
    /**
     * Load the report properties from the file specified.
     * 
     * @param filename - the file name to load content from.  Must be relative to the output directory.
     * @param reportProperties The properties of the current file loaded
     * @return ReportProperties The new properties of report to generate
     * @throws Exception - can throw various I/O exceptions during file read operations
     */
    public static ReportProperties loadReportProperties(String filename, ReportProperties reportProperties) throws Exception{
        
        File file = new File(DEFAULT_SETTINGS_ROOT_DIR + File.separator + filename);
        if(!file.exists()){
            logger.error("Unable to find settings files with path "+file.getAbsolutePath());
            throw new IllegalArgumentException("The file named "+filename+" was not found.");
        }
        
        return ReportWriter.loadReportProperties(file, reportProperties);
    }

}
