/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.io;

import java.util.Arrays;
import java.util.List;

/**
 * This class contains the most common constant and simple variables like strings.
 * 
 * @author mhoffman
 *
 */
public class Constants {
    
    public static final String SVN = ".svn";
    public static final String XML = ".xml";
    public static final String PDF = ".pdf";
    public static final String ZIP = ".zip";
    
    /** the backup file suffix */
    public static final String BACKUP_SUFFIX = ".bak";

    public static final String SPACE = " ";
    public static final String ENCODED_SPACE = "%20";
    public static final String PLUS = "+";
    public static final String ENCODED_PLUS = "%2B";
    public static final String AND = "&";
    public static final String ENCODED_AND = "%26";
    public static final String OPEN_SQUARE_BRACKET = "[";
    public static final String ENCODED_OPEN_SQUARE_BRACKET = "%5B";
    public static final String CLOSE_SQUARE_BRACKET = "]";
    public static final String ENCODED_CLOSE_SQUARE_BRACKET = "%5D";
    public static final String OPEN_CURLY_BRACKET = "{";
    public static final String ENCODED_OPEN_CURLY_BRACKET = "%7B";
    public static final String CLOSE_CURLY_BRACKET = "}";
    public static final String ENCODED_CLOSE_CURLY_BRACKET = "%7D";
    public static final String BACK_QUOTE = "`";
    public static final String ENCODED_BACK_QUOTE = "%60";
    public static final String CARET = "^";
    public static final String ENCODED_CARET = "%5E";
    public static final String HTML_ENCODED_AND = "&amp;";
    public static final String OPEN_PARENTHESIS = "(";
    public static final String OPEN_PARENTHESIS_ENCODED = "%28";
    public static final String CLOSE_PARENTHESIS = ")";
    public static final String CLOSE_PARENTHESIS_ENCODED = "%29";
    public static final String LESS_THAN = "<";
    public static final String HTML_ENCODED_LESS_THAN = "&lt;";
    public static final String GREATER_THAN = ">";
    public static final String HTML_ENCODED_GREATER_THAN = "&gt;";
    public static final String EMPTY = "";
    
    public static final String COMMA = ",";
    public static final String PIPE = "|";
    public static final String PERIOD = ".";
    public static final String SINGLE_QUOTE = "'";
    public static final String SEMI_COLON = ";";
    public static final String QUESTION_MARK = "?";
    public static final String EQUALS = "=";
    
    public static final String FORWARD_SLASH = "/";
    public static final String BACKWARD_SLASH = "\\";
    
    public static final String NEWLINE = "\n";
    public static final String HTML_NEWLINE = "<br/>";
    public static final String TAB = "\t";
    public static final String HTML_TAB = "&nbsp;&nbsp;&nbsp;&nbsp;";
    
    public static final String HTTP = "http://";
    public static final String HTTPS = "https://";
    
    public static final String UTF8 = "UTF-8";
    
    //LTI constants
    public static final String USER_ID = "user_id";
    public static final String LTI_CONSUMER_KEY = "cnKey";
    public static final String LTI_CONSUMER_ID = "cnId";
    public static final String LTI_COURSE_ID = "csId";
    public static final String LTI_DATA_SET_ID = "dsId";
    public static final String LTI_OUTCOME_SERVICE_URL = "osUrl";
    public static final String LTI_OUTCOME_SERVICE_ID = "osSrcdid";
    public static final String OAUTH_TIMESTAMP = "oauth_timestamp";
    public static final String OAUTH_CONSUMER_KEY = "oauth_consumer_key";
    public static final String LIS_OUTCOME_SERVICE_URL = "lis_outcome_service_url";
    public static final String LIS_RESULT_SOURCEDID = "lis_result_sourcedid";
    
