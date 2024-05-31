/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.lessonmaterial;

import java.io.IOException;
import java.io.Serializable;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import generated.dkf.BooleanEnum;
import mil.arl.gift.common.course.CourseConceptsUtil;
import mil.arl.gift.common.course.GIFTValidationResults;
import mil.arl.gift.common.io.AbstractFolderProxy;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.common.io.FileValidationException;
import mil.arl.gift.common.io.UnmarshalledFile;
import mil.arl.gift.common.io.UriUtil;
import mil.arl.gift.common.io.UriUtil.InternetConnectionStatusEnum;
import mil.arl.gift.domain.DomainModuleProperties;

/**
 * Handles parsing a file containing the list of lesson material for a domain
 * module
 *
 * @author jleonard
 */
public class LessonMaterialFileHandler extends AbstractSchemaHandler {

    private String lessonMaterialFileName;
    
    private FileProxy file;

    /** the generated class instance for lesson material */
    private generated.course.LessonMaterialList lessonMaterial = null;
    
    private InternetConnectionStatusEnum connectionStatus;
    
    /**
     * Constructor - set attributes
     *
     * @param file The file containing the lesson material definitions
     * @param courseDirectory the course folder to use for course content references (e.g. DKF path relative to course folder) found in the course file.
     * @param connectionStatus The status of an Internet connection for the Domain module.  This is useful when validating.
     */
    public LessonMaterialFileHandler(FileProxy file, AbstractFolderProxy courseDirectory, InternetConnectionStatusEnum connectionStatus) {
        super(LESSON_MATERIAL_SCHEMA_FILE);
     
        this.connectionStatus = connectionStatus;
        this.lessonMaterialFileName = file.getFileId();
        
        if(!file.exists()){
            throw new IllegalArgumentException("Unable to find the file named "+file.getFileId());
        }
        
        this.file = file;
    }

    /**
     * Constructor - set attributes
     *
     * @param lessonMaterialFileName The name of the file containing the lesson material definitions, relative to
     *          the training material domain property directory
     * @param courseDirectory the course folder to use for course content references (e.g. DKF path relative to course folder) found in the course file.
     * @param connectionStatus The status of an Internet connection for the Domain module.  This is useful when validating.
     * @throws IOException  if there was a problem retrieving the file by the name
     */
    public LessonMaterialFileHandler(String lessonMaterialFileName, AbstractFolderProxy courseDirectory, InternetConnectionStatusEnum connectionStatus) 
            throws IOException {
        this(courseDirectory.getRelativeFile(lessonMaterialFileName), courseDirectory, connectionStatus);

    }

    /**
     * Gets the lesson material from the file, available after it is parsed
     *
     * @return Lesson Material The lesson material from the file, available
     * after it is parsed
     */
    public generated.course.LessonMaterialList getLessonMaterial() {
        return lessonMaterial;
    }

    /**
     * Parse the lesson material file and get the list of lesson material.
     * 
     * @param failOnFirstSchemaError - if true than a validation event will cause the parsing of the XML content to fail and throw an exception.
     * If there are no validation events than the XML contents are XML and schema valid.
     * From Java API docs: A validation event indicates that a problem was encountered while validating the incoming XML data during an unmarshal operation, while 
     * performing on-demand validation of the Java content tree, or while marshalling the Java content tree back to XML data.
     * @throws FileValidationException if there was a problem parsing the lesson material file
     */
    public void parse(boolean failOnFirstSchemaError) throws FileValidationException {
        
        try {
            UnmarshalledFile lessonMaterialObj = parseAndValidate(AbstractSchemaHandler.LESSON_MATERIAL_ROOT, file.getInputStream(), failOnFirstSchemaError);
            final generated.course.LessonMaterialList lessonMaterialList = (generated.course.LessonMaterialList) lessonMaterialObj
                    .getUnmarshalled();
            CourseConceptsUtil.cleanMediaConcepts(lessonMaterialList.getMedia());
            setLessonMaterial(lessonMaterialList);
        } catch (Exception e) {
            throw new FileValidationException("Failed to parse and validate the lesson material file.",
                    e.getMessage() != null ? e.getMessage() : e.toString(),
                    lessonMaterialFileName,
                    e);
        }

    }
    
