/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.metadata;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generated.course.TrainingApplicationWrapper;
import generated.metadata.Metadata;
import mil.arl.gift.common.course.CourseConceptsUtil;
import mil.arl.gift.common.course.GIFTValidationResults;
import mil.arl.gift.common.course.MetadataFileValidationException;
import mil.arl.gift.common.course.dkf.AbstractDKFHandler.AdditionalDKFValidationSettings;
import mil.arl.gift.common.course.dkf.DKFValidationException;
import mil.arl.gift.common.enums.MerrillQuadrantEnum;
import mil.arl.gift.common.enums.MetadataAttributeEnum;
import mil.arl.gift.common.io.AbstractFolderProxy;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.common.io.FileUtil;
import mil.arl.gift.common.io.FileValidationException;
import mil.arl.gift.common.io.UnmarshalledFile;
import mil.arl.gift.common.io.UriUtil;
import mil.arl.gift.common.io.UriUtil.InternetConnectionStatusEnum;
import mil.arl.gift.domain.DomainCourseFileHandler;
import mil.arl.gift.domain.knowledge.conversation.ConversationTreeFileHandler;
import mil.arl.gift.domain.lessonmaterial.LessonMaterialFileHandler;
import mil.arl.gift.net.api.message.codec.json.survey.QuestionJSON;
import mil.arl.gift.tools.authoring.common.conversion.AbstractLegacySchemaHandler;

/**
 * Responsible for parsing a metadata file into a generated class object.
 * This class also offers several validation methods.
 *
 * @author mhoffman
 *
 */
public class MetadataSchemaHandler extends AbstractLegacySchemaHandler{

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(MetadataSchemaHandler.class);    
    
    /** 
     * Enumerated content type that describes the metadata content reference (e.g. powerpoint show, training application
     * reference file)
     */
    public enum MetadataContentType{
        Powerpoint,
        TrainingAppXML,
        LessonMaterialXML,
        HTML,
        URL,
        QuestionExport,
        ConversationTreeXML
    }

    /**
     * Class constructor
     */
    public MetadataSchemaHandler(){
        super(METADATA_SCHEMA_FILE);
    }

    /**
     * Return the metadata generated class instance for the content of the file.
     * This method will also check that the XML file is schema valid.
     *
     * @param file - the metadata XML file to parse
     * @param failOnFirstSchemaError - if true than a validation event will
     *        cause the parsing of the XML content to fail and throw an
     *        exception. If there are no validation events than the XML contents
     *        are XML and schema valid. From Java API docs: A validation event
     *        indicates that a problem was encountered while validating the
     *        incoming XML data during an unmarshal operation, while performing
     *        on-demand validation of the Java content tree, or while
     *        marshalling the Java content tree back to XML data.
     * @return Metadata - the metadata object containing the values from the
     *         file
     * @throws MetadataFileValidationException if there was a problem with
     *         parsing and validating the metadata file against the schema
     */
    public static Metadata getMetadata(FileProxy file, boolean failOnFirstSchemaError)
            throws MetadataFileValidationException {

        try {
            UnmarshalledFile unmarshalledFile = getUnmarshalledFile(file, FileType.METADATA);
            Metadata metadata = (Metadata) unmarshalledFile.getUnmarshalled();
            CourseConceptsUtil.cleanMetadataConcepts(metadata);

            return metadata;
        } catch (Exception e) {
            logger.error("Caught exception while parsing metadata file named " + file.getFileId(), e);

            String details = e.getMessage();
            if (details == null && e.getCause() != null && e.getCause().getMessage() != null) {
                details = e.getCause().getMessage();
            } else {
                details = "There was a problem parsing the metadata file.";
            }

            throw new MetadataFileValidationException(
                    "The metadata failed to be parsed and validated against the schema.", details, file.getFileId(), e);
        }
    }

