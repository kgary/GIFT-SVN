/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.desktop.pcat;

import java.io.File;
import java.net.URI;

import generated.ped.EMAP;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.common.io.HelpDocuments;
import mil.arl.gift.common.io.UnmarshalledFile;
import mil.arl.gift.tools.authoring.common.ValidationUtil;
import mil.arl.gift.tools.authoring.common.conversion.AbstractConversionWizardUtil;
import mil.arl.gift.tools.authoring.desktop.common.XMLAuthoringToolForm;

/**
 * This class extends the XML authoring tool form by providing PCAT specific parameters.
 * In addition it can validate the pedagogy configuration xml using the pedagogy configuration file handler.
 * 
 * @author mhoffman
 *
 */
public class PCATForm extends XMLAuthoringToolForm {
    
    private static final long serialVersionUID = 1L;
    
    /** default title of form and prefix used when xml has been loaded*/
    private static final String TITLE_PREFIX = "Pedagogy Configuration Authoring Tool"; 
    
    /** the documentation */
    private static URI DOC_FILE = HelpDocuments.getConfigSettingsDoc();
    
    /** the root generated class for the content*/
    private static final Class<?> ROOT_CLASS = EMAP.class;
    
    private static final String FILE_COMMENT = "\n\tThis file was created with the GIFT Pedagogy Configuration Authoring Tool (PCAT).\n" +
            "\tThis file contains configuration information for pedagogical models (e.g. eMAP).\n";
    
    /**
     * Default constructor - use default schema
     * @throws DetailedException if there was a problem with the pedagogy configuration file schema
     */
    public PCATForm() throws DetailedException{
        this(AbstractSchemaHandler.EMAP_PEDAGOGICAL_SCHEMA_FILE);

    }
    
    /**
     * Class constructor - use custom schema
     * 
     * @param schemaFile - the metadata schema file
     * @throws DetailedException if there was a problem with the pedagogy configuration file schema
     */
    public PCATForm(File schemaFile) throws DetailedException{
        super(schemaFile, ROOT_CLASS, PCATProperties.getInstance().getWorkspaceDirectory(), TITLE_PREFIX, FILE_COMMENT, DOC_FILE, AbstractSchemaHandler.EMAP_PEDAGOGICAL_CONFIG_FILE_EXTENSION);
    }
    
    /**
     * Initialize and show the authoring tool form
     */
    public void init(){
        super.setVisible(true);        
    }
    
    @Override
    protected Object executeConversion(FileProxy fileToConvert) throws Exception{
        
    	// Get the conversion util based off of which version is being converted.
    	AbstractConversionWizardUtil conversionUtil = AbstractConversionWizardUtil.getConversionUtil(fileToConvert, true);
    	
    	if (conversionUtil != null) {    	
    		UnmarshalledFile uFile = conversionUtil.convertEMAPConfiguration(fileToConvert, true, true);
    		generated.ped.EMAP newPedConfig = (EMAP)uFile.getUnmarshalled();
        
    		return newPedConfig;
    	}
    	
    	return null;
    }
    
    @Override
    protected void giftValidate(FileProxy contentFile) throws Throwable{
        ValidationUtil.validatePedagogyConfiguration(contentFile, true);
    }

}
