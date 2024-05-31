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
import java.util.Iterator;
import java.util.logging.LogManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import mil.arl.gift.common.PackageUtil;

import com.rapidminer.RapidMiner;
import com.rapidminer.RapidMiner.ExecutionMode;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.tools.XMLException;
import com.rapidminer.tools.ParameterService;


/**
 * The primary class for running & processing RapidMiner.  External details on this
 * can be found at:  http://rapid-i.com/wiki/index.php?title=Integrating_RapidMiner_into_your_application
 * 
 * RapidMiner integration with GIFT allows for the ability to feed in TC3 game state data or kinect sensor data
 * and process that data using a RapidMiner model to output a 'confidence' value of a particular learner state.
 * The ability to use RapidMiner allows for external models without the requirement to adjust the code.  The goal
 * is that the rapidminer model can perform the heavy lifting of processing the raw data coming in from the training
 * application or the sensor data.  For GIFT the end result is currently that a confidence value is returned from RapidMiner
 * which indicates how 'confident' that the learner is in a particular state (anxious, frustrated, etc).  
 * 
 * @author mhoffman
 *
 */
public class RapidMinerInterface {
	

    // The anxious learner state is tied to the sample rapidminer model that is expecting TC3 game state data.  This can be adjusted as needed or 
    // additional learner states can have their own rapid miner process file.
    public static final File ANXIOUS_PROCESS_FILE = new File(PackageUtil.getConfiguration() + "/learner/model/RapidMiner/Sample_TC3.process.InApply.xml");
    
    // The engaged/concentration state is tied to the sample kinect rapid miner model that is expecting Kinect sensor data.  This can be adjusted as needed
    // or additional learner states can be added to have their own rapid miner process file.
    public static final File ENG_CONCENTRATION_PROCESS_FILE = new File(PackageUtil.getConfiguration() + "/learner/model/RapidMiner/Sample_Kinect.process.InApply.xml");

    
    // The remaining supported learner states are currently setup to use the sample TC3 models as an example only.  This should be changed
    // to actual models as needed for the learner state.
    public static final File BORED_PROCESS_FILE = new File(PackageUtil.getConfiguration() + "/learner/model/RapidMiner/Sample_TC3.process.InApply.xml");
    public static final File CONFUSION_PROCESS_FILE = new File(PackageUtil.getConfiguration() + "/learner/model/RapidMiner/Sample_TC3.process.InApply.xml");
    public static final File FRUSTRATED_PROCESS = new File(PackageUtil.getConfiguration() + "/learner/model/RapidMiner/Sample_TC3.process.InApply.xml");
    public static final File OFF_TASK_PROCESS_FILE = new File(PackageUtil.getConfiguration() + "/learner/model/RapidMiner/Sample_TC3.process.InApply.xml");
    public static final File SURPRISE_PROCESS_FILE = new File(PackageUtil.getConfiguration() + "/learner/model/RapidMiner/Sample_TC3.process.InApply.xml");
    
    private static final String PLUGIN_DIRECTORY = "external" + File.separator + "rapidminer" + File.separator + "lib" + File.separator + "plugins";
    
    /**
     * This is required to be called before RapidMiner API is used, otherwise an exception
     * will be thrown.
     */
    static{
    	
    	/*
    	 * Set up a bridge between RapidMiner's use of java.util.logging (JUL) and GIFT's use of SLF4J so that all logger output 
    	 * from RapidMiner is captured by SLF4J and rerouted to the appropriate learner log file.
    	 */
    	LogManager.getLogManager().reset();
    	
    	SLF4JBridgeHandler.removeHandlersForRootLogger();
    	SLF4JBridgeHandler.install();
    	
    	/*
    	 * Set JUL log level to FINEST so that the SLF4J bridge captures all the output from JUL loggers. The actual log levels that
    	 * will be used in the learner log are defined in config/learner/learner.log4j.properties.
    	 */
    	java.util.logging.Logger.getLogger("global").setLevel(java.util.logging.Level.FINEST);
    	    	
    	/* Set RapidMiner's execution mode so that it does not load extensions from its own directory */
    	RapidMiner.setExecutionMode(ExecutionMode.EMBEDDED_WITHOUT_UI);
    	
    	/* Set RapidMiner's properties so that it uses GIFT's external library to search for plugins */
    	ParameterService.setParameterValue(RapidMiner.PROPERTY_RAPIDMINER_INIT_PLUGINS, "true");
    	ParameterService.setParameterValue(RapidMiner.PROPERTY_RAPIDMINER_INIT_PLUGINS_LOCATION, PLUGIN_DIRECTORY);
    	
    	RapidMiner.init();
    }
	
