/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.gateway.interop.ppt;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jna.platform.win32.Msi;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.ptr.IntByReference;

import mil.arl.gift.common.ConfigurationException;
import mil.arl.gift.common.PackageUtil;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileFinderUtil;
import mil.arl.gift.common.io.UserEnvironmentUtil;
import mil.arl.gift.common.io.WindowsRegistryUtil;
import mil.arl.gift.gateway.GatewayModuleProperties;
import mil.arl.gift.gateway.installer.TrainingApplicationInstallPage;

/**
 * This class provides methods to find office applications (e.g. PowerPoint) on this machine.
 * 
 * @author mhoffman
 *
 */
public class FindOfficeInstallation {
    
    /** Instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(FindOfficeInstallation.class);
    
    /** the key to look for in a successful read of the windows registry */
    private static final String REG_SZ = "REG_SZ";
    
    private static final String MSPPT_OLB = "Msppt.olb";
    
    /**
     * Parse the file containing the various PowerPoint GUIDs
     */
    private static final String PPT_GUIDS_FILENAME = PackageUtil.getConfiguration() + File.separator + "gateway" + File.separator +
            "externalApplications" +File.separator + "PowerPoint" + File.separator + "PowerPointGUIDs.txt";
    private static final Properties guids = new Properties();
    static{
        try {
            
            InputStream iStream = FileFinderUtil.getFileByClassLoader(PPT_GUIDS_FILENAME);
            if(iStream == null){
                iStream = new FileInputStream(new File(PPT_GUIDS_FILENAME));
            }
            guids.load(iStream);
            
        } catch (FileNotFoundException e) {
            logger.error("Caught exception while trying to load the PowerPoint GUIDs file of "+PPT_GUIDS_FILENAME+".", e);
            e.printStackTrace();
        } catch (IOException e) {
            logger.error("Caught exception while trying to load the PowerPoint GUIDs file of "+PPT_GUIDS_FILENAME+".", e);
            e.printStackTrace();
        }
    }
    
    /**
     * Parse the file containing the various PowerPoint GUIDs
     */
    private static final String PPT_REGISTRY_FILENAME = PackageUtil.getConfiguration() + File.separator + "gateway" + File.separator + 
            "externalApplications" +File.separator + "PowerPoint" + File.separator + "PowerPointRegistryPaths.txt";
    private static final Properties registryPaths = new Properties();
    static{
        try {
            InputStream iStream = FileFinderUtil.getFileByClassLoader(PPT_REGISTRY_FILENAME);
            if(iStream == null){
                iStream = new FileInputStream(new File(PPT_REGISTRY_FILENAME));
            }
            registryPaths.load(iStream);
            
        } catch (FileNotFoundException e) {
            logger.error("Caught exception while trying to load the PowerPoint Registry Paths file of "+PPT_REGISTRY_FILENAME+".", e);
            e.printStackTrace();
        } catch (IOException e) {
            logger.error("Caught exception while trying to load the PowerPoint Registry Paths file file of "+PPT_REGISTRY_FILENAME+".", e);
            e.printStackTrace();
        }
    }
    
