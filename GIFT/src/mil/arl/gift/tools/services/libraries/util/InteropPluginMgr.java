/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.services.libraries.util;

import generated.gateway.InteropInterfaceConfig;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.enums.TrainingApplicationEnum;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileValidationException;
import mil.arl.gift.gateway.GatewayModuleProperties;
import mil.arl.gift.gateway.interop.AbstractInteropInterface;
import mil.arl.gift.gateway.interop.AbstractInteropInterface.InteractionMode;
import mil.arl.gift.gateway.interop.InteropConfigFileHandler;

/**
 * This class is used to manage gateway interop plugin classes outside of the gateway module.
 * These classes are used to communicate with external training applications.
 * 
 * @author mhoffman
 *
 */
public class InteropPluginMgr {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(InteropPluginMgr.class);
    
    /** the interop configuration XML file to parse */
    private static final String INTEROP_CONFIG_FILENAME = GatewayModuleProperties.getInstance().getInteropConfig();

    private Map<TrainingApplicationEnum, List<InteropInterfaceConfig>> trainingAppToInteropConfigs;
    
    /** parsed the interop config file, used to retrieve XML objects and instantiate/configure interop classes */
    private InteropConfigFileHandler interopFileHandler;

    /**
     * Parses the interop configuration file, instantiating but not configuring any of the
     * available interop classes defined in that XML file.
     * 
     * @throws FileValidationException if there was a validation issue with the interop configuration file
     * @throws DetailedException if there was a problem instantiating or configuring an interop plugin class based on the
     * optional list of interop classes (or all interop classes if not provided) and the configuration objects from
     * the interop configuration file.
     */
    public InteropPluginMgr() throws FileValidationException, DetailedException{
        processConfigFile();
    }
    
    /**
     * Parses the interop configuration file, instantiating but not configuring any of the
     * available interop classes defined in that XML file.
     * 
     * @throws FileValidationException if there was a validation issue with the interop configuration file
     * @throws DetailedException if there was a problem instantiating or configuring an interop plugin class based on the
     * optional list of interop classes (or all interop classes if not provided) and the configuration objects from
     * the interop configuration file.
     */
    private void processConfigFile() throws FileValidationException, DetailedException{

        //read in configurations
        interopFileHandler = new InteropConfigFileHandler(INTEROP_CONFIG_FILENAME);
        
        //Note: this will prevent calling configure on each interop class but still instantiate the class
        //      add have it added to the training app map
        interopFileHandler.configureByImplementationClassName(new ArrayList<String>(0));

        //build training app type map
        trainingAppToInteropConfigs = interopFileHandler.getTrainingAppMap();
    }
    
    /**
     * Return the collection of interop interface config objects from the interop config file.
     * 
     * This is useful configuring connection(s) parameters before connecting to a training application.
     * 
     * @param trainingAppEnum the type of training application to retrieve interop configuration objects for
     * @return collection of interop interface config objects for that training application.  Will be null if that
     * training application type is not properly represented in {@link TrainingAppPluginUtil}
     */
    public List<InteropInterfaceConfig> getTrainingAppInterops(TrainingApplicationEnum trainingAppEnum){
        return trainingAppToInteropConfigs.get(trainingAppEnum);        
    }
    
    /**
     * Return information about the scenarios available in the external application that the configured
     * interop interface class(es) is communicating with.
     * 
     * This is useful for authoring tools that want to interact with the external application's scenario editor and
     * allow the GIFT course author to associate a scenario with a GIFT course object.
     * 
     * @return contains metadata about the scenarios in the application (e.g. scenario names and descriptions) 
     * A return value of null indicates that none of the interop plugin class support this method.
     * @throws DetailedException if there was a problem retrieving information about the available scenarios
     */
    public Serializable getScenarios() throws DetailedException{
        
        for(AbstractInteropInterface interop : interopFileHandler.getInterops().values()){  
            
            try{
                Serializable value = interop.getScenarios();
                
                if(value != null){
                    return value;
                }
            }catch(Exception e){
                logger.error("Caught exception while trying to get the scenario list through the interop plugin of "+interop+".", e);
                throw e;
            }
            
        }
        
        return null;
    }
    
