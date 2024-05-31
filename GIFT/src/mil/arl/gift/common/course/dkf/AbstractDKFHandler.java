/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.course.dkf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;

import javax.xml.bind.JAXBException;

import org.xml.sax.SAXException;

import generated.dkf.Scenario;
import mil.arl.gift.common.course.CourseFileValidationException;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.common.io.FileValidationException;
import mil.arl.gift.common.io.UnmarshalledFile;
import mil.arl.gift.common.io.Version;

/**
 * This is the base class for DKF handlers and provides basic dkf xml parsing
 * logic.
 *
 * @author mhoffman
 */
public abstract class AbstractDKFHandler extends AbstractSchemaHandler {

    /** the domain knowledge file to parse */
    protected FileProxy file;

    /** root dkf.xsd generated class */
    protected Scenario scenario;

    /** the action knowledge of the dkf */
    private DomainActionKnowledge domainActionKnowledge = null;
    
    /**
     * Default constructor - use default schema
     */
    public AbstractDKFHandler(){
        super(DKF_SCHEMA_FILE);
    }

    /**
     * Class constructor - parse, validate and instantiate dkf related objects
     * Note: this will use the default schema location specified by SCHEMA.
     *
     * @param dkfFile - the DKF file to handle 
     * @param failOnFirstSchemaError - if true than a validation event will cause the parsing of the XML content to fail and throw an exception.
     * If there are no validation events than the XML contents are XML and schema valid.
     * From Java API docs: A validation event indicates that a problem was encountered while validating the incoming XML data during an unmarshal operation, while 
     * performing on-demand validation of the Java content tree, or while marshalling the Java content tree back to XML data.
     * @throws FileValidationException - thrown if there was an error during the
     * handling of the dkf
     */
    public AbstractDKFHandler(FileProxy dkfFile, boolean failOnFirstSchemaError) throws FileValidationException {
        this(dkfFile, DKF_SCHEMA_FILE, failOnFirstSchemaError);

    }

    /**
     * Class constructor - parse, validate and instantiate dkf related objects
     *
     * @param dkfFile - the dkf to handle.
     * @param schemaFile - the dkf schema to validate against. Must be an existing file.
     * @param failOnFirstSchemaError - if true than a validation event will cause the parsing of the XML content to fail and throw an exception.
     * If there are no validation events than the XML contents are XML and schema valid.
     * From Java API docs: A validation event indicates that a problem was encountered while validating the incoming XML data during an unmarshal operation, while 
     * performing on-demand validation of the Java content tree, or while marshalling the Java content tree back to XML data.
     * @throws FileValidationException Thrown when there is an problem validating
     * the DKF against the schema
     */
    public AbstractDKFHandler(FileProxy dkfFile, File schemaFile, boolean failOnFirstSchemaError) throws FileValidationException {
        super(schemaFile);

        try {
            compareFileVersions(dkfFile, new FileProxy(schemaFile), Version.getInstance().getCurrentSchemaVersion());
            setDKF(dkfFile);

            UnmarshalledFile unmarshalledFile = super.parseAndValidate(Scenario.class, dkfFile.getInputStream(),
                    failOnFirstSchemaError);
            scenario = (Scenario) unmarshalledFile.getUnmarshalled();
        } catch (Throwable e) {
            throw new CourseFileValidationException(
                    "A problem occurred when parsing and validating the dkf file '"+dkfFile.getName()+"'.",
                    buildValidationExceptionDetails(e), 
                    dkfFile.getFileId(), 
                    e);
        }
    }

