/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.desktop.lcat;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import mil.arl.gift.common.PackageUtil;
import mil.arl.gift.common.io.DetailedException;

import org.apache.log4j.PropertyConfigurator;

/**
 * This class is the entry point for the learner configuration authoring tool (LCAT).
 * 
 * @author mhoffman
 *
 */
public class LCAT {
    
    static {
        //use LCAT log4j
        PropertyConfigurator.configureAndWatch(PackageUtil.getConfiguration() + "/tools/authoring/lcat/lcat.log4j.properties");
    }    

    /**
     * Instantiate the LCAT dialog
     * 
     * @param args - not used
     * @throws DetailedException if there was a problem with the learner configuration file schema
     */
    public static void main(String[] args) throws DetailedException {

        LCATForm lcat;
        String schemaFile = LCATProperties.getInstance().getSchemaFilename();
        if(schemaFile != null){
            lcat = new LCATForm(new File(schemaFile));
        }else{
            lcat = new LCATForm();
        }
        
        lcat.setVisible(true);
        
        showStartedPrompt();
        
        lcat.dispose();
        
        System.out.println("Good-bye");
    }
    
    /**
     * Show a message indicating the LCAT has launched and prompt for user to exit the application
     */
    protected static void showStartedPrompt(){

        BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));

        try {
            System.out.println("LCAT is running, check log for more details");
            String input = null;
            do {
                System.out.print("Press Enter to Exit\n");
                input = inputReader.readLine();

            } while (input.length() != 0);

        } catch (Exception e) {

            System.err.println("Caught exception while reading input: \n");
            e.printStackTrace();
        }
    }

    private LCAT() {
    }
}
