/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.io;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import mil.arl.gift.common.PackageUtil;
import mil.arl.gift.common.util.StringUtils;

/**
 * This is the base class for xml parsing handlers that use a schema to validate against.
 * 
 * @author mhoffman
 *
 */
public abstract class AbstractSchemaHandler {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(AbstractSchemaHandler.class);
    
    /** 
     * creates schema objects used in the marshall/unmarshall process 
     * Note: uses of this factory are not thread safe (i.e. can't parse while already parsing), therefore uses
     * should be synchronized.  Otherwise you might see this error: "org.xml.sax.SAXException: FWK005 parse may not be called while parsing"
     */
    private static final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    
    /**
     * JAXBContext cache map.  Objects are cached to reduce the costly creation operation and thread contention found during profiling.
     * Note that JAXBContext is thread safe, but the Marshaller and Unmarshaller objects created by it are not.
     */
    private static final ConcurrentHashMap<Class<?>, JAXBContext> contextCache = new ConcurrentHashMap<Class<?>, JAXBContext>();
    
    /**
     * Enumerated GIFT File types
     */
    public enum FileType{
        COURSE,
        DKF,
        LEARNER_CONFIGURATION,
        SENSOR_CONFIGURATION,
        EMAP_PEDAGOGICAL_CONFIGURATION,
        METADATA,
        TRAINING_APP_REFERENCE,
        INTEROP_CONFIGURATION,
        LESSON_MATERIAL_REF,
        CONVERSATION,
        PARADATA,
        QUESTION_EXPORT,
        SURVEY_EXPORT,
        XTSP_JSON,
        ICAPPOLICY,
        VIDEO_METADATA,
        LMS_CONNECTIONS
    } 
    
    /**
     *  Default schema information
     */
    
    public static final File COMMON_SCHEMA_FILE = new File(PackageUtil.getConfiguration()+File.separator+"domain"+File.separator+"common.xsd");
    
    /** course schema info */
    public static final String COURSE_SCHEMA_PATH = PackageUtil.getConfiguration()+File.separator+"domain"+File.separator+"course"+File.separator+"course.xsd";
    public static final File COURSE_SCHEMA_FILE = new File(COURSE_SCHEMA_PATH);
    public static final Class<?> COURSE_ROOT = generated.course.Course.class;
    public static final String COURSE_FILE_EXTENSION = ".course.xml";
    
    /** Training Application element info 
     * Note: part of the course schema
     */
    public static final Class<?> TRAINING_APP_ELEMENT_ROOT = generated.course.TrainingApplicationWrapper.class;
    public static final File TRAINING_APP_ELEMENT_SCHEMA_FILE = COURSE_SCHEMA_FILE;
    public static final String TRAINING_APP_FILE_EXTENSION = ".trainingapp.xml";
    
    /** conversation tree info */
    public static final Class<?> CONVERSATION_TREE_ELEMENT_ROOT = generated.conversation.Conversation.class;
    public static final File CONVERSATION_TREE_SCHEMA_FILE = new File(PackageUtil.getConfiguration() + File.separator+"domain"+File.separator+"conversationTree"+File.separator+"conversationTree.xsd");
    public static final String CONVERSATION_TREE_FILE_EXTENSION = ".conversationTree.xml";
    
    /** dkf schema info */
    public static final File DKF_SCHEMA_FILE = new File(PackageUtil.getConfiguration()+File.separator+"domain"+File.separator+"dkf"+File.separator+"dkf.xsd");
    public static final Class<?> DKF_ROOT = generated.dkf.Scenario.class;
    public static final String DKF_FILE_EXTENSION = ".dkf.xml";
    
    /** learner configuration schema info */
    public static final File LEARNER_SCHEMA_FILE = new File(PackageUtil.getConfiguration() + File.separator+"learner"+File.separator+"learnerConfig.xsd");
    public static final Class<?> LEARNER_ROOT = generated.learner.LearnerConfiguration.class;
    public static final String LEARNER_CONFIG_FILE_EXTENSION = ".learnerconfig.xml";
    
    /** learner actions schema info */
    public static final File LEARNER_ACTIONS_FILE = new File(PackageUtil.getConfiguration() + File.separator+"domain"+File.separator+"learnerAction"+File.separator+"learnerActions.xsd");
    public static final String LEARNER_ACTIONS_FILE_EXTENSION = ".learnerActions.xml";
    
    /** metadata schema info */
    public static final File METADATA_SCHEMA_FILE = new File(PackageUtil.getConfiguration() + File.separator + "domain" + File.separator + "metadata" + File.separator + "metadata.xsd");
    public static final Class<?> METADATA_ROOT = generated.metadata.Metadata.class;
    public static final String METADATA_FILE_EXTENSION = ".metadata.xml";
    
    /** video metadata schema info */
    public static final String VIDEO_SCHEMA_PATH = PackageUtil.getConfiguration()+File.separator+"tools"+File.separator+"video"+File.separator+"imsmd_strict_v1p3p2.xsd";
    public static final File VIDEO_SCHEMA_FILE = new File(VIDEO_SCHEMA_PATH);
    public static final Class<?> VIDEO_ROOT = generated.video.LOMType.class;
    public static final String VIDEO_FILE_EXTENSION = ".vmeta.xml";

    /** EMAP pedagogical configuration schema info */
    public static final File EMAP_PEDAGOGICAL_SCHEMA_FILE = new File(PackageUtil.getConfiguration() + File.separator + "ped" + File.separator + "eMAP.xsd");
    public static final Class<?> EMAP_PEDAGOGICAL_ROOT = generated.ped.EMAP.class;
    public static final String EMAP_PEDAGOGICAL_CONFIG_FILE_EXTENSION = ".pedagogicalconfig.xml";
    
    /** ICAP policy schema info */
    public static final File ICAP_POLICY_SCHEMA_FILE = new File(PackageUtil.getConfiguration() + File.separator + "ped" + File.separator + "ICAPPolicy.xsd");
    public static final Class<?> ICAP_POLICY_ROOT = generated.ped.ICAPPolicy.class;
    public static final String ICAP_POLICY_FILE_EXTENSION = ".icap.policy.xml";
    
    /** sensor configuration schema info */
    public static final File SENSOR_SCHEMA_FILE = new File(PackageUtil.getConfiguration() + File.separator+"sensor"+File.separator+"sensorConfig.xsd");
    public static final Class<?> SENSOR_ROOT = generated.sensor.SensorsConfiguration.class;
    public static final String SENSOR_CONFIG_FILE_EXTENSION = ".sensorconfig.xml";
    
    /** interop configuration schema info */
    public static final File INTEROP_SCHEMA_FILE = new File(PackageUtil.getConfiguration() + File.separator+"gateway"+File.separator+"interopConfig.xsd");
    public static final Class<?> INTEROP_ROOT = generated.gateway.InteropConfig.class;
    public static final String INTEROP_FILE_EXTENSION = ".interopConfig.xml";
    
