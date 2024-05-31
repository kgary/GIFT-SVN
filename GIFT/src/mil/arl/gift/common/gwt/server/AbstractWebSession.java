/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.server;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletRequest;

import mil.arl.gift.common.gwt.server.authentication.UserAuthenticationException;
import mil.arl.gift.common.gwt.server.authentication.UserAuthenticationMgr;
import mil.arl.gift.common.gwt.server.authentication.UserAuthenticationMgr.UserAuthResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a session in the general sense.
 *
 * @author jleonard
 */
public abstract class AbstractWebSession {
    
    /**
     * Instance of the logger
     */
    private static Logger logger = LoggerFactory.getLogger(AbstractWebSession.class);
    
    /**
     * Listeners interested in status changes of a session
     */
    private final Set<SessionStatusListener> statusListeners = new HashSet<SessionStatusListener>();
    
    /** authentication the user */
    private static UserAuthenticationMgr authentication = UserAuthenticationMgr.getInstance();

    private SessionStatusEnum sessionStatus = SessionStatusEnum.RUNNING;
    
    /** Optional session data that may need to be stored in the web session. */
    private AbstractWebSessionData sessionData = null;
     
    /**
     * Authenticate the user's credentials
     * 
     * @param username the user name to check
     * @param password the password to check if the user name exists
     * @param loginAsUserName used to assume the identify of another GIFT user for debugging purposes.  In most cases this is null.
     * @param request the server request that called this method. Needed authenticators and cannot be null.
     * @return null if a valid user otherwise the reason for failed authentication
     * @throws UserAuthenticationException if there was a critical issue validating the credentials
     */
    public static String authenticate(String username, String password, String loginAsUserName, ServletRequest request) throws UserAuthenticationException{
        
        try{
            UserAuthResult authResult = authentication.isValidUser(username, password, loginAsUserName, request);
            return authResult.getAuthFailedReason();
        }catch(UserAuthenticationException authenticationException){
            //raise the issue higher up
            throw authenticationException;
        }catch(Throwable e){
            logger.error("Caught exception while trying to validate credentials for "+username+".", e);
        }
        
        return "An exception happened while trying to login.";
    }

    /**
     * Gets the current status of the session
     *
     * @return SessionStatusEnum
     */
    public final SessionStatusEnum getSessionStatus() {
        return sessionStatus;
    }
    
    /**
     * Called when the session is stopping
     */
    protected abstract void onSessionStopping();
    
    /**
     * Called when the session is stopped.
     */
    protected abstract void onSessionStopped();
    
    /**
     * Called when the session is ending
     */
    protected abstract void onSessionEnding();

    /**
     * Called when this session has ended
     *
     */
    protected abstract void onSessionEnded();


    /**
     * Stops the session
     */
    public void stopSession() {
        logger.info("Stopping session - "+ toString());
        sessionStatus = SessionStatusEnum.STOPPING;
        onSessionStopping();
        for (SessionStatusListener listener : statusListeners) {
            listener.onStop();
        }
        sessionStatus = SessionStatusEnum.STOPPED;
        logger.info(toString() + ": Stopped session");
    }

    /**
     * Ends the session
     */
    protected void endSession() {
        logger.info(toString() + ": Ending session");
        sessionStatus = SessionStatusEnum.ENDING;
        onSessionEnding();
        synchronized (statusListeners) {
            for (SessionStatusListener listener : statusListeners) {
                listener.onEnd();
            }
        }
        sessionStatus = SessionStatusEnum.ENDED;
        
        onSessionEnded();
        
        logger.info(toString() + ": Ended session");
    }

    /**
     * Adds a listener for changes in this session's status
     *
     * @param listener The status listener
     */
    public void addStatusListener(SessionStatusListener listener) {
        if (logger.isDebugEnabled()) {
            logger.debug(toString() + " - addStatusListener() " + listener);
        }
        
        synchronized (statusListeners) {
            statusListeners.add(listener);
        }
    }

    /**
     * Removes a listener from changes in this session's status
     *
     * @param listener The status listener
     */
    public void removeStatusListener(SessionStatusListener listener) {
        
        if (logger.isDebugEnabled()) {
            logger.debug(toString() + " - removeStatusListener() " + listener);
        }
        synchronized (statusListeners) {
            statusListeners.remove(listener);
        }
    }
    
    /**
     * Get the data associated with the session.
     * @return (optional) AbstractWebSessionData The data associated with the session.  Can be null.
     */
    public AbstractWebSessionData getSessionData() {
        return sessionData;
    }

    /**
     * Set the session data (optional) for the session.  Can be used
     * to store feature specific data.
     * 
     * @param sessionData The data to be stored in the session.
     */
    public void setSessionData(AbstractWebSessionData sessionData) {
        this.sessionData = sessionData;
    }
    
    
}
