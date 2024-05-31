/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor;

import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;

import mil.arl.gift.common.survey.Survey;
import mil.arl.gift.tools.authoring.server.gat.client.survey.SurveyRpcService;
import mil.arl.gift.tools.authoring.server.gat.client.survey.SurveyRpcServiceAsync;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.shared.SurveyDependencies;

/**
 * This class is responsible for managing and checking for survey permissions.  It is responsible
 * for managing acquiring/updating locks for surveys and releasing the locks.  It can also check user permissions
 * on surveys.  It is responsible for managing the permissions of a single survey at a time.  
 * 
 * A lot of this code was ported from the old SAS tool and refactored to work with the newer Survey Editor.
 * 
 * @author nblomberg
 *
 */
public class SurveyPermissionsManager {
	
    private static Logger logger = Logger.getLogger(SurveyPermissionsManager.class.getName());
    /**
     * RPC service that is used to retrieve the surveys from the database
     */
    private final SurveyRpcServiceAsync rpcService = GWT.create(SurveyRpcService.class);
    
    /** Flag to indicate if the survey context lock is acquired. */
    private boolean hasSurveyContextLock = false;
    
    /** Flag to indicate if the survey lock is acquired. */
    private boolean hasSurveyLock = false;
    
    /** Controls how frequently the survey lock timer is sent (in milliseconds). */
    private static final int RENEW_LOCK_DELAY_MS = 60000;
    
    /** The message that is displayed to the user if there are responses for the survey. */
    public static final String HAS_RESPONSES_MESSAGE = "There are responses to this survey in the database.  " + 
                                                       "The survey will not be editable if it has responses.  " + 
                                                       "<br/><br/> Do you wish to permanently delete the survey responses for this survey so you can edit it?"  + 
                                                       "<br/><font color='red'><b>If you choose to delete the survey responses, the action cannot be undone.</b></font>";
    
    public static final String NO_DEPENDENCIES = "none";
    

    //This timer will be used to continually renew the survey's lock
    //while we're editing the survey. We don't need to do anything
    //with the data the server returns.
    final AsyncCallback<Boolean> emptyCallback = new AsyncCallback<Boolean>(){
        @Override
        public void onFailure(Throwable arg0) {
        }
        @Override
        public void onSuccess(Boolean arg0) {
        }
    };    

    /** The cached survey context that the permissions manager uses to renew the lock. */ 
    private int surveyContextId;
    
    /** The cached survey that the permissions manager uses to renew the lock. */
    private Survey survey;    
    
    /** The timer used to renew the lock for the survey. */
    final Timer renewLockTimer = new Timer() {
        @Override
        public void run() {
            if (surveyContextId > 0 && survey != null) {
                logger.info("renewLockTimer() called for survey context (" + surveyContextId + ") and survey (" + survey.getId() + ")");
                renewSurveyLocks(surveyContextId, survey, emptyCallback);
            }            
        }
    };    
    
    /**
     * Constructor (default)
     */
    SurveyPermissionsManager() {  }
    
    /** 
     * Initializes the SurveyPermissionsManager.  This should be called once per load of a survey. 
     */
    public void initialize() {
        
        renewLockTimer.cancel();
        
        hasSurveyContextLock = false;
        hasSurveyLock = false;
        
        surveyContextId = 0;
        survey = null;
    }
    
    /**
     * Returns true if the survey is editable based on user permissions in the database.
     * If the user permissions are not set for the survey, then this will return false.
     * If the BypassSurveyPermissionsCheck flag is set to true and in desktop mode, always return true
     * 
     * @param surveyContextId - The survey context that the survey belongs to.
     * @param survey - The survey object to check.
     * @param callback used to notify the caller of the result from the survey.
     *  True if the survey (and survey context) are editable based on user permissions.  If the user does not have permissions, 
     *         then false is returned.
     */
    public void isSurveyEditable(int surveyContextId, final Survey survey, AsyncCallback<Boolean> callback) {
        
        if(GatClientUtility.getServerProperties().getBypassSurveyPermissionsCheck()){
        	logger.info("Bypass check is true and deployment mode is Desktop, allowing editing for survey");
        	callback.onSuccess(true);
        	return;
        }         
        
        if(survey.getId() == 0){
            callback.onFailure(new IllegalArgumentException("Unable to check permission on a survey with id of 0"));
        }else{
            rpcService.isSurveyEditable(surveyContextId, survey, GatClientUtility.getUserName(), callback);
        }
    }
    