    /**
     * Return information about the currently loaded scenario in the external application.
     * 
     * This is useful for determining whether the scenario is loaded as well as retrieve information
     * about the current external scenario of which could be shown to the user through a GIFT user interface.
     * 
     * @return contains metadata about the currently loaded scenario.  A return value of null indicates that the configured
     * interop plugin class(es) don't support this method or a scenario is not loaded.
     * @throws DetailedException if there was a problem retrieving the information about the current scenario.
     */
    public Serializable getCurrentScenarioMetadata() throws DetailedException{
        
        for(AbstractInteropInterface interop : interopFileHandler.getInterops().values()){  
            
            try{
                Serializable value = interop.getCurrentScenarioMetadata();
                if(value != null){
                    return value;
                }
            }catch(Exception e){
                logger.error("Caught exception while trying to get the current scenario metadata through the interop plugin of "+interop+".", e);
                throw e;
            }
            
        }
        
        return null;
    }
    
    /**
     * Return information about the selectable objects for the current scenario in the external application that the configured
     * interop interface class is communicating with.
     * 
     * This is useful for authoring tools that want to interact with those objects and allow the GIFT course
     * author to associate GIFT assessments with those objects.
     * 
     * @return contains metadata about the objects that are selectable in the application (e.g. object name, location, type)
     * A return value of null indicates that the none of the interop plugin class support this method.
     * @throws DetailedException if there was a problem retrieving information about the selectable objects
     */
    public Serializable getSelectableObjects() throws DetailedException{
        
        for(AbstractInteropInterface interop : interopFileHandler.getInterops().values()){  
            
            try{
                Serializable value = interop.getSelectableObjects();
                
                if(value != null){
                    return value;
                }
            }catch(Exception e){
                logger.error("Caught exception while trying to get the selectable objects through the interop plugin of "+interop+".", e);
                throw e;
            }
        }
        
        return null;
    }
    
    /**
     * Notify the external application to select the object in that application identified by the
     * provided identifier.  The external application may highlight that object in its user interface.
     * 
     * This is useful for when an author or learner is selecting an object in GIFT that is associated with
     * an object in the external application.  
     * 
     * @param objectIdentifier a unique identifier that will help the external application know which object
     * is being selected.  
     * @throws DetailedException if there was a problem communicating the selection event 
     */
    public void selectObject(Serializable objectIdentifier) throws DetailedException{
        
        for(AbstractInteropInterface interop : interopFileHandler.getInterops().values()){  
            
            try{
                interop.selectObject(objectIdentifier);
            }catch(Exception e){
                logger.error("Caught exception while trying to select the object identified by '"+objectIdentifier+"' through the interop plugin of "+interop+".", e);
                throw e;
            }
        }
    }
    
    /**
     * Uses the configured interop plugin implementation(s) to load a scenario in the external application that it is
     * communicating with.
     * 
     * @param scenarioIdentifier uniquely identifies the scenario in the external application.  This is normally
     * authored in the GIFT course.  A null value indicates that any loaded scenario should be cleared from view 
     * which might mean that the external application should return to the main menu.
     * @throws DetailedException if there was a problem loading the scenario.
     */
    public void loadScenario(String scenarioIdentifier) throws DetailedException{
        
        for(AbstractInteropInterface interop : interopFileHandler.getInterops().values()){  
            
            try{
                interop.loadScenario(scenarioIdentifier);
            }catch(Exception e){
                logger.error("Caught exception while trying to load a scenario using the scenario identifier '"+scenarioIdentifier+"' through the interop plugin of "+interop+".", e);
                throw e;
            }
        }
    }
    
