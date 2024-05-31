/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.gwt.shared.MediaHtml;
import mil.arl.gift.common.io.CommonProperties;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.io.UriUtil;
import mil.arl.gift.net.rest.RESTClient;

/**
 * A class that has utility methods for servlets used in GIFT
 *
 * @author jleonard
 */
public class GiftServletUtils {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(GiftServletUtils.class);

    /**
     * Virtual Human status values that can be received from the REST call
     */
    private static final String VH_RUNNING_STATUS = "Running";
    private static final String VH_STOPPED_STATUS = "Stopped";
    private static final String VH_INITIALIZING_STATUS = "Initializing";
    private static final String VH_ERROR_STATUS = "Error";
    
    /** temp folder file used to no longer remind the user that a character server can't be found */
    private static final File NoCharacterServerReminderFile = new File("temp" + File.separator + "NoCharacterServerReminder");

    private GiftServletUtils() {
    }
    
    /**
     * Used to check whether the virtual human character server is running by checking the RESTful
     * call at "/tutor/vhuman/VHTServiceStatus".
     * 
     * @param vhURL the base URL of the virtual human character server (e.g. http://localhost:8088).
     * @return either "true" or "false" where true means the VH character server is reachable and running.
     */
    public static String checkVHCharacterServer(String vhURL){
        
        Boolean reachable = false;
        try{
            // #3829 - VH character server doesn't support https at the moment.
//            String scheme = CommonProperties.getInstance().shouldUseHttps() ? Constants.HTTPS : Constants.HTTP;
            String scheme = Constants.HTTP;
            RESTClient restClient = new RESTClient();
            byte[] response = restClient.get(new URL(scheme + vhURL + "/tutor/vhuman/VHTServiceStatus"));
            String responseStr = new String(response);
            if(logger.isDebugEnabled()){
                logger.debug("response from virtual human character server is '"+responseStr+"'");
            }
            if(responseStr.equals(VH_RUNNING_STATUS)){
                reachable = true;
            }else if(responseStr.equals(VH_STOPPED_STATUS) || responseStr.equals(VH_INITIALIZING_STATUS) || responseStr.equals(VH_ERROR_STATUS)){
                logger.warn("The virtual human character server at '"+scheme+vhURL+"' returned a non-running status of '"+responseStr+"'.");
            }else{
                logger.warn("The virtual human character at '"+scheme+vhURL+"' returned an unhandled response of '"+responseStr+"'.");
            }
        }catch(Throwable t){
            //catch any exception to prevent catastrophic failure in the tutor module just because of the tutor character server configuration
            logger.error("Caught a server side error while trying to contact the virtual human character server at '"+vhURL+"'.", t);
        }
        
        return reachable.toString();
    }

