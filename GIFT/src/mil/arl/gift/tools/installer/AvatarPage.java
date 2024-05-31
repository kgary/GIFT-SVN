/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.installer;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.ciscavate.cjwizard.WizardPage;
import org.ciscavate.cjwizard.WizardSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This page is used to specify which character server to install (e.g. media semantics character).
 * 
 * @author mhoffman
 *
 */
public class AvatarPage extends WizardPage {

    /**
     * default value
     */
    private static final long serialVersionUID = 1L;
    
    /** Logger instance */
    private static Logger logger = LoggerFactory.getLogger(AvatarPage.class);
    
    /** dialog labels */
    private static final String TITLE = "Character Server";
    private static final String DESCRIPTION = "Character Server";
    
    /** gift tutoring files page */
    private static final String FILES_URL = "https://gifttutoring.org/projects/gift/files";
    private static final String FILES_LINK = "<html><a href="+FILES_URL+"\">"+FILES_URL+"</a></html>";
    
    /** the option for installing the Media Semantics Character application */
    private JCheckBox mscInstallCheckbox;
    
    /** the option for confirming that Virtual Human is installed */
    private JCheckBox vhInstallCheckbox = new JCheckBox("<html><h3>I have installed the Virtual Human Character Server<i><br/><font color=\"red\">(check to continue)</font></i></h3></html>");
    
    private JPanel pagePanel = new JPanel();
    
    /**
     * Default constructor 
     */
    public AvatarPage(){
        super(TITLE, DESCRIPTION);

        setupUi();
    }
    
    @Override
    public void updateSettings(WizardSettings settings) {
        super.updateSettings(settings);
        settings.put(InstallSettings.MEDIA_SEMANTICS, (mscInstallCheckbox != null && mscInstallCheckbox.isSelected()));
    }
    
    @Override
    public void rendering(List<WizardPage> path, WizardSettings settings) {
        setNextEnabled(vhInstallCheckbox.isSelected());
    }
    