    /** LMS connections schema info */
    public static final File LMS_CONNECTIONS_SCHEMA_FILE = new File(PackageUtil.getConfiguration() + File.separator+"lms"+File.separator+"LMSConnections.xsd");
    public static final Class<?> LMS_CONNECTIONS_ROOT = generated.lms.LMSConnections.class;
    
    /** lesson material schema info */
    public static final File LESSON_MATERIAL_SCHEMA_FILE = new File(PackageUtil.getConfiguration() + File.separator + "domain" + File.separator + "lessonMaterial" + File.separator + "lessonMaterial.xsd");
    public static final Class<?> LESSON_MATERIAL_ROOT = generated.course.LessonMaterialList.class;
    public static final String LESSON_MATERIAL_FILE_EXTENSION = ".lessonMaterial.xml";
    
    /** AutoTutorSKO info */
    public static final Class<?> AUTOTUTOR_SKO_ROOT = generated.course.AutoTutorSKO.class;
    public static final String AUTOTUTOR_SKO_EXTENSION = ".sko.xml";
    
    /** 
     * the schema file used to validate content 
     * For Java Web Start (JWS) this can be null because the schema file could be a jar instance
     */
    private File schemaFile;
    
    /**
     * used in Java Web Start (JWS) mode when the schema file is in a jar and is loaded by a classloader
     * resource.
     */
    private String schemaFileName;
    
    /**
     * Class constructor - set attributes
     * 
     * @param schemaFileName - the name of the schema file handled by this class.
     */
    public AbstractSchemaHandler(String schemaFileName){
        
        if(schemaFileName == null){
            throw new IllegalArgumentException("The schema file name can't be null");
        }
        
        this.schemaFileName = schemaFileName;
        
        InputStream iStream = FileFinderUtil.getFileByClassLoader(schemaFileName);
        if(iStream == null){    
            setSchemaFile(new File(schemaFileName)); 
        }
    }
    
    /**
     * Class constructor - set attributes
     * 
     * @param schemaFile - the schema file handled by this class. Must be an existing file.
     */
    public AbstractSchemaHandler(File schemaFile){
        
        try{
            setSchemaFile(schemaFile);
        }catch(IllegalArgumentException exception){
            if(logger.isDebugEnabled()){
                logger.debug("Failed to find '"+schemaFile+"' trying to find it on classpath: '"+schemaFile.getName()+"', '"+schemaFile.getAbsolutePath()+"', '"+schemaFile.getPath()+"', '"+schemaFile.getAbsoluteFile()+"'");
            }
            InputStream iStream = FileFinderUtil.getFileByClassLoader(schemaFile.getPath());
            if(iStream == null){    
                throw exception;
            }else{
                if(logger.isInfoEnabled()){
                    logger.info("Found '"+schemaFile+"' on classpath.");
                }
                this.schemaFileName = schemaFile.getPath();
            }
        }
    }
    
    private void setSchemaFile(File schemaFile){
        
        if(schemaFile == null || !schemaFile.exists()){
            throw new IllegalArgumentException("The schema file named "+schemaFile+" doesn't exist");
        }
        
        this.schemaFile = schemaFile;
    }
    
    /**
     * Return the schema file.
     * 
     * @return File. Can be null if the schema file is a resource and not a file on disk
     */
    public File getSchemaFile(){
        return schemaFile;
    }
    
    /**
     * Parse the xml into an instance of a generated class.
     * Note: this currently doesn't work with XML files that are schema valid against older schemas.  Use get
     * getUnmarshalledFile method for handling XML files that are OR could be from an older schema.
     * 
     * @param clazz - the class representing the root node of the xml tree being unmarshalled
     * @param file - the xml file wanting to be unmarshalled
     * @param failOnFirstSchemaError - if true than a validation event will cause the parsing of the XML content to fail and throw an exception.
     * If there are no validation events than the XML contents are XML and schema valid.
     * From Java API docs: A validation event indicates that a problem was encountered while validating the incoming XML data during an unmarshal operation, while 
     * performing on-demand validation of the Java content tree, or while marshalling the Java content tree back to XML data.
     * @return the unmarshalled object
     * @throws JAXBException Can occur when unmarshalling
     * @throws FileNotFoundException if the file doesn't exist
     * @throws SAXException If a SAX error occurs during parsing. 
     */
    public UnmarshalledFile parseAndValidate(Class<?> clazz, File file, boolean failOnFirstSchemaError) throws JAXBException, FileNotFoundException, SAXException{        
        
        if(logger.isInfoEnabled()){
            logger.info("Unmarshalling xml file "+file.getAbsolutePath()+" with root generated class of "+clazz);     
        }

        return parseAndValidate(clazz, new FileInputStream(file), failOnFirstSchemaError);
    }
    
    /**
     * Parse the xml string into an instance of a generated class.
     * Note: this currently doesn't work with XML files that are schema valid against older schemas.  Use get
     * getUnmarshalledFile method for handling XML files that are OR could be from an older schema.
     * 
     * @param clazz - the class representing the root node of the xml tree being unmarshalled
     * @param xmlContent - the xml content wanting to be unmarshalled
     * @param failOnFirstSchemaError - if true than a validation event will cause the parsing of the XML content to fail and throw an exception.
     * If there are no validation events than the XML contents are XML and schema valid.
     * From Java API docs: A validation event indicates that a problem was encountered while validating the incoming XML data during an unmarshal operation, while 
     * performing on-demand validation of the Java content tree, or while marshalling the Java content tree back to XML data.
     * @return the unmarshalled object
     * @throws SAXException If a SAX error occurs during parsing.
     * @throws JAXBException Can occur when unmarshalling
     */
    protected UnmarshalledFile parseAndValidate(Class<?> clazz, String xmlContent, boolean failOnFirstSchemaError) throws JAXBException, SAXException{        
        
        if(logger.isInfoEnabled()){
            logger.info("Unmarshalling xml content with root generated class of "+clazz);
        }
        
        return parseAndValidate(clazz, new ByteArrayInputStream(xmlContent.getBytes()), failOnFirstSchemaError);
    }
    