    /**
     * Builds the exception details from the failed validation.
     * 
     * @param t the throwable resulting from the failed validation.
     * @return the exception details.
     */
    private String buildValidationExceptionDetails(Throwable t) {
        StringBuilder detailsBuilder = new StringBuilder();
        detailsBuilder.append("\n<b>The following is helpful information for software developers.</b>");
        detailsBuilder.append("\n\n");
        detailsBuilder.append(
                "The most likely cause of this error is because the XML file content is not correctly formatted. This could be anything from a missing XML tag needed to ensure the general XML structure was followed (i.e. all start and end tags are found), to a missing required field (e.g. dkf name is required) or the value for a field doesn't satisfy the schema requirements (i.e. the dkf name must be at least 1 character).");
        detailsBuilder.append("\n\n");
        detailsBuilder.append(
                "Please take a look at the first part of stacktrace for a hint at the problem or ask for help on the GIFT <a href=\"https://gifttutoring.org/projects/gift/boards\" target=\"_blank\">forums</a>.");
        detailsBuilder.append("\n\n");
        detailsBuilder.append(
                "<b>For Example: </b><div style=\"padding: 20px; border: 1px solid gray; background-color: #DDDDDD\">This example stacktrace snippet that indicates the DKF name ('#AnonType_nameDKF') value doesn't satisfy the minimum length requirement of 1 character:\n\n<i>javax.xml.bind.UnmarshalException - with linked exception: [org.xml.sax.SAXParseException; lineNumber: 2; columnNumber: 69; cvc-minLength-valid: Value '' with length = '0' is not facet-valid with respect to minLength '1' for type '#AnonType_nameDKF'</i></div>");
        if (t.getMessage() != null) {
            detailsBuilder.append("\n\n");
            detailsBuilder.append("<b>Your Validation Error:</b> ");
            detailsBuilder.append(t.getMessage());
        }

        return t.toString();
    }
    
    /**
     * The top level DKF generated class.
     * 
     * @return Scenario - root dkf.xsd generated class
     */
    public Scenario getScenario(){
        return scenario;
    }

    /**
     * Set the dkf file that will be parsed and validated.
     *
     * @param dkfFile
     */
    private void setDKF(FileProxy dkfFile) {

        if (dkfFile == null) {
            throw new IllegalArgumentException("The dkf file can't be null");
        }

        file = dkfFile;
    }

    /**
     * Validate the dkf contents the dkf xsd Note: this will use the default
     * schema location specified by SCHEMA.
     *
     * @param scenario - the dkf contents
     * @throws SAXException If a SAX error occurs during parsing.
     * @throws JAXBException Can occur when marshalling
     */
    public void validateAgainstSchema(Scenario scenario) throws SAXException, JAXBException {
        validateAgainstSchema(scenario, Scenario.class, getSchemaObjectFactory(FileType.DKF));
    }

    /**
     * Write the dkf contents to the file name specified. Note: this will use
     * the default schema location specified by SCHEMA.
     *
     * @param scenario - the dkf contents
     * @param dkfFilename - the file name to write the dkf contents too
     * @throws SAXException If a SAX error occurs during parsing.
     * @throws JAXBException Can occur when marshalling
     * @throws FileNotFoundException if the file exists but is a directory rather than a regular file, does not exist but cannot be created, or cannot be opened for any other reason 
     * @throws IOException if there was a problem closing the output stream to the dkf file
     */
    public void writeDKFToFile(Scenario scenario, String dkfFilename) 
            throws SAXException, JAXBException, FileNotFoundException, IOException {
        writeToFile(scenario, new File(dkfFilename), false);
    }

    /**
     * Return the domain action knowledge for this dkf
     *
     * @return DomainActionKnowledge
     */
    public DomainActionKnowledge getDomainActionKnowledge() {

        if (domainActionKnowledge == null) {
            generated.dkf.PlacesOfInterest placesOfInterest = null;
            if(scenario.getAssessment() != null && scenario.getAssessment().getObjects() != null){
                placesOfInterest = scenario.getAssessment().getObjects().getPlacesOfInterest();
            }
            domainActionKnowledge = new DomainActionKnowledge(scenario.getActions(), placesOfInterest);
        }

        return domainActionKnowledge;
    }
    
