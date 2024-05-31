/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.common.conversion;

import java.awt.Color;
import java.awt.Desktop;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.border.EtchedBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import mil.arl.gift.common.enums.VersionEnum;
import mil.arl.gift.common.io.AbstractFolderProxy;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.AbstractSchemaHandler.FileType;
import mil.arl.gift.common.io.ClassFinderUtil;
import mil.arl.gift.common.io.ConversionIssueList;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileFinderUtil;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.common.io.FileVersionException;
import mil.arl.gift.common.io.UnmarshalledFile;
import mil.arl.gift.common.io.UriUtil;
import mil.arl.gift.common.io.Version;
import mil.arl.gift.common.io.XMLParseUtil;
import mil.arl.gift.common.util.StringUtils;

//TODO: currently the XML generation logic can't fully handle creating all XML elements when a choice element is encountered
//    that allows only 1 choice but has more than one item to choose.  Ideally, the logic would recursively pass that information
//    up the XML tree until a multiple choice or list item was found.  Then it would create similar objects to the level of the single 
//    choice element but each time choosing a different item until all items were chosen.
/**
 * This is the base class for conversion wizard utility classes.  It contains the common methods useful
 * for converting between XML content between XML schema (.xsd) versions.
 * 
 * In addition this class can auto-generate classes from a root element.  The auto generated classes instantiates every element/attribute specified 
 * by a schema, including choices and optional items.
 * 
 * @author mhoffman
 *
 */
public abstract class AbstractConversionWizardUtil {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(AbstractConversionWizardUtil.class);
    
    /** the current version of the file being converted */
    private String workingVersion = null;
    
    /** 
     * mapping of GIFT version to the conversion utility for XML files of that version
     * key: a GIFT version specified by Version.java
     * value: an instance of the conversion utility that can convert XML files of that version to the next GIFT version (e.g. 3.0 to 4.0).
     */
    private static Map<VersionEnum, AbstractConversionWizardUtil> registeredUtils = new HashMap<VersionEnum, AbstractConversionWizardUtil>();
    
    static{
        
        /* registeredUtils should hold all existing ConversionWizardUtils mapped to their version
         * number. The key is the source version of the conversion wizard (e.g. wizard converts from
         * v4 to v5; the key will be version 4) */
        try {
            registeredUtils.put(VersionEnum.VERSION_2_0, ConversionWizardUtil_v2_3.class.getDeclaredConstructor().newInstance());
            registeredUtils.put(VersionEnum.VERSION_3_0, ConversionWizardUtil_v3_4.class.getDeclaredConstructor().newInstance());
            registeredUtils.put(VersionEnum.VERSION_4_0, ConversionWizardUtil_v4_v2014_2.class.getDeclaredConstructor().newInstance());
            registeredUtils.put(VersionEnum.VERSION_5_0, ConversionWizardUtil_v2014_2_v2014_3X.class.getDeclaredConstructor().newInstance());
            registeredUtils.put(VersionEnum.VERSION_5_1, ConversionWizardUtil_v2014_3X_v2015_1.class.getDeclaredConstructor().newInstance());
            registeredUtils.put(VersionEnum.VERSION_6_0, ConversionWizardUtil_v2015_1_v2016_1.class.getDeclaredConstructor().newInstance());
            registeredUtils.put(VersionEnum.VERSION_7_0, ConversionWizardUtil_v2016_1_v2018_1.class.getDeclaredConstructor().newInstance());
            registeredUtils.put(VersionEnum.VERSION_8_0, ConversionWizardUtil_v2018_1_v2019_1.class.getDeclaredConstructor().newInstance());
            registeredUtils.put(VersionEnum.VERSION_9_0, ConversionWizardUtil_v2019_1_v2020_1.class.getDeclaredConstructor().newInstance());
            registeredUtils.put(VersionEnum.VERSION_10_0, ConversionWizardUtil_v2020_1_v2021_1.class.getDeclaredConstructor().newInstance());
            
        } catch (Exception e) {
            
            registeredUtils.clear();
            
            logger.error("The ability to convert between XML schema versions is disabled because of the following exception", e);
        }
    }
    
    /** container for conversion issues associated by file names */
    protected ConversionIssueList conversionIssueList = new ConversionIssueList();
    
    /**
     * Gets the ConversionWizardUtil for the specified version 
     * @param version the version of the ConversionWizardUtil to use
     * @return the AbstractConversionWizardUtil for the specified version
     */
    public static AbstractConversionWizardUtil getConversionWizardForVersion(String version) {
        return registeredUtils.get(VersionEnum.valueOf(version));
    }
    
    /**
     * Gets the Version for the specified conversion wizard.
     * 
     * @param conversionUtilClass the conversion wizard
     * @return the version of the specified conversion wizard. Can be null if the conversion wizard
     *         was not found.
     */
    public static <T extends AbstractConversionWizardUtil> VersionEnum getVersionForConversionWizard(T conversionUtilClass) {
        for (Entry<VersionEnum, AbstractConversionWizardUtil> entry : registeredUtils.entrySet()) {
            if (entry.getValue().getClass() == conversionUtilClass.getClass()) {
                return entry.getKey();
            }
        }

        return null;
    }

    /**
     * Return the schema version attribute from the GIFT XML file provided value as a number.  All
     * GIFT XML files have the version attribute as a root XML element attribute and are formatted
     * based on VersionEnum.java values (e.g. "6.0")
     * 
     * @param xmlFile the GIFT XML file to parse and retrieve the GIFT schema version number from.  If null or
     * doesn't exist an exception is thrown.
     * @return the schema version number of the file being parsed.  Null will be returned if the version
     * attribute could not be found (which normally means it is a v1.0 or v2.0 file)
     */
    public static Double getVersionFromDocument(FileProxy xmlFile){
        
        if(xmlFile == null || !xmlFile.exists()){
            throw new IllegalArgumentException("The file "+xmlFile+" doesn't exist.");
        }
        
        // Parse the to-be-converted xml file to find its version number.
        Document fileContents = XMLParseUtil.parseXmlFile(xmlFile);
        
        // The root element should contain the file's version number.
        Element rootElement = fileContents.getDocumentElement();
                
        if (rootElement.getAttribute("version").isEmpty()) {
            /* The root element does not specify a version number, which implies that it is either 
               a v1.0 or v2.0 file. GIFT does not currently support v1.0 conversion. */            
            return null;
        }else {
            // registeredUtils is currently holding all existing ConversionWizardUtil classes.
            // Return the class that the current file version needs.
            
            //Takes care of the case where version is X.X.X for conversion to double later
            String[] versionFormat = rootElement.getAttribute("version").split("\\.");
            String versionStr = versionFormat[0] + "." + versionFormat[1];
            double vNum = Double.parseDouble(versionStr);
            return vNum;
        }
    }
    
    /**
     * Retrieve the conversion wizard utility instance based on the current version of GIFT.
     * 
     * @param file the file that is being converted
     * @param showSwingDialogIfMissingVersion If the file doesn't have a version attribute that means it is either Version 1 or Version 2.
     * We can't convert Version 1 files but we can convert Version 2 files. If this flag is set to true then we'll prompt the user to explain
     * the situation and allow them to decide if we should move forward under the assumption that we're dealing with a Version 2 file or if
     * we should just give up. If this flag is set to false then we'll skip the prompt and move forward under the Version 2 assumption.</br>
     * Note: the prompt is current a Java Swing dialog and should only be used in conjuction with Desktop authoring tools, not the GAT!!!
     * @return AbstractConversionWizardUtil - The conversion wizard instance to use for this version of GIFT
     * @throws LatestVersionException - thrown if the user is trying to convert an already up-to-date file
     * @throws UnsupportedVersionException - thrown if the file that the user is trying to convert is not registered for conversion
     * @throws DetailedException - thrown if there was a problem parsing the file
     */
    public static AbstractConversionWizardUtil getConversionUtil(FileProxy file, boolean showSwingDialogIfMissingVersion) 
            throws LatestVersionException, UnsupportedVersionException, DetailedException {
        
        Double versionNumber = getVersionFromDocument(file);        
        
        // Store the file name in case there are problems with the migration.
        // issueList is used to report these problems
        ConversionIssueList issueList = new ConversionIssueList();
        issueList.put(file.getFileId(), new HashMap<String, Integer>()); 
        
        if (versionNumber == null) {
            /* The root element does not specify a version number, which implies that it is either 
               a v1.0 or v2.0 file. GIFT does not currently support v1.0 conversion.
               Warn the user that they can't convert v1.0 */            
            int choice = JOptionPane.OK_OPTION;
            if(showSwingDialogIfMissingVersion) {
                choice = showVersionUnknownDialog();
            }
        
            if (choice == JOptionPane.OK_OPTION) {
                
                // User wants to continue. Assume that the file is a v2.0 and attempt to convert it.
                // If it is a v1.0 the conversion will fail.
                AbstractConversionWizardUtil util = registeredUtils.get(VersionEnum.VERSION_2_0); // Return ConversionWizardUtil_v2_3
                util.setWorkingVersion(VersionEnum.VERSION_2_0.getSchemaVersion());
                util.setConversionIssueList(issueList);
                return util; 
                
            }else {
                // User cancelled
                return null;
            }
        }else {
            // registeredUtils is currently holding all existing ConversionWizardUtil classes.
            // Return the class that the current file version needs.
            
            if (versionNumber.toString().equals((Version.getInstance().getCurrentSchemaVersion()))) {                
                // The file is already at the latest version of GIFT.
                throw new LatestVersionException("This file is already at the latest version of GIFT.");
            }
            
            if (versionNumber > Double.parseDouble(Version.getInstance().getCurrentSchemaVersion()) || versionNumber < 2) {
                // The file's version number is not registered for conversion.
                // registeredUtils should contain every existing ConversionWizardUtil_v* class 
                throw new UnsupportedVersionException("Unable to convert '"+file.getName()+"' because its schema version number of "+versionNumber+" is not supported by this GIFT instance. The conversion wizard is able to "+
                                                        "convert files between:\nversion "+VersionEnum.VERSION_2_0.getName()+" "+VersionEnum.VERSION_2_0+"\nand\n this GIFT instance version of "+Version.getInstance().getName()+" "+Version.getInstance()+".\n\n"
                                                                + "If you think that this version of GIFT should be able to handle schema version number "+versionNumber+" files than perhaps the GIFT/config/version.txt file needs to be updated.");
            }
            
            if(!registeredUtils.containsKey(VersionEnum.valueOf(versionNumber.toString()))) {
                // If a conversion wizard for this specific version cannot be found, 
                // use the closest match to its major version.
                                
                List<VersionEnum> versionList = VersionEnum.VALUES();
                
                for(VersionEnum v : versionList){
                    if(versionNumber > v.getVersionAsDouble()) {
                        logger.warn("Conversion Wizard for version " + versionNumber + " could not be found. Using closest version match of " + v.getSchemaVersion());
                        return registeredUtils.get(v);
                    }
                }
            }
            
            AbstractConversionWizardUtil util =  registeredUtils.get(VersionEnum.valueOf(versionNumber.toString()));
            util.setWorkingVersion(versionNumber.toString());
            util.setConversionIssueList(issueList);
            
            return util;
        }
    }
    
    /**
     * Updates all the files within the provided folder to the latest version. Only the files
     * matching the provided file types will be updated.
     * 
     * @param courseFile the course xml file to use to determine if all the GIFT XML files
     * in the folder need to be updated as well
     * @param courseFolder the course folder that contains the course file
     * @param createBackup true to create a backup of the file (.bak) before updating. A backup will
     *        only be created if the file needs to be updated.
     * @return the list of files that were updated (the fully qualified path to the file). Can be
     *         empty if the course is up to date.
     * @throws IOException if there was a problem retrieving the files
     * @throws URISyntaxException if there was a problem retrieving the parent folder of the file
     */
    public static List<String> updateCourseToLatestVersion(FileProxy courseFile, AbstractFolderProxy courseFolder,
            boolean createBackup) throws IOException, URISyntaxException {

        // check if course.xml is the latest version
        try {
            AbstractSchemaHandler.checkFileVersion(courseFile, Version.getInstance().getCurrentSchemaVersion());
            // the check passed, course file is up to date
            return new ArrayList<String>();
        } catch (@SuppressWarnings("unused") FileVersionException e) {
            // the check failed, upconvert the files within the course folder
            return upconvertAll(courseFile, courseFolder, createBackup);
        }
    }
    
