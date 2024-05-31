/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.installer;

import java.io.File;

import mil.arl.gift.common.io.CommonProperties;

public class InstallerProperties extends CommonProperties {

	/** The properties file name */
	private static final String PROPERTIES_FILE="tools"+File.separator+"install"+File.separator+"install.properties";
	
    /** Singleton instance of this class */
	private static InstallerProperties instance = null;
	
	/** Installer specific property names */
	private static final String WINPYTHON_URL = "WinPythonURL";
	private static final String WINPYTHON_EXE = "WinPythonExe";
	
	/** the Unity Land Nav download URL */
	private static final String UNITY_LAND_NAV_BUILD_OUTPUT_URL = "UnityLandNavBuildOutputURL";
	
	/** is using default installer settings */
	private static final String USE_DEFAULT_SETTINGS = "UseDefaultSettings";
	
	/** Class constructor */
	private InstallerProperties() {
		super(PROPERTIES_FILE);
	}
		
	/** 
	 * Returns the singleton instance of this class
	 * @return InstallerProperties 
	 */
	public static synchronized InstallerProperties getInstance() {
		
		if (instance == null) {
			instance = new InstallerProperties();
		}
		
		return instance;
	}
	
	/** 
	 * Returns the WinPython URL
	 * 
	 * @return String
	 */
	public String getWinPythonURL() {
		return getPropertyValue(WINPYTHON_URL);
	}
	
	/** 
	 * Returns the WinPython installer name
	 * 
	 * @return String
	 */
	public String getWinPythonExe() {
		return getPropertyValue(WINPYTHON_EXE);
	}
	
	/**
	 * Return the unity land nav build output URL property value
	 * 
	 * @return a hosted URL value that can be used to download the Unity Land Nav Build file.  Can 
	 * be null or empty string if not set in the properties file.
	 */
	public String getUnityLandNavDownloadURL(){
	    return getPropertyValue(UNITY_LAND_NAV_BUILD_OUTPUT_URL);
	}
	
	/**
	 * Returns whether default settings should be used for the installer in place of prompting user input for each setting
	 * @return Default is false.
	 */
	public boolean getUseDefaultSettings() {
		return getPropertyBooleanValue(USE_DEFAULT_SETTINGS);
	}
}