    /**
     * Return a string representation of the DKF actions content in XML format.
     * 
     * @return String - DKF actions element as a string
     * @throws SAXException If a SAX error occurs during parsing.
     * @throws JAXBException Can occur when marshalling
     */
    public String getRawDKFActionKnowledge() throws SAXException, JAXBException{
        return getAsXMLString(scenario.getActions(), generated.dkf.Actions.class, getSchemaFile());        
    }
    
    /**
     * Return a string representation of the DKF actions content in XML format.
     * 
     * @param actions - DKF actions element
     * @return String -  DKF actions element as a string
     * @throws SAXException If a SAX error occurs during parsing.
     * @throws JAXBException Can occur when marshalling
     */
    public static String getRawDKFActionKnowledge(generated.dkf.Actions actions) throws SAXException, JAXBException{
        return getAsXMLString(actions, generated.dkf.Actions.class, DKF_SCHEMA_FILE);        
    }

    /**
     * Return an Actions object from the raw XML actions element string provided.
     * 
     * @param rawActions - DKF actions element as a string
     * @return Actions - new actions object
     * @throws SAXException If a SAX error occurs during parsing.
     * @throws JAXBException Can occur when marshalling
     */
    public generated.dkf.Actions getActions(String rawActions) throws JAXBException, SAXException{        
        UnmarshalledFile unmarshalledFile = parseAndValidate(generated.dkf.Actions.class, rawActions, true);
        return (generated.dkf.Actions)unmarshalledFile.getUnmarshalled();
    }
    
    /**
     * This class specifies additional DKF validation settings that can be requested upon
     * the core DKF validation logic.  For example, needing to check that the DKF contains
     * performance assessment nodes that assess course level concepts.
     * 
     * @author mhoffman
     *
     */
    public static class AdditionalDKFValidationSettings extends AbstractAdditionalValidationSettings{
        
        /** 
         * collection of node names to match in the DKF being validated
         * If the DKF doesn't contain these nodes, at a minimum, the DKF
         * is considered invalid.
         */
        private Collection<String> nodeNamesToMatch;
        
        /**
         * whether each of the matched node names referenced in 'node names to match' collection
         * also need to have at least 1 scoring rule.  If the DKF performance node doesn't contain
         * a scoring rule (at some descendant level in its hierarchy), the DKF is considered invalid.
         */
        private boolean matchedNamesNeedScoring;
        
        /**
         * Class constructor - set the reason.
         * 
         * @param reason a description of why this additional validation is needed.  Can't be null.
         */
        public AdditionalDKFValidationSettings(String reason){
            super(reason);
        }
        
        /**
         * Set the collection of performance assessment node names to match to the DKF
         * being validated.  If the DKF doesn't contain these nodes, at a minimum, the DKF
         * is considered invalid.
         * 
         * @param nodeNamesToMatch collection of node names to match in the DKF being validated.  Can be
         * null or empty.
         */
        public void setNodeNamesToMatch(Collection<String> nodeNamesToMatch){
            this.nodeNamesToMatch = nodeNamesToMatch;
        }
        
        public Collection<String> getNodeNamesToMatch(){
            return nodeNamesToMatch;
        }
        
        /**
         * Set whether each of the matched node names referenced in 'node names to match' collection
         * also need to have at least 1 scoring rule.  If the DKF performance node doesn't contain
         * a scoring rule (at some descendant level in its hierarchy), the DKF is considered invalid.
         * 
         * @param value flag used to indicate whether matched nodes must have at least 1 scoring rule
         */
        public void setMatchedNamesNeedScoring(boolean value){
            this.matchedNamesNeedScoring = value;
        }
        
        public boolean shouldMatchedNamesNeedScoring(){
            return matchedNamesNeedScoring;
        }
        
        @Override
        public String toString(){
            
            StringBuffer sb = new StringBuffer();
            sb.append("[AdditionalDKFValidationSettings: ");
            sb.append(super.toString());
            sb.append(", nodeNamesToMatch = ").append(getNodeNamesToMatch());
            sb.append(", needScoring = ").append(shouldMatchedNamesNeedScoring());
            sb.append("]");
            return sb.toString();
        }
    }
}