    /**
     * Check that the metadata XML file is schema and GIFT logic (i.e. do the element values make sense) valid.
     *
     * @param filename the name of the metadata file
     * @param metadata the metadata XML object
     * @param courseDirectory the course directory that contains course related files
     * @param courseConceptNames collection of unique course concept names used to find which DKF concept names
     * are course concepts.<br/>
     * When the dkf has:<br/>
     * 1. remediation option defined, the course concepts in the DKF are used to check for remediation metadata tagged
     * content. <br/>
     * 2. otherwise, the course concepts are used to make sure the DKF contains all of the concepts provided.  This
     * is useful when making sure a metadata referenced DKF actually assesses the concepts it should.<br/>
     * When the course concepts list is null or empty these checks are not performed.
     * @param additionalValidation contains additional validation checks that need to be performed.  Can be null.
     * @param connectionStatus used to indicate whether the Domain module has an Internet connection
     * @param failOnFirstSchemaError - if true than a validation event will cause the parsing of the XML content to fail and throw an exception.
     * If there are no validation events than the XML contents are XML and schema valid.
     * From Java API docs: A validation event indicates that a problem was encountered while validating the incoming XML data during an unmarshal operation, while
     * performing on-demand validation of the Java content tree, or while marshalling the Java content tree back to XML data.
     * @return validation results
     * @throws FileValidationException if there was any type of problem checking the metadata file
     * @throws DKFValidationException if there was a problem parsing the dkf associated with a metadata file reference chain (i.e. metadata->training app ref->dkf)
     */
    public static GIFTValidationResults checkMetadata(String filename, generated.metadata.Metadata metadata, AbstractFolderProxy courseDirectory,
            Set<String> courseConceptNames, AbstractAdditionalValidationSettings additionalValidation, InternetConnectionStatusEnum connectionStatus, boolean failOnFirstSchemaError) throws FileValidationException, DKFValidationException{

        try{
            GIFTValidationResults validationResults = checkMetadata(metadata, courseDirectory, additionalValidation, courseConceptNames, connectionStatus, failOnFirstSchemaError);
            return validationResults;
        }catch(FileValidationException e){
            throw new MetadataFileValidationException("An exception was thrown when validating '"+e.getFileName()+"'.\nReason: "+e.getReason(),
                    e.getDetails(),
                    filename,
                    e);
        }catch(Exception e){

            String details = e.getMessage();
            if(details == null && e.getCause() != null && e.getCause().getMessage() != null){
                details = e.getCause().getMessage();
            }else{
                details = "There was a problem checking the metadata file.";
            }
            throw new MetadataFileValidationException("An exception was thrown",
                    details,
                    filename,
                    e);
        }
    }

    /**
     * Return the content reference string (course folder path to a file or a website) in the provided metadata object.
     *
     * @param metadata the metadata to get the reference string from
     * @return the reference to content in the metadata file (course folder path to a file or a website)
     * @throws Exception if a reference couldn't be found
     */
    public static String getReference(Metadata metadata) throws Exception{

        Serializable content = metadata.getContent();
        if(content instanceof generated.metadata.Metadata.Simple){
            return ((generated.metadata.Metadata.Simple)content).getValue();
        }else if(content instanceof generated.metadata.Metadata.TrainingApp){
            return ((generated.metadata.Metadata.TrainingApp)content).getValue();
        }else if(content instanceof generated.metadata.Metadata.URL){
            return ((generated.metadata.Metadata.URL)content).getValue();
        }else if(content instanceof generated.metadata.Metadata.LessonMaterial){
            return ((generated.metadata.Metadata.LessonMaterial)content).getValue();
        }else{
            throw new Exception("Failed to retrieve the metadata reference.  Was a new metadata element added as a sibling to {simple, trainingApp, URL} ?");
        }
    }