    /**
     * Converts the all but the course file to the latest schema version.
     * 
     * @param trainingAppRefFile a training application reference file to use to determine if all the GIFT XML files
     * in the folder need to be updated as well
     * @param folder the training app course object folder that contains one or more GIFT XML files
     * @param createBackup true to create a backup of the file (.bak) before updating. A backup will
     *        only be created if the file needs to be updated.
     * @return the list of files that were updated (the fully qualified path to the file). Can be
     *         empty if the course is up to date.
     * @throws IOException if there was a problem retrieving the files
     * @throws URISyntaxException if there was a problem retrieving the parent folder of the file
     */
    public static List<String> updateTrainingAppToLatestVersion(FileProxy trainingAppRefFile, AbstractFolderProxy folder,
            boolean createBackup) throws IOException, URISyntaxException {
        
        // check if trainingApp.xml is the latest version
        try {
            AbstractSchemaHandler.checkFileVersion(trainingAppRefFile, Version.getInstance().getCurrentSchemaVersion());
            // the check passed, training app file is up to date
            return new ArrayList<String>();
        } catch (@SuppressWarnings("unused") FileVersionException e) {
            // the check failed, upconvert the files within the training app folder
            return upconvertAll(folder, createBackup);
        }    
    }

    /**
     * Upconverts the provided course file and all GIFT files within the course folder to the latest
     * schema version.
     * 
     * @param courseFile the course xml file
     * @param courseFolder the course folder that contains the course file and that contains one or more GIFT XML files
     * @param createBackup true to create a backup of the file (.bak) before updating. A backup will
     *        only be created if the file needs to be updated.
     * @return the list of files that were updated (the fully qualified path to the file).
     * @throws IOException if there was a problem retrieving the files
     * @throws URISyntaxException if there was a problem retrieving the parent folder of the file
     */
    private static List<String> upconvertAll(FileProxy courseFile, AbstractFolderProxy courseFolder,
            boolean createBackup) throws IOException, URISyntaxException {

        //
        // Ensure GIFT files are the latest version
        // 1. Courses
        // 2. DKFs
        // 3. Metadata
        // 4. Training app refs
        // 5. Sensor configs
        // 6. Learner configs
        // 7. Pedagogy configs
        //

        List<String> updatedFiles = new ArrayList<>();

        // convert course file
        boolean converted = upconvertFile(courseFile, FileType.COURSE, AbstractSchemaHandler.COURSE_ROOT,
                AbstractSchemaHandler.COURSE_SCHEMA_FILE, courseFolder, createBackup);
        if (converted) {
            updatedFiles.add(courseFile.getFileId());
        }

        updatedFiles.addAll(upconvertAll(courseFolder, createBackup));
        return updatedFiles;
    }
    
    /**
     * Up-convert all (non course.xml) GIFT XML files in the folder specified.
     * 
     * @param folder that contains one or more GIFT XML files
     * @param createBackup true to create a backup of the file (.bak) before updating. A backup will
     *        only be created if the file needs to be updated.
     * @return the list of files that were updated (the fully qualified path to the file).
     * @throws IOException if there was a problem retrieving the files
     */
    private static List<String> upconvertAll(AbstractFolderProxy folder, boolean createBackup) throws IOException{
        
        //
        // Ensure GIFT files are the latest version
        // 1. DKFs
        // 2. Metadata
        // 3. Training app refs
        // 4. Sensor configs
        // 5. Learner configs
        // 6. Pedagogy configs
        //
        
        boolean converted;
        List<String> updatedFiles = new ArrayList<>();
        List<FileProxy> files = new ArrayList<>();
        
        // convert dkf files
        FileFinderUtil.getFilesByExtension(folder, files, AbstractSchemaHandler.DKF_FILE_EXTENSION);
        for (FileProxy file : files) {
            converted = upconvertFile(file, FileType.DKF, AbstractSchemaHandler.DKF_ROOT,
                    AbstractSchemaHandler.DKF_SCHEMA_FILE, folder, createBackup);
            if (converted) {
                updatedFiles.add(file.getFileId());
            }
        }

        // convert metadata files
        files.clear();
        FileFinderUtil.getFilesByExtension(folder, files, AbstractSchemaHandler.METADATA_FILE_EXTENSION);
        for (FileProxy file : files) {
            converted = upconvertFile(file, FileType.METADATA, AbstractSchemaHandler.METADATA_ROOT,
                    AbstractSchemaHandler.METADATA_SCHEMA_FILE, folder, createBackup);
            if (converted) {
                updatedFiles.add(file.getFileId());
            }
        }

        // convert training app ref files
        files.clear();
        FileFinderUtil.getFilesByExtension(folder, files, AbstractSchemaHandler.TRAINING_APP_FILE_EXTENSION);
        for (FileProxy file : files) {
            converted = upconvertFile(file, FileType.TRAINING_APP_REFERENCE,
                    AbstractSchemaHandler.TRAINING_APP_ELEMENT_ROOT,
                    AbstractSchemaHandler.TRAINING_APP_ELEMENT_SCHEMA_FILE, folder, createBackup);
            if (converted) {
                updatedFiles.add(file.getFileId());
            }
        }

        // convert sensor config files
        files.clear();
        FileFinderUtil.getFilesByExtension(folder, files, AbstractSchemaHandler.SENSOR_CONFIG_FILE_EXTENSION);
        for (FileProxy file : files) {
            converted = upconvertFile(file, FileType.SENSOR_CONFIGURATION, AbstractSchemaHandler.SENSOR_ROOT,
                    AbstractSchemaHandler.SENSOR_SCHEMA_FILE, folder, createBackup);
            if (converted) {
                updatedFiles.add(file.getFileId());
            }
        }

        // convert learner config files
        files.clear();
        FileFinderUtil.getFilesByExtension(folder, files, AbstractSchemaHandler.LEARNER_CONFIG_FILE_EXTENSION);
        for (FileProxy file : files) {
            converted = upconvertFile(file, FileType.LEARNER_CONFIGURATION, AbstractSchemaHandler.LEARNER_ROOT,
                    AbstractSchemaHandler.LEARNER_SCHEMA_FILE, folder, createBackup);
            if (converted) {
                updatedFiles.add(file.getFileId());
            }
        }

        // convert pedagogy config files
        files.clear();
        FileFinderUtil.getFilesByExtension(folder, files,
                AbstractSchemaHandler.EMAP_PEDAGOGICAL_CONFIG_FILE_EXTENSION);
        for (FileProxy file : files) {
            converted = upconvertFile(file, FileType.EMAP_PEDAGOGICAL_CONFIGURATION, AbstractSchemaHandler.EMAP_PEDAGOGICAL_ROOT,
                    AbstractSchemaHandler.EMAP_PEDAGOGICAL_SCHEMA_FILE, folder, createBackup);
            if (converted) {
                updatedFiles.add(file.getFileId());
            }
        }

        return updatedFiles;
    }

    /**
     * Converts the provided file to the latest schema version.
     * 
     * @param fileProxy the file to update
     * @param fileType the type of the file
     * @param schemaRoot the schema root class
     * @param schemaFile the schema file to use
     * @param parentFolder the folder that contains the file to upconvert.
     * @param createBackup true to create a backup of the file before updating. A backup will only
     *        be created if the file needs to be updated.
     * @return true if the file was converted; false if the file was already the latest version.
     */
    private static boolean upconvertFile(FileProxy fileProxy, FileType fileType, Class<?> schemaRoot, File schemaFile,
            AbstractFolderProxy parentFolder, boolean createBackup) {

        try {
            AbstractConversionWizardUtil conversionUtil = AbstractConversionWizardUtil.getConversionUtil(fileProxy,
                    false);
            UnmarshalledFile newFile = conversionUtil.convertFile(fileType, fileProxy, false, false);
            parentFolder.updateFileContents(fileProxy, newFile, createBackup, true);
            return true;
        } catch (@SuppressWarnings("unused") LatestVersionException e) {
            // already at the latest version
        } catch (UnsupportedVersionException e) {
            // the file is pre-version 2.0 and can't be used.
            throw new DetailedException("Unable to up convert the file '" + fileProxy.getName() + "'.",
                    "Found an unsupported version with exception : " + e.getMessage(), e);
        } catch (Exception e) {
            throw new DetailedException("Unable to up convert the file '" + fileProxy.getName() + "'.",
                    "An error occurred during conversion: " + e.getMessage(), e);
        }

        return false;
    }

    /**
     * Overwrite this class's conversion issue object with the provided one.  This is useful
     * when chaining conversions together because the last converter will need to report
     * the issues of the converters earlier in the conversion sequence.
     * 
     * @param currentIssueList can't be null.
     */
    protected void setConversionIssueList(ConversionIssueList currentIssueList){
        
        if(currentIssueList == null){
            throw new IllegalArgumentException("The conversion issue list can't be null.");
        }
        
        this.conversionIssueList = currentIssueList;
    }
    
    /**
     * Return the container that contains conversion wizard issues associated by the file that
     * was converted and had issue(s).
     * 
     * @return conversion issues associated by file names 
     */
    public ConversionIssueList getConversionIssueList(){
        return conversionIssueList;
    }
    
    /**
     * Sets the working version of this conversion util. This is the "pre-converted" version.
     * 
     * @param version the working version of this util to set.
     */
    private void setWorkingVersion(String version) {
        workingVersion = version;
    }
    
    /**
     * Returns the version of the file that is being converted.
     * 
     * @return the schema version (see Version.java) of the file being converted.
     */
    public String getWorkingVersion() {
        return workingVersion;
    }
    
        
    /**
     * Displays a dialog to warn the user that the file's version number is unknown.
     * The user is given the choice to continue with the conversion or cancel.
     * 
     * @return 0 if user chooses "OK", or 2 if user chooses "Cancel"
     */
    private static int showVersionUnknownDialog() {
        
        String message = "<html><p>Unable to determine this file's version number. Please note that GIFT does not currently support " +
                            "v1.0 file conversion.<br>If this is not a v1.0 file, you may proceed.</p></html>";
        return JOptionPane.showConfirmDialog(null, 
                    new JLabel(message, JLabel.CENTER),
                    "Cannot determine version",
                    JOptionPane.OK_CANCEL_OPTION);
    }
    
    /**
     * Return the previous GIFT version's schema file for the specified file type.
     * Note: Returns null if there is no previous schema file for the specified file type.
     * Returns the current schema file if there were no changes since the last GIFT version.
     * 
     * @param fileType - the type of schema to retrieve
     * @return File - the previous GIFT version's schema file. 
     * Can be null if an unsupported file type was specified.
     */
    public File getPreviousSchemaFile(FileType fileType) {
        switch (fileType) {
        case DKF: 
            return getPreviousDKFSchemaFile();
        case COURSE: 
            return getPreviousCourseSchemaFile();
        case LEARNER_CONFIGURATION: 
            return getPreviousLearnerSchemaFile();
        case METADATA:
            return getPreviousMetadataSchemaFile();
        case SENSOR_CONFIGURATION:
            return getPreviousSensorSchemaFile();
        case EMAP_PEDAGOGICAL_CONFIGURATION:
            return getPreviousEMAPConfigSchemaFile();
        case ICAPPOLICY:
            return getPreviousICAPPolicySchemaFile();
        case TRAINING_APP_REFERENCE:
            return getPreviousTrainingAppSchemaFile();
        case CONVERSATION:
            return getPreviousConversationSchemaFile();
        case LESSON_MATERIAL_REF:
            return getPreviousLessonMaterialRefSchemaFile();
        default:
            logger.error("Cannot get previous schema file for file type " + fileType);
            return null;
        }
    }
    
    /**
     * Return the previous GIFT version's schema root for the specified file type.
     * Note: Returns null if there is no previous schema root for the specified file type.
     * Returns the current schema root if there were no changes since the last GIFT version.
     * 
     * @param fileType - the type of schema to retrieve
     * @return Class - the previous GIFT version's schema root generate class. 
     * Can be null if an unsupported file type was specified.
     */
    public Class<?> getPreviousSchemaRoot(FileType fileType) {
        switch (fileType) {
        case DKF:
            return getPreviousDKFSchemaRoot();
        case COURSE:
            return getPreviousCourseSchemaRoot();
        case LEARNER_CONFIGURATION:
            return getPreviousLearnerSchemaRoot();
        case METADATA:
            return getPreviousMetadataSchemaRoot();
        case SENSOR_CONFIGURATION:
            return getPreviousSensorSchemaRoot();
        case EMAP_PEDAGOGICAL_CONFIGURATION:
            return getPreviousEMAPConfigSchemaRoot();
        case ICAPPOLICY:
            return getPreviousICAPPolicySchemaRoot();
        case TRAINING_APP_REFERENCE:
            return getPreviousTrainingAppSchemaRoot();
        case CONVERSATION:
            return getPreviousConversationSchemaRoot();
        case LESSON_MATERIAL_REF:
            return getPreviousLessonMaterialRefSchemaRoot();
        default:
            logger.error("Cannot get previous schema root for file type " + fileType);
            return null;
        }
    }

