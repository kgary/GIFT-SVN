/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import mil.arl.gift.common.DomainSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class will parse a bookmark event log file and create Bookmark Entry objects.
 * 
 * @author mhoffman
 *
 */
public class BookmarkReader {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(BookmarkReader.class);
    
    public static final String DELIM = " ";
    private static final int TIME_INDEX = 0;
    private static final int DS_TIME_INDEX = 1;
    private static final int ANNOTATION_INDEX = 2;
    private static final int USER_ID_INDEX = 0;
    private static final int DS_ID_INDEX = 1;    

    private static final String FILENAME_PREFIX = "bookmark";
    private static final String USER_ID_SUFFIX = "_uId";
    private static final String FILENAME_SUFFIX = "log";
    
    /** newer versions of bookmark files will have the first line like:  "user_id domainSession_id" */
    private Integer userId;
    private Integer domainSessionId;
    
    /** the bookmark file being read */
    private File file;
    
    /** container for bookmark entries found in the parsed file */
    private List<BookmarkEntry> bookmarks = new ArrayList<BookmarkEntry>();
    
    /**
     * Default constructor
     */
    public BookmarkReader(){
        
    }
    
    /**
     * Parse the bookmark file with the given name (including path) and create new bookmark event
     * objects for any bookmark event found.
     * 
     * @param fileName the bookmark file being read
     */
    public void parse(String fileName){
        
        file = new File(fileName);
        if(!file.exists()){
            throw new IllegalArgumentException("The bookmark event file named "+fileName+" doesn't exist");
        }
        
        reset();
        
        logger.info("Starting to parse bookmark event file named "+fileName);
        
        BufferedReader br = null;
        try{
            String line, annotation;
            String[] tokens = null;
            long time;
            Double ds_time;
            br = new BufferedReader(new FileReader(file)); 
            boolean idCheckCompleted = false;
            
            while((line = br.readLine()) != null){
                
                //reset
                ds_time = null;
              
                // Formats supported are:
                // "<user id integer> <domain session id integer>"   [first row only]    example: "12 64"
                // "<epoch integer> <message>"                                           example: "12345678 This is a bookmark message"
                // "<epoch integer> <elapse domain session time double> <message>"       example: "12345678 12.34 This is a bookmark message"                
                tokens = line.split(DELIM, 3);
                
                if(!idCheckCompleted){
                    //check for user id and domain session id                    
                    
                    //don't try again if this attempt fails
                    idCheckCompleted = true;
                    
                    if(tokens.length == 2){
                        //the first line may contain the user id and domain session id
                        
                        String userIdToken = tokens[USER_ID_INDEX];
                        String dsIdToken = tokens[DS_ID_INDEX];
                        
                        try{
                            userId = Integer.parseInt(userIdToken);
                            domainSessionId = Integer.parseInt(dsIdToken);
                            
                            //successfully set the ids
                            continue;
                            
                        }catch(@SuppressWarnings("unused") Exception e){
                            //something went wrong, assume the first line doesn't contain the ids
                            userId = null;
                            domainSessionId = null;
                        }
                    }
                    
                    //try to get the user id and domain session id from the default formatted file name
                    DomainSession ds = parseFilename(file);
                    if(ds != null){
                        userId = ds.getUserId();
                        domainSessionId = ds.getDomainSessionId();
                    }
                    
                }
                
                if(tokens != null && tokens.length >= 2){
                    //has at least 1 delimeter in the line, need to determine if the line is a legacy bookmark entry or not
                    
                    //check if the 2nd token is a double
                    try{
                        ds_time = Double.parseDouble(tokens[DS_TIME_INDEX]);
                    }catch(@SuppressWarnings("unused") Exception e){
                        //not a double, therefore the entry is a legacy entry w/o the elapsed domain session time value as the 2nd token
                    }

                    
                    time = Long.parseLong(tokens[TIME_INDEX]);
                    if(ds_time == null){
                        //the 2nd token is part of the annotation
                        
                        if(tokens.length == 2){
                            //the line has only 1 delimeter value, therefore the entire annotation is in the 2nd token
                            annotation = tokens[DS_TIME_INDEX];
                        }else{
                            //the line has more than 1 delimeter value, therefore the entire annotation is in the 2nd and 3rd tokens
                            annotation = tokens[DS_TIME_INDEX] + DELIM + tokens[ANNOTATION_INDEX];
                        }
                                                
                        bookmarks.add(new BookmarkEntry(time, annotation));
                        
                    }else{
                        //the 3rd token contains the entire annotation
                        annotation = tokens[ANNOTATION_INDEX];
                        
                        bookmarks.add(new BookmarkEntry(time, ds_time, annotation));
                    }

                }else{
                    logger.error("Unable to parse bookmark entry = "+line);
                }
            }
            
            logger.info("Successfully read in "+bookmarks.size()+" bookmark events");
            
        }catch(Exception e){
            logger.error("Caught exception while reading bookmark file named "+fileName, e);
        }finally{
            
            if(br != null){
                try {
                    br.close();
                } catch (IOException e) {
                    logger.error("Caught exception while trying to close the file "+fileName+" for reading",e);
                }
            }
        }
        
    }
    
