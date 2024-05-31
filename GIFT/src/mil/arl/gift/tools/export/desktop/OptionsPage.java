/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.export.desktop;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;

import org.ciscavate.cjwizard.WizardPage;
import org.ciscavate.cjwizard.WizardSettings;

/**
 * Shows the user a page of general export options.
 * 
 * @author mhoffman
 *
 */
public class OptionsPage extends WizardPage {
    
    /** Generated serial */
    private static final long serialVersionUID = -1L;
    
    private static final String TITLE = "Options";
    private static final String DESCRIPTION = "Options";
    
    /** the option for exporting domain content only */
    private JCheckBox contentOnlyCheckbox;
    
    /** the option for exporting user data in database(s) */
    private JCheckBox exportUserDataCheckbox;
    
    public OptionsPage(){
        super(TITLE, DESCRIPTION);
        
        setupUi();
    }
    
    @Override
    public void updateSettings(WizardSettings settings) {
        super.updateSettings(settings);
        settings.put(ExportSettings.getExportDomainContentOnly(), contentOnlyCheckbox.isSelected());
        settings.put(ExportSettings.getExportUserData(), exportUserDataCheckbox.isEnabled() && exportUserDataCheckbox.isSelected());
    }
    
    /**
     * Sets up the user interface.
     */
    private void setupUi() {
        
        JLabel nameLabel = new JLabel("<html><u><b>Option Name</b></u></html>");
        JLabel descriptionLabel = new JLabel("<html><u><b>Description</b></u></html>");        
        contentOnlyCheckbox = new JCheckBox("Domain Content Only");       
        JLabel contentOnlyDescription = new JLabel("<html>Whether or not to export selected Domain content only,<br>" +
        		"rather than the entire GIFT source plus selected Domain<br> content.</html>"); 
          
        exportUserDataCheckbox = new JCheckBox("Export LMS Learner History");       
        final JLabel exportLMSHistoryDescription = new JLabel("<html>Whether or not to export currently saved User<br>"+
        		"data from the GIFT database(s) (e.g. user id's, survey responses, <br>course history, etc.).</html>");
        
        contentOnlyCheckbox.addActionListener(new ActionListener(){
        	@Override
            public void actionPerformed(ActionEvent e){
        		exportUserDataCheckbox.setEnabled(!exportUserDataCheckbox.isEnabled());
        		exportLMSHistoryDescription.setEnabled(!exportLMSHistoryDescription.isEnabled());
        	}
        });
        
        //Note: created using netbeans GUI design tool
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(nameLabel)
                    .addComponent(contentOnlyCheckbox)
                    .addComponent(exportUserDataCheckbox))
                .addGap(34, 34, 34)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(contentOnlyDescription, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(exportLMSHistoryDescription)
                    .addComponent(descriptionLabel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nameLabel)
                    .addComponent(descriptionLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(contentOnlyCheckbox)
                    .addComponent(contentOnlyDescription, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)  
                    .addComponent(exportUserDataCheckbox)
                    .addComponent(exportLMSHistoryDescription, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(222, Short.MAX_VALUE))
        );

    }
}
