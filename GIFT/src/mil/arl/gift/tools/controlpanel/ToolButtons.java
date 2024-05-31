/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.controlpanel;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.PackageUtil;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.io.FileUtil;
import mil.arl.gift.common.io.PlatformUtils;
import mil.arl.gift.common.io.ZipUtils;
import mil.arl.gift.common.io.PlatformUtils.SupportedOSFamilies;
import mil.arl.gift.common.util.JOptionPaneUtil;
import mil.arl.gift.tools.remote.LaunchConstants;

/**
 * Creates all of the buttons, text, and action listeners
 * 
 * @author bzahid
 */
public class ToolButtons {

	private static Logger logger = LoggerFactory.getLogger(ToolButtons.class);

	/** 
	 * Tool Tip Text for buttons 
	 */
	private static final String MAT_TOOLTIP = ""
			+ "<html>Launch the MAT desktop application "
			+ "which is used to author metadata<br>files"
			+ " that contain attributes describing domain"
			+ " content files.</html>";

	private static final String MON_TOOLTIP = ""
			+ "<html>Launch the Monitor desktop application"
			+ " that can be used to observe and debug<br>module level interactions.</html>";

	private static final String DAT_TOOLTIP = ""
			+ "<html>Launch the DAT desktop application used "
			+ "to author DKFs that the Domain Module<br>uses "
			+ "to configure the assessment logic for a lesson"
			+ " in a course.</html>";

	private static final String GAT_TOOLTIP = ""
			+ "<html>Use the Course Creator through the GIFT Dashboard to author courses.</html>";

	private static final String ERT_TOOLTIP = ""
			+ "<html>Launch the ERT in a web browser. The ERT is used to create<br>" +
			  "reports based on output from one or more GIFT course executions.<br/>"
              + "<b>Note:</b> it can take up to a minute for the GAS to start before the ERT is shown.<br/>If a white webpage is shown, try refreshing the webpage a few times.</html>";
		
	private static final String EXP_TOOLTIP = ""
			+ "<html>Launch the GIFT Export Tool desktop application "
			+ "which can be used to create<br>GIFT content (e.g. one or more courses) that can be"
			+ " shared with others.</html>";
		
	private static final String PCAT_TOOLTIP = ""
			+ "<html>Launch the PCAT desktop application which is used"
			+ " to author Pedagogical model<br>configurations used by "
			+ "the Pedagogical module such as the Engine for Management<br>of Adaptive Pedagogy (eMAP).</html>";

	private static final String SCAT_TOOLTIP = ""
			+ "<html>Launch the SCAT desktop application which is used "
			+ "to author sensor Configuration files<br>that the Sensor"
			+ " Module uses to configure sensors, filters and writers."
			+ "</html>";

	private static final String CAT_TOOLTIP = ""
			+ "<html>Launch the CAT dekstop application which is used to "
			+ "author course files<br>that describe the flow of execution"
			+ " for a user in GIFT.</html>";
		
	private static final String LCAT_TOOLTIP = ""
			+ "<html>Launch the LCAT desktop application which is used to"
			+ " author learner Configuration files<br>used by the Learner "
			+ "Module to configure the learner model sensor data pipeline."
			+ "</html>";
		
	private static final String SWB_TOOLTIP = ""
			+ "<html>Launch the SIMILE Workbench Tool which is used to author<br>" +
			"SIMILE assessment engine configuration files (.ixs).</html>";

	private static final String ASAT_TOOLTIP = ""
			+ "<html>Launch the AutoTutor Script Authoring Tool which is used to author<br>" +
			  "autotutor sessions.</html>";
	
    private static final String TRADEM_TOOLTIP = ""
            + "<html>Launch the TRADEM webpage which is used to create expert<br>" +
              "models based on provided content.</html>";
    
    private static final String DASH_TOOLTIP = ""
            + "<html>Launch the GIFT Dashboard webpage which is used to login <br>" +
              "to the GIFT application.<br/>"
              + "<b>Note:</b> it can take up to a minute for the GAS to start before the Dashboard is shown.<br/>If a white webpage is shown, try refreshing the webpage a few times.</html>";
    
    private static final String WRAP_TOOLTIP = ""
            + "<html>Launch the GIFT Wrap webpage which is used to author <br>" +
              "course objects for external applications.<br/>"
              + "<b>Note:</b> it can take up to a minute for the GAS to start before GIFT Wrap is shown.<br/>If a white webpage is shown, try refreshing the webpage a few times.</html>";
    
    private static final String UMS_CLEAR_USER_DATA_TOOLTIP = 
            "<html>Clear UMS database entries created as the result of executing user and domain sessions.</html>";
    
    private static final String UMS_CLEANUP_IDS_DATA_TOOLTIP =
            "<html>Cleanup the UMS database Id generator tables by setting the next id for each generator to the max id found + 1."
            + " Sometimes these table values become out of sync with the actual highest id value being used elsewhere in the database."
            + " This is often a result of deleting database entries such as surveys or users.</html>";
    
    private static final String UMS_RECREATE_DB_TOOLTIP =
            "<html>Recreate the UMS database tables.  This is useful for when the database schemaM has changed "
            +"(e.g. adding a new column to a table)</html>";
    
    private static final String UMS_RESET_DB_TOOLTIP =
            "<html>Reset the UMS database back to the original content for this release of GIFT.<br>"
            +"Note: this will delete all entries in the UMS database created after GIFT was installed.</html>";
    
    private static final String LMS_CLEAR_USER_DATA_TOOLTIP = 
            "<html>Clear LMS database entries created as the result of executing user and domain sessions.</html>";
    
    private static final String LMS_RECREATE_DB_TOOLTIP =
            "<html>Recreate the LMS database tables.  This is useful for when the database schemaM has changed "
            +"(e.g. adding a new column to a table)</html>";
    
    private static final String LMS_RESET_DB_TOOLTIP =
            "<html>Reset the LMS database back to the original content for this release of GIFT.<br>"
            +"Note: this will delete all entries in the LMS database created after GIFT was installed.</html>";
    
    private static final String CLEAR_LOGGER_SENSOR_DATA_TOOLTIP =
            "<html>Permanently deletes logger, sensor and JUNIT testResults files.</html>";
    
    /** the tool tip for the clear unused domain session message log file button */
    private static final String CLEAR_UNUSED_DS_LOG_TOOLTIP =
            "<html>Permanently deletes domain session message log files that are not referenced by any published course (GIFT experiment or LTI).</html>";
    
    private static final String CREATE_SIMPLE_LOGIN_USERS_TOOLTIP =
            "<html>Creates a set of user ids in the UMS db for logging into the Simple Login webpage.</html>";
    
    public static final String DEFAULT_DASH_BUTTON_TEXT = "<html><b>GIFT Dashboard</b></html>";
    public static final String WAITING_DASH_BUTTON_TEXT = "<html><b>GIFT Dashboard</b> <i>(waiting for GAS...)</i></html>";
    
    public static final String DEFAULT_ERT_BUTTON_TEXT = "Event Report Tool (ERT)";
    public static final String WAITING_ERT_BUTTON_TEXT = "<html>Event Report Tool (ERT) <i>(waiting for GAS...)</i></html>";
    
