/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.spl;

import java.awt.SplashScreen;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.OperationNotSupportedException;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.PackageUtil;
import mil.arl.gift.common.enums.DeploymentModeEnum;
import mil.arl.gift.common.enums.LessonLevelEnum;
import mil.arl.gift.common.enums.MessageEncodingTypeEnum;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.enums.ModuleModeEnum;
import mil.arl.gift.common.enums.ModuleTypeEnum;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.ImageProperties;
import mil.arl.gift.common.io.PlatformUtils;
import mil.arl.gift.common.io.PlatformUtils.SupportedOSFamilies;
import mil.arl.gift.common.module.ModuleStatus;
import mil.arl.gift.common.util.JOptionPaneUtil;
import mil.arl.gift.common.util.ProgressDialog;
import mil.arl.gift.net.api.NetworkSession;
import mil.arl.gift.net.api.SubjectUtil;
import mil.arl.gift.net.api.message.Message;
import mil.arl.gift.net.api.message.MessageHandler;
import mil.arl.gift.net.util.Util;

/**
 * This class provides a way to launch GIFT modules under 1 process instead of a separate
 * process for each module.
 *  
 * @author cdettmering
 */
public class SingleProcessLauncher {

	/** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(SingleProcessLauncher.class);
    
    
    static {
        //use SPL log4j
        PropertyConfigurator.configureAndWatch(PackageUtil.getConfiguration() + "/tools/spl/spl.log4j.properties");
    }
    
    /**
     * Main arguments
     */
    private static final String ACTIVEMQ_ARG    = "activemq";
    private static final String GAS_ARG         = "gas";
    private static final String UMS_ARG         = "ums";
    private static final String LMS_ARG         = "lms";
    private static final String PED_ARG         = "ped";
    private static final String LEARNER_ARG     = "learner";
    private static final String SENSOR_ARG      = "sensor";
    private static final String DOMAIN_ARG      = "domain";
    private static final String TUTOR_ARG       = "tutor";
    private static final String GATEWAY_ARG     = "gateway";
    
    private static final List<String> NOT_MODULE_ARGS = new ArrayList<>();
    static{
        NOT_MODULE_ARGS.add(ACTIVEMQ_ARG);
        NOT_MODULE_ARGS.add(GAS_ARG);
    }
	
	private static final ActiveMQState activeMQState = new ActiveMQState();
	private static final GASState gasState = new GASState();
	private static final ModuleStatusManager moduleStatusManager = new ModuleStatusManager();
	private static final SingleProcessProperties properties = new SingleProcessProperties();
    private static final BuildDependencies buildProperties = new BuildDependencies();
    
    private static final DeploymentModeEnum deploymentMode = properties.getDeploymentMode();
    
    /** script used to check the Internet Explorer version against GIFT supported version(s) */
    private static final File IE_VERSION_CHECK = new File("scripts" + File.separator + "install" + File.separator + "IE.VersionCheck.bat");
    
    private static final Map<ModuleTypeEnum, Thread> moduleThreads = new HashMap<>();
    
    /** the process for the gift web browser launched by SPL */
    private static Process giftBrowserProcess = null;
    
    /** the process for the gift wrap web browser launched by SPL */
    private static Process wrapBrowserProcess = null;
    
    /** used to monitor the status of the GIFT webpage (i.e. TUI or Dashboard) process */
    private static Thread giftBrowserMonitorThread = null;
    
    /** used to monitor the status of the GIFT Wrap webpage process */
    private static Thread wrapBrowserMonitorThread = null;
    
    /** flag used to indicate if SPL is in the process of exiting (i.e. the JVM is terminating)*/
    private static boolean exiting = false;
    
    /** a server object initialized and used by the Tutor module thread */
    private static Object tutorWebServer;
    
    /** a server object initialized and used by the Dashboard thread */
    private static Object gasWebServer;
    
    /** the progress dialog used to indicate the rate of completion */
    private static ProgressDialog progressDialog;
    static{
        try{
            progressDialog = new ProgressDialog();
        }catch(Exception e){
            logger.error("Failed to create the progress dialog due to an exception", e);
            throw e;
        }
    }
    
    /** icon used on top of dialog windows */
    private static ImageIcon icon = null;

    /**
     * The url parameter for redirecting to the Wrap landing page. Must match
     * {@link mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility#LAUNCH_WRAP_URL_PARAM}
     * and {@link mil.arl.gift.tools.controlpanel.ToolButtons#LAUNCH_WRAP_URL_PARAM}
     */
    private final static String LAUNCH_WRAP_URL_PARAM = "launchwrap";
    
    /**
     * Used to create message bus connections (ActiveMQ) to receive and send messages
     * over queues/topics.  Will be initialized once ActiveMQ is started.
     */
    private static NetworkSession session = null;
        
    /**
     * Return the system small icon as set by the image properties
     * @return the image for the system small icon
     */
    private static ImageIcon getSystemSmallIcon(){
        if(icon == null){
            icon = new ImageIcon(SingleProcessLauncher.class.getResource(File.separator + ImageProperties.getInstance().getPropertyValue(ImageProperties.SYSTEM_ICON_SMALL)));
        }
        
        return icon;
    }

