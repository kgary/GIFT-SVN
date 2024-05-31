/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.util;


import java.util.logging.Logger;



/**
 * The AsyncOperationManager class is responsible for tracking a pending async operation.  An async operation
 * here is defined as a 'gwt' dispatch call from the web client to the web server.  The AsyncOperationManager class
 * is used to track 1) what operation is in progress and 2) when the operation completes.  
 * Only one async operation is supported at a time.  The caller is responsible for 'setting' which
 * async operation is in progress and then clearing the operation once it is completed.
 * 
 * In the future, this manager class could be extended to allow execution of the operations directly and actually be 
 * responsible for sending and getting the results for the async operation, but as of now it is not needed.
 * 
 * @author nblomberg
 *
 */
public class AsyncOperationManager {
	
    /** Instance of the logger */
    private static Logger logger = Logger.getLogger(AsyncOperationManager.class.getName());
    
    /** Label for "copy" operation */
    private static final String LABEL_COPY = "Copy";
    /** Label for "move" operation */
    private static final String LABEL_MOVE = "Move";
    /** Label for "delete" operation */
    private static final String LABEL_DELETE = "Delete";
    /** Label for "no" operation */
    private static final String LABEL_NONE = "None";
    
    /** Enum continaing the various operations that are supported */
    public enum AsyncOperation {
        COPY,
        MOVE,
        DELETE,
        NONE,
    }
    
   
    /** The current async operation (if any) */
    private AsyncOperation asyncOp = AsyncOperation.NONE;
    
    /**
     * Constructor
     */
    public AsyncOperationManager() {
        logger.info("AsyncOperationManager()");
        
        init();
    }

    /**
     * Initializes the async operation manager
     */
    public void init() {
        resetOperation();
    }
    
    /**
     * Signals to the AsyncOperationManager that there is a pending async operation
     * in progress.  Only one async operation is allowed at a given time.  The caller
     * should ensure that the resetOperation() function has already been called for 
     * any previous operation prior to setting a new pending operation.
     * 
     * @param op - The {@link AsyncOperation} that is in progress.
     */
    public void setAsyncOperation(AsyncOperation op) {
        
        // Log a warning if someone tries to set a new operation if there is already one in progress.  It's expected
        // that each operation is reset with resetOperation() before calling this function.
        if (asyncOp != AsyncOperation.NONE) {
            logger.warning("An operation is already in progress and should be cleared using resetOperation() before setting a new one.");
        }
        asyncOp = op;
    }
    
    /**
     * Gets the current async operation that is in progress. If no operation is in progress
     * then AsyncOperation.NONE is returned.
     * 
     * @return AsyncOperation - The enum {@link AsyncOperation} for the current operation in progress.
     */
    public AsyncOperation getAsyncOperation() {
        return asyncOp;
    }
    
    /**
     * Returns true if there is any async operation currently in progress.
     *   
     * @return True if there is an async operation in progress.  False otherwise.
     */
    public boolean isInProgress() {
        boolean inProgress = false;
        
        if (asyncOp != AsyncOperation.NONE) {
            inProgress = true;
        }
        
        return inProgress;
    }
    
    
    /**
     * Returns the current async operation value as a string that can be used as a label for 
     * displaying information to the user.
     * 
     * @return String - String value of the current async operation.
     */
    public String getAsyncOperationLabel() {
        String operationStr = LABEL_NONE;
        switch (asyncOp) {
        case COPY:
            operationStr = LABEL_COPY;
            break;
        case DELETE:
            operationStr = LABEL_DELETE;
            break;
        case MOVE:
            operationStr = LABEL_MOVE;
            break;
        case NONE:
            // intentional fallthrough
        default:
            operationStr = LABEL_NONE;
            break;
        
        }
        
        return operationStr;
    }
    

    
    /**
     * Resets the async operation.  This should be used after a gwt rpc has returned either
     * success or failure to let the AsyncOperationManager know that the async operation is 
     * completed.  
     * 
     */
    public void resetOperation() {
        asyncOp = AsyncOperation.NONE;
    }
    
    
    
    
}