    /**
     * Validates the list of lesson material.
     * 
     * @param course the course that contains the course properties. A null course will not validate
     *            if media properties correctly match with course properties (e.g. concepts, LTI
     *            provider identifiers, etc...)
     * @param courseDirectory the course directory to use for course content references (e.g. DKF
     *            path relative to course folder) found in the course file.
     * @return validation results
     * @throws FileValidationException If the list of lesson material could not be validated.
     */
    public GIFTValidationResults validateLessonMaterial(generated.course.Course course, AbstractFolderProxy courseDirectory) throws FileValidationException{

        GIFTValidationResults validationResults = null;
        if(this.lessonMaterial != null){
            
            validationResults = new GIFTValidationResults();
            
            for(generated.course.Media m : this.lessonMaterial.getMedia()){   
                try{
                    validateMedia(m, course, courseDirectory, connectionStatus);
                }catch(ConnectException exception){
                    
                    String lmName = m.getName() == null || m.getName().isEmpty() ? "'[no name given]'" : "'" + m.getName() + "'";
                    if(connectionStatus == InternetConnectionStatusEnum.CONNECTED){
                        validationResults.addImportantIssue(
                                new FileValidationException("Failed to validate media.", 
                                "The lesson material named "+lmName+" failed validation checks because of not being able to connection to a network resource (e.g. website)." +
                              "  If you are running GIFT without a network/internet connection then please provide an appropriate network connection for this course to be available.",
                              file.getFileId(),
                              exception));
                    }else{
                     
                        validationResults.addWarningIssue(
                                new FileValidationException("Failed to validate media.", 
                                "The lesson material named "+lmName+" failed validation checks because of not being able to connection to a network resource (e.g. website)." +
                              "  If you are running GIFT without a network/internet connection then please provide an appropriate network connection.",
                              file.getFileId(),
                              exception));
                    }
                }catch(Exception e){
                    validationResults.addImportantIssue(
                            new FileValidationException("Failed to validate media.", 
                            "The lesson material named '"+m.getName()+"' failed validation checks because of an error : " +e.getMessage(), file.getFileId(), e));
                }
            }

        }
        
        return validationResults;
    }
    
    /**
     * Validates a media object and updates its contents for URI corrections
     * 
     * @param m The media object to be validated and updated.
     * @param course the course that contains the course properties. A null course will not validate
     *            if media properties correctly match with course properties (e.g. concepts, LTI
     *            provider identifiers, etc...)
     * @param courseDirectory the course directory to use for course content references (e.g. DKF path relative to course folder) found in the course file.
     * @param connectionStatus used to indicate whether the Domain module has an Internet connection
     * @return String If a URI was corrected during validation, the corrected URI address. Otherwise, null.
     * @throws MalformedURLException If the URI address is not a local file and does not match URL conventions.
     * @throws IOException If an error occurs while trying to connect to a web resource specified by the URI address.
     * @throws ConnectException If a connection to a web resource specified by the URI address yields an invalid response code.
     * @throws Exception If the media object fails validation.
     */
    public static String validateMedia(generated.course.Media m, generated.course.Course course, AbstractFolderProxy courseDirectory, InternetConnectionStatusEnum connectionStatus) 
            throws MalformedURLException, IOException, ConnectException, Exception{
        
        if (m.getMediaTypeProperties() instanceof generated.course.SlideShowProperties) {
            validateSlideShow(m, courseDirectory, connectionStatus);
            return null;
        } 
        
        String uri = m.getUri();
        if(uri == null || uri.trim().isEmpty()){
            throw new Exception("the media reference (URI) can't be null or an empty string.  Please provide the appropriate media content (e.g. an image course object requires an image).");
        }
        
        String correctedUri;
        if (m.getMediaTypeProperties() instanceof generated.course.LtiProperties) {
            validateLtiProperties(m, course);
            // We do not want to ping the LTI provider during validation. Call the no-ping validation option.
            correctedUri = UriUtil.validateUriNoPing(uri, courseDirectory, connectionStatus);
        
        } else if(m.getDisplaySessionProperties() != null && generated.course.BooleanEnum.TRUE.equals(m.getDisplaySessionProperties().getRequestUsingSessionState())){
            
            /* this URL points to an external assessment server that doesn't need to be running during validation */
            correctedUri = UriUtil.validateUriNoPing(uri, courseDirectory, connectionStatus);
        
        } else {
            correctedUri = UriUtil.validateUri(uri, courseDirectory, connectionStatus);
        }
        
        if(correctedUri != null){
            //update URI to corrected one
            uri = correctedUri;
        } 
        
        try{                        
            String hostname = (new URL(uri)).getHost();                                 
            if(hostname.contains("youtube") || hostname.contains("youtu.be")){
            
                //if a valid URI references a YouTube video that is not embedded in an iFrame, add information to embed the video
                correctedUri = createEmbeddedYouTubeUrl(uri, m.getMediaTypeProperties());
            }
        } catch (@SuppressWarnings("unused") MalformedURLException e){                                  
            // skip YouTube URL conversion if the URI does not have a URL protocol or the URL does not match known YouTube URL conventions
        }
        
        if(correctedUri != null){
            m.setUri(correctedUri);
        }     
        
        return correctedUri;
    }  
    
