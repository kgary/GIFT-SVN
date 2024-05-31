/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.server;

import java.util.HashMap;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.AbstractRuntimeParameters;
import mil.arl.gift.common.gwt.client.RpcResponse;
import mil.arl.gift.common.gwt.server.AsyncResponseCallback;

/**
 * The CourseLaunchManager class is responsible for starting the launch sequence for a course and holding the results
 * for the web client.  This is used so the client can asynchronously start the course launch sequence and continue processing
 * other requests since the process of starting the domain session can take a bit.  Especially in server mode, the domain session
 * is not started until the *.jnlp file is opened by the user.  We don't want the client to block for minutes/hours while it waits for
 * the user to start the *.jnlp.
 * 
 * Instead the course launch manager gets the results back from the requests and saves them for the web client until they are requested
 * later.  The web client polls the server then via the checkCourseLaunchStatus request until a response is returned by the server.
 * 
 * @author nblomberg
 *
 */
public class CourseLaunchManager {

    private static Logger logger = LoggerFactory.getLogger(CourseLaunchManager.class.getName());
    /** singleton instance of this class */
    private static CourseLaunchManager instance = null;
    
    /** Contains a mapping of each unique course launch request along with a response (success or failure) once it has completed. */
    private HashMap<CourseLaunchKey, RpcResponse> courseLaunchMap = new HashMap<CourseLaunchKey, RpcResponse>();

    /**
     * Return the singleton instance of this class
     *
     * @return CourseLaunchManager
     */
    public static CourseLaunchManager getInstance() {

        if (instance == null) {
            instance = new CourseLaunchManager();
        }

        return instance;
    }

    /**
     * Class constructor
     */
    private CourseLaunchManager() {
       
    }
    
    /**
     * Starts the course launch request for a specific user.  This is an asynhronous call and the complete result of the
     * startCourse request is saved later on the server. The caller must then use polling via the getCourseLaunchStatus 
     * request to determine with the response has completed.
     * 
     * @param browserSessionKey The browser session key starting the course.
     * @param domainRuntimeId The runtime id of the course.
     * @param domainSourceId The source id of the course.
     * @return A response object containing success or failure of the initial request.  This is not the final asynchronous request. 
     */
    public RpcResponse startCourseAsync(final String browserSessionKey, final String domainRuntimeId, final String domainSourceId, final AbstractRuntimeParameters runtimeParams) {
        RpcResponse syncResponse = null;
        try{
            // The sync response returns immediately, but the async response is filled in later.
            //select domain
            //Note: allocates Domain module
            final TutorBrowserWebSession session = TutorModule.getInstance().getBrowserSession(browserSessionKey);
            if (session != null) {
                UserWebSession userSession = TutorModule.getInstance().getUserSession(session.getUserSessionKey());
                if (userSession != null) {
                    if(logger.isInfoEnabled()){
                        logger.info("User selected "+domainRuntimeId+" as the course for "+userSession);
                    }

                    final CourseLaunchKey key = new CourseLaunchKey(browserSessionKey, domainRuntimeId);
                    userSession.userSelectDomain(domainRuntimeId, domainSourceId, session, runtimeParams, new AsyncResponseCallback() {
                        
                        @Override
                        public void notify(boolean success, String response, String additionalInformation) {    

                            RpcResponse asyncResponse = null;
                            if(success){
                                asyncResponse = new RpcResponse(session.getUserSessionKey(), session.getBrowserSessionKey(), true, response);
                            }else{
                                asyncResponse = new RpcResponse(session.getUserSessionKey(), session.getBrowserSessionKey(), false, response);
                                asyncResponse.setAdditionalInformation(additionalInformation);
                            }

                            // Add the response to the course launch map.
                            synchronized(courseLaunchMap) {
                                courseLaunchMap.put(key, asyncResponse);
                            }
                        }
                    });

                    // Reset the course launch map (if there was a previous value).
                    synchronized(courseLaunchMap) {
                        courseLaunchMap.put(key, null);
                    }

                    syncResponse = new RpcResponse(session.getUserSessionKey(), session.getBrowserSessionKey(), true, null);
                } else {
                    logger.warn("userStartDomainSession userSession = null, using null for userSessionId and "+  session.getBrowserSessionKey() + " for broserSessionId and response = false");
                    syncResponse = new RpcResponse(null, session.getBrowserSessionKey(), false, "Browser session does not have a valid user session.");
                }
            } else {
                logger.warn("userStartDomainSession session = null, using null for userSessionId and browserSessionId and response = false");
                syncResponse = new RpcResponse(null, null, false, "Not a valid browser session.");
            }
        }catch(IllegalArgumentException e){
            logger.error("Exception caught in userStartDomainSession with the message: " + e.getMessage());
            throw e;
        }
        
        return syncResponse;
    }
    
