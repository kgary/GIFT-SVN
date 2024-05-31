/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.controlpanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Box.Filler;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.PackageUtil;
import mil.arl.gift.common.enums.DeploymentModeEnum;
import mil.arl.gift.common.io.ImageUtil;

/**
 * Creates the GUI of the main window and tabs.
 * 
 * @author bzahid
 */
public class ControlWindow {
    
    static {
        //use domain log4j
        PropertyConfigurator.configureAndWatch(PackageUtil.getConfiguration() + "/tools/controlpanel/controlpanel.log4j.properties");
    }    
    
    private static Logger logger = LoggerFactory.getLogger(ControlWindow.class);

	/** Status information for GAS */
    //TODO: implement a better approach here so there is only one component to update status label on
	private static GASStatus authPanelGasStatus;
	private static GASStatus dataMgtGasStatus;
	
	/** Status information for ActiveMQ */
	private static ActiveMQStatus activeMQStatus = new ActiveMQStatus();
	
	/** Filler Dimensions */
	private static Dimension panelPadding = new Dimension(10, 20);
	private static Dimension buttonPadding = new Dimension(10, 12);
	
	/** Button Dimensions */
	private static Dimension buttonSize = new Dimension(250, 30);

	private static JFrame frame;

	public static final String ONLINE  = "Online";
	public static final String OFFLINE = "Offline";
	
	/** how often to check if the GAS or ActiveMQ URL is available/online */
	private static final long GAS_PING_INTERVAL = 100;
	private static final long ACTIVEMQ_PING_INTERVAL = 100;	
	   
	/** 
	 * Creates the main window 
	 */
	public ControlWindow() {

	    authPanelGasStatus = new GASStatus();
	    dataMgtGasStatus = new GASStatus();
		JTabbedPane mainPane = new JTabbedPane();
		frame = new JFrame("Control Panel");
		
		frame.setSize(380, 695);
		frame.setIconImage(ImageUtil.getInstance().getSystemIcon());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		/* Create tabs and add to JTabbedPane. */
		mainPane.addTab("Operator Tools", createOperatorPanel());
		mainPane.addTab("Authoring Tools", createAuthPanel());
		mainPane.addTab("Data Management", createDataManagementPanel());

		/* Add JTabbedPane to main window. */
		frame.getContentPane().add(mainPane, BorderLayout.CENTER);
		frame.setVisible(true);
		frame.setLocationRelativeTo(null);
		
		authPanelGasStatus.checkStatus();
		dataMgtGasStatus.checkStatus();
	}

	static {
		PropertyConfigurator.configure(PackageUtil.getConfiguration() + "/tools/controlpanel/controlpanel.log4j.properties");
	}
	
	/** 
	 * Checks to see if the GAS is online
	 * 
	 * @return true if the GAS is online, false otherwise 
	 */
	public static boolean isGASOnline() {
		
		return dataMgtGasStatus.getStatus().equals(ONLINE);
	}
	
	/** 
	 * Checks to see if ActiveMQ is online
     * 
     * @return true if the ActiveMQ is online, false otherwise 
     */
    public static boolean isActiveMQOnline() {
        
        return activeMQStatus.getStatus().equals(ONLINE);
    }
	
	/** 
	 * Gets the root pane of the Control Panel
	 * 
	 * @return JFrame - the root pane of the Control Panel
	 */
	public static JFrame getRootPane() {
		return frame;
	}
	
