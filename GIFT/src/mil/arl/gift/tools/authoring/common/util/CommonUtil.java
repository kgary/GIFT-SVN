/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.common.util;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.DesktopFolderProxy;
import mil.arl.gift.common.io.FileFinderUtil;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.tools.authoring.common.CommonProperties;

/**
 * This class contains common utility methods used for authoring GIFT configuration files.
 * 
 * @author mhoffman
 *
 */
public class CommonUtil {
    
    protected static File workspaceDirectory = new File(CommonProperties.getInstance().getWorkspaceDirectory());
    
    private static final String DEFAULT_SCHEMA_VERSION = "1.0";
    private static final String VERSION_ATTRIBUTE = "version";
    private static final String DEFAULT_FILE_VERSION = "1";
    
    /**
     * Opens the given schema file and extracts the version attribute from it.
     * @param schemaFile Schema file whose version attribute should be read.
     * @return The version attribute read from the schema file, if no such
     * attribute can be read then a default value (1) is returned.
     */
    static public String getSchemaVersion(File schemaFile){
        
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        String version = DEFAULT_SCHEMA_VERSION;
        try {

            //Using factory get an instance of document builder
            DocumentBuilder db = dbf.newDocumentBuilder();

            //parse using builder to get DOM representation of the XML file
            Document dom = db.parse(schemaFile);
            
            //get the root element
            Element docEle = dom.getDocumentElement();

            String schemaVersion = docEle.getAttribute(VERSION_ATTRIBUTE);
            if(schemaVersion != null){
                version = schemaVersion;
            }

        }catch(Exception e) {
            e.printStackTrace();
        }
        
        return version;
    }

    /**
     * Generates a new Version attribute for a Course or DKF based on the
     * current version attribute.
     * 
     * Course and DKF files have a version attribute of the form "#.#.#" where
     * the first two numbers refer to the schema they were created against and
     * the second number refers to the number of times the Course or DKF has
     * been saved.
     * 
     * @param currentVersion The current version attribute for the Course or
     * DKF, if this is a new File that hasn't been assigned a version then
     * you can pass in null or the empty string.
     * @param schemaVersion The schema version that Course or DKF was built
     * against. Use the getSchemaVersion method to obtain this.
     * @return A version attribute in the form "#.#.#". The first two numbers
     * will correspond to the schema version. If currentVersion contained a
     * 3rd number then this third number will be one greater, otherwise the
     * 3rd number will be 1.
     */
    static public String generateVersionAttribute(String currentVersion, String schemaVersion) {
    	if(currentVersion == null || currentVersion.length() == 0) {
    		return schemaVersion + "." + DEFAULT_FILE_VERSION;
    	}
    	
    	String newVersion;
    	
    	//increment XML file version
        String[] versions = currentVersion.split("\\.");
        if(versions.length == 3){
            //the current version has the correct format
            
            if(currentVersion.startsWith(schemaVersion)){
                //the current version is of the current schema version, increment the file version only                            
                newVersion = schemaVersion + "." + (Integer.valueOf(versions[2])+1);
            }else{
                //the current version is of a different schema version, set the file version to 1
                //logger.info("The current schema version of "+versions[0]+"."+versions[1]+" referenced in the first part of the version value is being changed to "+schemaVersion+", therefore resetting the file version to 1.");
                newVersion = schemaVersion + "." + "1";
            }
            
        }else{
            //logger.error("Not sure how to increment version text of "+currentVersion);
            newVersion = currentVersion;
        }
        return newVersion;
    }
    
    /**
     * Returns a list of file names with the given extension.  The file names will include the path starting 
     * at the starting folder but not including that folder name.
     * 
     * @param startingFolder the location to start searching for files
     * @param extensions a list of file extensions to filter found files by (e.g. "html").  Can be null.
     * @return List<String> collection of file names found with the given extension
     * @throws IOException if there was a problem retrieving files by extension
     * @throws URISyntaxException if there was a problem making a file name relative
     */
    public static List<String> getRelativeFileNamesByExtensions(File startingFolder, String... extensions) throws IOException, URISyntaxException{
        
        if(startingFolder == null || !startingFolder.isDirectory()){
            throw new IllegalArgumentException("The starting directory of "+startingFolder+" must be a directory.");
        }
        
        DesktopFolderProxy folderProxy = new DesktopFolderProxy(startingFolder);
        
        //search for files of this extension                    
        List<FileProxy> files = new ArrayList<>();
        FileFinderUtil.getFilesByExtension(folderProxy, files, extensions);     

        List<String> values = new ArrayList<>(files.size());
        for(FileProxy file : files){
            values.add(folderProxy.getRelativeFileName(file));
        }
        
        return values;
    }
    
