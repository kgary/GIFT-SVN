/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.installer;

import java.awt.GridBagLayout;
import java.io.File;

import javax.swing.JCheckBox;
import javax.swing.JLabel;

import org.ciscavate.cjwizard.WizardPage;
import org.ciscavate.cjwizard.WizardSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This page is responsible for determining if the GIFT database needs to be installed (i.e. extracted)
 * as part of the installation process. 
 * 
 * @author mhoffman
 *
 */
public class DatabasePage extends WizardPage {

    /**
     * default value
     */
    private static final long serialVersionUID = 1L;
    
    private static Logger logger = LoggerFactory.getLogger(DatabasePage.class);
    
    /** dialog labels */
    private static final String TITLE = "Database";
    private static final String DESCRIPTION = "Database";
    
    /** the database directories to check if they exist */
    private static final File UMS_DB_HOME = new File("data" + File.separator + "derbyDb" + File.separator + "GiftUms");
    private static final File LMS_DB_HOME = new File("data" + File.separator + "derbyDb" + File.separator + "GiftLms");
    
    /** flag to indicate whether the databases need to be installed */
    private boolean extractUMSDb;
    private boolean extractLMSDb;
    
    /** the option for extracting the database */
    private JCheckBox extractDbCheckbox;
    
    /**
     * Class constructor - build page GUI components
     */
    public DatabasePage(){
        super(TITLE, DESCRIPTION);
        setupUi();
    }
    
    @Override
    public void updateSettings(WizardSettings settings) {
        super.updateSettings(settings);
        settings.put(InstallSettings.EXTRACT_UMS_DATABASE, extractUMSDb || (extractDbCheckbox != null && extractDbCheckbox.isSelected()));
        settings.put(InstallSettings.EXTRACT_LMS_DATABASE, extractLMSDb || (extractDbCheckbox != null && extractDbCheckbox.isSelected()));
    }
    
    /**
     * Sets up all of the swing components
     */
    private void setupUi() {
        
        //reset
        extractUMSDb = false;
        extractLMSDb = false;
        
        //cause the components to be centered vertically
        this.setLayout(new GridBagLayout());
        
        if(UMS_DB_HOME.exists() && LMS_DB_HOME.exists()){
            //show checkbox asking if user wishes to over-write existing database with original database
            logger.info("The GIFT database already exists.");
            
            extractDbCheckbox = new JCheckBox("<html><b>Would you like to over-write existing database(s) with original GIFT database entries?</b><br>" +
            		"Note: Only do this if you understand the consequences of replacing the existing databases<br>(e.g. causing course validation errors due to missing survey elements)<br></html>");   
            add(extractDbCheckbox);
            
        }else if(!UMS_DB_HOME.exists() && !LMS_DB_HOME.exists()){
            //show label that the derby database will be extracted
            
            JLabel label = new JLabel("The GIFT UMS and LMS database(s) will be installed.");
            add(label);
            
            extractUMSDb = true;
            extractLMSDb = true;
        }else if(!UMS_DB_HOME.exists()){
            
            JLabel label = new JLabel("The GIFT UMS database(s) will be installed.");
            add(label);
            
            extractUMSDb = true;
            
        }else if(!LMS_DB_HOME.exists()){
            
            JLabel label = new JLabel("The GIFT LMS database(s) will be installed.");
            add(label);
            
            extractLMSDb = true;
        }else{
            //ERROR
            
            JLabel label = new JLabel("An error occurred when detecting the state of your GIFT databases.");
            add(label);
        }
        
        
        //allow user to continue to next page
        this.setNextEnabled(true);
    }
}