    /**
     * Validates a media object and updates its contents for URI corrections
     * 
     * @param m The media object to be validated and updated.
     * @param course the course that contains the course properties. A null course will not validate
     *            if media properties correctly match with course properties (e.g. concepts, LTI
     *            provider identifiers, etc...)
     * @param courseDirectory the course directory to use for course content references (e.g. DKF path relative to course folder) found in the course file.
     * @param connectionStatus used to indicate whether the Domain module has an Internet connection
     * @return String If a URI was corrected during validation, the corrected URI address. Otherwise, null.
     * @throws MalformedURLException If the URI address is not a local file and does not match URL conventions.
     * @throws IOException If an error occurs while trying to connect to a web resource specified by the URI address.
     * @throws ConnectException If a connection to a web resource specified by the URI address yields an invalid response code.
     * @throws Exception If the media object fails validation.
     */
    public static String validateMedia(generated.dkf.Media m, AbstractFolderProxy courseDirectory, InternetConnectionStatusEnum connectionStatus) 
            throws MalformedURLException, IOException, ConnectException, Exception{
        
        if (m.getMediaTypeProperties() instanceof generated.dkf.SlideShowProperties) {
            validateSlideShow(m, courseDirectory, connectionStatus);
            return null;
        } 
        
        String uri = m.getUri();
        if(uri == null || uri.isEmpty()){
            throw new Exception("the media reference (URI) can't be null or an empty string.  Please provide the appropriate media content (e.g. an image course object requires an image).");
        }
        
        String correctedUri;
        if(m.getDisplaySessionProperties() != null && BooleanEnum.TRUE.equals(m.getDisplaySessionProperties().getRequestUsingSessionState())){
            
            /* this URL points to an external assessment server that doesn't need to be running during validation */
            correctedUri = UriUtil.validateUriNoPing(uri, courseDirectory, connectionStatus);
            
        } else {
            correctedUri = UriUtil.validateUri(uri, courseDirectory, connectionStatus);
        }
        
        if(correctedUri != null){
            //update URI to corrected one
            uri = correctedUri;
        } 
        
        try{                        
            String hostname = (new URL(uri)).getHost();                                 
            if(hostname.contains("youtube") || hostname.contains("youtu.be")){
            
                //if a valid URI references a YouTube video that is not embedded in an iFrame, add information to embed the video
                correctedUri = createEmbeddedYouTubeUrl(uri, m.getMediaTypeProperties());
            }
        } catch (@SuppressWarnings("unused") MalformedURLException e){                                  
            // skip YouTube URL conversion if the URI does not have a URL protocol or the URL does not match known YouTube URL conventions
        }
        
        if(correctedUri != null){
            m.setUri(correctedUri);
        }     
        
        return correctedUri;
    }  
    
