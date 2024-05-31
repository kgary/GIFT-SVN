/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.desktop.pcat;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import javax.swing.JOptionPane;

import mil.arl.gift.common.PackageUtil;
import mil.arl.gift.common.io.DetailedException;

import org.apache.log4j.PropertyConfigurator;

/**
 * This class is the entry point for the pedagogy configuration authoring tool (PCAT).
 * 
 * @author mhoffman
 *
 */
public class PCAT {
    
    static {
        //use LCAT log4j
        PropertyConfigurator.configureAndWatch(PackageUtil.getConfiguration() + "/tools/authoring/pcat/pcat.log4j.properties");
    }  
    
    /** the main window for this tool */
    private PCATForm form;
    
    /** singleton instance of this class */
    private static PCAT instance = null;
    
    /**
     * Return the singleton instance of this class
     * 
     * @return PCAT
     * @throws DetailedException if there was a problem with the pedagogy configuration file schema
     */
    public static PCAT getInstance() throws DetailedException{
        
        if(instance == null){
            instance = new PCAT();
        }
        
        return instance;
    }
    
    /**
     * Class constructor - build dialog
     * @throws DetailedException if there was a problem with the pedagogy configuration file schema
     */
    private PCAT() throws DetailedException{        

        String courseSchema = PCATProperties.getInstance().getSchemaFilename();
        if(courseSchema != null){
            form = new PCATForm(new File(courseSchema));
        }else{
            form  = new PCATForm();
        }
    }
    
    /**
     * Initialize the tool's dialog and display it
     */
    public void init(){
        form.init();
    }
    
    /**
     * Dispose the tool's dialog
     */
    public void dispose(){
        form.close();
    }
    
    /**
     * Get the tool's main window
     * 
     * @return PCATForm
     */
    public PCATForm getPCATForm(){
        return form;
    }

    /**
     * Instantiate the PCAT dialog
     * 
     * @param args - not used
     */
    public static void main(String[] args) {

        PCAT pcat = null;
        
        try{  
            pcat = PCAT.getInstance();
            
            pcat.init();
        
            showStartedPrompt();            
            
            //dispose after user has decided to close via terminal window
            pcat.dispose();
            
        }catch(Throwable e){
            e.printStackTrace();
            
            if(pcat != null){
                //close the dialog to force user to look at minimized terminal window for more information
                pcat.dispose();
            }
            
            JOptionPane.showMessageDialog(null,
                    "The Pedagogy Configuration Authoring Tool closed unexpectedly.  Check the log file and the console window for more information.",
                    "Pedagogy Configuration Authoring Tool Error",
                    JOptionPane.ERROR_MESSAGE);
            
            showUnexpectedExitPrompt();
        }
        
        System.out.println("Good-bye");
    }
    
    /**
     * Show a message indicating the PCAT has launched and prompt for user to exit the application
     */
    protected static void showStartedPrompt(){

        BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));

        try {
            System.out.println("PCAT is running, check log for more details");
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
     * Show an unexpected exit prompt and then exit.
     */
    protected static void showUnexpectedExitPrompt(){
        
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