    /**
     * Check that the metadata is GIFT logic (i.e. do the element values make sense) valid.
     *
     * @param metadata the generated metadata class instance to check
     * @param courseDirectory the course directory that contains course related files
     * @param additionalValidation contains additional validation checks that need to be performed.  Can be null
     * @param courseConceptNames collection of unique course concept names used to find which DKF concept names
     * are course concepts.<br/>
     * When the dkf has:<br/>
     * 1. remediation option defined, the course concepts in the DKF are used to check for remediation metadata tagged
     * content. <br/>
     * 2. otherwise, the course concepts are used to make sure the DKF contains all of the concepts provided.  This
     * is useful when making sure a metadata referenced DKF actually assesses the concepts it should.<br/>
     * When the course concepts list is null or empty these checks are not performed.
     * @param status used to indicate whether the Domain module has an Internet connection
     * @param failOnFirstSchemaError - if true than a validation event will cause the parsing of the XML content to fail and throw an exception.
     * If there are no validation events than the XML contents are XML and schema valid.
     * From Java API docs: A validation event indicates that a problem was encountered while validating the incoming XML data during an unmarshal operation, while
     * performing on-demand validation of the Java content tree, or while marshalling the Java content tree back to XML data.
     * @return validation results, wont be null but could have no validation issues.
     * @throws Exception if there was any type of validation error
     * @throws FileValidationException if there was a problem with the metadata file or the file it references
     */
    public static GIFTValidationResults checkMetadata(Metadata metadata, AbstractFolderProxy courseDirectory,
            AbstractAdditionalValidationSettings additionalValidation, Collection<String> courseConceptNames, InternetConnectionStatusEnum status, boolean failOnFirstSchemaError) throws Exception, FileValidationException{

        GIFTValidationResults validationResults = new GIFTValidationResults();

        //
        // First - check the quadrant value.  Value is optional if remediation only is true.
        //    remediation, quadrant (Good)
        //    no remediation, quadrant (Good)
        //    remediation, no quadrant (Good)
        //    no remediation, no quadrant (Bad)
        //
        generated.metadata.PresentAt presentAt = metadata.getPresentAt();
        String merrillQuadrant = presentAt.getMerrillQuadrant();
        MerrillQuadrantEnum quadrant = merrillQuadrant != null ? MerrillQuadrantEnum.valueOf(merrillQuadrant) : null;

        boolean remediationOnly = presentAt.getRemediationOnly() != null && presentAt.getRemediationOnly().equals(generated.metadata.BooleanEnum.TRUE);
        if(!remediationOnly && quadrant == null){
            //no remediation and quadrant value is not valid (i.e. null or wrong string)

            throw new Exception("The metadata must specify, at a minimum, an adaptive courseflow phase (e.g. Rule) OR be flagged as remediation only.");
        }

        //
        // Second - check the Attributes are valid for that quadrant
        //        - and no duplicate concepts
        //
        Set<String> conceptNames = new HashSet<>(metadata.getConcepts().getConcept().size());
        for(generated.metadata.Concept concept : metadata.getConcepts().getConcept()){

            String lowerCaseName = concept.getName().toLowerCase();
            if(conceptNames.contains(lowerCaseName)){
                //found duplicate
                throw new Exception("Found a duplicate concept named '"+concept.getName()+".");
            }

            conceptNames.add(lowerCaseName);

            generated.metadata.ActivityType activityType = concept.getActivityType();
            if(activityType.getType() instanceof generated.metadata.ActivityType.Passive){

                generated.metadata.ActivityType.Passive passive = (generated.metadata.ActivityType.Passive) activityType.getType();
                for(generated.metadata.Attribute attribute : passive.getAttributes().getAttribute()){

                    String value = attribute.getValue();
                    MetadataAttributeEnum attributeEnum = MetadataAttributeEnum.valueOf(value);
                    if(attributeEnum == null){
                        //ERROR
                        validationResults.addImportantIssue(
                                new DetailedException("Found an unhandled metadata attribute value.", "The metadata attribute of "+value+" for concept "+concept.getName()+" is not a valid metadata attribute enumeration.", null));
                    }else{

                        if(quadrant == MerrillQuadrantEnum.PRACTICE && !attributeEnum.isPracticeAttribute()){
                            //ERROR
                            validationResults.addImportantIssue(
                                    new DetailedException("Found an unhandled metadata attribute value.", "The metadata attribute of "+value+" for concept "+concept.getName()+" is not valid for the quadrant value of "+quadrant+".", null));
                        }else if(quadrant != null && quadrant != MerrillQuadrantEnum.PRACTICE && !attributeEnum.isContentAttribute()){
                            //ERROR
                            validationResults.addImportantIssue(
                                    new DetailedException("Found an unhandled metadata attribute value.", "The metadata attribute of "+value+" for concept "+concept.getName()+" is not valid for the quadrant value of "+quadrant+".", null));
                        }

                    }
                }
            }
        }

        //
        // Third - check the content reference
        //
        String reference = getReference(metadata);

        //does the reference exist?
        try{
            UriUtil.validateUri(reference, courseDirectory, status);
        }catch(Exception e){

            if(e.getCause() instanceof java.net.MalformedURLException) {

                // notify author of malformed URL
                throw new Exception("The metadata contains the reference of "+reference+" is invalid.  Reason: The specified URI is malformed.  The domain directory of "+courseDirectory+" was checked as well as trying to reach the reference as a URL.", e);

            } else if(e instanceof java.net.ConnectException) {

                // notify author of invalid HTTP status response
                throw new Exception("The metadata contains the reference of "+reference+" is invalid.  Reason: Received an invalid HTTP response code while connecting to the specified URI. The domain directory of "+courseDirectory+" was checked as well as trying to reach the reference as a URL.", e);

            } else {

                // notify author of general media validation failure
                throw new Exception("The metadata contains the reference of "+reference+" is invalid.  Reason:  The specified URI could not be verified as a reference to a valid local file or web resource. The domain directory of "+courseDirectory+" was checked as well as trying to reach the reference as a URL.", e);
            }

        }

        //is the reference supported?
        if(!isMetadataContentTypeSupported(reference)){
            throw new Exception("The metadata references content '"+reference+"' which is not one of the supported types. Does the file exist?  Does the URL start with a valid protocol like 'http://'?");
        }

        MetadataContentType contentType = getMetadataContentType(reference);
        GIFTValidationResults metadataValidationResults =
                checkMetadataContentReference(contentType, reference, additionalValidation, courseDirectory, courseConceptNames, status, failOnFirstSchemaError);
        validationResults.addGIFTValidationResults(metadataValidationResults);


        return validationResults;
    }
    
