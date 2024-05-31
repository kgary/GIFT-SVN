/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.gateway.interop;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import generated.gateway.InteropInterfaceConfig;
import mil.arl.gift.common.ConfigurationException;
import mil.arl.gift.common.PackageUtil;
import mil.arl.gift.common.course.TrainingAppUtil;
import mil.arl.gift.common.enums.TrainingApplicationEnum;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileFinderUtil;
import mil.arl.gift.common.io.FileUtil;
import mil.arl.gift.common.io.FileValidationException;
import mil.arl.gift.common.io.UnmarshalledFile;
import mil.arl.gift.common.util.JOptionPaneUtil;
import mil.arl.gift.gateway.GatewayModuleProperties;

/**
 * This class is responsible for parsing the interop config file and creating interop interface instances.
 *
 * @author mhoffman
 *
 */
public class InteropConfigFileHandler extends AbstractSchemaHandler {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(InteropConfigFileHandler.class);

    private String configFileName;

    private generated.gateway.InteropConfig interopConfig = null;

    /**
     * map of interop interfaces created
     * key: reference id value from interop configuration file, unique value for each interop configuration specified
     * value: the interop interface class instantiated and configured with the configuration parameters
     */
    private Map<Integer, AbstractInteropInterface> interops = new HashMap<>();

    /**
     * map of training application type to the interop interface configurations needed to connect to that application
     * key: unique training application type
     * value: list of configuration objects, one object per interop interface class needed to communication with that application
     */
    private Map<TrainingApplicationEnum, List<InteropInterfaceConfig>> trainingAppToInteropConfigs = new HashMap<>();

    /**
     * Class constructor - specify interop configuration file to parse
     *
     * @param configFileName - the file name (path assumed to be in configuration directory)
     */
    public InteropConfigFileHandler(String configFileName){
        super(INTEROP_SCHEMA_FILE);

        if(configFileName == null){
            throw new IllegalArgumentException("The interop config file name can't be null");
        }

        this.configFileName = configFileName;

        //
        // determine whether the configuration file is a file on disk or a resource on the classpath
        //
        InputStream iStream = null;
        File file = new File(configFileName);
        if(!file.exists()){

            logger.info("Trying to load Interop config file of "+configFileName+" from classpath." );
            iStream = FileFinderUtil.getFileByClassLoader(configFileName);
            if(iStream == null){
                throw new IllegalArgumentException("Unable to find interop config file named "+configFileName);
            }
        }else{
            try {
                logger.info("Loading interop config file of "+configFileName+" from file on disk.");
                iStream = new FileInputStream(file);
            } catch (@SuppressWarnings("unused") FileNotFoundException e) {
                //won't happen because checking for existence
            }
        }

        try{
            UnmarshalledFile uFile = parseAndValidate(generated.gateway.InteropConfig.class, iStream, true);
            interopConfig = (generated.gateway.InteropConfig) uFile.getUnmarshalled();
        }catch(Exception e){

            String details = e.getMessage();
            if(details == null && e.getCause() != null && e.getCause().getMessage() != null){
                details = e.getCause().getMessage();
            }else{
                details = "An exception was thrown while parsing the interop configuration file.";
            }

            throw new FileValidationException("Failed to parse and validate the interop configuration file.",
                    details,
                    file.getAbsolutePath(),
                    e);
        }

    }

    /**
     * Traverse the interop configurations found in the previously parsed interop configuration file and create the specified interop interfaces.
     *
     * @param selectedInterops - collection of interop implementation class names (e.g. gateway.interop.vbsplugin.VBSPluginInterface)
     * that should be the only interops instantiated and configured.  Can be null if the logic should rely on the "available"
     * value in the interop configuration XML file.
     * @throws FileValidationException if there was a validation issue with the interop configuration file
     * @throws DetailedException if there was a problem instantiating or configuring an interop plugin class based on the
     * optional list of interop classes (or all interop classes if not provided) and the configuration objects from
     * the interop configuration file.
     */
    public void configureByImplementationClassName(Collection<String> selectedInterops)
            throws FileValidationException, DetailedException{

        try{

            boolean changed = buildInteropInterfaces(interopConfig.getInterfaceConfigurations().getInteropInterfaceConfig(), selectedInterops);
            logger.info("Successfully configured using interop config file");

            if(changed){

                if (GatewayModuleProperties.getInstance().isRemoteMode()) {
                    logger.warn("The interop config file was changed but will not be update on disk because this instance is running in Java Web start mode.");
                }else{
                    logger.info("The configuration was changed by a plugin, therefore writting the new configuration values to "+configFileName+".");

                    updateInteropConfigFile(interopConfig, new File(configFileName), true);
                }
            }

        }catch(DetailedException e){
            throw e;
        }catch(Exception e){
            throw new ConfigurationException("Failed to configure the Gateway module using the interop configuration file.",
                    e.getMessage(),
                    e);
        }

    }