    public static final String DEFAULT_WRAP_BUTTON_TEXT = "<html><b>GIFT Wrap</b></html>";
    public static final String WAITING_WRAP_BUTTON_TEXT = "<html><b>GIFT Wrap</b> <i>(waiting for GAS...)</i></html>";

	/** Time taken to establish connection to the GAS and ActiveMQ */
	private static final int GAS_WAIT = 50;
	private static final int ACTIVEMQ_INITIAL_WAIT = 3000;
	private static final int ACTIVEMQ_WAIT = 500;
	private static final long GAS_MAX_WAIT_SEC = 60;
	private static final long ACTIVEMQ_MAX_WAIT_SEC = 30;
	
	/** 
	 * Buttons 
	 */
	public static final JButton MON_BUTTON = new JButton("Monitor");
	public static final JButton AMQ_BUTTON = new JButton("ActiveMQ");
	public static final JButton EXP_BUTTON = new JButton("Export Tool");
	public static final JButton SWB_BUTTON = new JButton("SIMILE Workbench");
	public static final JButton ERT_BUTTON = new JButton(DEFAULT_ERT_BUTTON_TEXT);
	public static final JButton DAT_BUTTON = new JButton("DKF Authoring Tool (DAT)");
	public static final JButton GAT_BUTTON = new JButton("<html><b>GIFT Authoring Tool (GAT)</b></html>");
	public static final JButton CAT_BUTTON = new JButton("Course Authoring Tool (CAT)");
	public static final JButton MAT_BUTTON = new JButton("Metadata Authoring Tool (MAT)");
	public static final JButton SCAT_BUTTON = new JButton("Sensor Config. Authoring Tool (SCAT)");
	public static final JButton LCAT_BUTTON = new JButton("Learner Config. Authoring Tool (LCAT)");
	public static final JButton ASAT_BUTTON = new JButton("AutoTutor Script Authoring Tool (ASAT)");
	public static final JButton PCAT_BUTTON = new JButton("Pedagogy Config. Authoring Tool (PCAT)");
	public static final JButton TRADEM_BUTTON = new JButton("TRADEM");
	public static final JButton DASH_BUTTON = new JButton(DEFAULT_DASH_BUTTON_TEXT);
	public static final JButton WRAP_BUTTON = new JButton(DEFAULT_WRAP_BUTTON_TEXT);
	
	public static final JButton UMS_CLEAR_USER_DATA_BUTTON = new JButton("Clear User Data");
	public static final JButton UMS_CLEANUP_IDS_DATA_BUTTON = new JButton("Cleanup Id Generators");
	public static final JButton UMS_RECREATE_DB_BUTTON = new JButton("Recreate Db");
	public static final JButton UMS_RESET_DB_BUTTON = new JButton("Reset Db");
	
    public static final JButton LMS_CLEAR_USER_DATA_BUTTON = new JButton("Clear User Data");
    public static final JButton LMS_RECREATE_DB_BUTTON = new JButton("Recreate Db");
    public static final JButton LMS_RESET_DB_BUTTON = new JButton("Reset Db");
    
    public static final JButton CLEAR_LOGGER_SENSOR_DATA_BUTTON = new JButton("Clear Logger+Sensor Data");
    
    public static final JButton CREATE_SIMPLE_LOGIN_USERS_BUTTON = new JButton("Create Simple Login Users");
    
    /** permanently delete domain session message log files that aren't referenced by published courses */
    public static final JButton CLEAR_UNUSED_DS_LOG_BUTTON = new JButton("Delete Unused Domain Session logs");

	/** 
	 * Paths 
	 */
	private static final String[] START_GAS = {"start", "gas"};
	
	/** checks OS to determine if the path should point to linux script or a .bat script for windows */
	private static final String MON_PATH = SupportedOSFamilies.UNIX.equals(PlatformUtils.getFamily())
            ? "./scripts/util/launchMonitor"
            :"scripts/util/launchMonitor.bat";
	
	/** checks OS to determine if the path should point to linux script or a .bat script for windows */
	private static final String AMQ_PATH = SupportedOSFamilies.UNIX.equals(PlatformUtils.getFamily())
	        ? "scripts/util/launchActiveMQ"
	        : "scripts/util/launchActiveMQ.bat";
	private static final String SWB_DIR  = "external/simile/Workbench";
	private static final String SWB_PATH = "external/simile/Workbench/SimileWorkbench.exe";
	private static final String IE_PATH  = "C:/Program Files/Internet Explorer/iexplore.exe";
	
	/* Determine the path to the launch script based on the operating system family */
	private static final String SCRIPT_PATH = SupportedOSFamilies.UNIX.equals(PlatformUtils.getFamily())
	        ? System.getProperty(LaunchConstants.USER_DIR) + LaunchConstants.UNIX_LAUNCH_SCRIPT_REL_PATH
            : System.getProperty(LaunchConstants.USER_DIR) + LaunchConstants.WIN_LAUNCH_SCRIPT_REL_PATH;
	

	private static String err;
	private static String ertPath;
	private static String gasPath;
	private static String dashPath;
	private static String wrapPath;
	
    /**
     * The url parameter for redirecting to the Wrap landing page. Must match
     * {@link mil.arl.gift.tools.spl.SingleProcessLauncher#LAUNCH_WRAP_URL_PARAM} and
     * {@link mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility#LAUNCH_WRAP_URL_PARAM}
     */
    private final static String LAUNCH_WRAP_URL_PARAM = "launchwrap";
	