    /**
     * Used to check whether Media Semantics character server is installed locally.
     * The check tries to gets the first valid directory from a list of directory paths to check.
     * These checks make sure the entries are existing directories that have a "Logs" sub-directory with write permissions.
     * The check will not happen if a virtual human character server is running as noted by the value of
     * {@link isVirtualHumanCharacterServerOnline}. If a local media semantics character server is not found when checked for,
     * a swing dialog will be shown mentioning the error or a more general message of the important of a character server.
     * 
     * @param directories The list of paths to check (from tutor.xml)
     * @param isVirtualHumanCharacterServerOnline a boolean value as a string that indicates whether the Virtual Human
     * character server is running.
     * @return String The first valid directory, empty string if no paths were valid
     */
    public static String getValidMSCDirectory(String[] directories, String isVirtualHumanCharacterServerOnline) {
        
        if(Boolean.valueOf(isVirtualHumanCharacterServerOnline)){
            if(logger.isDebugEnabled()){
                logger.debug("Virtual Human character server is running, skipping check for locally running and configured media semantics character server");
            }
            return "";
        }
        
        if(directories == null){
            return "";
        }

        if(logger.isDebugEnabled()){
            logger.debug("Checking for locally running and configured media semantics character server");
        }

        //used for logging purposes (if needed)
        StringBuffer directoriesStr = new StringBuffer();
        
        try{
        
            for (String characterServerDirName : directories) {
                directoriesStr.append(" ").append(characterServerDirName).append(",");
    
                File characterServerDir = new File(characterServerDirName);
    
                if (characterServerDir.exists() && characterServerDir.isDirectory()) {
                    
                    //check write permissions in the logs directory
                    final File characterServerLogsDir = new File(characterServerDir.getAbsolutePath() + File.separator + "Logs" + File.separator);
                    try{
                        File writeCheckFile = new File(characterServerLogsDir.getAbsolutePath() + File.separator + "writeCheck.txt");
                        new FileOutputStream(writeCheckFile, true).close();
                        
                        try{
                            writeCheckFile.delete();
                        }catch(@SuppressWarnings("unused") Exception e){ 
                          //don't care if delete failed, just trying to cleanup
                        }
                        
                        if(logger.isInfoEnabled()){
                            logger.info("The media semantics character server directory of "+characterServerDirName+" is valid and has been defined, therefore characters will be enabled.");
                        }
                        
                    }catch(Exception e){
    
                        logger.error("The media semantics character server directory of "+characterServerLogsDir+" can't be written to.  The directory needs write permissions in order to run properly.  The character will be disabled.", e);
                        
                        //since this dialog doesn't alter this method's return value and the fact that it will prevent the tutor module from progressing
                        //in it's initialization (therefore making SPL initialization timeout), the dialog is presented in its own thread.
                        new Thread("Media Semantics Character Server Warning Dialog"){
                        	
                            
                            @Override
                            public void run() {
                                JOptionPane.showMessageDialog(null, "The media semantics character server directory of "+characterServerLogsDir+" can't be written to.\n" +
                                        "The directory needs write permissions in order to run properly (refer to the GIFT installation instructions).\n" +
                                        "You can either continue on and the character will be disabled for the execution of this Tutor module\n" +
                                        "                                              OR\n" +
                                        "Fix the issue and then restart the Tutor module.", "Warning: Problem configuring Media Semantics Tutor character", JOptionPane.WARNING_MESSAGE);
                            }
                        }.start();
                        
                        break;
                    }
    
                    return characterServerDirName;
                }
                
            }//end for
            
            if(directories.length > 0 && !NoCharacterServerReminderFile.exists()){
                //none of the directories to check were valid        
                
                logger.error("The media semantics character server directory argument of {"+directoriesStr.toString().trim()+"} resulted in no valid directories.  Therefore the avatar will be disabled.");

                //since this dialog doesn't alter this method's return value and the fact that it will prevent the tutor module from progressing
                //in it's initialization (therefore making SPL initialization timeout), the dialog is presented in its own thread.
                final String directoriesList = directoriesStr.toString();
                new Thread("Character Server Warning Dialog"){
                    
                    @Override
                    public void run() {
                        
                        String title = "Warning: Unable to connect to a character server";
                        String message = "It is recommended that you allow GIFT to use a character server.  GIFT provides the license free versions of\n"+
                                "Virtual Human and Media Semantics on gifttutoring.org.  Either character server provides services like text to speech.\n"+
                                "You can either:\n" +
                                "    - continue on and the character features will be disabled\n" +
                                "   OR\n" +
                                "    - fix the issue and then restart GIFT (more specifically the Tutor module).\n\n"+
                                "To 'Fix' the issue choose from the following:\n"+
                                "    - (recommended) install the Virtual Human character server (run the GIFT install for help)\n"+
                                "    - (other choice) install the Media Semantics character server (run the GIFT installer for help)\n"+
                                "       [if already installed] correct the 'LocalMediaSemanticsCharacterServerDirectory' 'Arg' path value in\n"+
                                "            GIFT/config/tutor/server/contexts/tutor.xml (more information is available in that file)\n"+
                                "            -> None of the following directories exist:\n{"+directoriesList.trim()+"}.\n\n" +
                                "To disable this message in the future and NOT fix the issue, do one of the following:\n" +
                                "    - Select 'Don't remind me' below.\n" +
                                "    - follow step 1 of the \"if you wish to disable\" instructions in GIFT/config/tutor/server/contexts/tutor.xml.\n";
                        String[] options = {"Don't remind me", "Keep reminding me"};
                        
                        try {
                            int choice = JOptionPane.showOptionDialog(null, message, title, 
                                    JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[1]);
                            if(choice == 0 && !NoCharacterServerReminderFile.exists()){
                                // save users choice to not be reminded
                            	NoCharacterServerReminderFile.createNewFile();
                            }
                        } catch (IOException e) {
                        	logger.error("Failed to update setting for no character server reminder", e);
                        } catch (java.awt.HeadlessException e) {
                        	logger.error("No character server reminder cannot be displayed in a headless environment", e);
                    	}
                    }
                }.start();

            }
        
        }catch(Exception e){
            //catch any exception to prevent catastrophic failure in the tutor module just because of the tutor character server configuration
            logger.error("Caught exception while trying to configure the media semantics character server directory.", e);
        }

        return Constants.EMPTY;
    }
    
    
    /**
     * Gets the web client ip address as a string.  This function takes into account if the server
     * is behind a proxy server.  However, note that this should be used sparingly since there is no
     * reliable way to guarantee the ip of the web client (if there is a NAT gateway or forward proxy).
     * 
     * @param request - The httpservlet request to get the ip address for.  Cannot be null.
     * @return String - The ip address of the web client that initiated the request.
     */
    static public String getWebClientAddress(HttpServletRequest request) {
        
        String address = null;
        if (request != null) {
            address = request.getHeader("X-FORWARDED-FOR");
            
            if (address == null) {
                address = request.getRemoteAddr();
            }
        } else {
            logger.error("getWebClientAddress is called, but HttpServletRequest is incorrectly null.");
        }

        return address;
    }
    