    /**
     * Returns a collection of file names for the PowerPoint application(s) installed on this machine.
     * Example: "C:\Program Files (x86)\Microsoft Office\Office14\POWERPNT.EXE" for Powerpoint 2010 32bit installed on Windows 7 64bit.
     * Note: some version of PP (C2R) can return incorrect files paths and the registry must be used instead, invalid paths
     * 	will be stripped away later. 
     * 
     * @return Map<String, String> the found applications {key: application name, value: path to application}.  
     *                              Note: the collection is empty if PowerPoint was not found on this machine.
     * @throws IOException if there was a problem searching for the PPT applications.
     * @throws DetailedException if there was a problem loading the Msi library used to locate an installed instance of
     * Microsoft PowerPoint.
     */
    public static Map<String, String> findPowerPoint() throws IOException, DetailedException{
        
        if(guids.isEmpty()){
            throw new IOException("There are no PowerPoint GUIDs.  Please check the console and the appropriate log for more details.");
        }
        
        Map<String, String> pathsFound = new HashMap<>();
        
        if(logger.isDebugEnabled()){
            logger.debug("Looking through "+guids.size()+" GUIDs.");
        }

        //
        // Use GUIDs to find PowerPoint
        //
        for(Object pptName : guids.keySet()){
            
            String guid = (String) guids.get(pptName);
            
            String path = getPathToApplication(guid);
            
            if(path != null && !path.isEmpty()){
            	if(pathsFound.containsKey(pptName)){
            		logger.warn("The path to \"" + pptName + "\" has already been found and the path will be changed from \"" + pathsFound.get(pptName)
            				+ "\" to the new path \"" + path + "\".");
            	}
            	if(logger.isDebugEnabled()){
            	    logger.debug("Found PowerPoint application using GUID "+guid+" ("+pptName+"), which resulted in the type library path of '"+path+"'.");
            	}
                pathsFound.put((String) pptName, path);
            }
        }
        
        if(logger.isDebugEnabled()){
            logger.debug("Looking through "+registryPaths.size()+" registry paths.");
        }
        
        //
        // Use Registry to find PowerPoint
        //           
        for(Object pptName : registryPaths.keySet()){
            
            String regPath = (String) registryPaths.get(pptName);
            
            String path = readRegistry(regPath);
            
            if(path != null && !path.isEmpty()){
            	if(pathsFound.containsKey(pptName)){
            		logger.warn("The path to \"" + pptName + "\" has already been found and the path will be changed from \"" + pathsFound.get(pptName)
            				+ "\" to the new path \"" + path + "\".");
            	}
                if(logger.isDebugEnabled()){
                    logger.debug("Found PowerPoint application using registry path of "+regPath+" ("+pptName+"), which resulted in the type library path of '"+path+"'.");
                }
                pathsFound.put((String) pptName, path);
            }
        }
            
        
        return pathsFound;
    }
    
    /**
     * Find the path to the application executable using MsiLocateComponent and the GUID provided.
     * Reference: http://support.microsoft.com/kb/234788
     * 
     * @param guid the GUID of a microsoft application (e.g. PowerPoint GUID).  
     * @return String the absolute path to the application exe file.  If the application is not installed an empty string will be returned.
     * @throws DetailedException if there was a problem loading the Msi library used to locate an installed instance of
     * Microsoft PowerPoint.
     */
    private static String getPathToApplication(String guid) throws DetailedException{
        
        if(guid == null || guid.isEmpty()){
            throw new IllegalArgumentException("The guid must contain characters");
        }
        
        char[] componentBuffer = guid.toCharArray();
        String component = new String(componentBuffer).trim();
        char[] pathBuffer = new char[WinDef.MAX_PATH];
        IntByReference pathBufferSize = new IntByReference(pathBuffer.length);
        try{
            Msi.INSTANCE.MsiLocateComponent(component, pathBuffer, pathBufferSize);
        }catch(UnsatisfiedLinkError ule){
            throw new DetailedException("Failed to load the Msi library used to locate an installed instance of Microsoft PowerPoint.", 
                    "An exception was thrown while trying to retrieve the installation path of a Microsoft PowerPoint version with the static GUID of "+guid+".\n"
                            + "\n\nThis has been known to happen on Mac OS when you are running a GIFT course that uses Microsoft PowerPoint.  Please try a Windows computer with PowerPoint installed to run this course.\n\n"
                            + "The error reads: "+ule.getMessage(), ule);
        }
        
        return new String(pathBuffer).trim();
    }
    
    /**
     * Read the Windows registry at the location specified and return the value of the "Path" key.</br>
     * If the 'Path' key is not there an attempt is made at reading the default registry value.
     * 
     * @param location path in the registry
     * (e.g. HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Office\\ClickToRun\\REGISTRY\\MACHINE\\Software\\Microsoft\\Windows\\CurrentVersion\\App Paths\\powerpnt.exe)
     * @return registry value or null if not found
     */
    private static final String readRegistry(String location){

        String[] pathCommand = {"reg", "query", '"'+ location + "\"", "/v", "Path"};
        
        String pathResult = readRegistry(location, pathCommand);
        if(pathResult == null){
            String[] command = {"reg", "query", '"'+ location + "\""};
            pathResult = readRegistry(location, command);
        }
        
        return pathResult;            
    }
    