    /**
     * Parse the input stream into an instance of a generated class.
     * Note: this currently doesn't work with XML files that are schema valid against older schemas.  Use get
     * getUnmarshalledFile method for handling XML files that are OR could be from an older schema.
     * 
     * @param clazz - the class representing the root node of the xml tree being unmarshalled
     * @param inputStream - the xml contents wanting to be unmarshalled
     * @param failOnFirstSchemaError - if true than a validation event will cause the parsing of the XML content to fail and throw an exception.
     * If there are no validation events than the XML contents are XML and schema valid.</br>
     * Note: If the clazz is {@link AbstractSchemaHandler.COURSE_ROOT}, this is always false (i.e. continue parsing because
     * the validation error might be due to a disabled course object, which we can ignore.</br> 
     * From Java API docs: A validation event indicates that a problem was encountered while validating the incoming XML data during an unmarshal operation, while 
     * performing on-demand validation of the Java content tree, or while marshalling the Java content tree back to XML data.
     * @return the unmarshalled object
     * @throws SAXException If a SAX error occurs during parsing the schema
     * @throws JAXBException If any unexpected errors occur while unmarshalling either from the content not being a valid XML structure or the content violates the schema.
     * e.g. missing a required element or the string value's length is not long enough
     */
    public UnmarshalledFile parseAndValidate(Class<?> clazz, InputStream inputStream, boolean failOnFirstSchemaError) throws JAXBException, SAXException{
        
        try{
            if(schemaFile == null){
                if(logger.isInfoEnabled()){
                    logger.info("The schema file is null, trying to find the schema file named '"+schemaFileName+"' using the class loader.");
                }
                InputStream schemaFileInputStream = FileFinderUtil.getFileByClassLoader(schemaFileName);
                FileType fileType = getFileType();
                Class<?> objectFactory = getSchemaObjectFactory(fileType);
                return parseAndValidate(clazz, inputStream, new StreamSource[]{new StreamSource(schemaFileInputStream)}, failOnFirstSchemaError, objectFactory);
            }else{
                return parseAndValidate(clazz, inputStream, schemaFile, failOnFirstSchemaError);
            }
        }catch(FileNotFoundException fnfe){
            //this is not possible because the class schema File is checked for existence when set in this class
            throw new RuntimeException("Caught a file not found exception which shouldn't be possible.", fnfe);
        }
    }
    
    /**
     * Parse the input stream into an instance of a generated class.
     * Note: this currently doesn't work with XML files that are schema valid against older schemas.  Use get
     * getUnmarshalledFile method for handling XML files that are OR could be from an older schema.
     * 
     * @param inputStream - the xml contents wanting to be unmarshalled
     * @param fileType - the GIFT file type being unmarshalled.  This is used to lookup the appropriate schema and root JAXB object.
     * Can't be null.
     * @param failOnFirstSchemaError - if true than a validation event will cause the parsing of the XML content to fail and throw an exception.
     * If there are no validation events than the XML contents are XML and schema valid.
     * From Java API docs: A validation event indicates that a problem was encountered while validating the incoming XML data during an unmarshal operation, while 
     * performing on-demand validation of the Java content tree, or while marshalling the Java content tree back to XML data.
     * @return the unmarshalled object
     * @throws SAXException If a SAX error occurs during parsing.
     * @throws JAXBException Can occur when unmarshalling
     * @throws FileNotFoundException if the schema file was not found
     */
    public static UnmarshalledFile parseAndValidate(InputStream inputStream, FileType fileType, 
            boolean failOnFirstSchemaError) throws FileNotFoundException, JAXBException, SAXException{
        
        File schemaFile = AbstractSchemaHandler.getSchemaFile(fileType);
        Class<?> clazz = AbstractSchemaHandler.getRootClass(fileType);
        return parseAndValidate(clazz, inputStream, schemaFile, failOnFirstSchemaError);
    }
    
    /**
     * Parse the input stream into an instance of a generated class.
     * Note: this currently doesn't work with XML files that are schema valid against older schemas.  Use get
     * getUnmarshalledFile method for handling XML files that are OR could be from an older schema.
     * 
     * @param clazz - the class representing the root node of the xml tree being unmarshalled.   Can't be null.
     * @param inputStream - the xml contents wanting to be unmarshalled
     * @param schemaFile - the schema associated with the class and defines the structure of the contents in the input stream.
     * If null, no schema will be used during parsing to validate the XML contents.
     * @param failOnFirstSchemaError - if true than a validation event will cause the parsing of the XML content to fail and throw an exception.
     * If there are no validation events than the XML contents are XML and schema valid.</br>
     * Note: If the clazz is {@link AbstractSchemaHandler.COURSE_ROOT}, this is always false (i.e. continue parsing because
     * the validation error might be due to a disabled course object, which we can ignore.</br> 
     * From Java API docs: A validation event indicates that a problem was encountered while validating the incoming XML data during an unmarshal operation, while 
     * performing on-demand validation of the Java content tree, or while marshalling the Java content tree back to XML data.
     * @return the unmarshalled object
     * @throws SAXException If a SAX error occurs during parsing the schema
     * @throws JAXBException If any unexpected errors occur while unmarshalling either from the content not being a valid XML structure or the content violates the schema.
     * e.g. missing a required element or the string value's length is not long enough
     * @throws FileNotFoundException if the schema file was not found
     */
    public static UnmarshalledFile parseAndValidate(Class<?> clazz, InputStream inputStream, File schemaFile, 
            boolean failOnFirstSchemaError) throws JAXBException, SAXException, FileNotFoundException{
        
        if(clazz == null){
            throw new IllegalArgumentException("Unable to parse the GIFT XML file because the provided generated class to parse against is null.  This can happen if the GIFT file type is new or hasn't been included in the GIFT conversion wizard logic yet.");
        }
        
        FileType fileType = getFileType(schemaFile);
        Class<?> objectFactory = getSchemaObjectFactory(fileType);
        
        if(schemaFile == null){
            return parseAndValidate(clazz, inputStream, new StreamSource[]{}, failOnFirstSchemaError, objectFactory);
        }else if(schemaFile.exists()){
            return parseAndValidate(clazz, inputStream, new StreamSource[]{new StreamSource(schemaFile)}, failOnFirstSchemaError, objectFactory);
        }else{
            InputStream schemaInputStream = FileFinderUtil.getFileByClassLoader(schemaFile.getPath());
            return parseAndValidate(clazz, inputStream, new StreamSource[]{new StreamSource(schemaInputStream)}, failOnFirstSchemaError, objectFactory);
        }
    }
    
