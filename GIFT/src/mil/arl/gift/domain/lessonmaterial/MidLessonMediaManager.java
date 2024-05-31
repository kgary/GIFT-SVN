/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.lessonmaterial;

import java.io.IOException;
import java.net.ConnectException;
import java.net.MalformedURLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generated.dkf.LtiProperties;
import generated.dkf.Media;
import generated.dkf.SlideShowProperties;
import generated.dkf.WebpageProperties;
import mil.arl.gift.common.io.AbstractFolderProxy;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.io.UriUtil;
import mil.arl.gift.common.io.UriUtil.InternetConnectionStatusEnum;
import mil.arl.gift.domain.DomainModuleProperties;

/**
 * Manages the mid-lesson media for a domain session, similar to LessonMaterialManager
 *
 * @author nroberts
 */
public class MidLessonMediaManager {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(MidLessonMediaManager.class);    
    
    /** used to determine if the URI provided for a media item is web-based or not */
    private static final String NETWORK_IDENTIFIER = "://";

    private generated.dkf.LessonMaterialList lessonMaterial = new generated.dkf.LessonMaterialList();    

    /** server prefix to network resources */
    private String networkURL;
    
    /** the course folder that contains all course relevant files */
    private AbstractFolderProxy courseDirectory;
    
    /** the relative path of the runtime course folder from the domain folder, set during initialization of this class */
    private String domainRelativeCourseDirectory;

    /**
     * Constructor - get mid-lesson media host url (the domain jetty server) and set the course directory attribute.
     * 
     * @param courseDirectory the course folder that contains all course relevant files
     * @param runtimeCourseFolderRelativePath the relative path of the runtime course folder from the domain folder, set during initialization of this class
     */
    public MidLessonMediaManager(AbstractFolderProxy courseDirectory, String runtimeCourseFolderRelativePath) {
        
        try {
            networkURL = DomainModuleProperties.getInstance().getDomainContentServerAddress() + "/";
        } catch (Exception ex) {
            logger.error("Could not get the host IP address, defaulting to 'localhost'", ex);
            networkURL =  DomainModuleProperties.getInstance().getTransferProtocol() + "localhost:" + DomainModuleProperties.getInstance().getDomainContentServerPort() + "/";
        }
        
        setCourseDirectory(courseDirectory);
        
        // Firefox browsers do not automatically replace backslashes, resulting in invalid urls
        domainRelativeCourseDirectory = UriUtil.makeURICompliant(runtimeCourseFolderRelativePath);
    }
    
    private void setCourseDirectory(AbstractFolderProxy courseDirectory){
        
        if(courseDirectory == null){
            throw new NullPointerException("The course directory can't be null.");
        }
        
        this.courseDirectory = courseDirectory;
    }
    
    /**
     * Remove all of the current references to lesson material
     */
    public void clear(){
        
        lessonMaterial.getMedia().clear();
    }
    
    /**
     * Handle any augmentation of the media attributes.  This includes updating the network
     * address of the domain content server in the media URI.  For youtube videos this means
     * adding the necessary parameters to the youtube URL (e.g. fullscreen, auto play).
     * 
     * @param media the media element to check and possibly update it's URI.  Can't be null.
     * @throws MalformedURLException If the URI address is not a local file and does not match URL conventions.
     * @throws IOException If an error occurs while trying to connect to a web resource specified by the URI address.
     * @throws ConnectException If a connection to a web resource specified by the URI address yields an invalid response code.
     * @throws Exception If the media object fails validation.
     */
    private void handleMedia(Media media) throws ConnectException, MalformedURLException, IOException, Exception{
        
        LessonMaterialFileHandler.validateMedia(media, courseDirectory, InternetConnectionStatusEnum.UNKNOWN);
        
        //update the URI if needed            
    	String uriPrefix = networkURL + domainRelativeCourseDirectory + Constants.FORWARD_SLASH;
    	
    	if(media.getMediaTypeProperties() instanceof SlideShowProperties) {
    		
    		SlideShowProperties properties = (SlideShowProperties) media.getMediaTypeProperties();
    		for(int i = 0; i < properties.getSlideRelativePath().size(); i++) {
                
                if(!properties.getSlideRelativePath().get(i).contains(NETWORK_IDENTIFIER)){
                    properties.getSlideRelativePath().set(i, uriPrefix + UriUtil.makeURICompliant(properties.getSlideRelativePath().get(i)));
                }
    		}
    		
        } else if (media.getMediaTypeProperties() instanceof LtiProperties) {
            // nothing to do
        } else {
    
    		try{                        
    			String youtubeUrl = null;
    			youtubeUrl = LessonMaterialFileHandler.createEmbeddedYouTubeUrl(media.getUri(), media.getMediaTypeProperties());
    			if(youtubeUrl != null) {
    				media.setUri(youtubeUrl);
    			}
    		} catch (@SuppressWarnings("unused") Exception e){                                  
    			// skip YouTube URL conversion if the URI does not have a URL protocol or the URL does not match known YouTube URL conventions
    		}

            if(!media.getUri().contains(NETWORK_IDENTIFIER)){
                media.setUri(uriPrefix + UriUtil.makeURICompliant(media.getUri()));
            }
    	}

    }
    
    /**
     * Add an instance of lesson material list references to the current list of references.
     * 
     * @param lessonMaterialList course lesson material list information
     */
    public void addLessonMaterialList(generated.dkf.LessonMaterialList lessonMaterialList){
        
        if(lessonMaterialList != null){
            
        	lessonMaterial.setIsCollection(lessonMaterialList.getIsCollection());
        	
            for(Media media : lessonMaterialList.getMedia()){
                
                try{
                    handleMedia(media);
                    lessonMaterial.getMedia().add(media);
                }catch(ConnectException e){
                    Media errorMedia = new Media();
                    errorMedia.setName(media.getName());
                    errorMedia.setUri(media.getUri());
                    errorMedia.setMessage("There was a problem connecting to this content.  This is most likely due to not having an Internet connection at the moment.\n\nError:\n"+e.getLocalizedMessage());
                    errorMedia.setMediaTypeProperties(new WebpageProperties());
                    lessonMaterial.getMedia().add(errorMedia);
                }catch(Exception e){
                    Media errorMedia = new Media();
                    errorMedia.setName(media.getName());
                    errorMedia.setUri(media.getUri());
                    errorMedia.setMessage("There was a problem with this content.\n\nError:\n"+e.getLocalizedMessage());
                    errorMedia.setMediaTypeProperties(new WebpageProperties());
                    lessonMaterial.getMedia().add(errorMedia);
                }
            }
        }
    }


    /**
     * Gets the lesson material for a domain session
     *
     * @return LessonMaterial The lesson material for a domain session
     */
    public generated.dkf.LessonMaterialList getLessonMaterial() {
        return lessonMaterial;
    }
}
