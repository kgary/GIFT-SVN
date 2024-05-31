/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.python;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import mil.arl.gift.common.io.UserEnvironmentUtil;
import mil.arl.gift.common.python.ProcessStreamProcessor.Type;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Class to represent a connection to an XML RPC service.
 * 
 * Depending on constructor service may be started on local host.
 * 
 * @author cragusa
 */
public class XMLRPCPythonServerManager {
	
	 private static Logger logger = LoggerFactory.getLogger(XMLRPCPythonServerManager.class);
	 
	 public static final String DEFAULT_SERVER_BASE_URL = "localhost";
	 
	 /**
	 * Stream handler for the service process error stream
	 */
	private static ProcessStreamProcessor errStreamProcessor;	
	
	/**
	 * Stream handler for the service process output stream
	 */
	private static ProcessStreamProcessor outStreamProcessor;
	
	/**
	 * The xml rpc process (if the process was started by this instance).
	 * This may be null if the user is connecting to an existing (previously started) XML RPC server.
	 */
	private static Process serviceProcess;
	
	/**
	 * The GIFT path to the XML RPC Server script.
	 */
	private static final String PYTHON_XML_RPC_SERVER_SCRIPT = "src.py/xml.rpc.server/XmlRpcServer.py";
	
	private static final File PYTHON_XML_RPC_SERVER_SCRIPT_FILE = new File(PYTHON_XML_RPC_SERVER_SCRIPT);
	
	/**
	 * Method to launch XML RPC server on the local host. 
	 * 
	 * @param port port used by the server to listen for requests
	 * @param serviceClassname name of the class to be served by the XML RPC server
	 * @throws IOException if call to exec fails.
	 */
	public static void launchServer(int port, String serviceClassname) throws IOException {
		
		if(serviceProcess == null){
			
			String launchCommand = "python " + PYTHON_XML_RPC_SERVER_SCRIPT + " -p " + port + " -c " + serviceClassname;			
			
			if(logger.isInfoEnabled()){
			    logger.info("Launching XML RPC Service: '" + launchCommand + "'");
			}

			try{
			    serviceProcess = Runtime.getRuntime().exec(launchCommand);
			}catch(@SuppressWarnings("unused") Throwable t){ }
			
			if(serviceProcess == null){
			    
			    String pythonHome = UserEnvironmentUtil.getEnvironmentVariable(UserEnvironmentUtil.GIFT_PYTHON_HOME);
			    if(pythonHome != null){
			        
                    if(logger.isInfoEnabled()){
                        logger.info("Python is not on the path.  Trying to launch XML RPC Service using full path to python.exe");
                    }
                    
			        File pythonHomeDir = new File(pythonHome);
			        if(pythonHomeDir.exists() && pythonHomeDir.isDirectory()){
			            
			            //find path to python.exe
			            Collection<File> matchingFiles = FileUtils.listFiles(pythonHomeDir, new NameFileFilter("python.exe"), DirectoryFileFilter.DIRECTORY);
			            if(!matchingFiles.isEmpty()){
            			    String[] exeLaunchCommand =  {matchingFiles.toArray(new File[matchingFiles.size()])[0].getAbsolutePath(), PYTHON_XML_RPC_SERVER_SCRIPT_FILE.getAbsolutePath(),
            			            " -p " + port ," -c " + serviceClassname};            
            
            	            try{
            	                serviceProcess = Runtime.getRuntime().exec(exeLaunchCommand);
            	            }catch(Throwable t){
            	                throw new IOException("Failed to start python.  Tried each of the following commands:\n"+launchCommand+"\n"+exeLaunchCommand, t);
            	            }
			            }else{
			                throw new IOException("Failed to start python.  The "+UserEnvironmentUtil.GIFT_PYTHON_HOME+" user environment variable value of '"+pythonHome+" does not contain python.exe as a descendant file.");
			            }
			        }else{
			            throw new IOException("Failed to start python.  The "+UserEnvironmentUtil.GIFT_PYTHON_HOME+" user environment variable value of '"+pythonHome+" does not exist or is not a directory.");
			        }
			    }else{
			        //the python server is still not running
                    throw new IOException("Failed to start python.  Tried '"+launchCommand+"'.  Also failed to find the "+UserEnvironmentUtil.GIFT_PYTHON_HOME+" environment variable.  Please use the GIFT installer to help install Python.");
			    }
			}
		}
			
		if(errStreamProcessor == null){
			errStreamProcessor = new ProcessStreamProcessor(PYTHON_XML_RPC_SERVER_SCRIPT, serviceProcess.getErrorStream(), Type.ERROR);
			errStreamProcessor.start();
		}
		      
		if(outStreamProcessor == null){
			outStreamProcessor = new ProcessStreamProcessor(PYTHON_XML_RPC_SERVER_SCRIPT, serviceProcess.getInputStream(), Type.OUTPUT);
			outStreamProcessor.start();
		}

		//Make sure the process gets killed if the process is terminate 
		//(Note that shutdown hooks do not get called when process is terminated prematurely from from Eclipse)
		Runtime.getRuntime().addShutdownHook(new Thread("Python XML RPC Server Shutdown Thread") {
			@Override
			public void run() {
				logger.debug("running shutdown hook");
				XMLRPCPythonServerManager.destroyServiceProcess();				
			}
		});
	}
	
	/**
	 * Destroys the service process. 
	 * Optionally, call when done with XML RPS 
	 */
	public static void destroyServiceProcess() {	
		
		if(serviceProcess != null) {			
			serviceProcess.destroy();
			serviceProcess = null;
		}
		
		if(errStreamProcessor != null) {
			errStreamProcessor.shutdown();
			errStreamProcessor = null;
		}
		
		if(outStreamProcessor != null) {
			outStreamProcessor.shutdown();
			outStreamProcessor = null;
		}
	}
	
	/**
	 * Convenience method for test clients to print array of objects.
	 * 	
	 * @param result the array of objects to print.
	 */	
	public static void printResult(Object[] result) {
		System.out.print("(");		
		for(int i = 0; i < result.length; i++) {			
			System.out.print(result[i]);			
			if( i < (result.length - 1) ) {				
				System.out.print(",  ");
			}
		}
		System.out.println(")");		
	}
}
