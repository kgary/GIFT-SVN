/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generated.course.LtiProvider;
import mil.arl.gift.common.PackageUtil;
import mil.arl.gift.common.enums.DeploymentModeEnum;
import mil.arl.gift.common.enums.LessonLevelEnum;
import mil.arl.gift.common.lti.LtiJsonParser;
import mil.arl.gift.common.lti.TrustedLtiConsumer;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.net.util.Util;

/**
 * This contains the common GIFT properties as well as the property file parser.
 *
 * @author mhoffman
 *
 */
public class CommonProperties {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(CommonProperties.class);

    /** common property values*/
    protected static final String TRUE = "true";
    protected static final String YES = "yes";

    /** property keys*/
    private static final String DOMAIN_DIRECTORY = "DomainDirectory";
    private static final String TRAINING_APPS_DIRECTORY = "TrainingAppsDirectory";
    private static final String EXPORT_DIRECTORY = "ExportDirectory";
    private static final String IMPORT_DIRECTORY = "ImportDirectory";
    private static final String UPLOAD_DIRECTORY = "UploadDirectory";
    private static final String EXPERIMENT_DIRECTORY = "ExperimentDirectory";
    private static final String SENSOR_CONFIG_DIRECTORY = "SensorConfigDirectory";
    private static final String LEARNER_CONFIG_DIRECTORY = "LearnerConfigDirectory";
    private static final String PED_CONFIG_DIRECTORY = "PedagogicalConfigDirectory";
    private static final String GATEWAY_CONFIG_DIRECTORY = "GatewayConfigDirectory";
    private static final String DATABASE_BACKUPS_PATH_PROPERTY = "Database_Backups_Path";
    private static final String GIFT_WEBSITE = "GIFTWebsite";

    private static final String GIFT_ADMIN_SERVER_URL = "GiftAdminServerUrl";
    private static final String EVENT_REPORT_TOOL_PATH = "EventReportToolPath";
    private static final String AUTHORING_TOOL_PATH = "AuthoringToolPath";
    private static final String DASHBOARD_PATH = "DashboardPath";
    private static final String TUTOR_URL = "TutorURL";
    private static final String TUTOR_WEB_SOCKET_URL = "TutorWebSocketUrl";
    private static final String ASAT_URL = "ASAT_URL";
    private static final String AUTOTUTOR_ACE_URL = "AutoTutor_ACE_URL";
    private static final String AUTHORITATIVE_SYSTEM_URL="AuthoritativeSystemUrl";
    private static final String TRADEM_URL = "TRADEM_URL";
    private static final String METRICS_URL = "MetricsURL";
    private static final String JMX_URL = "JmxURL";

    private static final String VALIDATE_COURSE_AT_COURSE_LIST_REQUEST = "ValidateCoursesAtCourseListRequest";
    private static final String VALIDATE_SURVEY_AT_COURSE_LIST_REQUEST = "ValidateSurveysAtCourseListRequest";
    private static final String APPLY_LMS_RECORDS_AT_COURSE_LIST_REQUEST = "ApplyLMSRecordsAtCourseListRequest";
    private static final String CIPHOR_PASSWORD = "CiphorPassword";

    public static final String DEPLOYMENT_MODE = "DeploymentMode";
    public static final String IS_REMOTE_GATEWAY = "IsRemoteGateway";
    public static final String CMS_ADMIN_USER = "CMS_ADMIN_USER";
    public static final String CMS_URL = "CMS_URL";
    public static final String CMS_SECRET_KEY = "CMS_SECRET_KEY";
    public static final String CMS_USER_WORKSPACE_QUOTA = "CMS_USER_WORKSPACE_QUOTA";
    public static final String USE_HTTPS = "UseHttps";
    public static final String USE_PORT_FORWARDING = "UsePortForwarding";
    public static final String ENABLE_SERVER_METRICS = "EnableServerMetrics";
    public static final String ENABLE_STRESS_MODE = "EnableStressMode";
    public static final String STRESS_TOKEN = "StressToken";
    public static final String BYPASS_SURVEY_PERMISSION_CHECK = "BypassSurveyPermissionCheck";
    public static final String TRUSTED_LTI_CONSUMERS = "TrustedLtiConsumers";
    public static final String TRUSTED_LTI_PROVIDERS = "TrustedLtiProviders";
    public static final String LTI_TIMEOUT_DASHBOARD_MS = "LtiTimeoutDashboardMs";
    public static final String LTI_TIMEOUT_TUTOR_MS = "LtiTimeoutTutorMs";
    public static final String LTI_URL_SUBPATH = "LtiUrlSubPath";
    public static final String LTI_CONSUMER_URL_SUBPATH = "LtiConsumerUrlSubPath";
    public static final String GOOGLE_MAPS_API_KEY = "GoogleMapsApiKey";
    public static final String READ_ONLY_USER = "ReadOnlyUser";
    public static final String LESSON_LEVEL = "LessonLevel";
    
    /**
     * The client_id, as specified within the remote gateway zip in the config folder.
     * Generated from the initialize() method of BaseDomainSession.java
     * Fetched with calls to the getClientId() method within CommonProperties.java
     * 
     */
    public static final String CLIENT_ID = "ClientId";

    /**
     * Specifies the delay (in milliseconds) between when an assessment
     * notification is fired and when it can be fired again. This is a specific
     * property geared towards Game Master (and also used by ARES) to limit the
     * number of back-to-back notifications for the OC. Any notifications that
     * happen during the delay will be ignored; for example, if a learner state
     * toggles between At/Above and Below Expectation a few times in rapid
     * succession, only the first Below Expectation will trigger a notification
     * and the next few changes within the delay will not fire a notification.
     */
    public static final String ASSESSMENT_NOTIFICATION_DELAY_MS = "AssessmentNotificationDelayMs";
    /** The default value for {@link #ASSESSMENT_NOTIFICATION_DELAY_MS} */
    private static final int DEFAULT_ASSESSMENT_NOTIFICATION_DELAY_MS = 5000;

    public static final String ENABLE_CLIENT_ANALYTICS = "EnableClientAnalytics";
    public static final String CLIENT_ANALYTICS_URL = "ClientAnalyticsUrl";


    public static final String WINDOW_TITLE = "WindowTitle";
    public static final String DEFAULT_WINDOW_TITLE = "GIFT " + Version.getInstance().getName();

    /** The property that containst he value for the message ack timeout in the common.properties file */
    public static final String MESSAGE_ACK_TIMEOUT_MS = "MessageAckTimeoutMs";
    /** The property that contains the value for the module status monitor timeout in the common.properties file */
    protected static final String MODULE_STATUS_TIMEOUT_MS = "ModuleStatusMonitorTimeoutMs";
    /** Default timeout for the module status heartbeat */
    private static final int DEFAULT_MODULE_TIMEOUT_MS = 10000;

