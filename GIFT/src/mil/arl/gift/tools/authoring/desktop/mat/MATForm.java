/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.desktop.mat;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URI;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.xerces.dom.TextImpl;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import generated.metadata.Metadata;
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
 * This class extends the XML authoring tool form by providing MAT specific parameters.
 * In addition it can validate the metadata xml using the metadata file handler.
 * 
 * @author mhoffman
 *
 */
public class MATForm extends XMLAuthoringToolForm {
    
    private static final long serialVersionUID = 1L;
    
    /** the default starting location for the metadata file browser */
    private static final String DEFAULT_DIRECTORY = CommonProperties.getInstance().getWorkspaceDirectory();
    
    /** default title of form and prefix used when xml has been loaded*/
    private static final String TITLE_PREFIX = "Metadata Authoring Tool"; 
    
    /** the documentation */
    private static URI DOC_FILE = HelpDocuments.getConfigSettingsDoc();
    
    /** specifies the extension or suffix used on metadata files */
    private static final String CONTENT_FILE_SUFFIX = AbstractSchemaHandler.METADATA_FILE_EXTENSION;
    
    /** the root generated class for the metadata content*/
    private static final Class<?> ROOT_CLASS = Metadata.class;
    
    private static final String SIMPLE_REF = "simple";
    private static final String TRAINING_APP_REF = "trainingApp";
    private static final String URL_REF = "URL";
    private static final String LESSON_MATERIAL_REF = "lessonMaterial";
    
    private static final String PERIOD = ".";
    
    private static final String FILE_COMMENT = "\n\tThis file was created with the GIFT Metadata Authoring Tool (MAT).\n" +
    		"\tThis file contains metadata attributes about a single domain resource file (e.g. PowerPoint show).\n" +
    		"\tMetadata attributes are content dependent and learner state independent.\n";
    
    /**
     * Default constructor - use default schema
     * @throws DetailedException if there was a problem with the metadata file schema
     */
    public MATForm() throws DetailedException{
        this(AbstractSchemaHandler.METADATA_SCHEMA_FILE);

    }
    
    /**
     * Class constructor - use custom schema
     * 
     * @param schemaFile - the metadata schema file
     * @throws DetailedException if there was a problem with the metadata file schema
     */
    public MATForm(File schemaFile) throws DetailedException{
        super(schemaFile, ROOT_CLASS, DEFAULT_DIRECTORY, TITLE_PREFIX, FILE_COMMENT, DOC_FILE, CONTENT_FILE_SUFFIX);
    }
    
    /**
     * Initialize and show the authoring tool form
     */
    public void init(){
        super.setVisible(true);
        
        //change default save logic by allowing no save as option and save right away because
        //the MAT will automatically create the metadata file name based on the metadata's reference
        //content file name (see 'saveXML' method below)
        saveAsXMLMenuItem.addPropertyChangeListener(new PropertyChangeListener() {
            
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                
                if(saveAsXMLMenuItem.isEnabled()){
                    
                    SwingUtilities.invokeLater(new Runnable() {
                        
                        @Override
                        public void run() {
                            saveAsXMLMenuItem.setEnabled(false);
                            saveXMLMenuItem.setEnabled(true);                            
                        }
                    });
                    
                }
//                System.out.println("property - "+evt);
            }
        });
        
        
    }
    
    @Override
    protected void giftValidate(FileProxy contentFile) throws Throwable{
        File courseFolder = this.updateCourseFolder();
        GIFTValidationResults validationResults = ValidationUtil.validateMetadata(contentFile, new DesktopFolderProxy(courseFolder), true);
        if(validationResults.hasCriticalIssue() || validationResults.hasImportantIssues()){
            throw validationResults.getFirstError();
        }
    }
    
    @Override
    protected Object executeConversion(FileProxy fileToConvert) throws Exception{
    	
    	// Get the conversion util based off of which version is being converted.
    	AbstractConversionWizardUtil conversionUtil = AbstractConversionWizardUtil.getConversionUtil(fileToConvert, true);
    	
    	if (conversionUtil != null) {    	
    		UnmarshalledFile uFile = conversionUtil.convertMetadata(fileToConvert, true, true);
    		generated.metadata.Metadata newMetadata = (Metadata) uFile.getUnmarshalled();
        
    		return newMetadata;
    	}
    	
    	return null;
    }
    
    @Override
    protected boolean saveXML(File file){
        
        //
        // Get file to save too by looking at the content file reference metadata xml node value
        // This logic will automatically name the metadata file after the referenced data file and place
        // the metadata file in the same directory.
        // Note: doing this every time in case the content file node value has changed
        //
            
        String fileName = null;
        Element rootNode = this.getRootNode();
        NodeList nl = rootNode.getElementsByTagName(SIMPLE_REF);
        if(nl != null && nl.getLength() == 1){
            //found simpleRef element
            
            Element contentFile = (Element)nl.item(0);
            fileName = ((TextImpl)contentFile.getFirstChild().getFirstChild()).getData();

        }else{
            
            nl = rootNode.getElementsByTagName(TRAINING_APP_REF);
            if(nl != null && nl.getLength() == 1){
                //found training app ref
                Element contentFile = (Element)nl.item(0);
                fileName = ((TextImpl)contentFile.getFirstChild().getFirstChild()).getData();
            }else{
                
                nl = rootNode.getElementsByTagName(URL_REF);
                if(nl != null && nl.getLength() == 1){
                    //found URL ref
                    Element contentFile = (Element)nl.item(0);
                    fileName = ((TextImpl)contentFile.getFirstChild().getFirstChild()).getData();
                }else{
                    
                    nl = rootNode.getElementsByTagName(LESSON_MATERIAL_REF);
                    if(nl != null && nl.getLength() == 1){
                        //found lesson material ref
                        Element contentFile = (Element)nl.item(0);
                        fileName = ((TextImpl)contentFile.getFirstChild().getFirstChild()).getData();
                    }
                }
            }
        }
        
        if(fileName != null && fileName.length() > 0){
            //replace file extension
            
            int lastIndex = fileName.lastIndexOf(PERIOD);
            String metadataFileName = fileName.substring(0, lastIndex) + CONTENT_FILE_SUFFIX;
            file = new File(CommonProperties.getInstance().getWorkspaceDirectory() + File.separator + metadataFileName);
        }else{
            JOptionPane.showMessageDialog(this, "Please populate the reference (Simple or Training App Ref) before attempting to save because the metadata file name is created from that element's value.",
                    "Content File Required", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        return super.saveXML(file);
    }
}