    /**
     * Returns a list of lesson material file names.  A lesson material file is an XML file that adheres to 
     * the lesson material xsd.  It defines a list of lesson material content to be displayed in a lesson
     * material course transition.
     * 
     * The file names will include the path starting at the starting folder but not including that folder name.
     * 
     * @param startingFolder the location to start searching for files
     * @return List<String> collection of lesson material file names found
     * @throws IOException if there was a problem retrieving files by extension
     * @throws URISyntaxException if there was a problem making a file name relative
     */
    public static List<String> getLessonMaterialFiles(File startingFolder) throws IOException, URISyntaxException{
        
        if(startingFolder == null || !startingFolder.isDirectory()){
            throw new IllegalArgumentException("The starting directory of "+startingFolder+" must be a directory.");
        }
        
        DesktopFolderProxy folderProxy = new DesktopFolderProxy(startingFolder);
        
        //search for existing lesson material xml files
        List<FileProxy> files = new ArrayList<>();
        FileFinderUtil.getFilesByExtension(folderProxy, files, AbstractSchemaHandler.LESSON_MATERIAL_FILE_EXTENSION);       

        List<String> values = new ArrayList<>(files.size());
        for(FileProxy file : files){
            values.add(folderProxy.getRelativeFileName(file));
        }
        
        return values;
    }
    
    /**
     * Returns a list of learner action file names.  A learner action file is an XML file that adheres to 
     * the learner actions xsd.  It defines a list of learner actions user interface components (e.g. buttons)
     * that are to be displayed on the tutor during a training app course transition.
     * 
     * The file names will include the path starting at the starting folder but not including that folder name.
     * 
     * @param startingFolder the location to start searching for files
     * @return List<String> collection of learner action file names found
     * @throws IOException if there was a problem retrieving files by extension
     * @throws URISyntaxException if there was a problem making a file name relative
     */
    public static List<String> getLearnerActionsFiles(File startingFolder) throws IOException, URISyntaxException{
        
        if(startingFolder == null || !startingFolder.isDirectory()){
            throw new IllegalArgumentException("The starting directory of "+startingFolder+" must be a directory.");
        }
        
        DesktopFolderProxy folderProxy = new DesktopFolderProxy(startingFolder);
        
        //search for existing lesson material xml files
        List<FileProxy> files = new ArrayList<>();
        FileFinderUtil.getFilesByExtension(folderProxy, files, AbstractSchemaHandler.LEARNER_ACTIONS_FILE_EXTENSION);
   

        List<String> values = new ArrayList<>(files.size());
        for(FileProxy file : files){
            values.add(folderProxy.getRelativeFileName(file));
        }
        
        return values;
    }
    
    /**
     * Returns a list of SIMILE configuration files.  A SIMILE configuration file defines the conditions used
     * by the SIMILE assessment engine to assessment concepts.  The files are normally authored using the
     * SIMILE workbench application.
     * 
     * The file names will include the path starting at the starting folder but not including that folder name.
     * 
     * @param startingFolder the location to start searching for files
     * @return List<String> collection of learner action file names found
     * @throws IOException if there was a problem retrieving SIMILE files by extension
     * @throws URISyntaxException if there was a problem making relative file names
     */
    public static List<String> getSIMILEConfigFiles(File startingFolder) throws IOException, URISyntaxException{
        
        if(startingFolder == null || !startingFolder.isDirectory()){
            throw new IllegalArgumentException("The starting directory of "+startingFolder+" must be a directory.");
        }
        
        DesktopFolderProxy folderProxy = new DesktopFolderProxy(startingFolder);
        
        //the file extensions to search for when finding SIMILE config files
        String[] FILE_EXTENSION = {"ixs"};
        
        List<FileProxy> files = new ArrayList<>();
        FileFinderUtil.getFilesByExtension(folderProxy, files, FILE_EXTENSION);
        
        List<String> values = new ArrayList<>(files.size());
        for(FileProxy file : files){
            values.add(folderProxy.getRelativeFileName(file));
        }
        
        return values;
    }
    
    /**
     * Returns a list of DKF names.  The file names will include the path starting
     * at the starting folder provided (but not including that folder name).
     * 
     * @param startingFolder the location to start searching for files
     * @return List<String> collection of DKF names found
     * @throws IOException if there was a problem retrieving the DKFs
     * @throws URISyntaxException if there was a problem building a URI to determine the relative path for a DKF
     */
    public static List<String> getDKFs(File startingFolder) throws IOException, URISyntaxException{

        if(startingFolder == null || !startingFolder.isDirectory()){
            throw new IllegalArgumentException("The starting directory of "+startingFolder+" must be a directory.");
        }
        
        DesktopFolderProxy folderProxy = new DesktopFolderProxy(startingFolder);
        
        List<FileProxy> files = new ArrayList<FileProxy>();
        FileFinderUtil.getFilesByExtension(folderProxy, files, AbstractSchemaHandler.DKF_FILE_EXTENSION);   

        List<String> values = new ArrayList<>(files.size());
        for(FileProxy file : files){
            values.add(folderProxy.getRelativeFileName(file));
        }
        
        return values;
    }
    
}