    public static final String FILE_SERVLET_FILE_KEY = "file";
    public static final String FILE_SERVLET_RESULT_KEY = "result";
    public static final String FILE_SERVLET_RESULT_SUCCESS = "success";
    public static final String FILE_SERVLET_RESULT_FAILURE = "failure";
    public static final String FILE_SERVLET_MESSAGE = "message";
    public static final String FILE_SERVLET_ERROR = "error";
    public static final String FILE_SERVLET_FILE_ALREADY_EXISTS = "fileAlreadyExists";
    public static final String FILE_SERVLET_FILE_UPLOAD_FAILURE = "uploadFailure";
    public static final String FILE_SERVLET_SERVLET_PATH = "servletPath";

    /** the source of a strategy approval being the game master client */
    public static final String GAMEMASTER_SOURCE = "Created by game master";
    /** the source of a strategy approval being an automatic GIFT process */
    public static final String AUTO_APPLIED_BY_GIFT = "Automatically applied by GIFT";
    /** the source of a strategy approval being manually done by the game master */
    public static final String MANUALLY_APPROVED = "Approved by game master";

    public static final String[] VALID_URL_SCHEMES = {HTTPS, HTTP};
    
	/** the file extensions for the PowerPoint shows supported through course training application element */
	public static final String[] ppt_show_supported_types = {".pps", ".ppsx", ".ppsm", ".pptx"};
	
	/** the file extensions for the HTML type files supported through course element */
	public static final String[] html_supported_types = {".htm", ".html"};
	
	/** the file extensions for images supported through authoring */
	public static final String[] image_supported_types = {".png", ".jpg", ".jpeg", ".bmp", ".gif", ".tiff", ".tif"};
	
	/** the file extensions for videos supported through authoring */
    public static final String[] VIDEO = {".mp4"};
	
	/** the file extensions for audio supported through authoring */
    public static final List<String> AUDIO = Arrays.asList(".wav", ".wma", ".mp3", ".ogg");
	
	/** HTML for labels used to indicate required fields */
	public static final String REQUIRED_FIELD_LABEL_HTML = ""
			+ 	"<span style='color: red'>"
			+ 		"(<span style='font-weight: bold;'>*</span> indicates a required field)"
			+ 	"</span>";
	
	public static final String[] YOUTUBE_VIDEO_PATTERNS = 
	    {"^[^v]+v=(.{11}).*", "^[^v]+youtube/(.{11}).*", "^[^v]+youtu.be/(.{11}).*"};
    
    /**
     * Private constructor - all access should be through static class members
     */
    private Constants(){}
    
    /**
     * Checks if the given file name has a file extension matching a known 
     * type of image file
     * 
     * @param fileName the file name to check. If null, false will be returned.
     * @return whether the file name has an image file extension.
     */
    public static boolean isImageFile(String fileName) {
        
        if(fileName == null) {
            return false;
        }
        
        String fileNameLc = fileName.toLowerCase();
        
        for(String extension : Constants.image_supported_types) {
            if(fileNameLc.endsWith(extension)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Checks if the given file name has a file extension matching a known 
     * type of video file
     * 
     * @param fileName the file name to check. If null, false will be returned.
     * @return whether the file name has an video file extension.
     */
    public static boolean isVideoFile(String fileName) {
        
        if(fileName == null) {
            return false;
        }
        
        String fileNameLc = fileName.toLowerCase();
        
        for(String extension : Constants.VIDEO) {
            if(fileNameLc.endsWith(extension)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Checks if the given file name has a file extension matching a known 
     * type of audio file
     * 
     * @param fileName the file name to check. If null, false will be returned.
     * @return whether the file name has an audio file extension.
     */
    public static boolean isAudioFile(String fileName) {
    
        if(fileName == null) {
            return false;
        }
        
        String fileNameLc = fileName.toLowerCase();
        
        for(String extension : Constants.AUDIO) {
            if(fileNameLc.endsWith(extension)) {
                return true;
            }
        }
        
        return false;
    }
}
