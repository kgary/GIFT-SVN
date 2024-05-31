/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.aar;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import generated.video.GeneralType;
import generated.video.IdentifierType;
import generated.video.LOMType;
import generated.video.TechnicalType;
import mil.arl.gift.common.aar.VideoMetadata.VideoMetadataField;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.common.io.FileValidationException;
import mil.arl.gift.common.io.UnmarshalledFile;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.common.util.StringUtils;

/**
 * This class is responsible for parsing and validating a LOM file.
 * 
 * @author sharrison
 */
public class VideoMetadataFileHandler extends AbstractSchemaHandler {

    /** The catalog metadata field */
    private static final String CATALOG = "catalog";

    /** The entry metadata field */
    private static final String ENTRY = "entry";

    /** The video title metadata field */
    private static final String IDENTIFIER = "identifier";

    /** The LOM Type namespace */
    private static final String LOM_NAMESPACE = "http://ltsc.ieee.org/xsd/LOM";

    /** The metadata file to parse */
    private final FileProxy metadataFile;

    /** The session instance folder that contains the metadata file */
    private final File sessionFolder;

    /** The LOM type xml object parsed from the file */
    private final LOMType lomType;

    /** The parsed metadata from the {@link #lomType} */
    private VideoMetadata videoMetadata;

    /**
     * Parse and validate the LOM type file.
     * 
     * @param metadataFile contains the file elements to parse and validate.
     *        Can't be null and must exist.
     * @param sessionFolder the session instance folder that contains the
     *        metadata file. Can't be null.
     * @param failOnFirstSchemaError - if true than a validation event will
     *        cause the parsing of the XML content to fail and throw an
     *        exception. If there are no validation events than the XML contents
     *        are XML and schema valid. From Java API docs: A validation event
     *        indicates that a problem was encountered while validating the
     *        incoming XML data during an unmarshal operation, while performing
     *        on-demand validation of the Java content tree, or while
     *        marshalling the Java content tree back to XML data.
     * @throws DetailedException if there was a validation issue
     * @throws FileValidationException if there was an issue parsing the file or
     *         validating against the schema
     */
    public VideoMetadataFileHandler(FileProxy metadataFile, File sessionFolder, boolean failOnFirstSchemaError)
            throws DetailedException, FileValidationException {
        super(AbstractSchemaHandler.VIDEO_SCHEMA_FILE);

        if (metadataFile == null) {
            throw new IllegalArgumentException("The parameter 'metadataFile' cannot be null.");
        } else if (sessionFolder == null) {
            throw new IllegalArgumentException("The parameter 'sessionFolder' cannot be null.");
        }

        this.metadataFile = metadataFile;
        this.sessionFolder = sessionFolder;

        try {
            UnmarshalledFile uFile = parseAndValidate(AbstractSchemaHandler.VIDEO_ROOT,
                    metadataFile.getSingleUseInputStream(), (File) null, failOnFirstSchemaError);
            lomType = (LOMType) uFile.getUnmarshalled();
            parseVideoMetadata(lomType);
        } catch (DetailedException e) {
            throw new FileValidationException("Failed to parse and validate the LOM file '" + metadataFile.getFileId()
                    + "' because " + e.getReason(), e.getDetails(), metadataFile.getFileId(), e);

        } catch (Throwable e) {

            String details = "The most likely cause of this error is because the XML file content is not correctly formatted.  This could be anything from a missing XML tag"
                    + " needed to ensure the general XML structure was followed (i.e. all start and end tags are found), to a missing required field (e.g. course name is"
                    + " required) or the value for a field doesn't satisfy the schema requirements (i.e. the course name must be at least 1 character)."
                    + "Please take a look at the first part of stacktrace for a hint at the problem or ask for help on the GIFT <a href=\"https://gifttutoring.org/projects/gift/boards\" target=\"_blank\">forums</a>.\n\n"
                    + "<b>For Example: </b><div style=\"padding: 20px; border: 1px solid gray; background-color: #DDDDDD\">This example stacktrace snippet that indicates the course name ('#AnonType_nameCourse') value doesn't satisfy the minimum length requirement of 1 character:\n\n"
                    + "<i>javax.xml.bind.UnmarshalException - with linked exception: [org.xml.sax.SAXParseException; lineNumber: 2; columnNumber: 69; cvc-minLength-valid: Value '' with length = '0' is not facet-valid with respect to minLength '1' for type '#AnonType_nameCourse'</i></div>";

            if (e.getMessage() != null) {
                details += "\n\n<b>Your Validation error</b>: " + e.getMessage();
            } else if (e.getCause() != null && e.getCause().getMessage() != null) {
                details += "\n\n<b>Your Validation error</b>: " + e.getCause().getMessage();
            }

            throw new FileValidationException(
                    "A problem occurred when parsing and validating the LOM file '" + metadataFile.getName() + "'.",
                    details, metadataFile.getFileId(), e);
        }
    }

