/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.gateway.desktop;

import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import mil.arl.gift.common.io.ImageUtil;
import mil.arl.gift.common.io.Version;
import mil.arl.gift.gateway.GatewayModule;
import mil.arl.gift.gateway.GatewayModuleProperties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.javafx.application.HostServicesDelegate;

/**
 * Creates the system tray icon for the Gateway Module
 * 
 * @author bzahid
 */
public class GatewayTray extends Application {
	
	/** Instance of the logger */
	private static final Logger logger = LoggerFactory.getLogger(GatewayTray.class);
	
	/** Singleton instance of this class */
	private static GatewayTray instance;
	
	/** The system tray*/
	private static final SystemTray systemTray = SystemTray.getSystemTray();
	
	/** The system tray icon object */
	private TrayIcon trayIcon;
	
	/** Labels for application choices */
	private static final String ABOUT = "About";
	private static final String EXIT  = "Exit";
	
	/** Tray icon alert messages */
	private static final String ENTER_MSG = "GIFT Gateway Module is running in the system tray.";
	private static final String EXIT_MSG  = "GIFT Gateway Module is still running in the system tray.";
	
	/** About dialog components*/
	private static VBox vBox;
	private static HBox hBox;
	private static Label label;
	private static Hyperlink link;
	private static Button okButton;
	private static Stage dialogStage;
	
	static {
		// Initialize JavaFX
	    @SuppressWarnings("unused")
        JFXPanel panel = new JFXPanel();
		// Prevent JavaFX thread from implicitly exiting. 
		Platform.setImplicitExit(false);
	}
	
	/**
	 * Return the singleton instance of the JWSTray
	 * 
	 * @return JWSTray
	 */
	public static GatewayTray getInstance() {
		if(instance == null) {
			instance = new GatewayTray();
		}
		return instance;
	}
		
	private GatewayTray() {
		
		if(SystemTray.isSupported()) {

			try {
				// Set icon
				Image giftImg = ImageUtil.getInstance().getSystemIcon();
				trayIcon = new TrayIcon(giftImg);
				trayIcon.setImageAutoSize(true);
				trayIcon.setToolTip("GIFT Gateway Module");
				
				// Create menu
				PopupMenu menu = new PopupMenu();
				MenuItem exitItem = new MenuItem(EXIT);
				MenuItem aboutItem = new MenuItem(ABOUT);
				
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						createAbout();
					} 
				});
				
				menu.add(aboutItem);
				menu.add(exitItem);
				trayIcon.setPopupMenu(menu);
				
				exitItem.addActionListener(new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(java.awt.event.ActionEvent e) {
						int choice = JOptionPane.showConfirmDialog(null, 
								"Are you sure you want to exit the GIFT Gateway Module?", 
								"GIFT Gateway Module", 
								JOptionPane.YES_NO_OPTION, 
								JOptionPane.QUESTION_MESSAGE, 
								new ImageIcon(giftImg));
						
						if(choice == JOptionPane.OK_OPTION) {
							GatewayModule.getInstance().killModule();
							clean();
						}
					}
				});
				
				aboutItem.addActionListener(new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(java.awt.event.ActionEvent e) {

						Platform.runLater(new Runnable() {
							@Override
							public void run() {								
								dialogStage.show();
							}
						});
					}
				});
				
				systemTray.add(trayIcon);
				
			} catch (Exception x) {
				logger.error("Caught exception while adding tray icon: ", x);
			}
		}
	}

	/**
	 * Causes the system tray to issue an alert message indicating that GIFT is running in the tray.
	 */
	public void displayAlert() {
		trayIcon.displayMessage("GIFT", ENTER_MSG, TrayIcon.MessageType.INFO);
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
	
	private void createAbout() {
		
		vBox = new VBox();
		hBox = new HBox();
		dialogStage = new Stage();
		okButton = new Button("OK");
		link = new Hyperlink("For more information, please visit gifttutoring.org");
		label = new Label("Generalized Intelligent Framework for Tutoring\n"
				+ "Version: " + Version.getInstance().getName() + "\n"
				+ "Release Date: " + Version.getInstance().getReleaseDate() 
				+ "\n\nThe GIFT Gateway module application allows GIFT to"
				+ " communicate with desktop Training Applications (e.g. "
				+ "PowerPoint, TC3) running on your computer.");
		
		label.setWrapText(true);
		label.setFont(new Font(13));
		label.setPadding(new Insets(0, 0, 10, 0));
		link.setFont(new Font(13));
		link.setBorder(null);
		
		okButton.setMinWidth(100);
		okButton.setPadding(new Insets(5, 5, 5, 5));
		hBox.setPadding(new Insets(20, 0, 0, 0));
		hBox.setAlignment(Pos.BOTTOM_CENTER);
		hBox.getChildren().add(okButton);
		
		vBox.setAlignment(Pos.TOP_CENTER);
		vBox.setPadding(new Insets(15, 20, 0, 20));
		vBox.getChildren().addAll(label, link, hBox);
		
		dialogStage.setTitle("About GIFT Gateway Module");
		dialogStage.setScene(new Scene(vBox, 450, 260));
		dialogStage.initModality(Modality.WINDOW_MODAL);
		dialogStage.initStyle(StageStyle.UTILITY);
		
		okButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent evt) {
				dialogStage.hide();										
			}
		});
		
		link.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent evt) {
				HostServicesDelegate.getInstance(instance).showDocument(GatewayModuleProperties.getInstance().getGIFTWebsite());
			}
		});
	}

	/** 
	 * Displays a notification that the GIFT Gateway Module is still running.
	 */
	public void displayExitMessage() {
		trayIcon.displayMessage("GIFT", EXIT_MSG, TrayIcon.MessageType.INFO);
	}

	@Override
	public void start(Stage arg0) throws Exception {
		// nothing to do here
	}
}
