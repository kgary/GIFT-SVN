/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.learner.common.rapidminer;


import com.rapidminer.operator.IOContainer;

/**
 * This is the interface implemented by RapidMiner data models.  Data models contain
 * data that is used as input to a RapidMiner process and model.
 * 
 * @author mhoffman
 *
 */
public interface DataModel {
    
    /**
     * Return a wrapper that encapsulates the data model's data.
     * This wrapper is used as input to a RapidMiner process and model.
     * 
     * @return IOContainer
     */
    public IOContainer getIOContainer();
    
    
    /**
     * Adds a training application state data to the data model.  Essentially this will examine the 
     * training app state and push data (like bloodvolume, etc) into the data model.
     * @param trainingAppState - The training application state data do add to the data model.  This should not be null.
     * @throws Exception - Throws any exception that may be found while processing the data (could be from RapidMiner or the XMLRpc server)
     */
    public void addState(Object trainingAppState) throws Exception;

    
    /**
     * Clears all states known to the data model.  
     * This should be used to reset any state data or table data that is held in memory for the data model.
     */
    public void clearStates(); 

}
