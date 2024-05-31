/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.installer;

import java.awt.Dimension;
import java.awt.Image;
import java.io.File;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.UIManager;

import mil.arl.gift.common.PackageUtil;
import mil.arl.gift.common.io.ImageUtil;
import mil.arl.gift.common.io.PlatformUtils;

import org.apache.log4j.PropertyConfigurator;
import org.ciscavate.cjwizard.WizardContainer;
import org.ciscavate.cjwizard.WizardPage;
import org.ciscavate.cjwizard.WizardSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Opens the Install Tool GUI.
 * 
 * @author cdettmering
 */
public class InstallMain {

	static {
        //use Export log4j
        PropertyConfigurator.configureAndWatch(PackageUtil.getConfiguration() + "/tools/install/install.log4j.properties");
    }
    
	/** Logger instance */
	private static Logger logger = LoggerFactory.getLogger(InstallMain.class);
	
	/** Title for GIFT install dialog */
	private static final String TITLE = "GIFT Install Tool";
	
	/** Size of window for GIFT install dialog */
	private static final Dimension SIZE = new Dimension(870, 435);
	
	/** Determines if default settings should be used in place of opening dialog for user input */
	private static final Boolean USE_DEFAULT = InstallerProperties.getInstance().getUseDefaultSettings();
	
    /** the database directories to check if they exist */
    private static final File UMS_DB_HOME = new File("data" + File.separator + "derbyDb" + File.separator + "GiftUms");
    private static final File LMS_DB_HOME = new File("data" + File.separator + "derbyDb" + File.separator + "GiftLms");
    
    /** flag to indicate whether the databases need to be installed */
    private static boolean extractUMSDb;
    private static boolean extractLMSDb;

	public static void main(String[] args)  {
		
		if(!USE_DEFAULT 
		        && !PlatformUtils.isHeadless() 
		        && PlatformUtils.getFamily().equals(PlatformUtils.SupportedOSFamilies.WINDOWS)) {
		
		    /* 
		     * If GIFT is in a non-headless Windows environment and is not using the default controls, display 
		     * UI components to let the user change the installer properties. 
		     */
			try {
				UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
			} catch(Exception e) {
				logger.error("Caught exeption while setting look and feel.", e);
			}
			
			InstallPageFactory factory = InstallPageFactory.getInstance();
			List<WizardPage> pages = factory.getPages();
			StepPageTemplate template = new StepPageTemplate();
			for(WizardPage page : pages) {
				template.addStep(page);
			}
			WizardContainer container = new WizardContainer(factory, template);
			JFrame dialog = new JFrame();
			dialog.setTitle(TITLE);
			dialog.setSize(SIZE);
			dialog.setMinimumSize(SIZE);
			
			Image icon = ImageUtil.getInstance().getSystemIcon();
			if(icon != null) {
				dialog.setIconImage(icon);
			} else {
				logger.error("Could not load GIFT icon.");
			}
			
			dialog.getContentPane().add(container);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setLocationRelativeTo(null);	// According to the Javadoc this centers a JDialog
			dialog.setVisible(true);
			container.addWizardListener(new InstallWizardListener(dialog));		
			
		} else {
			
			if(logger.isInfoEnabled()) {
				logger.info("Installing GIFT using default settings");
			}
			
		    /* 
		     * Otherwise, just provide the bare minimum installer properties to get GIFT running.
		     */
			if(!UMS_DB_HOME.exists() && !LMS_DB_HOME.exists()){
	            
	            extractUMSDb = true;
	            extractLMSDb = true;
	        }
			
			
			WizardSettings settings = new WizardSettings();
			
	        settings.put(InstallSettings.EXTRACT_UMS_DATABASE, extractUMSDb);
	        settings.put(InstallSettings.EXTRACT_LMS_DATABASE, extractLMSDb);

			InstallThread installer = new InstallThread(settings);
			
			installer.doInBackground();
		}
		
	}

}
