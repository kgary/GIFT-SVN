/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.server.lti;


import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.client.utils.URIBuilder;
import org.imsglobal.lti.launch.LtiOauthVerifier;
import org.imsglobal.lti.launch.LtiVerificationException;
import org.imsglobal.lti.launch.LtiVerificationResult;
import org.imsglobal.lti.launch.LtiVerifier;
import org.imsglobal.pox.IMSPOXRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.CourseRecord;
import mil.arl.gift.common.experiment.ExperimentUtil.ExperimentStatus;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.ImageProperties;
import mil.arl.gift.common.lti.TrustedLtiConsumer;
import mil.arl.gift.tools.dashboard.server.DashboardProperties;
import mil.arl.gift.tools.services.ServicesManager;
import mil.arl.gift.tools.services.db.DbServicesInterface;
import mil.arl.gift.tools.services.experiment.DataCollectionServicesInterface;
import mil.arl.gift.ums.db.table.DbDataCollection;
import oauth.signpost.exception.OAuthException;

/**
 * The LTI Tool Provider Tutor Servlet is a servlet that implements the LTI Tool Provider specification
 * for LTI 1.1.1.  This class allows GIFT to be an LTI Tool Provider and communicate with
 * external Tool Consumers.  It is a Tutor servlet, because this implementation will
 * redirect LTI Consumers back to the Tutor Web Client to run courses.
 * This servlet will be responsible for handling the incoming LTI launch requests,
 * and sending back the proper redirect URL.  The effect will be for the incoming launch request to specify
 * a specific GIFT course that an external user wants to launch.  The servlet should manage mapping that external
 * user and setting up the course internally so that the user can run the course.
 * 
 * @author nblomberg
 *
 */