	/** 
	 * Creates the "Operator Tools" tab and adds buttons 
	 * 
	 * @return the panel
	 */
	private JPanel createOperatorPanel() {

	    JPanel operatorPanel = new JPanel();
		JPanel priorityPanel = new JPanel();
		operatorPanel.setLayout(new BoxLayout(operatorPanel, BoxLayout.Y_AXIS));
		priorityPanel.setLayout(new BoxLayout(priorityPanel, BoxLayout.Y_AXIS));
		
		
		/* Padding between first button and tab title */
		operatorPanel.add(new Filler(panelPadding, panelPadding, panelPadding));

		/* Add buttons from ActionButtons class */
		
		Dimension priorityPanelDimension;
		if(ControlPanelProperties.getInstance().getDeploymentMode() != DeploymentModeEnum.SIMPLE){
		    
		    priorityPanelDimension = new Dimension(300, 180);

		    
    		//dashboard
    		JPanel interfaceLabelPanel = new JPanel();
    		interfaceLabelPanel.setLayout(new BorderLayout());   
    		interfaceLabelPanel.add(new Filler(buttonPadding, buttonPadding, buttonPadding), BorderLayout.BEFORE_FIRST_LINE);
    		interfaceLabelPanel.add(new JLabel("<html><font color=\"0000FF\" size=\"3\"><b>GIFT Browser:</b></font></html>"));
    		interfaceLabelPanel.add(new Filler(buttonPadding, buttonPadding, buttonPadding), BorderLayout.AFTER_LAST_LINE);
            priorityPanel.add(interfaceLabelPanel);
    	    addButton(ToolButtons.DASH_BUTTON, priorityPanel);
    	    priorityPanel.add(new JSeparator());
		}else{
		    priorityPanelDimension = new Dimension(300, 100);
		}
		
        priorityPanel.setMaximumSize(priorityPanelDimension);
        priorityPanel.setPreferredSize(priorityPanelDimension);
	    
	    //'Runtime' label
	    JPanel runtimeLabelPanel = new JPanel();
	    runtimeLabelPanel.setLayout(new BorderLayout());	
	    runtimeLabelPanel.add(new Filler(buttonPadding, buttonPadding, buttonPadding), BorderLayout.BEFORE_FIRST_LINE);
	    runtimeLabelPanel.add(new JLabel("<html><font color=\"#A00000\" size=\"3\"><b>Tutor Runtime:</b></font></html>"));
	    runtimeLabelPanel.add(new Filler(buttonPadding, buttonPadding, buttonPadding), BorderLayout.AFTER_LAST_LINE);
	    priorityPanel.add(runtimeLabelPanel);
	    
	    //monitor
	    addButton(ToolButtons.MON_BUTTON, priorityPanel);
	    priorityPanel.add(new JSeparator());
	    priorityPanel.add(new Filler(buttonPadding, buttonPadding, buttonPadding));

	    operatorPanel.add(priorityPanel);
	    
		addButton(ToolButtons.EXP_BUTTON, operatorPanel);
		addButton(ToolButtons.ERT_BUTTON, operatorPanel);
        
		
		return operatorPanel;
	}
	
	/**
	 * Create the "Data Management" tab and add buttons
	 * 
	 * @return the panel
	 */
	private JComponent createDataManagementPanel(){
	    
	    JPanel rootPanel = new JPanel();
        
        JTabbedPane tabbedPane = new JTabbedPane();

        JPanel umsPanel = new JPanel();
        JPanel lmsPanel = new JPanel();
        JPanel miscPanel = new JPanel();

        umsPanel.setLayout(new BoxLayout(umsPanel, BoxLayout.Y_AXIS));
        lmsPanel.setLayout(new BoxLayout(lmsPanel, BoxLayout.Y_AXIS));
        miscPanel.setLayout(new BoxLayout(miscPanel, BoxLayout.Y_AXIS));
        
        /* Padding between first button and tab title */
        umsPanel.add(new Filler(panelPadding, panelPadding, panelPadding));
        lmsPanel.add(new Filler(panelPadding, panelPadding, panelPadding));
        miscPanel.add(new Filler(panelPadding, panelPadding, panelPadding));
        
        addButton(ToolButtons.UMS_CLEAR_USER_DATA_BUTTON, umsPanel);
        addButton(ToolButtons.UMS_CLEANUP_IDS_DATA_BUTTON, umsPanel);
        addButton(ToolButtons.UMS_RECREATE_DB_BUTTON, umsPanel);
        addButton(ToolButtons.UMS_RESET_DB_BUTTON, umsPanel);
        
        addButton(ToolButtons.LMS_CLEAR_USER_DATA_BUTTON, lmsPanel);
        addButton(ToolButtons.LMS_RECREATE_DB_BUTTON, lmsPanel);
        addButton(ToolButtons.LMS_RESET_DB_BUTTON, lmsPanel);
        
        addButton(ToolButtons.CLEAR_LOGGER_SENSOR_DATA_BUTTON, miscPanel);
        addButton(ToolButtons.CREATE_SIMPLE_LOGIN_USERS_BUTTON, miscPanel);
        addButton(ToolButtons.CLEAR_UNUSED_DS_LOG_BUTTON, miscPanel);
        
        tabbedPane.add("UMS DB", new JScrollPane(umsPanel));
        tabbedPane.add("LMS DB", new JScrollPane(lmsPanel));
        tabbedPane.add("Misc.", new JScrollPane(miscPanel));
        
        rootPanel.add(tabbedPane);
        dataMgtGasStatus.addGASLabel(rootPanel);
        
        return rootPanel;
	}