    /**
     * Checks the user permissions for the survey only.
     * This is an internal method and should not be called externally since both the survey context and
     * survey permissions need to be checked at the same time.
     * 
     * @param survey The survey to check the permissions for.
     *        
     * @param callback used to notify the caller of the result from the survey.
     *  True if the survey is editable based on user permissions.  If the user does not have permissions for the
     *         survey, then false is returned.
     */
    @SuppressWarnings("unused")
    private void isSurveyEditable(final Survey survey, AsyncCallback<Boolean> callback) {

        if(GatClientUtility.getServerProperties().getBypassSurveyPermissionsCheck()){
            logger.info("Bypass check is true and deployment mode is Desktop, allowing editing for survey");
            callback.onSuccess(true);
        }         
        
        if(survey.getId() == 0){
            callback.onFailure(new IllegalArgumentException("Unable to check permission on a survey with id of 0"));
        }else{
            rpcService.isSurveyEditable(survey.getId(), GatClientUtility.getUserName(), callback);
        }
    }    
    
    /**
     * Acquires or renews the survey locks.  The survey locks are for the survey and the survey context.
     * There is an order that is done here first.  The survey context lock is retrieved first, followed by the 
     * survey.  The survey lock can be optional.  For example, in the case a new survey is being authored, the
     * survey context will be valid, but the survey will have an id of 0 (since it is not yet in the db).  In this case
     * the lock for the survey context is acquired, and then later a separate lock for the survey itself can be acquired once
     * the survey is saved.  Once the survey has a valid id, then this method can be used to acquire or simply renew the locks
     * for both the survey context and survey.  
     * 
     * @param surveyContextId The survey context to get/renew the lock.  Cannot be null and should not have an id of 0.
     * @param survey The survey to get/renew the lock (optional).  This cannot be null, but can have an id of 0.
     * @param acquire True if the lock is new and should be acquired, false if the lock should just be renewed.
     * @param callback Callback used to signal once the locks are retrieved.
     */
    private void acquireOrRenewSurveyLocks(final int surveyContextId, final Survey survey, final boolean acquire, final AsyncCallback<Boolean> callback) {
        
        if (surveyContextId > 0) {
            rpcService.lockSurveyContext(surveyContextId, acquire, new AsyncCallback<Boolean>() {

                @Override
                public void onFailure(Throwable t) {
                    callback.onFailure(t);
                }

                @Override
                public void onSuccess(Boolean lockSuccess) {
                    
                    if (lockSuccess) {
                        
                        if (acquire) {
                            logger.info("Acquired lock for survey context: " + surveyContextId);
                        }
                        hasSurveyContextLock = true;
                        
                        if (survey.getId() > 0) {
                            rpcService.lockSurvey(survey.getId(), acquire, new AsyncCallback<Boolean>() {

                                @Override
                                public void onFailure(Throwable surveyThrowable) {
                                    callback.onFailure(surveyThrowable);                                    
                                }

                                @Override
                                public void onSuccess(Boolean result) {
                                    
                                    if (result != null && result) {
                                        if (acquire) {
                                            logger.info("Acquired lock for survey " + survey.getId());
                                        }
                                        hasSurveyLock = true;
                                    }
                                    
                                    callback.onSuccess(result);
                                }
                                
                            });
                        } else {
                            callback.onSuccess(lockSuccess);
                        }
                    } else {
                        callback.onSuccess(lockSuccess);
                    }                    
                    
                }
            });
        } else {
            logger.severe("The surveycontext is not valid: " + surveyContextId + ".  Unable to acquire or renew the survey lock.");
            callback.onSuccess(false);
        }        
    }
    
    /**
     * Acquires a lock on a survey only.  This should primarily be used in cases where a new survey was saved to the database
     * and now has a valid id to lock on.  This assumes that the lock for the survey context that the survey belongs to is
     * already obtained. 
     * 
     * @param survey The survey to get the lock for.  This cannot be null, and should not have an id of 0.
     * @param callback Callback used to signal once the lock is retrieved.
     */
    public void acquireSurveyOnlyLock(final Survey survey, final AsyncCallback<Boolean> callback) {
        this.survey = survey;
        rpcService.lockSurvey(survey.getId(), true, new AsyncCallback<Boolean>() {

            @Override
            public void onFailure(Throwable surveyThrowable) {
                logger.info("lockSurvey() returned throwable failure: " + surveyThrowable.getMessage());                
                callback.onFailure(surveyThrowable);                
            }

            @Override
            public void onSuccess(Boolean result) {
                logger.info("lockSurvey() returned result: " + result);
                
                callback.onSuccess(result);
            }
        });
    }        
    