    /**
     * Returns the JAXBContext for the given class.  If the class is found in the cache then the previously
     * created JAXBContext is returned, otherwise a new JAXBContext is created.
     * 
     * @param clazz a generated class to search for an existing JAXBContext for or create a new one.
     * @param objectFactory the class loader for our generated classes (e.g. generated.course.ObjectFactory.class), used to
     * locate the implementation classes for unmashalling the jaxb object.  A non-null value is used to prevent using
     * the thread's context class loader.  Refer to #5138 for an issue this solved.  If null, logic falls back to previous
     * logic that was here for a while which is to use the JAXBContext.newInstance(Class).
     * @return the JAXBContext for that class.  Can't be null.
     * @throws JAXBException if there was a problem creating a new JAXBContext for the class provided.
     * @throws IllegalArgumentException if the class provided is null
     */
    private static JAXBContext getJAXBContext(Class<?> clazz, Class<?> objectFactory) throws JAXBException, IllegalArgumentException {
        
        if(clazz == null){
            throw new IllegalArgumentException("Unable to find the JAXB context for a null class");
        }
        
        JAXBContext jc = contextCache.get(clazz);
        if (jc == null) {
            
            if(objectFactory != null) {
                if(logger.isDebugEnabled()) {
                    logger.debug("using object factory of "+objectFactory);
                }
                jc = JAXBContext.newInstance(clazz.getPackageName(), objectFactory.getClassLoader());
            }else {
                jc = JAXBContext.newInstance(clazz);
            }
            // There is a chance that another thread has created an instance and put it in the map
            // already, so the one we created will be discarded after use, but this is not a problem.
            contextCache.putIfAbsent(clazz, jc);
        }
        return jc;
    }
    /**
     * Parse the input stream into an instance of a generated class.
     * Note: this currently doesn't work with XML files that are schema valid against older schemas.  Use get
     * getUnmarshalledFile method for handling XML files that are OR could be from an older schema.
     * 
     * Note: this is a private method only to limit what is available public.  If absolutely necessary for some reason
     * you can make this a public method.
     * 
     * @param clazz - the class representing the root node of the xml tree being unmarshalled.  Can't be null.
     * @param inputStream - the xml contents wanting to be unmarshalled
     * @param schemaStreams - the schemas associated with the class and defines the structure of the contents in the input stream.
     * If null or empty, no schema will be used to validate the XML contents being parsed.
     * @param failOnFirstSchemaError - if true than a validation event will cause the parsing of the XML content to fail and throw an exception.
     * If there are no validation events than the XML contents are XML and schema valid.</br>
     * Note: If the clazz is {@link AbstractSchemaHandler.COURSE_ROOT}, this is always false (i.e. continue parsing because
     * the validation error might be due to a disabled course object, which we can ignore.</br> 
     * From Java API docs: A validation event indicates that a problem was encountered while validating the incoming XML data during an unmarshal operation, while 
     * performing on-demand validation of the Java content tree, or while marshalling the Java content tree back to XML data.
     * @param objectFactory the class loader for our generated classes (e.g. generated.course.ObjectFactory.class), used to
     * locate the implementation classes for unmashalling the jaxb object.  A non-null value is used to prevent using
     * the thread's context class loader.  Refer to #5138 for an issue this solved.  If null, logic falls back to previous
     * logic that was here for a while which is to use the JAXBContext.newInstance(Class).
     * @return the unmarshalled object
     * @throws SAXException If a SAX error occurs during parsing the schema
     * @throws JAXBException If any unexpected errors occur while unmarshalling either from the content not being a valid XML structure or the content violates the schema.
     * e.g. missing a required element or the string value's length is not long enough
     */
    public static UnmarshalledFile parseAndValidate(final Class<?> clazz, InputStream inputStream, StreamSource[] schemaStreams, 
            final boolean failOnFirstSchemaError, Class<?> objectFactory) throws JAXBException, SAXException {
        
        // input stream can't be null
        if(inputStream == null){
            throw new NullPointerException("The input stream can't be null.");
        }
        
        //handles classes generated in "generated" package
        JAXBContext jc = AbstractSchemaHandler.getJAXBContext(clazz, null);
                  
        // unmarshal (xml to java)
        Unmarshaller unmarshaller = jc.createUnmarshaller();

        if(schemaStreams != null && schemaStreams.length != 0){
            synchronized(schemaFactory) {
                Schema schema = schemaFactory.newSchema(schemaStreams);
                unmarshaller.setSchema(schema);
            }
        }
        
        // Create list for validation event errors.
        List<ValidationEvent> validationEvents = new ArrayList<>();

        // If the class is a course, always continue because the validation error might be due to a
        // disabled course object, which we can ignore. If the class is not a course object, obey
        // failOnFirstSchemaError boolean.
        boolean failOnFirst = AbstractSchemaHandler.COURSE_ROOT == clazz ? false : failOnFirstSchemaError;
        UnmarshallerValidator validator = new UnmarshallerValidator(validationEvents, failOnFirst);
        unmarshaller.setListener(validator);
        unmarshaller.setEventHandler(validator);
        
        // perform unmarshalling        
        Object unmarshalledObj = unmarshaller.unmarshal( new StreamSource(inputStream), clazz ).getValue();        
        UnmarshalledFile unmarshalledFile = new UnmarshalledFile((Serializable) unmarshalledObj);
        unmarshalledFile.setValidationEvents(validationEvents);
                
        return unmarshalledFile;
    }
    
    /**
     * Validate the contents against an xsd
     * Note: this currently doesn't work with XML files that are schema valid against older schemas.  Use get
     * getUnmarshalledFile method for handling XML files that are OR could be from an older schema.
     * 
     * @param object - the generated class content to validate against a schema
     * @param clazz - the generated class that is the root of the content.   Can't be null.
     * @param objectFactory the class loader for our generated classes (e.g. generated.course.ObjectFactory.class), used to
     * locate the implementation classes for mashalling the jaxb object.  A non-null value is used to prevent using
     * the thread's context class loader.  Refer to #5138 for an issue this solved.  If null, logic falls back to previous
     * logic that was here for a while which is to use the JAXBContext.newInstance(Class).
     * @throws SAXException If a SAX error occurs during parsing.
     * @throws JAXBException Can occur when marshalling
     */
    public void validateAgainstSchema(Object object, Class<?> clazz, Class<?> objectFactory) throws SAXException, JAXBException{
        
        if(object == null){
            throw new IllegalArgumentException("The object to validate can't be null");
        }
        
        if(logger.isInfoEnabled()){
            logger.info("validating contents of type "+clazz+" against schema...");
        }
        
        Schema schema;
        synchronized(schemaFactory) {
            schema = schemaFactory.newSchema(new StreamSource(schemaFile)); 
        }

        JAXBContext jc = AbstractSchemaHandler.getJAXBContext(clazz, objectFactory);
        Marshaller marshaller = jc.createMarshaller();
        marshaller.setSchema(schema);
        marshaller.setEventHandler(new ValidationEventHandler() {
            
            @Override
            public boolean handleEvent(ValidationEvent event) {
                //for now any parsing events should stop the marshall process
                return false;
            }
        });

        //DefaultHandler will discard all the events, and the marshal() operation will throw a JAXBException if validation against the schema fails
        marshaller.marshal(object, new DefaultHandler());
        
        if(logger.isInfoEnabled()){
            logger.info("Contents have passed xsd validation");
        }
    }
    
