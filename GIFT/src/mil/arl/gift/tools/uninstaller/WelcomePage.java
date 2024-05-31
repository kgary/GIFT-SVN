/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.uninstaller;

import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import mil.arl.gift.common.io.Version;

import org.ciscavate.cjwizard.WizardPage;
import org.ciscavate.cjwizard.WizardSettings;

/**
 * Shows a welcome message page to the user
 * 
 * @author mhoffman
 *
 */
public class WelcomePage extends WizardPage {

	/** Generated serial */
	private static final long serialVersionUID = -3448243092273537929L;
	
	private static final String TITLE = "Welcome";
	private static final String DESCRIPTION = "Welcome";
	private static final String WELCOME_LABEL = "<html><br><h3>Welcome to the GIFT Uninstall Tool</h3><br><br>" +
            "This tool will help guide you through uninstalling GIFT "+Version.getInstance().getName()+" on this machine.</html>";  			
	
	private static final String NEXT_LABEL = "<html><br><br>Click <b>Next</b> to continue...</html>";
	
	/**
	 * Creates a new WelcomePage
	 */
	public WelcomePage() {
		super(TITLE, DESCRIPTION);
		setupUi();
	}
	
	@Override
	public void rendering(List<WizardPage> path, WizardSettings settings) {
		setNextEnabled(true);
		setFinishEnabled(false);
	}
	
    /**
     * Sets up all of the swing components
     */
	private void setupUi() {
	    
	    JPanel panel = new JPanel();
	    BoxLayout layout = new BoxLayout(panel, BoxLayout.PAGE_AXIS);
	    panel.setLayout(layout);
	    
		panel.add(new JLabel(WELCOME_LABEL));
               
        panel.add(new JLabel(NEXT_LABEL));
		
		add(panel);
	}
}