    /**
     * Creates a URL that embeds a YouTube video into an iFrame.
     * 
     * @param url The URL of the YouTube video.
     * @param mediaProperties The media properties associated with the YouTube video URL.  Can be null if there are no properties for this URL.
     * @return String A URL that embeds the YouTube video into an iFrame.
     * @throws MalformedURLException If the URL does not match any known YouTube video URL conventions.
     * @throws Exception If an error occurs while creating the embedded URL.
     */
    public static String createEmbeddedYouTubeUrl(String url, Serializable mediaProperties) throws MalformedURLException, Exception{
        
        boolean autoPlayEnabled = false;
        
        if(mediaProperties != null) {
            
            if(mediaProperties instanceof generated.course.YoutubeVideoProperties){
            
                // if the non-embedded URL has YouTube video properties associated with it, use those for creating the new URL
                generated.course.YoutubeVideoProperties videoProperties = (generated.course.YoutubeVideoProperties) mediaProperties;
                
                autoPlayEnabled = videoProperties.getAllowAutoPlay() != null 
                        && videoProperties.getAllowAutoPlay() == generated.course.BooleanEnum.TRUE;
            
            } else if(mediaProperties instanceof generated.dkf.YoutubeVideoProperties){
            
                // if the non-embedded URL has YouTube video properties associated with it, use those for creating the new URL
                generated.dkf.YoutubeVideoProperties videoProperties = (generated.dkf.YoutubeVideoProperties) mediaProperties;
                
                autoPlayEnabled = videoProperties.getAllowAutoPlay() != null 
                        && videoProperties.getAllowAutoPlay() == generated.dkf.BooleanEnum.TRUE;
            
            }
        }
        
        StringBuilder iFrameEmbeddedUrl = new StringBuilder();
        
        if(url.contains("/embed/")){
            
            // if the URL already embeds the video, check to see if it has the correct AutoPlay setting
            
            if(!url.contains("autoplay=1") && autoPlayEnabled){
                
                // if the AutoPlay property is enabled and the URL does not enable it, change the URL to enable it
                iFrameEmbeddedUrl.append(url.replaceAll("\\Q&\\E*autoplay=0", ""));
                if(url.contains("?")){
                    //when this isn't the only argument after the video id
                    iFrameEmbeddedUrl.append("&autoplay=1");   
                }else{
                    //when this is the first argument after the video id
                    iFrameEmbeddedUrl.append("?autoplay=1");   
                }
                
                return iFrameEmbeddedUrl.toString();
                
            } else if(url.contains("autoplay=1") && !autoPlayEnabled){
                
                // if the AutoPlay property is disabled and the URL enables it, change the URL to disable it
                iFrameEmbeddedUrl.append(url.replaceAll("\\Q&\\E*autoplay=1", "")); 
                
                return iFrameEmbeddedUrl.toString();
                
            } else{         
                
                // otherwise, the URL does not need to be changed
                return null;
            }
            
        }
        
        // define a pattern describing known YouTube URL conventions in order to retrieve the video ID
        // Pattern 1:
        //        ^[^v]+v=(.{11}).*  --- ignore everything until 'v=', grab video ID from next 11 characters
        //        example URL : https://www.youtube.com/watch?v=qD9V4d-m4EQ
        // Pattern 2:
        //        ^[^v]+youtube\/(.{11}).* --- ignore everything until 'youtube/', grab video ID from next 11 characters
        //        example URL : https://youtube/IEEHr46uDpU
        // Pattern 3: 
        //        ^[^v]+youtu.be//(.{11}).* --- ignore everything until 'youtu.be/', grab video ID from next 11 characters
        //        example URL : https://youtu.be/IEEHr46uDpU 
        
        String youTubeVideoID = null;
        for(String expression : Constants.YOUTUBE_VIDEO_PATTERNS){
                    
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(expression);
            java.util.regex.Matcher match = pattern.matcher(url);            
            
            if(match.matches()){
                
                // if the non-embedded URL matches any known YouTube URL conventions, get the video ID of the YouTube video
                youTubeVideoID = match.group(1);
                
                if(youTubeVideoID == null || youTubeVideoID.isEmpty() || youTubeVideoID.length() != 11){                    
                    youTubeVideoID = null;
                    continue;
                }
                
                break;
                
            }
        }//end for
        
        if(youTubeVideoID == null){
            throw new java.net.MalformedURLException("URL address of YouTube video does not match any known YouTube URL conventions.");
        }

        // build the URL that will embed the YouTube video within an iFrame
        
        // put the YouTube video ID in an embedded YouTube video address and add the address to the embedded URL
        iFrameEmbeddedUrl.append("https://www.youtube.com/embed/").append(youTubeVideoID).append("?rel=0&showinfo=0");
        
        if(autoPlayEnabled){
            
            // if automatically playing the video is enabled, add "&amp;autoplay=1" to the embedded URL
            iFrameEmbeddedUrl.append("&autoplay=1");
        }
      
        return iFrameEmbeddedUrl.toString();
    }
    
