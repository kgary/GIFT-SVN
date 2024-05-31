/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.common.conversion;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.common.io.UnmarshalledFile;
import mil.arl.gift.common.survey.AbstractQuestion;
import mil.arl.gift.common.survey.SurveyContext;
import mil.arl.gift.net.api.message.codec.json.survey.QuestionJSON;
import mil.arl.gift.net.api.message.codec.json.survey.SurveyContextJSON;

/**
 * Provides methods used to parse legacy XML files by using the GIFT conversion logic.
 * 
 * @author mhoffman
 *
 */
public class AbstractLegacySchemaHandler extends AbstractSchemaHandler {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(AbstractLegacySchemaHandler.class);

    /**
     * Set attribute(s)
     * 
     * @param schemaFile - the schema file handled by this class. Must be an existing file.
     */
    public AbstractLegacySchemaHandler(File schemaFile) {
        super(schemaFile);

    }
    
    /**
     * Return the unmarshalled file representation for the file specified. 
     * This file is converted if necessary.
     * 
     * @param fileProxy contains the file contents
     * @param fileType the type of file being unmarshalled (parsed)
     * @return the unmarshalled file
     * @throws FileNotFoundException if the file to unmarshal could not be found
     * @throws DetailedException if there was a problem reading the file
     * @throws IllegalArgumentException if there is a problem with one of the method arguments
     * @throws UnsupportedVersionException if the file that the user is trying to convert is not registered for conversion
     */
    public static UnmarshalledFile getUnmarshalledFile(FileProxy fileProxy, FileType fileType) 
            throws FileNotFoundException, DetailedException, IllegalArgumentException, UnsupportedVersionException{
        
        AbstractConversionWizardUtil cWizard = null;
        UnmarshalledFile preconvertedObj = null;
        UnmarshalledFile uFile;
        String version = null;
        boolean upconverted = false;
        try{
            try{
                
                if (fileType.equals(FileType.QUESTION_EXPORT)) {
                    // no conversion logic

                    String fileContent = IOUtils.toString(fileProxy.getInputStream());
                    JSONObject obj = (JSONObject) JSONValue.parse(fileContent);
                    QuestionJSON json = new QuestionJSON();
                    AbstractQuestion question = (AbstractQuestion) json.decode(obj);
                    uFile = new UnmarshalledFile(question);
                } else if (fileType.equals(FileType.SURVEY_EXPORT)) {
                    // no conversion logic

                    String fileContent = IOUtils.toString(fileProxy.getInputStream());
                    JSONObject obj = (JSONObject) JSONValue.parse(fileContent);
                    SurveyContextJSON json = new SurveyContextJSON();
                    SurveyContext surveyContext = (SurveyContext) json.decode(obj);
                    uFile = new UnmarshalledFile(surveyContext);
                } else {
                    cWizard = AbstractConversionWizardUtil.getConversionUtil(fileProxy, false);
                    version = cWizard.getWorkingVersion();
                     
                    if(fileType.equals(FileType.INTEROP_CONFIGURATION)|| fileType.equals(FileType.LESSON_MATERIAL_REF)) {
                        // no conversion logic developed yet for these file types
                        uFile = AbstractSchemaHandler.parseAndValidate(fileProxy.getInputStream(), fileType, false);
                        
                    } else if(fileType.equals(FileType.TRAINING_APP_REFERENCE)) {
                        // no need to create backup of this file type
                        //Note: unmarshal even if there is a schema validation in order to support disabled (incomplete) course objects [#3570]
                        uFile = cWizard.convertTrainingApplicationRef(fileProxy, false, false);                
                        upconverted = true;
                        
                    } else {
                        // Store the original file contents to create a backup later
                        preconvertedObj = AbstractSchemaHandler.parseAndValidate(cWizard.getPreviousSchemaRoot(fileType), 
                                fileProxy.getInputStream(), cWizard.getPreviousSchemaFile(fileType), false);
                        
                        //Note: unmarshal even if there is a schema validation in order to support disabled (incomplete) course objects [#3570]
                        uFile = cWizard.convertFile(fileType, fileProxy, false, false);                
                        upconverted = true;
                    }
                }
            }catch(@SuppressWarnings("unused") LatestVersionException e){
                if(logger.isInfoEnabled()){
                    logger.info("The file "+fileProxy+" is the latest GIFT version, no need to upconvert.");
                }
                uFile = AbstractSchemaHandler.parseAndValidate(fileProxy.getInputStream(), fileType, false);
            }
        }catch(JAXBException | SAXException | IOException e){
            throw new DetailedException("Failed to parse and validate the "+fileType+".", 
                    "An exception was thrown while trying to parse and validate '"+fileProxy.getFileId()+"' because "+e.getMessage()+".", e);
        }catch (Exception e){
            throw new DetailedException("There was a general problem when converting the file "+fileProxy.getFileId()+"'.", "An exception was thrown with the message "+e.getMessage()+".", e);
        }
        
        if(preconvertedObj != null){
            uFile.setPreConverted(preconvertedObj.getUnmarshalled());
        }
        
        uFile.setUpconverted(upconverted);
        uFile.setPreconvertedVersion(version);
        
        if(cWizard != null){
            uFile.setConversionIssueList(cWizard.getConversionIssueList());
        }
        
        return uFile;
    }

}
