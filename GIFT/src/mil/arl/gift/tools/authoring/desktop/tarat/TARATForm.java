/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.desktop.tarat;

import java.io.File;
import java.net.URI;

import generated.course.TrainingApplicationWrapper;
import mil.arl.gift.common.course.GIFTValidationResults;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.DesktopFolderProxy;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.common.io.HelpDocuments;
import mil.arl.gift.common.io.UnmarshalledFile;
import mil.arl.gift.tools.authoring.common.CommonProperties;
import mil.arl.gift.tools.authoring.common.ValidationUtil;
import mil.arl.gift.tools.authoring.common.conversion.AbstractConversionWizardUtil;
import mil.arl.gift.tools.authoring.desktop.common.XMLAuthoringToolForm;

/**
 * This class extends the XML authoring tool form by providing TARAT specific parameters.
 * In addition it can validate the xml using the course file handler.
 * 
 * @author mhoffman
 *
 */
public class TARATForm extends XMLAuthoringToolForm {
    
    private static final long serialVersionUID = 1L;
    
    /** the default starting location for the metadata file browser */
    private static final String DEFAULT_DIRECTORY = CommonProperties.getInstance().getWorkspaceDirectory();
    
    /** default title of form and prefix used when xml has been loaded*/
    private static final String TITLE_PREFIX = "Training Application Reference Authoring Tool"; 
    
    /** the documentation */
    private static URI DOC_FILE = HelpDocuments.getConfigSettingsDoc();
    
    /** the root generated class for the metadata content*/
    private static final Class<?> ROOT_CLASS = generated.course.TrainingApplicationWrapper.class;
    
    private static final String FILE_COMMENT = "\n\tThis file was created with the GIFT Training Application Reference Authoring Tool (TARAT).\n" +
    		"\tThis file contains information needed for GIFT to launch a training application course element.\n" +
    		"\tThe information includes the training application file (e.g. powerpoint show, VBS scenario name) as well as other configuration parameters.\n";
    
    /**
     * Default constructor - use default schema
     * @throws DetailedException if there was a problem with the training application reference file schema
     */
    public TARATForm() throws DetailedException{
        this(AbstractSchemaHandler.COURSE_SCHEMA_FILE);

    }
    
    /**
     * Class constructor - use custom schema
     * 
     * @param schemaFile - the metadata schema file
     * @throws DetailedException if there was a problem with the training application reference file schema
     */
    public TARATForm(File schemaFile) throws DetailedException{
        super(schemaFile, ROOT_CLASS, DEFAULT_DIRECTORY, TITLE_PREFIX, FILE_COMMENT, DOC_FILE, AbstractSchemaHandler.TRAINING_APP_FILE_EXTENSION);
    }
    
    /**
     * Initialize and show the authoring tool form
     */
    public void init(){
        super.setVisible(true);        
    }
    
    @Override
    protected void giftValidate(FileProxy contentFile) throws Throwable{
        File courseFolder = this.updateCourseFolder();
        GIFTValidationResults validationResult = 
                ValidationUtil.validateTrainingAppReference(contentFile, new DesktopFolderProxy(courseFolder), true);
        if(validationResult.getCriticalIssue() != null){
            throw validationResult.getCriticalIssue();
        }else if(validationResult.getImportantIssues() != null && !validationResult.getImportantIssues().isEmpty()){
            throw validationResult.getImportantIssues().get(0);  //for now just show the first issue found
        }
    }
    
    @Override
    protected Object executeConversion(FileProxy fileToConvert) throws Exception{
    	
    	// Get the conversion util based off of which version is being converted.
    	AbstractConversionWizardUtil conversionUtil = AbstractConversionWizardUtil.getConversionUtil(fileToConvert, true);
    	
    	if (conversionUtil != null) { 
    	    UnmarshalledFile unmarshalledFile = conversionUtil.convertTrainingApplicationRef(fileToConvert, true, true);
    	    generated.course.TrainingApplicationWrapper newTAWrapper = (TrainingApplicationWrapper) unmarshalledFile.getUnmarshalled();
    		return newTAWrapper;
    	}
    	
    	return null;
    }

}