    private static final String DEFAULT_DOMAIN_DIRECTORY = "../Domain";

    /** The default path of the Training.Apps directory */
    private static final String DEFAULT_TRAINING_APPS_DIRECTORY = "../Training.Apps";

    /** The default timeout before the lti launch request is considered stale when connecting to the dashboard server. */
    private static final int DEFAULT_LTI_TIMEOUT_DASHBOARD_MS = 10000;
    /** The default timeout before the lti launch request is considered stale when connecting to the tutor server. */
    private static final int DEFAULT_LTI_TIMEOUT_TUTOR_MS = 300000;

    /** Default ack timeout for messages if not specified in the common.properties file */
    private static final int DEFAULT_ACK_TIMEOUT_MS = 10000;

    public static final String WORKSPACE = "workspace";

    /** delimiter used to separate entries in a list */
    private static final String DELIM = ",";

    private static final String COMMON_PROP_FILE = "common.properties";
    
    /** The properties file found in the remote Gateway zip that is used to connect to Domain session */
    private static final String REMOTE_GATEWAY_PROP_FILE = "gateway.remote.properties";

    private static final String RUNTIME_DIR_NAME = "runtime";
    private static final String TEMPLATE_DIR_NAME = "templates";
    private static final String RESOURCE_DIR_NAME = "resources";
    private static final String EXTERNAL_DIR_NAME = "external";

    private static final String DOMAIN_CONTENT_SERVER_PORT = "DomainContentServerPort";
    private static final String DOMAIN_CONTENT_SERVER_HOST = "DomainContentServerHost";

    private static final String DASHBOARD_MEMORY_SERVLET_SUBPATH = "DashboardMemoryFileServletSubPath";

    private static final String DEFAULT_CHARACTER = "DefaultCharacter";
    
    /** the current file servlet path for session output folder  */
    private static final String DEFAULT_SESSION_OUTPUT_SERVER_PATH = "/dashboard/recorder/output/domainSessions/";
    
    private static final String EXTERNAL_STRATEGY_PROVIDER_URL = "ExternalStrategyProviderUrl";
    
    /**
     * the URL of the service providing military symbols (e.g. 2525C)
     */
    public static final String MILITARY_SYMBOL_SERVICE_URL = "MilitarySymbolServiceURL";

    private static final int NO_PORT_DEFINED = 0;

    /** collection of usernames that are not allowed in this gift instance (e.g. Public). */
    private static final Set<String> prohibitedUsernames = new HashSet<String>(1);
    static{
        prohibitedUsernames.add("public");             // because there is a 'Public' workspace
        prohibitedUsernames.add("workspaces");         // 2nd level folder of Nuxeo file system path
        prohibitedUsernames.add("default-domain");     // root of Nuxeo file system path
        prohibitedUsernames.add("templates");          // currently only in GIFT Desktop Domain/workspaces/
        prohibitedUsernames.add("administrator");      // nuxeo - the admin user we use to login to the nuxeo dashboard
        prohibitedUsernames.add("administrators");     // nuxeo - group that that Administrators is in
        prohibitedUsernames.add("members");            // nuxeo - group that everyone is in (GIFT controlled)
        prohibitedUsernames.add("everyone");           // nuxeo - inherited group that everyone is in (assumption, not under GIFT control)
    }

    /** java properties parser which reads the properties file */
    private Properties properties = new Properties();

    /** the property file name */
    private String propFileName = null;

    /** the singleton instance of this class */
    private static CommonProperties instance = null;

    /** Flag indicating if the JRE is 64-bit */
    private Boolean isJRE64Bit = null;
    
    /** Flag indicating if a valid CLIENT_ID exists within the remote gateway zip */
    private Boolean isClientIdValid;

    /**
     * Returns the singleton instance of this class.  This instance will only have access to the common
     * properties.
     *
     * @return CommonProperties
     */
    public static synchronized CommonProperties getInstance(){

        if(instance == null){
            instance = new CommonProperties();
        }

        return instance;
    }

    /**
     * Just read the common properties, no other property files
     */
    private CommonProperties(){
        finishInit();
    }

    /**
     * Class constructor - create parser instance
     *
     * @param propertyFileNames - one of more property file names relative to the GIFT/config directory.
     *                        Note: the list is a descending priority list where a property value found in a
     *                        file with a lower index will not be over-written by a file with a higher index.
     *                        The first file in the list will also be used for refreshing and updating the file's properties on disk.
     */
    protected CommonProperties(String... propertyFileNames){

        if (propertyFileNames == null ) {
            throw new IllegalArgumentException("The property file names can't be null.");
        }else if(propertyFileNames.length == 0){
            throw new IllegalArgumentException("The property file names can't be empty.");
        }

        for(int index = 0; index < propertyFileNames.length; index++){

            try{
                String fileName = extendProperties(propertyFileNames[index]);

                if(propFileName == null){
                    propFileName = fileName;
                }
            }catch(FileNotFoundException e){
                //this file was not found

                if(index + 1 < propertyFileNames.length){
                    //there is another file to check, ignore the issue
                    //Note: this logic could be improved but this allows us to run junits using DashboardServiceImpl.java (see #3140)
                    continue;
                }else{
                    e.printStackTrace();
                    logger.error("Failed to find the property file '"+propertyFileNames[index]+"'.", e);
                }
            }

            //TESTING
            //System.out.println("read file = "+propertyFileNames[index]+", Property 'x' = "+properties.getProperty("x"));
        }

        finishInit();
    }

    private void finishInit() throws RuntimeException{

        //extend the property entries with the common properties
        try{
            extendProperties(COMMON_PROP_FILE);
        }catch(FileNotFoundException e){
            throw new RuntimeException(e);
        }

        //TESTING
        //System.out.println("read file = "+COMMON_PROP_FILE+", Property 'x' = "+properties.getProperty("x"));

        checkRequired();
    }

    /**
     * Check required properties.
     *
     * @throws RuntimeException if there was a problem with one of the required property values
     */
    private void checkRequired() throws RuntimeException{

        DeploymentModeEnum mode = getDeploymentMode();
        if(mode == DeploymentModeEnum.SERVER){
            String cmsURL = getCMSURL();

            if(cmsURL == null || cmsURL.isEmpty()){
                throw new RuntimeException("The CMS URL property value can't be null or empty.");
            }
        }
    }