    /**
     * Sets up all of the swing components
     */
    private void setupUi() {
        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));      
        pagePanel.setLayout(new BoxLayout(pagePanel, BoxLayout.Y_AXIS));
        
        //
        // Virtual Human character
        //
        createVHPanel();
        
        //
        // Media Semantics Character (MSC)
        //
        createMSCPanel();        

        add(pagePanel);
    }
    
    /**
     * Create GUI components for Virtual Human character install option
     */
    private void createVHPanel(){
        
        setNextEnabled(vhInstallCheckbox.isSelected());
        
        JPanel descPanel = new JPanel();
        descPanel.setLayout(new GridBagLayout());
        GridBagConstraints descPanelConstraints = new GridBagConstraints();
        descPanelConstraints.fill = GridBagConstraints.WEST; 
        descPanelConstraints.gridy = 0;
        
        vhInstallCheckbox.setAlignmentX(Component.LEFT_ALIGNMENT);
        vhInstallCheckbox.setPreferredSize(new Dimension(500, 50));
        vhInstallCheckbox.addItemListener(new ItemListener() {
        
            @Override
            public void itemStateChanged(ItemEvent itemEvent) {                
                setNextEnabled(itemEvent.getStateChange() == ItemEvent.SELECTED);
            }
        });
        descPanel.add(vhInstallCheckbox, descPanelConstraints);
        
        JLabel label = new JLabel("<html>Virtual Human is the default character server for GIFT.  It provides interactive characters "+
                                        "and dynamic speech to engage the learner in the GIFT Tutor User Interface web browser.<br/><br/>"+
                                        "<b>Note:</b> You can still run GIFT without Virtual Human but a warning will appear each time you "+
                                        "start GIFT if no character server of any kind is available.<br/><br/>" +
                                        "Visit the link below to find the latest Virtual Human Character server download package:</html>");
        descPanelConstraints.fill = GridBagConstraints.RELATIVE;
        descPanelConstraints.gridy = ++descPanelConstraints.gridy;
        label.setPreferredSize(new Dimension(450, 115));
        descPanel.add(label, descPanelConstraints);        
        
        descPanelConstraints.gridy = ++descPanelConstraints.gridy;   
        JLabel linkLabel = new JLabel(FILES_LINK);
        linkLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        linkLabel.setPreferredSize(new Dimension(450, 20));
        descPanel.add(linkLabel, descPanelConstraints);
        linkLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    if(Desktop.isDesktopSupported()){
                        Desktop.getDesktop().browse(new URI(FILES_URL));
                    }
                } catch (Exception ex) {
                    logger.error("Caught exception while trying to open URL of "+FILES_URL+".", ex);
                }
            }
        });

        descPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        pagePanel.add(descPanel);        
    }
        
    /**
     * Create GUI components for Media Semantics Character (MSC) install option
     */
    private void createMSCPanel(){
        
        JPanel descPanel = new JPanel();
        descPanel.setLayout(new GridBagLayout());
        GridBagConstraints descPanelConstraints = new GridBagConstraints();
        descPanelConstraints.fill = GridBagConstraints.WEST; 
        descPanelConstraints.gridy = 0;
        
        mscInstallCheckbox = new JCheckBox("<html><h3>Install Media Semantics Character (MSC)</h3></html>");  
        mscInstallCheckbox.setAlignmentX(Component.LEFT_ALIGNMENT);
        mscInstallCheckbox.setPreferredSize(new Dimension(500, 30));
        descPanel.add(mscInstallCheckbox, descPanelConstraints);
        
        JLabel label = new JLabel("<html>MSC Provides interactive characters and dynamic speech to engage the learner in the " +
        		"GIFT Tutor User Interface web browser.</html>");
        label.setPreferredSize(new Dimension(450, 30));
        descPanelConstraints.fill = GridBagConstraints.RELATIVE;
        descPanelConstraints.gridy = ++descPanelConstraints.gridy;
        descPanel.add(label, descPanelConstraints);
        
        if(!InstallThread.MSC_BUILDER_EXE.exists() || !InstallThread.MSC_SERVER_EXE.exists()){
            //unable to find the MSC builder OR MSC server install exe
            mscInstallCheckbox.setEnabled(false);
        }
        
        if(!mscInstallCheckbox.isEnabled()){
            
            descPanelConstraints.gridy = ++descPanelConstraints.gridy;
            
            StringBuffer errorMsg = new StringBuffer();
            errorMsg.append("<html><br/><font color=\"black\">If you would like GIFT to install Media Semantics application(s) for you, ")
                    .append("please download these file(s):<ul>");
            
            if(!InstallThread.MSC_BUILDER_EXE.exists()){
                errorMsg.append("<li>").append(InstallThread.MSC_BUILDER_EXE.getName()).append("</li>");
            }
            
            if(!InstallThread.MSC_SERVER_EXE.exists()){
                errorMsg.append("<li>").append(InstallThread.MSC_SERVER_EXE.getName()).append("</li>");
            }
            
            errorMsg.append("</ul>from the link below:</font></html>");
            
            JLabel errorLabelA = new JLabel(errorMsg.toString());
            errorLabelA.setPreferredSize(new Dimension(450, 100));
            descPanel.add(errorLabelA, descPanelConstraints);
            
            descPanelConstraints.gridy = ++descPanelConstraints.gridy;
            JLabel linkLabel = new JLabel(FILES_LINK);
            linkLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            linkLabel.setPreferredSize(new Dimension(450, 20));
            descPanel.add(linkLabel, descPanelConstraints);
            
            linkLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    try {
                        if(Desktop.isDesktopSupported()){
                            Desktop.getDesktop().browse(new URI(FILES_URL));
                        }
                    } catch (Exception ex) {
                        logger.error("Caught exception while trying to open URL of "+FILES_URL+".", ex);
                    }
                }
            });
            
            descPanelConstraints.gridy = ++descPanelConstraints.gridy;
            
            JLabel errorLabelB = new JLabel("<html><br/><font color=black>Then place the file(s) in GIFT/external.  Finally, " +
            		"restart this installation and the checkbox will become enabled.</font></html>");
            errorLabelB.setPreferredSize(new Dimension(450, 40));
            descPanel.add(errorLabelB, descPanelConstraints);
        }
        
        descPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        pagePanel.add(descPanel);        
    }
}
