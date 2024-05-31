/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.nuxeo;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.time.FastDateFormat;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;
import org.dom4j.DocumentException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.RemoteException;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.adapters.DocumentService;
import org.nuxeo.ecm.automation.client.jaxrs.impl.HttpAutomationClient;
import org.nuxeo.ecm.automation.client.jaxrs.spi.auth.PortalSSOAuthInterceptor;
import org.nuxeo.ecm.automation.client.model.Blob;
import org.nuxeo.ecm.automation.client.model.DocRef;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.Documents;
import org.nuxeo.ecm.automation.client.model.PaginableDocuments;
import org.nuxeo.ecm.automation.client.model.PropertyMap;
import org.nuxeo.ecm.automation.client.model.StreamBlob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import mil.arl.gift.common.DomainOption.DomainOptionPermissions;
import mil.arl.gift.common.enums.SharedCoursePermissionsEnum;
import mil.arl.gift.common.io.CommonProperties;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.ExternalFileSystemInterface;
import mil.arl.gift.common.io.FileProxyPermissions;
import mil.arl.gift.common.io.FileUtil;
import mil.arl.gift.common.io.ProgressIndicator;
import mil.arl.gift.common.io.UriUtil;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.net.nuxeo.NuxeoInterface.PermissionsEntityType.AceEntity;
import mil.arl.gift.net.nuxeo.NuxeoInterface.PermissionsEntityType.Acl;
import mil.arl.gift.net.nuxeo.jmx.Nuxeo;

//TODO: better handling of closing buffers and connections when exception is thrown
//TODO: "When you are done with the client you must call the client.disconnect method to free any resource held by the client. 
//       Usually this is done only once when the client application is shutdown. Creating new client instances and destroying 
//       them may be costly so you should use a singleton client instance and use it from different threads (which is safe)."
/**
 * This class contains the logic to interact with the Nuxeo API.
 * 
 * User/Group:
 * 1) create user (done)
 * 2) delete user (done)
 * 3) get user    (done)
 * 4) create group (done)
 * 5) delete group (done)
 * 6) get group    (done)
 * 7) add user to group (done)
 * 
 * Documents:
 * 1) create folder in workspace     (done)  [selecting to create a course]
 * 2) upload document to that created folder (done) [authoring a first saved version of a course.xml file]
 * 3) update document by upload    (done)    [authoring an update to the course.xml file] 
 * 4) delete document              (done)                    
 * 5) delete folder                 (done)
 * 6) get download URL for document (done)
 * 7) get document (file and folder) by name (done) 
 * 8) get files by file extension   (done)
 * 9) export folder to local zip file (done)
 */
public class NuxeoInterface {
    
    /** instance of the logger */
    private final static Logger logger = LoggerFactory.getLogger(NuxeoInterface.class);
    
    private static final String ADMINISTRATOR_USERNAME = "Administrator";
    private static final String PUBLIC_WORKSPACE = "Public";
    
    /** The name of the training apps lib folder (can be found at workspace\Public\TrainingAppsLib) */
    private static final String TRAINING_APPS_LIB_NAME = "TrainingAppsLib";

    /** timeouts for all interactions with GIFT to Nuxeo */
    private static final int CONNECTION_TIMEOUT = 15000;
    private static final int READ_TIMEOUT       = 15000;
    private static final int DEFAULT_MAX_QUOTA = 100 * 1024 * 1024; // ~100MB
    
    private static final String ACCEPT_PROPERTY         = "Accept";
    private static final String ACCEPT_PROPERTY_VALUE   = "*/*";                    //other value(s): application/json+nxentity
    private static final String CONTENT_TYPE_PROPERTY   = "Content-Type";
    private static final String CONTENT_TYPE_PROPERTY_VALUE = "application/json";   //other value(s): aplication/json+nxentity
    private static final String AUTHORIZATION_PROPERTY  = "Authorization";
    private static final String AUTHORIZATION_PROPERTY_VALUE_PREFIX    = "Basic ";
    public static final String WORKSPACES = "Workspaces";
    public static final String DEFAULT_WORKSPACE_ROOT = "/default-domain/workspaces/";
    
    /** a version of DEFAULT_WORKSPACE_ROOT without the forward slash at the end */
    public static final String DEFAULT_WORKSPACE_ROOT_NOFOLDER = "/default-domain/workspaces";
    
    private static final String GET_BY_PATH_WHERE_CLAUSE_PART_1 = " where (ecm:path STARTSWITH '";
    private static final String GET_BY_PATH_WHERE_CLAUSE_PART_2 = "' AND ecm:primaryType = 'Folder')";
    private static final String GET_BY_PATH_WHERE_CLAUSE_PART_3 = " OR (ecm:path STARTSWITH '";
    private static final String GET_BY_PATH_WHERE_CLAUSE_PART_4 = "' AND ecm:primaryType <> 'Folder'";
    private static final String GET_BY_PATH_WHERE_CLAUSE_PART_5 = " AND dc:title LIKE '%";
    private static final String QUERY = "query";
    private static final String PAGE_SIZE = "pageSize";
    private static final int DEFAULT_PAGE_SIZE = 1000;
    private static final String SORT_ORDER = "sortOrder";
    private static final String ASC = "ASC";
    private static final String CURRENT_PAGE_INDEX = "currentPageIndex";
    private static final String SELECT_STAR_FROM_DOCUMENT = "SELECT * FROM Document";
    
    /** nuxeo document date 'modified' property name */
    private static final String DC_MODIFIED_PROP = "dc:modified";
    
    /** nuxeo document 'rights' property name */
    private static final String DC_RIGHTS_PROP = "dc:rights";
    
    /** used to indicate the setting of access control list (ACE) for a document (folder/file) */
    private static final String SET_ACE_COMMAND = "Document.SetACE";
    
    /** first arg when setting ACE username */
    private static final String ACE_USER_ARG = "user";
    
    /** first arg when specifying permission type */
    private static final String ACE_PERMISSION_ARG = "permission";
    
    /** first arg when specifying to give or remove the permission */
    private static final String ACE_GRANT_ARG = "grant";
    
    /** first arg when specifying to overwrite permission */
    private static final String ACE_OVERWRITE_ARG = "overwrite";
    
    private static final String LOCAL_ACL = "local";
    
    /**
     * Matches the format found on Nuxeo's documentation at: https://doc.nuxeo.com/nxdoc/client-api-test-suite-tck/
     * Snippet example:  "dc:modified" : "2013-07-01T22:44:48.08Z",
     */
    private static final FastDateFormat DOC_MODIFIED_DATE_FORMAT = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSz", null, null);
    
    //HTTP Methods
    private static final String DELETE  = "DELETE";
    private static final String POST    = "POST";
    
    private static final String SPACE = " ";
    private static final String URL_SPACE = "%20";
    private static final String COLON = ":";
    
    // An escaped single quotation character
    private static final String ESCAPED_QUOTE = "\\\\'";
    
    //
    // Various Nuxeo API URL suffixes
    // Sources:
    //      http://nuxeo.github.io/api-playground/#/resources
    //      http://doc.nuxeo.com/display/NXDOC/Downloading+Files, http://doc.nuxeo.com/display/NXDOC56/Simple+REST
    //
    private static final String USER_URL             = "api/v1/user/";
    private static final String GROUP_URL            = "api/v1/group/";
//    private static final String FILE_IMPORT_URL             = "site/fileImporter/run?";
    private static final String AUTOMATION_URL              = "site/automation";
    private static final String QUOTA_INFO_URL = AUTOMATION_URL + "/Quotas.GetInfo";
    private static final String QUOTA_SET_SIZE_URL = AUTOMATION_URL + "/Quotas.SetMaxSize";
//    private static final String AUTOMATION_BLOB_URL         = "api/v1/automation/batch/upload";
    private static final String QUERY_URL                   = "api/v1/query?";
    private static final String QUERY_FILE_NAME_URL         = "query=SELECT * FROM Document WHERE content/name LIKE ";
    private static final String DOWNLOAD_FILE_URL_1         = "nxbigfile/default";
    private static final String DOWNLOAD_FILE_URL_2         = "file:content";
    private static final String DELETE_DOCUMENT_URL         = "site/api/v1/id/";
    private static final String PATH_URL             = "api/v1/path/";
    private static final String IMPORT_SERVER_FOLDER_URL    = "site/fileImporter/run?targetPath="+DEFAULT_WORKSPACE_ROOT;

    private static final String EVERYONE_GROUP = "members";
    // Should only be used for readonly access.
    private static final String ADMINISTRATORS_GROUP = "administrators";
    private static final long PRUNE_SESSION_DELAY_MILLIS = 30*60*1000;
    private static final int DEFAULT_SESSION_EXPIRATION_MILLIS = 60*60*1000;
    /** Class encapsulating a user Session object with the time it was last used */
    static class SessionEntry {
        Session session;
        long lastUsed;
    }
    /**
     * Map used to speed up UserEntityType queries through caching. Gets cleaned
     * out by the cleanupTimer that also cleans up sessions.
     */
    final ConcurrentHashMap<String, UserEntityType> userEntityMap = new ConcurrentHashMap<>();
    
    /**
     * Map used to cache user sessions.  Sessions time out after a configured period of time.
     */
    final ConcurrentHashMap<String, SessionEntry> sessionMap = new ConcurrentHashMap<>();
    final Object clientSessionLock = new Object();
    private String adminUser = ADMINISTRATOR_USERNAME;
    private String secretKey = null;
    private int userWorkspaceQuota = DEFAULT_MAX_QUOTA;
    
    /**
     * Enumerated types of Nuxeo documents.
     * Note: there are more types but this seemed like all we needed for GIFT.  Other types
     * include workspace, note, forum, collection,... (see the create "new" options in the default Nuxeo CMS UI)
     * 
     * @author mhoffman
     *
     */
    private enum DocumentTypes{
        File,
        Folder,
        Workspace,
        WorkspaceRoot
    }
    
    /** the nuxeo server url used for API calls */
    private String serverUrl;
    private final HttpAutomationClient automationClient;
    private final static Nuxeo nuxeoMbean = new Nuxeo();
    private Timer cleanupTimer = new Timer("Nuxeo-Cache-Cleanup");
    
    static {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer(); 
        try { 
            ObjectName name = new ObjectName("mil.arl.gift.net.nuxeo.jmx:type=Nuxeo");
            mbs.registerMBean(nuxeoMbean, name);
        } catch (MalformedObjectNameException | InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException ex) {
            logger.warn("Error registering Nuxeo JMX object for monitoring available Nuxeo metrics.  This capability is useful in a server deployment configuration.", ex);
        }
    }
    
    /**
     * Set attribute(s).
     * Note: currently this constructor should only be called once per GIFT JVM as we have made no effort
     * to ensure the GIFT Nuxeo connection/logic can be multi-threaded or handle multiple concurrent requests.
     * 
     * @param serverUrl the URL of the Nuxeo server (e.g. http://10.1.21.172:8080/nuxeo/).  Can't be null or empty.
     * @param secretKey Secret key string used to authenticate with Nuxeo instance.  Can't be null or empty. 
     */
    public NuxeoInterface(String serverUrl, String secretKey){
        
        if(serverUrl == null || serverUrl.isEmpty()){
            throw new IllegalArgumentException("The server URL can't be null or empty.");
        }else if(!serverUrl.endsWith(Constants.FORWARD_SLASH)){
            serverUrl += Constants.FORWARD_SLASH;
        }
        
        if(secretKey == null || secretKey.isEmpty()){
            throw new IllegalArgumentException("The secret key can't be null or empty.");
        }
        
        this.serverUrl = serverUrl;
        this.secretKey = secretKey;
        
        automationClient = createAutomationClient();
        
        
        cleanupTimer.schedule(new TimerTask() {

                @Override
                public void run() {
                    cleanUpSessions();
                }
            
            }, PRUNE_SESSION_DELAY_MILLIS, PRUNE_SESSION_DELAY_MILLIS);
        
        
        // This will create the internal readonly nuxeo user if it doesn't exist.
        createReadOnlyUser();
    }

    /**
     * Converts the username into a Nuxeo-safe version. Since Nuxeo is case
     * sensitive, all GIFT user names need to be lower case.
     * 
     * @param username the username to make safe.
     * @return the Nuxeo-safe version of the username.
     */
    private String toSafeUsername(String username) {
        /* Leave the admin user as-is (case-sensitive) */
        if (StringUtils.equals(username, adminUser)) {
            return username;
        }

        /* Nuxeo is case sensitive, so make all GIFT user names lowercase */
        return username.toLowerCase();
    }

    /**
     * Creates the internal read only user if it doesn't exist.  This internal
     * user is used for readonly access where a normal user is not logged into Nuxeo.
     * It is similar to an admin user, but has less privileges.  
     * This user needs to only be created once (if it doesn't exist)
     * and does not need to have a workspace.
     * 
     * @return true if the creation was successful or if the user already exists.  False if the operation was not successful.
     */
    private boolean createReadOnlyUser() {
        // Create the internal user which is used for read only access to nuxeo.
        boolean success = false;
        try {
            final String readOnlyUsername = CommonProperties.getInstance().getReadOnlyUser();
            boolean exists = userExists(readOnlyUsername);
            
            if (!exists) {
                
                UserEntityType.Properties props = new UserEntityType.Properties(readOnlyUsername, readOnlyUsername,
                        readOnlyUsername, readOnlyUsername, readOnlyUsername, readOnlyUsername);
                UserEntityType user = new UserEntityType(props);
                
                if (createUser(user)) {
                    
                    // There should be a proper readonly group to add this user to, for now we are using the admin group
                    // to separate the logic for readonly that will be used in the future.
                    if(addUserToGroup(user.getProperties().getUsername(), ADMINISTRATORS_GROUP)){
                        if(logger.isInfoEnabled()){
                            logger.info("Successfully created the internal readonly user in nuxeo.");
                        }
                        success = true;
                    
                    } else {
                        logger.error("Failed to add the internal readonly user to the '"+ADMINISTRATORS_GROUP+"' for "+user+". Deleting Nuxeo user account since it isn't complete.");
                        deleteUser(user.getProperties().getUsername());
                    }
                } else {
                    logger.error("Failed to create the internal readonly user in nuxeo.");
                }
            } else {
                if(logger.isInfoEnabled()){
                    logger.info("Readonly user already exists in nuxeo, so no need to create it at startup.");
                }
                success = true;
            }
                    
            
        } catch (IOException e) {
            logger.error("Exception caught while creating the internal readonly user in nuxeo: ", e);
        }
        
        return success;
    }
    
    /**
     * Return the Nuxeo server URL.
     * 
     * @return The Server URL that the Nuxeo instance is using
     */
    public String getServerURL(){
        return serverUrl;
    }
    
    /**
     * Set the quota used for user workspaces on creation
     * @param quota Limit of workspace size in bytes
     */
    public void setUserWorkspaceQuota(int quota) {
        this.userWorkspaceQuota = quota;
    }
    
    /**
     * Set the username that is used for Admin operations
     * @param username User that has admin privileges.  Can't be null or empty.
     */
    public void setAdminUsername(String username) {
        
        if(username == null || username.isEmpty()){
            throw new IllegalArgumentException("The admin username can't be null or empty.");
        }
        
        this.adminUser = username;
    }

    /**
     * Constructs a token for secret key authorization
     * @param timestamp Current time stamp
     * @param random Random element
     * @param user User that will be used in server request
     * @return Base64 version of the constructed token
     * @throws Exception 
     */
    private String constructRequestToken(String timestamp, String random, String user) {
        
        String tokenInput = timestamp + ":" + random + ":" + secretKey + ":"
                + user;
        byte[] md5;
        String result = "ERROR";
        try {
            md5 = MessageDigest.getInstance("MD5").digest(
                    tokenInput.getBytes());
            result = DatatypeConverter.printBase64Binary(md5);
        } catch (NoSuchAlgorithmException ex) {
            logger.error("Error creating requst token", ex);
        }
        return result;
    }
    
    /**
     * Escapes single quotations so that they can be used in nuxeo queries 
     * (ie OperationRequest.set("query", "value")). This method handles 
     * quotes that have already been escaped.
     * 
     * @param string The string containing quotes to escape
     * @return A string with escaped quotes
     */
    private String escapeQuotes(String string) {
    	// Replace any quotes that are not preceded by a backslash
    	return string.replaceAll("(?<!\\\\)'", ESCAPED_QUOTE);
    }
    
    /**
     * Return the encoding needed to authenticate Nuxeo interactions for the credentials specified.  
     * The authentication string is an encoded version of the user name and password.
     * 
     * @param username a Nuxeo user name. Can't be null.
     * @param password the password for that user.  Can't be null.
     * @return contains the encoded user name and password.
     */
    public static String getEncodedAuthority(String username, String password){
        
        if(username == null){
            throw new IllegalArgumentException("The user name can't be null.");
        }else if(password == null){
            throw new IllegalArgumentException("The password can't be null.");
        }
        
        //Nuxeo is case sensitive, so make all GIFT user names lowercase
        username = username.toLowerCase();
        
        String auth = username + COLON + password;
        return new String(Base64.encodeBase64(auth.getBytes()));
    }
    
    /**
     * Return a connection for the URL specified.
     * 
     * @param url a Nuxeo API URL used for interacting with Nuxeo. 
     * @param encodedAuthority an encoding of a Nuxeo user's username and password
     * @return HttpURLConnection can be used to interact (PUT, GET, DELETE) with Nuxeo.
     * @throws IOException if there was a problem opening a connection to the URL specified.
     */
    @SuppressWarnings("unused")
    private static HttpURLConnection getConnectionBasicAuth(URL url, String encodedAuthority) throws IOException{
        
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);       
        connection.setRequestProperty(AUTHORIZATION_PROPERTY, AUTHORIZATION_PROPERTY_VALUE_PREFIX + encodedAuthority);
        connection.setRequestProperty(CONTENT_TYPE_PROPERTY, CONTENT_TYPE_PROPERTY_VALUE);
        connection.setRequestProperty(ACCEPT_PROPERTY, ACCEPT_PROPERTY_VALUE);
        connection.setConnectTimeout(CONNECTION_TIMEOUT);
        connection.setReadTimeout(READ_TIMEOUT);