    /**
     * Read the Windows registry at the location specified and return the value of the "Path" key.
     * 
     * @param location path in the registry
     * (e.g. HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Office\\ClickToRun\\REGISTRY\\MACHINE\\Software\\Microsoft\\Windows\\CurrentVersion\\App Paths\\powerpnt.exe)
     * @param command the registry query command to execute (e.g. reg query "HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Office\\ClickToRun\\REGISTRY\\MACHINE\\Software\\Microsoft\\Windows\\CurrentVersion\\App Paths\\powerpnt.exe")
     * @return registry value or null if not found
     */
    private static final String readRegistry(String location, String[] command){
        
        try {    
            
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.redirectErrorStream(true);
            Process process = builder.start();          
            
            // wait until command is done
            process.waitFor();
            
            InputStream inputStream = process.getInputStream();
            BufferedReader reader = new BufferedReader (new InputStreamReader(inputStream));
            String line;
            StringBuffer sb = new StringBuffer();
            while ((line = reader.readLine ()) != null) {
                sb.append(line);
            }
          
            String output = sb.toString();

            // Output has the following format:
            // \n<Version information>\n    <key>    <registry type>    <value>
            if(!output.contains(REG_SZ)){
                
                if(logger.isDebugEnabled()){
                    logger.debug("The output for location '"+location+"' doesn't contain the key '"+REG_SZ+"', therefore trying to retrieve the Windows registry value.\noutput = '"+output+"'.");
                }
                    
            	/* if the output does not contain what we're looking for, GIFT may be getting redirected to an invalid 
            	 * location in the registry by a 64-bit Windows installation because, by default, GIFT uses a 32-bit JRE. In
            	 * this case, use WindowsRegistryUtil to look at the 64-bit registry explicitly. */
            	
            	String powerPointDirectoryPath = null;
            	
            	if(location.contains("HKEY_LOCAL_MACHINE\\")){
            		powerPointDirectoryPath = WindowsRegistryUtil.readString(WindowsRegistryUtil.HKEY_LOCAL_MACHINE, location.replace("HKEY_LOCAL_MACHINE\\", ""), "Path", WindowsRegistryUtil.KEY_WOW64_64KEY);
            	
            	}else if(location.contains("HKEY_CURRENT_USER\\")){
            		output = WindowsRegistryUtil.readString(WindowsRegistryUtil.HKEY_CURRENT_USER, location.replace("HKEY_CURRENT_USER\\", ""), "Path", WindowsRegistryUtil.KEY_WOW64_64KEY);
            	}
            	            	
            	if(powerPointDirectoryPath != null){
            	    
                    if(logger.isDebugEnabled()){
                        logger.debug("After checking the registry value, the PowerPoint directory path to check for a PowerPoint exe for the location '"+location+" is '"+powerPointDirectoryPath+"'.");
                    }
            		
            		//if a path to the PowerPoint directory is found, make sure powerpnt.exe exists
	            	File powerPointFile = new File(powerPointDirectoryPath + "powerpnt.exe");
	            	
	            	if(powerPointFile.exists()){
	                	return powerPointDirectoryPath + "powerpnt.exe";
	                }           
            	}
               
                return null;
            }else{
                
                if(logger.isDebugEnabled()){
                    logger.debug("The output for location '"+location+"' is a registry entry.  Checking if the value is a path that results in finding a PowerPoint exe.\noutput = '"+output+"'.");
                }
                
                //MH (#781): found that when running the Java Web Start application for a server hosted GIFT course,
                //    the registry query might return the following when running 64x Java 8 with 2013 PowerPoint C2R install:
                //    HKEY_LOCAL_MACHINE\SOFTWARE\Microsoft\Office\15.0\ClickToRun\REGISTRY\MACHINE\Software\Microsoft\Windows\CurrentVersion\App Paths\powerpnt.exe    Path    REG_SZ    C:\Program Files\Microsoft Office 15\Root\Office15\
                //    which contains the REG_SZ string and therefore the if statement above doesn't get executed.
                //    This may have been caused by my Windows not having an updated Java 8 version as I get a update warning when running the JWS app
                
                String powerPointDirectoryPath = null;
                
                //if a path to the PowerPoint directory is found, make sure powerpnt.exe exists
                if(location.contains("HKEY_LOCAL_MACHINE\\")){
                    powerPointDirectoryPath = WindowsRegistryUtil.readString(WindowsRegistryUtil.HKEY_LOCAL_MACHINE, location.replace("HKEY_LOCAL_MACHINE\\", ""), "Path", WindowsRegistryUtil.KEY_WOW64_64KEY);
                
                }else if(location.contains("HKEY_CURRENT_USER\\")){
                    output = WindowsRegistryUtil.readString(WindowsRegistryUtil.HKEY_CURRENT_USER, location.replace("HKEY_CURRENT_USER\\", ""), "Path", WindowsRegistryUtil.KEY_WOW64_64KEY);
                }                
                
                if(powerPointDirectoryPath != null){
                    
                    if(logger.isDebugEnabled()){
                        logger.debug("After checking the registry value, the PowerPoint directory path to check for a PowerPoint exe for the location '"+location+" is '"+powerPointDirectoryPath+"'.");
                    }
                    
                    //if a path to the PowerPoint directory is found, make sure powerpnt.exe exists
                    File powerPointFile = new File(powerPointDirectoryPath + "powerpnt.exe");
                    
                    if(powerPointFile.exists()){
                        return powerPointDirectoryPath + "powerpnt.exe";
                    }           
                }
            }
            
            if(logger.isDebugEnabled()){
                logger.debug("Registry output for location of '"+location+"' is '"+output+"'.");
            }
            String value = output.substring(output.indexOf(REG_SZ) + 6).trim();
            if(logger.isDebugEnabled()){
                logger.debug("Returning trimmed registry output ofr '"+value+"'.");
            }
            return value;
          
        }catch (Exception e) {
            logger.error("When searching for a Microsoft Office installation an exception was caught while trying to read the registry location of "+location+".", e);
            return null;
        }

    }
    