    /**
     * Parses the video metadata from the {@link #lomType}.
     * 
     * @param lomType the LOM type xml object to parse.
     */
    private void parseVideoMetadata(LOMType lomType) {

        final Map<String, String> propertyMap = new HashMap<>();
        for (Serializable type : lomType.getGeneralOrLifeCycleOrMetaMetadata()) {
            if (type instanceof GeneralType) {
                GeneralType general = (GeneralType) type;
                for (JAXBElement<? extends Serializable> generalItem : general.getIdentifierOrTitleOrLanguage()) {
                    if (generalItem.getValue() instanceof IdentifierType) {
                        IdentifierType identType = (IdentifierType) generalItem.getValue();

                        for (VideoMetadataField metaField : VideoMetadataField.identifierTypeFields) {
                            String value = getValueFromIdentifierTypes(metaField, identType.getCatalogOrEntry());
                            if (value != null) {
                                propertyMap.put(metaField.getTag(), value);
                                break;
                            }
                        }
                    }
                }
            } else if (type instanceof TechnicalType) {
                TechnicalType technical = (TechnicalType) type;
                for (JAXBElement<? extends Serializable> technicalItem : technical.getFormatOrSizeOrLocation()) {
                    for (VideoMetadataField metaField : VideoMetadataField.technicalTypeFields) {
                        String value = getValueFromTechnicalItem(metaField, technicalItem);
                        if (value != null) {
                            /* Location need more modification */
                            if (metaField == VideoMetadataField.LOCATION) {
                                value = sessionFolder.getName() + File.separator + value;
                            }

                            propertyMap.put(metaField.getTag(), value);
                            break;
                        }
                    }
                }
            }
        }

        videoMetadata = VideoMetadata.generateMetadataFromProperties(propertyMap);
        videoMetadata.setMetadataFile(sessionFolder.getName() + File.separator + metadataFile.getName());
        if (videoMetadata == null) {
            throw new DetailedException(
                    "Unable to use the video metadata because it does not contain a location and/or start time.",
                    "The video metadata '" + metadataFile.getName()
                            + "' does not contain a location and/or start time. Look for '"
                            + VideoMetadataField.LOCATION + "' and '" + VideoMetadataField.START_TIME
                            + "' tags in the file.",
                    null);
        }
    }

    /**
     * Return the LOM root object.
     * 
     * @return will not be null
     */
    public LOMType getLomType() {
        return lomType;
    }

    /**
     * Get the populated {@link VideoMetadata} based on the parsed
     * {@link #lomType}.
     * 
     * @return the video metadata from the LOM file.
     */
    public VideoMetadata getVideoMetadata() {
        return videoMetadata;
    }

    /**
     * Gets the value for the technical item if it matches the metadata field
     * key.
     * 
     * @param metaField the key to identify the specific technical item.
     * @param technicalItem the technical item that contains the field key and
     *        the value.
     * @return the value from the technical item if it matches with the metadata
     *         field key. Can return null if the key does not match.
     */
    private String getValueFromTechnicalItem(VideoMetadataField metaField,
            JAXBElement<? extends Serializable> technicalItem) {
        if (metaField == null || technicalItem == null) {
            return null;
        }

        if (technicalItem.getValue() instanceof String
                && StringUtils.equalsIgnoreCase(technicalItem.getName().getLocalPart(), metaField.getTag())) {
            return (String) technicalItem.getValue();
        }

        return null;
    }