    /**
     * Checks the status of a course launch request.  The course launch request response will be null until
     * a response is returned by ActiveMQ (or an error occurs).
     * 
     * @param browserSessionKey The browser session making the request.
     * @param domainRuntimeId The domain runtime id for the request.
     * @param domainSourceId (unused) The domain source id of the request.
     * @return
     */
    public RpcResponse getCourseLaunchStatus(String browserSessionKey, String domainRuntimeId, String domainSourceId) {
        // Reset the course launch map (if there was a previous value).
           final CourseLaunchKey key = new CourseLaunchKey(browserSessionKey, domainRuntimeId);
           synchronized(courseLaunchMap) {
               
               return courseLaunchMap.get(key);
           }
       }
    
    
    /**
     * The CourseLaunchKey class holds a key that is a combination of the browser session and domain runtime id.  These
     * values form a unique key value that's used to hold the result of a course launch request.
     * 
     * @author nblomberg
     *
     */
    public class CourseLaunchKey {
        
        private String browserSession;
        private String domainRuntimeId;
        
        public CourseLaunchKey(String browserSession, String domainRuntimeId) {
            
            if (browserSession == null) {
                throw new IllegalArgumentException("The browser session cannot be null.");
                
            }
            
            if (domainRuntimeId == null) {
                throw new IllegalArgumentException("The domain runtime id cannot be null.");
            }
            
            this.browserSession = browserSession;
            this.domainRuntimeId = domainRuntimeId;
        }
        
        /**
         * @return the browserSession
         */
        public String getBrowserSession() {
            return browserSession;
        }

        /**
         * @param browserSession the browserSession to set
         */
        public void setBrowserSession(String browserSession) {
            this.browserSession = browserSession;
        }

        /**
         * @return the domainRuntimeId
         */
        public String getDomainRuntimeId() {
            return domainRuntimeId;
        }

        /**
         * @param domainRuntimeId the domainRuntimeId to set
         */
        public void setDomainRuntimeId(String domainRuntimeId) {
            this.domainRuntimeId = domainRuntimeId;
        }

        @Override
        public boolean equals(Object obj) {
            
            if (obj == null) {
                return false;
            }
            
            if (!CourseLaunchKey.class.isAssignableFrom(obj.getClass())) {
                return false;
            }
            
            CourseLaunchKey other = (CourseLaunchKey)obj;
            
            if (Objects.equals(this.browserSession, other.browserSession) &&
                Objects.equals(this.domainRuntimeId, other.domainRuntimeId)) {
                    return true;
            }
            
            return false;
        }
        
        @Override
        public int hashCode() {
            int hashCode = getBrowserSession().hashCode() + getDomainRuntimeId().hashCode();
            return hashCode;
        }

        @Override 
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("[CourseLaunchKey:");
            sb.append(" browserSession = ").append(getBrowserSession());
            sb.append(", domainRuntimeId = ").append(getDomainRuntimeId());
            sb.append("]");
            return sb.toString();
        }
    }



    
    

}