	/** 
	 * Sets all button attributes except ActionListener 
	 */
	static {

		/* Set tool tip text for all buttons */
		MON_BUTTON.setToolTipText(MON_TOOLTIP);
		EXP_BUTTON.setToolTipText(EXP_TOOLTIP);
		DAT_BUTTON.setToolTipText(DAT_TOOLTIP);
		GAT_BUTTON.setToolTipText(GAT_TOOLTIP);
		CAT_BUTTON.setToolTipText(CAT_TOOLTIP);
		ERT_BUTTON.setToolTipText(ERT_TOOLTIP);
		MAT_BUTTON.setToolTipText(MAT_TOOLTIP);
		SWB_BUTTON.setToolTipText(SWB_TOOLTIP);
		ASAT_BUTTON.setToolTipText(ASAT_TOOLTIP);
		TRADEM_BUTTON.setToolTipText(TRADEM_TOOLTIP);
		LCAT_BUTTON.setToolTipText(LCAT_TOOLTIP);
		PCAT_BUTTON.setToolTipText(PCAT_TOOLTIP);
		SCAT_BUTTON.setToolTipText(SCAT_TOOLTIP);
		DASH_BUTTON.setToolTipText(DASH_TOOLTIP);
		WRAP_BUTTON.setToolTipText(WRAP_TOOLTIP);
		UMS_CLEAR_USER_DATA_BUTTON.setToolTipText(UMS_CLEAR_USER_DATA_TOOLTIP);
		UMS_CLEANUP_IDS_DATA_BUTTON.setToolTipText(UMS_CLEANUP_IDS_DATA_TOOLTIP);
		UMS_RECREATE_DB_BUTTON.setToolTipText(UMS_RECREATE_DB_TOOLTIP);
		UMS_RESET_DB_BUTTON.setToolTipText(UMS_RESET_DB_TOOLTIP);
		LMS_CLEAR_USER_DATA_BUTTON.setToolTipText(LMS_CLEAR_USER_DATA_TOOLTIP);
	    LMS_RECREATE_DB_BUTTON.setToolTipText(LMS_RECREATE_DB_TOOLTIP);
	    LMS_RESET_DB_BUTTON.setToolTipText(LMS_RESET_DB_TOOLTIP);
	    CLEAR_LOGGER_SENSOR_DATA_BUTTON.setToolTipText(CLEAR_LOGGER_SENSOR_DATA_TOOLTIP);
	    CREATE_SIMPLE_LOGIN_USERS_BUTTON.setToolTipText(CREATE_SIMPLE_LOGIN_USERS_TOOLTIP);
	    CLEAR_UNUSED_DS_LOG_BUTTON.setToolTipText(CLEAR_UNUSED_DS_LOG_TOOLTIP);
		
		//As of 2016-1 the GAT should only be accessed through the GIFT dashboard
		GAT_BUTTON.setEnabled(false);

		/* Creates and sets action listeners for all buttons */
		setActions();
		
		/* Set urls*/
		ControlPanelProperties properties = ControlPanelProperties.getInstance();
		gasPath = properties.getGiftAdminServerUrl();
		ertPath = properties.getEventReportToolPath();
		dashPath = properties.getDashboardPath();
        wrapPath = properties.getGiftAuthorToolURL() + "#" + LAUNCH_WRAP_URL_PARAM;
		
		if(ControlPanelProperties.getInstance().getASATURL() == null || ControlPanelProperties.getInstance().getASATURL().isEmpty()){
		    /* Disable AutoTutor Script Authoring Tool button. */
		    ASAT_BUTTON.setEnabled(false);
		}
		
        if(ControlPanelProperties.getInstance().getTRADEMURL() == null || ControlPanelProperties.getInstance().getTRADEMURL().isEmpty()){
            /* Disable TRADEM button. */
            TRADEM_BUTTON.setEnabled(false);
        }
	}

