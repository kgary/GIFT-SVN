/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.metadata;

import java.util.Collection;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.io.AbstractFolderProxy;
import mil.arl.gift.common.io.FileProxy;

/**
 * This class provides logic to help sort through paradata files including how to select the best resource based
 * on paradata file content.
 * 
 * @author mhoffman
 *
 */
public class ParadataUtil {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(ParadataUtil.class);
    
    /** file extension for paradata files */
    public static final String EXTENSION = ".paradata";
    
    /** used to randomly select the best paradata file among a provided collection */
    private static final Random randomBest = new Random();
    
    /**
     * Return the paradata file instance for the resource file specified.  Basically this adds the paradata extension to
     * the resource file's name.<br/>
     * 
     * @param resource - the resource to use as a prefix for the paradata file name, e.g. "Test.pptx".  Can't be null and must exist.
     * @param courseFolder the course folder that contains all course relevant files.  Can't be null.
     * @return File - the paradata file.  Can be null if there is no paradata.
     * @throws IllegalArgumentException - if there is a problem with the parameters specified
     */
    public static FileProxy getParadataFileForResource(FileProxy resource, AbstractFolderProxy courseFolder) throws IllegalArgumentException{
        
        if(resource == null){
            throw new IllegalArgumentException("The resource file can't be null.");
        }else if(courseFolder == null){
            throw new IllegalArgumentException("The course folder can't be null.");
        }

        try{
            String filename = courseFolder.getRelativeFileName(resource);
            String paradataFileName = filename + EXTENSION;
            return courseFolder.getRelativeFile(paradataFileName);
        }catch(@SuppressWarnings("unused") Exception e){
            //paradata file doesn't exist or isn't accessible
        }
     
        return null;
    }
    
    /**
     * Return the "best" resource file by looking at the paradata for each resource file.
     * If none of the files have appropriate paradata, one of the files will still be selected.
     * 
     * @param resourceFiles - collection of resource files to select the "best" one from
     * @param courseFolder the course folder that contains all course relevant files
     * @return FileProxy - the "best" resource file.  Can be null if no resource files were provided.
     */
    public static FileProxy selectBest(Collection<FileProxy> resourceFiles, AbstractFolderProxy courseFolder){
        
        FileProxy bestResourceFile = null;
        
        // #4473 - improved paradata file format, need to replace the following logic with a reinforcement machine learning algorithm
//        int highestPrecedence = Integer.MIN_VALUE;
//        
//        for(FileProxy resourceFile : resourceFiles){
//            
//            try{
//                FileProxy paradata = ParadataFileHandler.getParadataFileForResource(resourceFile, courseFolder);
//                if(paradata != null){
//                    ParadataFileHandler handler = new ParadataFileHandler(paradata);
//                    
//                    if(handler.getPrecedence() > highestPrecedence){
//                        //found higher precedence resource
//                        
//                        bestResourceFile = resourceFile;
//                        highestPrecedence = handler.getPrecedence();
//                    }
//                }
//                
//            }catch(Exception e){
//                if(logger.isInfoEnabled()){
//                    logger.info("Skipping resource file "+resourceFile.getFileId()+" because caught exception while trying to get the paradata file", e);
//                }
//            }
//            
//        }
        
        if(bestResourceFile == null && !resourceFiles.isEmpty()){
            //just select a random one from the list
            int index = randomBest.nextInt(resourceFiles.size());
            bestResourceFile = (FileProxy) resourceFiles.toArray()[index];
            
            if(logger.isInfoEnabled()){
                logger.info("Unable to select the 'best' file from "+resourceFiles.size()+" choices based on paradata.  "+
                        "Therefore selecting the random index of "+index+" which is "+bestResourceFile+".");
            }
        }
        
        return bestResourceFile;
    }
}
