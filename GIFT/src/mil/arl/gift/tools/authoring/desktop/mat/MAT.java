/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.desktop.mat;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import javax.swing.JOptionPane;

import mil.arl.gift.common.PackageUtil;
import mil.arl.gift.common.io.DetailedException;

import org.apache.log4j.PropertyConfigurator;

/**
 * This class is the entry point for the metadata authoring tool (MAT).
 * 
 * @author mhoffman
 *
 */
public class MAT {
    
    static {
        //use LCAT log4j
        PropertyConfigurator.configureAndWatch(PackageUtil.getConfiguration() + "/tools/authoring/mat/mat.log4j.properties");
    }  
    
    /** the main window for this tool */
    private MATForm form;
    
    /** singleton instance of this class */
    private static MAT instance = null;
    
    /**
     * Return the singleton instance of this class
     * 
     * @return MAT
     * @throws DetailedException if there was a problem with the metadata file schema
     */
    public static MAT getInstance() throws DetailedException{
        
        if(instance == null){
            instance = new MAT();
        }
        
        return instance;
    }
    
    /**
     * Class constructor - build dialog
     * @throws DetailedException if there was a problem with the metadata file schema
     */
    private MAT() throws DetailedException{        

        String courseSchema = MATProperties.getInstance().getSchemaFilename();
        if(courseSchema != null){
            form = new MATForm(new File(courseSchema));
        }else{
            form  = new MATForm();
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
     * @return MATForm
     */
    public MATForm getMATForm(){
        return form;
    }

    /**
     * Instantiate the MAT dialog
     * 
     * @param args - not used
     */
    public static void main(String[] args) {

        MAT mat = null;
        
        try{  
            mat = MAT.getInstance();
            
            mat.init();
        
            showStartedPrompt();            
            
            //dispose after user has decided to close via terminal window
            mat.dispose();
            
        }catch(Throwable e){
            e.printStackTrace();
            
            if(mat != null){
                //close the dialog to force user to look at minimized terminal window for more information
                mat.dispose();
            }
            
            JOptionPane.showMessageDialog(null,
                    "The Metadata Authoring Tool closed unexpectedly.  Check the log file and the console window for more information.",
                    "Metadata Authoring Tool Error",
                    JOptionPane.ERROR_MESSAGE);
            
            showUnexpectedExitPrompt();
        }
        
        System.out.println("Good-bye");
    }
    
    /**
     * Show a message indicating the MAT has launched and prompt for user to exit the application
     */
    protected static void showStartedPrompt(){

        BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));

        try {
            System.out.println("MAT is running, check log for more details");
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