    /**
     * Return whether or not the metadata content reference file is supported by GIFT's branch point course
     * element logic.
     * 
     * @param contentFileName the domain relative content file name from a metadata file.
     * @return boolean true iff the content file is supported.
     */
    public static boolean isMetadataContentTypeSupported(String contentFileName){
        
        MetadataContentType type = getMetadataContentType(contentFileName);
        return type != null;
    }
    
    /**
     * Determine the type of branch point content based on the content reference.
     *  ppsx, pps, ppsm = PowerPoint show
     *  trainingapp.xml = training application reference XML
     *  htm, html = local HTML file
     *  
     *  
     * @param contentReference the content reference to check
     * @return {@link MetadataContentType} enumerated type.  Null if a type is not found.
     */
    public static MetadataContentType getMetadataContentType(String contentReference){
        
        MetadataContentType type = null;
        if(contentReference != null){
            
            if(DomainCourseFileHandler.isSupportedPowerPointShow(contentReference)){
                //it's a powerpoint show
                type = MetadataContentType.Powerpoint;
                
            }else if(contentReference.endsWith(AbstractSchemaHandler.TRAINING_APP_FILE_EXTENSION)){
                //it's a training app element XML
                type = MetadataContentType.TrainingAppXML;
                
            }else if(contentReference.endsWith(AbstractSchemaHandler.LESSON_MATERIAL_FILE_EXTENSION)){
                //it's a lesson material element XML
                type = MetadataContentType.LessonMaterialXML;
                
            }else if(DomainCourseFileHandler.isSupportedURL(contentReference)){
                //it's a URL
                type = MetadataContentType.URL;
            }else if(DomainCourseFileHandler.isSupportedHTML(contentReference)){
                /* it's a HTML file; make sure URL is checked before HTML
                 * because the URL can point to an HTML file and we don't want
                 * to get a false positive */
                type = MetadataContentType.HTML;
            }else if(contentReference.endsWith(FileUtil.QUESTION_EXPORT_SUFFIX)){
                //it's a survey export
                type = MetadataContentType.QuestionExport;
            }else if(contentReference.endsWith(AbstractSchemaHandler.CONVERSATION_TREE_FILE_EXTENSION)){
                // it's a conversation tree
                type = MetadataContentType.ConversationTreeXML;
            }
        }
        
        return type;
    }