    /**
     * Converts a media item to its HTML representation
     *
     * @param media The media item to convert
     * @return MediaHtml The HTML representation of the media item
     */
    private static MediaHtml convertMediaToHtml(Serializable media) {
        
        try {
            
            //check to see which schema (i.e. course, DKF, etc.) the media was defined by and derive the appropriate HTML accordingly
            if(media instanceof generated.course.Media) {
                
                generated.course.Media mediaItem = (generated.course.Media) media;
                
                if (mediaItem.getName() != null) {
                    
                    boolean displayContentInNewWindow = false;
                    
                    if (mediaItem.getMediaTypeProperties() instanceof generated.course.WebpageProperties) {

                        StringBuilder uriStr = new StringBuilder(mediaItem.getUri());

                        displayContentInNewWindow = UriUtil.validateUriForSOPViolationOrBlockedContent(uriStr);
                    }
                    
                    return new MediaHtml(mediaItem, displayContentInNewWindow);

                } else {

                    logger.error("The media item has no name: " + mediaItem);

                    //TODO: Display an error how the media does not have a name
                }
                
            } else if(media instanceof generated.dkf.Media) {
                
                generated.dkf.Media mediaItem = (generated.dkf.Media) media;
                
                if (mediaItem.getName() != null) {
                    
                    boolean displayContentInNewWindow = false;
                    
                    if (mediaItem.getMediaTypeProperties() instanceof generated.dkf.WebpageProperties) {

                        StringBuilder uriStr = new StringBuilder(mediaItem.getUri());

                        displayContentInNewWindow = UriUtil.validateUriForSOPViolationOrBlockedContent(uriStr);
                    }
                    
                    return new MediaHtml(mediaItem, displayContentInNewWindow);

                } else {

                    logger.error("The media item has no name: " + mediaItem);

                    //TODO: Display an error how the media does not have a name
                }
                
            }

        } catch (Exception e) {

            logger.error("Caught an exception while converting some media to HTML", e);
        }
        
        return null;
    }

    /**
     * Converts a list of media items to their HTML representations
     *
     * @param mediaList The list of media items
     * @return List<MediaHtml> The list of HTML representations
     */
    public static List<MediaHtml> convertMediaListToHtml(List<Serializable> mediaList) {
        List<MediaHtml> mediaHtmlList = new ArrayList<MediaHtml>();
        for (Serializable mediaItem : mediaList) {
        	MediaHtml mediaHtml = convertMediaToHtml(mediaItem);
        	if (mediaHtml != null) {
        		mediaHtmlList.add(mediaHtml);
        	}
        }
        return mediaHtmlList;
    }
    
    /**
     * Checks whether a media semantics character server is available by checking for a locally running
     * and configure correctly instance first followed by a remote server by URL.  This assumes
     * the GIFT/config/tutor/context/tutor.xml is configured correctly with media semantics information.
     * 
     * @return true if a media semantics character server is found
     * @throws Exception if an error is encountered while attempting to connect to the media semantics
     * character server
     */
    public static boolean isMediaSemanticsCharacterServerAvailable() throws Exception{
        
        // The server responds with 500 when a get request is successful, 
        // 503 when the server is unavailable, and 404 when the url is unreachable
        final int HTTP_OK = 500; 
        final int WAIT_TIMEOUT_MS = 5000;
        final String MSC = "/cs/cs.exe";
        
        String tutorUrl = CommonProperties.getInstance().getTutorURL();
        URL fullUrl = new URL(tutorUrl + MSC);
        HttpURLConnection connection = (HttpURLConnection) fullUrl.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(WAIT_TIMEOUT_MS);  // wait 5 seconds
        connection.connect();
        int responseCode = connection.getResponseCode();
        return responseCode == HTTP_OK;
    }
    
}