        return connection;
    }
    
    /**
     * Create a connection to the specified URL
     * @param url URL to create connection
     * @param authorizedUser User who is authorized to make calls to the resource
     * @return
     * @throws IOException 
     */
    private HttpURLConnection getConnection(URL url, String authorizedUser) throws IOException{
        
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);       
        connection.setRequestProperty(CONTENT_TYPE_PROPERTY, CONTENT_TYPE_PROPERTY_VALUE);
        connection.setRequestProperty(ACCEPT_PROPERTY, ACCEPT_PROPERTY_VALUE);
        
        long time = new Date().getTime();
        String timeString = Long.toString(time);
        long random = new Random(time).nextInt();
        String randomString = Long.toString(random);

        String token = constructRequestToken(timeString, randomString, authorizedUser);
        connection.setRequestProperty("NX_TS", timeString);
        connection.setRequestProperty("NX_RD", randomString);
        connection.setRequestProperty("NX_TOKEN", token);
        connection.setRequestProperty("NX_USER", authorizedUser);
        connection.setConnectTimeout(CONNECTION_TIMEOUT);
        connection.setReadTimeout(READ_TIMEOUT);
        
        NuxeoInterface.nuxeoMbean.incrementAccumulatedHttpUrlConnectionCount();
        
        return connection;
    }
    
    private HttpAutomationClient createAutomationClient() {
        return new HttpAutomationClient(serverUrl + AUTOMATION_URL);
    }
    
    /**
     * Get a session using the Secret Key and SSO Authorization
     * @param username User for which to create the session.  The username is case sensitive so make sure public usernames
     * are all lower case. 
     * @return Session for the user
     * @throws IOException 
     */
    private Session getSession(String username) throws IOException {
        
        SessionEntry entry = sessionMap.get(username);
        if (entry == null) {
            entry = new SessionEntry();
            synchronized (clientSessionLock) {
                // Though the client is thread safe we have to sync on these operations to prevent
                // a session being created for the wrong user
                automationClient.setRequestInterceptor(new PortalSSOAuthInterceptor(secretKey, username));
                entry.session = automationClient.getSession();
            }
            SessionEntry tempEntry = sessionMap.putIfAbsent(username, entry);
            // it is possible that another session has been created for the user in the time spent to create
            // this session, so we return that one and close the one we just created to minimize the number of 
            // sessions floating around
            if (tempEntry != null) {
                entry.session.close();
                entry = tempEntry;
            } else {
                NuxeoInterface.nuxeoMbean.incrementActiveClientSessionCount();
            }
        }
        entry.lastUsed = System.currentTimeMillis();

        NuxeoInterface.nuxeoMbean.incrementClientSessionUseTotal();

        return entry.session;
    }
    
    private void cleanUpSessions() {
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<String, SessionEntry>> iter = sessionMap.entrySet().iterator();
        Map.Entry<String, SessionEntry> entry;
        while (iter.hasNext()) {
            entry = iter.next();
            if ((now - entry.getValue().lastUsed) > DEFAULT_SESSION_EXPIRATION_MILLIS) {
                // Remove session from map. Should clean itself up on GC
                iter.remove();
                NuxeoInterface.nuxeoMbean.decrementActiveClientSessionCount();
                userEntityMap.remove(entry.getKey());
            }
        }
    }
    
    /**
     * Get a session using basic authentication.
     * @param username User for which to create a session
     * @param password Password of the user
     * @return
     * @throws IOException 
     */
    @SuppressWarnings("unused")
    private Session getSessionBasicAuth(String username, String password) throws IOException {
        
        //Nuxeo is case sensitive, so make all GIFT user names lowercase
        username = toSafeUsername(username);
        
        HttpAutomationClient client = createAutomationClient();
        return client.getSession(username, password);
    }
    
    /**
     * Create the URL for the Nuxeo API command specified.
     * For example, to get the user with username "bill", the arguments would be "api/v1//user/" and "bill". The
     * URL returned might look like "http://10.1.21.172:8080/nuxeo/api/v1//user/bill".
     * 
     * @param apiCmdSubURL the API command (e.g. get user) URL snippet (e.g. "api/v1//user/")
     * @param apiCmdURLArg the API command (e.g. get user) URL argument (e.g. <username>). Can be null if the command
     * doesn't require and argument (e.g. create user doesn't provide a URL argument, rather is writes the argument
     * to the connection's stream) 
     * @return the URL for the API command being performed
     * @throws MalformedURLException if there was a problem building the URL
     */
    private URL constructURL(String apiCmdSubURL, String apiCmdURLArg) throws MalformedURLException{
        
        String urlStr;
        if(apiCmdURLArg == null){
            urlStr = serverUrl + apiCmdSubURL;
        }else{
            urlStr = serverUrl + apiCmdSubURL + apiCmdURLArg;  
        }
        
        return new URL(UriUtil.makeURICompliant(urlStr));
    }
    
    /**
     * Return the Nuxeo user information for the username specified.
     * 
     * @param usernameToGet the Nuxeo username to retrieve user information for.  Can't be null or empty.
     * @return the Nuxeo user information for the user specified.  Will be null in the following cases:
     * 1) the user was not found
     * 2) the authorization failed
     * 3) there was an issue with the query or server
     */
    public UserEntityType getUser(String usernameToGet){
        
        if(usernameToGet == null || usernameToGet.isEmpty()){
            throw new IllegalArgumentException("The username to get can't be null or empty.");
        }
        
        //Nuxeo is case sensitive, so make all GIFT user names lowercase
        usernameToGet = toSafeUsername(usernameToGet);

        UserEntityType user = userEntityMap.get(usernameToGet);
        if (user != null) {
            return user;
        }
        HttpURLConnection connection = null;
        try {
            URL url = constructURL(USER_URL, usernameToGet);
            // Use adminUser here because the user may exist resulting in 401 instead of 404
            connection = getConnection(url, adminUser);            
            int response = connection.getResponseCode();
            if (response != HttpURLConnection.HTTP_OK) {
                throwUnhandledResponseError(response);
            }
            try (BufferedReader in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()))) {

                Gson gson = new Gson();
                user = gson.fromJson(in, UserEntityType.class);
            }
            
        } catch (ConnectException e) {
            logger.error("Error connecting to server", e);
        } catch (Exception e) {
            logger.error("Error while getting user of username = "+usernameToGet, e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        if (user != null) {
            userEntityMap.putIfAbsent(usernameToGet, user);
        }
        return user;
    }

    /**
     * Checks the Nuxeo server to see if the user account exists
     * @param username User to check. Can't be null or empty.
     * @return True if user account exists, false otherwise
     * @throws java.io.IOException Exception thrown if there is an unexpected error
     */
    public boolean userExists(String username) throws IOException {
        
        //Nuxeo is case sensitive, so make all GIFT user names lowercase
        username = toSafeUsername(username);
        
        boolean userExists = false;
        HttpURLConnection connection = null;
        try {
            //Nuxeo is case sensitive, so make all GIFT user names lowercase
            URL url = constructURL(USER_URL, username);
            // Use adminUser here because the user may exist resulting in 401 instead of 404
            connection = getConnection(url, adminUser);            
            int response = connection.getResponseCode();
            if(logger.isInfoEnabled()){
                logger.info("userExists response code: " + response);
            }
            if (response == HttpURLConnection.HTTP_OK || response == HttpURLConnection.HTTP_NOT_FOUND) {
                // we only handle two cases, success or not found
                userExists = response == HttpURLConnection.HTTP_OK;
            } else {
                throwUnhandledResponseError(response);
            }
            
        } catch (ConnectException e) {
            throw new IOException("Error connecting to server", e);
        } catch (IOException e) {
            throw new IOException("Error checking if user '"+username+"' exists", e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return userExists;
    }
    
    private void throwUnhandledResponseError(int responseCode) throws IOException {
        switch (responseCode) {
            
            case HttpURLConnection.HTTP_UNAUTHORIZED:
                throw new IOException("Server returned 'UNAUTHORIZED'");
            case HttpURLConnection.HTTP_NOT_FOUND:
                throw new IOException("Server returned 'NOT FOUND'");
            case HttpURLConnection.HTTP_INTERNAL_ERROR:
                throw new IOException("Server returned 'INTERNAL ERROR'");
            default:
                throw new IOException("Server returned unhandled response code: " + responseCode);
        }
    }
    /**
     * Return the Nuxeo group information for the groupname specified.
     * 
     * @param groupnameToGet the Nuxeo group name to retrieve group information for.  Can't be null or empty.
     * @param authority the authority to use to retrieve this information
     * @return the Nuxeo group information for the group specified.  Will be null in the following cases:
     * 1) the group was not found
     * 2) the authority was insufficient for the query
     * 3) there was an issue with the query or server
     */
    public GroupEntityType getGroup(String groupnameToGet, String authority){
        
        if(groupnameToGet == null || groupnameToGet.isEmpty()){
            throw new IllegalArgumentException("The groupname to get can't be null or empty.");
        }

        GroupEntityType group = null;
        HttpURLConnection connection = null;
        try {
            URL url = constructURL(GROUP_URL, groupnameToGet);
            connection = getConnection(url, authority);            

            BufferedReader in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));

            Gson gson = new Gson();
            group = gson.fromJson(in, GroupEntityType.class);

            in.close();
        } catch (Exception e) {
            logger.error("Error while getting group named '"+groupnameToGet+"'.", e);
        } finally{
            if(connection != null){
                connection.disconnect();
            }
        }
        
        return group;
    }
    
    /**
     * Add the user to the existing group.
     * 
     * @param usernameToAdd the existing Nuxeo username to add to the group
     * @param groupnameAddingTo the existing Nuxeo groupname to add the user to
     * @return boolean whether or not adding the user to the group succeeded without error.
     */
    public boolean addUserToGroup(String usernameToAdd, String groupnameAddingTo){

        HttpURLConnection connection = null;
        try {
            //Nuxeo is case sensitive, so make all GIFT user names lowercase
            usernameToAdd = toSafeUsername(usernameToAdd);
            
            URL url = constructURL(USER_URL, usernameToAdd + "/group/" + groupnameAddingTo);
            // Must use the adminUser to add a user to a group
            connection = getConnection(url, adminUser); 
            connection.setRequestMethod(POST);

            BufferedReader in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));

            in.close();
            return true;
            
        } catch (Exception e) {
            logger.error("Error while adding user to group.  Username = '"+usernameToAdd+"', Group = '"+groupnameAddingTo+"'.", e);
        } finally{
            if(connection != null){
                connection.disconnect();
            }
        }

        return false;
    }
    
    /**
     * Delete the user from Nuxeo.
     * Also deletes the user's username workspace folder
     * 
     * @param username the user to delete
     * @return boolean whether or not deleting the user succeeded without error.
     */
    public boolean deleteUser(String username){
        
        HttpURLConnection connection = null;
        try {
            
            //Nuxeo is case sensitive, so make all GIFT user names lowercase
            username = toSafeUsername(username);
            
            try{
                deleteUserWorkspace(username);
            }catch(@SuppressWarnings("unused") FileNotFoundException fnf){
                //do nothing - this means the user named workspace doesn't exist so there is nothing to delete
            }
            
            URL url = constructURL(USER_URL, username);
            // Must use the adminUser to delete a user
            connection = getConnection(url, adminUser);
            connection.setRequestMethod(DELETE);

            BufferedReader in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));

            in.close();
            connection.disconnect();
            
            return true;
            
        } catch (Exception e) {
            logger.error("Error while deleting user named '"+username+"'.", e);
        }finally{
            if(connection != null){
                connection.disconnect();
            }
        }
        
        return false;
    }
    
    /**
     * Delete the group from Nuxeo.
     * 
     * @param groupname the group to delete
     * @param authority the authority to use to complete this interaction
     * @return boolean whether or not deleting the group succeeded without error.
     */
    public boolean deleteGroup(String groupname, String authority){
        
        HttpURLConnection connection = null;
        try {
            //Nuxeo is case sensitive, so make all GIFT user names lowercase
            authority = toSafeUsername(authority);
            
            URL url = constructURL(GROUP_URL, groupname);
            connection = getConnection(url, authority);
            connection.setRequestMethod(DELETE);

            BufferedReader in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));

            in.close();
            connection.disconnect();
            
            return true;
        } catch (Exception e) {
            logger.error("Error while deleting group named '"+groupname+"'.", e);
        }finally{
            if(connection != null){
                connection.disconnect();
            }
        }
        
        return false;
    }
    
    /**
     * Creates a new user in Nuxeo, adds the user to the "members" group, 
     * and creates a workspace for the user named for the username with "manage everything" permissions.
     * 
     * @param userInfo information about the user to create
     * @return boolean true if the createNewUser was successful, false otherwise.
     * @throws IOException if creating there was a problem creating the user
     */
    public boolean createNewUser(UserEntityType userInfo) throws IOException{

        boolean success = false;
        if (createUser(userInfo)) {
            
            if(addUserToGroup(userInfo.getProperties().getUsername(), EVERYONE_GROUP)){
                
                try{
                    if(createUserWorkspace(userInfo) != null){
                        success = true;
                    }else{
                        logger.error("Failed to create the user's workspace for "+userInfo+". Deleting Nuxeo user account since it isn't complete.");
                        deleteUser(userInfo.getProperties().getUsername());
                    }
                }catch(IOException e){
                    logger.error("Failed to create the user's workspace for "+userInfo+". Deleting Nuxeo user account since it isn't complete.", e);
                    deleteUser(userInfo.getProperties().getUsername());
                }
            }else{
                logger.error("Failed to add the user to the '"+EVERYONE_GROUP+"' for "+userInfo+". Deleting Nuxeo user account since it isn't complete.");
                deleteUser(userInfo.getProperties().getUsername());
            }
            
        }
        
        return success;
    }
    
    /**
     * Create the user in Nuxeo.
     * Note: this is now an internal method that should be exposed for GIFT callers due to the 
     * fact that GIFT requires newly created users to have a workspace created and be added
     * to the members groups (all users should be in this group).  See createNewUser method.
     * 
     * @param user information about the user to create
     * @return boolean whether or not creating the user was successful
     */
    private boolean createUser(UserEntityType user){
        
        HttpURLConnection connection = null;
        try {
            URL url = constructURL(USER_URL, null);
            // Must use the adminUser to create a new user
            connection = getConnection(url, adminUser);  

            Gson gson = new Gson();
            String json = gson.toJson(user);
            
            OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
            out.write(json);
            out.close();

            BufferedReader in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));

            in.close();
            connection.disconnect();
            
            return true;
        } catch (Exception e) {
            logger.error("Error while creating user of "+user, e);
        }finally{
            if(connection != null){
                connection.disconnect();
            }
        }
        
        return false;
    }
    
    /**
     * Create the group in Nuxeo.
     * 
     * @param group information about the group to create
     * @param authority the authority to use to complete this interaction
     * @return boolean whether or not deleting the group succeeded without error.
     */
    public boolean createGroup(GroupEntityType group, String authority){
        
        HttpURLConnection connection = null;
        try {
            //Nuxeo is case sensitive, so make all GIFT user names lowercase
            authority = toSafeUsername(authority);
            
            URL url = constructURL(GROUP_URL, null);
            connection = getConnection(url, authority);  

            Gson gson = new Gson();
            String json = gson.toJson(group);
            
            OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
            out.write(json);
            out.close();

            BufferedReader in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));

            in.close();
            connection.disconnect();
            
            return true;
        } catch (Exception e) {
            logger.error("Error while creating group of "+group, e);
        }finally{
            if(connection != null){
                connection.disconnect();
            }
        }

        return false;
    }
    
    /**
     * Return Nuxeo Documents with the file extension available for the given user.
     * 
     * @param extension a file extension (suffix) to search for (e.g. ".course.xml")
     * @param authority the authority to use to complete this interaction
     * @return collection of Nuxeo Document objects representing each file found.  Will be null if the query failed.  Can be empty.
     */
    @Deprecated
    public DocumentsEntityType getDocumentsByFileExtension(String extension, String authority){
        
        DocumentsEntityType documents = null;
        HttpURLConnection connection = null;
        try {
            //Nuxeo is case sensitive, so make all GIFT user names lowercase
            authority = toSafeUsername(authority);
            
            URL url = constructURL(QUERY_URL + QUERY_FILE_NAME_URL, "'%" + extension+"'");
            connection = getConnection(url, authority);            

            BufferedReader in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));

            Gson gson = new Gson();
            documents = gson.fromJson(in, DocumentsEntityType.class);

            in.close();
            connection.disconnect();
        } catch (Exception e) {
            logger.error("Error while getting all files with file extensions of '"+extension+"'.", e);
        }finally{
            if(connection != null){
                connection.disconnect();
            }
        }
        
        return documents;
    }
    
    /**
     * Retrieve Nuxeo 'File' Documents with the given path and optional extensionFilter.
     * Note that this method recurses through the sub-folders in the path.
     * @param workspacePath Workspace path to search
     * @param pathsToExclude a collection of paths that should be excluded from consideration. If null or 
     * empty, no paths are excluded.
     * @param extensionFilter An optional file extension (suffix) to search for (e.g. ".course.xml"). null should be passed if no filter.
     * @param authority the authority to use to complete this interaction
     * @return collection of Nuxeo Document objects representing each file found.  Can be empty.
     * @throws IOException Thrown if a server error occurs
     */
    public Documents getDocumentsByPath(String workspacePath, Iterable<String> pathsToExclude, String extensionFilter, String authority) throws IOException {
        
        //Nuxeo is case sensitive, so make all GIFT user names lowercase
        authority = toSafeUsername(authority);
        
        Session session = getSession(authority);
        StringBuilder whereClause = new StringBuilder();
        whereClause.append(" where ecm:path STARTSWITH '").append(escapeQuotes(workspacePath)).append("' AND ecm:primaryType <> 'Folder'");
        if (extensionFilter != null) {
            whereClause.append(" AND dc:title LIKE '%").append(extensionFilter).append("'");
        }
        
        if(pathsToExclude != null) {
            for(String path : pathsToExclude) {
                whereClause.append(" AND NOT (ecm:path STARTSWITH '").append(path).append("')");
            }
        }
        
        try{
            Documents docs = (Documents) session.newRequest(DocumentService.Query).set(QUERY, SELECT_STAR_FROM_DOCUMENT + whereClause).execute();
            return docs;
        }catch(RemoteException remoteException){
            throw new IOException("Unable to retrieve documents under '"+workspacePath+"' with extension filter of '"+extensionFilter+" because "+remoteException.getMessage()+".", remoteException);
        }
    }
    
    /**
     * Retrieve Nuxeo 'Folder' and 'File' Documents that start with the given path.
     * If the optional extensionFilter is specified, only the files matching the
     * extension will be retrieved.
     * Note that this method recurses through the sub-folders in the path.
     * @param workspacePath Workspace path to search
     * @param extensionFilter An optional file extension (suffix) to search for (e.g. ".course.xml"). null should be passed if no filter.
     * @param authority the authority to use to complete this interaction
     * @return collection of Nuxeo Document objects representing each file found.  Can be empty. 
     * Note: folders will exist after all of the files in that folder.
     * @throws IOException Thrown if a server error occurs
     */
    public Documents getDocumentsAndFoldersByPath(String workspacePath, String extensionFilter, String authority) throws IOException {
        
        //Nuxeo is case sensitive, so make all GIFT user names lowercase
        authority = toSafeUsername(authority);
        
        workspacePath = prependDefaultWorkspacePath(escapeQuotes(workspacePath));        
        
        Session session = getSession(authority);
        StringBuilder whereClause = new StringBuilder();
        whereClause.append(GET_BY_PATH_WHERE_CLAUSE_PART_1).append(workspacePath).append(GET_BY_PATH_WHERE_CLAUSE_PART_2);
        whereClause.append(GET_BY_PATH_WHERE_CLAUSE_PART_3).append(workspacePath).append(GET_BY_PATH_WHERE_CLAUSE_PART_4);
        if (extensionFilter != null) {
            whereClause.append(GET_BY_PATH_WHERE_CLAUSE_PART_5).append(extensionFilter).append(Constants.SINGLE_QUOTE);
        }
        whereClause.append(Constants.CLOSE_PARENTHESIS);
        try{
            // create the request
            OperationRequest opRequest = session.newRequest(DocumentService.Query);
            opRequest.set(QUERY, SELECT_STAR_FROM_DOCUMENT + whereClause);
            // nuxeo limits the results to 1000 so set the page size to the max
            opRequest.set(PAGE_SIZE, DEFAULT_PAGE_SIZE);
            opRequest.set(SORT_ORDER, ASC);
            Documents docs = new Documents();
            PaginableDocuments pageDocs = null;
            do {
                // offset the results by the page size
                opRequest.set(CURRENT_PAGE_INDEX, pageDocs == null ? 0 : pageDocs.getCurrentPageIndex() + 1);
                
                // returns the set of documents
                pageDocs = (PaginableDocuments) opRequest.execute();

                if (pageDocs == null) {
                    break;
                }

                // add the retrieved documents to the master list
                for (Document nextDoc : pageDocs) {
                    docs.add(nextDoc);
                }
            } while (pageDocs.getCurrentPageIndex() + 1 < pageDocs.getNumberOfPages());
            
            return docs;
        }catch(RemoteException remoteException){
            throw new IOException("Unable to retrieve documents under '"+workspacePath+"' with extension filter of '"+extensionFilter+" because "+remoteException.getMessage()+".", remoteException);
        }
    }
    
    /**
     * Return the Nuxeo URL for which the document can be downloaded from.
     * 
     * @param document information about the Nuxeo document
     * @return the Nuxeo URL to download the document
     * @throws MalformedURLException if there was a problem creating the URL
     */
    public URL getDocumentURL(DocumentEntityType document) throws MalformedURLException{
        
        if(document == null){
            throw new IllegalArgumentException("The path to the document was invalid.");
        }
        
        return getFileURL(document.getUid(), document.getTitle());
    }
    
    /**
     * Return the Nuxeo URL for which the document can be downloaded from.
     * 
     * @param document information about the Nuxeo document
     * @return the Nuxeo URL to download the document
     * @throws MalformedURLException if there was a problem creating the URL
     */
    public URL getDocumentURL(Document document) throws MalformedURLException{
        
        if(document == null){
            throw new IllegalArgumentException("The path to the document was invalid.");
        }
        
        return getFileURL(document.getId(), document.getTitle());
    }
    
    /**
     * Return a Nuxeo Document connection object that can be used to access the specified document (i.e. read the document's content).
     * 
     * @param document the nuxeo document to access
     * @param authority the authority to access the document
     * @return a wrapper around the document that provides logic to access the document's contents
     * @throws MalformedURLException if there was a problem constructing the URL for the document
     * @throws IOException if there was a problem retrieving the document
     */
    public NuxeoDocumentConnection getDocumentConnection(DocumentEntityType document, String authority) throws MalformedURLException, IOException{
        
        //Nuxeo is case sensitive, so make all GIFT user names lowercase
        authority = toSafeUsername(authority);
        
        if(logger.isTraceEnabled()){
            logger.trace("getDocumentConnection "+ document + " - authority: " + authority);
        }
        return new NuxeoDocumentConnection(getDocumentURL(document), authority, this);
    }
    
    /**
     * Return a Nuxeo Document connection object that can be used to access the specified document (i.e. read the document's content).
     * 
     * @param document the nuxeo document to access
     * @param authority the authority to access the document
     * @return a wrapper around the document that provides logic to access the document's contents
     * @throws MalformedURLException if there was a problem constructing the URL for the document
     * @throws IOException if there was a problem retrieving the document
     */
    public NuxeoDocumentConnection getDocumentConnection(Document document, String authority) throws MalformedURLException, IOException{
        
        //Nuxeo is case sensitive, so make all GIFT user names lowercase
        authority = toSafeUsername(authority);

        return new NuxeoDocumentConnection(getDocumentURL(document), authority, this);
    }
    
    /**
     * Return the Nuxeo URL for which a document (uniquely identified by the parameters) can be downloaded from.
     * 
     * @param fileUId the Nuxeo unique id for a document in Nuxeo. Can't be null or empty.  E.g. 9d392edc-b54d-4ee8-aa78-6836cf6a5e14
     * @param fileTitle the (not guaranteed to be unique) title of the document identified by that uid.  Can't be null or empty. E.g. "My Course".
     * @return the Nuxeo URL to download the document
     * @throws MalformedURLException if there was a problem creating the URL
     */
    public URL getFileURL(String fileUId, String fileTitle) throws MalformedURLException{
        
        if(fileUId == null || fileUId.isEmpty()){
            throw new IllegalArgumentException("The file unique id can't be null or empty.");
        }else if(fileTitle == null || fileTitle.isEmpty()){
            throw new IllegalArgumentException("The file title can't be null or empty.");
        }
        
        fileTitle = fileTitle.replace(SPACE, URL_SPACE);
        
        StringBuilder sb = new StringBuilder();
        sb.append(serverUrl).append(DOWNLOAD_FILE_URL_1).append(Constants.FORWARD_SLASH).append(fileUId).append(Constants.FORWARD_SLASH).append(DOWNLOAD_FILE_URL_2).append(Constants.FORWARD_SLASH).append(fileTitle);
        return new URL(sb.toString());
    }
    
    /**
     * Download the file specified by the URL to the local file.
     * 
     * @param fileURL the Nuxeo URL of a document to be downloaded.  Must be correctly formatted (i.e. spaces replaced with "%20").
     * @param destination a file to write the downloaded document too.
     * @param username the user under which to complete this interaction
     * @throws IOException if there was a problem opening a connection or downloading the document
     */
    public void writeURLToFile(URL fileURL, File destination, String username) throws IOException{
        
        if(fileURL == null){
            throw new IllegalArgumentException("The fileURL can't be null.");
        }else if(destination == null || !destination.exists()){
            throw new IllegalArgumentException("The destination can't be null and must exits before writing to it.");
        }else if(username == null || username.isEmpty()){
            throw new IllegalArgumentException("The username can't be null or empty.");
        }
        
        //Nuxeo is case sensitive, so make all GIFT user names lowercase
        username = toSafeUsername(username);
        
        HttpURLConnection connection = getConnection(fileURL, username);
        try{
            try (InputStream in = connection.getInputStream()) {
                Files.copy(in, Paths.get(destination.getAbsolutePath()), StandardCopyOption.REPLACE_EXISTING);
            }
        }finally{
            if(connection != null){
                connection.disconnect();
            }
        }
    
    } 
    
    /**
     * Update the content file (aka "Main File" or "Content") of an existing Nuxeo document.  If the document already has a file, it will be replaced with
     * the specified file.  It it doesn't have a file, the file will be the Document's content file. This method causes the 'last modified' timestamp to be updated.
     * 
     * @param document the existing Nuxeo document to update the content file of.  Can't be null. Must be a valid document (i.e. have a valid uid).
     * @param courseFolder the course folder where the document is being updated.  Can't be null. Must be a valid document (i.e. have a valid uid).  This
     * is needed in order to update the last modified date since Nuxeo doesn't handle this automatically. 
     * @param fileContentsToImport the contents of the file to upload and associate with the Nuxeo document.  Can't be null and must exist.
     * @param username used for authentication
     * @throws mil.arl.gift.net.nuxeo.QuotaExceededException Thrown if file upload causes quota to be exceeded
     * @throws IOException if there was a problem getting the client session or updating the content file for the document
     */
    public void updateDocumentFile(DocumentEntityType document, DocumentEntityType courseFolder, InputStream fileContentsToImport, String username) throws QuotaExceededException, IOException{
    	updateDocumentFile(document, courseFolder, fileContentsToImport, username, false);
    }
    
    /**
     * Update the content file (aka "Main File" or "Content") of an existing Nuxeo document.  If the document already has a file, it will be replaced with
     * the specified file.  It it doesn't have a file, the file will be the Document's content file. This method causes the 'last modified' timestamp to be updated.
     * 
     * @param document the existing Nuxeo document to update the content file of.  Can't be null.  Should be a valid document (i.e. have a valid uid). 
     * @param courseFolder the course folder where the document is being updated.  Can't be null. Must be a valid document (i.e. have a valid uid).  This
     * is needed in order to update the last modified date since Nuxeo doesn't handle this automatically. 
     * @param fileContentsToImport the contents of the file to upload and associate with the Nuxeo document.  Can't be null and must exist.
     * @param username used for authentication. Can be null if useAdminPrivilege is set to true.
     * @param useAdminPrivilege Whether or not the operation should be performed with admin privileges
     * @throws mil.arl.gift.net.nuxeo.QuotaExceededException Thrown if file upload causes quota to be exceeded
     * @throws IOException if there was a problem getting the client session or updating the content file for the document
     */
    public void updateDocumentFile(DocumentEntityType document, DocumentEntityType courseFolder, InputStream fileContentsToImport, 
            String username, boolean useAdminPrivilege) throws QuotaExceededException, IOException{
        
        if(fileContentsToImport == null){
            throw new IllegalArgumentException("The file contents to import can't be null or empty.");
        }else if(document == null){
            throw new IllegalArgumentException("The document to update can't be null.");
        }else if(!useAdminPrivilege && (username == null || username.isEmpty())){
            throw new IllegalArgumentException("The username can't be null or empty.");
        }else if(courseFolder == null){
            throw new IllegalArgumentException("The course folder can't be null.");
        }
        
        //Nuxeo is case sensitive, so make all GIFT user names lowercase
        username = useAdminPrivilege ? adminUser : toSafeUsername(username);
        
        Session session = getSession(username);
        
        // The DocumentService will give you some nice shortcut to the most common operations
        DocumentService rs = session.getAdapter(DocumentService.class);
        
        // Create a document Ref object with the id of the document
        DocRef docRef = new DocRef(document.getUid());
        
        // create a document ref object with the id of the course folder that contains the document being updated
        DocRef courseFolderDocRef = new DocRef(courseFolder.getUid());
        
        // Create a blob object from the file to upload
        // We pass null as the ctype argument which is a mime type. This results 
        // in an application/octet-stream mime type, but GIFT probably doesn't care
        // about the type at this point, like a web browser would.
        Blob blob = new StreamBlob(fileContentsToImport, document.getTitle(), null);
        

        //another way that also worked for me (as long as we have the full path to the document as shown below)
        // http://doc.nuxeo.com/display/NXDOC/Java+Automation+Client#JavaAutomationClient-Invokingremoteoperations
//        session.newRequest("Blob.Attach").setHeader(
//                org.nuxeo.ecm.automation.client.Constants.HEADER_NX_VOIDOP, "true").setInput(blob)
//                .set("document", "/default-domain/workspaces/Public/myNewDoc.1427222284644.1427222450650").execute();
        
        //this will update the "Main Content" (or file) of the Nuxeo document
        try {
            rs.setBlob(docRef, blob);
        } catch (RemoteException e) {
            if (e.getRemoteCause().getMessage().contains("Quota Exceeded")) {
                throw new QuotaExceededException("You are running low on disk space. Please delete some files from your workspace before attempting to upload this file again.",
                        "Failed to create the document because the disk space quota has been exceeded.", e);
            }
            else{
                throw new DetailedException("Error",
                        "Failed to create document named '"+ document.getPath() + "/" +  document.getTitle() +"'.", 
                        e);
            }
        }
        
        //update the last modified property on the file being updated --- best effort so don't log any errors
        Date newLastModified = null;
        try{
            Document updatedDoc = rs.setProperty(docRef, DC_MODIFIED_PROP, Constants.EMPTY); //providing an empty string will cause Nuxeo to use the current time
            newLastModified = updatedDoc.getLastModified();
        }catch(@SuppressWarnings("unused") Exception e){}
        
        //update the last modified property on the course folder where the file is being updated --- best effort so don't log any errors
        try{
            if(newLastModified != null){
                rs.setProperty(courseFolderDocRef, DC_MODIFIED_PROP, DOC_MODIFIED_DATE_FORMAT.format(newLastModified));  //providing the same time nuxeo set on the document being updated
            }
        }catch(@SuppressWarnings("unused") Exception e){}
        
        //other options if we want to use attachments (which is in addition to the "Main Content" of a Nuxeo document)
        // http://www.nuxeo.com/blog/qa-friday-add-extra-files-document-content-automation/
//        // Use DocumentService to attach the blob. Giving files:files as
//        // argument will add the blob to the existing attachment
//        rs.setBlob(docRef, blob, "files:files");
////        // This will replace the content of the first attachment
//        rs.setBlob(docRef, blob, "files:files/0/file");
////        // If you do that, do not forget to change the filename too
//        rs.setProperty(docRef, "files:files/0/filename", "NewName");
        
    }
    
    /**
     * Update the document's last modified value to the current date.  
     * 
     * @param courseFolder the nuxeo representation of the document to update the last modified date on
     * @param username used for authentication when updating the document
     * @param useAdminPrivilege whether to use the admin user for this operation which is needed if the user doesn't have
     * write access (e.g. Public workspace course folder)
     * @throws IOException if there was a problem setting the last modified date including communicating with nuxeo
     */
    private void updateCourseFolderLastModified(DocumentEntityType courseFolder, String username, boolean useAdminPrivilege) throws IOException{
        
       Session session = (useAdminPrivilege ? getSession(adminUser) : getSession(username));
        
        // The DocumentService will give you some nice shortcut to the most common operations
        DocumentService rs = session.getAdapter(DocumentService.class);
        
        // create a document ref object with the id of the course folder that contains the document being updated
        DocRef courseFolderDocRef = new DocRef(courseFolder.getUid());
        
        //update the last modified property on the course folder where the file is being updated --- best effort so don't log any errors
        try{
            rs.setProperty(courseFolderDocRef, DC_MODIFIED_PROP, Constants.EMPTY);  //providing an empty string will cause Nuxeo to use the current time
        }catch(@SuppressWarnings("unused") Exception e){}
    }
    