    /**
     * Instantiate and configure the interop classes specified in the provided collection of interop configuration objects.
     *
     * @param configs contains the interop plugin configurations to use to instantiate and configure interop classes.
     * @throws DetailedException if there was a problem instantiating or configuring an interop plugin class based on the
     * configuration objects provided.
     */
    public void configureByInteropInterfaceConfigs(Collection<generated.gateway.InteropInterfaceConfig> configs)
            throws DetailedException{

        if(configs == null){
            throw new IllegalArgumentException("The list of interop configs can't be null.");
        }

        try{
            buildInteropInterfaces(configs, null);

        }catch(DetailedException e){
            throw e;
        }catch(Exception e){
            throw new ConfigurationException("Failed to configure the Gateway module using the list of "+configs.size()+" interop configurations.",
                    e.getMessage(),
                    e);
        }
    }

    /**
     * Update the interop configuration file specified by writing the provided configuration
     * object to that XML file.
     *
     * @param iConfig the configuration object to write
     * @param interopConfigFile the interop configuration XML file to write to
     * @param backupOriginalFile whether to backup the original interop configuration XML file
     * @throws IOException if there was a problem writing the XML file
     * @throws SAXException If a SAX error occurs during parsing.
     * @throws JAXBException Can occur when marshalling
     */
    public static void updateInteropConfigFile(generated.gateway.InteropConfig iConfig, File interopConfigFile, boolean backupOriginalFile) throws IOException, SAXException, JAXBException{

        if(backupOriginalFile){
            File backupConfigFile = FileUtil.backupFile(interopConfigFile);
            logger.info("Made a copy of the current interop config to "+backupConfigFile+".");
        }

        writeToFile(iConfig, interopConfigFile, false);
    }

    /**
     * Update the interop configuration file parsed by this handler by writing the current configuration
     * object to that XML file.
     *
     * @param backupOriginalFile whether to backup the original interop configuration XML file
     * @throws IOException if there was a problem writing the XML file
     * @throws SAXException If a SAX error occurs during parsing.
     * @throws JAXBException Can occur when marshalling
     */
    public void updateInteropConfigFile(boolean backupOriginalFile) throws IOException, SAXException, JAXBException{
        updateInteropConfigFile(interopConfig, new File(configFileName), backupOriginalFile);
    }

    /**
     * Update the 'available' element value in the Gateway module's interop configuration file.
     *
     * @param interopImplName the unique name of an interop implementation in the interop configuration file.
     * @param isAvailable the value to set the 'available' element to
     */
    public void setInteropAvailability(String interopImplName, boolean isAvailable){

        for(InteropInterfaceConfig config : interopConfig.getInterfaceConfigurations().getInteropInterfaceConfig()){

            if(config.getName().equals(interopImplName)){
                config.setAvailable(Boolean.valueOf(isAvailable));
                logger.info("Set interop implementation named '"+config.getName()+"' availability value to "+isAvailable+".");
            }
        }
    }

    /**
     * Return the container of interop interfaces created from the interop config file
     *
     * @return Map<Integer, AbstractInteropInterface>  Will not be null.
     * key: reference id value from interop configuration file, unique value for each interop configuration specified
     * value: the interop interface class instantiated and configured with the configuration parameters
     */
    public Map<Integer, AbstractInteropInterface> getInterops(){
        return interops;
    }

    /**
     * Return the map of training application type to the interop interface configurations needed to connect to that application
     *
     * @return the map where:
     * key: unique training application type
     * value: list of configuration objects, one object per interop interface class needed to communication with that application
     */
    public Map<TrainingApplicationEnum, List<InteropInterfaceConfig>> getTrainingAppMap(){
        return trainingAppToInteropConfigs;
    }

