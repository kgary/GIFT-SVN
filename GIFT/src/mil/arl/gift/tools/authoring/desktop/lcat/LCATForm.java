/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.desktop.lcat;

import java.io.File;
import java.net.URI;

import generated.learner.LearnerConfiguration;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.common.io.HelpDocuments;
import mil.arl.gift.common.io.UnmarshalledFile;
import mil.arl.gift.tools.authoring.common.ValidationUtil;
import mil.arl.gift.tools.authoring.common.conversion.AbstractConversionWizardUtil;
import mil.arl.gift.tools.authoring.desktop.common.XMLAuthoringToolForm;

/**
 * This class extends the XML authoring tool form by providing LCAT specific parameters.
 * In addition it can validate the learner configuration xml using the learner configuration file handler.
 * 
 * @author mhoffman
 *
 */
public class LCATForm extends XMLAuthoringToolForm {
    
    private static final long serialVersionUID = 1L;
    
    /** default title of form and prefix used when xml has been loaded*/
    private static final String TITLE_PREFIX = "Learner Configuration Authoring Tool"; 
    
    /** the documentation */
    private static URI DOC_FILE = HelpDocuments.getConfigSettingsDoc();
    
    /** the root generated class for the learner config content*/
    private static final Class<?> ROOT_CLASS = LearnerConfiguration.class;
    
    private static final String FILE_COMMENT = "This file was created with the GIFT Learner Configuration Authoring Tool (LCAT).\n" +
    		"It contains configuration information for how to translate incoming sensor data into learner state attributes.";
    
    /**
     * Default constructor - use default schema
     * @throws DetailedException if there was a problem with the learner configuration file schema
     */
    public LCATForm() throws DetailedException{
        this(AbstractSchemaHandler.LEARNER_SCHEMA_FILE);

    }
    
    /**
     * Class constructor - use custom schema
     * 
     * @param schemaFile - the learner config schema file
     * @throws DetailedException if there was a problem with the learner configuration file schema
     */
    public LCATForm(File schemaFile) throws DetailedException{
        super(schemaFile, ROOT_CLASS, LCATProperties.getInstance().getWorkspaceDirectory(), TITLE_PREFIX, FILE_COMMENT, DOC_FILE, AbstractSchemaHandler.LEARNER_CONFIG_FILE_EXTENSION);
    }
    
    @Override
    protected Object executeConversion(FileProxy fileToConvert) throws Exception{
    	
    	// Get the conversion util based off of which version is being converted.
    	AbstractConversionWizardUtil conversionUtil = AbstractConversionWizardUtil.getConversionUtil(fileToConvert, true);
    	
    	if (conversionUtil != null) {    	
    		UnmarshalledFile unmarshalledFile = conversionUtil.convertLearnerConfiguration(fileToConvert, true, true);
    		generated.learner.LearnerConfiguration newLearnerConfig = (LearnerConfiguration) unmarshalledFile.getUnmarshalled();
        
    		return newLearnerConfig;
    	}
    	
    	return null;
    }
    
    @Override
    public void giftValidate(FileProxy contentFile) throws Throwable{
        ValidationUtil.validateLearnerConfiguration(contentFile, true);
    }
}