    /**
     * Return a new document object for the generated class object provided.
     * 
     * @param object - the generated class object to create a document object for
     * @param clazz - the generated class root
     * @param schemaFile - the schema that describes the document
     * @param continueParsingOnValidationError - whether or not to continue the marshalling process if a validation error occurs.
     * @return Document the document for the object
     * @throws ParserConfigurationException if a DocumentBuilder cannot be created which satisfies the configuration requested.
     * @throws SAXException If a SAX error occurs during parsing.
     * @throws JAXBException Can occur when marshalling
     */
    public static Document getDocument(Object object, Class<?> clazz, File schemaFile, final boolean continueParsingOnValidationError) throws ParserConfigurationException, SAXException, JAXBException{
        
        if(logger.isInfoEnabled()){
            logger.info("Creating dom document object for "+object+" with schema file of "+schemaFile);
        }
        
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document doc = dbf.newDocumentBuilder().newDocument();

        Marshaller marshaller = getMarshaller(clazz, schemaFile); 
        marshaller.setEventHandler(new ValidationEventHandler() {

            @Override
            public boolean handleEvent(ValidationEvent event) {
                // Either continue or stop the marshalling process when a validation error occurs.
                return continueParsingOnValidationError;
            }
        });
        marshaller.marshal( object, doc );
        
        if(logger.isInfoEnabled()){
            logger.info("Successfully created the document object.");
        }
        
        return doc;
    }
    
    /**
     * Write the contents to the file provided.
     * 
     * @param object - the generated class contents to write to the file
     * @param outputStream - the stream to write the contents too.  Can't be null.
     * @param fileType - the type of GIFT xml file being written
     * @param continueWritingOnValidationError whether or not to continue the marshalling process if a validation error occurs.
     * @return contains a list of validation issues. Can be empty but not null.
     * @throws SAXException If a SAX error occurs during parsing.
     * @throws JAXBException Can occur when marshalling
     * @throws IOException if there was a problem closing the output stream on the file being written to
     */
    public static List<ValidationEvent> writeToFile(Serializable object, OutputStream outputStream, 
            FileType fileType, final boolean continueWritingOnValidationError) 
            throws SAXException, JAXBException, IOException{
        
        File schemaFile = AbstractSchemaHandler.getSchemaFile(fileType);
        Class<?> clazz = AbstractSchemaHandler.getRootClass(fileType);
        return writeToFile(object, clazz, outputStream, schemaFile, continueWritingOnValidationError);
    }
    
    /**
     * Write the contents to the file name specified.
     * 
     * @param object - the generated class contents to write to the file
     * @param outputFile - the file to write the contents too
     * @param continueWritingOnValidationError whether or not to continue the marshalling process if a validation error occurs.
     * @return contains a list of validation issues. Can be empty but not null.
     * @throws SAXException If a SAX error occurs during parsing.
     * @throws JAXBException Can occur when marshalling
     * @throws FileNotFoundException if the file exists but is a directory rather than a regular file, does not exist but cannot be created, or cannot be opened for any other reason
     * @throws IOException if there was a problem closing the output stream on the file being written to
     */
    public static List<ValidationEvent> writeToFile(Serializable object, File outputFile, final boolean continueWritingOnValidationError) 
            throws SAXException, JAXBException, FileNotFoundException, IOException{
        
        if(outputFile == null){
            throw new IllegalArgumentException("The output file name can't be null");
        }
        
        FileType fileType = getFileType(outputFile.getName());
        Class<?> clazz = getRootClass(fileType);
        File schemaFile = getSchemaFile(fileType);
        
        return writeToFile(object, clazz, new FileOutputStream(outputFile), schemaFile, continueWritingOnValidationError);
    }
    
    /**
     * Write the contents to the file name specified.
     * 
     * @param object - the generated class contents to write to the file
     * @param clazz - the generated class that is the root of the content
     * @param outputStream - the stream to write the contents too
     * @param schemaFile the schema file for the object
     * @param continueWritingOnValidationError whether or not to continue the marshalling process if a validation error occurs.
     * @return contains a list of validation issues. Can be empty but not null.
     * @throws SAXException If a SAX error occurs during parsing.
     * @throws JAXBException Can occur when marshalling
     * @throws IOException if there was a problem closing the output stream on the file being written to
     */
    public static List<ValidationEvent> writeToFile(Serializable object, Class<?> clazz, OutputStream outputStream, 
            File schemaFile, final boolean continueWritingOnValidationError) 
            throws SAXException, JAXBException, IOException{
        
        if(object == null){
            throw new IllegalArgumentException("The object can't be null");
        }else if(outputStream == null){
            throw new IllegalArgumentException("The output stream can't be null");
        }
        
        List<ValidationEvent> validationEvents = new ArrayList<>();
        try{    
            if(logger.isInfoEnabled()){
                logger.info("Writing contents to file of outputStream.");
            }
    
            Marshaller marshaller = getMarshaller(clazz, schemaFile);
            marshaller.setEventHandler(new ValidationEventHandler() {
                
                @Override
                public boolean handleEvent(ValidationEvent event) {
                    validationEvents.add(event);
                    return continueWritingOnValidationError;
                }
            });
            marshaller.marshal(object, outputStream);
        }finally{
            //make sure to close the output stream, otherwise this file can't be deleted
            outputStream.close();
        }
        
        if(logger.isInfoEnabled()){
            logger.info("Successfully wrote contents");
        }
        
        return validationEvents;
    }
    
