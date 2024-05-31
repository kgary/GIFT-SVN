/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;


/**
 * Developer tool that semi-automates creation of 
 * Action/Result classes and associated code.
 *
 * @author cragusa
 */
class ActionGenerator {
    
    /** The Constant ACTION_TEMPLATE_FILE. */
    static final String ACTION_TEMPLATE_FILE  = "data/code.templates/ActionTemplate.txt";
    
    /** The Constant RESULT_TEMPLATE_FILE. */
    static final String RESULT_TEMPLATE_FILE  = "data/code.templates/ResultTemplate.txt";
    
    /** The Constant HANDLER_TEMPLATE_FILE. */
    static final String HANDLER_TEMPLATE_FILE = "data/code.templates/HandlerTemplate.txt";
    
    /** The Constant ASYNC_CALLBACK_TEMPLATE_FILE. */
    static final String ASYNC_CALLBACK_TEMPLATE_FILE = "data/code.templates/AsyncCallbackTemplate.txt";

    /** The Constant PACKAGE_NAME. */
    static final String PACKAGE_NAME         = "mil.arl.gift.tools.authoring.gat.shared.action.dkf";
    
    /** The Constant HANDLER_PACKAGE_NAME. */
    static final String HANDLER_PACKAGE_NAME = "mil.arl.gift.tools.authoring.gat.server.handler.dkf";
         
    /** The output writer. */
    private BufferedWriter outputWriter = new BufferedWriter(new OutputStreamWriter(System.out));  
    
    /**
     * Replace tokens.
     *
     * @param line the line
     * @param actionName the action name
     * @return the string
     */
    String replaceTokens(String line, String actionName) {
        
        line = line.replace("%ACTION_NAME%", actionName);
        line = line.replace("%HANDLER_PACKAGE_NAME%", HANDLER_PACKAGE_NAME); //must come before "PACKAGE_NAME"
        line = line.replace("%PACKAGE_NAME%", PACKAGE_NAME);
        
        return line;
    }
    
    /**
     * Generate.
     *
     * @param reader the reader
     * @param writer the writer
     * @param actionName the action name
     * @throws IOException Signals that an I/O exception has occurred.
     */
    void generate(BufferedReader reader, BufferedWriter writer, String actionName) throws IOException {
        
        String line = "";
        
        while (( line = reader.readLine()) != null) {            
            line = replaceTokens(line, actionName);            
            writer.write(line);
            writer.newLine();
        }
    }
    
    /**
     * Generate.
     *
     * @param templateFile the template file
     * @param actionName the action name
     * @throws IOException Signals that an I/O exception has occurred.
     */
    void generate(String templateFile, String actionName) throws IOException { 
        
        BufferedReader templateReader = new BufferedReader(new FileReader(templateFile));             
        generate(templateReader, outputWriter, actionName);        
        templateReader.close();  
    }
    
    /**
     * Cleanup.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    void cleanup() throws IOException {
    	outputWriter.flush();
    	outputWriter.close();
    }
    
    
    /**
     * Generate.
     *
     * @param templateFile the template file
     * @param packageName the package name
     * @param actionName the action name
     * @param filenameSuffix the filename suffix
     * @throws IOException Signals that an I/O exception has occurred.
     */
    void generate(String templateFile, String packageName, String actionName, String filenameSuffix) throws IOException {        
        
        BufferedReader templateReader = new BufferedReader(new FileReader(templateFile));        
        
        String outputFilename = packageToPath(packageName)  + "/" + actionName + filenameSuffix;        
        File file = new File(outputFilename);        
        if(file.exists()) {            
            System.out.println("File already exits: " + file.getAbsolutePath());
            System.out.println("skipping...");            
        } else {        	
            file.createNewFile();
            BufferedWriter outputWriter = new BufferedWriter(new FileWriter(file));      
            generate(templateReader, outputWriter, actionName);        
            templateReader.close();
            outputWriter.close();            
        }
    }
    
    /**
     * Package to path.
     *
     * @param pkg the pkg
     * @return the string
     */
    String packageToPath(String pkg) {
        return "src/" + pkg.replace('.', '/');        
    }
    
    /**
     * Generate.
     *
     * @param actionName the action name
     * @throws IOException Signals that an I/O exception has occurred.
     */
    void generate(String actionName) throws IOException {
        
        generate(ACTION_TEMPLATE_FILE, PACKAGE_NAME,          actionName, ".java");
        generate(RESULT_TEMPLATE_FILE, PACKAGE_NAME,          actionName, "Result.java");
        generate(HANDLER_TEMPLATE_FILE, HANDLER_PACKAGE_NAME, actionName, "Handler.java");        
        generate(ASYNC_CALLBACK_TEMPLATE_FILE, actionName);
    }
    
    /**
     * Generate bindings.
     *
     * @param actionName the action name
     * @throws IOException Signals that an I/O exception has occurred.
     */
    void generateBindings(String actionName) throws IOException {
    	
    	outputWriter.newLine();
    	String line = "bindHandler(%ACTION_NAME%.class, %ACTION_NAME%Handler.class);";    	
    	line = replaceTokens(line, actionName);
    	outputWriter.write(line);
    }
    
    /**
     * The main method.
     *
     * @param args the arguments
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void main(String[] args) throws IOException {
    	
    	ActionGenerator generator = new ActionGenerator();
    	
    	String[] actionNames = {
    			"FetchDkfPerfNodes",
    	};
    	
    	for (int i = 0; i < actionNames.length; i++ ) {
    		generator.generate(actionNames[i]);
    	}  
    	
    	for (int i = 0; i < actionNames.length; i++ ) {
    		generator.generateBindings(actionNames[i]);
    	} 
    	
    	generator.cleanup();
    }
}
