/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.gat.server.survey;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.Test;

import mil.arl.gift.tools.authoring.server.gat.server.survey.QualtricsImport;

/**
 * Tests the Qualtrics survey export file (.qsf) import logic in GIFT.
 * 
 * @author mhoffman
 *
 */
public class QualtricsImportTest {
    
    private static final File testFile = new File("data" + File.separator + "tests" + File.separator + "Qualtrics_Fully_populated_export.qsf");

    @Test
    public void testQualtricsImport() throws IOException{
        
        byte[] encoded = Files.readAllBytes(testFile.toPath());
        QualtricsImport.importQsf(new String(encoded));
    }
}