    /**
     * Determines if a PowerPoint library exists on this machine.  
     * If it doesn't it attempts to find an installed GIFT supported version of PowerPoint.
     * The found library is returned.
     * 
     * @return String the PowerPoint type library file name found on this machine. Will not be null and will exist on this machine.
     * @throws IOException if there was a problem searching for an installed PowerPoint on this machine
     * @throws ConfigurationException if PowerPoint was not found on this machine
     * @throws Exception catch all for other critical exceptions that could be thrown by this method
     */
    public static String selectPowerPoint() throws IOException, ConfigurationException, Exception{
        
        String pptTypeLibrary = UserEnvironmentUtil.getEnvironmentVariable(TrainingApplicationInstallPage.PPT_HOME);
        if(pptTypeLibrary != null && new File(pptTypeLibrary).exists()){
            //found PowerPoint library, nothing to do
            return pptTypeLibrary;
        }
        
        if(logger.isInfoEnabled()){
            logger.info("The PowerPoint type library of "+pptTypeLibrary+" doesn't exist on this computer.  Attempting to find PowerPoint on this machine...");
        }
        
        //First:  try to find an existing PPT install.
        Map<String, String> installs = findSupportedPowerPointVersions();
        
        //Second: 
        //         if zero found then error
        //         if more than one found then present selection dialog
        
        if(installs.isEmpty()){   
            
            if (GatewayModuleProperties.getInstance().isRemoteMode()) {
                //allow the gateway installer to present information to the user
                if(logger.isInfoEnabled()){
                    logger.info("The collection of PowerPoint type libary installs is empty.");
                }
                return null;
            }else{
                //allow the gateway module to present a dialog to the user with yes/no option
                throw new ConfigurationException("Unable to find PowerPoint installed on your computer.",
                        "Would you like to continue by disabling the PowerPoint interface?\n"+
                        "(this automatically updates '"+GatewayModuleProperties.getInstance().getInteropConfig()+"' and requires you to use the GIFT installer to enable it in the future)\n\n" +
                        "Otherwise install a supported version of the PowerPoint program and configure GIFT by using the GIFT installer.",
                        null);
            }

        }else{
            
            String pptAppFileName = null;
            
            if(installs.size() == 1){
                
                pptAppFileName = (String) installs.values().toArray()[0];                    
                
                if(logger.isInfoEnabled()){
                    logger.info("Found a different PowerPoint installation on this computer at "+pptAppFileName+".");
                }
                
            }else if(installs.size() > 1){
                
                if(logger.isInfoEnabled()){
                    logger.info("Found "+installs.size()+" PowerPoint applications installed, therefore presenting selection dialog to user.");
                }
                
                SelectPPTApplication selectDialog = new SelectPPTApplication(installs);
                selectDialog.setVisible(true);
                
                try{
                    //wait for selection to happen
                    synchronized (selectDialog) {
                        selectDialog.wait();
                    }
                }catch(Exception e){
                    throw new ConfigurationException("Failed to wait for user to select which PowerPoint application to use.",
                            "Something interrupted the proces that was waiting on the user to select the PowerPoint version to use.",
                            e);
                }
                
                String selectedValue = selectDialog.getSelection();
                pptAppFileName = installs.get(selectedValue);
                
                if(logger.isInfoEnabled()){
                    logger.info("The user selected to use the PowerPoint application named "+selectedValue+" which is mapped to the PPT Application of "+pptAppFileName+".");
                }
                
            }
            
            //get the OLB file in the PPT App file directory
            pptTypeLibrary = getPowerPointOLBPath(pptAppFileName);
            if(pptTypeLibrary == null) {
            	throw new ConfigurationException("Unable to determine the appropriate OLB file to use.",
                        "There was no OLB file found in the same directory as "+pptAppFileName,  null);
            }
        }
        
        return pptTypeLibrary;
    }
    