    /**
     * Returns the version of the file after it gets converted.
     * 
     * @return the version of the file after it gets converted. (e.g. 9.0.1)
     */
    public String getConvertedVersionNumber() {
        VersionEnum thisVersion = getVersionForConversionWizard(this);
        VersionEnum nextVersion = thisVersion.getNextVersion();
        String schemaVersion = (nextVersion == null) ? thisVersion.getSchemaVersion() : nextVersion.getSchemaVersion();
        return schemaVersion + ".1";
    }

    /**
     * Converts a previous schema object to the current schema version. Returns null if there is no
     * conversion wizard available for the specified file type.
     * 
     * @param fileType - the type of file to convert
     * @param fileToConvert - the file to migrate to a newer version
     * @param showCompletionDialog - true if you want to show a dialog when the conversion is done, false otherwise.
     * @param failOnFirstSchemaError - if true than a validation event will cause the parsing of the XML content to fail and throw an exception.
     * If there are no validation events than the XML contents are XML and schema valid.
     * From Java API docs: A validation event indicates that a problem was encountered while validating the incoming XML data during an unmarshal operation, while 
     * performing on-demand validation of the Java content tree, or while marshalling the Java content tree back to XML data.
     * @return the new schema object or null if an unsupported file type was specified.
     * @throws Exception if there was a severe problem during conversion
     */
    public UnmarshalledFile convertFile(FileType fileType, FileProxy fileToConvert, boolean showCompletionDialog, boolean failOnFirstSchemaError) throws Exception {

        switch (fileType) {
        case DKF:
            return convertScenario(fileToConvert, showCompletionDialog, failOnFirstSchemaError);
        case COURSE:
            return convertCourse(fileToConvert, showCompletionDialog, failOnFirstSchemaError);
        case LEARNER_CONFIGURATION:
            return convertLearnerConfiguration(fileToConvert, showCompletionDialog, failOnFirstSchemaError);
        case METADATA:
            return convertMetadata(fileToConvert, showCompletionDialog, failOnFirstSchemaError);
        case SENSOR_CONFIGURATION:
            return convertSensorConfiguration(fileToConvert, showCompletionDialog, failOnFirstSchemaError);
        case EMAP_PEDAGOGICAL_CONFIGURATION:
            return convertEMAPConfiguration(fileToConvert, showCompletionDialog, failOnFirstSchemaError);
        case ICAPPOLICY:
            return convertICAPPolicy(fileToConvert, showCompletionDialog, failOnFirstSchemaError);
        case TRAINING_APP_REFERENCE:
            return convertTrainingApplicationRef(fileToConvert, showCompletionDialog, failOnFirstSchemaError);
        case CONVERSATION:
            return convertConversation(fileToConvert, showCompletionDialog, failOnFirstSchemaError);
        case LESSON_MATERIAL_REF:
            return convertLessonMaterialRef(fileToConvert, showCompletionDialog, failOnFirstSchemaError);
        default:
            logger.error("No conversion wizard available for file type " + fileType);
            return null;
        } 
    }
    
    /**
     * Return the previous GIFT version's course schema file.
     * Note: this can be the same as the current schema file if there were no changes since the last GIFT version.
     * 
     * @return File - the previous GIFT version's course schema file.
     */
    public File getPreviousCourseSchemaFile(){
        return AbstractSchemaHandler.COURSE_SCHEMA_FILE;
    }
    
    /**
     * Return the previous GIFT version's course schema root generated class.
     * Note: this can be the same as the current schema file if there were no changes since the last GIFT version.
     * 
     * @return Class<?> - the previous GIFT version's course schema root generated class.
     */
    public Class<?> getPreviousCourseSchemaRoot(){
        return AbstractSchemaHandler.COURSE_ROOT;
    }
    
    /**
     * Return the previous GIFT version's DKF schema file.
     * Note: this can be the same as the current schema file if there were no changes since the last GIFT version.
     * 
     * @return File - the previous GIFT version's DKF schema file.
     */
    public File getPreviousDKFSchemaFile(){
        return AbstractSchemaHandler.DKF_SCHEMA_FILE;
    }
    
    /**
     * Return the previous GIFT version's DKF schema root generated class.
     * Note: this can be the same as the current schema file if there were no changes since the last GIFT version.
     * 
     * @return Class<?> - the previous GIFT version's DKF schema root generated class.
     */
    public Class<?> getPreviousDKFSchemaRoot(){
        return AbstractSchemaHandler.DKF_ROOT;
    }
    
    /**
     * Return the previous GIFT version's learner config schema file.
     * Note: this can be the same as the current schema file if there were no changes since the last GIFT version.
     * 
     * @return File - the previous GIFT version's learner config schema file.
     */
    public File getPreviousLearnerSchemaFile(){
        return AbstractSchemaHandler.LEARNER_SCHEMA_FILE;
    }
    
    /**
     * Return the previous GIFT version's learner config schema root generated class.
     * Note: this can be the same as the current schema file if there were no changes since the last GIFT version.
     * 
     * @return Class<?> - the previous GIFT version's learner config schema root generated class.
     */
    public Class<?> getPreviousLearnerSchemaRoot(){
        return AbstractSchemaHandler.LEARNER_ROOT;
    }
    
    /**
     * Return the previous GIFT version's metadata schema file.
     * Note: this can be the same as the current schema file if there were no changes since the last GIFT version.
     * 
     * @return File - the previous GIFT version's metadata schema file.
     */
    public File getPreviousMetadataSchemaFile(){
        return AbstractSchemaHandler.METADATA_SCHEMA_FILE;
    }
    
    /**
     * Return the previous GIFT version's metadata schema root generated class.
     * Note: this can be the same as the current schema file if there were no changes since the last GIFT version.
     * 
     * @return Class<?> - the previous GIFT version's metadata schema root generated class.
     */
    public Class<?> getPreviousMetadataSchemaRoot(){
        return AbstractSchemaHandler.METADATA_ROOT;
    }
    
    /**
     * Return the previous GIFT version's EMAP pedagogy config schema file.
     * Note: this can be the same as the current schema file if there were no changes since the last GIFT version.
     * 
     * @return File - the previous GIFT version's EMAP pedagogy config schema file.
     */
    public File getPreviousEMAPConfigSchemaFile(){
        return AbstractSchemaHandler.EMAP_PEDAGOGICAL_SCHEMA_FILE;
    }
    
    /**
     * Return the previous GIFT version's ICAP policy schema file.
     * Note: this can be the same as the current schema file if there were no changes since the last GIFT version.
     * 
     * @return File - the previous GIFT version's ICAP policy schema file.
     */
    public File getPreviousICAPPolicySchemaFile(){
        return AbstractSchemaHandler.ICAP_POLICY_SCHEMA_FILE;
    }
    
    /**
     * Return the previous GIFT version's EMAP pedagogy config schema root generated class.
     * Note: this can be the same as the current schema file if there were no changes since the last GIFT version.
     * 
     * @return the previous GIFT version's EMAP pedagogy config schema root generated class.
     */
    public Class<?> getPreviousEMAPConfigSchemaRoot(){
        return AbstractSchemaHandler.EMAP_PEDAGOGICAL_ROOT;
    }
    
    /**
     * Return the previous GIFT version's ICAP policy schema root generated class.
     * Note: this can be the same as the current schema file if there were no changes since the last GIFT version.
     * 
     * @return the previous GIFT version's ICAP policy schema root generated class.
     */
    public Class<?> getPreviousICAPPolicySchemaRoot(){
        return AbstractSchemaHandler.ICAP_POLICY_ROOT;
    }
    
    /**
     * Return the previous GIFT version's sensor config schema file.
     * Note: this can be the same as the current schema file if there were no changes since the last GIFT version.
     * 
     * @return File - the previous GIFT version's sensor config schema file.
     */
    public File getPreviousSensorSchemaFile(){
        return AbstractSchemaHandler.SENSOR_SCHEMA_FILE;
    }
    
    /**
     * Return the previous GIFT version's sensor config schema root generated class.
     * Note: this can be the same as the current schema file if there were no changes since the last GIFT version.
     * 
     * @return Class<?> - the previous GIFT version's sensor config schema root generated class.
     */
    public Class<?> getPreviousSensorSchemaRoot(){
        return AbstractSchemaHandler.SENSOR_ROOT;
    }
    
    /**
     * Return the previous GIFT version's training app reference schema file.
     * Note: this can be the same as the current schema file if there were no changes since the last GIFT version.
     * 
     * @return File - the previous GIFT version's training app reference schema file.
     */
    public File getPreviousTrainingAppSchemaFile(){
        return AbstractSchemaHandler.TRAINING_APP_ELEMENT_SCHEMA_FILE;
    }
    
    /**
     * Return the previous GIFT version's conversation tree schema file.
     * Note: this can be the same as the current schema file if there were no changes since the last GIFT version.
     * 
     * @return  the previous GIFT version's conversation tree schema file.
     */
    public File getPreviousConversationSchemaFile(){
        return AbstractSchemaHandler.CONVERSATION_TREE_SCHEMA_FILE;
    }
    
    /**
     * Return the previous GIFT version's lesson material reference schema file.
     * Note: this can be the same as the current schema file if there were no changes since the last GIFT version.
     * 
     * @return the previous GIFT version'slesson material reference schema file.
     */
    public File getPreviousLessonMaterialRefSchemaFile(){
        return AbstractSchemaHandler.LESSON_MATERIAL_SCHEMA_FILE;
    }
    
    /**
     * Return the previous GIFT version's training app reference schema root generated class.
     * Note: this can be the same as the current schema file if there were no changes since the last GIFT version.
     * 
     * @return Class<?> - the previous GIFT version's training app reference schema root generated class.
     */
    public Class<?> getPreviousTrainingAppSchemaRoot(){
        return AbstractSchemaHandler.TRAINING_APP_ELEMENT_ROOT;
    }
    
    /**
     * Return the previous GIFT version's conversation tree schema root generated class.
     * Note: this can be the same as the current schema file if there were no changes since the last GIFT version.
     * 
     * @return the previous GIFT version's conversation tree schema root generated class.
     */
    public Class<?> getPreviousConversationSchemaRoot(){
        return AbstractSchemaHandler.CONVERSATION_TREE_ELEMENT_ROOT;
    }
    
    /**
     * Return the previous GIFT version's lesson material reference schema root generated class.
     * Note: this can be the same as the current schema file if there were no changes since the last GIFT version.
     * 
     * @return the previous GIFT version's lesson material reference schema root generated class.
     */
    public Class<?> getPreviousLessonMaterialRefSchemaRoot(){
        return AbstractSchemaHandler.LESSON_MATERIAL_ROOT;
    }       
    