	/** 
	 * Creates "Authoring Tools" tab and adds buttons 
	 */
	private JComponent createAuthPanel() {
	    
	    JPanel rootPanel = new JPanel();
	    
	    JTabbedPane tabbedPane = new JTabbedPane();

		JPanel authWebPanel = new JPanel();
		JPanel authDesktopPanel = new JPanel();
		JPanel authWebPriorityPanel = new JPanel();

		authWebPanel.setLayout(new BoxLayout(authWebPanel, BoxLayout.Y_AXIS));
		authDesktopPanel.setLayout(new BoxLayout(authDesktopPanel, BoxLayout.Y_AXIS));
		authWebPriorityPanel.setLayout(new BoxLayout(authWebPriorityPanel, BoxLayout.Y_AXIS));
		
	    Dimension d = new Dimension(300, 110);
		authWebPriorityPanel.setMaximumSize(d);
		authWebPriorityPanel.setPreferredSize(d);
		
		/* Padding between first button and tab title */
		authWebPanel.add(new Filler(panelPadding, panelPadding, panelPadding));
		authDesktopPanel.add(new Filler(panelPadding, panelPadding, panelPadding));
		
		addButton(ToolButtons.GAT_BUTTON, authWebPriorityPanel);
		addButton(ToolButtons.WRAP_BUTTON, authWebPriorityPanel);
		authWebPriorityPanel.add(new JSeparator());
		authWebPanel.add(authWebPriorityPanel);
		
		addButton(ToolButtons.ASAT_BUTTON, authWebPanel);	
		addButton(ToolButtons.CAT_BUTTON,  authDesktopPanel);
		addButton(ToolButtons.DAT_BUTTON,  authDesktopPanel);			
		addButton(ToolButtons.LCAT_BUTTON, authDesktopPanel);		
		addButton(ToolButtons.MAT_BUTTON,  authDesktopPanel);
		addButton(ToolButtons.PCAT_BUTTON, authDesktopPanel);
		addButton(ToolButtons.SCAT_BUTTON, authDesktopPanel);
		addButton(ToolButtons.SWB_BUTTON,  authDesktopPanel);
		addButton(ToolButtons.TRADEM_BUTTON, authWebPanel);
				
		tabbedPane.add("Web Browser ", new JScrollPane(authWebPanel));
		tabbedPane.add("Desktop App", new JScrollPane(authDesktopPanel));
		
		rootPanel.add(tabbedPane);
		authPanelGasStatus.addGASLabel(rootPanel);
		
		return rootPanel;
	}

	/** 
	 * Adds a button to a panel  
	 * @param button - the button to add to the panel
	 * @param panel - the panel to add the button to 
	 */
	private void addButton(JButton b, JPanel panel) {

		/* Set button attributes */
		b.setMaximumSize(buttonSize);
		b.setMinimumSize(buttonSize);
		b.setPreferredSize(buttonSize);
		b.setAlignmentX(Component.CENTER_ALIGNMENT);

		/* Add button to panel */
		panel.add(b);

		/* Add filler after button */
		panel.add(new Filler(buttonPadding, buttonPadding, buttonPadding));
	}
	
	/**
	 * Returns true if a connection was established to the endpoint specified.
	 * 
	 * @param endpoint the socket address to use to establish a socket connection
	 * @return boolean whether the connection succeeded (didn't timeout)
	 */
	private static boolean isConnected(SocketAddress endpoint){
	    
	    boolean connected = false;
	    
	    try{
	        Socket socket = new Socket();
	        socket.connect(endpoint, 500);
	        socket.close();
	        return true;
	    }catch (@SuppressWarnings("unused") Exception e) { 
            //don't want to flood the log file
        }        

        return connected;
	}
	
    /** 
     * Returns true if there is a connection to the given web address.
     * 
     * @param urlString - the string to test
     * @return boolean - true if there is a connection, false otherwise.
     */
    protected static boolean isConnected(String urlString) {

        boolean connected = false;

        try {

            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setConnectTimeout(2000);

            connected = (connection.getResponseCode() == HttpURLConnection.HTTP_OK 
                      || connection.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND);

            connection.disconnect();

        } catch (@SuppressWarnings("unused") Exception e) { 
            //don't want to flood the log file
        }        

        return connected;
    }
	
	/**
	 * Displays the Control Panel Application
	 * @param args - command line arguments
	 */
	public static void main(String[] args) {

		if (System.getProperty("os.name").toLowerCase().contains("windows")) {

			try {
				UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
			} catch (Exception e) {
				logger.error("Caught an exception while setting the look and feel", e);
			}
		}

		try {
			ControlWindow.class.getDeclaredConstructor().newInstance();
		} catch (@SuppressWarnings("unused") InstantiationException | IllegalAccessException | 
		        NoSuchMethodException | SecurityException | InvocationTargetException e) {
			// do nothing
		} 
	}
	
	private static class ActiveMQStatus {
	    
	    private String status;
	    
	    private String activeMqUrl = ControlPanelProperties.getInstance().getBrokerURL();
	    private SocketAddress address;
	    