    /**
     * Acquires the survey locks.  The survey locks are for the survey and the survey context.
     * There is an order that is done here first.  The survey context lock is retrieved first, followed by the 
     * survey.  The survey lock can be optional.  For example, in the case a new survey is being authored, the
     * survey context will be valid, but the survey will have an id of 0 (since it is not yet in the db).  In this case
     * the lock for the survey context is acquired, and then later a separate lock for the survey itself can be acquired once
     * the survey is saved.  Once the survey has a valid id, then this method can be used to acquire or simply renew the locks
     * for both the survey context and survey.  
     * 
     * @param surveyContextId The survey context to get/renew the lock.  Cannot be null and should not have an id of 0.
     * @param survey The survey to get/renew the lock (optional).  This cannot be null, but can have an id of 0.
     * @param callback Callback used to signal once the locks are retrieved.
     */
    public void acquireSurveyLocks(final int surveyContextId, final Survey survey, final AsyncCallback<Boolean> callback) {
        this.surveyContextId = surveyContextId;
        this.survey = survey;
        acquireOrRenewSurveyLocks(surveyContextId, survey, true, new AsyncCallback<Boolean>() {

            @Override
            public void onFailure(Throwable t) {
                callback.onFailure(t);                
            }

            @Override
            public void onSuccess(Boolean result) {
                if (result) {
                    // schedule the renewal timer.
                    startRenewLockTimer();                    
                }
                
                callback.onSuccess(result);                
            }            
        });
    }
    
    /**
     * Renews the survey locks.  The survey locks are for the survey and the survey context.
     * There is an order that is done here first.  The survey context lock is retrieved first, followed by the 
     * survey.  The survey lock can be optional.  For example, in the case a new survey is being authored, the
     * survey context will be valid, but the survey will have an id of 0 (since it is not yet in the db).  In this case
     * the lock for the survey context is acquired, and then later a separate lock for the survey itself can be acquired once
     * the survey is saved.  Once the survey has a valid id, then this method can be used to acquire or simply renew the locks
     * for both the survey context and survey.  
     * 
     * @param surveyContextId The survey context to get/renew the lock.  Cannot be null and should not have an id of 0.
     * @param survey The survey to get/renew the lock (optional).  This cannot be null, but can have an id of 0.
     * @param callback Callback used to signal once the locks are retrieved.
     */
    public void renewSurveyLocks(final int surveyContextId, final Survey survey, final AsyncCallback<Boolean> callback) {
        acquireOrRenewSurveyLocks(surveyContextId, survey, false, callback);
    }
    
    /**
     * Returns true if the manager class has any locks for either the survey context or the survey.
     * 
     * @return True if the manager has locks for the survey context or the survey.
     */
    public boolean hasLocks() {
        return this.hasSurveyContextLock || this.hasSurveyLock;
    }
    
    /**
     * Releases the survey locks.
     * 
     * @param callback - Callback that is signaled once the locks are released.
     */
    public void releaseSurveyLocks(final AsyncCallback<Boolean> callback) {
        // Regardless of what happens on the server, when this method is called the survey permission manager will no longer have the locks.
        this.hasSurveyContextLock = false;
        this.hasSurveyLock = false;
        
        stopRenewLockTimer();
        
        if (surveyContextId > 0) {
            rpcService.unlockSurveyContext(surveyContextId, new AsyncCallback<Boolean>() {

                @Override
                public void onFailure(Throwable t) {
                    logger.severe("releaseSurveyLocks() - Server failure encountered unlocking the survey context: " + t.getMessage());
                    unlockSurvey(survey.getId(), callback);
                }

                @Override
                public void onSuccess(Boolean result) {
                    logger.info("releaseSurveyLocks() - returned result: " + result);
                    unlockSurvey(survey.getId(), callback);
                }
                
            });
        } else {
            logger.severe("The surveycontext is not valid: " + surveyContextId + ".  Unable to release the survey context lock.");
            unlockSurvey(survey.getId(), callback);
        }        
    }
    
    /**
     * Unlock the survey based on the survey id.  This method should only be called internally
     * after the survey context is unlocked.
     * 
     * @param surveyId - The id of the survey to unlock.
     * @param callback - The callback that is used to signal if the unlock was successful.  True is returned if successful, false otherwise.
     */
    private void unlockSurvey(final int surveyId, final AsyncCallback<Boolean> callback) {
        if (surveyId > 0) {
            rpcService.unlockSurvey(surveyId,  new AsyncCallback<Boolean>() {

                @Override
                public void onFailure(Throwable t) {
                    logger.severe("unlockSurvey() - server returned failure: " + t);
                    callback.onFailure(t);
                }

                @Override
                public void onSuccess(Boolean result) {
                    logger.info("unlockSurvey() - returned result: " + result);
                    callback.onSuccess(result);
                }
                
            });
        } else {
            logger.info("The survey id is not valid: " + surveyId + ".  Unable to release the survey lock.");
            callback.onSuccess(false);
        }
    }
    
