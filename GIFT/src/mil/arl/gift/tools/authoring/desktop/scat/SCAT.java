/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.desktop.scat;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import javax.swing.JOptionPane;

import mil.arl.gift.common.PackageUtil;
import mil.arl.gift.common.io.DetailedException;

import org.apache.log4j.PropertyConfigurator;

/**
 * This class is the entry point for the sensor configuration authoring tool (SCAT).
 * 
 * @author mhoffman
 *
 */
public class SCAT {
    
    static {
        //use SCAT log4j
        PropertyConfigurator.configureAndWatch(PackageUtil.getConfiguration() + "/tools/authoring/scat/scat.log4j.properties");
    }  
    
    /** the main window for this tool */
    private SCATForm scatForm;
    
    /** singleton instance of this class */
    private static SCAT instance = null;
    
    /**
     * Return the singleton instance of this class
     * 
     * @return SCAT
     * @throws DetailedException if there was a problem with the sensor configuration schema file
     */
    public static SCAT getInstance() throws DetailedException{
        
        if(instance == null){
            instance = new SCAT();
        }
        
        return instance;
    }
    
    /**
     * Class constructor - build dialog
     * @throws DetailedException if there was a problem with the sensor configuration schema file
     */
    private SCAT() throws DetailedException{        

        String courseSchema = SCATProperties.getInstance().getSchemaFilename();
        if(courseSchema != null){
            scatForm = new SCATForm(new File(courseSchema));
        }else{
            scatForm  = new SCATForm();
        }
    }
    
    /**
     * Initialize the tool's dialog and display it
     */
    public void init(){
        scatForm.init();
    }
    
    /**
     * Dispose the tool's dialog
     */
    public void dispose(){
        scatForm.close();
    }
    
    /**
     * Get the tool's main window
     * 
     * @return SCATForm
     */
    public SCATForm getSCATForm(){
        return scatForm;
    }

    /**
     * Instantiate the SCAT dialog
     * 
     * @param args - not used
     */
    public static void main(String[] args) {

        SCAT scat = null;
        
        try{  
            scat = SCAT.getInstance();
            
            scat.init();
        
            showStartedPrompt();
        
            //dispose after user has decided to close via terminal window
            scat.dispose();
            
        }catch(Throwable e){
            e.printStackTrace();
            
            if(scat != null){
                //close the dialog to force user to look at minimized terminal window for more information
                scat.dispose();
            }
            
            JOptionPane.showMessageDialog(null,
                    "The Sensor Configuration Authoring Tool closed unexpectedly.  Check the log file and the console window for more information.",
                    "Sensor Configuration Authoring Tool Error",
                    JOptionPane.ERROR_MESSAGE);
            
            showModuleUnexpectedExitPrompt();
        }
        
        System.out.println("Good-bye");
    }
    
    /**
     * Show a message indicating the SCAT has launched and prompt for user to exit the application
     */
    protected static void showStartedPrompt(){

        BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));

        try {
            System.out.println("SCAT is running, check log for more details");
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
    
    /**
     * Show a common module unexpected exit prompt and then exit.
     */
    protected static void showModuleUnexpectedExitPrompt(){
        
        BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
        
        try {
            System.out.println("The tool has exited, check prompt and log for more details");
            String input = null;
            do {
                System.out.print("Press Enter to Exit");
                input = inputReader.readLine();

            } while (input.length() != 0);

        } catch (Exception e) {

            System.err.println("Caught exception while reading input: \n");
            e.printStackTrace();
        }
        
        System.out.println("Good-bye");
        //Instead of using the usual error code 1 here, GIFT uses an arbitrary 101 exit code which is checked for
        //in scripts/runCommand.bat
        System.exit(101);       
    }

}
