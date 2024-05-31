/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.aar;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.common.io.AbstractFolderProxy;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.DesktopFolderProxy;
import mil.arl.gift.common.io.FileFinderUtil;
import mil.arl.gift.common.io.FileProxy;

/**
 * Contains suite of tests for the video metadata file (.vmeta.xml).
 * @author mhoffman
 *
 */
@Ignore // ignoring this because it uses a hard coded course folder that doesn't exist in every GIFT instance
public class VideoMetadataFileHandlerTest {

    /**
     * Search a directory for .vmeta.xml files, parse them and determine if any have
     * a space metadata file reference.
     * @throws IOException if there was an issue searching the directory for vmeta.xml files
     */
    @Test
    public void FindSpaceReferencesTest() throws IOException {
        
        File startFolder = new File("../Domain/workspace/Public/VProcessing.STEELR.BattleDrill6a.Playback.Option 1.Demo");
        AbstractFolderProxy startingDirectory = new DesktopFolderProxy(startFolder);
        
        List<FileProxy> files = new ArrayList<>();
        FileFinderUtil.getFilesByExtension(startingDirectory, files, AbstractSchemaHandler.VIDEO_FILE_EXTENSION);
        
        boolean error = false;
        for(FileProxy vmetaFile : files){
            // for each vmeta.xml file, parse it and get the necessary data to send to the video processing engine
            
            try{
                VideoMetadataFileHandler handler = new VideoMetadataFileHandler(vmetaFile, startFolder, true);
                VideoMetadata vMeta = handler.getVideoMetadata();
                if(StringUtils.isNotBlank(vMeta.getSpaceMetadataFile())){
                    System.out.println("Found '"+vmetaFile.getName()+"' has space metadata reference.");
                }
            }catch(Exception e){
                System.out.println(vmetaFile.getName());
                e.printStackTrace();
                error = true;
            }
            
        }
        
        if(error){
            Assert.fail("At least one found vmeta.xml file failed to parse.");
        }
        
        System.out.println("Finished 'FindSpaceReferencesTest'");
    }

}