    /**
     * Checks to see if the survey has any dependencies.  This returns data if the survey has dependencies and/or survey
     * responses.  There is a precedence of order done here.  Survey dependencies are checked first since if the survey
     * has any responses, that is a higher priority than survey context dependencies.  If the survey has no dependencies,
     * then the survey context is checked for dependencies.
     * 
     * @param surveyContextId - The survey context that the survey belongs to.
     * @param survey The survey to check the dependencies for.
     * @param callback Callback used to send the data back from the server with the dependencies if any.  The string "none" is returned
     *                 if there are no dependencies.
     */
    public void getSurveyDependencies(final int surveyContextId, final Survey survey, final AsyncCallback<String> callback) {

        // Check the survey dependencies first, since the priority is on the survey.  If the survey has responses,
        // that result takes precedence.  If there are no dependencies on the survey, check the survey context.
        if (survey.getId() > 0) {
            rpcService.getSurveyDependencies(survey.getId(), surveyContextId, new AsyncCallback<SurveyDependencies>() {

                @Override
                public void onFailure(Throwable surveyThrowable) {
                    logger.severe("getSurveyDependencies() rpc returned failure while fetching the survey dependencies.");
                    callback.onFailure(surveyThrowable);                    
                }

                @Override
                public void onSuccess(SurveyDependencies surveyResult) {
                     if (surveyResult != null) {
                         if (!surveyResult.hasResponses() && surveyResult.getDependencies() == null) {
                             logger.info("getSurveyDependencies() rpc returned no dependencies for the survey.");
                             checkSurveyContextDependencies(surveyContextId, callback);
                             
                         } else {
                             logger.info("getSurveyDependencies() rpc returned that the survey has dependencies.");
                             
                             String message = "";
                             if (surveyResult.hasResponses()) {
                                 message = HAS_RESPONSES_MESSAGE;
                             } else {
                                 message = surveyResult.getDependencies();
                             }

                             callback.onSuccess(message);
                         }
                     } else {
                         logger.info("getSurveyDependencies() rpc returned null survey result.");
                         callback.onSuccess(null);
                     }                    
                }                
            });
        } else {
            logger.info("getSurveyDependencies() rpc ignoring fetching survey dependencies since the survey id is not valid. ");
            checkSurveyContextDependencies(surveyContextId, callback);
        }       
    }
    
    /**
     * Checks to see if the survey context has any dependencies.  If the survey context has no dependencies,
     * then the survey context is checked for dependencies.
     * 
     * @param surveyContextId - The survey context that the survey belongs to.
     * @param callback Callback used to send the data back from the server with the dependencies if any.  The string "none" is returned
     *                 if there are no dependencies.
     */
    private void checkSurveyContextDependencies(final int surveyContextId, final AsyncCallback<String> callback) {
        if (surveyContextId > 0) {
            rpcService.getSurveyContextDependencies(surveyContextId, new AsyncCallback<String>() {

                @Override
                public void onFailure(Throwable t) {
                     logger.severe("getSurveyContextDependencies() rpc returned failure while fetching the survey context dependencies.");
                     callback.onFailure(t);                    
                }

                @Override
                public void onSuccess(String result) {
                    if (result != null) {
                        if (result.equals(NO_DEPENDENCIES)) {
                            callback.onSuccess(NO_DEPENDENCIES);
                            
                        } else {
                            logger.info("getSurveyContextDependencies() rpc found dependencies for the survey context.  Returning result: " + result);
                            callback.onSuccess(result);
                        }
                    } else {
                        logger.info("getSurveyContextDependencies() rpc found null result when fetching the survey context dependencies.  Returning result: " + result);
                        callback.onSuccess(result);
                    }                    
                }
                
            });
        } else {
            logger.severe("The surveycontext is not valid: " + surveyContextId + ".  Unable to fetch the survey context dependencies.");
            callback.onSuccess(null);
        }
    }
    
    /**
     * Releases the survey locks but ignores the callback.  Can be used in cases where the locks should be released and the call
     * doesn't need to get the response back from the server.
     */
    public void releaseLocksIgnoreCallback() {
        this.releaseSurveyLocks(new AsyncCallback<Boolean>() {

            @Override
            public void onFailure(Throwable arg0) {
                // Do nothing                
            }

            @Override
            public void onSuccess(Boolean arg0) {
                // Do nothing                
            }
            
        });
    }
    
    /**
     * Starts the renew lock timer.
     */
    private void startRenewLockTimer() {
        renewLockTimer.scheduleRepeating(RENEW_LOCK_DELAY_MS);
    }    
    
    /**
     * Stops the renew lock timer.
     */
    private void stopRenewLockTimer() {
        renewLockTimer.cancel();
    }	
}