//    /**
//     * 
//     * 
//     * @param directoryName
//     * @param encodedAuthority the authority to use to complete this interaction
//     * @return 
//     */
//    public DirectoryEntries getDirectoryEntries(String directoryName, String encodedAuthority){
//        
//        DirectoryEntries entries = null;
//        try {
//            URL url = constructURL(GET_DIRECTORY_ENTRIES_URL, directoryName + "/@children");
////            URL url = new URL(GIFT_NUXEO_SERVER_URL+"site/automation/Directory.Entries");
////            URL url = new URL(GIFT_NUXEO_SERVER_URL+"site/automation/UserWorkspace.Get");
//            HttpURLConnection connection = getConnection(url, encodedAuthority);  
////            connection.setRequestMethod("POST");
////            OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
////            out.write("{\n\"context\":{},\n}");
////            out.close();
//
//            BufferedReader in = new BufferedReader(new InputStreamReader(
//                    connection.getInputStream()));
//            
////            Gson gson = new Gson();
////            entries = gson.fromJson(in, DirectoryEntries.class);
//            String line = in.readLine();
//            while(line != null){
//                System.out.println(line);
//                line = in.readLine();
//            }
//
//            in.close();
//            connection.disconnect();
//            
//        } catch (Exception e) {
//            System.err.println("\nError while getting directory entries for directory named "+directoryName);
//            e.printStackTrace();
//        }
//
//        return entries;
//    }
    
    /**
     * This method is used for testing Nuxeo API URLs.
     * 
     * @param urlStr the Nuxeo API URL being tested
     * @param username the credentials needed to test the URL
     * @param resultEncodingClass the return data class of the URL API call.  Null if nothing will be returned, the caller
     * doesn't know the return type or doesn't care.  If null any output of the URL will be written to the console.  
     * @throws IOException if there was a problem with the URL including connecting to it, reading the connection's 
     * input stream or closing the connection.
     */
    @SuppressWarnings("unused")
    private void generalURLTest(String urlStr, String username, Class<?> resultEncodingClass) throws IOException{
        
        //Nuxeo is case sensitive, so make all GIFT user names lowercase
        username = toSafeUsername(username);
        
        URL url = constructURL(urlStr, null);
        HttpURLConnection connection = getConnection(url, username);  
        try{

            BufferedReader in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            
            if(resultEncodingClass != null){
                Gson gson = new Gson();
                Object encodingClassInstance = gson.fromJson(in, resultEncodingClass);
                System.out.println("decoded = "+encodingClassInstance);
            }else{
                String line = in.readLine();
                while(line != null){
                    System.out.println(line);
                    line = in.readLine();
                }
            }
    
            in.close();
        }finally{
            if(connection != null){
                connection.disconnect();
            }
        }

    }
    
    /**
     * Checks to see if a document(file or folder) with the title at the provided path exists
     * @param path Repository search path. Assumes default workspace of '/default-domain/workspaces' if none
     * is supplied. If the title starts with {@value #DEFAULT_WORKSPACE_ROOT} the path set to an empty string.
     * @param title Name/title of the file or folder.  Can't be null. Backward slashes will be replaced with forward slashes.
     * e.g. "Rule Content\\B\\High Motivation Journeyman B - Rule.lessonMaterial.xml' is acceptable.  If this starts with {@value #DEFAULT_WORKSPACE_ROOT}
     * the path set to an empty string.
     * @param isFolder Parameter should be set to true if the document type is a folder
     * @param username User with authority to access the document
     * @return True if document file/folder exists, false otherwise
     * @throws IOException May be thrown if a server error occurs
     */
    public boolean documentExists(String path, String title, boolean isFolder, String username) throws IOException {        
        
        // this Nuxeo API can't handle backward slashes
        title = title.replace("\\", "/");
        
        // make sure the title or path starts with the workspaces root
        if(title.startsWith(DEFAULT_WORKSPACE_ROOT_NOFOLDER)){
            // the title already has the workspace root, don't need to prefix it later on with that string
            path = "";
        }else if (path == null || path.isEmpty()) {
            // the title needs the workspace root
            path = DEFAULT_WORKSPACE_ROOT;
        } else {
            // check the prefix path for the appropriate structure, alter it if needed
            path = prependDefaultWorkspacePath(path);
        }
        
        // if the path is not an empty string, make sure the path ends with a '/' before setting it as the prefix in the full path with the title
        if(!path.isEmpty() && !path.endsWith(Constants.FORWARD_SLASH)){
            path += Constants.FORWARD_SLASH;
        }
        
        //Nuxeo is case sensitive, so make all GIFT user names lowercase
        username = toSafeUsername(username);
        
        // make sure apostrophes are escaped
        title = escapeQuotes(title);
        path = escapeQuotes(path);
        
        Session session = getSession(username);
        StringBuilder whereClause = new StringBuilder();
        //MH: original was using 'STARTSWITH' but that will return documents with the same name in subfolders 
        //    which is not what we want
        whereClause.append(" where ecm:path = '").append(path).append(title).append("'");
//        whereClause.append(" AND dc:title = '").append(title).append("'");
//        whereClause.append( " AND ecm:path STARTSWITH '").append(path).append("'");
        if (!isFolder) {
            // <> means 'not like'
            whereClause.append(" AND ecm:primaryType <> 'Folder'");
        }
        
        try{
            Documents docs = (Documents) session.newRequest(DocumentService.Query).set("query", SELECT_STAR_FROM_DOCUMENT + whereClause).execute();        
            return !docs.isEmpty();
        }catch(RemoteException remoteException){
            throw new IOException("Failed to check if the file '"+path+Constants.FORWARD_SLASH+title+"' exists because "+remoteException.getMessage()+".", remoteException);
        }
    }
    
    /**
     * Create a Nuxeo document at the specified workspace location.  Then upload the content file as the "Main Content" for the nuxeo
     * document.  The document name/title will be the content file name (no path).  If a document already exists at the workspace location and 
     * with the same name then it will be replaced.
     * 
     * @param workspacePath the folder to create the new Nuxeo document.  This path should start after the "/default-domain/workspaces/" part of the workspace path
     * (e.g. "/default-domain/workspaces/Public" would be "Public") or include that prefix.  The path must exist for this logic to work. Can't be null.
     * @param fileName the name of the file being created. Must include the extension. Must not include the path. Can't be null or empty. (e.g. 'myCreatedDocument.txt')
     * @param courseFolderPath the path to the course folder.  Can't be null or empty.  Can include the default workspace path of "/default-domain/workspaces/".
     * This is used to update the last modified date of the course folder in Nuxeo since Nuxeo doesn't handle this automatically.
     * @param fileContents the contents of the file to upload and associate with the Nuxeo document.  Can't be null and must exist.
     * @param username used for authentication
     * @return the automation Nuxeo document information for the created document. 
     * @throws IOException if there was a problem getting the client session or updating the content file for the document
     * @throws mil.arl.gift.net.nuxeo.QuotaExceededException Thrown if file upload causes quota to be exceeded
     * @throws DocumentLockedException thrown if the document with the same name exists and is locked
     * @throws WritePermissionException thrown if the file being created can't be written due to permissions
     */
    public org.nuxeo.ecm.automation.client.model.Document createDocument(String workspacePath, String fileName, String courseFolderPath, InputStream fileContents, String username) 
    		throws IOException, QuotaExceededException, DocumentLockedException, WritePermissionException {
    	return createDocument(workspacePath, fileName, courseFolderPath, fileContents, username, false);
    }
    
    /**
     * Create a Nuxeo document at the specified workspace location.  Then upload the content file as the "Main Content" for the nuxeo
     * document.  The document name/title will be the content file name (no path).  If a document already exists at the workspace location and 
     * with the same name then it will be replaced.
     * 
     * @param workspacePath the folder to create the new Nuxeo document.  This path should start after the "/default-domain/workspaces/" part of the workspace path
     * (e.g. "/default-domain/workspaces/Public" would be "Public") or include that prefix.  Also supported is the internal "Workspaces" prefix (e.g. "Workspaces/Public").
     * The path must exist for this logic to work. Can't be null.
     * @param fileName the name of the file being created. Must include the extension. Must not include the path. Can't be null or empty. (e.g. 'myCreatedDocument.txt')
     * @param courseFolderPath the path to the course folder. Can include the default workspace path of "/default-domain/workspaces/".
     * This is used to update the last modified date of the course folder in Nuxeo since Nuxeo doesn't handle this automatically.
     * @param fileContents the contents of the file to upload and associate with the Nuxeo document.  Can't be null and must exist.
     * @param username used for authentication
     * @param useAdminPrivilege true to use admin privileges to create the document
     * @return the automation Nuxeo document information for the created document. 
     * @throws IOException if there was a problem getting the client session or updating the content file for the document
     * @throws mil.arl.gift.net.nuxeo.QuotaExceededException Thrown if file upload causes quota to be exceeded
     * @throws DocumentLockedException thrown if the document with the same name exists and is locked
     * @throws WritePermissionException thrown if the file being created can't be written due to permissions
     */
    public org.nuxeo.ecm.automation.client.model.Document createDocument(String workspacePath, String fileName, String courseFolderPath, InputStream fileContents, String username, boolean useAdminPrivilege) 
    		throws IOException, QuotaExceededException, DocumentLockedException, WritePermissionException {
        
        if(workspacePath == null){
            throw new IllegalArgumentException("The workspace path can't be null.");
        }else if(fileContents == null){
            throw new IllegalArgumentException("The file contents can't be null");
        }else if(fileName == null || fileName.length() == 0){
            throw new IllegalArgumentException("The file name can't be null or empty");
        }
        
        //remove leading/trailing spaces
        fileName = fileName.trim();
        
        //Nuxeo is case sensitive, so make all GIFT user names lowercase
        username = toSafeUsername(username);
        
        //the path to the parent folder where the new file should be created
        //needs to have the default workspace to identify it as an absolute path in Nuxeo
        String workspacePathValue = workspacePath;
        if(workspacePathValue.startsWith(WORKSPACES)){
            //remove this prefix since the DEFAULT_WORKSPACE_ROOT value will be added later (below)
            workspacePathValue = workspacePathValue.replaceFirst(Pattern.quote(WORKSPACES), Constants.EMPTY);
            
            //remove file separator now at beginning of path, otherwise your likely to get a path with two forward slashes, e.g. /default-domain/workspaces//blah/blah
            if(workspacePathValue.length() > 1){
                workspacePathValue = workspacePathValue.substring(1);
            }
        }
        
        if(!workspacePathValue.endsWith(Constants.FORWARD_SLASH)) {
        	workspacePathValue += Constants.FORWARD_SLASH;
        }
        
        if(!workspacePathValue.startsWith(DEFAULT_WORKSPACE_ROOT)){
            workspacePathValue = DEFAULT_WORKSPACE_ROOT + workspacePathValue;
        }
        
        boolean documentExists = documentExists(workspacePathValue, fileName, false, username);
        if (documentExists) {
        	
        	Document document = getDocumentByName(workspacePath, fileName, username);
        	deleteDocument(document.getId(), null, username, useAdminPrivilege);
        }
        Session session = useAdminPrivilege ? getSession(adminUser) : getSession(username);
        
        // Fetch the root of Nuxeo repository
        org.nuxeo.ecm.automation.client.model.Document root;
        try{
            root = (org.nuxeo.ecm.automation.client.model.Document) session.newRequest(DocumentService.FetchDocument).set("value", workspacePathValue).execute();
        }catch(RemoteException remoteException){
            throw new IOException("Failed to retrieve the folder '"+workspacePathValue+"' because "+remoteException.getMessage()+".", remoteException);
        }
         
        // Instantiate a new Document with the simple constructor
        org.nuxeo.ecm.automation.client.model.Document document = new org.nuxeo.ecm.automation.client.model.Document(fileName, DocumentTypes.File.name());
        document.set("dc:title", fileName);
//        document.set("dc:description", "My Description");
        
        // Create a document of File type by setting the parameter 'properties' with String metadata values delimited by comma ','
        DocumentEntityType documentEntityType;
        try{
            document = (org.nuxeo.ecm.automation.client.model.Document) session.newRequest(DocumentService.CreateDocument).setHeader(org.nuxeo.ecm.automation.client.Constants.HEADER_NX_SCHEMAS, "*").setInput(root).set("type", document.getType()).set("name", document.getId()).set("properties", document).execute();
            
            documentEntityType = new DocumentEntityType(document);
        } catch (RemoteException e) {

            Throwable cause = e.getCause();
            if (cause == null) {
                throw e;
            }
            
            // Error is inside the message string of the caused by throwable.
            // String parsing is not optimal, but allows us to get the user 
            // that locked the document
            String message = cause.getMessage();
            if(message != null){
                String matchString = "Document already locked by ";
                int startIndex = message != null ? message.indexOf(matchString) : -1;
                if (startIndex != -1) {
                    startIndex += matchString.length(); // adjust start index
                    int endIndex = message.indexOf(":", startIndex); // user ends with a :
                    String user = message.substring(startIndex, endIndex);
                    throw new DocumentLockedException("Document is already locked by user: " + user);
                } 
                
                // determine if the error is a write permissions issue
                matchString = "Privilege 'WriteProperties' is not granted to";
                startIndex = message != null ? message.indexOf(matchString) : -1;
                if (startIndex != -1) {
                    throw new WritePermissionException("'"+username+"' doesn't have write access to document.");
                }
                
                // again, determine if the error is a write permissions issue
                matchString = "Privilege 'AddChildren' is not granted to";
                startIndex = message != null ? message.indexOf(matchString) : -1;
                if (startIndex != -1) {
                    throw new WritePermissionException("'"+username+"' doesn't have write access to document.");
                }
            }
            
            // some other type of error occurred so rethrow
            throw new IOException("Failed to create the file of '"+root.getPath() + fileName + ".", e);

        }
        
        //retrieve the course folder document in order to update the last modified date later on -- this is best effort
        DocumentEntityType courseFolderDocument = null;
        
        if(courseFolderPath != null){
	        
	        try{
	            //passing empty string as the workspacePath because the folder path already has the default workspace path in it
	            courseFolderDocument = getFolderEntityByName(Constants.EMPTY, courseFolderPath, username);
	            
	        }catch(@SuppressWarnings("unused") Exception e){
	            courseFolderDocument = documentEntityType;
	        }
	        
        } else {
        	courseFolderDocument = documentEntityType;
        }
        
        try {
      		updateDocumentFile(documentEntityType, courseFolderDocument, fileContents, username, useAdminPrivilege);
        }catch (QuotaExceededException qe) {
        	// delete the document object on the server since no file is associated
            deleteDocument(document.getId(), null, username);
            throw qe;
        }catch (DetailedException de) {
            // delete the document object on the server since no file is associated
            deleteDocument(document.getId(), null, username);
            throw de;
        }
        
        
        return document;
    }
    
    /**
     * Deletes a Nuxeo Document uniquely identified by the specified id.
     * 
     * @param id a unique nuxeo document id
     * @param courseFolder contains the document being deleted.  Used to update the last modified date.  If null the last modified
     * date will not be updated.
     * @param username  the authority to use to complete this interaction
     * @throws IOException if there was a problem connecting/disconnecting to the Nuxeo API URL or reading from the connection.
     */
    public void deleteDocument(String id, DocumentEntityType courseFolder, String username) throws IOException{
        deleteDocument(id, courseFolder, username, false);
    }

    /**
     * Deletes a Nuxeo Document uniquely identified by the specified id.
     * 
     * @param id a unique nuxeo document id
     * @param courseFolder contains the document being deleted.  Used to update the last modified date.  If null the last modified
     * date will not be updated.
     * @param username  the authority to use to complete this interaction
     * @param useAdminPrivilege whether to use the admin user for this operation which is needed if the user doesn't have
     * write access (e.g. Public workspace course folder)
     * @throws IOException if there was a problem connecting/disconnecting to the Nuxeo API URL or reading from the connection.
     */
    public void deleteDocument(String id, DocumentEntityType courseFolder, String username, boolean useAdminPrivilege) throws IOException{
        
        //Nuxeo is case sensitive, so make all GIFT user names lowercase
        String permissionUser = useAdminPrivilege ? adminUser : toSafeUsername(username);
        
        URL url = constructURL(DELETE_DOCUMENT_URL, id);
        HttpURLConnection connection = getConnection(url, permissionUser);  
        try{
            connection.setRequestMethod(DELETE);
    
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            
            String line = in.readLine();
            while(line != null){
                System.out.println(line);
                line = in.readLine();
            }
    
            in.close();
        }finally{
            if(connection != null){
                connection.disconnect();
            }
        }
        
        if(courseFolder != null){
            updateCourseFolderLastModified(courseFolder, username, useAdminPrivilege);
        }
    }
    
    /**
     * Deletes a Nuxeo Folder unique identified by the specified id.
     * 
     * @param id a unique nuxeo folder id
     * @param courseFolderDocument contains the document being deleted.  Used to update the last modified date.  If null the last modified
     * date will not be updated.
     * @param username the authority to use to complete this interaction
     * @throws IOException if there was a problem connecting/disconnecting to the Nuxeo API URL or reading from the connection.
     */
    public void deleteFolder(String id, DocumentEntityType courseFolderDocument, String username) throws IOException{
        
        //Nuxeo is case sensitive, so make all GIFT user names lowercase
        username = toSafeUsername(username);
        
        deleteDocument(id, courseFolderDocument, username);
    }
    
    /**
     * Return the last modified date value of the course folder.
     * 
     * @param courseFolderPath the path to the course folder.  This can start with '/default-domain/workspaces' or 'Workspaces' or from the
     * workspace folder name such as 'Public' or 'mhoffman'. 
     * @param username used to authenticate the request to nuxeo
     * @return the last modified date for the folder.  
     * @throws IOException if the course folder path is invalid or there was a problem communicating with nuxeo
     */
    public Date getLastModified(String courseFolderPath, String username) throws IOException{
        
        Document document = getDocumentByName(Constants.EMPTY, courseFolderPath, username);
        
        if(document != null){        
            return document.getLastModified();
        }else{
            return null;
        }
        
    }
    
    /**
     * Deletes a Nuxeo folder identified by the specified path.
     * 
     * @param workspacePath the folder to delete.  This path should start after the "/default-domain/workspaces/" part of the workspace path
     * (e.g. "/default-domain/workspaces/Public" would be "Public") or include that prefix.  Also supported is the internal "Workspaces" prefix (e.g. "Workspaces/Public").
     * The path must exist for this logic to work. Can't be null.
     * @param username the authority to use to complete this interaction.  The username should be case insensitive.
     * @throws IOException if there was a problem connecting/disconnecting to the Nuxeo API URL or reading from the connection.
     */
    private void deleteWorkspaceFolderCaseSensitive(String workspacePath, String username) throws IOException{
        
        if(workspacePath == null){
            throw new IllegalArgumentException("The workspace folder path can't be null.");
        }
        
        //the path to the parent folder where the new folder should be created
        //needs to have the default workspace to identify it as an absolute path in Nuxeo
        String workspacePathValue = workspacePath;
        if(workspacePathValue.startsWith(WORKSPACES)){
            //remove this prefix since the DEFAULT_WORKSPACE_ROOT value will be added later (below)
            workspacePathValue = workspacePathValue.replaceFirst(Pattern.quote(WORKSPACES), Constants.EMPTY);
            
            //remove file separator now at beginning of path, otherwise your likely to get a path with two forward slashes, e.g. /default-domain/workspaces//blah/blah
            if(workspacePathValue.length() > 1){
                workspacePathValue = workspacePathValue.substring(1);
            }
        }
        
        if(!workspacePathValue.endsWith(Constants.FORWARD_SLASH)) {
            workspacePathValue += Constants.FORWARD_SLASH;
        }
        
        if(!workspacePathValue.startsWith(DEFAULT_WORKSPACE_ROOT)){
            workspacePathValue = DEFAULT_WORKSPACE_ROOT + workspacePathValue;
        }
        
        URL url = constructURL(PATH_URL, workspacePathValue);
        HttpURLConnection connection = getConnection(url, username);  
        try{
            connection.setRequestMethod(DELETE);
    
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            
            String line = in.readLine();
            while(line != null){
                System.out.println(line);
                line = in.readLine();
            }
    
            in.close();
        }finally{
            if(connection != null){
                connection.disconnect();
            }
        }
    }
    
    /**
     * Deletes a Nuxeo folder identified by the specified path.
     * 
     * @param workspacePath the folder to delete.  This path should start after the "/default-domain/workspaces/" part of the workspace path
     * (e.g. "/default-domain/workspaces/Public" would be "Public") or include that prefix.  The path must exist for this logic to work. Can't be null.
     * @param courseFolder contains the document being deleted.  Used to update the last modified date.  If null the last modified
     * date will not be updated.
     * @param username the authority to use to complete this interaction
     * @throws IOException if there was a problem connecting/disconnecting to the Nuxeo API URL or reading from the connection.
     */
    public void deleteWorkspaceFolder(String workspacePath, DocumentEntityType courseFolder, String username) throws IOException{
        
        if(workspacePath == null){
            throw new IllegalArgumentException("The workspace folder path can't be null.");
        }
        
        //Nuxeo is case sensitive, so make all GIFT user names lowercase
        username = toSafeUsername(username);
        
        deleteWorkspaceFolderCaseSensitive(workspacePath, username);
        
        if(courseFolder != null){
            updateCourseFolderLastModified(courseFolder, username, false);
        }
    }
    
    private void deleteUserWorkspace(String username) throws IOException {
        //Nuxeo is case sensitive, so make all GIFT user names lowercase
        username = toSafeUsername(username);
        
        // admin creates the user workspace so must also delete it
        deleteWorkspaceFolderCaseSensitive(username, adminUser);
    }
    
    /**
     * 
     * @param workspacePath the folder where the Nuxeo document to rename is located.  This path should start after the "/default-domain/workspaces/" part of the workspace path
     * (e.g. "/default-domain/workspaces/Public" would be "Public").  Can't be null.
     * @param currentName the current name of the document (file or folder) that will be renamed
     * @param newName the new name of the document.  Can't be null or empty.
     * @param username used for permission check.  Can't be null or empty.
     * @throws DocumentException if the current document doesn't exist
     * @throws DocumentExistsException if a document already exists with the new name
     * @throws {@link DetailedException} if there is a problem retrieving any document or parent folder, updating the document, 
     * moving the document or updating the documents attached file.
     * 
     */
    public void renameDocument(String workspacePath, String currentName, String newName, String username) 
            throws DocumentException, DocumentExistsException, DetailedException{
        
        if(newName == null || newName.isEmpty()){
            throw new IllegalArgumentException("The new name can't be null or empty.");
        }
        
        // Retrieve the document that will be renamed
        Document currentDocument;
        try{
            currentDocument = getDocumentByName(workspacePath, currentName, username);            
        }catch(Exception e){
            throw new DocumentException("Failed to find the file " + workspacePath + "/" + currentName + ".", e);
        }

        /* If the user has write permissions for the course folder, then use
         * admin privileges to modify the user workspace for the rename. This is
         * necessary because a rename requires access to the user workspace
         * which only the user it belongs to has permission for, even though a
         * course folder within that workspace can be shared and modified by
         * many users. */
        if (hasWritePermissions(currentDocument, username)) {
            username = adminUser;
        }

        // Check if another document exists with the new document name
        Document newDocument;
        try{
            newDocument = getDocumentByName(workspacePath, newName, username);
            if(newDocument != null){
                throw new DocumentExistsException("The file " + workspacePath + "/" + newName + " already exists.");
            }
        }catch(@SuppressWarnings("unused") Exception e){
            //this is good, means there will be no file naming conflict 
        }
        
        //where the document is being changed in
        Document targetFolder;
        try{
            targetFolder = getDocumentByName(workspacePath, username);
        }catch(Exception e){
            throw new DetailedException("Failed to rename the file",
                    "There was a problem retrieving the folder '"+workspacePath+"' where the file to rename resides.", 
                    e);
        }
        
        Session session;
        try{
            session = getSession(username);
        }catch(Exception e){
            throw new DetailedException("Failed to rename the file",
                    "There was a problem retrieving the session to execute the nuxeo logic.", 
                    e);
        }
         
        //update the title property (this is what is shown on the nuxeo dashboard)
        currentDocument.set("dc:title", newName);
        
        // The DocumentService will give you some nice shortcut to the most common operations
        DocumentService documentService = session.getAdapter(DocumentService.class);
        
        // push the update of the title property to the nuxeo db
        Document updatedDocument;
        try{
            updatedDocument = documentService.update(currentDocument);
        }catch(Exception e){
            throw new DetailedException("Failed to rename the file",
                    "There was a problem updating the title of the file '"+currentDocument.getPath()+"'.", 
                    e);
        }
      
        // update the document path to the new name value by calling move operation
        try{
            session.newRequest(DocumentService.MoveDocument).setInput(updatedDocument)
                .set("name", newName)
                .set("target", targetFolder)
                .execute();
        }catch(Exception e){
            throw new DetailedException("Failed to rename the file",
                    "There was a problem updating the path of the file '"+currentDocument.getPath()+"'.", 
                    e);
        }

        // Update the name of the document's attachment/blob, which is the file gift downloads and uses
        if(!isFolder(currentDocument)){
            
            //try to get the blob from the property
            //Note: in theory this property should be populated but in practice it is null
            Blob blob = (Blob)updatedDocument.getProperties().get("file:content");
            if(blob != null){
                blob.setFileName(newName);
            }else{
                
                try{
                    //get a connection to the document
                    NuxeoDocumentConnection nuxeoDocumentConnection = getDocumentConnection(updatedDocument, username);
                    
                    // Create a blob object from the file to upload
                    // We pass null as the ctype argument which is a mime type. This results 
                    // in an application/octet-stream mime type, but GIFT probably doesn't care
                    // about the type at this point, like a web browser would.
                    blob = new StreamBlob(nuxeoDocumentConnection.getInputStream(), newName, null);
                }catch(Exception e){
                    throw new DetailedException("Failed to rename the file",
                            "Failed to retrieve the Nuxeo 'blob' from the document named '"+ updatedDocument.getPath() + "/" +  updatedDocument.getTitle() +"'.", 
                            e);
                }
            }

            // Create a document Ref object with the id of the document
            DocRef docRef = new DocRef(updatedDocument.getId());
            
            //this will update the "Main Content" (or file) of the Nuxeo document
            try {
                documentService.setBlob(docRef, blob);
            } catch (Exception e) {
                    throw new DetailedException("Failed to rename the file",
                            "Failed to set the Nuxeo 'blob' to the document named '"+ updatedDocument.getPath() + "/" +  updatedDocument.getTitle() +"'.", 
                            e);
            }  
        }
        
    }
    
    /**
     * Create a Nuxeo folder at the specified workspace location.  If a folder already exists at the workspace location an exception will be thrown.
     * 
     * @param workspacePath the folder to create the new Nuxeo document.  This path can either start after the "/default-domain/workspaces/" part of the workspace path
     * (e.g. "/default-domain/workspaces/Public" would be "Public") or include that prefix. Also supported is the internal "Workspaces" prefix (e.g. "Workspaces/Public").
     * The path must exist for this logic to work. Can't be null.
     * @param folderName the file to upload as the content file for the Nuxeo document being created.  Can't be null.
     * @param username used for authentication
     * @param createAncestorFolders set to true if ancestor folders in the workspacepath should also be created in this call.
     * @param useAdminPrivilege True to to create the folder with admin privileges.
     * @return the automation Nuxeo document information for the created folder. 
     * @throws IOException if there was a problem getting the client session
     * @throws mil.arl.gift.net.nuxeo.DocumentExistsException Thrown if folder with name already exists
     */
    public org.nuxeo.ecm.automation.client.model.Document createWorkspaceFolder(String workspacePath, String folderName, String username, boolean createAncestorFolders) 
    		throws IOException, DocumentExistsException {
        
        //don't allow leading/trailing spaces
        folderName = folderName.trim();
        
        //Nuxeo is case sensitive, so make all GIFT user names lowercase
        username = toSafeUsername(username);
        
        // prevent duplicate folder names/titles
        boolean folderExists = documentExists(workspacePath, folderName, true, username);
        if (folderExists) {
            throw new DocumentExistsException(String.format("Folder with name '%s/%s' already exists", workspacePath, folderName));
        }        
        
        if(createAncestorFolders){
            //create any missing ancestor folders
            String[] ancestorFolders = workspacePath.split(Constants.FORWARD_SLASH);
            String path = Constants.EMPTY;
            for(String ancestor : ancestorFolders){
                
                if(ancestor.length() == 0){
                    //the first character in the string could be a '/' which results in a "" entry in the list
                    continue;
                }else if(path.length() == 0 && DEFAULT_WORKSPACE_ROOT.contains(ancestor)){
                    //this is part of the default workspace path
                    continue;
                }
                
                if(!documentExists(path, ancestor, true, username)){
                    //create this missing ancestor folder
                    createWorkspaceFolder(path, ancestor, username, false);
                }
                
                if(path.length() > 0){
                    //extending path therefore need separator
                    path +=  Constants.FORWARD_SLASH + ancestor;
                }else{
                    path = ancestor;
                }
                
            }
        }
        
        Session session = getSession(username);
        
        //the path to the parent folder where the new folder should be created
        //needs to have the default workspace to identify it as an absolute path in Nuxeo
        String workspacePathValue = workspacePath;
        if(workspacePathValue.startsWith(WORKSPACES)){
            //remove this prefix since the DEFAULT_WORKSPACE_ROOT value will be added later (below)
            workspacePathValue = workspacePathValue.replaceFirst(Pattern.quote(WORKSPACES), Constants.EMPTY);
            
            //remove file separator now at beginning of path, otherwise your likely to get a path with two forward slashes, e.g. /default-domain/workspaces//blah/blah
            if(workspacePathValue.length() > 1){
                workspacePathValue = workspacePathValue.substring(1);
            }
        }
        
        if(!workspacePathValue.endsWith(Constants.FORWARD_SLASH)) {
            workspacePathValue += Constants.FORWARD_SLASH;
        }
        
        if(!workspacePathValue.startsWith(DEFAULT_WORKSPACE_ROOT)){
            workspacePathValue = DEFAULT_WORKSPACE_ROOT + workspacePathValue;
        }
        
        // Fetch the root of Nuxeo repository
        org.nuxeo.ecm.automation.client.model.Document root;
        try{
            root = (org.nuxeo.ecm.automation.client.model.Document) session.newRequest(DocumentService.FetchDocument).set("value", workspacePathValue).execute();
        }catch(RemoteException remoteException){
            throw new IOException("Failed to retrieve the folder '"+workspacePathValue+"' because "+remoteException.getMessage()+".", remoteException);
        }
         
        // Instantiate a new Document with the simple constructor
        org.nuxeo.ecm.automation.client.model.Document document = new org.nuxeo.ecm.automation.client.model.Document(folderName, DocumentTypes.Folder.name());
        document.set("dc:title", folderName);
//        document.set("dc:description", "My Description");
        
        // Create a document of File type by setting the parameter 'properties' with String metadata values delimited by comma ','
        try{
            document = (org.nuxeo.ecm.automation.client.model.Document) session.newRequest(DocumentService.CreateDocument).setHeader(org.nuxeo.ecm.automation.client.Constants.HEADER_NX_SCHEMAS, "*").setInput(root).set("type", document.getType()).set("name", document.getId()).set("properties", document).execute();    
            return document;
        }catch(RemoteException remoteException){
            throw new IOException("Unable to create the workspace folder of '"+workspacePathValue+Constants.FORWARD_SLASH+folderName+".", remoteException);
        }
    }
    
    /** 
     * Creates a folder within the TrainingAppsLib directory for the specified user and sets access
     * rights
     * 
     * @param username the user to create a folder for
     * @return the created folder
     * @throws IOException if there was a problem creating the folder
     */
    public Document createTrainingAppsLibUserFolder(String username) throws IOException {
        Session session = getSession(adminUser);

        // Fetch the root of Nuxeo repository
        Document trainingAppsLib;
        try {
            trainingAppsLib = getTrainingAppsLibFolder(username);
        } catch (RemoteException remoteException) {
            throw new IOException(
                    "Unable to retrieve the training apps lib folder because " + remoteException.getMessage() + ".",
                    remoteException);
        }

        // Instantiate a new Document with the simple constructor
        Document newFolder = new Document(username, DocumentTypes.Folder.name());

        // Set title and description of new workspace
        newFolder.set("dc:title", username);
        newFolder.set("description", username + "'s Training Apps Lib Folder");

        // Create the new workspace
        try {
            newFolder = (Document) session.newRequest(DocumentService.CreateDocument)
                    .setHeader(org.nuxeo.ecm.automation.client.Constants.HEADER_NX_SCHEMAS, "*")
                    .setInput(trainingAppsLib).set("type", newFolder.getType()).set("name", newFolder.getId())
                    .set("properties", newFolder).execute();

            setUserWorkspacePermissions(newFolder, username);
            setWorkspaceQuotaByIdCaseSensitive(newFolder.getId(), userWorkspaceQuota, adminUser);

            return newFolder;
        } catch (RemoteException remoteException) {
            throw new IOException("Unable to create training apps lib folder for " + username + " because "
                    + remoteException.getMessage() + ".", remoteException);
        }
    }

    /** 
     * Creates a workspace for an existing user and sets access rights
     *  
     * @param user the user to create a workspace for
     * @return the created workspace
     * @throws IOException if there was a problem creating the workspace
     */
    public Document createUserWorkspace(UserEntityType user) throws IOException {
        // TODO can we use the user to create their own workspace?
        // Current it errors out with "Privilege 'AddChildren' is not granted" 
        Session session = getSession(adminUser);
    	String desc = user.getProperties().getFirstName() + "'s Workspace";
    	String usernameToCreate = user.getProperties().getUsername();
    	
        // Fetch the root of Nuxeo repository
    	Document root;
    	try{
            root = (Document) session.newRequest(DocumentService.FetchDocument).set("value", DEFAULT_WORKSPACE_ROOT).execute();
    //        Document root = (Document) session.newRequest("Document.Fetch").set("value", USER_WORKSPACE_ROOT).execute();
    	}catch(RemoteException remoteException){
            throw new IOException("Unable to retrieve the root workspace of '"+DEFAULT_WORKSPACE_ROOT+"' because "+remoteException.getMessage()+".", remoteException);
        }
                       
        // Instantiate a new Document with the simple constructor
        Document workspace = new Document(usernameToCreate, DocumentTypes.Workspace.name());
            
        // Set title and description of new workspace
        workspace.set("dc:title", usernameToCreate);
        workspace.set("description", desc);
                        
        // Create the new workspace
        try{
        	workspace = (Document) session.newRequest(DocumentService.CreateDocument).
        		setHeader(org.nuxeo.ecm.automation.client.Constants.HEADER_NX_SCHEMAS, "*").
        		setInput(root).set("type", workspace.getType()).set("name", workspace.getId()).
        		set("properties", workspace).execute();
    
            setUserWorkspacePermissions(workspace, usernameToCreate);
            setWorkspaceQuotaByIdCaseSensitive(workspace.getId(), userWorkspaceQuota, adminUser);
        	
        	return workspace;
        }catch(RemoteException remoteException){
            throw new IOException("Unable to create workspace for "+usernameToCreate+" because "+remoteException.getMessage()+".", remoteException);
        }
    }
    
    /**
     * Set the user's workspace permissions, allowing full access to user and blocking others
     * @param workspaceDoc Workspace document, if null the user's workspace will be retrieved
     * @param username User who owns the workspace
     * @throws IOException Thrown if server error occurs
     */
    // package private for unit test
    void setUserWorkspacePermissions(Document workspaceDoc, String username) throws IOException {
        if (workspaceDoc == null) {
            workspaceDoc = getDocumentByNameCaseSensitive(DEFAULT_WORKSPACE_ROOT + username, adminUser);
        }
        
        //Nuxeo is case sensitive, so make all GIFT user names lowercase
        username = toSafeUsername(username);
        
        Session session = getSession(adminUser);
        DocumentService ds = session.getAdapter(DocumentService.class);
        ds.removeAcl(workspaceDoc, "local"); // clear all local rights/permissions
        // grant user all permissions and block everyone else
        try{
            session.newRequest(SET_ACE_COMMAND).setInput(workspaceDoc).set(ACE_USER_ARG, username)
                .set(ACE_PERMISSION_ARG, PermissionsEntityType.PERMISSION_EVERYTHING).set(ACE_GRANT_ARG, true).set(ACE_OVERWRITE_ARG, false).execute();
            session.newRequest(SET_ACE_COMMAND).setInput(workspaceDoc).set(ACE_USER_ARG, "Everyone")
                .set(ACE_PERMISSION_ARG, PermissionsEntityType.PERMISSION_EVERYTHING).set(ACE_GRANT_ARG, false).set(ACE_OVERWRITE_ARG, false).execute();
        }catch(RemoteException remoteException){
            throw new IOException("Unable to set workspace permission for "+username+" on document '"+workspaceDoc.getPath()+"' because "+remoteException.getMessage()+".", remoteException);
        }
    }
    
    /**
     * Sets a single user permissions on the course folder specified.
     * 
     * @param courseFolderDoc the existing nuxeo course folder nuxeo document that needs local user permissions update.  If null
     * this method does nothing.  The ACL (e.g. read/write) permissions are set on this document and child documents (e.g. course.xml) will inherit these permissions.
     * @param courseXMLDoc the existing nuxeo course folder's course.xml nuxeo document that might need additional custom GIFT user permissions updated.
     * If null this method does nothing.  The custom GIFT permissions are stored in this document's 'rights' attribute as a JSON string.
     * @param permissions the permissions for one or more users to apply.  This could contain a null permission enum which means 
     * the user permissions should be removed for the specified user.
     * @param progressIndicator used to provide progress on updating course folder permissions.  Can be null.  Updates progress from 0 to 100 percent.
     * @throws IOException if there was a problem applying the change in permissions, either removing/changing/editing.
     */
    @SuppressWarnings("unchecked")
    public void setUserCoursePermissions(Document courseFolderDoc, Document courseXMLDoc, Set<DomainOptionPermissions> permissions, ProgressIndicator progressIndicator) throws IOException {

        // do nothing if there is no course document or permissions
        if (courseFolderDoc == null || courseXMLDoc == null || CollectionUtils.isEmpty(permissions)) {
            return;
        }
        
        DocumentService ds = null;

        Session session = getSession(adminUser);

        // Nuxeo is case sensitive, so make all GIFT user names lowercase
        Map<String, DomainOptionPermissions> usernameToPermissions = new HashMap<>();
        for(DomainOptionPermissions permission : permissions) {
            String username = toSafeUsername(permission.getUser());
            usernameToPermissions.put(username, permission);
        }
         
        try {
            
            PermissionsEntityType currentPermissions = getPermissionsEntity(courseFolderDoc);

            // First:
            // remove any permissions for each user provided to this method that maybe on this document
            // (This is necessary even if changing/adding permissions because Nuxeo can store both Read and ReadWrite for a single user)
            
            if(progressIndicator != null) {
                progressIndicator.setPercentComplete(0);
                progressIndicator.setTaskDescription("Gathering course folder permissions");
            }

            // final permissions to apply (in Acl attribution)
            // contains each user Acl permissions
            Acl storedAccessControlList = new Acl();
            
            // map of users from currentPermissions that already have Acl permissions on the document
            Map<String, AceEntity> usernameToAcessControlEntries = new HashMap<>();
            
            if (currentPermissions.getAclList() != null) {
                // there are permissions on this document already, go through each
                
                for (Acl currentAccessControlList : currentPermissions.getAclList()) {
                    if (LOCAL_ACL.equals(currentAccessControlList.getName())) {
                        // want to look at the local rights, not inherited rights
                        
                        for (AceEntity currentAce : currentAccessControlList.getAceList()) {
                            
                            String currentUser = currentAce.getUsername();
                            boolean found = false;
                            
                            // Note: don't think we can assume the username case will be the same, so can't use Map.contains here
                            for(String username : usernameToPermissions.keySet()) {
                                if (currentUser.equalsIgnoreCase(username)) {
                                    // user provided to this method already has some Acl permissions to this document,
                                    // add to collection of user permissions that need to be modified
                                    usernameToAcessControlEntries.put(username, currentAce);
                                    found = true;
                                    break;
                                }                                    
                            }
                            
                            if(!found) {
                                // the user has permissions and the method caller is NOT wanting to change that user
                                
                                if (!(ADMINISTRATOR_USERNAME.equals(currentUser) || ADMINISTRATORS_GROUP.equals(currentUser))) {
                                    // found an Acl permission on the document for a user not provided to this method (and is not an admin user),
                                    // keep record of the ACE Entities that we don't want to remove.
                                    // Skip the admin user and group because these are inherited.
                                    if(!storedAccessControlList.getAceList().contains(currentAce)) {
                                        storedAccessControlList.getAceList().add(currentAce);
                                    }
                                }
                            }
                        }
                        
                    }
                    
                }
                
            }

            if (!usernameToAcessControlEntries.isEmpty()) {
                // none of the users that are having permissions applied were found on the document already,
                // then no need to clear the ACL first because we will just be adding new permissions for each user
                
                if(progressIndicator != null) {
                    progressIndicator.setPercentComplete(5);
                    progressIndicator.setTaskDescription("Updating course folder permissions");
                }
                
                ds = session.getAdapter(DocumentService.class);

                // remove local (not inherited) permissions for all users to then later recreate them
                ds.removeAcl(courseFolderDoc, LOCAL_ACL);
                
                if(!storedAccessControlList.getAceList().isEmpty()) {
                    // there are some permissions on the document that we want to keep, not change
                    
                    // used to update progress per for loop iteration
                    double progressIncrement = 55.0 / storedAccessControlList.getAceList().size();
                    int preTaskProgress = progressIndicator != null ? progressIndicator.getPercentComplete() : 0;
                    int taskComplete = 1;
                    
                    OperationRequest request = session.newRequest(SET_ACE_COMMAND).setInput(courseFolderDoc);
                    // add back the ones we didn't want to remove
                    for (AceEntity ace : storedAccessControlList.getAceList()) {
                        request.set(ACE_USER_ARG, ace.getUsername())
                                .set(ACE_PERMISSION_ARG, ace.getPermission()).set(ACE_GRANT_ARG, true).set(ACE_OVERWRITE_ARG, false);
                        
                        // TODO: figure out how to only do this to do this once for performance reasons
                        request.execute();
                        
                        if(progressIndicator != null) {
                            progressIndicator.setPercentComplete((int) (preTaskProgress + taskComplete * progressIncrement));
                            progressIndicator.setTaskDescription("Updating course folder permissions");
                            taskComplete++;
                        }
                    }  
                }

            }
            
            if(progressIndicator != null) {
                progressIndicator.setPercentComplete(60);
                progressIndicator.setTaskDescription("Updating course folder permissions");
            }

            // Second
            // 1. set Read (directly or inherited for Take Course) or Write permissions on the document through Access Control List's Access Control Entry
            // 2. set GIFT unique/specific permission in the 'Rights' document property - this is for permissions beyond just read and write - e.g. Take Course.
  
            // When a custom GIFT permission (not something ACL can handle e.g. read/write, such as Take Course only) set 'Rights' property value on the course.xml 
            // document (not the course folder document) because permissions will be later checked using the course.xml in this class. 
            // Get current rights which is only retrieve when querying with 'X-NXDocumentProperties' header parameter
            // Only want to do this once per method call.
            JSONObject currRightsJSON = new JSONObject();
            Document courseDocFull = (Document) session.newRequest(DocumentService.FetchDocument).setHeader("X-NXDocumentProperties", "*").set("value", courseXMLDoc.getPath()).execute();
            String currRights = courseDocFull.getProperties().getString(DC_RIGHTS_PROP);
            
            // get the json object to update
            if(StringUtils.isNotBlank(currRights)){
                // have a previous json string in the 'rights' attribute
                try{
                    currRightsJSON = (JSONObject) new JSONParser().parse(currRights);
                } catch (ParseException ex) {
                    logger.error("Failed to get the current document rights JSON object for on '"+courseXMLDoc.getPath()+"'.  Creating a new 'rights' object.", ex);
                }
            }
            
            // used to update progress per for loop iteration
            double progressIncrement = 40.0 / usernameToPermissions.size();
            int preTaskProgress = progressIndicator != null ? progressIndicator.getPercentComplete() : 0;
            int taskComplete = 1;
            
            // Cases:
            // 1. user has NO permissions
            //   1.1. adding take course permissions - add to document rights(*)
            //   1.2. adding view course permissions - add Read ACL
            //   1.3. adding edit course permissions - add Write ACL
            // 2. user has TAKE course permissions (also have READ to see the course)
            //   2.1. changing to view course permissions - remove document rights(*), keep Read ACL
            //   2.2. changing to edit course permissions - remove document rights(*), replace Read ACL with Write ACL
            //   2.3. removing all permissions - remove document rights(*)
            // 3. user has VIEW/READ course permissions
            //   3.1. changing to take course permissions - add to document rights(*), remove Read ACL 
            //   3.2. changing to edit course permissions - replace Read ACL with Write ACL
            //   3.3. removing all permissions - remove Read ACL
            // 4. user has EDIT/WRITE course permissions
            //   4.1. changing to take course permissions - add to document rights(*), remove Write ACL
            //   4.2. changing to view course permissions - replace Write ACL with Read ACL
            //   4.3. removing all permissions - remove Write ACL
            
            OperationRequest request = session.newRequest(SET_ACE_COMMAND).setInput(courseFolderDoc);
            for(String username : usernameToPermissions.keySet()) {
                try {
                    DomainOptionPermissions permission = usernameToPermissions.get(username);
                    SharedCoursePermissionsEnum permissionEnum = permission.getPermission();

                    if(permissionEnum != null){
                        // the user is still being given some level of permissions in this method call (i.e. not being removed)
    
                        // 1. set access control list information (read/write)
                        //    Write is the only elevated permissions that Nuxeo ACL handles, every other GIFT permission enum (e.g. Take) goes to Read.
                        String permissionStr = SharedCoursePermissionsEnum.EDIT_COURSE.equals(permissionEnum) ? PermissionsEntityType.PERMISSION_WRITE
                                : PermissionsEntityType.PERMISSION_READ;
    
                        request.set(ACE_USER_ARG, username).set(ACE_PERMISSION_ARG, permissionStr)
                                .set(ACE_GRANT_ARG, true).set(ACE_OVERWRITE_ARG, false);  
                                                   
                        // TODO: figure out how to only do this to do this once for performance reasons
                        request.execute();
                    }
                    
                    boolean isRemovingAllPermission = permissionEnum == null;
                    boolean hasTakePermission = SharedCoursePermissionsEnum.TAKE_COURSE.getName().equals(currRightsJSON.get(username));
                    boolean needsTakePermission = !hasTakePermission && SharedCoursePermissionsEnum.TAKE_COURSE.equals(permissionEnum);
                    
                    boolean needsTakePermissionRemoved = hasTakePermission && !SharedCoursePermissionsEnum.TAKE_COURSE.equals(permissionEnum); // case 2.1, 2.2
                    boolean needsRightsPropertyRemoved = isRemovingAllPermission; // case 2.3
                    boolean needsRightsPropertySet = needsTakePermission; // case 1.1, 3.1, 4.1
                    boolean needsRightsPropertyEdited = needsTakePermissionRemoved || needsRightsPropertyRemoved || needsRightsPropertySet;
                    
                    if(needsRightsPropertyEdited) {
                        // the document Rights property needs to be edited (e.g. TakeCourse added or removed)                    
                        
                        if(needsRightsPropertyRemoved) {
                            // removing custom permissions for this user
                            currRightsJSON.remove(username);
                        }else{
                            // preventing potential NPE warning reported by IDE, logically it won't happen because needsRightsPropertyRemoved = true
                            String permissionEnumName = permissionEnum == null ? "null" : permissionEnum.getName();
                            currRightsJSON.put(username, permissionEnumName);
                        }                        

                    }                        
                    
                    if(progressIndicator != null) {
                        progressIndicator.setPercentComplete((int) (preTaskProgress + taskComplete * progressIncrement));
                        progressIndicator.setTaskDescription("Updating course folder permissions");
                        taskComplete++;
                    }
                    
                }catch (RemoteException remoteException) {
                    throw new IOException("Unable to set workspace permission for " + username + " on document '" + courseFolderDoc.getPath()
                    + "' because " + remoteException.getMessage() + ".", remoteException);
                }

            } // end for
            
            if(courseDocFull != null) {
                
                DocumentEntityType docEntityType = new DocumentEntityType(courseDocFull);
                if(ds == null){
                    ds = session.getAdapter(DocumentService.class);
                }
                
                DocRef docRef = new DocRef(docEntityType.getUid());
                ds.setProperty(docRef, DC_RIGHTS_PROP, currRightsJSON.toJSONString());
            }
            
            if(progressIndicator != null) {
                progressIndicator.setPercentComplete(100);
                progressIndicator.setTaskDescription("Updated course folder permissions");
            }
        } catch (RemoteException remoteException) {
            throw new IOException("Unable to set workspace permission on document '" + courseFolderDoc.getPath()
                    + "' because " + remoteException.getMessage() + ".", remoteException);
        }
    }
    
    /**
     * Return the Nuxeo root folder for this user.  The root folder contains the Public workspace as
     * well as the user's personal workspace.
     * 
     * @param username used for authentication.  Can't be null.
     * @return the automation document (folder) that is the root folder for this user.
     * @throws IOException if there was a problem getting the client session or the document doesn't exist.
     */
    public Document getUserRootFolder(String username) throws IOException{
        
        if(username == null){
            throw new IllegalArgumentException("The username can't be null.");
        }
        
        //Nuxeo is case sensitive, so make all GIFT user names lowercase
        username = toSafeUsername(username);
        
        Session session = getSession(username);
        
        Document root = (Document) session.newRequest(DocumentService.FetchDocument).set("value", DEFAULT_WORKSPACE_ROOT).execute();
        return root;
    }
    
    /**
     * Get the private folder for the user
     * @param username User for which to retrieve the private folder
     * @return Document representing the user's private folder
     * @throws IOException Thrown if a server error occurs
     */
    public Document getUserPrivateFolder(String username) throws IOException{
        
        //Nuxeo is case sensitive, so make all GIFT user names lowercase
        username = toSafeUsername(username);
        
        Session session = getSession(username);
        try{
            Document root = (Document) session.newRequest(DocumentService.FetchDocument).set("value", DEFAULT_WORKSPACE_ROOT + username).execute();
            return root;
        }catch(RemoteException remoteException){
            throw new IOException("Unable to retrieve "+username+"'s private workspace folder because "+remoteException.getMessage()+".", remoteException);
        }
    }
    
    /**
     * Get the Public folder which contains all the showcase courses
     * @param username used for authentication
     * @throws IOException thrown if Nuxeo reports an error while processing
     * the request
     */
    public Document getPublicFolder(String username) throws IOException {
        //Nuxeo is case sensitive, so make all GIFT user names lowerscase
        username = toSafeUsername(username);
        
        try {
            Document userRoot = getUserRootFolder(username);
            return getFolderByName(userRoot.getPath(), PUBLIC_WORKSPACE, username);
        } catch(RemoteException remoteException) {
            throw new IOException("Unable to retrive the Public courses' folder because " + remoteException.getMessage() + ".", remoteException);
        }
    }
    
    /**
     * Get the Training Apps Lib folder which contains training applications.
     * 
     * @param username used for authentication
     * @return the training apps lib folder
     * @throws IOException thrown if Nuxeo reports an error while processing the request
     */
    public Document getTrainingAppsLibFolder(String username) throws IOException {
        // Nuxeo is case sensitive, so make all GIFT user names lowerscase
        username = toSafeUsername(username);

        try {
            Document publicFolder = getPublicFolder(username);
            return getFolderByName(publicFolder.getPath(), TRAINING_APPS_LIB_NAME, username);
        } catch (RemoteException remoteException) {
            throw new IOException(
                    "Unable to retrive the Training Apps Lib folder because " + remoteException.getMessage() + ".",
                    remoteException);
        }
    }
    
    /**
     * Return the default workspace root of Nuxeo
     * 
     * @return the root prefix of all workspaces in Nuxeo {@value #DEFAULT_WORKSPACE_ROOT}
     */
    public String getDefaultWorkspacePath(){
        return DEFAULT_WORKSPACE_ROOT;
    }
    
    /**
     * Prepends the default workspace path if necessary
     * @param path Path to check and prepend.  If the path starts with internal "Workspaces" prefix it will be removed for you (e.g. "Workspaces/Public").
     * If the path doesn't start with the {@link #DEFAULT_WORKSPACE_ROOT} path it will be added as the prefix to the provided path.  Can't be null.
     * @return New path with default workspace prepended or original path if it already starts with the path. Null will be returned if null was provided.
     */
    private String prependDefaultWorkspacePath(String path) {
        
        if(path == null){
            return null;
        }
        
        //the path to the parent folder where the new file should be created
        //needs to have the default workspace to identify it as an absolute path in Nuxeo
        // - At some point the path started having lowercase 'workspaces' for some calls (e.g. delete course), so now removing both
        if(path.startsWith(WORKSPACES) || path.startsWith(WORKSPACES.toLowerCase())){
            //remove this prefix since the DEFAULT_WORKSPACE_ROOT value will be added later (below)
            path = path.replaceFirst(Pattern.quote(WORKSPACES), Constants.EMPTY);
            path = path.replaceFirst(Pattern.quote(WORKSPACES.toLowerCase()), Constants.EMPTY);  //this is ok since the next part of the path is workspace name (e.g. Public, mhoffman) and can't be workspaces.
            
            //remove file separator now at beginning of path, otherwise your likely to get a path with two forward slashes, e.g. /default-domain/workspaces//blah/blah
            if(path.length() > 1){
                path = path.substring(1);
            }
        }
        
        /* check to see if path starts with default workspace path. Note we check for it without the
         * trailing slash since it may not end with one */
        if (path.startsWith(DEFAULT_WORKSPACE_ROOT_NOFOLDER)) {
            return path;
        } else if (path.startsWith(DEFAULT_WORKSPACE_ROOT.substring(1, DEFAULT_WORKSPACE_ROOT.length() - 1))) {
            /* Sometimes the path is from a FileTreeModel which may cut the first forward slash off
             * the path. Check if it is the default workspace without that slash. If that is the
             * case, add it back in. */
            return Constants.FORWARD_SLASH + path;
    }
    
        return DEFAULT_WORKSPACE_ROOT + path;
    }
    
    /**
     * Returns the concatenated string of the two strings provided adding a forward slash (the nuxeo file system file separator character)
     * in between.
     * 
     * @param path the prefix of the returned string
     * @param documentName the suffix of the returned string
     * @return the resulting string concatenation.  Note if the documentName already contains the path as a prefix than the
     * documentName is returned.
     */
    private String joinPathAndDocumentName(String path, String documentName) {    
        
        if(documentName.startsWith(path)){
            return documentName;
        }
        
        if (path.endsWith(Constants.FORWARD_SLASH)) {
            return path + documentName;
        } else {
            return path + Constants.FORWARD_SLASH + documentName;
        }
    }
    
    /**
     * Return the Nuxeo automation document with the given document name at the specified workspace path.
     * 
     * @param workspacePath the folder where the Nuxeo document to get is located.  This path should start after the "/default-domain/workspaces/" part of the workspace path
     * (e.g. "/default-domain/workspaces/Public" would be "Public").  Can't be null.
     * @param documentName the name of the document in the workspace path to retrieve.
     * @param username used for authentication
     * @return the automation document instance found
     * @throws IOException if there was a problem getting the client session or the document doesn't exist.
     */
    public org.nuxeo.ecm.automation.client.model.Document getDocumentByName(String workspacePath, String documentName, String username) throws IOException{
        
        if(documentName == null || documentName.isEmpty()){
            throw new IllegalArgumentException("The document name can't be null.");
        }
        
        //Nuxeo is case sensitive, so make all GIFT user names lowercase
        username = toSafeUsername(username);

        Session session = getSession(username);
        
        String fullpath = joinPathAndDocumentName(prependDefaultWorkspacePath(workspacePath), documentName);
        
        // Fetch the root of Nuxeo repository
        try{
            org.nuxeo.ecm.automation.client.model.Document document = (org.nuxeo.ecm.automation.client.model.Document) session.newRequest(DocumentService.FetchDocument).set("value", fullpath).execute();
             
            return document;
        }catch(RemoteException remoteException){
            throw new IOException("Unable to retrieve document at '"+fullpath+"' because "+remoteException.getMessage()+".", remoteException);
        }
    }
    
    /**
     * Return the Nuxeo automation document with the given document name at the specified workspace path.
     * 
     * @param workspacePath the full nuxeo workspace path to the document as well as the document name.  
     * (e.g. "/default-domain/workspaces/Public/my first course/my.course.xml").  Can't be null.
     * @param username used for authentication
     * @return the automation document instance found
     * @throws IOException if there was a problem getting the client session or the document doesn't exist.
     */
    public org.nuxeo.ecm.automation.client.model.Document getDocumentByName(String workspacePath, String username) throws IOException{
        
        if(workspacePath == null || workspacePath.isEmpty()){
            throw new IllegalArgumentException("The workspace path can't be null.");
        }
        
        //Nuxeo is case sensitive, so make all GIFT user names lowercase
        username = toSafeUsername(username);
        
        return getDocumentByNameCaseSensitive(workspacePath, username);
    }
    
    /**
     * Return the Nuxeo automation document with the given document name at the specified workspace path.
     * 
     * @param workspacePath the full nuxeo workspace path to the document as well as the document name.  
     * (e.g. "/default-domain/workspaces/Public/my first course/my.course.xml").  Can't be null.
     * @param username used for authentication
     * @return the automation document instance found
     * @throws IOException if there was a problem getting the client session or the document doesn't exist.
     */
    private org.nuxeo.ecm.automation.client.model.Document getDocumentByNameCaseSensitive(String workspacePath, String username) throws IOException{
        
        if(workspacePath == null || workspacePath.isEmpty()){
            throw new IllegalArgumentException("The workspace path can't be null.");
        }
        
        Session session = getSession(username);
        
        String fullpath = prependDefaultWorkspacePath(workspacePath);
        
        // Fetch the root of Nuxeo repository
        try{
            org.nuxeo.ecm.automation.client.model.Document document = (org.nuxeo.ecm.automation.client.model.Document) session.newRequest(DocumentService.FetchDocument).set("value", fullpath).execute();
             
            return document;
        }catch(RemoteException remoteException){
            throw new IOException("Unable to retrieve document at '"+workspacePath+"' because "+remoteException.getMessage()+".", remoteException);
        }
    }
    
    /**
     * Return whether the document is a folder or not.
     * 
     * @param document the document to check.  If null false is returned.
     * @return true iff the document is a folder type
     */
    public boolean isFolder(org.nuxeo.ecm.automation.client.model.Document document){        
        return document != null && document.getType().equals(DocumentTypes.Folder.name());
    }
    
    /**
     * Return whether the document is a workspace or not.
     * 
     * @param document the document to check.  If null false is returned.
     * @return true iff the document is a workspace type
     */
    public boolean isWorkspace(org.nuxeo.ecm.automation.client.model.Document document){        
        return document != null && (document.getType().equals(DocumentTypes.Workspace.name()) || document.getType().equals(DocumentTypes.WorkspaceRoot.name()));
    }

    /**
     * Return the Nuxeo document with the given document name at the specified workspace path.
     * 
     * WARNING!!!! - this method has a problem with file names that have special characters.  e.g. when the URL 
     * "https://www.youtube.com/watch?v=0QNiZfSsPc0" is authored as branch point content, the metadata file named
     * "https%3Awww.youtube.comwatch%3Fv=0QNiZfSsPc0.metadata.xml" is created.  While getDocumentByName method works,
     * this method returns null.
     * 
     * @param workspacePath the folder where the Nuxeo document to get is located.  This path should start after the "/default-domain/workspaces/" part of the workspace path
     * (e.g. "/default-domain/workspaces/Public" would be "Public").  Can't be null.
     * @param documentName the name of the document in the workspace path to retrieve.
     * @param username the authority to use to complete this interaction
     * @return the Nuxeo Document found.  Will be null if the document wasn't found.
     * @throws IOException if there was a problem getting the client session
     */
    public DocumentEntityType getDocumentEntityByName(String workspacePath, String documentName, String username) throws IOException{        
        
        if(workspacePath == null){
            throw new IllegalArgumentException("The workspace folder path can't be null.");
        }
        
        //Nuxeo is case sensitive, so make all GIFT user names lowercase
        username = toSafeUsername(username);
        
        String fullpath = joinPathAndDocumentName(prependDefaultWorkspacePath(workspacePath), documentName);
        URL url = constructURL(PATH_URL, fullpath);
        
        DocumentEntityType document = null;
        HttpURLConnection connection = getConnection(url, username);  

        try {
            int response = connection.getResponseCode();
            if (response != HttpURLConnection.HTTP_NOT_FOUND) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))){
                    Gson gson = new Gson();
                    document = gson.fromJson(in, DocumentEntityType.class);
                }
            } else if (logger.isInfoEnabled()) {
                logger.info("Document not found: " + fullpath);
            }
        } finally {
            connection.disconnect();
        }
        
        if (document == null && logger.isWarnEnabled()) {
            logger.warn(
                    "The document retrieved by getDocumentEntityByName() is null. This method has a problem with file names that have special characters, so if the path '"
                            + fullpath
                            + "' contains a special character (e.g. percent sign) then this method should not be used.");
        }

        return document;
    }
    
    /**
     * This method will retrieve the Access Control List (ACL) of the specified document.
     * 
     * @param document the document to get permissions on
     * @return nuxeo ACL information 
     * e.g. {"entity-type":"acls","acl":[{"name":"inherited","ace":[{"username":"Administrator","permission":"Everything","granted":true},{"username":"members","permission":"Read","granted":true}]}]})
     */
    private String getACLJSONStringPermissions(Document document){
        
        String acl = null;
        HttpURLConnection connection = null;
        try {
            URL url = constructURL("api/v1/id/"+document.getId()+"/@acl", null);
            // Must use the adminUser to create a new user
            connection = getConnection(url, adminUser);  

            BufferedReader in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            
            acl = in.readLine();
            in.close();
            
        } catch (Exception e) {
            logger.error("Error while getting permissions of document "+document+".", e);
        }finally{
            if(connection != null){
                connection.disconnect();
            }
        }
        
        return acl;
    }
    
    /**
     * Retrieve the permissions for the document by querying Nuxeo for that information.
     * 
     * @param document has to be an existing nuxeo document, can't be null.  
     * @return that document's permissions for all users with stored permissions.
     */
    public FileProxyPermissions getPermissions(Document document){        
        PermissionsEntityType acl = getPermissionsEntity(document);
        FileProxyPermissions filePermissions = convertToFileProxyPermissions(acl);        
        return filePermissions;
    }
    
    /** 
     * Get the permissions object for a specified nuxeo document.  Note that this will return
     * only the permissions settings (acls) on a specific document.  If a user has access to a
     * document, this method may not always return a username, but can also return what nuxeo
     * groups have access to the document. This means that you cannot rely on this method alone
     * to see if a user has permissions to a document. Instead, see the {@link NuxeoInterface#hasWritePermissions} function
     * which queries both the permissions of a user, along with the permissions of a document to see
     * if the user has access to the document.
     * 
     * @param document - The Nuxeo document to get the permissions for.
     * 
     * @return PermissionsEntityType representing the permissions (acls) for the document.  Can return null if there was an error or if the document doesn't exist.
     */
    private PermissionsEntityType getPermissionsEntity(Document document) {
        PermissionsEntityType currAclPermissions = null;
        HttpURLConnection connection = null;
        try {
            URL url = constructURL("api/v1/id/"+document.getId()+"/@acl", null);
            // Must use the adminUser to create a new user
            connection = getConnection(url, adminUser);  

            BufferedReader in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            
            // Convert the json string into a permissions entity object using google's gson.
            Gson gson = new Gson();
            currAclPermissions = gson.fromJson(in, PermissionsEntityType.class);
            in.close();

            connection.disconnect();
            
            boolean retrievedRights = false;
            String currRights = null;
            for(Acl aclEntry : currAclPermissions.getAclList()){
                
                for (AceEntity ace : aclEntry.getAceList()) {
                    
                    if(PermissionsEntityType.PERMISSION_READ.equals(ace.getPermission())){
                        // found a read only permission, check if user has further customized access
                        
                        if(!retrievedRights){
                            // get current rights which is only retrieve when querying with 'X-NXDocumentProperties' header parameter
                            // Only want to do this once
                            Document courseDocFull = (Document) getSession(adminUser).newRequest(DocumentService.FetchDocument).setHeader("X-NXDocumentProperties", "*").set("value", document.getPath()).execute();
                            currRights = courseDocFull.getProperties().getString(DC_RIGHTS_PROP);
                            retrievedRights = true;
                        }
                        
                        if(StringUtils.isNotBlank(currRights)){
                            // there is most likely a json string here, possibly empty ("{}")
                            try{
                                JSONObject currRightsJSON = (JSONObject) new JSONParser().parse(currRights);
                                Object customPermission = currRightsJSON.get(ace.getUsername());
                                if(customPermission != null && customPermission instanceof String){
                                    // there are custom GIFT permissions
                                    SharedCoursePermissionsEnum customPermissionEnum = SharedCoursePermissionsEnum.valueOf((String) customPermission);
                                    if(SharedCoursePermissionsEnum.TAKE_COURSE.equals(customPermissionEnum)){
                                        // currently we only care about 'take course' custom permission, since Read/Write is already managed by Nuxeo ACL 
                                        ace.setDirtyPermission((String) customPermission);
                                    }
                                }
                            } catch (ParseException ex) {
                                logger.error("Failed to get the current document rights JSON object for '"+document.getPath()+"'.", ex);
                            }
                        }else{
                            // don't keep checking because this document has no 'rights' value
                            break;
                        }
                       
                    }
                }
            }
            
        } catch (Exception e) {
            logger.error("Error while getting permissions of document "+document+".", e);
        }finally{
            if(connection != null){
                connection.disconnect();
            }
        }
        
        return currAclPermissions;
    }
    
    /**
     * Converts {@link PermissionsEntityType} to {@link FileProxyPermissions}. This is useful if you
     * want to pass the permissions around since {@link FileProxyPermissions} is in the common
     * package.
     * 
     * @param permissionsEntityType the nuxeo permissions entity.
     * @return the mapping of users to their granted permissions for a single file.
     */
    public FileProxyPermissions convertToFileProxyPermissions(PermissionsEntityType permissionsEntityType) {
        FileProxyPermissions fpPermissions = null;

        if (permissionsEntityType != null) {
            Map<String, SharedCoursePermissionsEnum> userPermissionMap = new HashMap<String, SharedCoursePermissionsEnum>();

            if (permissionsEntityType.getAclList() != null) {
                for (Acl acl : permissionsEntityType.getAclList()) {

                    for (AceEntity ace : acl.getAceList()) {

                        if (ace.isGranted() && ace.getPermission() != null && !ace.getPermission().trim().isEmpty()) {

                            SharedCoursePermissionsEnum permEnum;
                            if (PermissionsEntityType.PERMISSION_EVERYTHING.equalsIgnoreCase(ace.getPermission())
                                    || PermissionsEntityType.PERMISSION_WRITE.equalsIgnoreCase(ace.getPermission())) {
                                permEnum = SharedCoursePermissionsEnum.EDIT_COURSE;
                            } else if (PermissionsEntityType.PERMISSION_READ.equalsIgnoreCase(ace.getPermission())) {
                                permEnum = SharedCoursePermissionsEnum.VIEW_COURSE;
                            } else if (SharedCoursePermissionsEnum.TAKE_COURSE.getName().equalsIgnoreCase(ace.getPermission())){
                                permEnum = SharedCoursePermissionsEnum.TAKE_COURSE;
                            } else {
                                // not one of the expected permissions, skip it.
                                continue;
                            }
                            
                            String username = ace.getUsername();
                            if(EVERYONE_GROUP.equals(username)){
                                // this permission is for everyone, not a specific username
                                username = DomainOptionPermissions.ALL_USERS;
                            }

                            //check for multiple entries of the same username and make sure to return the entry with the highest permission
                            // e.g. if read and readwrite, return readwrite
                            SharedCoursePermissionsEnum currPermEnum = userPermissionMap.get(username);
                            if(currPermEnum != null && SharedCoursePermissionsEnum.EDIT_COURSE.equals(currPermEnum)){
                                //currently no permission higher than this
                                continue;
                            }
                            
                            userPermissionMap.put(username, permEnum);

                        }
                    }
                }
            }
            
            if (!userPermissionMap.isEmpty()) {
                fpPermissions = new FileProxyPermissions(userPermissionMap);
            }
        }

        return fpPermissions;
    }
    
    /**
     * Checks if the username is contained in the prohibited user list, i.e. should not be allowed to login,
     * create workspace, etc.
     * 
     * @param username the username to check.  This will ignore case.
     * @return true if the name is prohibited; false otherwise.
     */
    @SuppressWarnings("unused")
    private boolean isProhibitedUser(String username) {        
        return CommonProperties.getProhibitedNames().contains(toSafeUsername(username));
    }
    
    /**
     * Determine if the user has permissions to a document.  This function is intended to be an internal
     * function that is called externally by {@link NuxeoInterface#hasWritePermissions}  or {@link NuxeoInterface#hasReadPermissions}
     * 
     * This function can take a list of permissions to check (eg. "Everything", "Write") and see if the user has
     * met one of those permissions on the document.   Once we find at least one permission has matched, then the function
     * returns true.  If no permissions are matched, then false is returned.
     * 
     * Unfortunately, at this time, we don't find a simple query to do this in Nuxeo.  So what we are doing first is 
     * to leverage the ACL interface from Nuxeo as well as the User.  We make a query to get the ACL list of the document
     * first, followed by doing a getUser() query that will get the groups that the user belongs to in Nuxeo.  We compare
     * to see if the user is contained in the ACL directly from Nuxeo for a given permission and then secondly we see if the
     * user belongs to any of the groups that are in the ACL for any granted permissions.  
     * 
     * The old method was to lock and unlock the document, however it spams the nuxeo logs with security exceptions if the user
     * doesn't have permissions to lock the document.  Which is why we removed the older logic.
     * 
     * @param document the document to check for write permissions for the user
     * @param username the user to check permissions for
     * @param permissionList list of the permissions to check to see if the user has on the document.
     * @return true if the user has met at least ONE of the permissions on the document.  false otherwise.
     */
    private boolean hasPermissions(Document document, String username, List<String> permissionList){
        
        //Nuxeo is case sensitive, so make all GIFT user names lowercase
        username = toSafeUsername(username);

        boolean hasPermissions = false;

        UserEntityType userEntity = getUser(username);
        
        if (userEntity != null) {
            // User has write permissions if:
            // 1) Examine the acelist of the document.
            PermissionsEntityType permissions = getPermissionsEntity(document);
            
            if (permissions != null) {
                // 2) For all permissions where "granted = true" 
                //      If the ace username matches the user name then the user has write permissions.
                //      If the username doesn't match, then check to see if it's a group name and if the
                //      user belongs to the same group name.
                for(Acl acl : permissions.getAclList()) {
                    
                    for (AceEntity aceEntity : acl.getAceList()) {
                        
                        // This checks to see if the permission is granted.  If it is, then check to make sure
                        // that the permission type is in the list of permissions we want to check (out of the 4 permissions)
                        // and if it is in the list then check to see if the acl has that permission type.
                        if (aceEntity.isGranted() && 
                                (permissionList.contains(PermissionsEntityType.PERMISSION_EVERYTHING) && 
                                        aceEntity.getPermission().equals(PermissionsEntityType.PERMISSION_EVERYTHING)) ||
                                (permissionList.contains(PermissionsEntityType.PERMISSION_WRITE) && 
                                        aceEntity.getPermission().equals(PermissionsEntityType.PERMISSION_WRITE)) || 
                                (permissionList.contains(PermissionsEntityType.PERMISSION_READ) && 
                                        aceEntity.getPermission().equals(PermissionsEntityType.PERMISSION_READ)) || 
                                (permissionList.contains(PermissionsEntityType.PERMISSION_REMOVE) && 
                                        aceEntity.getPermission().equals(PermissionsEntityType.PERMISSION_REMOVE))) {
                            
                            // If the username of the user matches the ace username directly, then the user has permissions.
                            if (userEntity.getProperties().getUsername().equals(aceEntity.getUsername())){
                                hasPermissions = true;
                                break;
                            }
                            
                            // If the username, doesn't match, then check to see if the user belongs to a group of the same name.
                            for (String groupName : userEntity.getProperties().getGroups()) {
                                
                                // Note the aceEntity 'username' can actually be a user, or the name of a group, so we are checking
                                // the groupnames here.
                                if (aceEntity.getUsername().equals(groupName)) {
                                    hasPermissions = true;
                                    break;
                                }
                            }
                        }
                        
                        // Stop processing once we found the user has permissions.
                        if (hasPermissions) { 
                            break;
                        }
                        
                    }
                    
                    // Stop processing once we found the user has permissions.
                    if (hasPermissions) {
                        break;
                    }
                }
                
            } else {
                logger.error("Unable to get permissions for document: " + document);
            }
        } else {
            logger.error("Unable to get user entity for user: " + username);
        }
        
        return hasPermissions;
    }
    
    /**
     * Determine if a user has write permissiosn for a document in Nuxeo.  
     * 
     * @param document the document to check for write permissions for the user
     * @param username the user to check write permissions for
     * @return true if the user has write permissions to the document, false otherwise.
     */
    public boolean hasWritePermissions(Document document, String username) {
        
        //Nuxeo is case sensitive, so make all GIFT user names lowercase
        username = toSafeUsername(username);
        
        ArrayList<String> permissions = new ArrayList<String>();
        
        // User has write permissions if they have permission "Everything" or permission "ReadWrite"
        permissions.add(PermissionsEntityType.PERMISSION_EVERYTHING);
        permissions.add(PermissionsEntityType.PERMISSION_WRITE);
        boolean hasWrite = hasPermissions(document, username, permissions);
        
        if(logger.isTraceEnabled()){
            logger.trace("Checking write permissions for document: " + document.getPath() + " and user: " + username + " write permission = " + hasWrite);
        }
        return hasWrite;
    }
    
    /**
     * Determine if a user has read permissiosn for a document in Nuxeo.  
     * 
     * @param document the document to check for read permissions for the user
     * @param username the user to check read permissions for
     * @return true if the user has read permissions to the document, false otherwise.
     */
    public boolean hasReadPermissions(Document document, String username) {
        
        //Nuxeo is case sensitive, so make all GIFT user names lowercase
        username = toSafeUsername(username);
        
        ArrayList<String> permissions = new ArrayList<String>();
        
        // User has read permissions if they have permission "Everything" or permission "Read"
        permissions.add(PermissionsEntityType.PERMISSION_EVERYTHING);
        permissions.add(PermissionsEntityType.PERMISSION_READ);
        
        boolean hasRead = hasPermissions(document, username, permissions);
        if(logger.isTraceEnabled()){
            logger.trace("Checking read permissions for document: " + document.getPath() + " and user: " + username + " read permission = " + hasRead);
        }
        return hasRead;
    }
    
    /**
     * Lock the document with the lock being held by the supplied user.  If the 
     * lock is already being held by the user it will return true.  If another
     * user owns the lock then an exception will be thrown.  Note that it is
     * the caller's responsibility to have an up to date document object retrieved
     * from the server.
     * @param username User placing lock on document
     * @param doc Document on which to place lock
     * @return True if the document lock request is successful
     * @throws DocumentLockedException Thrown if another user already owns the lock on the document
     * @throws IOException Exception from server call
     */
    public boolean lockDocument(String username, org.nuxeo.ecm.automation.client.model.Document doc) throws DocumentLockedException, IOException {
        
        //Nuxeo is case sensitive, so make all GIFT user names lowercase
        username = toSafeUsername(username);
        
        DocumentService ds = getSession(username).getAdapter(DocumentService.class);
        boolean locked = false;
        if (doc.isLocked()) {
            if (username.equals(doc.getLockOwner())) {
                locked = true;
            } else {
                throw new DocumentLockedException("Document is already locked by user: " + doc.getLockOwner());
            }
        } else {
            try {
                Document document = ds.lock(doc);
                locked = document.isLocked();
            } catch (RemoteException e) {
                // Check to see if the RemoteException is from a document already
                // being locked. This would only occur if the document object
                // passed into the method is stale/out of date
                Throwable cause = e.getCause();
                if (cause == null) {
                    throw e;
                }
                
                // Error is inside the message string of the caused by throwable.
                // String parsing is not optimal, but allows us to get the user 
                // that locked the document
                String message = cause.getMessage();
                if(message != null){
                    String matchString = "Document already locked by ";
                    int startIndex = message != null ? message.indexOf(matchString) : -1;
                    if (startIndex != -1) {
                        startIndex += matchString.length(); // adjust start index
                        int endIndex = message.indexOf(":", startIndex); // user ends with a :
                        String user = message.substring(startIndex, endIndex);
                        throw new DocumentLockedException("Document is already locked by user: " + user);
                    } 
                    
                    // again, determine if the error is a write permissions issue
                    matchString = "Privilege 'WriteProperties' is not granted to";
                    startIndex = message != null ? message.indexOf(matchString) : -1;
                    if (startIndex != -1) {
                        throw new WritePermissionException("'"+username+"' doesn't have write access to document.");
                    }
                }
                
                // some other type of error occurred so rethrow
                throw e;

            }
        }
        return locked;
    }
    
    /**
     * Unlock the document
     * @param username User authorized to unlock the document
     * @param doc Document from which to remove the lock
     * @return True if the document unlock request is successful
     * @throws IOException May be thrown if a server error occurs
     */
    public boolean unlockDocument(String username, org.nuxeo.ecm.automation.client.model.Document doc) throws IOException {
        
        //Nuxeo is case sensitive, so make all GIFT user names lowercase
        username = toSafeUsername(username);
        
        DocumentService ds = getSession(username).getAdapter(DocumentService.class);
        Document document = ds.unlock(doc);
        return !document.isLocked();
    }
    
    /**
     * Return the Nuxeo folder with the given folder name at the specified workspace path.
     * 
     * @param workspacePath the folder where the Nuxeo folder to get is located.  This path should start after the "/default-domain/workspaces/" part of the workspace path
     * (e.g. "/default-domain/workspaces/Public" would be "Public").  Can't be null.
     * @param folderName the name of the folder in the workspace path to retrieve.
     * @param username the authority to use to complete this interaction
     * @return the Nuxeo Document found.  Will be null if the folder wasn't found.
     * @throws IOException if there was a problem getting the client session
     */
    public DocumentEntityType getFolderEntityByName(String workspacePath, String folderName, String username) throws IOException{
        
        //Nuxeo is case sensitive, so make all GIFT user names lowercase
        username = toSafeUsername(username);
        
        return getDocumentEntityByName(workspacePath, folderName, username);
    }
    
    /**
     * Return the Nuxeo automation folder with the given folder name at the specified workspace path.
     * 
     * @param workspacePath the folder where the Nuxeo folder to get is located.  This path should start after the "/default-domain/workspaces/" part of the workspace path
     * (e.g. "/default-domain/workspaces/Public" would be "Public").  Can't be null.
     * @param folderName the name of the folder in the workspace path to retrieve.
     * @param username used for authentication
     * @return the automation document instance found
     * @throws IOException if there was a problem getting the client session or the folder doesn't exist.
     */
    public org.nuxeo.ecm.automation.client.model.Document getFolderByName(String workspacePath, String folderName, String username) throws IOException{
        
        //Nuxeo is case sensitive, so make all GIFT user names lowercase
        username = toSafeUsername(username);
        
        return getDocumentByName(workspacePath, folderName, username);
    }
    
    /**
     * Return the folder size in MB.
     * 
     * @param workspacePath Path of the folder
     * @param username User who has access to read the folder data
     * @return folder size in MB
     * @throws IOException Thrown if a server error occurs
     */
    public float getFolderSizeByPath(String workspacePath, String username) throws IOException {
        
        //Nuxeo is case sensitive, so make all GIFT user names lowercase
        username = toSafeUsername(username);
        
        Document folder = getFolderByName(DEFAULT_WORKSPACE_ROOT, workspacePath, username);
        if(logger.isDebugEnabled()){
            logger.debug("Folder ref: {}", folder.getId());
        }
        return getFolderSizeById(folder.getId(), username);
    }
    
    /**
     * Calls the Nuxeo Quota automation URL to get quota info for the folder
     * @param folderId ID of the folder
     * @param username User with authority to read 
     * @return QuotaInfo object representing server result
     * @throws IOException 
     */
    private QuotaInfo getQuotaInfo(String folderId, String username) throws IOException {
        
        //Nuxeo is case sensitive, so make all GIFT user names lowercase
        username = toSafeUsername(username);
        
        String requestData = String.format("{\"params\" : { \"documentRef\" : \"%s\" } }", folderId);
        String result = executeQuotaRequest(QUOTA_INFO_URL, requestData, username);
        
        QuotaInfo quotaInfo = new QuotaInfo();
        // sample result:
        // {"entity-type":"org.nuxeo.ecm.quota.automation.SimpleQuotaInfo","value":{"innerSize":0,"totalSize":80237,"maxQuota":-1}}
        // https://github.com/nuxeo/nuxeo-quota
        JSONParser parser = new JSONParser();
        try {
            JSONObject obj = (JSONObject)parser.parse(result);
            JSONObject values = (JSONObject) obj.get("value");
            quotaInfo.innerSize = (long) values.get("innerSize");
            quotaInfo.totalSize = (long) values.get("totalSize");
            quotaInfo.maxQuota = (long) values.get("maxQuota");
        } catch (ParseException ex) {
            java.util.logging.Logger.getLogger(NuxeoInterface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            throw new IOException("Error parsing server result", ex);
        }
        return quotaInfo;
    }
    
    /**
     * Execute a Nuxeo Quota automation API request
     * @param requestUrl Quota Automation API request
     * @param requestData JSON request data
     * @param username User with authority to access server data
     * @return Server JSON result
     * @throws IOException Thrown if a server error occurs
     */
    private String executeQuotaRequest(String requestUrl, String requestData, String username) throws IOException{
        
        //Nuxeo is case sensitive, so make all GIFT user names lowercase
        username = toSafeUsername(username);
        
        return executeQuotaRequestCaseSensitive(requestUrl, requestData, username);
    }
    
    /**
     * Execute a Nuxeo Quota automation API request
     * @param requestUrl Quota Automation API request
     * @param requestData JSON request data
     * @param username User with authority to access server data
     * @return Server JSON result
     * @throws IOException Thrown if a server error occurs
     */
    private String executeQuotaRequestCaseSensitive(String requestUrl, String requestData, String username) throws IOException{
        
        URL url = constructURL(requestUrl, null);
        HttpURLConnection connection = getConnection(url, username);
        String result;
        try (PrintWriter writer = new PrintWriter(connection.getOutputStream(), true)) {
            writer.println(requestData);
            int response = connection.getResponseCode();
            if (response == HttpURLConnection.HTTP_INTERNAL_ERROR) {
                throw new IOException("Make sure the Nuxeo Quota plugin is installed");
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                result = reader.readLine();
            }
            if(logger.isDebugEnabled()){
                logger.debug("Folder size request result: {}", result);
            }
        } catch (IOException e) {
            throw e;
        } finally {
            connection.disconnect();
        }
        return result;
    }
    
    /**
     * Return the folder size in MB.
     * 
     * @param folderId ID of the folder
     * @param username User who has access to read the folder data
     * @return Folder size in MB
     * @throws IOException Thrown if a server error occurs
     */
    public float getFolderSizeById(String folderId, String username) throws IOException {
        
        //Nuxeo is case sensitive, so make all GIFT user names lowercase
        username = toSafeUsername(username);
        
        QuotaInfo quotaInfo = getQuotaInfo(folderId, username);
        return 1.0f*quotaInfo.totalSize/(1024*1024);
    }
    
    /**
     * Class representing the Nuxeo QuotaInfo JSON data
     */
    static class QuotaInfo {
        long innerSize;
        long totalSize;
        long maxQuota;
    }
    
    /**
     * Set the quota on the provided workspace path
     * @param workspacePath Path to workspace
     * @param quotaBytes Quota in bytes, set to -1 to deactivate quota
     * @param username User with authority to set quota
     * @return True if the set operation is successful
     * @throws IOException Thrown if a server error occurs
     */
    public boolean setWorkspaceQuota(String workspacePath, long quotaBytes, String username) throws IOException {
        
        //Nuxeo is case sensitive, so make all GIFT user names lowercase
        username = toSafeUsername(username);
        
        Document workspace = getFolderByName(DEFAULT_WORKSPACE_ROOT, workspacePath, username);
        if(logger.isDebugEnabled()){
            logger.debug("Workspace ref: {}", workspace.getId());
        }
        return setWorkspaceQuotaByIdCaseSensitive(workspace.getId(), quotaBytes, username);
    }
    
    /**
     * Set the quota on the workspace with the provided ID
     * @param workspaceId ID of workspace
     * @param quotaBytes Quota in bytes, set to -1 to deactivate quota
     * @param username User with authority to set quota
     * @return True if the set operation is successful
     * @throws IOException Thrown if a server error occurs
     */
    private boolean setWorkspaceQuotaByIdCaseSensitive(String workspaceId, long quotaBytes, String username) throws IOException {

        String requestData = String.format("{\"params\" : { \"documentRef\" : \"%s\", \"targetSize\" : %d } }", workspaceId, quotaBytes);
        String result = executeQuotaRequestCaseSensitive(QUOTA_SET_SIZE_URL, requestData, username);
        boolean success;
        // sample result:
        // {"entity-type":"number","value":1000000000}
        // https://github.com/nuxeo/nuxeo-quota
        JSONParser parser = new JSONParser();
        try {
            JSONObject obj = (JSONObject)parser.parse(result);
            long value = (Long)obj.get("value");
            success = value == quotaBytes;
            
        } catch (ParseException ex) {
            java.util.logging.Logger.getLogger(NuxeoInterface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            throw new IOException("Error parsing server result", ex);
        }
        return success;
    }
    
    /**
     * Get the remaining workspace quota size
     * @param workspacePath Path to workspace
     * @param username User with authority to make request
     * @return Remaining quota size in bytes. Long.MAX_VALUE is returned for unlimited
     * @throws IOException Thrown if server error occurs
     */
    public long getRemainingWorkspaceQuota(String workspacePath, String username) throws IOException {
        
        //Nuxeo is case sensitive, so make all GIFT user names lowercase
        username = toSafeUsername(username);
        
        Document workspace = getFolderByName(DEFAULT_WORKSPACE_ROOT, workspacePath, username);
        if(logger.isDebugEnabled()){
            logger.debug("Workspace ref: {}", workspace.getId());
        }
        return getRemainingWorkspaceQuotaById(workspace.getId(), username);
    }
    
    /**
     * Get the remaining workspace quota size
     * @param workspaceId ID of workspace
     * @param username User with authority to make request
     * @return Remaining quota size in bytes. Long.MAX_VALUE is returned for unlimited
     * @throws IOException Thrown if server error occurs
     */
    public long getRemainingWorkspaceQuotaById(String workspaceId, String username) throws IOException {
        
        //Nuxeo is case sensitive, so make all GIFT user names lowercase
        username = toSafeUsername(username);
        
        QuotaInfo quotaInfo = getQuotaInfo(workspaceId, username);
        long remaining;
        if (quotaInfo.maxQuota == -1) {
            remaining = Long.MAX_VALUE;
        } else {
            remaining = quotaInfo.maxQuota - quotaInfo.totalSize;
        }
        return remaining;
    }
    
    /**
     * Deletes a Nuxeo Document referenced by the automation document specified.
     * 
     * @param document an automation nuxeo document to delete
     * @param username used for authentication
     * @throws IOException if there was a problem connecting/disconnecting to the Nuxeo API URL or reading from the connection.
     */
    @Deprecated
    public void deleteDocument(org.nuxeo.ecm.automation.client.model.Document document, String username) throws IOException{
        
        if(document == null){
            throw new IllegalArgumentException("The document can't be null.");
        }
        
        //Nuxeo is case sensitive, so make all GIFT user names lowercase
        username = toSafeUsername(username);
        
        Session session = getSession(username);
        
        // Delete the document
        try{
            session.newRequest(DocumentService.DeleteDocument).setInput(document).execute();
        }catch(RemoteException remoteException){
            throw new IOException("Unable to delete document at '"+document.getPath()+"' because "+remoteException.getMessage()+".", remoteException);
        }
    }
    
    /**
     * This method will import a folder (and all descendants) that is on the Nuxeo VM into the Nuxeo repository.
     * 
     * Reference: http://doc.nuxeo.com/display/ADMINDOC/Bulk+Document+Importer
     * Notes:
     * i. [from website] The importer requires a lot of memory. Make sure your maximum heap size is set as high as possible for your environment.
     * 
     * @param serverFolderPath the server side path to the folder being imported (e.g. "/home/jsmith/Desktop/ClearBuilding")
     * @param workspacePath the workspace folder to import into.  This path should start after the "/default-domain/workspaces/" part of the workspace path
     * (e.g. "/default-domain/workspaces/Public" would be "Public").  The folder must exist, this method won't create it.  Can't be null.
     * @param username the authority to use to complete this interaction
     * @throws IOException if there was a problem getting the client session
     */
    private void importServerFolder(String serverFolderPath, String workspacePath, String username) throws IOException{
    
        //Nuxeo is case sensitive, so make all GIFT user names lowercase
        username = toSafeUsername(username);
        
        URL url = constructURL(IMPORT_SERVER_FOLDER_URL, workspacePath + "&inputPath="+serverFolderPath);
        
        HttpURLConnection connection = getConnection(url, username);  

        BufferedReader in = null;
        try{
            
            try (InputStream iStream = connection.getInputStream()){
                in = new BufferedReader(new InputStreamReader(iStream));
                
                //for debugging purposes
                String line = in.readLine();
                while(line != null){
                    System.out.println(line);
                    line = in.readLine();
                }
            }
            
        }finally{
            
            if(in != null){
                in.close();
            }            

            connection.disconnect();
        }
    }
    
    /**
     * Exports the contents of a Nuxeo folder to a local zip file.  If the user
     * cancels the export through the progressIndicator, the destination file will be deleted.
     * 
     * @param documentId unique id of a nuxeo folder that will be exported.  Must be a valid id.
     * @param destinationZip the local file to export the contents of the folder too.  Can't be null.
     * @param authority the authority to use to complete this interaction
     * @param progressIndicator used to show progress to the user.  Can be null. Note: the subtask progress will not be updated here,
     * only the parent progress information will be updated. 
     * @throws IOException if there was a problem getting the client session, the folder id was invalid or
     * there was a problem writing the folder's contents to the file provided.
     */
    public void exportFolder(String documentId, File destinationZip, String authority, ProgressIndicator progressIndicator) throws IOException{
        
        if(documentId == null || documentId.isEmpty()){
            throw new IllegalArgumentException("The document id can't be null.");
        }else if(destinationZip == null){
            throw new IllegalArgumentException("The destination zip file can't be null.");
        }else if(authority == null || authority.isEmpty()){
            throw new IllegalArgumentException("The username can't be null or empty.");
        }
        
        //Nuxeo is case sensitive, so make all GIFT user names lowercase
        authority = toSafeUsername(authority);       
        
        float folderSizeMb = getFolderSizeById(documentId, authority);
        float folderSizeBytes = folderSizeMb*1024*1024;
        if (folderSizeBytes <= 0) {
            logger.warn("Reported folder size is less than or equal to zero");
        }
        // get the divisor for progress calculation, prevent divide by zero
        float folderSizeInverse = folderSizeBytes > 0 ? 1/folderSizeBytes : 0;
        
        String repository = "default";
        URL url = constructURL("restAPI/"+repository+"/", documentId + "/exportTree/binary");
        HttpURLConnection connection = getConnection(url, authority);  

        // process the inputstream and report progress
        byte buffer[] = new byte[4096];
        try (BufferedInputStream iStream = new BufferedInputStream(connection.getInputStream());
                FileOutputStream fileOutput = FileUtils.openOutputStream(destinationZip)) {
            if (progressIndicator != null) {
                progressIndicator.setPercentComplete(0);
            }
            int bytesRead;
            long totalBytesRead = 0;
            int lastPercentage = 0;
            int newPercentage;
            while ((bytesRead = iStream.read(buffer)) != -1) {
                fileOutput.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;
                if (progressIndicator != null) {
                    if (progressIndicator.shouldCancel()) {
                        if(logger.isInfoEnabled()){
                            logger.info("User canceled export");
                        }
                        break;
                    }
                    newPercentage = (int)(totalBytesRead*folderSizeInverse*100);
                    if (newPercentage > lastPercentage) {
                        // only report progress if it's changed
                        lastPercentage = newPercentage;
                        progressIndicator.setPercentComplete(newPercentage);
                    }
                }
            }
        } finally{       
            connection.disconnect();
        }
        
        if (progressIndicator != null) {
            if (progressIndicator.shouldCancel()) {
                // clean up since user cancelled
                if (destinationZip.exists()) {
                    if (!destinationZip.delete()) {
                        logger.error("Error deleting zip export");
                    }
                }
            } else {
                progressIndicator.setPercentComplete(100);
            }
        }
    }
    
    /**
     * Deletes all users in the {@link #EVERYONE_GROUP} along with their workspaces.
     */
    @SuppressWarnings("unused")
    private void deleteAllUserData(){
        
        HttpURLConnection connection = null;
        try{
            URL url = constructURL("api/v1/group/"+EVERYONE_GROUP, Constants.EMPTY);

            connection = getConnection(url, adminUser); 
            
            try(BufferedReader in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()))){

            
                Gson gson = new Gson();
                GroupEntityType group = gson.fromJson(in, GroupEntityType.class);
                
                for(String userId : group.getMemberUsers()){
                    
                    if(!userId.equals(adminUser)){
                        //just in case the Admin user name made it into the group, don't want to delete that user data
                        
                        try{
                            deleteUser(userId);
                        }catch(Exception e){
                            System.out.println("Failed to delete user for : "+userId);
                            e.printStackTrace();
                        }

                    }
                }
            }
                
        }catch(IOException e){
            throw new DetailedException("Failed to delete all user data in Nuxeo.", 
                    "There was an exception while trying to retrieve all the users in Nuxeo.", e);
                
        }finally{
            
            if(connection != null){
                connection.disconnect();
            }
        }
    }
    