    /**
     * Return a marshaller for the given schema class.
     * 
     * @param clazz - the generated class to get a marshaller for.  Can't be null.
     * @param schemaFile - the schema that defines the content structure.  Shouldn't be null.
     * @return a marshaller responsible for serializing trees back into XML content
     * @throws SAXException If a SAX error occurs during parsing.
     * @throws JAXBException Can occur when marshalling
     */
    private static Marshaller getMarshaller(Class<?> clazz, File schemaFile) throws SAXException, JAXBException{
        
        if(clazz == null){
            throw new IllegalArgumentException("The class can't be null");
        }
        
        Schema schema;
        synchronized(schemaFactory) {
            schema = schemaFactory.newSchema(new StreamSource(schemaFile)); 
        }
        
        JAXBContext jc = AbstractSchemaHandler.getJAXBContext(clazz, null);
        Marshaller marshaller = jc.createMarshaller();
        marshaller.setSchema(schema);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, StandardCharsets.UTF_8.name());
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        
        return marshaller;
    }

    /**
     * Return the schema version attribute from the GIFT XML file provided value as a number. All
     * GIFT XML files have the version attribute as a root XML element attribute and are formatted
     * based on VersionEnum.java values (e.g. "6.0")
     * 
     * @param xmlFile the GIFT XML file to parse and retrieve the GIFT schema version number from.
     *        If null or doesn't exist an exception is thrown.
     * @return the schema version number of the file being parsed. Null will be returned if the
     *         version attribute could not be found (which normally means it is a v1.0 or v2.0 file)
     */
    public static Double getVersionFromDocument(FileProxy xmlFile) {

        if (xmlFile == null || !xmlFile.exists()) {
            throw new IllegalArgumentException("The file " + xmlFile + " doesn't exist.");
        }

        // Parse the to-be-converted xml file to find its version number.
        Document fileContents = XMLParseUtil.parseXmlFile(xmlFile);

        // The root element should contain the file's version number.
        Element rootElement = fileContents.getDocumentElement();

        if (rootElement.getAttribute("version").isEmpty()) {
            /* The root element does not specify a version number, which implies that it is either a
             * v1.0 or v2.0 file. GIFT does not currently support v1.0 conversion. */
            return null;
        } else {
            // registeredUtils is currently holding all existing ConversionWizardUtil classes.
            // Return the class that the current file version needs.

            // Takes care of the case where version is X.X.X for conversion to double later
            String[] versionFormat = rootElement.getAttribute("version").split("\\.");
            String versionStr = versionFormat[0] + "." + versionFormat[1];
            double vNum = Double.parseDouble(versionStr);
            return vNum;
        }
    }

    /**
     * Compares the versions from the two provided files.
     * 
     * @param file1 the first file to compare. Can't be null.
     * @param file2 the second file to compare. Can't be null.
     * @param expectedVersion optional field. Will perform an additional check to see if the
     *        provided files are of the expected version or not.
     * @throws FileVersionException if the file versions are different or (optionally) if the files
     *         do not match the expected version.
     */
    public static void compareFileVersions(FileProxy file1, FileProxy file2, String expectedVersion) throws FileVersionException {
        if (file1 == null) {
            throw new IllegalArgumentException("The parameter 'file1' cannot be null.");
        } else if (file2 == null) {
            throw new IllegalArgumentException("The parameter 'file2' cannot be null.");
        }

        Double file1Version = getVersionFromDocument(file1);
        if (file1Version == null) {
            throw new IllegalArgumentException(
                    "A version number could not be found within the file '" + file1.getFileId() + "'.");
        }
        Double file2Version = getVersionFromDocument(file2);
        if (file2Version == null) {
            throw new IllegalArgumentException(
                    "A version number could not be found within the file '" + file2.getFileId() + "'.");
        }

        // the file and schema should be the same version
        if (file1Version.compareTo(file2Version) != 0) {
            // String.valueOf is null safe
            throw new FileVersionException(file1.getFileId(), String.valueOf(file1Version), file2.getFileId(),
                    String.valueOf(file2Version), null);
        }

        // checking the expected version is optional. If none was provided, return from the method.
        if (StringUtils.isBlank(expectedVersion)) {
            return;
        }

        /* File versions are the same by this point, only need to check 1 of them. The reason we
         * aren't calling checkFileVersion is because we don't want to have to parse the document
         * again for the version */
        if (!StringUtils.equals(file1Version.toString(), expectedVersion)) {
            // String.valueOf is null safe
            throw new FileVersionException(file1.getFileId(), String.valueOf(file1Version), expectedVersion, null);
        }
    }

    /**
     * Compares the version of the file against the expected version.
     * 
     * @param file the file to compare. Can't be null.
     * @param expectedVersion the expected version of the provided file.
     * @throws FileVersionException if the file version is different than the expected version.
     */
    public static void checkFileVersion(FileProxy file, String expectedVersion) throws FileVersionException {
        if (file == null) {
            throw new IllegalArgumentException("The parameter 'file' cannot be null.");
        } else if (StringUtils.isBlank(expectedVersion)) {
            throw new IllegalArgumentException("The parameter 'expectedVersion' cannot be blank.");
        }

        Double fileVersion = getVersionFromDocument(file);
        if (fileVersion == null) {
            throw new IllegalArgumentException(
                    "A version number could not be found within the file '" + file.getFileId() + "'.");
        }

        if (!StringUtils.equals(fileVersion.toString(), expectedVersion)) {
            // String.valueOf is null safe
            throw new FileVersionException(file.getFileId(), String.valueOf(fileVersion), expectedVersion, null);
        }
    }

    /**
     * Return a string representation of the object.  This will convert the object, which is a generated class object of class 'clazz',
     * into an XML formatted string based on the schema known to this handler.
     * Note: this currently doesn't work with XML files that are schema valid against older schemas.  Use get
     * getUnmarshalledFile method for handling XML files that are OR could be from an older schema.
     * 
     * @param object the object to return as an XML string
     * @param clazz the generated class for the object
     * @param schemaFile the schema file for the object
     * @return String - the XML string with the content from the object provided.
     * @throws SAXException If a SAX error occurs during parsing.
     * @throws JAXBException Can occur when marshalling
     */
    public static String getAsXMLString(Serializable object, Class<?> clazz, File schemaFile) throws SAXException, JAXBException{
        return getAsXMLString(object, clazz, schemaFile, false);
    }

    /**
     * Return a string representation of the object.  This will convert the object, which is a generated class object of class 'clazz',
     * into an XML formatted string based on the schema known to this handler.
     * Note: this currently doesn't work with XML files that are schema valid against older schemas.  Use get
     * getUnmarshalledFile method for handling XML files that are OR could be from an older schema.
     * 
     * @param object the object to return as an XML string
     * @param clazz the generated class for the object
     * @param schemaFile the schema file for the object
     * @param continueWritingOnValidationError whether or not to continue the marshalling process if a validation error occurs.
     * @return String - the XML string with the content from the object provided.
     * @throws SAXException If a SAX error occurs during parsing.
     * @throws JAXBException Can occur when marshalling
     */
    public static String getAsXMLString(Serializable object, Class<?> clazz, File schemaFile, boolean continueWritingOnValidationError) throws SAXException, JAXBException{
        
        StringWriter stringWriter = new StringWriter();
        
        if(logger.isInfoEnabled()){
            logger.info("Writing contents to a string...");
        }
        
        Marshaller marshaller = getMarshaller(clazz, schemaFile);
        marshaller.setEventHandler(new ValidationEventHandler() {
            @Override
            public boolean handleEvent(ValidationEvent event) {
                return continueWritingOnValidationError;
            }
        });
        marshaller.marshal(object, stringWriter);
        
        String value = stringWriter.toString();
        if(logger.isInfoEnabled()){
            logger.info("Successfully wrote contents to a string = "+value);
        }
        
        return value;
        
    }
    
    /**
     * Return an instance of a generated class from the XML string provided.
     * Note: this currently doesn't work with XML files that are schema valid against older schemas.  Use get
     * getUnmarshalledFile method for handling XML files that are OR could be from an older schema.
     * 
     * @param xmlStr the XML string starting with the element associated with the 'clazz' value
     * @param clazz a generated class associated with the schema    
     * @param schemaFile the schema containing the element definition.
     * If null, the schema is not used during parsing to validate the XML contents.
     * @param failOnFirstSchemaError - if true than a validation event will cause the parsing of the XML content to fail and throw an exception.
     * If there are no validation events than the XML contents are XML and schema valid.
     * From Java API docs: A validation event indicates that a problem was encountered while validating the incoming XML data during an unmarshal operation, while 
     * performing on-demand validation of the Java content tree, or while marshalling the Java content tree back to XML data.
     * @return a generated class object containing the values provided in the XML string
     * @throws SAXException If a SAX error occurs during parsing.
     * @throws JAXBException Can occur when unmarshalling
     * @throws UnsupportedEncodingException  If the named charset is not supported when encoding the XML string
     * @throws FileNotFoundException if the schema file was not found
     */
    public static UnmarshalledFile getFromXMLString(String xmlStr, Class<?> clazz, File schemaFile, 
            boolean failOnFirstSchemaError) throws JAXBException, SAXException, UnsupportedEncodingException, FileNotFoundException{
                
        InputStream inputStream = new ByteArrayInputStream(xmlStr.getBytes("UTF-8"));
        return parseAndValidate(clazz, inputStream, schemaFile, failOnFirstSchemaError);
    }
    
    /**
     * Return the file type of this handler instance based on the {@link #schemaFile} value.
     * See {@link #getSchemaFile(FileType)}.
     * @return the file type for the schema file provided to this handler.  Can be null if 
     * the schema file was not set (only 1 use case so far) or the mapping of schema to file type
     * was not set yet.
     */
    public FileType getFileType() {
        return getFileType(schemaFile);
    }
    
    /**
     * Return the file type for the schema file provided.
     * 
     * @param schemaFile used to lookup the file type for that schema
     * @return the file type for the schema provided.  Can be null if the schema file is null
     * or the mapping of schema to file type was not set yet.
     */
    public static FileType getFileType(File schemaFile) {
        if(COURSE_SCHEMA_FILE.equals(schemaFile)) {
            return FileType.COURSE;
        }else if(DKF_SCHEMA_FILE.equals(schemaFile)) {
            return FileType.DKF;
        }else if(LEARNER_SCHEMA_FILE.equals(schemaFile)) {
            return FileType.LEARNER_CONFIGURATION;
        }else if(METADATA_SCHEMA_FILE.equals(schemaFile)) {
            return FileType.METADATA;
        }else if(EMAP_PEDAGOGICAL_SCHEMA_FILE.equals(schemaFile)) {
            return FileType.EMAP_PEDAGOGICAL_CONFIGURATION;
        }else if(ICAP_POLICY_SCHEMA_FILE.equals(schemaFile)) {
            return FileType.ICAPPOLICY;
        }else if(SENSOR_SCHEMA_FILE.equals(schemaFile)) {
            return FileType.SENSOR_CONFIGURATION;
        }else if(TRAINING_APP_ELEMENT_SCHEMA_FILE.equals(schemaFile)) {
            return FileType.TRAINING_APP_REFERENCE;
        }else if(INTEROP_SCHEMA_FILE.equals(schemaFile)) {
            return FileType.INTEROP_CONFIGURATION;
        }else if(LESSON_MATERIAL_SCHEMA_FILE.equals(schemaFile)) {
            return FileType.LESSON_MATERIAL_REF;
        }else if(CONVERSATION_TREE_SCHEMA_FILE.equals(schemaFile)) {
            return FileType.CONVERSATION;
        }else if(VIDEO_SCHEMA_FILE.equals(schemaFile)) {
            return FileType.VIDEO_METADATA;
        }else if(LMS_CONNECTIONS_SCHEMA_FILE.equals(schemaFile)) {
            return FileType.LMS_CONNECTIONS;
        }else {
            return null;
        }
    }
    
    /**
     * Return the enumerated GIFT file type based on the extension of the file name provided.
     * 
     * @param filenameOrExtension a file extension (see AbstractSchemaHandler.java) or GIFT XML file name that is used
     * to determine the enumerated file type.  Can't be null or empty.
     * @return the enumerated GIFT file type based on the provided parameter (e.g. ".course.xml" or "ABC.course.xml")
     * @throws IllegalArgumentException if the file name is not a GIFT authorable file type (e.g. dkf.xml)
     */
    public static FileType getFileType(String filenameOrExtension) throws IllegalArgumentException{
        
        if(filenameOrExtension == null || filenameOrExtension.isEmpty()){
            throw new IllegalArgumentException("The filenameOrExtension can't be null or empty.");
        }
                
        if(filenameOrExtension.endsWith(FileUtil.BACKUP_FILE_EXTENSION)) {
            // handle backup files
        	filenameOrExtension = filenameOrExtension.replace(FileUtil.BACKUP_FILE_EXTENSION, "");
        }
        
        if(filenameOrExtension.endsWith(AbstractSchemaHandler.COURSE_FILE_EXTENSION)){
            return FileType.COURSE;
        }else if(filenameOrExtension.endsWith(AbstractSchemaHandler.DKF_FILE_EXTENSION)){
            return FileType.DKF;
        }else if(filenameOrExtension.endsWith(AbstractSchemaHandler.LEARNER_CONFIG_FILE_EXTENSION)){
            return FileType.LEARNER_CONFIGURATION;
        }else if(filenameOrExtension.endsWith(AbstractSchemaHandler.METADATA_FILE_EXTENSION)){
            return FileType.METADATA;
        }else if(filenameOrExtension.endsWith(AbstractSchemaHandler.EMAP_PEDAGOGICAL_CONFIG_FILE_EXTENSION)){
            return FileType.EMAP_PEDAGOGICAL_CONFIGURATION;
        }else if(filenameOrExtension.endsWith(AbstractSchemaHandler.ICAP_POLICY_FILE_EXTENSION)){
            return FileType.ICAPPOLICY;
        }else if(filenameOrExtension.endsWith(AbstractSchemaHandler.SENSOR_CONFIG_FILE_EXTENSION)){
            return FileType.SENSOR_CONFIGURATION;
        }else if(filenameOrExtension.endsWith(AbstractSchemaHandler.TRAINING_APP_FILE_EXTENSION)){
            return FileType.TRAINING_APP_REFERENCE;
        }else if(filenameOrExtension.endsWith(AbstractSchemaHandler.INTEROP_FILE_EXTENSION)){
            return FileType.INTEROP_CONFIGURATION;
        }else if(filenameOrExtension.endsWith(AbstractSchemaHandler.LESSON_MATERIAL_FILE_EXTENSION)){
            return FileType.LESSON_MATERIAL_REF;
        }else if(filenameOrExtension.endsWith(AbstractSchemaHandler.CONVERSATION_TREE_FILE_EXTENSION)){
            return FileType.CONVERSATION;
        }else if(filenameOrExtension.endsWith(FileUtil.PARADATA_FILE_EXTENSION)){
            return FileType.PARADATA;
        }else if(filenameOrExtension.endsWith(FileUtil.QUESTION_EXPORT_SUFFIX)){
            return FileType.QUESTION_EXPORT;
        }else if(filenameOrExtension.endsWith(FileUtil.SURVEY_REF_EXPORT_SUFFIX)){
            return FileType.SURVEY_EXPORT;
        }else if(filenameOrExtension.endsWith(FileUtil.XTSP_JSON_EXPORT_SUFFIX)) {
        	return FileType.XTSP_JSON;
        }else if(filenameOrExtension.endsWith(AbstractSchemaHandler.VIDEO_FILE_EXTENSION)){
            return FileType.VIDEO_METADATA;
        }else{
            throw new IllegalArgumentException("Unhandled extension in '"+filenameOrExtension+"'.");
        }
    }
    
    /**
     * Return the file extension for the given GIFT file type.
     * 
     * @param fileType a GIFT file type. Can't be null or empty.
     * @return the file extension for this file type
     */
    public static String getFileExtension(FileType fileType){
        
        switch(fileType){
        
        case COURSE:
            return COURSE_FILE_EXTENSION;
        case DKF:
            return DKF_FILE_EXTENSION;
        case INTEROP_CONFIGURATION:
            return INTEROP_FILE_EXTENSION;
        case LEARNER_CONFIGURATION:
            return LEARNER_CONFIG_FILE_EXTENSION;
        case LESSON_MATERIAL_REF:
            return LESSON_MATERIAL_FILE_EXTENSION;
        case METADATA:
            return METADATA_FILE_EXTENSION;
        case EMAP_PEDAGOGICAL_CONFIGURATION:
            return EMAP_PEDAGOGICAL_CONFIG_FILE_EXTENSION;
        case ICAPPOLICY:
            return ICAP_POLICY_FILE_EXTENSION;
        case SENSOR_CONFIGURATION:
            return SENSOR_CONFIG_FILE_EXTENSION;
        case TRAINING_APP_REFERENCE:
            return TRAINING_APP_FILE_EXTENSION;
        case CONVERSATION:
            return CONVERSATION_TREE_FILE_EXTENSION;
        case VIDEO_METADATA:
            return VIDEO_FILE_EXTENSION;
        default:
            throw new IllegalArgumentException("Unhandled file type of '"+fileType+"'.");
        }
    }
    
    /**
     * Return the root JAXB generated class for the XML file type specified.  The root class
     * represents the root XML element in that XML files of the file type.
     * 
     * @param fileType used to lookup the root JAXB generated class
     * @return the root JAXB generated class for that file type
     */
    public static Class<?> getRootClass(FileType fileType){
        
        switch(fileType){
        
        case COURSE:
            return COURSE_ROOT;
        case DKF:
            return DKF_ROOT;
        case INTEROP_CONFIGURATION:
            return INTEROP_ROOT;
        case LEARNER_CONFIGURATION:
            return LEARNER_ROOT;
        case LESSON_MATERIAL_REF:
            return LESSON_MATERIAL_ROOT;
        case METADATA:
            return METADATA_ROOT;
        case EMAP_PEDAGOGICAL_CONFIGURATION:
            return EMAP_PEDAGOGICAL_ROOT;
        case ICAPPOLICY:
            return ICAP_POLICY_ROOT;
        case SENSOR_CONFIGURATION:
            return SENSOR_ROOT;
        case TRAINING_APP_REFERENCE:
            return TRAINING_APP_ELEMENT_ROOT;
        case CONVERSATION:
            return CONVERSATION_TREE_ELEMENT_ROOT;
        case VIDEO_METADATA:
            return VIDEO_ROOT;
        default:
            throw new IllegalArgumentException("Unhandled file type of '"+fileType+"'.");
        }
    }
    
    /**
     * Return the object factory for the file type provided.  The object factory
     * is the class loader for our generated classes (e.g. generated.course.ObjectFactory.class).
     * @param fileType an enumerated file type for the GIFT schemas to look up the generated object factory class.
     * @return the generated object factory class for the file type provided (e.g. generated.course.ObjectFactory.class).
     */
    public static Class<?> getSchemaObjectFactory(FileType fileType){
        
        if(fileType == null) {
            return null;
        }
        
        switch(fileType){
        
        case COURSE:
            return generated.course.ObjectFactory.class;
        case DKF:
            return generated.dkf.ObjectFactory.class;
        case INTEROP_CONFIGURATION:
            return generated.course.ObjectFactory.class;
        case LEARNER_CONFIGURATION:
            return generated.learner.ObjectFactory.class;
        case LESSON_MATERIAL_REF:
            return generated.course.ObjectFactory.class;
        case METADATA:
            return generated.metadata.ObjectFactory.class;
        case EMAP_PEDAGOGICAL_CONFIGURATION:
            return generated.ped.ObjectFactory.class;
        case ICAPPOLICY:
            return generated.ped.ObjectFactory.class;
        case SENSOR_CONFIGURATION:
            return generated.sensor.ObjectFactory.class;
        case TRAINING_APP_REFERENCE:
            return generated.course.ObjectFactory.class;
        case CONVERSATION:
            return generated.conversation.ObjectFactory.class;
        case VIDEO_METADATA:
            return generated.video.ObjectFactory.class;
        case LMS_CONNECTIONS:
            return generated.lms.ObjectFactory.class;
        default:
            return null;
        }
    }
    
    /**
     * Return the XML schema file (.xsd) for the XML file type specified.  
     * 
     * @param fileType used to lookup the XML schema file
     * @return the schema file for XML files of that type
     */
    public static File getSchemaFile(FileType fileType){
        
        switch(fileType){
        
        case COURSE:
            return COURSE_SCHEMA_FILE;
        case DKF:
            return DKF_SCHEMA_FILE;
        case INTEROP_CONFIGURATION:
            return INTEROP_SCHEMA_FILE;
        case LEARNER_CONFIGURATION:
            return LEARNER_SCHEMA_FILE;
        case LESSON_MATERIAL_REF:
            return LESSON_MATERIAL_SCHEMA_FILE;
        case METADATA:
            return METADATA_SCHEMA_FILE;
        case EMAP_PEDAGOGICAL_CONFIGURATION:
            return EMAP_PEDAGOGICAL_SCHEMA_FILE;
        case ICAPPOLICY:
            return ICAP_POLICY_SCHEMA_FILE;
        case SENSOR_CONFIGURATION:
            return SENSOR_SCHEMA_FILE;
        case TRAINING_APP_REFERENCE:
            return TRAINING_APP_ELEMENT_SCHEMA_FILE;
        case CONVERSATION:
            return CONVERSATION_TREE_SCHEMA_FILE;
        case VIDEO_METADATA:
            return VIDEO_SCHEMA_FILE;
        default:
            throw new IllegalArgumentException("Unhandled file type of '"+fileType+"'.");
        }
    }
    
    /**
     * This base class is for the additional validation settings needed for various types of
     * GIFT schema based XML validation.
     * 
     * @author mhoffman
     *
     */
    public static class AbstractAdditionalValidationSettings{
        
        /** a description of why this additional validation is needed */
        private String reason;
        
        /**
         * Class constructor - set the reason for this additional validation logic
         * 
         * @param reason a description of why this additional validation is needed 
         */
        public AbstractAdditionalValidationSettings(String reason){            
            
            if(reason == null){
                throw new IllegalArgumentException("The reason can't be null.");
            }
            
            this.reason = reason;
        }
        
        public String getReason(){
            return reason;
        }
        
        @Override
        public String toString(){
            return "reason = "+getReason();
        }
        
    }
}