	/**  
	 * Creates Action Listeners for all buttons 
	 */
	private static void setActions() {
	    
	    // 64bit takes precedence
	    File javaFolder = new File("external" + File.separator + "openjdk-11.64x");
	    final String javaPath = javaFolder.exists() ? "external\\openjdk-11.64x\\jdk-11\\bin\\java" : "external\\openjdk-11\\jdk-11\\bin\\java";

	    AMQ_BUTTON.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent evt) {
                
                try {
                    Runtime.getRuntime().exec(AMQ_PATH);
                } catch (IOException e) {
                    logger.error("Failed to launch " + AMQ_PATH, e);
                }
            }
        });
		
		MON_BUTTON.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent evt) {
			    
			    //check if activeMQ is running - if not launch it
			    if(!ControlWindow.isActiveMQOnline()){
			        launchActiveMQ();
			        
			        Thread t = new Thread(new Runnable() {
			            
			            @Override
			            public void run() {			                
                            
                            /* Change cursor to busy */
                            ControlWindow.getRootPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                            
                            try{
                                Thread.sleep(ACTIVEMQ_INITIAL_WAIT);
                            }catch(@SuppressWarnings("unused") Exception e){ }

			                /* Wait while the ActiveMQ is offline. 
			                 * Limit the number of times the thread waits */
                            long startTime = System.currentTimeMillis();
			                while(!ControlWindow.isActiveMQOnline() && (System.currentTimeMillis() - startTime) < (ACTIVEMQ_MAX_WAIT_SEC * 1000)) {
			                    
			                    try {			                                        
			                        Thread.sleep(ACTIVEMQ_WAIT);
			                    } catch (InterruptedException e) { 
			                        logger.error("Caught exception while waiting for ActiveMQ to be online" , e);
			                    }
			                }
			                
			                /* Change cursor from busy to default */
			                ControlWindow.getRootPane().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			                
			                if(ControlWindow.isActiveMQOnline()){
			                    launchPath(MON_PATH);
			                }else{
			                    //there was a problem, show an error dialog
			                    JOptionPane.showMessageDialog(null, 
			                            "Unable to automatically launch the monitor because there was a problem with determining if ActiveMQ is running.\n\n" +
			                            "Wait a second and try again (maybe more time was needed before checking ActiveMQ).", 
			                            "Problem with detecting ActiveMQ",
			                            JOptionPane.WARNING_MESSAGE);
			                }

			            }
			        }, "Wait for ActiveMQ");
			        
			        t.start();
			        
			    }else{
			        launchPath(MON_PATH);
			    }
			}
		});

		ERT_BUTTON.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent evt) {
				
				if (gasPath != null && ertPath != null) {
					
				    // try to prevent multiple starts of the ERT
				    ERT_BUTTON.setEnabled(false);
				    
				    if(!ControlWindow.isGASOnline()){
				        //show the user that the ERT can't start until the GAS is running
				        ERT_BUTTON.setText(WAITING_ERT_BUTTON_TEXT);
				        launchScript(START_GAS);
				    }
				    
				    final Thread launchThread = launchGASWebpage(gasPath + "/" + ertPath);
                    Thread buttonThread = new Thread(new Runnable() {
                        
                        @Override
                        public void run() {

                            try {
                                launchThread.join();
                            } catch (@SuppressWarnings("unused") InterruptedException e) {
                                
                            }
                            
                            //reset button
                            ERT_BUTTON.setEnabled(true);
                            ERT_BUTTON.setText(DEFAULT_ERT_BUTTON_TEXT);
                        }
                    }, "ERT Button waiting for gas");
                    
                    buttonThread.start();

				} else {
					/* If the paths weren't found, put whichever wasn't found 
					 * in the log file */
					
					err = "Failed to find path to " + ((ertPath != null) ? 
							"GIFT Admin Server" : "Event Reporting Tool.");
					
					logger.error(err);
				}
			}
		});
		
		DASH_BUTTON.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent evt) {
                
                if (dashPath != null && dashPath != null) {
                    
                    // try to prevent multiple starts of the dashboard
                    DASH_BUTTON.setEnabled(false);
                    
                    if(!ControlWindow.isGASOnline()){
                        //show the user that the dashboard can't start until the GAS is running
                        DASH_BUTTON.setText(WAITING_DASH_BUTTON_TEXT);
                        launchScript(START_GAS);
                    }
                    
                    // Check if activeMQ is running - if not launch it
                    // ActiveMQ is needed by the dashboard for the Game Master (Web Monitor) logic that was recently added
                    //
                    if(!ControlWindow.isActiveMQOnline()){
                        launchActiveMQ();
                    }
                    

                    final Thread launchThread = launchGASWebpage(gasPath + "/" + dashPath);
                    Thread buttonThread = new Thread(new Runnable() {
                        
                        @Override
                        public void run() {

                            try {
                                launchThread.join();
                            } catch (@SuppressWarnings("unused") InterruptedException e) {
                                
                            }
                            
                            //reset button
                            DASH_BUTTON.setEnabled(true);
                            DASH_BUTTON.setText(DEFAULT_DASH_BUTTON_TEXT);
                        }
                    }, "Dashboard Button waiting for gas");
                    
                    buttonThread.start();

                } else {
                    /* If the paths weren't found, put whichever wasn't found 
                     * in the log file */
                    
                    err = "Failed to find path to " + ((dashPath != null) ? 
                            "GIFT Admin Server" : "Dashboard.");
                    
                    logger.error(err);
                }
            }
        });
		
		WRAP_BUTTON.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent evt) {
				if (wrapPath != null) {
				    
				    // try to prevent multiple starts of the gift wrap
                    WRAP_BUTTON.setEnabled(false);
					
                    if(!ControlWindow.isGASOnline()){
                        //show the user that the gift wrap can't start until the GAS is running
                        WRAP_BUTTON.setText(WAITING_WRAP_BUTTON_TEXT);
                        launchScript(START_GAS);
                    }
                    
                    final Thread launchThread = launchGASWebpage(wrapPath);
                    Thread buttonThread = new Thread(new Runnable() {
                        
                        @Override
                        public void run() {

                            try {
                                launchThread.join();
                            } catch (@SuppressWarnings("unused") InterruptedException e) {
                                
                            }
                            
                            //reset button
                            WRAP_BUTTON.setEnabled(true);
                            WRAP_BUTTON.setText(DEFAULT_WRAP_BUTTON_TEXT);
                        }
                    }, "Wrap Button waiting for gas");
                    
                    buttonThread.start();

				} else {
					/* If the paths weren't found, put whichever wasn't found 
					 * in the log file */
					
					err = "Failed to find path to " + ((wrapPath != null) ? 
							"GIFT Admin Server" : "GIFT Wrap.");
					
					logger.error(err);
				}
			}
		});

		ASAT_BUTTON.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent evt) {
				
				String[] args = {IE_PATH, ControlPanelProperties.getInstance().getASATURL() + "?TheEmail=GiftUsername@gifttutoring.org&TheFullName=GiftUsername"};

				try {			
					Runtime.getRuntime().exec(args);
				} catch (Exception e) {
					
					logger.error("Caught exception while launching AutoTutor:\n"
							+ args[0] + "\n" + args[1], e);
				}
			}
		});
		
      TRADEM_BUTTON.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent evt) {
                
                String[] args = {IE_PATH, ControlPanelProperties.getInstance().getTRADEMURL()};

                try {           
                    Runtime.getRuntime().exec(args);
                } catch (Exception e) {
                    
                    logger.error("Caught exception while launching TRADEM:\n"
                            + args[0] + "\n" + args[1], e);
                }
            }
        });

		DAT_BUTTON.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent evt) {
				launchScript(LaunchConstants.LAUNCH_DAT.split(Constants.SPACE));
			}
		});

		CAT_BUTTON.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent evt) {
				launchScript(LaunchConstants.LAUNCH_CAT.split(Constants.SPACE));
			}
		});

		MAT_BUTTON.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent evt) {
				launchScript(LaunchConstants.LAUNCH_MAT.split(Constants.SPACE));
			}
		});

		EXP_BUTTON.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent evt) {
			    
			    JOptionPane.showConfirmDialog(null, 
			            "The desktop based export tool has been deprecated and will soon be replaced by the export logic in the GIFT Dashboard.\n\nPlease use the GIFT Dashboard to export courses.", 
			            "Deprecated Export Tool", 
			            JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE);
				launchScript(LaunchConstants.LAUNCH_EXPORT_TOOL.split(Constants.SPACE));
			}
		});

		SWB_BUTTON.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent evt) {
				
				if(new File(SWB_PATH).isFile()) {
					/* If SimileWorkbench.exe exists, launch it. */
					
					try {
						// Set the working directory and launch the workbench
						Runtime.getRuntime().exec(SWB_PATH, null, new File(SWB_DIR));	
						
					} catch (IOException | NullPointerException e) {
						logger.error("Failed to launch " + SWB_PATH, e);
					}
					
				} else {
					/* Otherwise, display error dialog. */
					
					JOptionPane.showMessageDialog(null, ""
							+ "Could not find the Simile Workbench at\nGIFT/"
							+ SWB_PATH, "File Not Found", JOptionPane.ERROR_MESSAGE);
			}
			}
		});

		LCAT_BUTTON.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent evt) {
				launchScript(LaunchConstants.LAUNCH_LCAT.split(Constants.SPACE));
			}
		});

		PCAT_BUTTON.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent evt) {
				launchScript(LaunchConstants.LAUNCH_PCAT.split(Constants.SPACE));
			}
		});

		SCAT_BUTTON.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent evt) {
				launchScript(LaunchConstants.LAUNCH_SCAT.split(Constants.SPACE));
			}
		});
		
		UMS_CLEAR_USER_DATA_BUTTON.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {

                int choice = JOptionPaneUtil.showOptionDialog("<html><b>Warning</b>...This script will remove UMS database entries created as the result of executing"+
                        "user and domain sessions (i.e. courses).<br><br>"+
                        "In addition it will remove experiments and subjects from the UMS database.<br><br>"+
                        "It will not remove authored surveys but it will remove all survey responses.<br><br>"+
                        "Pre-requisites:<br>"+
                        ".... 1) GIFT is built<br>"+
                        ".... 2) UMS database has been extracted and exists<br>"+
                        ".... 3) the third parties are in the GIFT/external directory<br>"+
                        ".... 4) Derby database server is running (check with \"netstat -aon | find \"1527\" \" command)<br>"+
                        "....    To manually start the server: GIFT\\external\\db-derby-10-15.2.0-bin\\bin\\startNetworkServer.bat", 
                        "Are you sure?", 
                        JOptionPane.WARNING_MESSAGE, 
                        "Continue", null, "Cancel");
                
                if(choice == JOptionPane.OK_OPTION){
                    
                    String[] cmd = {javaPath, 
                            "-Dderby.system.home=\"data\"",
                            "-classpath",
                            "\"external\\slf4j\\*;external\\*;external\\activemq\\activemq-all-5.18.3.jar;external\\hibernate\\*;external\\jsonsimple\\json_simple-1.1.jar;external\\db-derby-10-15.2.0-bin\\lib\\derbyclient.jar;bin\\gift-common.jar;bin\\gift-ums.jar;bin\\gift-ums-db.jar;external\\derby\\derby.jar\"",
                            "mil.arl.gift.ums.db.UMSDatabaseManager",
                            "clear"};
                    
                    try{
                        String output = launchCommand(cmd);
                        
                        JOptionPaneUtil.showConfirmDialog("Result of clear UMS user data operation:\n\n"+output, 
                                "Clear UMS User Data", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
                    }catch(Exception exception){
                        logger.error("Caught exception while trying to clear UMS user data", exception);
                        JOptionPaneUtil.showConfirmDialog("FAILED to clear UMS user data:\n"+exception.getMessage(), 
                                "Clear UMS User Data", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
		
        UMS_CLEANUP_IDS_DATA_BUTTON.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {

                int choice = JOptionPaneUtil.showOptionDialog("<html>This script will cleanup the UMS database Id generator tables.<br><br>"+
                        "Pre-requisites:<br>"+
                        ".... 1) GIFT is built<br>"+
                        ".... 2) UMS database has been extracted and exists<br>"+
                        ".... 3) the third parties are in the GIFT/external directory<br>"+
                        ".... 4) Derby database server is running (check with \"netstat -aon | find \"1527\" \" command)<br>"+
                        "....    To manually start the server: GIFT\\external\\db-derby-10-15.2.0-bin\\bin\\startNetworkServer.bat", 
                        "Are you sure?", 
                        JOptionPane.WARNING_MESSAGE, 
                        "Continue", null, "Cancel");
                
                if(choice == JOptionPane.OK_OPTION){
                    
                    String[] cmd = {javaPath, 
                            "-Dderby.system.home=\"data\"",
                            "-classpath",
                            "\"external\\slf4j\\*;external\\*;external\\activemq\\activemq-all-5.18.3.jar;external\\hibernate\\*;external\\jsonsimple\\json_simple-1.1.jar;external\\db-derby-10-15.2.0-bin\\lib\\derbyclient.jar;bin\\gift-common.jar;bin\\gift-ums.jar;bin\\gift-ums-db.jar;external\\derby\\derby.jar\"",
                            "mil.arl.gift.ums.db.UMSDatabaseManager",
                            "cleanupIdGenerators"};
                    
                    try{
                        String output = launchCommand(cmd);
                        
                        JOptionPaneUtil.showConfirmDialog("Result of cleanup UMS id generators operation:\n\n"+output, 
                                "Cleanup UMS id generators", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
                    }catch(Exception exception){
                        logger.error("Caught exception while trying to cleanup UMS id generators", exception);
                        JOptionPaneUtil.showConfirmDialog("FAILED to cleanup UMS id generators:\n"+exception.getMessage(), 
                                "Cleanup UMS id generators", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        
        UMS_RECREATE_DB_BUTTON.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {

                int choice = JOptionPaneUtil.showOptionDialog("<html>This script is used to recreate the UMS database.<br><br>"+
                        "<b>YOU WILL LOOSE ALL OF YOUR DATA IN THIS DATABASE!!!</b><br><br>"+
                        "Pre-requisites:<br>"+
                        ".... 1) GIFT is built<br>"+
                        ".... 2) UMS database has been extracted and exists<br>"+
                        ".... 3) the third parties are in the GIFT/external directory<br>"+
                        ".... 4) UMS module is NOT running<br>"+
                        ".... 5) Derby database server is running (check with \"netstat -aon | find \"1527\" \" command)<br>"+
                        "....    To manually start the server: GIFT\\external\\db-derby-10-15.2.0-bin\\bin\\startNetworkServer.bat", 
                        "Are you sure?", 
                        JOptionPane.WARNING_MESSAGE, 
                        "Continue", null, "Cancel");
                
                if(choice == JOptionPane.OK_OPTION){
                    
                    String[] cmd = {javaPath, 
                            "-Dderby.system.home=\"data\"",
                            "-classpath",
                            "\"external\\slf4j\\*;external\\*;external\\activemq\\activemq-all-5.18.3.jar;external\\hibernate\\*;external\\jsonsimple\\json_simple-1.1.jar;external\\db-derby-10-15.2.0-bin\\lib\\derbyclient.jar;bin\\gift-common.jar;bin\\gift-ums.jar;bin\\gift-ums-db.jar;external\\derby\\derby.jar\"",
                            "mil.arl.gift.ums.db.UMSDatabaseManager",
                            "recreate-db"};
                    
                    try{
                        String output = launchCommand(cmd);
                        
                        JOptionPaneUtil.showConfirmDialog("Result of recreate UMS db:\n\n"+output, 
                                "Recreate UMS db", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
                    }catch(Exception exception){
                        logger.error("Caught exception while trying to recreate UMS db", exception);
                        JOptionPaneUtil.showConfirmDialog("FAILED to recreate UMS db:\n"+exception.getMessage(), 
                                "Recreate UMS db", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        
        LMS_CLEAR_USER_DATA_BUTTON.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {

                int choice = JOptionPaneUtil.showOptionDialog("<html><b>Warning</b>...This script will remove LMS database entries created as the result of executing"+
                        "user and domain sessions (i.e. courses).<br><br>"+
                        "Pre-requisites:<br>"+
                        ".... 1) GIFT is built<br>"+
                        ".... 2) LMS database has been extracted and exists<br>"+
                        ".... 3) the third parties are in the GIFT/external directory<br>"+
                        ".... 4) Derby database server is running (check with \"netstat -aon | find \"1527\" \" command)<br>"+
                        "....    To manually start the server: GIFT\\external\\db-derby-10-15.2.0-bin\\bin\\startNetworkServer.bat", 
                        "Are you sure?", 
                        JOptionPane.WARNING_MESSAGE, 
                        "Continue", null, "Cancel");
                
                if(choice == JOptionPane.OK_OPTION){
                    
                    String[] cmd = {javaPath, 
                            "-Dderby.system.home=\"data\"",
                            "-classpath",
                            "\"external\\slf4j\\*;external\\*;external\\activemq\\activemq-all-5.18.3.jar;external\\hibernate\\*;external\\jsonsimple\\json_simple-1.1.jar;external\\db-derby-10-15.2.0-bin\\lib\\derbyclient.jar;bin\\gift-common.jar;bin\\gift-lms.jar;external\\derby\\derby.jar\"",
                            "mil.arl.gift.lms.impl.simple.db.LMSDatabaseManager",
                            "clear"};
                    
                    try{
                        String output = launchCommand(cmd);
                        
                        JOptionPaneUtil.showConfirmDialog("Result of clear LMS user data operation:\n\n"+output, 
                                "Clear LMS User Data", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
                    }catch(Exception exception){
                        logger.error("Caught exception while trying to clear LMS user data", exception);
                        JOptionPaneUtil.showConfirmDialog("FAILED to clear LMS user data:\n"+exception.getMessage(), 
                                "Clear LMS User Data", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        
        LMS_RECREATE_DB_BUTTON.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {

                int choice = JOptionPaneUtil.showOptionDialog("<html>This script is used to recreate the LMS database.<br><br>"+
                        "<b>YOU WILL LOOSE ALL OF YOUR DATA IN THIS DATABASE!!!</b><br><br>"+
                        "Pre-requisites:<br>"+
                        ".... 1) GIFT is built<br>"+
                        ".... 2) LMS database has been extracted and exists<br>"+
                        ".... 3) the third parties are in the GIFT/external directory<br>"+
                        ".... 4) LMS module is NOT running<br>"+
                        ".... 5) Derby database server is running (check with \"netstat -aon | find \"1527\" \" command)<br>"+
                        "....    To manually start the server: GIFT\\external\\db-derby-10-15.2.0-bin\\bin\\startNetworkServer.bat", 
                        "Are you sure?", 
                        JOptionPane.WARNING_MESSAGE, 
                        "Continue", null, "Cancel");
                
                if(choice == JOptionPane.OK_OPTION){
                    
                    String[] cmd = {javaPath, 
                            "-Dderby.system.home=\"data\"",
                            "-classpath",
                            "\"external\\slf4j\\*;external\\*;external\\activemq\\activemq-all-5.18.3.jar;external\\hibernate\\*;external\\jsonsimple\\json_simple-1.1.jar;external\\db-derby-10-15.2.0-bin\\lib\\derbyclient.jar;bin\\gift-common.jar;bin\\gift-lms.jar;external\\derby\\derby.jar\"",
                            "mil.arl.gift.lms.impl.simple.db.LMSDatabaseManager",
                            "recreate-db"};
                    
                    try{
                        String output = launchCommand(cmd);
                        
                        JOptionPaneUtil.showConfirmDialog("Result of recreate LMS db:\n\n"+output, 
                                "Recreate LMS db", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
                    }catch(Exception exception){
                        logger.error("Caught exception while trying to recreate LMS db", exception);
                        JOptionPaneUtil.showConfirmDialog("FAILED to recreate LMS db:\n"+exception.getMessage(), 
                                "Recreate LMS db", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        
        UMS_RESET_DB_BUTTON.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {

                int choice = JOptionPaneUtil.showOptionDialog("<html>Are you sure you want to restore the UMS database back to the original content?<br><br>"+
                        "<b>Note:  This will delete all entries in the UMS database created after GIFT was installed.</b><br><br>"+
                        "<b>WARNING:</b> This will temporarily shut down the network server, which may prevent applications<br>"+
                        "like the UMS module, the single process launcher and the authoring tools from accessing the database<br>"+
                        "if they are left running when the network server is shut down.",
                        "Are you sure?", 
                        JOptionPane.WARNING_MESSAGE, 
                        "Continue", null, "Cancel");
                
                if(choice == JOptionPane.OK_OPTION){
                    
                    logger.info("stopping derby db server...");
                    String[] stopDerbyCmd = {"external\\db-derby-10-15.2.0-bin\\bin\\stopNetworkServer.bat"};
                    try{
                        launchCommand(stopDerbyCmd);
                    }catch(Exception exception){
                        logger.error("Caught exception while trying to stop the derby database server when reseting the UMS db", exception);
                        JOptionPaneUtil.showConfirmDialog("FAILED to reset UMS db.  There was a problem while stoping the derby database server:\n"+exception.getMessage(), 
                                "Reset UMS db", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    logger.info("deleting UMS database...");
                    File umsDbFolder = new File("data" + File.separator + "derbyDb" + File.separator + "GiftUms");
                    try {
                        FileUtil.deleteDirectory(umsDbFolder);
                    } catch (IOException e1) {
                        logger.error("Caught exception while trying to delete the UMS derby database folder when reseting the UMS db", e1);
                        JOptionPaneUtil.showConfirmDialog("FAILED to reset UMS db.  There was a problem while deleting the UMS derby database folder:\n"+e1.getMessage(), 
                                "Reset UMS db", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    logger.info("extracting UMS database...");
                    
                    File umsZip = new File("data" + File.separator + "derbyDb" + File.separator + "DerbyDB.UMS.Backup.Original.zip");
                    File destinationFolder = new File("data" + File.separator + "derbyDb");
                    try {
                        ZipUtils.unzipArchive(umsZip, destinationFolder, null);
                    } catch (IOException e1) {
                        logger.error("Caught exception while trying to unzip the original UMS derby database folder when reseting the UMS db", e1);
                        JOptionPaneUtil.showConfirmDialog("FAILED to reset UMS db.  There was a problem while unzipping the original UMS derby database folder:\n"+e1.getMessage(), 
                                "Reset UMS db", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    //check that UMS db directory exists
                    if(umsDbFolder.exists()){
                        //success
                        JOptionPaneUtil.showConfirmDialog("Successfully reset the UMS database", 
                                "Reset UMS db", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
                    }else{
                        //Error
                        JOptionPaneUtil.showConfirmDialog("FAILED to reset UMS db.  The UMS derby database folder doesn't exist where it should: '"+umsDbFolder.getAbsolutePath()+"'.", 
                                "Reset UMS db", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
                    }

                }
            }
        });
        
        LMS_RESET_DB_BUTTON.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {

                int choice = JOptionPaneUtil.showOptionDialog("<html>Are you sure you want to restore the LMS database back to the original content?<br><br>"+
                        "<b>Note:  This will delete all entries in the LMS database created after GIFT was installed.</b><br><br>"+
                        "<b>WARNING:</b> This will temporarily shut down the network server, which may prevent applications<br>"+
                        "like the LMS module, the single process launcher and the authoring tools from accessing the database<br>"+
                        "if they are left running when the network server is shut down.",
                        "Are you sure?", 
                        JOptionPane.WARNING_MESSAGE, 
                        "Continue", null, "Cancel");
                
                if(choice == JOptionPane.OK_OPTION){
                    
                    logger.info("stopping derby db server...");
                    String[] stopDerbyCmd = {"external\\db-derby-10-15.2.0-bin\\bin\\stopNetworkServer.bat"};
                    try{
                        launchCommand(stopDerbyCmd);
                    }catch(Exception exception){
                        logger.error("Caught exception while trying to stop the derby database server when reseting the LMS db", exception);
                        JOptionPaneUtil.showConfirmDialog("FAILED to reset LMS db.  There was a problem while stoping the derby database server:\n"+exception.getMessage(), 
                                "Reset LMS db", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    logger.info("deleting LMS database...");
                    File lmsDbFolder = new File("data" + File.separator + "derbyDb" + File.separator + "GiftLms");
                    try {
                        FileUtil.deleteDirectory(lmsDbFolder);
                    } catch (IOException e1) {
                        logger.error("Caught exception while trying to delete the LMS derby database folder when reseting the UMS db", e1);
                        JOptionPaneUtil.showConfirmDialog("FAILED to reset LMS db.  There was a problem while deleting the LMS derby database folder:\n"+e1.getMessage(), 
                                "Reset LMS db", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    logger.info("extracting LMS database...");
                    
                    File umsZip = new File("data" + File.separator + "derbyDb" + File.separator + "DerbyDB.LMS.Backup.Original.zip");
                    File destinationFolder = new File("data" + File.separator + "derbyDb");
                    try {
                        ZipUtils.unzipArchive(umsZip, destinationFolder, null);
                    } catch (IOException e1) {
                        logger.error("Caught exception while trying to unzip the original LMS derby database folder when reseting the LMS db", e1);
                        JOptionPaneUtil.showConfirmDialog("FAILED to reset LMS db.  There was a problem while unzipping the original LMS derby database folder:\n"+e1.getMessage(), 
                                "Reset LMS db", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    //check that UMS db directory exists
                    if(lmsDbFolder.exists()){
                        //success
                        JOptionPaneUtil.showConfirmDialog("Successfully reset the LMS database", 
                                "Reset LMS db", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
                    }else{
                        //Error
                        JOptionPaneUtil.showConfirmDialog("FAILED to reset LMS db.  The LMS derby database folder doesn't exist where it should: '"+lmsDbFolder.getAbsolutePath()+"'.", 
                                "Reset LMS db", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
                    }

                }
            }
        });
        
        CLEAR_LOGGER_SENSOR_DATA_BUTTON.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {

                int choice = JOptionPaneUtil.showOptionDialog("<html>Are you sure you want to permanetly delete:<br>"+
                        ".... log files in output\\logger<br>"+
                        ".... csv files in output\\sensor<br>"+
                        ".... ALL files/folders in output\\testResults",
                        "Are you sure?", 
                        JOptionPane.WARNING_MESSAGE, 
                        "Continue", null, "Cancel");
                
                if(choice == JOptionPane.OK_OPTION){
                    
                    try{
                    
                        // Sensor files
                        File sensorDir = new File(PackageUtil.getSensorOutput());
                        for (File f : sensorDir.listFiles()) {
                            if (f.getName().endsWith(".csv")) {
                                f.delete(); 
                            }
                        }
                        
                        // Logger module files
                        File loggerModuleDir = new File(PackageUtil.getOutput() + File.separator + "logger" + File.separator + "module");
                        for (File f : loggerModuleDir.listFiles()) {
                            if (f.getName().endsWith(".log")) {
                                f.delete(); 
                            }
                        }
                        
                        // Logger module files
                        File loggerToolsDir = new File(PackageUtil.getOutput() + File.separator + "logger" + File.separator + "tools");
                        for (File f : loggerToolsDir.listFiles()) {
                            if (f.getName().endsWith(".log")) {
                                f.delete(); 
                            }
                        }
                        
                        // Logger message files
                        File loggerMessageDir = new File(PackageUtil.getOutput() + File.separator + "logger" + File.separator + "message");
                        for (File f : loggerMessageDir.listFiles()) {
                            if (f.getName().endsWith(".log")) {
                                f.delete(); 
                            }
                        }
                        
                        // JUnit test result created directories
                        File testResultsDir = new File(PackageUtil.getOutput() + File.separator + "testResults");
                        for (File f : testResultsDir.listFiles()) {
                            if (f.isDirectory()) {
                                try {
                                    FileUtil.deleteDirectory(f);
                                } catch (IOException e1) {
                                    logger.warn("Failed to delete the JUnit test result directory of '"+f+"'.", e1);
                                } 
                            }else{
                                f.delete();
                            }
                        }
                        
                        JOptionPaneUtil.showConfirmDialog("Successfully removed files", 
                                "Files Deleted", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
                        
                    }catch(Throwable t){
                        logger.error("Caught exception while trying to clear output files.", t);
                        JOptionPaneUtil.showConfirmDialog("ERROR: failed to remove files because "+t.getMessage(), 
                                "Failed to delete files", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        
        CLEAR_UNUSED_DS_LOG_BUTTON.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {

                int choice = JOptionPaneUtil.showOptionDialog("<html>This script is used to <b>permanently delete<b/> all domain session<br>"+
                        "message log files that are not referenced by a published course (GIFT Experiment or LTI).<br/><br/>"+
                        "This will not delete system and domain session log files that are currently being written too by an actively<br/>"+
                        "running GIFT instance or course execution.<br/><br/>"+
                        "Pre-requisites:<br/>"+
                        ".... 1) GIFT is built<br/>"+
                        ".... 2) UMS database has been extracted and exists<br/>"+
                        ".... 3) the third parties are in the GIFT/external directory<br>"+
                        ".... 4) Derby database server is running (check with \"netstat -aon | find \"1527\" \" command)<br>"+
                        "....    To manually start the server: GIFT\\external\\db-derby-10-15.2.0-bin\\bin\\startNetworkServer.bat", 
                        "Are you sure?", 
                        JOptionPane.WARNING_MESSAGE, 
                        "Continue", null, "Cancel");
                
                if(choice == JOptionPane.OK_OPTION){
                    
                    String[] cmd = {javaPath, 
                            "-Dderby.system.home=\"data\"",
                            "-classpath",
                            "\"external\\slf4j\\*;external\\*;external\\activemq\\activemq-all-5.18.3.jar;external\\hibernate\\*;external\\jsonsimple\\json_simple-1.1.jar;external\\db-derby-10-15.2.0-bin\\lib\\derbyclient.jar;bin\\gift-common.jar;bin\\gift-ums.jar;bin\\gift-ums-db.jar;external\\derby\\derby.jar\"",
                            "mil.arl.gift.ums.db.UMSDatabaseManager",
                            "clearUnusedLogFiles"};
                    
                    try{
                        String output = launchCommand(cmd);
                        
                        JOptionPaneUtil.showConfirmDialog("Result of deleting unused domain session message log files:\n\n"+output, 
                                "Recreate UMS db", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
                    }catch(Exception exception){
                        logger.error("Caught exception while trying to delete unused domain session message log files", exception);
                        JOptionPaneUtil.showConfirmDialog("FAILED to deleting unused domain session message log files:\n"+exception.getMessage(), 
                                "Recreate UMS db", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        
        CREATE_SIMPLE_LOGIN_USERS_BUTTON.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                
                int choice = JOptionPaneUtil.showOptionDialog("<html>This script will create new user ids in the UMS database<br>"+
                " The user's created will have the same LMS username.  To login as one of the user ids created you will have to use<br>"+
                        " the Simple Login webpage of the TUI.<br><br>"+
                        "Pre-requisites:<br>"+
                        ".... 1) GIFT is built<br>"+
                        ".... 2) UMS database has been extracted and exists<br>"+
                        ".... 3) the third parties are in the GIFT/external directory<br>"+
                        ".... 4) Derby database server is running (check with \"netstat -aon | find \"1527\" \" command)<br>"+
                        "....    To manually start the server: GIFT\\external\\db-derby-10-15.2.0-bin\\bin\\startNetworkServer.bat", 
                        "Are you sure?", 
                        JOptionPane.WARNING_MESSAGE, 
                        "Continue", null, "Cancel");
                
                if(choice == JOptionPane.OK_OPTION){
                    
                    //present dialog to obtain user id information
                    JFrame frame = new JFrame("Create Users");
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    
                    JPanel panel = new JPanel();
                    panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
                    
                    JLabel startingUserIdLabel = new JLabel("Starting User Id");
                    JLabel numOfUsers = new JLabel("Number of users");
                    
                    JTextField startingUserIdTextfield = new JTextField();
                    startingUserIdTextfield.setText("1");
                    startingUserIdTextfield.setPreferredSize(new Dimension(40, 20));
                    
                    JTextField numOfUsersTextfield = new JTextField();
                    numOfUsersTextfield.setText("1");
                    numOfUsersTextfield.setPreferredSize(new Dimension(40, 20));
                    
                    JButton createButton = new JButton("Create");
                    createButton.addActionListener(new ActionListener() {
                        
                        @Override
                        public void actionPerformed(ActionEvent e) {

                            //
                            // check inputs
                            //
                            int startingUserId;
                            String startingUserIdStr = startingUserIdTextfield.getText();
                            if(startingUserIdStr == null || startingUserIdStr.isEmpty()){
                                JOptionPaneUtil.showConfirmDialog("Please provide a starting user id value (e.g. 100)", 
                                        "Incorrect Starting User id value", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
                                return;
                            }else{
                                
                                try{
                                    startingUserId = Integer.valueOf(startingUserIdStr);
                                }catch(@SuppressWarnings("unused") NumberFormatException exception){
                                    JOptionPaneUtil.showConfirmDialog("The starting user id value of "+startingUserIdStr+" is not a valid integer", 
                                            "Incorrect Starting User id value", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
                                    return;
                                }
                            }
                            
                            int numOfUsers;
                            String numOfUsersStr = numOfUsersTextfield.getText();
                            if(numOfUsersStr == null || numOfUsersStr.isEmpty()){
                                JOptionPaneUtil.showConfirmDialog("Please provide the number of users (e.g. 10)", 
                                        "Incorrect Number of Users value", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
                                return;
                            }else{
                                
                                try{
                                    numOfUsers = Integer.valueOf(numOfUsersStr);
                                }catch(@SuppressWarnings("unused") NumberFormatException exception){
                                    JOptionPaneUtil.showConfirmDialog("The number of users value of "+numOfUsersStr+" is not a valid integer", 
                                            "Incorrect Number of Users value", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
                                    return;
                                }
                            }
                            
                            String[] cmd = {javaPath, 
                                    "-Dderby.system.home=\"data\"",
                                    "-classpath",
                                    "\"external\\slf4j\\*;external\\*;external\\activemq\\activemq-all-5.18.3.jar;external\\hibernate\\*;external\\jsonsimple\\json_simple-1.1.jar;external\\db-derby-10-15.2.0-bin\\lib\\derbyclient.jar;bin\\gift-common.jar;bin\\gift-ums.jar;bin\\gift-ums-db.jar;external\\derby\\derby.jar;config\\ums\\ums.log4j.properties\"",
                                    "mil.arl.gift.ums.db.UMSDatabaseManager",
                                    "create-users",
                                    startingUserId+":"+numOfUsers};
                            
                            try{
                                logger.info("Creating "+numOfUsers+" users in the UMS db starting at user id of "+startingUserId+".");
                                String output = launchCommand(cmd);
                                
                                JOptionPaneUtil.showConfirmDialog("Result of creating users in the UMS db:\n\n"+output, 
                                        "Create Users", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
                                
                                frame.dispose();
                            }catch(Exception exception){
                                logger.error("Caught exception while trying to create users in the UMS db", exception);
                                JOptionPaneUtil.showConfirmDialog("FAILED to create users in the UMS db:\n"+exception.getMessage(), 
                                        "Create Users", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    });
                    JButton cancelButton = new JButton("Cancel");
                    cancelButton.addActionListener(new ActionListener() {
                        
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            frame.dispose();
                        }
                    });
                    
                    JPanel startingUserIdPanel = new JPanel();
                    
                    startingUserIdPanel.add(startingUserIdLabel);
                    startingUserIdPanel.add(startingUserIdTextfield);
                    
                    JPanel numOfUsersPanel = new JPanel();
                    
                    numOfUsersPanel.add(numOfUsers);
                    numOfUsersPanel.add(numOfUsersTextfield);
                    
                    JPanel buttonPanel = new JPanel();
                    
                    buttonPanel.add(createButton);
                    buttonPanel.add(cancelButton);
                    
                    panel.add(startingUserIdPanel);
                    panel.add(numOfUsersPanel);
                    panel.add(buttonPanel);
                    
                    frame.setContentPane(panel);
                    frame.setLocationRelativeTo(null);
                    frame.setPreferredSize(new Dimension(250, 150));
                    
                    //Display the window.
                    frame.pack();
                    frame.setVisible(true);  
                }

          
            }
        });
	}
	
	private static void launchActiveMQ(){
	    
	    try {
	          Runtime.getRuntime().exec(AMQ_PATH);
        } catch (IOException e) {
            logger.error("Failed to launch " + AMQ_PATH, e);
        }
	}

	/** 
	 * Executes the launchProcess script with the arguments provided.
	 * 
	 * @param args arguments to use for the launchProcess script
	 */
	private static void launchScript(String[] args) {
		
		try {
		    String[] cmdArray = new String[args.length+1];
		    cmdArray[0] = SCRIPT_PATH;
		    for(int i = 0; i < args.length; i++){
		        cmdArray[i+1] = args[i];
		    }
		    
		    Runtime.getRuntime().exec(cmdArray);
		} catch (IOException e) {
			logger.error("Failed to execute command args: '" + args + "'", e);
		}
	}
	
	/**
	 * Runs the command provided.
	 * 
	 * @param cmdArray contains the command and any arguments
	 * @return the output of running the command (error and input stream)
	 * @throws IOException if there was a problem running the command
	 */
	private static String launchCommand(String[] cmdArray) throws IOException{
	    
        try {
            
            ProcessBuilder builder = new ProcessBuilder(cmdArray);
            builder.redirectErrorStream(true);
            Process process = builder.start();
            
            StringBuilder commandsb = new StringBuilder();
            for(String str : cmdArray){
                commandsb.append(str).append("\n   ");
            }
            
            InputStream stdout = process.getInputStream ();
            BufferedReader reader = new BufferedReader (new InputStreamReader(stdout));
            String line;
            StringBuffer outputStringBuffer = new StringBuffer();
            outputStringBuffer.append("command:\n").append(commandsb.toString()).append("\noutput:\n");

            //Note: this while loop will finish once the process has finished, even if some part of the process doesn't write anything
            //      to the output stream.  This was tested by putting a "<sleep milliseconds="100000"/>" in the database-convert.xml.
            while ((line = reader.readLine ()) != null) {
                outputStringBuffer.append("\n").append(line);
            }  
            
            logger.info("Result of command:\n"+outputStringBuffer);
            
            return outputStringBuffer.toString();
            
        } catch (IOException e) {
            throw e;
        }
        
	}

	/** 
	 * Opens a webpage specified by 'uri' in the default browser for the user
	 * 
	 * @param uri - the uri of the wepbage
	 * @return the thread that is waiting for the GAS to be online and then launching the URI in a browser.
	 */
	private static Thread launchGASWebpage(final String uri) {
		
		Thread t = new Thread(new Runnable() {
			
			@Override
			public void run() {			    
                
                /* Change cursor to busy */
			    ControlWindow.getRootPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

				/* 
				 * Wait until the GAS:Dashboard URL is online/responding. 
				 * Limit the amount of time the thread waits just in case there is a problem
				 */
			    long startTime = System.currentTimeMillis();
                while(!ControlWindow.isGASOnline() && (System.currentTimeMillis() - startTime) < (GAS_MAX_WAIT_SEC * 1000)) {
                    
                    try {                                     
                        Thread.sleep(GAS_WAIT);

                    } catch (InterruptedException e) { 
                        logger.error("Caught exception while waiting for "+uri+" to be online." , e);
                    }
                }

                /* Change cursor from busy to default */
				ControlWindow.getRootPane().setCursor
				(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				
				if(SupportedOSFamilies.WINDOWS.equals(PlatformUtils.getFamily())) {

    				String browserURL = uri;
    				try {
                        // NOTE: This only works on Microsoft Windows
                        Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + browserURL);
    				} catch (IOException e) {				
    					logger.error("Failed to launch webpage: "+browserURL, e);
    				}
				}
			} 
		}, "Wait for GAS");
		
		t.start();
		return t;
	}
	
	/** 
	 * Launches anything based on the 'path' specified
	 * @param path - the path to the executable
	 */
	private static void launchPath(String path) {
		
		try {
			Runtime.getRuntime().exec(path);
        } catch (IOException e) {
            logger.error("Failed to launch " + path, e);
        }
    }
	
	}
