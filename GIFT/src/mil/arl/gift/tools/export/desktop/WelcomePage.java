/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.export.desktop;

import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import mil.arl.gift.common.io.HelpDocuments;

import org.ciscavate.cjwizard.WizardPage;
import org.ciscavate.cjwizard.WizardSettings;

/**
 * Shows a welcome message page to the user
 * 
 * @author cdettmering
 *
 */
public class WelcomePage extends WizardPage {

	/** Generated serial */
	private static final long serialVersionUID = -3448243092273537929L;
	
	/**
	 * The default size of the Export tutor tool wizard panel.  
	 * Note: the size used here should be a size that considers all the other wizard pages as the dialog is 'packed'
	 *       when it is first created.  At that point only this panel has been instantiated, therefore the sizes of 
	 *       the other panels in the tool are not known yet.
	 */
	private static final Dimension WIZARD_PREFERRED_SIZE = new Dimension(600, 400);
	
	private static final String TITLE = "Welcome";
	private static final String DESCRIPTION = "Welcome";
	private static final URI DOCUMENTATION = HelpDocuments.getExportTutorDoc();
	private static final String WELCOME_LABEL = "<html><br><h3>Welcome to the GIFT Export Tool</h3><br><br>" +
			"Before using the tool be sure to read and understand the limitations documented in the<br></html>";			
	private static final String DOCUMENT_LINK = "<html><a href=\""+DOCUMENTATION+"\">GIFT Export Tutor</a> documentation.</html>";
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
	
	private void setupUi() {
	    
	    JPanel panel = new JPanel();	    
	    this.setPreferredSize(WIZARD_PREFERRED_SIZE);
	    
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
        
        panel.add(new JLabel(NEXT_LABEL));
		
		add(panel);
	}
}
