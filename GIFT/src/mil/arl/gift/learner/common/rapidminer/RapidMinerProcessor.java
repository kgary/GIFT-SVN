/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.learner.common.rapidminer;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rapidminer.operator.OperatorException;
import com.rapidminer.tools.XMLException;
import mil.arl.gift.common.enums.AbstractEnum;
import mil.arl.gift.common.enums.ArousalLevelEnum;
import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;
import mil.arl.gift.common.state.LearnerStateAttribute;
import mil.arl.gift.learner.common.rapidminer.RapidMinerInterface.RapidMinerProcess;

/**
 * The RapidMiner Processor class is responsible for processing data that will be fed into rapidminer and updating the 
 * learnerstate based on the results from the rapidminer model.  It contains the core process function to take in 
 * state data (coming from sensors or training application).  The state data is updated (could be appended to a table or 
 * it could be 'preprocessed' via a python script via xmlrpc server.  As state data is updated, every 20 seconds, the rapidminer
 * process is called with the current set of data that has been received over the 20 second window.  The output of the rapidminer
 * process should return a confidence value (high, low) on the learner state.  The confidence value is then examined and if it has
 * changed, the learner state is updated to the new value.
 * 
 * @author nblomberg
 *
 */
public class RapidMinerProcessor  {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(RapidMinerProcessor.class);

    /** the learner's current state */
    private LearnerStateAttribute state;
    
    private LearnerStateAttributeNameEnum stateAttribute; 
    
    
    // $TODO$ nblomberg
    // Arousal Level enum should change to a different enum type here perhaps.
    private static final AbstractEnum LOW_VALUE = ArousalLevelEnum.LOW;
    private static final AbstractEnum HIGH_VALUE = ArousalLevelEnum.HIGH;
    private static final AbstractEnum UKNOWN_VALUE = ArousalLevelEnum.UNKNOWN;
    
    /**
     * the interface to RapidMiner - used to run a process with a model on a dataset
     */
    RapidMinerProcess rapidMiner;
    
    /** 
     * contains TC3 sim data from a training app game state and is used to create a data table
     * for RapidMiner
      */
    private DataModel dataModel;
    
    /** the confidence value must be greater than this to change to a 'higher' Anxious value */
    private static final double CONFIDENCE_THRESHOLD = 0.5;
    
    /** number of milliseconds that must elapse before providing the data model to RapidMiner for processing */
    protected static final long WINDOW_SIZE_MS = 20000; 
    
    /** the starting epoch time of the current data window */
    protected long startOfWindow = 0;
    
    /**
     * Constructor for the rapidminer processor class. 
     * 
     * @param processFile - the external RapidMiner process file that should be called when executing rapidminer.
     * @param attribute - The learnerstate attributename enum that the process will affect (ANXIOUS, FRUSTRATED, etc)
     * @param model - The data model that will be updated as new state data is received.
     * @param outputKey - The key value that is expected to be returned from the RapidMiner model.  
     * @throws IOException  if there was a problem (e.g. unable to read the file) with the process file
     * @throws XMLException if there was a problem (e.g. XML format) with the process file
     */
    public RapidMinerProcessor(File processFile, LearnerStateAttributeNameEnum attribute, DataModel model, String outputKey) throws IOException, XMLException{
        
        stateAttribute = attribute;
        rapidMiner = new RapidMinerProcess(processFile, outputKey);
        dataModel = model;
    }
    
    
    /**
     * The main processing function for the rapidminer pipeline.
     * State data is received and fed into the data model.  This stateData could come from TC3 (training application) or
     * a sensor (like Kinect).  State data is fed into the model and on a 20 second interval, the rapidminer process will be called
     * with the data that it has received over the 20 second window.   The output of the rapidminer process is used to 
     * 
     * @param stateData - the new state data that is being received (either from a sensor like Kinect or a training application like TC3).  It should not be null.
     * @return - true if the learnerstate is updated.
     */
    public boolean updateState(Object stateData){
        
        logger.trace("Received state for " + this.getClass().getName());
        boolean updated = false;

        long currentTime = System.currentTimeMillis();
       
        //add the state's attributes to the data model,
        //if the state doesn't have any matching attributes for the data model then nothing
        //relevant will be added 
        try {
            dataModel.addState(stateData);
        } catch (Exception e) {
            logger.error("Failed to add the state contents to "+dataModel+" - "+stateData+".", e);
        }
        
        //collect 20sec interval, then apply model by running process
        if(startOfWindow == 0){
            startOfWindow = currentTime;
        }else if((currentTime - startOfWindow) > WINDOW_SIZE_MS){
        
            try {
                
                // Make sure the data model has data in it before we send it to rapidminer.
                if (dataModel.getIOContainer() != null) {
                    double confidence = rapidMiner.runProcess(dataModel.getIOContainer());
                    
                    AbstractEnum aLevel = null;
                    
                    if(confidence > CONFIDENCE_THRESHOLD){
                        aLevel = HIGH_VALUE;
                    }else{
                        aLevel = LOW_VALUE;
                    }
                    
                    logger.trace(stateAttribute.getDisplayName() + " Confidence = " + confidence + " -- aLevel = " + aLevel);
                    
                    //update state (if necessary)
                    if(state == null || aLevel != state.getShortTerm()){
                        state = new LearnerStateAttribute(stateAttribute, aLevel, UKNOWN_VALUE, UKNOWN_VALUE);
                        updated = true;
                        
                        logger.info("Current State was updated to "+state+".");
                    }
                    
                }
                
            } catch (OperatorException e) {
                logger.error("Failed to run RapidMiner process", e);
            } catch (Exception e) {
                logger.error("Unhandled exception caught while trying to updateState for rapidminer classifier: " + this.getClass().getName() + ". ", e);
            }
            
            //reset
            dataModel.clearStates();
            startOfWindow = 0;
        }

        
        return updated;
    }


    /**
     * Accessor to get the current learner state.
     * @return the current learner state (should not be null).
     */
    public Object getState() {
        return state;
    }


    /**
     * Accessor to get the current learnerstate attribute enum.  For example if this processor is configured
     * to use the ANXIOUS learner state, this will return the learnerstateattributenameenum for ANXIOUS learner state.
     * 
     * @return the learnerstate attribute name enum (ANXIOUS, FRUSTRATED, etc) that the processor is affecting.
     */
    public LearnerStateAttributeNameEnum getAttribute() {
        return stateAttribute;
    }


    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[RapidMinerProcessor:");
        sb.append(" current state = ").append(getState());
        sb.append(", windowSizeMs = ").append(WINDOW_SIZE_MS);
        sb.append(", startOfWindow = ").append(startOfWindow);
        sb.append("]");
        return sb.toString();
    }
}