    /**
     * Validates a slideshow object
     * 
     * @param m The media object to be validated and updated.
     * @param courseDirectory the course directory to use for course content references (e.g. DKF path relative to course folder) found in the course file.
     * @param connectionStatus used to indicate whether the Domain module has an Internet connection
     * @throws Exception If the media object fails validation.
     */
    public static void validateSlideShow(generated.course.Media m, AbstractFolderProxy courseDirectory, InternetConnectionStatusEnum connectionStatus)
            throws Exception{
        Serializable obj = m.getMediaTypeProperties();
        generated.course.SlideShowProperties properties = (generated.course.SlideShowProperties) obj;
        if(properties != null){
            List<String> slides = properties.getSlideRelativePath();
            if(slides.isEmpty()) {
                throw new Exception("No Slides found");
            }
            for(String uri : slides){
                UriUtil.validateUri(uri, courseDirectory, connectionStatus);
            }
        }else {
            throw new Exception("Object was not a SlideShowProperties");
        }
    }
    
    /**
     * Validates a slideshow object
     * 
     * @param m The media object to be validated and updated.
     * @param courseDirectory the course directory to use for course content references (e.g. DKF path relative to course folder) found in the course file.
     * @param connectionStatus used to indicate whether the Domain module has an Internet connection
     * @throws Exception If the media object fails validation.
     */
    public static void validateSlideShow(generated.dkf.Media m, AbstractFolderProxy courseDirectory, InternetConnectionStatusEnum connectionStatus)
            throws Exception{
        Serializable obj = m.getMediaTypeProperties();
        generated.dkf.SlideShowProperties properties = (generated.dkf.SlideShowProperties) obj;
        if(properties != null){
            List<String> slides = properties.getSlideRelativePath();
            if(slides.isEmpty()) {
                throw new Exception("No Slides found");
            }
            for(String uri : slides){
                UriUtil.validateUri(uri, courseDirectory, connectionStatus);
            }
        }else {
            throw new Exception("Object was not a SlideShowProperties");
        }
    }
    
