/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.installer;

import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import mil.arl.gift.common.enums.LessonLevelEnum;
import mil.arl.gift.common.io.HelpDocuments;
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
	private static final URI DOCUMENTATION = HelpDocuments.getInstallDoc();
	private static final String WELCOME_LABEL = "<html><br><h3>Welcome to the GIFT Install Tool</h3><br><br>" +
            "This tool will help guide you through installing GIFT "+Version.getInstance().getName()+" on this machine.<br><br>" +
            "For more information on installing GIFT please refer to the</html>";  			
	private static final String DOCUMENT_LINK = "<html><a href=\""+DOCUMENTATION+"\">GIFT Install</a> documentation.</html>";
	
	/** message to show on this installer page when in RTA lesson level setting */
	private static final String RTA_LESSON_LEVEL_WARNING = "<html><br><font color=\"red\"> GIFT is currently configured in "+LessonLevelEnum.RTA.getDisplayName()+
	        " lesson level.  The Tutor module<br/>(and web Tutor User Interface) will not be available.  Therefore the GIFT installer will<br/>not recommend installing a "+
	        "character server at this time.<br/>If you change the lesson level configuration in the future, please run the GIFT installer again.</html>";
	
	private static final String DEVELOPER_WARNING = "<html><br><b>Note:</b> If you have modified source code please run GIFT/build.bat to compile GIFT appropriately.</html>";
	
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
		
		JLabel website = new JLabel();
		website.setText(DOCUMENT_LINK);
        website.setCursor(new Cursor(Cursor.HAND_CURSOR));
		panel.add(website);
		
        website.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(DOCUMENTATION);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        
        if(InstallerProperties.getInstance().getLessonLevel() == LessonLevelEnum.RTA){
            panel.add(new JLabel(RTA_LESSON_LEVEL_WARNING));
        }
        
        panel.add(new JLabel(DEVELOPER_WARNING));
        
        panel.add(new JLabel(NEXT_LABEL));
		
		add(panel);
	}
}