    /**
     * Exports the current external training application scenario into a single file that can 
     * be saved in the GIFT course folder.  This file can then be used by this interop plugin implementation
     * to load the scenario in the external training application during GIFT course execution.
     * 
     * @param exportFolder the folder to export the scenario file(s) to.  The folder must exist.
     * @return the file that contains the scenario.  This file might be a zip that contains many files/folders.  A null
     * value could indicate that the external application has no file representation of the scenario that can be accessed
     * or that the configured interop plugin class(es) don't support this method.
     * @throws DetailedException if there was a problem exporting the scenario from the external training application.
     */
    public File exportScenario(File exportFolder) throws DetailedException{
        
        for(AbstractInteropInterface interop : interopFileHandler.getInterops().values()){  

            try{
                File file = interop.exportScenario(exportFolder);
                
                if(file != null){
                    return file;
                }
            }catch(Exception e){
                logger.error("Caught exception while trying to export a scenario through the interop plugin of "+interop+".", e);
                throw e;
            }
        }
        
        return null;
    }
    
    /**
     * Configures and enables the interop interfaces defined by the configuration objects
     * provided.
     * 
     * @param configs collection of interop configuration objects to use to configure and enable interop plugin classes.
     * @throws DetailedException if there was a problem instantiating or configuring an interop plugin class based on the
     * configuration objects provided.
     */
    public void connectInterops(Collection<InteropInterfaceConfig> configs) throws DetailedException{
        
        if(configs == null){
            throw new IllegalArgumentException("The configurations list can't be null.");
        }
        
        //instantiates each interop class and calls configure
        interopFileHandler.configureByInteropInterfaceConfigs(configs);
        
        //set those interops to author mode
        for(AbstractInteropInterface interop : interopFileHandler.getInterops().values()){  
            
            try{
                interop.setInteractionMode(InteractionMode.Author);
            }catch(Exception e){
                logger.error("Caught exception while trying to set the mode to "+InteractionMode.Author+" for the interop plugin of "+interop+".", e);
                throw e;
            }
        }
        
        //enable those interops for use
        for(AbstractInteropInterface interop : interopFileHandler.getInterops().values()){  
            
            try{
                interop.setEnabled(true);
            }catch(Exception e){
                logger.error("Caught exception while trying to enable the interop plugin of "+interop+".  Cleaning up all interops since one failed.", e);
                disconnectCurrentInterops();
                throw e;
            }
        }

    }
    
    /**
     * Disables and cleanups any interop connections that had been configured and enabled.
     * If any of the interop plugin classes have an issue an attempt will be made to disconnect any other interop
     * classes before throwing an exception that contains a summary of issues.
     * 
     * @throws DetailedException if there was a problem enabling or cleaning up any interop plugin.
     */
    public void disconnectCurrentInterops() throws DetailedException{
        
        StringBuilder errorMsg = new StringBuilder();
        
        //unload any loaded scenario
        for(AbstractInteropInterface interop : interopFileHandler.getInterops().values()){  
            
            try{
                interop.loadScenario(null);
            }catch(Exception e){
                logger.error("Caught exception while trying to unload/clear any loaded scenario using the interop plugin of "+interop+". Continuing to unload the scenario using other known interops anyway.", e);
                errorMsg.append("\nFailed to unload the scenario using the interop plugin named '").append(interop.getName()).append("' because of the exception that reads: '").append(e.getMessage()).append("'.");
            }
        }
        
        //disable
        for(AbstractInteropInterface interop : interopFileHandler.getInterops().values()){    
            
            try{
                interop.setEnabled(false);
            }catch(Exception e){
                logger.error("Caught exception while trying to disable the interop plugin of "+interop+". Continuing to disable other known interops anyway.", e);
                errorMsg.append("\nFailed to disable the interop plugin named '").append(interop.getName()).append("' because of the exception that reads: '").append(e.getMessage()).append("'.");
            }
        }
        
        //cleanup
        for(AbstractInteropInterface interop : interopFileHandler.getInterops().values()){  
            
            try{
                interop.cleanup();
            }catch(Exception e){
                logger.error("Caught exception while trying to cleanup the interop plugin of "+interop+". Continuing to cleanup other known interops anyway.", e);
                errorMsg.append("\nFailed to cleanup the interop plugin named '").append(interop.getName()).append("' because of the exception that reads: '").append(e.getMessage()).append("'.");
            }
        }
        
        if(errorMsg.length() > 0){
            throw new DetailedException("Failed to properly disconnect all of the current Gateway interop plugins.", 
                    "One or more issues happened while disconnecting:"+errorMsg.toString(), null);
        }
    }
}