    /**
     * Add an interop interface configuration to the map of configurations for a training application type.
     *
     * @param config the interop interface configuration to map to a training application type
     */
    private void addTrainingAppMapping(InteropInterfaceConfig config){

        TrainingApplicationEnum trainingApp = TrainingAppUtil.getTrainingAppTypes(config.getImpl());
        if(trainingApp != null){
            List<InteropInterfaceConfig> configs = trainingAppToInteropConfigs.get(trainingApp);

            if(configs == null){
                configs = new ArrayList<>();
                trainingAppToInteropConfigs.put(trainingApp, configs);
            }

            configs.add(config);
        }
    }
    
    /**
     * Get the latest name for a interop plugin class implementation.  For most interops this will
     * return the same value that is provided.  In other cases the class name for the interop impl maybe
     * have been changed after course.xml were created.  This method allows the GW to interpret and change
     * interop impl strings to the current values without forcing an update to the pre-existing course.xml content.
     * 
     * @param candidateInteropPluginImplName the same value of the Gateway interop plugin class you
     * would find in the GIFT\config\gateway\configurations\default.interopConfig.xml and course.xml training
     * application element.  E.g. gateway.interop.unity.UnityInterface
     * @return either the value of candidateInteropPluginImplName if the name doesn't need to be changed, or
     * the new name of the interop class.
     */
    public static String convertFromLegacyInteropName(String candidateInteropPluginImplName){
     
        //
        // Translate legacy interop plugin class names
        //
        if("gateway.interop.havenplugin.HAVENPluginInterface".equals(candidateInteropPluginImplName)){
            // HAVEN was renamed SE Sandbox in Oct 2021
            return "gateway.interop.sesandboxplugin.SESandboxPluginInterface";
        }  
        
        return candidateInteropPluginImplName;
    }

