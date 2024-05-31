/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.spl;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import mil.arl.gift.common.io.ImageUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles the creation and deletion of the system tray icon for GIFT.
 * 
 * @author cdettmering
 */
public class SystemTrayManager {
	
	private static final Logger logger = LoggerFactory.getLogger(SystemTrayManager.class);
	
	/** The singleton system tray */
	private static final SystemTray systemTray = SystemTray.getSystemTray();
	
	/** labels for application choices */
	private static final String GIFT_WEBPAGE = "Open GIFT Webpage";
	private static final String WRAP_WEBPAGE = "Open GIFT Wrap Webpage";
	private static final String EXIT = "Exit";
	
	/** The system tray icon object */
	private TrayIcon trayIcon;
	
	/** The one and only instance **/
	private static SystemTrayManager instance;
	
	/**
	 * Get the one and only singleton instance of the SystemTrayManager.
	 * @return the one and only SystemTrayManager instance
	 */
	public static SystemTrayManager getInstance() {		
		if(instance == null) {			
			instance = new SystemTrayManager();
		}		
		return instance;		
	}	
	
	/**
	 * Private constructor to enforce the singleton pattern.
	 */
	private SystemTrayManager() {
		
		if(SystemTray.isSupported()) {
			Image giftImage = ImageUtil.getInstance().getSystemIcon();
			if(giftImage != null) {

				try {

                    SwingUtilities.invokeAndWait(new Runnable() {
                        
                        @Override
                        public void run() {
                            trayIcon = createTrayIcon(giftImage);
                            createPopupMenu();                            
                        }
                    });
			            
                    systemTray.add(trayIcon);

				} catch (AWTException | InvocationTargetException | InterruptedException e) {
					logger.error("Caught exception while creating the system tray icon.", e);
					trayIcon = null;
                }				
			}
		}
	}	
	
	/**
	 * Causes the system tray to issue an alert message indicating that GIFT is running in the tray.
	 * 
	 */
	public void displayAlert() {
		
		trayIcon.displayMessage("GIFT", "GIFT is running in the system tray.\nUse this icon to interact with GIFT.", TrayIcon.MessageType.INFO);
	}	
	
	
	/**
	 * Checks if the current platform has a system tray, and if GIFT was able 
	 * to use it.
	 * @return False of the system tray is unavailable, true otherwise.
	 */
	public boolean hasSystemTray() {
		return SystemTray.isSupported() && trayIcon != null;
	}
	
	/**
	 * Cleans the system tray, removing the GIFT icon.
	 */
	public void clean() {
		if(hasSystemTray()) {
			systemTray.remove(trayIcon);
		}
	}
	
	/**
	 * Displays a notification that GIFT is still running upon exiting the browser.
	 */
	public void displayBrowserExitMessage(){
		trayIcon.displayMessage("GIFT", "GIFT is still running in the system tray.\nIf you wish to exit GIFT, please right click this icon and select \"Exit\".", TrayIcon.MessageType.INFO);
	}
	
	/**
	 * Displays a notification that GIFT is closing.
	 */
	public void displayClosingMessage(){
	    trayIcon.displayMessage("GIFT", "GIFT is closing...", TrayIcon.MessageType.INFO);
	}
	
	/**
	 * Creates the GIFT system tray icon object.
	 * Note: should be called on the AWT event dispatching thread (e.g. use SwingUtilities.invokeLater)
	 *  
	 * @param image The image to use for the system tray icon.
	 * @return GIFT system tray icon.
	 */
	private TrayIcon createTrayIcon(Image image) {
	    
	    TrayIcon icon = new TrayIcon(image); 
        icon.setImageAutoSize(true);
        icon.setToolTip("GIFT");
		
		return icon;
	}
	
	/**
	 * Creates the right-click pop up menu for the GIFT system tray icon.
	 */
	private void createPopupMenu() {
		// Create the pop up menu
		PopupMenu popMenu = new PopupMenu();
		
		MenuItem giftBrowserItem = new MenuItem(GIFT_WEBPAGE);
		MenuItem wrapBrowserItem = new MenuItem(WRAP_WEBPAGE);
		MenuItem exitItem = new MenuItem(EXIT);
		
		popMenu.add(giftBrowserItem);
		
		if(!SingleProcessProperties.getInstance().isServerDeploymentMode()){
		    popMenu.add(wrapBrowserItem);
		    popMenu.addSeparator();
		}
		
	    popMenu.add(exitItem);
		
	    trayIcon.setPopupMenu(popMenu);
		
		// Setup listeners
	    giftBrowserItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SingleProcessLauncher.launchGIFTWebpage();
			}
		});
	    
	    wrapBrowserItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SingleProcessLauncher.launchWrapWebpage();
            }
        });
		
		exitItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
			    
			    int choice = JOptionPane.showConfirmDialog(null,
			            "Are you sure you want to close this instance of GIFT?",
			            "GIFT Instance",
	                    JOptionPane.YES_NO_OPTION,
			            JOptionPane.QUESTION_MESSAGE,
			            new ImageIcon(ImageUtil.getInstance().getSystemIcon()));
			    
			    if(choice == JOptionPane.OK_OPTION){			    
			        SingleProcessLauncher.exit();
			    }
			}
		});
	}
}
