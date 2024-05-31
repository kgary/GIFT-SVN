/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.services;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.enums.DeploymentModeEnum;
import mil.arl.gift.common.io.FileFinderUtil;
import mil.arl.gift.common.io.UriUtil;
import mil.arl.gift.tools.services.db.DbServices;
import mil.arl.gift.tools.services.db.DbServicesInterface;
import mil.arl.gift.tools.services.experiment.DataCollectionServices;
import mil.arl.gift.tools.services.experiment.DataCollectionServicesInterface;
import mil.arl.gift.tools.services.file.AbstractFileServices;
import mil.arl.gift.tools.services.file.FileServices;
import mil.arl.gift.tools.services.user.UserServicesInterface;
import mil.arl.gift.tools.services.user.desktop.DesktopUserServices;
import mil.arl.gift.tools.services.user.server.ServerUserServices;

/**
 * This class manages the various GIFT services.  Each services is constructing according to the deployment mode.
 * 
 * @author mhoffman
 *
 */
public class ServicesManager {
    
    /** instance of the logger */
    private final static Logger logger = LoggerFactory.getLogger(ServicesManager.class);
    
    /** the deployment mode of this GIFT instance */
    private final static DeploymentModeEnum deploymentMode = ServicesProperties.getInstance().getDeploymentMode();
    
    /** where runtime course folders are located  */
    protected static final File DOMAIN_DIRECTORY = new File(ServicesProperties.getInstance().getDomainDirectory());
    
    /**
     * the various service interfaces
     */
    private FileServices fileServices = FileServices.getInstance();
    private DbServicesInterface dbServices;
    private UserServicesInterface userServices;
    private DataCollectionServicesInterface dataCollectionServices;
    
    /** singleton instance of this class */
    private final static ServicesManager instance = new ServicesManager();
    
    /**
     * Return the singleton instance of this class
     * 
     * @return ServicesManager
     */
    public static ServicesManager getInstance(){
        return instance;
    }

    /**
     * Initialize the appropriate services based on deployment mode
     */
    private ServicesManager(){
        
        if(deploymentMode == DeploymentModeEnum.DESKTOP){
            initDesktopServices();
        }else if(deploymentMode == DeploymentModeEnum.SERVER){
            initServerServices();
        }else if(deploymentMode == DeploymentModeEnum.SIMPLE){
            initDesktopServices();
        }else{
            logger.error("Found unhandled deployment mode of "+deploymentMode+".  Defaulting to desktop services.");
            initDesktopServices();
        }
    }
    
    /**
     * Initialize the desktop services
     */
    private void initDesktopServices(){
        
        dbServices = new DbServices();
        userServices = new DesktopUserServices();
        dataCollectionServices = new DataCollectionServices();
    }
    
    /**
     * Initialize the server services
     */
    private void initServerServices(){
        
        dbServices = new DbServices();
        userServices = new ServerUserServices();
        dataCollectionServices = new DataCollectionServices();
    }
    
    /**
     * Return the file services interface used to interact with GIFT file system based on the deployment mode.
     * 
     * @return file services for this deployment instance
     */
    public AbstractFileServices getFileServices(){
        return fileServices.getFileServices();
    }
    
    /**
     * Return the database services interface used to interact with GIFT database(s) based on the deployment mode.
     * 
     * @return DbServicesInterface
     */
    public DbServicesInterface getDbServices(){
        return dbServices;
    }
    
    /**
     * Return the user services interface used to interact with the GIFT user management system
     * based on the deployment mode.
     * 
     * @return UserServicesInterface
     */
    public UserServicesInterface getUserServices(){
        return userServices;
    }
    
    /**
     * Return the data collection services interface used to interact with the GIFT experiment logic.
     * 
     * @return DataCollectionServicesInterface
     */
    public DataCollectionServicesInterface getDataCollectionServices(){
        return dataCollectionServices;
    }
    
    /**
     * Return the URL for the export file specified.  The export file must be a descendant of the domain server
     * directory in order for the browser to download it.
     * 
     * @param file the export zip file that is hosted by the domain server.
     * @return a URL used to access the export zip file
     * @throws MalformedURLException if there was a problem constructing the URL
     */
    public static URL getExportURL(File file) throws MalformedURLException{
        
        //get export file's path in the domain directory
        String filePath = FileFinderUtil.getRelativePath(DOMAIN_DIRECTORY, file);
        
        String networkURL;
        try {
            networkURL = ServicesProperties.getInstance().getDomainContentServerAddress() + "/";

        } catch (Exception ex) {

            logger.error("Could not get the host IP address, defaulting to 'localhost'", ex);
            networkURL = ServicesProperties.getInstance().getTransferProtocol() + "localhost:" + ServicesProperties.getInstance().getDomainContentServerPort() + "/";
        }        
        
        networkURL += filePath;
        
        String encodedUri = UriUtil.makeURICompliant(networkURL);
        return new URL(encodedUri);
    }
}