    /**
     * Retrieves the PowerPoint OLB file path.
     * 
     * @param pptExeFilePath The path to the PowerPoint.exe file.  If null, empty or doesn't exist, null will be returned.
     * @return a path to the PowerPoint OLB file or null if it was not found.
     */
    public static String getPowerPointOLBPath(String pptExeFilePath) {
        
        if(pptExeFilePath == null || pptExeFilePath.isEmpty()){
            return null;
        }
    	
    	//get the OLB file in the PPT App file directory
        File pptAppFile = new File(pptExeFilePath);
        
        if(!pptAppFile.exists()){
            logger.warn("The powerpoint exe path of '"+pptExeFilePath+"' doesn't exist.");
            return null;
        }
        
        try {
	    	File[] files = pptAppFile.getParentFile().listFiles(new FilenameFilter() {
	            
	            @Override
	            public boolean accept(File dir, String name) {
	                return name.equalsIgnoreCase(MSPPT_OLB);
	            }
	        });
	        
	        if(files.length == 1){
	        	return files[0].getCanonicalPath();
	        } else {
	        	logger.error("Cannot determine the OLB file path to use because more "
	        			+ "than one PowerPoint OLB file was found at: " + pptExeFilePath);
	        }
	        
        } catch (Exception e) {
        	logger.error("Caught exception while attempting to get the path to the PowerPoint OLB file from '"+pptExeFilePath+"'.", e);
        }
        
        return null;
    }
    
    /**
     * Returns a collection of file names for the PowerPoint application(s) installed on this machine
     * and removes any invalid paths. Example: "C:\Program Files (x86)\Microsoft Office\Office14\MSPPT.OLB" 
     * for Powerpoint 2010 32bit installed on Windows 7 64bit.  
     * 
     * @return Map the found applications {key: application name, value: path to application}. Note: the 
     * collection is empty if PowerPoint was not found on this machine.
     * 
     * @throws IOException if there was a problem searching for the PPT applications
     * @throws DetailedException if there was a problem loading the Msi library used to locate an installed instance of
     * Microsoft PowerPoint.
     */
    public static Map<String, String> findSupportedPowerPointVersions() throws IOException, DetailedException {
    	
    	Map<String, String> installs = findPowerPoint();
		stripInvalidInstalls(installs);
		
		for(String key : installs.keySet()) {
			installs.put(key, getPowerPointOLBPath(installs.get(key)));
		}
		
    	return installs;
    }
    
