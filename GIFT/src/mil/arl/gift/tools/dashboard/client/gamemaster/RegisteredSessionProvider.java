/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.gamemaster;

import java.util.logging.Level;
import java.util.logging.Logger;

import mil.arl.gift.common.aar.LogMetadata;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.gwt.client.util.AbstractProvider;
import mil.arl.gift.tools.dashboard.client.gamemaster.RegisteredSessionProvider.RegisteredSessionChangeHandler;

/**
 * A singleton class that handles changes to the registered session.
 * 
 * @author sharrison
 */
public class RegisteredSessionProvider extends AbstractProvider<RegisteredSessionChangeHandler> {
    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(RegisteredSessionProvider.class.getName());

    /** The instance of the class */
    private static RegisteredSessionProvider instance = null;

    /** The registered session */
    private AbstractKnowledgeSession registeredSession;

    /** The log metadata for the selected session playback */
    private LogMetadata logMetadata;

    /**
     * Singleton constructor
     */
    private RegisteredSessionProvider() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(".ctor()");
        }
    }

    /**
     * Accessor to the provider singleton object. If it doesn't exist yet it
     * will be created.
     * 
     * @return the instance to the provider singleton object.
     */
    public static RegisteredSessionProvider getInstance() {
        if (instance == null) {
            instance = new RegisteredSessionProvider();
        }

        return instance;
    }

    /**
     * Set the registered session.
     * 
     * @param knowledgeSession the session that is registered. Can be null if no
     *        session is registered.
     */
    public void setRegisteredSession(final AbstractKnowledgeSession knowledgeSession) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("setRegisteredSession(" + knowledgeSession + ")");
        }

        final AbstractKnowledgeSession oldSession = registeredSession;

        /* Determine if the session changed */
        if (knowledgeSession == null && oldSession == null) {
            /* Both null */
            return;
        } else if (knowledgeSession != null && knowledgeSession.equals(oldSession)) {
            /* Both equal */
            return;
        }

        /* Update registered session */
        registeredSession = knowledgeSession;
        this.logMetadata = null;

        /* Notify handlers */
        executeHandlers(new SafeHandlerExecution<RegisteredSessionChangeHandler>() {
            @Override
            public void execute(RegisteredSessionChangeHandler handler) {
                handler.registeredSessionChanged(registeredSession, oldSession);
            }
        });
    }
    
    /**
     * Set the registered session.
     * 
     * @param logMetadata the log metadata associated with the
     */
    public void setRegisteredSessionByLog(final LogMetadata logMetadata) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("setRegisteredSessionByLog(" + logMetadata + ")");
        }

        final AbstractKnowledgeSession oldSession = registeredSession;

        /* Determine if the session changed */
        if (logMetadata == null && oldSession == null) {
            /* Both null */
            return;
        } else if (logMetadata != null && logMetadata.getSession().equals(oldSession)) {
            /* Both equal */
            return;
        }

        /* Update registered session */
        this.logMetadata = logMetadata;
        registeredSession = logMetadata == null ? null : logMetadata.getSession();

        /* Notify handlers */
        executeHandlers(new SafeHandlerExecution<RegisteredSessionChangeHandler>() {
            @Override
            public void execute(RegisteredSessionChangeHandler handler) {
                handler.registeredSessionChanged(registeredSession, oldSession);
            }
        });
    }

    /**
     * Retrieve the registered knowledge session.
     * 
     * @return the knowledge session that is currently registered. Can be null
     *         if no session is registered.
     */
    public AbstractKnowledgeSession getRegisteredSession() {
        return registeredSession;
    }

    /**
     * Checks if the provided domain session id matches the registered session's
     * domain session id.
     * 
     * @param domainSessionId the domain session id to check.
     * @return true if the domain session id matches the registered session's
     *         domain session id; false otherwise.
     */
    public boolean isRegistered(int domainSessionId) {
        if (registeredSession != null) {
            return registeredSession.getHostSessionMember().getDomainSessionId() == domainSessionId;
        }

        return false;
    }

    /**
     * Checks if any knowledge session is registered.
     * 
     * @return true if a session is currently registered; false otherwise.
     */
    public boolean hasRegisteredSession() {
        return registeredSession != null;
    }

    /**
     * Retrieve the log metadata for the registered knowledge session.
     * 
     * @return the log metadata for the registered knowledge session. Can be
     *         null if no session is registered or if there is no associated log
     *         metadata for the registered session.
     */
    public LogMetadata getLogMetadata() {
        return logMetadata;
    }

    /**
     * Checks if the registered knowledge session has an associated log
     * metadata.
     * 
     * @return true if a log metadata exists; false otherwise.
     */
    public boolean hasLogMetadata() {
        return logMetadata != null;
    }

    /**
     * Update the log patch file with the new name
     * 
     * @param logPatchFileName the new log patch file name. Can be null if the
     *        log patch file was deleted.
     */
    public void updateLogPatchFile(final String logPatchFileName) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("updateLogPatchFile(" + logPatchFileName + ")");
        }

        if (!hasLogMetadata()) {
            return;
        }

        logMetadata.setLogPatchFile(logPatchFileName);

        /* Notify handlers */
        executeHandlers(new SafeHandlerExecution<RegisteredSessionChangeHandler>() {
            @Override
            public void execute(RegisteredSessionChangeHandler handler) {
                handler.logPatchFileChanged(logPatchFileName);
            }
        });
    }

    /**
     * Handler for listening to changes in the active knowledge session.
     * 
     * @author sharrison
     */
    public interface RegisteredSessionChangeHandler {
        /**
         * The registered session has changed.
         * 
         * @param newSession the new registered session. Can be null.
         * @param oldSession the old registered session. Can be null.
         */
        void registeredSessionChanged(AbstractKnowledgeSession newSession, AbstractKnowledgeSession oldSession);

        /**
         * Called when the log patch file is modified.
         * 
         * @param logPatchFileName the new log patch file name. Can be null if
         *        the log patch file was deleted.
         */
        void logPatchFileChanged(String logPatchFileName);
    }
}
