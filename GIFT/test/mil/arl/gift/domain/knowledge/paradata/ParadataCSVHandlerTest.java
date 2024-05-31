/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.paradata;

import java.io.File;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import mil.arl.gift.common.PackageUtil;
import mil.arl.gift.common.io.DesktopFolderProxy;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.domain.knowledge.metadata.ParadataUtil;
import mil.arl.gift.domain.knowledge.paradata.ParadataBean.PassedEnum;
import mil.arl.gift.domain.knowledge.paradata.ParadataBean.PhaseEnum;

/**
 * Used to test the reading and writing of paradata CSV files.
 * 
 * @author mhoffman
 *
 */
public class ParadataCSVHandlerTest {
    
    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    // Read paradata csv file created for this junit and confirm values in it
    @Test
    public void ReadParadataCSVTest() throws Exception {
        
        File curFile = new File(System.getProperty("user.dir"));

        String resourceFileName = PackageUtil.getTestData() + File.separator + "example2.resource";
        File resourceFile = new File(curFile, resourceFileName);

        if (!resourceFile.exists()) {
            throw new Exception("The resource file " + resourceFile.getAbsolutePath() + " can't be found.");
        }

        File relativeInitPath = new File(PackageUtil.getTestData());
        FileProxy file = ParadataUtil.getParadataFileForResource(new FileProxy(resourceFile), new DesktopFolderProxy(new File(relativeInitPath.getAbsolutePath())));
        System.out.println("Found paradata file of " + file.getFileId() + " from resource file of " + resourceFile.getAbsolutePath());
        
        File folder = new File(curFile, PackageUtil.getTestData());
        DesktopFolderProxy folderProxy = new DesktopFolderProxy(folder);

        System.out.println("loading " + file.getFileId() + "\n");
        ParadataCSVHandler handler = new ParadataCSVHandler(file, folderProxy);
        
        List<ParadataBean> paradataBeans = handler.getData();
        
        Assert.assertTrue("The paradata file is empty", !paradataBeans.isEmpty());
        Assert.assertEquals("The date on row 1 is not correct", paradataBeans.get(0).getDate(), 1234567890);
        Assert.assertEquals("The passed enum on row 2 is not correct", paradataBeans.get(1).getPassed(), PassedEnum.y);

        System.out.println("\nGood-bye.");
    }
    
    // Read the paradata csv file, update it, write it and reread the data to confirm the update happened
    @Test
    public void WriteUpdateParadataCSVTest() throws Exception{
        
        File curFile = new File(System.getProperty("user.dir"));

        String resourceFileName = PackageUtil.getTestData() + File.separator + "example2.resource";
        File resourceFile = new File(curFile, resourceFileName);

        if (!resourceFile.exists()) {
            throw new Exception("The resource file " + resourceFile.getAbsolutePath() + " can't be found.");
        }

        File relativeInitPath = new File(PackageUtil.getTestData());
        FileProxy file = ParadataUtil.getParadataFileForResource(new FileProxy(resourceFile), new DesktopFolderProxy(new File(relativeInitPath.getAbsolutePath())));
        System.out.println("Found paradata file of " + file.getFileId() + " from resource file of " + resourceFile.getAbsolutePath());
        
        File folder = new File(curFile, PackageUtil.getTestData());
        DesktopFolderProxy folderProxy = new DesktopFolderProxy(folder);

        System.out.println("loading " + file.getFileId() + "\n");
        ParadataCSVHandler handler = new ParadataCSVHandler(file, folderProxy);
        
        long now = System.currentTimeMillis();
        ParadataBean newParadataBean = new ParadataBean(handler.getNextId(), now, PhaseEnum.K, 101, PassedEnum.y, "{TEST1:ABC},{TEST2:XYZ}");
        handler.add(newParadataBean);
        
        handler.write();
        
        handler = new ParadataCSVHandler(file, folderProxy); 
        
        List<ParadataBean> paradataBeans = handler.getData();
        Assert.assertEquals("The added row's date value is not correct", paradataBeans.get(paradataBeans.size()-1).getDate(), now);
    }
    
    // Create a new paradata csv file and confirm it was created
    @Test
    public void CreateParadataCSVTest() throws Exception{
        
        String folderName = "temp";
        String fileName = "test.paradata";
        
        File createdFile = null;
        File folder = new File(folderName);
        DesktopFolderProxy folderProxy = new DesktopFolderProxy(folder);
        try{
            FileProxy paradataFileProxy = ParadataCSVHandler.createParadataCSVFile(fileName, folderProxy);
            
            createdFile = new File(folderName + File.separator + fileName);
            Assert.assertTrue("Failed to create the test paradata file", createdFile.exists());
            
            // read the paradata file to make sure it has the header and now rows
            ParadataCSVHandler handler = new ParadataCSVHandler(paradataFileProxy, folderProxy);
            Assert.assertTrue(handler.getData().isEmpty());
        }finally{
        
            // cleanup - delete the created paradata file
            if(createdFile != null){
                createdFile.delete();
            }
        }
    }

}
