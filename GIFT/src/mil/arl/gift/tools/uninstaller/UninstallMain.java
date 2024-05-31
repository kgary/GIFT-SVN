/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.uninstaller;

import java.awt.Dimension;
import java.awt.Image;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.UIManager;

import mil.arl.gift.common.PackageUtil;
import mil.arl.gift.common.io.ImageUtil;

import org.apache.log4j.PropertyConfigurator;
import org.ciscavate.cjwizard.WizardContainer;
import org.ciscavate.cjwizard.WizardPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Opens the Uninstall Tool GUI.
 * 
 * @author mhoffman
 */
public class UninstallMain {
	
    static {
        //use Export log4j
        PropertyConfigurator.configureAndWatch(PackageUtil.getConfiguration() + "/tools/uninstall/uninstall.log4j.properties");
    }
    
	/** Logger instance */
	private static Logger logger = LoggerFactory.getLogger(UninstallMain.class);
	
	private static final String TITLE = "GIFT Uninstall Tool";
	
	private static final Dimension SIZE = new Dimension(850, 435);

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch(Exception e) {
			logger.error("Caught exeption while setting look and feel.", e);
		}
		
		UninstallPageFactory factory = new UninstallPageFactory();
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
		container.addWizardListener(new UninstallWizardListener(dialog));		
	}

}
