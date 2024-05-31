/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.io;

import java.io.File;
import java.net.URI;

/**
 * This class handles references to GIFT documentation.
 * 
 * @author mhoffman
 *
 */
public class HelpDocuments {
    
    private static final String VERSION_NAME = Version.getInstance().getDocumentationToken();
    
    private static final File INSTALL_DOC_FILE = new File("docs"+File.separator+"content"+File.separator+"GIFTInstallInstructionsWin7.htm");
    private static final URI INSTALL_DOC_WEB = URI.create("https://gifttutoring.org/projects/gift/wiki/Install_Instructions_"+VERSION_NAME);
    
    private static final File COURSE_DOC_FILE = new File("docs"+File.separator+"content"+File.separator+"GIFTDomainCourseFile.htm");
    private static final URI COURSE_DOC_WEB = URI.create("https://gifttutoring.org/projects/gift/wiki/Domain_Course_File_"+VERSION_NAME);
    
    private static final File DKF_DOC_FILE = new File("docs"+File.separator+"content"+File.separator+"GIFTDomainKnowledgeFile.htm");
    private static final URI DKF_DOC_WEB = URI.create("https://gifttutoring.org/projects/gift/wiki/Domain_Knowledge_File_"+VERSION_NAME);
    
    private static final File CONFIG_DOC_FILE = new File("docs"+File.separator+"content"+File.separator+"GIFTConfigurationSettings.htm");
    private static final URI CONFIG_DOC_WEB = URI.create("https://gifttutoring.org/projects/gift/wiki/Configuration_Settings_"+VERSION_NAME);
    
    private static final File XML_AUTHORING_DOC_FILE = new File("docs"+File.separator+"content"+File.separator+"GIFTXMLAuthoringTools.htm");
    private static final URI XML_AUTHORING_DOC_WEB = URI.create("https://gifttutoring.org/projects/gift/wiki/XML_Authoring_Tools_"+VERSION_NAME);
    
    private static final File EXPORT_TUTOR_DOC_FILE = new File("docs"+File.separator+"content"+File.separator+"GIFTExportTutor.htm");
    private static final URI EXPORT_TUTOR_DOC_WEB = URI.create("https://gifttutoring.org/projects/gift/wiki/Export_Tutor_"+VERSION_NAME);
    
    private static final File MONITOR_DOC_FILE = new File("docs"+File.separator+"content"+File.separator+"GIFTOperatorStationInstructions.htm");
    private static final URI MONITOR_DOC_WEB = URI.create("https://gifttutoring.org/projects/gift/wiki/GIFT_Operator_Station_Instructions_"+VERSION_NAME);
    
    private static final URI LTI_DOC_WEB = URI.create("https://gifttutoring.org/projects/gift/wiki/Gift_Lti_Integration_"+VERSION_NAME);
    
    private static boolean isServerMode = CommonProperties.getInstance().isServerDeploymentMode();
    
    private HelpDocuments() {}
    
    /**
     * Return the URI of the install documentation to use.
     * 
     * @return if in server mode or the install documentation local file is not present, the online
     * documentation URI is returned
     */
    public static URI getInstallDoc(){
        
        if(isServerMode || !INSTALL_DOC_FILE.exists()){
            return INSTALL_DOC_WEB;
        }else{
            return INSTALL_DOC_FILE.toURI();
        }
    }
    
    /**
     * Return the URI of the course documentation to use.
     * 
     * @return if in server mode or the course documentation local file is not present, the online
     * documentation URI is returned
     */
    public static URI getCourseDoc(){
        
        if(isServerMode || !COURSE_DOC_FILE.exists()){
            return COURSE_DOC_WEB;
        }else{
            return COURSE_DOC_FILE.toURI();
        }
    }
    
    /**
     * Return the URI of the DKF documentation to use.
     * 
     * @return if in server mode or the DKF documentation local file is not present, the online
     * documentation URI is returned
     */
    public static URI getDKFDoc(){
        
        if(isServerMode || !DKF_DOC_FILE.exists()){
            return DKF_DOC_WEB;
        }else{
            return DKF_DOC_FILE.toURI();
        }
    }
    
    /**
     * Return the URI of the configuration settings documentation to use.
     * 
     * @return if in server mode or the configuration settings documentation local file is not present, the online
     * documentation URI is returned
     */
    public static URI getConfigSettingsDoc(){
        
        if(isServerMode || !CONFIG_DOC_FILE.exists()){
            return CONFIG_DOC_WEB;
        }else{
            return CONFIG_DOC_FILE.toURI();
        }
    }
    
    /**
     * Return the URI of the xml authoring tools documentation to use.
     * 
     * @return if in server mode or the xml authoring tools documentation local file is not present, the online
     * documentation URI is returned
     */
    public static URI getXMLAuthoringToolDoc(){
        
        if(isServerMode || !XML_AUTHORING_DOC_FILE.exists()){
            return XML_AUTHORING_DOC_WEB;
        }else{
            return XML_AUTHORING_DOC_FILE.toURI();
        }
    }
    
    /**
     * Return the URI of the export tutor documentation to use.
     * 
     * @return if in server mode or the export tutor documentation local file is not present, the online
     * documentation URI is returned
     */
    public static URI getExportTutorDoc(){
        
        if(isServerMode || !EXPORT_TUTOR_DOC_FILE.exists()){
            return EXPORT_TUTOR_DOC_WEB;
        }else{
            return EXPORT_TUTOR_DOC_FILE.toURI();
        }
    }
    
    /**
     * Return the URI of the monitor documentation to use.
     * 
     * @return if in server mode or the monitor documentation local file is not present, the online
     * documentation URI is returned
     */
    public static URI getMonitorDoc(){
        
        if(isServerMode || !MONITOR_DOC_FILE.exists()){
            return MONITOR_DOC_WEB;
        }else{
            return MONITOR_DOC_FILE.toURI();
        }
    }
    
    /**
     * Return the URI of the lti documentation to use.  Lti implementation documentation is only online
     * since the feature is an online only feature.
     * 
     * @return the online documentation URI for the LTI implementation.
     */
    public static URI getLtiDoc() {
        return LTI_DOC_WEB;
    }

}