	/**
	 * Launches the GIFT Webpage (i.e. TUI or Dashboard) for the user to interact with 
	 * using the URL from the properties file.
	 */
	public static void launchGIFTWebpage() {		
		try {
			// Close splash screen if open
			SplashScreen splash = SplashScreen.getSplashScreen();
			if(splash != null && splash.isVisible()) {
				splash.close();
			}
			
			SystemTrayManager.getInstance().displayAlert();
			
			if(giftBrowserMonitorThread == null || !giftBrowserMonitorThread.isAlive()){
			    
			    //
			    //create new browser process instance
			    //
			    String ipAddress = null;
		        try {

		            // Get IP Address
		            ipAddress = Util.getLocalHostAddress().getHostAddress();

		        } catch (Exception e) {
		            logger.error("Caught an exception while getting localhost IP address", e);
		        }
		        
		        String browserURL;
		        if(deploymentMode == DeploymentModeEnum.SIMPLE){
		            browserURL = properties.getTutorURL();
		        }else{
		            browserURL = properties.getDashboardURL();
		        }
		        
		        if(ipAddress != null && browserURL.contains("localhost")){
		            //replace 'localhost' with IP address of SPL machine (this computer)
		            //This is to help with the occasional issue discovered in ticket 816.
		            browserURL = browserURL.replace("localhost", ipAddress);
		        }

	            // NOTE: This only works on Microsoft Windows
		        if(SupportedOSFamilies.WINDOWS.equals(PlatformUtils.getFamily())) {
    	            giftBrowserProcess = Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + browserURL);
    		        
    		        //create new thread to monitor browser process in order to notify users that GIFT is 
    		        //still running when the browser is closed
    		        giftBrowserMonitorThread = new Thread("Wait-For-GIFT-Browser-Exit"){
    		            
    		            @Override
    		            public void run(){
    		                
    		                try{
    		                    giftBrowserProcess.waitFor();
    		                    
    		                    if(!exiting){
    		                        //only display the message if SPL is not in the process of exiting 
    		                    	SystemTrayManager.getInstance().displayBrowserExitMessage();		                    	
    		                    }
    		                    
    		                } catch (InterruptedException e) {
    		                    logger.error("Caught exception while trying to monitor the browser, therefore the browser exit message will not be presented to the SPL user.", e);
    		                }
    		            }
    		        };
		        		
    		        giftBrowserMonitorThread.start();
		        
		        }
		        
		        // Close the dialog since the browser is being opened
                progressDialog.hide();
                
			}else if(giftBrowserMonitorThread.isAlive()){
			    //the browser monitor thread is still running, therefore the browser is still open
			    
			    //present dialog saying that the webpage is already opened
				//Custom button text
				JButton retryBtn = new JButton("Retry");
				JButton closeBtn = new JButton("Close");
				JButton[] options = {retryBtn, closeBtn};

				//One Dialog for already open, one for attempting to re-open
				JOptionPane alreadyOpenedDialog = new JOptionPane();
				JOptionPane pleaseWaitDialog = new JOptionPane();
				

				alreadyOpenedDialog.setMessage("<html><center>The GIFT Webpage is already opened.<br><br>If you recently closed the GIFT Webpage, please try again later"
						+ "<br>(Sometimes the browser process takes 20-30 seconds to terminate)</center></html>");				
				alreadyOpenedDialog.setOptions(options);				
				alreadyOpenedDialog.setMessageType(JOptionPane.INFORMATION_MESSAGE);
				
				
				JDialog dialog = alreadyOpenedDialog.createDialog("GIFT Webpage Exists");
				JDialog wait = pleaseWaitDialog.createDialog("Opening Webpage");
				
				dialog.setIconImage(getSystemSmallIcon().getImage());
				wait.setIconImage(getSystemSmallIcon().getImage());
				wait.setSize(dialog.getSize().width - 25, dialog.getSize().height-25);
				
				//Used to stall and wait for browserProcess to end for 2.5 seconds, then attempts to launch the page again
				Timer timer = new Timer(2500, new ActionListener() {
		            @Override
					public void actionPerformed(ActionEvent e) {
		            	wait.dispose();
		            	launchGIFTWebpage();
		            }
		        });
				
				//Close already open dialog and open attempting to reopen dialog
				retryBtn.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						dialog.dispose();
						wait.setModal(false);
						wait.setVisible(true);
						pleaseWaitDialog.setOptions(new Object[] {});
						pleaseWaitDialog.setMessage("Please Wait, Attempting to re-open the GIFT Webpage...");
						pleaseWaitDialog.setMessageType(JOptionPane.INFORMATION_MESSAGE);						
						timer.setRepeats(false);
						timer.start();
					}					
				});
				
				//Close the Dialog
				closeBtn.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						dialog.dispose();
					}					
				});

				dialog.setVisible(true);		
			}
		
		} catch (IOException e1) {
			logger.error("Caught exception while trying to launch GIFT webpage.", e1);
			
			SwingUtilities.invokeLater(new Runnable() {
                
                @Override
                public void run() {
                    JOptionPane.showMessageDialog(null, "There was a problem when attempting to launch the GIFT webpage.\n" +
                            "Error reads: "+e1.getMessage()+"\n\nFor more information check the latest SPL named log in GIFT\\output\\logger\\tools\\.\n\n");
                    exit();                    
                }
            });

		}
	}
	
	   /**
     * Launches the GIFT Wrap Webpage for the user to interact with 
     * using the URL from the properties file.
     */
    public static void launchWrapWebpage() {        
        try {
            // Close splash screen if open
            SplashScreen splash = SplashScreen.getSplashScreen();
            if(splash != null && splash.isVisible()) {
                splash.close();
            }
            
            SystemTrayManager.getInstance().displayAlert();
            
            if(wrapBrowserMonitorThread == null || !wrapBrowserMonitorThread.isAlive()){
                
                //
                //create new browser process instance
                //
                String ipAddress = null;
                try {

                    // Get IP Address
                    ipAddress = Util.getLocalHostAddress().getHostAddress();

                } catch (Exception e) {
                    logger.error("Caught an exception while getting localhost IP address", e);
                }

                if (deploymentMode == DeploymentModeEnum.SERVER) {
                    throw new DetailedException(
                            "Unable to launch GIFT Wrap through the system tray in current GIFT configuration.",
                            "GIFT Wrap can't be launched through the system tray in Server deployment mode.  Please change the deployment mode and restart GIFT.",
                            null);
                }

                String browserURL = properties.getGiftAuthorToolURL() + "#" + LAUNCH_WRAP_URL_PARAM;
                if(ipAddress != null && browserURL.contains("localhost")){
                    //replace 'localhost' with IP address of SPL machine (this computer)
                    //This is to help with the occasional issue discovered in ticket 816.
                    browserURL = browserURL.replace("localhost", ipAddress);
                }
                
                //
                // Perform Internet Explorer version check
                //
                try{
                    Runtime.getRuntime().exec(IE_VERSION_CHECK.getAbsolutePath());
                }catch(Throwable t){
                    logger.error("Caught exception while trying to run the Internet Explorer version check script.", t);
                }

                // NOTE: This only works on Microsoft Windows
                wrapBrowserProcess = Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + browserURL);
                
                //create new thread to monitor browser process in order to notify users that GIFT is 
                //still running when the browser is closed
                wrapBrowserMonitorThread = new Thread("Wait-For-Wrap-Browser-Exit"){
                    
                    @Override
                    public void run(){
                        
                        try{
                            wrapBrowserProcess.waitFor();
                            
                            if(!exiting){
                                //only display the message if SPL is not in the process of exiting 
                                SystemTrayManager.getInstance().displayBrowserExitMessage();                                
                            }
                            
                        } catch (InterruptedException e) {
                            logger.error("Caught exception while trying to monitor the GIFT Wrap browser, therefore the browser exit message will not be presented to the user.", e);
                        }
                    }
                };
                        
                wrapBrowserMonitorThread.start();
                
                // Close the dialog since the browser is being opened
                progressDialog.hide();
                
            }else if(wrapBrowserMonitorThread.isAlive()){
                //the wrap browser monitor thread is still running, therefore the browser is still open
                
                //present dialog saying that the webpage is already opened
                //Custom button text
                JButton retryBtn = new JButton("Retry");
                JButton closeBtn = new JButton("Close");
                JButton[] options = {retryBtn, closeBtn};

                //One Dialog for already open, one for attempting to re-open
                JOptionPane alreadyOpenedDialog = new JOptionPane();
                JOptionPane pleaseWaitDialog = new JOptionPane();
                

                alreadyOpenedDialog.setMessage("<html><center>The GIFT Wrap Webpage is already opened.<br><br>If you recently closed the GIFT Wrap Webpage, please try again later"
                        + "<br>(Sometimes the browser process takes 20-30 seconds to terminate)</center></html>");              
                alreadyOpenedDialog.setOptions(options);                
                alreadyOpenedDialog.setMessageType(JOptionPane.INFORMATION_MESSAGE);
                
                
                JDialog dialog = alreadyOpenedDialog.createDialog("GIFT Wrap Webpage Exists");
                JDialog wait = pleaseWaitDialog.createDialog("Opening Webpage");
                
                dialog.setIconImage(getSystemSmallIcon().getImage());
                wait.setIconImage(getSystemSmallIcon().getImage());
                wait.setSize(dialog.getSize().width - 25, dialog.getSize().height-25);
                
                //Used to stall and wait for browserProcess to end for 2.5 seconds, then attempts to launch the page again
                Timer timer = new Timer(2500, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        wait.dispose();
                        launchWrapWebpage();
                    }
                });
                
                //Close already open dialog and open attempting to reopen dialog
                retryBtn.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent arg0) {
                        dialog.dispose();
                        wait.setModal(false);
                        wait.setVisible(true);
                        pleaseWaitDialog.setOptions(new Object[] {});
                        pleaseWaitDialog.setMessage("Please Wait, Attempting to re-open the GIFT Wrap Webpage...");
                        pleaseWaitDialog.setMessageType(JOptionPane.INFORMATION_MESSAGE);                       
                        timer.setRepeats(false);
                        timer.start();
                    }                   
                });
                
                //Close the Dialog
                closeBtn.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent arg0) {
                        dialog.dispose();
                    }                   
                });

                dialog.setVisible(true);        
            }
        
        } catch (IOException e1) {
            logger.error("Caught exception while trying to launch GIFT wrap webpage.", e1);
            
            SwingUtilities.invokeLater(new Runnable() {
                
                @Override
                public void run() {
                    JOptionPane.showMessageDialog(null, "There was a problem when attempting to launch the GIFT Wrap webpage.\n" +
                            "Error reads: "+e1.getMessage()+"\n\nFor more information check the latest SPL named log in GIFT\\output\\logger\\tools\\.\n\n");
                    exit();                    
                }
            });

        }
    }
	
	/**
	 * Cleans up any modules or other applications started by SPL
	 */
	private static void cleanup(){
	    
        try{
            
            //check whether or not the gift web browser launched by SPL should be closed
            if(giftBrowserMonitorThread != null && giftBrowserMonitorThread.isAlive()){    
                
                JButton closeBrowserBtn = new JButton("Close Browser");
                JButton leaveOpenBtn = new JButton("Leave Open");
                JButton[] options = {closeBrowserBtn, leaveOpenBtn}; 
                leaveOpenBtn.setPreferredSize(closeBrowserBtn.getPreferredSize());
                
                JOptionPane closeGIFT = new JOptionPane();
                closeGIFT.setMessage("<html><center>The GIFT Webpage is still open.<br>(If you recently closed the GIFT Webpage, the browser process may still be terminating...)" + 
                    "<br><br>Would you like GIFT to close the browser?</center></html>");                        
                closeGIFT.setOptions(options);            
                
                JDialog closeDialog = closeGIFT.createDialog("Close GIFT Webpage Browser?");
                closeDialog.setIconImage(getSystemSmallIcon().getImage());
                
                closeBrowserBtn.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent arg0) {
                        exiting = true;
                        closeDialog.dispose();
                        giftBrowserProcess.destroy();                   
                    }                   
                });
                
                leaveOpenBtn.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent arg0) {
                        closeDialog.dispose();                  
                    }                   
                });
                
            	closeDialog.setVisible(true);
            }
            
            //check whether or not the gift wrap web browser launched by SPL should be closed
            if(wrapBrowserMonitorThread != null && wrapBrowserMonitorThread.isAlive()){    
                
                JButton closeBrowserBtn = new JButton("Close Browser");
                JButton leaveOpenBtn = new JButton("Leave Open");
                JButton[] options = {closeBrowserBtn, leaveOpenBtn}; 
                leaveOpenBtn.setPreferredSize(closeBrowserBtn.getPreferredSize());
                
                JOptionPane closeGIFT = new JOptionPane();
                closeGIFT.setMessage("<html><center>The GIFT Wrap Webpage is still open.<br>(If you recently closed the GIFT Wrap Webpage, the browser process may still be terminating...)" + 
                    "<br><br>Would you like GIFT to close the GIFT Wrap browser?</center></html>");                        
                closeGIFT.setOptions(options);            
                
                JDialog closeDialog = closeGIFT.createDialog("Close GIFT Wrap Webpage Browser?");
                closeDialog.setIconImage(getSystemSmallIcon().getImage());
                
                closeBrowserBtn.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent arg0) {
                        exiting = true;
                        closeDialog.dispose();
                        wrapBrowserProcess.destroy();                   
                    }                   
                });
                
                leaveOpenBtn.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent arg0) {
                        closeDialog.dispose();                  
                    }                   
                });
                
                closeDialog.setVisible(true);
            }
            
            SystemTrayManager.getInstance().displayClosingMessage();
            
            killModules();
            
            moduleStatusManager.close();
            
            if (SystemTrayManager.getInstance().hasSystemTray()) {    
                SystemTrayManager.getInstance().clean();
            }
            
        }catch(Throwable e){
            logger.error("Caught exception while trying to exit gracefully.", e);
        }
	    
	}

	
	/**
	 * Exits
	 */
    public static void exit() {

        try{
            if(logger.isInfoEnabled()){
                logger.info("Starting exit sequence.");
            }
            
            progressDialog.hide();
            cleanup();
        }catch(Throwable t){
            logger.error("Caught throwable while trying to cleanup and exit.", t);
            JOptionPane.showConfirmDialog(null, 
                    "There was a problem exiting GIFT.  Check the latest spl log files in GIFT/output/logger/tools.",
                    "Problem Exiting GIFT", 
                    JOptionPane.OK_OPTION, 
                    JOptionPane.ERROR_MESSAGE);
        }

        if(logger.isInfoEnabled()){
            logger.info("Exiting single process mode.");
        }
        System.exit(0);
    }

    /**
     * Exits the module on an exit
     *
     * @param message - an error message about why SPL is exiting
     */
    public static void errorExit(String message) {
        
        if(logger.isInfoEnabled()){
            logger.info("Starting exit sequence due to error.");
        }

        cleanup();

        logger.error(message);
        JOptionPaneUtil.showConfirmDialog(message, "Single Process Launcher Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
        System.exit(101);
    }
	
	/**
	 * Calls a class's main method on a new thread.
	 * @param c The name of the class to start.
	 * @throws OperationNotSupportedException - if there is a severe error in handling the launch command
	 */
	private static void launch(String c) throws OperationNotSupportedException {
	    
		Thread t = null;
		final String[] args = {ModuleModeEnum.LEARNER_MODE.getName()};
		
		switch(c) {
				
		case UMS_ARG:
		    
		    if(moduleThreads.containsKey(ModuleTypeEnum.UMS_MODULE)){
		       throw new OperationNotSupportedException("Unable to launch UMS module because that module has already been launched.");
		       
		    }else{
		          
		        if(logger.isInfoEnabled()){
		            logger.info("Launching " + c);
		        }
	            
    			moduleStatusManager.addModule(ModuleTypeEnum.UMS_MODULE);
    			t = new Thread("UMS Single Process Thread") {
    				@Override
                    public void run() {
    					try {
    					    progressDialog.update("loading " + c + " module...", 3);
    					    
    						Class<?> umsModuleClass = this.getContextClassLoader().loadClass("mil.arl.gift.ums.UMSModule");
    						Method umsMain = umsModuleClass.getMethod("main", String[].class);
    						umsMain.invoke(null, (Object)args);
    					} catch (Throwable e) {
    						logger.error("Caught exception while trying to execute UMS module", e);
    						errorExit("Could not start UMS module, check log file for more details.");
    					}
    				}
    			};
    			
    			progressDialog.update("building " + c + " dependencies...", 1);
    			
    			URL[] umsUrls = { };
    			ModuleClassLoader umsLoader = new ModuleClassLoader(umsUrls, t.getContextClassLoader());
    			List<String> umsDepend = buildProperties.getUmsDependencies();
    			umsLoader.handleDependencies(umsDepend);
    			t.setContextClassLoader(umsLoader);
                moduleThreads.put(ModuleTypeEnum.UMS_MODULE, t);
    			break;
		    }
		    
		case LMS_ARG:
		    
	          if(moduleThreads.containsKey(ModuleTypeEnum.LMS_MODULE)){
	               throw new OperationNotSupportedException("Unable to launch LMS module because that module has already been launched.");
	               
              }else{                  
                  
                  if(logger.isInfoEnabled()){
                      logger.info("Launching " + c);
                  }
                  
                  moduleStatusManager.addModule(ModuleTypeEnum.LMS_MODULE);
                  t = new Thread("LMS Single Process Thread") {
                      @Override
                      public void run() {
                          try {
                              progressDialog.update("loading " + c + " module...", 3);
                              
                              Class<?> lmsModuleClass = this.getContextClassLoader().loadClass("mil.arl.gift.lms.LmsModule");
                              Method lmsMain = lmsModuleClass.getMethod("main", String[].class);
                              lmsMain.invoke(null, (Object)args);
                          } catch (Throwable e) {
                              logger.error("Caught exception while trying to execute LMS module", e);
                              errorExit("Could not start LMS module, check log file for more details.");
                          }
                      }
                  };
                  
                  progressDialog.update("building " + c + " dependencies...", 1);
                  
                  URL[] lmsUrls = { };
                  ModuleClassLoader lmsLoader = new ModuleClassLoader(lmsUrls, t.getContextClassLoader());
                  List<String> lmsDepend = buildProperties.getLmsDependencies();
                  lmsLoader.handleDependencies(lmsDepend);
                  t.setContextClassLoader(lmsLoader);
                  moduleThreads.put(ModuleTypeEnum.LMS_MODULE, t);
                  break;
              }
	          
		case PED_ARG:
		    
            if(moduleThreads.containsKey(ModuleTypeEnum.PEDAGOGICAL_MODULE)){
                throw new OperationNotSupportedException("Unable to launch Pedagogical module because that module has already been launched.");
                
            }else{
                
                if(logger.isInfoEnabled()){
                    logger.info("Launching " + c);
                }
                
    			moduleStatusManager.addModule(ModuleTypeEnum.PEDAGOGICAL_MODULE);
    			t = new Thread("Ped Single Process Thread") {
    				@Override
                    public void run() {
    					try {
    					    progressDialog.update("loading " + c + " module...", 3);
    					    
    						Class<?> pedModuleClass = this.getContextClassLoader().loadClass("mil.arl.gift.ped.PedagogicalModule");
    						Method pedMain = pedModuleClass.getMethod("main", String[].class);
    						pedMain.invoke(null, (Object)args);
    					} catch (Throwable e) {
    						logger.error("Caught exception while trying to execute Ped module", e);
    						errorExit("Could not start Pedagogical module, check log file for more details.");
    					}
    				}
    			};
    			
    			progressDialog.update("building " + c + " dependencies...", 1);
    			
    			URL[] pedUrls = { };
    			ModuleClassLoader pedLoader = new ModuleClassLoader(pedUrls, t.getContextClassLoader());
    			List<String> pedDepend = buildProperties.getPedDependencies();
    			pedLoader.handleDependencies(pedDepend);
    			t.setContextClassLoader(pedLoader);
                moduleThreads.put(ModuleTypeEnum.PEDAGOGICAL_MODULE, t);
    			break;
            }
            
		case LEARNER_ARG:
		    
		    if(moduleThreads.containsKey(ModuleTypeEnum.LEARNER_MODULE)){
                throw new OperationNotSupportedException("Unable to launch Learner module because that module has already been launched.");
                
            }else{
                
                if(logger.isInfoEnabled()){
                    logger.info("Launching " + c);
                }
                
    			moduleStatusManager.addModule(ModuleTypeEnum.LEARNER_MODULE);
    			t = new Thread("Learner Single Process Thread") {
    				@Override
                    public void run() {
    					try {
    					    progressDialog.update("loading " + c + " module...", 3);
    					    
    						Class<?> learnerModuleClass = this.getContextClassLoader().loadClass("mil.arl.gift.learner.LearnerModule");
    						Method learnerMain = learnerModuleClass.getMethod("main", String[].class);
    						learnerMain.invoke(null, (Object)args);
    					} catch (Throwable e) {
    						logger.error("Caught exception while trying to execute Learner module", e);
    						errorExit("Could not start Learner module, check log file for more details.");
    					}
    				}
    			};
    			
    			progressDialog.update("building " + c + " dependencies...", 1);
    			
    			URL[] learnerUrls = { };
    			ModuleClassLoader learnerLoader = new ModuleClassLoader(learnerUrls, t.getContextClassLoader());
    			List<String> learnerDepend = buildProperties.getLearnerDependencies();
    			learnerLoader.handleDependencies(learnerDepend);
    			t.setContextClassLoader(learnerLoader);
                moduleThreads.put(ModuleTypeEnum.LEARNER_MODULE, t);
    			break;
            }
		    
		case SENSOR_ARG:
		    
		    if(moduleThreads.containsKey(ModuleTypeEnum.SENSOR_MODULE)){
                throw new OperationNotSupportedException("Unable to launch Sensor module because that module has already been launched.");
                
            }else{
                
                if(logger.isInfoEnabled()){
                    logger.info("Launching " + c);
                }
                
    			moduleStatusManager.addModule(ModuleTypeEnum.SENSOR_MODULE);
    			t = new Thread("Sensor Single Process Thread") {
    				@Override
                    public void run() {
    					try {
    					    progressDialog.update("loading " + c + " module...", 3);
    					    
    						Class<?> sensorModuleClass = this.getContextClassLoader().loadClass("mil.arl.gift.sensor.SensorModule");
    						Method sensorMain = sensorModuleClass.getMethod("main", String[].class);
    						sensorMain.invoke(null, (Object)args);
    					} catch (Throwable e) {
    						logger.error("Caught exception while trying to execute Sensor module", e);
    						errorExit("Could not start Sensor module, check log file for more details.");
    					}
    				}
    			};
    			
    			progressDialog.update("building " + c + " dependencies...", 1);
    			
    			URL[] sensorUrls = { };
    			ModuleClassLoader sensorLoader = new ModuleClassLoader(sensorUrls, t.getContextClassLoader());
    			List<String> sensorDepend = buildProperties.getSensorDependencies();
    			sensorLoader.handleDependencies(sensorDepend);
    			t.setContextClassLoader(sensorLoader);
                moduleThreads.put(ModuleTypeEnum.SENSOR_MODULE, t);
    			break;
            }
		    
		case DOMAIN_ARG:
		    
		    if(moduleThreads.containsKey(ModuleTypeEnum.DOMAIN_MODULE)){
                throw new OperationNotSupportedException("Unable to launch Domain module because that module has already been launched.");
                
            }else{
                
                if(logger.isInfoEnabled()){
                    logger.info("Launching " + c);
                }
                
    			moduleStatusManager.addModule(ModuleTypeEnum.DOMAIN_MODULE);
    			t = new Thread("Domain Single Process Thread") {
    				@Override
                    public void run() {
    					try {
    					    progressDialog.update("loading " + c + " module...", 3);
    					    
    						Class<?> domainModuleClass = this.getContextClassLoader().loadClass("mil.arl.gift.domain.DomainModule");
    						Method domainMain = domainModuleClass.getMethod("main", String[].class);
    						domainMain.invoke(null, (Object)args);
    					} catch (Throwable e) {
    						logger.error("Caught exception while trying to execute Domain module", e);
    						errorExit("Could not start Domain module, check log file for more details.");
    					}
    				}
    			};
    			
    			progressDialog.update("building " + c + " dependencies...", 1);
    			
    			URL[] domainUrls = { };
    			ModuleClassLoader domainLoader = new ModuleClassLoader(domainUrls, t.getContextClassLoader());
    			List<String> domainDepend = buildProperties.getDomainDependencies();
    			domainLoader.handleDependencies(domainDepend);
    			t.setContextClassLoader(domainLoader);
                moduleThreads.put(ModuleTypeEnum.DOMAIN_MODULE, t);
    			break;
            }
		    
		case GATEWAY_ARG:
		    
		    //SPL should not launch the gateway module in server mode because server mode causes a java web start jnlp file to be
		    //downloaded on the user's machine when a course starts (if the course needs a gateway module instance on the user's computer)
		    if(deploymentMode == DeploymentModeEnum.SERVER){
		        logger.warn("Ignoring the request to start the Gateway module because the gateway module instance will be handled on a per course basis using Java Web Start solution.");
		        break;
		    }
		    
		    if(moduleThreads.containsKey(ModuleTypeEnum.GATEWAY_MODULE)){
                throw new OperationNotSupportedException("Unable to launch Gateway module because that module has already been launched.");
                
            }else{
                
                if(logger.isInfoEnabled()){
                    logger.info("Launching " + c);
                }
                
    			moduleStatusManager.addModule(ModuleTypeEnum.GATEWAY_MODULE);
    			t = new Thread("Gateway Single Process Thread") {
    				@Override
                    public void run() {
    					try {
    					    progressDialog.update("loading " + c + " module...", 3);
    					    
    						Class<?> gatewayModuleClass = this.getContextClassLoader().loadClass("mil.arl.gift.gateway.GatewayModule");
    						Method gatewayMain = gatewayModuleClass.getMethod("main", String[].class);
    						gatewayMain.invoke(null, (Object)args);
    					} catch (Throwable e) {
    						logger.error("Caught exception while trying to execute Gateway module", e);
    						errorExit("Could not start Gateway module, check log file for more details.");
    					}
    				}
    			};
    			
    			progressDialog.update("building " + c + " dependencies...", 1);
    			
    			URL[] gatewayUrls = { };
    			ModuleClassLoader gatewayLoader = new ModuleClassLoader(gatewayUrls, t.getContextClassLoader());
    			List<String> gatewayDepend = buildProperties.getGatewayDependencies();
    			gatewayLoader.handleDependencies(gatewayDepend);
    			t.setContextClassLoader(gatewayLoader);
                moduleThreads.put(ModuleTypeEnum.GATEWAY_MODULE, t);
    			break;
            }
		    
		case TUTOR_ARG:
		    
		    if(moduleThreads.containsKey(ModuleTypeEnum.TUTOR_MODULE)){
                throw new OperationNotSupportedException("Unable to launch Tutor module because that module has already been launched.");
            }else if(SingleProcessProperties.getInstance().getLessonLevel() == LessonLevelEnum.RTA){
                // don't start the tutor module
                break;
            }
	          
            if(logger.isInfoEnabled()){
                logger.info("Launching " + c);
            }
            
			moduleStatusManager.addModule(ModuleTypeEnum.TUTOR_MODULE);
			t = new Thread("Tutor Single Process Thread") {
				
			    @Override
			    public void run() {
			        
			        if(logger.isInfoEnabled()){
			            logger.info("Starting Tutor");
			        }
   
			        try {
			            progressDialog.update("loading " + c + " module...", 3);
			            
			            tutorWebServer = configureServer(System.getProperty("user.dir") + File.separator + "config" + File.separator + "tutor" + File.separator + "server");
			            
			            Method startMethod = tutorWebServer.getClass().getMethod("start", (Class<?>[]) null);
			            startMethod.invoke(tutorWebServer, (Object[]) null);

			            Method joinMethod = tutorWebServer.getClass().getMethod("join", (Class<?>[]) null);
			            joinMethod.invoke(tutorWebServer, (Object[]) null);
			            
			        } catch (Throwable e) {
			        	logger.error("Caught exception while trying to execute Tutor module", e);
			        	errorExit("Could not start Tutor module, check log file for more details.");
					}
			    } 
			};
			
			progressDialog.update("building " + c + " dependencies...", 1);
			
			URL[] tutorUrls = {};
			ModuleClassLoader tutorLoader = new ModuleClassLoader(tutorUrls, t.getContextClassLoader());
			tutorLoader.addFileDirectory("./config/tutor/server/");
			tutorLoader.addFileDirectory("./config/tutor/server/resources/");
			List<String> tutorDepend = buildProperties.getTutorDependencies();
			tutorLoader.handleDependencies(tutorDepend);
			t.setContextClassLoader(tutorLoader);
			moduleThreads.put(ModuleTypeEnum.TUTOR_MODULE, t);
			break;
			
	   case GAS_ARG:
	            
           if(gasState.isStarted()) {
               break;
           }
           
           if(logger.isInfoEnabled()){
               logger.info("Launching " + c);
           }
           
           t = new Thread("GAS Single Process Thread") {
               @Override
               public void run() {
                   
                   if(logger.isInfoEnabled()){
                       logger.info("Starting GAS");
                   }
                   progressDialog.update("starting the server...", 3);
                   
                   try{
                       gasWebServer = configureServer(System.getProperty("user.dir") + File.separator + "config" + File.separator + "tools" + File.separator + "gas");

                       long preStartTime = System.currentTimeMillis();
                       Method startMethod = gasWebServer.getClass().getMethod("start", (Class<?>[]) null);
                       startMethod.invoke(gasWebServer, (Object[]) null);
                       if(logger.isInfoEnabled()){
                           logger.info("It took " +(System.currentTimeMillis()-preStartTime)+ " ms to call start on GAS.");
                       }
                       
                       //Note: using the 'join' method (like is done for the tutor web server) appeared to hang 
                       //      forever on the invoke call 
                       long preInvokeTime = System.currentTimeMillis();
                       Method stateMethod = gasWebServer.getClass().getMethod("isStarted", (Class<?>[]) null);
                       Boolean isStarted = (Boolean) stateMethod.invoke(gasWebServer, (Object[]) null);
                       while(!isStarted){
                           if(logger.isInfoEnabled()){
                               logger.info("Waiting for GAS server to start.");
                           }
                           progressDialog.update("waiting for the server...", 1);
                           Thread.sleep(10);
                           isStarted = (Boolean) stateMethod.invoke(gasWebServer, (Object[]) null);
                       }
                       if(logger.isInfoEnabled()){
                           logger.info("It took " +(System.currentTimeMillis()-preInvokeTime)+ " ms to call isStarted on GAS.");
                       }
                       
                       gasState.start();
                       
                       synchronized (gasState) {
                           gasState.notifyAll();
                       }
                       
                       if(logger.isInfoEnabled()){
                           logger.info("Finished starting GAS");
                       }

                   } catch (Throwable e) {
                       logger.error("Caught exception while trying to execute GAS", e);
                       errorExit("Could not start the GIFT Admin Server (GAS). Check the GAS log file for more details.\n\n"+
                               "Tip:  Did you happen to use the GIFT Control Panel which may have started the GAS already.\nCheck to see if there is a command prompt application running with the title of \"GIFT Admin Server\".\n"+
                               "If so, close it before starting GIFT again.");
                   }
               }

           };
           
           progressDialog.update("building " + c + " dependencies...", 1);
           
           URL[] gasUrls = {};
           ModuleClassLoader gasLoader = new ModuleClassLoader(gasUrls, t.getContextClassLoader());
           gasLoader.addFileDirectory("./config/tools/gas/");
           gasLoader.addFileDirectory("./config/tools/gas/resources/");
           List<String> gasDepend = buildProperties.getDomainDependencies(); //this matches launchProcess.bat GAS 'ClassPathExtension' variable
           gasLoader.handleDependencies(gasDepend);
           t.setContextClassLoader(gasLoader);
           
           break;
		    
		case ACTIVEMQ_ARG:
		    
			if(activeMQState.isStarted()) {
				break;
			}
	         
			if(logger.isInfoEnabled()){
			    logger.info("Launching " + c);
			}
            
			t = new Thread("ActiveMQ Single Process Thread") {
				@Override
                public void run() {
					//configure and launch activemq
				    //Note: when starting ActiveMQ with SPL, ActiveMQ will use the SPL log4j properties file.  (ticket #561)
				    
				    if(logger.isInfoEnabled()){
				        logger.info("Starting ActiveMQ");
				    }
                    progressDialog.update("loading " + c + "...", 3);
                    
					try {					    					    
						
						//create the broker
						String configUri = "xbean:file:external/activemq/conf/activemq.xml";
						URI brokerUri = new URI(configUri);
						Class<?> brokerFactoryClass = this.getContextClassLoader().loadClass("org.apache.activemq.broker.BrokerFactory");
						Method createBroker = brokerFactoryClass.getMethod("createBroker", URI.class);
						Object broker = createBroker.invoke(null, brokerUri);
						
						//start the broker
						if(logger.isInfoEnabled()){
						    logger.info("Starting ActiveMQ broker");
						}
						Method start = broker.getClass().getMethod("start");
						start.invoke(broker);
					
						Method wait = broker.getClass().getMethod("waitUntilStarted");
						wait.invoke(broker);
						if(logger.isInfoEnabled()){
						    logger.info("ActiveMQ has been started.");
						}
						activeMQState.start();
						
						synchronized (activeMQState) {
						    activeMQState.notifyAll();
                        }
						
					} catch (Throwable e) {
						logger.error("Caught exception while trying to start ActiveMQ", e);
						errorExit("Could not start ActiveMQ, check log file for more details.");
					}
				}
			};
//			t.setContextClassLoader(new ClassLoader() {
//            });
			break;
		default:
		    if(logger.isInfoEnabled()){
		        logger.info("Unrecognized launch command " + c);
		    }
			break;
		}
		
		if(t != null) {
			t.start();
		}
	}
	
	/**
	 * Loads the configuration files for the server with the given base folder path and loads it into an 
	 * Jetty instance that is embedded in the current thread and uses its classloader. This is used to allow
	 * servers representing multiple GIFT applications to coexist within the same main Java process.
	 * 
	 * @param serverBasePath the path to the base folder containing the server's configuration files
	 * @return the configured server. Will only be null if an issue occurs during configuration.
	 * @throws Exception if an error is thrown during configuration.
	 */
    private static Object configureServer(String serverBasePath) throws Exception{
        
        Object server = null;
        
        //needed because gas log4j uses gift.home env variable
        //Note: must be done before 'Object resource = newResourceMethod.invoke(null, xmlFile);' which needs the env variable
        System.setProperty("gift.home", System.getProperty("user.dir"));
    
        Class<?> resourceClass = Thread.currentThread().getContextClassLoader().loadClass("org.eclipse.jetty.util.resource.Resource");
        Method newResourceMethod = resourceClass.getMethod("newResource", File.class);
        Method getInputSteamMethod = resourceClass.getMethod("getInputStream", (Class<?>[]) null);
        
        Class<?> xmlConfigurationClass = Thread.currentThread().getContextClassLoader().loadClass("org.eclipse.jetty.xml.XmlConfiguration");
        Method getPropertiesMethod = xmlConfigurationClass.getMethod("getProperties", (Class<?>[]) null);
        
        File xmlFile = new File(serverBasePath + File.separator + "etc/jetty.xml"); 
        
        progressDialog.update("configuring the server...", 3);
        
        long preConfigureTime = System.currentTimeMillis();       
            
        Object resource = newResourceMethod.invoke(null, xmlFile);
        Object inputStream = getInputSteamMethod.invoke(resource, (Object[]) null);
       
        Object xmlConfig = xmlConfigurationClass.getConstructor(inputStream.getClass().getSuperclass()).newInstance(inputStream);
        
        Object properties = getPropertiesMethod.invoke(xmlConfig, (Object[]) null);

        Method putMethod = properties.getClass().getMethod("put", new Class<?>[]{Object.class, Object.class});
        
        /* Needed by some Jetty XML configurations */
        putMethod.invoke(properties, "jetty.base", serverBasePath);
        
        //make sure the tutor module will use single process mode (cause of ticket https://gifttutoring.org/issues/551)
        putMethod.invoke(properties, "isSingleProcess", "true");
        System.setProperty("isSingleProcess", "true");
        
        //needed because tutor/server log4j uses gift.home env variable
        putMethod.invoke(properties, "gift.home", System.getProperty("user.dir"));
            
        Method configurationMethod = xmlConfigurationClass.getMethod("configure", (Class<?>[]) null);
        server = configurationMethod.invoke(xmlConfig, (Object[]) null);
        
        if(logger.isInfoEnabled()){
            logger.info("It took " +(System.currentTimeMillis()-preConfigureTime)+ " ms to call configure the server at: " + serverBasePath);
        }
        
        return server;
    }
    
    /**
     * Gracefully ends the tutor web server (jetty).  This is needed anytime the tutor module
     * is stopped using the kill module messages.  While the tutor module process will end the servlet hosting
     * the tutor module is in another process.
     */
    private static void killTutorWebserver() {
        
        try{
            // if killing the tutor module thread, stop the Tutor module thread's server 
            if(logger.isInfoEnabled()){
                logger.info("Stopping "+ModuleTypeEnum.TUTOR_MODULE+" module thread web server.");
            }
            
            Method stopMethod = tutorWebServer.getClass().getMethod("stop", (Class<?>[]) null);
            stopMethod.invoke(tutorWebServer, (Object[]) null);
            
        } catch(Exception e){
            logger.warn("Caught an exception while stopping "+ModuleTypeEnum.TUTOR_MODULE+" module thread web server.", e);
        }
    }

    /**
     * Send a kill module message to each module queue for each module SPL was told to start in the beginning. 
     */
    private static void killModules() {
        
        if(session == null) {
            logger.warn("Unable to kill modules because network session is null.  If SPL closes correctly "+
                    "than this is not an issue and this was mostly caused by some upstream error like SPL not starting at all.");
            return;
        }

        //need to copy to avoid concurrent modification exceptions
        List<ModuleStatus> modules = new ArrayList<>(moduleStatusManager.getModules());
        for (ModuleStatus module : modules) {

            String moduleQueue = module.getQueueName();
            
            if(logger.isInfoEnabled()){
                logger.info("Creating connection to "+moduleQueue+" in order to send a kill message to it.");
            }
            
            session.createSubjectQueueClient(moduleQueue, module.getModuleType(), false);
            
            if(logger.isInfoEnabled()){
                logger.info("Notifying "+moduleQueue+" to terminate.");
            }

            session.sendMessage(moduleQueue, null, MessageTypeEnum.KILL_MODULE, null);
            
            //wait for module thread to complete before ordering other modules to be killed.
            //(fix for ticket #551)
            Thread thread = moduleThreads.get(module.getModuleType());
            try{
                if(logger.isInfoEnabled()){
                    logger.info("Joining "+module.getModuleType()+" module thread");
                }
                
                if(module.getModuleType() == ModuleTypeEnum.TUTOR_MODULE){
                    killTutorWebserver();
                }
                
                thread.join(10000);
                if(logger.isInfoEnabled()){
                    logger.info(module.getModuleType()+" module thread ended gracefully");
                }
                
            } catch (@SuppressWarnings("unused") InterruptedException ex) {
                if(logger.isInfoEnabled()) {
                    logger.info(module.getModuleType()+" module thread ended with an interrupt.");
                }
            }
        }
        
        if(logger.isInfoEnabled()){
            logger.info("Finished waiting for modules to terminate.");
        }
        
        session.cleanup(false);
    }
    
    /**
     * Return whether or not the arguments contains the specified value.
     * 
     * @param value the value to find in the args.  Case sensitive.
     * @param args the arguments array to search for the value in
     * @return true if the value was found in the args.
     */
    private static boolean hasArg(String value, String[] args){
        
        if(value == null){
            return false;
        }
        
        for(String arg : args){
            
            if(value.equals(arg)){
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Handles launching ActiveMQ and the GIFT modules specified by the args array.
     * 
     * @param args the arguments to SPL that include the modules to launch
     * @param needActiveMQ whether or not ActiveMQ should be started before the modules are started
     * @throws Exception if there was a problem launching any application specified by the args
     */
    private static void handleModules(String[] args, boolean needActiveMQ) throws Exception{
        
        if(needActiveMQ){
            
            long preLaunch = System.currentTimeMillis();
            launch(ACTIVEMQ_ARG);
            synchronized (activeMQState) {                     
                activeMQState.wait();
            }

            if(logger.isInfoEnabled()){
                logger.info("It took "+(System.currentTimeMillis()-preLaunch)+" ms for ActiveMQ to start.");
                logger.info("ActiveMQ has been started, continuing on to launch modules...");
            }
            
            session = new NetworkSession("SingleProcessLauncher", ModuleTypeEnum.MONITOR_MODULE, "SPL_Queue",
                    true, "SPL_Queue_Inbox", properties.getBrokerURL(), MessageEncodingTypeEnum.BINARY,
                    new MessageHandler() {
                @Override
                public boolean processMessage(Message message) {
                    return true;
                }
            }, false, false);
            
            // Need to catch when a tutor module is delivered a kill module message in order to end the jetty server.  
            // The easiest way to do this is to listen to all messages.  The monitor topic is delivered all messages via GIFT
            // camel logic.
            // Note: destroyOnCleanup - not sure if it should be true or false.  If this network session is cleaned up
            // which currently happens in killAllModules method, should the monitor topic be destroyed.  I believe yes
            // because this means that SPL is closing, which means ActiveMQ is closing.
            session.createSubjectTopicClient(SubjectUtil.MONITOR_TOPIC, new MessageHandler() {
                
                @Override
                public boolean processMessage(Message message) {

                    if(message.getMessageType() == MessageTypeEnum.KILL_MODULE) {
                        // is this for the tutor module that SPL started?
                        
                        for(ModuleStatus moduleStatus : moduleStatusManager.getModules()) {
                            
                            if(moduleStatus.getModuleType() == ModuleTypeEnum.TUTOR_MODULE) {
                                
                                if(moduleStatus.getQueueName().equals(message.getDestinationQueueName())){
                                    killTutorWebserver();
                                    break;
                                }
                            }
                        }
                    }
                    return false;
                }
            }, true);
        }
        
        // Setup ActiveMQ URL to check if the modules are ready
        moduleStatusManager.setMessageBrokerUri(properties.getBrokerURL());
        
        for(String launchCommand : args) {
            
            try{
                if(!NOT_MODULE_ARGS.contains(launchCommand)){
                    launch(launchCommand);
                }
            }catch(Exception e){
                logger.error("Caught exception while executing launch command of "+launchCommand+".", e);
                throw e;  //cause SPL to terminate
            }
        }
        
        // Wait for all modules to be ready
        progressDialog.update("initializing modules...", 3);
        long preModuleTime = System.currentTimeMillis();
        long elapsed = 0;
        long timeoutTime = properties.getTimeout() * 1000; // needs to be in milliseconds, property is given in seconds
        if(logger.isInfoEnabled()){
            logger.info("Waiting for modules to initialize (timeout set to "+properties.getTimeout()+" seconds).");
        }
        boolean timeout = false;
        while(!moduleStatusManager.isSystemReady()) {
            
            if((elapsed % 500) == 0 && progressDialog.getCurrentProgress() < 80) {
                // steadily advance the progress bar
                progressDialog.update("waiting for modules...", 1);
            }
            
            Thread.sleep(50);
            elapsed += 50;
            if(elapsed > timeoutTime) {
                timeout = true;
                break;
            }
        }
        if(logger.isInfoEnabled()){
            logger.info("It took "+(System.currentTimeMillis()-preModuleTime)+" ms for all modules to start.");
        }
        
        if(timeout){
            //gather modules not ready
            String modules = "{";
            for(ModuleTypeEnum mType : moduleStatusManager.getModulesNotReady()){
                modules += " " + mType+",";
            }
            logger.error("GIFT timed out.  The following modules are not ready = "+modules+".");
            errorExit("GIFT timed out while trying to initialize.\n\n"+
                    "Reason: The following module(s) did not start in time "+modules+" }.\n\n"+
                    "Details: the most common reasons for this problem are\n"+
                    "1) A GIFT module has presented a dialog that needs your attention.\n"+
                    "2) A GIFT module has had a severe problem while starting.\n"+
                    "3) It is taking longer than normal to start GIFT on your computer.  Try increasing the 'Timeout' property value in 'GIFT\\config\\tools\\spl\\spl.properties'.\n\n"+
                    "Finally, check the latest 'spl' prefixed named log file in GIFT\\output\\logger\\tools\\ for more details.");
        }

    }

    /**
     * Main used to launch SPL. 
     * 
     * @param args Class names of all modules to launch, separated by a space.
     */
    public static void main(String[] args) {    	
    	
		try {
            long startTime = System.currentTimeMillis();
            
            // Start Sequence
            //i.a ActiveMQ then modules
            //i.b GAS anytime
            //ii. Webpage after i.a and i.b finish 
            
            progressDialog.show();

            boolean needActiveMQ = hasArg(ACTIVEMQ_ARG, args);
            boolean needGAS = hasArg(GAS_ARG, args);
                    
            Thread handleGASThread = null;
            if(needGAS){
                handleGASThread = new Thread("Handle GAS"){
                    
                    @Override
                    public void run(){
                        
                        try{
                            if(deploymentMode != DeploymentModeEnum.SIMPLE){
                                
                                long preLaunch = System.currentTimeMillis();
                                launch(GAS_ARG);
                                synchronized (gasState) {                     
                                    gasState.wait();
                                }
                                if(logger.isInfoEnabled()){
                                    logger.info("It took "+(System.currentTimeMillis()-preLaunch)+" ms for GAS to start.");
                                }
                                
                            }else{
                                logger.warn("Skipping launching GAS because running in "+deploymentMode+".");
                            }
                        }catch(Exception e) {
                            logger.error("Caught exception while trying to initialize single process mode", e);
                            errorExit("Single process mode failed to initialize, check log file for more details.");
                        }
                    }
                };
                handleGASThread.start();
            }
            
            Thread handleModulesThread = new Thread("Handle Modules"){
                
                @Override
                public void run(){
                    
                    try{
                        handleModules(args, needActiveMQ);
                    }catch(Exception e) {
                        logger.error("Caught exception while trying to initialize single process mode", e);
                        errorExit("Single process mode failed to initialize, check log file for more details.");
                    }
                }
            };
            handleModulesThread.start();
            
            //wait for thread(s) to finish
            try{
                progressDialog.update("waiting for modules...", 1);
                handleModulesThread.join();
                if(handleGASThread != null){
                    progressDialog.update("waiting for the server...", 85);
                    handleGASThread.join();
                }
            }catch(Exception e) {
                logger.error("Caught exception while waiting for launch threads to finish", e);
                errorExit("Single process mode failed to initialize, check log file for more details.");
            }

            // Launch the GIFT webpage (i.e. TUI or Dashboard) when GIFT is ready.
            if(logger.isInfoEnabled()){
                logger.info("GIFT initialized after "+(System.currentTimeMillis()-startTime)+" ms, launching GIFT webpage.");
            }
            progressDialog.update("launching webpage...", 99);
            launchGIFTWebpage();
            
            // Wait for exit.
            synchronized (Thread.currentThread()) {
                Thread.currentThread().wait();
            }
            
            killModules();
            
            moduleStatusManager.close();

                 
        } catch(Throwable e) {
            logger.error("Caught exception while trying to initialize single process mode", e);
            errorExit("Single process mode failed to initialize, check log file for more details.");
        }
	}
    
//    public static class SimpleClassLoader extends ClassLoader {
//        
//        private Hashtable classes = new Hashtable();
//
//        // NEW
//        private String myPackage;
//        private String codeBase;
//
//        public SimpleClassLoader(String myPackage, String codeBase) {
//            // NEW
//            this.myPackage = myPackage;
//            this.codeBase = codeBase;
//        }
//
//        /**
//         * This sample function for reading class implementations reads
//         * them from the local file system
//         */
//        private byte getClassImplFromDataBase(String className)[] {
//            System.err.println("        >>>>>> Fetching the implementation of " + className);
//            byte result[];
//
//
//            // NEW: convert package seperators '.' to directory seperators
//            StringBuffer sb = new StringBuffer(className);
//            for (int i=0, n=sb.length(); i < n; i++) {
//                if (sb.charAt(i) == '.') {
//                    sb.setCharAt(i, File.separatorChar);
//                }
//            }
//            className = sb.append(".class").toString();
//            System.err.println("New className: " + className); // to double check
//
//            try {
//                // EDIT: Moved some stuff up a few lines
//                // FileInputStream fi = new FileInputStream("store\\"+className+".impl");
//                FileInputStream fi = new FileInputStream(codeBase + className);
//                result = new byte[fi.available()];
//                fi.read(result);
//                return result;
//            } catch (Exception e) {
//
//                /*
//                 * If we caught an exception, either the class wasnt found or it
//                 * was unreadable by our process.
//                 */
//                return null;
//            }
//        }
//
//        /**
//         * This is a simple version for external clients since they
//         * will always want the class resolved before it is returned
//         * to them.
//         */
//        public Class loadClass(String className) throws ClassNotFoundException {
//            return (loadClass(className, true));
//        }
//
//        /**
//         * This is the required version of loadClass which is called
//         * both from loadClass above and from the internal function
//         * FindClassFromClass.
//         */
//        public synchronized Class loadClass(String className, boolean resolveIt) throws ClassNotFoundException {
//            Class result;
//            byte classData[];
//
//            System.err.println("        >>>>>> Load class : " + className);
//
//            /* Check our local cache of classes */
//            result = (Class)classes.get(className);
//            if (result != null) {
//                System.err.println("        >>>>>> returning cached result.");
//                return result;
//            }
//
//            // NEW: Added the if startsWith
//            if (!className.startsWith(myPackage)) {
//                /* Check with the primordial class loader */
//                try {
//                    result = super.findSystemClass(className);
//                    System.err.println("        >>>>>> returning system class (in CLASSPATH).");
//                    return result;
//                } catch (ClassNotFoundException e) {
//                    System.err.println("        >>>>>> Not a system class.");
//                }
//            }
//
//            /* Try to load it from our repository */
//            classData = getClassImplFromDataBase(className);
//            if (classData == null) {
//                throw new ClassNotFoundException();
//            }
//
//            /* Define it (parse the class file) */
//            result = defineClass(className, classData, 0, classData.length);
//            if (result == null) {
//                throw new ClassFormatError();
//            }
//
//            if (resolveIt) {
//                resolveClass(result);
//            }
//
//            classes.put(className, result);
//            System.err.println("        >>>>>> Returning newly loaded class.");
//            return result;
//        }
//    }
}