    /**
     * Returns whether there is at least one metadata file that references the branch point concepts
     * being practiced.
     *
     * @param quadrantMetadataFiles the metadata files to check.
     * @param branchPtConcepts collection of concepts covered by a branch point course element.  If this list is empty
     * this method will always return true.
     * @return boolean - whether at least 1 of the metadata files assesses, at a minimum, the concepts in the branch point.
     */
    public static boolean metadataFileConceptCheck(Map<FileProxy, generated.metadata.Metadata> quadrantMetadataFiles, List<String> branchPtConcepts){

        boolean match = false;

        AdditionalDKFValidationSettings additionalValidation = new AdditionalDKFValidationSettings("Metadata validation for course concepts that need assessment");
        additionalValidation.setMatchedNamesNeedScoring(true);
        additionalValidation.setNodeNamesToMatch(branchPtConcepts);

        //contains the list of branch point concepts that have at least 1 metadata file associated with it
        List<String> conceptsCovered = new ArrayList<>(branchPtConcepts.size());

        //checking each of the branch point concepts
        for(String branchPtConcept : branchPtConcepts){

            match = false;

            //check all of the available metadata files that reference the appopriate quadrant
            for(FileProxy metadataFile : quadrantMetadataFiles.keySet()){

                generated.metadata.Metadata metadata = quadrantMetadataFiles.get(metadataFile);

                //check if this branch point concept is one of the metadata concepts
                for(generated.metadata.Concept metadataConcept : metadata.getConcepts().getConcept()){

                    if(branchPtConcept.equalsIgnoreCase(metadataConcept.getName())){

                        //keep track of the branch point concepts that have at least 1 metadata file associated with it
                        if(!conceptsCovered.contains(branchPtConcept.toLowerCase())){
                            conceptsCovered.add(branchPtConcept.toLowerCase());
                        }

                        match = true;
                        break;  //stop searching this metadata
                    }
                }//end for

                if(match){
                    break;
                }
            }//end for

            if(!match){
                //didn't find a single metadata file for this concept
                return false;
            }
        }

        return true;

    }