    /**
     * Validates an LTI Provider object
     * 
     * @param media The media object to be validated.
     * @param course the course that contains the course properties. A null course will not validate
     *            if media properties correctly match with course properties (e.g. concepts, LTI
     *            provider identifiers, etc...)
     * @throws DetailedException If the media object fails validation.
     */
    public static void validateLtiProperties(generated.course.Media media, generated.course.Course course) throws DetailedException, Exception {
        if (media.getMediaTypeProperties() instanceof generated.course.LtiProperties) {
            generated.course.LtiProperties properties = (generated.course.LtiProperties) media.getMediaTypeProperties();
            String mediaName = media.getName() == null || media.getName().isEmpty() ? "'[no name given]'" : "'" + media.getName() + "'";
            if (properties != null) {
                // missing lti identifier
                if (properties.getLtiIdentifier() == null || properties.getLtiIdentifier().isEmpty()) {
                    throw new DetailedException(mediaName + " in the LTI course object is missing a selected LTI identifier.",
                            "Please make sure to select an LTI identifier for the LTI course object with media name " + mediaName, null);
                }
                
                //missing URL
                if (media.getUri() == null || media.getUri().isEmpty()) {
                    throw new DetailedException(mediaName + " in the LTI course object is missing a LTI URL", 
                            "Please make sure to specify a valid LTI URL for the LTI course object with media name " + mediaName, null);
                }
                
                //custom parameter validation
                if (properties.getCustomParameters() != null) {
                    Iterator<generated.course.Nvpair> nvIter = properties.getCustomParameters().getNvpair().iterator();
                    HashSet<String> pastKeys = new HashSet<String>();
                    while (nvIter.hasNext()) {
                        generated.course.Nvpair nv = nvIter.next();
                        boolean isNameBlank = nv.getName() == null || nv.getName().trim().isEmpty();
                        boolean isValueBlank = nv.getValue() == null || nv.getValue().trim().isEmpty();

                        if (isNameBlank && isValueBlank) {
                            nvIter.remove();
                        } else if (isNameBlank || isValueBlank) {
                            throw new DetailedException(
                                    mediaName + " in the LTI course object has malformed custom parameters",
                                    "Please make sure that the 'LTI Custom Parameters' table's key-value pairs have data for both the key and the value for the LTI course object with media name "
                                            + mediaName + ".",
                                    null);
                        }
                        
                        if (!isNameBlank) {
                            String standardizedKey = standardizeCustomParameter(nv.getName());
                            if (pastKeys.contains(standardizedKey)) {
                                throw new DetailedException(
                                        mediaName + " in the LTI course object has duplicate custom parameter key: '" + nv.getName() + "'.",
                                        "Duplicate custom parameter key: '" + nv.getName()
                                                + "'. Please make sure that the 'LTI Custom Parameters' table's key values are unique for the LTI course object with media name "
                                                + mediaName + ". Note that anything other than a letter or number is converted to an underscore.",
                                        null);
                            } else {
                                pastKeys.add(standardizedKey);
                            }
                        }
                    }
                }

                //learner state attribute
                if(properties.getIsKnowledge() == null) {
                    throw new DetailedException(mediaName + " in the LTI course object is missing a boolean for specifying learner state",
                            "Please make sure to specify an LTI learner state attribute for the LTI course object with media name " + mediaName, null);
                }
                
                if (generated.course.BooleanEnum.TRUE.equals(properties.getAllowScore())) {
                    // missing concepts
                    if (properties.getLtiConcepts() == null || properties.getLtiConcepts().getConcepts().isEmpty()) {
                        throw new DetailedException(mediaName + " in the LTI course object is missing a selected concept.",
                                "Please make sure one or more course concepts have been selected for the LTI course object with media name "
                                        + mediaName,
                                null);
                    }

                    // validate slider values
                    if (properties.getSliderMinValue() == null) {
                        throw new DetailedException(mediaName + " in the LTI course object is missing a minimum value",
                                "Please make sure the min slider value is set for the LTI course object with media name " + mediaName, null);
                    }

                    if (properties.getSliderMaxValue() == null) {
                        throw new DetailedException(mediaName + " in the LTI course object is missing a maximum value",
                                "Please make sure the max slider value is set for the LTI course object with media name " + mediaName, null);
                    }

                    int min = properties.getSliderMinValue().intValue();
                    int max = properties.getSliderMaxValue().intValue();

                    if (!(min >= 0 && max <= 100 && min <= max)) {
                        throw new DetailedException(mediaName + " in the LTI course object has min and max values that are out of range",
                                "Please make sure that the min value is 0 or greater, " + "the max value is 100 or less, "
                                        + "and the min value is less than or equal to the max value " + "for the LTI course object with media name "
                                        + mediaName,
                                null);
                    }
                }
                
                //display mode
                generated.course.DisplayModeEnum displayMode = properties.getDisplayMode();
                if(displayMode == null || 
                        !displayMode.equals(generated.course.DisplayModeEnum.INLINE)
                        && !displayMode.equals(generated.course.DisplayModeEnum.MODAL)
                        && !displayMode.equals(generated.course.DisplayModeEnum.NEW_WINDOW)) {
                    throw new DetailedException(mediaName + " in the LTI course object is missing a valid display mode", 
                            "Please make sure that a valid display mode is included. Valid display modes include: " 
                                + Arrays.asList(generated.course.DisplayModeEnum.values())
                                + ". Use a valid display mode for the LTI course object media name " + mediaName, null);
                }
                
                verifyLtiPropertiesMatchesCourseProperties(properties, course);
            }
        } else {
            throw new Exception("Object was not an LtiProperties");
        }
    }
    