	/**
	 * This class is the API to a RapidMiner process.  It provides a means to input
	 * data into the process.  The process must return a result that contains a confidence
	 * value.   
	 * 
	 * @author mhoffman
	 *
	 */
	public static class RapidMinerProcess{
	    
	    /** instance of the logger */
	    private static Logger logger = LoggerFactory.getLogger(RapidMinerProcess.class);
	    
	    /**
	     * used to find the confidence value in the output of the process
	     * Note: based on RapidMiner v5.3 output format
	     */
	    private final String CONFIDENCE_KEY;
	    
	    /** the RapidMiner exported process XML formatted file */
	    private File processFile;
	    
	    /** the RapidMiner process created from the Process file */
	    private com.rapidminer.Process process;
	    
	    /**
	     * Create the RapidMiner process from the Process file.
	     * 
	     * @param processFile the RapidMiner exported process XML formatted file
	     * @param outputKey the RapidMiner output key
	     * @throws IOException if there was a problem (e.g. unable to read the file) with the process file
	     * @throws XMLException if there was a problem (e.g. XML format) with the process file
	     */
	    public RapidMinerProcess(File processFile, String outputKey) throws IOException, XMLException{
	        
	        if(processFile == null || !processFile.exists()){
	            throw new IllegalArgumentException("The process file of "+processFile+" doesn't exist.");
	        }
	        
	        this.processFile = processFile;
	        
	        process = new com.rapidminer.Process(this.processFile);
	        
	        CONFIDENCE_KEY = outputKey;
	    }
	    
	    /**
	     * Run the process given the input data provided.
	     *  
	     * @param input the input data to process
	     * @return double the confidence value returned from the process.  Currently a value of 0.0
	     * means that either the process returned a confidence of 0.0 -OR- no confidence value was found.
	     * @throws OperatorException if there was a problem running the process
	     */
	    public synchronized double runProcess(IOContainer input) throws OperatorException{	
	        
	        IOContainer output = process.run(input);
	        
	        return getConfidence(output);
	    }
	    
	    /**
	     * Search the process output for the confidence value.
	     * 
	     * @param result the process output to search
	     * @return double the confidence value. Currently a value of 0.0
         * means that either the process returned a confidence of 0.0 -OR- no confidence value was found.
	     */
	    private double getConfidence(IOContainer result){
	        
	        
            if (result.getElementAt(0) instanceof ExampleSet) {
                ExampleSet resultSet = (ExampleSet) result.getElementAt(0);
                
                logger.trace("ResultSet = " + resultSet);
                for (Example example : resultSet) {
                    Iterator<Attribute> allAtts = example.getAttributes().allAttributes();
                    while (allAtts.hasNext()) {
                        Attribute a = allAtts.next();
                        
                        //find the confidence column in the output
                        if(a.getName().equalsIgnoreCase(CONFIDENCE_KEY)){
                            return example.getValue(a);
                        }

                    }
                }
                
                logger.warn("Unable to find a confidence value in the process output of "+resultSet);
            }
	        
	        return 0.0;
	    }
	    
	    @Override
	    public String toString(){
	        
	        StringBuffer sb = new StringBuffer();
	        sb.append("[RapidMinerProcess: ");
	        sb.append("processFile = ").append(processFile);
	        sb.append("]");
	        return sb.toString();
	    }

	}
}
