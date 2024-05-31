/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.paradata;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.io.AbstractFolderProxy;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.domain.knowledge.metadata.ParadataUtil;

/**
 * Used to manage the updating of paradata files so that the updates are synchronous.
 * @author mhoffman
 *
 */
public class ParadataFileManager {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(ParadataFileManager.class);
    
    //TODO: release calling thread, queue up threads to be synchronized
    public static void updateParadataFiles(final Map<ParadataBean, String> paradataBeanToMetadataFile, final AbstractFolderProxy authoredCourseFolder){
        
        for(ParadataBean paradataBean : paradataBeanToMetadataFile.keySet()){
            
            String courseFolderRelativeMetadataFileId = paradataBeanToMetadataFile.get(paradataBean);
            try{
                // convert to paradata file name
                int replaceIndex = courseFolderRelativeMetadataFileId.lastIndexOf(AbstractSchemaHandler.METADATA_FILE_EXTENSION);
                String courseFolderRelativeParadataFileId = courseFolderRelativeMetadataFileId.substring(0, replaceIndex) + ParadataUtil.EXTENSION;
                
                if(!authoredCourseFolder.fileExists(courseFolderRelativeParadataFileId)){
                    //create it
                    ParadataCSVHandler.createParadataCSVFile(courseFolderRelativeParadataFileId, authoredCourseFolder);
                }
                FileProxy paradataFileProxy = authoredCourseFolder.getRelativeFile(courseFolderRelativeParadataFileId);
                ParadataCSVHandler handler = new ParadataCSVHandler(paradataFileProxy, authoredCourseFolder);
                handler.add(paradataBean);
                handler.write();
            }catch(Throwable t){
                logger.warn("There was a problem updating the paradata file for the metadata file of "+courseFolderRelativeMetadataFileId+".", t);
            }
        }
    }
}