    /**
     * Convert the relative property file name into the appropriate file name needed to find the file.
     *
     * @param propertyFileName - property file name relative to the GIFT/config directory
     * @return String - file name needed by Java to find the file on disk
     */
    private String buildFileName(String propertyFileName){

        String fileName;

        // Attempt to get the file off the class path first
        //Note: can't use File.separator here because it caused Tutor module to throw file not found exception
        URL fileUrl = getClass().getResource("/" + propertyFileName);

        // If this is not null, the property file was found on the classpath
        if (fileUrl != null) {
            try {

                //Get the absolute path to the file on the disk
                //Note: need to replace '+' in file path to their UTF-8 hexadecimal representation,
                //otherwise Java's URLDecoder automatically decodes '+' in URLs as spaces.
                fileName = URLDecoder.decode(fileUrl.getFile().replaceAll("\\+", "%2b"), "UTF-8");

            } catch (@SuppressWarnings("unused") UnsupportedEncodingException ex) {

                logger.warn("Tried to decode property file name with UTF-8, but it is not supported. Using undecoded file name instead.");

                fileName = fileUrl.getFile();
            }

            logger.info("Found the property file of "+propertyFileName+" on the classpath.");

        } else {
            fileName = PackageUtil.getConfiguration() + File.separator + propertyFileName;
        }

        return fileName;
    }

    /**
     * Add, but don't override, properties from the given file.
     *
     * @param propertyFileName - property file name relative to the GIFT/config directory
     * @return String - the actual file name used to find the file.
     * @throws FileNotFoundException if the file specified could not be found
     */
    protected String extendProperties(String propertyFileName) throws FileNotFoundException{

        if (propertyFileName == null) {
            throw new IllegalArgumentException("The property file name can't be null");
        }

        String fileName = buildFileName(propertyFileName);

        readProperties(fileName, false);

        return fileName;
    }

    /**
     * Refresh the properties values by re-parsing the properties file
     */
    public void refresh(){

        try {
            readProperties(propFileName, true);
        } catch (@SuppressWarnings("unused") FileNotFoundException e) {
            throw new IllegalArgumentException("The property file "+propFileName+" doesn't exist");
        }
    }

    /**
     * Read the properties file
     *
     * @param propertyFileName - file name needed by Java to find the file on disk
     * @param overwriteEntries - whether or not to over-ride any current property entries
     * @throws FileNotFoundException
     */
    private void readProperties(String propertyFileName, boolean overwriteEntries) throws FileNotFoundException{

        if(logger.isInfoEnabled()){
            logger.info("Reading properties file named "+propertyFileName);
        }

        InputStream iStream = FileFinderUtil.getFileByClassLoader(propertyFileName);
        if(iStream != null){
            readProperties(iStream, overwriteEntries);
        }else{
            readProperties(new FileInputStream(propertyFileName), overwriteEntries);
        }

        if(logger.isInfoEnabled()){
            logger.info("Finished reading properties file");
        }
    }