//    //works
//    private void createDocument(File contentFile, String targetPath, String encodedAuthority){
//        
//        if(contentFile == null || !contentFile.exists()){
//            throw new IllegalArgumentException("The file to import can't be null or empty.");
//        }
//        
//        try {
////            String inputPath = "/var/lib/nuxeo/binaries/data/f3/cf"; //works, this is a folder on the Nuxeo VM
////            inputPath = "http://10.1.21.13:8885/Explicit%20Feedback%20within%20Game-Based%20Training"; //doesn't work
////            inputPath = "/home/admin/Desktop/stuff";
////            URL url = constructURL(FILE_IMPORT_URL + "targetPath=/default-domain/workspaces/" + targetPath + "&inputPath=" + inputPath, null);
//            URL url = constructURL(AUTOMATION_BLOB_URL, null);
//            
//            HttpURLConnection connection = getConnection(url, encodedAuthority);   
//            connection.setDoInput(true);
//            connection.setDoOutput(true);
//            connection.setUseCaches(false);
//            connection.setRequestMethod(POST);
//            connection.setRequestProperty("X-Batch-Id", "test");
//            connection.setRequestProperty("X-File-Idx", "0");
//            connection.setRequestProperty("X-File-Name", contentFile.getName());
//            
//            
//            DataOutputStream dos = new DataOutputStream(connection.getOutputStream());
//            
//            //Note: The files attached to the batch are stored on a temporary disk storage (inside java.io.tmp) until the batch is executed or dropped.
//            FileInputStream fileInputStream = new FileInputStream(contentFile);
//            
//            // create a buffer of maximum size            
//            int maxBufferSize = 1 * 1024 * 1024;
//            int bytesAvailable = fileInputStream.available();
//            int bufferSize = Math.min(bytesAvailable, maxBufferSize);
//            byte[] buffer = new byte[bufferSize];
//
//            // read file and write it into form...
//            int bytesRead = fileInputStream.read(buffer, 0, bufferSize);
//
//            while (bytesRead > 0) {
//                dos.write(buffer, 0, bufferSize);
//                bytesAvailable = fileInputStream.available();
//                bufferSize = Math.min(bytesAvailable, maxBufferSize);
//                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
//            }
//
//            
//            fileInputStream.close();
//            dos.flush();
//            dos.close();
//
//            BufferedReader in = new BufferedReader(new InputStreamReader(
//                    connection.getInputStream()));
//            
//            String line = in.readLine();
//            while(line != null){
//                System.out.println(line);
//                line = in.readLine();
//            }
//
//            in.close();
//            
//            //////////////////////////
//            
//            //create new URL
//            url = constructURL("api/v1/path/default-domain/workspaces/"+targetPath, null);
////            url = constructURL("api/v1/id/aee4cd87-88d0-44b2-8282-cc439ef67fbf", null);
//            connection = getConnection(url, encodedAuthority); 
//            
//            Document document = new Document();
//            document.setName("myNewDoc");
//            document.setType("File");
////            document.setUid("aee4cd87-88d0-44b2-8282-cc439ef67fbf");
////            document.setPath("/default-domain/workspaces/Public/myNewDoc.1427222284644.1427222450650");
//            Document.Properties dProperties = new Document.Properties();
//            dProperties.setTitle("my new document");
////            dProperties.setDescription("my description");
////            dProperties.setIcon("/icons/file.gif");
//            Document.Properties.FileProperties fileProps = new Document.Properties.FileProperties();
//            fileProps.setUploadBatch("test");
//            fileProps.setUploadFileId("0");
//            dProperties.setFileContent(fileProps);
//            document.setProperties(dProperties);
//            
//            Gson gson = new Gson();
//            String json = gson.toJson(document);
//            
//            OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
//            out.write(json);
//            out.close();
//            
//            BufferedReader in2 = new BufferedReader(new InputStreamReader(
//                    connection.getInputStream()));
//            
//            String line2 = in2.readLine();
//            while(line2 != null){
//                System.out.println(line2);
//                line2 = in2.readLine();
//            }
//
//            in2.close();
//            
//            connection.disconnect();
//        } catch (Exception e) {
//            System.err.println("\nError while uploading file '"+fileToImport+"' to '"+targetPath+"'.");
//            e.printStackTrace();
//        }
//    }
    
    
    private static final String filenameToUpload = "../Domain/README.txt";
    
    /**
     * Used to test the interface logic by performing calls to the Nuxeo API.
     * 
     * @param args not used
     */
    public static void main(String[] args){
        
        /////////////////////////////////////////////////
        //cause log4j to redirect to console
        ConsoleAppender console = new ConsoleAppender(); //create appender
        //configure the appender
        String PATTERN = "%d{ABSOLUTE} %5p %c{1}:%L - %m%n";
        console.setLayout(new PatternLayout(PATTERN)); 
        console.setThreshold(Level.INFO);
        console.activateOptions();
        //add appender to any Logger (here is root)
        org.apache.log4j.Logger logger = org.apache.log4j.Logger.getRootLogger();
        logger.addAppender(console);
        //////////////////////////////////////////////////
        
        // Init variables
        String GIFT_NUXEO_SERVER_URL = "http://10.0.21.204:8080/nuxeo/";
        String GIFT_NUXEO_SECRET_KEY = "yourSecretKeyHere";
        
        String adminUser = ADMINISTRATOR_USERNAME;
        String nonAdminUser = "bill";
        String group_test = "test";
        String targetWorkspace = "Public";
        
        try {
            NuxeoInterface nInterface = new NuxeoInterface(GIFT_NUXEO_SERVER_URL, GIFT_NUXEO_SECRET_KEY);   
            nInterface.setAdminUsername(adminUser);
            GroupEntityType groupToCreate = new GroupEntityType(group_test, group_test);
            
            /////////////////////// Testing area (often not working) ////////////////////////////////// 
//            
//            String testURLStr;
//            testURLStr = "api/v1/query?query=SELECT * FROM Document WHERE content/name LIKE '%.txt'";
//            nInterface.generalURLTest(testURLStr, encodedAuth, Documents.class);
            ///////////////////////////////////////////////////////////////////////////////////////////
               
            String serverFolderPathToUpload = "/work/Courses/Explicit Feedback within Game-Based Training";
            String serverFolderToUpload = serverFolderPathToUpload.substring(serverFolderPathToUpload.lastIndexOf("/")+1, 
                    serverFolderPathToUpload.length());
            
            try {
                // TEST SERVER SIDE BULK IMPORTER
                // NOTE THAT THE UPLOAD FOLDER MUST EXIST IN THE PATH ON THE SERVER
                // AND THAT A "Public" WORKSPACE MUST BE CREATED FOR THE USER
                
                System.out.println("Importing server side folder of "+serverFolderPathToUpload+" to workspace folder of "+targetWorkspace+".");
                nInterface.importServerFolder(serverFolderPathToUpload, targetWorkspace, adminUser); 
                
                //retrieve folder that was just uploaded
                DocumentEntityType previouslyImportedFolder = nInterface.getFolderEntityByName(targetWorkspace, serverFolderToUpload, adminUser);
                if (previouslyImportedFolder != null) {
                    System.out.println("Exporting Nuxeo folder w/ id "+previouslyImportedFolder.getUid());
                    File exportedFolderZip = new File("nuxeoFolder.Export.TEST.zip");
                    nInterface.exportFolder(previouslyImportedFolder.getUid(), exportedFolderZip, adminUser, null);
                } else {
                    System.err.println("ERROR: Could not find previously imported folder: " + serverFolderToUpload);
                }
                
            }catch(IOException e){
                System.err.println("Caught exception while trying to import server side folder.\nIs the 'Bulk Document Importer' installed in this Nuxeo instance (check the Admin panel in the Nuxeo UI).");
                e.printStackTrace();
            }
            
            System.out.println("Getting course files.");
            String courseFileExtension = ".txt";
            DocumentsEntityType documents = nInterface.getDocumentsByFileExtension(courseFileExtension, adminUser);
            if(documents != null && documents.getEntries() != null && !documents.getEntries().isEmpty()){
                System.out.println("Found "+ documents.getEntries().size()+" course files.");
                    
                DocumentEntityType documentToWrite = documents.getEntries().get(0);
                System.out.println("Writing first course file found to temp folder - "+documentToWrite);
                
                URL fileURL = nInterface.getDocumentURL(documentToWrite);
                File tempFile = File.createTempFile(documentToWrite.getTitle(), Long.toString(System.nanoTime()), FileUtil.getGIFTTempDirectory());                
                
                nInterface.writeURLToFile(fileURL, tempFile, adminUser);
                tempFile.deleteOnExit();
                System.out.println("Wrote file from URL "+fileURL+" to temp file '"+tempFile+"'.");
                
                System.out.println("Updating that same Document's file with '"+filenameToUpload+"'.");
                File fileToUpload = new File(filenameToUpload);
                nInterface.updateDocumentFile(documentToWrite, documentToWrite, new FileInputStream(fileToUpload), adminUser);

            }else{
                System.err.println("FAILED to find course files by extension.");
            }            
            
            
            System.out.println("Creating group");
            if(nInterface.createGroup(groupToCreate, adminUser)){
                System.out.println("Created group of "+groupToCreate);
            }else{
                System.err.println("FAILED to create group of "+groupToCreate);
            }
            
            System.out.println("Getting group");
            GroupEntityType groupRetrieved = nInterface.getGroup(group_test, adminUser); 
            System.out.println("\nRetrieved group: "+groupRetrieved);
            
            // non-admin user tests
            UserEntityType.Properties properties = new UserEntityType.Properties("myfirstname", "mylastname", nonAdminUser, "myemail", "mycompany", "mypassword");
            UserEntityType userToCreate = new UserEntityType(properties);
            String testWorkspace = userToCreate.getProperties().getUsername();
            
            // create user
            System.out.println("Creating new user of "+userToCreate+" in " + EVERYONE_GROUP + " group as well as a new workspace named after the username in " + testWorkspace);
            try { 
                nInterface.createNewUser(userToCreate);
                System.out.println("Created user of "+userToCreate + " in group " + EVERYONE_GROUP);
            }catch (Exception e) {
                System.err.println("FAILED to create user of "+userToCreate + ": " + e);
            }

            // get user
            System.out.println("Getting user");
            UserEntityType userRetrieved = nInterface.getUser(nonAdminUser);
            System.out.println("\nRetrieved user: "+userRetrieved);
            
            performFileTests(nInterface, nonAdminUser, testWorkspace);
            
            // delete user
            System.out.println("Deleting user named "+nonAdminUser);
            if(nInterface.deleteUser(nonAdminUser)){
                System.out.println("\nDeleted user with username "+adminUser);
            }else{
                System.err.println("\nFAILED to delete user with username "+adminUser);
            }
            
            // delete group
            System.out.println("Deleting group named "+group_test);
            if(nInterface.deleteGroup(group_test, adminUser)){
                System.out.println("\nDeleted group with groupname "+group_test);
            }else{
                System.err.println("\nFAILED to delete group with groupname "+group_test);
            }
            
            System.out.println("Deleting previously imported-from-server folder of "+targetWorkspace+"/"+serverFolderToUpload);
            DocumentEntityType serverFolderDocument = nInterface.getDocumentEntityByName(targetWorkspace, serverFolderToUpload, adminUser);
            if(serverFolderDocument != null){
                nInterface.deleteFolder(serverFolderDocument.getUid(), null, adminUser);
            }else{
                System.err.println("FAILED to delete workspace folder of "+targetWorkspace+serverFolderToUpload+".");
            }
            
            // Uncomment to test permissions
            // performPermissionTests(nInterface, nonAdminUser);
            
//            //working on documents...
//            DirectoryEntries entries = nInterface.getDirectoryEntries(directory_Public, encodedAuth);
//            System.out.println(entries);
            
            System.out.println("\nFINISHED");

        }catch(Exception e){
            e.printStackTrace();
        }
        
        System.out.println("Good-bye");
    }
    
    // Used as a test function to make permissions tests. 
    @SuppressWarnings("unused")
    private static void performPermissionTests(NuxeoInterface nInterface, String username) {
        try {
            System.out.println("\nStarting permissions tests.");
            // Change these as needed for testing.  Currently testing one document in the public workspace
            // and one document that exists in the user workspace.
            String publicDoc = "Public/templates/configurations/BioHarness.sensorconfig.xml";
            String userDocPath = username + "/COIN AutoTutor/test.course.xml";
            
            Document doc = nInterface.getDocumentByName(publicDoc, username);
            if (doc != null) {
                System.out.println("Public Document found " + doc.getPath());
                
                String permissions = nInterface.getACLJSONStringPermissions(doc);
                System.out.println("Permissions = " + permissions);
                
            } else {
                System.out.println("Cannot find file: " + publicDoc);
                return;
            }
            
            Document userDoc = nInterface.getDocumentByName(userDocPath, username);
            if (userDoc != null) {
                System.out.println("User Document found " + userDoc.getPath());
                
                String permissions = nInterface.getACLJSONStringPermissions(userDoc);
                System.out.println("Permissions = " + permissions);
                
            } else {
                System.out.println("Cannot find file: " + userDoc);
                return;
            }            
            
            
            UserEntityType userObj = nInterface.getUser(username);
            if (userObj != null) {
                System.out.println("User details = " + userObj);
  
            } else {
                System.out.println("User entity is null " + username);
            }
            
            
           PermissionsEntityType aclsObj = nInterface.getPermissionsEntity(userDoc); 
           if (aclsObj != null) {
               System.out.println("Permissions for doc: " + userDoc.getPath() + ", permissions =" + aclsObj);
           } else {
               System.out.println("Acls entity is null " + userDoc.getPath());
           }
           
           aclsObj = nInterface.getPermissionsEntity(doc); 
           if (aclsObj != null) {
               System.out.println("Permissions for doc: " + doc.getPath() + ", permissions =" + aclsObj);
           } else {
               System.out.println("Acls entity is null " + doc.getPath());
           }
           
           System.out.println("\nCHECKING PERMISSIONS");
           
           if (nInterface.hasWritePermissions(doc, username)) {
               System.out.println("User has write permissions for document: " + doc.getPath());
           } else {
               System.out.println("User does NOT have write permissions for document: " + doc.getPath());
           }
           
           if (nInterface.hasWritePermissions(userDoc, username)) {
               System.out.println("User has write permissions for document: " + userDoc.getPath());
           } else {
               System.out.println("User does NOT have write permissions for document: " + userDoc.getPath());
           }
           
           if (nInterface.hasReadPermissions(doc, username)) {
               System.out.println("User has read permissions for document: " + doc.getPath());
           } else {
               System.out.println("User does NOT have read permissions for document: " + doc.getPath());
           }
           
           if (nInterface.hasReadPermissions(userDoc, username)) {
               System.out.println("User has read permissions for document: " + userDoc.getPath());
           } else {
               System.out.println("User does NOT have read permissions for document: " + userDoc.getPath());
           }
           
           
           
        }catch(Exception e){
            e.printStackTrace();
        }
        
        System.out.println("\nCompleted Permissions Tests.");
    }
    
    private static void performFileTests(NuxeoInterface nInterface, String username, String testWorkspace) {
        try {
            // create folder
            String folderToCreate = "test-created-folder";
            System.out.println("Creating new folder at workspaces/"+testWorkspace+" with name "+folderToCreate);
            org.nuxeo.ecm.automation.client.model.Document createdFolder = 
                    nInterface.createWorkspaceFolder(testWorkspace, folderToCreate, username, false);
            
            // retrieve folder
            try {
                System.out.println("Retrieving new folder at workspaces/" + testWorkspace + "/" + folderToCreate);
                nInterface.getFolderByName(testWorkspace, folderToCreate, username);
            } catch (Exception e) {
            	System.err.println("FAILED to retrieve folder at " + createdFolder.getPath() + ": " + e);
            }
            
            // upload file
            File fileToUpload = new File(filenameToUpload);
            String newFolderPath = testWorkspace + "/" + folderToCreate;
            System.out.println("Creating new document at " + newFolderPath +" with content of "+fileToUpload);
            org.nuxeo.ecm.automation.client.model.Document createdDocument = 
                    nInterface.createDocument(newFolderPath, fileToUpload.getName(), newFolderPath, new FileInputStream(fileToUpload), username);
            
            // document retrieval
            System.out.println("Retrieving that newly created document by name using automation library.");
            @SuppressWarnings("unused")
            org.nuxeo.ecm.automation.client.model.Document retrievedDocument = nInterface.getDocumentByName(newFolderPath, createdDocument.getTitle(), username);
            
            System.out.println("Retrieving that newly created document by name using REST API.");
            @SuppressWarnings("unused")
            DocumentEntityType retrievedDocumentEntityType = nInterface.getDocumentEntityByName(newFolderPath, createdDocument.getTitle(), username);            
            
            System.out.println("Test retrieving document by name that doesn't exist using REST API.");
            DocumentEntityType retrievedBadDocumentEntityType = nInterface.getDocumentEntityByName(newFolderPath, createdDocument.getTitle() + "-garbage", username); 
            if(retrievedBadDocumentEntityType == null){
                System.out.println("Successfully determined bad document name doesn't exist.");
            }else{
                System.out.println("FAILED to determine bad document name actually doens't exist.");
            }
            
            // delete document
            System.out.println("Deleting previously created document of "+createdDocument);
            nInterface.deleteDocument(createdDocument.getId(), null, username);
            
            // delete folder
            System.out.println("Deleting previously created folder of "+createdFolder);
            nInterface.deleteFolder(createdFolder.getId(), null, username);
        } catch (Exception e) {
            System.err.println("Error performing file tests: " + e);
            e.printStackTrace();
        }
    }
    /**
     * Provides logic to get a Nuxeo document's content.
     * 
     * @author mhoffman
     *
     */
    public static class NuxeoDocumentConnection implements ExternalFileSystemInterface{
        
        /** the address of the document */
        private URL docURL;
        
        /** used to authenticate the connection to that document */
        private String encodedAuthority;
        
        /** the connection to the document */
        private HttpURLConnection connection;
        
        private byte[] documentContent;
        
        /**
         * The constructor is very powerful.  It will make the connection to the URL, 
         * retrieve the document contents and then disconnect the connection thereby
         * cleaning up the connections resources.
         * 
         * @param docURL the address of the document
         * @param encodedAuthority used to authenticate the connection to that document
         * @param nuxeoInterface - instance of the nuxeo interface used to establish the connection to the document (must not be null).
         * @throws IOException if there was a problem connecting
         */
        public NuxeoDocumentConnection(URL docURL, String encodedAuthority, NuxeoInterface nuxeoInterface) throws IOException{
            
            if(docURL == null){
                throw new NullPointerException("The doc URL can't be null");
            }
            
            this.docURL = docURL;
            
            this.encodedAuthority = encodedAuthority;
            
            connect(nuxeoInterface);
            
            try{
                //get the document contents into memory
                getInputStream();
            }finally{
            
                disconnect();
            }
        }
        
        /**
         * Establish the connection to the document.
         * 
         * @param NuxeoInterface - The instance of the nuxeo interface used to establish the connection to the document (cannot be null).
         * @throws IOException
         */
        private void connect(NuxeoInterface nuxeoInterface) throws IOException{
            
            connection = nuxeoInterface.getConnection(docURL, encodedAuthority);  
            
            if(logger.isTraceEnabled()){
                logger.trace("Connection established: " + connection);
            }
        }
        
        /**
         * Cleanup the connection by disconnecting it.
         */
        private void disconnect(){
            if(connection != null){
                connection.disconnect();
            }
        }
        
        /**
         * Return a new input stream to read the contents of the document.<br/>
         * Note: this reads the entire file content into memory.
         * 
         * @return the document stream 
         * @throws IOException if there was a problem getting the input stream
         */
        @Override
        public InputStream getInputStream() throws IOException{
            
            if(documentContent == null){
                documentContent = IOUtils.toByteArray(connection.getInputStream());
                
                if(logger.isTraceEnabled()){
                    logger.trace("document content size: " + documentContent.length);
                }
            }            

            return new ByteArrayInputStream(documentContent);
        }

    }
    
    /**
     * The base class for NUXEO API entity types (e.g. User, Group, Document)
     * Specification: 
     * http://doc.nuxeo.com/display/NXDOC/REST+API+Entity+Types
     * 
     * @author mhoffman
     *
     */
    public static abstract class AbstractEntityType{
        
        //Nuxeo API requires this JSON key value and Java attributes can't have a dash in the name
        @com.google.gson.annotations.SerializedName("entity-type")
        protected String entityType;
        
        public AbstractEntityType(){
            
        }
        
        public AbstractEntityType(String entityType){
            this.entityType = entityType;
        }
    }
    
    /**
     * Contains information about a list of Nuxeo Documents.
     * Attributes are from the Nuxeo Document entity type.
     * Specification: 
     * http://doc.nuxeo.com/display/NXDOC/REST+API+Entity+Types
     * 
     * @author mhoffman
     *
     */
    public static class DocumentsEntityType extends AbstractEntriesEntityType{
        
        private static final String DEFAULT_ENTITY_TYPE = "documents";
        
        private List<DocumentEntityType> entries;
        
        public DocumentsEntityType(){
            super(DEFAULT_ENTITY_TYPE);
            
        }
        
        public List<DocumentEntityType> getEntries() {
            return entries;
        }

        public void setEntries(List<DocumentEntityType> entries) {
            this.entries = entries;
        }
        
        @Override
        public String toString(){
            
            StringBuilder sb = new StringBuilder();
            sb.append("[DocumentsEntityType: ");
            sb.append(super.toString());
            sb.append(", entries = {\n");
            for(AbstractEntityType entity : getEntries()){
                sb.append(entity).append(",\n");
            }
          
            sb.append("}");
            sb.append("]");
            return sb.toString();
        }
    }
    
    /**
     * The base class for NUXEO API list of entity types (e.g. Documents)
     * Specification: 
     * http://doc.nuxeo.com/display/NXDOC/REST+API+Entity+Types
     * 
     * @author mhoffman
     *
     */
    public static abstract class AbstractEntriesEntityType extends AbstractEntityType{
        
        /** 
         * number of results returned from a query 
         * (could be more than in the collection if paged) 
         */
        private int resultsCount;
        
        /** error information corresponding to the request */
        private boolean hasError;
        private String errorMessage;
        
        public AbstractEntriesEntityType(){
            super();
        }
        
        public AbstractEntriesEntityType(String entityType){
            super(entityType);
        }

        public int getResultsCount() {
            return resultsCount;
        }

        public void setResultsCount(int resultsCount) {
            this.resultsCount = resultsCount;
        }

        public boolean isHasError() {
            return hasError;
        }

        public void setHasError(boolean hasError) {
            this.hasError = hasError;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        @Override
        public String toString() {
            
            StringBuilder builder = new StringBuilder();
            builder.append("resultsCount = ").append(getResultsCount());
            builder.append(", hasError = ").append(isHasError());
            builder.append(", errorMessage = ").append(getErrorMessage());
            return builder.toString();
        }
    }
    
    /**
     * Contains information about a Nuxeo document.
     * Attributes are from the Nuxeo Document entity type.
     * Specification: 
     * http://doc.nuxeo.com/display/NXDOC/REST+API+Entity+Types
     * 
     * @author mhoffman
     *
     */
    public static class DocumentEntityType extends AbstractEntityType{
        
        private static String DEFAULT_ENTITY_TYPE = "document";
        
        /** Nuxeo unique id of the document */
        private String uid;
        
        /** the workspace path in Nuxeo to this document */
        private String path;
        
        private String type;
        
        private boolean isCheckedOut;
        
        /** 
         * the label of this document 
         * (doesn't have to be the file name, e.g. "abc.txt" title could be "abc") 
         */
        private String title;
        
        /**
         * the last modified date known to Nuxeo
         * format:  YYYY-MM-DDThh:mm:ss:AAZ
         * example: 2014-06-06T10:23:59.76Z
         * Nuxeo source: it appears this field is encoded/decoded by nuxeo-features/nuxeo-automation/nuxeo-automation-client/src/main/java/org/nuxeo/ecm/automation/client/model/DateParser.java
         */
        private String lastModified;
        
        private Properties properties;
//        private Facets facets;
        
        /** generated by Nuxeo and may be used server-side for some concurrent modifications safety check. */
        private long changeToken;  
        
        public DocumentEntityType(){
            super(DEFAULT_ENTITY_TYPE);            
        }
        
        public DocumentEntityType(Document document){
            
            setUid(document.getId());
            
            if(document.getChangeToken() != null){
                setChangeToken(Long.valueOf(document.getChangeToken()));
            }
            
            setCheckdOut(document.isCheckedOut());
            setLastModified(document.getLastModified().toString());
            setPath(document.getPath());
            setTitle(document.getTitle());
            setType(document.getType());
            
            setProperties(new Properties(document.getProperties()));
        }
        

        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }

        /**
         * Return the path that also include the file name.
         * E.g. /default-domain/workspaces/Public/Hemorrhage Control Lesson/HemorrhageControl.course.xml
         * 
         * @return The Nuxeo path of the document
         */
        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
        
        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        /**
         * Return whether the document represented here is checked out.
         * From http://doc.nuxeo.com/display/NXDOC/Versioning:
         * "A working copy in the Checked Out state can be modified freely by users... A document ceases
         * to be Checked Out when the Check In operation is invoked... In the Checked In state, a working copy
         * is not modifiable.
         * 
         * @return whether this document is checked out, i.e. available to be changed
         */
        public boolean isCheckdOut() {
            return isCheckedOut;
        }

        public void setCheckdOut(boolean isCheckdOut) {
            this.isCheckedOut = isCheckdOut;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getLastModified() {
            return lastModified;
        }

        public void setLastModified(String lastModified) {
            this.lastModified = lastModified;
        }

        public long getChangeToken() {
            return changeToken;
        }

        public void setChangeToken(long changeToken) {
            this.changeToken = changeToken;
        }
        
        public Properties getProperties() {
            return properties;
        }

        public void setProperties(Properties properties) {
            this.properties = properties;
        }

        @Override
        public String toString(){
            
            StringBuilder sb = new StringBuilder();
            sb.append("[DocumentEntityType: ");
            sb.append("uid = ").append(getUid());
            sb.append(", path = ").append(getPath());
            sb.append(", type = ").append(getType());
            sb.append(", isCheckedOut = ").append(isCheckdOut());
            sb.append(", title = ").append(getTitle());
            sb.append(", lastModified = ").append(getLastModified());
            sb.append(", changeToken = ").append(getChangeToken());
            sb.append(", properties = ").append(getProperties());
            sb.append("]");
            return sb.toString();
        }
        
        public static class Properties{
            
            //Nuxeo API requires this JSON key value and Java attributes can't have a colon in the name
            @com.google.gson.annotations.SerializedName("dc:title")
            private String title;
            
            //Nuxeo API requires this JSON key value and Java attributes can't have a colon in the name
            @com.google.gson.annotations.SerializedName("dc:description")
            private String description;
            
            //Nuxeo API requires this JSON key value and Java attributes can't have a colon in the name
            @com.google.gson.annotations.SerializedName("common:icon")
            private String icon;
            
            //Nuxeo API requires this JSON key value and Java attributes can't have a colon in the name
            @com.google.gson.annotations.SerializedName("common:icon-expanded")
            private String iconExpanded;
            
            //Nuxeo API requires this JSON key value and Java attributes can't have a colon in the name
            @com.google.gson.annotations.SerializedName("common:size")
            private String size;
            
            //Nuxeo API requires this JSON key value and Java attributes can't have a colon in the name
            @com.google.gson.annotations.SerializedName("file:content")
            private FileProperties fileContent;
            
            public Properties(){
                
            }
            
            public Properties(PropertyMap propertyMap){
                
                setDescription(propertyMap.getString("dc:description"));
                setIcon(propertyMap.getString("common:icon"));
                setIconExpanded(propertyMap.getString("common:icon-expanded"));
                setSize(propertyMap.getString("common:size"));
                setTitle(propertyMap.getString("dc:title"));
                
                if(propertyMap.getMap("file:content") != null){
                    setFileContent(new FileProperties(propertyMap.getMap("file:content")));
                }
            }
            
            public String getTitle() {
                return title;
            }

            public void setTitle(String title) {
                this.title = title;
            }

            public FileProperties getFileContent() {
                return fileContent;
            }

            public void setFileContent(FileProperties fileContent) {
                this.fileContent = fileContent;
            }

            public String getDescription() {
                return description;
            }

            public void setDescription(String description) {
                this.description = description;
            }

            public String getIcon() {
                return icon;
            }

            public void setIcon(String icon) {
                this.icon = icon;
            }

            public String getIconExpanded() {
                return iconExpanded;
            }

            public void setIconExpanded(String iconExpanded) {
                this.iconExpanded = iconExpanded;
            }

            public String getSize() {
                return size;
            }

            public void setSize(String size) {
                this.size = size;
            }

            @Override
            public String toString() {
                StringBuilder builder = new StringBuilder();
                builder.append("Properties [title=").append(title)
                        .append(", description=").append(description)
                        .append(", icon=").append(icon)
                        .append(", iconExpanded=").append(iconExpanded)
                        .append(", size=").append(size)
                        .append(", fileContent=").append(fileContent)
                        .append("]");
                return builder.toString();
            }

            public static class FileProperties{
                
                //Nuxeo API requires this JSON key value and Java attributes can't have a dash in the name
                @com.google.gson.annotations.SerializedName("upload-batch")
                private String uploadBatch;
                
                //Nuxeo API requires this JSON key value and Java attributes can't have a dash in the name
                @com.google.gson.annotations.SerializedName("upload-fileId")
                private String uploadFileId;
                
                public FileProperties(){
                    
                }
                
                public FileProperties(PropertyMap propertyMap){
                    
                    setUploadBatch(propertyMap.getString("upload-batch"));
                    setUploadFileId(propertyMap.getString("upload-fileId"));
                }

                public String getUploadBatch() {
                    return uploadBatch;
                }

                public void setUploadBatch(String uploadBatch) {
                    this.uploadBatch = uploadBatch;
                }

                public String getUploadFileId() {
                    return uploadFileId;
                }

                public void setUploadFileId(String uploadFileId) {
                    this.uploadFileId = uploadFileId;
                }

                @Override
                public String toString() {
                    StringBuilder builder = new StringBuilder();
                    builder.append("FileProperties [uploadBatch=")
                            .append(uploadBatch).append(", uploadFileId=")
                            .append(uploadFileId).append("]");
                    return builder.toString();
                }
            }
        }
    }
    
    /**
     * Contains information about a list of Nuxeo directories.
     * Attributes are from the Nuxeo Directory Entry entity type.
     * Specification: 
     * http://doc.nuxeo.com/display/NXDOC/REST+API+Entity+Types
     * 
     * @author mhoffman
     *
     */
    public static class DirectoryEntriesEntityType extends AbstractEntityType{
        
        private static final String DEFAULT_ENTITY_TYPE = "directoryEntries";
        
        private List<DirectoryEntryEntityType> entries;
        
        public DirectoryEntriesEntityType(List<DirectoryEntryEntityType> entries){
            super(DEFAULT_ENTITY_TYPE);
            
            setEntries(entries);
        }

        public List<DirectoryEntryEntityType> getEntries() {
            return entries;
        }

        public void setEntries(List<DirectoryEntryEntityType> entries) {
            this.entries = entries;
        }
        
        @Override
        public String toString(){
            
            StringBuilder sb = new StringBuilder();
            sb.append("[DirectoryEntriesEntityType: ");
            sb.append("entries = {\n");
            for(DirectoryEntryEntityType entry : entries){
                sb.append(entry).append(",\n");
            }
            sb.append("}");
            sb.append("]");
            return sb.toString();
        }
    }
    
    /**
     * Contains information about a Nuxeo directory.
     * Attributes are from the Nuxeo Directory Entry entity type.
     * Specification: 
     * http://doc.nuxeo.com/display/NXDOC/REST+API+Entity+Types
     * 
     * @author mhoffman
     *
     */
    public static class DirectoryEntryEntityType extends AbstractEntityType{
        
        private static final String DEFAULT_ENTITY_TYPE = "directoryEntry";
        
        private String directoryName;
        
        private Properties properties;
        
        public DirectoryEntryEntityType(String directoryName, Properties properties){
            super(DEFAULT_ENTITY_TYPE);
            
            setDirectoryName(directoryName);
            setProperties(properties);
        }
        
        public String getDirectoryName() {
            return directoryName;
        }

        public void setDirectoryName(String directoryName) {
            this.directoryName = directoryName;
        }

        public Properties getProperties() {
            return properties;
        }

        public void setProperties(Properties properties) {
            this.properties = properties;
        }
        
        @Override
        public String toString(){
            
            StringBuilder sb = new StringBuilder();
            sb.append("[DirectoryEntryEntityType: ");
            sb.append("directoryName = ").append(getDirectoryName());
            sb.append(", properties = ").append(getProperties());
            sb.append("]");
            return sb.toString();
        }

        public static class Properties{
            
            private String id;
            private long obsolete;
            private long ordering;
            private String label;
            
            public Properties(String id, long obsolete, long ordering, String label){
                setId(id);
                setObsolete(obsolete);
                setOrdering(ordering);
                setLabel(label);
            }

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public long getObsolete() {
                return obsolete;
            }

            public void setObsolete(long obsolete) {
                this.obsolete = obsolete;
            }

            public long getOrdering() {
                return ordering;
            }

            public void setOrdering(long ordering) {
                this.ordering = ordering;
            }

            public String getLabel() {
                return label;
            }

            public void setLabel(String label) {
                this.label = label;
            }
            
            @Override
            public String toString(){
                
                StringBuilder sb = new StringBuilder();
                sb.append("[NuxeoInterface: ");
                sb.append("id = ").append(getId());
                sb.append(", obsolete = ").append(getObsolete());
                sb.append(", ordering = ").append(getOrdering());
                sb.append(", label = ").append(getLabel());
                sb.append("]");
                return sb.toString();
            }
        }
    }
    
    /**
     * Contains information about a Nuxeo group.
     * Attributes are from the Nuxeo Group entity type.
     * Specification: 
     * http://doc.nuxeo.com/display/NXDOC/REST+API+Entity+Types
     * 
     * @author mhoffman
     *
     */
    public static class GroupEntityType extends AbstractEntityType{
        
        private static final String DEFAULT_ENTITY_TYPE = "group";
        
        private String groupname;
        private String grouplabel;
        
        private List<String> memberUsers = new ArrayList<>();
        private List<String> memberGroups = new ArrayList<>();
        
        public GroupEntityType(String groupname, String grouplabel){
            super(DEFAULT_ENTITY_TYPE);
            setGroupname(groupname);
            setGrouplabel(grouplabel);
        }

        public String getGroupname() {
            return groupname;
        }

        public void setGroupname(String groupname) {
            this.groupname = groupname;
        }

        public String getGrouplabel() {
            return grouplabel;
        }

        public void setGrouplabel(String grouplabel) {
            this.grouplabel = grouplabel;
        }
        
        public List<String> getMemberUsers() {
            return memberUsers;
        }

        public void setMemberUsers(List<String> memberUsers) {
            this.memberUsers = memberUsers;
        }

        public List<String> getMemberGroups() {
            return memberGroups;
        }

        public void setMemberGroups(List<String> memberGroups) {
            this.memberGroups = memberGroups;
        }

        @Override
        public String toString(){
            
            StringBuilder sb = new StringBuilder();
            sb.append("[GroupEntityType: ");
            sb.append("groupName = ").append(getGroupname());
            sb.append(", groupLabel = ").append(getGrouplabel());
            sb.append(", memberUsers = {\n");
            for(String user : getMemberUsers()){
                sb.append(user).append(",\n");
            }
            sb.append("}");
            sb.append("]");
            return sb.toString();
        }
    }
    
    /**
     * Contains users.
     * 
     * @author mhoffman
     *
     */
    public static class UsersEntityType extends AbstractEntityType{
        
        private int resultsCount;
        private int pageSize;
        private int maxPageSize;
        private int currentPageSize;
        private boolean isNextPageAvailable = false;
        
        private List<UserEntityType> entries;
        
        private String errorMessage = null;
        
        public UsersEntityType(){
            
        }

        public List<UserEntityType> getUsers() {
            return entries;
        }

        public void setUsers(List<UserEntityType> entries) {
            this.entries = entries;
        }

        public boolean isNextPageAvailable() {
            return isNextPageAvailable;
        }

        public void setNextPageAvailable(boolean isNextPageAvailable) {
            this.isNextPageAvailable = isNextPageAvailable;
        }

        public int getResultsCount() {
            return resultsCount;
        }

        public void setResultsCount(int resultsCount) {
            this.resultsCount = resultsCount;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public int getPageSize() {
            return pageSize;
        }

        public void setPageSize(int pageSize) {
            this.pageSize = pageSize;
        }

        public int getMaxPageSize() {
            return maxPageSize;
        }

        public void setMaxPageSize(int maxPageSize) {
            this.maxPageSize = maxPageSize;
        }

        public int getCurrentPageSize() {
            return currentPageSize;
        }

        public void setCurrentPageSize(int currentPageSize) {
            this.currentPageSize = currentPageSize;
        }
        
        
    }

    /**
     * Contains information about a Nuxeo user.
     * Attributes are from the Nuxeo User entity type.
     * Specification: 
     * http://doc.nuxeo.com/display/NXDOC/REST+API+Entity+Types
     * 
     * @author mhoffman
     *
     */
    public static class UserEntityType extends AbstractEntityType{

        private static final String DEFAULT_ENTITY_TYPE = "user";
        
        /** 
         * unique Nuxeo user id (appears to usually be the username by default)
         *  
         * Blank when creating user as Nuxeo will assign the user id
         */
        private String id = Constants.EMPTY;
        private Properties properties;
        
        private boolean isAdministrator = false;
        private boolean isAnonymous = false;

        /**
         * For existing user
         * 
         * @param id Nuxeo ID of the user
         * @param properties User properties
         */
        public UserEntityType(String id, Properties properties){
            super(DEFAULT_ENTITY_TYPE);
            setId(id);
            setProperties(properties);
        }
        
        /**
         * For creating a user
         * 
         * @param properties User properties
         */
        public UserEntityType(Properties properties){
            super(DEFAULT_ENTITY_TYPE);
            setProperties(properties);
        }
        
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Properties getProperties() {
            return properties;
        }

        public void setProperties(Properties properties) {
            this.properties = properties;
        }
        
        public boolean isAdministrator() {
            return isAdministrator;
        }

        public void setAdministrator(boolean isAdministrator) {
            this.isAdministrator = isAdministrator;
        }

        public boolean isAnonymous() {
            return isAnonymous;
        }

        public void setAnonymous(boolean isAnonymous) {
            this.isAnonymous = isAnonymous;
        }

        @Override
        public String toString(){
            
            StringBuilder sb = new StringBuilder();
            sb.append("[UserEntityType: ");
            sb.append("id = ").append(getId());
            sb.append(", properties = ").append(getProperties());
            sb.append(", isAdmin = ").append(isAdministrator());
            sb.append(", isAnonymous = ").append(isAnonymous()); 
            sb.append("]");
            return sb.toString();
        }

        public static class Properties{
            
            private String lastName;
            private String firstName;
            private String username;
            private String email;
            private String company;
            private String password;
            
            private List<String> groups = new ArrayList<>();
            
            public Properties(String firstName, String lastName, String username, String email, String company, String password){
                setFirstName(firstName);
                setLastName(lastName);
                setUsername(username);
                setEmail(email);
                setCompany(company);
                setPassword(password);
            }

            public String getLastName() {
                return lastName;
            }

            public void setLastName(String lastName) {
                this.lastName = lastName;
            }

            public String getFirstName() {
                return firstName;
            }

            public void setFirstName(String firstName) {
                this.firstName = firstName;
            }

            /**
             * Return the lowercase version of the username.
             * 
             * @return the username
             */
            public String getUsername() {
                return username;
            }

            public void setUsername(String username) {
                //Nuxeo is case sensitive, so make all GIFT user names lowercase
                this.username = username.toLowerCase();
            }

            public String getEmail() {
                return email;
            }

            public void setEmail(String email) {
                this.email = email;
            }

            public String getCompany() {
                return company;
            }

            public void setCompany(String company) {
                this.company = company;
            }

            public String getPassword() {
                return password;
            }

            public void setPassword(String password) {
                this.password = password;
            }            
            
            public void setGroups(List<String> groups){
                this.groups = groups;
            }
            
            public List<String> getGroups(){
                return groups;
            }
            
            public void addGroup(String groupname){
                groups.add(groupname);
            }
            
            @Override
            public String toString(){
                
                StringBuilder sb = new StringBuilder();
                sb.append("[Properties: ");
                sb.append("firstName = ").append(getFirstName());
                sb.append(", lastName = ").append(getLastName());
                sb.append(", userName = ").append(getUsername());
                sb.append(", email = ").append(getEmail());
                sb.append(", Groups = {\n");
                for(String groupname : getGroups()){
                    sb.append(groupname).append(",\n");
                }
                sb.append("}");
                sb.append("]");
                return sb.toString();
            }
        }
        
    }
    
    
    /**
     * Contains information about ACLs (Access Control Lists/Permissions) that
     * belong to a document.  This class shows the permissions that are setup
     * for a given document.
     * 
     * This is encoded/decoded using Google's GSON, so the class variable names MUST match
     * the names of the JSON values.
     * 
     * Attributes are from the Nuxeo ACLs entity type.
     * Specification: 
     * http://doc.nuxeo.com/display/NXDOC/REST+API+Entity+Types
     * 
     * Permissions/Access Rights:
     * https://doc.nuxeo.com/display/USERDOC/Managing+Access+Rights
     * 
     * @author nblomberg
     *
     */
    public static class PermissionsEntityType extends AbstractEntityType{

        /** Strings containing the permission values available in Nuxeo 
         *  These are found in https://doc.nuxeo.com/display/USERDOC/Managing+Access+Rights
         */
        public static final String PERMISSION_EVERYTHING = "Everything";
        public static final String PERMISSION_WRITE = "ReadWrite";
        public static final String PERMISSION_READ = "Read";
        public static final String PERMISSION_REMOVE = "ReadRemove";
        
        /** The entity type (from the json 'entity-type' value) for this object. */
        private static final String DEFAULT_ENTITY_TYPE = "acls";
        
        /** The array */
        private List<Acl> acl = new ArrayList<>();
        
        /**
         * Constructor - default
         */
        public PermissionsEntityType() {
            super(DEFAULT_ENTITY_TYPE);
        }
        

        /**
         * Accessor to retrieve the array of acl objects for the entity 
         * 
         * @return List<Acl> - list containing acl objects for the entity.
         */
        public List<Acl> getAclList() {
            return acl;
        }

        @Override
        public String toString(){
            
            StringBuilder sb = new StringBuilder();
            sb.append("[Acls: {");
            
            for(Acl acl : getAclList()){
                sb.append(acl).append(", ");
            }
            sb.append("}");
            sb.append("]");
            return sb.toString();
        }

        /**
         * Contains information about a single ACL (Access Control List) that
         * belongs to a document.  This class shows the permissions that are setup
         * for a given document.
         * 
         * This is encoded/decoded using Google's GSON, so the class variable names MUST match
         * the names of the JSON values.
         * 
         * Attributes are from the Nuxeo ACLs entity type.
         * Specification: 
         * http://doc.nuxeo.com/display/NXDOC/REST+API+Entity+Types
         
         * @author nblomberg
         *
         */
        public static class Acl {
            
            /** Name of the acl */
            private String name;
            
            /** The ace list for the acl */
            List<AceEntity> ace;
            
            /**
             * Constructor - default
             */
            public Acl(){
              
            }
            
            /**
             * Accessor to get the ace list for the acl object.  The ace list contains
             * the array of each username/permission for the acl.
             * 
             * @return List<AceEntity> - The list of ace entities for the object.
             */
            public List<AceEntity> getAceList() {
                if (ace == null) {
                    ace = new ArrayList<AceEntity>();
                }
                return ace;
            }

            /** 
             * Accessor to get the name of the acl.
             * 
             * @return String containing the name of the acl. 
             */
            public String getName() {
                return name;
            }

            
            @Override
            public String toString(){
                
                StringBuilder sb = new StringBuilder();
                sb.append("[Acl: ");
                sb.append("name = ").append(getName());
                sb.append(", ace = { ");
                for(AceEntity aceEntity : ace){
                    sb.append(aceEntity).append(", ");
                }
                sb.append("}");
                sb.append("]");
                return sb.toString();
            }
        }
        
        /**
         * Contains information about a single ACE Entity (for a Nuxeo Access Control List) that
         * belongs to a document.  This class shows the permissions that are setup
         * for a given document.
         * 
         * This is encoded/decoded using Google's GSON, so the class variable names MUST match
         * the names of the JSON values.
         * 
         * Attributes are from the Nuxeo ACLs entity type.
         * Specification: 
         * http://doc.nuxeo.com/display/NXDOC/REST+API+Entity+Types
         
         * @author nblomberg
         *
         */
        public static class AceEntity {
            
            /** The username of the ace entity */
            String username;
            
            /** The permission level */
            String permission;
            
            /** Whether the permission is granted or not (true/false) */
            boolean granted;
            
            /** 
             * Accessor to get the username for the ace object.  This could be an actual
             * single user (username) or the name of the 'group' such as 'members'.
             * 
             * @return String containing the username of the ace object.
             */
            public String getUsername() {
                return username;
            }
            
            /**
             * Accessor to get the string containing the permission level for the ace entity.
             * The full list of Permission levels (access rights) are documented here:
             *   https://doc.nuxeo.com/display/USERDOC/Managing+Access+Rights
             * 
             * @return String containing the permission of the ace object.
             */
            public String getPermission() {
                return permission;
            }
            
            /**
             * Set a new permission level, locally in memory.  This doesn't apply the new permission
             * to the document in Nuxeo.
             */
            public void setDirtyPermission(String dirtyPermission){
                this.permission = dirtyPermission;
            }
            
            /**
             * Accessor to return if the permission is granted for the username/group of the entity.
             * 
             * @return boolean - true if the permission is granted, false otherwise.
             */
            public boolean isGranted() {
                return granted;
            }            
            
            @Override
            public int hashCode() {
                return Objects.hash(granted, permission, username);
            }

            @Override
            public boolean equals(Object obj) {
                if (this == obj)
                    return true;
                if (obj == null)
                    return false;
                if (getClass() != obj.getClass())
                    return false;
                AceEntity other = (AceEntity) obj;
                return granted == other.granted && Objects.equals(permission, other.permission)
                        && Objects.equals(username, other.username);
            }

            @Override
            public String toString(){
                
                StringBuilder sb = new StringBuilder();
                sb.append("[ace: ");
                sb.append("username = ").append(username);
                sb.append(", permission = ").append(permission);
                sb.append(", granted = ").append(granted);
                sb.append("]");
                return sb.toString();
            }
        }
        
    }
    
}