    /**
     * Reset this log reader by clearing out containers with messages parsed from a previous
     * log file parse.
     */
    private void reset(){
        bookmarks.clear();
    }
    
    /**
     * Return the list of bookmark events found in the parsed file.
     * 
     * @return List<BookmarkEntry>
     */
    public List<BookmarkEntry> getBookmarks(){
        return bookmarks;
    }
    
    /**
     * Return the user id associated with this bookmark file.
     * 
     * @return Integer - user id, can be null if no user id was determined.
     */
    public Integer getUserId(){
        return userId;
    }
    
    /**
     * Return the domain session id associated with this bookmark file.
     * 
     * @return Integer - domain session id, can be null if no domain session id was determined.
     */
    public Integer getDomainSessionId(){
        return domainSessionId;
    }
    
    /**
     * Generates the bookmark filename as a string using the domain session information.
     *
     * @param domainSession the domain session info.
     * @return String - the generated filename of the bookmark file
     */
    public static String genFilename(DomainSession domainSession) {           
    
        String filename = FILENAME_PREFIX + domainSession.getDomainSessionId() + USER_ID_SUFFIX + domainSession.getUserId() + "." + FILENAME_SUFFIX;

        return filename;
    }
    
    /**
     * Parse the bookmark file name and determine if the domain session information can be obtained
     * from it.  In order for the file name to be parsed successfully it should match the 'genFilename' method logic.
     * 
     * @param file a bookmark file to parse the file name
     * @return DomainSession - session information if found in the file name, otherwise it can be null.
     */
    public static DomainSession parseFilename(File file){
        
        String name = file.getName();
        if(name.startsWith(BookmarkReader.FILENAME_PREFIX) && name.contains(USER_ID_SUFFIX)){
            
            try{
                name = name.replaceFirst(BookmarkReader.FILENAME_PREFIX, "");
            
                String dsIdStr = name.substring(0, name.indexOf(USER_ID_SUFFIX));
                int dsId = Integer.parseInt(dsIdStr);
                
                String uIdStr = name.substring(name.indexOf(USER_ID_SUFFIX) + USER_ID_SUFFIX.length(), name.length() - FILENAME_SUFFIX.length() - 1);
                int uId = Integer.parseInt(uIdStr);
                
                return new DomainSession(dsId, uId, DomainSession.UNKNOWN_DOMAIN_NAME, DomainSession.UNKNOWN_DOMAIN_NAME);
            
            }catch(@SuppressWarnings("unused") Exception e){
                //something went wrong, the file name is not correctly formatted
                return null;
            }
            
        }
        
        return null;
    }
}