    /**
     * Check whether the metadata reference content is valid (e.g. does it exist, is it a supported file type).
     *
     * @param contentType the type of metadata reference
     * @param referenceFileName the content reference file name to check (e.g. PowerPoint show, trainingApp.xml)
     *             Note: the file name should be relative to the domain directory.
     * @param additionalValidation contains additional validation checks that need to be performed.  Can be null
     * @param courseConceptNames collection of unique course concept names used to find which DKF concept names
     * are course concepts.<br/>
     * When the dkf has:<br/>
     * 1. remediation option defined, the course concepts in the DKF are used to check for remediation metadata tagged
     * content. <br/>
     * 2. otherwise, the course concepts are used to make sure the DKF contains all of the concepts provided.  This
     * is useful when making sure a metadata referenced DKF actually assesses the concepts it should.<br/>
     * When the course concepts list is null or empty these checks are not performed.
     * @param courseDirectory the course directory that contains course related files
     * @param status used to indicate whether the Domain module has an Internet connection
     * @param failOnFirstSchemaError - if true than a validation event will cause the parsing of the XML content to fail and throw an exception.
     * If there are no validation events than the XML contents are XML and schema valid.
     * From Java API docs: A validation event indicates that a problem was encountered while validating the incoming XML data during an unmarshal operation, while
     * performing on-demand validation of the Java content tree, or while marshalling the Java content tree back to XML data.
     * @return validation results
     * @throws Exception if there was any type of validation error
     * @throws FileValidationException if there was a problem with the metadata file or a file it references
     */
    public static GIFTValidationResults checkMetadataContentReference(MetadataContentType contentType, String referenceFileName, AbstractAdditionalValidationSettings additionalValidation,
            AbstractFolderProxy courseDirectory, Collection<String> courseConceptNames, InternetConnectionStatusEnum status, boolean failOnFirstSchemaError) throws Exception, FileValidationException{

        if(contentType == null){
            return null;
        }

        GIFTValidationResults validationResults = null;

        switch(contentType){
        case TrainingAppXML:
            FileProxy taReferenceFile = courseDirectory.getRelativeFile(referenceFileName);
            UnmarshalledFile uFile = DomainCourseFileHandler.getTrainingAppReference(taReferenceFile, failOnFirstSchemaError);
            validationResults = DomainCourseFileHandler.validateTrainingAppReference((TrainingApplicationWrapper) uFile.getUnmarshalled(), courseDirectory, courseConceptNames, status);
            break;
        case LessonMaterialXML:
            FileProxy lmReferenceFile = courseDirectory.getRelativeFile(referenceFileName);
            LessonMaterialFileHandler handler = new LessonMaterialFileHandler(lmReferenceFile, courseDirectory, InternetConnectionStatusEnum.UNKNOWN);
            handler.parse(failOnFirstSchemaError);
            validationResults = handler.validateLessonMaterial(null, courseDirectory);
            break;
        case ConversationTreeXML:
            FileProxy convTreeReferenceFile = courseDirectory.getRelativeFile(referenceFileName);
            ConversationTreeFileHandler tree = new ConversationTreeFileHandler(convTreeReferenceFile, true);
            validationResults = tree.checkConversation();
            break;
        case QuestionExport:
            try{
                FileProxy questionExportReferenceFile = courseDirectory.getRelativeFile(referenceFileName);
                String fileContent = IOUtils.toString(questionExportReferenceFile.getInputStream());
                JSONObject obj = (JSONObject) JSONValue.parse(fileContent);
                QuestionJSON json = new QuestionJSON();
                json.decode(obj);
            }catch(Exception e){
                throw new FileValidationException("Failed to successfully check the question export referenced in a metadata file.",
                        "An exception was thrown while trying to parse the question export file.\n\nError message:"+e.getMessage(), referenceFileName, e);
            }
            break;
		default:
			break;
        }

        return validationResults;
    }

    /**
     * Return a display name for the metadata provided.
     *
     * @param metadata the metadata XML content to look for a display name in.
     * @return if the display name attribute is set it is returned, otherwise either the content reference or null is returned.
     */
    public static String getDisplayName(generated.metadata.Metadata metadata){

        String displayName = null;
        if(metadata.getDisplayName() != null){
            displayName = metadata.getDisplayName();
        }else if(metadata.getContent() instanceof generated.metadata.Metadata.Simple){
            displayName = ((generated.metadata.Metadata.Simple)metadata.getContent()).getValue();
        }else if(metadata.getContent() instanceof generated.metadata.Metadata.URL){
            displayName = ((generated.metadata.Metadata.URL)metadata.getContent()).getValue();
        }else if(metadata.getContent() instanceof generated.metadata.Metadata.TrainingApp){
            //for now just show this metadata file path,
            //in the future parse the training app xml file and get something from it (maybe the dkf name?)
            displayName = null;  //no generic rule for all training app ref file contents
        }else if(metadata.getContent() instanceof generated.metadata.Metadata.LessonMaterial){
            //for now just show this metadata file path,
            //in the future parse the training app xml file and get something from it (maybe the lesson material name?)
            displayName = null; //no generic rule for all lesson material file contents (mainly because it can be a list of media)
        }

        return displayName;
    }

    /**
     * Return true if the metadata has the remediation only element with a value of true.
     *
     * @param metadata the metadata object to check
     * @return false if the metadata is null, doesn't have the remediation only element or the element
     * value is false.
     */
    public static boolean isRemediationOnly(generated.metadata.Metadata metadata){

        boolean value = false;

        if(metadata != null){

            if(metadata.getPresentAt() != null && metadata.getPresentAt().getRemediationOnly() != null){
                value = metadata.getPresentAt().getRemediationOnly() == generated.metadata.BooleanEnum.TRUE;
            }
        }

        return value;
    }

}