public class LtiToolProviderTutorServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(LtiToolProviderTutorServlet.class);
    
    
    /** Mapping of trusted consumers (based on the consumer key) which is read in from the configuration file at launch. */
    private HashMap<String, TrustedLtiConsumer> consumerMap;


    // Constants ----------------------------------------------------------------------------------
    private static final String LTI_ERROR_PAGE_JSP_PATH = "/WEB-INF/ltiError.jsp";
    private static final String GENERIC_VALIDATION_TITLE = "LTI Verification Failure";
    private static final String INTERNAL_SERVER_ERROR_TITLE = "LTI Tool Provider Internal Server Error";
    
    // The following are parameters that are passed to the jsp page.
    private static final String JSP_PARAM_TITLE = "title";
    private static final String JSP_PARAM_MESSAGE = "message";
    private static final String JSP_PARAM_DETAILS = "details";
    
    // parameter used for background image
    private static final String JSP_PARAM_BACKGROUND = "backgroundUrl";
    
    // parameter used for logo image
    private static final String JSP_PARAM_LOGO = "logoUrl";
    
    private static final String GENERIC_VALIDATION_ERROR = "There was a failure validating the LTI launch request," +
            " please make sure that the LTI consumer key," + 
            " shared secret, and custom parameters are setup properly for the course.  If you are a student, you may need to" + 
            " contact the instructor to ensure the proper consumer key, shared secret, and custom parameters are used for this course.";
    
    private static final String GENERIC_INTERNAL_CONFIG_ERROR = "The LTI launch request could not be processed due to the GIFT server"
            + " not being configured correctly.";
    private static final String GENERIC_INTERNAL_ERROR = "The LTI launch request could not be processed.";
    
    private static final String GENERIC_STATUS_ERROR = "The LTI launch request could not be processed, because the instructor "+ 
            " has not made the course available.  If you are a student, you may need to contact the instructor to have the course enabled.";
    
    /** Note that the user fills out the tag 'course_id', but the Tool Consumer adds the prefix 'custom' to the parameter. */
    private static final String CUSTOM_PREFIX = "custom_";
    private static final String COURSE_ID_TAG = "course_id";
    private static final String DATA_SET_ID_TAG = "data_set_id";
    private static final String CUSTOM_COURSE_ID_TAG = CUSTOM_PREFIX + COURSE_ID_TAG;
    private static final String CUSTOM_DATA_SET_ID_TAG = CUSTOM_PREFIX + DATA_SET_ID_TAG;
    
    /** Services to the database api. */
    private DbServicesInterface dbServices;
    /** Services to the experiments api. */
    DataCollectionServicesInterface expServices;
    
    
    // Actions ------------------------------------------------------------------------------------
    @Override
    public void init() throws ServletException {

        if (logger.isInfoEnabled()) {
            logger.info("Servlet initialized.");
        }

        // Read in the trusted lti consumer map from the configuration file.
        consumerMap = DashboardProperties.getInstance().getTrustedLtiConsumers();
        
        if (consumerMap == null || consumerMap.isEmpty()) {
            logger.error("The Trusted LTI Consumer Map could not be parsed properly from the commons.properties file.  The servlet will not process any incoming LTI requests.");
        }
        
        // Initialize the services.
        dbServices = ServicesManager.getInstance().getDbServices();
        expServices = ServicesManager.getInstance().getDataCollectionServices();
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
       
        if (logger.isInfoEnabled()) {
            logger.info("Servlet received request: " + request.toString());
        }

        // Ensure that the services that will be used are valid.
        if (dbServices == null || expServices == null) {
            logger.error("One or more of the required services used by the servlet are null. DbServices = " + dbServices + ", ExperimentServices=" + expServices);
            showJspErrorPage(request, response, INTERNAL_SERVER_ERROR_TITLE, GENERIC_INTERNAL_CONFIG_ERROR, 
                    "The services api could not be properly accessed on the server.");            
            return;  
        }

        // Debug option to print out the contents of each incoming post request.
        if (logger.isDebugEnabled()) {
            
            // Print out the post contents.
            logger.debug("Incoming POST request START:");
            logger.debug("  Method type: " + request.getMethod());
            logger.debug("  Authorization type: " + request.getAuthType());
            
            @SuppressWarnings("rawtypes")
            Enumeration headerNames = request.getHeaderNames();
            while(headerNames.hasMoreElements()) {
              String headerName = (String)headerNames.nextElement();
              logger.debug("  Header Name - " + headerName + ", Value - " + request.getHeader(headerName));
            }
            
            @SuppressWarnings("rawtypes")
            Enumeration params = request.getParameterNames(); 
            while(params.hasMoreElements()){
             String paramName = (String)params.nextElement();
             logger.debug("  Parameter Name - "+paramName+", Value - "+request.getParameter(paramName));
            }
            
            logger.debug("Incoming POST request END");
            
        }
        
        if (consumerMap == null)
        {
            showJspErrorPage(request, response, INTERNAL_SERVER_ERROR_TITLE, GENERIC_INTERNAL_CONFIG_ERROR, 
                    "There are no Trusted LTI Tool Consumers configured for the GIFT LTI Tool Provider server.");            
            return;  
        }
        
        String consumerKey = request.getParameter(Constants.OAUTH_CONSUMER_KEY);
        if (consumerKey == null || consumerKey.isEmpty()) {
            logger.error("Could not find a required parameter '" + Constants.OAUTH_CONSUMER_KEY + "' in the incoming lti request.");
            showJspErrorPage(request, response, GENERIC_VALIDATION_TITLE, GENERIC_VALIDATION_ERROR,
                    "The LTI Tool Consumer did not provide the '" + Constants.OAUTH_CONSUMER_KEY + "' parameter.");
            return;  
        }
        
        TrustedLtiConsumer consumer = consumerMap.get(consumerKey);
        
        if (consumer == null) {
            String details = "Invalid consumer key received in request.  Invalid key is: " + consumerKey;
            logger.error(details);
            showJspErrorPage(request, response, GENERIC_VALIDATION_TITLE, GENERIC_VALIDATION_ERROR, details);
            return;
        }
        
        // The timestamp is used here and collected so that the gift db entry can record when the original timestamp
        // of the oauth request was encoded.   This is used as opposed to relying on the timestamp of the request
        // which is not correct.  Instead the timestamp of the oauth encoding should be used as the timestamp
        // the gift database for the lti user record.
        String oAuthTimestampStr = request.getParameter(Constants.OAUTH_TIMESTAMP);
        Long oAuthTimestamp = 0L;
        if (oAuthTimestampStr == null || oAuthTimestampStr.isEmpty()) {
            String details = "Missing expected OAuth parameters in the request.";
            logger.error(details);
            showJspErrorPage(request, response, GENERIC_VALIDATION_TITLE, GENERIC_VALIDATION_ERROR, details);
            return;
        } else {
            
            try {
                oAuthTimestamp = Long.parseLong(oAuthTimestampStr);
            } catch (@SuppressWarnings("unused") NumberFormatException e) {
                String details = "Expected timestamp parameter is not valid.";
                logger.error(details);
                showJspErrorPage(request, response, GENERIC_VALIDATION_TITLE, GENERIC_VALIDATION_ERROR, details);
                return;
            }
            
        }
        
        
        
        try {
            LtiVerifier ltiVerifier = new LtiOauthVerifier();
            // Note:  The LTI result can be verified directly against the request using the commented code below.
            // However, this will NOT work if a proxy server is forwarding the request to the servlet since the
            // launch url will no longer match the request url.  So instead of using the request url, the
            // alternative method that LTI provides to validate is to pass in the url directly instead of relying
            // on the request url.  Here's the method that can be setup if a proxy is not involved:
            //   LtiVerificationResult ltiResult = ltiVerifier.verify(request, consumer.getConsumerSharedSecret());
            String ltiUrl = DashboardProperties.getInstance().getLtiServletUrl();
            // The url that is passed in to verify against, must match the original client side lti launch url.
            // The request url should only be used when a proxy is not involved, otherwise, use the lti url from
            // the configuration files.
            logger.debug("LTI url: " + ltiUrl);
            logger.debug("Request url: " + request.getRequestURL());
            
            // Build the parameters to validate against.
            HashMap<String, String> parameters = new HashMap<String, String>();
            @SuppressWarnings("rawtypes")
            Enumeration requestParams = request.getParameterNames(); 
            while(requestParams.hasMoreElements()){
                String paramName = (String)requestParams.nextElement();
                String paramValue = request.getParameter(paramName);
                parameters.put(paramName,  paramValue);
            }
            
            // Validate the request.
            LtiVerificationResult ltiResult = ltiVerifier.verifyParameters(parameters, ltiUrl, 
                    request.getMethod(), consumer.getConsumerSharedSecret());
            logger.info("LTI Verification Result = " + ltiResult.toString());
            logger.info("LTI Verification Result Success = " + ltiResult.getSuccess());
            
            
            if (!ltiResult.getSuccess()) {
                logger.error("LTI Verification Result error = " + ltiResult.getError());
                logger.error("LTI Verification Result message = " + ltiResult.getMessage());
                showJspErrorPage(request, response, GENERIC_VALIDATION_TITLE, GENERIC_VALIDATION_ERROR, ltiResult.getMessage());
                return;
            }

            logger.info("LTI Launch request validated for consumer key: " + consumerKey);
        } catch (LtiVerificationException e) {
            String details = "Error caught verifying the incoming LTI launch request: " + e.getMessage();
            logger.error(details, e);
            showJspErrorPage(request, response, INTERNAL_SERVER_ERROR_TITLE, GENERIC_INTERNAL_ERROR, details);
            return;
        }
  
        // At this point the LTI request is validated.  The next steps are:
        // Check for other required parameters (such as course id).
        // Check for the user_id parameter from the incoming request.
        String consumerId = request.getParameter(Constants.USER_ID);
        if (consumerId == null || consumerId.isEmpty()) {
            logger.error("Could not find a required parameter 'user_id' in the incoming lti request.");
            showJspErrorPage(request, response, GENERIC_VALIDATION_TITLE, GENERIC_VALIDATION_ERROR, 
                    "The LTI Tool Consumer did not provide the 'user_id' parameter.");
            return;
        }

        // Check the custom course id parameter which links to the path of the course that will be launched in GIFT.
        String courseId = request.getParameter(CUSTOM_COURSE_ID_TAG);
        if (courseId == null || courseId.isEmpty()) {
            logger.error("Could not find a required parameter '" + CUSTOM_COURSE_ID_TAG + "' in the incoming lti request.");
            showJspErrorPage(request, response, GENERIC_VALIDATION_TITLE, GENERIC_VALIDATION_ERROR, 
                    "The LTI Tool Consumer did not provide the '" + COURSE_ID_TAG + "' parameter.");
            return;
        }
        
        // Check to see if the optional score parametes are available.
        // If these parameters are set, then GIFT will report a score back to the Tool Consumer during course
        // runtime.
        boolean scoringEnabled = false;
        String serviceUrl = request.getParameter(Constants.LIS_OUTCOME_SERVICE_URL);
        String sourcedid = request.getParameter(Constants.LIS_RESULT_SOURCEDID);
        if (serviceUrl != null && !serviceUrl.isEmpty() &&
            sourcedid != null && !sourcedid.isEmpty()) {
            
            // Indicate that the scoring parameters will be used and the score should
            // be reported back to the Tool Consumer.
            scoringEnabled = true;
        }
        
        
        
        
        // Validate the optional publish course id parameter (which causes the course to be tracked for data collection).
        String dataSetId = request.getParameter(CUSTOM_DATA_SET_ID_TAG);
        if (dataSetId == null || dataSetId.isEmpty()) {
            // Default to an empty string if the parameter doesn't exist.
            dataSetId = "";
        }
        
        
        if (!dataSetId.isEmpty()) {
            // If the publish course is being used, make sure it is valid and found in the GIFT tables.

            DbDataCollection experiment = null;
            try {
                experiment = expServices.getDataCollectionItem(dataSetId);
               
            } catch (DetailedException e) {
                logger.error("Detailed exception caught getting experiment: ", e);
                experiment = null;
            }
            
            if (experiment == null) {
                logger.error("Invalid parameter found '" + CUSTOM_DATA_SET_ID_TAG + "' in the incoming lti request.");
                showJspErrorPage(request, response, GENERIC_VALIDATION_TITLE, GENERIC_VALIDATION_ERROR, 
                        "The LTI Tool Consumer provided an invalid '" + DATA_SET_ID_TAG + "' parameter.  The LTI published course could not be found." + 
                        "  If you are an instructor, please make sure the 'Custom Parameters' are copied properly to the" + 
                        " Tool Consumer from the Publish Courses panel in GIFT for this published course.");
                return;
            }
            
            // The course id passed in must match the source course id of the published course.  If not, then this is invalid.
            // The dataset cannot be shared between multiple courses.
            if (experiment.getSourceCourseId() == null) {
                logger.error("Invalid parameter found '" + CUSTOM_DATA_SET_ID_TAG + "' in the incoming lti request.");
                showJspErrorPage(request, response, GENERIC_VALIDATION_TITLE, GENERIC_VALIDATION_ERROR, 
                        "The LTI Tool Consumer provided an invalid '" + DATA_SET_ID_TAG + "' parameter." + 
                        "  If you are an instructor, please make sure the 'Custom Parameters' are copied properly to the" + 
                        " Tool Consumer from the Publish Courses panel in GIFT for this published course.");
                return;
            }
            
            if (courseId.compareTo(experiment.getSourceCourseId()) != 0) {
                logger.error("Invalid parameter found '" + CUSTOM_DATA_SET_ID_TAG + "' in the incoming lti request.");
                showJspErrorPage(request, response, GENERIC_VALIDATION_TITLE, GENERIC_VALIDATION_ERROR, 
                        "The LTI Tool Consumer provided an invalid '" + DATA_SET_ID_TAG + "' parameter. The published course does not match the course." + 
                        "  If you are an instructor, please make sure the 'Custom Parameters' are copied properly to the" + 
                        " Tool Consumer from the Publish Courses panel in GIFT for this published course.");
                return;
            }
            
            // Ensure that the experiment status is set to ACTIVE (not paused or in an invalid state)..
            if (experiment.getStatus().compareTo(ExperimentStatus.RUNNING) != 0) {
                
                showJspErrorPage(request, response, GENERIC_VALIDATION_TITLE, GENERIC_STATUS_ERROR, 
                        "If you are an instructor, please make sure the data collection is set to 'Active' for this published course " + 
                        "in the GIFT Publish Courses panel. The published course status is currently set to: "+ experiment.getStatus());
                return;
            }
        }

        // Check to see if the courseId has an entry in the Db in the course table.
        CourseRecord record = dbServices.getCourseById(courseId, false);
        if (record == null) {
            logger.error("Could not find a valid course with id: " + courseId);
            showJspErrorPage(request, response, GENERIC_VALIDATION_TITLE, GENERIC_VALIDATION_ERROR, 
                    "A valid course with id '" +courseId + "' could not be found.  Please make sure the '" + COURSE_ID_TAG + "'" + 
                    " is specified correctly.");
            return;
        }
        
        // Establish the lti user session based on the consumer key and consumer user id.
        // convert oauth timestamp (in seconds) to milliseconds.
        Long oAuthTimestampMs = oAuthTimestamp * 1000;
        boolean success = dbServices.updateOrCreateLtiUserRecord(consumer,  consumerId, new Date(oAuthTimestampMs));
        if (!success) {
            logger.error("An lti user session could not be started based on the consumer key and consumer user id.");
            showJspErrorPage(request, response, INTERNAL_SERVER_ERROR_TITLE, GENERIC_INTERNAL_ERROR, 
                    "The GIFT server was not successful creating an lti user session.");
            return;
        }

        // Send back the redirect url.
        try {
            String dashboardUrl = DashboardProperties.getInstance().getDashboardURL() + Constants.FORWARD_SLASH;
            URIBuilder builder = new URIBuilder(dashboardUrl);
            builder.addParameter(Constants.LTI_CONSUMER_KEY, consumerKey);
            builder.addParameter(Constants.LTI_CONSUMER_ID, consumerId);
            builder.addParameter(Constants.LTI_COURSE_ID, courseId);
            
            // The data collection published course id is an optional parameter that the instructor can use to 
            // indicate that the course data should be collected for each user that launches the course in this manner.
            if (dataSetId != null && !dataSetId.isEmpty()) {
                builder.addParameter(Constants.LTI_DATA_SET_ID, dataSetId);
            }
            
            // If the lis outcome service (scoring) is enabled, then send the parameters so GIFT can report a score
            // back to the Tool Consumer.
            if (scoringEnabled) {
                builder.addParameter(Constants.LTI_OUTCOME_SERVICE_URL, serviceUrl);
                builder.addParameter(Constants.LTI_OUTCOME_SERVICE_ID, sourcedid);
            }

            builder.build().toString();
            
            // If we get this far consider a success, then use the data to send a redirect with 
            // the proper parameters.
            response.setContentType("text/html");
            response.setStatus(HttpServletResponse.SC_FOUND);
            response.sendRedirect(builder.build().toString());
        } catch (URISyntaxException e) {
            logger.error("Error constructing the redirect URL:", e);            
            showJspErrorPage(request, response, INTERNAL_SERVER_ERROR_TITLE, GENERIC_INTERNAL_ERROR, "A URI syntax exception occurred processing the LTI launch request: " + e.getMessage());
            return;
        } catch (Exception e) {
            logger.error("An exception occurred processing the LTI launch request: ", e);
            showJspErrorPage(request, response, INTERNAL_SERVER_ERROR_TITLE, GENERIC_INTERNAL_ERROR, "An exception occurred processing the LTI launch request: " + e.getMessage());
            return;
        }
    }
    
    /**
     * Forwards the request to a jsp page containing an error.  The jsp file can take 3 parameters:
     *   a title, a message, and a details.  The details is an optional parameter.
     * @param request The servlet request that is being processed.
     * @param response The servlet response that is being processed.
     * @param title The title of the error (required).
     * @param message The message of the error (required).
     * @param details The details of the error (optional).
     * @throws ServletException 
     * @throws IOException
     */
    private void showJspErrorPage(HttpServletRequest request, HttpServletResponse response, String title, String message, String details) throws ServletException, IOException {
        request.setAttribute(JSP_PARAM_TITLE, title);
        request.setAttribute(JSP_PARAM_MESSAGE, message);
        request.setAttribute(JSP_PARAM_BACKGROUND, ImageProperties.getInstance().getPropertyValue(ImageProperties.BACKGROUND));
        request.setAttribute(JSP_PARAM_LOGO, ImageProperties.getInstance().getPropertyValue(ImageProperties.LOGO));
        
        // Details is optional.
        if (details != null && !details.isEmpty()) {
            request.setAttribute(JSP_PARAM_DETAILS, details);
        }
        request.getRequestDispatcher(LTI_ERROR_PAGE_JSP_PATH).forward(request, response);
    }

    /**
     * Used to test the IMSGlobal Lti Utilities library.  The documentation/source is located
     * https://github.com/IMSGlobal/basiclti-util-java
     * 
     * The GIFT modified source code (to fix a bug in sending score result) is located:
     * https://github.com/GIFT-Tutoring/basiclti-util-java
     * 
     * Using the following logic it was observed that the scores were posted successfully to the
     * lti test app as well as the test edX course.
     */
    private static void TestLtiUtilitiesOutcomeService() {
        try {
            
            // local testing
            // The following can be used to test with the lti test application.  
            // The secret should match the common.properties secret that is setup for the course.
            /**
            String url = "http://ltiapps.net/test/tc-outcomes.php";
            String key = "7f240703-50e8-4263-8204-9cebb6cd5157";
            String secret = "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";
            String sourcedid = "20c28c3087fb9610922f1a2beed1f608:::S3294476:::29200:::dyJ86SiwwA9";
            String score = "0.887";
            */
            
            
            // server testing
            // The following was used to test sending scores to the edX course that was setup.
            // The secret should match the common.properties secret that is setup for the course.
            String url = "https://edge.edx.org/courses/UTAX/FST101/2014_T1/xblock/i4x:;_;_UTAX;_FST101;_lti_consumer;_ecec25af422940f697913b403bfa56ab/handler_noauth/outcome_service_handler";
            String key = "c820cc70-4c55-45c8-9fd2-2d30f3caaf45";
            String secret = "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";
            String sourcedid = "UTAX/FST101/2014_T1:edge.edx.org-i4x-UTAX-FST101-lti_consumer-ecec25af422940f697913b403bfa56ab:9cd7b5c37f5f998d2e81adf5625fa7c4";     
            String score = "0.333";

            // Send the score result using the basic lti utilities library.
            IMSPOXRequest.sendReplaceResult(url, key, secret, sourcedid, score);
            
            System.out.println("Successfully completed.");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (OAuthException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * The following main function was used to test the IMS Global LTI Basic Utilities library for sending score results
     * to the tool provider.
     * 
     * @param args
     */
    public static void main(String[] args) {

        TestLtiUtilitiesOutcomeService();
        
    }

}
