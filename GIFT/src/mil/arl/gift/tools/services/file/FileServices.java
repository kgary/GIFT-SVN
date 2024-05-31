/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.services.file;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.enums.DeploymentModeEnum;
import mil.arl.gift.net.nuxeo.NuxeoInterface;
import mil.arl.gift.tools.services.ServicesProperties;
import mil.arl.gift.tools.services.file.desktop.DesktopFileServices;
import mil.arl.gift.tools.services.file.server.ServerFileServices;

/**
 * Used to manage file operations that are abstracted from the underlying file system (e.g. Windows or Nuxeo)
 * 
 * @author mhoffman
 *
 */
public class FileServices {
    
    /** instance of the logger */
    private final static Logger logger = LoggerFactory.getLogger(FileServices.class);
    
    /** the deployment mode of this GIFT instance */
    private final static DeploymentModeEnum deploymentMode = ServicesProperties.getInstance().getDeploymentMode();
    
    /** where runtime course folders are located  */
    protected static final File DOMAIN_DIRECTORY = new File(ServicesProperties.getInstance().getDomainDirectory());    
    
    /**
     * create the connection to Nuxeo to be used by various services referenced in this manager
     * Note: will be null if not running in server deployment mode
     */
    public static final NuxeoInterface nuxeoInterface;
    static {

        if(ServicesProperties.getInstance().isServerDeploymentMode()){
            nuxeoInterface = new NuxeoInterface(ServicesProperties.getInstance().getCMSURL(), ServicesProperties.getInstance().getCMSSecretKey());
    
            int quota = ServicesProperties.getInstance().getCMSUserWorkspaceQuota();
            if (quota != Integer.MIN_VALUE) {
                nuxeoInterface.setUserWorkspaceQuota(quota);
            }
            
            String adminUsername = ServicesProperties.getInstance().getCMSAdminUser();
            if (adminUsername != null) {
                nuxeoInterface.setAdminUsername(adminUsername);
            }
        }else{
            nuxeoInterface = null;
        }
    }

    /** the abstracted file services instance to use based on deployment mode */
    private AbstractFileServices fileServices;

    /** singleton instance of this class */
    private final static FileServices instance = new FileServices();
    
    /**
     * Return the singleton instance of this class
     * 
     * @return the file services instance
     */
    public static FileServices getInstance(){
        return instance;
    }

    /**
     * Initialize the appropriate services based on deployment mode
     */
    private FileServices(){
        
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
        fileServices = new DesktopFileServices();
    }
    
    /**
     * Initialize the server services
     */
    private void initServerServices(){
        fileServices = new ServerFileServices();
    }
    
    /**
     * Return the file services interface used to interact with GIFT file system based on the deployment mode.
     * 
     * @return file services for this deployment instance
     */
    public AbstractFileServices getFileServices(){
        return fileServices;
    }
}