    /**
     * Searches the user's Program Files folder for PowerPoint installations.
     * 
     * @return The first PowerPoint installation path found or null if nothing was found.
     */
    public static String findUnsupportedPowerPointVersion() {
    	
    	String win32 = " (x86)";
		ArrayList<File> programFiles = new ArrayList<File>();
		String programFilesPath = System.getenv("ProgramFiles");
		
		// Search both "Program Files" & "Program Files (x86)"
		programFiles.add(new File(programFilesPath));
		if(programFilesPath.endsWith(win32)) {
			programFiles.add(new File(programFilesPath.replace(win32, "")));
		}
		
		for(File programFilesDir : programFiles) {
			
			if(logger.isDebugEnabled()){
			    logger.debug("Searching for Microsoft Office folder in " + programFilesDir.getPath());
			}
			
			/* Locate the PowerPoint executable and .OLB file. The path should look something like:
			 * C:\Program Files\Microsoft Office ##\root\office## or
			 * C:\Program Files\Microsoft Office ##\Office##
			 * The filenames may contain version numbers as well, for example msppt9.olb
			 * It is also possible for filenames to be in upper case.
			 */
			
			// Find the Microsoft Office folder
			File[] msFolders = programFilesDir.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File file, String name) {				
					return name.toUpperCase().startsWith("MICROSOFT OFFICE");
				}
			});

			for(File folder : msFolders) {
				// Search the root folder if it exists 
				File rootFolder = new File(folder.getPath() + File.separator + "root");
				if(!rootFolder.exists()) {
					rootFolder = folder;
				}
				
				if(logger.isDebugEnabled()){
				    logger.debug("Searching for Office folder in " + rootFolder.getPath());
				}
				
				// Find the office folder
				File[] folders = rootFolder.listFiles(new FilenameFilter() {

					@Override
					public boolean accept(File dir, String name) {
						return name.toUpperCase().startsWith("OFFICE");
					}

				});

				if(folders.length > 0) {
					File officeFolder = folders[0];
					// Find the PowerPoint files
					File[] pptFiles = officeFolder.listFiles(new FilenameFilter() {

						@Override
						public boolean accept(File dir, String name) {
							if(logger.isDebugEnabled()){
							    logger.debug("Checking if file " + name + " is a PowerPoint .exe or .olb file.");
							}
							return (isPowerPointExecutable(name) || isPowerPointOlb(name));
						}

					});

					if(pptFiles.length == 2) {
						String file = isPowerPointOlb(pptFiles[0].getName()) ? pptFiles[0].getPath() : pptFiles[1].getPath();
						if(logger.isInfoEnabled()){
						    logger.info("Found PowerPoint olb file at: " + file);
						}
						return file; 
					}

				}

			}
		}    	
    	
    	return null;
    }
    
    /**
     * Checks if the given filename represents a PowerPoint executable file.
     * 
     * @param filename The filename to check.
     * @return true if the filename matches a PowerPoint executable file. False otherwise.
     */
    public static boolean isPowerPointExecutable(String filename) {
    	// Account for filenames such as POWERPNT.EXE or powerpnt9.exe
    	return (filename.toUpperCase().startsWith("POWERPNT") && filename.toUpperCase().endsWith(".EXE"));
    }
    
    /**
     * Checks if the given filename represents a PowerPoint OLB file.
     * 
     * @param filename The filename to check.
     * @return true if the filename matches a PowerPoint OLB file. False otherwise.
     */
    public static boolean isPowerPointOlb(String filename) {
    	// Account for filenames such as MSPPT.OLB or msppt9.olb
    	return (filename.toUpperCase().startsWith("MSPPT")  && filename.toUpperCase().endsWith(".OLB"));
    }
        
    /**
     * Gets the path of a PowerPoint executable file.
     * 
     * @param powerPointOlbPath The path to the PowerPoint OLB file
     * @return the path to the PowerPoint executable file. Returns null if no file was found or if an error occurred.
     */
    public static String getPowerPointExePath(String powerPointOlbPath){
    	
        if(logger.isInfoEnabled()){
            logger.info("Searching for PowerPoint .exe file using the .olb file at: " + powerPointOlbPath);
        }
    	
    	try {
    		File officeDir = new File(powerPointOlbPath).getParentFile();
    		File[] exeFiles = officeDir.listFiles(new FilenameFilter() {

				@Override
				public boolean accept(File dir, String name) {
					if(logger.isDebugEnabled()){
					    logger.debug("Checking if file " + name + " is a PowerPoint .exe file.");
					}
					return (isPowerPointExecutable(name));
				}

			});

			if(exeFiles.length == 1) {
			    if(logger.isInfoEnabled()){
			        logger.info("Found PowerPoint.exe at: " +  exeFiles[0].getPath());
			    }
				return exeFiles[0].getPath();
			}
    		
    	} catch (Exception e) {
    		logger.error("Caught exception while searching for PowerPoint .exe file.", e);
    	}
    	
    	return null;
    }
    
    /**
     * Takes in the map of available install paths and makes sure it has the correct powerpnt.exe
     * Any install paths without the exe file will be removed from the list to avoid GIFT installer
     * allowing user to select that version of PP but not being able to actually find it when needed. 
     * 
     * @param installs - list of install paths, any paths that do not contain Powerpnt.exe will be removed.
     */
    private static void stripInvalidInstalls(Map<String, String> installs) {
    	Iterator<Map.Entry<String,String>> iter = installs.entrySet().iterator();
    	if(logger.isDebugEnabled()){
    	    logger.debug("Checking "+installs.size()+" PowerPoint type libary instances found on this computer.");
    	}
        while (iter.hasNext()) {
            Map.Entry<String,String> entry = iter.next();
            File file = new File(entry.getValue());
            
            String filePath;
            if(file.isDirectory()){
                //check this directory
                filePath = file.getAbsolutePath();
            }else{
                //check the parent directory of this file
                filePath = file.getParent();
            }
        	
            boolean check = new File(filePath, MSPPT_OLB).exists();
            if(!check){
                if(logger.isDebugEnabled()){
                    logger.debug("Removing "+entry+" from the list of potential type libraries because the file doesn't exist on this computer.");
                }
            	iter.remove();
            }
        }		
	}

	/**
     * Used to test the logic in this class.
     * 
     * @param args - not used
     */
    @SuppressWarnings("unused")
    public static void main(String[] args) {

        String PowerPoint2010 = "{E72E0D20-0D63-438B-BC71-92AB9F9E8B54}";
        String Office2010 = "{398E906A-826B-48DD-9791-549C649CACE5}";
        String PowerPoint2013_32x="{813139AD-6DAB-4DDD-8C6D-0CA30D073B41}";
        
        String path = getPathToApplication(PowerPoint2010);
        
        if(path == null){
            //Testing C2R install
            String location = "HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Office\\15.0\\ClickToRun\\REGISTRY\\MACHINE\\Software\\Microsoft\\Windows\\CurrentVersion\\App Paths\\powerpnt.exe";
            path = readRegistry(location);
        }
        
        System.out.println("specific search found: "+path+".\n");
        
        try {
            Map<String, String> ppts = findPowerPoint();
            stripInvalidInstalls(ppts);
            System.out.println("brute force search found:");
            for(String appName : ppts.keySet()){
                System.out.println(appName + " - " + ppts.get(appName));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        System.out.println("\nEND.");
    }
    
    /**
     * This inner class creates a dialog with a list of item to select from.
     * 
     * @author mhoffman
     *
     */
    private static class SelectPPTApplication extends JFrame{
        
        private static final long serialVersionUID = 1L;
        
        private JList<String> jList;
        
        /**
         * Class constructor - build GUI components
         * 
         * @param entries collection of entries to use to populate the selection list
         */
        public SelectPPTApplication(Map<String, String> entries){
            
            if(entries == null || entries.isEmpty()){
                throw new IllegalArgumentException("The entries must contain at least one item.");
            }
            
            initComponents(entries);
        }
        
        /**
         * Return the item selected.
         * 
         * @return String the value of the item selected
         */
        public String getSelection(){
            return jList.getSelectedValue();
        }
        
        /**
         * The ok button was selected.  Close this dialog.
         */
        private void okButtonActionPerformed(){
            this.setVisible(false);
            this.dispose();
            
            //wake up anyone waiting for the selection to happen
            synchronized(this){
                this.notifyAll();
            }
        }
        
        private void initComponents(Map<String, String> entries){
            
            this.setTitle("Select PowerPoint To Use");
            
            JScrollPane scrollPane = new javax.swing.JScrollPane();
            jList = new javax.swing.JList<>();
            JLabel jLabel = new javax.swing.JLabel();
            JButton okButton = new javax.swing.JButton();

            setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

            DefaultListModel<String> model = new DefaultListModel<>();            
            for(String name : entries.keySet()){
                model.addElement(name);
            }
            
            jList.setModel(model);
            jList.setSelectedIndex(0);
            scrollPane.setViewportView(jList);

            jLabel.setText("Select Installed PowerPoint Application");

            okButton.setText("OK");
            okButton.setActionCommand("OK");
            okButton.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    okButtonActionPerformed();
                }
            });

            javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
            getContentPane().setLayout(layout);
            layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(scrollPane)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(jLabel)
                            .addGap(0, 105, Short.MAX_VALUE))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                            .addGap(0, 0, Short.MAX_VALUE)
                            .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addContainerGap())
            );
            layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(19, 19, 19)
                    .addComponent(scrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(18, 18, 18)
                    .addComponent(okButton)
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            );           

            pack();
            
            //center on screen
            Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
            setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
        }
    }

}