    /**
     * Perform a 'shallow' schema update. This method uses this util's previous schema root and file
     * to put the serializable into an output stream and then uses the provided schema and root to
     * unmarshall it back into a serializable. This method should ONLY be called if the previous
     * schema and the provided schema are the SAME.
     * 
     * @param object the serializable to update schema version.
     * @param startSchemaRoot the schema root to update from
     * @param startSchemaFile the schema file to update from
     * @param endSchemaRoot the schema root to update to
     * @param endSchemaFile the schema file to update to
     * @return the updated serializable
     * @throws JAXBException Can occur when marshalling or unmarshalling
     * @throws SAXException If a SAX error occurs during parsing
     * @throws IOException if there was a problem closing the output stream
     * @throws FileNotFoundException if there was a problem finding the schema file
     */
    private UnmarshalledFile changeToNextSchemaVersion(Serializable object, Class<?> startSchemaRoot, File startSchemaFile, Class<?> endSchemaRoot, File endSchemaFile)
            throws JAXBException, SAXException, IOException, FileNotFoundException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        AbstractSchemaHandler.writeToFile(object, startSchemaRoot, outputStream, startSchemaFile, true);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        return AbstractSchemaHandler.parseAndValidate(endSchemaRoot, inputStream, endSchemaFile, false);
    }

    /**
     * Convert the previous course schema object to a newer version of the course schema.
     * 
     * @param courseFile - the course file to migrate to a newer version
     * @param showCompletionDialog True if you want to show a dialog when the conversion is done, false otherwise.
     * @param failOnFirstSchemaError - if true than a validation event will cause the parsing of the XML content to fail and throw an exception.
     * If there are no validation events than the XML contents are XML and schema valid.
     * From Java API docs: A validation event indicates that a problem was encountered while validating the incoming XML data during an unmarshal operation, while 
     * performing on-demand validation of the Java content tree, or while marshalling the Java content tree back to XML data.
     * @return the new unmarshalled course
     * @throws IOException if there was a problem reading the file
     * @throws JAXBException Can occur when unmarshalling
     * @throws FileNotFoundException if the file doesn't exist
     * @throws SAXException If a SAX error occurs during parsing. 
     * @throws IllegalArgumentException general issue with the file
     */
    public UnmarshalledFile convertCourse(FileProxy courseFile, boolean showCompletionDialog, boolean failOnFirstSchemaError) throws IOException, JAXBException, FileNotFoundException, SAXException, IllegalArgumentException{
        
        UnmarshalledFile course = parseFile(courseFile, getPreviousCourseSchemaFile(), getPreviousCourseSchemaRoot(), failOnFirstSchemaError);
        
        //the conversion didn't do anything, therefore the schemas are the same
        if(logger.isInfoEnabled()){
            logger.info("The Course conversion process returned the same object therefore the schema's have not changed.");
        }
        
        return convertCourse(course.getUnmarshalled(), showCompletionDialog);
    }

    /**
     * Perform a 'shallow' schema update to the provided next schemas if needed. If the last
     * conversion util has been reached, the latest schemas will be used.
     * 
     * @param object the object to convert
     * @param previousSchemaRoot the previous conversion schema root
     * @param nextSchemaRoot the next conversion schema root
     * @param previousSchemaFile the previous conversion schema file
     * @param nextSchemaFile the next conversion schema file
     * @param latestSchemaRoot the most recent conversion schema root
     * @param latestSchemaFile the most recent conversion schema file
     * @return the provided object that has been converted either to the next schema version or the
     *         latest if there is no next schema.
     * @throws FileNotFoundException - if there was a problem finding the schema file
     * @throws JAXBException - Can occur when marshalling or unmarshalling
     * @throws SAXException - If a SAX error occurs during parsing
     * @throws IOException - if there was a problem closing the output stream
     */
    protected Serializable convertObjectSchemaVersion(Serializable object, Class<?> previousSchemaRoot,
            Class<?> nextSchemaRoot, File previousSchemaFile, File nextSchemaFile, Class<?> latestSchemaRoot,
            File latestSchemaFile) throws FileNotFoundException, JAXBException, SAXException, IOException {

        boolean isPreviousVersion = StringUtils.equals(object.getClass().getName(), previousSchemaRoot.getName());
        boolean reachedLastConversionUtil = nextSchemaRoot == null || nextSchemaFile == null;

        if (isPreviousVersion) {
            Class<?> schemaRoot;
            File schemaFile;
            if (reachedLastConversionUtil) {
                schemaRoot = latestSchemaRoot;
                schemaFile = latestSchemaFile;
            } else {
                schemaRoot = nextSchemaRoot;
                schemaFile = nextSchemaFile;
            }

            UnmarshalledFile file = changeToNextSchemaVersion(object, previousSchemaRoot, previousSchemaFile,
                    schemaRoot, schemaFile);
            return file.getUnmarshalled();
        }

        return object;
    }

    /**
     * Convert the previous course schema object to a newer version of the course schema. This
     * method is used to chain conversion wizards together (e.g. converting version 6 to 8).
     *
     * @param course - the course schema object to migrate to a newer version
     * @param showCompletionDialog True if you want to show a dialog when the conversion is done,
     *        false otherwise.
     * @return the new course
     */
    protected UnmarshalledFile convertCourse(Serializable course, boolean showCompletionDialog) {
        VersionEnum thisVersion = getVersionForConversionWizard(this);
        if (thisVersion == null) {
            String versionClass = VersionEnum.class.getName();
            String wizClass = getClass().getName();
            throw new UnsupportedOperationException("Unable to get a " + versionClass + " for " + wizClass
                    + ". The registeredUtils map likely needs to be updated.");
        }
        VersionEnum nextVersion = thisVersion.getNextVersion();

        try {
            AbstractConversionWizardUtil nextConversionUtil = nextVersion == null ? null
                    : getConversionWizardForVersion(nextVersion.getSchemaVersion());
            Class<?> nextPreviousRoot = nextConversionUtil == null ? null
                    : nextConversionUtil.getPreviousCourseSchemaRoot();
            File nextPreviousFile = nextConversionUtil == null ? null
                    : nextConversionUtil.getPreviousCourseSchemaFile();

            // if the object needs to be updated to the next schema version, do it now
            Serializable upToDate = convertObjectSchemaVersion(course,
                    this.getPreviousCourseSchemaRoot(), nextPreviousRoot, this.getPreviousCourseSchemaFile(),
                    nextPreviousFile, AbstractSchemaHandler.COURSE_ROOT, AbstractSchemaHandler.COURSE_SCHEMA_FILE);

            // reached the latest version
            if (nextConversionUtil == null) {

                // The conversion process has completed.
                if (showCompletionDialog) {
                    showCompletionDialog();
                }

                generated.course.Course courseObj = (generated.course.Course) upToDate;
                courseObj.setVersion(getConvertedVersionNumber());
                return new UnmarshalledFile(courseObj);
            } else {
                // continue along the conversion chain to the next version
                nextConversionUtil.setConversionIssueList(conversionIssueList);
                return nextConversionUtil.convertCourse(upToDate, showCompletionDialog);
            }
        } catch (Exception e) {
            throw new DetailedException(
                    "Unable to convert the course from version '" + thisVersion + "' to version '" + nextVersion + "'.",
                    "The course was not able to be converted because: " + e.getMessage(), e);
        }
    }

    /**
     * Convert the previous training application reference object to a newer version of the training application element schema.
     * 
     * @param trainingAppRefFile - the training application element file to migrate to a newer version
     * @param showCompletionDialog True if you want to show a dialog when the conversion is done, false otherwise.
     * @param failOnFirstSchemaError - if true than a validation event will cause the parsing of the XML content to fail and throw an exception.
     * If there are no validation events than the XML contents are XML and schema valid.
     * From Java API docs: A validation event indicates that a problem was encountered while validating the incoming XML data during an unmarshal operation, while 
     * performing on-demand validation of the Java content tree, or while marshalling the Java content tree back to XML data.
     * @return Object - the new training application reference element
     * @throws IOException if there was a problem reading the file
     * @throws JAXBException Can occur when unmarshalling
     * @throws FileNotFoundException if the file doesn't exist
     * @throws SAXException If a SAX error occurs during parsing. 
     * @throws IllegalArgumentException general issue with the file
     */
    public UnmarshalledFile convertTrainingApplicationRef(FileProxy trainingAppRefFile, boolean showCompletionDialog, boolean failOnFirstSchemaError) throws IOException, JAXBException, FileNotFoundException, SAXException, IllegalArgumentException{
        
        UnmarshalledFile trainingAppRef = parseFile(trainingAppRefFile, getPreviousTrainingAppSchemaFile(), getPreviousTrainingAppSchemaRoot(), failOnFirstSchemaError);
        
        //the conversion didn't do anything, therefore the schemas are the same
        if(logger.isInfoEnabled()){
            logger.info("The Training Application reference conversion process returned the same object therefore the schema's have not changed.");
        }

        return convertTrainingApplicationRef(trainingAppRef.getUnmarshalled(), showCompletionDialog);
    }
    
    /**
     * Convert the previous training application ref schema object to a newer version of the schema.
     * This method is used to chain conversion wizards together (e.g. converting version 6 to 8).
     * 
     * @param trainingAppRef - the training application ref schema object to migrate to a newer
     *        version
     * @param showCompletionDialog True if you want to show a dialog when the conversion is done,
     *        false otherwise.
     * @return the new training application ref
     */
    protected UnmarshalledFile convertTrainingApplicationRef(Serializable trainingAppRef,
            boolean showCompletionDialog) {
        VersionEnum thisVersion = getVersionForConversionWizard(this);
        VersionEnum nextVersion = thisVersion.getNextVersion();

        try {
            AbstractConversionWizardUtil nextConversionUtil = nextVersion == null ? null
                    : getConversionWizardForVersion(nextVersion.getSchemaVersion());
            Class<?> nextPreviousRoot = nextConversionUtil == null ? null
                    : nextConversionUtil.getPreviousTrainingAppSchemaRoot();
            File nextPreviousFile = nextConversionUtil == null ? null
                    : nextConversionUtil.getPreviousTrainingAppSchemaFile();

            // if the object needs to be updated to the next schema version, do it now
            Serializable upToDate = convertObjectSchemaVersion(trainingAppRef, this.getPreviousTrainingAppSchemaRoot(),
                    nextPreviousRoot, this.getPreviousTrainingAppSchemaFile(), nextPreviousFile,
                    AbstractSchemaHandler.TRAINING_APP_ELEMENT_ROOT,
                    AbstractSchemaHandler.TRAINING_APP_ELEMENT_SCHEMA_FILE);

            // reached the latest version
            if (nextConversionUtil == null) {

                // The conversion process has completed.
                if (showCompletionDialog) {
                    showCompletionDialog();
                }

                generated.course.TrainingApplicationWrapper trainingAppRefObj = (generated.course.TrainingApplicationWrapper) upToDate;
                trainingAppRefObj.setVersion(getConvertedVersionNumber());
                return new UnmarshalledFile(trainingAppRefObj);
            } else {
                // continue along the conversion chain to the next version
                nextConversionUtil.setConversionIssueList(conversionIssueList);
                return nextConversionUtil.convertTrainingApplicationRef(upToDate, showCompletionDialog);
            }
        } catch (Exception e) {
            throw new DetailedException(
                    "Unable to convert the training application ref from version '" + thisVersion + "' to version '"
                            + nextVersion + "'.",
                    "The training application ref was not able to be converted because: " + e.getMessage(), e);
        }
    }

    /**
     * Convert the previous conversation tree object to a newer version of the conversation element schema.
     * 
     * @param conversationTreeFile - the conversation tree element file to migrate to a newer version
     * @param showCompletionDialog True if you want to show a dialog when the conversion is done, false otherwise.
     * @param failOnFirstSchemaError - if true than a validation event will cause the parsing of the XML content to fail and throw an exception.
     * If there are no validation events than the XML contents are XML and schema valid.
     * From Java API docs: A validation event indicates that a problem was encountered while validating the incoming XML data during an unmarshal operation, while 
     * performing on-demand validation of the Java content tree, or while marshalling the Java content tree back to XML data.
     * @return Object - the new conversation tree element
     * @throws IOException if there was a problem reading the file
     * @throws JAXBException Can occur when unmarshalling
     * @throws FileNotFoundException if the file doesn't exist
     * @throws SAXException If a SAX error occurs during parsing. 
     * @throws IllegalArgumentException general issue with the file
     */
    public UnmarshalledFile convertConversation(FileProxy conversationTreeFile, boolean showCompletionDialog, boolean failOnFirstSchemaError) throws IOException, JAXBException, FileNotFoundException, SAXException, IllegalArgumentException{
        
        UnmarshalledFile conversation = parseFile(conversationTreeFile, getPreviousConversationSchemaFile(), getPreviousConversationSchemaRoot(), failOnFirstSchemaError);
        
        //the conversion didn't do anything, therefore the schemas are the same
        if(logger.isInfoEnabled()){
            logger.info("The conversation tree conversion process returned the same object therefore the schema's have not changed.");
        }
     
        return convertConversation(conversation.getUnmarshalled(), showCompletionDialog);
    }
    
    /**
     * Convert the conversation schema object to a newer version of the schema. This method is used
     * to chain conversion wizards together (e.g. converting version 6 to 8).
     * 
     * @param conversation - the conversation schema object to migrate to a newer version
     * @param showCompletionDialog True if you want to show a dialog when the conversion is done,
     *        false otherwise.
     * @return the new conversation
     */
    protected UnmarshalledFile convertConversation(Serializable conversation, boolean showCompletionDialog) {
        VersionEnum thisVersion = getVersionForConversionWizard(this);
        VersionEnum nextVersion = thisVersion.getNextVersion();

        try {
            AbstractConversionWizardUtil nextConversionUtil = nextVersion == null ? null
                    : getConversionWizardForVersion(nextVersion.getSchemaVersion());
            Class<?> nextPreviousRoot = nextConversionUtil == null ? null
                    : nextConversionUtil.getPreviousConversationSchemaRoot();
            File nextPreviousFile = nextConversionUtil == null ? null
                    : nextConversionUtil.getPreviousConversationSchemaFile();

            // if the object needs to be updated to the next schema version, do it now
            Serializable upToDate = convertObjectSchemaVersion(conversation, this.getPreviousConversationSchemaRoot(),
                    nextPreviousRoot, this.getPreviousConversationSchemaFile(), nextPreviousFile,
                    AbstractSchemaHandler.CONVERSATION_TREE_ELEMENT_ROOT,
                    AbstractSchemaHandler.CONVERSATION_TREE_SCHEMA_FILE);

            // reached the latest version
            if (nextConversionUtil == null) {

                // The conversion process has completed.
                if (showCompletionDialog) {
                    showCompletionDialog();
                }

                generated.conversation.Conversation conversationObj = (generated.conversation.Conversation) upToDate;
                conversationObj.setVersion(getConvertedVersionNumber());
                return new UnmarshalledFile(conversationObj);
            } else {
                // continue along the conversion chain to the next version
                nextConversionUtil.setConversionIssueList(conversionIssueList);
                return nextConversionUtil.convertConversation(upToDate, showCompletionDialog);
            }
        } catch (Exception e) {
            throw new DetailedException("Unable to convert the conversation from version '" + thisVersion
                    + "' to version '" + nextVersion + "'.",
                    "The conversation was not able to be converted because: " + e.getMessage(), e);
        }
    }
    
    /**
     * Convert the previous lesson material reference object to a newer version of the conversation element schema.
     * 
     * @param lessonMaterialRefFile - the lesson material reference element file to migrate to a newer version
     * @param showCompletionDialog True if you want to show a dialog when the conversion is done, false otherwise.
     * @param failOnFirstSchemaError - if true than a validation event will cause the parsing of the XML content to fail and throw an exception.
     * If there are no validation events than the XML contents are XML and schema valid.
     * From Java API docs: A validation event indicates that a problem was encountered while validating the incoming XML data during an unmarshal operation, while 
     * performing on-demand validation of the Java content tree, or while marshalling the Java content tree back to XML data.
     * @return Object - the new lesson material reference element
     * @throws IOException if there was a problem reading the file
     * @throws JAXBException Can occur when unmarshalling
     * @throws FileNotFoundException if the file doesn't exist
     * @throws SAXException If a SAX error occurs during parsing. 
     * @throws IllegalArgumentException general issue with the file
     */
    public UnmarshalledFile convertLessonMaterialRef(FileProxy lessonMaterialRefFile, boolean showCompletionDialog, boolean failOnFirstSchemaError) throws IOException, JAXBException, FileNotFoundException, SAXException, IllegalArgumentException{
        
        UnmarshalledFile lessonMaterialRef = parseFile(lessonMaterialRefFile, getPreviousLessonMaterialRefSchemaFile(), getPreviousLessonMaterialRefSchemaRoot(), failOnFirstSchemaError);
        
        //the conversion didn't do anything, therefore the schemas are the same
        if(logger.isInfoEnabled()){
            logger.info("The lesson material reference conversion process returned the same object therefore the schema's have not changed.");
        }
        
        return convertLessonMaterialRef(lessonMaterialRef.getUnmarshalled(), showCompletionDialog);
    }
    
    /**
     * Convert the lesson material ref schema object to a newer version of the schema. This method
     * is used to chain conversion wizards together (e.g. converting version 6 to 8).
     * 
     * @param lessonMaterialRef - the lesson material ref schema object to migrate to a newer
     *        version
     * @param showCompletionDialog True if you want to show a dialog when the conversion is done,
     *        false otherwise.
     * @return the new lesson material ref
     */
    protected UnmarshalledFile convertLessonMaterialRef(Serializable lessonMaterialRef, boolean showCompletionDialog) {
        VersionEnum thisVersion = getVersionForConversionWizard(this);
        VersionEnum nextVersion = thisVersion.getNextVersion();

        try {
            AbstractConversionWizardUtil nextConversionUtil = nextVersion == null ? null
                    : getConversionWizardForVersion(nextVersion.getSchemaVersion());
            Class<?> nextPreviousRoot = nextConversionUtil == null ? null
                    : nextConversionUtil.getPreviousLessonMaterialRefSchemaRoot();
            File nextPreviousFile = nextConversionUtil == null ? null
                    : nextConversionUtil.getPreviousLessonMaterialRefSchemaFile();

            // if the object needs to be updated to the next schema version, do it now
            Serializable upToDate = convertObjectSchemaVersion(lessonMaterialRef,
                    this.getPreviousLessonMaterialRefSchemaRoot(), nextPreviousRoot,
                    this.getPreviousLessonMaterialRefSchemaFile(), nextPreviousFile,
                    AbstractSchemaHandler.LESSON_MATERIAL_ROOT, AbstractSchemaHandler.LESSON_MATERIAL_SCHEMA_FILE);

            // reached the latest version
            if (nextConversionUtil == null) {

                // The conversion process has completed.
                if (showCompletionDialog) {
                    showCompletionDialog();
                }

                return new UnmarshalledFile(upToDate);
            } else {
                // continue along the conversion chain to the next version
                nextConversionUtil.setConversionIssueList(conversionIssueList);
                return nextConversionUtil.convertLessonMaterialRef(upToDate, showCompletionDialog);
            }
        } catch (Exception e) {
            throw new DetailedException(
                    "Unable to convert the lesson material from version '" + thisVersion + "' to version '"
                            + nextVersion + "'.",
                    "The lesson material was not able to be converted because: " + e.getMessage(), e);
        }
    }
    
    /**
     * Convert the previous scenario schema object to a newer version of the scenario schema.
     * 
     * @param dkf - the dkf to migrate to a newer version
     * @param showCompletionDialog True if you want to show a dialog when the conversion is done, false otherwise.
     * @param failOnFirstSchemaError - if true than a validation event will cause the parsing of the XML content to fail and throw an exception.
     * If there are no validation events than the XML contents are XML and schema valid.
     * From Java API docs: A validation event indicates that a problem was encountered while validating the incoming XML data during an unmarshal operation, while 
     * performing on-demand validation of the Java content tree, or while marshalling the Java content tree back to XML data.
     * @return the new unmarshalled scenario
     * @throws IOException if there was a problem reading the file
     * @throws JAXBException Can occur when unmarshalling
     * @throws FileNotFoundException if the file doesn't exist
     * @throws SAXException If a SAX error occurs during parsing. 
     * @throws IllegalArgumentException general issue with the file
     */
    public UnmarshalledFile convertScenario(FileProxy dkf, boolean showCompletionDialog, boolean failOnFirstSchemaError) throws IOException, JAXBException, FileNotFoundException, SAXException, IllegalArgumentException{
        
        UnmarshalledFile scenario = parseFile(dkf, getPreviousDKFSchemaFile(), getPreviousDKFSchemaRoot(), failOnFirstSchemaError);
        
        //the conversion didn't do anything, therefore the schemas are the same
        if(logger.isInfoEnabled()){
            logger.info("The DKF conversion process returned the same object therefore the schema's have not changed.");
        }
        
        return convertScenario(scenario.getUnmarshalled(), showCompletionDialog);
    }
    
    /**
     * Convert the scenario schema object to a newer version of the schema. This method is used to
     * chain conversion wizards together (e.g. converting version 6 to 8).
     * 
     * @param scenario - the scenario schema object to migrate to a newer version
     * @param showCompletionDialog True if you want to show a dialog when the conversion is done,
     *        false otherwise.
     * @return the new scenario
     */
    protected UnmarshalledFile convertScenario(Serializable scenario, boolean showCompletionDialog) {
        VersionEnum thisVersion = getVersionForConversionWizard(this);
        VersionEnum nextVersion = thisVersion.getNextVersion();

        try {
            AbstractConversionWizardUtil nextConversionUtil = nextVersion == null ? null
                    : getConversionWizardForVersion(nextVersion.getSchemaVersion());
            Class<?> nextPreviousRoot = nextConversionUtil == null ? null
                    : nextConversionUtil.getPreviousDKFSchemaRoot();
            File nextPreviousFile = nextConversionUtil == null ? null : nextConversionUtil.getPreviousDKFSchemaFile();

            // if the object needs to be updated to the next schema version, do it now
            Serializable upToDate = convertObjectSchemaVersion(scenario, this.getPreviousDKFSchemaRoot(),
                    nextPreviousRoot, this.getPreviousDKFSchemaFile(), nextPreviousFile, AbstractSchemaHandler.DKF_ROOT,
                    AbstractSchemaHandler.DKF_SCHEMA_FILE);

            // reached the latest version
            if (nextConversionUtil == null) {

                // The conversion process has completed.
                if (showCompletionDialog) {
                    showCompletionDialog();
                }

                generated.dkf.Scenario scenarioObj = (generated.dkf.Scenario) upToDate;
                scenarioObj.setVersion(getConvertedVersionNumber());
                return new UnmarshalledFile(scenarioObj);
            } else {
                // continue along the conversion chain to the next version
                nextConversionUtil.setConversionIssueList(conversionIssueList);
                return nextConversionUtil.convertScenario(upToDate, showCompletionDialog);
            }
        } catch (Exception e) {
            throw new DetailedException("Unable to convert the scenario from version '" + thisVersion + "' to version '"
                    + nextVersion + "'.", "The scenario was not able to be converted because: " + e.getMessage(), e);
        }
    }

    /**
     * Convert the previous metadata schema object to a newer version of the metadata schema.
     * 
     * @param metadataFile - the metadata file to migrate to a newer version
     * @param showCompletionDialog True if you want to show a dialog when the conversion is done, false otherwise.
     * @param failOnFirstSchemaError - if true than a validation event will cause the parsing of the XML content to fail and throw an exception.
     * If there are no validation events than the XML contents are XML and schema valid.
     * From Java API docs: A validation event indicates that a problem was encountered while validating the incoming XML data during an unmarshal operation, while 
     * performing on-demand validation of the Java content tree, or while marshalling the Java content tree back to XML data.
     * @return Object - the new metadata
     * @throws IOException if there was a problem reading the file
     * @throws JAXBException Can occur when unmarshalling
     * @throws FileNotFoundException if the file doesn't exist
     * @throws SAXException If a SAX error occurs during parsing. 
     * @throws IllegalArgumentException general issue with the file
     */
    public UnmarshalledFile convertMetadata(FileProxy metadataFile, boolean showCompletionDialog, boolean failOnFirstSchemaError) throws IOException, JAXBException, FileNotFoundException, SAXException, IllegalArgumentException{
        
        UnmarshalledFile metadata = parseFile(metadataFile, getPreviousMetadataSchemaFile(), getPreviousMetadataSchemaRoot(), failOnFirstSchemaError);
        
        //the conversion didn't do anything, therefore the schemas are the same
        if(logger.isInfoEnabled()){
            logger.info("The Metadata conversion process returned the same object therefore the schema's have not changed.");
        }

        return convertMetadata(metadata.getUnmarshalled(), showCompletionDialog);
    }
    
    /**
     * Convert the metadata schema object to a newer version of the schema. This method is used to
     * chain conversion wizards together (e.g. converting version 6 to 8).
     * 
     * @param metadata - the metadata schema object to migrate to a newer version
     * @param showCompletionDialog True if you want to show a dialog when the conversion is done,
     *        false otherwise.
     * @return the new metadata
     */
    protected UnmarshalledFile convertMetadata(Serializable metadata, boolean showCompletionDialog) {
        VersionEnum thisVersion = getVersionForConversionWizard(this);
        VersionEnum nextVersion = thisVersion.getNextVersion();

        try {
            AbstractConversionWizardUtil nextConversionUtil = nextVersion == null ? null
                    : getConversionWizardForVersion(nextVersion.getSchemaVersion());
            Class<?> nextPreviousRoot = nextConversionUtil == null ? null
                    : nextConversionUtil.getPreviousMetadataSchemaRoot();
            File nextPreviousFile = nextConversionUtil == null ? null
                    : nextConversionUtil.getPreviousMetadataSchemaFile();

            // if the object needs to be updated to the next schema version, do it now
            Serializable upToDate = convertObjectSchemaVersion(metadata, this.getPreviousMetadataSchemaRoot(),
                    nextPreviousRoot, this.getPreviousMetadataSchemaFile(), nextPreviousFile,
                    AbstractSchemaHandler.METADATA_ROOT, AbstractSchemaHandler.METADATA_SCHEMA_FILE);

            // reached the latest version
            if (nextConversionUtil == null) {

                // The conversion process has completed.
                if (showCompletionDialog) {
                    showCompletionDialog();
                }

                generated.metadata.Metadata metadataObj = (generated.metadata.Metadata) upToDate;
                metadataObj.setVersion(getConvertedVersionNumber());
                return new UnmarshalledFile(metadataObj);
            } else {
                // continue along the conversion chain to the next version
                nextConversionUtil.setConversionIssueList(conversionIssueList);
                return nextConversionUtil.convertMetadata(upToDate, showCompletionDialog);
            }
        } catch (Exception e) {
            throw new DetailedException("Unable to convert the metadata from version '" + thisVersion + "' to version '"
                    + nextVersion + "'.", "The metadata was not able to be converted because: " + e.getMessage(), e);
        }
    }
    
    /**
     * Convert the previous learner config schema object to a newer version of the learner config schema.
     * 
     * @param learnerConfigurationFile - the learner config file to migrate to a newer version
     * @param showCompletionDialog True if you want to show a dialog when the conversion is done, false otherwise.
     * @param failOnFirstSchemaError - if true than a validation event will cause the parsing of the XML content to fail and throw an exception.
     * If there are no validation events than the XML contents are XML and schema valid.
     * From Java API docs: A validation event indicates that a problem was encountered while validating the incoming XML data during an unmarshal operation, while 
     * performing on-demand validation of the Java content tree, or while marshalling the Java content tree back to XML data.
     * @return Object - the new learner configuration
     * @throws IOException if there was a problem reading the file
     * @throws JAXBException Can occur when unmarshalling
     * @throws FileNotFoundException if the file doesn't exist
     * @throws SAXException If a SAX error occurs during parsing. 
     * @throws IllegalArgumentException general issue with the file
     */
    public UnmarshalledFile convertLearnerConfiguration(FileProxy learnerConfigurationFile, boolean showCompletionDialog, boolean failOnFirstSchemaError) throws IOException, JAXBException, FileNotFoundException, SAXException, IllegalArgumentException{
        
        UnmarshalledFile learnerConfiguration = parseFile(learnerConfigurationFile, getPreviousLearnerSchemaFile(), getPreviousLearnerSchemaRoot(), failOnFirstSchemaError);
        
        //the conversion didn't do anything, therefore the schemas are the same
        if(logger.isInfoEnabled()){
            logger.info("The Learner Configuration conversion process returned the same object therefore the schema's have not changed.");
        }

        return convertLearnerConfiguration(learnerConfiguration.getUnmarshalled(), showCompletionDialog);
    }
    
    /**
     * Convert the learner configuration schema object to a newer version of the schema. This method
     * is used to chain conversion wizards together (e.g. converting version 6 to 8).
     * 
     * @param learnerConfiguration - the learner configuration schema object to migrate to a newer
     *        version
     * @param showCompletionDialog True if you want to show a dialog when the conversion is done,
     *        false otherwise.
     * @return the new learner configuration
     */
    protected UnmarshalledFile convertLearnerConfiguration(Serializable learnerConfiguration,
            boolean showCompletionDialog) {
        VersionEnum thisVersion = getVersionForConversionWizard(this);
        VersionEnum nextVersion = thisVersion.getNextVersion();

        try {
            AbstractConversionWizardUtil nextConversionUtil = nextVersion == null ? null
                    : getConversionWizardForVersion(nextVersion.getSchemaVersion());
            Class<?> nextPreviousRoot = nextConversionUtil == null ? null
                    : nextConversionUtil.getPreviousLearnerSchemaRoot();
            File nextPreviousFile = nextConversionUtil == null ? null
                    : nextConversionUtil.getPreviousLearnerSchemaFile();

            // if the object needs to be updated to the next schema version, do it now
            Serializable upToDate = convertObjectSchemaVersion(learnerConfiguration,
                    this.getPreviousLearnerSchemaRoot(), nextPreviousRoot, this.getPreviousLearnerSchemaFile(),
                    nextPreviousFile, AbstractSchemaHandler.LEARNER_ROOT, AbstractSchemaHandler.LEARNER_SCHEMA_FILE);

            // reached the latest version
            if (nextConversionUtil == null) {

                // The conversion process has completed.
                if (showCompletionDialog) {
                    showCompletionDialog();
                }

                generated.learner.LearnerConfiguration learnerConfigurationObj = (generated.learner.LearnerConfiguration) upToDate;
                learnerConfigurationObj.setVersion(getConvertedVersionNumber());
                return new UnmarshalledFile(learnerConfigurationObj);
            } else {
                // continue along the conversion chain to the next version
                nextConversionUtil.setConversionIssueList(conversionIssueList);
                return nextConversionUtil.convertLearnerConfiguration(upToDate, showCompletionDialog);
            }
        } catch (Exception e) {
            throw new DetailedException("Unable to convert the learner config from version '" + thisVersion
                    + "' to version '" + nextVersion + "'.",
                    "The learner config was not able to be converted because: " + e.getMessage(), e);
        }
    }

    /**
     * Convert the previous EMAP pedagogical config schema object to a newer version of the pedagogical config schema.
     * 
     * @param emapConfigurationFile - the pedagogical config file to migrate to a newer version
     * @param showCompletionDialog True if you want to show a dialog when the conversion is done, false otherwise.
     * @param failOnFirstSchemaError - if true than a validation event will cause the parsing of the XML content to fail and throw an exception.
     * If there are no validation events than the XML contents are XML and schema valid.
     * From Java API docs: A validation event indicates that a problem was encountered while validating the incoming XML data during an unmarshal operation, while 
     * performing on-demand validation of the Java content tree, or while marshalling the Java content tree back to XML data.
     * @return Object - the new EMAP pedagogical configuration
     * @throws IOException if there was a problem reading the file
     * @throws JAXBException Can occur when unmarshalling
     * @throws FileNotFoundException if the file doesn't exist
     * @throws SAXException If a SAX error occurs during parsing. 
     * @throws IllegalArgumentException general issue with the file
     */
    public UnmarshalledFile convertEMAPConfiguration(FileProxy emapConfigurationFile, boolean showCompletionDialog, boolean failOnFirstSchemaError) throws IOException, JAXBException, FileNotFoundException, SAXException, IllegalArgumentException{
        
        UnmarshalledFile pedagogicalConfiguration = parseFile(emapConfigurationFile, getPreviousEMAPConfigSchemaFile(), getPreviousEMAPConfigSchemaRoot(), failOnFirstSchemaError);
        
        //the conversion didn't do anything, therefore the schemas are the same
        if(logger.isInfoEnabled()){
            logger.info("The EMAP Pedagogical Configuration conversion process returned the same object therefore the schema's have not changed.");
        }

        return convertEMAPConfiguration(pedagogicalConfiguration.getUnmarshalled(), showCompletionDialog);
    }
    
    /**
     * Convert the previous ICAP Policy schema object to a newer version of the pedagogical config schema.
     * 
     * @param icapPolicyFile - the ICAP policy file to migrate to a newer version
     * @param showCompletionDialog True if you want to show a dialog when the conversion is done, false otherwise.
     * @param failOnFirstSchemaError - if true than a validation event will cause the parsing of the XML content to fail and throw an exception.
     * If there are no validation events than the XML contents are XML and schema valid.
     * From Java API docs: A validation event indicates that a problem was encountered while validating the incoming XML data during an unmarshal operation, while 
     * performing on-demand validation of the Java content tree, or while marshalling the Java content tree back to XML data.
     * @return Object - the new EMAP pedagogical configuration
     * @throws IOException if there was a problem reading the file
     * @throws JAXBException Can occur when unmarshalling
     * @throws FileNotFoundException if the file doesn't exist
     * @throws SAXException If a SAX error occurs during parsing. 
     * @throws IllegalArgumentException general issue with the file
     */
    public UnmarshalledFile convertICAPPolicy(FileProxy icapPolicyFile, boolean showCompletionDialog, boolean failOnFirstSchemaError) throws IOException, JAXBException, FileNotFoundException, SAXException, IllegalArgumentException{
        
        UnmarshalledFile icapPolicy = parseFile(icapPolicyFile, getPreviousICAPPolicySchemaFile(), getPreviousICAPPolicySchemaRoot(), failOnFirstSchemaError);
        
        //the conversion didn't do anything, therefore the schemas are the same
        if(logger.isInfoEnabled()){
            logger.info("The ICAP Policy conversion process returned the same object therefore the schema's have not changed.");
        }

        return convertICAPPolicy(icapPolicy.getUnmarshalled(), showCompletionDialog);
    }

    /**
     * Convert the EMAP pedagogical configuration schema object to a newer version of the schema. This
     * method is used to chain conversion wizards together (e.g. converting version 6 to 8).
     * 
     * @param emapConfiguration - the EMAP pedagogical configuration schema object to migrate to a
     *        newer version
     * @param showCompletionDialog True if you want to show a dialog when the conversion is done,
     *        false otherwise.
     * @return the new EMAP pedagogical configuration
     */
    protected UnmarshalledFile convertEMAPConfiguration(Serializable emapConfiguration,
            boolean showCompletionDialog) {
        VersionEnum thisVersion = getVersionForConversionWizard(this);
        VersionEnum nextVersion = thisVersion.getNextVersion();

        try {
            AbstractConversionWizardUtil nextConversionUtil = nextVersion == null ? null
                    : getConversionWizardForVersion(nextVersion.getSchemaVersion());
            Class<?> nextPreviousRoot = nextConversionUtil == null ? null
                    : nextConversionUtil.getPreviousEMAPConfigSchemaRoot();
            File nextPreviousFile = nextConversionUtil == null ? null
                    : nextConversionUtil.getPreviousEMAPConfigSchemaFile();

            // if the object needs to be updated to the next schema version, do it now
            Serializable upToDate = convertObjectSchemaVersion(emapConfiguration,
                    this.getPreviousEMAPConfigSchemaRoot(), nextPreviousRoot, this.getPreviousEMAPConfigSchemaFile(),
                    nextPreviousFile, AbstractSchemaHandler.EMAP_PEDAGOGICAL_ROOT,
                    AbstractSchemaHandler.EMAP_PEDAGOGICAL_SCHEMA_FILE);

            // reached the latest version
            if (nextConversionUtil == null) {

                // The conversion process has completed.
                if (showCompletionDialog) {
                    showCompletionDialog();
                }

                generated.ped.EMAP pedConfigObj = (generated.ped.EMAP) upToDate;
                pedConfigObj.setVersion(getConvertedVersionNumber());
                return new UnmarshalledFile(pedConfigObj);
            } else {
                // continue along the conversion chain to the next version
                nextConversionUtil.setConversionIssueList(conversionIssueList);
                return nextConversionUtil.convertEMAPConfiguration(upToDate, showCompletionDialog);
            }
        } catch (Exception e) {
            throw new DetailedException(
                    "Unable to convert the EMAP pedagogical config from version '" + thisVersion + "' to version '"
                            + nextVersion + "'.",
                    "The EMAP pedagogical config was not able to be converted because: " + e.getMessage(), e);
        }
    }
    
    /**
     * Convert the ICAP policy schema object to a newer version of the schema. This
     * method is used to chain conversion wizards together (e.g. converting version 6 to 8).
     * 
     * @param icapPolicy - the pedagogical configuration schema object to migrate to a
     *        newer version
     * @param showCompletionDialog True if you want to show a dialog when the conversion is done,
     *        false otherwise.
     * @return the new ICAP policy
     */
    protected UnmarshalledFile convertICAPPolicy(Serializable icapPolicy,
            boolean showCompletionDialog) {
        VersionEnum thisVersion = getVersionForConversionWizard(this);
        VersionEnum nextVersion = thisVersion.getNextVersion();

        try {
            AbstractConversionWizardUtil nextConversionUtil = nextVersion == null ? null
                    : getConversionWizardForVersion(nextVersion.getSchemaVersion());
            Class<?> nextPreviousRoot = nextConversionUtil == null ? null
                    : nextConversionUtil.getPreviousICAPPolicySchemaRoot();
            File nextPreviousFile = nextConversionUtil == null ? null
                    : nextConversionUtil.getPreviousICAPPolicySchemaFile();

            // if the object needs to be updated to the next schema version, do it now
            Serializable upToDate = convertObjectSchemaVersion(icapPolicy,
                    this.getPreviousICAPPolicySchemaRoot(), nextPreviousRoot, this.getPreviousICAPPolicySchemaFile(),
                    nextPreviousFile, AbstractSchemaHandler.ICAP_POLICY_ROOT,
                    AbstractSchemaHandler.ICAP_POLICY_SCHEMA_FILE);

            // reached the latest version
            if (nextConversionUtil == null) {

                // The conversion process has completed.
                if (showCompletionDialog) {
                    showCompletionDialog();
                }

                generated.ped.ICAPPolicy icapPolicyObj = (generated.ped.ICAPPolicy) upToDate;
                icapPolicyObj.setVersion(getConvertedVersionNumber());
                return new UnmarshalledFile(icapPolicyObj);
            } else {
                // continue along the conversion chain to the next version
                nextConversionUtil.setConversionIssueList(conversionIssueList);
                return nextConversionUtil.convertICAPPolicy(upToDate, showCompletionDialog);
            }
        } catch (Exception e) {
            throw new DetailedException(
                    "Unable to convert the ICAP Policy from version '" + thisVersion + "' to version '"
                            + nextVersion + "'.",
                    "The ICAP Policy was not able to be converted because: " + e.getMessage(), e);
        }
    }

    /**
     * Convert the previous sensor config schema object to a newer version of the sensor config schema.
     * 
     * @param sensorConfigurationFile - the sensor config file to migrate to a newer version
     * @param showCompletionDialog True if you want to show a dialog when the conversion is done, false otherwise.
     * @param failOnFirstSchemaError - if true than a validation event will cause the parsing of the XML content to fail and throw an exception.
     * If there are no validation events than the XML contents are XML and schema valid.
     * From Java API docs: A validation event indicates that a problem was encountered while validating the incoming XML data during an unmarshal operation, while 
     * performing on-demand validation of the Java content tree, or while marshalling the Java content tree back to XML data.
     * @return Object - the new sensors configuration
     * @throws IOException if there was a problem reading the file
     * @throws JAXBException Can occur when unmarshalling
     * @throws FileNotFoundException if the file doesn't exist
     * @throws SAXException If a SAX error occurs during parsing. 
     * @throws IllegalArgumentException general issue with the file
     */
    public UnmarshalledFile convertSensorConfiguration(FileProxy sensorConfigurationFile, boolean showCompletionDialog, boolean failOnFirstSchemaError) throws IOException, JAXBException, FileNotFoundException, SAXException, IllegalArgumentException{
        
        UnmarshalledFile sensorConfiguration = parseFile(sensorConfigurationFile, getPreviousSensorSchemaFile(), getPreviousSensorSchemaRoot(), failOnFirstSchemaError);
        
        //the conversion didn't do anything, therefore the schemas are the same
        if(logger.isInfoEnabled()){
            logger.info("The Sensor Configuration conversion process returned the same object therefore the schema's have not changed.");
        }

        return convertSensorConfiguration(sensorConfiguration.getUnmarshalled(), showCompletionDialog);
    }

    /**
     * Convert the sensor configuration schema object to a newer version of the schema. This method
     * is used to chain conversion wizards together (e.g. converting version 6 to 8).
     * 
     * @param sensorConfiguration - the sensor configuration schema object to migrate to a newer
     *        version
     * @param showCompletionDialog True if you want to show a dialog when the conversion is done,
     *        false otherwise.
     * @return the new sensor configuration
     */
    protected UnmarshalledFile convertSensorConfiguration(Serializable sensorConfiguration,
            boolean showCompletionDialog) {
        VersionEnum thisVersion = getVersionForConversionWizard(this);
        VersionEnum nextVersion = thisVersion.getNextVersion();

        try {
            AbstractConversionWizardUtil nextConversionUtil = nextVersion == null ? null
                    : getConversionWizardForVersion(nextVersion.getSchemaVersion());
            Class<?> nextPreviousRoot = nextConversionUtil == null ? null
                    : nextConversionUtil.getPreviousSensorSchemaRoot();
            File nextPreviousFile = nextConversionUtil == null ? null
                    : nextConversionUtil.getPreviousSensorSchemaFile();

            // if the object needs to be updated to the next schema version, do it now
            Serializable upToDate = convertObjectSchemaVersion(sensorConfiguration, this.getPreviousSensorSchemaRoot(),
                    nextPreviousRoot, this.getPreviousSensorSchemaFile(), nextPreviousFile,
                    AbstractSchemaHandler.SENSOR_ROOT, AbstractSchemaHandler.SENSOR_SCHEMA_FILE);

            // reached the latest version
            if (nextConversionUtil == null) {

                // The conversion process has completed.
                if (showCompletionDialog) {
                    showCompletionDialog();
                }

                generated.sensor.SensorsConfiguration sensorConfigObj = (generated.sensor.SensorsConfiguration) upToDate;
                sensorConfigObj.setVersion(getConvertedVersionNumber());
                return new UnmarshalledFile(sensorConfigObj);
            } else {
                // continue along the conversion chain to the next version
                nextConversionUtil.setConversionIssueList(conversionIssueList);
                return nextConversionUtil.convertSensorConfiguration(upToDate, showCompletionDialog);
            }
        } catch (Exception e) {
            throw new DetailedException("Unable to convert the sensor config from version '" + thisVersion
                    + "' to version '" + nextVersion + "'.",
                    "The sensor config was not able to be converted because: " + e.getMessage(), e);
        }
    }

    /**
     * Parse the given file against the specified schema into a new instance of the root class.
     * 
     * @param file the XML file to parse (e.g. a course xml file)
     * @param previousVersionSchemaFile the previous GIFT version schema file to parse the provided file against
     * @param previousVersionSchemaRoot the previous GIFT version schema root generated class to create a new instance of
     * @param failOnFirstSchemaError - if true than a validation event will cause the parsing of the XML content to fail and throw an exception.
     * If there are no validation events than the XML contents are XML and schema valid.
     * From Java API docs: A validation event indicates that a problem was encountered while validating the incoming XML data during an unmarshal operation, while 
     * performing on-demand validation of the Java content tree, or while marshalling the Java content tree back to XML data.
     * @return Object the new instance of the root generated class
     * @throws IOException if there was a problem reading the file
     * @throws JAXBException Can occur when unmarshalling
     * @throws FileNotFoundException if the file doesn't exist
     * @throws SAXException If a SAX error occurs during parsing. 
     * @throws IllegalArgumentException general issue with the file
     */
    protected UnmarshalledFile parseFile(FileProxy file, File previousVersionSchemaFile, Class<?> previousVersionSchemaRoot, boolean failOnFirstSchemaError) throws IOException, JAXBException, FileNotFoundException, SAXException, IllegalArgumentException{
     
        //validate file against old schema to make sure the file is the appropriate version
        AbstractSchemaHandler handler = new AbstractSchemaHandler(previousVersionSchemaFile) { };
        UnmarshalledFile unmarshalledFile = handler.parseAndValidate(previousVersionSchemaRoot, file.getInputStream(), failOnFirstSchemaError);
        
        return unmarshalledFile;
    }

    /**
     * Create a fully populate instance of the given generated class.  To populate means that every
     * element in the schema for this generated class will have a value.  In most cases the default values
     * will be used (i.e. primitive types [check createPrimitiveType method], collections size = 0).  
     * 
     * Every choice at a choice element should be chosen as well (not fully implemented yet see $TODO's in this method).
     * 
     * @param generatedClassRoot the generated class to create a fully populated instance of
     * @param parentNode container for the generated nodes of the XML tree reflected by the generated class instances
     * @return Object an instance of the generated class provided with all its attributes and choices populated.
     * @throws Exception if there was a severe error in creating an instance of the generated class
     */
    protected static Object createFullInstance(Class<?> generatedClassRoot, Node parentNode) throws Exception{
        
        Object obj = generatedClassRoot.getDeclaredConstructor().newInstance();
        parentNode.nodeObj = obj;
                
        for(PropertyDescriptor propertyDescriptor : 
            Introspector.getBeanInfo(generatedClassRoot, Object.class).getPropertyDescriptors()){

            // propertyEditor.getReadMethod() exposes the getter
            // btw, this may be null if you have a write-only property
            //System.out.println(propertyDescriptor.getReadMethod());
            
            Method setterMethod = propertyDescriptor.getWriteMethod();
            if(setterMethod == null){
                
                //maybe a list (like Transitions.getTransitionType())
                Method getterMethod = propertyDescriptor.getReadMethod();
                Class<?> returnType = getterMethod.getReturnType();
                if(returnType.isAssignableFrom(List.class)){
                    
                    @SuppressWarnings("unchecked")
                    List<Object> list = (List<Object>) getterMethod.invoke(obj);
                    //add new child to parent
                    Node childNode = new Node();
                    childNode.nodeObj = list;
                    parentNode.children.add(childNode);
                    handleList(generatedClassRoot, propertyDescriptor, list, childNode);                   
                    
                    if(list.isEmpty()){
                        //ERROR
                        throw new Exception("Did not add a single item to the list from the getter method of "+getterMethod.getName()+" for "+obj);
                    }                    
                    
                    System.out.println("Created '"+propertyDescriptor.getName()+"' list with "+list.size()+" items.");
                    
                }else{
                    //ERROR
                    throw new Exception("Unable to handle getter method named "+getterMethod.getName()+" for property descriptor "+propertyDescriptor+" in "+obj);
                }

                continue;
            }
            
            Class<?>[] paramTypes = setterMethod.getParameterTypes();
            if(paramTypes.length != 1){
                //ERROR
                throw new Exception("Unable to handle a multiple parameter setter method named "+setterMethod.getName()+" in "+obj);

            }else{
                
                Class<?> paramType = paramTypes[0];
                Object createdObject = null;
                Node childNode = new Node();
                
                if(paramType.getName().equals(Object.class.getName())){
                    //must be a choice element
                    
                    Field field = generatedClassRoot.getDeclaredField(propertyDescriptor.getName());
                    List<Class<?>> choices = ClassFinderUtil.getChoiceTypes(field);
                    if(choices.isEmpty()){
                        //ERROR
                        throw new Exception("There are no annotated choices for field of "+field);
                    }else{
                        //create the first instance
                        
                        //TODO: create each instance - need to recursively look up the call tree to determine where a new entry can be added
                        int nextChoiceIndex = 0;
                        if(parentNode.choices.containsKey(propertyDescriptor.getName())){
                            for(ChoiceItem item : parentNode.choices.get(propertyDescriptor.getName()).items){
                                
                                choices.remove(item.choiceClazz);
//                                if(choices.contains(item)){
//                                    //found a choice not used yet
//                                    nextChoiceIndex = choices.indexOf(item);
//                                }
                            }
                        }
                        
                        if(choices.isEmpty()){
                            return null;
                        }
                        
                        Class<?> choiceClazz = choices.get(nextChoiceIndex);
                        createdObject = createPrimitiveType(choiceClazz);
                        if(createdObject == null){
                            //need to create object type
                            
                            createdObject = createFullInstance(choiceClazz, childNode);
                            
                            if(createdObject == null){
                                //ERROR
                                throw new Exception("Receive a null object for "+paramType+" in "+obj);
                            } 
                        }
                        
                        setterMethod.invoke(obj, createdObject);
                        
                        System.out.println("Created '"+propertyDescriptor.getName()+"' object of "+createdObject+".");
                        
                        //save choice implemented
                        ChoiceItem item = new ChoiceItem();
                        item.choiceClazz = choiceClazz;
                        if(parentNode.choices.containsKey(propertyDescriptor.getName())){
                            parentNode.choices.get(propertyDescriptor.getName()).items.add(item);
                        }else{
                            ChoiceImplemented choice = new ChoiceImplemented();
                            choice.items.add(item);
                            parentNode.choices.put(propertyDescriptor.getName(), choice);
                        }
                        
                        if(choices.size() > 1){
                            //there is more than 1 choice left
                            parentNode.choices.get(propertyDescriptor.getName()).moreAvailable = true;
                        }

                    }
                    
                }else if(paramType.isEnum()){
                    //Handle enumeration type
                    
                    Object[] constants = paramType.getEnumConstants();
                    
                    if(constants.length < 1){
                        //ERROR
                        throw new Exception("Unable to handle enum type in "+obj+" for setter method named "+setterMethod.getName());
                    }else{
                        setterMethod.invoke(obj, constants[0]);
                        
                        System.out.println("Created '"+propertyDescriptor.getName()+"' object using enum of "+constants[0]);
                    }
                    
                }else{
                    
                    //check for primitive
                    createdObject = createPrimitiveType(paramType);
                    
                    if(createdObject == null){
                        //Handle complex object
                        
                        createdObject = createFullInstance(paramType, childNode);
                                              
                        if(createdObject == null){
                            //ERROR
                            throw new Exception("Receive a null object for "+paramType+" in "+obj);
                        }                        
                    }
                    
                    setterMethod.invoke(obj, createdObject);
                    
                    System.out.println("Created '"+propertyDescriptor.getName()+"' object of "+createdObject);
                }
                
                //add new child to parent
                childNode.nodeObj = createdObject;
                parentNode.children.add(childNode);
                
            }//end else
            

            
        }//end for
        
        return obj;
    } 
    
    
    /**
     * Populate items in a list element.
     * 
     * @param generatedClassRoot - the generated class containing the list attribute/field.
     * @param propertyDescriptor - the field name of the list
     * @param list - the list to populate with new objects.
     * @param parentNode - information about the parent node to this list.
     * @throws Exception if there was a severe error in creating an instance of the generated class
     */
    protected static void handleList(Class<?> generatedClassRoot, PropertyDescriptor propertyDescriptor, List<Object> list, Node parentNode) throws Exception{
        
        //populate list with objects
        Field field = generatedClassRoot.getDeclaredField(propertyDescriptor.getName());
        Type type = ((ParameterizedType)field.getGenericType()).getActualTypeArguments()[0];
        
        //create one of each type that can go in list
        List<Class<?>> types = ClassFinderUtil.getChoiceTypes(field);
        for(int index = 0; index < types.size(); index++){
            Class<?> typeClass = types.get(index);
            Node node = new Node();
            Object listObj = createFullInstance(typeClass, node);
            list.add(listObj);
            
            if(parentNode.areMoreChoices()){
                index--; //repeat                  
            }

            parentNode.children.add(node);
        }
        
        if(list.isEmpty()){
            //create one of the specific type for this
            
            Class<?> typeClass = (Class<?>)type;
            
            if(!typeClass.getName().equals(Object.class.getName())){
                
                Node node = new Node();
                do{
                    
                    
                    //handle primitive types
                    Object listObj = createPrimitiveType(typeClass);
                    if(listObj == null){  
                        //create complex object
                        listObj = createFullInstance(typeClass, node);
                    }
                    
                    if(listObj == null){
                        break;
                    }
                    
                    list.add(listObj);
                    
                    parentNode.children.add(node);
                    
                    if(!parentNode.areMoreChoices()){
                        break;
                    }
                
                }while(true);
            }
        }

    }
    
    /**
     * Determine and return a primitive object for the class specified.  This will provide
     * a non-default value for the object as well.
     * 
     * @param clazz - the class to return an object for.
     * @return Object - an object of the type clazz.
     * @throws Exception if an unknown primitive type is found
     */
    protected static Object createPrimitiveType(Class<?> clazz) throws Exception{
        
        Object obj = null;
        
        if(clazz.getName().equals(Object.class.getName())){
            return obj;
        }
        
        if(clazz.isAssignableFrom(Number.class)){
            
            if(clazz.isAssignableFrom(BigDecimal.class)){
                obj = new BigDecimal(2.0);
            }else if(clazz.isAssignableFrom(BigInteger.class)){
                obj = new BigInteger("1");
            }else if(clazz.isAssignableFrom(Double.class)){
                obj = Double.valueOf(2.0);
            }else if(clazz.isAssignableFrom(Integer.class)){
                obj = Integer.valueOf(1);
            }else{
                throw new Exception("Unable to handle number type of "+clazz);
            }
            
        }else if(clazz.isAssignableFrom(BigDecimal.class)){
            obj = new BigDecimal(2.0);
            
        }else if(clazz.isAssignableFrom(BigInteger.class)){
            obj = new BigInteger("1");
            
        }else if(clazz.isAssignableFrom(String.class)){
            obj = clazz.getName();
            
        }else if(clazz.isAssignableFrom(Boolean.class)){
            obj = Boolean.valueOf("true");
        }
        
        return obj;
    }
    
    /**
     * This internal class is used to wrap information about a generated node object.
     * 
     * @author mhoffman
     *
     */
    protected static class Node extends ChoicesImplemented{
        
        /** the generated class object for this XML node */
        public Object nodeObj;
        
        /** children of this node */
        public List<Node> children = new ArrayList<>();
        
        @Override
        public boolean areMoreChoices(){
            
            if(super.areMoreChoices()){
                //this node has more choices directly
                return true;
            }else{
                //check the children for more choices
                
                for(Node child : children){
                    
                    if(child.areMoreChoices()){
                        return true;
                    }
                }
            }
            
            return false;
        }
        
        @Override
        public String toString(){
            
            StringBuffer sb = new StringBuffer();
            sb.append("[Node: ");
            if(nodeObj instanceof List){
                sb.append("nodeObj = list");
            }else{
                sb.append("nodeObj = ").append(nodeObj);
            }
            sb.append(", ").append(super.toString());
            sb.append(", children = {");
            for(Node child : children){
                sb.append("\n ").append(child);
            }
            sb.append("}");
            sb.append("]");
            return sb.toString();
        }
    } 
    
    /**
     * This internal class is a wrapper for choices made for one or more choice elements
     * that are a child of a node.
     * 
     * @author mhoffman
     *
     */
    protected static class ChoicesImplemented{
        
        // Key: attribute name
        // Value: wrapper for the choices made for the key attribute
        /** contains child choices elements and the choices made for those elements */
        public Map<String, ChoiceImplemented> choices = new HashMap<>();

        /**
         * Return whether there are more choices for a direct child choice element to this node.
         * 
         * @return boolean
         */
        public boolean areMoreChoices(){
            
            for(ChoiceImplemented choice : choices.values()){
                
                if(choice.getMoreAvailable()){
                    return true;
                }
            } 
            
            return false;
        }
        
        @Override
        public String toString(){
            
            StringBuffer sb = new StringBuffer();
            sb.append("choices = {");
            for(String key : choices.keySet()){
                sb.append("\n ").append(key).append(":").append(choices.get(key));
            }
            sb.append("}");
            return sb.toString();
        }
    }
    
    /**
     * This internal class is a wrapper for information about the choices made for a single choice element.
     * 
     * @author mhoffman
     *
     */
    protected static class ChoiceImplemented{
        
        /** the choices used so far */
        public List<ChoiceItem> items = new ArrayList<>();        
        
        /** Flag to indicate if there are more choices available for this element */
        private boolean moreAvailable = false;
        
        /**
         * Whether there are more choices available for this choice element.
         * 
         * @return boolean
         */
        public boolean getMoreAvailable(){
            return moreAvailable;
        }
        
        @Override
        public String toString(){
            
            StringBuffer sb = new StringBuffer();
            sb.append("[ChoiceImplemented: ");
            sb.append("more = ").append(moreAvailable);
            sb.append(", items = {");
            for(ChoiceItem item : items){
                sb.append("\n ").append(item);
            }
            sb.append("}");
            sb.append("]");
            return sb.toString();
        }
    }
    
    /**
     * This internal class is a wrapper for information about a choice made for a single choice element.
     * 
     * @author mhoffman
     *
     */
    protected static class ChoiceItem{
        
        /** the generated class chosen in a choice element */
        public Class<?> choiceClazz;
        
        @Override
        public String toString(){
            return choiceClazz.getName();
        }
    }
    
    
    /**
     * Displays a dialog informing the user that the conversion has completed. If there were no
     * issues during the conversion then a success dialog appears.  If there were issues, a 
     * detailed dialog is built that explains the issues to the user
     */
    protected void showCompletionDialog() {
        
        // Check if anything was unable to be migrated.
        if (conversionIssueList != null && conversionIssueList.isPopulated()) {

            // Build a dialog explaining what was unable to be migrated.
            String content = "<html>"+
                                "<body style='text-align:center;'>"+
                                    "<div style='width:400px; margin: 0 auto;'>" +
                                        "<p style='width:100%; margin: 0 auto;'>" +
                                            "The conversion completed successfully, but the original file " +
                                            "contained elements that are no longer supported by GIFT. " +
                                            "The following elements could not be migrated:" +
                                        "</p><br>";

            Set<Map.Entry<String, HashMap<String, Integer>>> set = conversionIssueList.entrySet();
            Iterator<Map.Entry<String, HashMap<String, Integer>>> i = set.iterator();
            
            // Info has been stored in issueList like so:
            // LinkedHashMap<"file name", HashMap<"element name", "occurrences">>
            Map.Entry<String, HashMap<String, Integer>> entry;
            
            // Iterate through each file name
            while (i.hasNext()) {
                entry = i.next();
                
                content += "<hr />";
                content += "<ul style='float:left; text-align:left;'>";
                
                Iterator<Map.Entry<String, Integer>> messageIterator = entry.getValue().entrySet().iterator();
                
                // For the current file name, iterate though every element stored in the HashMap
                while (messageIterator.hasNext()) {
                    Map.Entry<String, Integer> pairs = messageIterator.next();
                    content += "<li>" + pairs.getKey() + "<br>" +
                                "<i>Occurrences within file: " + pairs.getValue() + "</i>" +
                              "</li><br>";
                }
                
                String fileLink = getFileLink(entry.getKey());
                
                content += "<div style='width:100%; margin:0 auto;'><span style='color:#7092BE;'><i>Original File (click to open):</i></span><br>" + fileLink;
                content += "</ul><div style='clear:both' />";
            }
            
            content += "<br></div></body></html>";
            
            JEditorPane editorPane = new JEditorPane("text/html", content);

            editorPane.addHyperlinkListener(new HyperlinkListener() {
                @Override
                public void hyperlinkUpdate(HyperlinkEvent e) {
                    if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
                        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
                        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
                            try {
                                desktop.browse(e.getURL().toURI());
                            } catch (Exception e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                }
            });
            
            editorPane.setEditable(false);
            editorPane.setBackground(new Color(237,237,237));
            editorPane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
            
            // Inform user that the conversion wizard was unable to migrate some elements.
            JOptionPane.showMessageDialog(null, editorPane, "Unable to migrate some elements", JOptionPane.INFORMATION_MESSAGE);
            
            // Clear issueList for the next conversion
            conversionIssueList.clear();
            
        }else {
            //show success dialog
            JOptionPane.showMessageDialog(null,
                    "The Conversion Wizard successfully migrated the DKF XML file to version "+Version.getInstance().getName()+".",
                    "DKF successfully converted",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    /**
     * Builds an html link to open the file to be displayed in the completion dialog.
     * 
     * @see #showCompletionDialog()
     * @param filePath the path to the file
     * @return a link to open the file
     * 
     * @author mzellars
     */
    private static String getFileLink(String filePath) {
        
        filePath = UriUtil.makeURICompliant(filePath);
        String fileLink = "<a title='"+filePath+"' href='file:///"+filePath+"'>";

        String fileName = filePath;
        
        // Display just the file name if it can be determined (C:\folder\folder\...\filename.dkf)
        if (filePath.contains("\\")) {
            String[] splitPath = filePath.split("\\\\"); 
            fileName = splitPath[splitPath.length-1]; 
        }
        
        // Ensure that the file name is limited to a certain amount of characters
        int allowedCharCnt = 80;
        
        if (fileName.length() > allowedCharCnt) {
            // Shorten the file path 
            String half1 = fileName.substring(0, fileName.length()/2);
            String half2 = fileName.substring(fileName.length()/2);
            
            int deleteCharCnt = (fileName.length() - allowedCharCnt)/2;
            
            half1 = half1.substring(0, half1.length()-deleteCharCnt);
            half2 = half2.substring(deleteCharCnt);
            
            fileName = half1 + "..." + half2;
            
        }
        
        fileLink += fileName + "</a></div>";
        
        return fileLink;    
    }
    
}