    /**
     * Gets the value for the identifier type with the given key.
     * 
     * @param catalogKey the key to identify the specific identifier type.
     * @param identifierTypes the list of identifier types to search.
     * @return the value from the identifier type with the matching key. Can
     *         return null if the key is not found.
     */
    private String getValueFromIdentifierTypes(VideoMetadataField catalogKey,
            List<JAXBElement<String>> identifierTypes) {
        /* Must contain only a key and value pair */
        if (catalogKey == null || CollectionUtils.isEmpty(identifierTypes) || identifierTypes.size() != 2) {
            return null;
        }

        JAXBElement<String> item1 = identifierTypes.get(0);
        JAXBElement<String> item2 = identifierTypes.get(1);

        /* Determine which is the key and which is the value */
        if (StringUtils.equalsIgnoreCase(item1.getName().getLocalPart(), CATALOG)) {
            if (StringUtils.equalsIgnoreCase(item1.getValue(), catalogKey.getTag())
                    && StringUtils.equalsIgnoreCase(item2.getName().getLocalPart(), ENTRY)) {
                return item2.getValue();
            }
        } else if (StringUtils.equalsIgnoreCase(item2.getName().getLocalPart(), CATALOG)) {
            if (StringUtils.equalsIgnoreCase(item2.getValue(), catalogKey.getTag())
                    && StringUtils.equalsIgnoreCase(item1.getName().getLocalPart(), ENTRY)) {
                return item1.getValue();
            }
        }

        /* Couldn't find a key/value pair */
        return null;
    }

    /**
     * Builds a LOM type from the video metadata.
     * 
     * @param videoMetadata the metadata used to build the LOM.
     * @return the built jaxb LOM type.
     */
    public static JAXBElement<LOMType> buildLOM(VideoMetadata videoMetadata) {
        if (videoMetadata == null) {
            throw new IllegalArgumentException("The parameter 'videoMetadata' cannot be null.");
        }

        LOMType lom = new LOMType();

        /* Build Technical Type - Location */
        TechnicalType techType = new TechnicalType();
        JAXBElement<? extends Serializable> location = new JAXBElement<>(
                new QName(LOM_NAMESPACE, VideoMetadataField.LOCATION.getTag()), String.class,
                videoMetadata.getLocation());
        techType.getFormatOrSizeOrLocation().add(location);

        /* Build General Type - Start Time, Offset, Title */
        GeneralType generalType = new GeneralType();
        List<JAXBElement<? extends Serializable>> generalList = generalType.getIdentifierOrTitleOrLanguage();
        addIdentifierType(VideoMetadataField.START_TIME, Long.toString(videoMetadata.getStartTime().getTime()),
                generalList);
        addIdentifierType(VideoMetadataField.OFFSET, Long.toString(videoMetadata.getOffset()), generalList);
        addIdentifierType(VideoMetadataField.TITLE, videoMetadata.getTitle(), generalList);
        addIdentifierType(VideoMetadataField.TASK_CONCEPT_NAME, videoMetadata.getTaskConceptName(), generalList);
        addIdentifierType(VideoMetadataField.SPACE_METADATA_FILE, videoMetadata.getSpaceMetadataFile(), generalList);
        addIdentifierType(VideoMetadataField.VIDEO_SOURCE, videoMetadata.getVideoSource(), generalList);

        lom.getGeneralOrLifeCycleOrMetaMetadata().add(techType);
        lom.getGeneralOrLifeCycleOrMetaMetadata().add(generalType);

        return new JAXBElement<LOMType>(new QName(LOM_NAMESPACE, "lom"), LOMType.class, lom);
    }

    /**
     * Builds and adds the identifier jaxb object to the provided list.
     * 
     * @param catalog the catalog value for the identifier object. E.g. the name
     *        of the attribute.
     * @param entry the entry value for the identifier object. E.g. the value
     *        for the attribute.
     * @param jaxbList the list to add the built identifier type to.
     */
    private static void addIdentifierType(VideoMetadataField catalog, String entry,
            List<JAXBElement<? extends Serializable>> jaxbList) {
        if (jaxbList == null || catalog == null || StringUtils.isBlank(entry)) {
            return;
        }
        IdentifierType identifierType = new IdentifierType();

        List<JAXBElement<String>> objList = identifierType.getCatalogOrEntry();

        objList.add(new JAXBElement<String>(new QName(LOM_NAMESPACE, CATALOG), String.class, catalog.getTag()));
        objList.add(new JAXBElement<String>(new QName(LOM_NAMESPACE, ENTRY), String.class, entry));

        JAXBElement<? extends Serializable> identifierObj = new JAXBElement<>(new QName(LOM_NAMESPACE, IDENTIFIER),
                IdentifierType.class, identifierType);
        jaxbList.add(identifierObj);
    }
}