    /**
     * Reads the properties from a stream
     *
     * @param propertyInputStream The input stream to read properties from
     * @param overwriteEntries - whether or not to over-ride any current property entries
     */
    private void readProperties(InputStream propertyInputStream, boolean overwriteEntries)  {

        logger.info("Reading properties from stream");

        //load a properties file
        try {
            if(overwriteEntries || properties.isEmpty()){
                properties.load(propertyInputStream);
            }else{
                Properties otherProps = new Properties();
                otherProps.load(propertyInputStream);

                for(Object key : otherProps.keySet()){

                    if(!properties.containsKey(key)){
                        properties.put(key, otherProps.get(key));
                    }else{
                        logger.info("Skipping extension property of "+key+" : "+otherProps.get(key)+" because a value already exists of "+properties.get(key));
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Caught exception while trying to read properties file from input stream", e);
        } finally {
            try {
                if (propertyInputStream != null) {
                    propertyInputStream.close();
                }
            } catch (@SuppressWarnings("unused") IOException e) {
            }
        }

        logger.info("Finished reading properties stream");
    }

    /**
     * Return the property value for the given property name
     *
     * @param propertyName - the name of the property to get its value
     * @return String - the property's value, null if property is not found
     */
    public String getPropertyValue(String propertyName){

        return properties.getProperty(propertyName);
    }

    /**
     * Return the property value for the given property name
     *
     * @param propertyName - the name of the property to get its value
     * @param defaultValue - the value to use if the property is not found.
     * @return String - the property's value, use the default value if the property is not found
     */
    public String getPropertyValue(String propertyName, String defaultValue){

        if(properties.containsKey(propertyName)){
            return properties.getProperty(propertyName);
        }else{
            return defaultValue;
        }
    }

    /**
     * Return the value of the property with the given name as an array.
     * This is useful for properties that have a list of delimited entries.
     *
     * @param propertyName - the name of the property to get its value
     * @return String[] - the array of delimited entries for the named property, null if the property was not found
     */
    public String[] getPropertyArray(String propertyName) {

        String fullValue = getPropertyValue(propertyName);

        if (fullValue != null) {

            return fullValue.split(DELIM);
        }

        return null;
    }

    /**
     * Return the boolean property value for the given property name
     *
     * @param propertyName - the name of the property to get its value
     * @param defaultValue The default value to return when the property is not
     * set
     * @return boolean - the value as a boolean.
     */
    public boolean getPropertyBooleanValue(String propertyName, boolean defaultValue) {

        String value = getPropertyValue(propertyName);

        if (value != null) {

            //remove trailing white space that could have accidently been added while changing property value which will cause
            //the equals condition to fail
            value = value.trim();

            return value.equalsIgnoreCase(TRUE) || value.equalsIgnoreCase(YES);

        } else {

            return defaultValue;
        }
    }

    /**
     * Return the boolean property value for the given property name
     *
     * @param propertyName - the name of the property to get its value
     * @return boolean - the value as a boolean.  If the value is null, false is returned.
     */
    public boolean getPropertyBooleanValue(String propertyName) {

        return getPropertyBooleanValue(propertyName, false);
    }

    /**
     *
     * Generic method for getting an integer property.
     *
     * @param propertyName name of property to read
     * @param defaultValue default value to return in case property is missing
     *        or corrupt
     * @return integer value of the property. If property is not set or property
     *         value is not parsable to an int, returns the provided default
     *         value.
     */
    public int getPropertyIntValue(String propertyName, int defaultValue) {

        int value = defaultValue;
        String errorMessage = null;

        String valueAsString = getPropertyValue(propertyName);

        if( valueAsString != null ) {

            try {

                value = Integer.parseInt(valueAsString);

            }catch(@SuppressWarnings("unused") NumberFormatException ex) {
                errorMessage = "ERROR parsing value of '" + propertyName + "' property. Unparsable string is: " + valueAsString;
                System.err.println(errorMessage);
            }

        }

        return value;
    }

    /**
     *
     * Generic method for getting an double property.
     *
     * @param propertyName name of property to read
     * @param defaultValue default value to return in case property is missing or corrupt
     * @return double value of the property. If property is not set or property value is not parsable to a double, returns the provided default value.
     */
    public double getPropertyDoubleValue(String propertyName, double defaultValue) {

        double value = defaultValue;
        String errorMessage = null;

        String valueAsString = getPropertyValue(propertyName);

        if( valueAsString != null ) {

            try {

                value = Double.parseDouble(valueAsString);

            }catch(@SuppressWarnings("unused") NumberFormatException ex) {
                errorMessage = "ERROR parsing value of '" + propertyName + "' property. Unparsable string is: " + valueAsString;
                System.err.println(errorMessage);
            }

        }

        return value;
    }

    /**
     * Checks if the JRE is running in 64-bit.
     * https://docs.oracle.com/javame/config/cdc/cdc-opt-impl/ojmeec/1.1/architecture/html/properties.htm#g1001328
     * 
     * @return true if running in 64-bit; false otherwise.
     */
    public boolean isJRE64Bit() {
        if (isJRE64Bit == null) {
            String platformArch = System.getProperty("sun.arch.data.model");
            isJRE64Bit = StringUtils.isNotBlank(platformArch) && StringUtils.equals(platformArch, "64");
        }

        return isJRE64Bit.booleanValue();
    }

    /**
     * Returns the directory path specified to contain the domain files
     *
     * @return The directory path (e.g. "../Domain")
     */
    public String getDomainDirectory() {
        return getPropertyValue(DOMAIN_DIRECTORY, DEFAULT_DOMAIN_DIRECTORY);
    }

    /**
     * Returns the directory path specified to contain the training application files
     *
     * @return The directory path (e.g. "../Training.Apps")
     */
    public String getTrainingAppsDirectory() {
        return getPropertyValue(TRAINING_APPS_DIRECTORY, DEFAULT_TRAINING_APPS_DIRECTORY);
    }

    /**
     * Returns the workspace directory under the domain directory.
     *
     * @return the directory path (e.g. "../Domain/workspace")
     */
    public String getWorkspaceDirectory() {
        return getDomainDirectory() + File.separator + WORKSPACE;
    }

    /**
     * Returns the name of the workspace directory folder.  This only returns
     * the name of the folder (not a path).
     * @return the name of the workspace folder (not the path).
     */
    public String getWorkspaceDirectoryName() {
        return WORKSPACE;
    }

    /**
     * Returns the directory path specified to contain the runtime domain files
     *
     * @return the directory path (e.g. "../Domain/runtime")
     */
    public String getDomainRuntimeDirectory(){
        return getDomainDirectory() + File.separator + RUNTIME_DIR_NAME;
    }

    /**
     * Returns the directory path to the domain resource directory
     *
     * @return the directory path (e.g. "../Domain/resources")
     */
    public String getDomainResourceDirectory(){
        return getDomainDirectory() + File.separator + RESOURCE_DIR_NAME;
    }

    /**
     * Returns the directory path to the domain resource external directory
     *
     * @return the directory path (e.g. "../Domain/resources/external")
     */
    public String getDomainExternalDirectory(){
        return getDomainResourceDirectory() + File.separator + EXTERNAL_DIR_NAME;
    }

    /**
     * Returns the directory path specified to contain the template domain files
     *
     * @return the directory path (e.g. "../Domain/workspace/template")
     */
    public String getWorkspaceTemplatesDirectory(){
        return getWorkspaceDirectory() + File.separator + TEMPLATE_DIR_NAME;
    }

    /**
     * Returns the directory path specified to contain the export files
     *
     * @return The directory path
     */
    public String getExportDirectory() {
        return getPropertyValue(EXPORT_DIRECTORY);
    }

    /**
     * Returns the directory path specified to contain any uploaded files
     *
     * @return The directory path
     */
    public String getUploadDirectory() {
        return getPropertyValue(UPLOAD_DIRECTORY);
    }

    /**
     * Returns the directory path specified to contain any experiment course folders
     *
     * @return The directory path
     */
    public String getExperimentsDirectory() {
        return getPropertyValue(EXPERIMENT_DIRECTORY);
    }

    /**
     * Returns the directory path specified to contain the import files
     *
     * @return The directory path
     */
    public String getImportDirectory() {
        return getPropertyValue(IMPORT_DIRECTORY);
    }

    /**
     * Returns the directory path specified to contain the sensor configuration files
     *
     * @return The directory path
     */
    public String getSensorConfigDirectory() {
        return getPropertyValue(SENSOR_CONFIG_DIRECTORY);
    }

    /**
     * Returns the directory path specified to contain the learner configuration files
     *
     * @return The directory path
     */
    public String getLearnerConfigDirectory() {
        return getPropertyValue(LEARNER_CONFIG_DIRECTORY);
    }

    /**
     * Returns the directory path specified to contain the pedagogical configuration files
     *
     * @return The directory path
     */
    public String getPedConfigDirectory() {
        return getPropertyValue(PED_CONFIG_DIRECTORY);
    }

    /**
     * Returns the directory path specified to contain the gateway configuration files
     *
     * @return The directory path
     */
    public String getGatewayConfigDirectory() {
        return getPropertyValue(GATEWAY_CONFIG_DIRECTORY);
    }

    /**
     * Gets the path to the directory to put and get from database backups
     *
     * @return String The path to the directory for database backups
     */
    public String getDatabaseBackupsDirectory() {
        return getPropertyValue(DATABASE_BACKUPS_PATH_PROPERTY);
    }

    /**
     * Gets the URL to the GIFT web page
     *
     * @return String The URL
     */
    public String getGIFTWebsite() {
        return getPropertyValue(GIFT_WEBSITE);
    }

    /**
     * Gets the URL of the GIFT Admin Server
     * If this is undefined then the Survey System and ERT cannot be launched
     * @return String The URL of the GIFT Admin Server
     */
    public String getGiftAdminServerUrl() {
        return this.getPropertyValue(GIFT_ADMIN_SERVER_URL);
    }

    /**
     * Gets the path on the GAS to access the Event Report Tool
     * If this is undefined then the Event Report Tool cannot be launched
     * @return String The path on the GAS to access the Event Report Tool
     */
    public String getEventReportToolPath() {
        return this.getPropertyValue(EVENT_REPORT_TOOL_PATH);
    }

    /**
     * Return the URL of the Event Report Tool (ERT)
     * e.g. http://localhost:8080/ert
     *
     * @return the URL of the ERT
     */
    public String getEventReportToolURL(){
        return this.getGiftAdminServerUrl() + "/" + this.getEventReportToolPath();
    }

    /**
     * Gets the path on the GAS to access the Event Report Tool
     * If this is undefined then the Event Report Tool cannot be launched
     * @return String The path on the GAS to access the Event Report Tool
     */
    public String getGiftAuthorToolPath() {
        return this.getPropertyValue(AUTHORING_TOOL_PATH);
    }

    /**
     * Gets the port to bind the file server that hosts training material to
     * (e.g. 8885)
     *
     * @return int The training material file server's port
     */
    public int getDomainContentServerPort() {
        return this.getPropertyIntValue(DOMAIN_CONTENT_SERVER_PORT, NO_PORT_DEFINED);
    }

    /**
     * Gets the host name of file server that hosts training material to.
     * (e.g. 10.1.21.123)
     *
     * @return String The training material file server's host name
     * or localhost if not set in the property file
     */
    public String getDomainContentServerHost() {
        return getPropertyValue(DOMAIN_CONTENT_SERVER_HOST, getLocalHostAddress());
    }
    
    /**
     * Return the default domain session output file servlet path that can be used
     * to retrieve video/audio files to play in UIs like the Game Master.
     * @return {@link #DEFAULT_SESSION_OUTPUT_SERVER_PATH}
     */
    public String getSessionOutputServerPath(){
        return DEFAULT_SESSION_OUTPUT_SERVER_PATH;
    }

    /**
     * Return the domain content server (currently jetty instance) address as specified by the
     * host and port domain properties.  Note this address will not end with the forward slash needed
     * to reference a file path on that server.
     * (e.g. http://10.1.21.123:8885)
     *
     * @return String
     */
    public String getDomainContentServerAddress(){

        String host = getDomainContentServerHost();
        int port = getDomainContentServerPort();

        String protocol = Constants.HTTP;
        String url = "";

        if (shouldUseHttps()) {
            protocol = Constants.HTTPS;
        }

        if (shouldUsePortForwarding()) {
            url = protocol + host;
        } else {
            url = protocol + host + ":" + port;
        }

        return url;
    }

    /**
     * Gets the subpath of the dashboard memory file servlet.   This should match the sub path that is in the servlet
     * mapping of src\mil\arl\gift\tools\dashboard\war\WEB-INF\web.xml.
     *
     * @return String The subpath of the memory file servlet if found.  An empty string returned if not found.
     */
    public String getDashboardMemoryFileServletSubPath() {
        return this.getPropertyValue(DASHBOARD_MEMORY_SERVLET_SUBPATH, "");
    }

    /**
     * Return the URL of the gift authoring tool (GAT)
     * e.g. http://localhost:8080/gat
     *
     * @return the URL of the GAT
     */
    public String getGiftAuthorToolURL(){
        return this.getGiftAdminServerUrl() + "/" + this.getGiftAuthorToolPath();
    }

    /**
     * Gets the path on the GAS to access the GIFT Dashboard
     * If this is undefined then the Dashboard cannot be launched
     * @return String The path on the GAS to access the Dashboard
     */
    public String getDashboardPath() {
        return this.getPropertyValue(DASHBOARD_PATH);
    }

    /**
     * Return the URL of the dashboard
     * e.g. http://localhost:8080/dashboard
     *
     * @return the URL of the dashboard
     */
    public String getDashboardURL(){
        return this.getGiftAdminServerUrl() + "/" + this.getDashboardPath();
    }

    /**
     * Return the URL of the tutor
     * e.g. http://localhost:8090/tutor
     *
     * @return the URL of the tutor
     */
    public String getTutorURL(){
        return getPropertyValue(TUTOR_URL);
    }

    /**
     * Return the URL of the tutor web socket service.
     * e.g. ws://localhost:8090/tuiws
     *  or  wss://localhost:8090/tuiws
     *
     * @return the URL of the tutor web socket service
     */
    public String getTutorWebSocketURL(){
        return getPropertyValue(TUTOR_WEB_SOCKET_URL);
    }

    /**
     * Return the AutoTutor script authoring tool URL.
     * If no value is provided, the control panel launch button for ASAT will be disabled.
     *
     * @return the URL for the ASAT
     */
    public String getASATURL(){
        return this.getPropertyValue(ASAT_URL);
    }

    /**
     * Return the AutoTutor ACE engine URL (responsible for handling AutoTutor script execution)
     * If no value is provided or the URL can't be reached, AutoTutor courses will not run on this GIFT instance
     *
     * @return the URL for the AutoTutor ACE engine
     */
    public String getAutoTutorACEURL(){
        return getPropertyValue(AUTOTUTOR_ACE_URL);
    }

    /**
     * Return the URL of the competency framework
     * 
     *
     * @return the URL for the competency framework
     */
    public String getAuthoritativeSystemUrl(){
        return getPropertyValue(AUTHORITATIVE_SYSTEM_URL);
    }

    /**
     * Return the TRADEM URL.
     * If no value is provided, the launch button will be disabled.
     *
     * @return String the URL for TRADEM
     */
    public String getTRADEMURL(){
        return this.getPropertyValue(TRADEM_URL);
    }

    /**
     * Return the local host address of this machine.
     *
     * @return String - local host address (e.g. 127.0.0.1, 10.1.21.123)
     */
    public String getLocalHostAddress(){
        return Util.getLocalHostAddress().getHostAddress();
    }

    /**
     * Return the Content Management System URL.
     *
     * @return CMS URL (e.g. http://10.1.21.172:8080/nuxeo/).  Can't be null if
     * the deployment mode is server.
     */
    public String getCMSURL(){
        return getPropertyValue(CMS_URL);
    }

    /**
     * Get the CMS secret key
     * @return CMS secret key
     */
    public String getCMSSecretKey() {
        return getPropertyValue(CMS_SECRET_KEY);
    }

    /**
     * Get the CMS User Workspace quota size in bytes. Note that -1 means unlimited quota
     * @return Quota size in bytes or Integer.MIN_VALUE if not defined.
     */
    public int getCMSUserWorkspaceQuota() {
        int quotaSize = Integer.MIN_VALUE;
        String quotaString = getPropertyValue(CMS_USER_WORKSPACE_QUOTA);
        if (quotaString != null) {
            try {
                quotaSize = Integer.parseInt(quotaString);
            } catch (@SuppressWarnings("unused") Exception e) {
                logger.warn("Invalid CMS quota string: {}", quotaString);
            }
        }

        return quotaSize;
    }

    /**
     * Get the user that has permissions to perform admin operations in the CMS
     * @return Admin user name
     */
    public String getCMSAdminUser() {
        return getPropertyValue(CMS_ADMIN_USER);
    }

    /**
     * Returns the deployment mode for GIFT (Server, Experiment, Desktop)
     *
     * @return DeploymentModeEnumgetDeploymentMode - deployment mode
     */
    public DeploymentModeEnum getDeploymentMode() {
    	return DeploymentModeEnum.valueOf(getPropertyValue(DEPLOYMENT_MODE));
    }
    
    /**
     * Returns the lesson level for GIFT.
     * This value determines how training material should be presented in a learner lesson
     * Different levels will alter the logic that some modules use to authenticate learners and handle real-time assessment logic
     * Possible values: 
     *  Course  - uses the Tutor module to present training material within a browser
     *  RTA - uses an external training application to present training material
     *
     * @return the lesson level, defaults to LessonLevelEnum.COURSE
     */
    public LessonLevelEnum getLessonLevel() {
        return LessonLevelEnum.valueOf(getPropertyValue(LESSON_LEVEL, LessonLevelEnum.COURSE.getName()));
    }
    
    /**
     * Return whether the lesson level for GIFT is RTA.
     * @return true if the set lesson level is {@link LessonLevelEnum.RTA}.  The default is {@link LessonLevelEnum.COURSE}.
     */
    public boolean isRTALessonLevel(){
        return LessonLevelEnum.RTA.equals(getLessonLevel());
    }

    /**
     * Return true if the deployment mode is Server.
     *
     * @return whether the deployment mode property is server value
     */
    public boolean isServerDeploymentMode(){
        return getDeploymentMode() == DeploymentModeEnum.SERVER;
    }

    /**
     * Returns the ciphor password for encrypting and decrypting the user's password
     *
     * @return the ciphor password
     */
    public String getCiphorPassword() {
        return getPropertyValue(CIPHOR_PASSWORD);
    }

    /**
     * Returns whether courses should be validated when a course list is requested.
     *
     * Note: setting this property to true means that the course will be checked against the course schema
     * file but it won't be checked against GIFT validation logic (i.e. is the referenced website/file/etc reachable).
     *
     * @return boolean if the courses found should be validated when a course list request is requested (DEFAULT: false)
     */
    public boolean shouldValidateCourseContentAtCourseListRequest(){
        return getPropertyBooleanValue(VALIDATE_COURSE_AT_COURSE_LIST_REQUEST, false);
    }

    /**
     * Returns whether all survey references in all courses be validated when a
     * course list request is requested.
     *
     * Note: setting this property to true means that the survey element
     * references in the course (and its DKFs) will not be checked against the
     * connected survey system.
     *
     * @return boolean if the survey references found should be validated when a
     *         course list request is requested (DEFAULT: false)
     */
    public boolean shouldValidateSurveyAtCourseListRequest(){
        return getPropertyBooleanValue(VALIDATE_SURVEY_AT_COURSE_LIST_REQUEST, false);
    }

    /**
     * Returns whether LMS records found for a user should be applied to the courses the user can run when
     * a course list is requested.
     *
     * @return boolean if the LMS records should be applied to the course list (DEFAULT: true)
     */
    public boolean shouldApplyLMSRecordsAtCourseListRequest(){
        return getPropertyBooleanValue(APPLY_LMS_RECORDS_AT_COURSE_LIST_REQUEST, true);
    }

    /**
     * Return whether the secure HTTPS connection should be used rather than the HTTP connection.
     *
     * @return boolean true if "https://" connections should be used instead of "http://".  (DEFAULT: false)
     */
    public boolean shouldUseHttps(){
        return getPropertyBooleanValue(USE_HTTPS, false);
    }

    /**
     * Return whether the server is configured to forward urls to specific ports, which means urls that
     * GIFT may need to construct no longer need to append a port.
     *
     * @return boolean true if port forwarding is configured on the GIFT server, false otherwise (DEFAULT: false)
     */
    public boolean shouldUsePortForwarding(){
        return getPropertyBooleanValue(USE_PORT_FORWARDING, false);
    }


    /**
     * Get the flag to determine if server metrics tracking is enabled or not.  The metrics server allows for profiling of various
     * GIFT server side metrics such as rpc timings, hit rates in realtime.   The benefit is to enable
     * system health metrics when GIFT is running and expose that data to external monitoring services.
     * Primarily this will be used when GIFT is running in a cloud/server environment.  If GIFT is running
     * in desktop or experiment mode, likely this will not need to be enabled.
     *
     * @return boolean true if server metrics tracking is enabled, false otherwise (DEFAULT: false)
     */
    public boolean isServerMetricsEnabled(){
        return getPropertyBooleanValue(ENABLE_SERVER_METRICS, false);
    }

    /**
     * Return the url to the metrics server.  This is only used if the EnableServerMetrics flag is set to true.  The
     * url should be the full path to the metrics servlet.  This url is only accessed internally to GIFT so only
     * an internal IP address is required.
     *
     * @return METRICS_URL URL (e.g. http://localhost:8080/dashboard/dashboard/metrics/).  Can be empty string if
     * the value is not specified in the configuration file.
     */
    public String getMetricsServerUrl(){
        return getPropertyValue(METRICS_URL, "");
    }

    /**
     * Return the flag to allow stress/load testing.   If this flag is set to ON, then gift can be
     * configured to be in load testing mode.  This should NEVER be enabled in a production environment
     * and should only be used during development / load testing.
     *
     * @return boolean - true if the stress mode is enabled, false otherwise (DEFAULT: false)
     */
    public boolean isStressModeEnabled(){
        return getPropertyBooleanValue(ENABLE_STRESS_MODE, false);
    }

    /**
     * Used to create a unique token that is required for stress accounts.  This is only
     * used if StressLogin is enabled.  This should NEVER be enabled in a production environment
     * and should only be used during development / load testing.
     *
     * @return String - The token that is required for stress accounts to pass in the http header.  Can return an
     *                  empty string if the value is not specified in the configuration file.
     */
    public String getStressToken(){
        return getPropertyValue(STRESS_TOKEN, "");
    }


    /**
     * Get the message ACK timeout in milliseconds.  This value is the number of milliseconds that should be
     * waited before the ACK is considered to be timed out.
     *
     * @return integer value containing the timeout value (in millseconds).  (DEFAULT: 10000ms [10 seconds])
     */
    public int getMessageAckTimeoutMs(){
        return getPropertyIntValue(MESSAGE_ACK_TIMEOUT_MS, DEFAULT_ACK_TIMEOUT_MS);
    }

    /**
     * The timeout value for the module status monitor (in milliseconds).  If a module status update is not received
     * within this time period, the module is considered 'timed out' and will be removed.
     *
     * @return int - the timeout value for the module status monitor (in milliseconds).  Default: 10000ms (10 seconds).
     */
    public int getModuleStatusMonitorTimeoutMs() {
        return getPropertyIntValue(MODULE_STATUS_TIMEOUT_MS, DEFAULT_MODULE_TIMEOUT_MS);
    }

    /**
     * Set the new property value in the properties, then write all properties to disk.
     *
     * @param propertyName - the property key
     * @param value - the new property value
     * @throws FileNotFoundException - thrown if the properties file can't be found
     * @throws IOException - thrown if there was an issue while saving to disk
     */
    protected void setProperty(String propertyName, String value) throws FileNotFoundException, IOException{

        properties.setProperty(propertyName, value);
        properties.store(new FileOutputStream(propFileName), null);
    }

    /**
     * Set the new property value in the properties as a delimited list from the values provided, then write all properties to disk.
     *
     * @param propertyName - the property key
     * @param values - a list of values for the new property value
     * @throws FileNotFoundException - thrown if the properties file can't be found
     * @throws IOException - thrown if there was an issue while saving to disk
     */
    protected void setProperty(String propertyName, String[] values) throws FileNotFoundException, IOException{

        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < values.length; i++){
            sb.append(values[i]);

            if(i < values.length-1){
                sb.append(DELIM);
            }
        }

        setProperty(propertyName, sb.toString());
    }

    /**
     * Sets the command line arguments passed to the application to be parsed as
     * to properties
     *
     * @param optionsList The accepted command line arguments
     * @param args The command line arguments
     */
    protected void setCommandLineArgs(List<Option> optionsList, String[] args) {

        CommandLineParser parser = new GnuParser();

        Options options = new Options();

        if (optionsList != null) {

            for (Option option : optionsList) {

                options.addOption(option);
            }
        }

        try {

            CommandLine commandLine = parser.parse(options, args);

            for(Option option : commandLine.getOptions()) {

                if(commandLine.hasOption(option.getOpt())) {

                    properties.put(option.getOpt(), commandLine.getOptionValue(option.getOpt()));
                }
            }

        } catch (ParseException ex) {

            logger.error("Could not parse command line args", ex);
        }
    }

    /**
     * Sets the command line arguments passed to the application to be parsed as
     * to properties
     *
     * @param args The command line arguments
     */
    public void setCommandLineArgs(String[] args) {

        setCommandLineArgs(null, args);
    }

    @Override
    public String toString(){
        return properties.toString();
    }

    /**
     * If GIFT has been set up to use HTTPS, gets the URL scheme for HTTPS. Otherwise, gets the URL scheme for HTTP.
     *
     * @return if GIFT has been set up to use HTTPS, the URL scheme for HTTPS. Otherwise, the URL scheme for HTTP.
     */
    public String getTransferProtocol(){

    	if(shouldUseHttps()){
    		return Constants.HTTPS;

    	} else {
    		return Constants.HTTP;
    	}
    }

    /**
     * Return the url to where jmx is configured for activemq.  JMX must be enabled for activemq and is configured in the
     * wrapper.conf file for activemq.  The port must match what activemq is using in the wrapper.conf file.
     *
     * This jmx url is used to clean up topics & queues that are created by GIFT.  Most likely this value should not need
     * to change unless you are configuring your own cloud/server instance of GIFT.
     *
     * @return String JMX URL (e.g. localhost:1616).  Can be empty string if
     * the value is not specified in the configuration file.
     */
    public String getJmxUrl() {
        return getPropertyValue(JMX_URL, "");
    }

    /**
     * Gets the path to the directory that contains survey images.
     * As of 10/2021 media added to surveys are placed in the course folder.  This 
     * will only be used for support of courses authored with references to this folder
     * before this logic was introduced.  Therefore this is no longer a value that should
     * be changed in the properties file.
     *
     * @return "data/surveyWebResources/uploadedImages"
     */
    public String getSurveyImageUploadDirectory() {
        return "data/surveyWebResources/uploadedImages";
    }

    /**
     * Gets whether survey permissions checks should be ignored.  The returned
     * values is based on the property value and whether the deployment mode is
     * not server.
     * For example:
     * 1. a property value of true and server deployment mode
     * will return false.
     * 2. a property value of true and desktop deployment mode will return true.
     * 3. a property value of false will always return false;
     *
     *
     * @return true if the survey permissions checks should be ignored.
     */
    public boolean getByPassSurveyPermissionCheck(){
    	boolean bypass = this.getPropertyBooleanValue(BYPASS_SURVEY_PERMISSION_CHECK, false);
    	if(bypass){
    	    return !isServerDeploymentMode();
    	}else{
    	    return false;
    	}
    }


    /**
     * Gets the flag indicating whether client Analytics are enabled
     *
     * @return ClientAnalyticsFlag flag
     */
    public boolean isClientAnalyticsEnabled(){
    	return this.getPropertyBooleanValue(ENABLE_CLIENT_ANALYTICS);
    }

    /**
     * Gets the analytics server ip
     *
     * @return string ClientAnalyticsServerIp
     */
    public String getClientAnalyticsUrl(){
    	return this.getPropertyValue(CLIENT_ANALYTICS_URL);
    }

    /**
     * Gets the trusted lti consumer mapping defined in the configuration file.  This mapping is a list
     * of trusted lti consumers that GIFT supports.  The consumer key is used as the key into the mapping and must
     * be unique for each consumer.
     *
     * @return Mapping of the lti consumers based on the consumer key.  Null is returned if there is an error parsing the map.
     */
    public HashMap<String, TrustedLtiConsumer> getTrustedLtiConsumers() {
        String jsonString = getPropertyValue(TRUSTED_LTI_CONSUMERS);
        LtiJsonParser parser = new LtiJsonParser();
        return parser.parseTrustedConsumerListJson(jsonString);
    }

    /**
     * Gets the trusted lti provider mapping defined in the configuration file. This mapping is a
     * list of trusted lti providers that GIFT supports. The consumer key is used as the key into
     * the mapping and must be unique for each provider.
     *
     * @return Mapping of the lti providers based on the provider key. Null is returned if there is
     *         an error parsing the map.
     */
    public HashMap<String, LtiProvider> getTrustedLtiProviders() {
        String jsonString = getPropertyValue(TRUSTED_LTI_PROVIDERS);
        LtiJsonParser parser = new LtiJsonParser();
        return parser.parseTrustedProviderListJson(jsonString);
    }

    /**
     * Specifies the timeout (in milliseconds) of when an lti launch is considered 'stale' from the time the initial launch
     * request is received from the server to the time the gift dashboard is connected to. This is an lti security
     * measure to prevent stale launch requests from accessing the gift course.
     * @return int The timeout in milliseconds before an lti launch request is considered stale (when connecting to the dashboard server).
     */
    public int getLtiTimeoutDashboardMs(){
        return getPropertyIntValue(LTI_TIMEOUT_DASHBOARD_MS, DEFAULT_LTI_TIMEOUT_DASHBOARD_MS);
    }

    /**
     * Specifies the timeout (in milliseconds) of when an lti launch is considered 'stale' from the time the initial launch
     * request is received from the server to the time the lti course is requested to be started. This is an lti security
     * measure to prevent stale launch requests from accessing the gift course.
     * @return int The timeout in millseconds before an lti launch request is considered stale (when connecting to the tutor server).
     */
    public int getLtiTimeoutTutorMs(){
        return getPropertyIntValue(LTI_TIMEOUT_TUTOR_MS, DEFAULT_LTI_TIMEOUT_TUTOR_MS);
    }

    /**
     * Specifies the delay (in milliseconds) between when an assessment
     * notification is fired and when it can be fired again. Any notifications
     * that happen during the delay will be ignored.
     * 
     * @return int The delay in millseconds after an assessment notification is
     *         fired until it can be fired again.
     */
    public int getAssessmentNotificationDelayMs() {
        return getPropertyIntValue(ASSESSMENT_NOTIFICATION_DELAY_MS, DEFAULT_ASSESSMENT_NOTIFICATION_DELAY_MS);
    }

    /**
     * Specifies the lti sub path where the lti servlet is hosted.
     * This must match the <servlet-mapping> in the web.xml for where the servlet lives.
     * For example, the full url to the lti servlet may look like this:  http://localhost:8080/dashboard/lti
     * where /lti is the subpath pointing to the lti servlet.
     * @return String The sub path where the lti servlet is hosted.
     */
    private String getLtiUrlSubPath() {
        return this.getPropertyValue(LTI_URL_SUBPATH);
    }

    /**
     * Returns the full URL to the LTI Tool Provider Tutor Servlet.
     *
     * @return String The full URL to the LTI Tool Provider tutor servlet.
     */
    public String getLtiServletUrl() {
        return getDashboardURL() + Constants.FORWARD_SLASH + getLtiUrlSubPath();
    }

    /**
     * Specifies the lti sub path where the lti consumer servlet is hosted.
     * This must match the <servlet-mapping> in the web.xml for where the servlet lives.
     * For example, the full url to the lti servlet may look like this:  http://localhost:8080/dashboard/lticonsumer
     * where /lticonsumer is the subpath pointing to the lti consumer servlet.
     * @return String The sub path where the lti consumer servlet is hosted.
     */
    private String getLtiConsumerUrlSubPath() {
        return this.getPropertyValue(LTI_CONSUMER_URL_SUBPATH);
    }

    /**
     * Returns the full URL to the LTI Tool Consumer Tutor Servlet.
     *
     * @return String The full URL to the LTI Tool Consumer tutor servlet.
     */
    public String getLtiConsumerServletUrl() {
        return getDomainContentServerAddress() + Constants.FORWARD_SLASH + getLtiConsumerUrlSubPath();
    }

    /**
     * Return the window title to display for the GIFT dashboard.
     * A default value will be returned if not specified in the property file.
     *
     * @return the window title value.  Will not be null or empty.
     */
    public String getWindowTitle(){
        return getPropertyValue(WINDOW_TITLE, DEFAULT_WINDOW_TITLE);
    }

    /**
     * Return the set of usernames that are not allowed in this gift instance (e.g. Public)
     *
     * @return won't be null but can be empty.  Entries will be lowercase.
     */
    public static Set<String> getProhibitedNames(){
        return prohibitedUsernames;
    }

    /**
     * Return the path to the default character to use in the TUI
     * when a custom character is not authored for some action during a course.
     *
     * @return the path to the default character (e.g. avatarResources/VirtualHuman/DefaultAvatar.html)
     */
    public String getDefaultCharacterPath(){
        return getPropertyValue(DEFAULT_CHARACTER);
    }
    
    /**
     * Return the URL of the service providing military symbols (e.g. 2525C)
     * @return the URL
     */
    public String getMilitarySymbolServiceURL(){
        return getPropertyValue(MILITARY_SYMBOL_SERVICE_URL);
    }

    /**
     * This is an internal user id that should be used for readonly access.
     * 
     * @return the read only user
     */
    public String getReadOnlyUser() {
        return getPropertyValue(READ_ONLY_USER, "_giftreadonlyuser_");
    }
    
    /**
     * Return the URL of the external strategy provider that can be used to request
     * content for instructional strategies
     * 
     * @return the external strategy provider URL. Will be null if GIFT is not 
     * configured to use an external strategy provider.
     */
    public String getExternalStrategyProviderUrl() {
        return getPropertyValue(EXTERNAL_STRATEGY_PROVIDER_URL);
    }
    
    /**
     * Return the remote client identifier for this Gateway module instance.
     * 
     * @return a unique client id for this remote GW module instance.  Will be null if this module
     * is not running in a remote environment.
     */
    public String getClientId() {
        return getPropertyValue(CLIENT_ID);
    }
    
    /**
     * Retrieve all of the known property entries.  This is useful for sharing
     * with GWT applications.
     *  
     * @return the collection of properties 
     */
    public Set<Entry<Object,Object>> getProperties(){
        return properties.entrySet();
    }
    
    /**
     * Return whether the module is running in a remote launch environment.
     * 
     * @return a boolean value representing whether a valid CLIENT_ID was found within the remote gateway zip
     */
    public boolean isRemoteMode() {
        if(isClientIdValid != null) {
            return isClientIdValid;
        }
        else {
            File server = new File("config" + File.separator + REMOTE_GATEWAY_PROP_FILE);
            
            if (DeploymentModeEnum.SERVER.equals(getDeploymentMode()) && server.exists()) {
                if(getClientId() != null && !getClientId().equals("changeMe")) {
                    isClientIdValid = true;
                    return isClientIdValid;
                }
            } else {
                isClientIdValid = false;
                return isClientIdValid;
            }
        }
        return false;
    }
}