    /**
     * Instantiate and configure the interop configuration file listed interfaces
     *
     * @param iConfig - the interop configuration file values
     * @param selectedInterops - collection of interop implementation class names (e.g. gateway.interop.vbsplugin.VBSPluginInterface)
     * that should be the only interops instantiated and configured.  Can be null if the logic should rely on the "available"
     * value in the interop configuration XML file.
     * @return boolean - true iff a plugin update a configuration value and the update should be written to disk
     * @throws DetailedException if there was a problem instantiating or configuring an interop plugin class based on the
     * configuration objects provided.  Also if one of the selected interops specified was not instantiated and configured.
     */
    private boolean buildInteropInterfaces(Collection<generated.gateway.InteropInterfaceConfig> configs, Collection<String> selectedInterops)
            throws DetailedException{

        boolean configUpdated = false;

        if(configs == null){
            throw new IllegalArgumentException("The interop config list is null.");
        }

        //clear the previous map of instantiated and configured interops
        interops.clear();

        for(InteropInterfaceConfig config : configs){

            try{
                config.setName(convertFromLegacyInteropName(config.getName()));
                
                //ignore isAvailable when running Java Web Start mode
                boolean ignoreAvailability = GatewayModuleProperties.getInstance().isRemoteMode();

                //only configure and add the interop if it is available (default = is available)
                Boolean isAvailable = config.isAvailable();
                if(isAvailable != null && !isAvailable && !ignoreAvailability){
                    //skip this interface since its unavailable
                    logger.warn("Not instantiating interop interface named "+config.getName()+" with id of "+config.getRefID()+" because the 'isAvailable' value is false.");
                    continue;

                }else if(interops.containsKey(config.getRefID().intValue())){
                    //change reference ID to new value

                    int currVal = config.getRefID().intValue();
                    while(interops.containsKey(currVal)){
                        currVal++;
                    }

                    logger.info("Updating 'refID' value for "+config.getName()+" in interopConfig.xml from "+config.getRefID().intValue()+" to "+currVal+" to make it unique.");
                    config.setRefID(BigInteger.valueOf(currVal));

                    configUpdated = true;
                }

                if(isAvailable == null || isAvailable || ignoreAvailability){
                    //this is a candidate interop

                    //did the caller provide a filter (i.e. optional selected interops), if so it this in the list?
                    if (selectedInterops == null || selectedInterops.contains(config.getImpl())) {
                        //instantiate interop class and configure

                        AbstractInteropInterface interopInterface = getInteropInstantiation(config.getImpl(), config.getName());

                        configUpdated |= interopInterface.configure(config.getInput());

                        logger.info("Configured interop interface: " + interopInterface.getName());

                        interops.put(config.getRefID().intValue(), interopInterface);


                    }else{
                        logger.info("Ignoring interop interface named '"+config.getName()+"' because it is not in the selected interops list of "+selectedInterops+"." );
                    }

                    addTrainingAppMapping(config);

                }else{
                    logger.info("Ignoring interop interface named '"+config.getName()+"' because it is set to not available.");
                }

            }catch(ConfigurationException configException){

                logger.error("caught exception while trying to instantiate interop interface class "+config.getImpl(), configException);

                int choice = -1;
                if(!GatewayModuleProperties.getInstance().isRemoteMode()){

                    //ask the user if they wish to make the interface unavailable
                    choice = JOptionPaneUtil.showOptionDialog("The following configuration check failed:\n\n"+
                                configException.getReason() + "\n\n"+ configException.getDetails(),
                            "Update Gateway Configuration?",
                            JOptionPane.QUESTION_MESSAGE,
                            "Disable Gateway Interop Plugin",
                            "Close Gateway Module",
                            null);
                }

                if(choice == JOptionPane.YES_OPTION || choice == JOptionPane.OK_OPTION){

                    logger.info("Setting "+config.getName()+" interface to unavailable because user selected to do so after an issue with the interface.");
                    config.setAvailable(false);
                    configUpdated = true;

                }else{
                    throw new ConfigurationException("Failed to configure the interop interface class "+config.getImpl()+".\nReason: "+configException.getReason(),
                            configException.getDetails(),
                            configException);
                }


            }catch(Exception e){
                logger.error("caught exception while trying to instantiate interop interface class "+config.getImpl(), e);

                int choice = -1;
                if(!GatewayModuleProperties.getInstance().isRemoteMode()){

                    //ask the user if they wish to make the interface unavailable
                    choice = JOptionPaneUtil.showOptionDialog("The following configuration check failed:\n\n"+
                            "There was a problem with configuring the interop interface named "+config.getName()+" from "+configFileName+"\n\n" +
                            "Would you like to continue by making that Gateway module interop interface unavailable for future use by updating "+configFileName+"?\n" +
                            "(this automatically updates "+configFileName+" and require you to use the GIFT installer to enable it in the future)",
                            "Update Gateway Configuration?",
                            JOptionPane.QUESTION_MESSAGE,
                            "Disable Gateway Interop Plugin",
                            "Close Gateway Module",
                            null);
                }

                if(choice == JOptionPane.YES_OPTION || choice == JOptionPane.OK_OPTION){

                    logger.info("Setting "+config.getName()+" interface to unavailable because user selected to do so after an issue with the interface.");
                    config.setAvailable(false);
                    configUpdated = true;

                }else{

                    if(e instanceof DetailedException){
                        throw e;
                    }else{
                        throw new ConfigurationException("Failed to configure the interop interface class "+config.getImpl()+".",
                                e.getMessage(),
                                e);
                    }
                }
            }

        }

        if(selectedInterops != null){
            //check selected interops list for coverage in instantiated interops

            boolean found;
            for(String interopClassName : selectedInterops){

                found = false;
                for(AbstractInteropInterface interop : interops.values()){

                    if(interop.getClass().getName().contains(interopClassName)){
                        found = true;
                        break;
                    }
                }

                if(!found){
                    throw new DetailedException("Failed to instantiate and configure all of the requested Gateway interop plugin classes.",
                            "The interop plugin classes named '"+interopClassName+"' was requested but not instantiated.", null);
                }
            }

            logger.info("Instantiated and configured all "+selectedInterops.size()+" Gateway interop plugin classes requested.");
        }

        return configUpdated;
    }

    /**
     * Return an instantiated interop plugin interface class for the class name provided.
     *
     * @param interopClassName the name of a gateway interop plugin class, without the mil.arl.gift prefix.
     * @param interopInstanceName the name of the instance, used for display and debugging purposes
     * @return the new instance of the class created by this method.
     */
    public static AbstractInteropInterface getInteropInstantiation(String interopClassName, String interopInstanceName){

        AbstractInteropInterface interopInterface;
        try{
            Class<?> clazz = Class.forName(PackageUtil.getRoot() + "." + interopClassName);
            Constructor<?> constructor = clazz.getConstructor(String.class);
            interopInterface = (AbstractInteropInterface)constructor.newInstance(interopInstanceName);
        }catch(Exception e){
            throw new DetailedException("Failed to instantiate and configure the Gateway interop plugin class of '"+interopInstanceName+"'.",
                    "There was an exception thrown when trying to instantiate the class that reads:\n"+e.getMessage(), e);
        }

        return interopInterface;
    }

}
