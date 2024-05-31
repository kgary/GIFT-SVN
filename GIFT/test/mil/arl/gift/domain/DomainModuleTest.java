/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain;

import static org.junit.Assert.assertNotNull;

import java.io.File;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import mil.arl.gift.common.io.DesktopFolderProxy;
import mil.arl.gift.common.io.FileProxy;

/**
 * A jUnit test for the DomainModuleTest
 *
 * @author jleonard
 */
public class DomainModuleTest {

    /** path to a domain knowledge file (dkf) */
    private static final String DOMAIN_KNOWLEDGE_FILE = ".." + File.separator + "Domain" + File.separator + "workspace" + File.separator + "templates" + File.separator + "simplest.dkf.xml";

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

    @Test
    public void testConstructor() {

        try{
            //test parsing the dkf file
            DesktopFolderProxy courseFolder = new DesktopFolderProxy(new File("../Domain"));
            DomainDKFHandler dkfh = new DomainDKFHandler(new FileProxy(new File(DOMAIN_KNOWLEDGE_FILE)), courseFolder, null, true);
    
            assertNotNull(dkfh);
        }catch(Throwable e){
            e.printStackTrace();
            Assert.fail("Failed to parse the DKF of "+DOMAIN_KNOWLEDGE_FILE+".");
        }
    }
}
