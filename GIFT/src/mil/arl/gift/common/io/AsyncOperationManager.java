/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.io;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The AsyncOperationManager is a singleton class that is responsible for starting async operations from a servlet.
 * This allows an incoming servlet request to spawn an async operation in a separate thread which will be completed later.
 * Typically the client then will need to poll for the result of the operation once it has completed.  For now this class 
 * manages the starting of the async operations from the servlet.
 * 
 * @author nblomberg
 *
 */
public class AsyncOperationManager {
    
    private static Logger logger = LoggerFactory.getLogger(AsyncOperationManager.class.getName());
    
    /** singleton instance of this class */
    private static AsyncOperationManager instance = null;
    /**
     * Return the singleton instance of this class
     *
     * @return CourseLaunchManager
     */
    public static AsyncOperationManager getInstance() {

        if (instance == null) {
            instance = new AsyncOperationManager();
        }

        return instance;
    }

    /**
     * Class constructor
     */
    private AsyncOperationManager() {
       
    }
    
    /**
     * Starts an asyncronous operation (which will be started in a separate thread).
     * 
     * @param name The name of the async operation (will become the name of the thread).
     * @param asyncOp The async operation to be started.
     */
    public void startAsyncOperation(String name, Runnable asyncOp) {
        logger.info("startAsyncOperation() called with name=" + name);
        Thread thread = new Thread(asyncOp);
        thread.setName(name);
        thread.start();
    }

}