	    public ActiveMQStatus(){
	        
	        //Note: this assumes the URL is of the format
	        //  tcp://localhost:61617
	        //-> specifically [x]://[y]:[z]
	        String[] tokens = activeMqUrl.split(":");
	        String hostname = tokens[1].replace("/", "");
	        String port = tokens[2];
	        address = new InetSocketAddress(hostname, Integer.valueOf(port));
	        
	        checkStatus();
	    }
	    
	    public String getStatus(){
	        return status;
	    }
	    
	    /** 
         * Changes the ActiveMQ status.
         * 
         * @param online - true for 'Online', false for 'Offline'
         */
        private void updateActiveMQStatus(boolean online) {

            status = (online) ? ONLINE : OFFLINE;
        }
	    
	    /** 
         * Establishes a timer that checks if ActiveMQ is up or not. 
         */
        public void checkStatus() {

            Timer status_Timer = new Timer("ActiveMQ Status Timer"); 

            status_Timer.scheduleAtFixedRate(new TimerTask() {

                @Override
                public void run() {

                    updateActiveMQStatus(isConnected(address));
                }
            }, 0, ACTIVEMQ_PING_INTERVAL);
        }

	}
	
	/** 
	 * Class that creates the Status Label displayed under Admin Tools
	 */
	private static class GASStatus {

		private Filler filler;
		private String gasUrl;
		private String status;
		private String gasAddress;
		private JLabel statusLabel;	
		private TitledBorder border;	
		private static final Color RED = new Color(210, 15, 15);
		private static final Color GREEN = new Color(5, 142, 19);	
		private static final String CENTER = "<html><div style=\"text-align: center;\">";
		
		/** 
		 * Initializes Admin Server address 
		 */
		public GASStatus() {
			
		    status = OFFLINE;
			gasUrl = ControlPanelProperties.getInstance().getGiftAdminServerUrl();
			gasAddress = "Address: " + gasUrl;
		}
		
		public String getStatus(){
		    return status;
		}

		/** 
		 * Creates the status label and adds it to the panel specified by 'panel'
		 * @param panel - the JPanel to add the status label to
		 */
		public void addGASLabel(JPanel panel) {

		    statusLabel = new JLabel();
			
			JPanel gasPanel = new JPanel();
			JLabel addressLabel = new JLabel();
			JLabel giftDirectoryLabel = new JLabel();
			Box gasBox = Box.createVerticalBox(); 
			
			createBorder(gasPanel);

			statusLabel.setForeground(RED);
			statusLabel.setText(CENTER + getStatus());
			statusLabel.setHorizontalAlignment(JLabel.CENTER);
			addressLabel.setText(CENTER + "GIFT Admin Server:<br>" + gasAddress);
			
			gasBox.add(addressLabel);
			gasBox.add(statusLabel);
			gasPanel.add(gasBox);
			
			Box giftDirBox = Box.createVerticalBox(); 
	        giftDirectoryLabel.setText(CENTER + "<br>GIFT Directory:<br>" + new File("").getAbsolutePath()+"<br></div></html>");
	        giftDirBox.add(giftDirectoryLabel);
	        gasPanel.add(giftDirBox);
			
			panel.add(filler);
			panel.add(gasPanel);
		}

		/** 
		 * Creates the pretty "System Status" title and border within a panel
		 * @param panel - the panel to add the System Status border to 
		 */
		private void createBorder(JPanel panel) {

			/** Spacing between the buttons and status label */
			Dimension filSize = new Dimension(300, 30);		
			filler = new Filler(filSize, filSize, filSize);

			border = BorderFactory.createTitledBorder
					(BorderFactory.createEtchedBorder(new Color(153, 153, 153), null), "System Status");
			border.setTitleJustification(TitledBorder.CENTER);

			panel.setMaximumSize(new Dimension(300, 120));
			panel.setMinimumSize(new Dimension(300, 120));
			panel.setPreferredSize(new Dimension(300, 120));
			panel.setBorder(border);
		}

		/** 
		 * Changes the Admin Server status.
		 * @param online - true for 'Online', false for 'Offline'
		 */
		public void updateGASStatus(boolean online) {

		    status = (online) ? ONLINE : OFFLINE;

			statusLabel.setText(CENTER + getStatus());
			
			statusLabel.setForeground((online) ? GREEN : RED);
		}

		/** 
		 * Establishes a timer that checks if the GIFT Admin Server is up or not. 
		 */
		public void checkStatus() {

			Timer GAS_Timer = new Timer("GAS Status Timer"); 

			GAS_Timer.scheduleAtFixedRate(new TimerTask() {

				@Override
				public void run() {

					updateGASStatus(isConnected(gasUrl));
				}
			}, 0, GAS_PING_INTERVAL);
		}

	}
		
}
