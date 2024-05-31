/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.desktop.cat;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import javax.swing.JOptionPane;

import mil.arl.gift.common.PackageUtil;
import mil.arl.gift.common.io.DetailedException;

import org.apache.log4j.PropertyConfigurator;

/**
 * This class is the entry point for the course authoring tool (CAT).
 * 
 * @author mhoffman
 *
 */
public class CAT {
    
    static {
        //use CAT log4j
        PropertyConfigurator.configureAndWatch(PackageUtil.getConfiguration() + "/tools/authoring/cat/cat.log4j.properties");
    }  
    
    /** the main window for this tool */
    private CATForm catForm;
    
    /** singleton instance of this class */
    private static CAT instance = null;
    
    /**
     * Return the singleton instance of this class
     * 
     * @return CAT
     * @throws DetailedException if there was a problem with the course schema file
     */
    public static CAT getInstance() throws DetailedException{
        
        if(instance == null){
            instance = new CAT();
        }
        
        return instance;
    }
    
    /**
     * Class constructor - build dialog
     * @throws DetailedException if there was a problem with the course schema file
     */
    private CAT() throws DetailedException{        

        String courseSchema = CATProperties.getInstance().getSchemaFilename();
        if(courseSchema != null){
            catForm = new CATForm(new File(courseSchema));
        }else{
            catForm  = new CATForm();
        }
    }
    
    /**
     * Initialize the tool's dialog and display it
     * @throws Throwable if there was a problem connecting to the UMS database
     */
    public void init() throws Throwable{
        catForm.init();
    }
    
    /**
     * Dispose the tool's dialog
     */
    public void dispose(){
        catForm.close();
    }
    
    /**
     * Get the tool's main window
     * 
     * @return CATForm
     */
    public CATForm getCATForm(){
        return catForm;
    }

    /**
     * Instantiate the CAT dialog
     * 
     * @param args - not used
     */
    public static void main(String[] args) {        
        
        CAT cat = null;
        
        try{  
            cat = CAT.getInstance();
            
            cat.init();
        
            showStartedPrompt();            
            
            //dispose after user has decided to close via terminal window
            cat.dispose();
            
        }catch(Throwable e){
            e.printStackTrace();
            
            if(cat != null){
                //close the dialog to force user to look at minimized terminal window for more information
                cat.dispose();
            }
            
            JOptionPane.showMessageDialog(null,
                    "The Course Authoring Tool closed unexpectedly.  Check the log file and the console window for more information.",
                    "Course Authoring Tool Error",
                    JOptionPane.ERROR_MESSAGE);
            
            showModuleUnexpectedExitPrompt();
        }
        
        System.out.println("Good-bye");
    }
    
    /**
     * Show a message indicating the CAT has launched and prompt for user to exit the application
     */
    protected static void showStartedPrompt(){

        BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));

        try {
            System.out.println("CAT is running, check log for more details");
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