    /**
     * Standardizes the custom parameter names to follow LTI standards.
     * 
     * @param customParameterKey the custom parameter key string
     */
    private static String standardizeCustomParameter(String customParameterKey) {
        String standardized = customParameterKey;

        if (customParameterKey != null && !customParameterKey.isEmpty()) {

            // convert keyname to LTI standard
            // 1. trim off excess whitespace (e.g. if they did key = value instead of key=value)
            // 2. replace all non-alphanumeric values with an underscore
            // 3. make lower case
            standardized = customParameterKey.trim().replaceAll("[^A-Za-z0-9]", "_").toLowerCase();
        }

        return standardized;
    }
    
    /**
     * Verifies that the course specific properties exist for the LTI properties.
     * 
     * @param ltiProperties the LTI properties that contains the selected authored data. If null, no
     *            validation will occur.
     * @param course the course contains the property values that will be checked against. If null,
     *            no validation will occur.
     * @throws DetailedException exception will be thrown if the LTI properties contains values that
     *             do not exist in the course properties
     */
    private static void verifyLtiPropertiesMatchesCourseProperties(generated.course.LtiProperties ltiProperties, generated.course.Course course) {
        // can only verify if the course and LTI properties are populated.
        if (course != null && ltiProperties != null) {

            String ltiIdentifier = ltiProperties.getLtiIdentifier();

            // can only check the media's LTI provider ID if the course contains LTI providers
            if (course.getLtiProviders() != null && ltiIdentifier != null && !ltiIdentifier.trim().isEmpty()) {
                List<generated.course.LtiProvider> ltiProviders = course.getLtiProviders().getLtiProvider();

                // Ensure that there is an LTI provider that matches the reference in the course xml
                boolean ltiProviderFound = false;

                // LTI providers from authored course
                if (ltiProviders != null) {
                    for (generated.course.LtiProvider ltiProvider : ltiProviders) {
                        if (ltiProvider.getIdentifier().equalsIgnoreCase(ltiIdentifier)) {
                            ltiProviderFound = true;
                            break;
                        }
                    }
                }

                if (!ltiProviderFound) {
                    // LTI providers from property file
                    HashMap<String, generated.course.LtiProvider> propertyProviders = DomainModuleProperties.getInstance().getTrustedLtiProviders();
                    if (propertyProviders != null) {
                        ltiProviderFound = propertyProviders.containsKey(ltiIdentifier);
                    }
                }

                if (!ltiProviderFound) {
                    throw new DetailedException("Cannot locate the referenced LTI provider: '" + ltiIdentifier + "' within the course properties.",
                            "Change the LTI identifier '" + ltiIdentifier + "' to reference an existing LTI Provider identifier", null);
                }
            }

            // can only check the media's selected concepts if the course contains concepts
            if (course.getConcepts() != null) {
                // get a flattened list of the course concepts
                List<String> conceptNames = CourseConceptsUtil.getConceptNameList(course.getConcepts());

                // only validate concepts if the author is allowing a returned score
                if (generated.course.BooleanEnum.TRUE.equals(ltiProperties.getAllowScore()) && ltiProperties.getLtiConcepts() != null) {
                    // Ensure that all the concepts within the lti properties are contained within
                    // the list of concepts
                    for (String concept : ltiProperties.getLtiConcepts().getConcepts()) {
                        if (!conceptNames.contains(concept)) {
                            throw new DetailedException(String.format(
                                    "The concept '%s' was referenced in the LTI course object but wasn't defined in the list of course concepts",
                                    concept),
                                    String.format("Please add the concept '%s' to the list of course concepts or remove it from "
                                            + "the concepts within the LTI course object", concept),
                                    null);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Set the generated class instance of lesson material
     * 
     * @param lessonMaterial course lesson material information
     */
    public void setLessonMaterial(generated.course.LessonMaterialList lessonMaterial){        
        this.lessonMaterial = lessonMaterial;
    }

}
